package com.olam.warehouse.master

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.olam.warehouse.master.SQLCipherUtils.getDatabaseState
import com.olam.warehouse.master.common.dao.TrackTraceDao
import com.olam.warehouse.master.common.dao.VegaDummyQualityDao
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.migration.MIGRATION_10_11
import com.olam.warehouse.master.migration.MIGRATION_11_12
import com.olam.warehouse.master.migration.MIGRATION_12_13
import com.olam.warehouse.master.migration.MIGRATION_13_14
import com.olam.warehouse.master.migration.MIGRATION_14_15
import com.olam.warehouse.master.migration.MIGRATION_15_16
import com.olam.warehouse.master.migration.MIGRATION_16_17
import com.olam.warehouse.master.migration.MIGRATION_17_18
import com.olam.warehouse.master.migration.MIGRATION_18_19
import com.olam.warehouse.master.migration.MIGRATION_19_20
import com.olam.warehouse.master.migration.MIGRATION_1_2
import com.olam.warehouse.master.migration.MIGRATION_20_21
import com.olam.warehouse.master.migration.MIGRATION_21_22
import com.olam.warehouse.master.migration.MIGRATION_22_23
import com.olam.warehouse.master.migration.MIGRATION_23_24
import com.olam.warehouse.master.migration.MIGRATION_24_25
import com.olam.warehouse.master.migration.MIGRATION_25_26
import com.olam.warehouse.master.migration.MIGRATION_26_27
import com.olam.warehouse.master.migration.MIGRATION_27_28
import com.olam.warehouse.master.migration.MIGRATION_28_29
import com.olam.warehouse.master.migration.MIGRATION_29_30
import com.olam.warehouse.master.migration.MIGRATION_2_3
import com.olam.warehouse.master.migration.MIGRATION_30_31
import com.olam.warehouse.master.migration.MIGRATION_31_32
import com.olam.warehouse.master.migration.MIGRATION_32_33
import com.olam.warehouse.master.migration.MIGRATION_33_34
import com.olam.warehouse.master.migration.MIGRATION_34_35
import com.olam.warehouse.master.migration.MIGRATION_35_36
import com.olam.warehouse.master.migration.MIGRATION_36_37
import com.olam.warehouse.master.migration.MIGRATION_38_39
import com.olam.warehouse.master.migration.MIGRATION_38_40
import com.olam.warehouse.master.migration.MIGRATION_3_4
import com.olam.warehouse.master.migration.MIGRATION_40_41
import com.olam.warehouse.master.migration.MIGRATION_41_42
import com.olam.warehouse.master.migration.MIGRATION_4_5
import com.olam.warehouse.master.migration.MIGRATION_5_6
import com.olam.warehouse.master.migration.MIGRATION_6_7
import com.olam.warehouse.master.migration.MIGRATION_7_8
import com.olam.warehouse.master.migration.MIGRATION_8_9
import com.olam.warehouse.master.migration.MIGRATION_9_10
import com.olam.warehouse.master.user.model.VegaCoCoaThirdPartyMaterialDetail
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.dao.VegaDispatchDao
import com.olam.warehouse.master.vega.dao.VegaGateEntryDao
import com.olam.warehouse.master.vega.dao.VegaInventoryDao
import com.olam.warehouse.master.vega.dao.VegaMtntDao
import com.olam.warehouse.master.vega.dao.VegaNotificationConfigDao
import com.olam.warehouse.master.vega.dao.VegaOffloadingDao
import com.olam.warehouse.master.vega.dao.VegaProcessingDao
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.dao.VegaReceivingDao
import com.olam.warehouse.master.vega.dao.VegaStockReconDao
import com.olam.warehouse.master.vega.entity.ProcessingLotDetails
import com.olam.warehouse.master.vega.entity.TrackTraceFarmerModel
import com.olam.warehouse.master.vega.entity.TrackTraceFarmerTransDetails
import com.olam.warehouse.master.vega.entity.TrackTraceSourceLotDetails
import com.olam.warehouse.master.vega.entity.TrackTraceTransactionIdDetails
import com.olam.warehouse.master.vega.entity.VegaBcZoneMapping
import com.olam.warehouse.master.vega.entity.VegaBinDetails
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaCoffeePurchaseOrderMaterialModel
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaDispatchDelivery
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaDispatchTrucks
import com.olam.warehouse.master.vega.entity.VegaFeatureMaster
import com.olam.warehouse.master.vega.entity.VegaFgrnProcessingOrder
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vega.entity.VegaGateEntryDetails
import com.olam.warehouse.master.vega.entity.VegaGhanaProcessingOrder
import com.olam.warehouse.master.vega.entity.VegaGhanaProcessingOrderDetails
import com.olam.warehouse.master.vega.entity.VegaGhanaPurchaseOrderMaterialModel
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaLastSyncTime
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.entity.VegaMtntLineItem
import com.olam.warehouse.master.vega.entity.VegaOffloadingParameter
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.entity.VegaPlanRoute
import com.olam.warehouse.master.vega.entity.VegaProcessingCreatePoReq
import com.olam.warehouse.master.vega.entity.VegaProcessingRminBoms
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vega.entity.VegaPurchaseOrders
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaReceivingLineItem
import com.olam.warehouse.master.vega.entity.VegaReceivingMtn
import com.olam.warehouse.master.vega.entity.VegaReceivingMtnLots
import com.olam.warehouse.master.vega.entity.VegaReceivingWarehouse
import com.olam.warehouse.master.vega.entity.VegaStorageLocation
import com.olam.warehouse.master.vega.entity.VegaStorageLocationDetail
import com.olam.warehouse.master.vega.entity.VegaSupplyStorageLocation
import com.olam.warehouse.master.vega.entity.VegaTrackTraceFarmerData
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.entity.VegaWarehouse
import com.olam.warehouse.master.vega.entity.VegaWorkflowProcess
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonContainerSize
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonShippingLine
import com.olam.warehouse.master.vegacocoa.dao.VegaCoCoaOffloadDao
import com.olam.warehouse.master.vegacocoa.dao.VegaCoCoaQualityDao
import com.olam.warehouse.master.vegacocoa.dao.VegaCocoaDispatchDao
import com.olam.warehouse.master.vegacocoa.dao.VegaCocoaRMinDao
import com.olam.warehouse.master.vegacocoa.dao.VegaCocoaSalesDao
import com.olam.warehouse.master.vegacocoa.dao.VegaCocoaSweepingDao
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaOffloadingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaQualityWBDetail
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiveLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiving
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaStorageLocation
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchDelivery
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItemsGrades
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentLot
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentModel
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaOfflineStock
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeDispatchDao
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeExportSalesDao
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeGrnDao
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeInventoryDao
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeOffloadDao
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeQualityDao
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeRminDao
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeSalesDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesOrder
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorDispatchDao
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorGrnDao
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorOffloadingDao
import com.olam.warehouse.master.vegaecuador.entity.VegaCameroonOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchPurchaseOrders
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaghana.dao.VegaGhanaProcessingDao
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaGRNHistoryTransactions
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaMtnrHistoryTransactions
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaMtntHistoryTransactions
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaLotQualityDetails
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaMtnrQualityLot
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineFgrnData
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineFgrnProcessLotDetails
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminData
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminItems
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminLots
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminProcessLotDetails
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaQuality
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaQualityMtnBatch
import com.olam.warehouse.master.vegaghana.entity.VehicleDetails
import com.olam.warehouse.master.vegaindocoffee.entity.IndoExporSalesMaterialList
import com.olam.warehouse.master.vegaindocoffee.entity.VegaIndoCoffeeExportSalesOrder
import com.olam.warehouse.master.veganicaragua.dao.VegaNicInventoryDao
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaAdvanceDao
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaForwardPODao
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaGrnDao
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaInvoiceDao
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaMtntDao
import com.olam.warehouse.master.veganicaragua.entity.GrnDetails
import com.olam.warehouse.master.veganicaragua.entity.QualitativeParams
import com.olam.warehouse.master.veganicaragua.entity.VegaNicDispatchLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminData
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminItems
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminProcessLotDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceLineItemGrn
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceLineItems
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceTransactionDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaExchangeRate
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPODetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPOPriceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGRNInventoryDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnCharDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnPriceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaInvoiceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMtnt
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaPositionGradeMappings
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaPriceConfigDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaReconcilCashMovement
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.presentation.converters.Converters
import com.olam.warehouse.presentation.converters.EnumTypeConverter
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.ROOM_SCHEMA_VERSION
import com.olam.warehouse.presentation.utils.PreferenceHelper
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.io.IOException

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
@Database(
    entities = [VegaReceiving::class,
        VegaReceivingLineItem::class,
        VegaMaterial::class,
        VegaProcessingStage::class,
        VegaProcessingCreatePoReq::class,
        ProcessingLotDetails::class,
        VegaPackageMaterial::class,
        VegaQualitative::class,
        VegaQuality::class,
        VegaQualityParameter::class,
        VegaQualityWBDetails::class,
        VegaReceivingMtn::class,
        VegaReceivingMtnLots::class,
        VegaReceivingWarehouse::class,
        VegaStorageLocation::class,
        VegaVendor::class,
        VegaWarehouse::class,
        VegaCoffeeThirdPartyMaterialDetail::class,
        VegaCustomStLocation::class,
        VegaSupplyStorageLocation::class,
        VegaBinDetails::class,
        VegaOffloadingTrucks::class,
        VegaOffloadingParameter::class,
        VegaMtnt::class,
        VegaMtntLineItem::class,
        VegaDispatchTrucks::class,
        VegaDispatchLots::class,
        VegaDispatchDelivery::class,
        VegaPurchaseOrders::class,
        VegaProcessingRminBoms::class,
        VegaConfigDetails::class,
        VegaFgrnProcessingOrder::class,
        VegaGateEntry::class,
        VegaBcZoneMapping::class,
        VegaCoffeeReceiveLots::class,
        VegaCocoaDispatchWB::class,
        VegaCocoaDispatchDelivery::class,
        VegaCocoaDispatchLots::class,
        VegaCocoaOfflineStock::class,
        VegaCocoaPurchaseOrders::class,
        VegaCocoaNoWeighmentBagMaterial::class,
        VegaCocoaNoWeighmentModel::class,
        VegaCocoaNoWeighmentLot::class,
        VegaCocoaSweepingBagMaterial::class,
        VegaCocoaRminLots::class,
        VegaCocoaRminProcessing::class,
        VegaCocoaFgrnItems::class,
        VegaCocoaFgrnItemsGrades::class,
        VegaCocoaFgrnGradesMatrialWeights::class,
        VegaCocoaSalesWB::class,
        VegaCocoaSalesLots::class,
        VegaCocoaSalesBagMaterial::class,
        VegaCocoaMiscellaneous::class,
        VegaCoCoaReceiveLots::class,
        VegaCoCoaReceiving::class,
        VegaCoCoaStorageLocation::class,
        VegaCoCoaOffloadingBagMaterial::class,
        VegaCoCoaThirdPartyMaterialDetail::class,
        VegaEcuadorPurchaseOrder::class,
        VegaEcuadorDispatch::class,
        VegaGrnWeighBridgeId::class,
        VegaEcuadorOffloadingBagMaterial::class,
        VegaEcuadorDispatchLots::class,
        VegaEcuadorDispatchPurchaseOrders::class,
        VegaEcuadorDispatchStocks::class,
        VegaCoffeeRminLots::class,
        VegaCoffeeRminProcessing::class,
        VegaCoffeeFgrnItems::class,
        VegaCoffeeFgrnItemsGrades::class,
        VegaCoffeeFgrnGradesMatrialWeights::class,
        VegaCoffeePurchaseOrderMaterialModel::class,
        VegaCoffeeSalesOrder::class,
        VegaCoffeeSalesLots::class,
        VegaCoffeeSalesBagMaterial::class,
        VegaCoffeeExportSalesOrder::class,
        VegaCoffeeExportSalesContainer::class,
        VegaCoffeeExportSalesLots::class,
        VegaCoffeeReceiving::class,
        VegaCoffeeLot::class,
        VegaCoffeeOffloadingBagMaterial::class,
        VegaCoffeeThirdPartyRequestModel::class,
        VegaNicaraguaWeighmentBagMaterial::class,
        VegaNicaraguaPriceConfigDetails::class,
        VegaNicaraguaPositionGradeMappings::class,
        VegaNicaraguaGrnPriceDetails::class,
        VegaNicaraguaExchangeRate::class,
        VegaNicaraguaGrnCharDetails::class,
        VegaNicaraguaAdvanceLineItems::class,
        GrnDetails::class,
        QualitativeParams::class,
        VegaNicaraguaReconcilCashMovement::class,
        VegaNicaraguaInvoiceDetails::class,
        VegaNicaraguaAdvanceLineItemGrn::class,
        VegaNicaraguaGRNInventoryDetails::class,
        VegaNicaraguaMaterialQualitGrades::class,
        VegaNicaraguaForwardPOPriceDetails::class,
        VegaNicaraguaForwardPODetails::class,
        VegaNicaraguaAdvanceTransactionDetails::class,
        VegaNicaraguaAdvanceDetails::class,
        VegaNicaraguaMtnt::class,
        VegaNicDispatchLots::class,
        VegaLastSyncTime::class,
        VegaCoCoaQualityWBDetail::class,
        VegaCameroonOffloadingBagMaterial::class,
        VegaCameroonShippingLine::class,
        VegaCameroonContainerSize::class,
        VegaGhanaOfflineRminData::class,
        VegaGhanaOfflineRminProcessLotDetails::class,
        VegaGhanaOfflineRminLots::class,
        VegaGhanaOfflineRminItems::class,
        VegaGhanaProcessingOrder::class,
        VegaGhanaOfflineFgrnData::class,
        VegaGhanaOfflineFgrnProcessLotDetails::class,
        VegaGhanaProcessingOrderDetails::class,
        VegaGhanaCocoaDispatchLots::class,
        VegaGhanaPurchaseOrderMaterialModel::class,
        VegaGhanaQualityMtnBatch::class,
        VegaGhanaQuality::class,
        VegaGhanaMtnrQualityLot::class,
        VegaGhanaLotQualityDetails::class,
        VegaIndoCoffeeExportSalesOrder::class,
        VegaUomDetails::class,
        VegaPlanRoute::class,
        VegaStorageLocationDetail::class,
        VehicleDetails::class,
        VegaGateEntryDetails::class,
        VegaNicOfflineRminData::class,
        VegaNicOfflineRminProcessLotDetails::class,
        VegaNicOfflineRminLots::class,
        VegaNicOfflineRminItems::class,
        IndoExporSalesMaterialList::class,
        VegaGhanaCocoaMtnrHistoryTransactions::class,
        VegaGhanaCocoaGRNHistoryTransactions::class,
        VegaGhanaCocoaMtntHistoryTransactions::class,
        VegaWorkflowProcess::class,
        VegaFeatureMaster::class,
        VegaTrackTraceFarmerData::class,
        TrackTraceFarmerModel::class,
        TrackTraceSourceLotDetails::class,
        TrackTraceTransactionIdDetails::class,
        TrackTraceFarmerTransDetails::class
    ], version = ROOM_SCHEMA_VERSION, exportSchema = false
)
@TypeConverters(Converters::class, EnumTypeConverter::class)
abstract class VegaDatabase : RoomDatabase() {

