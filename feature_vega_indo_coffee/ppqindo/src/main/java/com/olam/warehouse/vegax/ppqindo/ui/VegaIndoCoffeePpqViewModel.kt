package com.olam.warehouse.vegax.ppqindo.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.ppqindo.data.domain.model.VegaIndoCoffeePpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqindo.data.domain.model.VegaIndoCoffeePpqInspectionLots
import com.olam.warehouse.vegax.ppqindo.data.domain.model.VegaIndoCoffeePpqPostResponse
import com.olam.warehouse.vegax.ppqindo.data.domain.usecase.VegaIndoCoffeePpqUsecase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
class VegaIndoCoffeePpqViewModel(
    private val useCase: VegaIndoCoffeePpqUsecase, private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var inspectionLotsSource: LiveData<Resource<GenericReqAndResp<List<VegaIndoCoffeePpqInspectionLots>>>> =
        MutableLiveData()
    private var _inspectionLots = MediatorLiveData<Resource<GenericReqAndResp<List<VegaIndoCoffeePpqInspectionLots>>>>()
    val inspectionLots: LiveData<Resource<GenericReqAndResp<List<VegaIndoCoffeePpqInspectionLots>>>> get() = _inspectionLots

    private var lotDetailsSource: LiveData<Resource<GenericReqAndResp<VegaIndoCoffeePpqInspectionLotDetails>>> =
        MutableLiveData()
    private var _lotDetails = MediatorLiveData<Resource<GenericReqAndResp<VegaIndoCoffeePpqInspectionLotDetails>>>()
    val lotDetails: LiveData<Resource<GenericReqAndResp<VegaIndoCoffeePpqInspectionLotDetails>>> get() = _lotDetails

    private var postLotDetailsSource: LiveData<Resource<GenericReqAndResp<VegaIndoCoffeePpqPostResponse>>> =
        MutableLiveData()
    private var _postLotDetails = MediatorLiveData<Resource<GenericReqAndResp<VegaIndoCoffeePpqPostResponse>>>()
    val postLotDetails: LiveData<Resource<GenericReqAndResp<VegaIndoCoffeePpqPostResponse>>> get() = _postLotDetails

    fun getInspectionLots() = viewModelScope.launch(dispatchers.main) {
        _inspectionLots.removeSource(inspectionLotsSource)
        withContext(dispatchers.io) {
            inspectionLotsSource = useCase.getInspectionLots()
        }
        _inspectionLots.addSource(inspectionLotsSource) {
            _inspectionLots.value = it
        }
    }

    fun getInspectionLotDetails(lotId: String) = viewModelScope.launch(dispatchers.main) {
        _lotDetails.removeSource(lotDetailsSource)
        withContext(dispatchers.io) {
            lotDetailsSource = useCase.getInspectionLotDetails(lotId)
        }
        _lotDetails.addSource(lotDetailsSource) {
            _lotDetails.value = it
        }
    }

    fun saveInspectionLotDetails(lotDetail: VegaIndoCoffeePpqInspectionLotDetails) =
        viewModelScope.launch(dispatchers.main) {
            _postLotDetails.removeSource(postLotDetailsSource)
            withContext(dispatchers.io) {
                postLotDetailsSource = useCase.saveInspectionLotDetails(lotDetail)
            }
            _postLotDetails.addSource(postLotDetailsSource) {
                _postLotDetails.value = it
            }
        }

}

