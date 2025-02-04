package com.olam.warehouse.vegax.mtntcocoa.ui.virtualmtnt

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentModel
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrder
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcocoa.R
import com.olam.warehouse.vegax.mtntcocoa.databinding.FragmentCocoaMtntNoWeighmentConsigmentLayoutBinding
import com.olam.warehouse.vegax.mtntcocoa.ui.CallBack
import com.olam.warehouse.vegax.mtntcocoa.ui.VegaCocoaMtntViewModel
import com.olam.warehouse.vegax.mtntcocoa.utils.NO_WEIGHMENT
import com.olam.warehouse.vegax.mtntcocoa.utils.NO_WEIGHMENT_ADD_LOT
import com.olam.warehouse.vegax.mtntcocoa.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class VegaCocoaMtntConsignmentFragment : BaseFragment(),
    VegaSingleSelectListener {

    override val layoutResourceId = R.layout.fragment_cocoa_mtnt_no_weighment_consigment_layout
    private lateinit var binding: FragmentCocoaMtntNoWeighmentConsigmentLayoutBinding

    private val vm: VegaCocoaMtntViewModel by viewModel()
    private var model: VegaCocoaNoWeighmentModel? = null
    private var supplierList = mutableListOf<VegaVendor>()
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private var purchaseOrderList = mutableListOf<VegaCocoaPurchaseOrder>()
    private var materialCode: String = ""
    private var storageLocation = ArrayList<String>()
    private var STONumbers = ArrayList<String>()
    private var wareHouseId = ""
    private var selectedPurchaseOrder: VegaCocoaPurchaseOrders? = null
    private var materials: ArrayList<VegaCocoaPurchaseOrders>? = null
    private var purchaseOrder = ArrayList<VegaCocoaPurchaseOrders>()
    private lateinit var callback: CallBack
    private var productList = emptyList<VegaMaterial>()
    private var thirdPartyMaterialList = ArrayList<VegaMaterial>()
    private var isThirdPartyMaterial: Boolean = false
    private var isEdit = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as CallBack
    }

    companion object {
        fun newInstance(data: VegaCocoaNoWeighmentModel?) =
            VegaCocoaMtntConsignmentFragment().putArgs { putParcelable("model", data) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentCocoaMtntNoWeighmentConsigmentLayoutBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        vm.getSuppliers()
        return binding.root
    }

    private fun initExtra() {
        model = arguments?.getParcelable("model")
        if (model != null) {
            initEditedData()
            isEdit = true
            model?.isEditData = true
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("/mtntcocoa/ui/virtualmtnt/VegaCocoaMtntConsignmentFragment")
            .title("Dispatch Cocoa")
            .with(tracker)
    }

    private fun initEditedData() {
        binding.tvWhValue.text = model?.warehouseId.plus("-").plus(model?.plantName)
        binding.tvstoValue.text = model?.purchaseDocNum.plus("-").plus(model?.purchaseDocDesc)
        binding.tvMaterialValue.text = model?.materialName
        binding.tvStoWeightValue.text = model?.netWeight.plus(" ").plus(model?.unitsOfMeasure)
        binding.tvWayBillValue.setText(model?.wayBillNo)
        binding.tvVendorValue.text = model?.transportVendorID.plus("-").plus(model?.transportVendor)
        binding.tvTruckNoValue.setText(model?.vehicleNumber)
        binding.tvDriverNameValue.setText(model?.driverName)
        binding.tvDriverNoValue.setText(model?.driverPhoneNumber)
        vm.dispatchNoWeighmentWh = model!!
    }

    private fun initUI() {
        binding.tvWhValue.setOnClickListener {
            showSingleSelectDialog(true, getString(R.string.select_dest_wh), false)
        }
        binding.tvstoValue.setOnClickListener {
            if (binding.tvWhValue.text.toString().isNotEmpty()) showSingleSelectDialog(
                false,
                getString(R.string.select_sto), false
            )
        }
        binding.tvWayBillValue.onChange { enableProceed() }

        if (AppUtils.isOnline())
            vm.getPurchaseOrder(/*getPlantDetails().plantId*/"")
        else vm.getOfflinePoList()

        binding.btnProceed.setOnClickListener { validateProceed() }
        enableProceed()
        vm.purchaseOrder.observe(viewLifecycleOwner, Observer { updatePurchaseOrder(it) })
        vm.purchaseOfflineOrder.observe(viewLifecycleOwner, Observer { updateOfflinePurchaseOrder(it) })
        binding.tvDriverNameValue.onChange { enableProceed() }
        binding.tvTruckNoValue.onChange { enableProceed() }
        binding.tvDriverNoValue.onChange { enableProceed() }

        binding.tvVendorValue.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.trans_vendor_popup),
                true
            )
        }

        binding.autextVendorValue.onChange {
            /*if (it.length < 10) {
                binding.autextVendorValue.filters = arrayOf(InputFilter.LengthFilter(10))
            }*/
            enableProceed()
        }

       /* vm.supplier.observe(viewLifecycleOwner, Observer {
            *//*if (it != null) {
                supplierList = it as MutableList
                val suppliers = supplierList.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
                val productAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suppliers)
                binding.autextVendorValue.threshold = 1
                binding.autextVendorValue.setAdapter(productAdapter)
                binding.autextVendorValue.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                    enableProceed()
                    it.forEach { material ->
                        if (material.vendorCode == binding.autextVendorValue.text.toString().split(" - ")[0]) {
                            vm.dispatchNoWeighmentWh.transportVendor = material.vendorName ?: ""
                            vm.dispatchNoWeighmentWh.transportVendorID = material.vendorCode
                            binding.autextVendorValue.filters = arrayOf(InputFilter.LengthFilter(30))
                            binding.autextVendorValue.setText(material.vendorName ?: "")

                        }
                    }
                }
            }*//*
        })*/
        vm.getAllProduct()
        vm.allProduct.observe(
            viewLifecycleOwner,
            Observer {
                if (it != null) {
                    productList = it
                }
            })

        vm.product.observe(
            viewLifecycleOwner,
            Observer {
                if (it != null) {
                    binding.tvMaterialValue.text = it.materialName ?: ""
                    selectedPurchaseOrder?.materialName = it.materialName ?: ""
                    vm.dispatchNoWeighmentWh.materialName = it.materialName ?: ""
                }
            })

        vm.getThirdPartyMaterials()
        vm.thirdPartyMaterial.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                thirdPartyMaterialList.addAll(it)
            }
        })

    }

    private fun validateProceed() =
        if (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverName.text.isNotEmpty()
            && binding.tvTruckNoValue.text.toString().isNotEmpty() && binding.tvWayBillValue.text.isNotEmpty()
        ) {
            if (!isEdit) prepareTruckWb()
            vm.saveNoWeighmentDetails()
            binding.autextVendorValue.isCursorVisible = false
            binding.tvWayBillValue.isCursorVisible = false
            callback.replaceFragment(NO_WEIGHMENT_ADD_LOT, vm.dispatchNoWeighmentWh)
        } else {
            //showSnack(getString(R.string.mandatory))
        }

    private fun prepareTruckWb() {
        if (!isEdit && (vm.dispatchNoWeighmentWh.weighBridgeId.isEmpty() || !AppUtils.isOnline())) {
            val randomDouble = "TMP".plus(Random.nextLong().toString())
            vm.dispatchNoWeighmentWh.weighBridgeId = randomDouble
            vm.dispatchNoWeighmentWh.weighBridgeId = randomDouble
        }
        /*
        vm.dispatchNoWeighmentWh.weighBridgeId = selectedPurchaseOrder?.plantId?.plus(
            selectedPurchaseOrder?.purchaseDocNum?.plus(selectedPurchaseOrder?.purchaseDocDesc)
        ).plus(selectedPurchaseOrder?.materialCode)*/
        if (vm.dispatchNoWeighmentWh.transportVendorID.isEmpty()) {
            vm.dispatchNoWeighmentWh.transportVendorID = binding.autextVendorValue.text.toString().trim()
            vm.dispatchNoWeighmentWh.transportVendor = binding.autextVendorValue.text.toString().trim()
        }
        vm.dispatchNoWeighmentWh.wbTempId = vm.dispatchNoWeighmentWh.weighBridgeId
        vm.dispatchNoWeighmentWh.purchaseDocNum = selectedPurchaseOrder?.purchaseDocNum
        vm.dispatchNoWeighmentWh.purchaseDocDesc = selectedPurchaseOrder?.purchaseDocDesc
        vm.dispatchNoWeighmentWh.weighBridgeType = NO_WEIGHMENT
        vm.dispatchNoWeighmentWh.netWeight = selectedPurchaseOrder?.openQuantity
        vm.dispatchNoWeighmentWh.warehouseId = selectedPurchaseOrder?.warehouseId
        vm.dispatchNoWeighmentWh.unitsOfMeasure = selectedPurchaseOrder?.meins
        vm.dispatchNoWeighmentWh.plantId = selectedPurchaseOrder?.plantId ?: ""
        vm.dispatchNoWeighmentWh.vehicleNumber = binding.tvTruckNoValue.text.toString()
        vm.dispatchNoWeighmentWh.driverName = binding.tvDriverNameValue.text.toString()
        vm.dispatchNoWeighmentWh.driverPhoneNumber = binding.tvDriverNoValue.text.toString()
        vm.dispatchNoWeighmentWh.materialCode = selectedPurchaseOrder?.materialCode
        vm.dispatchNoWeighmentWh.materialName = selectedPurchaseOrder?.materialName
        vm.dispatchNoWeighmentWh.wayBillNo = binding.tvWayBillValue.text.toString()
        vm.dispatchNoWeighmentWh.isThirdPartyMaterial = isThirdPartyMaterial
        vm.dispatchNoWeighmentWh.isOfflineData = AppUtils.isOnline()
        vm.dispatchNoWeighmentWh.erdat = getCurrentDate()
    }

    private fun prepareEditedData() {

    }

    private fun showSingleSelectDialog(isWh: Boolean, title: String, isVendor: Boolean) {
        val list: ArrayList<String>
        if (isVendor) {
            list = supplierList.map { it.vendorCode.plus("-").plus(it.vendorName) } as ArrayList<String>
        } else if (isWh) {
            list = storageLocation
        } else {
            list = STONumbers
        }

        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isWh, isVendor, false,
                list,
                activity!!,
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    private fun getPlantName(plantId: String): String {
        val plants = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.MTNT_PLANT_LIST, ""))
        val plant = plants.singleOrNull { it.plantId == plantId }
        return plant?.plantName ?: ""
    }

    private fun allPlants(): List<Plant> {
        return Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.PLANT_LIST, ""))
    }

    private fun enableProceed() {
        val enable =
            (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverNameValue.text.toString()
                .isNotEmpty()
                    && binding.tvTruckNoValue.text.toString().isNotEmpty() && binding.tvWayBillValue.text.toString()
                .isNotEmpty() && binding.autextVendorValue.text.toString().isNotEmpty())
        if (enable) {
            binding.btnProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btnProceed.isEnabled = enable
    }

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {
        customDialog?.dismiss()
        if (isWh) {
            binding.tvWhValue.text = data
            wareHouseId = data
            val da = data.split("-").toTypedArray()
            vm.dispatchNoWeighmentWh.warehouseId = da[0]
            vm.dispatchNoWeighmentWh.plantName = da[1]
            getPurchaseOrderByStorageLocation(da[0])
            clearExistingData()
            isEdit = false
            model?.isEditData = false
        } else if (!isVendor) {
            getWeight(data)
            selectedPurchaseOrder?.storageLocationName = vm.dispatchNoWeighmentWh.plantName
            binding.tvstoValue.text = data
            vm.dispatchNoWeighmentWh.purchaseDocNum = selectedPurchaseOrder?.purchaseDocNum
            vm.dispatchNoWeighmentWh.purchaseDocDesc = selectedPurchaseOrder?.purchaseDocDesc
            vm.dispatchNoWeighmentWh.plantId = selectedPurchaseOrder?.plantId
            vm.dispatchNoWeighmentWh.weighBridgeId = ""
            isEdit = false
            model?.isEditData = false
            if (AppUtils.isOnline()) {
                vm.getWeighScaleWithLotAndMaterial(
                    selectedPurchaseOrder?.warehouseId ?: "",
                    selectedPurchaseOrder?.purchaseDocNum ?: "", selectedPurchaseOrder?.purchaseDocDesc ?: ""
                )
            }
            vm.weighScaleWithLot.observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    vm.dispatchNoWeighmentWh = it.dispatch
                    vm.dispatchNoWeighmentWh.isEditData = isEdit
                    updateUIWithDbData()
                } else {
                    binding.tvWayBillValue.setText("")
                    binding.tvVendorValue.text = ""
                    binding.tvTruckNoValue.setText("")
                    binding.tvDriverNameValue.setText("")
                    binding.tvDriverNoValue.setText("")
                }
            })
            /*vm.getWeighScaleWithLotAndMaterial(
                selectedPurchaseOrder?.plantId?.plus(
                    selectedPurchaseOrder?.purchaseDocNum?.plus(selectedPurchaseOrder?.purchaseDocDesc)
                ).plus(selectedPurchaseOrder?.materialCode)
            )*/
            val materialSubStr = selectedPurchaseOrder?.materialCode?.substring(6)
            vm.getProduct(materialSubStr ?: "")
            binding.tvStoWeightValue.text =
                selectedPurchaseOrder?.openQuantity.plus(" ").plus(selectedPurchaseOrder?.meins)
            validateThirdPartyMaterialMapping(materialSubStr ?: "")
            enableProceed()
        } else if (isVendor) {
            binding.tvVendorValue.text = data
            vm.dispatchNoWeighmentWh.transportVendor = data.split("-")[1]
            vm.dispatchNoWeighmentWh.transportVendorID = data.split("-")[0]
            enableProceed()
        }
    }

    private fun clearExistingData() {
        binding.tvVendorValue.text = ""
        binding.tvWayBillValue.setText("")
        binding.tvTruckNoValue.setText("")
        binding.tvDriverNameValue.setText("")
        binding.tvDriverNoValue.setText("")
        binding.tvstoValue.text = ""
        binding.tvStoWeightValue.text = ""
        binding.tvMaterialValue.text = ""
    }

    private fun getWeight(purchaseId: String) {
        selectedPurchaseOrder =
            purchaseOrder.single { s -> s.purchaseDocNum + "-" + s.purchaseDocDesc == purchaseId }
    }

    private fun updateUIWithDbData() {
        binding.tvVendorValue.text =
            vm.dispatchNoWeighmentWh.transportVendorID.plus("-").plus(vm.dispatchNoWeighmentWh.transportVendor)
        binding.tvTruckNoValue.setText(vm.dispatchNoWeighmentWh.vehicleNumber)
        binding.tvDriverNameValue.setText(vm.dispatchNoWeighmentWh.driverName)
        binding.tvDriverNoValue.setText(vm.dispatchNoWeighmentWh.driverPhoneNumber)
        binding.tvWayBillValue.setText(vm.dispatchNoWeighmentWh.wayBillNo)
    }

    private fun updateOfflinePurchaseOrder(list: List<VegaCocoaPurchaseOrders>) {
        purchaseOrder.clear()
        purchaseOrder.addAll(list)
        val location =
            purchaseOrder.map { it.warehouseId.plus("-").plus(getPlantName(it.warehouseId)) }
                .toSet()
        storageLocation.addAll(location)
    }


    private fun updatePurchaseOrder(response: Resource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        purchaseOrderList = it1 as MutableList<VegaCocoaPurchaseOrder>
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

    private fun getPurchaseOrderByStorageLocation(storageLocationCode: String) {
        STONumbers.clear()
        val orderList = purchaseOrder.filter { it.warehouseId == storageLocationCode.trim() }
        STONumbers.addAll(orderList.map { it.purchaseDocNum.plus("-").plus(it.purchaseDocDesc) }.toSet().toList())
    }

    private fun filterSTONumber() {
        for (item in purchaseOrderList) {
            purchaseOrder.addAll(item.purchaseOrders)
        }
        val location =
            purchaseOrder.map { it.warehouseId.plus("-").plus(getPlantName(it.warehouseId)) }
                .toSet()
        storageLocation.addAll(location)
    }

    private fun getCurrentDate(): String {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        return simpleDateFormat.format(Date())
    }

    private fun validateThirdPartyMaterialMapping(materialCode: String) {
        val material = thirdPartyMaterialList.filter { materialCode.contains(it.materialCode) }
        if (material.isNotEmpty()) {
            isThirdPartyMaterial = true
        }
    }

    fun onBackResume() {
        vm.dispatchNoWeighmentWh.isEditData = true
        isEdit = true
        vm.getWeighScaleWithLotAndMaterial(
            selectedPurchaseOrder?.warehouseId ?: "",
            selectedPurchaseOrder?.purchaseDocNum ?: "", selectedPurchaseOrder?.purchaseDocDesc ?: ""
        )
    }
}
