package com.olam.warehouse.vegax.reports.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.presentation.data.domain.model.Auth
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.reports.data.domain.VegaCommonReportDataSetModel
import com.olam.warehouse.vegax.reports.data.domain.VegaCommonReportDataSetTokenModel
import com.olam.warehouse.vegax.reports.data.domain.VegaCommonReportDataSetTokenRequest
import com.olam.warehouse.vegax.reports.data.repo.VegaCommonReportRepo
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaCommonReportViewModel(
    private val repo: VegaCommonReportRepo, private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var authSource: LiveData<Resource<Auth>> = MutableLiveData()
    private val _auth = MediatorLiveData<Resource<Auth>>()
    val auth: LiveData<Resource<Auth>> get() = _auth


    private var dataSetSource: LiveData<Resource<VegaCommonReportDataSetModel>> = MutableLiveData()
    private val _dataSet = MediatorLiveData<Resource<VegaCommonReportDataSetModel>>()
    val dataSet: LiveData<Resource<VegaCommonReportDataSetModel>> get() = _dataSet

    private var dataSetTokenSource: LiveData<Resource<VegaCommonReportDataSetTokenModel>> = MutableLiveData()
    private val _dataSetToken = MediatorLiveData<Resource<VegaCommonReportDataSetTokenModel>>()
    val dataSetToken: LiveData<Resource<VegaCommonReportDataSetTokenModel>> get() = _dataSetToken

    fun getDataSetToken(model: VegaCommonReportDataSetTokenRequest) = viewModelScope.launch(dispatchers.main) {
        _dataSetToken.removeSource(dataSetTokenSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dataSetTokenSource = repo.getDataSetToken(model)
        }
        _dataSetToken.addSource(dataSetTokenSource) {
            _dataSetToken.value = it
        }
    }
    fun getDataSet(accessToken: String) = viewModelScope.launch(dispatchers.main) {
        _dataSet.removeSource(dataSetSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dataSetSource = repo.getDataSet(accessToken)
        }
        _dataSet.addSource(dataSetSource) {
            _dataSet.value = it
        }
    }

    fun getADToken() = viewModelScope.launch(dispatchers.main) {
        _auth.removeSource(authSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            authSource = repo.getADToken()
        }
        _auth.addSource(authSource) {
            _auth.value = it
        }
    }
}
