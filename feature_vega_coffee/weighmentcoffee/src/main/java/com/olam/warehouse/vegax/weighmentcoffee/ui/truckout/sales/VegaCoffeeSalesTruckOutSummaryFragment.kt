package com.olam.warehouse.vegax.weighmentcoffee.ui.truckout.sales

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.Data
import com.olam.warehouse.master.common.model.VegaMtntPost
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesBagMaterial
import com.olam.warehouse.master.work.convertKgToMT
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.weighmentcoffee.R
import com.olam.warehouse.vegax.weighmentcoffee.data.domain.usecase.VegaCoffeeSalesDeliveryDetail
import com.olam.warehouse.vegax.weighmentcoffee.data.domain.usecase.VegaCoffeeSalesPostRequest
import com.olam.warehouse.vegax.weighmentcoffee.databinding.FragmentVegaCoffeeTruckoutMtntSummaryBinding
import com.olam.warehouse.vegax.weighmentcoffee.databinding.ItemVegaCoffeeBagSummaryDetailsBinding
import com.olam.warehouse.vegax.weighmentcoffee.ui.VegaCoffeeMtntViewModel
import com.olam.warehouse.vegax.weighmentcoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 9/2/2020.
 */
class VegaCoffeeSalesTruckOutSummaryFragment : BaseFragment() {

    private var mMtntList = mutableListOf<VegaMtnt>()
    private var mtntData = VegaMtnt()
    private var mBagsTarWeight: Double? = 0.0
    private var bagList = ArrayList<VegaCoffeeSalesBagMaterial>()
    private val vm: VegaCoffeeMtntViewModel by viewModel()
    private lateinit var binding: FragmentVegaCoffeeTruckoutMtntSummaryBinding
    private var preQualityList = mutableListOf<VegaQualityParams>()
    override val layoutResourceId = R.layout.fragment_vega_coffee_truckout_mtnt_summary
    private var isThirdParty: Boolean = false
    private var tarweight: Double? = 0.0
    private var endLotFlag: Boolean = false

