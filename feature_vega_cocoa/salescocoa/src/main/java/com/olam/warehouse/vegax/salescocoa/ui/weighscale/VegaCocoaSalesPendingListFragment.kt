package com.olam.warehouse.vegax.salescocoa.ui.weighscale

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesWB
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.salescocoa.R
import com.olam.warehouse.vegax.salescocoa.databinding.FragmentCocoaSalesPendingListBinding
import com.olam.warehouse.vegax.salescocoa.databinding.ItemSalesPendingListBinding
import com.olam.warehouse.vegax.salescocoa.ui.VegaCocoaSalesViewModel
import com.olam.warehouse.vegax.salescocoa.utils.ADD_LOT
import com.olam.warehouse.vegax.salescocoa.utils.SALES_TYPE_WEIGHSCALE
import com.olam.warehouse.vegax.salescocoa.utils.SEARCH_HINT
import com.olam.warehouse.vegax.salescocoa.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCocoaSalesPendingListFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_cocoa_sales_pending_list
    private lateinit var binding: FragmentCocoaSalesPendingListBinding
    private var callBack: CallBack? = null
    private val vm: VegaCocoaSalesViewModel by viewModel()

    private var dispatch = mutableListOf<VegaCocoaSalesWB>()
    private val mSearchList = mutableListOf<VegaCocoaSalesWB>()
    private var vegaWbIds = mutableListOf<VegaCocoaSalesWB>()
    private var isRoundOff = false

    companion object {
        fun newInstance() = VegaCocoaSalesPendingListFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = SEARCH_HINT
    }

    interface CallBack {
        fun replaceFragment(
            fragment: String,
            item: VegaCocoaSalesWB,
            salesType: String,
            isRoundOff: Boolean
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("salescocoa/ui/weighscale/VegaCocoaSalesPendingListFragment")
            .title("Sales Cocoa")
            .with(tracker)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCocoaSalesPendingListBinding.inflate(inflater)
        setHasOptionsMenu(true)
        initUI()
        return binding.root
    }

    private fun initUI() {
        binding.tvTitle.text =
            getString(com.olam.warehouse.login.R.string.local_sales).plus(" - ").plus(getString(R.string.weighscale))
        vm.dispatchPendingWB.observe(viewLifecycleOwner, Observer { updateUIPendingList() })
        if (AppUtils.isOnline()) vm.getPendingList(SALES_TYPE_WEIGHSCALE)
        binding.tvSort.setOnClickListener { sortByDate() }
        binding.btnCreate.setOnClickListener {
            callBack?.replaceFragment(
                ADD_LOT, VegaCocoaSalesWB(),
                SALES_TYPE_WEIGHSCALE, false
            )
        }
    }

    private fun sortByDate() {
        if (vegaWbIds.isNotEmpty()) {
            val data = vegaWbIds
            vegaWbIds = data.asReversed()
            setUpAdapter(vegaWbIds)
        }
    }

    private fun updateUIPendingList() {
        dispatch = (vm.dispatchPendingWB.value ?: mutableListOf()).toMutableList()
        binding.tvTruckIDNo.text = getString(R.string.total_lot).plus(" ").plus(dispatch.size)
        setUpAdapter(dispatch)
    }

    private fun setUpAdapter(data: List<VegaCocoaSalesWB>?) {
        val dispatch1 = data as MutableList<VegaCocoaSalesWB>
        binding.rvLots.setUpAdapter(
            dispatch1,
            R.layout.item_sales_pending_list,
            ItemSalesPendingListBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvTruckNo.text = it.saleOrderId
                bindItem.tvMaterialName.text = it.materialName
                bindItem.tvWbId.text = it.soWeight
                bindItem.tvDateValue.text = it.ertim
                bindItem.tvCustomerValue.text = it.customerName
                isRoundOff = it.isRoundOff ?: false
            },
            {
                val item = this
                callBack?.replaceFragment(ADD_LOT, item, SALES_TYPE_WEIGHSCALE, isRoundOff ?: false)
            })
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
                            setUpAdapter(dispatch)
                        } else {
                            mSearchList.clear()
                            dispatch.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.saleOrderId.contains(text)) {
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

    fun updatePendingList() {
        vm.getPendingList(SALES_TYPE_WEIGHSCALE)
    }
}
