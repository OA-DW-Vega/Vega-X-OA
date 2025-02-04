package com.olam.warehouse.vegax.invoicenicaragua.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.modal.InventoryResponse
import com.olam.warehouse.master.common.model.AdvanceLineItems
import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.master.common.model.VendorGrnDetailsResponse
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.master.veganicaragua.model.VegaNicInvoiceReceipt
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaGrnWithInventoryDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaInvoicePostRequest
import com.olam.warehouse.vegax.invoicenicaragua.data.domain.usecase.VegaNicaraguaInvoiceUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */
class VegaNicaraguaInvoiceViewModel(
    private val useCase: VegaNicaraguaInvoiceUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val supplier: LiveData<List<VegaVendor>> get() = _supplier

    private var priceConfigInfoSource: LiveData<VegaNicaraguaPriceConfigDetails> = MutableLiveData()
    private val _priceConfigInfo = MediatorLiveData<VegaNicaraguaPriceConfigDetails>()
    val priceConfigInfo: LiveData<VegaNicaraguaPriceConfigDetails> get() = _priceConfigInfo

    private var grnDetailsSource: LiveData<Resource<GenericReqAndResp<List<VendorGrnDetailsResponse>>>> =
        MutableLiveData()
    private val _grnDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VendorGrnDetailsResponse>>>>()
    val grnDetails: LiveData<Resource<GenericReqAndResp<List<VendorGrnDetailsResponse>>>> get() = _grnDetails

    private var grnInventoryDetailsSource: LiveData<Resource<GenericReqAndResp<InventoryResponse>>> =
        MutableLiveData()
    private val _grnInventoryDetails = MediatorLiveData<Resource<GenericReqAndResp<InventoryResponse>>>()
    val grnInventoryDetails: LiveData<Resource<GenericReqAndResp<InventoryResponse>>> get() = _grnInventoryDetails

    private var grnInventoryDetailsOfflineSource: LiveData<List<VegaNicaraguaGRNInventoryDetails>> =
        MutableLiveData()
    private val _grnInventoryDetailsOffline = MediatorLiveData<List<VegaNicaraguaGRNInventoryDetails>>()
    val grnInventoryDetailsOffline: LiveData<List<VegaNicaraguaGRNInventoryDetails>> get() = _grnInventoryDetailsOffline

    private var grnDetailsOfflineSource: LiveData<List<VegaNicaraguaGrnWithInventoryDetails>> =
        MutableLiveData()
    private val _grnDetailsOffline = MediatorLiveData<List<VegaNicaraguaGrnWithInventoryDetails>>()
    val grnDetailsOffline: LiveData<List<VegaNicaraguaGrnWithInventoryDetails>> get() = _grnDetailsOffline

    private var invoiceOfflineSource: LiveData<List<VegaNicaraguaInvoiceDetails>> =
        MutableLiveData()
    private val _invoiceOffline = MediatorLiveData<List<VegaNicaraguaInvoiceDetails>>()
    val invoiceOffline: LiveData<List<VegaNicaraguaInvoiceDetails>> get() = _invoiceOffline

    private var exchangeRateSource: LiveData<Resource<GenericReqAndResp<ExchangeRate>>> =
        MutableLiveData()
    private val _exchangeRate = MediatorLiveData<Resource<GenericReqAndResp<ExchangeRate>>>()
    val exchangeRate: LiveData<Resource<GenericReqAndResp<ExchangeRate>>> get() = _exchangeRate

    private var exchangeRateSourceOffline: LiveData<VegaNicaraguaExchangeRate> =
        MutableLiveData()
    private val _exchangeRateOffline = MediatorLiveData<VegaNicaraguaExchangeRate>()
    val exchangeRateOffline: LiveData<VegaNicaraguaExchangeRate> get() = _exchangeRateOffline

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

    private var grnTransListSource: LiveData<List<VegaReceiving>> = MutableLiveData()
    private val _grnTransList = MediatorLiveData<List<VegaReceiving>>()
    val grnTransList: LiveData<List<VegaReceiving>> get() = _grnTransList

    private var qualityGradeDescSource: LiveData<QualitativeParams> = MutableLiveData()
    private val _qualityGradeDesc = MediatorLiveData<QualitativeParams>()
    val getQualityGradeDesc: LiveData<QualitativeParams> get() = _qualityGradeDesc

    private var qualityGradeDescListSource: LiveData<List<QualitativeParams>> = MutableLiveData()
    private val _qualityGradeDescList = MediatorLiveData<List<QualitativeParams>>()
    val getQualityGradeDescList: LiveData<List<QualitativeParams>> get() = _qualityGradeDescList

    private var createInvoiceSource: LiveData<Resource<GenericReqAndResp<VegaNicaraguaInvoicePostRequest>>> =
        MutableLiveData()
    private val _createInvoice = MediatorLiveData<Resource<GenericReqAndResp<VegaNicaraguaInvoicePostRequest>>>()
    val postInvoice: LiveData<Resource<GenericReqAndResp<VegaNicaraguaInvoicePostRequest>>> get() = _createInvoice

    private var invoiceReceiptSource: LiveData<Resource<GenericReqAndResp<List<VegaNicInvoiceReceipt>>>> =
        MutableLiveData()
    private val _invoiceReceipt = MediatorLiveData<Resource<GenericReqAndResp<List<VegaNicInvoiceReceipt>>>>()
    val invoiceReceipt: LiveData<Resource<GenericReqAndResp<List<VegaNicInvoiceReceipt>>>> get() = _invoiceReceipt

    private var invoiceReceiptOfflineSource: LiveData<List<VegaNicaraguaInvoiceDetails>> = MutableLiveData()
    private val _invoiceReceiptOffline = MediatorLiveData<List<VegaNicaraguaInvoiceDetails>>()
    val invoiceReceiptOffline: LiveData<List<VegaNicaraguaInvoiceDetails>> get() = _invoiceReceiptOffline

    fun getInvoiceReceiptOffline() = viewModelScope.launch(dispatchers.main) {
        _invoiceReceiptOffline.removeSource(invoiceReceiptOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            invoiceReceiptOfflineSource = useCase.getInvoiceReceiptOffline()
        }
        _invoiceReceiptOffline.addSource(invoiceReceiptOfflineSource) {
            _invoiceReceiptOffline.value = it
        }
    }

    fun getInvoiceReceipt() = viewModelScope.launch(dispatchers.main) {
        _invoiceReceipt.removeSource(invoiceReceiptSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            invoiceReceiptSource = useCase.getInvoiceReceipt()
        }
        _invoiceReceipt.addSource(invoiceReceiptSource) {
            _invoiceReceipt.value = it
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

    fun getPriceConfigInfo(materialCode: String, qualityCode: String) = viewModelScope.launch(dispatchers.main) {
        _priceConfigInfo.removeSource(priceConfigInfoSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            priceConfigInfoSource = useCase.getPriceConfigInfo(materialCode, qualityCode)
        }
        _priceConfigInfo.addSource(priceConfigInfoSource) {
            _priceConfigInfo.value = it
        }
    }

    fun getGrnDetailsByVendor(vendorCode: String) = viewModelScope.launch(dispatchers.main) {
        _grnDetails.removeSource(grnDetailsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            grnDetailsSource = useCase.getGrnDetails(vendorCode)
        }
        _grnDetails.addSource(grnDetailsSource) {
            _grnDetails.value = it
        }
    }

    fun getGrnDetailsByVendorOffline(vendorCode: String) = viewModelScope.launch(dispatchers.main) {
        _grnDetailsOffline.removeSource(grnDetailsOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            grnDetailsOfflineSource = useCase.getGrnDetailsOffline(vendorCode)
        }
        _grnDetailsOffline.addSource(grnDetailsOfflineSource) {
            _grnDetailsOffline.value = it
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

    fun getAdvanceDetailsByVendor(vendorCode: String) = viewModelScope.launch(dispatchers.main) {
        _advanceLineItemsDetail.removeSource(advanceLineItemsDetailsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            advanceLineItemsDetailsSource = useCase.getAdvanceLineDetails(vendorCode)
        }
        _advanceLineItemsDetail.addSource(advanceLineItemsDetailsSource) {
            _advanceLineItemsDetail.value = it
        }
    }

    fun saveInvoiceDetails(invoice: VegaNicaraguaInvoiceDetails) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveInvoiceDetails(invoice)
        }
    }

    fun getInvoiceOfflineData() = viewModelScope.launch(dispatchers.main) {
        _invoiceOffline.removeSource(invoiceOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            invoiceOfflineSource = useCase.getInvoiceOfflineData()
        }
        _invoiceOffline.addSource(invoiceOfflineSource) {
            _invoiceOffline.value = it
        }
    }

    fun deleteInvoiceItem(tmpWbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteInvoiceItem(tmpWbId)
        }
    }

    fun getAdvanceDetailsByVendorOffline(vendorCode: String) = viewModelScope.launch(dispatchers.main) {
        _advanceLineItemsOfflineDetail.removeSource(advanceLineItemsDetailsOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            advanceLineItemsDetailsOfflineSource = useCase.getAdvanceLineDetailsOffline(vendorCode)
        }
        _advanceLineItemsOfflineDetail.addSource(advanceLineItemsDetailsOfflineSource) {
            _advanceLineItemsOfflineDetail.value = it
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

    fun saveAdvanceLineItem(advanceLineItem: ArrayList<VegaNicaraguaAdvanceLineItemGrn>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveAdvanceLineItem(advanceLineItem)
        }
    }

    fun getGrnInventoryDetailsByVendor(lotId: String,materialCode:String) = viewModelScope.launch(dispatchers.main) {
        _grnInventoryDetails.removeSource(grnInventoryDetailsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            grnInventoryDetailsSource = useCase.getGrnInventoryDetails(lotId,materialCode)
        }
        _grnInventoryDetails.addSource(grnInventoryDetailsSource) {
            _grnInventoryDetails.value = it
        }
    }

    fun getGrnInventoryDetailsOfflineByVendor(lotId: String, materialCode: String) =
        viewModelScope.launch(dispatchers.main) {
            _grnInventoryDetailsOffline.removeSource(grnInventoryDetailsOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                grnInventoryDetailsOfflineSource = useCase.getGrnInventoryDetailsOffline(lotId, materialCode)
            }
            _grnInventoryDetailsOffline.addSource(grnInventoryDetailsOfflineSource) {
                _grnInventoryDetailsOffline.value = it
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

    fun getQualityGradeDesc(qualityGrade: String) = viewModelScope.launch(dispatchers.main) {
        _qualityGradeDesc.removeSource(qualityGradeDescSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityGradeDescSource = useCase.getQualityDesc(qualityGrade)
        }
        _qualityGradeDesc.addSource(qualityGradeDescSource) {
            _qualityGradeDesc.value = it
        }
    }

    fun getQualityGradesListWithDesc() = viewModelScope.launch(dispatchers.main) {
        _qualityGradeDescList.removeSource(qualityGradeDescListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityGradeDescListSource = useCase.getQualityGradesListWithDesc()
        }
        _qualityGradeDescList.addSource(qualityGradeDescListSource) {
            _qualityGradeDescList.value = it
        }
    }

    fun postCreateInvoice(
        receivingData:VegaNicaraguaInvoicePostRequest
    ) = viewModelScope.launch(dispatchers.main) {
        _createInvoice.removeSource(createInvoiceSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            createInvoiceSource = useCase.postInvoiceDetails(receivingData)
        }
        _createInvoice.addSource(createInvoiceSource) {
            _createInvoice.value = it
        }
    }

    fun removeAdvanceLineItem(documentNumber: String?, tempId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.removeAdvanceLineItem(documentNumber, tempId)
        }
    }

}
