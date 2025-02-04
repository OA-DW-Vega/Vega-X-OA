package com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.dao.ReceivingDao
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.api.IncomingMtnApi
import java.util.*

interface IncomingMtnRepository
{
    suspend fun   updateMtnBale(mtnBale: MtnBales)
  //  suspend fun setMtnWithBales(mtnNumber: String)
    suspend fun getMtns(): LiveData<Resource<GenericReqAndResp<List<Mtn>>>>
    suspend fun getMtnsOffline(): LiveData<List<Mtn>>
    suspend fun getMtnsWithGradesOffline():LiveData<List<MtnWithGrades>>
    suspend fun insertMtn(mtn: Mtn)
    suspend fun deleteMtnsByMtnId(mtnId: String)
    suspend fun updateMtn(mtn: Mtn)
    suspend   fun updateMtnBalesStatusVerified(mtnId: String)
     suspend fun getMtnWithBales(mtnId: String):LiveData<MtnWithBales>
    suspend fun getGrades(mtnNumber: String): LiveData<List<MtnGrades>>
    suspend fun getOfflineMtnWithBales(): LiveData<List<MtnWithBales>>
    suspend  fun updateOfflineMtnSReverttatus(mtnId: String)
    suspend fun getVerifiedBales(mtnNumber: String): LiveData<List<MtnBales>>
    suspend fun getBale(mtnNumber: String, baleId: String, lineItem: String):LiveData<MtnBales>
    suspend fun updateMtnToDeleteStatus(mtnNumber: String)
    suspend fun postMtnWithBales(mtn: Mtn):LiveData<Resource<GenericReqAndResp<GenericMessage>>>
}
/*constructor(
    private val api: IncomingApi,
    private val appExecutors: AppExecutors,
    private val model: ReceivingModel
) {
    fun getIncomingMtnList(): LiveData<Resource<List<MtnWithGrades>>> {
        return object :
            NetworkBoundResource<List<MtnWithGrades>, GenericResponse<List<Mtn>>>(appExecutors) {
            override fun saveCallResult(item: GenericResponse<List<Mtn>>) {

                val mtns = model.getMtnsAsList()

                if (mtns.isEmpty()) {
                    item.data?.let {
                        it.forEach { mtn ->
                            insertMtn(mtn)
                        }
                    }
                } else {
                    mtns.forEach { mtn ->
                        val mtnExistInServer = item.data?.any { it.mtnNumber == mtn.mtnNumber }

                        mtnExistInServer?.let {
                            if (!it) {
                                model.deleteMtnsByMtnId(mtn.mtnNumber)
                            }
                        }
                    }
                    item.data?.forEach { serverMtn ->
                        val isMtnExist = mtns.any { it.mtnNumber == serverMtn.mtnNumber }
                        if (!isMtnExist) {
                            insertMtn(serverMtn)
                        }
                    }
                }


            }

            override fun shouldFetch(data: List<MtnWithGrades>?) = true

            override fun loadFromDb() = model.getMtns()

            override fun createCall() = api.getMtnList()

        }.asLiveData()
    }

    fun postMtnWithBales(mtn: Mtn): LiveData<Resource<GenericResponse<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericResponse<GenericMessage>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<GenericResponse<GenericMessage>>> {
                return api.postMtnWithBales(mtn)
            }

        }.asLiveData()
    }

    fun getVerifiedBales(mtnNumber: String) = model.getVerifiedBales(mtnNumber)

    fun getBale(mtnNumber: String, baleId: String, lineItem: String) = model.getBale(mtnNumber, baleId, lineItem)

    fun updateMtnBale(mtnBale: MtnBales) = model.updateMtnBales(mtnBale)

    fun updateMtn(mtn: Mtn) = model.updateMtn(mtn)

    fun getBaleByBaleId(baleId: String) = model.getBaleByBaleId(baleId)

    fun getMtnWithBales(mtnId: String) = model.getMtnWithBales(mtnId)

    fun getOfflineMtnWithBales() = model.getOfflineMtnWithBales()

    fun getMtnsWithBales() = model.getMtnsWithBales()

    fun updateOfflineMtnStatus(mtnId: String) = model.updateOfflineMtnStatus(mtnId)

    fun updateMtnBalesStatusVerified(mtnId: String) = model.updateMtnBalesStatusVerified(mtnId)

    fun updateOfflineVerifiedMtnStatus(
        mtnId: String,
        verifiedBales: List<MtnBales>?
    ) = model.updateOfflineVerifiedMtnStatus(mtnId, verifiedBales)

    fun updateOfflineMtnRevertStatus(mtnId: String) = model.updateOfflineMtnSReverttatus(mtnId)

    fun deleteAllMtns() = model.deleteAllMtns()

    fun deleteAllMtnBales() = model.deleteAllMtnBales()

    fun deleteMtnsbyMtnId(mtnId: String) = model.deleteMtnsByMtnId(mtnId)

    private fun insertMtn(mtn: Mtn) {

        mtn.baleCount = mtn.mtnBales?.size.toString()
        model.insertOrReplaceMtn(mtn)
        mtn.mtnBales?.let { bales ->
            bales.forEach { mtnBale ->
                mtnBale.unitOfMeasurement = mtn.uom
                model.insertOrReplaceMtnBale(mtnBale)
            }
        }
        mtn.grades?.let { grades ->
            grades.forEach { grade ->
                grade.mtnNumber = mtn.mtnNumber
                model.insertOrReplaceMtnGrade(grade)
            }

        }

    }

    fun updateMtnToDeleteStatus(mtnNumber: String) = model.updateMtnToDeleteStatus(mtnNumber)

    fun getGrades(mtnNumber: String) = model.getGrades(mtnNumber)
}*/
class VegaGinningIncomingMtnRepositoryImpl(private val api: IncomingMtnApi, private val dao: ReceivingDao) :
    IncomingMtnRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
