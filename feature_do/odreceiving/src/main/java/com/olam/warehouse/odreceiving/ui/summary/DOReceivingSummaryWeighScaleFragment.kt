package com.olam.warehouse.odreceiving.ui.summary

//import org.matomo.sdk.Tracker
//import org.matomo.sdk.extra.TrackHelper
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.dorigin.entity.DOCustomStLocation
import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.master.dorigin.entity.DOReceivingLineItem
import com.olam.warehouse.master.dorigin.entity.DOTxnDetail
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.data.domain.model.*
import com.olam.warehouse.odreceiving.databinding.FragmentDoReceivingSummaryWeighScaleBinding
import com.olam.warehouse.odreceiving.ui.DOReceivingViewModel
import com.olam.warehouse.odreceiving.utils.FROM_DO_GHANA_CASH
import com.olam.warehouse.odreceiving.utils.RECEIVING_DATA
import com.olam.warehouse.odreceiving.utils.RECEIVING_POST_DATA
import com.olam.warehouse.odreceiving.utils.getTmpId
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.SUB_TITLE
import com.olam.warehouse.presentation.utils.AppUtils.TITLE
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
class DOReceivingSummaryWeighScaleFragment : BaseFragment() {

    private val vm: DOReceivingViewModel by viewModel()
    private var postData: MutableList<DOReceiving>? = mutableListOf<DOReceiving>()
    private var postDataOffline: MutableList<DOReceivingLineItem>? =
        mutableListOf<DOReceivingLineItem>()
    private var receivingData = DOReceiving()
    private var customLocationList = mutableListOf<DOCustomStLocation>()

    private var isBltEnabled: Boolean = false
    private var scanLevelId: Int = 0

