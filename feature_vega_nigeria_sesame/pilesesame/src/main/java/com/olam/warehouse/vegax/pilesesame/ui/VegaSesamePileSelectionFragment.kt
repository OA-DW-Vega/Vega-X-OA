package com.olam.warehouse.vegax.pilesesame.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.adapter.setUp
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
import com.olam.warehouse.vegax.pilesesame.R
import com.olam.warehouse.vegax.pilesesame.data.domain.model.VegaSesamePileProcessType
import com.olam.warehouse.vegax.pilesesame.data.domain.model.VegaSesamePileProcessTypeModel
import com.olam.warehouse.vegax.pilesesame.databinding.FragmentPileManagementPileSelectionSesameBinding
import com.olam.warehouse.vegax.pilesesame.utils.*
import kotlinx.android.synthetic.main.item_pile_managment_selection_sesame.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList

class VegaSesamePileSelectionFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_pile_management_pile_selection_sesame
    private lateinit var binding: FragmentPileManagementPileSelectionSesameBinding
    private var callBack: CallBack? = null
    private val vm: VegaSesamePileManagementViewModel by viewModel()
    private var materialList = arrayListOf<String>()
    private var customlist = ArrayList<String>()
    private var fromVegaVendor = arrayListOf<VegaVendor>()
    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var pileselectionList = mutableListOf<VegaCocoaDispatchLots>()
    private var filterData = mutableListOf<VegaCocoaDispatchLots>()
    private var mSearchList = mutableListOf<VegaCocoaDispatchLots>()
    private var vendorcode: String = ""
    private var weightNewPile: String = ""
    private var storeLocNewPile: String = ""
    private var materialNewPile: String = ""
    private var materialCodeNewPile: String = ""
    private var vendorNewPile: String = ""
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var storageLocationList = mutableListOf<VegaStorageLocation>()
    private var materialArrayList = ArrayList<String>()
    private var storageLocation: String = ""
    private var procureLocationCode: String = ""
    private var procureLocationName: String = ""
    private var batchidlogic: String = ""
    private var processType: String? = ""
    private var pileSequence: String? = ""
    private var pile: String? = ""
    private var currentMaterialName: String = ""
    private var currentMaterialCode: String = ""
    private var jsonData = mutableListOf<String>()
    private var startRange: String = "0"
    private var endRange: String = "30"
    private var aboveThirty: Boolean = false
    private var fullFilter = java.util.ArrayList<String>()
    private var isNewLotCreate: Boolean = false
    private var isChecked: Boolean? = false
    private var fulllist = VegaCocoaDispatchLots()
    private var alreadySelected = arrayListOf<VegaCocoaDispatchLots>()
    private var pileSelection = VegaCocoaDispatchLots()
    private var thirdPartyMaterials = mutableListOf<VegaCoffeeThirdPartyMaterialDetail>()
    private var sapMaterials = mutableListOf<VegaMaterial>()


    interface CallBack {
        fun replaceFragment(fragFilter: String, bundle: Bundle, fullFilter: java.util.ArrayList<String>)
        fun replaceFragment(
            fragment: String,
            materialArrayList: ArrayList<String>,
            alreadySelected: ArrayList<VegaCocoaDispatchLots>,
            pileSelectionList: VegaCocoaDispatchLots
        )

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(
            model: ArrayList<VegaCocoaDispatchLots>, materialList: ArrayList<String>,
            fromVendorList: String
        ) =
            VegaSesamePileSelectionFragment().putArgs {
                putStringArrayList(MATERIAL_LIST, materialList)
                putParcelableArrayList(MODEL_BUNDLE, model)
                putString(PILE_SELECT, fromVendorList)
            }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPileManagementPileSelectionSesameBinding.inflate(layoutInflater)
        if (getCurrentKey().split("_")[1].contains("NI") && getCurrentKey().split("_")[2].contains(
                "COFF"
            )
        )
            binding.tvAssignLot.isEnabled = false
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

        alreadySelected =
            arguments?.getParcelableArrayList<VegaCocoaDispatchLots>(MODEL_BUNDLE) as ArrayList
        alreadySelected.forEach {
            currentMaterialName = it.materialName!!
            currentMaterialCode = it.materialCode
            materialNewPile = currentMaterialName
            materialCodeNewPile = currentMaterialCode
        }
        vendorcode = arguments?.getString(PILE_SELECT) ?: ""
        materialList = arguments?.getStringArrayList(MATERIAL_LIST) as ArrayList<String>
//        vm.stockPile.observe(viewLifecycleOwner, Observer { updateUI(it) })
//        if (materialList.size > 0) {
//            vm.getStockPiles(materialList)
//        }

        vm.stockList.observe(viewLifecycleOwner, Observer { updateUI(it) })
        if (materialList.size > 0) {
            vm.getStockList(materialList)
        }

        vm.getShiftRemarksItems(getCurrentKey())
        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer {
            updateProcessType(it)
        })

        if (getCurrentKey().split("_")[1].contains("NI")) {
            vm.getSapMaterials()
            vm.sapMaterial.observe(viewLifecycleOwner, Observer {
                sapMaterials = it as MutableList<VegaMaterial>
            })
        } else {
            vm.getThirdPartyMaterials()
            vm.thirdPartyMaterial.observe(viewLifecycleOwner, Observer {
                thirdPartyMaterials = it as MutableList<VegaCoffeeThirdPartyMaterialDetail>
            })
        }


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

        if (getCurrentKey().split("_")[1].contains("NI")) {
            vm.storageLocation.observe(viewLifecycleOwner, Observer {
                storageLocationList = it.toMutableList()
                updateLoc(storageLocationList)
            })
            vm.getStorageLocations()
        } else {
            vm.custonLocation.observe(viewLifecycleOwner, Observer {
                customLocationList = it.toMutableList()
                updateStorageLoc(customLocationList)
            })
            vm.getCustomLocations()
        }


