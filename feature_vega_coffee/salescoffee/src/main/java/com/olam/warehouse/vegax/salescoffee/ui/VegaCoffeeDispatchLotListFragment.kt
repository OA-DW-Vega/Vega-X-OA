package com.olam.warehouse.vegax.salescoffee.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.salescoffee.R
import com.olam.warehouse.vegax.salescoffee.databinding.FragmentCoffeeSalesLotListBinding
import com.olam.warehouse.vegax.salescoffee.databinding.ItemCoffeeSalesLotListDetailBinding
import com.olam.warehouse.vegax.salescoffee.utils.MATERIAL_LIST
import com.olam.warehouse.vegax.salescoffee.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.salescoffee.utils.getColor
import com.olam.warehouse.vegax.salescoffee.utils.listOfField
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 8/22/2020.
 */
class VegaCoffeeDispatchLotListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_coffee_sales_lot_list
    private lateinit var binding: FragmentCoffeeSalesLotListBinding
    private val vm: VegaCoffeeSalesViewModel by viewModel()
    private var dispatchLotsList = mutableListOf<VegaCoffeeSalesLots>()
    private var filteredDispatchLotsList = mutableListOf<VegaCoffeeSalesLots>()
    private var wareHouseList = mutableListOf<String>()
    private var weight: Double? = 0.0
    private var count = 0
    private var alreadySelected = mutableListOf<VegaCoffeeSalesLots>()
    private val mSearchList = mutableListOf<VegaCoffeeSalesLots>()
    private var isMultipleAdd = true
    private var materialList = arrayListOf<String>()
    private var listener: CallBack? = null
    private var model: VegaCoffeeSalesOrder? = null

    interface CallBack {
        fun addedLots(
            lots: ArrayList<VegaCoffeeSalesLots>
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCoffeeSalesOrder, materialList: ArrayList<String>, isMultipleAdd: Boolean = true) =
            VegaCoffeeDispatchLotListFragment().putArgs {
                putStringArrayList(MATERIAL_LIST, materialList)
                putParcelable(MODEL_BUNDLE, model)
                putBoolean("multipleAdd", isMultipleAdd)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentCoffeeSalesLotListBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("thirdpartysalescoffee/ui/VegaThirdPartyTypeFragment").title("IVC/Coffee/Sales/Dispatch Lot List")
            .with(tracker)

    }

    private fun initExtra() {
        model = arguments?.getParcelable(MODEL_BUNDLE)
        alreadySelected.addAll(model?.lotList ?: mutableListOf())
        materialList = arguments?.getStringArrayList(MATERIAL_LIST) as ArrayList<String>
        isMultipleAdd = arguments?.getBoolean("multipleAdd") ?: true
    }

    private fun initUI() {
        vm.stockList.observe(viewLifecycleOwner, Observer { updateUI(it) })
        //vm.stocksLocal.observe(viewLifecycleOwner, Observer { updateLocalUI(it) })
        if (materialList.size > 0) {
            vm.getStockList(materialList)
        }
        binding.btnProceed.setOnClickListener { sendSelectedLots() }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()
                            /*val filteredDispatchStocksList = mutableListOf<VegaCoffeeSalesLots>()
                            val dataValue = it.data?.data!!
                            dataValue.let { item -> filteredDispatchStocksList.addAll(item) }*/
                            dispatchLotsList =
                                if (it.data?.data!!.size > 0) it.data?.data as MutableList<VegaCoffeeSalesLots> else mutableListOf()
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
        val filter = mutableListOf<VegaCoffeeSalesLots>()
        filter.addAll(dispatchLotsList)
        if (isMultipleAdd)
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
//        setupAdapter(dispatchLotsList)
    }


    private fun setupAdapter(data: MutableList<VegaCoffeeSalesLots>) {
        if (data.size > 0) {
            binding.rvLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
        }
        binding.rvLots.setUpAdapter(
            data,
            R.layout.item_coffee_sales_lot_list_detail,
            ItemCoffeeSalesLotListDetailBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvLotId.text = it.batchNumber
                bindItem.tvGradeValue.text = it.materialName
                bindItem.tvStLocationValue.text = it.storageLocationCode
                bindItem.tvWeightValue.text =
                    it.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitOfMeasure)
                bindItem.ivSelect.isChecked = it.isAdded ?: false
                bindItem.llLotItem.setOnClickListener { view ->
                    it.isAdded = !it.isAdded!!
                    bindItem.ivSelect.isChecked = it.isAdded ?: false
                    /* dispatchLotsList.forEach { item ->
                         if (item.batchNumber.equals(it.batchNumber) && item.materialCode.equals(it.materialCode)) item.isAdded =
                             it.isAdded
                     }*/
                    //binding.rvLots.adapter?.notifyItemChanged(pos)
                    //updateWeight()
                    if (!isMultipleAdd) {
                        sendSelectedLots()
                    }
                }

                bindItem.ivSelect.setOnClickListener { view ->
                    it.isAdded = !it.isAdded!!
                    bindItem.ivSelect.isChecked = it.isAdded ?: false
                    if (!isMultipleAdd) {
                        sendSelectedLots()
                    }
                }
            })
    }

    private fun removeChecked(item: Int, list: MutableList<VegaCoffeeSalesLots>) {
        list.forEach { it.isAdded = false }
        list[item].isAdded = true
        setupAdapter(list)
    }

    private fun getWarehouseList() {
        if (dispatchLotsList.isNotEmpty()) {
            wareHouseList.add(getString(R.string.all))
            wareHouseList.addAll(dispatchLotsList.listOfField(VegaCoffeeSalesLots::storageLocationCode).toSet())
            updateWareHouseSpinner()
        } else {
            setupAdapter(dispatchLotsList)
        }
    }

    private fun updateWareHouseSpinner() {
        val stageAdapter = ArrayAdapter(requireContext(), R.layout.item_vega_coffee_dispatch_wh, wareHouseList)
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
