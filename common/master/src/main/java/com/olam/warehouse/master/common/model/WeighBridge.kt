package com.olam.warehouse.master.common.model

import android.os.Parcelable
import com.olam.warehouse.presentation.enums.Status
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
data class WeighBridge(
    var weighBridgeId: String = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var bagWeight: String? = "",
    var batchNumber: String? = "",
    var direction: String? = "",
    var purchaseDocNum: String? = "",
    var purchaseDocDesc: String? = "",
    var erdat: String? = "",
    var ertim: String? = "",
    var grossWeight: String? = "",
    var item: String? = "",
    var customerNum: String? = "",
    var materialName: String? = "",
    var materialCode: String? = "",
    var delivery: String? = "",
    var netWeight: String? = "",
    var deliveryItem: String? = "",
    var challan: String? = "", // DO txn id
    var qcStatus: String? = "",
    var receivedWeight: String? = "",
    var sentWeight: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var unitsOfMeasure: String? = "",
    var plant: String? = "",
    var weighBridgeType: String? = "",
    var lgort: String? = "",
    var lotDetails: String? = "",
    var grnNumber: String? = "",
    var isSynced: Boolean = false,
    var isProcureCompleted: Boolean = false,
    var bsart: String? = "",
    var loc: String? = "",
    var status: Status = Status.SYNC_PENDING,
    var syncStatusMsg: String? = "",
    var location: String? = "",
    var unitPrice: String? = "",
    var wbTempId: String = "",
    var matUOM: String? = "",
    var currency: String? = "",
    var storageLocationCode: String? = "",
    var transactionBagDetails: MutableList<TransactionDetailWB>? = mutableListOf()
)

@Parcelize
data class TransactionDetailWB(
    var noOfBags: Int? = 0,
    var transactionId: String = "",
    var bagList: MutableList<BagWB> = mutableListOf()
) : Parcelable

@Parcelize
data class BagWB(
    var bagQrCode: String? = "",
    var weight: String? = "",
    var bagMissed: Boolean = false,
    var invalidQrCode: Boolean = false
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BagWB

        if (bagQrCode != other.bagQrCode) return false

        return true
    }

    override fun hashCode(): Int {
        return bagQrCode?.hashCode() ?: 0
    }
}
