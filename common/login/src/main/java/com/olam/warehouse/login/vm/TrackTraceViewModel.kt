package com.olam.warehouse.login.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.TrackTraceSourceLotDetails
import com.olam.warehouse.login.data.domain.usecase.TrackTraceUseCase
import com.olam.warehouse.master.vega.entity.TrackTraceTransactionIdDetails
import com.olam.warehouse.master.vega.entity.VegaTrackTraceFarmerData
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.TrackTraceModelTransactionIdDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrackTraceViewModel(
    private val useCase: TrackTraceUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var sourceLotIdDetailsSource: LiveData<Resource<GenericReqAndResp<List<TrackTraceSourceLotDetails>>>> =
        MutableLiveData()
    private val _sourceLotIdDetails = MediatorLiveData<Resource<GenericReqAndResp<List<TrackTraceSourceLotDetails>>>>()
    val sourceLotIdDetails: LiveData<Resource<GenericReqAndResp<List<TrackTraceSourceLotDetails>>>> get() = _sourceLotIdDetails

    private var allSourceLotIdSourceList: LiveData<List<TrackTraceSourceLotDetails>> =
        MutableLiveData()
    private val _allSourceLotIdList = MediatorLiveData<List<TrackTraceSourceLotDetails>>()
    val allSourceLotIdList: LiveData<List<TrackTraceSourceLotDetails>> get() = _allSourceLotIdList

    private var allTransactionIdSourceList: LiveData<List<TrackTraceTransactionIdDetails>> =
        MutableLiveData()
    private val _allTransactionIdList = MediatorLiveData<List<TrackTraceTransactionIdDetails>>()
    val allTransactionIdList: LiveData<List<TrackTraceTransactionIdDetails>> get() = _allTransactionIdList


    private var offlineSourceLotIdDetailsSource: LiveData<TrackTraceSourceLotDetails> =
        MutableLiveData()
    private val _offlineSourceLotIdDetails = MediatorLiveData<TrackTraceSourceLotDetails>()
    val offlineSourceLotIdDetails: LiveData<TrackTraceSourceLotDetails> get() = _offlineSourceLotIdDetails

    private var onlineTransactionIdIdDetailsSource: LiveData<Resource<GenericReqAndResp<List<TrackTraceModelTransactionIdDetails>>>> =
        MutableLiveData()
    private val _onlineTransactionIdDetails = MediatorLiveData<Resource<GenericReqAndResp<List<TrackTraceModelTransactionIdDetails>>>>()
    val onlineTransactionIdDetails: LiveData<Resource<GenericReqAndResp<List<TrackTraceModelTransactionIdDetails>>>> get() = _onlineTransactionIdDetails

    private var offlineTransactionIdIdDetailsSource: LiveData<TrackTraceTransactionIdDetails> =
        MutableLiveData()
    private val _offlineTransactionIdDetails = MediatorLiveData<TrackTraceTransactionIdDetails>()
    val offlineTransactionIdDetails: LiveData<TrackTraceTransactionIdDetails> get() = _offlineTransactionIdDetails


    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val supplier: LiveData<List<VegaVendor>> get() = _supplier

    private var farmerListSource: LiveData<List<VegaTrackTraceFarmerData>> = MutableLiveData()
    private val _farmerList = MediatorLiveData<List<VegaTrackTraceFarmerData>>()
    val farmerList: LiveData<List<VegaTrackTraceFarmerData>> get() = _farmerList

    private var farmerDataEudrDetailSource: LiveData<VegaTrackTraceFarmerData> = MutableLiveData()
    private val _farmerDataEudrDetail = MediatorLiveData<VegaTrackTraceFarmerData>()
    val farmerDataEudrDetail: LiveData<VegaTrackTraceFarmerData> get() = _farmerDataEudrDetail

    fun getSourceLotIdDetails(key: String, sourceLotId: String) {
        viewModelScope.launch(dispatchers.main) {
            _sourceLotIdDetails.removeSource(sourceLotIdDetailsSource)
            withContext(dispatchers.io) {
                sourceLotIdDetailsSource = useCase.getSourceLotIdDetails(key, sourceLotId)
            }
            _sourceLotIdDetails.addSource(sourceLotIdDetailsSource) {
                _sourceLotIdDetails.value = it
            }
        }
    }

    fun getAllSourceLotIdList() {
        viewModelScope.launch(dispatchers.main) {
            _allSourceLotIdList.removeSource(allSourceLotIdSourceList)
            withContext(dispatchers.io) {
                allSourceLotIdSourceList = useCase.getAllSourceLotIdList()
            }
            _allSourceLotIdList.addSource(allSourceLotIdSourceList) {
                _allSourceLotIdList.value = it
            }
        }
    }

    fun getAllTransactionIdList() {
        viewModelScope.launch(dispatchers.main) {
            _allTransactionIdList.removeSource(allTransactionIdSourceList)
            withContext(dispatchers.io) {
                allTransactionIdSourceList = useCase.getAllTransactionIdList()
            }
            _allTransactionIdList.addSource(allTransactionIdSourceList) {
                _allTransactionIdList.value = it
            }
        }
    }



    fun getOfflineSourceLotDetails(sourceLotId: String){
        viewModelScope.launch(dispatchers.main) {
            _offlineSourceLotIdDetails.removeSource(offlineSourceLotIdDetailsSource)
            withContext(dispatchers.io) {
                offlineSourceLotIdDetailsSource = useCase.getOfflineSourceLotDetails(sourceLotId)
            }
            _offlineSourceLotIdDetails.addSource(offlineSourceLotIdDetailsSource) {
                _offlineSourceLotIdDetails.value = it
            }
        }
    }

    fun getSuppliers(purChaseType: String?) {
        viewModelScope.launch(dispatchers.main) {
            _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                supplierSource = useCase.getSupplier(purChaseType)
            }
            _supplier.addSource(supplierSource) {
                _supplier.value = it
            }
        }
    }

    fun getFarmerList(){
        viewModelScope.launch(dispatchers.main) {
            _farmerList.removeSource(farmerListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                farmerListSource = useCase.getFarmerList()
            }
            _farmerList.addSource(farmerListSource) {
                _farmerList.value = it
            }
        }
    }

    fun getFarmerEudrDetails(farmerData: String) {
        viewModelScope.launch(dispatchers.main) {
            _farmerDataEudrDetail.removeSource(farmerDataEudrDetailSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                farmerDataEudrDetailSource = useCase.getFarmerEudrDetails(farmerData)
            }
            _farmerDataEudrDetail.addSource(farmerDataEudrDetailSource) {
                _farmerDataEudrDetail.value = it
            }
        }
    }

    fun getOnlineTransactionIdDetails(transactionId: String) {
        viewModelScope.launch(dispatchers.main) {
            _onlineTransactionIdDetails.removeSource(onlineTransactionIdIdDetailsSource)
            withContext(dispatchers.io) {
                onlineTransactionIdIdDetailsSource = useCase.getOnlineTransactionIdDetails(transactionId)
            }
            _onlineTransactionIdDetails.addSource(onlineTransactionIdIdDetailsSource) {
                _onlineTransactionIdDetails.value = it
            }
        }
    }

    fun getOfflineTransactionIdDetails(transactionId: String) {
        viewModelScope.launch(dispatchers.main) {
            _offlineTransactionIdDetails.removeSource(offlineTransactionIdIdDetailsSource)
            withContext(dispatchers.io) {
                offlineTransactionIdIdDetailsSource = useCase.getOfflineTransactionIdDetails(transactionId)
            }
            _offlineTransactionIdDetails.addSource(offlineTransactionIdIdDetailsSource) {
                _offlineTransactionIdDetails.value = it
            }
        }
    }
}
