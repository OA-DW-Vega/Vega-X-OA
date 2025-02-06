package com.olam.warehouse.master.dorigin.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 12/19/2019.
 */
@Entity
@Parcelize
data class DOReceivingLineItem(
    @PrimaryKey
    var tmpWbId: String = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var bagWeight: Double? = 0.0,
    var palletCount: String? = "",
    var palletType: String? = "",
    var palletWeight: Double? = 0.0,
    var charg: String = "",
    var grossWeight: Double = 0.0,
    var item: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var netWeight: Double = 0.0,
    var supplierCode: String? = "",
    var posnr: String? = "",
    var uom: String = "",
    var plantId: String? = PreferenceHelper.get(Constants.WERKS, ""),
    var wsGate: String = "",
    var wtype: String = "",
    var location: String? = "",
    var tareWeight: String? = "",
    var supplierName: String? = "",
    var isSynced: Boolean = false,
    var status: Status = Status.SYNC_PENDING,
    var syncStatusMsg: String? = "",
    var mtnCode: String? = "",
    var txnId: String? = "",
    var currentKey: String? = getCurrentKey()
) : Parcelable {
    override fun toString(): String {
        return "DOReceivingLineItem(tmpWbId='$tmpWbId', bagCount=$bagCount, bagType=$bagType, bagWeight=$bagWeight, palletCount=$palletCount, palletType=$palletType, palletWeight=$palletWeight, charg='$charg', grossWeight=$grossWeight, item=$item, materialCode=$materialCode, materialName=$materialName, netWeight=$netWeight, supplierCode=$supplierCode, posnr=$posnr, uom='$uom', plantId=$plantId, wsGate='$wsGate', wtype='$wtype', location=$location, tareWeight=$tareWeight, supplierName=$supplierName, isSynced=$isSynced, status=$status, syncStatusMsg=$syncStatusMsg, mtnCode=$mtnCode, txnId=$txnId, currentKey=$currentKey)"
    }
}
