package com.olam.warehouse.vegax.offloadingnigeria.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.model.VegaMtntResponse
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorOffloadingDao
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuaOffloadingWithLineItems
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkBoundResource
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.offloadingnigeria.data.api.VegaNigeriaOffloadingApi
import com.olam.warehouse.vegax.offloadingnigeria.data.domain.usecase.model.VegaCocoaWeighScalePallet
import com.olam.warehouse.vegax.offloadingnigeria.data.domain.usecase.model.VegaDMSImageResponse
import com.olam.warehouse.vegax.offloadingnigeria.data.domain.usecase.model.VegaNigeriaOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingnigeria.utils.ORGTYPE
import java.util.*

/**
 * Created by Keerthi Santhanam on 23/6/2020.
 */
interface VegaNigeriaOffloadingRepository {
    suspend fun getTrucks(): LiveData<Resource<List<VegaOffloadingTrucks>>>

    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun postOffloadingDetail(vegaOffloadingPost: VegaNigeriaOffloadingPostRequest): LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>>
    suspend fun getPalletDetailsWs(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaWeighScalePallet>>>>

    suspend fun getDMSUploadedImages(
        wbId: String,
        werks: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaDMSImageResponse>>>>

    suspend fun saveMtnrReceivingLots(
        vegaCoffeeReceivingData: VegaCoffeeReceiving,
        lot: VegaCoffeeReceiveLots
    )

    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getqcWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaOffloadingTrucks>>>>
    suspend fun updateStatus(mtnNumber: String)
    suspend fun getBagItemsNew(
        batchNumber: String?,
        mtnNumber: String
    ): LiveData<List<VegaCoffeeOffloadingBagMaterial>>

    suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>>
    suspend fun getOBDDetails(deliveryNumber: String): LiveData<VegaCoffeeReceivingMtnrWithLots>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getLocations(): LiveData<List<VegaSupplyStorageLocation>>
    suspend fun getWeighBridgeIdDetail(wbid: String): LiveData<Resource<GenericReqAndResp<VegaReceiving>>>
    suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>
    suspend fun getPOListLocal(): LiveData<List<VegaEcuadorPurchaseOrder>>
    suspend fun saveBagDetails(material: VegaEcuadorOffloadingBagMaterial)
    suspend fun saveBagDetailsNew(bagMaterial: VegaCoffeeOffloadingBagMaterial)
    suspend fun deleteBagDetails(id: Int, tmpWbId: String)
    suspend fun deleteBagDetailsNew(id: Int)
    suspend fun clearBagDetails()
    suspend fun getBagItems(
        materialCode: String?,
        supplierCode: String,
        type: String,
        poId: String,
        tmpWbId: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>>
    suspend fun postEcuadorOffloadingDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>
    suspend fun saveOffloading(receivingData: VegaReceiving)
    suspend fun saveReceivingLineItems(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>)
    suspend fun getOffloadingWithLineItem(): LiveData<List<VegaEcuaOffloadingWithLineItems>>
    suspend fun getOffloadingWithLineItemCount(): LiveData<List<VegaEcuaOffloadingWithLineItems>>
    suspend fun updateDeletedItem(tmpWbId: String)
    suspend fun updateWBToQualityAndGrnTable(tmpWbid: String, wbid: String)
}

class VegaNigeriaOffloadingRepositoryImpl(
    private val api: VegaNigeriaOffloadingApi,
    private val dao: VegaEcuadorOffloadingDao,
    private val masterDao: MasterDao
) : VegaNigeriaOffloadingRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getTrucks(): LiveData<Resource<List<VegaOffloadingTrucks>>> {

        return object : NetworkBoundResource<List<VegaOffloadingTrucks>, GenericReqAndResp<List<VegaOffloadingTrucks>>>() {

            override fun processResponse(response: GenericReqAndResp<List<VegaOffloadingTrucks>>): List<VegaOffloadingTrucks> =
                response.data

            override suspend fun saveCallResults(items: List<VegaOffloadingTrucks>) =
                dao.save(items)

            override fun shouldFetch(data: List<VegaOffloadingTrucks>?): Boolean = true

            override suspend fun loadFromDb(): List<VegaOffloadingTrucks> =
                dao.getOffloadingTruckListDetails()

            override suspend fun createCall(): GenericReqAndResp<List<VegaOffloadingTrucks>> =
                api.fetchTruckList(currentKey)

        }.build().asLiveData()
    }
    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getCustomLocations() = dao.getCustomLocations()
    override suspend fun getOBDDetails(deliveryNumber: String) = dao.getOBDDetails(deliveryNumber)


    override suspend fun postOffloadingDetail(vegaOffloadingPost: VegaNigeriaOffloadingPostRequest): LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaMtntResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaMtntResponse> =
                api.postOffloadingDetail(vegaOffloadingPost)
        }.build().asLiveData()
    }

