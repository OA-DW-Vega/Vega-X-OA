package com.olam.warehouse.vegax.processingindiacoffee.ui.rmin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.inventoryindiacoffee.data.domain.model.VegaInventoryAndSyncModel
import com.olam.warehouse.vegax.inventoryindiacoffee.data.domain.model.VegaInventoryWarehouseModel
import com.olam.warehouse.vegax.processingindiacoffee.R
import com.olam.warehouse.vegax.processingindiacoffee.databinding.FragmentRminSelectWhBinding
import com.olam.warehouse.vegax.processingindiacoffee.databinding.ItemLotSummaryBinding
import com.olam.warehouse.vegax.processingindiacoffee.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.processingindiacoffee.utils.MULTIPLE_LOT
import com.olam.warehouse.vegax.processingindiacoffee.utils.getColor
import com.olam.warehouse.vegax.processingindiacoffee.utils.listOfField
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaIndiaCoffeeRminInventoryLotListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_rmin_select_wh
    private lateinit var binding: FragmentRminSelectWhBinding

    private val vm: VegaIndiaCoffeeRminViewModel by viewModel()
    private var wareHouseList = mutableListOf<String>()
    private var dispatchLotsList = mutableListOf<VegaCocoaRminLots>()
    private var filteredDispatchLotsList = mutableListOf<VegaCocoaRminLots>()
    private var weight: Double? = 0.0
    private var count = 0
    private var model: VegaCocoaRminProcessing? = null
    private var listener: AddLotsListener? = null
    private var alreadySelected = mutableListOf<VegaCocoaRminLots>()
    private val mSearchList = mutableListOf<VegaCocoaRminLots>()
    private var isMultipleAdd = true
    var lotLis: ArrayList<VegaCocoaRminLots>? = ArrayList()
    private var materialList = mutableListOf<VegaMaterial>()
    private var materialFilter = ArrayList<String>()
    private var rmin: Boolean = false


    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? AddLotsListener
    }

    companion object {
        fun newInstance(model: VegaCocoaRminProcessing, isMultipleAdd: Boolean) =
            VegaIndiaCoffeeRminInventoryLotListFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
                putBoolean(MULTIPLE_LOT, isMultipleAdd)
            }

        const val SEARCH_HINT_TEXT = "Search Lot Item"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRminSelectWhBinding.inflate(layoutInflater)
        binding.btnProceed.isEnabled = false
        initExtra()
        initUI()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private fun initExtra() {
        model = arguments?.getParcelable(MODEL_BUNDLE)
        alreadySelected.addAll(model?.lotList ?: mutableListOf())
        isMultipleAdd = arguments?.getBoolean(MULTIPLE_LOT) ?: true
    }


    private fun initUI() {
        vm.grades.observe(viewLifecycleOwner, Observer {
            materialList.addAll(it)
        })
        vm.fetchGrades()
        vm.inventoryModelList.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getInventoryList(model?.baseMaterialCode ?: "")
        binding.btnProceed.setOnClickListener { sendSelectedLots() }
    }


    private fun updateUI(data: Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            var wareHouseList =
                                it.data?.data?.inventories as ArrayList<VegaInventoryWarehouseModel?>
                            wareHouseList.forEach {
                                var storageLoc = it?.storageLoc
                                var plantId = it?.warehouse?.plant?.plantId
                                var plantName = it?.warehouse?.plant?.plantName

                                when {
                                    storageLoc != null -> {
                                        storageLoc.forEach {
                                            rmin = false
                                            var storageLocationCode =
                                                it.warehouseLocation.procureLocationCode
                                            var weight = it.weight
                                            var warehouseId = it.warehouseLocation.id.toString()
                                            var inventory =
                                                it.inventory.filter { it.materialCode == model?.baseMaterialCode }
                                            inventory.forEach {
                                                var materialCode = it.materialCode
                                                var materialName = model?.baseMaterialName
                                                var vendorCode = it.vendorCode
                                                var lotId = it.lotId
                                                var unitOfMeasure = it.uom
                                                var stockQty = it.stockQty
                                                var QC = it.inventoryQC
                                                if (QC.isNotEmpty()) {
                                                    if (getCurrentKey().split("_")[1].contains("NI")) {
                                                        var filter_rmin =
                                                            QC.filter { it.qcName.equals("STOCKBAGS") }
                                                        filter_rmin.forEach {
                                                            if (it.value.equals("Y")) rmin = true
                                                        }
                                                    }
                                                    if (!rmin) {
                                                        var tallySheet =
                                                            QC.filter { it.qcName.equals("NICERTI") }
                                                        tallySheet.forEach {
                                                            var stock = VegaCocoaRminLots(

                                                                batchNumber = it.value,
                                                                materialCode = materialCode,
                                                                materialName = materialName,
                                                                plantId = plantId,
                                                                plantName = plantName,
                                                                storageLocationCode = storageLocationCode,
                                                                unitOfMeasure = unitOfMeasure,
                                                                weight = stockQty,
                                                                remarks = lotId, //lot id
                                                                shift = vendorCode.toString(),    //  vendor code
                                                                kor = warehouseId    //warehouse id

                                                            )
                                                            dispatchLotsList.add(stock)
                                                        }

                                                    }
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                            if (getCurrentKey().split("_")[1].contains("NI")) {
                                dispatchLotsList = dispatchLotsList.toSet().toMutableList()
                            }
                            updateSelectLotValues()
                            getWarehouseList()
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {

                }

            }


        }
    }


    private fun updateSelectLotValues() {
        val filter = mutableListOf<VegaCocoaRminLots>()
        filter.addAll(dispatchLotsList)
        //lotLis?.let { filter.addAll(it) }
        val map = alreadySelected.map { it.batchNumber to it.batchNumber }
        if (alreadySelected.isNotEmpty()) {
            filter.forEachIndexed { index, s ->
                if (map.contains(s.batchNumber to s.batchNumber)) {
                    dispatchLotsList[index].isAdded = true
                    //lotLis?.get(index)?.isAdded = true
                }
            }
        }
        setupAdapter(dispatchLotsList)
        updateWeight()
        // lotLis?.let { setupAdapter(it) }
        // updateWeight()
    }


    private fun setupAdapter(data1: MutableList<VegaCocoaRminLots>) {
        var data = data1
        if(getCurrentKey().contains("VEGA_NI")){
            if(model?.materialName?.contains("toll",true) == true){
                data= data1.filter { it.shift == model?.vendorCode } as MutableList<VegaCocoaRminLots>
            }
        }

        if (data.size > 0) {
            binding.rvLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
        }

        binding.rvLots.setUpAdapter(
            data,
            R.layout.item_lot_summary,
            ItemLotSummaryBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvLotId.text = it.batchNumber
                bindItem.tvGradeValue.text = it.materialName
                bindItem.tvStLocationValue.text = it.storageLocationCode
                bindItem.tvWeightValue.text =
                    it.weight?.toDouble()?.formatThreeDigits().plus(" ").plus("KG")
                bindItem.ivSelect.isChecked = it.isAdded ?: false


                /* ivSelect.setOnCheckedChangeListener { item, isChecked ->
                     it.isAdded = isChecked
                     if (!isMultipleAdd) {
                         if (isChecked)
                             removeChecked(pos, data)
                     }
                     updateWeight()
                 }*/
                bindItem.llLotItem.setOnClickListener { view ->
                    it.isAdded = !it.isAdded!!
                    bindItem.ivSelect.isChecked = it.isAdded!!
                    binding.btnProceed.isEnabled = true
                    binding.btnProceed.setBackgroundColor(
                        getColor(
                            if (getCurrentOriginEntity().contains("OFI"))
                                com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                            else com.olam.warehouse.presentation.R.color.green
                        )
                    )
                    if (getCurrentKey().split("_")[1].contains("NI")) {
                        dispatchLotsList.forEach { item ->
                            if (item.batchNumber.equals(it.batchNumber) &&
                                item.weight.equals(it.weight) &&
                                item.storageLocationCode.equals(it.storageLocationCode)
                            ) item.isAdded = it.isAdded
                        }
                    } else {
                        dispatchLotsList.forEach { item ->
                            if (item.batchNumber.equals(it.batchNumber)) item.isAdded = it.isAdded
                        }
                    }
                    if (!isMultipleAdd) {
                        if (it.isAdded!!)
                            removeChecked(pos, data)
                    } else {
                        binding.rvLots.adapter?.notifyItemChanged(pos)
                    }
                    updateWeight()
                }

            }, itemClick = {

            })
    }

    private fun removeChecked(item: Int, list: MutableList<VegaCocoaRminLots>) {
        list.forEach { it.isAdded = false }
        list[item].isAdded = true
        setupAdapter(list)
    }

    private fun getWarehouseList() {
        wareHouseList.clear()
        if (dispatchLotsList.isNotEmpty()) {
            wareHouseList.add(getString(R.string.all))
            wareHouseList.addAll(
                dispatchLotsList.listOfField(VegaCocoaRminLots::storageLocationCode)
            )
            if (getCurrentKey().split("_")[1].contains("NI")) {
                wareHouseList = wareHouseList.toSet().toMutableList()
            }
            updateWareHouseSpinner()
        }
    }

    private fun updateWareHouseSpinner() {
        val stageAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_vega_india_coffee_processing_rmin_grade,
            wareHouseList
        )
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spWareHouse.adapter = stageAdapter
        val defaultposition = 0
        binding.spWareHouse.setSelection(defaultposition)
        binding.spWareHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                filteredDispatchLotsList.clear()
                if (position > 0) {
                    val storageLocation = wareHouseList[position]
                    val lotsList =
                        dispatchLotsList.filter { it.storageLocationCode == storageLocation }
                    filteredDispatchLotsList.addAll(lotsList)
                    setupAdapter(filteredDispatchLotsList)
                } else {
                    setupAdapter(dispatchLotsList)
                    filteredDispatchLotsList.clear()
                }
            }
        }
    }

    private fun updateWeight() {
        val filter = dispatchLotsList.filter { it.isAdded == true }
        count = filter.size
        for (item in filter) {
            weight = item.weight?.toDouble() ?: 0.0
        }
        if (count == 0) weight = 0.0
        updateLotCountAndWeight(count.toString(), weight.toString())
    }

    private fun updateLotCountAndWeight(count: String, weight: String) {
        binding.tvLot.text = count.plus(getString(R.string.lot_selected))
        binding.tvLotWeight.text = weight.plus(" Kg")

        targetFragment?.onActivityResult(
            targetRequestCode,
            Activity.RESULT_OK,
            Intent().putExtra("sas", "sadasdas")
        )
    }

    private fun sendSelectedLots() {
        var data = dispatchLotsList.filter { it.isAdded == true } as ArrayList
        // val data = lotLis?.filter { it.isAdded == true } as ArrayList
        listener?.addedLots(data)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
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
