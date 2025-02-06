package com.olam.warehouse.odreceiving.data.domain.model

data class DODispatchDetailPost(val sapMaterialIds: List<String>, val fromDate: Long, val toDate: Long, val productId: String? = null)
