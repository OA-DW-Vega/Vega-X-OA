package com.olam.warehouse.vegax.grnindo.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaStorageLocation
import com.olam.warehouse.master.vega.entity.VegaUploadPrintRequest
import com.olam.warehouse.master.vega.entity.VegaUploadPrintResponse
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorGrnDao
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.grnindo.data.api.VegaIndoCoffeeGrnApi
import com.olam.warehouse.vegax.grnindo.utils.PROCURE
import com.olam.warehouse.vegax.grnindo.utils.prepareQualityWeighBridgeData

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */
interface VegaIndoCoffeeGrnRepository {
    suspend fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun getStorageLocation(code: String): LiveData<VegaStorageLocation>
    suspend fun postGrn(grnPost: VegaEcuadorGrnPost): LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>>
    suspend fun getWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>>
    suspend fun getWeighBridgeTransDetail(): LiveData<List<VegaGrnWeighBridgeId>>
    suspend fun updateGRNPrice(wbDetails: VegaGrnWeighBridgeId)
    suspend fun updateDeletedItem(weighBridgeId: String)
    suspend fun updateGrnSuccess(
        wbid: String,
        grnNo: String,
        batch: String,
        msg: String,
        status: Int,
        wbDetails: VegaGrnWeighBridgeId
    )

    suspend fun getOfflineWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>>
    suspend fun getOfflineWeighBridgeDetailCount(): LiveData<List<VegaGrnWeighBridgeId>>
    suspend fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String)
    suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>
    suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>

    suspend fun getPOListOffline(): LiveData<List<VegaEcuadorPurchaseOrder>>
    suspend fun deleteAllItem(wbTempId: String)
    suspend fun getQualityParams(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>
    suspend fun getWeighBridgeIdDetail(
        wbid: String
    ): LiveData<Resource<GenericReqAndResp<VegaReceiving>>>

    suspend fun uploadGrnPrint(req : VegaUploadPrintRequest): LiveData<Resource<GenericReqAndResp<VegaUploadPrintResponse>>>

}

class VegaIndoCoffeeGrnRepositoryImpl(private val api: VegaIndoCoffeeGrnApi, private val dao: VegaEcuadorGrnDao) :
    VegaIndoCoffeeGrnRepository {

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
        val qualityItem = dao.getQualityOfflineList().filter { it.weighBridgeType == PROCURE }.filter { it.status != 4 }
        qualityItem.forEach {
            if (dao.isWBExist(it.weighBridgeId).isNotEmpty()) return@forEach
            dao.insertQualityWbDetail(prepareQualityWeighBridgeData(it))
        }
        return dao.getQualityWeighBridgeDetail()
    }

    override suspend fun getWeighBridgeTransDetail() = dao.getWeighBridgeTransDetail()

    override suspend fun updateGRNPrice(wbDetails: VegaGrnWeighBridgeId) {
        wbDetails.wbTempId =
            if (wbDetails.wbTempId.isEmpty()) wbDetails.weighBridgeId.toString() else wbDetails.wbTempId
        dao.updateGRNPrice(wbDetails)
    }

    override suspend fun updateDeletedItem(weighBridgeId: String) {
        dao.updateDeletedItem(weighBridgeId, "")
        dao.updateDeletedItemQuality(weighBridgeId)
    }

    override suspend fun updateGrnSuccess(
        wbid: String,
        grnNo: String,
        batch: String,
        msg: String,
        status: Int,
        wbDetails: VegaGrnWeighBridgeId
    ) {
        wbDetails.wbTempId =
            if (wbDetails.wbTempId.isEmpty()) wbDetails.weighBridgeId.toString() else wbDetails.wbTempId
        wbDetails.grnNumber = grnNo
        wbDetails.batchNumber = batch
        wbDetails.message = msg
        wbDetails.status = status
        wbDetails.isTransStatus = true
        wbDetails.isSyncStatus = status == 4
        dao.insertWeighBridge(wbDetails)
    }

    override suspend fun getOfflineWeighBridgeDetail() = dao.getOfflineWeighBridgeDetail()

    override suspend fun getOfflineWeighBridgeDetailCount() = dao.getOfflineWeighBridgeDetailCount()

    override suspend fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String) =
        dao.updateGrnNoToQuality(wbid, grnNo, batchNo)

    override suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>> =
                api.getPurchaseOrders(getCurrentKey())
        }.build().asLiveData()
    }

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


    override suspend fun getWeighBridgeIdDetail(
        wbid: String
    ): LiveData<Resource<GenericReqAndResp<VegaReceiving>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceiving>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceiving> =
                api.getWeighBridgeIdDetail(getCurrentKey(), wbid)
        }.build().asLiveData()
    }

    override suspend fun uploadGrnPrint(req: VegaUploadPrintRequest): LiveData<Resource<GenericReqAndResp<VegaUploadPrintResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaUploadPrintResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaUploadPrintResponse> =
                api.uploadGrnPrint(req)
        }.build().asLiveData()
    }


    override suspend fun getPOListOffline(): LiveData<List<VegaEcuadorPurchaseOrder>> = dao.getPOListOffline()
    override suspend fun deleteAllItem(wbTempId: String) = dao.updatedeleteStatus(wbTempId)
    override suspend fun getQualityParams(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>  = dao.getQualityParameter(materialId)
}

