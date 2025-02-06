package com.olam.warehouse.login.ui.printformats

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.ItemTxnHisGrnBinding
import com.olam.warehouse.login.databinding.ItemTxnHisMtntBinding
import com.olam.warehouse.login.databinding.ItemTxnHisMtnrBinding
import com.olam.warehouse.login.databinding.VegaTransHisGrnBinding
import com.olam.warehouse.login.databinding.VegaTransHisMtntBinding
import com.olam.warehouse.login.databinding.VegaTransHisMtnrBinding
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.vega.model.VegaFGRNHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaGRNHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaMtntHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaMtnrHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaRMINHistoryTransactions
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone


fun printForTransactionHistoryGRN(intent: Intent, context: Context): ArrayList<String> {
    val historyList : ArrayList<VegaGRNHistoryTransactions> = intent.getParcelableArrayListExtra(UIUtils.HISTORY_DATA_GRN)!!
    val historyListNew = arrayListOf<VegaGRNHistoryTransactions>()
    val printKeys = ArrayList<String>()

    historyList.forEachIndexed { index, it ->
        val newItem = it.copy()
        newItem.itemIndex = index+1
        historyListNew.add(newItem)
    }
    try {

        val view =
            LayoutInflater.from(context).inflate(R.layout.vega_trans_his_grn, null)
        val viewBinder = VegaTransHisGrnBinding.bind(view)
        val noOfBlock = historyListNew.size.div(20)
        val noOfBlockBalnace = historyListNew.size % 20
        viewBinder.trHeader.weightSum=9f
        viewBinder.tvVendorNameLabel.gone()


        for (item: Int in 0 until noOfBlock) {
            val subItem = historyListNew.subList(item * 20, (item + 1) * 20).toMutableList()
            viewBinder.rvList.setUpAdapter(subItem.toMutableList(),
                R.layout.item_txn_his_grn,
                ItemTxnHisGrnBinding::inflate,
                { item, pos, bindingItem ->
                    bindingItem.trSubHeader.weightSum= 9f
                    bindingItem.tvSNo.text = item.itemIndex.toString()
                    bindingItem.tvGrnNo.text = item.grnNumber
                    bindingItem.tvMaterial.text = item.materialCode
                    bindingItem.tvQnty.text = item.quantity
                    bindingItem.tvDate.text = DateUtils.getFormatedDate(item.postingDate.toString())
                   // bindingItem.tvVendorName.text = item.vendorName
                    bindingItem.tvVendorName.gone()
                    bindingItem.tvVendorCode.text = item.vendorCode
                    bindingItem.tvGrnPrice.text = item.totalPrice
                    bindingItem.tvTotPrPerUnit.text = item.pricePerUnit
                    bindingItem.tvStLoc.text = item.storageLocationCode
                })

            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )
        }
        if(noOfBlock==0 || noOfBlockBalnace >0){
            val historyListRemain = historyListNew.takeLast(noOfBlockBalnace)
            viewBinder.rvList.setUpAdapter(historyListRemain.toMutableList(),
                R.layout.item_txn_his_grn,
                ItemTxnHisGrnBinding::inflate,
                { item, pos, bindingItem ->
                    bindingItem.trSubHeader.weightSum= 9f
                    bindingItem.tvSNo.text = item.itemIndex.toString()
                    bindingItem.tvGrnNo.text = item.grnNumber
                    bindingItem.tvMaterial.text = item.materialCode
                    bindingItem.tvQnty.text = item.quantity
                    bindingItem.tvDate.text = DateUtils.getFormatedDate(item.postingDate.toString())
                    // bindingItem.tvVendorName.text = item.vendorName
                    bindingItem.tvVendorName.gone()
                    bindingItem.tvVendorCode.text = item.vendorCode
                    bindingItem.tvGrnPrice.text = item.totalPrice
                    bindingItem.tvTotPrPerUnit.text = item.pricePerUnit
                    bindingItem.tvStLoc.text = item.storageLocationCode
                })

            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )
        }


    }catch (e:Exception){
        e.printStackTrace()
    }

    return printKeys
}

