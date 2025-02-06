package com.olam.warehouse.master.vega.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaOffloadingParameter
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.model.VegaOffloadingParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative

@Dao
abstract class VegaOffloadingDao {


    @Query("SELECT * FROM VegaOffloadingTrucks WHERE isSyncStatus = 0 and isOfflineData = 0")
    abstract suspend fun getOffloadingTruckListDetails(): List<VegaOffloadingTrucks>

    @Query("SELECT * FROM VegaOffloadingTrucks WHERE weighBridgeId = :wbid and isOfflineData = 1")
    abstract fun isWBExist(wbid: String): List<VegaOffloadingTrucks>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertQualityWbDetail(wbList: VegaOffloadingTrucks)

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
    @Transaction
    @Query("SELECT * FROM VegaOffloadingParameter WHERE materialCode = :materialId and wbid = :wbId and pre_sampling =:flag")
    abstract fun getQualityParameterWithData(
        materialId: String,
        wbId: String?,
        flag: String
    ): List<VegaOffloadingParamsWithQualitative>

    @Transaction
    @Query("SELECT * FROM VegaQualityParameter WHERE materialCode = :materialId and preSampling !=:flag")
    abstract fun getQualityParameter(materialId: String, flag: String): LiveData<List<VegaQualityParamsWithQualitative>>

    @Transaction
    @Query("SELECT * FROM VegaQualityParameter WHERE materialCode = :materialId")
    abstract fun getQualityParameterIndiaCoffee(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>

    @Query("DELETE FROM VegaOffloadingTrucks  where weighBridgeId = :wbid")
    abstract fun updateDB(wbid: String)

    suspend fun saveQualityData(qualityParameter: VegaOffloadingParameter, batchNo: String) {
        updateWBListStatus(qualityParameter.wbid, batchNo)
        qualityParameter.wbTempId = qualityParameter.wbid
        insertQuality(qualityParameter)
    }

    @Query("UPDATE VegaOffloadingTrucks SET isOfflineData = 1, batchNumber =:bid where weighBridgeId = :wbid")
    abstract fun updateWBListStatus(wbid: String, bid: String)

    @Transaction
    @Query("SELECT * FROM VegaPackageMaterial")
    abstract fun getMaterials(): LiveData<List<VegaPackageMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertQuality(qualityList: VegaOffloadingParameter)

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

}
