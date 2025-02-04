package com.olam.warehouse.vegax.forwardponicaragua.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.master.common.model.GrnCharDetails
import com.olam.warehouse.master.common.model.GrnPriceDetails
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaForwardPoPost
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.forwardponicaragua.data.domain.model.VegaNicaraguaUpdateLotSequencePost
import com.olam.warehouse.vegax.forwardponicaragua.data.domain.usecase.VegaNicaraguaForwardPOUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class VegaNicaraguaForwardPOViewModel(
    private val useCase: VegaNicaraguaForwardPOUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {
    private var supplierSource: LiveData<List<VegaVendor>> = MutableLiveData()
    private val _supplier = MediatorLiveData<List<VegaVendor>>()
    val supplier: LiveData<List<VegaVendor>> get() = _supplier

    private var productSource: LiveData<List<VegaMaterial>> = mutableLiveDataOf(emptyList())
    private val _product = MediatorLiveData<List<VegaMaterial>>()
    val product: LiveData<List<VegaMaterial>> get() = _product

    private var gradeSource: LiveData<List<VegaQualitative>> = mutableLiveDataOf(emptyList())
    private val _grade = MediatorLiveData<List<VegaQualitative>>()
    val grade: LiveData<List<VegaQualitative>> get() = _grade

    private var materialQualityGradeSource: LiveData<List<VegaNicaraguaMaterialQualitGrades>> = mutableLiveDataOf(emptyList())
    private val _materialQualitygrade = MediatorLiveData<List<VegaNicaraguaMaterialQualitGrades>>()
    val materialQualityGrades: LiveData<List<VegaNicaraguaMaterialQualitGrades>> get() = _materialQualitygrade

    private var grnCharDetailsSource: LiveData<Resource<GenericReqAndResp<List<GrnCharDetails>>>> =
        MutableLiveData()
    private val _grnCharDetails = MediatorLiveData<Resource<GenericReqAndResp<List<GrnCharDetails>>>>()
    val grnCharDetails: LiveData<Resource<GenericReqAndResp<List<GrnCharDetails>>>> get() = _grnCharDetails

    private var grnCharDetailsOfflineSource: LiveData<List<VegaNicaraguaGrnCharDetails>> =
        MutableLiveData()
    private val _grnCharDetailsOffline = MediatorLiveData<List<VegaNicaraguaGrnCharDetails>>()
    val grnCharDetailsOffline: LiveData<List<VegaNicaraguaGrnCharDetails>> get() = _grnCharDetailsOffline

    private var grnPriceDetailsSource: LiveData<Resource<GenericReqAndResp<List<GrnPriceDetails>>>> =
        MutableLiveData()
    private val _grnPriceDetails = MediatorLiveData<Resource<GenericReqAndResp<List<GrnPriceDetails>>>>()
    val grnPriceDetails: LiveData<Resource<GenericReqAndResp<List<GrnPriceDetails>>>> get() = _grnPriceDetails

    private var grnPriceDetailsOfflineSource: LiveData<List<VegaNicaraguaGrnPriceDetails>> =
        MutableLiveData()
    private val _grnPriceDetailsOffline = MediatorLiveData<List<VegaNicaraguaGrnPriceDetails>>()
    val grnPriceDetailsOffline: LiveData<List<VegaNicaraguaGrnPriceDetails>> get() = _grnPriceDetailsOffline

    private var qualityListSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist


    private var createForwardPoSource: LiveData<Resource<GenericReqAndResp<VegaNicaraguaForwardPoPost>>> =
        MutableLiveData()
    private val _createForwardPo = MediatorLiveData<Resource<GenericReqAndResp<VegaNicaraguaForwardPoPost>>>()
    val postForwardPo: LiveData<Resource<GenericReqAndResp<VegaNicaraguaForwardPoPost>>> get() = _createForwardPo

    private var configSource: LiveData<List<VegaConfigDetails>> = mutableLiveDataOf()
    private val _configItems = MediatorLiveData<List<VegaConfigDetails>>()
    val configItems: LiveData<List<VegaConfigDetails>> get() = _configItems

    private var exchangeRateSource: LiveData<Resource<GenericReqAndResp<ExchangeRate>>> =
        MutableLiveData()
    private val _exchangeRate = MediatorLiveData<Resource<GenericReqAndResp<ExchangeRate>>>()
    val exchangeRate: LiveData<Resource<GenericReqAndResp<ExchangeRate>>> get() = _exchangeRate

    private var exchangeRateSourceOffline: LiveData<VegaNicaraguaExchangeRate> =
        MutableLiveData()
    private val _exchangeRateOffline = MediatorLiveData<VegaNicaraguaExchangeRate>()
    val exchangeRateOffline: LiveData<VegaNicaraguaExchangeRate> get() = _exchangeRateOffline

    private var forwardPOPriceTransListSource: LiveData<List<VegaNicaraguaForwardPOPriceDetails>> = MutableLiveData()
    private val _forwardPOPriceTransList = MediatorLiveData<List<VegaNicaraguaForwardPOPriceDetails>>()
    val forwardPOPriceTransList: LiveData<List<VegaNicaraguaForwardPOPriceDetails>> get() = _forwardPOPriceTransList


    private var forwardPOTransListSource: LiveData<List<VegaNicaraguaForwardPODetails>> = MutableLiveData()
    private val _forwardPOTransList = MediatorLiveData<List<VegaNicaraguaForwardPODetails>>()
    val forwardPOTransList: LiveData<List<VegaNicaraguaForwardPODetails>> get() = _forwardPOTransList

    private var updateLotSequenceSource: LiveData<Resource<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>> =
        MutableLiveData()
    private val _updateLotSequence = MediatorLiveData<Resource<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>>()
    val updateLotSequence: LiveData<Resource<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>> get() = _updateLotSequence

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
    fun getGrades(materialCode: String) = viewModelScope.launch(dispatchers.main) {
        _grade.removeSource(gradeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            gradeSource = useCase.getGrades(materialCode)
        }
        _grade.addSource(gradeSource) {
            _grade.value = it
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

    //Quality
    fun getQualityParams(materialId: String) = viewModelScope.launch(dispatchers.main) {
        _qualitylist.removeSource(qualityListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityListSource = useCase.getQualityParams(materialId)
        }
        _qualitylist.addSource(qualityListSource) {
            _qualitylist.value = it
        }
    }

    fun postCreateForwardPO(
        receivingData: VegaNicaraguaForwardPoPost
    ) = viewModelScope.launch(dispatchers.main) {
        _createForwardPo.removeSource(createForwardPoSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            createForwardPoSource = useCase.postForwardPO(receivingData)
        }
        _createForwardPo.addSource(createForwardPoSource) {
            _createForwardPo.value = it
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

    fun saveForwardPoData(receivingData: VegaNicaraguaForwardPODetails) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.saveForwardPOData(receivingData)
        }
    }

    fun saveForwardPoPriceDetails(pricingDetails: ArrayList<VegaNicaraguaForwardPOPriceDetails>) =
        viewModelScope.launch {
            withContext(dispatchers.io) {
                useCase.saveForwardPOPriceDetails(pricingDetails)
            }
        }

    fun getForwardPODetails() = viewModelScope.launch(dispatchers.main) {
        _forwardPOTransList.removeSource(forwardPOTransListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            forwardPOTransListSource = useCase.getForwardPODetails()
        }
        _forwardPOTransList.addSource(forwardPOTransListSource) {
            _forwardPOTransList.value = it
        }
    }

    fun getForwardPOPriceDetails(tempId: String) = viewModelScope.launch(dispatchers.main) {
        _forwardPOPriceTransList.removeSource(forwardPOPriceTransListSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            forwardPOPriceTransListSource = useCase.getForwardPOPriceDetails(tempId)
        }
        _forwardPOPriceTransList.addSource(forwardPOPriceTransListSource) {
            _forwardPOPriceTransList.value = it
        }
    }

    fun deleteItem(tmpWbId: String) = viewModelScope.launch {
        withContext(dispatchers.io) {
            useCase.deleteItem(tmpWbId)
        }
    }
    fun generatePoSequnceNumber(): String {
        var grnSequnceId = ""
        val plant = getPlantDetails()
        val plantCode = plant.plantCode
        val plantId = plant.plantId
        val rightNow = Calendar.getInstance()
        val year = rightNow.get(Calendar.YEAR).toString().substring(2)
        val grnSequence = PreferenceHelper.get(Constants.PO_SEQUENCE, "")
        grnSequnceId = plantId.plus(year).plus(grnSequence)

        return grnSequnceId
    }
    fun postUpdateLotSequence(batchNumber: String, isInvoiceFlag: Boolean) = viewModelScope.launch(dispatchers.main) {
        _updateLotSequence.removeSource(updateLotSequenceSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            val poSequncce1 = PreferenceHelper.get(Constants.PO_SEQUENCE, "")
            var poSequence2=""
            val poSequncce = poSequncce1.substring(poSequncce1.length - 5)
            if (poSequncce.isNotEmpty()) poSequence2 = (poSequncce.toInt() - 1).toString()
            when (poSequence2.length) {
                1 ->

                    poSequence2="0000".plus(poSequence2.toString())

                2 ->
                    poSequence2="000".plus(poSequence2.toString())

                3 -> poSequence2="00".plus(poSequence2.toString())

                4 ->
                    poSequence2="0".plus(poSequence2.toString())

                5 -> poSequence2=poSequence2.toString()
            }
            Log.d("qwerty VM",poSequence2)
            val rightNow = Calendar.getInstance()
            val year = rightNow.get(Calendar.YEAR).toString()
            val prefix1 = PreferenceHelper.get(Constants.USER_NAME, "")

            val postData = VegaNicaraguaUpdateLotSequencePost(
                getPlantDetails(),
                prefix1,
                year,
              "",
                "N",
                "N",
                "",
                "",
                "N",
                poSequence = poSequence2,
                isPoRefSequence = "Y"
            )
            updateLotSequenceSource = useCase.updateLotSequence(postData)
        }
        _updateLotSequence.addSource(updateLotSequenceSource) {
            _updateLotSequence.value = it
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
}
