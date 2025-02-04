package com.olam.warehouse.odreceiving.data.domain.model

data class DispatchDetailsResponse(
//    var dispatchDetail: MutableList<DispatchDetail> = mutableListOf()
//    var dispatchDetail: DispatchDetail? = null
    var totalDispatched: Int = 0,
    var totalReceived: Int = 0,
    var totalQCComplete: Int = 0,
    var sapMaterialId: String = "",
    var productId: String = "",
    var originId: String = "",
    var totalGrnPosted: Int = 0
)

/*data class DispatchDetail(
    var totalDispatched: Int = 0,
    var totalReceived: Int = 0,
    var totalQCComplete: Int = 0,
    var sapMaterialId: String = "",
    var productId: String = "",
    var originId: String = "",
    var totalGrnPosted: Int = 0
)*/
