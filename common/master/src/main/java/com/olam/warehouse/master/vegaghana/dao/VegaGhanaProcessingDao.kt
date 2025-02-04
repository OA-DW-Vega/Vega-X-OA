package com.olam.warehouse.master.vegaghana.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olam.warehouse.master.vega.entity.VegaGhanaPurchaseOrderMaterialModel
import com.olam.warehouse.master.vega.entity.VegaProcessingRminBoms
import com.olam.warehouse.master.vega.model.VegaGhanaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineFgrnData
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineFgrnProcessLotDetails
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminProcessLotDetails

@Dao
abstract class VegaGhanaProcessingDao {

    @Query("SELECT * FROM VegaGhanaOfflineFgrnData where fgrnTempId = :fgrn")
    abstract fun getofflineFgrnPostItem(fgrn: String): VegaGhanaOfflineFgrnData

    @Query("SELECT * FROM VegaGhanaOfflineRminProcessLotDetails where rminTempId = :rmin")
    abstract fun getofflineRminPostItem(rmin: String): List<VegaGhanaOfflineRminProcessLotDetails>

    @Query("SELECT * FROM VegaGhanaOfflineFgrnProcessLotDetails where fgrnTempId = :fgrn and rminTempId = :rmin")
    abstract fun getofflineFgrnPostLotDetails(fgrn: String, rmin: String): VegaGhanaOfflineFgrnProcessLotDetails

    @Query("UPDATE VegaGhanaOfflineFgrnProcessLotDetails SET status = :status, syncStatusMsg = :msg WHERE fgrnTempId = :fgrnId and rminTempId = :rmin ")
    abstract fun updateSuccessResponse(
        fgrnId: String,
        rmin: String,
        msg: String,
        status: Int
    )

    @Query("UPDATE VegaGhanaOfflineFgrnData SET status = :status WHERE fgrnTempId = :fgrnId and rminTempId = :rmin ")
    abstract fun updateStatusResponse(
        fgrnId: String,
        rmin: String,
        status: Int
    )

    @Query("DELETE FROM VegaGhanaOfflineRminProcessLotDetails where rminTempId = :tmpWbId")
    abstract fun deleteOfflineRminLotDetails(tmpWbId: String)

    @Query("DELETE FROM VegaGhanaOfflineRminData where rminTempId = :tmpWbId")
    abstract fun deleteOfflineRminData(tmpWbId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveGhanaLots(item: List<VegaGhanaCocoaDispatchLots>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveGhanaLots(item: VegaGhanaCocoaDispatchLots)

    @Query("SELECT * FROM VegaGhanaCocoaDispatchLots WHERE weighBridgeId = :material and isSyncStatus = 0")
    abstract fun getGhanaLots(material: String): LiveData<List<VegaGhanaCocoaDispatchLots>>

    @Query("SELECT * FROM VegaGhanaCocoaDispatchLots WHERE batchNumber = :batchNumber and isSyncStatus = 0")
    abstract fun validateGhanaLotAlreadyAdded(batchNumber: String): VegaGhanaCocoaDispatchLots

    @Query("SELECT * FROM VegaGhanaCocoaDispatchLots WHERE  weighBridgeId = :wbId")
    abstract fun getDispatchLots(wbId: String): LiveData<List<VegaGhanaCocoaDispatchLots>>

    @Query("SELECT * FROM VegaCocoaDispatchWB where weighBridgeId = :weighBrideId and isSyncStatus = 0")
    abstract fun getGhanaMtntWithLotSingle(weighBrideId: String): VegaGhanaCocoaMtntWithLots

    @Query("SELECT * FROM VegaCocoaDispatchWB where plantId = :whId and purchaseDocNum = :stoNo and weighBridgeType =:type and isSyncStatus = 0 ")
    abstract fun getGhanaMtntWithLotSingle(
        whId: String,
        stoNo: String,
        type: String
    ): VegaGhanaCocoaMtntWithLots

    @Query("SELECT * FROM VegaCocoaDispatchWB where weighBridgeId = :weighBrideId and isSyncStatus = 0")
    abstract fun getGhanaMtntWithLotAndMaterial(weighBrideId: String): LiveData<VegaGhanaCocoaMtntWithLots>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertGhanaMaterialDetail(list: List<VegaGhanaPurchaseOrderMaterialModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveRminBomDetails(list: List<VegaProcessingRminBoms>)


}
