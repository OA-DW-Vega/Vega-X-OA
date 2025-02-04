package com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeThirdPartyModelWithLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeTPDeliveryPost
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeWeighbridgePostRequest
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.repo.VegaCoffeeThirdPartyRepository

class VegaCoffeeThirdPartyUseCase(private val repository: VegaCoffeeThirdPartyRepository) {
    suspend fun getQualityParams(charge: String, material: List<String>, whId: String) =
        repository.getQualityParams(charge, material, whId)

    suspend fun getVendors() = repository.getVendors()
    suspend fun getMaterials() = repository.getMaterials()

    suspend fun getTrucks() = repository.getTrucks()

    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getSuppliers(purchaseOrgType: String?) = repository.getSuppliers(purchaseOrgType)
    suspend fun insertLot(lot: VegaCocoaDispatchLots) = repository.insertLot(lot)
    suspend fun insertLotList(lot: List<VegaCocoaDispatchLots>) = repository.insertLotList(lot)
    suspend fun getStocks(material: ArrayList<String>) = repository.getStocksByMaterial(material)
    suspend fun validateLot(batchNumber: String): VegaCocoaDispatchLots = repository.validateLot(batchNumber)
    suspend fun removeLot(batchNumber: String) = repository.removeLot(batchNumber)
    suspend fun getThirdPartyMaterials() = repository.getThirdPartyMaterials()
    suspend fun getThirdPartyInfo(vendorTypeWithMaterial: String) =
        repository.getThirdPartyInfo(vendorTypeWithMaterial)

    suspend fun insertThirdPartyModel(model: VegaCoffeeThirdPartyRequestModel) = repository.insertThirdPartyModel(model)
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCoffeeTPDeliveryPost, type: String) =
        repository.postDeliveryDetail(vegaDeliveryPost, type)

    suspend fun postDeliveryDetailForWeighScale(vegaDeliveryPost: VegaCoffeeTPDeliveryPost, type: String) =
        repository.postDeliveryDetailForWeighScale(vegaDeliveryPost, type)

    suspend fun updateAllSyncStatus(model: VegaCoffeeThirdPartyModelWithLots) =
        repository.updateAllSync(model)

    suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = repository.saveBagDetails(bagMaterial)

    suspend fun deleteBagDetails(id: Int) = repository.deleteBagDetails(id)
    suspend fun getBagItems(batchNumber: String, material: String) = repository.getBagItems(batchNumber, material)
    suspend fun getAllBagItems() = repository.getAllBagItems()
    suspend fun getPalletDetails(batchNumber: String, material: String) =
        repository.getPalletDetails(batchNumber, material)

    suspend fun getPostWeighbridge(postPileRequest: VegaCoffeeWeighbridgePostRequest): LiveData<Resource<GenericReqAndResp<VegaCoffeeWeighbridgePostRequest>>> =
        repository.getPostWeighbridge(postPileRequest)

    suspend fun postSalesDetail(receivingData: VegaReceivingPost) = repository.postSaleTruckInData(receivingData)
}