//        binding.spDefaultStorageLoc.setOnClickListener {
//            customlist.clear()
//            showMaterialDialog(customLocationList)
//        }

//        vm.createPile.observe(viewLifecycleOwner, Observer { updatePileCreateUI(it) })

        binding.cbCreateNewLot.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                enableDisableBtn(true)
                removeChecked(pileselectionList)
                isNewLotCreate = true
//                vm.getCreatePile()            } else {
//                enableDisableBtn(false)
//                isNewLotCreate = false
            } else {
                enableDisableBtn(false)
                isNewLotCreate = false
            }
        }

        binding.tvAssignLot.setOnClickListener {
            movetoSummary()
        }


    }

    private fun removeChecked(list: MutableList<VegaCocoaDispatchLots>) {
        list.forEach { it.isChecked = false }
        updateAdapter(list)
    }

    private fun updateStorageLoc(customLocationList: MutableList<VegaCustomStLocation>) {
        customLocationList.let {
            val storageLoc = arrayListOf<String>()
            storageLoc.add(getString(R.string.select_storage_location))
            val storageLoc1 = customLocationList.map { it.procureLocationCode.plus(" - ").plus(it.procureLocationName) }
            storageLoc1.forEach { storageLoc.add(it) }
            val stageAdapter =
                ArrayAdapter(requireContext(), R.layout.item_vega_sesame_pile_grade, storageLoc)
            stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
            binding.spDefaultStorageLoc.adapter = stageAdapter
            binding.spDefaultStorageLoc.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    storageLocation = storageLoc[position].split(" - ")[0]
                    binding.cbCreateNewLot.isEnabled =
                        !storageLoc[position].equals(getString(R.string.select_storage_location))
                }
            }
            var ind = 0
            storageLoc.forEachIndexed { index, s -> ind = index }
            binding.spDefaultStorageLoc.setSelection(if (storageLoc.isNotEmpty()) ind else 0)
            if (storageLoc.isNotEmpty()) {
                binding.spDefaultStorageLoc.isEnabled = true
            }
        }
    }

    private fun updateLoc(storageLocationList: MutableList<VegaStorageLocation>) {
        storageLocationList.let {
            val storageLoc = arrayListOf<String>()
            storageLoc.add(getString(R.string.select_storage_location))
            val storageLoc1 = storageLocationList.map {
                it.storageLocationCode.plus(" - ").plus(it.storageLocationName)
            }
            storageLoc1.forEach { storageLoc.add(it) }
            val stageAdapter =
                ArrayAdapter(requireContext(), R.layout.item_vega_sesame_pile_grade, storageLoc)
            stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
            binding.spDefaultStorageLoc.adapter = stageAdapter
            binding.spDefaultStorageLoc.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                    override fun onItemSelected(
                        p0: AdapterView<*>?,
                        p1: View?,
                        position: Int,
                        p3: Long
                    ) {
                        storageLocation = storageLoc[position].split(" - ")[0]
                        binding.cbCreateNewLot.isEnabled =
                            !storageLoc[position].equals(getString(R.string.select_storage_location))
                    }
                }
            var ind = 0
            storageLoc.forEachIndexed { index, s -> ind = index }
            binding.spDefaultStorageLoc.setSelection(if (storageLoc.isNotEmpty()) ind else 0)
            if (storageLoc.isNotEmpty()) {
                binding.spDefaultStorageLoc.isEnabled = true
            }
        }
    }

    private fun updateProcessType(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        val gson = Gson()
        var processTypeList = mutableListOf<VegaSesamePileProcessType>()
        jsonData.forEach {
            if (it.contains(JSON_PROCESS_TYPE_LIST)) {
                val processTypeList = gson.fromJson(it, VegaSesamePileProcessTypeModel::class.java)
                processType = processTypeList.PROCESS_TYPE_LIST[0].PILE
            }
        }
    }


