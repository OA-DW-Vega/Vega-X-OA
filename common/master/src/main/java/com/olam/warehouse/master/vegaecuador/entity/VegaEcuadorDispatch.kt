package com.olam.warehouse.master.vegaecuador.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.android.parcel.Parcelize

/**
 * Created by Keerthi Santhanam on 7/16/2020.
 */
@Entity(primaryKeys = ["wbTempId", "purchaseDocNum"])
@Parcelize
data class VegaEcuadorDispatch(
    @ColumnInfo(index = true)
    var wbTempId: String = "",
    var batchPicking: String? = "",
    var item: String? = "",
    var plantId: String? = PreferenceHelper.get(Constants.WERKS, ""),
    var batchNumber: String? = "",
    var materialCode: String? = "",
    var deliveryItem: String = "",
    var deliveryId: String = "",
    var purchaseDocNum: String = "",
    var purchaseDocDesc: String? = "",
    var customerNum: String? = "",
    var materialName: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var isSyncStatus: Boolean = false,
    var isErrorStatus: Boolean = true,
    var isOfflineData: Boolean = false,
    var isNotWBID: Boolean = false,
    var isSynced: Boolean = false,
    var status: Status = Status.SYNC_PENDING,
    var syncStatusMsg: String = "",
    var message: String? = "",
    var netWeight: String? = "",
    var approximateWeight: String? = "",
    var erdat: String? = "",
    var ertim: String? = "",
    var qcStatus: String? = "",
    var bagWeight: String? = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var grossWeight: String? = "",
    var unitsOfMeasure: String? = "",
    var storageLocationCode: String? = "",
    var recStorageLocationCode: String? = "",
    var recPlantId: String? = "",
    var deliveryUOM: String? = "",            //Vrkme
    var stockQty: String? = "",               //Lgmng
    var deliveryQty: String? = "",            //Lfimg
    var stockUOM: String? = "",                //Meins
    var remarks: String = "",
    var turnAroundTime: String = "",
    var startTime: String = "",
    var endTime: String = "",
    var isStarted: Boolean = false,
    var isEnded: Boolean = false,
    @Ignore
    var isProgress: Boolean = false,
    var plantName: String = "",
    var deliveryStatus: Boolean = false,
    @Ignore
    var lotList: ArrayList<VegaEcuadorDispatchLots> = ArrayList(),
    @Ignore
    var materialList: ArrayList<String>? = ArrayList(),
    @Ignore
    var purchaseOrders: ArrayList<VegaEcuadorDispatchPurchaseOrders> = ArrayList(),
    var binFormation: Boolean = false,
    var delivery: Boolean = false,
    var pgi: Boolean = false,
    var picking: Boolean = false,
    @Ignore
    var isView: Boolean? = false,
    var deliveryDetail: String? = ""
) : Parcelable
