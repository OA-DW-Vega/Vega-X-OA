package com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeBagIssue
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeBagIssuePost
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeCurrentBagsIssued
import com.olam.warehouse.vegax.bagissueindiacoffee.data.repo.VegaIndiaCoffeeBagIssueRepository


/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
class VegaIndiaCoffeeBagIssueUseCase(
    private val repository: VegaIndiaCoffeeBagIssueRepository
) {
    suspend fun getMaterials() = repository.getMaterials()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getCurrentBagsIssued(materialCode:String,supplierCode:String,storageLocation:String): LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeCurrentBagsIssued>>>> = repository.getCurrentBagsIssued(materialCode,supplierCode,storageLocation)
    suspend fun postBagIssueData(bagIssuePost: VegaIndiaCoffeeBagIssuePost) =
        repository.postBagIssueData(bagIssuePost)

}
