package com.olam.warehouse.odreceiving.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.dorigin.entity.*
import com.olam.warehouse.master.dorigin.model.DOReceivingMtnWrapper
import com.olam.warehouse.master.dorigin.model.DOReceivingWarehouseWithMtns
import com.olam.warehouse.master.dorigin.model.DOReceivingWithLineItems
import com.olam.warehouse.odreceiving.data.domain.model.DODispatchDetailPost
import com.olam.warehouse.odreceiving.data.domain.model.DOReceivingPost
import com.olam.warehouse.odreceiving.data.domain.model.DOReceivingResponse
import com.olam.warehouse.odreceiving.data.domain.model.DispatchDetailsResponse
import com.olam.warehouse.odreceiving.data.domain.usecase.DOReceivingUseCase
import com.olam.warehouse.odreceiving.utils.getLineItemFromReceiving
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
class DOReceivingViewModel(private val useCase: DOReceivingUseCase, private val dispatchers: AppDispatchers) :
    BaseViewModel() {

    private var receiveSource: LiveData<Resource<GenericReqAndResp<DOReceivingResponse>>> = MutableLiveData()
    private var receiveLocalSource: LiveData<List<DOReceiving>> = MutableLiveData()
    private var receiveWithLineItemLocalSource: LiveData<List<DOReceivingWithLineItems>> = MutableLiveData()
    private var productSource: LiveData<List<DOMaterial>> = mutableLiveDataOf(emptyList())
    private var supplierSource: LiveData<List<DOVendor>> = MutableLiveData()
    private var locationSource: LiveData<List<DOStorageLocation>> = MutableLiveData()
    private var materialSource: LiveData<List<DOPackageMaterial>> = MutableLiveData()
    private var sapMaterialSource: LiveData<List<DOMaterial>> = MutableLiveData()
    /*private var warehouseSource: LiveData<Resource<GenericReqAndResp<DOReceivingMtnWrapper>>> =
        MutableLiveData()*/
    private var warehouseSourceLocal: LiveData<List<DOReceivingWarehouse>> = MutableLiveData()
    private var warehouseWithMtnsSource: LiveData<DOReceivingWarehouseWithMtns> = MutableLiveData()

    private val _receive = MediatorLiveData<Resource<GenericReqAndResp<DOReceivingResponse>>>()
    private val _receiveLocal = MediatorLiveData<List<DOReceiving>>()
    private val _receiveWithLineItemLocal = MediatorLiveData<List<DOReceivingWithLineItems>>()
    private val _product = MediatorLiveData<List<DOMaterial>>()
    private val _supplier = MediatorLiveData<List<DOVendor>>()
    private val _location = MediatorLiveData<List<DOStorageLocation>>()
    private val _material = MediatorLiveData<List<DOPackageMaterial>>()
    private val _sapMaterial = MediatorLiveData<List<DOMaterial>>()

    private var dispatchDetailSource: LiveData<Resource<GenericReqAndResp<DispatchDetailsResponse>>> = MutableLiveData()
    private val _dispatchDetail = MediatorLiveData<Resource<GenericReqAndResp<DispatchDetailsResponse>>>()
    val dispatchDetail: LiveData<Resource<GenericReqAndResp<DispatchDetailsResponse>>> get() = _dispatchDetail

    //private val _warehouse = MediatorLiveData<Resource<GenericReqAndResp<DOReceivingMtnWrapper>>>()
    private val _warehouseLocal = MediatorLiveData<List<DOReceivingWarehouse>>()
    private val _warehouseWithMtns = MediatorLiveData<DOReceivingWarehouseWithMtns>()

    val receive: LiveData<Resource<GenericReqAndResp<DOReceivingResponse>>> get() = _receive
    val receiveLocal: LiveData<List<DOReceiving>> get() = _receiveLocal
    val receiveWithLineItemLocal: LiveData<List<DOReceivingWithLineItems>> get() = _receiveWithLineItemLocal
    val product: LiveData<List<DOMaterial>> get() = _product
    val suppplier: LiveData<List<DOVendor>> get() = _supplier
    val location: LiveData<List<DOStorageLocation>> get() = _location
    val material: LiveData<List<DOPackageMaterial>> get() = _material
    val sapMaterial: LiveData<List<DOMaterial>> get() = _sapMaterial
    //val warehouse: LiveData<Resource<GenericReqAndResp<DOReceivingMtnWrapper>>> get() = _warehouse
    val warehouseLocal: LiveData<List<DOReceivingWarehouse>> get() = _warehouseLocal
    val warehouseWithMtns: LiveData<DOReceivingWarehouseWithMtns> get() = _warehouseWithMtns

    private var customLocationSource: LiveData<List<DOCustomStLocation>> = MutableLiveData()
    private val _customLocation = MediatorLiveData<List<DOCustomStLocation>>()
    val customLocation: LiveData<List<DOCustomStLocation>> get() = _customLocation


    fun getProducts() = viewModelScope.launch(dispatchers.main) {
        _product.removeSource(productSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            productSource = useCase.getProducts()
        }
        _product.addSource(productSource) {
            _product.value = it
        }
    }

    fun getSAPMaterials() = viewModelScope.launch(dispatchers.main) {
        _sapMaterial.removeSource(sapMaterialSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            sapMaterialSource = useCase.getSAPMaterials()
        }
        _sapMaterial.addSource(sapMaterialSource) {
            _sapMaterial.value = it
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

    fun getLocations() = viewModelScope.launch(dispatchers.main) {
        _location.removeSource(locationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            locationSource = useCase.getLocations()
        }
        _location.addSource(locationSource) {
            _location.value = it
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

    fun postReceivingData(receivingData: DOReceivingPost) = viewModelScope.launch(dispatchers.main) {
        _receive.removeSource(receiveSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            receiveSource = useCase.postReceivingDetail(receivingData)
        }
        _receive.addSource(receiveSource) {
            _receive.value = it
        }
    }

    fun postReceivingMtnData(receivingData: DOReceivingPost) = viewModelScope.launch(dispatchers.main) {
        _receive.removeSource(receiveSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            receiveSource = useCase.postReceivingMtnDetail(receivingData)
        }
        _receive.addSource(receiveSource) {
            _receive.value = it
        }
    }

    fun saveReceiving(receivingData: DOReceiving) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveReceiving(receivingData)
        }
    }

    fun saveTransactionDetail(transactionDetail: DOTransactionDetail) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveTransactionDetail(transactionDetail)
        }
    }

    fun saveBagDetail(bag: DOBag) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveBagDetail(bag)
        }
    }

    fun getReceiving() = viewModelScope.launch(dispatchers.main) {
        _receiveLocal.removeSource(receiveLocalSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            receiveLocalSource = useCase.getReceiving()
        }
        _receiveLocal.addSource(receiveLocalSource) {
            _receiveLocal.value = it
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


    fun getWarehouses() = viewModelScope.launch(dispatchers.main) {
        _warehouseLocal.removeSource(warehouseSourceLocal) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            warehouseSourceLocal = useCase.getWarehouses()
        }
        _warehouseLocal.addSource(warehouseSourceLocal) {
            _warehouseLocal.value = it
        }
    }

    fun getWarehousesWithMtns(whID: String) = viewModelScope.launch(dispatchers.main) {
        _warehouseWithMtns.removeSource(warehouseWithMtnsSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            warehouseWithMtnsSource = useCase.getWarehousesWithMtns(whID)
        }
        _warehouseWithMtns.addSource(warehouseWithMtnsSource) {
            _warehouseWithMtns.value = it
        }
    }

    /*fun fetchWarehouseWithMtns() = viewModelScope.launch(dispatchers.main) {
        _warehouse.removeSource(warehouseSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            warehouseSource = useCase.fetchWarehouseWithMtns()
        }
        _warehouse.addSource(warehouseSource) {
            _warehouse.value = it
        }
    }*/

    fun saveWarehouseWithMtns(it: DOReceivingMtnWrapper) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.saveWarehouseWithMtns(it)
        }
    }

    fun getDispatchDetailsOffline() = viewModelScope.launch {
        _dispatchOffline.removeSource(dispatchOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchOfflineSource = useCase.getDispatchDetailsOffline()
        }
        _dispatchOffline.addSource(dispatchOfflineSource) {
            _dispatchOffline.value = it
        }
    }

    fun getDispatchDetails(key: String, dispatchDetailPost: DODispatchDetailPost) = viewModelScope.launch(dispatchers.main)
    {
        _dispatchDetail.removeSource(dispatchDetailSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            dispatchDetailSource = useCase.getDispatchDetails(key, dispatchDetailPost)
        }
        _dispatchDetail.addSource(dispatchDetailSource) {
            _dispatchDetail.value = it
        }
    }

    fun getTareWeightLineItem(postData: List<DOReceivingLineItem>) : Double {
        var result = 0.0
        postData.forEach {
            result = it.bagWeight!!
        }
        return result
    }
    fun getTareWeight(postData: List<DOReceiving>) = postData.sumByDouble { it.tareWeight?.toDouble() ?: 0.0 }
    fun getGrossWeight(postData: List<DOReceiving>) = postData.sumByDouble { it.grossWeight }
    fun getGrossWeightOffline(postDataOffline: List<DOReceivingLineItem>) = postDataOffline.sumByDouble { it.grossWeight }
    fun getNetWeight(postData: List<DOReceiving>) = postData.sumByDouble { it.netWeight }
    fun getNetWeightOffline(postDataOffline: List<DOReceivingLineItem>) = postDataOffline.sumByDouble { it.netWeight }
    fun getBagCount(postData: List<DOReceiving>) = postData.sumBy { it.bagCount?.toInt() ?: 0 }
    fun getBagCountOffline(postDataOffline: List<DOReceivingLineItem>) = postDataOffline.sumBy { it.bagCount?.toInt() ?: 0 }
    fun saveReceivingLineItems(postData: MutableList<DOReceiving>) = viewModelScope.launch(dispatchers.main) {
        val lineItems = getLineItemFromReceiving(postData)
        withContext(dispatchers.io) {
            useCase.saveReceivingLineItems(lineItems)
        }
    }

    fun updateDeletedItem(Wbid: String?, txnId: String?) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            Wbid?.let { useCase.updateDeletedItem(it, txnId) }
        }
    }

    fun getTareWeightTemp(postData: List<DOReceiving>) =
        postData.sumByBigDecimal { it.tareWeight?.toDouble() ?: 0.0 }

    fun getGrossWeightTemp(postData: List<DOReceiving>) =
        postData.sumByBigDecimal { it.grossWeight }

    fun getNetWeightTemp(postData: List<DOReceiving>) = postData.sumByBigDecimal { it.netWeight }


    inline fun <T> Iterable<T>.sumByBigDecimal(selector: (T) -> Double): BigDecimal {

        var b = BigDecimal("0.0")
        for (element in this) {
            b += BigDecimal(selector(element))
        }
        return b
    }

    fun updateReceivingFailMsg(msg: String, tmpWbId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateReceivingFailMsg(msg, tmpWbId)
        }
    }

