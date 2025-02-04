package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.*
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuaOffloadingWithLineItems
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.VegaGhanaCocoaOffloadingUseCase
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGhanaOffloadingPostRequest
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGhanaWeighScalePallet
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.*

class VegaGhanaCocoaOffloadingViewModel(
    private val useCaseCocoa: VegaGhanaCocoaOffloadingUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var storageLocationListSource: LiveData<List<VegaStorageLocationDetail>> = MutableLiveData()
    private val _storageLocationList = MediatorLiveData<List<VegaStorageLocationDetail>>()
    val storageLocationList: LiveData<List<VegaStorageLocationDetail>> get() = _storageLocationList


    private var materialStorgeLocationSource: LiveData<List<VegaStorageLocationDetail>> = mutableLiveDataOf(emptyList())
    private val _materialStorgeLocation = MediatorLiveData<List<VegaStorageLocationDetail>>()
    val materialStorgeLocation: LiveData<List<VegaStorageLocationDetail>> get() = _materialStorgeLocation

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val supplier: LiveData<List<VegaVendor>> get() = _supplier

    private var mtnrSource: LiveData<List<VegaReceivingMtn>> = MutableLiveData()
    private val _mtnrLocal = MediatorLiveData<List<VegaReceivingMtn>>()
    val mtnrLocal: LiveData<List<VegaReceivingMtn>> get() = _mtnrLocal

    private var storageLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _storageLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val storageLocation: LiveData<List<VegaCustomStLocation>> get() = _storageLocation

    private var stocksSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> = MutableLiveData()
    private val _stocks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>>()
    val stocks: LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> get() = _stocks

    private var stocksSourceOffline: LiveData<List<VegaEcuadorDispatchStocks>> = MutableLiveData()
    private val _stocksOffline = MediatorLiveData<List<VegaEcuadorDispatchStocks>>()
    val stocksOffline: LiveData<List<VegaEcuadorDispatchStocks>> get() = _stocksOffline

    private var bagSourceSaved: LiveData<List<VegaEcuadorOffloadingBagMaterial>> = mutableLiveDataOf()
    private val _bagItemsSaved = MediatorLiveData<List<VegaEcuadorOffloadingBagMaterial>>()
    val bagItemsSaved: LiveData<List<VegaEcuadorOffloadingBagMaterial>> get() = _bagItemsSaved

    private var poListSource: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> =
        MutableLiveData()
    private val _poList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>()
    val poList: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> get() = _poList

    private var poListLocalSource: LiveData<List<VegaEcuadorPurchaseOrder>> =
        MutableLiveData()
    private val _poListLocal = MediatorLiveData<List<VegaEcuadorPurchaseOrder>>()
    val poListLocal: LiveData<List<VegaEcuadorPurchaseOrder>> get() = _poListLocal

    private var bagSource: LiveData<List<VegaEcuadorOffloadingBagMaterial>> = mutableLiveDataOf()
    private val _bagItems = MediatorLiveData<List<VegaEcuadorOffloadingBagMaterial>>()
    val bagItems: LiveData<List<VegaEcuadorOffloadingBagMaterial>> get() = _bagItems

    private var receiveSource: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> = MutableLiveData()
    private val _receive = MediatorLiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>()
    val receive: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> get() = _receive

    private var offloadingSource: LiveData<List<VegaEcuaOffloadingWithLineItems>> = MutableLiveData()
    private val _offloadingItemLocal = MediatorLiveData<List<VegaEcuaOffloadingWithLineItems>>()
    val offloadingItemLocal: LiveData<List<VegaEcuaOffloadingWithLineItems>> get() = _offloadingItemLocal

    private var weighBridgeOnlineSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> =
        MutableLiveData()
    private val _weighBridgeOnline = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>()
    val weighBridgeOnline: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> get() = _weighBridgeOnline


    private var offloadingCountSource: LiveData<List<VegaEcuaOffloadingWithLineItems>> = MutableLiveData()
    private val _offloadingItemCountLocal = MediatorLiveData<List<VegaEcuaOffloadingWithLineItems>>()
    val offloadingItemCountLocal: LiveData<List<VegaEcuaOffloadingWithLineItems>> get() = _offloadingItemCountLocal


    var vegaCoffeeReceivingData = VegaCoffeeReceiving()

    private var receiveLocalSource: LiveData<List<VegaReceiving>> = MutableLiveData()
    private var receiveWithLineItemLocalSource: LiveData<List<VegaReceivingWithLineItems>> = MutableLiveData()
    private var locationSource: LiveData<List<VegaSupplyStorageLocation>> = MutableLiveData()
    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    private var uomDetailSource: LiveData<List<VegaUomDetails>> = MutableLiveData()
    private var warehouseSource: LiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>> =
        MutableLiveData()
    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()

    private var weightBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> = MutableLiveData()
    private var warehouseSourceLocal: LiveData<List<VegaReceivingWarehouse>> = MutableLiveData()
    private var warehouseWithMtnsSource: LiveData<VegaReceivingWarehouseWithMtns> = MutableLiveData()
    private val _receiveLocal = MediatorLiveData<List<VegaReceiving>>()
    private val _receiveWithLineItemLocal = MediatorLiveData<List<VegaReceivingWithLineItems>>()
    private val _location = MediatorLiveData<List<VegaSupplyStorageLocation>>()
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()
    private val _uomDetail = MediatorLiveData<List<VegaUomDetails>>()
    private val _warehouse = MediatorLiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>>()
    private val _warehouseLocal = MediatorLiveData<List<VegaReceivingWarehouse>>()
    private val _warehouseWithMtns = MediatorLiveData<VegaReceivingWarehouseWithMtns>()
    private var _weighBridge = MediatorLiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()

    val receiveLocal: LiveData<List<VegaReceiving>> get() = _receiveLocal
    val receiveWithLineItemLocal: LiveData<List<VegaReceivingWithLineItems>> get() = _receiveWithLineItemLocal
    val suppplier: LiveData<List<VegaVendor>> get() = _supplier
    val location: LiveData<List<VegaSupplyStorageLocation>> get() = _location
    val material: LiveData<List<VegaPackageMaterial>> get() = _material
    val uomDetail: LiveData<List<VegaUomDetails>> get() = _uomDetail
    val warehouse: LiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>> get() = _warehouse
    val warehouseLocal: LiveData<List<VegaReceivingWarehouse>> get() = _warehouseLocal
    val warehouseWithMtns: LiveData<VegaReceivingWarehouseWithMtns> get() = _warehouseWithMtns
    val weighBridge: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> get() = _weighBridge
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var truckInWeightBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> =
        MutableLiveData()
    private var _truckInWeighBridge = MediatorLiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>()
    val truckInWeighBridge: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> get() = _truckInWeighBridge

    private var weighBridgeIdSource: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> =
        MutableLiveData()
    private var _weighBridgeId = MediatorLiveData<Resource<GenericReqAndResp<VegaReceiving>>>()
    val weighBridgeId: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> get() = _weighBridgeId

    private var miscellaneousSource: LiveData<List<VegaCocoaMiscellaneous>> = mutableLiveDataOf()
    private val _miscellaneousItems = MediatorLiveData<List<VegaCocoaMiscellaneous>>()
    val miscellaneousItems: LiveData<List<VegaCocoaMiscellaneous>> get() = _miscellaneousItems

    private var bagSourceMtnr: LiveData<List<VegaCoffeeOffloadingBagMaterial>> = mutableLiveDataOf()
    private val _bagItemsMtnr = MediatorLiveData<List<VegaCoffeeOffloadingBagMaterial>>()
    val bagItemsMtnr: LiveData<List<VegaCoffeeOffloadingBagMaterial>> get() = _bagItemsMtnr

    private var palletResource: LiveData<Resource<GenericReqAndResp<List<VegaGhanaWeighScalePallet>>>> =
        MutableLiveData()
    private val _pallet = MediatorLiveData<Resource<GenericReqAndResp<List<VegaGhanaWeighScalePallet>>>>()
    val pallet: LiveData<Resource<GenericReqAndResp<List<VegaGhanaWeighScalePallet>>>> get() = _pallet

    private var lotSource: LiveData<List<VegaReceivingMtnLots>> = MutableLiveData()
    private val _lotLocal = MediatorLiveData<List<VegaReceivingMtnLots>>()
    val lotLocal: LiveData<List<VegaReceivingMtnLots>> get() = _lotLocal

    private var storageSource: LiveData<List<VegaSupplyStorageLocation>> = MutableLiveData()
    private val _storageLocal = MediatorLiveData<List<VegaSupplyStorageLocation>>()
    val storageLocal: LiveData<List<VegaSupplyStorageLocation>> get() = _storageLocal

    private var offloadingMtnrSource: LiveData<VegaCoffeeReceivingMtnrWithLots> = mutableLiveDataOf()
    private val _offloadingMtnr = MediatorLiveData<VegaCoffeeReceivingMtnrWithLots>()
    val offloadingMtnr: LiveData<VegaCoffeeReceivingMtnrWithLots> get() = _offloadingMtnr

    private var offlineMtnrSource: LiveData<List<VegaCoffeeReceivingMtnrWithLots>> = mutableLiveDataOf()
    private val _offlineMtnr = MediatorLiveData<List<VegaCoffeeReceivingMtnrWithLots>>()
    val offlineMtnr: LiveData<List<VegaCoffeeReceivingMtnrWithLots>> get() = _offlineMtnr

    private var offloadingPostSource: LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>> =
        MutableLiveData()
    private val _offloadingPost = MediatorLiveData<Resource<GenericReqAndResp<VegaMtntResponse>>>()
    val offloadingPost: LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>> get() = _offloadingPost

    fun getProducts() = viewModelScope.launch(dispatchers.main) {
        _product.removeSource(productSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            productSource = useCaseCocoa.getProducts()
        }
        _product.addSource(productSource) {
            _product.value = it
        }
    }


    suspend fun getMaterialStorageLocation() = useCaseCocoa.getMaterialStorageLocation()
   /* fun getMaterialStorageLocation() = viewModelScope.launch(dispatchers.main) {
        _materialStorgeLocation.removeSource(materialStorgeLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            materialStorgeLocationSource = useCaseCocoa.getMaterialStorageLocation()
        }
        _materialStorgeLocation.addSource(materialStorgeLocationSource) {
            _materialStorgeLocation.value = it
        }
    }*/

    fun getSuppliers() = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCaseCocoa.getSupplier()
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
        }
    }

    fun getUomDetails() = viewModelScope.launch(dispatchers.main) {
        _uomDetail.removeSource(uomDetailSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            uomDetailSource = useCaseCocoa.getuomDetail()
        }
        _uomDetail.addSource(uomDetailSource) {
            _uomDetail.value = it
        }
    }

    fun getStorageLocation() = viewModelScope.launch(dispatchers.main) {
        _storageLocation.removeSource(storageLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            storageLocationSource = useCaseCocoa.getStorageLocation()
        }
        _storageLocation.addSource(storageLocationSource) {
            _storageLocation.value = it
        }
    }

    fun fetchStocksOffline() = viewModelScope.launch(dispatchers.main) {
        _stocksOffline.removeSource(stocksSourceOffline)
        withContext(dispatchers.io) {
            stocksSourceOffline = useCaseCocoa.getStockListOffline()
        }
        _stocksOffline.addSource(stocksSourceOffline) {
            _stocksOffline.value = it
        }
    }

    fun fetchStocks(material: String) = viewModelScope.launch(dispatchers.main) {
        _stocks.removeSource(stocksSource)
        withContext(dispatchers.io) {
            stocksSource = useCaseCocoa.getStocks(material)
        }
        _stocks.addSource(stocksSource) {
            _stocks.value = it
        }
    }

    fun getPOList() = viewModelScope.launch(dispatchers.main) {
        _poList.removeSource(poListSource)
        withContext(dispatchers.io) {
            poListSource = useCaseCocoa.getPOList()
        }
        _poList.addSource(poListSource) {
            _poList.value = it
        }
    }

    fun getTransactions() = viewModelScope.launch(dispatchers.main) {
        _offlineMtnr.removeSource(offlineMtnrSource)
        withContext(dispatchers.io) {
            offlineMtnrSource = useCaseCocoa.getTransactions()
        }
        _offlineMtnr.addSource(offlineMtnrSource) {
            _offlineMtnr.value = it
        }
    }

    fun getWeighBridgeDataOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        getWeighBridgeDetailOnline()
        return weighBridgeOnline
    }

    private fun getWeighBridgeDetailOnline() = viewModelScope.launch(dispatchers.main) {
        _weighBridgeOnline.removeSource(weighBridgeOnlineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeOnlineSource = useCaseCocoa.getMtnrWeighBridgeDetailOnline()
        }
        _weighBridgeOnline.addSource(weighBridgeOnlineSource) {
            _weighBridgeOnline.value = it
        }
    }

    fun getPOListLocal() = viewModelScope.launch(dispatchers.main) {
        _poListLocal.removeSource(poListLocalSource)
        withContext(dispatchers.io) {
            poListLocalSource = useCaseCocoa.getPOListLocal()
        }
        _poListLocal.addSource(poListLocalSource) {
            _poListLocal.value = it
        }
    }

    fun saveBagDetails(material: VegaEcuadorOffloadingBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.saveBagDetails(material)
        }
    }

    fun getSavedBagItems() =
        viewModelScope.launch(dispatchers.main) {
            _bagItemsSaved.removeSource(bagSourceSaved) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                bagSourceSaved = useCaseCocoa.getSavedBagItems()
            }
            _bagItemsSaved.addSource(bagSourceSaved) {
                _bagItemsSaved.value = it
            }
        }

    fun deleteBagDetails(id: Int, tmpWbId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.deleteBagDetails(id, tmpWbId)
        }
    }

    fun clearBagDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.clearBagDetails()
        }
    }

    fun getBagItems(materialCode: String?, supplierCode: String, type: String, poId: String, tmpWbId: String) =
        viewModelScope.launch(dispatchers.main) {
            _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                bagSource = useCaseCocoa.getBagItems(materialCode, supplierCode, type, poId, tmpWbId)
            }
            _bagItems.addSource(bagSource) {
                _bagItems.value = it
            }
        }

    fun updateSyncedMtnrDeletedItem() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCaseCocoa.updateSyncedMtnrDeletedItem()
        }
    }

    fun postGhanaCocoaOffloadingData(
        receivingData: VegaGhanaCocoaOffloadingPost
    ) = viewModelScope.launch(dispatchers.main) {
        _receive.removeSource(receiveSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            receiveSource = useCaseCocoa.postGhanaCocoaOffloadingDetail(receivingData)
        }
        _receive.addSource(receiveSource) {
            _receive.value = it
        }
    }

    fun saveOffloading(receivingData: VegaReceiving) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCaseCocoa.saveOffloading(receivingData)
        }
    }

    fun saveReceivingLineItems(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                useCaseCocoa.saveReceivingLineItems(bagList)
            }
        }

    fun getOffloadingWithLineItem() = viewModelScope.launch(dispatchers.main) {
        _offloadingItemLocal.removeSource(offloadingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offloadingSource = useCaseCocoa.getOffloadingWithLineItem()
        }
        _offloadingItemLocal.addSource(offloadingSource) {
            _offloadingItemLocal.value = it
        }
    }

    fun deleteOffloadingItem() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCaseCocoa.deleteOffloadingItem()
        }
    }

    fun getOffloadingWithLineItemCount() = viewModelScope.launch(dispatchers.main) {
        _offloadingItemCountLocal.removeSource(offloadingCountSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offloadingCountSource = useCaseCocoa.getOffloadingWithLineItemCount()
        }
        _offloadingItemCountLocal.addSource(offloadingCountSource) {
            _offloadingItemCountLocal.value = it
        }
    }

    fun getProcessTypeList(role: String) = viewModelScope.launch(dispatchers.main) {
        _miscellaneousItems.removeSource(miscellaneousSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            miscellaneousSource = useCaseCocoa.getProcessTypeList(role)
        }
        _miscellaneousItems.addSource(miscellaneousSource) {
            _miscellaneousItems.value = it
        }
    }

    fun updateDeletedItem(tmpWbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCaseCocoa.updateDeletedItem(tmpWbId)
        }
    }

    fun deleteStatus(mtnNumber: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.deleteStatus(mtnNumber)
        }
    }

    fun updateWBToQualityAndGrnTable(tmpWbid: String, wbid: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCaseCocoa.updateWBToQualityAndGrnTable(tmpWbid, wbid)
        }
    }


    fun getWeighBridgeData(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> {
        getWeighBridgeDetail()
        return weighBridge
    }

    private fun getWeighBridgeDetail() = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weightBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weightBridgeSource = useCaseCocoa.getWeighBridgeDetailOnline()
        }
        _weighBridge.addSource(weightBridgeSource) {
            _weighBridge.value = it
        }
    }

    fun getLocations() = viewModelScope.launch(dispatchers.main) {
        _location.removeSource(locationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            locationSource = useCaseCocoa.getLocations()
        }
        _location.addSource(locationSource) {
            _location.value = it
        }
    }

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = useCaseCocoa.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
        }
    }

    fun getMaterials() = viewModelScope.launch(dispatchers.main) {
        _material.removeSource(materialSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            materialSource = useCaseCocoa.getMaterials()
        }
        _material.addSource(materialSource) {
            _material.value = it
        }
    }

    fun postReceivingData(
        receivingData: VegaReceivingPost
    ) = viewModelScope.launch(dispatchers.main) {
        _receive.removeSource(receiveSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            receiveSource = useCaseCocoa.postReceivingDetail(receivingData)
        }
        _receive.addSource(receiveSource) {
            _receive.value = it
        }
    }

    fun postReceivingMtnData(receivingData: VegaReceivingPost) = viewModelScope.launch(dispatchers.main) {
        _receive.removeSource(receiveSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            receiveSource = useCaseCocoa.postReceivingMtnDetail(receivingData)
        }
        _receive.addSource(receiveSource) {
            _receive.value = it
        }
    }

    fun saveReceiving(receivingData: VegaReceiving) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCaseCocoa.saveReceiving(receivingData)
        }
    }

    fun getReceiving() = viewModelScope.launch(dispatchers.main) {
        _receiveLocal.removeSource(receiveLocalSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            receiveLocalSource = useCaseCocoa.getReceiving()
        }
        _receiveLocal.addSource(receiveLocalSource) {
            _receiveLocal.value = it
        }
    }

    fun getReceivingWithLineItem() = viewModelScope.launch(dispatchers.main) {
        _receiveWithLineItemLocal.removeSource(receiveWithLineItemLocalSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            receiveWithLineItemLocalSource = useCaseCocoa.getReceivingWithLineItem()
        }
        _receiveWithLineItemLocal.addSource(receiveWithLineItemLocalSource) {
            _receiveWithLineItemLocal.value = it
        }
    }


    fun getWarehouses() = viewModelScope.launch(dispatchers.main) {
        _warehouseLocal.removeSource(warehouseSourceLocal) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            warehouseSourceLocal = useCaseCocoa.getWarehouses()
        }
        _warehouseLocal.addSource(warehouseSourceLocal) {
            _warehouseLocal.value = it
        }
    }

    fun getMTNRs() = viewModelScope.launch(dispatchers.main) {
        _mtnrLocal.removeSource(mtnrSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            mtnrSource = useCaseCocoa.getMTNRs()
        }
        _mtnrLocal.addSource(mtnrSource) {
            _mtnrLocal.value = it
        }
    }

    fun getOfflineLots() = viewModelScope.launch(dispatchers.main) {
        _lotLocal.removeSource(lotSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            lotSource = useCaseCocoa.getOfflineLots()
        }
        _lotLocal.addSource(lotSource) {
            _lotLocal.value = it
        }
    }

    fun getStorageLoc() = viewModelScope.launch(dispatchers.main) {
        _storageLocal.removeSource(storageSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            storageSource = useCaseCocoa.getStorageLoc()
        }
        _storageLocal.addSource(storageSource) {
            _storageLocal.value = it
        }
    }

    fun getWarehousesWithMtns(whID: String) = viewModelScope.launch(dispatchers.main) {
        _warehouseWithMtns.removeSource(warehouseWithMtnsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            warehouseWithMtnsSource = useCaseCocoa.getWarehousesWithMtns(whID)
        }
        _warehouseWithMtns.addSource(warehouseWithMtnsSource) {
            _warehouseWithMtns.value = it
        }
    }

    fun fetchWarehouseWithMtns() = viewModelScope.launch(dispatchers.main) {
        _warehouse.removeSource(warehouseSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            warehouseSource = useCaseCocoa.fetchWarehouseWithMtns()
        }
        _warehouse.addSource(warehouseSource) {
            _warehouse.value = it
        }
    }

    fun saveWarehouseWithMtns(it: VegaCoffeeReceivingMtnWrapper) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCaseCocoa.saveWarehouseWithMtns(it)
        }
    }

    fun getTareWeight(postData: List<VegaReceiving>) = postData.sumByDouble { it.tareWeight?.toDouble() ?: 0.0 }
    fun getGrossWeight(postData: List<VegaReceiving>) = postData.sumByDouble { it.grossWeight!!.toDouble() }
    fun getNetWeight(postData: List<VegaReceiving>) = postData.sumByDouble { it.netWeight.toDouble() }
    fun getBagCount(postData: List<VegaReceiving>) = postData.sumBy { it.bagCount?.toInt() ?: 0 }
    /*fun saveReceivingLineItems(postData: MutableList<VegaReceiving>) = viewModelScope.launch(dispatchers.main) {
        val lineItems = getLineItemFromReceiving(postData)
        withContext(dispatchers.io) {
            useCase.saveReceivingLineItems(lineItems)
        }
    }*/

    fun updateDeletedItem(Wbid: String?, txnId: String?) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            Wbid?.let { useCaseCocoa.updateDeletedItem(it, txnId) }
        }
    }

    fun getTareWeightTemp(postData: List<VegaReceiving>) =
        postData.sumByBigDecimal { it.tareWeight?.toDouble() ?: 0.0 }

    fun getGrossWeightTemp(postData: List<VegaReceiving>) =
        postData.sumByBigDecimal { it.grossWeight!!.toDouble() }

    fun getNetWeightTemp(postData: List<VegaReceiving>) = postData.sumByBigDecimal { it.netWeight.toDouble() }


    inline fun <T> Iterable<T>.sumByBigDecimal(selector: (T) -> Double): BigDecimal {

        var b = BigDecimal("0.0")
        for (element in this) {
            b += BigDecimal(selector(element))
        }
        return b
    }

    fun updateReceivingFailMsg(msg: String, tmpWbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCaseCocoa.updateReceivingFailMsg(msg, tmpWbId)
        }
    }

    fun getTruckInWeighBridgeDetail() = viewModelScope.launch(dispatchers.main) {
        _truckInWeighBridge.removeSource(truckInWeightBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            truckInWeightBridgeSource = useCaseCocoa.getTruckInWeighBridgeDetailOnline()
        }
        _truckInWeighBridge.addSource(truckInWeightBridgeSource) {
            _truckInWeighBridge.value = it
        }
    }

    fun getWeighBridgeIdDetail(wbid: String) = viewModelScope.launch(dispatchers.main) {
        _weighBridgeId.removeSource(weighBridgeIdSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeIdSource = useCaseCocoa.getWeighBridgeIdDetail(wbid)
        }
        _weighBridgeId.addSource(weighBridgeIdSource) {
            _weighBridgeId.value = it
        }
    }

    fun getBagItems(batchNumber: String?, mtnNumber: String) = viewModelScope.launch(dispatchers.main) {
        _bagItemsMtnr.removeSource(bagSourceMtnr) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSourceMtnr = useCaseCocoa.getBagItems(batchNumber, mtnNumber)
        }
        _bagItemsMtnr.addSource(bagSourceMtnr) {
            _bagItemsMtnr.value = it
        }
    }

    fun saveBagDetails(bagMaterial: VegaCoffeeOffloadingBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.saveBagDetails(bagMaterial)
        }
    }

    fun deleteBagDetails(id: Int) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.deleteBagDetails(id)
        }
    }

    fun deleteMtnrQuality(wbId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.deleteMtnrQuality(wbId)
        }
    }

    fun getPalletInfo(batchNumber: String, material: String) = viewModelScope.launch(dispatchers.main) {
        _pallet.removeSource(palletResource)
        withContext(dispatchers.io) {
            palletResource = useCaseCocoa.getPalletDetails(batchNumber, material)
        }
        _pallet.addSource(palletResource) {
            _pallet.value = it
        }
    }

    fun getOBDDetails(deliveryNumber: String) = viewModelScope.launch(dispatchers.main) {
        _offloadingMtnr.removeSource(offloadingMtnrSource)
        withContext(dispatchers.io) {
            offloadingMtnrSource = useCaseCocoa.getOBDDetails(deliveryNumber)
        }
        _offloadingMtnr.addSource(offloadingMtnrSource) {
            _offloadingMtnr.value = it
        }
    }

    fun saveMtnrReceivingLots(vegaCoffeeReceivingData: VegaCoffeeReceiving, lot: VegaCoffeeReceiveLots) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCaseCocoa.saveMtnrReceivingLots(vegaCoffeeReceivingData, lot)
            }
        }
    fun updateOBD(mtnNumber: String, flag: Boolean) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCaseCocoa.updateOBD(mtnNumber, flag)
            }
        }

    fun postOffloadingDetail(vegaOffloadingPost: VegaGhanaOffloadingPostRequest) =
        viewModelScope.launch(dispatchers.main) {
            _offloadingPost.removeSource(offloadingPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                offloadingPostSource = useCaseCocoa.postOffloadingDetail(vegaOffloadingPost)
            }
            _offloadingPost.addSource(offloadingPostSource) {
                _offloadingPost.value = it
            }
        }

    fun updateStatus(mtnNumber: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCaseCocoa.updateStatus(mtnNumber)
        }
    }

    fun getStorageLocationDetail() = viewModelScope.launch(dispatchers.main) {
        _storageLocationList.removeSource(storageLocationListSource)
        withContext(dispatchers.io) {
            storageLocationListSource = useCaseCocoa.getStorageLocationDetail()
        }
        _storageLocationList.addSource(storageLocationListSource) {
            _storageLocationList.value = it
        }
    }
}
