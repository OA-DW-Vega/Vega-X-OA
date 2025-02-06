package com.olam.warehouse.vegax.gateentrycameroon.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.gson.Gson
import com.olam.warehouse.login.ui.common.VegaTrackTraceFragment
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.TrackTraceFarmerModel
import com.olam.warehouse.master.vega.entity.TrackTraceSourceLotDetails
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaReceivingMtn
import com.olam.warehouse.master.vega.entity.VegaReceivingMtnLots
import com.olam.warehouse.master.vega.entity.VegaSupplyStorageLocation
import com.olam.warehouse.master.vega.entity.VegaTrackTraceFarmerData
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTime
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTimeMillisCameroon
import com.olam.warehouse.presentation.utils.LocaleHelper
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentrycameroon.R
import com.olam.warehouse.vegax.gateentrycameroon.databinding.FragmentVegaCameroonAddNewTruckBinding
import com.olam.warehouse.vegax.gateentrycameroon.utils.GATE_ENTRY_DATA
import com.olam.warehouse.vegax.gateentrycameroon.utils.PROCURE
import com.olam.warehouse.vegax.gateentrycameroon.utils.PRODUCT
import com.olam.warehouse.vegax.gateentrycameroon.utils.STO
import com.olam.warehouse.vegax.gateentrycameroon.utils.SUMMARY_FRAG
import com.olam.warehouse.vegax.gateentrycameroon.utils.SUPPLIER
import com.olam.warehouse.vegax.gateentrycameroon.utils.WS01
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

class VegaCameroonAddNewTruckFragment : BaseFragment() , VegaSingleSelectCommonListener {

    private var callBack: CallBack? = null
    private var gateEntryData = VegaGateEntry()
    private var mtnsList = mutableListOf<VegaReceivingMtn>()
    private var productList = mutableListOf<VegaReceivingMtnLots>()
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var plantList = mutableListOf<Plant>()
    private var warehouselist = mutableListOf<VegaSupplyStorageLocation>()
    private var selectedStorageLoc: String? = ""
    private var productLists = ArrayList<String>()
    private var supplierLists = ArrayList<String>()
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null
    private var materials = ArrayList<VegaMaterial>()
    private var supplierList = mutableListOf<VegaVendor>()

    /*Track & Trace*/
    var ttProcurementType = ""
    var ttComplaintType = ""
    var ttDirectFarmerDataList = ArrayList<TrackTraceFarmerModel>()
    var ttIndirectSourceLotDetails = TrackTraceSourceLotDetails()
    var ttDbFarmerList = mutableListOf<VegaTrackTraceFarmerData>()
    var isTTComplaint = false
    var ttVendorFlag = false

    interface CallBack {
        fun replaceFragment(
            paramsListFrag: String,
            item: VegaGateEntry,
            plantDetails: Plant
        )
    }

    private val vm: VegaGateEntryCameroonViewModel by viewModel()
    private lateinit var binding: FragmentVegaCameroonAddNewTruckBinding
    override val layoutResourceId = R.layout.fragment_vega_cameroon_add_new_truck

    companion object {
        fun newInstance(gateEntryData: VegaGateEntry, procurementType: String, complaintType: String) = VegaCameroonAddNewTruckFragment().putArgs {
            putParcelable(GATE_ENTRY_DATA, gateEntryData)
            putString(Constants.PROCUREMENT_TYPE, procurementType)
            putString(Constants.COMPLAINT_TYPE, complaintType)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCameroonAddNewTruckBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("gateentrycameroon/ui/VegaCameroonAddNewTruckFragment")
            .title("Vega_Cameroon/Gate Entry").with(tracker)
        initUI()
    }

    private fun initUI() {
        gateEntryData = arguments?.getParcelable(GATE_ENTRY_DATA)!!
//        ttProcurementType = arguments?.getString(Constants.PROCUREMENT_TYPE)?:""
//        ttComplaintType = arguments?.getString(Constants.COMPLAINT_TYPE)?:""
        updateMandatory()
        if (gateEntryData.weighBridgeType == PROCURE) {
            binding.llSupplier.visible()
            binding.llMtnr.gone()
        } else {
            binding.llSupplier.gone()
            binding.llMtnr.visible()
        }
        /*if(ttProcurementType.isNotEmpty()) {
            loadTTFragment()
        }*/
        binding.tvDate.text =
            getUTCDateTime(System.currentTimeMillis().toString(), App.getAppContext())
        binding.tvDate.setOnClickListener { getDatePickerDialog() }
        binding.btnConfirm.setOnClickListener { validateInputs() }

//        vm.product.observe(viewLifecycleOwner, Observer {
//            val products = it.map { data -> data.materialName }
//            productLists = products as ArrayList<String>
//            materials = it as ArrayList<VegaMaterial>
//            it.forEach { it1 ->
//                if (it1.materialName.equals("Ivory Coast Raw Cashew Nut")) {
//                    gateEntryData.materialCode =
//                        MATERIAL_CODE.plus(it1.materialCode.toString())
//                    gateEntryData.materialName = it1.materialName.toString()
//                    binding.tvProduct.setText(it1.materialName, TextView.BufferType.EDITABLE)
//                    binding.tvUom.text = it1.unitsOfMeasure.toString()
//                    gateEntryData.unitsOfMeasure = it1.unitsOfMeasure.toString()
//                }
//            }
           /* val productAdapter =
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
            }*/
//        })
//        vm.getProducts()

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList = it.toMutableList()
            val suppliers = it
                .map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            supplierLists = suppliers as ArrayList<String>
         /*   val supplierAdapter =
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
            }*/
        })
        vm.getSuppliers()

