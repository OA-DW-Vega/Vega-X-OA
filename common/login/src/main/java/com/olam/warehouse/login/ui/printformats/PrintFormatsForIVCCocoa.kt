package com.olam.warehouse.login.ui.printformats

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.ItemGrnQualityParamsBinding
import com.olam.warehouse.login.databinding.ItemOffBinding
import com.olam.warehouse.login.databinding.ItemPrintLotCardPreviewBinding
import com.olam.warehouse.login.databinding.VegaGrnSheetBinding
import com.olam.warehouse.login.databinding.VegaIvcOffloadingBinding
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmap
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiving
import com.olam.warehouse.master.vegaecuador.model.VegaGrnqualityList
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.DoAsync
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.formatTwoDigitString
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import java.util.Calendar
import kotlin.time.Duration.Companion.minutes

fun generateLotCardPrint(intent: Intent, context: SuccessActivity): ArrayList<String> {
    var vegaCocoReceivingData = VegaCoCoaReceiving()
    vegaCocoReceivingData = intent.getParcelableExtra(AppUtils.IVC_OFFLOAD_RECEIPT) ?: VegaCoCoaReceiving()
    var printKeys = ArrayList<String>()
    //   DoAsync {
    val view =
        LayoutInflater.from(context).inflate(R.layout.item_print_lot_card_preview, null)
    val viewBinder = ItemPrintLotCardPreviewBinding.bind(view)
    if (vegaCocoReceivingData.batchNumber?.isNotEmpty() == true) {
        viewBinder.ivPreview.setImageBitmap(getBitmap(vegaCocoReceivingData.batchNumber ?: ""))
    }
    viewBinder.tvLotValue.text = vegaCocoReceivingData.batchNumber ?: ""
    viewBinder.tvMaterialValue.text = vegaCocoReceivingData.materialName
    viewBinder.tvWeightValue.text =
        vegaCocoReceivingData.grossWeight.toString().plus(" ").plus(vegaCocoReceivingData.unitsOfMeasure)
    if (PreferenceHelper.get(Constants.DIRECT, "").isNotEmpty() || PreferenceHelper.get(Constants.IN_DIRECT, "")
            .isNotEmpty()
    ) {
        viewBinder.tvComplianceValue.visible()
        viewBinder.tvComplianceValue.visible()
        viewBinder.tvComplianceValue.text =
            if (vegaCocoReceivingData.eudrStatus == true) Constants.EUDR_QP_VALUE else Constants.ATTR_UNKNOWN_QP_VALUE
    }
    if (vegaCocoReceivingData.sourceLotId.isNotEmpty()) {
        viewBinder.tvSrcLotLabel.visible()
        viewBinder.tvSrcLotValue.visible()
        viewBinder.tvSrcLotValue.text = vegaCocoReceivingData.sourceLotId
    }


    printKeys.add(
        bitmapToString(
            getBitmapFromView(
                view, Color.WHITE
            )
        )
    )
    // }.execute()
    return printKeys
}

