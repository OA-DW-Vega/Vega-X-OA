package com.olam.warehouse.master.dorigin.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.dorigin.entity.*
import com.olam.warehouse.master.dorigin.model.DOQualityParamsWithQualitative
import com.olam.warehouse.master.dorigin.model.DOQualityWithQualitative
import com.olam.warehouse.master.dorigin.model.DOWeighBridgeWithQualityParams

/**
 * Created by Baskaran Kannan on 12/19/2019.
 */
@Dao
abstract class DOQualityDao {
    @Query("SELECT * FROM DOReceiving WHERE isSynced = 0 and currentKey = :key")
    abstract fun getReceivingDetail(key: String): List<DOReceiving>

    //Master Data

    @Query("SELECT * FROM DOQualityWBDetails WHERE isSyncStatus = 0 and isOfflineData = 0 and currentKey = :key")
    abstract fun getQualityWeighBridgeDetail(key: String): LiveData<List<DOQualityWBDetails>>

    @Query("SELECT * FROM DOQualityWBDetails WHERE isSyncStatus = 0 and isOfflineData = 0 and currentKey = :key")
    abstract suspend fun getWeighBridgeDetailOnline(key: String): List<DOQualityWBDetails>

    @Query("SELECT * FROM DOMaterial")
    abstract fun getSAPMaterials(): LiveData<List<DOMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertQualityWbDetails(wbList: List<DOQualityWBDetails>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertQualityWbDetail(wbList: DOQualityWBDetails)

    @Query("SELECT * FROM DOQualityWBDetails WHERE weighBridgeId = :wbid and isOfflineData = 1")
    abstract fun isWBExist(wbid: String): List<DOQualityWBDetails>

    fun save(item: List<DOQualityWBDetails>) {
        item.forEach {
            if (isWBExist(it.weighBridgeId).isNotEmpty()) return@forEach
            it.wbTempId = it.weighBridgeId
            it.supplierCode = it.supplierCode
            insertQualityWbDetail(it)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertQuality(qualityList: DOQuality)

    suspend fun saveQualityData(qualityParameter: DOQuality, batchno: String) {
        updateWBListStatus(qualityParameter.wbid, batchno)
        qualityParameter.wbTempId = qualityParameter.wbid
        insertQuality(qualityParameter)
    }

    @Query("DELETE FROM DOQualityWBDetails where weighBridgeId = :wbid")
    abstract fun updateWBDB(wbid: String)

    @Query("UPDATE DOQualityWBDetails SET isOfflineData = 1, batchNumber =:bid where weighBridgeId = :wbid")
    abstract fun updateWBListStatus(wbid: String, bid: String)

    @Query("SELECT * FROM DOQualityWBDetails WHERE isOfflineData =1 and currentKey = :key")
    abstract fun getQualityOfflineList(key: String): LiveData<List<DOQualityWBDetails>>

    @Query("UPDATE DOQualityWBDetails SET isOfflineData = 0 where weighBridgeId = :wbid")
    abstract fun updateDeletedWBItem(wbid: String)

    @Query("DELETE FROM DOQuality where wbid = :wbid")
    abstract fun deleteOfflineParams(wbid: String)

    fun updateDeletedItem(wbid: String) {
        updateDeletedWBItem(wbid)
        deleteOfflineParams(wbid)
    }

    @Transaction
    @Query("SELECT * FROM DOQualityWBDetails WHERE isOfflineData = 1 and isSyncStatus =0 and currentKey = :key")
    abstract fun getWBWithQualityAll(key: String): LiveData<List<DOWeighBridgeWithQualityParams>>

    @Transaction
    @Query("SELECT * FROM DOQualityWBDetails WHERE isOfflineData = 1 and isSyncStatus =0 and weighBridgeId = :wbid")
    abstract fun getWBWithQualitySingle(wbid: String): List<DOWeighBridgeWithQualityParams>

    @Query("UPDATE DOQualityWBDetails SET weighBridgeId = :wbid1, grossWeight = :grossWeight, isNotWBID = 0 WHERE wbTempId = :tempId")
    abstract fun updateTempToWBItem(wbid1: String, grossWeight: String, tempId: String)

    @Query("UPDATE DOQuality SET wbid = :wbid1 WHERE wbTempId = :tempId")
    abstract fun updateTempToWBQuality(wbid1: String, tempId: String)

    fun updateTempIdToWbid(wbid: String, grossWeight: String, tempId: String) {
        updateTempToWBQuality(wbid, tempId)
        updateTempToWBItem(wbid, grossWeight, tempId)
    }

    @Transaction
    @Query("SELECT * FROM DOQualityParameter WHERE materialCode = :materialId")
    abstract fun getQualityParameter(materialId: String): LiveData<List<DOQualityParamsWithQualitative>>

    @Transaction
    @Query("SELECT * FROM DOQuality WHERE materialCode = :materialId and wbid = :wbId")
    abstract fun getQualityParameterWithData(materialId: String, wbId: String?): List<DOQualityWithQualitative>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertWeighBridge(storageLocation: DOQualityWBDetails)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertQualitative(storageLocation: DOQualitative)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertQParam(storageLocation: List<DOQualityParameter>)

    @Query("UPDATE DOQualityWBDetails SET message = :msg, isErrorStatus = 0, status = 3 WHERE weighBridgeId = :weighBridgeId")
    abstract fun updateWBMessage(msg: String, weighBridgeId: String?)

    @Query("SELECT COUNT(*) FROM DOQualityWBDetails WHERE isOfflineData = 1")
    abstract fun getOfflineDOQualityWBDetailsCount(): Int
}
