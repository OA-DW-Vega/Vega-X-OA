package com.olam.warehouse.vegax.secretidnigeria.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.secretidnigeria.data.api.VegaNigeriaSecretIdApi
import com.olam.warehouse.vegax.secretidnigeria.data.domain.model.VegaNigeriaSecretId
import com.olam.warehouse.vegax.secretidnigeria.utils.WB
import com.olam.warehouse.vegax.secretidnigeria.utils.WEIGHBRIDGE
import com.olam.warehouse.vegax.secretidnigeria.utils.WEIGHSCALE

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
interface VegaNigeriaSecretIdRepository {
    suspend fun getDashboardResult(
        startDate: String,
        endDate: String,
        isMtnt: Boolean
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaSecretId>>>>

    suspend fun getWbWeightDetails(wbid: String,wbType: String): LiveData<Resource<GenericReqAndResp<VegaQualityWBDetails>>>
    suspend fun getfetchWBListforMultiPlants(
        isMTNT: Boolean,
        startDate: String,
        endDate: String,
        plantList: List<String>,
        wbtype: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaSecretId>>>>
}

class VegaNigeriaSecretIdRepositoryImpl(
    private val api: VegaNigeriaSecretIdApi//, private val dao: VegaQualityDao
) : VegaNigeriaSecretIdRepository {

    override suspend fun getDashboardResult(
        startDate: String,
        endDate: String,
        isMtnt: Boolean
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaSecretId>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNigeriaSecretId>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNigeriaSecretId>> =
                api.getDashboardResult(getCurrentKey(), isMtnt, startDate, endDate)
        }.build().asLiveData()
    }


    override suspend fun getWbWeightDetails(wbid: String,wbType: String): LiveData<Resource<GenericReqAndResp<VegaQualityWBDetails>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityWBDetails>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaQualityWBDetails> =
                if(wbType.equals(WB)) {
                    api.getWeighBridgeIdDetail(getCurrentKey(), wbid)
                }else{
                    api.getWbWeightDetails(getCurrentKey(), wbid)
                }
        }.build().asLiveData()
    }

    override suspend fun getfetchWBListforMultiPlants(
        isMTNT: Boolean,
        startDate: String,
        endDate: String,
        plantList: List<String>,
        wbtype: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaSecretId>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNigeriaSecretId>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNigeriaSecretId>> =
                if(wbtype.equals(WB)){
                    api.fetchWBListforMultiPlants(
                        getCurrentKey(),
                        isMTNT,
                        startDate,
                        endDate,
                        plantList,
                        WEIGHBRIDGE
                    )
                }else{
                    api.fetchWBListforMultiPlants(
                        getCurrentKey(),
                        isMTNT,
                        startDate,
                        endDate,
                        plantList,
                        WEIGHSCALE
                    )
                }

        }.build().asLiveData()
    }

}
