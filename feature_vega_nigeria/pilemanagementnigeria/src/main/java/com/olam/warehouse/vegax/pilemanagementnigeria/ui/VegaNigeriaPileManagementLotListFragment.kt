package com.olam.warehouse.vegax.pilemanagementnigeria.ui

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
import com.olam.warehouse.vegax.pilemanagementnigeria.R
import com.olam.warehouse.vegax.pilemanagementnigeria.databinding.FragmentPileManagementLotListNigeriaBinding
import com.olam.warehouse.vegax.pilemanagementnigeria.databinding.ItemPileManagementLotListCardNigeriaBinding
import com.olam.warehouse.vegax.pilemanagementnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaNigeriaPileManagementLotListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_pile_management_lot_list_nigeria
    private lateinit var binding: FragmentPileManagementLotListNigeriaBinding
    private val vm: VegaNigeriaPileManagementViewModel by viewModel()
    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var lotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var filteredDispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var wareHouseList = mutableListOf<String>()
    private val mSearchList = mutableListOf<VegaCocoaDispatchLots>()
    private var materialList = arrayListOf<String>()
    private var listener: VegaNigeriaPileManagementAddLotListener? = null
    private var vendorcode: String = ""
    private var alreadySelected = mutableListOf<VegaCocoaDispatchLots>()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? VegaNigeriaPileManagementAddLotListener
    }

    companion object {
        fun newInstance(
            model: ArrayList<VegaCocoaDispatchLots>,
            materialList: ArrayList<String>,
            fromVendorList: String
        ) =
            VegaNigeriaPileManagementLotListFragment().putArgs {
                putStringArrayList(MATERIAL_LIST, materialList)
                putParcelableArrayList(MODEL_BUNDLE, model)
                putString(PILE_SELECT, fromVendorList)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentPileManagementLotListNigeriaBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        materialList = arguments?.getStringArrayList(MATERIAL_LIST) as ArrayList<String>
        vendorcode = arguments?.getString(PILE_SELECT) ?: ""
    }

    private fun initUI() {
        alreadySelected =
            arguments?.getParcelableArrayList<VegaCocoaDispatchLots>(MODEL_BUNDLE) as ArrayList
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
                            lotsList.clear()
                            dispatchLotsList.clear()
                            /*val filteredDispatchStocksList = mutableListOf<VegaCocoaDispatchLots>()
                            val dataValue = it.data?.data!!
                            dataValue.let { item -> filteredDispatchStocksList.addAll(item) }*/
                            lotsList =
                                if (it.data?.data!!.size > 0) it.data?.data as MutableList<VegaCocoaDispatchLots> else mutableListOf()
                            lotsList.forEach {
                                if (vendorcode != "") {
                                    if (it.vendor == vendorcode)
                                        dispatchLotsList.add(it)
                                } else
                                    dispatchLotsList = lotsList
                            }
                            updateSelectLotValues()
                            getWarehouseList()
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
//                    hideLoading()
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
        val map = alreadySelected.map { it.batchNumber to it.batchNumber }
        if (alreadySelected.isNotEmpty()) {
            filter.forEachIndexed { index, s ->
                if (map.contains(s.batchNumber to s.batchNumber)) {
                    dispatchLotsList[index].isAdded = true
                }
            }
        }
        hideLoading()
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
            R.layout.item_pile_management_lot_list_card_nigeria,
            ItemPileManagementLotListCardNigeriaBinding::inflate,
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
            wareHouseList.addAll(
                dispatchLotsList.listOfField(VegaCocoaDispatchLots::storageLocationCode).toSet()
            )
            updateWareHouseSpinner()
        } else {
            setupAdapter(dispatchLotsList)
        }
    }

    private fun updateWareHouseSpinner() {
        val stageAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_vega_nigeria_pile_management_wh,
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
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.green))
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
