package com.olam.warehouse.vegax.offloadingcameroon.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.model.VegaMtntResponse
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCameroonOffloadingWithBagItem
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeOffloadDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorOffloadingDao
import com.olam.warehouse.master.vegaecuador.entity.VegaCameroonOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuaOffloadingWithLineItems
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkBoundResource
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.offloadingcameroon.data.api.VegaCameroonOffloadingApi
import com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.model.*
import java.util.*

/**
 * Created by Keerthi Santhanam on 23/6/2020.
 */
interface VegaCameroonOffloadingRepository {
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getTrucks(qcFlag: String, selectedPlantId: String): LiveData<Resource<List<VegaOffloadingTrucks>>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun getPOList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>
    suspend fun getMtntWeightDetails(
        wbid: String,
        selectedPlantId: String
    ): LiveData<Resource<GenericReqAndResp<VegaCameroonMtntDetails>>>

    suspend fun getPOListLocal(): LiveData<List<VegaEcuadorPurchaseOrder>>
    suspend fun saveBagDetails(material: VegaEcuadorOffloadingBagMaterial)
    suspend fun saveSelectedBatchBagDetails(material: List<VegaEcuadorOffloadingBagMaterial>)
    suspend fun saveCameroonSaveBagDetails(material: List<VegaCameroonOffloadingBagMaterial>)
    suspend fun deleteBagDetails(id: Int, tmpWbId: String)
    suspend fun deleteSelectedBagDetails(id: Int, wbId: String)
    suspend fun deleteCameroonSavedBagDetails(batchId: String)
    suspend fun deleteBagDetails(batchNumber: String, wbId: String)
    suspend fun deleteBagDetails()
    suspend fun clearBagDetails()
    suspend fun getBagItems(
        materialCode: String?,
        supplierCode: String,
        type: String,
        poId: String,
        tmpWbId: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>>

    suspend fun getWBBagItems(
        tmpWbId: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>>
    suspend fun getSavedBagItems(
        materialCode: String?,
        supplierCode: String,
        type: String,
        poId: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>>

    suspend fun getSavedBagItems(plantId: String): LiveData<List<VegaEcuadorOffloadingBagMaterial>>
//    suspend fun getCameroonSavedBagItems(): LiveData<List<VegaCameroonOffloadingBagMaterial>>

    suspend fun postEcuadorOffloadingDetail(receivingData: VegaCameroonOffloadingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>
    suspend fun saveOffloading(receivingData: VegaReceiving)
    suspend fun saveReceivingLineItems(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>)
    suspend fun getOffloadingWithLineItem(): LiveData<List<VegaEcuaOffloadingWithLineItems>>
    suspend fun getOffloadingWithLineItemCount(): LiveData<List<VegaEcuaOffloadingWithLineItems>>
    suspend fun updateDeletedItem(tmpWbId: String)
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun updateWBToQualityAndGrnTable(tmpWbid: String, wbid: String)

    suspend fun postReceivingDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>
    suspend fun postReceivingMtnDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>
    suspend fun getLocations(): LiveData<List<VegaSupplyStorageLocation>>
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>>
    suspend fun saveReceiving(receivingData: VegaReceiving)
    suspend fun getReceiving(): LiveData<List<VegaReceiving>>
    suspend fun getWarehouses(): LiveData<List<VegaReceivingWarehouse>>
    suspend fun getWarehousesWithMtns(whID: String): LiveData<VegaReceivingWarehouseWithMtns>
    suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>>
    suspend fun saveWarehouseWithMtns(it: VegaCoffeeReceivingMtnWrapper)

    suspend fun saveReceivingLineItems(lineItems: List<VegaReceivingLineItem>)
    suspend fun getReceivingWithLineItem(): LiveData<List<VegaReceivingWithLineItems>>
    suspend fun updateDeletedItem(wbid: String, txnId: String?)
    suspend fun deleteReceiving(receivingData: VegaReceiving)
    suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String)
    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>
    suspend fun getTruckInWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>
    suspend fun getWeighBridgeIdDetail(wbid: String): LiveData<Resource<GenericReqAndResp<VegaReceiving>>>
    suspend fun getBagItems(batchNumber: String?, mtnNumber: String): LiveData<List<VegaCoffeeOffloadingBagMaterial>>
    suspend fun getBagItem(batchNumber: String): LiveData<List<VegaEcuadorOffloadingBagMaterial>>
    suspend fun saveBagDetails(bagMaterial: VegaCoffeeOffloadingBagMaterial)
    suspend fun deleteBagDetails(id: Int)
    suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCameroonWeighScalePallet>>>>

    suspend fun getOBDDetails(deliveryNumber: String): LiveData<VegaCoffeeReceivingMtnrWithLots>
    suspend fun saveMtnrReceivingLots(vegaCoffeeReceivingData: VegaCoffeeReceiving, lot: VegaCoffeeReceiveLots)
    suspend fun postOffloadingDetail(vegaOffloadingPost: VegaCameroonOffloadingPostRequest): LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>>
    suspend fun updateStatus(mtnNumber: String)
    suspend fun getSavedReceivingBagItems(wbId:String): LiveData<VegaCameroonOffloadingWithBagItem>
//    suspend fun getMultiPlantList(): LiveData<List<Plant>>
    suspend fun getReprintList(): LiveData<Resource<GenericReqAndResp<List<VegaReprintList>>>>

    suspend fun downloadRePrintItem(selectedItemId: String): LiveData<Resource<GenericReqAndResp<String>>>

    suspend fun getFeatureMaster(): LiveData<List<VegaFeatureMaster>>

}

class VegaCameroonOffloadingRepositoryImpl(
    private val api: VegaCameroonOffloadingApi,
    private val dao: VegaEcuadorOffloadingDao,
    private val daoe: VegaCoffeeOffloadDao,
    private val masterDao: MasterDao
) : VegaCameroonOffloadingRepository {
    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> =
        dao.getConfigItems(role)

    override suspend fun getTrucks(
        qcFlag: String,
        selectedPlantId: String
    ): LiveData<Resource<List<VegaOffloadingTrucks>>> {

        return object :
            NetworkBoundResource<List<VegaOffloadingTrucks>, GenericReqAndResp<List<VegaOffloadingTrucks>>>() {

            override fun processResponse(response: GenericReqAndResp<List<VegaOffloadingTrucks>>): List<VegaOffloadingTrucks> =
                response.data

            override suspend fun saveCallResults(items: List<VegaOffloadingTrucks>) = dao.save(items)

            override fun shouldFetch(data: List<VegaOffloadingTrucks>?): Boolean = true

            override suspend fun loadFromDb(): List<VegaOffloadingTrucks> = dao.getOffloadingTruckListDetails()

            override suspend fun createCall(): GenericReqAndResp<List<VegaOffloadingTrucks>> =
                api.fetchTruckList(currentKey, qcFlag, selectedPlantId)

        }.build().asLiveData()
    }

    override suspend fun getPOList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>> =
                api.getPurchaseOrders(getCurrentKey(), selectedPlantId)
        }.build().asLiveData()
    }

    override suspend fun getMtntWeightDetails(
        wbid: String,
        selectedPlantId: String
    ): LiveData<Resource<GenericReqAndResp<VegaCameroonMtntDetails>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCameroonMtntDetails>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCameroonMtntDetails> =
                api.getMtntWeightDetails(getCurrentKey(), wbid, selectedPlantId)
        }.build().asLiveData()
    }

    override suspend fun getPOListLocal(): LiveData<List<VegaEcuadorPurchaseOrder>> = dao.getPOListLocal()

    override suspend fun saveBagDetails(material: VegaEcuadorOffloadingBagMaterial) = dao.saveBagDetails(material)
    override suspend fun saveSelectedBatchBagDetails(material: List<VegaEcuadorOffloadingBagMaterial>) =
        dao.saveSelectedBatchBagDetails(material)
    override suspend fun deleteBagDetails(id: Int, tmpWbId: String) = dao.deleteBagDetails(id, tmpWbId)
    override suspend fun deleteSelectedBagDetails(id: Int, wbId: String) = dao.deleteSelectedBagDetails(id, wbId)
    override suspend fun deleteCameroonSavedBagDetails(tmpWbId: String) = dao.deleteCameroonSavedBagDetails(tmpWbId)
    override suspend fun deleteBagDetails(batchNumber: String, wbId: String) = dao.deleteBagDetails(batchNumber, wbId)
    override suspend fun deleteBagDetails() = dao.deleteBagDetails()
    override suspend fun clearBagDetails() = dao.clearBagDetails()
    override suspend fun getBagItems(
        materialCode: String?,
        supplierCode: String,
        type: String,
        poId: String,
        tmpWbId: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>> {
        return if (poId.isEmpty()) dao.getBagItems(materialCode, supplierCode, type, tmpWbId) else dao.getBagItems(
            materialCode,
            supplierCode,
            type,
            poId,
            tmpWbId
        )
    }
    override suspend fun getWBBagItems(
        tmpWbId: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>> {
        return  dao.getWBBagItems(tmpWbId)
    }
    override suspend fun getBagItem(
        batchNumber: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>> {
        return dao.getBagItem(batchNumber)
    }

    override suspend fun getSavedBagItems(
        materialCode: String?,
        supplierCode: String,
        type: String,
        poId: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>> {
        return dao.getSavedBagItems(
            materialCode,
            supplierCode,
            type,
            poId
        )
    }

    override suspend fun getSavedBagItems(plantId: String): LiveData<List<VegaEcuadorOffloadingBagMaterial>> {
        return dao.getSavedBagItems(plantId)
    }


//    override suspend fun getCameroonSavedBagItems(): LiveData<List<VegaCameroonOffloadingBagMaterial>> {
//        return dao.getSavedBagItems()
//    }

    override suspend fun postEcuadorOffloadingDetail(receivingData: VegaCameroonOffloadingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> {
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




    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun updateDeletedItem(wbid: String, txnId: String?) {
        dao.deleteItemReceiving(wbid)
        masterDao.updateWBDB(wbid)
        masterDao.deleteOfflineParams(wbid)
    }

    override suspend fun getReceivingWithLineItem() = dao.getReceivingWithLineItem()
    override suspend fun saveReceivingLineItems(lineItems: List<VegaReceivingLineItem>) {
        dao.deleteOfflineReceivingLineItem(lineItems[0].tmpWbId)
        dao.saveReceivingLineItems(lineItems)
    }

    override suspend fun saveWarehouseWithMtns(it: VegaCoffeeReceivingMtnWrapper) = dao.saveWarehouseAndMtns(it)
    override suspend fun getWarehouses() = dao.getWarehouses()
    override suspend fun getWarehousesWithMtns(whID: String) = dao.getWarehousesWithMtns(whID)
    override suspend fun getReceiving() = dao.getReceiving()
    override suspend fun saveReceiving(receivingData: VegaReceiving) = dao.saveOffloading(receivingData)
    override suspend fun deleteReceiving(receivingData: VegaReceiving) =
        dao.deleteItemReceiving(receivingData.tmpWbId)

    override suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String) {
        dao.updateReceivingFailMsg(msg, tmpWbId, Status.SYNC_ERROR)
        dao.updateReceivingLineItemFailMsg(msg, tmpWbId, Status.SYNC_ERROR)
    }


    override suspend fun getLocations() = dao.getLocations()
    override suspend fun getCustomLocations() = dao.getCustomLocations()
    override suspend fun getMaterials() = dao.getMaterials()

    override suspend fun postReceivingDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingResponse> =
                api.postReceivingDetail(receivingData)
        }.build().asLiveData()
    }

    override suspend fun postReceivingMtnDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingResponse> =
                api.postReceivingMtnDetail(receivingData)
        }.build().asLiveData()
    }

    override suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCoffeeReceivingMtnWrapper> =
                api.fetchWarehouseWithMtns(currentKey)
        }.build().asLiveData()
    }


    override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaReceiving>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaReceiving>> =
                api.fetchWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }

    /* override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<List<VegaReceiving>>> {

         return object : NetworkBoundResource<List<VegaReceiving>, GenericReqAndResp<List<VegaReceiving>>>() {

             override fun processResponse(response: GenericReqAndResp<List<VegaReceiving>>): List<VegaReceiving> =
                 response.data

             override suspend fun saveCallResults(items: List<VegaReceiving>) = dao.saveReceiving(items, DIRECTIONOUT)

             override fun shouldFetch(data: List<VegaReceiving>?): Boolean = true

             override suspend fun loadFromDb(): List<VegaReceiving> =
                 dao.getWeighBridgeDetailOnline(Status.SYNC_PENDING, "OUT")

             override suspend fun createCall(): GenericReqAndResp<List<VegaReceiving>> =
                 api.fetchWeighBridgeDetail(currentKey)

         }.build().asLiveData()
     }*/

    override suspend fun getTruckInWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaReceiving>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaReceiving>> =
                api.fetchTruckInWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getWeighBridgeIdDetail(wbid: String): LiveData<Resource<GenericReqAndResp<VegaReceiving>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceiving>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceiving> =
                api.getWeighBridgeIdDetail(currentKey, wbid)
        }.build().asLiveData()
    }

    override suspend fun getBagItems(batchNumber: String?, mtnNumber: String): LiveData<List<VegaCoffeeOffloadingBagMaterial>> {
        return if (batchNumber.isNullOrEmpty()) daoe.getBagItems() else daoe.getBagItems(batchNumber, mtnNumber)
    }

    override suspend fun saveBagDetails(bagMaterial: VegaCoffeeOffloadingBagMaterial) = daoe.saveBagDetails(bagMaterial)
    override suspend fun saveCameroonSaveBagDetails(material: List<VegaCameroonOffloadingBagMaterial>) =  dao.saveCameroonSaveBagDetails(material)


    override suspend fun deleteBagDetails(id: Int) = daoe.deleteBagDetails(id)
    override suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCameroonWeighScalePallet>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCameroonWeighScalePallet>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCameroonWeighScalePallet>> =
                api.getPalletDetails(batchNumber, currentKey, material)
        }.build().asLiveData()
    }

    override suspend fun getOBDDetails(deliveryNumber: String) = dao.getOBDDetails(deliveryNumber)
    override suspend fun saveMtnrReceivingLots(
        vegaCoffeeReceivingData: VegaCoffeeReceiving,
        lot: VegaCoffeeReceiveLots
    ) {
        dao.saveMtnrReceiving(vegaCoffeeReceivingData)
        if (lot.batch.isNotEmpty())
            dao.saveMtnrReceivingLot(lot)
    }

    override suspend fun postOffloadingDetail(vegaOffloadingPost: VegaCameroonOffloadingPostRequest): LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaMtntResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaMtntResponse> =
                api.postOffloadingDetail(vegaOffloadingPost)
        }.build().asLiveData()
    }

    override suspend fun updateStatus(mtnNumber: String) {
        dao.deleteReceivingItem(mtnNumber)
        dao.deleteReceivingLotItem(mtnNumber)
    }

    override suspend fun getSavedReceivingBagItems(wbId: String) = dao.getSavedReceivingBagItems(wbId)


//    override suspend fun getMultiPlantList() = dao.getMultiPlantList()

    override suspend fun getReprintList(): LiveData<Resource<GenericReqAndResp<List<VegaReprintList>>>>{
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaReprintList>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaReprintList>> =
                api.getReprintList(Constants.OFFLOADING,getCurrentKey().split("_")[1])

        }.build().asLiveData()
    }

    override suspend fun downloadRePrintItem(selectedItemId: String): LiveData<Resource<GenericReqAndResp<String>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<String>>() {
            override suspend fun createCall(): GenericReqAndResp<String> =
                api.downloadReprintItems(selectedItemId)

        }.build().asLiveData()
    }

    override suspend fun getFeatureMaster() = dao.getFeatureMaster()


}
