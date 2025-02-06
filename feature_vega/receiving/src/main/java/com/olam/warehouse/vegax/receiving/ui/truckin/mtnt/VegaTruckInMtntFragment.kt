package com.olam.warehouse.vegax.receiving.ui.truckin.mtnt

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.gson.Gson
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaPurchaseOrder
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.receiving.R
import com.olam.warehouse.vegax.receiving.databinding.FragmentVegaMtntTruckinBinding
import com.olam.warehouse.vegax.receiving.databinding.ItemVegaMaterialLayoutBinding
import com.olam.warehouse.vegax.receiving.ui.VegaMtntViewModel
import com.olam.warehouse.vegax.receiving.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 2/14/2020.
 */
class VegaTruckInMtntFragment : BaseFragment(), VegaSingleSelectCommonListener {

    private var mtntData = VegaMtnt()
    private var supplierList = mutableListOf<VegaVendor>()
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var purchaseOrderList = mutableListOf<VegaPurchaseOrder>()
    private var STONumbers: List<String> = emptyList()
    private var purchaseOrder = mutableListOf<VegaPurchaseOrders>()
    private var selectedPurchaseOrder: VegaPurchaseOrders? = null
    private var materials: ArrayList<VegaPurchaseOrders>? = null
    private var materialModelList: MutableList<VegaCoffeePurchaseOrderMaterialModel> = ArrayList()
    private var storageLocation = ArrayList<String>()
    private var productList = emptyList<VegaMaterial>()
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null

    private val vm: VegaMtntViewModel by viewModel()
    private lateinit var binding: FragmentVegaMtntTruckinBinding
    override val layoutResourceId = R.layout.fragment_vega_mtnt_truckin

    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceMtntFragment(
            flag: String,
            moveFrag: String,
            mtntData: VegaMtnt
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(mtntData: VegaMtnt) = VegaTruckInMtntFragment().putArgs {
            putParcelable(MTNT_DATA, mtntData)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaMtntTruckinBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("receiving/ui/mtnt/VegaTruckInMtntFragment").title("Receiving").with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnStartWeigh, it, true)
        }
        mtntData = arguments?.getParcelable<VegaMtnt>(MTNT_DATA)!!

        binding.etTruckNo.setText(mtntData.vehicleNumber)
        binding.etDriverName.setText(mtntData.driverName)
        binding.etPhoneNo.setText(mtntData.contactNumber)
        binding.tvProduct.text = mtntData.materialName.plus("-")
            .plus(mtntData.materialCode)
        if (mtntData.transportVendorCode?.isNotEmpty()!!)
            binding.tvTransVendor.text = mtntData.transportVendorCode.plus("-").plus(mtntData.transportVendorName)
        binding.tvDestWarehouse.text = mtntData.recStorageLocationCode
        binding.tvDisWarehouse.text = mtntData.storageLocationCode
        binding.etDispatchWeight.setText(mtntData.approximateWeight)
        binding.etTruckInWeight.setText(mtntData.tareWeight)
        binding.etDispatchWeight.setText(
            if (mtntData.approximateWeight.equals("0.000") || mtntData.approximateWeight.equals(
                    "0"
                )
            ) "" else mtntData.approximateWeight
        )
        binding.etTruckInWeight.setText(if (mtntData.tareWeight.equals("0.000") || mtntData.tareWeight.equals("0")) "" else mtntData.tareWeight)
        updateMandatory()
        binding.btnStartWeigh.setOnClickListener { validateInputs() }
        binding.tvDisWarehouse.setOnClickListener {
            showSingleSelectDialog(getString(R.string.dispatch_location), WAREHOUSE)
            }
        binding.tvDestWarehouse.setOnClickListener {
            showSingleSelectDialog(getString(R.string.receiving_location_popup), "")
        }
        binding.tvProduct.setOnClickListener {
            showSingleSelectDialog(getString(R.string.trans_product_popup),PRODUCT)
        }
        binding.tvTransVendor.setOnClickListener {
            showSingleSelectDialog(getString(R.string.transport_vendor), SUPPLIER)
        }
//        binding.tvDestinationPlantValue.setOnClickListener { showReceivingPlantDialog() }
        binding.tvStoValue.setOnClickListener {
            showSingleSelectDialog(
                getString(com.olam.warehouse.login.R.string.select_sto),
                STO
            )
        }
        vm.custonLocation.observe(viewLifecycleOwner, Observer { custonLocationList = it.toMutableList() })
        vm.getCustomLocations()

