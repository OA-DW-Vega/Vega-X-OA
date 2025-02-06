package com.olam.warehouse.vegax.thirdpartysalescoffee.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.model.*
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaFeatureMaster
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeDispatchDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeThirdPartyModelWithLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaGrnDao
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaPositionGradeMappings
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.api.VegaCoffeeThirdPartyApi
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeTPDeliveryPost
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeWeighScalePallet
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeWeighbridgePostRequest
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaNicaraguaCoffeeTPDeliveryPost
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.TP_TO_OLAM
import java.util.*
import kotlin.collections.ArrayList

interface VegaCoffeeThirdPartyRepository {
    suspend fun getQualityParams(
        charge: String,
        material: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>

    suspend fun getStocksByMaterial(
        materialList: ArrayList<String>
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>
    suspend fun getTTFeatureMaster(module: String): LiveData<List<VegaFeatureMaster>>
    suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>>
    suspend fun getVendors(): LiveData<List<VegaVendor>>
    suspend fun getMaterials(): LiveData<List<VegaMaterial>>
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
    suspend fun getPostWeighbridge(postPileRequest: VegaCoffeeWeighbridgePostRequest): LiveData<Resource<GenericReqAndResp<VegaCoffeeWeighbridgePostRequest>>>
    suspend fun postSaleTruckInData(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>
    suspend fun getSuppliers(purchaseOrgType: String?): LiveData<List<VegaVendor>>
    suspend fun getExchangeRate(): LiveData<Resource<GenericReqAndResp<ExchangeRate>>>
    suspend fun getGrnPriceDetails(): LiveData<Resource<GenericReqAndResp<List<GrnPriceDetails>>>>
    suspend fun getGradeMapping(grade: String): LiveData<VegaNicaraguaPositionGradeMappings>
    suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>

    suspend fun postNicDeliveryDetailForWeighScale(
        vegaDeliveryPost: VegaNicaraguaCoffeeTPDeliveryPost,
        type: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaDeliveryPostResponse>>>>

    suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>
}

class VegaCoffeeThirdPartyRepositoryImpl(
    private val api: VegaCoffeeThirdPartyApi,
    private val dao: VegaCoffeeDispatchDao,
    private val masterDao: MasterDao,
    private val nicDao: VegaNicaraguaGrnDao
) : VegaCoffeeThirdPartyRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    val currentDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")

    override suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchWB>> =
                api.fetchTruckList(currentKey, true)
        }.build().asLiveData()
    }

    override suspend fun getVendors() = dao.getSuppliers()
    override suspend fun getMaterials() = dao.getAllProducts()

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

    override suspend fun getTTFeatureMaster(module: String): LiveData<List<VegaFeatureMaster>> {
        return dao.getTTFeatureMaster(module)
    }

    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getSuppliers(purchaseOrgType: String?) =
        if (purchaseOrgType?.isNotEmpty() == true) nicDao.getSuppliers(purchaseOrgType.toString()) else nicDao.getMaterials()

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
        dao.getAllMaterials()

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

    override suspend fun getPostWeighbridge(postPileRequest: VegaCoffeeWeighbridgePostRequest): LiveData<Resource<GenericReqAndResp<VegaCoffeeWeighbridgePostRequest>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeeWeighbridgePostRequest>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaCoffeeWeighbridgePostRequest> =
                api.getPostPile(postPileRequest)

        }.build().asLiveData()
    }

    override suspend fun postSaleTruckInData(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingResponse> =
                api.postWeighbridgeData(receivingData)
        }.build().asLiveData()
    }

    override suspend fun getExchangeRate(): LiveData<Resource<GenericReqAndResp<ExchangeRate>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<ExchangeRate>>() {
            override suspend fun createCall() = api.getExchangeRate(currentDate.toString(), currentKey)
        }.build().asLiveData()
    }

    override suspend fun getGrnPriceDetails(): LiveData<Resource<GenericReqAndResp<List<GrnPriceDetails>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<GrnPriceDetails>>>() {
            override suspend fun createCall() = api.getGrnPriceDetails(currentKey)
        }.build().asLiveData()
    }
    override suspend fun getGradeMapping(grade: String) = nicDao.getGradeMapping(grade)
    override suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityPreParameter>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityPreParameter>> =
                api.fetchPreQualityDetails(currentKey, batchNo, materialId)
        }.build().asLiveData()
    }

    override suspend fun postNicDeliveryDetailForWeighScale(
        vegaDeliveryPost: VegaNicaraguaCoffeeTPDeliveryPost,
        type: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaDeliveryPostResponse>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaDeliveryPostResponse>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaDeliveryPostResponse>> =
              api.postNicTPtoOlamDeliveryDetail(vegaDeliveryPost)
        }.build().asLiveData()
    }

    override suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>> =
                api.getPurchaseOrders(getCurrentKey())
        }.build().asLiveData()
    }
}
