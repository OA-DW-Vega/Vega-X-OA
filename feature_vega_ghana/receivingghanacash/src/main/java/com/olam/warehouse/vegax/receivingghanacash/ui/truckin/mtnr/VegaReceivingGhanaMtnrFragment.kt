package com.olam.warehouse.vegax.receivingghanacash.ui.truckin.mtnr

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
import com.olam.warehouse.presentation.ui.widget.VegaCoffeeSingleSelectListener
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.receivingghanacash.R
import com.olam.warehouse.vegax.receivingghanacash.databinding.FragmentVegaReceivingGhanaMtnrTruckinBinding
import com.olam.warehouse.vegax.receivingghanacash.ui.VegaReceivingGhanaViewModel
import com.olam.warehouse.vegax.receivingghanacash.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 1/28/2020.
 */
class VegaReceivingGhanaMtnrFragment : BaseFragment(), VegaCoffeeSingleSelectListener, VegaSingleSelectListener {

    private var receivingData = VegaReceiving()
    private var warehouseWithMtn = VegaReceivingWarehouseWithMtns()
    private var wareHouseList: MutableList<VegaSupplyStorageLocation> = mutableListOf()
    private var productList = mutableListOf<VegaReceivingMtnLots>()
    private var supplierList = mutableListOf<VegaVendor>()
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null


