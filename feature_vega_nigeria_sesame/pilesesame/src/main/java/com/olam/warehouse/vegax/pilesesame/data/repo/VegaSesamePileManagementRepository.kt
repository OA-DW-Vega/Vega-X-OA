package com.olam.warehouse.vegax.pilesesame.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeDispatchDao
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeRminDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaGrnDao
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaMtntDao
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.pilesesame.data.api.VegaSesamePileManagementApi
import com.olam.warehouse.vegax.pilesesame.data.domain.model.*
import java.util.*

interface VegaSesamePileManagementRepository {

    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>
    suspend fun getStockPile(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaSesamePileSelectionModel>>>>
    suspend fun removeLot(batchNumber: String)
    suspend fun insertThirdPartyModel(model: VegaCoffeeThirdPartyRequestModel)
    suspend fun insertLotList(lot: List<VegaCocoaDispatchLots>)
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getStoreLocations(): LiveData<List<VegaStorageLocation>>
    suspend fun getCreatePile(): LiveData<Resource<GenericReqAndResp<VegaSesamePileSequence>>>
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
    suspend fun getSapMaterials(): List<VegaMaterial>
    suspend fun getPostPile(postPileRequest: VegaSesamePilePostRequest): LiveData<Resource<GenericReqAndResp<VegaSesamePileSuccessResponse>>>
    suspend fun getGrades(materialCode: String): LiveData<List<VegaQualitative>>
    suspend fun getMaterialQualityGrades(materialCode: String): LiveData<List<VegaNicaraguaMaterialQualitGrades>>
    suspend fun updatePileSequence(postData: NicaraguaUpdatePileSequence): LiveData<Resource<GenericReqAndResp<NicaraguaUpdatePileSequence>>>

}

class VegaSesamePileManagementRepositoryImpl(
    private val api: VegaSesamePileManagementApi,
    private val dao: VegaCoffeeDispatchDao,
    private val daoM: VegaCoffeeRminDao,
    private val daoNic: VegaNicaraguaGrnDao,
    private val daoMtntNic: VegaNicaraguaMtntDao

) : VegaSesamePileManagementRepository {

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

    override suspend fun getPostPile(postPileRequest: VegaSesamePilePostRequest): LiveData<Resource<GenericReqAndResp<VegaSesamePileSuccessResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaSesamePileSuccessResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaSesamePileSuccessResponse> =
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

    override suspend fun saveLots(
        dispatchLotsList: MutableList<VegaCocoaDispatchLots>
    ) {
        dao.saveLots(dispatchLotsList)
    }

    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getProducts() = dao.getAllProducts()
    override suspend fun removeLot(whId: String) = dao.removeLots(whId)

    override suspend fun getCustomLocations() = dao.getCustomLocations()

    override suspend fun getStoreLocations() = daoNic.getStorageLocations(getPlantDetails().plantId)

    override suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                api.getStockList(getCurrentKey(), materialList)
        }.build().asLiveData()
    }

    override suspend fun getCreatePile(): LiveData<Resource<GenericReqAndResp<VegaSesamePileSequence>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaSesamePileSequence>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaSesamePileSequence> =
                api.getCreatePile(getCurrentKey(), VegaSesamePilePlantDetails(plant = getPlantDetails()))
        }.build().asLiveData()
    }


    override suspend fun getStockPile(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaSesamePileSelectionModel>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaSesamePileSelectionModel>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaSesamePileSelectionModel>> =
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

    override suspend fun getSapMaterials(): List<VegaMaterial> =
        daoNic.getProductsLocal()

    override suspend fun getGrades(
        materialCode: String
    ) = daoMtntNic.getQualityGrades(materialCode, "NIPOSITI")

    override suspend fun getMaterialQualityGrades(
        materialCode: String
    ) = daoMtntNic.getMaterialQualityGrades(materialCode)

    override suspend fun updatePileSequence(postData: NicaraguaUpdatePileSequence): LiveData<Resource<GenericReqAndResp<NicaraguaUpdatePileSequence>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<NicaraguaUpdatePileSequence>>() {

            override suspend fun createCall(): GenericReqAndResp<NicaraguaUpdatePileSequence> =
                api.updatePileSequence(postData)
        }.build().asLiveData()
    }

}
