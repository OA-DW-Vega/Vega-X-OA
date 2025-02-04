package com.olam.warehouse.vegax.mtntghana.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchPurchaseOrders
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDeliveryPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDeliveryPostResponse
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchWithLineItems
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.mtntghana.data.domain.usecase.VegaGhanaMtntUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaGhanaMtntViewModel(
    private val useCase: VegaGhanaMtntUseCase,
    private val dispatchers: AppDispatchers
):BaseViewModel() {
    private var purchaseSource: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchPurchaseOrder>>>> =
        MutableLiveData()
    private val _purchaseOrder = MediatorLiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchPurchaseOrder>>>>()
    val purchaseOrder: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchPurchaseOrder>>>> get() = _purchaseOrder

    private var purchaseSourceLocal: LiveData<List<VegaCocoaPurchaseOrders>> = MutableLiveData()
    private val _purchaseOrderLocal = MediatorLiveData<List<VegaCocoaPurchaseOrders>>()
    val purchaseOrderLocal: LiveData<List<VegaCocoaPurchaseOrders>> get() = _purchaseOrderLocal

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf()
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var deliveryPostSource: LiveData<Resource<GenericReqAndResp<VegaEcuadorDeliveryPostResponse>>> =
        MutableLiveData()
    private val _deliveryPost = MediatorLiveData<Resource<GenericReqAndResp<VegaEcuadorDeliveryPostResponse>>>()
    val deliveryPost: LiveData<Resource<GenericReqAndResp<VegaEcuadorDeliveryPostResponse>>> get() = _deliveryPost

    private var dispatchLotsSource: LiveData<List<VegaEcuadorDispatchLots>> = MutableLiveData()
    private val _dispatchLots = MediatorLiveData<List<VegaEcuadorDispatchLots>>()
    val dispatchLots: LiveData<List<VegaEcuadorDispatchLots>> get() = _dispatchLots

    private var vegaEcuadorDispatchSource: LiveData<List<VegaEcuadorDispatch>> = MutableLiveData()
    private val _vegaEcuadorDispatchSource = MediatorLiveData<List<VegaEcuadorDispatch>>()
    val vegaEcuadorDispatch: LiveData<List<VegaEcuadorDispatch>> get() = _vegaEcuadorDispatchSource

    private var lotDetailsSource: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>> =
        MutableLiveData()
    private val _lotDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>>()
    val lotDetails: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>> get() = _lotDetails

    private var lotDetailsSourceLocal: LiveData<List<VegaEcuadorDispatchStocks>> = MutableLiveData()
    private val _lotDetailsLocal = MediatorLiveData<List<VegaEcuadorDispatchStocks>>()
    val lotDetailsLocal: LiveData<List<VegaEcuadorDispatchStocks>> get() = _lotDetailsLocal

    private var stocksSource: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>> = MutableLiveData()
    private val _stocks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>>()
    val stocks: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>> get() = _stocks

    private var stocksSourceLocal: LiveData<List<VegaEcuadorDispatchStocks>> = MutableLiveData()
    private val _stocksLocal = MediatorLiveData<List<VegaEcuadorDispatchStocks>>()
    val stocksLocal: LiveData<List<VegaEcuadorDispatchStocks>> get() = _stocksLocal

    private var dispatchSource: LiveData<List<VegaEcuadorDispatchWithLineItems>> = MutableLiveData()
    private val _dispatchItemLocal = MediatorLiveData<List<VegaEcuadorDispatchWithLineItems>>()
    val dispatchItemLocal: LiveData<List<VegaEcuadorDispatchWithLineItems>> get() = _dispatchItemLocal

    private var dispatchCountSource: LiveData<List<VegaEcuadorDispatchWithLineItems>> = MutableLiveData()
    private val _dispatchItemCountLocal = MediatorLiveData<List<VegaEcuadorDispatchWithLineItems>>()
    val dispatchItemCountLocal: LiveData<List<VegaEcuadorDispatchWithLineItems>> get() = _dispatchItemCountLocal

    val validateLot = MutableLiveData<VegaEcuadorDispatchLots>()

    fun postDeliveryDetail(vegaDeliveryPost: VegaEcuadorDeliveryPost) = viewModelScope.launch(dispatchers.main) {
        _deliveryPost.removeSource(deliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            deliveryPostSource = useCase.postDeliveryDetail(vegaDeliveryPost)
        }
        _deliveryPost.addSource(deliveryPostSource) {
            _deliveryPost.value = it
        }
    }

    fun getPurchaseOrder(plantId: String) = viewModelScope.launch(dispatchers.main) {
        _purchaseOrder.removeSource(purchaseSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            purchaseSource = useCase.getPurchaseOrder(plantId)
        }
        _purchaseOrder.addSource(purchaseSource) {
            _purchaseOrder.value = it
        }
    }

    fun getPurchaseOrderLocal() = viewModelScope.launch(dispatchers.main) {
        _purchaseOrderLocal.removeSource(purchaseSourceLocal) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            purchaseSourceLocal = useCase.getPurchaseOrderLocal()
        }
        _purchaseOrderLocal.addSource(purchaseSourceLocal) {
            _purchaseOrderLocal.value = it
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

    fun getLotDetails(charge: String, materialList: ArrayList<String>, whId: String) =
        viewModelScope.launch(dispatchers.main) {
            _lotDetails.removeSource(lotDetailsSource)
            withContext(dispatchers.io) {
                lotDetailsSource = useCase.getLotDetails(charge, materialList, whId)
            }
            _lotDetails.addSource(lotDetailsSource) {
                _lotDetails.value = it
            }
        }

    fun getLotDetailsLocal(charge: String) =
        viewModelScope.launch(dispatchers.main) {
            _lotDetailsLocal.removeSource(lotDetailsSourceLocal)
            withContext(dispatchers.io) {
                lotDetailsSourceLocal = useCase.getLotDetailsLocal(charge)
            }
            _lotDetailsLocal.addSource(lotDetailsSourceLocal) {
                _lotDetailsLocal.value = it
            }
        }

    fun validateLot(batchNumber: String, tmpId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateLot.postValue(useCase.validateLot(batchNumber, tmpId))
        }
    }

    fun updateRemarks(remark: String, whId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateRemark(remark, whId)
        }
    }

    fun fetchStocks(materialList: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        _stocks.removeSource(stocksSource)
        withContext(dispatchers.io) {
            stocksSource = useCase.getStockList(materialList)
        }
        _stocks.addSource(stocksSource) {
            _stocks.value = it
        }
    }

    fun fetchStocksOffline() = viewModelScope.launch(dispatchers.main) {
        _stocksLocal.removeSource(stocksSource)
        withContext(dispatchers.io) {
            stocksSourceLocal = useCase.getStockListOffline()
        }
        _stocksLocal.addSource(stocksSourceLocal) {
            _stocksLocal.value = it
        }
    }

    fun getDispatchLots(tmpId: String) {
        _dispatchLots.removeSource(dispatchLotsSource)
        dispatchLotsSource = useCase.getLots(tmpId)
        _dispatchLots.addSource(dispatchLotsSource) {
            _dispatchLots.value = it
        }
    }

    fun saveProcessOrderAndLotDetails(list: MutableList<VegaEcuadorDispatchLots>, model: VegaEcuadorDispatch) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveLotInDispatch(list, model)
            }
        }

    fun deleteLot(batchNo: String, tmpId: String) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.deleteLot(batchNo, tmpId)
            }
        }

    fun insertLot(lot: VegaEcuadorDispatchLots) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.insertLot(lot)
            }
        }

    fun saveDispatch(dispatchData: VegaEcuadorDispatch) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveDispatch(dispatchData)
        }
    }

    fun saveDispatchLotLineItems(lotList: ArrayList<VegaEcuadorDispatchLots>) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                useCase.saveDispatchLotLineItems(lotList)
            }
        }

    fun getDispatchWithLineItem() = viewModelScope.launch(dispatchers.main) {
        _dispatchItemLocal.removeSource(dispatchSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchSource = useCase.getDispatchWithLineItem()
        }
        _dispatchItemLocal.addSource(dispatchSource) {
            _dispatchItemLocal.value = it
        }
    }

    fun getDispatchWithLineItemCount() = viewModelScope.launch(dispatchers.main) {
        _dispatchItemCountLocal.removeSource(dispatchCountSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchCountSource = useCase.getDispatchWithLineItemCount()
        }
        _dispatchItemCountLocal.addSource(dispatchCountSource) {
            _dispatchItemCountLocal.value = it
        }
    }

    fun updateDeletedItem(tmpWbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateDeletedItem(tmpWbId)
        }
    }
}
