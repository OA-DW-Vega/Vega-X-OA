package com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.dao.VegaCottonGinningDispatchDao
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Grade
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.VegaCottonGinningDispatchDelivery
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.DeliveryWithBales
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.DeliveryWithGrades
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.api.GinningDispatchApi
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.model.DispatchPostResponse
import java.util.*

/**
 * Created by Baskaran Kannan on 3/20/2020.
 */

interface GinningDispatchRepository {
    suspend fun fetchDeliveryDetails(): LiveData<Resource<GenericReqAndResp<List<VegaCottonGinningDispatchDelivery>>>>
    suspend fun fetchDeliveryDetailsOffline(): LiveData<List<VegaCottonGinningDispatchDelivery>>
    suspend fun fetchExistedDelivery(): LiveData<List<VegaCottonGinningDispatchDelivery>>
    suspend fun insertOrReplaceDelivery(ot:VegaCottonGinningDispatchDelivery)
    suspend fun insertOrReplaceGrade(grade: Grade)
    suspend fun getDeliveryWithGrades(deliverNo: String) :LiveData<DeliveryWithGrades>
    suspend fun getDeliveryWithBales(deliverNo: String) :LiveData<DeliveryWithBales>
    suspend fun getAllDeliveryWithBales():LiveData<List<DeliveryWithBales>>
    suspend fun getBaleDetailsByBaleId(baleId: String): LiveData<Bale>
    suspend fun getBaleDetailsByBaleIdOffline(baleId: String): Bale
    suspend fun postDispatch(dto: VegaCottonGinningDispatchDelivery): LiveData<Resource<GenericReqAndResp<DispatchPostResponse>>>
    suspend fun validateBale(baleId: String, deliveryNumber: String):LiveData<Resource<GenericReqAndResp<Bale>>>
    suspend fun  saveBale(bale: Bale)
    suspend fun deleteBale(deliverNo: String)
    suspend fun updateDeliverySaveOffline(deliveryNo: String)
    suspend fun fetchOfflineDeliveryDetails(): LiveData<List<VegaCottonGinningDispatchDelivery>>
    suspend fun updateDeliveryStatusToEdit(deliveryNo: String)
    suspend fun deleteOfflineDeliveryWithBales(deliveryNumber: String)
    suspend fun updateErrorMessage(msg: String, deliveryNumber: String)
    suspend fun fetchBaleDetails(): LiveData<List<Bale>>
    //suspend fun   saveBaleDetails(bale: Bale)
    /* val warehouseId = PreferenceHelper.get(Constants.WAREHOUSE_ID, 0)
     fun fetchDeliveryDetails(): LiveData<Resource<List<GinningDispatchDelivery>>> {
         return object :
             NetworkBoundResource<List<GinningDispatchDelivery>, GenericResponse<List<GinningDispatchDelivery>>>(
                 appExecutors
             ) {
             override fun saveCallResult(item: GenericResponse<List<GinningDispatchDelivery>>) {
                 item.data?.let {
                     it.forEach { items ->
                         items.gradeDTO.forEach { grade ->
                             grade.deliveryNumber = items.deliveryNumber
                             dao.insertOrReplaceGrade(grade)
                         }
                         if (dao.isDeliveryExist(items.deliveryNumber).isNotEmpty()) return@forEach
                         items.userName = PreferenceHelper.get(Constants.USER_NAME, "")
                         dao.insertOrReplaceDelivery(items)
                     }
                     //dao.insertOrReplaceDeliverys(it)
                 }
             }

             override fun shouldFetch(data: List<GinningDispatchDelivery>?) = true

             override fun loadFromDb() = dao.getDeliverys()

             override fun createCall() =
                 api.fetchDeliveryDetails(PreferenceHelper.get(Constants.WAREHOUSE_ID, 0))
         }.asLiveData()
     }

     fun getDeliveryWithGrades(deliverNo: String) = dao.getDeliveryWithGrades(deliverNo)
     fun getDeliveryWithBales(deliverNo: String) = dao.getDeliveryWithBales(deliverNo)

     fun validateBale(
         baleId: String,
         deliveryNumber: String
     ): LiveData<Resource<GenericResponse<Bale>>> {
         return object : NetworkOnlyBoundResource<GenericResponse<Bale>>(appExecutors) {
             override fun createCall() = api.validateBale(
                 PreferenceHelper.get(Constants.WAREHOUSE_ID, 0),
                 baleId,
                 deliveryNumber
             )
         }.asLiveData()
     }

     fun saveBaleDetails(bale: Bale) = dao.saveBaleDetails(bale)


     fun postDispatch(deliveryDto: GinningDispatchDelivery): LiveData<Resource<GenericResponse<DispatchPostResponse>>> {
         return object :
             NetworkOnlyBoundResource<GenericResponse<DispatchPostResponse>>(appExecutors) {
             override fun createCall() =
                 api.postDispatch(PreferenceHelper.get(Constants.WAREHOUSE_ID, 0), deliveryDto)
         }.asLiveData()
     }

     fun deleteBales(deliveryNo: String) {
         dao.deleteBalesFromDB(deliveryNo)
         dao.deleteDelivery(deliveryNo)
     }

     fun fetchOfflineDeliveryDetails() = dao.fetchOfflineDeliveryDetails()

     fun fetchOfflineDeliveryList() = dao.getDeliverysInOffine()

     fun updateDeliveryStatusToEdit(deliveryNo: String) = dao.updateDeliveryStatusToEdit(deliveryNo)

     fun updateDeliverySaveOffline(deliveryNo: String) = dao.updateDeliverySaveOffline(deliveryNo)

     fun deleteOfflineDeliveryWithBales(deliveryNumber: String) {
         dao.updateDeliveryStatusToEdit(deliveryNumber)
         dao.deleteBales(deliveryNumber)
     }

     fun updateErrorMessage(msg: String, deliveryNumber: String) =
         dao.updateErrorMessage(msg, deliveryNumber)

     fun getBaleDetails(baleId: String) = dao.getBaleDetails(baleId)*/

}

