package com.olam.warehouse.vegax.processingcameroon.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.observeOnce
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingcameroon.R
import com.olam.warehouse.vegax.processingcameroon.data.domain.model.VegaCameroonRMINShiftModel
import com.olam.warehouse.vegax.processingcameroon.databinding.FragmentCameroonRminShiftSelectionBinding
import com.olam.warehouse.vegax.processingcameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCameroonRMINShiftSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_cameroon_rmin_shift_selection
    private lateinit var binding: FragmentCameroonRminShiftSelectionBinding

    private var callBack: CallBack? = null
    private val vm: VegaCameroonRminViewModel by viewModel()
    private var model: VegaCoffeeRminProcessing? = null
    private var shift: String? = ""
    private var remark: String? = ""
    private var fromSummary: Boolean? = false
    private var shiftList = mutableListOf<String>()
    private var remarksList = mutableListOf<String>()
    private var jsonData = mutableListOf<String>()


    interface CallBack {
        fun replaceFragment(fragment: String, model: VegaCoffeeRminProcessing)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
    }

    companion object {
        fun newInstance(fromSummary: Boolean, model: VegaCoffeeRminProcessing) =
            VegaCameroonRMINShiftSelectFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
                putBoolean(FROM_SUMMARY, fromSummary)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCameroonRminShiftSelectionBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcameroon/ui/rmin/VegaCameroonRMINShiftSelectFragment").title("Vega_Cameroon/Processing")
            .with(tracker)
    }

    private fun initExtra() {
        model = arguments?.getParcelable(MODEL_BUNDLE)
        fromSummary = arguments?.getBoolean(FROM_SUMMARY)
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnProceed, it, true)
        }
        binding.btnProceed.setOnClickListener { validateField() }
        vm.getShiftRemarksItems("VEGA_CM_COCO_SAP")
        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer { updateShiftAndRemarks(it) })
        vm.rminModel.observeOnce(viewLifecycleOwner, Observer { initCurrentPositions(it) })
        binding.spRemarks.setText(model?.remark ?: "")
        shift = model?.shift
        remark = model?.remark

        binding.spRemarks.onChange {
            if (it.isNotEmpty()) {
                remark = it.toString()

            }
        }

        binding.rgShift.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbA -> {
                    updateRadioTextColor(first = true, second = false, third = false)
                    shift = "A"
                }
                R.id.rbB -> {
                    updateRadioTextColor(second = true, first = false, third = false)
                    shift = "B"
                }
                R.id.rbC -> {
                    updateRadioTextColor(third = true, first = false, second = false)
                    shift = "C"
                }
            }

        }
    }

    private fun validateField() {

        if(shift.toString().isEmpty())
            model?.shift = ""
        else
            model?.shift = shift
        if(remark.toString().isEmpty())
            model?.remark = ""
        else
            model?.remark = remark
        vm.saveRMINProcess(model!!)
        callBack?.replaceFragment(SUMMARY, model!!)

    }

    private fun initCurrentPositions(localData: VegaCoffeeRminProcessing?) {
        if (localData != null) {
            model?.remark = localData.remark
            model?.shift = localData.shift
        }
        updateCurrentShiftSelection(model?.shift ?: "")
    }

    private fun updateRadioTextColor(first: Boolean, second: Boolean, third: Boolean) {
        binding.rbC.setTextColor(
            getColor(
                if (third) com.olam.warehouse.presentation.R.color.black
                else com.olam.warehouse.presentation.R.color.grey_dark
            )
        )
        binding.rbB.setTextColor(
            getColor(
                if (second) com.olam.warehouse.presentation.R.color.black
                else com.olam.warehouse.presentation.R.color.grey_dark
            )
        )
        binding.rbA.setTextColor(
            getColor(
                if (first) com.olam.warehouse.presentation.R.color.black
                else com.olam.warehouse.presentation.R.color.grey_dark
            )
        )
    }



    private fun updateCurrentShiftSelection(value: String) {
        when (value) {
            "A" -> binding.rbA.isChecked = true
            "B" -> binding.rbB.isChecked = true
            "C" -> binding.rbC.isChecked = true
        }
    }

    private fun updateShiftAndRemarks(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        val gson = Gson()

        jsonData.forEach {
            if (it.contains(JSON_SHIFT_DETAILS_LIST_EN)) {
                val shift = gson.fromJson(it, VegaCameroonRMINShiftModel::class.java)
                shiftList.addAll(shift.SHIFT_DETAILS_LIST_EN)
            }
        }

        updateAdapter()
        vm.getRminData(model?.poNumber ?: "")
    }

    private fun updateAdapter() {
        if (shiftList.size > 0) {
            binding.rbA.text = shiftList[0]
            binding.rbB.text = shiftList[1]
            binding.rbC.text = shiftList[2]
        }
    }


}
