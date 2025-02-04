package com.olam.warehouse.master.vegaecuador.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
@Dao
abstract class VegaEcuadorGrnDao {
    @Query("SELECT * FROM VegaConfigDetails where rolekey = :role")
    abstract fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

    @Query("SELECT * FROM VegaGrnWeighBridgeId WHERE weighBridgeId = :wbid and isOfflineData = 1")
    abstract fun isWBExist(wbid: String): List<VegaGrnWeighBridgeId>

    @Query("SELECT * FROM VegaStorageLocationDetail")
    abstract fun getStorageLocationDetail(): LiveData<List<VegaStorageLocationDetail>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertWeighBridge(storageLocation: VegaGrnWeighBridgeId)

    @Query("SELECT * FROM VegaReceiving WHERE isSynced = 0")
    abstract fun getReceivingDetail(): List<VegaReceiving>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertQualityWbDetail(wbList: VegaGrnWeighBridgeId)

    @Query("SELECT * FROM VegaGrnWeighBridgeId WHERE isSyncStatus = 0 and isOfflineData = 0")
    abstract fun getQualityWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateGRNPrice(wbDetails: VegaGrnWeighBridgeId)

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getAllProducts(): LiveData<List<VegaMaterial>>

    @Query("SELECT * FROM VegaCocoaMiscellaneous where  roleKey = :role")
    abstract fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>>

    @Transaction
    @Query("UPDATE VegaGrnWeighBridgeId SET isOfflineData =0, isSyncStatus = 0, grnNumber = :emptyString, unitPrice = :emptyString WHERE weighBridgeId =:weighBridgeId")
    abstract fun updateDeletedItem(weighBridgeId: String, emptyString: String)

    @Query("SELECT * FROM VegaGrnWeighBridgeId WHERE isOfflineData = 1")
    abstract fun getOfflineWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>>

    @Transaction
    @Query("SELECT * FROM VegaPackageMaterial")
    abstract fun getMaterials(): LiveData<List<VegaPackageMaterial>>

    @Transaction
    @Query("SELECT * FROM VegaGrnWeighBridgeId WHERE isTransStatus = 1")
    abstract fun getWeighBridgeTransDetail(): LiveData<List<VegaGrnWeighBridgeId>>

    @Query("SELECT * FROM VegaGrnWeighBridgeId WHERE isOfflineData = 1 and isSyncStatus =0")
    abstract fun getOfflineWeighBridgeDetailCount(): LiveData<List<VegaGrnWeighBridgeId>>

    @Query("SELECT * FROM VegaGrnWeighBridgeId WHERE isOfflineData = 1 and weighBridgeId =:wbid")
    abstract fun getOfflineWeighBridgeDetail(wbid: String): VegaGrnWeighBridgeId

    @Query("UPDATE VegaGrnWeighBridgeId SET grnNumber = :grnNo, message = :msg, status = :status, isSyncStatus = 0, batchNumber =:batch, isErrorStatus = 0 WHERE weighBridgeId =:wbid")
    abstract fun updateGrn(wbid: String, grnNo: String, batch: String, msg: String, status: Int)

    @Query("UPDATE VegaGrnWeighBridgeId SET grnNumber = :grnNo, message = :msg, status = :status, isSyncStatus = 0, batchNumber =:batch, isErrorStatus = 0 WHERE weighBridgeId =:wbid")
    abstract fun updateGrnIndo(wbid: String, grnNo: String, batch: String, msg: String, status: Int)

    @Query("UPDATE VegaGrnWeighBridgeId SET grnNumber = :grnNo, message = :msg, status = :status, isSyncStatus = 1, batchNumber =:batch,isErrorStatus = 0 WHERE weighBridgeId =:wbid")
    abstract fun updateGrnSuccess(wbid: String, grnNo: String, batch: String, msg: String, status: Int)

    @Transaction
    @Query("UPDATE VegaQualityWBDetails SET grnNumber = :grnNo, batchNumber = :batchNo WHERE weighBridgeId =:wbid")
    abstract fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String)

    @Query("UPDATE VegaGrnWeighBridgeId SET weighBridgeId = :wbid1 WHERE wbTempId = :tempId")
    abstract fun updateTempIdToWbid(wbid1: String, tempId: String)

    @Query("DELETE FROM VegaQualityWBDetails where weighBridgeId = :wbid")
    abstract fun updateDeletedWBItem(wbid: String)

    @Query("DELETE FROM VegaQuality where wbid = :wbid")
    abstract fun deleteVegaOfflineParams(wbid: String)

    fun updateDeletedItemQuality(wbid: String) {
        updateDeletedWBItem(wbid)
        deleteVegaOfflineParams(wbid)
    }

    @Query("SELECT * FROM VegaStorageLocation where storageLocationCode = :code")
    abstract fun getStorageLocation(code: String): LiveData<VegaStorageLocation>

    @Query("SELECT * FROM VegaVendor")
    abstract fun getSuppliers(): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaQualityParameter WHERE entryObligatory = :entryObligatory and materialCode =:material")
    abstract fun getQualityParamsDB(
        material: String,
        entryObligatory: String
    ): LiveData<List<VegaQualityParameter>>

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

    @Query("SELECT * FROM VegaEcuadorPurchaseOrder")
    abstract fun getPOListOffline(): LiveData<List<VegaEcuadorPurchaseOrder>>

    @Query("DELETE FROM VegaGrnWeighBridgeId where wbTempId = :wbTempId")
    abstract fun deleteWBItem(wbTempId: String)

    @Query("UPDATE VegaGrnWeighBridgeId SET isOfflineData = 0, isTransStatus=0 WHERE wbTempId = :wbTempId")
    abstract fun updatedeleteStatus(wbTempId: String)

    @Query("SELECT * FROM VegaGrnWeighBridgeId WHERE wbTempId =:wbid")
    abstract fun getGrnWBDetails(wbid: String): VegaGrnWeighBridgeId

    @Query("SELECT * FROM VegaQualityWBDetails WHERE isOfflineData =1 and isSyncStatus =0")
    abstract fun getQualityOfflineList(): List<VegaQualityWBDetails>

}
