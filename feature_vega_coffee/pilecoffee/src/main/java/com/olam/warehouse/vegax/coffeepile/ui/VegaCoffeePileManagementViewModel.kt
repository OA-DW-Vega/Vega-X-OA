package com.olam.warehouse.vegax.coffeepile.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.coffeepile.ui.data.domain.VegaCoffeePileManagementUseCase
import com.olam.warehouse.vegax.coffeepile.ui.data.domain.model.VegaCoffeePilePostRequest
import com.olam.warehouse.vegax.coffeepile.ui.data.domain.model.VegaCoffeePileResponse
import com.olam.warehouse.vegax.coffeepile.ui.data.domain.model.VegaPileSelectionModel
import com.olam.warehouse.vegax.coffeepile.ui.data.domain.model.VegaPileSequence
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class VegaCoffeePileManagementViewModel(
    private val useCase: VegaCoffeePileManagementUseCase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {

    var lotlist = VegaCocoaDispatchLots()
    var lots: ArrayList<VegaCocoaDispatchLots> = ArrayList()
    var model = VegaCoffeeThirdPartyRequestModel()
    var currentScanLot: String = ""
    var salesOrder = VegaCoffeeSalesOrder()
    val validateLot = MutableLiveData<VegaCocoaDispatchLots>()
    var lotsList = ArrayList<VegaCocoaDispatchLots>()


    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val supplier: LiveData<List<VegaVendor>> get() = _supplier

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var stockListSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> = MutableLiveData()
    private val _stockList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val stockList: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _stockList

    private var createPileSource: LiveData<Resource<GenericReqAndResp<VegaPileSequence>>> = MutableLiveData()
    private val _createPile = MediatorLiveData<Resource<GenericReqAndResp<VegaPileSequence>>>()
    val createPile: LiveData<Resource<GenericReqAndResp<VegaPileSequence>>> get() = _createPile

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> =
        MutableLiveData()
    private val _qualityDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _qualityDetails

    private var postPileSource: LiveData<Resource<GenericReqAndResp<VegaCoffeePileResponse>>> = MutableLiveData()
    private val _postPile = MediatorLiveData<Resource<GenericReqAndResp<VegaCoffeePileResponse>>>()
    val postPile: LiveData<Resource<GenericReqAndResp<VegaCoffeePileResponse>>> get() = _postPile

    fun getPostPile(postPileRequest: VegaCoffeePilePostRequest) = viewModelScope.launch(dispatchers.main) {
        _postPile.removeSource(postPileSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            postPileSource = useCase.getPostPile(postPileRequest)
        }
        _postPile.addSource(postPileSource) {
            _postPile.value = it
        }
    }


    fun getCreatePile(isPost: Boolean) = viewModelScope.launch(dispatchers.main) {
        _createPile.removeSource(createPileSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            createPileSource = useCase.getCreatePile(isPost)
        }
        _createPile.addSource(createPileSource) {
            _createPile.value = it
        }
    }

    fun addLoTInDB(lot: List<VegaCocoaDispatchLots>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertLotList(lot)
        }
    }

    fun validateLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateLot.postValue(useCase.validateLot(batchNumber))
        }
    }

    fun getLotDetails(charge: String, material: List<String>, whId: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetails.removeSource(qualityParamSource)
            withContext(dispatchers.io) {
                qualityParamSource = useCase.getQualityParams(charge, material, whId)
            }
            _qualityDetails.addSource(qualityParamSource) {
                _qualityDetails.value = it
            }
        }


    private var stockPileSource: LiveData<Resource<GenericReqAndResp<List<VegaPileSelectionModel>>>> = MutableLiveData()
    private val _stockPile = MediatorLiveData<Resource<GenericReqAndResp<List<VegaPileSelectionModel>>>>()
    val stockPile: LiveData<Resource<GenericReqAndResp<List<VegaPileSelectionModel>>>> get() = _stockPile

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = useCase.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
        }
    }

    fun getStockPiles(materialList: String) = viewModelScope.launch(dispatchers.main) {
        _stockPile.removeSource(stockPileSource)
        withContext(dispatchers.io) {
            stockPileSource = useCase.getStockPile(materialList)
        }
        _stockPile.addSource(stockPileSource) {
            _stockPile.value = it
        }
    }


    fun getStockList(materialList: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        _stockList.removeSource(stockListSource)
        withContext(dispatchers.io) {
            stockListSource = useCase.getStockList(materialList)
        }
        _stockList.addSource(stockListSource) {
            _stockList.value = it
        }
    }

    fun getSuppliers() = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCase.getSuppliers()
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

    fun removeLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.removeLot(batchNumber)
        }
    }

    fun saveThirdPartyInfo() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertThirdPartyModel(model)
            if (lots.isNotEmpty())
                useCase.insertLotList(lots)
        }
    }
}
