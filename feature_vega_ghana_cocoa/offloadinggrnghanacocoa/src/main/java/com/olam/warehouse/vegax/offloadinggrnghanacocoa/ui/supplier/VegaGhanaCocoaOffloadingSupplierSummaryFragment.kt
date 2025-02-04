package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.supplier

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.model.*
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.R
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.databinding.FragmentVegaOffloadingGhanaCocoaSupplierSummaryBinding
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.VegaGhanaCocoaOffloadingViewModel
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VegaGhanaCocoaOffloadingSupplierSummaryFragment : BaseFragment() {
    override val layoutResourceId: Int = R.layout.fragment_vega_offloading_ghana_cocoa_supplier_summary
    private val vm: VegaGhanaCocoaOffloadingViewModel by viewModel()
    private var receivingData = VegaReceiving()
    private var mReceiving = mutableListOf<VegaReceiving>()
    private var bagList = arrayListOf<VegaEcuadorOffloadingBagMaterial>()
    private lateinit var binding: FragmentVegaOffloadingGhanaCocoaSupplierSummaryBinding
    private var callBack: CallBack? = null
    private var grnPrice: String = ""
    private var isOffline: Boolean = false

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            receivingData: VegaReceiving
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(
            receivingData: VegaReceiving,
            mReceiving: ArrayList<VegaReceiving>,
            bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>,
            grnPrice: String,isOffline:Boolean
        ) = VegaGhanaCocoaOffloadingSupplierSummaryFragment().putArgs {
            putParcelable(OFFLOADING_DATA, receivingData)
            putParcelableArrayList(OFFLOADING_POST_DATA, mReceiving)
            putParcelableArrayList(OFFLOADING_POST_BAG_DATA, bagList)
            putString(GRN_PRICE, grnPrice)
            putBoolean(IS_OFFLINE, isOffline)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaOffloadingGhanaCocoaSupplierSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingsesame/ui/supplier/VegaSesameOffloadingSupplierSummaryFragment")
            .title("Ecuador Offloading").with(tracker)
        initUI()
    }

    private fun initUI() {
        receivingData = arguments?.getParcelable(OFFLOADING_DATA)!!
        mReceiving = arguments?.getParcelableArrayList<VegaReceiving>(OFFLOADING_POST_DATA)!!
        bagList = arguments?.getParcelableArrayList<VegaEcuadorOffloadingBagMaterial>(OFFLOADING_POST_BAG_DATA)!!
        grnPrice = arguments?.getString(GRN_PRICE)!!
        isOffline = arguments?.getBoolean(IS_OFFLINE)!!
        binding.tvLotValue.text = receivingData.materialName
        if (receivingData.purchaseDocNum.isNullOrEmpty()) {
            binding.tvProcurementTypeValue.text = getString(R.string.spot_purchase)
            binding.tvPONumber.gone()
            binding.tvPONumberValue.gone()
        } else {
            binding.tvProcurementTypeValue.text = getString(R.string.fixed_purchase)
            binding.tvPONumberValue.text = receivingData.purchaseDocNum
        }
        binding.tvProductValue.text = receivingData.product
        binding.tvSupplierValue.text = receivingData.supplierName
        binding.tvReceivingWHValue.text = receivingData.receivingWH
//        binding.tvWRNoValue.text = receivingData.challan


        binding.tvNoBagsValue.text = receivingData.bagCount
        binding.tvBagTypeValue.text = receivingData.bagType
        binding.tvDateValue.text = receivingData.createdDate
        binding.tvWHReceiptNoValue.text = receivingData.whReceiptNum
        binding.btProceed.setOnClickListener {
            if(isOffline){
                activity?.onBackPressed()
            }else{
                showConfirmDialog()
            }
        }
        vm.receive.observe(viewLifecycleOwner, Observer { updateUI(it) })


    }


    private fun updateUI(data: Resource<GenericReqAndResp<VegaReceivingResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> prepareSuccessData(
                    it.data?.data?.wbId,
                    it.data?.data?.grnNumber,
                    true,
                    it.data?.message.toString(),
                    it.data?.data?.encodedImageContent
                )
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                    prepareErrorData( false, it.error.toString())
                }
            }
        }
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_offloading)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    postReceiving()
                },
                { dismiss() })
        }
    }

    private fun postReceiving() {
        receivingData.batchNumber = generateBatchId()
        receivingData.wsType = "WS"

        if (AppUtils.isOnline()) {
//            receivingData.netWeight = "62.5"
//            receivingData.grossWeight = "62.5"
//            vm.postEcuadorOffloadingData(VegaReceivingPost(getCurrentKey(), getPlantDetails(), mReceiving))
            var grnList: ArrayList<VegaGhanaCocoaGrnData> = ArrayList()
            var grnData = VegaGhanaCocoaGrnData(
                generateBatchId(),
                "",
                receivingData.currency,
                "",
                receivingData.item,
                receivingData.materialCode,
                receivingData.netWeight,
                receivingData.plantId,
                grnPrice,
                receivingData.bagCount,
                receivingData.purchaseDocDesc,
                receivingData.purchaseDocNum,
                receivingData.storageLocationCode,
                receivingData.supplierCode,
                receivingData.unitsOfMeasure,
                //receivingData.weighBridgeId,
                "",
                receivingData.weighBridgeType,
                receivingData.whReceiptNum
            )
            grnList.add(grnData)

            var qualityDetails = ArrayList<OffloadingGhanaCocoaQualityDetails>()
            var cameroonOffloadingQualityDetails = GhanaCocoaOffloadingQualityDetails(
                receivingData.materialCode,
                receivingData.batchNumber,
                qualityDetails
            )
            var lotDetails = ArrayList<GhanaCocoaOffloadingQualityDetails>()
            lotDetails.add(cameroonOffloadingQualityDetails)

            mReceiving.add(receivingData)
            mReceiving.forEach {
                it.challan = receivingData.challan
                it.tmpWbId = ""
                it.weighBridgeId = ""
                it.grnNumber = ""
                it.direction = ""
                it.batchNumber = generateBatchId()
                it.erdat = DateUtils.getCurrentTimeInMills().toString()
            }

            vm.postGhanaCocoaOffloadingData(
                VegaGhanaCocoaOffloadingPost(
                    batchNumber = generateBatchId(),
                    cascara = "",
                    certificate = "",
                    encodedImageContent = "",
                    errorMessage = "",
                    exchangeRate = "",
                    grade = "",
                    grnData = grnList,
                    grnFlag = true,
                    grnNumber = "",
                    grnType = "",
                    humedad = "",
                    imageUploadMsg = "",
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    weighDetails = mReceiving,
                    lotDetails = lotDetails,
                    whReceiptNum = receivingData.whReceiptNum
                )
            )
        } else {
            prepareSuccessData(
                if (receivingData.tmpWbId.isEmpty()) getTmpId() else receivingData.tmpWbId,
                getTmpId(),
                false,
                "Data cached offline",
                ""
            )
        }
    }

    private fun generateBatchId(): String {
        var batchId = ""
        val yearFormat = SimpleDateFormat("yy") // Just the year, with 2 digits
        val monthFormat = SimpleDateFormat("MM") // Just the month, with 2 digits
        val dateFormat = SimpleDateFormat("dd") // Just the date, with 2 digits

        val formattedDate = dateFormat.format(Calendar.getInstance().time)
        val formattedMonth = monthFormat.format(Calendar.getInstance().time)
        val formattedYear = yearFormat.format(Calendar.getInstance().time)

        batchId = formattedYear.plus(formattedMonth).plus(formattedDate).plus(receivingData.storageLocationCode)
        return batchId
    }

    private fun prepareSuccessData(
        wbId: String?,
        grnNumber: String?,
        syncStatus: Boolean,
        msg: String,
        encodedImageContent: String?
    ) {
        hideLoading()
        receivingData.status = if (syncStatus) Status.RECEVING_COMPLETED else Status.SYNC_PENDING
        receivingData.tmpWbId = if (!syncStatus) wbId ?: "" else ""
        receivingData.isSynced = syncStatus
        receivingData.direction = "IN"
        receivingData.weighBridgeType = "PROCURE"
        receivingData.syncStatusMsg = msg
        receivingData.isOffline = true
        if (!syncStatus) {
            receivingData.item = "00001"
            receivingData.weighBridgeId = wbId.toString()
            receivingData.grnNumber = grnNumber
            vm.saveOffloading(receivingData)
            bagList.forEach { it.tmpWbId = wbId ?: "" }
            vm.saveReceivingLineItems(bagList)
        }
        moveToSuccessPage(wbId, grnNumber)
    }

    private fun prepareErrorData(syncStatus: Boolean, msg: String) {
        hideLoading()
        receivingData.status = Status.SYNC_ERROR
        receivingData.isSynced = syncStatus
        receivingData.syncStatusMsg = msg
        vm.saveOffloading(receivingData)
        /*if (!syncStatus) {
            mReceiving.forEach { it.tmpWbId = wbId ?: "" }
            vm.saveReceivingLineItems(mReceiving)
        }*/
    }

    private fun moveToSuccessPage(wbId: String?, grnNumber: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) {
            vm.clearBagDetails()
            intent.putExtra(
                AppUtils.TITLE,
                getString(R.string.success_offloading)
            )
//            intent.putExtra(
//                AppUtils.PRINT_ENABLE,
//                (encodedImageContent != null && encodedImageContent != "")
//            )
//            val tallyKeys = ArrayList<String>()
//            tallyKeys.add(encodedImageContent ?: "")
//            intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS, tallyKeys)
            if (receivingData.weighBridgeId.contains("TMP")) {
                vm.updateWBToQualityAndGrnTable(receivingData.weighBridgeId, wbId.toString())
            }
        } else {
            intent.putExtra(
                AppUtils.TITLE,
                getString(R.string.success_offloading_offline)
            )
        }
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id_is).plus(" ").plus(wbId).plus("\n").plus(getString(
                    R.string.grn_number)).plus(" ").plus(grnNumber))
        startActivity(intent)
        requireActivity().finish()
    }

}
