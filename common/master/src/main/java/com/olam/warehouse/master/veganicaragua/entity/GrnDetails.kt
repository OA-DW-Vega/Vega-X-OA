
package com.olam.warehouse.master.veganicaragua.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import com.olam.warehouse.master.common.model.InventoryRes
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(primaryKeys = ["wbid", "supplierCode"])
data class GrnDetails(
    var wbid: String = "",
    var tempId: String? = "",
    var item: String? = "",
    var charg: String? = "",
    var batchNumber: String? = "",
    var year: String? = "",
    var unitsOfMeasure: String? = "",
    var materialName: String? = "",
    var materialNumber: String? = "",
    var supplierName: String? = "",
    var supplierCode: String = "",
    var werks: String? = "",
    var grn: String? = "",
    var discount: String? = "",
    var pchar: String? = "",
    var unitPrice: String? = "",
    var basePrice: String? = "",
    var kpein: String? = "",
    var totalPrice: String? = "0",
    var grnQty: String? = "0",
    var grnType: String? = "",
    var totalGrnPrice: String? = "0",
    var toatlGrnQty: String? = "0",
    var discountWeight: String? = "",
    var meins: String? = "",
    var waers: String? = "",
    var qchar: String? = "",
    var werksName: String? = "",
    var plantDesc: String? = "",
    var bprme: String? = "",
    var matkl: String? = "",
    var netPayment: String? = "",
    var advance: String? = "",
    var qualityGrade: String? = "",
    @Ignore
    val inventoryRes: InventoryRes? = null,
    @Ignore
    var qualityGradeDesc: String? = "",
    @Ignore
    var inventoryDetail: VegaNicaraguaGRNInventoryDetails? = null,
    var grnNumber: String? = "",
    @Ignore
    var isSelected: Boolean = false,
    @Ignore
    var isGrnInOffline: Boolean = false
): Parcelable
