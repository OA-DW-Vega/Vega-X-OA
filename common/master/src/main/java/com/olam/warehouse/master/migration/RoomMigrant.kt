package com.olam.warehouse.master.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Created by Baskaran Kannan on 8/4/2020.
 */

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE VegaCocoaFgrnItemsGrades ADD COLUMN sequence TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaReceiving ADD COLUMN challan TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaQualityWBDetails ADD COLUMN storageLocation TEXT DEFAULT ''")
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE VegaReceiving ADD COLUMN storageLocation TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaReceiving ADD COLUMN FTDC TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaCoffeeFgrnGradesMatrialWeights ADD COLUMN totalBagCount TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaEcuadorDispatchLots ADD COLUMN grossWeight TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGateEntry ADD COLUMN challan TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaOffloadingTrucks ADD COLUMN storageLocationCode TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaOffloadingTrucks ADD COLUMN driverNumber TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaEcuadorDispatchLots ADD COLUMN endLotFlag INTEGER")
        database.execSQL("ALTER TABLE VegaCoCoaReceiving ADD COLUMN truckDriverName TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaPurchaseOrders ADD COLUMN menge TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaPurchaseOrders ADD COLUMN meins TEXT DEFAULT ''")
        database.execSQL("CREATE TABLE VegaNicaraguaMtnt (weighBridgeId TEXT, batchPicking TEXT, weighBridgeType TEXT, plantId TEXT, batchNumber TEXT, materialCode TEXT, materialName TEXT, item TEXT, deliveryItem TEXT, delivery TEXT, purchaseDocNum TEXT, purchaseDocDesc TEXT, supplierCode TEXT, supplierName TEXT, isSyncStatus INTEGER, isErrorStatus INTEGER, isOfflineData INTEGER, status INTEGER, message TEXT, netWeight TEXT, erdat TEXT, grossWeight TEXT, unitsOfMeasure TEXT, vehicleNumber TEXT, vehicleType TEXT, contactNumber TEXT, driverName TEXT, storageLocationCode TEXT, storageLocationName TEXT, recStorageLocationCode TEXT, recPlantId TEXT, remarks TEXT, turnAroundTime TEXT, startTime TEXT, endTime TEXT, transportVendor TEXT, transportVendorID TEXT, deliveryStatus INTEGER, deliveryFlag INTEGER, pickingFlag INTEGER, storageLossFlag INTEGER, tempId TEXT NOT NULL, PRIMARY KEY(tempId))")
        database.execSQL("CREATE TABLE VegaNicDispatchLots (tempIdWithBatch TEXT, materialCode TEXT, materialName TEXT, plantId TEXT, storageLocationCode TEXT, unitOfMeasure TEXT, weight TEXT, editedWeight TEXT, isChecked INTEGER, processOrderNo TEXT, meins TEXT, rsNum TEXT, rsPos TEXT, bwart TEXT, phase TEXT, deliveryItem TEXT, xchpf TEXT, isLowerWeight INTEGER, isEndLot INTEGER, storageLossFlag INTEGER, weightToDispatchUOM TEXT, batchNumber TEXT NOT NULL, tempId TEXT NOT NULL, PRIMARY KEY(batchNumber, tempId))")
    }
}

val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            database.execSQL("ALTER TABLE VegaNicaraguaGrnCharDetails ADD COLUMN effectiveDate TEXT DEFAULT ''")
            database.execSQL("ALTER TABLE VegaNicaraguaGrnCharDetails ADD COLUMN erdate TEXT DEFAULT ''")
            database.execSQL("ALTER TABLE VegaReceiving ADD COLUMN approximateWeight TEXT DEFAULT ''")
            database.execSQL("ALTER TABLE VegaReceiving ADD COLUMN commonPrimaryId TEXT DEFAULT ''")
            database.execSQL("ALTER TABLE VegaReceiving ADD COLUMN weighMethod TEXT DEFAULT ''")
            database.execSQL("ALTER TABLE VegaGateEntry ADD COLUMN commonPrimaryId TEXT DEFAULT ''")
            database.execSQL("ALTER TABLE VegaGateEntry ADD COLUMN weighMethod TEXT DEFAULT ''")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

val MIGRATION_2_3_App: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {}
}

val MIGRATION_4_5: Migration = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE VegaOffloadingTrucks ADD COLUMN challan TEXT DEFAULT ''")
    }
}

