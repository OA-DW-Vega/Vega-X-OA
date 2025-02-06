package com.olam.warehouse.master.common.model

/**
 * Created by Baskaran Kannan on 4/30/2020.
 */
data class MessageModel(
    var id: String? = "0",
    val title: String? = "",
    val message: String? = "",
    val flag: String? = "",
    val transactionId: String? = "",
    val navigationId: String? = ""
)
