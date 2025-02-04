package com.olam.warehouse.master.vegacocoa.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.common.dao.BaseDao
import com.olam.warehouse.master.user.model.VegaCoCoaThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoCoaReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.VegaCoCoaReceivingMtnrWithLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.enums.Status
import java.util.*

@Dao
abstract class VegaCoCoaOffloadDao : BaseDao<VegaCoCoaReceiving>() {

    suspend fun save(item: VegaCoCoaReceiving) {
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
    @Query("SELECT * FROM VegaCoCoaReceiving WHERE tempGrnNumber = :tempGrnNumber")
    abstract fun getReceivingWithLineItemWbid(tempGrnNumber: String): VegaCoCoaReceivingMtnrWithLots

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
    abstract fun getPlants(): LiveData<List<VegaCustomStLocation>>

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
    abstract suspend fun insertLots(batchDetails: List<VegaCoCoaReceiveLots>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertBcZone(bcList: List<VegaBcZoneMapping>)

    suspend fun saveWarehouseAndMtns(it: VegaCoCoaReceivingMtnWrapper) {
        insertLots(it.batchDetails)
        insertWarehouses(it.stockSupplyingPlants)
        insertMtns(it.mtns)
        //insertStorageLocation(it.storageLocationLst)
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
    abstract suspend fun insertThirdPartyMaterial(list: List<VegaCoCoaThirdPartyMaterialDetail>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertVendor(storageLocation: List<VegaVendor>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertWarehouse(storageLocation: List<VegaWarehouse>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertProcessingStage(processingStage: List<VegaProcessingStage>)

    @Query("UPDATE VegaReceiving SET syncStatusMsg = :msg, status =:syncError   WHERE tmpWbId=:tmpWbId")
    abstract fun updateReceivingFailMsg(msg: String, tmpWbId: String, syncError: Status)

    @Query("UPDATE VegaCoCoaReceiving SET grossWeight = :grossWeight, truckOutWeight =:truckoutWeight, netWeight=:netWeight WHERE delivery = :deliveryNumber")
    abstract fun updateWeight(grossWeight: String, truckoutWeight: String, netWeight: String, deliveryNumber: String)

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

    @Query("SELECT * FROM VegaCoCoaThirdPartyMaterialDetail")
    abstract fun getCoCoaThirdPartyMaterials(): LiveData<List<VegaCoCoaThirdPartyMaterialDetail>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertConfigDetails(configDetails: List<VegaConfigDetails>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertMiscellaneousDetails(configDetails: List<VegaCocoaMiscellaneous>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertConfigDetail(configDetails: VegaConfigDetails)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun savePurcheseOrder(preparePurcheseOrder: ArrayList<VegaEcuadorPurchaseOrder>)

    @Query("SELECT * FROM VegaCocoaOffloadingBagMaterial where isSyncStatus = 0")
    abstract fun getBagItems(): LiveData<List<VegaCoCoaOffloadingBagMaterial>>

    @Query("SELECT * FROM VegaCocoaOffloadingBagMaterial where batchNumber = :batchNumber and mtnNumber =:mtnNumber and isSyncStatus = 0")
    abstract fun getBagItems(batchNumber: String, mtnNumber: String): LiveData<List<VegaCoCoaOffloadingBagMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetails(bagMaterial: VegaCoCoaOffloadingBagMaterial)

    @Query("DELETE FROM VegaCoCoaOffloadingBagMaterial where batchNumber = :batchNumber and mtnNumber =:mtnNumber")
    abstract fun deleteBagDetails(batchNumber: String, mtnNumber: String)

    @Query("DELETE FROM VegaCoCoaOffloadingBagMaterial where bagType = :bagtype")
    abstract fun deleteBagDetails(bagtype: String)

    @Query("DELETE FROM VegaCoCoaOffloadingBagMaterial where id = :id")
    abstract fun deleteBagDetailsById(id: String)

    @Query("DELETE FROM VegaCoCoaReceiving where tempGrnNumber = :batchNumber")
    abstract fun deleteGrn(batchNumber: String)

    @Query("SELECT * FROM VegaCoCoaReceiving where delivery = :deliveryNumber")
    abstract fun getOBDDetails(deliveryNumber: String): LiveData<VegaCoCoaReceivingMtnrWithLots>

    @Query("SELECT * FROM VegaCoCoaReceiving where isSynced = 1")
    abstract fun getTransactions(): LiveData<List<VegaCoCoaReceivingMtnrWithLots>>

    @Query("SELECT * FROM VegaCoCoaReceiving where isOnlineData = 0 AND isSynced = 0")
    abstract fun getPendingList(): LiveData<List<VegaCoCoaReceivingMtnrWithLots>>

    @Query("SELECT COUNT(*) FROM VegaCoCoaReceiving where isOnlineData = 0 AND isSynced = 0")
    abstract fun getOfflinePendingCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveMtnrReceiving(vegaCoCoaReceivingData: VegaCoCoaReceiving)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveMtnrReceivingLot(lot: VegaCoCoaReceiveLots)

    @Query("DELETE FROM VegaCoCoaReceiving where mtnNumber = :mtnNumber")
    abstract fun deleteReceivingItem(mtnNumber: String)

    @Query("DELETE FROM VegaCoCoaReceiving where tempGrnNumber = :tempGrnNumber")
    abstract fun deleteCoCoaReceivingItem(tempGrnNumber: String)

    @Query("DELETE FROM VegaCoCoaReceiveLots where mtnNumber = :mtnNumber")
    abstract fun deleteReceivingLotItem(mtnNumber: String)

    @Query("DELETE FROM VegaCoCoaOffloadingBagMaterial where mtnNumber = :mtnNumber")
    abstract fun deleteReceivingLotItemWithBagItems(mtnNumber: String)

    @Query("SELECT * FROM VegaMaterial WHERE thirdPartyFlag = 'X'")
    abstract fun getThirdPartyMaterials(): LiveData<List<VegaMaterial>>

    @Query("SELECT * FROM VegaVendor WHERE vendorCode = :suppliercode")
    abstract fun getVendorInfo(suppliercode: String): LiveData<VegaVendor>

    @Query("SELECT * FROM VegaReceivingMtn")
    abstract fun getMTNRs(): LiveData<List<VegaReceivingMtn>>

    @Query("SELECT * FROM VegaReceivingMtnLots")
    abstract fun getOfflineLots(): LiveData<List<VegaReceivingMtnLots>>

    @Transaction
    @Query("SELECT * FROM VegaCoCoaQualityWBDetail where weighBridgeId = :weighBridgeId")
    abstract fun getCoCoaWBDetails(weighBridgeId: String): LiveData<VegaCoCoaQualityWBDetail>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertStorageLocation(storageLocation: VegaCoCoaStorageLocation)

    @Query("DELETE FROM VegaCoCoaStorageLocation")
    abstract fun deleteAll()

    @Query("SELECT * FROM VegaCoCoaStorageLocation")
    abstract fun getOfflineStorageLocations(): LiveData<List<VegaCoCoaStorageLocation>>
}
