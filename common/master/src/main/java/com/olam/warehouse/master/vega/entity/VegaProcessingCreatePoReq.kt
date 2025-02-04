package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.presentation.enums.Status
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(primaryKeys = ["cfgNo", "batchNumber"])
data class VegaProcessingCreatePoReq(
    var cfgNo: String = "",
    var processingStage: String? = "",
    var batchNumber: String = "",
    var key: String? = "",
    var outputMaterialCode: String? = "",
    var materialName: String? = "",
    var materialCode: String? = "",
    @Ignore
    var plant: Plant? = null,
    @Ignore
    var processingLotDtls: List<ProcessingLotDetails> = emptyList(),
    var rmin: Boolean? = false,
    var versionId: String? = "",
    var isSynced: Boolean = false,
    var status: Status = Status.SYNC_PENDING,
    var date: String? = "",
    var syncStatusMsg: String? = "",
    var isProgress: Boolean = false
) : Parcelable

@Parcelize
@Entity(primaryKeys = ["cfgNo", "batchNumber"])
data class ProcessingLotDetails(
    var cfgNo: String = "",
    var batchNumber: String = "",
    var materialCode: String? = "",
    var netWeight: String? = "",
    var plant: String? = "",
    var storageLocationCode: String? = "",
    var unitsOfMeasure: String? = ""
) : Parcelable



