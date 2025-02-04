package com.olam.warehouse.vegax.offloadingcameroon.ui


import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.getMultiPlantList
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcameroon.R
import com.olam.warehouse.vegax.offloadingcameroon.databinding.FragmentVegaCameroonOffloadingTruckListBinding
import com.olam.warehouse.vegax.offloadingcameroon.utils.*
import kotlinx.android.synthetic.main.item_vega_cameroon_offloading_truck_list.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCameroonOffloadingTruckListFragment : BaseFragment() {

    private lateinit var binding: FragmentVegaCameroonOffloadingTruckListBinding
    private var callBack: CallBack? = null
    private var offloadingData = VegaOffloadingTrucks()
    private var offloading = mutableListOf<VegaOffloadingTrucks>()
    private val mSearchList = mutableListOf<VegaOffloadingTrucks>()

    override val layoutResourceId = R.layout.fragment_vega_cameroon_offloading_truck_list
    private val vm: VegaCameroonOffloadingViewModel by viewModel()
    private var vegaWbIds = listOf<VegaOffloadingTrucks>()
    private var selectedPlantId = ""
    private var plantList = mutableListOf<Plant>()

    interface CallBack {
        fun replaceFragment(
            paramsListFrag: String,
            item: VegaOffloadingTrucks,
            plantId: String,
            plantDetails: Plant
        )
    }

    companion object {
        fun newInstance(offloadingData: VegaOffloadingTrucks) = VegaCameroonOffloadingTruckListFragment().putArgs {
            putParcelable(OFFLOADING_DATA, offloadingData)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCameroonOffloadingTruckListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        val wType = offloadingData.weighBridgeType
        TrackHelper.track()
            .screen("offloadingcameroon/ui/VegaCameroonOffloadingTruckListFragment - $wType")
            .title("Vega_Cameroon/Offloading")
            .with(tracker)
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
        context?.let {
            getActionBtnChangedView(binding.llSortBy, it, false)
        }
        offloadingData = arguments?.getParcelable(OFFLOADING_DATA)!!
        vm.trucks.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        binding.tvType.text = getString(R.string.offloading_supplier)



        binding.ivSortDownUp.setOnClickListener {
            if (vegaWbIds.isNotEmpty()) {
                vegaWbIds.let { offloading = it as MutableList<VegaOffloadingTrucks> }
                offloading = offloading.asReversed()
                vegaWbIds = offloading
                setUpAdapter(vegaWbIds)
            }
        }
        when (offloadingData.weighBridgeType) {
            PROCURE -> {
                binding.tvTruckList.text = "WB ID List"
            }
            STO -> {
            }
        }

        binding.btnGo.setOnClickListener {
            if (selectedPlantId.isNotEmpty()) {
                if (isOnline()) {
                    if (isOnline()) vm.getTruckList("false", selectedPlantId)
                }
            } else
                Toast.makeText(context, getString(R.string.msg_select_plant), Toast.LENGTH_SHORT).show()
        }

        //Multi Plant selection
        //Should be moved to observer of plant list

        plantList = getMultiPlantList() as MutableList<Plant>
        var ids = plantList.map { it.plantId }
        updatePlantListUI(ids as ArrayList<String>)



    }

    private fun updatePlantListUI(plantList: ArrayList<String>) {
        binding.spPlantSelection.isEnabled = true
        var plantIdList = ArrayList<String>()
        plantIdList.add(getString(R.string.select_plant_id))
        plantIdList.addAll(plantList)
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_cameroon_offloading_batchno_grade, plantIdList)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spPlantSelection.adapter = stageAdapter
        binding.spPlantSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (position > 0) {
                    selectedPlantId = plantIdList[position]
                }
            }
        }
    }


    private fun updateUIWithOnlineData(response: Resource<List<VegaOffloadingTrucks>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.let { it1 ->
                    if (it1.isNotEmpty()) {
                        vegaWbIds = listOf<VegaOffloadingTrucks>()
                        vegaWbIds = if (offloadingData.weighBridgeType == PROCURE)
                            it1.filter { wb -> wb.direction == DIRECTIONIN }
                                .filter { wb -> wb.weighBridgeType == PROCURE }
                                .filter { wb -> wb.qcStatus.isNullOrEmpty() }
                                .filter { wb -> wb.grossWeight.equals("0.000") }
                        else
                            it1.filter { wb -> wb.direction == DIRECTIONIN }
                                .filter { wb -> wb.weighBridgeType == STO }
                                .filter { wb -> wb.qcStatus.isNullOrEmpty() }
                                .filter { wb -> !wb.grossWeight.equals("0.000") }
                        offloading = vegaWbIds as MutableList<VegaOffloadingTrucks>
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

    private fun setUpAdapter(data: List<VegaOffloadingTrucks>?) {
        val offloading1 = data as MutableList<VegaOffloadingTrucks>
        binding.rvWeighbridge.setUp(
            offloading1.asReversed(),
            R.layout.item_vega_cameroon_offloading_truck_list,
            { it, pos ->
                tvTruckNo.text = it.materialName
                if (it.weighBridgeType == PROCURE) {
                    tvdifference.text = SUPPLIER_Caps
                    tvSupplierName.text = it.supplierName ?: it.supplierCode
                } else {
                    tvdifference.visibility = View.GONE
                    tvSupplierName.visibility = View.GONE
                    tvdifference.text = WAREHOUSE
                    tvSupplierName.text = "-"
                }
                tvWeight.text = it.delivery
                tvWeighBridgeId.text = it.weighBridgeId
                val times = it.erdat?.split('(', ')')
                tvDate.text = times?.get(1)?.let { it1 ->
                    DateUtils.getUTCDateTime(
                        it1,
                        App.getAppContext()
                    )
                }
            },
            {
                val item = this
                item.weighBridgeType = offloadingData.weighBridgeType
                var plantDetails = getPlantDetails(selectedPlantId).single()
                callBack?.replaceFragment(TRUCK_LIST_FRAG, item, selectedPlantId, plantDetails)

            })
    }

    private fun getPlantDetails(plantId: String?): List<Plant> {
        return plantList.filter { it.plantId == plantId }
    }

}
