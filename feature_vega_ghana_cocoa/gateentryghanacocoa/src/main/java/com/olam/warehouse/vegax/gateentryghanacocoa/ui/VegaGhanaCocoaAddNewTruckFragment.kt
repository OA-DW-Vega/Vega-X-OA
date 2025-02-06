package com.olam.warehouse.vegax.gateentryghanacocoa.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
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
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCocoaWBMultiPlants
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegaghana.entity.VehicleDetails
import com.olam.warehouse.presentation.data.domain.model.*
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTime
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTimeMillis
import com.olam.warehouse.presentation.utils.UIUtils.showSuccessDialog
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentryghanacocoa.R
import com.olam.warehouse.vegax.gateentryghanacocoa.databinding.FragmentVegaGhanaCocoaAddNewTruckBinding
import com.olam.warehouse.vegax.gateentryghanacocoa.utils.*
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VegaGhanaCocoaAddNewTruckFragment : BaseFragment(), VegaSingleSelectCommonListener,VegaSingleSelectListener {

    private lateinit var productNameList: List<VegaMaterial>
    private lateinit var supplierList: List<VegaVendor>
    private var callBack: CallBack? = null
    private var gateEntryData = VegaGateEntry()
    private var mtnsList = mutableListOf<VegaReceivingMtn>()
    private var productList = mutableListOf<VegaReceivingMtnLots>()
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var warehouselist = mutableListOf<VegaSupplyStorageLocation>()
    private var filteredWBList = mutableListOf<VegaReceivingMtn>()
    private var filteredWBListNew = mutableListOf<VegaReceivingMtn>()
    private var obdList = mutableListOf<VegaReceivingMtn>()
    private var obdListNew = mutableListOf<VegaReceivingMtn>()
    private val mtnrList = arrayListOf<String>()
    private var selectedStorageLoc: String? = ""
    private var truckData: TruckManagementVehicleResponse? = null
    private var currentSeason: String = ""
    private var truckNumber: String = ""
    private var weighbridgeIdList: List<String> = mutableListOf()
    private var wbList: List<VegaCocoaWBMultiPlants> = mutableListOf()
    private var weighbridgeIdMTNDetailsList: List<String> = mutableListOf()
    private var jsonData = mutableListOf<String>()
    private var plantList = ArrayList<String>()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    private var vehicleDetails = arrayListOf<VehicleDetails>()
    private var mWebridgeId: String = ""
    private var customDialog: VegaGhanaCommonSingleDiolog? = null
    private var customDialogGH: VegaCustomSingleSelectDialogWithSearch? = null

    interface CallBack {
        fun replaceFragment(paramsListFrag: String, item: VegaGateEntry)
    }

    private val vm: VegaGateEntryGhanaCocoaViewModel by viewModel()
    private lateinit var binding: FragmentVegaGhanaCocoaAddNewTruckBinding
    override val layoutResourceId = R.layout.fragment_vega_ghana_cocoa_add_new_truck

    companion object {
        fun newInstance(gateEntryData: VegaGateEntry) = VegaGhanaCocoaAddNewTruckFragment().putArgs {
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
        binding = FragmentVegaGhanaCocoaAddNewTruckBinding.inflate(layoutInflater)
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
//        if (gateEntryData.weighBridgeType == PROCURE) {
//            binding.llSupplier.visible()
//            binding.llMtnr.gone()
//        } else {
//            binding.llSupplier.gone()
//            binding.llMtnr.visible()
//        }

        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer { updateProcessType(it) })
        vm.getProcessTypeList(getCurrentKey())

        vm.material.observe(viewLifecycleOwner, Observer {
            bagTypeList = it.toMutableList()
            if (bagTypeList.size == 1) {
                gateEntryData.bagMaterialCode = bagTypeList[0].bagMaterialCode.toString().trim()
                gateEntryData.bagTareWeight = bagTypeList[0].tareWeight.toString().trim()
            }
        })
        vm.sdWaybil.observe(viewLifecycleOwner, Observer {
            updateUI(it)
        })

        vm.getMaterials()

        if (AppUtils.isOnline())
            vm.getSeasonDetails()
        else
            vm.getSeasonDetailsOffline()

        vm.seasonDetails.observe(viewLifecycleOwner, Observer {
            updateSeason(it)
        })

        vm.seasonDetailsOffline.observe(viewLifecycleOwner, Observer {
            vehicleDetails = it as ArrayList<VehicleDetails>
        })

        vm.truckDetails.observe(viewLifecycleOwner, Observer {
            updateTruckDetails(it)
        })


        binding.tvDate.text =
            getUTCDateTime(System.currentTimeMillis().toString(), App.getAppContext())
        binding.tvDate.setOnClickListener { getDatePickerDialog() }
        binding.btnConfirm.setOnClickListener { validateInputs() }
        binding.tvTruckIn.setOnClickListener {
            startActivityForResult(Intent(requireContext(), ScannerActivity::class.java), Constants.ACTIVITY_SCAN_REQUEST_CODE)
        }

        vm.product.observe(viewLifecycleOwner, Observer {
            productNameList= it
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
        })
        vm.getProducts()

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList=it
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
            gateEntryData.storageLocationCode = custonLocationList[0].procureLocationCode
        })
        vm.getCustomLocations()

       /* vm.weighBridge.observe(viewLifecycleOwner, Observer {
            handleWeighbridgeDetails(it)
        })*/

        //vm.trucks.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })

        vm.wbMultiPlant.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })

