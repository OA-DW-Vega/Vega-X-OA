package com.olam.warehouse.vegax.processingecuador.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.data.domain.model.VegaProcessingRminPo
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.dao.VegaCocoaRMinDao
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.*
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaGrnDao
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminData
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminProcessLotDetails
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaOfflineRmin
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.processingecuador.data.api.VegaEcuadorProcessingApi
import com.olam.warehouse.vegax.processingecuador.data.domain.model.*
import com.olam.warehouse.vegax.processingecuador.utils.prepareData

interface VegaEcuadorProcessingRepository {
    suspend fun getGrades(): LiveData<List<VegaMaterial>>
    suspend fun getStages(): LiveData<List<VegaProcessingStage>>
    suspend fun getBom(bomPostReq: VegaEcuadorProcessingRminBomPost): LiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>>

    suspend fun getStocks(material: String): LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>>
    suspend fun postCreatePo(bomPostReq: VegaEcuadorProcessingCreatePoReq): LiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>>

    suspend fun getInventoryDetails(material: String): LiveData<Resource<GenericReqAndResp<VegaEcuadorProcessingInventoryAndSyncModel>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getQualityParams(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorProcessingInventoryStocks>>>>

    suspend fun getStoragetLocations(): LiveData<List<VegaStorageLocation>>

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
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaFgrnItems>>>>

    suspend fun getShiftRemarks(role: String): LiveData<List<VegaCocoaMiscellaneous>>
    suspend fun saveLots(
        dispatchLotsList: MutableList<VegaCocoaRminLots>,
        model: VegaCocoaRminProcessing
    )

    suspend fun saveRminProcess(model: VegaCocoaRminProcessing)
    suspend fun deleteLot(poNo: String, cgfNo: String)
    suspend fun deleteAllLot(
        batchNo: String, cgfNo: String, bomNo: String,
        baseMaterialCode: String
    )

    suspend fun getLotList(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        baseCode: String
    ): LiveData<List<VegaCocoaRminLots>>

    suspend fun fetchOfflineFgrnList(processOrderNo: String): LiveData<List<VegaCocoaFgrnItems>>
    suspend fun getPoGrades(
        poNo: String,
        rmin: Boolean
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaFgrnItemsGrades>>>>

    suspend fun saveFgrnItem(
        fgrnItem: VegaCocoaFgrnItems,
        selectedGrades: List<VegaCocoaFgrnItemsGrades>,
        removeItem: ArrayList<String>
    )

    suspend fun getOfflineGrades(poNo: String, fgrnId: String): LiveData<List<VegaCocoaFgrnItemsGrades>>
    suspend fun getOfflineGradeWithBags(fgrnIdWithMatrial: String): LiveData<VegaCocoaFgrnGradesWithBagItems>
    suspend fun saveBagDetails(
        material: VegaCocoaFgrnGradesMatrialWeights,
        currentGrade: VegaCocoaFgrnItemsGrades
    )

    suspend fun deleteBagDetails(id: Int)
    suspend fun getRMINProcess(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        baseCode: String
    ): LiveData<VegaCocoaRminProcessing>

    suspend fun postRminDetails(model: VegaEcuadorRminProcessingPost): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorProcessingRminResponse>>>>
    suspend fun deleteItemInAllTable(fgrnId: String)
    suspend fun getFgrnItems(fgrnId: String): LiveData<VegaCocoaFgrnItemWithGrades>
    suspend fun saveFgrnGrade(vegaCocoaFgrnItemsGrades: VegaCocoaFgrnItemsGrades)
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun postFgrnDetails(poReq: VegaCocoaProcessingFgrnPost): LiveData<Resource<GenericReqAndResp<List<VegaCocoaProcessingFgrnResponse>>>>
    suspend fun updateFgrnStatus(message: String, status: Int, fgrnId: String)
    suspend fun updateFgrnShiftStatus(fgrnId: String, shiftSelection: String, operatorName: String)
    suspend fun fetchFgrnItem(fgrnId: String): LiveData<VegaCocoaFgrnItems>
    suspend fun getRminWithLotList(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        baseMaterialCode: String
    ): LiveData<VegaCocoaRminItemWithLots>

    suspend fun getStockList(materialList: java.util.ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>>
    suspend fun updateBatchToBagDetails(bagMaterialCode: String, batchNo: String, bagId: String)

    suspend fun getOfflineRminItem(): LiveData<List<VegaNicaraguaOfflineRmin>>
    suspend fun saveOfflineRminDetails(rminData: VegaNicOfflineRminData)

    suspend fun saveOfflineRminLotDetails(rminData: VegaNicOfflineRminProcessLotDetails)
    suspend fun saveOfflineRminSelectedLots(rminData: VegaNicOfflineRminLots)
    suspend fun getOfflineRminLots(po: String): LiveData<List<VegaNicOfflineRminLots>>
    suspend fun deleteOfflineRminLot(batchNumber: String)
    suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>

    suspend fun getQualityParams(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun updateFgrnSequence(postData: EcuadorProcessingUpdateFgrnSequencePost): LiveData<Resource<GenericReqAndResp<EcuadorProcessingUpdateFgrnSequencePost>>>

}

class VegaIndiaCoffeeProcessingRepositoryImpl(
    private val api: VegaEcuadorProcessingApi,
    private val dao: VegaCocoaRMinDao,
    private val repo: VegaNicaraguaGrnDao,
    private val daoQ: VegaQualityDao,
    private val masterDao: MasterDao
) : VegaEcuadorProcessingRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getGrades() = dao.getProducts()
    override suspend fun getStages() = dao.getProcessingStage()
    override suspend fun getBom(bomPostReq: VegaEcuadorProcessingRminBomPost): LiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaProcessingRminBom>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaProcessingRminBom> =
                api.fetchBomList(bomPostReq)
        }.build().asLiveData()
    }

    override suspend fun getInventoryDetails(material: String): LiveData<Resource<GenericReqAndResp<VegaEcuadorProcessingInventoryAndSyncModel>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaEcuadorProcessingInventoryAndSyncModel>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaEcuadorProcessingInventoryAndSyncModel> =
                api.getEcuadorProcessingInventoryList(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getStoragetLocations() =
        repo.getStorageLocations(getPlantDetails().plantId)


    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getQualityParams(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorProcessingInventoryStocks>>>> {

        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorProcessingInventoryStocks>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorProcessingInventoryStocks>> =
                api.getLotInfo(currentKey, charge, material, whId)

        }.build().asLiveData()
    }

    override suspend fun getStocks(material: String): LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaRminLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaRminLots>> =
                api.getStocks(currentKey, material)
        }.build().asLiveData()
    }


