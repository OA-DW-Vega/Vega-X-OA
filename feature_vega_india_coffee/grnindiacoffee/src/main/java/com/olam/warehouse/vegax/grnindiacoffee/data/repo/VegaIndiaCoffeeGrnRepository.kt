package com.olam.warehouse.vegax.grnindiacoffee.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorGrnDao
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.grnindiacoffee.data.api.VegaIndiaCoffeeGrnApi
import com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.model.VegaCameroonQcPost
import com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.model.VegaIndiaCoffeeGRNQuality
import com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.model.VegaIndiaCoffeeGrnPost
import com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.model.VegaIndiaCoffeeQualityApprovePostResponse
import com.olam.warehouse.vegax.grnindiacoffee.utils.prepareWeighBridgeData


interface VegaIndiaCoffeeGrnRepository {
    suspend fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun getStorageLocation(code: String): LiveData<VegaStorageLocation>
    suspend fun postGrn(grnPost: VegaIndiaCoffeeGrnPost): LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>>
    suspend fun getWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun updateGRNPrice(wbDetails: VegaGrnWeighBridgeId)
    suspend fun updateDeletedItem(weighBridgeId: String)
    suspend fun postQuality(qualityPost: VegaCameroonQcPost): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeQualityApprovePostResponse>>>
    suspend fun updateGrnSuccess(wbid: String, grnNo: String, batch: String, msg: String, status: Int)
    suspend fun getOfflineWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>>
    suspend fun getOfflineWeighBridgeDetailCount(): LiveData<List<VegaGrnWeighBridgeId>>
    suspend fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String)
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>>
    suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>
    suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeGRNQuality>>>>
}

class VegaIndiaCoffeeGrnRepositoryImpl(
    private val apiIndiaCoffee: VegaIndiaCoffeeGrnApi,
    private val dao: VegaEcuadorGrnDao
) : VegaIndiaCoffeeGrnRepository {

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaGrnWeighBridgeId>> =
                apiIndiaCoffee.fetchWeighBridgeList(currentKey)

        }.build().asLiveData()
    }

    override suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeGRNQuality>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaIndiaCoffeeGRNQuality>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaIndiaCoffeeGRNQuality>> =
                apiIndiaCoffee.getQuality(currentKey, charge, material)

        }.build().asLiveData()
    }

    override suspend fun getCustomLocations() = dao.getCustomLocations()


    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)
    override suspend fun getStorageLocation(code: String) = dao.getStorageLocation(code)

    override suspend fun postGrn(grnPost: VegaIndiaCoffeeGrnPost): LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaEcuadorGrnResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaEcuadorGrnResponse> =
                apiIndiaCoffee.postGrn(grnPost)
        }.build().asLiveData()
    }

    override suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>> =
                apiIndiaCoffee.getPurchaseOrders(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun getWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>> {
        val receiveItem = dao.getReceivingDetail()
        receiveItem.forEach {
            if (dao.isWBExist(it.tmpWbId).isNotEmpty()) return@forEach
            dao.insertQualityWbDetail(prepareWeighBridgeData(it))
        }
        return dao.getQualityWeighBridgeDetail()
    }

    override suspend fun postQuality(qualityPost: VegaCameroonQcPost): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeQualityApprovePostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaIndiaCoffeeQualityApprovePostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaIndiaCoffeeQualityApprovePostResponse> =
                apiIndiaCoffee.postQuality(qualityPost)
        }.build().asLiveData()
    }

    override suspend fun updateGRNPrice(wbDetails: VegaGrnWeighBridgeId) = dao.updateGRNPrice(wbDetails)
    override suspend fun updateDeletedItem(weighBridgeId: String) {
        dao.updateDeletedItem(weighBridgeId, "")
        dao.updateDeletedItemQuality(weighBridgeId)
    }

    override suspend fun updateGrnSuccess(wbid: String, grnNo: String, batch: String, msg: String, status: Int) {
        dao.updateGrnSuccess(wbid, grnNo, batch, msg, status)
    }

    override suspend fun getOfflineWeighBridgeDetail() = dao.getOfflineWeighBridgeDetail()

    override suspend fun getMaterials() = dao.getMaterials()

    override suspend fun getOfflineWeighBridgeDetailCount() = dao.getOfflineWeighBridgeDetailCount()

    override suspend fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String) =
        dao.updateGrnNoToQuality(wbid, grnNo, batchNo)
}