fun generatePeruGrnTallySheet(intent: Intent, context: Context): ArrayList<String> {
    var printKeys = ArrayList<String>()
    var wbDetails = VegaGrnWeighBridgeId()
    wbDetails = intent.getParcelableExtra(AppUtils.IVC_GRN_RECEIPT) ?: VegaGrnWeighBridgeId()
    // DoAsync {
    val view =
        LayoutInflater.from(context).inflate(R.layout.vega_grn_sheet, null)
    val viewBinder = VegaGrnSheetBinding.bind(view)

    val rightNow = Calendar.getInstance()
    var currentmonth = (rightNow.get(Calendar.MONTH) + 1)
    var currentyear = rightNow.get(Calendar.YEAR)
    if (currentmonth < 10) {
        viewBinder.tvLotCampaign.text = "LOT Campaign ".plus(currentyear - 1).plus("-").plus(currentyear)
    } else {
        viewBinder.tvLotCampaign.text = "LOT Campaign ".plus(currentyear).plus("-").plus(currentyear + 1)
    }

    viewBinder.tvGrnNo.text = wbDetails.grnNumber
    viewBinder.tvSupplier.text = wbDetails.supplierName
    viewBinder.tvSupplierCode.text = wbDetails.supplierCode
    viewBinder.tvWbId.text = wbDetails.weighBridgeId
    viewBinder.tvBatch.text =  wbDetails.batchNumber
    val times = wbDetails.erdat?.split('(', ')')
    if (times?.size ?: 0 > 0)
        viewBinder.tvDate.text =
            times?.get(1)?.toLong()?.let { DateUtils.getDate(it, "dd/MM/yyyy") }
    viewBinder.tvInterLocutor.text = PreferenceHelper.get(Constants.USER_NAME, "")
    viewBinder.tvPile.text = getPlantDetails().plantName
    viewBinder.tvPurchese.text =
        if (wbDetails.purchaseDocNum?.isNotEmpty() == true) wbDetails.purchaseDocNum else wbDetails.purchaseOrderNum
    viewBinder.tvNoOfBags.text = wbDetails.bagCount
    viewBinder.tvWeight.text = wbDetails.netWeight.plus(" ").plus(wbDetails.unitsOfMeasure)
    viewBinder.tvPrice.text = wbDetails.unitPrice
    if (wbDetails.purchaseType.equals("Spot", true)) {
        viewBinder.tvTotalPrice.text = wbDetails.totalPrice?.toDouble()?.formatThreeDigits()
    } else {
        viewBinder.tvTotalPrice.gone()
        viewBinder.totalPrice.gone()
    }
    viewBinder.tvDisCountLabel.gone()
    viewBinder.tvDisCountValue.gone()

    viewBinder.tvStorageLocation.text =
        wbDetails.storageLocationCode.toString()//.plus("-").plus(wbDetails.storageLocationName)
    viewBinder.tvbillId.text = wbDetails.billOfLading
    viewBinder.tvHeaderText.text = wbDetails.headerText
    viewBinder.tvReferenceText.text = wbDetails.reference
    viewBinder.tvDeliveryNoteId.text = wbDetails.deliveryNote
    viewBinder.tvshippingId.text = wbDetails.shippingCentre
    viewBinder.tvDeclaredWeightText.text = wbDetails.declaredWeight.toString().plus(" ").plus(wbDetails.unitsOfMeasure)
    viewBinder.tvdeclaredBagId.text = wbDetails.declaredBagcount
    viewBinder.tvAverageBagText.text = wbDetails.averageBag.toString().plus(" ").plus(wbDetails.unitsOfMeasure)
    viewBinder.tvTareWeightText.text = wbDetails.tareWeight.toString().plus(" ").plus(wbDetails.unitsOfMeasure)
    viewBinder.tvGrossWeightId.text = wbDetails.grossWeight.toString().plus(" ").plus(wbDetails.unitsOfMeasure)
    viewBinder.tvsourceLotId.text = if (!wbDetails.sourceLotId.isNullOrEmpty()) wbDetails.sourceLotId else wbDetails.batchNumber
    viewBinder.tvDriverNameText.text = wbDetails.truckDriverName
    viewBinder.tvtrailerId.text = wbDetails.vehicleNumber
    viewBinder.tvtracterNoText.text = wbDetails.vehicleNumber
    viewBinder.tvoriginId.text = wbDetails.shippingCentre
    viewBinder.tvproductText.text = "CACAO"
    viewBinder.tvSousText.text = wbDetails.department
    viewBinder.tvgrossweightAcceptedValue.text = wbDetails.grossweightAccepted
    viewBinder.tvnetWeightReceivedValue.text = wbDetails.netweightReceived




    viewBinder.tvPlantAddress.text = when {
        getPlantDetails().plantId.contains(
            "4161",
            true
        ) -> VegaPlantAddress.from(getPlantDetails().plantId.toInt())?.field

        getPlantDetails().plantId.contains(
            "4162",
            true
        ) -> VegaPlantAddress.from(getPlantDetails().plantId.toInt())?.field

        getPlantDetails().plantId.contains(
            "4176",
            true
        ) -> VegaPlantAddress.from(getPlantDetails().plantId.toInt())?.field

        getPlantDetails().plantId.contains(
            "4216",
            true
        ) -> VegaPlantAddress.from(getPlantDetails().plantId.toInt())?.field

        getPlantDetails().plantId.contains(
            "4217",
            true
        ) -> VegaPlantAddress.from(getPlantDetails().plantId.toInt())?.field

        getPlantDetails().plantId.contains(
            "4231",
            true
        ) -> VegaPlantAddress.from(getPlantDetails().plantId.toInt())?.field

        getPlantDetails().plantId.contains(
            "4232",
            true
        ) -> VegaPlantAddress.from(getPlantDetails().plantId.toInt())?.field

        getPlantDetails().plantId.contains(
            "4282",
            true
        ) -> VegaPlantAddress.from(getPlantDetails().plantId.toInt())?.field

        getPlantDetails().plantId.contains(
            "4284",
            true
        ) -> VegaPlantAddress.from(getPlantDetails().plantId.toInt())?.field

        getPlantDetails().plantId.contains(
            "4177",
            true
        ) -> VegaPlantAddress.from(getPlantDetails().plantId.toInt())?.field

        getPlantDetails().plantId.contains(
            "4281",
            true
        ) -> VegaPlantAddress.from(getPlantDetails().plantId.toInt())?.field

        getPlantDetails().plantId.contains(
            "4289",
            true
        ) -> VegaPlantAddress.from(getPlantDetails().plantId.toInt())?.field

        getPlantDetails().plantId.contains(
            "4215",
            true
        ) -> VegaPlantAddress.from(getPlantDetails().plantId.toInt())?.field

        else -> getPlantDetails().plantName
    }
    val qualityList = arrayListOf<VegaGrnqualityList>()
    qualityList.addAll(wbDetails.qualityDetailsFinal)
    var refractionAdmix = 0.0
    var fragmentRefraction = 0.0
    var foreignMatterRefraction = 0.0
    var refractionHumidity = 0.0
    var sumOfQuality = 0.0
    qualityList.forEach {
        when {
            it.nameChar.equals("B_ADMIX_REF") -> refractionAdmix =
                it.qualityParameterValue?.replace("kg", "")?.replace("%", "")?.toDouble() ?: 0.0

            it.nameChar.equals("B_FRAG_REF") -> fragmentRefraction =
                it.qualityParameterValue?.replace("kg", "")?.replace("%", "")?.toDouble() ?: 0.0

            it.nameChar.equals("B_FM_REF") -> foreignMatterRefraction =
                it.qualityParameterValue?.replace("kg", "")?.replace("%", "")?.toDouble() ?: 0.0

            it.nameChar.equals("B_MOIST_REF") -> refractionHumidity =
                it.qualityParameterValue?.replace("kg", "")?.replace("%", "")?.toDouble() ?: 0.0
        }
    }
    //sumOfQuality = refractionAdmix.plus(fragmentRefraction).plus(foreignMatterRefraction).plus(refractionHumidity)
    //var finalNetweight = wbDetails.netweightReceived?.toDouble()?.minus(sumOfQuality)
    var diffrence = wbDetails.netweightReceived?.toDouble()?.minus(wbDetails.declaredWeight?.toDouble() ?: 0.0)
    viewBinder.tvFinalValue.text = wbDetails.netweightReceived?.toDouble()?.formatThreeDigits()
    viewBinder.tvDifferenceValue.text = diffrence?.formatThreeDigits()
    viewBinder.tvRefractionAdmixValue.text = refractionAdmix.toString()
    viewBinder.tvRefractionFragmentsValue.text = fragmentRefraction.toString()
    viewBinder.tvForeignMatterValue.text = foreignMatterRefraction.toString()
    viewBinder.tvRefractionHumidityValue.text = refractionHumidity.toString()

//    viewBinder.tvRefractionHumidityValue.text = "CACAO"
//    viewBinder.tvForeignMatterValue.text = wbDetails.department
//    viewBinder.tvRefractionFragmentsValue.text = wbDetails.grossweightAccepted
//    viewBinder.tvRefractionAdmixValue.text = wbDetails.netweightReceived
//    viewBinder.tvFinalValue.text = "0"
//    viewBinder.tvDifferenceValue.text = ""
    viewBinder.rvQuality.setUpAdapter(
        qualityList.filter { !it.qualityParameterValue.isNullOrEmpty() }.toMutableList(),
        R.layout.item_grn_quality_params,
        ItemGrnQualityParamsBinding::inflate,
        { it, pos, bindingItem ->
            bindingItem.tvQualityLabel.setText(it.descrChar.toString())
            bindingItem.tvQualityValue.setText(it.qualityParameterValue.toString())
        }, {}, GridLayoutManager(context, 3)
    )

    printKeys.add(
        bitmapToString(
            getBitmapFromView(
                view, Color.WHITE
            )
        )
    )

    // }.execute()
    return printKeys
}