//    private fun updatePileCreateUI(response: Resource<GenericReqAndResp<VegaSesamePileSequence>>) {
//        response.let {
//            when (it.status) {
//                Resource.Status.SUCCESS -> {
//                    when (it.data?.success) {
//                        true -> {
//                            pileSelection.batchNumber = it.data?.data?.pileSequence
//                            pileSelection.materialCode = materialList[0]
//                            pileSelection.plantId = PreferenceHelper.get(Constants.WERKS,"")
//                        }
//                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
//                    }
//                    hideLoading()
//                }
//                Resource.Status.LOADING -> showLoading()
//                Resource.Status.ERROR -> {
//                    hideLoading()
//                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
//                }
//            }
//        }
//    }

    private fun movetoSummary() {
        if (isNewLotCreate) {

            if (getCurrentKey().split("_")[1].contains("NI")) {
                val position1 = getPlantDetails().plantId
                val current = Calendar.getInstance()
                val month = current.get(Calendar.MONTH)
                var year = current.get(Calendar.YEAR)
                var position2: String = ""
                if (month > 9) {
                    year = (year + 1)
                    position2 = year.toString().takeLast(2)
                } else {
                    position2 = year.toString().takeLast(2)
                }
                var currentBatch = PreferenceHelper.get(Constants.NEW_PILE, "0")

                when (currentBatch.length) {
                    1 -> {
                        PreferenceHelper.save(
                            Constants.NEW_PILE,
                            "000".plus(currentBatch.toInt().inc())
                        )
                        currentBatch = "000".plus(currentBatch.toInt().inc())
                    }
                    2 -> {
                        PreferenceHelper.save(
                            Constants.NEW_PILE,
                            "00".plus(currentBatch.toInt().inc())
                        )
                        currentBatch = "00".plus(currentBatch.toInt().inc())
                    }
                    3 -> {
                        PreferenceHelper.save(
                            Constants.NEW_PILE,
                            "0".plus(currentBatch.toInt().inc())
                        )
                        currentBatch = "0".plus(currentBatch.toInt().inc())
                    }
                    4 -> {
                        PreferenceHelper.save(
                            Constants.NEW_PILE,
                            "000".plus(currentBatch.toInt().inc())
                        )
                        currentBatch = "000".plus(currentBatch.toInt().inc().toString())
                    }
                    5 -> {
                        PreferenceHelper.save(
                            Constants.NEW_PILE,
                            "00".plus(currentBatch.toInt().inc())
                        )
                        currentBatch = "00".plus(currentBatch.toInt().inc().toString())
                    }
                }

                batchidlogic = position1.plus(position2).plus(currentBatch)
                /*pileSequence = PreferenceHelper.get(Constants.PILE_SEQUENCE, "")
                if (pileSequence?.isNotEmpty() == true) {
                    pileSequenceOrder = vm.generatePileSequnceNumber()
                    pile = pileSequenceOrder
                    pileSelection.batchNumber = pileSequenceOrder
                }*/

                pileSelection.batchNumber = batchidlogic
                if (vendorcode.isNotEmpty()) {
                    pileSelection.vendor = vendorcode.split("-")[0]
                    pileSelection.vendorName = vendorcode.split("-")[1]
                }
                pileSelection.weight = weightNewPile
                sapMaterials.forEach { item ->
                    if (item.materialName.toString().contains(currentMaterialName, true)
                        && item.materialCode.contains(currentMaterialCode, true)
                    )
                        materialNewPile = currentMaterialName
                    materialCodeNewPile = currentMaterialCode
                }
                pileSelection.materialName = materialNewPile
                pileSelection.materialCode = materialCodeNewPile
                pileSelection.plantId = getPlantDetails().plantId
                pileSelection.storageLocationCode = storageLocation
            } else {
                val position1 = getPlantDetails().plantId.takeLast(2)
                val position3 = storageLocation.takeLast(1)
                val position4 = Calendar.getInstance().get(Calendar.YEAR).toString().takeLast(1)
                val position5 = processType

                var productTypeCode = ""

                thirdPartyMaterials.forEach { item ->
                    if (item.materialName.toString().contains(currentMaterialName, true))
                        productTypeCode = item.typeCode.toString()
                }

                batchidlogic = position1.plus(position3).plus(position4).plus(position5)
                    .plus(productTypeCode.takeLast(1))
                pileSelection.batchNumber = batchidlogic
            }
        }

        callBack?.replaceFragment(
            PILE_MANAGEMENT_SUMMARY,
            materialArrayList,
            alreadySelected,
            pileSelection
        )
    }


