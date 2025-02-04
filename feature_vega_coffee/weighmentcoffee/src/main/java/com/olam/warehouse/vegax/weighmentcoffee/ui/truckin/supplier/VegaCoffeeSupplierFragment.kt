package com.olam.warehouse.vegax.weighmentcoffee.ui.truckin.supplier

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCoffeeSingleSelectListener
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.weighmentcoffee.R
import com.olam.warehouse.vegax.weighmentcoffee.databinding.FragmentVegaCoffeeSupplierTruckinBinding
import com.olam.warehouse.vegax.weighmentcoffee.ui.VegaCoffeeMtnrSupplierViewModel
import com.olam.warehouse.vegax.weighmentcoffee.ui.VegaCoffeeReplaceCallback
import com.olam.warehouse.vegax.weighmentcoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList


class VegaCoffeeSupplierFragment : BaseFragment(), VegaSingleSelectListener,
    VegaCoffeeSingleSelectListener {

    private var receivingData = VegaReceiving()
    private var supplierList = mutableListOf<VegaVendor>()
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()

    private val vm: VegaCoffeeMtnrSupplierViewModel by viewModel()
    private lateinit var binding: FragmentVegaCoffeeSupplierTruckinBinding
    override val layoutResourceId = R.layout.fragment_vega_coffee_supplier_truckin
    private var callBack: VegaCoffeeReplaceCallback? = null
    private var materialList = emptyList<VegaMaterial>()
    private var suppliersNameList = ArrayList<String>()
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private var purchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var filterpurchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var yearFilter = mutableListOf<String>()
    private var year = ""
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeReplaceCallback
    }

    companion object {
        fun newInstance(receivingData: VegaReceiving) = VegaCoffeeSupplierFragment().putArgs {
            putParcelable(RECEIVING_DATA, receivingData)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCoffeeSupplierTruckinBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("weighmentcoffee/ui/truckin/supplier/VegaCoffeeSupplierFragment")
            .title("WeighmentCoffee")
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
        updateMandatory()
        binding.tvProduct.setText(receivingData.materialName, TextView.BufferType.EDITABLE)
        binding.tvSupplier.setText(receivingData.supplierName, TextView.BufferType.EDITABLE)
        //binding.tvTransVendor.text = receivingData.transportVendorCode
        binding.tvReceivingLocation.text = receivingData.storageLocationCode
        binding.tvTruckNo.setText(receivingData.vehicleNumber, TextView.BufferType.EDITABLE)
        binding.tvDriverName.setText(receivingData.truckDriverName, TextView.BufferType.EDITABLE)
        binding.tvPhoneNo.setText(receivingData.contactNumber, TextView.BufferType.EDITABLE)
        binding.etTruckBagcount.setText(receivingData.declaredBagCount?.trim(), TextView.BufferType.EDITABLE)
        binding.etTruckDeclaredweight.setText(receivingData.vendorDeclaredWeight)
        binding.etConnaissement.setText(receivingData.challan)
        binding.etCooperative.setText(receivingData.remarks)
        if(binding.etTruckDeclaredweight.text.isNotEmpty())
            binding.etTruckDeclaredweight.isEnabled = false
        if( binding.etTruckBagcount.text.isNotEmpty())
            binding.etTruckBagcount.isEnabled = false
        if( binding.etConnaissement.text.isNotEmpty())
            binding.etConnaissement.isEnabled = false
        if( binding.etCooperative.text.isNotEmpty())
            binding.etCooperative.isEnabled = false
        binding.etTruckGrossWeight.setText(
            if (receivingData.grossWeight.equals("0.000") || receivingData.grossWeight.equals("0")) "" else receivingData.grossWeight,
            TextView.BufferType.EDITABLE
        )
        disableFilledField()

        if (receivingData.weighBridgeId.isNotEmpty()) {
            vm.weighBridgeId.observe(viewLifecycleOwner, Observer { updateValueUI(it) })
            vm.getWeighBridgeIdDetail(receivingData.weighBridgeId)
        }

        //vm.getPOList()
        updateYearValues()
        //vm.poList.observe(viewLifecycleOwner, Observer { updatePurchaseOrderUI(it) })

        binding.tvPo.setOnClickListener {
            if (year.isNotEmpty())
                showSingleSelectDialog(
                    false,
                    getString(R.string.select_purchase_order),
                    false,
                    false, true
                )
        }

        binding.tvProduct.setOnClickListener {
            showSingleSelectDialog(
                true,
                getString(R.string.select_material),
                false,
                false, false
            )
        }
        binding.tvSupplier.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.select_material),
                true,
                false, false
            )
        }
        binding.tvYearValue.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.select_year),
                false,
                false, false, true
            )
        }

        binding.tvReceivingLocation.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.select_material),
                false,
                true, false
            )
        }
        vm.product.observe(viewLifecycleOwner, Observer {
            materialList = it
        })
        vm.getProducts()

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList.clear()
            suppliersNameList.clear()
            supplierList.addAll(it/*.filter { data -> data.bcApprover?.isNotEmpty()!! }*/)
            suppliersNameList.addAll(supplierList.map { data -> data.vendorCode.plus("-").plus(data.vendorName) })
        })
        vm.getSuppliers()

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            custonLocationList = it.filter { it.storageLocationType.equals("P") }.toMutableList()
            custonLocationList.forEach {
                if (it.procureLocationCode.equals(receivingData.storageLocationCode)) {
                    binding.tvReceivingLocation.text =
                        receivingData.storageLocationCode.plus("-").plus(it.procureLocationName)
                    receivingData.storageLocationName = it.procureLocationName
                }
            }
        })
        vm.getCustomLocations()

        binding.btnNtx.setOnClickListener { validateInputs() }

        vm.commonReceiving.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                receivingData = it
                updateLocalData()
            }
        })
    }

    private fun updateYearValues() {
        val current: Int = Calendar.getInstance().get(Calendar.YEAR)
        binding.tvYearValue.text = current.toString()
        year = current.toString()
        for (i in current.minus(1)..current) {
            yearFilter.add(i.toString())
        }
    }

    private fun updateLocalData() {
        binding.etTruckGrossWeight.setText(receivingData.grossWeight)
        binding.tvTruckNo.setText(receivingData.vehicleNumber)
        binding.tvDriverName.setText(receivingData.truckDriverName)
        binding.etTruckBagcount.setText(receivingData.bagCount?.trim())
        binding.etTruckDeclaredweight.setText(receivingData.vendorDeclaredWeight)
        binding.etConnaissement.setText(receivingData.challan)
        binding.etCooperative.setText(receivingData.remarks)
        if( binding.etTruckDeclaredweight.text.isNotEmpty())
            binding.etTruckDeclaredweight.isEnabled = false
        if( binding.etTruckBagcount.text.isNotEmpty())
            binding.etTruckBagcount.isEnabled = false
        if( binding.etConnaissement.text.isNotEmpty())
            binding.etConnaissement.isEnabled = false
        if( binding.etCooperative.text.isNotEmpty())
            binding.etCooperative.isEnabled = false
        binding.tvPhoneNo.setText(receivingData.contactNumber)
        binding.tvTransVendor.text = receivingData.transportVendorCode.plus("-").plus(receivingData.transportVendorName)
        binding.tvReceivingLocation.text =
            receivingData.storageLocationCode.plus("-").plus(receivingData.storageLocationName)
        binding.tvProduct.text = receivingData.materialName
    }

    private fun updateValueUI(response: Resource<GenericReqAndResp<VegaReceiving>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                binding.tvDriverName.setText(response.data?.data?.truckDriverName, TextView.BufferType.EDITABLE)
                binding.tvPhoneNo.setText(response.data?.data?.contactNumber, TextView.BufferType.EDITABLE)
                binding.etTruckBagcount.setText(response.data?.data?.bagCount?.trim(), TextView.BufferType.EDITABLE)
                binding.etTruckDeclaredweight.setText(response.data?.data?.vendorDeclaredWeight)
                binding.etConnaissement.setText(response.data?.data?.challan)
                binding.etCooperative.setText(response.data?.data?.remarks)
                if( binding.etTruckDeclaredweight.text.isNotEmpty())
                    binding.etTruckDeclaredweight.isEnabled = false
                if( binding.etTruckBagcount.text.isNotEmpty())
                    binding.etTruckBagcount.isEnabled = false
                if( binding.etConnaissement.text.isNotEmpty())
                    binding.etConnaissement.isEnabled = false
                if( binding.etCooperative.text.isNotEmpty())
                    binding.etCooperative.isEnabled = false
                disableFilledField()
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
            }
        }
    }

    private fun showSingleSelectDialog(
        isProduct: Boolean,
        title: String,
        isVendor: Boolean,
        isReceiving: Boolean, isPo: Boolean, isYear: Boolean = false
    ) {
        val list = ArrayList<String>()
        if (isProduct) {
            list.addAll(materialList.map { it.materialCode.plus("-").plus(it.materialName) })
        } else if (isVendor) {
            list.addAll(suppliersNameList)
        } else if (isReceiving) {
            val receivingLoc = custonLocationList.filter {
                !it.storageLocationType.equals(
                    "B"
                )
            }
            list.addAll(receivingLoc.map { data ->
                data.procureLocationCode.plus("-").plus(data.procureLocationName)
            })
        } else if (isPo) {
            filterpurchaseOrderList.clear()
            val purchaseOrderList =
                purchaseOrderList.filter { it.year == year.trim() }.filter { it.poType == PTBF }
                    .filter { it.materialNumber.contains(receivingData.materialCode ?: "  ") }
                    .filter { it.supplier.trim().contains(receivingData.supplierCode?.trim() ?: " ") }
            filterpurchaseOrderList.addAll(purchaseOrderList)
            list.addAll(filterpurchaseOrderList.map { data ->
                data.poId.plus("-").plus(data.openQuantity).plus(receivingData.unitsOfMeasure)
            })
        } else if (isYear) {
            list.addAll(yearFilter)
        }
        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isProduct, isVendor, isReceiving,
                list,
                activity!!,
                this, isReceiving, isYear, this,isOrigin = false,isDepartment = false
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {
    }

    private fun disableFilledField() {
        binding.tvProduct.isEnabled = binding.tvProduct.text.isEmpty()
        binding.tvSupplier.isEnabled = binding.tvSupplier.text.isEmpty()
    }

    private fun validateInputs() {
        when {
            receivingData.materialCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_product))
            receivingData.supplierName.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_supplier))
            binding.tvReceivingLocation.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_location))
            binding.tvTruckNo.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
            binding.tvDriverName.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver_name))
            binding.etTruckGrossWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_weight))
            //receivingData.purchaseDocNum.isNullOrEmpty() -> showSnack(getString(R.string.select_po_error))
            binding.etTruckBagcount.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_bagcount))
            binding.etTruckDeclaredweight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_declared))
            binding.etConnaissement.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_connaissement))
            else -> moveToSummary()
        }
    }

    private fun moveToSummary() {
        receivingData.commonPrimaryId =
            receivingData.materialCode.plus(receivingData.supplierCode).plus(receivingData.weighBridgeType)
        receivingData.grossWeight = binding.etTruckGrossWeight.text.toString()
        receivingData.vehicleNumber = binding.tvTruckNo.text.toString()
        receivingData.truckDriverName = binding.tvDriverName.text.toString()
        receivingData.contactNumber = binding.tvPhoneNo.text.toString()
        receivingData.declaredBagCount = binding.etTruckBagcount.text.toString()
        receivingData.vendorDeclaredWeight = binding.etTruckDeclaredweight.text.toString()
        receivingData.challan = binding.etConnaissement.text.toString()
        receivingData.remarks = binding.etCooperative.text.toString()
        receivingData.wsGate = WB01
        prepareSuccessData(
            if (receivingData.weighBridgeId.isEmpty()) getTmpId() else receivingData.weighBridgeId,
            false
        )
        vm.saveTruckInData(receivingData)
        callBack?.replaceMtntFragment(TRUCKIN_SUMMARYT_FRAG, receivingData.direction ?: "", receivingData)
    }

    private fun prepareSuccessData(wbId: String?, syncStatus: Boolean) {
//        receivingData.weighBridgeId = wbId.toString()
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
        binding.tvProductLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.product_type)) { mandatoryStars() } }
        binding.tvSupplierLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.supplier)) { mandatoryStars() } }
        binding.tvReceivingLocationLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.receiving_location)) { mandatoryStars() } }
        binding.tvTruckNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_number)) { mandatoryStars() } }
        binding.tvDriverNameLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.driver_name)) { mandatoryStars() } }
        binding.tvTruckInWeightLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_in_weight)) { mandatoryStars() } }
        binding.tvTruckInBagLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.declared_bag_count)) { mandatoryStars() } }
        binding.tvTruckDeclaredweightLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.declared_weight)) { mandatoryStars() } }
        binding.tvConnaissementLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.connaissement)) { mandatoryStars() } }
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

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean, isSupplier: Boolean,isOrigin:Boolean,isDepartment:Boolean) {
        customDialog?.dismiss()
        if (isWh) {
            val split = data.split("-")
            val item = materialList.single { split[0] == it.materialCode }
            binding.tvUom.text = item.unitsOfMeasure.toString()
            receivingData.unitsOfMeasure = item.unitsOfMeasure.toString()
            receivingData.materialCode = item.materialCode
            receivingData.materialName = item.materialName
            binding.tvProduct.text = data
        } else if (isVendor) {
            val split = data.split("-")
            val vendor = supplierList.filter { split[0] == it.vendorCode }
            if (!vendor.isNullOrEmpty()) {
                receivingData.supplierCode = vendor[0].vendorCode
                receivingData.supplierName = vendor[0].vendorName
                binding.tvSupplier.text = data
                val commonId =
                    receivingData.materialCode.plus(receivingData.supplierCode).plus(receivingData.weighBridgeType)
                vm.getReceivingByCommonId(commonId)
            }
        } else if (isGrade) {
            binding.tvReceivingLocation.text = data
            val split = data.split("-")
            binding.tvReceivingLocation.text = data
            val item = custonLocationList.single { it.procureLocationCode == split[0] }
            receivingData.storageLocationCode = item.procureLocationCode
            receivingData.storageLocationName = item.procureLocationName
        } else if (isSupplier) {
            year = data
            binding.tvYearValue.text = data
        } else {
            binding.tvPo.text = data
            val split = data.split("-")
            val item = filterpurchaseOrderList.singleOrNull { it.poId == split[0] }
            receivingData.purchaseDocNum = item?.poId
            receivingData.purchaseDocDesc = item?.ebelp
            receivingData.purchaseDocQty = item?.openQuantity
            receivingData.unitsOfMeasure = item?.meins ?: ""
        }
    }
}

