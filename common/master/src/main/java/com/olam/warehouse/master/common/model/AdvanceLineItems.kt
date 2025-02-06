
package com.olam.warehouse.master.common.model

data class AdvanceLineItems(

    val companyCode :  String? = "",
    val vendor :  String? = "",
    val advanceLineItemDetails : List<AdvanceLineItemDetails>?=null
)
