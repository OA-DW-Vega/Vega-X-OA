package com.olam.warehouse.vegax.offloadingghana.ui.mtnr.weighbridge

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
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
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
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingghana.R
import com.olam.warehouse.vegax.offloadingghana.databinding.FragmentGhanaMtnrConsignmentLayoutBinding
import com.olam.warehouse.vegax.offloadingghana.ui.VegaGhanaOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingghana.ui.VegaGhanaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingghana.utils.MTNR_WEIGHSCALE_OFFLINE_SUMMARY
import com.olam.warehouse.vegax.offloadingghana.utils.MTNR_WEIGHSCALE_SUMMARY
import com.olam.warehouse.vegax.offloadingghana.utils.WB_DATA
import com.olam.warehouse.vegax.offloadingghana.utils.getColor
import kotlinx.android.synthetic.main.item_ghana_mtnr_weighscale_lot_card_layout.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.concurrent.TimeUnit

class VegaGhanaMtnrWbConsignmentFragment : BaseFragment(), VegaSingleSelectListener {

    override val layoutResourceId = R.layout.fragment_ghana_mtnr_consignment_layout
    private lateinit var binding: FragmentGhanaMtnrConsignmentLayoutBinding
    private var callBack: VegaGhanaOffloadReplaceFragmentCallback? = null
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private lateinit var moreWeightBatches: String
    private var addWeightPosition: Int = 0
    private val vm: VegaGhanaOffloadingViewModel by viewModel()
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaGhanaOffloadReplaceFragmentCallback
        replace = context as? CallBack
    }

    interface CallBack {
        fun replaceFragment(fragment: String, flag: VegaCoffeeReceiving, wbDetails: VegaQualityWBDetails)
        fun navigateToSummary(fragment: String, flag: VegaCoffeeReceiving, wbDetails: VegaQualityWBDetails)
    }

    companion object {
        fun newInstance(wbDetails: VegaQualityWBDetails?) = VegaGhanaMtnrWbConsignmentFragment()
            .putArgs {
                putParcelable(WB_DATA, wbDetails)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGhanaMtnrConsignmentLayoutBinding.inflate(layoutInflater)
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
        binding.tvTitle.text = "MTNR WeighBridge"
        wbDetails = arguments?.getParcelable(WB_DATA)!!
//        updateMandatory()
        enableProceed()
        binding.btSave.isVisible = false


        vm.weighBridgeId.observe(viewLifecycleOwner, Observer {
            updateTruckDetail(it.data?.data)
        })

        vm.location.observe(viewLifecycleOwner, Observer { wareHouseList = it.toMutableList() })

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            customLocationList = it
                .filter { it.storageLocationType.equals("P") }
                .toMutableList()
                if(customLocationList.size == 1){
                    customLocationList.forEach {
                        binding.tvReceivingValue.text = customLocationList[0].procureLocationCode.plus(" - ").plus(
                            customLocationList[0].procureLocationName
                        )
                        vm.vegaCoffeeReceivingData.storageLocationCode = customLocationList[0].procureLocationCode
                        vm.vegaCoffeeReceivingData.storageLocationName = customLocationList[0].procureLocationName
                        selectedReceivingLocation =
                            customLocationList.single { it.procureLocationCode == customLocationList[0].procureLocationCode }
                        vm.vegaCoffeeReceivingData.materialCode = customLocationList[0].plant

                    }
            }
        })
        vm.getCustomLocations()
        vm.getSuppliers()

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

        enableProceed()
        binding.tvDriverNoValue.onChange { enableProceed() }
        binding.tvDriverNameValue.onChange { enableProceed() }
        binding.tvTruckNoValue.onChange { enableProceed() }
        binding.btProceed.setOnClickListener {
            vm.vegaCoffeeReceivingData.delivery = binding.tvstoValue.text.toString()
            vm.vegaCoffeeReceivingData.vehicleNumber = binding.tvTruckNoValue.text.toString()
            vm.vegaCoffeeReceivingData.driverName = binding.tvDriverNameValue.text.toString()
            vm.vegaCoffeeReceivingData.contactNumber = binding.tvDriverNoValue.text.toString()
            batchList.forEach {
                vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, it)
            }
            vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, list)
            moveToSummary()
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

    private fun moveToSummary() {
        binding.tvVendorValue.isCursorVisible = false
        vm.vegaCoffeeReceivingData.vehicleNumber = binding.tvTruckNoValue.text.toString()
        vm.vegaCoffeeReceivingData.driverName = binding.tvDriverNameValue.text.toString()
        vm.vegaCoffeeReceivingData.contactNumber = binding.tvDriverNoValue.text.toString()
        validateInputs()

    }

    private fun updateTruckDetail(response: VegaReceiving?) {
        if (!response?.vehicleNumber.isNullOrEmpty()) {
            binding.tvTruckNoValue.setText(response?.vehicleNumber)
            binding.tvTruckNoValue.isEnabled = true
        } else binding.tvTruckNoValue.isEnabled = true
        if (!response?.driverName.isNullOrEmpty()) {
            binding.tvDriverNameValue.setText(response?.driverName)
            binding.tvDriverNameValue.isEnabled = true
        } else binding.tvDriverNameValue.isEnabled = true
        if (!response?.contactNumber.isNullOrEmpty()) {
            binding.tvDriverNoValue.setText(response?.contactNumber)
            binding.tvDriverNoValue.isEnabled = true
        } else binding.tvDriverNoValue.isEnabled = true
    }

    private fun updateOBDDetails(data: VegaCoffeeReceivingMtnrWithLots?) {
        val fragment = activity?.supportFragmentManager?.findFragmentById(R.id.flContainer)
        setValueEmpty(fragment)
        if (data != null) {

            if (data.receiving.transportVendorCode?.isNotEmpty()!!) {
                when (fragment) {
                    is VegaGhanaMtnrWbConsignmentFragment -> {
                        binding.tvVendorValue.setText(
                            data.receiving.transportVendorCode.plus(" - ").plus(data.receiving.transportVendorName)
                        )
                    }
                }
            } else
                binding.tvVendorValue.setText("")
            binding.tvTruckNoValue.setText(data.receiving.vehicleNumber)
            binding.tvDriverNameValue.setText(data.receiving.driverName)
            binding.tvDriverNoValue.setText(data.receiving.contactNumber)
            vm.vegaCoffeeReceivingData = data.receiving
            if (data.lineItems.size > 0) {
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
                setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
            } else {
                setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
            }
        } else {
            vm.getWeighBridgeIdDetail(wbDetails.weighBridgeId)
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
            binding.tvWhValue.text = sendingStCode.plus(" - ").plus(sendingStName)

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

    private fun showSingleSelectDialog(isWh: Boolean, title: String, isVendor: Boolean) {
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
                activity!!,
                this
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
            wareHouseList.forEach {
               if(it.storageLocationCode == whId[0].trim() && it.storageLocationName == whId[1].trim()) {
                   vm.vegaCoffeeReceivingData.plantId = it.plant
               }
            }
            vm.vegaCoffeeReceivingData.supplierCode = whId[0].trim()
            vm.vegaCoffeeReceivingData.supplierName = whId[1].trim()
            selectedSendingLocation = wareHouseList.single {
                it.storageLocationName == whId[1].trim()
            }
            filteredOBDList =
                allOBDList.filter { it.storageLocationCode == selectedSendingLocation.storageLocationCode && it.supplyingPlantId == selectedSendingLocation.plant}
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
            vm.getOBDDetails(data)
        } else {
            val whId = data.split("-")
            binding.tvReceivingValue.text = data
            vm.vegaCoffeeReceivingData.storageLocationCode = whId[0].trim()
            vm.vegaCoffeeReceivingData.storageLocationName = whId[1].trim()
            selectedReceivingLocation = customLocationList.single { it.procureLocationCode == whId[0].trim() }
            customLocationList.forEach {
                if(it.procureLocationCode == whId[0].trim() && it.procureLocationName == whId[1].trim()){
                    vm.vegaCoffeeReceivingData.materialCode = it.plant
                }
            }
        }
    }

    private fun updateTruckDetails(isUpdate: Boolean) {
        if (isUpdate) {
            /*binding.tvTruckNoValue.setText()
              binding.tvDriverNameValue.setText()
              binding.tvVendorValue.text = */
        } else {
            binding.tvTruckNoValue.setText("")
            binding.tvDriverNameValue.setText("")
            binding.tvVendorValue.setText("")
        }
    }

    private fun setUpAdapter(list: ArrayList<VegaCoffeeReceiveLots>) {
        var lineItems = mutableListOf<VegaCoffeeReceiveLots>()
        if (editLotId.isNotEmpty()) {
            enableDisableItem(false)
            lineItems = list.filter { it.batch.equals(editLotId) } as MutableList<VegaCoffeeReceiveLots>
        } else {
            enableDisableItem(true)
            lineItems = list
        }
        binding.rvList.setUp(lineItems, R.layout.item_ghana_mtnr_weighscale_lot_card_layout, { item, pos ->
            ll_weight_loss.gone()
            tvScaleLotValue.text = item.batch
            tvStLocationValue.text = item.storageLocationCode
            tvScaleWeightValue.text =
                item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus("MT")
            tvScaleGradeValue.text = item.materialName
            var receivingKg = item.editedWeight
            val editedWeight = if (receivingKg.isNullOrEmpty()) "0.0" else receivingKg.toString().toDouble()
                .formatThreeDigits()
            tvScaleDispatchValue.text = wbDetails.netWeight
            tvDispatchUOMValue.text = "MT"
            ivEdit.visibility = View.GONE
            tv_add_weight.visibility = View.GONE
            item.editedWeight = wbDetails.netWeight
            item.delivery = item.mtnNumber
//            vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, item)
//            tv_add_weight.setOnClickListener {
//                binding.tvVendorValue.isCursorVisible = false
//                addWeightPosition = pos
//                vm.vegaCoffeeReceivingData.vehicleNumber = binding.tvTruckNoValue.text.toString()
//                vm.vegaCoffeeReceivingData.driverName = binding.tvDriverNameValue.text.toString()
//                vm.vegaCoffeeReceivingData.contactNumber = binding.tvDriverNoValue.text.toString()
//                item.delivery = item.mtnNumber
//                vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, item)
//                callBack?.replaceFragment(ADD_WEIGHT, item)
//            }
        })

        updateUI()
    }

    private fun updateUI() {
        customLocationList.forEach {
            if (it.procureLocationCode == wbDetails.storageLocationCode) {
                stName = it.procureLocationName!!
                vm.vegaCoffeeReceivingData.storageLocationCode = it.procureLocationCode
                vm.vegaCoffeeReceivingData.storageLocationName = it.procureLocationName
                vm.vegaCoffeeReceivingData.materialCode = it.plant

            }
        }
        binding.tvReceivingValue.text = wbDetails.storageLocationCode.plus(" - ").plus(stName)
        binding.tvstoValue.text = wbDetails.delivery
        vm.vegaCoffeeReceivingData.mtnCode = wbDetails.delivery
        vm.vegaCoffeeReceivingData.delivery = wbDetails.delivery!!
        binding.tvTruckNoValue.setText(wbDetails.vehicleNumber)
        if (binding.tvTruckNoValue.text.toString() == "")
            binding.tvTruckNoValue.isEnabled = true
        binding.tvDriverNameValue.setText(wbDetails.driverName)
        if (binding.tvDriverNameValue.text.toString() == "")
            binding.tvDriverNameValue.isEnabled = true
        binding.tvDriverNoValue.setText(wbDetails.contactNumber)
        if (binding.tvDriverNoValue.text.toString() == "")
            binding.tvDriverNoValue.isEnabled = true
    }

    private fun enableDisableItem(flag: Boolean) {
        binding.tvWhValue.isEnabled = flag
        binding.tvstoValue.isEnabled = flag
        binding.tvReceivingValue.isEnabled = flag
        binding.tvVendorValue.isEnabled = flag
        binding.tvTruckNoValue.isEnabled = flag
        binding.tvDriverNameValue.isEnabled = flag
        binding.tvDriverNoValue.isEnabled = flag
    }

    private fun setValueEmpty(fragment: Fragment?) {
        binding.tvTruckNoValue.setText("")
        binding.tvDriverNameValue.setText("")
        binding.tvDriverNoValue.setText("")
        when (fragment) {
            is VegaGhanaMtnrWbConsignmentFragment -> binding.tvVendorValue.setText("")
        }
    }

    private fun validateProceed() {
//        showRemarkDialog()
        replace?.navigateToSummary(MTNR_WEIGHSCALE_OFFLINE_SUMMARY, vm.vegaCoffeeReceivingData,wbDetails)


//        if (batchList.isNotEmpty()) {
//            if (validateEmptyWeight()) {
//                if (validateLotWeight()) {
//                    if (editLotId.isEmpty()) showRemarkDialog() else {
//                        batchList.forEach {
//                            it.delivery = vm.vegaCoffeeReceivingData.delivery
//                            vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, it)
//                        }
//                        callBack?.replaceFragment(MTNR_WEIGHSCALE_SUMMARY, vm.vegaCoffeeReceivingData)
//                    }
//                } else Toast.makeText(
//                    activity,
//                    getString(R.string.lot_more_weight)
//                    ,
//                    Toast.LENGTH_SHORT
//                ).show()
//            } else Toast.makeText(
//                activity,
//                getString(R.string.less_weight_error),
//                Toast.LENGTH_SHORT
//            ).show()
//        } else {
//            Toast.makeText(
//                activity,
//                getString(R.string.please_add_lot),
//                Toast.LENGTH_SHORT
//            ).show()
//        }
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
        batchList[addWeightPosition].editedWeight = split[0]
        batchList[addWeightPosition].editedUOM = split[1]
        binding.rvList.adapter?.notifyItemChanged(addWeightPosition)
        val come: Int? = split[0].toDouble().compareTo(batchList[addWeightPosition].weight?.toDouble() ?: 0.0)
        batchList[addWeightPosition].isLowerWeight = come ?: 0 <= 0
    }

    private fun validateInputs() {
        validateProceed()
//        when {
//            vm.vegaCoffeeReceivingData.vehicleNumber.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
//            vm.vegaCoffeeReceivingData.driverName.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver))
//            vm.vegaCoffeeReceivingData.storageLocationCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_loc))
//            else -> validateProceed()
//        }
    }

    private fun validateLotWeight(): Boolean {
        val selected = batchList.filter { !it.isLowerWeight }
        moreWeightBatches = selected.map { it.batch }.toString().replace("[", "").replace("]", "")
        return selected.isEmpty()
    }

    private fun validateEmptyWeight(): Boolean {
        val emptyWeight =
            batchList.filter { it.editedWeight.equals("0.0") || it.editedWeight.equals("0") || it.editedWeight.equals("") }
        return emptyWeight.isEmpty()
    }

    private fun enableProceed() {
        val enable = true
//            (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverName.text.isNotEmpty()
//                    && binding.tvTruckNoValue.text.toString().isNotEmpty()
//                    && binding.tvReceivingValue.text.isNotEmpty())
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
        vm.getOBDDetails(vm.vegaCoffeeReceivingData.delivery)
    }

    fun editLot(vegaCoffeeReceiveLots: VegaCoffeeReceiveLots) {
        editLotId = vegaCoffeeReceiveLots.batch
        vm.getOBDDetails(vegaCoffeeReceiveLots.mtnNumber)
    }

    private fun updateMandatory() {
        binding.tvDest.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.sending_wh)) { mandatoryStars() } }
        binding.tvsto.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.obd_number)) { mandatoryStars() } }
        binding.tvReceivingWH.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.receiving_locations)) { mandatoryStars() } }
        binding.tvTruckNo.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_number)) { mandatoryStars() } }
        binding.tvDriverName.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.driver_name)) { mandatoryStars() } }
    }
}
