package com.olam.wharhouse.vegax.transactionhistory.ui.rmin

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.gson.Gson
import com.microsoft.appcenter.utils.HandlerUtils
import com.olam.warehouse.login.ui.printformats.printForTransactionHistoryRmin
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.VegaRMINHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaRMINHistoryTranxResponse
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.DoAsync
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.LocaleHelper
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.HISTORY_DATA_RMIN
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.showOrHide
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.wharhouse.vegax.transactionhistory.R
import com.olam.wharhouse.vegax.transactionhistory.databinding.FragmentHistoryTransactionsRminListBinding
import com.olam.wharhouse.vegax.transactionhistory.databinding.ItemHistoryTransactionsRminBinding
import com.olam.wharhouse.vegax.transactionhistory.ui.VegaTransHisViewModel
import com.olam.wharhouse.vegax.transactionhistory.ui.VegaHistoryTransactionsReplaceFragmentCallback
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_DATA_MTNT
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_LOT_RMIN
import com.olam.wharhouse.vegax.transactionhistory.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VegaHistoryRminListFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_history_transactions_rmin_list
    private lateinit var binding: FragmentHistoryTransactionsRminListBinding
    private var callBack: VegaHistoryTransactionsReplaceFragmentCallback? = null
    private val vm: VegaTransHisViewModel by viewModel()

    private var history = mutableListOf<VegaRMINHistoryTransactions>()
    private var filteredHistory = mutableListOf<VegaRMINHistoryTransactions>()
    private var historyData = VegaRMINHistoryTransactions()
    private val mSearchList = mutableListOf<VegaRMINHistoryTransactions>()
    private var vegaWbIds = listOf<VegaRMINHistoryTransactions>()
    private var vegaWbsortedIds = listOf<VegaRMINHistoryTransactions>()
    private var wareHouseList = mutableListOf<String>()
    private var sendingWareHouseList = mutableListOf<VegaCustomStLocation>()
    private var storageLocationList: ArrayList<String> = ArrayList()
    private var storage: String? = ""
    private var toDate: String? = ""
    private var fromDate: String? = ""
    private var supplierList = mutableListOf<VegaVendor>()
    private var tallyPrintKeys = ArrayList<String>()
    private var isAdminUser= false
    private var plantId=""
    private var isLast30DaysSubmitted= false


    companion object {
        fun newInstance(
            historyTransactions: VegaRMINHistoryTransactions
        ) = VegaHistoryRminListFragment().putArgs {
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
        binding = FragmentHistoryTransactionsRminListBinding.inflate(inflater)
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
                                     if (qtyWb.poNumber?.contains(text) == true ||
                                         qtyWb.date.toString()
                                             .contains(text) ||
                                         qtyWb.dryingLoss?.contains(text) == true ||
                                         qtyWb.quantity?.contains(text) == true
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
        TrackHelper.track().screen("historytransactions/ui/VegaHistoryTransactionsRminListFragment")
            .title("History Transactions - RMIN").with(tracker)
        initUI()
    }

    private fun initUI() {
        historyData = arguments?.getParcelable(HISTORY_DATA_MTNT)!!
        if(PreferenceHelper.get(Constants.ADMIN_USER,false)) isAdminUser= true
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
                /*vegaWbIds.let { history = it as MutableList<VegaGhanaCashewRMINHistoryTransactions> }
                history = history.asReversed()
                vegaWbIds = history*/
                setUpAdapter(history)
            }
        }
        binding.btnPrint.setOnClickListener {
            if(isAdminUser){
                    showDialog()
                }else{
               printStart()
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
            if(!isAdminUser) {
                vm.getHistoryTranxRmin(
                    DateUtils.formatDateGHCashew(binding.tvToDate.text.toString()),
                    if (isAdminUser) plantId else "",
                    DateUtils.formatDateGHCashew(binding.tvFromDate.text.toString())
                )
            }

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
            if(isAdminUser && plantId.isEmpty()){
                Toast.makeText(context, getString(R.string.pls_select_plant), Toast.LENGTH_SHORT).show()
            }else if (!(binding.tvFromDate.text.toString().isEmpty() || binding.tvToDate.text.toString()
                    .isEmpty())
            ) {
                fromDate = DateUtils.formatDateGHCashew(binding.tvFromDate.text.toString())
                toDate = DateUtils.formatDateGHCashew(binding.tvToDate.text.toString())
                vm.getHistoryTranxRmin(toDate.toString(), if(isAdminUser) plantId else "", fromDate.toString())

            } else {
                Toast.makeText(context, "Please enter Date", Toast.LENGTH_SHORT).show()
            }
        }


        //fromDate = DateUtils.formatDateGHCashew(binding.tvFromDate.text.toString())
       // toDate = DateUtils.formatDateGHCashew(binding.tvToDate.text.toString())
        if(isAdminUser) binding.llPlant.visible() else binding.llPlant.gone()

        binding.tvPlantList.setOnClickListener {
            showPlantDialog()
        }

        vm.historyrmintranx.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })

    }

    private fun printStart(){
        isLast30DaysSubmitted= false
        if(!history.isEmpty()) {
            showCustomLoading()
            DoAsync {
                tallyPrintKeys.clear()
                val intent = Intent()
                val historyArrayList = ArrayList(history)
                intent.putParcelableArrayListExtra(HISTORY_DATA_RMIN, historyArrayList)
                tallyPrintKeys = printForTransactionHistoryRmin(intent, requireContext())

                HandlerUtils.runOnUiThread {
                    showPreviewDialog()
                }

            }.execute()
        }

    }

    private fun showDialog(){
        showDialogWithRadioChoice(object : DialogClick{
            override fun onPositive(remark: String) {
                var selectedPosition = remark.toInt()
                if(selectedPosition>=0){
                    when(selectedPosition){
                        0 ->  printStart()
                        1 -> last30DaysPrint()
                    }

                }else{
                    Toast.makeText(context, "Please choose your choice", Toast.LENGTH_SHORT).show()
                }

            }

        })
    }


    private fun last30DaysPrint(){
        isLast30DaysSubmitted = true
        fromDate= DateUtils.formatDateGHCashew(DateUtils.subtractingDaysToDate((DateUtils.getDate()), 30))
        toDate= DateUtils.getCurrentDateYearFormat()
        vm.getHistoryTranxRmin(toDate.toString(), if(isAdminUser) plantId else "", fromDate.toString())
    }

    private fun showPreviewDialog() {
        DoAsync {
            HandlerUtils.runOnUiThread {
                hideCustomLoading()
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                startActivity(Intent(requireContext(), WifiMainActivity::class.java))
            }
        }.execute()
    }

    private fun showPlantDialog() {
        val plantList = Gson().fromJson<List<Plant>>(
            PreferenceHelper.get(
                Constants.PLANT_LIST,
                ""
            )
        )
        val plants = plantList.map { data -> data.plantId }

        MaterialDialog(requireContext()).show {
            title(R.string.select_plant)
            listItemsSingleChoice(items = plants) { _, index, text ->
                binding.tvPlantList.setText(text)
                plantId= text.toString()
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
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
                    var maxToDate = DateUtils.addingDaysToDate(fromDate, if(isAdminUser) DateUtils.NO_OF_DAYS_30 else DateUtils.NO_OF_DAYS)
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

    @SuppressLint("SuspiciousIndentation")
    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<VegaRMINHistoryTranxResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.rminTxn.let { it1 ->
                        if (it1?.isNotEmpty() == true) {

                            vegaWbsortedIds = listOf()
                            vegaWbsortedIds = it1.filter {
                                it.date!! >= fromDate.toString()
                                        && it.date!! <= toDate.toString()
                            }
                            if (vegaWbsortedIds.size>0) {

                                vegaWbIds = vegaWbsortedIds

                            }
                            if(vegaWbIds.size>0) {
                           history = vegaWbIds as MutableList<VegaRMINHistoryTransactions>
                            }

                           history = vegaWbIds.toMutableList()
                            if (vegaWbIds.size > 0) {
                                setUpAdapter(vegaWbIds)
                                binding.tvNoTransactions.gone()
                                binding.rvLots.visible()
                            } else {
                                binding.tvNoTransactions.visible()
                                binding.rvLots.gone()
                            }

                            if(isLast30DaysSubmitted) printStart()

                        } else {
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
            wareHouseList.addAll(history.listOfField(VegaGhanaCashewRMINHistoryTransactions::storageLocCode).toSet())
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

    private fun setUpAdapter(data: List<VegaRMINHistoryTransactions>?) {
        val history1 = data as MutableList<VegaRMINHistoryTransactions>
        binding.rvLots.setUpAdapter(
            history1,
            R.layout.item_history_transactions_rmin,
            ItemHistoryTransactionsRminBinding::inflate,
            { it1, pos, bindItem ->
                bindItem.tvprocesstype.text = it1.processingType
                bindItem.tvquantity.text = it1.quantity.plus(" ").plus(it1.unitsOfMeasure)
                bindItem.tvdate.text = it1.date.toString()
                bindItem.tvdryloss.text = it1.dryingLoss
                bindItem.tvponumber.text=it1.poNumber
                bindItem.apply {
                    it1.materialDetail.apply {
                        layoutMaterialDesc.showOrHide(!this?.materialName.isNullOrEmpty())
                        layMaterial.showOrHide(!this?.materialName.isNullOrEmpty())
                        tvItemName.text = this?.materialName?:""
                    }
                }

                bindItem.llItem.setOnClickListener {
                    val item = it1
                   // item.vendorName = bindItem.tvWbId.text.toString()
                    callBack?.replaceFragment(HISTORY_TRANSACTIONS_LOT_RMIN, item)
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
