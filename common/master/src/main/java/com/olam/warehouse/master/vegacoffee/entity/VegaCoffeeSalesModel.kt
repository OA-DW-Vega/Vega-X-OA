package com.olam.warehouse.master.vegacoffee.entity

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Relation
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.parcelize.Parcelize

@Entity(primaryKeys = ["salesTempId", "saleOrderId"])
@Parcelize
data class VegaCoffeeSalesOrder(
    var salesTempId: String = "",
    var saleOrderId: String = "",
    var salesType: String = "",
    var salesOrderDesc: String = "",
    var customerId: String? = "",
    var customerName: String? = "",
    var soWeight: String? = "",
    var salesItem: String? = "",
    var createdDate: String? = "",
    var startTime: String? = "",
    var endTime: String? = "",
    var turnAroundTime: String = "",
    var remarks: String? = "",
    var operatorName: String? = "",
    var plantId: String? = PreferenceHelper.get(Constants.WERKS, ""),
    @Ignore
    var isProgress: Boolean = false,
    var plantName: String = "",
    @Ignore
    var lotList: ArrayList<VegaCoffeeSalesLots> = ArrayList(),
    @Ignore
    var materialCode: String = "",
    @Ignore
    var materialName: String = "",
    @Ignore
    var truckNo: String = ""
) : Parcelable


@Entity(primaryKeys = ["batchNumber", "saleOrderId", "salesTempId", "materialCode"])
@Parcelize
data class VegaCoffeeSalesLots(
    var salesTempId: String = "",
    var batchNumber: String = "",
    var materialCode: String = "",
    var materialName: String? = "",
    var grade: String? = "", //required for nicaragua coffee lot card printing
    var certificate: String? = "", //required for nicaragua coffee lot card printing
    var plantId: String? = "",
    var plantName: String? = "",
    var storageLocationCode: String? = "",
    var unitOfMeasure: String? = "",
    var weight: String? = "",
    var editedWeight: String? = "0.0",
    var isAdded: Boolean? = false,
    var isChecked: Boolean? = false,
    var endLotFlag: Boolean? = false,
    var processOrderNo: String? = "",
    var meins: String = "",
    var rsNum: String? = "",
    var rsPos: String? = "",
    var bwart: String? = "",
    var phase: String? = "",
    var deliveryItem: String? = "",
    var xchpf: String? = "",
    var noOfBags: String? = "",
    var slPostion: Int? = 0,
    var isProgress: Boolean? = false,
    @Ignore
    var isLowerWeight: Boolean = true,
    var saleOrderId: String = "",
    var salesType: String? = "",
    var weightToDispatchUOM: String? = "",
    var deliveryFlag: Boolean? = false,
    var pickingFlag: Boolean? = false,
    var pgiFlag: Boolean? = false,
    var storageLossFlag: Boolean? = false,
    var delivery: String? = "",
    var weighBridgeId: String? = "",
    var mergedLotId: String? = "",
    var receivingStorageLocation: String? = "",
    @Ignore
    var bagList: ArrayList<VegaCoffeeSalesBagMaterial> = ArrayList(),
    var palletWeight: String? = "",
    var palletCount: String? = "",
    var palletAvg:String?="0"
) : Parcelable

@Entity(primaryKeys = ["id", "salesTempId", "batchNumber"])
@Parcelize
data class VegaCoffeeSalesBagMaterial(
    var id: Int = 0,
    var salesTempId: String = "",
    var batchNumber: String = "",
    var materialCode: String = "",
    var materialName: String = "",
    var grossWeight: String = "0",
    var netWeight: String = "0",
    var bagType: String = "",
    var bagCount: String = "",
    var tareWeight: String? = "0",
    var bagMaterialCode: String = "",
    var unitsOfMeasure: String? = "",
    var palletWeight: String? = "",
    var noOfPallet: String? = "0",
    var palletAverage: String? = "0",
    var status: Int? = 1,
    var message: String? = "",
    var createdPosition: Int? = 0,
    var isPalletDetails: Boolean? = false,
    var isSyncStatus: Boolean? = false
) : Parcelable


class VegaCameroonSalesWithBagItems{

    @Embedded
    lateinit var salesLots: VegaCoffeeSalesLots

    @Relation(
        parentColumn = "batchNumber",
        entityColumn = "batchNumber",
        entity = VegaCoffeeSalesBagMaterial::class
    )
    var bagMaterialList: List<VegaCoffeeSalesBagMaterial> = emptyList()
}
