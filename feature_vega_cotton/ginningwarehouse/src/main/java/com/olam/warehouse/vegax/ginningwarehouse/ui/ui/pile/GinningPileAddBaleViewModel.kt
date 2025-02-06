package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.pile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.GinningPileStorageLocationModel
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.GinningPileSuccessResponse
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.PileBale
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.extensions.noOfLetters
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.ginningwarehouse.data.domain.VegaCottonPileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class GinningPileAddBaleViewModel (
    private val useCase: VegaCottonPileUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var getExistedBaleListSource:LiveData<List<PileBale>> =
        MutableLiveData()
    private val _getExistedBaleList= MediatorLiveData<List<PileBale>>()
    val getExistedBaleList: LiveData<List<PileBale>> get() = _getExistedBaleList


    private var getPileLocationsSource: LiveData<Resource<GenericReqAndResp<List<GinningPileStorageLocationModel>>>> =
        MutableLiveData()
    private val _getPileLocations = MediatorLiveData<Resource<GenericReqAndResp<List<GinningPileStorageLocationModel>>>>()
    val getPileLocations: LiveData<Resource<GenericReqAndResp<List<GinningPileStorageLocationModel>>>> get() = _getPileLocations

    private var postPileSource: LiveData<Resource<GenericReqAndResp<GinningPileSuccessResponse>>> = MutableLiveData()
    private val _postPile = MediatorLiveData<Resource<GenericReqAndResp<GinningPileSuccessResponse>>>()
    val postPile: LiveData<Resource<GenericReqAndResp<GinningPileSuccessResponse>>>get() = _postPile



    private var validateBaleSource: LiveData<Resource<GenericReqAndResp<List<PileBale>>>> =
        MutableLiveData()
    private val _validateBale = MediatorLiveData<Resource<GenericReqAndResp<List<PileBale>>>>()
    val validateBale: LiveData<Resource<GenericReqAndResp<List<PileBale>>>> get() = _validateBale

    var mBaleHashMap: HashMap<String, List<PileBale>> = HashMap<String, List<PileBale>>()
    var mCurrentBaleList: HashMap<String, List<PileBale>> = HashMap<String, List<PileBale>>()
    var mCurrentPiles = GinningPileStorageLocationModel()

    fun validateBaleId(id: String?): Boolean {
        return when {
            id.isNullOrBlank() || id.contains(" ") || id.length != 10 || id.substring(0, 9)
                .noOfLetters() > 0 -> false
            else -> true
        }
    }


    fun getStorageLocationList() = viewModelScope.launch(dispatchers.main) {
        _getPileLocations.removeSource(getPileLocationsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getPileLocationsSource = useCase.getPileLocations()
        }
        _getPileLocations.addSource(getPileLocationsSource) {
            _getPileLocations.value = it
        }
    }

   suspend fun getBaleListByStorageId(id: String): List<PileBale> = useCase.getBaleListByStorageId(id)

    fun getExistedBaleList() = viewModelScope.launch(dispatchers.main) {
        _getExistedBaleList.removeSource(getExistedBaleListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getExistedBaleListSource = useCase.getExistedBaleList()
        }
        _getExistedBaleList.addSource(getExistedBaleListSource) {
            _getExistedBaleList.value = it
        }
    }

    fun validateBaleWithAPI(id: String, storageId: String)= viewModelScope.launch(dispatchers.main) {
        _validateBale.removeSource(validateBaleSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            validateBaleSource = useCase.validateBale(id,storageId)
        }
        _validateBale.addSource(validateBaleSource) {
            _validateBale.value = it
        }
    }

    fun removeBaleFromStorage(bale: PileBale)  = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.removeBaleFromStorage(bale)
        }
    }


    fun deleteBalesDB(pileId: String)  = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteBalesDB(pileId)
        }
    }

    fun saveListBaletoDB(listBale: List<PileBale>) {

        runBlocking {
            withContext(Dispatchers.IO) {
                useCase.insertBaleToStorage(listBale)
            }
        }

    }

    fun postPile(mCurrentPiles: GinningPileStorageLocationModel)= viewModelScope.launch(dispatchers.main) {
        _postPile.removeSource(postPileSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            postPileSource = useCase.postPile(mCurrentPiles)
        }
        _postPile.addSource(postPileSource) {
            _postPile.value = it
        }
    }

   /* suspend fun removeBaleFromStorage(bale: PileBale) = repo.removeBale(bale)
    fun saveListBale(data: List<PileBale>?) {}
    fun postPile(mCurrentPiles: GinningPileStorageLocationModel) = repo.postPile(mCurrentPiles)
    fun deleteBalesDB(pileId: String) = repo.deleteBalesDB(pileId)*/
}
