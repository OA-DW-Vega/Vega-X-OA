package com.olam.warehouse.vegax.exportsalescoffee.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesOrder
import com.olam.warehouse.master.vegacoffee.model.ContainerWithLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeExportOTWithContainer
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.exportsalescoffee.data.domain.model.VegaCoffeeExportSalesOrderModel
import com.olam.warehouse.vegax.exportsalescoffee.data.domain.model.VegaCoffeeExportSalesPostRequest
import com.olam.warehouse.vegax.exportsalescoffee.data.domain.usecase.VegaCoffeeExportSalesUsecase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 9/1/2020.
 */
class VegaCoffeeExportSalesViewModel(
    private val useCase: VegaCoffeeExportSalesUsecase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    var lotList: MutableList<VegaCoffeeExportSalesLots> = mutableListOf()
    var containerList: ArrayList<ContainerWithLots> = ArrayList()
    var currentOT: String = ""
    var currentContainer: String = ""
    var currentNewContainer: String = ""
    var currentOldContainer: String = ""
    var currentOTDetails = VegaCoffeeExportSalesOrder()
    val validateLot = MutableLiveData<VegaCoffeeExportSalesLots>()
    val validateContainer = MutableLiveData<VegaCoffeeExportSalesContainer>()
    var currentScanLot: String = ""

    private var dispatchSalesOrder: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesOrderModel>>>> =
        MutableLiveData()
    private val _dispatchSalesOrder =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesOrderModel>>>>()
    val dispatchSalesOrderModel: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesOrderModel>>>> get() = _dispatchSalesOrder

    private var otContainerSource: LiveData<VegaCoffeeExportOTWithContainer> = MutableLiveData()
    private val _otContainer = MediatorLiveData<VegaCoffeeExportOTWithContainer>()
    val otContainer: LiveData<VegaCoffeeExportOTWithContainer> get() = _otContainer

    private var stockListSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>> =
        MutableLiveData()
    private val _stockList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>>()
    val stockList: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>> get() = _stockList

    private var lotSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>> =
        MutableLiveData()
    private val _lotDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>>()
    val lotDetails: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>> get() = _lotDetails

    private var containerLotsSource: LiveData<ContainerWithLots> = MutableLiveData()
    private val _containerLots = MediatorLiveData<ContainerWithLots>()
    val containerLots: LiveData<ContainerWithLots> get() = _containerLots

    private var addedLotsSource: LiveData<List<VegaCoffeeExportSalesLots>> = MutableLiveData()
    private val _addedLots = MediatorLiveData<List<VegaCoffeeExportSalesLots>>()
    val addedLots: LiveData<List<VegaCoffeeExportSalesLots>> get() = _addedLots

    private var deliveryPostSource: LiveData<Resource<GenericReqAndResp<VegaCoffeeExportSalesPostRequest>>> =
        MutableLiveData()
    private val _deliveryPost = MediatorLiveData<Resource<GenericReqAndResp<VegaCoffeeExportSalesPostRequest>>>()
    val deliveryPost: LiveData<Resource<GenericReqAndResp<VegaCoffeeExportSalesPostRequest>>> get() = _deliveryPost

    fun getPurchaseOrder() = viewModelScope.launch(dispatchers.main) {
        _dispatchSalesOrder.removeSource(dispatchSalesOrder) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchSalesOrder = useCase.getPurchaseOrder()
        }
        _dispatchSalesOrder.addSource(dispatchSalesOrder) {
            _dispatchSalesOrder.value = it
        }
    }

    fun getOTWithContainer(otNumber: String) = viewModelScope.launch(dispatchers.main) {
        _otContainer.removeSource(otContainerSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            otContainerSource = useCase.getOTWithContainer(otNumber)
        }
        _otContainer.addSource(otContainerSource) {
            _otContainer.value = it
        }
    }

    fun saveContainer(container: VegaCoffeeExportSalesContainer) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveContainer(container)
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

    fun postDeliveryDetail(vegaDeliveryPost: VegaCoffeeExportSalesPostRequest) =
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

}
