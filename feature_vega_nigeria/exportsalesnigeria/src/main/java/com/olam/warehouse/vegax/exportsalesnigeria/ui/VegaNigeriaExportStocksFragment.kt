package com.olam.warehouse.vegax.exportsalesnigeria.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.exportsalesnigeria.R
import com.olam.warehouse.vegax.exportsalesnigeria.databinding.FragmentVegaExportSalesNigeriaLotsBinding
import com.olam.warehouse.vegax.exportsalesnigeria.databinding.ItemVegaNigeriaExportLotsBinding
import com.olam.warehouse.vegax.exportsalesnigeria.utils.MATERIAL_LIST
import com.olam.warehouse.vegax.exportsalesnigeria.utils.SELECTED_MATERIAL_LIST
import com.olam.warehouse.vegax.exportsalesnigeria.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 9/7/2020.
 */
class VegaNigeriaExportStocksFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_export_sales_nigeria_lots
    private val vm: VegaNigeriaExportSalesViewModel by viewModel()
    private var callback: CallBack? = null
    private lateinit var binding: FragmentVegaExportSalesNigeriaLotsBinding
    private var materialList = arrayListOf<String>()
    private var dispatchLotsList = mutableListOf<VegaCoffeeExportSalesLots>()
    private var filteredDispatchLotsList = mutableListOf<VegaCoffeeExportSalesLots>()
    private var wareHouseList = mutableListOf<String>()
    private var weight: Double? = 0.0
    private var count = 0
    private var alreadySelected = arrayListOf<String>()
    private val mSearchList = mutableListOf<VegaCoffeeExportSalesLots>()

    companion object {
        fun newInstance(materialList: ArrayList<String>, selectedList: ArrayList<String>) =
            VegaNigeriaExportStocksFragment().putArgs {
                putStringArrayList(MATERIAL_LIST, materialList)
                putStringArrayList(SELECTED_MATERIAL_LIST, selectedList)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as? CallBack
    }

    interface CallBack {
        fun addedLots(data: ArrayList<VegaCoffeeExportSalesLots>)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("exportsalesNigeria/ui/VegaNigeriaExportStocksFragment").title("Vega_Nigeria/Export Sales")
            .with(tracker)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaExportSalesNigeriaLotsBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        alreadySelected = arguments?.getStringArrayList(SELECTED_MATERIAL_LIST) as ArrayList<String>
        materialList = arguments?.getStringArrayList(MATERIAL_LIST) as ArrayList<String>
    }

    private fun initUI() {
        vm.stockList.observe(viewLifecycleOwner, Observer { updateUI(it) })
        if (materialList.size > 0) {
            vm.getStockList(materialList)
        }
        binding.btnProceed.setOnClickListener { sendSelectedLots() }
    }

    private fun sendSelectedLots() {
        val selctedData = arrayListOf<VegaCoffeeExportSalesLots>()
        val data = dispatchLotsList.filter { it.isAdded == true } as ArrayList
        //  data.forEach { if (!alreadySelected.contains(it.batchNumber)) selctedData.add(it) }
        callback?.addedLots(data)
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()
                            dispatchLotsList =
                                if (it.data?.data!!.size > 0) it.data?.data as MutableList<VegaCoffeeExportSalesLots> else mutableListOf()
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
        val filter = mutableListOf<VegaCoffeeExportSalesLots>()
        filter.addAll(dispatchLotsList)
        if (alreadySelected.isNotEmpty()) {
            dispatchLotsList.forEachIndexed { index, s ->
                alreadySelected.forEach { item ->
                    if (s.batchNumber.contains(item))
                        dispatchLotsList[index].isAdded = true
                }
            }
        }
    }

    private fun getWarehouseList() {
        if (dispatchLotsList.isNotEmpty()) {
            wareHouseList.add(getString(R.string.all))
            wareHouseList.addAll(dispatchLotsList.map { it.storageLocationCode.toString() }.distinct().toList())
            updateWareHouseSpinner()
        } else {
            setupAdapter(dispatchLotsList)
        }
    }

    private fun updateWareHouseSpinner() {
        val stageAdapter = ArrayAdapter(requireContext(), R.layout.item_vega_nigeria_export_sales, wareHouseList)
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

    private fun setupAdapter(data: MutableList<VegaCoffeeExportSalesLots>) {
        if (data.size > 0) {
            binding.rvLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
        }
        binding.rvLots.setUpAdapter(
            data,
            R.layout.item_vega_nigeria_export_lots,
            ItemVegaNigeriaExportLotsBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvLotId.text = it.batchNumber
                bindItem.tvGradeValue.text = it.materialName
                bindItem.tvStLocationValue.text = it.storageLocationCode
                var weightValue = ""
                when (it.unitOfMeasure) {
                    "KG" -> {
                        weightValue = it.weight.toString()
                    }
                    "MT" -> {
                        weightValue = (it.weight.toString())
                    }
                }
//            tvWeightValue.text = it.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitOfMeasure)
                bindItem.tvWeightValue.text =
                    weightValue.toDouble().formatThreeDigits().plus(" ").plus("MT")
                bindItem.ivSelect.isChecked = it.isAdded ?: false
                bindItem.llLotItem.setOnClickListener { view ->
                    it.isAdded = !it.isAdded!!
                    bindItem.ivSelect.isChecked = it.isAdded ?: false
                    dispatchLotsList.forEach { item ->
                        if (item.batchNumber.equals(it.batchNumber) && item.materialCode.equals(it.materialCode)) item.isAdded =
                            it.isAdded
                    }
                }

                bindItem.ivSelect.setOnClickListener { view ->
                    it.isAdded = !it.isAdded!!
                    bindItem.ivSelect.isChecked = it.isAdded ?: false
                    dispatchLotsList.forEach { item ->
                        if (item.batchNumber.equals(it.batchNumber) && item.materialCode.equals(it.materialCode)) item.isAdded =
                            it.isAdded
                    }
                }
            })
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
