package com.olam.warehouse.vegax.offloading.ui.presampling

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaOffloadingParameter
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloading.R
import com.olam.warehouse.vegax.offloading.data.domain.model.BatchNumResponse
import com.olam.warehouse.vegax.offloading.data.domain.model.OffloadingQualityPostResponse
import com.olam.warehouse.vegax.offloading.data.domain.model.VegaOffloadingLotQuality
import com.olam.warehouse.vegax.offloading.data.domain.model.VegaOffloadingQualityPost
import com.olam.warehouse.vegax.offloading.databinding.FragmentVegaOffloadingPresamplingBinding
import com.olam.warehouse.vegax.offloading.databinding.ItemVegaOffloadingPresamplingBinding
import com.olam.warehouse.vegax.offloading.ui.VegaOffloadingViewModel
import com.olam.warehouse.vegax.offloading.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaOffloadingPreSamplingFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private var offloadingData = VegaOffloadingTrucks()
    private var isData: Boolean? = false

    private var offloadingParamList = arrayListOf<VegaQualityParameter>()
    private var preSamplingList = mutableListOf<VegaQualityParamsWithQualitative?>()
    private var qualityPostList = arrayListOf<VegaOffloadingTrucks>()

    var preList = mutableListOf<VegaQualityParameter>()
    var locationList = mutableListOf<VegaQualityParameter>()
    var bagList = mutableListOf<VegaQualityParameter>()
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var suggLocationList = mutableListOf<VegaCustomStLocation>()

    private lateinit var binding: FragmentVegaOffloadingPresamplingBinding
    override val layoutResourceId = R.layout.fragment_vega_offloading_presampling
    private val vm: VegaOffloadingViewModel by viewModel()


    interface CallBack {
        fun replaceFragment(
            paramsListFrag: String,
            item: VegaOffloadingTrucks,
            offloadingParamList: ArrayList<VegaQualityParameter>
        )
    }

    companion object {
        fun newInstance(offloadingData: VegaOffloadingTrucks) = VegaOffloadingPreSamplingFragment().putArgs {
            putParcelable(OFFLOADING_DATA, offloadingData)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaOffloadingPresamplingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloading/ui/presampling/VegaOffloadingPreSamplingFragment").title("Offloading")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnAccept, it, true)
            getActionBtnChangedView(binding.btnNext, it, true)
        }
        offloadingData = arguments?.getParcelable(OFFLOADING_DATA)!!
        vm.paramList.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateParams(it) })
        vm.batch.observe(viewLifecycleOwner, Observer { updateBatchNumber(it) })

        offloadingData.materialCode?.let {
            vm.getOffloadingParams(it, isData, offloadingData.weighBridgeId)
        }

        if (offloadingData.weighBridgeType == STO) {
            binding.btnAccept.gone()
            binding.btnReject.gone()
            binding.llLocation.visible()
            binding.btnNext.visible()
            binding.btnNext.text = getString(R.string.confirm)
            binding.rvPreSampling.isEnabled = true
            //binding.rvPreSampling.alpha = 0.5f
        }

        binding.tvBatchNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.batch_number)) { mandatoryStars() } }
        binding.tvType.text = if (offloadingData.weighBridgeType.equals(PROCURE)) SUPPLIER else MTNR
        binding.tvTruckID.text = "TruckID : ".plus(offloadingData.vehicleNumber)
        binding.btnAccept.setOnClickListener { v -> validateParameter(v.id) }
        binding.btnReject.setOnClickListener { v -> validateParameter(v.id) }
        binding.btnNext.setOnClickListener { v -> validateParameter(v.id) }

        vm.quality.observe(viewLifecycleOwner, Observer { updateRejectUI(it) })
        vm.custonLocation.observe(
            viewLifecycleOwner,
            Observer { custonLocationList = it.filter { !it.storageLocationType.equals("P") }.toMutableList() })
        vm.getCustomLocations()

        vm.suggestCustonLocation.observe(viewLifecycleOwner, Observer { updateSuggLocationParams(it) })

    }

    private fun updateBatchNumber(response: Resource<GenericReqAndResp<BatchNumResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            binding.etBatchNo.setText(it.data?.data?.batchNumber, TextView.BufferType.EDITABLE)
                            binding.etBatchNo.isEnabled = false
                            if (offloadingData.weighBridgeType == STO) {
                                vm.getQualityParams(
                                    it.data?.data?.batchNumber.toString(),
                                    offloadingData.materialCode.toString()
                                )
                            }
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun updateRejectUI(response: Resource<GenericReqAndResp<OffloadingQualityPostResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            moveToSuccessPage(it.data?.data?.currentWbid, it.data?.data?.charg)
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")

                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun moveToSuccessPage(currentWbid: String?, charg: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.quality_reject)
        ) else intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.quality_success_offline)
        )
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id).plus(currentWbid))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showConfirmDialog(msg: Int) {
        MaterialDialog(requireContext()).show {
            message(msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(R.string.cancel),
                {
                    if (msg != R.string.confirm_reject_message) {
                        moveToLocationParams()
                    } else {
                        RejectData()
                    }
                    if (this.isShowing) dismiss()
                },
                { dismiss() })
        }
    }

    private fun RejectData() {

        offloadingParamList.clear()
        offloadingParamList.addAll(preList)
        offloadingParamList.addAll(locationList)
        offloadingParamList.addAll(bagList)
        offloadingData.batchNumber = binding.etBatchNo.text.toString()
        offloadingData.finalApproval = "D"
        postQuality(offloadingParamList)
    }

    private fun postQuality(offloadingParamList: MutableList<VegaQualityParameter>) {

        if (AppUtils.isOnline()) {
            @Suppress("UNCHECKED_CAST")
            offloadingData.qualityDetails = offloadingParamList as List<VegaOffloadingParameter>
            offloadingData.plant = getPlantDetails().plantId
            qualityPostList.add(offloadingData)
            vm.postQualityParams(
                VegaOffloadingQualityPost(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    lotDetails = qualityPostList
                )
            )
        } else {
            offloadingParamList.forEach {
                it.wbid = offloadingData.weighBridgeId.toString()
                vm.saveQualityData(prepareDOQualityData(it), offloadingData.batchNumber.toString())
            }
            // moveToSuccessPage(offloadingData.weighBridgeId, "")

        }

    }

    private fun moveTosummary() {
        offloadingParamList.clear()
        offloadingParamList.addAll(preList)
        offloadingParamList.addAll(locationList)
        offloadingParamList.addAll(bagList)
        offloadingData.batchNumber = binding.etBatchNo.text.toString()
        offloadingData.bagCount = binding.tvBagsCount.text.toString()
        callBack?.replaceFragment(SUMMARY_FRAG, offloadingData, offloadingParamList)
    }


    private fun validateParameter(id: Int) {
        val preSample = preList.filter { it.qualityParameterValue.isNullOrEmpty() }
        val location = locationList.filter { it.qualityParameterValue.isNullOrEmpty() }
        var count = 0
        bagList.forEach {
            if (it.qualityParamLabel == "Good Bags" && (it.qualityParameterValue.equals("") /*|| it.qualityParameterValue.equals(
                    "0"
                )*/)
            ) {
                count += 1
            }
        }
        if (offloadingData.weighBridgeType.equals(PROCURE)) {
            when {
                binding.etBatchNo.text.toString().isEmpty() -> {
                    showSnack(getString(R.string.enter_batch_no))
                }
                !preSample.isNullOrEmpty() -> {
                    showSnack(getString(R.string.enter_kor_origin))
                }
                else -> {
                    when (id) {
                        R.id.btnAccept -> showConfirmDialog(R.string.confirm_message)
                        R.id.btnReject -> showConfirmDialog(R.string.confirm_reject_message)
                        R.id.btnNext -> {
                            when {
                                !location.isNullOrEmpty() -> {
                                    showSnack(getString(R.string.enter_stroage_location))
                                }
                                else -> {
                                    when {
                                        !binding.btnNext.text.contains(getString(R.string.confirm)) -> {
                                            moveToBagParams()
                                        }
                                        else -> {
                                            when {
                                                count != 0 -> {
                                                    showSnack(getString(R.string.enter_no_of_bags))
                                                }
                                                else -> moveTosummary()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        } else {
            when {
                binding.etBatchNo.text.toString().isEmpty() -> {
                    showSnack(getString(R.string.enter_batch_no))
                }
                !preSample.isNullOrEmpty() -> {
                    showSnack(getString(R.string.enter_kor_origin))
                }
                !location.isNullOrEmpty() -> {
                    showSnack(getString(R.string.enter_stroage_location))
                }
                else -> {
                    moveTosummary()
                }
            }
        }

    }

    private fun moveToLocationParams() {
        var kor = ""
        var origin = ""
        preList.forEach {
            if (it.nameChar.equals("CI_RCN_KOR")) kor = it.qualityParameterValue.toString()
            else if (it.nameChar.equals("CI_RCN_REGION")) origin = it.qualityParameterValue.toString()
        }
        offloadingData.materialCode?.let { vm.getSuggestedLocation(kor, origin, it) }

        binding.btnReject.gone()
        binding.btnAccept.gone()
        binding.btnNext.visible()
        binding.llLocation.visible()
        binding.etBatchNo.isEnabled = false
        preSamplingList.forEach { it?.qualityParameter?.isEditable = false }
        //binding.rvPreSampling.adapter?.notifyDataSetChanged()
        setUpPreAdapter(preSamplingList)
        setUpLocationAdapter(locationList)

    }

    private fun updateSuggLocationParams(response: Resource<GenericReqAndResp<List<VegaCustomStLocation>>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data.let { it1 ->
                        it1?.let { it2 ->
                            suggLocationList = it2.filter { !it.storageLocationType.equals("P") }.toMutableList()
                        }
                    }
                    moveToLocationParamsAndSuggest()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }

    }

    private fun moveToLocationParamsAndSuggest() {
        binding.llSuggLocation.visible()
        setUpSuggLocationAdapter(locationList)
    }

    private fun moveToBagParams() {
        binding.llBag.visible()
        binding.btnNext.text = getString(R.string.confirm)
        setUpBagAdapter(bagList)
    }

    private fun updateUI(list: List<VegaQualityParamsWithQualitative>?) {

        if (offloadingData.weighBridgeType == STO) vm.getDeliveryBatchNumber(
            offloadingData.delivery.toString(),
            offloadingData.deliveryItem.toString()
        )
        preList.clear()
        locationList.clear()
        bagList.clear()
        if (list!!.size > 0) {
            list.forEach {
                it.qualitative = it.qualitative?.filter { it.materialCode.equals(offloadingData.materialCode) }
            }
            list.let {
                val preData = mutableListOf<VegaQualityParamsWithQualitative>()
                it.forEach { item ->
                    if (item.qualityParameter.nameChar.equals("CI_RCN_REGION")
                        || item.qualityParameter.nameChar.equals("CI_RCN_KOR")
                        || item.qualityParameter.nameChar.equals("CI_RCN_MOISTURE")
                    )
                        preData.add(item)
                }
                preList = preData.map { it.qualityParameter } as MutableList<VegaQualityParameter>
                setUpPreAdapter(preData.toMutableList())

                //location list
                val plocData = it.filter { item -> item.qualityParameter.nameChar.equals("CI_SLOC_KOR") }
                locationList = plocData.map { it.qualityParameter } as MutableList<VegaQualityParameter>
                if (offloadingData.weighBridgeType == STO) setUpLocationAdapter(locationList)

                // Bag List
                val bagData = mutableListOf<VegaQualityParamsWithQualitative>()
                it.forEach { item ->
                    if (item.qualityParameter.nameChar != "CI_RCN_REGION"
                        && item.qualityParameter.nameChar != "CI_RCN_KOR"
                        && item.qualityParameter.nameChar != "CI_RCN_MOISTURE"
                        && item.qualityParameter.nameChar != "CI_SLOC_KOR"
                    ) {
                        bagData.add(item)
                    }
                }
                if (offloadingData.weighBridgeType != STO) bagList =
                    bagData.map { it.qualityParameter } as MutableList<VegaQualityParameter>

            }
        } else {
            binding.llItemView.gone()
            binding.tvNoData.visible()
            binding.llItemBottomView.gone()
            setErrorContentView("No data available")
        }

    }


    private fun setUpPreAdapter(data: MutableList<VegaQualityParamsWithQualitative?>) {
        data.let { preSamplingList = it }
        binding.rvPreSampling.setUpAdapter(
            preSamplingList,
            R.layout.item_vega_offloading_presampling,
            ItemVegaOffloadingPresamplingBinding::inflate,
            { item, pos, bindingItem ->
                bindingItem.tvDescription.text =
                    if (!item?.qualityParameter?.qualityParamLabel.isNullOrEmpty()) with(UIUtils) {
                        with(
                            item?.qualityParameter?.qualityParamLabel.toString()
                        ) { mandatoryStars() }
                    }
                    else
                        with(UIUtils) { with(item?.qualityParameter?.descrChar.toString()) { mandatoryStars() } }
                bindingItem.etItem.setText(
                    item?.qualityParameter?.qualityParameterValue,
                    TextView.BufferType.EDITABLE
                )
                bindingItem.etItem.isEnabled = item?.qualityParameter?.isEditable ?: true
                bindingItem.tvPopupItem.isEnabled = item?.qualityParameter?.isEditable ?: true
                bindingItem.tvPopupItem.text = item?.qualityParameter?.qualityParameterValue
                if (item?.qualitative?.size!! > 0) {
                    bindingItem.etItem.gone()
                    bindingItem.tvPopupItem.visible()
                    // tvPopupItem.isEnabled = !offloadingData.weighBridgeType.equals(STO)
                } else {
                    bindingItem.etItem.visible()
                    bindingItem.tvPopupItem.gone()
                    // etItem.isEnabled = !offloadingData.weighBridgeType.equals(STO)
                }
                bindingItem.etItem.onChange {
                    //updateValueToPreList(it, item)
                    item.qualityParameter.qualityParameterValue = it
                    var itemPos: Int = 0
                    preList.forEachIndexed { pos, vegaQualityParameter ->
                        if (vegaQualityParameter.nameChar.equals(item.qualityParameter.nameChar)) itemPos =
                            pos
                    }
                    preList.removeAt(itemPos)
                    preList.add(itemPos, item.qualityParameter)
                }
                bindingItem.etItem.setOnFocusChangeListener { view, b -> if (b) getSuggestLocation() }


                bindingItem.tvPopupItem.setOnClickListener {
                    val origin = item.qualitative?.map { data -> data.descValue }
                    MaterialDialog(requireContext()).show {
                        title(R.string.select_location)
                        listItemsSingleChoice(items = origin) { _, index, text ->
                            bindingItem.tvPopupItem.text = text
                            item.qualityParameter.qualityParameterValue = text.toString()
                            var itemPos: Int = 0
                            preList.forEachIndexed { pos, vegaQualityParameter ->
                                if (vegaQualityParameter.nameChar.equals(item.qualityParameter.nameChar)) itemPos =
                                    pos
                            }
                            preList.removeAt(itemPos)
                            preList.add(itemPos, item.qualityParameter)
                            getSuggestLocation()
                        }
                        positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
                    }
                }

                if (pos == preSamplingList.size - 1) getSuggestLocation()


            })
    }

    private fun getSuggestLocation() {
        if (offloadingData.weighBridgeType.equals(STO)) {
            var kor = ""
            var origin = ""
            preList.forEach {
                if (it.nameChar.equals("CI_RCN_KOR")) kor = it.qualityParameterValue.toString()
                else if (it.nameChar.equals("CI_RCN_REGION")) origin = it.qualityParameterValue.toString()
            }
            offloadingData.materialCode?.let {
                if (kor.isNotEmpty() && origin.isNotEmpty()) vm.getSuggestedLocation(kor, origin, it)
            }
        }
    }

    private fun setUpSuggLocationAdapter(data: MutableList<VegaQualityParameter>) {
        //data.let { it -> locationList = it }
        binding.rvSuggLocation.setUpAdapter(
            data,
            R.layout.item_vega_offloading_presampling,
            ItemVegaOffloadingPresamplingBinding::inflate,
            { item, pos, bindingItem ->
                bindingItem.tvDescription.text =
                    if (!item.qualityParamLabel.isNullOrEmpty()) with(UIUtils) { with(item.qualityParamLabel.toString()) { mandatoryStars() } } else with(
                        UIUtils
                    ) { with(item.descrChar.toString()) { mandatoryStars() } }
                //tvPopupItem.text = item.qualityParameterValue
                bindingItem.tvPopupItem.visible()
                bindingItem.etItem.gone()
                bindingItem.tvPopupItem.setOnClickListener {
                    val location = suggLocationList.map { data ->
                        data.procureLocationCode.plus(" - ").plus(data.procureLocationName)
                    }
                    MaterialDialog(requireContext()).show {
                        title(R.string.select_location)
                        listItemsSingleChoice(items = location) { _, index, text ->
                            bindingItem.tvPopupItem.text = text
                            item.qualityParameterValue = text.split(" - ")[0]
                            var itemPos: Int = 0
                            locationList.forEachIndexed { pos, vegaQualityParameter ->
                                if (vegaQualityParameter.nameChar.equals(item.nameChar)) itemPos =
                                    pos
                            }
                            locationList.removeAt(itemPos)
                            locationList.add(itemPos, item)
                            val dataList: ArrayList<VegaQualityParameter> = ArrayList()
                            locationList.forEach { qty ->
                                val qualityParameter = qty.copy()
                                qualityParameter.qualityParameterValue = ""
                                dataList.add(qualityParameter)
                            }
                            setUpLocationAdapter(dataList)
                        }
                        positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
                    }
                }
            })
    }

    private fun setUpLocationAdapter(data: MutableList<VegaQualityParameter>) {
//        data.let { it -> locationList = it }
        binding.rvLocation.setUpAdapter(
            data,
            R.layout.item_vega_offloading_presampling,
            ItemVegaOffloadingPresamplingBinding::inflate,
            { item, pos, bindingItem ->
                bindingItem.tvDescription.text =
                    if (!item.qualityParamLabel.isNullOrEmpty()) with(UIUtils) { with(item.qualityParamLabel.toString()) { mandatoryStars() } } else with(
                        UIUtils
                    ) { with(item.descrChar.toString()) { mandatoryStars() } }
                bindingItem.tvPopupItem.text = item.qualityParameterValue
                bindingItem.tvPopupItem.visible()
                bindingItem.etItem.gone()
                bindingItem.tvPopupItem.setOnClickListener {
                    val suggList = suggLocationList.map { it.procureLocationName }
                    val location =
                        custonLocationList.filter { item -> !suggList.contains(item.procureLocationName) }
                            .map { data ->
                                data.procureLocationCode.plus(" - ").plus(data.procureLocationName)
                            }

                    MaterialDialog(requireContext()).show {
                        title(R.string.select_location)
                        listItemsSingleChoice(items = location) { _, index, text ->
                            bindingItem.tvPopupItem.text = text
                            item.qualityParameterValue = text.split(" - ")[0]
                            var itemPos: Int = 0
                            locationList.forEachIndexed { pos, vegaQualityParameter ->
                                if (vegaQualityParameter.nameChar.equals(item.nameChar)) itemPos =
                                    pos
                            }
                            locationList.removeAt(itemPos)
                            locationList.add(itemPos, item)

                            val dataList: ArrayList<VegaQualityParameter> = ArrayList()
                            locationList.forEach { qty ->
                                val qualityParameter = qty.copy()
                                qualityParameter.qualityParameterValue = ""
                                dataList.add(qualityParameter)
                            }
                            setUpSuggLocationAdapter(dataList)
                        }
                        positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
                    }
                }
            })
    }

    private fun setUpBagAdapter(data: MutableList<VegaQualityParameter>) {
        data.let { it -> bagList = it }
        binding.rvBagRefractionLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.bag_refraction)) { mandatoryStars() } }
        binding.rvBagRefraction.setHasFixedSize(true)
        binding.rvBagRefraction.setUpAdapter(
            bagList,
            R.layout.item_vega_offloading_presampling,
            ItemVegaOffloadingPresamplingBinding::inflate,
            { item, pos, bindingItem ->
                bindingItem.tvDescription.text = item.qualityParamLabel.toString()
                if (!item.qualityParamLabel.isNullOrEmpty()) item.qualityParamLabel else item.descrChar
                bindingItem.etItem.setText(item.qualityParameterValue, TextView.BufferType.EDITABLE)
                bindingItem.etItem.inputType = InputType.TYPE_CLASS_NUMBER
                bindingItem.etItem.onChange {
                    item.qualityParameterValue = it
                    var itemPos: Int = 0
                    bagList.forEachIndexed { index, vegaQualityParameter ->
                        if (vegaQualityParameter.nameChar.equals(item.nameChar)) itemPos = index
                    }
                    bagList.removeAt(itemPos)
                    bagList.add(itemPos, item)
                    updateTotalBagCount()

                }
            })
    }

    private fun updateTotalBagCount() {
        val count =
            bagList.filter { !it.qualityParameterValue.isNullOrEmpty() }
                .sumBy { it.qualityParameterValue?.toInt() ?: 0 }
        binding.tvBagsCount.text = count.toString()
    }

    private fun updateParams(data: Resource<GenericReqAndResp<List<VegaOffloadingLotQuality>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let { items ->
                        items[0].qualityParameters.forEach {
                            if (it.sapQCName?.equals("CI_RCN_REGION")!!) {
                                preSamplingList.forEach { item ->
                                    if (item?.qualityParameter?.nameChar.equals(it.sapQCName)) item?.qualityParameter?.qualityParameterValue =
                                        it.satNam
                                }
                            } else if (it.sapQCName?.equals("CI_RCN_KOR")!!) {
                                preSamplingList.forEach { item ->
                                    if (item?.qualityParameter?.nameChar.equals(it.sapQCName)) item?.qualityParameter?.qualityParameterValue =
                                        it.satNam!!.split(" ")[0]
                                }
                            } else if (it.sapQCName?.equals("CI_RCN_MOISTURE")!!) {
                                preSamplingList.forEach { item ->
                                    if (item?.qualityParameter?.nameChar.equals(it.sapQCName)) item?.qualityParameter?.qualityParameterValue =
                                        it.satNam!!.split(" ")[0]
                                }
                            }
                        }

                    }
                    setUpPreAdapter(preSamplingList)
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireActivity(), "Quality Fetching Failed")
                }
                else -> {
                }
            }
        }
    }


}
