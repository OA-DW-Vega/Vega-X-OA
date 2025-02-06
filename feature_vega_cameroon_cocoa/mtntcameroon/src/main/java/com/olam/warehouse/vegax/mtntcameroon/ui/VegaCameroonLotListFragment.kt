package com.olam.warehouse.vegax.mtntcameroon.ui

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
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcameroon.R
import com.olam.warehouse.vegax.mtntcameroon.data.domain.model.VegaCameroonLotListModel
import com.olam.warehouse.vegax.mtntcameroon.databinding.FragmentCameroonMtntLotListBinding
import com.olam.warehouse.vegax.mtntcameroon.databinding.ItemCameroonMtntInventoryLotBinding
import com.olam.warehouse.vegax.mtntcameroon.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.mtntcameroon.utils.convertMtToKg
import com.olam.warehouse.vegax.mtntcameroon.utils.getColor
import com.olam.warehouse.vegax.mtntcameroon.utils.listOfField
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCameroonLotListFragment : BaseFragment(), UpdateSelectedLotWeightListener {

    override val layoutResourceId = R.layout.fragment_cameroon_mtnt_lot_list
    private lateinit var binding: FragmentCameroonMtntLotListBinding

    private val vm: VegaCameroonMtntViewModel by viewModel()
    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var wareHouseList = mutableListOf<String>()
    private var filteredDispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()

    private var listener: VegaCameroonAddLotListener? = null
    private var alreadySelected = mutableListOf<VegaCocoaDispatchLots>()
    private val mSearchList = mutableListOf<VegaCocoaDispatchLots>()
    private var isMultipleAdd = true
    private var materialCode: ArrayList<String> = ArrayList()
    private var isThirdPartyMaterial = false
    private var storageLocation: String = ""
    private var model: VegaCocoaDispatchWB? = null

    private lateinit var adapter: VegaCameroonLotListAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? VegaCameroonAddLotListener
    }

    companion object {
        fun newInstance(
            vegaLotModel: VegaCameroonLotListModel,
            list: VegaCocoaDispatchWB
        ) =
            VegaCameroonLotListFragment().putArgs {
                putParcelable(MODEL_BUNDLE, list)
                putParcelableArrayList("SelectedList", vegaLotModel.selectedList)
                putBoolean("MULTIPLE_LOT", vegaLotModel.isMultipleAdd)
                putStringArrayList("Material", vegaLotModel.material)
                putBoolean("thirdParty", vegaLotModel.isThirdParty)
            }

        const val SEARCH_HINT_TEXT = "Search Lot Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentCameroonMtntLotListBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        model = arguments?.getParcelable(MODEL_BUNDLE)
        storageLocation = model?.storageLocationCode!!
        alreadySelected =
            arguments?.getParcelableArrayList<VegaCocoaDispatchLots>("SelectedList") as ArrayList
        isMultipleAdd = arguments?.getBoolean("MULTIPLE_LOT") ?: true
        materialCode = arguments?.getStringArrayList("Material") ?: ArrayList()
        isThirdPartyMaterial = arguments?.getBoolean("thirdParty") ?: false
        initAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("mtntcameroon/ui/VegaCameroonLotListFragment")
            .title("Vega_Cameroon/Mtnt").with(tracker)
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnProceed, it, true)
        }
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
                            dataValue.forEachIndexed { index, s ->
                                dataValue[index].weight = convertMtToKg(
                                    dataValue[index].weight.toString(),
                                    dataValue[index].unitOfMeasure.toString()
                                )
                            }
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
        binding.tvWareHouse.text = storageLocation
        if (dispatchLotsList.isNotEmpty())
            setAdapter()
        else {
            binding.rvLots.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
        }
        updateWeight()
        hideLoading()
    }

    private fun initAdapter() {
        val list = ArrayList<VegaCocoaDispatchLots>()
        binding.rvLots.layoutManager = LinearLayoutManager(activity).apply { LinearLayoutManager.VERTICAL }
        adapter =
            VegaCameroonLotListAdapter(
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
            if (wareHouseList.contains(storageLocation)) {
                binding.tvWareHouse.text = storageLocation
                filterWarehouseList(storageLocation)
            } else {
                wareHouseList.add(storageLocation)
                adapter.addAllValues(mutableListOf<VegaCocoaDispatchLots>())
                binding.rvLots.visibility = View.GONE
                binding.tvNoData.visibility = View.VISIBLE
            }
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
            filteredDispatchLotsList.clear()
            filteredDispatchLotsList.addAll(lotsList)
            if (filteredDispatchLotsList.size > 0) {
                binding.rvLots.visibility = View.VISIBLE
                binding.tvNoData.visibility = View.GONE
            } else {
                binding.rvLots.visibility = View.GONE
                binding.tvNoData.visibility = View.VISIBLE
            }
            adapter.addAllValues(filteredDispatchLotsList)
        }
    }

    private fun setAdapter() {
        adapter.addAllValues(dispatchLotsList)

    }


    class VegaCameroonLotListAdapter(
        var data: ArrayList<VegaCocoaDispatchLots>,
        var isMultiple: Boolean = false, var listener: UpdateSelectedLotWeightListener
    ) :
        RecyclerView.Adapter<VegaCameroonLotListAdapter.LotViewHolder>() {

        private var previousSelected = -1

        class LotViewHolder(bind: ItemCameroonMtntInventoryLotBinding) :
            RecyclerView.ViewHolder(bind.root) {
            val binding = bind
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LotViewHolder {
            val viewHolder =
                ItemCameroonMtntInventoryLotBinding.inflate(
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
            holder.binding.tvLotId.text = data[holder.adapterPosition].batchNumber
            holder.binding.tvGradeValue.text = data[holder.adapterPosition].materialName
            holder.binding.tvStLocationValue.text = data[holder.adapterPosition].storageLocationCode
            /*when(data[holder.adapterPosition].unitOfMeasure){
                "KG" ->
                    holder.itemView.tvWeightValue.text =
                        data[holder.adapterPosition].weight?.toDouble()?.formatThreeDigits().plus(" ").plus(data[position].unitOfMeasure)
                "MT" ->
                    holder.itemView.tvWeightValue.text =
                        convertMtToKg(data[holder.adapterPosition].weight.toString()).toDouble().formatThreeDigits().plus(" ")
                            .plus("KG")

            }*/
            holder.binding.tvWeightValue.text =
                data[holder.adapterPosition].weight?.toDouble()?.formatThreeDigits().plus(" ")
                    .plus("KG")

            holder.binding.ivSelect.isChecked = data[holder.adapterPosition].isAdded
            if (data[holder.adapterPosition].isAdded) {
                previousSelected = holder.adapterPosition
            }
            holder.binding.llLotItem.setOnClickListener { view ->
                if (isMultiple) {
                    holder.binding.ivSelect.isChecked = !data[holder.adapterPosition].isAdded
                    data[holder.adapterPosition].isAdded = !data[holder.adapterPosition].isAdded
                } else {
                    if (!data[holder.adapterPosition].isAdded) {
                        if (previousSelected > -1) {
                            data[previousSelected].isAdded = false
                            notifyItemChanged(previousSelected)
                        }
                        previousSelected = holder.adapterPosition
                        data[holder.adapterPosition].isAdded = true
                        holder.binding.ivSelect.isChecked = true
                    } else {
                        previousSelected = -1
                        data[holder.adapterPosition].isAdded = false
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
}
