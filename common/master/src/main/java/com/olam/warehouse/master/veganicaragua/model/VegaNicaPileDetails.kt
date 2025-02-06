package com.olam.warehouse.master.veganicaragua.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VegaNicaPileDetails(
    var pileNumber:String?="",
    var ticketNumber:String?="",
    var receivingDate:String?="",
    var gradeDetails:String?="",
    var gradeDesc:String?="",
    var certificate:String?="",
    var netWeight:String?="",
    var unitsOfMeasure:String?="",
    var storageLocation:String?="",
    var bagCount:String?="",
    var materialName:String?="",
    var vendor:String?=""
): Parcelable
