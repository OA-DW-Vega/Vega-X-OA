package com.olam.warehouse.vegax.processingcameroon.ui.rmin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingcameroon.R
import com.olam.warehouse.vegax.processingcameroon.databinding.FragmentCameroonInventoryLotListBinding
import com.olam.warehouse.vegax.processingcameroon.databinding.ItemCameroonInventoryLotBinding
import com.olam.warehouse.vegax.processingcameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCameroonRMINLotListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_cameroon_inventory_lot_list
    private lateinit var binding: FragmentCameroonInventoryLotListBinding

    private val vm: VegaCameroonRminViewModel by viewModel()
    private var dispatchLotsList = mutableListOf<VegaCoffeeRminLots>()
    private var wareHouseList = mutableListOf<String>()
    private var filteredDispatchLotsList = mutableListOf<VegaCoffeeRminLots>()
    private var weight: Double? = 0.0
    private var count = 0
    private var model: VegaCoffeeRminProcessing? = null
    private var listener: CameroonAddLotsListener? = null
    private var alreadySelected = mutableListOf<VegaCoffeeRminLots>()
    private val mSearchList = mutableListOf<VegaCoffeeRminLots>()
    private var isMultipleAdd = true
    private var materialCode: String? = ""
    private var isThirdPartyMaterial = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? CameroonAddLotsListener
    }

    companion object {
        fun newInstance(
            model: VegaCoffeeRminProcessing,
            isMultipleAdd: Boolean,
            material: String,
            isThirdParty: Boolean
        ) =
            VegaCameroonRMINLotListFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
                putBoolean(MULTIPLE_LOT, isMultipleAdd)
                putString("Material", material)
                putBoolean("thirdParty", isThirdParty)
            }

        const val SEARCH_HINT_TEXT = "Search Lot Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentCameroonInventoryLotListBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        model = arguments?.getParcelable(MODEL_BUNDLE)
        alreadySelected.addAll(model?.lotList ?: mutableListOf())
        isMultipleAdd = arguments?.getBoolean(MULTIPLE_LOT) ?: true
        materialCode = arguments?.getString("Material")
        isThirdPartyMaterial = arguments?.getBoolean("thirdParty") ?: false
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnProceed, it, true)
        }
        vm.stocks.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.fetchStocks(materialCode ?: "")
        binding.btnProceed.setOnClickListener { sendSelectedLots() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcameroon/ui/rmin/VegaCameroonRMINLotListFragment")
            .title("Vega_Cameroon/Processing")
            .with(tracker)
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()
                            val dataValue = it.data?.data!!
                            dataValue.forEachIndexed { index, s ->
                                when (s.unitOfMeasure) {
                                    "KG" ->
                                        dataValue[index].weight = dataValue[index].weight.toString()
                                    "MT" ->
                                        dataValue[index].weight =
                                            convertMtToKg(dataValue[index].weight.toString())
                                }
                            }
                            if (!isThirdPartyMaterial) {
                                dispatchLotsList.addAll(dataValue)
                            } else {
                                dispatchLotsList.addAll(dataValue.filter { it.vendor == model?.vendor })
                            }

                            updateSelectLotValues()
                            getWarehouseList()
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
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
        val filter = mutableListOf<VegaCoffeeRminLots>()
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
        updateWeight()
    }


    private fun setupAdapter(data: MutableList<VegaCoffeeRminLots>) {
        if (data.size > 0) {
            binding.rvLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
        }

        binding.rvLots.setUpAdapter(
            data,
            R.layout.item_cameroon_inventory_lot,
            ItemCameroonInventoryLotBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvLotId.text = it.batchNumber
                bindItem.tvGradeValue.text = it.materialName
                bindItem.tvStLocationValue.text = it.storageLocationCode
                bindItem.tvWeightValue.text =
                    it.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitOfMeasure)
                bindItem.ivSelect.isChecked = it.isAdded ?: false

                bindItem.llLotItem.setOnClickListener { view ->


                    data[pos].isAdded= !it.isAdded!!
                   // it.isAdded = !it.isAdded!!
                   /* data.forEach { item ->
                        if (item.batchNumber.equals(it.batchNumber)) item.isAdded = data[pos].isAdded
                    }*/
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

    private fun removeChecked(item: Int, list: MutableList<VegaCoffeeRminLots>) {
        list.forEach { it.isAdded = false }
        list[item].isAdded = true
        setupAdapter(list)
    }

    private fun getWarehouseList() {
        if (dispatchLotsList.isNotEmpty()) {
            wareHouseList.add(getString(R.string.all))
            wareHouseList.addAll(dispatchLotsList.listOfField(VegaCoffeeRminLots::storageLocationCode).toSet())
            updateWareHouseSpinner()
        }
    }

    private fun updateWareHouseSpinner() {
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_cameroon_processing_rmin_grade, wareHouseList)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spWareHouse.adapter = stageAdapter
        val defaultposition = 0
        binding.spWareHouse.setSelection(defaultposition)
        binding.spWareHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Comment for Sonar Fix
            }

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

    private fun updateWeight() {
        weight = 0.0
        val filter = dispatchLotsList.filter { it.isAdded == true }
        count = filter.size
        for (item in filter) {
            weight = weight?.plus(item.weight?.toDouble() ?: 0.0)
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
        val data = dispatchLotsList.filter {
            it.isAdded == true } as ArrayList
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
            searchView.queryHint = getString(com.olam.warehouse.presentation.R.string.search_by_lot)
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
                            dispatchLotsList.forEach { qtyWb ->
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