fun printForTransactionHistoryRmin(intent: Intent, context: Context): ArrayList<String> {
    val historyList : ArrayList<VegaRMINHistoryTransactions> = intent.getParcelableArrayListExtra(UIUtils.HISTORY_DATA_RMIN)!!
    val historyListNew = arrayListOf<VegaRMINHistoryTransactions>()
    val printKeys = ArrayList<String>()

    historyList.forEachIndexed { index, it ->
        val newItem = it.copy()
        newItem.itemIndex = index+1
        historyListNew.add(newItem)
    }

    try {

        val view =
            LayoutInflater.from(context).inflate(R.layout.vega_trans_his_grn, null)
        val viewBinder = VegaTransHisGrnBinding.bind(view)

        val noOfBlock = historyListNew.size.div(20)
        val noOfBlockBalnace = historyListNew.size % 20
        viewBinder.trHeader.weightSum=5f
        viewBinder.tvGrnNoLabel.text= context.getString(R.string.txn_po_number)
        viewBinder.tvVendorNameLabel.gone()
        viewBinder.tvVendorCodeLabel.gone()
        viewBinder.tvGrnPriceLabel.gone()
        viewBinder.tvTotPerUnit.gone()
        viewBinder.tvStLocLabel.gone()

        for (item: Int in 0 until noOfBlock) {
            val subItem = historyListNew.subList(item * 20, (item + 1) * 20).toMutableList()
            viewBinder.rvList.setUpAdapter(subItem.toMutableList(),
                R.layout.item_txn_his_grn,
                ItemTxnHisGrnBinding::inflate,
                { item, pos, bindingItem ->
                    bindingItem.trSubHeader.weightSum=5f
                    bindingItem.tvSNo.text = item.itemIndex.toString()
                    bindingItem.tvGrnNo.text = item.poNumber
                    bindingItem.tvMaterial.text= item.materialDetail?.materialCode
                    bindingItem.tvQnty.text= item.quantity
                    bindingItem.tvDate.text = DateUtils.getFormatedDate(item.date.toString())

                    bindingItem.tvVendorName.gone()
                    bindingItem.tvVendorCode.gone()
                    bindingItem.tvGrnPrice.gone()
                    bindingItem.tvTotPrPerUnit.gone()
                    bindingItem.tvStLoc.gone()


                })

            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )
        }

        if(noOfBlock==0 || noOfBlockBalnace >0){
            val historyListRemain = historyListNew.takeLast(noOfBlockBalnace)
            viewBinder.rvList.setUpAdapter(historyListRemain.toMutableList(),
                R.layout.item_txn_his_grn,
                ItemTxnHisGrnBinding::inflate,
                { item, pos, bindingItem ->
                    bindingItem.trSubHeader.weightSum=5f
                    bindingItem.tvSNo.text = item.itemIndex.toString()
                    bindingItem.tvGrnNo.text = item.poNumber
                    bindingItem.tvMaterial.text= item.materialDetail?.materialCode
                    bindingItem.tvQnty.text= item.quantity
                    bindingItem.tvDate.text = DateUtils.getFormatedDate(item.date.toString())

                    bindingItem.tvVendorName.gone()
                    bindingItem.tvVendorCode.gone()
                    bindingItem.tvGrnPrice.gone()
                    bindingItem.tvTotPrPerUnit.gone()
                    bindingItem.tvStLoc.gone()
                })

            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )
        }

    }catch (e:Exception){
        e.printStackTrace()
    }

    return printKeys
}

