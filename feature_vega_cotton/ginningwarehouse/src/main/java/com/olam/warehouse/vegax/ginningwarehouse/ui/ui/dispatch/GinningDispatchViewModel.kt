package com.olam.warehouse.ginning.ui.dispatch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.ginning.data.model.DispatchPostResponse
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Grade
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.VegaCottonGinningDispatchDelivery
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.DeliveryWithBales
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.DeliveryWithGrades
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.extensions.noOfLetters
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.ginningwarehouse.data.domain.VegaCottonDispatchUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 3/20/2020.
 */

class GinningDispatchViewModel(
    private val useCase: VegaCottonDispatchUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {
    var Deliverys = emptyList<String>()
    var lastSelectedPosition = 0
    var mBaleList = mutableListOf<Bale>()
    var mDeliveryList = emptyList<VegaCottonGinningDispatchDelivery>()
    var mCurrentDelivery = VegaCottonGinningDispatchDelivery()
    var baleHashMap: HashMap<String, List<Bale>> = HashMap<String, List<Bale>>()
    var mCurrentBaleList: HashMap<String, List<Bale>> = HashMap<String, List<Bale>>()

    private var deliveryWithBalesSource: LiveData<DeliveryWithBales> =
        MutableLiveData()
    private val _deliveryWithBales = MediatorLiveData<DeliveryWithBales>()
    val deliveryWithBales: LiveData<DeliveryWithBales> get() = _deliveryWithBales

    private var allDeliveryWithBalesSource: LiveData<List<DeliveryWithBales>> =
        MutableLiveData()
    private val _allDeliveryWithBales = MediatorLiveData<List<DeliveryWithBales>>()
    val allDeliveryWithBales: LiveData<List<DeliveryWithBales>> get() = _allDeliveryWithBales


    private var getBaleDetailsByBaleIdOfflineSource: LiveData<Bale> =
        MutableLiveData()
    private val _getBaleDetailsByBaleIdOffline = MediatorLiveData<Bale>()
    val getBaleDetailsByBaleIdOffline: LiveData<Bale> get() = _getBaleDetailsByBaleIdOffline


    private var deliveryWithGradesSource: LiveData<DeliveryWithGrades> =
        MutableLiveData()
    private val _deliveryWithGrades = MediatorLiveData<DeliveryWithGrades>()
    val deliveryWithGrades: LiveData<DeliveryWithGrades> get() = _deliveryWithGrades


    private var deliveryDetailsOfflineSourceVegaCotton: LiveData<List<VegaCottonGinningDispatchDelivery>> =
        MutableLiveData()
    private val _deliveryDetailsOffline = MediatorLiveData<List<VegaCottonGinningDispatchDelivery>>()
    val deliveryDetailsOfflineVegaCotton: LiveData<List<VegaCottonGinningDispatchDelivery>> get() = _deliveryDetailsOffline

    private var existedDeliverysSource: LiveData<List<VegaCottonGinningDispatchDelivery>> =
        MutableLiveData()
    private val _existedDeliverys = MediatorLiveData<List<VegaCottonGinningDispatchDelivery>>()
    val existedDeliverys: LiveData<List<VegaCottonGinningDispatchDelivery>> get() = _existedDeliverys



    private var validateBaleSource: LiveData<Resource<GenericReqAndResp<Bale>>> =
        MutableLiveData()
    private val _validateBale = MediatorLiveData<Resource<GenericReqAndResp<Bale>>>()
    val validateBale: LiveData<Resource<GenericReqAndResp<Bale>>> get() = _validateBale


    private var postDispatchSource
            : LiveData<Resource<GenericReqAndResp<DispatchPostResponse>>> =
        MutableLiveData()
    private val _postDispatch = MediatorLiveData<Resource<GenericReqAndResp<DispatchPostResponse>>>()
    val postDispatch: LiveData<Resource<GenericReqAndResp<DispatchPostResponse>>> get() = _postDispatch


    private var deliveryDetailsSourceVegaCotton: LiveData<Resource<GenericReqAndResp<List<VegaCottonGinningDispatchDelivery>>>> =
        MutableLiveData()
    private val _deliveryDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCottonGinningDispatchDelivery>>>>()
    val deliveryDetailsVegaCotton: LiveData<Resource<GenericReqAndResp<List<VegaCottonGinningDispatchDelivery>>>> get() = _deliveryDetails

    fun fetchDeliveryDetails() = viewModelScope.launch(dispatchers.main) {
        _deliveryDetails.removeSource(deliveryDetailsSourceVegaCotton) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            deliveryDetailsSourceVegaCotton = useCase.fetchDeliveryDetails()
        }
        _deliveryDetails.addSource(deliveryDetailsSourceVegaCotton) {
            _deliveryDetails.value = it
        }
    }

    private var offlineDeliveryDetailsSourceVegaCotton: LiveData<List<VegaCottonGinningDispatchDelivery>> =
        MutableLiveData()
    private val _offlineDeliveryDetails= MediatorLiveData<List<VegaCottonGinningDispatchDelivery>>()
    val offlineDeliveryDetailsVegaCotton: LiveData<List<VegaCottonGinningDispatchDelivery>> get() = _offlineDeliveryDetails

    fun insertOrReplaceDelivery(ot:VegaCottonGinningDispatchDelivery)  = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.insertOrReplaceDelivery(ot)
        }
    }

    fun insertOrReplaceGrade(grade: Grade)  = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.insertOrReplaceGrade(grade)
        }
    }

    fun fetchDeliveryDetailsOffline() = viewModelScope.launch(dispatchers.main) {
        _deliveryDetailsOffline.removeSource(deliveryDetailsOfflineSourceVegaCotton) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            deliveryDetailsOfflineSourceVegaCotton = useCase.fetchDeliveryDetailsOffline()
        }
        _deliveryDetailsOffline.addSource(deliveryDetailsOfflineSourceVegaCotton) {
            _deliveryDetailsOffline.value = it
        }
    }

    fun fetchOfflineDeliveryDetails() = viewModelScope.launch(dispatchers.main) {
        _offlineDeliveryDetails.removeSource(offlineDeliveryDetailsSourceVegaCotton) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offlineDeliveryDetailsSourceVegaCotton = useCase.fetchOfflineDeliveryDetails()
        }
        _offlineDeliveryDetails.addSource(offlineDeliveryDetailsSourceVegaCotton) {
            _offlineDeliveryDetails.value = it
        }
    }


    fun fetchExistedDeliverys() = viewModelScope.launch(dispatchers.main) {
        _existedDeliverys.removeSource(existedDeliverysSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            existedDeliverysSource = useCase.fetchExistedDelivery()
        }
        _existedDeliverys.addSource(existedDeliverysSource) {
            _existedDeliverys.value = it
        }
    }


    fun setDelivery(it: List<VegaCottonGinningDispatchDelivery>?) {
        if (it != null) {
            mDeliveryList = it
        }
        val data = ArrayList<String>()
        data.add("Select Delivery Number")
        it?.forEach {
            data.add(it.deliveryNumber)
        }
        Deliverys = data
    }


    fun getDeliveryWithGrades(deliverNo: String)= viewModelScope.launch(dispatchers.main) {
        _deliveryWithGrades.removeSource(deliveryWithGradesSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            deliveryWithGradesSource = useCase.getDeliveryWithGrades(deliverNo)
        }
        _deliveryWithGrades.addSource(deliveryWithGradesSource) {
            _deliveryWithGrades.value = it
        }
    }



    fun getDeliveryWithBales(deliverNo: String)= viewModelScope.launch(dispatchers.main) {
        _deliveryWithBales.removeSource(deliveryWithBalesSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            deliveryWithBalesSource = useCase.getDeliveryWithBales(deliverNo)
        }
        _deliveryWithBales.addSource(deliveryWithBalesSource) {
            _deliveryWithBales.value = it
        }
    }

    fun getAllDeliveryWithBales()= viewModelScope.launch(dispatchers.main) {
        _allDeliveryWithBales.removeSource(allDeliveryWithBalesSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            allDeliveryWithBalesSource = useCase.getAllDeliveryWithBales()
        }
        _allDeliveryWithBales.addSource(allDeliveryWithBalesSource) {
            _allDeliveryWithBales.value = it
        }
    }

    fun getBaleDetailsById(deliverNo: String)= viewModelScope.launch(dispatchers.main) {
        _getBaleDetailsByBaleIdOffline.removeSource(getBaleDetailsByBaleIdOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getBaleDetailsByBaleIdOfflineSource = useCase.getBaleDetailsByBaleId(deliverNo)
        }
        _getBaleDetailsByBaleIdOffline.addSource(getBaleDetailsByBaleIdOfflineSource) {
            _getBaleDetailsByBaleIdOffline.value = it
        }
    }


    suspend fun getBaleDetailsByIdOffline(deliverNo: String) = useCase.getBaleDetailsByBaleIdOffline(deliverNo)
    fun validateBale(baleId: String, deliverNo: String) = viewModelScope.launch(dispatchers.main) {
        _validateBale.removeSource(validateBaleSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            validateBaleSource = useCase.validateBale(baleId, deliverNo)
        }
        _validateBale.addSource(validateBaleSource) {
            _validateBale.value = it
        }
    }


    fun saveBale(bale: Bale)  = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveBale(bale)
        }
    }

    fun deleteBales(deliverNo: String)  = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteBale(deliverNo)
        }
    }
    fun updateDeliveryStatusToEdit(deliveryNo: String) = viewModelScope.launch(dispatchers.main) {

        withContext(dispatchers.io) {
            useCase.updateDeliveryStatusToEdit(deliveryNo)
        }
    }
    fun updateDeliverySaveOffline(deliveryNo: String)= viewModelScope.launch(dispatchers.main) {

        withContext(dispatchers.io) {
            useCase.updateDeliverySaveOffline(deliveryNo)
        }
    }
    fun deleteOfflineDeliveryWithBales(deliveryNumber: String)= viewModelScope.launch(dispatchers.main) {

        withContext(dispatchers.io) {
            useCase.deleteOfflineDeliveryWithBales(deliveryNumber)
        }
    }
    fun updateErrorMessage(msg: String, deliveryNumber: String)= viewModelScope.launch(dispatchers.main) {

        withContext(dispatchers.io) {
            useCase.updateErrorMessage(msg,deliveryNumber)
        }
    }

    fun validateBaleId(id: String?): Boolean {
        return when {
            id.isNullOrBlank() || id.contains(" ") || id.length != 10 || id.substring(
                0,
                9
            ).noOfLetters() > 0 -> false
            else -> true
        }
    }

    fun isAlreadyExistBale(id: String?): Boolean {
        return mBaleList.any { it.baleID == id }
    }

    fun postDispatch(deliveryDtoVegaCotton: VegaCottonGinningDispatchDelivery)= viewModelScope.launch(dispatchers.main) {
        _postDispatch.removeSource(postDispatchSource) /// We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            postDispatchSource = useCase.postDispatch(deliveryDtoVegaCotton)
        }
        _postDispatch.addSource(postDispatchSource) {
            _postDispatch.value = it
        }
    }



  /* fun fetchDeliveryDetails() = repo.fetchDeliveryDetails()*
   fun fetchOfflineDeliveryDetails() = repo.fetchOfflineDeliveryDetails()

    fun fetchOfflineDeliveryList() = repo.fetchOfflineDeliveryList()

    fun updateDeliveryStatusToEdit(deliveryNo: String) = repo.updateDeliveryStatusToEdit(deliveryNo)

    fun updateDeliverySaveOffline(deliveryNo: String) = repo.updateDeliverySaveOffline(deliveryNo)


    fun validateBaleId(id: String?): Boolean {
        return when {
            id.isNullOrBlank() || id.contains(" ") || id.length != 10 || id.substring(
                0,
                9
            ).noOfLetters() > 0 -> false
            else -> true
        }
    }

    fun isAlreadyExistBale(id: String?): Boolean {
        return mBaleList.any { it.baleID == id }
    }



    fun saveBaleDetails(bale: Bale) = repo.saveBaleDetails(bale)

    fun postDispatch(deliveryDto: GinningDispatchDelivery) = repo.postDispatch(deliveryDto)

    fun deleteBales(deliveryNo: String) = repo.deleteBales(deliveryNo)

    fun deleteOfflineDeliveryWithBales(deliveryNumber: String) =
        repo.deleteOfflineDeliveryWithBales(deliveryNumber)

    fun updateErrorMessage(msg: String, deliveryNumber: String) =
        repo.updateErrorMessage(msg, deliveryNumber)

    fun getBaleDetails(baleId: String) = repo.getBaleDetails(baleId)*/

}
