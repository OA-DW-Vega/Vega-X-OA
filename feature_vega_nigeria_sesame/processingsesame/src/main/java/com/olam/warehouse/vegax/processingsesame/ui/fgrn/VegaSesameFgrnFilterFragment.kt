package com.olam.warehouse.vegax.processingsesame.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.processingsesame.R
import com.olam.warehouse.vegax.processingsesame.data.domain.model.SesameFilterList
import com.olam.warehouse.vegax.processingsesame.databinding.FragmentVegaSesameFgrnFilterBinding
import com.olam.warehouse.vegax.processingsesame.databinding.ItemVegaSesameFgrnInventoryFilterBinding
import com.olam.warehouse.vegax.processingsesame.utils.*
import java.util.*

class VegaSesameFgrnFilterFragment : BaseFragment() {

    private lateinit var binding: FragmentVegaSesameFgrnFilterBinding
    override val layoutResourceId = R.layout.fragment_vega_sesame_fgrn_filter
    private var callBack: CallBack? = null
    private var fullFilter = ArrayList<String>()
    private var listFliterWhLoc = ArrayList<SesameFilterList>()
    private var startRange: String = ""
    private var endRange: String = ""
    private var aboveThirty: Boolean = false

    companion object {
        fun newInstance(bundle: Bundle, fullFilter: ArrayList<String>) = VegaSesameFgrnFilterFragment().putArgs {
            putBundle("BUNDLE_DATA", bundle)
            putStringArrayList(FULL_FILTER, fullFilter)
        }
    }

    interface CallBack {
        fun applyFilter(bundle: Bundle)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentVegaSesameFgrnFilterBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.btFilterApply, it, true)
        }
        val bundle = arguments?.getBundle("BUNDLE_DATA")
        fullFilter = arguments?.getStringArrayList(FULL_FILTER) ?: ArrayList()
        val whLocations = bundle?.getStringArrayList(FILTER_WH_LOC) ?: ArrayList()
        startRange = bundle?.getString(FILTER_START_RANGE) ?: ""
        endRange = bundle?.getString(FILTER_END_RANGE) ?: ""
        aboveThirty = bundle?.getBoolean(FILTER_ABOVE_RANGE) ?: false
        binding.btFilterApply.setOnClickListener { applyFilter() }
        binding.cbWhLocation.setOnClickListener { updateCheckBox() }
        binding.cbAbove30.isChecked = aboveThirty
        updateBackgound(aboveThirty)
        binding.cbAbove30.setOnCheckedChangeListener { buttonView, isChecked ->
            aboveThirty = isChecked
            if (isChecked) {
                startRange = "0"
                endRange = "30"
                setupRangeBar()
                updateBackgound(true)
            } else {
                updateBackgound(false)
            }
        }

        binding.seekbarPlaceholder.setOnRangeSeekBarChangeListener { bar, minValue, maxValue ->
            updateBackgound(false)
            aboveThirty = false
        }

        for (item in whLocations.distinct()) {
            if (!item.isNullOrEmpty()) {
                if (fullFilter.size > 0 && fullFilter.contains(item)) listFliterWhLoc.add(SesameFilterList(item, true))
                else listFliterWhLoc.add(SesameFilterList(item))
            }
        }
        setUpAdapter(listFliterWhLoc)
        setupRangeBar()
    }

    private fun updateBackgound(isSelected: Boolean) {
        ViewCompat.setBackground(
            binding.cbAbove30,
            ContextCompat.getDrawable(
                binding.cbAbove30.context,
                if (isSelected) com.olam.warehouse.presentation.R.drawable.custom_edit_text_blue_bg
                else
                    com.olam.warehouse.presentation.R.drawable.custom_edit_text
            )
        )
        binding.cbAbove30.setTextColor(
            ContextCompat.getColor(
                binding.cbAbove30.context,
                if (isSelected) com.olam.warehouse.presentation.R.color.white
                else
                    com.olam.warehouse.presentation.R.color.black
            )
        )
    }

    private fun updateCheckBox() {
        binding.cbWhLocation.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                listFliterWhLoc.forEach { it.isSelected = true }
            } else {
                listFliterWhLoc.forEach { it.isSelected = false }
            }
            binding.rvWhLocation.adapter?.notifyDataSetChanged()
        }
    }

    private fun setUpAdapter(listFliterWhLoc: ArrayList<SesameFilterList>) {
        binding.rvWhLocation.setUpAdapter(
            listFliterWhLoc, R.layout.item_vega_sesame_fgrn_inventory_filter,
            ItemVegaSesameFgrnInventoryFilterBinding::inflate,
            { it, pos, bindItem ->

                bindItem.tvInventoryFilter.text = it.value
                bindItem.tvInventoryFilter.setOnClickListener { view ->
                    it.isSelected = !it.isSelected
                    listFliterWhLoc[pos].isSelected = it.isSelected
                    binding.rvWhLocation.adapter?.notifyDataSetChanged()
                }
                ViewCompat.setBackground(
                    bindItem.tvInventoryFilter,
                    ContextCompat.getDrawable(
                        bindItem.tvInventoryFilter.context,
                        if (it.isSelected) com.olam.warehouse.presentation.R.drawable.custom_edit_text_blue_bg
                        else
                            com.olam.warehouse.presentation.R.drawable.custom_edit_text
                    )
                )
                bindItem.tvInventoryFilter.setTextColor(
                    ContextCompat.getColor(
                        bindItem.tvInventoryFilter.context,
                        if (it.isSelected) com.olam.warehouse.presentation.R.color.white
                        else
                            com.olam.warehouse.presentation.R.color.black
                    )
                )
            }, {}, GridLayoutManager(context, 2)
        )
    }

    private fun setupRangeBar() {
        binding.seekbarPlaceholder.setRangeValues(0, 30)
        binding.seekbarPlaceholder.setTextAboveThumbsColorResource(com.olam.warehouse.presentation.R.color.blue_light)
        binding.seekbarPlaceholder.selectedMinValue = startRange.toInt()
        binding.seekbarPlaceholder.selectedMaxValue = endRange.toInt()
    }

    private fun applyFilter() {
        val selectedStoLoc = listFliterWhLoc.filter { item -> item.isSelected }.map { it.value } as ArrayList
        val bundle = Bundle()
        bundle.putStringArrayList(FILTER_WH_LOC, selectedStoLoc)
        bundle.putString(FILTER_START_RANGE, binding.seekbarPlaceholder.selectedMinValue.toString())
        bundle.putString(FILTER_END_RANGE, binding.seekbarPlaceholder.selectedMaxValue.toString())
        bundle.putBoolean(FILTER_ABOVE_RANGE, aboveThirty)
        activity?.onBackPressed()
        callBack?.applyFilter(bundle)
    }

}
