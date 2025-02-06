package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.incomingmtn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Mtn
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnWithBales
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnWithGrades
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain.VegaCottonIncomingLotsIncomingMtnUseCase
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.enums.BaleStatus
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GinningIncomingMtnViewModel (private val useCase: VegaCottonIncomingLotsIncomingMtnUseCase,
                                   private val dispatchers: AppDispatchers
) :
        BaseViewModel() {

    private var getMtnSource:  LiveData<Resource<GenericReqAndResp<List<Mtn>>>> =
            MutableLiveData()
    private val _getMtn = MediatorLiveData<Resource<GenericReqAndResp<List<Mtn>>>>()
    val getMtn: LiveData<Resource<GenericReqAndResp<List<Mtn>>>> get() = _getMtn

    private var getOfflineMtnWithBalesSource:  LiveData<List<MtnWithBales>> =
            MutableLiveData()
    private val _getOfflineMtnWithBales = MediatorLiveData<List<MtnWithBales>>()
    val getOfflineMtnWithBales: LiveData<List<MtnWithBales>> get() = _getOfflineMtnWithBales

    private var getMtnWithBalesOfflineSource:  LiveData<MtnWithBales> = MutableLiveData()
    private val _getMtnWithBalesOffline= MediatorLiveData<MtnWithBales>()
    val getMtnWithBalesOffline: LiveData<MtnWithBales> get() = _getMtnWithBalesOffline

    private var getMtnOfflineSource:  LiveData<List<Mtn>> = MutableLiveData()
    private val _getMtnOffline= MediatorLiveData<List<Mtn>>()
    val getMtnOffline: LiveData<List<Mtn>> get() = _getMtnOffline

    private var getMtnWithGradesOfflineSource:  LiveData<List<MtnWithGrades>> = MutableLiveData()
    private val _getMtnWithGradesOffline= MediatorLiveData<List<MtnWithGrades>>()
    val getMtnWithGradesOffline: LiveData<List<MtnWithGrades>> get() = _getMtnWithGradesOffline

    fun getMtnsOffline() = viewModelScope.launch(dispatchers.main) {
        _getMtnOffline.removeSource(getMtnOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getMtnOfflineSource = useCase.getMtnsOffline()
        }
        _getMtnOffline.addSource(getMtnOfflineSource) {
            _getMtnOffline.value = it
        }
    }

    fun getMtnsWithGradesOffline() = viewModelScope.launch(dispatchers.main) {
        _getMtnWithGradesOffline.removeSource(getMtnWithGradesOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getMtnWithGradesOfflineSource = useCase.getMtnsWithGradesOffline()
        }
        _getMtnWithGradesOffline.addSource(getMtnWithGradesOfflineSource) {
            _getMtnWithGradesOffline.value = it
        }
    }

     fun insertMtn(mtn: Mtn)=viewModelScope.launch (dispatchers.main){
         withContext(dispatchers.io) {
             useCase.insertMtn(mtn)
         }
     }


    fun deleteMtnsByMtnId(mtnId: String)=viewModelScope.launch (dispatchers.main){
        withContext(dispatchers.io) {
            useCase.deleteMtnsByMtnId(mtnId)
        }
    }
    private var mIncomingMtnList: MutableList<MtnWithGrades> = mutableListOf()
    private val mSearchList: MutableList<MtnWithGrades> = mutableListOf()
    var mUIMtnList = MutableLiveData<MutableList<MtnWithGrades>>()


    fun getMtns() = viewModelScope.launch(dispatchers.main) {
        _getMtn.removeSource(getMtnSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getMtnSource = useCase.getMtns()
        }
        _getMtn.addSource(getMtnSource) {
            _getMtn.value = it
        }
    }


    fun updateMtn(mtn: Mtn) = viewModelScope.launch(dispatchers.main) {
        mtn.classificationInProgress = 1
        withContext(dispatchers.io) {
            useCase.updateMtn(mtn)
        }
    }

    fun updateMtnBalesStatusVerified(mtnId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateMtnBalesStatusVerified(mtnId)
        }
    }

    fun getMtnWithBales(mtnId: String) = viewModelScope.launch(dispatchers.main) {
        _getMtnWithBalesOffline.removeSource(getMtnWithBalesOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            getMtnWithBalesOfflineSource = useCase.getMtnWithBales(mtnId)
        }
        _getMtnWithBalesOffline.addSource(getMtnWithBalesOfflineSource) {
            _getMtnWithBalesOffline.value = it
        }
    }


    fun filter(text: String?) {
        mSearchList.clear()

        mIncomingMtnList.forEach { mtn ->
            text?.let { text ->
                if (mtn.mtn.mtnNumber.contains(text)) {
                    mSearchList.add(mtn)
                }
            }
        }
        mUIMtnList.value = mSearchList
    }

    fun updateMtnList(incomingMtn: MtnWithGrades) {
        if (mSearchList.isNotEmpty()) {
            mIncomingMtnList.forEach {
                it.mtn.isChecked = it.mtn.mtnNumber == incomingMtn.mtn.mtnNumber
            }
        }
    }

    fun getSelectedMtn(): MtnWithGrades {
        return mIncomingMtnList.single {
            it.mtn.isChecked
        }
    }

    fun setMtnList(mtnList: List<MtnWithGrades>?) {
        /*this.mIncomingMtnList = mtnList?.filter { mtn ->
            mtn.mtnBales?.isNotEmpty() ?: false
        }?.toMutableList() ?: mutableListOf()*/
        this.mIncomingMtnList = mtnList?.toMutableList() ?: mutableListOf()
        mUIMtnList.value = this.mIncomingMtnList
    }

    fun displayMtnList() {
        mSearchList.clear()
        mUIMtnList.value = mIncomingMtnList
    }



    /*fun offloadBales(mtnId: String): LiveData<Resource<GenericResponse<GenericMessage>>> {
        val mtnWithBales = getMtnWithBales(mtnId)
        val mtnModel = getMtnModel(mtnWithBales)

        return repo.postMtnWithBales(mtnModel)
    }*/


    fun getMtnModel(mtnWithBales: MtnWithBales): Mtn {
        val mtn = mtnWithBales.mtn
        val bales = mtnWithBales.bales

        bales.forEach {
            it.toSloc = BaleStatus.Good.id.toString()
            it.baleStatus = BaleStatus.Good.status
            it.isVerified = 1
        }

        return Mtn(
            mtnNumber = mtn.mtnNumber,
            suplierPlantId = mtn.suplierPlantId,
            suplierPlantDesc = mtn.suplierPlantDesc,
            recievedPlantId = mtn.recievedPlantId,
            recievedPlantDesc = mtn.recievedPlantDesc,
            lineItem = mtn.lineItem,
            containerNo = mtn.containerNo,
            grade = mtn.grade,
            uom = mtn.uom,
            sourceNetWeight = mtn.sourceNetWeight,
            baleCount = mtn.baleCount,
            truckNumber = mtn.truckNumber,
            istogrnPost = "X",
            itransferPost = "",
            mtnBales = bales
        )
    }

    fun getGrades(mtnNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.getGrades(mtnNumber)
        }
    }
       //fun deleteAllMtns() = repo.deleteAllMtns()
    //fun deleteAllMtnBales() = repo.deleteAllMtnBales()
    fun getOfflineMtnWithBales ()= viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.getOfflineMtnWithBales()
        }
           _getOfflineMtnWithBales.removeSource(getOfflineMtnWithBalesSource) // We make sure there is only one source of livedata (allowing us properly refresh)
           withContext(dispatchers.io) {
               getOfflineMtnWithBalesSource = useCase.getOfflineMtnWithBales()
           }
           _getOfflineMtnWithBales.addSource(getOfflineMtnWithBalesSource) {
               _getOfflineMtnWithBales.value = it
           }
    }
   // fun getMtnsWithBales() = repo.getMtnsWithBales()

  //  fun updateOfflineMtnStatus(mtnId: String) = repo.updateOfflineMtnStatus(mtnId)


    fun updateofflineMtnRevertStatus(mtnId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateOfflineMtnSReverttatus(mtnId)
        }
    }

}
