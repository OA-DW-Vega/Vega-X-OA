 package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created by SangiliPandian C on 12-02-2019.
 */
@Entity
@Parcelize
data class DispatchOT(
    @PrimaryKey
    var otNumber: String = "",
    var containerType: String? = "",
    var numberOfContainer: Int = 0,
    var numberOfBales: Int = 0,
    var grade: String? = "",
    @SerializedName("deliveryQty")
    var weight: Double? = 0.0,
    var netWeight: Double? = 0.0,
    var tareWeight: Double? = 0.0,
    var grossWeight: Double? = 0.0,
    var unitOfMeasure: String? = "",
    var cropYear: String? = "",
    var totalNumberOfBales: Int = 0,
    var otBaleNetWeight: Double? = 0.0,
    var otBaleGrossWeight: Double? = 0.0,
    @Embedded
    @Ignore
    var containers: List<Container>? = emptyList(),
    var otOverallNetWeight: Double = 0.0,
    var thresholdOTMinWeight: Double = 0.0,
    @Ignore
    var numberOfInventoryContainer: Int = 0,
    var direct: Boolean = false,
    var normal: Boolean = false,
    var splitOt: String? = "",
    var originalOt: String? = "",
    var originalNoOfCont: Int? = 0,
    var originalDelQty: String? = ""

) : Parcelable
