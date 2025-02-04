package com.olam.warehouse.vegax.receiving.ui.truckin.supplier

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
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.receiving.R
import com.olam.warehouse.vegax.receiving.databinding.FragmentVegaSupplierTruckinBinding
import com.olam.warehouse.vegax.receiving.ui.VegaReceivingViewModel
import com.olam.warehouse.vegax.receiving.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 1/28/2020.
 */
class VegaSupplierFragment : BaseFragment() {

    private var receivingData = VegaReceiving()
    private var supplierList = mutableListOf<VegaVendor>()
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()

    private val vm: VegaReceivingViewModel by viewModel()
    private lateinit var binding: FragmentVegaSupplierTruckinBinding
    override val layoutResourceId = R.layout.fragment_vega_supplier_truckin

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
        fun newInstance(receivingData: VegaReceiving) = VegaSupplierFragment().putArgs {
            putParcelable(RECEIVING_DATA, receivingData)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaSupplierTruckinBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("receiving/ui/truckin/supplier/VegaSupplierFragment").title("Receiving")
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
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnNtx, it, true)
        }
        receivingData = arguments?.getParcelable(RECEIVING_DATA)!!
        updateMandatory()
        binding.tvProduct.setText(receivingData.materialName, TextView.BufferType.EDITABLE)
        binding.tvSupplier.setText(receivingData.supplierName, TextView.BufferType.EDITABLE)
        //binding.tvTransVendor.text = receivingData.transportVendorCode
        binding.tvReceivingLocation.text = receivingData.storageLocationCode
        binding.tvTruckNo.setText(receivingData.vehicleNumber, TextView.BufferType.EDITABLE)
        binding.tvDriverName.setText(receivingData.driverName, TextView.BufferType.EDITABLE)
        binding.tvPhoneNo.setText(receivingData.contactNumber, TextView.BufferType.EDITABLE)
        binding.etTruckGrossWeight.setText(
            if (receivingData.grossWeight.equals("0.000") || receivingData.grossWeight.equals("0")) "" else receivingData.grossWeight,
            TextView.BufferType.EDITABLE
        )
        disableFilledField()

        if (receivingData.weighBridgeId.isNotEmpty()) {
            vm.weighBridgeId.observe(viewLifecycleOwner, Observer { updateValueUI(it) })
            vm.getWeighBridgeIdDetail(receivingData.weighBridgeId)
        }

        vm.product.observe(viewLifecycleOwner, Observer {
            val products = it.map { data -> data.materialName }
            it.forEach { it1 ->
                if (it1.materialName.equals("Ivory Coast Raw Cashew Nut")) {
                    receivingData.materialCode =
                        MATERIAL_CODE.plus(it1.materialCode.toString())
                    receivingData.materialName = it1.materialName.toString()
                    binding.tvProduct.setText(it1.materialName, TextView.BufferType.EDITABLE)
                    binding.tvUom.text = it1.unitsOfMeasure.toString()
                    receivingData.unitsOfMeasure = it1.unitsOfMeasure.toString()
                }
            }
            val productAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, products)
            binding.tvProduct.threshold = 1
            binding.tvProduct.setAdapter(productAdapter)
            binding.tvProduct.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                it.forEach { material ->
                    if (material.materialName == binding.tvProduct.text.toString()) {
                        receivingData.materialCode =
                            MATERIAL_CODE.plus(material.materialCode.toString())
                        receivingData.materialName = material.materialName.toString()
                        binding.tvUom.text = material.unitsOfMeasure.toString()
                        receivingData.unitsOfMeasure = material.unitsOfMeasure.toString()
                    }
                }
            }
        })
        vm.getProducts()

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList = it.toMutableList()
            val suppliers = it/*.filter { data -> data.bcApprover?.isNotEmpty()!! }
                */.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            val supplierAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suppliers)
            binding.tvSupplier.threshold = 1
            binding.tvSupplier.setAdapter(supplierAdapter)
            binding.tvSupplier.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                it.forEach { vendor ->
                    if (vendor.vendorCode == binding.tvSupplier.text.toString().split(" - ")[0]) {
                        receivingData.supplierCode = vendor.vendorCode
                        receivingData.supplierName = vendor.vendorName
                    }
                }
            }
        })
        vm.getSuppliers()

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            custonLocationList = it.toMutableList()
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
        //binding.tvTransVendor.setOnClickListener { showTransportVendorDialog(supplierList) }
        binding.tvReceivingLocation.setOnClickListener {
            showReceivingLocationDialog(custonLocationList.filter {
                !it.storageLocationType.equals(
                    "B"
                )
            })
        }
    }

    private fun updateValueUI(response: Resource<GenericReqAndResp<VegaReceiving>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                binding.tvDriverName.setText(response.data?.data?.driverName, TextView.BufferType.EDITABLE)
                binding.tvPhoneNo.setText(response.data?.data?.contactNumber, TextView.BufferType.EDITABLE)
                disableFilledField()
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
            }
        }
    }

    private fun disableFilledField() {
        binding.tvProduct.isEnabled = binding.tvProduct.text.isEmpty()
        binding.tvSupplier.isEnabled = binding.tvSupplier.text.isEmpty()
        /*binding.tvReceivingLocation.isEnabled = binding.tvReceivingLocation.text.isEmpty()
        binding.tvTruckNo.isEnabled = binding.tvTruckNo.text.isEmpty()
        binding.tvDriverName.isEnabled = binding.tvDriverName.text.isEmpty()
        binding.tvPhoneNo.isEnabled = binding.tvPhoneNo.text.isEmpty()
        binding.etTruckGrossWeight.isEnabled = binding.etTruckGrossWeight.text.isEmpty()*/
    }

    private fun showTransportVendorDialog(it: List<VegaVendor>) {
        val suppliers = it.map { data -> data.vendorName!! }
        MaterialDialog(requireContext()).show {
            title(R.string.trans_vendor_popup)
            listItemsSingleChoice(items = suppliers) { _, index, text ->
                binding.tvTransVendor.text = text
                receivingData.transportVendorCode = it[index].vendorCode
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun showReceivingLocationDialog(it: List<VegaCustomStLocation>) {
        val location = it.map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }
        MaterialDialog(requireContext()).show {
            title(R.string.receiving_location_popup)
            listItemsSingleChoice(items = location) { _, index, text ->
                binding.tvReceivingLocation.text = text
                receivingData.storageLocationCode = it[index].procureLocationCode
                receivingData.storageLocationName = it[index].procureLocationName
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun validateInputs() {
        when {
            receivingData.materialCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_product))
            receivingData.supplierName.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_supplier))
            binding.tvReceivingLocation.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_location))
            binding.tvTruckNo.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
            binding.tvDriverName.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver_name))
            binding.etTruckGrossWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_weight))
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
//        receivingData.weighBridgeId = wbId.toString()
        receivingData.tmpWbId = wbId ?: ""
        receivingData.erdat =
            if (receivingData.erdat.isNullOrEmpty()) "/Date(".plus(getCurrentTimeInMills().toString()).plus(")/") else receivingData.erdat
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
    }


}

