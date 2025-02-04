package com.olam.warehouse.vegax.qualitysesame.ui.params

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils.getFormattedCurrentDate
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitysesame.R
import com.olam.warehouse.vegax.qualitysesame.databinding.FragmentVegaNigeriaSesameQualityParamsBinding
import com.olam.warehouse.vegax.qualitysesame.ui.VegaNigeriaSesameQualityViewModel
import com.olam.warehouse.vegax.qualitysesame.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
class VegaNigeriaSesameQualityParameterFragment : BaseFragment() {

    private var spottedCashewQualityDetails: VegaQualityParameter = VegaQualityParameter()
    private var goodKernalQualityDetails: VegaQualityParameter = VegaQualityParameter()
    private var immatureCashewQualityDetails: VegaQualityParameter = VegaQualityParameter()
    private var wbId: String? = ""
    private var bagCount: String? = ""
    private var plant: String? = ""
    private var batchNo: String? = ""
    private var isData: Boolean? = false
    private var materialNo: String? = ""
    private var netWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var itemValue: String? = ""
    private var wbType: String? = ""
    private var weighBridgeDetails = VegaQualityWBDetails()
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var mAdapter = VegaNigeriaSesameQualityParamsAdapter { enableProceedBtn(it) }
    private var isSort: Boolean = false
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private val vm: VegaNigeriaSesameQualityViewModel by viewModel()
    private lateinit var mListener: OnParamsListener
    private var paidWeight: String = ""
    private var grnQty: Double = 0.0
    private var refraction: Double = 0.0
    private var discWeight: Double = 0.0
    private var grnNumber: String? = ""
    private var immatureValue: Double = 0.0
    private var spottedValue: Double = 0.0
    private var goodKernelValue: Double = 0.0
    private var korValue: Double = 0.0
    private var kor: String = ""
    private var currentKey = getCurrentKey()

    interface OnParamsListener {
        fun onParamsProceed(
            qualityParameter: ArrayList<VegaQualityParameter?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String,
            kor: String
        )
    }

    companion object {
        fun newInstance() = VegaNigeriaSesameQualityParameterFragment().putArgs {}
    }

