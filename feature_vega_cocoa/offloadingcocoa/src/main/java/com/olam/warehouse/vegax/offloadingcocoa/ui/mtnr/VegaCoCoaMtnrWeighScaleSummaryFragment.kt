package com.olam.warehouse.vegax.offloadingcocoa.ui.mtnr

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64.*
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
import com.olam.warehouse.master.common.utils.getBase64FromFile
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vegacocoa.entity.BagTypeList
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaOffloadingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiveLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiving
import com.olam.warehouse.master.vegacocoa.model.VegaCoCoaReceivingMtnrWithLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcocoa.R
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.model.VegaCoCoaOffloadingDeliveryDetail
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.model.VegaCoCoaOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.model.VegaCocoOffloadingSupplierPostRequest
import com.olam.warehouse.vegax.offloadingcocoa.databinding.FragmentCocoaMtnrSummaryBinding
import com.olam.warehouse.vegax.offloadingcocoa.databinding.ItemMtnrSummaryLotCardLayoutBinding
import com.olam.warehouse.vegax.offloadingcocoa.databinding.OffloadingCocoaCustomEditTextBinding
import com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingcocoa.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.absoluteValue


class VegaCoCoaMtnrWeighScaleSummaryFragment : BaseFragment() {
    override val layoutResourceId: Int = R.layout.fragment_cocoa_mtnr_summary
    private lateinit var binding: FragmentCocoaMtnrSummaryBinding
    private var callBack: VegaCoCoaOffloadReplaceFragmentCallback? = null
    private var bagList = arrayListOf<VegaCoCoaOffloadingBagMaterial>()
    private val vm: VegaCoCoaOffloadingViewModel by viewModel()
    private var vegaCoCoaReceivingData = VegaCoCoaReceiving()
    private var batchList = mutableListOf<VegaCoCoaReceiveLots>()
    private var transit: Double = 0.0
    private var grnFlag: Boolean = false
    private var weighBridgeId: String = ""
    val bagTypeList = arrayListOf<BagTypeList>()

