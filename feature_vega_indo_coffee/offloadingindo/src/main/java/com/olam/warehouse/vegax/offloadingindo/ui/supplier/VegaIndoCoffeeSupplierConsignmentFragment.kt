package com.olam.warehouse.vegax.offloadingindo.ui.supplier

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCoffeeSingleSelectListener
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingindo.R
import com.olam.warehouse.vegax.offloadingindo.databinding.FragmentIndoCoffeeSupplierConsignmentLayoutBinding
import com.olam.warehouse.vegax.offloadingindo.ui.VegaIndoCoffeeOffloadingViewModel
import com.olam.warehouse.vegax.offloadingindo.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */
class VegaIndoCoffeeSupplierConsignmentFragment : BaseFragment(), VegaSingleSelectListener,
    VegaCoffeeSingleSelectListener {

    override val layoutResourceId = R.layout.fragment_indo_coffee_supplier_consignment_layout
    private lateinit var binding: FragmentIndoCoffeeSupplierConsignmentLayoutBinding
    private var callBack: Callback? = null
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private lateinit var moreWeightBatches: String
    private var addWeightPosition: Int = 0
    private val vm: VegaIndoCoffeeOffloadingViewModel by viewModel()
    private var wareHouseList: MutableList<VegaSupplyStorageLocation> = mutableListOf()
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var selectedReceivingLocation = VegaCustomStLocation()
    private var selectedOBD = VegaReceivingMtn()
    private var batchList = mutableListOf<VegaCoffeeReceiveLots>()
    private var allBatchList = mutableListOf<VegaCoffeeReceiveLots>()
    private var allOBDList = mutableListOf<VegaReceivingMtn>()
    private var filteredOBDList = mutableListOf<VegaReceivingMtn>()
    private var supplierList = mutableListOf<VegaVendor>()
    private var editLotId: String = ""
    private var materialCode: String = ""
    private var offloadData = VegaCoffeeReceiving()
    private var isNewTruckCall = true
    private var materialProduct = ArrayList<VegaMaterial>()
    private var batchInfo = VegaCoffeeReceiveLots()
    private var purchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var OriginList = mutableListOf<VegaQualitative>()
    private var Departmentlist = mutableListOf<VegaQualitative>()
    private var filterpurchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var yearFilter = mutableListOf<String>()
    private var year = ""

    interface Callback {
        fun callBack(type: String, receiveLots: VegaCoffeeReceiveLots, receiving: VegaCoffeeReceiving)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? Callback
    }

    companion object {
        fun newInstance(offload: VegaCoffeeReceiving) = VegaIndoCoffeeSupplierConsignmentFragment().putArgs {
            putParcelable(OFFLOAD_DATA, offload)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        offloadData = arguments?.getParcelable(OFFLOAD_DATA)!!
        binding = FragmentIndoCoffeeSupplierConsignmentLayoutBinding.inflate(layoutInflater)
        isNewTruckCall = offloadData.weighBridgeId.isBlank()
       // vm.poList.observe(viewLifecycleOwner, Observer { updatePurchaseOrderUI(it) })
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingcoffee/ui/mtnr/VegaCoffeeMtnrTypeSelectFragment").title("Mtnr Coffee")
            .with(tracker)
    }

    private fun initUI() {
        if (isNewTruckCall) {
            clearAllValues()
        }
        enableProceed()
        vm.getLocations()
        vm.getCustomLocations()
        vm.getSuppliers()
        if (offloadData.imageString.equals(WEIGHBRIDGE_WEIHSCALE)) {
            binding.tvyear.gone()
            binding.tvYearValue.gone()
            binding.tvPo.gone()
            binding.tvPOValue.gone()
            binding.llDeptLayout.gone()
            binding.llOriginLayout.gone()
            binding.tvTransportCostLabel.gone()
        } else {
            binding.tvyear.gone()
            binding.tvYearValue.gone()
            binding.llDeptLayout.gone()
            binding.llOriginLayout.gone()
            binding.tvTransportCostLabel.gone()
        }
        binding.tvWhValue.setOnClickListener {
            showSingleSelectDialog(
                true,
                getString(R.string.select_material),
                false,
                false, false, false
            )
        }
        binding.tvPOValue.setOnClickListener {
            //if (offloadData.imageString.equals(WEIGHSCALE) && year.isNotEmpty())
                showSingleSelectDialog(
                    false,
                    getString(R.string.select_purchase_order),
                    false,
                    true, false, false
                )
        }
        binding.tvstoValue.setOnClickListener {
            if (binding.tvWhValue.text.toString().isNotEmpty()) showSingleSelectDialog(
                false,
                getString(R.string.select_supplier),
                true, false, false, false
            )
        }
        binding.tvReceivingValue.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.select_receive_loc),
                false, false, false, false
            )
        }
        binding.tvOrigin.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.select_origin),
                false, false, true, false
            )
        }
        binding.tvDestinationLocation.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.select_Destination),
                false, false, false, true
            )
        }

