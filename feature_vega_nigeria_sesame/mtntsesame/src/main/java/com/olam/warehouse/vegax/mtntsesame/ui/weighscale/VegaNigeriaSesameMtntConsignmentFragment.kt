package com.olam.warehouse.vegax.mtntsesame.ui.weighscale

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.mtntsesame.R
import com.olam.warehouse.vegax.mtntsesame.data.domain.model.VegaNigeriaSesameMtntPurchaseOrder
import com.olam.warehouse.vegax.mtntsesame.data.domain.model.VegaNigeriaSesameMtntPurchaseOrders
import com.olam.warehouse.vegax.mtntsesame.databinding.FragmentNigeriaSesameWeighscaleConsignmentLayoutBinding
import com.olam.warehouse.vegax.mtntsesame.ui.VegaNigeriaSesameCustomSingleSelectDialog
import com.olam.warehouse.vegax.mtntsesame.ui.VegaNigeriaSesameMtntViewModel
import com.olam.warehouse.vegax.mtntsesame.ui.VegaNigeriaSesameReplaceFragmentCallback
import com.olam.warehouse.vegax.mtntsesame.ui.VegaNigeriaSesameSingleSelectListener
import com.olam.warehouse.vegax.mtntsesame.utils.MTNT_WEIGHSCALE
import com.olam.warehouse.vegax.mtntsesame.utils.WEIGHSCALE_ADD_LOT
import com.olam.warehouse.vegax.mtntsesame.utils.convertMtToKg
import com.olam.warehouse.vegax.mtntsesame.utils.getColor
import kotlinx.android.synthetic.main.item_nigeria_sesame_material_layout.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class VegaNigeriaSesameMtntConsignmentFragment : BaseFragment(),
    VegaNigeriaSesameSingleSelectListener {

    override val layoutResourceId = R.layout.fragment_nigeria_sesame_weighscale_consignment_layout
    private lateinit var binding: FragmentNigeriaSesameWeighscaleConsignmentLayoutBinding

    private val vm: VegaNigeriaSesameMtntViewModel by viewModel()
    private var model: VegaCocoaDispatchWB? = null
    private var supplierList = mutableListOf<VegaVendor>()
    private var customDialog: VegaNigeriaSesameCustomSingleSelectDialog? = null
    private var purchaseOrderList = mutableListOf<VegaNigeriaSesameMtntPurchaseOrder>()
    private var materialCode: String = ""
    private var storageLocation = ArrayList<String>()
    private var STONumbers = ArrayList<String>()
    private var wareHouseId = ""
    private var selectedPurchaseOrder: VegaNigeriaSesameMtntPurchaseOrders? = null
    private var selectedMaterial :String =""
    private var materials: ArrayList<VegaNigeriaSesameMtntPurchaseOrders>? = null
    private var purchaseOrder = ArrayList<VegaNigeriaSesameMtntPurchaseOrders>()
    private var filteredPurchaseOrderList = ArrayList<VegaNigeriaSesameMtntPurchaseOrders>()
    private lateinit var callback: VegaNigeriaSesameReplaceFragmentCallback
    private var materialModelList: ArrayList<VegaCoffeePurchaseOrderMaterialModel> = ArrayList()
    private var productList = emptyList<VegaMaterial>()
    private var warehouseWithMtn = VegaReceivingWarehouseWithMtns()
    private var wareHouseList: MutableList<VegaSupplyStorageLocation> = mutableListOf()
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var sendingWareHouseList = mutableListOf<VegaCustomStLocation>()
    private var selectedMaterialList: ArrayList<VegaCoffeePurchaseOrderMaterialModel> = ArrayList()
    private var selectedSendingWH =""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as VegaNigeriaSesameReplaceFragmentCallback
    }

    companion object {
        fun newInstance() =
            VegaNigeriaSesameMtntConsignmentFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentNigeriaSesameWeighscaleConsignmentLayoutBinding.inflate(layoutInflater)
        initUI()
        vm.getSuppliers()
        return binding.root
    }

    private fun initUI() {
//        binding.tvWhValue.setOnClickListener {
//            showReceivingLocationDialog(customLocationList.filter {
//                !it.storageLocationType.equals(
//                    "B"
//                )
//            })
//        }
//        binding.tvSendingWHValue.setOnClickListener { showDispatchWHDialog(wareHouseList) } // Sending
//        binding.tvstoValue.setOnClickListener { showStoDialog(warehouseWithMtn) }
//        binding.tvmaterialValue.setOnClickListener { showProductDialog(productList.filter { it.mtnNumber == binding.tvSto.text.toString() }) }
//
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnProceed, it, true)
        }
        binding.tvWhValue.setOnClickListener {
            showSingleSelectDialog(true, getString(R.string.select_dest_wh), false, false)
        }
        binding.tvSendingWHValue.setOnClickListener {
            if (sendingWareHouseList.size > 1) {
//                binding.tvSendingWHValue.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.ic_add_icon, 0)
                showSingleSelectDialog(
                    false,
                    getString(R.string.select_sent_wh), true, false
                )
            }
        }
        binding.tvstoValue.setOnClickListener {
            if (binding.tvWhValue.text.toString().isNotEmpty()) showSingleSelectDialog(
                false,
                getString(R.string.select_sto), false,false
            )
        }
        binding.tvmaterialValue.setOnClickListener {
            if (binding.tvWhValue.text.toString().isNotEmpty()) showSingleSelectDialog(
                false,
                getString(R.string.select_material), false,true
            )
        }

        vm.getPurchaseOrder("")
        binding.btnProceed.setOnClickListener { validateProceed() }
        enableProceed()
        vm.purchaseOrder.observe(viewLifecycleOwner, Observer { updatePurchaseOrder(it) })
        binding.tvDriverNameValue.onChange { enableProceed() }
        binding.tvTruckNoValue.onChange { enableProceed() }
        binding.tvDriverNoValue.onChange { enableProceed() }


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
        // vm.location.observe(viewLifecycleOwner, Observer { sendingWareHouseList = it.toMutableList() })
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
        //vm.getSendingWHLocations()


    }

    private fun validateProceed() =
        if (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverNameValue.text.toString().isNotEmpty()
            && binding.tvTruckNoValue.text.toString().isNotEmpty() && binding.tvSendingWHValue.text.toString().isNotEmpty()
            && binding.tvmaterialValue.text.toString().isNotEmpty()
        ) {
            prepareTruckWb()
            vm.saveWeighBridgeDetails()
            selectedMaterialList.forEach { it.weighBridgeId = vm.dispatchWh.weighBridgeId }
            saveMaterialDetails(selectedMaterialList)
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
        vm.dispatchWh.netWeight = selectedPurchaseOrder?.openQuantity
        vm.dispatchWh.unitsOfMeasure = selectedPurchaseOrder?.meins
        vm.dispatchWh.plantId = selectedPurchaseOrder?.plantId ?: ""
        vm.dispatchWh.storageLocationCode = binding.tvSendingWHValue.text.toString()
        vm.dispatchWh.recStorageLocationCode = selectedSendingWH
        vm.dispatchWh.recPlantId = selectedPurchaseOrder?.warehouseId
        vm.dispatchWh.materialName = binding.tvmaterialValue.text.toString()
        vm.dispatchWh.vehicleNumber = binding.tvTruckNoValue.text.toString()
        vm.dispatchWh.driverName = binding.tvDriverNameValue.text.toString()
        vm.dispatchWh.driverPhoneNumber = binding.tvDriverNoValue.text.toString()
        vm.dispatchWh.erdat = getCurrentDate()
    }

    private fun showSingleSelectDialog(isWh: Boolean, title: String, isSendingWH: Boolean,isMaterial:Boolean) {
        var list: ArrayList<String> = ArrayList()
        if (isSendingWH) {
            list = sendingWareHouseList.map{data -> data.procureLocationCode.plus("-").plus(data.procureLocationName)} as  ArrayList<String>
        } else if (isWh) {
            list = storageLocation
        }else if(isMaterial){
            if(materialModelList.size >1)
             list = materialModelList.map { it.materialName }.distinct() as ArrayList<String>
        } else {
            list = STONumbers
        }

        if(list.isNotEmpty())
        {
            customDialog =
                VegaNigeriaSesameCustomSingleSelectDialog(
                    title,
                    isWh, isSendingWH,isMaterial,
                    list,
                    activity!!,
                    this
                )
            customDialog?.show()
            customDialog?.setCanceledOnTouchOutside(false)
        }

    }

    private fun enableProceed() {
        val enable =
            (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty()
                    && binding.tvTruckNoValue.text.toString().isNotEmpty() && binding.tvmaterialValue.text.toString().isNotEmpty()
                    )
        if (enable) {
            binding.btnProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btnProceed.isEnabled = enable
    }

    override fun clickOnItem(data: String, isWh: Boolean, isSendingWH: Boolean,isMaterial: Boolean) {
        customDialog?.dismiss()
        if (isWh) {
            binding.tvWhValue.text = data
            binding.tvstoValue.text = ""
            binding.tvmaterialValue.text = ""
            materialModelList.clear()
            selectedMaterialList.clear()
            binding.rvMaterialList.adapter?.notifyDataSetChanged()
            wareHouseId = data
            val da = data.split("-").toTypedArray()
            vm.dispatchWh.recPlantId=da[0]
            vm.dispatchWh.plantName = da[1]
            getPurchaseOrderByStorageLocation(da[0])
        }
        else if (isMaterial){

            binding.tvmaterialValue.text = data
            selectedMaterial = data
            selectedMaterialList = materialModelList.filter { it.materialName.toString() == data} as ArrayList<VegaCoffeePurchaseOrderMaterialModel>
            setUpMaterialAdapter()
            if (materialModelList.isNotEmpty()) {
                val material = materialModelList
                vm.deleteMaterialData(material[0].weighBridgeId)
            }

        }
        else if (!isSendingWH) {
            vm.removeLotList()

            vm.deleteMaterialData()
            if (materialModelList.isNotEmpty()) {
                val material = materialModelList
                vm.deleteMaterialData(material[0].weighBridgeId)
            }
            binding.tvmaterialValue.text = ""

            selectedMaterialList.clear()
            setUpMaterialAdapter()

            getWeight(data)

            selectedPurchaseOrder?.storageLocationName = vm.dispatchWh.plantName
            binding.tvstoValue.text = data
            vm.dispatchWh.purchaseDocNum = selectedPurchaseOrder?.purchaseDocNum
            vm.dispatchWh.purchaseDocDesc = selectedPurchaseOrder?.purchaseDocDesc
            vm.dispatchWh.plantId = selectedPurchaseOrder?.plantId
            vm.weighScaleWithLotMaterial.observe(viewLifecycleOwner, Observer {
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
            )
            enableProceed()
        } else if (isSendingWH) {
            binding.tvSendingWHValue.text = data
            val da = data.split("-").toTypedArray()

            selectedSendingWH = da[0]
            enableProceed()
        }
    }

    private fun getListforSelectedMaterial(materialName: String) {
        val orderList = purchaseOrder.filter { it.materialName == materialName.trim() }
        STONumbers.addAll(orderList.map { it.purchaseDocNum ?: "" }.toSet().toList())

    }

    private fun getWeight(purchaseId: String) {
        materialModelList.clear()
        materials?.clear()
        selectedMaterialList.clear()

        materials =
            filteredPurchaseOrderList.filter { s ->
                s.purchaseDocNum == purchaseId
            } as ArrayList<VegaNigeriaSesameMtntPurchaseOrders>

        materials?.forEach {
            val materialModel = VegaCoffeePurchaseOrderMaterialModel()
            materialModel.materialCode = it.materialCode
            materialModel.soNumber = it.purchaseDocNum
            materialModel.soWeight = convertMtToKg(it.openQuantity!!, it.meins.toString())
            materialModel.uom = it.meins
            materialModel.purchaseOrderNum = it.purchaseDocNum ?: ""
            materialModel.purchaseOrderDesc = it.purchaseDocDesc ?: ""
            materialModelList.add(materialModel)
        }

        materialModelList.forEach { item ->
            try {
                val materialName =
                    productList.single { item.materialCode.contains(it.materialCode) }
                item.materialName = materialName.materialName
            } catch (e: NoSuchElementException) {
                e.printStackTrace()
            }
        }

        if (materialModelList.size == 1) {
            binding.tvmaterialValue.text = materialModelList[0].materialName
            selectedMaterialList = materialModelList
            setUpMaterialAdapter()
        }
        selectedPurchaseOrder = materials?.get(0)

    }

    private fun updateUIWithDbData() {
        binding.tvTruckNoValue.setText(vm.dispatchWh.vehicleNumber)
        binding.tvDriverNameValue.setText(vm.dispatchWh.driverName)
        binding.tvDriverNoValue.setText(vm.dispatchWh.driverPhoneNumber)
    }

    private fun saveMaterialDetails(list: ArrayList<VegaCoffeePurchaseOrderMaterialModel>) {
        vm.saveMaterialDetails(list)
    }

    private fun setUpMaterialAdapter() {

        binding.rvMaterialList.setUp(selectedMaterialList, R.layout.item_nigeria_sesame_material_layout, { item, pos ->
            tvMaterialName.text = item.materialName
            tvStoWeightValue.text = item.soWeight?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                .plus("KG")
            tvDispatchWeight.visibility = View.GONE
            tvDispatchWeightValue.visibility = View.GONE
            ll_total_weight_loss.visibility = View.GONE
        })
    }

    private fun updatePurchaseOrder(response: Resource<GenericReqAndResp<List<VegaNigeriaSesameMtntPurchaseOrder>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        purchaseOrderList = it1 as MutableList<VegaNigeriaSesameMtntPurchaseOrder>
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

    private fun getPurchaseOrderByStorageLocation(warehouseId: String) {
        STONumbers.clear()
        val orderList = purchaseOrder.filter { it.warehouseId == warehouseId.trim() }
        filteredPurchaseOrderList = orderList as ArrayList<VegaNigeriaSesameMtntPurchaseOrders>
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
        val location = purchaseOrder.map {it.warehouseId.plus("-").plus(getMTNTPlantName(it.warehouseId ?: "")) }.toSet()
        storageLocation.addAll(location)
    }


    private fun getCurrentDate(): String {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        return simpleDateFormat.format(Date())
    }
}
