package com.olam.warehouse.vegax.reconcilnicaragua.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.GrnDetailsResponse
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.reconcilnicaragua.data.domain.usecase.VegaNicaraguaReconcilReportUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class VegaNicaraguaReconcilReportViewModel(
    private val useCase: VegaNicaraguaReconcilReportUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    var bitmapPrintKeys = ArrayList<String>()

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val supplier: LiveData<List<VegaVendor>> get() = _supplier

    var reportItem = VegaNicaraguaReconcilCashMovement()

    private var grnDetailsSource: LiveData<Resource<GenericReqAndResp<List<GrnDetailsResponse>>>> =
        MutableLiveData()
    private val _grnDetails = MediatorLiveData<Resource<GenericReqAndResp<List<GrnDetailsResponse>>>>()
    val grnDetails: LiveData<Resource<GenericReqAndResp<List<GrnDetailsResponse>>>> get() = _grnDetails

    private var grnTransListSource: LiveData<List<VegaReceiving>> = MutableLiveData()
    private val _grnTransList = MediatorLiveData<List<VegaReceiving>>()
    val grnTransList: LiveData<List<VegaReceiving>> get() = _grnTransList

    private var invoiceOfflineSource: LiveData<List<VegaNicaraguaInvoiceDetails>> =
        MutableLiveData()
    private val _invoiceOffline = MediatorLiveData<List<VegaNicaraguaInvoiceDetails>>()
    val invoiceOffline: LiveData<List<VegaNicaraguaInvoiceDetails>> get() = _invoiceOffline


    private var advanceLineItemsDetailsSource: LiveData<Resource<GenericReqAndResp<List<VegaNicaraguaAdvanceLineItems>>>> =
        MutableLiveData()
    private val _advanceLineItemsDetail =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaNicaraguaAdvanceLineItems>>>>()
    val advanceLineItemsDetails: LiveData<Resource<GenericReqAndResp<List<VegaNicaraguaAdvanceLineItems>>>> get() = _advanceLineItemsDetail

    private var reconReportSource: LiveData<VegaNicaraguaReconcilCashMovement> =
        MutableLiveData()
    private val _reconReport = MediatorLiveData<VegaNicaraguaReconcilCashMovement>()
    val reconReport: LiveData<VegaNicaraguaReconcilCashMovement> get() = _reconReport

    private var qualityGradeDescListSource: LiveData<List<QualitativeParams>> = MutableLiveData()
    private val _qualityGradeDescList = MediatorLiveData<List<QualitativeParams>>()
    val getQualityGradeDescList: LiveData<List<QualitativeParams>> get() = _qualityGradeDescList

    private var advanceSource: LiveData<List<VegaNicaraguaAdvanceTransactionDetails>> =
        MutableLiveData()
    private val _advance = MediatorLiveData<List<VegaNicaraguaAdvanceTransactionDetails>>()
    val advance: LiveData<List<VegaNicaraguaAdvanceTransactionDetails>> get() = _advance


    fun getReceivingWithLineItem(startTime: Long, endTime: Long) = viewModelScope.launch(dispatchers.main) {
        _grnTransList.removeSource(grnTransListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            grnTransListSource = useCase.getReceivingWithLineItem(startTime, endTime)
        }
        _grnTransList.addSource(grnTransListSource) {
            _grnTransList.value = it
        }
    }


    fun getGrnDetailsByDate(vendorCode: String) = viewModelScope.launch(dispatchers.main) {
        _grnDetails.removeSource(grnDetailsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            grnDetailsSource = useCase.getGrnDetails(vendorCode)
        }
        _grnDetails.addSource(grnDetailsSource) {
            _grnDetails.value = it
        }
    }

    fun getInvoiceOfflineData(startTime: Long, endTime: Long) = viewModelScope.launch(dispatchers.main) {
        _invoiceOffline.removeSource(invoiceOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            invoiceOfflineSource = useCase.getInvoiceOfflineData(startTime, endTime)
        }
        _invoiceOffline.addSource(invoiceOfflineSource) {
            _invoiceOffline.value = it
        }
    }

    fun insertOrUpdateReportData() = viewModelScope.launch(dispatchers.io) {
        withContext(dispatchers.io) {
            useCase.insertOrUpdatereport(reportItem)
        }
    }


    fun getAdvanceDetailsByVendor(grnId: String) = viewModelScope.launch(dispatchers.main) {
        _advanceLineItemsDetail.removeSource(advanceLineItemsDetailsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            advanceLineItemsDetailsSource = useCase.getAdvanceLineDetails(grnId)
        }
        _advanceLineItemsDetail.addSource(advanceLineItemsDetailsSource) {
            _advanceLineItemsDetail.value = it
        }
    }

    fun getReconReport() = viewModelScope.launch(dispatchers.main) {
        _reconReport.removeSource(reconReportSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            reconReportSource = useCase.getReconReport()
        }
        _reconReport.addSource(reconReportSource) {
            _reconReport.value = it
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

    fun getTransactionAdvanceData(startTime: Long, endTime: Long) = viewModelScope.launch(dispatchers.main) {
        _advance.removeSource(advanceSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            advanceSource = useCase.getTransactionAdvanceData(startTime, endTime)
        }
        _advance.addSource(advanceSource) {
            _advance.value = it
        }
    }
}
