package com.olam.warehouse.vegax.bcapproveecuador.ui

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
import com.olam.warehouse.master.vegacameroon.model.QualityDetails
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorBcApproveWBDetails
import com.olam.warehouse.master.vegaecuador.model.*
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
import com.olam.warehouse.vegax.bcapproveecuador.R
import com.olam.warehouse.vegax.bcapproveecuador.data.domain.model.VegaEcuadorApproveWbDetail
import com.olam.warehouse.vegax.bcapproveecuador.databinding.FragmentEcuadorApproveWbSummaryBinding
import com.olam.warehouse.vegax.bcapproveecuador.ui.VegaEcuadorBcApproveViewModel
import com.olam.warehouse.vegax.bcapproveecuador.utils.*
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
class VegaEcuadorApproveWBSummaryFragment : BaseFragment() {

    private var wbDetails = VegaEcuadorBcApproveWBDetails()
    private var approveQualityList = ArrayList<VegaEcuadorApproveWbDetail>()
    private var postData = mutableListOf<VegaGrnWeighBridgeId>()
    private var grnPrice: String = ""
    private var mAlertDialog: AlertDialog? = null
    private val vm: VegaEcuadorBcApproveViewModel by viewModel()
    private lateinit var binding: FragmentEcuadorApproveWbSummaryBinding
    override val layoutResourceId = R.layout.fragment_ecuador_approve_wb_summary

    companion object {
        fun newInstance(
            approveQualityList: ArrayList<VegaEcuadorApproveWbDetail>,
            wbDetails: VegaEcuadorBcApproveWBDetails
        ) = VegaEcuadorApproveWBSummaryFragment().putArgs {
            putParcelableArrayList(BC_QUALITY_DETAILS, approveQualityList)
            putParcelable(BC_WB_DETAILS, wbDetails)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentEcuadorApproveWbSummaryBinding.inflate(layoutInflater)
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
        wbDetails = arguments?.getParcelable(BC_WB_DETAILS)!!
        approveQualityList = arguments?.getParcelableArrayList(BC_QUALITY_DETAILS)!!

        binding.tvMaterialValue.text =
            wbDetails.materialNumber.plus("-").plus(wbDetails.materialName)
        binding.tvSupplier.text = wbDetails.supplierName
        binding.tvGrnNumber.text = wbDetails.grnNumber
        binding.tvGRNPriceValue.text = wbDetails.grnNumber
//        if (wbDetails.purchaseType.equals())
//            binding.tvProcurementTypeValue.text = "Fixed"
//        else
//            binding.tvProcurementTypeValue.text = wbDetails.purchaseType

//        binding.tvNoOfBags.text = wbDetails.b
//            wbDetails.plantId.plus("-").plus(wbDetails.plantName)
//        binding.tvNetWeightValue.text = approveQualityList[0].n.plus(" KG")
//        binding.tvGRNPriceValue.text = wbDetails.grossWeight.plus(" USD")
//        binding.tvStorageLocationValue.text = wbDetails.storageLocationCode


        binding.btnConfirm.setOnClickListener {

            showConfirmDialog()
        }

//        vm.grn.observe(viewLifecycleOwner, Observer { updateGrnUI(it) })

    }

    private fun updateUI(data: Resource<GenericReqAndResp<VegaEcuadorBcApproveResponse>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()

                    if (data.data?.success!!) {
                        moveToSuccessPage()
                    }

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
            message(R.string.confirm_dispatch)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(R.string.cancel),
                {
                    vm.postApproval(
                        VegaEcuadorBcApprovePost(
                            key = getCurrentKey(),
                            plant = getPlantDetails(),
                            approvalDetailsList = prepareDeliveryList()
                        )
                    )
                    vm.approval.observe(viewLifecycleOwner, Observer { updateUI(it) })


                },
                { dismiss() })
        }
    }

    private fun prepareDeliveryList(): List<VegaEcuadorBcApproveDetails> {
        val list = ArrayList<VegaEcuadorBcApproveDetails>()
//        for (item in approveQualityList) {
        val deliveryDetail = VegaEcuadorBcApproveDetails()
        deliveryDetail.batchNumber = wbDetails.batchNumber
        deliveryDetail.materialCode = wbDetails.materialNumber
        deliveryDetail.plant = getPlantDetails().plantId
        deliveryDetail.autoTransfer = ""
        deliveryDetail.discount = wbDetails.discount
        deliveryDetail.finalApproval = wbDetails.finalApproval
        deliveryDetail.grnQty = wbDetails.grnQty?.trim()
        deliveryDetail.item = wbDetails.item
        deliveryDetail.msg = ""
        deliveryDetail.paidWeight = ""
        deliveryDetail.priceCharacter = ""
        deliveryDetail.qchar = wbDetails.qchar
        deliveryDetail.qualityDetails = ArrayList<VegaEcuadorBcApproveQualityDetails>()
        var usageDecision = VegaEcuadorBcApproveQualityDetails()
        usageDecision.descrChar = USAGE_POST
        usageDecision.nameChar = LOBM_UDCODE
        usageDecision.qualityParameterValue = USAGE_DECISION_ACCEPT
        deliveryDetail.qualityDetails.add(usageDecision)
        deliveryDetail.receivingStorageLoc = ""
        deliveryDetail.sendingStorageLoc = ""
        deliveryDetail.status = false
        deliveryDetail.supplierCode = wbDetails.supplierCode
        deliveryDetail.uom = wbDetails.unitsOfMeasure
        deliveryDetail.waers = wbDetails.waers
        deliveryDetail.weighBridgeId = wbDetails.wbid
        deliveryDetail.poNumber = wbDetails.poNumber

        list.add(deliveryDetail)
//        }
        return list
    }


    private fun moveToSuccessPage() {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(
            AppUtils.TITLE,
            " BC Approval Successful"
        )
        intent.putExtra(AppUtils.SUB_TITLE, " ")
        startActivity(intent)
        requireActivity().finish()
    }


}

