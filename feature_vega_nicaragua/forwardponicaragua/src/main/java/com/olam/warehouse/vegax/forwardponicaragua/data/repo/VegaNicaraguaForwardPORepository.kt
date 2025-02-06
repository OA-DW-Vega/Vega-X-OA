package com.olam.warehouse.vegax.forwardponicaragua.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.master.common.model.GrnCharDetails
import com.olam.warehouse.master.common.model.GrnPriceDetails
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaForwardPODao
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaForwardPoPost
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaForwardPoPostResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.forwardponicaragua.data.api.VegaNicaraguaForwardPOApi
import com.olam.warehouse.vegax.forwardponicaragua.data.domain.model.VegaNicaraguaUpdateLotSequencePost
import java.util.*

/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */
interface VegaNicaraguaInvoiceRepository {
    suspend fun getSuppliers(purchaseOrgType: String?): LiveData<List<VegaVendor>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getGrades(materialCode: String): LiveData<List<VegaQualitative>>
    suspend fun getMaterialQualityGrades(materialCode: String): LiveData<List<VegaNicaraguaMaterialQualitGrades>>
    suspend fun getGrnCharDetails(
        grade: String,
        materialCode: String
    ): LiveData<Resource<GenericReqAndResp<List<GrnCharDetails>>>>

    suspend fun getGrnCharDetailsOffline(
        grade: String,
        materialCode: String
    ): LiveData<List<VegaNicaraguaGrnCharDetails>>

    suspend fun getGrnPriceDetails(): LiveData<Resource<GenericReqAndResp<List<GrnPriceDetails>>>>
    suspend fun getGrnPriceDetailsOffline(): LiveData<List<VegaNicaraguaGrnPriceDetails>>
    suspend fun getQualityParams(
        materialId: String
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun postForwardPO(receivingData: VegaNicaraguaForwardPoPost): LiveData<Resource<GenericReqAndResp<VegaNicaraguaForwardPoPost>>>
    suspend fun getExchangeRate(): LiveData<Resource<GenericReqAndResp<ExchangeRate>>>
    suspend fun getExchangeRateOffline(): LiveData<VegaNicaraguaExchangeRate>
    suspend fun saveForwardPOData(receivingData: VegaNicaraguaForwardPODetails)
    suspend fun saveForwardPOPriceDetails(receivingData: ArrayList<VegaNicaraguaForwardPOPriceDetails>)
    suspend fun getForwardPODetails(): LiveData<List<VegaNicaraguaForwardPODetails>>
    suspend fun getForwardPOPriceDetails(tempId: String): LiveData<List<VegaNicaraguaForwardPOPriceDetails>>
    suspend fun deleteItem(tmpWbId: String)
    suspend fun updateLotSequence(postData: VegaNicaraguaUpdateLotSequencePost): LiveData<Resource<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
}

class VegaNicaraguaInvoiceRepositoryImpl(
    private val api: VegaNicaraguaForwardPOApi,
    private val dao: VegaNicaraguaForwardPODao
) :
    VegaNicaraguaInvoiceRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    val currentDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getGrades(materialCode: String) = dao.getQualityGrades(materialCode, "NIPOSITI")
    override suspend fun getMaterialQualityGrades(materialCode: String) = dao.getMaterialQualityGrades(materialCode)
    override suspend fun getSuppliers(purchaseOrgType: String?) = dao.getSuppliers(purchaseOrgType.toString())
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

    override suspend fun getGrnPriceDetails(): LiveData<Resource<GenericReqAndResp<List<GrnPriceDetails>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<GrnPriceDetails>>>() {
            override suspend fun createCall() = api.getGrnPriceDetails(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getGrnPriceDetailsOffline() = dao.getGrnPriceDetails()

    override suspend fun getQualityParams(materialId: String)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return dao.getQualityParameter(materialId)
    }


    override suspend fun postForwardPO(receivingData: VegaNicaraguaForwardPoPost): LiveData<Resource<GenericReqAndResp<VegaNicaraguaForwardPoPost>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNicaraguaForwardPoPost>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaNicaraguaForwardPoPost> =
                api.postForwardPo(receivingData)
        }.build().asLiveData()
    }

    override suspend fun getExchangeRateOffline() = dao.getExchangeRateOffline(currentKey)
    override suspend fun getExchangeRate(): LiveData<Resource<GenericReqAndResp<ExchangeRate>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<ExchangeRate>>() {
            override suspend fun createCall() = api.getExchangeRate(currentDate.toString(), currentKey)
        }.build().asLiveData()
    }

    override suspend fun saveForwardPOData(receivingData: VegaNicaraguaForwardPODetails) =
        dao.saveForwardPOData(receivingData)

    override suspend fun saveForwardPOPriceDetails(receivingData: ArrayList<VegaNicaraguaForwardPOPriceDetails>) =
        dao.saveForwardPOPriceDetails(receivingData)

    override suspend fun getForwardPODetails(): LiveData<List<VegaNicaraguaForwardPODetails>> =
        dao.getForwardPODetails()

    override suspend fun getForwardPOPriceDetails(tempId: String): LiveData<List<VegaNicaraguaForwardPOPriceDetails>> =
        dao.getForwardPOPriceDetails(tempId)

    override suspend fun deleteItem(tmpWbId: String) {
        dao.deleteForwardPoDetails(tmpWbId)
        dao.deleteForwardPoPriceDetails(tmpWbId)
    }
    override suspend fun updateLotSequence(postData: VegaNicaraguaUpdateLotSequencePost): LiveData<Resource<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost> =
                api.updateLotSequence(postData)
        }.build().asLiveData()
    }

    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)

}
