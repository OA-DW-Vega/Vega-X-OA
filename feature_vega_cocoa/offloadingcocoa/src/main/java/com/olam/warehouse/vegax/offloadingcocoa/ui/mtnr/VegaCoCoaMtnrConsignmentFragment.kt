package com.olam.warehouse.vegax.offloadingcocoa.ui.mtnr

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoCoaReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingWarehouseWithMtns
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaOffloadingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaQualityWBDetail
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiveLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiving
import com.olam.warehouse.master.vegacocoa.model.VegaCoCoaReceivingMtnrWithLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcocoa.R
import com.olam.warehouse.vegax.offloadingcocoa.databinding.FragmentCocoaMtnrConsignmentLayoutBinding
import com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingcocoa.utils.ADD_WEIGHT
import com.olam.warehouse.vegax.offloadingcocoa.utils.MTNR_WEIGHSCALE_SUMMARY
import com.olam.warehouse.vegax.offloadingcocoa.utils.convertKgToMT
import com.olam.warehouse.vegax.offloadingcocoa.utils.getColor
import kotlinx.android.synthetic.main.item_mtnr_lot_card_layout.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class VegaCoCoaMtnrConsignmentFragment : BaseFragment(), VegaSingleSelectListener {

    override val layoutResourceId = R.layout.fragment_cocoa_mtnr_consignment_layout
    private lateinit var binding: FragmentCocoaMtnrConsignmentLayoutBinding
    private var callBack: VegaCoCoaOffloadReplaceFragmentCallback? = null
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private lateinit var moreWeightBatches: String
    private var addWeightPosition: Int = 0
    private val vm: VegaCoCoaOffloadingViewModel by viewModel()
    private var wareHouseList: MutableList<VegaReceivingWarehouse> = mutableListOf()
    private var plantList: MutableList<VegaCustomStLocation> = mutableListOf()
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var warehouseWithMtn = VegaCoffeeReceivingWarehouseWithMtns()
    private var selectedSendingLocation = VegaReceivingWarehouse()
    private var selectedOBD = VegaReceivingMtn()
    private var batchList = mutableListOf<VegaCoCoaReceiveLots>()
    private var allBatchList = mutableListOf<VegaCoCoaReceiveLots>()
    private var allOBDList = mutableListOf<VegaReceivingMtn>()
    private var filteredOBDList = mutableListOf<VegaReceivingMtn>()
    private var supplierList = mutableListOf<VegaVendor>()
    private var thirdPartyMaterialList = mutableListOf<VegaMaterial>()
    private var editLotId: String = ""
    private var vendorName: String = ""
    private var isThirdParty: Boolean = false
    private var vegaCoCoaReceivingData = VegaCoCoaReceiving()
    var bagMaterialList = mutableListOf<VegaCoCoaOffloadingBagMaterial>()
    private var currentImagePath: String? = ""
    val CAMERA_REQUEST_CODE = 0
    var imageFilePath: String = ""
    private val TAG = "PermissionDemo"
    private val RECORD_REQUEST_CODE = 101

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoCoaOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaCoCoaMtnrConsignmentFragment().putArgs {
        }

        fun newInstance(vegaCoCoaReceiving: VegaCoCoaReceiving) = VegaCoCoaMtnrConsignmentFragment().putArgs {
            putParcelable("editData", vegaCoCoaReceiving)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCocoaMtnrConsignmentLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vegaCoCoaReceivingData = arguments?.getParcelable("editData") ?: VegaCoCoaReceiving()
        if (!vegaCoCoaReceivingData.supplierCode.isNullOrEmpty()) {
            initExtra()
        }
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingcocoa/ui/mtnr/VegaCoCoaMtnrTypeSelectFragment").title("Mtnr CoCoa")
            .with(tracker)
    }

    private fun initExtra() {
        binding.tvWhValue.text = vegaCoCoaReceivingData.supplierCode + " - " + vegaCoCoaReceivingData.supplierName
        binding.tvstoValue.text = vegaCoCoaReceivingData.mtnCode
        binding.tvReceivingValue.text =
            vegaCoCoaReceivingData.storageLocationCode + " - " + vegaCoCoaReceivingData.storageLocationName
        vm.vegaCoCoaReceivingData.mtnCode = vegaCoCoaReceivingData.mtnCode
        vm.vegaCoCoaReceivingData.delivery = vegaCoCoaReceivingData.mtnCode!!
        vm.vegaCoCoaReceivingData.supplierCode = vegaCoCoaReceivingData.supplierCode
        vm.vegaCoCoaReceivingData.supplierName = vegaCoCoaReceivingData.supplierName
        vm.vegaCoCoaReceivingData.purchaseDocNum = if (batchList.size > 0) batchList[0].purchaseOrder else ""
        vm.vegaCoCoaReceivingData.purchaseDocDesc = if (batchList.size > 0) batchList[0].ebelp else ""
        vm.getOBDDetails(vegaCoCoaReceivingData.mtnCode!!)
        vm.vegaCoCoaReceivingData.storageLocationCode = ""
        vm.vegaCoCoaReceivingData.storageLocationName = ""
        vm.vegaCoCoaReceivingData.storageLocationCode = ""
        vm.vegaCoCoaReceivingData.storageLocationName = ""
        vm.vegaCoCoaReceivingData.storageLocationCode = vegaCoCoaReceivingData.storageLocationCode
        vm.vegaCoCoaReceivingData.storageLocationName = vegaCoCoaReceivingData.storageLocationName
    }

    private fun initUI() {
        enableProceed()
        binding.ivCamere.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.upload_ticket_photo)) { mandatoryStars() } }
        binding.tvTruckNo.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_number)) { mandatoryStars() } }
        binding.tvDriverName.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.driver_name)) { mandatoryStars() } }
        binding.tvVendor.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.transport_vendor)) { mandatoryStars() } }
        binding.tvReceivingWH.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.receiving_wh)) { mandatoryStars() } }
        binding.tvWaybillNumber.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.way_bill_number)) { mandatoryStars() } }

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
        /*vm.location.observe(viewLifecycleOwner, Observer { wareHouseList = it.toMutableList() })*/

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            customLocationList = it.filter { it.storageLocationType.equals("P") }.toMutableList()
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
        vm.getThirdPartyMaterials()
        vm.thirdPartyMaterialList.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                thirdPartyMaterialList.addAll(it)
            }
        })
        vm.weighBridgeId.observe(viewLifecycleOwner, Observer {
            val supplierCode = it.data?.data?.supplierCode
            if (!supplierCode.isNullOrEmpty()) {
                vendorName = supplierList.single { it.vendorCode.equals(supplierCode) }.vendorName!!
            }
            updateTruckDetail(it.data?.data)
        })

        if (AppUtils.isOnline()) {
            fetchMtnDetails()
            fetchPlantList()
        } else {
            fetchOfflineMtnDetails()
        }

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList = it/*.filter { data -> data.bcApprover?.isNotEmpty()!! }*/.toMutableList()
            val supplierData = supplierList/*.filter { data -> data.vendorCode.startsWith("2", true) }*/
            /*val suppliers = supplierData.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            val productAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suppliers)
            binding.tvVendorValue.threshold = 1
            binding.tvVendorValue.setAdapter(productAdapter)
            binding.tvVendorValue.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                enableProceed()
                it.forEach { material ->
                    if (material.vendorCode == binding.tvVendorValue.text.toString().split(" - ")[0]) {
                        vm.vegaCoCoaReceivingData.transportVendorCode = material.vendorCode
                        vm.vegaCoCoaReceivingData.transportVendorName = material.vendorName
                        binding.tvVendorValue.setText(material.vendorName)
                        binding.tvVendorValue.filters = arrayOf(InputFilter(30))
                    }
                }
            }*/
        })

        binding.tvDriverNoValue.onChange { enableProceed() }
        binding.tvDriverNameValue.onChange { enableProceed() }
        binding.tvTruckNoValue.onChange { enableProceed() }
        binding.tvWaybillNumberValue.onChange { enableProceed() }
        binding.btProceed.setOnClickListener { moveToSummary() }
        binding.btSave.setOnClickListener {
            vm.vegaCoCoaReceivingData.imagePath = imageFilePath
            vm.vegaCoCoaReceivingData.vehicleNumber = binding.tvTruckNoValue.text.toString()
            vm.vegaCoCoaReceivingData.truckDriverName = binding.tvDriverNameValue.text.toString()
            vm.vegaCoCoaReceivingData.contactNumber = binding.tvDriverNoValue.text.toString()
            vm.vegaCoCoaReceivingData.wayBillNo = binding.tvWaybillNumberValue.text.toString()
            vm.saveMtnrReceivingLots(vm.vegaCoCoaReceivingData, VegaCoCoaReceiveLots())
            activity?.finish()
        }
        binding.llCamera.setOnClickListener { setupPermissions() }
        vm.offloadingMtnr.observe(viewLifecycleOwner, Observer { updateOBDDetails(it) })

    }

    private fun moveToSummary() {
        binding.tvWaybillNumberValue.isCursorVisible = false
        binding.tvVendorValue.isCursorVisible = false
        vm.vegaCoCoaReceivingData.imagePath = imageFilePath
        vm.vegaCoCoaReceivingData.vehicleNumber = binding.tvTruckNoValue.text.toString()
        vm.vegaCoCoaReceivingData.truckDriverName = binding.tvDriverNameValue.text.toString()
        vm.vegaCoCoaReceivingData.contactNumber = binding.tvDriverNoValue.text.toString()
        vm.vegaCoCoaReceivingData.wayBillNo = binding.tvWaybillNumberValue.text.toString()
        vm.vegaCoCoaReceivingData.transportVendorCode = binding.tvVendorValue.text.toString()
        vm.vegaCoCoaReceivingData.transportVendorName = binding.tvVendorValue.text.toString()
        validateInputs()
        /*val addedWeight = batchList.any { it.editedWeight.isNullOrEmpty() || it.editedWeight.equals("0.0") }
        if(addedWeight) activity?.toast(getString(R.string.add_lot_weight))
        else callBack?.replaceFragment(MTNR_WEIGHSCALE_SUMMARY,vm.vegaCoCoaReceivingData)*/
    }

    private fun updateOBDDetails(data: VegaCoCoaReceivingMtnrWithLots?) {
        val fragment = activity?.supportFragmentManager?.findFragmentById(R.id.flContainer)
        setValueEmpty(fragment)
        if (data != null) {
            if (!data.receiving.imagePath.isNullOrEmpty()) {
                imageFilePath = data.receiving.imagePath ?: ""
                updateCameraLayout()
            }
            binding.tvReceivingValue.text =
                data.receiving.storageLocationCode.plus(" - ").plus(data.receiving.storageLocationName)
            if (data.receiving.transportVendorCode?.isNotEmpty()!!) {
                when (fragment) {
                    is VegaCoCoaMtnrConsignmentFragment -> {
                        binding.tvVendorValue.setText(
                            data.receiving.transportVendorCode
                        )
                    }
                }
            } else
                binding.tvVendorValue.setText("")
            binding.tvTruckNoValue.setText(data.receiving.vehicleNumber)
            binding.tvDriverNameValue.setText(data.receiving.truckDriverName)
            binding.tvDriverNoValue.setText(data.receiving.contactNumber)
            binding.tvWaybillNumberValue.setText(data.receiving.wayBillNo)

            vm.vegaCoCoaReceivingData = data.receiving
            if (data.lineItems.size > 0) {
                if (batchList.isEmpty()) {
                    data.lineItems.forEach {
                        batchList.add(it.lots)
                    }
                }
                batchList.forEach {
                    var netWeight = 0.0
                    var grossWeight = 0.0
                    var truckOutWeight = 0.0
                    val bags = data.lineItems.filter { it2 -> it2.lots.mtnNumber.equals(it.mtnNumber) }
                        .filter { it1 -> it1.lots.batch.equals(it.batch) }
                    if (bags.size == 1) {
                        if (vm.vegaCoCoaReceivingData.startTime?.isEmpty() == true) vm.vegaCoCoaReceivingData.startTime =
                            DateUtils.getCurrentTimeInMills().toString()
                    }
                    try {
                        if (bags.isNotEmpty() && bags[0].bagItem.isNotEmpty()) netWeight =
                            bags[0].bagItem.filter { it2 -> it2.mtnNumber.equals(it.mtnNumber) }
                                .filter { it1 -> it1.batchNumber.equals(it.batch) }
                                .map { it3 -> it3.netWeight }[0].toDouble()
                        if (bags.isNotEmpty() && bags[0].bagItem.isNotEmpty()) grossWeight =
                            bags[0].bagItem.filter { it2 -> it2.mtnNumber.equals(it.mtnNumber) }
                                .filter { it1 -> it1.batchNumber.equals(it.batch) }
                                .map { it3 -> it3.grossWeight }[0].toDouble()
                        if (bags.isNotEmpty() && bags[0].bagItem.isNotEmpty()) truckOutWeight =
                            bags[0].bagItem.filter { it2 -> it2.mtnNumber.equals(it.mtnNumber) }
                                .filter { it1 -> it1.batchNumber.equals(it.batch) }
                                .map { it3 -> it3.truckOutWeight }[0].toDouble()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    vm.vegaCoCoaReceivingData.grossWeight =
                        if (it.uom.equals("MT")) convertKgToMT(grossWeight.toString()) else grossWeight.toString()
                    vm.vegaCoCoaReceivingData.netWeight =
                        if (it.uom.equals("MT")) convertKgToMT(netWeight.toString()) else netWeight.toString()
                    vm.vegaCoCoaReceivingData.truckOutWeight =
                        if (it.uom.equals("MT")) convertKgToMT(truckOutWeight.toString()) else truckOutWeight.toString()

                    val totalNetWeight = netWeight/*grossWeight.minus(truckOutWeight).minus(netWeight)*/
                    it.editedWeight =
                        if (it.uom.equals("MT")) convertKgToMT(totalNetWeight.toString()) else totalNetWeight.toString()
                }
                setUpAdapter(batchList as ArrayList<VegaCoCoaReceiveLots>)
            } else {
                setUpAdapter(batchList as ArrayList<VegaCoCoaReceiveLots>)
            }
        } else {
            if (AppUtils.isOnline())
                vm.getWeighBridgeIdDetail(selectedOBD.mtntWbid)
            else
                vm.getWBDetails(selectedOBD.mtntWbid)
            setUpAdapter(batchList as ArrayList<VegaCoCoaReceiveLots>)
        }
    }


    private fun fetchMtnDetails() {
        vm.warehouse.observe(viewLifecycleOwner, Observer { processApiResult(it) })
        vm.fetchWarehouseWithMtns()
    }

    private fun fetchPlantList() {
        vm.plant.observe(viewLifecycleOwner, Observer {
            plantList = it.toMutableList()
        })
        vm.getPlants()
    }

    private fun fetchOfflineMtnDetails() {
        vm.getWarehouses()
        vm.getMTNRs()
        vm.getOfflineLots()
        vm.getPlants()

        vm.plant.observe(viewLifecycleOwner, Observer {
            plantList = it.toMutableList()
        })
        vm.warehouseLocal.observe(viewLifecycleOwner, Observer {
            wareHouseList = it.toMutableList()
        })
        vm.mtnrLocal.observe(viewLifecycleOwner, Observer {
            allOBDList = it.toMutableList()
        })
        vm.lotLocal.observe(viewLifecycleOwner, Observer {
            allBatchList = prepareBatchList(it)
        })
        vm.wbDetailsLocal.observe(viewLifecycleOwner, Observer {
            if (it != null) updateTruckDetail(it)
        })

    }

    private fun processApiResult(data: Resource<GenericReqAndResp<VegaCoCoaReceivingMtnWrapper>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> saveResult(it.data)
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> UIUtils.showErrorDialog(requireContext(), it.error.toString())
            }
        }
    }

    private fun saveResult(data: GenericReqAndResp<VegaCoCoaReceivingMtnWrapper>?) {
        hideLoading()
        data?.data?.let {
            wareHouseList = data.data.stockSupplyingPlants.toMutableList()
            allOBDList = data.data.mtns.toMutableList()
            allBatchList = data.data.batchDetails.toMutableList()
        }
    }

    private fun showSingleSelectDialog(isWh: Boolean, title: String, isVendor: Boolean) {
        val list: List<String>
        if (isVendor) {
            /*list = customLocationList.map {
                it.procureLocationCode.plus(" - ").plus(it.procureLocationName)
            } as ArrayList<String>*/
            list = plantList.map {
                it.procureLocationCode.plus(" - ").plus(it.procureLocationName)
            } as ArrayList<String>
        } else if (isWh) {
            list = wareHouseList.map {
                it.supplyingPlantId.plus(" - ").plus(it.supplyingPlantName)
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
            vm.vegaCoCoaReceivingData.supplierCode = whId[0].trim()
            vm.vegaCoCoaReceivingData.supplierName = whId[1].trim()
            selectedSendingLocation = wareHouseList.single { it.supplyingPlantId == whId[0].trim() }
            filteredOBDList =
                allOBDList.filter { it.supplyingPlantId == selectedSendingLocation.supplyingPlantId }
                    .toMutableList()
        } else if (!isVendor) {
            binding.tvstoValue.text = data
            vm.vegaCoCoaReceivingData.mtnCode = data
            vm.vegaCoCoaReceivingData.delivery = data
            selectedOBD = filteredOBDList.filter { it.mtnNumber == data }[0]

            batchList = allBatchList.filter { it.mtnNumber == selectedOBD.mtnNumber }.toMutableList()
            vm.vegaCoCoaReceivingData.purchaseDocNum =
                if (batchList.size > 0) batchList[0].purchaseOrder else selectedOBD.purchaseOrder
            vm.vegaCoCoaReceivingData.purchaseDocDesc =
                if (batchList.size > 0) batchList[0].ebelp else selectedOBD.ebelp
            vm.getOBDDetails(data)
            vm.vegaCoCoaReceivingData.storageLocationCode = ""
            vm.vegaCoCoaReceivingData.storageLocationName = ""
            vm.vegaCoCoaReceivingData.storageLocationCode = ""
            vm.vegaCoCoaReceivingData.storageLocationName = ""
            //setUpAdapter(batchList as ArrayList<VegaCoCoaReceiveLots>)
        } else {
            val whId = data.split("-")
            binding.tvReceivingValue.text = data
            vm.vegaCoCoaReceivingData.storageLocationCode = whId[0].trim()
            vm.vegaCoCoaReceivingData.storageLocationName = whId[1].trim()
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

    private fun setUpAdapter(list: ArrayList<VegaCoCoaReceiveLots>) {
        var lineItems = mutableListOf<VegaCoCoaReceiveLots>()
        if (editLotId.isNotEmpty()) {
            //enableDisableItem(false)
            lineItems = list.filter { it.batch.equals(editLotId) } as MutableList<VegaCoCoaReceiveLots>
        } else {
            //enableDisableItem(true)
            lineItems = list
        }
        binding.rvList.setUp(lineItems, R.layout.item_mtnr_lot_card_layout, { item, pos ->
            tvScaleLotValue.text = item.batch
            //tvStLocationValue.text = item.storageLocationCode
            tvScaleWeightValue.text =
                item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(item.uom)
            tvScaleGradeValue.text = item.materialName
            val editedWeight = if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString().toDouble()
                .formatThreeDigits()
            tvScaleDispatchValue.setText(editedWeight, TextView.BufferType.EDITABLE)
            tvDispatchUOMValue.text = item.uom
            ivEdit.visibility = View.GONE
            tvVendorName.visibility = if (isThirdPartyMaterial(item.materialNumber)) View.VISIBLE else View.GONE
            tvVendorNameValue.visibility =
                if (isThirdPartyMaterial(item.materialNumber)) View.VISIBLE else View.GONE
            tvVendorNameValue.text = vendorName
            cbEndLot.isChecked = vm.vegaCoCoaReceivingData.endLot ?: false
            cbEndLot.setOnCheckedChangeListener { buttonView, isChecked ->
                vm.vegaCoCoaReceivingData.endLot = isChecked
            }

            tv_add_weight.setOnClickListener {
                binding.tvWaybillNumberValue.isCursorVisible = false
                binding.tvVendorValue.isCursorVisible = false
                addWeightPosition = pos
                vm.vegaCoCoaReceivingData.imagePath = imageFilePath
                vm.vegaCoCoaReceivingData.vehicleNumber = binding.tvTruckNoValue.text.toString()
                vm.vegaCoCoaReceivingData.truckDriverName = binding.tvDriverNameValue.text.toString()
                vm.vegaCoCoaReceivingData.contactNumber = binding.tvDriverNoValue.text.toString()
                vm.vegaCoCoaReceivingData.wayBillNo = binding.tvWaybillNumberValue.text.toString()
                vm.vegaCoCoaReceivingData.transportVendorCode =
                    if (vm.vegaCoCoaReceivingData.transportVendorCode.isNullOrEmpty()) binding.tvVendorValue.text.toString() else vm.vegaCoCoaReceivingData.transportVendorCode
                vm.vegaCoCoaReceivingData.materialName = item.materialName
                vm.vegaCoCoaReceivingData.tempGrnNumber = Random.nextLong().toString()

                item.delivery = item.mtnNumber
               vm.saveMtnrReceivingLots(vm.vegaCoCoaReceivingData, item)
                var data = Bundle()
                data.putParcelable("MODEL_BUNDLE", item)
                data.putParcelableArrayList(UIUtils.BAGS_DATA, ArrayList(bagMaterialList))
                callBack?.replaceFragment(ADD_WEIGHT, data)
            }
        })
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
        binding.tvReceivingValue.text = ""
        binding.tvTruckNoValue.setText("")
        binding.tvDriverNameValue.setText("")
        binding.tvDriverNoValue.setText("")
        binding.tvWaybillNumberValue.setText("")
        when (fragment) {
            is VegaCoCoaMtnrConsignmentFragment -> binding.tvVendorValue.setText("")
        }
    }

    private fun validateProceed() {
        if (batchList.isNotEmpty()) {
            if (imageFilePath.isNotEmpty()) {
                if (validateEmptyWeight()) {
                    if (validateLotWeight()) {
                        /*if (editLotId.isEmpty()) showRemarkDialog() else {*/

                        if (isThirdParty)
                            vm.vegaCoCoaReceivingData.thirdPartyVendorName = vendorName
                        val endTime = System.currentTimeMillis()
                        vm.vegaCoCoaReceivingData.endTime = endTime.toString()
                        if (vm.vegaCoCoaReceivingData.startTime.isNullOrEmpty()) vm.vegaCoCoaReceivingData.startTime =
                            "0"
                        val duration = endTime.minus(vm.vegaCoCoaReceivingData.startTime?.toLong() ?: 0)
                        vm.vegaCoCoaReceivingData.remarks = ""
                        vm.vegaCoCoaReceivingData.turnAroundTime = TimeUnit.MILLISECONDS.toMinutes(duration).toString()


                        batchList.forEach {
                            it.delivery = vm.vegaCoCoaReceivingData.delivery
                            vm.saveMtnrReceivingLots(vm.vegaCoCoaReceivingData, it)
                            /*}*/
                            callBack?.replaceFragment(MTNR_WEIGHSCALE_SUMMARY, vm.vegaCoCoaReceivingData)
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
                    getString(R.string.error_valid_ticket_photo),
                    Toast.LENGTH_SHORT
                ).show()
            }
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
                    batchList.forEach {
                        it.delivery = vm.vegaCoCoaReceivingData.delivery
                        vm.saveMtnrReceivingLots(vm.vegaCoCoaReceivingData, it)
                    }
                    callBack?.replaceFragment(MTNR_WEIGHSCALE_SUMMARY, vm.vegaCoCoaReceivingData)
                }
            }

        }, true, vm.vegaCoCoaReceivingData.remarks.toString())
    }


    fun updateAddWeight(weight: String) {
        /* val split = weight.split(" ")
         val come: Int? = split[0].toDouble().compareTo(batchList[addWeightPosition].weight?.toDouble() ?: 0.0)
         batchList[addWeightPosition].isLowerWeight = come ?: 0 <= 0
         batchList[addWeightPosition].editedWeight = split[0]
         batchList[addWeightPosition].editedUOM = split[1]
         binding.rvList.adapter?.notifyItemChanged(addWeightPosition)*/
        /*vm.lotList[addWeightPosition].editedWeight = split[0]
        vm.lotList[addWeightPosition].weightToDispatchUOM = split[1]
        binding.clLotSummary.rvList.adapter?.notifyItemChanged(addWeightPosition)
        val come: Int? = split[0].toDouble().compareTo(vm.lotList[addWeightPosition].weight?.toDouble() ?: 0.0)
        vm.lotList[addWeightPosition].isLowerWeight = come ?: 0 <= 0
        vm.addLoTInDB(vm.lotList[addWeightPosition])
        calculateAndUpdateWeightToDispatch()*/
    }

    private fun validateInputs() {
        when {
            vm.vegaCoCoaReceivingData.vehicleNumber.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
            vm.vegaCoCoaReceivingData.truckDriverName.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver))
            vm.vegaCoCoaReceivingData.transportVendorCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_trans_vendor))
            vm.vegaCoCoaReceivingData.storageLocationCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_loc))
            vm.vegaCoCoaReceivingData.wayBillNo.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_waybill_number))
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
            batchList.filter { it.editedWeight.equals("0.0") || it.editedWeight.equals("0") || it.editedWeight.equals("") }
        return emptyWeight.isEmpty()
    }

    private fun enableProceed() {
        val enable =
            (binding.tvWhValue.text.isNotEmpty() &&
                    binding.tvstoValue.text.isNotEmpty() &&
                    binding.tvDriverName.text.isNotEmpty() &&
                    binding.tvTruckNoValue.text.toString().isNotEmpty() &&
                    binding.tvVendorValue.text.toString().isNotEmpty() &&
                    binding.tvReceivingValue.text.isNotEmpty() &&
                    binding.tvWaybillNumberValue.text.isNotEmpty())
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
        vm.getOBDDetails(vm.vegaCoCoaReceivingData.delivery)
    }

    fun editLot(vegaCoCoaReceiveLots: VegaCoCoaReceiveLots) {
        editLotId = vegaCoCoaReceiveLots.batch
        vm.getOBDDetails(vegaCoCoaReceiveLots.mtnNumber)
    }

    private fun setupPermissions() {
        val permission = activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) }
        if (permission != PackageManager.PERMISSION_GRANTED) makeRequest() else moveToCameraView()
    }

    private fun makeRequest() {
        requestPermissions(
            arrayOf(Manifest.permission.CAMERA),
            RECORD_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                val granted = grantResults.isNotEmpty()
                        && permissions.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && !activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, permissions[0]) }!!

                when (granted) {
                    true -> moveToCameraView()
                }
            }
        }
    }

    private fun moveToCameraView() {
        try {
            val imageFile = createImageFile()
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (activity?.packageManager?.let { callCameraIntent.resolveActivity(it) } != null) {
                val authorities = activity!!.packageName + ".fileprovider"
                val imageUri = activity?.let { FileProvider.getUriForFile(it, authorities, imageFile) }
                callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
            }
        } catch (e: IOException) {
            UIUtils.showErrorDialog(requireContext(), "Could not create file!")
            //activity?.toast("Could not create file!")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    updateCameraLayout()
                } else {
                    imageFilePath = ""
                }
            }
            else -> {
                UIUtils.showErrorDialog(requireContext(), "Unrecognized request code")
                //activity?.toast("Unrecognized request code")
            }
        }
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val imageFileName: String = "JPEG_".plus(binding.tvstoValue.text)
        val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!storageDir!!.exists()) storageDir.mkdirs()
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        imageFilePath = imageFile.absolutePath
        return imageFile
    }

    fun updateCameraLayout() {
        binding.ivCamere.text = binding.tvstoValue.text.toString().plus(".jpg")
        ViewCompat.setBackgroundTintList(
            binding.ivCamere,
            ContextCompat.getColorStateList(
                activity?.applicationContext!!,
                com.olam.warehouse.presentation.R.color.dark_marun
            )
        )
        ViewCompat.setBackgroundTintList(
            binding.llCamera,
            ContextCompat.getColorStateList(
                activity?.applicationContext!!,
                com.olam.warehouse.presentation.R.color.dark_marun
            )
        )
    }

    private fun isThirdPartyMaterial(materialCode: String): Boolean {
        val material = thirdPartyMaterialList.filter { materialCode.contains(it.materialCode) }
        if (material.isNotEmpty()) {
            isThirdParty = true
            return true
        }
        return false
    }

    private fun updateTruckDetail(response: VegaCoCoaQualityWBDetail?) {

      bagMaterialList.clear()

        if(!response?.bagCount.isNullOrEmpty())
        {
            var receiving = VegaCoCoaOffloadingBagMaterial()

            receiving.bagCount = response?.bagCount!!
            receiving.bagType = response.bagType!!
            receiving.unitsOfMeasure = response.unitsOfMeasure
            receiving.tareWeight = response.bagWeight
            receiving.createdPosition = 0
            bagMaterialList.add(receiving)

        }
        if(!response?.pmat2Count.isNullOrEmpty())
        {
            var receiving = VegaCoCoaOffloadingBagMaterial()

            receiving.bagCount = response?.pmat2Count!!
            receiving.bagType = response.pmat2Type!!
            receiving.unitsOfMeasure = response.unitsOfMeasure
            receiving.tareWeight = response.pmat2Weight
            receiving.createdPosition = 1
            bagMaterialList.add(receiving)

        }
        if(!response?.pmat3Count.isNullOrEmpty())
        {
            var receiving = VegaCoCoaOffloadingBagMaterial()

            receiving.bagCount = response?.pmat3Count!!
            receiving.bagType = response.pmat3Type!!
            receiving.unitsOfMeasure = response.unitsOfMeasure
            receiving.tareWeight = response.pmat3Weight
            receiving.createdPosition = 2
            bagMaterialList.add(receiving)

        }

        bagMaterialList= bagMaterialList.filter { it.bagCount.trim().toInt()>0 }.toMutableList()
        vm.vegaCoCoaReceivingData.erdat = DateUtils.getCurrentDate()
        if (!response?.vehicleNumber.isNullOrEmpty()) {
            binding.tvTruckNoValue.setText(response?.vehicleNumber)
            binding.tvTruckNoValue.isEnabled = false
        } else binding.tvTruckNoValue.isEnabled = true
        if (!response?.truckDriverName.isNullOrEmpty()) {
            binding.tvDriverNameValue.setText(response?.truckDriverName)
            binding.tvDriverNameValue.isEnabled = false
        } else binding.tvDriverNameValue.isEnabled = true
        if (!response?.contactNumber.isNullOrEmpty()) {
            binding.tvDriverNoValue.setText(response?.contactNumber)
            binding.tvDriverNoValue.isEnabled = false
        } else binding.tvDriverNoValue.isEnabled = true
        if (!response?.challan.isNullOrEmpty()) {
            binding.tvWaybillNumberValue.setText(response?.challan)
            binding.tvWaybillNumberValue.isEnabled = false
        } else binding.tvWaybillNumberValue.isEnabled = true
        if (!response?.transportVendorCode.isNullOrEmpty()) {
            binding.tvVendorValue.setText(response?.transportVendorCode)
            binding.tvVendorValue.isEnabled = false
        } else binding.tvVendorValue.isEnabled = true
        val vendorCode = response?.transportVendorCode
        if (!vendorCode.isNullOrEmpty()) {
            vendorName = supplierList.singleOrNull { it.vendorCode == vendorCode }?.vendorName ?: ""
            binding.rvList.adapter?.notifyDataSetChanged()
        }
    }

    fun prepareBatchList(data: List<VegaReceivingMtnLots>): MutableList<VegaCoCoaReceiveLots> {
        val lotList = arrayListOf<VegaCoCoaReceiveLots>()
        data.forEach {
            val receiveLots = VegaCoCoaReceiveLots()
            receiveLots.batch = it.batch
            receiveLots.materialName = it.materialName
            receiveLots.materialNumber = it.materialNumber
            receiveLots.weight = it.weight.toString()
            receiveLots.mtnNumber = it.mtnNumber
            receiveLots.posnr = it.posnr
            receiveLots.weightAdded = it.weightAdded
            receiveLots.uom = it.uom
            receiveLots.purchaseOrder = it.purchaseOrder
            receiveLots.ebelp = it.ebelp
            receiveLots.supplyingPlantId = it.supplyingPlantId
            receiveLots.supplyingPlantName = it.supplyingPlantName
            receiveLots.hasWeightAdded = it.hasWeightAdded
            receiveLots.bagCount = it.bagCount
            lotList.add(receiveLots)
        }
        return lotList.toMutableList()
    }

}
