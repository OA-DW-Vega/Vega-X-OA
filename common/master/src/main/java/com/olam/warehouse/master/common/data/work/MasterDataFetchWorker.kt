package com.olam.warehouse.master.common.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.data.domain.model.ODMasterUseCase
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by SangiliPandian C on 17-11-2019.
 */

class MasterDataFetchWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        //makeStatusNotification("Master data downloading...", applicationContext)
        sleep()
        val selectedKey = PreferenceHelper.get(Constants.SELECTED_KEYS, "")
        val selectedKeys = Gson().fromJson<List<String>>(selectedKey)
        val useCase: MasterUseCase by inject()
        val useCaseOd: ODMasterUseCase by inject()
        val api: MasterApi by inject()
        try {
            val response = api.getMasterData(selectedKeys).execute()
            if (response.isSuccessful) {
                val data = response.body()
                if (selectedKeys.size > 1 || selectedKeys.any { it.split("_")[0].contains("VEGA") })
                    data?.data?.let { useCase.saveMasterData(it) }
                else
                    data?.data?.let { useCaseOd.saveMasterData(it) }
                Result.success()
            } else if (response.body() == null) {
                try {
                    val error = response.errorBody()?.source()?.readUtf8()
                    val gson = Gson()
                    val errorMsg = gson.fromJson(error, GenericMessage::class.java)
                    Result.failure(workDataOf(MASTER_OUTPUT_DATA to errorMsg.message))
                }catch (e1: Exception){
                    Result.failure(workDataOf(MASTER_OUTPUT_DATA to response.message()))
                }
             } else {
                Result.failure(workDataOf(MASTER_OUTPUT_DATA to response.message()))
             }
            //Result.success()
        } catch (error: Throwable) {
            error.printStackTrace()
            Result.failure(workDataOf(MASTER_OUTPUT_DATA to error.message))
        }
    }
}
