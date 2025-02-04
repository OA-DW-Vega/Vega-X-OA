package com.olam.warehouse.vegax.grnecuador.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnecuador.R
import com.olam.warehouse.vegax.grnecuador.databinding.FragmentVegaEcuadorGrnDetailsBinding
import com.olam.warehouse.vegax.grnecuador.utils.*
import kotlinx.android.synthetic.main.fragment_vega_ecuador_grn_details.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 6/30/2020.
 */
class VegaEcuadorGrnDetailsFragment : BaseFragment() {
    private var paidWeight: Double = 0.0
    private var mAlertDialog: AlertDialog? = null
    private val vm: VegaEcuadorGrnViewModel by viewModel()
    private var weighBridgeId = VegaGrnWeighBridgeId()
    private var wbDetails = VegaGrnWeighBridgeId()
    private var currentMaterial: String = ""
    private var defaultStorageLocation: String = ""

    private lateinit var binding: FragmentVegaEcuadorGrnDetailsBinding
    override val layoutResourceId = R.layout.fragment_vega_ecuador_grn_details

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaEcuadorGrnDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("approveecuador/ui/details/VegaEcuadorGrnDetailsFragment").title("GRN Ecuador")
            .with(tracker)
        initUI()
    }

    companion object {
        fun newInstance(grnData: VegaGrnWeighBridgeId) = VegaEcuadorGrnDetailsFragment().putArgs {
            putParcelable(GRN_DATA, grnData)
        }
    }

    private fun initUI() {
        wbDetails = arguments?.getParcelable(GRN_DATA)!!
        if (wbDetails.unitPrice!!.isNotEmpty() && !wbDetails.unitPrice.equals("0.00")) {
            binding.etPrice.setText(wbDetails.unitPrice.toString())
            binding.etPrice.isEnabled = false
            binding.tvTotalValue.text = wbDetails.totalPrice
        }
        currentMaterial = wbDetails.materialCode.toString()
        updateUIValues()
        vm.grn.observe(viewLifecycleOwner, Observer { updateGrnUI(it) })

        binding.tvUsdLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.final_price_per_unit)) { mandatoryStars() } }
        binding.btnProceed.setOnClickListener {
            if (wbDetails.purchaseDocNum.isNullOrEmpty()) {
                wbDetails.unitPrice = binding.etPrice.text.toString()
                when {
                    binding.etPrice.text.isNullOrEmpty() -> showSnack(getString(R.string.price_validation))
                    else -> showConfirmDialog()
                }
            } else showConfirmDialog()
        }
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.getConfigItems(UserRoles.GRN.role)
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>) {
        val defaultStorageLoc = configItems.filter { it.process.equals(ConfigItems.DEFAULT_STORAGE_LOC.item) }
        var isExist = false
        val isMaterial = defaultStorageLoc.map { it.materialCode }.contains(currentMaterial.removeRange(0, 6))
        defaultStorageLoc.forEach {
            if (currentMaterial.contains(it.materialCode) && !it.materialCode.isEmpty() && !isExist && isMaterial) {
                if (it.applicable?.contains("Y")!!) {
                    defaultStorageLocation = it.value.toString()
                    isExist = true
                }
            } else if (it.materialCode.isEmpty() && !isExist && !isMaterial) {
                if (it.applicable?.contains("Y")!!) {
                    defaultStorageLocation = it.value.toString()
                    isExist = true
                }
            }
        }
        vm.storageLocation.observe(viewLifecycleOwner, Observer {
            tvStorageLocation.text = it?.storageLocationName
        })
        vm.fetchStorageLocation(defaultStorageLocation)
    }

    private fun updateGrnUI(data: Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    mAlertDialog?.dismiss()
                    moveToSuccessPage(it.data?.data?.grnNumber.toString())
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

    private fun updateUIValues() {
        binding.tvParamsWeighBID.text = wbDetails.weighBridgeId
        binding.tvTruckSupplier.text = wbDetails.supplierName
        binding.tvNumberOfBags.text = wbDetails.bagCount
        binding.tvNetWeight.text = wbDetails.netWeight.plus(" ").plus(wbDetails.unitsOfMeasure)
        paidWeight = wbDetails.netWeight.toDouble()
        binding.tvPaidWeight.text = wbDetails.netWeight.plus(" ").plus(wbDetails.unitsOfMeasure)
        //paidWeight.formatThreeDigits().replace(",", "").plus(" ").plus(wbDetails.unitsOfMeasure)
        //binding.tvStorageLocation.text = defaultStorageLocation
        if (wbDetails.purchaseDocNum.isNullOrEmpty()) {
            binding.grnDetailsTitle.text = getString(R.string.grn).plus(" - ").plus(SPOT)
            binding.tvProcurementType.text = getString(R.string.spot_purchase)
            binding.etPrice.onChange {
                try {
                    val paidData = paidWeight.formatThreeDigits().replace(",", "")
                    val totalVal = it.toDouble() * paidData.toDouble()
                    binding.tvTotalValue.text = totalVal.formatThreeDigits()
                    wbDetails.totalPrice = totalVal.formatThreeDigits()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            binding.grnDetailsTitle.text = getString(R.string.grn).plus(" - ").plus(FIXED)
            binding.tvProcurementType.text = getString(R.string.fixed_purchase)
            binding.tvPoNumber.text = wbDetails.purchaseDocNum
            binding.llPoNumber.visible()
            binding.llPriceDetails.gone()
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
                    val wbData = preparePostGrnData(wbDetails)
                    if (isOnline()) {
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
        moveToSuccessPage(wbDetails.weighBridgeId.toString())
    }

    private fun moveToSuccessPage(grn: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (isOnline()) {
            intent.putExtra(AppUtils.TITLE, getString(R.string.grn_success))
            // vm.updateDeletedItem(wbDetails.weighBridgeId.toString())
        } else
            intent.putExtra(AppUtils.TITLE, getString(R.string.grn_success_offline))
        intent.putExtra(AppUtils.SUB_TITLE, grn)
        startActivity(intent)
    }

    private fun moveToFailurePage(wbId: String, msg: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.grn_fail_msg))
        intent.putExtra(AppUtils.SUB_TITLE, msg.plus("\n").plus(wbId))
        intent.putExtra(AppUtils.FAILURE, false)
        startActivity(intent)
    }
}
