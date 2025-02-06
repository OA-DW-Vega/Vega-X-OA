package com.olam.warehouse.master.vegaghana.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.olam.warehouse.master.user.model.CountryDetail
import kotlinx.parcelize.Parcelize

data class VegaGhanaCashewHistoryTranxMtnr(
    var grnTxnDetails: List<VegaGhanaCashewGRNHistoryTransactions> = emptyList(),
    var mtnrTxnDetails: List<VegaGhanaCashewMtnrHistoryTransactions> = emptyList()
)

@Parcelize
@Entity(primaryKeys = ["id"])
data class VegaGhanaCashewMtnrHistoryTransactions(
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
    var stoNumber: String? = "",
    var transitLoss: String? = "",
    var transactionStatus: String? = "",

    ) : Parcelable

data class VegaGhanaCashewGRNHistoryTranxResponse(
    var grnTxnDetails: List<VegaGhanaCashewGRNHistoryTransactions> = emptyList()
)

@Parcelize
@Entity(primaryKeys = ["id"])
data class VegaGhanaCashewGRNHistoryTransactions(
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
    var totalPrice: String? = "",
    var pricePerUnit: String? = "",
    var batchNumber: String? = "",
    var materialCode: String? = "",
    @Ignore
    var vendorName: String? = ""

) : Parcelable

data class VegaGhanaCashewMtntHistoryTranxResponse(
    var mtnTxnDetails: List<VegaGhanaCashewMtntHistoryTransactions> = emptyList()
)

@Parcelize
@Entity(primaryKeys = ["id"])
data class VegaGhanaCashewMtntHistoryTransactions(
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
    var stoNumber: String? = "",
    @Ignore
    var vendorName: String? = ""

) : Parcelable
///==============================================

data class VegaGhanaCashewFGRNHistoryTranxResponse(
    var fgrnTxn: List<VegaGhanaCashewFGRNHistoryTransactions> = emptyList(),
    var rminTxn: List<VegaGhanaCashewRMINHistoryTransactions> = emptyList()

)

@Parcelize
data class VegaGhanaCashewFGRNHistoryTransactions(

    var date: String? ="",
    var processingType: String? = "",
    var quantity: String? = "",
    var unitsOfMeasure: String? = "",
    var dryingLoss: String? = "",
    var poNumber: String? = "",
    var materialDetail: MaterialDetail? = null,

) : Parcelable


@Parcelize
data class MaterialDetail(
   var  id: String?="",
    var materialName: String?="",
    var materialCode: String?="",
    var countryDetail:CountryDetail?=null
):Parcelable

data class VegaGhanaCashewRMINHistoryTranxResponse(
    var rminTxn: List<VegaGhanaCashewRMINHistoryTransactions> = emptyList(),
    var fgrnTxn: List<VegaGhanaCashewFGRNHistoryTransactions> = emptyList()
    )

@Parcelize
data class VegaGhanaCashewRMINHistoryTransactions(
    var date: String? ="",
    var processingType: String? = "",
    var quantity: String? = "",
    var dryingLoss: String? = "",
    var unitsOfMeasure: String? = "",
    var poNumber: String? = "",
    var materialDetail: MaterialDetail? = null
    ) : Parcelable
