package com.olam.warehouse.vegax.ppqcameroon.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.ppqcameroon.data.api.VegaCameroonPpqApi
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqInspectionLots
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqPostResponse

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
interface VegaCameroonPpqRepository {
    suspend fun getQualityParams(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>
    suspend fun getInspectionLots(): LiveData<Resource<GenericReqAndResp<List<VegaCameroonPpqInspectionLots>>>>
    suspend fun getInspectionLotDetails(lotId: String): LiveData<Resource<GenericReqAndResp<VegaCameroonPpqInspectionLotDetails>>>
    suspend fun saveInspectionLotDetails(lotDetail: VegaCameroonPpqInspectionLotDetails): LiveData<Resource<GenericReqAndResp<VegaCameroonPpqPostResponse>>>
}

class VegaCameroonPpqRepositoryImpl(
    private val api: VegaCameroonPpqApi,private val dao: VegaQualityDao
) : VegaCameroonPpqRepository {

    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return dao.getQualityParameter(materialId)
    }

    override suspend fun getInspectionLots(): LiveData<Resource<GenericReqAndResp<List<VegaCameroonPpqInspectionLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCameroonPpqInspectionLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCameroonPpqInspectionLots>> =
                api.getInspectionLots(getCurrentKey(), PreferenceHelper.get(Constants.WERKS, ""))
        }.build().asLiveData()
    }

    override suspend fun getInspectionLotDetails(lotId: String): LiveData<Resource<GenericReqAndResp<VegaCameroonPpqInspectionLotDetails>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCameroonPpqInspectionLotDetails>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCameroonPpqInspectionLotDetails> =
                api.getInspectionLotDetails(getCurrentKey(), lotId, PreferenceHelper.get(Constants.WERKS, ""))
        }.build().asLiveData()
    }

    override suspend fun saveInspectionLotDetails(lotDetail: VegaCameroonPpqInspectionLotDetails): LiveData<Resource<GenericReqAndResp<VegaCameroonPpqPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCameroonPpqPostResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCameroonPpqPostResponse> =
                api.saveInspectionLotDetails(getCurrentKey(), PreferenceHelper.get(Constants.WERKS, ""), lotDetail)
        }.build().asLiveData()
    }

}
