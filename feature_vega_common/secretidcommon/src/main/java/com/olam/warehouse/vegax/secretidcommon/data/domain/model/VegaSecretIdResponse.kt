package com.olam.warehouse.vegax.secretidcommon.data.domain.model

data class VegaSecretIdResponse(
    var batchNumber: String = "",
    var message: String = "",
    var secretKey: String = "",
    var isRetriveFlag: Boolean = false,
    var isSecretIdExists: Boolean = false,
)
