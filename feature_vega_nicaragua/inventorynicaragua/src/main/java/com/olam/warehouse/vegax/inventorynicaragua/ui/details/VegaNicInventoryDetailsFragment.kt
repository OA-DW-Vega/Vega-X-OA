package com.olam.warehouse.vegax.inventorynicaragua.ui.details

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import com.olam.warehouse.login.databinding.ItemPrintLotCardPreviewBinding
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmap
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.common.model.Warehouse
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.Constants.SELECTED_STOCKS_LIST
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventorynicaragua.R
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.*
import com.olam.warehouse.vegax.inventorynicaragua.databinding.FragmentVegaNicInventoryListBinding
import com.olam.warehouse.vegax.inventorynicaragua.ui.VegaNicInventoryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 12/14/2020.
 */
class VegaNicInventoryDetailsFragment : BaseFragment() {
    private lateinit var binding: FragmentVegaNicInventoryListBinding
    private val vm: VegaNicInventoryViewModel by viewModel()
    override val layoutResourceId = R.layout.fragment_vega_nic_inventory_filter
    private var callBack: CallBack? = null
    private lateinit var bundle: Bundle
    private var storage: ArrayList<NicStorageLoc> = arrayListOf()
    private var isDescends: Boolean = false
    private var colorCode: Int = 0
    private val mSearchList = arrayListOf<NicInventory>()
    private var materialList = arrayListOf<VegaMaterial>()
    var fullFilter = ArrayList<String>()
    val filterData = arrayListOf<NicInventory>()
    var adapterData = arrayListOf<NicInventory>()
    private var lotId: String = ""
    var inventoryList = arrayListOf<NicInventory>()
    private var tallyPrintKeys = ArrayList<String>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    interface CallBack {
        fun replaceFragment(type: String, data: Any)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNicInventoryListBinding.inflate(layoutInflater)
        //stubData()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments ?: bundle
        storage = bundle.getParcelableArrayList<NicStorageLoc>("Lots") ?: ArrayList()
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
        TrackHelper.track().screen("inventorycoffee.ui.details/VegaNicInventoryDetailsFragment").title("Inventory")
            .with(tracker)
        //initUI()
        binding.tvSearchLot.gone()
        binding.clPrintLotCard.visible()
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })
        if (lotId.isNotEmpty()) {
            vm.getLotDetails(lotId, "", PreferenceHelper.get(Constants.WERKS, ""))
            binding.tvLotList.gone()
        }

        vm.product.observe(viewLifecycleOwner, Observer {
            materialList = it as ArrayList<VegaMaterial>
            stubData()
        })
        if (lotId.isEmpty()) vm.getProducts()

        if (lotId.isEmpty()) {
            binding.tvTruckNo.text = getString(R.string.lot_details)
//                    storage.map { it.warehouseLocation }[0].warehouse.warehouseName.plus(" - ").plus(storage.map { it.warehouseLocation }[0].procureLocationName)
        } else {
            binding.tvTruckNo.text = getString(R.string.lot_details)
            binding.cvLotDetails.gone()
        }


        binding.tvSort.setOnClickListener {
            isDescends = !isDescends
            sortByWeight(if (filterData.size == 0) adapterData else filterData)
        }

        binding.tvFilter.setOnClickListener {
            moveToFilter()
        }
        totalCountOfLots()
        binding.tvTotalCount.setOnClickListener { totalCountOfLots() }
        binding.tvPrintLotCard.setOnClickListener { createLotCardBitMap() }
    }

    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaNicInventoryStocks>>>) {
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

    private fun prepareInventoryList(items: List<VegaNicInventoryStocks>): ArrayList<NicInventory> {
        val inventoryList = arrayListOf<NicInventory>()
        val qcList = listOf<NicInventoryQC>()
        items.forEach {
            val stock = NicInventory(
                lotId = it.batchNumber,
                materialCode = it.materialCode.toString(),
                materialName = it.materialName,
                qualityParameters = it.materialQuality.qualityParameters,
                storageLocationCode = it.storageLocationCode,
                stockQty = it.weight.toString(),
                openQuantity = it.weight.toString(),
                warehouseLocation = NicWarehouseLocation(warehouse = Warehouse()),
                inventoryQC = qcList,
                uom = it.unitOfMeasure,
                gradeDesc = it.materialQuality.qualityParams.NIPOSITI,
                certification = it.materialQuality.qualityParams.NIFG0014,
                CI_MATIERE_ETRANGERE_CAFE = it.materialQuality.qualityParams.CI_MATIERE_ETRANGERE_CAFE,
                CI_GRAINS_NOIRS_CAFE = it.materialQuality.qualityParams.CI_GRAINS_NOIRS_CAFE,
                CI_BRISSURE_CAFE = it.materialQuality.qualityParams.CI_BRISSURE_CAFE
            )
            inventoryList.add(stock)
        }
        return inventoryList
    }


    private fun moveToFilter() {
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
            putStringArrayList("Selected_List", fullFilter)
            putParcelableArrayList("Storage_List", storage)
            putBoolean("IS_STORAGE", false)
            putParcelableArrayList("Material_List", materialList)
        }
        callBack?.replaceFragment("Filter", bundle)
    }


    private fun sortByWeight(list: List<NicInventory>) {
        if (list.size == 0) return
        changeBtnBacground()
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

        binding.tvSort.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.layout_border_with_down)
        val sortedList: List<NicInventory>
        val lotItems =
                list.sortedByDescending { DateUtils.getTimeStamp(if (!it.grnDate.isNullOrEmpty()) it.grnDate else "0") } as MutableList<NicInventory>
        if (isDescends) {
            //sortedList = lotItems.sortedByDescending { wareHouse -> wareHouse.stockQty.toFloat() }
            sortedList = lotItems.asReversed()
        } else {
            //sortedList = lotItems.sortedBy { wareHouse -> wareHouse.stockQty.toFloat() }
            sortedList = lotItems
        }
        setAdapter(sortedList)
    }

    private fun stubData() {
        if (lotId.isEmpty()) storage.forEach { adapterData.addAll(it.inventory) } else adapterData = inventoryList
        val data = if (lotId.isEmpty()) adapterData else inventoryList
        val lotItems =
            data.sortedByDescending { DateUtils.getTimeStamp(if (it.grnDate.isNotEmpty()) it.grnDate else "00000000") } as MutableList<NicInventory>
        setAdapter(lotItems)
    }

    private fun setAdapter(data: List<NicInventory>) {
        updateHeader(data)
        binding.rvWeighBridgeId.layoutManager = LinearLayoutManager(activity).apply { LinearLayoutManager.VERTICAL }
        binding.rvWeighBridgeId.adapter =
            VegaNicInventoryDetailsAdapter(
                data,
                lotId,
                materialList, { addPrintLotCard(it) }, { moveQualityParams(it) })
    }

    private fun moveQualityParams(data: NicInventory) {
        val bundle = Bundle().apply {
            putParcelable(SELECTED_STOCKS_LIST, data)
        }
        callBack?.replaceFragment("Quality_Params", bundle)
    }

    private fun addPrintLotCard(nicPrintInventory: MutableList<NicInventory>) {
        val nicInventoryList = nicPrintInventory.filter { it.isChecked }
        if (nicInventoryList.isEmpty()) {
            enableAndDisablePrintLotCard(false)
        } else {
            val nicInventory = nicPrintInventory.single { it.isChecked }
            adapterData.single {
                it.lotId.equals(nicInventory.lotId) && it.storageLocationCode.equals(
                    nicInventory.storageLocationCode
                )
            }
                .apply { isChecked = nicInventory.isChecked }
            val isPrintEnable = adapterData.any { it.isChecked }
            enableAndDisablePrintLotCard(isPrintEnable)
            setAdapter(nicPrintInventory)
        }
    }

    private fun enableAndDisablePrintLotCard(printEnable: Boolean) {
        binding.tvPrintLotCard.isEnabled = printEnable
        if (printEnable) {
            binding.tvPrintLotCard.setTextColor(
                ContextCompat.getColor(
                    binding.tvPrintLotCard.context,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.tvPrintLotCard.compoundDrawableTintList = context?.let { it1 ->
                    ContextCompat.getColor(
                        it1,
                        com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                    )
                }?.let { it2 -> ColorStateList.valueOf(it2) }
            }
        } else {
            binding.tvPrintLotCard.setTextColor(
                    ContextCompat.getColor(
                            binding.tvPrintLotCard.context,
                            com.olam.warehouse.presentation.R.color.dark_marun
                    )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.tvPrintLotCard.compoundDrawableTintList = context?.let { it1 ->
                    ContextCompat.getColor(it1, com.olam.warehouse.presentation.R.color.dark_marun)
                }?.let { it2 -> ColorStateList.valueOf(it2) }
            }
        }
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
                            when {
                                mSearchList.size > 0 -> setAdapter(mSearchList)
                                filterData.size > 0 -> setAdapter(filterData)
                                else -> setAdapter(adapterData)
                            }
                        } else {
                            when {
                                filterData.size > 0 -> {
                                    filterData.forEach { qtyWb ->
                                        newText?.let { text ->
                                            if (qtyWb.lotId.contains(text, true)) {
                                                mSearchList.add(qtyWb)
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    adapterData.forEach { qtyWb ->
                                        newText?.let { text ->
                                            if (qtyWb.lotId.contains(text, true)) {
                                                mSearchList.add(qtyWb)
                                            }
                                        }
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

    fun applyFilterValues(bundle: Bundle) {
        filterData.clear()
        val filtersItem = bundle.getStringArrayList("Full_Filter") ?: arrayListOf()
        val material = bundle.getStringArrayList("Material") ?: arrayListOf()
        val certification = bundle.getStringArrayList("Certification") ?: arrayListOf()
        val qualityGrade = bundle.getStringArrayList("QualityGrade") ?: arrayListOf()
        val storage = bundle.getStringArrayList("Storage") ?: arrayListOf()
        if (filtersItem.isEmpty() == true) {
            this.fullFilter.clear()
            setAdapter(adapterData)
            return
        }
        val fullFilter = ArrayList<String>()
        fullFilter.addAll(filtersItem)
        this.fullFilter = fullFilter

        var filterData1 = ArrayList<NicInventory>()
        var filterData2 = ArrayList<NicInventory>()
        var filterData3 = ArrayList<NicInventory>()
        var filterData4 = ArrayList<NicInventory>()

        adapterData.forEach { inven ->
            material.forEach { item ->
                when {
                    item.contains(inven.materialCode) -> {
                        filterData1.add(inven)
                    }
                }

            }
            qualityGrade.forEach { item ->
                when {
                    item.contains(inven.gradeDesc.toString()) -> {
                        filterData2.add(inven)
                    }
                }

            }
            certification.forEach { item ->
                when {
                    item.contains(inven.certification.toString()) -> {
                        filterData3.add(inven)
                    }
                }

            }
            storage.forEach { item ->
                when {
                    item.contains(inven.storageLocationCode.toString()) -> {
                        filterData4.add(inven)
                    }
                }

            }
        }

        adapterData.forEach { lotItem ->
            if (filterData1.size == 0 && filterData2.size == 0 && filterData3.size == 0 && filterData4.size == 0) return@forEach
            val lot = lotItem.lotId
            if ((material.size == 0 || filterData1.map { it.lotId }
                    .contains(lot)) && (qualityGrade.size == 0 || filterData2.map { it.lotId }.contains(
                    lot
                )) && (certification.size == 0 || filterData3.map { it.lotId }.contains(
                    lot
                ))
                && (storage.size == 0 || filterData4.map { it.lotId }
                    .contains(lot))
            )
                filterData.add(lotItem)

        }

        val result = HashSet<NicInventory>()
        result.addAll(filterData)
        setAdapter(if (filterData.size == 0) adapterData else result.toList())
    }

    private fun updateHeader(lotList1: List<NicInventory>) {
        binding.tvTotalCount.text = getString(R.string.lots_count).plus(lotList1.size.toString())
        binding.tvTotalWeight.text =
            getString(R.string.total_wt).plus(lotList1.sumByDouble { it.stockQty?.toDouble() ?: 0.0 }
                .formatThreeDigits())
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
        if (lotId.isEmpty()) setAdapter(adapterData)
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

    private fun createLotCardBitMap() {
        tallyPrintKeys.clear()
        showCustomLoading()
        DoAsync {
            adapterData.filter { it.isChecked }.forEachIndexed { index, item ->
                val view = LayoutInflater.from(context).inflate(com.olam.warehouse.login.R.layout.item_print_lot_card_preview, null)
                val viewBinder = ItemPrintLotCardPreviewBinding.bind(view)
                viewBinder.ivPreview.setImageBitmap(getBitmap(item.lotId))
                viewBinder.tvLotValue.text = item.lotId
                viewBinder.tvMaterialValue.text = getMaterialName(item.materialCode)
                if (item.gradeDesc?.isNotEmpty() == true) {
                    viewBinder.tvGradeValue.text = item.gradeDesc
                    viewBinder.tvGrade.visible()
                    viewBinder.tvGradeValue.visible()
                }
                if (item.certification?.isNotEmpty() == true) {
                    viewBinder.tvCertificateValue.text = item.certification
                    viewBinder.tvCertificate.visible()
                    viewBinder.tvCertificateValue.visible()
                }
                viewBinder.tvWeightValue.text = item.openQuantity.plus(" ").plus(item.uom)

                //bitmapValue.put(index, getBitmapFromView(parent))
                tallyPrintKeys.add(
                        bitmapToString(
                                getBitmapFromView(
                                        view, Color.WHITE
                                )
                        )
                )
            }
            runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                startActivity(Intent(context, WifiMainActivity::class.java))
            }
        }.execute()
    }

    override fun onResume() {
        super.onResume()
        hideCustomLoading()
    }

    private fun getMaterialName(materialCode: String): String {
        val material = materialList.filter { materialCode.contains(it.materialCode) }
        return if (material.size > 0) material[0].materialName.toString() else materialCode
    }
}
