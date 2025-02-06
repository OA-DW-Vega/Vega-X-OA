package com.olam.warehouse.vegax.offloadingnigeria.ui.supplier

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getMultiPlantList
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingnigeria.R
import com.olam.warehouse.vegax.offloadingnigeria.databinding.FragmentVegaNigeriaOffloadingWaitingTruckListBinding
import com.olam.warehouse.vegax.offloadingnigeria.databinding.ItemVegaNigeriaOffloadingWaitingTruckListBinding
import com.olam.warehouse.vegax.offloadingnigeria.ui.VegaNigeriaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaOffloadingWaitingTruckListFragment : BaseFragment() {

    private lateinit var binding: FragmentVegaNigeriaOffloadingWaitingTruckListBinding
    private var callBack: CallBack? = null
    private var offloadingData = VegaOffloadingTrucks()
    private var offloading = mutableListOf<VegaOffloadingTrucks>()
    private val mSearchList = mutableListOf<VegaOffloadingTrucks>()
    private var selectedPlantId = ""
    private var plantList = mutableListOf<Plant>()
    private var ttProcurementType=""

    override val layoutResourceId = R.layout.fragment_vega_nigeria_offloading_waiting_truck_list
    private val vm: VegaNigeriaOffloadingViewModel by viewModel()
    private var vegaWbIds = listOf<VegaOffloadingTrucks>()

    interface CallBack {
        fun replaceFragment(paramsListFrag: String, item: VegaOffloadingTrucks, procurementType: String?)
    }

    companion object {
        fun newInstance(offloadingData: VegaOffloadingTrucks, ttProcurementType: String) = VegaNigeriaOffloadingWaitingTruckListFragment().putArgs {
            putParcelable(OFFLOADING_DATA, offloadingData)
            putString(TT_PROCURE_TYPE, ttProcurementType)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNigeriaOffloadingWaitingTruckListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        val wType = offloadingData.weighBridgeType
        TrackHelper.track().screen("offloading/ui/truck/VegaOffloadingTruckListFragment - $wType").title("Offloading")
            .with(tracker)
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
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint = SEARCH_HINT_TEXT
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setUpAdapter(offloading)
                        } else {
                            mSearchList.clear()
                            offloading.forEach { qtyWb ->
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

    private fun initUI() {
        offloadingData = arguments?.getParcelable(OFFLOADING_DATA)!!
        ttProcurementType= arguments?.getString(TT_PROCURE_TYPE).toString()
        // vm.trucks.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        vm.qcweighBridge.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        binding.tvType.text =
            getString(R.string.offloading_truck_list)

        binding.ivSortDownUp.setOnClickListener {
            if (vegaWbIds.isNotEmpty()) {
                vegaWbIds.let { offloading = it as MutableList<VegaOffloadingTrucks> }
                offloading = offloading.asReversed()
                vegaWbIds = offloading
                setUpAdapter(vegaWbIds)
            }
        }

        binding.btnGo.setOnClickListener {
            if (selectedPlantId.isNotEmpty()) {
                // if (isOnline()) vm.getTruckList()
                if (isOnline()) vm.getQCWeighBridgeList(selectedPlantId)
            } else
                Toast.makeText(context, "Select Plant to continue", Toast.LENGTH_SHORT).show()
        }

        plantList = getMultiPlantList() as MutableList<Plant>
        var ids = plantList.map { it.plantId }
        updatePlantListUI(ids as ArrayList<String>)

    }

    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaOffloadingTrucks>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.let { it1 ->
                    if (it1.data.isNotEmpty()) {
                        vegaWbIds = listOf<VegaOffloadingTrucks>()
                        vegaWbIds = if (offloadingData.weighBridgeType == PROCURE) {
                            it1.data.filter { wb -> wb.direction == DIRECTIONIN }
                                .filter { wb -> wb.weighBridgeType == PROCURE }
                                .filter { wb -> wb.qcStatus.isNullOrEmpty() }
                                .filter { wb -> if(wb.weighMethod == WS) wb.grossWeight.equals("0.000") else !wb.grossWeight.equals("0.000") }
                        }else
                            it1.data.filter { wb -> wb.direction == DIRECTIONIN }
                                .filter { wb -> wb.weighBridgeType == STO }
                                .filter { wb -> wb.qcStatus.isNullOrEmpty() }
                                .filter { wb -> wb.weighMethod == WS }
                                .filter { wb -> wb.grossWeight.equals("0.000") }
                        offloading = vegaWbIds as MutableList<VegaOffloadingTrucks>
                        setUpAdapter(vegaWbIds)

                    } else {
                        binding.tvNoData.visible()
                        binding.rvWeighbridge.gone()
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

    private fun updatePlantListUI(plantList: ArrayList<String>) {
        binding.spPlantSelection.isEnabled = true
        var plantIdList = ArrayList<String>()
        plantIdList.add(getString(R.string.select_plant_id))
        plantIdList.addAll(plantList)
        val stageAdapter =
            ArrayAdapter(
                requireContext(),
                R.layout.item_vega_nigeria_offloading_plant_select,
                plantIdList
            )
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spPlantSelection.adapter = stageAdapter
        binding.spPlantSelection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    if (position > 0) {
                        selectedPlantId = plantIdList[position]
//                    validateLot(selectedPlantId)
                    }
//                binding.spPlantSelection.setSelection(0)
                }
            }
    }

    private fun setUpAdapter(data: List<VegaOffloadingTrucks>?) {
        val offloading1 = data as MutableList<VegaOffloadingTrucks>
        if(offloading1.isEmpty()){
            binding.tvNoData.visible()
            binding.rvWeighbridge.gone()
        }else{    binding.tvNoData.gone()
            binding.rvWeighbridge.visible() }

        binding.rvWeighbridge.setUpAdapter(
            offloading1.asReversed(),
            R.layout.item_vega_nigeria_offloading_waiting_truck_list,
            ItemVegaNigeriaOffloadingWaitingTruckListBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvTruckNo.text =
                    if (it.vehicleNumber.isNullOrEmpty()) "-" else it.vehicleNumber
                if (it.weighBridgeType == PROCURE) {
                    bindItem.tvdifference.text = SUPPLIER
                    bindItem.tvSupplierName.text = it.supplierName ?: it.supplierCode
                } else {
                    bindItem.tvdifference.visibility = View.GONE
                    bindItem.tvSupplierName.visibility = View.GONE
                    //tvdifference.text = WAREHOUSE
                    bindItem.tvSupplierName.text = "-"
                }
                bindItem.tvWeight.text = it.netWeight.plus(it.unitsOfMeasure)
                bindItem.tvWeighBridgeId.text = it.weighBridgeId
                val times = it.erdat?.split('(', ')')
                bindItem.tvDate.text = times?.get(1)?.let { it1 ->
                    DateUtils.getUTCDateTime(
                        it1,
                        App.getAppContext()
                    )
                }
            }, {
                val item = this
                item.weighBridgeType = offloadingData.weighBridgeType
                callBack?.replaceFragment(PARAMS_LIST_FRAG, item,ttProcurementType)

            })
    }

}
