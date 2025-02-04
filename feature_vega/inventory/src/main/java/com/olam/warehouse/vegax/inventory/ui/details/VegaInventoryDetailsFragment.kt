package com.olam.warehouse.vegax.inventory.ui.details

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventory.R
import com.olam.warehouse.vegax.inventory.data.domain.model.Inventory
import com.olam.warehouse.vegax.inventory.data.domain.model.StorageLoc
import com.olam.warehouse.vegax.inventory.databinding.FragmentVegaInventoryListBinding
import com.olam.warehouse.vegax.inventory.ui.CallBack
import com.olam.warehouse.vegax.inventory.ui.VegaInventoryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaInventoryDetailsFragment : BaseFragment() {
    private lateinit var binding: FragmentVegaInventoryListBinding
    private val vm: VegaInventoryViewModel by viewModel()
    override val layoutResourceId = R.layout.fragment_vega_inventory_list
    private var callBack: CallBack? = null
    private lateinit var bundle: Bundle
    private lateinit var storage: StorageLoc
    private var isDescends: Boolean = true
    private var colorCode: Int = 0
    private val mSearchList = mutableListOf<Inventory>()
    private var materialList = mutableListOf<VegaMaterial>()
    var fullFilter = ArrayList<String>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments ?: bundle
        storage = bundle.getSerializable("Lots") as StorageLoc
        colorCode = bundle.getInt("code")
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("inventory/ui/details/VegaInventoryDetailsFragment")
            .title("Inventory").with(tracker)
        //initUI()
        context?.let {
            getActionBtnChangedView(binding.tvSyncDetails, it, false)
            getActionBtnChangedView(binding.tvTruckNo, it, false)
        }
        vm.product.observe(viewLifecycleOwner, Observer {
            materialList = it as MutableList<VegaMaterial>
            stubData()
        })
        vm.getProducts()
        val bundle = Bundle().apply { putString("s", "") }
        binding.tvFilter.setOnClickListener { callBack?.replaceFragment("", bundle) }
        binding.tvTruckNo.text =
            storage.warehouseLocation.warehouse.warehouseName + " - " + storage.warehouseLocation.procureLocationName

        binding.tvSort.setOnClickListener {
            isDescends = !isDescends
            sortByWeight(storage.inventory)
        }

        binding.tvFilter.setOnClickListener {
            val kor = storage.inventory.map { it.kor } as ArrayList<String>
            val origin = storage.inventory.map { it.origin } as ArrayList<String>
            moveToFilter(
                kor,
                origin
            )
        }
    }


    private fun moveToFilter(korList: ArrayList<String>, orgins: ArrayList<String>) {
        val bundle = Bundle().apply {
            putStringArrayList("kors", korList)
            putStringArrayList("origins", orgins)
        }
        callBack?.replaceFragment("", bundle, fullFilter)
    }


    private fun sortByWeight(list: List<Inventory>) {
        val sortedList: List<Inventory>
        if (isDescends) {
            sortedList = list.sortedByDescending { wareHouse -> wareHouse.stockQty.toFloat() }
        } else {
            sortedList = list.sortedBy { wareHouse -> wareHouse.stockQty.toFloat() }
        }
        setAdapter(sortedList)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaInventoryListBinding.inflate(layoutInflater)
        //stubData()
        return binding.root
    }

    private fun stubData() {
        setAdapter(storage.inventory)
    }

    private fun setAdapter(data: List<Inventory>) {
        binding.rvWeighBridgeId.layoutManager = LinearLayoutManager(activity).apply { LinearLayoutManager.VERTICAL }
        binding.rvWeighBridgeId.adapter =
            VegaInventoryDetailsAdapter(
                data,
                colorCode,
                materialList
            )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        //activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
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
            searchView.queryHint = "Search by Lots"
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setAdapter(storage.inventory)
                        } else {
                            mSearchList.clear()
                            storage.inventory.forEach { qtyWb ->
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

    fun applyFilterValues(listKor: ArrayList<String>, listOrigin: ArrayList<String>) {

        val fullFilter = ArrayList<String>()
        fullFilter.addAll(listKor)
        fullFilter.addAll(listOrigin)
        this.fullFilter = fullFilter

        val filterData = ArrayList<Inventory>()

        storage.inventory.forEach { inven ->
            fullFilter.forEach { korOrigin ->
                inven.kor?.let {
                    if (korOrigin.contains(inven.kor) || korOrigin.contains(inven.origin.toString())) {
                        filterData.add(inven)
                    }
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

        val result = HashSet<Inventory>()
        result.addAll(filterData)
        setAdapter(if (filterData.size == 0) storage.inventory else result.toList())
    }
}
