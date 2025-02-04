package com.olam.warehouse.master.vegacoffee.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaIndoWeighBridgeWithQualityParams
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving

@Dao
abstract class VegaCoffeeQualityDao{
    @Transaction
    @Query("SELECT * FROM VegaQualityParameter WHERE materialCode = :materialId")
    abstract fun getQualityParameter(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>

    @Transaction
    @Query("SELECT * FROM VegaQuality WHERE materialCode = :materialId and wbid = :wbId")
    abstract fun getQualityParameterWithData(materialId: String, wbId: String?): List<VegaQualityWithQualitative>

    @Query("SELECT * FROM VegaQualityWBDetails WHERE isOfflineData =1 and isSyncStatus=0")
    abstract fun getQualityOfflineList(): LiveData<List<VegaQualityWBDetails>>

    @Query("SELECT * FROM VegaQualityWBDetails WHERE isErrorStatus =0")
    abstract fun getQualityOfflineListIndo(): LiveData<List<VegaQualityWBDetails>>

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

    suspend fun saveQualityData(qualityParameter: VegaQuality, batchno: String) {
        updateWBListStatus(qualityParameter.wbid, batchno)
        qualityParameter.wbTempId = qualityParameter.wbid
        insertQuality(qualityParameter)
    }

    @Query("UPDATE VegaQualityWBDetails SET isOfflineData = 1, batchNumber =:bid where weighBridgeId = :wbid")
    abstract fun updateWBListStatus(wbid: String, bid: String)

    @Query("UPDATE VegaQualityWBDetails SET isSyncStatus = 1,status = 4, batchNumber =:bid, message = :msg where weighBridgeId = :wbid")
    abstract fun updateWBListSuccessStatusWorks(wbid: String, bid: String, msg: String)

    @Query("UPDATE VegaQualityWBDetails SET status = 3, message = :msg where weighBridgeId = :wbid")
    abstract fun updateWBListErrorStatusWorks(wbid: String, msg: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertQuality(qualityList: VegaQuality)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertWeighBridge(storageLocation: VegaQualityWBDetails)

    @Transaction
    @Query("SELECT * FROM VegaCoffeeReceiving WHERE isSynced = 0 and isNotWBID = 1")
    abstract fun getOffloadingDetail(): List<VegaCoffeeReceiving>

    @Transaction
    @Query("SELECT * FROM VegaQualityWBDetails WHERE wbTempId = :wbid and isOfflineData = 1")
    abstract fun isWBExist(wbid: String): List<VegaQualityWBDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertQualityWbDetail(wbList: VegaQualityWBDetails)

    @Query("SELECT * FROM VegaQualityWBDetails WHERE isSyncStatus = 0 and isOfflineData = 0")
    abstract fun getQualityWeighBridgeDetail(): LiveData<List<VegaQualityWBDetails>>

    @Query("SELECT * FROM VegaCoffeeReceiveLots WHERE tempWBId = :tempWbId")
    abstract fun getLotDetailOfflineLocal(tempWbId: String): LiveData<List<VegaCoffeeReceiveLots>>

    @Query("SELECT * FROM VegaCoffeeLot WHERE tempId = :tempWbId")
    abstract fun getQtyLotDetailOfflineLocal(tempWbId: String): List<VegaCoffeeLot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveQualityLot(lot: List<VegaCoffeeLot>)

    @Query("DELETE FROM VegaQualityWBDetails where weighBridgeId = :wbid")
    abstract fun deleteWBList(wbid: String)

    @Query("UPDATE VegaQualityWBDetails SET isOfflineData = 0 WHERE weighBridgeId = :wbTempId")
    abstract fun updatedeleteStatus(wbTempId: String)

    @Query("DELETE FROM VegaQuality where wbid = :wbid")
    abstract fun deleteOfflineParams(wbid: String)

    @Query("DELETE FROM VegaCoffeeLot where weighBridgeId = :wbid")
    abstract fun deleteOfflineQualityLot(wbid: String)

    @Query("SELECT * FROM VegaCoffeeLot WHERE tempId = :tempWbId")
    abstract fun getLotDetailOfflineQtyLocal(tempWbId: String): LiveData<List<VegaCoffeeLot>>

    @Transaction
    @Query("SELECT * FROM VegaQualityWBDetails WHERE weighBridgeId = :wbid")
    abstract fun getWBWithQualitySingleSync(wbid: String): VegaWeighBridgeWithQualityParams

    @Transaction
    @Query("SELECT * FROM VegaQualityWBDetails WHERE weighBridgeId = :wbid")
    abstract fun getWBWithLotWithQualitySingleSync(wbid: String): VegaIndoWeighBridgeWithQualityParams

    @Query("UPDATE VegaGrnWeighBridgeId SET batchNumber = :batchno WHERE weighBridgeId = :wbid1")
    abstract fun updateBatchToGrn(wbid1: String, batchno: String)

    @Query("DELETE FROM VegaGrnWeighBridgeId where wbTempId = :wbTempId")
    abstract fun deleteWBItem(wbTempId: String)
}