        vm.purchaseOrder.observe(viewLifecycleOwner, Observer { updatePurchaseOrder(it) })
        vm.getPurchaseOrder("")
        // filterWHFromPurchaseOrder()

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList = it/*.filter { data -> data.bcApprover?.isNotEmpty()!! }*/.toMutableList()
        })
        vm.getSuppliers()

        vm.product.observe(viewLifecycleOwner, Observer {
            productList = it
            val products = it.map { data -> data.materialName }
            it.forEach { it1 ->
                if (it1.materialCode.equals("100000000939")) {
                    mtntData.materialCode =
                        MATERIAL_CODE.plus(it1.materialCode.toString())
                    mtntData.materialName = it1.materialName.toString()
                    val prodname = it1.materialName.plus("-").plus(it1.materialCode)
                    binding.tvProduct.text = prodname
                    binding.tvTruckUom.text = it1.unitsOfMeasure.toString()
                    binding.tvDispatchUom.text = it1.unitsOfMeasure.toString()
                    mtntData.unitsOfMeasure = it1.unitsOfMeasure.toString()
                }
            }
        })
        vm.getProducts()
    }

    private fun updatePurchaseOrder(response: Resource<GenericReqAndResp<List<VegaPurchaseOrder>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data.let { it1 ->
                        purchaseOrderList = it1 as MutableList<VegaPurchaseOrder>
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }

    }



