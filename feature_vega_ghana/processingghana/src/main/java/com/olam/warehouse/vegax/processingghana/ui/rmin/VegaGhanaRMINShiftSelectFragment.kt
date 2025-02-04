package com.olam.warehouse.vegax.processingghana.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.observeOnce
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.processingghana.R
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaRMINShiftModel
import com.olam.warehouse.vegax.processingghana.databinding.FragmentGhanaRminShiftSelectionBinding
import com.olam.warehouse.vegax.processingghana.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaGhanaRMINShiftSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_ghana_rmin_shift_selection
    private lateinit var binding: FragmentGhanaRminShiftSelectionBinding

    private var callBack: CallBack? = null
    private val vm: VegaGhanaRminViewModel by viewModel()
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
        //menu.clear()
    }

    companion object {
        fun newInstance(fromSummary: Boolean, model: VegaCoffeeRminProcessing) =
            VegaGhanaRMINShiftSelectFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
                putBoolean(FROM_SUMMARY, fromSummary)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGhanaRminShiftSelectionBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun initExtra() {
        model = arguments?.getParcelable(MODEL_BUNDLE)
        fromSummary = arguments?.getBoolean(FROM_SUMMARY)
    }

    private fun initUI() {

        binding.btnProceed.setOnClickListener { validateField() }
        vm.getShiftRemarksItems(getCurrentKey())
        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer { updateShiftAndRemarks(it) })
        vm.rminModel.observeOnce(viewLifecycleOwner, Observer { initCurrentPositions(it) })
        binding.spRemarks.setText(model?.remark ?: "")
        shift = model?.shift
        remark = model?.remark

        binding.spRemarks.onChange {
            if (it.isNotEmpty()) {
                remark = it.toString()

            } else {
            }
        }

        binding.rgShift.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbA -> {
                    updateRadioTextColor(first = true, second = false)
                    shift = "Morning"
                }
                R.id.rbB -> {
                    updateRadioTextColor(second = true, first = false)
                    shift = "Night"
                }
            }
        }
    }

    private fun validateField() {
        when {
            shift.toString().isEmpty() -> model?.shift = ""
            remark.toString().isEmpty() -> model?.remark = ""
            else -> {
                model?.remark = remark
                model?.shift = shift
                vm.saveRMINProcess(model!!)
                callBack?.replaceFragment(SUMMARY, model!!)
            }
        }

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

    private fun updateRadioTextColor(first: Boolean, second: Boolean) {
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

    private fun getCurrentRemarkPosition(value: String) {
        var currentPos = 0
        remarksList.forEachIndexed { index, s -> if (s == value) currentPos = index }
        binding.spRemarks.setSelection(currentPos)
    }

    private fun updateCurrentShiftSelection(value: String) {
        when (value) {
            "Morning" -> binding.rbA.isChecked = true
            "Night" -> binding.rbB.isChecked = true
        }
    }

    private fun updateShiftAndRemarks(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        val gson = Gson()

        jsonData.forEach{
            if(it.contains(JSON_SHIFT_DETAILS_LIST)){
                val shift = gson.fromJson(it, VegaGhanaRMINShiftModel::class.java)
                shiftList.addAll(shift.SHIFT_DETAILS_LIST)
            }
        }
        updateAdapter()
        vm.getRminData(model?.poNumber ?: "")
    }

    private fun updateAdapter() {
        if (shiftList.size > 0) {
            binding.rbA.text = shiftList[0]
            binding.rbB.text = shiftList[1]
        }
    }

    private fun enableDisableBtn(flag: Boolean) {
        if (flag) {
            binding.btnProceed.isEnabled = true
            binding.btnProceed.setBackgroundColor(getColor(if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnProceed.isEnabled = false
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
    }
}
