package com.olam.warehouse.vegax.inventorycocoa.ui

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants.SCANNED_ID
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventorycocoa.R
import com.olam.warehouse.vegax.inventorycocoa.data.domain.model.SyncStatus
import com.olam.warehouse.vegax.inventorycocoa.data.domain.model.VegaCocoaInventoryLotHead
import com.olam.warehouse.vegax.inventorycocoa.data.domain.model.VegaCocoaInventoryLots
import com.olam.warehouse.vegax.inventorycocoa.databinding.FragmentVegaCocoaInventoryLotsBinding
import com.olam.warehouse.vegax.inventorycocoa.ui.dialog.VegaCocoaInventorySyncDetailsDailog
import com.olam.warehouse.vegax.inventorycocoa.utils.*
import kotlinx.android.synthetic.main.item_vega_cocoa_inventory_lot.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 5/21/2020.
 */
class VegaCocoaInventroyLotsFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_cocoa_inventory_lots
    private lateinit var binding: FragmentVegaCocoaInventoryLotsBinding
    private val vm: VegaCocoaInventoryViewModel by viewModel()
    private var callBack: CallBack? = null
    private var lotList = arrayListOf<VegaCocoaInventoryLots>()
    private val mSearchList = arrayListOf<VegaCocoaInventoryLots>()
    private var materialList = mutableListOf<VegaMaterial>()
    private var lotId: String? = ""
    private var isThirdParty: Boolean? = false
    private var isDescends: Boolean = false
    var filterData = ArrayList<VegaCocoaInventoryLots>()
    private var syncStatusList = ArrayList<SyncStatus>()
    private var isMovedToQualityFragment: Boolean? = false

    private var materialFilter = ArrayList<String>()
    private var stoLocFilter = ArrayList<String>()
    private var moistFilter = ArrayList<String>()
    private var beanFilter = ArrayList<String>()
    private var ffaFilter = ArrayList<String>()
    private var fatFilter = ArrayList<String>()
    private var fullFilter = ArrayList<String>()

    interface CallBack {
        fun replaceQualityFragment(it: VegaCocoaInventoryLots)
        fun replaceFilterFragment(bundle: Bundle, fullFilter: ArrayList<String>)
    }

    companion object {
        fun newInstance(lotId: String?, isThirdParty: Boolean) = VegaCocoaInventroyLotsFragment().putArgs {
            putString(SCANNED_ID, lotId)
            putBoolean(IS_THIRD_PARTY, isThirdParty)
        }

        const val SEARCH_HINT_TEXT = "Search Lots Item"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (lotId?.isNotEmpty()!!) menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCocoaInventoryLotsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("inventorycocoa/ui/VegaCocoaInventroyLotsFragment").title("Inventory Cocoa")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        lotId = arguments?.getString(SCANNED_ID)
        isThirdParty = arguments?.getBoolean(IS_THIRD_PARTY)
        vm.scanLot.observe(viewLifecycleOwner, Observer { updateUIScannedLotData(it) })
        vm.lotList.observe(viewLifecycleOwner, Observer { updateUILotData(it) })
        vm.product.observe(viewLifecycleOwner, Observer {
            materialList = it as MutableList<VegaMaterial>
            vm.getInventoryList()
        })

        when {
            lotId?.isNotEmpty()!! -> {
                binding.tvInventoryHead.text = getString(R.string.lot_details)
                binding.tvLotNo.text = getString(R.string.lot_no).plus(lotId)
                binding.tvLotNo.visible()
                binding.cvLotDetails.gone()
                binding.tvLotListLbl.gone()
                lotId?.let { id -> vm.getScanLotDetail(id) }
            }
            else -> {
                binding.tvLotNo.gone()
                binding.cvLotDetails.visible()
                vm.getProducts()
            }
        }
        totalCountOfLots()
        binding.tvTotalCount.setOnClickListener { totalCountOfLots() }
        binding.tvSort.setOnClickListener { sortItems() }
        binding.tvFilter.setOnClickListener { moveFilterFragments() }
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.getConfigItems(UserRoles.INVENTORY.role)

    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.BEAN_COUNT.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> it.value?.split(",")?.let { it1 -> beanFilter.addAll(it1) }
                    }
                }
                ConfigItems.BEAN_MOISTURE.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> it.value?.split(",")?.let { it1 -> moistFilter.addAll(it1) }
                    }
                }
                ConfigItems.BEAN_FFA.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> it.value?.split(",")?.let { it1 -> ffaFilter.addAll(it1) }
                    }
                }
                ConfigItems.BEAN_FAT.item -> {
                    when {
                        it.applicable?.contains("Y") ?: false -> it.value?.split(",")
                            ?.let { it1 -> fatFilter.addAll(it1) }
                    }
                }
            }
        }
    }

    private fun syncDetails() {
        val bottomDialog = VegaCocoaInventorySyncDetailsDailog.newInstance(syncStatusList)
        activity?.supportFragmentManager?.let { it1 -> bottomDialog.show(it1, "BottomSheet") }
    }

    private fun updateUILotData(response: Resource<GenericReqAndResp<VegaCocoaInventoryLotHead>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data.let { it1 ->
                        it1?.stocks?.let { it2 -> lotList = it2 as ArrayList<VegaCocoaInventoryLots> }
                        syncStatusList = it.data?.data?.syncStatus as ArrayList<SyncStatus>
                    }
                    lotList.forEach { lot ->
                        materialList.forEach { item ->
                            if (lot.materialCode.contains(item.materialCode))
                                lot.materialName = item.materialName.toString()
                        }
                    }
                    lotList = if (isThirdParty!!) {
                        lotList.filter { it.thirdPartyFlag.equals("X") }
                            .toMutableList() as ArrayList<VegaCocoaInventoryLots>
                    } else {
                        lotList.filter { it.thirdPartyFlag.equals(null) }
                            .toMutableList() as ArrayList<VegaCocoaInventoryLots>
                    }
                    setUpAdapter(lotList)
                    syncDetails()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun updateUIScannedLotData(response: Resource<GenericReqAndResp<List<VegaCocoaInventoryLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data.let { it1 ->
                        it1?.let { it2 -> lotList = it2 as ArrayList<VegaCocoaInventoryLots> }
                    }
                    setUpAdapter(lotList)
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun updateHeader(lotList1: List<VegaCocoaInventoryLots>) {
        binding.tvTotalCount.text = getString(R.string.total_count1).plus(lotList1.size.toString())
        binding.tvTotalWeight.text =
            getString(R.string.total_wt).plus(lotList1.sumByDouble { it.weight.toDouble() }.formatThreeDigits())
                .plus(" ")
                .plus(if (lotList1.size > 0) lotList1[0].unitOfMeasure ?: "MT" else "")

        val mat = lotList1.map { it.materialName.toString() }.distinct()
        materialFilter.addAll(mat)
        val sto = lotList1.map { it.storageLocationCode.toString() }.distinct()
        stoLocFilter.addAll(sto)
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
        setUpAdapter(lotList)
        filterData.clear()
        fullFilter.clear()
    }

    private fun moveFilterFragments() {
        val bundle = Bundle()
        bundle.putStringArrayList(FILTER_MATERIAL, materialFilter)
        bundle.putStringArrayList(FILTER_LOCATION, stoLocFilter)
        bundle.putStringArrayList(FILTER_BEAN, beanFilter)
        bundle.putStringArrayList(FILTER_MOIST, moistFilter)
        bundle.putStringArrayList(FILTER_FFA, ffaFilter)
        bundle.putStringArrayList(FILTER_FAT, fatFilter)
        callBack?.replaceFilterFragment(bundle, fullFilter)
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
    }

    private fun sortItems() {
        isDescends = !isDescends
        changeBtnBacground()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.tvSort.compoundDrawableTintList = context?.let { it1 ->
                ContextCompat.getColor(it1, com.olam.warehouse.presentation.R.color.white)
            }?.let { it2 -> ColorStateList.valueOf(it2) }
        }
        binding.tvSort.setTextColor(
            ContextCompat.getColor(
                binding.tvTotalCount.context,
                com.olam.warehouse.presentation.R.color.white
            )
        )
        binding.tvSort.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.layout_border_with_down)
        sortByWeight(if (filterData.size == 0) lotList else filterData)
    }

    private fun sortByWeight(lotList: ArrayList<VegaCocoaInventoryLots>) {
        val sortedList = arrayListOf<VegaCocoaInventoryLots>()
        if (isDescends) {
            sortedList.addAll(lotList.sortedByDescending { lot -> lot.weight.toFloat() })
        } else {
            sortedList.addAll(lotList.sortedBy { lot -> lot.weight.toFloat() })
        }
        setUpAdapter(sortedList)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
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
                        if (newText?.isEmpty() == true && isMovedToQualityFragment!!) {
                            when {
                                mSearchList.size > 0 -> setUpAdapter(mSearchList)
                                filterData.size > 0 -> setUpAdapter(filterData)
                                else -> setUpAdapter(lotList)
                            }
                            //setUpAdapter(if (filterData.size > 0) filterData else lotList)
                        } else {
                            mSearchList.clear()
                            when {
                                filterData.size > 0 -> {
                                    filterData.forEach { qtyWb ->
                                        newText?.let { text ->
                                            if (qtyWb.batchNumber.contains(text)) {
                                                mSearchList.add(qtyWb)
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    lotList.forEach { qtyWb ->
                                        newText?.let { text ->
                                            if (qtyWb.batchNumber.contains(text)) {
                                                mSearchList.add(qtyWb)
                                            }
                                        }
                                    }
                                }
                            }

                            setUpAdapter(mSearchList)
                            isMovedToQualityFragment = false
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun setUpAdapter(lotList: ArrayList<VegaCocoaInventoryLots>) {
        updateHeader(lotList)
        if (lotList.size > 0) binding.tvNoData.gone() else binding.tvNoData.visible()
        binding.rvLotList.setUp(lotList, R.layout.item_vega_cocoa_inventory_lot, { it, pos ->
            when (it.materialQuality) {
                null -> {
                    tvQualityDetails.gone()
                }
                else -> {
                    tvQualityDetails.visible()
                    tvMoisture.text = it.materialQuality.qualityParams.B_MOIST
                    tvBeanCount.text = it.materialQuality.qualityParams.B_BEANCOUNT
                    tvFFA.text = it.materialQuality.qualityParams.B_FFA1
                    tvFAT.text = it.materialQuality.qualityParams.B_FAT1
                }
            }
            tvLotNo.text = it.batchNumber
            tvStLocValue.text = it.storageLocationCode
            tv_material_name.text = it.materialName
            tv_weight_data.text = it.weight.plus(" ").plus(it.unitOfMeasure ?: "MT")
            tvQualityDetails.setOnClickListener { view ->
                isMovedToQualityFragment = true
                callBack?.replaceQualityFragment(it)
            }

        })
    }

    fun applyFilter(bundle: Bundle) {
        val materialList = bundle.getStringArrayList(FILTER_MATERIAL) ?: ArrayList()
        val locationList = bundle.getStringArrayList(FILTER_LOCATION) ?: ArrayList()
        val moistList = bundle.getStringArrayList(FILTER_MOIST) ?: ArrayList()
        val beanList = bundle.getStringArrayList(FILTER_BEAN) ?: ArrayList()
        val ffaList = bundle.getStringArrayList(FILTER_FFA) ?: ArrayList()
        val fatList = bundle.getStringArrayList(FILTER_FAT) ?: ArrayList()
        fullFilter.clear()
        filterData.clear()
        var filterData0 = ArrayList<VegaCocoaInventoryLots>()
        var filterData1 = ArrayList<VegaCocoaInventoryLots>()
        var filterData2 = ArrayList<VegaCocoaInventoryLots>()
        var filterData3 = ArrayList<VegaCocoaInventoryLots>()
        var filterData4 = ArrayList<VegaCocoaInventoryLots>()
        var filterData5 = ArrayList<VegaCocoaInventoryLots>()
        fullFilter.addAll(materialList)
        fullFilter.addAll(locationList)
        fullFilter.addAll(moistList)
        fullFilter.addAll(beanList)
        fullFilter.addAll(ffaList)
        fullFilter.addAll(fatList)

        lotList.forEach { lot ->
            locationList.forEach { item ->
                when {
                    item.contains(lot.storageLocationCode) -> {
                        filterData0.add(lot)
                    }
                }

            }

            materialList.forEach { item ->
                when {
                    item.contains(lot.materialName ?: "null") -> {
                        filterData1.add(lot)
                    }
                }

            }

            moistList.forEach { moist ->
                if (lot.materialQuality == null || lot.materialQuality.qualityParams.B_MOIST.isEmpty()) return@forEach
                val dta = moist.replace("%", "")
                when {
                    dta.contains(">") -> {
                        if (dta.replace(">", "").contains(".")) {
                            if (lot.materialQuality.qualityParams.B_MOIST.replace(
                                    "%",
                                    ""
                                ).trim().toDouble() > dta.replace(">", "").toDouble()
                            )
                                filterData2.add(lot)
                        } else {
                            if (lot.materialQuality.qualityParams.B_MOIST.replace(
                                    "%",
                                    ""
                                ).trim().toDouble() > dta.replace(">", "").toDouble()
                            )
                                filterData2.add(lot)
                        }
                    }
                    dta.contains("<") -> {
                        if (lot.materialQuality.qualityParams.B_MOIST.replace(
                                "%",
                                ""
                            ).trim().toDouble() < dta.replace("<", "").toDouble()
                        )
                            filterData2.add(lot)
                    }
                    else -> {
                        if (lot.materialQuality.qualityParams.B_MOIST.startsWith(if (dta.contains("(")) dta.split("(")[0].trim() else dta))
                            filterData2.add(lot)
                    }
                }
            }

            beanList.forEach { bean ->
                if (lot.materialQuality == null || lot.materialQuality.qualityParams.B_BEANCOUNT.isEmpty()) return@forEach
                val data = bean.replace("<BC", "").replace("BC", "")
                when {
                    data.contains("<=") -> {
                        val spdata = data.split("<=")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.materialQuality.qualityParams.B_BEANCOUNT.toDouble() <= spdata[1].toDouble())
                                    filterData3.add(lot)
                            }
                            false -> {
                                if (lot.materialQuality.qualityParams.B_BEANCOUNT.toDouble() > spdata[0].toDouble() && lot.materialQuality.qualityParams.B_BEANCOUNT.toDouble() <= spdata[1].toDouble())
                                    filterData3.add(lot)
                            }
                        }
                    }
                    data.contains(">") -> {
                        val spdata = data.split(">")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.materialQuality.qualityParams.B_BEANCOUNT.toDouble() > spdata[1].toDouble()) filterData3.add(
                                    lot
                                )
                            }
                        }
                    }
                    data.contains("<") -> {
                        val spdata = data.split("<")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.materialQuality.qualityParams.B_BEANCOUNT.toDouble() < spdata[1].toDouble()) filterData3.add(
                                    lot
                                )
                            }
                        }
                    }
                }
            }

            ffaList.forEach { ffa ->
                if (lot.materialQuality == null || lot.materialQuality.qualityParams.B_FFA1.isEmpty()) return@forEach
                val data = ffa.replace("%", "").replace("<FFA", "").replace("FFA", "")
                when {
                    data.contains("<=") -> {
                        val spdata = data.split("<=")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.materialQuality.qualityParams.B_FFA1.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() <= spdata[1].toDouble()
                                ) filterData4.add(
                                    lot
                                )
                            }
                            false -> {
                                if (lot.materialQuality.qualityParams.B_FFA1.replace(
                                        "%",
                                        ""
                                    ).trim()
                                        .toDouble() > spdata[0].toDouble() && lot.materialQuality.qualityParams.B_FFA1.replace(
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
                                if (lot.materialQuality.qualityParams.B_FFA1.replace(
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
                                if (lot.materialQuality.qualityParams.B_FFA1.replace(
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

            fatList.forEach { fat ->
                if (lot.materialQuality == null || lot.materialQuality.qualityParams.B_FAT1.isEmpty()) return@forEach
                val data = fat.replace("%", "").replace("<FAT", "").replace("FAT", "")
                when {
                    data.contains("<=") -> {
                        val spdata = data.split("<=")
                        when (spdata[0].trim() == "") {
                            true -> {
                                if (lot.materialQuality.qualityParams.B_FAT1.replace(
                                        "%",
                                        ""
                                    ).trim().toDouble() <= spdata[1].toDouble()
                                ) filterData5.add(
                                    lot
                                )
                            }
                            false -> {
                                if (lot.materialQuality.qualityParams.B_FAT1.replace(
                                        "%",
                                        ""
                                    ).trim()
                                        .toDouble() > spdata[0].toDouble() && lot.materialQuality.qualityParams.B_FAT1.replace(
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
                                if (lot.materialQuality.qualityParams.B_FAT1.replace(
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
                                if (lot.materialQuality.qualityParams.B_FFA1.replace(
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
        }

        lotList.forEach { lotItem ->
            if (filterData0.size == 0 && filterData1.size == 0 && filterData2.size == 0 && filterData3.size == 0 && filterData4.size == 0 && filterData5.size == 0) return@forEach
            val lot = lotItem.batchNumber
            if ((locationList.size == 0 || filterData0.map { it.batchNumber }
                    .contains(lot)) && (materialList.size == 0 || filterData1.map { it.batchNumber }.contains(
                    lot
                )) && (moistList.size == 0 || filterData2.map { it.batchNumber }.contains(
                    lot
                ))
                && (beanList.size == 0 || filterData3.map { it.batchNumber }
                    .contains(lot)) && (ffaList.size == 0 || filterData4.map { it.batchNumber }.contains(
                    lot
                )) && (fatList.size == 0 || filterData5.map { it.batchNumber }.contains(
                    lot
                ))
            )
                filterData.add(lotItem)

        }

        val result = HashSet<VegaCocoaInventoryLots>()
        result.addAll(filterData)
        filterData = result.toMutableList() as ArrayList<VegaCocoaInventoryLots>
        if(locationList.isNotEmpty()) {
            val locMap = locationList.map { it }
            val filter = filterData.filter { locMap.contains(it.storageLocationCode) } as ArrayList
            setUpAdapter(if (fullFilter.size == 0) lotList else filter)
        } else {
            setUpAdapter(if (fullFilter.size == 0) lotList else filterData)
        }
        //setUpAdapter(if (fullFilter.size == 0) lotList else filterData)
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
