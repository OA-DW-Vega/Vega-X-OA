package com.olam.warehouse.master.common.model

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
data class PackageMaterial(
    val bagType: String = "",
    val weightType: String = "",
    val bagMaterialCode: String? = "",
    val standardWeight: String? = "",
    val tareWeight: String? = "",
    val unitsOfMeasure: String? = "",
    val tolerance: String? = "",
    val plant: String? = "",
    var isDefault: Boolean? = false
)
