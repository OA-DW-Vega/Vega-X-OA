package com.olam.warehouse.vegax.secretidcommon.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.secretidcommon.data.domain.model.VegaSecretIdResponse
import com.olam.warehouse.vegax.secretidcommon.data.domain.usecase.VegaCommonSecretIdUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class VegaCommonSecretIdViewModel(private val useCase: VegaCommonSecretIdUseCase,
                                  private val dispatchers: AppDispatchers
):BaseViewModel() {


    private var stockPlantListSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> = MutableLiveData()
    private val _stockPlantList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val stockPlantList: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _stockPlantList

    private var weighBridgeOnlineSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> =
        MutableLiveData()
    private val _weighBridgeOnline = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>()
    val weighBridgeOnline: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> get() = _weighBridgeOnline

    private var lotListSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> =
        MutableLiveData()
    private val _lotList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val lotList: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _lotList

    private var secretIdSource: LiveData<Resource<GenericReqAndResp<List<VegaSecretIdResponse>>>> =
        MutableLiveData()
    private val _secretId = MediatorLiveData<Resource<GenericReqAndResp<List<VegaSecretIdResponse>>>>()
    val secretIdLot: LiveData<Resource<GenericReqAndResp<List<VegaSecretIdResponse>>>> get() = _secretId

    fun getSecretId(batchNo: String,
                    isSecretIdExists: Boolean,
                    materialCode: String,
                    plantFk:String) =
        viewModelScope.launch(dispatchers.main) {
            _secretId.removeSource(secretIdSource)
            withContext(dispatchers.io) {
                secretIdSource = useCase.getSecretIdForLot(batchNo, isSecretIdExists, materialCode, plantFk)
            }
            _secretId.addSource(secretIdSource) {
                _secretId.value = it
            }
        }


    fun getAllStockByPlantList() = viewModelScope.launch(dispatchers.main) {
        _stockPlantList.removeSource(stockPlantListSource)
        withContext(dispatchers.io) {
            stockPlantListSource = useCase.getAllStockByPlant()
        }
        _stockPlantList.addSource(stockPlantListSource) {
            _stockPlantList.value = it
        }
    }


     fun getWeighBridgeDetailOnline() = viewModelScope.launch(dispatchers.main) {
        _weighBridgeOnline.removeSource(weighBridgeOnlineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeOnlineSource = useCase.getWeighBridgeDetailOnline()
        }
        _weighBridgeOnline.addSource(weighBridgeOnlineSource) {
            _weighBridgeOnline.value = it
        }
    }

    fun getLotDetails(charge: String, material: String, whId: String) =
        viewModelScope.launch(dispatchers.main) {
            _lotList.removeSource(lotListSource)
            withContext(dispatchers.io) {
                lotListSource = useCase.getValidLotsList(charge, material, whId)
            }
            _lotList.addSource(lotListSource) {
                _lotList.value = it
            }
        }


}
