package com.olam.warehouse.odquality.ui.params

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.dorigin.entity.DOQualityParameter
import com.olam.warehouse.master.dorigin.model.DOQualityParamsWithQualitative
import com.olam.warehouse.odquality.R
import com.olam.warehouse.odquality.data.domain.model.DOQualitySavedResponse
import com.olam.warehouse.odquality.databinding.FragmentDoQualityParamsBinding
import com.olam.warehouse.odquality.ui.DOQualityViewModel
import com.olam.warehouse.odquality.utils.*
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import kotlinx.android.synthetic.main.fragment_do_quality_params.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
class DOQualityParameterFragment : BaseFragment() {

    private var wbId: String? = ""
    private var batchNo1: String? = ""
    private var batchNo: String? = ""
    private var isData: Boolean? = false
    private var materialNo: String? = ""
    private var netWeight: String? = ""
    private var grossWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var avgBagWeight: String? = ""
    private var batchNumber: String? = ""
    private var plantId: String? = ""
    private var qcStatus: String? = ""
    private var unitsOfMeasure: String? = ""
    private var qualityParameterList = arrayListOf<DOQualityParameter?>()
    private var mAdapter = DOQualityParamsAdapter { enableProceedBtn(it) }
    private var isSort: Boolean = false
    private var isAscendingSort: Boolean = true

    private val vm: DOQualityViewModel by viewModel()
    private lateinit var mListener: OnParamsListener

    interface OnParamsListener {
        fun onParamsSave(qualityParameter: ArrayList<DOQualityParameter?>, wbId: String?, batchNo: String)
        fun onParamsProceed(qualityParameter: ArrayList<DOQualityParameter?>, wbId: String?, batchNo: String)
    }

    companion object {
        fun newInstance() = DOQualityParameterFragment().putArgs {
        }
    }

