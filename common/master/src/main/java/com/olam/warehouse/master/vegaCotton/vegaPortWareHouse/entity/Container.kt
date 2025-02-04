package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity

import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by SangiliPandian C on 12-02-2019.
 */
@Entity(indices = [Index(value = ["containerNumber", "otNumber"], unique = true)])
@Parcelize
data class Container(
    @PrimaryKey
    var containerNumber: String = "",
    var containerType: String? = "",
    @SerializedName("otId")
    var otNumber: String = "",
    var sealNumber: String? = "",
    @SerializedName("containerStatusId")
    var status: Int? = 0,
    @SerializedName("deliveryQty")
    var weight: Double? = 0.0,
    var grade: String? = "",
    var unitOfMeasure: String? = "",
    var numberOfBales: Int? = 0,
    var baleCount: Int? = 0,
    var sumOfBaleWeights: String? = "",
    @Embedded
    @Ignore
    var bales: List<PortBale>? = emptyList(),
    var isSelected: Boolean? = false

) : Parcelable
