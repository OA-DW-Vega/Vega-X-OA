package com.olam.warehouse.master.vegacoffee.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.common.dao.BaseDao
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.*
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaindocoffee.entity.IndoExporSalesMaterialList
import com.olam.warehouse.master.vegaindocoffee.entity.VegaIndoCoffeeExportSalesOrder
import com.olam.warehouse.master.vegaindocoffee.model.VegaIndoCoffeeReceivingItemWithBags
import com.olam.warehouse.master.vegaindocoffee.model.VegaIndoCoffeeReceivingMtnrWithLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.presentation.enums.Status
import java.util.*

@Dao
abstract class VegaCoffeeOffloadDao : BaseDao<VegaReceiving>() {

    suspend fun save(item: VegaReceiving) {
        insert(item)
    }

    @Query("SELECT * FROM VegaReceiving WHERE isSynced = 0")
    abstract fun getReceiving(): LiveData<List<VegaReceiving>>

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

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getProducts(): LiveData<List<VegaMaterial>>

    @Query("SELECT * FROM VegaSupplyStorageLocation")
    abstract fun getLocations(): LiveData<List<VegaSupplyStorageLocation>>

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

    @Query("SELECT * FROM VegaVendor")
    abstract fun getSuppliers(): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaPackageMaterial")
    abstract fun getMaterials(): LiveData<List<VegaPackageMaterial>>

    @Query("SELECT * FROM VegaReceivingWarehouse")
    abstract suspend fun getWarehouse(): List<VegaReceivingWarehouse>

    @Query("SELECT * FROM VegaReceivingWarehouse")
    abstract fun getWarehouses(): LiveData<List<VegaReceivingWarehouse>>

    @Query("SELECT * FROM VegaReceivingMtn")
    abstract fun getMTNRs(): LiveData<List<VegaReceivingMtn>>

    @Query("SELECT * FROM VegaReceivingMtnLots")
    abstract fun getOfflineLots(): LiveData<List<VegaReceivingMtnLots>>

    @Query("SELECT * FROM VegaSupplyStorageLocation")
    abstract fun getStorageLoc(): LiveData<List<VegaSupplyStorageLocation>>

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
    abstract fun insertConfigDetail(configDetails: VegaConfigDetails)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun savePurcheseOrder(preparePurcheseOrder: ArrayList<VegaEcuadorPurchaseOrder>)

    @Query("SELECT * FROM VegaCoffeeOffloadingBagMaterial where isSyncStatus = 0")
    abstract fun getBagItems(): LiveData<List<VegaCoffeeOffloadingBagMaterial>>

    @Query("SELECT * FROM VegaCoffeeOffloadingBagMaterial where batchNumber = :batchNumber and mtnNumber =:mtnNumber and isSyncStatus = 0")
    abstract fun getBagItems(batchNumber: String, mtnNumber: String): LiveData<List<VegaCoffeeOffloadingBagMaterial>>

    @Query("SELECT * FROM VegaCoffeeOffloadingBagMaterial where tempWBId = :tmpWbId and isSyncStatus = 0")
    abstract fun getBagItems(tmpWbId: String): LiveData<List<VegaCoffeeOffloadingBagMaterial>>

    @Query("SELECT * FROM VegaCoffeeOffloadingBagMaterial where tempWBId = :tmpWbId and batchNumber = :batchNumber")
    abstract fun getLotBagItems(tmpWbId: String, batchNumber: String): LiveData<List<VegaCoffeeOffloadingBagMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetails(bagMaterial: VegaCoffeeOffloadingBagMaterial)

    @Query("DELETE FROM VegaCoffeeOffloadingBagMaterial where id = :id")
    abstract fun deleteBagDetails(id: Int)

    @Query("SELECT * FROM VegaCoffeeReceiving where delivery = :deliveryNumber")
    abstract fun getOBDDetails(deliveryNumber: String): LiveData<VegaCoffeeReceivingMtnrWithLots>

    @Query("SELECT * FROM VegaCoffeeReceiving where tempWBId = :tmpWbId")
    abstract fun getOBDDetailsIndo(tmpWbId: String): LiveData<VegaIndoCoffeeReceivingMtnrWithLots>

    @Query("SELECT * FROM VegaCoffeeReceiving where tempWBId = :tmpWbId")
    abstract fun getOBDDetailsIndoOffline(tmpWbId: String): VegaIndoCoffeeReceivingMtnrWithLots

    @Query("SELECT * FROM VegaCoffeeReceiving where tempWBId = :tmpWbId")
    abstract fun getOBDDetailsSupIndo(tmpWbId: String): LiveData<VegaIndoCoffeeReceivingItemWithBags>

    @Query("SELECT * FROM VegaCoffeeReceiving where tempWBId = :tmpWbId")
    abstract fun getOBDDetailsSupIndoOffline(tmpWbId: String): VegaIndoCoffeeReceivingItemWithBags

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveMtnrReceiving(vegaCoffeeReceivingData: VegaCoffeeReceiving)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveMtnrReceivingLot(lot: VegaCoffeeReceiveLots)

