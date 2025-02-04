package com.olam.warehouse.presentation.data.domain.model

/**
 * Created by SangiliPandian C on 17-11-2019.
 */

data class GenericReqAndResp<T>(val data: T, val message: String, val success: Boolean, val errors: String? = "")
