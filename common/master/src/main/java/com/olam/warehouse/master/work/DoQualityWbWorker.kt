package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.prepareVegaVendor
import com.olam.warehouse.master.dorigin.dao.DOQualityDao
import com.olam.warehouse.master.user.model.Key
import com.olam.warehouse.master.vega.dao.VegaReceivingDao
import com.olam.warehouse.presentation.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.Exception

class DoQualityWbWorker(context: Context, params: WorkerParameters)
    : CoroutineWorker(context,params),KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
            sleep()
            val api: MasterApi by inject()
            val dao: DOQualityDao by inject()
            var count = ""

            val errorMsg = "Quality Count Not Available."

        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
            val response = api.fetchDOWeighBridgeDetailOnWorker(currentKey).execute()

        try {
            if (response.isSuccessful) {
                val data = response.body()
                val dbWbList = data?.data?.toList() ?: emptyList()

                if (dbWbList.isNotEmpty()) {
                    dao.save(dbWbList)
                    dbWbList.let { it1 ->
                        val doWbIds =
                            it1.filter { wb ->
                                !wb.challan.isNullOrEmpty() && !wb.qcStatus!!.contains(
                                    "X"
                                )
                            }
                        doWbIds.distinctBy { Pair(it.weighBridgeId, it.weighBridgeId) }
                        count = doWbIds.size.toString()
                    }
                }



                Result.success(workDataOf(DO_WB_QUALITY_OUTPUT_DATA to count))
            } else if (response.body() == null) {
                Result.failure(workDataOf(DO_WB_QUALITY_OUTPUT_DATA to errorMsg))
            } else {
                Result.failure(workDataOf(DO_WB_QUALITY_OUTPUT_DATA to errorMsg))
            }
        }catch (e: Throwable){
            Result.failure(workDataOf(DO_WB_QUALITY_OUTPUT_DATA to errorMsg))

        }




    }
}
