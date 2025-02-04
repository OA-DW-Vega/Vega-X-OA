package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity

import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Entity(indices = [Index(value = ["mtnNumber"], unique = true)])
@Parcelize
data class PortMtn(
    @PrimaryKey
    @SerializedName("deliveryNo")
    var mtnNumber: String = "",
    var suplierPlantId: String = "",
    var suplierPlantDesc: String = "",
    var recievedPlantId: String = "",
    var recievedPlantDesc: String = "",
    var lineItem: String = "",
    var containerNo: String = "",
    var grade: String = "",
    var uom: String = "",
    var sourceNetWeight: String = "",
    var baleCount: String = "",
    var truckNumber: String = "",
    var classificationInProgress: Int = 0,
    @Ignore
    var istogrnPost: String = "",
    @Ignore
    var itransferPost: String = "",
    @Ignore
    var isChecked: Boolean = false,
    var isOfflineData: Boolean = false,
    var status: Int? = 1,
    var isSyncStatus: Boolean = false,
    var isErrorStatus: Boolean = true,
    var message: String? = "",
    @Embedded
    @Ignore
    var mtnBales: List<PortMtnBales>? = emptyList(),
    @Embedded
    @Ignore
    var grades: List<PortMtnGrades>? = emptyList()
) : Parcelable
