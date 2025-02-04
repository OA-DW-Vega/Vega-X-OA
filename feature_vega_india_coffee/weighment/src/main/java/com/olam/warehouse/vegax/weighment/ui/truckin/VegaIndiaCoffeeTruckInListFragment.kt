package com.olam.warehouse.vegax.weighment.ui.truckin

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.weighment.R
import com.olam.warehouse.vegax.weighment.databinding.FragmentVegaIndiaCoffeeTruckinWblistBinding
import com.olam.warehouse.vegax.weighment.ui.VegaIndiaCoffeeReceivingViewModel
import com.olam.warehouse.vegax.weighment.utils.*
import kotlinx.android.synthetic.main.item_vega_india_coffee_truckout_wb_list.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaIndiaCoffeeTruckInListFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private var receivingData = VegaReceiving()
    private var mReceiving = mutableListOf<VegaReceiving>()
    private val mSearchList = mutableListOf<VegaReceiving>()

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            receivingData: VegaReceiving
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    private val vm: VegaIndiaCoffeeReceivingViewModel by viewModel()
    private lateinit var binding: FragmentVegaIndiaCoffeeTruckinWblistBinding
    override val layoutResourceId = R.layout.fragment_vega_india_coffee_truckin_wblist

    companion object {
        fun newInstance(receivingData: VegaReceiving) = VegaIndiaCoffeeTruckInListFragment().putArgs {
            putParcelable(RECEIVING_DATA, receivingData)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaIndiaCoffeeTruckinWblistBinding.inflate(layoutInflater)
        return binding.root
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
                            setUpAdapter(mReceiving)
                        } else {
                            mSearchList.clear()
                            mReceiving.forEach { qtyWb ->
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


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("receiving/ui/truckin/VegaTruckInListFragment").title("Receiving").with(tracker)
        initUI()
    }

    private fun initUI() {
        receivingData = arguments?.getParcelable(RECEIVING_DATA)!!
        vm.truckInWeighBridge.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        vm.getTruckInWeighBridgeDetail()
        binding.ivSortDownUp.setOnClickListener {
            if (mReceiving.isNotEmpty()) {
                val data = mReceiving
                mReceiving = data.asReversed()
                setUpAdapter(mReceiving)
            }
        }

        binding.llAddTruckIn.setOnClickListener { moveToNext() }
    }

    private fun moveToNext() {
        when (receivingData.weighBridgeType == PROCURE) {
            true -> callBack?.replaceFragment(SUPPLIER_FRAG, receivingData)
            else -> callBack?.replaceFragment(MTNR_FRAG, receivingData)
        }
    }

    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaReceiving>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        if (it1.isNotEmpty()) {
                            var vegaWbIds = listOf<VegaReceiving>()
                            if (receivingData.weighBridgeType == PROCURE)
                                vegaWbIds = it1.filter { wb -> wb.direction == DIRECTIONIN }
                                    .filter { wb -> wb.weighBridgeType == PROCURE }
                            else
                                vegaWbIds = it1.filter { wb -> wb.direction == DIRECTIONIN }
                                    .filter { wb -> wb.weighBridgeType == STO }

                            vegaWbIds.forEach { item ->
                                item.tmpWbId = item.weighBridgeId
                                item.truckDirection = DIRECTIONIN
                            }
                            if (vegaWbIds.size > 0) {
                                mReceiving = vegaWbIds as MutableList<VegaReceiving>
                                setUpAdapter(vegaWbIds)
                                binding.tvNoData.gone()
                                binding.rvWeighbridge.visible()
                            } else {
                                binding.tvNoData.visible()
                                binding.rvWeighbridge.gone()
                            }
                        } else {
                            binding.tvNoData.visible()
                            binding.rvWeighbridge.gone()
                        }

                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }


    private fun setUpAdapter(data: List<VegaReceiving>?) {
        val receiving = data as MutableList<VegaReceiving>
        binding.rvWeighbridge.setUp(receiving.asReversed(), R.layout.item_vega_india_coffee_truckin_wb_list, { it, pos ->
            tvTruckNo.text = if (it.vehicleNumber.isNullOrEmpty()) "-" else it.vehicleNumber
            tvWeighBridgeId.text = it.weighBridgeId
            if (it.weighBridgeType == PROCURE) {
                tvdifference.text = SUPPLIER
                tvSupplierName.text = it.supplierCode.plus("-").plus(it.supplierName)
            } else {
                tvdifference.visibility = View.GONE
                tvSupplierName.visibility = View.GONE
                tvdifference.text = WAREHOUSE
                tvSupplierName.text = "-"
            }

                if ("null" != it.erdat) {
                    val times = it.erdat?.split('(', ')')
                    tvDate.text = times?.get(1)?.let { it1 ->
                        DateUtils.getUTCDateTime(
                            it1,
                            App.getAppContext()
                        )
                    }
                }
            },
            {
                val item = this
                item.weighBridgeType = receivingData.weighBridgeType
                item.truckDirection = receivingData.truckDirection
                item.bagTareWeight = "0"
                when (receivingData.weighBridgeType == PROCURE) {
                    true -> callBack?.replaceFragment(SUPPLIER_FRAG, item)
                    else -> callBack?.replaceFragment(MTNR_FRAG, item)
                }
            })
    }
}
