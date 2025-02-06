package com.olam.warehouse.vegax.mtntcocoa.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCocoaMtntWithLots
import com.olam.warehouse.master.vega.model.VegaCocoaNoWeighmentWithLots
import com.olam.warehouse.master.vegacocoa.dao.VegaCocoaDispatchDao
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaDeliveryPost
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrder
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.mtntcocoa.data.api.VegaCocoaMtntApi
import com.olam.warehouse.vegax.mtntcocoa.data.domain.model.*
import com.olam.warehouse.vegax.mtntcocoa.utils.MTNT_WEIGHSCALE

interface VegaCocoaMtntRepository {
    suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>>
    suspend fun getPurchaseOrder(receivingWerks: String): LiveData<Resource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>>

    suspend fun getQualityParams(
        charge: String,
        material: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>

    suspend fun getQualityNoWeighmentParams(
            charge: String,
            material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaNoWeighmentLot>>>>

    suspend fun getSingleProduct(code: String): LiveData<VegaMaterial>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCocoaDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>>

    suspend fun saveDispatchAndLots(
            dispatchData: VegaCocoaDispatchWB,
            dispatchLotsList: MutableList<VegaCocoaDispatchLots>
    )

    suspend fun insertLot(weighBridgeId: String, lot: VegaCocoaDispatchLots)
    suspend fun insertLotList(weighBridgeId: String, lot: List<VegaCocoaDispatchLots>)
    suspend fun insertLot(weighBridgeId: String, lot: VegaCocoaNoWeighmentLot)
    suspend fun insertLot(weighBridgeId: String, lot: List<VegaCocoaNoWeighmentLot>)
    suspend fun getWbInfo(wbId: String): LiveData<VegaCocoaDispatchWB>
    suspend fun updateLot(batchNumber: String, weight: String)
    suspend fun removeLot(batchNumber: String, material: String)
    suspend fun removeLotWs(batchNumber: String)
    suspend fun removeNoWeighmentLot(batchNumber: String, material: String)
    suspend fun removeNoWeighment(wbId: String)
    suspend fun updateRemarks(remark: String, isStart: Boolean, batchNumber: String)
    suspend fun updateDeliveryItem(deliveryItem: String, deliveryStatus: Boolean, whId: String)
    suspend fun updateStartLoadTime(startLoadTime: String, whId: String)
    suspend fun updateEndLoadTime(endLoadTime: String, turnAroundTime: String, whId: String)
    suspend fun insertTruckInfo(dispatchData: VegaCocoaNoWeighmentModel)
    suspend fun insertTruckInfoWeighscale(dispatchData: VegaCocoaDispatchWB)
    suspend fun getLotsDetails(whId: String): LiveData<List<VegaCocoaDispatchLots>>
    suspend fun validateLot(batchNumber: String, material: String): VegaCocoaDispatchLots
    suspend fun vendorInfo(batchNumber: String): VegaVendor
    suspend fun deleteTruckAndLots(whId: String)
    suspend fun getStocksByMaterial(
            materialList: ArrayList<String>
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>

    suspend fun getNoWeighmentStocksByMaterial(
            materialList: ArrayList<String>
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaNoWeighmentLot>>>>

    suspend fun getSuppliers(): LiveData<List<VegaVendor>>

    suspend fun updateSuccessStatus(
            wbId: VegaCocoaDispatchWB,
            syncStatus: Boolean,
            status: Int,
            msg: String,
            lots: List<VegaCocoaDispatchLots>
    )

    suspend fun updateVirtualSuccessStatus(
            wbId: VegaCocoaNoWeighmentModel,
            syncStatus: Boolean,
            status: Int,
            msg: String,
            lots: List<VegaCocoaNoWeighmentLot>
    )

    suspend fun getAllProduct(): LiveData<List<VegaMaterial>>

    suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial)
    suspend fun saveBagDetails(bagMaterial: ArrayList<VegaCocoaSweepingBagMaterial>)

    suspend fun deleteBagDetails(id: Int)
    suspend fun deleteBagDetails(batchNumber: String, mtnNumber: String)
    suspend fun getPalletDetails(
            batchNumber: String,
            material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaNoWeighmentPallet>>>>

    suspend fun getPalletDetailsWs(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaWeighScalePallet>>>>

    suspend fun getBagItems(isweighscale:Boolean,batchNumber: String, material: String): LiveData<List<VegaCocoaSweepingBagMaterial>>
    suspend fun getAllBagItems(): LiveData<List<VegaCocoaSweepingBagMaterial>>
    suspend fun getWeighScaleInfo(whId: String, stoNo: String, purchase: String): VegaCocoaNoWeighmentWithLots
    suspend fun getWeighScaleInfo(whId: String): VegaCocoaNoWeighmentWithLots
    suspend fun getThirdPartyMaterials(): List<VegaMaterial>
    suspend fun virtualDeliveryDetail(vegaDeliveryPost: VegaCocoaVirtualPostRequest): LiveData<Resource<GenericReqAndResp<List<VegaCocoaVirtualDeliveryDetail>>>>
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>>
    suspend fun getOfflinePurchaseOrderList(): LiveData<List<VegaCocoaPurchaseOrders>>
    suspend fun getOfflineStockList(material: String): LiveData<List<VegaEcuadorDispatchStocks>>
    suspend fun getMtntWithLotsAndMaterial(wbId: String): LiveData<VegaCocoaMtntWithLots>
    suspend fun getOfflineStockInfo(batchNumber: String, material: String): List<VegaEcuadorDispatchStocks>
    suspend fun getTransactionData(): LiveData<List<VegaCocoaNoWeighmentWithLots>>
    suspend fun getPendingList(): LiveData<List<VegaCocoaNoWeighmentModel>>
    suspend fun getPendingListWithLots(): LiveData<List<VegaCocoaNoWeighmentWithLots>>
    suspend fun deleteMaterialData(whId: String)
    suspend fun deleteMaterialCodeData(mcode: String)
    suspend fun getWeighScaleInfo(whId: String, stoNo: String): VegaCocoaMtntWithLots
    suspend fun insertMaterialInfo(list: List<VegaCoffeePurchaseOrderMaterialModel>)
    suspend fun deleteMaterialInfo()
    suspend fun getMtntWithLots(wbId: String): VegaCocoaMtntWithLots
    suspend fun validateLotWs(batchNumber: String): VegaCocoaDispatchLots
    suspend fun updateAllSync(model: VegaCocoaMtntWithLots)
    suspend fun postWeighScaleDeliveryDetail(vegaDeliveryPost: VegaCocoaWeighscaleDeliveryPost): LiveData<Resource<GenericReqAndResp<List<VegaCocoaMtntDeliveryDetail>>>>

}

class VegaCocoaMtntRepositoryImpl(
        private val api: VegaCocoaMtntApi,
        private val dao: VegaCocoaDispatchDao,
        private val masterDao: MasterDao
) : VegaCocoaMtntRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchWB>> =
                    api.fetchTruckList(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getPurchaseOrder(receivingWerks: String): LiveData<Resource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaPurchaseOrder>> =
                    if (receivingWerks.isNotEmpty()) api.getPurchaseOrder(
                            currentKey,
                            receivingWerks
                    ) else api.getPurchaseOrderForNoWeighment(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getQualityParams(
        charge: String,
        material: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                    api.getQuality(currentKey, charge, material, whId)

        }.build().asLiveData()
    }

    override suspend fun getQualityNoWeighmentParams(
            charge: String,
            material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaNoWeighmentLot>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaNoWeighmentLot>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaNoWeighmentLot>> =
                    api.getNoWeighmentQuality(currentKey, charge, material, whId)

        }.build().asLiveData()
    }

    override suspend fun getSingleProduct(code: String): LiveData<VegaMaterial> = dao.getSingleProducts(code)
    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)

    override suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCocoaDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaDeliveryPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaDeliveryPostResponse> =
                    api.postDeliveryDetail(vegaDeliveryPost)
        }.build().asLiveData()
    }

    override suspend fun saveDispatchAndLots(
            dispatchData: VegaCocoaDispatchWB,
            dispatchLotsList: MutableList<VegaCocoaDispatchLots>
    ) {
        dao.insertDispatchTruckDetail(dispatchData)
        dispatchLotsList.forEach {
            it.weighBridgeId = dispatchData.weighBridgeId
            it.isAdded = true
        }
        dao.saveLots(dispatchLotsList)
    }

    override suspend fun insertLot(weighBridgeId: String, lot: VegaCocoaDispatchLots) {
        lot.weighBridgeId = weighBridgeId
        lot.isAdded = true
        dao.saveLots(mutableListOf(lot))
    }

    override suspend fun insertLot(weighBridgeId: String, lot: VegaCocoaNoWeighmentLot) {
        lot.weighBridgeId = weighBridgeId
        dao.saveNoWeighmentLots(mutableListOf(lot))
    }

    override suspend fun insertLot(weighBridgeId: String, lot: List<VegaCocoaNoWeighmentLot>) {
        dao.saveNoWeighmentLots(lot)
    }

    override suspend fun getWbInfo(wbId: String) = dao.getTruckData(wbId)

    override suspend fun updateLot(batchNumber: String, weight: String) = dao.updateLotWeight(batchNumber, weight)

    override suspend fun removeLot(batchNumber: String, material: String) = dao.
    removeLots(batchNumber, material)

    override suspend fun removeNoWeighmentLot(batchNumber: String, material: String) = dao.
    removeNoWeighmentLots(batchNumber, material)

    override suspend fun removeNoWeighment(wbId: String) {
        dao.removeNoWeighment(wbId)
        dao.removeLotsByWeighBridgeId(wbId)
    }
    override suspend fun updateRemarks(remark: String, isStart: Boolean, batchNumber: String) =
            dao.updateRemark(remark, isStart, batchNumber)

    override suspend fun updateDeliveryItem(deliveryItem: String, deliveryStatus: Boolean, whId: String) =
            dao.updateDeliveryItem(deliveryItem, deliveryStatus, whId)

    override suspend fun updateStartLoadTime(startLoadTime: String, whId: String) =
            dao.updateStartTime(true, startLoadTime, whId)

    override suspend fun updateEndLoadTime(endLoadTime: String, turnAroundTime: String, whId: String) =
            dao.updateEndTime(
                    true,
                    endLoadTime, turnAroundTime, whId
            )

    override suspend fun insertTruckInfo(dispatchData: VegaCocoaNoWeighmentModel) =
            dao.insertNoWeighmentDetail(dispatchData)

    override suspend fun getLotsDetails(whId: String) = dao.geLots(whId)

    override suspend fun validateLot(whId: String, purchaseOrder: String) =
            dao.validateLotAlreadyAdded(whId, purchaseOrder)

    override suspend fun vendorInfo(whId: String) = dao.vendorInfo(whId)
    override suspend fun deleteTruckAndLots(whId: String) {
        dao.deleteVegaDispatchTrucks(whId)
        dao.removeLotsByWeighBridgeId(whId)
    }

    override suspend fun getStocksByMaterial(
            materialList: ArrayList<String>
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                    api.getStockList(currentKey, materialList)

        }.build().asLiveData()
    }

