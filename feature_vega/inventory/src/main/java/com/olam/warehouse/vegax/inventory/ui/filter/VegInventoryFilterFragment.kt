package com.olam.warehouse.vegax.inventory.ui.filter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventory.R
import com.olam.warehouse.vegax.inventory.data.domain.model.FilterList
import com.olam.warehouse.vegax.inventory.databinding.FragmentVegaInventoryFilterBinding
import com.olam.warehouse.vegax.inventory.ui.CallBack
import com.olam.warehouse.vegax.inventory.ui.UnCheckListener
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegInventoryFilterFragment : BaseFragment(),
    UnCheckListener {
    private var callBack: CallBack? = null
    private lateinit var binding: FragmentVegaInventoryFilterBinding
    override val layoutResourceId: Int = R.layout.fragment_vega_inventory_filter
    private var listFliterKor = ArrayList<FilterList>()
    private var listFliterOrigin = ArrayList<FilterList>()
    private var fullFilter = ArrayList<String>()
    /*private val filterOrigin = ArrayList<FilterList>()
    private val filterKOR = ArrayList<FilterList>()*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //binding.btApply.setOnClickListener { callBack?.filterList(ArrayList()) }
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("inventory/ui/filter/VegInventoryFilterFragment").title("Inventory").with(tracker)
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
        binding = FragmentVegaInventoryFilterBinding.inflate(layoutInflater)

        context?.let {
            getActionBtnChangedView(binding.btFilter, it, true)
        }
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
        binding.rvKor.layoutManager = GridLayoutManager(activity, 3)
        binding.rvOrgin.layoutManager = GridLayoutManager(activity, 3)
        binding.btFilter.setOnClickListener { finishAndFilter() }
        return binding.root
    }

    private fun finishAndFilter() {
        val korFiltered = listFliterKor.filter { item -> item.isSelected }.map { it.value } as ArrayList
        val orgFiltered = listFliterOrigin.filter { item -> item.isSelected }.map { it.value } as ArrayList

            val bundle = Bundle().apply {
                putStringArrayList("KOR", korFiltered)
                putStringArrayList("Origin", orgFiltered)
            }
        activity?.onBackPressed()
            callBack?.replaceFragment("filterList", bundle)
    }

    private fun setKorAdapter(data: MutableList<FilterList>) {
        binding.rvKor.adapter =
            VegaInventoryFilterAdapter(data, this)
        listFliterKor = data as ArrayList<FilterList>
    }

    private fun setOrgonFilter(data: MutableList<FilterList>) {
        binding.rvOrgin.adapter =
            VegaInventoryFilterAdapter(data, this)
        listFliterOrigin = data as ArrayList<FilterList>
    }

    override fun isAllSelected(position: Int) {
        /*if (position == 0)
            binding.cbKor.isChecked = false
        else binding.cbOrgin.isChecked = false*/
    }

    private fun makeFilterData() {
        val kors = arguments?.getStringArrayList("kors") ?: ArrayList()
        val orgins = arguments?.getStringArrayList("origins") ?: ArrayList()
        fullFilter = arguments?.getStringArrayList("FULL_FILTER") ?: ArrayList()

        val filterKOR = ArrayList<FilterList>()
        val filterOrigin = ArrayList<FilterList>()
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
        if (!filterKOR.any { !it.isSelected }) binding.cbKor.isChecked = true
        if (!filterOrigin.any { !it.isSelected }) binding.cbOrgin.isChecked = true
        setKorAdapter(filterKOR)
        setOrgonFilter(filterOrigin)
    }
}
