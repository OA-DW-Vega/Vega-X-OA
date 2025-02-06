package com.olam.warehouse.vegax.offloadingnigeria.ui.supplier

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
import com.olam.warehouse.master.common.model.BagDetails
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentWorkflowDetails
import com.olam.warehouse.master.common.utils.getMultiPlantList
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.WorkflowFields
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.presentation.BuildConfig
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.EUDR_STATUS
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentrynigeria.utils.isNGCashewEnabled
import com.olam.warehouse.vegax.offloadingnigeria.R
import com.olam.warehouse.vegax.offloadingnigeria.databinding.FragmentVegaNigeriaOffloadingSupplierSummaryBinding
import com.olam.warehouse.vegax.offloadingnigeria.ui.VegaNigeriaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 25/6/2020.
 */
class VegaNigeriaOffloadingSupplierSummaryFragment : BaseFragment() {
    override val layoutResourceId: Int = R.layout.fragment_vega_nigeria_offloading_supplier_summary
    private val vm: VegaNigeriaOffloadingViewModel by viewModel()
    private var receivingData = VegaReceiving()
    private var plantList = mutableListOf<Plant>()
    private var mReceiving = mutableListOf<VegaReceiving>()
    private var bagList = arrayListOf<VegaEcuadorOffloadingBagMaterial>()
    private lateinit var binding: FragmentVegaNigeriaOffloadingSupplierSummaryBinding
    private var callBack: CallBack? = null
    private var workFlowData: WorkflowFields? = null
    private var palletWeight: String = "0"
    private var palletCount: String = "0"
    private var palletAvg: String = "0"

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
            bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>
        ) = VegaNigeriaOffloadingSupplierSummaryFragment().putArgs {
            putParcelable(OFFLOADING_DATA, receivingData)
            putParcelableArrayList(OFFLOADING_POST_DATA, mReceiving)
            putParcelableArrayList(OFFLOADING_POST_BAG_DATA, bagList)
        }
        fun newInstance(
            receivingData: VegaReceiving,
            mReceiving: ArrayList<VegaReceiving>,
            bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>,
            palletCount: String,
            palletGrossweight: String,
            palletAvergaeCount: String
        ) = VegaNigeriaOffloadingSupplierSummaryFragment().putArgs {
            putParcelable(OFFLOADING_DATA, receivingData)
            putParcelableArrayList(OFFLOADING_POST_DATA, mReceiving)
            putParcelableArrayList(OFFLOADING_POST_BAG_DATA, bagList)
            putString("palletCount",palletCount)
            putString("palletGrossweight",palletGrossweight)
            putString("palletAvergaeCount",palletAvergaeCount)

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNigeriaOffloadingSupplierSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingnigeria/ui/supplier/VegaNigeriaOffloadingSupplierSummaryFragment")
            .title("Ecuador Offloading").with(tracker)
        initUI()
    }

    private fun initUI() {
        workFlowData = getCurrentWorkflowDetails(com.olam.warehouse.master.common.utils.getPlantDetails().plantId, "6")
        receivingData = arguments?.getParcelable(OFFLOADING_DATA)!!
        if(isNGCashewEnabled()) {
            palletWeight = arguments?.getString("palletGrossweight")!!
            palletCount = arguments?.getString("palletCount")!!
            palletAvg = arguments?.getString("palletAvergaeCount")!!
        }
        if(receivingData.weighMethod.equals("WB")) hideWbItems()
        mReceiving = arguments?.getParcelableArrayList<VegaReceiving>(OFFLOADING_POST_DATA)!!
        bagList = arguments?.getParcelableArrayList<VegaEcuadorOffloadingBagMaterial>(
            OFFLOADING_POST_BAG_DATA
        )!!
        binding.tvLotValue.text = receivingData.materialName
        /* if (receivingData.purchaseDocNum.isNullOrEmpty()) {
 //            binding.tvProcurementTypeValue.text = getString(R.string.spot_purchase)
 //            binding.tvPONumber.gone()
 //            binding.tvPONumberValue.gone()
             binding.tvPONumberValue.text = receivingData.supplierName

         } else {*/
        binding.tvProcurementTypeValue.text = receivingData.vehicleNumber
        binding.tvPONumberValue.text = receivingData.supplierName
        //}
        binding.tvGrossValue.text =
            receivingData.grossWeight.plus(" ").plus(receivingData.unitsOfMeasure)
        binding.tvNoBagsValue.text = receivingData.bagCount
        binding.tvBagsTareValue.text =
            receivingData.tareWeight.plus(" ").plus(receivingData.unitsOfMeasure)
        binding.tvNetWeightValue.text =
            receivingData.netWeight.plus(" ").plus(receivingData.unitsOfMeasure)
        updateTotalWeightValues()
        binding.btProceed.setOnClickListener { showConfirmDialog() }
        vm.receive.observe(viewLifecycleOwner, Observer { updateUI(it) })
        setTrackTraceValue()

        if (PreferenceHelper.get(Constants.CROP_LIMIT, "").isNotEmpty()) {
            binding.cropLimit.visibility=View.VISIBLE
            binding.tvMaxCropLimitValue.text=receivingData.capacityWeight +" "+receivingData.unitsOfMeasure
            binding.tvLeftOverWeightValue.text=receivingData.leftOverWeight+" "+receivingData.unitsOfMeasure
        }
        if(isNGCashewEnabled()){
            binding.llEudr.gone()
        }

    }

    private fun setTrackTraceValue(){
        /*Indirect flow*/
        if(receivingData.sourceLotId.isNotEmpty()) {
            binding.sourcelotll.visible()
            binding.tvSourceLotIdValue.text = receivingData.sourceLotId
            if (receivingData.eudrStatus)
                binding.tvEudrStatusValue.setText(Constants.EUDR_QP_VALUE) else binding.tvEudrStatusValue.setText(Constants.ATTR_UNKNOWN_QP_VALUE)
        }
        /*Indirect Flow*/
        if(receivingData.ttFarmerList.size>0){
            if (receivingData.eudrStatus) binding.tvEudrStatusValue.setText(Constants.EUDR_QP_VALUE) else binding.tvEudrStatusValue.setText(Constants.ATTR_UNKNOWN_QP_VALUE)
        }
        if(receivingData.farmerLessTransactionId?.isNotEmpty() == true){
            binding.sourcelotll.visible()
            binding.tvSourceLotIdValue.text = receivingData.farmerLessTransactionId
            binding.tvSourceLotIdLabel.text = getString(R.string.transaction_id)
            if (receivingData.eudrStatus) binding.tvEudrStatusValue.setText(Constants.EUDR_QP_VALUE) else binding.tvEudrStatusValue.setText(Constants.ATTR_UNKNOWN_QP_VALUE)
        }

        }

    private fun updateTotalWeightValues() {
        binding.clNet.tvGrossWeightValue.text = binding.tvGrossValue.text
        binding.clNet.tvTareWeightValue.text = binding.tvBagsTareValue.text
        binding.clNet.tvNetWeightValue.text = binding.tvNetWeightValue.text
    }

    private fun updateUI(data: Resource<GenericReqAndResp<VegaReceivingResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> prepareSuccessData(it.data?.data?.wbId, true, it.data?.message.toString(),
                    it.data?.data?.encodedImageContent,it.data?.data?.batchNumber)
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                    prepareErrorData(receivingData.weighBridgeId, false, it.error.toString())
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
        if(receivingData.weighMethod=="WS"){
        mReceiving.forEach {
            it.netWeight = /*convertKgToMTNigeria(*/it.netWeight/*)*/
            it.grossWeight = it.grossWeight /*?.let { it1 -> convertKgToMTNigeria(it1) }*/
            it.unitsOfMeasure = "MT"
            it.ttFarmerList= receivingData.ttFarmerList
        }}
        if (AppUtils.isOnline()) {
            plantList = getMultiPlantList() as MutableList<Plant>
            var plantDetails = getPlantDetails(receivingData.plantId).single()
            vm.postEcuadorOffloadingData(
                VegaReceivingPost(
                    key = getCurrentKey(),
                    plant = plantDetails,
                    weighDetails = mReceiving,
                    notificationFlag = workFlowData?.NotificationFlag?.trim().equals("0"),
                    nextWorkFlowRole = workFlowData?.workflowRole,
                    navId = workFlowData?.workflowId,
                    currentWorkFlowRole = workFlowData?.module,
                    environment = BuildConfig.BUILD_TYPE,
                    eudrComplaint = receivingData.eudrStatus,
                    isDelete = receivingData.isDelete,
                    ttFarmerList = receivingData.ttFarmerList,
                    sourceLotId = receivingData.sourceLotId,
                    farmerLessTransactionId = if(receivingData.farmerLessTransactionId?.isNotEmpty()==true)Constants.FARMERLESS_TRANSACTION_ID_PREFIX.plus(receivingData.farmerLessTransactionId) else "",
                    )
            )

            if(isNGCashewEnabled()){
                var bagDetails=BagDetails()
                bagDetails = prepareBagDetails(bagList,receivingData)

                val netWeight= bagDetails.grossWeight?.toDouble()?.minus(bagDetails.totalTarWeight?.toDouble()?:0.0)

                val receivingList = mutableListOf<VegaReceiving>()
                val item= mReceiving[0]
                item.unitsOfMeasure="KG"
                receivingList.add(item)

                vm.postEcuadorOffloadingData(
                    VegaReceivingPost(
                        key = getCurrentKey(),
                        plant = plantDetails,
                        weighDetails = receivingList,
                        notificationFlag = workFlowData?.NotificationFlag?.trim().equals("0"),
                        nextWorkFlowRole = workFlowData?.workflowRole,
                        navId = workFlowData?.workflowId,
                        currentWorkFlowRole = workFlowData?.module,
                        environment = BuildConfig.BUILD_TYPE,
                        eudrComplaint = receivingData.eudrStatus,
                        isDelete = receivingData.isDelete,
                        ttFarmerList = receivingData.ttFarmerList,
                        sourceLotId = receivingData.sourceLotId,
                        bagCount = bagDetails.bagCount ,
                        bagCount1 = bagDetails.bagCount1,
                        bagCount2 = bagDetails.bagCount2,
                        bagType = bagDetails.bagType,
                        bagType1 = bagDetails.bagType1,
                        bagType2 = bagDetails.bagType2,
                        tareWeight = bagDetails.tareWeight,
                        tareWeight1 = bagDetails.tareWeight1,
                        tareWeight2 = bagDetails.tareWeight2,
                        totalTarWeight = bagDetails.totalTarWeight,
                        grossWeight =bagDetails.grossWeight,
                        netWeight = netWeight.toString()
                    )
                )
            }else {

                vm.postEcuadorOffloadingData(
                    VegaReceivingPost(
                        key = getCurrentKey(),
                        plant = plantDetails,
                        weighDetails = mReceiving,
                        notificationFlag = workFlowData?.NotificationFlag?.trim().equals("0"),
                        nextWorkFlowRole = workFlowData?.workflowRole,
                        navId = workFlowData?.workflowId,
                        currentWorkFlowRole = workFlowData?.module,
                        environment = BuildConfig.BUILD_TYPE,
                        eudrComplaint = receivingData.eudrStatus,
                        isDelete = receivingData.isDelete,
                        ttFarmerList = receivingData.ttFarmerList,
                        sourceLotId = receivingData.sourceLotId
                    )
                )
            }


        } else {
            prepareSuccessData(
                if (receivingData.tmpWbId.isEmpty()) getTmpId() else receivingData.tmpWbId,
                false,
                "Data cached offline",
                "",
                receivingData.batchNumber
            )
        }
    }

    private fun getPlantDetails(plantId: String?): List<Plant> {
        return plantList.filter { it.plantId == plantId }
    }

    private fun prepareSuccessData(
        wbId: String?,
        syncStatus: Boolean,
        msg: String,
        encodedImageContent: String?,
        batchNumber: String?
    ) {
        hideLoading()
        receivingData.status = if (syncStatus) Status.RECEVING_COMPLETED else Status.SYNC_PENDING
        receivingData.tmpWbId = if (!syncStatus) wbId ?: "" else ""
        receivingData.isSynced = syncStatus
        receivingData.direction = "IN"
        receivingData.weighBridgeType = "PROCURE"
        receivingData.syncStatusMsg = msg
        if (!syncStatus) {
            receivingData.item = "00001"
            receivingData.weighBridgeId = wbId.toString()
            vm.saveOffloading(receivingData)
            bagList.forEach { it.tmpWbId = wbId ?: "" }
            vm.saveReceivingLineItems(bagList)
        }
        moveToSuccessPage(wbId, encodedImageContent,batchNumber)
    }

    private fun prepareErrorData(wbId: String?, syncStatus: Boolean, msg: String) {
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

    private fun moveToSuccessPage(wbId: String?, encodedImageContent: String?, batchNumber: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) {
            if(isNGCashewEnabled()){
                vm.clearBagDetails()
            }

            intent.putExtra(
                AppUtils.TITLE,
                getString(R.string.success_offloading)
            )
            intent.putExtra(
                AppUtils.PRINT_ENABLE,
                (encodedImageContent != null && encodedImageContent != "")
            )
            val tallyKeys = ArrayList<String>()
            tallyKeys.add(encodedImageContent ?: "")
            intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS, tallyKeys)
            if (receivingData.weighBridgeId.contains("TMP")) {
                vm.updateWBToQualityAndGrnTable(receivingData.weighBridgeId, wbId.toString())
            }
        } else intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_offloading_offline)
        )
        if(batchNumber==null || batchNumber.isNullOrEmpty()) {
            intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id_is).plus(wbId))
        }else{
            intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id_is).plus(wbId).plus("\n").
            plus(getString(R.string.lot_no_new)).plus(batchNumber))
        }
        intent.putExtra(EUDR_STATUS,if(receivingData.eudrStatus)"1" else "0")
        startActivity(intent)
        requireActivity().finish()
    }
    fun hideWbItems(){
        binding.clNet.clNet.gone()
        binding.tvGross.gone()
        binding.tvGrossValue.gone()
        binding.viGross.gone()
        binding.tvNoBags.gone()
        binding.tvNoBagsValue.gone()
        binding.tvBagsTare.gone()
        binding.tvBagsTareValue.gone()
        binding.viBags.gone()
        binding.tvNetWeight.gone()
        binding.tvNetWeightValue.gone()
        binding.viProcure.gone()
    }
    fun prepareBagDetails(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>, receivingData: VegaReceiving): BagDetails {
        val firstBag = bagList.getOrNull(0) ?: VegaEcuadorOffloadingBagMaterial() // Default empty bag if not available
        val secondBag = bagList.getOrNull(1) ?: VegaEcuadorOffloadingBagMaterial() // Default empty bag if second one is missing
       var bagCount2= palletCount
       var bagType2= "Pallet"
       var tareWeight2= palletAvg
        if(bagList.size>2){
            val bag2= bagList[2]
            bagCount2= bag2.bagCount
            bagType2= bag2.bagType
            tareWeight2= bag2.tareWeight.toString()
        }
        return BagDetails(
            bagCount = firstBag.bagCount ?: "",
            bagCount1 = secondBag.bagCount ?: "",
            bagCount2 = bagCount2,
            bagType = firstBag.bagType ?: "",
            bagType1 = secondBag.bagType ?: "",
            bagType2 = bagType2,
            tareWeight = firstBag.tareWeight ?: "",
            tareWeight1 = secondBag.tareWeight ?: "",
            tareWeight2 = tareWeight2,
            totalTarWeight = receivingData.tareWeight?.toDouble()?.formatThreeDigits() ?: "0",
            grossWeight = receivingData.grossWeight?.toDouble()?.formatThreeDigits()?: "0",
            netWeight = receivingData.netWeight.toDouble().formatThreeDigits()?:"0"
        )
    }

}
