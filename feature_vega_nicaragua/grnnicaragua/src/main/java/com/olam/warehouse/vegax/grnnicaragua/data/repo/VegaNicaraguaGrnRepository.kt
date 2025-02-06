package com.olam.warehouse.vegax.grnnicaragua.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.AdvanceLineItems
import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.master.common.model.GrnCharDetails
import com.olam.warehouse.master.common.model.GrnPriceDetails
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaGrnDao
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaGrnPost
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.grnnicaragua.data.api.VegaNicaraguaGrnApi
import com.olam.warehouse.vegax.grnnicaragua.data.domain.model.VegaNicaraguaUpdateLotSequencePost
import com.olam.warehouse.vegax.grnnicaragua.utils.preparaDataOffline
import java.util.*

/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */
interface VegaNicaraguaGrnRepository {
    suspend fun getSuppliers(purchaseOrgType: String?): LiveData<List<VegaVendor>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getStorageLocations(): LiveData<List<VegaStorageLocation>>
    suspend fun getGrades(materialCode: String): LiveData<List<VegaQualitative>>
    suspend fun getMaterialQualityGrades(materialCode: String): LiveData<List<VegaNicaraguaMaterialQualitGrades>>
    suspend fun saveBagDetails(material: VegaNicaraguaWeighmentBagMaterial)
    suspend fun deleteBagDetails(id: Int, tmpWbId: String)
    suspend fun getPriceConfigInfo(materialCode: String, qualityCode: String): LiveData<VegaNicaraguaPriceConfigDetails>
    suspend fun getExchangeRate(): LiveData<Resource<GenericReqAndResp<ExchangeRate>>>
    suspend fun getGrnPriceDetails(): LiveData<Resource<GenericReqAndResp<List<GrnPriceDetails>>>>
    suspend fun getGrnPriceDetailsOffline(): LiveData<List<VegaNicaraguaGrnPriceDetails>>
    suspend fun getExchangeRateOffline(): LiveData<VegaNicaraguaExchangeRate>
    suspend fun getBagItems(tmpWbId: String): LiveData<List<VegaNicaraguaWeighmentBagMaterial>>
    suspend fun getQualityParams(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun postGrnDetails(receivingData: VegaNicaraguaGrnPost): LiveData<Resource<GenericReqAndResp<VegaNicaraguaGrnPost>>>
    suspend fun getGrnCharDetails(
        grade: String,
        materialCode: String
    ): LiveData<Resource<GenericReqAndResp<List<GrnCharDetails>>>>

    suspend fun getGrnCharDetailsOffline(
        grade: String,
        materialCode: String
    ): LiveData<List<VegaNicaraguaGrnCharDetails>>
    suspend fun getGradeMapping(grade: String): LiveData<VegaNicaraguaPositionGradeMappings>
    suspend fun saveQualityData(prepareQualityData: VegaQuality)
    suspend fun saveGrnData(receivingData: VegaReceiving)
    suspend fun getReceivingWithLineItem(): LiveData<List<VegaReceiving>>
    suspend fun getQuality(tmpWbId: String): LiveData<List<VegaQuality>>
    suspend fun updateGrnDataSuccess(
        data: VegaNicaraguaGrnPost,
        vegaReceiving: VegaReceiving
    )

    suspend fun deleteAllItem(tmpWbId: String)
    suspend fun getGlDetails(role: String): LiveData<List<VegaCocoaMiscellaneous>>
    suspend fun geAdvanceLineItemDetails(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<AdvanceLineItems>>>>
    suspend fun geAdvanceLineItemDetailsOffline(vendorCode: String): LiveData<List<VegaNicaraguaAdvanceLineItems>>
    suspend fun saveAdvanceLineItem(advanceLineItem: ArrayList<VegaNicaraguaAdvanceLineItemGrn>)
    suspend fun getAdvanceItem(tmpId: String): LiveData<List<VegaNicaraguaAdvanceLineItemGrn>>
    suspend fun updateLotSequence(postData: VegaNicaraguaUpdateLotSequencePost): LiveData<Resource<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>>
    suspend fun updateSyncStartedStatus(tmpWbId: String)
    suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>
    suspend fun getPOListLocal(): LiveData<List<VegaEcuadorPurchaseOrder>>
    suspend fun getGrnPrintDetails(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>
    suspend fun getGrnPrintDetailsOffline(): LiveData<List<VegaReceiving>>
    suspend fun removeAdvanceLineItem(documentNumber: String?, tmpWbId: String)
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun updatePostingDate(tmpWbId: String, postdate: String)
    suspend fun getReceivingWithLineItemDirect(): List<VegaReceiving>
    suspend fun getInvoiceOfflineData(): LiveData<List<VegaNicaraguaInvoiceDetails>>
    suspend fun getFarmerList(): LiveData<List<VegaTrackTraceFarmerData>>

}

class VegaNicaraguaGrnRepositoryImpl(private val api: VegaNicaraguaGrnApi, private val dao: VegaNicaraguaGrnDao) :
    VegaNicaraguaGrnRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    val currentDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")

    override suspend fun getSuppliers(purchaseOrgType: String?) =
        if (purchaseOrgType?.isNotEmpty() == true) dao.getSuppliers(purchaseOrgType.toString()) else dao.getMaterials()

    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getStorageLocations() = dao.getStorageLocations(PreferenceHelper.get(Constants.WERKS, ""))
    override suspend fun getGrades(materialCode: String) = dao.getQualityGrades(materialCode, "NIPOSITI")
    override suspend fun saveBagDetails(material: VegaNicaraguaWeighmentBagMaterial) = dao.saveBagDetails(material)
    override suspend fun deleteBagDetails(id: Int, tmpWbId: String) = dao.deleteBagDetails(id, tmpWbId)
    override suspend fun getBagItems(tmpWbId: String): LiveData<List<VegaNicaraguaWeighmentBagMaterial>> =
        dao.getBagItems(tmpWbId)

    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        val qty = dao.getQualityParams(wbId.toString())
        val qtyParams = dao.getQualityParameterOffline(materialId)
        return preparaDataOffline(qty, qtyParams)
    }

    override suspend fun postGrnDetails(receivingData: VegaNicaraguaGrnPost): LiveData<Resource<GenericReqAndResp<VegaNicaraguaGrnPost>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNicaraguaGrnPost>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaNicaraguaGrnPost> =
                api.postGrnData(receivingData)
        }.build().asLiveData()
    }

    override suspend fun getPriceConfigInfo(materialCode: String, qualityCode: String) =
        dao.getPriceConfigInfo(materialCode, qualityCode)

    override suspend fun getExchangeRateOffline() = dao.getExchangeRateOffline(currentKey)
    override suspend fun getExchangeRate(): LiveData<Resource<GenericReqAndResp<ExchangeRate>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<ExchangeRate>>() {
            override suspend fun createCall() = api.getExchangeRate(currentDate.toString(), currentKey)
        }.build().asLiveData()
    }

    override suspend fun getGrnPriceDetails(): LiveData<Resource<GenericReqAndResp<List<GrnPriceDetails>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<GrnPriceDetails>>>() {
            override suspend fun createCall() = api.getGrnPriceDetails(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getGrnPriceDetailsOffline() = dao.getGrnPriceDetails()

    override suspend fun getGrnCharDetails(
        grade: String,
        materialCode: String
    ): LiveData<Resource<GenericReqAndResp<List<GrnCharDetails>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<GrnCharDetails>>>() {
            override suspend fun createCall() = api.getGrnCharDetails(grade, currentKey, materialCode)
        }.build().asLiveData()
    }

    override suspend fun getGrnCharDetailsOffline(
        grade: String,
        materialCode: String
    ): LiveData<List<VegaNicaraguaGrnCharDetails>> {
        val data = dao.getGrnCharDetailsCount(grade, materialCode)
        return if (data.isNotEmpty()) dao.getGrnCharDetails(
            grade,
            materialCode
        ) else dao.getGrnCharDetails(grade.replace("\\s+".toRegex(), " "), materialCode)
    }

    override suspend fun getGradeMapping(grade: String) = dao.getGradeMapping(grade)
    override suspend fun saveQualityData(prepareQualityData: VegaQuality) = dao.saveQualityData(prepareQualityData)
    override suspend fun saveGrnData(receivingData: VegaReceiving){
        dao.saveGrnData(receivingData)
        dao.saveTTFarmerData(receivingData.ttFarmerList)
        dao.saveFarmerTransData(receivingData.farmerTransDetails)
    }
    override suspend fun getReceivingWithLineItem(): LiveData<List<VegaReceiving>> = dao.getReceivingWithLineItem()
    override suspend fun getQuality(tmpWbId: String): LiveData<List<VegaQuality>> = dao.getQuality(tmpWbId)
    override suspend fun updateGrnDataSuccess(
        data: VegaNicaraguaGrnPost,
        vegaReceiving: VegaReceiving
    ) {
        if(data.wbFlag == true && data.qcFlag == true && data.grnFlag ==true) {
            if (vegaReceiving.grnType.equals("ptbf")) {
                vegaReceiving.weighBridgeId = data.wbId.toString()
                vegaReceiving.status = Status.SYNC_COMPLETED
                vegaReceiving.isSynced = true
                vegaReceiving.syncStatusMsg = ""
                vegaReceiving.batchNumber = data.batchNumber.toString()
                vegaReceiving.grnNumber = data.grnNumber.toString()
                vegaReceiving.purchaseDocNum =
                    if (data.poNumber?.isNotEmpty() == true) data.poNumber.toString() else vegaReceiving.purchaseDocNum
            } else if (vegaReceiving.grnType?.contains("tolling", true) == true) {
                vegaReceiving.weighBridgeId = data.wbId.toString()
                vegaReceiving.status = Status.SYNC_COMPLETED
                vegaReceiving.isSynced = true
                vegaReceiving.syncStatusMsg = ""
                vegaReceiving.batchNumber = data.batchNumber.toString()
                vegaReceiving.grnNumber = data.grnNumber.toString()
                vegaReceiving.purchaseDocNum =
                    if (data.poNumber?.isNotEmpty() == true) data.poNumber.toString() else vegaReceiving.purchaseDocNum
            } else {
                if (data.invoiceFlag == true) {
                    vegaReceiving.weighBridgeId = data.wbId.toString()
                    vegaReceiving.status = Status.SYNC_COMPLETED
                    vegaReceiving.isSynced = true
                    vegaReceiving.syncStatusMsg = ""
                    vegaReceiving.batchNumber = data.batchNumber.toString()
                    vegaReceiving.grnNumber = data.grnNumber.toString()
                    vegaReceiving.purchaseDocNum =
                        if (data.poNumber?.isNotEmpty() == true) data.poNumber.toString() else vegaReceiving.purchaseDocNum

                }else {
                    vegaReceiving.weighBridgeId = data.wbId.toString()
                    vegaReceiving.status = Status.SYNC_PENDING
                    vegaReceiving.isSynced = false
                    vegaReceiving.batchNumber =
                        if (data.batchNumber.toString().isNotEmpty()) data.batchNumber.toString() else vegaReceiving.batchNumber
                    vegaReceiving.grnNumber = data.grnNumber.toString()
                    vegaReceiving.purchaseDocNum =
                        if (data.poNumber?.isNotEmpty() == true) data.poNumber.toString() else vegaReceiving.purchaseDocNum
                }

            }
        }else {
            vegaReceiving.weighBridgeId = data.wbId.toString()
            vegaReceiving.status = Status.SYNC_PENDING
            vegaReceiving.isSynced = false
            vegaReceiving.syncStatusMsg = data.errorMessage
            vegaReceiving.batchNumber =
                if (data.batchNumber.toString().isNotEmpty()) data.batchNumber.toString() else vegaReceiving.batchNumber
            vegaReceiving.grnNumber = data.grnNumber.toString()
            vegaReceiving.purchaseDocNum =
                if (data.poNumber?.isNotEmpty() == true) data.poNumber.toString() else vegaReceiving.purchaseDocNum
        }
        vegaReceiving.wbFlag = data.wbFlag
        vegaReceiving.qcFlag = data.qcFlag
        vegaReceiving.grnFlag = data.grnFlag
        vegaReceiving.invoiceFlag=data.invoiceFlag
        vegaReceiving.erdat = DateUtils.getCurrentTimeInMills().toString()
        dao.saveGrnData(vegaReceiving)
    }

    override suspend fun deleteAllItem(tmpWbId: String) {
        dao.updateDeleteFlag(tmpWbId)
        /*dao.deleteAllBagDetails(tmpWbId)
        dao.deleteGrnDetail(tmpWbId)
        dao.deleteQualityDetail(tmpWbId)*/
    }

    override suspend fun getGlDetails(role: String): LiveData<List<VegaCocoaMiscellaneous>> = dao.getGlDetails(role)

    override suspend fun geAdvanceLineItemDetails(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<AdvanceLineItems>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<AdvanceLineItems>>>() {
            override suspend fun createCall() = api.getAdvanceLineItems(currentKey, vendorCode)
        }.build().asLiveData()
    }

    override suspend fun geAdvanceLineItemDetailsOffline(vendorCode: String) = dao.getAdvanceLineItems(vendorCode)
    override suspend fun saveAdvanceLineItem(advanceLineItem: ArrayList<VegaNicaraguaAdvanceLineItemGrn>) =
        dao.saveAdvanceLineItem(advanceLineItem)

    override suspend fun getAdvanceItem(tmpId: String) = dao.getAdvanceItem(tmpId)

    override suspend fun updateLotSequence(postData: VegaNicaraguaUpdateLotSequencePost): LiveData<Resource<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost> =
                api.updateLotSequence(postData)
        }.build().asLiveData()
    }

    override suspend fun updateSyncStartedStatus(tmpWbId: String) =
        dao.updateSyncStartedStatus(tmpWbId)

    override suspend fun getMaterialQualityGrades(materialCode: String) =
        if (materialCode.isNotEmpty()) dao.getMaterialQualityGrades(materialCode) else dao.getAllMaterialQualityGrades()

    override suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>> =
                api.getPurchaseOrders(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun getPOListLocal(): LiveData<List<VegaEcuadorPurchaseOrder>> =
        dao.getPOListLocal()

    override suspend fun getGrnPrintDetails(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaReceiving>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaReceiving>> =
                api.getGrnPrintDetails(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun getGrnPrintDetailsOffline(): LiveData<List<VegaReceiving>> =
        dao.getGrnPrintDetailsOffline(Status.SYNC_REPRINT)

    override suspend fun removeAdvanceLineItem(documentNumber: String?, tmpWbId: String) =
        dao.removeAdvanceLineItem(documentNumber, tmpWbId)

    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)
    override suspend fun updatePostingDate(tmpWbId: String, postdate: String)  = dao.updatePostingDate(tmpWbId,postdate)
    override suspend fun getReceivingWithLineItemDirect(): List<VegaReceiving>  = dao.getReceivingWithLineItemDirect()
    override suspend fun getInvoiceOfflineData() = dao.getInvoiceOfflineData1()

    override suspend fun getFarmerList() = dao.getFarmerList()

}
