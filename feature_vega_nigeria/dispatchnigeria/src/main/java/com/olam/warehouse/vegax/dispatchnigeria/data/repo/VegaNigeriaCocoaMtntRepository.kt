package com.olam.warehouse.vegax.dispatchnigeria.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.common.model.VegaQualityNigeriaCocoaPostResponse
import com.olam.warehouse.master.common.model.VegaQualityNigeriaPost
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCocoaMtntWithLots
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeDispatchDao
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.dispatchnigeria.data.api.VegaNigeriaCocoaMtntApi
import com.olam.warehouse.vegax.dispatchnigeria.data.domain.model.*
import com.olam.warehouse.vegax.dispatchnigeria.utils.MTNT_WEIGHSCALE
import com.olam.warehouse.vegax.dispatchnigeria.utils.prepareData

interface VegaNigeriaCocoaMtntRepository {
    suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>>
    suspend fun getPurchaseOrder(receivingWerks: String): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaMtntPurchaseOrder>>>>

    suspend fun getQualityParams(
        charge: String,
        material: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>

    suspend fun getQualityParams(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveNigeria>>>>

    suspend fun getSingleProduct(code: String): LiveData<VegaMaterial>
    suspend fun getAllProduct(): LiveData<List<VegaMaterial>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaNigeriaCocoaMtntDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>>
    suspend fun postWeighScaleDeliveryDetail(vegaDeliveryPost: VegaNigeriaCocoaMtntMergedDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaMergedDeliveryPostResponse>>>

    suspend fun saveLots(
        dispatchLotsList: VegaCocoaDispatchLots
    )

    suspend fun getWaitingTrucks1(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>>
    suspend fun postNigeriaQuality(qualityPost: VegaQualityNigeriaPost): LiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>>
    suspend fun getDelivery(
        delivery: String,
        deliveryItem: String
    ): LiveData<Resource<GenericReqAndResp<VegaDispatchDelivery>>>

    suspend fun saveDispatchAndLots(
        dispatchData: VegaCocoaDispatchWB,
        dispatchLotsList: MutableList<VegaCocoaDispatchLots>
    )

    suspend fun insertLot(weighBridgeId: String, lot: VegaCocoaDispatchLots)
    suspend fun insertLotList(weighBridgeId: String, lot: List<VegaCocoaDispatchLots>)
    suspend fun getWbInfo(wbId: String): LiveData<VegaCocoaDispatchWB>
    suspend fun updateLot(batchNumber: String, weight: String)
    suspend fun removeLot(batchNumber: String)
    suspend fun removeLotList()
    suspend fun updateRemarks(remark: String, isStart: Boolean, batchNumber: String)
    suspend fun updateDeliveryItem(deliveryItem: String, deliveryStatus: Boolean, whId: String)
    suspend fun updateStartLoadTime(startLoadTime: String, whId: String)
    suspend fun updateEndLoadTime(endLoadTime: String, turnAroundTime: String, whId: String)
    suspend fun insertTruckInfo(dispatchData: VegaCocoaDispatchWB)
    suspend fun insertMaterialInfo(list: List<VegaCoffeePurchaseOrderMaterialModel>)
    suspend fun getLotsDetails(whId: String): LiveData<List<VegaCocoaDispatchLots>>
    suspend fun validateLot(batchNumber: String): VegaCocoaDispatchLots
    suspend fun deleteTruckAndLots(whId: String)
    suspend fun deleteMaterialData(whId: String)
    suspend fun deleteMaterialData()
    suspend fun getStocksByMaterial(
        materialList: ArrayList<String>
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>

    suspend fun getMtntWithLots(wbId: String): VegaCocoaMtntWithLots
    suspend fun getMtntWithLotsAndMaterial(wbId: String): LiveData<VegaCocoaMtntWithLots>
    suspend fun getOfflineGradeWithBagsRmin(
        fgrnIdWithMatrial: String,
        batchNumber: String
    ): List<VegaCocoaFgrnGradesMatrialWeights>

    suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial)

    suspend fun deleteBagDetails(id: Int)
    suspend fun deleteBagDetails()

    suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaMtntWeighScalePallet>>>>

    suspend fun getBagItems(
        batchNumber: String,
        material: String
    ): LiveData<List<VegaCocoaSweepingBagMaterial>>

    suspend fun getAllBagItems(): LiveData<List<VegaCocoaSweepingBagMaterial>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getWeighScaleInfo(whId: String, stoNo: String): VegaCocoaMtntWithLots
    suspend fun updateAllSync(model: VegaCocoaMtntWithLots)
    suspend fun getLocations(): LiveData<List<VegaSupplyStorageLocation>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getThirdPartyMaterials(): List<VegaCoffeeThirdPartyMaterialDetail>
    suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>>
    suspend fun postWs(vegaCocoaDeliveryPost: VegaNigeriaCocoaMtntMergedDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaMergedDeliveryPostResponse>>>
    suspend fun postWsBinMerge(vegaCocoaDeliveryPost: VegaNigeriaCocoaMtntBinMerge): LiveData<Resource<GenericReqAndResp<MergedData>>>
    suspend fun postWeightedAverage(vegaWeightedAvgPost: VegaNigeriaCocoaWeightedAveragePost): LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaWeightedAverageResponse>>>

}

class VegaNigeriaCocoaMtntRepositoryImpl(
    private val api: VegaNigeriaCocoaMtntApi,
    private val dao: VegaCoffeeDispatchDao,
    private val masterDao: MasterDao
) : VegaNigeriaCocoaMtntRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchWB>> =
                api.fetchTruckList(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getPurchaseOrder(receivingWerks: String): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaMtntPurchaseOrder>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNigeriaCocoaMtntPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNigeriaCocoaMtntPurchaseOrder>> =
                api.getPurchaseOrder(currentKey)
        }.build().asLiveData()
    }

    override suspend fun saveLots(
        dispatchLotsList: VegaCocoaDispatchLots
    ) {
        val list = ArrayList<VegaCocoaDispatchLots>()
        list.add(dispatchLotsList)
        dao.saveLots(list)
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

    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return if (valueExist!!) prepareData(dao.getQualityParameterWithData(materialId, wbId)) else
            dao.getQualityParameter(materialId)
    }

    override suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveNigeria>>>> {

        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityApproveNigeria>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityApproveNigeria>> =
                api.getQuality(currentKey, charge, material)

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

    override suspend fun getMtntWithLots(wbId: String): VegaCocoaMtntWithLots = dao.getMtntWithLotSingle(wbId)
    override suspend fun getMtntWithLotsAndMaterial(wbId: String) = dao.getMtntWithLotAndMaterial(wbId)
    override suspend fun getLocations() = dao.getLocations()
    override suspend fun getCustomLocations() = dao.getCustomLocations()

    override suspend fun getSingleProduct(code: String): LiveData<VegaMaterial> = dao.getSingleProducts(code)
    override suspend fun getAllProduct(): LiveData<List<VegaMaterial>> = dao.getAllProducts()
    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)

    override suspend fun postDeliveryDetail(vegaDeliveryPost: VegaNigeriaCocoaMtntDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaDeliveryPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaDeliveryPostResponse> =
                api.postDeliveryDetail(vegaDeliveryPost)
        }.build().asLiveData()
    }



    override suspend fun postWeighScaleDeliveryDetail(vegaDeliveryPost: VegaNigeriaCocoaMtntMergedDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaMergedDeliveryPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNigeriaCocoaMergedDeliveryPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaNigeriaCocoaMergedDeliveryPostResponse> =
                api.postWeighScaleDeliveryDetailsAuto(vegaDeliveryPost)
        }.build().asLiveData()
    }

    override suspend fun postWs(vegaCocoaDeliveryPost: VegaNigeriaCocoaMtntMergedDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaMergedDeliveryPostResponse>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<VegaNigeriaCocoaMergedDeliveryPostResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaNigeriaCocoaMergedDeliveryPostResponse> =
                api.postWeighScaleDeliveryDetailsAuto(vegaCocoaDeliveryPost)
        }.build().asLiveData()
    }

