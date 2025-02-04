package com.olam.warehouse.vegax.mtntghana.ui.weighscale

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaGhanaPurchaseOrderMaterialModel
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.mtntghana.R
import com.olam.warehouse.vegax.mtntghana.data.domain.model.VegaGhanaMtntPurchaseOrder
import com.olam.warehouse.vegax.mtntghana.data.domain.model.VegaGhanaMtntPurchaseOrders
import com.olam.warehouse.vegax.mtntghana.databinding.FragmentGhanaWeighscaleConsignmentLayoutBinding
import com.olam.warehouse.vegax.mtntghana.ui.VegaGhanaCustomSingleSelectDialog
import com.olam.warehouse.vegax.mtntghana.ui.VegaGhanaMtntViewModel
import com.olam.warehouse.vegax.mtntghana.ui.VegaGhanaReplaceFragmentCallback
import com.olam.warehouse.vegax.mtntghana.ui.VegaGhanaSingleSelectListener
import com.olam.warehouse.vegax.mtntghana.utils.DISPATCH_OFFLINE_SUMMARY
import com.olam.warehouse.vegax.mtntghana.utils.MTNT_WEIGHSCALE
import com.olam.warehouse.vegax.mtntghana.utils.WEIGHSCALE_ADD_LOT
import com.olam.warehouse.vegax.mtntghana.utils.getColor
import kotlinx.android.synthetic.main.item_ghana_material_layout.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class VegaGhanaMtntConsignmentFragment : BaseFragment(),
    VegaGhanaSingleSelectListener {

    override val layoutResourceId = R.layout.fragment_ghana_weighscale_consignment_layout
    private lateinit var binding: FragmentGhanaWeighscaleConsignmentLayoutBinding

    private val vm: VegaGhanaMtntViewModel by viewModel()
    private var model: VegaCocoaDispatchWB? = null
    private var supplierList = mutableListOf<VegaVendor>()
    private var customDialog: VegaGhanaCustomSingleSelectDialog? = null
    private var purchaseOrderList = mutableListOf<VegaGhanaMtntPurchaseOrder>()
    private var materialCode: String = ""
    private var storageLocation = ArrayList<String>()
    private var STONumbers = ArrayList<String>()
    private var filteredPurchaseOrderList = ArrayList<VegaGhanaMtntPurchaseOrders>()
    private var wareHouseId = ""
    private var selectedPurchaseOrder: VegaGhanaMtntPurchaseOrders? = null
    private var materials: ArrayList<VegaGhanaMtntPurchaseOrders>? = null
    private var purchaseOrder = ArrayList<VegaGhanaMtntPurchaseOrders>()
    private lateinit var callback: VegaGhanaReplaceFragmentCallback
    private var materialModelList: ArrayList<VegaGhanaPurchaseOrderMaterialModel> = ArrayList()
    private var productList = emptyList<VegaMaterial>()
    private var sendingWareHouseList = mutableListOf<VegaCustomStLocation>()
    private var selectedSendingWH = ""
    private var selectedDestinationWH = ""
    private var selectedDestinationWHName = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as VegaGhanaReplaceFragmentCallback
    }

    companion object {
        fun newInstance() =
            VegaGhanaMtntConsignmentFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentGhanaWeighscaleConsignmentLayoutBinding.inflate(layoutInflater)
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

        binding.tvSendingWHValue.setOnClickListener {
            if (sendingWareHouseList.size > 1) {
//                binding.tvSendingWHValue.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.ic_add_icon, 0)
                showSingleSelectDialog(
                    false,
                    getString(R.string.select_sent_wh), true
                )
            }
        }

        if (AppUtils.isOnline())
            vm.getPurchaseOrder("")
        else
            vm.getPurchaseOrderOffline()

//        vm.getPurchaseOrder("")
        binding.btnProceed.setOnClickListener { validateProceed() }
        enableProceed()
        vm.purchaseOrder.observe(viewLifecycleOwner, Observer { updatePurchaseOrder(it) })
        vm.purchaseOrderOffline.observe(viewLifecycleOwner, Observer { updatePurchaseOrderOffline(it) })
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
        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            sendingWareHouseList = it.toMutableList()
            if (sendingWareHouseList.size == 1) {
                sendingWareHouseList.forEach {
                    binding.tvSendingWHValue.text = sendingWareHouseList[0].procureLocationCode.plus(" - ").plus(
                        sendingWareHouseList[0].procureLocationName
                    )
                    selectedSendingWH = sendingWareHouseList[0].procureLocationCode
                }
            }
        })
        vm.getCustomLocations()

        vm.truck.observe(this, Observer {
            if (it != null && it.size > 0) {
                binding.llOffline.visible()
            } else {
                binding.llOffline.gone()
            }
        })
        vm.getOfflineTruckDetails()
        binding.llOffline.setOnClickListener { moveToOfflineSummary() }

    }

    private fun moveToOfflineSummary() {
        callback.replaceFragment(DISPATCH_OFFLINE_SUMMARY)
    }

    private fun validateProceed() =
        if (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverName.text.isNotEmpty()
            && binding.tvTruckNoValue.text.toString().isNotEmpty() && binding.tvSendingWHValue.text.toString()
                .isNotEmpty()&& binding.tvPhDocNo.text.toString()
                .isNotEmpty()
        ) {
            prepareTruckWb()
            vm.saveWeighBridgeDetails()
            materialModelList.forEach { it.weighBridgeId = vm.dispatchWh.weighBridgeId }
            saveMaterialDetails(materialModelList)
            println("Roshna => $materialModelList")
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
        vm.dispatchWh.storageLocationCode = binding.tvSendingWHValue.text.toString()
        vm.dispatchWh.recStorageLocationCode = selectedSendingWH
        vm.dispatchWh.recPlantId = selectedPurchaseOrder?.warehouseId
        vm.dispatchWh.destinationWH = selectedDestinationWH
        vm.dispatchWh.destinationWHName = selectedDestinationWHName
        vm.dispatchWh.vehicleNumber = binding.tvTruckNoValue.text.toString()
        vm.dispatchWh.driverName = binding.tvDriverNameValue.text.toString()
        vm.dispatchWh.driverPhoneNumber = binding.tvDriverNoValue.text.toString()
        vm.dispatchWh.frbnr1 = binding.tvPhDocNo.text.toString()
        vm.dispatchWh.erdat = getCurrentDate()
    }

    private fun showSingleSelectDialog(isWh: Boolean, title: String, isSendingWH: Boolean) {
        val list: java.util.ArrayList<String>
        if (isSendingWH) {
            list = sendingWareHouseList.map{data -> data.procureLocationCode.plus("-").plus(data.procureLocationName)} as  ArrayList<String>
        } else if (isWh) {
            list = storageLocation
        } else {
            list = STONumbers
        }

        customDialog =
            VegaGhanaCustomSingleSelectDialog(
                title,
                isWh, isSendingWH,
                list,
                activity!!,
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
                    && binding.tvTruckNoValue.text.toString().isNotEmpty()
                    )
        if (enable) {
            binding.btnProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btnProceed.isEnabled = enable
    }

    override fun clickOnItem(data: String, isWh: Boolean, isSendingWH: Boolean) {
        customDialog?.dismiss()
        if (isWh) {
            binding.tvWhValue.text = data
            wareHouseId = data
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
            binding.rvMaterialList.adapter?.notifyDataSetChanged()

        } else if (!isSendingWH) {
//            vm.removeLotList()
            val da = data.split(" (").toTypedArray()

//            vm.deleteMaterialData()
            if (materialModelList.isNotEmpty()) {
                val material = materialModelList
                vm.deleteMaterialData(material[0].weighBridgeId)
                vm.removeGhanaLotList(material[0].weighBridgeId)
            }
            binding.tvmaterialValue.text = ""

            materialModelList.clear()
            setUpMaterialAdapter()

//            getWeight(data)
            getWeight(da[0])

            selectedPurchaseOrder?.storageLocationName = vm.dispatchWh.plantName
            binding.tvstoValue.text = da[0]
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
            binding.tvSendingWHValue.text = data
            val da = data.split("-").toTypedArray()

            selectedSendingWH = da[0]
            getPurchaseOrderByStorageLocation("")
            enableProceed()
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

    private fun updateUIWithDbData() {
        binding.tvVendorValue.text = vm.dispatchWh.transportVendor
        binding.tvTruckNoValue.setText(vm.dispatchWh.vehicleNumber)
        binding.tvDriverNameValue.setText(vm.dispatchWh.driverName)
        binding.tvDriverNoValue.setText(vm.dispatchWh.driverPhoneNumber)
    }


    private fun saveMaterialDetails(list: ArrayList<VegaGhanaPurchaseOrderMaterialModel>) {
        vm.saveMaterialDetails(list)
    }

    private fun setUpMaterialAdapter() {
        binding.rvMaterialList.setUp(materialModelList, R.layout.item_ghana_material_layout, { item, pos ->

            val materialName = productList.single { item.materialCode.contains(it.materialCode) }
            item.materialName = materialName.materialName
            tvMaterialName.text = item.materialName
            tvStoWeightValue.text = item.soWeight?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                .plus(item.uom)
            tvDispatchWeight.visibility = View.GONE
            tvDispatchWeightValue.visibility = View.GONE
        })
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
            val location = purchaseOrder
                .map {it.warehouseId.plus("-").plus(getMTNTPlantName(it.warehouseId ?: "")).plus("-").plus(it.storageLocationCode ?: "").plus("-").plus(it.storageLocationName ?: "") }.toSet()
            storageLocation.addAll(location)

        }
    }

    private fun getPurchaseOrderByStorageLocation(warehouseId: String) {
        STONumbers.clear()
        val orderList = purchaseOrder.filter { it.warehouseId == warehouseId.trim() }.filter { it.storageLocationCode == selectedDestinationWH }
            .filter { it.issueLocation == selectedSendingWH }
//            .filter { it.warehouseId == selectedDestinationWH }
//          if selectedSendingWH is not empty
        filteredPurchaseOrderList = orderList as ArrayList<VegaGhanaMtntPurchaseOrders>

       /*var multiMaterialPO =  filteredPurchaseOrderList.groupingBy { it.purchaseDocNum }.eachCount().filter{it.value > 1}
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

        }*/


        STONumbers.addAll(orderList.map { it.purchaseDocNum ?: "" }.toSet().toList())

    }

    private fun getMTNTPlantName(plantId: String): String {
        val plants = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.MTNT_PLANT_LIST, ""))
        val plant = plants.singleOrNull { it.plantId == plantId }
        return plant?.plantName ?: ""
    }

    private fun filterSTONumber() {
        for (item in purchaseOrderList) {
            purchaseOrder.addAll(item.purchaseOrders)
        }
//        val location = purchaseOrder.map {it.warehouseId.plus("-").plus(getMTNTPlantName(it.warehouseId ?: "")) }.toSet()
        //.filter { !it.storageLocationCode.isNullOrEmpty() }
        val location = purchaseOrder
            .map {it.warehouseId.plus("-").plus(getMTNTPlantName(it.warehouseId ?: "")).plus("-").plus(it.storageLocationCode ?: "").plus("-").plus(it.storageLocationName ?: "") }.toSet()
        storageLocation.addAll(location)
    }


    private fun getCurrentDate(): String {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        return simpleDateFormat.format(Date())
    }

}