/*
    private fun showReceivingPlantDialog() {
        val location = storageLocation
        MaterialDialog(requireContext()).show {
            title(R.string.receiving_location_popup)
            listItemsSingleChoice(items = location) { _, index, text ->
                //binding.tvDestinationPlantValue.text = text
                val palntid = text.split("-")
                if (palntid.isNotEmpty()) vm.getPurchaseOrder(palntid[0])
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }
*/

    private fun filterWHFromPurchaseOrder() {
        for (item in allPlants()) {
            storageLocation.add(item.plantId.plus("-").plus(getPlantName(item.plantId)))
        }
    }

    private fun getPlantName(plantId: String): String {
        val plants = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.MTNT_PLANT_LIST, ""))
        val plant = plants.singleOrNull { it.plantId == plantId }
        return plant?.plantName ?: ""
    }

    private fun allPlants(): List<Plant> {
        return Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.MTNT_PLANT_LIST, ""))
    }




    private fun getPurchaseOrderByStorageLocation(storageLocationCode: String) {
        val orderList = purchaseOrder.filter { it.storageLocationCode == storageLocationCode.trim() }
        STONumbers= orderList.map { it.purchaseDocNum }.toSet().toList()

    }

    private fun getWeight(purchaseId: String) {
        materialModelList.clear()
        materials =
            purchaseOrder.filter { s -> s.purchaseDocNum == purchaseId } as ArrayList<VegaPurchaseOrders>
        materials?.forEach {
            val materialModel = VegaCoffeePurchaseOrderMaterialModel()
            materialModel.materialCode = it.materialCode
            materialModel.soNumber = it.purchaseDocNum
            materialModel.soWeight = it.openQuantity
            materialModel.uom = it.meins
            materialModel.purchaseOrderNum = it.purchaseDocNum
            materialModel.purchaseOrderDesc = it.purchaseDocDesc ?: ""
            materialModelList.add(materialModel)
        }
        selectedPurchaseOrder = materials?.get(0)
    }

    private fun setUpMaterialAdapter() {
        binding.rvMaterialList.setUpAdapter(
            materialModelList,
            R.layout.item_vega_material_layout,
            ItemVegaMaterialLayoutBinding::inflate,
            { item, pos, bindingItem ->

                val materialName =
                    productList.single { item.materialCode.contains(it.materialCode) }
                item.materialName = materialName.materialName
                bindingItem.tvMaterialName.text = item.materialName
                bindingItem.tvStoWeightValue.text =
                    item.soWeight?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                        .plus(item.uom)
                bindingItem.tvDispatchWeight.visibility = View.GONE
                bindingItem.tvDispatchWeightValue.visibility = View.GONE
            })
    }

    private fun filterSTONumber() {
        for (item in purchaseOrderList) {
            purchaseOrder.addAll(item.purchaseOrders)
        }
        val location = purchaseOrder.map { it.storageLocationCode.plus(" - ").plus(it.storageLocationName) }.toSet()
        storageLocation.addAll(location)
    }

    private fun validateInputs() {
        when {
            binding.etTruckNo.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
            binding.etDriverName.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver_name))
            binding.etDispatchWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_dispatch_weight))
            binding.etTruckInWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_weight))
            mtntData.transportVendorCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_transportation_vendor))
            mtntData.materialCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_product))
            // mtntData.recStorageLocationCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_location))
            mtntData.storageLocationCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_dispatch_location))
            else -> moveToSummary()
        }
    }

    private fun moveToSummary() {
        mtntData.approximateWeight = binding.etDispatchWeight.text.toString()
        mtntData.tareWeight = binding.etTruckInWeight.text.toString()
        mtntData.vehicleNumber = binding.etTruckNo.text.toString()
        mtntData.driverName = binding.etDriverName.text.toString()
        mtntData.contactNumber = binding.etPhoneNo.text.toString()
        mtntData.wsGate = WB01
        prepareSuccessData(if (mtntData.tmpWbId.isEmpty()) getTmpId() else mtntData.tmpWbId, false)
        callBack?.replaceMtntFragment("", TRUCKIN_SUMMARYT_FRAG, mtntData)
    }

    private fun prepareSuccessData(wbId: String?, syncStatus: Boolean) {
        mtntData.weighBridgeId = if (syncStatus) wbId ?: "" else ""
        mtntData.tmpWbId = wbId ?: ""
        mtntData.erdat =
            if (mtntData.erdat.isNullOrEmpty()) "/Date(".plus(DateUtils.getCurrentTimeInMills().toString()).plus(")/") else mtntData.erdat
        mtntData.truckDirection = DIRECTIONIN
        mtntData.syncStatusMsg = "Data cached offline"
        mtntData.status = Status.SYNC_PENDING
        mtntData.isSynced = syncStatus
        vm.saveMtnt(mtntData)
    }

    private fun updateMandatory() {
        binding.tvTruckNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_number)) { mandatoryStars() } }
        binding.tvDriverNameLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.driver_name)) { mandatoryStars() } }
        binding.tvTransVendorLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.transport_vendor)) { mandatoryStars() } }
        binding.tvProductLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.product_type)) { mandatoryStars() } }
        binding.tvDestinationWhLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.destination_warehouse)) { mandatoryStars() } }
        binding.tvDispatchWhLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.warehouse)) { mandatoryStars() } }
        binding.tvDispatchWeightLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.dispatch_weight)) { mandatoryStars() } }
        binding.tvTruckInLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_in_weight)) { mandatoryStars() } }

    }

    private fun showSingleSelectDialog(title: String, currentFlag: String) {
        val list:List<String>
        when (currentFlag) {
            STO -> {
                list= STONumbers
            }
            WAREHOUSE->{
                list =
                    custonLocationList.filter { !it.storageLocationType.equals("B") }
                        .map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }

            }
            PRODUCT->{
                list = productList.map { data -> data.materialName!! }
            }
            SUPPLIER->{
                val supplierData = supplierList.filter { data -> data.vendorCode.startsWith("2", true) }
                list = supplierData.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            }
            else->{
            val purData = purchaseOrderList.filter { it.materialCode == mtntData.materialCode }
            if (!purData.isNullOrEmpty())
                purchaseOrder = purData[0].purchaseOrders.filter { it.storageLocationCode.isNotEmpty() }
                        /*.distinctBy { it.storageLocationCode to it.purchaseDocNum} */ as MutableList<VegaPurchaseOrders>
            list = purchaseOrder.map { data ->
                if (data.storageLocationName.isNullOrEmpty()) data.storageLocationCode else data.storageLocationCode.plus(" - ")
                    .plus(
                        data.storageLocationName
                    )
            }.distinct()

        }

        }
        customDialog =
            VegaCommonSingleSelectDialogWithSearch(
                title,
                currentFlag,
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
            STO -> {
                getWeight(data.toString())
                binding.tvStoValue.text = data
                val cureentPur = purchaseOrder.filter { it.purchaseDocNum.equals(data) }
                if (cureentPur.isNotEmpty()) {
                    mtntData.recStorageLocationCode = cureentPur[0].storageLocationCode
                    mtntData.purchaseDocNum = cureentPur[0].purchaseDocNum
                    mtntData.purchaseDocDesc = cureentPur[0].purchaseDocDesc
                    mtntData.recPlantId = cureentPur[0].plantId
                }
                setUpMaterialAdapter()
            }
            PRODUCT->{
                productList.forEach { material ->
                    if (material.materialCode.equals("100000000939")) {
                        val prodname = material.materialName.plus("-").plus(material.materialCode)
                        binding.tvProduct.text = prodname
                        mtntData.materialCode =
                            MATERIAL_CODE.plus(material.materialCode)
                        mtntData.materialName = material.materialName.toString()
                        binding.tvTruckUom.text = material.unitsOfMeasure.toString()
                        binding.tvDispatchUom.text = material.unitsOfMeasure.toString()
                        mtntData.unitsOfMeasure = material.unitsOfMeasure.toString()
                    }
                }

            }
            WAREHOUSE->{
                binding.tvDisWarehouse.text = data
                mtntData.storageLocationCode = data.split(" - ")[0]

            }
            SUPPLIER->{
                val supplierData = supplierList.filter { data -> data.vendorCode.startsWith("2", true) }
                     binding.tvTransVendor.text= data
                supplierData.forEach { material ->
                    if (material.vendorCode == binding.tvTransVendor.text.toString().split(" - ")[0]) {
                        mtntData.transportVendorCode = material.vendorCode
                        mtntData.transportVendorName = material.vendorName
                    }
                }

            }
            else->{
                binding.tvDestWarehouse.text=data
                if (data.contains("-")) {
                    val storage = data.split(" - ")
                    getPurchaseOrderByStorageLocation(storage[0])
                } else
                    getPurchaseOrderByStorageLocation(data)

            }
        }
    }
}
