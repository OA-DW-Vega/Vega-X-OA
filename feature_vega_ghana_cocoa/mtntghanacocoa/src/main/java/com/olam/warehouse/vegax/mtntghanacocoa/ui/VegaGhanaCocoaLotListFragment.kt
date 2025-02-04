package com.olam.warehouse.vegax.mtntghanacocoa.ui

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
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.mtntghanacocoa.R
import com.olam.warehouse.vegax.mtntghanacocoa.data.domain.model.VegaGhanaMtntLotListModel
import com.olam.warehouse.vegax.mtntghanacocoa.databinding.FragmentGhanaCocoaMtntLotListBinding
import com.olam.warehouse.vegax.mtntghanacocoa.databinding.ItemGhanaCocoaInventoryLotBinding
import com.olam.warehouse.vegax.mtntghanacocoa.utils.*
import kotlinx.android.synthetic.main.item_ghana_cocoa_inventory_lot.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaGhanaCocoaLotListFragment : BaseFragment(), UpdateNigeriaSesameMtntSelectedLotWeightListener {

    override val layoutResourceId = R.layout.fragment_ghana_cocoa_mtnt_lot_list
    private lateinit var binding: FragmentGhanaCocoaMtntLotListBinding

    private val vm: VegaGhanaCocoaMtntViewModel by viewModel()
    private var dispatchLotsList = mutableListOf<VegaGhanaCocoaDispatchLots>()
    private var wareHouseList = mutableListOf<String>()
    private var filteredDispatchLotsList = mutableListOf<VegaGhanaCocoaDispatchLots>()
    private var weight: Double? = 0.0
    private var count = 0
    private var listenerCocoa: VegaGhanaCocoaAddLotListener? = null
    private var alreadySelected = mutableListOf<VegaGhanaCocoaDispatchLots>()
    private val mSearchList = mutableListOf<VegaGhanaCocoaDispatchLots>()
    private var isMultipleAdd = true
    private var materialCode: ArrayList<String> = ArrayList()
    private var isThirdPartyMaterial = false
    private var sendingWareHouseList = mutableListOf<VegaCustomStLocation>()
    private var storageLocationList: ArrayList<String> = ArrayList()
    private var uomDetails = ArrayList<VegaUomDetails>()
    private var model: VegaCocoaDispatchWB? = null

    private lateinit var adapter: VegaCoffeeLotListAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listenerCocoa = context as? VegaGhanaCocoaAddLotListener
    }

    companion object {
        fun newInstance(
            vegaLotModel: VegaGhanaMtntLotListModel,
            list: VegaCocoaDispatchWB
        ) =
            VegaGhanaCocoaLotListFragment().putArgs {
                putParcelableArrayList("SelectedList", vegaLotModel.selectedList)
                putBoolean("MULTIPLE_LOT", vegaLotModel.isMultipleAdd)
                putStringArrayList("Material", vegaLotModel.material)
                putBoolean("thirdParty", vegaLotModel.isThirdParty)
                putParcelable(MODEL_BUNDLE, list)
            }

        const val SEARCH_HINT_TEXT = "Search Lot Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentGhanaCocoaMtntLotListBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        alreadySelected =
            arguments?.getParcelableArrayList<VegaGhanaCocoaDispatchLots>("SelectedList") as ArrayList
        isMultipleAdd = arguments?.getBoolean("MULTIPLE_LOT") ?: true
        materialCode = arguments?.getStringArrayList("Material") ?: ArrayList()
        isThirdPartyMaterial = arguments?.getBoolean("thirdParty") ?: false
        model = arguments?.getParcelable(MODEL_BUNDLE)
        vm.dispatchWh = model ?: VegaCocoaDispatchWB()
    }

    private fun initUI() {
        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            sendingWareHouseList = it.toMutableList()
            storageLocationList = sendingWareHouseList.map { it.procureLocationCode } as ArrayList<String>
        })
        vm.getCustomLocations()
        vm.uomDetail.observe(viewLifecycleOwner, Observer {
            uomDetails = it as ArrayList<VegaUomDetails>
            initAdapter()
            println("Roshna => $it")
        })
        vm.getUomDetails()

        vm.stockLots.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.stocksOffline.observe(viewLifecycleOwner, Observer { item ->
            var tempList = item.filter { !it.batchNumber.startsWith("Z") }
            var dataList = tempList
            updateOfflineUI(dataList)
        })
        if (AppUtils.isOnline()) vm.getStockList(materialCode) else vm.fetchStocksOffline()
