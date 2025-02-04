package com.olam.warehouse.vegax.processingcocoa.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.observeOnce
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingcocoa.R
import com.olam.warehouse.vegax.processingcocoa.data.domain.model.VegaCocoaRMINRemarkModel
import com.olam.warehouse.vegax.processingcocoa.data.domain.model.VegaCocoaRMINShiftModel
import com.olam.warehouse.vegax.processingcocoa.databinding.FragmentShiftSelectionBinding
import com.olam.warehouse.vegax.processingcocoa.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCocoaRminShiftSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_shift_selection
    private lateinit var binding: FragmentShiftSelectionBinding

    private var callBack: CallBack? = null
    private val vm: VegaCocoaRminViewModel by viewModel()
    private var model: VegaCocoaRminProcessing? = null
    private var shift: String? = ""
    private var remark: String? = ""
    private var fromSummary: Boolean? = false
    private var shiftList = mutableListOf<String>()
    private var remarksList = mutableListOf<String>()
    private var jsonData = mutableListOf<String>()


    interface CallBack {
        fun replaceFragment(fragment: String, model: VegaCocoaRminProcessing)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    companion object {
        fun newInstance(fromSummary: Boolean, model: VegaCocoaRminProcessing) =
            VegaCocoaRminShiftSelectFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
                putBoolean(FROM_SUMMARY, fromSummary)
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcocoa/ui/rmin/VegaCocoaRminShiftSelectFragment")
            .title("Rmin Cocoa")
            .with(tracker)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShiftSelectionBinding.inflate(layoutInflater)
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
        vm.getShiftRemarksItems("VEGA_IV_COCO_SAP")
        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer { updateShiftAndRemarks(it) })
        vm.rmin.observeOnce(viewLifecycleOwner, Observer { initCurrentPositions(it) })

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
                callBack?.replaceFragment("Summary", model!!)
            }
        }

    }

    private fun initCurrentPositions(localData: VegaCocoaRminProcessing?) {
        if (localData != null) {
            model?.remark = localData.remark
            model?.shift = localData.shift
        }
        getCurrentRemarkPosition(model?.remark ?: "")
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
                    val shift = gson.fromJson(it, VegaCocoaRMINShiftModel::class.java)
                    shiftList.addAll(shift.SHIFT_DETAILS_LIST_EN)
                } else if (it.contains(JSON_PROCESSING_TYPE_LIST_EN)) {
                    val shift = gson.fromJson(it, VegaCocoaRMINRemarkModel::class.java)
                    remarksList.add(getString(R.string.select_remark))
                    remarksList.addAll(shift.PROCESSING_TYPE_LIST_EN)
                }
            } else if (language.equals("fr", true)) {
                if (it.contains(JSON_SHIFT_DETAILS_LIST_FR)) {
                    val shift = gson.fromJson(it, VegaCocoaRMINShiftModel::class.java)
                    shiftList.addAll(shift.SHIFT_DETAILS_LIST_FR)
                } else if (it.contains(JSON_PROCESSING_TYPE_LIST_FR)) {
                    val shift = gson.fromJson(it, VegaCocoaRMINRemarkModel::class.java)
                    remarksList.add(getString(R.string.select_remark))
                    remarksList.addAll(shift.PROCESSING_TYPE_LIST_FR)
                }
            }

        }
        updateAdapter()
        vm.getRMINProcessModel(model?.cgfNo ?: "", model?.poNumber ?: "", model?.bom ?: "", model?.materialName ?: "")
    }

    private fun updateAdapter() {
        val gradeAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_vega_cocoa_processing_rmin_grade,
            remarksList
        )
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spRemarks.adapter = gradeAdapter
        val defaultPosition = 0
        binding.spRemarks.setSelection(defaultPosition)
        binding.spRemarks.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    binding.btnProceed.isEnabled = true
                    val da = view as TextView
                    remark = da.text.toString()
                } else {
                    remark = ""
                    binding.btnProceed.isEnabled = false
                }

                if (shift.toString().isNotEmpty() && remark.toString().isNotEmpty()) enableDisableBtn(true)
                else enableDisableBtn(false)
            }
        }
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
