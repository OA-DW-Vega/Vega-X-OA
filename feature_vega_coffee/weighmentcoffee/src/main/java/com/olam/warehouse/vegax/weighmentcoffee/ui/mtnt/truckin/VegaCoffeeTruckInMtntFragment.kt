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
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
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
class VegaCoffeeTruckInMtntFragment : BaseFragment(), VegaSingleSelectCommonListener {

    private var mtntData = VegaMtnt()
    private var supplierList = mutableListOf<VegaVendor>()
    private lateinit var productNameList:List<VegaMaterial>
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var purchaseOrderList = mutableListOf<VegaPurchaseOrder>()
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null


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
        TrackHelper.track().screen("receiving/ui/mtnt/VegaTruckInMtntFragment").title("IVC/Coffee/Weighment/Truck In MTNT").with(tracker)
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
        binding.tvDisWarehouse.setOnClickListener {
            showSingleSelectDialog(getString(R.string.dispatch_location), "")
        }
        binding.tvDestWarehouse.setOnClickListener {
            showSingleSelectDialog(getString(R.string.destination_warehouse), WAREHOUSE) }
        binding.tvPurchaseOrder.setOnClickListener {
            showSingleSelectDialog(getString(R.string.receiving_purchase_order), PURCHASE_ORDER) }
        binding.tvTransVendor.setOnClickListener {
            showSingleSelectDialog(getString(R.string.trans_vendor_popup), TRANSPORT_VENDOR)
        }
        binding.tvProduct.setOnClickListener {
            showSingleSelectDialog(getString(R.string.trans_product_popup),PRODUCT)
        }
        vm.custonLocation.observe(viewLifecycleOwner, Observer { custonLocationList = it.toMutableList() })
        vm.getCustomLocations()

        vm.purchaseOrder.observe(viewLifecycleOwner, Observer { updatePurchaseOrder(it) })
        vm.getPurchaseOrder()

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList = it/*.filter { data -> data.bcApprover?.isNotEmpty()!! }*/.toMutableList()
        })
        vm.getSuppliers()

        vm.product.observe(viewLifecycleOwner, Observer {
            productNameList =it
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

    override fun clickOnItem(data: String, currentFlag: String) {
        customDialog?.dismiss()
     when(currentFlag){
         TRANSPORT_VENDOR->{
             binding.tvTransVendor.text=data
             supplierList.forEach { material ->
                 if (material.vendorCode == binding.tvTransVendor.text.toString().split(" - ")[0]) {
                     mtntData.transportVendorCode = material.vendorCode
                     mtntData.transportVendorName = material.vendorName
                     return
                 }
             }
         }
         PRODUCT->{
             binding.tvProduct.text= data
             productNameList.forEach { material ->
                 if (material.materialName == binding.tvProduct.text.toString()) {
                     mtntData.materialCode =
                         MATERIAL_CODE.plus(material.materialCode)
                     mtntData.materialName = material.materialName.toString()
                     binding.tvTruckUom.text = material.unitsOfMeasure.toString()
                     binding.tvDispatchUom.text = material.unitsOfMeasure.toString()
                     mtntData.unitsOfMeasure = material.unitsOfMeasure.toString()
                     return
                 }
             }

         }
         RECEIVING_DATA->{
             binding.tvDestWarehouse.text= data

             val code= if(data.contains("-")) data.split(" - ")[0] else data
             mtntData.recStorageLocationCode = code

         }
         PURCHASE_ORDER->{
             val purData = purchaseOrderList.filter { it.materialCode == mtntData.materialCode }
             var purchaseOrder = mutableListOf<VegaPurchaseOrders>()
             if (!purData.isNullOrEmpty())
                 purchaseOrder = purData[0].purchaseOrders as MutableList<VegaPurchaseOrders>
             val purchaseFilter = purchaseOrder.filter { it.storageLocationCode.equals(mtntData.recStorageLocationCode) }
             purchaseFilter.forEach {
                 binding.tvPurchaseOrder.text = data
                 if(it.purchaseDocNum== data){
                     mtntData.purchaseDocNum = it.purchaseDocNum
                     mtntData.purchaseDocDesc = it.purchaseDocDesc
                     mtntData.recPlantId = it.plantId
                     return
                 }
             }

         }else->{
         binding.tvDisWarehouse.text = data
         mtntData.storageLocationCode = data.split(" - ")[0]
         }
     }

    }


    private fun showSingleSelectDialog(title: String,currentFalg: String) {
        val list: List<String>

        when(currentFalg) {
            TRANSPORT_VENDOR->{
                 list= supplierList.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }

            }
            PRODUCT -> {
                list= productNameList.map { data -> data.materialName!! }
            }
            WAREHOUSE->{
                val purData = purchaseOrderList.filter { it.materialCode == mtntData.materialCode }
                var purchaseOrder = mutableListOf<VegaPurchaseOrders>()
                if (!purData.isNullOrEmpty())
                    purchaseOrder = purData[0].purchaseOrders as MutableList<VegaPurchaseOrders>
                list = purchaseOrder.map { data ->
                    if (data.storageLocationName.isNullOrEmpty()) data.storageLocationCode else data.storageLocationCode.plus(" - ").plus(
                        data.storageLocationName
                    )
                }.distinct()
            }
            PURCHASE_ORDER->{
                val purData = purchaseOrderList.filter { it.materialCode == mtntData.materialCode }
                var purchaseOrder = mutableListOf<VegaPurchaseOrders>()
                if (!purData.isNullOrEmpty())
                    purchaseOrder = purData[0].purchaseOrders as MutableList<VegaPurchaseOrders>
                val purchaseFilter = purchaseOrder.filter { it.storageLocationCode.equals(mtntData.recStorageLocationCode) }
                 list = purchaseFilter.map { data -> data.purchaseDocNum }

            }
            else->{
                list =
                    custonLocationList.filter { it.storageLocationType.equals("P") }
                        .map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }

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



}
