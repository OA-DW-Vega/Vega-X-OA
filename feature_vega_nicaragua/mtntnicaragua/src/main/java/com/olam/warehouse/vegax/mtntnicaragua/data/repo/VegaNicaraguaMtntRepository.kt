package com.olam.warehouse.vegax.mtntnicaragua.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.OfflineInventory
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaMtntDao
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.master.veganicaragua.model.VegaMtntWithLotsWithBags
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.mtntnicaragua.data.api.VegaNicaraguaMtntApi
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.*
import java.util.*

/**
 * Created by Baskaran Kannan on 11/11/2020.
 */
interface VegaNicaraguaMtntRepository {
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getAllProduct(): LiveData<List<VegaMaterial>>
    suspend fun getPurchaseOrder(receivingWerks: String): LiveData<Resource<GenericReqAndResp<List<VegaNicPurchaseOrderModel>>>>
    suspend fun insertTruckInfo(mtnt: VegaNicaraguaMtnt)
    suspend fun getMtntWithLots(tmpId: String): LiveData<VegaMtntWithLotsWithBags>
    suspend fun validateLot(batchNumber: String): VegaNicDispatchLots
    suspend fun getLotDetails(
        charge: String,
        material: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNicDispatchLots>>>>

    suspend fun saveLotDetails(lot: VegaNicDispatchLots)
    suspend fun removeLotFromList(batchNumber: String, tempId: String)
    suspend fun saveBagDetails(material: VegaNicaraguaWeighmentBagMaterial)
    suspend fun getBagItems(batchNumber: String, tempId: String): LiveData<List<VegaNicaraguaWeighmentBagMaterial>>
    suspend fun deleteBagDetails(id: Int)
    suspend fun getStocks(material: ArrayList<String>): LiveData<Resource<GenericReqAndResp<OfflineInventory>>>
    suspend fun getStocksSap(material: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>
    suspend fun saveLotList(lotList: List<VegaNicDispatchLots>)
    suspend fun getStockListOffline(material: ArrayList<String>): LiveData<List<VegaNicaraguaGRNInventoryDetails>>
    suspend fun getGrades(materialCode: String): LiveData<List<VegaQualitative>>
    suspend fun getCertification(materialCode: String): LiveData<List<VegaQualitative>>
    suspend fun getMaterialQualityGrades(materialCode: String): LiveData<List<VegaNicaraguaMaterialQualitGrades>>
    suspend fun postNicMtntWSDeliveryDetails(vegaNicMtntDeliveryPost: VegaNicMtntDeliveryPost):
            LiveData<Resource<GenericReqAndResp<List<VegaNicMtntDeliveryDetail>>>>
    suspend fun getListOfMtntWithLots(): LiveData<List<VegaMtntWithLotsWithBags>>
    suspend fun deleteAllItem(tmpWbId: String)
    suspend fun getProducts(): List<VegaMaterial>
    suspend fun getPurchaseOrderOffline(): List<VegaCocoaPurchaseOrders>
    suspend fun getGrnPrintDetails(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>
    suspend fun getmtnrPrintDetails(grnno: String, batchNo: String, MTNR: String): LiveData<Resource<GenericReqAndResp<String>>>
    suspend fun getmtnrreprintlist(): LiveData<Resource<GenericReqAndResp<List<VegaMtnrReprintList>>>>
    suspend fun getOfflineLotDetails(
        lotId: String,
        materialCode: String
    ): List<VegaNicaraguaGRNInventoryDetails>

    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun getWeighBridgeDetailOnline(material: String): LiveData<Resource<GenericReqAndResp<List<VegaNicTicketListModel>>>>

    //    suspend fun getLotQuality(material: String): LiveData<Resource<GenericReqAndResp<List<VegaDispatchLotQuality>>>>
    suspend fun updateLotSequence(postData: VegaNicaraguaUpdateLotSequencePost): LiveData<Resource<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>>
    suspend fun getQualityParams(
        materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>

}

class VegaNicaraguaMtntRepositoryImpl(private val api: VegaNicaraguaMtntApi, private val dao: VegaNicaraguaMtntDao) :
    VegaNicaraguaMtntRepository {
    override suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>> = dao.getMaterials()
    override suspend fun getAllProduct(): LiveData<List<VegaMaterial>> = dao.getAllProducts()
    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getPurchaseOrder(receivingWerks: String): LiveData<Resource<GenericReqAndResp<List<VegaNicPurchaseOrderModel>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNicPurchaseOrderModel>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNicPurchaseOrderModel>> =
                api.getPurchaseOrder(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun insertTruckInfo(mtnt: VegaNicaraguaMtnt) = dao.insertTruckInfo(mtnt)
    override suspend fun getMtntWithLots(tmpId: String) = dao.getMtntWithLots(tmpId)
    override suspend fun validateLot(batchNumber: String): VegaNicDispatchLots =
        dao.validateLotAlreadyAdded(batchNumber)

    override suspend fun getLotDetails(
        charge: String,
        material: List<String>,
        whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNicDispatchLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNicDispatchLots>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaNicDispatchLots>> =
                api.getLotDetails(getCurrentKey(), charge, material, whId)

        }.build().asLiveData()
    }

    override suspend fun saveLotDetails(lot: VegaNicDispatchLots) = dao.saveLotDetails(lot)
    override suspend fun removeLotFromList(batchNumber: String, tempId: String) =
        dao.removeLotFromList(batchNumber, tempId)

    override suspend fun saveBagDetails(material: VegaNicaraguaWeighmentBagMaterial) = dao.saveBagDetails(material)
    override suspend fun getBagItems(batchNumber: String, tempId: String) = dao.getBagItems(batchNumber, tempId)
    override suspend fun deleteBagDetails(id: Int) = dao.deleteBagDetails(id)
    override suspend fun getStocks(material: ArrayList<String>): LiveData<Resource<GenericReqAndResp<OfflineInventory>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<OfflineInventory>>() {
            override suspend fun createCall(): GenericReqAndResp<OfflineInventory> =
                api.getStockList(getCurrentKey(), material[0])
        }.build().asLiveData()
    }

    override suspend fun getStocksSap(material: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                api.getStockListSap(getCurrentKey(), material)
        }.build().asLiveData()
    }

    override suspend fun saveLotList(lotList: List<VegaNicDispatchLots>) = dao.saveLotList(lotList)
    override suspend fun getStockListOffline(material: ArrayList<String>) = dao.getStockListOffline(material[0])
    override suspend fun getGrades(materialCode: String) = dao.getQualityGrades(materialCode, "NIPOSITI")
    override suspend fun getCertification(materialCode: String) = dao.getCertification(materialCode, "NIFG0014")
    override suspend fun getMaterialQualityGrades(materialCode: String) = dao.getMaterialQualityGrades(materialCode)
    override suspend fun postNicMtntWSDeliveryDetails(vegaNicMtntDeliveryPost: VegaNicMtntDeliveryPost): LiveData<Resource<GenericReqAndResp<List<VegaNicMtntDeliveryDetail>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNicMtntDeliveryDetail>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNicMtntDeliveryDetail>> =
                api.postMtntWeighScaleDeliveryDetails(vegaNicMtntDeliveryPost)

        }.build().asLiveData()
    }

