package com.olam.warehouse.vegax.processingcoffee.data.repo

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.data.domain.model.VegaProcessingRminPo
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeRminDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.master.vegacoffee.model.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.processingcoffee.data.api.VegaCoffeeProcessingApi
import com.olam.warehouse.vegax.processingcoffee.data.domain.model.*

interface VegaCoffeeProcessingRepository {
    suspend fun getGrades(): LiveData<List<VegaMaterial>>
    suspend fun getStages(): LiveData<List<VegaProcessingStage>>
    suspend fun getBom(bomPostReq: VegaCoffeeProcessingRminBomPost): LiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>>

    suspend fun getStocks(material: String): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>>
    suspend fun postCreatePo(bomPostReq: VegaCoffeeProcessingCreatePoReq): LiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>>

    suspend fun getFgrnPoDetailsList(
        auart: String,
        cfgNo: String,
        fevor: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>>

    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun fetchFgrnPoDetailsList(
        stageFevor: String,
        cfgNo: String,
        auart: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeFgrnItems>>>>

    suspend fun getShiftRemarks(role: String): LiveData<List<VegaCocoaMiscellaneous>>
    suspend fun saveLots(
        dispatchLotsList: MutableList<VegaCoffeeRminLots>,
        model: VegaCoffeeRminProcessing
    )

    suspend fun saveLots(
        dispatchLotsList: VegaCoffeeRminLots
    )

    suspend fun saveRminProcess(model: VegaCoffeeRminProcessing)
    suspend fun deleteLot(poNo: String, cgfNo: String)
    suspend fun deleteAllLot(
        batchNo: String, cgfNo: String, bomNo: String,
        baseMaterialCode: String
    )

    suspend fun updateAllSync(model: VegaCoffeeRminProcessing, lots: ArrayList<VegaCoffeeRminLots>)

    suspend fun getLotList(
        poNo: String, material: String
    ): LiveData<List<VegaCoffeeRminLots>>

    suspend fun fetchOfflineFgrnList(processOrderNo: String): LiveData<List<VegaCoffeeFgrnItems>>
    suspend fun getPoGrades(
        poNo: String,
        rmin: Boolean
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeFgrnItemsGrades>>>>

    suspend fun saveFgrnItem(
        fgrnItem: VegaCoffeeFgrnItems,
        selectedGrades: List<VegaCoffeeFgrnItemsGrades>,
        removeItem: ArrayList<String>
    )

    suspend fun saveRminItem(
        fgrnItem: VegaCoffeeRminProcessing,
        selectedGrades: List<VegaCoffeeFgrnItemsGrades>,
        removeItem: ArrayList<String>
    )

    suspend fun getOfflineGrades(poNo: String, fgrnId: String): LiveData<List<VegaCoffeeFgrnItemsGrades>>
    suspend fun getOfflineGrades(poNo: String): LiveData<List<VegaCoffeeFgrnItemsGrades>>
    suspend fun getOfflineGradeWithBags(fgrnIdWithMatrial: String): LiveData<VegaCoffeeFgrnGradesWithBagItems>
    suspend fun getOfflineGradeWithBagsRmin(
        fgrnIdWithMatrial: String,
        batchNumber: String
    ): List<VegaCoffeeFgrnGradesMatrialWeights>

    suspend fun getOfflineRminLotsWithBags(
        fgrnIdWithMatrial: String,
        batchNumber: String
    ): LiveData<VegaCoffeeRMINGradesWithBagItems>

    suspend fun saveBagDetails(
        material: VegaCoffeeFgrnGradesMatrialWeights,
        currentGrade: VegaCoffeeFgrnItemsGrades
    )

    suspend fun deleteBagDetails(id: Int)
    suspend fun updateWeightToProcessDetails(weight: String, fgrnId: String, material: String, poNumber: String)
    suspend fun getRMINProcess(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        baseCode: String
    ): LiveData<VegaCoffeeRminProcessing>

    suspend fun postRminDetails(model: VegaCoffeeProcessingFgrnPost): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeProcessingRminResponse>>>>
    suspend fun deleteItemInAllTable(fgrnId: String)
    suspend fun getFgrnItems(fgrnId: String): LiveData<VegaCoffeeFgrnItemWithGrades>
    suspend fun getRminItems(fgrnId: String): VegaCoffeeRminItemWithGrades
    suspend fun getRminItemsLive(fgrnId: String): LiveData<VegaCoffeeRminItemWithGrades>
    suspend fun saveFgrnGrade(vegaCocoaFgrnItemsGrades: VegaCoffeeFgrnItemsGrades)
    suspend fun updateRminGrade(vegaCocoaFgrnItemsGrades: VegaCoffeeFgrnItemsGrades, batchNo: String)
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun postFgrnDetails(poReq: VegaCoffeeProcessingFgrnPost): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeProcessingFgrnResponse>>>>
    suspend fun updateFgrnStatus(message: String, status: Int, fgrnId: String)
    suspend fun updateFgrnShiftStatus(fgrnId: String, shiftSelection: String, operatorName: String)
    suspend fun fetchFgrnItem(fgrnId: String): LiveData<VegaCoffeeFgrnItems>
    suspend fun getRminWithLotList(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        baseMaterialCode: String
    ): LiveData<VegaCoffeeRminItemWithLots>

    suspend fun getStockList(materialList: java.util.ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>>
    suspend fun updateBatchToBagDetails(bagMaterialCode: String, batchNo: String, bagId: String, isBagConsumed: Boolean)
    suspend fun validateLot(batchNumber: String): VegaCoffeeRminLots
    suspend fun getProcessingModel(batchNumber: String): VegaCoffeeRminProcessing
    suspend fun getLots(poNo: String, material: String): List<VegaCoffeeRminLots>
    suspend fun getAllLots(poNo: String, material: String): LiveData<List<VegaCoffeeRminLots>>

    suspend fun getQualityParams(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>>

    suspend fun getThirdPartyMaterials(): List<VegaCoffeeThirdPartyMaterialDetail>
    suspend fun getCoffeeProducts(): LiveData<List<VegaMaterial>>
}

class VegaCoffeeProcessingRepoImpl(
    private val api: VegaCoffeeProcessingApi,
    private val dao: VegaCoffeeRminDao,
    private val masterDao: MasterDao
) : VegaCoffeeProcessingRepository {

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getGrades() = dao.getProducts()
    override suspend fun getStages() = dao.getProcessingStage()
    override suspend fun getBom(bomPostReq: VegaCoffeeProcessingRminBomPost): LiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaProcessingRminBom>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaProcessingRminBom> =
                api.fetchBomList(bomPostReq)
        }.build().asLiveData()
    }

    override suspend fun getQualityParams(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeRminLots>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeRminLots>> =
                api.getLotInfo(currentKey, charge, material, whId)

        }.build().asLiveData()
    }

    override suspend fun getThirdPartyMaterials(): List<VegaCoffeeThirdPartyMaterialDetail> =
        dao.getThirdPartyMaterials()

    override suspend fun getCoffeeProducts(): LiveData<List<VegaMaterial>> = dao.getProducts()


    override suspend fun getStocks(material: String): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeRminLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeRminLots>> =
                api.getStocks(currentKey, material)
        }.build().asLiveData()
    }


