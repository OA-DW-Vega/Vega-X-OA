package com.olam.warehouse.master.common.model

import android.os.Parcelable
import com.olam.warehouse.master.common.modal.Inventory
import com.olam.warehouse.master.veganicaragua.entity.GrnDetails
import kotlinx.android.parcel.Parcelize

data class VendorGrnDetailsResponse(

    val vendor: Int,
    val grnDetails: List<GrnDetails>
)

/*data class GrnDetails(
    val item: String? = "",
    val charg: String = "",
    var batchNumber: String? = "",
    var unitsOfMeasure: String? = "",
    val materialName: String? = "",
    val materialNumber: String? = "",
    val supplierName: String? = "",
    val supplierCode: String? = "",
    val wbid: String? = "",
    val werks: String? = "",
    val grn: String? = "",
    val discount: String? = "",
    val pchar: String? = "",
    val unitPrice: String? = "",
    val kpein: String? = "",
    val totalPrice: String? = "",
    val grnQty: String? = "",
    val discountWeight: String? = "",
    val meins: String? = "",
    val waers: String? = "",
    val qchar: String? = "",
    val werksName: String? = "",
    val plantDesc: String? = "",
    val bprme: String? = "",
    val matkl: String? = "",
    val grnNumber: String? = "",
    val year: String? = "",
    val inventoryRes: InventoryRes? = null,
    val bsart: String? = "",
    val grnType: String? = ""
)*/

@Parcelize
data class InventoryRes(

    val inventory: List<Inventory>
) : Parcelable
