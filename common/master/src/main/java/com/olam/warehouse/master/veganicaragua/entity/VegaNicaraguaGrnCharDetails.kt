package com.olam.warehouse.master.veganicaragua.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

@Entity(primaryKeys = ["materialNumber", "grade", "qualityParamName"])
@Parcelize
data class VegaNicaraguaGrnCharDetails (
    var materialNumber: String = "",
    var materialName: String? = "",
    var plant: String? = "",
    var qualityParamName: String = "",
    var grade: String = "",
    var qualityParamDesc: String? = "",
    var effectiveDate: String? = "",
    var erdate: String? = "",
    var charValue: String? = "",
    var numValue: String? = "",
    var toValue: String? = ""
): Parcelable

