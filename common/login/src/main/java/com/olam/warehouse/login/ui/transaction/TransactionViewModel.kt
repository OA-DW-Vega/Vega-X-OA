package com.olam.warehouse.login.ui.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.*
import com.olam.warehouse.master.vegacocoa.model.VegaCoCoaReceivingMtnrWithLots
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaFgrnItemWithGrades
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaRminItemWithLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchWithLineItems
import com.olam.warehouse.master.vegaindocoffee.entity.VegaIndoCoffeeExportSalesOrder
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceTransactionDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPODetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaInvoiceDetails
import com.olam.warehouse.master.veganicaragua.model.VegaMtntWithLotsWithBags
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 5/6/2020.
 */
class TransactionViewModel(private val useCase: MasterUseCase, private val dispatchers: AppDispatchers) :
    BaseViewModel() {

    private var receivingSource: LiveData<List<VegaReceiving>> = mutableLiveDataOf(emptyList())
    val receiving: LiveData<List<VegaReceiving>> get() = _receiving
    private val _receiving = MediatorLiveData<List<VegaReceiving>>()

    private var receiveWithLineItemLocalSource: LiveData<List<VegaReceivingWithLineItems>> =
        MutableLiveData()
    private val _receiveWithLineItemLocal = MediatorLiveData<List<VegaReceivingWithLineItems>>()
    val receiveWithLineItemLocal: LiveData<List<VegaReceivingWithLineItems>> get() = _receiveWithLineItemLocal

    private var weighBridgeSource: LiveData<List<VegaQualityWBDetails>> = MutableLiveData()
    private val _weighBridge = MediatorLiveData<List<VegaQualityWBDetails>>()
    val weighBridge: LiveData<List<VegaQualityWBDetails>> get() = _weighBridge

    private var DOweighBridgeSourceoffline: LiveData<List<DOQualityWBDetails>> = MutableLiveData()
    private val _DOweighBridgeoffline = MediatorLiveData<List<DOQualityWBDetails>>()
    val DOweighBridgeoffline: LiveData<List<DOQualityWBDetails>> get() = _DOweighBridgeoffline

    private var mtntWithLineItemSource: LiveData<List<VegaMtntWithLineItems>> = MutableLiveData()
    private val _mtntWithLineItem = MediatorLiveData<List<VegaMtntWithLineItems>>()
    val mtntWithLineItem: LiveData<List<VegaMtntWithLineItems>> get() = _mtntWithLineItem

    private var dispatchLotsSource: LiveData<List<VegaDispatchWithLots>> = MutableLiveData()
    private val _dispatchLots = MediatorLiveData<List<VegaDispatchWithLots>>()
    val dispatchLots: LiveData<List<VegaDispatchWithLots>> get() = _dispatchLots

    private var rminSource: LiveData<List<VegaRminBomWithLots>> = MutableLiveData()
    private val _rminLots = MediatorLiveData<List<VegaRminBomWithLots>>()
    val rminLots: LiveData<List<VegaRminBomWithLots>> get() = _rminLots

    private var materialSource: LiveData<List<VegaPackageMaterial>> = MutableLiveData()
    val material: LiveData<List<VegaPackageMaterial>> get() = _material
    private val _material = MediatorLiveData<List<VegaPackageMaterial>>()

    private var grnTransListSource: LiveData<List<VegaReceiving>> = MutableLiveData()
    private val _grnTransList = MediatorLiveData<List<VegaReceiving>>()
    val grnTransList: LiveData<List<VegaReceiving>> get() = _grnTransList

    private var offlinePendingSource: LiveData<List<VegaCoCoaReceivingMtnrWithLots>> = mutableLiveDataOf()
    private val _offlinePendingMtnr = MediatorLiveData<List<VegaCoCoaReceivingMtnrWithLots>>()
    val offlinePendingMtnr: LiveData<List<VegaCoCoaReceivingMtnrWithLots>> get() = _offlinePendingMtnr

    private var pendingWithLotSource: LiveData<List<VegaCocoaNoWeighmentWithLots>> = MutableLiveData()
    private val _pendingWithLot = MediatorLiveData<List<VegaCocoaNoWeighmentWithLots>>()
    val dispatchPendingWithLot: LiveData<List<VegaCocoaNoWeighmentWithLots>> get() = _pendingWithLot

    private var invoiceOfflineSource: LiveData<List<VegaNicaraguaInvoiceDetails>> =
        MutableLiveData()
    private val _invoiceOffline = MediatorLiveData<List<VegaNicaraguaInvoiceDetails>>()
    val invoiceOffline: LiveData<List<VegaNicaraguaInvoiceDetails>> get() = _invoiceOffline

    private var forwardPoOfflineSource: LiveData<List<VegaNicaraguaForwardPODetails>> =
        MutableLiveData()
    private val _forwardPoOffline = MediatorLiveData<List<VegaNicaraguaForwardPODetails>>()
    val forwardPoOffline: LiveData<List<VegaNicaraguaForwardPODetails>> get() = _forwardPoOffline

    private var advanceTransactionOfflineSource: LiveData<List<VegaNicaraguaAdvanceTransactionDetails>> =
        MutableLiveData()
    private val _advanceTransaction = MediatorLiveData<List<VegaNicaraguaAdvanceTransactionDetails>>()
    val advanceTransactionOffline: LiveData<List<VegaNicaraguaAdvanceTransactionDetails>> get() = _advanceTransaction


    private var lastSyncSource: LiveData<VegaLastSyncTime> = MutableLiveData()
    private val _lastSync = MediatorLiveData<VegaLastSyncTime>()
    val lastSync: LiveData<VegaLastSyncTime> get() = _lastSync

    private var listWBLotsWithBagsSource: LiveData<List<VegaMtntWithLotsWithBags>> = mutableLiveDataOf()
    private val _listWBLotsWithBags = MediatorLiveData<List<VegaMtntWithLotsWithBags>>()
    val listWBLotsWithBags: LiveData<List<VegaMtntWithLotsWithBags>> get() = _listWBLotsWithBags

    private var offlodingListSource: LiveData<List<VegaCoffeeReceiving>> = MutableLiveData()
    private val _offlodingList = MediatorLiveData<List<VegaCoffeeReceiving>>()
    val offlodingList: LiveData<List<VegaCoffeeReceiving>> get() = _offlodingList

    private var qualityListSource: LiveData<List<VegaQualityWBDetails>> = MutableLiveData()
    private val _qualityList = MediatorLiveData<List<VegaQualityWBDetails>>()
    val qualityList: LiveData<List<VegaQualityWBDetails>> get() = _qualityList

    private var weighBridgeOfflineCountSource: LiveData<List<VegaGrnWeighBridgeId>> =
        MutableLiveData()
    private val _weighBridgeOfflineCount = MediatorLiveData<List<VegaGrnWeighBridgeId>>()
    val weighBridgeOfflineCount: LiveData<List<VegaGrnWeighBridgeId>> get() = _weighBridgeOfflineCount

    private var dispatchCountSource: LiveData<List<VegaEcuadorDispatchWithLineItems>> =
        MutableLiveData()
    private val _dispatchItemCountLocal = MediatorLiveData<List<VegaEcuadorDispatchWithLineItems>>()
    val dispatchItemCountLocal: LiveData<List<VegaEcuadorDispatchWithLineItems>> get() = _dispatchItemCountLocal

    private var exportSalesSource: LiveData<List<VegaIndoCoffeeExportSalesOrder>> =
        MutableLiveData()
    private val _exportSalesLocal = MediatorLiveData<List<VegaIndoCoffeeExportSalesOrder>>()
    val exportSalesLocal: LiveData<List<VegaIndoCoffeeExportSalesOrder>> get() = _exportSalesLocal

    private var weighBridgeOnlineSource: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> =
        MutableLiveData()
    private val _weighBridgeOnline =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>()
    val weighBridgeOnline: LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> get() = _weighBridgeOnline

    private var DOweighBridgeOnlineSource: LiveData<Resource<List<DOQualityWBDetails>>> =
        MutableLiveData()
    val DOweighBridgeOnline: LiveData<Resource<List<DOQualityWBDetails>>> get() = _DOweighBridgeOnline
    private val _DOweighBridgeOnline = MediatorLiveData<Resource<List<DOQualityWBDetails>>>()

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems


    fun getIndoExportSalesItem() = viewModelScope.launch(dispatchers.main) {
        _exportSalesLocal.removeSource(exportSalesSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            exportSalesSource = useCase.getIndoExportSalesItem()
        }
        _exportSalesLocal.addSource(exportSalesSource) {
            _exportSalesLocal.value = it
        }
    }

    fun getDispatchWithLineItemCount() = viewModelScope.launch(dispatchers.main) {
        _dispatchItemCountLocal.removeSource(dispatchCountSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchCountSource = useCase.getDispatchWithLineItemCount()
        }
        _dispatchItemCountLocal.addSource(dispatchCountSource) {
            _dispatchItemCountLocal.value = it
        }
    }

    fun getOfflineWeighBridgeDetailCount() = viewModelScope.launch(dispatchers.main) {
        _weighBridgeOfflineCount.removeSource(weighBridgeOfflineCountSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeOfflineCountSource = useCase.getOfflineWeighBridgeDetailCount()
        }
        _weighBridgeOfflineCount.addSource(weighBridgeOfflineCountSource) {
            _weighBridgeOfflineCount.value = it
        }
    }

    fun getQualityItem() = viewModelScope.launch(dispatchers.main) {
        _qualityList.removeSource(qualityListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityListSource = useCase.getQualityItem()
        }
        _qualityList.addSource(qualityListSource) {
            _qualityList.value = it
        }
    }

    fun getOffloadingItem() = viewModelScope.launch(dispatchers.main) {
        _offlodingList.removeSource(offlodingListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            offlodingListSource = useCase.getOffloadingItem()
        }
        _offlodingList.addSource(offlodingListSource) {
            _offlodingList.value = it
        }
    }

    fun getListOfMtntWithLots() = viewModelScope.launch(dispatchers.main) {
        _listWBLotsWithBags.removeSource(listWBLotsWithBagsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            listWBLotsWithBagsSource = useCase.getListOfMtntWithLots()
        }
        _listWBLotsWithBags.addSource(listWBLotsWithBagsSource) {
            _listWBLotsWithBags.value = it
        }
    }

    fun getLastSyncTime() = viewModelScope.launch(dispatchers.main) {
        _lastSync.removeSource(lastSyncSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            lastSyncSource = useCase.getLastSyncTime()
        }
        _lastSync.addSource(lastSyncSource) {
            _lastSync.value = it
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

    fun getReceivingItem() = viewModelScope.launch(dispatchers.main) {
        _receiving.removeSource(receivingSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            receivingSource = useCase.getReceivingItem()
        }
        _receiving.addSource(receivingSource) {
            _receiving.value = it
        }
    }

    fun getReceivingWithLineItem() = viewModelScope.launch(dispatchers.main) {
        _receiveWithLineItemLocal.removeSource(receiveWithLineItemLocalSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            receiveWithLineItemLocalSource = useCase.getReceivingWithLineItem()
        }
        _receiveWithLineItemLocal.addSource(receiveWithLineItemLocalSource) {
            _receiveWithLineItemLocal.value = it
        }
    }

    fun deleteWeighBride(tmpWbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteWeighBride(tmpWbId)
        }
    }

    fun getWeighBridgeDetail() = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weighBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeSource = useCase.getWeighBridgeDetail()
        }
        _weighBridge.addSource(weighBridgeSource) {
            _weighBridge.value = it
        }
    }

    fun deleteQualityWeighBride(tmpWbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteQualityWeighBride(tmpWbId)
        }
    }

    fun getMtntWithLineItem() = viewModelScope.launch(dispatchers.main) {
        _mtntWithLineItem.removeSource(mtntWithLineItemSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            mtntWithLineItemSource = useCase.getMtntWithLineItem()
        }
        _mtntWithLineItem.addSource(mtntWithLineItemSource) {
            _mtntWithLineItem.value = it
        }
    }

    fun deleteMtnt(tmpWbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteMtnt(tmpWbId)
        }
    }

    fun getDispatchWithLots() = viewModelScope.launch(dispatchers.main) {
        _dispatchLots.removeSource(dispatchLotsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchLotsSource = useCase.getDispatchWithLots()
        }
        _dispatchLots.addSource(dispatchLotsSource) {
            _dispatchLots.value = it
        }
    }

    fun deleteDispatchItem(tmpWbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteDispatchItem(tmpWbId)
        }
    }

    fun getRminWithLots() = viewModelScope.launch(dispatchers.main) {
        _rminLots.removeSource(rminSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            rminSource = useCase.getRminWithLots()
        }
        _rminLots.addSource(rminSource) {
            _rminLots.value = it
        }
    }

    fun deleteRmin(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteRmin(batchNumber)
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


    //Cocoa Transaction

    private var mtntLotsSource: LiveData<List<VegaCocoaMtntWithLots>> = MutableLiveData()
    private val _mtntLots = MediatorLiveData<List<VegaCocoaMtntWithLots>>()
    val mtntLots: LiveData<List<VegaCocoaMtntWithLots>> get() = _mtntLots

    private var fgrnItemSource: LiveData<List<VegaCocoaFgrnItemWithGrades>> = MutableLiveData()
    private val _fgrnItem = MediatorLiveData<List<VegaCocoaFgrnItemWithGrades>>()
    val fgrnItem: LiveData<List<VegaCocoaFgrnItemWithGrades>> get() = _fgrnItem

    private var rminItemSource: LiveData<List<VegaCocoaRminItemWithLots>> = MutableLiveData()
    private val _rminItem = MediatorLiveData<List<VegaCocoaRminItemWithLots>>()
    val rminItem: LiveData<List<VegaCocoaRminItemWithLots>> get() = _rminItem

    fun getMtntWithLots() = viewModelScope.launch(dispatchers.main) {
        _mtntLots.removeSource(mtntLotsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            mtntLotsSource = useCase.getMtntWithLots()
        }
        _mtntLots.addSource(mtntLotsSource) {
            _mtntLots.value = it
        }
    }

    fun deleteMtntItem(tmpWbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteMtntItem(tmpWbId)
        }
    }

    fun getFgrnItems() = viewModelScope.launch(dispatchers.main) {
        _fgrnItem.removeSource(fgrnItemSource)
        withContext(dispatchers.io) {
            fgrnItemSource = useCase.getFgrnItems()
        }
        _fgrnItem.addSource(fgrnItemSource) {
            _fgrnItem.value = it
        }
    }

    fun deleteFgrnItem(fgrnId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteFgrnItem(fgrnId)
        }
    }

    fun getRminItems() = viewModelScope.launch(dispatchers.main) {
        _rminItem.removeSource(rminItemSource)
        withContext(dispatchers.io) {
            rminItemSource = useCase.getRminItems()
        }
        _rminItem.addSource(rminItemSource) {
            _rminItem.value = it
        }
    }

    fun deleteRminItem(rminId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteRminItem(rminId)
        }
    }

    fun getGrnItem() = viewModelScope.launch(dispatchers.main) {
        _grnTransList.removeSource(grnTransListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            grnTransListSource = useCase.getGrnItem()
        }
        _grnTransList.addSource(grnTransListSource) {
            _grnTransList.value = it
        }
    }

    fun getPendingList() = viewModelScope.launch(dispatchers.main) {
        _offlinePendingMtnr.removeSource(offlinePendingSource)
        withContext(dispatchers.io) {
            offlinePendingSource = useCase.getPendingItem()
        }
        _offlinePendingMtnr.addSource(offlinePendingSource) {
            _offlinePendingMtnr.value = it
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

    fun getPendingListWithLot() = viewModelScope.launch(dispatchers.main) {
        _pendingWithLot.removeSource(pendingWithLotSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            pendingWithLotSource = useCase.getPendingListWithLot()
        }
        _pendingWithLot.addSource(pendingWithLotSource) {
            _pendingWithLot.value = it
        }
    }

    fun getForwardPOOfflineData() = viewModelScope.launch(dispatchers.main) {
        _forwardPoOffline.removeSource(forwardPoOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            forwardPoOfflineSource = useCase.getForwardOfflineData()
        }
        _forwardPoOffline.addSource(forwardPoOfflineSource) {
            _forwardPoOffline.value = it
        }
    }

    fun getAdvanceTransactionOfflineData() = viewModelScope.launch(dispatchers.main) {
        _advanceTransaction.removeSource(advanceTransactionOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            advanceTransactionOfflineSource = useCase.getAdvanceTransaction()
        }
        _advanceTransaction.addSource(advanceTransactionOfflineSource) {
            _advanceTransaction.value = it
        }
    }

    fun getWeighBridgeDetailqualitycount() = viewModelScope.launch(dispatchers.main) {
        _weighBridge.removeSource(weighBridgeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeSource = useCase.getWeighBridgeDetailqualitycount()
        }
        _weighBridge.addSource(weighBridgeSource) {
            _weighBridge.value = it
        }
    }

    fun getWeighBridgeDataOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        getWeighBridgeDetailOnline()
        return weighBridgeOnline
    }

    fun getDOWeighBridgeDataOnline(): LiveData<Resource<List<DOQualityWBDetails>>> {
        getWeighBridgeDetailOnline()
        return DOweighBridgeOnline
    }

    private fun getWeighBridgeDetailOnline() = viewModelScope.launch(dispatchers.main) {
        _weighBridgeOnline.removeSource(weighBridgeOnlineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            weighBridgeOnlineSource = useCase.getWeighBridgeDetailOnline()
        }
        _weighBridgeOnline.addSource(weighBridgeOnlineSource) {
            _weighBridgeOnline.value = it
        }
    }

    fun getDOWeighBridgeDetailOnline() = viewModelScope.launch(dispatchers.main) {
        _DOweighBridgeOnline.removeSource(DOweighBridgeOnlineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            DOweighBridgeOnlineSource = useCase.DOgetWeighBridgeDetailOnline()
        }
        _DOweighBridgeOnline.addSource(DOweighBridgeOnlineSource) {
            _DOweighBridgeOnline.value = it
        }
    }

    fun getDOWeighBridgeDataoffline(): LiveData<List<DOQualityWBDetails>> {
        getDOWeighBridgeDetailoffline()
        return DOweighBridgeoffline
    }

    private fun getDOWeighBridgeDetailoffline() = viewModelScope.launch(dispatchers.main) {
        _DOweighBridgeoffline.removeSource(DOweighBridgeSourceoffline) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            DOweighBridgeSourceoffline = useCase.getDOWeighBridgeDetailoffline()
        }
        _DOweighBridgeoffline.addSource(DOweighBridgeSourceoffline) {
            _DOweighBridgeoffline.value = it
        }
    }

}