    override suspend fun postWsBinMerge(vegaCocoaDeliveryPost: VegaNigeriaCocoaMtntBinMerge): LiveData<Resource<GenericReqAndResp<MergedData>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<MergedData>>() {
            override suspend fun createCall(): GenericReqAndResp<MergedData> =
                api.postWeighScaleDeliveryDetailsBinMerge(vegaCocoaDeliveryPost)
        }.build().asLiveData()
    }

    override suspend fun postWeightedAverage(vegaWeightedAvgPost: VegaNigeriaCocoaWeightedAveragePost): LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaWeightedAverageResponse>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<VegaNigeriaCocoaWeightedAverageResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaNigeriaCocoaWeightedAverageResponse> =
                api.postWeightedAverage(vegaWeightedAvgPost)
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

    override suspend fun insertLotList(weighBridgeId: String, lot: List<VegaCocoaDispatchLots>) {
        lot.forEach {
            it.weighBridgeId = weighBridgeId
            it.isAdded = true
        }
        dao.saveLots(lot)
    }

    override suspend fun getWbInfo(wbId: String) = dao.getTruckData(wbId)

    override suspend fun updateLot(batchNumber: String, weight: String) = dao.updateLotWeight(batchNumber, weight)

    override suspend fun removeLot(batchNumber: String) = dao.removeLots(batchNumber)
    override suspend fun removeLotList() = dao.removeLotsList()
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

    override suspend fun insertMaterialInfo(list: List<VegaCoffeePurchaseOrderMaterialModel>) =
        dao.insertMaterialDetail(list)

    override suspend fun getLotsDetails(whId: String) = dao.geLots(whId)

    override suspend fun validateLot(whId: String) = dao.validateLotAlreadyAdded(whId)
    override suspend fun deleteTruckAndLots(whId: String) {
        dao.deleteVegaDispatchTrucks(whId)
        dao.removeLotsByWeighBridgeId(whId)
    }

    override suspend fun deleteMaterialData(whId: String) {
        dao.deleteMaterialData(whId)
    }

    override suspend fun deleteMaterialData() {
        dao.deleteMaterialData()
    }

    override suspend fun getOfflineGradeWithBagsRmin(fgrnIdWithMatrial: String, batchNo: String) =
        dao.getOfflineGradeWithBagsRmin(fgrnIdWithMatrial, batchNo)

    override suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = dao.saveBagDetails(bagMaterial)

    override suspend fun deleteBagDetails(id: Int) = dao.deleteBagDetails(id)
    override suspend fun deleteBagDetails() = dao.deleteBagDetails()

    override suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCocoaMtntWeighScalePallet>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNigeriaCocoaMtntWeighScalePallet>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNigeriaCocoaMtntWeighScalePallet>> =
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

    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getWeighScaleInfo(whId: String, stoNo: String): VegaCocoaMtntWithLots =
        dao.getMtntWithLotSingle(whId, stoNo, MTNT_WEIGHSCALE)

    override suspend fun updateAllSync(model: VegaCocoaMtntWithLots) {
        dao.updateDispatchMtntStatus(model.dispatch.weighBridgeId, true, 4, "")
        model.lineItems.forEach { dao.updateSyncStatusLot(it.batchNumber, 1, 4) }
    }

    override suspend fun getWaitingTrucks1(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaGateEntry>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaGateEntry>> =
                api.fetchWaitingTruckList(currentKey, selectedPlantId)
        }.build().asLiveData()
    }

    override suspend fun postNigeriaQuality(qualityPost: VegaQualityNigeriaPost): LiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse> =
                api.postQualityNigeriaPost(qualityPost)
        }.build().asLiveData()
    }

    override suspend fun getDelivery(
        delivery: String,
        deliveryItem: String
    ): LiveData<Resource<GenericReqAndResp<VegaDispatchDelivery>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaDispatchDelivery>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaDispatchDelivery> =
                api.getDelivery(currentKey, delivery, deliveryItem)
        }.build().asLiveData()
    }

    override suspend fun getThirdPartyMaterials(): List<VegaCoffeeThirdPartyMaterialDetail> =
        dao.getThirdPartyMaterialDetails()

    override suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        dao.getProcessTypeList(role)


}
