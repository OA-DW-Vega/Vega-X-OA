package com.olam.warehouse.login.ui.printformats

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.ItemStockReconAuditDetailsBinding
import com.olam.warehouse.login.databinding.VegaStockReconAuditDetailsReciptBinding
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.vega.model.VegaStockReconBagDetails
import com.olam.warehouse.master.vega.model.VegaStockReconGetAllAuditData
import com.olam.warehouse.presentation.adapter.setUpAdapter

fun stockReconPrintRecipt(
    bagDetails: VegaStockReconBagDetails,
    auditDetails: ArrayList<VegaStockReconGetAllAuditData>,
    context: Context
): ArrayList<String> {
    var printKeys = ArrayList<String>()
    var listSize = auditDetails.size
    if (auditDetails.size > 0) {
        printKeys.add(
            bitmapToString(
                getBitmapFromView(
                    getView(
                        context,
                        auditDetails.take(9) as MutableList<VegaStockReconGetAllAuditData>, bagDetails
                    ), Color.WHITE
                )
            )
        )
    }

    if (auditDetails.size >= 10) {
        if (listSize >= 10 && listSize >= 20) {
            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        getView(
                            context,
                            auditDetails.slice(10..19) as ArrayList<VegaStockReconGetAllAuditData>, bagDetails
                        ), Color.WHITE
                    )
                )
            )
        } else {
            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        getView(
                            context,
                            auditDetails.slice(10..listSize - 1) as ArrayList<VegaStockReconGetAllAuditData>, bagDetails
                        ), Color.WHITE
                    )
                )
            )
        }
    }
    if (auditDetails.size >= 20) {
        if (listSize >= 20 && listSize >= 30) {
            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        getView(
                            context,
                            auditDetails.slice(20..29) as ArrayList<VegaStockReconGetAllAuditData>, bagDetails
                        ), Color.WHITE
                    )
                )
            )
        } else {
            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        getView(
                            context,
                            auditDetails.slice(20..listSize - 1) as ArrayList<VegaStockReconGetAllAuditData>, bagDetails
                        ), Color.WHITE
                    )
                )
            )
        }
    }
    if (auditDetails.size >= 30) {
        if (listSize >= 30 && listSize >= 40) {
            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        getView(
                            context,
                            auditDetails.slice(30..39) as ArrayList<VegaStockReconGetAllAuditData>, bagDetails
                        ), Color.WHITE
                    )
                )
            )
        } else {
            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        getView(
                            context,
                            auditDetails.slice(30..listSize - 1) as ArrayList<VegaStockReconGetAllAuditData>, bagDetails
                        ), Color.WHITE
                    )
                )
            )
        }

    }
    if (auditDetails.size >= 40) {
        if (listSize >= 40 && listSize >= 50) {
            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        getView(
                            context,
                            auditDetails.slice(40..49) as ArrayList<VegaStockReconGetAllAuditData>, bagDetails
                        ), Color.WHITE
                    )
                )
            )
        } else {
            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        getView(
                            context,
                            auditDetails.slice(40..listSize - 1) as ArrayList<VegaStockReconGetAllAuditData>, bagDetails
                        ), Color.WHITE
                    )
                )
            )
        }
    }

    return printKeys
}

