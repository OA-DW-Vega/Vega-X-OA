package com.olam.warehouse.vegax.offloadingnigeria.data.domain.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.model.VegaEcuaOffloadingWithLineItems
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.offloadingnigeria.data.domain.usecase.model.VegaNigeriaOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingnigeria.data.repo.VegaNigeriaOffloadingRepository
import java.util.*

/**
 * Created by Keerthi Santhanam on 23/6/2020.
 */
class VegaNigeriaOffloadingUseCase(private val repository: VegaNigeriaOffloadingRepository) {
    suspend fun getTrucks() = repository.getTrucks()
    suspend fun getProducts() = repository.getProducts()
    suspend fun postOffloadingDetail(vegaOffloadingPost: VegaNigeriaOffloadingPostRequest) =
        repository.postOffloadingDetail(vegaOffloadingPost)

    suspend fun getPalletDetailsWs(batchNumber: String, material: String) =
        repository.getPalletDetailsWs(batchNumber, material)

    suspend fun getDMSUploadedImages(wbId: String, werks: String) =
        repository.getDMSUploadedImages(wbId, werks)

    suspend fun saveMtnrReceivingLots(
        vegaCoffeeReceivingData: VegaCoffeeReceiving,
        lot: VegaCoffeeReceiveLots
    ) =
        repository.saveMtnrReceivingLots(vegaCoffeeReceivingData, lot)

    suspend fun qcinvoke(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaOffloadingTrucks>>>> {
        return Transformations.map(repository.getqcWeighBridgeList(selectedPlantId)) {
            it // Place here your specific logic actions (if any)
        }
    }

    suspend fun updateStatus(mtnNumber: String) = repository.updateStatus(mtnNumber)
    suspend fun getBagItemsNew(batchNumber: String?, mtnNumber: String) =
        repository.getBagItemsNew(batchNumber, mtnNumber)

    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun fetchWarehouseWithMtns() = repository.fetchWarehouseWithMtns()
    suspend fun getOBDDetails(deliveryNumber: String) = repository.getOBDDetails(deliveryNumber)
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getLocations() = repository.getLocations()
    suspend fun getWeighBridgeIdDetail(wbid: String) = repository.getWeighBridgeIdDetail(wbid)
    suspend fun getPOList() = repository.getPOList()
    suspend fun getPOListLocal() = repository.getPOListLocal()

    suspend fun saveBagDetails(material: VegaEcuadorOffloadingBagMaterial) =
        repository.saveBagDetails(material)

    suspend fun saveBagDetailsNew(bagMaterial: VegaCoffeeOffloadingBagMaterial) =
        repository.saveBagDetailsNew(bagMaterial)

    suspend fun deleteBagDetails(id: Int, tmpWbId: String) =
        repository.deleteBagDetails(id, tmpWbId)

    suspend fun deleteBagDetailsNew(id: Int) = repository.deleteBagDetailsNew(id)
    suspend fun clearBagDetails() = repository.clearBagDetails()
    suspend fun getBagItems(
        materialCode: String?,
        supplierCode: String,
        type: String,
        poId: String,
        tmpWbId: String
    ) = repository.getBagItems(materialCode, supplierCode, type, poId, tmpWbId)

    suspend fun postEcuadorOffloadingDetail(receivingData: VegaReceivingPost) =
        repository.postEcuadorOffloadingDetail(receivingData)

    suspend fun saveOffloading(receivingData: VegaReceiving) = repository.saveOffloading(receivingData)
    suspend fun saveReceivingLineItems(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>) =
        repository.saveReceivingLineItems(bagList)

    suspend fun getOffloadingWithLineItem(): LiveData<List<VegaEcuaOffloadingWithLineItems>> =
        repository.getOffloadingWithLineItem()

    suspend fun getOffloadingWithLineItemCount(): LiveData<List<VegaEcuaOffloadingWithLineItems>> =
        repository.getOffloadingWithLineItemCount()

    suspend fun updateDeletedItem(tmpWbId: String) = repository.updateDeletedItem(tmpWbId)
    suspend fun updateWBToQualityAndGrnTable(tmpWbid: String, wbid: String) =
        repository.updateWBToQualityAndGrnTable(tmpWbid, wbid)
}
