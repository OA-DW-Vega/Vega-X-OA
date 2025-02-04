package com.olam.warehouse.vegax.inventory.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventory.R
import com.olam.warehouse.vegax.inventory.data.domain.model.StorageLoc
import com.olam.warehouse.vegax.inventory.data.domain.model.SyncStatus
import com.olam.warehouse.vegax.inventory.data.domain.model.VegaInventoryAndSyncModel
import com.olam.warehouse.vegax.inventory.data.domain.model.VegaInventoryWarehouseModel
import com.olam.warehouse.vegax.inventory.databinding.FragmentVegaInventoryListBinding
import com.olam.warehouse.vegax.inventory.ui.dialog.VegaInventorySyncDetailsDailog
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaInventoryListFragment : BaseFragment(), NavigateToDetailsListener {
    private lateinit var binding: FragmentVegaInventoryListBinding
    override val layoutResourceId = R.layout.fragment_vega_inventory_list
    private var callBack: CallBack? = null
    private var isDescends: Boolean = true
    private var mSearchList = mutableListOf<VegaInventoryWarehouseModel>()
    private var wareHouseList = ArrayList<VegaInventoryWarehouseModel>()
    private var syncStatusList = ArrayList<SyncStatus>()
    private var expandPosition = 0
    var fullFilter = ArrayList<String>()

    private val vm: VegaInventoryViewModel by viewModel()
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("inventory/ui/VegaInventoryListFragment").title("Inventory")
            .with(tracker)

        context?.let {
            getActionBtnChangedView(binding.tvTruckNo, it, false)
            getActionBtnChangedView(binding.tvSyncDetails, it, false)
        }
        binding.tvTruckNo.text = resources.getString(R.string.inventory_list)
        binding.tvFilter.setOnClickListener {

            val positions: List<Int> =
                wareHouseList.withIndex().filter { it.value.isExpanded }.map { it.index }
            if (positions.isNotEmpty()) {
                expandPosition = positions[0]
            }
            moveToFilter(kors, origins)
        }
        vm.getInventoryList()
        vm.inventoryModelList.observe(viewLifecycleOwner, Observer { updateUI(it) })

        binding.tvSort.setOnClickListener {
            isDescends = !isDescends
            sortByWeight(vm.inventoryModelList.value?.data?.data?.inventories ?: ArrayList())
        }

        binding.tvSyncDetails.setOnClickListener { syncDetails() }
    }

    private fun syncDetails() {
        val bottomDialog = VegaInventorySyncDetailsDailog.newInstance(syncStatusList)
        activity?.supportFragmentManager?.let { it1 -> bottomDialog.show(it1, "BottomSheet") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(
                ContextCompat.getColor(
                    searchView.context,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            searchView.queryHint = SEARCH_HINT_TEXT
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setAdapter(wareHouseList)
                        } else {
                            mSearchList.clear()
                            wareHouseList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.warehouse.warehouseName.contains(text, true)) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            setAdapter(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun moveToFilter(korList: ArrayList<String>, orgins: ArrayList<String>) {
        val bundle = Bundle().apply {
            putStringArrayList("kors", korList)
            putStringArrayList("origins", orgins)
        }
        callBack?.replaceFragment("", bundle, fullFilter)
    }

    private fun sortByWeight(list: List<VegaInventoryWarehouseModel>) {
        val sortedList: List<VegaInventoryWarehouseModel>
        if (isDescends) {
            sortedList = list.sortedByDescending { wareHouse -> wareHouse.weight.toFloat() }
        } else {
            sortedList = list.sortedBy { wareHouse -> wareHouse.weight.toFloat() }
        }
        setAdapter(sortedList)
    }

    private fun updateUI(data: Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    wareHouseList = it.data?.data?.inventories as ArrayList<VegaInventoryWarehouseModel>
                    syncStatusList = it.data?.data?.syncStatus as ArrayList<SyncStatus>
                    getAllFilterValues()
                    setAdapter(it.data?.data?.inventories ?: ArrayList())
                    syncDetails()
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {

                }

            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaInventoryListBinding.inflate(layoutInflater)
        return binding.root
    }

    private fun setAdapter(data: List<VegaInventoryWarehouseModel>) {
        binding.rvWeighBridgeId.layoutManager = LinearLayoutManager(activity).apply { LinearLayoutManager.VERTICAL }
        binding.rvWeighBridgeId.adapter = VegaInventoryParentAdapter(data, this)
    }

    override fun navigateToDetails(storage: StorageLoc, colorCode: Int) {
        val bundle = Bundle().apply {
            putSerializable("Lots", storage)
            putInt("code", colorCode)
        }
        callBack?.replaceFragment("details", bundle)
    }

    companion object {
        const val SEARCH_HINT_TEXT = "Search Warehouse ID"
    }

    fun applyFilterValues(
        listKor: ArrayList<String>,
        listOrigin: ArrayList<String>
    ) {

        if (listKor.isEmpty() && listOrigin.isEmpty()) {
            this.fullFilter.clear()
            setAdapter(wareHouseList)
            return
        }
        val fullFilter = ArrayList<String>()
        fullFilter.addAll(listKor)
        fullFilter.addAll(listOrigin)
        this.fullFilter = fullFilter
        val model = ArrayList<VegaInventoryWarehouseModel>()
        for (item in wareHouseList) {
            model.add(item.copy())
        }
        val filterData = ArrayList<StorageLoc>()
        val filterWarehouse = ArrayList<VegaInventoryWarehouseModel>()

        model.forEachIndexed { index, mod ->
            fullFilter.forEach { korOrigin ->
                mod.storageLoc.forEach { storage ->
                    storage.averageKors.let {
                        if (storage.averageKors.contains(korOrigin) || storage.origin?.contains(korOrigin)!!) {
                            filterData.add(storage)
                        }
                    }
                }
            }
            if (filterData.isNotEmpty()) {
                @Suppress("UNCHECKED_CAST")
                val storageFilter = filterData.clone() as ArrayList<StorageLoc>
                val modd = model[index]
                modd.storageLoc = storageFilter
                filterWarehouse.add(modd)
                filterData.clear()
            }
        }

        val result = HashSet<VegaInventoryWarehouseModel>()
        result.addAll(filterWarehouse)
        val warehouse = result.toList()

        for (item in warehouse) {
            item.storageLoc = removeDuplicates(item)
        }
        setAdapter(warehouse)
    }

    private fun removeDuplicates(warehouse: VegaInventoryWarehouseModel): List<StorageLoc> {
        val result = HashSet<StorageLoc>()
        result.addAll(warehouse.storageLoc)
        return result.toList()
    }

    private fun getAllFilterValues() {
        for (item in wareHouseList) {
            for (sub in item.storageLoc) {
                for (kor in sub.averageKors)
                    kors.add(kor)
            }
        }
        for (item in wareHouseList) {
            for (sub in item.storageLoc) {
                for (origin in sub.origins)
                    origins.add(origin)
            }
        }
    }

    var kors = ArrayList<String>()
    var origins = ArrayList<String>()
}
