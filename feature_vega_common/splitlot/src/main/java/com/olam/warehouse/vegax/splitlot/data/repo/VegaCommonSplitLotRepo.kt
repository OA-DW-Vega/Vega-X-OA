package com.olam.warehouse.vegax.splitlot.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaGrnDao
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.splitlot.data.api.VegaCommonSplitLotApi
import com.olam.warehouse.vegax.splitlot.data.domain.model.SplitUpdateTallySequencePost
import com.olam.warehouse.vegax.splitlot.data.domain.model.VegaCoffeeQualityParamPost
import com.olam.warehouse.vegax.splitlot.data.domain.model.VegaCoffeeQualityParamPostResponse
import com.olam.warehouse.vegax.splitlot.data.domain.model.VegaCommonSplitMainModel

/**
 * Created by Baskaran Kannan on 9/26/2022.
 */
interface VegaCommonSplitLotRepo {
    suspend fun getLotQualityDetails(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>
    suspend fun getMaterialQualityGrades(materialCode: String): LiveData<List<VegaNicaraguaMaterialQualitGrades>>
    suspend fun getGrades(materialCode: String): LiveData<List<VegaQualitative>>
    suspend fun postSplitDeatils(postData: VegaCommonSplitMainModel): LiveData<Resource<GenericReqAndResp<VegaCommonSplitMainModel>>>
    suspend fun postQuality(paramPost: VegaCoffeeQualityParamPost): LiveData<Resource<GenericReqAndResp<VegaCoffeeQualityParamPostResponse>>>
    suspend fun updateTallySequence(paramPost: SplitUpdateTallySequencePost): LiveData<Resource<GenericReqAndResp<SplitUpdateTallySequencePost>>>
    suspend fun getQualityParams(materialId: String ): LiveData<List<VegaQualityParamsWithQualitative>>

}

class VegaCommonSplitLotRepoImpl(private val api: VegaCommonSplitLotApi, private val dao: VegaNicaraguaGrnDao) : VegaCommonSplitLotRepo{
    override suspend fun getLotQualityDetails(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityPreParameter>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityPreParameter>> =
                api.getLotQualityDetails(currentKey, batchNo, materialId)
        }.build().asLiveData()
    }

    override suspend fun getMaterialQualityGrades(materialCode: String) =
        if (materialCode.isNotEmpty()) dao.getMaterialQualityGrades(materialCode) else dao.getAllMaterialQualityGrades()

    override suspend fun getGrades(materialCode: String) = dao.getQualityGrades(materialCode, "NIPOSITI")

    override suspend fun postSplitDeatils(postData: VegaCommonSplitMainModel): LiveData<Resource<GenericReqAndResp<VegaCommonSplitMainModel>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCommonSplitMainModel>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaCommonSplitMainModel> =
                api.postSplitDeatils(postData)
        }.build().asLiveData()
    }

    override suspend fun postQuality(paramPost: VegaCoffeeQualityParamPost): LiveData<Resource<GenericReqAndResp<VegaCoffeeQualityParamPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeeQualityParamPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaCoffeeQualityParamPostResponse> =
                api.postQuality(paramPost)
        }.build().asLiveData()
    }

    override suspend fun updateTallySequence(paramPost: SplitUpdateTallySequencePost): LiveData<Resource<GenericReqAndResp<SplitUpdateTallySequencePost>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<SplitUpdateTallySequencePost>>() {

            override suspend fun createCall(): GenericReqAndResp<SplitUpdateTallySequencePost> =
                api.updateLotSequence(paramPost)
        }.build().asLiveData()
    }
    override suspend fun getQualityParams(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>> {
        return dao.getQualityParameter(materialId)
    }
}
