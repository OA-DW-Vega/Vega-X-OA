package com.olam.warehouse.vegax.inventorycoffee.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.inventorycoffee.data.domain.model.VegaCoffeInventoryStocks
import com.olam.warehouse.vegax.inventorycoffee.data.domain.model.VegaCoffeeInventoryAndSyncModel
import com.olam.warehouse.vegax.inventorycoffee.data.domain.usecase.VegaCoffeeInventoryUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaCoffeeInventoryViewModel(
    private val useCase: VegaCoffeeInventoryUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var inventoryList = MediatorLiveData<Resource<GenericReqAndResp<VegaCoffeeInventoryAndSyncModel>>>()
    private var inventorySource: LiveData<Resource<GenericReqAndResp<VegaCoffeeInventoryAndSyncModel>>> =
        MutableLiveData()
    val inventoryModelList: LiveData<Resource<GenericReqAndResp<VegaCoffeeInventoryAndSyncModel>>> get() = inventoryList

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeInventoryStocks>>>> =
        MutableLiveData()
    private val _qualityDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeInventoryStocks>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaCoffeInventoryStocks>>>> get() = _qualityDetails

    fun getInventoryList() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            inventorySource = useCase.getCoffeeInventoryList()
        }
        inventoryList.addSource(inventorySource) {
            inventoryList.value = it
        }
    }

    fun getProducts() = viewModelScope.launch(dispatchers.main) {
        _product.removeSource(productSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            productSource = useCase.getCoffeeProducts()
        }
        _product.addSource(productSource) {
            _product.value = it
        }
    }

    fun getLotDetails(charge: String, material: String, whId: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetails.removeSource(qualityParamSource)
            withContext(dispatchers.io) {
                qualityParamSource = useCase.getQualityParams(charge, material, whId)
            }
            _qualityDetails.addSource(qualityParamSource) {
                _qualityDetails.value = it
            }
        }
}
