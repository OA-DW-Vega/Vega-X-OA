package com.olam.warehouse.master.common.model

data class GrnCharDetails (
    val materialNumber: String? = "",
    val grade: String? = "",
    val characterstics: List<Characteristics?> = emptyList()
)

data class Characteristics (
    val materialNumber: String? = "",
    val materialName: String? = "",
    val plant: String? = "",
    val effectiveDate: String? = "",
    val createdDate: String? = "",
    val qualityParamName: String? = "",
    val grade: String? = "",
    val qualityParamDesc: String? = "",
    val charValue: String? = "",
    val numValue: String? = "",
    val toValue: String? = "",
    val erdate: String? = "",
    val aedate: String? = ""
)
