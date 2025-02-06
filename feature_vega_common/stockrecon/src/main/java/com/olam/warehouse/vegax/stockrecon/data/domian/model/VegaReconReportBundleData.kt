package com.olam.warehouse.vegax.stockrecon.data.domian.model

import android.os.Parcelable
import com.olam.warehouse.master.vega.model.VegaStockReconGetAllAuditData
import kotlinx.parcelize.Parcelize

@Parcelize
/*This model class contain all recon report details throught all page*/
data class VegaReconReportBundleData(
    /*The below variable contains, user plant and date selection details*/
    var plantAndDate: PlantAndDate? = PlantAndDate(),
    /*The below variable contains, total reconlist details*/
    var reconListDetails: VegaReconReportReconList? = VegaReconReportReconList(),
    /*The below variable contains, user sected reconList details*/
    var selectedReconDetails: StockReconList? = StockReconList(),
    /*The below variable contains, user selected audit details*/
    var auditDetails: VegaStockReconGetAllAuditData? = VegaStockReconGetAllAuditData(),
    /*The below variable is for mainting the date range count, which is selected by the user*/
    var selectedDaysCount: String? = "",
    var isReport: Boolean = false
) : Parcelable

@Parcelize
data class PlantAndDate(
    var plant: String? = "",
    var fromDate: String? = "",
    var toDate: String? = ""
) : Parcelable
