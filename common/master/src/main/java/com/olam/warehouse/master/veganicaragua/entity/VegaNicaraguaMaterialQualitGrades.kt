package com.olam.warehouse.master.veganicaragua.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(primaryKeys = ["materialCode", "gradeCode"])
data class VegaNicaraguaMaterialQualitGrades(
    @ColumnInfo(index = true)
    var materialCode : String="",
    var materialName : String?="",
    var price : String?="",
    var unitsOfMeasure : String?="",
    var currency : String?="",
    var materialType : String?="",
    var grade : String?="",
    var gradeCode : String="",
    var bagType : String?="",
    var tareWeight : String?=""
) : Parcelable
