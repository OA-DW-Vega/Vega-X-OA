package com.olam.warehouse.master.vegaecuador.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import kotlinx.android.parcel.Parcelize

/**
 * Created by Keerthi Santhanam on 7/14/2020.
 */

@Entity(primaryKeys = ["wbTempId", "batchNumber"])
@Parcelize
data class VegaEcuadorDispatchLots(
    @ColumnInfo(index = true)
    var wbTempId: String = "",
    var batchNumber: String = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var plantId: String? = "",
    var plantName: String? = "",
    var storageLocationCode: String? = "",
    var netWeight: String? = "",
    var editedWeight: String? = "",
    var isAdded: Boolean? = false,
    var pairId: Int? = 0,
    var binBatch: String? = "",
    var isChecked: Boolean? = false,
    var processOrderNo: String? = "",
    var deliveryItem: String? = "",
    var xchpf: String? = "",
    var noOfBags: String? = "",
    var slPostion: Int? = 0,
    var isProgress: Boolean? = false,
    var remarks: String = "",
    var turnAroundTime: String = "",
    var startTime: String = "",
    var endTime: String = "",
    var purchaseDocNum: String = "",
    var purchaseDocDesc: String = "",
    var deliveryStatus: Boolean = false,
    var encodedImageContent: String? = "",
    var imageUploadMsg: String? = "",
    var unitsOfMeasure: String? = "",
    var recPlantId: String? = "",
    var recStorageLocationCode: String? = "",
    @Ignore
    var isLowerWeight: Boolean = true,
    var delivery: String? = "",
    var grossWeight: String? = "",
    var endLotFlag: Boolean? = false,
    @Ignore
    var postingDate: String? = "",
    @Ignore
    var bagList: List<VegaCocoaSweepingBagMaterial> = emptyList(),
) : Parcelable
