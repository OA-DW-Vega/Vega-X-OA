package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.mtnr.weighbridge

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingWarehouseWithMtns
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.R
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.databinding.FragmentGhanaCocoaMtnrConsignmentLayoutBinding
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.databinding.LayoutGhanaCocoaRejectDetailsBinding
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.VegaGhanaCocoaOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.VegaGhanaCocoaOffloadingViewModel
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.JSON_REJECTED_MAPPING_LIST
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.MTNR_WEIGHBRIDGE_SUMMARY
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.WB_DATA
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.getColor
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGhanaCocoaMtnrWbConsignmentFragment : BaseFragment(), VegaSingleSelectListener {

    override val layoutResourceId = R.layout.fragment_ghana_cocoa_mtnr_consignment_layout
    private lateinit var binding: FragmentGhanaCocoaMtnrConsignmentLayoutBinding
    private var callBack: VegaGhanaCocoaOffloadReplaceFragmentCallback? = null
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private lateinit var moreWeightBatches: String
    private var addWeightPosition: Int = 0
    private val vm: VegaGhanaCocoaOffloadingViewModel by viewModel()
    private var wareHouseList: MutableList<VegaSupplyStorageLocation> = mutableListOf()
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
    private var vendorName: String = ""
    private var stName: String = ""
    private var sendingStCode: String = ""
    private var sendingStName: String = ""
    private var wbDetails = VegaQualityWBDetails()
    private var list = VegaCoffeeReceiveLots()
    private var replace: CallBack? = null
    private var totalRejectedBags: Long = 0L

    private var pendingRejectReasonBagCount: Long = 0L
    private var isPresent: Boolean = false
    private var rejectList = HashMap<String, String>()
    private var jsonData = mutableListOf<String>()
    private var counter: Int = 0
    private var rejectionMappingList = ArrayList<String>()
    private var rejectionRepeatedList = ArrayList<String>()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaGhanaCocoaOffloadReplaceFragmentCallback
        replace = context as? CallBack
    }

    interface CallBack {
        fun replaceFragment(fragment: String, flag: VegaCoffeeReceiving, wbDetails: VegaQualityWBDetails)
        fun navigateToSummary(fragment: String, flag: VegaCoffeeReceiving, wbDetails: VegaQualityWBDetails)
    }

    companion object {
        fun newInstance(wbDetails: VegaQualityWBDetails?) = VegaGhanaCocoaMtnrWbConsignmentFragment()
            .putArgs {
                putParcelable(WB_DATA, wbDetails)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGhanaCocoaMtnrConsignmentLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingsesame/ui/mtnr/VegaSesameMtnrTypeSelectFragment").title("Mtnr Coffee")
            .with(tracker)
    }

    private fun initUI() {
        // binding.tvTitle.text = "MTNR WeighBridge"
        wbDetails = arguments?.getParcelable(WB_DATA)!!

        binding.tvWeighBridgeId.text = wbDetails.vehicleNumber
        binding.tvBagCount.text = wbDetails.bagCount
        binding.tvSupplierName.text = wbDetails.plant
        binding.tvWeight.text = wbDetails.delivery
        binding.tvProcureType.text = wbDetails.materialName
        binding.tvTitle.text = getString(R.string.mtnr_weighscale)

        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer { updateProcessType(it) })
        vm.getProcessTypeList(getCurrentKey())

        binding.tvstoValue.onChange {
            if (it.length > 0) {
                totalRejectedBags = it.toLong()
                if (totalRejectedBags > 0L && !isPresent) {
                    isPresent = true
                    handleRejectReason()
                } else if (totalRejectedBags == 0L) {
                    totalRejectedBags = 0L
                    isPresent = false
                    handleRejectReason()
                }
            } else {
                totalRejectedBags = 0L
                isPresent = false
                handleRejectReason()
            }

        }

//        updateMandatory()
        enableProceed()
        binding.btSave.isVisible = false

        vm.weighBridgeId.observe(viewLifecycleOwner, Observer {
            //updateTruckDetail(it.data?.data)
        })

        vm.location.observe(viewLifecycleOwner, Observer { wareHouseList = it.toMutableList() })

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
//            customLocationList = it
//                .toMutableList()
//            if (customLocationList.size == 1) {
//                customLocationList.forEach {
//                    binding.tvReceivingValue.text = customLocationList[0].procureLocationCode.plus(" - ").plus(
//                        customLocationList[0].procureLocationName
//                    )
//                    vm.vegaCoffeeReceivingData.storageLocationCode = customLocationList[0].procureLocationCode
//                    vm.vegaCoffeeReceivingData.storageLocationName = customLocationList[0].procureLocationName
//                    selectedReceivingLocation =
//                        customLocationList.single { it.procureLocationCode == customLocationList[0].procureLocationCode }
//                    vm.vegaCoffeeReceivingData.materialCode = customLocationList[0].plant
//
//                }
//            }
        })
        vm.getCustomLocations()
        vm.getSuppliers()