    abstract fun vegaQualityDao(): VegaQualityDao
    abstract fun vegaReceivingDao(): VegaReceivingDao
    abstract fun vegaOffloadingDao(): VegaOffloadingDao
    abstract fun vegaMtntDao(): VegaMtntDao
    abstract fun vegaDispatchDao(): VegaDispatchDao
    abstract fun vegaProcessingDao(): VegaProcessingDao
    abstract fun vegaGateEntryDao(): VegaGateEntryDao
    abstract fun vegaInventoryDao(): VegaInventoryDao
    abstract fun vegaCocoaDispatchDao(): VegaCocoaDispatchDao
    abstract fun vegaCoCoaOffloadDao(): VegaCoCoaOffloadDao
    abstract fun vegaCocoaSweepingDao(): VegaCocoaSweepingDao
    abstract fun vegaCocoaRminDao(): VegaCocoaRMinDao
    abstract fun vegaCocoaSalesDao(): VegaCocoaSalesDao
    abstract fun vegaEcuadorOffloadingDao(): VegaEcuadorOffloadingDao
    abstract fun vegaEcuadorGrnDao(): VegaEcuadorGrnDao
    abstract fun vegaEcuadorDispatchDao(): VegaEcuadorDispatchDao
    abstract fun vegaCoffeeInventoryDao(): VegaCoffeeInventoryDao
    abstract fun vegaCoffeeRminDao(): VegaCoffeeRminDao
    abstract fun vegaCoffeeDispatchDao(): VegaCoffeeDispatchDao
    abstract fun vegaCoffeeQualityDoa(): VegaCoffeeQualityDao
    abstract fun vegaCoCoaQualityDoa(): VegaCoCoaQualityDao
    abstract fun vegaCoffeeSalesDao(): VegaCoffeeSalesDao
    abstract fun vegaCoffeeOffloadDao(): VegaCoffeeOffloadDao
    abstract fun vegaCoffeeExportSalesDao(): VegaCoffeeExportSalesDao
    abstract fun vegaNicaraguaGrnDao(): VegaNicaraguaGrnDao
    abstract fun vegaNicaraguaMtntDao(): VegaNicaraguaMtntDao
    abstract fun vegaNicaraguaInvoiceDao(): VegaNicaraguaInvoiceDao
    abstract fun vegaCoffeeGrnDao(): VegaCoffeeGrnDao
    abstract fun VegaNicaraguaForwardPODao(): VegaNicaraguaForwardPODao
    abstract fun vegaNicaraguaAdvanceDao(): VegaNicaraguaAdvanceDao
    abstract fun vegaNicInventoryDao(): VegaNicInventoryDao
    abstract fun vegaGhanaProcessingDao(): VegaGhanaProcessingDao
    abstract fun vegaDummyQualityDao(): VegaDummyQualityDao

