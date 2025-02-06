package com.olam.warehouse.vegax.grncoffee.data.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeGrnDao
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeGrnPost
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeGrnResponse
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.grncoffee.data.api.VegaCoffeeGrnApi
import com.olam.warehouse.vegax.grncoffee.utils.prepareData
import com.olam.warehouse.vegax.grncoffee.utils.prepareWeighBridgeData
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
interface VegaCoffeeGrnRepository {
    suspend fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun getStorageLocation(code: String): LiveData<VegaStorageLocation>
    suspend fun postGrn(grnPost: VegaCoffeeGrnPost): LiveData<Resource<GenericReqAndResp<VegaCoffeeGrnResponse>>>
    suspend fun getWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>>
    suspend fun updateGRNPrice(wbDetails: VegaGrnWeighBridgeId)
    suspend fun updateDeletedItem(weighBridgeId: String)
    suspend fun updateGrnSuccess(wbid: String, grnNo: String, batch: String, msg: String, status: Int)
    suspend fun getOfflineWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>>
    suspend fun getOfflineWeighBridgeDetailCount(): LiveData<List<VegaGrnWeighBridgeId>>
    suspend fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String)
    suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>

    suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>
    suspend fun getWeighBridgeIdDetail(
            wbid: String,
            isWeighscale: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaReceiving>>>
    suspend fun getQualityParams(
            materialId: String,
            valueExist: Boolean?,
            wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>
}

class VegaCoffeeGrnRepositoryImpl(private val api: VegaCoffeeGrnApi, private val dao: VegaCoffeeGrnDao) :
    VegaCoffeeGrnRepository {

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaGrnWeighBridgeId>> =
                api.fetchWeighBridgeList(currentKey)

        }.build().asLiveData()
    }

    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)
    override suspend fun getStorageLocation(code: String) = dao.getStorageLocation(code)

    override suspend fun postGrn(grnPost: VegaCoffeeGrnPost): LiveData<Resource<GenericReqAndResp<VegaCoffeeGrnResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeeGrnResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaCoffeeGrnResponse> =
                api.postGrn(grnPost)
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

    override suspend fun updateGRNPrice(wbDetails: VegaGrnWeighBridgeId) = dao.updateGRNPrice(wbDetails)
    override suspend fun updateDeletedItem(weighBridgeId: String) {
        dao.updateDeletedItem(weighBridgeId, "")
        dao.updateDeletedItemQuality(weighBridgeId)
    }

    override suspend fun updateGrnSuccess(wbid: String, grnNo: String, batch: String, msg: String, status: Int) {
        dao.updateGrnSuccess(wbid, grnNo, batch, msg, status)
    }

    override suspend fun getOfflineWeighBridgeDetail() = dao.getOfflineWeighBridgeDetail()

    override suspend fun getOfflineWeighBridgeDetailCount() = dao.getOfflineWeighBridgeDetailCount()

    override suspend fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String) =
        dao.updateGrnNoToQuality(wbid, grnNo, batchNo)

    override suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityPreParameter>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityPreParameter>> =
                api.fetchPreQualityDetails(currentKey, batchNo, materialId)
        }.build().asLiveData()
    }

    override suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>> =
                api.getPurchaseOrders(getCurrentKey())
        }.build().asLiveData()
    }
    override suspend fun getWeighBridgeIdDetail(
            wbid: String,
            isWeighscale: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaReceiving>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceiving>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceiving> =
                    if (isWeighscale) api.getWeighScaleIdDetail(
                            getCurrentKey(),
                            wbid
                    ) else api.getWeighBridgeIdDetail(getCurrentKey(), wbid)
        }.build().asLiveData()
    }

    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return if (valueExist!!) prepareData(dao.getQualityParameterWithData(materialId, wbId)) else
            dao.getQualityParameter(materialId)
    }

}
