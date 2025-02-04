package com.olam.warehouse.vegax.mtntnicaragua.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicDispatchLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMtnt
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.master.veganicaragua.model.VegaNicDispatchLotsWithBags
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.mtntnicaragua.R
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicMtntDeliveryDetail
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicMtntDeliveryPost
import com.olam.warehouse.vegax.mtntnicaragua.databinding.FragmentNicMtntWsSummaryBinding
import com.olam.warehouse.vegax.mtntnicaragua.utils.*
import kotlinx.android.synthetic.main.item_lot_nic_mtnt_summary.view.*
import kotlinx.android.synthetic.main.vega_nic_mtnt_custom_edit_text.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 11/17/2020.
 */
class VegaNicaraguaMtntSummaryFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_nic_mtnt_ws_summary
    private lateinit var binding: FragmentNicMtntWsSummaryBinding
    private val vm: VegaNicaraguaMtntViewModel by viewModel()
    private var mtnt: VegaNicaraguaMtnt? = null
    private var callBack: Callback? = null
    var count:Int=0

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
                if(it.lineItems.size>1)
                    updateAdapter(vm.lotList.filter { it.lots.isMergedLot==true } as ArrayList<VegaNicDispatchLotsWithBags>)
                else
                    updateAdapter(vm.lotList)
            }
        })
        vm.getMtntWithLots(mtnt?.tempId ?: "")
        vm.getConfigItems(UserRoles.DAYS_LIMIT.role)
        vm.configItems.observe(this, androidx.lifecycle.Observer {
            updateConfigItems(it)
        })

        binding.ivEdit.setOnClickListener { showRemarkDialog() }
        binding.btProceed.setOnClickListener { showConformationDialog() }

        vm.weighScaleDeliveryPost.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateUI(it) })

    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaNicMtntDeliveryDetail>>>) {
        vm.mtnt.erdat =
            if (vm.mtnt.erdat?.isEmpty() == true) DateUtils.getCurrentTimeInMills().toString() else vm.mtnt.erdat
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                if (response.data?.success == true) {
                    vm.mtnt.isOfflineData = false
                    vm.mtnt.isErrorStatus = true
                    vm.mtnt.isSyncStatus = true
                    var docNo = listOf<String>()
                    docNo =
                        response.data?.data?.filter { !it.documentNum.isNullOrEmpty() }
                            ?.map { it.documentNum.toString() }
                            ?: listOf()
                    moveToSuccessPage(prepareSuccessMessage(response.data?.data), docNo)
                    //  vm.updateSyncStatus(vegaCocoaMtntWithLots!!)
                } else {
                    vm.mtnt.isOfflineData = true
                    vm.mtnt.isErrorStatus = false
                    vm.mtnt.isSyncStatus = false
                    UIUtils.showErrorDialog(requireContext(), response.data?.message ?: "")
                }
                val success = response.data?.data ?: ArrayList()
                vm.mtnt.message = response.data?.message
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
                            vm.mtnt.batchNumber = item.batchNumber
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
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
                vm.mtnt.isOfflineData = true
                vm.mtnt.isErrorStatus = false
                vm.mtnt.isSyncStatus = false
                vm.mtnt.message = response.error
                vm.saveWeighBridgeDetails()
            }
        }
    }

    private fun prepareSuccessMessage(data: List<VegaNicMtntDeliveryDetail>?): String {

        if (!data.isNullOrEmpty()) {
            var message = ""
            data.forEach {
                if (it.msgList.isEmpty()) return@forEach
                message =
                    "WeighScaleId".plus(" " + it.weighBridgeId).plus(" ").plus(message.plus(it.msgList[0]).plus("\n"))
            }
            return message
        }
        return ""
    }

    private fun moveToSuccessPage(deliveryId: String?, docNo: List<String>) {
        val documentNo = if (docNo.size > 0) docNo.toString().replace("[", "").replace("]", "") else ""
        val intent = Intent(requireContext(), SuccessActivity::class.java)
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
                AppUtils.SUB_TITLE,
                getString(R.string.mtnr_ws_success, deliveryId).plus(
                    getString(
                        R.string.mtnr_ws_success_docu_no,
                        documentNo
                    )
                )
            )
        else
            intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.mtnr_ws_success, deliveryId))
        intent.putExtra(UIUtils.FROM_NICARAGUA_COFFEE, true)
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
        val obdArray = deliveryId?.split(":")
        val obdNumber = obdArray?.get(2)?.trim()
        intent.putExtra(UIUtils.NICARAGUA_MTNT_OBD_NUMBER, obdNumber)
        intent.putExtra(UIUtils.NICARAGUA_MTNT_SCAN_OBD_NUMBER, true)

        startActivity(intent)
        requireActivity().finish()
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
        binding.header.tvQualityGradeValue.text = mtnt?.qualityGrade.plus("-").plus(mtnt?.qulityGradeDesc)
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
            val editValue = this.getCustomView().etValue
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

    private fun updateAdapter(lotlist:ArrayList<VegaNicDispatchLotsWithBags>) {
        binding.rvLots.setUp(lotlist, R.layout.item_lot_nic_mtnt_summary, { it, pos ->
             val lot = it.lots
            if (it.bagItems.size > 0)
                lot.editedWeight =
                    it.bagItems.sumByDouble { if (it.netWeight.isNotEmpty()) it.netWeight.toDouble() else 0.0 }
                        .formatThreeDigits()
            else lot.editedWeight = "0"
            vm.lotList[pos].lots.editedWeight = lot.editedWeight
            vm.mtnt.mergedNetWeight=lot.editedWeight
            tvLotId.text = lot.batchNumber
            vm.lotList[pos].lots.isEndLot=true
            tvWeightValue.text = lot.weight.plus(" ").plus(lot.unitOfMeasure)
            if(lot.isMergedLot!!)
            {
                StLocationLabel.visibility=View.INVISIBLE
                tvStLocation.visibility=View.INVISIBLE
            }
            tvStLocation.text = lot.storageLocationCode
            tvGradeValue.text = lot.materialName
            tvWeightToProcessValue.text = lot.editedWeight.plus(" ").plus(lot.unitOfMeasure)
          /*  tvStLoss.text =
                if (lot.isEndLot == true) calculateStorageLoss(lot.weight.toString(), lot.editedWeight.toString()).plus(
                    " "
                )
                    .plus(lot.unitOfMeasure) else "0".plus(lot.unitOfMeasure)*/

               val lossvalue=  calculateStorageLoss(lot.weight.toString(),lot.editedWeight.toString())
                if(lossvalue.equals("0")||covertToDouble(lossvalue)==0.0)
                {
                    vm.lotList[0].lots.storageLossFlag=true
                }
                else if(covertToDouble(lossvalue)>0.00)
                {
                    vm.lotList[0].lots.storageLossFlag=false
                }
                tvStLoss.text =lossvalue.plus(" ").plus(lot.unitOfMeasure)


            ivEdit.setOnClickListener {
                showLotEditDialog(lot, it)
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
        MaterialDialog(requireContext()).show {
            message(R.string.conform_dispatch)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (AppUtils.isOnline())
                        postDelivery()
                    else
                        moveToOfflineSuccessPage()
                },
                { dismiss() })
        }
    }

    private fun postDelivery() {
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
            vm.mtnt.vehicleType.toString()
        ,vm.mtnt.mergedNetWeight.toString()
        )
        if (DateUtils.isFirstDayOfMonth(context!!,count)) {
            vm.postNicMtntWSDeliveryDetails(vegaNicMtntDeliveryPost)
        } else {
            showSnack(getString(R.string.month_close_error))
        }

    }

    private fun prepareDeliveryList(): List<VegaNicMtntDeliveryDetail> {
        val list = ArrayList<VegaNicMtntDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        vm.lotList.filter { it.lots.isMergedLot==false }.forEach { item ->
            val deliveryDetail = VegaNicMtntDeliveryDetail()
            var bagItems =
                item.bagItems.filter { it.batchNumber == item.lots.batchNumber /*&& it.baseMaterial == item.materialCode */ }
            deliveryDetail.batchNumber = item.lots.batchNumber
            deliveryDetail.materialCode = item.lots.materialCode
            deliveryDetail.plantId = item.lots.plantId
            deliveryDetail.recPlantId = vm.mtnt.recPlantId
            deliveryDetail.netWeight = item.lots.weight
            deliveryDetail.endLotFlag = true
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
//            deliveryDetail.isPgiFlag = vm.mtnt.isPgiFlag
            deliveryDetail.storageLossFlag = item.lots.storageLossFlag ?: false
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
            if (bagItems.isNotEmpty()) {
                val bagSort = arrayListOf<VegaNicaraguaWeighmentBagMaterial>()
                val dat = bagItems.sortedByDescending { it.status }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
                deliveryDetail.huno = bagItems[0].unitsOfMeasure
                deliveryDetail.huwt = bagTareWeight.toString().trim()
                deliveryDetail.huno2 = "KG"
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.grossWeight = grossWeight.toString().trim()
                deliveryDetail.bagList = bagItems
            } else {
                deliveryDetail.huno2 = vm.mtnt.unitsOfMeasure
                deliveryDetail.grossWeight = grossWeight.toString().trim()
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
                            if(!it.value!!.isNullOrEmpty())
                                count=it.value!!.toInt()
                            else
                                count=0
                        }
                        it.applicable?.contains("N")!! -> {
                            count=0
                        }
                    }
                }
            }
        }
    }


}
