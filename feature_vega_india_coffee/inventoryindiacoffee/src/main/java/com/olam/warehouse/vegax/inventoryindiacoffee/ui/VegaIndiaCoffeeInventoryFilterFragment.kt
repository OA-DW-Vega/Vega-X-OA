package com.olam.warehouse.vegax.inventoryindiacoffee.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventoryindiacoffee.R
import com.olam.warehouse.vegax.inventoryindiacoffee.data.domain.model.FilterList
import com.olam.warehouse.vegax.inventoryindiacoffee.databinding.FragmentVegaIndiaCoffeeInventoryFilterBinding
import com.olam.warehouse.vegax.inventoryindiacoffee.utils.*
import kotlinx.android.synthetic.main.item_vega_india_coffee_inventory_filter.view.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaIndiaCoffeeInventoryFilterFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_india_coffee_inventory_filter
    private lateinit var binding: FragmentVegaIndiaCoffeeInventoryFilterBinding
    private var callBack: CallBack? = null
    private var fullFilter = ArrayList<String>()
    private var listFilterMaterial = ArrayList<FilterList>()
    private var listFilterImpurity = ArrayList<FilterList>()
    private var listFilterHumidity = ArrayList<FilterList>()
    private var listFilterMould = ArrayList<FilterList>()

    interface CallBack {
        fun applyFilter(bundle: Bundle)
    }

    companion object {
        fun newInstance(bundle: Bundle, fullFilter: ArrayList<String>) =
                VegaIndiaCoffeeInventoryFilterFragment().putArgs {
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
        binding = FragmentVegaIndiaCoffeeInventoryFilterBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("inventoryindiacoffee/ui/VegaIndiaCoffeeInventoryFilterFragment")
                .title("Inventory Ecuador")
                .with(tracker)
        initUI()
    }

    private fun initUI() {
        val bundle = arguments?.getBundle("BUNDLE_DATA")
        val materialList = bundle?.getStringArrayList(FILTER_MATERIAL) ?: ArrayList()
        val humidityList = bundle?.getStringArrayList(FILTER_HUMIDITY) ?: ArrayList()
        val impurityList = bundle?.getStringArrayList(FILTER_IMPURITY) ?: ArrayList()
        val mouldList = bundle?.getStringArrayList(FILTER_MOULD) ?: ArrayList()
        fullFilter = arguments?.getStringArrayList(FULL_FILTER) ?: ArrayList()

        binding.btFilter.setOnClickListener { applyFilter() }

        //Material Adapter
        for (item in materialList.distinct()) {
            if (!item.isNullOrEmpty()) {
                if (fullFilter.size > 0 && fullFilter.contains(item)) listFilterMaterial.add(FilterList(item, true))
                else listFilterMaterial.add(FilterList(item))
            }
        }
        setUpMaterialAdapter(listFilterMaterial)

        //Admixture Adapter
        for (item in humidityList.distinct()) {
            if (!item.isNullOrEmpty()) {
                if (fullFilter.size > 0 && fullFilter.contains(item)) listFilterHumidity.add(FilterList(item, true))
                else listFilterHumidity.add(FilterList(item))
            }
        }
        setUpHumidityAdapter(listFilterHumidity)

        //Impurity Adapter
        for (item in impurityList.distinct()) {
            if (!item.isNullOrEmpty()) {
                if (fullFilter.size > 0 && fullFilter.contains(item)) listFilterImpurity.add(FilterList(item, true))
                else listFilterImpurity.add(FilterList(item))
            }
        }
        setUpImpurityAdapter(listFilterImpurity)

        //Mould Adapter
        for (item in mouldList.distinct()) {
            if (!item.isNullOrEmpty()) {
                if (fullFilter.size > 0 && fullFilter.contains(item)) listFilterMould.add(FilterList(item, true))
                else listFilterMould.add(FilterList(item))
            }
        }
        setUpMouldAdapter(listFilterMould)
//        binding.tvAdmixture.text = "KOR"
        updateCheckedAll()
    }

    private fun applyFilter() {
        val selectedMaterial = listFilterMaterial.filter { item -> item.isSelected }.map { it.value } as ArrayList
        val selectedHumidity = listFilterHumidity.filter { item -> item.isSelected }.map { it.value } as ArrayList
        val selectedImpurity = listFilterImpurity.filter { item -> item.isSelected }.map { it.value } as ArrayList
        val selectedMould = listFilterMould.filter { item -> item.isSelected }.map { it.value } as ArrayList
        val bundle = Bundle()
        bundle.putStringArrayList(FILTER_MATERIAL, selectedMaterial)
        bundle.putStringArrayList(FILTER_HUMIDITY, selectedHumidity)
        bundle.putStringArrayList(FILTER_IMPURITY, selectedImpurity)
        bundle.putStringArrayList(FILTER_MOULD, selectedMould)
        activity?.onBackPressed()
        callBack?.applyFilter(bundle)
    }

    private fun updateCheckedAll() {
        binding.cbMaterial.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                listFilterMaterial.forEach { it.isSelected = true }
            } else {
                listFilterMaterial.forEach { it.isSelected = false }
            }
            binding.rvMaterial.adapter?.notifyDataSetChanged()
        }

        binding.cbAdmixture.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                listFilterHumidity.forEach { it.isSelected = true }
            } else {
                listFilterHumidity.forEach { it.isSelected = false }
            }
            binding.rvAdmixture.adapter?.notifyDataSetChanged()
        }

        binding.cbTotalImpurity.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                listFilterImpurity.forEach { it.isSelected = true }
            } else {
                listFilterImpurity.forEach { it.isSelected = false }
            }
            binding.rvTotalImpurity.adapter?.notifyDataSetChanged()
        }

        binding.cbMould.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                listFilterMould.forEach { it.isSelected = true }
            } else {
                listFilterMould.forEach { it.isSelected = false }
            }
            binding.rvMould.adapter?.notifyDataSetChanged()
        }

    }

    private fun setUpMaterialAdapter(listFilterMaterial: ArrayList<FilterList>) {
        binding.rvMaterial.setUp(listFilterMaterial, R.layout.item_vega_india_coffee_inventory_filter, { it, pos ->

            tvInventoryFilter.text = it.value
            tvInventoryFilter.setOnClickListener { view ->
                it.isSelected = !it.isSelected
                listFilterMaterial[pos].isSelected = it.isSelected
                binding.rvMaterial.adapter?.notifyDataSetChanged()
            }
            ViewCompat.setBackground(
                    tvInventoryFilter,
                    ContextCompat.getDrawable(
                            tvInventoryFilter.context,
                            if (it.isSelected) com.olam.warehouse.presentation.R.drawable.item_selector_green
                            else
                                com.olam.warehouse.presentation.R.drawable.item_deselected_white
                    )
            )
            tvInventoryFilter.setTextColor(
                    ContextCompat.getColor(
                            tvInventoryFilter.context,
                            if (it.isSelected) com.olam.warehouse.presentation.R.color.white
                            else
                                com.olam.warehouse.presentation.R.color.black
                    )
            )
        }, {}, GridLayoutManager(context, 2))
    }

    private fun setUpHumidityAdapter(listFilterHumidity: ArrayList<FilterList>) {
        binding.rvAdmixture.setUp(listFilterHumidity, R.layout.item_vega_india_coffee_inventory_filter, { it, pos ->

            tvInventoryFilter.text = it.value
            tvInventoryFilter.setOnClickListener { view ->
                it.isSelected = !it.isSelected
                listFilterHumidity[pos].isSelected = it.isSelected
                binding.rvAdmixture.adapter?.notifyDataSetChanged()
            }
            ViewCompat.setBackground(
                    tvInventoryFilter,
                    ContextCompat.getDrawable(
                            tvInventoryFilter.context,
                            if (it.isSelected) com.olam.warehouse.presentation.R.drawable.item_selector_green
                            else
                                com.olam.warehouse.presentation.R.drawable.item_deselected_white
                    )
            )
            tvInventoryFilter.setTextColor(
                    ContextCompat.getColor(
                            tvInventoryFilter.context,
                            if (it.isSelected) com.olam.warehouse.presentation.R.color.white
                            else
                                com.olam.warehouse.presentation.R.color.black
                    )
            )
        }, {}, GridLayoutManager(context, 3))
    }

    private fun setUpImpurityAdapter(listFilterImpurity: ArrayList<FilterList>) {
        binding.rvTotalImpurity.setUp(listFilterImpurity, R.layout.item_vega_india_coffee_inventory_filter, { it, pos ->

            tvInventoryFilter.text = it.value
            tvInventoryFilter.setOnClickListener { view ->
                it.isSelected = !it.isSelected
                listFilterImpurity[pos].isSelected = it.isSelected
                binding.rvTotalImpurity.adapter?.notifyDataSetChanged()
            }
            ViewCompat.setBackground(
                    tvInventoryFilter,
                    ContextCompat.getDrawable(
                            tvInventoryFilter.context,
                            if (it.isSelected) com.olam.warehouse.presentation.R.drawable.item_selector_green
                            else
                                com.olam.warehouse.presentation.R.drawable.item_deselected_white
                    )
            )
            tvInventoryFilter.setTextColor(
                    ContextCompat.getColor(
                            tvInventoryFilter.context,
                            if (it.isSelected) com.olam.warehouse.presentation.R.color.white
                            else
                                com.olam.warehouse.presentation.R.color.black
                    )
            )
        }, {}, GridLayoutManager(context, 2))
    }

    private fun setUpMouldAdapter(listFilterMould: ArrayList<FilterList>) {
        binding.rvMould.setUp(listFilterMould, R.layout.item_vega_india_coffee_inventory_filter, { it, pos ->

            tvInventoryFilter.text = it.value
            tvInventoryFilter.setOnClickListener { view ->
                it.isSelected = !it.isSelected
                listFilterMould[pos].isSelected = it.isSelected
                binding.rvMould.adapter?.notifyDataSetChanged()
            }
            ViewCompat.setBackground(
                    tvInventoryFilter,
                    ContextCompat.getDrawable(
                            tvInventoryFilter.context,
                            if (it.isSelected) com.olam.warehouse.presentation.R.drawable.item_selector_green
                            else
                                com.olam.warehouse.presentation.R.drawable.item_deselected_white
                    )
            )
            tvInventoryFilter.setTextColor(
                    ContextCompat.getColor(
                            tvInventoryFilter.context,
                            if (it.isSelected) com.olam.warehouse.presentation.R.color.white
                            else
                                com.olam.warehouse.presentation.R.color.black
                    )
            )
        }, {}, GridLayoutManager(context, 2))
    }
}
