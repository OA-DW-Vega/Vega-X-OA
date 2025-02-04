package com.olam.warehouse.vegax.receiving.ui.truckout

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
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.receiving.R
import com.olam.warehouse.vegax.receiving.databinding.FragmentVegaTruckoutWblistBinding
import com.olam.warehouse.vegax.receiving.ui.VegaReceivingViewModel
import com.olam.warehouse.vegax.receiving.utils.*
import kotlinx.android.synthetic.main.item_vega_truckout_wb_list.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 1/28/2020.
 */
class VegaTruckOutWBListFragment : BaseFragment() {

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

    private val vm: VegaReceivingViewModel by viewModel()
    private lateinit var binding: FragmentVegaTruckoutWblistBinding
    override val layoutResourceId = R.layout.fragment_vega_truckout_wblist

    companion object {
        fun newInstance(receivingData: VegaReceiving) = VegaTruckOutWBListFragment().putArgs {
            putParcelable(RECEIVING_DATA, receivingData)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaTruckoutWblistBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        //search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
        //searchView = search?.actionView as SearchView?
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
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
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("receiving/ui/truckout/VegaTruckOutWBListFragment - ${receivingData.weighBridgeType}")
            .title("Receiving").with(tracker)
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llSortBy, it, false)
        }
        receivingData = arguments?.getParcelable(RECEIVING_DATA)!!
        vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        vm.getWeighBridgeData()
        binding.ivSortDownUp.setOnClickListener {
            if (mReceiving.isNotEmpty()) {
                val data = mReceiving
                mReceiving = data.asReversed()
                setUpAdapter(mReceiving)
            }
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
                            //val vegaWbIds = it1.filter { wb -> wb.qcStatus.equals("R") || wb.qcStatus.equals("X")}
                            if (receivingData.weighBridgeType == PROCURE)
                                vegaWbIds =
                                    it1.filter { wb -> wb.direction == DIRECTIONIN }
                                        .filter { wb -> wb.weighBridgeType == PROCURE }
                                        .filter { wb -> !wb.qcStatus.equals("") }
                                        .filter { wb -> wb.netWeight.equals("0.000") }
                            else
                                vegaWbIds =
                                    it1.filter { wb -> wb.direction == DIRECTIONIN }
                                        .filter { wb -> wb.weighBridgeType == STO }
                                        .filter { wb -> !wb.qcStatus.equals("") }
                                        .filter { wb -> wb.netWeight.equals("0.000") }

                            vegaWbIds.forEach { item ->
                                item.tmpWbId = item.weighBridgeId
                                item.truckDirection = DIRECTIONOUT
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
        binding.rvWeighbridge.setUp(receiving.asReversed(), R.layout.item_vega_truckout_wb_list, { it, pos ->
            tvTruckNo.text = if (it.vehicleNumber.isNullOrEmpty()) "-" else it.vehicleNumber
            tvWeighBridgeId.text = it.weighBridgeId
            if (it.weighBridgeType == PROCURE) {
                tvdifference.text = SUPPLIER
                tvSupplierName.text = it.supplierName ?: it.supplierCode
            } else {
                tvdifference.visibility = View.GONE
                tvSupplierName.visibility = View.GONE
                tvdifference.text = WAREHOUSE
                tvSupplierName.text = "-"
            }

            tvWeight.text = it.netWeight.plus(" ").plus(it.unitsOfMeasure)
            if ("null" != it.erdat) {
                val times = it.erdat?.split('(', ')')
                tvDate.text = times?.get(1)?.let { it1 ->
                    DateUtils.getUTCDateTime(
                        it1,
                        App.getAppContext()
                    )
                }
            }
        }, {
            val item = this
            item.weighBridgeType = receivingData.weighBridgeType
            item.truckDirection = receivingData.truckDirection
            callBack?.replaceFragment(TRUCKOUT_ADD_WEIGHT_FRAG, item)

        })
    }
}

