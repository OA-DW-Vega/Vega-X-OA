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
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getBase64FromFile
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaOffloadingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiveLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiving
import com.olam.warehouse.master.vegacocoa.model.VegaCoCoaReceivingMtnrWithLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcocoa.R
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.model.VegaCoCoaOffloadingDeliveryDetail
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.model.VegaCoCoaOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingcocoa.databinding.FragmentCocoaMtnrSummaryBinding
import com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingcocoa.utils.*
import kotlinx.android.synthetic.main.item_mtnr_summary_lot_card_layout.view.*
import kotlinx.android.synthetic.main.offloading_cocoa_custom_edit_text.view.*
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
                    UIUtils.showErrorDialog(requireContext(), response.data?.message ?: "")
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
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
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
                        if (vm.vegaCoffeeReceivingData.startTime?.isEmpty() == true) vm.vegaCoffeeReceivingData.startTime =
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
            customView(R.layout.offloading_cocoa_custom_edit_text)
            val editValue = this.getCustomView().etValue
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
        binding.rvLots.setUp(lots, R.layout.item_mtnr_summary_lot_card_layout, { item, pos ->
            tvScaleLotValue.text = item.batch
            tvScaleWeightValue.text =
                item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(item.uom)
            tvScaleGradeValue.text = item.materialName
            val editedWeight = if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString().toDouble()
                .formatThreeDigits()
            tvScaleDispatchValue.text = editedWeight
            if (!vegaCoCoaReceivingData.thirdPartyVendorName.isNullOrEmpty()) {
                tvVendorName.visibility = View.VISIBLE
                tvVendorNameValue.visibility = View.VISIBLE
                tvVendorNameValue.text = vegaCoCoaReceivingData.thirdPartyVendorName
            }

            /*if (item.editedWeight.toString().toDouble() < 0) {
                tvStTransitValue.text = getString(R.string.transit_loss)
                transit = editedWeight.toDouble().plus(item.weight!!.toDouble())
            } else {
                tvStTransitValue.text = getString(R.string.transit_gain)
                transit = item.weight!!.toDouble().minus(editedWeight.toDouble())
            }*/

            if (item.weight!!.toDouble() > editedWeight.toDouble()) {
                tvStTransit.text = getString(R.string.transit_loss)
                transit = item.weight!!.toDouble().minus(editedWeight.toDouble())
                vegaCoCoaReceivingData.endLot = true
            } else {
                tvStTransit.text = getString(R.string.transit_gain)
                transit = editedWeight.toDouble().minus(item.weight!!.toDouble())
            }

            tvStTransitValue.text = transit.absoluteValue.formatThreeDigits().plus(" ").plus(item.uom).toString()
            tvDispatchUOMValue.text = item.uom
            ivEdit.visible()
            tv_add_weight.visibility = View.GONE
            ivEdit.setOnClickListener { showLotEditDialog(item, it) }
        })
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
        val byteArray =
            resizeBase64Image(getBase64FromFile(vegaCoCoaReceivingData.imagePath.toString())!!).toByteArray(charset)

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
                wayBillNo = vegaCoCoaReceivingData.wayBillNo
            )
        )

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

}
