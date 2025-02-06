package com.olam.warehouse.vegax.grnecuador.ui.weighbridge

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.model.*
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnecuador.R
import com.olam.warehouse.vegax.grnecuador.databinding.FragmentEcuadorGrnSummaryBinding
import com.olam.warehouse.vegax.grnecuador.ui.VegaEcuadorGrnViewModel
import com.olam.warehouse.vegax.grnecuador.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

/**
 * Created by Roshna Parambil on 1/13/2022.
 */
class VegaEcuadorGrnSummaryFragment : BaseFragment() {

    private var wbDetails = VegaGrnWeighBridgeId()
    private var postData = mutableListOf<VegaGrnWeighBridgeId>()
    private var grnPrice: String = ""
    private var mAlertDialog: AlertDialog? = null
    private val vm: VegaEcuadorGrnViewModel by viewModel()
    private lateinit var binding: FragmentEcuadorGrnSummaryBinding
    override val layoutResourceId = R.layout.fragment_ecuador_grn_summary
    private var materialObj = VegaMaterial()


    companion object {
        fun newInstance(
            wbDetails: VegaGrnWeighBridgeId, grnPrice: String
        ) = VegaEcuadorGrnSummaryFragment().putArgs {
            putParcelable(GRN_DATA, wbDetails)
            putString(GRN_PRICE, grnPrice)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentEcuadorGrnSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("gateentryNigeria/ui/VegaGateEntrySummaryFragment")
            .title("Gate Entry")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        wbDetails = arguments?.getParcelable(GRN_DATA)!!
        grnPrice = arguments?.getString(GRN_PRICE)!!
        if(wbDetails.materialCode?.isNotEmpty() == true){
            wbDetails.materialCode?.takeLast(12)?.let { vm.getSAPMaterialUsingMaterialCode(it) }
            vm.sapMaterialUsingMaterialCode.observe(viewLifecycleOwner, Observer{
                it?.let {
                    materialObj = it
                }
            })
        }
        binding.tvMaterialValue.text =
            wbDetails.materialCode.plus("-").plus(wbDetails.materialName)
        binding.tvSupplier.text = wbDetails.supplierName
        if (wbDetails.purchaseType.equals(FIXED))
            binding.tvProcurementTypeValue.text = "Fixed"
        else
            binding.tvProcurementTypeValue.text = wbDetails.purchaseType

        binding.tvNoOfBags.text = wbDetails.bagCount
//            wbDetails.plantId.plus("-").plus(wbDetails.plantName)
        binding.tvNetWeightValue.text = wbDetails.netWeight.plus(" KG")
        binding.tvGRNPriceValue.text = wbDetails.grossWeight.plus(" USD")
        binding.tvStorageLocationValue.text = wbDetails.storageLocationCode


        binding.btnConfirm.setOnClickListener { showConfirmDialog() }

        vm.grn.observe(viewLifecycleOwner, Observer { updateGrnUI(it) })

    }

    private fun updateGrnUI(data: Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    mAlertDialog?.dismiss()
                    moveToSuccessPage(it.data?.data?.grnNumber.toString(), it.data?.data?.batchNumber.toString())
                    vm.updateGrnNoToQuality(
                        wbDetails.weighBridgeId.toString(),
                        it.data?.data?.grnNumber.toString(),
                        it.data?.data?.batchNumber.toString()
                    )
                    vm.updateGrnSuccess(
                        wbDetails.weighBridgeId.toString(),
                        it.data?.data?.grnNumber.toString(),
                        it.data?.data?.batchNumber.toString(),
                        getString(R.string.grn_success),
                        4
                    )
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_grn)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(R.string.cancel),
                {
                    //wbDetails.storageLocationCode = binding.tvStorageLocation.text.toString()
                    val wbData = preparePostGrnWbData(wbDetails)
                    if (AppUtils.isOnline()) {
                        vm.postGrn(
                            VegaEcuadorGrnPost(
                                key = getCurrentKey(),
                                plant = getPlantDetails(),
                                grnData = listOf(wbData)
                            )
                        )
                    } else {
                        saveData()
                    }
                },
                { dismiss() })
        }
    }

    private fun saveData() {
        wbDetails.isOfflineData = true
        if (!wbDetails.wbTempId.contains("TMP")) {
            wbDetails.isNotWBID = true
            wbDetails.grnNumber = getTmpId()
        }
        vm.updateGRNPrice(wbDetails)
        moveToSuccessPage(wbDetails.weighBridgeId.toString(),"")
    }

    private fun moveToSuccessPage(grn: String, batchNumber:String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) {
            intent.putExtra(AppUtils.TITLE, getString(R.string.grn_success))
            intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.batch_number).plus(batchNumber))
            // vm.updateDeletedItem(wbDetails.weighBridgeId.toString())
        } else
            intent.putExtra(AppUtils.TITLE, getString(R.string.grn_success_offline))
        intent.putExtra(AppUtils.SUB_TITLE, grn)
        intent.putExtra(AppUtils.EUDR_STATUS, if(materialObj?.complainceFlag?.equals("Compliant")==true) "1" else "0" )
        startActivity(intent)
    }

    private fun moveToFailurePage(wbId: String, msg: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.grn_fail_msg))
        intent.putExtra(AppUtils.SUB_TITLE, msg.plus("\n").plus(wbId))
        intent.putExtra(AppUtils.FAILURE, false)
        intent.putExtra(AppUtils.MSG, msg)
        startActivity(intent)
    }
}

