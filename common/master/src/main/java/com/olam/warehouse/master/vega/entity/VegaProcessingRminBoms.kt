package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.android.parcel.Parcelize

@Parcelize
class VegaProcessingRminBom(
    var boms: List<VegaProcessingRminBoms> = emptyList()
) : Parcelable

@Entity(primaryKeys = ["cfgno", "materialCode"])
@Parcelize
class VegaProcessingRminBoms(
    var cfgno: String = "",
    var materialCode: String = "",
    @Ignore
    var baseMaterialCode: String? = "",
    var inputMaterialCode: String? = "",
    var materialName: String? = "",
    var versionId: String? = ""
) : Parcelable




