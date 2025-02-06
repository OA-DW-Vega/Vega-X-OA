package com.olam.warehouse.vegax.grnnigeria.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.gson.Gson
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.presentation.data.domain.model.TruckManagementVehicleResponse
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.grnnigeria.R
import com.olam.warehouse.vegax.grnnigeria.databinding.FragmentNigeriaGrntLayoutBinding
import com.olam.warehouse.vegax.grnnigeria.utils.*
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel


class VegaNigeriaGRNTDetailsFragement : BaseFragment(),
    VegaNigeriaSingleSelectListener {
    private var uomDetails = ArrayList<VegaUomDetails>()
    private var truckData: TruckManagementVehicleResponse? = null
    private var currentSeason: String = ""
    override val layoutResourceId = R.layout.fragment_nigeria_grnt_layout
    private lateinit var binding: FragmentNigeriaGrntLayoutBinding
    private var grntData = VegaGateEntry()
    private var plantList = mutableListOf<Plant>()
    private val vm: VegaNigeriaGrnViewModel by viewModel()
    private var model: VegaCocoaDispatchWB? = null
    private var supplierList = mutableListOf<VegaVendor>()
    private var supplierListString = arrayListOf<String>()
    private var plantListString = arrayListOf<String>()
    private var materialList = mutableListOf<VegaMaterial>()
    private var materialStorageLocationList = mutableListOf<VegaStorageLocationDetail>()
    private var cocoaCustomDialog: VegaNigeriaCustomSingleSelectDialog? = null
    private var storageLocation = ArrayList<String>()
    private var callBack: CallBack? = null
    private var productList = emptyList<VegaMaterial>()
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var productnameList = ArrayList<String>()
    private var jsonData = mutableListOf<String>()
    private var procurementTypeList = ArrayList<String>()
    private var grnPrice: String = "0"
    private var recLocationList = arrayListOf<String>()

    interface CallBack {
        fun replaceFragment(gateEntryType: String, gateEntryData: VegaGateEntry, grnPrice: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    companion object {
        fun newInstance(gateEntryData: VegaGateEntry) = VegaNigeriaGRNTDetailsFragement().putArgs {
            putParcelable(GRNT_DATA, gateEntryData)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentNigeriaGrntLayoutBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        grntData = arguments?.getParcelable(GRNT_DATA)!!

        plantList = getMultiPlantList() as MutableList<Plant>
        /* plantList.forEach {
             plantListString.add(it.plantId.plus("-").plus(it.plantName))
         }*/

        vm.storageLocationList.observe(
            viewLifecycleOwner,
            Observer { updatestorageLocationList(it) })
        vm.getStorageLocationDetail()

        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.getConfigItems(UserRoles.QUALITY.role)

        var plantIdNew: String = PreferenceHelper.get(Constants.WERKS, "")
        vm.getSuppliers(plantIdNew)
        vm.suppplier.observe(viewLifecycleOwner, Observer {
            if (it != null)
                supplierList = it as MutableList
            supplierList.forEach {
                supplierListString.add(
                    (SUPPLIER_CODE.plus(it.vendorCode.toString())).plus("-")
                        .plus(it.vendorName.toString())
                )
            }
        })

        binding.tvSupplierValue.setOnClickListener {
            showSingleSelectDialog(false, getString(R.string.select_supplier), false)
        }

        binding.tvMaterialValue.setOnClickListener {
            showSingleSelectDialog(false, getString(R.string.select_material), true)
        }

        binding.tvReceivingPlantValue.setOnClickListener {
            showSingleSelectDialog(true, getString(R.string.select_plant), false)
        }

        binding.tvReceivingCustomValue.setOnClickListener {
            if (binding.tvReceivingPlantValue.text != "") {

                var plantId = binding.tvReceivingPlantValue.text.split("-")[0]
               /* var receivingLocationList = custonLocationList.filter {
                    !it.storageLocationType.equals(
                        "B"
                    )
                }.filter { it.plant == plantId }*/
                val receivingLocationList = recLocationList.filter { it.contains(plantId) }
                if (receivingLocationList.size >= 1) {
                    showReceivingLocationDialog(receivingLocationList)
                }
            }
        }

        binding.btnProceed.setOnClickListener { validateProceed() }

        binding.tvNetweightValue.onChange { enableProceed() }
        binding.tvGRNPriceValue.onChange { enableProceed() }

        binding.tvUpcountryPlantValue.text =
            getPlantDetails().plantId.plus("-").plus(getPlantDetails().plantName)

        binding.tvProcurementTypeValue.setOnClickListener {
            showProcurementDialogDialog(
                procurementTypeList
            )
        }

        vm.getAllProduct()
        vm.allProduct.observe(
            viewLifecycleOwner,
            Observer {
                if (it != null) {
                    productList = it
                    productList.forEach { it1 ->
                        productnameList.add(
                            (MATERIAL_CODE.plus(it1.materialCode.plus("-"))).plus(
                                it1.materialName
                            )
                        )
                    }
                }
            })

        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer { updateProcessType(it) })
        vm.getProcessTypeList(getCurrentKey())

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            custonLocationList = it.toMutableList()
          /*  var receivingLocationList = custonLocationList.filter {
                !it.storageLocationType.equals(
                    "B"
                )
            }
            if (receivingLocationList.size == 1) {
                binding.tvReceivingCustomValue.text =
                    custonLocationList[0].procureLocationCode.plus(" - ")
                        .plus(custonLocationList[0].procureLocationName)
                grntData.storageLocationCode = custonLocationList[0].procureLocationCode
                grntData.storageLocationName = custonLocationList[0].procureLocationName
            }*/

        })
        vm.getCustomLocations()
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        val receivingPlantList =
                configItems?.filter { it.process.equals(ConfigItems.DEFAULT_STORAGE_LOC.item) }
        if (((!receivingPlantList.isNullOrEmpty()) && (receivingPlantList.size > 0))) {
            var value = receivingPlantList.get(0).value
            var value1 = receivingPlantList.get(0).value1
            plantListString.add(value.toString())
            plantListString.add(value1.toString())
        }
    }

    private fun updatestorageLocationList(data: List<VegaStorageLocationDetail>) {
        materialStorageLocationList.clear()
        materialStorageLocationList.addAll(data)

    }

    private fun showProcurementDialogDialog(procurementTypeList: ArrayList<String>) {
        //procurementTypeList as MutableList<String>
        MaterialDialog(requireContext()).show {
            title(R.string.tittle_procurement_type)
            listItemsSingleChoice(items = procurementTypeList) { _, index, text ->
                binding.tvProcurementTypeValue.text = text
                enableProceed()
                grntData.procurementType = binding.tvProcurementTypeValue.text.toString().trim()
                if (grntData.procurementType.equals(PR) || grntData.procurementType.equals(PX) || grntData.procurementType.equals(
                        DR
                    )
                ) {
                    grntData.grnModel = GRN_VALUE
                } else {
                    grntData.grnModel = Z01
                }
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun updateProcessType(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        jsonData.forEach {
            if (it.contains(JSON_RECEVING_LOCATION_LIST)) {
                var receiveLocation =
                    (JSONObject(it).getJSONArray(JSON_RECEVING_LOCATION_LIST).get(0)).toString()
                        .split(",")
                receiveLocation.forEach { it1 ->
                    storageLocation.add(
                        ((it1.split(":")[0]).replace("{", "").replace("\"", "")).plus(" - ")
                            .plus((it1.split(":")[1]).replace("}", "").replace("\"", ""))
                    )
                }
            }
            if (it.contains(RECEVING_PLANT_SLOC)) {
                var receiveLocation = (JSONObject(it).getJSONArray(RECEVING_PLANT_SLOC))
                recLocationList.clear()
                for(i in 0 until receiveLocation.length()){
                    val objects: JSONObject = receiveLocation.getJSONObject(i)
                    val iterator = objects.keys()
                    while (iterator.hasNext()){
                        val key = iterator.next()
                        recLocationList.add(key.toString().plus(" _ ").plus(objects.get(key)))
                    }
                }
            }




            /*if (it.contains(JSON_PROCUREMENT_TYPE)) {
                var procurementTypeSelection  = (JSONObject(it).getJSONArray(JSON_PROCUREMENT_TYPE)).toString().split(",")
                procurementTypeSelection.forEach { it1 ->
                    procurementTypeList.add((it1.split(":")[0]).replace("[", "").replace("]", "").replace("\"", ""))
                }
            }else{*/
            //}
        }
        procurementTypeList.add(DD)
        procurementTypeList.add(DX)
    }


    private fun showReceivingLocationDialog(it: List<String>) {
        val location = arrayListOf<String>()
        it.forEach {it1->
            val item = it1.split(" _ ")
            location.add(item[1])
        }
        MaterialDialog(requireContext()).show {
            title(R.string.select_location)
            listItemsSingleChoice(items = location) { _, index, text ->
                binding.tvReceivingCustomValue.text = text
                val items = text.split("-")
                grntData.storageLocationCode = items[0]
                grntData.storageLocationName = items[1]
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun moveToOfflineSummary() {
        // callbackCocoa.replaceFragment(DISPATCH_OFFLINE_SUMMARY)
    }

    private fun validateProceed() {
        if ((binding.tvSupplierValue.text.isNotEmpty() && binding.tvUpcountryPlantValue.text.isNotEmpty() && binding.tvMaterialValue.text.isNotEmpty()
                    && binding.tvNetweightValue.text.toString()
                .isNotEmpty() && binding.tvGRNPriceValue.text.toString()
                .isNotEmpty() && binding.tvReceivingPlantValue.text.toString().isNotEmpty()
                    && binding.tvProcurementTypeValue.text.toString().isNotEmpty())
        ) {
            grntData.supplierName = binding.tvSupplierValue.text.toString()
            grntData.plantId = (binding.tvUpcountryPlantValue.text.toString()).split("-")[0]
            grntData.plantName = (binding.tvUpcountryPlantValue.text.toString()).split("-")[1]
            grntData.materialCode = binding.tvMaterialValue.text.split("-")[0]
            grntData.materialName = binding.tvMaterialValue.text.split("-")[1]
            grntData.netWeight = binding.tvNetweightValue.text.toString()
            grntData.grossWeight = binding.tvGRNPriceValue.text.toString()
            grntData.receivingPlant = binding.tvReceivingPlantValue.text.toString()
           // grntData.receivingPlant = binding.tvReceivingCustomValue.text.toString()
            grntData.procurementType = binding.tvProcurementTypeValue.text.toString()
            grntData.unitsOfMeasure = UNIT_KG
            callBack?.replaceFragment(SUMMARY_FRAG, grntData, grnPrice)
        } else {
            showSnack(getString(R.string.mandatory))
        }
    }

    /* private fun prepareTruckWb() {
         if (vm.dispatchWh.weighBridgeId.isEmpty()) {
             val randomDouble = "TMP".plus(Random.nextLong().toString())
             vm.dispatchWh.weighBridgeId = randomDouble
             vm.dispatchWh.weighBridgeId = randomDouble
         }
         *//*vm.dispatchWh.purchaseDocNum = selectedPurchaseOrder?.purchaseDocNum
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
        vm.dispatchWh.erdat = getCurrentDate()*//*


        vm.dispatchWh.fromVendorCode = binding.tvTransportVendorValue.text.toString()
        vm.dispatchWh.truckID = binding.tvTruckIdValue.text.toString()
        vm.dispatchWh.vehicleNumber = binding.tvTruckNoValue.text.toString()
        vm.dispatchWh.driverLicenseNumber = binding.tvLicenseNumberValue.text.toString()
        vm.dispatchWh.driverName = binding.tvDriverNameValue.text.toString()
        vm.dispatchWh.driverPhoneNumber = binding.tvDriverNoValue.text.toString()
        vm.dispatchWh.recStorageLocationCode = selectedSendingWH
        vm.dispatchWh.storageLocationCode =
                binding.tvSendingWHValue.text.toString().split("-")[0].trim()
        vm.dispatchWh.storageLocationName =
                binding.tvSendingWHValue.text.toString().split("-")[1].trim()
        val destinationWarehouse = binding.tvWhValue.text.toString().split("-")
        vm.dispatchWh.destinationWH = destinationWarehouse[0]
        vm.dispatchWh.recPlantId = destinationWarehouse[0].replace(" ","")
        vm.dispatchWh.destinationWHName = destinationWarehouse[1]
        vm.dispatchWh.recStorageLocationCode = destinationWarehouse[1].replace(" ","")
        vm.dispatchWh.departurePoint =  binding.tvDeparturePointValue.text.toString()
        vm.dispatchWh.route =  binding.tvRouteValue.text.toString()
        val materialValue = binding.tvproductValue.text.toString().split("-")
        vm.dispatchWh.materialCode = MATERIAL_CODE.plus(materialValue[0])
        vm.dispatchWh.frbnr1 = binding.tvBillOFLadingValue.text.toString()
        vm.dispatchWh.materialName  = materialValue[1]
        vm.dispatchWh.purchaseQuantity =  binding.tvnoofbagsValue.text.toString()
        vm.dispatchWh.routeLocCode =  binding.tvRouteValue.text.toString()
        vm.dispatchWh.textId = "Z003"
        vm.dispatchWh.netWeight  = calculateWeight()
        vm.dispatchWh.grossWeight  = calculateWeight()
        vm.dispatchWh.textValue =  binding.tvDriverNameValue.text.toString()
        *//* vm.dispatchWh.materialCode
         vm.dispatchWh.materialName *//*

    }*/

    private fun getMultiPlantList(): List<Plant> {
        val plants = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.PLANT_LIST, ""))
        return plants
    }

    private fun showSingleSelectDialog(isWh: Boolean, title: String, isSendingWH: Boolean) {
        val list: java.util.ArrayList<String>
        if (isSendingWH) {
            //list = sendingWareHouseList.map{data -> data.procureLocationCode.plus("-").plus(data.procureLocationName)} as  ArrayList<String>
            list = productnameList
        } else if (isWh) {
            list = plantListString
        } else {
            list = supplierListString
        }

        cocoaCustomDialog =
            VegaNigeriaCustomSingleSelectDialog(
                title,
                isWh, isSendingWH,
                list,
                requireActivity(),
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
            (binding.tvSupplierValue.text.isNotEmpty() && binding.tvUpcountryPlantValue.text.isNotEmpty() && binding.tvMaterialValue.text.isNotEmpty()
                    && binding.tvNetweightValue.text.toString()
                .isNotEmpty() && binding.tvGRNPriceValue.text.toString()
                .isNotEmpty() && binding.tvReceivingPlantValue.text.toString().isNotEmpty()
                    && binding.tvProcurementTypeValue.text.toString().isNotEmpty())
        if (enable) {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btnProceed.isEnabled = enable
    }

    override fun clickOnItem(data: String, isWh: Boolean, isSendingWH: Boolean) {
        cocoaCustomDialog?.dismiss()
        if (isWh) {
            binding.tvReceivingPlantValue.text = data
            var receivingLocationList = custonLocationList.filter {
                !it.storageLocationType.equals(
                    "B"
                )
            }.filter {
                it.plant == data.split("-")[0]
            }
            if (receivingLocationList.size == 1) {
                binding.tvReceivingCustomValue.text =
                    custonLocationList[0].procureLocationCode.plus(" - ")
                        .plus(custonLocationList[0].procureLocationName)
                grntData.storageLocationCode = custonLocationList[0].procureLocationCode
                grntData.storageLocationName = custonLocationList[0].procureLocationName
            }
            enableProceed()
        } else if (((!isWh) && (!isSendingWH))) {
            binding.tvSupplierValue.text = data
            grntData.supplierCode = (binding.tvSupplierValue.text.split("-")[0])
            grntData.supplierName = (binding.tvSupplierValue.text.split("-")[1])
            enableProceed()
        } else if (!isSendingWH) {
            binding.tvMaterialValue.text = data
            grntData.materialCode = (binding.tvMaterialValue.text.split("-")[0])
            grntData.materialName = (binding.tvMaterialValue.text.split("-")[1])
            val data =
                materialStorageLocationList.filter { it.storageLocationCode.equals(grntData.storageLocationCode) }
            enableProceed()
            if (data.size > 0) {
                val materialData =
                    materialList.filter { ((MATERIAL_CODE.plus(it.materialCode)).equals(data[0].materialCode)) }
                if (materialData.size > 0) {
                    grnPrice = materialData[0].price.toString()
                }
            }

        } else if (isSendingWH) {
            binding.tvMaterialValue.text = data
            grntData.materialCode = (binding.tvMaterialValue.text.split("-")[0])
            grntData.materialName = (binding.tvMaterialValue.text.split("-")[1])
            enableProceed()
            val data =
                materialStorageLocationList.filter { it.storageLocationCode.equals(grntData.storageLocationCode) }
            enableProceed()
            if (data.size > 0) {
                val materialData =
                    materialList.filter { ((MATERIAL_CODE.plus(it.materialCode)).equals(data[0].materialCode)) }
                if (materialData.size > 0) {
                    grnPrice = materialData[0].price.toString()
                }
            }
        }
    }
}

