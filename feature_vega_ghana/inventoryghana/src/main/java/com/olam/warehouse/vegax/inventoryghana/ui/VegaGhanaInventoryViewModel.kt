package com.olam.warehouse.vegax.inventoryghana.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.inventoryghana.data.domain.model.VegaInventoryAndSyncModel
import com.olam.warehouse.vegax.inventoryghana.data.domain.model.VegaGhanaInventoryStocks
import com.olam.warehouse.vegax.inventoryghana.data.domain.usecase.VegaGhanaInventoryUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
class VegaGhanaInventoryViewModel(
    private val useCase: VegaGhanaInventoryUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var inventoryList = MediatorLiveData<Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>>()
    private var inventorySource: LiveData<Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>> =
        MutableLiveData()
    val inventoryModelList: LiveData<Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>> get() = inventoryList

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaGhanaInventoryStocks>>>> =
        MutableLiveData()
    private val _qualityDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaGhanaInventoryStocks>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaGhanaInventoryStocks>>>> get() = _qualityDetails


    fun getInventoryList() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            inventorySource = useCase.getInventoryList()
        }
        inventoryList.addSource(inventorySource) {
            inventoryList.value = it
        }
    }

    fun getProducts() = viewModelScope.launch(dispatchers.main) {
        _product.removeSource(productSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            productSource = useCase.getProducts()
        }
        _product.addSource(productSource) {
            _product.value = it
        }
    }

    fun getConfigItems(role: String) = viewModelScope.launch(dispatchers.main) {
        _configItems.removeSource(configSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            configSource = useCase.getConfigItems(role)
        }
        _configItems.addSource(configSource) {
            _configItems.value = it
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
