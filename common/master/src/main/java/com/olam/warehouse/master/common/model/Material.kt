package com.olam.warehouse.master.common.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
data class Material(
    val materialCode: String = "",
    val materialName: String? = "",
    val materialType: String? = "",
    val price: String? = "",
    val currency: String? = "",
    val plant: String? = "",
    val languageCode: String? = "",
    val unitsOfMeasure: String? = "",
    val thirdPartyFlag: String? = "",
    val typeCode: String? = "",
    val thirdPartyMaterialCode: String? = "",
    val bltEnabled: Boolean? = false,
    val scanLevelId: Int? = -1,
    @Embedded
    @Ignore
    val uomDetails: List<VegaUomDetails>? = emptyList(),
    val scanLevelName: String? = ""
)

@Entity(primaryKeys = ["materialCode","fromUom"])
@Parcelize
data class VegaUomDetails(
    var materialCode:String = "",
    var fromUom:String = "",
    var toUom:String? = "",
    var value1:String? = "",
    var value2:String? = ""
): Parcelable