fun getView(
    context: Context,
    auditDetails: MutableList<VegaStockReconGetAllAuditData>,
    bagDetails: VegaStockReconBagDetails
): View {
    val view = LayoutInflater.from(context).inflate(R.layout.vega_stock_recon_audit_details_recipt, null)
    val viewBinder = VegaStockReconAuditDetailsReciptBinding.bind(view)
    viewBinder.rvReconDetails.setUpAdapter(
        auditDetails.toMutableList(),
        R.layout.item_stock_recon_audit_details,
        ItemStockReconAuditDetailsBinding::inflate,
        { it, pos, bindingItem ->
            val item = auditDetails.get(pos)
            bindingItem.row1.setText(item.reconId)
            bindingItem.row2.setText(bagDetails.reconIdDetails.createdAt?.split(" ")?.get(0).toString())
            bindingItem.row3.setText(bagDetails.reconIdDetails.plant.plus("-").plus(bagDetails.plant.plantName))
            bindingItem.row4.setText(bagDetails.reconIdDetails.plant.plus("-").plus(bagDetails.plant.plantName))
            bindingItem.row5.setText(bagDetails.reconIdDetails.storageLocation)
            bindingItem.row6.setText(item.lotNumber)
            bindingItem.row7.setText(item.material)
            bindingItem.row8.setText(item.systemNetWeight.plus(item.unitOfMeasure))
            bindingItem.row9.setText(item.totalNoOfBags1)
            bindingItem.row10.setText(getBagType(item))
            bindingItem.row11.setText(getFullBagCount(item))
            bindingItem.row12.setText(getHalfBagCount(item))
            bindingItem.row13.setText(if (item.bagDamaged) "Yes" else "No")
            bindingItem.row14.setText(item.noOfDamagedBags)
            bindingItem.row15.setText(item.weightGainLoss.plus(item.unitOfMeasure))
            bindingItem.row16.setText(item.remarks)
        })

    return view

}

private fun getBagType(auditDetails: VegaStockReconGetAllAuditData): String {
    var bagType = ""
    if (auditDetails.bagType1?.isNotEmpty() == true) {
        bagType = auditDetails?.bagType1 ?: ""
    }
    if (auditDetails.bagType2?.isNotEmpty() == true) {
        bagType = auditDetails.bagType1.plus(",").plus(auditDetails.bagType2)
    }
    if (auditDetails.bagType3?.isNotEmpty() == true) {
        bagType = auditDetails.bagType1.plus(",").plus(auditDetails.bagType2).plus(",").plus(auditDetails.bagType3)
    }
    return bagType
}

private fun getFullBagCount(auditDetails: VegaStockReconGetAllAuditData): String {
    var fullBagCount = 0
    if (auditDetails.noOfFullBags1?.isNotEmpty() == true) {
        fullBagCount = auditDetails.noOfFullBags1.let { it?.toInt() ?: 0 }
    }
    if (auditDetails.noOfFullBags2?.isNotEmpty() == true) {
        fullBagCount =
            auditDetails.noOfFullBags1.let { it?.toInt() ?: 0 } + auditDetails.noOfFullBags2.let { it?.toInt() ?: 0 }
    }
    if (auditDetails.noOfFullBags3?.isNotEmpty() == true) {
        fullBagCount =
            auditDetails.noOfFullBags1.let { it?.toInt() ?: 0 } + auditDetails.noOfFullBags2.let {
                it?.toInt() ?: 0
            } + auditDetails.noOfFullBags3.let { it?.toInt() ?: 0 }
    }
    return fullBagCount.toString()
}

private fun getHalfBagCount(auditDetails: VegaStockReconGetAllAuditData): String {
    var halfBagCount = 0
    if (auditDetails.noOfHalfBags1?.isNotEmpty() == true) {
        halfBagCount = auditDetails.noOfHalfBags1.let { it?.toInt() ?: 0 }
    }
    if (auditDetails.noOfHalfBags2?.isNotEmpty() == true) {
        halfBagCount =
            auditDetails.noOfHalfBags1.let { it?.toInt() ?: 0 } + auditDetails.noOfHalfBags2.let { it?.toInt() ?: 0 }
    }
    if (auditDetails.noOfHalfBags3?.isNotEmpty() == true) {
        halfBagCount =
            auditDetails.noOfHalfBags1.let { it?.toInt() ?: 0 } + auditDetails.noOfHalfBags2.let {
                it?.toInt() ?: 0
            } + auditDetails.noOfHalfBags3.let { it?.toInt() ?: 0 }
    }
    return halfBagCount.toString()
}
