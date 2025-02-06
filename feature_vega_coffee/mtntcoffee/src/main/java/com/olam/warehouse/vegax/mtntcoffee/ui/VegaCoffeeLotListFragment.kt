package com.olam.warehouse.vegax.mtntcoffee.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcoffee.R
import com.olam.warehouse.vegax.mtntcoffee.data.domain.model.VegaCoffeeLotListModel
import com.olam.warehouse.vegax.mtntcoffee.databinding.FragmentCoffeeMtntLotListBinding
import com.olam.warehouse.vegax.mtntcoffee.databinding.ItemCoffeeInventoryLotBinding
import com.olam.warehouse.vegax.mtntcoffee.utils.getColor
import com.olam.warehouse.vegax.mtntcoffee.utils.listOfField
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCoffeeLotListFragment : BaseFragment(), UpdateSelectedLotWeightListener {

    override val layoutResourceId = R.layout.fragment_coffee_mtnt_lot_list
    private lateinit var binding: FragmentCoffeeMtntLotListBinding

    private val vm: VegaCoffeeMtntViewModel by viewModel()
    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var wareHouseList = mutableListOf<String>()
    private var filteredDispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var weight: Double? = 0.0
    private var count = 0
    private var listener: VegaCoffeeAddLotListener? = null
    private var alreadySelected = mutableListOf<VegaCocoaDispatchLots>()
    private val mSearchList = mutableListOf<VegaCocoaDispatchLots>()
    private var isMultipleAdd = true
    private var materialCode: ArrayList<String> = ArrayList()
    private var isThirdPartyMaterial = false

    private lateinit var adapter: VegaCoffeeLotListAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? VegaCoffeeAddLotListener
    }

    companion object {
        fun newInstance(
            vegaLotModel: VegaCoffeeLotListModel
        ) =
            VegaCoffeeLotListFragment().putArgs {
                putParcelableArrayList("SelectedList", vegaLotModel.selectedList)
                putBoolean("MULTIPLE_LOT", vegaLotModel.isMultipleAdd)
                putStringArrayList("Material", vegaLotModel.material)
                putBoolean("thirdParty", vegaLotModel.isThirdParty)
            }

        const val SEARCH_HINT_TEXT = "Search Lot Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentCoffeeMtntLotListBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        alreadySelected = arguments?.getParcelableArrayList<VegaCocoaDispatchLots>("SelectedList") as ArrayList
        isMultipleAdd = arguments?.getBoolean("MULTIPLE_LOT") ?: true
        materialCode = arguments?.getStringArrayList("Material") ?: ArrayList()
        isThirdPartyMaterial = arguments?.getBoolean("thirdParty") ?: false
        initAdapter()
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
                            dispatchLotsList.addAll(dataValue)
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
        if (alreadySelected.isNotEmpty()) {
            val mapSelected = alreadySelected.map { it.batchNumber }.toString()
            dispatchLotsList.forEachIndexed { index, s ->
                dispatchLotsList[index].isAdded = mapSelected.contains(s.batchNumber)
            }
        }
        setAdapter()
        updateWeight()
        hideLoading()
    }

    private fun initAdapter() {
        val list = ArrayList<VegaCocoaDispatchLots>()
        binding.rvLots.layoutManager = LinearLayoutManager(activity).apply { LinearLayoutManager.VERTICAL }
        adapter =
            VegaCoffeeLotListAdapter(
                list,
                isMultipleAdd,
                this
            )
        binding.rvLots.adapter = adapter
    }

    private fun updateWeight() {
        var weight = 0.0
        val filter = dispatchLotsList.filter { it.isAdded }
        val count = filter.size
        for (item in filter) {
            weight = weight.plus(item.weight?.toDouble() ?: 0.0)
        }
        if (count == 0) weight = 0.0
        updateWeight(count.toString(), weight.toString())
    }

    private fun updateWeight(count: String, weight: String) {
        binding.tvLot.text = count.plus(getString(R.string.lot_selected))
        binding.tvLotWeight.text = weight.plus(" Kg")
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, Intent().putExtra("sas", "sadasdas"))
    }

    private fun getWarehouseList() {
        if (dispatchLotsList.isNotEmpty()) {
            wareHouseList.addAll(dispatchLotsList.listOfField(VegaCocoaDispatchLots::storageLocationCode).toSet())
        }
    }

    private fun sendSelectedLots() {
        listener?.addedLots(adapter.getSelected() as ArrayList<VegaCocoaDispatchLots>)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
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
                            adapter.addAllValues(if (filteredDispatchLotsList.isEmpty()) dispatchLotsList else filteredDispatchLotsList)
                        } else {
                            mSearchList.clear()
                            dispatchLotsList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.batchNumber.contains(text)) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            adapter.addAllValues(mSearchList)
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
            title(R.string.select_dest_wh)
            listItemsMultiChoice(items = it) { _, index, text ->
                filterWarehouseList(text.toString())
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.presentation.R.string.ok), true))
        }
    }

    private fun filterWarehouseList(locations: String) {
        val filter = locations.replace("[", "").replace("]", "").trim()
        if (filter.isEmpty()) {
            adapter.addAllValues(dispatchLotsList)
            binding.tvWareHouse.text = getString(R.string.all)
        } else {
            binding.tvWareHouse.text = filter
            val lotsList = dispatchLotsList.filter { locations.contains(it.storageLocationCode ?: "") }
            filteredDispatchLotsList.addAll(lotsList)
            adapter.addAllValues(filteredDispatchLotsList)
        }
    }

    private fun setAdapter() {
        adapter.addAllValues(dispatchLotsList)

    }


    class VegaCoffeeLotListAdapter(
        var data: ArrayList<VegaCocoaDispatchLots>,
        var isMultiple: Boolean = false, var listener: UpdateSelectedLotWeightListener
    ) :
        RecyclerView.Adapter<VegaCoffeeLotListAdapter.LotViewHolder>() {

        private var previousSelected = -1

        class LotViewHolder(bind: ItemCoffeeInventoryLotBinding) :
            RecyclerView.ViewHolder(bind.root) {
            val binding = bind
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LotViewHolder {
            val viewHolder =
                ItemCoffeeInventoryLotBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            return LotViewHolder(
                viewHolder
            )
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: LotViewHolder, position: Int) {
            holder.binding.tvLotId.text = data[position].batchNumber
            holder.binding.tvGradeValue.text = data[position].materialName
            holder.binding.tvStLocationValue.text = data[position].storageLocationCode
            holder.binding.tvWeightValue.text =
                data[position].weight?.toDouble()?.formatThreeDigits().plus(" ")
                    .plus(data[position].unitOfMeasure)
            holder.binding.ivSelect.isChecked = data[position].isAdded
            if (data[position].isAdded) {
                previousSelected = position
            }
            holder.binding.llLotItem.setOnClickListener { view ->
                if (isMultiple) {
                    holder.binding.ivSelect.isChecked = !data[position].isAdded
                    data[position].isAdded = !data[position].isAdded
                } else {
                    if (!data[position].isAdded) {
                        if (previousSelected > -1) {
                            data[previousSelected].isAdded = false
                            notifyItemChanged(previousSelected)
                        }
                        previousSelected = position
                        data[position].isAdded = true
                        holder.binding.ivSelect.isChecked = true
                    } else {
                        previousSelected = -1
                        data[position].isAdded = false
                        holder.binding.ivSelect.isChecked = false
                    }
                }
                updateWeight()
            }
        }

        private fun updateWeight() {
            var weight = 0.0
            val filter = data.filter { it.isAdded }
            val count = filter.size
            for (item in filter) {
                weight = weight.plus(item.weight?.toDouble() ?: 0.0)
            }
            if (count == 0) weight = 0.0
            listener.updateLotWeight(count.toString(), weight.toString())
        }

        fun addAllValues(list: MutableList<VegaCocoaDispatchLots>) {
            data.clear()
            notifyDataSetChanged()
            data.addAll(list)
            notifyDataSetChanged()
        }

        fun getSelected(): List<VegaCocoaDispatchLots> {
            return data.filter { it.isAdded }
        }
    }

    override fun updateLotWeight(count: String, weight: String) {
        updateWeight(count, weight)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("mtntcoffee.ui.weighbridge").title("IVC/Coffee/MTNT/Lot List")
            .with(tracker)
    }
}
