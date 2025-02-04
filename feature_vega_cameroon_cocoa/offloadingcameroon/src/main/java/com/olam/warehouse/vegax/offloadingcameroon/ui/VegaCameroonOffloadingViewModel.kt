package com.olam.warehouse.vegax.offloadingcameroon.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaMtntResponse
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.master.vegaecuador.entity.VegaCameroonOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuaOffloadingWithLineItems
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.VegaCameroonOffloadingUseCase
import com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.model.VegaCameroonMtntDetails
import com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.model.VegaCameroonOffloadingPost
import com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.model.VegaCameroonOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.model.VegaCameroonWeighScalePallet
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.*

/**
 * Created by Keerthi Santhanam on 23/6/2020.
 */
class VegaCameroonOffloadingViewModel(
    private val useCase: VegaCameroonOffloadingUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    var bagModelList = ArrayList<VegaEcuadorOffloadingBagMaterial>()
    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val supplier: LiveData<List<VegaVendor>> get() = _supplier

    private var poListSource: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> =
        MutableLiveData()
    private val _poList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>()
    val poList: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> get() = _poList

    private var mtntWeightSource: LiveData<Resource<GenericReqAndResp<VegaCameroonMtntDetails>>> =
        MutableLiveData()
    private val _mtntWeight = MediatorLiveData<Resource<GenericReqAndResp<VegaCameroonMtntDetails>>>()
    val mtntWeight: LiveData<Resource<GenericReqAndResp<VegaCameroonMtntDetails>>> get() = _mtntWeight


    private var poListLocalSource: LiveData<List<VegaEcuadorPurchaseOrder>> =
        MutableLiveData()
    private val _poListLocal = MediatorLiveData<List<VegaEcuadorPurchaseOrder>>()
    val poListLocal: LiveData<List<VegaEcuadorPurchaseOrder>> get() = _poListLocal

    private var bagSource: LiveData<List<VegaEcuadorOffloadingBagMaterial>> = mutableLiveDataOf()
    private val _bagItems = MediatorLiveData<List<VegaEcuadorOffloadingBagMaterial>>()
    val bagItems: LiveData<List<VegaEcuadorOffloadingBagMaterial>> get() = _bagItems

    private var bagSourceSaved: LiveData<List<VegaEcuadorOffloadingBagMaterial>> = mutableLiveDataOf()
    private val _bagItemsSaved = MediatorLiveData<List<VegaEcuadorOffloadingBagMaterial>>()
    val bagItemsSaved: LiveData<List<VegaEcuadorOffloadingBagMaterial>> get() = _bagItemsSaved


    private var bagSourceCameroonSaved: LiveData<List<VegaCameroonOffloadingBagMaterial>> = mutableLiveDataOf()
    private val _bagCameroonSaved = MediatorLiveData<List<VegaCameroonOffloadingBagMaterial>>()
    val bagCameroonSaved: LiveData<List<VegaCameroonOffloadingBagMaterial>> get() = _bagCameroonSaved

    private var bagSourceSavedB: LiveData<List<VegaEcuadorOffloadingBagMaterial>> = mutableLiveDataOf()
    private val _bagItemsSavedB = MediatorLiveData<List<VegaEcuadorOffloadingBagMaterial>>()
    val bagItemsSavedB: LiveData<List<VegaEcuadorOffloadingBagMaterial>> get() = _bagItemsSavedB

    private var receiveSource: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> = MutableLiveData()
    private val _receive = MediatorLiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>()
    val receive: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> get() = _receive

    private var offloadingSource: LiveData<List<VegaEcuaOffloadingWithLineItems>> = MutableLiveData()
    private val _offloadingItemLocal = MediatorLiveData<List<VegaEcuaOffloadingWithLineItems>>()
    val offloadingItemLocal: LiveData<List<VegaEcuaOffloadingWithLineItems>> get() = _offloadingItemLocal

    private var offloadingCountSource: LiveData<List<VegaEcuaOffloadingWithLineItems>> = MutableLiveData()
    private val _offloadingItemCountLocal = MediatorLiveData<List<VegaEcuaOffloadingWithLineItems>>()
    val offloadingItemCountLocal: LiveData<List<VegaEcuaOffloadingWithLineItems>> get() = _offloadingItemCountLocal

    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    var vegaCoffeeReceivingData = VegaCoffeeReceiving()

    private var receiveLocalSource: LiveData<List<VegaReceiving>> = MutableLiveData()
    private var receiveWithLineItemLocalSource: LiveData<List<VegaReceivingWithLineItems>> = MutableLiveData()
    private var locationSource: LiveData<List<VegaSupplyStorageLocation>> = MutableLiveData()
    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    private var warehouseSource: LiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>> =
        MutableLiveData()

    private var weightBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> = MutableLiveData()
    private var warehouseSourceLocal: LiveData<List<VegaReceivingWarehouse>> = MutableLiveData()
    private var warehouseWithMtnsSource: LiveData<VegaReceivingWarehouseWithMtns> = MutableLiveData()
    private val _receiveLocal = MediatorLiveData<List<VegaReceiving>>()
    private val _receiveWithLineItemLocal = MediatorLiveData<List<VegaReceivingWithLineItems>>()
    private val _location = MediatorLiveData<List<VegaSupplyStorageLocation>>()
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()
    private val _warehouse = MediatorLiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>>()
    private val _warehouseLocal = MediatorLiveData<List<VegaReceivingWarehouse>>()
    private val _warehouseWithMtns = MediatorLiveData<VegaReceivingWarehouseWithMtns>()
    private var _weighBridge = MediatorLiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>()

    val receiveLocal: LiveData<List<VegaReceiving>> get() = _receiveLocal
    val receiveWithLineItemLocal: LiveData<List<VegaReceivingWithLineItems>> get() = _receiveWithLineItemLocal
    val suppplier: LiveData<List<VegaVendor>> get() = _supplier
    val location: LiveData<List<VegaSupplyStorageLocation>> get() = _location
    val material: LiveData<List<VegaPackageMaterial>> get() = _material
    val warehouse: LiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>> get() = _warehouse
    val warehouseLocal: LiveData<List<VegaReceivingWarehouse>> get() = _warehouseLocal
    val warehouseWithMtns: LiveData<VegaReceivingWarehouseWithMtns> get() = _warehouseWithMtns
    val weighBridge: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> get() = _weighBridge

    private var truckInWeightBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> =
        MutableLiveData()
    private var _truckInWeighBridge = MediatorLiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>()
    val truckInWeighBridge: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> get() = _truckInWeighBridge

    private var weighBridgeIdSource: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> =
        MutableLiveData()
    private var _weighBridgeId = MediatorLiveData<Resource<GenericReqAndResp<VegaReceiving>>>()
    val weighBridgeId: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> get() = _weighBridgeId


    private var bagSourceMtnr: LiveData<List<VegaCoffeeOffloadingBagMaterial>> = mutableLiveDataOf()
    private val _bagItemsMtnr = MediatorLiveData<List<VegaCoffeeOffloadingBagMaterial>>()
    val bagItemsMtnr: LiveData<List<VegaCoffeeOffloadingBagMaterial>> get() = _bagItemsMtnr

    private var palletResource: LiveData<Resource<GenericReqAndResp<List<VegaCameroonWeighScalePallet>>>> =
        MutableLiveData()
    private val _pallet = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCameroonWeighScalePallet>>>>()
    val pallet: LiveData<Resource<GenericReqAndResp<List<VegaCameroonWeighScalePallet>>>> get() = _pallet


    private var offloadingMtnrSource: LiveData<VegaCoffeeReceivingMtnrWithLots> =
        mutableLiveDataOf()
    private val _offloadingMtnr = MediatorLiveData<VegaCoffeeReceivingMtnrWithLots>()
    val offloadingMtnr: LiveData<VegaCoffeeReceivingMtnrWithLots> get() = _offloadingMtnr

    private var offloadingPostSource: LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>> =
        MutableLiveData()
    private val _offloadingPost = MediatorLiveData<Resource<GenericReqAndResp<VegaMtntResponse>>>()
    val offloadingPost: LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>> get() = _offloadingPost

    private var offloadingTruckResource: LiveData<Resource<List<VegaOffloadingTrucks>>> =
        MutableLiveData()
    private val _trucks = MediatorLiveData<Resource<List<VegaOffloadingTrucks>>>()
    val trucks: LiveData<Resource<List<VegaOffloadingTrucks>>> get() = _trucks

    private var multiPlantSource: LiveData<List<Plant>> = MutableLiveData()
    private val _multiPlant = MediatorLiveData<List<Plant>>()
    val multiPlant: LiveData<List<Plant>> get() = _multiPlant

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    fun getConfigItems(role: String) = viewModelScope.launch(dispatchers.main) {
        _configItems.removeSource(configSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            configSource = useCase.getConfigItems(role)
        }
        _configItems.addSource(configSource) {
            _configItems.value = it
        }
    }

    fun getTruckList(qcFlag: String, selectedPlantId: String) =
        viewModelScope.launch(dispatchers.main) {
            _trucks.removeSource(offloadingTruckResource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                offloadingTruckResource = useCase.getTrucks(qcFlag, selectedPlantId)
            }
            _trucks.addSource(offloadingTruckResource) {
                _trucks.value = it
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

    fun getSuppliers() = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCase.getSupplier()
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
        }
    }

    fun getPOList(selectedPlantId: String) = viewModelScope.launch(dispatchers.main) {
        _poList.removeSource(poListSource)
        withContext(dispatchers.io) {
            poListSource = useCase.getPOList(selectedPlantId)
        }
        _poList.addSource(poListSource) {
            _poList.value = it
        }
    }

    fun getMtntWeightDetails(wbid: String, selectedPlantId: String) = viewModelScope.launch(dispatchers.main) {
        _mtntWeight.removeSource(mtntWeightSource)
        withContext(dispatchers.io) {
            mtntWeightSource = useCase.getMtntWeightDetails(wbid, selectedPlantId)
        }
        _mtntWeight.addSource(mtntWeightSource) {
            _mtntWeight.value = it
        }
    }

    fun getPOListLocal() = viewModelScope.launch(dispatchers.main) {
        _poListLocal.removeSource(poListLocalSource)
        withContext(dispatchers.io) {
            poListLocalSource = useCase.getPOListLocal()
        }
        _poListLocal.addSource(poListLocalSource) {
            _poListLocal.value = it
        }
    }

    fun saveBagDetails(material: VegaEcuadorOffloadingBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveBagDetails(material)
        }
    }

    fun saveSelectedBatchBagDetails(material: List<VegaEcuadorOffloadingBagMaterial>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveSelectedBatchBagDetails(material)
        }
    }

    fun saveCameroonSaveBagDetails(material: List<VegaCameroonOffloadingBagMaterial>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveCameroonSaveBagDetails(material)
        }
    }

    fun deleteCameroonSavedBagDetails(batchId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteCameroonSavedBagDetails(batchId)
        }
    }

    fun deleteBagDetails(id: Int, tmpWbId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(id, tmpWbId)
        }
    }

    fun deleteSelectedBagDetails(id: Int, wbId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteSelectedBagDetails(id, wbId)
        }
    }

    fun deleteBagDetails(batchNumber: String, wbId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(batchNumber, wbId)
        }
    }
    fun deleteBagDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails()
        }
    }
    fun clearBagDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.clearBagDetails()
        }
    }

    fun getBagItems(materialCode: String?, supplierCode: String, type: String, poId: String, tmpWbId: String) =
        viewModelScope.launch(dispatchers.main) {
            _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                bagSource = useCase.getBagItems(materialCode, supplierCode, type, poId, tmpWbId)
            }
            _bagItems.addSource(bagSource) {
                _bagItems.value = it
            }
        }
    fun getWBBagItems(tmpWbId: String) =
        viewModelScope.launch(dispatchers.main) {
            _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                bagSource = useCase.getWBBagItems( tmpWbId)
            }
            _bagItems.addSource(bagSource) {
                _bagItems.value = it
            }
        }

    fun getBagItem(batchId: String) =
        viewModelScope.launch(dispatchers.main) {
            _bagItemsSavedB.removeSource(bagSourceSavedB) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                bagSourceSavedB = useCase.getBagItem(batchId)
            }
            _bagItemsSavedB.addSource(bagSourceSavedB) {
                _bagItemsSavedB.value = it
            }
        }



    fun getSavedBagItems(plantId: String) =
        viewModelScope.launch(dispatchers.main) {
            _bagItemsSaved.removeSource(bagSourceSaved) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                bagSourceSaved = useCase.getSavedBagItems(plantId)
            }
            _bagItemsSaved.addSource(bagSourceSaved) {
                _bagItemsSaved.value = it
            }
        }

    fun postEcuadorOffloadingData(
        receivingData: VegaCameroonOffloadingPost
    ) = viewModelScope.launch(dispatchers.main) {
        _receive.removeSource(receiveSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            receiveSource = useCase.postEcuadorOffloadingDetail(receivingData)
        }
        _receive.addSource(receiveSource) {
            _receive.value = it
        }
    }

    fun saveOffloading(receivingData: VegaReceiving) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveOffloading(receivingData)
        }
    }

    fun saveReceivingLineItems(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                useCase.saveReceivingLineItems(bagList)
            }
        }

    fun getOffloadingWithLineItem() = viewModelScope.launch(dispatchers.main) {
        _offloadingItemLocal.removeSource(offloadingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offloadingSource = useCase.getOffloadingWithLineItem()
        }
        _offloadingItemLocal.addSource(offloadingSource) {
            _offloadingItemLocal.value = it
        }
    }

    fun getOffloadingWithLineItemCount() = viewModelScope.launch(dispatchers.main) {
        _offloadingItemCountLocal.removeSource(offloadingCountSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offloadingCountSource = useCase.getOffloadingWithLineItemCount()
        }
        _offloadingItemCountLocal.addSource(offloadingCountSource) {
            _offloadingItemCountLocal.value = it
        }
    }

    fun updateDeletedItem(tmpWbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateDeletedItem(tmpWbId)
        }
    }

    fun updateWBToQualityAndGrnTable(tmpWbid: String, wbid: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateWBToQualityAndGrnTable(tmpWbid, wbid)
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

    fun getWeighBridgeData(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> {
        getWeighBridgeDetail()
        return weighBridge
    }

    private fun getWeighBridgeDetail() = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weightBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weightBridgeSource = useCase.getWeighBridgeDetailOnline()
        }
        _weighBridge.addSource(weightBridgeSource) {
            _weighBridge.value = it
        }
    }

    fun getLocations() = viewModelScope.launch(dispatchers.main) {
        _location.removeSource(locationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            locationSource = useCase.getLocations()
        }
        _location.addSource(locationSource) {
            _location.value = it
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

    fun postReceivingData(
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

    fun postReceivingMtnData(receivingData: VegaReceivingPost) = viewModelScope.launch(dispatchers.main) {
        _receive.removeSource(receiveSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            receiveSource = useCase.postReceivingMtnDetail(receivingData)
        }
        _receive.addSource(receiveSource) {
            _receive.value = it
        }
    }

    fun saveReceiving(receivingData: VegaReceiving) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveReceiving(receivingData)
        }
    }

    fun getReceiving() = viewModelScope.launch(dispatchers.main) {
        _receiveLocal.removeSource(receiveLocalSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            receiveLocalSource = useCase.getReceiving()
        }
        _receiveLocal.addSource(receiveLocalSource) {
            _receiveLocal.value = it
        }
    }

    fun getReceivingWithLineItem() = viewModelScope.launch(dispatchers.main) {
        _receiveWithLineItemLocal.removeSource(receiveWithLineItemLocalSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            receiveWithLineItemLocalSource = useCase.getReceivingWithLineItem()
        }
        _receiveWithLineItemLocal.addSource(receiveWithLineItemLocalSource) {
            _receiveWithLineItemLocal.value = it
        }
    }


    fun getWarehouses() = viewModelScope.launch(dispatchers.main) {
        _warehouseLocal.removeSource(warehouseSourceLocal) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            warehouseSourceLocal = useCase.getWarehouses()
        }
        _warehouseLocal.addSource(warehouseSourceLocal) {
            _warehouseLocal.value = it
        }
    }

    fun getWarehousesWithMtns(whID: String) = viewModelScope.launch(dispatchers.main) {
        _warehouseWithMtns.removeSource(warehouseWithMtnsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            warehouseWithMtnsSource = useCase.getWarehousesWithMtns(whID)
        }
        _warehouseWithMtns.addSource(warehouseWithMtnsSource) {
            _warehouseWithMtns.value = it
        }
    }

    fun fetchWarehouseWithMtns() = viewModelScope.launch(dispatchers.main) {
        _warehouse.removeSource(warehouseSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            warehouseSource = useCase.fetchWarehouseWithMtns()
        }
        _warehouse.addSource(warehouseSource) {
            _warehouse.value = it
        }
    }

    fun saveWarehouseWithMtns(it: VegaCoffeeReceivingMtnWrapper) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveWarehouseWithMtns(it)
        }
    }

    fun getTareWeight(postData: List<VegaReceiving>) = postData.sumByDouble { it.tareWeight?.toDouble() ?: 0.0 }
    fun getGrossWeight(postData: List<VegaReceiving>) = postData.sumByDouble { it.grossWeight!!.toDouble() }
    fun getNetWeight(postData: List<VegaReceiving>) = postData.sumByDouble { it.netWeight.toDouble() }
    fun getBagCount(postData: List<VegaReceiving>) = postData.sumBy { it.bagCount?.toInt() ?: 0 }


    fun updateDeletedItem(Wbid: String?, txnId: String?) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            Wbid?.let { useCase.updateDeletedItem(it, txnId) }
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
            useCase.updateReceivingFailMsg(msg, tmpWbId)
        }
    }

    fun getTruckInWeighBridgeDetail() = viewModelScope.launch(dispatchers.main) {
        _truckInWeighBridge.removeSource(truckInWeightBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            truckInWeightBridgeSource = useCase.getTruckInWeighBridgeDetailOnline()
        }
        _truckInWeighBridge.addSource(truckInWeightBridgeSource) {
            _truckInWeighBridge.value = it
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

    fun getBagItems(batchNumber: String?, mtnNumber: String) = viewModelScope.launch(dispatchers.main) {
        _bagItemsMtnr.removeSource(bagSourceMtnr) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSourceMtnr = useCase.getBagItems(batchNumber, mtnNumber)
        }
        _bagItemsMtnr.addSource(bagSourceMtnr) {
            _bagItemsMtnr.value = it
        }
    }

    fun saveBagDetails(bagMaterial: VegaCoffeeOffloadingBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveBagDetails(bagMaterial)
        }
    }

    fun deleteBagDetails(id: Int) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(id)
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

    fun getOBDDetails(deliveryNumber: String) = viewModelScope.launch(dispatchers.main) {
        _offloadingMtnr.removeSource(offloadingMtnrSource)
        withContext(dispatchers.io) {
            offloadingMtnrSource = useCase.getOBDDetails(deliveryNumber)
        }
        _offloadingMtnr.addSource(offloadingMtnrSource) {
            _offloadingMtnr.value = it
        }
    }

    fun saveMtnrReceivingLots(vegaCoffeeReceivingData: VegaCoffeeReceiving, lot: VegaCoffeeReceiveLots) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveMtnrReceivingLots(vegaCoffeeReceivingData, lot)
            }
        }

    fun postOffloadingDetail(vegaOffloadingPost: VegaCameroonOffloadingPostRequest) =
        viewModelScope.launch(dispatchers.main) {
            _offloadingPost.removeSource(offloadingPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                offloadingPostSource = useCase.postOffloadingDetail(vegaOffloadingPost)
            }
            _offloadingPost.addSource(offloadingPostSource) {
                _offloadingPost.value = it
            }
        }

    fun updateStatus(mtnNumber: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateStatus(mtnNumber)
        }
    }


}