fun offloadingPrint(intent: Intent, context: Context): ArrayList<String> {
    var vegaCocoReceivingData = VegaCoCoaReceiving()
    vegaCocoReceivingData = intent.getParcelableExtra(AppUtils.IVC_OFFLOAD_RECEIPT) ?: VegaCoCoaReceiving()
    val bagList = vegaCocoReceivingData.bagList
    var printKeys = ArrayList<String>()
    //DoAsync {
    val view = LayoutInflater.from(context).inflate(R.layout.vega_ivc_offloading, null)
    val viewBinder = VegaIvcOffloadingBinding.bind(view)
    viewBinder.tvHeaderDate.text = DateUtils.getDate(DateUtils.getCurrentTimeInMills(), "dd/MM/yyyy")
    viewBinder.tvHeaderUnit.text = getPlantDetails().plantId
    viewBinder.tvHeaderSupplier.text =
        vegaCocoReceivingData.supplierCode.toString().plus(" - ").plus(vegaCocoReceivingData.supplierName)
    viewBinder.tvHeaderWbno.text = vegaCocoReceivingData.weighBridgeId
    viewBinder.tvHeaderTruckno.text = vegaCocoReceivingData.vehicleNumber
    viewBinder.tvHeaderDrivername.text = vegaCocoReceivingData.driverName
    viewBinder.tvHeaderRecWh.text =
        vegaCocoReceivingData.storageLocationCode.toString().plus(" - ").plus(vegaCocoReceivingData.storageLocationName)
    viewBinder.tvHeaderMaterial.text =
        vegaCocoReceivingData.materialCode.toString().plus(" - ").plus(vegaCocoReceivingData.materialName)

    viewBinder.tvTotalNoBags.text = bagList.sumOf { it.bagCount.toInt() }.toString()
    viewBinder.tvTotalTareWeight.text = bagList.sumOf { it.tareWeight?.toDouble() ?: 0.0 }.toString()
    viewBinder.tvTotalNetWeight.text = bagList.sumOf { it.netWeight.toDouble() }.toString()
    //val gross = bagList.sumOf { it.grossWeight.toDouble() }.toString()
    viewBinder.tvTotalGrossWeight.text = vegaCocoReceivingData.grossWeight

    viewBinder.tableListOff.setUpAdapter(
        bagList.toMutableList(),
        R.layout.item_off,
        ItemOffBinding::inflate,
        { it, pos, bindingItem ->
            bindingItem.rowOne.text = (pos + 1).toString()
            bindingItem.rowTwo.text = it.bagType
            bindingItem.rowThree.text = it.bagCount
            bindingItem.rowFour.text = it.tareWeight
            bindingItem.rowFive.text = it.netWeight
            bindingItem.rowSix.text = it.grossWeight
        })
    printKeys.add(
        bitmapToString(
            getBitmapFromView(view, Color.WHITE)
        )
    )

    // }.execute()
    return printKeys
}

