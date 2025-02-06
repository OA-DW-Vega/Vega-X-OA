package com.olam.wharhouse.vegax.transhistoryghanacashew.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.dao.VegaReceivingDao
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorOffloadingDao
import com.olam.warehouse.master.vegaghana.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.wharhouse.vegax.transhistoryghanacashew.data.api.VegaGhanaCashewTransHisApi

/**
 * Created by Baskaran Kannan on 10/8/2022.
 */
interface VegaGhanaCashewTransHisRepo

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
    ) : LiveData<Resource<GenericReqAndResp<VegaGhanaCashewHistoryTranxMtnr>>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getHistoryTranxGrn(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaGhanaCashewGRNHistoryTranxResponse>>>

    suspend fun getHistoryTranxMtnt(
        endDate: String,
        plant: String,
        startDate: String
    ): LiveData<Resource<GenericReqAndResp<VegaGhanaCashewMtntHistoryTranxResponse>>>

    suspend fun getHistoryTranxFgrn(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaGhanaCashewFGRNHistoryTranxResponse>>>

    suspend fun getHistoryTranxRmin(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaGhanaCashewRMINHistoryTranxResponse>>>


    suspend fun getuomDetail(): LiveData<List<VegaUomDetails>>
    suspend fun getQualityParams(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>
}

class VegaHistoryTransactionsGhanaCashewRepositoryImpl(
    private val apicashew: VegaGhanaCashewTransHisApi,
    private val dao: VegaEcuadorOffloadingDao
) : VegaGhanaCashewTransHisRepo {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getHistoryTranxMtnt(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaGhanaCashewMtntHistoryTranxResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGhanaCashewMtntHistoryTranxResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaGhanaCashewMtntHistoryTranxResponse> =
                apicashew.fetchHistoryTranxMtnt(endDate, getCurrentKey(), plant, startDate)
        }.build().asLiveData()
    }

    override suspend fun getHistoryTranxMtnr(
        endDate: String,
        plant: String,
        startDate: String
    ): LiveData<Resource<GenericReqAndResp<VegaGhanaCashewHistoryTranxMtnr>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGhanaCashewHistoryTranxMtnr>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaGhanaCashewHistoryTranxMtnr> =
                apicashew.fetchHistoryTranxMtnr(endDate, getCurrentKey(), plant, startDate)
        }.build().asLiveData()
    }

    override suspend fun getHistoryTranxGrn(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaGhanaCashewGRNHistoryTranxResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGhanaCashewGRNHistoryTranxResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaGhanaCashewGRNHistoryTranxResponse> =
                apicashew.fetchHistoryTranxGrn(endDate, getCurrentKey(), plant, startDate)
        }.build().asLiveData()
    }

    override suspend fun getHistoryTranxFgrn(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaGhanaCashewFGRNHistoryTranxResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGhanaCashewFGRNHistoryTranxResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaGhanaCashewFGRNHistoryTranxResponse> =
                apicashew.fetchHistoryTranxFgrn(endDate, getCurrentKey(), plant, startDate,"FGRN")
        }.build().asLiveData()
    }

    override suspend fun getHistoryTranxRmin(
        endDate: String,
        plant: String,
        startDate: String
    ) : LiveData<Resource<GenericReqAndResp<VegaGhanaCashewRMINHistoryTranxResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGhanaCashewRMINHistoryTranxResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaGhanaCashewRMINHistoryTranxResponse> =
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
