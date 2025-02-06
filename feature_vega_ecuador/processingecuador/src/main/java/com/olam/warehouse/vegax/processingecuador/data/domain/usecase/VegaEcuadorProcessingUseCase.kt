package com.olam.warehouse.vegax.processingecuador.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaProcessingFgrnPost
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminData
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminProcessLotDetails
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaOfflineRmin
import com.olam.warehouse.vegax.processingecuador.data.domain.model.EcuadorProcessingUpdateFgrnSequencePost
import com.olam.warehouse.vegax.processingecuador.data.domain.model.VegaEcuadorProcessingCreatePoReq
import com.olam.warehouse.vegax.processingecuador.data.domain.model.VegaEcuadorProcessingRminBomPost
import com.olam.warehouse.vegax.processingecuador.data.domain.model.VegaEcuadorRminProcessingPost
import com.olam.warehouse.vegax.processingecuador.data.repo.VegaEcuadorProcessingRepository

class VegaEcuadorProcessingUseCase(private val repository: VegaEcuadorProcessingRepository) {
    suspend fun getGrades() = repository.getGrades()
    suspend fun getStages() = repository.getStages()
    suspend fun getBom(bomPostReq: VegaEcuadorProcessingRminBomPost) =
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
    suspend fun saveFgrnItem(
        fgrnItem: VegaCocoaFgrnItems,
        selectedGrades: List<VegaCocoaFgrnItemsGrades>,
        removeItem: ArrayList<String>
    ) =
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

    suspend fun postRminDetails(model: VegaEcuadorRminProcessingPost) = repository.postRminDetails(
        model
    )

    suspend fun getShiftRemarkItems(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        repository.getShiftRemarks(role)

    suspend fun deleteItemInAllTable(fgrnId: String) = repository.deleteItemInAllTable(fgrnId)
    suspend fun getFgrnItems(fgrnId: String) = repository.getFgrnItems(fgrnId)
    suspend fun saveFgrnGrade(vegaIndiaCoffeeFgrnItemsGrades: VegaCocoaFgrnItemsGrades) =
        repository.saveFgrnGrade(vegaIndiaCoffeeFgrnItemsGrades)

    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun postFgrnDetails(poReq: VegaCocoaProcessingFgrnPost) =
        repository.postFgrnDetails(poReq)

    suspend fun updateFgrnStatus(message: String, status: Int, fgrnId: String) =
        repository.updateFgrnStatus(message, status, fgrnId)

    suspend fun updateFgrnShiftStatus(
        fgrnId: String,
        shiftSelection: String,
        operatorName: String
    ) =
        repository.updateFgrnShiftStatus(fgrnId, shiftSelection, operatorName)

    suspend fun fetchFgrnItem(fgrnId: String): LiveData<VegaCocoaFgrnItems> =
        repository.fetchFgrnItem(fgrnId)

    suspend fun postCreatePo(bomPostReq: VegaEcuadorProcessingCreatePoReq) =
        repository.postCreatePo(
            bomPostReq
        )

    suspend fun getRminWithLotList(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        baseMaterialCode: String
    ) = repository.getRminWithLotList(cgfNo, poNo, bomNo, baseMaterialCode)

    suspend fun getStockList(materialList: ArrayList<String>) =
        repository.getStockList(materialList)

    suspend fun updateBatchToBagDetails(bagMaterialCode: String, batchNo: String, bagId: String) =
        repository.updateBatchToBagDetails(bagMaterialCode, batchNo, bagId)

    suspend fun getInventoryList(material: String) = repository.getInventoryDetails(material)
    suspend fun getProducts() = repository.getProducts()
    suspend fun getQualityParams(charge: String, material: String, whId: String) =
        repository.getQualityParams(charge, material, whId)

    suspend fun getStorageLocations() = repository.getStoragetLocations()
    suspend fun getOfflineRminItem(): LiveData<List<VegaNicaraguaOfflineRmin>> =
        repository.getOfflineRminItem()

    suspend fun saveOfflineRminSelectedLots(rminLotData: VegaNicOfflineRminLots) =
        repository.saveOfflineRminSelectedLots(rminLotData)

    suspend fun saveOfflineRminDetails(rminData: VegaNicOfflineRminData) =
        repository.saveOfflineRminDetails(rminData)

    suspend fun saveOfflineRminLotDetails(rminLotData: VegaNicOfflineRminProcessLotDetails) =
        repository.saveOfflineRminLotDetails(rminLotData)

    suspend fun getOfflineRminLots(po: String): LiveData<List<VegaNicOfflineRminLots>> =
        repository.getOfflineRminLots(po)

    suspend fun deleteOfflineRminLot(batchNo: String) = repository.deleteOfflineRminLot(batchNo)

    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String) =
        repository.getPreSamplingQualityList(batchNo, materialId)

    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParams(materialId, valueExist, wbId)

    suspend fun updateFgrnSequence(post: EcuadorProcessingUpdateFgrnSequencePost) =
        repository.updateFgrnSequence(post)


}