    private lateinit var binding: FragmentDoQualityParamsBinding
    override val layoutResourceId = R.layout.fragment_do_quality_params

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnParamsListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDoQualityParamsBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odquality/ui/params/DOQualityParameterFragment").title("OD/Quality")
            .with(tracker)
        initUI()
        initExtra()
    }

    // save = 0, proceed = 1
    private var saveOrProceed = -1

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnParamsProceed, it, true)
            getActionBtnChangedView(binding.btnParamsSave, it, true)
        }
        binding.rvBagDetail.layoutManager = LinearLayoutManager(this.context)
        mAdapter.setHasStableIds(true)
        binding.rvBagDetail.adapter = mAdapter

        binding.btnParamsSave.setOnClickListener {
            saveOrProceed = 0
            batchNo1 = binding.etBatchNo.text.toString()
            qualityParameterList.clear()
            mAdapter.getItems().forEachIndexed { index, it ->
                if (it?.qualityParameter?.entryObligatory?.contains("X")!! || it.qualityParameter.doMandatory?.contains(
                        "X"
                    )!!
                )
                    if (it.qualityParameter.qualityParameterValue.isNullOrEmpty()) {
                        it.qualityParameter.mandatory = 1
                    } else {
                        it.qualityParameter.mandatory = 0
                    }

                qualityParameterList.add(it.qualityParameter)
            }

            showConfirmDialog(batchNo1!!, saveOrProceed)
        }

        binding.btnParamsProceed.setOnClickListener {
            saveOrProceed = 1
            var isValueNeed = true
            batchNo1 = binding.etBatchNo.text.toString()
            qualityParameterList.clear()
            mAdapter.isProceedBtnClicked = true

            val missedPos = mutableListOf<Int>()
            mAdapter.getItems().forEachIndexed { index, it ->
                if (it?.qualityParameter?.entryObligatory?.contains("X")!! || it.qualityParameter.doMandatory?.contains("X")!!)
                    if (it.qualityParameter.qualityParameterValue.isNullOrEmpty()
                        || (it.qualityParameter.qualityParameterValue != ""
                                && it.qualityParameter.qualityParameterValue?.toDoubleOrNull() != null
                                && it.qualityParameter.nameChar != "ID_BROKENWEIGHT"
                                && it.qualityParameter.nameChar != "ID_BROKENPERCENT"
                                && it.qualityParameter.nameChar != "GTPESTAR"
                                && isMandatory(it.qualityParameter.qualityParameterValue.toString(),it.qualityParameter.dataType.toString()))){

                        isValueNeed = false
                        missedPos.add(index)
                        it.qualityParameter.mandatory = 1
                    } else {
                        it.qualityParameter.mandatory = 0
                    }
                qualityParameterList.add(it.qualityParameter)
             }
            val validDataList =mAdapter.getItems().filter { data -> data?.qualityParameter?.nameChar.equals("GTRM0027") || data?.qualityParameter?.nameChar.equals("GTRM0026") || data?.qualityParameter?.nameChar.equals("GTRM0021")|| data?.qualityParameter?.nameChar.equals("GTRM0023")|| data?.qualityParameter?.nameChar.equals("GTRM0070") }
            val totalQuantity =validDataList.sumBy { it?.qualityParameter?.qualityParameterValue?.toInt()?:0 }

            if (isValueNeed)
                if(validDataList.size==5 && totalQuantity!=100)
                    context?.toast(getString(R.string.enter_lesser_or_greater))
                else{
                    showConfirmDialog(batchNo1!!, saveOrProceed)
                }
            else {
                context?.toast(getString(R.string.enter_all_mandatory_fields))
                mAdapter.updateMissedPos(missedPos, mAdapter.getItems())
            }
        }

        binding.tvFilter.setOnClickListener {
            if (!isSort) {
                mAdapter.updateFilter(mAdapter.getItems())
                isSort = true
            }
        }
        binding.ivSortDownUp.setOnClickListener {
            mAdapter.sortAlphabetically(mAdapter.getItems(), isAscendingSort)
            isAscendingSort = !isAscendingSort
            isSort = false
        }
    }

    private fun isNotString() {}

    private fun initExtra() {
        arguments?.let {
            wbId = it.getString(WEIGHBRIDGE_LIST)
            batchNo = it.getString(BATCH_NO)
            isData = it.getBoolean(IS_PARAMS_VALUE, false)
            materialNo = it.getString(MATERIAL_NO)
            netWeight = it.getString(NET_WEIGHT)
            grossWeight = it.getString(GROSS_WEIGHT)
            tarWeight = it.getString(TAR_WEIGHT)
            challanNo = it.getString(CHALLAN)
            avgBagWeight = it.getString(AVG_BAG_WEIGHT)
            unitsOfMeasure = it.getString(UOM)
            batchNumber = it.getString(BATCH_NUMBER)
            plantId = it.getString(PLANT_ID)
            qcStatus = it.getString(QC_STATUS)
        }
        binding.tvParamsWeighBID.text = wbId

        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        if (currentKey.startsWith("DO_ID")) {
            if (qcStatus == "R") {
                btnParamsProceed.visibility = View.VISIBLE
            } else {
                btnParamsProceed.visibility = View.GONE
            }
            btnParamsSave.visibility = View.VISIBLE
        } else {
            btnParamsSave.visibility = View.GONE
            btnParamsProceed.visibility = View.VISIBLE
        }

        if (qcStatus == "R") {
            showCustomLoading()
            vm.fetchSavedWeighBridgeDetail(plantId!!, materialNo!!, batchNumber!!)
            vm.qualitySaved.observe(viewLifecycleOwner, Observer { response ->
                response.let { savedResponse ->
                    hideCustomLoading()
                    when (savedResponse.status) {
                        Resource.Status.SUCCESS -> {
                            when (savedResponse.data?.success) {
                                true -> {
                                    materialNo?.let {
                                        vm.getQualityParams(materialNo!!, isData, wbId)
                                        vm.qualitylist.observe(viewLifecycleOwner, Observer { param ->
                                            updateUI(param, savedResponse?.data?.data!!)
                                        })
                                    }
                                } else -> activity?.toast("Error ${savedResponse.data?.message}")
                            }
                        }
                        Resource.Status.ERROR -> {
                            activity?.toast("${savedResponse.error}")
                        }
                        Resource.Status.LOADING -> {
                        }
                    }
                }
            })
        } else {
            materialNo?.let {
                vm.getQualityParams(materialNo!!, isData, wbId)
                vm.qualitylist.observe(viewLifecycleOwner, Observer {
                    updateUI(it)
                })
            }
        }
        binding.etBatchNo.setText(batchNo, TextView.BufferType.EDITABLE)
        binding.etBatchNo.isEnabled = batchNo.isNullOrEmpty()
    }

    private fun updateUI(params: List<DOQualityParamsWithQualitative>?, savedQualityData: DOQualitySavedResponse? = null) {
        hideCustomLoading()
        params?.let {
            it.forEach { item -> item.qualitative = item.qualitative?.filter { it.materialCode.equals(materialNo) } }
            when (it.isNotEmpty()) {
                true -> mAdapter.addItems(sortByListOfItems(it), tarWeight, netWeight, grossWeight, challanNo, avgBagWeight, unitsOfMeasure, savedQualityData)
                else -> {
                    binding.llContentView.gone()
                    binding.btnParamsProceed.gone()
                    binding.tvParamsNotAvailable.visible()
                    setErrorContentView(getString(R.string.quality_params_not_available))
                }
            }
        }
    }

    private fun showConfirmDialog(batchNo: String, saveOrProceed: Int) {
        var spannedText = if (saveOrProceed == 0) getString(R.string.save) else getString(R.string.proceed)
        var title = if (saveOrProceed == 0) R.string.save_Quty_message else R.string.confirm_Quty_message
        MaterialDialog(requireContext()).show {
            message(title)
            getMetirialCustomView(
                this,
                spannedText,
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (saveOrProceed == 0) {
                        mListener.onParamsSave(qualityParameterList, wbId, batchNo)
                    } else if (saveOrProceed == 1) {
                        mListener.onParamsProceed(qualityParameterList, wbId, batchNo)
                    }
                },
                { dismiss() })
        }
    }

    private fun enableProceedBtn(item: List<DOQualityParamsWithQualitative?>) {
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

    fun isMandatory(mMandatory: String,mDataType: String): Boolean {
        if (mMandatory != null && !mMandatory.trim().isEmpty() && mDataType.trim().equals("NUM")&& mMandatory.toDouble() >=0.0 ){
            return false
        }else if(mMandatory != null && !mMandatory.trim().isEmpty() && mDataType.trim().equals("CHAR")){
            return false
         }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}
