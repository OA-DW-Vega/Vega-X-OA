package com.olam.warehouse.master.dorigin.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class DOSapMaterialList(
    @PrimaryKey
    var tradingProductId:String="",
    var tradingProductName:String?="",
    var sapMaterialId:String?="",
)
