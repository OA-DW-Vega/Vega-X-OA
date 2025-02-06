package com.olam.warehouse.vegax.mtntghana.ui.weighbridge

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntghana.R
import com.olam.warehouse.vegax.mtntghana.databinding.FragmentGhanaTruckListLayoutBinding
import com.olam.warehouse.vegax.mtntghana.databinding.ItemGhanaMtntTruckLayoutBinding
import com.olam.warehouse.vegax.mtntghana.ui.VegaGhanaMtntViewModel
import com.olam.warehouse.vegax.mtntghana.ui.VegaGhanaReplaceFragmentCallback
import com.olam.warehouse.vegax.mtntghana.utils.DIRECTIONOUT
import com.olam.warehouse.vegax.mtntghana.utils.WEIGHBRIDGE
import com.olam.warehouse.vegax.mtntghana.utils.WEIGHBRIDGE_ADD_LOT
import com.olam.warehouse.vegax.mtntghana.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaGhanaMtntWeighbridgeTruckListFragment : BaseFragment() {
    override val layoutResourceId: Int = R.layout.fragment_ghana_truck_list_layout
    private lateinit var binding: FragmentGhanaTruckListLayoutBinding
    private var callBack: VegaGhanaReplaceFragmentCallback? = null
    private val vm: VegaGhanaMtntViewModel by viewModel()

    private var dispatch = mutableListOf<VegaCocoaDispatchWB>()
    private val mSearchList = mutableListOf<VegaCocoaDispatchWB>()
    private var vegaWbIds = mutableListOf<VegaCocoaDispatchWB>()

    companion object {
        fun newInstance() = VegaGhanaMtntWeighbridgeTruckListFragment()
            .putArgs {
            }

        const val SEARCH_HINT_TEXT = "Search truck Item"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaGhanaReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGhanaTruckListLayoutBinding.inflate(inflater)
        setHasOptionsMenu(true)
        initUI()
        return binding.root
    }

    private fun initUI() {
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


    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.data?.let {
                    if (it.isNotEmpty()) {
                        vegaWbIds =
                            it.filter { it.batchPicking.isNullOrEmpty() && it.direction.equals(DIRECTIONOUT) } as ArrayList
                        dispatch = vegaWbIds
                        dispatch.forEach { it.weighBridgeType = WEIGHBRIDGE }
                        vegaWbIds.sortBy { it.erdat }
                        setUpAdapter(vegaWbIds.asReversed())
                        //setUpAdapter(vegaWbIds)
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
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun setUpAdapter(data: List<VegaCocoaDispatchWB>?) {
        val dispatch1 = data as MutableList<VegaCocoaDispatchWB>
        binding.rvLots.setUpAdapter(
            dispatch1,
            R.layout.item_ghana_mtnt_truck_layout,
            ItemGhanaMtntTruckLayoutBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvTruckNo.text = if (it.vehicleNumber.isEmpty()) "-" else it.vehicleNumber
                bindItem.tvMaterialName.text = it.materialName
                bindItem.tvWbId.text = it.weighBridgeId
                val times = it.erdat?.split('(', ')')
                bindItem.tvDate.text = times?.get(1).let { it1 ->
                    it1?.let { it2 ->
                        DateUtils.getUTCDateTime(
                            it2,
                            App.getAppContext()
                        )
                    }
                }
            },
            {
                val item = this
                callBack?.replaceFragment(WEIGHBRIDGE_ADD_LOT, item)
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
            searchView.queryHint =
                SEARCH_HINT_TEXT
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
