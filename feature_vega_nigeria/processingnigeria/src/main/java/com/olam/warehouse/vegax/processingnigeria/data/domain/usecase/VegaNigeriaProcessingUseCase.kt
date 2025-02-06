package com.olam.warehouse.vegax.processingnigeria.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.vegax.processingnigeria.data.domain.model.VegaNigeriaProcessingCreatePoReq
import com.olam.warehouse.vegax.processingnigeria.data.domain.model.VegaNigeriaProcessingFgrnPost
import com.olam.warehouse.vegax.processingnigeria.data.domain.model.VegaNigeriaRminBomPost
import com.olam.warehouse.vegax.processingnigeria.data.repo.VegaNigeriaProcessingRepository

class VegaNigeriaProcessingUseCase(val repository: VegaNigeriaProcessingRepository) {
    suspend fun getGrades() = repository.getGrades()
    suspend fun getStages() = repository.getStages()
    suspend fun getBom(bomPostReq: VegaNigeriaRminBomPost) =
        repository.getBom(bomPostReq)

    suspend fun getStocks(material: String) = repository.getStocks(material)

    suspend fun getFgrnPoDetailsList(
        auart: String,
        cfgNo: String,
        fevor: String
    ) = repository.getFgrnPoDetailsList(auart, cfgNo, fevor)

    suspend fun getSubStages(material: String, werks: String) =
        repository.getSubStages(material, werks)

    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> =
        repository.getConfigItems(role)

    suspend fun fetchFgrnPoDetailsList(stageFevor: String, cfgNo: String, auart: String) =
        repository.fetchFgrnPoDetailsList(stageFevor, cfgNo, auart)

    suspend fun fetchOfflineFgrnList(processOrderNo: String) =
        repository.fetchOfflineFgrnList(processOrderNo)

    suspend fun getPoGrades(poNo: String, rmin: Boolean) = repository.getPoGrades(poNo, rmin)
    suspend fun saveFgrnItem(
        fgrnItem: VegaCoffeeFgrnItems,
        selectedGrades: List<VegaCoffeeFgrnItemsGrades>,
        removeItem: ArrayList<String>
    ) =
        repository.saveFgrnItem(fgrnItem, selectedGrades, removeItem)

    suspend fun saveRminItem(
        fgrnItem: VegaCoffeeRminProcessing,
        selectedGrades: List<VegaCoffeeFgrnItemsGrades>,
        removeItem: ArrayList<String>
    ) =
        repository.saveRminItem(fgrnItem, selectedGrades, removeItem)

    suspend fun getOfflineGrades(poNo: String, fgrnId: String) = repository.getOfflineGrades(poNo, fgrnId)
    suspend fun getOfflineGrades(poNo: String) = repository.getOfflineGrades(poNo)
    suspend fun getOfflineGradeWithBags(fgrnIdWithMatrial: String) =
        repository.getOfflineGradeWithBags(fgrnIdWithMatrial)

    suspend fun getOfflineGradeWithBagsRmin(fgrnIdWithMatrial: String, batchNumber: String) =
        repository.getOfflineGradeWithBagsRmin(fgrnIdWithMatrial, batchNumber)

    suspend fun getOfflineRminLotsWithBags(fgrnIdWithMatrial: String, batchNumber: String) =
        repository.getOfflineRminLotsWithBags(fgrnIdWithMatrial, batchNumber)

    suspend fun saveBagDetails(
        material: VegaCoffeeFgrnGradesMatrialWeights,
        currentGrade: VegaCoffeeFgrnItemsGrades
    ) = repository.saveBagDetails(material, currentGrade)

    suspend fun deleteBagDetails(id: Int) = repository.deleteBagDetails(id)
    suspend fun updateWeighToProcess(weight: String, fgrnId: String, material: String, poNumber: String) =
        repository.updateWeightToProcessDetails(weight, fgrnId, material, poNumber)

    suspend fun saveLotInDispatch(
        list: MutableList<VegaCoffeeRminLots>,
        model: VegaCoffeeRminProcessing
    ) =
        repository.saveLots(list, model)

    suspend fun saveLot(
        list: VegaCoffeeRminLots
    ) =
        repository.saveLots(list)

    suspend fun saveRMINProcess(model: VegaCoffeeRminProcessing) =
        repository.saveRminProcess(model)

