package com.olam.warehouse.vegax.ppqcoffee.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.ppqcoffee.data.domain.model.VegaCoffeePpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqcoffee.data.domain.model.VegaCoffeePpqInspectionLots
import com.olam.warehouse.vegax.ppqcoffee.data.domain.model.VegaCoffeePpqPostResponse
import com.olam.warehouse.vegax.ppqcoffee.data.domain.usecase.VegaCoffeePpqUsecase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
class VegaCoffeePpqViewModel(
    private val useCase: VegaCoffeePpqUsecase, private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var inspectionLotsSource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeePpqInspectionLots>>>> =
        MutableLiveData()
    private var _inspectionLots = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeePpqInspectionLots>>>>()
    val inspectionLots: LiveData<Resource<GenericReqAndResp<List<VegaCoffeePpqInspectionLots>>>> get() = _inspectionLots

    private var lotDetailsSource: LiveData<Resource<GenericReqAndResp<VegaCoffeePpqInspectionLotDetails>>> =
        MutableLiveData()
    private var _lotDetails = MediatorLiveData<Resource<GenericReqAndResp<VegaCoffeePpqInspectionLotDetails>>>()
    val lotDetails: LiveData<Resource<GenericReqAndResp<VegaCoffeePpqInspectionLotDetails>>> get() = _lotDetails

    private var postLotDetailsSource: LiveData<Resource<GenericReqAndResp<VegaCoffeePpqPostResponse>>> =
        MutableLiveData()
    private var _postLotDetails = MediatorLiveData<Resource<GenericReqAndResp<VegaCoffeePpqPostResponse>>>()
    val postLotDetails: LiveData<Resource<GenericReqAndResp<VegaCoffeePpqPostResponse>>> get() = _postLotDetails

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

    fun saveInspectionLotDetails(lotDetail: VegaCoffeePpqInspectionLotDetails) =
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
