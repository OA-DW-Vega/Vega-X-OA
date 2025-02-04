package com.olam.warehouse.master.vega.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails

/**
 * Created by Baskaran Kannan on 4/1/2020.
 */
@Dao
abstract class VegaInventoryDao {

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getProducts(): LiveData<List<VegaMaterial>>

    fun updateDeletedItem(wbid: String) {
        updateDeletedWBItem(wbid)
        deleteOfflineParams(wbid)
    }

    @Transaction
    @Query("UPDATE VegaQualityWBDetails SET isOfflineData = 0, isSyncStatus =0 where weighBridgeId = :wbid")
    abstract fun updateDeletedWBItem(wbid: String)

    @Query("DELETE FROM VegaQuality where wbid = :wbid")
    abstract fun deleteOfflineParams(wbid: String)

    @Query("SELECT * FROM VegaConfigDetails where rolekey = :role")
    abstract fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

    @Query("SELECT * FROM VegaUomDetails")
    abstract fun getuomDetail(): LiveData<List<VegaUomDetails>>

    suspend fun saveQualityData(qualityParameter: VegaQuality, batchno: String) {
        updateWBListStatus(qualityParameter.wbid, batchno)
        qualityParameter.wbTempId = qualityParameter.wbid
        insertQuality(qualityParameter)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertWeighBridge(storageLocation: VegaQualityWBDetails)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertQuality(qualityList: VegaQuality)

    @Query("UPDATE VegaQualityWBDetails SET isOfflineData = 1, batchNumber =:bid where weighBridgeId = :wbid")
    abstract fun updateWBListStatus(wbid: String, bid: String)
}