        vm.suppplierZone.observe(viewLifecycleOwner, Observer {
            it?.let { item ->
                if (it.size > 0) {
                    binding.tvSupplierZone.text = item[0].bczone.toString()
                    binding.tvSupplierZone.isEnabled = false
                }
            }

        })

        vm.getFarmerList()
        vm.farmerList.observe(viewLifecycleOwner, Observer {
            ttDbFarmerList = it.toMutableList()
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
        binding.tvSupplier.setOnClickListener {
            showSingleSelectDialog(getString(R.string.select_supplier), SUPPLIER )
        }

//        binding.tvProduct.setOnClickListener {
//            showSingleSelectDialog(getString(R.string.select_material), PRODUCT)
//        }
    }

    /*private fun loadTTFragment(){
        val bundle = Bundle()
        bundle.putString(
            Constants.PROCUREMENT_TYPE, ttProcurementType)
        displayFragment(VegaTrackTraceFragment.newInstance(bundle), false)
    }
    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            "",
            allowStateLoss = true,
            containerViewId = R.id.flTTContainer,
            allowBackStack = flag
        )
    }

    fun replaceFragment(
        fragment: Fragment,
        tag: String?,
        allowStateLoss: Boolean = false,
        @IdRes containerViewId: Int,
        @AnimRes enterAnimation: Int = 0,
        @AnimRes exitAnimation: Int = 0,
        @AnimRes popEnterAnimation: Int = 0,
        @AnimRes popExitAnimation: Int = 0,
        allowBackStack: Boolean = false
    ) {
        val ft = activity?.supportFragmentManager
            ?.beginTransaction()
            ?.setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
        when {
            allowBackStack -> {
                ft?.add(containerViewId, fragment, tag)
                ft?.addToBackStack(tag)
            }
            else -> ft?.replace(containerViewId, fragment, tag)
        }
        if (!activity?.supportFragmentManager?.isStateSaved!!) {
            ft?.commit()
        } else if (allowStateLoss) {
            ft?.commitAllowingStateLoss()
        }
    }*/

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
            datePicker.show()
            datePicker.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        }

    }

    private fun validateInputs() {
        when {
//            gateEntryData.materialCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_product))
//            binding.tvProduct.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_product))
            binding.tvDate.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_date))
            binding.tvReceivingLocation.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_location))
            binding.tvTruckNo.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
            binding.tvDriverName.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver_name))
            binding.tvMtntNoValue.text.isNullOrEmpty()-> showSnack(getString(R.string.error_valid_mtnt_no))
            binding.etWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_weight))