    suspend fun deleteLot(batchNo: String, cfgNo: String) = repository.deleteLot(batchNo, cfgNo)
    suspend fun deleteAllLot(batchNo: String, cfgNo: String, bomNo: String, baseMaterialCode: String) =
        repository.deleteAllLot(batchNo, cfgNo, bomNo, baseMaterialCode)

    suspend fun updateAllSyncStatus(model: VegaCoffeeRminProcessing, lot: ArrayList<VegaCoffeeRminLots>) =
        repository.updateAllSync(model, lot)

    suspend fun getLotList(poNo: String, material: String) =
        repository.getLotList(poNo, material)

    suspend fun getRMINProcess(cgfNo: String, poNo: String, bomNo: String, baseMaterialCode: String) =
        repository.getRMINProcess(cgfNo, poNo, bomNo, baseMaterialCode)

    suspend fun deleteAllRminItems() = repository.deleteAllRminItems()

    suspend fun postRminDetails(model: VegaNigeriaProcessingFgrnPost) = repository.postRminDetails(model)

    suspend fun getShiftRemarkItems(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        repository.getShiftRemarks(role)

    suspend fun deleteItemInAllTable(fgrnId: String) = repository.deleteItemInAllTable(fgrnId)
    suspend fun getFgrnItems(fgrnId: String) = repository.getFgrnItems(fgrnId)
    suspend fun getRminItems(rminId: String) = repository.getRminItems(rminId)
    suspend fun getRminItemLive(rminId: String) = repository.getRminItemsLive(rminId)
    suspend fun saveFgrnGrade(vegaCocoaFgrnItemsGrades: VegaCoffeeFgrnItemsGrades) =
        repository.saveFgrnGrade(vegaCocoaFgrnItemsGrades)

    suspend fun updateRminGrade(vegaCocoaFgrnItemsGrades: VegaCoffeeFgrnItemsGrades, batchNo: String) =
        repository.updateRminGrade(vegaCocoaFgrnItemsGrades, batchNo)

    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun postFgrnDetails(poReq: VegaNigeriaProcessingFgrnPost) = repository.postFgrnDetails(poReq)
    suspend fun updateFgrnStatus(message: String, status: Int, fgrnId: String) =
        repository.updateFgrnStatus(message, status, fgrnId)

    suspend fun updateFgrnShiftStatus(fgrnId: String, shiftSelection: String, operatorName: String) =
        repository.updateFgrnShiftStatus(fgrnId, shiftSelection, operatorName)

    suspend fun fetchFgrnItem(fgrnId: String): LiveData<VegaCoffeeFgrnItems> = repository.fetchFgrnItem(fgrnId)

    suspend fun postCreatePo(bomPostReq: VegaNigeriaProcessingCreatePoReq) = repository.postCreatePo(bomPostReq)
    suspend fun getRminWithLotList(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        baseMaterialCode: String
    ) = repository.getRminWithLotList(cgfNo, poNo, bomNo, baseMaterialCode)

    suspend fun getStockList(materialList: ArrayList<String>) = repository.getStockList(materialList)
    suspend fun updateBatchToBagDetails(
        bagMaterialCode: String,
        batchNo: String,
        bagId: String,
        isBagConsumed: Boolean
    ) =
        repository.updateBatchToBagDetails(bagMaterialCode, batchNo, bagId, isBagConsumed)

    suspend fun validateLot(batchNumber: String): VegaCoffeeRminLots = repository.validateLot(batchNumber)
    suspend fun getProcessingModel(batchNumber: String): VegaCoffeeRminProcessing =
        repository.getProcessingModel(batchNumber)

    suspend fun getLots(poNo: String, material: String): List<VegaCoffeeRminLots> = repository.getLots(poNo, material)
    suspend fun getAllLots(poNo: String, material: String): LiveData<List<VegaCoffeeRminLots>> =
        repository.getAllLots(poNo, material)

    suspend fun getQualityParams(charge: String, material: String, whId: String) =
        repository.getQualityParams(charge, material, whId)

    suspend fun getThirdPartyMaterials() = repository.getThirdPartyMaterials()
    suspend fun getCoffeeProducts(): LiveData<List<VegaMaterial>> = repository.getCoffeeProducts()
    suspend fun getFeatureMaster(module: String) = repository.getFeatureMaster(module)
}
