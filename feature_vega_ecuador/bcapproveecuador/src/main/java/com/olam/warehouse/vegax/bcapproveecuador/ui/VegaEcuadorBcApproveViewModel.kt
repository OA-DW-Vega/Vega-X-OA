package com.olam.warehouse.vegax.bcapproveecuador.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoCoaReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.VegaCoCoaReceivingMtnrWithLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorBcApproveWBDetails
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorBcApprovePost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorBcApproveResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.bcapproveecuador.data.domain.model.VegaEcuadorApproveWbDetail
import com.olam.warehouse.vegax.bcapproveecuador.data.domain.model.VegaEcuadorBcApprovePostResponse
import com.olam.warehouse.vegax.bcapproveecuador.data.domain.model.VegaEcuadorPostApprovalRequest
import com.olam.warehouse.vegax.bcapproveecuador.data.domain.usecase.VegaEcuadorBcApproveUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaEcuadorBcApproveViewModel (
    private val useCase: VegaEcuadorBcApproveUseCase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {

    var vegaCoCoaReceivingData = VegaCoCoaReceiving()

    private var receiveSource: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> = MutableLiveData()
    private var receiveLocalSource: LiveData<List<VegaReceiving>> = MutableLiveData()
    private var receiveWithLineItemLocalSource: LiveData<List<VegaReceivingWithLineItems>> = MutableLiveData()
    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private var locationSource: LiveData<List<VegaSupplyStorageLocation>> = MutableLiveData()
    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    private var warehouseSource: LiveData<Resource<GenericReqAndResp<VegaCoCoaReceivingMtnWrapper>>> =
        MutableLiveData()
    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private var plantSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private var offlineStorageLocationsSource: LiveData<List<VegaCoCoaStorageLocation>> = MutableLiveData()

    private var weightBridgeSource: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorBcApproveWBDetails>>>> = MutableLiveData()
    private var warehouseSourceLocal: LiveData<List<VegaReceivingWarehouse>> = MutableLiveData()
    private var warehouseWithMtnsSource: LiveData<VegaReceivingWarehouseWithMtns> = MutableLiveData()
    private val _receive = MediatorLiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>()
    private val _receiveLocal = MediatorLiveData<List<VegaReceiving>>()
    private val _receiveWithLineItemLocal = MediatorLiveData<List<VegaReceivingWithLineItems>>()
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    private val _location = MediatorLiveData<List<VegaSupplyStorageLocation>>()
    private val _plant = MediatorLiveData<List<VegaCustomStLocation>>()
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()
    private val _warehouse = MediatorLiveData<Resource<GenericReqAndResp<VegaCoCoaReceivingMtnWrapper>>>()
    private val _warehouseLocal = MediatorLiveData<List<VegaReceivingWarehouse>>()
    private val _warehouseWithMtns = MediatorLiveData<VegaReceivingWarehouseWithMtns>()
    private var _weighBridge = MediatorLiveData<Resource<GenericReqAndResp<List<VegaEcuadorBcApproveWBDetails>>>>()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    private val _offlineStorageLocations = MediatorLiveData<List<VegaCoCoaStorageLocation>>()

    val receive: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> get() = _receive
    val receiveLocal: LiveData<List<VegaReceiving>> get() = _receiveLocal
    val receiveWithLineItemLocal: LiveData<List<VegaReceivingWithLineItems>> get() = _receiveWithLineItemLocal
    val product: LiveData<List<VegaMaterial>> get() = _product
    val suppplier: LiveData<List<VegaVendor>> get() = _supplier
    val location: LiveData<List<VegaSupplyStorageLocation>> get() = _location
    val plant: LiveData<List<VegaCustomStLocation>> get() = _plant
    val material: LiveData<List<VegaPackageMaterial>> get() = _material
    val warehouse: LiveData<Resource<GenericReqAndResp<VegaCoCoaReceivingMtnWrapper>>> get() = _warehouse
    val warehouseLocal: LiveData<List<VegaReceivingWarehouse>> get() = _warehouseLocal
    val warehouseWithMtns: LiveData<VegaReceivingWarehouseWithMtns> get() = _warehouseWithMtns
    val weighBridge: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorBcApproveWBDetails>>>> get() = _weighBridge


   private var weighBridgeIdSource: LiveData<Resource<GenericReqAndResp<VegaCoCoaQualityWBDetail>>> =
        MutableLiveData()
    private var _weighBridgeId = MediatorLiveData<Resource<GenericReqAndResp<VegaCoCoaQualityWBDetail>>>()
    val weighBridgeId: LiveData<Resource<GenericReqAndResp<VegaCoCoaQualityWBDetail>>> get() = _weighBridgeId

    private var approvalSource: LiveData<Resource<GenericReqAndResp<VegaEcuadorBcApproveResponse>>> =
        MutableLiveData()
    private val _approval = MediatorLiveData<Resource<GenericReqAndResp<VegaEcuadorBcApproveResponse>>>()
    val approval: LiveData<Resource<GenericReqAndResp<VegaEcuadorBcApproveResponse>>> get() = _approval

    private var qualitySource: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorApproveWbDetail>>>> =
        MutableLiveData()
    private val _qualityDetails =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaEcuadorApproveWbDetail>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorApproveWbDetail>>>> get() = _qualityDetails
   /* fun getWeighBridgeData(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> {
        getWeighBridgeDetail()
        return weighBridge
    }*/

    private var approvalWbSource: LiveData<Resource<GenericReqAndResp<VegaEcuadorBcApprovePostResponse>>> =
        MutableLiveData()
    private val _approvalWb =
        MediatorLiveData<Resource<GenericReqAndResp<VegaEcuadorBcApprovePostResponse>>>()
    val approvalWb: LiveData<Resource<GenericReqAndResp<VegaEcuadorBcApprovePostResponse>>> get() = _approvalWb

    private var featureMasterSource: LiveData<List<VegaFeatureMaster>> = MutableLiveData()
    private val _featureMaster = MediatorLiveData<List<VegaFeatureMaster>>()
    val featureMaster: LiveData<List<VegaFeatureMaster>> get() = _featureMaster

    fun getWeighBridgeDetail(plantId:String) = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weightBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weightBridgeSource = useCase.getWeighBridgeDetailOnline(plantId)
        }
        _weighBridge.addSource(weightBridgeSource) {
            _weighBridge.value = it
        }
    }
    fun postApproval(VegaEcuadorBcApprovePost: VegaEcuadorBcApprovePost) = viewModelScope.launch(dispatchers.main) {
        _approval.removeSource(approvalSource)
        withContext(dispatchers.io) {
            approvalSource = useCase.postApproval(VegaEcuadorBcApprovePost)
        }
        _approval.addSource(approvalSource) {
            _approval.value = it
        }
    }

    fun postApproval(postApprovalData: VegaEcuadorPostApprovalRequest) = viewModelScope.launch(dispatchers.main) {
        _approvalWb.removeSource(approvalWbSource)
        withContext(dispatchers.io) {
            approvalWbSource = useCase.postApproval(postApprovalData)
        }
        _approvalWb.addSource(approvalWbSource) {
            _approvalWb.value = it
        }
    }

    fun getQualityDetails(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorApproveWbDetail>>>> {
        fetchQualityDetails(charge, material)
        return qualityDetails
    }
    private fun fetchQualityDetails(charge: String, material: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetails.removeSource(qualitySource)
            withContext(dispatchers.io) {
                qualitySource = useCase.fetchQualityDetails(charge, material)
            }
            _qualityDetails.addSource(qualitySource) {
                _qualityDetails.value = it
            }
        }
    fun getFeatureMaster(module: String) = viewModelScope.launch(dispatchers.main) {
        _featureMaster.removeSource(featureMasterSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            featureMasterSource = useCase.getFeatureMaster(module)
        }
        _featureMaster.addSource(featureMasterSource) {
            _featureMaster.value = it
        }
    }
}

