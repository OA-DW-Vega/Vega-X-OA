package com.olam.warehouse.vegax.stockrecon.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.CustomStLocation
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.model.VegaStockReconGetAllAuditData
import com.olam.warehouse.master.vega.model.VegaStockReconIdDetails
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaReconReportAuditDetails
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaReconReportReconList
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconCreateReconIdReq
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconPostAuditDataReq
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconPostAuditDataResp
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconPrintRecipt
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconUpdateStatus
import com.olam.warehouse.vegax.stockrecon.data.domian.usecase.VegaStockReconUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaStockReconViewModel(
    private val useCase: VegaStockReconUseCase, private val dispatchers: AppDispatchers
) : BaseViewModel() {


    private var stocksSource: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>> = MutableLiveData()
    private val _stocks = MediatorLiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>>()
    val stocks: LiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>> get() = _stocks

    val validateLot = MutableLiveData<VegaDispatchLots>()

    private var lotDetailsSource: LiveData<Resource<GenericReqAndResp<List<VegaDispatchLots>>>> = MutableLiveData()
    private val _lotDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaDispatchLots>>>>()
    val lotDetails: LiveData<Resource<GenericReqAndResp<List<VegaDispatchLots>>>> get() = _lotDetails

    private var stockReconInprogressListSource: LiveData<Resource<GenericReqAndResp<List<VegaStockReconIdDetails>>>> =
        MutableLiveData()
    private val _stockReconInprogressList =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaStockReconIdDetails>>>>()
    val stockReconInprogressList: LiveData<Resource<GenericReqAndResp<List<VegaStockReconIdDetails>>>> get() = _stockReconInprogressList

    private var reconIdSource: LiveData<Resource<GenericReqAndResp<VegaStockReconIdDetails>>> =
        MutableLiveData()
    private val _reconId = MediatorLiveData<Resource<GenericReqAndResp<VegaStockReconIdDetails>>>()
    val reconId: LiveData<Resource<GenericReqAndResp<VegaStockReconIdDetails>>> get() = _reconId

    private var postDataRespSource: LiveData<Resource<GenericReqAndResp<VegaStockReconPostAuditDataResp>>> =
        MutableLiveData()
    private val _postDataResp = MediatorLiveData<Resource<GenericReqAndResp<VegaStockReconPostAuditDataResp>>>()
    val postDataResp: LiveData<Resource<GenericReqAndResp<VegaStockReconPostAuditDataResp>>> get() = _postDataResp

    private var allAuditDataResource: LiveData<Resource<GenericReqAndResp<List<VegaStockReconGetAllAuditData>>>> =
        MutableLiveData()
    private val _allAuditData = MediatorLiveData<Resource<GenericReqAndResp<List<VegaStockReconGetAllAuditData>>>>()
    val allAuditData: LiveData<Resource<GenericReqAndResp<List<VegaStockReconGetAllAuditData>>>> get() = _allAuditData

    private var updatedReconIdStatusResource: LiveData<Resource<GenericReqAndResp<VegaStockReconUpdateStatus>>> =
        MutableLiveData()
    private val _updatedReconIdStatus =
        MediatorLiveData<Resource<GenericReqAndResp<VegaStockReconUpdateStatus>>>()
    val updatedReconIdStatus: LiveData<Resource<GenericReqAndResp<VegaStockReconUpdateStatus>>> get() = _updatedReconIdStatus

    private var deleteAuditDataResource: LiveData<Resource<GenericMessage>> =
        MutableLiveData()
    private val _deleteAuditData =
        MediatorLiveData<Resource<GenericMessage>>()
    val deleteAuditData: LiveData<Resource<GenericMessage>> get() = _deleteAuditData

    private var packingMaterialResource: LiveData<List<VegaPackageMaterial>> =
        MutableLiveData()
    private val _packingMaterials =
        MediatorLiveData<List<VegaPackageMaterial>>()
    val packingMaterial: LiveData<List<VegaPackageMaterial>> get() = _packingMaterials

    private var vegaMaterialResource: LiveData<List<VegaMaterial>> =
        MutableLiveData()
    private val _vegaMaterials =
        MediatorLiveData<List<VegaMaterial>>()
    val vegaMaterials: LiveData<List<VegaMaterial>> get() = _vegaMaterials

    private var printReceiptStatusResource: LiveData<Resource<GenericReqAndResp<VegaStockReconPrintRecipt>>> =
        MutableLiveData()
    private val _printReciptStatus =
        MediatorLiveData<Resource<GenericReqAndResp<VegaStockReconPrintRecipt>>>()
    val printReceiptStatus: LiveData<Resource<GenericReqAndResp<VegaStockReconPrintRecipt>>> get() = _printReciptStatus

    private var storageLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _storageLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val storageLocation: LiveData<List<VegaCustomStLocation>> get() = _storageLocation

    private var reconReportReconListSource: LiveData<Resource<GenericReqAndResp<VegaReconReportReconList>>> =
        MutableLiveData()
    private val _reconReportReconList =
        MediatorLiveData<Resource<GenericReqAndResp<VegaReconReportReconList>>>()
    val reconReportReconList: LiveData<Resource<GenericReqAndResp<VegaReconReportReconList>>> get() = _reconReportReconList

    private var reconReportAuditListResource: LiveData<Resource<GenericReqAndResp<List<VegaStockReconGetAllAuditData>>>> =
        MutableLiveData()
    private val _reconReportAuditList =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaStockReconGetAllAuditData>>>>()
    val reconReportAuditList: LiveData<Resource<GenericReqAndResp<List<VegaStockReconGetAllAuditData>>>> get() = _reconReportAuditList


    private var reconReportAuditDetailSource: LiveData<Resource<GenericReqAndResp<VegaReconReportAuditDetails>>> =
        MutableLiveData()
    private val _reconReportAuditDetail =
        MediatorLiveData<Resource<GenericReqAndResp<VegaReconReportAuditDetails>>>()
    val reconReportAuditDetail: LiveData<Resource<GenericReqAndResp<VegaReconReportAuditDetails>>> get() = _reconReportAuditDetail

    private var auditImageSource: LiveData<Resource<GenericReqAndResp<String>>> =
        MutableLiveData()
    private val _auditImage =
        MediatorLiveData<Resource<GenericReqAndResp<String>>>()
    val auditImage: LiveData<Resource<GenericReqAndResp<String>>> get() = _auditImage

    private var reconReportImageSource: LiveData<Resource<GenericReqAndResp<String>>> =
        MutableLiveData()
    private val _reconReportImage =
        MediatorLiveData<Resource<GenericReqAndResp<String>>>()
    val reconReportImage: LiveData<Resource<GenericReqAndResp<String>>> get() = _reconReportImage


    fun fetchStocks(materialList: ArrayList<String>, plantId: String) = viewModelScope.launch(dispatchers.main) {
        _stocks.removeSource(stocksSource)
        withContext(dispatchers.io) {
            stocksSource = useCase.getStockList(materialList, plantId)
        }
        _stocks.addSource(stocksSource) {
            _stocks.value = it
        }
    }

    fun validateLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateLot.postValue(useCase.validateLot(batchNumber))
        }
    }

    fun getLotDetails(charge: String, whId: String) = viewModelScope.launch(dispatchers.main) {
        _lotDetails.removeSource(lotDetailsSource)
        withContext(dispatchers.io) {
            lotDetailsSource = useCase.getLotDetails(charge, whId)
        }
        _lotDetails.addSource(lotDetailsSource) {
            _lotDetails.value = it
        }
    }

    fun getStockReconInprogressCompletedList(plantId: String, storageLocation: String) =
        viewModelScope.launch(dispatchers.main) {
            _stockReconInprogressList.removeSource(stockReconInprogressListSource)
            withContext(dispatchers.io) {
                stockReconInprogressListSource = useCase.getStockReconInProgressCompletedList(plantId, storageLocation)
            }
            _stockReconInprogressList.addSource(stockReconInprogressListSource) {
                _stockReconInprogressList.value = it
            }
        }

    fun getReconId(req: VegaStockReconCreateReconIdReq) = viewModelScope.launch(dispatchers.main) {
        _reconId.removeSource(reconIdSource)
        withContext(dispatchers.io) {
            reconIdSource = useCase.getStockReconId(req)
        }
        _reconId.addSource(reconIdSource) {
            _reconId.value = it
        }
    }

    fun postAuditData(req: VegaStockReconPostAuditDataReq) = viewModelScope.launch(dispatchers.main) {
        _postDataResp.removeSource(postDataRespSource)
        withContext(dispatchers.io) {
            postDataRespSource = useCase.postAuditData(req)
        }
        _postDataResp.addSource(postDataRespSource) {
            _postDataResp.value = it
        }
    }

    fun fetchAllAuditData(reconId: String) {
        viewModelScope.launch(dispatchers.main) {
            _allAuditData.removeSource(allAuditDataResource)
            withContext(dispatchers.io) {
                allAuditDataResource = useCase.fetchAllAuditData(reconId)
            }
            _allAuditData.addSource(allAuditDataResource) {
                _allAuditData.value = it
            }
        }
    }

    fun updateStockReconIdStatus(reconId: String, status: String) {
        viewModelScope.launch(dispatchers.main) {
            _updatedReconIdStatus.removeSource(updatedReconIdStatusResource)
            withContext(dispatchers.io) {
                updatedReconIdStatusResource = useCase.updateReconIdStatus(reconId, status)
            }
            _updatedReconIdStatus.addSource(updatedReconIdStatusResource) {
                _updatedReconIdStatus.value = it
            }
        }
    }

    fun deleteAuditData(reconId: String, auditId: String) {
        viewModelScope.launch(dispatchers.main) {
            _deleteAuditData.removeSource(deleteAuditDataResource)
            withContext(dispatchers.io) {
                deleteAuditDataResource = useCase.deleteAuditData(reconId, auditId)
            }
            _deleteAuditData.addSource(deleteAuditDataResource) {
                _deleteAuditData.value = it
            }
        }
    }

    fun getPackingMaterials() {
        viewModelScope.launch(dispatchers.main) {
            _packingMaterials.removeSource(packingMaterialResource)
            withContext(dispatchers.io) {
                packingMaterialResource = useCase.getPackageMaterial()
            }
            _packingMaterials.addSource(packingMaterialResource) {
                _packingMaterials.value = it
            }
        }
    }

    fun getVegaMaterials() {
        viewModelScope.launch(dispatchers.main) {
            _vegaMaterials.removeSource(vegaMaterialResource)
            withContext(dispatchers.io) {
                vegaMaterialResource = useCase.getVegaMaterials()
            }
            _vegaMaterials.addSource(vegaMaterialResource) {
                _vegaMaterials.value = it
            }
        }
    }

    fun postPrintReceipt(req: VegaStockReconPrintRecipt) {
        viewModelScope.launch(dispatchers.main) {
            _printReciptStatus.removeSource(printReceiptStatusResource)
            withContext(dispatchers.io) {
                printReceiptStatusResource = useCase.postPrintRecipt(req)
            }
            _printReciptStatus.addSource(printReceiptStatusResource) {
                _printReciptStatus.value = it
            }
        }
    }

    fun getStorageLocations() = viewModelScope.launch(dispatchers.main) {
        _storageLocation.removeSource(storageLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            storageLocationSource = useCase.getCustomLocations()
        }
        _storageLocation.addSource(storageLocationSource) {
            _storageLocation.value = it
        }
    }

    fun getReconReportReconList(plantId: String, fromDate: String, toDate: String) =
        viewModelScope.launch(dispatchers.main) {
            _reconReportReconList.removeSource(reconReportReconListSource)
            withContext(dispatchers.io) {
                reconReportReconListSource = useCase.getReconReportReconList(plantId, fromDate, toDate)
            }
            _reconReportReconList.addSource(reconReportReconListSource) {
                _reconReportReconList.value = it
            }
        }

    fun getReconReportAuditDetails(reconId: String, auditId: String) =
        viewModelScope.launch(dispatchers.main) {
            _reconReportAuditDetail.removeSource(reconReportAuditDetailSource)
            withContext(dispatchers.io) {
                reconReportAuditDetailSource = useCase.getReconReportAuditDetails(reconId, auditId)
            }
            _reconReportAuditDetail.addSource(reconReportAuditDetailSource) {
                _reconReportAuditDetail.value = it
            }
        }

    fun getReconReportAuditList(reconId: String) {
        viewModelScope.launch(dispatchers.main) {
            _reconReportAuditList.removeSource(reconReportAuditListResource)
            withContext(dispatchers.io) {
                reconReportAuditListResource = useCase.getReconReportAuditList(reconId)
            }
            _reconReportAuditList.addSource(reconReportAuditListResource) {
                _reconReportAuditList.value = it
            }
        }
    }

    fun getReconReportImage(reconId: String) {
        viewModelScope.launch(dispatchers.main) {
            _reconReportImage.removeSource(reconReportImageSource)
            withContext(dispatchers.io) {
                reconReportImageSource = useCase.getReconReportImage(reconId)
            }
            _reconReportImage.addSource(reconReportImageSource) {
                _reconReportImage.value = it
            }
        }
    }

    fun getAuditImage(reconId: String, auditId: String) {
        viewModelScope.launch(dispatchers.main) {
            _auditImage.removeSource(auditImageSource)
            withContext(dispatchers.io) {
                auditImageSource = useCase.getAuditImage(reconId, auditId)
            }
            _auditImage.addSource(auditImageSource) {
                _auditImage.value = it
            }
        }
    }
}
