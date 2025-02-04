package com.olam.warehouse.vegax.processingindo.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.entity.VegaProcessingList
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingindo.R
import com.olam.warehouse.vegax.processingindo.databinding.FragmentVegaProcessingIndoFgrnAssignLotBinding
import com.olam.warehouse.vegax.processingindo.utils.*
import kotlinx.android.synthetic.main.item_vega_processing_indo_fgrn_lot.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 6/2/2020.
 */
class VegaProcessingIndoFgrnAssignLotFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_processing_indo_fgrn_assign_lot
    private lateinit var binding: FragmentVegaProcessingIndoFgrnAssignLotBinding
    private val vm: VegaProcessingIndoFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var fgrnItem = VegaCocoaFgrnItems()
    private var currentMaterial: String = ""
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var stockList = mutableListOf<VegaCocoaRminLots>()
    private var filterData = mutableListOf<VegaCocoaRminLots>()
    private var fullFilter = java.util.ArrayList<String>()
    private var startRange: String = "0"
    private var endRange: String = "30"
    private var aboveThirty: Boolean = false
    private var storageLocation: String = ""
    private var DefaultStoLoc: String = ""
    private var isNewLotCreate: Boolean = false
    private var isNewLotId: String = ""

    interface CallBack {
        fun replaceFGrnFragment(fragFilter: String, bundle: Bundle, fullFilter: java.util.ArrayList<String>)
        fun updateLotDetails(bundle: Bundle)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCocoaFgrnItems, id: String) = VegaProcessingIndoFgrnAssignLotFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
            putString(MATERIAL_CODE, id)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaProcessingIndoFgrnAssignLotBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcocoa/ui/fgrn/VegaCocoaFgrnAssignLotFragment").title("Processing Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCocoaFgrnItems()
        currentMaterial = arguments?.getString(MATERIAL_CODE) ?: ""
        fgrnItem.rminList?.let { updateRminRetainLot(it) }
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

        binding.tvFilter.setOnClickListener { FilterActivity() }

        binding.cbCreateNewLot.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                enableDisableBtn(true)
                isNewLotCreate = true
                if(binding.etLotId.isVisible)
                    binding.etNewLotId.gone()
                else
                binding.etNewLotId.visible()
            } else {
                enableDisableBtn(false)
                isNewLotCreate = false
                binding.etNewLotId.gone()
                isNewLotId = ""
                binding.etNewLotId.setText("")
            }
        }
        /* binding.etLotId.onChange { text ->
             when {
                 text.isEmpty() -> enableDisableBtn(false)
                 text.isNotEmpty() && text.length == 2 -> enableDisableBtn(true)
                 else -> enableDisableBtn(false)
             }
         }
         binding.etLotId.filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(2))*/

        binding.etNewLotId.onChange { text ->
            when {
                text.isEmpty() -> isNewLotId = ""
                text.isNotEmpty() && text.length == 4 -> isNewLotId = text
            }
        }

        binding.tvAssignLot.setOnClickListener {
            if (binding.etNewLotId.isVisible) {
                when (binding.etNewLotId.text?.length) {
                    4 -> moveBackToFgrn()
                    else -> activity?.toast("Please enter 4 digits")
                }
            }
            else
            {
                if(isNewLotCreate) {
                    when (binding.etLotId.text?.length) {
                        2 -> moveBackToFgrn()
                        else -> activity?.toast("Please enter 2 digits")
                    }
                }
                else
                {
                    moveBackToFgrn()
                }

            }

            /*else
                when (binding.etLotId.text?.length) {
                    10 -> moveBackToFgrn()
                    else -> activity?.toast("Please enter 10 digits")
                }*/

        }

        vm.stocks.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.fetchStocks(currentMaterial.removeRange(0, 6)/*"100000037431"*/)
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.getConfigItems(UserRoles.PROCESSING.role)
        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            customLocationList = it.toMutableList()
            updateStorageLoc(customLocationList)
