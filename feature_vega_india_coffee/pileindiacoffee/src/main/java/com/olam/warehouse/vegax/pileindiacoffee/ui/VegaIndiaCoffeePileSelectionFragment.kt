package com.olam.warehouse.vegax.pileindiacoffee.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.pileindiacoffee.R
import com.olam.warehouse.vegax.pileindiacoffee.databinding.FragmentIndiaCoffeePileManagementPileSelectionBinding
import com.olam.warehouse.vegax.pileindiacoffee.databinding.ItemIndiaCoffeePileManagmentSelectionBinding
import com.olam.warehouse.vegax.pileindiacoffee.ui.data.domain.model.VegaPileSelectionModel
import com.olam.warehouse.vegax.pileindiacoffee.ui.data.domain.model.VegaPileSequence
import com.olam.warehouse.vegax.pileindiacoffee.ui.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaIndiaCoffeePileSelectionFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_india_coffee_pile_management_pile_selection
    private lateinit var binding: FragmentIndiaCoffeePileManagementPileSelectionBinding
    private var callBack: CallBack? = null
    private val vm: VegaIndiaCoffeePileManagementViewModel by viewModel()
    private var materialList: String = ""
    private var customlist = ArrayList<String>()
    private var fromVegaVendor = arrayListOf<VegaVendor>()
    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var pileselectionList = mutableListOf<VegaPileSelectionModel>()
    private var filterData = mutableListOf<VegaPileSelectionModel>()
    private var mSearchList = mutableListOf<VegaPileSelectionModel>()
    private var vendorcode: String = ""
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var materialArrayList = ArrayList<String>()
    private var storageLocation: String = ""
    private var procureLocationCode: String = ""
    private var procureLocationName: String = ""
    private var startRange: String = "0"
    private var endRange: String = "30"
    private var aboveThirty: Boolean = false
    private var fullFilter = java.util.ArrayList<String>()
    private var isNewLotCreate: Boolean = false
    private var isChecked: Boolean? = false
    private var fulllist = VegaCocoaDispatchLots()
    private var alreadySelected = arrayListOf<VegaCocoaDispatchLots>()
    private var pileSelection = VegaPileSelectionModel()
    private var selectedPosition=-1


    interface CallBack {
        fun replaceFragment(fragFilter: String, bundle: Bundle, fullFilter: java.util.ArrayList<String>)
        fun replaceFragment(
            fragment: String,
            materialArrayList: ArrayList<String>,
            alreadySelected: ArrayList<VegaCocoaDispatchLots>,
            pileSelectionList: VegaPileSelectionModel
        )

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(
            model: ArrayList<VegaCocoaDispatchLots>, materialList: String,
            fromVendorList: String
        ) =
            VegaIndiaCoffeePileSelectionFragment().putArgs {
                putString(MATERIAL_LIST, materialList)
                putParcelableArrayList(MODEL_BUNDLE, model)
                putString(PILE_SELECT, fromVendorList)
            }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentIndiaCoffeePileManagementPileSelectionBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("pilemanagement/ui//VegaCoffeePileManagementFragment").title("PileMangement")
            .with(tracker)
        initUI()
    }

    private fun initUI() {

        alreadySelected = arguments?.getParcelableArrayList<VegaCocoaDispatchLots>(MODEL_BUNDLE) as ArrayList
        vendorcode = arguments?.getString(PILE_SELECT) ?: ""
        materialList = arguments?.getString(MATERIAL_LIST) ?: ""
        vm.stockPile.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getStockPiles(materialList)

        binding.tvCreateLot.setOnClickListener {
            when (binding.clCreateLot.isVisible) {
                true -> {
                    binding.clCreateLot.gone()
//                    binding.tvCreateLot.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.ic_arrow_down_black_24dp)
                }
                false -> {
                    setVisibility()
//                    binding.tvCreateLot.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.ic_arrow_up)
                    binding.clCreateLot.visible()
                }
            }
        }

        binding.tvInventory.setOnClickListener {
            when (binding.clInventory.isVisible) {
                true -> {
                    binding.clInventory.gone()
//                    binding.tvInventory.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.ic_arrow_down_black_24dp)
                }
                false -> {
                    setVisibility()
//                    binding.tvInventory.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.ic_arrow_up)
                    binding.clInventory.visible()
                }
            }
        }

        binding.tvFilter.setOnClickListener { filterActivity() }

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            customLocationList = it.toMutableList()
        })
        vm.getCustomLocations()

        binding.spDefaultStorageLoc.setOnClickListener {
            customlist.clear()
            showMaterialDialog(customLocationList)
        }

        binding.tvAssignLot.setOnClickListener { movetoSummary() }

        vm.createPile.observe(viewLifecycleOwner, Observer { updatePileCreateUI(it) })

        binding.cbCreateNewLot.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                enableDisableBtn(true)
                isNewLotCreate = true
                vm.getCreatePile(false)
            } else {
                enableDisableBtn(false)
                isNewLotCreate = false
            }
        }


    }


    private fun updatePileCreateUI(response: Resource<GenericReqAndResp<VegaPileSequence>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            pileSelection.batchNumber = it.data?.data?.pileSequence
                            pileSelection.materialCode = materialList
                            pileSelection.plantId = PreferenceHelper.get(Constants.WERKS, "")
                            pileSelection.vendor = vendorcode
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

    private fun movetoSummary() {
        activity?.onBackPressed()
        pileSelection.vendor = vendorcode
        pileSelection.materialCode = materialList
        callBack?.replaceFragment(
            PILE_MANAGEMENT_SUMMARY,
            materialArrayList,
            alreadySelected,
            pileSelection
        )
    }


    private fun showMaterialDialog(materialList: List<VegaCustomStLocation>) {
        val suppliers = materialList.map { it.procureLocationCode.plus("-").plus(it.procureLocationName) }
        MaterialDialog(requireContext()).show {
            title(R.string.select_storage_location)
            listItemsSingleChoice(items = suppliers) { _, index, text ->
                binding.spDefaultStorageLoc.text = text
                procureLocationCode = materialList[index].procureLocationCode
                procureLocationName = materialList[index].procureLocationName ?: ""
                pileSelection.storageLocationCode = procureLocationCode
                if (suppliers.size > 0)
                    materialArrayList.add(procureLocationName.plus(procureLocationCode))
            }
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.ok),
                    true
                )
            )

        }
    }

    private fun setVisibility() {
        binding.clCreateLot.gone()
        binding.clInventory.gone()
    }

    private fun filterActivity() {
        val bundle = Bundle()
        val stoFilter = ArrayList<String>()
        val stoLoc = pileselectionList.map { it.storageLocationCode!! }
        stoFilter.addAll(stoLoc)
        bundle.putStringArrayList(FILTER_WH_LOC, stoFilter)
        bundle.putString(FILTER_START_RANGE, startRange)
        bundle.putString(FILTER_END_RANGE, endRange)
        bundle.putBoolean(FILTER_ABOVE_RANGE, aboveThirty)
        callBack?.replaceFragment(FRAG_FILTER, bundle, fullFilter)
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaPileSelectionModel>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            pileselectionList.clear()
                            /*val filteredDispatchStocksList = mutableListOf<VegaCocoaDispatchLots>()
                            val dataValue = it.data?.data!!
                            dataValue.let { item -> filteredDispatchStocksList.addAll(item) }*/
                            pileselectionList =
                                if (it.data?.data!!.size > 0) it.data?.data as MutableList<VegaPileSelectionModel> else mutableListOf()
                            updateAdapter(pileselectionList)

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

    private fun updateAdapter(stockList: MutableList<VegaPileSelectionModel>) {
        when {
            pileselectionList.size > 0 -> {
                binding.rvInventoryLot.visible()
                binding.tvNoData.gone()
            }
            else -> {
                binding.rvInventoryLot.gone()
                binding.tvNoData.visible()
            }
        }
        binding.rvInventoryLot.setUpAdapter(
            stockList,
            R.layout.item_india_coffee_pile_managment_selection,
            ItemIndiaCoffeePileManagmentSelectionBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvPileNo.text = it.batchNumber
                bindItem.tvWeight.text = it.weight.plus(" ").plus(it.unitOfMeasure)

                bindItem.checkbox.isChecked = it.isChecked!!
                bindItem.llLotItem.setOnClickListener { view ->
                    stockList.forEach { it.isChecked = false }
                    stockList[pos].isChecked = !it.isChecked!!
                    if (it.isChecked!!) pileSelection = it
                    binding.rvInventoryLot.adapter?.notifyDataSetChanged()
                    enableDisableBtn(stockList.any { it.isChecked!! })
                }
                bindItem.checkbox.setOnClickListener {
                    bindItem.llLotItem.performClick()
                }
            })
    }




    private fun enableDisableBtn(flag: Boolean) {
        if (flag) {
            binding.tvAssignLot.isEnabled = true
            binding.tvAssignLot.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.tvAssignLot.isEnabled = false
            binding.tvAssignLot.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
    }

    fun applyFilter(bundle: Bundle) {
        val stoLocation = bundle.getStringArrayList(FILTER_WH_LOC) ?: ArrayList()
        startRange = bundle.getString(FILTER_START_RANGE) ?: ""
        endRange = bundle.getString(FILTER_END_RANGE) ?: ""
        aboveThirty = bundle.getBoolean(FILTER_ABOVE_RANGE, false)
        fullFilter = stoLocation
        if (!aboveThirty && startRange.equals("0") && endRange.equals("30") && stoLocation.size == 0) {
            updateAdapter(pileselectionList)
        } else {
            filterData.clear()
            var filterData1 = mutableListOf<VegaPileSelectionModel>()
            pileselectionList.forEach { stock ->
                stoLocation.forEach { loc ->
                    if (loc.contains(stock.storageLocationCode.toString())) {
                        filterData1.add(stock)
                    }
                }
            }
            if (stoLocation.size == 0) filterData1 = pileselectionList

            when {
                !aboveThirty && startRange.equals("0") && endRange.equals("30") -> filterData = filterData1
                !aboveThirty -> {
                    filterData1.forEach {
                        if (startRange.toDouble() <= it.weight?.toDouble() ?: 0.0 && it.weight?.toDouble() ?: 0.0 <= endRange.toDouble())
                            filterData.add(it)
                    }
                }
                else -> {
                    filterData1.forEach {
                        if ("30".toDouble() < it.weight?.toDouble() ?: 0.0)
                            filterData.add(it)
                    }
                }
            }
            updateAdapter(filterData)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint = getString(com.olam.warehouse.presentation.R.string.search_by_lot)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            updateAdapter(pileselectionList)
                        } else {
                            mSearchList.clear()
                            pileselectionList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.batchNumber!!.contains(text)) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            updateAdapter(mSearchList)
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