//    private var transactionSource: LiveData<Resource<GenericReqAndResp<DOTransactionDetail>>> = MutableLiveData()
    private var transactionSource: LiveData<Resource<GenericReqAndResp<DOTxnDetail>>> = MutableLiveData()
//    private val _transaction = MediatorLiveData<Resource<GenericReqAndResp<DOTransactionDetail>>>()
    private val _transaction = MediatorLiveData<Resource<GenericReqAndResp<DOTxnDetail>>>()
//    val transaction: LiveData<Resource<GenericReqAndResp<DOTransactionDetail>>> get() = _transaction
    val transaction: LiveData<Resource<GenericReqAndResp<DOTxnDetail>>> get() = _transaction

    private var dispatchOfflineSource: LiveData<List<DispatchDetail>> = MutableLiveData()
    private val _dispatchOffline = MediatorLiveData<List<DispatchDetail>>()
    val dispatchOffline: LiveData<List<DispatchDetail>> get() = _dispatchOffline

    private var transactionOfflineSource: LiveData<DOTransactionDetail> = MutableLiveData()
    private val _transactionOffline = MediatorLiveData<DOTransactionDetail>()
    val transactionOffline: LiveData<DOTransactionDetail> get() = _transactionOffline

    private var transactionListSource: LiveData<Resource<GenericReqAndResp<List<DOTransactionDetail>>>> = MutableLiveData()
    private val _transactions = MediatorLiveData<Resource<GenericReqAndResp<List<DOTransactionDetail>>>>()
    val transactions: LiveData<Resource<GenericReqAndResp<List<DOTransactionDetail>>>> get() = _transactions

    private var transactionListOfflineSource: LiveData<List<DOTransactionDetail>> = MutableLiveData()
    private val _transactionsOffline = MediatorLiveData<List<DOTransactionDetail>>()
    val transactionsOffline: LiveData<List<DOTransactionDetail>> get() = _transactionsOffline

    private var bagSource: LiveData<List<DOBag>> = MutableLiveData()
    private val _bagSource = MediatorLiveData<List<DOBag>>()
    val doBags: LiveData<List<DOBag>> get() = _bagSource

    private var allDOBagsInfo: LiveData<List<DOBag>> = MutableLiveData()
    private val _allDOBagsInfo = MediatorLiveData<List<DOBag>>()
    val doBagsInfo: LiveData<List<DOBag>> get() = _allDOBagsInfo

    fun getTransactionDetail(bagQrCode: String, id: String) = viewModelScope.launch(dispatchers.main) {
        _transaction.removeSource(transactionSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            transactionSource = useCase.getTransactionDetail(bagQrCode, id)
        }
        _transaction.addSource(transactionSource) {
            _transaction.value = it
        }
    }

    fun getTransactionDetailOffline(id: String) = viewModelScope.launch(dispatchers.main)
    {
        _transactionOffline.removeSource(transactionOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            transactionOfflineSource = useCase.getTransactionDetailOffline(id)
        }
        _transactionOffline.addSource(transactionOfflineSource) {
            _transactionOffline.value = it
        }
    }

    fun updateTransactionDetails(lotTransactionId: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.updateTransactionDetails(lotTransactionId)
        }
    }

    fun getTransactions() = viewModelScope.launch(dispatchers.main) {
        _transactions.removeSource(transactionListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            transactionListSource = useCase.getTransactions()
        }
        _transactions.addSource(transactionListSource) {
            _transactions.value = it
        }
    }

    fun getTransactionsOffline() = viewModelScope.launch(dispatchers.main)
    {
        _transactionsOffline.removeSource(transactionListOfflineSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            transactionListOfflineSource = useCase.getTransactionsOffline()
        }
        _transactionsOffline.addSource(transactionListOfflineSource) {
            _transactionsOffline.value = it
        }
    }

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _customLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = useCase.getCustomLocations()
        }
        _customLocation.addSource(customLocationSource) {
            _customLocation.value = it
        }
    }


    fun getAllDOBagsInfo() = viewModelScope.launch(dispatchers.main) {
        _allDOBagsInfo.removeSource(allDOBagsInfo) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            allDOBagsInfo = useCase.getAllDOBagsInfo()
        }
        _allDOBagsInfo.addSource(allDOBagsInfo) {
            _allDOBagsInfo.value = it
        }
    }

    fun getDOBags(txnId: String?, isAllBag: Boolean? = false) = viewModelScope.launch(dispatchers.main) {
        _bagSource.removeSource(bagSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            bagSource = useCase.getDOBags(txnId, isAllBag)
        }
        _bagSource.addSource(bagSource) {
            _bagSource.value = it
        }
    }


    fun deleteBagDetail(lotTransactionId: String?, currentQrCode: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteBag(lotTransactionId, currentQrCode)
        }
    }

    fun deleteBagDetail(doBag: DOBag) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteBag(doBag)
        }
    }

    fun deleteAndSaveBagDetail(doBag: DOBag) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.deleteBag(doBag)
            useCase.saveBagDetail(doBag)
        }
    }

    val scannedQrCodes: MutableList<String> = mutableListOf()

    fun addQr(qr: String) {
        if (!scannedQrCodes.contains(qr))
            scannedQrCodes.add(qr)
    }

    fun getQr() = scannedQrCodes
}
