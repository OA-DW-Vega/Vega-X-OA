package com.olam.warehouse.vegax.mtntghanacocoa.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoffeeMtntWithLots
import com.olam.warehouse.master.vega.model.VegaGhanaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeDispatchDao
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaghana.dao.VegaGhanaProcessingDao
import com.olam.warehouse.master.vegaghana.entity.VehicleDetails
import com.olam.warehouse.presentation.data.api.TruckManageApi
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.domain.model.TruckManagementSeasonResponse
import com.olam.warehouse.presentation.data.domain.model.TruckManagementVehicleResponse
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.mtntghanacocoa.data.api.VegaGhanaCocoaMtntApi
import com.olam.warehouse.vegax.mtntghanacocoa.data.domain.model.*
import com.olam.warehouse.vegax.mtntghanacocoa.utils.MTNT_WEIGHSCALE

interface VegaGhanaCocoaMtntRepository {
    suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>>
    suspend fun getTruckDetails(seasonId:String): LiveData<Resource<TruckManagementVehicleResponse>>
    suspend fun getStocks(material: String): LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>>
    suspend fun getSeasonDetails(): LiveData<Resource<TruckManagementSeasonResponse>>
    suspend fun getSeasonDetailsOffline(): LiveData<List<VehicleDetails>>
    suspend fun getPurchaseOrder(receivingWerks: String): LiveData<Resource<GenericReqAndResp<List<VegaGhanaMtntPurchaseOrder>>>>
    suspend fun getStorageLocationDetail(): LiveData<List<VegaStorageLocationDetail>>
    suspend fun getQualityParams(
        charge: String,
        material: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>>>>

    suspend fun getSingleProduct(code: String): LiveData<VegaMaterial>
    suspend fun getAllProduct(): LiveData<List<VegaMaterial>>
    suspend fun getuomDetail(): LiveData<List<VegaUomDetails>>
    suspend fun getAllPlantRoute(): LiveData<List<VegaPlanRoute>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaGhanaMtntDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>>
    suspend fun postWeighScaleDeliveryDetail(vegaDeliveryPost: VegaGhanaMtntMergedDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaGhanaMergedDeliveryPostResponse>>>
    suspend fun postWeighScaleDeliveryDetail(
        vegaDeliveryPost: VegaGhanaMtntDeliveryPost,
        wayBillNumber: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaGhanaMtntDeliveryDetail>>>>

    suspend fun saveLots(
        dispatchLotsList: VegaGhanaCocoaDispatchLots
    )

    suspend fun saveLotData(
        dispatchLotsList: VegaGhanaCocoaDispatchLots
    )

    suspend fun getDelivery(
        delivery: String,
        deliveryItem: String
    ): LiveData<Resource<GenericReqAndResp<VegaDispatchDelivery>>>

    suspend fun saveDispatchAndLots(
        dispatchData: VegaCocoaDispatchWB,
        dispatchLotsList: MutableList<VegaGhanaCocoaDispatchLots>
    )

    suspend fun insertLot(weighBridgeId: String, lot: VegaGhanaCocoaDispatchLots)
    suspend fun insertLotList(weighBridgeId: String, lot: List<VegaGhanaCocoaDispatchLots>)
    suspend fun getWbInfo(wbId: String): LiveData<VegaCocoaDispatchWB>
    suspend fun getWbInfo(): LiveData<List<VegaCocoaDispatchWB>>
    suspend fun getGhanaWbInfo(): LiveData<List<VegaCocoaDispatchWB>>
    suspend fun updateLot(batchNumber: String, weight: String)
    suspend fun removeLot(batchNumber: String)
    suspend fun removeGhanaLotFromList(batchNumber: String,wbId: String)
    suspend fun removeLotList()
    suspend fun removeGhanaLotList(wbId: String)
    suspend fun updateRemarks(remark: String, isStart: Boolean, batchNumber: String)
    suspend fun updateDeliveryItem(deliveryItem: String, deliveryStatus: Boolean, whId: String)
    suspend fun updateStartLoadTime(startLoadTime: String, whId: String)
    suspend fun updateEndLoadTime(endLoadTime: String, turnAroundTime: String, whId: String)
    suspend fun insertTruckInfo(dispatchData: VegaCocoaDispatchWB)
    suspend fun insertMaterialInfo(list: List<VegaGhanaPurchaseOrderMaterialModel>)
    suspend fun getLotsDetails(whId: String): LiveData<List<VegaGhanaCocoaDispatchLots>>
    suspend fun validateLot(batchNumber: String): VegaGhanaCocoaDispatchLots
    suspend fun deleteTruckAndLots(whId: String)
    suspend fun deleteMaterialData(whId: String)
    suspend fun deleteGhanaLotList()
    suspend fun deleteMaterialData()
    suspend fun getStocksByMaterial(
        materialList: ArrayList<String>
    ): LiveData<Resource<GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>>>>

    suspend fun getDispatchLotList(
        materialList: String
    ): LiveData<List<VegaGhanaCocoaDispatchLots>>

    suspend fun getMtntWithLots(wbId: String): VegaGhanaCocoaMtntWithLots
    suspend fun getMtntWithLotsAndMaterial(wbId: String): LiveData<VegaGhanaCocoaMtntWithLots>
    suspend fun getWeighBridgeStockCount(): LiveData<List<VegaGhanaCocoaMtntWithLots>>
    suspend fun getOfflineGradeWithBagsRmin(
        fgrnIdWithMatrial: String,
        batchNumber: String
    ): List<VegaCocoaFgrnGradesMatrialWeights>

    suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial)

    suspend fun deleteBagDetails(id: Int)
    suspend fun deleteBagDetails()
    suspend fun deleteBag(batchNumber: String)

    suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaGhanaMtntWeighScalePallet>>>>

    suspend fun getBagItems(batchNumber: String, material: String): LiveData<List<VegaCocoaSweepingBagMaterial>>
    suspend fun getBagItems(
        batchNumber: String,
        material: String,
        wbid: String
    ): LiveData<List<VegaCocoaSweepingBagMaterial>>

    suspend fun getAllBagItems(): LiveData<List<VegaCocoaSweepingBagMaterial>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getWeighScaleInfo(whId: String, stoNo: String): VegaGhanaCocoaMtntWithLots
    suspend fun getWeighScaleInfo(): LiveData<List<VegaCoffeeMtntWithLots>>
    suspend fun updateAllSync(model: VegaGhanaCocoaMtntWithLots)
    suspend fun updateSync(model: VegaGhanaCocoaMtntWithLots)
    suspend fun getLocations(): LiveData<List<VegaSupplyStorageLocation>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getMaterialStorageLocation(): LiveData<List<VegaStorageLocationDetail>>
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>>
    suspend fun getThirdPartyMaterials(): List<VegaCoffeeThirdPartyMaterialDetail>
    suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>>
    suspend fun postWs(vegaSesameDeliveryPost: VegaGhanaMtntMergedDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaGhanaMergedDeliveryPostResponse>>>
    suspend fun getPurchaseOrderOffline(): LiveData<List<VegaCocoaPurchaseOrders>>
    suspend fun getStockListOffline(): LiveData<List<VegaEcuadorDispatchStocks>>
    suspend fun updateStockDetails(weight: String, batchNo: String)
    suspend fun updateMtntPurchaseOrderDetails(weight: String, po: String)
    suspend fun updateSyncedMtntDeletedItem(status: Int)
    suspend fun validateNumbers(plantId: String,wrNumber:String,
                                wbType:String): LiveData<Resource<GenericReqAndResp<ValidateNumber>>>

}

class VegaGhanaMtntRepositoryImpl(
    private val apiCocoa: VegaGhanaCocoaMtntApi,
    private val apiTruckManage: TruckManageApi,
    private val dao: VegaCoffeeDispatchDao,
    private val dao1: VegaGhanaProcessingDao,
    private val masterDao: MasterDao
) : VegaGhanaCocoaMtntRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchWB>> =
                apiCocoa.fetchTruckList(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getSeasonDetails(): LiveData<Resource<TruckManagementSeasonResponse>> {
        return object : NetworkOnlyBoundResource<TruckManagementSeasonResponse>() {
            override suspend fun createCall(): TruckManagementSeasonResponse =
                apiTruckManage.getSeasonDetails()
        }.build().asLiveData()
    }

    override suspend fun getMaterials() = dao.getMaterials()

    override suspend fun getSeasonDetailsOffline() = dao.getSeasonDetailsOffline()

    override suspend fun getMaterialStorageLocation() = dao.getMaterialStlocDetails()


    override suspend fun getStorageLocationDetail(): LiveData<List<VegaStorageLocationDetail>> =
        dao.getStorageLocationDetail()

    override suspend fun getuomDetail() = dao.getuomDetail()

    override suspend fun getTruckDetails(seasonId: String): LiveData<Resource<TruckManagementVehicleResponse>> {
        return object : NetworkOnlyBoundResource<TruckManagementVehicleResponse>() {
            override suspend fun createCall(): TruckManagementVehicleResponse =
                apiTruckManage.getTruckDetails(seasonId)
        }.build().asLiveData()
    }

    override suspend fun postWeighScaleDeliveryDetail(
        vegaDeliveryPost: VegaGhanaMtntDeliveryPost,
        wayBillNumber: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaGhanaMtntDeliveryDetail>>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaGhanaMtntDeliveryDetail>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaGhanaMtntDeliveryDetail>> =
                apiCocoa.postWeighScaleDeliveryDetails(vegaDeliveryPost, wayBillNumber)
        }.build().asLiveData()
    }

    override suspend fun getPurchaseOrder(receivingWerks: String): LiveData<Resource<GenericReqAndResp<List<VegaGhanaMtntPurchaseOrder>>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaGhanaMtntPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaGhanaMtntPurchaseOrder>> =
                apiCocoa.getPurchaseOrder(currentKey)
        }.build().asLiveData()
    }

