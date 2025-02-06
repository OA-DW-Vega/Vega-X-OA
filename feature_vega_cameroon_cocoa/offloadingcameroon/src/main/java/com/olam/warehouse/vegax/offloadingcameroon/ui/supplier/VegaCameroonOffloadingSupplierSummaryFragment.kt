package com.olam.warehouse.vegax.offloadingcameroon.ui.supplier

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.printformats.ticketPrint
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcameroon.R
import com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.model.CameroonOffloadingQualityDetails
import com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.model.OffloadingQualityDetails
import com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.model.VegaCameroonGrnData
import com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.model.VegaCameroonOffloadingPost
import com.olam.warehouse.vegax.offloadingcameroon.databinding.FragmentVegaCameroonOffloadingSupplierSummaryBinding
import com.olam.warehouse.vegax.offloadingcameroon.ui.VegaCameroonOffloadingViewModel
import com.olam.warehouse.vegax.offloadingcameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 25/6/2020.
 */
class VegaCameroonOffloadingSupplierSummaryFragment : BaseFragment() {
    override val layoutResourceId: Int = R.layout.fragment_vega_cameroon_offloading_supplier_summary
    private val vm: VegaCameroonOffloadingViewModel by viewModel()
    private var receivingData = VegaReceiving()
    private var mReceiving = mutableListOf<VegaReceiving>()
    private var bagList = arrayListOf<VegaEcuadorOffloadingBagMaterial>()
    private lateinit var binding: FragmentVegaCameroonOffloadingSupplierSummaryBinding
    private var callBack: CallBack? = null
    private var receivingPlantBatchChar: String? = ""
    private var otLotBatchChar: String? = ""
    private var noOfJuteBags: Int = 0
    private var noOfNylonBags: Int = 0
    private var plantDetails = Plant()
    private var intent: Intent? = null

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
            receivingPlant: String?,
            otLotNumber: String?,
            plantDetails: Plant
        ) = VegaCameroonOffloadingSupplierSummaryFragment().putArgs {
            putParcelable(OFFLOADING_DATA, receivingData)
            putParcelable(OFFLOADING_PLANT_DETAILS_2, plantDetails)
            putParcelableArrayList(OFFLOADING_POST_DATA, mReceiving)
            putParcelableArrayList(OFFLOADING_POST_BAG_DATA, bagList)
            putString(OFFLOADING_RECEIVING_PLANT,receivingPlant)
            putString(OFFLOADING_OT_LOT_NUMBER,otLotNumber)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCameroonOffloadingSupplierSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("offloadingcameroon/ui/supplier/VegaCameroonOffloadingSupplierSummaryFragment")
            .title("Vega_Cameroon/Offloading")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btProceed, it, true)
        }
        receivingData = arguments?.getParcelable(OFFLOADING_DATA)!!
        plantDetails = arguments?.getParcelable(OFFLOADING_PLANT_DETAILS_2)!!
        mReceiving = arguments?.getParcelableArrayList<VegaReceiving>(OFFLOADING_POST_DATA)!!
        bagList = arguments?.getParcelableArrayList<VegaEcuadorOffloadingBagMaterial>(
            OFFLOADING_POST_BAG_DATA
        )!!
        receivingPlantBatchChar = arguments?.getString(OFFLOADING_RECEIVING_PLANT)
        otLotBatchChar = arguments?.getString(OFFLOADING_OT_LOT_NUMBER)
        binding.tvLotValue.text = receivingData.materialName
        if (receivingData.purchaseDocNum.isNullOrEmpty()) {
            binding.tvProcurementTypeValue.text = getString(R.string.spot_purchase)
            binding.tvPONumber.gone()
            binding.tvPONumberValue.gone()
        } else {
            binding.tvProcurementTypeValue.text = getString(R.string.fixed_purchase)
            binding.tvPONumberValue.text = receivingData.purchaseDocNum
        }

        binding.tvProductValue.text = receivingData.materialName
        binding.tvDestValue.text = receivingData.supplierName
        binding.tvReceivingWHValue.text = receivingData.receivingWH
        binding.tvPlantValue.text = receivingData.plantId
        binding.tvWRNoValue.text = receivingData.wRNo
        binding.tvBatchNoValue.text = receivingData.batchNumber

        binding.tvGrossValue.text = receivingData.grossWeight.plus(" ").plus(getString(R.string.uom_kg))
        binding.tvNoBagsValue.text = receivingData.bagCount
        binding.tvBagsTareValue.text = receivingData.tareWeight.plus(" ").plus(getString(R.string.uom_kg))
        binding.tvPalletWeightValue.text = receivingData.palletWeight.plus(" ").plus(getString(R.string.uom_kg))
        binding.tvNetWeightValue.text = receivingData.netWeight.plus(" ").plus(getString(R.string.uom_kg))
        updateTotalWeightValues()
        settingTrackTracevalue()
        binding.btProceed.setOnClickListener { showConfirmDialog() }
        vm.receive.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun settingTrackTracevalue(){
        /*indirect flow*/
        if(receivingData.sourceLotId.isNotEmpty()){
            binding.llSourceLotId.visible()
            binding.llEudrStatus.visible()
            binding.tvSourceLotIdValue.setText(receivingData.sourceLotId)
            if(receivingData.eudrStatus){
                binding.tvEudrStatusValue.setText(Constants.EUDR_QP_VALUE)
            } else {
                binding.tvEudrStatusValue.setText(Constants.ATTR_UNKNOWN_QP_VALUE)
            }
        }
        /*direct flow*/
        if(receivingData.ttFarmerList.size>0){
            binding.llEudrStatus.visible()
            if(receivingData.eudrStatus){
                binding.tvEudrStatusValue.setText(Constants.EUDR_QP_VALUE)
            } else {
                binding.tvEudrStatusValue.setText(Constants.ATTR_UNKNOWN_QP_VALUE)
            }
        }

        if(receivingData.farmerLessTransactionId?.isNotEmpty() == true){
            binding.llSourceLotId.visible()
            binding.llEudrStatus.visible()
            binding.tvSourceLotId.setText(getString(R.string.transaction_id))
            binding.tvSourceLotIdValue.setText(receivingData.farmerLessTransactionId)
            if(receivingData.eudrStatus){
                binding.tvEudrStatusValue.setText(Constants.EUDR_QP_VALUE)
            } else {
                binding.tvEudrStatusValue.setText(Constants.ATTR_UNKNOWN_QP_VALUE)
            }
        }
    }

    private fun updateTotalWeightValues() {
        binding.clNet.tvGrossWeightValue.text = binding.tvGrossValue.text
        binding.clNet.tvTareWeightValue.text = binding.tvBagsTareValue.text
        binding.clNet.tvPalletWeightValue.text = binding.tvPalletWeightValue.text
        binding.clNet.tvNetWeightValue.text = binding.tvNetWeightValue.text
    }

    private fun updateUI(data: Resource<GenericReqAndResp<VegaReceivingResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    prepareSuccessData(
                        receivingData.weighBridgeId,
                        it.data?.data?.grnNumber,
                        it.data?.data?.batchNumber,
                        true,
                        it.data?.message.toString(),
                        it.data?.data?.encodedImageContent
                    )
                    vm.deleteCameroonSavedBagDetails(it.data?.data?.batchNumber.toString())
                }
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
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                { postReceiving() },
                { dismiss() })
        }
    }

    private fun postReceiving() {
        noOfJuteBags = 0
        noOfNylonBags = 0
        if (AppUtils.isOnline()) {
            var grnList: ArrayList<VegaCameroonGrnData> = ArrayList()
            var grnData = VegaCameroonGrnData(
                receivingData.batchNumber,
                "",
                "",
                "",
                receivingData.item,
                receivingData.materialCode,
                receivingData.netWeight,
                receivingData.plantId,
                "",
                receivingData.purchaseDocDesc,
                receivingData.purchaseDocNum,
                receivingData.storageLocationCode,
                receivingData.supplierCode,
                receivingData.unitsOfMeasure,
                receivingData.weighBridgeId,
                receivingData.weighBridgeType
            )

            grnList.add(grnData)

            mReceiving.forEach {
                it.mtnCode=receivingData.mtnCode
                it.approximateWeight= receivingData.approximateWeight
                it.challan = receivingData.challan
                it.bagWeight = it.bagCount?.toInt()?.times(it.tareWeight?.toDouble()!!).toString()
                it.tmpWbId = ""
                if(it.bagType == JUTE_BAG)
                    noOfJuteBags = noOfJuteBags + (it.bagCount?.toInt() ?: 0)
                else if(it.bagType == POLY_BAG)
                    noOfNylonBags = noOfNylonBags + (it.bagCount1?.toInt() ?: 0)
            }
            var juteBagCountChar = OffloadingQualityDetails(
                materialCode = receivingData.materialCode,
                batchNumber = receivingData.batchNumber,
                descrChar = CI_JUTEBAG_CAM,
                nameChar = CI_JUTEBAG_CAM,
                qualityParameterValue = noOfJuteBags.toString()
            )
            var nylonBagCountChar = OffloadingQualityDetails(
                materialCode = receivingData.materialCode,
                batchNumber = receivingData.batchNumber,
                descrChar = CI_NYLONBAG_CAM,
                nameChar = CI_NYLONBAG_CAM,
                qualityParameterValue = noOfNylonBags.toString()
            )
            var palletCountChar = OffloadingQualityDetails(
                materialCode = receivingData.materialCode,
                batchNumber = receivingData.batchNumber,
                descrChar = CI_NOPALLET_CAM,
                nameChar = CI_NOPALLET_CAM,
                qualityParameterValue = mReceiving[0].palletCount
            )

            var otBatchChar = OffloadingQualityDetails(
                materialCode = receivingData.materialCode,
                batchNumber = receivingData.batchNumber,
                descrChar = SOURCE_LOT,
                nameChar = SOURCE_LOT,
                qualityParameterValue = getString(R.string.batch_prefix_lcm).plus(otLotBatchChar)
            )
            var receivingPlantChar = OffloadingQualityDetails(
                materialCode = receivingData.materialCode,
                batchNumber = receivingData.batchNumber,
                descrChar = RECEIVING_PLANT,
                nameChar = RECEIVING_PLANT,
                qualityParameterValue = receivingPlantBatchChar
            )
            var qualityDetails = ArrayList<OffloadingQualityDetails>()
            qualityDetails.add(otBatchChar)
            qualityDetails.add(receivingPlantChar)
            qualityDetails.add(juteBagCountChar)
            qualityDetails.add(nylonBagCountChar)
            qualityDetails.add(palletCountChar)
            var cameroonOffloadingQualityDetails = CameroonOffloadingQualityDetails(receivingData.materialCode,receivingData.batchNumber,qualityDetails)

            var lotDetails = ArrayList<CameroonOffloadingQualityDetails>()
            lotDetails.add(cameroonOffloadingQualityDetails)

            vm.postEcuadorOffloadingData(
                VegaCameroonOffloadingPost(
                    batchNumber = receivingData.batchNumber,
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
                    plant = plantDetails,
                    weighDetails = mReceiving,
                    lotDetails = lotDetails,
                    sourceLotId = receivingData.sourceLotId,
                    eudrStatus = receivingData.eudrStatus,
                    ttFarmerList = receivingData.ttFarmerList,
                    farmerLessTransactionId = if(receivingData.farmerLessTransactionId?.isNotEmpty()==true)Constants.FARMERLESS_TRANSACTION_ID_PREFIX.plus(receivingData.farmerLessTransactionId) else "",
                )
            )
        } else {
            prepareSuccessData(
                if (receivingData.tmpWbId.isEmpty()) getTmpId() else receivingData.tmpWbId,
                "",
                receivingData.batchNumber,
                false,
                "Data cached offline",
                ""
            )
        }
    }

    private fun prepareSuccessData(
        wbId: String?,
        grnNumber: String?,
        batchNumber: String?,
        syncStatus: Boolean,
        msg: String,
        encodedImageContent: String?
    ) {
        hideLoading()
        receivingData.status = if (syncStatus) Status.RECEVING_COMPLETED else Status.SYNC_PENDING
        receivingData.tmpWbId = if (!syncStatus) wbId ?: "" else ""
        receivingData.isSynced = syncStatus
        receivingData.direction = DIRECTIONIN
        receivingData.weighBridgeType = PROCURE
        receivingData.syncStatusMsg = msg

        if (!syncStatus) {
            receivingData.item = "00001"
            receivingData.weighBridgeId = wbId.toString()
            receivingData.bagWeight = receivingData.tareWeight
            vm.saveOffloading(receivingData)
            bagList.forEach { it.tmpWbId = wbId ?: "" }
            vm.saveReceivingLineItems(bagList)
        }
        moveToSuccessPage(wbId, grnNumber, batchNumber, encodedImageContent)
    }

    private fun prepareErrorData(wbId: String?, syncStatus: Boolean, msg: String) {
        hideLoading()
        receivingData.status = Status.SYNC_ERROR
        receivingData.isSynced = syncStatus
        receivingData.syncStatusMsg = msg
        vm.saveOffloading(receivingData)

    }

    private fun moveToSuccessPage(
        wbId: String?,
        grnNumber: String?,
        batchNumber: String?,
        encodedImageContent: String?
    ) {
        intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) {
            intent?.putExtra(
                AppUtils.TITLE,
                getString(R.string.success_offloading)
            )

            intent?.putExtra(AppUtils.PRINT_ENABLE, true)

            val tallyKeys = ArrayList<String>()
            tallyKeys.add(encodedImageContent ?: "")
            intent?.putStringArrayListExtra(AppUtils.TALLY_SHEETS, tallyKeys)
            if (receivingData.weighBridgeId.contains("TMP")) {
                vm.updateWBToQualityAndGrnTable(receivingData.weighBridgeId, wbId.toString())
            }
        } else intent?.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_offloading_offline)
        )
        val lotlist = ArrayList<VegaCoffeeSalesLots>()
        lotlist.add(
            VegaCoffeeSalesLots(
                "",
                batchNumber.toString(),
                receivingData.materialCode.toString(),
                receivingData.materialName.toString(),
                "",
                "",
                "",
                "",
                "",
                receivingData.unitsOfMeasure,
                "",
                receivingData.netWeight
            )
        )
        intent?.putExtra(UIUtils.FROM_CAMEROON_COCOA_OFFLOADING, true)
        intent?.putExtra(AppUtils.EUDR_STATUS, if(receivingData.eudrStatus == true)"1" else "0" )
        intent?.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotlist)
        intent?.putExtra(
            AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id_is).plus(wbId)
                .plus("\n").plus(getString(R.string.grn_id)).plus(grnNumber)
                .plus("\n").plus(getString(R.string.batch_no)).plus(": ").plus(batchNumber)
        )



        /*saved ticket keys in preference*/
        ticketPrint(intent!!,requireContext())
        var materialCode=""
        if(receivingData.materialCode?.isNotEmpty() == true){
            materialCode= receivingData.materialCode.toString().substring(6,receivingData.materialCode.toString().length)
        }
        if(PreferenceHelper.get(Constants.DIRECT,"").isNotEmpty() || PreferenceHelper.get(Constants.IN_DIRECT,"").isNotEmpty())
            intent?.putExtra(AppUtils.EUDR_STATUS, if(receivingData.eudrStatus) "1" else "0" )

        postPrintReceipt(grnNumber!!,batchNumber!!, materialCode,"ticket")
    }


    private fun postPrintReceipt(
        grnNo: String, batchNo: String,
        materialname: String,  isTicketType:String = "ticket"
    ) {

        val input = workDataOf(
            UIUtils.GRN_DATA to grnNo, BATCH_NO to batchNo,
            PRINT_TYPE to isTicketType, UIUtils.MATERIAL to materialname
        )
        val worker = getCmCocoOneTimeRequestWorker(input)
        enQueueWorker(worker, App.getAppContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(requireActivity(), androidx.lifecycle.Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            hideCustomLoading()
                            intent?.let { startActivity(it) }
                            activity?.finish()

                        }
                        WorkInfo.State.FAILED -> {
                            hideCustomLoading()
                            intent?.let { startActivity(it) }
                            activity?.finish()
                        }
                        WorkInfo.State.RUNNING -> {
                            showCustomLoading()
                        }
                        else -> {}
                    }
                }

            })
    }


}
