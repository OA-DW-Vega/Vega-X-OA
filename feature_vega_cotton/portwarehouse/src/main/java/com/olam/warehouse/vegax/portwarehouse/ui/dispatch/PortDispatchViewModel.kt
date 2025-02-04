package com.olam.warehouse.vegax.portwarehouse.ui.dispatch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.*
import com.olam.warehouse.portwarehouse.utils.enums.ContainerStatus
import com.olam.warehouse.presentation.data.domain.model.AbsentLiveData
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.extensions.noOfLetters
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.portwarehouse.data.repository.DirectDispatchRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 4/1/2021.
 */

class PortDispatchViewModel constructor(val repo: DirectDispatchRepository, private val dispatchers: AppDispatchers) :
    BaseViewModel() {
    var lastSelectedPosition = 0
    var mOTs = emptyList<String>()
    var mContainer: Container? = null
    var mBale: PortBale? = null
    var mBaleList = emptyList<PortBale>()
    var mSelectedOT: DispatchOT? = null
    var mOTWithContainers: DispatchOTWithContainers? = null
    var isDirectDispatch = MutableLiveData(false)

    private val otId: MutableLiveData<String> = MutableLiveData()
    val conId: MutableLiveData<String> = MutableLiveData()
    var deletedContainer: Container = Container()

    fun setOtId(otNumber: String) = otId.postValue(otNumber)

    private var otDetailsSource: LiveData<Resource<GenericReqAndResp<DispatchOT>>> = MutableLiveData()
    private val _otDetails = MediatorLiveData<Resource<GenericReqAndResp<DispatchOT>>>()
    val otDetails: LiveData<Resource<GenericReqAndResp<DispatchOT>>> get() = _otDetails

    private var otDetailsComSource: LiveData<Resource<GenericReqAndResp<DispatchOT>>> = MutableLiveData()
    private val _otDetailsCom = MediatorLiveData<Resource<GenericReqAndResp<DispatchOT>>>()
    val otDetailsCom: LiveData<Resource<GenericReqAndResp<DispatchOT>>> get() = _otDetailsCom
    private var containerDetailsSource: LiveData<Resource<GenericReqAndResp<Container>>> = MutableLiveData()
    private val _containerDetails = MediatorLiveData<Resource<GenericReqAndResp<Container>>>()
    val containerDetails: LiveData<Resource<GenericReqAndResp<Container>>> get() = _containerDetails

    private var containerDetailsWithBalesSource: LiveData<Resource<GenericReqAndResp<Container>>> = MutableLiveData()
    private val _containerWithBalesDetails = MediatorLiveData<Resource<GenericReqAndResp<Container>>>()
    val containerWithBalesDetails: LiveData<Resource<GenericReqAndResp<Container>>> get() = _containerWithBalesDetails

    private var mtnsSource: LiveData<Resource<GenericReqAndResp<List<PortMtn>>>> =
        MutableLiveData()
    private val _mtns = MediatorLiveData<Resource<GenericReqAndResp<List<PortMtn>>>>()
    val mtns: LiveData<Resource<GenericReqAndResp<List<PortMtn>>>> get() = _mtns

    private var updateBalesSource: LiveData<Resource<GenericReqAndResp<GenericMessage>>> = MutableLiveData()
    private val _updateBales = MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val updateBales: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _updateBales

    private var otDetailListSource: LiveData<Resource<GenericReqAndResp<List<DispatchOT>>>> = MutableLiveData()
    private val _otDetailList = MediatorLiveData<Resource<GenericReqAndResp<List<DispatchOT>>>>()
    val otDetailList: LiveData<Resource<GenericReqAndResp<List<DispatchOT>>>> get() = _otDetailList

    private var deleteContainerSource: LiveData<Resource<GenericReqAndResp<GenericMessage>>> = MutableLiveData()
    private val _deleteContainer = MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val deleteContainer: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _deleteContainer

    private var holdStuffingSource: LiveData<Resource<GenericReqAndResp<GenericMessage>>> = MutableLiveData()
    private val _holdStuffing = MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val holdStuffing: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _holdStuffing

    private var updateDispatchModeSource: LiveData<Resource<GenericReqAndResp<GenericMessage>>> = MutableLiveData()
    private val _updateDispatchMode = MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val updateDispatchMode: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _updateDispatchMode

    private var baleDetailsSource: LiveData<Resource<GenericReqAndResp<PortBale>>> = MutableLiveData()
    private val _baleDetails = MediatorLiveData<Resource<GenericReqAndResp<PortBale>>>()
    val baleDetails: LiveData<Resource<GenericReqAndResp<PortBale>>> get() = _baleDetails

    private var deleteBaleSource: LiveData<Resource<GenericReqAndResp<GenericMessage>>> =
        MutableLiveData()
    private val _deleteBale = MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val deleteBale: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _deleteBale

    private var confirmDispatchSource: LiveData<Resource<GenericReqAndResp<GenericMessage>>> =
        MutableLiveData()
    private val _confirmDispatch = MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val confirmDispatch: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _confirmDispatch

    private var dbBaleDetailsSource: LiveData<PortBale> = MutableLiveData()
    private val _dbBaleDetails = MediatorLiveData<PortBale>()
    val dbBaleDetails: LiveData<PortBale> get() = _dbBaleDetails

    private var updateOTNumberSource: LiveData<Resource<GenericReqAndResp<SplitContainer>>> = MutableLiveData()
    private val _updateOTNumber = MediatorLiveData<Resource<GenericReqAndResp<SplitContainer>>>()
    val updateOTNumber: LiveData<Resource<GenericReqAndResp<SplitContainer>>> get() = _updateOTNumber

    fun confirmDispatch(otNumber: String) = viewModelScope.launch(dispatchers.main) {
        _confirmDispatch.removeSource(confirmDispatchSource)
        withContext(dispatchers.io) {
            confirmDispatchSource = repo.confirmDispatch(otNumber)
        }
        _confirmDispatch.addSource(confirmDispatchSource) {
            _confirmDispatch.value = it
        }
    }

    fun deleteBaleDetail(baleId: String, containerNumber: String) = viewModelScope.launch(dispatchers.main) {
        _deleteBale.removeSource(deleteBaleSource)
        withContext(dispatchers.io) {
            deleteBaleSource = repo.deleteBaleDetail(baleId, containerNumber)
        }
        _deleteBale.addSource(deleteBaleSource) {
            _deleteBale.value = it
        }
    }

    fun getBaleDetails(baleId: String, containerId: String, otNumber: String?, isDirect: Boolean) =
        viewModelScope.launch(dispatchers.main) {
            _baleDetails.removeSource(baleDetailsSource)
            withContext(dispatchers.io) {
                baleDetailsSource = repo.getBaleDetails(baleId, containerId, otNumber, isDirect)
            }
            _baleDetails.addSource(baleDetailsSource) {
                _baleDetails.value = it
            }
        }

    fun updateDispatchMode(otNumber: String, isDirect: Boolean) = viewModelScope.launch(dispatchers.main) {
        _updateDispatchMode.removeSource(updateDispatchModeSource)
        withContext(dispatchers.io) {
            updateDispatchModeSource = repo.updateDispatchMode(otNumber, isDirect)
        }
        _updateDispatchMode.addSource(updateDispatchModeSource) {
            _updateDispatchMode.value = it
        }
    }

    fun holdStuffing(containerNumber: String, otNumber: String, status: Int) = viewModelScope.launch(dispatchers.main) {
        _holdStuffing.removeSource(holdStuffingSource)
        withContext(dispatchers.io) {
            holdStuffingSource = repo.holdStuffing(containerNumber, otNumber, status)
        }
        _holdStuffing.addSource(holdStuffingSource) {
            _holdStuffing.value = it
        }
    }

    fun deleteContainerFromServer(containerNumber: String, otNumber: String) = viewModelScope.launch(dispatchers.main) {
        _deleteContainer.removeSource(deleteContainerSource)
        withContext(dispatchers.io) {
            deleteContainerSource = repo.deleteContainerFromServer(containerNumber, otNumber)
        }
        _deleteContainer.addSource(deleteContainerSource) {
            _deleteContainer.value = it
        }
    }

    fun getOtList() = viewModelScope.launch(dispatchers.main) {
        _otDetailList.removeSource(otDetailListSource)
        withContext(dispatchers.io) {
            otDetailListSource = repo.getOtList()
        }
        _otDetailList.addSource(otDetailListSource) {
            _otDetailList.value = it
        }
    }

    fun updateBalesToDirectDispatch(bales: List<PortMtnBales>) = viewModelScope.launch(dispatchers.main) {
        _updateBales.removeSource(updateBalesSource)
        withContext(dispatchers.io) {
            updateBalesSource = repo.updateBalesToDirectDispatch(bales)
        }
        _updateBales.addSource(updateBalesSource) {
            _updateBales.value = it
        }
    }

    fun getMtns() = viewModelScope.launch(dispatchers.main) {
        _mtns.removeSource(mtnsSource)
        withContext(dispatchers.io) {
            mtnsSource = repo.getMtns()
        }
        _mtns.addSource(mtnsSource) {
            _mtns.value = it
        }
    }

    fun getOTDetails() = viewModelScope.launch(dispatchers.main) {
        _otDetails.removeSource(otDetailsSource)
        withContext(dispatchers.io) {
            otDetailsSource = otId.value?.let { repo.getOTDetails(it) } ?: AbsentLiveData.create()
        }
        _otDetails.addSource(otDetailsSource) {
            _otDetails.value = it
        }
    }

    fun getOTDetailCom(otNumber: String) = viewModelScope.launch(dispatchers.main) {
        _otDetailsCom.removeSource(otDetailsComSource)
        withContext(dispatchers.io) {
            otDetailsComSource = repo.getOTDetailCom(otNumber)
        }
        _otDetailsCom.addSource(otDetailsComSource) {
            _otDetailsCom.value = it
        }
    }

    fun getContainerDetails(otNumber: String) = viewModelScope.launch(dispatchers.main) {
        _containerDetails.removeSource(containerDetailsSource)
        withContext(dispatchers.io) {
            containerDetailsSource =
                conId.value?.let { repo.getContainerDetails(it, otNumber) } ?: AbsentLiveData.create()
        }
        _containerDetails.addSource(containerDetailsSource) {
            _containerDetails.value = it
        }
    }

    fun getContainerDetailsWithBales(otNumber: String) = viewModelScope.launch(dispatchers.main) {
        _containerWithBalesDetails.removeSource(containerDetailsWithBalesSource)
        withContext(dispatchers.io) {
            containerDetailsWithBalesSource =
                conId.value?.let { repo.getContainerBaleDetails(it, otNumber) } ?: AbsentLiveData.create()
        }
        _containerWithBalesDetails.addSource(containerDetailsWithBalesSource) {
            _containerWithBalesDetails.value = it
        }
    }


    fun validateContainerId(id: String?): Boolean {
        return when {
            id.isNullOrBlank() || id.length != 11 || id.substring(0, 4).noOfLetters() != 4
                    || id.substring(4, 11).noOfLetters() > 0 -> false
            else -> true
        }
    }

    fun isAnyContainerInCompleteState(otNumber: String): Boolean {
        val containers = repo.getContainersByOtId(otNumber)
        return containers.any { it.status == ContainerStatus.Completed.id }
    }
    fun isAllContainerInCompleteState(otNumber: String): Boolean {
        val containers = repo.getContainersByOtId(otNumber)
        return containers.all{ it.status == ContainerStatus.Completed.id }
    }

    fun clearLastOT() {
        mOTWithContainers = null
        mSelectedOT = null
    }

    suspend fun updateOT(data: DispatchOT) = repo.updateOT(data)
    fun getOTById(otNumber: String) = repo.getOTById(otNumber)
    fun loadContainerAndBales(containerNo: String, otNumber: String) =
        repo.loadContainerAndBales(containerNo, otNumber)

    suspend fun insertOrReplaceBales(bales: List<PortBale>) = repo.insertOrReplaceBales(bales)

    //suspend fun insertOrReplaceBale(bale: PortBale) = repo.insertOrReplaceBale(bale)
    //suspend fun getBaleDeatils(baleId: String) = repo.getBaleDeatils(baleId)
    suspend fun insertOrReplaceContainer(container: Container) =
        repo.insertOrReplaceContainer(container)

    suspend fun deleteAllBalesByOtId(otNumber: String) = repo.deleteAllBalesByOtId(otNumber)
    fun setSelectedOT(data: DispatchOTWithContainers) {
        data.dispatchOT.let {
            mSelectedOT = it
            it.containers?.let { mOTWithContainers = data }
        }
    }

    suspend fun insertOrReplaceOTs(it: List<DispatchOT>) = repo.insertOrReplaceOTs(it)
    fun getOTsLocal() = repo.getOTsLocal()

    fun setOTs(it: List<DispatchOT>?) {
        val data = ArrayList<String>()
        data.add("Select OT Number")
        it?.forEach {
            data.add(it.otNumber)
        }
        mOTs = data
    }

    fun setContainerId(containerNumber: String) = conId.postValue(containerNumber)

    fun getContainerWithBaleDetail(containerId: String, otNumber: String) =
        repo.getContainerAndBale(containerId, otNumber)

    fun deleteContainer(containerId: String) {
        repo.deleteContainer(containerId)
    }

    fun updateContainerAndBaleDetail(containerId: String, otNumber: String) {
        val data = repo.getContainerAndBale(containerId, otNumber)
        try {
            data.let {
                mContainer = it.container
                mBaleList = it.bales.filter { it.otNumber == otNumber }.reversed()
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    fun validateBaleId(id: String?): Boolean {
        return when {
            id.isNullOrBlank() || id.contains(" ") || id.length != 10 || id.substring(0, 9)
                .noOfLetters() > 0 -> false
            else -> true
        }
    }

    fun setBale(bale: PortBale?) {
        mBale = bale
    }

    fun deleteBaleFromDB(bale: PortBale) {
        repo.deleteBaleFromDB(bale)
    }

    fun insertOrReplaceBale(bale: PortBale) = viewModelScope.launch {
        withContext(dispatchers.io) {
            repo.insertOrReplaceBale(bale)
        }
    }

    fun getBaleDeatils(baleId: String) =
        viewModelScope.launch(dispatchers.main) {
            _dbBaleDetails.removeSource(dbBaleDetailsSource)
            withContext(dispatchers.io) {
                dbBaleDetailsSource = repo.getBaleDeatils(baleId)
            }
            _dbBaleDetails.addSource(dbBaleDetailsSource) {
                _dbBaleDetails.value = it
            }
        }
    fun updateOTNumberToLocalDB(otNumber: String, containerNumber: String) =
            repo.updateOTNumberLocalDB(otNumber, containerNumber)

    fun updateSplitOTNumberDispatch(otNumber: String, oldOTNumber: String, containerList: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        _updateOTNumber.removeSource(updateOTNumberSource)
        withContext(dispatchers.io) {
            updateOTNumberSource = repo.updateOTNumberDispatch(otNumber, oldOTNumber, containerList)
        }
        _updateOTNumber.addSource(updateOTNumberSource) {
            _updateOTNumber.value = it
        }
    }
    fun updateSplitContainer(isSelected: Boolean, containerNumber: String) =
            repo.updateSplitContainer(isSelected, containerNumber)
}
