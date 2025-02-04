package com.olam.warehouse.master.vegacocoa.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaFgrnItemWithGrades
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaRminItemWithLots
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots

@Dao
abstract class VegaCocoaRMinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertRminModel(process: VegaCocoaRminProcessing)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertRminModelNew(process: VegaCocoaRminProcessing)


    @Query("DELETE FROM VegaCocoaRminLots where batchNumber = :batchNumber")
    abstract fun deleteRminLots(batchNumber: String)

    @Query("DELETE FROM VegaCocoaRminProcessing where cgfNo = :batchNumber")
    abstract fun deleteRminMaterial(batchNumber: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveLots(item: MutableList<VegaCocoaRminLots>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveLotsNew(item: List<VegaCocoaRminLots>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveBatches(item: List<VegaCocoaRminLots>)

    @Query("SELECT * FROM VegaConfigDetails where rolekey = :role")
    abstract fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

    @Query("SELECT * FROM VegaCocoaMiscellaneous where  roleKey = :role")
    abstract fun getShiftRemarkItems(role: String): LiveData<List<VegaCocoaMiscellaneous>>

    @Query("SELECT * FROM VegaMaterial")
    abstract fun getProducts(): LiveData<List<VegaMaterial>>

    @Query("SELECT * FROM VegaProcessingStage")
    abstract fun getProcessingStage(): LiveData<List<VegaProcessingStage>>

    @Query("DELETE FROM VegaCocoaRminLots where batchNumber = :batchNumber and cgfNo = :cfgNo")
    abstract fun deleteRminLotsByStage(batchNumber: String, cfgNo: String)

    @Query("UPDATE VegaCoffeeFgrnItemsGrades SET weightToProcess = :weight WHERE fgrnId = :fgrnId and processOrderNo = :poNumber and materialCode = :material")
    abstract fun updateWeighToProcess(
        weight: String,
        fgrnId: String,
        material: String,
        poNumber: String
    )

    @Query("DELETE FROM VegaCocoaRminLots where poNumber = :batchNumber and cgfNo = :cfgNo and baseMaterialCode = :code")
    abstract fun deleteRminAllLotsWithPo(batchNumber: String, cfgNo: String, code: String)

    @Query("DELETE FROM VegaCocoaRminLots where cgfNo = :cfgNo and BOMNumber = :bom and baseMaterialCode = :code")
    abstract fun deleteRminAllLotsWithBom(cfgNo: String, bom: String, code: String)

    @Query("DELETE FROM VegaCocoaRminProcessing where poNumber = :batchNumber and cgfNo = :cfgNo and baseMaterialCode = :code")
    abstract fun deleteRminProcessingWithPo(batchNumber: String, cfgNo: String, code: String)

    @Query("DELETE FROM VegaCocoaRminProcessing where cgfNo = :cfgNo and bom = :bom and baseMaterialCode = :code")
    abstract fun deleteRminProcessingWithBom(cfgNo: String, bom: String, code: String)

    @Query("DELETE FROM VegaCocoaRminProcessing where cgfNo = :cfgNo and bom = :bom and baseMaterialName = :code")
    abstract fun deleteRminProcessingIndiaCoffeeWithBom(cfgNo: String, bom: String, code: String)

    @Query("DELETE FROM VegaCocoaRminProcessing where poNumber = :batchNumber and cgfNo = :cfgNo and baseMaterialName = :code")
    abstract fun deleteRminProcessingIndiaCoffeeWithPo(batchNumber: String, cfgNo: String, code: String)

    @Query("SELECT * FROM VegaCocoaRminLots where cgfNo = :cgfNo and poNumber = :poNo and BOMNumber = :bomNo and baseMaterialCode = :stage")
    abstract fun getLotList(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        stage: String
    ): LiveData<List<VegaCocoaRminLots>>

    @Query("SELECT * FROM VegaCocoaRminProcessing where cgfNo = :cgfNo and poNumber = :poNo and bom = :bomNo and materialName = :stage")
    abstract fun getRMINProcessing(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        stage: String
    ): LiveData<VegaCocoaRminProcessing>

    @Query("SELECT * FROM VegaCocoaFgrnItems where processOrderNo =:processOrderNo and synStatus = 0")
    abstract fun fetchOfflineFgrnList(processOrderNo: String): LiveData<List<VegaCocoaFgrnItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveFgrnItem(fgrnItem: VegaCocoaFgrnItems)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveFgrnItemGrades(selectedGrades: List<VegaCocoaFgrnItemsGrades>)

    @Query("SELECT * FROM VegaCocoaFgrnItemsGrades where processOrderNo =:poNo and fgrnId = :fgrnId")
    abstract fun getOfflineGrades(
        poNo: String,
        fgrnId: String
    ): LiveData<List<VegaCocoaFgrnItemsGrades>>

    @Query("SELECT * FROM VegaCocoaFgrnItemsGrades where fgrnIdMaterialCode =:fgrnIdWithMatrial")
    abstract fun getOfflineGradeWithBags(fgrnIdWithMatrial: String): LiveData<VegaCocoaFgrnGradesWithBagItems>

    @Query("SELECT * FROM VegaCoffeeRminLots WHERE  poNumber = :poNo and materialCode = :materialCode and status = 1")
    abstract fun getLots(poNo: String, materialCode: String): List<VegaCoffeeRminLots>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetails(material: VegaCocoaFgrnGradesMatrialWeights)

    @Query("DELETE FROM VegaCocoaFgrnGradesMatrialWeights where id = :id")
    abstract fun deleteBagDetails(id: Int)

    @Query("DELETE FROM VegaCocoaRminProcessing where cgfNo = :cgfNo and poNumber =:poNo and bom =:bomNo and materialCode=:materialCode")
    abstract fun deleteRMINProcess(cgfNo: String, poNo: String, bomNo: String, materialCode: String)

    @Query("DELETE FROM VegaCocoaFgrnGradesMatrialWeights where fgrnId = :fgrnId")
    abstract fun deleteAllBagDetails(fgrnId: String)

    @Query("DELETE FROM VegaCocoaFgrnItemsGrades where fgrnId = :fgrnId")
    abstract fun deleteAllGrades(fgrnId: String)

    @Query("DELETE FROM VegaCocoaFgrnItemsGrades where fgrnIdMaterialCode = :fgrnIdMaterialCode")
    abstract fun deleteGrade(fgrnIdMaterialCode: String)

    @Query("DELETE FROM VegaCocoaFgrnGradesMatrialWeights where fgrnIdMaterialCode = :fgrnIdMaterialCode")
    abstract fun deleteBagDetails(fgrnIdMaterialCode: String)

    @Query("DELETE FROM VegaCocoaFgrnItems where fgrnId = :fgrnId")
    abstract fun deleteFgrnItems(fgrnId: String)

    @Query("SELECT * FROM VegaCocoaFgrnItems where fgrnId =:fgrnId")
    abstract fun getFgrnItems(fgrnId: String): LiveData<VegaCocoaFgrnItemWithGrades>

    @Query("SELECT * FROM VegaCoffeeRminLots WHERE batchNumber = :batchNumber and status = 1")
    abstract fun validateLotAlreadyAdded(batchNumber: String): VegaCoffeeRminLots

    @Query("SELECT * FROM VegaCocoaFgrnItems")
    abstract fun getFgrnItemsAll(): LiveData<List<VegaCocoaFgrnItemWithGrades>>

    @Query("SELECT  * FROM VegaCocoaRminProcessing")
    abstract fun getRminItemsAll(): LiveData<List<VegaCocoaRminItemWithLots>>

    @Query("DELETE FROM VegaCocoaRminProcessing where rminId = :rminId")
    abstract fun deleteRminItems(rminId: String)

    @Query("DELETE FROM VegaCocoaRminLots where rminId = :rminId")
    abstract fun deleteRminLotItems(rminId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveFgrnGrade(vegaCocoaFgrnItemsGrades: VegaCocoaFgrnItemsGrades)

    @Query("UPDATE VegaCocoaFgrnItemsGrades SET startTime = :startTime WHERE fgrnIdMaterialCode = :fgrnIdMaterialCode")
    abstract fun updateFgrnGradeStartTime(startTime: String, fgrnIdMaterialCode: String)

    @Query("UPDATE VegaCocoaFgrnItemsGrades SET endTime = :endTime WHERE fgrnIdMaterialCode = :fgrnIdMaterialCode")
    abstract fun updateFgrnGradeEndTime(endTime: String, fgrnIdMaterialCode: String)

    @Query("SELECT COUNT(*) FROM VegaCocoaFgrnItemsGrades WHERE fgrnIdMaterialCode = :fgrnIdMaterialCode")
    abstract fun getGradeCount(fgrnIdMaterialCode: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveBagDetailsNew(material: VegaCoffeeFgrnGradesMatrialWeights)

    @Query("SELECT * FROM VegaCoffeeFgrnGradesMatrialWeights where fgrnIdMaterialCode =:fgrnIdWithMatrial and batchNumber = :batchNo")
    abstract fun getOfflineGradeWithBagsRmin(
        fgrnIdWithMatrial: String, batchNo: String
    ): List<VegaCoffeeFgrnGradesMatrialWeights>

    @Query("SELECT * FROM VegaCustomStLocation")
    abstract fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

    @Query("UPDATE VegaCocoaFgrnItems SET message = :message, status = :status, synStatus =:synStatus WHERE fgrnId = :fgrnId")
    abstract fun updateFgrnStatus(message: String, status: Int, fgrnId: String, synStatus: Int)

    @Query("UPDATE VegaCocoaFgrnItems SET shiftSelection = :shiftSelection, operatorName = :operatorName WHERE fgrnId = :fgrnId")
    abstract fun updateFgrnShiftStatus(
        fgrnId: String,
        shiftSelection: String,
        operatorName: String
    )

    @Query("SELECT * FROM VegaCocoaFgrnItems where fgrnId = :fgrnId")
    abstract fun fetchFgrnItem(fgrnId: String): LiveData<VegaCocoaFgrnItems>

    @Query("SELECT * FROM VegaCocoaRminProcessing where cgfNo = :cgfNo and poNumber = :poNo and bom = :bomNo and baseMaterialName = :baseMaterialCode")
    abstract fun getRminWithLotList(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        baseMaterialCode: String
    ): LiveData<VegaCocoaRminItemWithLots>

    @Query("UPDATE VegaCocoaFgrnGradesMatrialWeights SET batchNumber = :batchNo WHERE bagMaterialCode = :bagMaterialCode and fgrnId =:bagId")
    abstract fun updateBatchToBagDetails(bagMaterialCode: String, batchNo: String, bagId: String)

}