    private lateinit var binding: FragmentVegaNigeriaSesameQualityParamsBinding
    override val layoutResourceId = R.layout.fragment_vega_nigeria_sesame_quality_params

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
        binding = FragmentVegaNigeriaSesameQualityParamsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualitysesame/ui/params/VegaNigeriaSesameQualityParameterFragment")
            .title("Ecuador Quality").with(tracker)
        initUI()
        initExtra()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnAccept, it, true)
            getActionBtnChangedView(binding.btnParamsProceed, it, true)
        }
        binding.rvBagDetail.layoutManager = LinearLayoutManager(this.context)
        binding.rvBagDetail.adapter = mAdapter

        if (currentKey.split("_")[2].contains("CASH")) {

            //        binding.btnParamsProceed.setOnClickListener {
//            proceedToPost(
//                FNQUALITY,
//                R.string.confirm_quality_message
//            )
//        }
//        binding.btnAccept.setOnClickListener {
//            proceedToPost(
//                FNQUALITY,
//                R.string.confirm_quality_message
//            )
//        }
//        binding.btnReject.setOnClickListener {
//            proceedToPost(
//                FNREJECT,
//                R.string.confirm_reject_message
//            )
//        }

        } else {

            binding.btnParamsProceed.setOnClickListener {
                proceedToPost(
                    FNQUALITY,
                    R.string.confirm_quality_message
                )
            }
            binding.btnAccept.setOnClickListener {
                proceedToPost(
                    FNQUALITY,
                    R.string.confirm_quality_message
                )
            }
            binding.btnReject.setOnClickListener {
                proceedToPost(
                    FNREJECT,
                    R.string.confirm_reject_message
                )
            }

        }


    }

    private fun initExtra() {
        arguments?.let {
            weighBridgeDetails = it.getParcelable(WEIGHSCALE)!!
            wbId = weighBridgeDetails.weighBridgeId
            batchNo = weighBridgeDetails.batchNumber
            isData = it.getBoolean(IS_PARAMS_VALUE, false)
            materialNo = weighBridgeDetails.materialCode
            netWeight = weighBridgeDetails.netWeight
            tarWeight = weighBridgeDetails.bagWeight
            challanNo = weighBridgeDetails.challan
            itemValue = weighBridgeDetails.item
            wbType = weighBridgeDetails.weighBridgeType
            bagCount = weighBridgeDetails.bagCount
            plant = weighBridgeDetails.plant
            grnNumber = weighBridgeDetails.grnNumber
        }
        binding.tvParamsWeighBID.text = getString(R.string.wb_id).plus(" ").plus(wbId)
        materialNo?.let {
            vm.getQualityParams(materialNo!!, isData, wbId)
            vm.qualitylist.observe(viewLifecycleOwner, Observer {
                updateUI(it)
            })
        }
        vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })

        if (wbType.equals(PROCURE)) {
            binding.tvType.text = (" ").plus(getString(R.string.supplier))
        } else {
            binding.tvType.text = MTNR
        }
        enableProceedBtn(weighBridgeDetails.status)

        binding.edImmatureValue.onChange {
            if (it.length > 0) {
                immatureValue = it.toDouble()
                if (immatureValue != 0.00)
                    calculateKorValue()
            } else
                binding.edKorValue.setText("")
        }

        binding.edSpottedValue.onChange {
            if (it.length > 0) {
                spottedValue = it.toDouble()
                if (spottedValue != 0.00)
                    calculateKorValue()
            } else
                binding.edKorValue.setText("")
        }

        binding.edGoodKernelValue.onChange {
            if (it.length > 0) {
                goodKernelValue = it.toDouble()
                if (goodKernelValue != 0.00)
                    calculateKorValue()
            } else
                binding.edKorValue.setText("")

        }
    }

    private fun calculateKorValue() {
        if (immatureValue != 0.00 && spottedValue != 0.00 && goodKernelValue != 0.00) {
            korValue = (((immatureValue + spottedValue) / 2 + goodKernelValue) * 0.176)
            binding.edKorValue.setText(korValue.toString())
        }
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
                if (currentKey.split("_")[2].contains("CASH")) {
                    //
                } else {
                    binding.btnParamsProceed.isEnabled = true
                    binding.btnAccept.isEnabled = true
                    binding.btnReject.isEnabled = true
                    ViewCompat.setBackgroundTintList(
                        binding.btnParamsProceed,
                        context?.let {
                            ContextCompat.getColorStateList(
                                it,
                                if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green
                            )
                        }
                    )
                ViewCompat.setBackgroundTintList(
                    binding.btnAccept,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green
                        )
                    }
                )
                    ViewCompat.setBackgroundTintList(
                        binding.btnReject,
                        context?.let {
                            ContextCompat.getColorStateList(
                                it,
                                com.olam.warehouse.presentation.R.color.red
                            )
                        }
                    )
                }
            }


        }

    }

    private fun proceedToPost(finalApproval: String, msg: Int) {
        var isValueNeed = true
        qualityParameterList.clear()
        val missedPos = mutableListOf<Int>()
        var data = arrayListOf<VegaQualityParamsWithQualitative?>()
        data = mAdapter.getItems()
        mAdapter.getItems().forEachIndexed { index, itValue ->
            if ((itValue?.qualityParameter?.entryObligatory.equals("X")) || (!itValue?.qualityParameter?.preSampling.isNullOrEmpty())
            ) {
                if (itValue?.qualityParameter?.qualityParameterValue.isNullOrEmpty()) {
                    if (itValue?.qualityParameter?.formulaParam.equals("X")) {
                        itValue?.qualityParameter?.mandatory = 0
                    } else {
                        isValueNeed = false
                        missedPos.add(index)
                        itValue?.qualityParameter?.mandatory = 1
                    }

                } else {
                    itValue?.qualityParameter?.mandatory = 0
                }
            }
            qualityParameterList.add(itValue?.qualityParameter)
        }
        if (isValueNeed)
            showConfirmDialog(batchNo!!, msg, finalApproval)
        else {
            showSnack(requireContext().resources.getString(R.string.enter_all_mandatory_fields))
            mAdapter.updateMissedPos(missedPos, data)
        }
    }

    private fun updateUI(params: List<VegaQualityParamsWithQualitative>?) {
        params?.let {
            when (it.isNotEmpty()) {
                true -> {
                    val value = mutableListOf<VegaQualityParamsWithQualitative>()
                    if (!isData!!) {
                        it.forEach { item ->
                            if (item.qualityParameter.vegaMandatory.equals("X") || item.qualityParameter.entryObligatory.equals(
                                    "X"
                                ) || (!item.qualityParameter.preSampling.equals("") && item.qualityParameter.qualityParameterValue?.isNotEmpty()!!)
                            ) {
                                value.add(item)
                            }
                            preQualityList.forEach { item1 ->
                                if (item.qualityParameter.nameChar == item1.sapQCName) {
                                    item.qualityParameter.qualityParameterValue =
                                        item1.satNam!!.split(" ")[0]
                                    value.add(item)
                                }
                            }
                        }
                    } else {
                        it.forEach { item ->
                            value.addAll(it)
                        }

                    }
                    value.forEach {
                        if (it.qualityParameter.nameChar == "ZNG_GR_DATE")
                            it.qualityParameter.qualityParameterValue = getFormattedCurrentDate()
                    }
                    mAdapter.addItems(
                        sortByListOfItems(value.distinctBy { it.qualityParameter.nameChar }),
                        tarWeight,
                        netWeight,
                        challanNo,
                        bagCount,
                        plant,
                        isData!!
                    )

                    binding.btnParamsProceed.isEnabled = true
                    binding.btnAccept.isEnabled = true
                    binding.btnReject.isEnabled = true
                    ViewCompat.setBackgroundTintList(
                        binding.btnParamsProceed,
                        context?.let {
                            ContextCompat.getColorStateList(
                                it,
                                if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green
                            )
                        }
                    )
                    ViewCompat.setBackgroundTintList(
                        binding.btnAccept,
                        context?.let {
                            ContextCompat.getColorStateList(
                                it,
                                if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green
                            )
                        }
                    )
                    ViewCompat.setBackgroundTintList(
                        binding.btnReject,
                        context?.let {
                            ContextCompat.getColorStateList(
                                it,
                                com.olam.warehouse.presentation.R.color.red
                            )
                        }
                    )

                    binding.btnParamsProceed.setOnClickListener {
                        proceedToPost(
                            FNQUALITY,
                            R.string.confirm_quality_message
                        )
                    }
                    binding.btnAccept.setOnClickListener {
                        proceedToPost(
                            FNQUALITY,
                            R.string.confirm_quality_message
                        )
                    }
                    binding.btnReject.setOnClickListener {
                        proceedToPost(
                            FNREJECT,
                            R.string.confirm_reject_message
                        )
                    }


                }
                else -> {
                    setErrorContentView(getString(R.string.quality_params_not_available))

                    binding.btnParamsProceed.isEnabled = false
                    binding.btnAccept.isEnabled = false
                    binding.btnReject.isEnabled = false
                    ViewCompat.setBackgroundTintList(
                        binding.btnParamsProceed,
                        context?.let {
                            ContextCompat.getColorStateList(
                                it,
                                com.olam.warehouse.presentation.R.color.grey
                            )
                        }
                    )
                    ViewCompat.setBackgroundTintList(
                        binding.btnAccept,
                        context?.let {
                            ContextCompat.getColorStateList(
                                it,
                                com.olam.warehouse.presentation.R.color.grey
                            )
                        }
                    )
                    ViewCompat.setBackgroundTintList(
                        binding.btnReject,
                        context?.let {
                            ContextCompat.getColorStateList(
                                it,
                                com.olam.warehouse.presentation.R.color.grey
                            )
                        }
                    )
                }


            }


        }
    }

    /*private fun updateUI(params: List<VegaQualityParamsWithQualitative>?) {
        params?.let {
            when (it.isNotEmpty()) {
                true -> {
                    val value = mutableListOf<VegaQualityParamsWithQualitative>()
                    if (!isData!!) {
                        it.forEach { item ->
                            if (item.qualityParameter.vegaMandatory.equals("X") || item.qualityParameter.entryObligatory.equals(
                                    "X"
                                ) || (!item.qualityParameter.preSampling.equals("") && item.qualityParameter.qualityParameterValue?.isNotEmpty()!!)
                            ) {
                                if (getCurrentKey().split("_")[2].contains("CASH")) {
                                    when (item.qualityParameter.nameChar) {
                                        "ZNG_CASHEW_GOOD_KERNEL" ->
                                            goodKernalQualityDetails = item.qualityParameter
                                        "ZNG_CASHEW_SPOTTED_GMS" ->
                                            spottedCashewQualityDetails = item.qualityParameter
                                        "ZNG_CASHEW_IMMATURE_GMS" ->
                                            immatureCashewQualityDetails = item.qualityParameter
                                    }
                                    if (!item.qualityParameter.nameChar.equals("ZNG_CASHEW_GOOD_KERNEL") && !item.qualityParameter.nameChar.equals(
                                            "ZNG_CASHEW_SPOTTED_GMS"
                                        )
                                        && !item.qualityParameter.nameChar.equals("ZNG_CASHEW_IMMATURE_GMS") && !item.qualityParameter.nameChar.equals(
                                            "B_GRNQTY1"
                                        )
                                    ) {
                                        value.add(item)
                                    }

                                } else {
                                    value.add(item)
                                }

                            }
                            preQualityList.forEach { item1 ->
                                if (item.qualityParameter.nameChar == item1.sapQCName) {
                                    item.qualityParameter.qualityParameterValue = item1.satNam!!.split(" ")[0]
                                    if (getCurrentKey().split("_")[2].contains("CASH")) {
                                        if (!item.qualityParameter.nameChar.equals("ZNG_CASHEW_GOOD_KERNEL") && !item.qualityParameter.nameChar.equals(
                                                "ZNG_CASHEW_SPOTTED_GMS"
                                            )
                                            && !item.qualityParameter.nameChar.equals("ZNG_CASHEW_IMMATURE_GMS") && !item.qualityParameter.nameChar.equals(
                                                "B_GRNQTY1"
                                            )
                                        ) {
                                            value.add(item)
                                        }
                                    } else {
                                        value.addAll(it)
                                    }
                                }
                            }

                        }
                    } else {
                        it.forEach { item ->
                            if (getCurrentKey().split("_")[2].contains("CASH")) {
                                if (!item.qualityParameter.nameChar.equals("ZNG_CASHEW_GOOD_KERNEL") && !item.qualityParameter.nameChar.equals(
                                        "ZNG_CASHEW_SPOTTED_GMS"
                                    )
                                    && !item.qualityParameter.nameChar.equals("ZNG_CASHEW_IMMATURE_GMS") && !item.qualityParameter.nameChar.equals(
                                        "B_GRNQTY1"
                                    )
                                ) {
                                    value.addAll(it)
                                }
                            } else {
                                value.addAll(it)
                            }
                        }

                    }
                    mAdapter.addItems(
                        sortByListOfItems(value.distinctBy { it.qualityParameter.nameChar }),
                        tarWeight,
                        netWeight,
                        challanNo,
                        bagCount,
                        plant,
                        isData!!
                    )
                    if (getCurrentKey().split("_")[2].contains("CASH")) {
                        binding.llParams.visibility = View.VISIBLE
                        val spannable: SpannableStringBuilder?
                        spannable = SpannableStringBuilder("Cashew Immature in gms")
                        if (!immatureCashewQualityDetails.numValFm.isNullOrEmpty()) {
                            spannable.insert(
                                spannable.length,
                                "\n(".plus(immatureCashewQualityDetails.numValFm?.trim()).plus(" - ").plus(immatureCashewQualityDetails.numValTo?.trim()).plus(")")
                            )
                            when {
                                immatureCashewQualityDetails.qualityParamLabel.isNullOrEmpty() -> {
                                    spannable.setSpan(
                                        ForegroundColorSpan(Color.LTGRAY),
                                        immatureCashewQualityDetails.descrChar?.length!! + 1,
                                        spannable.length,
                                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                                    )
                                }
                                else -> {
                                    spannable.setSpan(
                                        ForegroundColorSpan(Color.LTGRAY),
                                        immatureCashewQualityDetails.qualityParamLabel?.length!! + 1,
                                        spannable.length,
                                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                                    )
                                }
                            }
                        }

                        binding.tvImmatureLabel.setText(spannable)
                    }

                }
                else -> setErrorContentView(getString(R.string.quality_params_not_available))
            }
        }
    }*/

    private fun showConfirmDialog(batchNo: String, msg: Int, finalApproval: String) {
        MaterialDialog(requireContext()).show {
            message(msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    handleQualityPost(qualityParameterList, wbId, batchNo, finalApproval)
                },
                { dismiss() })
        }
    }

    private fun handleQualityPost(
        qualityParameterList: ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String
    ) {
        /*if (getCurrentKey().split("_")[2].contains("CASH")) {
            goodKernalQualityDetails.qualityParameterValue = goodKernelValue.toString()
            spottedCashewQualityDetails.qualityParameterValue = spottedValue.toString()
            immatureCashewQualityDetails.qualityParameterValue = immatureValue.toString()

            qualityParameterList.add(goodKernalQualityDetails)
            qualityParameterList.add(spottedCashewQualityDetails)
            qualityParameterList.add(immatureCashewQualityDetails)
        }*/
        qualityParameterList.forEach {
            if (it?.nameChar == "ZNG_CASHEW_KOR")
                kor = it.qualityParameterValue.toString()
        }
        mListener.onParamsProceed(qualityParameterList, wbId, batchNo, finalApproval, kor)

    }

    private fun calculatePaidWeight() {
        qualityParameterList.forEach { it ->
            if (it?.nameChar.equals("B_GRNQTY1")) {
                grnQty = it?.qualityParameterValue?.toDouble() ?: 0.0
            } else if (it?.nameChar.equals("B_SECONDARY_REFR")) {
                refraction = it?.qualityParameterValue?.toDouble() ?: 0.0
            }
        }
        discWeight = 100 - refraction
        paidWeight = ((discWeight * grnQty) / 100).formatThreeDigits()
    }

    private fun enableProceedBtn(item: List<VegaQualityParamsWithQualitative?>) {
        var isEnable = false
        item.forEach {
            it?.qualityParameter?.qualityParameterValue?.let { it1 -> if (it1.isNotEmpty()) isEnable = true }
        }
        if (isEnable) {
            binding.btnParamsProceed.isEnabled = true
            binding.btnParamsProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnParamsProceed.isEnabled = false
            binding.btnParamsProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }
    }
}
