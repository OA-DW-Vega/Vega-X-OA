package com.olam.warehouse.vegax.offloadingcoffee.ui.mtnr.weighscale

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils.DIRECTIONIN
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcoffee.R
import com.olam.warehouse.vegax.offloadingcoffee.databinding.FragmentVegaCoffeeOffloadWeighscaleListBinding
import com.olam.warehouse.vegax.offloadingcoffee.databinding.ItemVegaCoffeeWeighscaleTruckListBinding
import com.olam.warehouse.vegax.offloadingcoffee.ui.VegaCoffeeOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingcoffee.ui.VegaCoffeeOffloadingViewModel
import com.olam.warehouse.vegax.offloadingcoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaCoffeeOffloadWeighscaleListFragment : BaseFragment() {
    private lateinit var binding: FragmentVegaCoffeeOffloadWeighscaleListBinding
    private var callBack: VegaCoffeeOffloadReplaceFragmentCallback? = null
    private var gateEntryData = VegaCoffeeReceiving()
    private var gateEntry = mutableListOf<VegaCoffeeReceiving>()
    private val mSearchList = mutableListOf<VegaCoffeeReceiving>()

    override val layoutResourceId = R.layout.fragment_vega_coffee_offload_weighscale_list
    private val vm: VegaCoffeeOffloadingViewModel by viewModel()
    private var vegaWbIds = listOf<VegaCoffeeReceiving>()
    private var weighmentType: String = ""
    private var currentKey = getCurrentKey()

    companion object {
        fun newInstance(type: VegaCoffeeReceiving) = VegaCoffeeOffloadWeighscaleListFragment().putArgs {
            putParcelable(OFFLOAD_DATA, type)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as VegaCoffeeOffloadReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        gateEntryData = arguments?.getParcelable(OFFLOAD_DATA)!!
        binding = FragmentVegaCoffeeOffloadWeighscaleListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        val wType = gateEntryData.weighBridgeType
        TrackHelper.track()
            .screen("offloadingcoffee/ui/mtnr/weighscale/VegaCoffeeOffloadWeighscaleListFragment - $wType")
            .title("IVC/Coffee/Offloading/MTNR Weighscale List")
            .with(tracker)
    }

    private fun initUI() {
        binding.llWeighbridge.gone()
        if (currentKey.split("_")[1].contains("NI")) {
            binding.tvNoData.visible()
        } else {
            vm.truckInWeighBridge.observe(
                viewLifecycleOwner,
                Observer { updateUIWithOnlineData(it) })
            binding.tvType.text =
                getString(R.string.waiting_truck_header).plus(" ")
                    .plus(if (gateEntryData.weighBridgeType.equals(PROCURE)) SUPPLIER else MTNR)
            if (isOnline()) if (gateEntryData.imageString.equals(WEIGHSCALE)) vm.getTruckInWeighBridgeDetail(
                gateEntryData.imageString.equals(
                    WEIGHSCALE
                )
            ) else vm.getWeighBridgeDetail()
        }
        binding.llAddNewTruck.setOnClickListener {
            callBack?.replaceFragment(
                if (gateEntryData.weighBridgeType == STO) WEIGHSCALE else WEIGHSCALE_SUPPLIER,
                gateEntryData
            )
        }
        binding.llAddNewTruck.visibility =
            if (gateEntryData.imageString.equals(WEIGHSCALE)) View.VISIBLE else View.GONE
        binding.ivSortDownUp.setOnClickListener {
            if (gateEntry.isNotEmpty()) {
                val data = gateEntry
                gateEntry = data.asReversed()
                setUpAdapter(gateEntry)
            }
        }
        if (currentKey.split("_")[1].contains("NI")) {
            binding.tvNoData.visible()
        } else {
            vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint = SEARCH_HINT_TEXT
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setUpAdapter(gateEntry)
                        } else {
                            mSearchList.clear()
                            gateEntry.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.vehicleNumber?.contains(text)!!) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            setUpAdapter(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun setUpAdapter(data: List<VegaCoffeeReceiving>) {
        val gateEntry1 = data as MutableList<VegaCoffeeReceiving>
        binding.tvTruckTotal.text = getString(R.string.truck_count).plus(" ").plus(gateEntry1.size.toString())
        binding.rvWeighbridge.setUpAdapter(
            gateEntry1.asReversed(),
            R.layout.item_vega_coffee_weighscale_truck_list,
            ItemVegaCoffeeWeighscaleTruckListBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvTruckNo.text =
                    if (it.vehicleNumber.isNullOrEmpty()) "-" else it.vehicleNumber
                if (gateEntryData.weighBridgeType == PROCURE) {
                    bindItem.tvdifference.text = SUPPLIER
                    bindItem.tvSupplierName.text = it.supplierName ?: it.supplierCode
                    bindItem.tvObdNumber.visibility = View.GONE
                    bindItem.tvObdNumberLabel.visibility = View.GONE
                } else {
                    bindItem.tvdifference.visibility = View.GONE
                    bindItem.tvSupplierName.visibility = View.GONE
                    bindItem.tvdifference.text = WAREHOUSE
                    bindItem.tvSupplierName.text = "-"
                    bindItem.tvObdNumber.visibility = View.VISIBLE
                    bindItem.tvObdNumberLabel.visibility = View.VISIBLE
                }
                bindItem.tvdifference.text = SUPPLIER
                bindItem.tvSupplierName.text = it.supplierName ?: it.supplierCode
                bindItem.tvWeighBridgeId.text = it.weighBridgeId
                bindItem.tvMaterial.text = it.materialName
                bindItem.tvObdNumber.text = it.delivery
                bindItem.tvWeight.text = it.grossWeight.plus(" ").plus(it.unitsOfMeasure)
                val times = it.erdat?.split('(', ')')
                bindItem.tvDate.text = times?.get(1)?.let { it1 ->
                    DateUtils.getUTCDateTime(
                        it1,
                        App.getAppContext()
                    )
                }
            },
            {
                val item = this
                item.weighBridgeType = gateEntryData.weighBridgeType
                item.imageString = gateEntryData.imageString
                callBack?.replaceFragment(
                    if (gateEntryData.weighBridgeType == STO) WEIGHSCALE else WEIGHSCALE_SUPPLIER,
                    item
                )
            })
    }

    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaCoffeeReceiving>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.let { it1 ->
                    if (it1.data.isNotEmpty()) {
                        vegaWbIds = listOf()
                        var filterList = ArrayList<VegaCoffeeReceiving>()
                        if (gateEntryData.imageString.equals(WEIGHSCALE)) {
                            filterList = it1.data.filter { it.weighMethod == "WS" } as ArrayList<VegaCoffeeReceiving>
                        } else {
                            filterList = it1.data.filter { it.weighMethod == "WB" }
                                .filter { wb -> !wb.grossWeight.equals("0.000") } as ArrayList<VegaCoffeeReceiving>
                        }

                        vegaWbIds = if (gateEntryData.weighBridgeType == PROCURE)
                            filterList.filter { wb -> wb.direction == DIRECTIONIN }
                                .filter { wb -> wb.weighBridgeType == PROCURE }
                                .filter { wb -> wb.qcStatus.isNullOrEmpty() }
                        else
                            filterList.filter { wb -> wb.direction == DIRECTIONIN }
                                .filter { wb -> wb.weighBridgeType == STO }
                                .filter { wb -> wb.qcStatus.isNullOrEmpty() }

                        gateEntry = vegaWbIds as MutableList<VegaCoffeeReceiving>
                        if (vegaWbIds.size > 0) {
                            setUpAdapter(vegaWbIds)
                            binding.tvNoData.gone()
                            binding.llWeighbridge.visible()
                        } else {
                            binding.tvNoData.visible()
                            binding.llWeighbridge.gone()
                        }
                    } else {
                        binding.tvNoData.visible()
                        binding.llWeighbridge.gone()
                    }
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }
}
