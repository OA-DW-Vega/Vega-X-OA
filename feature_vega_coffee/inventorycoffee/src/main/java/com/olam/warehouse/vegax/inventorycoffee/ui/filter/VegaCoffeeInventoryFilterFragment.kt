package com.olam.warehouse.vegax.inventorycoffee.ui.filter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventory.ui.filter.VegaCoffeeInventoryFilterAdapter
import com.olam.warehouse.vegax.inventorycoffee.R
import com.olam.warehouse.vegax.inventorycoffee.data.domain.model.FilterList
import com.olam.warehouse.vegax.inventorycoffee.databinding.FragmentVegaCoffeeInventoryFilterBinding
import com.olam.warehouse.vegax.inventorycoffee.ui.UnCheckListener
import com.olam.warehouse.vegax.inventorycoffee.utils.CallBack
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCoffeeInventoryFilterFragment : BaseFragment(),
    UnCheckListener {
    private var callBack: CallBack? = null
    private lateinit var binding: FragmentVegaCoffeeInventoryFilterBinding
    override val layoutResourceId: Int = R.layout.fragment_vega_coffee_inventory_filter
    private var listFliterKor = ArrayList<FilterList>()
    private var listFliterOrigin = ArrayList<FilterList>()
    private var listFliterMaterial = ArrayList<FilterList>()
    private var fullFilter = ArrayList<String>()
    /*private val filterOrigin = ArrayList<FilterList>()
    private val filterKOR = ArrayList<FilterList>()*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //binding.btApply.setOnClickListener { callBack?.filterList(ArrayList()) }
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("inventory/ui/filter/VegaCoffeeInventoryFilterFragment").title("Inventory").with(tracker)
        makeFilterData()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCoffeeInventoryFilterBinding.inflate(layoutInflater)

        binding.cbKor.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                listFliterKor.forEach { it.isSelected = true }
            } else {
                listFliterKor.forEach { it.isSelected = false }
            }
            binding.rvKor.adapter?.notifyDataSetChanged()
        }
        binding.cbOrgin.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                listFliterOrigin.forEach { it.isSelected = true }
            } else {
                listFliterOrigin.forEach { it.isSelected = false }
            }
            binding.rvOrgin.adapter?.notifyDataSetChanged()
        }
        binding.cbMaterial.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                listFliterMaterial.forEach { it.isSelected = true }
            } else {
                listFliterMaterial.forEach { it.isSelected = false }
            }
            binding.rvMaterial.adapter?.notifyDataSetChanged()
        }
        binding.rvKor.layoutManager = GridLayoutManager(activity, 3)
        binding.rvOrgin.layoutManager = GridLayoutManager(activity, 3)
        binding.rvMaterial.layoutManager = GridLayoutManager(activity, 3)
        binding.btFilter.setOnClickListener { finishAndFilter() }
        return binding.root
    }

    private fun finishAndFilter() {
        val korFiltered = listFliterKor.filter { item -> item.isSelected }.map { it.value } as ArrayList
        val orgFiltered = listFliterOrigin.filter { item -> item.isSelected }.map { it.value } as ArrayList
        val materialFiltered = listFliterMaterial.filter { item -> item.isSelected }.map { it.value } as ArrayList

        val bundle = Bundle().apply {
            putStringArrayList("KOR", korFiltered)
            putStringArrayList("Origin", orgFiltered)
            putStringArrayList("Material", materialFiltered)
        }
        activity?.onBackPressed()
        callBack?.replaceFragment("filterList", bundle)
    }

    private fun setKorAdapter(data: MutableList<FilterList>) {
        binding.rvKor.adapter =
            VegaCoffeeInventoryFilterAdapter(data, this)
        listFliterKor = data as ArrayList<FilterList>
    }

    private fun setOrgonFilter(data: MutableList<FilterList>) {
        binding.rvOrgin.adapter =
            VegaCoffeeInventoryFilterAdapter(data, this)
        listFliterOrigin = data as ArrayList<FilterList>
    }

    private fun setMaterialFilter(data: MutableList<FilterList>) {
        binding.rvMaterial.adapter =
            VegaCoffeeInventoryFilterAdapter(data, this)
        listFliterMaterial = data as ArrayList<FilterList>
    }

    override fun isAllSelected(position: Int) {
        /*if (position == 0)
            binding.cbKor.isChecked = false
        else binding.cbOrgin.isChecked = false*/
    }

    private fun makeFilterData() {
        val kors = arguments?.getStringArrayList("kors") ?: ArrayList()
        val orgins = arguments?.getStringArrayList("origins") ?: ArrayList()
        val materials = arguments?.getStringArrayList("materials") ?: ArrayList()
        fullFilter = arguments?.getStringArrayList("FULL_FILTER") ?: ArrayList()

        val filterKOR = ArrayList<FilterList>()
        val filterOrigin = ArrayList<FilterList>()
        val filterMaterial = ArrayList<FilterList>()
        for (item in kors.distinct()) {
            if (!item.isNullOrEmpty()) {
                if (fullFilter.size > 0 && fullFilter.contains(item)) filterKOR.add(FilterList(item, true))
                else filterKOR.add(FilterList(item))
            }
        }
        for (item in orgins.distinct()) {
            if (!item.isNullOrEmpty()) {
                if (fullFilter.size > 0 && fullFilter.contains(item)) filterOrigin.add(FilterList(item, true))
                else filterOrigin.add(FilterList(item))
            }
        }

        for (item in materials.distinct()) {
            if (!item.isNullOrEmpty()) {
                if (fullFilter.size > 0 && fullFilter.contains(item)) filterMaterial.add(FilterList(item, true))
                else filterMaterial.add(FilterList(item))
            }
        }
        if (!filterKOR.any { !it.isSelected }) binding.cbKor.isChecked = true
        if (!filterOrigin.any { !it.isSelected }) binding.cbOrgin.isChecked = true
        if (!filterMaterial.any { !it.isSelected }) binding.cbMaterial.isChecked = true
        setKorAdapter(filterKOR)
        setOrgonFilter(filterOrigin)
        setMaterialFilter(filterMaterial)
    }
}
