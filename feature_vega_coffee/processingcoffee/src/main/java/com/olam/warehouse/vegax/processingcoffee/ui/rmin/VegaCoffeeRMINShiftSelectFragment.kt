package com.olam.warehouse.vegax.processingcoffee.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.observeOnce
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.processingcoffee.R
import com.olam.warehouse.vegax.processingcoffee.data.domain.model.VegaCoffeeRMINRemarkModel
import com.olam.warehouse.vegax.processingcoffee.data.domain.model.VegaCoffeeRMINShiftModel
import com.olam.warehouse.vegax.processingcoffee.databinding.FragmentRminShiftSelectionBinding
import com.olam.warehouse.vegax.processingcoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaCoffeeRMINShiftSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_rmin_shift_selection
    private lateinit var binding: FragmentRminShiftSelectionBinding

    private var callBack: CallBack? = null
    private val vm: VegaCoffeeRminViewModel by viewModel()
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
            VegaCoffeeRMINShiftSelectFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
                putBoolean(FROM_SUMMARY, fromSummary)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRminShiftSelectionBinding.inflate(layoutInflater)
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
        vm.getShiftRemarksItems("VEGA_IV_COFF_SAP")
        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer { updateShiftAndRemarks(it) })
        vm.rminModel.observeOnce(viewLifecycleOwner, Observer { initCurrentPositions(it) })
        binding.spRemarks.setText(model?.remark ?: "")
        shift = model?.shift
        remark = model?.remark
        enableDisableBtn(model?.shift?.isNotEmpty() == true && model?.remark?.isNotEmpty() == true)

        binding.spRemarks.onChange {
            if (it.isNotEmpty()) {
                enableDisableBtn(shift.toString().isNotEmpty() && it.isNotEmpty())
                remark = it.toString()

            } else {
                enableDisableBtn(false)
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
            if (shift.toString().isNotEmpty() && remark.toString().isNotEmpty()) enableDisableBtn(true)
            else enableDisableBtn(false)
        }
    }

    private fun validateField() {
        when {
            shift.toString().isEmpty() -> showSnack(getString(R.string.choose_shift))
            remark.toString().isEmpty() -> showSnack(getString(R.string.choose_remarks))
            else -> {
                model?.remark = remark
                model?.shift = shift
                vm.saveRMINProcess(model!!)
                callBack?.replaceFragment(SUMMARY, model!!)
            }
        }

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

    private fun getCurrentRemarkPosition(value: String) {
        var currentPos = 0
        remarksList.forEachIndexed { index, s -> if (s == value) currentPos = index }
        binding.spRemarks.setSelection(currentPos)
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
        val language = PreferenceHelper.get(LANGUAGE, "en")
        jsonData.forEach {
            if (language.equals("en", true)) {
                if (it.contains(JSON_SHIFT_DETAILS_LIST_EN)) {
                    val shift = gson.fromJson(it, VegaCoffeeRMINShiftModel::class.java)
                    shiftList.addAll(shift.SHIFT_DETAILS_LIST_EN)
                } else if (it.contains(JSON_PROCESSING_TYPE_LIST_EN)) {
                    val shift = gson.fromJson(it, VegaCoffeeRMINRemarkModel::class.java)
                    remarksList.add(getString(R.string.select_remark))
                    remarksList.addAll(shift.PROCESSING_TYPE_LIST_EN)
                }
            } else if (language.equals("fr", true)) {
                if (it.contains(JSON_SHIFT_DETAILS_LIST_FR)) {
                    val shift = gson.fromJson(it, VegaCoffeeRMINShiftModel::class.java)
                    shiftList.addAll(shift.SHIFT_DETAILS_LIST_FR)
                } else if (it.contains(JSON_PROCESSING_TYPE_LIST_FR)) {
                    val shift = gson.fromJson(it, VegaCoffeeRMINRemarkModel::class.java)
                    remarksList.add(getString(R.string.select_remark))
                    remarksList.addAll(shift.PROCESSING_TYPE_LIST_FR)
                }
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

    private fun enableDisableBtn(flag: Boolean) {
        if (flag) {
            binding.btnProceed.isEnabled = true
            binding.btnProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnProceed.isEnabled = false
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
    }
}
