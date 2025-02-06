package com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.dao.GinningInprogressDao
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.GinningInprogress
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.api.GinningInprogreessApi
import java.util.*

/**
 * Created by SangiliPandian C on 16-03-2020.
 */

interface GinningInprogressRepository
{
    suspend fun fetchLotDetails():LiveData<Resource<GenericReqAndResp<GinningInprogress>>>
    suspend fun validateBale(baleId: String): LiveData<Resource<GenericReqAndResp<GenericMessage>>>
    suspend fun saveGinning(inprogress: GinningInprogress):LiveData<Resource<GenericReqAndResp<GenericMessage>>>
    suspend fun insertBale(bale: Bale)
    suspend fun deleteGinningBale(bale: Bale)
    suspend fun deleteBalesByLotId(lotId: String)
    suspend fun getBalesByLotNumber(lotId: String):LiveData<List<Bale>>
}

class VegaGinningInprogressRepositoryImpl(private val api: GinningInprogreessApi, private val dao: GinningInprogressDao) :
    GinningInprogressRepository {
    //val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    // val warehouseId = PreferenceHelper.get(Constants.WAREHOUSE_ID, 0)
    val currentDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")

    override suspend fun fetchLotDetails(): LiveData<Resource<GenericReqAndResp<GinningInprogress>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GinningInprogress>>() {
            override suspend fun createCall() = api.getLotDetails(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun validateBale(baleId: String): LiveData<Resource<GenericReqAndResp<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
            override suspend fun createCall() = api.validateBale(getCurrentKey(), baleId)
        }.build().asLiveData()
    }

    override suspend fun  saveGinning(inprogress: GinningInprogress):LiveData<Resource<GenericReqAndResp<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
            override suspend fun createCall() = api.saveGinning(getCurrentKey(), inprogress)
        }.build().asLiveData()
    }
    override suspend fun insertBale(bale: Bale) = dao.save(bale)
    override suspend fun deleteGinningBale(bale: Bale) = dao.remove(bale)
    override suspend fun getBalesByLotNumber(lotId: String) = dao.getBalesByLotNumber(lotId)
   override suspend fun deleteBalesByLotId(lotId: String) = dao.deleteBalesByLotId(lotId)
}

/*(
    private val api: GinningApi,
    private val appExecutors: AppExecutors,
    private val mDao: GinningDao
) {

    fun fetchLotDetails(): LiveData<Resource<GenericResponse<GinningInprogress>>> {
        return object : NetworkOnlyBoundResource<GenericResponse<GinningInprogress>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<GenericResponse<GinningInprogress>>> {
                return api.getLotDetails(getWHId())
            }

        }.asLiveData()
    }

    fun validateBale(baleId: String): LiveData<Resource<GenericResponse<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericResponse<GenericMessage>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<GenericResponse<GenericMessage>>> {
                return api.validateBale(getWHId(), baleId)
            }

        }.asLiveData()
    }

    fun saveGinning(ginningInprogress: GinningInprogress): LiveData<Resource<GenericResponse<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericResponse<GenericMessage>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<GenericResponse<GenericMessage>>> {
                return api.saveGinning(ginningInprogress)
            }

        }.asLiveData()
    }

    suspend fun insertBale(bale: Bale) = mDao.save(bale)

    suspend fun deleteGinningBale(bale: Bale) = mDao.remove(bale)

    suspend fun getBalesByLotNumber(lotId: String) = mDao.getBalesByLotNumber(lotId)

    suspend fun deleteBalesByLotId(lotId: String) = mDao.deleteBalesByLotId(lotId)
}*/
