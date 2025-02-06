package com.olam.warehouse.vegax.qualitycameroon.ui.params

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitycameroon.R
import com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.model.VegaCameroonQualityParamPost
import com.olam.warehouse.vegax.qualitycameroon.databinding.FragmentVegaCameroonMtnrQualityParamsBinding
import com.olam.warehouse.vegax.qualitycameroon.ui.VegaCameroonQualityViewModel
import com.olam.warehouse.vegax.qualitycameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaCameroonMtnrQualityParameterFragment : BaseFragment() {

    private var wbId: String? = ""
    private var lotbatchNo: String? = ""
    private var lotmaterialNo: String? = ""
    private var batchNo1: String? = ""
    private var batchNo: String? = ""
    private var isData: Boolean? = false
    private var materialNo: String? = ""
    private var netWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var itemValue: String? = ""
    private var wbType: String? = ""
    private var trucNo: String? = ""
    private var lotDetails = VegaCoffeeLot()
    private var weighBridgeDetails = VegaQualityWBDetails()
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var mAdapter = VegaCameroonMtnrQualityParamsAdapter { enableProceedBtn(it) }
    private var isSort: Boolean = false
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private val vm: VegaCameroonQualityViewModel by viewModel()
    private lateinit var mListener: OnParamsListener
    private var copiedWbid: String = ""
    private var copiedMaterial: String = ""
    private var lotItems = arrayListOf<VegaCoffeeLot>()
    private var qualityPostList = arrayListOf<VegaCoffeeLot>()
    private var qualityMasterData = mutableListOf<VegaQualityParamsWithQualitative>()

    interface OnParamsListener {
        fun onMtnrProceed(
            qualityParameter: ArrayList<VegaQualityParameter?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String,
            lotItems: ArrayList<VegaCoffeeLot>,
            lotDetails: VegaCoffeeLot,
            flag: String
        )
    }

    companion object {
        fun newInstance() = VegaCameroonMtnrQualityParameterFragment().putArgs {}
    }

    private lateinit var binding: FragmentVegaCameroonMtnrQualityParamsBinding
    override val layoutResourceId = R.layout.fragment_vega_cameroon_mtnr_quality_params

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnParamsListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCameroonMtnrQualityParamsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualitycameroon/ui/params/VegaCameroonMtnrQualityParameterFragment")
            .title("Vega_Cameroon/Quality").with(tracker)
        initUI()
        initExtra()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnParamsProceed, it, true)
            getActionBtnChangedView(binding.btnAccept, it, true)
        }
        binding.rvBagDetail.layoutManager = LinearLayoutManager(this.context)
        binding.rvBagDetail.adapter = mAdapter
        binding.btnParamsProceed.setOnClickListener {
            proceeToPost(
                FNQUALITY,
                R.string.confirm_Quty_message
            )
        }
        binding.btnAccept.setOnClickListener {
            proceeToPost(
                FNQUALITY,
                R.string.confirm_Quty_message
            )
        }
        binding.btnReject.setOnClickListener {
            proceeToPost(
                FNREJECT,
                R.string.confirm_reject_message
            )
        }
        binding.tvFilter.setOnClickListener {
            if (!isSort) {
                mAdapter.upadteFilter(mAdapter.getItems())
                isSort = true
            }
        }

        vm.custonLocation.observe(
            this,
            Observer {
                custonLocationList =
                    it.filter { !it.storageLocationType.equals("P") }.toMutableList()
            })
        vm.getCustomLocations()
        vm.qualityMtnr.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun initExtra() {
        arguments?.let {
            lotDetails = it.getParcelable(LOT)!!
            weighBridgeDetails = it.getParcelable(WEIGHSCALE)!!
            lotItems = it.getParcelableArrayList<VegaCoffeeLot>(LOT_LIST) ?: ArrayList()
            lotItems.forEach {
                it.bagCount = it.bagCount.toString().trim()
                it.bagType = it.bagType.toString().trim()
                it.bagWeight = it.bagWeight.toString().trim()
                it.pmat2Count = it.pmat2Count.toString().trim()
                it.pmat2Type = it.pmat2Type.toString().trim()
                it.pmat2Weight = it.pmat2Weight.toString().trim()
                it.pmat3Count = it.pmat3Count.toString().trim()
                it.pmat3Type = it.pmat3Type.toString().trim()
                it.pmat3Weight = it.pmat3Weight.toString().trim()
                it.netWeight = it.netWeight.toString().trim()
                it.grossWeight = it.grossWeight.toString().trim()
                it.bagTareWeight = it.bagTareWeight.toString().trim()
                it.storageLocation = it.storageLocationCode.toString().trim()
                it.weighBridgeType =
                    if (it.weighBridgeType.isNullOrEmpty()) weighBridgeDetails.weighBridgeType else it.weighBridgeType
            }
            trucNo = lotDetails.vehicleNumber
            wbId = lotDetails.weighBridgeId
            batchNo = lotDetails.batchNumber
            isData = it.getBoolean(IS_PARAMS_VALUE, false)
            materialNo =
                if (!lotDetails.materialCode?.length?.equals(18)!!) "000000".plus(lotDetails.materialCode) else lotDetails.materialCode
            netWeight = lotDetails.netWeight
            tarWeight = lotDetails.bagWeight
            challanNo = lotDetails.challan
            itemValue = lotDetails.item
            wbType = lotDetails.weighBridgeType

            lotbatchNo = lotDetails.batchNumber
            lotmaterialNo = lotDetails.materialCode
        }
        binding.tvParamsWeighBID.text = trucNo

        vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })
        if (isData!!)
            materialNo?.let {
                if (!copiedWbid.equals("null") && copiedWbid.isNotEmpty())
                    vm.getQualityParams(copiedMaterial, isData, copiedWbid)
                else
                    vm.getQualityParams(materialNo!!, isData, wbId)
            }
        else vm.getPreSamplingQualitydata(lotbatchNo?.trim()!!, lotmaterialNo?.trim()!!)
        materialNo?.let {
            vm.getQualityParams(materialNo!!, isData, wbId)
            vm.qualitylist.observe(viewLifecycleOwner, Observer {
                qualityMasterData = it.filter { it.qualityParameter.materialCode == materialNo }.toMutableList()
                updateUI(it)
            })
        }
        vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })

        binding.etBatchNo.setText(batchNo, TextView.BufferType.EDITABLE)
        binding.etBatchNo.isEnabled = batchNo.isNullOrEmpty()
        if (wbType.equals(PROCURE)) {
            binding.btnReject.visible()
            binding.btnAccept.visible()
            binding.btnParamsProceed.gone()
            binding.tvType.text = SUPPLIER
        } else {
            binding.btnReject.gone()
            binding.btnAccept.gone()
            binding.btnParamsProceed.visible()
            binding.tvType.text = MTNR
        }
        enableProceedBtn(weighBridgeDetails.status)

    }

    private fun enableProceedBtn(status: Int?) {

        when (status) {
            4 -> {
                binding.btnParamsProceed.isEnabled = false
                binding.btnAccept.isEnabled = false
                binding.btnReject.isEnabled = false
                ViewCompat.setBackgroundTintList(
                    binding.btnParamsProceed,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.grey) }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnAccept,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.grey) }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnReject,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.grey) }
                )
            }
            else -> {
                binding.btnParamsProceed.isEnabled = true
                binding.btnAccept.isEnabled = true
                binding.btnReject.isEnabled = true
                ViewCompat.setBackgroundTintList(
                    binding.btnParamsProceed,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                        )
                    }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnAccept,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                        )
                    }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnReject,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.red) }
                )
            }
        }
    }

    private fun proceeToPost(finalApproval: String, msg: Int) {
        var isValueNeed = true
        batchNo1 = binding.etBatchNo.text.toString()
        qualityParameterList.clear()
        val missedPos = mutableListOf<Int>()
        var data = arrayListOf<VegaQualityParamsWithQualitative?>()
        data = mAdapter.getItems()
        mAdapter.getItems().forEachIndexed { index, itValue ->
            if ((itValue?.qualityParameter?.entryObligatory.equals("X")) || (itValue?.qualityParameter?.vegaMandatory.equals(
                    "X"
                )) || (!itValue?.qualityParameter?.preSampling.equals(""))
            ) {
                if ((itValue?.qualityParameter?.vegaValueMandatory.equals("X"))) {
                    if (itValue?.qualityParameter?.qualityParameterValue.isNullOrEmpty()) {
                        if (itValue?.qualityParameter?.formulaParam.equals("X")) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else {
                            isValueNeed = false
                            missedPos.add(index)
                            itValue?.qualityParameter?.mandatory = 1
                        }
                    }
                } else {
                    itValue?.qualityParameter?.mandatory = 0
                }
                qualityParameterList.add(itValue?.qualityParameter)
            }
        }
        if (isValueNeed) {
            qualityParameterList.forEach { quality ->
                qualityMasterData.forEach {
                    if (!it.qualitative.isNullOrEmpty()) {
                        it.qualitative?.forEach { item ->
                            if (quality?.nameChar == item.nameChar && quality.qualityParameterValue == item.descValue) {
                                quality.qualityParameterValue = item.charValue
                            }
                        }
                    }
                }
            }

            moveToSummaryScreen(batchNo!!, msg, finalApproval)
        } else {
            showSnack(requireContext().resources.getString(R.string.enter_all_mandatory_fields))
            mAdapter.updateMissedPos(missedPos, data)
        }
    }

    private fun moveToSummaryScreen(batchNo: String, msg: Int, finalApproval: String) {
        mListener.onMtnrProceed(qualityParameterList, wbId, batchNo, finalApproval, lotItems, lotDetails, "MTNR")
    }

    private fun updateUI(params: List<VegaQualityParamsWithQualitative>?) {
        params?.let {
            when (it.isNotEmpty()) {
                true -> {

                    val value = mutableListOf<VegaQualityParamsWithQualitative>()
                    if (!isData!!) {
                        it.forEach { item ->
                            preQualityList.forEach { item1 ->
                                if ((item.qualityParameter.nameChar == item1.sapQCName) && !(item.qualityParameter.preSampling.equals(
                                        "X"
                                    ))
                                ) {
                                    if (item.qualitative!!.isNotEmpty())
                                        item.qualityParameter.qualityParameterValue = item1.satNam!!
                                    else
                                        item.qualityParameter.qualityParameterValue = item1.satNam!!.split(" ")[0]

                                    value.add(item)
                                }
                            }
                        }
                    } else {
                        value.addAll(it)
                    }

                    mAdapter.addItems(
                        sortByListOfItems(value.distinctBy { it.qualityParameter.nameChar }),
                        tarWeight,
                        netWeight,
                        challanNo
                    )
                }
                else -> setErrorContentView("Quality params not available for this material")
            }
        }
    }


    private fun updatePreQuality(response: Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            preQualityList = it.data?.data?.get(0)?.qualityParameters as MutableList<VegaQualityParams>
                            materialNo?.let { vm.getQualityParams(materialNo!!, isData, wbId) }
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }



    private fun enableProceedBtn(item: List<VegaQualityParamsWithQualitative?>) {
        var isEnable = false
        item.forEach {
            it?.qualityParameter?.qualityParameterValue?.let { it1 -> if (it1.isNotEmpty()) isEnable = true }
        }
        if (isEnable) {
            binding.btnParamsProceed.isEnabled = true
            binding.btnParamsProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btnParamsProceed.isEnabled = false
            binding.btnParamsProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }
    }



    private fun updateUI(response: Resource<GenericReqAndResp<VegaCameroonQualityParamPost>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            moveToSuccessPage(
                                it.data?.data?.grnNumber
                            )


                        }
                        else -> {
                            showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                            it.data?.data?.let {
                                it.lotDetails?.let {
                                    it.forEach {
                                        it.let { it1 ->
                                            lotItems.forEach { it2 ->
                                                if (it.batchNumber.equals(it1.batchNumber)) it2.qualityFlag =
                                                    it1.qualityFlag
                                            }
                                        }
                                    }
                                }
                            }

                        }
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



    private fun moveToSuccessPage( grnNo: String?) {
        val intent = Intent(activity, SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.quality_success))
        if (!grnNo.isNullOrEmpty())
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.new_lot_id_created_mtnr).plus(lotDetails.batchNumber).plus("\n GRN No : ")
                    .plus(grnNo)
            )
        else if (grnNo.isNullOrEmpty())
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.new_lot_id_created_mtnr).plus(lotDetails.batchNumber)
            )
        else
            intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id).plus(lotDetails.weighBridgeId))

        val lotlist = ArrayList<VegaCoffeeSalesLots>()
        lotlist.add(
            VegaCoffeeSalesLots(
                "",
                lotDetails.batchNumber,
                weighBridgeDetails.materialCode.toString(),
                weighBridgeDetails.materialName.toString(),
                "",
                "",
                "",
                "",
                "",
                weighBridgeDetails.unitsOfMeasure,
                "",
                weighBridgeDetails.netWeight


            )
        )
        intent.putExtra("fromcoffee", true)
        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotlist)
        intent.putExtra(AppUtils.PRINT_ENABLE, true)
        startActivity(intent)
        activity?.finish()
    }
}