fun printForTransactionHistoryFGRN(intent: Intent, context: Context): ArrayList<String> {
    val historyList : ArrayList<VegaFGRNHistoryTransactions> = intent.getParcelableArrayListExtra(UIUtils.HISTORY_DATA_FGRN)!!
    val historyListNew = arrayListOf<VegaFGRNHistoryTransactions>()
    val printKeys = ArrayList<String>()

    historyList.forEachIndexed { index, it ->
        val newItem = it.copy()
        newItem.itemIndex = index+1
        historyListNew.add(newItem)
    }

    try {

        val view =
            LayoutInflater.from(context).inflate(R.layout.vega_trans_his_grn, null)
        val viewBinder = VegaTransHisGrnBinding.bind(view)
        val noOfBlock = historyListNew.size.div(20)
        val noOfBlockBalnace = historyListNew.size % 20

        viewBinder.trHeader.weightSum=5f
        viewBinder.tvGrnNoLabel.text= context.getString(R.string.txn_po_number)
        viewBinder.tvMaterialLabel.text= context.getString(R.string.txn_drying_loss)
        viewBinder.tvVendorNameLabel.gone()
        viewBinder.tvVendorCodeLabel.gone()
        viewBinder.tvGrnPriceLabel.gone()
        viewBinder.tvTotPerUnit.gone()
        viewBinder.tvStLocLabel.gone()


        for (item: Int in 0 until noOfBlock) {
            val subItem = historyListNew.subList(item * 20, (item + 1) * 20).toMutableList()
            viewBinder.rvList.setUpAdapter(subItem.toMutableList(),
                R.layout.item_txn_his_grn,
                ItemTxnHisGrnBinding::inflate,
                { item, pos, bindingItem ->
                    bindingItem.trSubHeader.weightSum=5f
                    bindingItem.tvSNo.text = item.itemIndex.toString()
                    bindingItem.tvGrnNo.text = item.poNumber
                    bindingItem.tvMaterial.text= item.dryingLoss
                    bindingItem.tvQnty.text= item.quantity
                    bindingItem.tvDate.text = DateUtils.getFormatedDate(item.date.toString())

                    bindingItem.tvVendorName.gone()
                    bindingItem.tvVendorCode.gone()
                    bindingItem.tvGrnPrice.gone()
                    bindingItem.tvTotPrPerUnit.gone()
                    bindingItem.tvStLoc.gone()
                })

            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )
        }


        if(noOfBlock==0 || noOfBlockBalnace >0){
            val historyListRemain = historyListNew.takeLast(noOfBlockBalnace)
            viewBinder.rvList.setUpAdapter(historyListRemain.toMutableList(),
                R.layout.item_txn_his_grn,
                ItemTxnHisGrnBinding::inflate,
                { item, pos, bindingItem ->
                    bindingItem.trSubHeader.weightSum=5f
                    bindingItem.tvSNo.text = item.itemIndex.toString()
                    bindingItem.tvGrnNo.text = item.poNumber
                    bindingItem.tvMaterial.text= item.dryingLoss
                    bindingItem.tvQnty.text= item.quantity
                    bindingItem.tvDate.text = DateUtils.getFormatedDate(item.date.toString())

                    bindingItem.tvVendorName.gone()
                    bindingItem.tvVendorCode.gone()
                    bindingItem.tvGrnPrice.gone()
                    bindingItem.tvTotPrPerUnit.gone()
                    bindingItem.tvStLoc.gone()
                })

            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )
        }

    }catch (e:Exception){
        e.printStackTrace()
    }

    return printKeys
}

fun printForTransactionHistoryMTNT(intent: Intent, context: Context): ArrayList<String> {
    val historyList : ArrayList<VegaMtntHistoryTransactions> = intent.getParcelableArrayListExtra(UIUtils.HISTORY_DATA_MTNT)!!
    val historyListNew = arrayListOf<VegaMtntHistoryTransactions>()
    val printKeys = ArrayList<String>()

    historyList.forEachIndexed { index, it ->
        val newItem = it.copy()
        newItem.itemIndex = index+1
        historyListNew.add(newItem)
    }

    try {

        val view =
            LayoutInflater.from(context).inflate(R.layout.vega_trans_his_mtnt, null)
        val viewBinder = VegaTransHisMtntBinding.bind(view)
        val noOfBlock = historyListNew.size.div(20)
        val noOfBlockBalnace = historyListNew.size % 20

        for (item: Int in 0 until noOfBlock) {
            val subItem = historyListNew.subList(item * 20, (item + 1) * 20).toMutableList()
            viewBinder.rvListMtnt.setUpAdapter(subItem.toMutableList(),
                R.layout.item_txn_his_mtnt,
                ItemTxnHisMtntBinding::inflate,
                { item, pos, bindingItem ->
                    bindingItem.tvSNo.text = item.itemIndex.toString()
                    bindingItem.tvWeighbridgeId.text=item.wbId
                    bindingItem.tvDate.text=DateUtils.getFormatedDate(item.postingDate.toString())
                    bindingItem.tvDestinationWh.text=item.destinationLocation
                    bindingItem.tvQnty.text=item.quantity
                    bindingItem.tvUom.text=item.uom
                    bindingItem.tvMaterial.text=item.materialCode
                    bindingItem.tvDriverName.text=item.driverName
                    bindingItem.tvDriverLicence.text=item.driverLicenseNumber
                    bindingItem.tvTruckNo.text=item.truckNumber
                    bindingItem.tvTransporter.text=item.transporterName
                    bindingItem.stoNo.text=item.deliveryNumber

                })

            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )
        }

        if(noOfBlock==0 || noOfBlockBalnace >0) {
            val historyListRemain = historyListNew.takeLast(noOfBlockBalnace)

            viewBinder.rvListMtnt.setUpAdapter(historyListRemain.toMutableList(),
                R.layout.item_txn_his_mtnt,
                ItemTxnHisMtntBinding::inflate,
                { item, pos, bindingItem ->
                    bindingItem.tvSNo.text = item.itemIndex.toString()
                    bindingItem.tvWeighbridgeId.text=item.wbId
                    bindingItem.tvDate.text=DateUtils.getFormatedDate(item.postingDate.toString())
                    bindingItem.tvDestinationWh.text=item.destinationLocation
                    bindingItem.tvQnty.text=item.quantity
                    bindingItem.tvUom.text=item.uom
                    bindingItem.tvMaterial.text=item.materialCode
                    bindingItem.tvDriverName.text=item.driverName
                    bindingItem.tvDriverLicence.text=item.driverLicenseNumber
                    bindingItem.tvTruckNo.text=item.truckNumber
                    bindingItem.tvTransporter.text=item.transporterName
                    bindingItem.stoNo.text=item.deliveryNumber

                })

            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )
        }




    }catch (e:Exception){
        e.printStackTrace()
    }

    return printKeys
}


