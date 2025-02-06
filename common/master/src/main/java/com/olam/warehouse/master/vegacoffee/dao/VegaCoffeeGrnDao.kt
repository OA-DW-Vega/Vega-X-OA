package com.olam.warehouse.master.vegacoffee.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
@Dao
abstract class VegaCoffeeGrnDao {
    @Query("SELECT * FROM VegaConfigDetails where rolekey = :role")
    abstract fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

    @Query("SELECT * FROM VegaGrnWeighBridgeId WHERE weighBridgeId = :wbid and isOfflineData = 1")
    abstract fun isWBExist(wbid: String): List<VegaGrnWeighBridgeId>

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

    @Transaction
    @Query("UPDATE VegaGrnWeighBridgeId SET isOfflineData =0, isSyncStatus = 0, grnNumber = :emptyString, unitPrice = :emptyString WHERE weighBridgeId =:weighBridgeId")
    abstract fun updateDeletedItem(weighBridgeId: String, emptyString: String)

    @Query("SELECT * FROM VegaGrnWeighBridgeId WHERE isOfflineData = 1")
    abstract fun getOfflineWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>>

    @Query("SELECT * FROM VegaGrnWeighBridgeId WHERE isOfflineData = 1 and isSyncStatus =0")
    abstract fun getOfflineWeighBridgeDetailCount(): LiveData<List<VegaGrnWeighBridgeId>>

    @Query("SELECT * FROM VegaGrnWeighBridgeId WHERE isOfflineData = 1 and weighBridgeId =:wbid")
    abstract fun getOfflineWeighBridgeDetail(wbid: String): VegaGrnWeighBridgeId

    @Query("UPDATE VegaGrnWeighBridgeId SET grnNumber = :grnNo, message = :msg, status = :status, isSyncStatus = 0, batchNumber =:batch, isErrorStatus = 0 WHERE weighBridgeId =:wbid")
    abstract fun updateGrn(wbid: String, grnNo: String, batch: String, msg: String, status: Int)

    @Query("UPDATE VegaGrnWeighBridgeId SET grnNumber = :grnNo, message = :msg, status = :status, isSyncStatus = 1, batchNumber =:batch WHERE weighBridgeId =:wbid")
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

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

    @Transaction
    @Query("SELECT * FROM VegaQualityParameter WHERE materialCode = :materialId")
    abstract fun getQualityParameter(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>

    @Transaction
    @Query("SELECT * FROM VegaQuality WHERE materialCode = :materialId and wbid = :wbId")
    abstract fun getQualityParameterWithData(materialId: String, wbId: String?): List<VegaQualityWithQualitative>

}
