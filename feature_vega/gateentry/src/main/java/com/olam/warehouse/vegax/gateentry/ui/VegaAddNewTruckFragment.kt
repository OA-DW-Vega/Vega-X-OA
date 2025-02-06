package com.olam.warehouse.vegax.gateentry.ui

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
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTime
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTimeCameroon
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTimeMillis
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTimeMillisCameroon
import com.olam.warehouse.presentation.utils.LocaleHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentry.R
import com.olam.warehouse.vegax.gateentry.databinding.FragmentVegaAddNewTruckBinding
import com.olam.warehouse.vegax.gateentry.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*

class VegaAddNewTruckFragment : BaseFragment(), VegaSingleSelectCommonListener {

    private var suppliersList: List<VegaVendor> = emptyList()
    private var productNameList: List<VegaMaterial> = emptyList()
    private var callBack: CallBack? = null
    private var gateEntryData = VegaGateEntry()
    private var mtnsList = mutableListOf<VegaReceivingMtn>()
    private var productList = mutableListOf<VegaReceivingMtnLots>()
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var warehouselist = mutableListOf<VegaSupplyStorageLocation>()
    private var selectedStorageLoc: String? = ""
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null


    interface CallBack {
        fun replaceFragment(paramsListFrag: String, item: VegaGateEntry)
    }

    private val vm: VegaGateEntryViewModel by viewModel()
    private lateinit var binding: FragmentVegaAddNewTruckBinding
    override val layoutResourceId = R.layout.fragment_vega_add_new_truck

    companion object {
        fun newInstance(gateEntryData: VegaGateEntry) = VegaAddNewTruckFragment().putArgs {
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
        binding = FragmentVegaAddNewTruckBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("gateentry/ui/VegaAddNewTruckFragment").title("Gate Entry").with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnConfirm, it, true)
        }
        gateEntryData = arguments?.getParcelable(GATE_ENTRY_DATA)!!
        updateMandatory()
        if (gateEntryData.weighBridgeType == PROCURE) {
            binding.llSupplier.visible()
            binding.llMtnr.gone()
        } else {
            binding.llSupplier.gone()
            binding.llMtnr.visible()
        }
        binding.tvDate.text =
            getUTCDateTime(System.currentTimeMillis().toString(), App.getAppContext())
        binding.tvDate.setOnClickListener { getDatePickerDialog() }
        binding.btnConfirm.setOnClickListener { validateInputs() }

