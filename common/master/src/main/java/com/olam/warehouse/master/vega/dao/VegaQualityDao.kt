package com.olam.warehouse.master.vega.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.common.model.Material
import com.olam.warehouse.master.dorigin.entity.DOMaterial
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaLotQualityDetails
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaMtnrQualityLot
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaQuality
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaQualityMtnBatch
import com.olam.warehouse.master.vegaghana.model.VegaGhanaWeighBridgeWithQualityParams

/**
 * Created by Baskaran Kannan on 1/16/2020.
 */
@Dao
abstract class VegaQualityDao {
    @Query("SELECT * FROM VegaReceiving WHERE isSynced = 0")
    abstract fun getReceivingDetail(): List<VegaReceiving>

    @Query("SELECT * FROM VegaCocoaMiscellaneous where  roleKey = :role")
    abstract fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>>

    //Getting Threshold Value
    @Query("SELECT * FROM VegaCocoaMiscellaneous where  roleKey = :role")
    abstract fun getThresholdValue(role: String): LiveData<List<VegaCocoaMiscellaneous>>

    @Query("SELECT * FROM VegaCoffeeThirdPartyMaterialDetail")
    abstract fun getThirdPartyMaterials(): List<VegaCoffeeThirdPartyMaterialDetail>

    @Transaction
    @Query("UPDATE VegaQualityWBDetails SET grnNumber = :grnNo, batchNumber = :batchNo WHERE weighBridgeId =:wbid")
    abstract fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String)

    @Query("UPDATE VegaGrnWeighBridgeId SET grnNumber = :grnNo, message = :msg, status = :status, isSyncStatus = 1, batchNumber =:batch,isErrorStatus = 0 WHERE weighBridgeId =:wbid")
    abstract fun updateGrnSuccess(wbid: String, grnNo: String, batch: String, msg: String, status: Int)

    //Master Data

    @Query("SELECT * FROM VegaConfigDetails where rolekey = :role")
    abstract fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

    @Query("SELECT * FROM VegaQualityWBDetails WHERE isSyncStatus = 0 and isOfflineData = 0")
    abstract fun getQualityWeighBridgeDetail(): LiveData<List<VegaQualityWBDetails>>

    @Query("SELECT * FROM VegaQualityWBDetails")
    abstract fun getQualityWeighBridgeDetailAll(): LiveData<List<VegaQualityWBDetails>>

    @Query("SELECT * FROM VegaQualityWBDetails WHERE isSyncStatus = 0 and isOfflineData = 0")
    abstract suspend fun getWeighBridgeDetailOnline(): List<VegaQualityWBDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertQualityWbDetails(wbList: List<VegaQualityWBDetails>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertQualityWbDetail(wbList: VegaQualityWBDetails)

    @Query("SELECT * FROM VegaQualityWBDetails WHERE wbTempId = :wbid and isOfflineData = 1")
    abstract fun isWBExist(wbid: String): List<VegaQualityWBDetails>

    fun save(item: List<VegaQualityWBDetails>) {
        item.forEach {
            if (isWBExist(it.weighBridgeId).isNotEmpty()) return@forEach
            it.wbTempId = it.weighBridgeId
            it.supplierCode = it.supplierCode
            insertQualityWbDetail(it)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertQuality(qualityList: VegaQuality)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun savePostLotDetails(qualityList: List<VegaGhanaMtnrQualityLot>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMtnrQuality(qualityList: VegaGhanaQuality)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOfflineQuality(qualityList: List<VegaQuality>)

    @Transaction
    @Query("SELECT * FROM VegaPackageMaterial")
    abstract fun getMaterials(): LiveData<List<VegaPackageMaterial>>

    suspend fun saveQualityData(qualityParameter: VegaQuality, batchno: String) {
        updateWBListStatus(qualityParameter.wbid, batchno)
        qualityParameter.wbTempId = qualityParameter.wbid
        insertQuality(qualityParameter)
    }

    fun saveVegaQualityWBDetails(wbid: String, batchno: String) {
        updateWBListStatus(wbid, batchno)
    }

    fun saveVegaQualityWeightWBDetails(wbid: String, batchno: String, weight: String) {
        updateWeightWBListStatus(wbid, batchno, weight)
    }

    suspend fun saveMtnrQualityData(qualityParameter: VegaGhanaQuality, batchno: String) {
        updateWBListStatus(qualityParameter.wbid, batchno)
        qualityParameter.wbTempId = qualityParameter.wbid
        insertMtnrQuality(qualityParameter)
    }
/*
    suspend fun saveOfflineQualityData(qualityParameter: List<VegaQuality>, batchno: String, wbid: String) {
//        updateWBListStatus(qualityParameter.wbid, batchno)
//        qualityParameter.wbTempId = qualityParameter.wbid
        insertQuality(qualityParameter)
    }*/

    suspend fun saveQualityDataWithOffline(qualityParameter: VegaQuality, batchno: String) {
        //updateWBListStatus(qualityParameter.wbid, batchno)
        qualityParameter.wbTempId = qualityParameter.wbid
        qualityParameter.wbid = ""
        insertQuality(qualityParameter)
    }

    @Query("DELETE FROM VegaQualityWBDetails where weighBridgeId = :wbid")
    abstract fun updateWBDB(wbid: String)

    @Query("UPDATE VegaQualityWBDetails SET isSyncStatus = 1, isErrorStatus = 1, batchNumber =:bid, status =:status , message = :msg where weighBridgeId = :wbid")
    abstract fun updateWBListStatusSuccess(wbid: String, bid: String, msg: String, status: Int)

    @Query("UPDATE VegaQualityWBDetails SET isSyncStatus = 0, isErrorStatus = 0, batchNumber =:bid, status =:status , message = :msg where weighBridgeId = :wbid")
    abstract fun updateWBListStatusFail(wbid: String, bid: String, msg: String, status: Int)

    @Query("UPDATE VegaQualityWBDetails SET isOfflineData = 1, batchNumber =:bid where weighBridgeId = :wbid")
    abstract fun updateWBListStatus(wbid: String, bid: String)

    @Query("UPDATE VegaQualityWBDetails SET isOfflineData = 1, batchNumber =:bid, paidWeight = :weight  where weighBridgeId = :wbid")
    abstract fun updateWeightWBListStatus(wbid: String, bid: String, weight: String)

    @Query("SELECT * FROM VegaQualityWBDetails WHERE isOfflineData =1 and isSyncStatus =0")
    abstract fun getQualityOfflineList(): LiveData<List<VegaQualityWBDetails>>

    @Query("SELECT * FROM VegaQualityWBDetails WHERE isOfflineData =1 and isSyncStatus =0 and weighBridgeType= :weighType ")
    abstract fun getGhanaQualityOfflineList(weighType: String): LiveData<List<VegaQualityWBDetails>>

    @Query("SELECT * FROM VegaQualityWBDetails WHERE isOfflineData =1 and isSyncStatus =0")
    abstract fun getGhanaMtnrQualityOfflineList(): LiveData<List<VegaQualityWBDetails>>

    @Transaction
    @Query("UPDATE VegaQualityWBDetails SET isOfflineData = 0, isSyncStatus =0 where weighBridgeId = :wbid")
    abstract fun updateDeletedWBItem(wbid: String)

    @Query("DELETE FROM VegaQuality where wbid = :wbid")
    abstract fun deleteOfflineParams(wbid: String)

    @Query("DELETE FROM VegaQualityWBDetails where isOfflineData = 1 and weighBridgeType = :weighBridgeType")
    abstract fun deleteWBDetals(weighBridgeType: String)

    fun updateDeletedItem(wbid: String) {
        updateDeletedWBItem(wbid)
        deleteOfflineParams(wbid)
    }

    @Transaction
    @Query("SELECT * FROM VegaQualityWBDetails WHERE isOfflineData = 1 and isSyncStatus =0")
    abstract fun getWBWithQualityAll(): LiveData<List<VegaWeighBridgeWithQualityParams>>

    @Transaction
    @Query("SELECT * FROM VegaQualityWBDetails WHERE isOfflineData = 1 and isSyncStatus =0 and weighBridgeId = :wbid")
    abstract fun getWBWithQualitySingle(wbid: String): List<VegaWeighBridgeWithQualityParams>

    @Transaction
    @Query("SELECT * FROM VegaQualityWBDetails WHERE weighBridgeId = :wbid")
    abstract fun getWBWithQualitySingleSync(wbid: String): VegaWeighBridgeWithQualityParams

    @Transaction
    @Query("SELECT * FROM VegaGhanaMtnrQualityLot WHERE weighBridgeId = :wbid")
    abstract fun getGhanaMtnrLotDetails(wbid: String): VegaGhanaWeighBridgeWithQualityParams

    @Transaction
    @Query("SELECT * FROM VegaGhanaMtnrQualityLot WHERE weighBridgeId = :wbid")
    abstract fun getLotQualityDetails(wbid: String): LiveData<VegaGhanaMtnrQualityLot>

    @Transaction
    @Query("SELECT * FROM VegaGhanaMtnrQualityLot ")
    abstract fun getAllLotQualityDetails(): LiveData<List<VegaGhanaMtnrQualityLot>>

    @Query("UPDATE VegaQualityWBDetails SET weighBridgeId = :wbid1, isNotWBID = 0 WHERE wbTempId = :tempId")
    abstract fun updateTempToWBItem(wbid1: String, tempId: String)

    @Query("UPDATE VegaQuality SET wbid = :wbid1 WHERE wbTempId = :tempId")
    abstract fun updateTempToWBQuality(wbid1: String, tempId: String)

    fun updateTempIdToWbid(wbid: String, tempId: String) {
        updateTempToWBQuality(wbid, tempId)
        updateTempToWBItem(wbid, tempId)
    }

    @Transaction
    @Query("SELECT * FROM VegaQualityParameter WHERE materialCode = :materialId")
    abstract fun getQualityParameter(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>

    @Transaction
    @Query("SELECT * FROM VegaQuality WHERE materialCode = :materialId and wbid = :wbId")
    abstract fun getQualityParameterWithData(materialId: String, wbId: String?): List<VegaQualityWithQualitative>

    @Transaction
    @Query("SELECT * FROM VegaQuality WHERE materialCode = :materialId and wbid = :wbId")
    abstract fun getOfflineSavedQuality(materialId: String, wbId: String?): LiveData<List<VegaQualityWithQualitative>>

    @Transaction
    @Query("SELECT * FROM VegaGhanaQuality WHERE materialCode = :materialId and wbid = :wbId")
    abstract fun getMtnrQualityParameterWithData(materialId: String, wbId: String?): List<VegaQualityWithQualitative>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertWeighBridge(storageLocation: VegaQualityWBDetails)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertQualitative(storageLocation: VegaQualitative)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertQParam(storageLocation: List<VegaQualityParameter>)

    @Query("UPDATE VegaQualityWBDetails SET message = :msg, isErrorStatus = 0, status = 3 WHERE weighBridgeId = :weighBridgeId")
    abstract fun updateWBMessage(msg: String, weighBridgeId: String?)

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

    @Query("DELETE FROM VegaQualityWBDetails  where weighBridgeId = :wbid")
    abstract fun updateDB(wbid: String)

    @Query("SELECT * FROM VegaGhanaLotQualityDetails where charg = :batchNo and materialNumber = :materialId")
    abstract fun getOfflinePreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<List<VegaGhanaLotQualityDetails>>

    @Query("SELECT * FROM VegaGhanaQualityMtnBatch where mtnNumber = :deliveryNo ")
    abstract fun getOfflineDeliveryBatchNumber(deliveryNo: String): LiveData<VegaGhanaQualityMtnBatch>

    @Transaction
    @Query("SELECT * FROM VegaQuality WHERE materialCode = :materialId and wbTempId =:tmpId")
    abstract fun getWBWithQualityAllOffline(
        materialId: String,
        tmpId: String
    ): LiveData<List<VegaQuality>>

    @Transaction
    @Query("SELECT * FROM VegaQualityWBDetails WHERE weighBridgeId = :weighBridgeID")
    abstract fun getWBWithQuality(weighBridgeID: String): LiveData<List<VegaWeighBridgeWithQualityParams>>

    @Transaction
    @Query("SELECT * FROM VegaQualityWBDetails WHERE isOfflineData = 1")
    abstract fun getWBWithQuality(): LiveData<List<VegaWeighBridgeWithQualityParams>>

    @Transaction
    @Query("SELECT * FROM VegaQualityWBDetails WHERE isOfflineData = 1 and weighBridgeType= :weighBridgeType ")
    abstract fun getGhanaCashewWBWithQuality(weighBridgeType: String): LiveData<List<VegaWeighBridgeWithQualityParams>>

    @Transaction
    @Query("SELECT * FROM VegaQualityWBDetails WHERE isOfflineData = 1 and status != 4   ")
    abstract fun getGhanaWBWithQuality(): LiveData<List<VegaWeighBridgeWithQualityParams>>

    @Transaction
    @Query("SELECT * FROM VegaGrnWeighBridgeId WHERE isSyncStatus = 0 and isNotWBID = 1 and isOfflineData = 1")
    abstract fun getGrnDetail(): List<VegaGrnWeighBridgeId>

    @Query("DELETE FROM VegaQualitative")
    abstract fun clearQualitative()

    @Query("DELETE FROM VegaQualityParameter")
    abstract fun clearQualityParameters()

    //Clear Master Data before Inserting
    fun clearMasterData() {
        clearQualitative()
        clearQualityParameters()
    }

    @Query("SELECT * FROM VegaReceiving WHERE isSynced = 0")
    abstract fun getReceivingQualityDetail(): List<VegaReceiving>

/*    @Query("SELECT * FROM VegaCameroonMultiPlantDetails")
    abstract fun getMultiPlantList(): LiveData<List<Plant>>*/

    @Query("SELECT * FROM VegaMaterial where materialCode=:materialCode")
    abstract fun getSAPMaterialsUsingMaterialCode(materialCode: String): LiveData<VegaMaterial>

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getVegaMaterials(): LiveData<List<VegaMaterial>>

}
