package com.olam.wharhouse.vegax.transactionhistory.ui.grn

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.gson.Gson
import com.microsoft.appcenter.utils.HandlerUtils
import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import com.olam.warehouse.login.ui.printformats.printForTransactionHistoryGRN
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaSupplyStorageLocation
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.VegaGRNHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaGRNHistoryTranxResponse
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCashewGRNHistoryTransactions
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.wharhouse.vegax.transactionhistory.R
import com.olam.wharhouse.vegax.transactionhistory.databinding.FragmentHistoryTransactionsGrnListBinding
import com.olam.wharhouse.vegax.transactionhistory.databinding.ItemHistoryTransactionsGrnBinding
import com.olam.wharhouse.vegax.transactionhistory.ui.VegaTransHisViewModel
import com.olam.wharhouse.vegax.transactionhistory.ui.VegaHistoryTransactionsReplaceFragmentCallback
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_DATA_GRN
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_LOT_GRN
import com.olam.wharhouse.vegax.transactionhistory.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VegaHistoryGrnListFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_history_transactions_grn_list
    private lateinit var binding: FragmentHistoryTransactionsGrnListBinding
    private var callBack: VegaHistoryTransactionsReplaceFragmentCallback? = null
    private val vm: VegaTransHisViewModel by viewModel()
    private var supplierList = mutableListOf<VegaVendor>()
    private var history = mutableListOf<VegaGRNHistoryTransactions>()
    private var filteredHistory = mutableListOf<VegaGRNHistoryTransactions>()
    private var historyData = VegaGRNHistoryTransactions()
    private val mSearchList = mutableListOf<VegaGRNHistoryTransactions>()
    private var vegaWbIds = listOf<VegaGRNHistoryTransactions>()
    private var vegaWbsortedIds = listOf<VegaGRNHistoryTransactions>()
    private var sortList = VegaGhanaCashewGRNHistoryTransactions()
    private var wareHouseList = mutableListOf<String>()
    private var sendingWareHouseList = mutableListOf<VegaCustomStLocation>()
    private var storageLocationList: ArrayList<String> = ArrayList()
    private var storage: String? = ""
    private var toDate: String? = ""
    private var fromDate: String? = ""
    private var plantId=""
    private var tallyPrintKeys = ArrayList<String>()
    private var isAdminUser= false
    private var isLast30DaysSubmitted= false

    companion object {
        fun newInstance(
            historyTransactions: VegaGRNHistoryTransactions
        ) = VegaHistoryGrnListFragment().putArgs {
            putParcelable(HISTORY_DATA_GRN, historyTransactions)
        }

        const val SEARCH_HINT_TEXT = "Search"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaHistoryTransactionsReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentHistoryTransactionsGrnListBinding.inflate(inflater)
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
                                    if (qtyWb.grnNumber?.contains(text) == true ||
                                        DateUtils.formatDateGhana(qtyWb.postingDate.toString())
                                            .contains(text)
                                        || qtyWb.wbId?.contains(text) == true || qtyWb.quantity?.contains(
                                            text
                                        ) == true
                                        || qtyWb.vendorCode?.contains(text) == true || qtyWb.whReceiptNumber?.contains(
                                            text
                                        ) == true
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
        TrackHelper.track()
            .screen("historytransactions/ui/grn/VegaHistoryTransactionsGrnListFragment")
            .title("History Transactions - GRN").with(tracker)

        initUI()
    }

    private fun initUI() {
        historyData = arguments?.getParcelable(HISTORY_DATA_GRN)!!

        if(PreferenceHelper.get(Constants.ADMIN_USER,false)) isAdminUser= true


        /*vm.custonLocation.observe(viewLifecycleOwner, Observer {
            sendingWareHouseList = it.toMutableList()
            storageLocationList = sendingWareHouseList.map { it.procureLocationCode } as ArrayList<String>
        })
        vm.getCustomLocations()*/
        binding.ivSortDownUp.setOnClickListener {
            if (history.isNotEmpty()) {
                val data = history
                history = data.asReversed()
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



        binding.btnPrint.setOnClickListener {
            if(isAdminUser){
                showDialog()
            }else{
                printStart()
            }
        }


       vm.getSuppliers()

        vm.supplier.observe(viewLifecycleOwner, Observer {
            supplierList = it.toMutableList()

            if(!isAdminUser) {
                vm.getHistoryTranxGrn(
                    DateUtils.formatDateGHCocoa(binding.tvToDate.text.toString()),
                    if (isAdminUser) plantId else "",
                    DateUtils.formatDateGHCocoa(binding.tvFromDate.text.toString())
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
                    .isEmpty() )
            ) {
                vm.getHistoryTranxGrn(
                    DateUtils.formatDateGHCocoa(binding.tvToDate.text.toString()),
                    if(isAdminUser)plantId else "",
                    DateUtils.formatDateGHCocoa(binding.tvFromDate.text.toString())
                )
                fromDate = DateUtils.formatDateGHCocoa(binding.tvFromDate.text.toString())
                toDate = DateUtils.formatDateGHCocoa(binding.tvToDate.text.toString())
                vm.historytranx.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
                //storage = binding.tvWareHouse.text.toString().trim()
            }else {
                Toast.makeText(context, "Please enter Date", Toast.LENGTH_SHORT).show()
            }
        }


        vm.historytranx.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        fromDate = DateUtils.formatDateGHCocoa(binding.tvFromDate.text.toString())
        toDate = DateUtils.formatDateGHCocoa(binding.tvToDate.text.toString())

        if(isAdminUser) binding.llPlant.visible() else binding.llPlant.gone()

        binding.tvPlantList.setOnClickListener {
            showPlantDialog()
        }

    }
    private fun printStart(){
        isLast30DaysSubmitted= false
        if(!history.isEmpty()) {
            showCustomLoading()
            DoAsync {
                tallyPrintKeys.clear()
                val intent = Intent()
                val historyArrayList = java.util.ArrayList(history)
                intent.putParcelableArrayListExtra(UIUtils.HISTORY_DATA_GRN, historyArrayList)
                tallyPrintKeys = printForTransactionHistoryGRN(intent, requireContext())

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
        vm.getHistoryTranxFgrn(
            toDate.toString(),
            if(isAdminUser) plantId else "",
            fromDate.toString()
        )
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

    private fun showPreviewDialog() {
        DoAsync {
            runOnUiThread {
                hideCustomLoading()
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                startActivity(Intent(requireContext(), WifiMainActivity::class.java))
            }
        }.execute()
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
                    var maxToDate = DateUtils.addingDaysToDate(fromDate,  if(isAdminUser) DateUtils.NO_OF_DAYS_30 else DateUtils.NO_OF_DAYS)
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
    }

    private fun filterWarehouseList(locations: String) {
        val filter = locations.replace("[", "").replace("]", "").trim()
        if (filter.isEmpty()) {
            //setUpAdapter(history)
            //adapter.addAllValues(dispatchLotsList)
            setUpAdapter(history)
            binding.tvWareHouse.text = getString(R.string.all)
        } else {
            binding.tvWareHouse.text = filter
            val lotsList = history.filter { locations.contains(it.storageLocationCode ?: "") }
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

    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<VegaGRNHistoryTranxResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.grnTxnDetails.let { it1 ->
                        if (it1?.isNotEmpty() == true) {
                            vegaWbIds= it1
                            history = vegaWbIds as MutableList<VegaGRNHistoryTransactions>

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

    /* private fun getWarehouseList() {
         if (history.isNotEmpty()) {
             wareHouseList.clear()
             wareHouseList.addAll(history.listOfField(VegaGhanaCocoaGRNHistoryTransactions::storageLocationCode).toSet())
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

    private fun setUpAdapter(data: List<VegaGRNHistoryTransactions>) {
        val history1 = data as MutableList<VegaGRNHistoryTransactions>
        binding.rvLots.setUpAdapter(
            history1,
            R.layout.item_history_transactions_grn,
            ItemHistoryTransactionsGrnBinding::inflate,
            { it1, pos, bindItem ->
                /*  val vendorNameList = supplierList.filter { it.vendorCode.equals(it1.vendorCode) }
                  if (((!vendorNameList.isNullOrEmpty()) && (vendorNameList.size > 0))) {
                      tvWbId.text = vendorNameList.get(0).vendorName
                  }*/
                bindItem.tvWbId.text = it1.vendorName
                bindItem.tvWHReceiptNo.text = it1.whReceiptNumber
                bindItem.tvDate.text =  DateUtils.getFormatedDate(it1.postingDate.toString())
                bindItem.tvGRNNumber.text = it1.grnNumber
                bindItem.tvQuantity.text = it1.quantity.plus(" ").plus(it1.uom)
                bindItem.tvVendorCode.text = it1.vendorCode

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

                val roleData =
                    Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
                        .filter { key ->
                            key.roleKey.equals(
                                PreferenceHelper.get(
                                    Constants.CURRENT_KEY,
                                    ""
                                ),true
                            )
                        }
                bindItem.apply {
                    tvStorageLocation.text = it1.storageLocationCode
                    tvMaterialDesc.text = it1.materialCode
                    for (rol in roleData.withIndex())
                    {
                        when (UserRoles.valueOfEnum(rol.value.roleName.trim())) {
                            /*UserRoles.GRN -> {
                                isGrn= true
                                //  binding.llHistoryTrans.visibility = View.VISIBLE
                            }*/
                            UserRoles.PCH -> {
                                //isUnit= true
                                //  binding.llHistoryTrans.visibility = View.VISIBLE
                                tvMaterialPrice.text = it1.totalPrice
                                tvTotatalMaterialPrice.text = it1.pricePerUnit
                                layoutPriceFirst.visible()
                                layoutPriceTwo.visible()
                                break
                            }
                            else -> {
                                layoutPriceFirst.gone()
                                layoutPriceTwo.gone()
                            }
                        }
                    }
                }


                bindItem.llItem.setOnClickListener {
                    val item = it1
                    item.vendorName = bindItem.tvWbId.text.toString()
                    callBack?.replaceFragment(HISTORY_TRANSACTIONS_LOT_GRN, item)
                }
            }, {

            })
    }

}
