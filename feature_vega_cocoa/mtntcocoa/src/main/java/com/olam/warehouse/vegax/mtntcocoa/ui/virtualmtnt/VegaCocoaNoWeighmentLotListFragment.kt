package com.olam.warehouse.vegax.mtntcocoa.ui.virtualmtnt

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
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentLot
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcocoa.R
import com.olam.warehouse.vegax.mtntcocoa.data.domain.model.VegaCocoaLotListModel
import com.olam.warehouse.vegax.mtntcocoa.databinding.FragmentCocoaMtntLotListBinding
import com.olam.warehouse.vegax.mtntcocoa.databinding.ItemCocoaInventoryLotBinding
import com.olam.warehouse.vegax.mtntcocoa.ui.VegaCocoaMtntViewModel
import com.olam.warehouse.vegax.mtntcocoa.utils.getColor
import com.olam.warehouse.vegax.mtntcocoa.utils.listOfField
import com.olam.warehouse.vegax.mtntcocoa.utils.prepareOfflineStockInfo
import kotlinx.android.synthetic.main.item_cocoa_inventory_lot.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCocoaNoWeighmentLotListFragment : BaseFragment(), UpdateSelectedLotWeightListener {

    override val layoutResourceId = R.layout.fragment_cocoa_mtnt_lot_list
    private lateinit var binding: FragmentCocoaMtntLotListBinding

    private val vm: VegaCocoaMtntViewModel by viewModel()
    private var dispatchLotsList = mutableListOf<VegaCocoaNoWeighmentLot>()
    private var wareHouseList = mutableListOf<String>()
    private var filteredDispatchLotsList = mutableListOf<VegaCocoaNoWeighmentLot>()
    private var weight: Double? = 0.0
    private var count = 0
    private var listener: VegaCocoaAddLotListener? = null
    private var alreadySelected = mutableListOf<VegaCocoaNoWeighmentLot>()
    private val mSearchList = mutableListOf<VegaCocoaNoWeighmentLot>()
    private var isMultipleAdd = true
    private var materialCode: ArrayList<String> = ArrayList()
    private var isThirdPartyMaterial = false


    private lateinit var adapter: VegaCoffeeLotListAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? VegaCocoaAddLotListener
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("/mtntcocoa/ui/virtualmtnt/VegaCocoaNoWeighmentLotListFragment")
            .title("Dispatch Cocoa")
            .with(tracker)
    }

    companion object {
        fun newInstance(
            vegaLotModel: VegaCocoaLotListModel
        ) =
            VegaCocoaNoWeighmentLotListFragment().putArgs {
                putParcelableArrayList("SelectedList", vegaLotModel.selectedList)
                putBoolean("MULTIPLE_LOT", vegaLotModel.isMultipleAdd)
                putStringArrayList("Material", vegaLotModel.material)
                putBoolean("thirdParty", vegaLotModel.isThirdParty)
            }

        const val SEARCH_HINT_TEXT = "Search Lot Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentCocoaMtntLotListBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    private fun initExtra() {
        alreadySelected = arguments?.getParcelableArrayList<VegaCocoaNoWeighmentLot>("SelectedList") as ArrayList
        isMultipleAdd = arguments?.getBoolean("MULTIPLE_LOT") ?: true
        materialCode = arguments?.getStringArrayList("Material") ?: ArrayList()
        isThirdPartyMaterial = arguments?.getBoolean("thirdParty") ?: false
        initAdapter()
    }

    private fun initUI() {
        vm.qualityNoWeighmentDetails.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.offlineStockLots.observe(viewLifecycleOwner, Observer { updateOfflineUI(prepareOfflineStockInfo(it)) })
        if (AppUtils.isOnline()) {
            vm.getNoWeighmentStockList(materialCode)
        } else {
            showLoading()
            vm.getOfflineStockList(materialCode[0])
        }
        binding.btnProceed.setOnClickListener { sendSelectedLots() }
        binding.tvWareHouse.setOnClickListener { showWarehouseListDialog(wareHouseList) }
    }

    private fun updateOfflineUI(list: List<VegaCocoaNoWeighmentLot>) {
        dispatchLotsList.clear()
        dispatchLotsList.addAll(list)
        updateSelectLotValues()
        getWarehouseList()
        hideLoading()
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaNoWeighmentLot>>>) {
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
        val list = ArrayList<VegaCocoaNoWeighmentLot>()
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
            wareHouseList.addAll(dispatchLotsList.listOfField(VegaCocoaNoWeighmentLot::storageLocationCode).toSet())
        }
    }

    private fun sendSelectedLots() {
        listener?.addedLots(adapter.getSelected() as ArrayList<VegaCocoaNoWeighmentLot>)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        //menu.clear()
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
            searchView.queryHint = SEARCH_HINT_TEXT
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
        var data: ArrayList<VegaCocoaNoWeighmentLot>,
        var isMultiple: Boolean = false, var listener: UpdateSelectedLotWeightListener
    ) :
        RecyclerView.Adapter<VegaCoffeeLotListAdapter.LotViewHolder>() {

        private var previousSelected = -1

        class LotViewHolder(bind: ItemCocoaInventoryLotBinding) : RecyclerView.ViewHolder(bind.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LotViewHolder {
            val viewHolder =
                ItemCocoaInventoryLotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return LotViewHolder(
                viewHolder
            )
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: LotViewHolder, position: Int) {
            holder.itemView.tvLotId.text = data[position].batchNumber
            holder.itemView.tvGradeValue.text = data[position].materialName
            holder.itemView.tvStLocationValue.text = data[position].storageLocationCode
            holder.itemView.tvWeightValue.text =
                data[position].weight?.toDouble()?.formatThreeDigits().plus(" ").plus(data[position].unitOfMeasure)
            holder.itemView.ivSelect.isChecked = data[position].isAdded
            if (data[position].isAdded) {
                previousSelected = position
            }
            holder.itemView.llLotItem.setOnClickListener { view ->
                if (isMultiple) {
                    holder.itemView.ivSelect.isChecked = !data[position].isAdded
                    data[position].isAdded = !data[position].isAdded
                } else {
                    if (!data[position].isAdded) {
                        if (previousSelected > -1) {
                            data[previousSelected].isAdded = false
                            notifyItemChanged(previousSelected)
                        }
                        previousSelected = position
                        data[position].isAdded = true
                        holder.itemView.ivSelect.isChecked = true
                    } else {
                        previousSelected = -1
                        data[position].isAdded = false
                        holder.itemView.ivSelect.isChecked = false
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

        fun addAllValues(list: MutableList<VegaCocoaNoWeighmentLot>) {
            data.clear()
            notifyDataSetChanged()
            data.addAll(list)
            notifyDataSetChanged()
        }

        fun getSelected(): List<VegaCocoaNoWeighmentLot> {
            return data.filter { it.isAdded }
        }
    }

    override fun updateLotWeight(count: String, weight: String) {
        updateWeight(count, weight)
    }
}
