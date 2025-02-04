
package com.olam.warehouse.master.veganicaragua.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class VegaNicaraguaPositionGradeMappings(
    @PrimaryKey
    var id : Int?=0,
    var companyCode : String?="",
    var codeGroup : String?="",
    var code : String?="",
    var description : String?="",
    var createdAt : String?="",
    var createdBy : String?="",
    var updatedAt : String?="",
    var updatedBy : String?=""
)

