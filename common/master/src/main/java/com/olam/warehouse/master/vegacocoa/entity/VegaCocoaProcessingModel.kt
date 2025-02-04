package com.olam.warehouse.master.vegacocoa.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.android.parcel.Parcelize

@Entity(primaryKeys = arrayOf("rminId", "materialCode", "cgfNo", "bom", "poNumber"))
@Parcelize
data class VegaCocoaRminProcessing(
    var materialCode: String = "",
    var materialName: String = "",
    var stage: String = "",
    var cgfNo: String = "",
    var foreverNo: String = "",
    var bom: String = "",
    var poNumber: String = "",
    var weightToProcess: String = "",
    var remark: String? = "",
    var shift: String? = "",
    var auartNo: String? = "",
    var baseMaterialCode: String? = "",
    var baseMaterialName: String? = "",
    var rsnum: String? = "",
    var rspos: String? = "",
    var xchpf: String? = "",
    var bwart: String? = "",
    var phase: String? = "",
    var resource: String? = "",
    var versionId: String? = "",
    var status: Int? = 1,
    @Ignore
    var lotList: ArrayList<VegaCocoaRminLots>? = ArrayList(),
    var rminId: String = "",
    var poQuantity: String? = ""
) : Parcelable


