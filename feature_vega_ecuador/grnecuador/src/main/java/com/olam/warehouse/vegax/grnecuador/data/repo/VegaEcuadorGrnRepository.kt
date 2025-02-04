package com.olam.warehouse.vegax.grnecuador.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaStorageLocation
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorGrnDao
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.grnecuador.data.api.VegaEcuadorGrnApi
import com.olam.warehouse.vegax.grnecuador.utils.prepareWeighBridgeData

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
interface VegaEcuadorGrnRepository {
    suspend fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun getStorageLocation(code: String): LiveData<VegaStorageLocation>
    suspend fun postGrn(grnPost: VegaEcuadorGrnPost): LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>>
    suspend fun getWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>>
    suspend fun updateGRNPrice(wbDetails: VegaGrnWeighBridgeId)
    suspend fun updateDeletedItem(weighBridgeId: String)
    suspend fun updateGrnSuccess(wbid: String, grnNo: String, batch: String, msg: String, status: Int)
    suspend fun getOfflineWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>>
    suspend fun getOfflineWeighBridgeDetailCount(): LiveData<List<VegaGrnWeighBridgeId>>
    suspend fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String)
}

class VegaEcuadorGrnRepositoryImpl(private val api: VegaEcuadorGrnApi, private val dao: VegaEcuadorGrnDao) : VegaEcuadorGrnRepository {

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaGrnWeighBridgeId>> =
                api.fetchWeighBridgeList(currentKey)

        }.build().asLiveData()
    }

    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)
    override suspend fun getStorageLocation(code: String) = dao.getStorageLocation(code)

    override suspend fun postGrn(grnPost: VegaEcuadorGrnPost): LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaEcuadorGrnResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaEcuadorGrnResponse> =
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
}
