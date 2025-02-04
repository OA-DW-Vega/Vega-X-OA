package com.olam.warehouse.vegax.weighmentcoffee.ui.mtnt.truckin

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
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.entity.VegaPurchaseOrders
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.VegaPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.weighmentcoffee.R
import com.olam.warehouse.vegax.weighmentcoffee.databinding.FragmentVegaCoffeeMtntTruckinBinding
import com.olam.warehouse.vegax.weighmentcoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 2/14/2020.
 */
class VegaCoffeeTruckInMtntFragment : BaseFragment() {

    private var mtntData = VegaMtnt()
    private var supplierList = mutableListOf<VegaVendor>()
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var purchaseOrderList = mutableListOf<VegaPurchaseOrder>()

    private val vm: com.olam.warehouse.vegax.weighmentcoffee.ui.VegaCoffeeMtntViewModel by viewModel()
    private lateinit var binding: FragmentVegaCoffeeMtntTruckinBinding
    override val layoutResourceId = R.layout.fragment_vega_coffee_mtnt_truckin

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
        fun newInstance(mtntData: VegaMtnt) = VegaCoffeeTruckInMtntFragment().putArgs {
            putParcelable(MTNT_DATA, mtntData)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCoffeeMtntTruckinBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("receiving/ui/mtnt/VegaTruckInMtntFragment").title("Receiving").with(tracker)
        initUI()
    }

