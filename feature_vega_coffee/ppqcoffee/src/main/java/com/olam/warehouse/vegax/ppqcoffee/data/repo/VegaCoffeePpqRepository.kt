package com.olam.warehouse.vegax.ppqcoffee.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.ppqcoffee.data.api.VegaCoffeePpqApi
import com.olam.warehouse.vegax.ppqcoffee.data.domain.model.VegaCoffeePpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqcoffee.data.domain.model.VegaCoffeePpqInspectionLots
import com.olam.warehouse.vegax.ppqcoffee.data.domain.model.VegaCoffeePpqPostResponse

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
interface VegaCoffeePpqRepository {
    suspend fun getInspectionLots(): LiveData<Resource<GenericReqAndResp<List<VegaCoffeePpqInspectionLots>>>>
    suspend fun getInspectionLotDetails(lotId: String): LiveData<Resource<GenericReqAndResp<VegaCoffeePpqInspectionLotDetails>>>
    suspend fun saveInspectionLotDetails(lotDetail: VegaCoffeePpqInspectionLotDetails): LiveData<Resource<GenericReqAndResp<VegaCoffeePpqPostResponse>>>
}

class VegaCoffeePpqRepositoryImpl(
    private val api: VegaCoffeePpqApi
) : VegaCoffeePpqRepository {

    override suspend fun getInspectionLots(): LiveData<Resource<GenericReqAndResp<List<VegaCoffeePpqInspectionLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeePpqInspectionLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeePpqInspectionLots>> =
                api.getInspectionLots(getCurrentKey(), PreferenceHelper.get(Constants.WERKS, ""))
        }.build().asLiveData()
    }

    override suspend fun getInspectionLotDetails(lotId: String): LiveData<Resource<GenericReqAndResp<VegaCoffeePpqInspectionLotDetails>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeePpqInspectionLotDetails>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCoffeePpqInspectionLotDetails> =
                api.getInspectionLotDetails(getCurrentKey(), lotId, PreferenceHelper.get(Constants.WERKS, ""))
        }.build().asLiveData()
    }

    override suspend fun saveInspectionLotDetails(lotDetail: VegaCoffeePpqInspectionLotDetails): LiveData<Resource<GenericReqAndResp<VegaCoffeePpqPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeePpqPostResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCoffeePpqPostResponse> =
                api.saveInspectionLotDetails(getCurrentKey(), PreferenceHelper.get(Constants.WERKS, ""), lotDetail)
        }.build().asLiveData()
    }

}
