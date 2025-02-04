package com.olam.warehouse.vegax.salescoffee.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeePendingSalesOrderWithLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.salescoffee.R
import com.olam.warehouse.vegax.salescoffee.databinding.FragmentCoffeeSalesPendingListBinding
import com.olam.warehouse.vegax.salescoffee.utils.ADD_LOT
import com.olam.warehouse.vegax.salescoffee.utils.SALES_TYPE_WEIGHSCALE
import com.olam.warehouse.vegax.salescoffee.utils.SEARCH_HINT
import com.olam.warehouse.vegax.salescoffee.utils.getColor
import kotlinx.android.synthetic.main.item_coffee_sales_pending_list.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 8/21/2020.
 */
class VegaCoffeeSalesPendingFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_coffee_sales_pending_list
    private lateinit var binding: FragmentCoffeeSalesPendingListBinding
    private var callBack: CallBack? = null
    private val vm: VegaCoffeeSalesViewModel by viewModel()

    private var dispatch = mutableListOf<VegaCoffeePendingSalesOrderWithLots>()
    private val mSearchList = mutableListOf<VegaCoffeePendingSalesOrderWithLots>()
    private var vegaWbIds = mutableListOf<VegaCoffeePendingSalesOrderWithLots>()

    companion object {
        fun newInstance(salesTypeWeighscale: String) = VegaCoffeeSalesPendingFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = SEARCH_HINT
    }

    interface CallBack {
        fun replaceFragment(fragment: String, item: VegaCoffeeSalesOrder, salesType: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCoffeeSalesPendingListBinding.inflate(inflater)
        setHasOptionsMenu(true)
        initUI()
        return binding.root
    }

    private fun initUI() {
        binding.tvTitle.text =
            getString(com.olam.warehouse.login.R.string.local_sales).plus(" - ").plus(getString(R.string.weighscale))
        vm.dispatchPendingSales.observe(viewLifecycleOwner, Observer { updateUIPendingList(it) })
         if (AppUtils.isOnline()) vm.getPendingList(SALES_TYPE_WEIGHSCALE)
         binding.tvSort.setOnClickListener { sortByDate() }
         binding.btnCreate.setOnClickListener {
             callBack?.replaceFragment(
                 ADD_LOT, VegaCoffeeSalesOrder(),
                 SALES_TYPE_WEIGHSCALE
             )
         }
    }

    private fun sortByDate() {
        if (dispatch.isNotEmpty()) {
            dispatch.let { vegaWbIds = it }
            vegaWbIds = vegaWbIds.asReversed()
            dispatch = vegaWbIds
            setUpAdapter(dispatch)
        }
    }

    private fun updateUIPendingList(it: List<VegaCoffeePendingSalesOrderWithLots>) {
        dispatch = it.toMutableList()
          binding.tvTruckIDNo.text = getString(R.string.total_lot).plus(" ").plus(dispatch.size)
        setUpAdapter(dispatch)
    }

    private fun setUpAdapter(data: List<VegaCoffeePendingSalesOrderWithLots>?) {
        val dispatch1 = data as MutableList<VegaCoffeePendingSalesOrderWithLots>
        binding.rvLots.setUp(dispatch1, R.layout.item_coffee_sales_pending_list, { it, pos ->
            val salesOrder = it.salesOrder
            tv_truck_no.text = salesOrder.salesTempId
            val materials =
                it.lineItems.filter { mat -> mat.salesTempId.equals(salesOrder.salesTempId) }
                    .map { it1 -> it1.materialName }.distinct().toString().replace("[", "").replace("]", "")
            tvMaterialName.text = materials.replace(",", "\n")
            tvWbId.text = salesOrder.saleOrderId
            tvCustomerValue.text = salesOrder.customerName
            if (!salesOrder.startTime.isNullOrEmpty())
                tvDateValue.text = salesOrder.startTime?.let { it1 ->
                    DateUtils.getUTCDateTime(
                        it1,
                        App.getAppContext()
                    )
                }
            ivDelete.setOnClickListener { showConfirmDeleteDialog(salesOrder.salesTempId) }
        }, {
            val item = this
            callBack?.replaceFragment(ADD_LOT, item.salesOrder, SALES_TYPE_WEIGHSCALE)
        })
    }

    private fun showConfirmDeleteDialog(salesTempId: String) {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_delete_item)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    vm.deleteTempData(salesTempId)
                },
                { dismiss() })
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
            searchView.queryHint = getString(com.olam.warehouse.presentation.R.string.search_by_lot)
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
                                    if (qtyWb.salesOrder.saleOrderId.contains(text)) {
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
//        vm.getPendingList(SALES_TYPE_WEIGHSCALE)
    }

}