val MIGRATION_5_6: Migration = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE VegaCoffeeReceiving ADD COLUMN commonPrimaryId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaCoffeeReceiving ADD COLUMN weighMethod TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaReceiving ADD COLUMN truckDriverName TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaReceiving ADD COLUMN syncStarted INTEGER")
        database.execSQL("ALTER TABLE VegaCoffeeReceiving ADD COLUMN truckDriverName TEXT DEFAULT ''")

        //FORWARD PO
        database.execSQL("CREATE TABLE VegaNicaraguaForwardPODetails (tempId TEXT NOT NULL, cascara TEXT, certificate TEXT,createdDate TEXT, currency TEXT, docDate TEXT, exchangeRate TEXT, grade TEXT,  humedad TEXT, materialCode TEXT, materialName TEXT, netPrice TEXT, netWeight TEXT, pricePerUnit TEXT,qualityGradeDesc TEXT, rendimientoBruto TEXT, unitsOfMeasure TEXT, vendorCode TEXT, vendorName TEXT, syncStatusMsg TEXT, syncStarted INTEGER,  poNumber TEXT, syncStatus INTEGER,PRIMARY KEY(tempId))")
        database.execSQL("CREATE TABLE VegaNicaraguaForwardPOPriceDetails (tempId TEXT NOT NULL, fieldName TEXT NOT NULL, companyCode TEXT, division TEXT, plant TEXT, purchasingOrg TEXT, purchasingGroup TEXT, description TEXT, priceDate TEXT, price TEXT, differential TEXT, currency TEXT, baseUnit TEXT, createdOn TEXT, percentage TEXT,PRIMARY KEY(tempId,fieldName))")
        database.execSQL("ALTER TABLE VegaNicaraguaMtnt ADD COLUMN qualityGrade TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaNicaraguaMtnt ADD COLUMN qulityGradeDesc TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaNicaraguaMtnt ADD COLUMN certification TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaNicaraguaMtnt ADD COLUMN vendorName TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaNicaraguaMtnt ADD COLUMN soWeight TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaNicaraguaMtnt ADD COLUMN soUOM TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaNicaraguaMtnt ADD COLUMN sendingPlant TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaNicaraguaMtnt ADD COLUMN vendorCode TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaNicDispatchLots ADD COLUMN truckNo TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaNicDispatchLots ADD COLUMN deliveryFlag INTEGER")
        database.execSQL("ALTER TABLE VegaNicDispatchLots ADD COLUMN pickingFlag INTEGER")
        database.execSQL("ALTER TABLE VegaNicDispatchLots ADD COLUMN pgiFlag INTEGER")
        database.execSQL("ALTER TABLE VegaEcuadorPurchaseOrder ADD COLUMN year TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGateEntry ADD COLUMN truckDriverName TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaEcuadorOffloadingBagMaterial ADD COLUMN truckNo TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaEcuadorOffloadingBagMaterial ADD COLUMN mtntNo TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaEcuadorOffloadingBagMaterial ADD COLUMN receivingLocation TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaEcuadorOffloadingBagMaterial ADD COLUMN wbId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaEcuadorOffloadingBagMaterial ADD COLUMN supplierName TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaEcuadorOffloadingBagMaterial ADD COLUMN materialName TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaCoffeeThirdPartyRequestModel ADD COLUMN remarks TEXT DEFAULT ''")
        //database.execSQL("ALTER TABLE VegaEcuadorOffloadingBagMaterial ADD COLUMN openQuantity TEXT DEFAULT ''")
        //GRN Purchase Order table changes
        database.execSQL("ALTER TABLE VegaEcuadorPurchaseOrder ADD COLUMN openQuantity TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaNicDispatchLots ADD COLUMN delivery TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaNicDispatchLots ADD COLUMN weighScaleWbId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaNicDispatchLots ADD COLUMN documentNum TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaPurchaseOrders ADD COLUMN warehouseId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaNicaraguaGRNInventoryDetails ADD COLUMN vendorCode TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaNicaraguaGRNInventoryDetails ADD COLUMN uom TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaNicaraguaGRNInventoryDetails ADD COLUMN materialName TEXT DEFAULT ''")

        database.execSQL("ALTER TABLE VegaEcuadorPurchaseOrder ADD COLUMN unitPrice TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaQualityWBDetails ADD COLUMN weighMethod TEXT DEFAULT ''")
        //advancing
        database.execSQL("CREATE TABLE VegaNicaraguaAdvanceDetails( vendor TEXT NOT NULL, availableLimit TEXT, creditLimit TEXT,PRIMARY KEY(vendor))")
        database.execSQL("CREATE TABLE VegaNicaraguaAdvanceTransactionDetails( tempId TEXT NOT NULL, vendorCode TEXT, vendorName TEXT,availableLimit TEXT, creditLimit TEXT, exchangeRate TEXT, requestedAdvanceAmount TEXT, tenureInDays TEXT, promissoryNumber TEXT, maturityDate TEXT, documentNumber TEXT, syncStatusMsg TEXT, syncStarted INTEGER,isOfflineData INTEGER, syncStatus INTEGER, createdDate TEXT, docDate TEXT, PRIMARY KEY(tempId))")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN yieldPercentage TEXT DEFAULT ''")
    }
}

val MIGRATION_6_7: Migration = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN invoiceFlag INTEGER")
        database.execSQL("ALTER TABLE VegaOffloadingTrucks ADD COLUMN appoximateWeight TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaAdvanceTransactionDetails ADD COLUMN erdat TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN isReceiptData INTEGER")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN netWeight TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN certificatePremium TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN volumePremium TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN humidityPremium TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN qualityDiscounting TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN humidityDiscounting TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN grossValue TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN exportnCentives TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN withholdingTax TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN NSExchangeRate TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN bankCommission TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN FTDC TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN totalDduction TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN finalPayment TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN currency TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN grossValuePerKg TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN qualityGradeDesc TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN netWeightQQs TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN advanceSummary TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN advanceInterestSummary TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN advanceCommissionSummary TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN advanceLegalExpenseSummary TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN advanceMaintainceSummary TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN totalAdvanceSummary TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN exchangeRate TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaEcuadorOffloadingBagMaterial ADD COLUMN mtntWeight TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicDispatchLots ADD COLUMN qualityGrade TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicDispatchLots ADD COLUMN certification TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  GrnDetails ADD COLUMN batchNumber TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  GrnDetails ADD COLUMN year TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  GrnDetails ADD COLUMN unitsOfMeasure TEXT DEFAULT ''")

    }
}

val MIGRATION_7_8: Migration = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN basePrice TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN certificatePremium TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN volumePremium TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN humidityPremium TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN qualityDiscounting TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN humidityDiscounting TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN grossValue TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN exportnCentives TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN withholdingTax TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN NSExchangeRate TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN bankCommission TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN totalDduction TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN totalPrice TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN finalPayment TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN currency TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN grossValuePerKg TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN qualityGradeDesc TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN netWeightQQs TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN advanceSummary TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN advanceInterestSummary TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN advanceCommissionSummary TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN advanceLegalExpenseSummary TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN advanceMaintainceSummary TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN totalAdvanceSummary TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN exchangeRate TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaAdvanceTransactionDetails ADD COLUMN accountingNumber TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN tareWeight TEXT DEFAULT ''")
    }
}

val MIGRATION_8_9: Migration = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN certification TEXT DEFAULT ''")
    }
}

val MIGRATION_9_10: Migration = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE VegaCameroonShippingLine (id INTEGER NOT NULL,shippingLineNm TEXT NOT NULL,shippingLineDesc TEXT NOT NULL,PRIMARY KEY(id) )")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_VegaCameroonShippingLine_id ON VegaCameroonShippingLine(id)")
        database.execSQL("CREATE TABLE VegaCameroonContainerSize(id TEXT NOT NULL,containerSize TEXT NOT NULL,PRIMARY KEY(id) )")
        database.execSQL("ALTER TABLE VegaCoffeeExportSalesLots ADD COLUMN mergedLotId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaCoffeeExportSalesLots ADD COLUMN receivingStorageLocation TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaEcuadorOffloadingBagMaterial ADD COLUMN purchaseDocDesc TEXT NOT NULL DEFAULT ''")
        database.execSQL("CREATE TABLE VegaCameroonOffloadingBagMaterial(tmpWbId TEXT NOT NULL, id INTEGER NOT NULL,materialCode TEXT NOT NULL,supplierCode TEXT NOT NULL, procureType TEXT NOT NULL, purcheseOrderNo TEXT NOT NULL, purchaseDocDesc TEXT  NOT NULL, batchNumber TEXT NOT NULL,  grossWeight TEXT NOT NULL, netWeight TEXT NOT NULL, bagType TEXT NOT NULL, bagCount TEXT NOT NULL, tareWeight TEXT, unitsOfMeasure TEXT,palletWeight TEXT, noOfPallet TEXT, palletAverage TEXT, status INTEGER, message TEXT, isSyncStatus INTEGER, truckNo TEXT, mtntNo TEXT, receivingLocation TEXT, wbId TEXT, supplierName TEXT, materialName TEXT, mtntWeight TEXT, PRIMARY KEY(tmpWbId,id))")
        database.execSQL("ALTER TABLE VegaGrnWeighBridgeId ADD COLUMN weighMethod TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaVendor ADD COLUMN vendorType TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaVendor ADD COLUMN vendorAdvLimit TEXT DEFAULT ''")
    }
}

