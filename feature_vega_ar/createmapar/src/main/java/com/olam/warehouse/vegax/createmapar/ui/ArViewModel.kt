package com.olam.warehouse.vegax.createmapar.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.createmapar.data.domain.model.ArLotDetails
import com.olam.warehouse.vegax.createmapar.data.domain.usecase.ArUsecase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 6/15/2021.
 */
class ArViewModel(
    private val useCase: ArUsecase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var lotDeatilsSource: LiveData<Resource<ArLotDetails>> = MutableLiveData()
    private val _lotDeatils = MediatorLiveData<Resource<ArLotDetails>>()
    val lotDeatils: LiveData<Resource<ArLotDetails>> get() = _lotDeatils

    private var getLotDeatilsSource: LiveData<Resource<List<ArLotDetails>>> = MutableLiveData()
    private val _getLotDeatils = MediatorLiveData<Resource<List<ArLotDetails>>>()
    val getLotDeatils: LiveData<Resource<List<ArLotDetails>>> get() = _getLotDeatils

    fun saveLotDetails(lotDetails: ArLotDetails) = viewModelScope.launch(dispatchers.main) {
        _lotDeatils.removeSource(lotDeatilsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            lotDeatilsSource = useCase.saveLotDetails(lotDetails)
        }
        _lotDeatils.addSource(lotDeatilsSource) {
            _lotDeatils.value = it
        }
    }

    fun getLotDetails() = viewModelScope.launch(dispatchers.main) {
        _getLotDeatils.removeSource(getLotDeatilsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getLotDeatilsSource = useCase.getLotDetails()
        }
        _getLotDeatils.addSource(getLotDeatilsSource) {
            _getLotDeatils.value = it
        }
    }
}