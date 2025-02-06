package com.olam.warehouse.vegax.pileecuador.ui.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeDispatchDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.pileecuador.ui.data.api.VegaEcuadorPileManagementApi
import com.olam.warehouse.vegax.pileecuador.ui.data.domain.model.*
import java.util.*

interface VegaEcuadorPileManagementRepository {

    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>
    suspend fun getStockPile(materialList: String): LiveData<Resource<GenericReqAndResp<List<VegaPileSelectionModel>>>>
    suspend fun removeLot(batchNumber: String)
    suspend fun insertThirdPartyModel(model: VegaCoffeeThirdPartyRequestModel)
    suspend fun insertLotList(lot: List<VegaCocoaDispatchLots>)
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getCreatePile(isPost: Boolean): LiveData<Resource<GenericReqAndResp<VegaPileSequence>>>
    suspend fun validateLot(batchNumber: String): VegaCocoaDispatchLots

    suspend fun saveDispatchAndLots(salesOrder: VegaCoffeeSalesOrder, lots: ArrayList<VegaCoffeeSalesLots>)
    suspend fun getQualityParams(
        charge: String,
        material: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>

    suspend fun getPostPile(postPileRequest: VegaCoffeePilePostRequest): LiveData<Resource<GenericReqAndResp<VegaCoffeePileResponse>>>

}

class VegaEcuadorPileManagementRepositoryImpl(
    private val api: VegaEcuadorPileManagementApi,
    private val dao: VegaCoffeeDispatchDao

) : VegaEcuadorPileManagementRepository {

    override suspend fun getQualityParams(
        charge: String,
        material: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                api.getQuality(getCurrentKey(), charge, material, whId)

        }.build().asLiveData()
    }

    override suspend fun getPostPile(postPileRequest: VegaCoffeePilePostRequest): LiveData<Resource<GenericReqAndResp<VegaCoffeePileResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeePileResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaCoffeePileResponse> =
                api.getPostPile(postPileRequest)

        }.build().asLiveData()
    }

    override suspend fun validateLot(whId: String) = dao.validateLotAlreadyAdded(whId)

    override suspend fun saveDispatchAndLots(salesOrder: VegaCoffeeSalesOrder, lots: ArrayList<VegaCoffeeSalesLots>) {
        dao.insertSalesOrderDetail(salesOrder)
        lots.forEach {
            it.isAdded = true
        }
        dao.saveLotset(lots)
    }

    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getProducts() = dao.getAllProducts()
    override suspend fun removeLot(whId: String) = dao.removeLots(whId)

    override suspend fun getCustomLocations() = dao.getPileCustomLocations()

    override suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                api.getStockList(getCurrentKey(), materialList)
        }.build().asLiveData()
    }

    override suspend fun getCreatePile(isPost: Boolean): LiveData<Resource<GenericReqAndResp<VegaPileSequence>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaPileSequence>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaPileSequence> =
                api.getCreatePile(getCurrentKey(), isPost, VegaCoffeePilePlantDetails(plant = getPlantDetails()))
        }.build().asLiveData()
    }


    override suspend fun getStockPile(materialList: String): LiveData<Resource<GenericReqAndResp<List<VegaPileSelectionModel>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaPileSelectionModel>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaPileSelectionModel>> =
                api.getStockPile(getCurrentKey(), materialList)
        }.build().asLiveData()
    }

    override suspend fun insertThirdPartyModel(model: VegaCoffeeThirdPartyRequestModel) {
        dao.saveThirdPartyInfo(model)
    }

    override suspend fun insertLotList(lot: List<VegaCocoaDispatchLots>) {
        lot.forEach {
            it.isAdded = true
        }
        dao.saveLots(lot)
    }

}
