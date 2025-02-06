package com.olam.warehouse.vegax.reports.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.presentation.data.domain.model.Auth
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.reports.data.api.VegaCommonReportAdTokenApi
import com.olam.warehouse.vegax.reports.data.api.VegaCommonReportApi
import com.olam.warehouse.vegax.reports.data.domain.VegaCommonReportDataSetModel
import com.olam.warehouse.vegax.reports.data.domain.VegaCommonReportDataSetTokenModel
import com.olam.warehouse.vegax.reports.data.domain.VegaCommonReportDataSetTokenRequest
import com.olam.warehouse.vegax.reports.utils.clientId
import com.olam.warehouse.vegax.reports.utils.clientSecret
import com.olam.warehouse.vegax.reports.utils.grantType
import com.olam.warehouse.vegax.reports.utils.groupId
import com.olam.warehouse.vegax.reports.utils.reportId
import com.olam.warehouse.vegax.reports.utils.resource
import com.olam.warehouse.vegax.reports.utils.tanetId

interface VegaCommonReportRepo {
    suspend fun getADToken(): LiveData<Resource<Auth>>
    suspend fun getDataSet(accessToken: String): LiveData<Resource<VegaCommonReportDataSetModel>>
    suspend fun getDataSetToken(model: VegaCommonReportDataSetTokenRequest): LiveData<Resource<VegaCommonReportDataSetTokenModel>>
}

class VegaCommonReportRepoImpl(private val api: VegaCommonReportApi, private val adApi: VegaCommonReportAdTokenApi) : VegaCommonReportRepo{
    override suspend fun getADToken(): LiveData<Resource<Auth>> {
        return object : NetworkOnlyBoundResource<Auth>() {
            override suspend fun createCall(): Auth =
                adApi.getAdToken(
                    grantType = grantType,
                    resource = resource,
                    clientId = clientId,
                    clientSecret = clientSecret,
                    tanetId = tanetId
                )
        }.build().asLiveData()
    }

    override suspend fun getDataSet(accessToken: String): LiveData<Resource<VegaCommonReportDataSetModel>> {
        return object : NetworkOnlyBoundResource<VegaCommonReportDataSetModel>() {
//            val map: HashMap<String, String> = hashMapOf("Authorization" to "bearer $accessToken")
            override suspend fun createCall(): VegaCommonReportDataSetModel =
                api.getDataSet(
                    groupId = groupId,
                    reportId = reportId
                )
        }.build().asLiveData()
    }

    override suspend fun getDataSetToken(model: VegaCommonReportDataSetTokenRequest): LiveData<Resource<VegaCommonReportDataSetTokenModel>> {
        return object : NetworkOnlyBoundResource<VegaCommonReportDataSetTokenModel>() {
            override suspend fun createCall(): VegaCommonReportDataSetTokenModel =
                api.getDatasetToken(model)
        }.build().asLiveData()
    }

}
