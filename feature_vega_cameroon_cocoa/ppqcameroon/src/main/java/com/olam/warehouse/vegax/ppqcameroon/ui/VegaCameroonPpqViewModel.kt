package com.olam.warehouse.vegax.ppqcameroon.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqInspectionLots
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqPostResponse
import com.olam.warehouse.vegax.ppqcameroon.data.domain.usecase.VegaCameroonPpqUsecase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
class VegaCameroonPpqViewModel(
    private val useCase: VegaCameroonPpqUsecase, private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var inspectionLotsSource: LiveData<Resource<GenericReqAndResp<List<VegaCameroonPpqInspectionLots>>>> =
        MutableLiveData()
    private var _inspectionLots = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCameroonPpqInspectionLots>>>>()
    val inspectionLots: LiveData<Resource<GenericReqAndResp<List<VegaCameroonPpqInspectionLots>>>> get() = _inspectionLots

    private var lotDetailsSource: LiveData<Resource<GenericReqAndResp<VegaCameroonPpqInspectionLotDetails>>> =
        MutableLiveData()
    private var _lotDetails = MediatorLiveData<Resource<GenericReqAndResp<VegaCameroonPpqInspectionLotDetails>>>()
    val lotDetails: LiveData<Resource<GenericReqAndResp<VegaCameroonPpqInspectionLotDetails>>> get() = _lotDetails

    private var postLotDetailsSource: LiveData<Resource<GenericReqAndResp<VegaCameroonPpqPostResponse>>> =
        MutableLiveData()
    private var _postLotDetails = MediatorLiveData<Resource<GenericReqAndResp<VegaCameroonPpqPostResponse>>>()
    val postLotDetails: LiveData<Resource<GenericReqAndResp<VegaCameroonPpqPostResponse>>> get() = _postLotDetails

    private var qualityListSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist

    fun getQualityParams(materialId: String, isValueExist: Boolean?, wbId: String?) = viewModelScope.launch(dispatchers.main) {
        _qualitylist.removeSource(qualityListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityListSource = useCase.getQualityParams(materialId, isValueExist, wbId)
        }
        _qualitylist.addSource(qualityListSource) {
            _qualitylist.value = it
        }
    }
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

    fun saveInspectionLotDetails(lotDetail: VegaCameroonPpqInspectionLotDetails) =
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
