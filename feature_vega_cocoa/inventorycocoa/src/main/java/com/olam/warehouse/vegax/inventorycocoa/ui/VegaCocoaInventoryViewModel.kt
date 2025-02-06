package com.olam.warehouse.vegax.inventorycocoa.ui

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
import com.olam.warehouse.vegax.inventorycocoa.data.domain.model.MaterialQuality
import com.olam.warehouse.vegax.inventorycocoa.data.domain.model.VegaCocoaInventoryLotHead
import com.olam.warehouse.vegax.inventorycocoa.data.domain.model.VegaCocoaInventoryLots
import com.olam.warehouse.vegax.inventorycocoa.data.domain.usecase.VegaCocoaInventoryUsecase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 5/21/2020.
 */
class VegaCocoaInventoryViewModel(private val useCase: VegaCocoaInventoryUsecase,
    private val dispatchers: AppDispatchers) :
    BaseViewModel() {

    private var scanLotSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaInventoryLots>>>> = MutableLiveData()
    private val _scanLots = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaInventoryLots>>>>()
    val scanLot: LiveData<Resource<GenericReqAndResp<List<VegaCocoaInventoryLots>>>> get() = _scanLots

    private var lotListSource: LiveData<Resource<GenericReqAndResp<VegaCocoaInventoryLotHead>>> = MutableLiveData()
    private val _lotList = MediatorLiveData<Resource<GenericReqAndResp<VegaCocoaInventoryLotHead>>>()
    val lotList: LiveData<Resource<GenericReqAndResp<VegaCocoaInventoryLotHead>>> get() = _lotList

    private var qualitySource: LiveData<Resource<GenericReqAndResp<List<MaterialQuality>>>> = MutableLiveData()
    private val _qualityDetails = MediatorLiveData<Resource<GenericReqAndResp<List<MaterialQuality>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<MaterialQuality>>>> get() = _qualityDetails

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    fun getScanLotDetail(lotId: String) = viewModelScope.launch(dispatchers.main) {
        _scanLots.removeSource(scanLotSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            scanLotSource = useCase.getScanLotDetail(lotId)
        }
        _scanLots.addSource(scanLotSource) {
            _scanLots.value = it
        }
    }

    fun getInventoryList() = viewModelScope.launch(dispatchers.main) {
        _lotList.removeSource(lotListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            lotListSource = useCase.getInventoryList()
        }
        _lotList.addSource(lotListSource) {
            _lotList.value = it
        }
    }

    fun fetchQualityDetails(charge: String, material: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetails.removeSource(qualitySource)
            withContext(dispatchers.io) {
                qualitySource = useCase.fetchQualityDetails(charge, material)
            }
            _qualityDetails.addSource(qualitySource) {
                _qualityDetails.value = it
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

}
