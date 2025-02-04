package com.olam.warehouse.presentation.data.remote

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.utils.Constants.USER_NAME
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

/**
 * Created by SangiliPandian C on 17-11-2019.
 */
abstract class NetworkOnlyBoundResource<ResultType> {

    private val result = MediatorLiveData<Resource<ResultType>>()
    private val supervisorJob = SupervisorJob()

    suspend fun build(): NetworkOnlyBoundResource<ResultType> {
        withContext(Dispatchers.Main) {
            result.value =
                Resource.loading(null)
        }
        CoroutineScope(coroutineContext).launch(supervisorJob) {
            try {
                fetchFromNetwork()
            } catch (e: Exception) {
                Log.e("NetworkOnlyBoundResourc", "An error happened: $e")
                setValue(Resource.error(e.message, null))
            }
        }
        return this
    }

    private suspend fun fetchFromNetwork() {
        Log.d(NetworkOnlyBoundResource::class.java.name, "Fetch data from network")
        val apiResponse = createCall()
        Log.e(NetworkOnlyBoundResource::class.java.name, "Data fetched from network")
        val data = setData(apiResponse)
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                when {
                    PreferenceHelper.get(USER_NAME, "").isNotEmpty() -> {
                        try {
                            val responseData = apiResponse as GenericReqAndResp<*>
                            if (responseData.data != null) {
                                result.addSource(data) { newData ->
                                    setValue(Resource.success(newData))
                                }
                            } else {
                                result.addSource(data) { newData ->
                                    val errorMsg =
                                        if (responseData.message.isNullOrEmpty()) responseData.errors else responseData.message
                                    setValue(Resource.error(errorMsg, newData))
                                }
                            }
                        }
                        catch(e:ClassCastException){
                            result.addSource(data) { newData ->
                                setValue(Resource.success(newData))
                            }
                        }
                    }
                    else -> {
                        result.addSource(data) { newData ->
                            setValue(Resource.success(newData))
                        }
                    }
                }
            }
        }
    }

    @MainThread
    private fun setData(response: ResultType): MutableLiveData<ResultType> {
        val data = MutableLiveData<ResultType>()
        GlobalScope.launch {
            withContext(Dispatchers.Main) { data.value = response }
        }
        return data
    }

    fun asLiveData() = result as LiveData<Resource<ResultType>>

    @MainThread
    private fun setValue(newValue: Resource<ResultType>) {
        Log.d(NetworkOnlyBoundResource::class.java.name, "Resource: $newValue")
        if (result.value != newValue) result.postValue(newValue)

    }

    @MainThread
    protected abstract suspend fun createCall(): ResultType
}