//            vm.getConfigItems(UserRoles.PROCESSING.role)
        })
        //vm.getCustomLocations()
    }


    private fun moveBackToFgrn() {
        val bundle = Bundle()
        bundle.putString(STORAGE_LOC, storageLocation)
        bundle.putString(LOT_ID, binding.etLotId.text.toString())
        bundle.putBoolean(CREATE_NEW_LOT, isNewLotCreate)
        bundle.putString(CREATE_NEW_ID, isNewLotId)
        bundle.putParcelableArrayList(STOCK_LIST, stockList.filter { it.isChecked!! } as ArrayList<VegaCocoaRminLots>)
        activity?.onBackPressed()
        callBack?.updateLotDetails(bundle)
    }

    private fun FilterActivity() {
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
                    return@forEach
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
                binding.cbCreateNewLot.gone()
            } else if (it.applicable?.contains("N")!!) {
                binding.etLotId.gone()
                binding.cbCreateNewLot.visible()
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
                ArrayAdapter(requireContext(), R.layout.item_vega_processing_indo_rmin_grade, storageLoc)
            stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
            binding.spDefaultStorageLoc.adapter = stageAdapter
            binding.spDefaultStorageLoc.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    storageLocation = storageLoc[position].split(" - ")[0]
                    /*binding.etLotId.isEnabled =
                        !storageLoc[position].equals(getString(R.string.select_storage_location))*/
                    if (!storageLoc[position].equals(getString(R.string.select_storage_location))) {
                        binding.etLotId.setText(storageLoc[position].split(" - ")[1].substring(0, 3))
                        binding.etLotId.text?.length?.let { it1 -> binding.etLotId.setSelection(it1) }
                        binding.cbCreateNewLot.isEnabled = true
                        binding.cbCreateNewLot.visibility = View.VISIBLE
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
            var ind: Int = 0
            storageLoc.forEachIndexed { index, s ->
                if (s.split(" - ")[0].equals(DefaultStoLoc)) ind = index
            }
            binding.spDefaultStorageLoc.setSelection(if (DefaultStoLoc.isNotEmpty()) ind else 0)
            if (DefaultStoLoc.isNotEmpty()) binding.spDefaultStorageLoc.isEnabled = false
        }
    }

    private fun updateRminRetainLot(retainLots: List<VegaProcessingList>) {
        val lots = retainLots.map { it.batchNumber.toString().take(5) }
        val lotsItem = arrayListOf<String>()
        //if(lots.size>1){
        lotsItem.add(getString(R.string.please_select))
        lotsItem.addAll(lots)
        /*}else
            lotsItem.addAll(lots)     */
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_processing_indo_rmin_grade, lotsItem)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spDefaultLot.adapter = stageAdapter
        binding.spDefaultLot.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (position > 0) {
                    enableDisableBtn(true)
                    binding.etLotId.setText(lotsItem[position])
                } else {
                    enableDisableBtn(false)
                    binding.etLotId.setText("")
                }
            }
        }
        // binding.spDefaultLot.setSelection(if (currentGrade.batchNumber.isNotEmpty()) lots.indexOf(currentGrade.batchNumber) else 0)

    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            stockList.clear()
                            val dataValue = it.data?.data!!
                            stockList.addAll(dataValue)
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

    private fun updateAdapter(stockList: MutableList<VegaCocoaRminLots>) {
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
        binding.rvInventoryLot.setUp(stockList, R.layout.item_vega_processing_indo_fgrn_lot, { it, pos ->
            ivDelete.gone()
            tvLotNoValue.text = it.batchNumber
            tvWeightValue.text = it.weight.plus(" ").plus(it.unitOfMeasure)
            tvStorageValue.text = it.storageLocationCode
            cbLotId.isChecked = it.isChecked!!
            clItem.setOnClickListener { view ->
                stockList.forEach { it.isChecked = false }
                stockList[pos].isChecked = !it.isChecked!!
                binding.rvInventoryLot.adapter?.notifyDataSetChanged()
                enableDisableBtn(stockList.any { it.isChecked!! })
            }
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
            var filterData1 = mutableListOf<VegaCocoaRminLots>()
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
}
