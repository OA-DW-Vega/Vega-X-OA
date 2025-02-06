package com.olam.warehouse.vegax.containermanagementnigeria.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.dao.VegaReceivingDao
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonContainerSize
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonShippingLine
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.containermanagementnigeria.data.api.VegaNigeriaContainerManagementApi
import com.olam.warehouse.vegax.containermanagementnigeria.data.domain.model.VegaNigeriaAddContainerPost
import com.olam.warehouse.vegax.containermanagementnigeria.data.domain.model.VegaNigeriaAddContainerResponse
import com.olam.warehouse.vegax.containermanagementnigeria.data.domain.model.VegaNigeriaContainerInventoryModel


/**
 * Created by Roshna Parambil on 11/29/2020.
 */
interface VegaNigeriaContainerManagementRepository {
    suspend fun getContainerSizeList(): LiveData<List<VegaCameroonContainerSize>>
    suspend fun getShippingLineList(): LiveData<List<VegaCameroonShippingLine>>
    suspend fun postContainerData(postData: VegaNigeriaAddContainerPost): LiveData<Resource<GenericReqAndResp<VegaNigeriaAddContainerResponse>>>
    suspend fun getContainerInventory(status:String): LiveData<Resource<GenericReqAndResp<VegaNigeriaContainerInventoryModel>>>
}

class VegaNigeriaContainerManagementRepositoryImpl(
    private val api: VegaNigeriaContainerManagementApi,
    private val dao: VegaReceivingDao
) : VegaNigeriaContainerManagementRepository {

    val werks = PreferenceHelper.get(Constants.WERKS, "")

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getContainerSizeList(): LiveData<List<VegaCameroonContainerSize>> = dao.getContainerSizeList()
    override suspend fun getShippingLineList(): LiveData<List<VegaCameroonShippingLine>> = dao.getShippingLineList()

    override suspend fun postContainerData(postData: VegaNigeriaAddContainerPost): LiveData<Resource<GenericReqAndResp<VegaNigeriaAddContainerResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNigeriaAddContainerResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaNigeriaAddContainerResponse> =
                api.postContainerData(postData)
        }.build().asLiveData()
    }

    override suspend fun getContainerInventory(status:String): LiveData<Resource<GenericReqAndResp<VegaNigeriaContainerInventoryModel>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNigeriaContainerInventoryModel>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaNigeriaContainerInventoryModel> =
                api.getContainerInventory(currentKey,status,werks)
        }.build().asLiveData()
    }


}
