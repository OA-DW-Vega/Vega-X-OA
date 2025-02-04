package com.olam.warehouse.vegax.processingghana.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
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
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingghana.R
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessType
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaShiftProcessTypeModel
import com.olam.warehouse.vegax.processingghana.databinding.FragmentVegaGhanaFgrnAssignLotBinding
import com.olam.warehouse.vegax.processingghana.utils.*
import kotlinx.android.synthetic.main.item_vega_ghana_fgrn_lot.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList

class VegaGhanaFgrnAssignLotFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_ghana_fgrn_assign_lot
    private lateinit var binding: FragmentVegaGhanaFgrnAssignLotBinding
    private val vm: VegaGhanaFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var fgrnItem = VegaCoffeeFgrnItems()
    private var currentMaterial: String = ""
    private var currentMaterialName: String = ""
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var stockList = mutableListOf<VegaCoffeeRminLots>()
    private var tempStockList = mutableListOf<VegaCoffeeRminLots>()
    private var filterData = mutableListOf<VegaCoffeeRminLots>()
    private var fullFilter = java.util.ArrayList<String>()

    private var thirdPartyMaterials = mutableListOf<VegaCoffeeThirdPartyMaterialDetail>()
    private var startRange: String = "0"
    private var endRange: String = "30"
    private var aboveThirty: Boolean = false
    private var storageLocation: String = ""
    private var DefaultStoLoc: String = ""
    private var isNewLotCreate: Boolean = false
    private var isThirdPartyMaterialDetail = false
    private val mSearchList = mutableListOf<VegaCoffeeRminLots>()
    private var processType:String? =""
    private var jsonData = mutableListOf<String>()
    private var dispatchLotsList = mutableListOf<VegaCoffeeRminLots>()

    interface CallBack {
        fun replaceFGrnFragment(fragFilter: String, bundle: Bundle, fullFilter: java.util.ArrayList<String>)
        fun updateLotDetails(bundle: Bundle)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(
            model: VegaCoffeeFgrnItems,
            id: String,
            isThirdPartyMaterialDetail: Boolean,
            material: String,
            vegaStage: VegaProcessingStage
        ) =
            VegaGhanaFgrnAssignLotFragment().putArgs {
                putParcelable(FRAG_ITEM, model)
                putString(MATERIAL_CODE, id)
                putString(MATERIAL_NAME, material)
                putBoolean("thirdParty", isThirdPartyMaterialDetail)
                putParcelable(VEGA_STAGE, vegaStage)
            }

        const val SEARCH_HINT_TEXT = "Search Lot Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaGhanaFgrnAssignLotBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingsesame/ui/fgrn/VegaSesameFgrnAssignLotFragment").title("Processing Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCoffeeFgrnItems()
        currentMaterial = arguments?.getString(MATERIAL_CODE) ?: ""
        currentMaterialName = arguments?.getString(MATERIAL_NAME) ?: ""
        isThirdPartyMaterialDetail = arguments?.getBoolean("thirdParty") ?: false
        vm.getCustomLocations()
        binding.tvCreateLot.setOnClickListener {
            when (binding.clCreateLot.isVisible) {
                true -> {
                    binding.clCreateLot.gone()
                }
                false -> {
                    setVisibility()
                    binding.clCreateLot.visible()
                }
            }
        }
        binding.tvInventory.setOnClickListener {
            when (binding.clInventory.isVisible) {
                true -> {
                    binding.clInventory.gone()
                }
                false -> {
                    setVisibility()
                    binding.clInventory.visible()
                }
            }
        }

        binding.tvFilter.setOnClickListener { filterActivity() }

        binding.cbCreateNewLot.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                isNewLotCreate = true
                removeChecked(stockList)
                enableDisableBtn(true)

            } else {
                enableDisableBtn(false)
                isNewLotCreate = false
            }
        }

        /*binding.etLotId.onChange { text ->
            when {
                text.isEmpty() -> enableDisableBtn(false)
                text.isNotEmpty() -> {
                    when (binding.tvBatchDefault.text.length) {
                        6 -> if (text.length == 4) enableDisableBtn(true) else enableDisableBtn(false)
                        7 -> if (text.length == 3) enableDisableBtn(true) else enableDisableBtn(false)
                    }
                }
                else -> enableDisableBtn(false)
            }
        }*/

        binding.tvAssignLot.setOnClickListener { moveBackToFgrn() }
        /* vm.product.observe(viewLifecycleOwner, Observer {
             materialList = it as MutableList<VegaMaterial>
             val currentMatName = materialList.filter { it.materialCode.equals(currentMaterial.removeRange(0, 6)) }
             if (currentMatName.size > 0)
                 binding.tvBatchDefault.text = vm.createBatchFormat(currentMatName[0])
         })
         vm.getProducts()*/
        vm.getShiftRemarksItems(getCurrentKey())
        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer {
            updateProcessType(it)
        })

        vm.getThirdPartyMaterials()
        vm.thirdPartyMaterial.observe(viewLifecycleOwner, Observer {
            thirdPartyMaterials = it as MutableList<VegaCoffeeThirdPartyMaterialDetail>
            val currentMatName = thirdPartyMaterials.filter { it.materialName.toString().contains(currentMaterialName) }
            if (currentMatName.size > 0)
                binding.tvBatchDefault.text = vm.createBatchFormat(currentMatName[0])
            when (binding.tvBatchDefault.text.length) {
                6 -> binding.etLotId.filters = arrayOf(InputFilter.LengthFilter(4))
                7 -> binding.etLotId.filters = arrayOf(InputFilter.LengthFilter(3))
                else -> binding.etLotId.filters = arrayOf(InputFilter.LengthFilter(10))
            }
        })

        vm.stocks.observe(viewLifecycleOwner, Observer { updateUI(it) })
        if (isOnline()) {
            vm.fetchStocks(currentMaterial.removeRange(0, 6)/*"100000037431"*/)
        } else {
            vm.getStockDetails(currentMaterial)
        }
        vm.stockDetailsItemLocal.observe(viewLifecycleOwner, Observer {
            it.forEach {
                var dataValue = prepareStockList(it)
                tempStockList.add(dataValue)
            }
            customLocationList.forEach { item ->
                var tempList = tempStockList.filter { item.procureLocationCode == it.storageLocationCode!! }
                stockList.addAll(tempList)
            }


            updateAdapter(stockList)
        })
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.getConfigItems(UserRoles.PROCESSING.role)
        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            customLocationList = it.toMutableList()
            customLocationList.forEach { DefaultStoLoc = it.procureLocationCode }
            updateStorageLoc(customLocationList)
        })
        binding.etLotId.filters = arrayOf(InputFilter.AllCaps())
    }

    private fun prepareStockList(stocks: VegaEcuadorDispatchStocks): VegaCoffeeRminLots {
        var list = VegaCoffeeRminLots()
        list.batchNumber = stocks.batchNumber
        list.materialCode = stocks.materialCode
        list.materialName = stocks.materialName
        list.plantId = stocks.plantId
        list.storageLocationCode = stocks.storageLocationCode
        list.unitOfMeasure = stocks.unitOfMeasure
        list.vendor = stocks.vendor
        list.weight = stocks.weight
        return list
    }

    private fun removeChecked(list: MutableList<VegaCoffeeRminLots>) {
        list.forEach { it.isChecked = false }
        updateAdapter(list)
    }

    private fun updateProcessType(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        val gson = Gson()
        var processTypeList = mutableListOf<VegaGhanaProcessType>()
        jsonData.forEach {
            if (it.contains(JSON_PROCESS_TYPE_LIST)) {
                val processTypeList = gson.fromJson(it, VegaGhanaShiftProcessTypeModel::class.java)
                processType = processTypeList.PROCESS_TYPE_LIST[0].FGRN
            }
        }
    }

    private fun moveBackToFgrn() {
        val bundle = Bundle()


        //  Batch id logic for FGRN
        var position1 = getPlantDetails().plantId.takeLast(2)
        var position3 = storageLocation.takeLast(1)
        var position4 = Calendar.getInstance().get(Calendar.YEAR).toString().takeLast(1)
        var position5 = processType
        var positon6 = "Z"

        var productTypeCode = ""

        thirdPartyMaterials.forEach { item ->
            if (item.materialName.toString().contains(currentMaterialName, true))
                productTypeCode = item.typeCode.toString()
        }

//        var batchidlogic = position1.plus(position3).plus(position4).plus(position5).plus(productTypeCode.takeLast(1))
        var batchidlogic = ""
        bundle.putString(STORAGE_LOC, storageLocation)
        bundle.putString(LOT_ID, batchidlogic)
        bundle.putBoolean(CREATE_NEW_LOT, isNewLotCreate)
        bundle.putParcelableArrayList(STOCK_LIST, stockList.filter { it.isChecked!! } as ArrayList<VegaCoffeeRminLots>)
        activity?.onBackPressed()
        callBack?.updateLotDetails(bundle)
    }

    private fun filterActivity() {
        val bundle = Bundle()
        val stoFilter = ArrayList<String>()
        val stoLoc = stockList.map { it.storageLocationCode!! }
        stoFilter.addAll(stoLoc)
        bundle.putStringArrayList(FILTER_WH_LOC, stoFilter)
        bundle.putString(FILTER_START_RANGE, startRange)
        bundle.putString(FILTER_END_RANGE, endRange)
        bundle.putBoolean(FILTER_ABOVE_RANGE, aboveThirty)
        callBack?.replaceFGrnFragment(FRAG_FILTER, bundle, fullFilter)
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>) {
        val defaultStorageLoc = configItems.filter { it.process.equals(ConfigItems.DEFAULT_STORAGE_LOC.item) }
        var isExist = false
        val isMaterial = defaultStorageLoc.map { it.materialCode }.contains(currentMaterial.removeRange(0, 6))
        defaultStorageLoc.forEach {
            if (currentMaterial.contains(it.materialCode) && !it.materialCode.isNullOrEmpty() && !isExist && isMaterial) {
                if (it.applicable?.contains("Y")!!) {
                    DefaultStoLoc = it.value.toString()
                    isExist = true
                }
            } else if (it.materialCode.isNullOrEmpty() && !isExist && !isMaterial) {
                if (it.applicable?.contains("Y")!!) {
                    DefaultStoLoc = it.value.toString()
                    isExist = true
                }
            }
        }
        val createNewLot = configItems.filter { it.process.equals(ConfigItems.CREATE_LOT.item) }
        createNewLot.forEach {
            if (it.applicable?.contains("Y")!!) {
                binding.etLotId.visible()
                binding.tvBatchDefault.visible()
                binding.cbCreateNewLot.gone()
            } else if (it.applicable?.contains("N")!!) {
                binding.etLotId.gone()
                binding.cbCreateNewLot.visible()
                binding.tvBatchDefault.gone()
            }
        }

        vm.getCustomLocations()
    }

    private fun updateStorageLoc(customLocationList: MutableList<VegaCustomStLocation>) {
        customLocationList.let {
            val storageLoc = arrayListOf<String>()
            storageLoc.add(getString(R.string.select_storage_location))
            val storageLoc1 = customLocationList.map { it.procureLocationCode.plus(" - ").plus(it.procureLocationName) }
            storageLoc1.forEach { storageLoc.add(it) }
            val stageAdapter =
                ArrayAdapter(requireContext(), R.layout.item_vega_ghana_processing_rmin_grade, storageLoc)
            stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
            binding.spDefaultStorageLoc.adapter = stageAdapter
            binding.spDefaultStorageLoc.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    storageLocation = storageLoc[position].split(" - ")[0]
                    binding.etLotId.isEnabled =
                        !storageLoc[position].equals(getString(R.string.select_storage_location))
                    if (!storageLoc[position].equals(getString(R.string.select_storage_location))) {
                        binding.cbCreateNewLot.isEnabled = true
                        binding.etLotId.setOnClickListener {
                            binding.etLotId.text?.length?.let { it1 ->
                                binding.etLotId.setSelection(it1)
                            }
                        }
                    } else {
                        binding.cbCreateNewLot.isEnabled = false
                    }
                }
            }
            var ind = 0
            storageLoc.forEachIndexed { index, s ->  ind = index }
            binding.spDefaultStorageLoc.setSelection(if (storageLoc.isNotEmpty()) ind else 0)
            if (storageLoc.isNotEmpty()) {
                binding.spDefaultStorageLoc.isEnabled = true
            }
        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            stockList.clear()
                            val dataValue = it.data?.data!!
                            var tempList = dataValue.filter { it.batchNumber.contains("FG") }
                            if (isThirdPartyMaterialDetail) {
                                stockList.addAll(tempList)
                            } else {
                                stockList.addAll(tempList)
                            }
                            stockList.forEachIndexed { index, s ->
                                stockList[index].weight = stockList[index].weight.toString()
                            }
                            updateAdapter(stockList)
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
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

    private fun updateAdapter(stockList: MutableList<VegaCoffeeRminLots>) {
        when {
            stockList.size > 0 -> {
                binding.rvInventoryLot.visible()
                binding.tvNoData.gone()
            }
            else -> {
                binding.rvInventoryLot.gone()
                binding.tvNoData.visible()
            }
        }
        binding.rvInventoryLot.setUp(stockList, R.layout.item_vega_ghana_fgrn_lot, { it, pos ->
            ivDelete.gone()
            tvLotNoValue.text = it.batchNumber
            tvWeightValue.text = it.weight.plus(" ").plus("MT")
            tvStorageValue.text = it.storageLocationCode
            cbLotId.isChecked = it.isChecked!!
            clItem.setOnClickListener { view ->
                stockList.forEach { it.isChecked = false }
                stockList[pos].isChecked = !it.isChecked!!
                binding.rvInventoryLot.adapter?.notifyDataSetChanged()
                binding.cbCreateNewLot.isChecked = false
                isNewLotCreate = false
            }
            enableDisableBtn(stockList.any { it.isChecked!! })
        })
    }

    private fun enableDisableBtn(flag: Boolean) {
        if (flag) {
            binding.tvAssignLot.isEnabled = true
            binding.tvAssignLot.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.tvAssignLot.isEnabled = false
            binding.tvAssignLot.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
    }

    private fun setVisibility() {
        binding.clCreateLot.gone()
        binding.clInventory.gone()
    }

    fun applyFilter(bundle: Bundle) {
        val stoLocation = bundle.getStringArrayList(FILTER_WH_LOC) ?: ArrayList()
        startRange = bundle.getString(FILTER_START_RANGE) ?: ""
        endRange = bundle.getString(FILTER_END_RANGE) ?: ""
        aboveThirty = bundle.getBoolean(FILTER_ABOVE_RANGE, false)
        fullFilter = stoLocation
        if (!aboveThirty && startRange.equals("0") && endRange.equals("30") && stoLocation.size == 0) {
            updateAdapter(stockList)
        } else {
            filterData.clear()
            var filterData1 = mutableListOf<VegaCoffeeRminLots>()
            stockList.forEach { stock ->
                stoLocation.forEach { loc ->
                    if (loc.contains(stock.storageLocationCode.toString())) {
                        filterData1.add(stock)
                    }
                }
            }
            if (stoLocation.size == 0) filterData1 = stockList

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
            searchView.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
            searchView.queryHint = SEARCH_HINT_TEXT
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            updateAdapter(stockList)
                        } else {
                            mSearchList.clear()
                            stockList.forEach { qtyWb ->
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
