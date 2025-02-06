package com.olam.warehouse.vegax.grnnicaragua.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.AdvanceLineItems
import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.master.common.model.GrnCharDetails
import com.olam.warehouse.master.common.model.GrnPriceDetails
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.common.utils.saveGrnSequence
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaGrnPost
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.grnnicaragua.data.domain.model.VegaNicaraguaUpdateLotSequencePost
import com.olam.warehouse.vegax.grnnicaragua.data.domain.usecase.VegaNicaraguaGrnUseCase
import com.olam.warehouse.vegax.grnnicaragua.utils.saveLotSequence
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */
class VegaNicaraguaGrnViewModel(
    private val useCase: VegaNicaraguaGrnUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {
    //val grnTransListDirect = MutableLiveData<List<VegaReceiving>>()

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val supplier: LiveData<List<VegaVendor>> get() = _supplier

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var storageLocationSource: LiveData<List<VegaStorageLocation>> = MutableLiveData()
    private val _storageLocation = MediatorLiveData<List<VegaStorageLocation>>()
    val storageLocation: LiveData<List<VegaStorageLocation>> get() = _storageLocation

    private var gradeSource: LiveData<List<VegaQualitative>> = mutableLiveDataOf(emptyList())
    private val _grade = MediatorLiveData<List<VegaQualitative>>()
    val grade: LiveData<List<VegaQualitative>> get() = _grade

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var materialQualityGradeSource: LiveData<List<VegaNicaraguaMaterialQualitGrades>> = mutableLiveDataOf(emptyList())
    private val _materialQualitygrade = MediatorLiveData<List<VegaNicaraguaMaterialQualitGrades>>()
    val materialQualityGrades: LiveData<List<VegaNicaraguaMaterialQualitGrades>> get() = _materialQualitygrade

    private var bagSource: LiveData<List<VegaNicaraguaWeighmentBagMaterial>> = mutableLiveDataOf()
    private val _bagItems = MediatorLiveData<List<VegaNicaraguaWeighmentBagMaterial>>()
    val bagItems: LiveData<List<VegaNicaraguaWeighmentBagMaterial>> get() = _bagItems

    private var grnCharDetailsSource: LiveData<Resource<GenericReqAndResp<List<GrnCharDetails>>>> =
        MutableLiveData()
    private val _grnCharDetails = MediatorLiveData<Resource<GenericReqAndResp<List<GrnCharDetails>>>>()
    val grnCharDetails: LiveData<Resource<GenericReqAndResp<List<GrnCharDetails>>>> get() = _grnCharDetails

    private var grnCharDetailsOfflineSource: LiveData<List<VegaNicaraguaGrnCharDetails>> =
        MutableLiveData()
    private val _grnCharDetailsOffline = MediatorLiveData<List<VegaNicaraguaGrnCharDetails>>()
    val grnCharDetailsOffline: LiveData<List<VegaNicaraguaGrnCharDetails>> get() = _grnCharDetailsOffline

    private var priceConfigInfoSource: LiveData<VegaNicaraguaPriceConfigDetails> = MutableLiveData()
    private val _priceConfigInfo = MediatorLiveData<VegaNicaraguaPriceConfigDetails>()
    val priceConfigInfo: LiveData<VegaNicaraguaPriceConfigDetails> get() = _priceConfigInfo

    private var exchangeRateSource: LiveData<Resource<GenericReqAndResp<ExchangeRate>>> =
        MutableLiveData()
    private val _exchangeRate = MediatorLiveData<Resource<GenericReqAndResp<ExchangeRate>>>()
    val exchangeRate: LiveData<Resource<GenericReqAndResp<ExchangeRate>>> get() = _exchangeRate

    private var grnPriceDetailsSource: LiveData<Resource<GenericReqAndResp<List<GrnPriceDetails>>>> =
        MutableLiveData()
    private val _grnPriceDetails = MediatorLiveData<Resource<GenericReqAndResp<List<GrnPriceDetails>>>>()
    val grnPriceDetails: LiveData<Resource<GenericReqAndResp<List<GrnPriceDetails>>>> get() = _grnPriceDetails

    private var grnPriceDetailsOfflineSource: LiveData<List<VegaNicaraguaGrnPriceDetails>> =
        MutableLiveData()
    private val _grnPriceDetailsOffline = MediatorLiveData<List<VegaNicaraguaGrnPriceDetails>>()
    val grnPriceDetailsOffline: LiveData<List<VegaNicaraguaGrnPriceDetails>> get() = _grnPriceDetailsOffline

    private var exchangeRateSourceOffline: LiveData<VegaNicaraguaExchangeRate> =
        MutableLiveData()
    private val _exchangeRateOffline = MediatorLiveData<VegaNicaraguaExchangeRate>()
    val exchangeRateOffline: LiveData<VegaNicaraguaExchangeRate> get() = _exchangeRateOffline

    private var qualityListSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist

    private var createGRNSource: LiveData<Resource<GenericReqAndResp<VegaNicaraguaGrnPost>>> = MutableLiveData()
    private val _createGRN = MediatorLiveData<Resource<GenericReqAndResp<VegaNicaraguaGrnPost>>>()
    val postGRN: LiveData<Resource<GenericReqAndResp<VegaNicaraguaGrnPost>>> get() = _createGRN

    private var gradeMappingSource: LiveData<VegaNicaraguaPositionGradeMappings> = MutableLiveData()
    private val _gradeMapping = MediatorLiveData<VegaNicaraguaPositionGradeMappings>()
    val gradeMapping: LiveData<VegaNicaraguaPositionGradeMappings> get() = _gradeMapping

    private var grnTransListSource: LiveData<List<VegaReceiving>> = MutableLiveData()
    private val _grnTransList = MediatorLiveData<List<VegaReceiving>>()
    val grnTransList: LiveData<List<VegaReceiving>> get() = _grnTransList

    private var qualitySource: LiveData<List<VegaQuality>> = MutableLiveData()
    private val _quality = MediatorLiveData<List<VegaQuality>>()
    val quality: LiveData<List<VegaQuality>> get() = _quality

    private var miscellaneousSource: LiveData<List<VegaCocoaMiscellaneous>> = mutableLiveDataOf()
    private val _miscellaneousItems = MediatorLiveData<List<VegaCocoaMiscellaneous>>()
    val miscellaneousItems: LiveData<List<VegaCocoaMiscellaneous>> get() = _miscellaneousItems

    private var advanceLineItemsDetailsSource: LiveData<Resource<GenericReqAndResp<List<AdvanceLineItems>>>> =
        MutableLiveData()
    private val _advanceLineItemsDetail = MediatorLiveData<Resource<GenericReqAndResp<List<AdvanceLineItems>>>>()
    val advanceLineItemsDetails: LiveData<Resource<GenericReqAndResp<List<AdvanceLineItems>>>> get() = _advanceLineItemsDetail

    private var advanceLineItemsDetailsOfflineSource: LiveData<List<VegaNicaraguaAdvanceLineItems>> =
        MutableLiveData()
    private val _advanceLineItemsOfflineDetail = MediatorLiveData<List<VegaNicaraguaAdvanceLineItems>>()
    val advanceLineItemsOfflineDetails: LiveData<List<VegaNicaraguaAdvanceLineItems>> get() = _advanceLineItemsOfflineDetail

    private var advanceItemsSource: LiveData<List<VegaNicaraguaAdvanceLineItemGrn>> =
        MutableLiveData()
    private val _advanceItems = MediatorLiveData<List<VegaNicaraguaAdvanceLineItemGrn>>()
    val advanceItems: LiveData<List<VegaNicaraguaAdvanceLineItemGrn>> get() = _advanceItems

    private var updateLotSequenceSource: LiveData<Resource<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>> =
        MutableLiveData()
    private val _updateLotSequence = MediatorLiveData<Resource<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>>()
    val updateLotSequence: LiveData<Resource<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>> get() = _updateLotSequence

    private var poListSource: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> =
            MutableLiveData()
    private val _poList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>()
    val poList: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> get() = _poList

    private var poListLocalSource: LiveData<List<VegaEcuadorPurchaseOrder>> =
        MutableLiveData()
    private val _poListLocal = MediatorLiveData<List<VegaEcuadorPurchaseOrder>>()
    val poListLocal: LiveData<List<VegaEcuadorPurchaseOrder>> get() = _poListLocal

    private var grnPrintDetailsSource: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> =
        MutableLiveData()
    private val _grnPrintDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>()
    val grnPrintDetails: LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> get() = _grnPrintDetails

    private var grnPrintDetailsOfflineSource: LiveData<List<VegaReceiving>> =
        MutableLiveData()
    private val _grnPrintDetailsOffline = MediatorLiveData<List<VegaReceiving>>()
    val grnPrintDetailsOffline: LiveData<List<VegaReceiving>> get() = _grnPrintDetailsOffline

    private var invoiceOfflineSource: LiveData<List<VegaNicaraguaInvoiceDetails>> =
        MutableLiveData()
    private val _invoiceOffline = MediatorLiveData<List<VegaNicaraguaInvoiceDetails>>()
    val invoiceOffline: LiveData<List<VegaNicaraguaInvoiceDetails>> get() = _invoiceOffline

    private var farmerListSource: LiveData<List<VegaTrackTraceFarmerData>> = MutableLiveData()
    private val _farmerList = MediatorLiveData<List<VegaTrackTraceFarmerData>>()
    val farmerList: LiveData<List<VegaTrackTraceFarmerData>> get() = _farmerList


    fun getInvoiceOfflineData() = viewModelScope.launch(dispatchers.main) {
        _invoiceOffline.removeSource(invoiceOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            invoiceOfflineSource = useCase.getInvoiceOfflineData()
        }
        _invoiceOffline.addSource(invoiceOfflineSource) {
            _invoiceOffline.value = it
        }
    }

    fun getGrnPrintDetailsOffline() = viewModelScope.launch(dispatchers.main) {
        _grnPrintDetailsOffline.removeSource(grnPrintDetailsOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            grnPrintDetailsOfflineSource = useCase.getGrnPrintDetailsOffline()
        }
        _grnPrintDetailsOffline.addSource(grnPrintDetailsOfflineSource) {
            _grnPrintDetailsOffline.value = it
        }
    }

    fun getGrnPrintDetails() = viewModelScope.launch(dispatchers.main) {
        _grnPrintDetails.removeSource(grnPrintDetailsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            grnPrintDetailsSource = useCase.getGrnPrintDetails()
        }
        _grnPrintDetails.addSource(grnPrintDetailsSource) {
            _grnPrintDetails.value = it
        }
    }

    fun getSuppliers(purchaseOrgType: String?) = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCase.getSuppliers(purchaseOrgType)
        }
        _supplier.addSource(supplierSource) {
            _supplier.value = it
        }
    }

    fun getProducts() = viewModelScope.launch(dispatchers.main) {
        _product.removeSource(productSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            productSource = useCase.getProducts()
        }
        _product.addSource(productSource) {
            _product.value = it
        }
    }

    fun getStorageLocations() = viewModelScope.launch(dispatchers.main) {
        _storageLocation.removeSource(storageLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            storageLocationSource = useCase.getStorageLocations()
        }
        _storageLocation.addSource(storageLocationSource) {
            _storageLocation.value = it
        }
    }

    fun getGrades(materialCode: String) = viewModelScope.launch(dispatchers.main) {
        _grade.removeSource(gradeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            gradeSource = useCase.getGrades(materialCode)
        }
        _grade.addSource(gradeSource) {
            _grade.value = it
        }
    }

    fun saveBagDetails(material: VegaNicaraguaWeighmentBagMaterial) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveBagDetails(material)
        }
    }

    fun deleteBagDetails(id: Int, tmpWbId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteBagDetails(id, tmpWbId)
        }
    }

    fun getBagItems(tmpWbId: String) =
        viewModelScope.launch(dispatchers.main) {
            _bagItems.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                bagSource = useCase.getBagItems(tmpWbId)
            }
            _bagItems.addSource(bagSource) {
                _bagItems.value = it
            }
        }

    fun getPriceConfigInfo(materialCode: String, qualityCode: String) = viewModelScope.launch(dispatchers.main) {
        _priceConfigInfo.removeSource(priceConfigInfoSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            priceConfigInfoSource = useCase.getPriceConfigInfo(materialCode, qualityCode)
        }
        _priceConfigInfo.addSource(priceConfigInfoSource) {
            _priceConfigInfo.value = it
        }
    }

    fun getConfigItems(role: String) = viewModelScope.launch(dispatchers.main) {
        _configItems.removeSource(configSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            configSource = useCase.getConfigItems(role)
        }
        _configItems.addSource(configSource) {
            _configItems.value = it
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

    fun getExchangeRateOffline() = viewModelScope.launch(dispatchers.main) {
        _exchangeRateOffline.removeSource(exchangeRateSourceOffline) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            exchangeRateSourceOffline = useCase.getExchangeRateOffline()
        }
        _exchangeRate.addSource(exchangeRateSourceOffline) {
            _exchangeRateOffline.value = it
        }
    }

    //Quality
    fun getQualityParams(materialId: String, isValueExist: Boolean?, wbId: String?) = viewModelScope.launch(dispatchers.main) {
        _qualitylist.removeSource(qualityListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityListSource = useCase.getQualityParams(materialId, isValueExist, wbId)
        }
        _qualitylist.addSource(qualityListSource) {
            _qualitylist.value = it
        }
    }

    fun getGrnCharDetails(grade: String, materialCode: String) = viewModelScope.launch(dispatchers.main) {
        _grnCharDetails.removeSource(grnCharDetailsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            grnCharDetailsSource = useCase.getGrnCharDetails(grade, materialCode)
        }
        _grnCharDetails.addSource(grnCharDetailsSource) {
            _grnCharDetails.value = it
        }
    }

    fun getGrnCharDetailsOffline(grade: String, materialCode: String) = viewModelScope.launch(dispatchers.main) {
        _grnCharDetailsOffline.removeSource(grnCharDetailsOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            grnCharDetailsOfflineSource = useCase.getGrnCharDetailsOffline(grade, materialCode)
        }
        _grnCharDetailsOffline.addSource(grnCharDetailsOfflineSource) {
            _grnCharDetailsOffline.value = it
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

    fun getGrnPriceDetailsOffline() = viewModelScope.launch(dispatchers.main) {
        _grnPriceDetailsOffline.removeSource(grnPriceDetailsOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            grnPriceDetailsOfflineSource = useCase.getGrnPriceDetailsOffline()
        }
        _grnPriceDetailsOffline.addSource(grnPriceDetailsOfflineSource) {
            _grnPriceDetailsOffline.value = it
        }
    }

    fun postCreateGRN(
        receivingData:VegaNicaraguaGrnPost
    ) = viewModelScope.launch(dispatchers.main) {
        _createGRN.removeSource(createGRNSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            createGRNSource = useCase.postGrnDetails(receivingData)
        }
        _createGRN.addSource(createGRNSource) {
            _createGRN.value = it
        }
    }

    fun getGradeMapping(qualityGrade: String) = viewModelScope.launch(dispatchers.main) {
        _gradeMapping.removeSource(gradeMappingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            gradeMappingSource = useCase.getGradeMapping(qualityGrade)
        }
        _gradeMapping.addSource(gradeMappingSource) {
            _gradeMapping.value = it
        }
    }

    fun saveQualityData(prepareQualityData: VegaQuality) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveQualityData(prepareQualityData)
        }
    }

    fun saveGrnData(receivingData: VegaReceiving) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveGrnData(receivingData)
        }
    }

    fun getReceivingWithLineItem() = viewModelScope.launch(dispatchers.main) {
        _grnTransList.removeSource(grnTransListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            grnTransListSource = useCase.getReceivingWithLineItem()
        }
        _grnTransList.addSource(grnTransListSource) {
            _grnTransList.value = it
        }
    }

    /*fun getReceivingWithLineItemDirect() = viewModelScope.launch {
        withContext(dispatchers.io) {
            grnTransListDirect.postValue(useCase.getReceivingWithLineItemDirect())
        }
    }*/

    fun getQuality(tmpWbId: String)  = viewModelScope.launch(dispatchers.main) {
        _quality.removeSource(qualitySource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualitySource = useCase.getQuality(tmpWbId)
        }
        _quality.addSource(qualitySource) {
            _quality.value = it
        }
    }

    fun  updateGrnDataSuccess(
        data: VegaNicaraguaGrnPost,
        vegaReceiving: VegaReceiving
    ) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateGrnDataSuccess(data, vegaReceiving)
        }
    }

    fun deleteAllItem(tmpWbId: String)  = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteAllItem(tmpWbId)
        }
    }

    fun getGlDetails(role: String) = viewModelScope.launch(dispatchers.main) {
        _miscellaneousItems.removeSource(miscellaneousSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            miscellaneousSource = useCase.getGlDetails(role)
        }
        _miscellaneousItems.addSource(miscellaneousSource) {
            _miscellaneousItems.value = it
        }
    }

    fun getAdvanceDetailsByVendor(vendorCode:String) = viewModelScope.launch(dispatchers.main) {
        _advanceLineItemsDetail.removeSource(advanceLineItemsDetailsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            advanceLineItemsDetailsSource = useCase.getAdvanceLineDetails(vendorCode)
        }
        _advanceLineItemsDetail.addSource(advanceLineItemsDetailsSource) {
            _advanceLineItemsDetail.value = it
        }
    }

    fun getAdvanceDetailsByVendorOffline(vendorCode:String) = viewModelScope.launch(dispatchers.main) {
        _advanceLineItemsOfflineDetail.removeSource(advanceLineItemsDetailsOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            advanceLineItemsDetailsOfflineSource = useCase.getAdvanceLineDetailsOffline(vendorCode)
        }
        _advanceLineItemsOfflineDetail.addSource(advanceLineItemsDetailsOfflineSource) {
            _advanceLineItemsOfflineDetail.value = it
        }
    }

    fun generateBatchNumber(transactionList: MutableList<VegaReceiving>): String {
       executeAgain@ var lotId = ""
        val plant = getPlantDetails()
        val plantCode = plant.plantCode
        val rightNow = Calendar.getInstance()
        var year: String? = ""
        var currentmonth = (rightNow.get(Calendar.MONTH)+1).toString()
        var  currentyear = rightNow.get(Calendar.YEAR)
//        val year = rightNow.get(Calendar.YEAR).toString().substring(2)
        if (PreferenceHelper.get(Constants.CROP_FINANCIAL_YEAR, "").isEmpty()) {
            year = if (currentmonth.equals("10") || currentmonth.equals("11") || currentmonth.equals("12"))
                (currentyear + 1).toString().substring(2) else currentyear.toString().substring(2)
        } else {
            year = PreferenceHelper.get(Constants.CROP_FINANCIAL_YEAR, "").takeLast(2)
        }
//        val year = if(currentmonth.equals("10") || currentmonth.equals("11") ||currentmonth.equals("12"))
//            (currentyear+1).toString().substring(2) else currentyear.toString().substring(2)
        var lotSequence = PreferenceHelper.get(Constants.LOT_SEQUENCE, "")
        val isSeqExist = transactionList.filter { it.status.equals(Status.SYNC_COMPLETED)||it.status.equals(Status.SYNC_PENDING) }.map { it.batchNumber }.any { it?.contains(lotSequence) == true }
        if(isSeqExist){
            saveLotSequence("", transactionList, true)
            return generateBatchNumberAgain(transactionList)
        }
        lotId = plantCode.plus(year).plus(lotSequence)

        return lotId
    }

    fun generateBatchNumberAgain(transactionList: MutableList<VegaReceiving>): String {
        executeAgain@ var lotId = ""
        val plant = getPlantDetails()
        val plantCode = plant.plantCode
        val rightNow = Calendar.getInstance()
        var currentmonth = (rightNow.get(Calendar.MONTH)+1).toString()
        var  currentyear = rightNow.get(Calendar.YEAR)
        var year: String? = ""
//        val year = rightNow.get(Calendar.YEAR).toString().substring(2)
        if (PreferenceHelper.get(Constants.CROP_FINANCIAL_YEAR, "").isEmpty()) {
            year = if(currentmonth.equals("10") || currentmonth.equals("11") ||currentmonth.equals("12"))
            (currentyear+1).toString().substring(2) else currentyear.toString().substring(2)
        } else {
            year = PreferenceHelper.get(Constants.CROP_FINANCIAL_YEAR, "").takeLast(2)
        }
//        val year = if(currentmonth.equals("10") || currentmonth.equals("11") ||currentmonth.equals("12"))
//            (currentyear+1).toString().substring(2) else currentyear.toString().substring(2)
        val lotSequence = PreferenceHelper.get(Constants.LOT_SEQUENCE, "")
        /*val isSeqExist = transactionList.filter { it.status.equals(Status.SYNC_COMPLETED)||it.status.equals(Status.SYNC_PENDING) }.map { it.batchNumber }.any { it?.contains(lotSequence) == true }
        if(isSeqExist){
            saveLotSequence("", transactionList, true)
            generateBatchNumber(transactionList)
        }*/
        lotId = plantCode.plus(year).plus(lotSequence)

        return lotId
    }

    fun generateGrnSequnceNumber(transactionList: MutableList<VegaReceiving>): String {
        /*val grnNos = transactionList.filter { it.status.equals(Status.SYNC_COMPLETED)||it.status.equals(
            Status.SYNC_PENDING) }.map { it.palletType }*/
        var grnSequnceId = ""
        val plant = getPlantDetails()
        val plantCode = plant.plantCode
        val plantId = plant.plantId
        val rightNow = Calendar.getInstance()
        var currentmonth = (rightNow.get(Calendar.MONTH)+1).toString()
        var  currentyear = rightNow.get(Calendar.YEAR)
        var year: String? = ""
        if (PreferenceHelper.get(Constants.CROP_FINANCIAL_YEAR, "").isEmpty()) {
            year = if(currentmonth.equals("10") || currentmonth.equals("11") ||currentmonth.equals("12"))
            (currentyear+1).toString().substring(2) else currentyear.toString().substring(2)
        } else {
            year = PreferenceHelper.get(Constants.CROP_FINANCIAL_YEAR, "").takeLast(2)
        }
//        val year = if(currentmonth.equals("10") || currentmonth.equals("11") ||currentmonth.equals("12"))
//            (currentyear+1).toString().substring(2) else currentyear.toString().substring(2)
        val grnSequence = PreferenceHelper.get(Constants.GRN_SEQUENCE, "")
        val isSeqExist = transactionList.filter { it.status.equals(Status.SYNC_COMPLETED)||it.status.equals(Status.SYNC_PENDING) }.map { it.palletType }.any { it?.contains(grnSequence) == true }
        if(isSeqExist){
            saveGrnSequence(transactionList, true)
            return generateGrnSequnceNumberAgain(transactionList)
        }
        grnSequnceId = plantId.plus(year).plus(grnSequence)

        return grnSequnceId
    }

    fun generateGrnSequnceNumberAgain(transactionList: MutableList<VegaReceiving>): String {
        /*val grnNos = transactionList.filter { it.status.equals(Status.SYNC_COMPLETED)||it.status.equals(
            Status.SYNC_PENDING) }.map { it.palletType }*/
        var grnSequnceId = ""
        val plant = getPlantDetails()
        val plantCode = plant.plantCode
        val plantId = plant.plantId
        val rightNow = Calendar.getInstance()
        var currentmonth = (rightNow.get(Calendar.MONTH)+1).toString()
        var  currentyear = rightNow.get(Calendar.YEAR)
        var year: String? = ""
        if (PreferenceHelper.get(Constants.CROP_FINANCIAL_YEAR, "").isEmpty()) {
            year = if(currentmonth.equals("10") || currentmonth.equals("11") ||currentmonth.equals("12"))
            (currentyear+1).toString().substring(2) else currentyear.toString().substring(2)
        } else {
            year = PreferenceHelper.get(Constants.CROP_FINANCIAL_YEAR, "").takeLast(2)
        }
//        val year = if(currentmonth.equals("10") || currentmonth.equals("11") ||currentmonth.equals("12"))
//            (currentyear+1).toString().substring(2) else currentyear.toString().substring(2)
        val grnSequence = PreferenceHelper.get(Constants.GRN_SEQUENCE, "")
        /*val isSeqExist = transactionList.filter { it.status.equals(Status.SYNC_COMPLETED)||it.status.equals(Status.SYNC_PENDING) }.map { it.palletType }.any { it?.contains(grnSequence) == true }
        if(isSeqExist){
            saveGrnSequence(transactionList, true)
            generateGrnSequnceNumber(transactionList)
        }*/
        grnSequnceId = plantId.plus(year).plus(grnSequence)

        return grnSequnceId
    }

    fun postUpdateLotSequence(batchNumber: String, isInvoiceFlag: Boolean) = viewModelScope.launch(dispatchers.main) {
        _updateLotSequence.removeSource(updateLotSequenceSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            val invoiceNo = PreferenceHelper.get(Constants.INVOICE_SEQUENCE, "")
            val grnSequncce1 = PreferenceHelper.get(Constants.GRN_SEQUENCE, "")
            var grnSequncce = ""
            var lotSequence1 = if (batchNumber.length == 5) batchNumber else batchNumber.substring(batchNumber.length - 5)
            var lotSequence = if(lotSequence1.isNotEmpty()) (lotSequence1.toInt()-1).toString() else lotSequence1
            when (lotSequence.toString().length) {
                1 ->lotSequence= "0000".plus(lotSequence.toString())
                2 ->lotSequence= "000".plus(lotSequence.toString())
                3 ->lotSequence= "00".plus(lotSequence.toString())
                4 ->lotSequence= "0".plus(lotSequence.toString())
                5 -> lotSequence.toString()
            }
            val grnSequncce2 = grnSequncce1.substring(grnSequncce1.length - 5)

            if (grnSequncce2.isNotEmpty()) grnSequncce = (grnSequncce2.toInt() - 1).toString()
            when (grnSequncce.toString().length) {
                1 ->grnSequncce= "0000".plus(grnSequncce.toString())
                2 -> grnSequncce="000".plus(grnSequncce.toString())
                3 -> grnSequncce="00".plus(grnSequncce.toString())
                4 -> grnSequncce="0".plus(grnSequncce.toString())
                5 -> grnSequncce.toString()
            }
            var invoiceSequence = ""
            var invoiceSequence1 = ""
            val rightNow = Calendar.getInstance()
            var currentmonth = (rightNow.get(Calendar.MONTH)+1).toString()
            var  currentyear = rightNow.get(Calendar.YEAR)

            val year = if(currentmonth.equals("10") || currentmonth.equals("11") ||currentmonth.equals("12"))
               (currentyear+1).toString() else currentyear.toString()

            val prefix1 = PreferenceHelper.get(Constants.USER_NAME, "")
            var isInvoiceSequence = ""
            if (isInvoiceFlag && invoiceNo.isNotEmpty()) {
                invoiceSequence1 = if (invoiceNo.length == 5) invoiceNo else invoiceNo.substring(invoiceNo.length - 5)

                if (invoiceSequence1.isNotEmpty()) invoiceSequence = (invoiceSequence1.toInt() - 1).toString()
                when (invoiceSequence.toString().length) {
                    1 -> invoiceSequence="0000".plus(invoiceSequence.toString())
                    2 -> invoiceSequence="000".plus(invoiceSequence.toString())
                    3 ->invoiceSequence= "00".plus(invoiceSequence.toString())
                    4 ->invoiceSequence= "0".plus(invoiceSequence.toString())
                    5 -> invoiceSequence.toString()
                }

                isInvoiceSequence = "Y"
            } else {
                invoiceSequence = ""
                isInvoiceSequence = "N"
            }
            val postData = VegaNicaraguaUpdateLotSequencePost(
                plant = getPlantDetails(),
                prefix1 = prefix1,
                year = year,
                sequence = lotSequence,
                isLotSequence = "Y",
                isInSequence = isInvoiceSequence,
                invoiceSequence = invoiceSequence,
                grnSequence = grnSequncce,
                isGrnRefSequence = "Y",
                poSequence = "",
                isPoRefSequence = "N",
                prefix3 = Constants.GRN_SEQUENCE
            )
            updateLotSequenceSource = useCase.updateLotSequence(postData)
        }
        _updateLotSequence.addSource(updateLotSequenceSource) {
            _updateLotSequence.value = it
        }
    }

    fun saveAdvanceLineItem(advanceLineItem: ArrayList<VegaNicaraguaAdvanceLineItemGrn>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveAdvanceLineItem(advanceLineItem)
        }
    }

    fun getAdvanceItem(tmpId: String) = viewModelScope.launch(dispatchers.main) {
        _advanceItems.removeSource(advanceItemsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            advanceItemsSource = useCase.getAdvanceItem(tmpId)
        }
        _advanceItems.addSource(advanceItemsSource) {
            _advanceItems.value = it
        }
    }

    fun getMaterialQualityGrades(materialCode: String) = viewModelScope.launch(dispatchers.main) {
        _materialQualitygrade.removeSource(materialQualityGradeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            materialQualityGradeSource = useCase.getMaterialQualityGrades(materialCode)
        }
        _materialQualitygrade.addSource(materialQualityGradeSource) {
            _materialQualitygrade.value = it
        }
    }

    fun updateSyncStartedStatus(tmpWbId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updateSyncStartedStatus(tmpWbId)
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

    fun getPOListLocal() = viewModelScope.launch(dispatchers.main) {
        _poListLocal.removeSource(poListLocalSource)
        withContext(dispatchers.io) {
            poListLocalSource = useCase.getPOListLocal()
        }
        _poListLocal.addSource(poListLocalSource) {
            _poListLocal.value = it
        }
    }

    fun removeAdvanceLineItem(documentNumber: String?, tmpWbId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.removeAdvanceLineItem(documentNumber, tmpWbId)
        }
    }

    fun updatePostingDate(tmpWbId: String, postdate :String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.updatePostingDate(tmpWbId,postdate)
        }
    }

    fun getFarmerList(){
        viewModelScope.launch(dispatchers.main) {
            _farmerList.removeSource(farmerListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                farmerListSource = useCase.getFarmerList()
            }
            _farmerList.addSource(farmerListSource) {
                _farmerList.value = it
            }
        }
    }



}
