package com.olam.wharhouse.vegax.transactionhistory.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.VegaFGRNHistoryTranxResponse
import com.olam.warehouse.master.vega.model.VegaGRNHistoryTranxResponse
import com.olam.warehouse.master.vega.model.VegaHistoryTranxMtnr
import com.olam.warehouse.master.vega.model.VegaMtntHistoryTranxResponse
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaRMINHistoryTranxResponse
import com.olam.warehouse.master.vegaghana.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 10/8/2022.
 */
class VegaTransHisViewModel(private val usecase: com.olam.wharhouse.vegax.transactionhistory.data.domain.usecase.VegaTransactionHistoryUsecase, private val dispatchers: AppDispatchers) : BaseViewModel()
{
    private var historyTranxMtntSource: LiveData<Resource<GenericReqAndResp<VegaMtntHistoryTranxResponse>>> = MutableLiveData()
    private val _historyTranxMtnt = MediatorLiveData<Resource<GenericReqAndResp<VegaMtntHistoryTranxResponse>>>()
    val historyTranxMtnt: LiveData<Resource<GenericReqAndResp<VegaMtntHistoryTranxResponse>>> get() = _historyTranxMtnt

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val supplier: LiveData<List<VegaVendor>> get() = _supplier

    private var historyTranxSource: LiveData<Resource<GenericReqAndResp<VegaGRNHistoryTranxResponse>>> = MutableLiveData()
    private val _historyTranx = MediatorLiveData<Resource<GenericReqAndResp<VegaGRNHistoryTranxResponse>>>()
    val historytranx: LiveData<Resource<GenericReqAndResp<VegaGRNHistoryTranxResponse>>> get() = _historyTranx

    private var historyTranxMtnrSource: LiveData<Resource<GenericReqAndResp<VegaHistoryTranxMtnr>>> = MutableLiveData()
    private val _historyTransMtnr = MediatorLiveData<Resource<GenericReqAndResp<VegaHistoryTranxMtnr>>>()
    val historyTranxMtnr: LiveData<Resource<GenericReqAndResp<VegaHistoryTranxMtnr>>> get() = _historyTransMtnr

    private var historyTranxFgrnSource: LiveData<Resource<GenericReqAndResp<VegaFGRNHistoryTranxResponse>>> = MutableLiveData()
    private val _historyFgrnTranx = MediatorLiveData<Resource<GenericReqAndResp<VegaFGRNHistoryTranxResponse>>>()
    val historyfgrntranx: LiveData<Resource<GenericReqAndResp<VegaFGRNHistoryTranxResponse>>> get() = _historyFgrnTranx


    private var historyTranxRminSource: LiveData<Resource<GenericReqAndResp<VegaRMINHistoryTranxResponse>>> = MutableLiveData()
    private val _historyRminTranx = MediatorLiveData<Resource<GenericReqAndResp<VegaRMINHistoryTranxResponse>>>()
    val historyrmintranx: LiveData<Resource<GenericReqAndResp<VegaRMINHistoryTranxResponse>>> get() = _historyRminTranx


    private var uomDetailSource: LiveData<List<VegaUomDetails>> = MutableLiveData()
    private val _uomDetail = MediatorLiveData<List<VegaUomDetails>>()
    val uomDetail: LiveData<List<VegaUomDetails>> get() = _uomDetail

    private var qualityPreSamplingSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> =
        MutableLiveData()
    private val _preSamplingQuality = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>()
    val preSamplingQuality: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> get() = _preSamplingQuality

    private var qualityListSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist

    fun getQualityParams(materialId: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualitylist.removeSource(qualityListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityListSource = usecase.getQualityParams(materialId)
            }
            _qualitylist.addSource(qualityListSource) {
                _qualitylist.value = it
            }
        }

    fun getHistoryTranxMtnt(endDate: String, plant: String, startDate: String) = viewModelScope.launch(dispatchers.main) {
        _historyTranxMtnt.removeSource(historyTranxMtnt)
        withContext(dispatchers.io) {
            historyTranxMtntSource = usecase.getHistoryTranxMtnt(endDate, plant, startDate)
        }
        _historyTranxMtnt.addSource(historyTranxMtntSource){
            _historyTranxMtnt.value = it
        }
    }

    fun getHistoryTranxMtnr(endDate: String, plant: String, startDate: String) = viewModelScope.launch(dispatchers.main) {
        _historyTransMtnr.removeSource(historyTranxMtnrSource)
        withContext(dispatchers.io){
            historyTranxMtnrSource = usecase.getHistoryTranxMtnr(endDate, plant, startDate)
        }
        _historyTransMtnr.addSource(historyTranxMtnrSource){
            _historyTransMtnr.value = it
        }
    }

    fun getSuppliers() = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = usecase.getSupplier()
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
        }
    }

    fun getHistoryTranxGrn(endDate: String, plant: String, startDate: String) = viewModelScope.launch(dispatchers.main) {
        _historyTranx.removeSource(historyTranxSource)
        withContext(dispatchers.io) {
            historyTranxSource = usecase.getHistoryTranxGrn(endDate, plant, startDate)
        }
        _historyTranx.addSource(historyTranxSource){
            _historyTranx.value = it
        }
    }


    fun getHistoryTranxFgrn(endDate: String, plant: String, startDate: String) = viewModelScope.launch(dispatchers.main) {
        _historyFgrnTranx.removeSource(historyTranxFgrnSource)
        withContext(dispatchers.io) {
            historyTranxFgrnSource = usecase.getHistoryTranxFGrn(endDate, plant, startDate)
        }
        _historyFgrnTranx.addSource(historyTranxFgrnSource){
            _historyFgrnTranx.value = it
        }
    }



    fun getHistoryTranxRmin(endDate: String, plant: String, startDate: String) = viewModelScope.launch(dispatchers.main) {
        _historyRminTranx.removeSource(historyTranxRminSource)
        withContext(dispatchers.io) {
            historyTranxRminSource = usecase.getHistoryTranxRmin(endDate, plant, startDate)
        }
        _historyRminTranx.addSource(historyTranxRminSource){
            _historyRminTranx.value = it
        }
    }


    fun getUomDetails() = viewModelScope.launch(dispatchers.main) {
        _uomDetail.removeSource(uomDetailSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            uomDetailSource = usecase.getuomDetail()
        }
        _uomDetail.addSource(uomDetailSource) {
            _uomDetail.value = it
        }
    }


    fun getPreSamplingQualitydata(batchNo: String, materialId: String,PlantId: String) = viewModelScope.launch(dispatchers.main) {
        _preSamplingQuality.removeSource(qualityPreSamplingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityPreSamplingSource = usecase.getPreSamplingQualityList(batchNo, materialId,PlantId)
        }
        _preSamplingQuality.addSource(qualityPreSamplingSource) {
            _preSamplingQuality.value = it
        }
    }
}
