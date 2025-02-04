package com.olam.warehouse.master.vegaecuador.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.common.dao.BaseDao
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.master.vegaecuador.entity.VegaCameroonOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuaOffloadingWithLineItems
import com.olam.warehouse.presentation.enums.Status
import java.util.*

/**
 * Created by Keerthi Santhanam on 23/6/2020.
 */
@Dao
abstract class VegaEcuadorOffloadingDao : BaseDao<VegaReceiving>() {
    suspend fun saveOffloading(item: VegaReceiving) {
        insert(item)
    }
    @Query("SELECT * FROM VegaOffloadingTrucks WHERE isSyncStatus = 0 and isOfflineData = 0")
    abstract suspend fun getOffloadingTruckListDetails(): List<VegaOffloadingTrucks>


    fun save(item: List<VegaOffloadingTrucks>) {
        deleteVegaOffloadingTrucks()
        item.forEach {
            if (isWBExist(it.weighBridgeId).isNotEmpty()) return@forEach
            it.wbTempId = it.weighBridgeId
            it.supplierCode = it.supplierCode
            insertQualityWbDetail(it)
        }
    }
    @Transaction
    @Query("DELETE FROM VegaOffloadingTrucks WHERE isSyncStatus = 0")
    abstract fun deleteVegaOffloadingTrucks()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertQualityWbDetail(wbList: VegaOffloadingTrucks)

    @Query("SELECT * FROM VegaOffloadingTrucks WHERE weighBridgeId = :wbid and isOfflineData = 1")
    abstract fun isWBExist(wbid: String): List<VegaOffloadingTrucks>


    @Query("SELECT * FROM VegaReceiving WHERE isSynced = 0")
    abstract fun getReceiving(): LiveData<List<VegaReceiving>>

    //Master Data

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getProducts(): LiveData<List<VegaMaterial>>

    @Query("SELECT * FROM VegaStorageLocationDetail")
    abstract fun getMaterialStlocDetails(): List<VegaStorageLocationDetail>

    @Query("SELECT * FROM VegaStorageLocationDetail")
    abstract fun getStorageLocationDetail(): LiveData<List<VegaStorageLocationDetail>>

    @Query("SELECT * FROM VegaEcuadorDispatchStocks")
    abstract fun getStockListOffline(): LiveData<List<VegaEcuadorDispatchStocks>>

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getMaterialData(): List<VegaMaterial>

    @Query("SELECT * FROM VegaStorageLocation")
    abstract fun getStorageLocations(): LiveData<List<VegaStorageLocation>>

    @Query("SELECT * FROM VegaVendor")
    abstract fun getSuppliers(): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaConfigDetails where rolekey = :role")
    abstract fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

    @Query("SELECT * FROM VegaUomDetails")
    abstract fun getuomDetail(): LiveData<List<VegaUomDetails>>

    @Query("SELECT * FROM VegaUomDetails")
    abstract fun getuomDetailSync(): List<VegaUomDetails>

    @Query("SELECT * FROM VegaPackageMaterial")
    abstract fun getMaterials(): LiveData<List<VegaPackageMaterial>>

