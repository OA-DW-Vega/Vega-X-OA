package com.olam.warehouse.vegax.portwarehouse.data.repository

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.dao.PortReceivingDao
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.portwarehouse.data.api.IncomingApi
import com.olam.warehouse.vegax.portwarehouse.data.model.Deliverylist
import java.util.*

interface PortIncomingRepository {
    //suspend fun getMtns(): LiveData<Resource<GenericReqAndResp<List<PortMtn>>>>
    suspend fun getMtnsOffline(): List<PortMtn>
    suspend fun insertMtn(mtn: PortMtn)
    suspend fun updateMtn(mtn: PortMtn)
    suspend fun getMtnsWithGradesOffline(): List<MtnWithGrades>
    suspend fun updateOfflineMtnStatus(mtnId: String,storageId:String)
    suspend fun getMtnWithBales(mtnId: String): MtnWithBales
    suspend fun postMtnWithBales(mtn: PortMtn): LiveData<Resource<GenericReqAndResp<GenericMessage>>>
    suspend fun deleteMtnsByMtnId(mtnId: String)
    fun getOfflineMtnWithBales(): List<MtnWithBales>
    fun getMtnsWithBales(): List<MtnWithGrades>
    suspend fun getIncomingMtnList(baleList: Deliverylist): LiveData<Resource<GenericReqAndResp<List<PortMtn>>>>
    fun updateMtnBale(mtnBale: PortMtnBales)
    fun getListMtnWithBales(): LiveData<List<MtnWithGrades>>
    suspend fun deleteMtnsbyMtnId(mtnId: String)
    suspend fun getVerifiedBales(mtnNumber: String): LiveData<List<PortMtnBales>>
    suspend fun getMtnInClassification(mtnNumber: String): LiveData<Resource<GenericReqAndResp<GenericMessage>>>
    fun updateMtnToDeleteStatus(mtnNumber: String)
    fun getBale(mtnNumber: String, baleId: String, lineItem: String): PortMtnBales?
    fun updateOfflineVerifiedMtnStatus(it: String, verifiedBales: List<PortMtnBales>?)
    suspend fun updateOfflineMtnRevertStatus(mtnId: String)
    fun getGrades(mtnNumber: String): List<PortMtnGrades>

    suspend fun getStorageLocationList(): LiveData<Resource<GenericReqAndResp<List<PortPileStorageLocationModel>>>>

}

class PortIncomingRepositoryImpl(private val api: IncomingApi, private val dao: PortReceivingDao) :
    PortIncomingRepository {
    //val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    //    val warehouseId = PreferenceHelper.get(Constants.WAREHOUSE_ID, 0)
    val currentDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")

    /* override suspend fun getMtns(): LiveData<Resource<GenericReqAndResp<List<PortMtn>>>> {
         return object : NetworkOnlyBoundResource<GenericReqAndResp<List<PortMtn>>>() {
             override suspend fun createCall() = api.getMtnList()
         }.build().asLiveData()
     }*/

    override suspend fun updateMtn(mtn: PortMtn) = dao.updateMtn(mtn)
    override suspend fun getMtnsWithGradesOffline() = dao.getMtns()
    override suspend fun updateOfflineMtnStatus(mtnId: String,storageId: String) {
        dao.updateMtnStatus(mtnId)
        dao.updateMtnBalesStatus(mtnId,storageId)
    }

    override fun getOfflineMtnWithBales() = dao.getOfflineMtnWithBales()

    override suspend fun insertMtn(mtn: PortMtn) {

        mtn.baleCount = mtn.mtnBales?.size.toString()
        dao.insertOrReplaceMtn(mtn)
        mtn.mtnBales?.let { bales ->
            bales.forEach { mtnBale ->
                mtnBale.unitOfMeasurement = mtn.uom
                dao.insertOrReplaceMtnBale(mtnBale)
            }
        }
        mtn.grades?.let { grades ->
            grades.forEach { grade ->
                grade.mtnNumber = mtn.mtnNumber
                dao.insertOrReplaceMtnGrade(grade)
            }

        }

    }

    override suspend fun postMtnWithBales(mtn: PortMtn): LiveData<Resource<GenericReqAndResp<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
            override suspend fun createCall() = api.postMtnWithBales(getCurrentKey(), mtn)
        }.build().asLiveData()
    }

    override suspend fun deleteMtnsByMtnId(mtnId: String) = dao.deleteMtnsByMtnId(mtnId)
    override suspend fun getMtnWithBales(mtnId: String) = dao.getMtnWithBales(mtnId)
    override suspend fun getMtnsOffline() = dao.getMtnsAsList()
    override fun getMtnsWithBales() = dao.getAllMtnWithBales()
    override suspend fun getIncomingMtnList(baleList: Deliverylist): LiveData<Resource<GenericReqAndResp<List<PortMtn>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<PortMtn>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<PortMtn>> =
                api.getMtnList(getCurrentKey(), baleList)
        }.build().asLiveData()
    }

    override fun updateMtnBale(mtnBale: PortMtnBales) = dao.updateMtnBale(mtnBale)
    override fun getListMtnWithBales(): LiveData<List<MtnWithGrades>> = dao.getMtnss()
    override suspend fun deleteMtnsbyMtnId(mtnId: String) = dao.deleteMtnsByMtnId(mtnId)
    override suspend fun getVerifiedBales(mtnNumber: String): LiveData<List<PortMtnBales>> =
        dao.getVerifiedBales(mtnNumber)

    override suspend fun getMtnInClassification(mtnNumber: String): LiveData<Resource<GenericReqAndResp<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
            override suspend fun createCall() =
                api.getMtnInClassification(getCurrentKey(), mtnNumber)
        }.build().asLiveData()
    }

    override fun updateMtnToDeleteStatus(mtnNumber: String) = dao.updateMtnRevertStatus(mtnNumber)
    override fun getBale(mtnNumber: String, baleId: String, lineItem: String) = dao.getBale(mtnNumber, baleId)
    override fun updateOfflineVerifiedMtnStatus(mtnId: String, verifiedBales: List<PortMtnBales>?) {
        dao.updateMtnStatus(mtnId)
        verifiedBales?.forEach { dao.updateVerifiedMtnBalesStatus(it.baleId) }
    }

    override suspend fun updateOfflineMtnRevertStatus(mtnId: String) {
        dao.updateMtnRevertStatus(mtnId)
        dao.updateMtnBalesRevertStatus(mtnId)
    }

    override fun getGrades(mtnNumber: String) = dao.getGrades(mtnNumber)
    override suspend fun getStorageLocationList(
    ): LiveData<Resource<GenericReqAndResp<List<PortPileStorageLocationModel>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<PortPileStorageLocationModel>>>() {
            override suspend fun createCall() = api.getStorageLocationList(getCurrentKey())
        }.build().asLiveData()
    }
}