    override suspend fun getNoWeighmentStocksByMaterial(
            materialList: ArrayList<String>
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaNoWeighmentLot>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaNoWeighmentLot>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaNoWeighmentLot>> =
                    api.getNoWeighmentStockList(currentKey, materialList)

        }.build().asLiveData()
    }

    override suspend fun getSuppliers() = dao.getSuppliers()

    override suspend fun updateSuccessStatus(
            wbId: VegaCocoaDispatchWB,
            syncStatus: Boolean,
            status: Int,
            msg: String,
            lots: List<VegaCocoaDispatchLots>
    ) {
        dao.updateDispatchStatus(wbId.weighBridgeId, syncStatus, status, msg)
        lots.forEach {
            it.isProgress = true
            dao.updateDispatchLotStatus(wbId.weighBridgeId, true)
        }
    }


    override suspend fun updateVirtualSuccessStatus(
            wbId: VegaCocoaNoWeighmentModel,
            syncStatus: Boolean,
            status: Int,
            msg: String,
            lots: List<VegaCocoaNoWeighmentLot>
    ) {
        dao.updateVirtualDispatchStatus(wbId.weighBridgeId, syncStatus, status, msg)
        lots.forEach {
            it.isProgress = true
            dao.updateVirtualDispatchLotStatus(wbId.weighBridgeId, true)
        }
    }

    override suspend fun getAllProduct(): LiveData<List<VegaMaterial>> = dao.getAllProducts()

    override suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = dao.saveBagDetails(bagMaterial)

    override suspend fun saveBagDetails(bagMaterial: ArrayList<VegaCocoaSweepingBagMaterial>) = dao.saveBagDetails(bagMaterial)

    override suspend fun deleteBagDetails(id: Int) = dao.deleteBagDetails(id)
    override suspend fun deleteBagDetails(batchNumber: String, mtnNumber: String) =
            dao.deleteBagDetails(batchNumber, mtnNumber)

    override suspend fun getPalletDetails(
            batchNumber: String,
            material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaNoWeighmentPallet>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaNoWeighmentPallet>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaNoWeighmentPallet>> =
                    api.getPalletDetails(batchNumber, currentKey, material)
        }.build().asLiveData()
    }

    override suspend fun getBagItems(isweighscale:Boolean,
            batchNumber: String,
            material: String
    ): LiveData<List<VegaCocoaSweepingBagMaterial>> {

        return if (batchNumber.isNotEmpty())if(isweighscale) dao.getBagItemsWeighscale(batchNumber, material)  else dao.getBagItems(batchNumber, material) else dao.getBagItems()
    }

    override suspend fun getAllBagItems(): LiveData<List<VegaCocoaSweepingBagMaterial>> {
        return dao.getBagItems()
    }

    override suspend fun getWeighScaleInfo(
            whId: String,
            stoNo: String,
            purchase: String
    ): VegaCocoaNoWeighmentWithLots =
            dao.getNoWeighmentWithLotSingle(whId, stoNo, purchase, AppUtils.isOnline())

    override suspend fun getWeighScaleInfo(whId: String): VegaCocoaNoWeighmentWithLots =
            dao.getNoWeighmentWithLot(whId)

    override suspend fun getThirdPartyMaterials(): List<VegaMaterial> =
            dao.getThirdPartyMaterials()

    override suspend fun virtualDeliveryDetail(vegaDeliveryPost: VegaCocoaVirtualPostRequest): LiveData<Resource<GenericReqAndResp<List<VegaCocoaVirtualDeliveryDetail>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaVirtualDeliveryDetail>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaVirtualDeliveryDetail>> =
                    api.postVirtualDeliveryDetails(vegaDeliveryPost)
        }.build().asLiveData()
    }

    override suspend fun getMaterials() = dao.getMaterials()

    override suspend fun getOfflinePurchaseOrderList(): LiveData<List<VegaCocoaPurchaseOrders>> = dao.offlinePOList()
    override suspend fun getOfflineStockList(material: String): LiveData<List<VegaEcuadorDispatchStocks>> =
            dao.offlineStockList(material)

    override suspend fun getOfflineStockInfo(batchNumber: String, material: String): List<VegaEcuadorDispatchStocks> =
            dao.offlineStockInfo(batchNumber, material)

    override suspend fun getTransactionData(): LiveData<List<VegaCocoaNoWeighmentWithLots>> = dao.getTransactionData()
    override suspend fun getPendingList(): LiveData<List<VegaCocoaNoWeighmentModel>> = dao.getPendingList()
    override suspend fun getPendingListWithLots(): LiveData<List<VegaCocoaNoWeighmentWithLots>> =
            dao.getPendingListWithLot()

    override suspend fun insertTruckInfoWeighscale(dispatchData: VegaCocoaDispatchWB) {
        dao.insertDispatchTruckDetail(dispatchData)
    }
    override suspend fun deleteMaterialData(whId: String) {
        dao.deleteMaterialData(whId)
    }
    override suspend fun getWeighScaleInfo(whId: String, stoNo: String): VegaCocoaMtntWithLots =
        dao.getMtntWeighsclaeWithLotSingle(whId, stoNo, MTNT_WEIGHSCALE)

    override suspend fun insertMaterialInfo(list: List<VegaCoffeePurchaseOrderMaterialModel>) =
        dao.insertMaterialDetail(list)
    override suspend fun deleteMaterialInfo() =
        dao.deleteMaterialInfo()

    override suspend fun deleteMaterialCodeData(mcode: String) {
        dao.deleteMaterialCodeData(mcode)
    }
    override suspend fun getMtntWithLots(wbId: String): VegaCocoaMtntWithLots = dao.getMtntWithLotSingle(wbId)

    override suspend fun validateLotWs(whId: String) = dao.validateLotAlreadyAdded(whId)

    override suspend fun removeLotWs(batchNumber: String) = dao.removeLotsWs(batchNumber)

    override suspend fun insertLotList(weighBridgeId: String, lot: List<VegaCocoaDispatchLots>) {
        lot.forEach {
            it.weighBridgeId = weighBridgeId
            it.isAdded = true
        }
        dao.saveLots(lot)
    }
    override suspend fun getPalletDetailsWs(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaWeighScalePallet>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaWeighScalePallet>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaWeighScalePallet>> =
                api.getPalletDetailsWeighscale(batchNumber, currentKey, material)
        }.build().asLiveData()
    }

    override suspend fun getMtntWithLotsAndMaterial(wbId: String) = dao.getMtntWithLotAndMaterial(wbId)

    override suspend fun updateAllSync(model: VegaCocoaMtntWithLots) {
        dao.updateDispatchMtntStatus(model.dispatch.weighBridgeId, true, 4, "")
        model.lineItems.forEach { dao.updateSyncStatusLot(it.batchNumber, 1, 4) }
    }
    override suspend fun postWeighScaleDeliveryDetail(vegaDeliveryPost: VegaCocoaWeighscaleDeliveryPost): LiveData<Resource<GenericReqAndResp<List<VegaCocoaMtntDeliveryDetail>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaMtntDeliveryDetail>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaMtntDeliveryDetail>> =
                api.postWeighScaleDeliveryDetails(vegaDeliveryPost)
        }.build().asLiveData()
    }

}
