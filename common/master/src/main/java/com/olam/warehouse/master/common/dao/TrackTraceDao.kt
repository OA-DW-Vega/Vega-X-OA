package com.olam.warehouse.master.common.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.olam.warehouse.master.vega.entity.TrackTraceSourceLotDetails
import com.olam.warehouse.master.vega.entity.TrackTraceTransactionIdDetails
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaTrackTraceFarmerData
import com.olam.warehouse.master.vega.entity.VegaVendor

@Dao
abstract class TrackTraceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSingleFarmerData(farmerData: VegaTrackTraceFarmerData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMultipleSourceLotDetails(sourceLotData: List<TrackTraceSourceLotDetails>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract  fun insertMultipleTransIdList(transIdData: List<TrackTraceTransactionIdDetails>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveFarmerLessTransactionDetails(transactionIdDetails: List<TrackTraceTransactionIdDetails>)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMultipleFarmerData(farmerData: List<VegaTrackTraceFarmerData>)

    @Transaction
    @Query("DELETE FROM VegaTrackTraceFarmerData")
    abstract fun clearTTFarmerData()

    @Query("DELETE FROM TrackTraceSourceLotDetails")
    abstract fun clearTTSourceLotDetails()

    @Query("DELETE FROM TrackTraceTransactionIdDetails")
    abstract fun clearTTTransactionIdDetails()

    @Query("SELECT * FROM VegaTrackTraceFarmerData")
    abstract fun getFarmerList(): LiveData<List<VegaTrackTraceFarmerData>>

    @Query("SELECT * FROM VegaVendor")
    abstract fun getSuppliers(): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaVendor where purchaseOrgType =:purchaseOrgType")
    abstract fun getSuppliers(purchaseOrgType: String): LiveData<List<VegaVendor>>

    @Query("SELECT * FROM VegaTrackTraceFarmerData where farmerId =:farmerId")
    abstract fun getFarmerEudrDetails(farmerId: String): LiveData<VegaTrackTraceFarmerData>

    @Query("SELECT * FROM TrackTraceSourceLotDetails where sourceLotId =:sourceLotId")
    abstract fun getOfflineSourceLotDetails(sourceLotId: String): LiveData<TrackTraceSourceLotDetails>

    @Query("SELECT * FROM TrackTraceSourceLotDetails")
    abstract fun getAllSourceLotIdList(): LiveData<List<TrackTraceSourceLotDetails>>

    @Query("SELECT * FROM TrackTraceTransactionIdDetails")
    abstract fun getAllTransIdList(): LiveData<List<TrackTraceTransactionIdDetails>>


    @Query("SELECT * FROM TrackTraceTransactionIdDetails where dwTransactionId =:transactionId")
    abstract fun getOfflineTransactionIdDetails(transactionId: String): LiveData<TrackTraceTransactionIdDetails>

    fun clearMasterData(){
        clearTTSourceLotDetails()
        clearTTFarmerData()
        clearTTTransactionIdDetails()
    }

}