val MIGRATION_10_11: Migration = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE VegaGhanaOfflineRminData (rminTempId TEXT NOT NULL,processingStage TEXT,outputMaterialCode TEXT,plant TEXT,rmin INTEGER,shiftType TEXT,operatorName TEXT,versionId TEXT,PRIMARY KEY(rminTempId) )")
        //database.execSQL("CREATE INDEX IF NOT EXISTS index_VegaGhanaOfflineRminData_rminTempId ON VegaGhanaOfflineRminData(rminTempId)")
        database.execSQL("CREATE TABLE VegaGhanaOfflineRminProcessLotDetails (rminTempId TEXT NOT NULL,bagCount TEXT,batchNumber TEXT,confText TEXT,deliveryItem TEXT,materialCode TEXT,menge TEXT,movementType TEXT,netWeight TEXT,grossWeight TEXT,startTime TEXT,endTime TEXT,phase TEXT,plant TEXT,bagType TEXT,year TEXT,processOrderNum TEXT,rsnum TEXT,rspos TEXT,storageLocationCode TEXT,unitsOfMeasure TEXT,xchpf TEXT,huno TEXT,huwt TEXT,huno2 TEXT,huwt2 TEXT,nohu1 TEXT,nohu2 TEXT,bagMaterialCode TEXT,endLotFlag INTEGER,vendorCode TEXT,storageLossFlag INTEGER,poNo TEXT,stage TEXT,materialName TEXT,syncStatusMsg TEXT,status INTEGER,PRIMARY KEY(rminTempId) )")
        //database.execSQL("CREATE INDEX IF NOT EXISTS index_VegaGhanaOfflineRminProcessLotDetails_rminTempId ON VegaGhanaOfflineRminProcessLotDetails(rminTempId)")
        database.execSQL("CREATE TABLE VegaGhanaOfflineRminLots (batchNumber TEXT NOT NULL,rminTempId TEXT,storageLocationCode TEXT,materialName TEXT,weight TEXT,editedWeight TEXT,PRIMARY KEY(batchNumber) )")
        database.execSQL("CREATE TABLE VegaGhanaOfflineRminItems (materialCode TEXT NOT NULL,rminTempId TEXT,materialName TEXT,weightToProcess TEXT,PRIMARY KEY(materialCode) )")
        database.execSQL("ALTER TABLE  VegaFgrnProcessingOrder ADD COLUMN storageLocationCode TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeFgrnItems ADD COLUMN storageLocationCode TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaInvoiceDetails ADD COLUMN invoiceNo TEXT DEFAULT ''")
    }
}
val MIGRATION_11_12: Migration = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE  VegaCoffeeExportSalesLots ADD COLUMN isBagCountMatched INTEGER ")
        database.execSQL("ALTER TABLE  VegaCoffeeExportSalesLots ADD COLUMN isLowerWeight INTEGER")
        database.execSQL("ALTER TABLE  VegaCoffeeSalesLots ADD COLUMN mergedLotId TEXT")
        database.execSQL("ALTER TABLE  VegaCoffeeSalesLots ADD COLUMN receivingStorageLocation TEXT")
        database.execSQL("ALTER TABLE  VegaCameroonOffloadingBagMaterial  ADD COLUMN isAutoBatchId INTEGER ")
        database.execSQL("ALTER TABLE  VegaCameroonOffloadingBagMaterial  ADD COLUMN receivingPlant TEXT DEFAULT '' ")
        database.execSQL("ALTER TABLE  VegaCameroonOffloadingBagMaterial  ADD COLUMN otLotNumber TEXT  DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaEcuadorOffloadingBagMaterial  ADD COLUMN isAutoBatchId INTEGER ")
        database.execSQL("ALTER TABLE  VegaEcuadorOffloadingBagMaterial  ADD COLUMN receivingPlant TEXT DEFAULT '' ")
        database.execSQL("ALTER TABLE  VegaEcuadorOffloadingBagMaterial  ADD COLUMN otLotNumber TEXT  DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoCoaQualityWBDetail ADD COLUMN pmat2Type TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoCoaQualityWBDetail ADD COLUMN pmat2Count TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoCoaQualityWBDetail ADD COLUMN pmat2Weight TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoCoaQualityWBDetail ADD COLUMN pmat3Type TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoCoaQualityWBDetail ADD COLUMN pmat3Count TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoCoaQualityWBDetail ADD COLUMN pmat3Weight TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaAdvanceLineItemGrn ADD COLUMN itemNum TEXT DEFAULT ''")

    }
}

