package com.olam.warehouse.vegax.secretid.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.secretid.data.api.VegaCameroonSecretIdApi
import com.olam.warehouse.vegax.secretid.data.domain.model.VegaCameroonSecretId

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
interface VegaCameroonSecretIdRepository {
    suspend fun getDashboardResult(
        startDate: String,
        endDate: String,
        isMtnt: Boolean
    ): LiveData<Resource<GenericReqAndResp<List<VegaCameroonSecretId>>>>

    suspend fun getWbWeightDetails(wbid: String): LiveData<Resource<GenericReqAndResp<VegaQualityWBDetails>>>

}

class VegaCameroonSecretIdRepositoryImpl(
    private val api: VegaCameroonSecretIdApi, private val dao: VegaQualityDao
) : VegaCameroonSecretIdRepository {

    override suspend fun getDashboardResult(
        startDate: String,
        endDate: String,
        isMtnt: Boolean
    ): LiveData<Resource<GenericReqAndResp<List<VegaCameroonSecretId>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCameroonSecretId>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCameroonSecretId>> =
                api.getDashboardResult(getCurrentKey(), isMtnt, startDate, endDate)
        }.build().asLiveData()
    }


    override suspend fun getWbWeightDetails(wbid: String): LiveData<Resource<GenericReqAndResp<VegaQualityWBDetails>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityWBDetails>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaQualityWBDetails> =
                api.getWbWeightDetails(getCurrentKey(), wbid)
        }.build().asLiveData()
    }

}
