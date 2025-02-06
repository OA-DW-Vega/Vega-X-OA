package com.olam.warehouse.vegax.dummyquality.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.vegax.dummyquality.data.api.VegaCommonDummyQualityApi
import com.olam.warehouse.master.common.dao.VegaDummyQualityDao
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.dummyquality.data.domain.model.*

interface VegaCommonDummyQualityRepository {

    suspend fun getSupplierList(): LiveData<List<VegaVendor>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getQualityParams(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>>
    suspend fun postDummySample(paramPost: VegaCommonDummySampleModel): LiveData<Resource<GenericMessage>>
    suspend fun getDummySample(key: String,plantId:String,materialCode:String,vendorCode:String,dummySampleFlag:Boolean
    ): LiveData<Resource<GenericReqAndResp<ResponseDummySample>>>

    suspend fun getDummySampleList(key: String,plantId:String,materialCode:String,vendorCode:String,dummySampleFlag:Boolean
    ): LiveData<Resource<GenericReqAndResp<List<ResponseDummySampleList>>>>

    suspend fun getDummySampleDetails(key: String,id:String
    ): LiveData<Resource<GenericReqAndResp<ResponseDummySampleList>>>


    suspend fun deleteDummySample(id: String
    ): LiveData<Resource<GenericReqAndResp<List<ResponseDummySampleList>>>>

    suspend fun getVendorAndMaterial(plantId: String
    ): LiveData<Resource<GenericReqAndResp<List<ResponseVendorAndMaterial>>>>
}

class VegaCommonDummyQualityRepositoryImp(private val api: VegaCommonDummyQualityApi,private val dao: VegaDummyQualityDao
) : VegaCommonDummyQualityRepository {

    override suspend fun getSupplierList(): LiveData<List<VegaVendor>> = dao.getSupplierList()

    override suspend fun getProducts(): LiveData<List<VegaMaterial>> = dao.getProducts()

    override suspend fun getQualityParams(materialId: String): LiveData<List<VegaQualityParamsWithQualitative>> = dao.getQualityParameter(materialId)

    override suspend fun postDummySample(paramPost: VegaCommonDummySampleModel): LiveData<Resource<GenericMessage>> {
        return object : NetworkOnlyBoundResource<GenericMessage>() {
            override suspend fun createCall(): GenericMessage =
                api.postDummySample(paramPost)
        }.build().asLiveData()
    }

    override suspend fun getDummySample(
        key: String,
        plantId: String,
        materialCode: String,
        vendorCode: String,
        dummySampleFlag:Boolean
    ): LiveData<Resource<GenericReqAndResp<ResponseDummySample>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<ResponseDummySample>>() {
            override suspend fun createCall(): GenericReqAndResp<ResponseDummySample> =
                api.getDummySample(key,plantId,materialCode,vendorCode,dummySampleFlag)
        }.build().asLiveData()
    }

    override suspend fun getDummySampleList(
        key: String,
        plantId: String,
        materialCode: String,
        vendorCode: String,
        dummySampleFlag:Boolean
    ): LiveData<Resource<GenericReqAndResp<List<ResponseDummySampleList>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<ResponseDummySampleList>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<ResponseDummySampleList>> =
                api.getDummySampleList(key,plantId,materialCode,vendorCode,dummySampleFlag)
        }.build().asLiveData()
    }

    override suspend fun getDummySampleDetails(
        key: String,
        id: String
    ): LiveData<Resource<GenericReqAndResp<ResponseDummySampleList>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<ResponseDummySampleList>>() {
            override suspend fun createCall(): GenericReqAndResp<ResponseDummySampleList> =
                api.getDummySampleDetails(key,id)
        }.build().asLiveData()
    }

    override suspend fun deleteDummySample(id: String): LiveData<Resource<GenericReqAndResp<List<ResponseDummySampleList>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<ResponseDummySampleList>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<ResponseDummySampleList>> =
                api.deleteDummySample(id)
        }.build().asLiveData()
    }

    override suspend fun getVendorAndMaterial(plantId: String): LiveData<Resource<GenericReqAndResp<List<ResponseVendorAndMaterial>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<ResponseVendorAndMaterial>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<ResponseVendorAndMaterial>> =
                api.getVendorAndMaterial(plantId)
        }.build().asLiveData()
    }
}
