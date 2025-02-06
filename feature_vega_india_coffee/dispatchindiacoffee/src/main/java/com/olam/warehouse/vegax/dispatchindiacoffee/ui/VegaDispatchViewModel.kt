package com.olam.warehouse.vegax.dispatchindiacoffee.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaDispatchDelivery
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaDispatchTrucks
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.dispatchindiacoffee.data.domain.model.VegaDeliveryPost
import com.olam.warehouse.vegax.dispatchindiacoffee.data.domain.model.VegaDeliveryPostResponse
import com.olam.warehouse.vegax.dispatchindiacoffee.data.domain.model.VegaDispatchLotQuality
import com.olam.warehouse.vegax.dispatchindiacoffee.data.domain.usecase.VegaDispatchUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Keerthi Santhanam on 2/17/2020.
 */
class VegaDispatchViewModel(private val useCase: VegaDispatchUseCase, private val dispatchers: AppDispatchers) :
    BaseViewModel() {

    private var dispatchTruckResource: LiveData<Resource<GenericReqAndResp<List<VegaDispatchTrucks>>>> =
        MutableLiveData()
    private val _trucks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaDispatchTrucks>>>>()
    val trucks: LiveData<Resource<GenericReqAndResp<List<VegaDispatchTrucks>>>> get() = _trucks

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _customLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val customLocation: LiveData<List<VegaCustomStLocation>> get() = _customLocation

    private var stocksSource: LiveData<Resource<List<VegaDispatchLots>>> = MutableLiveData()
    private val _stocks = MediatorLiveData<Resource<List<VegaDispatchLots>>>()
    val stocks: LiveData<Resource<List<VegaDispatchLots>>> get() = _stocks

    private var stocksSourceOffline: LiveData<List<VegaDispatchLots>> = MutableLiveData()
    private val _stocksOffline = MediatorLiveData<List<VegaDispatchLots>>()
    val stocksOffline: LiveData<List<VegaDispatchLots>> get() = _stocksOffline

    private var deliveryPostSource: LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> = MutableLiveData()
    private val _deliveryPost = MediatorLiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>>()
    val deliveryPost: LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> get() = _deliveryPost

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaDispatchLotQuality>>>> =
        MutableLiveData()
    private val _qualityDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaDispatchLotQuality>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaDispatchLotQuality>>>> get() = _qualityDetails

    private var deliverySource: LiveData<Resource<GenericReqAndResp<VegaDispatchDelivery>>> = MutableLiveData()
    private val _delivery = MediatorLiveData<Resource<GenericReqAndResp<VegaDispatchDelivery>>>()
    val delivery: LiveData<Resource<GenericReqAndResp<VegaDispatchDelivery>>> get() = _delivery


    fun getTruckList() = viewModelScope.launch(dispatchers.main) {
        _trucks.removeSource(dispatchTruckResource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchTruckResource = useCase.getTrucks()
        }
        _trucks.addSource(dispatchTruckResource) {
            _trucks.value = it
        }
    }

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _customLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = useCase.getCustomLocations()
        }
        _customLocation.addSource(customLocationSource) {
            _customLocation.value = it
        }
    }

    fun fetchStocks(material: String) = viewModelScope.launch(dispatchers.main) {
        _stocks.removeSource(stocksSource)
        withContext(dispatchers.io) {
            stocksSource = useCase.getStocks(material)
        }
        _stocks.addSource(stocksSource) {
            _stocks.value = it
        }
    }

    fun fetchStocksOffline(material: String) = viewModelScope.launch(dispatchers.main) {
        _stocksOffline.removeSource(stocksSourceOffline)
        withContext(dispatchers.io) {
            stocksSourceOffline = useCase.getStocksOffline(material)
        }
        _stocksOffline.addSource(stocksSourceOffline) {
            _stocksOffline.value = it
        }
    }

    fun postDeliveryDetail(vegaDeliveryPost: VegaDeliveryPost) = viewModelScope.launch(dispatchers.main) {
        _deliveryPost.removeSource(deliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            deliveryPostSource = useCase.postDeliveryDetail(vegaDeliveryPost)
        }
        _deliveryPost.addSource(deliveryPostSource) {
            _deliveryPost.value = it
        }
    }

    fun getQualityParams(charge: String, material: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetails.removeSource(qualityParamSource)
            withContext(dispatchers.io) {
                qualityParamSource = useCase.getQualityParams(charge, material)
            }
            _qualityDetails.addSource(qualityParamSource) {
                _qualityDetails.value = it
            }
        }


    fun getDelivery(delivery: String, deliveryItem: String) = viewModelScope.launch(dispatchers.main) {
        _delivery.removeSource(deliverySource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            deliverySource = useCase.getDelivery(delivery, deliveryItem)
        }
        _delivery.addSource(deliverySource) {
            _delivery.value = it
        }
    }

    fun saveDispatchAndLots(
        dispatchData: VegaDispatchTrucks,
        dispatchLotsList: MutableList<VegaDispatchLots>
    ) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveDispatchAndLots(dispatchData, dispatchLotsList)
        }
    }

    fun updateLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateLot(batchNumber)
        }
    }

    fun updateDispatchStatus(
        weighBridgeId: String,
        syncStatus: Boolean,
        status: Int,
        msg: String?,
        batch: String
    ) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                useCase.updateDispatchStatus(weighBridgeId, syncStatus, status, msg, batch)
            }
        }
}
