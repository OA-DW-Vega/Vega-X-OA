package com.olam.warehouse.vegax.containermanagement.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.dao.VegaReceivingDao
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonContainerSize
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonShippingLine
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorGrnDao
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.containermanagement.data.api.VegaCameroonContainerManagementApi
import com.olam.warehouse.vegax.containermanagement.data.domain.model.VegaCameroonAddContainerPost
import com.olam.warehouse.vegax.containermanagement.data.domain.model.VegaCameroonAddContainerResponse
import com.olam.warehouse.vegax.containermanagement.data.domain.model.VegaCameroonContainerInventoryModel


/**
 * Created by Roshna Parambil on 11/29/2020.
 */
interface VegaCameroonContainerManagementRepository {
    suspend fun getContainerSizeList(): LiveData<List<VegaCameroonContainerSize>>
    suspend fun getShippingLineList(): LiveData<List<VegaCameroonShippingLine>>
    suspend fun postContainerData(postData: VegaCameroonAddContainerPost): LiveData<Resource<GenericReqAndResp<VegaCameroonAddContainerResponse>>>
    suspend fun getContainerInventory(status:String): LiveData<Resource<GenericReqAndResp<VegaCameroonContainerInventoryModel>>>
}

class VegaCameroonContainerManagementRepositoryImpl(
    private val api: VegaCameroonContainerManagementApi,
    private val dao: VegaReceivingDao
) : VegaCameroonContainerManagementRepository {

    val werks = PreferenceHelper.get(Constants.WERKS, "")

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getContainerSizeList(): LiveData<List<VegaCameroonContainerSize>> = dao.getContainerSizeList()
    override suspend fun getShippingLineList(): LiveData<List<VegaCameroonShippingLine>> = dao.getShippingLineList()

    override suspend fun postContainerData(postData: VegaCameroonAddContainerPost): LiveData<Resource<GenericReqAndResp<VegaCameroonAddContainerResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCameroonAddContainerResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCameroonAddContainerResponse> =
                api.postContainerData(postData)
        }.build().asLiveData()
    }

    override suspend fun getContainerInventory(status:String): LiveData<Resource<GenericReqAndResp<VegaCameroonContainerInventoryModel>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCameroonContainerInventoryModel>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCameroonContainerInventoryModel> =
                api.getContainerInventory(currentKey,status,werks)
        }.build().asLiveData()
    }


}
