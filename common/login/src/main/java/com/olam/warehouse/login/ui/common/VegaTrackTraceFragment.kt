package com.olam.warehouse.login.ui.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.gson.Gson
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.FragmentSupplierTypeBinding
import com.olam.warehouse.login.databinding.ItemFarmerListBinding
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.login.vm.TrackTraceViewModel
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.TrackTraceFarmerModel
import com.olam.warehouse.master.vega.entity.TrackTraceSourceLotDetails
import com.olam.warehouse.master.vega.entity.TrackTraceTransactionIdDetails
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaTrackTraceFarmerData
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.TrackTraceModelTransactionIdDetails
import com.olam.warehouse.master.vega.model.TrackTraceScannerSourceLotDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiving
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.ui.widget.VegaTrackTraceSingleSelectDialogWithSearchWithEmptyListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.PURCHASE_TYPE
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.DataFormatException
import java.util.zip.Inflater


class VegaTrackTraceFragment : BaseFragment(), VegaSingleSelectCommonListener {
    override val layoutResourceId = R.layout.fragment_supplier_type
    private lateinit var binding: FragmentSupplierTypeBinding
    var procurementType = ""
    private var callBack: VegaTrackTraceListener? = null
    private val vm: TrackTraceViewModel by viewModel()
    private var supplierList = mutableListOf<VegaVendor>()
    private var supplierListText = ArrayList<String>()
    private var ttOfisFarmerList = mutableListOf<VegaTrackTraceFarmerData>()
    private var ttOfisfarmerListText = ArrayList<String>()
    private var customDialog: VegaTrackTraceSingleSelectDialogWithSearchWithEmptyListener? = null
    var farmerDataList = ArrayList<TrackTraceFarmerModel>()
    var vendorStatusFlag = false
    var ofisFarmerListDelimiters = "/"
    private var offloadData = VegaReceiving()
    private var purChaseType: String? = ""
    private var allSourceLotIdList = mutableListOf<TrackTraceSourceLotDetails>()
    private var allTransactionIdList = mutableListOf<TrackTraceTransactionIdDetails>()


