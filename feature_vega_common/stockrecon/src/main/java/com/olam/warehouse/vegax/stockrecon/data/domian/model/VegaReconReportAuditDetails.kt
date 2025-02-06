package com.olam.warehouse.vegax.stockrecon.data.domian.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaReconReportAuditDetails(
    var id:String? = "",
    var reconId: String? = "",
    var storageLocation: String? = "",
    var material: String? = "",
    var lotNumber: String? = "",
    var sysNoOfBags: String? = "",
    var bagType1: String? = "",
    var totalNoOfBags1: String? = "",
    var bagWeight1: String? = "",
    var noOfFullBags1: String? = "",
    var noOfHalfBags1: String? = "",
    var bagType2: String? = "",
    var totalNoOfBags2: String? = "",
    var bagWeight2: String? = "",
    var noOfFullBags2: String? = "",
    var noOfHalfBags2: String? = "",
    var bagType3: String? = "",
    var totalNoOfBags3: String? = "",
    var bagWeight3: String? = "",
    var noOfFullBags3: String? = "",
    var noOfHalfBags3: String? = "",
    var systemNetWeight: String? = "",
    var stockAuditWeight: String? = "",
    var weightGainLoss: String? = "",
    var noOfDamagedBags: String? = "",
    var spillage: Boolean = false,
    var bagDamaged: Boolean = false,
    var imageUrl: String = "",
    var remarks: String = "",
    var unitOfMeasure: String = ""
) : Parcelable
