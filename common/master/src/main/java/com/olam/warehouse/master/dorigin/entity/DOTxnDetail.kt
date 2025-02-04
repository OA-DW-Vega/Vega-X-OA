package com.olam.warehouse.master.dorigin.entity

import android.os.Parcelable
import androidx.room.Ignore
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DOTxnDetail(
    var lotTransactionId: String = "",
    var materialId: String? = "",
    var materialName: String? = "",
    var weightUOM: String? = "",
    var isLot: String? = "",
    var netweight: String? = "",
    var supplierId: String? = "",
    var supplierName: String? = "",
    var unitsOfMeasure: String? = "",
    var isMapped: Int? = 0,
    var displaySupplierId: String? = "",
    var creationDate: String? = "",
    var transactionDetails: MutableList<TransactionDetail>? = mutableListOf(),
    var emptyBagWeight: String? = "",
    var bagType: String? = "",
    var weightConversionToKG: String? = ""
) : Parcelable

@Parcelize
data class TransactionDetail(
    var noOfBags: Int? = 0,
    var transactionId: String = "",
    var netWeight: String = "",
    var bagList: MutableList<Bag> = mutableListOf()
) : Parcelable

@Parcelize
data class Bag(
    var bagQrCode: Int = 0,
    var weight: String? = "",
    var bagMissed: Boolean = false,
    var invalidQrCode: Boolean = false,
    @Ignore
    var isQrCodeReplaced: Boolean = false,
    @Ignore
    var newQrCode: Int = 0
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Bag

        if (bagQrCode != other.bagQrCode) return false

        return true
    }

    override fun hashCode(): Int {
        return bagQrCode
    }
}
