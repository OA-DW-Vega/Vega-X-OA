package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.ginninginprogress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.GinningInprogress
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain.VegaCottonGinningInprogressUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by SangiliPandian C on 16-03-2020.
 */
class GinningInprogressViewModel(
    private val useCase: VegaCottonGinningInprogressUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var fetchLotDetailsSource:  LiveData<Resource<GenericReqAndResp<GinningInprogress>>> =
        MutableLiveData()
    private val _fetchLotDetails = MediatorLiveData<Resource<GenericReqAndResp<GinningInprogress>>>()
    val fetchLotDetails:LiveData<Resource<GenericReqAndResp<GinningInprogress>>>  get() = _fetchLotDetails

    private var validateBaleSource: LiveData<Resource<GenericReqAndResp<GenericMessage>>> =
        MutableLiveData()
    private val _validateBale = MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val validateBale: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _validateBale

    private var saveGinningSource: LiveData<Resource<GenericReqAndResp<GenericMessage>>> =
        MutableLiveData()
    private val _saveGinning = MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val saveGinning: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _saveGinning


    private var getBalesByLotNumberSource: LiveData<List<Bale>> =
        MutableLiveData()
    private val _getBalesByLotNumber = MediatorLiveData<List<Bale>>()
    val getBalesByLotNumber:LiveData<List<Bale>> get() = _getBalesByLotNumber



    suspend fun insertGInningBales(bale: Bale) =viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
           useCase.insertGInningBales(bale)
        }

    }
    suspend fun deleteBaleFromDb(bale: Bale) =viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteGinningBale(bale)
        }

    }

    suspend fun deleteBalesByLotId(lotId: String) =viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteBalesByLotId(lotId)
        }

    }


    fun fetchLotDetails() = viewModelScope.launch(dispatchers.main) {
        _fetchLotDetails.removeSource(fetchLotDetailsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            fetchLotDetailsSource = useCase.fetchLotDetails()
        }
        _fetchLotDetails.addSource(fetchLotDetailsSource) {
            _fetchLotDetails.value = it
        }
    }

    fun validateBale(baleId: String) = viewModelScope.launch(dispatchers.main) {
        _validateBale.removeSource(validateBaleSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            validateBaleSource = useCase.validateBale(baleId)
        }
        _validateBale.addSource(validateBaleSource) {
            _validateBale.value = it
        }
    }
    fun saveGinning(inprogress: GinningInprogress)= viewModelScope.launch(dispatchers.main) {
        _saveGinning.removeSource(saveGinningSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            saveGinningSource = useCase.saveGinning(inprogress)
        }
        _saveGinning.addSource(saveGinningSource) {
            _saveGinning.value = it
        }
    }

    fun getBalesByLotNumber(lotId: String)= viewModelScope.launch(dispatchers.main) {
        _getBalesByLotNumber.removeSource(getBalesByLotNumberSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getBalesByLotNumberSource = useCase.getBalesByLotNumber(lotId)
        }
        _getBalesByLotNumber.addSource(getBalesByLotNumberSource) {
            _getBalesByLotNumber.value = it
        }
    }

}
