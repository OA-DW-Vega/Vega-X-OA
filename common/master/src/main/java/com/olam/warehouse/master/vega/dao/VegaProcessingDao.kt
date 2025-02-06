package com.olam.warehouse.master.vega.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaRminBomWithLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItemsGrades
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaFgrnGradesWithBagItems
import com.olam.warehouse.presentation.enums.Status

@Dao
abstract class VegaProcessingDao {
    @Query("SELECT * FROM VegaMaterial")
    abstract fun getProducts(): LiveData<List<VegaMaterial>>

    @Query("SELECT * FROM VegaProcessingStage")
    abstract fun getProcessingStage(): LiveData<List<VegaProcessingStage>>

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

    @Query("DELETE FROM VegaDispatchLots where materialCode = :material")
    abstract fun deleteStocks(material: String)

    @Query("SELECT * FROM VegaDispatchLots WHERE materialCode = :material")
    abstract suspend fun getStocks(material: String): List<VegaDispatchLots>

    @Query("SELECT * FROM VegaDispatchLots WHERE materialCode = :material")
    abstract fun getStocksOfflineSingle(material: String): LiveData<List<VegaDispatchLots>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveStock(item: List<VegaCocoaRminLots>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveRminBom(prepareRminCreatePo: VegaProcessingCreatePoReq)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveProcessingLots(processLots: ProcessingLotDetails)

    @Query("UPDATE VegaProcessingCreatePoReq SET syncStatusMsg = :msg, isSynced = :syncStatus, status = :status where batchNumber = :batchNo")
    abstract fun updateRminData(batchNo: String?, msg: String, syncStatus: Boolean, status: Status)

    @Query("SELECT * FROM VegaProcessingCreatePoReq")
    abstract fun getRminWithLots(): LiveData<List<VegaRminBomWithLots>>

    @Query("DELETE FROM VegaProcessingCreatePoReq where batchNumber = :batchNumber")
    abstract fun deleteRmin(batchNumber: String)

    @Query("DELETE FROM ProcessingLotDetails where batchNumber = :batchNumber")
    abstract fun deleteRminLots(batchNumber: String)

    @Query("SELECT * FROM VegaProcessingCreatePoReq where batchNumber =:batchNo")
    abstract fun getSingleRminLots(batchNo: String): VegaRminBomWithLots

    @Query("SELECT * FROM VegaConfigDetails where rolekey = :role")
    abstract fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

    @Query("SELECT * FROM VegaCocoaFgrnItems where processOrderNo =:processOrderNo")
    abstract fun fetchOfflineFgrnList(processOrderNo: String): LiveData<List<VegaCocoaFgrnItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveFgrnItem(fgrnItem: VegaCocoaFgrnItems)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveFgrnItemGrades(selectedGrades: List<VegaCocoaFgrnItemsGrades>)

    @Query("SELECT * FROM VegaCocoaFgrnItemsGrades where processOrderNo =:poNo and fgrnId = :fgrnId")
    abstract fun getOfflineGrades(poNo: String, fgrnId: String): LiveData<List<VegaCocoaFgrnItemsGrades>>

    @Query("SELECT * FROM VegaCocoaFgrnItemsGrades where fgrnIdMaterialCode =:fgrnIdWithMatrial")
    abstract fun getOfflineGradeWithBags(fgrnIdWithMatrial: String): LiveData<VegaCocoaFgrnGradesWithBagItems>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetails(material: VegaCocoaFgrnGradesMatrialWeights)

    @Query("DELETE FROM VegaCocoaFgrnGradesMatrialWeights where id = :id")
    abstract fun deleteBagDetails(id: Int)

}
