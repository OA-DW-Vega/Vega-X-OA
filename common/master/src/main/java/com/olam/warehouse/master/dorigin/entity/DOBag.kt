package com.olam.warehouse.master.dorigin.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DOBag(

    @PrimaryKey
    var bagQrCode: Int = 0,
    var oldQrCode: Int = 0,
    var newQrCode: Int = 0,
    var lotTransactionId: String = "",
    var weight: String? = "",
    var bagMissed: Boolean = false,
    var invalidQrCode: Boolean = false,
    var isScanned: Boolean = false,
    var isReplaced: Boolean = false,
    var transactionId: String = ""


) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DOBag

        if (bagQrCode != other.bagQrCode) return false

        return true
    }

    override fun hashCode(): Int {
        return bagQrCode
    }
}
