package com.olam.warehouse.master.vega.model

import android.os.Parcelable
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaStockReconBagDetails(
    var fullBagCount1: String? = "",
    var halfBagCount1: String? = "",
    var fullBagCount2: String? = "",
    var halfBagCount2: String? = "",
    var fullBagCount3: String? = "",
    var halfBagCount3: String? = "",
    var totalFullBackCount: String? = "",
    var totalHalfBackCount: String? = "",
    var bagType1: String? = "",
    var bagType2: String? = "",
    var bagType3: String? = "",
    var bagWeight1: String? = "",
    var bagWeight2: String? = "",
    var bagWeight3: String? = "",
    var bagDamaged: String? = "",
    var damagedBagCount: String? = "",
    var spillage: String? = "",
    var weightLoss: String? = "",
    var remarks: String? = "",
    var imageString: String? = "",
    var stockAuditWeight: String? = "",
    var selectedLots: VegaDispatchLots = VegaDispatchLots(),
    var reconIdDetails: VegaStockReconIdDetails = VegaStockReconIdDetails(),
    var plant:Plant = Plant()
) : Parcelable
