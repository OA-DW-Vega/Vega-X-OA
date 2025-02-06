package com.olam.warehouse.vegax.localsalesnigeria.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesWB
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeePendingSalesOrderWithLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeSalesOrderWithLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.model.VegaNigeriaSalesOrderModel
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.model.VegaNigeriaSalesPallet
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.model.VegaNigeriaSalesPostRequest
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.usecase.VegaNigeriaSalesDispatchUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 8/20/2020.
 */
class VegaNigeriaSalesViewModel(
    private val useCase: VegaNigeriaSalesDispatchUseCase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {
    var dispatchWh = VegaCocoaSalesWB()
    var salesOrder = VegaCoffeeSalesOrder()
    var lots: ArrayList<VegaCoffeeSalesLots> = ArrayList()
    val validateLot = MutableLiveData<VegaCoffeeSalesLots>()
    var currentScanLot: String = ""
    var currentTempId: String = ""

    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation
    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = useCase.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
        }
    }

    private var dispatchSalesItemSource: LiveData<VegaCoffeeSalesOrderWithLots> =
        MutableLiveData()
    private val _dispatchSalesItem = MediatorLiveData<VegaCoffeeSalesOrderWithLots>()
    val dispatchSalesItem: LiveData<VegaCoffeeSalesOrderWithLots> get() = _dispatchSalesItem

    private var dispatchSalesOrder: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaSalesOrderModel>>>> =
        MutableLiveData()
    private val _dispatchSalesOrder = MediatorLiveData<Resource<GenericReqAndResp<List<VegaNigeriaSalesOrderModel>>>>()
    val dispatchSalesOrderModel: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaSalesOrderModel>>>> get() = _dispatchSalesOrder

    private var stockListSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>> = MutableLiveData()
    private val _stockList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>>()
    val stockList: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>> get() = _stockList

    private var stocksSourceLocal: LiveData<List<VegaCoffeeSalesLots>> = MutableLiveData()
    private val _stocksLocal = MediatorLiveData<List<VegaCoffeeSalesLots>>()
    val stocksLocal: LiveData<List<VegaCoffeeSalesLots>> get() = _stocksLocal

    private var lotSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>> =
        MutableLiveData()
    private val _lotDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>>()
    val lotDetails: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>> get() = _lotDetails

    private var bagSource: LiveData<List<VegaCoffeeSalesBagMaterial>> = mutableLiveDataOf()
    private val _bagItems = MediatorLiveData<List<VegaCoffeeSalesBagMaterial>>()
    val bagItems: LiveData<List<VegaCoffeeSalesBagMaterial>> get() = _bagItems

    private var palletResource: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaSalesPallet>>>> =
        MutableLiveData()
    private val _pallet = MediatorLiveData<Resource<GenericReqAndResp<List<VegaNigeriaSalesPallet>>>>()
    val pallet: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaSalesPallet>>>> get() = _pallet

    private var dispatchPendingSource: LiveData<List<VegaCoffeePendingSalesOrderWithLots>> = MutableLiveData()
    private val _dispatchPendingSales = MediatorLiveData<List<VegaCoffeePendingSalesOrderWithLots>>()
    val dispatchPendingSales: LiveData<List<VegaCoffeePendingSalesOrderWithLots>> get() = _dispatchPendingSales

    private var deliveryPostSource: LiveData<Resource<GenericReqAndResp<VegaNigeriaSalesPostRequest>>> =
        MutableLiveData()
    private val _deliveryPost = MediatorLiveData<Resource<GenericReqAndResp<VegaNigeriaSalesPostRequest>>>()
    val deliveryPost: LiveData<Resource<GenericReqAndResp<VegaNigeriaSalesPostRequest>>> get() = _deliveryPost

    private var allProductSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf()
    private val _allProduct = MediatorLiveData<List<VegaMaterial>>()
    val allProduct: LiveData<List<VegaMaterial>> get() = _allProduct

    fun getPurchaseOrder() = viewModelScope.launch(dispatchers.main) {
        _dispatchSalesOrder.removeSource(dispatchSalesOrder) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchSalesOrder = useCase.getPurchaseOrder()
        }
        _dispatchSalesOrder.addSource(dispatchSalesOrder) {
            _dispatchSalesOrder.value = it
        }
    }

    fun getDispatchSalesItem(soNumber: String, salesType: String, salesTempId: String) =
        viewModelScope.launch(dispatchers.main) {
        _dispatchSalesItem.removeSource(dispatchSalesItemSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchSalesItemSource = useCase.getDispatchSalesItem(soNumber, salesType, salesTempId)
        }
        _dispatchSalesItem.addSource(dispatchSalesItemSource) {
            _dispatchSalesItem.value = it
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

    fun saveWeighBridgeAndLotDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            /* if (salesOrder.salesTempId.isBlank()) {
                 val randomDouble = "TMP".plus(Random.nextLong().toString())
                 salesOrder.salesTempId = randomDouble
             }*/
            if (salesOrder.saleOrderId.isNotEmpty()) useCase.saveDispatchAndLots(salesOrder, lots)
        }
    }

    fun validateLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateLot.postValue(useCase.validateLot(batchNumber))
        }
    }

    fun getLotDetails(charge: String, materialList: List<String>, whId: String) =
        viewModelScope.launch(dispatchers.main) {
            _lotDetails.removeSource(lotSource)
            withContext(dispatchers.io) {
                lotSource = useCase.getQualityParams(charge, materialList, whId)
            }
            _lotDetails.addSource(lotSource) {
                _lotDetails.value = it
            }
        }

    fun removeLotDetails(lot: VegaCoffeeSalesLots) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.removeLotDetails(lot)
        }
    }

    fun getBagItems(batchNumber: String, materialCode: String, salesTempId: String) = viewModelScope.launch(dispatchers.main) {
        _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSource = useCase.getBagItems(batchNumber, materialCode, salesTempId)
        }
        _bagItems.addSource(bagSource) {
            _bagItems.value = it
        }
    }

    fun getPalletInfo(batchNumber: String, material: String) = viewModelScope.launch(dispatchers.main) {
        _pallet.removeSource(palletResource)
        withContext(dispatchers.io) {
            palletResource = useCase.getPalletDetails(batchNumber, material)
        }
        _pallet.addSource(palletResource) {
            _pallet.value = it
        }
    }

    fun saveBagDetails(bagMaterial: VegaCoffeeSalesBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveBagDetails(bagMaterial)
        }
    }

    fun deleteBagDetails(bagMaterial: VegaCoffeeSalesBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(bagMaterial)
        }
    }

    fun getPendingList(type: String) = viewModelScope.launch(dispatchers.main) {
        _dispatchPendingSales.removeSource(dispatchPendingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchPendingSource = useCase.getPendingListLot(type)
        }
        _dispatchPendingSales.addSource(dispatchPendingSource) {
            _dispatchPendingSales.value = it
        }
    }

    fun updateLotWeightInfo(lot: ArrayList<VegaCoffeeSalesLots>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateLotEditWeight(lot)
        }
    }

    fun postDeliveryDetail(vegaDeliveryPost: VegaNigeriaSalesPostRequest) = viewModelScope.launch(dispatchers.main) {
        _deliveryPost.removeSource(deliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            deliveryPostSource = useCase.postDeliveryDetail(vegaDeliveryPost)
        }
        _deliveryPost.addSource(deliveryPostSource) {
            _deliveryPost.value = it
        }
    }

    fun deleteTempData(salesTempId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteTempData(salesTempId)
        }
    }

    fun getAllProduct() = viewModelScope.launch(dispatchers.main) {
        _allProduct.removeSource(allProductSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            allProductSource = useCase.getAllProducts()
        }
        _allProduct.addSource(allProductSource) {
            _allProduct.value = it
        }
    }
}