        binding.tvProduct.setOnClickListener { showSingleSelectDialog(getString(R.string.select_product),PRODUCT) }
        vm.product.observe(viewLifecycleOwner, Observer {
            productNameList= it
            it.forEach { it1 ->
                if (it1.materialName.equals("Ivory Coast Raw Cashew Nut")
                    && it1.materialCode.equals("100000000939")) {
                    gateEntryData.materialCode =
                        MATERIAL_CODE.plus(it1.materialCode.toString())
                    gateEntryData.materialName = it1.materialName.toString()
                    binding.tvProduct.setText(it1.materialName.plus("-").plus(it1.materialCode), TextView.BufferType.EDITABLE)
                    binding.tvUom.text = it1.unitsOfMeasure.toString()
                    gateEntryData.unitsOfMeasure = it1.unitsOfMeasure.toString()
                }
            }
        })
        vm.getProducts()
        binding.tvSupplier.setOnClickListener { showSingleSelectDialog(getString(R.string.select_supplier),SUPPLIER) }

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            suppliersList= it.filter { data -> data.bcApprover?.isNotEmpty() == true }
        })
        vm.getSuppliers()

        vm.suppplierZone.observe(viewLifecycleOwner, Observer {
            it?.let { item ->
                if (it.size > 0) {
                    binding.tvSupplierZone.setText(item[0].bczone.toString())
                    binding.tvSupplierZone.isEnabled = false
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
            showSingleSelectDialog(getString(R.string.tittle_odb_popup),OBD)
        }
        binding.tvDispatchWarehouse.setOnClickListener {
            showSingleSelectDialog(getString(R.string.trans_Warehouse_popup), WAREHOUSE)
        }
        binding.tvReceivingLocation.setOnClickListener {
            showSingleSelectDialog(getString(R.string.receiving_location_popup),"") }
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


    private fun showReceivingLocationDialog(it: List<VegaCustomStLocation>) {
        val location = it.map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }
        MaterialDialog(requireContext()).show {
            title(R.string.receiving_location_popup)
            listItemsSingleChoice(items = location) { _, index, text ->
                binding.tvReceivingLocation.text = text
                gateEntryData.storageLocationCode = it[index].procureLocationCode
                gateEntryData.storageLocationName = it[index].procureLocationName
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }



    private fun getDatePickerDialog() {
        val cal = Calendar.getInstance()
        val dateTxt = binding.tvDate.text.split("/")
        cal.set(dateTxt[2].toInt(), dateTxt[0].toInt() - 1, dateTxt[1].toInt())
        val DATE_FORMAT = "dd/MM/yyyy"
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
                        binding.tvSupplierZone.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_supplier_zone))
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
        gateEntryData.erdat = getUTCDateTimeMillisCameroon(binding.tvDate.text.toString(), App.getAppContext())
        gateEntryData.vehicleNumber = binding.tvTruckNo.text.toString()
        gateEntryData.driverName = binding.tvDriverName.text.toString()
        gateEntryData.supplierZone = binding.tvSupplierZone.text.toString()
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
        binding.tvSupplierZoneLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.supplier_zone)) { mandatoryStars() } }
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

    private fun showSingleSelectDialog(title: String,currentFalg: String) {
        val list :List<String>
        when(currentFalg) {
            PRODUCT -> {
                list = productNameList.map { data -> data.materialName!! }
            }
            SUPPLIER->{
             list= suppliersList.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            }
            WAREHOUSE->{
                list = warehouselist.map { data -> data.storageLocationCode.plus("-").plus(data.storageLocationName) }

            }OBD->{
            val data = mtnsList.distinct().filter { it.storageLocationCode == selectedStorageLoc }
            list = data.map { item -> item.mtnNumber }
            }
            else->{
                val locationList=custonLocationList.filter { !it.storageLocationType.equals("B") }
                list = locationList.map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }

            }
        }
        customDialog =
            VegaCommonSingleSelectDialogWithSearch(
                title,
                currentFalg,
                list,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, currentFlag: String) {
        customDialog?.dismiss()
        when (currentFlag) {
            PRODUCT -> {
                productNameList.forEach { material ->
                    if (material.materialCode.equals("100000000939")) {
                        binding.tvProduct.text= data
                        gateEntryData.materialCode =
                            MATERIAL_CODE.plus(material.materialCode.toString())
                        gateEntryData.materialName = material.materialName.toString()
                        binding.tvUom.text = material.unitsOfMeasure.toString()
                        gateEntryData.unitsOfMeasure = material.unitsOfMeasure.toString()
                    }
                }

            }
            SUPPLIER->{
                binding.tvSupplier.text= data
                suppliersList.forEach { vendor ->
                    if (vendor.vendorCode == binding.tvSupplier.text.toString().split(" - ")[0]) {
                        gateEntryData.supplierCode = vendor.vendorCode
                        gateEntryData.supplierName = vendor.vendorName
                        vendor.bcApprover?.let { it1 -> vm.getSupplierZone(it1) }
                    }
                }

            }
            WAREHOUSE->{
                binding.tvDispatchWarehouse.text = data
                warehouselist.forEach {
                    if(it.storageLocationCode== data.split("-")[0].trim()){
                        binding.tvObdNumber.text = ""
                        val item = it.storageLocationCode
                        selectedStorageLoc = item
                        gateEntryData.plantId = UIUtils.getWarehouseId()
                        gateEntryData.supplierCode = it.plant
                        gateEntryData.supplierName = data
                    }
                }
            }
            OBD->{
                val dataList = mtnsList.distinct().filter { it.storageLocationCode == selectedStorageLoc }
                binding.tvObdNumber.text = data
                dataList.forEach { mtn->
                    if(mtn.mtnNumber == data.trim()){
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
     else->{
         binding.tvReceivingLocation.text = data
         val locationList=custonLocationList.filter { !it.storageLocationType.equals("B") }
         locationList.forEach {
             if(it.procureLocationCode== data.split("-")[0].trim()) {
                 gateEntryData.storageLocationCode = it.procureLocationCode
                 gateEntryData.storageLocationName = it.procureLocationName
             }
         }

     }
    }


}
}
