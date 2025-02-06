package com.olam.warehouse.vegax.exportsalesindo.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesOrder
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaindocoffee.entity.IndoExporSalesMaterialList
import com.olam.warehouse.master.vegaindocoffee.entity.VegaIndoCoffeeExportSalesOrder
import com.olam.warehouse.master.vegaindocoffee.model.IndoContainerWithLots
import com.olam.warehouse.master.vegaindocoffee.model.VegaIndoCoffeeExportOTWithContainer
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.exportsalesindo.data.domain.model.VegaIndoCoffeeExportSalesOrderModel
import com.olam.warehouse.vegax.exportsalesindo.data.domain.model.VegaIndoCoffeeExportSalesPostRequest
import com.olam.warehouse.vegax.exportsalesindo.data.domain.usecase.VegaIndoCoffeeExportSalesUsecase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
class VegaIndoCoffeeExportSalesViewModel(
    private val useCase: VegaIndoCoffeeExportSalesUsecase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    var lotList: MutableList<VegaCoffeeExportSalesLots> = mutableListOf()
    var containerList: ArrayList<IndoContainerWithLots> = ArrayList()
    var currentOT: String = ""
    var currentSaleOrder: MutableList<IndoExporSalesMaterialList> = mutableListOf()
    var currentTmpId: String = ""
    var currentContainer: String = ""
    var currentNewContainer: String = ""
    var currentOldContainer: String = ""
    var currentOTDetails = VegaCoffeeExportSalesOrder()
    val validateLot = MutableLiveData<VegaCoffeeExportSalesLots>()
    val validateContainer = MutableLiveData<VegaCoffeeExportSalesContainer>()
    var currentScanLot: String = ""

    private var dispatchSalesOrder: LiveData<Resource<GenericReqAndResp<List<VegaIndoCoffeeExportSalesOrderModel>>>> =
        MutableLiveData()
    private val _dispatchSalesOrder =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaIndoCoffeeExportSalesOrderModel>>>>()
    val dispatchSalesOrderModel: LiveData<Resource<GenericReqAndResp<List<VegaIndoCoffeeExportSalesOrderModel>>>> get() = _dispatchSalesOrder

    private var offlineDispatchSalesOrder: LiveData<List<IndoExporSalesMaterialList>> =
        MutableLiveData()
    private val _offlineDispatchSalesOrder =
        MediatorLiveData<List<IndoExporSalesMaterialList>>()
    val offlineDispatchSalesOrderModel: LiveData<List<IndoExporSalesMaterialList>> get() = _offlineDispatchSalesOrder

    private var otContainerSource: LiveData<VegaIndoCoffeeExportOTWithContainer> = MutableLiveData()
    private val _otContainer = MediatorLiveData<VegaIndoCoffeeExportOTWithContainer>()
    val otContainer: LiveData<VegaIndoCoffeeExportOTWithContainer> get() = _otContainer

    private var stockListSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>> =
        MutableLiveData()
    private val _stockList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>>()
    val stockList: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>> get() = _stockList

    private var stocksSourceLocal: LiveData<List<VegaEcuadorDispatchStocks>> = MutableLiveData()
    private val _stocksLocal = MediatorLiveData<List<VegaEcuadorDispatchStocks>>()
    val stocksLocal: LiveData<List<VegaEcuadorDispatchStocks>> get() = _stocksLocal

    private var lotSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>> =
        MutableLiveData()
    private val _lotDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>>()
    val lotDetails: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>> get() = _lotDetails

    private var containerLotsSource: LiveData<IndoContainerWithLots> = MutableLiveData()
    private val _containerLots = MediatorLiveData<IndoContainerWithLots>()
    val containerLots: LiveData<IndoContainerWithLots> get() = _containerLots

    private var addedLotsSource: LiveData<List<VegaCoffeeExportSalesLots>> = MutableLiveData()
    private val _addedLots = MediatorLiveData<List<VegaCoffeeExportSalesLots>>()
    val addedLots: LiveData<List<VegaCoffeeExportSalesLots>> get() = _addedLots

    private var deliveryPostSource: LiveData<Resource<GenericReqAndResp<VegaIndoCoffeeExportSalesPostRequest>>> =
        MutableLiveData()
    private val _deliveryPost = MediatorLiveData<Resource<GenericReqAndResp<VegaIndoCoffeeExportSalesPostRequest>>>()
    val deliveryPost: LiveData<Resource<GenericReqAndResp<VegaIndoCoffeeExportSalesPostRequest>>> get() = _deliveryPost

    private var exportSalesSource: LiveData<List<VegaIndoCoffeeExportSalesOrder>> = MutableLiveData()
    private val _exportSalesLocal = MediatorLiveData<List<VegaIndoCoffeeExportSalesOrder>>()
    val exportSalesLocal: LiveData<List<VegaIndoCoffeeExportSalesOrder>> get() = _exportSalesLocal

    fun getIndoExportSalesItem() = viewModelScope.launch(dispatchers.main) {
        _exportSalesLocal.removeSource(exportSalesSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            exportSalesSource = useCase.getIndoExportSalesItem()
        }
        _exportSalesLocal.addSource(exportSalesSource) {
            _exportSalesLocal.value = it
        }
    }

    fun getPurchaseOrder() = viewModelScope.launch(dispatchers.main) {
        _dispatchSalesOrder.removeSource(dispatchSalesOrder) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchSalesOrder = useCase.getPurchaseOrder()
        }
        _dispatchSalesOrder.addSource(dispatchSalesOrder) {
            _dispatchSalesOrder.value = it
        }
    }

    fun getOfflinePurchaseOrder() = viewModelScope.launch(dispatchers.main) {
        _offlineDispatchSalesOrder.removeSource(offlineDispatchSalesOrder) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offlineDispatchSalesOrder = useCase.getOfflinePurchaseOrder()
        }
        _offlineDispatchSalesOrder.addSource(offlineDispatchSalesOrder) {
            _offlineDispatchSalesOrder.value = it
        }
    }

    fun getOTWithContainer(tmpId: String) = viewModelScope.launch(dispatchers.main) {
        _otContainer.removeSource(otContainerSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            otContainerSource = useCase.getOTWithContainer(tmpId)
        }
        _otContainer.addSource(otContainerSource) {
            _otContainer.value = it
        }
    }

    fun saveContainer(
        container: VegaCoffeeExportSalesContainer,
        currentSaleOrder: ArrayList<IndoExporSalesMaterialList>
    ) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveContainer(container, currentSaleOrder)
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

    fun fetchStocksOffline() = viewModelScope.launch(dispatchers.main) {
        _stocksLocal.removeSource(stocksSourceLocal)
        withContext(dispatchers.io) {
            stocksSourceLocal = useCase.getStockListOffline()
        }
        _stocksLocal.addSource(stocksSourceLocal) {
            _stocksLocal.value = it
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

    fun validateLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateLot.postValue(useCase.validateLot(batchNumber))
        }
    }

    fun validateContainer(containerNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateContainer.postValue(useCase.validateContainer(containerNumber))
        }
    }

    fun saveLotDetails(vegaCoffeeSalesLots: ArrayList<VegaCoffeeExportSalesLots>) =
        viewModelScope.launch(dispatchers.io) {
            withContext(dispatchers.io) {
                useCase.saveLotDetails(vegaCoffeeSalesLots)
            }
        }

    fun updateLotWeightInfo(lot: ArrayList<VegaCoffeeExportSalesLots>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateLotEditWeight(lot)
        }
    }

    fun getContainerWithLots(containerNumber: String) = viewModelScope.launch(dispatchers.main) {
        _containerLots.removeSource(containerLotsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            containerLotsSource = useCase.getContainerWithLots(containerNumber)
        }
        _containerLots.addSource(containerLotsSource) {
            _containerLots.value = it
        }
    }

    fun removeLotDetails(lot: VegaCoffeeExportSalesLots) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.removeLotDetails(lot)
        }
    }

    fun getAddedLotList() = viewModelScope.launch(dispatchers.main) {
        _addedLots.removeSource(addedLotsSource)
        withContext(dispatchers.io) {
            addedLotsSource = useCase.getAddedLotList()
        }
        _addedLots.addSource(addedLotsSource) {
            _addedLots.value = it
        }
    }

    fun postDeliveryDetail(vegaDeliveryPost: VegaIndoCoffeeExportSalesPostRequest) =
        viewModelScope.launch(dispatchers.main) {
            _deliveryPost.removeSource(deliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                deliveryPostSource = useCase.postDeliveryDetail(vegaDeliveryPost)
            }
            _deliveryPost.addSource(deliveryPostSource) {
                _deliveryPost.value = it
            }
        }

    fun removeConatinerWitfLots(containerNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.removeConatinerWitfLots(containerNumber)
        }
    }

    fun updateContainerId(oldContainerId: String, newContainerId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateContainerId(oldContainerId, newContainerId)
        }
    }

    fun deletedItem(tmpId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deletedItem(tmpId)
        }
    }
}
