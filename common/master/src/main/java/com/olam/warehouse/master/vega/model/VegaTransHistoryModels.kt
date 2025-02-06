package com.olam.warehouse.master.vega.model

/**
 * Created by Baskaran Kannan on 10/8/2022.
 */


import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.olam.warehouse.master.user.model.CountryDetail
import kotlinx.parcelize.Parcelize

data class VegaHistoryTranxMtnr(
    var grnTxnDetails: List<VegaGRNHistoryTransactions> = emptyList(),
    var mtnrTxnDetails: List<VegaMtnrHistoryTransactions> = emptyList()
)

@Parcelize
@Entity(primaryKeys = ["id"])
data class VegaMtnrHistoryTransactions(
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
    @Ignore
    var itemIndex: Int? = 0

    ) : Parcelable

data class VegaGRNHistoryTranxResponse(
    var grnTxnDetails: List<VegaGRNHistoryTransactions> = emptyList()
)

@Parcelize
@Entity(primaryKeys = ["id"])
data class VegaGRNHistoryTransactions(
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
    var vendorName: String? = "",
    @Ignore
    var supplierName: String? = "",
    @Ignore
    var itemIndex: Int? = 0

) : Parcelable

data class VegaMtntHistoryTranxResponse(
    var mtnTxnDetails: List<VegaMtntHistoryTransactions> = emptyList()
)

@Parcelize
@Entity(primaryKeys = ["id"])
data class VegaMtntHistoryTransactions(
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
    var vendorName: String? = "",
    @Ignore
    var itemIndex: Int? = 0

) : Parcelable
///==============================================

data class VegaFGRNHistoryTranxResponse(
    var fgrnTxn: List<VegaFGRNHistoryTransactions> = emptyList(),
    var rminTxn: List<VegaRMINHistoryTransactions> = emptyList()

)

@Parcelize
data class VegaFGRNHistoryTransactions(

    var date: String? ="",
    var processingType: String? = "",
    var quantity: String? = "",
    var unitsOfMeasure: String? = "",
    var dryingLoss: String? = "",
    var poNumber: String? = "",
    var materialDetail: MaterialDetail? = null,
    var storageLocation: StorageLocation? = null,
    @Ignore
    var itemIndex: Int? = 0

    ) : Parcelable


@Parcelize
data class MaterialDetail(
    var  id: String?="",
    var materialName: String?="",
    var materialCode: String?="",
    var countryDetail:CountryDetail?=null
):Parcelable

data class VegaRMINHistoryTranxResponse(
    var rminTxn: List<VegaRMINHistoryTransactions> = emptyList(),
    var fgrnTxn: List<VegaFGRNHistoryTransactions> = emptyList()
)

@Parcelize
data class VegaRMINHistoryTransactions(
    var date: String? ="",
    var processingType: String? = "",
    var quantity: String? = "",
    var dryingLoss: String? = "",
    var unitsOfMeasure: String? = "",
    var poNumber: String? = "",
    var materialDetail: MaterialDetail? = null,
    var storageLocation: StorageLocation? = null,
    @Ignore
    var itemIndex: Int? = 0
) : Parcelable

@Parcelize
data class StorageLocation(
    var plant: String? = "",
    var createdBy: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = ""
): Parcelable
