package com.olam.warehouse.vegax.containermanagement.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonContainerSize
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonShippingLine
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.containermanagement.data.domain.model.VegaCameroonAddContainerPost
import com.olam.warehouse.vegax.containermanagement.data.domain.model.VegaCameroonAddContainerResponse
import com.olam.warehouse.vegax.containermanagement.data.domain.model.VegaCameroonContainerInventoryModel
import com.olam.warehouse.vegax.containermanagement.data.domain.usecase.VegaCameroonContainerManagementUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaCameroonContainerManagementViewModel(
    private val useCase: VegaCameroonContainerManagementUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    // Start of Add Container

    private var containerSizeSource: LiveData<List<VegaCameroonContainerSize>> = MutableLiveData()
    private val _containerSize = MediatorLiveData<List<VegaCameroonContainerSize>>()
    val containerSize: LiveData<List<VegaCameroonContainerSize>> get() = _containerSize

    private var shippingLineSource: LiveData<List<VegaCameroonShippingLine>> = MutableLiveData()
    private val _shippingLine = MediatorLiveData<List<VegaCameroonShippingLine>>()
    val shippingLine: LiveData<List<VegaCameroonShippingLine>> get() = _shippingLine

    private var containerSource: LiveData<Resource<GenericReqAndResp<VegaCameroonAddContainerResponse>>> =
        MutableLiveData()
    private val _container = MediatorLiveData<Resource<GenericReqAndResp<VegaCameroonAddContainerResponse>>>()
    val container: LiveData<Resource<GenericReqAndResp<VegaCameroonAddContainerResponse>>> get() = _container

    //Start of Container Inventory
    private var inventorySource: LiveData<Resource<GenericReqAndResp<VegaCameroonContainerInventoryModel>>> =
        MutableLiveData()
    private val _inventory = MediatorLiveData<Resource<GenericReqAndResp<VegaCameroonContainerInventoryModel>>>()
    val inventory: LiveData<Resource<GenericReqAndResp<VegaCameroonContainerInventoryModel>>> get() = _inventory


    // Start of Add Container

    fun getContainerSizeList() = viewModelScope.launch(dispatchers.main) {
        _containerSize.removeSource(containerSizeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            containerSizeSource = useCase.getContainerSizeList()
        }
        _containerSize.addSource(containerSizeSource) {
            _containerSize.value = it
        }
    }

    fun getShippingLineList() = viewModelScope.launch(dispatchers.main) {
        _shippingLine.removeSource(shippingLineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            shippingLineSource = useCase.getShippingLineList()
        }
        _shippingLine.addSource(shippingLineSource) {
            _shippingLine.value = it
        }
    }

     fun postContainerData(postData: VegaCameroonAddContainerPost) = viewModelScope.launch(dispatchers.main) {
        _container.removeSource(containerSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            containerSource = useCase.postContainerData(postData)
        }
        _container.addSource(containerSource) {
            _container.value = it
        }
    }
    fun saveContainerData(postData: VegaCameroonAddContainerPost) = viewModelScope.launch(dispatchers.main) {
        _container.removeSource(containerSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            containerSource = useCase.postContainerData(postData)
        }
        _container.addSource(containerSource) {
            _container.value = it
        }
    }

    fun deleteContainerData(postData: VegaCameroonAddContainerPost) = viewModelScope.launch(dispatchers.main) {
        _container.removeSource(containerSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            containerSource = useCase.postContainerData(postData)
        }
        _container.addSource(containerSource) {
            _container.value = it
        }
    }

    //Start of Container Inventory

    fun getContainerInventory(status:String) = viewModelScope.launch(dispatchers.main) {
        _inventory.removeSource(inventorySource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            inventorySource = useCase.getContainerInventory(status)
        }
        _inventory.addSource(inventorySource) {
            _inventory.value = it
        }
    }
}