val MIGRATION_12_13: Migration = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE  VegaQualityWBDetails ADD COLUMN unitPrice TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaSalesWB ADD COLUMN isRoundOff INTEGER")
        database.execSQL("ALTER TABLE  VegaCocoaFgrnItems ADD COLUMN isRoundOff INTEGER")
        database.execSQL("ALTER TABLE  VegaCocoaFgrnGradesMatrialWeights ADD COLUMN isRoundOff INTEGER")

        database.execSQL("ALTER TABLE  VegaCoffeeReceiving ADD COLUMN isOnlineData INTEGER")
        database.execSQL("ALTER TABLE  VegaCoffeeReceiving ADD COLUMN tempWBId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeReceiving ADD COLUMN syncId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceivingMtn ADD COLUMN isSynced INTEGER")

        database.execSQL("CREATE TABLE TempVegaGhanaOfflineRminLots (batchNumber TEXT NOT NULL,rminTempId TEXT NOT NULL,storageLocationCode TEXT,materialName TEXT,weight TEXT,editedWeight TEXT,PRIMARY KEY(batchNumber,rminTempId) )")
        database.execSQL("CREATE TABLE TempVegaGhanaOfflineRminItems (materialCode TEXT NOT NULL,rminTempId TEXT NOT NULL,materialName TEXT,weightToProcess TEXT,PRIMARY KEY(materialCode,rminTempId) )")
        database.execSQL("INSERT INTO TempVegaGhanaOfflineRminLots SELECT * FROM VegaGhanaOfflineRminLots")
        database.execSQL("INSERT INTO TempVegaGhanaOfflineRminItems SELECT * FROM VegaGhanaOfflineRminItems")
        database.execSQL("DROP TABLE VegaGhanaOfflineRminLots")
        database.execSQL("DROP TABLE VegaGhanaOfflineRminItems")
        database.execSQL("ALTER TABLE TempVegaGhanaOfflineRminLots RENAME TO VegaGhanaOfflineRminLots")
        database.execSQL("ALTER TABLE TempVegaGhanaOfflineRminItems RENAME TO VegaGhanaOfflineRminItems")

        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN recPlantName TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeOffloadingBagMaterial ADD COLUMN totalBagCount TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeOffloadingBagMaterial ADD COLUMN truckOutWeight TEXT DEFAULT ''")

        database.execSQL("CREATE TABLE VegaGhanaPurchaseOrderMaterialModel (weighBridgeId TEXT NOT NULL, materialCode TEXT NOT NULL, materialName TEXT, soWeight TEXT,uom TEXT ,soNumber TEXT, dispatchWeight TEXT, purchaseOrderNum TEXT, purchaseOrderDesc TEXT,PRIMARY KEY(weighBridgeId,materialCode))")
        database.execSQL("CREATE TABLE VegaGhanaCocoaDispatchLots (batchNumber TEXT NOT NULL, weighBridgeId TEXT NOT NULL, materialCode TEXT NOT NULL, materialName TEXT, plantId TEXT,status INTEGER, isSyncStatus INTEGER NOT NULL, plantName TEXT, storageLocationCode TEXT, unitOfMeasure TEXT, weight TEXT,  editedWeight TEXT, kor TEXT, region TEXT, isAdded INTEGER NOT NULL, isChecked INTEGER NOT NULL, processOrderNo TEXT, meins TEXT NOT NULL, rsNum TEXT, rsPos TEXT, bwart TEXT, phase TEXT, deliveryItem TEXT, xchpf TEXT,  noOfBags TEXT, slPostion INTEGER,isProgress INTEGER, isLowerWeight INTEGER NOT NULL, weightToDispatchUOM TEXT, deliveryFlag INTEGER NOT NULL, pickingFlag INTEGER NOT NULL, weighScaleWbId TEXT,  isPgiFlag INTEGER NOT NULL, delivery TEXT, vendor TEXT, vendorName TEXT, isEndLot  INTEGER, vendorWithTransferType TEXT NOT NULL,  storageLossFlag INTEGER, isBagCountMatched INTEGER NOT NULL, PRIMARY KEY(batchNumber,materialCode,vendorWithTransferType,weighBridgeId))")
        database.execSQL("CREATE TABLE VegaGhanaQualityMtnBatch (mtnNumber TEXT NOT NULL, posnr TEXT NOT NULL, batchNumber TEXT, deliveryQty TEXT,deliveryUOM TEXT ,stockQty TEXT, stockUOM TEXT, storageLocationCode TEXT, werks TEXT,PRIMARY KEY(mtnNumber,posnr))")
        database.execSQL("CREATE TABLE VegaGhanaLotQualityDetails (materialNumber TEXT NOT NULL, charg TEXT NOT NULL, sapQCName TEXT NOT NULL, satNam TEXT NOT NULL,PRIMARY KEY(charg,sapQCName))")
        database.execSQL(
            "CREATE TABLE VegaGhanaMtnrQualityLot(item TEXT , delivery TEXT , customerNum TEXT NOT NULL, purchaseDocNum TEXT NOT NULL,purchaseDocDesc TEXT NOT NULL,batchNumber TEXT NOT NULL, receivedWeight TEXT NOT NULL, sentWeight TEXT NOT NULL, materialName TEXT,materialCode TEXT, supplierName TEXT, supplierCode TEXT, deliveryItem TEXT, bagType TEXT,bagCount TEXT, bagWeight TEXT, pmat2Count TEXT, pmat2Type TEXT, pmat2Weight TEXT,pmat3Count TEXT, pmat3Type TEXT, pmat3Weight TEXT, unitsOfMeasure TEXT, netWeight TEXT,grossWeight TEXT, weighBridgeId TEXT NOT NULL, challan TEXT, plant TEXT, direction TEXT,weighBridgeType TEXT, erdat TEXT, ertim TEXT, qcStatus TEXT, bcApprover TEXT,vehicleNumber TEXT, storageLocationCode TEXT, storageLocation TEXT, transportVendorCode TEXT, tareWeight TEXT,batchPicking TEXT, vehicleType TEXT, contactNumber TEXT, driverNumber TEXT, truckType TEXT,truckDirection TEXT, bagTareWeight TEXT, driverName TEXT, batchWeight TEXT, weighMethod TEXT,grnNumber TEXT, dstorageLocationName TEXT, dstorageLocationCode TEXT, finalApproval TEXT, qualityFlag INTEGER,PRIMARY KEY(weighBridgeId,batchNumber))"
        )
        database.execSQL("CREATE TABLE VegaGhanaQuality (position INTEGER NOT NULL, wbid TEXT NOT NULL, wbTempId TEXT NOT NULL, materialCode TEXT NOT NULL,descrChar TEXT ,nameChar TEXT NOT NULL, entryObligatory TEXT, unitText TEXT, dataType TEXT,unitsOfMeasure TEXT ,numberDigits TEXT, numberDecimals TEXT, numValFm TEXT, numValTo TEXT,currValFm TEXT ,currValTo TEXT, valRelatn TEXT, timeStamp TEXT, qualityParameterValue TEXT,isSyncStatus INTEGER NOT NULL,status INTEGER, syncStatusMsg TEXT, preSampling TEXT, vegaMandatory TEXT,qualityParamLabel TEXT ,formulaParam TEXT,PRIMARY KEY(wbTempId,nameChar))")

        database.execSQL("ALTER TABLE  VegaNicaraguaAdvanceTransactionDetails ADD COLUMN isLocalCurrency INTEGER")
        database.execSQL("ALTER TABLE  VegaNicaraguaAdvanceDetails ADD COLUMN isLocalCurrency INTEGER")
        database.execSQL("ALTER TABLE  VegaNicaraguaAdvanceTransactionDetails ADD COLUMN availableLimitSign TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaAdvanceDetails ADD COLUMN availableLimitSign TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeFgrnItems ADD COLUMN rminId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeFgrnItems ADD COLUMN rminQty TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaGhanaOfflineRminProcessLotDetails ADD COLUMN fgrnStatus INTEGER")
        database.execSQL("CREATE TABLE VegaGhanaProcessingOrder (processOrderNo TEXT NOT NULL,plant TEXT,netWeight TEXT,unitsOfMeasure TEXT,materialName TEXT, auart TEXT, materialCode TEXT, rminTotal TEXT, rfgrnTotal TEXT, startDate TEXT, vendor TEXT, storageLocationCode TEXT, PRIMARY KEY(processOrderNo) )")
        database.execSQL("CREATE TABLE VegaGhanaProcessingOrderDetails (processOrderNo TEXT NOT NULL, rsPos TEXT NOT NULL, bwart TEXT,deliveryItem TEXT, materialName TEXT, materialCode TEXT, meins TEXT, phase TEXT, plant TEXT, resource TEXT, rsNum TEXT, storageLocationCode TEXT, xchpf TEXT, PRIMARY KEY(processOrderNo, rsPos) )")
        database.execSQL("CREATE TABLE VegaGhanaOfflineFgrnData (fgrnTempId TEXT NOT NULL, rminTempId TEXT,processingStage TEXT, outputMaterialCode TEXT, plant TEXT, rmin INTEGER, shiftType TEXT, operatorName TEXT, versionId TEXT, rminQty TEXT, status INTEGER, PRIMARY KEY(fgrnTempId) )")
        database.execSQL("CREATE TABLE VegaGhanaOfflineFgrnProcessLotDetails (fgrnTempId TEXT NOT NULL, rminTempId TEXT, bagCount TEXT,batchNumber TEXT,confText TEXT,deliveryItem TEXT,materialCode TEXT,menge TEXT,movementType TEXT,netWeight TEXT,grossWeight TEXT,startTime TEXT,endTime TEXT,phase TEXT,plant TEXT,bagType TEXT,year TEXT,processOrderNum TEXT,rsnum TEXT,rspos TEXT,storageLocationCode TEXT,unitsOfMeasure TEXT,xchpf TEXT,huno TEXT,huwt TEXT,huno2 TEXT,huwt2 TEXT,nohu1 TEXT,nohu2 TEXT,bagMaterialCode TEXT,endLotFlag INTEGER,vendorCode TEXT,storageLossFlag INTEGER,poNo TEXT,stage TEXT,materialName TEXT,syncStatusMsg TEXT,status INTEGER,PRIMARY KEY(fgrnTempId) )")
    }
}
val MIGRATION_13_14: Migration = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE  VegaCocoaFgrnItems ADD COLUMN isIndexweighmenttype INTEGER")
        database.execSQL("ALTER TABLE  VegaCocoaFgrnItemsGrades ADD COLUMN isIndexweighmenttype INTEGER")
        database.execSQL("ALTER TABLE  VegaCocoaFgrnItems ADD COLUMN weighmentType TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaFgrnItemsGrades ADD COLUMN isRoundOff INTEGER")
        database.execSQL("ALTER TABLE VegaReceiving ADD COLUMN declaredBagCount TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaReceiving ADD COLUMN declaredWeight TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaCoffeeReceiving ADD COLUMN declaredBagCount TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaCoffeeReceiving ADD COLUMN declaredWeight TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaReceiving ADD COLUMN vendorDeclaredWeight TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaReceiving ADD COLUMN origin TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaCoffeeReceiving ADD COLUMN origin TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaQualityWBDetails ADD COLUMN origin TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaReceiving ADD COLUMN department TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaCoffeeReceiving ADD COLUMN department TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaQualityWBDetails ADD COLUMN department TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaCoffeeReceiving ADD COLUMN vendorDeclaredWeight TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGateEntry ADD COLUMN vendorDeclaredWeight TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaQualityWBDetails ADD COLUMN vendorDeclaredWeight TEXT DEFAULT ''")

        }
    }

