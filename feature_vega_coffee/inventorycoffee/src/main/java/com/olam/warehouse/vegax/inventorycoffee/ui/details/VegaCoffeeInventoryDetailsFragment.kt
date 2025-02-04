package com.olam.warehouse.vegax.inventorycoffee.ui.details

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.master.common.model.Warehouse
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils.getTimeStamp
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventorycoffee.R
import com.olam.warehouse.vegax.inventorycoffee.data.domain.model.*
import com.olam.warehouse.vegax.inventorycoffee.databinding.FragmentVegaCoffeeInventoryListBinding
import com.olam.warehouse.vegax.inventorycoffee.ui.VegaCoffeeInventoryViewModel
import com.olam.warehouse.vegax.inventorycoffee.utils.CallBack
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCoffeeInventoryDetailsFragment : BaseFragment() {
    private lateinit var binding: FragmentVegaCoffeeInventoryListBinding
    private val vm: VegaCoffeeInventoryViewModel by viewModel()
    override val layoutResourceId = R.layout.fragment_vega_coffee_inventory_list
    private var callBack: CallBack? = null
    private lateinit var bundle: Bundle
    private lateinit var storage: CoffeeStorageLoc
    private var isDescends: Boolean = false
    private var colorCode: Int = 0
    private val mSearchList = mutableListOf<CoffeeInventory>()
    private var materialList = mutableListOf<VegaMaterial>()
    var fullFilter = ArrayList<String>()
    val filterData = ArrayList<CoffeeInventory>()
    var adapterData = listOf<CoffeeInventory>()
    private var lotId: String = ""
    var inventoryList = arrayListOf<CoffeeInventory>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments ?: bundle
        val sto = bundle.getSerializable("Lots")
        if (sto != null) storage = bundle.getSerializable("Lots") as CoffeeStorageLoc
        lotId = bundle.getString("LOTID", "")
        colorCode = bundle.getInt("code")
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (lotId.isNotEmpty()) menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("inventorycoffee.ui.details/VegaCoffeeInventoryDetailsFragment").title("Inventory")
            .with(tracker)
        //initUI()
        binding.tvSearchLot.gone()
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })
        if (lotId.isNotEmpty()) vm.getLotDetails(lotId, "", PreferenceHelper.get(Constants.WERKS, ""))

        vm.product.observe(viewLifecycleOwner, Observer {
            materialList = it as MutableList<VegaMaterial>
            stubData()
        })
        if (lotId.isEmpty()) vm.getProducts()
        val bundle = Bundle().apply { putString("s", "") }
        binding.tvFilter.setOnClickListener {
            callBack?.replaceFragment("", bundle)
        }
        if (lotId.isEmpty()) {
            binding.tvTruckNo.text =
                storage.warehouseLocation.warehouse.warehouseName + " - " + storage.warehouseLocation.procureLocationName
        } else {
            binding.tvTruckNo.text = getString(R.string.lot_details)
            binding.cvLotDetails.gone()
        }


        binding.tvSort.setOnClickListener {
            isDescends = !isDescends
            sortByWeight(adapterData)
        }

        binding.tvFilter.setOnClickListener {
            val kor = storage.inventory.map { it.kor } as ArrayList<String>
            val origin = storage.inventory.map { it.origin } as ArrayList<String>
            val material = storage.inventory.map { it.materialCode.substring(6) }.distinct() as ArrayList<String>
            val materialName = arrayListOf<String>()
            materialList.forEach { item ->
                if (material.contains(item.materialCode)) materialName.add(item.materialName.toString())
            }
            moveToFilter(
                materialName,
                kor,
                origin
            )
        }
        totalCountOfLots()
        binding.tvTotalCount.setOnClickListener { totalCountOfLots() }
    }

    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaCoffeInventoryStocks>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        inventoryList = prepareInventoryList(it1)
                        vm.getProducts()

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

    private fun prepareInventoryList(items: List<VegaCoffeInventoryStocks>): ArrayList<CoffeeInventory> {
        val inventoryList = arrayListOf<CoffeeInventory>()
        val qcList = listOf<CoffeeInventoryQC>()
        items.forEach {
            val stock = CoffeeInventory(
                lotId = it.batchNumber,
                materialCode = it.materialCode.toString(),
                materialName = it.materialName,
                storageLocationCode = it.storageLocationCode,
                stockQty = it.weight.toString(),
                warehouseLocation = CoffeeWarehouseLocation(warehouse = Warehouse()),
                inventoryQC = qcList,
                CI_MATIERE_ETRANGERE_CAFE = it.materialQuality.qualityParams.CI_MATIERE_ETRANGERE_CAFE,
                CI_GRAINS_NOIRS_CAFE = it.materialQuality.qualityParams.CI_GRAINS_NOIRS_CAFE,
                CI_BRISSURE_CAFE = it.materialQuality.qualityParams.CI_BRISSURE_CAFE
            )
            inventoryList.add(stock)
        }
        return inventoryList
    }


    private fun moveToFilter(materialName: ArrayList<String>, korList: ArrayList<String>, orgins: ArrayList<String>) {
        changeBtnBacground()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.tvFilter.compoundDrawableTintList = context?.let { it1 ->
                ContextCompat.getColor(it1, com.olam.warehouse.presentation.R.color.white)
            }?.let { it2 -> ColorStateList.valueOf(it2) }
        }
        binding.tvFilter.setTextColor(
            ContextCompat.getColor(
                binding.tvTotalCount.context,
                com.olam.warehouse.presentation.R.color.white
            )
        )
        binding.tvFilter.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.layout_border_with_down)
        val bundle = Bundle().apply {
            putStringArrayList("kors", korList)
            putStringArrayList("origins", orgins)
            putStringArrayList("materials", materialName)
        }
        callBack?.replaceFragment("", bundle, fullFilter)
    }


    private fun sortByWeight(list: List<CoffeeInventory>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.tvSort.compoundDrawableTintList = context?.let { it1 ->
                ContextCompat.getColor(it1, com.olam.warehouse.presentation.R.color.white)
            }?.let { it2 -> ColorStateList.valueOf(it2) }
        }
        binding.tvSort.setTextColor(
            ContextCompat.getColor(
                binding.tvSort.context,
                com.olam.warehouse.presentation.R.color.white
            )
        )
        changeBtnBacground()
        binding.tvSort.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.layout_border_with_down)
        val sortedList: List<CoffeeInventory>
        val lotItems =
            list.sortedByDescending { getTimeStamp(if (!it.grnDate.isNullOrEmpty()) it.grnDate else "0") } as MutableList<CoffeeInventory>
        if (isDescends) {
            //sortedList = lotItems.sortedByDescending { wareHouse -> wareHouse.stockQty.toFloat() }
            sortedList = lotItems.asReversed()
        } else {
            //sortedList = lotItems.sortedBy { wareHouse -> wareHouse.stockQty.toFloat() }
            sortedList = lotItems
        }
        setAdapter(sortedList)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCoffeeInventoryListBinding.inflate(layoutInflater)
        //stubData()
        return binding.root
    }

    private fun stubData() {
        if (lotId.isEmpty()) adapterData = storage.inventory
        val data = if (lotId.isEmpty()) storage.inventory else inventoryList
        val lotItems =
            data.sortedByDescending { getTimeStamp(if (!it.grnDate.isNullOrEmpty()) it.grnDate else "0") } as MutableList<CoffeeInventory>
        setAdapter(lotItems)
    }

    private fun setAdapter(data: List<CoffeeInventory>) {
        updateHeader(data)
        binding.rvWeighBridgeId.layoutManager = LinearLayoutManager(activity).apply { LinearLayoutManager.VERTICAL }
        binding.rvWeighBridgeId.adapter =
            VegaCoffeeInventoryDetailsAdapter(
                data,
                colorCode,
                materialList
            )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(
                ContextCompat.getColor(
                    searchView.context,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            searchView.queryHint = getString(com.olam.warehouse.presentation.R.string.search_by_lot)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setAdapter(adapterData)
                        } else {
                            mSearchList.clear()
                            adapterData.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.lotId.contains(text, true)) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            setAdapter(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    fun applyFilterValues(
        listKor: ArrayList<String>,
        listOrigin: ArrayList<String>,
        listMaterials: java.util.ArrayList<String>
    ) {

        val fullFilter = ArrayList<String>()
        fullFilter.addAll(listKor)
        fullFilter.addAll(listOrigin)
        fullFilter.addAll(listMaterials)
        this.fullFilter = fullFilter
        filterData.clear()

        storage.inventory.forEach { inven ->
            fullFilter.forEach { korOrigin ->
                var mName = ""
                val materialItems = materialList.filter { inven.materialCode.contains(it.materialCode) }
                if (materialItems.size > 0) mName = materialItems[0].materialName.toString()
                if (korOrigin.contains(inven.kor.toString()) || korOrigin.contains(inven.origin.toString()) || korOrigin.equals(
                        mName
                    )
                ) {
                        filterData.add(inven)
                    }
            }
        }


        /* for (kor in listKor) {
             for (items in storage.inventory) {
                 if (kor.equals(items.kor)) {
                     filterData.add(items)
                 }
             }
         }
         for (origin in listOrigin) {
             for (items in storage.inventory) {
                 if (origin.equals(items.origin)) {
                     filterData.add(items)
                 }
             }
         }*/

        val result = HashSet<CoffeeInventory>()
        result.addAll(filterData)
        adapterData = filterData
        setAdapter(if (filterData.size == 0) storage.inventory else result.toList())
    }

    private fun updateHeader(lotList1: List<CoffeeInventory>) {
        binding.tvTotalCount.text = getString(R.string.lots_count).plus(lotList1.size.toString())
        binding.tvTotalWeight.text =
            getString(R.string.total_wt).plus(lotList1.sumByDouble { it.stockQty.toDouble() }.formatThreeDigits())
                .plus(" KG")
    }

    private fun totalCountOfLots() {
        changeBtnBacground()
        binding.tvTotalCount.setTextColor(
            ContextCompat.getColor(
                binding.tvTotalCount.context,
                com.olam.warehouse.presentation.R.color.white
            )
        )
        binding.tvTotalCount.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.layout_border_with_down)
        if (lotId.isEmpty()) setAdapter(storage.inventory)
        filterData.clear()
        fullFilter.clear()
    }

    private fun changeBtnBacground() {
        binding.tvTotalCount.setTextColor(
            ContextCompat.getColor(
                binding.tvTotalCount.context,
                com.olam.warehouse.presentation.R.color.dark_marun
            )
        )
        binding.tvSort.setTextColor(
            ContextCompat.getColor(
                binding.tvTotalCount.context,
                com.olam.warehouse.presentation.R.color.dark_marun
            )
        )
        binding.tvFilter.setTextColor(
            ContextCompat.getColor(
                binding.tvTotalCount.context,
                com.olam.warehouse.presentation.R.color.dark_marun
            )
        )
        binding.tvTotalCount.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.layout_border_without_down)
        binding.tvSort.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.layout_border_without_down)
        binding.tvFilter.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.layout_border_without_down)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.tvSort.compoundDrawableTintList = context?.let { it1 ->
                ContextCompat.getColor(it1, com.olam.warehouse.presentation.R.color.dark_marun)
            }?.let { it2 -> ColorStateList.valueOf(it2) }
            binding.tvFilter.compoundDrawableTintList = context?.let { it1 ->
                ContextCompat.getColor(it1, com.olam.warehouse.presentation.R.color.dark_marun)
            }?.let { it2 -> ColorStateList.valueOf(it2) }
        }
    }
}
