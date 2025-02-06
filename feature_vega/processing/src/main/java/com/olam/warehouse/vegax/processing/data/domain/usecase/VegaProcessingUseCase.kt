package com.olam.warehouse.vegax.processing.data.domain.usecase

import com.olam.warehouse.master.vega.entity.ProcessingLotDetails
import com.olam.warehouse.master.vega.entity.VegaProcessingCreatePoReq
import com.olam.warehouse.master.vega.entity.VegaProcessingRminBoms
import com.olam.warehouse.vegax.processing.data.domain.model.VegaProcessingFgrnPost
import com.olam.warehouse.vegax.processing.data.domain.model.VegaProcessingRminBomPost
import com.olam.warehouse.vegax.processing.data.repo.VegaProcessingRepository

class VegaProcessingUseCase(private val repository: VegaProcessingRepository) {
    suspend fun getGrades() = repository.getGrades()
    suspend fun getStages() = repository.getStages()
    suspend fun getBom(bomPostReq: VegaProcessingRminBomPost) =
        repository.getBom(bomPostReq)
    suspend fun getStorageLocation() = repository.getStroageLocation()
    suspend fun getStocks(material: String) = repository.getStocks(material)
    suspend fun getQualityParams(charge: String, material: String) = repository.getQualityParams(charge, material)
    suspend fun postCreatePo(bomPostReq: VegaProcessingCreatePoReq) = repository.postCreatePo(bomPostReq)


    suspend fun getFgrnPoDetailsList(stageFevor: String?, cfgNo: String?) = repository.getFgrnPoDetailsList(stageFevor, cfgNo)
    suspend fun getFgrnGrades(poNumber: String) = repository.getFgrnGrades(poNumber)
    suspend fun postFgrnDetails(fgrnReq: VegaProcessingFgrnPost) = repository.postFgrnDetails(fgrnReq)
    suspend fun saveRmin(
        bom: VegaProcessingRminBoms,
        processLots: ProcessingLotDetails,
        stageFevor: String?,
        materialName: String,
        materialNo: String
    ) =
        repository.saveRmin(bom, processLots, stageFevor, materialName, materialNo)

    suspend fun updateRminData(batchNo: String?, msg: String, syncStatus: Boolean) =
        repository.updateRminData(batchNo, msg, syncStatus)
}

