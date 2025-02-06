package com.olam.warehouse.vegax.thirdpartysalescoffee.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeDispatchDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeThirdPartyModelWithLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.api.VegaCocoaThirdPartyApi
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeTPDeliveryPost
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeWeighScalePallet
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.TP_TO_OLAM

interface VegaCocoaThirdPartyRepository {
    suspend fun getQualityParams(
        charge: String,
        material: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>

    suspend fun getStocksByMaterial(
        materialList: ArrayList<String>
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>

    suspend fun insertLot(lot: VegaCocoaDispatchLots)
    suspend fun insertLotList(lot: List<VegaCocoaDispatchLots>)
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun validateLot(batchNumber: String): VegaCocoaDispatchLots
    suspend fun removeLot(batchNumber: String)
    suspend fun getThirdPartyMaterials(): List<VegaMaterial>
    suspend fun getThirdPartyInfo(vendorTypeWithMaterial: String): VegaCoffeeThirdPartyModelWithLots
    suspend fun insertThirdPartyModel(model: VegaCoffeeThirdPartyRequestModel)
    suspend fun postDeliveryDetail(
        vegaDeliveryPost: VegaCoffeeTPDeliveryPost,
        type: String
    ): LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>>

    suspend fun postDeliveryDetailForWeighScale(
        vegaDeliveryPost: VegaCoffeeTPDeliveryPost,
        type: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaDeliveryPostResponse>>>>

    suspend fun updateAllSync(model: VegaCoffeeThirdPartyModelWithLots)

    suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial)

    suspend fun deleteBagDetails(id: Int)

    suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeWeighScalePallet>>>>

    suspend fun getBagItems(batchNumber: String, material: String): LiveData<List<VegaCocoaSweepingBagMaterial>>
    suspend fun getAllBagItems(): LiveData<List<VegaCocoaSweepingBagMaterial>>
}

class VegaCocoaThirdPartyRepositoryImpl(
    private val api: VegaCocoaThirdPartyApi,
    private val dao: VegaCoffeeDispatchDao,
    private val masterDao: MasterDao
) : VegaCocoaThirdPartyRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getQualityParams(
        charge: String,
        material: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                api.getQuality(currentKey, charge, material, whId)

        }.build().asLiveData()
    }

    override suspend fun getStocksByMaterial(
        materialList: ArrayList<String>
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                api.getStockList(currentKey, materialList)

        }.build().asLiveData()
    }

    override suspend fun getSuppliers() = dao.getSuppliers()

    override suspend fun insertLot(lot: VegaCocoaDispatchLots) {
        lot.isAdded = true
        dao.saveLots(mutableListOf(lot))
    }

    override suspend fun insertLotList(lot: List<VegaCocoaDispatchLots>) {
        lot.forEach {
            it.isAdded = true
        }
        dao.saveLots(lot)
    }

    override suspend fun validateLot(whId: String) = dao.validateLotAlreadyAdded(whId)
    override suspend fun removeLot(whId: String) = dao.removeLots(whId)

    override suspend fun getThirdPartyMaterials(): List<VegaMaterial> =
        dao.getThirdPartyMaterials()

    override suspend fun getThirdPartyInfo(
        vendorTypeWithMaterial: String
    ): VegaCoffeeThirdPartyModelWithLots = dao.getThirdPartyInfo(vendorTypeWithMaterial)

    override suspend fun insertThirdPartyModel(model: VegaCoffeeThirdPartyRequestModel) {
        dao.saveThirdPartyInfo(model)
    }

    override suspend fun postDeliveryDetail(
        vegaDeliveryPost: VegaCoffeeTPDeliveryPost,
        type: String
    ): LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaDeliveryPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaDeliveryPostResponse> =
                api.postDeliveryDetail(vegaDeliveryPost)
        }.build().asLiveData()
    }


    override suspend fun postDeliveryDetailForWeighScale(
        vegaDeliveryPost: VegaCoffeeTPDeliveryPost,
        type: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaDeliveryPostResponse>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaDeliveryPostResponse>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaDeliveryPostResponse>> =
                if (type.equals(TP_TO_OLAM)) api.postTPtoOlamDeliveryDetail(vegaDeliveryPost) else api.postSameTPDeliveryDetail(
                    vegaDeliveryPost
                )
        }.build().asLiveData()
    }

    override suspend fun updateAllSync(model: VegaCoffeeThirdPartyModelWithLots) {
        dao.deleteThirdPartyOwnershipStatus(model.model.vendorWithTransferType)
        model.lots?.forEach { dao.deleteLot(it.vendorWithTransferType) }
    }

    override suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = dao.saveBagDetails(bagMaterial)

    override suspend fun deleteBagDetails(id: Int) = dao.deleteBagDetails(id)

    override suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeWeighScalePallet>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeWeighScalePallet>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeWeighScalePallet>> =
                api.getPalletDetails(batchNumber, currentKey, material)
        }.build().asLiveData()
    }

    override suspend fun getBagItems(
        batchNumber: String,
        material: String
    ): LiveData<List<VegaCocoaSweepingBagMaterial>> {
        return if (batchNumber.isNotEmpty()) dao.getBagItems(batchNumber, material) else dao.getBagItems()
    }

    override suspend fun getAllBagItems(): LiveData<List<VegaCocoaSweepingBagMaterial>> {
        return dao.getBagItems()
    }
}
