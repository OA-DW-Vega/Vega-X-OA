package com.olam.warehouse.vegax.thirdpartysalescoffee.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.*
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaFeatureMaster
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeThirdPartyModelWithLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaPositionGradeMappings
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeTPDeliveryPost
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeWeighScalePallet
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeWeighbridgePostRequest
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaNicaraguaCoffeeTPDeliveryPost
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.usecase.VegaCoffeeThirdPartyUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VegaCoffeeThirdPartyViewModel(
    private val useCase: VegaCoffeeThirdPartyUseCase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {

    var lots: ArrayList<VegaCocoaDispatchLots> = ArrayList()
    var model = VegaCoffeeThirdPartyRequestModel()
    var currentScanLot: String = ""
    var salesOrder = VegaCoffeeSalesOrder()
    val thirdPartyMaterial = MutableLiveData<List<VegaMaterial>>()
    val thirdPartyInfo = MutableLiveData<VegaCoffeeThirdPartyModelWithLots>()
    var lotList = ArrayList<VegaCocoaDispatchLots>()
    var dispatchWh = VegaCocoaDispatchWB()

    fun generateBatchNumber(lotSequence:String): String {
        //var lotSequence = PreferenceHelper.get(Constants.LOT_SEQUENCE, "")
        var lotId = ""
        val plant = getPlantDetails()
        val plantCode = plant.plantCode
        val rightNow = Calendar.getInstance()
        var year: String? = ""
        var currentmonth = (rightNow.get(Calendar.MONTH)+1).toString()
        var  currentyear = rightNow.get(Calendar.YEAR)
        if (PreferenceHelper.get(Constants.CROP_FINANCIAL_YEAR, "").isEmpty()) {
            year = if (currentmonth.equals("10") || currentmonth.equals("11") || currentmonth.equals("12"))
                (currentyear + 1).toString().substring(2) else currentyear.toString().substring(2)
        } else {
            year = PreferenceHelper.get(Constants.CROP_FINANCIAL_YEAR, "").takeLast(2)
        }
        lotId = plantCode.plus(year).plus(lotSequence)

        return lotId
    }


    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> =
        MutableLiveData()
    private val _qualityDetails =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _qualityDetails

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val suppplier: LiveData<List<VegaVendor>> get() = _supplier

    private var filtersupplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _filteredsupplier = MediatorLiveData<List<VegaVendor>>()
    val filteredsupplier: LiveData<List<VegaVendor>> get() = _filteredsupplier

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf()
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    val validateLot = MutableLiveData<VegaCocoaDispatchLots>()

    private var deliveryPostSource: LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> =
        MutableLiveData()
    private val _deliveryPost =
        MediatorLiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>>()
    val deliveryPost: LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> get() = _deliveryPost

    private var receiveSource: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> =
        MutableLiveData()
    private val _receive = MediatorLiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>()
    val receive: LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> get() = _receive


    private var deliveryWSPostSource: LiveData<Resource<GenericReqAndResp<List<VegaDeliveryPostResponse>>>> =
        MutableLiveData()
    private val _deliveryWSPost =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaDeliveryPostResponse>>>>()
    val deliveryWSPost: LiveData<Resource<GenericReqAndResp<List<VegaDeliveryPostResponse>>>> get() = _deliveryWSPost

    private var bagSource: LiveData<List<VegaCocoaSweepingBagMaterial>> = mutableLiveDataOf()
    private val _bagItems = MediatorLiveData<List<VegaCocoaSweepingBagMaterial>>()
    val bagItems: LiveData<List<VegaCocoaSweepingBagMaterial>> get() = _bagItems

    private var palletResource: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeWeighScalePallet>>>> =
        MutableLiveData()
    private val _pallet =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaCoffeeWeighScalePallet>>>>()
    val pallet: LiveData<Resource<GenericReqAndResp<List<VegaCoffeeWeighScalePallet>>>> get() = _pallet

    private var dispatchTruckResource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> =
        MutableLiveData()
    private val _trucks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>>()
    val trucks: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> get() = _trucks

    private var vendorSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _vendor = MediatorLiveData<List<VegaVendor>>()
    val vendor: LiveData<List<VegaVendor>> get() = _supplier

    private var materialSource: LiveData<List<VegaMaterial>> = MutableLiveData()
    private val _material = MediatorLiveData<List<VegaMaterial>>()
    val material: LiveData<List<VegaMaterial>> get() = _material

    private var lotSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> = MutableLiveData()
    private val _lots = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val stockLots: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _lots

    private var stockListSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> = MutableLiveData()
    private val _stockList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val stockList: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _stockList

    private var postPileSource: LiveData<Resource<GenericReqAndResp<VegaCoffeeWeighbridgePostRequest>>> =
        MutableLiveData()
    private val _postPile = MediatorLiveData<Resource<GenericReqAndResp<VegaCoffeeWeighbridgePostRequest>>>()
    val postPile: LiveData<Resource<GenericReqAndResp<VegaCoffeeWeighbridgePostRequest>>> get() = _postPile

    private var exchangeRateSource: LiveData<Resource<GenericReqAndResp<ExchangeRate>>> =
        MutableLiveData()
    private val _exchangeRate = MediatorLiveData<Resource<GenericReqAndResp<ExchangeRate>>>()
    val exchangeRate: LiveData<Resource<GenericReqAndResp<ExchangeRate>>> get() = _exchangeRate

    private var grnPriceDetailsSource: LiveData<Resource<GenericReqAndResp<List<GrnPriceDetails>>>> =
        MutableLiveData()
    private val _grnPriceDetails = MediatorLiveData<Resource<GenericReqAndResp<List<GrnPriceDetails>>>>()
    val grnPriceDetails: LiveData<Resource<GenericReqAndResp<List<GrnPriceDetails>>>> get() = _grnPriceDetails

    private var poListSource: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> =
        MutableLiveData()
    private val _poList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>()
    val poList: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> get() = _poList

    private var trackTraceFeatureMasterSource: LiveData<List<VegaFeatureMaster>> = MutableLiveData()
    private val _trackTraceFeatureMaster = MediatorLiveData<List<VegaFeatureMaster>>()
    val trackTraceFeatureMaster: LiveData<List<VegaFeatureMaster>> get() = _trackTraceFeatureMaster


    fun getPostPile(postPileRequest: VegaCoffeeWeighbridgePostRequest) = viewModelScope.launch(dispatchers.main) {
        _postPile.removeSource(postPileSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            postPileSource = useCase.getPostWeighbridge(postPileRequest)
        }
        _postPile.addSource(postPileSource) {
            _postPile.value = it
        }
    }

    fun getVendors() = viewModelScope.launch(dispatchers.main) {
        _vendor.removeSource(vendorSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            vendorSource = useCase.getVendors()
        }
        _vendor.addSource(vendorSource) {
            _vendor.value = it
        }
    }

    fun getMaterials() = viewModelScope.launch(dispatchers.main) {
        _material.removeSource(materialSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            materialSource = useCase.getMaterials()
        }
        _material.addSource(materialSource) {
            _material.value = it
        }
    }

    fun getTruckList() = viewModelScope.launch(dispatchers.main) {
        _trucks.removeSource(dispatchTruckResource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchTruckResource = useCase.getTrucks()
        }
        _trucks.addSource(dispatchTruckResource) {
            _trucks.value = it
        }
    }

    fun getTTFeatureMaster(module: String) = viewModelScope.launch(dispatchers.main) {
        _trackTraceFeatureMaster.removeSource(trackTraceFeatureMasterSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            trackTraceFeatureMasterSource = useCase.getTTFeatureMaster(module)
        }
        _trackTraceFeatureMaster.addSource(trackTraceFeatureMasterSource) {
            _trackTraceFeatureMaster.value = it
        }
    }


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

    fun getSuppliers(purchaseOrgType: String?) = viewModelScope.launch(dispatchers.main) {
        _filteredsupplier.removeSource(filtersupplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            filtersupplierSource = useCase.getSuppliers(purchaseOrgType)
        }
        _filteredsupplier.addSource(filtersupplierSource) {
            _filteredsupplier.value = it
        }
    }


    fun getThirdPartyLocalData(vendorTypeWithMaterial: String) =
        viewModelScope.launch(dispatchers.main) {
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

    fun postSalesTruckInData(
        receivingData: VegaReceivingPost
    ) = viewModelScope.launch(dispatchers.main) {
        _receive.removeSource(receiveSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            receiveSource = useCase.postSalesDetail(receivingData)
        }
        _receive.addSource(receiveSource) {
            _receive.value = it
        }
    }

    fun getExchangeRate() = viewModelScope.launch(dispatchers.main) {
        _exchangeRate.removeSource(exchangeRateSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            exchangeRateSource = useCase.getExchangeRate()
        }
        _exchangeRate.addSource(exchangeRateSource) {
            _exchangeRate.value = it
        }
    }

    fun getGrnPriceDetails() = viewModelScope.launch(dispatchers.main) {
        _grnPriceDetails.removeSource(grnPriceDetailsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            grnPriceDetailsSource = useCase.getGrnPriceDetails()
        }
        _grnPriceDetails.addSource(grnPriceDetailsSource) {
            _grnPriceDetails.value = it
        }
    }

    private var gradeMappingSource: LiveData<VegaNicaraguaPositionGradeMappings> = MutableLiveData()
    private val _gradeMapping = MediatorLiveData<VegaNicaraguaPositionGradeMappings>()
    val gradeMapping: LiveData<VegaNicaraguaPositionGradeMappings> get() = _gradeMapping

    private var qualityPreSamplingSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> =
        MutableLiveData()
    private val _preSamplingQuality = MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>()
    val preSamplingQuality: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> get() = _preSamplingQuality

    fun getGradeMapping(qualityGrade: String) = viewModelScope.launch(dispatchers.main) {
        _gradeMapping.removeSource(gradeMappingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            gradeMappingSource = useCase.getGradeMapping(qualityGrade)
        }
        _gradeMapping.addSource(gradeMappingSource) {
            _gradeMapping.value = it
        }
    }
    fun getPreSamplingQualitydata(batchNo: String, materialId: String) =
        viewModelScope.launch(dispatchers.main) {
            _preSamplingQuality.removeSource(qualityPreSamplingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityPreSamplingSource = useCase.getPreSamplingQualityList(batchNo, materialId)
            }
            _preSamplingQuality.addSource(qualityPreSamplingSource) {
                _preSamplingQuality.value = it
            }
        }


    fun postNicDeliveryDetailForWeighScale(vegaDeliveryPost: VegaNicaraguaCoffeeTPDeliveryPost, type: String) =
        viewModelScope.launch(dispatchers.main) {
            _deliveryWSPost.removeSource(deliveryWSPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                deliveryWSPostSource = useCase.postNicDeliveryDetailForWeighScale(vegaDeliveryPost, type)
            }
            _deliveryWSPost.addSource(deliveryWSPostSource) {
                _deliveryWSPost.value = it
            }
        }

    fun getPOList() = viewModelScope.launch(dispatchers.main) {
        _poList.removeSource(poListSource)
        withContext(dispatchers.io) {
            poListSource = useCase.getPOList()
        }
        _poList.addSource(poListSource) {
            _poList.value = it
        }
    }
}
