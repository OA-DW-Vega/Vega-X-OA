package com.olam.warehouse.odreceiving.utils

import android.os.Build
import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.master.dorigin.entity.DOReceivingLineItem
import com.olam.warehouse.vegax.App
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
const val RECEIVING_MTN = "receiving_mtn"
const val UOM = "KG"
const val POSNR = "000010"
const val WS02 = "WS02"
const val WS01 = "WS01"
const val MATERIAL_CODE = "000000"
const val RECEIVING_DATA = "receiving_intent_data"
const val RECEIVING_POST_DATA = "receiving_intent_post_data"
const val RECEIVING_OUTPUT_DATA = "receiving_work_data"
const val RECEIVE_OFFLINE = "receive_offline"
const val REQUEST_CODE = 1
const val TRANSACTION_LIST = "transaction_list"
const val DISPATCH_DETAILS = "dispatch_details"
const val BAG_DETAILS = "bag_details"
const val MISSING_BAG = "missing_bag"
const val ATTACH_NEW_QR = "attach_new_qr"

fun getLineItemFromReceiving(postData: MutableList<DOReceiving>): List<DOReceivingLineItem> {
    val items = mutableListOf<DOReceivingLineItem>()
    postData.forEach {
        val item = DOReceivingLineItem()
        item.tmpWbId = it.tmpWbId
        item.bagCount = it.bagCount
        item.bagType = it.bagType
        item.bagWeight = it.bagWeight
        item.palletCount = it.palletCount
        item.palletType = it.palletType
        item.palletWeight = it.palletWeight
        item.charg = it.charg
        item.grossWeight = it.grossWeight
        item.item = it.item
        item.materialCode = it.materialCode
        item.materialName = it.materialName
        item.netWeight = it.netWeight
        item.supplierCode = it.supplierCode
        item.posnr = it.posnr
        item.uom = it.uom
        item.plantId = it.plantId
        item.wsGate = it.wsGate
        item.wtype = it.wtype
        item.location = it.location
        item.tareWeight = it.tareWeight
        item.supplierName = it.supplierName
        item.isSynced = it.isSynced
        item.status = it.status
        item.syncStatusMsg = it.syncStatusMsg
        item.mtnCode = it.mtnCode
        item.txnId = it.txnId
        items.add(item)
    }
    return items
}

fun getTmpId() = "TMP_".plus(Random.nextLong().toString())
@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}
