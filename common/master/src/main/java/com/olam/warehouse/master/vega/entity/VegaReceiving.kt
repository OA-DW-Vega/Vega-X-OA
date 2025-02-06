package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceLineItemGrn
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 1/13/2020.
 */
@Entity
@Parcelize
data class VegaReceiving(
    @ColumnInfo(index = true)
    @PrimaryKey
    var tmpWbId: String = "",
    var weighBridgeId: String = "",
    var batchNumber: String? = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var bagWeight: String? = "0",
    var palletCount: String? = "",
    var palletType: String? = "", // grnSequnceNumber
    var palletWeight: String? = "0",
    var charg: String = "",
    var grossWeight: String? = "0",
    var item: String? = "",
    var delivery: String? = "",
    var deliveryItem: String? = "",
    var bagMaterialCode: String? = "",
    var costCentre: String? = "",
    var purchaseDocNum: String? = "",
    var purchaseDocDesc: String? = "",
    var purchaseDocQty: String? = "",
    var customerNum: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var materialPrice: Double? = 0.0,
    var grade: String? = "",
    var gradeDesc: String? = "",
    var certificate: String? = "",
    var yieldPercentage: String? = "",
    var netWeight: String = "0",
    var supplierCode: String? = "",
    var posnr: String? = "",
    var unitsOfMeasure: String = "",
    var plantId: String? = PreferenceHelper.get(Constants.WERKS, ""),
    var plantName: String? = "",
    var wsGate: String = "",
    var weighBridgeType: String = "",
    var location: String? = "",
    var tareWeight: String? = "0",
    var supplierName: String? = "",
    var isSynced: Boolean = false,
    var syncStarted: Int? = 0,
    var status: Status = Status.SYNC_PENDING,
    var syncStatusMsg: String? = "",
    var mtnCode: String? = "",
    var txnId: String? = "",
    var doWeightThreshold: Int? = 0,
    var truckDirection: String? = "",
    var imagePath: String? = "",
    var imageString: String? = "",
    var vehicleNumber: String? = "",
    var vehicleType: String? = "",
    var contactNumber: String? = "",
    var driverName: String? = "",
    var qcStatus: String? = "",
    var erdat: String? = "",
    var ertim: String? = "",
    var direction: String? = "",
    var transportVendorCode: String? = "",
    var transportVendorName: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var bagTareWeight: String? = "0",
    var dstorageLocationCode: String? = "",
    var dstorageLocationName: String? = "",
    var batchWeight: String? = "",
    var encodedImageContent: String? = "",
    var grnType: String? = "",
    var grnNumber: String? = "",
    var invoiceNumber: String? = "",
    var createdDate: String? = "",
    var docDate: String? = "",
    var price: String? = "",
    var netPayment: String? = "",
    var advance: String? = "",
    var wbFlag: Boolean? = false,
    var qcFlag: Boolean? = false,
    var grnFlag: Boolean? = false,
    var wRNo: String? = "",
    var product: String? = "",
    var receivingWH: String? = "",
    var grnModel: String? = "",
    var procurementType: String? = "",
    var grntNumber: String? = "",
    var truckNo: String? = "",
    var phoneNo: String? = "",
    var taxId: String? = "",
    var cascara: String? = "",
    var humedad: String? = "",
    var rendimientoBruto: String? = "",
    var exportablePercentage: String? = "",
    var DESMA: String? = "",
    var DESMC: String? = "",
    var DESMD: String? = "",
    var DESMR: String? = "",
    var FTDC: String? = "",
    var challan: String? = "",
    var storageLocation: String? = "",
    var declaredBagCount: String? = "",
    var declaredWeight: String? = "",
    var vendorDeclaredWeight: String? = "",
    var origin: String? = "",
    var department: String? = "",
    var shipmentNumber: String? = "",
    var remarks: String? = "",
    var mtnrDocSequence: String? = "",
    @Ignore
    var isProgress: Boolean = false,
    @Ignore
    var totalStockWeight: Double? = 0.0,
    @Ignore
    var isEdit: Boolean = false,
    var approximateWeight: String? = "",
    var commonPrimaryId: String? = "",
    var weighMethod: String? = "",
    var truckDriverName: String? = "",
    var invoiceFlag: Boolean? = false,
    @Ignore
    var inventoryDTO: List<InventoryDTO>? = emptyList(),
//New Fields
    var basePrice: String? = "",
    var certificatePremium: String? = "",
    var volumePremium: String? = "",
    var humidityPremium: String? = "",
    var qualityDiscounting: String? = "",
    var humidityDiscounting: String? = "",
    var grossValue: String? = "",
    var exportnCentives: String? = "",
    var withholdingTax: String? = "",
    var NSExchangeRate: String? = "",
    var bankCommission: String? = "",
    var totalDduction: String? = "",
    var totalPrice: String? = "",
    var finalPayment: String? = "",
    var currency: String? = "",
    var grossValuePerKg: String? = "",
    var qualityGradeDesc: String? = "",
    var netWeightQQs: String? = "",
    var advanceSummary: String? = "0.00",
    var advanceInterestSummary: String? = "0.00",
    var advanceCommissionSummary: String? = "0.00",
    var advanceLegalExpenseSummary: String? = "0.00",
    var advanceMaintainceSummary: String? = "0.00",
    var totalAdvanceSummary: String? = "0.00",
    var exchangeRate: String? = "",
    var tempBagCount: String? = "",
    var whReceiptNum: String? = "",
    var postDate: String? = "",
    var tollingVendorCode :String?="",
    var tollingVendorName :String?="",
    @Ignore
    var postingDate: String? = "",
    @Ignore
    var wtype: String? = "",
    @Ignore
    var vehNo: String? = "",
    var wsType: String? = "",
    var isOffline: Boolean? = false,
    var deleteFlag: Boolean? = false,
    var bagCount1: String? = "0",
    var bagType1: String? = "",
    var bagWeight1: String? = "0",
    var tareWeight1: String? = "0",
    @Ignore
    var advanceItems: List<VegaNicaraguaAdvanceLineItemGrn> = emptyList(),
    @Ignore
    var dseLotId:String?="",
    @Ignore
    var bagReturnFlag:Boolean=false,
    @Ignore
    var ttFarmerList: List<TrackTraceFarmerModel> = emptyList(),
    @Ignore
    var farmerTransDetails: TrackTraceFarmerTransDetails = TrackTraceFarmerTransDetails(),
    var sourceLotId: String = "",
    var farmerLessTransactionId: String? = "",
    var eudrStatus: Boolean = false,
    @Ignore
    var isDelete: Boolean= false,
    @Ignore
    var ftdcValue: String? = "",

    @Ignore
    var capacityWeight: String = "",
    @Ignore
    var leftOverWeight: String = ""

) : Parcelable

@Parcelize
data class InventoryDTO(
    var gradeDesc: String? = "",
    var certification: String? = "",
    var vendorCode: String? = "",
    @Ignore
    var inventoryQC:List<InventoryQc>? = emptyList()
) : Parcelable

@Parcelize
data class InventoryQc(
    var qcId:String?="",
    var qcName:String?="",
    var value:String?=""
): Parcelable



