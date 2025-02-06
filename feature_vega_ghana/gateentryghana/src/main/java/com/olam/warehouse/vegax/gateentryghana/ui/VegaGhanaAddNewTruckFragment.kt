package com.olam.warehouse.vegax.gateentryghana.ui

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCoffeeSingleSelectListener
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTime
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTimeMillis
import com.olam.warehouse.presentation.utils.LocaleHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentryghana.R
import com.olam.warehouse.vegax.gateentryghana.databinding.FragmentVegaGhanaAddNewTruckBinding
import com.olam.warehouse.vegax.gateentryghana.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*

class VegaGhanaAddNewTruckFragment : BaseFragment(), VegaCoffeeSingleSelectListener ,VegaSingleSelectListener{

    private var callBack: CallBack? = null
    private var gateEntryData = VegaGateEntry()
    private var mtnsList = mutableListOf<VegaReceivingMtn>()
    private var productList = mutableListOf<VegaReceivingMtnLots>()
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var warehouselist = mutableListOf<VegaSupplyStorageLocation>()
    private var selectedStorageLoc: String? = ""
    private lateinit var productNameList:List<VegaMaterial>
    private lateinit var suppliersList:List<VegaVendor>

    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null

    interface CallBack {
        fun replaceFragment(paramsListFrag: String, item: VegaGateEntry)
    }

    private val vm: VegaGateEntryGhanaViewModel by viewModel()
    private lateinit var binding: FragmentVegaGhanaAddNewTruckBinding
    override val layoutResourceId = R.layout.fragment_vega_ghana_add_new_truck

    companion object {
        fun newInstance(gateEntryData: VegaGateEntry) = VegaGhanaAddNewTruckFragment().putArgs {
            putParcelable(GATE_ENTRY_DATA, gateEntryData)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaGhanaAddNewTruckBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("gateentry/ui/VegaGhanaAddNewTruckFragment").title("Gate Entry").with(tracker)
        initUI()
    }

    private fun initUI() {
        gateEntryData = arguments?.getParcelable(GATE_ENTRY_DATA)!!
        updateMandatory()
        if (gateEntryData.weighBridgeType == PROCURE) {
            binding.llSupplier.visible()
            binding.llMtnr.gone()
        } else {
            binding.llSupplier.gone()
            binding.llMtnr.visible()
        }
        binding.tvDate.text = getUTCDateTime(System.currentTimeMillis().toString(), App.getAppContext())
        binding.tvDate.setOnClickListener { getDatePickerDialog() }
        binding.btnConfirm.setOnClickListener { validateInputs() }

        vm.product.observe(viewLifecycleOwner, Observer {
            productNameList = it
        })
        vm.getProducts()

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            suppliersList= it
        })
        vm.getSuppliers()

