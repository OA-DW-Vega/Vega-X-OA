package com.olam.warehouse.vegax.inventoryindo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventoryindo.R
import com.olam.warehouse.vegax.inventoryindo.data.domain.model.FilterList
import com.olam.warehouse.vegax.inventoryindo.databinding.FragmentIndoCoffeeInventoryFilterBinding
import com.olam.warehouse.vegax.inventoryindo.databinding.ItemIndoCoffeeInventoryFilterBinding
import com.olam.warehouse.vegax.inventoryindo.utils.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
class VegaIndoCoffeeInventoryFilterFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_indo_coffee_inventory_filter
    private lateinit var binding: FragmentIndoCoffeeInventoryFilterBinding
    private var callBack: CallBack? = null
    private var fullFilter = ArrayList<String>()
    private var listFliterMaterial = ArrayList<FilterList>()
    private var listFliterLocation = ArrayList<FilterList>()
    private var listFliterBean = ArrayList<FilterList>()
    private var listFliterMoist = ArrayList<FilterList>()
    private var listFliterFfa = ArrayList<FilterList>()
    private var listFliterFat = ArrayList<FilterList>()

    interface CallBack {
        fun applyFilter(bundle: Bundle)
    }

    companion object {
        fun newInstance(bundle: Bundle, fullFilter: ArrayList<String>) =
            VegaIndoCoffeeInventoryFilterFragment().putArgs {
                putBundle("BUNDLE_DATA", bundle)
                putStringArrayList(FULL_FILTER, fullFilter)
            }
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
        binding = FragmentIndoCoffeeInventoryFilterBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("inventorycocoa/ui/VegaCocoaInventoryFilterFragment").title("Inventory Cocoa")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        val bundle = arguments?.getBundle("BUNDLE_DATA")
        val materialList = bundle?.getStringArrayList(FILTER_MATERIAL) ?: ArrayList()
        val locationList = bundle?.getStringArrayList(FILTER_LOCATION) ?: ArrayList()
        val beanList = bundle?.getStringArrayList(FILTER_BEAN) ?: ArrayList()
        val moistList = bundle?.getStringArrayList(FILTER_MOIST) ?: ArrayList()
        val ffaList = bundle?.getStringArrayList(FILTER_FFA) ?: ArrayList()
        val fatList = bundle?.getStringArrayList(FILTER_FAT) ?: ArrayList()
        fullFilter = arguments?.getStringArrayList(FULL_FILTER) ?: ArrayList()

        binding.btFilter.setOnClickListener { applyFilter() }

        //Material Adapter
        for (item in materialList.distinct()) {
            if (!item.isNullOrEmpty()) {
                if (fullFilter.size > 0 && fullFilter.contains(item)) listFliterMaterial.add(FilterList(item, true))
                else listFliterMaterial.add(FilterList(item))
            }
        }
        setUpMaterialAdapter(listFliterMaterial)

        //Material Adapter
        for (item in locationList.distinct()) {
            if (!item.isNullOrEmpty()) {
                if (fullFilter.size > 0 && fullFilter.contains(item)) listFliterLocation.add(FilterList(item, true))
                else listFliterLocation.add(FilterList(item))
            }
        }
        setUpLocationAdapter(listFliterLocation)

        //Bean Adapter
        for (item in beanList.distinct()) {
            if (!item.isNullOrEmpty()) {
                if (fullFilter.size > 0 && fullFilter.contains(item)) listFliterBean.add(FilterList(item, true))
                else listFliterBean.add(FilterList(item))
            }
        }
        setUpBeanAdapter(listFliterBean)

        //Moist Adapter
        for (item in moistList.distinct()) {
            if (!item.isNullOrEmpty()) {
                if (fullFilter.size > 0 && fullFilter.contains(item)) listFliterMoist.add(FilterList(item, true))
                else listFliterMoist.add(FilterList(item))
            }
        }
        setUpMoistAdapter(listFliterMoist)

        //FFA Adapter
        for (item in ffaList.distinct()) {
            if (!item.isNullOrEmpty()) {
                if (fullFilter.size > 0 && fullFilter.contains(item)) listFliterFfa.add(FilterList(item, true))
                else listFliterFfa.add(FilterList(item))
            }
        }
        setUpFFAAdapter(listFliterFfa)

        //FAT Adapter
        for (item in fatList.distinct()) {
            if (!item.isNullOrEmpty()) {
                if (fullFilter.size > 0 && fullFilter.contains(item)) listFliterFat.add(FilterList(item, true))
                else listFliterFat.add(FilterList(item))
            }
        }
        setUpFATAdapter(listFliterFat)
        updateCheckedAll()
    }

    private fun applyFilter() {
        val selectedMaterial = listFliterMaterial.filter { item -> item.isSelected }.map { it.value } as ArrayList
        val selectedLocation = listFliterLocation.filter { item -> item.isSelected }.map { it.value } as ArrayList
        val selectedMoist = listFliterMoist.filter { item -> item.isSelected }.map { it.value } as ArrayList
        val selectedBean = listFliterBean.filter { item -> item.isSelected }.map { it.value } as ArrayList
        val selectedFfa = listFliterFfa.filter { item -> item.isSelected }.map { it.value } as ArrayList
        val selectedFat = listFliterFat.filter { item -> item.isSelected }.map { it.value } as ArrayList
        val bundle = Bundle()
        bundle.putStringArrayList(FILTER_MATERIAL, selectedMaterial)
        bundle.putStringArrayList(FILTER_LOCATION, selectedLocation)
        bundle.putStringArrayList(FILTER_BEAN, selectedBean)
        bundle.putStringArrayList(FILTER_MOIST, selectedMoist)
        bundle.putStringArrayList(FILTER_FFA, selectedFfa)
        bundle.putStringArrayList(FILTER_FAT, selectedFat)
        activity?.onBackPressed()
        callBack?.applyFilter(bundle)
    }

    private fun updateCheckedAll() {
        binding.cbMaterial.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                listFliterMaterial.forEach { it.isSelected = true }
            } else {
                listFliterMaterial.forEach { it.isSelected = false }
            }
            binding.rvMaterial.adapter?.notifyDataSetChanged()
        }

        binding.cbLocation.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                listFliterLocation.forEach { it.isSelected = true }
            } else {
                listFliterLocation.forEach { it.isSelected = false }
            }
            binding.rvLocation.adapter?.notifyDataSetChanged()
        }

        binding.cbMoisture.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                listFliterMoist.forEach { it.isSelected = true }
            } else {
                listFliterMoist.forEach { it.isSelected = false }
            }
            binding.rvMoisture.adapter?.notifyDataSetChanged()
        }

        binding.cbBeanCount.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                listFliterBean.forEach { it.isSelected = true }
            } else {
                listFliterBean.forEach { it.isSelected = false }
            }
            binding.rvBeanCount.adapter?.notifyDataSetChanged()
        }

        binding.cbFFA.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                listFliterFfa.forEach { it.isSelected = true }
            } else {
                listFliterFfa.forEach { it.isSelected = false }
            }
            binding.rvFFA.adapter?.notifyDataSetChanged()
        }

        binding.cbFAT.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                listFliterFat.forEach { it.isSelected = true }
            } else {
                listFliterFat.forEach { it.isSelected = false }
            }
            binding.rvFAT.adapter?.notifyDataSetChanged()
        }
    }

    private fun setUpMaterialAdapter(listFliterMaterial: ArrayList<FilterList>) {
        binding.rvMaterial.setUpAdapter(
            listFliterMaterial,
            R.layout.item_indo_coffee_inventory_filter,
            ItemIndoCoffeeInventoryFilterBinding::inflate,
            { it, pos, bindItem ->

                bindItem.tvInventoryFilter.text = it.value
                bindItem.tvInventoryFilter.setOnClickListener { view ->
                    it.isSelected = !it.isSelected
                    listFliterMaterial[pos].isSelected = it.isSelected
                    binding.rvMaterial.adapter?.notifyDataSetChanged()
                }
                ViewCompat.setBackground(
                    bindItem.tvInventoryFilter,
                    ContextCompat.getDrawable(
                        bindItem.tvInventoryFilter.context,
                        if (it.isSelected) com.olam.warehouse.presentation.R.drawable.item_selector_green
                        else
                            com.olam.warehouse.presentation.R.drawable.item_deselected_white
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

    private fun setUpLocationAdapter(listFliterLocation: ArrayList<FilterList>) {
        binding.rvLocation.setUpAdapter(
            listFliterLocation,
            R.layout.item_indo_coffee_inventory_filter,
            ItemIndoCoffeeInventoryFilterBinding::inflate,
            { it, pos, bindItem ->

                bindItem.tvInventoryFilter.text = it.value
                bindItem.tvInventoryFilter.setOnClickListener { view ->
                    it.isSelected = !it.isSelected
                    listFliterLocation[pos].isSelected = it.isSelected
                    binding.rvLocation.adapter?.notifyDataSetChanged()
                }
                ViewCompat.setBackground(
                    bindItem.tvInventoryFilter,
                    ContextCompat.getDrawable(
                        bindItem.tvInventoryFilter.context,
                        if (it.isSelected) com.olam.warehouse.presentation.R.drawable.item_selector_green
                        else
                            com.olam.warehouse.presentation.R.drawable.item_deselected_white
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

    private fun setUpBeanAdapter(listFliterBean: ArrayList<FilterList>) {
        binding.rvBeanCount.setUpAdapter(
            listFliterBean,
            R.layout.item_indo_coffee_inventory_filter,
            ItemIndoCoffeeInventoryFilterBinding::inflate,
            { it, pos, bindItem ->

                bindItem.tvInventoryFilter.text = it.value
                bindItem.tvInventoryFilter.setOnClickListener { view ->
                    it.isSelected = !it.isSelected
                    listFliterBean[pos].isSelected = it.isSelected
                    binding.rvBeanCount.adapter?.notifyDataSetChanged()
                }
                ViewCompat.setBackground(
                    bindItem.tvInventoryFilter,
                    ContextCompat.getDrawable(
                        bindItem.tvInventoryFilter.context,
                        if (it.isSelected) com.olam.warehouse.presentation.R.drawable.item_selector_green
                        else
                            com.olam.warehouse.presentation.R.drawable.item_deselected_white
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

    private fun setUpMoistAdapter(listFliterMoist: ArrayList<FilterList>) {
        binding.rvMoisture.setUpAdapter(
            listFliterMoist,
            R.layout.item_indo_coffee_inventory_filter,
            ItemIndoCoffeeInventoryFilterBinding::inflate,
            { it, pos, bindItem ->

                bindItem.tvInventoryFilter.text = it.value
                bindItem.tvInventoryFilter.setOnClickListener { view ->
                    it.isSelected = !it.isSelected
                    listFliterMoist[pos].isSelected = it.isSelected
                    binding.rvMoisture.adapter?.notifyDataSetChanged()
                }
                ViewCompat.setBackground(
                    bindItem.tvInventoryFilter,
                    ContextCompat.getDrawable(
                        bindItem.tvInventoryFilter.context,
                        if (it.isSelected) com.olam.warehouse.presentation.R.drawable.item_selector_green
                        else
                            com.olam.warehouse.presentation.R.drawable.item_deselected_white
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
            }, {}, GridLayoutManager(context, 3)
        )
    }

    private fun setUpFFAAdapter(listFliterFfa: ArrayList<FilterList>) {
        binding.rvFFA.setUpAdapter(
            listFliterFfa,
            R.layout.item_indo_coffee_inventory_filter,
            ItemIndoCoffeeInventoryFilterBinding::inflate,
            { it, pos, bindItem ->

                bindItem.tvInventoryFilter.text = it.value
                bindItem.tvInventoryFilter.setOnClickListener { view ->
                    it.isSelected = !it.isSelected
                    listFliterFfa[pos].isSelected = it.isSelected
                    binding.rvFFA.adapter?.notifyDataSetChanged()
                }
                ViewCompat.setBackground(
                    bindItem.tvInventoryFilter,
                    ContextCompat.getDrawable(
                        bindItem.tvInventoryFilter.context,
                        if (it.isSelected) com.olam.warehouse.presentation.R.drawable.item_selector_green
                        else
                            com.olam.warehouse.presentation.R.drawable.item_deselected_white
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

    private fun setUpFATAdapter(listFliterFat: ArrayList<FilterList>) {
        binding.rvFAT.setUpAdapter(
            listFliterFat,
            R.layout.item_indo_coffee_inventory_filter,
            ItemIndoCoffeeInventoryFilterBinding::inflate,
            { it, pos, bindItem ->

                bindItem.tvInventoryFilter.text = it.value
                bindItem.tvInventoryFilter.setOnClickListener { view ->
                    it.isSelected = !it.isSelected
                    listFliterFat[pos].isSelected = it.isSelected
                    binding.rvFAT.adapter?.notifyDataSetChanged()
                }
                ViewCompat.setBackground(
                    bindItem.tvInventoryFilter,
                    ContextCompat.getDrawable(
                        bindItem.tvInventoryFilter.context,
                        if (it.isSelected) com.olam.warehouse.presentation.R.drawable.item_selector_green
                        else
                            com.olam.warehouse.presentation.R.drawable.item_deselected_white
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
}