    private val vm: VegaReceivingGhanaViewModel by viewModel()
    private lateinit var binding: FragmentVegaReceivingGhanaMtnrTruckinBinding
    override val layoutResourceId = R.layout.fragment_vega_receiving_ghana_mtnr_truckin

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
        fun newInstance(receivingData: VegaReceiving) = VegaReceivingGhanaMtnrFragment().putArgs {
            putParcelable(RECEIVING_DATA, receivingData)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaReceivingGhanaMtnrTruckinBinding.inflate(layoutInflater)
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
        binding.tvDispatchWarehouse.setOnClickListener {
            showSingleSelectDialog(getString(R.string.trans_Warehouse_popup),true,false,false,false,false)
        } //sending
        binding.tvSto.setOnClickListener {
            showSingleSelectDialog(getString(R.string.trans_sto_popup),false,true,false,false,false)
        }
        binding.tvProduct.setOnClickListener {
        showSingleSelectDialog(getString(R.string.trans_product_popup),false,false,false,false,true)
        }
        binding.tvTransVendor.setOnClickListener {
            showSingleSelectDialog(getString(R.string.trans_vendor_popup),false,false,true,false,false)
        }
        binding.tvReceivingLocation.setOnClickListener {
            showSingleSelectDialog(getString(R.string.receiving_location_popup),false,
                false,false,true,false)
        }

        vm.location.observe(viewLifecycleOwner, Observer { wareHouseList = it.toMutableList() })
        vm.warehouseWithMtns.observe(viewLifecycleOwner, Observer { warehouseWithMtn = it })

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList = it/*.filter { data -> data.bcApprover?.isNotEmpty()!! }*/.toMutableList()
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
                val recTypeAdapter = ArrayAdapter(
                    binding.spBatchNumber.context,
                    android.R.layout.simple_list_item_1,
                    batchList
                )
                binding.spBatchNumber.adapter = recTypeAdapter
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
            else -> moveToSummary()
        }
    }

    private fun moveToSummary() {
        receivingData.grossWeight = binding.etTruckGrossWeight.text.toString()
        receivingData.vehicleNumber = binding.tvTruckNo.text.toString()
        receivingData.driverName = binding.tvDriverName.text.toString()
        receivingData.contactNumber = binding.tvPhoneNo.text.toString()
        receivingData.wsGate = WB01
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

    private fun showSingleSelectDialog( title: String,isWh:Boolean,isDepartment:Boolean,
                                       isVendor: Boolean,isOrigin:Boolean,isGrade: Boolean) {
        val list: List<String>

        if(isDepartment){
            list= warehouseWithMtn.mtns.map { data -> data.mtn.mtnNumber }
        }else if(isWh){
            list = wareHouseList.map { data -> data.storageLocationCode.plus("-").plus(data.storageLocationName) }
        }else if(isOrigin){
           val customStorageLocationList= customLocationList.filter { !it.storageLocationType.equals("B") }
            list = customStorageLocationList.map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }
        }else if(isVendor){
            val supplierData = supplierList.filter { data -> data.vendorCode.startsWith("2", true) }
            list = supplierData.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
        }else{
            list = productList.map { data -> data.materialName }.distinct()
        }


        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
               isWh= isWh, isVendor=isVendor, isGrade=isGrade,
                list,
                requireActivity(),
                this, isOBD = false, isSupplier = false, this, null, isOrigin = isOrigin,
                isDepartment = isDepartment
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }


    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {
       println("data:"+data)
    }

    override fun clickOnItem(
        data: String,
        isWh: Boolean,
        isVendor: Boolean,
        isGrade: Boolean,
        isSupplier: Boolean,
        isOrigin: Boolean,
        isDepartment: Boolean // isOBD
    ) {
        customDialog?.dismiss()
       if(isWh){
           binding.tvDispatchWarehouse.text = data
           binding.tvSto.text = ""
           wareHouseList.forEach {
               if(it.storageLocationCode == data.split("-")[0].trim()){
                   val item = it.storageLocationCode
                   vm.getWarehousesWithMtns(item)
                   receivingData.plantId = UIUtils.getWarehouseId().toString()
                   receivingData.supplierCode = it.plant
                   receivingData.supplierName = it.storageLocationName
               }
           }
       }else if(isDepartment){
           binding.tvSto.text = data
           binding.tvProduct.text = ""
           warehouseWithMtn.mtns.forEach {
               if(it.mtn.mtnNumber == data){
                   val mtn = it.mtn
                   productList = it.lots.toMutableList()
                   receivingData.mtnCode = mtn.mtnNumber
                   receivingData.delivery = mtn.mtnNumber
                   binding.cvLotDetails.visible()
                   binding.tvLotDetails.visible()
                   val batchList = productList.filter { it.mtnNumber == mtn.mtnNumber }
                   val recTypeAdapter = ArrayAdapter(
                       binding.spBatchNumber.context,
                       android.R.layout.simple_list_item_1,
                       batchList.map { it.batch })
                   binding.spBatchNumber.adapter = recTypeAdapter
                   binding.spBatchNumber.onItemSelectedListener =
                       object : AdapterView.OnItemSelectedListener {
                           override fun onNothingSelected(p0: AdapterView<*>?) {/*nothing selected*/}
                           override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {

                               receivingData.batchNumber = batchList[pos].batch
                               receivingData.purchaseDocNum = batchList[pos].purchaseOrder
                               receivingData.purchaseDocDesc = batchList[pos].ebelp
                               receivingData.deliveryItem = batchList[pos].posnr
                               binding.tvMaterialName.text = batchList[pos].materialName
                               binding.tvWeight.text = batchList[pos].weight.toString()
                           }
                       }
               }
           }

       }else if(isVendor){
           binding.tvTransVendor.text= data
           supplierList.forEach { material ->
               if (material.vendorCode == binding.tvTransVendor.text.toString().split(" - ")[0].trim()) {
                   receivingData.transportVendorCode = material.vendorCode
                   receivingData.transportVendorName = material.vendorName
               }

           }
       }else if(isOrigin){
           binding.tvReceivingLocation.text = data
           val customStorageLocationList= customLocationList.filter { !it.storageLocationType.equals("B") }
            customStorageLocationList.forEach {
                if(it.procureLocationCode== data.split("-")[0].trim()){
                    receivingData.storageLocationCode = it.procureLocationCode
                    receivingData.storageLocationName = it.procureLocationName
                }
            }
       }else{
           binding.tvProduct.text = data
           productList.forEach {
               if(it.materialName == data){
                   receivingData.materialCode = it.materialNumber
                   receivingData.materialName = it.materialName
                   binding.tvUom.text = it.uom
                   receivingData.unitsOfMeasure = it.uom
               }
           }

       }
    }
}
