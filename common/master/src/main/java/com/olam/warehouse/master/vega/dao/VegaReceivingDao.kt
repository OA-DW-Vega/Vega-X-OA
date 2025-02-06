package com.olam.warehouse.master.vega.dao

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.common.dao.BaseDao
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.*
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonContainerSize
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonShippingLine
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaLotQualityDetails
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaQualityMtnBatch
import com.olam.warehouse.master.vegaghana.entity.VehicleDetails
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.presentation.enums.Status
import java.util.*

/**
 * Created by Baskaran Kannan on 1/16/2020.
 */
@Dao
abstract class VegaReceivingDao : BaseDao<VegaReceiving>() {

    suspend fun save(item: VegaReceiving) {
        insert(item)
    }

    @Query("SELECT * FROM VegaReceiving WHERE isSynced = 0")
    abstract fun getReceiving(): LiveData<List<VegaReceiving>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveReceivingLineItems(lineItems: List<VegaReceivingLineItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveCoffeeTruckOutBagList(lineItems: List<VegaCoffeeOffloadingBagMaterial>)

    @Transaction
    @Query("SELECT * FROM VegaReceiving WHERE weighBridgeId = :weighBridgeId and isSynced = 0")
    abstract fun getTruckOutInfo(weighBridgeId: String): LiveData<VegaCoffeeTruckOutReceivingWithBags>

    @Transaction
    @Query("SELECT * FROM VegaReceiving WHERE isSynced=0")
    abstract fun getReceivingWithLineItem(): LiveData<List<VegaReceivingWithLineItems>>

    @Transaction
    @Query("SELECT * FROM VegaReceiving WHERE tmpWbId = :wbid")
    abstract fun getReceivingWithLineItemWbid(wbid: String): VegaReceivingWithLineItems

    @Transaction
    @Query("SELECT * FROM VegaReceiving")
    abstract fun getReceivingWithLineItemAll(): LiveData<List<VegaReceivingWithLineItems>>

    fun deleteItemReceiving(wbid: String) {
        deleteOfflineReceiving(wbid)
        deleteOfflineReceivingLineItem(wbid)
    }

    @Transaction
    @Query("DELETE FROM VegaReceiving where tmpWbId = :wbid")
    abstract fun deleteOfflineReceiving(wbid: String)

    @Transaction
    @Query("DELETE FROM VegaReceivingLineItem where tmpWbId = :wbid")
    abstract fun deleteOfflineReceivingLineItem(wbid: String)

    //Master Data

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getProducts(): LiveData<List<VegaMaterial>>

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getProductsAll(): List<VegaMaterial>

    @Query("SELECT * FROM VegaSupplyStorageLocation")
    abstract fun getLocations(): LiveData<List<VegaSupplyStorageLocation>>

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

    @Query("SELECT * FROM VegaVendor")
    abstract fun getSuppliers(): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaPlanRoute")
    abstract fun getPlanRoute(): LiveData<List<VegaPlanRoute>>

    @Query("SELECT * FROM VegaStorageLocationDetail")
    abstract fun getMaterialStlocDetails(): LiveData<List<VegaStorageLocationDetail>>

    @Query("SELECT * FROM VegaCameroonContainerSize")
    abstract fun getContainerSizeList(): LiveData<List<VegaCameroonContainerSize>>

    @Query("SELECT * FROM VegaCameroonShippingLine")
    abstract fun getShippingLineList(): LiveData<List<VegaCameroonShippingLine>>

    @Query("SELECT * FROM VegaPackageMaterial")
    abstract fun getMaterials(): LiveData<List<VegaPackageMaterial>>

    @Query("SELECT * FROM VegaReceivingWarehouse")
    abstract suspend fun getWarehouse(): List<VegaReceivingWarehouse>

    @Query("SELECT * FROM VegaReceivingWarehouse")
    abstract fun getWarehouses(): LiveData<List<VegaReceivingWarehouse>>

    @Transaction
    @Query("SELECT * FROM VegaSupplyStorageLocation WHERE storageLocationCode=:whId")
    abstract fun getWarehousesWithMtns(whId: String): LiveData<VegaReceivingWarehouseWithMtns>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertMtns(mtns: List<VegaReceivingMtn>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertWarehouses(stockSupplyingPlants: List<VegaReceivingWarehouse>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertCustomStLocation(prepareVegaCustomStLocation: List<VegaCustomStLocation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertBinDetails(data: VegaBinDetails)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertLots(batchDetails: List<VegaReceivingMtnLots>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertProcessOrders(processOrder: List<VegaGhanaProcessingOrder>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertProcessOrder(processOrder: VegaGhanaProcessingOrder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertProcessOrdersDetails(processOrderDetails: List<VegaGhanaProcessingOrderDetails>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertBcZone(bcList: List<VegaBcZoneMapping>)

    suspend fun saveWarehouseAndMtns(it: VegaReceivingMtnWrapper) {
        insertLots(it.batchDetails)
        insertWarehouses(it.stockSupplyingPlants)
        insertMtns(it.mtns)
        insertStorageLocation(it.storageLocationLst)
    }

    suspend fun saveProcessOrders(it: VegaGhanaProcessOrder) {
        it.vegaProcessOrderList.forEach { item ->
            item.materialType = "OP"
        }
        insertProcessOrders(it.vegaProcessOrderList)
        insertProcessOrdersDetails(it.vegaProcessOrderDetailsList)
        it.vegaProcessOrderList.forEach { item ->
            item.rmin?.forEach { item1 ->
                item.materialCode = item1.materialCode.toString()
                item.materialName = item1.materialName.toString()
                item.unitsOfMeasure = item1.unitsOfMeasure.toString()
                item.materialType = "IP"
                insertProcessOrder(item)
            }
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertStorageLocation(storageLocation: List<VegaSupplyStorageLocation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertBagType(packageMaterial: List<VegaPackageMaterial>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertLocation(storageLocation: List<VegaStorageLocation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMaterial(storageLocation: List<VegaMaterial>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertUomDetails(uomDetail: VegaUomDetails)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertThirdPartyMaterial(list: List<VegaCoffeeThirdPartyMaterialDetail>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertVendor(storageLocation: List<VegaVendor>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertVehicleDetails(storageLocation: List<VehicleDetails>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPlantRoute(planDetails: List<VegaPlanRoute>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMaterialStlocDetails(planDetails: List<VegaStorageLocationDetail>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertWarehouse(storageLocation: List<VegaWarehouse>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertProcessingStage(processingStage: List<VegaProcessingStage>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertQualitativeParams(processingStage: List<QualitativeParams>)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMaterialQualityGrades(processingStage: List<VegaNicaraguaMaterialQualitGrades>)


/*
    @Query("SELECT * FROM VegaTransactionDetail WHERE lotTransactionId=:id and isMapped = 0")
    abstract fun getTransactionDetailOffline(id: String): LiveData<VegaTransactionDetail>

    @Query("UPDATE VegaTransactionDetail SET isMapped = 0 WHERE lotTransactionId=:lotTransactionId")
    abstract fun updateTransactionDetails(lotTransactionId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTransactionDetail(transItem: VegaTransactionDetail)

    @Query("SELECT * FROM VegaTransactionDetail WHERE lotTransactionId = :transId and isMapped = 1")
    abstract fun isTarnsactionIdExist(transId: String): List<VegaTransactionDetail>*/

    @Query("UPDATE VegaReceiving SET syncStatusMsg = :msg, status =:syncError   WHERE tmpWbId=:tmpWbId")
    abstract fun updateReceivingFailMsg(msg: String, tmpWbId: String, syncError: Status)

    @Query("UPDATE VegaReceivingLineItem SET syncStatusMsg = :msg, status =:syncError   WHERE tmpWbId=:tmpWbId")
    abstract fun updateReceivingLineItemFailMsg(msg: String, tmpWbId: String, syncError: Status)

    fun saveReceiving(lineItems: List<VegaReceiving>, directionout: String) {
        deleteReceiving()
        lineItems.forEach { it.truckDirection = directionout }
        saveReceivingItemList(lineItems)
    }

    @Transaction
    @Query("DELETE FROM VegaReceiving WHERE isSynced = 0")
    abstract fun deleteReceiving()

    @Query("SELECT * FROM VegaReceiving WHERE weighBridgeId = :wbid and isSynced = 1")
    abstract fun isWBExist(wbid: String): List<VegaReceiving>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveReceivingItemList(lineItems: List<VegaReceiving>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveReceivingItem(lineItems: VegaReceiving)

    @Query("SELECT * FROM VegaReceiving WHERE status = :syncPending and direction !=:dir")
    abstract suspend fun getWeighBridgeDetailOnline(syncPending: Status, dir: String): List<VegaReceiving>

    @Query("SELECT * FROM VegaReceiving")
    abstract fun getReceivingItem(): LiveData<List<VegaReceiving>>

    @Query("SELECT * FROM VegaCoffeeThirdPartyMaterialDetail")
    abstract fun getCoffeeThirdPartyMaterials(): LiveData<List<VegaCoffeeThirdPartyMaterialDetail>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertConfigDetails(configDetails: List<VegaConfigDetails>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertMiscellaneousDetails(configDetails: List<VegaCocoaMiscellaneous>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertContainerSizeDetails(containerList: List<VegaCameroonContainerSize>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertShippingLineList(shippingLineList: List<VegaCameroonShippingLine>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertConfigDetail(configDetails: VegaConfigDetails)

    @Query("DELETE FROM VegaConfigDetails")
    abstract fun deleteConfigItems()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun savePurcheseOrder(preparePurcheseOrder: ArrayList<VegaEcuadorPurchaseOrder>)

    @Transaction
    @Query("DELETE FROM VegaEcuadorPurchaseOrder")
    abstract fun deletePurcheseOrder()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPriceConfigDetails(priceConfigDetails: List<VegaNicaraguaPriceConfigDetails>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPositionGradeMappings(positionGradeMappings: List<VegaNicaraguaPositionGradeMappings>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveGrnPriceDetails(item: List<VegaNicaraguaGrnPriceDetails>)

    @Transaction
    @Query("DELETE FROM VegaNicaraguaGrnPriceDetails")
    abstract fun deleteGrnPriceDetails()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertExchangeRate(exchangeRate: VegaNicaraguaExchangeRate)

    @Transaction
    @Query("DELETE FROM VegaNicaraguaExchangeRate")
    abstract fun deleteExchangeRate()

    @Query("SELECT * FROM VegaReceiving where status = :syncStatus")
    abstract fun getGrnItem(syncStatus: Status): LiveData<List<VegaReceiving>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveGrnDetails(grnDetails: List<GrnDetails>)

    @NonNull
    @Query("DELETE FROM GrnDetails")
    abstract fun deleteGrnDetails()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveLastSyncTime(vegaLastSyncTime: VegaLastSyncTime)

    @Query("SELECT * FROM VegaLastSyncTime")
    abstract fun getLastSyncTime(): LiveData<VegaLastSyncTime>

    @Query("DELETE FROM VegaPackageMaterial")
    abstract fun clearPackageMaterials()

    @Query("DELETE FROM VegaStorageLocation")
    abstract fun clearStorageLocations()

    @Query("DELETE FROM VegaMaterial")
    abstract fun clearMaterials()

    @Query("DELETE FROM VegaVendor")
    abstract fun clearVendors()

    @Query("DELETE FROM VegaPlanRoute")
    abstract fun clearPlanRoutes()

    @Query("DELETE FROM VegaStorageLocationDetail")
    abstract fun clearMaterialStlocDetails()

    @Query("DELETE FROM VegaUomDetails")
    abstract fun clearUOMDetails()

    @Query("DELETE FROM VegaWarehouse")
    abstract fun clearWarehouses()

    @Query("DELETE FROM VegaProcessingStage")
    abstract fun clearProcessingStages()

    @Query("DELETE FROM QualitativeParams")
    abstract fun clearQualitativeParams()

    @Query("DELETE FROM VegaNicaraguaMaterialQualitGrades")
    abstract fun clearMaterialQualitGrades()

    @Query("DELETE FROM VegaCoffeeThirdPartyMaterialDetail")
    abstract fun clearCoffeeThirdPartyMaterialDetails()

    @Query("DELETE FROM VegaCustomStLocation")
    abstract fun clearCustomStLocations()

    @Query("DELETE FROM VegaBcZoneMapping")
    abstract fun clearBcZoneMappings()

    @Query("DELETE FROM VegaCocoaMiscellaneous")
    abstract fun clearCocoaMiscellaneous()

    @Query("DELETE FROM VegaNicaraguaPriceConfigDetails")
    abstract fun clearPriceConfigDetails()

    @Query("DELETE FROM VegaNicaraguaPositionGradeMappings")
    abstract fun clearPositionGradeMappings()

    @Query("DELETE FROM VegaLastSyncTime")
    abstract fun clearLastSyncTime()

    //Clear Master Data before Inserting
    fun clearMasterData() {
        clearPackageMaterials()
        clearStorageLocations()
        clearMaterials()
        clearVendors()
        clearPlanRoutes()
        clearMaterialStlocDetails()
        clearUOMDetails()
        clearWarehouses()
        deleteConfigItems()
        clearProcessingStages()
        clearQualitativeParams()
        clearMaterialQualitGrades()
        clearCoffeeThirdPartyMaterialDetails()
        clearCustomStLocations()
        clearBcZoneMappings()
        clearCocoaMiscellaneous()
        clearPriceConfigDetails()
        clearPositionGradeMappings()
        clearLastSyncTime()
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveTruckInData(receive: VegaReceiving)

    @Query("SELECT * FROM VegaReceiving WHERE commonPrimaryId =:commonId and isSynced = 0")
    abstract fun getReceivingData(commonId: String): LiveData<VegaReceiving>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveMtnBatchNumbers(item: List<VegaGhanaQualityMtnBatch>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveLotQualityDetails(item: List<VegaGhanaLotQualityDetails>)

    @Transaction
    @Query("SELECT * FROM VegaQuality WHERE materialCode = :materialId and wbid = :wbId")
    abstract fun getQualityParameterWithData(materialId: String, wbId: String?): List<VegaQualityWithQualitative>

    @Transaction
    @Query("SELECT * FROM VegaQualityParameter WHERE materialCode = :materialId")
    abstract fun getQualityParameter(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveWorkflowProcess(items: List<VegaWorkflowProcess>)

    @Transaction
    @Query("SELECT * FROM VegaWorkflowProcess")
    abstract fun getNavigationFlow(): List<VegaWorkflowProcess>

    @Query("SELECT * FROM VegaVendor where werks =:selectedPlantId")
    abstract fun getSuppliersPlant(selectedPlantId: String): LiveData<List<VegaVendor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertFeatureMaster(it: List<VegaFeatureMaster>)

    @Query("SELECT * FROM VegaFeatureMaster WHERE moduleName = :module")
    abstract fun getTTFeatureMaster(module: String): LiveData<List<VegaFeatureMaster>>



}