val MIGRATION_14_15: Migration = object : Migration(14, 15) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE  VegaReceivingMtnLots ADD COLUMN storageLocationCode TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaPurchaseOrders ADD COLUMN issueLocation TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN destinationWH TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN destinationWHName TEXT DEFAULT ''")
    }
}
val MIGRATION_15_16: Migration = object : Migration(15, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE  VegaMaterial ADD COLUMN typeCode TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaFgrnItemsGrades ADD COLUMN grossWeight TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaFgrnItemsGrades ADD COLUMN tareWeight TEXT DEFAULT ''")
      //  database.execSQL("CREATE TABLE VegaEcuadorBcApproveWBDetails (grnNumber TEXT NOT NULL,batchNumber TEXT,discount TEXT,discountWeight TEXT,grn TEXT,grnQty TEXT,grnType TEXT,item TEXT,materialName TEXT,materialNumber TEXT,wbid TEXT,werks TEXT,werksName TEXT,pchar TEXT,kpein TEXT,supplierCode TEXT,supplierName TEXT,plantDesc TEXT,waers TEXT,bprme TEXT,matkl TEXT,totalPrice TEXT,unitPrice TEXT,qchar TEXT,meins TEXT,unitsOfMeasure TEXT,year TEXT,inventoryRes TEXT,finalApproval TEXT,isAdded INTEGER,isChecked INTEGER,PRIMARY KEY(grnNumber) )")
    }
}
val MIGRATION_16_17: Migration = object : Migration(16, 17) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE  VegaCameroonOffloadingBagMaterial ADD COLUMN plant TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaEcuadorOffloadingBagMaterial ADD COLUMN plant TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaGateEntry ADD COLUMN tempBagCount TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaQualityWBDetails ADD COLUMN kor TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaEcuadorDispatchPurchaseOrders ADD COLUMN openQuantity TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeOffloadingBagMaterial ADD COLUMN tempWBId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeReceiveLots ADD COLUMN tempWBId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeReceiving ADD COLUMN isNotWBID INTEGER")
        //Double Data Type Added Here
        database.execSQL("CREATE TABLE TempVegaCoffeeReceiveLots (batch TEXT NOT NULL,materialNumber TEXT NOT NULL,mtnNumber TEXT NOT NULL,tempWBId TEXT NOT NULL,materialName TEXT,plantId TEXT,plantName TEXT,storageLocationCode TEXT,weight TEXT,editedWeight TEXT,processOrderNo TEXT,deliveryItem TEXT,delivery TEXT,posnr TEXT NOT NULL,uom TEXT NOT NULL,purchaseOrder TEXT NOT NULL,ebelp TEXT NOT NULL,supplyingPlantId TEXT NOT NULL,supplyingPlantName TEXT NOT NULL,editedUOM TEXT NOT NULL,status INTGER,isSyncStatus INTGER NOT NULL,isLowerWeight INTGER NOT NULL,deliveryFlag INTGER NOT NULL,pickingFlag INTGER NOT NULL,hasWeightAdded INTGER NOT NULL,bagCount INTGER NOT NULL,weightAdded REAL NOT NULL,PRIMARY KEY(batch,materialNumber,mtnNumber,tempWBId) )")
        database.execSQL("INSERT INTO TempVegaCoffeeReceiveLots SELECT * FROM VegaCoffeeReceiveLots")
        database.execSQL("DROP TABLE VegaCoffeeReceiveLots")
        database.execSQL("ALTER TABLE TempVegaCoffeeReceiveLots RENAME TO VegaCoffeeReceiveLots")
        database.execSQL(
            "CREATE TABLE VegaCoffeeLot (batchNumber TEXT NOT NULL,weighBridgeId TEXT NOT NULL,tempId TEXT,delivery TEXT,item TEXT,customerNum TEXT,purchaseDocNum TEXT,purchaseDocDesc TEXT,receivedWeight TEXT,sentWeight TEXT,materialName TEXT,materialCode TEXT,supplierName TEXT,supplierCode TEXT,deliveryItem TEXT,bagType TEXT,bagCount TEXT,bagWeight TEXT,pmat2Count TEXT,pmat2Type TEXT,pmat2Weight TEXT,pmat3Count TEXT,pmat3Type TEXT,pmat3Weight TEXT,unitsOfMeasure TEXT,netWeight TEXT,grossWeight TEXT,challan TEXT,plant TEXT,direction TEXT,weighBridgeType TEXT,erdat TEXT,ertim TEXT,qcStatus TEXT,bcApprover TEXT,vehicleNumber TEXT,storageLocationCode TEXT,storageLocation TEXT,transportVendorCode TEXT,tareWeight TEXT,batchPicking TEXT,vehicleType TEXT,contactNumber TEXT,driverNumber TEXT,truckType TEXT,truckDirection TEXT,bagTareWeight TEXT,driverName TEXT,batchWeight TEXT,weighMethod TEXT,grnNumber TEXT,dstorageLocationName TEXT,dstorageLocationCode TEXT,finalApproval TEXT,qualityFlag INTEGER,isOffline INTEGER, PRIMARY KEY(batchNumber,weighBridgeId) )"
        )
        database.execSQL("ALTER TABLE  VegaGrnWeighBridgeId ADD COLUMN challan TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaGrnWeighBridgeId ADD COLUMN purchaseType TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaGrnWeighBridgeId ADD COLUMN isTransStatus INTEGER")
        database.execSQL("ALTER TABLE  VegaEcuadorDispatch ADD COLUMN deliveryDetail TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeExportSalesContainer ADD COLUMN tmpId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeExportSalesLots ADD COLUMN tmpId TEXT DEFAULT ''")

    }
}

