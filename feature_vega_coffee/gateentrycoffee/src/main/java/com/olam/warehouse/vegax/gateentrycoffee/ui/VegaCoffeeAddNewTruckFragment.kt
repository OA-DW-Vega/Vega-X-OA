package com.olam.warehouse.vegax.gateentrycoffee.ui

import  android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCoffeeSingleSelectListener
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTime
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTimeMillis
import com.olam.warehouse.presentation.utils.LocaleHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentrycoffee.R
import com.olam.warehouse.vegax.gateentrycoffee.databinding.FragmentVegaCoffeeAddNewTruckBinding
import com.olam.warehouse.vegax.gateentrycoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VegaCoffeeAddNewTruckFragment : BaseFragment(), VegaCoffeeSingleSelectListener,
    VegaSingleSelectListener {

    private var distinctMtnsList = ArrayList<VegaReceivingMtn>()
    private var callBack: VegaCoffeeReplaceCallback? = null
    private var gateEntryData = VegaGateEntry()
    private var mtnsList = mutableListOf<VegaReceivingMtn>()
    private var productList = mutableListOf<VegaReceivingMtnLots>()
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var warehouselist = mutableListOf<VegaSupplyStorageLocation>()
    private var selectedStorageLoc: String? = ""
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private var materialProduct = ArrayList<VegaMaterial>()
    private var suppliersNameList = ArrayList<String>()
    private var supplierList = ArrayList<VegaVendor>()

    private val vm: VegaCoffeeGateEntryViewModel by viewModel()
    private lateinit var binding: FragmentVegaCoffeeAddNewTruckBinding
    override val layoutResourceId = R.layout.fragment_vega_coffee_add_new_truck

    companion object {
        fun newInstance(gateEntryData: VegaGateEntry) = VegaCoffeeAddNewTruckFragment().putArgs {
            putParcelable(GATE_ENTRY_DATA, gateEntryData)
        }
        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as VegaCoffeeReplaceCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCoffeeAddNewTruckBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("gateentry/ui/VegaAddNewTruckFragment").title("Gate Entry").with(tracker)
        initUI()
    }

    private fun initUI() {
        gateEntryData = arguments?.getParcelable(GATE_ENTRY_DATA)!!
        updateMandatory()
        gateEntryData.tmpWbId = ""
        gateEntryData.weighBridgeId = ""
        if (gateEntryData.weighBridgeType == PROCURE) {
            binding.llSupplier.visible()
            binding.llMtnr.gone()
            binding.tvBagCountLabel.visible()
            binding.llDeclaredWeightcontainerID.visible()
            binding.tvDeclaredWeightLabel.visible()
            binding.llBagCountcontainerID.visible()
            binding.tvConnaissementLabel.visible()
            binding.llConnaissementcontainerID.visible()
            binding.tvCooperativeLabel.visible()
            binding.llCooperativecontainerID.visible()

        } else {
            binding.llSupplier.gone()
            binding.llMtnr.visible()
            binding.tvBagCountLabel.gone()
            binding.llDeclaredWeightcontainerID.gone()
            binding.tvDeclaredWeightLabel.gone()
            binding.llBagCountcontainerID.gone()
            binding.tvConnaissementLabel.gone()
            binding.llConnaissementcontainerID.gone()
            binding.tvCooperativeLabel.gone()
            binding.llCooperativecontainerID.gone()
        }
        binding.tvDate.text = getUTCDateTime(System.currentTimeMillis().toString(), App.getAppContext())
        binding.tvDate.setOnClickListener { getDatePickerDialog() }
        binding.btnConfirm.setOnClickListener { validateInputs() }

        vm.product.observe(viewLifecycleOwner, Observer {
            materialProduct.clear()
            val products = it.map { data -> data.materialName }
            materialProduct.addAll(it)
            it.forEach { it1 ->
                /*if (it1.materialName.equals("Ivory Coast Raw Cashew Nut")) {*/
                gateEntryData.materialCode =
                    MATERIAL_CODE.plus(it1.materialCode.toString())
                gateEntryData.materialName = it1.materialName.toString()
                // binding.tvProduct.setText(it1.materialName, TextView.BufferType.EDITABLE)
                binding.tvUom.text = it1.unitsOfMeasure.toString()
                binding.tvweightUom.text=it1.unitsOfMeasure.toString()
                gateEntryData.unitsOfMeasure = it1.unitsOfMeasure.toString()
                /*}*/
            }
        })
        vm.getProducts()
        binding.tvProduct.setOnClickListener {
            if (gateEntryData.weighBridgeType == PROCURE)
                showSingleSelectDialog(
                    true,
                    getString(R.string.select_material),
                    false,
                    false,
                    false
                )
        }

        binding.tvSupplier.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.select_supplier),
                false,
                false,
                false, true
            )
        }
        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList.clear()
            suppliersNameList.clear()
            supplierList.addAll(it/*.filter { data -> data.bcApprover?.isNotEmpty()!! }*/)
            suppliersNameList.addAll(supplierList.map { data -> data.vendorCode.plus("-").plus(data.vendorName) })
        })
        vm.getSuppliers()

        vm.suppplierZone.observe(viewLifecycleOwner, Observer {
            it?.let { item ->
                if (it.size > 0) {
                    binding.tvSupplierZone.setText(item[0].bczone.toString())
                    binding.tvSupplierZone.isEnabled = false
                }
            }

        })

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            custonLocationList = it.filter { it.storageLocationType.equals("P") }.toMutableList()
        })
        vm.getCustomLocations()

        if (AppUtils.isOnline() && gateEntryData.weighBridgeType == STO) {
            fetchMtnDetails()
        }
        binding.tvObdNumber.setOnClickListener { /*showStoDialog(mtnsList)*/showSingleSelectDialog(
            false,
            getString(R.string.tittle_odb_popup),
            false,
            true,
            false
        )
        }
        binding.tvDispatchWarehouse.setOnClickListener { /*showDispatchWHDialog(warehouselist)*/ showSingleSelectDialog(
            false,
            getString(R.string.trans_Warehouse_popup),
            true,
            false,
            false
        )
        }
        binding.tvReceivingLocation.setOnClickListener {
            showSingleSelectDialog(false, getString(R.string.receiving_location_popup), false, false, true)
        }

        vm.gateEntryLocal.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                gateEntryData = it
                updateLocalCache()
            } else {
                if (gateEntryData.weighBridgeId.isNotEmpty())
                    vm.getWeighBridgeIdDetail(gateEntryData.weighBridgeId, gateEntryData.imageString.equals(WEIGHSCALE))
            }
        })
        vm.weighBridgeId.observe(viewLifecycleOwner, Observer { updateWeighbridgeValueUI(it) })
    }


    private fun updateLocalCache() {
        binding.tvPhoneNo.setText(gateEntryData.contactNumber ?: "")
        binding.tvDate.text = getUTCDateTime(gateEntryData.erdat ?: "", App.getAppContext())
        binding.tvTruckNo.setText(gateEntryData.vehicleNumber)
        binding.tvDriverName.setText(gateEntryData.truckDriverName)
        binding.tvSupplierZone.setText(gateEntryData.supplierZone)
        binding.etWeight.setText(gateEntryData.approximateWeight)
        binding.tvProduct.text = gateEntryData.materialCode.plus("-").plus(gateEntryData.materialName)
        binding.tvReceivingLocation.text = gateEntryData.supplierName
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
            val list = it.mtns.toSet()
            mtnsList.clear()
            mtnsList.addAll(list)
            productList = it.batchDetails as MutableList<VegaReceivingMtnLots>
            warehouselist = it.storageLocationLst as MutableList<VegaSupplyStorageLocation>
        }
    }


    private fun getDatePickerDialog() {
        val cal = Calendar.getInstance()
        val dateTxt = binding.tvDate.text.split("/")
        cal.set(dateTxt[2].toInt(), dateTxt[0].toInt() - 1, dateTxt[1].toInt())
        val DATE_FORMAT = "MM/dd/yyyy"
        val UTC = "UTC"

        context?.let {
            val datePicker = DatePickerDialog(
                it,
                com.olam.warehouse.presentation.R.style.DatePickerTheme,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val sdf = SimpleDateFormat(DATE_FORMAT, LocaleHelper.getLocale(it))
                    sdf.timeZone = TimeZone.getTimeZone(UTC)
                    binding.tvDate.text = sdf.format(cal.time)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            //datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
            datePicker.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        }

    }

    private fun validateInputs() {
        when {
            gateEntryData.materialCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_product))
            binding.tvDate.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_date))
            binding.tvReceivingLocation.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_location))
            binding.tvTruckNo.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
            binding.tvDriverNameLabel.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver_name))
            binding.tvPhoneNo.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_phone_no))
            binding.etWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_weight))
            else -> {
                if (gateEntryData.weighBridgeType == PROCURE) {
                    when {
                        binding.tvSupplier.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_supplier))
                        binding.etDeclaredWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_supplier))
                        binding.etConnaissement.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_connaissement))
                        binding.etBagCount.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_bagcount))
                        /*binding.tvSupplierZone.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_supplier_zone))*/
                        else -> moveToSummary()
                    }

                } else if (gateEntryData.weighBridgeType == STO) {
                    when {
                        binding.tvDispatchWarehouse.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_dispatch_wh))
                        binding.tvObdNumber.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_obd_no))
                        else -> moveToSummary()
                    }
                } else moveToSummary()

            }
        }
    }

    private fun moveToSummary() {
        gateEntryData.contactNumber = binding.tvPhoneNo.text.toString()
        gateEntryData.erdat = getUTCDateTimeMillis(binding.tvDate.text.toString(), App.getAppContext())
        gateEntryData.vehicleNumber = binding.tvTruckNo.text.toString()
        gateEntryData.driverName = binding.tvDriverName.text.toString()
        gateEntryData.truckDriverName = binding.tvDriverName.text.toString()
        gateEntryData.supplierZone = binding.tvSupplierZone.text.toString()
        gateEntryData.delivery = binding.tvObdNumber.text.toString()
        gateEntryData.approximateWeight = binding.etWeight.text.toString()
        gateEntryData.bagCount = binding.etBagCount.text.toString()
        gateEntryData.vendorDeclaredWeight = binding.etDeclaredWeight.text.toString()
        gateEntryData.challan = binding.etConnaissement.text.toString()
        gateEntryData.remarks = binding.etCooperative.text.toString()
        //gateEntryData.grossWeight = binding.etWeight.text.toString()
        gateEntryData.wsGate = if (gateEntryData.imageString.equals(WEIGHSCALE)) WS01 else WB01
        if (gateEntryData.tmpWbId.isBlank()) {
            gateEntryData.tmpWbId = getTmpId()
        }
        gateEntryData.commonPrimaryId =
            gateEntryData.weighBridgeType.plus(gateEntryData.supplierCode)
                .plus(gateEntryData.delivery)
        saveLocalCache()
        callBack?.replaceFragment(SUMMARY_FRAG, gateEntryData)
    }

    private fun showSingleSelectDialog(
        isProduct: Boolean,
        title: String,
        isWh: Boolean,
        isObd: Boolean,
        isReceiving: Boolean,
        isSupplier: Boolean = false
    ) {
        val list = ArrayList<String>()
        if (isProduct) {
            list.addAll(materialProduct.map { it.materialCode.plus("-").plus(it.materialName) } as ArrayList<String>)
        } else if (isWh) {
            list.addAll(warehouselist.map { it.storageLocationCode.plus("-").plus(it.storageLocationName) })
        } else if (isObd) {
            distinctMtnsList = mtnsList.distinct()
                .filter { it.storageLocationCode == selectedStorageLoc } as ArrayList<VegaReceivingMtn>
            list.addAll(distinctMtnsList.map { it.mtnNumber })
            val distinctList = list.distinct()
            list.clear()
            list.addAll(distinctList)
        } else if (isReceiving) {
            val location = custonLocationList.filter {
                !it.storageLocationType.equals(
                    "B"
                )
            }
            list.addAll(location.map { data ->
                data.procureLocationCode.plus("-").plus(data.procureLocationName)
            })
        } else if (isSupplier) {
            list.clear()
            list.addAll(suppliersNameList)
        }

        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isProduct, isWh, isObd,
                list,
                activity!!,
                this, isReceiving, isSupplier, this,isOrigin = false,isDepartment = false
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, isProduct: Boolean, isWh: Boolean, isObd: Boolean, isSupplier: Boolean,isOrigin:Boolean,isDepartment:Boolean) {
        customDialog?.dismiss()
        if (isProduct) {
            val split = data.split("-")
            val material = materialProduct.single { it.materialCode == split[0] }
            gateEntryData.materialCode =
                MATERIAL_CODE.plus(material.materialCode)
            gateEntryData.materialName = material.materialName.toString()
            binding.tvUom.text = material.unitsOfMeasure.toString()
            gateEntryData.unitsOfMeasure = material.unitsOfMeasure.toString()
            binding.tvProduct.text = data
        } else if (isWh) {
            binding.tvDispatchWarehouse.text = data
            val split = data.split("-")
            binding.tvObdNumber.text = ""
            val item = warehouselist.single { it.storageLocationCode == split[0] }
            selectedStorageLoc = item.storageLocationCode
            gateEntryData.plantId = /*UIUtils.getWarehouseId().toString()*/ getPlantDetails().plantId
            gateEntryData.supplierCode = item.plant
            gateEntryData.supplierName = data
        } else if (isObd) {
            binding.tvObdNumber.text = data
            val mtn = distinctMtnsList.filter { it.mtnNumber == data }
            gateEntryData.mtnCode = mtn[0].mtnNumber
            gateEntryData.delivery = mtn[0].mtnNumber
            gateEntryData.deliveryItem = mtn[0].posnr
            val batchList = productList.filter { it.mtnNumber == mtn[0].mtnNumber }
            if (batchList.isNotEmpty()) {
                if (gateEntryData.weighBridgeType == STO) {
                    gateEntryData.materialCode =
                        batchList[0].materialNumber
                    gateEntryData.materialName = batchList[0].materialName
                    binding.tvUom.text = batchList[0].uom
                    gateEntryData.unitsOfMeasure = batchList[0].uom
                    binding.tvProduct.text = gateEntryData.materialCode.plus("-").plus(gateEntryData.materialName)
                }
                gateEntryData.batchNumber = batchList[0].batch
                gateEntryData.purchaseDocNum = batchList[0].purchaseOrder
                gateEntryData.purchaseDocDesc = batchList[0].ebelp
            }
            val commonId =
                gateEntryData.weighBridgeType.plus(gateEntryData.supplierCode).plus(gateEntryData.delivery)
            vm.getGateEntryDetails(commonId)

            if (mtn[0].mtntWbid.isNotEmpty()) {
                //gateEntryData.weighBridgeId = mtn[0].mtntWbid
                vm.getWeighBridgeIdDetail(mtn[0].mtntWbid, gateEntryData.imageString.equals(WEIGHSCALE))
            }

        } else if (isSupplier) {
            val split = data.split("-")
            val vendor = supplierList.filter { split[0] == it.vendorCode }
            if (!vendor.isNullOrEmpty()) {
                gateEntryData.supplierCode = vendor[0].vendorCode
                gateEntryData.supplierName = vendor[0].vendorName
                binding.tvSupplier.text = data
            }

        } else {
            binding.tvReceivingLocation.text = data
            val split = data.split("-")
            val item = custonLocationList.single { it.procureLocationCode == split[0] }
            gateEntryData.storageLocationCode = item.procureLocationCode
            gateEntryData.storageLocationName = item.procureLocationName
        }
    }

    private fun updateMandatory() {
        binding.tvProductLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.product)) { mandatoryStars() } }
        binding.tvSupplierLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.supplier)) { mandatoryStars() } }
        binding.tvSupplierZoneLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.supplier_zone)) { mandatoryStars() } }
        binding.tvDispatchWHLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.dispatch_wh)) { mandatoryStars() } }
        binding.tvObdLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.mtn_obd_number)) { mandatoryStars() } }
        binding.tvLocationLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.receiving_location)) { mandatoryStars() } }
        binding.tvDateLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.arrival_date)) { mandatoryStars() } }
        binding.tvDriverNameLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.driver_name)) { mandatoryStars() } }
        binding.tvTruckNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_no)) { mandatoryStars() } }
        binding.tvDriverNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.driver_no)) { mandatoryStars() } }
        binding.tvWeightLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.weight)) { mandatoryStars() } }
        binding.tvBagCountLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.declared_bag_count)) { mandatoryStars() } }
        binding.tvDeclaredWeightLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.declared_weight)) { mandatoryStars() } }
        binding.tvConnaissementLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.connaissement)) { mandatoryStars() } }

    }

    fun saveLocalCache() {
        if (!gateEntryData.commonPrimaryId.isNullOrBlank())
            vm.saveNewGateEntryData(gateEntryData)
    }

    private fun updateWeighbridgeValueUI(response: Resource<GenericReqAndResp<VegaReceiving>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                binding.tvDriverName.setText(response.data?.data?.truckDriverName, TextView.BufferType.EDITABLE)
                binding.tvPhoneNo.setText(response.data?.data?.contactNumber, TextView.BufferType.EDITABLE)
                binding.tvTruckNo.setText(response.data?.data?.vehicleNumber)
               // binding.tvReceivingLocation.text = response.data?.data?.dstorageLocationCode.plus("-").plus(response.data?.data?.dstorageLocationName)
                gateEntryData.vehicleNumber = response.data?.data?.vehicleNumber
                gateEntryData.driverName = response.data?.data?.driverName
                gateEntryData.driverName = response.data?.data?.truckDriverName
                gateEntryData.truckDriverName = response.data?.data?.truckDriverName
                gateEntryData.contactNumber = response.data?.data?.contactNumber
                gateEntryData.storageLocationCode = response.data?.data?.dstorageLocationCode
                gateEntryData.storageLocationName = response.data?.data?.dstorageLocationName

            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
            }
        }
    }

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {

    }
}