    override suspend fun postCreatePo(bomPostReq: VegaCoffeeProcessingCreatePoReq): LiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaProcessingRminPo>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaProcessingRminPo> =
                api.postCreatePo(bomPostReq)
        }.build().asLiveData()
    }

    override suspend fun getFgrnPoDetailsList(
        auart: String,
        cfgNo: String,
        fevor: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaFgrnProcessingOrder>> =
                api.getFgrnPoDetailsList(
                    VegaCoffeeProcessingOrderReq(
                        getCurrentKey(),
                        auart,
                        cfgNo,
                        fevor,
                        getPlantDetails()
                    )
                )
        }.build().asLiveData()
    }

    override suspend fun fetchFgrnPoDetailsList(
        stageFevor: String,
        cfgNo: String,
        auart: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeFgrnItems>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeFgrnItems>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeFgrnItems>> =
                api.fetchFgrnPoDetailsList(
                    VegaCoffeeProcessingOrderReq(
                        getCurrentKey(),
                        auart,
                        cfgNo,
                        stageFevor,
                        getPlantDetails()
                    )
                )
        }.build().asLiveData()
    }

    override suspend fun fetchOfflineFgrnList(processOrderNo: String) = dao.fetchOfflineFgrnList(processOrderNo)

    override suspend fun getPoGrades(
        poNo: String,
        rmin: Boolean
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeFgrnItemsGrades>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeFgrnItemsGrades>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeFgrnItemsGrades>> =
                api.getFgrnGrades(VegaCoffeeProcessOrderDetailsPostReq(getCurrentKey(), getPlantDetails(), poNo, rmin))
        }.build().asLiveData()
    }

    override suspend fun saveFgrnItem(
        fgrnItem: VegaCoffeeFgrnItems,
        selectedGrades: List<VegaCoffeeFgrnItemsGrades>,
        removeItem: ArrayList<String>
    ) {
        removeItem.forEach {
            dao.deleteGrade(it.trim())
            dao.deleteBagDetails(it.trim())
        }
        dao.saveFgrnItem(fgrnItem)
        selectedGrades.forEach {
            if (dao.getGradeCount(it.fgrnIdMaterialCode) == 0) dao.saveFgrnGrade(it)
        }
    }


    override suspend fun saveRminItem(
        fgrnItem: VegaCoffeeRminProcessing,
        selectedGrades: List<VegaCoffeeFgrnItemsGrades>,
        removeItem: ArrayList<String>
    ) {
        removeItem.forEach {
            dao.deleteGrade(it.trim())
            dao.deleteBagDetails(it.trim())
        }
        dao.saveRminItem(fgrnItem)
        selectedGrades.forEach {
            if (dao.getGradeCount(it.fgrnIdMaterialCode) == 0) dao.saveFgrnGrade(it)
        }
    }

    override suspend fun getOfflineGrades(poNo: String, fgrnId: String) = dao.getOfflineGrades(poNo, fgrnId)
    override suspend fun getOfflineGrades(poNo: String) = dao.getOfflineGrades(poNo)
    override suspend fun getOfflineGradeWithBags(fgrnIdWithMatrial: String) =
        dao.getOfflineGradeWithBags(fgrnIdWithMatrial)

    override suspend fun getOfflineGradeWithBagsRmin(fgrnIdWithMatrial: String, batchNo: String) =
        dao.getOfflineGradeWithBagsRmin(fgrnIdWithMatrial, batchNo)

    override suspend fun getOfflineRminLotsWithBags(
        fgrnIdWithMatrial: String,
        batchNumber: String
    ): LiveData<VegaCoffeeRMINGradesWithBagItems> = dao.getRminOfflineGradeWithLotsAndBags(fgrnIdWithMatrial)

    override suspend fun saveBagDetails(
        material: VegaCoffeeFgrnGradesMatrialWeights,
        currentGrade: VegaCoffeeFgrnItemsGrades
    ) {
        if (currentGrade.startTime?.isNotEmpty()!!)
            dao.updateFgrnGradeStartTime(currentGrade.startTime.toString(), currentGrade.fgrnIdMaterialCode)
        if (currentGrade.endTime?.isNotEmpty()!!)
            dao.updateFgrnGradeEndTime(currentGrade.endTime.toString(), currentGrade.fgrnIdMaterialCode)
        dao.saveBagDetails(material)
    }

    override suspend fun deleteBagDetails(id: Int) = dao.deleteBagDetails(id)
    override suspend fun updateWeightToProcessDetails(
        weight: String,
        fgrnId: String,
        material: String,
        poNumber: String
    ) = dao.updateWeighToProcess(weight, fgrnId, material, poNumber)

    override suspend fun deleteItemInAllTable(fgrnId: String) {
        dao.deleteAllBagDetails(fgrnId)
        dao.deleteAllGrades(fgrnId)
        dao.deleteFgrnItems(fgrnId)
    }

    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)

    override suspend fun getShiftRemarks(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        dao.getShiftRemarkItems(role)

    override suspend fun saveLots(
        dispatchLotsList: MutableList<VegaCoffeeRminLots>,
        model: VegaCoffeeRminProcessing
    ) {
        dao.insertRminModel(model)
        dao.saveLots(dispatchLotsList)
    }

    override suspend fun saveLots(
        dispatchLotsList: VegaCoffeeRminLots
    ) {
        val list = ArrayList<VegaCoffeeRminLots>()
        list.add(dispatchLotsList)
        dao.saveLots(list)
    }

    override suspend fun saveRminProcess(model: VegaCoffeeRminProcessing) = dao.saveRminItem(model)

    override suspend fun deleteLot(batchNo: String, cgfNo: String) {
        dao.deleteRminLotsByStage(batchNo, cgfNo)
    }

    override suspend fun deleteAllLot(
        batchNo: String, cgfNo: String, bomNo: String,
        baseMaterialCode: String
    ) {
        if (batchNo.isNotEmpty()) {
            dao.deleteRminAllLotsWithPo(batchNo, cgfNo, baseMaterialCode)
            dao.deleteRminProcessingWithPo(batchNo, cgfNo, baseMaterialCode)
        } else {
            dao.deleteRminAllLotsWithBom(cgfNo, bomNo, baseMaterialCode)
            dao.deleteRminProcessingWithBom(cgfNo, bomNo, baseMaterialCode)
        }
    }

    override suspend fun updateAllSync(model: VegaCoffeeRminProcessing, lots: ArrayList<VegaCoffeeRminLots>) {
        val gradeList = Gson().fromJson<List<VegaCoffeeFgrnItemsGrades>>(model.gradeListDetails ?: "")
        dao.updateSyncStatusProcess(model.poNumber, 1, 4)
        gradeList.forEach { dao.updateSyncStatusGrade(it.processOrderNo, it.fgrnId, it.materialCode, 1, 4) }
        lots.forEach { dao.updateSyncStatusLot(it.batchNumber, 1, 4) }

    }

    override suspend fun getLotList(
        poNo: String, material: String
    ): LiveData<List<VegaCoffeeRminLots>> =
        dao.getLotList(poNo)

    override suspend fun getRMINProcess(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        baseMaterialCode: String
    ): LiveData<VegaCoffeeRminProcessing> =
        dao.getRMINProcessing(cgfNo, poNo, bomNo, baseMaterialCode)

    override suspend fun postRminDetails(model: VegaCoffeeProcessingFgrnPost): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeProcessingRminResponse>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeProcessingRminResponse>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeProcessingRminResponse>> =
                api.postRminDetails(model)
        }.build().asLiveData()
    }

    override suspend fun getFgrnItems(fgrnId: String) = dao.getFgrnItems(fgrnId)
    override suspend fun getRminItems(fgrnId: String) = dao.getRminItems(fgrnId)

    override suspend fun getRminItemsLive(fgrnId: String) = dao.getRminItemsLive(fgrnId)
    override suspend fun saveFgrnGrade(vegaCoffeeFgrnItemsGrades: VegaCoffeeFgrnItemsGrades) =
        dao.saveFgrnGrade(vegaCoffeeFgrnItemsGrades)

    override suspend fun updateRminGrade(vegaCoffeeFgrnItemsGrades: VegaCoffeeFgrnItemsGrades, batch: String) =
        dao.updateRminGradeBatch(
            vegaCoffeeFgrnItemsGrades.fgrnIdMaterialCode,
            vegaCoffeeFgrnItemsGrades.fgrnIdMaterialCode.plus(batch)
        )

    override suspend fun getCustomLocations() = dao.getCustomLocations()

    override suspend fun postFgrnDetails(poReq: VegaCoffeeProcessingFgrnPost): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeProcessingFgrnResponse>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeProcessingFgrnResponse>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeProcessingFgrnResponse>> =
                api.postFgrnDetails(poReq)
        }.build().asLiveData()
    }

    override suspend fun updateFgrnStatus(message: String, status: Int, fgrnId: String) {
        if (status == 4) dao.updateFgrnStatus(message, status, fgrnId, 1)
        else dao.updateFgrnStatus(
            message,
            status,
            fgrnId,
            0
        )
    }

    override suspend fun updateFgrnShiftStatus(
        fgrnId: String,
        shiftSelection: String,
        operatorName: String
    ) =
        dao.updateFgrnShiftStatus(fgrnId, shiftSelection, operatorName)

    override suspend fun fetchFgrnItem(fgrnId: String): LiveData<VegaCoffeeFgrnItems> = dao.fetchFgrnItem(fgrnId)
    override suspend fun getRminWithLotList(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        baseMaterialCode: String
    ) = dao.getRminWithLotList(cgfNo, poNo, bomNo, baseMaterialCode)

    override suspend fun getStockList(materialList: java.util.ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeRminLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeRminLots>> =
                api.getStockList(getCurrentKey(), materialList)
        }.build().asLiveData()
    }

    override suspend fun updateBatchToBagDetails(
        bagMaterialCode: String,
        batchNo: String,
        bagId: String, isBagConsumed: Boolean
    ) = dao.updateBatchToBagDetails(bagMaterialCode, batchNo, bagId, isBagConsumed)

    override suspend fun validateLot(whId: String) = dao.validateLotAlreadyAdded(whId)
    override suspend fun getProcessingModel(poNumber: String) = dao.getProcessingModel(poNumber)

    override suspend fun getLots(poNo: String, material: String) = dao.getLots(poNo, material)
    override suspend fun getAllLots(poNo: String, material: String) = dao.getAllLots(poNo, material)
}