    override suspend fun postCreatePo(bomPostReq: VegaEcuadorProcessingCreatePoReq): LiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>> {
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
                    VegaEcuadorProcessingOrderReq(
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
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaFgrnItems>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaFgrnItems>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaFgrnItems>> =
                api.fetchFgrnPoDetailsList(
                    VegaEcuadorProcessingOrderReq(
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
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaFgrnItemsGrades>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaFgrnItemsGrades>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaFgrnItemsGrades>> =
                api.getFgrnGrades(
                    VegaEcuadorProcessingOrderDetailsPostReq(
                        getCurrentKey(),
                        getPlantDetails(),
                        poNo,
                        rmin
                    )
                )
        }.build().asLiveData()
    }

    override suspend fun saveFgrnItem(
        fgrnItem: VegaCocoaFgrnItems,
        selectedGrades: List<VegaCocoaFgrnItemsGrades>,
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

    override suspend fun getOfflineGrades(poNo: String, fgrnId: String) = dao.getOfflineGrades(poNo, fgrnId)
    override suspend fun getOfflineGradeWithBags(fgrnIdWithMatrial: String) =
        dao.getOfflineGradeWithBags(fgrnIdWithMatrial)

    override suspend fun saveBagDetails(
        material: VegaCocoaFgrnGradesMatrialWeights,
        currentGrade: VegaCocoaFgrnItemsGrades
    ) {
        if (currentGrade.startTime?.isNotEmpty()!!)
            dao.updateFgrnGradeStartTime(
                currentGrade.startTime.toString(),
                currentGrade.fgrnIdMaterialCode,
                currentGrade.isRoundOff
            )
        if (currentGrade.endTime?.isNotEmpty()!!)
            dao.updateFgrnGradeEndTime(
                currentGrade.endTime.toString(),
                currentGrade.fgrnIdMaterialCode,
                currentGrade.isRoundOff
            )
        dao.saveBagDetails(material)
    }

    override suspend fun deleteBagDetails(id: Int) = dao.deleteBagDetails(id)
    override suspend fun deleteItemInAllTable(fgrnId: String) {
        dao.deleteAllBagDetails(fgrnId)
        dao.deleteAllGrades(fgrnId)
        dao.deleteFgrnItems(fgrnId)
    }

    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)

    override suspend fun getShiftRemarks(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        dao.getShiftRemarkItems(role)

    override suspend fun saveLots(
        dispatchLotsList: MutableList<VegaCocoaRminLots>,
        model: VegaCocoaRminProcessing
    ) {
        dao.insertRminModel(model)
        dao.saveLots(dispatchLotsList)
    }

    override suspend fun saveRminProcess(model: VegaCocoaRminProcessing) = dao.insertRminModel(model)

    override suspend fun deleteLot(batchNo: String, cgfNo: String) {
        dao.deleteRminLotsByStage(batchNo, cgfNo)
    }

    override suspend fun deleteAllLot(
        batchNo: String, cgfNo: String, bomNo: String,
        baseMaterialCode: String
    ) {
        if (batchNo.isNotEmpty()) {
            dao.deleteRminAllLotsWithPo(batchNo, cgfNo, baseMaterialCode)
            dao.deleteRminProcessingIndiaCoffeeWithPo(batchNo, cgfNo, baseMaterialCode)
        } else {
            dao.deleteRminAllLotsWithBom(cgfNo, bomNo, baseMaterialCode)
            dao.deleteRminProcessingIndiaCoffeeWithBom(cgfNo, bomNo, baseMaterialCode)
        }
    }

    override suspend fun getLotList(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        baseMaterialCode: String
    ): LiveData<List<VegaCocoaRminLots>> =
        dao.getLotList(cgfNo, poNo, bomNo, baseMaterialCode)

    override suspend fun getRMINProcess(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        baseMaterialCode: String
    ): LiveData<VegaCocoaRminProcessing> =
        dao.getRMINProcessing(cgfNo, poNo, bomNo, baseMaterialCode)

    override suspend fun postRminDetails(model: VegaEcuadorRminProcessingPost): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorProcessingRminResponse>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorProcessingRminResponse>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorProcessingRminResponse>> =
                api.postRminDetails(model)
        }.build().asLiveData()
    }

    override suspend fun getFgrnItems(fgrnId: String) = dao.getFgrnItems(fgrnId)
    override suspend fun saveFgrnGrade(vegaCocoaFgrnItemsGrades: VegaCocoaFgrnItemsGrades) =
        dao.saveFgrnGrade(vegaCocoaFgrnItemsGrades)

    override suspend fun getCustomLocations() = dao.getCustomLocations()


    override suspend fun postFgrnDetails(poReq: VegaCocoaProcessingFgrnPost): LiveData<Resource<GenericReqAndResp<List<VegaCocoaProcessingFgrnResponse>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaProcessingFgrnResponse>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaProcessingFgrnResponse>> =
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

    override suspend fun fetchFgrnItem(fgrnId: String): LiveData<VegaCocoaFgrnItems> = dao.fetchFgrnItem(fgrnId)
    override suspend fun getRminWithLotList(
        cgfNo: String,
        poNo: String,
        bomNo: String,
        baseMaterialCode: String
    ) = dao.getRminWithLotList(cgfNo, poNo, bomNo, baseMaterialCode)

    override suspend fun getStockList(materialList: java.util.ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaRminLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaRminLots>> =
                api.getStockList(getCurrentKey(), materialList)
        }.build().asLiveData()
    }