    companion object {
        fun newInstance(data: VegaCoCoaReceiving) =
            VegaCoCoaMtnrWeighScaleSummaryFragment().putArgs {
                putParcelable("summaryData", data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoCoaOffloadReplaceFragmentCallback
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingcocoa/ui/mtnr/VegaCoCoaMtnrWeighScaleSummaryFragment")
            .title("Mtnr CoCoa")
            .with(tracker)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentCocoaMtnrSummaryBinding.inflate(inflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initUI() {
        binding.ivEdit.setOnClickListener { showRemarkDialog() }
        binding.btProceed.setOnClickListener { showConformationDialog() }
        vm.offloadingPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.offloadingSupplierPost.observe(viewLifecycleOwner, Observer { updateSupplierUI(it) })
        settingTrackTracevalue()
    }

    private fun settingTrackTracevalue(){
        /*indirect flow*/
        if(vegaCoCoaReceivingData.sourceLotId.isNotEmpty()){
            binding.tvSourceLotId.visible()
            binding.tvSourceLotIdValue.visible()
            binding.tvEudrStatus.visible()
            binding.tvEudrStatusValue.visible()
            binding.tvSourceLotIdValue.setText(vegaCoCoaReceivingData.sourceLotId)
            if(vegaCoCoaReceivingData.eudrStatus){
                binding.tvEudrStatusValue.setText(Constants.EUDR_QP_VALUE)
            } else {
                binding.tvEudrStatusValue.setText(Constants.ATTR_UNKNOWN_QP_VALUE)
            }
        }
        /*direct flow*/
        if(vegaCoCoaReceivingData.ttFarmerList.size>0){
            binding.tvEudrStatus.visible()
            binding.tvEudrStatusValue.visible()
            if(vegaCoCoaReceivingData.eudrStatus){
                binding.tvEudrStatusValue.setText(Constants.EUDR_QP_VALUE)
            } else {
                binding.tvEudrStatusValue.setText(Constants.ATTR_UNKNOWN_QP_VALUE)
            }
        }

        /*indirect flow*/
        if(vegaCoCoaReceivingData.farmerLessTransactionId?.isNotEmpty() == true){
            binding.tvSourceLotId.visible()
            binding.tvSourceLotIdValue.visible()
            binding.tvEudrStatus.visible()
            binding.tvEudrStatusValue.visible()
            binding.tvSourceLotId.setText(getString(R.string.farmerless_transaction_id))
            binding.tvSourceLotIdValue.setText(vegaCoCoaReceivingData.farmerLessTransactionId)
            if(vegaCoCoaReceivingData.eudrStatus){
                binding.tvEudrStatusValue.setText(Constants.EUDR_QP_VALUE)
            } else {
                binding.tvEudrStatusValue.setText(Constants.ATTR_UNKNOWN_QP_VALUE)
            }
        }
    }

    private fun updateSupplierUI(it: Resource<GenericReqAndResp<VegaMtntResponse>>?) {
        it?.let { response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    // hideCustomLoading()
                    val res = response.data
//                    vegaCoffeeReceivingData.batchNumber =
//                        if (!res?.data?.batchNumber.isNullOrEmpty()) res?.data?.batchNumber else vegaCoffeeReceivingData.batchNumber
//                    vegaCoffeeReceivingData.wbFlag = res?.data?.wbFlag
//                    vegaCoffeeReceivingData.batchCharFlag = res?.data?.batchCharFlag
                    if (response.data?.success == true) {
                        vegaCoCoaReceivingData.syncStatusMsg = res?.message
                        vegaCoCoaReceivingData.isSynced = true
                        vegaCoCoaReceivingData.status = Status.SYNC_COMPLETED
                        response.data?.data?.let { moveToSupplierSuccessPage(it) }
                        //vm.updateStatus(vegaCoffeeReceivingData.delivery.toString())
                    } else {
                        vegaCoCoaReceivingData.syncStatusMsg = res?.message
                        vegaCoCoaReceivingData.isSynced = false
                        vegaCoCoaReceivingData.status = Status.SYNC_ERROR
                        showErrorDialogWithFAQLink(requireContext(), response.data?.message ?: "")
                        //UIUtils.showErrorDialog(requireContext(), response.data?.message ?: "")
                    }
                    if (!res?.data?.wbId.isNullOrEmpty()) vegaCoCoaReceivingData.weighBridgeId =
                        res?.data?.wbId.toString()
                    //vm.saveMtnrReceivingLots(vegaCoffeeReceivingData, VegaOffloadingReceiveLots())
                }

                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    vegaCoCoaReceivingData.syncStatusMsg = response.error.toString()
                    vegaCoCoaReceivingData.isSynced = false
                    vegaCoCoaReceivingData.status = Status.SYNC_ERROR
//                    vm.saveMtnrReceivingLots(vegaCoffeeReceivingData, VegaOffloadingReceiveLots())
                    showErrorDialogWithFAQLink(requireContext(), response.error.toString())
                    //UIUtils.showErrorDialog(requireContext(), response.error.toString())
                }
            }
        }
    }


    private fun updateUI(response: Resource<GenericReqAndResp<VegaCoCoaOffloadingPostRequest>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {

                val respData = response.data?.data
                val isSuccess = response.data?.success ?: true
                if (respData != null && isSuccess) {
                    vegaCoCoaReceivingData.tempGrnNumber = respData.grnNumber.toString()
                    vegaCoCoaReceivingData.transitLossDocNo =
                        if (!respData.lotDetails.isNullOrEmpty()) respData.lotDetails[0].documentNum else ""
                    vegaCoCoaReceivingData.isSynced = true
                    vegaCoCoaReceivingData.isProgress = false
                    vegaCoCoaReceivingData.syncStatusMsg = response.data?.message
                    vegaCoCoaReceivingData.status = Status.RECEVING_COMPLETED
                    vm.saveMtnrReceivingLots(vegaCoCoaReceivingData, VegaCoCoaReceiveLots())
                    response.data?.data?.let { moveToSuccessPage(it) }
                } else {
                    showErrorDialogWithFAQLink(requireContext(), response.data?.message ?: "")
                    weighBridgeId = response.data?.data?.lotDetails?.get(0)?.weighBridgeId.toString()
                    grnFlag = response.data?.data?.grnFlag!!

                    val msg = response.data?.message ?: response.data?.errors
                    vegaCoCoaReceivingData.isSynced = false
                    vegaCoCoaReceivingData.isProgress = false
                    vegaCoCoaReceivingData.syncStatusMsg = msg
                    vegaCoCoaReceivingData.status = Status.SYNC_ERROR
                    vm.saveMtnrReceivingLots(vegaCoCoaReceivingData, VegaCoCoaReceiveLots())
                }

                hideLoading()

            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {

                vegaCoCoaReceivingData.isSynced = false
                vegaCoCoaReceivingData.isProgress = false
                vegaCoCoaReceivingData.syncStatusMsg = response.error.toString()
                vegaCoCoaReceivingData.status = Status.SYNC_ERROR
                vm.saveMtnrReceivingLots(vegaCoCoaReceivingData, VegaCoCoaReceiveLots())

                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun initExtra() {
        vegaCoCoaReceivingData = arguments?.getParcelable("summaryData") ?: VegaCoCoaReceiving()
        binding.tvDestValue.text =
            vegaCoCoaReceivingData.supplierCode.plus(" - ").plus(vegaCoCoaReceivingData.supplierName)
        binding.tvReceivingWHValue.text =
            vegaCoCoaReceivingData.storageLocationCode.plus(" - ").plus(vegaCoCoaReceivingData.storageLocationName)
        binding.tvStoNumberValue.text = vegaCoCoaReceivingData.delivery
        binding.tvTruckNoValue.text = vegaCoCoaReceivingData.vehicleNumber
        binding.tvRemarkValue.text = vegaCoCoaReceivingData.remarks
        binding.tvDriverNameValue.text = vegaCoCoaReceivingData.truckDriverName
        binding.tvDateValue.text = DateUtils.getCurrentDate()

        if (vegaCoCoaReceivingData.imagePath?.isNotEmpty()!!) {
            binding.photoImageView.visible()
            binding.photoImageView.setImageBitmap(setScaledBitmap(vegaCoCoaReceivingData.imagePath!!))
        }

        vm.offloadingMtnr.observe(viewLifecycleOwner, Observer { updateOBDDetails(it) })
        vm.getOBDDetails(vegaCoCoaReceivingData.delivery)
        if(vegaCoCoaReceivingData.weighBridgeType == PROCURE){
            binding.tvDispatch.gone()
            binding.tvSnapShot.gone()
            binding.tvOperatorValue.visible()
            binding.tvOperator.visible()
            binding.tvDest.text = getString(R.string.supplier)
            binding.tvTitle.text = getString(R.string.summary_title)
            binding.tvStoNumber.text = getString(R.string.material)
            binding.tvStoNumberValue.text = vegaCoCoaReceivingData.materialName
            binding.tvOperatorValue.text = vegaCoCoaReceivingData.operatorName
            setUpBagAdapter()
        }
    }

    fun setScaledBitmap(imagePath: String): Bitmap? {
        try {
            val imageViewWidth = 347
            val imageViewHeight = 413

            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, bmOptions)
            val bitmapWidth = bmOptions.outWidth
            val bitmapHeight = bmOptions.outHeight

            val scaleFactor = Math.min(bitmapWidth / imageViewWidth, bitmapHeight / imageViewHeight)

            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor

            return BitmapFactory.decodeFile(imagePath, bmOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null

    }

    private fun updateOBDDetails(data: VegaCoCoaReceivingMtnrWithLots?) {
        batchList.clear()
        if (data != null) {
            if (data.lineItems.size > 0) {
                data.lineItems.forEach {
                    /*var edWeight = 0.0
                    val bags = data.lineItems.filter { it1 -> it1.lots.batch.equals(it.batch) }
                    if (bags.size == 1) {
                        if (vm.vegaCoCoaReceivingData.startTime?.isEmpty() == true) vm.vegaCoCoaReceivingData.startTime =
                            DateUtils.getCurrentTimeInMills().toString()
                    }
                    if (bags.size > 0) edWeight = bags[0].bagItem.sumByDouble { it2 -> it2.netWeight.toDouble() }
                    it.editedWeight = edWeight.toString()*/
                    batchList.add(it.lots)
                    bagList.addAll(
                        it.bagItem.filter { it2 -> it2.mtnNumber.equals(it.lots.mtnNumber) }.filter { it1 ->
                            it1.batchNumber.equals(
                                it.lots.batch
                            )
                        }
                    )
                }
                setUpAdapter(batchList as ArrayList<VegaCoCoaReceiveLots>)
            } else {
                setUpAdapter(batchList as ArrayList<VegaCoCoaReceiveLots>)
            }
        } else {
            setUpAdapter(batchList as ArrayList<VegaCoCoaReceiveLots>)
        }
    }

    private fun showRemarkDialog() {
        MaterialDialog(requireContext()).show {
            title(R.string.edit_remark)
            message(R.string.remark)
            cancelOnTouchOutside(false)
            var remark = ""
            val bindingDia = OffloadingCocoaCustomEditTextBinding.inflate(layoutInflater)
            customView(R.layout.offloading_cocoa_custom_edit_text)
            val editValue = this.getCustomView().findViewById<EditText>(R.id.etValue)
            if (!vegaCoCoaReceivingData.remarks.isNullOrEmpty()) editValue.setText(
                vegaCoCoaReceivingData.remarks
            )
            editValue.hint = getString(com.olam.warehouse.presentation.R.string.enter_remark)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(R.string.cancel),
                {
                    if (!editValue.text.isNullOrEmpty()) {
                        vegaCoCoaReceivingData.remarks = editValue.text.toString()
                        binding.tvRemarkValue.text = editValue.text.toString()
                        vm.saveMtnrReceivingLots(vegaCoCoaReceivingData, VegaCoCoaReceiveLots())
                    } else {
                        editValue.error =
                            getString(com.olam.warehouse.presentation.R.string.enter_remark)
                    }
                },
                { dismiss() })
        }
    }

    private fun setUpAdapter(list: List<VegaCoCoaReceiveLots>) {
        val lots = list as MutableList
        if(lots.isNotEmpty()) {
            binding.rvLots.setUpAdapter(
                lots,
                R.layout.item_mtnr_summary_lot_card_layout,
                ItemMtnrSummaryLotCardLayoutBinding::inflate,
                { item, pos, bindItem ->
                    bindItem.tvScaleLotValue.text = item.batch
                    bindItem.tvScaleWeightValue.text =
                        item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(item.uom)
                    bindItem.tvScaleGradeValue.text = item.materialName
                    val editedWeight =
                        if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString()
                            .toDouble()
                            .formatThreeDigits()
                    bindItem.tvScaleDispatchValue.text = editedWeight
                    if (!vegaCoCoaReceivingData.thirdPartyVendorName.isNullOrEmpty()) {
                        bindItem.tvVendorName.visibility = View.VISIBLE
                        bindItem.tvVendorNameValue.visibility = View.VISIBLE
                        bindItem.tvVendorNameValue.text = vegaCoCoaReceivingData.thirdPartyVendorName
                    }

                    /*if (item.editedWeight.toString().toDouble() < 0) {
                tvStTransitValue.text = getString(R.string.transit_loss)
                transit = editedWeight.toDouble().plus(item.weight!!.toDouble())
            } else {
                tvStTransitValue.text = getString(R.string.transit_gain)
                transit = item.weight!!.toDouble().minus(editedWeight.toDouble())
            }*/

                    if (item.weight!!.toDouble() > editedWeight.toDouble()) {
                        bindItem.tvStTransit.text = getString(R.string.transit_loss)
                        transit = item.weight!!.toDouble().minus(editedWeight.toDouble())
                        vegaCoCoaReceivingData.endLot = true
                    } else {
                        bindItem.tvStTransit.text = getString(R.string.transit_gain)
                        transit = editedWeight.toDouble().minus(item.weight!!.toDouble())
                    }

                    bindItem.tvStTransitValue.text =
                        transit.absoluteValue.formatThreeDigits().plus(" ").plus(item.uom).toString()
                    bindItem.tvDispatchUOMValue.text = item.uom
                    bindItem.ivEdit.visible()
                    bindItem.tvAddWeight.visibility = View.GONE
                    bindItem.ivEdit.setOnClickListener { showLotEditDialog(item, it) }
                })
        }
    }

    private fun showLotEditDialog(lot: VegaCoCoaReceiveLots, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.edit_lot))
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
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
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    if (AppUtils.isOnline())
                        postDelivery()
                    else
                        moveToOfflineSuccessPage()
                },
                { dismiss() })
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun postDelivery() {

        val charset = Charsets.UTF_8
        if (vegaCoCoaReceivingData.weighBridgeType== PROCURE) {
            vm.postOffloadingSupplierDetail(prepareSupplierPostData())
        } else {
            val byteArray = resizeBase64Image(getBase64FromFile(vegaCoCoaReceivingData.imagePath.toString())!!).toByteArray(charset)
            vm.postOffloadingDetail(
                VegaCoCoaOffloadingPostRequest(
                    contactNumber = vegaCoCoaReceivingData.contactNumber,
                    driverName = vegaCoCoaReceivingData.truckDriverName,
                    grnFlag = grnFlag,
                    grnNumber = "",
                    imageString = byteArray.let { it1 -> String(it1) },
                    imageUploadMsg = "",
                    key = getCurrentKey(),
                    lotDetails = prepareDeliveryList(),
                    message = "",
                    plant = getPlantDetails(),
                    success = false,
                    transportVendorCode = vegaCoCoaReceivingData.transportVendorCode,
                    vehicleNumber = vegaCoCoaReceivingData.vehicleNumber,
                    vehicleType = vegaCoCoaReceivingData.vehicleType,
                    wayBillNo = vegaCoCoaReceivingData.wayBillNo,

                )
            )
        }

    }

    private fun prepareDeliveryList(): List<VegaCoCoaOffloadingDeliveryDetail> {
        val list = ArrayList<VegaCoCoaOffloadingDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in batchList) {
            val deliveryDetail = VegaCoCoaOffloadingDeliveryDetail()
            var bagItems = bagList.toMutableList()/*.filter { it.batchNumber == item.batch }*/
            deliveryDetail.batchNumber = item.batch
            deliveryDetail.materialCode = item.materialNumber
            deliveryDetail.netWeight = item.editedWeight?.trim() ?: "0.0"
            deliveryDetail.recStorageLocation = vegaCoCoaReceivingData.storageLocationCode
            deliveryDetail.storageLocationCode = vegaCoCoaReceivingData.supplierCode
            deliveryDetail.storageLocation = vegaCoCoaReceivingData.supplierCode
            deliveryDetail.purchaseDocNum = vegaCoCoaReceivingData.purchaseDocNum
            deliveryDetail.purchaseDocDesc = vegaCoCoaReceivingData.purchaseDocDesc
            deliveryDetail.endLotFlag = vegaCoCoaReceivingData.endLot
            deliveryDetail.delivery = item.delivery
            deliveryDetail.deliveryItem = item.posnr
            deliveryDetail.unitsOfMeasure = item.uom
            deliveryDetail.plant = getPlantDetails().plantId
            deliveryDetail.weighBridgeId = weighBridgeId
            deliveryDetail.weighBridgeType = vegaCoCoaReceivingData.weighBridgeType
            var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
            bagItems.forEach { item1 ->
                grossWeight = item1.grossWeight.toDouble()/*grossWeight.plus(item1.grossWeight.toDouble())*/
                tareWeight = tareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
            }
            deliveryDetail.grossWeight = if (deliveryDetail.unitsOfMeasure.equals("MT"))
                convertKgToMT(grossWeight.toString().trim()) else
                grossWeight.toString().trim()//
            deliveryDetail.tareWeight =
                if (deliveryDetail.unitsOfMeasure.equals("MT"))
                    convertKgToMT(tareWeight.toString().trim()) else
                    tareWeight.toString().trim()//
            if (bagItems.isNotEmpty()) {
                val bagSort = mutableListOf<VegaCoCoaOffloadingBagMaterial>()
                val dat = bagItems.sortedByDescending { it.createdPosition }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
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

    private fun moveToSuccessPage(data: VegaCoCoaOffloadingPostRequest) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.success_offloading))
        var message = getString(R.string.grn_number_is, data.grnNumber)
        if (!data.lotDetails[0].documentNum.isNullOrEmpty())
            message = message.plus(("\n").plus(getString(R.string.transit_loss_val, data.lotDetails[0].documentNum)))
        intent.putExtra(AppUtils.SUB_TITLE, message)
        startActivity(intent)
        requireActivity().finish()
    }
    private fun moveToSupplierSuccessPage(data: VegaMtntResponse) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.success_offloading))
        var message = getString(R.string.wb_number_is, data.wbId)
        intent.putExtra(AppUtils.SUB_TITLE, message)
        intent.putExtra(AppUtils.IVC_OFFLOAD_RECEIPT, vegaCoCoaReceivingData)
        if(PreferenceHelper.get(Constants.DIRECT,"").isNotEmpty() || PreferenceHelper.get(Constants.IN_DIRECT,"").isNotEmpty())
            intent.putExtra(AppUtils.EUDR_STATUS, if(vegaCoCoaReceivingData.eudrStatus) "1" else "0" )

        startActivity(intent)
        requireActivity().finish()
    }

    private fun moveToOfflineSuccessPage() {
        vegaCoCoaReceivingData.isOnlineData = false
        vegaCoCoaReceivingData.syncStatusMsg = ""
        vegaCoCoaReceivingData.isSynced = false
        vm.saveMtnrReceivingLots(vegaCoCoaReceivingData, VegaCoCoaReceiveLots())

        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.success_offloading))
        intent.putExtra(
            AppUtils.SUB_TITLE,
            getString(R.string.grn_number_is, abs(vegaCoCoaReceivingData.tempGrnNumber!!.toLong()).toString())
        )
        if(PreferenceHelper.get(Constants.DIRECT,"").isNotEmpty() || PreferenceHelper.get(Constants.IN_DIRECT,"").isNotEmpty())
            intent.putExtra(AppUtils.EUDR_STATUS, if(vegaCoCoaReceivingData.eudrStatus) "1" else "0" )
        startActivity(intent)
        requireActivity().finish()
    }

    fun resizeBase64Image(base64image: String): String {
        val encodeByte = decode(base64image.toByteArray(), DEFAULT)
        val options = BitmapFactory.Options()
        options.inPurgeable = true
        var image = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size, options)
        if (image.height <= 400 && image.width <= 400) {
            return base64image
        }
        image = Bitmap.createScaledBitmap(image, 200, 200, false)
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        System.gc()
        return encodeToString(b, NO_WRAP)
    }


    private fun setUpBagAdapter() {
//        val lots = list as MutableList<VegaOffloadingBagMaterial>
        val bagItem = arrayListOf<VegaCoCoaOffloadingBagMaterial>()
        val groupItem = vegaCoCoaReceivingData.bagList.groupBy { it.bagType }
        groupItem.forEach {
            val bag = VegaCoCoaOffloadingBagMaterial()
            bag.bagType = it.value.get(0).bagType
            bag.bagCount = it.value.sumOf { (it.bagCount.toInt())}.toString()
            bagItem.add(bag)
        }

        vegaCoCoaReceivingData.bagList.forEach {
            val bagType = BagTypeList()
            bagType.bagType = it.bagType
            bagType.bagCount = it.bagCount
            bagType.tareWeight = it.tareWeight.toString().trim()
            bagTypeList.add(bagType)

            /*if (it.bagType1?.isNotEmpty() == true) {
                val bagType1 = BagTypeList()
                bagType1.bagType = it.bagType1.toString()
                bagType1.bagCount = it.bagCount1.toString()
                bagType1.tareWeight = it.tareWeight1.toString().trim()
                bagTypeList.add(bagType1)
            }*/
        }

        val bagGroupItem = bagTypeList.groupBy { it.bagType }

        /*Getting single bag type and tare weight */
        bagTypeList.clear()
        bagGroupItem.forEach {
            val bagType = BagTypeList()
            bagType.bagType = it.value.get(0).bagType
            bagType.bagCount = it.value.sumOf { it.bagCount.toInt() }.toString()
            bagType.tareWeight = it.value.get(0).tareWeight
            bagTypeList.add(bagType)
        }

        binding.rvLots.setUpAdapter(
            bagItem,
            R.layout.item_mtnr_summary_lot_card_layout,
            ItemMtnrSummaryLotCardLayoutBinding::inflate,
            { item, pos, bindingItem ->

                bindingItem.tvScaleLotValue.text = item.bagType
                bindingItem.tvStTransitValue.text =
                    (item.bagCount.toInt()).toString()
                bindingItem.tvLotLbl.text = getString(R.string.bag_details)
                bindingItem.tvStTransit.text = getString(R.string.count)
                bindingItem.tvScaleWeightValue.visibility = View.GONE
                bindingItem.tvScaleGradeValue.visibility = View.GONE
                bindingItem.tvAddWeight.visibility = View.GONE
                bindingItem.tvWeight.visibility = View.GONE
                bindingItem.tvWeightDispatch.visibility = View.GONE
                bindingItem.tvScaleDispatchValue.visibility = View.GONE
                bindingItem.tvGrade.visibility = View.GONE
                bindingItem.tvScaleGradeValue.visibility = View.GONE
            })
    }

    private fun prepareSupplierPostData(): VegaCocoOffloadingSupplierPostRequest {
        val charset = Charsets.UTF_8
        val byteArray =
            getBase64FromFile(vegaCoCoaReceivingData.imagePath.toString())?.toByteArray(charset)
        var bagsModel = arrayListOf<BagTypeList>()
        var bagCount1 = ""
        var bagTareWeight = 0.0


        vegaCoCoaReceivingData.bagList.forEachIndexed { index, item ->
            bagTareWeight += item.bagCount.toInt().times(item.tareWeight?.toDouble()?:0.0)
            if (item.bagType.isNotEmpty()) {
                if (bagsModel.any { it.bagType.equals(item.bagType) }) {
                    bagsModel.find { it.bagType.equals(item.bagType) }?.apply {
                        bagCount = (bagCount.toInt().plus(item.bagCount.toInt())).toString()
                        tareWeight = item.tareWeight?.trim()
                    }
                } else {
                    val bModel = BagTypeList()
                    bModel.bagCount = item.bagCount
                    bModel.bagType = item.bagType
                    bModel.tareWeight = item.tareWeight?.trim()
                    bagsModel.add(bModel)
                }
            }
            /*if (item.bagType1?.isNotEmpty() == true) {
                if (bagsModel.any { it.bagType.equals(item.bagType1) }) {
                    bagsModel.find { it.bagType.equals(item.bagType1) }?.apply {
                        bagCount = (bagCount.toInt().plus(item.bagCount1?.toInt() ?: 0)).toString()
                        tareWeight = item.tareWeight1.toString()
                    }
                } else {
                    val bModel = BagModel()
                    bModel.bagCount = item.bagCount1.toString()
                    bModel.bagType = item.bagType1.toString()
                    bModel.tareWeight = item.tareWeight1.toString()
                    bagsModel.add(bModel)
                }
            }
            if (index == bagList.size - 1 && item.palletWeight?.isNotEmpty() == true && !item.palletWeight.equals("0")) {
                val bModel = BagModel()
                bModel.bagCount = item.noOfPallet.toString()
                bModel.bagType = "Pallet"
                bModel.tareWeight = item.palletWeight.toString()
                bagsModel.add(bModel)
            }*/
           // if(currentkey.contains("VEGA_PE")) item.netWeight = item.netWeight.toDouble().roundToInt().toString()
        }

        bagCount1 = if (bagsModel.size > 0) bagsModel.get(0).bagCount else ""

       /* if ((currentkey.contains("VEGA_GH") && currentkey.contains("PADD"))) {
            if (bagList.isNotEmpty()) {
                bagList[0].bagCount = vegaCoCoaReceivingData.bagCount.toString()
            }
            bagCount1 = vegaCoCoaReceivingData.bagCount.toString()
        }*/


//        val pecaValue=vegaCoCoaReceivingData.pecaNumber?.split("–","-")
//        val pecaNo= if (pecaValue?.isNotEmpty() == true) pecaValue[0] else vegaCoCoaReceivingData.pecaNumber
        var qualitylist = ArrayList<VegaQualityParams>()
        if(getCurrentKey().contains("IV") && getCurrentKey().contains("COCO")) {
            val item1 = VegaQualityParams()
            item1.sapQCName = "COMPLIANCE"
            item1.satNam =
                if (vegaCoCoaReceivingData.eudrStatus) Constants.EUDR_QP_VALUE else Constants.ATTR_UNKNOWN_QP_VALUE
            qualitylist.add(item1)
            val item2 = VegaQualityParams()
            item2.sapQCName = "SOURCE_LOT"
            item2.satNam = if (vegaCoCoaReceivingData.sourceLotId?.isNotEmpty() == true) vegaCoCoaReceivingData.sourceLotId else vegaCoCoaReceivingData.batchNumber
            qualitylist.add(item2)
        }

        val postRequest = VegaCocoOffloadingSupplierPostRequest(
            vegaCoCoaReceivingData.bagList,
            getCurrentKey(),
            if (vegaCoCoaReceivingData.imageString == WEIGHBRIDGE_WEIHSCALE) MISC else if (vegaCoCoaReceivingData.weighBridgeType == STO) STO else PROCURE,
            getPlantDetails(),
            if (vegaCoCoaReceivingData.weighBridgeId.contains("TMP")) "" else vegaCoCoaReceivingData.weighBridgeId,
            vegaCoCoaReceivingData.delivery,
            vegaCoCoaReceivingData.grossWeight,
            vegaCoCoaReceivingData.deliveryItem,
            vegaCoCoaReceivingData.purchaseDocNum,
            vegaCoCoaReceivingData.purchaseDocDesc,
            vegaCoCoaReceivingData.purchaseDocQty,
            vegaCoCoaReceivingData.materialCode,
            vegaCoCoaReceivingData.materialName,
            netWeight = getNetWeight(bagTareWeight),
            vegaCoCoaReceivingData.supplierCode,
            vegaCoCoaReceivingData.supplierName,
            unitsOfMeasure = vegaCoCoaReceivingData.unitsOfMeasure,
            //  wsGate = if (vegaCoCoaReceivingData.imageString == WEIGHSCALE) WS01 else WB01,/*vegaCoCoaReceivingData.wsGate*/
            wsGate = if (vegaCoCoaReceivingData.imageString == WEIGHBRIDGE || vegaCoCoaReceivingData.imageString.isNullOrEmpty()) WB01 else WS01,/*vegaCoCoaReceivingData.wsGate*/
            truckDirection = vegaCoCoaReceivingData.truckDirection,
            vehicleNumber = vegaCoCoaReceivingData.vehicleNumber,
            batchNumber = vegaCoCoaReceivingData.batchNumber,
            driverName = vegaCoCoaReceivingData.truckDriverName,
            contactNumber = vegaCoCoaReceivingData.contactNumber,
            erdat = vegaCoCoaReceivingData.erdat,
            ertim = vegaCoCoaReceivingData.ertim,
            direction = vegaCoCoaReceivingData.direction,
            storageLocationCode = vegaCoCoaReceivingData.storageLocationCode,
            storageLocationName = vegaCoCoaReceivingData.storageLocationName,
            dstorageLocationCode = vegaCoCoaReceivingData.dstorageLocationCode,
            dstorageLocationName = vegaCoCoaReceivingData.dstorageLocationName,
            declaredBagCount = vegaCoCoaReceivingData.bagCount,
//            declaredWeight = vegaCoCoaReceivingData.vendorDeclaredWeight,
//            vendorDeclaredWeight = vegaCoCoaReceivingData.vendorDeclaredWeight,
//            origin = vegaCoCoaReceivingData.origin,
//            department = vegaCoCoaReceivingData.department,
            plantName = getPlantDetails().plantName,
            item = "1",
//            challan = vegaCoCoaReceivingData.challan,
//            secretSampleId = vegaCoCoaReceivingData.secretSampleId,
            remarks = vegaCoCoaReceivingData.remarks,
//            batchCharApplicable = vegaCoCoaReceivingData.batchCharApplicable,
//            wbFlag = vegaCoCoaReceivingData.wbFlag,
//            batchCharFlag = vegaCoCoaReceivingData.batchCharFlag,
//            qualityDetails = vegaCoCoaReceivingData.qualityDetails,
            imageString = byteArray?.let { it1 -> String(it1) },
//            drivingLicense = vegaCoCoaReceivingData.drivingLicense,
//            taxNumber = vegaCoCoaReceivingData.taxNumber,
//            pecaNumber = pecaNo,
//            pileId = vegaCoCoaReceivingData.pileId,
//            trailer = vegaCoCoaReceivingData.trailer,
//            businessName = vegaCoCoaReceivingData.businessName,
//            driverId = vegaCoCoaReceivingData.driverId,
//            coffeeType = vegaCoCoaReceivingData.coffeeType,
            bagType = if (bagsModel.size > 0) bagsModel.get(0).bagType else "",
            bagCount = bagCount1,
            tareWeight = if (bagsModel.size > 0) bagsModel.get(0).tareWeight else "",
            bagType1 = if (bagsModel.size > 1) bagsModel.get(1).bagType else "",
            bagCount1 = if (bagsModel.size > 1) bagsModel.get(1).bagCount else "",
            tareWeight1 = if (bagsModel.size > 1) bagsModel.get(1).tareWeight else "",
            bagType2 = if (bagsModel.size > 2) bagsModel.get(2).bagType else "",
            bagCount2 = if (bagsModel.size > 2) bagsModel.get(2).bagCount else "",
            tareWeight2 = if (bagsModel.size > 2) bagsModel.get(2).tareWeight else "",
            operatorName = vegaCoCoaReceivingData.operatorName,
            sourceLotId = vegaCoCoaReceivingData.sourceLotId,
            eudrStatus = vegaCoCoaReceivingData.eudrStatus,
            ttFarmerList = vegaCoCoaReceivingData.ttFarmerList,
            isDelete = vegaCoCoaReceivingData.isDelete,
            qualityDetails = qualitylist,
            farmerLessTransactionId = if(vegaCoCoaReceivingData.farmerLessTransactionId?.isNotEmpty()==true)Constants.FARMERLESS_TRANSACTION_ID_PREFIX.plus(vegaCoCoaReceivingData.farmerLessTransactionId) else "",

//            invoiceNumber = vegaCoCoaReceivingData.invoiceNumber,
//            invoiceQty = vegaCoCoaReceivingData.invoiceQty,
//            invoicePrice = vegaCoCoaReceivingData.invoicePrice,
//            bagOwnership = vegaCoCoaReceivingData.bagOwnership,
//            totalTarWeight = vegaCoCoaReceivingData.totalTareWeight.toString(),
//            icoLotNumber = vegaCoCoaReceivingData.icoLotNumber,
//            moisture = vegaCoCoaReceivingData.moisture,
//            impurity = vegaCoCoaReceivingData.impurity
        )
        return postRequest
    }

    fun getNetWeight(bagTareWeight: Double): String{
        return if(!vegaCoCoaReceivingData.netWeight.isNullOrEmpty() && !vegaCoCoaReceivingData.netWeight.equals("0") && !vegaCoCoaReceivingData.netWeight.equals("0.000"))vegaCoCoaReceivingData.netWeight.toDouble().minus(bagTareWeight).toString() else vegaCoCoaReceivingData.netWeight
    }
}
