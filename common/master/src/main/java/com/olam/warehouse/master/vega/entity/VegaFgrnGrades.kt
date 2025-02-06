package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class VegaFgrnGrades(
    var materialCode: String? = "",
    var processOrderNo: String? = "",
    var storageLocationCode: String? = "",
    var materialName: String? = "",
    var meins: String? = "",
    var rsNum: String? = "",
    var rsPos: String? = "",
    var bwart: String? = "",//movementType
    var resource: String? = "",
    var phase: String? = "",
    var plant: String? = "",
    var deliveryItem: String? = "",
    var xchpf: String? = "",
    var isGradeChecked: Boolean? = true

) : Parcelable


