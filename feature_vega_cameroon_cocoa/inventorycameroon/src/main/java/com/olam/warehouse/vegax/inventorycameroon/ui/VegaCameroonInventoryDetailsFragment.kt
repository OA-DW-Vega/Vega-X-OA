package com.olam.warehouse.vegax.inventorycameroon.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventorycameroon.R
import com.olam.warehouse.vegax.inventorycameroon.data.domain.model.InventoryLots
import com.olam.warehouse.vegax.inventorycameroon.data.domain.model.StorageLoc
import com.olam.warehouse.vegax.inventorycameroon.databinding.FragmentVegaCameroonInventoryListBinding
import com.olam.warehouse.vegax.inventorycameroon.utils.*
import kotlinx.android.synthetic.main.fragment_vega_cameroon_inventory_list.*
import kotlinx.android.synthetic.main.item_vega_cameroon_inventory_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCameroonInventoryDetailsFragment : BaseFragment() {
    private lateinit var binding: FragmentVegaCameroonInventoryListBinding
    private val vm: VegaCameroonInventoryViewModel by viewModel()
    override val layoutResourceId = R.layout.fragment_vega_cameroon_inventory_list
    private var callBack: CallBack? = null
    private lateinit var storage: StorageLoc
    private var isDescends: Boolean = true
    private var colorCode: Int = 0
    private val mSearchList = mutableListOf<InventoryLots>()
    private var materialList = mutableListOf<VegaMaterial>()
    private var lotList = arrayListOf<InventoryLots>()
    var filterData = ArrayList<InventoryLots>()

    private var materialFilter = ArrayList<String>()
    private var humidityFilter = ArrayList<String>()
    private var smokyFilter = ArrayList<String>()
    private var mouldFilter = ArrayList<String>()
    private var wasteFilter = ArrayList<String>()
    private var fullFilter = ArrayList<String>()

    interface CallBack {
        fun replaceFragment(moveFrag: String, bundle: Bundle)
        fun replaceFilterFragment(bundle: Bundle, fullFilter: ArrayList<String>)
    }

    companion object {
        fun newInstance(bundle: Bundle) = VegaCameroonInventoryDetailsFragment().putArgs {
            putBundle("BUNDLE_DATA", bundle)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("inventorycameroon/ui/VegaCameroonInventoryDetailsFragment")
            .title("Vega_Cameroon/Inventory").with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvInventoryHead, it, false)
        }
        val bundle = arguments?.getBundle("BUNDLE_DATA")
        storage = bundle?.getSerializable(INVENTORY_LOTS) as StorageLoc
        colorCode = bundle.getInt(INVENTORY_CODE)
        tv_sort.visible()
        tv_filter.visible()
        ivSortDownUp.visible()
        ivFilter.visible()
        vm.product.observe(viewLifecycleOwner, Observer {
            materialList = it as MutableList<VegaMaterial>
            stubData()
        })
        vm.getProducts()
        val bundle1 = Bundle().apply { putString("s", "") }
        binding.tvFilter.setOnClickListener { callBack?.replaceFragment("", bundle1) }
        binding.tvInventoryHead.text =
            storage.warehouseLocation.warehouse.warehouseName.plus(" - ")
                .plus(storage.warehouseLocation.procureLocationName)

        binding.tvSort.setOnClickListener {
            isDescends = !isDescends
            sortByWeight(if (filterData.size > 0) filterData else storage.inventory)
        }

        binding.tvFilter.setOnClickListener {
            moveToFilter()
        }
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.getConfigItems(UserRoles.INVENTORY.role)
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.BEAN_SMOKY.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> it.value?.split(",")
                            ?.let { it1 -> smokyFilter.addAll(it1) }
                    }
                }
                ConfigItems.B_MOIST.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> it.value?.split(",")
                            ?.let { it1 -> humidityFilter.addAll(it1) }
                    }
                }
                ConfigItems.MOULD.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> it.value?.split(",")
                            ?.let { it1 -> mouldFilter.addAll(it1) }
                    }
                }
                ConfigItems.WASTE.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> it.value?.split(",")
                            ?.let { it1 -> wasteFilter.addAll(it1) }
                    }
                }
            }
        }
    }

    private fun moveToFilter() {
        val bundle = Bundle()
        bundle.putStringArrayList(FILTER_MATERIAL, materialFilter)
        bundle.putStringArrayList(FILTER_SMOKY, smokyFilter)
        bundle.putStringArrayList(FILTER_HUMIDITY, humidityFilter)
        bundle.putStringArrayList(FILTER_MOULD, mouldFilter)
        bundle.putStringArrayList(FILTER_WASTE, wasteFilter)
        callBack?.replaceFilterFragment(bundle, fullFilter)
    }


    private fun sortByWeight(list: List<InventoryLots>) {
        val sortedList: List<InventoryLots>
        if (isDescends) {
            sortedList = list.sortedByDescending { wareHouse -> wareHouse.stockQty.toFloat() }
        } else {
            sortedList = list.sortedBy { wareHouse -> wareHouse.stockQty.toFloat() }
        }
        setUpAdapter(sortedList)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCameroonInventoryListBinding.inflate(layoutInflater)
        return binding.root
    }

    private fun stubData() {
        lotList = storage.inventory as ArrayList<InventoryLots>
        lotList.forEach { lot ->
            materialList.forEach { item ->
                if (lot.materialCode.contains(item.materialCode))
                    lot.materialName = item.materialName.toString()
            }
        }
        setUpAdapter(lotList)
    }

    private fun setUpAdapter(data: List<InventoryLots>) {
        val lotList = data as MutableList<InventoryLots>
        if (lotList.size > 0) binding.tvNoData.gone() else binding.tvNoData.visible()
        binding.rvInventory.setUp(lotList, R.layout.item_vega_cameroon_inventory_details, { it, pos ->
            when(it.smoky){
                "IR-AB/PR 003" -> tv_smoky_value.text = "Absence"
                "IR-AB/PR 004" -> tv_smoky_value.text = "Presence"
            }
            tv_humidity_value.text = it.beanMoisture
            tv_mould_value.text = it.mould
            tv_waste_value.text = it.waste
            tv_lot_name.text = it.lotId
            tv_weight_data.text = it.stockQty.plus(" ").plus("KG")
            divideView.background = ContextCompat.getDrawable(divideView.context, colorCode)
            materialList.forEach { item ->
                if (it.materialCode.contains(item.materialCode)) {
                    tv_material_name.text = item.materialName
                }
            }
        })
        val mat = lotList.map { it.materialName }.distinct()
        materialFilter.addAll(mat)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            search.isVisible = true
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(
                ContextCompat.getColor(
                    searchView.context,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            searchView.queryHint = getString(R.string.search_by_lots)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            when {
                                mSearchList.size > 0 -> setUpAdapter(mSearchList)
                                filterData.size > 0 -> setUpAdapter(filterData)
                                else -> setUpAdapter(storage.inventory)
                            }
                        } else {
                            mSearchList.clear()
                            when {
                                filterData.size > 0 -> {
                                    filterData.forEach { qtyWb ->
                                        newText?.let { text ->
                                            if (qtyWb.lotId.contains(text)) {
                                                mSearchList.add(qtyWb)
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    storage.inventory.forEach { qtyWb ->
                                        newText?.let { text ->
                                            if (qtyWb.lotId.contains(text)) {
                                                mSearchList.add(qtyWb)
                                            }
                                        }
                                    }
                                }
                            }
                            setUpAdapter(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    fun applyFilter(bundle: Bundle) {
        val materialList = bundle.getStringArrayList(FILTER_MATERIAL) ?: ArrayList()
        val humidityList = bundle.getStringArrayList(FILTER_HUMIDITY) ?: ArrayList()
        val smokyList = bundle.getStringArrayList(FILTER_SMOKY) ?: ArrayList()
        val mouldList = bundle.getStringArrayList(FILTER_MOULD) ?: ArrayList()
        val wasteList = bundle.getStringArrayList(FILTER_WASTE) ?: ArrayList()
        fullFilter.clear()
        filterData.clear()
        val filterData1 = ArrayList<InventoryLots>()
        val filterData2 = ArrayList<InventoryLots>()
        val filterData3 = ArrayList<InventoryLots>()
        val filterData4 = ArrayList<InventoryLots>()
        val filterData5 = ArrayList<InventoryLots>()
        fullFilter.addAll(materialList)
        fullFilter.addAll(humidityList)
        fullFilter.addAll(smokyList)
        fullFilter.addAll(mouldList)
        fullFilter.addAll(wasteList)

        lotList.forEach { lot ->
            materialList.forEach { item ->
                when {
                    item.contains(lot.materialName) -> {
                        filterData1.add(lot)
                    }
                }
            }

            humidityList.forEach { humidity ->
                if (lot.beanMoisture.isNullOrEmpty()) return@forEach
                val data = humidity.replace("%", "").replace("<H", "").replace("H", "")
                when {
                    data.contains("<=") -> {
                        val spdata = data.split("<=")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.beanMoisture.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() <= spdata[1].toDouble()
                                ) filterData2.add(
                                    lot
                                )
                            }
                            false -> {
                                if (lot.beanMoisture.replace(
                                        "%",
                                        ""
                                    ).trim()
                                        .toDouble() > spdata[0].toDouble() && lot.beanMoisture.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() <= spdata[1].toDouble()
                                ) filterData2.add(
                                    lot
                                )
                            }
                        }
                    }
                    data.contains(">") -> {
                        val spdata = data.split(">")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.beanMoisture.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() > spdata[1].toDouble()
                                ) filterData2.add(
                                    lot
                                )
                            }
                        }
                    }
                    data.contains("<") -> {
                        val spdata = data.split("<")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.beanMoisture.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() < spdata[1].toDouble()
                                ) filterData2.add(
                                    lot
                                )
                            }
                        }
                    }
                }
            }

            var smokyDesc = ""
            when(lot.smoky){
                "IR-AB/PR 003" -> smokyDesc = "Absence"
                "IR-AB/PR 004" -> smokyDesc = "Presence"
                else -> smokyDesc = ""
            }
            smokyList.forEach { smoky ->
                when {
                    smoky.contains(smokyDesc,true) -> {
                        filterData3.add(lot)
                    }
                }
            }



            mouldList.forEach { mould ->
                if (lot.mould.isNullOrEmpty()) return@forEach
                val data = mould.replace("%", "").replace("<M", "").replace("M", "")
                when {
                    data.contains("<=") -> {
                        val spdata = data.split("<=")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.mould.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() <= spdata[1].toDouble()
                                ) filterData4.add(
                                    lot
                                )
                            }
                            false -> {
                                if (lot.mould.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() > spdata[0].toDouble() && lot.mould.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() <= spdata[1].toDouble()
                                ) filterData4.add(
                                    lot
                                )
                            }
                        }
                    }
                    data.contains(">") -> {
                        val spdata = data.split(">")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.mould.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() > spdata[1].toDouble()
                                ) filterData4.add(
                                    lot
                                )
                            }
                        }
                    }
                    data.contains("<") -> {
                        val spdata = data.split("<")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.mould.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() < spdata[1].toDouble()
                                ) filterData4.add(
                                    lot
                                )
                            }
                        }
                    }
                }
            }

            wasteList.forEach { waste ->
                if (lot.waste.isNullOrEmpty()) return@forEach
                val data = waste.replace("%", "").replace("<W", "").replace("W", "")
                when {
                    data.contains("<=") -> {
                        val spdata = data.split("<=")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.waste.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() <= spdata[1].toDouble()
                                ) filterData5.add(
                                    lot
                                )
                            }
                            false -> {
                                if (lot.waste.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() > spdata[0].toDouble() && lot.waste.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() <= spdata[1].toDouble()
                                ) filterData5.add(
                                    lot
                                )
                            }
                        }
                    }
                    data.contains(">") -> {
                        val spdata = data.split(">")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.waste.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() > spdata[1].toDouble()
                                ) filterData5.add(
                                    lot
                                )
                            }
                        }
                    }
                    data.contains("<") -> {
                        val spdata = data.split("<")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.waste.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() < spdata[1].toDouble()
                                ) filterData5.add(
                                    lot
                                )
                            }
                        }
                    }
                }
            }

            lotList.forEach { lotItem ->
                if (filterData1.size == 0 && filterData2.size == 0 && filterData3.size == 0 && filterData4.size == 0 && filterData5.size == 0) return@forEach
                val lot = lotItem.lotId
                if ((materialList.size == 0 ||
                            (filterData1.map { it.lotId }.contains(lot) && filterData1.map { it.materialCode }
                                .contains(lotItem.materialCode)))
                    && (humidityList.size == 0 || filterData2.map { it.lotId }.contains(lot))
                    && (smokyList.size == 0 || filterData3.map { it.lotId }.contains(lot))
                    && (mouldList.size == 0 || filterData4.map { it.lotId }.contains(lot))
                    && (wasteList.size == 0 || filterData5.map { it.lotId }.contains(lot))
                )
                    filterData.add(lotItem)

            }

            val result = HashSet<InventoryLots>()
            result.addAll(filterData)
            filterData = result.toMutableList() as ArrayList<InventoryLots>
            setUpAdapter(if (fullFilter.size == 0) lotList else filterData)

        }
    }
}
