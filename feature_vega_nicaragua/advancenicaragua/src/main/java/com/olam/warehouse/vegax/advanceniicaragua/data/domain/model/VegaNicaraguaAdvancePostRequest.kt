package com.olam.warehouse.vegax.advanceniicaragua.data.domain.model

import com.olam.warehouse.master.user.model.Plant

data class VegaNicaraguaAdvancePostRequest
    (
    var documentNumber:String?="",
    var totalAmount:String?="",
    var vendorName:String?="",
    var vendorNo:String?="",
    var plant: Plant? = null,
    var key:String?="",
    var advanceFlag: Boolean? = false

)
