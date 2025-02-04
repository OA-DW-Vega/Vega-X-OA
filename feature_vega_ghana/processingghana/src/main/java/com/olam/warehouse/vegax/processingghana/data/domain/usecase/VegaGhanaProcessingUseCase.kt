package com.olam.warehouse.vegax.processingghana.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaghana.entity.*
import com.olam.warehouse.master.vegaghana.model.VegaGhanaOfflineFgrn
import com.olam.warehouse.master.vegaghana.model.VegaGhanaOfflineRmin
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessingCreatePoReq
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessingFgrnPost
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessingRminBomPost
import com.olam.warehouse.vegax.processingghana.data.repo.VegaGhanaProcessingRepository

class VegaGhanaProcessingUseCase(val repository: VegaGhanaProcessingRepository) {
    suspend fun getGrades() = repository.getGrades()
    suspend fun getStages() = repository.getStages()
    suspend fun getBom(bomPostReq: VegaGhanaProcessingRminBomPost) =
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
    suspend fun getMaterials() = repository.getMaterials()

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
    suspend fun deleteAllRminItems() = repository.deleteAllRminItems()
    suspend fun deleteAllRminLots() = repository.deleteAllRminLots()
    suspend fun deleteAllFgrnItems() = repository.deleteAllFgrnItems()
    suspend fun updateFgrnStatus(status: Boolean, rminId: String) = repository.updateFgrnStatus(status, rminId)
    suspend fun getAllFGRNItems() = repository.getAllFGRNItems()
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

    suspend fun saveOfflineRminDetails(rminData: VegaGhanaOfflineRminData) =
        repository.saveOfflineRminDetails(rminData)

    suspend fun saveOfflineRminLotDetails(rminLotData: VegaGhanaOfflineRminProcessLotDetails) =
        repository.saveOfflineRminLotDetails(rminLotData)

    suspend fun saveOfflineFgrnDetails(rminData: VegaGhanaOfflineFgrnData) =
        repository.saveOfflineFgrnDetails(rminData)

    suspend fun saveOfflineFgrnLotDetails(rminLotData: VegaGhanaOfflineFgrnProcessLotDetails) =
        repository.saveOfflineFgrnLotDetails(rminLotData)

    suspend fun saveOfflineRminSelectedLots(rminLotData: VegaGhanaOfflineRminLots) =
        repository.saveOfflineRminSelectedLots(rminLotData)

    suspend fun saveOfflineRminItem(rminLotData: VegaGhanaOfflineRminItems) =
        repository.saveOfflineRminItem(rminLotData)


    suspend fun saveRMINProcess(model: VegaCoffeeRminProcessing) =
        repository.saveRminProcess(model)

    suspend fun deleteLot(batchNo: String, cfgNo: String) = repository.deleteLot(batchNo, cfgNo)
    suspend fun deleteAllLot(
        batchNo: String,
        cfgNo: String,
        bomNo: String,
        baseMaterialCode: String
    ) =
        repository.deleteAllLot(batchNo, cfgNo, bomNo, baseMaterialCode)

    suspend fun updateAllSyncStatus(
        model: VegaCoffeeRminProcessing,
        lot: ArrayList<VegaCoffeeRminLots>
    ) =
        repository.updateAllSync(model, lot)

    suspend fun getLotList(poNo: String, material: String) =
        repository.getLotList(poNo, material)

    suspend fun getofflineRmindata(RminTempId: String) =
        repository.getofflineRmindata(RminTempId)

    suspend fun getOfflineRminItem(): LiveData<List<VegaGhanaOfflineRmin>> =
        repository.getOfflineRminItem()

    suspend fun getofflineFgrnItem(): LiveData<List<VegaGhanaOfflineFgrn>> =
        repository.getofflineFgrnItem()

    suspend fun getofflineFgrnPostItem(): LiveData<List<VegaGhanaOfflineFgrnData>> =
        repository.getofflineFgrnPostItem()

    suspend fun getofflineRminPostItem(rmin: String): LiveData<List<VegaGhanaOfflineRminProcessLotDetails>> =
        repository.getofflineRminPostItem(rmin)

    suspend fun getProcessOrders(): LiveData<List<VegaGhanaProcessingOrder>> =
        repository.getProcessOrders()

