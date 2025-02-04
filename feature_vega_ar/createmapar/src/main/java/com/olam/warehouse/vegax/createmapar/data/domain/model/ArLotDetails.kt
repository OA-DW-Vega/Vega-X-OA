package com.olam.warehouse.vegax.createmapar.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.user.model.Plant
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 6/15/2021.
 */
@Parcelize
data class ArLotDetails(
    var lotId: String? = "",
    var id: String? = "",
    var date: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var createdAt: String? = "",
    var updatedAt: String? = "",
    var createdBy: String? = "",
    var updatedBy: String? = "",
    var plant: Plant = Plant(),
    var anchorIds: List<AnchorId> = emptyList(),
    var arReq: ArWarehouse = ArWarehouse()
) : Parcelable

@Parcelize
data class ArWarehouse(
    var storageLocationCode: String? = "",
    var anchorIds: List<AnchorId> = emptyList()
) : Parcelable

@Parcelize
data class AnchorId(
    var arValue: String? = "",
    var id: String? = "",
    var createdAt: String? = "",
    var createdBy: String? = "",
    var updatedAt: String? = "",
    var updatedBy: String? = ""
) : Parcelable