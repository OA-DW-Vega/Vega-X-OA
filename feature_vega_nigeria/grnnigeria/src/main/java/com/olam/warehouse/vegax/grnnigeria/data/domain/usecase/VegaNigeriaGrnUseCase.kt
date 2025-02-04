package com.olam.warehouse.vegax.grnnigeria.data.domain.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.olam.warehouse.master.common.model.VegaNigeriaCocoaOffloadingPost
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaStorageLocationDetail
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.VegaCameroonQcPost
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.VegaNigeriaGRNQuality
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.VegaNigeriaGrnPost
import com.olam.warehouse.vegax.grnnigeria.data.repo.VegaNigeriaGrnRepository

/**
 * Created by Roshna Parambil on 9/9/2020.
 */
class VegaNigeriaGrnUseCase(
    private val repository: VegaNigeriaGrnRepository
) {
    suspend operator fun invoke(): LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> {
        return Transformations.map(repository.getWeighBridgeList()) {
            it // Place here your specific logic actions (if any)
        }
    }

    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> =
        repository.getConfigItems(role)

    suspend fun postNigeriaCocoa(receivingData: VegaNigeriaCocoaOffloadingPost) =
        repository.postNigeriaCocoa(receivingData)

    suspend fun getStorageLocation(code: String) = repository.getStorageLocation(code)

    suspend fun postGrn(grnPost: VegaNigeriaGrnPost): LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>> =
        repository.postGrn(grnPost)

    suspend fun getStorageLocationDetail(): LiveData<List<VegaStorageLocationDetail>> =
        repository.getStorageLocationDetail()

    suspend fun getWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>> =
        repository.getWeighBridgeDetail()

    suspend fun updateGRNPrice(wbDetails: VegaGrnWeighBridgeId) =
        repository.updateGRNPrice(wbDetails)

    suspend fun updateDeletedItem(weighBridgeId: String) =
        repository.updateDeletedItem(weighBridgeId)

    suspend fun updateGrnSuccess(
        wbid: String,
        grnNo: String,
        batch: String,
        msg: String,
        status: Int
    ) =
        repository.updateGrnSuccess(wbid, grnNo, batch, msg, status)

    suspend fun getOfflineWeighBridgeDetail() = repository.getOfflineWeighBridgeDetail()
    suspend fun getOfflineWeighBridgeDetailCount() = repository.getOfflineWeighBridgeDetailCount()
    suspend fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String) =
        repository.updateGrnNoToQuality(wbid, grnNo, batchNo)

    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getMaterials() = repository.getMaterials()
    suspend fun getAllProducts() = repository.getAllProduct()
    suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        repository.getProcessTypeList(role)

    suspend fun invokeQualityDB(material: String, entryObligatory: String) =
        repository.getQualityParamsDB(material, entryObligatory)

    suspend fun postQuality(qualityPost: VegaCameroonQcPost) = repository.postQuality(qualityPost)
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun invokeQuality(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaGRNQuality>>>> {
        return Transformations.map(repository.getQualityParams(charge, material)) {
            it
        }
    }

}
