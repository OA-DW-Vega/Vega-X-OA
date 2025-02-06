package com.olam.warehouse.master.common.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.data.domain.model.ODMasterUseCase
import com.olam.warehouse.master.common.model.TransCountModel
import com.olam.warehouse.master.common.model.TransactionMaster
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaGrnDao
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.DateUtils.getTransLastSyncTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.HttpException
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
                    if (selectedKeys.size > 1 || selectedKeys.any { it.split("_")[0].contains("VEGA") }) {
                        data?.data?.let { useCase.saveTrnasMasterData(it) }
                        try {
                            val transList1 = arrayListOf<TransCountModel>()
                            data?.data?.let { transList->
                                var tranModel = TransCountModel()
                                tranModel.poCount = transList.get(0).masterDTO.purchaseOrderList?.size
                                tranModel.invoiceCount = transList.get(0).masterDTO.invoiceDetails?.size
                                tranModel.grnReprintCount =
                                    transList.get(0).masterDTO.receiptReprintDto?.size
                                tranModel.grnPriceCount =
                                    transList.get(0).masterDTO.grnPriceDetails?.size
                                tranModel.charDetailsCount =
                                    transList.get(0).masterDTO.grnCharDetails?.size
                                tranModel.advanceCount =
                                    transList.get(0).masterDTO.advanceLineItems?.size
                                tranModel.vendorCreditCount =
                                    transList.get(0).masterDTO.advanceDetails?.size
                                tranModel.stocksCount =
                                    transList.get(0).masterDTO.stockDetails?.size
                                transList1.add(tranModel)
                            }
                            PreferenceHelper.save(Constants.TRANS_LIST, gson.toJson(transList1))
                        } catch (e:Exception){
                            e.printStackTrace()
                        }
                    }else {
                        data?.data?.let {
                            useCaseOd.saveTrnasMasterData(it)
                        }
                    }
                    //Result.success(workDataOf(TRANS_OUTPUT_DATA to gson.toJson(transList)))
                    //val dataCount = dao.getAdvanceItemCount()
                    Result.success(/*workDataOf(TRANS_OUTPUT_DATA to dataCount.toString())*/)
                }
            } else if (response.body() == null) {
                try {
                    val error = response.errorBody()?.source()?.readUtf8()
                    val gson = Gson()
                    val errorMsg = gson.fromJson(error, GenericMessage::class.java)
                    Result.failure(workDataOf(TRANS_OUTPUT_DATA to errorMsg.message))
                }catch (e1: Exception){
                    Result.failure(workDataOf(TRANS_OUTPUT_DATA to response.message()))
                }
            } else {
                Result.failure(workDataOf(TRANS_OUTPUT_DATA to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(TRANS_OUTPUT_DATA to error.message))
        }
    }
}
