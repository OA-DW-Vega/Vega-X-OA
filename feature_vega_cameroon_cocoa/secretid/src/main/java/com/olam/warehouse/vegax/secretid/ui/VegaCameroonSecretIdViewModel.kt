package com.olam.warehouse.vegax.secretid.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.secretid.data.domain.model.VegaCameroonSecretId
import com.olam.warehouse.vegax.secretid.data.domain.usecase.VegaCameroonSecretIdUsecase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
class VegaCameroonSecretIdViewModel(
    private val useCase: VegaCameroonSecretIdUsecase, private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var secretIdSource: LiveData<Resource<GenericReqAndResp<List<VegaCameroonSecretId>>>> =
        MutableLiveData()
    private var _secretId = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCameroonSecretId>>>>()
    val secretId: LiveData<Resource<GenericReqAndResp<List<VegaCameroonSecretId>>>> get() = _secretId

    private var wbWeightSource: LiveData<Resource<GenericReqAndResp<VegaQualityWBDetails>>> =
        MutableLiveData()
    private val _wbWeight = MediatorLiveData<Resource<GenericReqAndResp<VegaQualityWBDetails>>>()
    val wbWeight: LiveData<Resource<GenericReqAndResp<VegaQualityWBDetails>>> get() = _wbWeight


    fun getSecretIdList(startDate: String, endDate: String, isMtnt: Boolean) = viewModelScope.launch(dispatchers.main) {
        _secretId.removeSource(secretIdSource)
        withContext(dispatchers.io) {
            secretIdSource = useCase.getDashboardResult(startDate, endDate, isMtnt)
        }
        _secretId.addSource(secretIdSource) {
            _secretId.value = it
        }
    }

    fun getWbWeightDetails(wbid: String) = viewModelScope.launch(dispatchers.main) {
        _wbWeight.removeSource(wbWeightSource)
        withContext(dispatchers.io) {
            wbWeightSource = useCase.getWbWeightDetails(wbid)
        }
        _wbWeight.addSource(wbWeightSource) {
            _wbWeight.value = it
        }
    }

}
