package com.olam.warehouse.odquality.data.domain.model

import java.util.*

class DOQualityWeighBridgeBagDetail : ArrayList<WeighBridgeBagDetail>()

data class WeighBridgeBagDetail(
    val bagCount: String = "",
    val bagType: String = "",
    val bagWeight: String = "",
    val challan: String = "",
    val charg: String = "",
    val direction: String = "",
    val ebeln: String = "",
    val ebelp: String = "",
    val erdat: String = "",
    val ertim: String = "",
    val grossWeight: String = "",
    val item: String = "",
    val kunnr: String = "",
    val materialName: String = "",
    val materialNumber: String = "",
    val mtn: String = "",
    val netWeight: String = "",
    val posnr: String = "",
    val qcStatus: String = "",
    val receivedWeight: String = "",
    val sentWeight: String = "",
    val supplierCode: String = "",
    val supplierName: String = "",
    val transactionBagDetails: MutableList<QualityTxnDetail> = mutableListOf(),
    val uom: String = "",
    val wbid: String = "",
    val werks: String = "",
    val wtype: String = ""
)

data class QualityTxnDetail(
    var transactionId: String = "",
    var noOfBags: Int = -1,
    var bagList: MutableList<QualityBag> = mutableListOf()
) {
    override fun toString(): String {
        return "QualityTxnDetail(transactionId='$transactionId', noOfBags=$noOfBags, bagList=$bagList)"
    }
}

data class QualityBag(
    var bagQrCode: String = "",
    var weight: String? = "",
    var isBagMissed: Boolean = false,
    var isInvalidQrCode: Boolean = false,
    var isSelected: Boolean = false


) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QualityBag

        if (bagQrCode != other.bagQrCode) return false

        return true
    }

    override fun hashCode(): Int {
        return bagQrCode.hashCode()
    }

    override fun toString(): String {
        return "QualityBag(bagQrCode='$bagQrCode', weight=$weight, isBagMissed=$isBagMissed, isInvalidQrCode=$isInvalidQrCode)"
    }


}
