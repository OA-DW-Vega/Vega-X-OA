package com.olam.warehouse.vegax.inventoryghanacocoa.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventoryghanacocoa.R
import com.olam.warehouse.vegax.inventoryghanacocoa.data.domain.model.SyncStatus
import com.olam.warehouse.vegax.inventoryghanacocoa.data.domain.model.VegaInventoryWarehouseModel
import com.olam.warehouse.vegax.inventoryghanacocoa.databinding.FragmentVegaGhanaCocoaTransitListBinding
import com.olam.warehouse.vegax.inventoryghanacocoa.ui.dialog.VegaGhanaCocoaInventorySyncDetailsDailog
import kotlinx.android.synthetic.main.item_vega_india_coffee_offloading_truck_list.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGhanaCocoaTransitFragment : BaseFragment(), VegaGhanaCocoaInventoryTransitDetailsListener {
    private lateinit var binding: FragmentVegaGhanaCocoaTransitListBinding
    override val layoutResourceId = R.layout.fragment_vega_ghana_cocoa_transit_list
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
        fun newInstance() = VegaGhanaCocoaTransitFragment().putArgs {
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
        TrackHelper.track()
            .screen("inventoryghanacocoa/ui/VegaGhanaCocoaInventoryTransitDetailsListener")
            .title("Ghana Inventory").with(tracker)
        vm.product.observe(viewLifecycleOwner, Observer {
            materialList = it as MutableList<VegaMaterial>
        })
        vm.getProducts()

        fetchMtnDetails()

        binding.ivSortDownUp.setOnClickListener {
            isDescends = !isDescends
            sortByWeight(productList)
        }
    }

    private fun fetchMtnDetails() {
        vm.warehouse.observe(viewLifecycleOwner, Observer { processApiResult(it) })
        vm.fetchWarehouseWithMtns(true)
    }

    private fun processApiResult(data: Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> saveResult(it.data)
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR ->{
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                }
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

            setAdapter(productList,)
        }
    }

    private fun setAdapter(data: MutableList<VegaReceivingMtnLots>) {
        val batchDetails = data
        binding.rvWeighbridge.setUp(
            batchDetails,
            R.layout.item_vega_india_coffee_offloading_truck_list,
            { it, pos ->
                tvTruckNo.text =
                    if (it.storageLocationCode.isNullOrEmpty()) "-" else it.storageLocationCode
                tvWeighBridgeId.text = (it.materialName)
                tvSupplierName.text = it.batch
                tvNoofBags.text = it.weight.formatThreeDigits().plus(" ").plus(it.uom)
                tvSupplyingPlant.text = it.supplyingPlantId.plus("-").plus(it.supplyingPlantName)
                tvMTNTNumber.text = it.mtnNumber
                //tvDate.text=DateUtils.getFormatedDate(mtnsList.get(pos).mtntDate) //2019/05/21
                if ((mtnsList.get(pos).mtntDate?.isNotEmpty() == true && mtnsList.get(pos).mtntDate?.length == 8)) tvDate.text =
                    mtnsList.get(pos).mtntDate?.let { it1 -> DateUtils.parseDate(it1) }
            },
        )
    }

    private fun syncDetails() {
        val bottomDialog = VegaGhanaCocoaInventorySyncDetailsDailog.newInstance(syncStatusList)
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
                    com.olam.warehouse.presentation.R.color.green
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
        }
    }

    private fun sortByWeight(data: MutableList<VegaReceivingMtnLots>) {
        if (isDescends) {
            data.sortedByDescending { wareHouse -> wareHouse.weight.toFloat() }
        } else {
            data.sortedBy { wareHouse -> wareHouse.weight.toFloat() }
        }
        setAdapter(data)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaGhanaCocoaTransitListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun navigateTransitToDetails(vegaReceivingMtnLots: VegaReceivingMtnLots, colorCode: Int) {
        TODO("Not yet implemented")
    }


}


