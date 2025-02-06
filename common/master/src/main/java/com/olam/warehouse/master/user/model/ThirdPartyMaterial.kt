package com.olam.warehouse.master.user.model

import androidx.room.Entity
import androidx.room.PrimaryKey


data class ThirdPartyMaterialItem(
    @PrimaryKey
    val id: Int,
    val materialDetail: VegaCoffeeThirdPartyMaterialDetail,
    val productType: String,
    val typeCode: String
)

@Entity
data class VegaCoffeeThirdPartyMaterialDetail(
    val currency: String,
    @PrimaryKey
    val id: Int? = 0,
    var mainId: Int? = 0,
    var productType: String = "",
    var typeCode: String? = "",
    var isBltEnabled: Boolean? = false,
    var languageCode: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var plant: String? = "",
    var price: String? = "",
    var scanLevelId: String? = "",
    var scanLevelName: String? = "",
    var unitsOfMeasure: String? = ""
)

@Entity
data class VegaCoCoaThirdPartyMaterialDetail(
    val currency: String,
    @PrimaryKey
    val id: Int? = 0,
    var mainId: Int? = 0,
    var productType: String = "",
    var typeCode: String? = "",
    var isBltEnabled: Boolean? = false,
    var languageCode: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var plant: String? = "",
    var price: String? = "",
    var scanLevelId: String? = "",
    var scanLevelName: String? = "",
    var unitsOfMeasure: String? = ""
)
