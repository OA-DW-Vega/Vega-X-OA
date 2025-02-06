package com.olam.warehouse.master.common.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.TrackTraceFarmerModel
import com.olam.warehouse.master.vega.entity.VegaReceiving

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */

data class VegaReceivingPost(
    val key: String,
    val plant: Plant,
    val weighDetails: List<VegaReceiving>,
    var notificationFlag: Boolean = false,
    var nextWorkFlowRole: String? = "",
    var navId: String? = "",
    var currentWorkFlowRole: String? = "",
    var environment: String? = "",
    var sourceLotId:String? = "",
    var eudrComplaint:Boolean? = false,
    var isDelete:Boolean=false,
    var ttFarmerList: List<TrackTraceFarmerModel> = emptyList(),
    var farmerLessTransactionId:String? = "",
    var bagCount:String? = "",
    var bagCount1:String? = "",
    var bagCount2:String? = "",
    var bagType:String? = "",
    var bagType1:String? = "",
    var bagType2:String? = "",
    var tareWeight:String? = "",
    var tareWeight1:String? = "",
    var tareWeight2:String? = "",
    var totalTarWeight:String? = "",
    var netWeight:String? = "",
    var grossWeight:String? = "",

)
data class BagDetails(
    var bagCount:String? = "",
    var bagCount1:String? = "",
    var bagCount2:String? = "",
    var bagType:String? = "",
    var bagType1:String? = "",
    var bagType2:String? = "",
    var tareWeight:String? = "",
    var tareWeight1:String? = "",
    var tareWeight2:String? = "",
    var totalTarWeight:String? = "",
    var netWeight:String? = "",
    var grossWeight:String? = ""

)
