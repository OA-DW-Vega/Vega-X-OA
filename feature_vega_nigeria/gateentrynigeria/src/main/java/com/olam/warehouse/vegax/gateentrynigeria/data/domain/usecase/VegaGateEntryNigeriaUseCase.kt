package com.olam.warehouse.vegax.gateentrynigeria.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.gateentrynigeria.data.domain.model.VegaGateEntryNigeriaPost
import com.olam.warehouse.vegax.gateentrynigeria.data.domain.model.VegaNigeriaGateEntryPostData
import com.olam.warehouse.vegax.gateentrynigeria.data.repo.VegaGateEntryNigeriaRepository

/**
 * Created by Keerthi Santhanam on 3/5/2020.
 */
class VegaGateEntryNigeriaUseCase(private val repository: VegaGateEntryNigeriaRepository) {

    //    suspend fun getWaitingTrucks() = repository.getWaitingTrucks()
    suspend fun getWaitingTrucks1(selectedPlantId: String) = repository.getWaitingTrucks1(
        selectedPlantId
    )

    suspend fun postGateEntryData(gateEntryPost: VegaGateEntryNigeriaPost) =
        repository.postGateEntryData(gateEntryPost)

    suspend fun getProducts() = repository.getProducts()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getSupplierZone(bcApprover: String) = repository.getSupplierZone(bcApprover)
    suspend fun fetchWarehouseWithMtns() = repository.fetchWarehouseWithMtns()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        repository.getProcessTypeList(role)

    suspend fun openGrntDetails(
        role: String,
        materialCodes: String,
        vendorCodes: String,
        plantId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaGateEntryPostData>>>> =
        repository.openGrntDetails(role, materialCodes, vendorCodes, plantId)
//    suspend fun getMultiPlantList() = repository.getMultiPlantList()

}