    override suspend fun saveLots(
        dispatchLotsList: VegaGhanaCocoaDispatchLots
    ) {
        val list = ArrayList<VegaGhanaCocoaDispatchLots>()
        list.add(dispatchLotsList)
        dao1.saveGhanaLots(list)
    }

    override suspend fun saveLotData(
        dispatchLotsList: VegaGhanaCocoaDispatchLots
    ) {

        dao1.saveGhanaLots(dispatchLotsList)
    }

    override suspend fun getQualityParams(
        charge: String,
        material: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>> =
                apiCocoa.getQuality(currentKey, charge, material, whId)

        }.build().asLiveData()
    }

    override suspend fun getStocksByMaterial(
        materialList: ArrayList<String>
    ): LiveData<Resource<GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>> =
                apiCocoa.getStockList(currentKey, materialList)

        }.build().asLiveData()
    }

    override suspend fun getDispatchLotList(wbId: String) = dao1.getDispatchLots(wbId)

    override suspend fun getMtntWithLots(wbId: String): VegaGhanaCocoaMtntWithLots =
        dao1.getGhanaMtntWithLotSingle(wbId)

    override suspend fun getMtntWithLotsAndMaterial(wbId: String) = dao1.getGhanaMtntWithLotAndMaterial(wbId)
    override suspend fun getWeighBridgeStockCount(): LiveData<List<VegaGhanaCocoaMtntWithLots>> = dao1.getWeighBridgeStockCount()
    override suspend fun getLocations() = dao.getLocations()
    override suspend fun getCustomLocations() = dao.getCustomLocations()

    override suspend fun getSingleProduct(code: String): LiveData<VegaMaterial> = dao.getSingleProducts(code)
    override suspend fun getAllProduct(): LiveData<List<VegaMaterial>> = dao.getAllProducts()
    override suspend fun getAllPlantRoute(): LiveData<List<VegaPlanRoute>> = dao.getAllPlantRoute()
    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)

    override suspend fun postDeliveryDetail(vegaDeliveryPost: VegaGhanaMtntDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaDeliveryPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaDeliveryPostResponse> =
                apiCocoa.postDeliveryDetail(vegaDeliveryPost)
        }.build().asLiveData()
    }



    override suspend fun postWeighScaleDeliveryDetail(vegaDeliveryPost: VegaGhanaMtntMergedDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaGhanaMergedDeliveryPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGhanaMergedDeliveryPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaGhanaMergedDeliveryPostResponse> =
                apiCocoa.postWeighScaleDeliveryDetailsAuto(vegaDeliveryPost)
        }.build().asLiveData()
    }

    override suspend fun postWs(vegaSesameDeliveryPost: VegaGhanaMtntMergedDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaGhanaMergedDeliveryPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGhanaMergedDeliveryPostResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaGhanaMergedDeliveryPostResponse> =
                apiCocoa.postWeighScaleDeliveryDetailsAuto(vegaSesameDeliveryPost)
        }.build().asLiveData()
    }

    override suspend fun getPurchaseOrderOffline(): LiveData<List<VegaCocoaPurchaseOrders>> =
        dao.getPurchaseOrdersOffline()

    override suspend fun getStockListOffline(): LiveData<List<VegaEcuadorDispatchStocks>> = dao.getStockListOffline()
    override suspend fun saveDispatchAndLots(
        dispatchData: VegaCocoaDispatchWB,
        dispatchLotsList: MutableList<VegaGhanaCocoaDispatchLots>
    ) {
        dao.insertDispatchTruckDetail(dispatchData)
        dispatchLotsList.forEach {
            it.weighBridgeId = dispatchData.weighBridgeId
            it.isAdded = true
        }
        dao1.saveGhanaLots(dispatchLotsList)
    }

    override suspend fun insertLot(weighBridgeId: String, lot: VegaGhanaCocoaDispatchLots) {
        lot.weighBridgeId = weighBridgeId
        lot.isAdded = true
        dao1.saveGhanaLots(mutableListOf(lot))
    }

    override suspend fun insertLotList(weighBridgeId: String, lot: List<VegaGhanaCocoaDispatchLots>) {
        lot.forEach {
            it.weighBridgeId = weighBridgeId
            it.isAdded = true
        }
        dao1.saveGhanaLots(lot)
    }

    override suspend fun getWbInfo(wbId: String) = dao.getTruckData(wbId)
    override suspend fun getWbInfo() = dao.getTruckData()
    override suspend fun getGhanaWbInfo() = dao.getGhanaTruckData()

    override suspend fun updateLot(batchNumber: String, weight: String) = dao.updateLotWeight(batchNumber, weight)

    override suspend fun removeLot(batchNumber: String) = dao.removeLots(batchNumber)
    override suspend fun removeGhanaLotFromList(batchNumber: String,wbId: String) = dao.removeGhanaLotFromList(batchNumber,wbId)
    override suspend fun removeLotList() = dao.removeLotsList()
    override suspend fun removeGhanaLotList(wbId: String) = dao.removeGhanaLotList(wbId)
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

    override suspend fun insertTruckInfo(dispatchData: VegaCocoaDispatchWB) =
        dao.insertDispatchTruckDetail(dispatchData)

    override suspend fun insertMaterialInfo(list: List<VegaGhanaPurchaseOrderMaterialModel>) =
        dao1.insertGhanaMaterialDetail(list)

    override suspend fun getLotsDetails(whId: String) = dao1.getGhanaLots(whId)

    override suspend fun validateLot(whId: String) = dao1.validateGhanaLotAlreadyAdded(whId)
    override suspend fun deleteTruckAndLots(whId: String) {
        dao.deleteVegaDispatchTrucks(whId)
        dao.removeLotsByWeighBridgeId(whId)
    }

    override suspend fun deleteMaterialData(whId: String) {
//        dao.deleteMaterialData(whId)
        dao.deleteGhanaMaterialData(whId)
    }

    override suspend fun deleteGhanaLotList() {
//        dao.deleteMaterialData(whId)
        dao.deleteGhanaLotList()
    }

    override suspend fun deleteMaterialData() {
        dao.deleteMaterialData()
    }

    override suspend fun getOfflineGradeWithBagsRmin(fgrnIdWithMatrial: String, batchNo: String) =
        dao.getOfflineGradeWithBagsRmin(fgrnIdWithMatrial, batchNo)

    override suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) =
        dao.saveBagDetails(bagMaterial)

    override suspend fun deleteBagDetails(id: Int) = dao.deleteBagDetails(id)
    override suspend fun deleteBagDetails() = dao.deleteBagDetails()
    override suspend fun deleteBag(batchNumber: String) = dao.deleteBag(batchNumber)

    override suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaGhanaMtntWeighScalePallet>>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaGhanaMtntWeighScalePallet>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaGhanaMtntWeighScalePallet>> =
                apiCocoa.getPalletDetails(batchNumber, currentKey, material)
        }.build().asLiveData()
    }

    override suspend fun getBagItems(
        batchNumber: String,
        material: String
    ): LiveData<List<VegaCocoaSweepingBagMaterial>> {
        return if (batchNumber.isNotEmpty()) dao.getBagItems(batchNumber, material) else dao.getBagItems()
    }

    override suspend fun getBagItems(
        batchNumber: String,
        material: String,
        wbid: String
    ): LiveData<List<VegaCocoaSweepingBagMaterial>> {
        return if (batchNumber.isNotEmpty()) dao.getBagItems(batchNumber, material, wbid) else dao.getBagItems()
    }

    override suspend fun getAllBagItems(): LiveData<List<VegaCocoaSweepingBagMaterial>> {
        return dao.getBagItems()
    }

    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getWeighScaleInfo(whId: String, stoNo: String): VegaGhanaCocoaMtntWithLots =
        dao1.getGhanaMtntWithLotSingle(whId, stoNo, MTNT_WEIGHSCALE)

    override suspend fun getWeighScaleInfo(): LiveData<List<VegaCoffeeMtntWithLots>> =
        dao.getMtntWithLots()

    override suspend fun updateAllSync(model: VegaGhanaCocoaMtntWithLots) {
        dao.updateDispatchMtntStatus(model.dispatch.weighBridgeId, true, 4, "")
        model.lineItems.forEach {
            dao.updateSyncStatusLot(it.batchNumber, 1, 4)
        }
    }

    override suspend fun updateSync(model: VegaGhanaCocoaMtntWithLots) {
//        dao.updateDispatchMtntStatus(model.dispatch.weighBridgeId, true, 4, "")
        dao.deleteVegaDispatchTrucks(model.dispatch.weighBridgeId)
        model.lineItems.forEach {
//            dao.updateSyncStatusLot(it.batchNumber, 1, 4)
            dao.removeLotsFromLocal(it.weighBridgeId)
        }
    }

    override suspend fun getDelivery(
        delivery: String,
        deliveryItem: String
    ): LiveData<Resource<GenericReqAndResp<VegaDispatchDelivery>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaDispatchDelivery>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaDispatchDelivery> =
                apiCocoa.getDelivery(currentKey, delivery, deliveryItem)
        }.build().asLiveData()
    }

    override suspend fun getStocks(material: String): LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaRminLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaRminLots>> =
                    apiCocoa.getStocks(currentKey, material)
        }.build().asLiveData()
    }

    override suspend fun getThirdPartyMaterials(): List<VegaCoffeeThirdPartyMaterialDetail> =
        dao.getThirdPartyMaterialDetails()

    override suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        dao.getProcessTypeList(role)

    override suspend fun updateStockDetails(weight: String, batchNo: String) = dao.updateStockDetails(weight, batchNo)
    override suspend fun updateMtntPurchaseOrderDetails(weight: String, po: String) =
        dao.updateMtntPurchaseOrderDetails(weight, po)

    override suspend fun updateSyncedMtntDeletedItem(status: Int) {
        dao.deleteSyncedOfflineMtntDetails(status)
    }
    override suspend fun validateNumbers(
        plantId: String,
        wrNumber: String,
        wbType: String
    ): LiveData<Resource<GenericReqAndResp<ValidateNumber>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<ValidateNumber>>() {
            override suspend fun createCall(): GenericReqAndResp<ValidateNumber> =
                apiCocoa.validateReceiptNumbers(currentKey, plantId, wrNumber, wbType)
        }.build().asLiveData()
    }
}
