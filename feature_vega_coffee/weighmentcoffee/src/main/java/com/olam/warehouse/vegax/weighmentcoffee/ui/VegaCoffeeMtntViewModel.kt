package com.olam.warehouse.vegax.weighmentcoffee.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.*
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.weighmentcoffee.data.domain.usecase.VegaCoffeeMtntUseCase
import com.olam.warehouse.vegax.weighmentcoffee.data.domain.usecase.VegaCoffeeSalesPostRequest
import com.olam.warehouse.vegax.weighmentcoffee.utils.getLineItemFromMtnt
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class VegaCoffeeMtntViewModel(
    private val useCase: VegaCoffeeMtntUseCase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {

    private var weightBridgeOutSource: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> = MutableLiveData()
    private var _weighBridgeTruckOut = MediatorLiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>()
    val weighBridgeTruckOut: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> get() = _weighBridgeTruckOut


    private var weighBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaMtnt>>>> = MutableLiveData()
    private val _weighBridge = MediatorLiveData<Resource<GenericReqAndResp<List<VegaMtnt>>>>()
    val weighBridge: LiveData<Resource<GenericReqAndResp<List<VegaMtnt>>>> get() = _weighBridge

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val suppplier: LiveData<List<VegaVendor>> get() = _supplier

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var mtntSource: LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>> = MutableLiveData()
    private val _mtnt = MediatorLiveData<Resource<GenericReqAndResp<VegaMtntResponse>>>()
    val mtnt: LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>> get() = _mtnt

    private var dataSource: LiveData<Resource<GenericReqAndResp<Data>>> = MutableLiveData()
    private val _data = MediatorLiveData<Resource<GenericReqAndResp<Data>>>()
    val data: LiveData<Resource<GenericReqAndResp<Data>>> get() = _data

    private var purchaseSource: LiveData<Resource<GenericReqAndResp<List<VegaPurchaseOrder>>>> = MutableLiveData()
    private val _purchaseOrder = MediatorLiveData<Resource<GenericReqAndResp<List<VegaPurchaseOrder>>>>()
    val purchaseOrder: LiveData<Resource<GenericReqAndResp<List<VegaPurchaseOrder>>>> get() = _purchaseOrder

    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    val material: LiveData<List<VegaPackageMaterial>> get() = _material
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()

    private var truckInWeightBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> =
        MutableLiveData()
    private var _truckInWeighBridge = MediatorLiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>()
    val truckInWeighBridge: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> get() = _truckInWeighBridge

    private var deliveryPostSource: LiveData<Resource<GenericReqAndResp<VegaCoffeeSalesPostRequest>>> =
        MutableLiveData()
    private val _deliveryPost = MediatorLiveData<Resource<GenericReqAndResp<VegaCoffeeSalesPostRequest>>>()
    val deliveryPost: LiveData<Resource<GenericReqAndResp<VegaCoffeeSalesPostRequest>>> get() = _deliveryPost

    private var qualityPreSamplingSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> =
        MutableLiveData()
    private val _preSamplingQuality = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>()
    val preSamplingQuality: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> get() = _preSamplingQuality

    private var weighBridgeIdSource: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> =
        MutableLiveData()
    private var _weighBridgeId = MediatorLiveData<Resource<GenericReqAndResp<VegaReceiving>>>()
    val weighBridgeId: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> get() = _weighBridgeId

    private var receiveSource: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> = MutableLiveData()
    private val _receive = MediatorLiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>()
    val receive: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> get() = _receive

    fun getTruckInWeighBridgeDetail() = viewModelScope.launch(dispatchers.main) {
        _truckInWeighBridge.removeSource(truckInWeightBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            truckInWeightBridgeSource = useCase.getTruckInWeighBridgeDetailOnline()
        }
        _truckInWeighBridge.addSource(truckInWeightBridgeSource) {
            _truckInWeighBridge.value = it
        }
    }

    fun getWeighBridgeDetail(isSales: Boolean,isThirdParty: Boolean) = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weighBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeSource = useCase.getWeighBridgeDetail(isSales,isThirdParty)
        }
        _weighBridge.addSource(weighBridgeSource) {
            _weighBridge.value = it
        }
    }

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = useCase.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
        }
    }

    fun getSuppliers() = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCase.getSupplier()
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
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

    fun postMtntData(
        mtntData: VegaMtntPost
    ) = viewModelScope.launch(dispatchers.main) {
        _mtnt.removeSource(mtntSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            mtntSource = useCase.postMtntData(mtntData)
        }
        _mtnt.addSource(mtntSource) {
            _mtnt.value = it
        }
    }

    fun postThirdPartyData(mtntData: VegaMtntPost)  = viewModelScope.launch(dispatchers.main) {
        _data.removeSource(dataSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dataSource = useCase.postThirdPartyData(mtntData)
        }
        _data.addSource(dataSource) {
            _data.value = it
        }
    }

    fun saveMtnt(mtntData: VegaMtnt) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveMtnt(mtntData)
        }
    }

    fun saveMtntLineItems(postData: MutableList<VegaMtnt>) = viewModelScope.launch(dispatchers.main) {
        val lineItems = getLineItemFromMtnt(postData)
        withContext(dispatchers.io) {
            useCase.saveMtntLineItems(lineItems)
        }
    }

    fun getPurchaseOrder() = viewModelScope.launch(dispatchers.main) {
        _purchaseOrder.removeSource(purchaseSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            purchaseSource = useCase.getPurchaseOrder()
        }
        _purchaseOrder.addSource(purchaseSource) {
            _purchaseOrder.value = it
        }
    }

    fun getMaterials() = viewModelScope.launch(dispatchers.main) {
        _material.removeSource(materialSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            materialSource = useCase.getMaterials()
        }
        _material.addSource(materialSource) {
            _material.value = it
        }
    }

    fun getWeighBridgeTruckOutDetail() = viewModelScope.launch(dispatchers.main) {
        _weighBridgeTruckOut.removeSource(weightBridgeOutSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weightBridgeOutSource = useCase.getWeighBridgeDetailOnline()
        }
        _weighBridgeTruckOut.addSource(weightBridgeOutSource) {
            _weighBridgeTruckOut.value = it
        }
    }

    /*fun getGateEntryDetails(commonPrimaryId: String) = viewModelScope.launch(dispatchers.main) {
        _gateEntryLocal.removeSource(gateLocalEntrySource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            gateLocalEntrySource = useCase.getGateEntryDetails(commonPrimaryId)
        }
        _gateEntryLocal.addSource(gateLocalEntrySource) {
            _gateEntryLocal.value = it
        }
    }*/

    fun postSalesTruckOutDetail(vegaDeliveryPost: VegaCoffeeSalesPostRequest, isThirdParty: Boolean) =
        viewModelScope.launch(dispatchers.main) {
            _deliveryPost.removeSource(deliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                deliveryPostSource = useCase.postSalesTruckOutDetail(vegaDeliveryPost, isThirdParty)
            }
            _deliveryPost.addSource(deliveryPostSource) {
                _deliveryPost.value = it
            }
        }


    fun postThirdPartyReceivingData(
        receivingData: VegaReceivingPost
    ) = viewModelScope.launch(dispatchers.main) {
        _receive.removeSource(receiveSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            receiveSource = useCase.postReceivingDetail(receivingData)
        }
        _receive.addSource(receiveSource) {
            _receive.value = it
        }
    }

    fun getPreSamplingQualitydata(batchNo: String, materialId: String) = viewModelScope.launch(dispatchers.main) {
        _preSamplingQuality.removeSource(qualityPreSamplingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityPreSamplingSource = useCase.getPreSamplingQualityList(batchNo, materialId)
        }
        _preSamplingQuality.addSource(qualityPreSamplingSource) {
            _preSamplingQuality.value = it
        }
    }

    fun getWeighBridgeIdDetail(wbid: String) = viewModelScope.launch(dispatchers.main) {
        _weighBridgeId.removeSource(weighBridgeIdSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeIdSource = useCase.getWeighBridgeIdDetail(wbid)
        }
        _weighBridgeId.addSource(weighBridgeIdSource) {
            _weighBridgeId.value = it
        }
    }
}