    override suspend fun updateBatchToBagDetails(
        bagMaterialCode: String,
        batchNo: String,
        bagId: String
    ) = dao.updateBatchToBagDetails(bagMaterialCode, batchNo, bagId)

    override suspend fun getOfflineRminItem(): LiveData<List<VegaNicaraguaOfflineRmin>> =
        repo.getofflineRminItem()

    override suspend fun saveOfflineRminDetails(rminData: VegaNicOfflineRminData) {
        repo.saveOfflineRminDetails(rminData)
    }

    override suspend fun saveOfflineRminLotDetails(rminLotData: VegaNicOfflineRminProcessLotDetails) {
        repo.saveOfflineRminLotDetails(rminLotData)
    }

    override suspend fun saveOfflineRminSelectedLots(rminLotData: VegaNicOfflineRminLots) {
        repo.saveOfflineRminSelectedLots(rminLotData)
    }

    override suspend fun getOfflineRminLots(po: String): LiveData<List<VegaNicOfflineRminLots>> =
        repo.getOfflineRminLots(po)

    override suspend fun deleteOfflineRminLot(batchNumber: String) {
        repo.deleteOfflineRminLot(batchNumber)
    }

    override suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityPreParameter>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityPreParameter>> =
                api.fetchPreQualityDetails(currentKey, batchNo, materialId)
        }.build().asLiveData()
    }

    override suspend fun updateFgrnSequence(postData: EcuadorProcessingUpdateFgrnSequencePost): LiveData<Resource<GenericReqAndResp<EcuadorProcessingUpdateFgrnSequencePost>>> {
            return object :
                NetworkOnlyBoundResource<GenericReqAndResp<EcuadorProcessingUpdateFgrnSequencePost>>() {

                override suspend fun createCall(): GenericReqAndResp<EcuadorProcessingUpdateFgrnSequencePost> =
                    api.updateLotSequence(postData)
            }.build().asLiveData()

    }

    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return if (valueExist!!) prepareData(
            daoQ.getQualityParameterWithData(
                materialId,
                wbId
            )
        ) else
            daoQ.getQualityParameter(materialId)
    }
}
