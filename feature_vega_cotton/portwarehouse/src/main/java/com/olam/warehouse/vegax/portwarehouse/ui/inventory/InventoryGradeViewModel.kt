package com.olam.warehouse.portwarehouse.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.BaleGrade
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.ChangeBaleStatus
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.PilesMaster
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.portwarehouse.data.domain.VegaCottonPortWareHouseInventoryUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InventoryGradeViewModel(
                              private val useCase: VegaCottonPortWareHouseInventoryUseCase,
                              private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var getPileListSource: LiveData<Resource<GenericReqAndResp<PilesMaster>>> =
        MutableLiveData()
    private val _getPileList = MediatorLiveData<Resource<GenericReqAndResp<PilesMaster>>>()
    val getPileList: LiveData<Resource<GenericReqAndResp<PilesMaster>>> get() = _getPileList

    private var getInventoryGradesSource: LiveData<Resource<GenericReqAndResp<List<BaleGrade>>>> =
        MutableLiveData()
    private val _getInventoryGrades = MediatorLiveData<Resource<GenericReqAndResp<List<BaleGrade>>>>()
    val getInventoryGrades: LiveData<Resource<GenericReqAndResp<List<BaleGrade>>>> get() = _getInventoryGrades

    private var syncBaleByGradeSource: LiveData<Resource<GenericReqAndResp<GenericMessage>>> =
        MutableLiveData()
    private val _syncBaleByGrade= MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val syncBaleByGrade: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _syncBaleByGrade

    private var fetchInventoryBaleListSource: LiveData<Resource<GenericReqAndResp<com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.InventoryBale>>> =
        MutableLiveData()
    private val _fetchInventoryBaleList = MediatorLiveData<Resource<GenericReqAndResp<com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.InventoryBale>>>()
    val fetchInventoryBaleList: LiveData<Resource<GenericReqAndResp<com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.InventoryBale>>> get() = _fetchInventoryBaleList


    private var changeBaleStatusSource: LiveData<Resource<GenericReqAndResp<GenericMessage>>> =
        MutableLiveData()
    private val _changeBaleStatus = MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val changeBaleStatus: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _changeBaleStatus


    fun syncBaleByGrade(grade: String) = viewModelScope.launch(dispatchers.main) {
        _syncBaleByGrade.removeSource(syncBaleByGradeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {

            syncBaleByGradeSource = useCase.syncBaleByGrade(grade )

        }
        _syncBaleByGrade.addSource(syncBaleByGradeSource) {
            _syncBaleByGrade.value = it
        }
    }

    fun getInventoryGrades(selectedPileList: List<String>)= viewModelScope.launch(dispatchers.main) {
        _getInventoryGrades.removeSource(getInventoryGradesSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getInventoryGradesSource = useCase.getInventoryGrades(selectedPileList)
        }
        _getInventoryGrades.addSource(getInventoryGradesSource) {
            _getInventoryGrades.value = it
        }
    }

    fun getPileList() = viewModelScope.launch(dispatchers.main) {
        _getPileList.removeSource(getPileListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getPileListSource = useCase.getPileList()
        }
        _getPileList.addSource(getPileListSource) {
            _getPileList.value = it
        }
    }

    var mInventoryBaleList: List<PortBale>? = emptyList<PortBale>()

    fun setInventoryList(it: List<PortBale>?) {
        mInventoryBaleList = emptyList<PortBale>()
        mInventoryBaleList = it
    }

    fun changeBaleStatus(changeBaleStatus: ChangeBaleStatus) = viewModelScope.launch(dispatchers.main) {
        _changeBaleStatus.removeSource(changeBaleStatusSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            changeBaleStatusSource = useCase.changeBaleStatus(changeBaleStatus)
        }
        _changeBaleStatus.addSource(changeBaleStatusSource) {
            _changeBaleStatus.value = it
        }
    }

    fun fetchInventoryBaleList(
        baleNo: String,
        baleTypeList: ArrayList<String>,
        gradeList: ArrayList<String>,
        pageNo: Int,
        pageSize: Int,
        marks: ArrayList<String>,
        years: ArrayList<String>
    ) = viewModelScope.launch(dispatchers.main) {
        _fetchInventoryBaleList.removeSource(fetchInventoryBaleListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            fetchInventoryBaleListSource = useCase.fetchInventoryBaleList(baleNo, baleTypeList, gradeList, pageNo, pageSize, marks, years)
        }
        _fetchInventoryBaleList.addSource(fetchInventoryBaleListSource) {
            _fetchInventoryBaleList.value = it
        }
    }

}