//    val warehouseId = PreferenceHelper.get(Constants.WAREHOUSE_ID, 0)
    val currentDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")

    override suspend fun updateMtnBale(mtnBale: MtnBales)=dao.updateMtnBale(mtnBale)

    /* override suspend fun fetchDeliveryDetails(): LiveData<Resource<GenericReqAndResp<List<VegaCottonGinningDispatchDelivery>>>> {
         return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCottonGinningDispatchDelivery>>>() {
             override suspend fun createCall() = api.fetchDeliveryDetails(warehouseId)
         }.build().asLiveData()
     }

     override suspend fun fetchDeliveryDetailsOffline(): LiveData<List<VegaCottonGinningDispatchDelivery>> =daoVegaCotton.fetchOfflineDeliveryDetails()
 */
    override suspend fun getMtns(): LiveData<Resource<GenericReqAndResp<List<Mtn>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<Mtn>>>() {
            override suspend fun createCall() = api.getMtnList(getCurrentKey())
        }.build().asLiveData()
    }
    override suspend fun postMtnWithBales(mtn: Mtn): LiveData<Resource<GenericReqAndResp<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
            override suspend fun createCall() = api.postMtnWithBales(getCurrentKey(), mtn)
        }.build().asLiveData()
    }
    override suspend fun getMtnsOffline()=dao.getMtnsAsList()
    override suspend fun getMtnsWithGradesOffline()=dao.getMtns()
    override suspend fun getMtnWithBales(mtnId: String) = dao.getMtnWithBales(mtnId)
    override suspend fun insertMtn(mtn: Mtn) {

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
    override suspend fun getOfflineMtnWithBales()=dao.getOfflineMtnWithBales()
    override suspend fun getGrades(mtnNumber: String)=dao.getGrades(mtnNumber)
    override  suspend fun deleteMtnsByMtnId(mtnId: String) = dao.deleteMtnsByMtnId(mtnId)
    override suspend fun updateMtn(mtn: Mtn) = dao.updateMtn(mtn)
    override suspend   fun updateMtnBalesStatusVerified(mtnId: String) = dao.updateMtnBalesStatusVerified(mtnId)
    override suspend  fun updateOfflineMtnSReverttatus(mtnId: String) {
        dao.updateMtnRevertStatus(mtnId)
        dao.updateMtnBalesRevertStatus(mtnId)
    }

    override suspend fun getVerifiedBales(mtnNumber: String) = dao.getVerifiedBales(mtnNumber)
    override suspend fun getBale(mtnNumber: String, baleId: String, lineItem: String) = dao.getBale(mtnNumber, baleId)
    override suspend fun updateMtnToDeleteStatus(mtnNumber: String) = dao.updateMtnRevertStatus(mtnNumber)
    }