    override suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>> =
                api.getPurchaseOrders(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCoffeeReceivingMtnWrapper> =
                api.fetchWarehouseWithMtns(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getBagItemsNew(
        batchNumber: String?,
        mtnNumber: String
    ): LiveData<List<VegaCoffeeOffloadingBagMaterial>> {
        return if (batchNumber.isNullOrEmpty()) dao.getBagItemsNew() else dao.getBagItemsNew(
            batchNumber,
            mtnNumber
        )
    }

    override suspend fun updateStatus(mtnNumber: String) {
        dao.deleteReceivingItem(mtnNumber)
        dao.deleteReceivingLotItem(mtnNumber)
    }

    override suspend fun getqcWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaOffloadingTrucks>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaOffloadingTrucks>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaOffloadingTrucks>> =
                api.fetchQCWeighBridgeList(currentKey, "true", selectedPlantId)

        }.build().asLiveData()
    }

    override suspend fun getLocations() = dao.getLocations()

    override suspend fun getWeighBridgeIdDetail(wbid: String): LiveData<Resource<GenericReqAndResp<VegaReceiving>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceiving>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceiving> =
                api.getWeighBridgeIdDetail(currentKey, wbid)
        }.build().asLiveData()
    }


    override suspend fun getPalletDetailsWs(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaWeighScalePallet>>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaWeighScalePallet>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaWeighScalePallet>> =
                api.getPalletDetailsWeighscale(batchNumber, currentKey, material)
        }.build().asLiveData()
    }

    override suspend fun getDMSUploadedImages(
        wbId: String,
        werks: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaDMSImageResponse>>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaDMSImageResponse>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaDMSImageResponse>> =
                api.getDMSUploadedImages(currentKey, ORGTYPE, wbId, werks)
        }.build().asLiveData()
    }

    override suspend fun saveMtnrReceivingLots(
        vegaCoffeeReceivingData: VegaCoffeeReceiving,
        lot: VegaCoffeeReceiveLots
    ) {
        dao.saveMtnrReceiving(vegaCoffeeReceivingData)
        if (lot.batch.isNotEmpty())
            dao.saveMtnrReceivingLot(lot)
    }

    override suspend fun getPOListLocal(): LiveData<List<VegaEcuadorPurchaseOrder>> =
        dao.getPOListLocal()

    override suspend fun saveBagDetails(material: VegaEcuadorOffloadingBagMaterial) =
        dao.saveBagDetails(material)

    override suspend fun saveBagDetailsNew(bagMaterial: VegaCoffeeOffloadingBagMaterial) =
        dao.saveBagDetailsNew(bagMaterial)

    override suspend fun deleteBagDetails(id: Int, tmpWbId: String) =
        dao.deleteBagDetails(id, tmpWbId)

    override suspend fun deleteBagDetailsNew(id: Int) = dao.deleteBagDetailsNew(id)
    override suspend fun clearBagDetails() = dao.clearBagDetails()
    override suspend fun getBagItems(
        materialCode: String?,
        supplierCode: String,
        type: String,
        poId: String,
        tmpWbId: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>> {
        return if (poId.isEmpty()) dao.getBagItems(
            materialCode,
            supplierCode,
            type,
            tmpWbId
        ) else dao.getBagItems(
            materialCode,
            supplierCode,
            type,
            poId,
            tmpWbId
        )
    }

    override suspend fun postEcuadorOffloadingDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingResponse> =
                api.postEcuadorOffloadingDetail(receivingData)
        }.build().asLiveData()
    }

    override suspend fun saveOffloading(receivingData: VegaReceiving) = dao.saveOffloading(receivingData)
    override suspend fun saveReceivingLineItems(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>) =
        dao.saveReceivingLineItems(bagList)

    override suspend fun getOffloadingWithLineItem(): LiveData<List<VegaEcuaOffloadingWithLineItems>> =
        dao.getOffloadingWithLineItem()

    override suspend fun getOffloadingWithLineItemCount(): LiveData<List<VegaEcuaOffloadingWithLineItems>> =
        dao.getOffloadingWithLineItemCount()

    override suspend fun updateDeletedItem(tmpWbId: String) {
        dao.deleteOffloadingItem(tmpWbId)
        dao.deleteBagItem(tmpWbId)
        if (tmpWbId.contains("TMP")) {
            dao.deleteGrnData(tmpWbId)
            dao.updateDeletedItem(tmpWbId)
        }
    }

    override suspend fun updateWBToQualityAndGrnTable(tmpWbid: String, wbid: String) {
        dao.updateTempIdToWbid(tmpWbid, wbid)
    }
}
