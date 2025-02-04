package com.olam.warehouse.vegax.offloadingcoffee.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaMtntResponse
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.offloadingcoffee.data.domain.model.VegaCoffeeOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingcoffee.data.domain.model.VegaCoffeeOffloadingSupplierPostRequest
import com.olam.warehouse.vegax.offloadingcoffee.data.domain.model.VegaCoffeeWeighScalePallet
import com.olam.warehouse.vegax.offloadingcoffee.data.domain.usecase.VegaCoffeeOffloadingUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class VegaCoffeeOffloadingViewModel(
    private val useCase: VegaCoffeeOffloadingUseCase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {

    var vegaCoffeeReceivingData = VegaCoffeeReceiving()

    private var receiveSource: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> =
        MutableLiveData()
    private var receiveLocalSource: LiveData<List<VegaReceiving>> = MutableLiveData()
    private var receiveWithLineItemLocalSource: LiveData<List<VegaReceivingWithLineItems>> =
        MutableLiveData()
    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private var locationSource: LiveData<List<VegaSupplyStorageLocation>> = MutableLiveData()
    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    private var warehouseSource: LiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>> =
        MutableLiveData()
    private var storageLocationSource: LiveData<List<VegaStorageLocation>> = MutableLiveData()

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()

    private var weightBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeReceiving>>>> =
        MutableLiveData()
    private var warehouseSourceLocal: LiveData<List<VegaReceivingWarehouse>> = MutableLiveData()
    private var warehouseWithMtnsSource: LiveData<VegaReceivingWarehouseWithMtns> =
        MutableLiveData()
    private val _receive = MediatorLiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>()
    private val _receiveLocal = MediatorLiveData<List<VegaReceiving>>()
    private val _receiveWithLineItemLocal = MediatorLiveData<List<VegaReceivingWithLineItems>>()
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    private val _location = MediatorLiveData<List<VegaSupplyStorageLocation>>()
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()
    private val _warehouse =
        MediatorLiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>>()
    private val _warehouseLocal = MediatorLiveData<List<VegaReceivingWarehouse>>()
    private val _warehouseWithMtns = MediatorLiveData<VegaReceivingWarehouseWithMtns>()
    private var _weighBridge =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeReceiving>>>>()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    private val _storageLocation = MediatorLiveData<List<VegaStorageLocation>>()

    val receive: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> get() = _receive
    val receiveLocal: LiveData<List<VegaReceiving>> get() = _receiveLocal
    val receiveWithLineItemLocal: LiveData<List<VegaReceivingWithLineItems>> get() = _receiveWithLineItemLocal
    val product: LiveData<List<VegaMaterial>> get() = _product
    val suppplier: LiveData<List<VegaVendor>> get() = _supplier
    val location: LiveData<List<VegaSupplyStorageLocation>> get() = _location
    val material: LiveData<List<VegaPackageMaterial>> get() = _material
    val warehouse: LiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>> get() = _warehouse
    val warehouseLocal: LiveData<List<VegaReceivingWarehouse>> get() = _warehouseLocal
    val warehouseWithMtns: LiveData<VegaReceivingWarehouseWithMtns> get() = _warehouseWithMtns
    val weighBridge: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeReceiving>>>> get() = _weighBridge
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation
    val storeLocation: LiveData<List<VegaStorageLocation>> get() = _storageLocation

    private var truckInWeightBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeReceiving>>>> =
        MutableLiveData()
    private var _truckInWeighBridge = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeReceiving>>>>()
    val truckInWeighBridge: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeReceiving>>>> get() = _truckInWeighBridge

    private var weighBridgeIdSource: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> =
        MutableLiveData()
    private var _weighBridgeId = MediatorLiveData<Resource<GenericReqAndResp<VegaReceiving>>>()
    val weighBridgeId: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> get() = _weighBridgeId


    private var bagSource: LiveData<List<VegaCoffeeOffloadingBagMaterial>> = mutableLiveDataOf()
    private val _bagItems = MediatorLiveData<List<VegaCoffeeOffloadingBagMaterial>>()
    val bagItems: LiveData<List<VegaCoffeeOffloadingBagMaterial>> get() = _bagItems

    private var palletResource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeWeighScalePallet>>>> =
        MutableLiveData()
    private val _pallet = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeWeighScalePallet>>>>()
    val pallet: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeWeighScalePallet>>>> get() = _pallet


    private var offloadingMtnrSource: LiveData<VegaCoffeeReceivingMtnrWithLots> = mutableLiveDataOf()
    private val _offloadingMtnr = MediatorLiveData<VegaCoffeeReceivingMtnrWithLots>()
    val offloadingMtnr: LiveData<VegaCoffeeReceivingMtnrWithLots> get() = _offloadingMtnr

    private var offloadingPostSource: LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>> =
        MutableLiveData()
    private val _offloadingPost = MediatorLiveData<Resource<GenericReqAndResp<VegaMtntResponse>>>()
    val offloadingPost: LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>> get() = _offloadingPost

    private var poListSource: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> =
        MutableLiveData()
    private val _poList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>()
    val poList: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> get() = _poList

    //for qualitative
    private var qualityGetSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist

    /*fun getWeighBridgeData(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> {
        getWeighBridgeDetail()
        return weighBridge
    }*/

    fun getWeighBridgeDetail() = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weightBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weightBridgeSource = useCase.getWeighBridgeDetailOnline()
        }
        _weighBridge.addSource(weightBridgeSource) {
            _weighBridge.value = it
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

    fun getLocations() = viewModelScope.launch(dispatchers.main) {
        _location.removeSource(locationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            locationSource = useCase.getLocations()
        }
        _location.addSource(locationSource) {
            _location.value = it
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

    //used storageLocation for receiving plant for nicaragua coffee
    fun getStorageLocations() = viewModelScope.launch(dispatchers.main) {
        _storageLocation.removeSource(storageLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            storageLocationSource = useCase.getStorageLocations()
        }
        _storageLocation.addSource(storageLocationSource) {
            _storageLocation.value = it
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

    fun getQualityParams(materialId: String, isValueExist: Boolean?, wbId: String?) =
        viewModelScope.launch(dispatchers.main) {
            _qualitylist.removeSource(qualityGetSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityGetSource = useCase.getQualityParams(materialId, isValueExist, wbId)
            }
            _qualitylist.addSource(qualityGetSource) {
                _qualitylist.value = it
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
    /*fun saveReceivingLineItems(postData: MutableList<VegaReceiving>) = viewModelScope.launch(dispatchers.main) {
        val lineItems = getLineItemFromReceiving(postData)
        withContext(dispatchers.io) {
            useCase.saveReceivingLineItems(lineItems)
        }
    }*/

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

    fun getTruckInWeighBridgeDetail(isWeighScale: Boolean) = viewModelScope.launch(dispatchers.main) {
        _truckInWeighBridge.removeSource(truckInWeightBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            truckInWeightBridgeSource = useCase.getTruckInWeighBridgeDetailOnline(isWeighScale)
        }
        _truckInWeighBridge.addSource(truckInWeightBridgeSource) {
            _truckInWeighBridge.value = it
        }
    }

    fun getWeighBridgeIdDetail(wbid: String, isWeighScale: Boolean) = viewModelScope.launch(dispatchers.main) {
        _weighBridgeId.removeSource(weighBridgeIdSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeIdSource = useCase.getWeighBridgeIdDetail(wbid, isWeighScale)
        }
        _weighBridgeId.addSource(weighBridgeIdSource) {
            _weighBridgeId.value = it
        }
    }

    fun getBagItems(batchNumber: String?, mtnNumber: String) = viewModelScope.launch(dispatchers.main) {
        _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSource = useCase.getBagItems(batchNumber, mtnNumber)
        }
        _bagItems.addSource(bagSource) {
            _bagItems.value = it
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

    fun postOffloadingDetail(vegaOffloadingPost: VegaCoffeeOffloadingPostRequest) =
        viewModelScope.launch(dispatchers.main) {
            _offloadingPost.removeSource(offloadingPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                offloadingPostSource = useCase.postOffloadingDetail(vegaOffloadingPost)
            }
            _offloadingPost.addSource(offloadingPostSource) {
                _offloadingPost.value = it
            }
        }

    fun postOffloadingSupplierDetail(vegaOffloadingPost: VegaCoffeeOffloadingSupplierPostRequest) =
        viewModelScope.launch(dispatchers.main) {
            _offloadingPost.removeSource(offloadingPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                offloadingPostSource = useCase.postOffloadingSupplierDetail(vegaOffloadingPost)
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

    fun getPOList() = viewModelScope.launch(dispatchers.main) {
        _poList.removeSource(poListSource)
        withContext(dispatchers.io) {
            poListSource = useCase.getPOList()
        }
        _poList.addSource(poListSource) {
            _poList.value = it
        }
    }

}
