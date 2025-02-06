package com.olam.warehouse.vegax.mtntnicaragua.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.veganicaragua.entity.VegaNicDispatchLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMtnt
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaMtnrReprintList
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicMtntDeliveryPost
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicaraguaUpdateLotSequencePost
import com.olam.warehouse.vegax.mtntnicaragua.data.repo.VegaNicaraguaMtntRepository
import com.olam.warehouse.vegax.mtntnicaragua.utils.MTNR
import java.util.*

/**
 * Created by Baskaran Kannan on 11/11/2020.
 */
class VegaNicaraguaMtntUsecase(private val repo: VegaNicaraguaMtntRepository) {
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>> = repo.getMaterials()
    suspend fun getAllProducts() = repo.getAllProduct()
    suspend fun getSupplier() = repo.getSuppliers()
    suspend fun getPurchaseOrder(receivingWerks: String) = repo.getPurchaseOrder(receivingWerks)
    suspend fun insertTruckInfo(mtnt: VegaNicaraguaMtnt) = repo.insertTruckInfo(mtnt)
    suspend fun getMtntWithLots(tmpId: String) = repo.getMtntWithLots(tmpId)
    suspend fun validateLot(batchNumber: String): VegaNicDispatchLots = repo.validateLot(batchNumber)
    suspend fun getLotDetails(charge: String, material: List<String>, whId: String) =
        repo.getLotDetails(charge, material, whId)

    suspend fun saveLotDetails(lot: VegaNicDispatchLots) = repo.saveLotDetails(lot)
    suspend fun removeLotFromList(batchNumber: String, tempId: String) = repo.removeLotFromList(batchNumber, tempId)
    suspend fun saveBagDetails(material: VegaNicaraguaWeighmentBagMaterial) = repo.saveBagDetails(material)
    suspend fun getBagItems(batchNumber: String, tempId: String): LiveData<List<VegaNicaraguaWeighmentBagMaterial>> =
        repo.getBagItems(batchNumber, tempId)

    suspend fun deleteBagDetails(id: Int) = repo.deleteBagDetails(id)
    suspend fun getStocks(material: ArrayList<String>) = repo.getStocks(material)
    suspend fun getStocksSap(material: ArrayList<String>) = repo.getStocksSap(material)
    suspend fun saveLotList(lotList: List<VegaNicDispatchLots>) = repo.saveLotList(lotList)
    suspend fun getStockListOffline(material: ArrayList<String>) = repo.getStockListOffline(material)
    suspend fun getGrades(materialCode: String) = repo.getGrades(materialCode)
    suspend fun getCertification(materialCode: String) = repo.getCertification(materialCode)
    suspend fun getMaterialQualityGrades(materialCode: String) = repo.getMaterialQualityGrades(materialCode)
    suspend fun postNicMtntWSDeliveryDetails(vegaNicMtntDeliveryPost: VegaNicMtntDeliveryPost) =
        repo.postNicMtntWSDeliveryDetails(vegaNicMtntDeliveryPost)

    suspend fun getListOfMtntWithLots() = repo.getListOfMtntWithLots()
    suspend fun deleteAllItem(tmpWbId: String) = repo.deleteAllItem(tmpWbId)
    suspend fun getProducts(): List<VegaMaterial> = repo.getProducts()
    suspend fun getPurchaseOrderOffline(): List<VegaCocoaPurchaseOrders> = repo.getPurchaseOrderOffline()
    suspend fun getGrnPrintDetails() = repo.getGrnPrintDetails()
    suspend fun getMtnrPrintDetails(grnno: String, batchNo: String, MTNR_RECEIPT: String) = repo.getmtnrPrintDetails(grnno,batchNo, MTNR_RECEIPT)
    suspend fun getMtnrReprintList() = repo.getmtnrreprintlist()
    suspend fun getOfflineLotDetails(lotId: String, materialCode: String) =
        repo.getOfflineLotDetails(lotId, materialCode)
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> =
        repo.getConfigItems(role)

    suspend fun getWeighBridgeDetailOnline(material: String) =
        repo.getWeighBridgeDetailOnline(material)

    suspend fun updateLotSequence(postData: VegaNicaraguaUpdateLotSequencePost) =
        repo.updateLotSequence(postData)
    suspend fun getQualityParams(materialId: String) =
        repo.getQualityParams(materialId)

//    suspend fun getLotQuality(material: String): LiveData<Resource<GenericReqAndResp<List<VegaDispatchLotQuality>>>>  = repo.getLotQuality(material)
}
