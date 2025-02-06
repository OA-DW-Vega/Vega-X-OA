package com.olam.warehouse.vegax.portwarehouse.ui.incoming

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnWithBales
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnWithGrades
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtnBales
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileStorageLocationModel
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.MtnNumber
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.extensions.noOfLetters
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.portwarehouse.data.model.Deliverylist
import com.olam.warehouse.vegax.portwarehouse.data.repository.PortIncomingRepository
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.vegax.portwarehouse.utils.enums.BaleStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Created by Baskaran Kannan on 4/5/2021.
 */

class PortIncomingMtnViewModel constructor(
    private val repo: PortIncomingRepository,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var mIncomingMtnList: MutableList<MtnWithGrades> = mutableListOf()
    private val mSearchList: MutableList<MtnWithGrades> = mutableListOf()
    var mUIMtnList = MutableLiveData<MutableList<MtnWithGrades>>()

    private var incomingMtntSource: MutableList<MtnNumber> = mutableListOf()
    private val _incomingMtns: MutableList<MtnNumber> = mutableListOf()
    var incomingMtns = MutableLiveData<MutableList<MtnNumber>>()

    private var listMtnsSource: LiveData<Resource<GenericReqAndResp<List<PortMtn>>>> = MutableLiveData()
    private val _listMtns = MediatorLiveData<Resource<GenericReqAndResp<List<PortMtn>>>>()
    val listMtns: LiveData<Resource<GenericReqAndResp<List<PortMtn>>>> get() = _listMtns

    private var listMtnWithGradesSource: LiveData<List<MtnWithGrades>> = MutableLiveData()
    private val _listMtnWithGrades = MediatorLiveData<List<MtnWithGrades>>()
    val listMtnWithGrades: LiveData<List<MtnWithGrades>> get() = _listMtnWithGrades

    private var offloadBalesSource: LiveData<Resource<GenericReqAndResp<GenericMessage>>> = MutableLiveData()
    private val _offloadBales = MediatorLiveData<Resource<GenericReqAndResp<GenericMessage>>>()
    val offloadBales: LiveData<Resource<GenericReqAndResp<GenericMessage>>> get() = _offloadBales

    private var getStorageLocationListSource: LiveData<Resource<GenericReqAndResp<List<PortPileStorageLocationModel>>>> =
        MutableLiveData()
    private val _getStorageLocationList = MediatorLiveData<Resource<GenericReqAndResp<List<PortPileStorageLocationModel>>>>()
    val getStorageLocationList: LiveData<Resource<GenericReqAndResp<List<PortPileStorageLocationModel>>>> get() = _getStorageLocationList





    fun postMtnWithBales(mtnModel: PortMtn) = viewModelScope.launch(dispatchers.main) {
        _offloadBales.removeSource(offloadBalesSource)
        withContext(dispatchers.io) {
            offloadBalesSource = repo.postMtnWithBales(mtnModel)
        }
        _offloadBales.addSource(offloadBalesSource) {
            _offloadBales.value = it
        }
    }

    suspend fun insertMtn(mtn: PortMtn) = repo.insertMtn(mtn)
    suspend fun getMtnsOffline() = repo.getMtnsOffline()
    fun getListMtnWithBales() = viewModelScope.launch(dispatchers.main) {
        _listMtnWithGrades.removeSource(listMtnWithGradesSource)
        withContext(dispatchers.io) {
            listMtnWithGradesSource = repo.getListMtnWithBales()
        }
        _listMtnWithGrades.addSource(listMtnWithGradesSource) {
            _listMtnWithGrades.value = it
        }
    }

    fun getMtns(baleList: Deliverylist) = viewModelScope.launch(dispatchers.main) {
        _listMtns.removeSource(listMtnsSource)
        withContext(dispatchers.io) {
            listMtnsSource = repo.getIncomingMtnList(baleList)
        }
        _listMtns.addSource(listMtnsSource) {
            _listMtns.value = it
        }
    }
    fun getScannedBaleid(baleId: String)  {
        _incomingMtns.clear()
        incomingMtntSource.forEach {
                it.baleID=baleId
                    _incomingMtns.add(it)

        }
        incomingMtns.value = _incomingMtns

    }

    fun updateMtn(mtn: PortMtn) {
        mtn.classificationInProgress = 1
        runBlocking {
            withContext(Dispatchers.IO) {
                repo.updateMtn(mtn)
            }
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

    fun updateMtnBale(mtnBale: PortMtnBales?, baleStatus: BaleStatus?) {
        mtnBale?.let {
            it.isVerified = 1
            baleStatus?.let { baleStatus ->
                it.toSloc =
                    if(getCurrentKey().split("_")[2].contains("COTTPORT") && baleStatus.id==1001){
                        PortWHUtil.getStorageID()
                    }else{
                        baleStatus.id.toString()
                    }
                it.baleStatus = baleStatus.status
                it.dateTime = Date()
            }
            repo.updateMtnBale(it)
        }

    }

    fun getMtnsWithBales() = repo.getMtnsWithBales()

    fun getOfflineMtnWithBales() = repo.getOfflineMtnWithBales()

    suspend fun getMtnWithBales(mtnId: String) = repo.getMtnWithBales(mtnId)

    suspend fun updateOfflineMtnStatus(mtnId: String,storageId: String) = repo.updateOfflineMtnStatus(mtnId,storageId)

    suspend fun deleteMtnsByMtnId(mtnId: String) = repo.deleteMtnsbyMtnId(mtnId)

    suspend fun getMtnsWithGradesOffline() = repo.getMtnsWithGradesOffline()

    fun updateofflineMtnRevertStatus(mtnNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            repo.updateOfflineMtnRevertStatus(mtnNumber)
        }
    }

    fun getGrades(mtnNumber: String) = repo.getGrades(mtnNumber)

    fun getMtnModel(mtnWithBales: MtnWithBales,storageId:String): PortMtn {
        val mtn = mtnWithBales.mtn
        val bales = mtnWithBales.bales

        bales.forEach {
            it.toSloc = if(getCurrentKey().split("_")[2].contains("COTTPORT")){
                storageId
            }else{
                BaleStatus.Good.id.toString()
            }
            it.baleStatus = BaleStatus.Good.status
        }
        return PortMtn(
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

            getStorageLocationListSource = repo.getStorageLocationList()

        }
        _getStorageLocationList.addSource(getStorageLocationListSource) {
            _getStorageLocationList.value = it
        }
    }
}
