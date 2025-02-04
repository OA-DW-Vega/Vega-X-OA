package com.olam.warehouse.vegax.processingcocoa.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaProcessingFgrnPost
import com.olam.warehouse.vegax.processingcocoa.data.domain.model.VegaCocoProcessingRminBomPost
import com.olam.warehouse.vegax.processingcocoa.data.domain.model.VegaCocoaProcessingCreatePoReq
import com.olam.warehouse.vegax.processingcocoa.data.domain.model.VegaCocoaRminProcessingPost
import com.olam.warehouse.vegax.processingcocoa.data.repo.VegaCocoaProcessingRepository

class VegaCocoaProcessingUseCase(private val repository: VegaCocoaProcessingRepository) {
    suspend fun getGrades() = repository.getGrades()
    suspend fun getStages() = repository.getStages()
    suspend fun getBom(bomPostReq: VegaCocoProcessingRminBomPost) =
        repository.getBom(bomPostReq)

    suspend fun getStocks(material: String) = repository.getStocks(material)

    suspend fun getFgrnPoDetailsList(
        auart: String,
        cfgNo: String,
        fevor: String
    ) = repository.getFgrnPoDetailsList(auart, cfgNo, fevor)

    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repository.getConfigItems(role)
    suspend fun fetchFgrnPoDetailsList(stageFevor: String, cfgNo: String, auart: String) =
        repository.fetchFgrnPoDetailsList(stageFevor, cfgNo, auart)

    suspend fun fetchOfflineFgrnList(processOrderNo: String) = repository.fetchOfflineFgrnList(processOrderNo)

    suspend fun getPoGrades(poNo: String, rmin: Boolean) = repository.getPoGrades(poNo, rmin)
    suspend fun saveFgrnItem(fgrnItem: VegaCocoaFgrnItems,
        selectedGrades: List<VegaCocoaFgrnItemsGrades>,
        removeItem: ArrayList<String>) =
        repository.saveFgrnItem(fgrnItem, selectedGrades, removeItem)

    suspend fun getOfflineGrades(poNo: String, fgrnId: String) = repository.getOfflineGrades(poNo, fgrnId)
    suspend fun getOfflineGradeWithBags(fgrnIdWithMatrial: String) =
        repository.getOfflineGradeWithBags(fgrnIdWithMatrial)

    suspend fun saveBagDetails(
        material: VegaCocoaFgrnGradesMatrialWeights,
        currentGrade: VegaCocoaFgrnItemsGrades
    ) = repository.saveBagDetails(material, currentGrade)
    suspend fun deleteBagDetails(id: Int) = repository.deleteBagDetails(id)
    suspend fun saveLotInDispatch(
        list: MutableList<VegaCocoaRminLots>,
        model: VegaCocoaRminProcessing
    ) =
        repository.saveLots(list, model)

    suspend fun saveRMINProcess(model: VegaCocoaRminProcessing) =
        repository.saveRminProcess(model)

    suspend fun deleteLot(batchNo: String, cfgNo: String) = repository.deleteLot(batchNo, cfgNo)
    suspend fun deleteAllLot(batchNo: String, cfgNo: String, bomNo: String, baseMaterialCode: String) =
        repository.deleteAllLot(batchNo, cfgNo, bomNo, baseMaterialCode)

    suspend fun getLotList(cgfNo: String, poNo: String, bomNo: String, baseMaterialCode: String) =
        repository.getLotList(cgfNo, poNo, bomNo, baseMaterialCode)

    suspend fun getRMINProcess(cgfNo: String, poNo: String, bomNo: String, baseMaterialCode: String) =
        repository.getRMINProcess(cgfNo, poNo, bomNo, baseMaterialCode)

    suspend fun postRminDetails(model: VegaCocoaRminProcessingPost) = repository.postRminDetails(model)

    suspend fun getShiftRemarkItems(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        repository.getShiftRemarks(role)

    suspend fun deleteItemInAllTable(fgrnId: String) = repository.deleteItemInAllTable(fgrnId)
    suspend fun getFgrnItems(fgrnId: String) = repository.getFgrnItems(fgrnId)
    suspend fun saveFgrnGrade(vegaCocoaFgrnItemsGrades: VegaCocoaFgrnItemsGrades) =
        repository.saveFgrnGrade(vegaCocoaFgrnItemsGrades)

    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun postFgrnDetails(poReq: VegaCocoaProcessingFgrnPost) = repository.postFgrnDetails(poReq)
    suspend fun updateFgrnStatus(message: String, status: Int, fgrnId: String) =
        repository.updateFgrnStatus(message, status, fgrnId)

    suspend fun updateFgrnShiftStatus(fgrnId: String, shiftSelection: String, operatorName: String) =
        repository.updateFgrnShiftStatus(fgrnId, shiftSelection, operatorName)

    suspend fun fetchFgrnItem(fgrnId: String): LiveData<VegaCocoaFgrnItems> = repository.fetchFgrnItem(fgrnId)

    suspend fun postCreatePo(bomPostReq: VegaCocoaProcessingCreatePoReq) = repository.postCreatePo(bomPostReq)
    suspend fun getRminWithLotList(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        baseMaterialCode: String
    ) = repository.getRminWithLotList(cgfNo, poNo, bomNo, baseMaterialCode)

    suspend fun getStockList(materialList: ArrayList<String>) = repository.getStockList(materialList)
    suspend fun updateBatchToBagDetails(bagMaterialCode: String, batchNo: String, bagId: String) =
        repository.updateBatchToBagDetails(bagMaterialCode, batchNo, bagId)
}

