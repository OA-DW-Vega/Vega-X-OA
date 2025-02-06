package com.olam.warehouse.vegax.mtntcocoa.ui.mtnt

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.presentation.adapter.setUpAdapter
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
import com.olam.warehouse.vegax.mtntcocoa.R
import com.olam.warehouse.vegax.mtntcocoa.databinding.FragmentDisbatchMtntLayoutBinding
import com.olam.warehouse.vegax.mtntcocoa.databinding.ItemMtntTruckLayoutBinding
import com.olam.warehouse.vegax.mtntcocoa.ui.CallBack
import com.olam.warehouse.vegax.mtntcocoa.ui.VegaCocoaMtntViewModel
import com.olam.warehouse.vegax.mtntcocoa.utils.WEIGH_METHOD_WB
import com.olam.warehouse.vegax.mtntcocoa.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaMtntTruckListFragment : BaseFragment() {
    override val layoutResourceId: Int = R.layout.fragment_disbatch_mtnt_layout
    private lateinit var binding: FragmentDisbatchMtntLayoutBinding
    private var callBack: CallBack? = null
    private val vm: VegaCocoaMtntViewModel by viewModel()

    private var dispatch = mutableListOf<VegaCocoaDispatchWB>()
    private val mSearchList = mutableListOf<VegaCocoaDispatchWB>()
    private var vegaWbIds = mutableListOf<VegaCocoaDispatchWB>()
    private var isCompliantMaterial: Boolean = false
    companion object {
        fun newInstance() = VegaMtntTruckListFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = "Search truck Item"
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
        binding = FragmentDisbatchMtntLayoutBinding.inflate(inflater)
        setHasOptionsMenu(true)
        initUI()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("/mtntcocoa/ui/mtnt/VegaMtntTruckListFragment")
            .title("Dispatch Cocoa")
            .with(tracker)
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
                            it.filter { it.batchPicking.isNullOrEmpty() && it.delivery.isNullOrEmpty() && it.weighMethod == WEIGH_METHOD_WB } as ArrayList
                        dispatch = vegaWbIds
                        setUpAdapter(vegaWbIds)
                        binding.tvNoTruck.gone()
                        binding.rvLots.visible()
                        binding.tvTruckIDNo.text =
                            getString(R.string.total_lot).plus(" ").plus(vegaWbIds.size)
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
            R.layout.item_mtnt_truck_layout,
            ItemMtntTruckLayoutBinding::inflate,
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
                if(this.materialCode.isNullOrEmpty()){
                    showTTDialog(this)
                }else {
                    val item = this
                    callBack?.replaceFragment("Add_Lot", item)
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
       // menu.clear()
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

    private fun showTTDialog(vegaCocoaDispatchWB: VegaCocoaDispatchWB) {
        MaterialDialog(requireContext()).show {
            cancelOnTouchOutside(false)
            message(com.olam.warehouse.login.R.string.select_procurement_type)
            UIUtils.getTTDialogOnlyForMaterial(
                this,
                "",
                "",

                {
                        isComplaint, isThirdParty,isFarmerLessTransaction ->

                    if (isComplaint)
                        vegaCocoaDispatchWB.isCompliant = true
                    else
                        vegaCocoaDispatchWB.isCompliant = false
                    callBack?.replaceFragment("Add_Lot", vegaCocoaDispatchWB)
                },
                { dismiss() },
                false)
        }

    }
}