        vm.suppplierZone.observe(viewLifecycleOwner, Observer {
            it?.let { item ->
                if (it.size > 0) {
//                    binding.tvSupplierZone.setText(item[0].bczone.toString())
//                    binding.tvSupplierZone.isEnabled = false
                }
            }

        })

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            custonLocationList = it.toMutableList()
        })
        vm.getCustomLocations()

        if (AppUtils.isOnline() && gateEntryData.weighBridgeType == STO) {
            fetchMtnDetails()
        }
        binding.tvObdNumber.setOnClickListener {
            showSingleSelectDialog(false,getString(R.string.tittle_odb_popup),false,false,false)
        }
        binding.tvDispatchWarehouse.setOnClickListener {
            showSingleSelectDialog(false,getString(R.string.trans_Warehouse_popup),false,true,false)
        }
        binding.tvReceivingLocation.setOnClickListener {
            showSingleSelectDialog(false,getString(R.string.receiving_location_popup),false,false,true)
        }

        binding.tvProduct.setOnClickListener {
            showSingleSelectDialog(true,getString(R.string.select_product),false,false,false)
        }
        binding.tvSupplier.setOnClickListener {
            showSingleSelectDialog(false,getString(R.string.select_supplier),true,false,false)
        }
    }


    private fun fetchMtnDetails() {
        vm.warehouse.observe(viewLifecycleOwner, Observer { processApiResult(it) })
        vm.fetchWarehouseWithMtns()
    }

    private fun processApiResult(data: Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> saveResult(it.data)
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> showErrorDialogWithFAQLink(requireContext(), it.error.toString())
            }
        }
    }

    private fun saveResult(data: GenericReqAndResp<VegaReceivingMtnWrapper>?) {
        hideLoading()
        data?.data?.let {
            mtnsList = it.mtns as MutableList<VegaReceivingMtn>
            productList = it.batchDetails as MutableList<VegaReceivingMtnLots>
            warehouselist = it.storageLocationLst as MutableList<VegaSupplyStorageLocation>
        }
    }


    private fun showSingleSelectDialog(isGrade: Boolean, title: String, isVendor: Boolean,isWh:Boolean,isOrigin:Boolean) {
        val list: List<String>

        if(isGrade){
            list = productNameList.map { data -> data.materialName!! }
        }else if(isWh){
            list = warehouselist.map { data -> data.storageLocationCode.plus("-").plus(data.storageLocationName) }
        }else if(isOrigin){
            val customStorageLocationList= custonLocationList.filter { !it.storageLocationType.equals("B") }
            list = customStorageLocationList.map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }
        }else if(isVendor){
            list = suppliersList.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
        }else{
            val data = mtnsList.distinct().filter { it.storageLocationCode == selectedStorageLoc }
            list = data.map { item -> item.mtnNumber }
        }


        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isWh, isVendor, isGrade,
                list,
                requireActivity(),
                this, false, false, this, null, isOrigin = isOrigin, isDepartment = false
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }






    private fun getDatePickerDialog() {
        val cal = Calendar.getInstance()
        val dateTxt = binding.tvDate.text.split("/")
        cal.set(dateTxt[2].toInt(), dateTxt[0].toInt() - 1, dateTxt[1].toInt())
        val DATE_FORMAT = "MM/dd/yyyy"
        val UTC = "UTC"

        context?.let {
            val datePicker = DatePickerDialog(
                it,
                com.olam.warehouse.presentation.R.style.DatePickerTheme,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val sdf = SimpleDateFormat(DATE_FORMAT, LocaleHelper.getLocale(it))
                    sdf.timeZone = TimeZone.getTimeZone(UTC)
                    binding.tvDate.text = sdf.format(cal.time)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            //datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
            datePicker.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        }

    }

    private fun validateInputs() {
        when {
            gateEntryData.materialCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_product))
            binding.tvDate.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_date))
            binding.tvReceivingLocation.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_location))
            binding.tvTruckNo.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
            binding.tvDriverNameLabel.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver_name))
            binding.tvPhoneNo.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_phone_no))
            binding.etWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_weight))
            else -> {
                if (gateEntryData.weighBridgeType == PROCURE) {
                    when {
                        binding.tvSupplier.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_supplier))
//                        binding.tvSupplierZone.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_supplier_zone))
                        else -> moveToSummary()
                    }

                } else if (gateEntryData.weighBridgeType == STO) {
                    when {
                        binding.tvDispatchWarehouse.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_dispatch_wh))
                        binding.tvObdNumber.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_obd_no))
                        else -> moveToSummary()
                    }
                } else moveToSummary()

            }
        }
    }

    private fun moveToSummary() {
        gateEntryData.contactNumber = binding.tvPhoneNo.text.toString()
        gateEntryData.erdat = getUTCDateTimeMillis(binding.tvDate.text.toString(), App.getAppContext())
        gateEntryData.vehicleNumber = binding.tvTruckNo.text.toString()
        gateEntryData.driverName = binding.tvDriverName.text.toString()
//        gateEntryData.supplierZone = binding.tvSupplierZone.text.toString()
        gateEntryData.delivery = binding.tvObdNumber.text.toString()
        gateEntryData.approximateWeight = binding.etWeight.text.toString()
        gateEntryData.wsGate = WB01
        callBack?.replaceFragment(SUMMARY_FRAG, gateEntryData)
    }

    private fun updateMandatory() {
        binding.tvProductLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.product)) { mandatoryStars() } }
        binding.tvSupplierLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.supplier)) { mandatoryStars() } }
