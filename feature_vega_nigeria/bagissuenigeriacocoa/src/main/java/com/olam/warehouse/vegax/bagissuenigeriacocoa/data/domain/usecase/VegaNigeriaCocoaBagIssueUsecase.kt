package com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.model.VegaNigeriaCocoaBagIssuePost
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.model.VegaNigeriaCocoaCurrentBagsIssued
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.repo.VegaNigeriaCocoaBagIssueRepository


/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
class VegaNigeriaCocoaBagIssueUseCase(
    private val repository: VegaNigeriaCocoaBagIssueRepository
) {
    suspend fun getMaterials() = repository.getMaterials()
    suspend fun getSupplier(selectedPlantId: String) = repository.getSuppliers(selectedPlantId)
    suspend fun getVendors() = repository.getSuppliers()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getCurrentBagsIssued(materialCode:String,supplierCode:String,storageLocation:String): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaCurrentBagsIssued>>>> = repository.getCurrentBagsIssued(materialCode,supplierCode,storageLocation)
    suspend fun postBagIssueData(bagIssuePost: VegaNigeriaCocoaBagIssuePost) =
        repository.postBagIssueData(bagIssuePost)
    suspend fun getStocks(material: String) = repository.getStocks(material)


}
