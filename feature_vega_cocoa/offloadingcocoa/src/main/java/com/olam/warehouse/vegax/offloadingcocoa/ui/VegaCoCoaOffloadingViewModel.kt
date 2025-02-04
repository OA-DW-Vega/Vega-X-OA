package com.olam.warehouse.vegax.offloadingcocoa.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoCoaReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.VegaCoCoaReceivingMtnrWithLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.model.VegaCoCoaOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.model.VegaCoCoaWeighScalePallet
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.usecase.VegaCoCoaOffloadingUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class VegaCoCoaOffloadingViewModel(
    private val useCase: VegaCoCoaOffloadingUseCase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {

    var vegaCoCoaReceivingData = VegaCoCoaReceiving()

    private var receiveSource: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> = MutableLiveData()
    private var receiveLocalSource: LiveData<List<VegaReceiving>> = MutableLiveData()
    private var receiveWithLineItemLocalSource: LiveData<List<VegaReceivingWithLineItems>> = MutableLiveData()
    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private var locationSource: LiveData<List<VegaSupplyStorageLocation>> = MutableLiveData()
    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    private var warehouseSource: LiveData<Resource<GenericReqAndResp<VegaCoCoaReceivingMtnWrapper>>> =
        MutableLiveData()
    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private var plantSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private var offlineStorageLocationsSource: LiveData<List<VegaCoCoaStorageLocation>> = MutableLiveData()

    private var weightBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> = MutableLiveData()
    private var warehouseSourceLocal: LiveData<List<VegaReceivingWarehouse>> = MutableLiveData()
    private var warehouseWithMtnsSource: LiveData<VegaReceivingWarehouseWithMtns> = MutableLiveData()
    private val _receive = MediatorLiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>()
    private val _receiveLocal = MediatorLiveData<List<VegaReceiving>>()
    private val _receiveWithLineItemLocal = MediatorLiveData<List<VegaReceivingWithLineItems>>()
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    private val _location = MediatorLiveData<List<VegaSupplyStorageLocation>>()
    private val _plant = MediatorLiveData<List<VegaCustomStLocation>>()
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()
    private val _warehouse = MediatorLiveData<Resource<GenericReqAndResp<VegaCoCoaReceivingMtnWrapper>>>()
    private val _warehouseLocal = MediatorLiveData<List<VegaReceivingWarehouse>>()
    private val _warehouseWithMtns = MediatorLiveData<VegaReceivingWarehouseWithMtns>()
    private var _weighBridge = MediatorLiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    private val _offlineStorageLocations = MediatorLiveData<List<VegaCoCoaStorageLocation>>()

    val receive: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> get() = _receive
    val receiveLocal: LiveData<List<VegaReceiving>> get() = _receiveLocal
    val receiveWithLineItemLocal: LiveData<List<VegaReceivingWithLineItems>> get() = _receiveWithLineItemLocal
    val product: LiveData<List<VegaMaterial>> get() = _product
    val suppplier: LiveData<List<VegaVendor>> get() = _supplier
    val location: LiveData<List<VegaSupplyStorageLocation>> get() = _location
    val plant: LiveData<List<VegaCustomStLocation>> get() = _plant
    val material: LiveData<List<VegaPackageMaterial>> get() = _material
    val warehouse: LiveData<Resource<GenericReqAndResp<VegaCoCoaReceivingMtnWrapper>>> get() = _warehouse
    val warehouseLocal: LiveData<List<VegaReceivingWarehouse>> get() = _warehouseLocal
    val warehouseWithMtns: LiveData<VegaReceivingWarehouseWithMtns> get() = _warehouseWithMtns
    val weighBridge: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> get() = _weighBridge
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation
    val offlineStorageLocations: LiveData<List<VegaCoCoaStorageLocation>> get() = _offlineStorageLocations

    private var truckInWeightBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> =
        MutableLiveData()
    private var _truckInWeighBridge = MediatorLiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>()
    val truckInWeighBridge: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> get() = _truckInWeighBridge

    private var weighBridgeIdSource: LiveData<Resource<GenericReqAndResp<VegaCoCoaQualityWBDetail>>> =
        MutableLiveData()
    private var _weighBridgeId = MediatorLiveData<Resource<GenericReqAndResp<VegaCoCoaQualityWBDetail>>>()
    val weighBridgeId: LiveData<Resource<GenericReqAndResp<VegaCoCoaQualityWBDetail>>> get() = _weighBridgeId


    private var bagSource: LiveData<List<VegaCoCoaOffloadingBagMaterial>> = mutableLiveDataOf()
    private val _bagItems = MediatorLiveData<List<VegaCoCoaOffloadingBagMaterial>>()
    val bagItems: LiveData<List<VegaCoCoaOffloadingBagMaterial>> get() = _bagItems

    private var palletResource: LiveData<Resource<GenericReqAndResp<List<VegaCoCoaWeighScalePallet>>>> =
        MutableLiveData()
    private val _pallet = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoCoaWeighScalePallet>>>>()
    val pallet: LiveData<Resource<GenericReqAndResp<List<VegaCoCoaWeighScalePallet>>>> get() = _pallet


    private var offloadingMtnrSource: LiveData<VegaCoCoaReceivingMtnrWithLots> = mutableLiveDataOf()
    private val _offloadingMtnr = MediatorLiveData<VegaCoCoaReceivingMtnrWithLots>()
    val offloadingMtnr: LiveData<VegaCoCoaReceivingMtnrWithLots> get() = _offloadingMtnr

    private var offlineMtnrSource: LiveData<List<VegaCoCoaReceivingMtnrWithLots>> = mutableLiveDataOf()
    private val _offlineMtnr = MediatorLiveData<List<VegaCoCoaReceivingMtnrWithLots>>()
    val offlineMtnr: LiveData<List<VegaCoCoaReceivingMtnrWithLots>> get() = _offlineMtnr

    private var offlinePendingSource: LiveData<List<VegaCoCoaReceivingMtnrWithLots>> = mutableLiveDataOf()
    private val _offlinePendingMtnr = MediatorLiveData<List<VegaCoCoaReceivingMtnrWithLots>>()
    val offlinePendingMtnr: LiveData<List<VegaCoCoaReceivingMtnrWithLots>> get() = _offlinePendingMtnr

    private var offloadingPostSource: LiveData<Resource<GenericReqAndResp<VegaCoCoaOffloadingPostRequest>>> =
        MutableLiveData()
    private val _offloadingPost = MediatorLiveData<Resource<GenericReqAndResp<VegaCoCoaOffloadingPostRequest>>>()
    val offloadingPost: LiveData<Resource<GenericReqAndResp<VegaCoCoaOffloadingPostRequest>>> get() = _offloadingPost

    private var vendorSource: LiveData<VegaVendor> = MutableLiveData()
    private val _vendorDetails = MediatorLiveData<VegaVendor>()
    val vendorDetailList: LiveData<VegaVendor> get() = _vendorDetails

    private var thirdPartyMaterialSource: LiveData<List<VegaMaterial>> = MutableLiveData()
    private val _thirdpartymaterial = MediatorLiveData<List<VegaMaterial>>()
    val thirdPartyMaterialList: LiveData<List<VegaMaterial>> get() = _thirdpartymaterial

    private var mtnrSource: LiveData<List<VegaReceivingMtn>> = MutableLiveData()
    private var lotSource: LiveData<List<VegaReceivingMtnLots>> = MutableLiveData()
    private var wbSource: LiveData<VegaCoCoaQualityWBDetail> = MutableLiveData()


    private val _wbDetailsLocal = MediatorLiveData<VegaCoCoaQualityWBDetail>()
    private val _mtnrLocal = MediatorLiveData<List<VegaReceivingMtn>>()
    private val _lotLocal = MediatorLiveData<List<VegaReceivingMtnLots>>()

    val wbDetailsLocal: LiveData<VegaCoCoaQualityWBDetail> get() = _wbDetailsLocal
    val mtnrLocal: LiveData<List<VegaReceivingMtn>> get() = _mtnrLocal
    val lotLocal: LiveData<List<VegaReceivingMtnLots>> get() = _lotLocal

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

    fun getPlants() = viewModelScope.launch(dispatchers.main) {
        _plant.removeSource(plantSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            plantSource = useCase.getPlants()
        }
        _plant.addSource(plantSource) {
            _plant.value = it
        }
    }

    fun getOfflineStorageLocations() = viewModelScope.launch(dispatchers.main) {
        _offlineStorageLocations.removeSource(offlineStorageLocationsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offlineStorageLocationsSource = useCase.getOfflineStorageLocations()
        }
        _offlineStorageLocations.addSource(offlineStorageLocationsSource) {
            _offlineStorageLocations.value = it
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

    fun saveReceiving(receivingData: VegaCoCoaReceiving) = viewModelScope.launch(dispatchers.main) {
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

    fun saveWarehouseWithMtns(it: VegaCoCoaReceivingMtnWrapper) = viewModelScope.launch(dispatchers.main) {
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

    fun updateWeight(grossWeight: String, truckoutWeight: String, netWeight: String, deliveryNumber: String) =
        viewModelScope.launch(dispatchers.main) {
            withContext(dispatchers.io) {
                useCase.updateWeight(grossWeight, truckoutWeight, netWeight, deliveryNumber)
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
            weighBridgeIdSource = useCase.geCocoaWeighBridgeIdDetail(wbid)
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

    fun saveBagDetails(bagMaterial: VegaCoCoaOffloadingBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveBagDetails(bagMaterial)
        }
    }

    fun deleteBagDetails(batchNumber: String, mtnNumber: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(batchNumber, mtnNumber)
        }
    }

    fun deleteBagDetails(bagtype: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(bagtype)
        }
    }

    fun deleteBagDetailsById(id: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetailsById(id)
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
        Log.e("Delivery No", deliveryNumber)
        _offloadingMtnr.removeSource(offloadingMtnrSource)
        withContext(dispatchers.io) {
            offloadingMtnrSource = useCase.getOBDDetails(deliveryNumber)
        }
        _offloadingMtnr.addSource(offloadingMtnrSource) {
            _offloadingMtnr.value = it
        }
    }

    fun getTransactions() = viewModelScope.launch(dispatchers.main) {
        _offlineMtnr.removeSource(offlineMtnrSource)
        withContext(dispatchers.io) {
            offlineMtnrSource = useCase.getTransactions()
        }
        _offlineMtnr.addSource(offlineMtnrSource) {
            _offlineMtnr.value = it
        }
    }

    fun getPendingList() = viewModelScope.launch(dispatchers.main) {
        _offlinePendingMtnr.removeSource(offlinePendingSource)
        withContext(dispatchers.io) {
            offlinePendingSource = useCase.getPendingList()
        }
        _offlinePendingMtnr.addSource(offlinePendingSource) {
            _offlinePendingMtnr.value = it
        }
    }

    fun getMtnrReceivingLots(vegaegaCoCoaReceivingData: VegaCoCoaReceiving, lot: VegaCoCoaReceiveLots) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveMtnrReceivingLots(vegaegaCoCoaReceivingData, lot)
            }
        }

    fun saveMtnrReceivingLots(vegaegaCoCoaReceivingData: VegaCoCoaReceiving, lot: VegaCoCoaReceiveLots) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveMtnrReceivingLots(vegaegaCoCoaReceivingData, lot)
            }
        }

    fun postOffloadingDetail(vegaOffloadingPost: VegaCoCoaOffloadingPostRequest) =
        viewModelScope.launch(dispatchers.main) {
            _offloadingPost.removeSource(offloadingPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                offloadingPostSource = useCase.postOffloading(vegaOffloadingPost)
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

    fun deleteStatus(grnNumber: String, mtnNumber: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteStatus(grnNumber, mtnNumber)
        }
    }

    fun getThirdPartyMaterials() = viewModelScope.launch(dispatchers.main) {
        _thirdpartymaterial.removeSource(thirdPartyMaterialSource) // We make sure there is only one source of livedata (allowing us properly refresh)thi
        withContext(dispatchers.io) {
            thirdPartyMaterialSource = useCase.getThirdPartyMaterials()
        }
        _thirdpartymaterial.addSource(thirdPartyMaterialSource) {
            _thirdpartymaterial.value = it
        }
    }

    fun getVendorInfo(vendorid: String) = viewModelScope.launch(dispatchers.main) {
        _vendorDetails.removeSource(vendorSource) // We make sure there is only one source of livedata (allowing us properly refresh)thi
        withContext(dispatchers.io) {
            vendorSource = useCase.getVendorInfo(vendorid)
        }
        _vendorDetails.addSource(vendorSource) {
            _vendorDetails.value = it
        }
    }

    fun getMTNRs() = viewModelScope.launch(dispatchers.main) {
        _mtnrLocal.removeSource(mtnrSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            mtnrSource = useCase.getMTNRs()
        }
        _mtnrLocal.addSource(mtnrSource) {
            _mtnrLocal.value = it
        }
    }

    fun getOfflineLots() = viewModelScope.launch(dispatchers.main) {
        _lotLocal.removeSource(lotSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            lotSource = useCase.getOfflineLots()
        }
        _lotLocal.addSource(lotSource) {
            _lotLocal.value = it
        }
    }

    fun getWBDetails(wbid: String) = viewModelScope.launch(dispatchers.main) {
        Log.e("wb", "wb ".plus(wbid))
        _wbDetailsLocal.removeSource(wbSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            wbSource = useCase.getCoCoaWBDetails(wbid)
        }
        _wbDetailsLocal.addSource(wbSource) {
            _wbDetailsLocal.value = it
        }
    }

}
