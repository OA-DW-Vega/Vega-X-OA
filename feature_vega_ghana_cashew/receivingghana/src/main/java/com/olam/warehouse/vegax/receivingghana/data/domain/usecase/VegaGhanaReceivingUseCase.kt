package com.olam.warehouse.vegax.receivingghana.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.model.VegaEcuaOffloadingWithLineItems
import com.olam.warehouse.vegax.receivingghana.data.repo.VegaGhanaReceivingRepository

/**
 * Created by Baskaran Kannan on 9/24/2020.
 */
class VegaGhanaReceivingUseCase(private val repository: VegaGhanaReceivingRepository) {
    suspend fun getProducts() = repository.getProducts()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getPOList() = repository.getPOList()
    suspend fun getPOListLocal() = repository.getPOListLocal()

    suspend fun saveBagDetails(material: VegaEcuadorOffloadingBagMaterial) = repository.saveBagDetails(material)
    suspend fun deleteBagDetails(id: Int, tmpWbId: String) = repository.deleteBagDetails(id, tmpWbId)
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
