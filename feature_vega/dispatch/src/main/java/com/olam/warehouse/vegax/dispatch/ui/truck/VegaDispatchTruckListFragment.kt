package com.olam.warehouse.vegax.dispatch.ui.truck

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaDispatchTrucks
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.dispatch.R
import com.olam.warehouse.vegax.dispatch.databinding.FragmentVegaDispatchTruckListBinding
import com.olam.warehouse.vegax.dispatch.ui.VegaDispatchViewModel
import com.olam.warehouse.vegax.dispatch.utils.DIRECTION
import com.olam.warehouse.vegax.dispatch.utils.DISPATCH_PROCESS
import com.olam.warehouse.vegax.dispatch.utils.getColor
import kotlinx.android.synthetic.main.item_vega_dispatch_truck_list.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 2/17/2020.
 */
class VegaDispatchTruckListFragment : BaseFragment() {

    private lateinit var binding: FragmentVegaDispatchTruckListBinding
    private var callBack: CallBack? = null
    private var dispatchData = VegaDispatchTrucks()
    private var dispatch = mutableListOf<VegaDispatchTrucks>()
    private val mSearchList = mutableListOf<VegaDispatchTrucks>()

    override val layoutResourceId = R.layout.fragment_vega_dispatch_truck_list
    private val vm: VegaDispatchViewModel by viewModel()
    private var vegaWbIds = listOf<VegaDispatchTrucks>()

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            item: VegaDispatchTrucks
        )
    }

    companion object {
        fun newInstance() = VegaDispatchTruckListFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaDispatchTruckListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("dispatch/ui/truck/VegaDispatchTruckListFragment").title("Dispatch").with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llSortBy, it, false)
        }
        vm.trucks.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        if (AppUtils.isOnline()) vm.getTruckList()

        binding.ivSortDownUp.setOnClickListener {
            if (vegaWbIds.isNotEmpty()) {
                vegaWbIds.let { dispatch = it as MutableList<VegaDispatchTrucks> }
                dispatch = dispatch.asReversed()
                vegaWbIds = dispatch
                setUpAdapter(vegaWbIds)
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
                            setUpAdapter(dispatch)
                        } else {
                            mSearchList.clear()
                            dispatch.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.weighBridgeId.contains(text)) {
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

    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaDispatchTrucks>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.data?.let { it1 ->
                    if (it1.isNotEmpty()) {
                        vegaWbIds = listOf<VegaDispatchTrucks>()
                        vegaWbIds =
                            it1.filter { wb -> wb.direction == DIRECTION }
                                .filter { wb -> wb.batchPicking.isNullOrEmpty() }
                        dispatch = vegaWbIds as MutableList<VegaDispatchTrucks>
                        setUpAdapter(vegaWbIds)
                        binding.tvNoData.gone()
                        binding.rvWeighbridge.visible()
                    } else {
                        binding.tvNoData.visible()
                        binding.rvWeighbridge.gone()
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

    private fun setUpAdapter(data: List<VegaDispatchTrucks>?) {
        val dispatch1 = data as MutableList<VegaDispatchTrucks>
        binding.rvWeighbridge.setUp(dispatch1.asReversed(), R.layout.item_vega_dispatch_truck_list, { it, pos ->
            tvTruckNo.text = if (it.vehicleNumber.isNullOrEmpty()) "-" else it.vehicleNumber
            tvDelivery.text = it.delivery
//            tvWeight.text = it.netWeight.plus(it.unitsOfMeasure)
            tvWeighBridgeId.text = it.weighBridgeId
//            tvBagCount.text = it.bagCount
            val times = it.erdat?.split('(', ')')
            tvDate.text = times?.get(1).let { it1 ->
                it1?.let { it2 ->
                    DateUtils.getUTCDateTime(
                        it2,
                        App.getAppContext()
                    )
                }
            }
        }, {
            val item = this
            callBack?.replaceFragment(DISPATCH_PROCESS, item)

        })
    }
}
