package com.olam.warehouse.vegax.grnnigeria.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaNigeriaCocoaOffloadingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorGrnDao
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.grnnigeria.data.api.VegaNigeriaGrnApi
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.VegaCameroonQcPost
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.VegaNigeriaCocoaQualityApprovePostResponse
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.VegaNigeriaGRNQuality
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.VegaNigeriaGrnPost
import com.olam.warehouse.vegax.grnnigeria.utils.prepareWeighBridgeData

/**
 * Created by Roshna Parambil on 9/9/2020.
 */
interface VegaNigeriaGrnRepository {
    suspend fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun getStorageLocation(code: String): LiveData<VegaStorageLocation>
    suspend fun postNigeriaCocoa(receivingData: VegaNigeriaCocoaOffloadingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>
    suspend fun postGrn(grnPost: VegaNigeriaGrnPost): LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>>
    suspend fun getWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>>
    suspend fun getStorageLocationDetail(): LiveData<List<VegaStorageLocationDetail>>
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>>
    suspend fun getAllProduct(): LiveData<List<VegaMaterial>>
    suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>>
    suspend fun updateGRNPrice(wbDetails: VegaGrnWeighBridgeId)
    suspend fun updateDeletedItem(weighBridgeId: String)
    suspend fun updateGrnSuccess(
        wbid: String,
        grnNo: String,
        batch: String,
        msg: String,
        status: Int
    )

    suspend fun getOfflineWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>>
    suspend fun getOfflineWeighBridgeDetailCount(): LiveData<List<VegaGrnWeighBridgeId>>
    suspend fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String)
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaGRNQuality>>>>

    suspend fun getQualityParamsDB(
        material: String, entryObligatory: String
    ): LiveData<List<VegaQualityParameter>>

    suspend fun postQuality(qualityPost: VegaCameroonQcPost): LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaQualityApprovePostResponse>>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
}

class VegaNigeriaGrnRepositoryImpl(private val api: VegaNigeriaGrnApi, private val dao: VegaEcuadorGrnDao) : VegaNigeriaGrnRepository {

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaGrnWeighBridgeId>> =
                api.fetchWeighBridgeList(currentKey)

        }.build().asLiveData()
    }

    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)
    override suspend fun getStorageLocation(code: String) = dao.getStorageLocation(code)

    override suspend fun postGrn(grnPost: VegaNigeriaGrnPost): LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaEcuadorGrnResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaEcuadorGrnResponse> =
                api.postGrn(grnPost)
        }.build().asLiveData()
    }

    override suspend fun postNigeriaCocoa(receivingData: VegaNigeriaCocoaOffloadingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingResponse> =
                api.postNigeriaCocoa(receivingData)
        }.build().asLiveData()
    }

    override suspend fun getMaterials() = dao.getMaterials()

    override suspend fun getStorageLocationDetail(): LiveData<List<VegaStorageLocationDetail>> =
        dao.getStorageLocationDetail()

    override suspend fun getWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>> {
        val receiveItem = dao.getReceivingDetail()
        receiveItem.forEach {
            if (dao.isWBExist(it.tmpWbId).isNotEmpty()) return@forEach
            dao.insertQualityWbDetail(prepareWeighBridgeData(it))
        }
        return dao.getQualityWeighBridgeDetail()
    }

    override suspend fun updateGRNPrice(wbDetails: VegaGrnWeighBridgeId) =
        dao.updateGRNPrice(wbDetails)

    override suspend fun updateDeletedItem(weighBridgeId: String) {
        dao.updateDeletedItem(weighBridgeId, "")
        dao.updateDeletedItemQuality(weighBridgeId)
    }

    override suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        dao.getProcessTypeList(role)

    override suspend fun getAllProduct(): LiveData<List<VegaMaterial>> = dao.getAllProducts()

    override suspend fun updateGrnSuccess(
        wbid: String,
        grnNo: String,
        batch: String,
        msg: String,
        status: Int
    ) {
        dao.updateGrnSuccess(wbid, grnNo, batch, msg, status)
    }

    override suspend fun getOfflineWeighBridgeDetail() = dao.getOfflineWeighBridgeDetail()

    override suspend fun getOfflineWeighBridgeDetailCount() = dao.getOfflineWeighBridgeDetailCount()

    override suspend fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String) =
        dao.updateGrnNoToQuality(wbid, grnNo, batchNo)

    override suspend fun getSuppliers() = dao.getSuppliers()

    override suspend fun getQualityParamsDB(
        material: String, entryObligatory: String
    ) = dao.getQualityParamsDB(material, entryObligatory)

    override suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaGRNQuality>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNigeriaGRNQuality>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaNigeriaGRNQuality>> =
                api.getQuality(currentKey, charge, material)

        }.build().asLiveData()
    }

    override suspend fun getCustomLocations() = dao.getCustomLocations()

    override suspend fun postQuality(qualityPost: VegaCameroonQcPost): LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaQualityApprovePostResponse>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<VegaNigeriaCocoaQualityApprovePostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaNigeriaCocoaQualityApprovePostResponse> =
                api.postQuality(qualityPost)
        }.build().asLiveData()
    }

}