    override suspend fun getListOfMtntWithLots() = dao.getListOfMtntWithLots()
    override suspend fun deleteAllItem(tmpWbId: String) {
        dao.deleteMtntItems(tmpWbId)
        dao.deleteMtntLots(tmpWbId)
        dao.deleteMtntBagItems(tmpWbId)
    }

    override suspend fun getProducts(): List<VegaMaterial> = dao.getProducts()
    override suspend fun getPurchaseOrderOffline(): List<VegaCocoaPurchaseOrders> = dao.getPurchaseOrderOffline()
    override suspend fun getGrnPrintDetails(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaReceiving>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaReceiving>> =
                api.getReprintDetailsForMtnt(getCurrentKey())

        }.build().asLiveData()
    }
    override suspend fun getmtnrPrintDetails(grnno: String, batchNo: String, receipt: String): LiveData<Resource<GenericReqAndResp<String>>>{
        return object : NetworkOnlyBoundResource<GenericReqAndResp<String>>() {
            override suspend fun createCall(): GenericReqAndResp<String> =
                api.getMTNRPrintRDetails(grnno.toInt())

        }.build().asLiveData()
    }
    override suspend fun getmtnrreprintlist(): LiveData<Resource<GenericReqAndResp<List<VegaMtnrReprintList>>>>{
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaMtnrReprintList>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaMtnrReprintList>> =
                api.getMTNRreprintList(Constants.MTNR,getCurrentKey().split("_")[1])

        }.build().asLiveData()
    }

    override suspend fun getOfflineLotDetails(lotId: String, materialCode: String) =
        dao.getOfflineLotDetails(lotId, materialCode)

    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> =
        dao.getConfigItems(role)

    override suspend fun getWeighBridgeDetailOnline(material: String): LiveData<Resource<GenericReqAndResp<List<VegaNicTicketListModel>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNicTicketListModel>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNicTicketListModel>> =
                api.fetchWeighBridgeDetail(currentKey, material)
        }.build().asLiveData()
    }

    override suspend fun updateLotSequence(postData: VegaNicaraguaUpdateLotSequencePost): LiveData<Resource<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost> =
                api.updateLotSequence(postData)
        }.build().asLiveData()
    }
    override suspend fun getQualityParams(materialId: String)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return  dao.getQualityParameter(materialId)
    }

}
