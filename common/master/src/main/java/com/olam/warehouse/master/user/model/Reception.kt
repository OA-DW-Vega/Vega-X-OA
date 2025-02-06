package com.olam.warehouse.master.user.model

/**
 * Created by Baskaran Kannan on 12/24/2019.
 */
data class Reception(
    var receptionName: String? = "",
    var products: List<String>? = emptyList(),
    var selectedProducts: ArrayList<String> = ArrayList<String>(),
    var product: String? = "",
    var isChecked: Boolean? = false
)
