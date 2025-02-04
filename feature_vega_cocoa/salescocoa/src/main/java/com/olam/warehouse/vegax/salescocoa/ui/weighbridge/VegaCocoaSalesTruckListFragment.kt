package com.olam.warehouse.vegax.salescocoa.ui.weighbridge

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesWB
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.salescocoa.R
import com.olam.warehouse.vegax.salescocoa.databinding.FragmentCocoaSalesTruckListBinding
import com.olam.warehouse.vegax.salescocoa.ui.VegaCocoaSalesViewModel
import com.olam.warehouse.vegax.salescocoa.utils.ADD_LOT
import com.olam.warehouse.vegax.salescocoa.utils.SALES_TYPE_WEIGHBRIDGE
import com.olam.warehouse.vegax.salescocoa.utils.SEARCH_HINT
import com.olam.warehouse.vegax.salescocoa.utils.getColor
import kotlinx.android.synthetic.main.item_sales_truck_layout.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCocoaSalesTruckListFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_cocoa_sales_truck_list
    private lateinit var binding: FragmentCocoaSalesTruckListBinding
    private var callBack: CallBack? = null
    private val vm: VegaCocoaSalesViewModel by viewModel()

    private var dispatch = mutableListOf<VegaCocoaSalesWB>()
    private val mSearchList = mutableListOf<VegaCocoaSalesWB>()
    private var vegaWbIds = mutableListOf<VegaCocoaSalesWB>()

    companion object {
        fun newInstance() = VegaCocoaSalesTruckListFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = SEARCH_HINT
    }

    interface CallBack {
        fun replaceFragment(fragment: String, wb: VegaCocoaSalesWB, saleType: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("salescocoa/ui/weighbridge/VegaCocoaSalesSummaryFragment")
            .title("Sales Cocoa")
            .with(tracker)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCocoaSalesTruckListBinding.inflate(inflater)
        setHasOptionsMenu(true)
        initUI()
        return binding.root
    }

    private fun initUI() {
        binding.tvTitle.text =
            getString(com.olam.warehouse.login.R.string.local_sales).plus(" - ")
                .plus(getString(R.string.weighbridge))
        vm.trucks.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        if (AppUtils.isOnline()) vm.getTruckList()
        binding.tvSort.setOnClickListener { sortByDate() }
    }

    private fun sortByDate() {
        if (vegaWbIds.isNotEmpty()) {
            val data = vegaWbIds
            vegaWbIds = data.asReversed()
            setUpAdapter(vegaWbIds)
        }
    }


    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaCocoaSalesWB>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.data?.let {
                    if (it.isNotEmpty()) {
                        vegaWbIds = it.filter { it.batchPicking.isNullOrEmpty() } as ArrayList
                        dispatch = vegaWbIds
                        setUpAdapter(vegaWbIds)
                        binding.tvNoTruck.gone()
                        binding.rvLots.visible()
                        binding.tvTruckIDNo.text = getString(R.string.total_lot).plus(" ").plus(vegaWbIds.size)
                    } else {
                        binding.tvTruckIDNo.text = getString(R.string.total_lot).plus(" 0")
                        binding.tvNoTruck.visible()
                        binding.rvLots.gone()
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

    private fun setUpAdapter(data: List<VegaCocoaSalesWB>?) {
        val dispatch1 = data as MutableList<VegaCocoaSalesWB>
        binding.rvLots.setUp(dispatch1, R.layout.item_sales_truck_layout, { it, pos ->
            tv_truck_no.text = if (it.vehicleNumber.isEmpty()) "-" else it.vehicleNumber
            tvMaterialName.text = it.materialName
            tvWbId.text = it.weighBridgeId
            val times = it.erdat?.split('(', ')')
            tvDate.text = times?.get(1).let { it1 ->
                it1.let { it2 ->
                    it2?.let { it3 ->
                        DateUtils.getUTCDateTime(
                            it3,
                            App.getAppContext()
                        )
                    }
                }
            }
        }, {
            val item = this
            callBack?.replaceFragment(ADD_LOT, item, SALES_TYPE_WEIGHBRIDGE)
        })
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
                            setUpAdapter(dispatch)
                        } else {
                            mSearchList.clear()
                            dispatch.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.vehicleNumber.contains(text)) {
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
}
