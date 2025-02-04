package com.olam.warehouse.master

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.olam.warehouse.master.SQLCipherUtils.getDatabaseState
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.migration.*
import com.olam.warehouse.master.user.model.VegaCoCoaThirdPartyMaterialDetail
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.dao.*
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonContainerSize
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonShippingLine
import com.olam.warehouse.master.vegacocoa.dao.*
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.vegacoffee.dao.*
import com.olam.warehouse.master.vegacoffee.entity.*
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorDispatchDao
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorGrnDao
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorOffloadingDao
import com.olam.warehouse.master.vegaecuador.entity.*
import com.olam.warehouse.master.vegaghana.dao.VegaGhanaProcessingDao
import com.olam.warehouse.master.vegaghana.entity.*
import com.olam.warehouse.master.vegaindocoffee.entity.IndoExporSalesMaterialList
import com.olam.warehouse.master.vegaindocoffee.entity.VegaIndoCoffeeExportSalesOrder
import com.olam.warehouse.master.veganicaragua.dao.*
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.presentation.converters.Converters
import com.olam.warehouse.presentation.converters.EnumTypeConverter
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.FIRST_TIME_OPENED
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
        IndoExporSalesMaterialList::class
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
    companion object {
        private lateinit var VEGA_DB_INSTANCE: VegaDatabase
        fun buildDatabase(context: Context): VegaDatabase {
            PreferenceHelper.save(Constants.DBVEGA, Constants.DVBGA)
            val dbState = getDatabaseState(context.applicationContext,
                    Constants.VEGADATABASE)
            when (dbState) {
                SQLCipherUtils.State.UNENCRYPTED -> {
                    when (PreferenceHelper.get(FIRST_TIME_OPENED, 0)) {
                        0 -> {
                            //VEGA_DB_INSTANCE = getNoEncryptDB(context)
                            VEGA_DB_INSTANCE = getEncryptDB(context)
                            PreferenceHelper.save(
                                FIRST_TIME_OPENED,
                                PreferenceHelper.get(FIRST_TIME_OPENED, 0) + 1
                            )
                        }
                        1 -> {
//                            encryptDB(dbState, context)
                            VEGA_DB_INSTANCE = getEncryptDB(context)
                            PreferenceHelper.save(
                                FIRST_TIME_OPENED,
                                PreferenceHelper.get(FIRST_TIME_OPENED, 0) + 1
                            )
                        }
                    }
                }
                else -> {
                    VEGA_DB_INSTANCE = getEncryptDB(context)
                }
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
                    MIGRATION_24_25
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
                        Databasehelper.getInstance(context.applicationContext, "vegax123")
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
