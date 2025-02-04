package com.olam.warehouse.vegax.mtntghanacocoa.ui.weighscale

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaghana.entity.VehicleDetails
import com.olam.warehouse.presentation.data.domain.model.*
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.mtntghanacocoa.R
import com.olam.warehouse.vegax.mtntghanacocoa.data.domain.model.VegaGhanaMtntPurchaseOrder
import com.olam.warehouse.vegax.mtntghanacocoa.data.domain.model.VegaGhanaMtntPurchaseOrders
import com.olam.warehouse.vegax.mtntghanacocoa.databinding.FragmentGhanaCocoaWeighscaleConsignmentLayoutBinding
import com.olam.warehouse.vegax.mtntghanacocoa.ui.VegaGhanaCocoaCustomSingleSelectDialog
import com.olam.warehouse.vegax.mtntghanacocoa.ui.VegaGhanaCocoaMtntViewModel
import com.olam.warehouse.vegax.mtntghanacocoa.ui.VegaGhanaCocoaReplaceFragmentCallback
import com.olam.warehouse.vegax.mtntghanacocoa.ui.VegaGhanaCocoaSingleSelectListener
import com.olam.warehouse.vegax.mtntghanacocoa.utils.*
import kotlinx.android.synthetic.main.fragment_ghana_cocoa_port_consignment_layout.*
import kotlinx.android.synthetic.main.fragment_ghana_cocoa_port_consignment_layout.tvnoofbagsValue
import kotlinx.android.synthetic.main.fragment_ghana_cocoa_port_consignment_layout.tvproductValue
import kotlinx.android.synthetic.main.fragment_ghana_cocoa_weighscale_consignment_layout.*
import kotlinx.android.synthetic.main.item_ghana_cocoa_material_layout.view.*
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt
import kotlin.random.Random