enum class VegaPlantAddress(val plantId: Int, val field: String) {
    PlantA(plantId = 4161, "Outspan Ivoire S.A.\n 15 BP 300 ABJ 15\n Abidjan\n"),
    PlantB(plantId = 4162, "Outspan Ivoire S.A.\n	01 BP 1663, SAN PEDRO 01\n San Pedro\n"),
    PlantC(plantId = 4176, "Outspan Ivoire S.A.\n 01 BP 1663, SAN PEDRO 01\n	San Pedro\n"),
    PlantD(plantId = 4216, "OCP UNICO trading\nZone Industrielle N° 4 du Doma\n414020\n3214916834\n"),
    PlantE(plantId = 4217, "OCP San Pedro trading \nZone Industrielle N° 4 du Doma\nSan Pedro\n"),
    PlantF(plantId = 4231, "O.C.P. Site d ’Abidjan\nZone Industrielle de Vridi\nAbidjan, Vridi\n"),
    PlantG(plantId = 4232, "OCP Tuci trading\nZone Industrielle de Vridi\nAbidjan, Vridi\n"),

    PlantI(plantId = 4282, "OCP Tuci trading\nZone Industrielle de Vridi\nAbidjan, Vridi\n"),
    PlanJ(plantId = 4284, "OCP Tuci trading\nZone Industrielle de Vridi\nAbidjan, Vridi\n"),
    PlantK(plantId = 4177, "OCP Tuci trading\nZone Industrielle de Vridi\nAbidjan, Vridi\n"),
    PlanL(plantId = 4289, "OCP Tuci trading\nZone Industrielle de Vridi\nAbidjan, Vridi\n"),
    PlanM(plantId = 4215, "OCP Tuci trading\nZone Industrielle de Vridi\nAbidjan, Vridi\n"),
    PlanO(plantId = 4281, "OCP Tuci trading\nZone Industrielle de Vridi\nAbidjan, Vridi\n");

    companion object {
        fun from(plantId: Int): VegaPlantAddress? = VegaPlantAddress.values().find { it.plantId == plantId }
    }

}