    private lateinit var binding: FragmentDoReceivingSummaryWeighScaleBinding
    override val layoutResourceId = R.layout.fragment_do_receiving_summary_weigh_scale
    private var doTxnDetail = DOTxnDetail()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDoReceivingSummaryWeighScaleBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odreceiving/ui/summary/DOReceivingSummaryWeighScaleFragment")
            .title("OD/Receiving")
            .with(tracker)
        initUI()
    }

    companion object {
        fun newInstance(
            data: DOReceiving,
            postData: ArrayList<DOReceiving>?,
            isBltEnabled: Boolean,
            scanLevelId: Int,
            postDataOffline: ArrayList<DOReceivingLineItem>?,
            doTxnDetail: DOTxnDetail
        ) =
            DOReceivingSummaryWeighScaleFragment().putArgs {
                putParcelable(RECEIVING_DATA, data)
                putParcelableArrayList(RECEIVING_POST_DATA, postData)
                putParcelableArrayList("postDataOffline", postDataOffline)
                putBoolean("isBltEnabled", isBltEnabled)
                putInt("scanLevelId", scanLevelId)
                putParcelable("doTxnDetail", doTxnDetail)
            }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnConfirm, it, true)
        }
        receivingData = arguments?.getParcelable(RECEIVING_DATA)!!
        doTxnDetail = arguments?.getParcelable("doTxnDetail")!!
        Log.i("receivingBundle", receivingData.toString())
        if (arguments?.containsKey(RECEIVING_POST_DATA)!!) {
            postData = arguments?.getParcelableArrayList<DOReceiving>(RECEIVING_POST_DATA)
        }
        postData?.forEach { it.netWeight = it.grossWeight.minus(it.tareWeight?.toDouble() ?: 0.0) }
        if (arguments?.containsKey("postDataOffline")!!) {
            postDataOffline =
                arguments?.getParcelableArrayList<DOReceivingLineItem>("postDataOffline")
        }
        postDataOffline?.forEach {
            it.netWeight = it.grossWeight.minus(it.tareWeight?.toDouble() ?: 0.0)
        }
        isBltEnabled = arguments?.getBoolean("isBltEnabled", false)!!
        scanLevelId = arguments?.getInt("scanLevelId", 0)!!
        var tare: Double = 0.0
        //var grossWeight: Double = 0.0


        if (postData != null) {
            loadSourceLotData()
            receivingData.bagCount = vm.getBagCount(postData!!).toString()
            tare = vm.getTareWeight(postData!!)
            receivingData.bagWeight = tare
            receivingData.grossWeight = vm.getGrossWeight(postData!!)
//            receivingData.netWeight = vm.getNetWeight(postData!!)
            receivingData.netWeight =
                receivingData.grossWeight.minus(receivingData.bagWeight ?: 0.0)

            val tareWeight = tare.formatThreeDigits().plus(" ").plus(receivingData.uom)
            val grossWeight =
                receivingData.grossWeight.formatThreeDigits().plus(" ").plus(receivingData.uom)
            binding.tvProductType.text = receivingData.materialName
            binding.tvSupplier.text = receivingData.supplierName
            binding.tvNoOfBags.text = receivingData.bagCount
            binding.tvBagWeight.text =
                receivingData.bagWeight?.formatThreeDigits().plus(" ").plus(receivingData.uom)
            binding.tvTareWeight.text = tareWeight
            binding.tvGrossWeight.text = grossWeight
            binding.tvGross.text = grossWeight
            binding.tvTare.text = tareWeight
            binding.tvNetWeight.text =
                receivingData.netWeight.formatThreeDigits().plus(" ").plus(receivingData.uom)
            binding.tvReceivedOn.text = DateUtils.fromMillisToTimeString(DateUtils.getCurrentTimeInMills())

            binding.btnConfirm.visibility = View.VISIBLE
        }

        if (postDataOffline != null) {
            receivingData.bagCount = vm.getBagCountOffline(postDataOffline!!).toString()
            tare = vm.getTareWeightLineItem(postDataOffline!!)
            receivingData.bagWeight = tare
            receivingData.grossWeight = vm.getGrossWeightOffline(postDataOffline!!)
//            receivingData.netWeight = vm.getNetWeightOffline(postDataOffline!!)
            receivingData.netWeight =
                receivingData.grossWeight.minus(receivingData.bagWeight ?: 0.0)

            val tareWeight = tare.formatThreeDigits().plus(" ").plus(receivingData.uom)
            val grossWeight =
                receivingData.grossWeight.formatThreeDigits().plus(" ").plus(receivingData.uom)
            binding.tvProductType.text = receivingData.materialName
            binding.tvSupplier.text = receivingData.supplierName
            binding.tvNoOfBags.text = receivingData.bagCount
            binding.tvBagWeight.text =
                receivingData.bagWeight?.formatThreeDigits().plus(" ").plus(receivingData.uom)
            binding.tvTareWeight.text = tareWeight
            binding.tvGrossWeight.text = grossWeight
            binding.tvGross.text = grossWeight
            binding.tvTare.text = tareWeight
            binding.tvNetWeight.text =
                receivingData.netWeight.formatThreeDigits().plus(" ").plus(receivingData.uom)
            binding.tvReceivedOn.text = DateUtils.fromMillisToTimeString(DateUtils.getCurrentTimeInMills())

            binding.btnConfirm.visibility = View.GONE
        }

        binding.btnConfirm.setOnClickListener { validateWeight() }
        vm.receive.observe(viewLifecycleOwner, Observer { updateUI(it) })

        vm.customLocation.observe(viewLifecycleOwner, Observer {
            customLocationList = it.toMutableList()
            if (it.isNotEmpty()) {
                receivingData.storageLocationCode = it[0].procureLocationCode
                if(postData!=null) {
                    postData!!.forEach { item -> item.storageLocationCode = it[0].procureLocationCode }
                }
            }
        })
       vm.getCustomLocations()
        vm.doBagsInfo.observe(viewLifecycleOwner, Observer { doBags ->
            var missedQrCodes = mutableListOf<QrCodes>()
            var replaceQrCodes = mutableListOf<ReplaceQrCodes>()
            missedQrCodes.clear()
            replaceQrCodes.clear()
            val plant = getPlantDetails()
            var mDoBags = doBags.filter { it.lotTransactionId.contains(doTxnDetail.lotTransactionId) }
            var transactionIdList = mDoBags.map { it.transactionId }.distinct()
            transactionIdList.forEach { id ->
                var temptransid = mDoBags.filter { it.transactionId.equals(id) }
                var missedQrCode = mutableListOf<String>()
                var qrCodeMapping = mutableListOf<QrCodeMapping>()
                temptransid.forEach { doBag ->

                    if (doBag.bagMissed != null && doBag.bagMissed) {
                        missedQrCode.add(doBag.bagQrCode.toString())
                    }
                    if (doBag.newQrCode != 0) {
                        qrCodeMapping.add(
                            QrCodeMapping(
                                newQrCode = doBag.newQrCode.toString(),
                                oldQrCode = doBag.bagQrCode.toString()
                            )
                        )
                    }
                }
                if (qrCodeMapping.size != 0) {
                    val replaceQrCode = ReplaceQrCodes(transactionId = id, qrCodeMapping = qrCodeMapping)
                    replaceQrCodes.add(replaceQrCode)
                }
                if (missedQrCode.size != 0) {
                    val QrCode = QrCodes(transactionId = id, qrCodes = missedQrCode)
                    missedQrCodes.add(QrCode)
                }
            }

            if (isOnline()) {
                vm.postReceivingData(
                    DOReceivingPost(
                        getCurrentKey(),
                        plant,
                        missedQrCodes,
                        replaceQrCodes,
                        postData!!,
                        postData?.get(0)?.sourceLotId,
                        postData?.get(0)?.eudrComplaint
                    )
                )
            } else {
                prepareSuccessData(
                    if (receivingData.tmpWbId.isEmpty()) getTmpId() else receivingData.tmpWbId,
                    false)

            }
        })
    }

    private fun validateWeight() {
        when {
            receivingData.netWeight <= 0 -> showSnack(
                "The net weight value is not matched " +
                        "with the threshold weight value ${receivingData.doWeightThreshold}"
            )
            else -> {
                if (postData != null)
                    showConfirmDialog()
            }
        }
    }

    private fun updateUI(data: Resource<GenericReqAndResp<DOReceivingResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> prepareSuccessData(it.data?.data?.wbId, true)
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    requireContext().toast("${it.error}")
                }
            }
        }
    }

    /* private fun prepareSuccessData(wbId: String?) {
         hideLoading()
         receivingData.wbId = wbId ?: ""
         receivingData.tmpWbId = wbId ?: ""
         receivingData.status = Status.RECEVING_COMPLETED
         vm.saveReceiving(receivingData)
         postData.forEach { it.tmpWbId = wbId ?: "" }
         vm.saveReceivingLineItems(postData)
         moveToSuccessPage(wbId)
     }*/

    private fun prepareSuccessData(
        wbId: String?,
        syncStatus: Boolean
    ) {
        hideLoading()
        receivingData.wbId = if (syncStatus) wbId ?: "" else ""
        receivingData.tmpWbId = wbId ?: ""
        receivingData.status = if (syncStatus) Status.RECEVING_COMPLETED else Status.SYNC_PENDING
        receivingData.isSynced = syncStatus
        if (!syncStatus) {
            postData!!.forEach {
                it.tmpWbId = wbId ?: ""
                receivingData.bagType = it.bagType
                receivingData.item = it.item
            }
            vm.saveReceivingLineItems(postData!!)
        }
        vm.saveReceiving(receivingData)
        if (!isOnline()) receivingData.txnId?.let { vm.updateTransactionDetails(it) }
        moveToSuccessPage(wbId)
    }

    private fun moveToSuccessPage(wbId: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (isOnline()) intent.putExtra(TITLE, getString(R.string.receiving_success)) else intent.putExtra(
            TITLE,
            getString(R.string.receiving_success_offline)
        )
        intent.putExtra("receivingData", receivingData)
        intent.putExtra(SUB_TITLE, getString(R.string.weigh_bridge_id_is).plus(wbId))
        intent.putExtra("Weighbridgetype", receivingData.wtype)
        if(getCurrentKey().contains("DO_GH") && getCurrentKey().contains("CASH")) intent.putExtra(FROM_DO_GHANA_CASH, true)
        else intent.putExtra(FROM_DO_GHANA_CASH, false)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_message)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(R.string.cancel),
                { postReceiving() },
                { dismiss() })
        }
    }

    private fun loadSourceLotData(){
        binding.llSourceLotLabel.visibility = if(postData?.get(0)?.sourceLotId?.isNotEmpty() == true)View.VISIBLE else View.GONE
        binding.llSourceLotValues.visibility = if(postData?.get(0)?.sourceLotId?.isNotEmpty() == true)View.VISIBLE else View.GONE
        binding.tvSourceLotId.setText(postData?.get(0)?.sourceLotId)
        binding.tvEudrStatus.setText(if(postData?.get(0)?.eudrComplaint == true) Constants.COMPLAINT else Constants.NON_COMPLAINT)
    }

    private fun postReceiving() {

        //val txnId = receivingData.txnId
        /*var transcationId= doTxnDetail!!.transactionDetails?.get(0)?.transactionId
        if(transcationId==null) {
            transcationId=doTxnDetail?.lotTransactionId!!
            replaceQrCodes?.get(0)?.transactionId = doTxnDetail?.lotTransactionId!!
            vm.getDOBags(transcationId, true)
        }else{
            //postData!!.get(0).txnId=transcationId
            vm.getDOBags(transcationId, true)
          //  replaceQrCodes?.get(0)?.transactionId=transcationId
        }*/
        vm.getAllDOBagsInfo()

    }

    /*private fun postReceiving() {
        var missedQrCodes = mutableListOf<String>()
        var qrCodeMapping = mutableListOf<QrCodeMapping>()
        val plant = getPlantDetails()
        val txnId = receivingData.txnId

        if (isBltEnabled) {
            vm.getDOBags(txnId)
            vm.doBags.observe(viewLifecycleOwner, Observer {
                it?.forEach { doBag ->
                    missedQrCodes.add(doBag.bagQrCode?.toString())
                    val qrCodeMap = QrCodeMapping(
                        wbId = "",
                        tmpWbId = "",
                        newQrCode = doBag.bagQrCode.toString(),
                        oldQrCode = doBag.oldQrCode.toString()
                    )
                    qrCodeMapping.add(qrCodeMap)
                }

            })
        } else {
            if (isOnline()) {
                vm.postReceivingData(
                    DOReceivingPost(
                        getCurrentKey(),
                        plant,
                        missedQrCodes,
                        qrCodeMapping,
                        postData
                    )
                )
            } else {
                prepareSuccessData(
                    if (receivingData.tmpWbId.isEmpty()) getTmpId() else receivingData.tmpWbId,
                    false
                )
            }
        }
    }*/
}

private fun <E> MutableList<E>?.add(element: MutableList<E>) {

}
