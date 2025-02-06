package com.olam.warehouse.vegax.pilemanagementnigeria.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeDispatchDao
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeRminDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaMtntDao
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.pilemanagementnigeria.data.api.VegaNigeriaPileManagementApi
import com.olam.warehouse.vegax.pilemanagementnigeria.data.domain.model.*
import java.util.*

interface VegaNigeriaPileManagementRepository {

    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>
    suspend fun getStockPile(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaPileSelectionModel>>>>
    suspend fun removeLot(batchNumber: String)
    suspend fun insertThirdPartyModel(model: VegaCoffeeThirdPartyRequestModel)
    suspend fun insertLotList(lot: List<VegaCocoaDispatchLots>)
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getCreatePile(): LiveData<Resource<GenericReqAndResp<VegaNigeriaPileSequence>>>
    suspend fun validateLot(batchNumber: String): VegaCocoaDispatchLots
    suspend fun saveLots(
        dispatchLotsList: MutableList<VegaCocoaDispatchLots>
    )

    suspend fun saveDispatchAndLots(
        salesOrder: VegaCoffeeSalesOrder,
        lots: ArrayList<VegaCoffeeSalesLots>
    )

    suspend fun getQualityParams(
        charge: String,
        material: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>

    suspend fun getShiftRemarks(role: String): LiveData<List<VegaCocoaMiscellaneous>>
    suspend fun getThirdPartyMaterials(): List<VegaCoffeeThirdPartyMaterialDetail>
    suspend fun getPostPile(postPileRequest: VegaNigeriaPilePostRequest): LiveData<Resource<GenericReqAndResp<VegaNigeriaPileSuccessResponse>>>
    suspend fun getPackingMaterial(): LiveData<List<VegaPackageMaterial>>
}

class VegaNigeriaPileManagementRepositoryImpl(
    private val api: VegaNigeriaPileManagementApi,
    private val dao: VegaCoffeeDispatchDao,
    private val daoM: VegaCoffeeRminDao,
    private val daoN: VegaNicaraguaMtntDao

) : VegaNigeriaPileManagementRepository {

    override suspend fun getQualityParams(
        charge: String,
        material: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                api.getQuality(getCurrentKey(), charge, material, whId)

        }.build().asLiveData()
    }

    override suspend fun getShiftRemarks(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        daoM.getShiftRemarkItems(role)

    override suspend fun getPostPile(postPileRequest: VegaNigeriaPilePostRequest): LiveData<Resource<GenericReqAndResp<VegaNigeriaPileSuccessResponse>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<VegaNigeriaPileSuccessResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaNigeriaPileSuccessResponse> =
                api.getPostPile(postPileRequest)

        }.build().asLiveData()
    }

    override suspend fun validateLot(whId: String) = dao.validateLotAlreadyAdded(whId)

    override suspend fun saveDispatchAndLots(
        salesOrder: VegaCoffeeSalesOrder,
        lots: ArrayList<VegaCoffeeSalesLots>
    ) {
        dao.insertSalesOrderDetail(salesOrder)
        lots.forEach {
            it.isAdded = true
        }
        dao.saveLotset(lots)
    }

    override suspend fun saveLots(
        dispatchLotsList: MutableList<VegaCocoaDispatchLots>
    ) {
        dao.saveLots(dispatchLotsList)
    }

    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getProducts() = dao.getAllProducts()
    override suspend fun removeLot(whId: String) = dao.removeLots(whId)

    override suspend fun getCustomLocations() = dao.getCustomLocations()

    override suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                api.getStockList(getCurrentKey(), materialList)
        }.build().asLiveData()
    }

    override suspend fun getCreatePile(): LiveData<Resource<GenericReqAndResp<VegaNigeriaPileSequence>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNigeriaPileSequence>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaNigeriaPileSequence> =
                api.getCreatePile(
                    getCurrentKey(),
                    VegaNigeriaPilePlantDetails(plant = getPlantDetails())
                )
        }.build().asLiveData()
    }


    override suspend fun getStockPile(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaPileSelectionModel>>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNigeriaPileSelectionModel>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNigeriaPileSelectionModel>> =
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

    override suspend fun getThirdPartyMaterials(): List<VegaCoffeeThirdPartyMaterialDetail> =
        daoM.getThirdPartyMaterials()

    override suspend fun getPackingMaterial(): LiveData<List<VegaPackageMaterial>> = dao.getMaterials()
}
