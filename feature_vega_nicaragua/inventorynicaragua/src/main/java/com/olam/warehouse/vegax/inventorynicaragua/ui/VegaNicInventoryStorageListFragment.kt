package com.olam.warehouse.vegax.inventorynicaragua.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventory.ui.dialog.VegaNicInventorySyncDetailsDailog
import com.olam.warehouse.vegax.inventorynicaragua.R
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.MaterialDetails
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.NicStorageLoc
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.NicSyncStatus
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.VegaNicInventoryAndSyncModel
import com.olam.warehouse.vegax.inventorynicaragua.databinding.FragmentVegaNicInventoryListBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 12/11/2020.
 */
class VegaNicInventoryStorageListFragment : BaseFragment() {
    private lateinit var binding: FragmentVegaNicInventoryListBinding
    override val layoutResourceId = R.layout.fragment_vega_nic_inventory_list
    private var isDescends: Boolean = true
    private var mSearchList = mutableListOf<NicStorageLoc>()
    private var wareHouseList = ArrayList<NicStorageLoc>()
    private var syncStatusList = ArrayList<NicSyncStatus>()
    private var callback: CallBack? = null
    private var expandPosition = 0
    var fullFilter = ArrayList<String>()
    private val vm: VegaNicInventoryViewModel by viewModel()
    private var materialList = arrayListOf<VegaMaterial>()

    interface CallBack {
        fun replaceFragment(fragment: String, data: Any)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as? CallBack
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("inventorynicaragua/ui/VegaNicInventoryStorageListFragment").title("Inventory")
                .with(tracker)

        binding.tvTruckNo.text = resources.getString(R.string.inventory_list)
        binding.tvFilterStorage.setOnClickListener {
            moveToFilter(fullFilter)
        }
        vm.product.observe(viewLifecycleOwner, Observer {
            materialList = it as ArrayList<VegaMaterial>
            vm.getInventoryList()
        })
        vm.getProducts()
        vm.inventoryModelList.observe(viewLifecycleOwner, Observer { updateUI(it) })
        binding.cvLotDetails.gone()
        binding.clStorageHeader.visible()
        binding.tvViewLots.visible()
        binding.tvSortByWeight.setOnClickListener {
            isDescends = !isDescends
            sortByWeight(wareHouseList)
        }

