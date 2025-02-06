package com.olam.warehouse.master.dorigin.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.common.dao.BaseDao
import com.olam.warehouse.master.dorigin.entity.*
import com.olam.warehouse.master.dorigin.model.DOReceivingMtnWrapper
import com.olam.warehouse.master.dorigin.model.DOReceivingWarehouseWithMtns
import com.olam.warehouse.master.dorigin.model.DOReceivingWithLineItems
import com.olam.warehouse.presentation.enums.Status

/**
 * Created by Baskaran Kannan on 12/19/2019.
 */

@Dao
abstract class DOReceivingDao : BaseDao<DOReceiving>() {

    suspend fun save(item: DOReceiving) {
        insert(item)
    }

    @Query("SELECT * FROM DOReceiving WHERE isSynced = 0")
    abstract fun getReceiving(): LiveData<List<DOReceiving>>

    @Query("SELECT * FROM DOBag WHERE lotTransactionId=:txnId and bagMissed =:isMissed")
    abstract fun getDOBags(txnId: String, isMissed: Boolean? = true): LiveData<List<DOBag>>

    @Query("DELETE FROM DOBag WHERE transactionId=:txnId AND bagQrCode=:currentQrCode")
    abstract fun deleteBag(txnId: String?, currentQrCode: String)

    @Delete
    abstract fun deleteBag(doBag: DOBag)

    @Query("SELECT * FROM DOBag WHERE transactionId=:txnId")
    abstract fun getAllDOBags(txnId: String): LiveData<List<DOBag>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertBag(bag: DOBag)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveReceivingLineItems(lineItems: List<DOReceivingLineItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveSapMaterialByProductList(materialList:List<DOSapMaterialList>)

    @Transaction
    @Query("SELECT * FROM DOReceiving WHERE isSynced=0 and currentKey =:key")
    abstract fun getReceivingWithLineItem(key: String): LiveData<List<DOReceivingWithLineItems>>

    fun deleteItemReceiving(wbid: String) {
        deleteOfflineReceiving(wbid)
        deleteOfflineReceivingLineItem(wbid)
    }

    @Query("SELECT * FROM DOSapMaterialList")
    abstract fun getSapMaterialByProductList():LiveData<List<DOSapMaterialList>>

    @Query("SELECT * FROM DispatchDetail")
    abstract fun isDispatchDetailIdExist(): List<DispatchDetail>

    @Query("SELECT * FROM DispatchDetail")
    abstract fun getDispatchDetails(): LiveData<List<DispatchDetail>>

    @Transaction
    @Query("DELETE FROM DOReceiving where tmpWbId = :wbid")
    abstract fun deleteOfflineReceiving(wbid: String)

    @Transaction
    @Query("UPDATE DOTransactionDetail SET isMapped = 0 WHERE lotTransactionId=:txnId")
    abstract fun updateDeletedTransactionDetail(txnId: String)


    @Transaction
    @Query("DELETE FROM DOReceivingLineItem where tmpWbId = :wbid")
    abstract fun deleteOfflineReceivingLineItem(wbid: String)

    //Master Data

    @Query("SELECT * FROM DOMaterial")
    abstract fun getProducts(): LiveData<List<DOMaterial>>

    @Query("SELECT * FROM DOStorageLocation")
    abstract fun getLocations(): LiveData<List<DOStorageLocation>>

    @Query("SELECT * FROM DOVendor")
    abstract fun getSuppliers(): LiveData<List<DOVendor>>

    @Query("SELECT * FROM DOPackageMaterial ORDER BY isDefault DESC")
    abstract fun getMaterials(): LiveData<List<DOPackageMaterial>>

    @Query("SELECT * FROM DOMaterial")
    abstract fun getSAPMaterials(): LiveData<List<DOMaterial>>

    @Query("SELECT * FROM DOReceivingWarehouse")
    abstract suspend fun getWarehouse(): List<DOReceivingWarehouse>

    @Query("SELECT * FROM DOReceivingWarehouse")
    abstract fun getWarehouses(): LiveData<List<DOReceivingWarehouse>>

    @Transaction
    @Query("SELECT * FROM DOReceivingWarehouse WHERE supplyingPlantId=:whId")
    abstract fun getWarehousesWithMtns(whId: String): LiveData<DOReceivingWarehouseWithMtns>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertMtns(mtns: List<DOReceivingMtn>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertWarehouses(stockSupplyingPlants: List<DOReceivingWarehouse>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertLots(batchDetails: List<DOReceivingMtnLots>)

    suspend fun saveWarehouseAndMtns(it: DOReceivingMtnWrapper) {
        insertLots(it.batchDetails)
        insertWarehouses(it.stockSupplyingPlants)
        insertMtns(it.mtns)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertBagType(packageMaterial: List<DOPackageMaterial>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertLocation(storageLocation: List<DOStorageLocation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMaterial(storageLocation: List<DOMaterial>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertVendor(storageLocation: List<DOVendor>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertWarehouse(storageLocation: List<DOWarehouse>)

    @Query("SELECT * FROM DOTransactionDetail WHERE lotTransactionId=:id")
    abstract fun getTransactionDetailOffline(id: String): LiveData<DOTransactionDetail>

    @Query("UPDATE DOTransactionDetail SET isMapped = 1 WHERE lotTransactionId=:lotTransactionId")
    abstract fun updateTransactionDetails(lotTransactionId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDispatchDetail(dispatchDetail: DispatchDetail)

    @Query("DELETE FROM DispatchDetail")
    abstract suspend fun deleteAllDispatchDetails()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTransactionDetail(transItem: DOTransactionDetail)

    @Query("SELECT * FROM DOTransactionDetail WHERE lotTransactionId = :transId and isMapped = 1")
    abstract fun isTarnsactionIdExist(transId: String): List<DOTransactionDetail>

    @Query("UPDATE DOReceiving SET syncStatusMsg = :msg, status =:syncError   WHERE tmpWbId=:tmpWbId")
    abstract fun updateReceivingFailMsg(msg: String, tmpWbId: String, syncError: Status)

    @Query("UPDATE DOReceivingLineItem SET syncStatusMsg = :msg, status =:syncError   WHERE tmpWbId=:tmpWbId")
    abstract fun updateReceivingLineItemFailMsg(msg: String, tmpWbId: String, syncError: Status)

    @Query("SELECT COUNT(*) FROM DOReceiving WHERE isSynced = 0")
    abstract fun getOfflineDOReceivingCount(): Int

    @Query("SELECT * FROM DOTransactionDetail WHERE isMapped = 0")
    abstract fun getTransactionsOffline(): LiveData<List<DOTransactionDetail>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertCustomStLocation(prepareVegaCustomStLocation: List<DOCustomStLocation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertBinDetails(data: DOBinDetails)

    @Query("SELECT * FROM DOCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<DOCustomStLocation>>

    @Query("SELECT * FROM DOBag")
    abstract fun getAllDOBagsInfo(): LiveData<List<DOBag>>

    @Query("SELECT * FROM DOBag")
    abstract fun getAllDOBagsOfflineInfo(): List<DOBag>

    @Query("SELECT * FROM DOSapMaterialList")
    abstract fun getDoSapMaterialByProductList():List<DOSapMaterialList>

}