    @Query("SELECT * FROM VegaCocoaMiscellaneous")
    abstract fun getMiscellaneous(): LiveData<List<VegaCocoaMiscellaneous>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetails(material: VegaEcuadorOffloadingBagMaterial)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetailsNew(bagMaterial: VegaCoffeeOffloadingBagMaterial)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveSelectedBatchBagDetails(material: List<VegaEcuadorOffloadingBagMaterial>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveCameroonSaveBagDetails(material: List<VegaCameroonOffloadingBagMaterial>)

    @Query("DELETE FROM VegaEcuadorOffloadingBagMaterial where id = :id and tmpWbId =:tmpWbId")
    abstract fun deleteBagDetails(id: Int, tmpWbId: String)

    @Query("DELETE FROM VegaEcuadorOffloadingBagMaterial where id = :id and wbId =:wbId")
    abstract fun deleteSelectedBagDetails(id: Int, wbId: String)

    @Query("DELETE FROM VegaCameroonOffloadingBagMaterial where batchNumber =:batchId")
    abstract fun deleteCameroonSavedBagDetails(batchId: String)

    @Query("DELETE FROM VegaEcuadorOffloadingBagMaterial where batchNumber = :batchNumber and wbId =:wbId")
    abstract fun deleteBagDetails(batchNumber: String, wbId: String)

    @Query("DELETE FROM VegaEcuadorOffloadingBagMaterial")
    abstract fun deleteBagDetails()

    @Query("SELECT * FROM VegaEcuadorOffloadingBagMaterial where materialCode =:materialCode and supplierCode =:supplierCode and procureType = :type and tmpWbId =:tmpWbId")
    abstract fun getBagItems(
        materialCode: String?,
        supplierCode: String,
        type: String,
        tmpWbId: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>>

    @Query("SELECT * FROM VegaEcuadorOffloadingBagMaterial where wbId =:tmpWbId")
    abstract fun getWBBagItems(
        tmpWbId: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>>

    @Query("SELECT * FROM VegaCameroonOffloadingBagMaterial where batchNumber =:batchNumber")
    abstract fun getBagItem(
        batchNumber: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>>

    @Query("SELECT * FROM VegaEcuadorOffloadingBagMaterial where materialCode =:materialCode and supplierCode =:supplierCode and procureType = :type and purcheseOrderNo = :poId and tmpWbId =:tmpWbId")
    abstract fun getBagItems(
        materialCode: String?,
        supplierCode: String,
        type: String,
        poId: String,
        tmpWbId: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>>

    @Query("SELECT * FROM VegaCameroonOffloadingBagMaterial where materialCode =:materialCode and supplierCode =:supplierCode and procureType = :type and purcheseOrderNo = :poId ")
    abstract fun getSavedBagItems(
        materialCode: String?,
        supplierCode: String,
        type: String,
        poId: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>>

    @Query("SELECT * FROM VegaCameroonOffloadingBagMaterial where plant =:plantId")
    abstract fun getSavedBagItems(plantId: String): LiveData<List<VegaEcuadorOffloadingBagMaterial>>

    @Query("DELETE FROM VegaEcuadorOffloadingBagMaterial")
    abstract fun clearBagDetails()

    @Query("DELETE FROM VegaCoffeeOffloadingBagMaterial where id = :id")
    abstract fun deleteBagDetailsNew(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveReceivingLineItems(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>)

    @Query("SELECT * FROM VegaReceiving")
    abstract fun getOffloadingWithLineItem(): LiveData<List<VegaEcuaOffloadingWithLineItems>>

    @Query("DELETE FROM VegaReceiving")
    abstract fun deleteOffloadingItem()

    @Query("SELECT * FROM VegaReceiving where isSynced = 0 and isOffline = 1")
    abstract fun getOffloadingWithLineItemCount(): LiveData<List<VegaEcuaOffloadingWithLineItems>>

    @Query("SELECT * FROM VegaReceiving WHERE tmpWbId = :wbid")
    abstract fun getOffloadingWithLineItem(wbid: String): VegaEcuaOffloadingWithLineItems

    @Query("DELETE FROM VegaReceiving where tmpWbId = :tmpWbId")
    abstract fun deleteOffloadingItem(tmpWbId: String)

    @Query("DELETE FROM VegaEcuadorOffloadingBagMaterial where tmpWbId = :tmpWbId")
    abstract fun deleteBagItem(tmpWbId: String)

    @Query("SELECT * FROM VegaEcuadorPurchaseOrder")
    abstract fun getPOListLocal(): LiveData<List<VegaEcuadorPurchaseOrder>>

    @Query("DELETE FROM VegaGrnWeighBridgeId WHERE weighBridgeId =:tmpWbId")
    abstract fun deleteGrnData(tmpWbId: String)

    @Query("DELETE FROM VegaQualityWBDetails where weighBridgeId = :wbid")
    abstract fun updateDeletedWBItem(wbid: String)

    @Query("DELETE FROM VegaQuality where wbid = :wbid")
    abstract fun deleteVegaOfflineParams(wbid: String)

    fun updateDeletedItem(wbid: String) {
        updateDeletedWBItem(wbid)
        deleteVegaOfflineParams(wbid)
    }

    @Query("UPDATE VegaGrnWeighBridgeId SET weighBridgeId = :wbid1 WHERE wbTempId = :tempId")
    abstract fun updateTempIdToWbidGrn(wbid1: String, tempId: String)

    @Query("UPDATE VegaQualityWBDetails SET weighBridgeId = :wbid1, isNotWBID = 0 WHERE wbTempId = :tempId")
    abstract fun updateTempToWBItem(wbid1: String, tempId: String)

    @Query("UPDATE VegaQuality SET wbid = :wbid1 WHERE wbTempId = :tempId")
    abstract fun updateTempToWBQuality(wbid1: String, tempId: String)

    @Query("SELECT * FROM VegaCocoaMiscellaneous where  roleKey = :role")
    abstract fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>>

    fun updateTempIdToWbid(tempId: String, wbid: String) {
        updateTempIdToWbidGrn(wbid, tempId)
        updateTempToWBQuality(wbid, tempId)
        updateTempToWBItem(wbid, tempId)
        deleteOffloadingItem(tempId)
        deleteBagItem(tempId)
    }



    /*****************************MTNR************************************************/



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveReceivingLineItems(lineItems: List<VegaReceivingLineItem>)

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


    @Query("SELECT * FROM VegaSupplyStorageLocation")
    abstract fun getLocations(): LiveData<List<VegaSupplyStorageLocation>>

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>


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
    abstract suspend fun insertLots(batchDetails: List<VegaCoffeeReceiveLots>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertBcZone(bcList: List<VegaBcZoneMapping>)

    suspend fun saveWarehouseAndMtns(it: VegaCoffeeReceivingMtnWrapper) {
        insertLots(it.batchDetails)
        insertWarehouses(it.stockSupplyingPlants)
        insertMtns(it.mtns)
        insertStorageLocation(it.storageLocationLst)
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
    abstract suspend fun insertThirdPartyMaterial(list: List<VegaCoffeeThirdPartyMaterialDetail>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertVendor(storageLocation: List<VegaVendor>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertWarehouse(storageLocation: List<VegaWarehouse>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertProcessingStage(processingStage: List<VegaProcessingStage>)

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
    abstract fun isWBExistMtnr(wbid: String): List<VegaReceiving>

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
    abstract fun insertConfigDetail(configDetails: VegaConfigDetails)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun savePurcheseOrder(preparePurcheseOrder: ArrayList<VegaEcuadorPurchaseOrder>)

    @Query("SELECT * FROM VegaCocoaSweepingBagMaterial where isSyncStatus = 0")
    abstract fun getBagItems(): LiveData<List<VegaCocoaSweepingBagMaterial>>

    @Query("SELECT * FROM VegaCoffeeOffloadingBagMaterial where isSyncStatus = 0")
    abstract fun getBagItemsNew(): LiveData<List<VegaCoffeeOffloadingBagMaterial>>

    @Query("SELECT * FROM VegaCoffeeOffloadingBagMaterial where batchNumber = :batchNumber and mtnNumber =:mtnNumber and isSyncStatus = 0")
    abstract fun getBagItemsNew(
        batchNumber: String,
        mtnNumber: String
    ): LiveData<List<VegaCoffeeOffloadingBagMaterial>>


    @Query("SELECT * FROM VegaCocoaSweepingBagMaterial where batchNumber = :batchNumber and isSyncStatus = 0")
    abstract fun getBagItems(batchNumber: String): LiveData<List<VegaCocoaSweepingBagMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial)

    @Query("DELETE FROM VegaCocoaSweepingBagMaterial where id = :id")
    abstract fun deleteBagDetails(id: Int)

    @Query("SELECT * FROM VegaCoffeeReceiving where delivery = :deliveryNumber")
    abstract fun getOBDDetails(deliveryNumber: String): LiveData<VegaCoffeeReceivingMtnrWithLots>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveMtnrReceiving(vegaCoffeeReceivingData: VegaCoffeeReceiving)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveMtnrReceivingLot(lot: VegaCoffeeReceiveLots)

    @Query("DELETE FROM VegaCoffeeReceiving where delivery = :mtnNumber")
    abstract fun deleteReceivingItem(mtnNumber: String)

    @Query("DELETE FROM VegaCoffeeReceiveLots where delivery = :mtnNumber")
    abstract fun deleteReceivingLotItem(mtnNumber: String)

    @Query("SELECT * FROM VegaReceivingMtn")
    abstract fun getMTNRs(): LiveData<List<VegaReceivingMtn>>

    @Query("SELECT * FROM VegaReceivingMtnLots")
    abstract fun getOfflineLots(): LiveData<List<VegaReceivingMtnLots>>

    @Query("SELECT * FROM VegaSupplyStorageLocation")
    abstract fun getStorageLoc(): LiveData<List<VegaSupplyStorageLocation>>

    @Query("SELECT * FROM VegaCoffeeReceiving")
    abstract fun getTransactions(): LiveData<List<VegaCoffeeReceivingMtnrWithLots>>

    @Transaction
    @Query("SELECT * FROM VegaCoffeeReceiving WHERE tempWBId = :tempGrnNumber")
    abstract fun getMtnrReceivingWithLineItemWbid(tempGrnNumber: String): VegaCoffeeReceivingMtnrWithLots

    @Query("UPDATE VegaCoffeeReceiving SET isSynced = :sync,syncStatusMsg = :message,status = :status,syncId = :id WHERE tempWBId = :tempId and isOnlineData= :offline")
    abstract fun updateMtnrOfflineResponse(
        sync: Boolean,
        message: String,
        status: Status,
        tempId: String,
        offline: Boolean,
        id: String
    )

    @Query("DELETE FROM VegaCoffeeReceiving where delivery = :mtnNumber")
    abstract fun deleteMtnrReceivingItem(mtnNumber: String)

    @Query("DELETE FROM VegaCoffeeReceiveLots where mtnNumber = :mtnNumber")
    abstract fun deleteMtnrReceivingLotItem(mtnNumber: String)

    @Query("DELETE FROM VegaCoffeeOffloadingBagMaterial where mtnNumber = :mtnNumber")
    abstract fun deleteMtnrReceivingLotItemWithBagItems(mtnNumber: String)

    @Query("DELETE FROM VegaCoffeeReceiving ")
    abstract fun deleteSyncedMtnrs()

    @Query("UPDATE VegaCoffeeReceiving SET tempWBId = :tempId WHERE syncId = :id")
    abstract fun updateWBId(tempId: String, id: String)

    @Query("UPDATE VegaReceivingMtn SET isSynced = :tag WHERE  mtnNumber = :mtnNumber")
    abstract fun updateOBD(mtnNumber: String, tag: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveMtnrQualityList(data: VegaQualityWBDetails)

    @Query("DELETE FROM VegaQualityWBDetails where weighBridgeId = :wbId")
    abstract fun deleteMtnrQuality(wbId: String)

    @Query("UPDATE VegaQualityWBDetails SET weighBridgeId = :wbId WHERE wbTempId = :tempId")
    abstract fun updateMtnrQuality(wbId: String, tempId: String)
/*
    @Query("SELECT * FROM VegaCameroonMultiPlantDetails")
    abstract fun getMultiPlantList(): LiveData<List<Plant>>*/
}
