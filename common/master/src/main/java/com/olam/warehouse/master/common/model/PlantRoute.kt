package com.olam.warehouse.master.common.model

data class PlanRoute(
    val id: Int? = 0,
    val sourceLocCode: String = "",
    val routeLocCode: String? = "",
    val routeLocationName: String? = "",
    val departureLocName: String? = "",
    val departureLocCode: String? = ""
)
