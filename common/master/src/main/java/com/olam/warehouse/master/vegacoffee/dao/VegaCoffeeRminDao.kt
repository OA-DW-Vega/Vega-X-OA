package com.olam.warehouse.master.vegacoffee.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.master.vegacoffee.model.*
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaghana.entity.*
import com.olam.warehouse.master.vegaghana.model.VegaGhanaOfflineFgrn
import com.olam.warehouse.master.vegaghana.model.VegaGhanaOfflineRmin

@Dao
abstract class VegaCoffeeRminDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertRminModel(process: VegaCoffeeRminProcessing)

    @Query("DELETE FROM VegaCoffeeRminLots where batchNumber = :batchNumber")
    abstract fun deleteRminLots(batchNumber: String)

    @Query("DELETE FROM VegaCoffeeRminProcessing where cgfNo = :batchNumber")
    abstract fun deleteRminMaterial(batchNumber: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveLots(item: List<VegaCoffeeRminLots>)

    @Query("SELECT * FROM VegaConfigDetails where rolekey = :role")
    abstract fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

    @Query("SELECT * FROM VegaCocoaMiscellaneous where  roleKey = :role")
    abstract fun getShiftRemarkItems(role: String): LiveData<List<VegaCocoaMiscellaneous>>

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getProducts(): LiveData<List<VegaMaterial>>

    @Query("SELECT * FROM VegaProcessingStage")
    abstract fun getProcessingStage(): LiveData<List<VegaProcessingStage>>

    @Query("DELETE FROM VegaCoffeeRminLots where batchNumber = :batchNumber and cgfNo = :cfgNo")
    abstract fun deleteRminLotsByStage(batchNumber: String, cfgNo: String)

    @Query("DELETE FROM VegaCoffeeRminLots where poNumber = :batchNumber and cgfNo = :cfgNo and baseMaterialCode = :code")
    abstract fun deleteRminAllLotsWithPo(batchNumber: String, cfgNo: String, code: String)


    @Query("UPDATE VegaCoffeeRminLots SET isSyncStatus = :synStatus, status = :status where batchNumber = :batchNumber")
    abstract fun updateSyncStatusLot(batchNumber: String, synStatus: Int, status: Int)

    @Query("UPDATE VegaCoffeeRminProcessing SET isSyncStatus = :synStatus, status = :status where poNumber = :batchNumber")
    abstract fun updateSyncStatusProcess(batchNumber: String, synStatus: Int, status: Int)

    @Query("UPDATE VegaCoffeeFgrnItemsGrades SET synStatus = :synStatus, status = :status where processOrderNo = :poNumber and fgrnId = :rminId and materialCode = :code")
    abstract fun updateSyncStatusGrade(poNumber: String, rminId: String, code: String, synStatus: Int, status: Int)

    @Query("DELETE FROM VegaCoffeeRminLots where cgfNo = :cfgNo and BOMNumber = :bom and baseMaterialCode = :code")
    abstract fun deleteRminAllLotsWithBom(cfgNo: String, bom: String, code: String)

    @Query("DELETE FROM VegaCoffeeRminProcessing where poNumber = :batchNumber and cgfNo = :cfgNo and baseMaterialCode = :code")
    abstract fun deleteRminProcessingWithPo(batchNumber: String, cfgNo: String, code: String)

    @Query("DELETE FROM VegaCoffeeRminProcessing where cgfNo = :cfgNo and bom = :bom and baseMaterialCode = :code")
    abstract fun deleteRminProcessingWithBom(cfgNo: String, bom: String, code: String)

    @Query("SELECT * FROM VegaCoffeeRminLots where poNumber = :poNo and status = 1")
    abstract fun getLotList(
        poNo: String
    ): LiveData<List<VegaCoffeeRminLots>>

    @Query("SELECT * FROM VegaCoffeeRminProcessing where cgfNo = :cgfNo and poNumber = :poNo and bom = :bomNo and materialName = :stage and status = 1")
    abstract fun getRMINProcessing(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        stage: String
    ): LiveData<VegaCoffeeRminProcessing>

    @Query("SELECT * FROM VegaCoffeeFgrnItems where processOrderNo =:processOrderNo and synStatus = 0")
    abstract fun fetchOfflineFgrnList(processOrderNo: String): LiveData<List<VegaCoffeeFgrnItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveFgrnItem(fgrnItem: VegaCoffeeFgrnItems)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveRminItem(fgrnItem: VegaCoffeeRminProcessing)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveFgrnItemGrades(selectedGrades: List<VegaCoffeeFgrnItemsGrades>)

    @Query("SELECT * FROM VegaCoffeeFgrnItemsGrades where processOrderNo =:poNo and fgrnId = :fgrnId")
    abstract fun getOfflineGrades(poNo: String, fgrnId: String): LiveData<List<VegaCoffeeFgrnItemsGrades>>

    @Query("SELECT * FROM VegaCoffeeFgrnItemsGrades where processOrderNo =:poNo and status = 1")
    abstract fun getOfflineGrades(poNo: String): LiveData<List<VegaCoffeeFgrnItemsGrades>>

    @Query("SELECT * FROM VegaCoffeeFgrnItemsGrades where fgrnIdMaterialCode =:fgrnIdWithMatrial and status = 1")
    abstract fun getOfflineGradeWithBags(fgrnIdWithMatrial: String): LiveData<VegaCoffeeFgrnGradesWithBagItems>


    @Query("SELECT * FROM VegaCoffeeFgrnItemsGrades where fgrnIdMaterialCode =:fgrnIdWithMatrial and status = 1")
    abstract fun getRminOfflineGradeWithLotsAndBags(
        fgrnIdWithMatrial: String
    ): LiveData<VegaCoffeeRMINGradesWithBagItems>

    @Query("SELECT * FROM VegaCoffeeFgrnGradesMatrialWeights where fgrnIdMaterialCode =:fgrnIdWithMatrial and batchNumber = :batchNo")
    abstract fun getOfflineGradeWithBagsRmin(
        fgrnIdWithMatrial: String, batchNo: String
    ): List<VegaCoffeeFgrnGradesMatrialWeights>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetails(material: VegaCoffeeFgrnGradesMatrialWeights)

    @Query("DELETE FROM VegaCoffeeFgrnGradesMatrialWeights where id = :id")
    abstract fun deleteBagDetails(id: Int)

    @Query("DELETE FROM VegaCoffeeRminProcessing where cgfNo = :cgfNo and poNumber =:poNo and bom =:bomNo and materialCode=:materialCode")
    abstract fun deleteRMINProcess(cgfNo: String, poNo: String, bomNo: String, materialCode: String)

    @Query("DELETE FROM VegaCoffeeFgrnGradesMatrialWeights where fgrnId = :fgrnId")
    abstract fun deleteAllBagDetails(fgrnId: String)

    @Query("DELETE FROM VegaCoffeeFgrnItemsGrades where fgrnId = :fgrnId")
    abstract fun deleteAllGrades(fgrnId: String)

    @Query("DELETE FROM VegaCoffeeFgrnItemsGrades where fgrnIdMaterialCode = :fgrnIdMaterialCode")
    abstract fun deleteGrade(fgrnIdMaterialCode: String)

    @Query("DELETE FROM VegaCoffeeFgrnGradesMatrialWeights where fgrnIdMaterialCode = :fgrnIdMaterialCode")
    abstract fun deleteBagDetails(fgrnIdMaterialCode: String)

    @Query("DELETE FROM VegaCoffeeFgrnItems where fgrnId = :fgrnId")
    abstract fun deleteFgrnItems(fgrnId: String)

    @Query("SELECT * FROM VegaCoffeeFgrnItems where fgrnId =:fgrnId")
    abstract fun getFgrnItems(fgrnId: String): LiveData<VegaCoffeeFgrnItemWithGrades>

    @Query("SELECT * FROM VegaCoffeeRminProcessing where rminId =:fgrnId and status = 1")
    abstract fun getRminItems(fgrnId: String): VegaCoffeeRminItemWithGrades

    @Query("SELECT * FROM VegaCoffeeRminProcessing where rminId =:fgrnId and status = 1")
    abstract fun getRminItemsLive(fgrnId: String): LiveData<VegaCoffeeRminItemWithGrades>

    @Query("SELECT * FROM VegaCoffeeFgrnItems")
    abstract fun getFgrnItemsAll(): LiveData<List<VegaCoffeeFgrnItemWithGrades>>

    @Query("SELECT  * FROM VegaCoffeeRminProcessing")
    abstract fun getRminItemsAll(): LiveData<List<VegaCoffeeRminItemWithLots>>

    @Query("DELETE FROM VegaCoffeeRminProcessing where rminId = :rminId")
    abstract fun deleteRminItems(rminId: String)

    @Query("DELETE FROM VegaCoffeeRminLots where rminId = :rminId")
    abstract fun deleteRminLotItems(rminId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveFgrnGrade(vegaCoffeeFgrnItemsGrades: VegaCoffeeFgrnItemsGrades)

    @Query("UPDATE VegaCoffeeFgrnItemsGrades SET fgrnIdMaterialCode = :newMaterial WHERE fgrnIdMaterialCode = :fgrnIdMaterialCode")
    abstract fun updateRminGradeBatch(fgrnIdMaterialCode: String, newMaterial: String)

    @Query("UPDATE VegaCoffeeFgrnItemsGrades SET startTime = :startTime WHERE fgrnIdMaterialCode = :fgrnIdMaterialCode")
    abstract fun updateFgrnGradeStartTime(startTime: String, fgrnIdMaterialCode: String)

    @Query("UPDATE VegaCoffeeFgrnItemsGrades SET endTime = :endTime WHERE fgrnIdMaterialCode = :fgrnIdMaterialCode")
    abstract fun updateFgrnGradeEndTime(endTime: String, fgrnIdMaterialCode: String)

    @Query("SELECT COUNT(*) FROM VegaCoffeeFgrnItemsGrades WHERE fgrnIdMaterialCode = :fgrnIdMaterialCode")
    abstract fun getGradeCount(fgrnIdMaterialCode: String): Int

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

    @Query("UPDATE VegaCoffeeFgrnItems SET message = :message, status = :status, synStatus =:synStatus WHERE fgrnId = :fgrnId")
    abstract fun updateFgrnStatus(message: String, status: Int, fgrnId: String, synStatus: Int)

    @Query("UPDATE VegaCoffeeFgrnItems SET shiftSelection = :shiftSelection, remarks = :operatorName WHERE fgrnId = :fgrnId")
    abstract fun updateFgrnShiftStatus(
        fgrnId: String,
        shiftSelection: String,
        operatorName: String
    )

    @Query("SELECT * FROM VegaCoffeeFgrnItems where fgrnId = :fgrnId")
    abstract fun fetchFgrnItem(fgrnId: String): LiveData<VegaCoffeeFgrnItems>

    @Query("SELECT * FROM VegaCoffeeRminProcessing where cgfNo = :cgfNo and poNumber = :poNo and bom = :bomNo and baseMaterialName = :baseMaterialCode")
    abstract fun getRminWithLotList(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        baseMaterialCode: String
    ): LiveData<VegaCoffeeRminItemWithLots>

    @Query("UPDATE VegaCoffeeFgrnGradesMatrialWeights SET batchNumber = :batchNo, isBagConsumed =:isBagConsumed WHERE bagMaterialCode = :bagMaterialCode and fgrnId =:bagId")
    abstract fun updateBatchToBagDetails(
        bagMaterialCode: String,
        batchNo: String,
        bagId: String,
        isBagConsumed: Boolean
    )

    @Query("SELECT * FROM VegaCoffeeRminLots WHERE batchNumber = :batchNumber and status = 1")
    abstract fun validateLotAlreadyAdded(batchNumber: String): VegaCoffeeRminLots

    @Query("SELECT * FROM VegaCoffeeRminProcessing WHERE poNumber = :batchNumber and status = 1")
    abstract fun getProcessingModel(batchNumber: String): VegaCoffeeRminProcessing

    @Query("SELECT * FROM VegaCoffeeRminLots WHERE  poNumber = :poNo and materialCode = :materialCode and status = 1")
    abstract fun getLots(poNo: String, materialCode: String): List<VegaCoffeeRminLots>

    @Query("SELECT * FROM VegaCoffeeRminLots WHERE  poNumber = :poNo and materialCode = :materialCode and status = 1")
    abstract fun getAllLots(poNo: String, materialCode: String): LiveData<List<VegaCoffeeRminLots>>

    @Query("UPDATE VegaCoffeeRminProcessing SET shift = :shift and remark = :remark WHERE rminId = :rminId and poNumber =:poNumber and materialCode=:materialCode")
    abstract fun updateShiftRemarkRmin(
        shift: String,
        remark: String,
        rminId: String,
        poNumber: String,
        materialCode: String
    )

    @Query("UPDATE VegaCoffeeFgrnItemsGrades SET weightToProcess = :weight WHERE fgrnId = :fgrnId and processOrderNo = :poNumber and materialCode = :material")
    abstract fun updateWeighToProcess(weight: String, fgrnId: String, material: String, poNumber: String)

    @Query("SELECT * FROM VegaCoffeeThirdPartyMaterialDetail")
    abstract fun getThirdPartyMaterials(): List<VegaCoffeeThirdPartyMaterialDetail>

    @Query("DELETE FROM VegaCoffeeFgrnGradesMatrialWeights")
    abstract fun deleteAllRminItems()

    @Query("SELECT * FROM VegaCoffeeFgrnGradesMatrialWeights")
    abstract fun getAllFGRNItems(): LiveData<VegaCoffeeFgrnGradesMatrialWeights>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveOfflineRminDetails(rminItem: VegaGhanaOfflineRminData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveOfflineRminLotDetails(rminLotItem: VegaGhanaOfflineRminProcessLotDetails)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveOfflineFgrnDetails(rminItem: VegaGhanaOfflineFgrnData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveOfflineFgrnLotDetails(rminLotItem: VegaGhanaOfflineFgrnProcessLotDetails)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveOfflineRminSelectedLots(rminItem: VegaGhanaOfflineRminLots)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveOfflineRminItem(rminItem: VegaGhanaOfflineRminItems)

    @Query("SELECT * FROM VegaGhanaOfflineRminProcessLotDetails")
    abstract fun getofflineRminItem(): LiveData<List<VegaGhanaOfflineRmin>>

    @Query("SELECT * FROM VegaGhanaOfflineRminLots")
    abstract fun getOfflineRminLots(): LiveData<List<VegaGhanaOfflineRminLots>>

    @Query("SELECT * FROM VegaGhanaOfflineRminItems")
    abstract fun getOfflineRminItems(): LiveData<List<VegaGhanaOfflineRminItems>>

    @Query("DELETE FROM VegaGhanaOfflineRminProcessLotDetails where rminTempId = :tmpWbId")
    abstract fun deleteOfflineRminLotDetails(tmpWbId: String)

    @Query("DELETE FROM VegaGhanaOfflineRminData where rminTempId = :tmpWbId")
    abstract fun deleteOfflineRminData(tmpWbId: String)

    @Query("SELECT * FROM VegaGhanaOfflineFgrnProcessLotDetails")
    abstract fun getofflineFgrnItem(): LiveData<List<VegaGhanaOfflineFgrn>>

    @Query("SELECT * FROM VegaGhanaOfflineFgrnData ")
    abstract fun getofflineFgrnPostItem(): LiveData<List<VegaGhanaOfflineFgrnData>>

    @Query("SELECT * FROM VegaGhanaOfflineRminProcessLotDetails where rminTempId = :rmin")
    abstract fun getofflineRminPostItem(rmin: String): LiveData<List<VegaGhanaOfflineRminProcessLotDetails>>

    @Query("SELECT * FROM VegaGhanaOfflineFgrnProcessLotDetails where fgrnTempId = :fgrn and rminTempId = :rmin")
    abstract fun getofflineFgrnPostLotDetails(fgrn: String, rmin: String): VegaGhanaOfflineFgrnProcessLotDetails

    @Query("DELETE FROM VegaGhanaOfflineFgrnProcessLotDetails where fgrnTempId = :tmpWbId")
    abstract fun deleteOfflineFgrnLotDetails(tmpWbId: String)

    @Query("DELETE FROM VegaGhanaOfflineFgrnProcessLotDetails where status = :status")
    abstract fun deleteSyncedOfflineFgrnLotDetails(status: Int)

    @Query("DELETE FROM VegaGhanaOfflineFgrnData where status = :status")
    abstract fun deleteSyncedOfflineFgrnData(status: Int)

    @Query("DELETE FROM VegaGhanaOfflineFgrnData where fgrnTempId = :tmpWbId")
    abstract fun deleteOfflineFgrnData(tmpWbId: String)

    @Query("SELECT * FROM VegaGhanaProcessingOrder")
    abstract fun getProcessOrders(): LiveData<List<VegaGhanaProcessingOrder>>

    @Query("SELECT * FROM VegaGhanaProcessingOrderDetails where processOrderNo = :poNumber")
    abstract fun getProcessOrderDetails(poNumber: String): LiveData<List<VegaGhanaProcessingOrderDetails>>

    @Query("SELECT * FROM VegaEcuadorDispatchStocks where materialCode = :materialCode")
    abstract fun getStockDetails(materialCode: String): LiveData<List<VegaEcuadorDispatchStocks>>

    @Query("DELETE FROM VegaCoffeeRminLots")
    abstract fun deleteAllRminLots()

    @Query("DELETE FROM VegaCoffeeFgrnItems ")
    abstract fun deleteAllFgrnItems()

    @Query("UPDATE VegaGhanaOfflineRminProcessLotDetails SET fgrnStatus = :status WHERE rminTempId = :rminId")
    abstract fun updateFgrnStatus(status: Boolean, rminId: String)

    @Query("UPDATE VegaEcuadorDispatchStocks SET weight = :weight where batchNumber = :batchNo")
    abstract fun updateStockDetails(weight: String, batchNo: String)

    @Query("UPDATE VegaGhanaProcessingOrder SET rfgrnTotal = :total where processOrderNo = :poNo")
    abstract fun updateFgrnTotal(total: String, poNo: String)

    @Query("SELECT * FROM VegaEcuadorDispatchStocks where batchNumber = :batchNo")
    abstract fun getStockDetailsByBatchNo(batchNo: String): LiveData<VegaEcuadorDispatchStocks>

    @Query("SELECT * FROM VegaProcessingRminBoms where inputMaterialCode = :materialcode")
    abstract fun getRminBomDetails(materialcode: String): LiveData<List<VegaProcessingRminBoms>>

    @Query("SELECT * FROM VegaGhanaOfflineRminData where rminTempId = :RminTempId")
    abstract fun getofflineRminData(
        RminTempId: String
    ): LiveData<List<VegaGhanaOfflineRminData>>

}
