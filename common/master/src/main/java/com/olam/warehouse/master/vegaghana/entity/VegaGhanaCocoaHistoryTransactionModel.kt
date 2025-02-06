package com.olam.warehouse.master.vegaghana.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.parcelize.Parcelize

data class VegaGhanaCocoaHistoryTranxMtnr(
    var grnTxnDetails: List<VegaGhanaCocoaGRNHistoryTransactions> = emptyList(),
    var mtnrTxnDetails: List<VegaGhanaCocoaMtnrHistoryTransactions> = emptyList()
)

@Parcelize
@Entity(primaryKeys = ["id"])
data class VegaGhanaCocoaMtnrHistoryTransactions(
    @ColumnInfo(index = true)
    var id: Int = 0,
    var username: String? ="",
    var plantId: String? = "",
    var storageLocCode: String? = "",
    var deliveryNumber: String? = "",
    var deliveryType: String? = "",
    var wbId: String? = "",
    var postingDate: String? = "",
    var quantity: String? = "",
    var uom: String? = "",
    var wayBillNumber: String? = "",
    var materialCode: String? = "",
    var evacuationCertificate: String? = "",
    var driverName: String? = "",
    var driverContactNumber: String? = "",
    var driverLicenseNumber: String? = "",
    var truckNumber: String? = "",
    var transporterName: String? = "",
    var sendingLocation: String? = "",
    var destinationLocation: String? = "",
    var grnNumber: String? = "",
    var transactionStatus: String? = "",

    ) : Parcelable

data class VegaGhanaCocoaGRNHistoryTranxResponse(
    var grnTxnDetails: List<VegaGhanaCocoaGRNHistoryTransactions> = emptyList()
)

@Parcelize
@Entity(primaryKeys = ["id"])
data class VegaGhanaCocoaGRNHistoryTransactions(
    @ColumnInfo(index = true)
    var id: Int = 0,
    var username: String? ="",
    var plantId: String? = "",
    var storageLocationCode: String? = "",
    var wbId: String? = "",
    var grnNumber: String? = "",
    var postingDate: String? = "",
    var whReceiptNumber: String? = "",
    var quantity: String? = "",
    var uom: String? = "",
    var vendorCode: String? = "",
    @Ignore
    var vendorName: String? = ""

) : Parcelable

data class VegaGhanaCocoaMtntHistoryTranxResponse(
    var mtnTxnDetails: List<VegaGhanaCocoaMtntHistoryTransactions> = emptyList()
)

@Parcelize
@Entity(primaryKeys = ["id"])
data class VegaGhanaCocoaMtntHistoryTransactions(
    @ColumnInfo(index = true)
    var id: Int = 0,
    var username: String? ="",
    var plantId: String? = "",
    var storageLocCode: String? = "",
    var deliveryNumber: String? = "",
    var deliveryType: String? = "",
    var wbId: String? = "",
    var postingDate: String? = "",
    var quantity: String? = "",
    var uom: String? = "",
    var wayBillNumber: String? = "",
    var materialCode: String? = "",
    var evacuationCertificate: String? = "",
    var driverName: String? = "",
    var driverContactNumber: String? = "",
    var driverLicenseNumber: String? = "",
    var truckNumber: String? = "",
    var transporterName: String? = "",
    var destinationLocation: String? = "",
    var grnNumber: String? = "",
    var sendingLocation: String? = "",
    var transactionStatus: String? = "",
    @Ignore
    var vendorName: String? = ""

) : Parcelable
