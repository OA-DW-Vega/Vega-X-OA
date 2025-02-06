package com.olam.warehouse.vegax.processingnigeria.data.domain.model

data class VegaNigeriaCustomerModel(
    var CUSTOMER: List<CUSTOMER> = emptyList()
)

data class CUSTOMER(
    var key:String? = "",
    var value:String? = ""

)
