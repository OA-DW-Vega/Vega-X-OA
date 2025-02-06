package com.olam.warehouse.vegax.weighment.ui.truckin.mtnr

import android.content.Context
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
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.weighment.R
import com.olam.warehouse.vegax.weighment.databinding.FragmentVegaIndiaCoffeeMtnrTruckinBinding
import com.olam.warehouse.vegax.weighment.ui.VegaIndiaCoffeeReceivingViewModel
import com.olam.warehouse.vegax.weighment.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaIndiaCoffeeMtnrFragment : BaseFragment() , VegaSingleSelectCommonListener {

    private var receivingData = VegaReceiving()
    private var warehouseWithMtn = VegaReceivingWarehouseWithMtns()
    private var wareHouseList: MutableList<VegaSupplyStorageLocation> = mutableListOf()
    private var productList = mutableListOf<VegaReceivingMtnLots>()
    private var supplierList = mutableListOf<VegaVendor>()
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var transportVendorList = ArrayList<String>()
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null

    private val vm: VegaIndiaCoffeeReceivingViewModel by viewModel()
    private lateinit var binding: FragmentVegaIndiaCoffeeMtnrTruckinBinding
    override val layoutResourceId = R.layout.fragment_vega_india_coffee_mtnr_truckin

    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            receivingData: VegaReceiving
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }


    companion object {
        fun newInstance(receivingData: VegaReceiving) = VegaIndiaCoffeeMtnrFragment().putArgs {
            putParcelable(RECEIVING_DATA, receivingData)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaIndiaCoffeeMtnrTruckinBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("receiving/ui/truckin/mtnr/VegaMtnrFragment").title("Receiving").with(tracker)
        initUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun initUI() {
        receivingData = arguments?.getParcelable(RECEIVING_DATA)!!
        receivingData.supplierCode = receivingData.customerNum
        if (AppUtils.isOnline() && receivingData.weighBridgeId.isEmpty()) {
            fetchMtnDetails()
        }
        if (receivingData.weighBridgeId.isNotEmpty()) {
            vm.weighBridgeId.observe(viewLifecycleOwner, Observer { updateValueUI(it) })
            vm.getWeighBridgeIdDetail(receivingData.weighBridgeId)
        }
        updateMandatory()
        binding.tvTransVendor.setText(receivingData.transportVendorCode, TextView.BufferType.EDITABLE)

        binding.tvReceivingLocation.text = receivingData.storageLocationCode
        binding.tvSto.text = receivingData.delivery
        binding.tvDispatchWarehouse.text = receivingData.supplierName
        binding.tvProduct.text = receivingData.materialName
        binding.tvTruckNo.setText(receivingData.vehicleNumber, TextView.BufferType.EDITABLE)
        binding.tvDriverName.setText(receivingData.driverName, TextView.BufferType.EDITABLE)
        binding.tvPhoneNo.setText(receivingData.contactNumber, TextView.BufferType.EDITABLE)
        binding.etTruckGrossWeight.setText(
            if (receivingData.grossWeight.equals("0.000") || receivingData.grossWeight.equals(
                    "0"
                )
            ) "" else receivingData.grossWeight
        )

        binding.btnNxt.setOnClickListener { validateInputs() }
        binding.tvDispatchWarehouse.setOnClickListener { showDispatchWHDialog(wareHouseList) } //sending
        binding.tvSto.setOnClickListener { showStoDialog(warehouseWithMtn) }
        binding.tvProduct.setOnClickListener { showProductDialog(productList.filter { it.mtnNumber == binding.tvSto.text.toString() }) }
        binding.tvTransVendor.setOnClickListener { showSingleSelectDialog(getString(R.string.select_trans_vendor),VENDOR)}
        binding.tvReceivingLocation.setOnClickListener {
            showReceivingLocationDialog(customLocationList.filter {
                !it.storageLocationType.equals(
                    "B"
                )
            })
        }

        vm.location.observe(viewLifecycleOwner, Observer { wareHouseList = it.toMutableList() })
        vm.warehouseWithMtns.observe(viewLifecycleOwner, Observer { warehouseWithMtn = it })

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList = it/*.filter { data -> data.bcApprover?.isNotEmpty()!! }*/.toMutableList()

            val supplierData = supplierList.filter { data -> data.vendorCode.startsWith("2", true) }
            val suppliers = supplierData.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            transportVendorList = suppliers as ArrayList<String>
         //   val productAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suppliers)
           /* binding.tvTransVendor.threshold = 1
            binding.tvTransVendor.setAdapter(productAdapter)
            binding.tvTransVendor.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                it.forEach { material ->
                    if (material.vendorCode == binding.tvTransVendor.text.toString().split(" - ")[0]) {
                        receivingData.transportVendorCode = material.vendorCode
                        receivingData.transportVendorName = material.vendorName
                    }

                }
            }*/
        })
        vm.getSuppliers()

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            customLocationList = it.toMutableList()
            customLocationList.forEach {
                if (it.procureLocationCode.equals(receivingData.storageLocationCode)) {
                    binding.tvReceivingLocation.text =
                        receivingData.storageLocationCode.plus("-").plus(it.procureLocationName)
                    receivingData.storageLocationName = it.procureLocationName
                }
            }

        })
        vm.getCustomLocations()

        //disable edit if data already exist
        enableFields()
    }

    private fun enableFields() {
        binding.tvDispatchWarehouse.isEnabled = binding.tvDispatchWarehouse.text.isEmpty()
        binding.tvSto.isEnabled = binding.tvSto.text.isEmpty()
        binding.tvProduct.isEnabled = binding.tvProduct.text.isEmpty()
        /*binding.tvTransVendor.isEnabled = binding.tvTransVendor.text.isEmpty()
        binding.tvReceivingLocation.isEnabled = binding.tvReceivingLocation.text.isEmpty()
        binding.tvTruckNo.isEnabled = binding.tvTruckNo.text.isEmpty()
        binding.tvDriverName.isEnabled = binding.tvDriverName.text.isEmpty()
        binding.tvPhoneNo.isEnabled = binding.tvPhoneNo.text.isEmpty()
        binding.etTruckGrossWeight.isEnabled = binding.etTruckGrossWeight.text.isEmpty()*/

    }

    private fun updateValueUI(response: Resource<GenericReqAndResp<VegaReceiving>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                binding.tvDriverName.setText(response.data?.data?.driverName, TextView.BufferType.EDITABLE)
                binding.tvPhoneNo.setText(response.data?.data?.contactNumber, TextView.BufferType.EDITABLE)
                binding.tvDispatchWarehouse.text =
                    response.data?.data?.dstorageLocationCode.plus("-").plus(response.data?.data?.dstorageLocationName)
                receivingData.supplierName =
                    response.data?.data?.dstorageLocationCode.plus("-").plus(response.data?.data?.dstorageLocationName)
                binding.tvMaterialName.text = response.data?.data?.materialName
                binding.tvWeight.text = response.data?.data?.batchWeight.toString()
                val batchList = arrayListOf<String>()
                batchList.add(response.data?.data?.batchNumber.toString())
                binding.cvLotDetails.visible()
                binding.tvLotDetails.visible()
                /*val recTypeAdapter = ArrayAdapter(
                    binding.spBatchNumber.context,
                    android.R.layout.simple_list_item_1,
                    batchList
                )
                binding.spBatchNumber.adapter = recTypeAdapter*/
                enableFields()
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
            else -> {}
        }
    }


    private fun fetchMtnDetails() {
        vm.warehouse.observe(viewLifecycleOwner, Observer { processApiResult(it) })
        vm.fetchWarehouseWithMtns()
    }

    private fun processApiResult(data: Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> saveResult(it.data)
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> showErrorDialogWithFAQLink(requireContext(), it.error.toString())
            }
        }
    }

    private fun saveResult(data: GenericReqAndResp<VegaReceivingMtnWrapper>?) {
        hideLoading()
        data?.data?.let {

            vm.saveWarehouseWithMtns(it)
            //vm.getWarehouses()
            vm.getLocations()
        }
    }

    /*private fun showTransportVendorDialog(it: List<VegaVendor>) {
        val supplierData = it.filter { data -> data.vendorCode.startsWith("2", true) }
        val suppliers = supplierData.map { data -> data.vendorName!! }
        MaterialDialog(requireContext()).show {
            title(R.string.trans_vendor_popup)
            listItemsSingleChoice(items = suppliers) { _, index, text ->
                // binding.tvTransVendor.text = text
                receivingData.transportVendorCode = it[index].vendorCode
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }*/

    private fun showReceivingLocationDialog(it: List<VegaCustomStLocation>) {
        val location = it.map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }
        MaterialDialog(requireContext()).show {
            title(R.string.receiving_location_popup)
            listItemsSingleChoice(items = location) { _, index, text ->
                binding.tvReceivingLocation.text = text
                receivingData.storageLocationCode = it[index].procureLocationCode
                receivingData.storageLocationName = it[index].procureLocationName
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun showDispatchWHDialog(it: List<VegaSupplyStorageLocation>) {
        val warehouses = it.map { data -> data.storageLocationCode.plus("-").plus(data.storageLocationName) }

        MaterialDialog(requireContext()).show {
            title(R.string.trans_Warehouse_popup)
            listItemsSingleChoice(items = warehouses) { _, index, text ->
                binding.tvDispatchWarehouse.text = text
                binding.tvSto.text = ""
                val item = it[index].storageLocationCode
                vm.getWarehousesWithMtns(item)
                receivingData.plantId = UIUtils.getWarehouseId().toString()
                receivingData.supplierCode = it[index].plant
                receivingData.supplierName = it[index].storageLocationName
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun showStoDialog(it: VegaReceivingWarehouseWithMtns) {
        val mtnNumbers = it.mtns.map { data -> data.mtn.mtnNumber }
        MaterialDialog(requireContext()).show {
            title(R.string.trans_sto_popup)
            listItemsSingleChoice(items = mtnNumbers) { _, index, text ->
                binding.tvSto.text = text
                binding.tvProduct.text = ""
                val mtn = it.mtns[index]
                productList = it.mtns[index].lots.toMutableList()
                receivingData.mtnCode = mtn.mtn.mtnNumber
                receivingData.delivery = mtn.mtn.mtnNumber
                binding.cvLotDetails.visible()
                binding.tvLotDetails.visible()
                val batchList = productList.filter { it.mtnNumber == mtn.mtn.mtnNumber }
                if(batchList.size == 1)
                {
                    receivingData.batchNumber = batchList[0].batch
                    receivingData.purchaseDocNum = batchList[0].purchaseOrder
                    receivingData.purchaseDocDesc = batchList[0].ebelp
                    receivingData.deliveryItem = batchList[0].posnr
                    binding.tvMaterialName.text = batchList[0].materialName
                    binding.spBatchNumber.text = batchList[0].batch
                    binding.tvWeight.text = batchList[0].weight.toString()
                }
                /*val recTypeAdapter = ArrayAdapter(
                    binding.spBatchNumber.context,
                    android.R.layout.simple_list_item_1,
                    batchList.map { it.batch })
                binding.spBatchNumber.adapter = recTypeAdapter
                binding.spBatchNumber.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {

                            receivingData.batchNumber = batchList[pos].batch
                            receivingData.purchaseDocNum = batchList[pos].purchaseOrder
                            receivingData.purchaseDocDesc = batchList[pos].ebelp
                            receivingData.deliveryItem = batchList[pos].posnr
                            binding.tvMaterialName.text = batchList[pos].materialName
                            binding.tvWeight.text = batchList[pos].weight.toString()
                        }
                    }*/
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }


    private fun showProductDialog(productList: List<VegaReceivingMtnLots>) {
        val mtnNumbers = productList.map { data -> data.materialName }.distinct()
        MaterialDialog(requireContext()).show {
            title(R.string.trans_product_popup)
            listItemsSingleChoice(items = mtnNumbers) { _, index, text ->
                binding.tvProduct.text = text
                receivingData.materialCode = productList[index].materialNumber
                receivingData.materialName = productList[index].materialName
                binding.tvUom.text = productList[index].uom
                receivingData.unitsOfMeasure = productList[index].uom
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun validateInputs() {
        when {
            binding.tvDispatchWarehouse.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_dispatch_wh))
            binding.tvSto.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_obd_no))
//            binding.tvTransVendor.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_transportation_vendor))
            binding.tvReceivingLocation.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_location))
            binding.tvProduct.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_product))
            binding.tvTruckNo.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
            binding.tvDriverName.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver_name))
            binding.etTruckGrossWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_gross_weight))
            else -> moveToSummary()
        }
    }

    private fun moveToSummary() {
        receivingData.grossWeight = binding.etTruckGrossWeight.text.toString()
        receivingData.vehicleNumber = binding.tvTruckNo.text.toString()
        receivingData.driverName = binding.tvDriverName.text.toString()
        receivingData.contactNumber = binding.tvPhoneNo.text.toString()
        receivingData.wsGate = "0002"
        prepareSuccessData(
            if (receivingData.weighBridgeId.isEmpty()) getTmpId() else receivingData.weighBridgeId,
            false
        )
        callBack?.replaceFragment(TRUCKIN_SUMMARYT_FRAG, receivingData)
    }

    private fun prepareSuccessData(wbId: String?, syncStatus: Boolean) {
//        receivingData.weighBridgeId = if (syncStatus) wbId ?: "" else ""
        receivingData.tmpWbId = wbId ?: ""
        receivingData.erdat =
            if (receivingData.erdat.isNullOrEmpty()) "/Date(".plus(getCurrentTimeInMills().toString())
                .plus(")/") else receivingData.erdat
        receivingData.truckDirection = DIRECTIONIN
        receivingData.syncStatusMsg = "Data cached offline"
        receivingData.status = if (syncStatus) Status.RECEVING_COMPLETED else Status.SYNC_PENDING
        receivingData.isSynced = syncStatus
        vm.saveReceiving(receivingData)
    }


    private fun updateMandatory() {
        binding.tvDispatchWhLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.warehouse)) { mandatoryStars() } }
        binding.tvObdLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.sto)) { mandatoryStars() } }
//        binding.tvTransVendorLabel.text =
//            with(UIUtils) { with(requireContext().resources.getString(R.string.transport_vendor)) { mandatoryStars() } }
        binding.tvReceivingLocationLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.receiving_location)) { mandatoryStars() } }
        binding.tvProductLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.product_type)) { mandatoryStars() } }
        binding.tvTruckNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_number)) { mandatoryStars() } }
        binding.tvDriverNameLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.driver_name)) { mandatoryStars() } }
        binding.tvTruckInLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_in_weight)) { mandatoryStars() } }
    }

    private fun showSingleSelectDialog(title: String,currentFalg: String) {
        var list = ArrayList<String>()
        when(currentFalg) {
            VENDOR -> {
                list = transportVendorList
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

    override fun clickOnItem(data: String, currentFlag: String) {
        customDialog?.dismiss()
        when (currentFlag) {
            VENDOR -> {
                binding.tvTransVendor.text = data
                receivingData.transportVendorCode = data.split("-")[0].trim()
                receivingData.transportVendorName = data.split("-")[1].trim()

            }

        }
    }


}