        binding.tvSearchLot.setOnClickListener {
            MaterialDialog(requireContext()).show {
                title(R.string.search_lot_number)
                var lotNo = ""
                input(
                    waitForPositiveButton = false,
                    hint = getString(R.string.enter_lot_no)
                ) { dialog, text ->
                    val inputField = dialog.getInputField()
                    val isValid = text.isNotEmpty()
                    lotNo = text.toString()
                    inputField.error = if (isValid) null else getString(R.string.enter_lot_no)
                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
                }
                UIUtils.getMetirialCustomView(
                    this,
                    getString(com.olam.warehouse.presentation.R.string.submit),
                    getString(R.string.cancel),
                    {
                        val bundle = Bundle().apply {
                            putString("LOTID", lotNo)
                        }
                        // callback?.replaceFragment("details", bundle)
                    },
                    { dismiss() })
            }
        }
        binding.tvViewLots.setOnClickListener { moveToLotList() }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        /* val gson = Gson()
         val vegaInventoryAndSyncModel = gson.fromJson(data, VegaNicInventoryAndSyncModel::class.java)
         if (vegaInventoryAndSyncModel != null) {
             wareHouseList = vegaInventoryAndSyncModel.inventories as ArrayList<NicStorageLoc>
             syncStatusList = vegaInventoryAndSyncModel.syncStatus as ArrayList<NicSyncStatus>
         }*/
        binding = FragmentVegaNicInventoryListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
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
            searchView.queryHint = getString(R.string.search_wh)
            getAllFilterValues()
            //setAdapter(wareHouseList)
            //syncDetails()
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setAdapter(wareHouseList)
                        } else {
                            mSearchList.clear()
                            wareHouseList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.warehouseLocation.warehouse.warehouseName!!.contains(text, true)) {
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

    companion object {
        const val SEARCH_HINT_TEXT = "Search warehouse"
    }

    private fun setAdapter(data: List<NicStorageLoc>) {
        binding.rvWeighBridgeId.layoutManager = LinearLayoutManager(activity).apply { LinearLayoutManager.VERTICAL }
        binding.rvWeighBridgeId.adapter = VegaNicInventoryParentAdapter(data, materialList) { moveLotDetails(it) }
    }

    private fun moveLotDetails(storage: NicStorageLoc) {
        wareHouseList.single { it.procureLocationCode.equals(storage.procureLocationCode) }.apply { isChecked = storage.isChecked }
    }

    private fun moveToLotList() {
        if (wareHouseList.any { it.isChecked }) {
            val selectedStorageItems = wareHouseList.filter { it.isChecked } as ArrayList
            val bundle = Bundle().apply {
                putParcelableArrayList("Lots", selectedStorageItems)
            }
            callback?.replaceFragment("details", bundle)
        } else {
            activity?.toast("Please select atleast one item")
        }
    }

    private fun getAllFilterValues() {
        /*for (sub in item.storageLoc) {
            for (kor in sub.averageKors)
                kors.add(kor)
        }

        for (sub in item.storageLoc) {
            for (origin in sub.origins)
                origins.add(origin)
        }*/
    }

    var kors = ArrayList<String>()
    var origins = ArrayList<String>()


    /*override fun navigateToDetails(storage: NicStorageLoc, colorCode: Int) {
        val bundle = Bundle().apply {
            putSerializable("Lots", storage)
            putInt("code", colorCode)
        }
        callback?.replaceFragment("details", bundle)
    }*/

    private fun syncDetails() {
        val bottomDialog = VegaNicInventorySyncDetailsDailog.newInstance(syncStatusList)
        activity?.supportFragmentManager?.let { it1 -> bottomDialog.show(it1, "BottomSheet") }
    }


    fun applyFilterValues(filterItems: ArrayList<String>) {

        if (filterItems.isEmpty()) {
            this.fullFilter.clear()
            setAdapter(wareHouseList)
            return
        }
        val fullFilter = ArrayList<String>()
        fullFilter.addAll(filterItems)
        this.fullFilter = fullFilter

        val filterData = ArrayList<NicStorageLoc>()

        wareHouseList.forEach { sto ->
            if (fullFilter.contains(sto.procureLocationCode))
                filterData.add(sto)
        }

        /*model.forEachIndexed { index, mod ->
            fullFilter.forEach { korOrigin ->
                mod.storageLoc.forEach { storage ->
                    storage.averageKors.let {
                        if (storage.averageKors.contains(korOrigin) || storage.origin?.contains(korOrigin)!!) {
                            filterData.add(storage)
                        }
                    }
                }
            }
            if (filterData.isNotEmpty()) {
                @Suppress("UNCHECKED_CAST")
                val storageFilter = filterData.clone() as ArrayList<NicStorageLoc>
                val modd = model[index]
                modd.storageLoc = storageFilter
                filterWarehouse.add(modd)
                filterData.clear()
            }
        }

        val result = HashSet<NicStorageLoc>()
        result.addAll(filterWarehouse)
        val warehouse = result.toList()

        for (item in warehouse) {
            item.storageLoc = removeDuplicates(item)
        }*/
        setAdapter(filterData)
    }

    private fun removeDuplicates(warehouse: NicStorageLoc): List<NicStorageLoc> {
        val result = HashSet<NicStorageLoc>()
//        result.addAll(warehouse.storageLoc)
        return result.toList()
    }

    private fun moveToFilter(fullFilter: ArrayList<String>) {
        val bundle = Bundle().apply {
            putStringArrayList("Selected_List", fullFilter)
            putBoolean("IS_STORAGE", true)
            putParcelableArrayList("Storage_List", wareHouseList)
            putParcelableArrayList("Material_List", materialList)
        }
        callback?.replaceFragment("Filter", bundle)
    }

    private fun sortByWeight(list: List<NicStorageLoc>) {
        val sortedList: List<NicStorageLoc>
        if (isDescends) {
            sortedList = list.sortedByDescending { wareHouse ->
                wareHouse.materialDetails.sumByDouble { if (it.totalWeight.isNotEmpty()) it.totalWeight.toDouble() else 0.0 }
                    .toFloat()
            }
        } else {
            sortedList = list.sortedBy { wareHouse ->
                wareHouse.materialDetails.sumByDouble { if (it.totalWeight.isNotEmpty()) it.totalWeight.toDouble() else 0.0 }
                    .toFloat()
            }
        }
        setAdapter(sortedList)
    }

    private fun updateUI(data: Resource<GenericReqAndResp<VegaNicInventoryAndSyncModel>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    val storageList = arrayListOf<NicStorageLoc>()
                    it.data?.data?.inventories?.forEach {
                        it.storageLoc.forEach { sto ->
                            sto.procureLocationCode = sto.warehouseLocation.procureLocationCode
                            sto.procureLocationName = sto.warehouseLocation.procureLocationName
                            val matList = arrayListOf<MaterialDetails>()
                            sto.inventory.forEach { inven ->
                                val matItem = MaterialDetails()
                                matItem.materialCode = inven.materialCode
                                matItem.qualityGrade = inven.gradeDesc.toString()
                                matItem.certification = inven.certification.toString()
                                matItem.totalWeight = inven.openQuantity ?: ""
                                matItem.uom = inven.uom ?: ""
                                matList.add(matItem)
                            }
                            sto.materialDetails = matList

                        }
                        storageList.addAll(it.storageLoc)
                    }
                    wareHouseList.addAll(storageList)
                    syncStatusList = it.data?.data?.syncStatus as ArrayList<NicSyncStatus>
                    getAllFilterValues()
                    setAdapter(wareHouseList)
                    syncDetails()
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {

                }

            }
        }
    }
}