class VegaGinningDispatchRepositoryImpl(
    private val api: GinningDispatchApi,
    private val daoVegaCotton: VegaCottonGinningDispatchDao
) :
    GinningDispatchRepository {
    //val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    //val warehouseId = PreferenceHelper.get(Constants.WAREHOUSE_ID, 0)
    val currentDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")

    override suspend fun fetchDeliveryDetails(): LiveData<Resource<GenericReqAndResp<List<VegaCottonGinningDispatchDelivery>>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCottonGinningDispatchDelivery>>>() {
            override suspend fun createCall() = api.fetchDeliveryDetails(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun fetchDeliveryDetailsOffline(): LiveData<List<VegaCottonGinningDispatchDelivery>> =
        daoVegaCotton.fetchOfflineDeliveryDetails()

    override suspend fun insertOrReplaceDelivery(ot: VegaCottonGinningDispatchDelivery) {
        daoVegaCotton.insertOrReplaceDelivery(ot)
    }

    override suspend fun saveBale(bale: Bale) = daoVegaCotton.saveBaleDetails(bale)

    override suspend fun insertOrReplaceGrade(grade: Grade) =
        daoVegaCotton.insertOrReplaceGrade(grade)

    override suspend fun getDeliveryWithGrades(deliverNo: String): LiveData<DeliveryWithGrades> =
        daoVegaCotton.getDeliveryWithGrades(deliverNo)

    override suspend fun getDeliveryWithBales(deliverNo: String): LiveData<DeliveryWithBales> =
        daoVegaCotton.getDeliveryWithBales(deliverNo)

    override suspend fun fetchExistedDelivery(): LiveData<List<VegaCottonGinningDispatchDelivery>> =
        daoVegaCotton.fetchExistedDeliveries()

    override suspend fun getBaleDetailsByBaleId(baleId: String): LiveData<Bale> =
        daoVegaCotton.getBaleDetails(baleId)

    override suspend fun validateBale(
        baleId: String,
        deliverNo: String
    ): LiveData<Resource<GenericReqAndResp<Bale>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<Bale>>() {
            override suspend fun createCall() = api.validateBale(getCurrentKey(), baleId, deliverNo)
        }.build().asLiveData()
    }

    override suspend fun getBaleDetailsByBaleIdOffline(baleId: String): Bale =
        daoVegaCotton.getBaleDetailsOffline(baleId)

    override suspend fun postDispatch(dto: VegaCottonGinningDispatchDelivery): LiveData<Resource<GenericReqAndResp<DispatchPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<DispatchPostResponse>>() {
            override suspend fun createCall() = api.postDispatch(getCurrentKey(), dto)
        }.build().asLiveData()
    }

    override suspend fun deleteBale(deliveryNo: String) {
        daoVegaCotton.deleteDelivery(deliveryNo)
        daoVegaCotton.deleteBalesFromDB(deliveryNo)

    }
    override  suspend fun updateDeliverySaveOffline(deliveryNo: String)
    {
        daoVegaCotton.updateDeliverySaveOffline(deliveryNo)
    }

    override suspend fun getAllDeliveryWithBales(): LiveData<List<DeliveryWithBales>> = daoVegaCotton.getAllDeliveryWithBales()

    override suspend fun fetchOfflineDeliveryDetails(): LiveData<List<VegaCottonGinningDispatchDelivery>> = daoVegaCotton.getDeliverysInOffine()
    override suspend fun updateDeliveryStatusToEdit(deliveryNo: String) = daoVegaCotton.updateDeliveryStatusToEdit(deliveryNo)
    override suspend fun deleteOfflineDeliveryWithBales(deliveryNumber: String) {
        daoVegaCotton.updateDeliveryStatusToEdit(deliveryNumber)
        daoVegaCotton.deleteBales(deliveryNumber)
    }

    override suspend fun updateErrorMessage(msg: String, deliveryNumber: String) =
            daoVegaCotton.updateErrorMessage(msg, deliveryNumber)

    override suspend fun fetchBaleDetails() = daoVegaCotton.fetchBaleDetails()
}