class VegaGhanaCocoaCocoaMtntConsignmentFragment : BaseFragment() ,
    VegaGhanaCocoaSingleSelectListener {
    private var uomDetails = ArrayList<VegaUomDetails>()
    private var truckData : TruckManagementVehicleResponse? = null
    private var currentSeason: String = ""
    override val layoutResourceId = R.layout.fragment_ghana_cocoa_weighscale_consignment_layout
    private lateinit var binding: FragmentGhanaCocoaWeighscaleConsignmentLayoutBinding

    private val vm: VegaGhanaCocoaMtntViewModel by viewModel()
    private var model: VegaCocoaDispatchWB? = null
    private var supplierList = mutableListOf<VegaVendor>()
    private var materialList = mutableListOf<VegaMaterial>()
    private var materialStorageLocationList = mutableListOf<VegaStorageLocationDetail>()
    private var cocoaCustomDialog: VegaGhanaCocoaCustomSingleSelectDialog? = null
    private var purchaseOrderList = mutableListOf<VegaGhanaMtntPurchaseOrder>()
    private var materialCode: String = ""
    private var storageLocation = ArrayList<String>()
    private var STONumbers = ArrayList<String>()
    private var filteredPurchaseOrderList = ArrayList<VegaGhanaMtntPurchaseOrders>()
    private var wareHouseId = ""
    private var vehicleDetails = arrayListOf<VehicleDetails>()
    private var selectedPurchaseOrder: VegaGhanaMtntPurchaseOrders? = null
    private var materials: ArrayList<VegaGhanaMtntPurchaseOrders>? = null
    private var purchaseOrder = ArrayList<VegaGhanaMtntPurchaseOrders>()
    private lateinit var callbackCocoa: VegaGhanaCocoaReplaceFragmentCallback
    private var materialModelList: ArrayList<VegaGhanaPurchaseOrderMaterialModel> = ArrayList()
    private var productList = emptyList<VegaMaterial>()
    private var productnameList = ArrayList<String>()
    private var plantRouteList = emptyList<VegaPlanRoute>()
    private var departurePointList = ArrayList<String>()
    private var sendingWareHouseList = mutableListOf<VegaCustomStLocation>()
    private var selectedSendingWH = ""
    private var selectedDestinationWH = ""
    private var loggedInPlantId: String = ""
    private var stocksList = mutableListOf<VegaCocoaRminLots>()
    private var offlineStocksList = mutableListOf<VegaEcuadorDispatchStocks>()
    private var selectedDestinationWHName = ""
    private var jsonData = mutableListOf<String>()
    private var receivingLocationValue :String? =""
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbackCocoa = context as VegaGhanaCocoaReplaceFragmentCallback
    }

    companion object {
        fun newInstance() =
            VegaGhanaCocoaCocoaMtntConsignmentFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentGhanaCocoaWeighscaleConsignmentLayoutBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        binding.tvTruckNoValue.isEnabled = false

        /*var driver = DriverData("1012","Rajesh","04423489023","GH192345678")
        var driverList = ArrayList<DriverData>()
        driverList.add(driver)

        var vendor = Vendor("0002008455","MUNTARU SAKIBU TRANSPORT")


        var vehicle = VehicleData("1541","GH1989012",driverList,vendor,"","","")
        var vehicleList = ArrayList<VehicleData>()
        vehicleList.add(vehicle)
        truckData = TruckManagementVehicleResponse("1541", vehicleList)*/




        vm.storageLocationList.observe(this, Observer { updatestorageLocationList(it) })
        vm.getStorageLocationDetail()

        vm.getSuppliers()

        binding.tvWhValue.setOnClickListener {
            showSingleSelectDialog(true, getString(R.string.select_dest_wh), false)
        }
        binding.tvnoofbagsValue.onChange { text ->
            when {
                text.isEmpty() -> {

                }
                else -> {
                    if (AppUtils.isOnline()) {
                        updateStockDetails()
                    } else {
                        updateofflineStockDetails()
                    }

                }
            }

        }
        /* binding.tvDeparturePointValue.setOnClickListener {
             showSingleSelectDialog(
                 false,
                 getString(R.string.select_departure_point), false
             )
         }*/

        vm.stocks.observe(viewLifecycleOwner, Observer { updateUI(it) })

        vm.material.observe(viewLifecycleOwner, Observer {
            bagTypeList = it.toMutableList()
            if (bagTypeList.size == 1) {
//                binding.clSupplierInfo.tvBagTypeName.setText(bagTypeList[0].bagType)
                vm.dispatchWh.bagType = bagTypeList[0].bagType
                vm.dispatchWh.bagMaterialCode = bagTypeList[0].bagMaterialCode
                vm.dispatchWh.tareWeight = bagTypeList[0].tareWeight
                vm.dispatchWh.unitsOfMeasure = bagTypeList[0].unitsOfMeasure
            }
        })
        vm.getMaterials()

        vm.seasonDetails.observe(viewLifecycleOwner, Observer {
            updateSeason(it)
        })

        vm.seasonDetailsOffline.observe(viewLifecycleOwner, Observer {
            vehicleDetails = it as ArrayList<VehicleDetails>
        })

        vm.truckDetails.observe(viewLifecycleOwner, Observer {
            updateTruckDetails(it)
        })

        /*binding.tvSendingWHValue.setOnClickListener {
            if (sendingWareHouseList.size > 1) {
//                binding.tvSendingWHValue.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.ic_add_icon, 0)
                showSingleSelectDialog(
                    false,
                    getString(R.string.select_sent_wh), true
                )
            }
        }*/

        if (AppUtils.isOnline()){
            vm.getSeasonDetails()
//            vm.getPurchaseOrder("")
            }
        else
            vm.getSeasonDetailsOffline()

//        vm.getPurchaseOrder("")
        binding.btnProceed.setOnClickListener { validateProceed() }
        enableProceed()
        vm.purchaseOrder.observe(viewLifecycleOwner, Observer { updatePurchaseOrder(it) })
        vm.purchaseOrderOffline.observe(viewLifecycleOwner, Observer { updatePurchaseOrderOffline(it) })
        binding.tvTruckIdValue.setOnClickListener {
            startActivityForResult(Intent(requireContext(), ScannerActivity::class.java), Constants.ACTIVITY_SCAN_REQUEST_CODE)
        }
        binding.tvDriverNameValue.onChange { enableProceed() }
        binding.tvTruckNoValue.onChange { enableProceed() }
        binding.tvLicenseNumberValue.onChange { enableProceed() }
        binding.tvDriverNoValue.onChange { enableProceed() }
        binding.tvnoofbagsValue.onChange { enableProceed() }
        /*binding.tvproductValue.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.select_material_name),
                true
            )
        }*/

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            if (it != null)
                supplierList = it as MutableList
        })
        vm.getAllProduct()
        vm.allProduct.observe(
            viewLifecycleOwner,
            Observer {
                if (it != null) {
                    productList = it
                    productList.forEach {  it1 ->
                        productnameList.add(it1.materialCode.plus("-").plus(it1.materialName))
                    }
                }
            })

        /*vm.materialStorgeSourceValue.observe(viewLifecycleOwner, Observer {
            materialStorageLocationList = it.toMutableList()
        })
        vm.getMaterialStorageLocation()*/

        vm.truck.observe(this, Observer {
            if (it != null && it.size > 0) {
                binding.llOffline.visible()
            } else {
                binding.llOffline.gone()
            }
        })
        vm.getOfflineTruckDetails()
        binding.llOffline.setOnClickListener { moveToOfflineSummary() }

        vm.stocksOffline.observe(viewLifecycleOwner, Observer { item ->
            //  var tempList = item.filter { !it.batchNumber.startsWith("Z") }

            offlineStocksList.clear()
            offlineStocksList.addAll(item)
            totalWeight(stocksList, offlineStocksList)

        })

        vm.uomDetail.observe(viewLifecycleOwner, Observer {
            uomDetails = it as ArrayList<VegaUomDetails>
        })
        vm.getUomDetails()

        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer { updateProcessType(it) })
        vm.getProcessTypeList(getCurrentKey())

        vm.allPlantRoute.observe(
            viewLifecycleOwner,
            Observer {
                plantRouteList = it.filter { it.sourceLocCode.equals(selectedSendingWH) }
                plantRouteList.forEach {
                    if(plantRouteList.size>0) {
                        binding.tvRouteValue.text =
                            it.routeLocCode.plus(" - ").plus(it.routeLocationName)
                        departurePointList.add(it.departureLocCode.plus(" - ").plus(it.departureLocName))
                    }
                }
                val supplierAdapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, departurePointList)
                binding.tvDeparturePointValue.threshold = 1
                binding.tvDeparturePointValue.setAdapter(supplierAdapter)
                binding.tvDeparturePointValue.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->

                }
            })

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            sendingWareHouseList = it.toMutableList()
            if (sendingWareHouseList.size == 1) {
                sendingWareHouseList.forEach {
                    binding.tvSendingWHValue.text =
                        sendingWareHouseList[0].procureLocationCode.plus(" - ").plus(
                            sendingWareHouseList[0].procureLocationName
                        )
                    selectedSendingWH = sendingWareHouseList[0].procureLocationCode
                    loggedInPlantId = (sendingWareHouseList[0].plant).toString()

                    val data = materialStorageLocationList.filter {
                        it.storageLocationCode.equals(selectedSendingWH)
                    }
                    if (data.size > 0) {
                        val materialData =
                            productList.filter { ((MATERIAL_CODE.plus(it.materialCode)).equals(data[0].materialCode)) }
                        if (materialData.size > 0) {
                            tvproductValue.text = materialData[0].materialCode.plus("-")
                                .plus(materialData[0].materialName)
                            if (AppUtils.isOnline()) {
                                var materialCode = binding.tvproductValue.text.toString().split("-")
                                vm.fetchStocks(materialCode[0])
                            } else {
                                vm.fetchStocksOffline()
                            }
                        }
                    }

                    vm.getPlantRouteDetails()

                }
                enableProceed()
            }
        })

    }

    private fun updatestorageLocationList(data: List<VegaStorageLocationDetail>) {
        materialStorageLocationList.clear()
        materialStorageLocationList.addAll(data)

        vm.getCustomLocations()

    }

    private fun updateProcessType(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        jsonData.forEach {
            if (it.contains(JSON_RECEVING_LOCATION_LIST)) {
                var receiveLocation =
                    (JSONObject(it).getJSONArray(JSON_RECEVING_LOCATION_LIST).get(0)).toString()
                        .split(",")
                receiveLocation.forEach { it1 ->
                    try {
                        allPlants().single { it1.split(":")[0].contains(it.plantId) }.apply {
                            storageLocation.add(
                                plantName.plus(" - ")
                                    .plus((it1.split(":")[1]).replace("}", "").replace("\"", ""))
                            )
                        }
                    } catch (e: NoSuchElementException) {
                        storageLocation.add(
                            ((it1.split(":")[0]).replace("{", "").replace("\"", "")).plus(" - ")
                                .plus((it1.split(":")[1]).replace("}", "").replace("\"", ""))
                        )
                    }


                }
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
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
                else -> {
                    // no implementation
                }
            }
        }
    }

    private fun populateTruckData(validTruckData: List<VehicleData>) {
        binding.tvDriverNameValue.setText(validTruckData[0].driverDetails[0].driverName)
        binding.tvTruckNoValue.setText(validTruckData[0].vehicleNumber)
        binding.tvLicenseNumberValue.setText(validTruckData[0].driverDetails[0].driverLicenseNumber)
        binding.tvDriverNoValue.setText(validTruckData[0].driverDetails[0].driverPhone)
        binding.tvTruckIdValue.setText(validTruckData[0].vehicleId)
        binding.tvTransportVendorValue.setText(
            validTruckData[0].vendor.vendorCode.plus("-").plus(validTruckData[0].vendor.name)
        )

    }

    private fun populateOfflineTruckData(validTruckData: List<VehicleDetails>) {
        binding.tvDriverNameValue.setText(validTruckData[0].driverName)
        binding.tvTruckNoValue.setText(validTruckData[0].vehicleNumber)
        binding.tvLicenseNumberValue.setText(validTruckData[0].driverLicenseNumber)
        binding.tvDriverNoValue.setText(validTruckData[0].driverPhone)
        binding.tvTruckIdValue.setText(validTruckData[0].vehicleId.toString())
        binding.tvTransportVendorValue.setText(
            validTruckData[0].vendorCode.plus("-").plus(validTruckData[0].name)
        )

    }

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
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
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
        vm.getTruckDetails(currentSeason)
    }

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

            if (validTruck?.size ?: 0 > 0)
                validTruck.let { it?.let { it1 -> populateTruckData(it1) } }
            else
                UIUtils.showErrorDialog(requireContext(), " Invalid Truck ID")
        } else {
            var validTruck = vehicleDetails.filter { ((it.qrCodeNumber) == (data)) }
            if (validTruck.size > 0)
                validTruck.let { populateOfflineTruckData(it) }
            else
                UIUtils.showErrorDialog(requireContext(), " Invalid Truck ID")
        }
    }

    private fun moveToOfflineSummary() {
        callbackCocoa.replaceFragment(DISPATCH_OFFLINE_SUMMARY)
    }

    private fun validateProceed() =
        if (binding.tvWhValue.text.isNotEmpty() && binding.tvDeparturePointValue.text.isNotEmpty() && binding.tvDriverName.text.isNotEmpty()
            && binding.tvproductValue.text.toString()
                .isNotEmpty() && binding.tvTruckNoValue.text.toString()
                .isNotEmpty() && binding.tvLicenseNumberValue.text.toString().isNotEmpty()
            && binding.tvSDWaybillNoValue.text.toString()
                .isNotEmpty() && binding.tvEvacuationNoValue.text.toString()
                .isNotEmpty() && binding.tvClosingStockValue.text.toString()
                .isNotEmpty() && binding.tvSendingWHValue.text.toString()
                .isNotEmpty() && binding.tvRouteValue.text.toString().isNotEmpty()
        ) {
            if (tvnoofbagsValue.text.toString().equals("0")) {
                showSnack(getString(R.string.number_of_bags_zero))
            } else {
                prepareTruckWb()
                vm.saveWeighBridgeDetails()
                materialModelList.forEach {
                    it.weighBridgeId = vm.dispatchWh.weighBridgeId
                }
                saveMaterialDetails(materialModelList)
                callbackCocoa.replaceFragment(WEIGHSCALE_SUMMARY, vm.dispatchWh)
                /*if (AppUtils.isOnline()) {
                    var materialCode = binding.tvproductValue.text.toString().split("-")
                    vm.fetchStocks(materialCode[0])
                }else{
                    vm.fetchStocksOffline()
                }*/
            }
        } else {
                showSnack(getString(R.string.mandatory))
        }

    private fun prepareTruckWb() {
        if (vm.dispatchWh.weighBridgeId.isEmpty()) {
            val randomDouble = "TMP".plus(Random.nextLong().toString())
            vm.dispatchWh.weighBridgeId = randomDouble
            vm.dispatchWh.weighBridgeId = randomDouble
        }
        /*vm.dispatchWh.purchaseDocNum = selectedPurchaseOrder?.purchaseDocNum
        vm.dispatchWh.purchaseDocDesc = selectedPurchaseOrder?.purchaseDocDesc
        vm.dispatchWh.weighBridgeType = MTNT_WEIGHSCALE
        vm.dispatchWh.netWeight = selectedPurchaseOrder?.menge
        vm.dispatchWh.unitsOfMeasure = selectedPurchaseOrder?.meins
        vm.dispatchWh.plantId = selectedPurchaseOrder?.plantId ?: ""
        vm.dispatchWh.storageLocationCode = binding.tvSendingWHValue.text.toString()
        vm.dispatchWh.recStorageLocationCode = selectedSendingWH
        vm.dispatchWh.recPlantId = selectedPurchaseOrder?.warehouseId
        vm.dispatchWh.destinationWH = selectedDestinationWH
        vm.dispatchWh.destinationWHName = selectedDestinationWHName
        vm.dispatchWh.vehicleNumber = binding.tvTruckNoValue.text.toString()
        vm.dispatchWh.driverName = binding.tvDriverNameValue.text.toString()
        vm.dispatchWh.driverPhoneNumber = binding.tvDriverNoValue.text.toString()
        vm.dispatchWh.erdat = getCurrentDate()*/


        vm.dispatchWh.fromVendorCode = binding.tvTransportVendorValue.text.toString()
        vm.dispatchWh.truckID = binding.tvTruckIdValue.text.toString()
        vm.dispatchWh.vehicleNumber = binding.tvTruckNoValue.text.toString()
        vm.dispatchWh.driverLicenseNumber = binding.tvLicenseNumberValue.text.toString()
        vm.dispatchWh.evacuationCertificateNumber = binding.tvEvacuationNoValue.text.toString()
        vm.dispatchWh.sidingDepotwayBillNumber = binding.tvSDWaybillNoValue.text.toString()
        vm.dispatchWh.driverName = binding.tvDriverNameValue.text.toString()
        vm.dispatchWh.driverPhoneNumber = binding.tvDriverNoValue.text.toString()
        vm.dispatchWh.recStorageLocationCode = selectedSendingWH
        vm.dispatchWh.storageLocationCode =
            binding.tvSendingWHValue.text.toString().split("-")[0].trim()
        vm.dispatchWh.storageLocationName =
            binding.tvSendingWHValue.text.toString().split("-")[1].trim()
        val destinationWarehouse = binding.tvWhValue.text.toString().split("-")
        vm.dispatchWh.destinationWH = getMTNTPlantName(destinationWarehouse[0], false)
        vm.dispatchWh.recPlantId = getMTNTPlantName(destinationWarehouse[0], false).replace(" ", "")
        vm.dispatchWh.destinationWHName = destinationWarehouse[1]
        vm.dispatchWh.recStorageLocationCode = destinationWarehouse[1].replace(" ", "")
        vm.dispatchWh.departurePoint = binding.tvDeparturePointValue.text.toString()
        vm.dispatchWh.route = binding.tvRouteValue.text.toString()
        val materialValue = binding.tvproductValue.text.toString().split("-")
        vm.dispatchWh.materialCode = MATERIAL_CODE.plus(materialValue[0])
        val billLandingNumber = "New-Temp-"
        vm.dispatchWh.frbnr1 = billLandingNumber.plus(UIUtils.getTenDigRandomId())
        vm.dispatchWh.materialName = materialValue[1]
        vm.dispatchWh.purchaseQuantity = binding.tvnoofbagsValue.text.toString()
        vm.dispatchWh.routeLocCode = binding.tvRouteValue.text.toString()
        vm.dispatchWh.textId = "Z003"
        vm.dispatchWh.netWeight = calculateWeight()
        vm.dispatchWh.grossWeight = calculateWeight()
        vm.dispatchWh.textValue = binding.tvDriverNameValue.text.toString()
        /* vm.dispatchWh.materialCode
         vm.dispatchWh.materialName */

    }

    private fun showSingleSelectDialog(isWh: Boolean, title: String, isSendingWH: Boolean) {
        val list: java.util.ArrayList<String>
        if (isSendingWH) {
            //list = sendingWareHouseList.map{data -> data.procureLocationCode.plus("-").plus(data.procureLocationName)} as  ArrayList<String>
            list = productnameList
        } else if (isWh) {
            list = storageLocation
        } else {
            list = departurePointList
        }

        cocoaCustomDialog =
            VegaGhanaCocoaCustomSingleSelectDialog(
                title,
                isWh, isSendingWH,
                list,
                activity!!,
                this
            )
        cocoaCustomDialog?.show()
        cocoaCustomDialog?.setCanceledOnTouchOutside(false)
    }


  /*  private fun getPlantName(plantId: String): String {
        val plants = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.PLANT_LIST, ""))
        val plant = plants.singleOrNull { it.plantId == plantId }
        return plant?.plantName ?: ""
    }

    private fun allPlants(): List<Plant> {
        return Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.PLANT_LIST, ""))
    }*/

    private fun enableProceed() {
        val enable =
            (binding.tvWhValue.text.isNotEmpty() && binding.tvDeparturePointValue.text.isNotEmpty() && binding.tvDriverName.text.isNotEmpty()
                    && binding.tvTruckNoValue.text.toString().isNotEmpty() && binding.tvLicenseNumberValue.text.toString().isNotEmpty() && binding.tvnoofbagsValue.text.toString().isNotEmpty()
                    )
        if (enable) {
            binding.btnProceed.setBackgroundColor(getColor(if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btnProceed.isEnabled = enable
    }

    override fun clickOnItem(data: String, isWh: Boolean, isSendingWH: Boolean) {
        cocoaCustomDialog?.dismiss()
        if (isWh) {
            binding.tvWhValue.text = data
            enableProceed()
           /* wareHouseId = data
            val da = data.split("-").toTypedArray()
            //vm.getPurchaseOrder(da[0])
            vm.dispatchWh.recPlantId = da[0]
            vm.dispatchWh.plantName = da[1]
            vm.dispatchWh.recPlantName = da[1]
//            vm.dispatchWh.plantName = da[1]
//            vm.dispatchWh.recPlantName = da[1]
            vm.dispatchWh.plantId = da[0]
            selectedDestinationWH = da[2]
            selectedDestinationWHName = da[3]
            getPurchaseOrderByStorageLocation(da[0])
            binding.tvstoValue.text = ""
            binding.tvmaterialValue.text = ""
            if (materialModelList.isNotEmpty()) {
                val material = materialModelList
                vm.deleteMaterialData(material[0].weighBridgeId)
                vm.removeGhanaLotList(material[0].weighBridgeId)
            }

            materialModelList.clear()
//            selectedMaterialList.clear()
            binding.rvMaterialList.adapter?.notifyDataSetChanged()*/

        } else if(((!isWh) && (!isSendingWH))){
           /* binding.tvDeparturePointValue.text = data
            enableProceed()*/
        }else if (!isSendingWH) {
//            vm.removeLotList()
            val da = data.split(" (").toTypedArray()

//            vm.deleteMaterialData()
            if (materialModelList.isNotEmpty()) {
                val material = materialModelList
                vm.deleteMaterialData(material[0].weighBridgeId)
                vm.removeGhanaLotList(material[0].weighBridgeId)
            }

            materialModelList.clear()

//            getWeight(data)
            getWeight(da[0])

            // selectedPurchaseOrder?.storageLocationName = vm.dispatchWh.plantName
            // binding.tvDeparturePointValue.text = da[0]
//            binding.tvstoValue.text = data
            vm.dispatchWh.purchaseDocNum = selectedPurchaseOrder?.purchaseDocNum
            vm.dispatchWh.purchaseDocDesc = selectedPurchaseOrder?.purchaseDocDesc
            vm.dispatchWh.plantId = selectedPurchaseOrder?.plantId
            /*vm.weighScaleWithLotMaterial.observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    vm.dispatchWh = it.dispatch
                } else {
                    binding.tvTruckNoValue.setText("")
                    binding.tvDriverNameValue.setText("")
                    binding.tvDriverNoValue.setText("")
                }
            })
            vm.getWeighScaleWithLotAndMaterial(
                vm.dispatchWh.plantId ?: "",
                vm.dispatchWh.purchaseDocNum ?: ""
            )*/
            enableProceed()
        } else if (isSendingWH) {
            binding.tvproductValue.text = data

            /*val da = data.split("-").toTypedArray()

            selectedSendingWH = da[0]
            getPurchaseOrderByStorageLocation("")
            enableProceed()*/
        }
    }

    private fun getWeight(purchaseId: String) {
        materialModelList.clear()
        materials =
            purchaseOrder.filter { s -> s.purchaseDocNum == purchaseId } as ArrayList<VegaGhanaMtntPurchaseOrders>
        materials?.forEach {
            val materialModel = VegaGhanaPurchaseOrderMaterialModel()
            materialModel.materialCode = it.materialCode
            materialModel.soNumber = it.purchaseDocNum
            materialModel.soWeight = it.openQuantity!!
            materialModel.uom = it.meins
            materialModel.purchaseOrderNum = it.purchaseDocNum ?: ""
            materialModel.purchaseOrderDesc = it.purchaseDocDesc ?: ""
            materialModelList.add(materialModel)
        }
        selectedPurchaseOrder = materials?.get(0)
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            stocksList.clear()
                            val dataValue = it.data?.data!!
                            stocksList.addAll(dataValue)
                            totalWeight(stocksList, offlineStocksList)
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
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

    private fun totalWeight(
        stocksList: MutableList<VegaCocoaRminLots>,
        stocksOfflineList: MutableList<VegaEcuadorDispatchStocks>
    ) {
        var materialCode = (binding.tvproductValue.text.toString()).split("-")
        var weight: Double? = 0.0
        var totalWeight: Double? = 0.0
        if (AppUtils.isOnline()) {
            var stockValue =
                stocksList.filter { it.storageLocationCode.equals(selectedSendingWH) }
                    .filter {
                        it.plantId.equals(loggedInPlantId)
                    }
                    .filter { it.materialCode.equals(MATERIAL_CODE.plus(materialCode[0])) }
            totalWeight = (stockValue.sumByDouble { it.weight?.toDouble() ?: 0.0 })

        } else {
            var stockValue =
                stocksOfflineList.filter { it.storageLocationCode.equals(selectedSendingWH) }
                    .filter {
                        it.plantId.equals(loggedInPlantId)
                    }
                    .filter { it.materialCode.equals(MATERIAL_CODE.plus(materialCode[0])) }
            totalWeight = (stockValue.sumByDouble { it.weight?.toDouble() ?: 0.0 })

        }
        isStockAvailable(calculateBag(totalWeight))
    }


    private fun saveMaterialDetails(list: ArrayList<VegaGhanaPurchaseOrderMaterialModel>) {
        vm.saveMaterialDetails(list)
    }

    private fun updatePurchaseOrder(response: Resource<GenericReqAndResp<List<VegaGhanaMtntPurchaseOrder>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        purchaseOrderList = it1 as MutableList<VegaGhanaMtntPurchaseOrder>
                    }
                    filterSTONumber()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun updatePurchaseOrderOffline(data: List<VegaCocoaPurchaseOrders>?) {
        if (!data.isNullOrEmpty()) {
            purchaseOrderList.clear()
            var offlineData = data
//                .filter { it.openQuantity != "0.000" }
            offlineData.forEach {
                var vegaGhanaMtntPurchaseOrders = VegaGhanaMtntPurchaseOrders()
                vegaGhanaMtntPurchaseOrders.batchNumber = it.batchNumber
                vegaGhanaMtntPurchaseOrders.materialCode = it.materialCode
                vegaGhanaMtntPurchaseOrders.materialName = it.materialName
                vegaGhanaMtntPurchaseOrders.meins = it.meins
                vegaGhanaMtntPurchaseOrders.menge = it.menge
                vegaGhanaMtntPurchaseOrders.plantId = it.plantId
                vegaGhanaMtntPurchaseOrders.warehouseId = it.warehouseId
                vegaGhanaMtntPurchaseOrders.purchaseDocDesc = it.purchaseDocDesc
                vegaGhanaMtntPurchaseOrders.purchaseDocNum = it.purchaseDocNum
                vegaGhanaMtntPurchaseOrders.purchaseOrderType = it.purchaseOrderType
                vegaGhanaMtntPurchaseOrders.openQuantity = it.openQuantity
                vegaGhanaMtntPurchaseOrders.storageLocationCode = it.storageLocationCode
                vegaGhanaMtntPurchaseOrders.storageLocationName = it.storageLocationName
                vegaGhanaMtntPurchaseOrders.issueLocation = it.issueLocation

                purchaseOrder.add(vegaGhanaMtntPurchaseOrders)
            }
//            val location =
//                purchaseOrder.map { it.warehouseId.plus("-").plus(getMTNTPlantName(it.warehouseId ?: "")) }.toSet()
            //.filter { !it.storageLocationCode.isNullOrEmpty() }
            /* val location = purchaseOrder
                 .map {it.warehouseId.plus("-").plus(getMTNTPlantName(it.warehouseId ?: "")).plus("-").plus(it.storageLocationCode ?: "").plus("-").plus(it.storageLocationName ?: "") }.toSet()
             storageLocation.addAll(location)*/

        }
    }

    /*private fun getPurchaseOrderByStorageLocation(warehouseId: String) {
        STONumbers.clear()
        val orderList = purchaseOrder.filter { it.warehouseId == warehouseId.trim() }.filter { it.storageLocationCode == selectedDestinationWH }
            .filter { it.issueLocation == selectedSendingWH }
//            .filter { it.warehouseId == selectedDestinationWH }
//          if selectedSendingWH is not empty
        filteredPurchaseOrderList = orderList as ArrayList<VegaGhanaMtntPurchaseOrders>

       *//*var multiMaterialPO =  filteredPurchaseOrderList.groupingBy { it.purchaseDocNum }.eachCount().filter{it.value > 1}
        if(multiMaterialPO.isNotEmpty()) {
            multiMaterialPO.keys.forEach{
                var o = filteredPurchaseOrderList.filter { item -> item.purchaseDocNum == it }
                var material = ""
                var iteration = 0
                o.forEach { item1 ->
                    material = if(iteration++ == o.size -1)
                        material.plus(productList.single { item1.materialCode.contains(it.materialCode)}.materialName)
                    else
                        material.plus(productList.single { item1.materialCode.contains(it.materialCode)}.materialName).plus(", ")
                }
                STONumbers.add(it.plus(" (").plus(material).plus(")"))
            }
        }

        var singleMaterialPO = filteredPurchaseOrderList.groupingBy { it.purchaseDocNum }.eachCount().filter{it.value == 1}
        if(singleMaterialPO.isNotEmpty()){
            singleMaterialPO.keys.forEach{
                var o = filteredPurchaseOrderList.filter { item -> item.purchaseDocNum == it }
                var material = ""
                o.forEach { item1 ->
                    material = material.plus(productList.single { item1.materialCode.contains(it.materialCode)}.materialName)
                }
                STONumbers.add(it.plus(" (").plus(material).plus(")"))
            }

        }*//*


        STONumbers.addAll(orderList.map { it.purchaseDocNum ?: "" }.toSet().toList())

    }*/

   /* private fun getMTNTPlantName(plantId: String): String {
        val plants = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.MTNT_PLANT_LIST, ""))
        val plant = plants.singleOrNull { it.plantId == plantId }
        return plant?.plantName ?: ""
    }*/

    private fun filterSTONumber() {
        for (item in purchaseOrderList) {
            purchaseOrder.addAll(item.purchaseOrders)
        }
//        val location = purchaseOrder.map {it.warehouseId.plus("-").plus(getMTNTPlantName(it.warehouseId ?: "")) }.toSet()
        //.filter { !it.storageLocationCode.isNullOrEmpty() }
        /* val location = purchaseOrder
             .map {it.warehouseId.plus("-").plus(getMTNTPlantName(it.warehouseId ?: "")).plus("-").plus(it.storageLocationCode ?: "").plus("-").plus(it.storageLocationName ?: "") }.toSet()
         storageLocation.addAll(location)*/
    }


    /* private fun getCurrentDate(): String {
         val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
         return simpleDateFormat.format(Date())
     }*/

    private fun updateStockDetails() {
        var materialCode = (binding.tvproductValue.text.toString()).split("-")
        var stockValue =
            stocksList.filter { it.storageLocationCode.equals(selectedSendingWH) }
                .filter {
                    it.plantId.equals(loggedInPlantId)
                }
                .filter { it.materialCode.equals(MATERIAL_CODE.plus(materialCode[0])) }
        var weight: Double? = 0.0
        var totalWeight: Double? = 0.0
        totalWeight = (stockValue.sumByDouble { it.weight?.toDouble() ?: 0.0 })
        /*stockValue.forEach { it1 ->
            totalWeight = ((weight!!) + (it1.weight?.toDouble()!!))
        }*/

        var enteredBags: Double? = (calculateWeight()).toDouble()
        if (((totalWeight) >= (enteredBags!!))) {
            isStockAvailable(calculateBag(totalWeight))
            //binding.tvClosingStockValue.text=closingStockCount.toInt().toString()

        } else {
            UIUtils.showErrorDialog(
                requireContext(),
                getString(R.string.dis_stock_not_available)
            )
        }
    }

    private fun updateofflineStockDetails() {
        var materialCode = (binding.tvproductValue.text.toString()).split("-")
        var stockValue =
            offlineStocksList.filter { it.storageLocationCode.equals(selectedSendingWH) }
                .filter {
                    it.plantId.equals(loggedInPlantId)
                }.filter { it.materialCode.equals(MATERIAL_CODE.plus(materialCode[0])) }
        var weight: Double? = 0.0
        var totalWeight: Double? = 0.0
        /*stockValue.forEach { it1 ->
            totalWeight = ((weight!!) + (it1.weight?.toDouble()!!))
        }*/
        totalWeight = (stockValue.sumByDouble { it.weight?.toDouble() ?: 0.0 })
        var enteredBags: Double? = (calculateWeight()).toDouble()
        if (((totalWeight) >= (enteredBags!!))) {
            isStockAvailable(calculateBag(totalWeight))


        } else {
            UIUtils.showErrorDialog(
                requireContext(),
                getString(R.string.dis_stock_not_available)
            )
        }
    }

    private fun calculateWeight(): String {
        var netWeight = ""
        var materialValue = (binding.tvproductValue.text.toString()).split("-")
        var uom = ((uomDetails.filter { ((it.materialCode).equals(materialValue[0])) }).filter {
            it.fromUom.equals(BAG)
        }).single()
        netWeight =
            ((uom.value1?.toInt()?.toDouble()?.div(uom.value2?.toInt()?.toDouble()!!))?.times(
                (binding.tvnoofbagsValue.text.toString()
                    .toInt().toDouble())
            )).toString()
        return netWeight
    }

    private fun calculateBag(totalWeight: Double): Int {
        var noOfBags = 0
        var singleBags = 0.0
        var materialValue = (binding.tvproductValue.text.toString()).split("-")
        var uom = ((uomDetails.filter { ((it.materialCode).equals(materialValue[0])) }).filter {
            it.fromUom.equals(BAG)
        }).single()
        singleBags =
            (((uom.value1?.toInt()?.toDouble()?.div(uom.value2?.toInt()?.toDouble()!!))) ?: 0.0)
        noOfBags = totalWeight.div(singleBags).roundToInt()
        return noOfBags
    }


    private fun isStockAvailable(count: Int) {
        when {
            count < 0 -> {
                binding.tvClosingStockValue.text = ""
                enableProceed()
                UIUtils.showErrorDialog(
                    requireContext(),
                    getString(R.string.number_of_bags)
                )
            }
            count >= 0 -> {
                binding.tvClosingStockValue.text = count.toString()
            }

        }

    }

}