    private fun initUI() {
        mtntData = arguments?.getParcelable<VegaMtnt>(MTNT_DATA)!!

        binding.etTruckNo.setText(mtntData.vehicleNumber)
        binding.etDriverName.setText(mtntData.driverName)
        binding.etPhoneNo.setText(mtntData.contactNumber)
        binding.tvProduct.setText(mtntData.materialName)
        if (mtntData.transportVendorCode?.isNotEmpty()!!)
            binding.tvTransVendor.setText(mtntData.transportVendorCode.plus("-").plus(mtntData.transportVendorName))
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
        binding.tvDisWarehouse.setOnClickListener { showDispatchWarehouse() }
        binding.tvDestWarehouse.setOnClickListener { showReceivingLocationDialog() }
        binding.tvPurchaseOrder.setOnClickListener { showPurchaseOrderDialog() }
        vm.custonLocation.observe(viewLifecycleOwner, Observer { custonLocationList = it.toMutableList() })
        vm.getCustomLocations()

        vm.purchaseOrder.observe(viewLifecycleOwner, Observer { updatePurchaseOrder(it) })
        vm.getPurchaseOrder()

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList = it/*.filter { data -> data.bcApprover?.isNotEmpty()!! }*/.toMutableList()

            val supplierData = supplierList/*.filter { data -> data.vendorCode.startsWith("2", true) }*/
            val suppliers = supplierData.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            val productAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suppliers)
            binding.tvTransVendor.threshold = 1
            binding.tvTransVendor.setAdapter(productAdapter)
            binding.tvTransVendor.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                it.forEach { material ->
                    if (material.vendorCode == binding.tvTransVendor.text.toString().split(" - ")[0]) {
                        mtntData.transportVendorCode = material.vendorCode
                        mtntData.transportVendorName = material.vendorName
                    }
                }
            }
        })
        vm.getSuppliers()

        vm.product.observe(viewLifecycleOwner, Observer {
            val products = it.map { data -> data.materialName }
            it.forEach { it1 ->
                if (it1.materialName.equals("Ivory Coast Raw Cashew Nut")) {
                    mtntData.materialCode =
                        MATERIAL_CODE.plus(it1.materialCode.toString())
                    mtntData.materialName = it1.materialName.toString()
                    binding.tvProduct.setText(it1.materialName, TextView.BufferType.EDITABLE)
                    binding.tvTruckUom.text = it1.unitsOfMeasure.toString()
                    binding.tvDispatchUom.text = it1.unitsOfMeasure.toString()
                    mtntData.unitsOfMeasure = it1.unitsOfMeasure.toString()
                }
            }
            val productAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, products)
            binding.tvProduct.threshold = 1
            binding.tvProduct.setAdapter(productAdapter)
            binding.tvProduct.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                it.forEach { material ->
                    if (material.materialName == binding.tvProduct.text.toString()) {
                        mtntData.materialCode =
                            MATERIAL_CODE.plus(material.materialCode)
                        mtntData.materialName = material.materialName.toString()
                        binding.tvTruckUom.text = material.unitsOfMeasure.toString()
                        binding.tvDispatchUom.text = material.unitsOfMeasure.toString()
                        mtntData.unitsOfMeasure = material.unitsOfMeasure.toString()
                    }
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
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }

    }

    private fun showTransportVendorDialog(it: List<VegaVendor>) {
        val suppliers = it.map { data -> data.vendorName!! }
        MaterialDialog(requireContext()).show {
            title(R.string.trans_vendor_popup)
            listItemsSingleChoice(items = suppliers) { _, index, text ->
                //binding.tvTransVendor.text = text
                mtntData.transportVendorCode = it[index].vendorCode
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.presentation.R.string.ok), true))
        }
    }

    private fun showReceivingLocationDialog() {
        val purData = purchaseOrderList.filter { it.materialCode == mtntData.materialCode }
        var purchaseOrder = mutableListOf<VegaPurchaseOrders>()
        if (!purData.isNullOrEmpty())
            purchaseOrder = purData[0].purchaseOrders as MutableList<VegaPurchaseOrders>
        val location = purchaseOrder.map { data ->
            if (data.storageLocationName.isNullOrEmpty()) data.storageLocationCode else data.storageLocationCode.plus(" - ").plus(
                data.storageLocationName
            )
        }.distinct()
        MaterialDialog(requireContext()).show {
            title(R.string.receiving_location_popup)
            listItemsSingleChoice(items = location) { _, index, text ->
                binding.tvDestWarehouse.text = text
                mtntData.recStorageLocationCode = text.split(" - ")[0]
                /* mtntData.purchaseDocNum = purchaseOrder[index].purchaseDocNum
                 mtntData.purchaseDocDesc = purchaseOrder[index].purchaseDocDesc
                 mtntData.recPlantId = purchaseOrder[index].plantId*/
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.presentation.R.string.ok), true))
        }
    }

    private fun showPurchaseOrderDialog() {
        val purData = purchaseOrderList.filter { it.materialCode == mtntData.materialCode }
        var purchaseOrder = mutableListOf<VegaPurchaseOrders>()
        if (!purData.isNullOrEmpty())
            purchaseOrder = purData[0].purchaseOrders as MutableList<VegaPurchaseOrders>
        val purchaseFilter = purchaseOrder.filter { it.storageLocationCode.equals(mtntData.recStorageLocationCode) }
        val location = purchaseFilter.map { data -> data.purchaseDocNum }
        MaterialDialog(requireContext()).show {
            title(R.string.receiving_purchase_order)
            listItemsSingleChoice(items = location) { _, index, text ->
                binding.tvPurchaseOrder.text = text
                mtntData.purchaseDocNum = purchaseFilter[index].purchaseDocNum
                mtntData.purchaseDocDesc = purchaseFilter[index].purchaseDocDesc
                mtntData.recPlantId = purchaseFilter[index].plantId
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.presentation.R.string.ok), true))
        }
    }

    private fun showDispatchWarehouse() {
        val location =
            custonLocationList.filter { it.storageLocationType.equals("P") }
                .map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }
        MaterialDialog(requireContext()).show {
            title(R.string.dispatch_location)
            listItemsSingleChoice(items = location) { _, index, text ->
                binding.tvDisWarehouse.text = text
                mtntData.storageLocationCode = text.split(" - ")[0]
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.presentation.R.string.ok), true))
        }
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
            mtntData.purchaseDocNum.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_purchase_order))
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
            with(UIUtils) { with(requireContext().resources.getString(com.olam.warehouse.presentation.R.string.product)) { mandatoryStars() } }
        binding.tvDestinationWhLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.destination_warehouse)) { mandatoryStars() } }
        binding.tvPurchaseOrderLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.purchase_order)) { mandatoryStars() } }
        binding.tvDispatchWhLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.dispatch_wh)) { mandatoryStars() } }
        binding.tvDispatchWeightLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.dispatch_weight)) { mandatoryStars() } }
        binding.tvTruckInLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truckin_weight)) { mandatoryStars() } }

    }

}
