package com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity(
    foreignKeys = [ForeignKey(
        entity = Mtn::class,
        parentColumns = arrayOf("mtnNumber"),
        childColumns = arrayOf("mtnNumber"),
        onDelete = ForeignKey.CASCADE
    )]
)
@Parcelize
data class MtnBales(
    @PrimaryKey
    var baleId: String = "",
    @SerializedName("deliveryNo")
    var mtnNumber: String = "",
    var lineItem: String = "",
    var grade: String = "",
    var grossWeight: String? = "",
    var fromSloc: String? = "",
    var toSloc: String? = "",
    var netWeight: String? = "",
    var userId: String? = "",
    var containerNumber: String? = "",
    var otNumber: String? = "",
    var delyNetQty: String? = "",
    var blineItem: String? = "",
    var unitOfMeasurement: String = "",
    var isVerified: Int = 0,
    var baleStatus: String? = "",
    var isOfflineData: Boolean = false,
    var dateTime:Date? = Date()
) : Parcelable