//        vm.weighBridgeId.observe(viewLifecycleOwner, Observer {
////            val supplierCode = it.data?.data?.supplierCode
////            if (!supplierCode.isNullOrEmpty()) {
////                vendorName = supplierList.single { it.vendorCode.equals(supplierCode) }.vendorName!!
////            }
//            updateTruckDetail(it.data?.data)
//        })

//        if (AppUtils.isOnline() && gateEntryData.weighBridgeType == STO) {
//            fetchMtnDetails()
//        }
        binding.tvProduct.setOnClickListener {
            showSingleSelectDialog(getString(R.string.select_product),true,false,false)
        }
        binding.tvSupplier.setOnClickListener { showSingleSelectDialog(getString(R.string.select_supplier),false,false,true) }
        binding.tvOBDNumber.setOnClickListener { showStoDialog(obdListNew) }
        binding.tvDispatchWarehouse.setOnClickListener {
        showSingleSelectDialog(getString(R.string.trans_Warehouse_popup),false,true,false)
      }
        binding.tvReceivingLocation.setOnClickListener {
            showSingleSelectDialog(getString(R.string.receiving_location_popup),false,false,false)
        }
    }

    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaCocoaWBMultiPlants>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.data?.let { it1 ->
                    if (it1.isNotEmpty()) {
                        wbList = it1
                        weighbridgeIdList = wbList.filter { it.vehNo.equals(truckNumber) }
                            .map { it.weighBridgeId.toString() }
                        weighbridgeIdMTNDetailsList = mtnsList.map { it.mtntWbid.toString() }

                        val commonWeighBridgeIdList =
                            weighbridgeIdList.intersect(weighbridgeIdMTNDetailsList)
                        filteredWBListNew.clear()
                        commonWeighBridgeIdList.forEach { it1 ->
                            filteredWBList =
                                mtnsList.filter { it.mtntWbid.equals(it1) } as MutableList<VegaReceivingMtn>
                            filteredWBListNew.addAll(filteredWBList)
                        }
                        obdListNew.clear()
                        filteredWBListNew.forEach { it1 ->
                            obdList =
                                    mtnsList.filter { it.mtnNumber.equals(it1.mtnNumber) } as MutableList<VegaReceivingMtn>
                            obdListNew.addAll(obdList)
                        }
                    }
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

   /* private fun handleWeighbridgeDetails(response: Resource<GenericReqAndResp<List<VegaReceiving>>>?) {
        response.let {
            when (it?.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response?.data?.let { it1 ->
                        var wbList = it1
                        weighbridgeIdList = wbList.data.filter { it.truckNo.equals(truckNumber) }.map { it.weighBridgeId }
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
                else -> {
                    // No Implementation
                }
            }
        }
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == Constants.ACTIVITY_SCAN_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let { validateId(it) }
            }
        }
    }

    private fun validateId(data: String) {
        if (AppUtils.isOnline()) {
            var validTruck = truckData?.vehicleList?.filter { it.qrCodeNumber == data }
            if (validTruck != null) {
                if (validTruck.isNotEmpty())
                    if (validTruck.size!! > 0)
                        validTruck.let { populateTruckData(it) }
                    else
                        showErrorDialogWithFAQLink(requireContext(), " Invalid Truck ID")
            }
        } else {
            var validTruck = vehicleDetails.filter { ((it.qrCodeNumber) == (data)) }
            if (validTruck.size > 0)
                validTruck.let { populateOfflineTruckData(it) }
            else
                showErrorDialogWithFAQLink(requireContext(), " Invalid Truck ID")
        }
    }

    private fun populateTruckData(validTruckData: List<VehicleData>) {
        if(validTruckData[0].driverDetails.size>0) {
            binding.tvDriverName.setText(validTruckData[0].driverDetails[0].driverName)
            binding.tvPhoneNo.setText(validTruckData[0].driverDetails[0].driverPhone)
        }
        binding.tvTruckNo.setText(validTruckData[0].vehicleNumber)
        binding.tvTruckIn.text = validTruckData[0].vehicleId
        binding.tvTransportVendorIn.text =
            validTruckData[0].vendor.vendorCode.plus("-").plus(validTruckData[0].vendor.name)
        truckNumber = validTruckData[0].vehicleNumber.trim()
        vm.warehouse.observe(viewLifecycleOwner, Observer { processApiResult(it) })
        vm.fetchWarehouseWithMtns()
    }

    private fun populateOfflineTruckData(validTruckData: List<VehicleDetails>) {
        binding.tvDriverName.setText(validTruckData[0].driverName)
        binding.tvTruckNo.setText(validTruckData[0].vehicleNumber)
        truckNumber = validTruckData[0].vehicleNumber.trim()
        binding.tvPhoneNo.setText(validTruckData[0].driverPhone)
        binding.tvTruckIn.text = validTruckData[0].vehicleId.toString()
        binding.tvTransportVendorIn.text =
            validTruckData[0].vendorCode.plus("-").plus(validTruckData[0].name)
        vm.warehouse.observe(viewLifecycleOwner, Observer { processApiResult(it) })
        vm.fetchWarehouseWithMtns()
    }

    /* private fun fetchMtnDetails() {
         vm.warehouse.observe(viewLifecycleOwner, Observer { processApiResult(it) })
         vm.fetchWarehouseWithMtns()
     }*/

    private fun updateSeason(response: Resource<TruckManagementSeasonResponse>?) {
        var seasonList = mutableListOf<TruckManagementData>()

        response.let {
            when (it?.status) {

                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response?.data?.let { it1 ->
                        seasonList = it1.seasonList as MutableList<TruckManagementData>
                    }
                    fetchCurrentSeason(seasonList)
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
                else -> {}
            }
        }
    }

    private fun updateTruckDetails(response: Resource<TruckManagementVehicleResponse>?) {

        response.let {
            when (it?.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response?.data?.let { it1 ->
                        truckData = it1
                    }
                   // fetchMtnDetails()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
                else -> {
                    // fetchMtnDetails()
                }
            }
        }
    }

    private fun fetchCurrentSeason(seasonData: List<TruckManagementData>) {
        val currentDate = Calendar.getInstance().time

        val sdf = SimpleDateFormat("yyyy-MM-dd")
        var seasonList = seasonData as MutableList<TruckManagementData>
//        seasonList.add(TruckManagementSeasonResponse("1","2019-10-01T00:00:00.000+0000","2020-10-01T00:00:00.000+0000","GH06","1"))
//        seasonList.add(TruckManagementSeasonResponse("2","2020-10-01T00:00:00.000+0000","2021-09-15T00:00:00.000+0000","GH06","1"))
        seasonList.forEach {
            var startDate = sdf.parse(it.startingPeriod.split("T")[0])
            var endDate = sdf.parse(it.endingPeriod.split("T")[0])

            if(currentDate.after(startDate) && currentDate.before(endDate)) {
                currentSeason = it.seasonID
                return@forEach
            }
        }
        if(currentSeason.isEmpty())
            context?.let { showSuccessDialog(it, getString(R.string.current_seasion_id_is_empty)) }
        else
            vm.getTruckDetails(currentSeason)
    }

    private fun processApiResult(data: Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> saveResult(it.data)
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun updateProcessType(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        jsonData.forEach {
            if (it.contains(JSON_UPCOUNTRY_PLANTLIST)) {
                var receiveLocation = (JSONObject(it).getJSONArray(JSON_UPCOUNTRY_PLANTLIST)).toString().split(",")
                receiveLocation.forEach { it1->
                    plantList.add(((it1.split(":")[0]).replace("[","").replace("\"", "").replace("]","")))
                }

            }
        }
    }

    private fun saveResult(data: GenericReqAndResp<VegaReceivingMtnWrapper>?) {
        hideLoading()
        data?.data?.let {
            mtnsList = it.mtns as MutableList<VegaReceivingMtn>
            productList = it.batchDetails as MutableList<VegaReceivingMtnLots>
            warehouselist = it.storageLocationLst as MutableList<VegaSupplyStorageLocation>
           // vm.getWeighBridgeDetail()
            //if (AppUtils.isOnline()) vm.getTruckList()
            var currentDate = DateUtils.formatDate(DateUtils.getDate())
             if (AppUtils.isOnline()) vm.getfetchWBListforMultiPlants(true, currentDate,DateUtils.formatDate(DateUtils.subtractingDaysToDate((DateUtils.getDate()), 12)),
                     plantList)
        }
    }

    private fun showStoDialog(mtnsList: MutableList<VegaReceivingMtn>) {
        //val data = mtnsList.distinct().filter { it.storageLocationCode == selectedStorageLoc }
        val mtnNumbers = mtnsList.map { item -> item.mtnNumber }
        val distinct = mtnNumbers.toSet().toList()
        mtnrList.clear()
        mtnsList.forEach {
            mtnrList.add(it.mtnNumber)
        }

        customDialog =
            VegaGhanaCommonSingleDiolog(
                getString(R.string.tittle_odb_popup),
                "sto",
                mtnsList.distinctBy { it.mtnNumber },
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)

        /*MaterialDialog(requireContext()).show {
            title(R.string.tittle_odb_popup)
            listItemsSingleChoice(items = distinct) { _, index, text ->
                binding.tvOBDNumber.text = text

               *//*var vegaReceivingMtnLots =  productList.filter { it.mtnNumber.equals((binding.tvOBDNumber.text.toString())) }.filter { it.posnr.equals(mtnsList.get(index).posnr.toString()) }
                var vegaReceivingMtn =  mtnsList.filter { it.mtnNumber.equals((binding.tvOBDNumber.text.toString())) }.filter { it.posnr.equals(mtnsList.get(index).posnr.toString()) }*//*

                var vegaReceivingMtnLots =  productList.filter { it.mtnNumber.equals((binding.tvOBDNumber.text.toString())) }
                var vegaReceivingMtn =  mtnsList.filter { it.mtnNumber.equals((binding.tvOBDNumber.text.toString())) }

                if(vegaReceivingMtnLots.isNotEmpty() && vegaReceivingMtnLots.size>0){
                    gateEntryData.materialCode = vegaReceivingMtnLots.get(0).materialNumber
                    gateEntryData.materialName = vegaReceivingMtnLots.get(0).materialName
                    gateEntryData.unitsOfMeasure = vegaReceivingMtnLots.get(0).uom
                    gateEntryData.mtnCode = vegaReceivingMtnLots.get(0).mtnNumber
                    gateEntryData.delivery = vegaReceivingMtnLots.get(0).mtnNumber
                    gateEntryData.deliveryItem = vegaReceivingMtnLots.get(0).posnr
                    gateEntryData.batchNumber = vegaReceivingMtnLots.get(0).batch
                    gateEntryData.purchaseDocNum =vegaReceivingMtnLots.get(0).purchaseOrder
                    gateEntryData.purchaseDocDesc = vegaReceivingMtnLots.get(0).ebelp
                    var totalWeight: Double? = 0.0
                    vegaReceivingMtnLots.forEach { it1 ->
                        if (it1.uom.equals("BAG")) {
                            totalWeight = ((totalWeight!!) + (it1.weight))
                        }
                    }
                    gateEntryData.bagCount = totalWeight?.formatThreeDigits()

                *//*
                    if(vegaReceivingMtnLots.get(0).uom.equals("BAG")){
                        gateEntryData.bagCount =
                            vegaReceivingMtnLots.get(0).weight.formatThreeDigits()
                    }*//*
                }
                if(vegaReceivingMtn.isNotEmpty() && vegaReceivingMtn.size>0) {
                    var vegaCocoaWBMultiPlants =
                        wbList.filter { it.weighBridgeId.equals((vegaReceivingMtn.get(0).mtntWbid)) }
                            .filter { it.vehNo.equals(truckNumber) }
                    mWebridgeId=vegaReceivingMtn.get(0).mtntWbid
                    if (vegaCocoaWBMultiPlants.isNotEmpty() && vegaCocoaWBMultiPlants.size > 0) {
                        var totalNetweight: Double? = 0.0
                        var totalGrossWeight: Double? = 0.0
                        vegaCocoaWBMultiPlants.forEach { it1 ->
                            if (gateEntryData.unitsOfMeasure.equals("BAG")) {
                                totalNetweight = ((totalNetweight!!) + (it1.netWeight?.toDouble()!!))
                                totalGrossWeight = ((totalGrossWeight!!) + (it1.grossWeight?.toDouble()!!))
                            }
                        }
                        if (gateEntryData.unitsOfMeasure.equals("BAG")) {
                            gateEntryData.netWeight = totalNetweight.toString()
                            gateEntryData.grossWeight = totalGrossWeight.toString()
                            gateEntryData.unitsOfMeasure = "KG"
                        }
                        gateEntryData.bagType = vegaCocoaWBMultiPlants.get(0).pmat1.toString()
                        //gateEntryData.tareWeight = vegaCocoaWBMultiPlants.get(0).netWeight.toString()
                        gateEntryData.bagWeight = gateEntryData.bagTareWeight?.let {
                            (gateEntryData.bagCount?.toDouble())?.times(it.toDouble()).toString()
                        }
                    }
                }
               // val mtn = data[index]
              *//*  gateEntryData.mtnCode = mtn.mtnNumber
                gateEntryData.delivery = mtn.mtnNumber
                gateEntryData.deliveryItem = mtn.posnr

                val batchList = productList.filter { it.mtnNumber == mtn.mtnNumber }
                if (batchList.size > 0) {
                    gateEntryData.batchNumber = batchList[0].batch
                    gateEntryData.purchaseDocNum = batchList[0].purchaseOrder
                    gateEntryData.purchaseDocDesc = batchList[0].ebelp
                }*//*


            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }*/
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
            binding.tvTruckIn.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_in))
            binding.tvTransportVendorIn.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_transport_vendor))
            binding.tvTruckNo.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
            binding.tvDriverNameLabel.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver_name))
            binding.tvPhoneNo.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_phone_no))
            binding.tvOBDNumber.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_obd_no))
            else -> {
                if (gateEntryData.weighBridgeType == PROCURE) {
                    when {
                        binding.tvSupplier.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_supplier))
//                        binding.tvSupplierZone.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_supplier_zone))
                        else -> vm.getSDWaybillNumber(mWebridgeId)
                    }

                } else if (gateEntryData.weighBridgeType == STO) {
                    when {
                        binding.tvOBDNumber.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_obd_no))
                        else -> vm.getSDWaybillNumber(mWebridgeId)
                    }
                } else vm.getSDWaybillNumber(mWebridgeId)

            }
        }
    }

    private fun moveToSummary(mSDWayBillNumber: String) {
        gateEntryData.contactNumber = binding.tvPhoneNo.text.toString()
        gateEntryData.erdat = getCurrentTimeInMills().toString()
      //      getUTCDateTimeMillis(binding.tvDate.text.toString(), App.getAppContext())
        gateEntryData.vehicleNumber = binding.tvTruckNo.text.toString()
        gateEntryData.driverName = binding.tvDriverName.text.toString()
        gateEntryData.truckDriverName = binding.tvDriverName.text.toString()
        gateEntryData.truckId = binding.tvTruckIn.text.toString()
        var transportDetails = binding.tvTransportVendorIn.text.toString().split("-")
        gateEntryData.transportVendorCode = transportDetails[0]
        gateEntryData.supplierCode = transportDetails[0]
        gateEntryData.transportVendorName = transportDetails[1]
//        gateEntryData.supplierZone = binding.tvSupplierZone.text.toString()
        gateEntryData.delivery = binding.tvOBDNumber.text.toString()
        // gateEntryData.approximateWeight = binding.etWeight.text.toString()
        gateEntryData.wsGate = WB01
        gateEntryData.challan = mSDWayBillNumber

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

    private fun updateUI(response: Resource<GenericReqAndResp<VegaCoffeeReceiving>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    moveToSummary(it.data?.data?.challan ?: "")
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    override fun clickOnItem(data: String, currentFlag: String) {
        customDialog?.dismiss()
        binding.tvOBDNumber.text = data

        /*var vegaReceivingMtnLots =  productList.filter { it.mtnNumber.equals((binding.tvOBDNumber.text.toString())) }.filter { it.posnr.equals(mtnsList.get(index).posnr.toString()) }
         var vegaReceivingMtn =  mtnsList.filter { it.mtnNumber.equals((binding.tvOBDNumber.text.toString())) }.filter { it.posnr.equals(mtnsList.get(index).posnr.toString()) }*/

        var vegaReceivingMtnLots =
            productList.filter { it.mtnNumber.equals((binding.tvOBDNumber.text.toString())) }
        var vegaReceivingMtn =
            mtnsList.filter { it.mtnNumber.equals((binding.tvOBDNumber.text.toString())) }

        if (vegaReceivingMtnLots.isNotEmpty() && vegaReceivingMtnLots.size > 0) {
            gateEntryData.materialCode = vegaReceivingMtnLots.get(0).materialNumber
            gateEntryData.materialName = vegaReceivingMtnLots.get(0).materialName
            gateEntryData.unitsOfMeasure = vegaReceivingMtnLots.get(0).uom
            gateEntryData.mtnCode = vegaReceivingMtnLots.get(0).mtnNumber
            gateEntryData.delivery = vegaReceivingMtnLots.get(0).mtnNumber
            gateEntryData.deliveryItem = vegaReceivingMtnLots.get(0).posnr
            gateEntryData.batchNumber = vegaReceivingMtnLots.get(0).batch
            gateEntryData.purchaseDocNum = vegaReceivingMtnLots.get(0).purchaseOrder
            gateEntryData.purchaseDocDesc = vegaReceivingMtnLots.get(0).ebelp
            var totalWeight: Double? = 0.0
            vegaReceivingMtnLots.forEach { it1 ->
                if (it1.uom.equals("BAG")) {
                    totalWeight = ((totalWeight!!) + (it1.weight))
                }
            }
            gateEntryData.bagCount = totalWeight?.formatThreeDigits()

            /*
                if(vegaReceivingMtnLots.get(0).uom.equals("BAG")){
                    gateEntryData.bagCount =
                        vegaReceivingMtnLots.get(0).weight.formatThreeDigits()
                }*/
        }
        if (vegaReceivingMtn.isNotEmpty() && vegaReceivingMtn.size > 0) {
            var vegaCocoaWBMultiPlants =
                wbList.filter { it.weighBridgeId.equals((vegaReceivingMtn.get(0).mtntWbid)) }
                    .filter { it.vehNo.equals(truckNumber) }
            mWebridgeId = vegaReceivingMtn.get(0).mtntWbid
            if (vegaCocoaWBMultiPlants.isNotEmpty() && vegaCocoaWBMultiPlants.size > 0) {
                var totalNetweight: Double? = 0.0
                var totalGrossWeight: Double? = 0.0
                vegaCocoaWBMultiPlants.forEach { it1 ->
                    if (gateEntryData.unitsOfMeasure.equals("BAG")) {
                        totalNetweight = ((totalNetweight!!) + (it1.netWeight?.toDouble()!!))
                        totalGrossWeight = ((totalGrossWeight!!) + (it1.grossWeight?.toDouble()!!))
                    }
                }
                if (gateEntryData.unitsOfMeasure.equals("BAG")) {
                    gateEntryData.netWeight = totalNetweight.toString()
                    gateEntryData.grossWeight = totalGrossWeight.toString()
                    gateEntryData.unitsOfMeasure = "KG"
                }
                gateEntryData.bagType = vegaCocoaWBMultiPlants.get(0).pmat1.toString()
                //gateEntryData.tareWeight = vegaCocoaWBMultiPlants.get(0).netWeight.toString()
                gateEntryData.bagWeight = gateEntryData.bagTareWeight?.let {
                    (gateEntryData.bagCount?.toDouble())?.times(it.toDouble()).toString()
                }
            }
        }
    }

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {
        customDialogGH?.dismiss()
         if(isGrade){
             binding.tvProduct.text= data
             productNameList.forEach { material ->
                 if (material.materialName == binding.tvProduct.text.toString()) {
                     gateEntryData.materialCode =
                         MATERIAL_CODE.plus(material.materialCode)
                     gateEntryData.materialName = material.materialName.toString()
                     binding.tvUom.text = material.unitsOfMeasure.toString()
                     gateEntryData.unitsOfMeasure = material.unitsOfMeasure.toString()
                 }
             }

         }else if(isVendor){
             binding.tvSupplier.text= data
             supplierList.forEach { vendor ->
                 if (vendor.vendorCode == binding.tvSupplier.text.toString().split(" - ")[0].trim()) {
                     gateEntryData.supplierCode = vendor.vendorCode
                     gateEntryData.supplierName = vendor.vendorName
                     vendor.bcApprover?.let { it1 -> vm.getSupplierZone(it1) }
                 }
             }
         }else if(isWh){
             binding.tvDispatchWarehouse.text = data
             warehouselist.forEach {
               if(it.storageLocationCode == data.split("-")[0].trim())  {
                   binding.tvObdNumber.text = ""
                   val item = it.storageLocationCode
                   selectedStorageLoc = item
                   gateEntryData.plantId = UIUtils.getWarehouseId()
                   gateEntryData.supplierCode = it.plant
                   gateEntryData.supplierName = data.split("-")[1].trim()
               }
             }
         }else{
             binding.tvReceivingLocation.text = data
             val customLocation=  custonLocationList.filter { !it.storageLocationType.equals("B") }
             customLocation.forEach {
                 if(it.procureLocationCode == data.split("-")[0].trim()){
                     gateEntryData.storageLocationCode = it.procureLocationCode
                     gateEntryData.storageLocationName = it.procureLocationName
                 }
             }
         }
    }


    private fun showSingleSelectDialog(title: String, isGrade: Boolean,isWh:Boolean,isVendor: Boolean) {
        val list: List<String>

        if(isGrade){
            list = productNameList.map { data -> data.materialName!! }
        }else if(isVendor){
            list = supplierList.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
        }else if(isWh){
            list = warehouselist.map { data -> data.storageLocationCode.plus("-").plus(data.storageLocationName) }

        }else{
            list =
                custonLocationList.filter { !it.storageLocationType.equals("B") }
                    .map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }
        }


        customDialogGH =
            VegaCustomSingleSelectDialogWithSearch(
                title =  title,
                isWh = isWh,isVendor=isVendor,isGrade= isGrade,
                items = list,
                activity = requireActivity(),
                listener = this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }


}
