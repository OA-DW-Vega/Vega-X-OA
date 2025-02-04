package com.olam.warehouse.vegax.inventorynigeria.ui

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
import com.olam.warehouse.vegax.inventorynigeria.data.domain.model.MaterialQuality
import com.olam.warehouse.vegax.inventorynigeria.data.domain.model.VegaCocoaInventoryStocks
import com.olam.warehouse.vegax.inventorynigeria.data.domain.model.VegaInventoryAndSyncModel
import com.olam.warehouse.vegax.inventorynigeria.data.domain.model.VegaNigeriaInventoryLots
import com.olam.warehouse.vegax.inventorynigeria.data.domain.usecase.VegaNigeriaInventoryUsecase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Roshna Parambil on 9/3/2020.
 */
class VegaNigeriaInventoryViewModel(private val useCase: VegaNigeriaInventoryUsecase,
                                    private val dispatchers: AppDispatchers) :
    BaseViewModel() {

    private var scanLotSource: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaInventoryLots>>>> = MutableLiveData()
    private val _scanLots = MediatorLiveData<Resource<GenericReqAndResp<List<VegaNigeriaInventoryLots>>>>()
    val scanLot: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaInventoryLots>>>> get() = _scanLots

    private var qualitySource: LiveData<Resource<GenericReqAndResp<List<MaterialQuality>>>> = MutableLiveData()
    private val _qualityDetails = MediatorLiveData<Resource<GenericReqAndResp<List<MaterialQuality>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<MaterialQuality>>>> get() = _qualityDetails

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaInventoryStocks>>>> =
        MutableLiveData()
    private val _qualityParamDetails =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaInventoryStocks>>>>()
    val qualityParamDetails: LiveData<Resource<GenericReqAndResp<List<VegaCocoaInventoryStocks>>>> get() = _qualityParamDetails

    private var inventoryList =
        MediatorLiveData<Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>>()
    private var inventorySource: LiveData<Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>> =
        MutableLiveData()
    val inventoryModelList: LiveData<Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>> get() = inventoryList

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
        withContext(dispatchers.io) {
            inventorySource = useCase.getInventoryList()
        }
        inventoryList.addSource(inventorySource) {
            inventoryList.value = it
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

    fun getLotDetails(charge: String, material: String, whId: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityParamDetails.removeSource(qualityParamSource)
            withContext(dispatchers.io) {
                qualityParamSource = useCase.getQualityParams(charge, material, whId)
            }

            _qualityParamDetails.addSource(qualityParamSource) {
                _qualityParamDetails.value = it
            }
        }

}
