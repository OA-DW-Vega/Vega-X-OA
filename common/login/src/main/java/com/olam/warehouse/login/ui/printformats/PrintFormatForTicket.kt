package com.olam.warehouse.login.ui.printformats

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.ItemPrintLotCardPreviewBinding
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmap
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.EUDR_STATUS
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.ATTR_UNKNOWN_QP_VALUE
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.isNumericToX
import com.olam.warehouse.presentation.utils.extension.visible

const val OFFLOADING_TICKET_PRINT_KEY = "offloading_ticket_print_key"


fun ticketPrint(intent: Intent, context: Context): ArrayList<String> {
    var fromCameroonCocoaGateEntry=false
    var fromCameroonCocoaaddContainer=false
    var fromCameroonCocoaQA=false
    var fromNigeriaCocoaQA=false
    val lotList = intent.getParcelableArrayListExtra(AppUtils.LOT_CARD)
        ?: java.util.ArrayList<VegaCoffeeSalesLots>()
    val printKeys = ArrayList<String>()
    var receiving= intent.getParcelableExtra<VegaQualityApproveCameroonWeighBridge>(UIUtils.RECEIVING_DATA)

    try {
        fromCameroonCocoaGateEntry =
            intent.getBooleanExtra(UIUtils.FROM_CAMEROON_GATEENTRY_COCOA, false) ?: false
        fromCameroonCocoaaddContainer =
            intent.getBooleanExtra(UIUtils.FROM_CAMEROON_COCOA_ADD_CONTAINER, false) ?: false
        fromCameroonCocoaQA =
            intent.getBooleanExtra(UIUtils.FROM_CAMEROON_COCOA_QA, false) ?: false
        fromNigeriaCocoaQA = intent.getBooleanExtra(UIUtils.FROM_NIGERIA_COCOA_QA, false) ?: false
    }catch (e:Exception){
        e.printStackTrace()
        //Toast.makeText(context, "error1", Toast.LENGTH_SHORT).show()

    }



    // DoAsync {
    try {
        lotList.forEachIndexed { index, item ->
            val view = LayoutInflater.from(context).inflate(R.layout.item_print_lot_card_preview, null)
            val viewBinder = ItemPrintLotCardPreviewBinding.bind(view)
            if (fromCameroonCocoaGateEntry) {
                viewBinder.tvLot.text = "Sample ID"
                viewBinder.tvWeight.visibility = View.GONE
                viewBinder.tvWeightValue.visibility = View.GONE
            }
            if (fromCameroonCocoaaddContainer) {
                viewBinder.tvLot.text = "Container ID"
                viewBinder.tvWeight.text = "Shipping Line"
                viewBinder.tvMaterial.text = "Container Size"
            }
            if (fromCameroonCocoaQA) {
                if (fromNigeriaCocoaQA) {
                    viewBinder.tvLot.text = context.getString(R.string.print_batch_number)
                }
            }
            viewBinder.ivPreview.setImageBitmap(getBitmap(item.batchNumber))
            viewBinder.tvLotValue.text = item.batchNumber
            viewBinder.tvMaterialValue.text = item.materialName
            if (!fromCameroonCocoaQA) {
                if (item.grade?.isNotEmpty() == true) {
                    viewBinder.tvGradeValue.text = item.grade
                    viewBinder.tvGrade.visible()
                    viewBinder.tvGradeValue.visible()
                }
                if (item.certificate?.isNotEmpty() == true) {
                    viewBinder.tvCertificateValue.text = item.certificate
                    viewBinder.tvCertificate.visible()
                    viewBinder.tvCertificateValue.visible()
                }
            }

                viewBinder.tvComplianceLabel.visible()
                viewBinder.tvComplianceValue.visible()
                viewBinder.tvComplianceValue.text = if(intent.getStringExtra(EUDR_STATUS).equals("1")) Constants.EUDR_QP_VALUE else ATTR_UNKNOWN_QP_VALUE

            if(receiving?.sourceLotId?.isNotEmpty() == true) {
                viewBinder.tvSrcLotLabel.visible()
                viewBinder.tvSrcLotValue.visible()
                viewBinder.tvSrcLotValue.text = receiving.sourceLotId
            }


            viewBinder.tvWeightValue.text = if(isNumericToX(item.editedWeight.toString()))item.editedWeight?.toDouble()?.formatThreeDigits().plus(" ").plus(item.unitOfMeasure) else item.editedWeight

            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        viewBinder.root, Color.WHITE
                    )
                )
            )
            if(printKeys.size>0){
                PreferenceHelper.save(OFFLOADING_TICKET_PRINT_KEY, printKeys.get(0))
            }
        }

    }catch (e:Exception){
        e.printStackTrace()
       // Toast.makeText(context, "error2", Toast.LENGTH_SHORT).show()
    }

    //  }.execute()

    return printKeys
}
