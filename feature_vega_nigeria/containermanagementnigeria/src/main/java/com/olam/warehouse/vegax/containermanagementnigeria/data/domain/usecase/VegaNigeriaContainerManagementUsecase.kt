package com.olam.warehouse.vegax.containermanagementnigeria.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.containermanagementnigeria.data.domain.model.VegaNigeriaAddContainerPost
import com.olam.warehouse.vegax.containermanagementnigeria.data.domain.model.VegaNigeriaContainerInventoryModel
import com.olam.warehouse.vegax.containermanagementnigeria.data.repo.VegaNigeriaContainerManagementRepository


/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
class VegaNigeriaContainerManagementUseCase(
    private val repository: VegaNigeriaContainerManagementRepository
) {
    suspend fun getContainerSizeList() = repository.getContainerSizeList()
    suspend fun getShippingLineList() = repository.getShippingLineList()

    suspend fun postContainerData(postData: VegaNigeriaAddContainerPost) = repository.postContainerData(postData)
    suspend fun getContainerInventory(status:String): LiveData<Resource<GenericReqAndResp<VegaNigeriaContainerInventoryModel>>> = repository.getContainerInventory(status)
}
