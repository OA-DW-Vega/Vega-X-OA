package com.olam.warehouse.vegax.thirdpartysalescoffee.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeThirdPartyModelWithLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeTPDeliveryPost
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeWeighScalePallet
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.usecase.VegaCocoaThirdPartyUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VegaCocoaThirdPartyViewModel(
    private val useCase: VegaCocoaThirdPartyUseCase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {

    val thirdPartyMaterial = MutableLiveData<List<VegaMaterial>>()
    val thirdPartyInfo = MutableLiveData<VegaCoffeeThirdPartyModelWithLots>()
    var model = VegaCoffeeThirdPartyRequestModel()
    var lotList = ArrayList<VegaCocoaDispatchLots>()

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> =
        MutableLiveData()
    private val _qualityDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _qualityDetails

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val suppplier: LiveData<List<VegaVendor>> get() = _supplier

    private var lotSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> = MutableLiveData()
    private val _lots = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val stockLots: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _lots

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf()
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    val validateLot = MutableLiveData<VegaCocoaDispatchLots>()

    private var deliveryPostSource: LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> = MutableLiveData()
    private val _deliveryPost = MediatorLiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>>()
    val deliveryPost: LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> get() = _deliveryPost


    private var deliveryWSPostSource: LiveData<Resource<GenericReqAndResp<List<VegaDeliveryPostResponse>>>> =
        MutableLiveData()
    private val _deliveryWSPost = MediatorLiveData<Resource<GenericReqAndResp<List<VegaDeliveryPostResponse>>>>()
    val deliveryWSPost: LiveData<Resource<GenericReqAndResp<List<VegaDeliveryPostResponse>>>> get() = _deliveryWSPost

    private var bagSource: LiveData<List<VegaCocoaSweepingBagMaterial>> = mutableLiveDataOf()
    private val _bagItems = MediatorLiveData<List<VegaCocoaSweepingBagMaterial>>()
    val bagItems: LiveData<List<VegaCocoaSweepingBagMaterial>> get() = _bagItems

    private var palletResource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeWeighScalePallet>>>> =
        MutableLiveData()
    private val _pallet = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeWeighScalePallet>>>>()
    val pallet: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeWeighScalePallet>>>> get() = _pallet


    fun getThirdPartyMaterials() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            thirdPartyMaterial.postValue(useCase.getThirdPartyMaterials())
        }
    }

    fun addLoTInDB(lot: VegaCocoaDispatchLots) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertLot(lot)
        }
    }

    fun addLoTInDB(lot: List<VegaCocoaDispatchLots>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertLotList(lot)
        }
    }


    fun getStockList(material: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        _lots.removeSource(lotSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            lotSource = useCase.getStocks(material)
        }
        _lots.addSource(lotSource) {
            _lots.value = it
        }
    }

    fun getSuppliers() = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCase.getSupplier()
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
        }
    }


    fun getThirdPartyLocalData(vendorTypeWithMaterial: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            thirdPartyInfo.postValue(useCase.getThirdPartyInfo(vendorTypeWithMaterial))
        }
    }

    fun getLotDetails(charge: String, material: List<String>, whId: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualityDetails.removeSource(qualityParamSource)
            withContext(dispatchers.io) {
                qualityParamSource = useCase.getQualityParams(charge, material, whId)
            }
            _qualityDetails.addSource(qualityParamSource) {
                _qualityDetails.value = it
            }
        }

    fun validateLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateLot.postValue(useCase.validateLot(batchNumber))
        }
    }

    fun removeLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.removeLot(batchNumber)
        }
    }

    fun getCurrentDate(): String {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return simpleDateFormat.format(Date())
    }

    fun saveThirdPartyInfo() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertThirdPartyModel(model)
            if (lotList.isNotEmpty())
                useCase.insertLotList(lotList)
        }
    }

    fun updateSyncStatus(model: VegaCoffeeThirdPartyModelWithLots) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.updateAllSyncStatus(model)
            }
        }

    fun postDeliveryDetail(vegaDeliveryPost: VegaCoffeeTPDeliveryPost, type: String) =
        viewModelScope.launch(dispatchers.main) {
            _deliveryPost.removeSource(deliveryPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                deliveryPostSource = useCase.postDeliveryDetail(vegaDeliveryPost, type)
            }
            _deliveryPost.addSource(deliveryPostSource) {
                _deliveryPost.value = it
            }
        }

    fun postDeliveryDetailForWeighScale(vegaDeliveryPost: VegaCoffeeTPDeliveryPost, type: String) =
        viewModelScope.launch(dispatchers.main) {
            _deliveryWSPost.removeSource(deliveryWSPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                deliveryWSPostSource = useCase.postDeliveryDetailForWeighScale(vegaDeliveryPost, type)
            }
            _deliveryWSPost.addSource(deliveryWSPostSource) {
                _deliveryWSPost.value = it
            }
        }

    fun getBagItems() = viewModelScope.launch(dispatchers.main) {
        _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSource = useCase.getAllBagItems()
        }
        _bagItems.addSource(bagSource) {
            _bagItems.value = it
        }
    }

    fun getBagItems(batchNumber: String, material: String) = viewModelScope.launch(dispatchers.main) {
        _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSource = useCase.getBagItems(batchNumber, material)
        }
        _bagItems.addSource(bagSource) {
            _bagItems.value = it
        }
    }

    fun getPalletInfo(batchNumber: String, material: String) = viewModelScope.launch(dispatchers.main) {
        _pallet.removeSource(palletResource)
        withContext(dispatchers.io) {
            palletResource = useCase.getPalletDetails(batchNumber, material)
        }
        _pallet.addSource(palletResource) {
            _pallet.value = it
        }
    }

    fun deleteBagDetails(id: Int) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(id)
        }
    }

    fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveBagDetails(bagMaterial)
        }
    }

    fun getTotalTransferWeight(): Double {
        var sumOfWeight: Double = 0.0
        lotList.forEach {
            sumOfWeight = sumOfWeight.plus(prepareWeight(it.editedWeight ?: "0", it.unitOfMeasure ?: ""))
        }
        return sumOfWeight
    }

    private fun prepareWeight(weight: String, uom: String): Double {
        if (uom.isEmpty() || weight.isEmpty()) {
            return 0.0
        }
        when (uom) {
            "MT" -> return weight.toDouble() * 1000
            else -> return weight.toDouble()
        }
    }

    fun getTotalGrnPriceValue(price: String, weight: Double): String {
        return (price.toDouble() * weight).formatThreeDigits()
    }
}
