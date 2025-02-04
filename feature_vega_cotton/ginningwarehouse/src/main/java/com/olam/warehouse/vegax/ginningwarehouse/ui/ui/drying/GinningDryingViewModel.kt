package com.olam.warehouse.ginning.ui.drying

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.IncomingLot
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.DeliveryWithBales
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.ginningwarehouse.data.domain.VegaCottonDispatchUseCase
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain.VegaCottonGinningDryingUsecase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by SangiliPandian C on 13-03-2020.
 */
class GinningDryingViewModel(
    private val useCase: VegaCottonGinningDryingUsecase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var fetchingDryingLotsSource: LiveData<Resource<GenericReqAndResp<List<IncomingLot>>>> =
        MutableLiveData()
    private val _fetchingDryingLots = MediatorLiveData<Resource<GenericReqAndResp<List<IncomingLot>>>>()
    val fetchingDryingLots: LiveData<Resource<GenericReqAndResp<List<IncomingLot>>>> get() = _fetchingDryingLots


    private var postLotForGinningSource: LiveData<Resource<GenericReqAndResp<GenericMessage>>> =
        MutableLiveData()
    private val _postLotForGinning= MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val postLotForGinning: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _postLotForGinning



    fun fetchDryingLots() = viewModelScope.launch(dispatchers.main) {
        _fetchingDryingLots.removeSource(fetchingDryingLotsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            fetchingDryingLotsSource = useCase.fetchDryingLots()
        }
        _fetchingDryingLots.addSource(fetchingDryingLotsSource) {
            _fetchingDryingLots.value = it
        }
    }
    fun postLotForGinning(mIncomingLot: List<IncomingLot>)= viewModelScope.launch(dispatchers.main) {
        _postLotForGinning.removeSource(postLotForGinningSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            postLotForGinningSource = useCase.postLotForGinning(mIncomingLot)
        }
        _postLotForGinning.addSource(postLotForGinningSource) {
            _postLotForGinning.value = it
        }
    }
}
