package com.olam.warehouse.master.vegacocoa.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import com.olam.warehouse.master.vega.entity.TrackTraceFarmerModel
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 9/10/2020.
 */


@Entity(primaryKeys = ["weighBridgeId", "delivery"])
@Parcelize
data class VegaCoCoaReceiving(
    var grnNumber: String? = "",
    var weighBridgeId: String = "",
    var delivery: String = "",
    var mtnNumber: String? = "",
    var batchNumber: String? = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var bagWeight: String? = "0",
    var charg: String = "",
    var grossWeight: String? = "0",
    var truckOutWeight: String? = "0",
    var item: String? = "",
    var deliveryItem: String? = "",
    var purchaseDocNum: String? = "",
    var purchaseDocDesc: String? = "",
    var purchaseDocQty: String? = "",
    var customerNum: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var netWeight: String = "0",
    var supplierCode: String? = "",
    var posnr: String? = "",
    var unitsOfMeasure: String = "",
    var plantId: String? = PreferenceHelper.get(Constants.WERKS, ""),
    var wsGate: String = "",
    var weighBridgeType: String = "",
    var location: String? = "",
    var tareWeight: String? = "0",
    var supplierName: String? = "",
    var isSynced: Boolean = false,
    var isOfflineData: Boolean = false,
    var isOnlineData: Boolean? = true,
    var status: Status = Status.SYNC_PENDING,
    var syncStatusMsg: String? = "",
    var mtnCode: String? = "",
    var txnId: String? = "",
    var truckDirection: String? = "",
    var imagePath: String? = "",
    var imageString: String? = "",
    var vehicleNumber: String? = "",
    var vehicleType: String? = "",
    var contactNumber: String? = "",
    var driverName: String? = "",
    var truckDriverName: String? = "",
    var qcStatus: String? = "",
    var erdat: String? = "",
    var ertim: String? = "",
    var direction: String? = "",
    var transportVendorCode: String? = "",
    var transportVendorName: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var bagTareWeight: String? = "0",
    var dstorageLocationCode: String? = "",
    var dstorageLocationName: String? = "",
    var batchWeight: String? = "",
    var encodedImageContent: String? = "",
    var startTime: String? = "",
    var endTime: String? = "",
    var remarks: String? = "",
    var turnAroundTime: String? = "",
    var wayBillNo: String? = "",
    var endLot: Boolean? = false,
    var sourceLotId: String = "",
    var farmerLessTransactionId:String? = "",
    var eudrStatus: Boolean = false,
    @Ignore
    var ttFarmerList: List<TrackTraceFarmerModel> = emptyList(),
    @Ignore
    var isProgress: Boolean = false,
    var tempGrnNumber: String? = "",
    var thirdPartyVendorName: String? = "",
    var transitLossDocNo: String? = "",
    @Ignore
    var weighMethod: String? ="",
    @Ignore
    var operatorName: String? ="",
    @Ignore
    var bagList: List<VegaCoCoaOffloadingBagMaterial> = emptyList(),
    @Ignore
    var isDelete: Boolean= false
) : Parcelable

@Entity(primaryKeys = ["bagType", "batchNumber", "mtnNumber"])
@Parcelize
data class VegaCoCoaOffloadingBagMaterial(
    var id: Int = 0,
    var bagCount: String = "0",
    var bagMaterialCode: String = "",
    var bagType: String = "",
    var grossWeight: String = "0",
    var truckOutWeight: String = "0",
    var netWeight: String = "0",
    var tareWeight: String? = "0",
    var unitsOfMeasure: String? = "",
    var batchNumber: String = "",
    var mtnNumber: String = "",

    var status: Int? = 1,
    var message: String? = "",
    var isSyncStatus: Boolean? = false,
    var createdPosition: Int? = 0
) : Parcelable

@Entity(primaryKeys = ["plant", "storageLocationCode"])
data class VegaCoCoaStorageLocation(
    var plant: String = "",
    var storageLocationCode: String = "",
    var storageLocationName: String? = ""
)

@Parcelize
data class BagTypeList(
    var bagType:  String = "",
    var bagCount: String = "0",
    var tareWeight: String? = "",
) :Parcelable
