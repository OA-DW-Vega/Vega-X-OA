package com.olam.warehouse.vegax.mtntnicaragua.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.common.utils.saveMtntSequence
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicDispatchLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMtnt
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.master.veganicaragua.model.VegaNicDispatchLotsWithBags
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.mtntnicaragua.R
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicMtntDeliveryDetail
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicMtntDeliveryPost
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicaraguaQualityDetails
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicaraguaUpdateLotSequencePost
import com.olam.warehouse.vegax.mtntnicaragua.databinding.FragmentNicMtntWsSummaryBinding
import com.olam.warehouse.vegax.mtntnicaragua.databinding.ItemLotNicMtntSummaryBinding
import com.olam.warehouse.vegax.mtntnicaragua.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

/**
 * Created by Baskaran Kannan on 11/17/2020.
 */
class VegaNicaraguaMtntSummaryFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_nic_mtnt_ws_summary
    private lateinit var binding: FragmentNicMtntWsSummaryBinding
    private val vm: VegaNicaraguaMtntViewModel by viewModel()
    private var mtnt: VegaNicaraguaMtnt? = null
    private var callBack: Callback? = null
    var count: Int = 0
    var batchno: String = ""
    var dryMillbatchno = listOf<String>()
    var stlossvalue: Double = 0.0
    var isPostCreated: Boolean = false
    var autoTransferPlant=""
    var diglog:MaterialDialog?= null
    var vegaNicMtntDeliveryPostOneTime: VegaNicMtntDeliveryPost? = null
    private var intent = Intent()

    companion object {
        fun newInstance(vegaNicaraguaMtnt: VegaNicaraguaMtnt) =
            VegaNicaraguaMtntSummaryFragment()
                .putArgs {
                    putParcelable(MODEL_BUNDLE, vegaNicaraguaMtnt)
                }
    }

    interface Callback {
        fun replaceFragment(type: String, data: Any)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? Callback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentNicMtntWsSummaryBinding.inflate(layoutInflater)
        intent = Intent(requireContext(), SuccessActivity::class.java)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        mtnt = arguments?.getParcelable(MODEL_BUNDLE) ?: VegaNicaraguaMtnt()
        vm.mtnt = mtnt ?: VegaNicaraguaMtnt()
        if (vm.mtnt.isView == true) binding.btProceed.gone() else binding.btProceed.visible()
    }

    private fun initUI() {
        updateTruckAndConsignmentDetails()
        vm.weighBridgeLotsWithBags.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                vm.lotList.clear()
                binding.tvRemarkValue.text = it.mtnt.remarks
                val lotWithBags = it.lineItems
                vm.lotList.addAll(lotWithBags)
                if (it.lineItems.size > 1) {
                    if (MERGED)
                        updateAdapter(vm.lotList.filter { it.lots.isMergedLot == true } as ArrayList<VegaNicDispatchLotsWithBags>)
                    else updateAdapter(vm.lotList)
                }
                //  updateAdapter(vm.lotList.filter { it.lots.isMergedLot == true } as ArrayList<VegaNicDispatchLotsWithBags>)
                else
                    updateAdapter(vm.lotList)
            }
        })
        vm.getMtntWithLots(mtnt?.tempId ?: "")
        vm.getConfigItems(UserRoles.DAYS_LIMIT.role)
        vm.getAutoTransferConfigItems(UserRoles.ROLE_DISPATCH_STO.role)
        vm.configItems.observe(viewLifecycleOwner, Observer {
            updateConfigItems(it)
        })
        vm.autoTransferConfigItems.observe(viewLifecycleOwner, Observer {
            updateConfigItems(it)
        })
        binding.ivEdit.setOnClickListener { showEditRemarkDialog() }
        // binding.ivEdit.setOnClickListener { showRemarkDialog() }
        binding.btProceed.setOnClickListener { showConformationDialog() }

        vm.weighScaleDeliveryPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.updateLotSequence.observe(viewLifecycleOwner, Observer { updateLotSequnceUI(it) })

    }

    private fun updateLotSequnceUI(data: Resource<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    startActivity(intent)
                    requireActivity().finish()
                }
                Resource.Status.LOADING -> {showCustomLoading()}
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaNicMtntDeliveryDetail>>>) {
        vm.mtnt.erdat =
            if (vm.mtnt.erdat?.isEmpty() == true) DateUtils.getCurrentTimeInMills().toString() else vm.mtnt.erdat
        when (response.status) {
            Resource.Status.SUCCESS -> {
                diglog = null
                vegaNicMtntDeliveryPostOneTime = null
                var docNo = listOf<String>()
                if (response.data?.success == true) {
                    vm.mtnt.isOfflineData = false
                    vm.mtnt.isErrorStatus = true
                    vm.mtnt.isSyncStatus = true

                    docNo =
                        response.data?.data?.filter { !it.documentNum.isNullOrEmpty() }
                            ?.map { it.documentNum.toString() }?.distinct()
                            ?: listOf()
                    if (!MERGED) {
                        if (response.data?.data?.size!! > 1) {
                            dryMillbatchno = response.data?.data?.filter { !it.batchNumber.isNullOrEmpty() }
                                ?.map { it.batchNumber.toString() }?.distinct()
                                ?: listOf()
                            vm.mtnt.batchNumber = dryMillbatchno.toString().replace("[", "").replace("]", "").trim()
                        } else
                            vm.mtnt.batchNumber = response.data?.data?.get(0)?.batchNumber.toString()
                    } else {
                        vm.mtnt.batchNumber = response.data?.data?.get(0)?.mergedBatchNumber.toString()
                    }

                    //  vm.updateSyncStatus(vegaCocoaMtntWithLots!!)
                } else {
                    var message = ""
                    vm.mtnt.isOfflineData = true
                    vm.mtnt.isErrorStatus = false
                    vm.mtnt.isSyncStatus = false
                    /*var docNo = listOf<String>()
                    docNo =
                        response.data?.data?.filter { !it.documentNum.isNullOrEmpty() }
                            ?.map { it.documentNum.toString() }
                            ?: listOf()

                    if (!MERGED) {
                        if (response.data?.data?.size!! > 1) {
                            dryMillbatchno = response.data?.data?.filter { !it.batchNumber.isNullOrEmpty() }
                                ?.map { it.batchNumber.toString() }
                                ?: listOf()
                        }
                        if (dryMillbatchno.size > 1)

                            if (response.data?.data?.get(0)!!.msgList[0].contains("Delivery Number: ", true))
                                message =
                                    "WeighScaleId".plus(" " + response.data?.data?.get(0)?.weighBridgeId).plus(" ")
                                        .plus(
                                            getString(R.string.batch_no).plus(" ")
                                                .plus(dryMillbatchno.toString().replace("[", "").replace("]", ""))
                                                .plus(" ")
                                                .plus(
                                                    getString(R.string.delivery_no)
                                                        .plus(" ")
                                                        .plus(
                                                            response.data?.data?.get(0)!!.msgList[0]
                                                                .split("Batch Number:", "Delivery Number:")[2]
                                                        )
                                                )
                                        )
                            else
                                message =
                                    "WeighScaleId".plus(" " + response.data?.data?.get(0)?.weighBridgeId).plus(" ")
                                        .plus(
                                            getString(R.string.batch_no).plus(" ")
                                                .plus(dryMillbatchno.toString().replace("[", "").replace("]", ""))
                                                .plus(" ")
                                                .plus(
                                                    getString(R.string.delivery_no)
                                                        .plus(" ")
                                                        .plus(docNo)
                                                )
                                        )


                        if (response.data?.message?.contains("Error in Goods issue") == true)
                            showErrorDialogWithFAQLink(requireContext(), response.data?.message ?: "")
                        else
                            if (response.data?.message?.contains("DOFI_VEGA already processing Purchasing") == true)
                                showErrorDialogWithFAQLink(requireContext(), response.data?.message ?: "")
                            else
                                if (response.data?.message?.contains("Please check the entries. No work list selected") == true)
                                    showErrorDialogWithFAQLink(
                                        requireContext(),
                                        "Please check the entries. No work list selected"
                                    )
                                else
                                    if (response.data?.message!!.contains("already active"))
                                        showErrorDialogWithFAQLink(
                                            requireContext(), message.plus("  ").plus(
                                                getString(
                                                    R.string.mtnr_ws_failure_docu_no,
                                                    docNo.toString().replace("[", " ").replace("]", "")
                                                )
                                            )
                                        )
                                    else
                                        showErrorDialogWithFAQLink(requireContext(), response.data?.message ?: "")

                    } else {
                        message =
                            "WeighScaleId".plus(" " + response.data?.data?.get(0)?.weighBridgeId).plus(" ")
                                .plus(
                                    message.plus(
                                        getString(R.string.batch_no).plus(" ")
                                            .plus(dryMillbatchno.toString().replace("[", "").replace("]", ""))
                                            .plus(" ")
                                            .plus(
                                                getString(R.string.delivery_no)
                                                    .plus(" ")
                                                    .plus(
                                                        response.data?.data?.get(0)!!.msgList[0]
                                                            .split("Batch Number:", "Delivery Number:")[2]
                                                    )
                                            )
                                    )
                                )

                        if (response.data?.message?.contains("Error in Goods issue") == true)
                            showErrorDialogWithFAQLink(requireContext(), message.plus("  Error in Goods issue"))
                        else
                            if (response.data?.message?.contains("DOFI_VEGA already processing Purchasing") == true)
                                showErrorDialogWithFAQLink(requireContext(), response.data?.message ?: "")
                            else
                                if (response.data?.message?.contains("Please check the entries. No work list selected") == true)
                                    showErrorDialogWithFAQLink(
                                        requireContext(),
                                        "Please check the entries. No work list selected"
                                    )
                                else
                                    if (response.data?.message!!.contains("already active"))
                                        showErrorDialogWithFAQLink(
                                            requireContext(), message.plus("  ").plus(
                                                getString(
                                                    R.string.mtnr_ws_failure_docu_no,
                                                    docNo.toString().replace("[", " ").replace("]", "")
                                                )
                                            )
                                        )
                                    else
                                        showErrorDialogWithFAQLink(requireContext(), response.data?.message ?: "")


                    }*/


                }
                val success = response.data?.data ?: ArrayList()
                vm.mtnt.message = response.data?.message
                isPostCreated = false
                try {
                    for (item in success) {
                        vm.lotList.forEach /*single { it.lots.batchNumber == item.batchNumber }.apply*/ {
                            it.lots.weighScaleWbId = item.weighBridgeId
                            it.lots.pickingFlag = item.pickingFlag
                            it.lots.deliveryFlag = item.deliveryFlag
                            it.lots.deliveryItem = item.deliveryItem
                            it.lots.documentNum = item.documentNum
                            it.lots.delivery = item.delivery
                            vm.mtnt.delivery = item.delivery
                            /*if (!batchno.equals(""))
                                vm.mtnt.batchNumber = batchno
                            else
                                vm.mtnt.batchNumber = item.batchNumber*/
                            it.lots.pgiFlag = item.pgiFlag
                            it.lots.storageLossFlag = item.storageLossFlag
                        }
                        /* vm.mtnt.weighBridgeId = item.weighBridgeId
                         vm.mtnt.pickingFlag = item.pickingFlag
                         vm.mtnt.deliveryFlag = item.deliveryFlag
                         vm.mtnt.deliveryItem = item.deliveryItem
                         vm.mtnt.documentNum = item.documentNum
                         vm.mtnt.delivery = item.delivery
                         vm.mtnt.delivery = item.delivery
                         vm.mtnt.pickingFlag = item.pgiFlag
                         vm.mtnt.storageLossFlag = item.storageLossFlag*/
                    }
                    vm.saveWeighBridgeDetails()
                    vm.saveLotList(vm.lotList.map { it.lots })

                } catch (e: NoSuchElementException) {
                    e.printStackTrace()
                }
                if(vm.mtnt.isErrorStatus==false){
                    hideCustomLoading()
                    response.data?.message?.let { moveToFailurePage(it) }
                }else{
                    var BatchNo = ""
                    BatchNo = response.data?.data?.get(0)?.batchNumber.toString()
                    moveToSuccessPage(/*prepareSuccessMessage(response.data?.data)*/response.data?.data,
                        docNo,
                        response.data?.message
                    )
                    vm.postUpdateLotSequence(BatchNo)
                }
            }
            Resource.Status.LOADING -> showCustomLoading()
            Resource.Status.ERROR -> {
                diglog = null
                vegaNicMtntDeliveryPostOneTime = null
                hideCustomLoading()
                isPostCreated = false
                //showErrorDialogWithFAQLink(requireContext(), response.error.toString())
                vm.mtnt.isOfflineData = true
                vm.mtnt.isErrorStatus = false
                vm.mtnt.isSyncStatus = false
                vm.mtnt.message = response.error
                vm.saveWeighBridgeDetails()
                moveToFailurePage(response.error.toString())
            }
        }
    }

    private fun prepareSuccessMessage(data: List<VegaNicMtntDeliveryDetail>?): String {

        if (!data.isNullOrEmpty()) {
            var message = ""
            data.forEach {
                if (it.msgList.isEmpty()) return@forEach
                batchno = it.msgList[0].split("Batch Number:", "Delivery Number:")[1].replace(" ", "").trim()
                if (batchno.isNotEmpty())
                    vm.mtnt.batchNumber = batchno
                if (MERGED)
                    message =
                        "WeighScaleId".plus(" " + it.weighBridgeId).plus(" ")
                            .plus(message.plus(it.msgList[0]).plus("\n"))
                else if (dryMillbatchno.size > 1)
                    message =
                        "WeighScaleId".plus(" " + it.weighBridgeId).plus(" ")
                            .plus(
                                message.plus(
                                    getString(R.string.batch_no).plus(" ")
                                        .plus(dryMillbatchno.toString().replace("[", "").replace("]", "")).plus(" ")
                                        .plus(
                                            getString(R.string.delivery_no)
                                                .plus(" ")
                                                .plus(it.msgList[0].split("Batch Number:", "Delivery Number:")[2])
                                        )
                                )
                            )
                else if (batchno.isNullOrEmpty())
                    message =
                        "WeighScaleId".plus(" " + it.weighBridgeId).plus(" ")
                            .plus(
                                message.plus(
                                    getString(R.string.batch_no).plus(" ").plus(data.get(0).batchNumber.toString())
                                        .plus(" ")
                                        .plus(
                                            getString(R.string.delivery_no)
                                                .plus(" ")
                                                .plus(it.msgList[0].split("Batch Number:", "Delivery Number:")[2])
                                        )
                                )
                            )

            }
            return message
        }
        return ""
    }

    private fun moveToSuccessPage(
        deliveryList: List<VegaNicMtntDeliveryDetail>?,
        docNo: List<String>,
        message: String?
    ) {
        val msg = message
        val wbid = if (deliveryList?.isNotEmpty() == true) deliveryList.get(0).weighBridgeId else ""
        val documentNo = if (docNo.size > 0) docNo.get(0).toString().replace("[", "").replace("]", "") else ""
        //val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_dispatch)
        ) else intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.dispatch_success_offline)
        )
        /*intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.delivery_no).plus(deliveryId))*/
        if (!documentNo.isEmpty())

            intent.putExtra(
                AppUtils.SUB_TITLE, getString(
                    R.string.wb_id_is,
                    wbid
                ).plus(msg)
                /*getString(R.string.mtnr_ws_success, deliveryId).plus(
                    getString(
                        R.string.mtnr_ws_success_docu_no,
                        documentNo
                    )
                )*/
            )
        else if(wbid?.isEmpty() == true)  intent.putExtra(AppUtils.SUB_TITLE, msg)
        else
            intent.putExtra(
                AppUtils.SUB_TITLE, getString(
                    R.string.wb_id_is,
                    wbid
                ).plus(msg)/*getString(R.string.mtnr_ws_success, deliveryId)*/
            )
        intent.putExtra(UIUtils.FROM_NICARAGUA_COFFEE, true)
        intent.putExtra(UIUtils.FROM_NICARAGUA_UPCOUNTRY, MERGED)
        intent.putExtra(UIUtils.NICARAGUA_PRINT_TYPE, UIUtils.NICARAGUA_PRINT_MTNT_RECEIPT)
        intent.putExtra(UIUtils.MTNT_DATA, vm.mtnt)
        var lotList = arrayListOf<VegaNicDispatchLots>()
        var bagList = arrayListOf<VegaNicaraguaWeighmentBagMaterial>()
        lotList = vm.lotList.map { it.lots } as ArrayList<VegaNicDispatchLots>
        vm.lotList.forEach {
            bagList.addAll(it.bagItems)
        }
        intent.putExtra(UIUtils.MTNT_OUTPUT_DATA, lotList)
        intent.putExtra(UIUtils.BAGS_DATA, bagList)
        val truckNumber = vm.mtnt.vehicleNumber
        intent.putExtra(UIUtils.NICARAGUA_MTNT_TRUCK_NO, truckNumber)
        val obdNumber = if (deliveryList?.isNotEmpty() == true) deliveryList.get(0).delivery else ""
        vm.mtnt.obdnumber = obdNumber
        intent.putExtra(UIUtils.NICARAGUA_MTNT_OBD_NUMBER, obdNumber)
        saveMtntSequence()
        if (obdNumber?.isNotEmpty() == true) intent.putExtra(
            UIUtils.NICARAGUA_MTNT_SCAN_OBD_NUMBER,
            true
        )
       /* startActivity(intent)
        requireActivity().finish()*/
    }

    private fun moveToFailurePage(msg: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.transacci_n_fallida))
        intent.putExtra(AppUtils.SUB_TITLE, msg)
        intent.putExtra(AppUtils.FAILURE, false)
        intent.putExtra(AppUtils.MSG, msg)
        startActivity(intent)
    }

    private fun updateTruckAndConsignmentDetails() {
        binding.header.tvSendPlantValue.text = mtnt?.sendingPlant
        binding.header.tvStoNumberValue.text = mtnt?.purchaseDocNum
        binding.header.tvMaterialValue.text = mtnt?.materialName
        binding.header.tvStoQuantityValue.text = mtnt?.netWeight.plus(" ").plus(mtnt?.unitsOfMeasure)
        binding.header.tvTruckNoValue.text = mtnt?.vehicleNumber
        binding.header.tvDriverNameValue.text = mtnt?.driverName
        binding.header.tvTransVendorValue.text = mtnt?.transportVendor
        binding.header.tvDriverNoValue.text = mtnt?.contactNumber
        binding.tvRemarkValue.text = mtnt?.remarks
        binding.tvTruckValue.text = mtnt?.vehicleNumber
        //  binding.header.tvQualityGradeValue.text = mtnt?.qualityGrade.plus("-").plus(mtnt?.qulityGradeDesc)
        binding.header.tvQualityGradeValue.text = mtnt?.qulityGradeDesc
        binding.header.tvCertificationValue.text = mtnt?.certification
        if (mtnt?.vendorName?.isEmpty() == true) {
            binding.header.tvVendorName.gone()
            binding.header.tvVendorNameValue.gone()
        } else {
            binding.header.tvVendorName.visible()
            binding.header.tvVendorNameValue.visible()
            binding.header.tvVendorNameValue.text = mtnt?.vendorName
        }
    }

    private fun showRemarkDialog() {
        MaterialDialog(requireContext()).show {
            title(R.string.edit_remark)
            message(R.string.remarks)
            cancelOnTouchOutside(false)
            var remark = ""
            customView(R.layout.vega_nic_mtnt_custom_edit_text)
            val editValue = this.getCustomView().findViewById<EditText>(R.id.etValue)
            if (!vm.mtnt.remarks.isNullOrEmpty()) editValue.setText(vm.mtnt.remarks)
            editValue.hint = getString(com.olam.warehouse.presentation.R.string.enter_remark)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (!editValue.text.isNullOrEmpty()) {
                        vm.mtnt.remarks = editValue.text.toString()
                        binding.tvRemarkValue.text = editValue.text.toString()
                        vm.saveWeighBridgeDetails()
                    } else {
                        editValue.error =
                            getString(com.olam.warehouse.presentation.R.string.enter_remark)
                    }
                },
                { dismiss() })
        }
    }

    private fun showEditRemarkDialog() {
        var remark = vm.mtnt.remarks.toString()
        showDialog(getString(R.string.edit_remark), object : DialogClick {
            override fun onPositive(remark: String) {
                if (!remark.isNullOrEmpty()) {
                    vm.mtnt.remarks = remark
                    binding.tvRemark.text = remark
                }
                moveToSummary(remark)
            }

        }, true, remark)
    }

    private fun moveToSummary(remark: String) {
        vm.mtnt.endTime = DateUtils.getCurrentTimeInMills().toString()
        vm.saveWeighBridgeDetails()
        vm.updateLotDetails()
        callBack?.replaceFragment(FRAG_SUMMARY, vm.mtnt)
    }

    private fun updateAdapter(lotlist: ArrayList<VegaNicDispatchLotsWithBags>) {
        binding.rvLots.setUpAdapter(
            lotlist,
            R.layout.item_lot_nic_mtnt_summary,
            ItemLotNicMtntSummaryBinding::inflate,
            { it, pos, bindItem ->
                if (MERGED) {
                    val lot = it.lots
                    if (it.bagItems.size > 0) lot.editedWeight =
                        it.bagItems.sumOf { if (it.netWeight.isNotEmpty()) it.netWeight.toDouble() else 0.0 }
                            .formatThreeDigits()
                    else lot.editedWeight = "0"

                    vm.lotList[pos].lots.editedWeight = lot.editedWeight
                    vm.mtnt.mergedNetWeight = lot.editedWeight

                    bindItem.tvLotId.text = lot.batchNumber
                    // vm.lotList[pos].lots.isEndLot = true
                    bindItem.tvWeightValue.text =
                        lot.weight.plus(" ").toDouble().formatThreeDigits().plus(lot.unitOfMeasure)
                    if (lot.materialName?.contains("TOLLING", true) == true) {
                        bindItem.vendorLl.visible()
                        if (lot.vendorName!!.isNotEmpty())
                            bindItem.tvVendorValue.text = lot.vendorName
                        else {
                            bindItem.tvVendorValue.text = mtnt?.vendorName
                            lot.vendorName = mtnt?.vendorName
                        }
                    }
                    if (lot.isMergedLot!!) {
                        bindItem.StLocationLabel.visibility = View.INVISIBLE
                        bindItem.tvStLocation.visibility = View.INVISIBLE
                    }
                    bindItem.tvStLocation.text = lot.storageLocationCode
                    bindItem.tvGradeValue.text = lot.materialName
                    bindItem.tvWeightToProcessValue.text =
                        lot.editedWeight.plus(" ").plus(lot.unitOfMeasure)
                    /*  tvStLoss.text =
                    if (lot.isEndLot == true) calculateStorageLoss(lot.weight.toString(), lot.editedWeight.toString()).plus(
                        " "
                    )
                        .plus(lot.unitOfMeasure) else "0".plus(lot.unitOfMeasure)*/

                    val lossvalue =
                        calculateStorageLoss(lot.weight.toString(), lot.editedWeight.toString())
                    if (lossvalue.equals("0") || covertToDouble(lossvalue) == 0.0) {
                        vm.lotList[0].lots.storageLossFlag = true
                        bindItem.llStorageLoss.visible()
                    } else if (covertToDouble(lossvalue) > 0.00) {
                        vm.lotList[0].lots.storageLossFlag = false
                        bindItem.llStorageLoss.visible()
                    }


                    bindItem.tvStLoss.text = lossvalue.plus(" ").plus(lot.unitOfMeasure)
                    if (vm.lotList[pos].lots.isEndLot == true)
                        bindItem.llStorageLoss.visible()
                    else if (islotMERGED == true && MERGED == true)
                        bindItem.llStorageLoss.visible()
                    else
                        bindItem.llStorageLoss.gone()

                    stlossvalue = lossvalue.toDouble()

                    bindItem.ivEdit.setOnClickListener {
                        showLotEditDialog(lot, it)
                    }
                } else {
                    val lot = vm.lotList
                    bindItem.tvLotId.text = vm.lotList[pos].lots.batchNumber
                    vm.mtnt.mergedNetWeight = ""
                    bindItem.tvWeightValue.text =
                        vm.lotList[pos].lots.weight?.toDouble()?.formatThreeDigits().plus(" ")
                            .plus(vm.lotList[pos].lots.unitOfMeasure)
                    bindItem.tvStLocation.text = vm.lotList[pos].lots.storageLocationCode
                    bindItem.tvGradeValue.text = vm.lotList[pos].lots.materialName
                    bindItem.tvWeightToProcessValue.text =
                        vm.lotList[pos].lots.editedWeight.plus(" ").plus(vm.lotList[pos].lots.unitOfMeasure)
                    /*  tvStLoss.text =
                    if (lot.isEndLot == true) calculateStorageLoss(lot.weight.toString(), lot.editedWeight.toString()).plus(
                        " "
                    )
                        .plus(lot.unitOfMeasure) else "0".plus(lot.unitOfMeasure)*/
                    if (vm.lotList[pos].lots.materialName?.contains("TOLLING", true) == true) {
                        bindItem.vendorLl.visible()
                        bindItem.tvVendorValue.text = vm.lotList[pos].lots.vendorName
                    }
                    val lossvalue = calculateStorageLoss(
                        vm.lotList[pos].lots.weight.toString(),
                        vm.lotList[pos].lots.editedWeight.toString()
                    )

                    //vm.lotList[pos].lots.storageLossFlag = vm.lotList[pos].lots.isEndLot

                    if (vm.lotList[pos].lots.isEndLot == true)
                        bindItem.llStorageLoss.visible()
                    else if (islotMERGED == true && MERGED == true)
                        bindItem.llStorageLoss.visible()
                    else
                        bindItem.llStorageLoss.gone()

                    bindItem.tvStLoss.text = lossvalue.plus(" ").plus(vm.lotList[pos].lots.unitOfMeasure)
                    stlossvalue = lossvalue.toDouble()


                    bindItem.ivEdit.setOnClickListener {
                        showLotEditDialog(vm.lotList[pos].lots, it)
                    }
                }
            })
    }

    private fun showLotEditDialog(lot: VegaNicDispatchLots, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.edit_lot))
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    //activity?.onBackPressed()
                    callBack?.replaceFragment(EDIT_LOT, lot)
                },
                { dismiss() })
        }
    }

    private fun showConformationDialog() {
        if (diglog==null)
            MaterialDialog(requireContext()).show {
                diglog = this
                cancelOnTouchOutside(false)
                cancelable(false)
                message(R.string.conform_dispatch)
                UIUtils.getMetirialCustomView(
                    this,
                    getString(com.olam.warehouse.presentation.R.string.confirm),
                    getString(com.olam.warehouse.presentation.R.string.cancel),
                    {
                        if (AppUtils.isOnline()) {
                            postDelivery()
                        } else
                            moveToOfflineSuccessPage()
                    },
                    {
                        diglog = null
                        dismiss() })
            }
    }

    private fun postDelivery() {
        if (DateUtils.isFirstDayOfMonth(requireContext(), count) && vegaNicMtntDeliveryPostOneTime ==null) {
            val vegaNicMtntDeliveryPost = VegaNicMtntDeliveryPost(
                getCurrentKey(),
                getPlantDetails(),
                "",
                prepareDeliveryList(),
                WEIGHSCALE,
                vm.mtnt.contactNumber.toString(),
                vm.mtnt.driverName.toString(),
                vm.mtnt.vehicleNumber.toString(),
                vm.mtnt.remarks.toString(),
                vm.mtnt.vehicleType.toString(), vm.mtnt.mergedNetWeight.toString(),
                vm.mtnt.mtntDocSequence.toString()
            )
            vegaNicMtntDeliveryPostOneTime = vegaNicMtntDeliveryPost
            isPostCreated = true
            val plants= autoTransferPlant.split(",")
            if(plants.isNotEmpty()) {
               // plants[0] is 1550 and plants[1] is 1569
                if (vegaNicMtntDeliveryPost.deliveryDetails[0].plantId == plants[0] && vegaNicMtntDeliveryPost.deliveryDetails[0].recPlantId == plants[1])
                    vegaNicMtntDeliveryPost.autotransferFlag = true
            }

            vm.postNicMtntWSDeliveryDetails(vegaNicMtntDeliveryPost)
        } else {
            showSnack(getString(R.string.month_close_error))
        }
    }

    private fun prepareDeliveryList(): List<VegaNicMtntDeliveryDetail> {
        val list = ArrayList<VegaNicMtntDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        vm.lotList.filter { it.lots.isMergedLot == false }.forEach { item ->
            val deliveryDetail = VegaNicMtntDeliveryDetail()
            var bagItems =
                item.bagItems.filter { it.batchNumber == item.lots.batchNumber /*&& it.baseMaterial == item.materialCode */ }
            // if (item.lots.materialName!!.contains("Tolling", true))
            //deliveryDetail.toVendorCode = if (item.lots.vendorCode.isNullOrEmpty()) "" else "000" + item.lots.vendorCode
            var addedbadsize = item.bagItems.sumOf { it.bagCount.toInt() }
            deliveryDetail.toVendorCode = vm.mtnt.transportVendorID
            deliveryDetail.batchNumber = item.lots.batchNumber
            deliveryDetail.isPartially = item.lots.Partially
            // dryMillbatchno = item.lots.batchNumber
            deliveryDetail.materialCode = item.lots.materialCode
            deliveryDetail.plantId = item.lots.plantId
            deliveryDetail.recPlantId = vm.mtnt.recPlantId
            deliveryDetail.createdDate = vm.mtnt.startTime
            deliveryDetail.purchaseDocNum = vm.mtnt.purchaseDocNum
            deliveryDetail.purchaseDocDesc = vm.mtnt.purchaseDocDesc
            deliveryDetail.recStorageLocationCode = item.lots.storageLocationCode
            deliveryDetail.storageLocationCode = item.lots.storageLocationCode
            deliveryDetail.unitsOfMeasure = item.lots.unitOfMeasure
            deliveryDetail.startTime = vm.mtnt.startTime
            deliveryDetail.endTime = vm.mtnt.endTime
            deliveryDetail.turnAroundTime = vm.mtnt.turnAroundTime ?: "0"
            deliveryDetail.weighBridgeId = item.lots.weighScaleWbId
            deliveryDetail.deliveryFlag = vm.mtnt.deliveryFlag ?: false
            deliveryDetail.pickingFlag = vm.mtnt.pickingFlag ?: false
            if (islotMERGED == true)
                item.lots.isEndLot = stlossvalue > 0.0
            else
                item.lots.isEndLot = item.lots.isEndLot
            deliveryDetail.endLotFlag = item.lots.isEndLot == true

//            deliveryDetail.isPgiFlag = vm.mtnt.isPgiFlag
            deliveryDetail.storageLossFlag = !deliveryDetail.endLotFlag

            //  deliveryDetail.storageLossFlag = vm.mtnt.storageLossFlag
            deliveryDetail.delivery = item.lots.delivery
            deliveryDetail.deliveryItem = item.lots.deliveryItem
            deliveryDetail.year = year.toString()
            //Bag and pallet details
            var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
            /*var startTime = ""
            var endTime = ""*/
            bagItems.forEach { item1 ->
                grossWeight = grossWeight.plus(item1.grossWeight.toDouble())
                tareWeight = tareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                /*if (item1.startTime?.isNotEmpty()!!) startTime = item1.startTime.toString()
                if (item1.endTime?.isNotEmpty()!!) endTime = item1.endTime.toString()*/
                //item1.noOfPallet = if (item1.noOfPallet?.toInt() ?: 0 <= 0) "0" else item1.noOfPallet
            }

            if (MERGED)
                deliveryDetail.netWeight = item.lots.weight
            else
                deliveryDetail.netWeight = item.lots.editedWeight
            // deliveryDetail.netWeight = item.lots.weight
            if (bagItems.isNotEmpty()) {
                val bagSort = arrayListOf<VegaNicaraguaWeighmentBagMaterial>()
                val dat = bagItems.sortedByDescending { it.status }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
                deliveryDetail.huno = bagItems[0].unitsOfMeasure
                deliveryDetail.huwt = bagTareWeight.toString().trim()
                deliveryDetail.huno2 = "KG"
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumOf { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.grossWeight = grossWeight.toString().trim()
                deliveryDetail.bagList = bagItems

            } else {
                deliveryDetail.huwt = "0.0"
                deliveryDetail.huwt2 = "0.0"
                deliveryDetail.huno2 = vm.mtnt.unitsOfMeasure
                deliveryDetail.grossWeight = grossWeight.toString().trim()
            }
            var qualityList = ArrayList<VegaNicaraguaQualityDetails>()
            if (MERGED)
                deliveryDetail.qualityDetails = emptyList()
            else {
                var qc = VegaNicaraguaQualityDetails()
                qc.sapQCDesc = mtnt!!.deschar
                qc.sapQCName = mtnt!!.namechar
                qc.satNam = ((if(item.lots.availbagCount.isNullOrEmpty()) "0" else item.lots.availbagCount ?: "0").toInt() - addedbadsize).toString()
                qualityList.add(qc)
                deliveryDetail.qualityDetails = qualityList
            }

            list.add(deliveryDetail)
        }
        return list
    }

    private fun moveToOfflineSuccessPage() {
        vm.mtnt.isOfflineData = true
        vm.mtnt.message = getString(R.string.stored_local)
        vm.mtnt.erdat =
            if (vm.mtnt.erdat?.isEmpty() == true) DateUtils.getCurrentTimeInMills().toString() else vm.mtnt.erdat
        vm.saveWeighBridgeDetails()
        saveMtntSequence()
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.dispatch_success_offline))
        intent.putExtra(
            AppUtils.SUB_TITLE,
            getString(R.string.dispatch_tmp_id, vm.mtnt.tempId.toString())
        )
        intent.putExtra(UIUtils.MTNT_DATA, vm.mtnt)
        var lotList = arrayListOf<VegaNicDispatchLots>()
        var bagList = arrayListOf<VegaNicaraguaWeighmentBagMaterial>()
        lotList = vm.lotList.map { it.lots } as ArrayList<VegaNicDispatchLots>
        vm.lotList.forEach {
            bagList.addAll(it.bagItems)
        }
        intent.putExtra(UIUtils.FROM_NICARAGUA_COFFEE, true)
        intent.putExtra(UIUtils.NICARAGUA_PRINT_TYPE, UIUtils.NICARAGUA_PRINT_MTNT_RECEIPT)
        intent.putExtra(UIUtils.MTNT_OUTPUT_DATA, lotList)
        intent.putExtra(UIUtils.BAGS_DATA, bagList)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.DAYS_LIMIT.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> {
                            if (!it.value!!.isNullOrEmpty())
                                count = it.value!!.toInt()
                            else
                                count = 0
                        }
                        it.applicable?.contains("N")!! -> {
                            count = 0
                        }
                    }
                }
                ConfigItems.AUTO_TRANSFER.item ->{
                    if(it.applicable?.contains("Y") == true){
                        autoTransferPlant= it.value.toString()
                    }

                }

            }
        }
    }
    fun onBackRefreshed() {
        if (isPostCreated)
        {
            Toast.makeText(
                requireActivity(),
                getString(R.string.transaction_incomplete),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