//        vm.suppplier.observe(viewLifecycleOwner, Observer {
//            supplierList = it/*.filter { data -> data.bcApprover?.isNotEmpty()!! }*/.toMutableList()
//
//            val supplierData = supplierList/*.filter { data -> data.vendorCode.startsWith("2", true) }*/
//            val suppliers = supplierData.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
//            val productAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suppliers)
//            binding.tvVendorValue.threshold = 1
//            binding.tvVendorValue.setAdapter(productAdapter)
//            binding.tvVendorValue.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
//                enableProceed()
//                it.forEach { material ->
//                    if (material.vendorCode == binding.tvVendorValue.text.toString().split(" - ")[0]) {
//                        vm.vegaCoffeeReceivingData.transportVendorCode = material.vendorCode
//                        vm.vegaCoffeeReceivingData.transportVendorName = material.vendorName
//                    }
//                }
//            }
//        })

        enableProceed()
//        binding.tvDriverNoValue.onChange { enableProceed() }
//        binding.tvDriverNameValue.onChange { enableProceed() }
//        binding.tvTruckNoValue.onChange { enableProceed() }
        binding.btProceed.setOnClickListener {
            var rejectedValue = 0L
            var accepteddValue = 0L
            if (binding.tvstoValue.text.toString().isNullOrEmpty()) {
                rejectedValue = 0L
            } else {
                rejectedValue = binding.tvstoValue.text.toString().toLong()
            }
            if (binding.tvWhValue.text.toString().isNullOrEmpty()) {
                accepteddValue = 0L
            } else {
                accepteddValue = binding.tvWhValue.text.toString().toLong()
            }
            var sum = (accepteddValue + rejectedValue)
            if (binding.tvWhValue.text.length == 0 || binding.tvstoValue.text.length == 0) {
                Toast.makeText(
                    context,
                    "Please enter accepted bag and rejected bag count",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!(sum == wbDetails.bagCount.toString().toDouble().toLong())) {
                Toast.makeText(
                    context,
                    "Please enter accepted and rejected bags to match the total count",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!binding.tvstoValue.text.toString().toLong()
                    .equals(0L) && binding.tvstoValue.text.toString()
                    .isNotEmpty() && (rejectList.size == 0 || rejectList.values.size == 0)
            )
                Toast.makeText(
                    context,
                    "Please enter the reason and its mapped count",
                    Toast.LENGTH_SHORT
                ).show()
            else if ((rejectList.size > 0 || rejectList.values.size > 0) && (!(pendingRejectReasonBagCount.equals(
                    rejectedValue
                )))
            )
                Toast.makeText(
                    context,
                    "Please enter the correct rejected count for the mapped reason",
                    Toast.LENGTH_SHORT
                ).show()
            else if ((rejectList.values.size == (counter + 1) || binding.tvstoValue.text.toString()
                    .toLong().equals(0L))
            )
                moveToSummary(rejectedValue)
            else
                Toast.makeText(context, "Please enter reason and its count", Toast.LENGTH_SHORT)
                    .show()
        }
//        binding.btSave.setOnClickListener {
//            vm.vegaCoffeeReceivingData.vehicleNumber = binding.tvTruckNoValue.text.toString()
//            vm.vegaCoffeeReceivingData.driverName = binding.tvDriverNameValue.text.toString()
//            vm.vegaCoffeeReceivingData.contactNumber = binding.tvDriverNoValue.text.toString()
//            vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, VegaCoffeeReceiveLots())
//            activity?.finish()
//        }
        vm.offloadingMtnr.observe(viewLifecycleOwner, Observer { updateOBDDetails(it) })
        fetchMtnDetails()
    }

    private fun updateProcessType(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        val gson = Gson()
        jsonData.forEach {
            if (it.contains(JSON_REJECTED_MAPPING_LIST)) {
                var rejectedReason =
                    (JSONObject(it).getJSONArray(JSON_REJECTED_MAPPING_LIST).get(0)).toString().split(",")
                rejectedReason.forEach { it1 ->
                    rejectionMappingList.add(
                        ((it1.split(":")[0]).replace("{", "").replace("\"", "")).plus(" - ")
                            .plus((it1.split(":")[1]).replace("}", "").replace("\"", ""))
                    )
                }
            }
        }
    }

    private fun handleRejectReason() {
        if (totalRejectedBags > 0) {
//            var newView = layoutInflater.inflate(R.layout.layout_ghana_cocoa_reject_details, binding.llReject, false)
            var newView = LayoutGhanaCocoaRejectDetailsBinding.inflate(
                LayoutInflater.from(binding.llReject.context),
                binding.llReject,
                false
            )

            newView.tvReason.tag = counter
            newView.tvRejectReasonCount.tag = counter
            newView.tvReasonLabel.text = getString(R.string.reason_text).plus(" ").plus(counter + 1)

            newView.tvReason.isEnabled = true
            var containerNumberList = ArrayList<String>()
            containerNumberList.add(getString(R.string.select_reason))
            containerNumberList.addAll(rejectionMappingList)
            val stageAdapter =
                ArrayAdapter(requireContext(), R.layout.item_vega_ghana_cocoa_reason, containerNumberList)
            stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
            newView.tvReason.adapter = stageAdapter
            newView.tvReason.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {/*Nothing is selected*/}
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    if (position > 0) {
                        if (!rejectList.values.contains(containerNumberList[position])) {
                            rejectList.put(
                                newView.tvReason.tag.toString(),
                                containerNumberList[position]
                            )

                            var reason = rejectList.values
                            pendingRejectReasonBagCount = 0L
                            reason.forEach {
                                if (it.contains(":")) {
                                    pendingRejectReasonBagCount =
                                        it.split(":")[1].toLong() + pendingRejectReasonBagCount
                                } else {
                                    newView.tvRejectReasonCount.setText("")
                                }
                            }


                        } else {
                            Toast.makeText(context, "Please choose a different Reason", Toast.LENGTH_SHORT).show()
                            newView.tvReason.setSelection(0)
                        }
                    }
//                    newView.tvReason.setSelection(0)
                }
            }

            newView.tvRejectReasonCount.onChange {
//                if (newView.tvReason.selectedItemPosition != 0) {
                if (it.length > 0) {

                    if (rejectList.size > 0) {
                        if (rejectList.containsKey(newView.tvRejectReasonCount.tag.toString())) {
                            var val2 = rejectList.getValue(newView.tvRejectReasonCount.tag.toString()).split(":")[0]
                            rejectList[newView.tvRejectReasonCount.tag.toString()] =
                                val2.plus(":").plus(newView.tvRejectReasonCount.text.toString())
                        } else {
                            newView.tvRejectReasonCount.error =
                                "Please select Reason and then enter reason Count"
                        }
                    }

                    var reason = rejectList.values
                    pendingRejectReasonBagCount = 0L
                    reason.forEach {
                        if (it.contains(":")) {
                            pendingRejectReasonBagCount =
                                it.split(":")[1].toLong() + pendingRejectReasonBagCount
                        }
                    }
                    if (it.toLong() > totalRejectedBags)
                        newView.tvRejectReasonCount.setError(
                            getString(R.string.error_mesage_reject_count),
                            null
                        )
                    else if (pendingRejectReasonBagCount > totalRejectedBags) {
                        newView.tvAddRejectReason.visibility = View.GONE
                        newView.tvRejectReasonCount.error = "Please enter lesser count"
                    } else if (it.toLong() == totalRejectedBags) {
                        newView.tvAddRejectReason.visibility = View.GONE
                    } else if (pendingRejectReasonBagCount == totalRejectedBags) {
                        newView.tvAddRejectReason.visibility = View.GONE
                    } else if (it.toLong() > pendingRejectReasonBagCount) {
                        newView.tvAddRejectReason.setError("Please enter less than total reject bags", null)
                        newView.tvAddRejectReason.visibility = View.GONE
                    } else {
//                        if(rejectList[newView.tvReason.tag.toString()]?.split(":")?.get(1)?.length ?: 0 > 0)
//                            newView.tvAddRejectReason.visibility = View.GONE
//                        else

                        if (newView.tvReason.tag.toString().toInt() == (counter))
                            newView.tvAddRejectReason.visibility = View.VISIBLE
                    }
                }
//                }
//            else {
//                    Toast.makeText(context,"Please select Reason",Toast.LENGTH_SHORT).show()
////                    binding.llReject.tvReason.setError("Please select Reason", null)
//                }
            }
            newView.tvAddRejectReason.setOnClickListener {
                newView.tvAddRejectReason.visibility = View.GONE
                counter += 1
//                var reason = rejectList.values
//                println("Roshna reason => $reason")
//                reason.forEach{
//                    pendingRejectReasonBagCount += it.split(":")[1].toLong()
//                }
//                        pendingRejectReasonBagCount = totalRejectedBags - (reason.sum())
//                println("Roshna pending => $pendingRejectReasonBagCount")
                handleRejectReason()
            }

            binding.llReject.addView(newView.root)

        } else {
            counter = 0
            binding.llReject.removeAllViewsInLayout()
        }
    }


    private fun moveToSummary(rejectedValue: Long) {
//        binding.tvVendorValue.isCursorVisible = false
//        vm.vegaCoffeeReceivingData.vehicleNumber = binding.tvTruckNoValue.text.toString()
//        vm.vegaCoffeeReceivingData.driverName = binding.tvDriverNameValue.text.toString()
//        vm.vegaCoffeeReceivingData.contactNumber = binding.tvDriverNoValue.text.toString()
        rejectionRepeatedList.clear()
        rejectList.values.forEach {
            if (it.contains("-") && !rejectionRepeatedList.contains(it.split(" - ")[0])) {
                    rejectionRepeatedList.add(it.split(" - ")[0])
            }
        }
        if (rejectionRepeatedList.size == rejectList.values.size && pendingRejectReasonBagCount.equals(
                rejectedValue
            )
        ) {
            callBack?.replaceFragment(
                MTNR_WEIGHBRIDGE_SUMMARY,
                prepareOffloadingData(),
                wbDetails,
                rejectList,
                binding.tvWhValue.text.toString(),
                batchList
            )
        } else {
            Toast.makeText(context, "Please choose the different reason", Toast.LENGTH_SHORT).show()
        }

    }

    private fun prepareOffloadingData(): VegaCoffeeReceiving {

        var data = VegaCoffeeReceiving()

        data.bagCount = wbDetails.bagCount
        data.bagType = wbDetails.bagType
        data.bagWeight = wbDetails.bagWeight
        data.delivery = wbDetails.delivery.toString()
        data.deliveryItem = wbDetails.deliveryItem.toString()
        data.materialCode = wbDetails.materialCode
        data.materialName = wbDetails.materialName
        data.netWeight = wbDetails.netWeight.toString()
        data.storageLocationCode = wbDetails.storageLocationCode
        data.storageLocationName = wbDetails.storageLocation
        data.unitsOfMeasure = wbDetails.unitsOfMeasure.toString()
        data.purchaseDocNum = wbDetails.purchaseDocNum.toString()
        data.purchaseDocDesc = wbDetails.purchaseDocDesc.toString()
        data.supplierCode = wbDetails.supplierCode.toString()
        data.supplierName = wbDetails.supplierName.toString()
        data.vehicleNumber = wbDetails.vehicleNumber.toString()
        data.challan = wbDetails.challan.toString()
        return data
    }


    /*private fun updateTruckDetail(response: VegaReceiving?) {
//        if (!response?.vehicleNumber.isNullOrEmpty()) {
//            binding.tvTruckNoValue.setText(response?.vehicleNumber)
//            binding.tvTruckNoValue.isEnabled = true
//        } else binding.tvTruckNoValue.isEnabled = true
//        if (!response?.driverName.isNullOrEmpty()) {
//            binding.tvDriverNameValue.setText(response?.driverName)
//            binding.tvDriverNameValue.isEnabled = true
//        } else binding.tvDriverNameValue.isEnabled = true
//        if (!response?.contactNumber.isNullOrEmpty()) {
//            binding.tvDriverNoValue.setText(response?.contactNumber)
//            binding.tvDriverNoValue.isEnabled = true
//        } else binding.tvDriverNoValue.isEnabled = true
    }*/

    private fun updateOBDDetails(data: VegaCoffeeReceivingMtnrWithLots?) {
        val fragment = activity?.supportFragmentManager?.findFragmentById(R.id.flContainer)
        setValueEmpty(fragment)
        if (data != null) {

            if (data.receiving.transportVendorCode?.isNotEmpty()!!) {
                when (fragment) {
                    is VegaGhanaCocoaMtnrWbConsignmentFragment -> {
//                        binding.tvVendorValue.setText(
//                            data.receiving.transportVendorCode.plus(" - ").plus(data.receiving.transportVendorName)
//                        )
                    }
                }
            } else
//                binding.tvVendorValue.setText("")
//            binding.tvTruckNoValue.setText(data.receiving.vehicleNumber)
//            binding.tvDriverNameValue.setText(data.receiving.driverName)
//            binding.tvDriverNoValue.setText(data.receiving.contactNumber)
                vm.vegaCoffeeReceivingData = data.receiving
            if (data.lineItems.size > 0) {
                batchList.forEach {
                    var edWeight = 0.0
                    val bags = data.lineItems.filter { it2 -> it2.lots.mtnNumber.equals(it.mtnNumber) }
                        .filter { it1 -> it1.lots.batch.equals(it.batch) }
                    if (bags.size == 1 && vm.vegaCoffeeReceivingData.startTime?.isEmpty() == true) {
                        vm.vegaCoffeeReceivingData.startTime = DateUtils.getCurrentTimeInMills().toString()
                    }
                    if (bags.size > 0) edWeight = bags[0].bagItem.filter { it2 -> it2.mtnNumber.equals(it.mtnNumber) }
                        .filter { it1 -> it1.batchNumber.equals(it.batch) }
                        .sumByDouble { it2 -> it2.netWeight.toDouble() }
                    it.editedWeight = edWeight.toString()
                }
               // setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
            } else {
                //setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
            }
        } else {
            vm.getWeighBridgeIdDetail(wbDetails.weighBridgeId)
            //setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
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
                Resource.Status.ERROR -> showErrorDialogWithFAQLink(requireContext(), it.error.toString())
            }
        }
    }

    private fun saveResult(data: GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>?) {
        hideLoading()
        data?.data?.let {
            wareHouseList = data.data.storageLocationLst.distinct().toMutableList()
            allOBDList = data.data.mtns.toMutableList()
            allBatchList = data.data.batchDetails.toMutableList()
//            allBatchList.forEachIndexed { index, s ->
////                allBatchList[index].weight = convertMtToKg(allBatchList[index].weight!!)
////            }

            allOBDList.forEach {
                if (it.mtnNumber == wbDetails.delivery)
                    sendingStCode = it.storageLocationCode
            }

            wareHouseList.forEach {
                if (it.storageLocationCode == sendingStCode)
                    sendingStName = it.storageLocationName!!
                vm.vegaCoffeeReceivingData.plantId = it.plant
            }
//            binding.tvWhValue.text = sendingStCode.plus(" - ").plus(sendingStName)

            vm.vegaCoffeeReceivingData.supplierCode = sendingStCode
            vm.vegaCoffeeReceivingData.supplierName = sendingStName

            batchList = allBatchList.filter { it.mtnNumber == wbDetails.delivery }.toMutableList()
            vm.vegaCoffeeReceivingData.purchaseDocNum =
                if (batchList.size > 0) batchList[0].purchaseOrder else selectedOBD.purchaseOrder
            vm.vegaCoffeeReceivingData.purchaseDocDesc =
                if (batchList.size > 0) batchList[0].ebelp else selectedOBD.ebelp
            vm.getOBDDetails(wbDetails.delivery!!)
        }
    }

   /* private fun showSingleSelectDialog(isWh: Boolean, title: String, isVendor: Boolean) {
        val list: List<String>
        if (isVendor) {
            list = customLocationList.map {
                it.procureLocationCode.plus(" - ").plus(it.procureLocationName)
            } as ArrayList<String>

        } else if (isWh) {
            list = wareHouseList.map {
                it.storageLocationCode.plus(" - ").plus(it.storageLocationName)
            } as ArrayList<String>
        } else {
            list = filteredOBDList.map { it.mtnNumber }.toSet().toMutableList()
        }
        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isWh, isVendor, false,
                list,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }*/

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {
        customDialog?.dismiss()
        enableProceed()
        if (isWh) {
            val whId = data.split("-")
//            binding.tvWhValue.text = data
            wareHouseList.forEach {
                if (it.storageLocationCode == whId[0].trim() && it.storageLocationName == whId[1].trim()) {
                    vm.vegaCoffeeReceivingData.plantId = it.plant
                }
            }
            vm.vegaCoffeeReceivingData.supplierCode = whId[0].trim()
            vm.vegaCoffeeReceivingData.supplierName = whId[1].trim()
            selectedSendingLocation = wareHouseList.single {
                it.storageLocationName == whId[1].trim()
            }
            filteredOBDList =
                allOBDList.filter { it.storageLocationCode == selectedSendingLocation.storageLocationCode && it.supplyingPlantId == selectedSendingLocation.plant }
                    .toMutableList()
        } else if (!isVendor) {
//            binding.tvstoValue.text = data
            vm.vegaCoffeeReceivingData.mtnCode = data
            vm.vegaCoffeeReceivingData.delivery = data
            selectedOBD = filteredOBDList.filter { it.mtnNumber == data }[0]
            batchList = allBatchList.filter { it.mtnNumber == selectedOBD.mtnNumber }.toMutableList()
            vm.vegaCoffeeReceivingData.purchaseDocNum =
                if (batchList.size > 0) batchList[0].purchaseOrder else selectedOBD.purchaseOrder
            vm.vegaCoffeeReceivingData.purchaseDocDesc =
                if (batchList.size > 0) batchList[0].ebelp else selectedOBD.ebelp
            vm.getOBDDetails(data)
        } else {
            val whId = data.split("-")
//            binding.tvReceivingValue.text = data
            vm.vegaCoffeeReceivingData.storageLocationCode = whId[0].trim()
            vm.vegaCoffeeReceivingData.storageLocationName = whId[1].trim()
            selectedReceivingLocation = customLocationList.single { it.procureLocationCode == whId[0].trim() }
            customLocationList.forEach {
                if (it.procureLocationCode == whId[0].trim() && it.procureLocationName == whId[1].trim()) {
                    vm.vegaCoffeeReceivingData.materialCode = it.plant
                }
            }
        }
    }

    /*private fun updateTruckDetails(isUpdate: Boolean) {
        if (isUpdate) {
            *//*binding.tvTruckNoValue.setText()
              binding.tvDriverNameValue.setText()
              binding.tvVendorValue.text = *//*
        } else {
//            binding.tvTruckNoValue.setText("")
//            binding.tvDriverNameValue.setText("")
//            binding.tvVendorValue.setText("")
        }
    }*/

    /*private fun setUpAdapter(list: ArrayList<VegaCoffeeReceiveLots>) {
//        var lineItems = mutableListOf<VegaCoffeeReceiveLots>()
//        if (editLotId.isNotEmpty()) {
//            enableDisableItem(false)
//            lineItems = list.filter { it.batch.equals(editLotId) } as MutableList<VegaCoffeeReceiveLots>
//        } else {
//            enableDisableItem(true)
//            lineItems = list
//        }
//        binding.rvList.setUp(lineItems, R.layout.item_ghana_cocoa_mtnr_weighscale_lot_card_layout, { item, pos ->
//            ll_weight_loss.gone()
//            tvScaleLotValue.text = item.batch
//            tvStLocationValue.text = item.storageLocationCode
//            tvScaleWeightValue.text =
//                item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus("MT")
//            tvScaleGradeValue.text = item.materialName
//            var receivingKg = item.editedWeight
//            val editedWeight = if (receivingKg.isNullOrEmpty()) "0.0" else receivingKg.toString().toDouble()
//                .formatThreeDigits()
//            tvScaleDispatchValue.text = wbDetails.netWeight
//            tvDispatchUOMValue.text = "MT"
//            ivEdit.visibility = View.GONE
//            tv_add_weight.visibility = View.GONE
//            item.editedWeight = wbDetails.netWeight
//            item.delivery = item.mtnNumber
////            vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, item)
////            tv_add_weight.setOnClickListener {
////                binding.tvVendorValue.isCursorVisible = false
////                addWeightPosition = pos
////                vm.vegaCoffeeReceivingData.vehicleNumber = binding.tvTruckNoValue.text.toString()
////                vm.vegaCoffeeReceivingData.driverName = binding.tvDriverNameValue.text.toString()
////                vm.vegaCoffeeReceivingData.contactNumber = binding.tvDriverNoValue.text.toString()
////                item.delivery = item.mtnNumber
////                vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, item)
////                callBack?.replaceFragment(ADD_WEIGHT, item)
////            }
//        })
//
//        updateUI()
    }*/

   /* private fun updateUI() {
        customLocationList.forEach {
            if (it.procureLocationCode == wbDetails.storageLocationCode) {
                stName = it.procureLocationName!!
                vm.vegaCoffeeReceivingData.storageLocationCode = it.procureLocationCode
                vm.vegaCoffeeReceivingData.storageLocationName = it.procureLocationName
                vm.vegaCoffeeReceivingData.materialCode = it.plant

            }
        }
//        binding.tvReceivingValue.text = wbDetails.storageLocationCode.plus(" - ").plus(stName)
//        binding.tvstoValue.text = wbDetails.delivery
        vm.vegaCoffeeReceivingData.mtnCode = wbDetails.delivery
        vm.vegaCoffeeReceivingData.delivery = wbDetails.delivery!!
//        binding.tvTruckNoValue.setText(wbDetails.vehicleNumber)
//        if (binding.tvTruckNoValue.text.toString() == "")
//            binding.tvTruckNoValue.isEnabled = true
//        binding.tvDriverNameValue.setText(wbDetails.driverName)
//        if (binding.tvDriverNameValue.text.toString() == "")
//            binding.tvDriverNameValue.isEnabled = true
//        binding.tvDriverNoValue.setText(wbDetails.contactNumber)
//        if (binding.tvDriverNoValue.text.toString() == "")
//            binding.tvDriverNoValue.isEnabled = true
    }*/

    /*private fun enableDisableItem(flag: Boolean) {
        binding.tvWhValue.isEnabled = flag
        binding.tvstoValue.isEnabled = flag
//        binding.tvReceivingValue.isEnabled = flag
//        binding.tvVendorValue.isEnabled = flag
//        binding.tvTruckNoValue.isEnabled = flag
//        binding.tvDriverNameValue.isEnabled = flag
//        binding.tvDriverNoValue.isEnabled = flag
    }*/

    private fun setValueEmpty(fragment: Fragment?) {
//        binding.tvTruckNoValue.setText("")
//        binding.tvDriverNameValue.setText("")
//        binding.tvDriverNoValue.setText("")
        when (fragment) {
//            is VegaGhanaCocoaMtnrWbConsignmentFragment -> binding.tvVendorValue.setText("")
        }
    }

    private fun enableProceed() {
        val enable = true
//            (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverName.text.isNotEmpty()
//                    && binding.tvTruckNoValue.text.toString().isNotEmpty()
//                    && binding.tvReceivingValue.text.isNotEmpty())
        if (enable) {
            binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            binding.btSave.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
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

   /* fun editLot(vegaCoffeeReceiveLots: VegaCoffeeReceiveLots) {
        editLotId = vegaCoffeeReceiveLots.batch
        vm.getOBDDetails(vegaCoffeeReceiveLots.mtnNumber)
    }

    private fun updateMandatory() {
        binding.tvDest.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.accepted_bags)) { mandatoryStars() } }
        binding.tvsto.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.rejected_bags)) { mandatoryStars() } }
//        binding.tvReceivingWH.text =
//            with(UIUtils) { with(requireContext().resources.getString(R.string.receiving_locations)) { mandatoryStars() } }
//        binding.tvTruckNo.text =
//            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_number)) { mandatoryStars() } }
//        binding.tvDriverName.text =
//            with(UIUtils) { with(requireContext().resources.getString(R.string.driver_name)) { mandatoryStars() } }
    }*/
}
