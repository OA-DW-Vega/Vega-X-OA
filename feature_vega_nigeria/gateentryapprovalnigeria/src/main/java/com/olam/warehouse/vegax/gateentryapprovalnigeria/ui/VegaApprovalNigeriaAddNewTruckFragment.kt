package com.olam.warehouse.vegax.gateentryapprovalnigeria.ui

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
import com.google.gson.Gson
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTime
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTimeMillis
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentryapprovalnigeria.R
import com.olam.warehouse.vegax.gateentryapprovalnigeria.databinding.FragmentVegaApprovalNigeriaAddNewTruckBinding
import com.olam.warehouse.vegax.gateentryapprovalnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*

class VegaApprovalNigeriaAddNewTruckFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private var gateEntryData = VegaGateEntry()
    private var mtnsList = mutableListOf<VegaReceivingMtn>()
    private var productList = mutableListOf<VegaReceivingMtnLots>()
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var plantList = mutableListOf<Plant>()
    private var warehouselist = mutableListOf<VegaSupplyStorageLocation>()
    private var selectedStorageLoc: String? = ""

    interface CallBack {
        fun replaceFragment(
            paramsListFrag: String,
            item: VegaGateEntry,
            plantDetails: Plant
        )
    }

    private val vm: VegaGateEntryApprovalNigeriaViewModel by viewModel()
    private lateinit var binding: FragmentVegaApprovalNigeriaAddNewTruckBinding
    override val layoutResourceId = R.layout.fragment_vega_approval_nigeria_add_new_truck

    companion object {
        fun newInstance(gateEntryData: VegaGateEntry) = VegaApprovalNigeriaAddNewTruckFragment().putArgs {
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
        binding = FragmentVegaApprovalNigeriaAddNewTruckBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("gateentryApprovalNigeria/ui/VegaApprovalNigeriaAddNewTruckFragment").title("Vega_ApprovalNigeria/Gate Entry").with(tracker)
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
            val products = it.map { data -> data.materialName }
            it.forEach { it1 ->
                if (it1.materialName.equals("Ivory Coast Raw Cashew Nut")) {
                    gateEntryData.materialCode =
                        MATERIAL_CODE.plus(it1.materialCode.toString())
                    gateEntryData.materialName = it1.materialName.toString()
                    binding.tvProduct.setText(it1.materialName, TextView.BufferType.EDITABLE)
                    binding.tvUom.text = it1.unitsOfMeasure.toString()
                    gateEntryData.unitsOfMeasure = it1.unitsOfMeasure.toString()
                }
            }
            val productAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, products)
            binding.tvProduct.threshold = 1
            binding.tvProduct.setAdapter(productAdapter)
            binding.tvProduct.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                it.forEach { material ->
                    if (material.materialName == binding.tvProduct.text.toString()) {
                        gateEntryData.materialCode =
                            MATERIAL_CODE.plus(material.materialCode.toString())
                        gateEntryData.materialName = material.materialName.toString()
                        binding.tvUom.text = material.unitsOfMeasure.toString()
                        gateEntryData.unitsOfMeasure = material.unitsOfMeasure.toString()
                    }
                }
            }
        })
        vm.getProducts()

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            val suppliers = it
//                .filter { data -> data.bcApprover?.isNotEmpty()!! }
                .map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            val supplierAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suppliers)
            binding.tvSupplier.threshold = 1
            binding.tvSupplier.setAdapter(supplierAdapter)
            binding.tvSupplier.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                it.forEach { vendor ->
                    if (vendor.vendorCode == binding.tvSupplier.text.toString().split(" - ")[0]) {
                        gateEntryData.supplierCode = vendor.vendorCode
                        gateEntryData.supplierName = vendor.vendorName
                        vendor.bcApprover?.let { it1 -> vm.getSupplierZone(it1) }
                    }
                }
            }
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
            var receivingLocationList = custonLocationList.filter {
                !it.storageLocationType.equals(
                    "B"
                )
            }
            if (receivingLocationList.size == 1) {
                binding.tvReceivingLocation.text = custonLocationList[0].procureLocationCode.plus(" - ")
                    .plus(custonLocationList[0].procureLocationName)
                gateEntryData.storageLocationCode = custonLocationList[0].procureLocationCode
                gateEntryData.storageLocationName = custonLocationList[0].procureLocationName
            }

        })
        vm.getCustomLocations()

        plantList = getMultiPlantList() as MutableList<Plant>
        if (plantList.size == 1) {
            binding.tvPlant.text = plantList[0].plantId.plus(" - ")
                .plus(plantList[0].plantName)
            gateEntryData.plantId = plantList[0].plantId
        }

        /*vm.multiPlant.observe(viewLifecycleOwner, Observer {
            plantList = it.toMutableList()

            if (plantList.size == 1) {
                binding.tvReceivingLocation.text = plantList[0].plantId.plus(" - ")
                    .plus(plantList[0].plantName)
            }
        })
        vm.getMultiPlantList()*/

        binding.tvPlant.setOnClickListener {
            if (plantList.size >= 1) {
                showPlantSelectionDialog(plantList)
                binding.tvReceivingLocation.text = ""
            }
        }

        if (AppUtils.isOnline() && gateEntryData.weighBridgeType == STO) {
            fetchMtnDetails()
        }
        binding.tvObdNumber.setOnClickListener { showStoDialog(mtnsList) }
        binding.tvDispatchWarehouse.setOnClickListener { showDispatchWHDialog(warehouselist) }
        binding.tvReceivingLocation.setOnClickListener {
            if (binding.tvPlant.text != "") {
                2
                var plantid = binding.tvPlant.text
                var receivingLocationList = custonLocationList.filter {
                    !it.storageLocationType.equals(
                        "B"
                    )
                }.filter { it.plant == gateEntryData.plantId }
                if (receivingLocationList.size >= 1) {
                    showReceivingLocationDialog(receivingLocationList)
                }
            }
        }
    }

    private fun getMultiPlantList(): List<Plant> {
        val plants = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.PLANT_LIST, ""))
        return plants
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
                Resource.Status.ERROR -> UIUtils.showErrorDialog(requireContext(), it.error.toString())
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

    private fun showStoDialog(mtnsList: MutableList<VegaReceivingMtn>) {
        val data = mtnsList.distinct().filter { it.storageLocationCode == selectedStorageLoc }
        val mtnNumbers = data.map { item -> item.mtnNumber }
        MaterialDialog(requireContext()).show {
            title(R.string.tittle_odb_popup)
            listItemsSingleChoice(items = mtnNumbers) { _, index, text ->
                binding.tvObdNumber.text = text
                val mtn = data[index]
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
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
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

    private fun showPlantSelectionDialog(it: List<Plant>) {
        val location = it.map { data -> data.plantId.plus(" - ").plus(data.plantName) }
        MaterialDialog(requireContext()).show {
            title(R.string.plant_popup)
            listItemsSingleChoice(items = location) { _, index, text ->
                binding.tvPlant.text = text
                gateEntryData.plantId = it[index].plantId
//                gateEntryData.storageLocationName = it[index].procureLocationName
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun showDispatchWHDialog(it: List<VegaSupplyStorageLocation>) {
        val warehouses = it.map { data -> data.storageLocationCode.plus("-").plus(data.storageLocationName) }

        MaterialDialog(requireContext()).show {
            title(R.string.trans_Warehouse_popup)
            listItemsSingleChoice(items = warehouses) { _, index, text ->
                binding.tvDispatchWarehouse.text = text
                binding.tvObdNumber.text = ""
                val item = it[index].storageLocationCode
                selectedStorageLoc = item
                gateEntryData.plantId = UIUtils.getWarehouseId().toString()
                gateEntryData.supplierCode = it[index].plant
                gateEntryData.supplierName = text.toString()
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
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
            binding.tvProduct.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_product))
            binding.tvDate.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_date))
            binding.tvReceivingLocation.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_location))
            binding.tvTruckNo.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
            binding.tvDriverName.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver_name))
            binding.tvMtntNoValue.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_mtnt_no))
            binding.etWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_weight))
            binding.tvNoOfBagsValue.text.isNullOrEmpty() -> showSnack(getString(R.string.error_no_of_bags))
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
        gateEntryData.truckDriverName = binding.tvDriverName.text.toString()