val MIGRATION_17_18: Migration = object : Migration(17, 18) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IndoExporSalesMaterialList (salesOrderId TEXT NOT NULL,salesItemNum TEXT,materialNumber TEXT,materialDesc TEXT,plantId TEXT,vkorg TEXT,vtweg TEXT,Spart TEXT,soldToPartyName TEXT,createdDate TEXT,menge TEXT,meins TEXT,openQuantity TEXT,soldToPartyCode TEXT,shipToPartyName TEXT,shipToPartyCode TEXT,PRIMARY KEY(salesOrderId) )")
        database.execSQL("CREATE TABLE VegaIndoCoffeeExportSalesOrder (tmpId TEXT NOT NULL,saleOrderId TEXT,salesType TEXT,salesOrderDesc TEXT,customerId TEXT,customerName TEXT,openQuantity TEXT,unitOfMeasure TEXT,materialCode TEXT,materialName TEXT,salesItem TEXT,createdDate TEXT,startTime TEXT,endTime TEXT,turnAroundTime TEXT,isSynced INTEGER,isOffline INTEGER,isTransStatus INTEGER,status INTEGER,syncStatusMsg TEXT,message TEXT,erdat TEXT,remarks TEXT,plantId TEXT,deliveryFlag INTEGER,pickingFlag INTEGER,pgiFlag INTEGER,containerFlag INTEGER,plantName TEXT,PRIMARY KEY(tmpId) )")
    }
}

val MIGRATION_18_19: Migration = object : Migration(18, 19) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE  VegaCoffeeReceiveLots ADD COLUMN wbFlag INTEGER")
        database.execSQL("ALTER TABLE  VegaCoffeeReceiveLots ADD COLUMN qcFlag INTEGER")
        database.execSQL("ALTER TABLE  VegaCoffeeReceiveLots ADD COLUMN grnFlag INTEGER")
        database.execSQL("ALTER TABLE  VegaCoffeeReceiving ADD COLUMN grnNumber TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaEcuadorDispatchLots ADD COLUMN binBatch TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCameroonOffloadingBagMaterial ADD COLUMN tempBagCount TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaEcuadorOffloadingBagMaterial ADD COLUMN tempBagCount TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN tempBagCount TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaQualityWBDetails ADD COLUMN qualityFlag INTEGER")
        database.execSQL("ALTER TABLE  VegaDispatchLots ADD COLUMN mergedLotId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaDispatchLots ADD COLUMN receivingStorageLocation TEXT DEFAULT ''")

        database.execSQL("ALTER TABLE  VegaGrnWeighBridgeId ADD COLUMN warehouseRecieptNum TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaGrnWeighBridgeId ADD COLUMN billOfLading TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaGrnWeighBridgeId ADD COLUMN pmat2Count TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaGrnWeighBridgeId ADD COLUMN pmat2Type TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaQualityWBDetails ADD COLUMN appName TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaQualityWBDetails ADD COLUMN LOBM_UDCODE TEXT DEFAULT ''")

        database.execSQL("ALTER TABLE  VegaCocoaRminProcessing ADD COLUMN poQuantity TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicDispatchLots ADD COLUMN isMergedLot INTEGER")
        database.execSQL("ALTER TABLE  VegaGateEntry ADD COLUMN remarks TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN remarks TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeReceiving ADD COLUMN challan TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaForwardPODetails ADD COLUMN poSequenceNumber TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaForwardPODetails ADD COLUMN message TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaForwardPODetails ADD COLUMN taxNumber TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaMtnt ADD COLUMN mergedNetWeight TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaOffloadingTrucks ADD COLUMN qualityFlag INTEGER")
        database.execSQL("ALTER TABLE  VegaOffloadingTrucks ADD COLUMN appName TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaQualityWBDetails ADD COLUMN mergedBatchNumber TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaQualityWBDetails ADD COLUMN receivedWeight TEXT DEFAULT ''")
    }
}
val MIGRATION_19_20: Migration = object : Migration(19, 20) {
    override fun migrate(database: SupportSQLiteDatabase) {
        //database.execSQL("ALTER TABLE  Container ADD COLUMN isSelected INTEGER")
    }
}

