package com.olam.warehouse.master.common.model


data class MaterialGradeResponse(
    val materialQualitGrades : List<MaterialQualitGrades>
)

data class MaterialQualitGrades(
    val materialDetails : MaterialDetails?=null,
    val qualityGrades : List<QualityGrades>?=null
)

data class QualityGrades (

    val id : Int?=0,
    val grade : String?="",
    val gradeCode : String?="",
    val createdAt : String?="",
    val createdBy : String?="",
    val updatedAt : String?="",
    val updatedBy : String?="",
    val bagType : String?="",
    val bagTareWeight : String?=""
)

data class MaterialDetails (

    val id : Int?=0,
    val materialCode : String?="",
    val materialName : String?="",
    val price : String?="",
    val unitsOfMeasure : String?="",
    val currency : String?="",
    val plant : String?="",
    val languageCode : String?="",
    val scanLevelId : String?="",
    val scanLevelName : String?="",
    val thirdPartyFlag : String?="",
    val thirdPartyMaterialCode : String?="",
    val materialType : String?="",
    val bltEnabled : String?=""
)