//            binding.tvNoOfBagsValue.text.isNullOrEmpty()-> showSnack(getString(R.string.error_no_of_bags))
            binding.tvPhoneNo.text.isNotEmpty() && binding.tvPhoneNo.text.toString().toLong() == 0L -> showSnack(getString(R.string.error_driver_phone_no))
            binding.tvNoOfBagsValue.text.isNotEmpty() && binding.tvNoOfBagsValue.text.toString().toLong() == 0L -> showSnack(getString(R.string.error_no_of_bags))
            binding.tvMtntNoValue.text.isNotEmpty() && binding.tvMtntNoValue.text.toString().toLong() == 0L -> showSnack(getString(R.string.error_valid_mtnt_no))
            binding.etWeight.text.isNotEmpty() && binding.etWeight.text.toString().toBigInteger() == BigInteger.ZERO  -> showSnack(getString(R.string.error_valid_weight))

            else -> {
                if (gateEntryData.weighBridgeType == PROCURE) {
                    when {
                        binding.tvSupplier.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_supplier))
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
        //getUTCDateTime(System.currentTimeMillis().toString(), App.getAppContext())
        //gateEntryData.erdat = binding.tvDate.text.toString()
        gateEntryData.erdat =
            getUTCDateTimeMillisCameroon(binding.tvDate.text.toString(), App.getAppContext())
        gateEntryData.vehicleNumber = binding.tvTruckNo.text.toString()
        gateEntryData.driverName = binding.tvDriverName.text.toString()
        gateEntryData.truckDriverName = binding.tvDriverName.text.toString()
        gateEntryData.mtnCode = binding.tvMtntNoValue.text.toString()

        //Modify below to add Tentative no of bags
        gateEntryData.tempBagCount = binding.tvNoOfBagsValue.text.toString()

        gateEntryData.delivery = binding.tvObdNumber.text.toString()
        gateEntryData.approximateWeight = binding.etWeight.text.toString()
        gateEntryData.wsGate = WS01
       /* *//*Track & Trace*//*
        if(ttProcurementType.equals(Constants.DIRECT)){
            gateEntryData.ttFarmerList = getTTFarmerDataList()
            gateEntryData.eudrStatus = isTTComplaint

        } else if(ttProcurementType.equals(Constants.IN_DIRECT)) {
            gateEntryData.sourceLotId = ttIndirectSourceLotDetails?.sourceLotId.toString()
            gateEntryData.eudrStatus = ttIndirectSourceLotDetails?.isEudrComplaintFlag ?: false
        }*/
        var plantDetails = getPlantDetails(gateEntryData.plantId).single()
        callBack?.replaceFragment(SUMMARY_FRAG, gateEntryData, plantDetails)
    }

   /* private fun getTTFarmerDataList():ArrayList<TrackTraceFarmerModel>{
        if(ttDirectFarmerDataList.isNotEmpty() && ttDirectFarmerDataList.size > 0) {
            if (AppUtils.isOnline()) {
                return ttDirectFarmerDataList
            } else{
                ttDirectFarmerDataList.forEach {
                    it.tmpWbId = gateEntryData.tmpWbId
                }
                return ttDirectFarmerDataList
            }
        }
        return ttDirectFarmerDataList
    }*/

    private fun getPlantDetails(plantId: String?): List<Plant> {
        return plantList.filter { it.plantId == plantId }
    }

    private fun updateMandatory() {
//        binding.tvProductLabel.text =
//            with(UIUtils) { with(requireContext().resources.getString(R.string.product)) { mandatoryStars() } }
        binding.tvSupplierLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.supplier)) { mandatoryStars() } }
        binding.tvPlantLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.plant)) { mandatoryStars() } }
        binding.tvNoOfBagsLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.tentative_no_of_bags)) { mandatoryStars() } }

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

    private fun showSingleSelectDialog(title: String,currentFalg: String) {
        var list = ArrayList<String>()
        when(currentFalg) {
            SUPPLIER -> {
                list = supplierLists
            }
            PRODUCT -> {
                if(ttProcurementType.isNotEmpty() && ttProcurementType.equals(Constants.DIRECT)) {
                    if (gateEntryData.supplierCode?.isEmpty() == true) {
                        showSnack("Kindly select supplier")
                        return
                    }
                }
                if(ttProcurementType.isNotEmpty()) {
                    if (isTTComplaint) {
                        list = materials.filter { it.complainceFlag.equals(Constants.COMPLAINT) }
                            .map { it.materialCode.plus("-").plus(it.materialName) } as java.util.ArrayList<String>
                    } else {
                        list = materials.filter { it.complainceFlag.equals(Constants.NON_COMPLAINT) }
                            .map { it.materialCode.plus("-").plus(it.materialName) } as java.util.ArrayList<String>
                    }
                    if (list.isEmpty()) {
                        list = materials.map {
                            it.materialCode.plus("-").plus(it.materialName)
                        } as java.util.ArrayList<String>
                    }
                } else {
                    list = materials.map {
                        it.materialCode.plus("-").plus(it.materialName)
                    } as java.util.ArrayList<String>
                }
//                list = productLists
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
            SUPPLIER -> {
                binding.tvSupplier.text = data
                var list = supplierList.filter { it.vendorCode == binding.tvSupplier.text.toString().split(" - ")[0] }
               list.forEach {
                   vendor ->
                   gateEntryData.supplierCode = vendor.vendorCode
                   gateEntryData.supplierName = vendor.vendorName
                   vendor.bcApprover?.let { it1 -> vm.getSupplierZone(it1) }
               }
               /* if(ttProcurementType.isNotEmpty()){
                    if(ttProcurementType.equals(Constants.DIRECT)) {
                        validateFarmer(gateEntryData.supplierName?: "")
                    }
                }*/

            }
            /*PRODUCT -> {
                binding.tvProduct.text = data
                var list = materials.filter { it.materialName == data.split("-")[1] }
                list.forEach { material ->
                    gateEntryData.materialCode =
                        MATERIAL_CODE.plus(material.materialCode.toString())
                    gateEntryData.materialName = material.materialName.toString()
                    binding.tvUom.text = material.unitsOfMeasure.toString()
                    gateEntryData.unitsOfMeasure = material.unitsOfMeasure.toString()
                }
            }*/
        }
    }

    /*fun isVendor(flag: Boolean){
        binding.tvEudrStatus.gone()
        if(flag) {
            binding.llSupplier.gone()
            ttVendorFlag = true
        } else {
            binding.llSupplier.visible()
            ttVendorFlag = false
        }
    }

    *//*Track & Trace*//*
    fun updateFarmerListDetails(farmerList: ArrayList<TrackTraceFarmerModel>){
        ttDirectFarmerDataList.clear()
        ttDirectFarmerDataList.addAll(farmerList)
        if(ttDirectFarmerDataList.isNotEmpty()) {
            gateEntryData.supplierCode = ttDirectFarmerDataList.get(0).supplier?.split("-")?.get(0)
            var result = ttDirectFarmerDataList.any { it.isComplaint == 0 }
            if(result){
                isTTComplaint = false
            }else{
                isTTComplaint = true
            }
        } else {
            isTTComplaint = false
        }
        updateEudrStatus()
    }

    *//*Track & Trace*//*
    fun updateEudrStatus(){
        if(ttProcurementType.equals(Constants.DIRECT)) {
            binding.tvEudrStatus.visible()
            binding.tvEudrStatus.text =
                if (isTTComplaint) "Eudr Status : ${Constants.COMPLAINT}" else "Eudr Status : ${Constants.ATTR_UNKNOWN_QP_VALUE}"
        }
    }

    *//*Track & Trace*//*
    fun isComplaint(status:Int){
        if(status == 0){
            isTTComplaint = false
        } else {
            isTTComplaint = true
        }
    }

    *//*Track & Trace*//*
    fun updateSourceLotDetails(sourceLotDetails: TrackTraceSourceLotDetails){
        isTTComplaint = sourceLotDetails?.isEudrComplaintFlag?:false
        this.ttIndirectSourceLotDetails = sourceLotDetails
        if(sourceLotDetails.vendorCode?.isNotEmpty() == true){
            var checkSupplier = supplierList.filter { it.vendorCode.equals(sourceLotDetails.vendorCode) }
            if(checkSupplier.isNullOrEmpty()){
                showOkDialog("The vendor details in the source lot is not available in SAP, do you want to continue?")
            } else {
                var data = checkSupplier.get(0).vendorCode.plus(" - ").plus(checkSupplier.get(0).vendorName)
                binding.tvSupplier.text = data
//                receivingData.supplierCode = data.split("-")[0].trim()
//                receivingData.supplierName = data.split("-")[1].trim()
//                clearPurchaseOrder()
            }
        } else {
            showOkDialog("The vendor details in the source lot is not available in SAP, do you want to continue?")
        }
    }

    private fun validateFarmer(farmerName: String){
        var localList = mutableListOf<VegaTrackTraceFarmerData>()
        ttDbFarmerList.forEach {
            if(it.farmerName.contains(farmerName)){
                localList.add(it)
            }
        }
        if(localList.size > 1){
            chooseOneLotDialog(localList)
        } else if(localList.size == 1){
            if(localList.get(0).isComplaint == 1){
                isTTComplaint = true
            }
        } else{
            isTTComplaint = false
        }
        updateEudrStatus()
    }

    @SuppressLint("CheckResult")
    private fun chooseOneLotDialog(farmerList: List<VegaTrackTraceFarmerData>) {
        val farmerItem = farmerList.map { it.farmerName.plus(" : ").plus(it.farmerId) }
        MaterialDialog(requireContext()).show {
            message(R.string.choose_farmer)
            cancelOnTouchOutside(false)
            cancelable(false)
            listItemsSingleChoice(items = farmerItem) { _, index, text ->
//                context.toast("dfedf")
                gateEntryData.supplierCode = text.split("-")[0]
                gateEntryData.supplierName = text.split("-")[1]
                binding.tvSupplier.text = text
                if(farmerList.get(index).isComplaint == 1){
                    isTTComplaint = true
                } else {
                    isTTComplaint = false
                }
                updateEudrStatus()
            }
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.login.R.string.ok),
                    true
                )
            )
        }
    }*/



}
