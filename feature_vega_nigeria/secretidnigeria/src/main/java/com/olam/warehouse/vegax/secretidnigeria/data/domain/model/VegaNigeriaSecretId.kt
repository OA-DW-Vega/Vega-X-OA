package com.olam.warehouse.vegax.secretidnigeria.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */

@Parcelize
data class VegaNigeriaSecretId(
    var weighBridgeId: String = "",
    var item: String = "",
    var ajahr: String = "",
    var plant: String = "",
    var plntName: String = "",
    var counter: String = "",
    var chargeNum: String = "",
    var tmode: String = "",
    var direction: String = "",
    var directionT: String = "",
    var qcFlag: String = "",
    var qcDate: String = "",
    var qcTime: String = "",
    var bcDate: String = "",
    var bcTime: String = "",
    var grDate: String = "",
    var grTime: String = "",
    var invDate: String = "",
    var invTime: String = "",
    var wtype: String = "",
    var wtypeT: String = "",
    var contno: String = "",
    var ernam: String = "",
    var erdat: String = "",
    var ertim: String = "",
    var voref: String = "",
    var delT: String = "",
    var value: String = "",
    var brgew: String = "",
    var unitsOfMeasure: String = "",
    var gtime: String = "",
    var ttrwgt: String = "",
    var trwgt: String = "",
    var tdate: String = "",
    var ttime: String = "",
    var ntgew: String = "",
    var pkwgt: String = "",
    var ebeln: String = "",
    var ebelp: String = "",
    var posnr: String = "",
    var materialCode: String = "",
    var materialName: String = "",
    var supplierCode: String = "",
    var grnNumber: String = "",
    var pmat1: String = "",
    var bagCount: String = "",
    var pmatqty1: String = "",
    var unitsOfMeasure1: String = "",
    var pmatno2: String = "",
    var pmatqty2: String = "",
    var unitsOfMeasure2: String = "",
    var pmatno3: String = "",
    var pmatqty3: String = "",
    var unitsOfMeasure3: String = "",
    var rueck: String = "",
    var rmzhl: String? = "",
    var batchNumber: String = "",
    var tnwt: String = "",
    var grossWeight: String = "",
    var tareWeight: String = "",
    var netWeight: String = "",
    var wsPkwgt: String = "",
    var zeile: String = "",
    var actweight: String = "",
    var aenam: String = "",
    var aedat: String = "",
    var aetim: String = "",
    var storageLocationCode: String = "",
    var palletType: String = "",
    var grnType: String? = "",
    var inventoryDTO: String? = "",
    var qcRecordSetDetails: List<VegaNigeriaDashBoardQCRecordNav>? = emptyList(),
    var challan: String = "",
    var vehNo: String = "",
    var tempBagCount: String = "",
    var wsItemSetDetails: List<VegaNigeriaDashBoardWSNav>? = emptyList()
) : Parcelable

@Parcelize
data class VegaNigeriaDashBoardWSNav(
    var weighBridgeId: String = "",
    var storageLocationCode: String? = "",
    var grnDate: String? = ""
) : Parcelable

@Parcelize
data class VegaNigeriaDashBoardQCRecordNav(
    var weighBridgeId: String = "",
    var plantId: String? = "",
    var qcFlag: String = "",
    var grnNumber: String? = ""
) : Parcelable

