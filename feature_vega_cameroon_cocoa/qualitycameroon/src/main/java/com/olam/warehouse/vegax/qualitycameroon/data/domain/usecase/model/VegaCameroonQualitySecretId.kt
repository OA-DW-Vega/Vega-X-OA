package com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
@Parcelize
data class VegaCameroonQualitySecretId(
    var challan: String = "",
    var weighBridgeId: String = "",
    var item: String = "",
    var plant: String = "",
    var plntName: String = "",
    var chargeNum: String = "",
    var batchNumber: String = "",
    var materialName: String = "",
    var materialCode: String = "",
    var supplierCode: String = "",
    var weight: String = "",
    var unitOfMeasure: String = "",
    var inspectionDate: String = "",
    var wtype: String = "",
    var direction: String = "",
    var ntgew: String = "",
    var pkwgt: String = "",
    var grnNumber: String = "",
    var bagCount: String = "",
    var grossWeight: String = "",
    var tareWeight: String = "",
    var netWeight: String = "",
    var erdat: String = "",
    var ertim: String = "",
    var qcFlag: String = "",
    var pmat1: String = "",
    var pmatqty1: String = "",
    var unitsOfMeasure1: String = "",
    var pmatno2: String = "",
    var pmatqty2: String = "",
    var unitsOfMeasure2: String = "",
    var pmatno3: String = "",
    var pmatqty3: String = "",
    var unitsOfMeasure3: String = "",
    var wsPkwgt: String = "",
    var ebeln: String = "",
    var ebelp: String = "",
    var posnr: String = "",
    var vehNo: String = "",
    var wsItemSetDetails: List<VegaDashBoardWSNav> = emptyList() ,
    var qcRecordSetDetails: List<VegaCameroonDashBoardQCRecordNav> = emptyList() ,
    var date: String = ""
) : Parcelable

@Parcelize
data class VegaDashBoardWSNav(
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
