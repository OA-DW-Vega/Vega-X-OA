package com.olam.warehouse.vegax.inventorynicaragua.ui

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
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.VegaNicInventoryAndSyncModel
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.VegaNicInventoryStocks
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.usecase.VegaNicInventoryUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 12/11/2020.
 */
class VegaNicInventoryViewModel(
        private val useCase: VegaNicInventoryUseCase,
        private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var inventoryList = MediatorLiveData<Resource<GenericReqAndResp<VegaNicInventoryAndSyncModel>>>()
    private var inventorySource: LiveData<Resource<GenericReqAndResp<VegaNicInventoryAndSyncModel>>> =
        MutableLiveData()
    val inventoryModelList: LiveData<Resource<GenericReqAndResp<VegaNicInventoryAndSyncModel>>> get() = inventoryList

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaNicInventoryStocks>>>> =
        MutableLiveData()
    private val _qualityDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaNicInventoryStocks>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaNicInventoryStocks>>>> get() = _qualityDetails

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
