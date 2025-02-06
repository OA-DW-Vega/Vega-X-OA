package com.olam.warehouse.vegax.processingecuador.ui.rmin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingecuador.R
import com.olam.warehouse.vegax.processingecuador.databinding.FragmentRminEcuadorCocoaSelectWhBinding
import com.olam.warehouse.vegax.processingecuador.databinding.RminEcuadorCocoaItemLotSummaryBinding
import com.olam.warehouse.vegax.processingecuador.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.processingecuador.utils.MULTIPLE_LOT
import com.olam.warehouse.vegax.processingecuador.utils.getColor
import com.olam.warehouse.vegax.processingecuador.utils.listOfField
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaEcuadorCocoaRminLotListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_rmin_ecuador_cocoa_select_wh
    private lateinit var binding: FragmentRminEcuadorCocoaSelectWhBinding

    private val vm: VegaEcuadorCocoaRminViewModel by viewModel()
    private var dispatchLotsList = mutableListOf<VegaCocoaRminLots>()
    private var wareHouseList = mutableListOf<String>()
    private var filteredDispatchLotsList = mutableListOf<VegaCocoaRminLots>()
    private var weight: Double? = 0.0
    private var count = 0
    private var model: VegaCocoaRminProcessing? = null
    private var listener: AddLotsListener? = null
    private var alreadySelected = mutableListOf<VegaCocoaRminLots>()
    private val mSearchList = mutableListOf<VegaCocoaRminLots>()
    private var isMultipleAdd = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? AddLotsListener
    }

    companion object {
        fun newInstance(model: VegaCocoaRminProcessing, isMultipleAdd: Boolean) =
            VegaEcuadorCocoaRminLotListFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
                putBoolean(MULTIPLE_LOT, isMultipleAdd)
            }

        const val SEARCH_HINT_TEXT = "Search Lot Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentRminEcuadorCocoaSelectWhBinding.inflate(layoutInflater)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingecuador/ui/rmin/VegaCocoaRminLotListFragment")
            .title("Rmin Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        vm.stocks.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.fetchStocks(model?.baseMaterialCode ?: "")
        binding.btnProceed.setOnClickListener { sendSelectedLots() }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()
                            val dataValue = it.data?.data!!
                            dataValue.let { item -> dispatchLotsList.addAll(item) }
                            updateSelectLotValues()
                            getWarehouseList()
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

    private fun updateSelectLotValues() {
        val filter = mutableListOf<VegaCocoaRminLots>()
        filter.addAll(dispatchLotsList)
        val map = alreadySelected.map { it.batchNumber to it.batchNumber }
        if (alreadySelected.isNotEmpty()) {
            filter.forEachIndexed { index, s ->
                if (map.contains(s.batchNumber to s.batchNumber)) {
                    dispatchLotsList[index].isAdded = true
                }
            }
        }
        setupAdapter(dispatchLotsList)
        updateWeight()
    }


    private fun setupAdapter(data: MutableList<VegaCocoaRminLots>) {
        if (data.size > 0) {
            binding.rvLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
        }

        binding.rvLots.setUpAdapter(
            data,
            R.layout.rmin_ecuador_cocoa_item_lot_summary,
            RminEcuadorCocoaItemLotSummaryBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvLotId.text = it.batchNumber
                bindItem.tvGradeValue.text = it.materialName
                bindItem.tvStLocationValue.text = it.storageLocationCode
                bindItem.tvWeightValue.text =
                    it.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitOfMeasure)
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
                    dispatchLotsList.forEach { item ->
                        if (item.batchNumber.equals(it.batchNumber)) item.isAdded = it.isAdded
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
        if (dispatchLotsList.isNotEmpty()) {
            wareHouseList.add(getString(R.string.all))
            wareHouseList.addAll(dispatchLotsList.listOfField(VegaCocoaRminLots::storageLocationCode).toSet())
            updateWareHouseSpinner()
        }
    }

    private fun updateWareHouseSpinner() {
        val stageAdapter = ArrayAdapter(requireContext(), R.layout.item_vega_ecuador_cocoa_processing_rmin_grade, wareHouseList)
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
                }
                else {
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

        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, Intent().putExtra("sas", "sadasdas"))
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
