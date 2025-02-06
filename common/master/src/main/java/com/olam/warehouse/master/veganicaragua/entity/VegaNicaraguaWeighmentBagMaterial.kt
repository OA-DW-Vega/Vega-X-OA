package com.olam.warehouse.master.veganicaragua.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

/**
 * Created by Keerthi Santhanam on 9/04/2020.
 */

@Entity(primaryKeys = ["tmpWbId", "id"])
@Parcelize
data class VegaNicaraguaWeighmentBagMaterial(

    var id: Int = 0,
    var tmpWbId: String = "",
    var materialCode: String = "",
    var supplierCode: String = "",
    var procureType: String = "",
    var purcheseOrderNo: String = "",
    var batchNumber: String = "",
    var grossWeight: String = "0",
    var netWeight: String = "",
    var bagType: String = "",
    var bagCount: String = "0",
    var tareWeight: String? = "0",
    var unitsOfMeasure: String? = "",
    var palletWeight: String? = "",
    var noOfPallet: String? = "",
    var palletAverage: String? = "0",
    var status: Int? = 1,
    var message: String? = "",
    var isSyncStatus: Boolean? = false,
    var approximateWeight : String?="",
    var bagTareWeight : String?="",
    var bagWeight : String?="",
    var contactNumber : String?="",
    var delivery : String?="",
    var deliveryItem : String?="",
    var driverName : String?="",
    var driverNumber : String?="",
    var erdat : String?="",
    var imageString: String?="",
    var item : String?="",
    var materialName : String?="",
    var mtnCode : String?="",
    var palletCount : String?="",
    var palletType : String?="",
    var plantId : String?="",
    var posnr : String?="",
    var purchaseDocDesc : String?="",
    var purchaseDocNum : String?="",
    var storageLocationCode : String?="",
    var supplierName : String?="",
    var supplierZone : String?="",
    var systemDate : String?="",
    var systemTime : String?="",
    var transportVendorCode : String?="",
    var truckDirection : String?="",
    var truckType : String?="",
    var txnId : String?="",
    var userName : String?="",
    var vehicleNumber : String?="",
    var vehicleType : String?="",
    var weighBridgeId : String?="",
    var weighBridgeType : String?="",
    var wsGate : String?="",
    var wsType : String?=""
) : Parcelable
