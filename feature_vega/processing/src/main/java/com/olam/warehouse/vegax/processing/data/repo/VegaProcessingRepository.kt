package com.olam.warehouse.vegax.processing.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.data.domain.model.VegaProcessingRminPo
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.dao.VegaProcessingDao
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.processing.data.api.VegaProcessingApi
import com.olam.warehouse.vegax.processing.data.domain.model.*
import com.olam.warehouse.vegax.processing.utils.prepareRminCreatePo

interface VegaProcessingRepository {
    suspend fun getGrades(): LiveData<List<VegaMaterial>>
    suspend fun getStages(): LiveData<List<VegaProcessingStage>>
    suspend fun getBom(bomPostReq: VegaProcessingRminBomPost): LiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>>
    suspend fun getStroageLocation(): LiveData<List<VegaCustomStLocation>>
    suspend fun getFgrnPoDetailsList(stageFevor: String?, cfgNo: String?): LiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>>


    suspend fun getStocks(material: String): LiveData<Resource<GenericReqAndResp<List<VegaDispatchLots>>>>
    suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaProcessingQualityDetails>>>>

    suspend fun postCreatePo(bomPostReq: VegaProcessingCreatePoReq): LiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>>
    suspend fun getFgrnGrades(poNumber: String): LiveData<Resource<GenericReqAndResp<List<VegaFgrnGrades>>>>
    suspend fun postFgrnDetails(fgrnReq: VegaProcessingFgrnPost): LiveData<Resource<GenericReqAndResp<List<VegaProcessingFgrnResponse>>>>
    suspend fun saveRmin(
        bom: VegaProcessingRminBoms,
        processLots: ProcessingLotDetails,
        stageFevor: String?,
        materialName: String,
        materialNo: String
    )

    suspend fun updateRminData(batchNo: String?, msg: String, syncStatus: Boolean)
}

class VegaProcessingRepositoryImpl(
    private val api: VegaProcessingApi,
    private val dao: VegaProcessingDao,
    private val masterDao: MasterDao
) : VegaProcessingRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getGrades() = dao.getProducts()
    override suspend fun getStages() = dao.getProcessingStage()
    override suspend fun getBom(bomPostReq: VegaProcessingRminBomPost): LiveData<Resource<GenericReqAndResp<VegaProcessingRminBom>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaProcessingRminBom>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaProcessingRminBom> =
                api.fetchBomList(bomPostReq)
        }.build().asLiveData()
    }

    override suspend fun getStroageLocation(): LiveData<List<VegaCustomStLocation>> = dao.getCustomLocations()


    override suspend fun getStocks(material: String): LiveData<Resource<GenericReqAndResp<List<VegaDispatchLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaDispatchLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaDispatchLots>> =
                api.getStocks(currentKey, material)
        }.build().asLiveData()
    }

    override suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaProcessingQualityDetails>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaProcessingQualityDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaProcessingQualityDetails>> =
                api.getQuality(currentKey, charge, material)
        }.build().asLiveData()
    }

    override suspend fun postCreatePo(bomPostReq: VegaProcessingCreatePoReq): LiveData<Resource<GenericReqAndResp<VegaProcessingRminPo>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaProcessingRminPo>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaProcessingRminPo> =
                api.postCreatePo(bomPostReq)
        }.build().asLiveData()
    }

    override suspend fun getFgrnGrades(poNumber: String): LiveData<Resource<GenericReqAndResp<List<VegaFgrnGrades>>>> {
        val poRequest = VegaProcessOrderDetailsPostReq(currentKey, getPlantDetails(), poNumber)
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaFgrnGrades>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaFgrnGrades>> =
                api.getFgrnGrades(poRequest)
        }.build().asLiveData()
    }

    override suspend fun postFgrnDetails(fgrnReq: VegaProcessingFgrnPost): LiveData<Resource<GenericReqAndResp<List<VegaProcessingFgrnResponse>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaProcessingFgrnResponse>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaProcessingFgrnResponse>> =
                api.postFgrnDetails(fgrnReq)
        }.build().asLiveData()
    }

    override suspend fun saveRmin(
        bom: VegaProcessingRminBoms,
        processLots: ProcessingLotDetails,
        stageFevor: String?,
        materialName: String,
        materialNo: String
    ) {
        dao.saveRminBom(
            prepareRminCreatePo(
                bom,
                stageFevor.toString(),
                materialName,
                materialNo,
                processLots.batchNumber
            )
        )
        processLots.cfgNo = bom.cfgno
        dao.saveProcessingLots(processLots)
    }

    override suspend fun updateRminData(batchNo: String?, msg: String, syncStatus: Boolean) {
        when (syncStatus) {
            true -> dao.updateRminData(batchNo, msg, syncStatus, Status.RMIN_COMPLETED)
            false -> dao.updateRminData(batchNo, msg, syncStatus, Status.SYNC_ERROR)
        }

    }

    override suspend fun getFgrnPoDetailsList(stageFevor: String?, cfgNo: String?): LiveData<Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaFgrnProcessingOrder>> =
                api.getFgrnPoDetailsList(VegaProcessingOrderReq(getCurrentKey(), getPlantDetails(), stageFevor, cfgNo))
        }.build().asLiveData()
    }

}