fun printForTransactionHistoryMTNR(intent: Intent, context: Context): ArrayList<String> {
    val historyList : ArrayList<VegaMtnrHistoryTransactions> = intent.getParcelableArrayListExtra(UIUtils.HISTORY_DATA_MTNR)!!
    val historyListNew = arrayListOf<VegaMtnrHistoryTransactions>()
    val printKeys = ArrayList<String>()

    historyList.forEachIndexed { index, it ->
        val newItem = it.copy()
        newItem.itemIndex = index+1
        historyListNew.add(newItem)
    }

    try {

        val view =
            LayoutInflater.from(context).inflate(R.layout.vega_trans_his_mtnr, null)
        val viewBinder = VegaTransHisMtnrBinding.bind(view)
        //viewBinder.trHeader.weightSum=11f

        val noOfBlock = historyListNew.size.div(20)
        val noOfBlockBalnace = historyListNew.size % 20

        for (item: Int in 0 until noOfBlock) {
            val subItem = historyListNew.subList(item * 20, (item + 1) * 20).toMutableList()

            viewBinder.rvListMtnr.setUpAdapter(subItem.toMutableList(),
                R.layout.item_txn_his_mtnr,
                ItemTxnHisMtnrBinding::inflate,
                { item, pos, bindingItem ->
                  //  bindingItem.trSubHeader.weightSum=11f
                    bindingItem.tvSNo.text = item.itemIndex.toString()
                    bindingItem.tvDeliveryNo.text=item.deliveryNumber
                    bindingItem.tvWeighbridgeId.text=item.wbId
                    bindingItem.tvDate.text=DateUtils.getFormatedDate(item.postingDate.toString())
                    bindingItem.tvQnty.text=item.quantity
                    bindingItem.tvUom.text=item.uom
                    bindingItem.tvDriverName.text=item.driverName
                    bindingItem.tvTruckNo.text=item.truckNumber
                    bindingItem.tvTransporter.text=item.transporterName
                    bindingItem.stoNo.text=item.stoNumber
                    //bindingItem.tvTransitLoss.text=item.transitLoss

                })

            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )


        }

        if(noOfBlock==0 || noOfBlockBalnace >0) {
            val historyListRemain = historyListNew.takeLast(noOfBlockBalnace)

            viewBinder.rvListMtnr.setUpAdapter(historyListRemain.toMutableList(),
                R.layout.item_txn_his_mtnr,
                ItemTxnHisMtnrBinding::inflate,
                { item, pos, bindingItem ->
                    bindingItem.trSubHeader.weightSum=11f
                    bindingItem.tvSNo.text = item.itemIndex.toString()
                    bindingItem.tvDeliveryNo.text=item.deliveryNumber
                    bindingItem.tvWeighbridgeId.text=item.wbId
                    bindingItem.tvDate.text=DateUtils.getFormatedDate(item.postingDate.toString())
                    bindingItem.tvQnty.text=item.quantity
                    bindingItem.tvUom.text=item.uom
                    bindingItem.tvDriverName.text=item.driverName
                    bindingItem.tvTruckNo.text=item.truckNumber
                    bindingItem.tvTransporter.text=item.transporterName
                    bindingItem.stoNo.text=item.stoNumber
                    //bindingItem.tvTransitLoss.text=item.transitLoss

                })

            printKeys.add(
                bitmapToString(
                    getBitmapFromView(
                        view, Color.WHITE
                    )
                )
            )
        }


    }catch (e:Exception){
        e.printStackTrace()
    }

    return printKeys
}




