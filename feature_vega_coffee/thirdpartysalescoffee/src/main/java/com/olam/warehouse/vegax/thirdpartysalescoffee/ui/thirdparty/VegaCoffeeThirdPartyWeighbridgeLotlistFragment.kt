package com.olam.warehouse.vegax.thirdpartysalescoffee.ui.thirdparty

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.thirdpartysalescoffee.R
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeThirdPartyLotListModel
import com.olam.warehouse.vegax.thirdpartysalescoffee.databinding.FragmentThirdPartyWeighbridgeInventoryBinding
import com.olam.warehouse.vegax.thirdpartysalescoffee.databinding.ItemWeighbridgeInventoryLotBinding
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCoffeeThirdPartyAddLotListener
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCoffeeThirdPartyLotListFragment
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCoffeeThirdPartyViewModel
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.getColor
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.listOfField
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCoffeeThirdPartyWeighbridgeLotlistFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_third_party_weighbridge_inventory
    private lateinit var binding: FragmentThirdPartyWeighbridgeInventoryBinding
    private val vm: VegaCoffeeThirdPartyViewModel by viewModel()
    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var filteredDispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var wareHouseList = mutableListOf<String>()
    private var materialList = arrayListOf<String>()
    private var listener: VegaCoffeeThirdPartyAddLotListener? = null
    private var alreadySelected = mutableListOf<VegaCocoaDispatchLots>()
    private val mSearchList = mutableListOf<VegaCocoaDispatchLots>()
    private var isMultipleAdd = true
    private var materialCode: ArrayList<String> = ArrayList()
    private var isThirdPartyMaterial = false
    private var vendorCode = ""


    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? VegaCoffeeThirdPartyAddLotListener
    }

    companion object {
        fun newInstance(
            vegaLotModel: VegaCoffeeThirdPartyLotListModel
        ) =
            VegaCoffeeThirdPartyLotListFragment().putArgs {
                putParcelableArrayList("SelectedList", vegaLotModel.selectedList)
                putBoolean("MULTIPLE_LOT", vegaLotModel.isMultipleAdd)
                putStringArrayList("Material", vegaLotModel.material)
                putBoolean("thirdParty", vegaLotModel.isThirdParty)
                putString("vendor", vegaLotModel.vendorCode)
            }

        const val SEARCH_HINT_TEXT = "Search Lot Item"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentThirdPartyWeighbridgeInventoryBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("pilemanagement/ui//VegaCoffeePileManagementFragment").title("IVC/Coffee/Third Party Sales/Weighbridge Lot List")
            .with(tracker)
    }

    private fun initExtra() {
        alreadySelected = arguments?.getParcelableArrayList<VegaCocoaDispatchLots>("SelectedList") as ArrayList
        isMultipleAdd = arguments?.getBoolean("MULTIPLE_LOT") ?: true
        materialList = arguments?.getStringArrayList("Material") ?: ArrayList()
        isThirdPartyMaterial = arguments?.getBoolean("thirdParty") ?: false
        vendorCode = arguments?.getString("vendor", "") ?: ""
    }

    private fun initUI() {
        vm.stockList.observe(viewLifecycleOwner, Observer { updateUI(it) })
        //vm.stocksLocal.observe(viewLifecycleOwner, Observer { updateLocalUI(it) })
        if (materialList.size > 0) {
            vm.getStockList(materialList)
        }
        binding.btnProceed.setOnClickListener { sendSelectedLots() }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()
                            val dataValue = it.data?.data!!
                            val filterVendor = dataValue.filter { it.vendor == vendorCode }
                            dispatchLotsList.addAll(filterVendor)
                            updateSelectLotValues()
                            getWarehouseList()
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                    hideLoading()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun updateSelectLotValues() {
        val filter = mutableListOf<VegaCocoaDispatchLots>()
        filter.addAll(dispatchLotsList)
        if (materialList.isNotEmpty()) {
            filter.forEachIndexed { index, s ->
                dispatchLotsList.forEach { item ->
                    if (s.batchNumber.equals(item.batchNumber) && s.materialCode.equals(item.materialCode) && s.storageLocationCode.equals(
                            item.storageLocationCode
                        )
                    )
                        dispatchLotsList[index].isAdded = false
                }
            }
        }
        setupAdapter(dispatchLotsList)
    }


    private fun setupAdapter(data: MutableList<VegaCocoaDispatchLots>) {
        if (data.size > 0) {
            binding.rvLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
        }
        binding.rvLots.setUpAdapter(
            data,
            R.layout.item_weighbridge_inventory_lot,
            ItemWeighbridgeInventoryLotBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvLotId.text = it.batchNumber
                bindItem.tvGradeValue.text = it.materialName
                bindItem.tvStLocation.text = it.storageLocationCode
                bindItem.tvWeightValue.text =
                    it.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitOfMeasure)
                bindItem.ivSelect.isChecked = it.isAdded
                bindItem.llLotItem.setOnClickListener { view ->
                    it.isAdded = !it.isAdded
                    bindItem.ivSelect.isChecked = it.isAdded

                }

                bindItem.ivSelect.setOnClickListener { view ->
                    it.isAdded = !it.isAdded
                    bindItem.ivSelect.isChecked = it.isAdded
                }
            })
    }

    private fun removeChecked(item: Int, list: MutableList<VegaCocoaDispatchLots>) {
        list.forEach { it.isAdded = false }
        list[item].isAdded = true
        setupAdapter(list)
    }

    private fun getWarehouseList() {
        if (dispatchLotsList.isNotEmpty()) {
            wareHouseList.add(getString(R.string.all))
            wareHouseList.addAll(dispatchLotsList.listOfField(VegaCocoaDispatchLots::storageLocationCode).toSet())
            updateWareHouseSpinner()
        } else {
            setupAdapter(dispatchLotsList)
        }
    }

    private fun updateWareHouseSpinner() {
        val stageAdapter = ArrayAdapter(requireContext(), R.layout.item_weighbridge_inventory_spinner, wareHouseList)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spWareHouse.adapter = stageAdapter
        val defaultposition = 0
        binding.spWareHouse.setSelection(defaultposition)
        binding.spWareHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filteredDispatchLotsList.clear()
                if (position > 0) {
                    val storageLocation = wareHouseList[position]
                    val lotsList = dispatchLotsList.filter { it.storageLocationCode == storageLocation }
                    filteredDispatchLotsList.addAll(lotsList)
                    setupAdapter(filteredDispatchLotsList)
                } else {
                    setupAdapter(dispatchLotsList)
                    filteredDispatchLotsList.clear()
                }
            }
        }
    }

    private fun sendSelectedLots() {
        val data = dispatchLotsList.filter { it.isAdded == true } as ArrayList
        listener?.addedLots(data)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint = getString(R.string.search_by_lots)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setupAdapter(if (filteredDispatchLotsList.isEmpty()) dispatchLotsList else filteredDispatchLotsList)
                        } else {
                            mSearchList.clear()
                            (if (filteredDispatchLotsList.isEmpty()) dispatchLotsList else filteredDispatchLotsList).forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.batchNumber.contains(text)) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            setupAdapter(mSearchList)
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