//        binding.tvSupplierZoneLabel.text =
//            with(UIUtils) { with(requireContext().resources.getString(R.string.supplier_zone)) { mandatoryStars() } }
        binding.tvDispatchWHLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.dispatch_wh)) { mandatoryStars() } }
        binding.tvObdLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.mtn_obd_number)) { mandatoryStars() } }
        binding.tvLocationLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.receiving_location)) { mandatoryStars() } }
        binding.tvDateLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.arrival_date)) { mandatoryStars() } }
        binding.tvDriverNameLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.driver_name)) { mandatoryStars() } }
        binding.tvTruckNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_no)) { mandatoryStars() } }
        binding.tvDriverNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.driver_no)) { mandatoryStars() } }
        binding.tvWeightLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.weight)) { mandatoryStars() } }
    }



    override fun clickOnItem(
        data: String,
        isWh: Boolean,
        isVendor: Boolean,
        isGrade: Boolean,
        isSupplier: Boolean,
        isOrigin: Boolean,
        isDepartment: Boolean
    ) {
        customDialog?.dismiss()
        if (isGrade) {
            binding.tvProduct.text= data
            productNameList.forEachIndexed { index, vegaMaterial ->
                if(vegaMaterial.materialName== binding.tvProduct.text.toString()){
                    gateEntryData.materialCode =
                        MATERIAL_CODE.plus(vegaMaterial.materialCode.toString())
                    gateEntryData.materialName = vegaMaterial.materialName.toString()
                    binding.tvUom.text = vegaMaterial.unitsOfMeasure.toString()
                    gateEntryData.unitsOfMeasure = vegaMaterial.unitsOfMeasure.toString()
                }
            }
        }else if(isVendor){
            binding.tvSupplier.text= data
            suppliersList.forEachIndexed { index, vegaVendor ->
                if (vegaVendor.vendorCode == binding.tvSupplier.text.toString().split(" - ")[0].trim()) {
                    gateEntryData.supplierCode = vegaVendor.vendorCode
                    gateEntryData.supplierName = vegaVendor.vendorName
                    vegaVendor.bcApprover?.let { it1 -> vm.getSupplierZone(it1) }
                }

            }
        }else if(isWh){
            binding.tvDispatchWarehouse.text = data
            warehouselist.forEach { it->
                if(it.storageLocationCode == data.split("-")[0].trim()){
                    binding.tvObdNumber.text = ""
                    val item = it.storageLocationCode
                    selectedStorageLoc = item
                    gateEntryData.plantId = UIUtils.getWarehouseId()
                    gateEntryData.supplierCode = it.plant
                    gateEntryData.supplierName = data
                }
            }
        }else if(isOrigin){
            val customStorageLocationList= custonLocationList.filter { !it.storageLocationType.equals("B") }
            customStorageLocationList.forEach {
                if(it.procureLocationCode== data.split("-")[0].trim()){
                    binding.tvReceivingLocation.text = data
                    gateEntryData.storageLocationCode = it.procureLocationCode
                    gateEntryData.storageLocationName = it.procureLocationName
                }

            }
        }else{
            val localData = mtnsList.distinct().filter { it.storageLocationCode == selectedStorageLoc }
            binding.tvObdNumber.text = data
            localData.forEach { mtn->
                if(mtn.mtnNumber== data){
                    gateEntryData.mtnCode = mtn.mtnNumber
                    gateEntryData.delivery = mtn.mtnNumber
                    gateEntryData.deliveryItem = mtn.posnr

                    val batchList = productList.filter { it.mtnNumber == mtn.mtnNumber }
                    if (batchList.size > 0) {
                        gateEntryData.batchNumber = batchList[0].batch
                        gateEntryData.purchaseDocNum = batchList[0].purchaseOrder
                        gateEntryData.purchaseDocDesc = batchList[0].ebelp
                    }

                }
            }


        }
    }

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {
        println("data:"+data)
    }


}