//        vm.getPOList()
        vm.location.observe(viewLifecycleOwner, Observer { wareHouseList = it.toMutableList() })
       /* vm.weighBridgeId.observe(viewLifecycleOwner, Observer {
            updateWeighbridgeValueUI(it)
        })*/
        if (offloadData.materialCode?.length != 18)
            materialCode = "000000".plus(offloadData.materialCode)
        else
            materialCode = offloadData.materialCode!!
        /*vm.getQualityParams(materialCode, false, offloadData.weighBridgeId)
        vm.qualitylist.observe(viewLifecycleOwner, Observer {
            updateoriginlist(it)
        })*/
        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            customLocationList = it/*.filter { it.storageLocationType.equals("P") }*/.toMutableList()
            val receive = it
            if (!isNewTruckCall) {
                val item = receive.singleOrNull { offloadData.dstorageLocationCode == it.procureLocationCode }
                offloadData.dstorageLocationName = item?.procureLocationName ?: ""
                binding.tvReceivingValue.text =
                    offloadData.storageLocationCode.plus("-").plus(offloadData.dstorageLocationName)
            }
            if (customLocationList.size == 1) {
                binding.tvReceivingValue.text =
                    customLocationList[0].procureLocationCode.plus("-").plus(customLocationList[0].procureLocationName)
                offloadData.storageLocationCode
                vm.vegaCoffeeReceivingData.storageLocationCode = customLocationList[0].procureLocationCode
                vm.vegaCoffeeReceivingData.storageLocationName = customLocationList[0].procureLocationName
                offloadData.storageLocationCode = customLocationList[0].procureLocationCode
                offloadData.storageLocationName = customLocationList[0].procureLocationName
            }



            if (!isNewTruckCall) {
                updateDateFromList()
            }
        })

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList = it/*.filter { data -> data.bcApprover?.isNotEmpty()!! }*/.toMutableList()
        })

        binding.tvDriverNoValue.onChange { enableProceed() }
        binding.tvDriverNameValue.onChange { enableProceed() }
        binding.tvTruckNoValue.onChange { enableProceed() }
        binding.etTruckBagcount.onChange { enableProceed() }
        binding.etTruckDeclaredweight.onChange { enableProceed() }
        binding.btProceed.setOnClickListener { moveToSummary() }
        vm.offloadingSupIndoMtnr.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                /*if (!isNewTruckCall && offloadData.weighBridgeId.isNotEmpty()) {
                    vm.getWeighBridgeIdDetail(offloadData.weighBridgeId, offloadData.imageString.equals(WEIGHSCALE))
                } else if (!selectedOBD.mtntWbid.isBlank()) {
                    vm.getWeighBridgeIdDetail(selectedOBD.mtntWbid, offloadData.imageString.equals(WEIGHSCALE))
                }*/
            } else {
                binding.tvTruckNoValue.setText(it.receiving.vehicleNumber)
                val driverName =
                    if (it.receiving.truckDriverName.isNullOrBlank()) it.receiving.driverName else it.receiving.truckDriverName
                binding.tvDriverNameValue.setText(driverName)
                binding.tvPOValue.text = it.receiving.purchaseDocNum
                offloadData.purchaseDocNum = it.receiving.purchaseDocNum
                offloadData.purchaseDocDesc = it.receiving.purchaseDocDesc
                offloadData.unitsOfMeasure = it.receiving.unitsOfMeasure
                offloadData.purchaseDocQty = it.receiving.purchaseDocQty
                binding.tvDriverNoValue.setText(it.receiving.contactNumber)
                binding.etTruckBagcount.setText(it.receiving.bagCount?.trim())
                binding.etTruckDeclaredweight.setText(it.receiving.vendorDeclaredWeight)
                binding.tvReceivingValue.text =
                    it.receiving.storageLocationCode.plus("-").plus(it.receiving.storageLocationName)
                vm.vegaCoffeeReceivingData.truckDriverName = it.receiving.truckDriverName
                vm.vegaCoffeeReceivingData.storageLocationCode = it.receiving.storageLocationCode
                vm.vegaCoffeeReceivingData.storageLocationName = it.receiving.storageLocationName
                vm.vegaCoffeeReceivingData.contactNumber = it.receiving.contactNumber
                vm.vegaCoffeeReceivingData.vehicleNumber = it.receiving.vehicleNumber
                vm.vegaCoffeeReceivingData.bagCount = it.receiving.bagCount
                vm.vegaCoffeeReceivingData.vendorDeclaredWeight = it.receiving.vendorDeclaredWeight
                offloadData = it.receiving
                enableProceed()
            }
        })

        vm.product.observe(viewLifecycleOwner, Observer {
            materialProduct.clear()
            it
            materialProduct.addAll(it)
        })
        vm.getProducts()
        binding.tvYearValue.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.select_year),
                false,
                false, false, false, true
            )
        }
        updateYearValues()

    }

    private fun updateYearValues() {
        val current: Int = Calendar.getInstance().get(Calendar.YEAR)
        binding.tvYearValue.text = current.toString()
        year = current.toString()
        for (i in current.minus(1)..current) {
            yearFilter.add(i.toString())
        }
    }

    private fun getLocationName(storageLocationName: String?): String {
        val location = ""
        if (storageLocationName != null) {
            val item = wareHouseList.singleOrNull { it.storageLocationCode == storageLocationName.trim() }
            return item?.storageLocationName ?: ""
        }
        return location
    }

    private fun updateDateFromList() {
        binding.tvWhValue.text = offloadData.materialCode.plus("-").plus(offloadData.materialName)
        binding.tvstoValue.text = offloadData.supplierCode.plus("-").plus(offloadData.supplierName)
        /*binding.tvReceivingValue.text =
            offloadData.dstorageLocationCode.plus("-").plus(offloadData.dstorageLocationName)*/
        enableDisableHeaderItem(false)
        vm.vegaCoffeeReceivingData = offloadData
        vm.getOBDDetailsSupIndo(offloadData.tempWBId.toString())
    }

    /*private fun updateoriginlist(params: List<VegaQualityParamsWithQualitative>?) {
        params?.let {
            when (it.isNotEmpty()) {
                true -> {
                    OriginList.clear()
                    Departmentlist.clear()
                    it.forEach { item ->
                        if (item.qualityParameter.materialCode == materialCode && item.qualityParameter.nameChar == "CI_COFFEE_TRANS_ORIGIN") {
                            item.qualitative?.forEach { item1 ->
                                OriginList.add(item1)
                            }
                        }
                        if (item.qualityParameter.materialCode == materialCode && item.qualityParameter.nameChar == "CI_COFFEE_TRANS_DEPT") {
                            item.qualitative?.forEach { item1 ->
                                Departmentlist.add(item1)
                            }
                        }
                    }
                    val value = mutableListOf<VegaQualityParamsWithQualitative>()

                    value.addAll(it)

                }
                else -> setErrorContentView("Quality params not available for this material")
            }
        }
    }*/

    private fun moveToSummary() {
        vm.vegaCoffeeReceivingData = offloadData
        vm.vegaCoffeeReceivingData.vehicleNumber = binding.tvTruckNoValue.text.toString()
        vm.vegaCoffeeReceivingData.driverName = binding.tvDriverNameValue.text.toString()
        vm.vegaCoffeeReceivingData.truckDriverName = binding.tvDriverNameValue.text.toString()
        vm.vegaCoffeeReceivingData.contactNumber = binding.tvDriverNoValue.text.toString()
        vm.vegaCoffeeReceivingData.declaredBagCount = binding.etTruckBagcount.text.toString()
        vm.vegaCoffeeReceivingData.vendorDeclaredWeight = binding.etTruckDeclaredweight.text.toString()
        vm.vegaCoffeeReceivingData.imageString = offloadData.imageString
        vm.vegaCoffeeReceivingData.plantId = getPlantDetails().plantId
        vm.vegaCoffeeReceivingData.weighBridgeType = offloadData.weighBridgeType
        vm.vegaCoffeeReceivingData.imageString = offloadData.imageString
        vm.vegaCoffeeReceivingData.origin = binding.tvOrigin.text.toString()
        vm.vegaCoffeeReceivingData.department = binding.tvDestinationLocation.text.toString()
        validateInputs()
    }

    private fun showSingleSelectDialog(
        isWh: Boolean,
        title: String,
        isVendor: Boolean,
        isPoList: Boolean,
        isOrgin: Boolean, isDepartment: Boolean,
        isYear: Boolean = false
    ) {
        var list = ArrayList<String>()
        if (isVendor) {
            list.addAll(supplierList.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) })
        } else if (isWh) {
            list.addAll(materialProduct.map { it.materialCode.plus("-").plus(it.materialName) } as ArrayList<String>)
        } else if (isPoList) {
            filterpurchaseOrderList.clear()
            val purchaseOrderList1 =
                purchaseOrderList/*.filter { it.year == year }.filter { it.poType == PTBF }*/
                    .filter { it.materialNumber.trim().contains(offloadData.materialCode ?: "  ") }
            /* .filter { it.supplier.trim().contains(offloadData.supplierCode?.trim() ?: " ") }*/
            filterpurchaseOrderList.addAll(purchaseOrderList1)
            list.addAll(filterpurchaseOrderList.map { data ->
                data.poId.plus("-").plus(data.openQuantity).plus(offloadData.unitsOfMeasure)
            })
        } else if (isYear) {
            list.addAll(yearFilter)
        } else if (isOrgin) {
            list = OriginList.map {
                it.charValue
            } as ArrayList<String>


        } else if (isDepartment) {
            list = Departmentlist.map {
                it.charValue
            } as ArrayList<String>


        } else {
            list = customLocationList.map {
                it.procureLocationCode.plus(" - ").plus(it.procureLocationName)
            } as ArrayList<String>
        }
        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isWh, isVendor, isPoList,
                list,
                activity!!,
                this, isSupplier = isYear, supplierListener = this, isOrigin = isOrgin, isDepartment = isDepartment
            )

        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }


    private fun enableDisableItem(flag: Boolean) {
        enableDisableHeaderItem(flag)
        binding.tvTruckNoValue.isEnabled = flag
        binding.tvDriverNameValue.isEnabled = flag
        binding.tvDriverNoValue.isEnabled = flag
    }

    private fun enableDisableHeaderItem(flag: Boolean) {
        binding.tvWhValue.isEnabled = flag
        binding.tvstoValue.isEnabled = flag
        binding.tvReceivingValue.isEnabled = flag
        binding.tvWhValue.isClickable = flag
        binding.tvstoValue.isClickable = flag
        binding.tvReceivingValue.isClickable = flag
    }

    private fun setValueEmpty(fragment: Fragment?) {
        binding.tvReceivingValue.text = ""
        binding.tvTruckNoValue.setText("")
        binding.tvDriverNameValue.setText("")
        binding.tvDriverNoValue.setText("")
        when (fragment) {
            is VegaIndoCoffeeSupplierConsignmentFragment -> binding.tvstoValue.text = ""
        }
    }

    private fun validateProceed() {
        vm.vegaCoffeeReceivingData.wsGate = if (offloadData.imageString.equals(WEIGHSCALE)) WS01 else WB01
        /*batchInfo.batch = vm.vegaCoffeeReceivingData.materialCode.plus(vm.vegaCoffeeReceivingData.supplierCode)
            .plus(vm.vegaCoffeeReceivingData.weighBridgeType)*/
        batchInfo.materialNumber = vm.vegaCoffeeReceivingData.materialCode ?: ""
        batchInfo.materialName = vm.vegaCoffeeReceivingData.materialName
        //batchInfo.mtnNumber = batchInfo.batch
        /* vm.vegaCoffeeReceivingData.delivery =
                 vm.vegaCoffeeReceivingData.materialCode.plus(vm.vegaCoffeeReceivingData.supplierCode)
                         .plus(vm.vegaCoffeeReceivingData.weighBridgeType)
         vm.vegaCoffeeReceivingData.commonPrimaryId =
                 vm.vegaCoffeeReceivingData.materialCode.plus(vm.vegaCoffeeReceivingData.supplierCode)
                         .plus(vm.vegaCoffeeReceivingData.weighBridgeType)
         batchInfo.delivery = vm.vegaCoffeeReceivingData.materialCode.plus(vm.vegaCoffeeReceivingData.supplierCode)
                 .plus(vm.vegaCoffeeReceivingData.weighBridgeType)*/
        /* if (vm.vegaCoffeeReceivingData.weighBridgeId.isNullOrEmpty()) {
             vm.vegaCoffeeReceivingData.weighBridgeId = getTmpId()
             vm.vegaCoffeeReceivingData.tempWBId = getTmpId()
         }*/
        vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, VegaCoffeeReceiveLots())
        callBack?.callBack(ADD_WEIGHT, batchInfo, vm.vegaCoffeeReceivingData)
        //callBack?.replaceFragment(MTNR_WEIGHSCALE_SUMMARY, vm.vegaCoffeeReceivingData)
    }

    private fun showRemarkDialog() {

        showDialog(getString(R.string.end_load_msg), object : DialogClick {
            override fun onPositive(remark: String) {
                if (remark.isEmpty()) Toast.makeText(
                    activity,
                    getString(com.olam.warehouse.presentation.R.string.enter_remark),
                    Toast.LENGTH_SHORT
                )
                    .show()
                else {
                    val endTime = System.currentTimeMillis()
                    vm.vegaCoffeeReceivingData.endTime = endTime.toString()
                    val duration = endTime.minus(vm.vegaCoffeeReceivingData.startTime?.toLong() ?: 0)
                    vm.vegaCoffeeReceivingData.remarks = remark
                    vm.vegaCoffeeReceivingData.turnAroundTime = TimeUnit.MILLISECONDS.toMinutes(duration).toString()
                    batchList.forEach {
                        it.delivery = vm.vegaCoffeeReceivingData.delivery
                        vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, it)
                    }
                    //callBack?.replaceFragment(MTNR_WEIGHSCALE_SUMMARY, vm.vegaCoffeeReceivingData)
                }
            }

        }, true, vm.vegaCoffeeReceivingData.remarks.toString())
    }


    fun updateAddWeight(weight: String) {
        val split = weight.split(" ")
        val come: Int? = split[0].toDouble().compareTo(batchList[addWeightPosition].weight?.toDouble() ?: 0.0)
        batchList[addWeightPosition].isLowerWeight = come ?: 0 <= 0
        batchList[addWeightPosition].editedWeight = split[0]
        batchList[addWeightPosition].editedUOM = split[1]
    }

    private fun validateInputs() {
        when {
            vm.vegaCoffeeReceivingData.vehicleNumber.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
            vm.vegaCoffeeReceivingData.driverName.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver))
            vm.vegaCoffeeReceivingData.supplierCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_trans_vendor))
            vm.vegaCoffeeReceivingData.storageLocationCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_loc))
            /*vm.vegaCoffeeReceivingData.purchaseDocNum.isNullOrEmpty() -> {
                if (offloadData.imageString.equals(WEIGHBRIDGE_WEIHSCALE))
                    validateProceed()
                else
                    showSnack(getString(R.string.select_po_error))
            }
            vm.vegaCoffeeReceivingData.declaredBagCount.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_bagcount))
            vm.vegaCoffeeReceivingData.declaredWeight.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_declared))
            binding.etTruckBagcount.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_bagcount))
            binding.etTruckDeclaredweight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_declared))*/
            else -> validateProceed()
        }
    }

    private fun enableProceed() {
        var enable = false
        enable =
            (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverName.text.isNotEmpty()
                    && binding.tvTruckNoValue.text.toString().isNotEmpty()
                    && binding.tvReceivingValue.text.isNotEmpty() /*&& binding.etTruckBagcount.text.isNotEmpty() && binding.etTruckDeclaredweight.text.isNotEmpty() && !binding.tvOrigin.text.equals(
                    " "
                ) && !binding.tvDestinationLabel.text.equals(" ")*/)
        /* if (offloadData.imageString.equals(WEIGHSCALE)) {
             enable =
                 (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverName.text.isNotEmpty()
                         && binding.tvTruckNoValue.text.toString().isNotEmpty()
                         && binding.tvReceivingValue.text.isNotEmpty() *//*&& binding.etTruckBagcount.text.isNotEmpty() && binding.etTruckDeclaredweight.text.isNotEmpty() && !binding.tvOrigin.text.equals(
                    " "
                ) && !binding.tvDestinationLabel.text.equals(" ")*//*)

        } else if (offloadData.imageString.equals(WEIGHBRIDGE_WEIHSCALE)) {
            enable =
                (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverName.text.isNotEmpty()
                        && binding.tvTruckNoValue.text.toString().isNotEmpty()
                        && binding.tvReceivingValue.text.isNotEmpty() && binding.etTruckBagcount.text.isNotEmpty() && binding.etTruckDeclaredweight.text.isNotEmpty())
        }*/

        if (enable) {
            binding.btProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
            binding.btSave.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
            binding.btSave.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btProceed.isEnabled = enable
        binding.btSave.isEnabled = enable
    }

    fun getBack() {
        editLotId = ""
        // vm.getOBDDetails(vm.vegaCoffeeReceivingData.delivery)
    }

    fun editLot(vegaCoffeeReceiveLots: VegaCoffeeReceiveLots) {
        editLotId = vegaCoffeeReceiveLots.batch
        //vm.getOBDDetails(vegaCoffeeReceiveLots.mtnNumber)
    }

    private fun updateWeighbridgeValueUI(response: Resource<GenericReqAndResp<VegaReceiving>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                binding.tvWhValue.text =
                    response.data?.data?.materialCode.plus("-").plus(response.data?.data?.materialName)
                /*binding.tvstoValue.text =
                    response.data?.data?.supplierCode.plus("-").plus(response.data?.data?.supplierName)*/
                /*binding.tvReceivingValue.text =
                    response.data?.data?.storageLocationCode.plus("-")
                        .plus(getLocationName(response.data?.data?.storageLocationCode))*/
                val driverName =
                    if (response.data?.data?.truckDriverName.isNullOrBlank()) response.data?.data?.driverName else response.data?.data?.truckDriverName
                binding.tvDriverNameValue.setText(driverName, TextView.BufferType.EDITABLE)
                binding.tvDriverNoValue.setText(response.data?.data?.contactNumber, TextView.BufferType.EDITABLE)
                binding.tvTruckNoValue.setText(response.data?.data?.vehicleNumber)
                binding.etTruckBagcount.setText(response.data?.data?.bagCount?.trim())
                binding.etTruckDeclaredweight.setText(response.data?.data?.vendorDeclaredWeight)
                binding.tvPOValue.text = response.data?.data?.purchaseDocNum
                offloadData.vehicleNumber = response.data?.data?.vehicleNumber
                offloadData.driverName = response.data?.data?.truckDriverName
                offloadData.truckDriverName = response.data?.data?.truckDriverName
                offloadData.contactNumber = response.data?.data?.contactNumber
                offloadData.storageLocationCode = response.data?.data?.storageLocationCode
                offloadData.storageLocationName = getLocationName(response.data?.data?.storageLocationCode)
                offloadData.materialCode = response.data?.data?.materialCode
                offloadData.materialName = response.data?.data?.materialName
                offloadData.purchaseDocNum = response.data?.data?.purchaseDocNum
                offloadData.purchaseDocDesc = response.data?.data?.purchaseDocDesc
                offloadData.purchaseDocQty = response.data?.data?.purchaseDocQty
                offloadData.unitsOfMeasure = response.data?.data?.unitsOfMeasure ?: ""
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun updatePurchaseOrderUI(response: Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let {
                                purchaseOrderList = it.toMutableList()
                            }
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
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

    override fun clickOnItem(
        data: String,
        isWh: Boolean,
        isVendor: Boolean,
        isGrade: Boolean,
        isSupplier: Boolean,
        isOrigin: Boolean,
        isDepartment: Boolean
    ) {
        customDialog?.dismiss()
        enableProceed()
        if (isWh) {
            binding.tvWhValue.text = data
            val split = data.split("-")
            val material = materialProduct.single { it.materialCode == split[0] }
            offloadData.materialCode = material.materialCode
            offloadData.materialName = material.materialName.toString()
            //binding.tvUom.text = material.unitsOfMeasure.toString()
            offloadData.unitsOfMeasure = material.unitsOfMeasure.toString()
            vm.vegaCoffeeReceivingData.materialCode = offloadData.materialCode
            vm.vegaCoffeeReceivingData.materialName = offloadData.materialName
            if (isNewTruckCall) {
                if (offloadData.materialCode?.length != 18)
                    materialCode = "000000".plus(offloadData.materialCode)
                else
                    materialCode = offloadData.materialCode!!
                /*vm.getQualityParams(materialCode, false, offloadData.weighBridgeId)
                vm.qualitylist.observe(viewLifecycleOwner, Observer {
                    updateoriginlist(it)
                })*/
            }
        } else if (isVendor) {
            val split = data.split("-")
            binding.tvstoValue.text = data
            offloadData.supplierCode = SUPPLIER_APPEND.plus(split[0]).trim()
            offloadData.supplierName = split[1]
            vm.vegaCoffeeReceivingData.supplierCode = offloadData.supplierCode
            vm.vegaCoffeeReceivingData.supplierName = offloadData.supplierName
            vm.vegaCoffeeReceivingData.weighBridgeType = offloadData.weighBridgeType
            /* if (!isNewTruckCall) {
                 vm.getOBDDetails(
                     vm.vegaCoffeeReceivingData.materialCode.plus(vm.vegaCoffeeReceivingData.supplierCode)
                         .plus(vm.vegaCoffeeReceivingData.weighBridgeType)
                 )
             }*/
        } else if (isGrade) {
            binding.tvPOValue.text = data
            val split = data.split("-")
            val item = filterpurchaseOrderList.singleOrNull { it.poId == split[0] }
            offloadData.purchaseDocNum = item?.poId
            offloadData.purchaseDocDesc = item?.ebelp
            offloadData.purchaseDocQty = item?.openQuantity
            offloadData.unitsOfMeasure = item?.meins ?: ""
            vm.vegaCoffeeReceivingData.purchaseDocNum = item?.poId
            vm.vegaCoffeeReceivingData.purchaseDocDesc = item?.ebelp
            vm.vegaCoffeeReceivingData.purchaseDocQty = item?.openQuantity
            vm.vegaCoffeeReceivingData.unitsOfMeasure = item?.meins ?: ""
        } else if (isSupplier) {
            year = data
            binding.tvYearValue.text = data
        } else if (isOrigin) {
            binding.tvOrigin.text = data
        } else if (isDepartment) {
            binding.tvDestinationLocation.text = data
        } else {
            val split = data.split("-")
            binding.tvReceivingValue.text = data
            vm.vegaCoffeeReceivingData.storageLocationCode = split[0].trim()
            vm.vegaCoffeeReceivingData.storageLocationName = split[1].trim()
            offloadData.storageLocationCode = split[0].trim()
            offloadData.storageLocationName = split[1].trim()
            selectedReceivingLocation = customLocationList.single { it.procureLocationCode == split[0].trim() }
        }
    }

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {

    }

    private fun clearAllValues() {

        binding.tvWhValue.text = ""
        binding.tvstoValue.text = ""
        binding.tvYearValue.text = ""
        binding.tvReceivingValue.text = ""
        binding.tvPOValue.text = ""
        binding.tvTruckNoValue.setText(" ")
        binding.tvDriverNameValue.setText(" ")
        binding.tvDriverNoValue.setText(" ")
        binding.etTruckBagcount.setText(" ")
        binding.etTruckDeclaredweight.setText(" ")
        binding.tvOrigin.text = " "
        binding.tvDestinationLocation.text = " "
    }

}

