package com.olam.warehouse.vegax.thirdpartysalescoffee.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.thirdpartysalescoffee.R
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeThirdPartyLotListModel
import com.olam.warehouse.vegax.thirdpartysalescoffee.databinding.FragmentCoffeeThirdPartyLotListBinding
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.getColor
import kotlinx.android.synthetic.main.item_coffee_thirdparty_inventory_lot.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaCoffeeThirdPartyLotListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_coffee_third_party_lot_list
    private lateinit var binding: FragmentCoffeeThirdPartyLotListBinding

    private val vm: VegaCoffeeThirdPartyViewModel by viewModel()
    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var wareHouseList = mutableListOf<String>()
    private var filteredDispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var weight: Double? = 0.0
    private var count = 0
    private var listener: VegaCoffeeThirdPartyAddLotListener? = null
    private var alreadySelected = mutableListOf<VegaCocoaDispatchLots>()
    private val mSearchList = mutableListOf<VegaCocoaDispatchLots>()
    private var isMultipleAdd = true
    private var materialCode: ArrayList<String> = ArrayList()
    private var isThirdPartyMaterial = false
    private var vendorCode = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? VegaCoffeeThirdPartyAddLotListener
    }

    companion object {
        fun newInstance(
            vegaLotModel: VegaCoffeeThirdPartyLotListModel
        ) =
            VegaCoffeeThirdPartyLotListFragment().putArgs {
                putParcelableArrayList("SelectedList", vegaLotModel.selectedList)
                putBoolean("MULTIPLE_LOT", vegaLotModel.isMultipleAdd)
                putStringArrayList("Material", vegaLotModel.material)
                putBoolean("thirdParty", vegaLotModel.isThirdParty)
                putString("vendor", vegaLotModel.vendorCode)
            }

        const val SEARCH_HINT_TEXT = "Search Lot Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentCoffeeThirdPartyLotListBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        alreadySelected = arguments?.getParcelableArrayList<VegaCocoaDispatchLots>("SelectedList") as ArrayList
        isMultipleAdd = arguments?.getBoolean("MULTIPLE_LOT") ?: true
        materialCode = arguments?.getStringArrayList("Material") ?: ArrayList()
        isThirdPartyMaterial = arguments?.getBoolean("thirdParty") ?: false
        vendorCode = arguments?.getString("vendor", "") ?: ""
    }

    private fun initUI() {
        vm.stockLots.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getStockList(materialCode)
        binding.btnProceed.setOnClickListener { sendSelectedLots() }
        binding.tvWareHouse.setOnClickListener { showWarehouseListDialog(wareHouseList) }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()
                            val dataValue = it.data?.data!!
                            val filterVendor = dataValue.filter { it.vendor == vendorCode }
                            dispatchLotsList.addAll(filterVendor)
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
        //showLoading()
        val mapSelected = alreadySelected.map { it.batchNumber }.toString()
        dispatchLotsList.forEachIndexed { index, s ->
            dispatchLotsList[index].isAdded = mapSelected.contains(s.batchNumber)
        }
        setupAdapter(dispatchLotsList)
        updateWeight()
    }


    private fun setupAdapter(data: MutableList<VegaCocoaDispatchLots>) {
        if (data.size > 0) {
            binding.rvLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
        }
        binding.rvLots.setUp(data, R.layout.item_coffee_thirdparty_inventory_lot, { it, pos ->
            tvLotId.text = it.batchNumber
            tvGradeValue.text = it.materialName
            tvStLocationValue.text = it.storageLocationCode
            tvWeightValue.text = it.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitOfMeasure)
            ivSelect.isChecked = it.isAdded
            ivSelect.setOnCheckedChangeListener { item, isChecked ->
                it.isAdded = isChecked
                if (!isMultipleAdd) {
                    if (isChecked)
                        removeChecked(pos, data)
                }
                updateWeight()
            }
            llLotItem.setOnClickListener { view ->
                    it.isAdded = !it.isAdded
                    if (!isMultipleAdd) {
                        if (it.isAdded)
                            removeChecked(pos, data)
                    } else {
                        it.isChecked = it.isAdded
                        ivSelect.isChecked = it.isAdded
                    }
                updateWeight()
            }

        })
        hideLoading()
    }

    private fun removeChecked(item: Int, list: MutableList<VegaCocoaDispatchLots>) {
        list.forEach { it.isAdded = false }
        list[item].isAdded = true
        setupAdapter(list)
    }

    private fun getWarehouseList() {
        if (dispatchLotsList.isNotEmpty()) {
            val list = dispatchLotsList.map { it.storageLocationCode ?: "" }.toSet()
            wareHouseList.addAll(list.toList())
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
        val data = dispatchLotsList.filter { it.isAdded == true } as ArrayList
        listener?.addedLots(data)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
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

    private fun showWarehouseListDialog(it: List<String>) {
        MaterialDialog(requireContext()).show {
            if (getCurrentKey().split("_")[1].contains("NI")) title(R.string.select_warehouse)
            else title(R.string.select_dest_wh)
            listItemsMultiChoice(items = it) { _, index, text ->
                filterWarehouseList(text.toString())
            }
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.ok),
                    true
                )
            )
        }
    }

    private fun filterWarehouseList(locations: String) {
        val filter = locations.replace("[", "").replace("]", "").trim()
        if (filter.isEmpty()) {
            setupAdapter(dispatchLotsList)
            binding.tvWareHouse.text = getString(R.string.all)
        } else {
            binding.tvWareHouse.text = filter
            val lotsList = dispatchLotsList.filter { locations.contains(it.storageLocationCode ?: "") }
            filteredDispatchLotsList.addAll(lotsList)
            setupAdapter(filteredDispatchLotsList)
        }
    }
}
