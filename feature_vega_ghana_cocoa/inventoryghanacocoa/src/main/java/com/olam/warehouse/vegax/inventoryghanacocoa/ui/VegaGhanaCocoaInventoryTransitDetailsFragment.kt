package com.olam.warehouse.vegax.inventoryghanacocoa.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventoryghanacocoa.R
import com.olam.warehouse.vegax.inventoryghanacocoa.data.domain.model.*
import com.olam.warehouse.vegax.inventoryghanacocoa.databinding.FragmentVegaGhanaCocoaInventoryListBinding
import com.olam.warehouse.vegax.inventoryghanacocoa.databinding.ItemVegaGhanaCocoaInventoryDetailsBinding
import com.olam.warehouse.vegax.inventoryghanacocoa.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGhanaCocoaInventoryTransitDetailsFragment : BaseFragment() {
    private lateinit var binding: FragmentVegaGhanaCocoaInventoryListBinding
    private val vm: VegaGhanaCocoaInventoryViewModel by viewModel()
    override val layoutResourceId = R.layout.fragment_vega_ghana_cocoa_inventory_list
    private var callBack: CallBack? = null
    private lateinit var storage: StorageLoc
    private var isDescends: Boolean = true
    private var colorCode: Int = 0
    private val mSearchList = mutableListOf<InventoryLots>()
    private var materialList = mutableListOf<VegaMaterial>()
    private var lotList = arrayListOf<InventoryLots>()
    private var scanlotList = arrayListOf<InventoryLots>()
    var filterData = ArrayList<InventoryLots>()
    private var lotId: String = ""
    private lateinit var bundle: Bundle



    private var materialFilter = ArrayList<String>()
    private var humidityFilter = ArrayList<String>()
    private var impurityFilter = ArrayList<String>()
    private var mouldFilter = ArrayList<String>()
    private var fullFilter = ArrayList<String>()

    interface CallBack {
        fun replaceFragment(moveFrag: String, bundle: Bundle)
        fun replaceFilterFragment(bundle: Bundle, fullFilter: ArrayList<String>)
    }

    companion object {
        fun newInstance(bundle: Bundle) = VegaGhanaCocoaInventoryDetailsFragment().putArgs {
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
        TrackHelper.track().screen("inventorysesame/ui/details/VegaNigeriaSesameInventoryDetailsFragment")
            .title("Inventory Ecuador").with(tracker)
        initUI()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (lotId.isNotEmpty()) menu.clear()
    }

    private fun initUI() {
        val bundle = arguments?.getBundle("BUNDLE_DATA")
        lotId = bundle?.getString("LOTID", "") ?: ""
        val sto = bundle?.getSerializable(INVENTORY_LOTS)
        if (sto != null) storage = bundle.getSerializable(INVENTORY_LOTS) as StorageLoc
        colorCode = bundle?.getInt(INVENTORY_CODE)!!

        if(lotId.isNotEmpty()){
            binding.tvInventoryHead.text = "LOT DETAILS"
            vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })
            vm.product.observe(viewLifecycleOwner, Observer {
                materialList = it as MutableList<VegaMaterial>
                stubData()
            })
            vm.getLotDetails(lotId, "", PreferenceHelper.get(Constants.WERKS, ""))
        }
        else {
            binding.tvSort.visible()
            binding.tvFilter.visible()
            binding.ivSortDownUp.visible()
            binding.ivFilter.visible()
            vm.product.observe(viewLifecycleOwner, Observer {
                materialList = it as MutableList<VegaMaterial>
                stubData()
            })
            vm.getProducts()
            val bundle1 = Bundle().apply { putString("s", "") }
            binding.tvFilter.setOnClickListener { callBack?.replaceFragment("", bundle1) }

            binding.tvInventoryHead.text =
                storage.warehouseLocation.warehouse.plant.plantName.plus(" - ")
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

    }

    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaGhanaInventoryStocks>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        scanlotList = prepareInventoryList(it1)
                        vm.getProducts()

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

    private fun prepareInventoryList(items: List<VegaGhanaInventoryStocks>): ArrayList<InventoryLots> {
        val inventoryList = arrayListOf<InventoryLots>()
        val qcList = listOf<InventoryQC>()
        items.forEach {
            val stock = InventoryLots(
                lotId = it.batchNumber,
                materialCode = it.materialCode.toString(),
                materialName = it.materialName.toString(),
                admixture =  it.materialQuality.qualityParams.ZNG_ADMIXTURE,
                stockQty = it.weight.toString(),
                warehouseLocation = WarehouseLocation(warehouse = Warehouse(Plant(CountryDetail()))),
                inventoryQC = qcList
            )
            inventoryList.add(stock)
        }
        return inventoryList
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.ADMIXTURE.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> it.value?.split(",")
                            ?.let { it1 -> humidityFilter.addAll(it1) }
                    }
                }
                ConfigItems.TOTAL_IMPURITY.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> it.value?.split(",")
                            ?.let { it1 -> impurityFilter.addAll(it1) }
                    }
                }
                ConfigItems.BEAN_HUMIDITY.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> it.value?.split(",")
                            ?.let { it1 -> humidityFilter.addAll(it1) }
                    }
                }
                ConfigItems.BEAN_MOULD.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> it.value?.split(",")?.let { it1 -> mouldFilter.addAll(it1) }
                    }
                }
            }
        }
    }

    private fun moveToFilter() {
        val bundle = Bundle()
        bundle.putStringArrayList(FILTER_MATERIAL, materialFilter)
        bundle.putStringArrayList(FILTER_IMPURITY, impurityFilter)
        bundle.putStringArrayList(FILTER_HUMIDITY, humidityFilter)
        bundle.putStringArrayList(FILTER_MOULD, mouldFilter)
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
        binding = FragmentVegaGhanaCocoaInventoryListBinding.inflate(layoutInflater)
        return binding.root
    }

    private fun stubData() {
        if (lotId.isEmpty()) lotList = storage.inventory  as ArrayList<InventoryLots>
        val data = if (lotId.isEmpty()) storage.inventory else scanlotList
        data.forEach { lot ->
            materialList.forEach { item ->
                if (lot.materialCode.contains(item.materialCode))
                    lot.materialName = item.materialName.toString()
            }
        }
        val lotItems =
            data.sortedByDescending { DateUtils.getTimeStamp(if (!it.grnDate.isNullOrEmpty()) it.grnDate else "0") } as MutableList<InventoryLots>
        setUpAdapter(lotItems)
    }

    private fun setUpAdapter(data: List<InventoryLots>) {
        val lotList = data as MutableList<InventoryLots>
        if (lotList.size > 0) binding.tvNoData.gone() else binding.tvNoData.visible()
        binding.rvInventory.setUpAdapter(
            lotList,
            R.layout.item_vega_ghana_cocoa_inventory_details,
            ItemVegaGhanaCocoaInventoryDetailsBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvAdmixtureValue.text = DateUtils.getFormatedDate(it.grnDate)
                bindItem.tvLotName.text = it.lotId
                bindItem.tvWeightData.text = it.stockQty.plus(" ").plus(it.uom)
                if (colorCode != 0)
                    bindItem.divideView.background =
                        ContextCompat.getDrawable(bindItem.divideView.context, colorCode)
                materialList.forEach { item ->
                    if (it.materialCode.contains(item.materialCode)) {
                        bindItem.tvMaterialName.text = item.materialName
                    }
                }
            })
        val mat = lotList.map { it.materialName }.distinct()
        materialFilter.addAll(mat)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        try {
            if(lotId.isEmpty()) {
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
            }
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    fun applyFilter(bundle: Bundle) {
        val materialList = bundle.getStringArrayList(FILTER_MATERIAL) ?: ArrayList()
        val admixtureList = bundle.getStringArrayList(FILTER_HUMIDITY) ?: ArrayList()
        val impurityList = bundle.getStringArrayList(FILTER_IMPURITY) ?: ArrayList()
        val mouldList = bundle.getStringArrayList(FILTER_MOULD) ?: ArrayList()
        fullFilter.clear()
        filterData.clear()
        val filterData1 = ArrayList<InventoryLots>()
        val filterData2 = ArrayList<InventoryLots>()
        val filterData3 = ArrayList<InventoryLots>()
        val filterData4 = ArrayList<InventoryLots>()
        fullFilter.addAll(materialList)
        fullFilter.addAll(admixtureList)
        fullFilter.addAll(impurityList)
        fullFilter.addAll(mouldList)

        lotList.forEach { lot ->
            materialList.forEach { item ->
                when {
                    item.contains(lot.materialName) -> {
                        filterData1.add(lot)
                    }
                }

            }

            admixtureList.forEach { admixture ->
                if (lot.admixture.isNullOrEmpty()) return@forEach
                val data = admixture.replace("%", "").replace("<A", "").replace("A", "")
                when {
                    data.contains("<=") -> {
                        val spdata = data.split("<=")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.admixture.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() <= spdata[1].toDouble()
                                ) filterData2.add(
                                    lot
                                )
                            }
                            false -> {
                                if (lot.admixture.replace(
                                        "%",
                                        ""
                                    ).trim()
                                        .toDouble() > spdata[0].toDouble() && lot.admixture.replace(
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
                                if (lot.admixture.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() > spdata[1].toDouble()
                                ) filterData2.add(
                                    lot
                                )
                            }
                            else -> {}
                        }
                    }
                    data.contains("<") -> {
                        val spdata = data.split("<")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.admixture.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() < spdata[1].toDouble()
                                ) filterData2.add(
                                    lot
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }

            impurityList.forEach { impurity ->
                if (lot.impurity.isNullOrEmpty()) return@forEach
                val data = impurity.replace("%", "").replace("<TI", "").replace("TI", "")
                when {
                    data.contains("<=") -> {
                        val spdata = data.split("<=")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.impurity.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() <= spdata[1].toDouble()
                                ) filterData3.add(
                                    lot
                                )
                            }
                            false -> {
                                if (lot.impurity.replace(
                                        "%",
                                        ""
                                    ).trim()
                                        .toDouble() > spdata[0].toDouble() && lot.impurity.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() <= spdata[1].toDouble()
                                ) filterData3.add(
                                    lot
                                )
                            }
                        }
                    }
                    data.contains(">") -> {
                        val spdata = data.split(">")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.impurity.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() > spdata[1].toDouble()
                                ) filterData3.add(
                                    lot
                                )
                            }
                            else -> {}
                        }
                    }
                    data.contains("<") -> {
                        val spdata = data.split("<")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.impurity.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() < spdata[1].toDouble()
                                ) filterData3.add(
                                    lot
                                )
                            }
                            else -> {}
                        }
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
                            else -> {}
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
                            else -> {}
                        }
                    }
                }
            }

            lotList.forEach { lotItem ->
                if (filterData1.size == 0 && filterData2.size == 0 && filterData3.size == 0 && filterData4.size == 0) return@forEach
                val lot = lotItem.lotId
                if ((materialList.size == 0 || (filterData1.map { it.lotId }
                        .contains(lot) && filterData1.map { it.materialCode }.contains(lotItem.materialCode))) && (admixtureList.size == 0 || filterData2.map { it.lotId }.contains(
                        lot
                    ))
                    && (impurityList.size == 0 || filterData3.map { it.lotId }
                        .contains(lot)) && (mouldList.size == 0 || filterData4.map { it.lotId }.contains(
                        lot
                    ))
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
