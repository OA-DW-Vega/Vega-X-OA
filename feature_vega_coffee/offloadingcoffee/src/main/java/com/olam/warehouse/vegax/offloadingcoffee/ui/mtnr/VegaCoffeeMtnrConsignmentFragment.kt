package com.olam.warehouse.vegax.offloadingcoffee.ui.mtnr

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingWarehouseWithMtns
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcoffee.R
import com.olam.warehouse.vegax.offloadingcoffee.databinding.FragmentMtnrConsignmentLayoutBinding
import com.olam.warehouse.vegax.offloadingcoffee.ui.VegaCoffeeOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingcoffee.ui.VegaCoffeeOffloadingViewModel
import com.olam.warehouse.vegax.offloadingcoffee.utils.*
import com.olam.warehouse.vegax.offloadingcoffee.work.getDispatchQualityRequestWorker
import kotlinx.android.synthetic.main.item_mtnr_weighscale_lot_card_layout.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.concurrent.TimeUnit

class VegaCoffeeMtnrConsignmentFragment : BaseFragment(), VegaSingleSelectListener {

    override val layoutResourceId = R.layout.fragment_mtnr_consignment_layout
    private lateinit var binding: FragmentMtnrConsignmentLayoutBinding
    private var callBack: VegaCoffeeOffloadReplaceFragmentCallback? = null
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private lateinit var moreWeightBatches: String
    private var addWeightPosition: Int = 0
    private val vm: VegaCoffeeOffloadingViewModel by viewModel()
    private var wareHouseList: MutableList<VegaSupplyStorageLocation> = mutableListOf()
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var stoargeLocationList = mutableListOf<VegaStorageLocation>()
    private var warehouseWithMtn = VegaCoffeeReceivingWarehouseWithMtns()
    private var selectedSendingLocation = VegaSupplyStorageLocation()
    private var selectedStorageReceivingLocation = VegaStorageLocation()
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
    private var currentKey = getCurrentKey()
    private var userplantId = getPlantDetails().plantId
    private lateinit var storagePlantId: String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance(offload: VegaCoffeeReceiving) =
            VegaCoffeeMtnrConsignmentFragment().putArgs {
                putParcelable(OFFLOAD_DATA, offload)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        offloadData = arguments?.getParcelable(OFFLOAD_DATA)!!
        binding = FragmentMtnrConsignmentLayoutBinding.inflate(layoutInflater)
        isNewTruckCall = offloadData.weighBridgeId.isBlank()
        if (currentKey.split("_")[1].contains("NI")) {
            binding.tvTruckIdValue.visibility = View.VISIBLE
            binding.tvTruckIdValue.isEnabled = true
            binding.tvGrade.visibility = View.VISIBLE
            binding.tvGradeValue.visibility = View.VISIBLE
            binding.tvCertification.visibility = View.VISIBLE
            binding.tvCertificateValue.visibility = View.VISIBLE
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        if (currentKey.split("_")[1].contains("NI")) {
            binding.tvGradeValue.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    //
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    grade = binding.tvGradeValue.text.toString()
                }

                override fun afterTextChanged(p0: Editable?) {
                    //
                }
            })
            binding.tvCertificateValue.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    //
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    certification = binding.tvCertificateValue.text.toString()
                }

                override fun afterTextChanged(p0: Editable?) {
                    //
                }

            })

        }
        TrackHelper.track().screen("offloadingcoffee/ui/mtnr/VegaCoffeeMtnrTypeSelectFragment")
            .title("Mtnr Coffee")
            .with(tracker)
    }

    private fun initUI() {
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
        vm.weighBridgeId.observe(viewLifecycleOwner, Observer {
            updateWeighbridgeValueUI(it)
        })
        if (currentKey.split("_")[1].contains("NI")) {
            vm.storeLocation.observe(viewLifecycleOwner, Observer {
                stoargeLocationList = it.toMutableList()
                val receive = it
                if (!isNewTruckCall) {

                    val item =
                        receive.singleOrNull { offloadData.dstorageLocationCode == it.storageLocationCode }
                    offloadData.dstorageLocationName = item?.storageLocationName ?: ""
                    binding.tvReceivingValue.text =
                        offloadData.storageLocationCode.plus("-")
                            .plus(offloadData.dstorageLocationName)
                }
            })
            vm.getStorageLocations()
        } else {
            vm.custonLocation.observe(viewLifecycleOwner, Observer {
                customLocationList =
                    it.filter { it.storageLocationType.equals("P") }.toMutableList()
                val receive = it
                if (!isNewTruckCall) {

                    val item =
                        receive.singleOrNull { offloadData.dstorageLocationCode == it.procureLocationCode }
                    offloadData.dstorageLocationName = item?.procureLocationName ?: ""
                    binding.tvReceivingValue.text =
                        offloadData.storageLocationCode.plus("-")
                            .plus(offloadData.dstorageLocationName)
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
        }
        vm.getSuppliers()
        fetchMtnDetails()

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList = it/*.filter { data -> data.bcApprover?.isNotEmpty()!! }*/.toMutableList()

            val supplierData =
                supplierList/*.filter { data -> data.vendorCode.startsWith("2", true) }*/
            val suppliers =
                supplierData.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            val productAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suppliers)
            binding.tvVendorValue.threshold = 1
            binding.tvVendorValue.setAdapter(productAdapter)
            binding.tvVendorValue.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, _, _ ->
                    enableProceed()
                    it.forEach { material ->
                        if (material.vendorCode == binding.tvVendorValue.text.toString()
                                .split(" - ")[0]
                        ) {
                            vm.vegaCoffeeReceivingData.transportVendorCode = material.vendorCode
                            vm.vegaCoffeeReceivingData.transportVendorName = material.vendorName
                        }
                    }
                }
        })
        if (currentKey.split("_")[1].contains("NI")) {
            //added on 30-08-21
            binding.tvTruckIdValue.setOnClickListener {
                startActivityForResult(
                    Intent(requireContext(), ScannerActivity::class.java),
                    Constants.ACTIVITY_SCAN_REQUEST_CODE
                )
            }
        }
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
        vm.offloadingMtnr.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                if (!isNewTruckCall) {
                    vm.getWeighBridgeIdDetail(offloadData.weighBridgeId, offloadData.imageString.equals(WEIGHSCALE))
                } else if (!selectedOBD.mtntWbid.isBlank()) {
                    vm.getWeighBridgeIdDetail(selectedOBD.mtntWbid, offloadData.imageString.equals(WEIGHSCALE))
                }
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
            val item = wareHouseList.singleOrNull { it.storageLocationCode == storageLocationName }
            return item?.storageLocationName ?: ""
        }
        return location
    }

    private fun updateDateFromList() {
        binding.tvTruckNoValue.setText(offloadData.vehicleNumber)
        binding.tvDriverNameValue.setText(offloadData.driverName)
        binding.tvDriverNoValue.setText(offloadData.contactNumber)
        binding.tvWhValue.text =
            offloadData.dstorageLocationCode.plus("-").plus(getLocationName(offloadData.dstorageLocationCode))
        /*binding.tvWhValue.text = offloadData.storageLocationCode.plus("-").plus(offloadData.storageLocationName)*/
        binding.tvstoValue.text = offloadData.delivery
        binding.tvReceivingValue.text =
            offloadData.storageLocationCode.plus("-").plus(offloadData.storageLocationName)
        binding.tvMaterialValue.text = offloadData.materialName
        enableDisableHeaderItem(false)
        vm.vegaCoffeeReceivingData = offloadData
        vm.getOBDDetails(offloadData.delivery)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == Constants.ACTIVITY_SCAN_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                binding.tvstoValue.text = data?.extras?.getString(Constants.SCANNED_ID)
                    ?.trim()//?.let { /*validateId(it)*/ }
                binding.tvstoValue.text?.let { validateId(it as String) }
            }
        }
    }
    private fun validateId(data: String) {
        if (AppUtils.isOnline()) {
            val list = ArrayList<String>()
            list.addAll(filteredOBDList.map { it.mtnNumber }.toSet())

            if (list.contains(data))
                clickOnItem(data, false, false, false)
            else
                UIUtils.showErrorDialog(requireContext(), " Invalid OBD")

        }
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
            vm.vegaCoffeeReceivingData.location = offloadData.location
        }
        vm.vegaCoffeeReceivingData.bagCount = offloadData.bagCount
        validateInputs()
        /*val addedWeight = batchList.any { it.editedWeight.isNullOrEmpty() || it.editedWeight.equals("0.0") }
        if(addedWeight) activity?.toast(getString(R.string.add_lot_weight))
        else callBack?.replaceFragment(MTNR_WEIGHSCALE_SUMMARY,vm.vegaCoffeeReceivingData)*/
    }

    private fun updateOBDDetails(data: VegaCoffeeReceivingMtnrWithLots?) {
        val fragment = activity?.supportFragmentManager?.findFragmentById(R.id.flContainer)
        setValueEmpty(fragment)
        if (data != null) {
            /*binding.tvReceivingValue.text =
                data.receiving.storageLocationCode.plus(" - ").plus(data.receiving.storageLocationName)*/
            if (data.receiving.transportVendorCode?.isNullOrBlank() == false) {
                when (fragment) {
                    is VegaCoffeeMtnrConsignmentFragment -> {
                        binding.tvVendorValue.setText(
                            data.receiving.transportVendorCode.plus(" - ")
                                .plus(data.receiving.transportVendorName)
                        )
                    }
                }
            } else
                binding.tvVendorValue.setText("")
            binding.tvTruckNoValue.setText(data.receiving.vehicleNumber)
            binding.tvDriverNameValue.setText(data.receiving.truckDriverName)
            binding.tvDriverNoValue.setText(data.receiving.contactNumber)
            if (currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains("COFF")) {
                if (data.receiving.storageLocationCode?.isBlank() == false)
                    binding.tvReceivingValue.text = data.receiving.storageLocationCode.plus("-")
                        .plus(data.receiving.storageLocationName)
                enableProceed()
            }
            vm.vegaCoffeeReceivingData = data.receiving
            if (data.lineItems.size > 0) {
                batchList.forEach {
                    var edWeight = 0.0
                    val bags =
                        data.lineItems.filter { it2 -> it2.lots.mtnNumber.equals(it.mtnNumber) }
                            .filter { it1 -> it1.lots.batch.equals(it.batch) }
                    if (bags.size == 1) {
                        if (vm.vegaCoffeeReceivingData.startTime?.isEmpty() == true) vm.vegaCoffeeReceivingData.startTime =
                            DateUtils.getCurrentTimeInMills().toString()
                    }
                    if (bags.size > 0) edWeight =
                        bags[0].bagItem.filter { it2 -> it2.mtnNumber.equals(it.mtnNumber) }
                            .filter { it1 -> it1.batchNumber.equals(it.batch) }
                            .sumByDouble { it2 -> it2.netWeight.toDouble() }
                    it.editedWeight = edWeight.toString()
                }
                setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
            } else {
                setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
            }
        } else {
            setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
        }
    }


    private fun fetchMtnDetails() {
        vm.warehouse.observe(viewLifecycleOwner, Observer { processApiResult(it) })
        vm.fetchWarehouseWithMtns()
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
            wareHouseList = data.data.storageLocationLst.toSet().toMutableList()
            allOBDList = data.data.mtns.toMutableList()
            allBatchList = data.data.batchDetails.toMutableList()
            if (!isNewTruckCall) {
                val item = wareHouseList.singleOrNull { offloadData.storageLocationCode == it.storageLocationCode }
                if (item != null) {
                    offloadData.storageLocationName = item.storageLocationName
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
        var customlist = ArrayList<VegaSupplyStorageLocation>()
        if (isVendor) {
            if (currentKey.split("_")[1].contains("NI"))
                list = stoargeLocationList.map {
                    it.storageLocationCode.plus(" - ").plus(it.storageLocationName)
                } as ArrayList<String>
            else
                list = customLocationList.map {
                    it.procureLocationCode.plus(" - ").plus(it.procureLocationName)
                } as ArrayList<String>
        } else if (isWh) {
            if (currentKey.split("_")[1].contains("NI")) {
                //useFilterNot
                customlist = wareHouseList.filterNot {
                    it.plant.equals(userplantId)
                } as ArrayList<VegaSupplyStorageLocation>
                customlist.sortBy { it.plant }
                list = customlist.map {
                    it.plant.plus("-").plus(it.storageLocationCode).plus(" - ")
                        .plus(it.storageLocationName)
                } as ArrayList<String>
            } else
                list = wareHouseList.map {
                    it.storageLocationCode.plus(" - ").plus(it.storageLocationName)
                } as ArrayList<String>
        } else {
            list.addAll(filteredOBDList.map { it.mtnNumber }.sorted().toSet())
        }
        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isWh, isVendor, false,
                list,
                activity!!,
                this,isOrigin = false,isDepartment = false
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
            if (currentKey.split("_")[1].contains("NI")) {
                storagePlantId = whId[0].trim()
                vm.vegaCoffeeReceivingData.location = whId[0].trim()
                vm.vegaCoffeeReceivingData.supplierCode = whId[1].trim()
                vm.vegaCoffeeReceivingData.supplierName = whId[2].trim()
                vm.vegaCoffeeReceivingData.dstorageLocationCode = whId[1].trim()
                vm.vegaCoffeeReceivingData.dstorageLocationName = whId[2].trim()
                selectedSendingLocation =
                    wareHouseList.single { it.storageLocationCode == whId[1].trim() && it.plant == whId[0].trim() }
                filteredOBDList =
                    allOBDList.filter { it.supplyingPlantId == storagePlantId && it.storageLocationCode == selectedSendingLocation.storageLocationCode }
                        .toMutableList()
            } else {
                vm.vegaCoffeeReceivingData.supplierCode = whId[0].trim()
                vm.vegaCoffeeReceivingData.supplierName = whId[1].trim()
                vm.vegaCoffeeReceivingData.dstorageLocationCode = whId[0].trim()
                vm.vegaCoffeeReceivingData.dstorageLocationName = whId[1].trim()
                selectedSendingLocation =
                    wareHouseList.single { it.storageLocationCode == whId[0].trim() }
                filteredOBDList =
                    allOBDList.filter { it.storageLocationCode == selectedSendingLocation.storageLocationCode }
                        .toMutableList()
            }
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
            vm.getOBDDetails(data)
            vm.vegaCoffeeReceivingData.storageLocationCode = ""
            vm.vegaCoffeeReceivingData.storageLocationName = ""
            vm.vegaCoffeeReceivingData.storageLocationCode = ""
            vm.vegaCoffeeReceivingData.storageLocationName = ""
            setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
        } else {
            val whId = data.split("-")
            binding.tvReceivingValue.text = data
            vm.vegaCoffeeReceivingData.plantId = getPlantDetails().plantId
            vm.vegaCoffeeReceivingData.storageLocationCode = whId[0].trim()
            vm.vegaCoffeeReceivingData.storageLocationName = whId[1].trim()

            if (currentKey.split("_")[1].contains("NI")) {
                selectedStorageReceivingLocation =
                    stoargeLocationList.single { it.storageLocationCode == whId[0].trim() }
            } else {
                selectedReceivingLocation =
                    customLocationList.single { it.procureLocationCode == whId[0].trim() }
            }
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
        binding.rvList.setUp(lineItems, R.layout.item_mtnr_weighscale_lot_card_layout,
            { item, pos ->
                //binding.tvMaterialValue.text = item.materialName
                tvScaleLotValue.text = item.batch
                tvStLocationValue.text = item.storageLocationCode
                tvScaleWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(item.uom)
                tvScaleGradeValue.text = item.materialName
                val editedWeight =
                    if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString()
                        .toDouble()
                        .formatThreeDigits()
                tvScaleDispatchValue.text = editedWeight
                tvDispatchUOMValue.text = item.uom
                if (currentKey.split("_")[1].contains("NI")) {
                    onGetQualityParams(item.batch, item.materialNumber, pos)
                    vm.vegaCoffeeReceivingData.declaredWeight =
                        item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(item.uom)
                }
                ivEdit.visibility = View.GONE
                tv_add_weight.setOnClickListener {
                    binding.tvVendorValue.isCursorVisible = false
                    addWeightPosition = pos
                    vm.vegaCoffeeReceivingData.vehicleNumber =
                        binding.tvTruckNoValue.text.toString()
                    vm.vegaCoffeeReceivingData.driverName =
                        binding.tvDriverNameValue.text.toString()
                    vm.vegaCoffeeReceivingData.truckDriverName =
                        binding.tvDriverNameValue.text.toString()
                    vm.vegaCoffeeReceivingData.contactNumber =
                        binding.tvDriverNoValue.text.toString()
                    item.delivery = item.mtnNumber
                    if (vm.vegaCoffeeReceivingData.startTime.isNullOrBlank()) {
                        vm.vegaCoffeeReceivingData.startTime = System.currentTimeMillis().toString()
                    }
                    vm.vegaCoffeeReceivingData.commonPrimaryId =
                        vm.vegaCoffeeReceivingData.weighBridgeType.plus(vm.vegaCoffeeReceivingData.supplierCode)
                            .plus(vm.vegaCoffeeReceivingData.delivery)
                    vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, item)
                    callBack?.replaceFragment(ADD_WEIGHT, item)
                }
            })
    }

    private fun onGetQualityParams(batchNumber: String, materialCode: String, pos: Int) {
        val input = workDataOf(Constants.BATCH_NUMBER to batchNumber, MATERIAL to materialCode)
        val worker = getDispatchQualityRequestWorker(input, pos)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer { workInfo ->

                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val position = AppUtils.posExtension(workInfo.tags)
                            val batchNo = workInfo.outputData.getString(LOT_ID)
                            val quality = workInfo.outputData.getString(QUALITY_GRADE)
                            val certificate = workInfo.outputData.getString(CERTIFICATION)
                            binding.tvGradeValue.setText(quality)
                            binding.tvCertificateValue.setText(certificate)
                            grade = quality
                            certification = certificate
                        }
                        WorkInfo.State.FAILED -> {
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                        else -> {
                        }
                    }
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
        binding.tvReceivingValue.text = ""
        binding.tvTruckNoValue.setText("")
        binding.tvDriverNameValue.setText("")
        binding.tvDriverNoValue.setText("")
        when (fragment) {
            is VegaCoffeeMtnrConsignmentFragment -> binding.tvVendorValue.setText("")
        }
    }

    private fun validateProceed() {
        if (batchList.isNotEmpty()) {
            if (validateEmptyWeight()) {
                if (validateLotWeight()) {
                    if (editLotId.isEmpty()) showRemarkDialog() else {
                        batchList.forEach {
                            it.delivery = vm.vegaCoffeeReceivingData.delivery
                            vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, it)
                        }
                        callBack?.replaceFragment(MTNR_WEIGHSCALE_SUMMARY, vm.vegaCoffeeReceivingData)
                    }
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
            vm.vegaCoffeeReceivingData.transportVendorCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_trans_vendor))
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
                    && binding.tvTruckNoValue.text.toString().isNotEmpty() && binding.tvVendorValue.text.toString()
                .isNotEmpty()
                    && binding.tvReceivingValue.text.isNotEmpty())
        if (enable) {
            binding.btProceed.setBackgroundColor(getColor(if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
            binding.btSave.setBackgroundColor(getColor(if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
            binding.btSave.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btProceed.isEnabled = enable
        binding.btSave.isEnabled = enable
    }

    fun getBack() {
        editLotId = ""
        vm.getOBDDetails(vm.vegaCoffeeReceivingData.delivery)
    }

    fun editLot(vegaCoffeeReceiveLots: VegaCoffeeReceiveLots) {
        editLotId = vegaCoffeeReceiveLots.batch
        vm.getOBDDetails(vegaCoffeeReceiveLots.mtnNumber)
    }

    private fun updateWeighbridgeValueUI(response: Resource<GenericReqAndResp<VegaReceiving>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                binding.tvMaterialValue.text =
                    response.data?.data?.materialCode.plus("-")
                        .plus(response.data?.data?.materialName)
                var driverName =
                    if (response.data?.data?.driverName.isNullOrEmpty()) response.data?.data?.truckDriverName else response.data?.data?.driverName
                binding.tvDriverNameValue.setText(driverName, TextView.BufferType.EDITABLE)
                binding.tvDriverNoValue.setText(
                    response.data?.data?.contactNumber,
                    TextView.BufferType.EDITABLE
                )
                binding.tvTruckNoValue.setText(response.data?.data?.vehicleNumber)
                if (currentKey.split("_")[1].contains("NI")) {
                    binding.tvWhValue.text =
                        storagePlantId.plus("-").plus(response.data?.data?.dstorageLocationCode)
                            .plus("-")
                            .plus(response.data?.data?.dstorageLocationName)
                } else
                    binding.tvWhValue.text =
                        response.data?.data?.dstorageLocationCode.plus("-")
                            .plus(response.data?.data?.dstorageLocationName)
                if (offloadData.imageString == WEIGHBRIDGE_WEIHSCALE && offloadData.weighBridgeType == STO)
                    binding.tvReceivingValue.text =
                        response.data?.data?.dstorageLocationCode.plus("-")
                            .plus(response.data?.data?.dstorageLocationName)
                offloadData.vehicleNumber = response.data?.data?.vehicleNumber
                offloadData.driverName = response.data?.data?.driverName
                offloadData.contactNumber = response.data?.data?.contactNumber
                offloadData.storageLocationCode = response.data?.data?.storageLocationCode
                offloadData.storageLocationName =
                    getLocationName(response.data?.data?.storageLocationCode)
                offloadData.materialCode = response.data?.data?.materialCode
                offloadData.materialName = response.data?.data?.materialName
                offloadData.bagCount = response.data?.data?.bagCount
                vm.vegaCoffeeReceivingData.materialCode = offloadData.materialCode
                vm.vegaCoffeeReceivingData.materialName = offloadData.materialName
                offloadData.dstorageLocationName = response.data?.data?.dstorageLocationName
                offloadData.dstorageLocationCode = response.data?.data?.dstorageLocationCode
                offloadData.location = response.data?.data?.plantId
                vm.vegaCoffeeReceivingData.dstorageLocationCode =
                    response.data?.data?.dstorageLocationCode
                vm.vegaCoffeeReceivingData.dstorageLocationName =
                    response.data?.data?.dstorageLocationName
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
