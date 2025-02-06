package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.seedcotton

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.IncomingLot
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain.VegaCottonIncomingLotsSeedCottonUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by SangiliPandian C on 05-03-2020.
 */

class GinningIncomingLotViewModel(private val useCase: VegaCottonIncomingLotsSeedCottonUseCase,
                                  private val dispatchers: AppDispatchers
) :
    BaseViewModel() {

    private var fetchingIncomingLotSource:  LiveData<Resource<GenericReqAndResp<List<IncomingLot>>>> =
        MutableLiveData()
    private val _fetchingIncomingLots = MediatorLiveData<Resource<GenericReqAndResp<List<IncomingLot>>>>()
    val fetchingIncomingLots: LiveData<Resource<GenericReqAndResp<List<IncomingLot>>>> get() = _fetchingIncomingLots


    private var  postLotForGinningSource:  LiveData<Resource<GenericReqAndResp<GenericMessage>>> =
        MutableLiveData()
    private val _postLotForGinning = MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val  postLotForGinning: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _postLotForGinning

    fun fetchingIncomingLots() = viewModelScope.launch(dispatchers.main) {
        _fetchingIncomingLots.removeSource(fetchingIncomingLotSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            fetchingIncomingLotSource = useCase.fetchingIncomingLots()
        }
        _fetchingIncomingLots.addSource(fetchingIncomingLotSource) {
            _fetchingIncomingLots.value = it
        }
    }
    fun postLotForGinning(incomingLot: IncomingLot) = viewModelScope.launch(dispatchers.main) {
        _postLotForGinning.removeSource(postLotForGinningSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            postLotForGinningSource = useCase.postLotForGinning(incomingLot)
        }
        _postLotForGinning.addSource(postLotForGinningSource) {
            _postLotForGinning.value = it
        }
    }
}
