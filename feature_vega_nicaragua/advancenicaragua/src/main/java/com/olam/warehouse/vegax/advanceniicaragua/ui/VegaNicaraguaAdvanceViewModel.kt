package com.olam.warehouse.vegax.advanceniicaragua.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.advanceniicaragua.data.domain.model.VegaNicaraguaAdvancePostRequest
import com.olam.warehouse.vegax.advanceniicaragua.data.domain.model.VegaNicaraguaAdvancePostResponse
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaGetAdvanceDetails
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.advanceniicaragua.data.domain.usecase.VegaNicaraguaAdvanceUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class VegaNicaraguaAdvanceViewModel(
    private val useCase: VegaNicaraguaAdvanceUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {
    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val supplier: LiveData<List<VegaVendor>> get() = _supplier

    private var exchangeRateSource: LiveData<Resource<GenericReqAndResp<ExchangeRate>>> =
        MutableLiveData()
    private val _exchangeRate = MediatorLiveData<Resource<GenericReqAndResp<ExchangeRate>>>()
    val exchangeRate: LiveData<Resource<GenericReqAndResp<ExchangeRate>>> get() = _exchangeRate

    private var exchangeRateSourceOffline: LiveData<VegaNicaraguaExchangeRate> =
        MutableLiveData()
    private val _exchangeRateOffline = MediatorLiveData<VegaNicaraguaExchangeRate>()
    val exchangeRateOffline: LiveData<VegaNicaraguaExchangeRate> get() = _exchangeRateOffline

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var getAdvanceDetailsSource: LiveData<Resource<GenericReqAndResp<List<VegaNicaraguaGetAdvanceDetails>>>> =
        MutableLiveData()
    private val _getAdvanceDetails =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaNicaraguaGetAdvanceDetails>>>>()
    val getAdvanceDetails: LiveData<Resource<GenericReqAndResp<List<VegaNicaraguaGetAdvanceDetails>>>> get() = _getAdvanceDetails

    private var getAdvanceDetailsOfflineSource: LiveData<VegaNicaraguaAdvanceDetails> =
        MutableLiveData()
    private val _getAdvanceDetailsOffline = MediatorLiveData<VegaNicaraguaAdvanceDetails>()
    val getAdvanceDetailsOffline: LiveData<VegaNicaraguaAdvanceDetails> get() = _getAdvanceDetailsOffline

    private var createAdvanceSource: LiveData<Resource<GenericReqAndResp<VegaNicaraguaAdvancePostResponse>>> =
        MutableLiveData()
    private val _createAdvance = MediatorLiveData<Resource<GenericReqAndResp<VegaNicaraguaAdvancePostResponse>>>()
    val createAdvance: LiveData<Resource<GenericReqAndResp<VegaNicaraguaAdvancePostResponse>>> get() = _createAdvance


    private var advanceTransListSource: LiveData<List<VegaNicaraguaAdvanceTransactionDetails>> = MutableLiveData()
    private val _advanceTransList = MediatorLiveData<List<VegaNicaraguaAdvanceTransactionDetails>>()
    val  advanceTransList: LiveData<List<VegaNicaraguaAdvanceTransactionDetails>> get() = _advanceTransList



    fun getSuppliers(purchaseOrgType: String) = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCase.getSuppliers(purchaseOrgType)
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
        }
    }

    fun getExchangeRate() = viewModelScope.launch(dispatchers.main) {
        _exchangeRate.removeSource(exchangeRateSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            exchangeRateSource = useCase.getExchangeRate()
        }
        _exchangeRate.addSource(exchangeRateSource) {
            _exchangeRate.value = it
        }
    }

    fun getExchangeRateOffline() = viewModelScope.launch(dispatchers.main) {
        _exchangeRateOffline.removeSource(exchangeRateSourceOffline) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            exchangeRateSourceOffline = useCase.getExchangeRateOffline()
        }
        _exchangeRate.addSource(exchangeRateSourceOffline) {
            _exchangeRateOffline.value = it
        }
    }

    fun getAdvanceDetailsOffline(vendorCode: String) = viewModelScope.launch(dispatchers.main) {
        _getAdvanceDetailsOffline.removeSource(getAdvanceDetailsOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getAdvanceDetailsOfflineSource = useCase.getAdvanceDetailsByVendorOffline(vendorCode)
        }
        _getAdvanceDetailsOffline.addSource(getAdvanceDetailsOfflineSource) {
            _getAdvanceDetailsOffline.value = it
        }
    }

    fun getAdvanceDetails(vendorCode: String) = viewModelScope.launch(dispatchers.main)
    {
        _getAdvanceDetails.removeSource(getAdvanceDetailsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getAdvanceDetailsSource = useCase.getAdvanceDetailsByVendor(vendorCode)
        }
        _getAdvanceDetails.addSource(getAdvanceDetailsSource) {
            _getAdvanceDetails.value = it
        }
    }

    fun postCreateAdvance(postingData: VegaNicaraguaAdvancePostRequest) = viewModelScope.launch(dispatchers.main)
    {
        _createAdvance.removeSource(createAdvanceSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            createAdvanceSource = useCase.postAdvanceDetails(postingData)
        }
        _createAdvance.addSource(createAdvanceSource) {
            _createAdvance.value = it
        }
    }

    fun saveTransactionAdvanceData(postingData: VegaNicaraguaAdvanceTransactionDetails) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveAdvanceTransactionDetails(postingData)
        }
    }

    fun deleteItem(tmpWbId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteItem(tmpWbId)
        }
    }


    fun getAdvanceDetails() = viewModelScope.launch(dispatchers.main) {
        _advanceTransList.removeSource(advanceTransListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            advanceTransListSource = useCase.getAdvanceDetails()
        }
        _advanceTransList.addSource(advanceTransListSource) {
            _advanceTransList.value = it
        }
    }
    fun getConfigItems(role: String) = viewModelScope.launch(dispatchers.main) {
        _configItems.removeSource(configSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            configSource = useCase.getConfigItems(role)
        }
        _configItems.addSource(configSource) {
            _configItems.value = it
        }
    }

}
