package com.olam.warehouse.login.ui.printformats

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.view.LayoutInflater
import android.view.View
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.*
import com.olam.warehouse.login.utils.*
import com.olam.warehouse.master.common.model.VegaCommonSplitLotModel
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("SuspiciousIndentation")
fun NicaraguaMTNRPrintReceipt(intent: Intent, context: Context?): ArrayList<String> {
    var receivingData = VegaReceiving()
    var issplit_lot: Boolean = false
    var splitlots = arrayListOf<VegaCommonSplitLotModel>()
    receivingData = intent.getParcelableExtra<VegaReceiving>(UIUtils.RECEIVING_DATA) ?: VegaReceiving()
    issplit_lot = intent.getBooleanExtra(UIUtils.ISSPLIT_LOT, false)
    if (issplit_lot)
        splitlots = intent.getParcelableArrayListExtra<VegaCommonSplitLotModel>(UIUtils.SPLIT_LOTS) ?: ArrayList()
    var printKeys = ArrayList<String>()
    DoAsync {
        try {
            val view =
                LayoutInflater.from(context)
                    .inflate(R.layout.vega_nicaragua_mtnr_tallysheet_print, null)
            val viewBinder = VegaNicaraguaMtnrTallysheetPrintBinding.bind(view)
            UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogo)
            UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogoRp)
            viewBinder.tvmtnrDocSequence.text =
                receivingData.mtnrDocSequence.toString()
            viewBinder.tvmtnrDocSequenceRp.text =
                receivingData.mtnrDocSequence.toString()
            viewBinder.origenValue.text =
                receivingData.origin.plus(" - ")
                    .plus(receivingData.supplierName)
            viewBinder.origenValueRp.text =
                receivingData.origin.plus(" - ")
                    .plus(receivingData.supplierName)

            viewBinder.tvmtnrDeliverynumber.text = receivingData.delivery
            viewBinder.tvmtnrDeliverynumberRp.text = receivingData.delivery

            if (receivingData.materialName!!.contains("Tolling", true)) {
                viewBinder.vendorValue.text = receivingData.tollingVendorName
                viewBinder.vendorValueRp.text = receivingData.tollingVendorName
            }

            viewBinder.origenValue.text = receivingData.origin.plus(" - ")
                .plus(receivingData.supplierName)
            viewBinder.origenValueRp.text = receivingData.origin.plus(" - ")
                .plus(receivingData.supplierName)

            viewBinder.dateandHourValue.text = DateUtils.getDate(
                Calendar.getInstance().timeInMillis,
                "dd-MMM-yyyy hh:mm:ss"
            )
            viewBinder.dateandHourValueRp.text = DateUtils.getDate(
                Calendar.getInstance().timeInMillis,
                "dd-MMM-yyyy hh:mm:ss"
            )

            viewBinder.tvRemarksCopy.text = receivingData.remarks
            viewBinder.tvRemarksCopyRp.text = receivingData.remarks

            viewBinder.tvSLossCopy.text = receivingData.tareWeight1
            viewBinder.tvSLossCopyRp.text = receivingData.tareWeight1


            viewBinder.truckNoValue.text =
                receivingData.transportVendorCode.plus(" - ")
                    .plus(receivingData.transportVendorName)
            viewBinder.truckNoValueRp.text =
                receivingData.transportVendorCode.plus(" - ")
                    .plus(receivingData.transportVendorName)

            viewBinder.recibidoValue.text =
                receivingData.plantId.plus(" - ").plus(receivingData.plantName)
            viewBinder.recibidoValueRp.text =
                receivingData.plantId.plus(" - ").plus(receivingData.plantName)

            viewBinder.conductorValue.text = receivingData.driverName
            viewBinder.conductorValueRp.text = receivingData.driverName

            viewBinder.locationValue.text = receivingData.vehicleNumber
            viewBinder.locationValueRp.text = receivingData.vehicleNumber

            if (receivingData.certificate!!.length > 0) {
                viewBinder.certificationValue.text = receivingData.certificate
                viewBinder.certificationValueRp.text = receivingData.certificate
            }
            viewBinder.tvBagValue.append(receivingData.bagCount)
            viewBinder.tvBagValueRp.append(receivingData.bagCount)

            viewBinder.tvTareWeightValue.text = receivingData.tareWeight
            viewBinder.tvTareWeightValueRp.text = receivingData.tareWeight

            viewBinder.tvGrossWeightValue.append(formatTwoDigString(receivingData.grossWeight.toString(), context))
            viewBinder.tvGrossWeightValueRp.append(formatTwoDigString(receivingData.grossWeight.toString(), context))
            viewBinder.tvNetWeightValue.append(formatTwoDigString(receivingData.netWeight, context))
            viewBinder.tvNetWeightValueRp.append(formatTwoDigString(receivingData.netWeight, context))

            viewBinder.tvGrossQQsValue.append(receivingData.batchNumber)
            viewBinder.tvGrossQQsValueRp.append(receivingData.batchNumber)

            viewBinder.tvTicketValue.append(receivingData.palletType)
            viewBinder.tvTicketValueRp.append(receivingData.palletType)
            if (receivingData.qualityGradeDesc?.isNotEmpty() == true) {
                viewBinder.tvQualityGradeValue.append(receivingData.qualityGradeDesc)
                viewBinder.tvQualityGradeValueRp.append(receivingData.qualityGradeDesc)
            } else {
                viewBinder.tvQualityGradeValue.append(receivingData.gradeDesc)
                viewBinder.tvQualityGradeValueRp.append(receivingData.gradeDesc)
            }

            viewBinder.tvMaterialname.append(receivingData.materialName)
            viewBinder.tvMaterialnameRp.append(receivingData.materialName)

            when (receivingData.certificate) {
                UIUtils.NICERTFD_SBUX -> {
                    viewBinder.mtnrCertificationLogo.setImageDrawable(
                        context!!.resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo)
                    )
                    viewBinder.mtnrCertificationLogo.visibility = View.VISIBLE
                    viewBinder.mtnrCertificationLogoRp.setImageDrawable(
                        context.resources.getDrawable(com.olam.warehouse.presentation.R.drawable.starbucks_logo)
                    )
                    viewBinder.mtnrCertificationLogoRp.visibility = View.VISIBLE
                }
                UIUtils.NICERTFD_UTZ -> {
                    viewBinder.mtnrCertificationLogo.setImageDrawable(context!!.resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                    viewBinder.mtnrCertificationLogo.visibility = View.VISIBLE
                    viewBinder.mtnrCertificationLogoRp.setImageDrawable(context.resources.getDrawable(com.olam.warehouse.presentation.R.drawable.utz_logo))
                    viewBinder.mtnrCertificationLogoRp.visibility = View.VISIBLE
                }
                UIUtils.NICERTFD_RFA -> {
                    viewBinder.mtnrCertificationLogo.setImageDrawable(context!!.resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                    viewBinder.mtnrCertificationLogo.visibility = View.VISIBLE
                    viewBinder.mtnrCertificationLogoRp.setImageDrawable(context.resources.getDrawable(com.olam.warehouse.presentation.R.drawable.rfa_logo))
                    viewBinder.mtnrCertificationLogoRp.visibility = View.VISIBLE
                }
                else -> {
                    viewBinder.mtnrCertificationLogo.visibility = View.GONE
                    viewBinder.mtnrCertificationLogoRp.visibility = View.GONE
                }
            }
            if (issplit_lot && splitlots.isNotEmpty()) {
                viewBinder.qualityTable1.gone()
                viewBinder.qualityTable1Rp.gone()
                viewBinder.rvtablelayout.visible()
                viewBinder.rvtablelayoutRp.visible()
                if (splitlots.size >1)
                setAdapter1(viewBinder, splitlots,receivingData)
                else
                {  viewBinder.qualityTable1.visible()
                    viewBinder.qualityTable1Rp.visible()
                    viewBinder.rvtablelayout.gone()
                    viewBinder.rvtablelayoutRp.gone()
                }

            }
            printKeys.add(
                bitmapToString(
                    getBitmapFromView(view, Color.WHITE)
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()

        }

    }.execute()
    return printKeys
}

private fun setAdapter1(
    viewBinder: VegaNicaraguaMtnrTallysheetPrintBinding,
    splitlots: ArrayList<VegaCommonSplitLotModel>,
    receivingData: VegaReceiving
) {
    viewBinder.rvtablelayout.setUpAdapter(
        splitlots,
        R.layout.item_vega_nicaragua_tablelayout,
        ItemVegaNicaraguaTablelayoutBinding::inflate,
        { it, pos, bindingItem ->
            bindingItem.tvQualityGradeValue.text =it.qualityGradeDesc
            bindingItem.tvMaterialname.text = receivingData.materialName
            bindingItem.tvBagValue.text =it.bagCount
            bindingItem.tvGrossWeightValue.text = it.netWeight
            bindingItem.tvTareWeightValue.text = receivingData.tareWeight
            bindingItem.tvNetWeightValue.text = it.netWeight
            bindingItem.tvGrossQQsValue.append(it.batchNumber)
            bindingItem.tvTicketValue.append(it.ticketNumber)

        })
    viewBinder.rvtablelayoutRp.setUpAdapter(
        splitlots,
        R.layout.item_vega_nicaragua_tablelayout,
        ItemVegaNicaraguaTablelayoutBinding::inflate,
        { it, pos, bindingItem ->
            bindingItem.tvQualityGradeValue.text =it.qualityGradeDesc
            bindingItem.tvMaterialname.text = receivingData.materialName
            bindingItem.tvBagValue.text =it.bagCount
            bindingItem.tvGrossWeightValue.text = it.netWeight
            bindingItem.tvTareWeightValue.text = receivingData.tareWeight
            bindingItem.tvNetWeightValue.text = it.netWeight
            bindingItem.tvGrossQQsValue.append(it.batchNumber)
            bindingItem.tvTicketValue.append(it.ticketNumber)

        })
}


private fun formatTwoDigString(str: String, context: Context?): String {
    if (str.isEmpty() || !str.contains(".") || !str.contains(",")) return str
    val strFormat = String.format(Locale.ENGLISH, "%.2f", str)
    return strFormat.format(context).replace(",", "")
}

fun NicaraguaMTNRPrintTicket(intent: Intent, context: Context?): ArrayList<String> {
    var receivingData = VegaReceiving()
    /* if(intent.hasExtra(UIUtils.RECEIVING_DATA)) {
         val bundle = intent.extras
         receivingData = bundle?.getParcelable<VegaReceiving>(UIUtils.RECEIVING_DATA)?: VegaReceiving()
     }*/
    receivingData = intent.getParcelableExtra<VegaReceiving>(UIUtils.RECEIVING_DATA) ?: VegaReceiving()
    var printKeys = ArrayList<String>()
    try {
        DoAsync {
            val view =
                LayoutInflater.from(context)
                    .inflate(R.layout.item_print_ticket_preview_nicaragua, null)
            val viewBinder = ItemPrintTicketPreviewNicaraguaBinding.bind(view)
            //viewBinder.ivPreview.setImageBitmap(getBitmap(receivingData.palletType.toString().trim()))
            viewBinder.ivPreview.setImageBitmap(getBitmap(receivingData.batchNumber.toString().trim()))
            viewBinder.tvLotValue.text = receivingData.palletType?.trim()
            viewBinder.tvMaterialValue.text = receivingData.erdat?.trim()
            viewBinder.tvGradeValue.text =
                receivingData.gradeDesc?.trim().plus(" - ").plus(receivingData.qualityGradeDesc?.trim())
            viewBinder.tvCertificateValue.text = receivingData.certificate?.trim()
            viewBinder.tvWeightValue.text =
                receivingData.netWeight.trim().plus(" ").plus(receivingData.unitsOfMeasure)
                    .trim()
            if (receivingData.materialName?.contains("tolling", true) == true)
                viewBinder.tvClientValue.text = receivingData.tollingVendorName.toString()
            else {
                viewBinder.tvClientValue.gone()
                viewBinder.tvClient.gone()
            }
            viewBinder.tvSacksValue.text = receivingData.bagCount?.trim()
            viewBinder.tvStLoc.visible()
            viewBinder.tvStLocValue.visible()
            viewBinder.tvStLocValue.text = receivingData.storageLocationCode


            printKeys.add(
                bitmapToString(
                    getBitmapFromViewforNic(view, Color.WHITE)
                )
            )

        }.execute()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return printKeys
}

fun getBitmapFromViewforNic(view: View, defaultColor: Int): Bitmap {
    view.measure(98, 152)
    var bitmap =
        Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
    var canvas = Canvas(bitmap)
    canvas.drawColor(defaultColor)
    view.layout(view.left, view.top, view.right, view.bottom)
    view.draw(canvas)
    return bitmap
}

fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
    val width = bm.width
    val height = bm.height
    val scaleWidth = newWidth.toFloat() / width
    val scaleHeight = newHeight.toFloat() / height
    // CREATE A MATRIX FOR THE MANIPULATION
    val matrix = Matrix()
    // RESIZE THE BIT MAP
    matrix.postScale(scaleWidth, scaleHeight)

    // "RECREATE" THE NEW BITMAP
    val resizedBitmap = Bitmap.createBitmap(
        bm, 0, 0, width, height, matrix, false
    )
    bm.recycle()
    return resizedBitmap
}

fun generateFgrnTallySheetBitMap(ticketNo: String, context: Context?): ArrayList<String> {
    var printKeys = ArrayList<String>()
    try {
        DoAsync {
            val view =
                LayoutInflater.from(context).inflate(R.layout.item_print_fgrn_tallysheet_preview, null)
            val viewBinder = ItemPrintFgrnTallysheetPreviewBinding.bind(view)
            viewBinder.ivPreview.setImageBitmap(getBitmap(ticketNo))
            viewBinder.tvLotValue.text = ticketNo
            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )
        }.execute()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return printKeys
}
