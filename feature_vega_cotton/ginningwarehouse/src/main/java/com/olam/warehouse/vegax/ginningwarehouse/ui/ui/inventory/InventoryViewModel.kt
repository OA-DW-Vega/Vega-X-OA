package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.inventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain.VegaCottonGinningInventoryUseCase
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.model.BaleGrade
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InventoryViewModel (
private val useCase: VegaCottonGinningInventoryUseCase,
private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var getInventoryGradesSource: LiveData<Resource<GenericReqAndResp<List<BaleGrade>>>> =
        MutableLiveData()
    private val _getInventoryGrades = MediatorLiveData<Resource<GenericReqAndResp<List<BaleGrade>>>>()
    val getInventoryGrades: LiveData<Resource<GenericReqAndResp<List<BaleGrade>>>> get() = _getInventoryGrades

    private var fetchInventoryBaleListSource: LiveData<Resource<GenericReqAndResp<List<Bale>>>> =
        MutableLiveData()
    private val _fetchInventoryBaleList = MediatorLiveData<Resource<GenericReqAndResp<List<Bale>>>>()
    val fetchInventoryBaleList: LiveData<Resource<GenericReqAndResp<List<Bale>>>> get() = _fetchInventoryBaleList

    private var syncBaleByGradeSource: LiveData<Resource<GenericReqAndResp<GenericMessage>>> =
        MutableLiveData()
    private val _syncBaleByGrade= MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val syncBaleByGrade: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _syncBaleByGrade

    var mInventoryBaleList: List<Bale>? = emptyList<Bale>()

    fun setInventoryList(it: List<Bale>?) {
        it?.let {
            mInventoryBaleList = it
        }
    }

    /*fun chageBaleStatus(changeBaleStatus: ChangeBaleStatus) = repo.chageBaleStatus(changeBaleStatus)

    private val baleGradeSync: LiveData<Resource<GenericResponse<GenericMessage>>>
    private val baleGrade: MutableLiveData<String> = MutableLiveData()

    init {
        baleGradeSync = Transformations.switchMap(baleGrade) {
            baleGrade.value?.let { repo.syncBaleByGrade(it) }
                ?: AbsentLiveData.create()
        }
    }

    fun setGrade(grade: String) {
        baleGrade.postValue(grade)
    }*/

    fun getInventoryGrades() = viewModelScope.launch(dispatchers.main) {
        _getInventoryGrades.removeSource(getInventoryGradesSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getInventoryGradesSource = useCase.getInventoryGrades()
        }
        _getInventoryGrades.addSource(getInventoryGradesSource) {
            _getInventoryGrades.value = it
        }
    }

    fun fetchInventoryBaleList() = viewModelScope.launch(dispatchers.main) {
        _fetchInventoryBaleList.removeSource(fetchInventoryBaleListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            fetchInventoryBaleListSource = useCase.fetchInventoryBaleList()
        }
        _fetchInventoryBaleList.addSource(fetchInventoryBaleListSource) {
            _fetchInventoryBaleList.value = it
        }
    }

    fun syncBaleByGrade(grade: String) = viewModelScope.launch(dispatchers.main) {
        _syncBaleByGrade.removeSource(syncBaleByGradeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            syncBaleByGradeSource = useCase.syncBaleByGrade(grade )
        }
        _syncBaleByGrade.addSource(syncBaleByGradeSource) {
            _syncBaleByGrade.value = it
        }
    }
}
