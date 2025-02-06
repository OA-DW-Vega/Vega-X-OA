package com.olam.warehouse.vegax.offloadingcoffee.ui.mtnr

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.VegaMtntResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.common.utils.saveMtnrSequence
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants.MTNT_VENDOR_DETAIL
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcoffee.R
import com.olam.warehouse.vegax.offloadingcoffee.data.domain.model.VegaCoffeeOffloadingDeliveryDetail
import com.olam.warehouse.vegax.offloadingcoffee.data.domain.model.VegaCoffeeOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingcoffee.data.domain.model.VegaCoffeeOffloadingSupplierPostRequest
import com.olam.warehouse.vegax.offloadingcoffee.databinding.FragmentCoffeeMtnrWsSummaryBinding
import com.olam.warehouse.vegax.offloadingcoffee.databinding.ItemLotNicaraguaMtnrSummaryBinding
import com.olam.warehouse.vegax.offloadingcoffee.databinding.ItemMtnrWeighscaleLotCardLayoutBinding
import com.olam.warehouse.vegax.offloadingcoffee.databinding.OffloadingCustomEditTextCoffeeBinding
import com.olam.warehouse.vegax.offloadingcoffee.ui.VegaCoffeeOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingcoffee.ui.VegaCoffeeOffloadingViewModel
import com.olam.warehouse.vegax.offloadingcoffee.utils.*
import com.olam.warehouse.vegax.qualitycoffee.ui.VegaCoffeeQualityActivity
import com.olam.warehouse.vegax.qualitycoffee.utils.IsGain
import com.olam.warehouse.vegax.qualitycoffee.utils.IsGainWeight
import com.olam.warehouse.vegax.qualitycoffee.utils.receivingData
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class VegaCoffeeMtnrWeighScaleSummaryFragment : BaseFragment() {
    override val layoutResourceId: Int = R.layout.fragment_coffee_mtnr_ws_summary
    private lateinit var binding: FragmentCoffeeMtnrWsSummaryBinding
    private var callBack: VegaCoffeeOffloadReplaceFragmentCallback? = null
    private var bagList = arrayListOf<VegaCoffeeOffloadingBagMaterial>()
    private val vm: VegaCoffeeOffloadingViewModel by viewModel()
    private var vegaCoffeeReceivingData = VegaCoffeeReceiving()
    private var batchList = mutableListOf<VegaCoffeeReceiveLots>()
    private var isSupplier = false
    private var flag = false
    private var vegaReceiving = VegaReceiving()
    private var weighBridgeId = ""
    private var isMovedSuccess = false
    private var mtnrdocSequence = ""
    private lateinit var mtnrWithLots: VegaCoffeeReceivingMtnrWithLots
    private var isgainWeigh: Double = 0.0
    private var dialog: MaterialDialog? = null
    private var offloadPost: VegaCoffeeOffloadingPostRequest? = null

    companion object {
        fun newInstance(data: VegaCoffeeReceiving) =
            VegaCoffeeMtnrWeighScaleSummaryFragment().putArgs {
                putParcelable("summaryData", data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeOffloadReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentCoffeeMtnrWsSummaryBinding.inflate(inflater)
        if (getCurrentKey().split("_")[1].contains("NI")) {
            binding.tvOriginLabel.visibility = View.GONE
            binding.tvOriginValue.visibility = View.GONE
            binding.tvDepartmentLabel.visibility = View.GONE
            binding.tvDepartmentValue.visibility = View.GONE
            binding.tvconnaissementLabel.visibility = View.GONE
            binding.tvconnaissementValue.visibility = View.GONE
            binding.tvcooperativeLabel.visibility = View.GONE
            binding.tvcooperativeValue.visibility = View.GONE
            binding.tvGradeLabel.visible()
            binding.tvGradeValue.visible()
            binding.tvCertificateLabel.visible()
            binding.tvCertificateValue.visible()
            binding.tvGradeValue.text = grade
            binding.tvCertificateValue.text = certification
        }
        initExtra()
        initUI()
        return binding.root
    }

    /*override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingcoffee/ui/mtnr/VegaCoffeeMtnrTypeSelectFragment")
            .title("IVC/Coffee/Offloading/MTNR Weighscale Summary")
            .with(tracker)
    }*/

    private fun initUI() {
        if (getCurrentKey().split("_")[1].contains("NI")) {
            binding.ivEdit.setOnClickListener { showEditRemarkDialog() }
        } else binding.ivEdit.setOnClickListener { showRemarkDialog() }
        binding.btProceed.setOnClickListener { showConformationDialog() }
        vm.offloadingPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
        isSupplier = vegaCoffeeReceivingData.weighBridgeType.equals(PROCURE)
        binding.tvTitle.text = getString(R.string.summary_title)
        if (isSupplier) {
            binding.tvRemark.visibility = View.GONE
            binding.cvRemark.visibility = View.GONE
            binding.tvStoNumber.text = getString(R.string.driver_name)
            val driverName =
                if (vegaCoffeeReceivingData.truckDriverName.isNullOrBlank()) vegaCoffeeReceivingData.driverName else vegaCoffeeReceivingData.truckDriverName
            binding.tvStoNumberValue.text = driverName
            binding.tvDispatch.text = getString(R.string.bag_summary)
            binding.tvDest.text = getString(R.string.supplier)
            binding.tvDestValue.text =
                vegaCoffeeReceivingData.supplierCode.plus("-").plus(vegaCoffeeReceivingData.supplierName)
            binding.tvDeclaredBagCount.visibility = View.VISIBLE
            binding.tvDeclaredBagCountValue.visibility = View.VISIBLE
            binding.tvDeclaredWeight.visibility = View.VISIBLE
            binding.tvDeclaredWeightValue.visibility = View.VISIBLE
            binding.tvOriginLabel.visibility = View.VISIBLE
            binding.tvOriginValue.visibility = View.VISIBLE
            binding.tvDepartmentLabel.visibility = View.VISIBLE
            binding.tvDepartmentValue.visibility = View.VISIBLE
            binding.tvconnaissementValue.visibility = View.VISIBLE
            binding.tvcooperativeValue.visibility = View.VISIBLE
        }
        if (vegaCoffeeReceivingData.imageString == WEIGHBRIDGE_WEIHSCALE) {
            binding.tvOriginLabel.visibility = View.GONE
            binding.tvOriginValue.visibility = View.GONE
            binding.tvDepartmentLabel.visibility = View.GONE
            binding.tvDepartmentValue.visibility = View.GONE

        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaMtntResponse>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                dialog = null
                offloadPost = null
                hideLoading()
                if (response.data?.success == true) {
                    response.data?.data?.let { moveToSuccessPage(it) }
                    if (!getCurrentKey().split("_")[1].contains("NI")) {
                        PreferenceHelper.save(MTNT_VENDOR_DETAIL, "")
                        vm.updateStatus(vegaCoffeeReceivingData.delivery)
                    }
                } else {
                    if(response.data?.message?.contains("Weighbridge completed for delivery document") == true){
                        moveQualityPage()
                    }else {
                        showErrorDialogWithFAQLink(requireContext(), response.data?.message ?: "")
                    }
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                dialog = null
                offloadPost = null
                hideLoading()
                if(response.error?.contains("Weighbridge completed for delivery document") == true){
                    moveQualityPage()
                }else{
                    showErrorDialogWithFAQLink(requireContext(), response.error.toString())
                }

            }
        }
    }

    private fun initExtra() {
        vegaCoffeeReceivingData = arguments?.getParcelable("summaryData") ?: VegaCoffeeReceiving()
        if (vegaCoffeeReceivingData.dstorageLocationCode.isNullOrEmpty()) {
            binding.tvDestValue.text =
                vegaCoffeeReceivingData.supplierCode.plus(" - ")
                    .plus(vegaCoffeeReceivingData.supplierName)
        } else {
            if (getCurrentKey().split("_")[1].contains("NI")) {
                binding.tvDestValue.text = vegaCoffeeReceivingData.location.plus("-")
                    .plus(vegaCoffeeReceivingData.dstorageLocationCode).plus("-")
                    .plus(vegaCoffeeReceivingData.dstorageLocationName)
            } else {
                binding.tvDestValue.text =
                    vegaCoffeeReceivingData.dstorageLocationCode.plus("-")
                        .plus(vegaCoffeeReceivingData.dstorageLocationName)
            }
        }
        binding.tvDriverNameValue.text =
            vegaCoffeeReceivingData.storageLocationCode.plus(" - ")
                .plus(vegaCoffeeReceivingData.storageLocationName)

        binding.tvStoNumberValue.text = vegaCoffeeReceivingData.delivery
        binding.tvTruckNoValue.text = vegaCoffeeReceivingData.vehicleNumber
        binding.tvRemarkValue.text = vegaCoffeeReceivingData.remarks
        binding.tvDeclaredBagCountValue.text = vegaCoffeeReceivingData.bagCount
        if (getCurrentKey().split("_")[1].contains("NI"))
            binding.tvDeclaredWeightValue.text = vegaCoffeeReceivingData.declaredWeight
        else binding.tvDeclaredWeightValue.text =
            vegaCoffeeReceivingData.vendorDeclaredWeight.plus(vegaCoffeeReceivingData.unitsOfMeasure)
        binding.tvOriginValue.text = vegaCoffeeReceivingData.origin
        binding.tvDepartmentValue.text = vegaCoffeeReceivingData.department
        binding.tvconnaissementValue.text = vegaCoffeeReceivingData.challan
        binding.tvcooperativeValue.text = vegaCoffeeReceivingData.remarks
        if (getCurrentKey().split("_")[1].contains("NI")) {
            if (vegaCoffeeReceivingData.materialCode.isNullOrEmpty()) {
                binding.tvMaterialValue.text = vegaCoffeeReceivingData.materialName
            } else {
                binding.tvMaterialValue.text =
                    vegaCoffeeReceivingData.materialCode.plus("-")
                        .plus(vegaCoffeeReceivingData.materialName)
            }
        } else {
            binding.tvMaterialValue.text =
                vegaCoffeeReceivingData.materialCode.plus("-")
                    .plus(vegaCoffeeReceivingData.materialName)
        }

        binding.tvDrverNoValue.text = vegaCoffeeReceivingData.contactNumber
        binding.tvWeightValue.text =
            vegaCoffeeReceivingData.netWeight.plus(" ").plus(vegaCoffeeReceivingData.unitsOfMeasure)

        vm.offloadingMtnr.observe(viewLifecycleOwner, Observer {
            mtnrWithLots = it
            updateOBDDetails(it)
        })

        vm.getOBDDetails(vegaCoffeeReceivingData.delivery)
    }

    private fun updateOBDDetails(data: VegaCoffeeReceivingMtnrWithLots?) {
        batchList.clear()
        if (data != null) {
            if (!data.receiving.purchaseDocNum.isNullOrBlank()) {
                vegaCoffeeReceivingData.purchaseDocNum = data.receiving.purchaseDocNum
                vegaCoffeeReceivingData.purchaseDocQty = data.receiving.purchaseDocQty
                vegaCoffeeReceivingData.purchaseDocDesc = data.receiving.purchaseDocDesc
            }
            if (data.lineItems.isNotEmpty()) {
                data.lineItems.forEach {
                    /*var edWeight = 0.0
                    val bags = data.lineItems.filter { it1 -> it1.lots.batch.equals(it.batch) }
                    if (bags.size == 1) {
                        if (vm.vegaCoffeeReceivingData.startTime?.isEmpty() == true) vm.vegaCoffeeReceivingData.startTime =
                            DateUtils.getCurrentTimeInMills().toString()
                    }
                    if (bags.size > 0) edWeight = bags[0].bagItem.sumByDouble { it2 -> it2.netWeight.toDouble() }
                    it.editedWeight = edWeight.toString()*/
                    batchList.add(it.lots)
                    bagList.addAll(it.bagItem.filter { it2 -> it2.mtnNumber.equals(it.lots.mtnNumber) }
                        .filter { it1 ->
                            it1.batchNumber.equals(
                                it.lots.batch
                            )
                        })
                }
                if (isSupplier) setUpBagAdapter(bagList) else setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
            } else {
                if (isSupplier) setUpBagAdapter(bagList) else setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
            }
        } else {
            if (isSupplier) setUpBagAdapter(bagList) else setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
        }
    }

    private fun showEditRemarkDialog() {
        var remark = vegaCoffeeReceivingData.remarks.toString()
        showDialog(getString(R.string.edit_remark), object : DialogClick {
            override fun onPositive(remark: String) {
                val endTime = System.currentTimeMillis()
                vegaCoffeeReceivingData.endTime = endTime.toString()
                val duration = endTime.minus(vegaCoffeeReceivingData.startTime?.toLong() ?: 0)
                if (!remark.isNullOrEmpty()) {
                    vegaCoffeeReceivingData.remarks = remark
                    binding.tvRemarkValue.text = remark
                }
                vegaCoffeeReceivingData.turnAroundTime =
                    TimeUnit.MILLISECONDS.toMinutes(duration).toString()
                batchList.forEach {
                    it.delivery = vegaCoffeeReceivingData.delivery
                    vm.saveMtnrReceivingLots(vegaCoffeeReceivingData, it)
                }


                callBack?.replaceFragment(MTNR_WEIGHSCALE_SUMMARY, vegaCoffeeReceivingData)
            }

        }, true, remark)
    }

    private fun showRemarkDialog() {
        MaterialDialog(requireContext()).show {
            title(R.string.edit_remark)
            message(R.string.remark)
            cancelOnTouchOutside(false)
            var remark = ""
            val bindingDia = OffloadingCustomEditTextCoffeeBinding.inflate(layoutInflater)
            customView(R.layout.offloading_custom_edit_text_coffee)
            val editValue = this.getCustomView().findViewById<EditText>(R.id.etValue)
            if (!vegaCoffeeReceivingData.remarks.isNullOrEmpty()) editValue.setText(
                vegaCoffeeReceivingData.remarks
            )
            editValue.hint = getString(com.olam.warehouse.presentation.R.string.enter_remark)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (!editValue.text.isNullOrEmpty()) {
                        vegaCoffeeReceivingData.remarks = editValue.text.toString()
                        binding.tvRemarkValue.text = editValue.text.toString()
                        vm.saveMtnrReceivingLots(vegaCoffeeReceivingData, VegaCoffeeReceiveLots())
                    } else {
                        editValue.error =
                            getString(com.olam.warehouse.presentation.R.string.enter_remark)
                    }
                },
                { dismiss() })
        }
    }

    private fun setUpBagAdapter(list: ArrayList<VegaCoffeeOffloadingBagMaterial>) {
        val lots = list as MutableList<VegaCoffeeOffloadingBagMaterial>
        binding.rvLots.setUpAdapter(
            lots,
            R.layout.item_mtnr_weighscale_lot_card_layout,
            ItemMtnrWeighscaleLotCardLayoutBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvScaleLotValue.text = item.bagType
                bindItem.tvStLocationValue.text = item.bagCount
                bindItem.tvLotLbl.text = getString(R.string.bagmaterial)
                bindItem.tvStLocation.text = getString(R.string.bag_count)
                bindItem.tvScaleWeightValue.visibility = View.GONE
                bindItem.tvScaleGradeValue.visibility = View.GONE
                bindItem.tvAddWeight.visibility = View.GONE
                bindItem.tvWeight.visibility = View.GONE
                bindItem.tvWeightDispatch.visibility = View.GONE
                bindItem.tvScaleDispatchValue.visibility = View.GONE
                bindItem.tvGrade.visibility = View.GONE
                bindItem.tvScaleGradeValue.visibility = View.GONE

            })
    }


    private fun setUpAdapter(list: List<VegaCoffeeReceiveLots>) {
        val lots = list as MutableList
        if (list.isNotEmpty())
            calculateWeight(list)
        if (getCurrentKey().split("_")[1].contains("NI")) {
            binding.rvLots.setUpAdapter(
                lots,
                R.layout.item_lot_nicaragua_mtnr_summary,
                ItemLotNicaraguaMtnrSummaryBinding::inflate,
                { item, pos, bindItem ->
                    val mtntVendorName = PreferenceHelper.get(MTNT_VENDOR_DETAIL, "")
                    if (vegaCoffeeReceivingData.tollingVendorName!!.isNotEmpty()) {
                        bindItem.vendorLl.visible()
                        bindItem.tvVendorValue.text = vegaCoffeeReceivingData.tollingVendorCode.plus(" - ")
                            .plus(vegaCoffeeReceivingData.tollingVendorName)
                    }
                    bindItem.tvScaleLotValue.text = item.batch
                    bindItem.tvStLocationValue.text = item.storageLocationCode
                    bindItem.tvScaleWeightValue.text =
                        item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(item.uom)
                    bindItem.tvScaleGradeValue.text = item.materialName
                    val editedWeight =
                        if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString()
                            .toDouble()
                            .formatThreeDigits()
                    bindItem.tvScaleDispatchValue.text = editedWeight.plus(" ").plus(item.uom)
                    val lossvalue =
                        calculateStorageLoss(item.weight.toString(), item.editedWeight.toString()).replace("-","+")
                    receivingData.tareWeight1 = lossvalue.plus(" ").plus(item.uom)
                    if (lossvalue.toDouble() >= 0) {
                        bindItem.tvStLossheading.text = getString(R.string.transistloss)
                        IsGain = 0
                    }
                    else {
                        bindItem.tvStLossheading.text = getString(R.string.transistgain)
                        IsGain = 1
                        if (lots.size > 1) {
                            isgainWeigh = isgainWeigh + lossvalue.toDouble()
                            IsGainWeight = isgainWeigh.formatTwoDigits().toDouble()
                            receivingData.tareWeight1 = IsGainWeight.toString().replace("-","+")
                        }
                    }
                    bindItem.tvStLoss.text = lossvalue.plus(" ").plus(item.uom)


                    /* if (lossvalue.equals("0") || covertToDouble(lossvalue) == 0.0) {
                         item.hasWeightAdded = false
                     } else if (covertToDouble(lossvalue) > 0.00) {
                         item.hasWeightAdded = true
                     }*/
                    bindItem.ivEdit.setOnClickListener { showLotEditDialog(item, it) }


                })
        } else {
            binding.rvLots.setUpAdapter(
                lots,
                R.layout.item_mtnr_weighscale_lot_card_layout,
                ItemMtnrWeighscaleLotCardLayoutBinding::inflate,
                { item, pos, bindItem ->
                    bindItem.tvScaleLotValue.text = item.batch
                    bindItem.tvStLocationValue.text = item.storageLocationCode
                    bindItem.tvScaleWeightValue.text =
                        item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(item.uom)
                    bindItem.tvScaleGradeValue.text = item.materialName
                    val editedWeight =
                        if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString()
                            .toDouble()
                            .formatThreeDigits()
                    bindItem.tvScaleDispatchValue.text = editedWeight
                    bindItem.tvDispatchUOMValue.text = item.uom
                    bindItem.ivEdit.visible()
                    bindItem.tvAddWeight.visibility = View.GONE
                    bindItem.ivEdit.setOnClickListener { showLotEditDialog(item, it) }
                })
        }
    }

    private fun calculateWeight(list: List<VegaCoffeeReceiveLots>) {
        var totalWeight = 0.0
        list.forEach { totalWeight = totalWeight.plus(it.editedWeight?.toDouble() ?: 0.0) }
        if (list.isNotEmpty())
            binding.tvWeightValue.text =
                totalWeight.toString().plus(" ").plus(list[0].editedUOM)
    }

    private fun showLotEditDialog(lot: VegaCoffeeReceiveLots, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.edit_lot))
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    activity?.onBackPressed()
                    //callBack?.replaceFragment(EDIT_LOT, lot)
                },
                { dismiss() })
        }
    }

    private fun showConformationDialog() {
        if (dialog == null) {
            MaterialDialog(requireContext()).show {
                dialog = this
                cancelOnTouchOutside(false)
                cancelable(false)
                message(R.string.conform_dispatch)
                UIUtils.getMetirialCustomView(
                    this,
                    getString(com.olam.warehouse.presentation.R.string.confirm),
                    getString(com.olam.warehouse.presentation.R.string.cancel),
                    {
                        mtnrdocSequence = vm.generateMtnrSequnceNumber()
                        postDelivery()
                    },
                    {
                        dialog = null
                        dismiss()
                    })
            }
        }
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun postDelivery() {
        if (isSupplier) {
            vm.postOffloadingSupplierDetail(prepareSupplierPostData())
        } else {
            if(offloadPost==null) {
                val fPost = VegaCoffeeOffloadingPostRequest(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    operatorName = "",
                    batchNumber = "",
                    delFlag = "",
                    deliveryDetails = prepareDeliveryList(),
                    weighmentType = if (vegaCoffeeReceivingData.imageString == (WEIGHBRIDGE_WEIHSCALE)) MISC else STO,
                    vehicleNumber = vegaCoffeeReceivingData.vehicleNumber,
                    vehicleType = vegaCoffeeReceivingData.vehicleType,
                    contactNumber = vegaCoffeeReceivingData.contactNumber,
                    driverName = vegaCoffeeReceivingData.driverName,
                    remarks = vegaCoffeeReceivingData.remarks,
                    sequenceNumber = mtnrdocSequence
                )
                offloadPost = fPost
                vm.postOffloadingDetail(fPost)
            }
        }

    }

    private fun prepareDeliveryList(): List<VegaCoffeeOffloadingDeliveryDetail> {
        val list = ArrayList<VegaCoffeeOffloadingDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in batchList) {
            val deliveryDetail = VegaCoffeeOffloadingDeliveryDetail()
            var bagItems = bagList.filter { it.batchNumber == item.batch }
            deliveryDetail.batchNumber = item.batch
            deliveryDetail.materialCode = item.materialNumber
            deliveryDetail.plantId = vegaCoffeeReceivingData.plantId
            deliveryDetail.recPlantId = vegaCoffeeReceivingData.location
            deliveryDetail.remarks = vegaCoffeeReceivingData.remarks
            deliveryDetail.netWeight = item.editedWeight?.trim() ?: "0.0"
            deliveryDetail.recStorageLocationCode = vegaCoffeeReceivingData.storageLocationCode
            deliveryDetail.storageLocationCode = vegaCoffeeReceivingData.supplierCode
            deliveryDetail.purchaseDocNum = vegaCoffeeReceivingData.purchaseDocNum
            deliveryDetail.purchaseDocDesc = vegaCoffeeReceivingData.purchaseDocDesc
            deliveryDetail.delivery = item.delivery
            deliveryDetail.deliveryItem = item.posnr
            deliveryDetail.deliveryFlag = item.deliveryFlag
            deliveryDetail.pickingFlag = item.pickingFlag
            deliveryDetail.unitsOfMeasure = item.uom
            deliveryDetail.year = year.toString()
            deliveryDetail.wsGate = WS01
            deliveryDetail.weighBridgeId = vegaCoffeeReceivingData.weighBridgeId
            //Bag and pallet details
            var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
            var startTime = ""
            var endTime = ""
            bagItems.forEach { item1 ->
                val palletAvg =
                    if (item1.noOfPallet?.toInt() != 0) item1.palletWeight?.toDouble()
                        ?.div(item1.noOfPallet?.toInt()!!) else 0.0
                grossWeight = grossWeight.plus(item1.grossWeight.toDouble())
                tareWeight = tareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                    .plus(palletAvg!!)
                bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                item1.noOfPallet = if (item1.noOfPallet?.toInt() ?: 0 <= 0) "0" else item1.noOfPallet
            }
            deliveryDetail.grossWeight = grossWeight.toString().trim()
            deliveryDetail.startTime = vegaCoffeeReceivingData.startTime
            deliveryDetail.endTime = vegaCoffeeReceivingData.endTime
            deliveryDetail.turnAroundTime = vegaCoffeeReceivingData.turnAroundTime
            // deliveryDetail.storageLossFlag = item.hasWeightAdded
            if (bagItems.isNotEmpty()) {
                val bagSort = mutableListOf<VegaCoffeeOffloadingBagMaterial>()
                val dat = bagItems.sortedByDescending { it.createdPosition }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
                deliveryDetail.huno = bagItems[0].unitsOfMeasure
                deliveryDetail.huwt = bagTareWeight.formatThreeDigits().toString().trim()
                deliveryDetail.huno2 = "KG"
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumOf { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.bagList = bagItems
            } /*else {
                deliveryDetail.huno2 = summaryObj?.unitsOfMeasure
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.grossWeight = grossWeight.toString().trim()
            }*/
            list.add(deliveryDetail)
        }
        return list
    }

    private fun convertWeight(weight: String, soUom: String, lotUom: String): String {
        if (soUom == lotUom) {
            return weight
        } else {
            if (soUom == UNIT_MT && lotUom == UNIT_KG) {
                return convertKgToMT(weight)
            } else if (soUom == UNIT_KG && lotUom == UNIT_MT) return convertMtToKg(weight)
        }
        return "0"
    }

    private fun moveToSuccessPage(data: VegaMtntResponse) {
        if (!isMovedSuccess) {
            isMovedSuccess = true
            val intent = Intent(requireContext(), SuccessActivity::class.java)
            intent.putExtra(AppUtils.TITLE, getString(R.string.success_offloading))
            intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.wb_id_is, data.wbId))
            if (getCurrentKey().split("_")[1].contains("NI")) {
                intent.putExtra("offloading", true)
                weighBridgeId = data.wbId.toString()
                startActivity(intent)
            } else {
                startActivity(intent)
            }
            if (getCurrentKey().split("_")[1].contains("NI")) {
                flag = true
            } else
                requireActivity().finish()
        }
    }


    override fun onResume() {
        super.onResume()

        if (getCurrentKey().split("_")[1].contains("NI") && flag) {
            moveQualityPage()
        }

    }

    private fun moveQualityPage(){
        binding.tvTitle.text = getString(R.string.mtnr_weighscale)
        binding.moveToQuality.visibility = View.VISIBLE
        binding.btProceed.isEnabled = false
        binding.moveToQuality.isEnabled = true
        binding.btProceed.visibility = View.GONE
        binding.moveToQuality.setOnClickListener {
            vm.updateStatus(vegaCoffeeReceivingData.delivery.toString())
            val intentQ = Intent(requireContext(), VegaCoffeeQualityActivity::class.java)
            intentQ.putExtra("gradeData", grade)
            intentQ.putExtra("certificateData", certification)
            intentQ.putExtra("receivingData", prepareReceving())
            intentQ.putExtra("mtnr_wbid", weighBridgeId)
            startActivity(intentQ)
            activity?.finish()
        }
    }

    private fun prepareReceving(): VegaReceiving {
        receivingData.mtnrDocSequence = mtnrdocSequence
        receivingData.weighBridgeId = vegaCoffeeReceivingData.weighBridgeId
        receivingData.delivery = vegaCoffeeReceivingData.delivery
        receivingData.grossWeight = vegaCoffeeReceivingData.grossWeight
        receivingData.deliveryItem = vegaCoffeeReceivingData.deliveryItem
        receivingData.purchaseDocNum = vegaCoffeeReceivingData.purchaseDocNum
        receivingData.purchaseDocDesc = vegaCoffeeReceivingData.purchaseDocDesc
        receivingData.purchaseDocQty = vegaCoffeeReceivingData.purchaseDocQty
        receivingData.materialCode = vegaCoffeeReceivingData.materialCode
        receivingData.materialName = vegaCoffeeReceivingData.materialName
        receivingData.netWeight = vegaCoffeeReceivingData.netWeight
        receivingData.supplierCode = vegaCoffeeReceivingData.supplierCode
        receivingData.supplierName = vegaCoffeeReceivingData.supplierName
        receivingData.unitsOfMeasure = vegaCoffeeReceivingData.unitsOfMeasure
        receivingData.wsGate = WS01/*vegaCoffeeReceivingData.wsGate*/
        receivingData.truckDirection = vegaCoffeeReceivingData.truckDirection
        receivingData.vehicleNumber = vegaCoffeeReceivingData.vehicleNumber
        receivingData.driverName = vegaCoffeeReceivingData.truckDriverName
        receivingData.contactNumber = vegaCoffeeReceivingData.contactNumber
        receivingData.erdat = DateUtils.getCurrentDate()
        receivingData.ertim = vegaCoffeeReceivingData.ertim
        receivingData.direction = vegaCoffeeReceivingData.direction
        receivingData.storageLocationCode = vegaCoffeeReceivingData.storageLocationCode
        receivingData.storageLocationName = vegaCoffeeReceivingData.storageLocationName
        receivingData.dstorageLocationCode = vegaCoffeeReceivingData.dstorageLocationCode
        receivingData.dstorageLocationName = vegaCoffeeReceivingData.dstorageLocationName
        receivingData.declaredBagCount = vegaCoffeeReceivingData.bagCount
        receivingData.declaredWeight = vegaCoffeeReceivingData.vendorDeclaredWeight
        receivingData.vendorDeclaredWeight = vegaCoffeeReceivingData.vendorDeclaredWeight
        receivingData.origin = vegaCoffeeReceivingData.location
        receivingData.department = vegaCoffeeReceivingData.department
        receivingData.plantId = getPlantDetails().plantId
        receivingData.plantName = getPlantDetails().plantName
        receivingData.item = "1"
        receivingData.challan = vegaCoffeeReceivingData.challan
        receivingData.remarks = vegaCoffeeReceivingData.remarks
        receivingData.transportVendorCode = vegaCoffeeReceivingData.transportVendorCode
        receivingData.transportVendorName = vegaCoffeeReceivingData.transportVendorName
        receivingData.tollingVendorCode = vegaCoffeeReceivingData.tollingVendorCode
        receivingData.tollingVendorName = vegaCoffeeReceivingData.tollingVendorName
        return receivingData
    }

    private fun prepareSupplierPostData(): VegaCoffeeOffloadingSupplierPostRequest {
        val postRequest = VegaCoffeeOffloadingSupplierPostRequest(
            bagList,
            getCurrentKey(),
            if (vegaCoffeeReceivingData.imageString == WEIGHBRIDGE_WEIHSCALE) MISC else PROCURE,
            getPlantDetails(),
            vegaCoffeeReceivingData.weighBridgeId,
            vegaCoffeeReceivingData.delivery,
            vegaCoffeeReceivingData.grossWeight,
            vegaCoffeeReceivingData.deliveryItem,
            vegaCoffeeReceivingData.purchaseDocNum,
            vegaCoffeeReceivingData.purchaseDocDesc,
            vegaCoffeeReceivingData.purchaseDocQty,
            vegaCoffeeReceivingData.materialCode,
            vegaCoffeeReceivingData.materialName,
            vegaCoffeeReceivingData.netWeight,
            vegaCoffeeReceivingData.supplierCode,
            vegaCoffeeReceivingData.supplierName,
            unitsOfMeasure = vegaCoffeeReceivingData.unitsOfMeasure,
            wsGate = WS01/*vegaCoffeeReceivingData.wsGate*/,
            truckDirection = vegaCoffeeReceivingData.truckDirection,
            vehicleNumber = vegaCoffeeReceivingData.vehicleNumber,
            driverName = vegaCoffeeReceivingData.truckDriverName,
            contactNumber = vegaCoffeeReceivingData.contactNumber,
            erdat = vegaCoffeeReceivingData.erdat,
            ertim = vegaCoffeeReceivingData.ertim,
            direction = vegaCoffeeReceivingData.direction,
            storageLocationCode = vegaCoffeeReceivingData.storageLocationCode,
            storageLocationName = vegaCoffeeReceivingData.storageLocationName,
            dstorageLocationCode = vegaCoffeeReceivingData.dstorageLocationCode,
            dstorageLocationName = vegaCoffeeReceivingData.dstorageLocationName,
            declaredBagCount = vegaCoffeeReceivingData.bagCount,
            declaredWeight = vegaCoffeeReceivingData.vendorDeclaredWeight,
            vendorDeclaredWeight = vegaCoffeeReceivingData.vendorDeclaredWeight,
            origin = vegaCoffeeReceivingData.origin,
            department = vegaCoffeeReceivingData.department,
            plantName = getPlantDetails().plantName,
            item = "1",
            challan = vegaCoffeeReceivingData.challan,
            remarks = vegaCoffeeReceivingData.remarks
        )
        return postRequest
    }
}