//        vm.getStockList(materialCode)
        binding.btnProceed.setOnClickListener { sendSelectedLots() }
        binding.tvWareHouse.setOnClickListener { showWarehouseListDialog(wareHouseList) }
    }

    private fun updateOfflineUI(offlineStocks: List<VegaEcuadorDispatchStocks>?) {
        var offlineFilteredStock = ArrayList<VegaEcuadorDispatchStocks>()
        offlineStocks?.forEach { stock ->
            storageLocationList.forEach {
                if (stock.storageLocationCode == it)
                    offlineFilteredStock.add(stock)
            }
        }
        var stocks = ArrayList<VegaEcuadorDispatchStocks>()
        materialCode.forEach { material ->
            offlineFilteredStock.forEach { item ->
                if (item.materialCode.contains(material))
                    stocks.add(item)
            }
        }

        stocks.forEach { item ->
            var lot = VegaGhanaCocoaDispatchLots()

            lot.batchNumber = item.batchNumber
            lot.materialCode = item.materialCode.toString()
            lot.materialName = item.materialName
            lot.plantId = item.plantId
            lot.plantName = item.plantName
            lot.storageLocationCode = item.storageLocationCode
            lot.unitOfMeasure = item.unitOfMeasure
            lot.weight = item.weight

            dispatchLotsList.add(lot)
        }

        updateSelectLotValues()
        getWarehouseList()
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()
                            val dataList = it.data?.data!!
                            var dataValue = dataList.filter { !it.batchNumber.startsWith("Z") }
                                .filter { it.storageLocationCode.equals(vm.dispatchWh.storageLocationCode) }
                            dataValue.forEachIndexed { index, s ->
                                dataValue[index].weight = dataValue[index].weight.toString()
                            }
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
        val list = ArrayList<VegaGhanaCocoaDispatchLots>()
        binding.rvLots.layoutManager = LinearLayoutManager(activity).apply { LinearLayoutManager.VERTICAL }
        adapter =
            VegaCoffeeLotListAdapter(
                list,
                uomDetails,
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
        binding.tvLot.text = count.plus(" ").plus(getString(R.string.lot_selected))
        binding.tvLotWeight.text = weight.plus(" MT")
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, Intent().putExtra("sas", "sadasdas"))
    }

    private fun getWarehouseList() {
        if (dispatchLotsList.isNotEmpty()) {
            wareHouseList.addAll(dispatchLotsList.listOfField(VegaGhanaCocoaDispatchLots::storageLocationCode).toSet())
        }
    }

    private fun sendSelectedLots() {
        listenerCocoa?.addedLots(adapter.getSelected() as ArrayList<VegaGhanaCocoaDispatchLots>)
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
            title(R.string.select_wh)
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
        var data: ArrayList<VegaGhanaCocoaDispatchLots>,
        var uomDetails: ArrayList<VegaUomDetails>,
        var isMultiple: Boolean = false,
        var listener: UpdateNigeriaSesameMtntSelectedLotWeightListener
    ) :
        RecyclerView.Adapter<VegaCoffeeLotListAdapter.LotViewHolder>() {

        private var previousSelected = -1

        class LotViewHolder(bind: ItemGhanaCocoaInventoryLotBinding) : RecyclerView.ViewHolder(bind.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LotViewHolder {
            val viewHolder =
                ItemGhanaCocoaInventoryLotBinding.inflate(LayoutInflater.from(parent.context), parent, false)

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
            var uom =
                ((uomDetails.filter { ((MATERIAL_CODE.plus(it.materialCode)).equals(data[position].materialCode)) }).filter {
                    it.fromUom.equals(BAG) }
                        ).single()
//                ((uomDetails.filter { (((it.materialCode)).contains(data[position].materialCode)) }).filter {
//                    it.fromUom.equals(BAG)
//                }).single()
            holder.itemView.tvWeightValue.text = (data[position].weight?.toDouble()
                ?.div((uom.value1?.toInt()?.toDouble()?.div(uom.value2?.toInt()?.toDouble()!!)) ?: 1.0)).toString()
                .plus(" Bags")
//            holder.itemView.tvWeightValue.text =
//                data[position].weight?.toDouble()?.formatThreeDigits().plus(" ").plus("MT")
            holder.itemView.ivSelect.isChecked = data[position].isAdded
            if (data[position].isAdded) {
                previousSelected = position
            }
            holder.itemView.llLotItem.setOnClickListener { view ->
                if (true) {
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

        fun addAllValues(list: MutableList<VegaGhanaCocoaDispatchLots>) {
            data.clear()
            notifyDataSetChanged()
            data.addAll(list)
            notifyDataSetChanged()
        }

        fun getSelected(): List<VegaGhanaCocoaDispatchLots> {
            return data.filter { it.isAdded }
        }
    }

    override fun updateLotWeight(count: String, weight: String) {
        updateWeight(count, weight)
    }
}
