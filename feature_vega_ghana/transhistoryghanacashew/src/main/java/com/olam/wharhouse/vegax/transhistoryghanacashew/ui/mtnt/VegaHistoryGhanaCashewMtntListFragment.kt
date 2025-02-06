package com.olam.warehouse.vegax.transhistoryghanacashew.ui.mtnt

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCashewMtntHistoryTransactions
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCashewMtntHistoryTranxResponse
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.LocaleHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.wharhouse.vegax.transhistoryghanacashew.R
import com.olam.wharhouse.vegax.transhistoryghanacashew.databinding.FragmentHistoryTransactionsGhanaCashewMtntListBinding
import com.olam.wharhouse.vegax.transhistoryghanacashew.databinding.ItemHistoryTransactionsGhanaCashewMtntBinding
import com.olam.wharhouse.vegax.transhistoryghanacashew.ui.VegaGhanaCashewTransHisViewModel
import com.olam.wharhouse.vegax.transhistoryghanacashew.ui.VegaHistoryTransactionsReplaceFragmentCallback
import com.olam.wharhouse.vegax.transhistoryghanacashew.utils.HISTORY_DATA_MTNT
import com.olam.wharhouse.vegax.transhistoryghanacashew.utils.HISTORY_TRANSACTIONS_LOT_MTNT
import com.olam.wharhouse.vegax.transhistoryghanacashew.utils.allPlants
import com.olam.wharhouse.vegax.transhistoryghanacashew.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VegaHistoryGhanaCashewMtntListFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_history_transactions_ghana_cashew_mtnt_list
    private lateinit var binding: FragmentHistoryTransactionsGhanaCashewMtntListBinding
    private var callBack: VegaHistoryTransactionsReplaceFragmentCallback? = null
    private val vm: VegaGhanaCashewTransHisViewModel by viewModel()

    private var history = mutableListOf<VegaGhanaCashewMtntHistoryTransactions>()
    private var filteredHistory = mutableListOf<VegaGhanaCashewMtntHistoryTransactions>()
    private var historyData = VegaGhanaCashewMtntHistoryTransactions()
    private val mSearchList = mutableListOf<VegaGhanaCashewMtntHistoryTransactions>()
    private var vegaWbIds = listOf<VegaGhanaCashewMtntHistoryTransactions>()
    private var vegaWbsortedIds = listOf<VegaGhanaCashewMtntHistoryTransactions>()
    private var wareHouseList = mutableListOf<String>()
    private var sendingWareHouseList = mutableListOf<VegaCustomStLocation>()
    private var storageLocationList: ArrayList<String> = ArrayList()
    private var storage: String? = ""
    private var toDate: String? = ""
    private var fromDate: String? = ""
    private var supplierList = mutableListOf<VegaVendor>()

    companion object {
        fun newInstance(
            historyTransactions: VegaGhanaCashewMtntHistoryTransactions
        ) = VegaHistoryGhanaCashewMtntListFragment().putArgs {
            putParcelable(HISTORY_DATA_MTNT, historyTransactions)
        }

        const val SEARCH_HINT_TEXT = "Search"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaHistoryTransactionsReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentHistoryTransactionsGhanaCashewMtntListBinding.inflate(inflater)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
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
                            setUpAdapter(if (filteredHistory.isEmpty()) history else filteredHistory)
                        } else {
                            mSearchList.clear()
                            history.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.deliveryNumber?.contains(text) == true ||
                                        DateUtils.formatDateGhana(qtyWb.postingDate.toString())
                                            .contains(text) ||
                                        qtyWb.wbId?.contains(text) == true ||
                                        qtyWb.quantity?.contains(text) == true ||
                                        qtyWb.materialCode?.contains(text) == true ||
                                        qtyWb.wayBillNumber?.contains(text) == true
                                    ) {
                                        mSearchList.add(qtyWb)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("historytransactions/ui/VegaHistoryTransactionsMtntListFragment")
            .title("History Transactions - MTNT").with(tracker)
        initUI()
    }

    private fun initUI() {
        historyData = arguments?.getParcelable(HISTORY_DATA_MTNT)!!
        /*vm.historyTransactionsList.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        vm.getHistoryTransactions()*/
        /*vm.custonLocation.observe(viewLifecycleOwner, Observer {
            sendingWareHouseList = it.toMutableList()
            storageLocationList = sendingWareHouseList.map { it.procureLocationCode } as ArrayList<String>
        })
        vm.getCustomLocations()*/
        binding.ivSortDownUp.setOnClickListener {
            if (history.isNotEmpty()) {
                val data = history
                history = data.asReversed()
                /*vegaWbIds.let { history = it as MutableList<VegaGhanaCocoaMtntHistoryTransactions> }
                history = history.asReversed()
                vegaWbIds = history*/
                setUpAdapter(history)
            }
        }

        binding.tvFromDate.text = DateUtils.getUTCDateTimeCameroon(
            System.currentTimeMillis().toString(),
            App.getAppContext()
        )
        binding.tvToDate.text = DateUtils.getUTCDateTimeCameroon(
            System.currentTimeMillis().toString(),
            App.getAppContext()
        )
        vm.getSuppliers()
        vm.supplier.observe(viewLifecycleOwner, Observer {
            supplierList = it.toMutableList()
            vm.getHistoryTranxMtnt(
                DateUtils.formatDateGHCocoa(binding.tvToDate.text.toString()),
                getPlantDetails().plantId,
                DateUtils.formatDateGHCocoa(binding.tvFromDate.text.toString())
            )

        })
        binding.tvFromDate.setOnClickListener {
            getDatePickerDialog(
                binding.tvFromDate.text.toString(),
                "From"
            )
        }
        binding.tvToDate.setOnClickListener {
            if (!binding.tvFromDate.text.toString().isEmpty())
                getDatePickerDialog(binding.tvToDate.text.toString(), "To")
            else
                Toast.makeText(context, "Please enter From date", Toast.LENGTH_SHORT).show()
        }

        /*binding.tvWareHouse.setOnClickListener {
            showWarehouseListDialog(storageLocationList)
            binding.btnGo.isEnabled = true
        }*/

        binding.btnGo.setOnClickListener {
            if (!(binding.tvFromDate.text.toString().isEmpty() || binding.tvToDate.text.toString()
                    .isEmpty())
            ) {
                /*if(binding.tvWareHouse.text.toString().isNotEmpty()){*/
                vm.getHistoryTranxMtnt(
                    DateUtils.formatDateGHCocoa(binding.tvToDate.text.toString()),
                    getPlantDetails().plantId,
                    DateUtils.formatDateGHCocoa(binding.tvFromDate.text.toString())
                )
                fromDate = DateUtils.formatDateGHCocoa(binding.tvFromDate.text.toString())
                toDate = DateUtils.formatDateGHCocoa(binding.tvToDate.text.toString())
                vm.historyTranxMtnt.observe(
                    viewLifecycleOwner,
                    Observer { updateUIWithOnlineData(it) })
                //storage = binding.tvWareHouse.text.toString().trim()
                /*} else{
                    Toast.makeText(context,"Please select a storage location", Toast.LENGTH_SHORT).show()
                }*/
            } else {
                Toast.makeText(context, "Please enter Date", Toast.LENGTH_SHORT).show()
            }
        }

        vm.historyTranxMtnt.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        fromDate = DateUtils.formatDateGHCocoa(binding.tvFromDate.text.toString())
        toDate = DateUtils.formatDateGHCocoa(binding.tvToDate.text.toString())


    }

    private fun getDatePickerDialog(date: String, label: String) {
        val cal = Calendar.getInstance()
        if (date != "") {
            val dateTxt = date.split("/")
            cal.set(dateTxt[2].toInt(), dateTxt[0].toInt() - 1, dateTxt[1].toInt())
        }
        val dateFormat = "MM/dd/yyyy"
        val utc = "UTC"

        context?.let {
            val datePicker = DatePickerDialog(
                it,
                com.olam.warehouse.presentation.R.style.DatePickerTheme,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val sdf = SimpleDateFormat(dateFormat, LocaleHelper.getLocale(it))
                    sdf.timeZone = TimeZone.getTimeZone(utc)
                    when (label) {
                        "From" -> {
                            binding.tvFromDate.text = sdf.format(cal.time)
                            binding.tvToDate.text = ""
                        }
                        "To" ->
                            binding.tvToDate.text = sdf.format(cal.time)
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            //datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.datePicker.maxDate = System.currentTimeMillis()
            when (label) {

                "To" -> {
                    var fromDate = binding.tvFromDate.text.toString()
                    var maxToDate = DateUtils.addingDaysToDate(fromDate, 30)
                    val sdformat = SimpleDateFormat("MM/dd/yyyy")
                    val d1 = sdformat.parse(fromDate)
                    datePicker.datePicker.minDate = d1.time
                    val currentDate = sdformat.parse(
                        DateUtils.getUTCDateTimeCameroon(
                            System.currentTimeMillis().toString(),
                            App.getAppContext()
                        )
                    )
                    val d2 = sdformat.parse(maxToDate)
                    if (d2.compareTo(currentDate) > 0) {
                        datePicker.datePicker.maxDate = System.currentTimeMillis()
                    } else if (d2.compareTo(currentDate) < 0) {
                        datePicker.datePicker.maxDate = d2.time
                    } else if (d2.compareTo(d2) === 0) {
                        datePicker.datePicker.maxDate = System.currentTimeMillis()
                    }
                }
            }
            datePicker.show()
            datePicker.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    /*private fun showWarehouseListDialog(it: List<String>) {
        MaterialDialog(requireContext()).show {
            title(R.string.select_dest_wh)
            listItemsMultiChoice(items = it) { _, index, text ->
                filterWarehouseList(text.toString())
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.presentation.R.string.ok), true))
        }
    }*/

    /*private fun filterWarehouseList(locations: String) {
        val filter = locations.replace("[", "").replace("]", "").trim()
        if (filter.isEmpty()) {
            //setUpAdapter(history)
            //adapter.addAllValues(dispatchLotsList)
            setUpAdapter(history)
            binding.tvWareHouse.text = getString(R.string.all)
        } else {
            binding.tvWareHouse.text = filter
            val lotsList = history.filter { locations.contains(it.storageLocCode ?: "") }
            filteredHistory.clear()
            filteredHistory.addAll(lotsList)
            if(filteredHistory.size>0){
                binding.rvLots.visibility = View.VISIBLE
                binding.tvNoTransactions.visibility = View.GONE
            } else {
                binding.rvLots.visibility = View.GONE
                binding.tvNoTransactions.visibility = View.VISIBLE
            }
            setUpAdapter(filteredHistory)
        }
    }*/

    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<VegaGhanaCashewMtntHistoryTranxResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.mtnTxnDetails.let { it1 ->
                        if (it1?.isNotEmpty() == true) {
                          //  history.clear()
                            vegaWbsortedIds = listOf()
                            vegaWbsortedIds = it1.filter {
                                it.postingDate!! >= fromDate.toString()
                                        && it.postingDate!! <= toDate.toString()
                            }
                            if (vegaWbsortedIds.size>0)
                            {
                                vegaWbIds = vegaWbsortedIds.sortedWith(compareBy({ it.deliveryNumber })) as MutableList<VegaGhanaCashewMtntHistoryTransactions>

                                vegaWbIds.forEachIndexed { index, vegaGhanaCocoaMtntHistoryTransactions ->

                                    val desloc = vegaGhanaCocoaMtntHistoryTransactions.destinationLocation.toString().split("-")

                                    try {
                                        allPlants().single {  desloc[0].contains(it.plantId) }.apply {
                                            vegaGhanaCocoaMtntHistoryTransactions.destinationLocation = plantName .plus(" - ").plus(desloc[1])
                                        }
                                    } catch (e: NoSuchElementException) {

                                    }

                                }

                            }
                            history = vegaWbIds as MutableList<VegaGhanaCashewMtntHistoryTransactions>

                            //getWarehouseList()
                            //history.forEach { it.weighBridgeType = WEIGHBRIDGE }
                            /*(this.vegaWbIds as MutableList<VegaGhanaCocoaGRNHistoryTransactions>).sortBy { it.date }*/
                            if (vegaWbIds.size > 0) {
                                setUpAdapter(vegaWbIds)
                                binding.tvNoTransactions.gone()
                                binding.rvLots.visible()
                            } else {
                                binding.tvNoTransactions.visible()
                                binding.rvLots.gone()
                            }
                            //setUpAdapter(vegaWbIds)
                            //binding.tvTruckIDNo.text = getString(R.string.total_lot).plus(" ").plus(vegaWbIds.size)
                        } else {
                            //binding.tvTruckIDNo.text = getString(R.string.total_lot).plus(" 0")
                            binding.tvNoTransactions.visible()
                            binding.rvLots.gone()
                        }
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), response.error.toString())
                }
            }
        }

    }

    /*private fun getWarehouseList() {
        if (history.isNotEmpty()) {
            wareHouseList.clear()
            wareHouseList.addAll(history.listOfField(VegaGhanaCocoaMtntHistoryTransactions::storageLocCode).toSet())
            *//*if(wareHouseList.contains(storage)){
                binding.tvWareHouse.text = storage
                filterWarehouseList(storage.toString())
            } else {
                wareHouseList.add(storage.toString())
                setUpAdapter(history)
                binding.tvNoTransactions.visibility = View.VISIBLE
                binding.rvLots.visibility = View.GONE
            }*//*
        }
    }*/

    private fun setUpAdapter(data: List<VegaGhanaCashewMtntHistoryTransactions>?) {
        val history1 = data as MutableList<VegaGhanaCashewMtntHistoryTransactions>
        binding.rvLots.setUpAdapter(
            history1,
            R.layout.item_history_transactions_ghana_cashew_mtnt,
            ItemHistoryTransactionsGhanaCashewMtntBinding::inflate,
            { it1, pos, bindItem ->
                val vendorNameList =
                    supplierList.filter { it.vendorCode.equals((it1.transporterName?.substring(3))) }
                if (((!vendorNameList.isNullOrEmpty()) && (vendorNameList.size > 0))) {
                    bindItem.tvWbId.text = vendorNameList.get(0).vendorName
                }
                bindItem.tvDeliveryNo.text = it1.deliveryNumber
                bindItem.tvDate.text = DateUtils.getFormatedDate(it1.postingDate.toString())
                bindItem.tvMaterialValue.text = it1.materialCode
                bindItem.tvQuantityValue.text = it1.quantity.plus(" ").plus(it1.uom)
                bindItem.tvWaybillNo.text = it1.wayBillNumber
                bindItem.llItem.setOnClickListener {
                    val item = it1
                    item.vendorName = bindItem.tvWbId.text.toString()
                    callBack?.replaceFragment(HISTORY_TRANSACTIONS_LOT_MTNT, item)
                }
                /* tv_truck_no.text = if (it.vehicleNumber.isEmpty()) "-" else it.vehicleNumber
                 tvMaterialName.text = it.materialName
                 tvWbId.text = it.weighBridgeId
                 val times = it.erdat?.split('(', ')')
                 tvDate.text = times?.get(1).let { it1 ->
                     it1?.let { it2 ->
                         DateUtils.getUTCDateTime(
                             it2,
                             App.getAppContext()
                         )
                     }
                 }*/
            }, {

            })
    }

}
