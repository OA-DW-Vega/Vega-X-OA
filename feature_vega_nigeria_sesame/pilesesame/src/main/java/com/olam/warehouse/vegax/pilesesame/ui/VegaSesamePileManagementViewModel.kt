package com.olam.warehouse.vegax.pilesesame.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMtnt
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.pilesesame.data.domain.VegSesamePileManagementUseCase
import com.olam.warehouse.vegax.pilesesame.data.domain.model.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList


class VegaSesamePileManagementViewModel(
    private val useCase: VegSesamePileManagementUseCase,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {

    var lotlist = VegaCocoaDispatchLots()
    var lots: ArrayList<VegaCocoaDispatchLots> = ArrayList()
    var model = VegaCoffeeThirdPartyRequestModel()
    var currentScanLot: String = ""
    var salesOrder = VegaCoffeeSalesOrder()
    val validateLot = MutableLiveData<VegaCocoaDispatchLots>()
    var lotsList = ArrayList<VegaCocoaDispatchLots>()
    var mtnt = VegaNicaraguaMtnt()

    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val supplier: LiveData<List<VegaVendor>> get() = _supplier

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var stockListSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> = MutableLiveData()
    private val _stockList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val stockList: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _stockList

    private var createPileSource: LiveData<Resource<GenericReqAndResp<VegaSesamePileSequence>>> = MutableLiveData()
    private val _createPile = MediatorLiveData<Resource<GenericReqAndResp<VegaSesamePileSequence>>>()
    val createPile: LiveData<Resource<GenericReqAndResp<VegaSesamePileSequence>>> get() = _createPile

    private var qualityParamSource: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> =
        MutableLiveData()
    private val _qualityDetails = MediatorLiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>()
    val qualityDetails: LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> get() = _qualityDetails

    private var postPileSource: LiveData<Resource<GenericReqAndResp<VegaSesamePileSuccessResponse>>> = MutableLiveData()
    private val _postPile = MediatorLiveData<Resource<GenericReqAndResp<VegaSesamePileSuccessResponse>>>()
    val postPile: LiveData<Resource<GenericReqAndResp<VegaSesamePileSuccessResponse>>> get() = _postPile

    private var miscellaneousSource: LiveData<List<VegaCocoaMiscellaneous>> = mutableLiveDataOf()
    private val _miscellaneousItems = MediatorLiveData<List<VegaCocoaMiscellaneous>>()
    val miscellaneousItems: LiveData<List<VegaCocoaMiscellaneous>> get() = _miscellaneousItems

    val thirdPartyMaterial = MutableLiveData<List<VegaCoffeeThirdPartyMaterialDetail>>()

    fun getPostPile(postPileRequest: VegaSesamePilePostRequest) = viewModelScope.launch(dispatchers.main) {
        _postPile.removeSource(postPileSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            postPileSource = useCase.getPostPile(postPileRequest)
        }
        _postPile.addSource(postPileSource) {
            _postPile.value = it
        }
    }


    fun getCreatePile() = viewModelScope.launch(dispatchers.main) {
        _createPile.removeSource(createPileSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            createPileSource = useCase.getCreatePile()
        }
        _createPile.addSource(createPileSource) {
            _createPile.value = it
        }
    }

    fun addLoTInDB(lot: List<VegaCocoaDispatchLots>) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertLotList(lot)
        }
    }

    fun validateLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            validateLot.postValue(useCase.validateLot(batchNumber))
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

    fun getShiftRemarksItems(role: String) = viewModelScope.launch(dispatchers.main) {
        _miscellaneousItems.removeSource(miscellaneousSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            miscellaneousSource = useCase.getShiftRemarkItems(role)
        }
        _miscellaneousItems.addSource(miscellaneousSource) {
            _miscellaneousItems.value = it
        }
    }

    fun getThirdPartyMaterials() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            thirdPartyMaterial.postValue(useCase.getThirdPartyMaterials())
        }
    }

    val sapMaterial = MutableLiveData<List<VegaMaterial>>()

    fun getSapMaterials() = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            sapMaterial.postValue(useCase.getSapMaterials())
        }
    }


    private var stockPileSource: LiveData<Resource<GenericReqAndResp<List<VegaSesamePileSelectionModel>>>> =
        MutableLiveData()
    private val _stockPile =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaSesamePileSelectionModel>>>>()
    val stockPile: LiveData<Resource<GenericReqAndResp<List<VegaSesamePileSelectionModel>>>> get() = _stockPile

    private var customLocationSource: LiveData<List<VegaCustomStLocation>> = MutableLiveData()
    private val _custonLocation = MediatorLiveData<List<VegaCustomStLocation>>()
    val custonLocation: LiveData<List<VegaCustomStLocation>> get() = _custonLocation

    fun getCustomLocations() = viewModelScope.launch(dispatchers.main) {
        _custonLocation.removeSource(customLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            customLocationSource = useCase.getCustomLocations()
        }
        _custonLocation.addSource(customLocationSource) {
            _custonLocation.value = it
        }
    }

    fun getStockPiles(materialList: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        _stockPile.removeSource(stockPileSource)
        withContext(dispatchers.io) {
            stockPileSource = useCase.getStockPile(materialList)
        }
        _stockPile.addSource(stockPileSource) {
            _stockPile.value = it
        }
    }


    fun getStockList(materialList: ArrayList<String>) = viewModelScope.launch(dispatchers.main) {
        _stockList.removeSource(stockListSource)
        withContext(dispatchers.io) {
            stockListSource = useCase.getStockList(materialList)
        }
        _stockList.addSource(stockListSource) {
            _stockList.value = it
        }
    }

    fun saveWeighBridgeAndLotDetails(list: MutableList<VegaCocoaDispatchLots>) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveLotInDispatch(list)
            }
        }

    fun getSuppliers() = viewModelScope.launch(dispatchers.main) {
        _supplier.removeSource(supplierSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            supplierSource = useCase.getSuppliers()
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

    fun removeLot(batchNumber: String) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            useCase.removeLot(batchNumber)
        }
    }

    fun saveThirdPartyInfo() = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.insertThirdPartyModel(model)
            if (lots.isNotEmpty())
                useCase.insertLotList(lots)
        }
    }

    private var storageLocationSource: LiveData<List<VegaStorageLocation>> = MutableLiveData()
    private val _storageLocation = MediatorLiveData<List<VegaStorageLocation>>()
    val storageLocation: LiveData<List<VegaStorageLocation>> get() = _storageLocation

    fun getStorageLocations() = viewModelScope.launch(dispatchers.main) {
        _storageLocation.removeSource(storageLocationSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            storageLocationSource = useCase.getStoreLocations()
        }
        _storageLocation.addSource(storageLocationSource) {
            _storageLocation.value = it
        }
    }

    private var gradeSource: LiveData<List<VegaQualitative>> = MutableLiveData()
    private val _grade = MediatorLiveData<List<VegaQualitative>>()
    val grade: LiveData<List<VegaQualitative>> get() = _grade

    fun getGrades(materialCode: String) = viewModelScope.launch(dispatchers.main) {
        _grade.removeSource(gradeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            gradeSource = useCase.getGrades(materialCode)
        }
        _grade.addSource(gradeSource) {
            _grade.value = it
        }
    }

    private var materialQualityGradeSource: LiveData<List<VegaNicaraguaMaterialQualitGrades>> =
        MutableLiveData()
    private val _materialQualitygrade = MediatorLiveData<List<VegaNicaraguaMaterialQualitGrades>>()
    val materialQualityGrades: LiveData<List<VegaNicaraguaMaterialQualitGrades>> get() = _materialQualitygrade

    fun getMaterialQualityGrades(materialCode: String) = viewModelScope.launch(dispatchers.main) {
        _materialQualitygrade.removeSource(materialQualityGradeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            materialQualityGradeSource = useCase.getMaterialQualityGrades(materialCode)
        }
        _materialQualitygrade.addSource(materialQualityGradeSource) {
            _materialQualitygrade.value = it
        }
    }

    private var pileSequenceSource: LiveData<Resource<GenericReqAndResp<NicaraguaUpdatePileSequence>>> =
        MutableLiveData()
    private val _updatePileSequence =
        MediatorLiveData<Resource<GenericReqAndResp<NicaraguaUpdatePileSequence>>>()
    val updatePileSequence: LiveData<Resource<GenericReqAndResp<NicaraguaUpdatePileSequence>>> get() = _updatePileSequence

    fun updatePileSequence(paramPost: NicaraguaUpdatePileSequence) =
        viewModelScope.launch(dispatchers.main) {
            _updatePileSequence.removeSource(pileSequenceSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                pileSequenceSource = useCase.updatePileSequence(paramPost)
            }
            _updatePileSequence.addSource(pileSequenceSource) {
                _updatePileSequence.value = it
            }
        }

    fun generatePileSequnceNumber(): String {
        var pileSequnceId = ""
        val pileSequence = PreferenceHelper.get(Constants.PILE_SEQUENCE, "")

        val position1 = getPlantDetails().plantId
        val current = Calendar.getInstance()
        val month = current.get(Calendar.MONTH)
        var year = current.get(Calendar.YEAR)
        var position2: String = ""
        if (month > 9) {
            year = (year + 1)
            position2 = year.toString().takeLast(2)
        } else {
            position2 = year.toString().takeLast(2)
        }
        pileSequnceId = position1.plus(position2).plus(pileSequence)

        return pileSequnceId
    }

}
