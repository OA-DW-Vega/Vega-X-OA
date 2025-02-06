package com.olam.warehouse.vegax.receivingghanacash.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.dao.VegaMtntDao
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.receivingghanacash.data.api.VegaReceivingGhanaMtntApi
import com.olam.warehouse.vegax.receivingghanacash.data.domain.model.VegaReceivingGhanaMtntPost
import com.olam.warehouse.vegax.receivingghanacash.data.domain.model.VegaReceivingGhanaMtntResponse

/**
 * Created by Baskaran Kannan on 2/14/2020.
 */
interface VegaReceivingGhanaMtntRepository {

    suspend fun getWeighBridgeDetail(): LiveData<Resource<GenericReqAndResp<List<VegaMtnt>>>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun postMtntData(mtntData: VegaReceivingGhanaMtntPost): LiveData<Resource<GenericReqAndResp<VegaReceivingGhanaMtntResponse>>>
    suspend fun saveMtnt(mtntData: VegaMtnt)
    suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaPurchaseOrder>>>>
    suspend fun saveMtntLineItems(lineItems: List<VegaMtntLineItem>)
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>>
}

class VegaReceivingGhanaMtntRepositoryImpl(private val api: VegaReceivingGhanaMtntApi, private val dao: VegaMtntDao) : VegaReceivingGhanaMtntRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")


    override suspend fun getWeighBridgeDetail(): LiveData<Resource<GenericReqAndResp<List<VegaMtnt>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaMtnt>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaMtnt>> =
                api.getWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getCustomLocations() = dao.getCustomLocations()
    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getProducts() = dao.getProducts()

    override suspend fun postMtntData(mtntData: VegaReceivingGhanaMtntPost): LiveData<Resource<GenericReqAndResp<VegaReceivingGhanaMtntResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingGhanaMtntResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingGhanaMtntResponse> =
                api.postMtntData(mtntData)
        }.build().asLiveData()
    }

    override suspend fun saveMtnt(mtntData: VegaMtnt) = dao.insertMtnt(mtntData)

    override suspend fun saveMtntLineItems(lineItems: List<VegaMtntLineItem>) =
        dao.saveMtntLineItems(lineItems)

    override suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaPurchaseOrder>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaPurchaseOrder>> =
                api.getPurchaseOrder(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getMaterials() = dao.getMaterials()
}