    companion object {
        fun newInstance(bundle: Bundle?) = VegaTrackTraceFragment().putArgs {
            putBundle("BUNDLE_DATA", bundle)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaTrackTraceListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSupplierTypeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        updateMandatory(true)
    }

    private fun updateMandatory(farmerMandatory: Boolean) {
        binding.tvSupplierLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.supplier)) { mandatoryStars() } }
        if (farmerMandatory) {
            binding.tvFarmerLabel.text =
                with(UIUtils) { with(requireContext().resources.getString(R.string.farmer)) { mandatoryStars() } }
        } else {
            binding.tvFarmerLabel.text = getString(R.string.farmer)
        }
    }

    private fun switchUIUpdate(vendorStatus: Boolean) {
        if (vendorStatus) {
            binding.llManualEntryFarmer.visible()
            binding.rvFarmerList.visible()
            updateMandatory(false)
            vendorStatusFlag = true
            resetUI()
//            callBack?.isVendor(true)
        } else {
            binding.llManualEntryFarmer.gone()
            binding.rvFarmerList.gone()
            updateMandatory(true)
            resetUI()
//            callBack?.isVendor(false)
            vendorStatusFlag = false
        }

        callBack?.isVendor(vendorStatusFlag)
        callBack?.updateFarmerDetails(farmerDataList)

    }

    private fun initUI() {
        if (arguments?.getBundle("BUNDLE_DATA") != null) {
            val bundle = arguments?.getBundle("BUNDLE_DATA")
            procurementType = bundle?.getString(Constants.PROCUREMENT_TYPE) ?: ""
            purChaseType = bundle?.getString(PURCHASE_TYPE) ?: ""
            if (bundle?.containsKey(Constants.OFFLOADING) == true) {
                if ((getCurrentKey().contains("VEGA_NG") && getCurrentKey().contains("COCO"))
                ) {
                    val offloadDataTruck = bundle?.getParcelable<VegaOffloadingTrucks>(Constants.OFFLOADING)!!
                    offloadData = VegaReceiving(
                        supplierCode = offloadDataTruck.supplierCode,
                        supplierName = offloadDataTruck.supplierName
                    )

                }
                if ( (getCurrentKey().contains("VEGA_IV") && getCurrentKey().contains("COCO"))
                ) {
                    val offloadDataTruck = bundle?.getParcelable<VegaCoCoaReceiving>(Constants.OFFLOADING)!!
                    offloadData = VegaReceiving(
                        supplierCode = offloadDataTruck.supplierCode,
                        supplierName = offloadDataTruck.supplierName
                    )

                }
            }
        }
        if(offloadData.supplierCode?.isNotEmpty() == true){
            binding.tvSupplier.text = offloadData.supplierCode.plus("-").plus(offloadData.supplierName)
            binding.tvSupplier.isEnabled=false
            binding.tvSupplier.isClickable=false
        }
        toggleView()
        binding.rgSupplierType.setOnCheckedChangeListener { radioGroup, id ->
            var radioButton = view?.findViewById<RadioButton>(id)!!
            when (id) {
                R.id.rbFarmer -> {
                    switchUIUpdate(false)
                }

                R.id.rbVendor -> {
                    switchUIUpdate(true)
                }
            }
        }
        binding.rgSupplierType.checkedRadioButtonId
        vm.sourceLotIdDetails.observe(viewLifecycleOwner, Observer {
            updateSourceLotData(it)
        })

        vm.onlineTransactionIdDetails.observe(viewLifecycleOwner, Observer {
            updateOnlineTransactionIdData(it)
        })
        vm.offlineTransactionIdDetails.observe(viewLifecycleOwner, Observer {
            if(it!=null) {
                updateTransactionIdDetails(it)
            }else{
                showSnack(getString(R.string.entered_transaction_id_is_not_available))
            }
        })
        vm.offlineSourceLotIdDetails.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                updateSourceLotDetails(it)
            } else {
                binding.llIndirectEudrStatus.gone()
                showSnack(getString(R.string.no_data_available))
            }
        })

        if(PreferenceHelper.get(Constants.SOURCE_LOT_SYNC,"").isNotEmpty()){
            getAllSourceLotIdList()
//            binding.btnSrcLotProceed.gone()
            binding.ivSourceLotIdList.visible()
//            binding.etSourceLotId.isEnabled = false
        }

        if(PreferenceHelper.get(Constants.TRANS_ID_SYNC,"").isNotEmpty()){
            getAllTransIdList()
//            binding.btnSrcLotProceed.gone()
            binding.ivSourceLotIdList.visible()
//            binding.etSourceLotId.isEnabled = false
        }

        vm.getSuppliers(purChaseType)
        vm.supplier.observe(viewLifecycleOwner, Observer {
            supplierList = it.toMutableList()
            if(getCurrentKey().contains("NI")){
                supplierList = supplierList.filter { it.vendorCode.startsWith("1") } as MutableList
            }
            val suppliers = supplierList.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            supplierListText = suppliers as ArrayList<String>
        })

        vm.getFarmerList()
        vm.farmerList.observe(viewLifecycleOwner, Observer {
            ttOfisFarmerList = it.toMutableList()
            val farmer = it.map { data ->
                if (data.uom.isNullOrEmpty() && data.cropLimit.toString().isNullOrEmpty()) {
                    data.farmerId.plus(ofisFarmerListDelimiters).plus(data.farmerName)
                }else if(data.uom.isNullOrEmpty()){
                    data.farmerId.plus(ofisFarmerListDelimiters).plus(data.farmerName).plus(ofisFarmerListDelimiters)
                        .plus(data.cropLimit)
                } else {
                    data.farmerId.plus(ofisFarmerListDelimiters).plus(data.farmerName).plus(ofisFarmerListDelimiters)
                        .plus(data.cropLimit).plus(ofisFarmerListDelimiters).plus(data.uom)
                }
            }

            ttOfisfarmerListText = farmer as ArrayList<String>
        })

        vm.farmerDataEudrDetail.observe(viewLifecycleOwner, Observer {
            generateFarmerData(it)
        })


        binding.btnSrcLotProceed.setOnClickListener {
            if (procurementType.equals(Constants.IN_DIRECT)) {
                if (binding.etSourceLotId.text.trim().toString()
                        .isNotEmpty()
                ) getSourceLotIdDetails() else showSnack(getString(R.string.kindly_enter_source_lot_id))
            }
            else if (procurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION)) {
                if (binding.etSourceLotId.text.trim().toString()
                        .isNotEmpty()
                ) getTransactionIdDetails() else showSnack(getString(R.string.kindly_enter_trans_lot_id))
            }
        }
        binding.ivSourceLotIdList.setOnClickListener {
            if (procurementType.equals(Constants.IN_DIRECT)) {
                showSingleSelectDialog(getString(R.string.select_source_lot), Constants.SOURCE_LOT)
            } else if (procurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION)){
                showSingleSelectDialog(getString(R.string.select_transaction_id), Constants.TRANS_ID)
            }
        }
        binding.llScan.setOnClickListener { moveToScan() }
        binding.btnAdd.setOnClickListener { validateTTFarmerData() }
        binding.tvFarmer.setOnClickListener {
            if (binding.tvSupplier.text.isNotEmpty()) {
                showSingleSelectDialog(getString(R.string.select_farmer), Constants.FARMER)
            } else {
                showSnack(getString(R.string.kindly_select_supplier))
            }
        }
        binding.tvSupplier.setOnClickListener { showSingleSelectDialog(getString(R.string.select_vendor), Constants.VENDOR) }

    }

    private fun getTransactionIdDetails(){
        if (AppUtils.isOnline()) {
            vm.getOnlineTransactionIdDetails(binding.etSourceLotId.text.trim().toString())
        } else {
            vm.getOfflineTransactionIdDetails(binding.etSourceLotId.text.trim().toString())
        }
    }

    private fun getSourceLotIdDetails() {
        if (AppUtils.isOnline()) {
            vm.getSourceLotIdDetails(getPlantDetails().plantId, binding.etSourceLotId.text.trim().toString())
        } else {
            vm.getOfflineSourceLotDetails(binding.etSourceLotId.text.trim().toString())
        }
    }

    private fun getAllSourceLotIdList(){
        vm.getAllSourceLotIdList()
        vm.allSourceLotIdList.observe(viewLifecycleOwner, Observer{
            if (it != null && it.size >0 ) {
                allSourceLotIdList = it as MutableList
            }
        })
    }

    private fun getAllTransIdList(){
        vm.getAllTransactionIdList()
        vm.allTransactionIdList.observe(viewLifecycleOwner, Observer{
            if (it != null && it.size >0 ) {
                allTransactionIdList = it as MutableList
            }
        })
    }

    private fun updateOnlineTransactionIdData(response: Resource<GenericReqAndResp<List<TrackTraceModelTransactionIdDetails>>>){
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.data?.get(0)?.let { it1 ->
                    updateTransactionIdDetails(prepareTransactionIdData(it1))
                }
            }

            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun prepareTransactionIdData(transactionIdDetails: TrackTraceModelTransactionIdDetails): TrackTraceTransactionIdDetails {
        var data = TrackTraceTransactionIdDetails()
        if(transactionIdDetails.vendorDetails.isNotEmpty()) {
            var vendorDetails = transactionIdDetails.vendorDetails.get(0)
            data.dwTransactionId = transactionIdDetails.dwTransactionId
            data.vendorName = vendorDetails.vendorName
            data.vendorCode = vendorDetails.vendorCode
            data.country = vendorDetails.country
            data.product = vendorDetails.product
            data.totalProductionInMetricTon = vendorDetails.totalProductionInMetricTon
            data.dateGeoLocationCaptured = vendorDetails.dateGeoLocationCaptured
            data.compliantFlag = vendorDetails.compliantFlag
        }
        return data
    }

    private fun updateTransactionIdDetails(transactionIdDetails: TrackTraceTransactionIdDetails){
        binding.llIndirectEudrStatus.visible()
//        var vendorDetails = transactionIdDetails.vendorDetails.get(
        if (transactionIdDetails.compliantFlag == true) {
            updateInDirectEudrStatus(true)
//            binding.tvIndirectEudrStatus.setText(Constants.EUDR_STATUS_COLON.plus(Constants.COMPLAINT))
        } else {
//            binding.tvIndirectEudrStatus.setText(Constants.EUDR_STATUS_COLON.plus(Constants.ATTR_UNKNOWN_QP_VALUE))
            updateInDirectEudrStatus(false)
        }
        binding.tvLotSourceId.setText(getString(R.string.colon_transaction_id).plus(transactionIdDetails.dwTransactionId))
        callBack?.updateTransactionIdDetails(transactionIdDetails)
    }

    private fun toggleView() {
        if (procurementType.equals(Constants.DIRECT)) {
            binding.llDirectProcurement.visible()
        } else if (procurementType.equals(Constants.IN_DIRECT)) {
            binding.llInDirectProcurement.visible()
        } else if (procurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION)) {
            binding.llInDirectProcurement.visible()
            binding.llScan.gone()
            binding.etSourceLotId.setHint(getString(R.string.enter_transaction_id))
        }
    }

    private fun showSingleSelectDialog(title: String, currentFlag: String) {
        var list = ArrayList<String>()
        var searchListEmptyListener = ""
        when (currentFlag) {
            Constants.VENDOR -> {
                list = supplierListText
                searchListEmptyListener = ""
            }

            Constants.FARMER -> {
                list = ttOfisfarmerListText
                if (vendorStatusFlag) searchListEmptyListener = ""
                else searchListEmptyListener =
                    getString(R.string.farmer_not_available)
            }

            Constants.SOURCE_LOT -> {
                list = allSourceLotIdList.map { it.sourceLotId } as ArrayList<String>
            }

            Constants.TRANS_ID -> {
                list = allTransactionIdList.map { it.dwTransactionId } as ArrayList<String>
            }

        }
        customDialog =
            VegaTrackTraceSingleSelectDialogWithSearchWithEmptyListener(
                title,
                currentFlag,
                list,
                requireActivity(),
                this,
                searchListEmptyListener
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, currentFlag: String) {
        customDialog?.dismiss()
        when (currentFlag) {
            Constants.VENDOR -> {
                resetOnlyFarmerValues()
                binding.tvSupplier.setText(data)
                if (!vendorStatusFlag) {
                    validateFarmer(data.split(" - ")[1])
                } else generateEmptyFarmerData()

            }

            Constants.FARMER -> {
                if (data.isNotEmpty()) {
                    binding.tvFarmer.setText(data)
                    if (!vendorStatusFlag) {
                        vm.getFarmerEudrDetails(
                            binding.tvFarmer.text.toString().trim().split(ofisFarmerListDelimiters)[0]
                        )
                    } else {
                        disableManualEntryFarmerName(false)
                    }
                } else {
                    binding.tvFarmer.setText(getString(R.string.farmer_not_listed))
                    generateManualFarmerData()
                }
            }

            Constants.SOURCE_LOT -> {
                if(data.isNotEmpty()){
                    binding.etSourceLotId.setText(data.trim())
                    var sourceLotData = allSourceLotIdList.singleOrNull { it.sourceLotId?.equals(data.trim()) == true }
                    sourceLotData?.let { updateSourceLotDetails(it) }
                }
            }

            Constants.TRANS_ID -> {
                if(data.isNotEmpty()){
                    binding.etSourceLotId.setText(data.trim())
                    var transIdData = allTransactionIdList.singleOrNull { it.dwTransactionId?.equals(data.trim()) == true }
                    transIdData?.let { updateTransactionIdDetails(it) }
                }
            }
        }

    }

    private fun disableManualEntryFarmerName(status: Boolean){
        if(status){
            binding.etFarmerName.visible()
        } else {
            binding.etFarmerName.gone()
        }
    }

    private fun validateTTFarmerData() {
        if (binding.tvSupplier.text.toString().trim().isEmpty()) {
            showSnack(getString(R.string.kindly_select_supplier))
            return
        }
//        if (binding.tvFarmer.text.toString().trim().isEmpty() && binding.etFarmerName.text.toString().trim()
//                .isEmpty()
//        ) {
//            showSnack("Kindly select/enter farmer")
//            return
//        }
//        if (binding.etWeight.text.toString().trim().isEmpty()) {
//            showSnack("Kindly enter weight")
//            return
//        }
        if (binding.tvFarmer.text.toString().trim().isNotEmpty()) {
            vm.getFarmerEudrDetails(binding.tvFarmer.text.toString().trim().split(ofisFarmerListDelimiters)[0])
        } else {
            generateManualFarmerData()
        }

    }

    private fun generateFarmerData(farmerEudrdata: VegaTrackTraceFarmerData) {
        var farmerData = TrackTraceFarmerModel()
        farmerData.supplier = binding.tvSupplier.text.toString().trim()
        farmerData.farmer = binding.tvFarmer.text.toString().trim()
        farmerData.farmerWeight = if(binding.etWeight.text.toString().isEmpty())"0" else binding.etWeight.text.toString()
        farmerData.id = farmerEudrdata.id
        farmerData.countryId = farmerEudrdata.countryId
        farmerData.productId = farmerEudrdata.productId
//        farmerData.countryName = farmerEudrdata.countryName
        farmerData.farmerId = farmerEudrdata.farmerId
        farmerData.farmerName = farmerEudrdata.farmerName
        farmerData.farmerGroupName = farmerEudrdata.farmerGroupName
        farmerData.farmerGroupId = farmerEudrdata.farmerGroupId
//        farmerData.productName = farmerEudrdata.productName
        farmerData.isComplaint = farmerEudrdata.isComplaint
        farmerData.isActive = farmerEudrdata.active
        farmerData.createdAt = farmerEudrdata.createdAt
        farmerData.createdBy = farmerEudrdata.createdBy
        farmerData.updatedAt = farmerEudrdata.updatedAt
        farmerData.updatedBy = farmerEudrdata.updatedBy
        farmerData.cropLimit = farmerEudrdata.cropLimit
        farmerData.uomFromOfis = farmerEudrdata.uom
        if (!vendorStatusFlag) {
            /*Only for vendor flow multiple farmer list is allowed, so we are deleting for farmer flow*/
            farmerDataList.clear()
        }
        farmerDataList.add(farmerData)
        callBack?.updateFarmerDetails(farmerDataList)

        updateDirectEudrStatus(if (farmerEudrdata.isComplaint == 1) true else false)

        if (vendorStatusFlag) {
            if(farmerDataList.isNotEmpty()){
                val result = farmerDataList.any { it.isComplaint == 0 }
                if (result) {
                    updateDirectEudrStatus(false)
                } else {
                    updateDirectEudrStatus(true)
                }
            }
            setUpAdapter(farmerDataList)
            clearFarmerSupplierValues()
        }
    }

    private fun generateManualFarmerData() {
        var farmerData = TrackTraceFarmerModel()
        farmerData.supplier = binding.tvSupplier.text.toString().trim()
        farmerData.farmer = binding.etFarmerName.text.toString().trim()
        farmerData.farmerWeight = binding.etWeight.text.toString()
        farmerData.farmerName = binding.etFarmerName.text.toString()
        farmerData.isComplaint = 0
        if (!vendorStatusFlag) {
            /*Only for vendor flow multiple farmer list is allowed, so we are deleting for farmer flow*/
            farmerDataList.clear()
        }
        farmerDataList.add(farmerData)
        updateDirectEudrStatus(false)
        callBack?.updateFarmerDetails(farmerDataList)
        if (vendorStatusFlag) {
            setUpAdapter(farmerDataList)
            clearFarmerSupplierValues()
        }
    }

    private fun generateEmptyFarmerData() {
        var farmerData = TrackTraceFarmerModel()
        farmerData.supplier = binding.tvSupplier.text.toString().trim()
        farmerData.farmer = binding.tvSupplier.text.toString().trim().split("-").get(1)
        farmerData.isComplaint = 0
        updateDirectEudrStatus(false)
        callBack?.updateFarmerDetails(arrayListOf(farmerData))
    }

    private fun clearFarmerSupplierValues() {
        binding.tvFarmer.setText("")
//        binding.tvSupplier.setText("")
        binding.etWeight.setText("")
        binding.etFarmerName.setText("")
    }

    private fun resetUI() {
        if(offloadData.supplierCode?.isEmpty() == true) {
            binding.tvSupplier.setText("")
        } else{
            binding.tvSupplier.isEnabled=false
            binding.tvSupplier.isClickable=false
        }

        binding.tvFarmer.setText("")
        binding.etWeight.setText("")
        binding.etFarmerName.setText("")
//        binding.tvDirectEudrStatusValue.setText("")
        binding.DirectEudrStatus.llEudrStatus.gone()
        farmerDataList.clear()
        enableVendor()
    }

    private fun resetOnlyFarmerValues() {
        binding.tvFarmer.setText("")
//        binding.tvDirectEudrStatus.setText("")
        binding.DirectEudrStatus.llEudrStatus.gone()
    }

    fun updateDirectEudrStatus(isComplaint: Boolean) {
        if (procurementType.equals(Constants.DIRECT)) {
//            binding.tvDirectEudrStatus.visible()
            binding.DirectEudrStatus.llEudrStatus.visible()
            if (isComplaint) {
                binding.DirectEudrStatus.tvEudrStatusValue.text = Constants.EUDR_QP_VALUE
                binding.DirectEudrStatus.llEudrStatus.setBackground(resources.getDrawable(R.drawable.rounded_corners_green))
                binding.DirectEudrStatus.ivEudrFlag.setImageResource(R.drawable.ic_eudr_complaint_flag)
            } else {
                binding.DirectEudrStatus.tvEudrStatusValue.text = Constants.ATTR_UNKNOWN_QP_VALUE
                binding.DirectEudrStatus.llEudrStatus.setBackground(resources.getDrawable(R.drawable.rounded_corners_red))
                binding.DirectEudrStatus.ivEudrFlag.setImageResource(R.drawable.ic_attr_unknown_flag)
            }
        }
    }

    fun updateInDirectEudrStatus(isComplaint: Boolean) {
        binding.IndirectEudrStatus.llEudrStatus.visible()
        if (isComplaint) {
            binding.IndirectEudrStatus.tvEudrStatusValue.text = Constants.EUDR_QP_VALUE
            binding.IndirectEudrStatus.llEudrStatus.setBackground(resources.getDrawable(R.drawable.rounded_corners_green))
            binding.IndirectEudrStatus.ivEudrFlag.setImageResource(R.drawable.ic_eudr_complaint_flag)
//            binding.tvIndirectEudrStatus.setText(Constants.EUDR_STATUS_COLON.plus(Constants.COMPLAINT))
        } else {
//            binding.tvIndirectEudrStatus.setText(Constants.EUDR_STATUS_COLON.plus(Constants.ATTR_UNKNOWN_QP_VALUE))
            binding.IndirectEudrStatus.tvEudrStatusValue.text = Constants.ATTR_UNKNOWN_QP_VALUE
            binding.IndirectEudrStatus.llEudrStatus.setBackground(resources.getDrawable(R.drawable.rounded_corners_red))
            binding.IndirectEudrStatus.ivEudrFlag.setImageResource(R.drawable.ic_attr_unknown_flag)
        }
    }

    private fun enableVendor() {
        if (farmerDataList.size > 0) {
            binding.tvSupplier.isEnabled = false
//            binding.tvSupplier.setBackgroundColor(com.olam.warehouse.login.R.color.text_attempts)
        } else {
            binding.tvSupplier.isEnabled = true
//            binding.tvSupplier.setBackgroundColor(com.olam.warehouse.presentation.R.color.warm_white)
        }
    }


    private fun setUpAdapter(data: ArrayList<TrackTraceFarmerModel>) {
        enableVendor()
        disableManualEntryFarmerName(true)
        if (data != null) {
            binding.rvFarmerList.setUpAdapter(
                data,
                R.layout.item_farmer_list,
                ItemFarmerListBinding::inflate,
                { it, pos, bindingItem ->
                    var farmerData = data.get(pos)
                    bindingItem.tvSupplierId.setText(farmerData.supplier?.split("-")?.get(0) ?: "")
                    bindingItem.tvSupplierName.setText(farmerData.supplier?.split("-")?.get(1) ?: "")
                    if (farmerData.farmerId.isEmpty()) {
                        bindingItem.tvFarmerName.setText(if (farmerData?.farmerName?.isNotEmpty() == true) farmerData?.farmerName else Constants.NO_DATA_VALUE)
                        bindingItem.tvFarmerId.setText(Constants.NO_DATA_VALUE)
                    } else {
                        bindingItem.tvFarmerName.setText(
                            farmerData.farmer?.split(ofisFarmerListDelimiters)?.get(1) ?: ""
                        )
                        bindingItem.tvFarmerId.setText(farmerData.farmer?.split(ofisFarmerListDelimiters)?.get(0) ?: "")
                    }
                    bindingItem.tvFarmerWeight.setText(if (farmerData?.farmerWeight?.isNotEmpty() == true) farmerData?.farmerWeight else Constants.NO_DATA_VALUE)
                    bindingItem.ivDeleteData.setOnClickListener {
                        farmerDataList.removeAt(pos)

                        binding.rvFarmerList.adapter?.notifyDataSetChanged()
                        var result = farmerDataList.any { it.isComplaint == 0 }
                        if (result) {
                            updateDirectEudrStatus(false)
                        } else {
                            updateDirectEudrStatus(true)
                        }
                        if (farmerDataList.size == 0) {
                            enableVendor()
                            resetOnlyFarmerValues()
                        }
                        callBack?.updateFarmerDetails(farmerDataList)

                    }
                    if (farmerData.isComplaint == 1) {
                        bindingItem.tvEudrStatus.setText(Constants.EUDR_QP_VALUE)
                    } else {
                        bindingItem.tvEudrStatus.setText(Constants.ATTR_UNKNOWN_QP_VALUE)
                    }
                }, {

                })
        }
    }

    private fun updateSourceLotData(response: Resource<GenericReqAndResp<List<TrackTraceSourceLotDetails>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.data?.let {it->
                    if(it.size>0) {
                        updateSourceLotDetails(it.get(0))
                    } else {
                        showSnack(getString(R.string.no_data_available))
                    }

                }
            }

            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun updateSourceLotDetails(sourceLotData: TrackTraceSourceLotDetails) {
        binding.llIndirectEudrStatus.visible()
        if (sourceLotData.isEudrComplaintFlag == true) {
            updateInDirectEudrStatus(true)
//            binding.tvIndirectEudrStatus.setText(Constants.EUDR_STATUS_COLON.plus(Constants.COMPLAINT))
        } else {
//            binding.tvIndirectEudrStatus.setText(Constants.EUDR_STATUS_COLON.plus(Constants.ATTR_UNKNOWN_QP_VALUE))
            updateInDirectEudrStatus(false)
        }
        binding.tvLotSourceId.setText(getString(R.string.source_lot_id_colon).plus(sourceLotData.sourceLotId))
        callBack?.updateSourceLotDetails(sourceLotData)
    }


    private fun moveToScan() {
        val intent = Intent(requireContext(), ScannerActivity::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    try {
                        var scannerData = Gson().fromJson(decompress(it), TrackTraceScannerSourceLotDetails::class.java)
                        var convertedData = TrackTraceSourceLotDetails()
                        convertedData.vendorCode = scannerData.vendorId
                        convertedData.isEudrComplaintFlag = scannerData.complianceIndicator
                        convertedData.sourceLotId = scannerData.sourceLotId
                        updateSourceLotDetails(convertedData)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    @Throws(IOException::class, DataFormatException::class)
    fun decompress(response: String?): String {
        try {
        val decodedString: ByteArray = Base64.decode(response, Base64.NO_WRAP)
        val inflater = Inflater()
        inflater.setInput(decodedString)
        val outputStream = ByteArrayOutputStream(decodedString.size)
        val buffer = ByteArray(1024)
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        outputStream.close()
        val output = outputStream.toByteArray()
        return String(output, charset("UTF-8"))
        } catch (e:Exception) {
            e.printStackTrace()
            return response ?: ""
        }
    }

    private fun validateFarmer(farmerName: String) {
        var localList = mutableListOf<VegaTrackTraceFarmerData>()
        ttOfisFarmerList.forEach {
            if (it.farmerName.contains(farmerName)) {
                localList.add(it)
            }
        }
        if (localList.size > 1) {
            chooseOneLotDialog(localList)
        } else if (localList.size == 1) {
            binding.tvFarmer.setText(
                localList.get(0).farmerId.plus(ofisFarmerListDelimiters)
                    .plus(localList.get(0).farmerName)
            )
            generateFarmerData(localList.get(0))
        }

    }

    @SuppressLint("CheckResult")
    private fun chooseOneLotDialog(farmerList: List<VegaTrackTraceFarmerData>) {
        val farmerItem = farmerList.map { it.farmerName.plus(ofisFarmerListDelimiters).plus(it.farmerId) }
        MaterialDialog(requireContext()).show {
            message(R.string.choose_farmer)
            cancelOnTouchOutside(false)
            cancelable(false)
            listItemsSingleChoice(items = farmerItem) { _, index, text ->
//                context.toast("dfedf")
                binding.tvFarmer.text = text
                generateFarmerData(farmerList.get(index))
//                receivingData.supplierCode = text.split("-")[0]
//                receivingData.supplierName = text.split("-")[1]
//                if (farmerList.get(index).isComplaint == 1) {
//                    isComplaint = true
//                } else {
//                    isComplaint = false
//                }
//                updateEudrStatus()
            }
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.login.R.string.ok),
                    true
                )
            )
        }
    }
}
