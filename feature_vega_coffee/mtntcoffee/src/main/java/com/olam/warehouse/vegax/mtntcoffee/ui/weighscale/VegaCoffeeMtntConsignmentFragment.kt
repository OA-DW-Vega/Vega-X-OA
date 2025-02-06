package com.olam.warehouse.vegax.mtntcoffee.ui.weighscale

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaCoffeePurchaseOrderMaterialModel
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcoffee.R
import com.olam.warehouse.vegax.mtntcoffee.data.domain.model.VegaCoffeePurchaseOrder
import com.olam.warehouse.vegax.mtntcoffee.data.domain.model.VegaCoffeePurchaseOrders
import com.olam.warehouse.vegax.mtntcoffee.databinding.FragmentWeighscaleConsignmentLayoutBinding
import com.olam.warehouse.vegax.mtntcoffee.databinding.ItemMaterialLayoutBinding
import com.olam.warehouse.vegax.mtntcoffee.ui.VegaCoffeeCustomSingleSelectDialog
import com.olam.warehouse.vegax.mtntcoffee.ui.VegaCoffeeMtntViewModel
import com.olam.warehouse.vegax.mtntcoffee.ui.VegaCoffeeReplaceFragmentCallback
import com.olam.warehouse.vegax.mtntcoffee.ui.VegaCoffeeSingleSelectListener
import com.olam.warehouse.vegax.mtntcoffee.utils.MTNT_WEIGHSCALE
import com.olam.warehouse.vegax.mtntcoffee.utils.WEIGHSCALE_ADD_LOT
import com.olam.warehouse.vegax.mtntcoffee.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class VegaCoffeeMtntConsignmentFragment : BaseFragment(),
    VegaCoffeeSingleSelectListener {

    override val layoutResourceId = R.layout.fragment_weighscale_consignment_layout
    private lateinit var binding: FragmentWeighscaleConsignmentLayoutBinding

    private val vm: VegaCoffeeMtntViewModel by viewModel()
    private var model: VegaCocoaDispatchWB? = null
    private var supplierList = mutableListOf<VegaVendor>()
    private var customDialog: VegaCoffeeCustomSingleSelectDialog? = null
    private var purchaseOrderList = mutableListOf<VegaCoffeePurchaseOrder>()
    private var materialCode: String = ""
    private var storageLocation = ArrayList<String>()
    private var STONumbers = ArrayList<String>()
    private var wareHouseId = ""
    private var selectedPurchaseOrder: VegaCoffeePurchaseOrders? = null
    private var materials: ArrayList<VegaCoffeePurchaseOrders>? = null
    private var purchaseOrder = ArrayList<VegaCoffeePurchaseOrders>()
    private lateinit var callback: VegaCoffeeReplaceFragmentCallback
    private var materialModelList: ArrayList<VegaCoffeePurchaseOrderMaterialModel> = ArrayList()
    private var productList = emptyList<VegaMaterial>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as VegaCoffeeReplaceFragmentCallback
    }

    companion object {
        fun newInstance() =
            VegaCoffeeMtntConsignmentFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentWeighscaleConsignmentLayoutBinding.inflate(layoutInflater)
        initUI()
        vm.getSuppliers()
        return binding.root
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

        vm.getPurchaseOrder("")
        binding.btnProceed.setOnClickListener { validateProceed() }
        enableProceed()
        vm.purchaseOrder.observe(viewLifecycleOwner, Observer { updatePurchaseOrder(it) })
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
                }
            })


    }

    private fun validateProceed() =
        if (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverName.text.isNotEmpty()
            && binding.tvTruckNoValue.text.toString().isNotEmpty() && binding.tvVendorValue.text.isNotEmpty()
        ) {
            prepareTruckWb()
            vm.saveWeighBridgeDetails()
            materialModelList.forEach { it.weighBridgeId = vm.dispatchWh.weighBridgeId }
            saveMaterialDetails(materialModelList)
            callback.replaceFragment(WEIGHSCALE_ADD_LOT, vm.dispatchWh)
        } else {
            showSnack(getString(R.string.mandatory))
        }

    private fun prepareTruckWb() {
        if (vm.dispatchWh.weighBridgeId.isEmpty()) {
            val randomDouble = "TMP".plus(Random.nextLong().toString())
            vm.dispatchWh.weighBridgeId = randomDouble
            vm.dispatchWh.weighBridgeId = randomDouble
        }
        vm.dispatchWh.purchaseDocNum = selectedPurchaseOrder?.purchaseDocNum
        vm.dispatchWh.purchaseDocDesc = selectedPurchaseOrder?.purchaseDocDesc
        vm.dispatchWh.weighBridgeType = MTNT_WEIGHSCALE
        vm.dispatchWh.netWeight = selectedPurchaseOrder?.menge
        vm.dispatchWh.unitsOfMeasure = selectedPurchaseOrder?.meins
        vm.dispatchWh.plantId = selectedPurchaseOrder?.plantId ?: ""
        vm.dispatchWh.recStorageLocationCode = selectedPurchaseOrder?.storageLocationCode
        vm.dispatchWh.recPlantId = selectedPurchaseOrder?.plantId
        vm.dispatchWh.vehicleNumber = binding.tvTruckNoValue.text.toString()
        vm.dispatchWh.driverName = binding.tvDriverNameValue.text.toString()
        vm.dispatchWh.driverPhoneNumber = binding.tvDriverNoValue.text.toString()
        vm.dispatchWh.erdat = getCurrentDate()
    }

    private fun showSingleSelectDialog(isWh: Boolean, title: String, isVendor: Boolean) {
        val list: java.util.ArrayList<String>
        if (isVendor) {
            list = supplierList.map { it.vendorCode.plus("-").plus(it.vendorName) } as ArrayList<String>
        } else if (isWh) {
            list = storageLocation
        } else {
            list = STONumbers
        }

        customDialog =
            VegaCoffeeCustomSingleSelectDialog(
                title,
                isWh, isVendor,
                list,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }


    private fun getPlantName(plantId: String): String {
        val plants = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.PLANT_LIST, ""))
        val plant = plants.singleOrNull { it.plantId == plantId }
        return plant?.plantName ?: ""
    }

    private fun allPlants(): List<Plant> {
        return Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.PLANT_LIST, ""))
    }

    private fun enableProceed() {
        val enable =
            (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverName.text.isNotEmpty()
                    && binding.tvTruckNoValue.text.toString().isNotEmpty() && binding.tvVendorValue.text.isNotEmpty()
                    )
        if (enable) {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btnProceed.isEnabled = enable
    }

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean) {
        customDialog?.dismiss()
        if (isWh) {
            binding.tvWhValue.text = data
            wareHouseId = data
            val da = data.split("-").toTypedArray()
            //vm.getPurchaseOrder(da[0])
            vm.dispatchWh.storageLocationCode = da[0]
            vm.dispatchWh.plantName = da[1]
            getPurchaseOrderByStorageLocation(da[0])
        } else if (!isVendor) {
            if (materialModelList.isNotEmpty()) {
                val material = materialModelList
                vm.deleteMaterialData(material[0].weighBridgeId)
            }
            getWeight(data)
            selectedPurchaseOrder?.storageLocationName = vm.dispatchWh.plantName
            binding.tvstoValue.text = data
            vm.dispatchWh.purchaseDocNum = selectedPurchaseOrder?.purchaseDocNum
            vm.dispatchWh.purchaseDocDesc = selectedPurchaseOrder?.purchaseDocDesc
            vm.dispatchWh.plantId = selectedPurchaseOrder?.plantId
            setUpMaterialAdapter()
            vm.weighScaleWithLotMaterial.observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    vm.dispatchWh = it.dispatch
                    updateUIWithDbData()
                } else {
                    binding.tvVendorValue.text = ""
                    binding.tvTruckNoValue.setText("")
                    binding.tvDriverNameValue.setText("")
                    binding.tvDriverNoValue.setText("")
                }
            })
            vm.getWeighScaleWithLotAndMaterial(
                vm.dispatchWh.plantId ?: "",
                vm.dispatchWh.purchaseDocNum ?: ""
            )
            enableProceed()
        } else if (isVendor) {
            binding.tvVendorValue.text = data
            vm.dispatchWh.transportVendor = data.split("-")[1]
            vm.dispatchWh.transportVendorID = data.split("-")[0]
            enableProceed()
        }
    }

    private fun getWeight(purchaseId: String) {
        materialModelList.clear()
        materials =
            purchaseOrder.filter { s -> s.purchaseDocNum == purchaseId } as ArrayList<VegaCoffeePurchaseOrders>
        materials?.forEach {
            val materialModel = VegaCoffeePurchaseOrderMaterialModel()
            materialModel.materialCode = it.materialCode
            materialModel.soNumber = it.purchaseDocNum
            materialModel.soWeight = it.menge
            materialModel.uom = it.meins
            materialModel.purchaseOrderNum = it.purchaseDocNum ?: ""
            materialModel.purchaseOrderDesc = it.purchaseDocDesc ?: ""
            materialModelList.add(materialModel)
        }
        selectedPurchaseOrder = materials?.get(0)
    }

    private fun updateUIWithDbData() {
        binding.tvVendorValue.text = vm.dispatchWh.transportVendor
        binding.tvTruckNoValue.setText(vm.dispatchWh.vehicleNumber)
        binding.tvDriverNameValue.setText(vm.dispatchWh.driverName)
        binding.tvDriverNoValue.setText(vm.dispatchWh.driverPhoneNumber)
    }


    private fun saveMaterialDetails(list: ArrayList<VegaCoffeePurchaseOrderMaterialModel>) {
        vm.saveMaterialDetails(list)
    }

    private fun setUpMaterialAdapter() {
        binding.rvMaterialList.setUpAdapter(
            materialModelList,
            R.layout.item_material_layout,
            ItemMaterialLayoutBinding::inflate,
            { item, pos, bindItem ->

                val materialName =
                    productList.single { item.materialCode.contains(it.materialCode) }
                item.materialName = materialName.materialName
                bindItem.tvMaterialName.text = item.materialName
                bindItem.tvStoWeightValue.text =
                    item.soWeight?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                        .plus(item.uom)
                bindItem.tvDispatchWeight.visibility = View.GONE
                bindItem.tvDispatchWeightValue.visibility = View.GONE
            })
    }

    private fun updatePurchaseOrder(response: Resource<GenericReqAndResp<List<VegaCoffeePurchaseOrder>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        purchaseOrderList = it1 as MutableList<VegaCoffeePurchaseOrder>
                    }
                    filterSTONumber()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun getPurchaseOrderByStorageLocation(storageLocationCode: String) {
        STONumbers.clear()
        val orderList = purchaseOrder.filter { it.storageLocationCode == storageLocationCode.trim() }
        STONumbers.addAll(orderList.map { it.purchaseDocNum ?: "" }.toSet().toList())

    }

    private fun filterSTONumber() {
        for (item in purchaseOrderList) {
            purchaseOrder.addAll(item.purchaseOrders)
        }
        val location = purchaseOrder.map { it.storageLocationCode.plus(" - ").plus(it.storageLocationName) }.toSet()
        storageLocation.addAll(location)
    }


    private fun getCurrentDate(): String {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        return simpleDateFormat.format(Date())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("mtntcoffee.ui.weighbridge").title("IVC/Coffee/MTNT/Weighscale Consignment")
            .with(tracker)
    }
}
