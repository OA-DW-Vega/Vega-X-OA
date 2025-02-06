package com.olam.warehouse.vegax.secretid.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
@Parcelize
data class VegaCameroonSecretId(
    var challanNo: String = "",
    var weighBridgeId: String = "",
    var plant: String = "",
    var plntName: String = "",
    var chargeNum: String = "",
    var materialName: String = "",
    var materialCode: String = "",
    var weight: String = "",
    var unitOfMeasure: String = "",
    var inspectionDate: String = "",
    var wtype: String = "",
    var direction: String = "",
    var ntgew: String = "",
    var pkwgt: String = "",
    var grnNumber: String = "",
    var bagCount: String = "",
    var tempBagCount: String = "",
    var grossWeight: String = "",
    var tareWeight: String = "",
    var netWeight: String = "",
    var erdat: String = "",
    var ertim: String = "",
    var qcFlag: String = "",
    var vehNo: String = "",
    var wsItemSetDetails: List<VegaCameroonDashBoardWSNav> = emptyList() ,
    var qcRecordSetDetails: List<VegaCameroonDashBoardQCRecordNav> = emptyList() ,
    var date: String = ""
) : Parcelable

@Parcelize
data class VegaCameroonDashBoardWSNav(
    var weighBridgeId: String = "" ,
    var storageLocationCode:String = "",
    var grnDate:String =""
) : Parcelable

@Parcelize
data class VegaCameroonDashBoardQCRecordNav(
    var weighBridgeId: String = "" ,
    var plantId:String = "",
    var qcFlag:String = "",
    var grnNumber:String =""
) : Parcelable
