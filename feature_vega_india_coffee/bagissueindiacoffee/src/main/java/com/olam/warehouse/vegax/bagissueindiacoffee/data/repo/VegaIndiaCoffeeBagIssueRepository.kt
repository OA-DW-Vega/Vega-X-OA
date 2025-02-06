package com.olam.warehouse.vegax.bagissueindiacoffee.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.dao.VegaReceivingDao
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.bagissueindiacoffee.data.api.VegaIndiaCoffeeBagIssueApi
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeBagIssue
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeBagIssuePost
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeBagIssueResponse
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeCurrentBagsIssued


/**
 * Created by Roshna Parambil on 11/29/2020.
 */
interface VegaIndiaCoffeeBagIssueRepository {
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getCurrentBagsIssued(materialCode:String,supplierCode:String,storageLocation:String): LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeCurrentBagsIssued>>>>
    suspend fun postBagIssueData(bagIssuePost: VegaIndiaCoffeeBagIssuePost): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeBagIssueResponse>>>

}

class VegaIndiaCoffeeBagIssueRepositoryImpl(
    private val api: VegaIndiaCoffeeBagIssueApi,
    private val dao: VegaReceivingDao
) : VegaIndiaCoffeeBagIssueRepository {

    val werks = PreferenceHelper.get(Constants.WERKS, "")

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getMaterials() = dao.getMaterials()
    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getCustomLocations() = dao.getCustomLocations()

    override suspend fun getCurrentBagsIssued(materialCode:String,supplierCode:String,storageLocation:String): LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeCurrentBagsIssued>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaIndiaCoffeeCurrentBagsIssued>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaIndiaCoffeeCurrentBagsIssued>> =
                api.getCurrentBagsIssued(currentKey,materialCode,supplierCode)
        }.build().asLiveData()
    }

    override suspend fun postBagIssueData(bagIssuePost: VegaIndiaCoffeeBagIssuePost): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeBagIssueResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaIndiaCoffeeBagIssueResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaIndiaCoffeeBagIssueResponse> =
                api.postBagIssueData(bagIssuePost)
        }.build().asLiveData()
    }

}