    companion object {
        fun newInstance(mtntData: VegaMtnt, mMtntList: ArrayList<VegaMtnt>, isThirdParty: Boolean) =
            VegaCoffeeSalesTruckOutSummaryFragment().putArgs {
                putParcelable(MTNTDATA, mtntData)
                putParcelableArrayList(MTNT_POST_DATA, mMtntList)
                putBoolean(THIRD_PARTY_SALES, isThirdParty)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCoffeeTruckoutMtntSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("receiving/ui/truckout/mtnt/VegaTruckOutMtntSummaryFragment").title("IVC/Coffee/Weighment/Sales Truck Out Summary")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        mMtntList = arguments?.getParcelableArrayList<VegaMtnt>(MTNT_POST_DATA)!!
        mtntData = arguments?.getParcelable<VegaMtnt>(MTNTDATA)!!
        isThirdParty = arguments?.getBoolean(THIRD_PARTY_SALES) ?: false
        binding.tvMaterial.text = ": ".plus(mtntData.materialName)
        binding.tvTruckID.text =
            getString(R.string.truck_number).plus(": ").plus(mtntData.vehicleNumber ?: mtntData.weighBridgeId)
        binding.tvSupplierName.text = mtntData.supplierName ?: mtntData.supplierCode
        binding.tvWeight.text = mtntData.tareWeight.plus(" ").plus(mtntData.unitsOfMeasure)
        val times = mtntData.erdat?.split('(', ')')
        binding.tvDate.text = times?.get(1)?.let { it1 ->
            DateUtils.getUTCDateTime(
                it1,
                App.getAppContext()
            )
        }
        if (mtntData.imagePath?.isNotEmpty()!!) {
            binding.photoImageView.visible()
            binding.photoImageView.setImageBitmap(setScaledBitmap())
        }

        vm.deliveryPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
        binding.llBagDetails.visible()
        setBagDetails(mMtntList)
        var count = 0

        mMtntList.forEach {
            count += it.bagCount!!.toInt()
            val weight = it.bagCount!!.toDouble() * it.bagTareWeight!!.toDouble()
            mBagsTarWeight = mBagsTarWeight!! + weight


        }
        binding.tvTotalNoofBags.text = count.toString()
        binding.tvTruckGrossWeight.text = mtntData.grossWeight.plus(" ").plus(mtntData.unitsOfMeasure)
        binding.tvTruckTarWeight.text = mtntData.tareWeight.plus(" ").plus(mtntData.unitsOfMeasure)
        binding.tvNetWeight.text =
            getTotalWeight(mtntData.grossWeight!!, mtntData.tareWeight!!).toString().plus(" ")
                .plus(mtntData.unitsOfMeasure)

        binding.tvGross.text = mtntData.grossWeight.plus(" ").plus(mtntData.unitsOfMeasure)
        binding.tvTarWeight.text = mtntData.tareWeight.plus(" ").plus(mtntData.unitsOfMeasure)
        binding.tvBagTare.text = mBagsTarWeight?.formatThreeDigits().toString().plus(" ").plus(mtntData.unitsOfMeasure)
        tarweight = mtntData.tareWeight?.toDouble()!! + mBagsTarWeight!!.toDouble()
        val netWeight = mtntData.grossWeight?.toDouble()!! - tarweight!!
        binding.tvTotalNetWeight.text =
            netWeight.formatThreeDigits().replace(",", "").plus(" ").plus(mtntData.unitsOfMeasure)
        val netData = binding.tvTotalNetWeight.text.toString().replace(",", "").split(" ")[0]
        binding.btnConfirm.setOnClickListener { if (validate(netData)) showConfirmDialog() else showSnack(getString(R.string.negative_weight_error)) }
        vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })
        if (!mtntData.batchNumber.isNullOrBlank())
            vm.getPreSamplingQualitydata(mtntData.batchNumber ?: "", mtntData.materialCode ?: "")
        vm.data.observe(viewLifecycleOwner, Observer { updateThirdPartyUI(it) })

    }

    private fun updateThirdPartyUI(data: Resource<GenericReqAndResp<Data>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> prepareSuccessData(it.data?.data?.data, true, it.data?.message.toString())
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    prepareErrorData(mtntData.weighBridgeId, false, it.error.toString())
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }


    private fun updateUI(data: Resource<GenericReqAndResp<VegaCoffeeSalesPostRequest>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    if (data.data?.success == true) {
                        prepareSuccessData(
                            it.data?.data?.deliveryDetails?.get(0)?.weighBridgeId,
                            true,
                            it.data?.message.toString()
                        )
                    } else {
                        hideCustomLoading()
                        prepareErrorData(mtntData.weighBridgeId, false, it.data?.message.toString())
                        showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }

                }
                //  Resource.Status.SUCCESS -> prepareSuccessData("it.data?.data?.wbId", true, it.data?.message.toString())
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    prepareErrorData(mtntData.weighBridgeId, false, it.error.toString())
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_truck_out_message)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    postTruckOutDelivery()
                    //postReceiving()
                },
                { dismiss() })
        }
    }

    private fun postTruckOutDelivery() {
        if (isThirdParty) {
            postThirdPartyReceiving()
        } else
            vm.postSalesTruckOutDetail(
                VegaCoffeeSalesPostRequest(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    operatorName = "",
                    batchNumber = mtntData.batchNumber,
                    delFlag = "",
                    deliveryDetails = prepareDeliveryList(),
                    weighmentType = mtntData.weighBridgeType
                ), isThirdParty
            )
    }

    private fun validate(netweight: String): Boolean {
        return !netweight.contains("-")
    }

    private fun postThirdPartyReceiving() {
        mMtntList.forEachIndexed { index, vegaMtnt ->
            vegaMtnt.bagWeight = (vegaMtnt.bagCount?.toDouble()?.let { it1 ->
                vegaMtnt.bagTareWeight?.toDouble()?.times(it1)
            }).toString()

            if (index == 0) {
                val charset = Charsets.UTF_8
                val byteArray = getBase64FromFile(mtntData.imagePath.toString())?.toByteArray(charset)
                vegaMtnt.imageString = byteArray?.let { it1 -> String(it1) }
            }
            vegaMtnt.grossWeight = mtntData.grossWeight
            vegaMtnt.tareWeight = mtntData.tareWeight
            vegaMtnt.item = mtntData.item
            vegaMtnt.netWeight = binding.tvTotalNetWeight.text.toString().replace(",", "").split(" ")[0]
            vegaMtnt.truckDirection = "OUT"
            vegaMtnt.materialCode = "000000".plus(vegaMtnt.materialCode)
        }
        vm.postThirdPartyData(VegaMtntPost(getCurrentKey(), getPlantDetails(), mMtntList))
    }

    //    private fun preparePostRequest() : VegaCoffeeSalesPostRequest{
