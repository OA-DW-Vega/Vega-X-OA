package com.olam.warehouse.vegax.weighmentcoffee.ui.truckin.mtnr

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
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.weighmentcoffee.R
import com.olam.warehouse.vegax.weighmentcoffee.databinding.FragmentVegaCoffeeMtnrTruckinBinding
import com.olam.warehouse.vegax.weighmentcoffee.ui.VegaCoffeeMtnrSupplierViewModel
import com.olam.warehouse.vegax.weighmentcoffee.ui.VegaCoffeeReplaceCallback
import com.olam.warehouse.vegax.weighmentcoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaCoffeeMtnrFragment : BaseFragment(), VegaSingleSelectListener {
    private var isOBDSelect: Boolean = false
    private var receivingData = VegaReceiving()
    private var warehouseWithMtn = VegaReceivingWarehouseWithMtns()
    private var wareHouseList: MutableList<VegaSupplyStorageLocation> = mutableListOf()
    private var productList = mutableListOf<VegaReceivingMtnLots>()
    private var supplierList = mutableListOf<VegaVendor>()
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private val vm: VegaCoffeeMtnrSupplierViewModel by viewModel()
    private lateinit var binding: FragmentVegaCoffeeMtnrTruckinBinding
    override val layoutResourceId = R.layout.fragment_vega_coffee_mtnr_truckin
    private var materialProduct = ArrayList<VegaReceivingMtnLots>()

    private var callBack: VegaCoffeeReplaceCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeReplaceCallback
    }

    companion object {
        fun newInstance(receivingData: VegaReceiving) = VegaCoffeeMtnrFragment().putArgs {
            putParcelable(RECEIVING_DATA, receivingData)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCoffeeMtnrTruckinBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("weighmentcoffee/ui/truckin/mtnr/VegaCoffeeMtnrFragment").title("Weighment")
            .with(tracker)
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
        vm.weighBridgeId.observe(viewLifecycleOwner, Observer { updateValueUI(it) })
        if (receivingData.weighBridgeId.isNotEmpty()) {
            vm.getWeighBridgeIdDetail(receivingData.weighBridgeId)
        }
        updateMandatory()
        binding.tvTransVendor.setText(receivingData.transportVendorCode, TextView.BufferType.EDITABLE)
        /*binding.tvReceivingLocation.text =
            receivingData.storageLocationCode.plus("-").plus(getLocationName(receivingData.storageLocationCode))*/
        binding.tvSto.text = receivingData.delivery
        binding.tvDispatchWarehouse.text = receivingData.supplierName
        binding.tvProduct.text = receivingData.materialName
        binding.tvTruckNo.setText(receivingData.vehicleNumber, TextView.BufferType.EDITABLE)
        binding.tvDriverName.setText(receivingData.truckDriverName, TextView.BufferType.EDITABLE)
        binding.tvPhoneNo.setText(receivingData.contactNumber, TextView.BufferType.EDITABLE)
        binding.etTruckGrossWeight.setText(
            if (receivingData.grossWeight.equals("0.000") || receivingData.grossWeight.equals(
                    "0"
                )
            ) "" else receivingData.grossWeight
        )

        vm.commonReceiving.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                receivingData = it
                updateLocalData()
            }
        })


        binding.btnNxt.setOnClickListener {
            validateInputs() }
        binding.tvDispatchWarehouse.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.trans_Warehouse_popup),
                true,
                false,
                false
            )
        } //sending
        binding.tvSto.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.trans_sto_popup),
                false,
                true,
                false
            )
        }
        binding.tvProduct.setOnClickListener {
            showSingleSelectDialog(true, getString(R.string.trans_product_popup), false, false, false)
        }
        //binding.tvTransVendor.setOnClickListener { showTransportVendorDialog(supplierList) }
        binding.tvReceivingLocation.setOnClickListener {
            showSingleSelectDialog(false, getString(R.string.receiving_location_popup), false, false, true)
        }

        vm.location.observe(viewLifecycleOwner, Observer { wareHouseList = it.toMutableList() })
        vm.warehouseWithMtns.observe(viewLifecycleOwner, Observer { warehouseWithMtn = it })

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList = it/*.filter { data -> data.bcApprover?.isNotEmpty()!! }*/.toMutableList()

            val supplierData = supplierList.filter { data -> data.vendorCode.startsWith("2", true) }
            val suppliers = supplierData.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            val productAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suppliers)
            binding.tvTransVendor.threshold = 1
            binding.tvTransVendor.setAdapter(productAdapter)
            binding.tvTransVendor.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                it.forEach { material ->
                    if (material.vendorCode == binding.tvTransVendor.text.toString().split(" - ")[0]) {
                        receivingData.transportVendorCode = material.vendorCode
                        receivingData.transportVendorName = material.vendorName
                    }

                }
            }
        })
        vm.getSuppliers()

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            customLocationList = it.filter { it.storageLocationType.equals("P") }.toMutableList()
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

    private fun updateLocalData() {
        binding.etTruckGrossWeight.setText(receivingData.grossWeight)
        binding.tvTruckNo.setText(receivingData.vehicleNumber)
        binding.tvDriverName.setText(receivingData.truckDriverName)
        binding.tvPhoneNo.setText(receivingData.contactNumber)
        binding.tvTransVendor.setText(
            receivingData.transportVendorCode.plus("-").plus(receivingData.transportVendorName)
        )
        binding.tvReceivingLocation.text =
            receivingData.storageLocationCode.plus("-").plus(getLocationName(receivingData.storageLocationCode))
        binding.tvProduct.text = receivingData.materialName
    }

    private fun updateValueUI(response: Resource<GenericReqAndResp<VegaReceiving>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                binding.tvDriverName.setText(response.data?.data?.truckDriverName, TextView.BufferType.EDITABLE)
                binding.tvPhoneNo.setText(response.data?.data?.contactNumber, TextView.BufferType.EDITABLE)
                binding.tvDispatchWarehouse.text =
                    response.data?.data?.dstorageLocationCode.plus("-").plus(response.data?.data?.dstorageLocationName)
                receivingData.supplierName =
                    response.data?.data?.dstorageLocationCode.plus("-").plus(response.data?.data?.dstorageLocationName)
                binding.tvMaterialName.text = response.data?.data?.materialName
                binding.tvProduct.text = response.data?.data?.materialName
                /*binding.tvReceivingLocation.text =
                    response.data?.data?.dstorageLocationCode.plus("-").plus(response.data?.data?.dstorageLocationName)*/
                binding.tvWeight.text = response.data?.data?.batchWeight.toString().plus(" ").plus(response.data?.data?.unitsOfMeasure)
                binding.tvTruckNo.setText(response.data?.data?.vehicleNumber)
                /*binding.tvTransVendor.setText(
                    response.data?.data?.transportVendorCode.plus("-").plus(response.data?.data?.transportVendorName)
                )*/
                receivingData.transportVendorCode = response.data?.data?.transportVendorCode
                receivingData.transportVendorName = response.data?.data?.transportVendorName
                receivingData.dstorageLocationCode = response.data?.data?.dstorageLocationCode
                receivingData.dstorageLocationName = response.data?.data?.dstorageLocationName
                receivingData.materialCode = response.data?.data?.materialCode
                receivingData.materialName = response.data?.data?.materialName
                receivingData.batchNumber = response.data?.data?.batchNumber

                if (!isOBDSelect) {
                    val batchList = arrayListOf<String>()
                    batchList.add(response.data?.data?.batchNumber.toString())
                    binding.cvLotDetails.visible()
                    binding.tvLotDetails.visible()
                    val recTypeAdapter = ArrayAdapter(
                        binding.spBatchNumber.context,
                        android.R.layout.simple_list_item_1,
                        batchList
                    )
                    binding.spBatchNumber.adapter = recTypeAdapter
                    enableFields()
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
            }
        }
    }

    private fun getLocationName(storageLocationName: String?): String {
        val location = ""
        if (storageLocationName != null) {
            val item = wareHouseList.singleOrNull { it.storageLocationCode == storageLocationName }
            return item?.storageLocationName ?: ""
        }
        return location
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
                Resource.Status.ERROR -> UIUtils.showErrorDialog(requireContext(), it.error.toString())
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


    private fun validateInputs() {
        when {
            binding.tvDispatchWarehouse.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_dispatch_wh))
            binding.tvSto.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_obd_no))
            binding.tvTransVendor.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_transportation_vendor))
            binding.tvReceivingLocation.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_location))
            binding.tvProduct.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_product))
            binding.tvTruckNo.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
            binding.tvDriverName.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver_name))
            binding.etTruckGrossWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_gross_weight))
            else ->
               moveToSummary()
        }
    }

    private fun moveToSummary() {
        receivingData.vehicleNumber = binding.tvTruckNo.text.toString()
        receivingData.driverName = binding.tvDriverName.text.toString()
        receivingData.truckDriverName = binding.tvDriverName.text.toString()
        receivingData.contactNumber = binding.tvPhoneNo.text.toString()
        receivingData.wsGate = WB01
        receivingData.supplierCode = receivingData.transportVendorCode
        receivingData.supplierName = receivingData.transportVendorName
        receivingData.grossWeight = binding.etTruckGrossWeight.text.toString()
        receivingData.commonPrimaryId =
            receivingData.supplierCode.plus(receivingData.weighBridgeType).plus(receivingData.delivery)
        prepareSuccessData(
            if (receivingData.weighBridgeId.isEmpty()) getTmpId() else receivingData.weighBridgeId,
            false
        )
        vm.saveTruckInData(receivingData)
        callBack?.replaceMtntFragment(TRUCKIN_SUMMARYT_FRAG, receivingData.direction ?: "", receivingData)
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
        vm.saveTruckInData(receivingData)
    }


    private fun updateMandatory() {
        binding.tvDispatchWhLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.warehouse)) { mandatoryStars() } }
        binding.tvObdLabel.text =
            with(UIUtils) { with( requireContext().resources.getString(R.string.sto)) { mandatoryStars() } }
        binding.tvTransVendorLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.transport_vendor)) { mandatoryStars() } }
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

    private fun showSingleSelectDialog(
        isProduct: Boolean,
        title: String,
        isWh: Boolean,
        isObd: Boolean,
        isReceiving: Boolean
    ) {
        val list = ArrayList<String>()
        if (isProduct) {
            materialProduct.clear()
            materialProduct.addAll(productList.filter { it.mtnNumber == binding.tvSto.text.toString() })
            val mtnNumbers =
                materialProduct.map { data -> data.materialNumber.plus("-").plus(data.materialName) }.distinct()
            list.addAll(mtnNumbers)
        } else if (isWh) {
            val warehouses =
                wareHouseList.map { data -> data.storageLocationCode.plus("-").plus(data.storageLocationName) }
            list.addAll(warehouses)
        } else if (isObd) {
            val mtnNumbers = warehouseWithMtn.mtns.map { data -> data.mtn.mtnNumber }
            list.addAll(mtnNumbers)
        } else if (isReceiving) {

            val receivingLoc = customLocationList.filter {
                !it.storageLocationType.equals(
                    "B"
                )
            }

            list.addAll(receivingLoc.map { data ->
                data.procureLocationCode.plus("-").plus(data.procureLocationName)
            })
        }

        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isProduct, isWh, isObd,
                list,
                activity!!,
                this, isReceiving,isOrigin = false,isDepartment = false
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, isProduct: Boolean, isWh: Boolean, isObd: Boolean) {
        customDialog?.dismiss()
        if (isProduct) {
            val split = data.split("-")
            val material = materialProduct.filter { it.materialNumber == split[0] }
            binding.tvProduct.text = split[1]
            receivingData.materialCode = material[0].materialNumber
            receivingData.materialName = material[0].materialName
            binding.tvUom.text = material[0].uom
            receivingData.unitsOfMeasure = material[0].uom

        } else if (isWh) {
            binding.tvDispatchWarehouse.text = data
            binding.tvSto.text = ""
            val split = data.split("-")
            val item = wareHouseList.single { it.storageLocationCode == split[0] }
            vm.getWarehousesWithMtns(item.storageLocationCode)
            receivingData.plantId = getPlantDetails().plantId
            receivingData.plantName = getPlantDetails().plantName
            receivingData.supplierCode = item.plant
            receivingData.supplierName = item.storageLocationName
        } else if (isObd) {
            binding.tvSto.text = data
            binding.tvProduct.text = ""
            val mtn = warehouseWithMtn.mtns.single { it.mtn.mtnNumber == data }
            productList = mtn.lots.toMutableList()
            receivingData.mtnCode = mtn.mtn.mtnNumber
            receivingData.delivery = mtn.mtn.mtnNumber
            binding.cvLotDetails.visible()
            binding.tvLotDetails.visible()
            val batchList = productList.filter { it.mtnNumber == mtn.mtn.mtnNumber }
            val recTypeAdapter = ArrayAdapter(
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
                        binding.tvWeight.text = batchList[pos].weight.toString().plus(" ").plus(batchList[pos].uom)
                    }
                }
            if (mtn.mtn.mtntWbid.isNotEmpty()) {
                isOBDSelect = true
                vm.getWeighBridgeIdDetail(mtn.mtn.mtntWbid)
            }

        } else {
            binding.tvReceivingLocation.text = data
            val split = data.split("-")

            binding.tvReceivingLocation.text = data
            val item = customLocationList.single { it.procureLocationCode == split[0] }
            receivingData.storageLocationCode = split[0]
            receivingData.storageLocationName = split[1]
        }
        val commonId = receivingData.supplierCode.plus(receivingData.weighBridgeType).plus(receivingData.delivery)
        vm.getReceivingByCommonId(commonId)

    }

    fun saveLocalCache() {
        vm.saveTruckInData(receivingData)
    }
}
