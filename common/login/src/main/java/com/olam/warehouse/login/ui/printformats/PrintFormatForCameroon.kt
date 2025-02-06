package com.olam.warehouse.login.ui.printformats

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.ItemPrintLotCardPreviewBinding
import com.olam.warehouse.login.databinding.VegaCameroonCocoQaEntrySheetBinding
import com.olam.warehouse.login.databinding.VegaCameroonCocoaGrnReceiptPrintingBinding
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmap
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.visible


const val OFFLOADING_RECEIPT_PRINT_KEY = "offloading_receipt_print_key"

fun qualityApprovalDDNCMPrintFormat(intent: Intent, context: Context): ArrayList<String> {

    var mould = 0.000
    var slaty = 0.000

    val printKeys = ArrayList<String>()

    val receivingData: VegaQualityApproveCameroonWeighBridge = intent.getParcelableExtra(UIUtils.RECEIVING_DATA)
        ?: VegaQualityApproveCameroonWeighBridge()

    val lotList: ArrayList<VegaCoffeeSalesLots> = intent.getParcelableArrayListExtra(AppUtils.LOT_CARD) ?: ArrayList()

    val vegaBcApprovePostData: VegaQualityApproveCameroonPostData = intent.getParcelableExtra(UIUtils.QUALITY_DATA)
        ?: VegaQualityApproveCameroonPostData()


    try {

            val view =
                LayoutInflater.from(context).inflate(R.layout.vega_cameroon_cocoa_grn_receipt_printing, null)
            val viewBinder = VegaCameroonCocoaGrnReceiptPrintingBinding.bind(view)

            viewBinder.tvDdNo.text = receivingData.grnNumber
            viewBinder.tvLotNo.text = receivingData.batchNumber
            viewBinder.tvProduct.text = receivingData.materialName
            viewBinder.tvSupplier.text = receivingData.supplierName
            viewBinder.tvDate.text = receivingData.year
            viewBinder.tvUnit.text = receivingData.plantId
            viewBinder.tvBeNo.text = receivingData.wbid
            viewBinder.tvConsentEnglish.text = context.getString(R.string.consentEnglish)
            viewBinder.tvConsentForeign.text = context.getString(R.string.consentForeign)

            lotList.forEachIndexed { index, item ->
                viewBinder.tvCleanCocoa.text = item.rsNum
                viewBinder.tvRcvdAt.text = item.plantId
                viewBinder.tvTotalRefraction.text = item.meins
                viewBinder.tvNetWt.text = item.weight
                viewBinder.tvMtntNo.text = item.deliveryItem
                viewBinder.tvInspectionLot.text = item.bwart
                viewBinder.tvTruck.text = item.grade
                viewBinder.tvmtntWeight.text = item.phase
                viewBinder.tvGrossWeight.text = item.xchpf
                viewBinder.tvBagCount.text = item.noOfBags
                viewBinder.tvBagWeight.text = item.salesType
                viewBinder.tvPalletWeight.text = item.saleOrderId
                viewBinder.tvJuteBagCount.text = item.processOrderNo
                viewBinder.tvNylonBagCount.text = item.rsPos
                viewBinder.tvPalletCount.text = item.certificate
            }

            viewBinder.tvBeanCountRefraction.text = "0"

            viewBinder.tvOtherDefects.text = "0"
            vegaBcApprovePostData.qualityDetails.forEach {
                when (it.nameChar) {
                    "B_MOIST" -> viewBinder.tvHumidity.text = it.qualityParameterValue
                    "B_MOULD4" -> {
                        viewBinder.tvMould.text = it.qualityParameterValue
                        mould = it.qualityParameterValue.toString().toDouble()
                    }
                    "B_SL" -> {
                        viewBinder.tvSlaty.text = it.qualityParameterValue
                        slaty = it.qualityParameterValue.toString().toDouble()
                    }
                    "WASTE" -> viewBinder.tvWaste.text = it.qualityParameterValue
                    "B_SMOKY" -> {
                        viewBinder.tvSmoky.text = it.qualityParameterValue
                        when (it.qualityParameterValue) {
                            "IR-AB/PR 003" ->
                                viewBinder.tvSmoky.text = "Absence"
                            "IR-AB/PR 004" ->
                                viewBinder.tvSmoky.text = "Presence"
                        }
                    }
                    "B_CLUSTER1" -> viewBinder.tvCluster.text = it.qualityParameterValue
                    "FLAT" -> viewBinder.tvFlat.text = it.qualityParameterValue
                    "B_BEANCOUNT" -> viewBinder.tvBeanCount.text = it.qualityParameterValue
                    "B_RESIDUE" -> viewBinder.tvResidue.text = it.qualityParameterValue
                    "B_MOIS_REFR" -> viewBinder.tvHumidityRefraction.text = it.qualityParameterValue
                    "B_MOULD_REFR" -> viewBinder.tvMouldRefraction.text = it.qualityParameterValue
                    "B_WAST_REFR" -> viewBinder.tvWasteRefraction.text = it.qualityParameterValue
                    "B_RESIDU_REFR" -> viewBinder.tvResidueRefraction.text = it.qualityParameterValue
                    "B_CLUSTER_REFR" -> viewBinder.tvClusterRefraction.text = it.qualityParameterValue
                    "B_SECONDARY_REFR" -> viewBinder.tvSecondaryRefraction.text = it.qualityParameterValue
                }
            }

            val totalDefects = mould + slaty
            viewBinder.tvTotalDefects.text = totalDefects.toString()
            viewBinder.tvBeanCountRefraction.text = ""
            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )
            if(printKeys.size>0){
                PreferenceHelper.save(OFFLOADING_RECEIPT_PRINT_KEY, printKeys.get(0))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    return printKeys
}


@SuppressLint("SuspiciousIndentation")
fun entrySheetCMCocoPrint(intent: Intent, context: Context): ArrayList<String> {
    var mould = 0.000
    var slaty = 0.000

    val receivingData: VegaQualityApproveCameroonWeighBridge = intent.getParcelableExtra(UIUtils.RECEIVING_DATA)
        ?: VegaQualityApproveCameroonWeighBridge()

    val lotList: ArrayList<VegaCoffeeSalesLots> = intent.getParcelableArrayListExtra(AppUtils.LOT_CARD) ?: ArrayList()

    val vegaBcApprovePostData: VegaQualityApproveCameroonPostData = intent.getParcelableExtra(UIUtils.QUALITY_DATA)
        ?: VegaQualityApproveCameroonPostData()

    val printKeys = ArrayList<String>()

        try {
            val view =
                LayoutInflater.from(context).inflate(R.layout.vega_cameroon_coco_qa_entry_sheet, null)
            val viewBinder = VegaCameroonCocoQaEntrySheetBinding.bind(view)

            // viewBinder.tvPrintOn.text= "Print on".plus(DateUtils.getCurrentDataForPrint())
            //  viewBinder.tvEntrySheetNo.text=
            viewBinder.tvMaterialName.text = receivingData.materialName
            // viewBinder.tvCampagin.text=
            viewBinder.tvBatchNo.text = receivingData.batchNumber
            viewBinder.tvDate.text = receivingData.year

            //  viewBinder.tvExporter.text= receivingData.n
            viewBinder.tvSupplier.text = receivingData.supplierName
            // viewBinder.tvTransporter.text=
            //  viewBinder.tvDriver.text= receivingData.dr
            viewBinder.tvTruck.text = receivingData.vehicleNumber
            //  viewBinder.tvDvLetterNo.text=
            viewBinder.tvStartingDate.text = receivingData.year
            //  viewBinder.tvOrigin.text=
            // viewBinder.tvRegion.text=receivingData.


            viewBinder.tvGrossWt.text = receivingData.grossWeight

            // viewBinder.tvPackingWt.text=
            //  viewBinder.tvWtAfterRefraction.text=
            // viewBinder.tvNetWtAfterRefraction.text=

            viewBinder.tvQualityAnalysisNo.text = receivingData.charg

            // viewBinder.tvObservation.text=
            // viewBinder.tvWeightRetained.text=

            lotList.forEachIndexed { index, item ->
                viewBinder.tvJuteBag.text = item.processOrderNo
                viewBinder.tvNylonBag.text = item.rsPos
                viewBinder.tvNoPallet.text = item.certificate
                viewBinder.tvTarePal.text = item.saleOrderId
                viewBinder.tvInspectionLot.text = item.bwart
            }


            vegaBcApprovePostData.qualityDetails.forEach {
                when (it.nameChar) {
                    "B_MOIST" -> viewBinder.tvMoisture.text = it.qualityParameterValue
                    "B_MOIS_REFR" -> viewBinder.tvMoistureRate.text = it.qualityParameterValue
                    "B_CLASSMENT" -> viewBinder.tvClassement.text = it.qualityParameterValue
                    "B_MOULD4" -> viewBinder.tvMold.text = it.qualityParameterValue
                    "DEFECTIVES" -> viewBinder.tvDefect.text = it.qualityParameterValue
                }
            }

            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }



    return printKeys
}




