package com.olam.warehouse.vegax.inventorycameroon.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventorycameroon.R
import com.olam.warehouse.vegax.inventorycameroon.data.domain.model.StorageLoc
import com.olam.warehouse.vegax.inventorycameroon.data.domain.model.SyncStatus
import com.olam.warehouse.vegax.inventorycameroon.data.domain.model.VegaInventoryAndSyncModel
import com.olam.warehouse.vegax.inventorycameroon.data.domain.model.VegaInventoryWarehouseModel
import com.olam.warehouse.vegax.inventorycameroon.databinding.FragmentVegaCameroonInventoryListBinding
import com.olam.warehouse.vegax.inventorycameroon.ui.dialog.VegaCameroonInventorySyncDetailsDailog
import com.olam.warehouse.vegax.inventorycameroon.utils.INVENTORY_CODE
import com.olam.warehouse.vegax.inventorycameroon.utils.INVENTORY_DETAILS
import com.olam.warehouse.vegax.inventorycameroon.utils.INVENTORY_LOTS
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
class VegaCameroonInventoryListFragment : BaseFragment(),
    VegaCameroonInventoryNavigateToDetailsListener {
    private lateinit var binding: FragmentVegaCameroonInventoryListBinding
    override val layoutResourceId = R.layout.fragment_vega_cameroon_inventory_list
    private var callBack: CallBack? = null
    private var isDescends: Boolean = true
    private var mSearchList = mutableListOf<VegaInventoryWarehouseModel>()
    private var wareHouseList = ArrayList<VegaInventoryWarehouseModel>()
    private var materialList = mutableListOf<VegaMaterial>()
    private var syncStatusList = ArrayList<SyncStatus>()
    private var expandPosition = 0
    var fullFilter = ArrayList<String>()

    interface CallBack {
        fun replaceFragment(moveFrag: String, bundle: Bundle)
    }

    companion object {
        fun newInstance() = VegaCameroonInventoryListFragment().putArgs {
        }
    }

    private val vm: VegaCameroonInventoryViewModel by viewModel()
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("inventorycameroon/ui/VegaCameroonInventoryListFragment")
            .title("Vega_Cameroon/Inventory").with(tracker)
        context?.let {
            getActionBtnChangedView(binding.tvInventoryHead, it, false)
        }
        vm.product.observe(viewLifecycleOwner, Observer {
            materialList = it as MutableList<VegaMaterial>
        })
        vm.getProducts()
        vm.getInventoryList()
        vm.inventoryModelList.observe(viewLifecycleOwner, Observer { updateUI(it) })

        binding.tvSort.setOnClickListener {
            isDescends = !isDescends
            sortByWeight(vm.inventoryModelList.value?.data?.data?.inventories ?: ArrayList())
        }
    }

    private fun syncDetails() {
        val bottomDialog = VegaCameroonInventorySyncDetailsDailog.newInstance(syncStatusList)
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
            search.isVisible = false
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(
                ContextCompat.getColor(
                    searchView.context,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            searchView.queryHint = getString(com.olam.warehouse.presentation.R.string.search_wb_item)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun sortByWeight(list: List<VegaInventoryWarehouseModel>) {
        val sortedList: List<VegaInventoryWarehouseModel>
        sortedList = if (isDescends) {
            list.sortedByDescending { wareHouse -> wareHouse.weight.toFloat() }
        } else {
            list.sortedBy { wareHouse -> wareHouse.weight.toFloat() }
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
        binding = FragmentVegaCameroonInventoryListBinding.inflate(layoutInflater)
        return binding.root
    }

    private fun setAdapter(data: List<VegaInventoryWarehouseModel>) {
        binding.rvInventory.layoutManager = LinearLayoutManager(activity).apply { LinearLayoutManager.VERTICAL }
        binding.rvInventory.adapter = VegaCameroonInventoryParentAdapter(data, materialList, this)
    }

    override fun navigateToDetails(storage: StorageLoc, colorCode: Int) {
        val bundle = Bundle().apply {
            putSerializable(INVENTORY_LOTS, storage)
            putInt(INVENTORY_CODE, colorCode)
        }
        callBack?.replaceFragment(INVENTORY_DETAILS, bundle)
    }
}