val MIGRATION_20_21: Migration = object : Migration(20, 21) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN wsType TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN isOffline INTEGER")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN bagMaterialCode TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN costCentre TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN purchaseQuantity TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN routeLocCode TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN textId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN textValue TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN shipmentNumber TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN truckID TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN departurePoint TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN route TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN fromVendorCode TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN bagMaterialCode TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaGateEntry ADD COLUMN truckId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN shipmentNumber TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceivingWarehouse ADD COLUMN isExpanded INTEGER")
        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN driverLicenseNumber TEXT DEFAULT ''")
        database.execSQL("CREATE TABLE VegaUomDetails (materialCode TEXT NOT NULL,fromUom TEXT NOT NULL,toUom TEXT DEFAULT '',value1 TEXT DEFAULT '',value2 TEXT DEFAULT '',PRIMARY KEY(materialCode, fromUom))")
        database.execSQL("CREATE TABLE VegaPlanRoute (id INTEGER NOT NULL,departureLocCode TEXT,departureLocName TEXT,routeLocationName TEXT,routeLocCode TEXT,sourceLocCode TEXT,PRIMARY KEY(id))")
        database.execSQL("ALTER TABLE  VegaNicaraguaForwardPODetails ADD COLUMN vendorAddress TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaForwardPODetails ADD COLUMN deliveryDate TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaNicaraguaForwardPODetails ADD COLUMN receiptNetPrice TEXT DEFAULT ''")
        database.execSQL("CREATE TABLE VegaStoragelocationDetail (materialCode TEXT NOT NULL,storageLocationCode TEXT NOT NULL,plant TEXT,storageLocationName TEXT,PRIMARY KEY(materialCode, storageLocationCode))")
    }
}

val MIGRATION_21_22: Migration = object : Migration(21, 22) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE TempVegaEcuadorDispatchStocks (batchNumber TEXT NOT NULL,bkBez TEXT,bkLas TEXT,cinsm TEXT,materialCode TEXT NOT NULL,materialName TEXT,materialText TEXT,plantId TEXT,plantName TEXT,storageLocationCode TEXT NOT NULL,unitOfMeasure TEXT,weight TEXT,vendor TEXT,vendorName TEXT,PRIMARY KEY(batchNumber,materialCode,storageLocationCode) )")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_TempVegaEcuadorDispatchStocks_batchNumber ON TempVegaEcuadorDispatchStocks(batchNumber)")
        database.execSQL("INSERT INTO TempVegaEcuadorDispatchStocks SELECT * FROM VegaEcuadorDispatchStocks")
        database.execSQL("DROP TABLE VegaEcuadorDispatchStocks")
        database.execSQL("ALTER TABLE TempVegaEcuadorDispatchStocks RENAME TO VegaEcuadorDispatchStocks")
        database.execSQL("CREATE TABLE VehicleDetails (vehicleId INTEGER NOT NULL,vehicleNumber TEXT NOT NULL,driverLicenseNumber TEXT,driverName TEXT,driverPhone TEXT,vendorCode TEXT,name TEXT,PRIMARY KEY(vehicleId, vehicleNumber))")
        database.execSQL("ALTER TABLE  VegaQualityWBDetails ADD COLUMN shipmentNumber TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaGateEntry ADD COLUMN bagMaterialCode TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN whReceiptNum TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaCocoaDispatchWB ADD COLUMN operatorName TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaFgrnProcessingOrder ADD COLUMN versionId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaCocoaFgrnItems ADD COLUMN versionId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaSweepingBagMaterial ADD COLUMN bagCount1 TEXT DEFAULT  ''")
        database.execSQL("ALTER TABLE  VegaCocoaSweepingBagMaterial ADD COLUMN tareWeight1 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaSweepingBagMaterial ADD COLUMN bagType1 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaSweepingBagMaterial ADD COLUMN bagMaterialCode1 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeFgrnGradesMatrialWeights ADD COLUMN bagCount1 TEXT DEFAULT  ''")
        database.execSQL("ALTER TABLE  VegaCoffeeFgrnGradesMatrialWeights ADD COLUMN tareWeight1 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeFgrnGradesMatrialWeights ADD COLUMN bagType1 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeFgrnGradesMatrialWeights ADD COLUMN bagMaterialCode1 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeOffloadingBagMaterial ADD COLUMN bagCount1 TEXT DEFAULT  ''")
        database.execSQL("ALTER TABLE  VegaCoffeeOffloadingBagMaterial ADD COLUMN tareWeight1 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeOffloadingBagMaterial ADD COLUMN bagType1 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCoffeeOffloadingBagMaterial ADD COLUMN bagMaterialCode1 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaEcuadorOffloadingBagMaterial ADD COLUMN bagCount1 TEXT DEFAULT  ''")
        database.execSQL("ALTER TABLE  VegaEcuadorOffloadingBagMaterial ADD COLUMN tareWeight1 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaEcuadorOffloadingBagMaterial ADD COLUMN bagType1 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN bagCount1 TEXT DEFAULT  ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN tareWeight1 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN bagType1 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN bagWeight1 TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN sidingDepotwayBillNumber TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN evacuationCertificateNumber TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaQualityWBDetails ADD COLUMN mtnNumber TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceivingMtn ADD COLUMN gateEntry TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaQualityWBDetails ADD COLUMN gateEntry TEXT DEFAULT ''")
        database.execSQL("CREATE TABLE TempVegaCustomStLocation (procureLocationCode TEXT NOT NULL,plant TEXT NOT NULL,procureLocationName TEXT,storageLocationType TEXT,PRIMARY KEY(procureLocationCode,plant))")
        database.execSQL("INSERT INTO TempVegaCustomStLocation SELECT * FROM VegaCustomStLocation")
        database.execSQL("DROP TABLE VegaCustomStLocation")
        database.execSQL("ALTER TABLE TempVegaCustomStLocation RENAME TO VegaCustomStLocation")
        database.execSQL("ALTER TABLE VegaGrnWeighBridgeId ADD COLUMN currency TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGrnWeighBridgeId ADD COLUMN finalApproval TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGrnWeighBridgeId ADD COLUMN pmat2Weight TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGrnWeighBridgeId ADD COLUMN pmat3Count TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGrnWeighBridgeId ADD COLUMN pmat3Type TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGrnWeighBridgeId ADD COLUMN pmat3Weight TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGrnWeighBridgeId ADD COLUMN postingDate TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGrnWeighBridgeId ADD COLUMN price TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGrnWeighBridgeId ADD COLUMN purchaseOrderNum TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGrnWeighBridgeId ADD COLUMN qcParamValue TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGrnWeighBridgeId ADD COLUMN userName TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGrnWeighBridgeId ADD COLUMN currentWbId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaReceivingMtn ADD COLUMN mtntDate TEXT DEFAULT ''")

