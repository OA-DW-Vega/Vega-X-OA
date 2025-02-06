package com.olam.warehouse.vegax.portwarehouse.ui.dispatch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.Container
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortBale
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.portwarehouse.data.repository.DirectDispatchRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 4/1/2021.
 */
class PortDispatchSealViewModel constructor(
    val repo: DirectDispatchRepository,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {

    var mContainer: Container? = null
    var mBale: PortBale? = null
    var mBaleList = emptyList<PortBale>()

    fun validateSealId(id: String?): Boolean {
        return when {
            id.isNullOrBlank() || id.length < 10 || id.contains(" ") -> false
            else -> true
        }
    }

    private var sealContainerSource: LiveData<Resource<GenericReqAndResp<GenericMessage>>> = MutableLiveData()
    private val _sealContainer = MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val sealContainer: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _sealContainer

    private var containerDetailsWithBalesSource: LiveData<Resource<GenericReqAndResp<Container>>> = MutableLiveData()
    private val _containerWithBalesDetails = MediatorLiveData<Resource<GenericReqAndResp<Container>>>()
    val containerWithBalesDetails: LiveData<Resource<GenericReqAndResp<Container>>> get() = _containerWithBalesDetails

    private var containerDetailsSource: LiveData<Resource<GenericReqAndResp<Container>>> = MutableLiveData()
    private val _containerDetails = MediatorLiveData<Resource<GenericReqAndResp<Container>>>()
    val containerDetails: LiveData<Resource<GenericReqAndResp<Container>>> get() = _containerDetails

    private var breakSealSource: LiveData<Resource<GenericReqAndResp<GenericMessage>>> = MutableLiveData()
    private val _breakSeal = MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val breakSeal: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _breakSeal

    fun sealContainer(sealId: String, containerId: String, otNumber: String, direct: Boolean) =
        viewModelScope.launch(dispatchers.main) {
            _sealContainer.removeSource(sealContainerSource)
            withContext(dispatchers.io) {
                sealContainerSource = repo.sealContainer(sealId, containerId, otNumber, direct)
            }
            _sealContainer.addSource(sealContainerSource) {
                _sealContainer.value = it
            }
        }

    fun updateSealIDToLocalDB(sealNumber: String, containerNumber: String) =
        repo.updateSealIDToLocalDB(sealNumber, containerNumber)

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

    fun getContainerDetails(containerNumber: String, otNumber: String) = viewModelScope.launch(dispatchers.main) {
        _containerDetails.removeSource(containerDetailsSource)
        withContext(dispatchers.io) {
            containerDetailsSource = repo.getContainerDetails(containerNumber, otNumber)
        }
        _containerDetails.addSource(containerDetailsSource) {
            _containerDetails.value = it
        }
    }

    fun getContainerDetailsWithBales(containerNumber: String, otNumber: String) =
        viewModelScope.launch(dispatchers.main) {
            _containerWithBalesDetails.removeSource(containerDetailsWithBalesSource)
            withContext(dispatchers.io) {
                containerDetailsWithBalesSource = repo.getContainerBaleDetails(containerNumber, otNumber)
            }
            _containerWithBalesDetails.addSource(containerDetailsWithBalesSource) {
                _containerWithBalesDetails.value = it
            }
        }

    fun doBreakSealContainer(otNumber: String, containerNumber: String, sealNumber: String) =
        viewModelScope.launch(dispatchers.main) {
            _breakSeal.removeSource(breakSealSource)
            withContext(dispatchers.io) {
                breakSealSource = repo.doBreakSealContainer(otNumber, containerNumber, sealNumber)
            }
            _breakSeal.addSource(breakSealSource) {
                _breakSeal.value = it
            }
        }

    suspend fun insertOrReplaceBales(bales: List<PortBale>) = repo.insertOrReplaceBales(bales)
    suspend fun insertOrReplaceContainer(container: Container) = repo.insertOrReplaceContainer(container)
    suspend fun deleteAllBalesByOtId(otNumber: String) = repo.deleteAllBalesByOtId(otNumber)
    fun loadContainerAndBales(containerNo: String, otNumber: String) = repo.loadContainerAndBales(containerNo, otNumber)

}
