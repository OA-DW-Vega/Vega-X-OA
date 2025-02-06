package com.olam.warehouse.login.ui.printformats

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.VegaNigeriaCocoaBagMgmtPrintReceiptBinding
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.common.model.VegaNigeriaCocoaBagIssue
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DoAsync

fun nigeriaCocoaBagMgmtPrintRecipt(intent: Intent, context: Context): ArrayList<String> {
    var bagMgmtDetails = VegaNigeriaCocoaBagIssue()
    bagMgmtDetails = intent.getParcelableExtra(AppUtils.BAG_MGMT) ?: VegaNigeriaCocoaBagIssue()
    var printKeys = ArrayList<String>()
    DoAsync {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.vega_nigeria_cocoa_bag_mgmt_print_receipt, null)
        val viewBinder = VegaNigeriaCocoaBagMgmtPrintReceiptBinding.bind(view)
        viewBinder.colOne.text = "1"
        viewBinder.colTwo.text = bagMgmtDetails.documentNumber
        viewBinder.colThree.text = bagMgmtDetails.materialName
        viewBinder.colFour.text = bagMgmtDetails.supplierName
        viewBinder.colFive.text = bagMgmtDetails.storageLocationCode.plus("-").plus(bagMgmtDetails.storageLocationName)
        viewBinder.colSix.text = bagMgmtDetails.gatePassNum
        viewBinder.colSeven.text = bagMgmtDetails.bagIssued
        viewBinder.colEight.text = bagMgmtDetails.currentBalance
        var balance = if(bagMgmtDetails.screenType?.equals("bag_issue_fragment") == true) ((bagMgmtDetails.currentBalance?.toDouble()?.toLong() ?: 0) - (bagMgmtDetails.bagIssued?.toLong()
            ?: 0)).toString() else ((bagMgmtDetails.currentBalance?.toDouble()?.toLong() ?: 0) + (bagMgmtDetails.bagIssued?.toLong()
            ?: 0)).toString()
        viewBinder.colNine.text = balance
        printKeys.add(
            bitmapToString(
                getBitmapFromView(view, Color.WHITE)
            )
        )

    }.execute()
    return printKeys
}