    @Query("DELETE FROM VegaCoffeeReceiving where mtnNumber = :mtnNumber")
    abstract fun deleteReceivingItem(mtnNumber: String)

    @Query("DELETE FROM VegaCoffeeReceiveLots where mtnNumber = :mtnNumber")
    abstract fun deleteReceivingLotItem(mtnNumber: String)

    @Query("DELETE FROM VegaCoffeeOffloadingBagMaterial where mtnNumber = :mtnNumber")
    abstract fun deleteReceivingLotItemWithBagItems(mtnNumber: String)

    @Query("DELETE FROM VegaCoffeeReceiving where tempWBId = :tempWBId")
    abstract fun deleteReceivingItemIndo(tempWBId: String)

    @Query("DELETE FROM VegaCoffeeReceiveLots where tempWBId = :tempWBId")
    abstract fun deleteReceivingLotItemIndo(tempWBId: String)

    @Query("DELETE FROM VegaCoffeeOffloadingBagMaterial where tempWBId = :tempWBId")
    abstract fun deleteReceivingLotItemWithBagItemsIndo(tempWBId: String)

    @Query("DELETE FROM VegaGrnWeighBridgeId where wbTempId = :wbTempId")
    abstract fun deleteWBItem(wbTempId: String)

    @Query("DELETE FROM VegaQualityWBDetails where weighBridgeId = :wbid")
    abstract fun deleteWBList(wbid: String)

    @Transaction
    @Query("SELECT * FROM VegaQuality WHERE materialCode = :materialId and wbid = :wbId")
    abstract fun getQualityParameterWithData(materialId: String, wbId: String?): List<VegaQualityWithQualitative>

    @Transaction
    @Query("SELECT * FROM VegaQualityParameter WHERE materialCode = :materialId")
    abstract fun getQualityParameter(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>

    @Query("SELECT * FROM VegaCoffeeReceiving where isSynced = 0 and isOnlineData = 0")
    abstract fun getOffloadingItem(): LiveData<List<VegaCoffeeReceiving>>

    @Query("SELECT * FROM VegaCoffeeReceiving WHERE syncId =:s")
    abstract fun getOffloadingItemAll(s: String): LiveData<List<VegaIndoCoffeeReceivingMtnrWithLots>>

    @Query("UPDATE VegaCoffeeReceiving SET syncId = :s,isSynced =0, isNotWBID = 1,isOnlineData = 0, syncStatusMsg = :msg  WHERE tempWBId=:tmpWbId")
    abstract fun updateOffloadingComplete(tmpWbId: String, s: String, msg: String)

    @Query("UPDATE VegaCoffeeReceiving SET isSynced =:isSync,isNotWBID = 0,isOnlineData = 0, status = :status, weighBridgeId = :wbid,grnNumber =:grnNumber, syncId = :s, syncStatusMsg = :msg  WHERE tempWBId=:tmpWbId")
    abstract fun updateOffloadingCompleteSuccess(tmpWbId: String, s: String, msg: String, wbid: String, status: Status, grnNumber: String,isSync:Boolean)

    @Transaction
    @Query("UPDATE VegaQualityWBDetails SET weighBridgeId = :wbid1, isNotWBID = 0 WHERE wbTempId = :tempId")
    abstract fun updateTempToWBItem(wbid1: String, tempId: String)

    @Transaction
    @Query("UPDATE VegaQuality SET wbid = :wbid1 WHERE wbTempId = :tempId")
    abstract fun updateTempToWBQuality(wbid1: String, tempId: String)

    @Transaction
    @Query("UPDATE VegaCoffeeLot SET weighBridgeId = :wbid1 WHERE tempId = :tempId")
    abstract fun updateTempToWBQualityLot(wbid1: String, tempId: String)

    @Transaction
    @Query("UPDATE VegaGrnWeighBridgeId SET weighBridgeId = :wbid1 WHERE wbTempId = :tempId")
    abstract fun updateTempIdToWbidGrn(wbid1: String, tempId: String)

    fun updateTempIdToWbid(wbid: String, tempId: String) {
        updateTempToWBQuality(wbid, tempId)
        updateTempToWBItem(wbid, tempId)
        updateTempIdToWbidGrn(wbid, tempId)
        updateTempToWBQualityLot(wbid, tempId)
    }

    @Query("SELECT * FROM VegaQualityWBDetails where isSyncStatus = 0 and isOfflineData = 1")
    abstract fun getQualityItem(): LiveData<List<VegaQualityWBDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveIndoCoffeSalesOrder(salesOrderList: List<IndoExporSalesMaterialList>)

    @Query("SELECT * FROM VegaIndoCoffeeExportSalesOrder where isSynced = 0 and isOffline = 1")
    abstract fun getIndoExportSalesItem(): LiveData<List<VegaIndoCoffeeExportSalesOrder>>

    @Query("UPDATE VegaCoffeeReceiveLots SET wbFlag = :wbFlag, qcFlag=:qcFlag, grnFlag=:grnFlag WHERE batch = :batchNumber")
    abstract fun updateLotStatus(batchNumber: String, wbFlag: Boolean, qcFlag: Boolean, grnFlag: Boolean)
}
