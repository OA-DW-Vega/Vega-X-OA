package com.olam.warehouse.vegax.mtntindo.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.mtntindo.R
import com.olam.warehouse.vegax.mtntindo.databinding.FragmentIndoCoffeeDispatchLotListBinding
import com.olam.warehouse.vegax.mtntindo.databinding.ItemIndoCoffeeDispatchLotListDetailBinding
import com.olam.warehouse.vegax.mtntindo.ui.callback.VegaIndoCoffeeDispatchAddLotsListener
import com.olam.warehouse.vegax.mtntindo.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */
class VegaIndoCoffeeDispatchLotListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_indo_coffee_dispatch_lot_list
    private lateinit var binding: FragmentIndoCoffeeDispatchLotListBinding

    private val vm: VegaIndoCoffeeDispatchViewModel by viewModel()
    private var dispatchLotsList = mutableListOf<VegaEcuadorDispatchLots>()
    private var wareHouseList = mutableListOf<String>()
    private var filteredDispatchLotsList = mutableListOf<VegaEcuadorDispatchLots>()
    private var weight: Double? = 0.0
    private var count = 0
    private var model: VegaEcuadorDispatch? = null
    private var listener: VegaIndoCoffeeDispatchAddLotsListener? = null
    private var alreadySelected = mutableListOf<VegaEcuadorDispatchLots>()
    private val mSearchList = mutableListOf<VegaEcuadorDispatchLots>()
    private var isMultipleAdd = true
    private var materialList = arrayListOf<String>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? VegaIndoCoffeeDispatchAddLotsListener
    }

    companion object {
        fun newInstance(model: VegaEcuadorDispatch, materialList: ArrayList<String>) =
            VegaIndoCoffeeDispatchLotListFragment().putArgs {
                putStringArrayList(MATERIAL_LIST, materialList)
                putParcelable(MODEL_BUNDLE, model)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentIndoCoffeeDispatchLotListBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        model = arguments?.getParcelable(MODEL_BUNDLE)
        alreadySelected.addAll(model?.lotList ?: mutableListOf())
        materialList = arguments?.getStringArrayList(MATERIAL_LIST) as ArrayList<String>
    }

    private fun initUI() {
        vm.stocks.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.stocksLocal.observe(viewLifecycleOwner, Observer { updateLocalUI(it) })
        if (materialList.size > 0) {
            if (AppUtils.isOnline()) vm.fetchStocks(materialList) else vm.fetchStocksOffline()
        }
        binding.btnProceed.setOnClickListener { sendSelectedLots() }
    }

    private fun updateLocalUI(response: List<VegaEcuadorDispatchStocks>) {
        dispatchLotsList.clear()
        var filteredDispatchStocksList = mutableListOf<VegaEcuadorDispatchStocks>()
        materialList.forEach { item ->
            response.let { lots ->
                filteredDispatchStocksList.addAll(lots.filter { it.materialCode.equals(item) })
            }
        }
        dispatchLotsList.addAll(prepareLotsList(filteredDispatchStocksList))
        updateSelectLotValues()
        getWarehouseList()
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()
                            val filteredDispatchStocksList = mutableListOf<VegaEcuadorDispatchStocks>()
                            val dataValue = it.data?.data!!
                            dataValue.let { item -> filteredDispatchStocksList.addAll(item) }
                            dispatchLotsList.addAll(prepareLotsList(filteredDispatchStocksList))
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
        val filter = mutableListOf<VegaEcuadorDispatchLots>()
        filter.addAll(dispatchLotsList)
        if (alreadySelected.isNotEmpty()) {
            filter.forEachIndexed { index, s ->
                alreadySelected.forEach { item ->
                    if (s.batchNumber.equals(item.batchNumber) && s.materialCode.equals(item.materialCode) && s.storageLocationCode.equals(
                            item.storageLocationCode
                        )
                    )
                        dispatchLotsList[index].isAdded = true
                }
            }
        }
        setupAdapter(dispatchLotsList)
    }


    private fun setupAdapter(data: MutableList<VegaEcuadorDispatchLots>) {
        if (data.size > 0) {
            binding.rvLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
        }

        binding.rvLots.setUpAdapter(
            data,
            R.layout.item_indo_coffee_dispatch_lot_list_detail,
            ItemIndoCoffeeDispatchLotListDetailBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvLotId.text = it.batchNumber
                bindItem.tvGradeValue.text = it.materialName
                bindItem.tvStLocationValue.text = it.storageLocationCode
                bindItem.tvWeightValue.text =
                    it.netWeight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitsOfMeasure)
                bindItem.ivSelect.isChecked = it.isAdded ?: false
                bindItem.llLotItem.setOnClickListener { view ->
                    it.isAdded = !it.isAdded!!
                    dispatchLotsList.forEach { item ->
                        if (item.batchNumber.equals(it.batchNumber) && item.materialCode.equals(it.materialCode)) item.isAdded =
                            it.isAdded
                    }
                    binding.rvLots.adapter?.notifyItemChanged(pos)
                    //updateWeight()
                }

            },
            itemClick = {

            })
    }

    private fun removeChecked(item: Int, list: MutableList<VegaEcuadorDispatchLots>) {
        list.forEach { it.isAdded = false }
        list[item].isAdded = true
        setupAdapter(list)
    }

    private fun getWarehouseList() {
        if (dispatchLotsList.isNotEmpty()) {
            wareHouseList.add(getString(R.string.all))
            wareHouseList.addAll(dispatchLotsList.listOfField(VegaEcuadorDispatchLots::storageLocationCode).toSet())
            updateWareHouseSpinner()
        }
    }

    private fun updateWareHouseSpinner() {
        val stageAdapter = ArrayAdapter(requireContext(), R.layout.item_indo_coffee_dispatch_wh, wareHouseList)
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
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
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

