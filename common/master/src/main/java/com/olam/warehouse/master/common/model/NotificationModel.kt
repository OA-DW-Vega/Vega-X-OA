package com.olam.warehouse.master.common.model

/**
 * Created by Baskaran Kannan on 1/25/2022.
 */
data class NotificationModel(
    var notification: String = "",
    var Date: String = "",
    var timeMillis: Long = 0,
    var isViewed: Boolean = false,
    var isReceived: Boolean = false,
    var isRead: Boolean = false,
    var isUnRead: Boolean = false
)
