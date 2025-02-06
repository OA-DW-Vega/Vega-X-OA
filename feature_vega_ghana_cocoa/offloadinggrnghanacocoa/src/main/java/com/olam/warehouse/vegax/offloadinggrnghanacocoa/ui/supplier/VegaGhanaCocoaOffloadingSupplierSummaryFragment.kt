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
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.*
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
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
import kotlin.math.roundToInt

class VegaGhanaCocoaOffloadingSupplierSummaryFragment : BaseFragment() {
    override val layoutResourceId: Int =
        R.layout.fragment_vega_offloading_ghana_cocoa_supplier_summary
    private val vm: VegaGhanaCocoaOffloadingViewModel by viewModel()
    private var receivingData = VegaReceiving()
    private var mReceiving = mutableListOf<VegaReceiving>()
    private var bagList = arrayListOf<VegaEcuadorOffloadingBagMaterial>()
    private lateinit var binding: FragmentVegaOffloadingGhanaCocoaSupplierSummaryBinding
    private var callBack: CallBack? = null
    private var grnPrice: String = ""
    private var closingStock: Double = 0.0
    private var isOffline: Boolean = false
    private var supplierList = mutableListOf<VegaVendor>()
    private var uomDetails = ArrayList<VegaUomDetails>()
    var intent =Intent()
    var localWbId:String?=""
    var localGrnNumber:String?=""
    var localBatchNumber:String?=""
    var localMergedBatchNumber: String? = ""
    var finalPostingRequest : VegaGhanaCocoaOffloadingPost?= null


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
            grnPrice: String, isOffline: Boolean
        ) = VegaGhanaCocoaOffloadingSupplierSummaryFragment().putArgs {
            putParcelable(OFFLOADING_DATA, receivingData)
            putParcelableArrayList(OFFLOADING_POST_DATA, mReceiving)
            putParcelableArrayList(OFFLOADING_POST_BAG_DATA, bagList)
            putString(GRN_PRICE, grnPrice)
            putBoolean(IS_OFFLINE, isOffline)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVegaOffloadingGhanaCocoaSupplierSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("offloadingsesame/ui/supplier/VegaSesameOffloadingSupplierSummaryFragment")
            .title("Ecuador Offloading").with(tracker)
       intent= Intent(requireContext(), SuccessActivity::class.java)
        initUI()
    }

    private fun initUI() {
        receivingData = arguments?.getParcelable(OFFLOADING_DATA)!!
        mReceiving = arguments?.getParcelableArrayList<VegaReceiving>(OFFLOADING_POST_DATA)!!
        bagList = arguments?.getParcelableArrayList<VegaEcuadorOffloadingBagMaterial>(
            OFFLOADING_POST_BAG_DATA
        )!!
        grnPrice = arguments?.getString(GRN_PRICE)!!
        isOffline = arguments?.getBoolean(IS_OFFLINE)!!
        settingTrackTracevalue()
        binding.tvLotValue.text = receivingData.materialName
        if (receivingData.purchaseDocNum.isNullOrEmpty()) {
            binding.tvProcurementTypeValue.text = getString(R.string.spot_purchase)
            binding.tvPONumber.gone()
            binding.tvPONumberValue.gone()
        } else {
            binding.tvProcurementTypeValue.text = getString(R.string.fixed_purchase)
            binding.tvPONumberValue.text = receivingData.purchaseDocNum
        }

        if (!receivingData?.dseLotId.isNullOrEmpty())
        {
            binding.apply {
                llLotIdLayout.visible()
                tvlotsValue.text = receivingData.dseLotId
            }
        }

        binding.tvProductValue.text = receivingData.product
        binding.tvSupplierValue.text = receivingData.supplierName
        binding.tvReceivingWHValue.text = receivingData.receivingWH
//        binding.tvWRNoValue.text = receivingData.challan
        vm.suppplier.observe(viewLifecycleOwner, Observer {
            if (it != null)
                supplierList = it as MutableList
        })
        vm.getSuppliers()

        binding.tvNoBagsValue.text = receivingData.bagCount
        binding.tvBagTypeValue.text = receivingData.bagType
        binding.tvDateValue.text = receivingData.createdDate
        binding.tvWHReceiptNoValue.text = receivingData.whReceiptNum
        if ((receivingData.totalStockWeight != 0.0) && ((receivingData.totalStockWeight!!) >= (receivingData.bagCount?.toDouble()!!))) {
            closingStock =
                ((receivingData.totalStockWeight!!) - (receivingData.bagCount?.toDouble()!!))
        }
        binding.btProceed.setOnClickListener {
            if (isOffline) {
                activity?.onBackPressed()
            } else {
                showConfirmDialog()
            }
        }
        vm.receive.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.liveStocks.observe(viewLifecycleOwner, Observer { updateLiveStockUI(it) })


        vm.uomDetail.observe(viewLifecycleOwner, Observer {
            uomDetails = it as ArrayList<VegaUomDetails>
        })
        vm.getUomDetails()

    }


    private fun updateUI(data: Resource<GenericReqAndResp<VegaReceivingResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    var data = it.data?.data
                    if(data?.wbFlag == true && data?.qcFlag == true && data?.grnFlag == true && data?.bagFlag == true
                        && data.wbId?.isNotEmpty() == true && data.batchNumber?.isNotEmpty() == true && data.grnNumber?.isNotEmpty() == true){
                        prepareSuccessData(
                            data.wbId,
                            data.grnNumber,
                            true,
                            it.data?.message.toString(),
                            data.encodedImageContent,
                            data.batchNumber,
                            data.grnData?.get(0)?.batchNumber
                        )
                    } else {
                        data?.let { it1 -> retryGrnPost(it1) }
                        showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }

            }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                    prepareErrorData(false, it.error.toString())
                }
            }
        }
    }

    private fun retryGrnPost(data: VegaReceivingResponse){
        if (data != null) {
            if(data.wbFlag == true) {
                finalPostingRequest?.wbFlag = true
                finalPostingRequest?.grnData?.get(0)?.weighBridgeId = data?.wbId.toString()
            }
            if(data.qcFlag == true){
                finalPostingRequest?.qcFlag = true
                finalPostingRequest?.grnData?.get(0)?.batchNumber = data?.batchNumber.toString()
            }
            if(data.grnFlag == true){
                finalPostingRequest?.grnFlag = true
                finalPostingRequest?.grnNumber = data?.grnNumber.toString()
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
            if(finalPostingRequest ==null) {
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
                    "",
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
                var item1 = OffloadingGhanaCocoaQualityDetails()
                item1.nameChar = "COMPLIANCE"
                item1.qualityParameterValue =
                    if (receivingData.eudrStatus) Constants.EUDR_QP_VALUE else Constants.ATTR_UNKNOWN_QP_VALUE
                qualityDetails.add(item1)
                var item2 = OffloadingGhanaCocoaQualityDetails()
                item2.nameChar = "SOURCE_LOT"
                item2.qualityParameterValue =
                    if (receivingData.sourceLotId?.isNotEmpty() == true) receivingData.sourceLotId else receivingData.batchNumber
                qualityDetails.add(item2)
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
                    it.qcFlag = true
                }

                /*Retry mechanism*/
                finalPostingRequest = VegaGhanaCocoaOffloadingPost(
                    batchNumber = generateBatchId(),
                    cascara = "",
                    certificate = "",
                    encodedImageContent = "",
                    errorMessage = "",
                    exchangeRate = "",
                    grade = "",
                    grnData = grnList,
                    grnFlag = false,
                    wbFlag = false,
                    qcFlag = false,
                    bagFlag = false,
                    grnNumber = "",
                    grnType = "",
                    humedad = "",
                    imageUploadMsg = "",
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    weighDetails = mReceiving,
                    lotDetails = lotDetails,
                    whReceiptNum = receivingData.whReceiptNum,
                    if (supplierList.size > 0) supplierList.get(0).purchaseOrgType else " ",
                    sourceLotId = receivingData.sourceLotId,
                    eudrComplaint = receivingData.eudrStatus,
                    farmerLessTransactionId = if(receivingData.farmerLessTransactionId?.isNotEmpty()==true)Constants.FARMERLESS_TRANSACTION_ID_PREFIX.plus(receivingData.farmerLessTransactionId) else "",
                    ttFarmerList = receivingData.ttFarmerList
                )
            }
            finalPostingRequest?.let {
                vm.postGhanaCocoaOffloadingData(it)
            }

        } else {
            prepareSuccessData(
                if (receivingData.tmpWbId.isEmpty()) getTmpId() else receivingData.tmpWbId,
                getTmpId(),
                false,
                "Data cached offline",
                "","",""
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

        batchId = formattedYear.plus(formattedMonth).plus(formattedDate)
            .plus(receivingData.storageLocationCode)
        return batchId
//        /*Auto batch implemented, so no need this logic anymore*/
//        return ""
    }

    private fun prepareSuccessData(
        wbId: String?,
        grnNumber: String?,
        syncStatus: Boolean,
        msg: String,
        encodedImageContent: String?,
        batchNumber: String?,
        mergedBatchNumber: String?
    ) {
        localWbId= wbId
        localGrnNumber= grnNumber
        localBatchNumber = batchNumber
        localMergedBatchNumber = mergedBatchNumber
        hideLoading()
        receivingData.erdat = DateUtils.getCurrentTimeInMills().toString()
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


        moveToSuccessPage(wbId, grnNumber, batchNumber, mergedBatchNumber)
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

    private fun moveToSuccessPage(wbId: String?, grnNumber: String?, batchNumber: String?, mergedBatchNumber: String?) {
        if (AppUtils.isOnline()) {
            vm.clearBagDetails()
            intent.putExtra(
                AppUtils.TITLE,
                getString(R.string.success_offloading)
            )

            if (receivingData.weighBridgeId.contains("TMP")) {
                vm.updateWBToQualityAndGrnTable(receivingData.weighBridgeId, wbId.toString())
            }
        } else {
            intent.putExtra(
                AppUtils.TITLE,
                getString(R.string.success_offloading_offline)
            )
        }

        if (AppUtils.isOnline()) {
            receivingData.materialCode?.let { vm.getLiveStocks(it) }
        } else {
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.weigh_bridge_id_is).plus(" ").plus(wbId).plus("\n").plus(
                    getString(
                        R.string.grn_number
                    )
                ).plus(" ").plus(grnNumber).plus("\n").plus(getString(R.string.batch_number)
                ).plus(" ").plus(batchNumber).plus("\n").plus(getString(R.string.merged_batch_number)
                ).plus(" ").plus(mergedBatchNumber).toString()
                /* .plus("\n").plus(getString(R.string.closing_stock1)).plus(" ").plus((receivingData.palletCount).toString())*/
            )
            intent.putExtra(AppUtils.EUDR_STATUS, if(receivingData.eudrStatus) "1" else "0" )
            startActivity(intent)
            requireActivity().finish()
        }

    }

    private fun calculateBag(totalWeight: Double): Int {
        var noOfBags = 0
        var singleBags = 0.0
        var materialValue = (receivingData.product.toString()).split("-")
        var uom = ((uomDetails.filter { ((it.materialCode).equals(materialValue[0])) }).filter {
            it.fromUom.equals(BAG)
        }).single()
        singleBags =
            (((uom.value1?.toInt()?.toDouble()?.div(uom.value2?.toInt()?.toDouble()!!))) ?: 0.0)
        noOfBags = totalWeight.div(singleBags).roundToInt()
        return noOfBags
    }

    private fun updateLiveStockUI(response: Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            val warehouse =receivingData.receivingWH.toString()
                                    .split(" - ")
                            val stockcount = it.data?.data?.filter {
                                warehouse.get(0).trim()
                                    .let { it1 -> it.storageLocationCode?.equals(it1) } == true
                            }
                            val closingstock =
                                (stockcount?.sumOf {
                                    it.weight?.toDouble() ?: 0.0
                                })
                            receivingData.palletCount = closingstock?.let { it1 -> calculateBag(it1).toString() }
                            vm.saveOffloading(receivingData)

//                            receivingData.palletCount =
//                                ((if (receivingData.palletCount?.isNotEmpty() == true) receivingData.palletCount?.toInt()
//                                    ?: 0 else 0)).toString()
                            intent.putExtra(
                                AppUtils.SUB_TITLE,
                                getString(R.string.weigh_bridge_id_is).plus(" ").plus(localWbId)
                                    .plus("\n").plus(getString(R.string.grn_number)).plus(" ").plus(localGrnNumber)
                                    .plus("\n").plus(getString(R.string.batch_number)).plus(" ").plus(localBatchNumber)
                                    .plus("\n").plus(getString(R.string.merged_batch_number)).plus(" ").plus(localMergedBatchNumber)
                                    .toString()
                                    .plus("\n").plus(getString(R.string.closing_stock)).plus(" ")
                                    .plus((closingStock.toInt()).toString())
                                    .plus("\n").plus(getString(R.string.closing_stock1)).plus(" ")
                                    .plus((receivingData.palletCount).toString())
                            )

                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }

                    startActivity(intent)
                    requireActivity().finish()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                     startActivity(intent)
                    requireActivity().finish()

                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }

    }

    private fun settingTrackTracevalue(){
        /*indirect flow*/
        if(receivingData.sourceLotId.isNotEmpty()){
            binding.llSourceLot.visible()
            binding.llEudrStatus.visible()
            binding.tvSourceLotValue.setText(receivingData.sourceLotId)
            if(receivingData.eudrStatus){
                updateEudrStatusFlag(true)
            } else {
                updateEudrStatusFlag(false)
            }
        }
        /*direct flow*/
        if(receivingData.ttFarmerList.size>0){
            binding.llSourceLot.gone()
            binding.llEudrStatus.visible()
            if(receivingData.eudrStatus){
                updateEudrStatusFlag(true)
            } else {
                updateEudrStatusFlag(false)
            }
        }

        /*Farmerless transaction flow*/
        if(receivingData.farmerLessTransactionId?.isNotEmpty() == true){
            binding.llSourceLot.visible()
            binding.llEudrStatus.visible()
            binding.tvSourceLotLabel.text = "Transaction Id"
            binding.tvSourceLotValue.setText(receivingData.farmerLessTransactionId)
            if(receivingData.eudrStatus){
                updateEudrStatusFlag(true)
            } else {
                updateEudrStatusFlag(false)
            }
        }
    }

    fun updateEudrStatusFlag(isComplaint: Boolean) {
//            binding.tvDirectEudrStatus.visible()
        binding.eudrStatus.llEudrStatus.visible()
        if (isComplaint) {
            binding.eudrStatus.tvEudrStatusValue.text = Constants.EUDR_QP_VALUE
            binding.eudrStatus.llEudrStatus.setBackground(resources.getDrawable(com.olam.warehouse.login.R.drawable.rounded_corners_green))
            binding.eudrStatus.ivEudrFlag.setImageResource(com.olam.warehouse.login.R.drawable.ic_eudr_complaint_flag)
        } else {
            binding.eudrStatus.tvEudrStatusValue.text = Constants.ATTR_UNKNOWN_QP_VALUE
            binding.eudrStatus.llEudrStatus.setBackground(resources.getDrawable(com.olam.warehouse.login.R.drawable.rounded_corners_red))
            binding.eudrStatus.ivEudrFlag.setImageResource(com.olam.warehouse.login.R.drawable.ic_attr_unknown_flag)
        }
    }


}
