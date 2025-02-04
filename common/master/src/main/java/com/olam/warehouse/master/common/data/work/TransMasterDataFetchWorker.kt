package com.olam.warehouse.master.common.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.data.domain.model.ODMasterUseCase
import com.olam.warehouse.master.common.model.TransactionMaster
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaGrnDao
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.DateUtils.getTransLastSyncTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
class TransMasterDataFetchWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        //makeStatusNotification("Data downloading...", applicationContext)
        sleep()
        val dao: VegaNicaraguaGrnDao by inject()
        val gson = GsonUtils()
        var lastSyncTime = getTransLastSyncTime()
        if (!getCurrentKey().split("_")[1].contains("NI")) lastSyncTime = 0L
        val selectedKey = PreferenceHelper.get(Constants.SELECTED_KEYS, "")
        val selectedKeys = Gson().fromJson<List<String>>(selectedKey)
        val useCase: MasterUseCase by inject()
        val useCaseOd: ODMasterUseCase by inject()
        val api: MasterApi by inject()
        val currentDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        val keyList = arrayListOf<String>()
        keyList.add(PreferenceHelper.get(Constants.CURRENT_KEY, ""))
        try {
            val response =
                api.getTransMasterData(keyList, currentDate.toString(), lastSyncTime).execute()
            if (response.isSuccessful) {
                val data = response.body()
                if (data?.errors?.isNotEmpty() == true) {
                    Result.failure(workDataOf(TRANS_OUTPUT_DATA to data.errors))
                } else {
                    if (selectedKeys.size > 1 || selectedKeys.any { it.split("_")[0].contains("VEGA") })
                        data?.data?.let { useCase.saveTrnasMasterData(it) }
                    else
                        data?.data?.let { useCaseOd.saveTrnasMasterData(it) }
                    val transList = arrayListOf<TransactionMaster>()
                    data?.data?.let {
                        transList.addAll(it)
                    }
                    PreferenceHelper.save(Constants.TRANS_LIST, gson.toJson(transList))
                    val dataCount = dao.getAdvanceItemCount()
                    Result.success(workDataOf(TRANS_OUTPUT_DATA to dataCount.toString()))
                }
            } else if (response.body() == null) {
                Result.failure(workDataOf(TRANS_OUTPUT_DATA to response.raw().message))
            } else {
                Result.retry()
            }

        } catch (error: Throwable) {
            Result.retry()
        }
    }
}
