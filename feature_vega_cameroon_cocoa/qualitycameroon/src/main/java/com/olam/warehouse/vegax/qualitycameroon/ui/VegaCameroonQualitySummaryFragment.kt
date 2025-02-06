package com.olam.warehouse.vegax.qualitycameroon.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitycameroon.R
import com.olam.warehouse.vegax.qualitycameroon.databinding.FragmentVegaCameroonQualitySummaryBinding
import com.olam.warehouse.vegax.qualitycameroon.databinding.ItemVegaCameroonQualitySummaryParamsBinding
import com.olam.warehouse.vegax.qualitycameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaCameroonQualitySummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_cameroon_quality_summary

    private lateinit var binding: FragmentVegaCameroonQualitySummaryBinding
    private lateinit var mListener: OnSummaryParamsListener
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var wbid: String? = ""
    private var batchNo: String? = ""
    private var finalApproval: String? = ""
    private var challanNo: String? = ""
    private var flag: String? = ""
    private var material: String = ""
    private var lotItems = arrayListOf<VegaCoffeeLot>()
    private var lotDetails = VegaCoffeeLot()
    private val vm: VegaCameroonQualityViewModel by viewModel()
    private var isData: Boolean? = false
    private var qualityMasterData = mutableListOf<VegaQualityParamsWithQualitative>()

    interface OnSummaryParamsListener {
        fun onParamsProceed(
            qualityParameter: ArrayList<VegaQualityParameter?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String,
            challan: String
        )

        fun onMtnrParamsProceed(
            qualityParameter: ArrayList<VegaQualityParameter?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String,
            lotItems: ArrayList<VegaCoffeeLot>,
            lotDetails: VegaCoffeeLot
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCameroonQualitySummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    companion object {
        fun newInstance() = VegaCameroonQualitySummaryFragment().putArgs {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualitycameroon/ui/VegaCameroonQualitySummaryFragment")
            .title("Vega_Cameroon/Quality").with(tracker)
        initUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnSummaryParamsListener
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnOkApprove, it, true)
        }
        wbid = arguments?.getString(WB_ID)
        batchNo = arguments?.getString(BATCH_NO)
        finalApproval = arguments?.getString(FINAL_APPROVAL)
        challanNo = arguments?.getString(CHALLAN)
        flag = arguments?.getString(FLAG)
        qualityParameterList =
            arguments?.getParcelableArrayList<VegaQualityParameter>(QUALITY_LIST) as ArrayList<VegaQualityParameter?>
        if (flag == "MTNR") {
            lotItems =
                arguments?.getParcelableArrayList<VegaCoffeeLot>("LOT_LIST") as ArrayList<VegaCoffeeLot>
            lotDetails = arguments?.getParcelable("LOT_DETAILS")!!
            material = "000000".plus(lotDetails.materialCode!!)
        }
        else{
            material = qualityParameterList[0]?.materialCode.toString()
        }
        vm.getQualityParams(material, isData, wbid)
        vm.qualitylist.observe(viewLifecycleOwner, Observer { it ->
            qualityMasterData = it.toMutableList()

            var list = qualityParameterList.filter { it1 -> it1?.nameChar != "B_GRNQTY1" }
            setUpAdapter(list as ArrayList<VegaQualityParameter?>)
        })


        binding.tvApproveParamsWeighBID.text = challanNo
        binding.tvBatchNo.text = batchNo

        binding.btnOkApprove.setOnClickListener { proceedToPost(FNQUALITY, R.string.confirm_quality_message) }

    }


    private fun setUpAdapter(data: ArrayList<VegaQualityParameter?>) {
        data.let {
            var i = 0
            binding.rvApproveQuality.setUpAdapter(
                data,
                R.layout.item_vega_cameroon_quality_summary_params,
                ItemVegaCameroonQualitySummaryParamsBinding::inflate,
                { item, pos, bindItem ->
                    i++
                    if (i % 2 == 0) {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                    } else {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                    }

                    bindItem.tvQualityNameApprove.text = item?.descrChar

                    var spinnerItem = qualityMasterData.filter { it.qualitative?.size ?: 0 > 0 }

                    var slectedItem =
                        spinnerItem.filter { it.qualityParameter.descrChar == item?.descrChar }

                    if (item?.qualitative?.size!! > 0) {
                        bindItem.tvUnitApprove.text = item.qualityParameterValue
                    } else
                        bindItem.tvUnitApprove.text = item.qualityParameterValue
                    if (slectedItem.size > 0) {
                        var i =
                            slectedItem[0].qualitative?.filter { it.charValue == item.qualityParameterValue }

                        bindItem.tvUnitApprove.text = i?.get(0)?.descValue
                    }

                },
                {

                })
        }
    }

    private fun proceedToPost(finalApproval: String, msg: Int) {
        showConfirmDialog(batchNo!!, msg, finalApproval)
    }

    private fun showConfirmDialog(batchNo: String, msg: Int, finalApproval: String) {
        MaterialDialog(requireContext()).show {
            message(msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    //calculatePaidWeight()
                    if (flag == "SUPPLIER")
                        mListener.onParamsProceed(
                            qualityParameterList,
                            wbid,
                            batchNo,
                            finalApproval,
                            challanNo.toString()
                        )
                    else
                        mListener.onMtnrParamsProceed(
                            qualityParameterList,
                            wbid,
                            batchNo,
                            finalApproval,
                            lotItems,
                            lotDetails
                        )

                },
                { dismiss() })
        }
    }

}