    abstract fun vegaStockReconDao(): VegaStockReconDao

    abstract fun vegaTrackTraceDao(): TrackTraceDao

    abstract fun vegaNotificationConfigDao(): VegaNotificationConfigDao
    companion object {
        private lateinit var VEGA_DB_INSTANCE: VegaDatabase
        fun buildDatabase(context: Context): VegaDatabase {
            PreferenceHelper.save(Constants.DBVEGA, Constants.DVBGA)
            VEGA_DB_INSTANCE = getEncryptDB(context)
            val dbState = getDatabaseState(context.applicationContext,
                    Constants.VEGADATABASE)
            when (dbState) {
                SQLCipherUtils.State.UNENCRYPTED -> {
                    encryptDB(dbState, context)
                }
                else -> {}
            }
            return VEGA_DB_INSTANCE
        }

        fun getInstance(): VegaDatabase {
            return VEGA_DB_INSTANCE
        }

        private fun getNoEncryptDB(context: Context): VegaDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                VegaDatabase::class.java,
                Constants.VEGADATABASE
            )
                /* .addMigrations(
                     MIGRATION_1_2,
                     MIGRATION_2_3,
                     MIGRATION_3_4,
                     MIGRATION_4_5,
                     MIGRATION_5_6,
                     MIGRATION_6_7,
                     MIGRATION_7_8,
                     MIGRATION_8_9,
                     MIGRATION_9_10,
                     MIGRATION_10_11,
                     MIGRATION_11_12,
                     MIGRATION_12_13,
                     MIGRATION_13_14,
                     MIGRATION_14_15,
                     MIGRATION_15_16,
                     MIGRATION_16_17,
                     MIGRATION_17_18,
                     MIGRATION_18_19,
                     MIGRATION_19_20,
                     MIGRATION_20_21
                 )*/
                .fallbackToDestructiveMigration()
                .build()
        }

        private fun getEncryptDB(context: Context): VegaDatabase {
            val admin = PreferenceHelper.get(Constants.DBVEGA, "").toCharArray()
            val supportFactory = SupportFactory(SQLiteDatabase.getBytes(admin))
            return Room.databaseBuilder(
                context.applicationContext,
                VegaDatabase::class.java,
                Constants.VEGADATABASE
            )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                    MIGRATION_10_11,
                    MIGRATION_11_12,
                    MIGRATION_12_13,
                    MIGRATION_13_14,
                    MIGRATION_14_15,
                    MIGRATION_15_16,
                    MIGRATION_16_17,
                    MIGRATION_17_18,
                    MIGRATION_18_19,
                    MIGRATION_19_20,
                    MIGRATION_20_21,
                    MIGRATION_21_22,
                    MIGRATION_22_23,
                    MIGRATION_23_24,
                    MIGRATION_24_25,
                    MIGRATION_25_26,
                    MIGRATION_26_27,
                    MIGRATION_27_28,
                    MIGRATION_28_29,
                    MIGRATION_29_30,
                    MIGRATION_30_31,
                    MIGRATION_31_32,
                    MIGRATION_32_33,
                    MIGRATION_33_34,
                    MIGRATION_34_35,
                    MIGRATION_35_36,
                    MIGRATION_36_37,
                    MIGRATION_38_39,
                    MIGRATION_38_40,
                    MIGRATION_40_41,
                    MIGRATION_41_42
                )
//                .fallbackToDestructiveMigration()
                .openHelperFactory(supportFactory)
                .build()
        }

        private fun encryptDB(dbState: SQLCipherUtils.State, context: Context) {
            when (dbState) {
                SQLCipherUtils.State.UNENCRYPTED -> {
                    SQLiteDatabase.loadLibs(context.applicationContext)
                    val databaseManager =
                        Databasehelper.getInstance(context.applicationContext, Constants.DVBGA)
                    try {
                        databaseManager?.encryptDB()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                else -> {
                }
            }
        }
    }
}