//        gateEntryData.supplierZone = binding.tvSupplierZone.text.toString()
        gateEntryData.mtnCode = binding.tvMtntNoValue.text.toString()

        //Modify below to add Tentative no of bags
        gateEntryData.tempBagCount = binding.tvNoOfBagsValue.text.toString()

        gateEntryData.delivery = binding.tvObdNumber.text.toString()
        gateEntryData.approximateWeight = binding.etWeight.text.toString()
        gateEntryData.wsGate = WS01
        var plantDetails = getPlantDetails(gateEntryData.plantId).single()
        callBack?.replaceFragment(SUMMARY_FRAG, gateEntryData, plantDetails)
    }

    private fun getPlantDetails(plantId: String?): List<Plant> {
        return plantList.filter { it.plantId == plantId }
    }

    private fun updateMandatory() {
        binding.tvProductLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.product)) { mandatoryStars() } }
        binding.tvSupplierLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.supplier)) { mandatoryStars() } }
        binding.tvPlantLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.plant)) { mandatoryStars() } }
        binding.tvNoOfBagsLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.tentative_no_of_bags)) { mandatoryStars() } }
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
        binding.tvWeightLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.mtnt_weight)) { mandatoryStars() } }
        binding.tvMtntNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.mtnt_number)) { mandatoryStars() } }
    }

}
