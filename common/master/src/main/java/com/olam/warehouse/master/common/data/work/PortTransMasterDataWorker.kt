package com.olam.warehouse.master.common.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.olam.warehouse.master.common.data.api.PortTransMasterApi
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.dao.PortReceivingDao
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.presentation.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PortTransMasterDataWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        makeStatusNotification("Data downloading...", applicationContext)
        sleep()
        val api: PortTransMasterApi by inject()
        val model: PortReceivingDao by inject()
        try {
            val response = api.getMtnListOffline(getCurrentKey()).execute()
            if (response.isSuccessful) {
                val data = response.body()
                data?.let {
                    /*val mtns = model.getMtnsAsListOffline()

                    if (mtns.isEmpty()) {
                        it.data?.let {
                            it.forEach { mtn ->
                                insertMtn(mtn, model)
                            }
                        }
                    } else {
                        mtns.forEach { mtn ->
                            val mtnExistInServer = it.data?.any { it.mtnNumber == mtn.mtnNumber }

                            mtnExistInServer?.let {
                                if (!it) {
                                    model.deleteMtnsByMtnId(mtn.mtnNumber)
                                }
                            }
                        }
                        it.data?.forEach { serverMtn ->
                            val isMtnExist = mtns.any { it.mtnNumber == serverMtn.mtnNumber }
                            if (!isMtnExist) {
                                insertMtn(serverMtn, model)
                            }
                        }
                    }*/
                    if (data.data != null) {
                        data.data.let { mtnData ->
                            val gson = GsonUtils()
                            PreferenceHelper.save(TRANS_OUTPUT_DATA, gson.toJson(mtnData))
                        }
                        Result.success()
                    }
                }
                Result.success()
            } else {
                Result.retry()
            }
        } catch (error: Throwable) {
            Result.retry()
        }
    }

    private fun insertMtn(
        mtn: PortMtn,
        model: PortReceivingDao
    ) {

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
}
