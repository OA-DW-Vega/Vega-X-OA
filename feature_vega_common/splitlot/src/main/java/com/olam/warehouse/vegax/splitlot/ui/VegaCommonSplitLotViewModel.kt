package com.olam.warehouse.vegax.splitlot.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.mutableLiveDataOf
import com.olam.warehouse.vegax.splitlot.data.domain.model.SplitUpdateTallySequencePost
import com.olam.warehouse.vegax.splitlot.data.domain.model.VegaCoffeeQualityParamPost
import com.olam.warehouse.vegax.splitlot.data.domain.model.VegaCoffeeQualityParamPostResponse
import com.olam.warehouse.vegax.splitlot.data.domain.model.VegaCommonSplitMainModel
import com.olam.warehouse.vegax.splitlot.data.domain.usecase.VegaCommonSplitLotUsecase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 9/26/2022.
 */
class VegaCommonSplitLotViewModel(
    private val useCase: VegaCommonSplitLotUsecase,
    private val dispatchers: AppDispatchers
): BaseViewModel() {

    fun generateTallySequnceNumber(): String {
        var tallySequnceId = ""
        val tallySequence = PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
        val cropyear = PreferenceHelper.get(Constants.CROP_YEAR, "")
        tallySequnceId = cropyear.plus("-").plus(tallySequence)

        return tallySequnceId
    }

    private var lotQualitySource: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> =
        MutableLiveData()
    private val _lotQuality =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>()
    val lotQuality: LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> get() = _lotQuality


    private var qualityGetSource: LiveData<List<VegaQualityParamsWithQualitative>> = MutableLiveData()
    private val _qualitylist = MediatorLiveData<List<VegaQualityParamsWithQualitative>>()
    val qualitylist: LiveData<List<VegaQualityParamsWithQualitative>> get() = _qualitylist

    private var gradeSource: LiveData<List<VegaQualitative>> = mutableLiveDataOf(emptyList())
    private val _grade = MediatorLiveData<List<VegaQualitative>>()
    val grade: LiveData<List<VegaQualitative>> get() = _grade

    private var materialQualityGradeSource: LiveData<List<VegaNicaraguaMaterialQualitGrades>> = mutableLiveDataOf(emptyList())
    private val _materialQualitygrade = MediatorLiveData<List<VegaNicaraguaMaterialQualitGrades>>()
    val materialQualityGrades: LiveData<List<VegaNicaraguaMaterialQualitGrades>> get() = _materialQualitygrade

    private var splitPostSource: LiveData<Resource<GenericReqAndResp<VegaCommonSplitMainModel>>> = MutableLiveData()
    private val _splitPost = MediatorLiveData<Resource<GenericReqAndResp<VegaCommonSplitMainModel>>>()
    val splitPost: LiveData<Resource<GenericReqAndResp<VegaCommonSplitMainModel>>> get() = _splitPost

    private var qualityPostSource: LiveData<Resource<GenericReqAndResp<VegaCoffeeQualityParamPostResponse>>> = MutableLiveData()
    private val _qualityPost = MediatorLiveData<Resource<GenericReqAndResp<VegaCoffeeQualityParamPostResponse>>>()
    val qualityPost: LiveData<Resource<GenericReqAndResp<VegaCoffeeQualityParamPostResponse>>> get() = _qualityPost

    private var tallySequenceSource: LiveData<Resource<GenericReqAndResp<SplitUpdateTallySequencePost>>> =
        MutableLiveData()
    private val _updatetallySequence =
        MediatorLiveData<Resource<GenericReqAndResp<SplitUpdateTallySequencePost>>>()
    val updatetallySequence: LiveData<Resource<GenericReqAndResp<SplitUpdateTallySequencePost>>> get() = _updatetallySequence

    fun getQualityParams(materialId: String) =
        viewModelScope.launch(dispatchers.main) {
            _qualitylist.removeSource(qualityGetSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                qualityGetSource = useCase.getQualityParams(materialId)
            }
            _qualitylist.addSource(qualityGetSource) {
                _qualitylist.value = it
            }
        }

    fun updateTallySequence(paramPost: SplitUpdateTallySequencePost) =
        viewModelScope.launch(dispatchers.main) {
            _updatetallySequence.removeSource(tallySequenceSource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                tallySequenceSource = useCase.updateTallySequence(paramPost)
            }
            _updatetallySequence.addSource(tallySequenceSource) {
                _updatetallySequence.value = it
            }
        }

    fun postQualityParams(paramPost: VegaCoffeeQualityParamPost) = viewModelScope.launch(dispatchers.main) {
        _qualityPost.removeSource(qualityPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            qualityPostSource = useCase.postQuality(paramPost)
        }
        _qualityPost.addSource(qualityPostSource) {
            _qualityPost.value = it
        }
    }

    fun postSplitDeatils(postData: VegaCommonSplitMainModel) = viewModelScope.launch(dispatchers.main) {
        _splitPost.removeSource(splitPostSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            splitPostSource = useCase.postSplitDeatils(postData)
        }
        _splitPost.addSource(splitPostSource) {
            _splitPost.value = it
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

    fun getGrades(materialCode: String) = viewModelScope.launch(dispatchers.main) {
        _grade.removeSource(gradeSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            gradeSource = useCase.getGrades(materialCode)
        }
        _grade.addSource(gradeSource) {
            _grade.value = it
        }
    }

    fun getLotQualityDetails(batchNo: String, materialId: String) =
        viewModelScope.launch(dispatchers.main) {
            _lotQuality.removeSource(lotQualitySource) // We make sure there is only one source of livedata (allowing us properly refresh)
            withContext(dispatchers.io) {
                lotQualitySource = useCase.getLotQualityDetails(batchNo, materialId)
            }
            _lotQuality.addSource(lotQualitySource) {
                _lotQuality.value = it
            }
        }


}
