package com.olam.warehouse.portwarehouse.ui.pile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileStorageLocationModel
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileSuccessResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.extensions.noOfLetters
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.portwarehouse.data.domain.VegaCottonPortWareHousePileUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PortPileAddBaleViewModel(
    private val useCase: VegaCottonPortWareHousePileUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {
    var baleHashMap: HashMap<String, List<PortPileBale>> = HashMap<String, List<PortPileBale>>()
    var mCurrentBaleList: HashMap<String, List<PortPileBale>> = HashMap<String, List<PortPileBale>>()
    var mCurrentPiles = PortPileStorageLocationModel()

    private var isAlreadyExistBaleSource: LiveData<Int> =
            MutableLiveData()
    private val _isAlreadyExistBale = MediatorLiveData<Int>()
    val isAlreadyExistBale: LiveData<Int> get() = _isAlreadyExistBale

    private var getBaleListByStorageIdSource: LiveData<List<PortPileBale>> =
            MutableLiveData()
    private val _getBaleListByStorageId = MediatorLiveData<List<PortPileBale>>()
    val getBaleListByStorageId: LiveData<List<PortPileBale>> get() = _getBaleListByStorageId

    private var validateBaleWithAPISource: LiveData<Resource<GenericReqAndResp<PortPileBale>>> =
            MutableLiveData()
    private val _validateBaleWithAPI = MediatorLiveData<Resource<GenericReqAndResp<PortPileBale>>>()
    val validateBaleWithAPI: LiveData<Resource<GenericReqAndResp<PortPileBale>>> get() = _validateBaleWithAPI

    private var getStorageLocationListSource: LiveData<Resource<GenericReqAndResp<List<PortPileStorageLocationModel>>>> =
            MutableLiveData()
    private val _getStorageLocationList = MediatorLiveData<Resource<GenericReqAndResp<List<PortPileStorageLocationModel>>>>()
    val getStorageLocationList: LiveData<Resource<GenericReqAndResp<List<PortPileStorageLocationModel>>>> get() = _getStorageLocationList


    private var postPileSource: LiveData<Resource<GenericReqAndResp<PortPileSuccessResponse>>> =
            MutableLiveData()
    private val _postPile = MediatorLiveData<Resource<GenericReqAndResp<PortPileSuccessResponse>>>()
    val postPile: LiveData<Resource<GenericReqAndResp<PortPileSuccessResponse>>> get() = _postPile


    fun validateBaleId(id: String?): Boolean {
        return when {
            id.isNullOrBlank() || id.contains(" ") || id.length != 10 || id.substring(0, 9)
                .noOfLetters() > 0 -> false
            else -> true
        }
    }

    fun getStorageLocationList() = viewModelScope.launch(dispatchers.main) {
        _getStorageLocationList.removeSource(getStorageLocationListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {

            getStorageLocationListSource = useCase.getStorageLocationList()

        }
        _getStorageLocationList.addSource(getStorageLocationListSource) {
            _getStorageLocationList.value = it
        }
    }


    fun validateBaleWithAPI(id: String, storageId: String)= viewModelScope.launch(dispatchers.main) {
        _validateBaleWithAPI.removeSource(validateBaleWithAPISource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {

            validateBaleWithAPISource = useCase.validateBale(id,storageId)

        }
        _validateBaleWithAPI.addSource(validateBaleWithAPISource) {
            _validateBaleWithAPI.value = it
        }
    }

    fun insertBaleToStorage(bale: PortPileBale) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {

            useCase.insertBale(bale)
        }
    }

    suspend fun isAlreadyExistBale(baleId: String) = useCase.isAlreadyExistBale(baleId)
    suspend fun getBaleListByStorageIdOffline(id: String) = useCase.getBaleByLocationIdOffline(id)
    fun getBaleListByStorageId(id: String) = viewModelScope.launch(dispatchers.main) {
        _getBaleListByStorageId.removeSource(getBaleListByStorageIdSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {

            getBaleListByStorageIdSource = useCase.getBaleByLocationId(id)
        }
        _getBaleListByStorageId.addSource(getBaleListByStorageIdSource) {
            _getBaleListByStorageId.value = it
        }
    }


    fun saveListBale(data: List<PortPileBale>?) {

    }
    fun postPile(mCurrentPiles: PortPileStorageLocationModel) = viewModelScope.launch(dispatchers.main) {
        _postPile.removeSource(postPileSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {

            postPileSource = useCase.postPile(mCurrentPiles)

        }
        _postPile.addSource(postPileSource) {
            _postPile.value = it
        }
    }

    fun deleteBalesDB(pileId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {

            useCase.deleteBalesDB(pileId)
        }
    }

    fun removeBaleFromStorage(bale: PortPileBale)= viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {

            useCase.removeBale(bale)
        }
    }
}
