package com.olam.warehouse.vegax.offloadingnigeria.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaMtntResponse
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuaOffloadingWithLineItems
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.offloadingnigeria.data.domain.usecase.VegaNigeriaOffloadingUseCase
import com.olam.warehouse.vegax.offloadingnigeria.data.domain.usecase.model.VegaCocoaWeighScalePallet
import com.olam.warehouse.vegax.offloadingnigeria.data.domain.usecase.model.VegaDMSImageResponse
import com.olam.warehouse.vegax.offloadingnigeria.data.domain.usecase.model.VegaNigeriaOffloadingPostRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Created by Keerthi Santhanam on 23/6/2020.
 */
class VegaNigeriaOffloadingViewModel(
    private val useCase: VegaNigeriaOffloadingUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var offloadingTruckResource: LiveData<Resource<List<VegaOffloadingTrucks>>> =
        MutableLiveData()
    private val _trucks = MediatorLiveData<Resource<List<VegaOffloadingTrucks>>>()
    val trucks: LiveData<Resource<List<VegaOffloadingTrucks>>> get() = _trucks

    private var bagSourceMtnr: LiveData<List<VegaCoffeeOffloadingBagMaterial>> = mutableLiveDataOf()
    private val _bagItemsMtnr = MediatorLiveData<List<VegaCoffeeOffloadingBagMaterial>>()
    val bagItemsMtnr: LiveData<List<VegaCoffeeOffloadingBagMaterial>> get() = _bagItemsMtnr


    private var palletResourceWs: LiveData<Resource<GenericReqAndResp<List<VegaCocoaWeighScalePallet>>>> =
        MutableLiveData()
    private val _palletWs =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaWeighScalePallet>>>>()
    val palletWs: LiveData<Resource<GenericReqAndResp<List<VegaCocoaWeighScalePallet>>>> get() = _palletWs

    private var imageResourceWs: LiveData<Resource<GenericReqAndResp<List<VegaDMSImageResponse>>>> =
        MutableLiveData()
    private val _imageWs =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaDMSImageResponse>>>>()
    val imageWs: LiveData<Resource<GenericReqAndResp<List<VegaDMSImageResponse>>>> get() = _imageWs

    private var qcweighBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaOffloadingTrucks>>>> =
        MutableLiveData()
    private val _qcweighBridge =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaOffloadingTrucks>>>>()
    val qcweighBridge: LiveData<Resource<GenericReqAndResp<List<VegaOffloadingTrucks>>>> get() = _qcweighBridge

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val supplier: LiveData<List<VegaVendor>> get() = _supplier

    private var weighBridgeIdSource: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> =
        MutableLiveData()
    private var _weighBridgeId = MediatorLiveData<Resource<GenericReqAndResp<VegaReceiving>>>()
    val weighBridgeId: LiveData<Resource<GenericReqAndResp<VegaReceiving>>> get() = _weighBridgeId

    private var warehouseSource: LiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>> =
        MutableLiveData()
    private val _warehouse =
        MediatorLiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>>()
    val warehouse: LiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>> get() = _warehouse

    var vegaCoffeeReceivingData = VegaCoffeeReceiving()

    private var locationSource: LiveData<List<VegaSupplyStorageLocation>> = MutableLiveData()
    private val _location = MediatorLiveData<List<VegaSupplyStorageLocation>>()
    val location: LiveData<List<VegaSupplyStorageLocation>> get() = _location

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    private var poListSource: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> =
        MutableLiveData()
    private val _poList =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>()
    val poList: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> get() = _poList

    private var poListLocalSource: LiveData<List<VegaEcuadorPurchaseOrder>> =
        MutableLiveData()
    private val _poListLocal = MediatorLiveData<List<VegaEcuadorPurchaseOrder>>()
    val poListLocal: LiveData<List<VegaEcuadorPurchaseOrder>> get() = _poListLocal

    private var offloadingPostSource: LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>> =
        MutableLiveData()
    private val _offloadingPost = MediatorLiveData<Resource<GenericReqAndResp<VegaMtntResponse>>>()
    val offloadingPost: LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>> get() = _offloadingPost

    private var bagSource: LiveData<List<VegaEcuadorOffloadingBagMaterial>> = mutableLiveDataOf()
    private val _bagItems = MediatorLiveData<List<VegaEcuadorOffloadingBagMaterial>>()
    val bagItems: LiveData<List<VegaEcuadorOffloadingBagMaterial>> get() = _bagItems

    private var receiveSource: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> =
        MutableLiveData()
    private val _receive = MediatorLiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>()
    val receive: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> get() = _receive

    private var offloadingSource: LiveData<List<VegaEcuaOffloadingWithLineItems>> =
        MutableLiveData()
    private val _offloadingItemLocal = MediatorLiveData<List<VegaEcuaOffloadingWithLineItems>>()
    val offloadingItemLocal: LiveData<List<VegaEcuaOffloadingWithLineItems>> get() = _offloadingItemLocal

    private var offloadingMtnrSource: LiveData<VegaCoffeeReceivingMtnrWithLots> =
        mutableLiveDataOf()
    private val _offloadingMtnr = MediatorLiveData<VegaCoffeeReceivingMtnrWithLots>()
    val offloadingMtnr: LiveData<VegaCoffeeReceivingMtnrWithLots> get() = _offloadingMtnr

    private var offloadingCountSource: LiveData<List<VegaEcuaOffloadingWithLineItems>> =
        MutableLiveData()
    private val _offloadingItemCountLocal =
        MediatorLiveData<List<VegaEcuaOffloadingWithLineItems>>()
    val offloadingItemCountLocal: LiveData<List<VegaEcuaOffloadingWithLineItems>> get() = _offloadingItemCountLocal

    private var productNameSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _productName = MediatorLiveData<List<VegaMaterial>>()
    val productName: LiveData<List<VegaMaterial>> get() = _productName

    fun getProductByName(materialName:String) = viewModelScope.launch(dispatchers.main) {
        _productName.removeSource(productNameSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            productNameSource = useCase.getProductByName(materialName)
        }
        _productName.addSource(productNameSource) {
            _productName.value = it
        }
    }

    fun getTruckList() = viewModelScope.launch(dispatchers.main) {
        _trucks.removeSource(offloadingTruckResource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offloadingTruckResource = useCase.getTrucks()
        }
        _trucks.addSource(offloadingTruckResource) {
            _trucks.value = it
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

    fun getProducts() = viewModelScope.launch(dispatchers.main) {
        _product.removeSource(productSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            productSource = useCase.getProducts()
        }
        _product.addSource(productSource) {
            _product.value = it
        }
    }

    fun getQCWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaOffloadingTrucks>>>> {
        fetchQcWeighBridgeList(selectedPlantId)
        return qcweighBridge
    }

    private fun fetchQcWeighBridgeList(selectedPlantId: String) =
        viewModelScope.launch(dispatchers.main) {
            _qcweighBridge.removeSource(qcweighBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qcweighBridgeSource = useCase.fetchQcWeighBridgeList(selectedPlantId)
            }
            _qcweighBridge.addSource(qcweighBridgeSource) {
                _qcweighBridge.value = it
            }
        }

    fun saveMtnrReceivingLots(
        vegaCoffeeReceivingData: VegaCoffeeReceiving,
        lot: VegaCoffeeReceiveLots
    ) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveMtnrReceivingLots(vegaCoffeeReceivingData, lot)
            }
        }

    fun updateStatus(mtnNumber: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateStatus(mtnNumber)
        }
    }

    fun getBagItemsNew(batchNumber: String?, mtnNumber: String) =
        viewModelScope.launch(dispatchers.main) {
            _bagItemsMtnr.removeSource(bagSourceMtnr) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                bagSourceMtnr = useCase.getBagItemsNew(batchNumber, mtnNumber)
            }
            _bagItemsMtnr.addSource(bagSourceMtnr) {
                _bagItemsMtnr.value = it
            }
        }

    fun getPalletInfoWs(batchNumber: String, material: String) =
        viewModelScope.launch(dispatchers.main) {
            _palletWs.removeSource(palletResourceWs)
            withContext(dispatchers.io) {
                palletResourceWs = useCase.getPalletDetailsWs(batchNumber, material)
            }
            _palletWs.addSource(palletResourceWs) {
                _palletWs.value = it
            }
        }

    fun getDMSUploadedImages(wbId: String, werks: String) =
        viewModelScope.launch(dispatchers.main) {
            _imageWs.removeSource(imageResourceWs)
            withContext(dispatchers.io) {
                imageResourceWs = useCase.getDMSUploadedImages(wbId, werks)
            }
            _imageWs.addSource(imageResourceWs) {
                _imageWs.value = it
            }
        }

    fun postOffloadingDetail(vegaOffloadingPost: VegaNigeriaOffloadingPostRequest) =
        viewModelScope.launch(dispatchers.main) {
            _offloadingPost.removeSource(offloadingPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                offloadingPostSource = useCase.postOffloadingDetail(vegaOffloadingPost)
            }
            _offloadingPost.addSource(offloadingPostSource) {
                _offloadingPost.value = it
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

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = useCase.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
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

    fun getSuppliers() = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCase.getSupplier()
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
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

    fun getPOList() = viewModelScope.launch(dispatchers.main) {
        _poList.removeSource(poListSource)
        withContext(dispatchers.io) {
            poListSource = useCase.getPOList()
        }
        _poList.addSource(poListSource) {
            _poList.value = it
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

    fun saveBagDetailsNew(bagMaterial: VegaCoffeeOffloadingBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveBagDetailsNew(bagMaterial)
        }
    }

    fun deleteBagDetails(id: Int, tmpWbId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(id, tmpWbId)
        }
    }

    fun deleteBagDetailsNew(id: Int) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetailsNew(id)
        }
    }

    fun clearBagDetails() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.clearBagDetails()
        }
    }

    fun getBagItems(
        materialCode: String?,
        supplierCode: String,
        type: String,
        poId: String,
        tmpWbId: String
    ) =
        viewModelScope.launch(dispatchers.main) {
            _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                bagSource = useCase.getBagItems(materialCode, supplierCode, type, poId, tmpWbId)
            }
            _bagItems.addSource(bagSource) {
                _bagItems.value = it
            }
        }

    fun postEcuadorOffloadingData(
        receivingData: VegaReceivingPost
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

    private var featureMasterSource: LiveData<List<VegaFeatureMaster>> = MutableLiveData()
    private val _featureMaster = MediatorLiveData<List<VegaFeatureMaster>>()
    val featureMaster: LiveData<List<VegaFeatureMaster>> get() = _featureMaster

    fun getFeatureMaster(module: String) = viewModelScope.launch(dispatchers.main) {
        _featureMaster.removeSource(featureMasterSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            featureMasterSource = useCase.getFeatureMaster(module)
        }
        _featureMaster.addSource(featureMasterSource) {
            _featureMaster.value = it
        }
    }
}
