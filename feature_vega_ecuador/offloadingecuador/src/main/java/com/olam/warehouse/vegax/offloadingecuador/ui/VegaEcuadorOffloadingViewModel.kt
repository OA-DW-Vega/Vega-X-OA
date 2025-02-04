package com.olam.warehouse.vegax.offloadingecuador.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuaOffloadingWithLineItems
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.offloadingecuador.data.domain.usecase.VegaEcuadorOffloadingUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Created by Keerthi Santhanam on 23/6/2020.
 */
class VegaEcuadorOffloadingViewModel(
    private val useCase: VegaEcuadorOffloadingUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

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

    private var offloadingCountSource: LiveData<List<VegaEcuaOffloadingWithLineItems>> = MutableLiveData()
    private val _offloadingItemCountLocal = MediatorLiveData<List<VegaEcuaOffloadingWithLineItems>>()
    val offloadingItemCountLocal: LiveData<List<VegaEcuaOffloadingWithLineItems>> get() = _offloadingItemCountLocal

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

    fun deleteBagDetails(id: Int, tmpWbId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(id, tmpWbId)
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
}
