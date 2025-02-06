package com.olam.warehouse.vegax.gateentryapprovalnigeria.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.gateentryapprovalnigeria.data.domain.model.VegaDMSImageResponse
import com.olam.warehouse.vegax.gateentryapprovalnigeria.data.domain.model.VegaGateEntryApprovalNigeriaPost
import com.olam.warehouse.vegax.gateentryapprovalnigeria.data.repo.VegaGateEntryApprovalNigeriaRepository

/**
 * Created by Keerthi Santhanam on 3/5/2020.
 */
class VegaGateEntryApprovalNigeriaUseCase(private val repository: VegaGateEntryApprovalNigeriaRepository) {

    //    suspend fun getWaitingTrucks() = repository.getWaitingTrucks()
    suspend fun getWaitingTrucks1(selectedPlantId: String) =
        repository.getWaitingTrucks1(selectedPlantId)

    suspend fun fetchWbDetails(selectedPlantId: String) = repository.fetchWbDetails(selectedPlantId)
    suspend fun getStorageLocation(code: String) = repository.getStorageLocation(code)
    suspend fun postGateEntryData(gateEntryPost: VegaGateEntryApprovalNigeriaPost) =
        repository.postGateEntryData(gateEntryPost)

    suspend fun getProducts() = repository.getProducts()
    suspend fun getDMSUploadedImages(
        wbId: String,
        werks: String,
        year: String
    ): LiveData<Resource<GenericReqAndResp<VegaDMSImageResponse>>> =
        repository.getDMSUploadedImages(wbId, werks, year)

    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getSupplierZone(bcApprover: String) = repository.getSupplierZone(bcApprover)
    suspend fun fetchWarehouseWithMtns() = repository.fetchWarehouseWithMtns()
    suspend fun getCustomLocations() = repository.getCustomLocations()
//    suspend fun getMultiPlantList() = repository.getMultiPlantList()

}
