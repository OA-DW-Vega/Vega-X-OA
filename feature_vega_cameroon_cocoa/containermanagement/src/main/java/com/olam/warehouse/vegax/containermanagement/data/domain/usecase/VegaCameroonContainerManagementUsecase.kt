package com.olam.warehouse.vegax.containermanagement.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.containermanagement.data.domain.model.VegaCameroonAddContainerPost
import com.olam.warehouse.vegax.containermanagement.data.domain.model.VegaCameroonContainerInventoryModel
import com.olam.warehouse.vegax.containermanagement.data.repo.VegaCameroonContainerManagementRepository


/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
class VegaCameroonContainerManagementUseCase(
    private val repository: VegaCameroonContainerManagementRepository
) {
    suspend fun getContainerSizeList() = repository.getContainerSizeList()
    suspend fun getShippingLineList() = repository.getShippingLineList()

    suspend fun postContainerData(postData: VegaCameroonAddContainerPost) = repository.postContainerData(postData)
    suspend fun getContainerInventory(status:String): LiveData<Resource<GenericReqAndResp<VegaCameroonContainerInventoryModel>>> = repository.getContainerInventory(status)
}