//    private fun showMaterialDialog(materialList: List<VegaCustomStLocation>) {
//        val suppliers = materialList.map { it.procureLocationName ?: "" }
//        MaterialDialog(requireContext()).show {
//            title(R.string.select_storage_location)
//            listItemsSingleChoice(items = suppliers) { _, index, text ->
//                binding.spDefaultStorageLoc.text = text
//                procureLocationCode = materialList[index].procureLocationCode
//                procureLocationName = materialList[index].procureLocationName ?: ""
//                pileSelection.storageLocationCode = procureLocationCode
//                storageLocation = procureLocationCode
//                if (suppliers.size > 0)
//                materialArrayList.add(procureLocationName. plus(procureLocationCode))
//            }
//            positiveButton(
//                text = UIUtils.getSpannedText(
//                    getString(com.olam.warehouse.presentation.R.string.ok),
//                    true
//                )
//            )
//
//        }
//    }

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

//    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaSesamePileSelectionModel>>>) {
//        response.let {
//            when (it.status) {
//                Resource.Status.SUCCESS -> {
//                    when (it.data?.success) {
//                        true -> {
//                            pileselectionList.clear()
//                            /*val filteredDispatchStocksList = mutableListOf<VegaCocoaDispatchLots>()
//                            val dataValue = it.data?.data!!
//                            dataValue.let { item -> filteredDispatchStocksList.addAll(item) }*/
//                            pileselectionList =
//                                if (it.data?.data!!.size > 0) it.data?.data as MutableList<VegaSesamePileSelectionModel> else mutableListOf()
//                            updateAdapter(pileselectionList)
//
//                        }
//                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
//                    }
//                    hideLoading()
//                }
//                Resource.Status.LOADING -> showLoading()
//                Resource.Status.ERROR -> {
//                    hideLoading()
//                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
//                }
//            }
//        }
//    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()
                            /*val filteredDispatchStocksList = mutableListOf<VegaCocoaDispatchLots>()
                            val dataValue = it.data?.data!!
                            dataValue.let { item -> filteredDispatchStocksList.addAll(item) }*/
                            dispatchLotsList =
                                if (it.data?.data!!.size > 0) it.data?.data as MutableList<VegaCocoaDispatchLots> else mutableListOf()
                            dispatchLotsList.forEach {
                                if (it.batchNumber.contains("P")) {
                                    pileselectionList.add(it)
                                } else if (getCurrentKey().split("_")[1].contains("NI")) {
                                    pileselectionList.add(it)
                                }
                            }
                            updateAdapter(pileselectionList)
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
                    hideLoading()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun updateAdapter(stockList: MutableList<VegaCocoaDispatchLots>) {
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

        if ((getCurrentKey().split("_")[1] == "NG") && (getCurrentKey().split("_")[2] == "SESA")) {
            binding.rvInventoryLot.setUp(
                stockList,
                R.layout.item_pile_managment_selection_sesame,
                { it, pos ->
                    tvBagStore.gone()
                    tvStoreLocation.gone()
                    tvPileNo.text = it.batchNumber
                    tvWeight.text = it.weight.plus(" ").plus(it.unitOfMeasure)
                    checkbox.isChecked = it.isChecked
                    llLotItem.setOnClickListener { view ->
                        stockList.forEach { it.isChecked = false }
                        stockList[pos].isChecked = !it.isChecked
                        binding.rvInventoryLot.adapter?.notifyDataSetChanged()
                        binding.cbCreateNewLot.isChecked = false
                        isNewLotCreate = false
                        if (it.isChecked) pileSelection = it
                    }
                    enableDisableBtn(stockList.any { it.isChecked })
                })
        } else if (getCurrentKey().split("_")[1].contains("NI")) {
            binding.rvInventoryLot.setUp(
                stockList, R.layout.item_pile_managment_selection_sesame,
                { it, pos ->
                    tvBagStore.visible()
                    tvStoreLocation.visible()
                    tvPileNo.text = it.batchNumber
                    tvWeight.text = it.weight.plus(" ").plus(it.unitOfMeasure)
                    tvStoreLocation.text = it.storageLocationCode
                    checkbox.isChecked = it.isChecked
                    weightNewPile = it.weight.plus(" ").plus(it.unitOfMeasure)
                    llLotItem.setOnClickListener { view ->
                        stockList.forEach { it.isChecked = false }
                        stockList[pos].isChecked = !it.isChecked
                        binding.rvInventoryLot.adapter?.notifyDataSetChanged()
                        binding.cbCreateNewLot.isChecked = false
                        isNewLotCreate = false
                        if (it.isChecked) pileSelection = it
                    }
                    enableDisableBtn(stockList.any { it.isChecked })
                })

        }
    }


    private fun enableDisableBtn(flag: Boolean) {
        if (flag) {
            binding.tvAssignLot.isEnabled = true
            binding.tvAssignLot.isClickable = true
            binding.tvAssignLot.setBackgroundColor(getColor(if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.tvAssignLot.isEnabled = false
            binding.tvAssignLot.isClickable = false
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
            var filterData1 = mutableListOf<VegaCocoaDispatchLots>()
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
            searchView.setBackgroundColor(getColor(if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
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
                                    if (qtyWb.batchNumber.contains(text)) {
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