//        database.execSQL("CREATE TABLE VegaGateEntryDetails (id INTEGER NOT NULL,tmpWbId TEXT,wtype TEXT,wsgate TEXT,wbid TEXT,updatedBy TEXT,updatedAt TEXT,truckNumber TEXT,tareWeight TEXT,storageLocationCode TEXT,sampleId TEXT,netWeight TEXT,item TEXT,grossWeight TEXT,geStatus TEXT,driverNumber TEXT,driverName TEXT,currentYear TEXT,createdBy TEXT,grnModel TEXT,procurementType TEXT,PRIMARY KEY(id))")

        database.execSQL("ALTER TABLE VegaGateEntry ADD COLUMN grnModel TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGateEntry ADD COLUMN procurementType TEXT DEFAULT ''")
//        database.execSQL("ALTER TABLE VegaGateEntry ADD COLUMN currentWbId TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGateEntry ADD COLUMN driverNumber TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGateEntry ADD COLUMN wsType TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGateEntry ADD COLUMN receivingPlant TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaOffloadingTrucks ADD COLUMN grnModel TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaOffloadingTrucks ADD COLUMN procurementType TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaQualityWBDetails ADD COLUMN grnModel TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaQualityWBDetails ADD COLUMN procurementType TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGrnWeighBridgeId ADD COLUMN grnModel TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaGrnWeighBridgeId ADD COLUMN procurementType TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaQualityWBDetails ADD COLUMN autoTransfer TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaQualityWBDetails ADD COLUMN recStorageLocation TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaCocoaDispatchLots ADD COLUMN grossWeight TEXT DEFAULT ''")
        //basePrice for DSE
        database.execSQL("ALTER TABLE  VegaQualityWBDetails ADD COLUMN basePrice TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaProcessingRminBoms ADD COLUMN inputMaterialCode TEXT DEFAULT ''")
        //Double Data Type Added Here
        database.execSQL("CREATE TABLE TempVegaProcessingRminBoms (cfgno TEXT NOT NULL,materialCode TEXT NOT NULL,inputMaterialCode TEXT,materialName TEXT,versionId TEXT,PRIMARY KEY(cfgno,materialCode) )")
        database.execSQL("INSERT INTO TempVegaProcessingRminBoms SELECT * FROM VegaProcessingRminBoms")
        database.execSQL("DROP TABLE VegaProcessingRminBoms")
        database.execSQL("ALTER TABLE TempVegaProcessingRminBoms RENAME TO VegaProcessingRminBoms")
    }
}
val MIGRATION_22_23: Migration = object : Migration(22, 23) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE VegaNicaraguaAdvanceLineItems ADD COLUMN itemNum TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE VegaNicaraguaAdvanceLineItems ADD COLUMN deletedFlag INTEGER")
    }
}

val MIGRATION_23_24: Migration = object : Migration(23, 24) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN grnModel TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN procurementType TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaGateEntry ADD COLUMN plantName TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaGateEntry ADD COLUMN grntNumber TEXT DEFAULT ''")
        database.execSQL("CREATE TABLE VegaGateEntryDetails (id INTEGER NOT NULL,tmpWbId TEXT,wtype TEXT,wsgate TEXT,wbid TEXT,updatedBy TEXT,updatedAt TEXT,truckNumber TEXT,tareWeight TEXT,storageLocationCode TEXT,sampleId TEXT,netWeight TEXT,item TEXT,grossWeight TEXT,geStatus TEXT,driverNumber TEXT,driverName TEXT,currentYear TEXT,createdBy TEXT,grnModel TEXT,procurementType TEXT, grntNumber TEXT,challan TEXT, PRIMARY KEY(id))")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_VegaGateEntryDetails_id ON VegaGateEntryDetails(id)")
        database.execSQL("ALTER TABLE  VegaOffloadingTrucks ADD COLUMN grntNumber TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaReceiving ADD COLUMN grntNumber TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaQualityWBDetails ADD COLUMN grntNumber TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaGrnWeighBridgeId ADD COLUMN grntNumber TEXT DEFAULT ''")
    }
}

val MIGRATION_24_25: Migration = object : Migration(24, 25) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE  VegaGateEntryDetails ADD COLUMN imageString TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaEcuadorDispatchLots ADD COLUMN encodedImageContent TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaEcuadorDispatchLots ADD COLUMN imageUploadMsg TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VegaCocoaDispatchWB ADD COLUMN encodedImageContent TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE  VehicleDetails ADD COLUMN qrCodeNumber TEXT DEFAULT ''")
        database.execSQL("CREATE TABLE VegaNicOfflineRminData (rminTempId TEXT NOT NULL,processingStage TEXT,outputMaterialCode TEXT,plant TEXT,rmin INTEGER,shiftType TEXT,operatorName TEXT,versionId TEXT,PRIMARY KEY(rminTempId) )")
        database.execSQL("CREATE TABLE VegaNicOfflineRminProcessLotDetails (rminTempId TEXT NOT NULL,bagCount TEXT,batchNumber TEXT,confText TEXT,deliveryItem TEXT,materialCode TEXT,menge TEXT,movementType TEXT,netWeight TEXT,grossWeight TEXT,startTime TEXT,endTime TEXT,phase TEXT,plant TEXT,bagType TEXT,year TEXT,processOrderNum TEXT,rsnum TEXT,rspos TEXT,storageLocationCode TEXT,unitsOfMeasure TEXT,xchpf TEXT,huno TEXT,huwt TEXT,huno2 TEXT,huwt2 TEXT,nohu1 TEXT,nohu2 TEXT,bagMaterialCode TEXT,endLotFlag INTEGER,vendorCode TEXT,storageLossFlag INTEGER,poNo TEXT,stage TEXT,materialName TEXT,syncStatusMsg TEXT,status INTEGER,fgrnStatus INTEGER,PRIMARY KEY(rminTempId) )")
        database.execSQL("CREATE TABLE VegaNicOfflineRminLots (batchNumber TEXT NOT NULL,rminTempId TEXT NOT NULL,poNo TEXT,storageLocationCode TEXT,materialName TEXT,weight TEXT,editedWeight TEXT,lotId TEXT NOT NULL, PRIMARY KEY(batchNumber, rminTempId) )")
        database.execSQL("CREATE TABLE VegaNicOfflineRminItems (materialCode TEXT NOT NULL,rminTempId TEXT NOT NULL,materialName TEXT,weightToProcess TEXT,PRIMARY KEY(materialCode, rminTempId) )")
    }
}
