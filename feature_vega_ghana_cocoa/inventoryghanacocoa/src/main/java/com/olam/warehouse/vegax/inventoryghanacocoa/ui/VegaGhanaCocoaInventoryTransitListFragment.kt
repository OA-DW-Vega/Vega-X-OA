package com.olam.warehouse.vegax.inventoryghanacocoa.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventoryghanacocoa.R
import com.olam.warehouse.vegax.inventoryghanacocoa.data.domain.model.SyncStatus
import com.olam.warehouse.vegax.inventoryghanacocoa.data.domain.model.VegaInventoryWarehouseModel
import com.olam.warehouse.vegax.inventoryghanacocoa.databinding.FragmentVegaGhanaCocoaInventoryListBinding
import com.olam.warehouse.vegax.inventoryghanacocoa.utils.INVENTORY_CODE
import com.olam.warehouse.vegax.inventoryghanacocoa.utils.INVENTORY_LOTS
import com.olam.warehouse.vegax.inventoryghanacocoa.utils.TRANSIT_INVENTORY_DETAILS
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGhanaCocoaInventoryTransitListFragment : BaseFragment(), VegaGhanaCocoaInventoryTransitDetailsListener {
    private lateinit var binding: FragmentVegaGhanaCocoaInventoryListBinding
    override val layoutResourceId = R.layout.fragment_vega_ghana_cocoa_inventory_list
    private var callBack: CallBack? = null
    private var isDescends: Boolean = true
    private var mSearchList = mutableListOf<VegaInventoryWarehouseModel>()
    private var wareHouseList = ArrayList<VegaInventoryWarehouseModel?>()
    private var materialList = mutableListOf<VegaMaterial>()
    private var syncStatusList = ArrayList<SyncStatus>()
    private var expandPosition = 0
    var fullFilter = ArrayList<String>()
    private var mtnsList = mutableListOf<VegaReceivingMtn>()
    private var productList = mutableListOf<VegaReceivingMtnLots>()
    private var warehouselist = mutableListOf<VegaSupplyStorageLocation>()
    private var stockSupplyPlantList = mutableListOf<VegaReceivingWarehouse>()

    interface CallBack {
        fun replaceFragment(moveFrag: String, bundle: Bundle)
    }

    companion object {
        fun newInstance() = VegaGhanaCocoaInventoryTransitListFragment().putArgs {
        }
    }

    private val vm: VegaGhanaCocoaInventoryViewModel by viewModel()
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("inventorysesame/ui/VegaNigeriaSesameInventoryListFragment").title("Ecuador Inventory").with(tracker)
        vm.product.observe(viewLifecycleOwner, Observer {
            materialList = it as MutableList<VegaMaterial>
        })
        vm.getProducts()

        fetchMtnDetails()

        binding.tvSort.setOnClickListener {
            isDescends = !isDescends
            sortByWeight(productList,stockSupplyPlantList,warehouselist)
        }
    }

    private fun fetchMtnDetails() {
        vm.warehouse.observe(viewLifecycleOwner, Observer { processApiResult(it) })
        vm.fetchWarehouseWithMtns(false)
    }

    private fun processApiResult(data: Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> saveResult(it.data)
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> showErrorDialogWithFAQLink(requireContext(), it.error.toString())
            }
        }
    }

    private fun saveResult(data: GenericReqAndResp<VegaReceivingMtnWrapper>?) {
        hideLoading()
        data?.data?.let {
            mtnsList = it.mtns as MutableList<VegaReceivingMtn>
            productList = it.batchDetails as MutableList<VegaReceivingMtnLots>
            warehouselist = it.storageLocationLst as MutableList<VegaSupplyStorageLocation>
            stockSupplyPlantList = it.stockSupplyingPlants as MutableList<VegaReceivingWarehouse>

            setAdapter(productList,stockSupplyPlantList,warehouselist)
        }
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

    private fun sortByWeight(data: List<VegaReceivingMtnLots>, datanew: List<VegaReceivingWarehouse>,storageLocation: List<VegaSupplyStorageLocation>) {
        val sortedReceivingLots: List<VegaReceivingMtnLots>
        sortedReceivingLots = if (isDescends) {
            data.sortedByDescending { wareHouse -> wareHouse.weight.toFloat() }
        } else {
            data.sortedBy { wareHouse -> wareHouse.weight.toFloat() }
        }
        setAdapter(sortedReceivingLots,datanew,storageLocation)
    }

/*    private fun updateUI(data: Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    wareHouseList = it.data?.data?.inventories as ArrayList<VegaInventoryWarehouseModel?>
                    syncStatusList = it.data?.data?.syncStatus as ArrayList<SyncStatus>
                    setAdapter(it.data?.data?.inventories ?: ArrayList())
                    syncDetails()
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {

                }

            }
        }
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaGhanaCocoaInventoryListBinding.inflate(layoutInflater)
        return binding.root
    }

    private fun setAdapter(data: List<VegaReceivingMtnLots>, datanew: List<VegaReceivingWarehouse>,storageLocation: List<VegaSupplyStorageLocation>) {

        if (data.isNotEmpty()) binding.tvNoData.gone() else binding.tvNoData.visible()

        binding.rvInventory.layoutManager = LinearLayoutManager(activity).apply { LinearLayoutManager.VERTICAL }
        binding.rvInventory.adapter = VegaGhanaCocoaInventoryTransitParentAdapter(data,datanew,storageLocation, materialList, this)
    }

   /* private fun setAdapter(data: List<VegaInventoryWarehouseModel>) {

        if (data.isNotEmpty()) binding.tvNoData.gone() else binding.tvNoData.visible()

        binding.rvInventory.layoutManager = LinearLayoutManager(activity).apply { LinearLayoutManager.VERTICAL }
        binding.rvInventory.adapter = VegaGhanaCocoaInventoryParentAdapter(data, materialList, this)
    }*/

    override fun navigateTransitToDetails(vegaReceivingMtnLots: VegaReceivingMtnLots, colorCode: Int) {
        val bundle = Bundle().apply {
            putSerializable(INVENTORY_LOTS, vegaReceivingMtnLots)
            putInt(INVENTORY_CODE, colorCode)
        }
        callBack?.replaceFragment(TRANSIT_INVENTORY_DETAILS, bundle)
    }
}