    suspend fun getProcessOrderDetails(poNumber: String): LiveData<List<VegaGhanaProcessingOrderDetails>> =
        repository.getProcessOrderDetails(poNumber)

    suspend fun getStockDetails(material: String): LiveData<List<VegaEcuadorDispatchStocks>> =
        repository.getStockDetails(material)

    suspend fun getOfflineRminLots(): LiveData<List<VegaGhanaOfflineRminLots>> =
        repository.getOfflineRminLots()

    suspend fun getOfflineRminItems(): LiveData<List<VegaGhanaOfflineRminItems>> =
        repository.getOfflineRminItems()

    suspend fun getRMINProcess(cgfNo: String, poNo: String, bomNo: String, baseMaterialCode: String) =
        repository.getRMINProcess(cgfNo, poNo, bomNo, baseMaterialCode)

    suspend fun postRminDetails(model: VegaGhanaProcessingFgrnPost) = repository.postRminDetails(model)

    suspend fun getShiftRemarkItems(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        repository.getShiftRemarks(role)

    suspend fun deleteItemInAllTable(fgrnId: String) = repository.deleteItemInAllTable(fgrnId)
    suspend fun getFgrnItems(fgrnId: String) = repository.getFgrnItems(fgrnId)
    suspend fun getRminItems(rminId: String) = repository.getRminItems(rminId)
    suspend fun getRminItemLive(rminId: String) = repository.getRminItemsLive(rminId)
    suspend fun saveFgrnGrade(vegaCocoaFgrnItemsGrades: VegaCoffeeFgrnItemsGrades) =
        repository.saveFgrnGrade(vegaCocoaFgrnItemsGrades)

    suspend fun updateDeletedItem(tmpWbId: String) = repository.updateDeletedItem(tmpWbId)

    suspend fun updateFgrnDeletedItem(tmpWbId: String) = repository.updateFgrnDeletedItem(tmpWbId)

    suspend fun updateSyncedFgrnDeletedItem(status: Int) = repository.updateSyncedFgrnDeletedItem(status)

    suspend fun updateRminGrade(vegaCocoaFgrnItemsGrades: VegaCoffeeFgrnItemsGrades, batchNo: String) =
        repository.updateRminGrade(vegaCocoaFgrnItemsGrades, batchNo)

    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun postFgrnDetails(poReq: VegaGhanaProcessingFgrnPost) = repository.postFgrnDetails(poReq)
    suspend fun getStockDetailsByBatchNo(batchNo: String) = repository.getStockDetailsByBatchNo(batchNo)
    suspend fun updateFgrnStatus(message: String, status: Int, fgrnId: String) =
        repository.updateFgrnStatus(message, status, fgrnId)

    suspend fun updateStockDetails(weight: String, batchNo: String) =
        repository.updateStockDetails(weight, batchNo)

    suspend fun updateFgrnTotal(total: String, poNo: String) =
        repository.updateFgrnTotal(total, poNo)

    suspend fun updateFgrnShiftStatus(fgrnId: String, shiftSelection: String, operatorName: String) =
        repository.updateFgrnShiftStatus(fgrnId, shiftSelection, operatorName)

    suspend fun fetchFgrnItem(fgrnId: String): LiveData<VegaCoffeeFgrnItems> = repository.fetchFgrnItem(fgrnId)

    suspend fun postCreatePo(bomPostReq: VegaGhanaProcessingCreatePoReq) = repository.postCreatePo(bomPostReq)
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

    suspend fun getLots(poNo: String, material: String): List<VegaCoffeeRminLots> =
        repository.getLots(poNo, material)

    suspend fun getAllLots(poNo: String, material: String): LiveData<List<VegaCoffeeRminLots>> =
        repository.getAllLots(poNo, material)

    suspend fun getQualityParams(charge: String, material: String, whId: String) =
        repository.getQualityParams(charge, material, whId)

    suspend fun getThirdPartyMaterials() = repository.getThirdPartyMaterials()
    suspend fun getCoffeeProducts(): LiveData<List<VegaMaterial>> = repository.getCoffeeProducts()

    suspend fun getBomoffline(materialcode: String): LiveData<List<VegaProcessingRminBoms>> =
        repository.getBomoffline(materialcode)
}
