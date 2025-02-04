package com.olam.warehouse.vegax.weighmentcoffee.ui.truckin.sales

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
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaReceivingMtnLots
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.weighmentcoffee.R
import com.olam.warehouse.vegax.weighmentcoffee.databinding.FragmentVegaCoffeeSalesTruckinBinding
import com.olam.warehouse.vegax.weighmentcoffee.ui.VegaCoffeeMtnrSupplierViewModel
import com.olam.warehouse.vegax.weighmentcoffee.ui.VegaCoffeeReplaceCallback
import com.olam.warehouse.vegax.weighmentcoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaCoffeeSalesFragment : BaseFragment(), VegaSingleSelectListener {

    private var isOBDSelect: Boolean = false
    private var receivingData = VegaReceiving()
    private var warehouseWithMtn = VegaReceivingWarehouseWithMtns()
    private var productList = mutableListOf<VegaReceivingMtnLots>()
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private val vm: VegaCoffeeMtnrSupplierViewModel by viewModel()
    private lateinit var binding: FragmentVegaCoffeeSalesTruckinBinding
    override val layoutResourceId = R.layout.fragment_vega_coffee_sales_truckin
    private var materialProduct = ArrayList<VegaMaterial>()

    private var callBack: VegaCoffeeReplaceCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeReplaceCallback
    }

    companion object {
        fun newInstance(receivingData: VegaReceiving) = VegaCoffeeSalesFragment().putArgs {
            putParcelable(RECEIVING_DATA, receivingData)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCoffeeSalesTruckinBinding.inflate(layoutInflater)
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
        vm.weighBridgeId.observe(viewLifecycleOwner, Observer { updateValueUI(it) })
        if (receivingData.weighBridgeId.isNotEmpty()) {
            vm.getWeighBridgeIdDetail(receivingData.weighBridgeId)
        }
        updateMandatory()

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

        vm.commonReceiving.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                receivingData = it
                updateLocalData()
            }
        })


        binding.btnNxt.setOnClickListener { validateInputs() }
        binding.tvDispatchWarehouse.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.trans_Warehouse_popup),
                true,
                false,
                false
            )
        } //sending

        binding.tvProduct.setOnClickListener {
            showSingleSelectDialog(true, getString(R.string.trans_product_popup), false, false, false)
        }

        vm.getSuppliers()

        vm.getProducts()

        vm.product.observe(viewLifecycleOwner, Observer {
            materialProduct.addAll(it)
        })

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            customLocationList = it.filter { it.storageLocationType.equals("P") }.toMutableList()
            customLocationList.forEach {
                if (it.procureLocationCode.equals(receivingData.storageLocationCode)) {
                    /*binding.tvReceivingLocation.text =
                        receivingData.storageLocationCode.plus("-").plus(it.procureLocationName)*/
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
        binding.tvProduct.isEnabled = binding.tvProduct.text.isEmpty()
    }

    private fun updateLocalData() {
        binding.etTruckGrossWeight.setText(receivingData.grossWeight)
        binding.tvTruckNo.setText(receivingData.vehicleNumber)
        binding.tvDriverName.setText(receivingData.driverName)
        binding.tvPhoneNo.setText(receivingData.contactNumber)
        binding.tvProduct.text = receivingData.materialName
    }

    private fun updateValueUI(response: Resource<GenericReqAndResp<VegaReceiving>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                binding.tvDriverName.setText(response.data?.data?.driverName, TextView.BufferType.EDITABLE)
                binding.tvPhoneNo.setText(response.data?.data?.contactNumber, TextView.BufferType.EDITABLE)
                binding.tvDispatchWarehouse.text =
                    response.data?.data?.storageLocationCode.plus("-").plus(response.data?.data?.storageLocationName)
                receivingData.supplierName =
                    response.data?.data?.dstorageLocationCode.plus("-").plus(response.data?.data?.dstorageLocationName)
                binding.tvMaterialName.text = response.data?.data?.materialName
                binding.tvProduct.text = response.data?.data?.materialName
                binding.tvWeight.text = response.data?.data?.batchWeight.toString()
                binding.tvTruckNo.setText(response.data?.data?.vehicleNumber)

                receivingData.transportVendorCode = response.data?.data?.transportVendorCode
                receivingData.transportVendorName = response.data?.data?.transportVendorName
                receivingData.dstorageLocationCode = response.data?.data?.dstorageLocationCode
                receivingData.dstorageLocationName = response.data?.data?.dstorageLocationName
                receivingData.materialCode = response.data?.data?.materialCode
                receivingData.materialName = response.data?.data?.materialName

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
            binding.tvProduct.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_product))
            binding.tvTruckNo.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
            //binding.tvDriverName.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver_name))
            binding.etTruckGrossWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_gross_weight))
            else -> moveToSummary()
        }
    }

    private fun moveToSummary() {
        receivingData.tareWeight = binding.etTruckGrossWeight.text.toString()
        receivingData.vehicleNumber = binding.tvTruckNo.text.toString()
        receivingData.driverName = binding.tvDriverName.text.toString()
        receivingData.contactNumber = binding.tvPhoneNo.text.toString()
        receivingData.wsGate = WB01
        receivingData.unitsOfMeasure = "KG"
        receivingData.approximateWeight = binding.etTruckGrossWeight.text.toString()
        receivingData.commonPrimaryId =
            receivingData.supplierCode.plus(receivingData.weighBridgeType).plus(receivingData.delivery)
        prepareSuccessData(
            if (receivingData.weighBridgeId.isEmpty()) getTmpId() else receivingData.weighBridgeId,
            false
        )
        callBack?.replaceMtntFragment(TRUCKIN_SALES_SUMMARYT_FRAG, receivingData.direction ?: "", receivingData)
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
        // vm.saveReceiving(receivingData)
    }


    private fun updateMandatory() {
        binding.tvDispatchWhLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.warehouse)) { mandatoryStars() } }
        binding.tvProductLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.product_type)) { mandatoryStars() } }
        binding.tvTruckNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_number)) { mandatoryStars() } }
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
            val mtnNumbers =
                materialProduct.map { data -> data.materialCode.plus("-").plus(data.materialName) }.distinct()
            list.addAll(mtnNumbers)
        } else if (isWh) {
            val warehouses =
                customLocationList.map { data -> data.procureLocationCode.plus("-").plus(data.procureLocationName) }
            list.addAll(warehouses)
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
            val material = materialProduct.filter { it.materialCode == split[0] }
            binding.tvProduct.text = split[1]
            receivingData.materialCode = material[0].materialCode
            receivingData.materialName = material[0].materialName
            /*binding.tvUom.text = material[0].uom
            receivingData.unitsOfMeasure = material[0].uom*/

        } else if (isWh) {
            binding.tvDispatchWarehouse.text = data
            val split = data.split("-")
            val item = customLocationList.single { it.procureLocationCode == split[0] }
            //vm.getWarehousesWithMtns(item.storageLocationCode)
            receivingData.plantId = getPlantDetails().plantId
            receivingData.plantName = getPlantDetails().plantName
            /*receivingData.supplierCode = item.plant
            receivingData.supplierName = item.storageLocationName*/
            receivingData.storageLocationCode = item.procureLocationCode
            receivingData.storageLocationName = item.procureLocationName
        } else if (isObd) {
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
                        binding.tvWeight.text = batchList[pos].weight.toString()
                    }
                }
            if (mtn.mtn.mtntWbid.isNotEmpty()) {
                isOBDSelect = true
                vm.getWeighBridgeIdDetail(mtn.mtn.mtntWbid)
            }

        } else {
            val split = data.split("-")
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
