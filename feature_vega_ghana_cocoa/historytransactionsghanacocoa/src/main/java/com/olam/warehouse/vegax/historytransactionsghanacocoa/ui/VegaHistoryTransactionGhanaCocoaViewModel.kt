package com.olam.warehouse.vegax.historytransactionsghanacocoa.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaGRNHistoryTranxResponse
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaHistoryTranxMtnr
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaMtntHistoryTranxResponse
import com.olam.warehouse.vegax.historytransactionsghanacocoa.data.domain.usecase.VegaHistoryTransactionsGhanaCocoaUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaHistoryTransactionGhanaCocoaViewModel(
    private val  useCaseCocoa: VegaHistoryTransactionsGhanaCocoaUseCase,
    private val dispatchers: AppDispatchers
    ) : BaseViewModel() {

    private var historyTranxMtntSource: LiveData<Resource<GenericReqAndResp<VegaGhanaCocoaMtntHistoryTranxResponse>>> = MutableLiveData()
    private val _historyTranxMtnt = MediatorLiveData<Resource<GenericReqAndResp<VegaGhanaCocoaMtntHistoryTranxResponse>>>()
    val historyTranxMtnt: LiveData<Resource<GenericReqAndResp<VegaGhanaCocoaMtntHistoryTranxResponse>>> get() = _historyTranxMtnt

    fun getHistoryTranxMtnt(endDate: String, plant: String, startDate: String) = viewModelScope.launch(dispatchers.main) {
        _historyTranxMtnt.removeSource(historyTranxMtnt)
        withContext(dispatchers.io) {
            historyTranxMtntSource = useCaseCocoa.getHistoryTranxMtnt(endDate, plant, startDate)
        }
        _historyTranxMtnt.addSource(historyTranxMtntSource){
            _historyTranxMtnt.value = it
        }
    }

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val supplier: LiveData<List<VegaVendor>> get() = _supplier

    private var historyTranxSource: LiveData<Resource<GenericReqAndResp<VegaGhanaCocoaGRNHistoryTranxResponse>>> = MutableLiveData()
    private val _historyTranx = MediatorLiveData<Resource<GenericReqAndResp<VegaGhanaCocoaGRNHistoryTranxResponse>>>()
    val historytranx: LiveData<Resource<GenericReqAndResp<VegaGhanaCocoaGRNHistoryTranxResponse>>> get() = _historyTranx

    private var historyTranxMtnrSource: LiveData<Resource<GenericReqAndResp<VegaGhanaCocoaHistoryTranxMtnr>>> = MutableLiveData()
    private val _historyTransMtnr = MediatorLiveData<Resource<GenericReqAndResp<VegaGhanaCocoaHistoryTranxMtnr>>>()
    val historyTranxMtnr: LiveData<Resource<GenericReqAndResp<VegaGhanaCocoaHistoryTranxMtnr>>> get() = _historyTransMtnr

    private var uomDetailSource: LiveData<List<VegaUomDetails>> = MutableLiveData()
    private val _uomDetail = MediatorLiveData<List<VegaUomDetails>>()
    val uomDetail: LiveData<List<VegaUomDetails>> get() = _uomDetail

    fun getHistoryTranxMtnr(endDate: String, plant: String, startDate: String) = viewModelScope.launch(dispatchers.main) {
        _historyTransMtnr.removeSource(historyTranxMtnrSource)
        withContext(dispatchers.io){
            historyTranxMtnrSource = useCaseCocoa.getHistoryTranxMtnr(endDate, plant, startDate)
        }
        _historyTransMtnr.addSource(historyTranxMtnrSource){
            _historyTransMtnr.value = it
        }
    }

    fun getSuppliers() = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCaseCocoa.getSupplier()
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
        }
    }

    fun getHistoryTranxGrn(endDate: String, plant: String, startDate: String) = viewModelScope.launch(dispatchers.main) {
        _historyTranx.removeSource(historyTranxSource)
        withContext(dispatchers.io) {
            historyTranxSource = useCaseCocoa.getHistoryTranxGrn(endDate, plant, startDate)
        }
        _historyTranx.addSource(historyTranxSource){
            _historyTranx.value = it
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
}
