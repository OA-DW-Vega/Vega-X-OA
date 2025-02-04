package com.olam.warehouse.vegax.secretidnigeria.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.secretidnigeria.data.domain.model.VegaNigeriaSecretId
import com.olam.warehouse.vegax.secretidnigeria.data.domain.usecase.VegaNigeriaSecretIdUsecase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
class VegaNigeriaSecretIdViewModel(
    private val useCase: VegaNigeriaSecretIdUsecase, private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var secretIdSource: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaSecretId>>>> =
        MutableLiveData()
    private var _secretId =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaNigeriaSecretId>>>>()
    val secretId: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaSecretId>>>> get() = _secretId

    private var wbWeightSource: LiveData<Resource<GenericReqAndResp<VegaQualityWBDetails>>> =
        MutableLiveData()
    private val _wbWeight = MediatorLiveData<Resource<GenericReqAndResp<VegaQualityWBDetails>>>()
    val wbWeight: LiveData<Resource<GenericReqAndResp<VegaQualityWBDetails>>> get() = _wbWeight

    private var wbMultiPlantsesource: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaSecretId>>>> =
        MutableLiveData()
    private val _wbMultiPlant =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaNigeriaSecretId>>>>()
    val wbMultiPlant: LiveData<Resource<GenericReqAndResp<List<VegaNigeriaSecretId>>>> get() = _wbMultiPlant

    fun getSecretIdList(startDate: String, endDate: String, isMtnt: Boolean) =
        viewModelScope.launch(dispatchers.main) {
            _secretId.removeSource(secretIdSource)
            withContext(dispatchers.io) {
                secretIdSource = useCase.getDashboardResult(startDate, endDate, isMtnt)
            }
            _secretId.addSource(secretIdSource) {
                _secretId.value = it
            }
        }

    fun getWbWeightDetails(wbid: String) = viewModelScope.launch(dispatchers.main) {
        _wbWeight.removeSource(wbWeightSource)
        withContext(dispatchers.io) {
            wbWeightSource = useCase.getWbWeightDetails(wbid)
        }
        _wbWeight.addSource(wbWeightSource) {
            _wbWeight.value = it
        }
    }

    fun getfetchWBListforMultiPlants(
        isMTNT: Boolean,
        startDate: String,
        endDate: String,
        plantList: List<String>
    ) = viewModelScope.launch(dispatchers.main) {
        _wbMultiPlant.removeSource(wbMultiPlantsesource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            wbMultiPlantsesource =
                useCase.getfetchWBListforMultiPlants(isMTNT, startDate, endDate, plantList)
        }
        _wbMultiPlant.addSource(wbMultiPlantsesource) {
            _wbMultiPlant.value = it
        }
    }

}
