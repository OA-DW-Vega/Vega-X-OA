package com.olam.wharhouse.vegax.transactionhistory.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.VegaFGRNHistoryTranxResponse
import com.olam.warehouse.master.vega.model.VegaGRNHistoryTranxResponse
import com.olam.warehouse.master.vega.model.VegaHistoryTranxMtnr
import com.olam.warehouse.master.vega.model.VegaMtntHistoryTranxResponse
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaRMINHistoryTranxResponse
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorOffloadingDao
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper


/**
 * Created by Baskaran Kannan on 10/8/2022.
 */
interface VegaTransHisRepo

{

    suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String,
        plantId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>

    suspend fun getHistoryTranxMtnr(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaHistoryTranxMtnr>>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getHistoryTranxGrn(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaGRNHistoryTranxResponse>>>

    suspend fun getHistoryTranxMtnt(
        endDate: String,
        plant: String,
        startDate: String
    ): LiveData<Resource<GenericReqAndResp<VegaMtntHistoryTranxResponse>>>

    suspend fun getHistoryTranxFgrn(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaFGRNHistoryTranxResponse>>>

    suspend fun getHistoryTranxRmin(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaRMINHistoryTranxResponse>>>


    suspend fun getuomDetail(): LiveData<List<VegaUomDetails>>
    suspend fun getQualityParams(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>
}

class VegaHistoryTransactionsRepositoryImpl(
    private val apicashew: com.olam.wharhouse.vegax.transactionhistory.data.api.VegaTransHistoryApi,
    private val dao: VegaEcuadorOffloadingDao
) : VegaTransHisRepo {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getHistoryTranxMtnt(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaMtntHistoryTranxResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaMtntHistoryTranxResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaMtntHistoryTranxResponse> =
                apicashew.fetchHistoryTranxMtnt(endDate, getCurrentKey(), plant, startDate)
        }.build().asLiveData()
    }

    override suspend fun getHistoryTranxMtnr(
        endDate: String,
        plant: String,
        startDate: String
    ): LiveData<Resource<GenericReqAndResp<VegaHistoryTranxMtnr>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaHistoryTranxMtnr>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaHistoryTranxMtnr> =
                apicashew.fetchHistoryTranxMtnr(endDate, getCurrentKey(), plant, startDate)
        }.build().asLiveData()
    }

    override suspend fun getHistoryTranxGrn(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaGRNHistoryTranxResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGRNHistoryTranxResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaGRNHistoryTranxResponse> =
                apicashew.fetchHistoryTranxGrn(endDate, getCurrentKey(), plant, startDate)
        }.build().asLiveData()
    }

    override suspend fun getHistoryTranxFgrn(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaFGRNHistoryTranxResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaFGRNHistoryTranxResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaFGRNHistoryTranxResponse> =
                apicashew.fetchHistoryTranxFgrn(endDate, getCurrentKey(), plant, startDate,"FGRN")
        }.build().asLiveData()
    }

    override suspend fun getHistoryTranxRmin(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaRMINHistoryTranxResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaRMINHistoryTranxResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaRMINHistoryTranxResponse> =
                apicashew.fetchHistoryTranxRmin(endDate, getCurrentKey(), plant, startDate,"RMIN")
        }.build().asLiveData()
    }

    override suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String,
        plantId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityPreParameter>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityPreParameter>> =
                apicashew.fetchQualityDetails(currentKey, batchNo, materialId,plantId)
        }.build().asLiveData()
    }

    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getuomDetail() = dao.getuomDetail()
    override suspend fun getQualityParams(materialId: String) = dao.getQualityParameter(materialId)

}

//class VegaGhanaCashewTransHisRepoImpl(private val api: VegaGhanaCashewTransHisApi, private val dao: VegaReceivingDao) : VegaGhanaCashewTransHisRepo
