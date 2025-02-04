package com.olam.warehouse.vegax.gateentryghana.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentryghana.R
import com.olam.warehouse.vegax.gateentryghana.databinding.FragmentVegaGhanaWaitingTruckListBinding
import com.olam.warehouse.vegax.gateentryghana.utils.*
import kotlinx.android.synthetic.main.item_vega_ghana_waiting_truck_list.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 3/4/2020.
 */
class VegaGhanaWaitingTruckListFragment : BaseFragment() {
    private lateinit var binding: FragmentVegaGhanaWaitingTruckListBinding
    private var callBack: CallBack? = null
    private var gateEntryData = VegaGateEntry()
    private var gateEntry = mutableListOf<VegaGateEntry>()
    private val mSearchList = mutableListOf<VegaGateEntry>()

    override val layoutResourceId = R.layout.fragment_vega_ghana_waiting_truck_list
    private val vm: VegaGateEntryGhanaViewModel by viewModel()
    private var vegaWbIds = listOf<VegaGateEntry>()

    interface CallBack {
        fun replaceFragment(paramsListFrag: String, item: VegaGateEntry)
    }

    companion object {
        fun newInstance(gateEntryData: VegaGateEntry) = VegaGhanaWaitingTruckListFragment().putArgs {
            putParcelable(GATE_ENTRY_DATA, gateEntryData)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaGhanaWaitingTruckListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        val wType = gateEntryData.weighBridgeType
        TrackHelper.track().screen("gateentry/ui/VegaWaitingTruckListFragment - $wType").title("Gate Entry")
            .with(tracker)
    }

    private fun initUI() {
        gateEntryData = arguments?.getParcelable(GATE_ENTRY_DATA)!!
        binding.llWeighbridge.gone()
        vm.waitingTrucks.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        binding.tvType.text =
            getString(R.string.waiting_truck_header).plus(" ")
                .plus(if (gateEntryData.weighBridgeType.equals(PROCURE)) SUPPLIER else MTNR)
        if (isOnline()) vm.getWaitingTruckList()
        binding.llAddNewTruck.setOnClickListener { callBack?.replaceFragment(PARAMS_LIST_FRAG, gateEntryData) }

        binding.ivSortDownUp.setOnClickListener {
            if (gateEntry.isNotEmpty()) {
                val data = gateEntry
                gateEntry = data.asReversed()
                setUpAdapter(gateEntry)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
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

    private fun setUpAdapter(data: List<VegaGateEntry>) {
        val gateEntry1 = data as MutableList<VegaGateEntry>
        binding.tvTruckTotal.text = getString(R.string.truck_count).plus(" ").plus(gateEntry1.size.toString())
        binding.rvWeighbridge.setUp(gateEntry1.asReversed(), R.layout.item_vega_ghana_waiting_truck_list, { it, pos ->
            tvTruckNo.text = if (it.vehicleNumber.isNullOrEmpty()) "-" else it.vehicleNumber
            if (it.weighBridgeType == PROCURE) {
                tvdifference.text = SUPPLIER
                tvSupplierName.text = it.supplierName ?: it.supplierCode
            } else {
                tvdifference.visibility = View.GONE
                tvSupplierName.visibility = View.GONE
                tvdifference.text = WAREHOUSE
                tvSupplierName.text = "-"
            }
            tvWeighBridgeId.text = it.weighBridgeId
            val times = it.erdat?.split('(', ')')
            tvDate.text = times?.get(1)?.let { it1 ->
                DateUtils.getUTCDateTime(
                    it1,
                    App.getAppContext()
                )
            }
        }, {
            /* val item = this
             item.weighBridgeType = gateEntryData.weighBridgeType
             callBack?.replaceFragment(PARAMS_LIST_FRAG, item)*/
        })
    }

    private fun updateUIWithOnlineData(response: Resource<List<VegaGateEntry>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.let { it1 ->
                    if (it1.isNotEmpty()) {
                        vegaWbIds = listOf()
                        vegaWbIds = if (gateEntryData.weighBridgeType == PROCURE)
                            it1.filter { wb -> wb.direction == DIRECTIONIN }
                                .filter { wb -> wb.weighBridgeType == PROCURE }
                        else
                            it1.filter { wb -> wb.direction == DIRECTIONIN }
                                .filter { wb -> wb.weighBridgeType == STO }
                        gateEntry = vegaWbIds as MutableList<VegaGateEntry>
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
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
            }
        }
    }

}
