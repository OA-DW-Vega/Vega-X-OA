package com.olam.warehouse.master.veganicaragua.model

import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceDetails

data class VegaNicaraguaGetAdvanceDetails (
    val advanceCreationDetailsDTO : List<VegaNicaraguaAdvanceDetails>?=null,
    val companyCode : String?="",
    val vendor : String?="",
    val errorCode:String?="",
    val message :String?="",
    val success :String?=""
)

