package com.olam.warehouse.odreceiving.data.domain.model

import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.master.user.model.Plant

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
data class DOReceivingPost(
    val key: String,
    val plant: Plant,
    val missedQrCodes: MutableList<QrCodes>?,
    val replaceQrCodes: MutableList<ReplaceQrCodes>?,
    val weighDetails: List<DOReceiving>)

data class QrCodeMapping(
    var newQrCode: String? = "",
    var oldQrCode: String? = ""
)

data class ReplaceQrCodes(
    var qrCodeMapping: MutableList<QrCodeMapping>?,
    var transactionId: String
)

data class QrCodes(
    val qrCodes: MutableList<String>?,
    var transactionId: String
)

data class QRMap(
    var qrCodeMapping: QrCodeMapping,
    var replaceQrCode: ReplaceQrCodes,
    var transactionId: String = ""
)
