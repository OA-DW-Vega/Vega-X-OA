package com.olam.warehouse.vegax.offloadingindo.ui.mtnr

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingWarehouseWithMtns
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegaindocoffee.model.VegaIndoCoffeeReceivingMtnrWithLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingindo.R
import com.olam.warehouse.vegax.offloadingindo.databinding.FragmentIndoCoffeeMtnrConsignmentLayoutBinding
import com.olam.warehouse.vegax.offloadingindo.ui.VegaIndoCoffeeOffloadingViewModel
import com.olam.warehouse.vegax.offloadingindo.ui.callback.VegaIndoCoffeeOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingindo.utils.*
import kotlinx.android.synthetic.main.item_indo_coffee_mtnr_weighscale_lot_card_layout.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.concurrent.TimeUnit

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */
class VegaIndoCoffeeMtnrConsignmentFragment : BaseFragment(), VegaSingleSelectListener {

    override val layoutResourceId = R.layout.fragment_indo_coffee_mtnr_consignment_layout
    private lateinit var binding: FragmentIndoCoffeeMtnrConsignmentLayoutBinding
    private var callBack: VegaIndoCoffeeOffloadReplaceFragmentCallback? = null
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private lateinit var moreWeightBatches: String
    private var addWeightPosition: Int = 0
    private val vm: VegaIndoCoffeeOffloadingViewModel by viewModel()
    private var wareHouseList: MutableList<VegaSupplyStorageLocation> = mutableListOf()
    private var sendWareHouseList: MutableList<VegaReceivingWarehouse> = mutableListOf()
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var warehouseWithMtn = VegaCoffeeReceivingWarehouseWithMtns()
    private var selectedSendingLocation = VegaSupplyStorageLocation()
    private var selectedReceivingLocation = VegaCustomStLocation()
    private var selectedOBD = VegaReceivingMtn()
    private var batchList = mutableListOf<VegaCoffeeReceiveLots>()
    private var allBatchList = mutableListOf<VegaCoffeeReceiveLots>()
    private var allOBDList = mutableListOf<VegaReceivingMtn>()
    private var filteredOBDList = mutableListOf<VegaReceivingMtn>()
    private var supplierList = mutableListOf<VegaVendor>()
    private var editLotId: String = ""
    private var offloadData = VegaCoffeeReceiving()
    private var isNewTruckCall = true
    private var materialProduct = ArrayList<VegaMaterial>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaIndoCoffeeOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance(offload: VegaCoffeeReceiving) = VegaIndoCoffeeMtnrConsignmentFragment().putArgs {
            putParcelable(OFFLOAD_DATA, offload)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentIndoCoffeeMtnrConsignmentLayoutBinding.inflate(layoutInflater)
        offloadData = arguments?.getParcelable(OFFLOAD_DATA)!!
        isNewTruckCall = offloadData.weighBridgeId.isBlank()
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
        vm.vegaCoffeeReceivingData = offloadData
        enableProceed()
        binding.tvWhValue.setOnClickListener { showSingleSelectDialog(true, getString(R.string.select_dest_wh), false) }
        binding.tvstoValue.setOnClickListener {
            if (binding.tvWhValue.text.toString().isNotEmpty()) showSingleSelectDialog(
                false,
                getString(R.string.select_obd),
                false
            )
        }
        binding.tvReceivingValue.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.select_receive_loc),
                true
            )
        }
        vm.location.observe(viewLifecycleOwner, Observer { wareHouseList = it.toMutableList() })
        vm.getLocations()
        /*vm.weighBridgeId.observe(viewLifecycleOwner, Observer {
            updateWeighbridgeValueUI(it)
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
            /*customLocationList.forEach {
                if (it.procureLocationCode.equals(receivingData.storageLocationCode)) {
                    binding.tvReceivingLocation.text =
                        receivingData.storageLocationCode.plus("-").plus(it.procureLocationName)
                    receivingData.storageLocationName = it.procureLocationName
                }
            }*/
        })
        vm.getCustomLocations()
        vm.getSuppliers()
        fetchMtnDetails()

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList = it/*.filter { data -> data.bcApprover?.isNotEmpty()!! }*/.toMutableList()

            val supplierData = supplierList/*.filter { data -> data.vendorCode.startsWith("2", true) }*/
            val suppliers = supplierData.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            val productAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suppliers)
            binding.tvVendorValue.threshold = 1
            binding.tvVendorValue.setAdapter(productAdapter)
            binding.tvVendorValue.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                enableProceed()
                it.forEach { material ->
                    if (material.vendorCode == binding.tvVendorValue.text.toString().split(" - ")[0]) {
                        vm.vegaCoffeeReceivingData.transportVendorCode = material.vendorCode
                        vm.vegaCoffeeReceivingData.transportVendorName = material.vendorName
                    }
                }
            }
        })

        binding.tvDriverNoValue.onChange { enableProceed() }
        binding.tvDriverNameValue.onChange { enableProceed() }
        binding.tvTruckNoValue.onChange { enableProceed() }
        binding.btProceed.setOnClickListener { moveToSummary() }
        binding.btSave.setOnClickListener {
            vm.vegaCoffeeReceivingData.vehicleNumber = binding.tvTruckNoValue.text.toString()
            vm.vegaCoffeeReceivingData.driverName = binding.tvDriverNameValue.text.toString()
            vm.vegaCoffeeReceivingData.contactNumber = binding.tvDriverNoValue.text.toString()
            vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, VegaCoffeeReceiveLots())
            activity?.finish()
        }
        vm.offloadingIndoMtnr.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                /*if (!isNewTruckCall) {
                    vm.getWeighBridgeIdDetail(offloadData.weighBridgeId, offloadData.imageString.equals(WEIGHSCALE))
                } else if (!selectedOBD.mtntWbid.isBlank()) {
                    vm.getWeighBridgeIdDetail(selectedOBD.mtntWbid, *//*offloadData.imageString.equals(WEIGHSCALE)*//*true)
                }*/
            } else updateOBDDetails(it)
        })
        if (!isNewTruckCall) {
            updateDateFromList()
        }

        vm.product.observe(viewLifecycleOwner, Observer {
            materialProduct.clear()
            materialProduct.addAll(it)
        })
    }

    private fun getLocationName(storageLocationName: String?): String {
        val location = ""
        if (storageLocationName != null) {
            val item = sendWareHouseList.singleOrNull { it.supplyingPlantId == storageLocationName }
            return item?.supplyingPlantName ?: ""
        }
        return location
    }

    private fun updateDateFromList() {
        binding.tvTruckNoValue.setText(offloadData.vehicleNumber)
        binding.tvDriverNameValue.setText(offloadData.driverName)
        binding.tvDriverNoValue.setText(offloadData.contactNumber)
        binding.tvWhValue.text =
            offloadData.dstorageLocationCode.plus("-").plus(offloadData.dstorageLocationName)
        /*binding.tvWhValue.text = offloadData.storageLocationCode.plus("-").plus(offloadData.storageLocationName)*/
        binding.tvstoValue.text = offloadData.delivery
        binding.tvReceivingValue.text =
            offloadData.storageLocationCode.plus("-").plus(offloadData.storageLocationName)
        binding.tvMaterialValue.text = offloadData.materialName
        enableDisableHeaderItem(false)
        vm.vegaCoffeeReceivingData = offloadData
        vm.getOBDDetailsIndo(offloadData.tempWBId.toString())
    }


    private fun moveToSummary() {
        binding.tvVendorValue.isCursorVisible = false
        vm.vegaCoffeeReceivingData.vehicleNumber = binding.tvTruckNoValue.text.toString()
        vm.vegaCoffeeReceivingData.driverName = binding.tvDriverNameValue.text.toString()
        vm.vegaCoffeeReceivingData.contactNumber = binding.tvDriverNoValue.text.toString()
        vm.vegaCoffeeReceivingData.imageString = offloadData.imageString
        vm.vegaCoffeeReceivingData.plantId = getPlantDetails().plantId
        if (vm.vegaCoffeeReceivingData.storageLocationCode.isNullOrBlank()) {
            vm.vegaCoffeeReceivingData.storageLocationCode = offloadData.storageLocationCode
            vm.vegaCoffeeReceivingData.storageLocationName = offloadData.storageLocationName
        }
        validateInputs()
        /*val addedWeight = batchList.any { it.editedWeight.isNullOrEmpty() || it.editedWeight.equals("0.0") }
        if(addedWeight) activity?.toast(getString(R.string.add_lot_weight))
        else callBack?.replaceFragment(MTNR_WEIGHSCALE_SUMMARY,vm.vegaCoffeeReceivingData)*/
    }

    private fun updateOBDDetails(data: VegaIndoCoffeeReceivingMtnrWithLots) {
        val fragment = activity?.supportFragmentManager?.findFragmentById(R.id.flContainer)
        if (data != null) {
            setValueEmpty(fragment)
            /*binding.tvReceivingValue.text =
                data.receiving.storageLocationCode.plus(" - ").plus(data.receiving.storageLocationName)*/
            if (data.receiving.transportVendorCode?.isNullOrBlank() == false) {
                when (fragment) {
                    is VegaIndoCoffeeMtnrConsignmentFragment -> {
                        binding.tvVendorValue.setText(
                            data.receiving.transportVendorCode.plus(" - ").plus(data.receiving.transportVendorName)
                        )
                    }
                }
            } else
                binding.tvVendorValue.setText("")
            binding.tvTruckNoValue.setText(data.receiving.vehicleNumber)
            binding.tvDriverNameValue.setText(data.receiving.truckDriverName)
            binding.tvDriverNoValue.setText(data.receiving.contactNumber)
            binding.tvReceivingValue.text =
                data.receiving.storageLocationCode.plus("-").plus(data.receiving.storageLocationName)
            vm.vegaCoffeeReceivingData = data.receiving
            if (data.lineItems.size > 0) {
                if (batchList.size == 0) batchList =
                    data.lineItems.map { it.lots } as MutableList<VegaCoffeeReceiveLots>
                batchList.forEach {
                    var edWeight = 0.0
                    val bags = data.lineItems.filter { it2 -> it2.lots.mtnNumber.equals(it.mtnNumber) }
                        .filter { it1 -> it1.lots.batch.equals(it.batch) }
                    if (bags.size == 1) {
                        if (vm.vegaCoffeeReceivingData.startTime?.isEmpty() == true) vm.vegaCoffeeReceivingData.startTime =
                            DateUtils.getCurrentTimeInMills().toString()
                    }
                    if (bags.size > 0) edWeight = bags[0].bagItem.filter { it2 -> it2.mtnNumber.equals(it.mtnNumber) }
                        .filter { it1 -> it1.batchNumber.equals(it.batch) }
                        .sumByDouble { it2 -> it2.netWeight.toDouble() }
                    it.editedWeight = edWeight.toString()
                }
                vm.vegaCoffeeReceivingData.netWeight = batchList.sumByDouble {
                    if (it.editedWeight?.isNotEmpty() == true) it.editedWeight?.toDouble() ?: 0.0 else 0.0
                }.toString()
                vm.vegaCoffeeReceivingData.grossWeight =
                    batchList.sumByDouble { if (it.weight?.isNotEmpty() == true) it.weight?.toDouble() ?: 0.0 else 0.0 }
                        .toString()
                setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
            } else {
                setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
            }
        } else {
            setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
        }
        enableProceed()
    }


    private fun fetchMtnDetails() {
        vm.warehouse.observe(viewLifecycleOwner, Observer { processApiResult(it) })
        if (isOnline()) vm.fetchWarehouseWithMtns() else fetchOfflineMtnDetails()
    }

    private fun fetchOfflineMtnDetails() {
        vm.getWarehouses()
        vm.getMTNRs()
        vm.getOfflineLots()
        vm.getStorageLoc()

        vm.warehouseLocal.observe(viewLifecycleOwner, Observer {
            sendWareHouseList = it.toMutableList()
        })
        vm.mtnrLocal.observe(viewLifecycleOwner, Observer { item ->
            var temp = item.filter { !it.isSynced!! }
            allOBDList = temp.toMutableList()
        })
        vm.lotLocal.observe(viewLifecycleOwner, Observer {
            allBatchList = prepareBatchList(it)
        })

        vm.storageLocal.observe(viewLifecycleOwner, Observer {
            wareHouseList = it.toMutableList()
        })

    }

    private fun processApiResult(data: Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> saveResult(it.data)
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> UIUtils.showErrorDialog(requireContext(), it.error.toString())
            }
        }
    }

    private fun saveResult(data: GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>?) {
        hideLoading()
        data?.data?.let {
            wareHouseList = data.data.storageLocationLst.toMutableList()
            sendWareHouseList = data.data.stockSupplyingPlants.toMutableList()
            allOBDList = data.data.mtns.toMutableList()
            allBatchList = data.data.batchDetails.toMutableList()
            if (!isNewTruckCall) {
                val item = sendWareHouseList.singleOrNull { offloadData.dstorageLocationCode == it.supplyingPlantId }
                if (item != null) {
                    offloadData.storageLocationName = item.supplyingPlantName
                    if (!offloadData.dstorageLocationCode.isNullOrBlank())
                        binding.tvWhValue.text =
                            offloadData.dstorageLocationCode.plus("-").plus(offloadData.dstorageLocationName)
                }
                batchList = allBatchList.filter { it.mtnNumber == offloadData.delivery }.toMutableList()
                setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
            }
        }
    }

    private fun showSingleSelectDialog(isWh: Boolean, title: String, isVendor: Boolean) {
        var list = ArrayList<String>()
        if (isVendor) {
            list = customLocationList.map {
                it.procureLocationCode.plus(" - ").plus(it.procureLocationName)
            } as ArrayList<String>

        } else if (isWh) {
            list = sendWareHouseList.map {
                it.supplyingPlantId.plus(" - ").plus(it.supplyingPlantName)
            } as ArrayList<String>
        } else {
            list.addAll(filteredOBDList.map { it.mtnNumber }.toSet())
        }
        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isWh, isVendor, false,
                list,
                activity!!,
                this, isOrigin = false, isDepartment = false
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {
        customDialog?.dismiss()
        enableProceed()
        if (isWh) {
            val whId = data.split("-")
            binding.tvWhValue.text = data
//            vm.vegaCoffeeReceivingData.storageLocationCode = whId[0].trim()
//            vm.vegaCoffeeReceivingData.storageLocationName = whId[1].trim()
            vm.vegaCoffeeReceivingData.dstorageLocationCode = whId[0].trim()
            vm.vegaCoffeeReceivingData.dstorageLocationName = whId[1].trim()
            // selectedSendingLocation = wareHouseList.single { it.storageLocationCode == whId[0].trim() }
            filteredOBDList =
                allOBDList.filter { it.supplyingPlantId == /*selectedSendingLocation.storageLocationCode*/ whId[0].trim() }
                    .toMutableList()
        } else if (!isVendor) {
            binding.tvstoValue.text = data
            vm.vegaCoffeeReceivingData.mtnCode = data
            vm.vegaCoffeeReceivingData.delivery = data
            selectedOBD = filteredOBDList.filter { it.mtnNumber == data }[0]
            batchList = allBatchList.filter { it.mtnNumber == selectedOBD.mtnNumber }.toMutableList()
            vm.vegaCoffeeReceivingData.purchaseDocNum =
                if (batchList.size > 0) batchList[0].purchaseOrder else selectedOBD.purchaseOrder
            vm.vegaCoffeeReceivingData.purchaseDocDesc =
                if (batchList.size > 0) batchList[0].ebelp else selectedOBD.ebelp
            //vm.getOBDDetails(data)
            vm.vegaCoffeeReceivingData.storageLocationCode = ""
            vm.vegaCoffeeReceivingData.storageLocationName = ""
            setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
            enableProceed()
        } else {
            val whId = data.split("-")
            binding.tvReceivingValue.text = data
            vm.vegaCoffeeReceivingData.plantId = getPlantDetails().plantId
            vm.vegaCoffeeReceivingData.storageLocationCode = whId[0].trim()
            vm.vegaCoffeeReceivingData.storageLocationName = whId[1].trim()
            selectedReceivingLocation = customLocationList.single { it.procureLocationCode == whId[0].trim() }
            enableProceed()
        }
    }

    private fun setUpAdapter(list: ArrayList<VegaCoffeeReceiveLots>) {
        var lineItems = mutableListOf<VegaCoffeeReceiveLots>()
        if (editLotId.isNotEmpty()) {
            enableDisableItem(false)
            lineItems = list.filter { it.batch.equals(editLotId) } as MutableList<VegaCoffeeReceiveLots>
        } else if (!isNewTruckCall) {
            enableDisableHeaderItem(false)
            lineItems = list
        } else {
            enableDisableItem(true)
            lineItems = list
        }
        binding.rvList.setUp(lineItems, R.layout.item_indo_coffee_mtnr_weighscale_lot_card_layout,
            { item, pos ->
                //binding.tvMaterialValue.text = item.materialName
                tvScaleLotValue.text = item.batch
                tvStLocationValue.text = item.storageLocationCode
                tvScaleWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(item.uom)
                tvScaleGradeValue.text = item.materialName
                val editedWeight =
                    if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString().toDouble()
                        .formatThreeDigits()
                tvScaleDispatchValue.text = editedWeight
                tvDispatchUOMValue.text = item.uom
                ivEdit.visibility = View.GONE
                tv_add_weight.setOnClickListener {
                    binding.tvVendorValue.isCursorVisible = false
                    addWeightPosition = pos
                    vm.vegaCoffeeReceivingData.vehicleNumber = binding.tvTruckNoValue.text.toString()
                    vm.vegaCoffeeReceivingData.driverName = binding.tvDriverNameValue.text.toString()
                    vm.vegaCoffeeReceivingData.truckDriverName = binding.tvDriverNameValue.text.toString()
                    vm.vegaCoffeeReceivingData.contactNumber = binding.tvDriverNoValue.text.toString()
                    item.delivery = item.mtnNumber
                    if (vm.vegaCoffeeReceivingData.startTime.isNullOrBlank()) {
                        vm.vegaCoffeeReceivingData.startTime = System.currentTimeMillis().toString()
                    }
                    vm.vegaCoffeeReceivingData.commonPrimaryId =
                        vm.vegaCoffeeReceivingData.weighBridgeType.plus(vm.vegaCoffeeReceivingData.supplierCode)
                            .plus(vm.vegaCoffeeReceivingData.delivery)
                    vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, item)
                    item.tempWBId = vm.vegaCoffeeReceivingData.tempWBId.toString()
                    callBack?.replaceFragment(ADD_WEIGHT, item)
                }
            })
    }

    private fun enableDisableItem(flag: Boolean) {
        enableDisableHeaderItem(flag)
        binding.tvVendorValue.isEnabled = flag
        binding.tvTruckNoValue.isEnabled = flag
        binding.tvDriverNameValue.isEnabled = flag
        binding.tvDriverNoValue.isEnabled = flag
    }

    private fun enableDisableHeaderItem(flag: Boolean) {
        binding.tvWhValue.isEnabled = flag
        binding.tvstoValue.isEnabled = flag
        // binding.tvReceivingValue.isEnabled = flag
        binding.tvWhValue.isClickable = flag
        binding.tvstoValue.isClickable = flag
    }

    private fun setValueEmpty(fragment: Fragment?) {
        when (fragment) {
            is VegaIndoCoffeeMtnrConsignmentFragment -> {
                binding.tvVendorValue.setText("")
                binding.tvReceivingValue.text = ""
                binding.tvTruckNoValue.setText("")
                binding.tvDriverNameValue.setText("")
                binding.tvDriverNoValue.setText("")
            }
        }
    }

    private fun validateProceed() {
        if (batchList.isNotEmpty()) {
            if (validateEmptyWeight()) {
                if (validateLotWeight()) {
                    // if (editLotId.isEmpty()) showRemarkDialog() else {
                        batchList.forEach {
                            it.delivery = vm.vegaCoffeeReceivingData.delivery
                            vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, it)
                        }
                        callBack?.replaceFragment(MTNR_WEIGHSCALE_SUMMARY, vm.vegaCoffeeReceivingData)
                    //}
                } else Toast.makeText(
                    activity,
                    getString(R.string.lot_more_weight).plus(" - ").plus(moreWeightBatches),
                    Toast.LENGTH_SHORT
                ).show()
            } else Toast.makeText(
                activity,
                getString(R.string.less_weight_error),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                activity,
                getString(R.string.please_add_lot),
                Toast.LENGTH_SHORT
            ).show()
        }
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
                    callBack?.replaceFragment(MTNR_WEIGHSCALE_SUMMARY, vm.vegaCoffeeReceivingData)
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
        binding.rvList.adapter?.notifyItemChanged(addWeightPosition)
        vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, batchList[addWeightPosition])
    }

    private fun validateInputs() {
        when {
            vm.vegaCoffeeReceivingData.vehicleNumber.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
            vm.vegaCoffeeReceivingData.driverName.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver))
            //vm.vegaCoffeeReceivingData.transportVendorCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_trans_vendor))
            vm.vegaCoffeeReceivingData.storageLocationCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_loc))
            else -> validateProceed()
        }
    }

    private fun validateLotWeight(): Boolean {
        val selected = batchList.filter { !it.isLowerWeight }
        moreWeightBatches = selected.map { it.batch }.toString().replace("[", "").replace("]", "")
        return selected.isEmpty()
    }

    private fun validateEmptyWeight(): Boolean {
        val emptyWeight =
            batchList.filter {
                it.editedWeight.equals("0.0") || it.editedWeight.equals("0") || it.editedWeight.equals(
                    ""
                )
            }
        return emptyWeight.isEmpty()
    }

    private fun enableProceed() {
        val enable =
            (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverName.text.isNotEmpty()
                    && binding.tvTruckNoValue.text.toString().isNotEmpty() /*&& binding.tvVendorValue.text.toString()
                .isNotEmpty()*/
                    && binding.tvReceivingValue.text.isNotEmpty())
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
        vm.getOBDDetailsIndo(vm.vegaCoffeeReceivingData.tempWBId.toString())
    }

    fun editLot(vegaCoffeeReceiveLots: VegaCoffeeReceiveLots) {
        editLotId = vegaCoffeeReceiveLots.batch
        vm.getOBDDetailsIndo(vegaCoffeeReceiveLots.tempWBId.toString())
    }

    private fun updateWeighbridgeValueUI(response: Resource<GenericReqAndResp<VegaReceiving>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                binding.tvMaterialValue.text =
                    response.data?.data?.materialCode.plus("-").plus(response.data?.data?.materialName)
                var driverName =
                    if (response.data?.data?.driverName.isNullOrEmpty()) response.data?.data?.truckDriverName else response.data?.data?.driverName
                binding.tvDriverNameValue.setText(driverName, TextView.BufferType.EDITABLE)
                binding.tvDriverNoValue.setText(response.data?.data?.contactNumber, TextView.BufferType.EDITABLE)
                binding.tvTruckNoValue.setText(response.data?.data?.vehicleNumber)
                binding.tvWhValue.text =
                    response.data?.data?.dstorageLocationCode.plus("-").plus(response.data?.data?.dstorageLocationName)
                if (offloadData.imageString == WEIGHBRIDGE_WEIHSCALE && offloadData.weighBridgeType == STO)
                    binding.tvReceivingValue.text =
                        response.data?.data?.dstorageLocationCode.plus("-")
                            .plus(response.data?.data?.dstorageLocationName)
                offloadData.vehicleNumber = response.data?.data?.vehicleNumber
                offloadData.driverName = response.data?.data?.driverName
                offloadData.contactNumber = response.data?.data?.contactNumber
                offloadData.storageLocationCode = response.data?.data?.storageLocationCode
                offloadData.storageLocationName = getLocationName(response.data?.data?.storageLocationCode)
                offloadData.materialCode = response.data?.data?.materialCode
                offloadData.materialName = response.data?.data?.materialName
                vm.vegaCoffeeReceivingData.materialCode = offloadData.materialCode
                vm.vegaCoffeeReceivingData.materialName = offloadData.materialName
                offloadData.dstorageLocationName = response.data?.data?.dstorageLocationName
                offloadData.dstorageLocationCode = response.data?.data?.dstorageLocationCode
                vm.vegaCoffeeReceivingData.dstorageLocationCode = response.data?.data?.dstorageLocationCode
                vm.vegaCoffeeReceivingData.dstorageLocationName = response.data?.data?.dstorageLocationName
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
}