//        val request = VegaCoffeeSalesPostRequest()
//    }
    private fun preparebagList(): ArrayList<VegaCoffeeSalesBagMaterial> {
        val list = ArrayList<VegaCoffeeSalesBagMaterial>()
        mMtntList.forEach {
            val bagDetail = VegaCoffeeSalesBagMaterial()
            bagDetail.batchNumber = it.batchNumber.toString()
            bagDetail.bagCount = it.bagCount.toString()
            bagDetail.bagMaterialCode = " "
            bagDetail.materialName = it.materialName.toString()
            bagDetail.materialCode = it.materialCode.toString()
            bagDetail.bagType = it.bagType.toString()
            if (it.palletCount.equals("")) {
                bagDetail.noOfPallet = "0"
            } else {
                bagDetail.noOfPallet = it.palletCount
            }
            bagDetail.palletWeight = it.palletWeight
            list.add(bagDetail)
        }
        return list
    }

    private fun prepareDeliveryList(): List<VegaCoffeeSalesDeliveryDetail> {
        val list = ArrayList<VegaCoffeeSalesDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        bagList.clear()
        bagList = preparebagList()

        for (item in mMtntList) {
            val deliveryDetail = VegaCoffeeSalesDeliveryDetail()
            var bagItems = bagList.filter { it.batchNumber == item.batchNumber }
            deliveryDetail.batchNumber = item.batchNumber
            deliveryDetail.bltxt = ""
            deliveryDetail.materialCode = "000000".plus(item.materialCode)
            deliveryDetail.plantId = item.plantId
            deliveryDetail.weighBridgeId = mtntData.weighBridgeId
            deliveryDetail.supplierCode = mtntData.supplier
            deliveryDetail.supplierName = mtntData.supplierName
            deliveryDetail.tareWeight = tarweight.toString()
            deliveryDetail.grossWeight = mtntData.grossWeight
            deliveryDetail.netWeight = binding.tvTotalNetWeight.text.toString().replace(",", "").split(" ")[0]
            deliveryDetail.createdDate = ""
            deliveryDetail.salesOrderNum = mtntData.purchaseDocNum
            deliveryDetail.recStorageLocationCode = item.storageLocationCode
            deliveryDetail.storageLocationCode = item.storageLocationCode
            deliveryDetail.qualityDetails = preQualityList
            deliveryDetail.salesItem = ""
            deliveryDetail.weighBridgeId = item.weighBridgeId
            deliveryDetail.delivery = mtntData.delivery
            deliveryDetail.deliveryItem = mtntData.deliveryItem
            deliveryDetail.deliveryStatus = true
            deliveryDetail.deliveryFlag = true
            deliveryDetail.pickingFlag = true
            deliveryDetail.pgiFlag = true
            deliveryDetail.frbnr1 = ""
            deliveryDetail.storageLossFlag = endLotFlag
            deliveryDetail.endLotFlag = endLotFlag
            deliveryDetail.unitsOfMeasure = mtntData.unitsOfMeasure
            deliveryDetail.year = year.toString()
            if (isThirdParty) {
                deliveryDetail.fromVendorCode = mtntData.supplier ?: ""
                deliveryDetail.toVendorCode = mtntData.supplier ?: ""
            }
            deliveryDetail.imagePath = mtntData.imagePath.toString()
            val charset = Charsets.UTF_8
            val byteArray = getBase64FromFile(mtntData.imagePath.toString())?.toByteArray(charset)
            deliveryDetail.imageString = byteArray?.let { it1 -> String(it1) }
            //Bag and pallet details
            var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
            var startTime = ""
            var endTime = ""
            bagItems.forEach { item1 ->
                //val palletAvg = 0.0
                val palletAvg =
                    if (item1.noOfPallet?.toInt() != 0) item1.palletWeight?.toDouble()
                        ?.div(item1.noOfPallet?.toInt()!!) else 0.0
                grossWeight = grossWeight.plus(item1.grossWeight.toDouble())
                tareWeight = tareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                    .plus(palletAvg!!)
                bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                if (item1.noOfPallet != "")
                    item1.noOfPallet = if (item1.noOfPallet?.toInt() ?: 0 <= 0) "0" else item1.noOfPallet
            }
            deliveryDetail.startTime = ""
            deliveryDetail.endTime = ""
            deliveryDetail.turnAroundTime = " "
            if (bagItems.isNotEmpty()) {
                val bagSort = mutableListOf<VegaCoffeeSalesBagMaterial>()
                val dat = bagItems.sortedByDescending { it.createdPosition }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
                deliveryDetail.huno = bagItems[0].unitsOfMeasure
                deliveryDetail.huwt = bagTareWeight.formatThreeDigits().toString().trim()
                deliveryDetail.huno2 = "KG"
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.bagList = bagItems
            } /*else {
                deliveryDetail.huno2 = summaryObj?.unitsOfMeasure
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.grossWeight = grossWeight.toString().trim()
            }*/
            deliveryDetail.bagList = bagItems
            list.add(deliveryDetail)
        }
        return list
    }

    private fun prepareSuccessData(wbId: String?, syncStatus: Boolean, msg: String) {
        hideLoading()
        if (isThirdParty)
            moveToThirdPartySuccessPage(msg)
        else
            moveToSuccessPage(msg)
    }

    private fun prepareErrorData(wbId: String?, syncStatus: Boolean, msg: String) {
        hideLoading()
//        mtntData.tmpWbId = wbId ?: ""
        /*mtntData.truckDirection = DIRECTIONOUT
        mtntData.status = Status.SYNC_ERROR
        mtntData.isSynced = syncStatus
        mtntData.syncStatusMsg = msg
        vm.saveMtnt(mtntData)
        if (!syncStatus) {
            mMtntList.forEach { it.tmpWbId = wbId ?: "" }
            vm.saveMtntLineItems(mMtntList)
        }*/
    }


    private fun setBagDetails(data: MutableList<VegaMtnt>?) {
        binding.rvBagDetailSummary.setUpAdapter(
            data!!,
            R.layout.item_vega_coffee_bag_summary_details,
            ItemVegaCoffeeBagSummaryDetailsBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvBagType.text = it.bagType
                bindItem.tvNoofBags.text = it.bagCount


            })
    }

    private fun getTotalWeight(grossWeight: String, tarWeight: String): Double {
        var totalWeight = 0.0
        try {
            totalWeight = grossWeight.toDouble() - tarWeight.toDouble()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return totalWeight
    }

    fun setScaledBitmap(): Bitmap? {
        try {
            val imageViewWidth = 347
            val imageViewHeight = 413

            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(mtntData.imagePath, bmOptions)
            val bitmapWidth = bmOptions.outWidth
            val bitmapHeight = bmOptions.outHeight

            val scaleFactor = Math.min(bitmapWidth / imageViewWidth, bitmapHeight / imageViewHeight)

            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor

            return BitmapFactory.decodeFile(mtntData.imagePath, bmOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null

    }

    fun getBase64FromFile(path: String?): String? {
        var bmp: Bitmap? = null
        var baos: ByteArrayOutputStream? = null
        var baat: ByteArray? = null
        var encodeString: String? = null
        try {
            bmp = BitmapFactory.decodeFile(path)
            baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            baat = baos.toByteArray()
            encodeString = Base64.encodeToString(baat, Base64.DEFAULT)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return encodeString
    }

    private fun moveToThirdPartySuccessPage(wbId: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_truck_out)
        ) else intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_truck_out_offline)
        )
        intent.putExtra(AppUtils.SUB_TITLE, wbId)
        startActivity(intent)
        requireActivity().finish()
    }


    private fun moveToSuccessPage(wbId: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_truck_out)
        ) else intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_truck_out_offline)
        )
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id_is).plus(wbId))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun updatePreQuality(response: Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            preQualityList = it.data?.data?.get(0)?.qualityParameters as MutableList<VegaQualityParams>
                            val data = preQualityList.filter { it.sapQCName == "CI_END_OF_LOT" }
                            if (data.isNotEmpty()) {
                                endLotFlag = (data[0].satNam?.isNotBlank() == true)
                            }
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
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

}
