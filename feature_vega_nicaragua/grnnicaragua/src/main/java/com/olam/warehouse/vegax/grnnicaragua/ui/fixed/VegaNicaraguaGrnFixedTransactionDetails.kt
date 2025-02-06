package com.olam.warehouse.vegax.grnnicaragua.ui.fixed

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.ui.common.VegaTrackTraceFragment
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getFtdcValues
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.entity.TrackTraceFarmerModel
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaNicaraguaSingleSelectListener
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.hideKeyboard
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnnicaragua.R
import com.olam.warehouse.vegax.grnnicaragua.databinding.FragmentVegaNicaraguaGrnFixedTransactionDetailsBinding
import com.olam.warehouse.vegax.grnnicaragua.ui.VegaNicaraguaGrnViewModel
import com.olam.warehouse.vegax.grnnicaragua.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Keerthi Santhanam on 11/13/2020
 */
class VegaNicaraguaGrnFixedTransactionDetails : BaseFragment(), VegaNicaraguaSingleSelectListener,
    VegaSingleSelectListener {
    private var vendorList = mutableListOf<VegaVendor>()
    private var materialList = mutableListOf<VegaMaterial>()
    private var gradeList = mutableListOf<VegaQualitative>()
    private var materialQualityGradeList = mutableListOf<VegaNicaraguaMaterialQualitGrades>()
    private var locationList = mutableListOf<VegaStorageLocation>()
    private var purchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var receivingData = VegaReceiving()
    private var grnType: String? = ""
    private var receivingGrade: String? = ""
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null

    private lateinit var binding: FragmentVegaNicaraguaGrnFixedTransactionDetailsBinding
    private var callBack: CallBack? = null
    private val vm: VegaNicaraguaGrnViewModel by viewModel()
    private var transactionList = mutableListOf<VegaReceiving>()
    var ttProcurementType = ""
    var ttComplaintType = ""
    var ttFarmerDataList = java.util.ArrayList<TrackTraceFarmerModel>()
    var ttFarmerlessTransactionDetails = TrackTraceTransactionIdDetails()
    var isComplaint = false
    var vendorFlag = false
    var ttDbFarmerList = mutableListOf<VegaTrackTraceFarmerData>()

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            weighmentBagMaterialData: Any
        )

        fun replaceFragment(
            moveFrag: String,
            grnType: String,
            receivingData: VegaReceiving
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_grn_fixed_transaction_details

    companion object {
        fun newInstance(grnType: String, vegaReceiving: VegaReceiving, procurementType: String, complaintType: String) =
            VegaNicaraguaGrnFixedTransactionDetails().putArgs {
                putString(GRN_TYPE, grnType)
                putParcelable(GRN_DATA, vegaReceiving)
                putString(Constants.PROCUREMENT_TYPE, procurementType)
                putString(Constants.COMPLAINT_TYPE, complaintType)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaGrnFixedTransactionDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnnicaragua/ui/VegaNicaraguaGrnSpotTransactionDetails")
            .title("Nicaragua GRN")
            .with(tracker)
        initUI()
    }

    override fun onResume() {
        changeState(1, binding.stateBar.root, context)
        super.onResume()
    }

    private fun initUI() {
        arguments?.let {
            grnType = it.getString(GRN_TYPE)
            receivingData = it.getParcelable(GRN_DATA) ?: VegaReceiving()
            receivingGrade = receivingData.grade ?: ""
            ttProcurementType = arguments?.getString(Constants.PROCUREMENT_TYPE)?:""
            ttComplaintType = arguments?.getString(Constants.COMPLAINT_TYPE)?:""
            if(ttProcurementType.equals(Constants.DIRECT) || ttProcurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION)) {
                loadTTSupplierTypeFragment()
            }
            if (receivingData.supplierCode?.isNotEmpty() == true) {
                binding.tvVendor.text = receivingData.supplierCode.plus("-").plus(receivingData.supplierName)
                binding.tvVendor.isEnabled = false
            }
            if (receivingData.materialCode?.isNotEmpty() == true) {
                binding.tvMaterial.text = receivingData.materialName
                binding.tvMaterial.isEnabled = false
            }
            if (receivingData.grade?.isNotEmpty() == true) {
                binding.tvGrade.text = receivingData.grade.plus("-").plus(receivingData.gradeDesc)
                binding.tvGradeLabel.visible()
                binding.tvGrade.visible()
                binding.tvGrade.isEnabled = false
            }
            if (receivingData.purchaseDocNum?.isNotEmpty() == true) {
                binding.tvPoNumber.text = receivingData.purchaseDocNum
                binding.tvPoNumberLabel.visible()
                binding.tvPoNumber.visible()
                binding.tvPoNumber.isEnabled = false
            }
            if (receivingData.storageLocationCode?.isNotEmpty() == true) {
                binding.tvLocation.text =
                    receivingData.storageLocationCode.plus("-").plus(receivingData.storageLocationName)
            }
        }
        updateMandatory()
        vm.supplier.observe(viewLifecycleOwner, Observer {
            vendorList = it.toMutableList()
        })
        vm.getSuppliers("NI01")
        vm.product.observe(viewLifecycleOwner, Observer {
            val material = it.toMutableList()
            material.forEach {
                if (!it.materialName!!.contains("PTBF") && !it.materialName!!.contains("Tolling"))
                    materialList.add(it)
            }
        })
        vm.getProducts()

        vm.storageLocation.observe(viewLifecycleOwner, Observer {
            locationList = it.toMutableList()
        })
        vm.getStorageLocations()

        vm.getFarmerList()
        vm.farmerList.observe(viewLifecycleOwner, Observer {
            ttDbFarmerList = it.toMutableList()
        })

        vm.poList.observe(viewLifecycleOwner, Observer { response ->
            response.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        hideLoading()
                        when (it.data?.success) {
                            true -> {
                                response.data?.data?.let {
                                    val poList = it.toMutableList()
                                    poList.forEach { po ->
                                        if (po.poType == "Z001") purchaseOrderList.add(po)
                                    }

                                }
                            }
                            else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                        }
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                    }
                }
            }
        })
        vm.poListLocal.observe(viewLifecycleOwner, Observer {
            val poList = it.toMutableList()
            poList.forEach { po ->
                if (po.poType == "Z001") purchaseOrderList.add(po)
            }
        })
        if (AppUtils.isOnline()) vm.getPOList() else vm.getPOListLocal()

        vm.grade.observe(viewLifecycleOwner, Observer {
            gradeList = it.toMutableList()
        })

        vm.materialQualityGrades.observe(viewLifecycleOwner, Observer {
            materialQualityGradeList = it.toMutableList()
        })

        binding.tvVendor.setOnClickListener {
            if(ttProcurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION) && PreferenceHelper.get(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION, "").isNotEmpty() && PreferenceHelper.get(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION_ID, "").isNotEmpty() && ttFarmerlessTransactionDetails.dwTransactionId.isNullOrEmpty()){
                showSnack(getString(R.string.kindly_enter_the_transaction_id_first))
            }else {
                showSingleSelectDialog(false, getString(R.string.enter_vendor_name), true, false, false)
            }

        }
        binding.tvMaterial.setOnClickListener {
            showSingleSelectDialog(true, getString(R.string.select_material), false, false, false)
        }
        binding.tvGrade.setOnClickListener {
            showSingleSelectDialog(false, getString(R.string.select_grade), false, true, false)
        }
        binding.tvPoNumber.setOnClickListener {
            showSingleSelectDialog(false, getString(R.string.select_po_number), false, false, false)
        }
        binding.tvLocation.setOnClickListener {
            showSingleSelectDialog(false, getString(R.string.select_location), false, false, true)
        }
        binding.btnProceed.setOnClickListener { validateFields() }
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        val currentDateAndTime: String = simpleDateFormat.format(Date())
        binding.tvpostingdate.text = currentDateAndTime
        binding.tvpostingdate.setOnClickListener {
            getDatePickerDialog()
        }
        vm.grnTransList.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateTransUI(it) })
        vm.getReceivingWithLineItem()
    }

    private fun updateTransUI(it: List<VegaReceiving>?) {
        it?.let {
            transactionList.clear()
            transactionList.addAll(it)
        }
    }

    private fun getDatePickerDialog() {
        val cal = Calendar.getInstance()
        val DATE_FORMAT = "dd/MM/yyyy"
        val UTC = "UTC"
        context?.let {
            val datePicker = DatePickerDialog(
                it,
                com.olam.warehouse.presentation.R.style.DatePickerTheme,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val sdf = SimpleDateFormat(DATE_FORMAT)
                    sdf.timeZone = TimeZone.getTimeZone(UTC)
                    binding.tvpostingdate.text = sdf.format(cal.time)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            // datePicker.datePicker.maxDate = System.currentTimeMillis()
            datePicker.show()
            datePicker.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
    private fun showSingleSelectDialog(
        isMaterial: Boolean,
        title: String,
        isVendor: Boolean,
        isGrade: Boolean,
        isLocation: Boolean
    ) {
        var list: ArrayList<String>
        if (isVendor) {
            list = vendorList.filter { it.vendorCode.startsWith("1") }
                .map { it.vendorCode.plus("-").plus(it.vendorName) } as ArrayList<String>
        } else if (isMaterial) {
            if(ttProcurementType.equals(Constants.DIRECT)) {
                if (receivingData.supplierCode?.isEmpty() == true) {
                    if (vendorFlag) {
                        showSnack("Kindly select vendor")
                    } else {
                        showSnack("Kindly select farmer")
                    }
                    return
                }
            }
            if(ttProcurementType.equals(Constants.DIRECT) || ttProcurementType.equals(Constants.IN_DIRECT)|| ttProcurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION)) {
                if (isComplaint) {
                    list = materialList.filter { it.complainceFlag.equals(Constants.COMPLAINT) }
                        .map { it.materialCode.plus("-").plus(it.materialName) } as java.util.ArrayList<String>
                } else {
                    list = materialList.filter { it.complainceFlag.equals(Constants.NON_COMPLAINT) }
                        .map { it.materialCode.plus("-").plus(it.materialName) } as java.util.ArrayList<String>
                }
            } else {
//                list = materialList.filter { it.complainceFlag.equals(Constants.NON_COMPLAINT) }
//                    .map { it.materialCode.plus("-").plus(it.materialName) } as java.util.ArrayList<String>
                list = materialList.map { it.materialCode.plus("-").plus(it.materialName) } as java.util.ArrayList<String>

            }
            if(list.isEmpty()){
                val hasComplint = materialList.any { it.complainceFlag.equals(Constants.COMPLAINT) }
                val hasNonComplint = materialList.any { it.complainceFlag.equals(Constants.NON_COMPLAINT) }
                if(!hasComplint && !hasNonComplint)
                    list = materialList.map { it.materialCode.plus("-").plus(it.materialName) } as ArrayList<String>
                else
                    list = materialList.filter { it.complainceFlag.equals(Constants.NON_COMPLAINT) }
                        .map { it.materialCode.plus("-").plus(it.materialName) } as java.util.ArrayList<String>
            }
//            list = materialList.map { it.materialName } as ArrayList<String>
        } else if (isLocation) {
            list =
                locationList.map { it.storageLocationCode.plus("-").plus(it.storageLocationName) } as ArrayList<String>

        } else if (isGrade) {
            var gradeListFilter = mutableListOf<VegaQualitative>()

            materialQualityGradeList.forEach { qualityGrade ->
                gradeListFilter.addAll(gradeList.filter {
                    it.charValue.split(" ").get(it.charValue.split(" ").size - 1) == qualityGrade.gradeCode
                })
            }

            list = gradeListFilter.map { it.charValue.plus("-").plus(it.descValue) } as ArrayList<String>
        } else {
            list = purchaseOrderList.filter { it.supplier.contains(receivingData.supplierCode.toString()) }
                .filter { it.materialNumber.equals(receivingData.materialCode) }.map { it.poId } as ArrayList<String>
        }
        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isMaterial, isVendor, isGrade,
                list,
                requireActivity(),
                this,false,false,null,this,isLocation,false
            )

        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    private fun getQualityGrades() {
        binding.tvGradeLabel.visible()
        binding.tvGrade.visible()
        receivingData.grade = ""
        binding.tvPoNumberLabel.visible()
        binding.tvPoNumber.visible()
        binding.tvPoNumber.text = getString(R.string.select_po_number)
        receivingData.purchaseDocNum = ""
        binding.tvPoQuantity.gone()
        binding.tvPoQuantityLabel.gone()
        vm.getGrades(receivingData.materialCode.toString())
        vm.getMaterialQualityGrades(receivingData.materialCode.toString())
    }

    private fun validateFields() {
        when {
            receivingData.supplierCode.isNullOrEmpty() -> {
                showSnack(requireContext().resources.getString(R.string.enter_vendor_error))
            }
            receivingData.materialCode.isNullOrEmpty() -> {
                showSnack(requireContext().resources.getString(R.string.select_material_error))
            }
            receivingData.grade.isNullOrEmpty() || receivingData.grade.equals(getString(R.string.select_grade)) -> {
                showSnack(requireContext().resources.getString(R.string.select_grade_error))
            }
            receivingData.purchaseDocNum.isNullOrEmpty() || receivingData.purchaseDocNum.equals(getString(R.string.select_po_number)) -> {
                showSnack(requireContext().resources.getString(R.string.enter_po_number_error))
            }
            receivingData.storageLocationCode.isNullOrEmpty() || receivingData.storageLocationCode.equals(getString(R.string.select_location)) -> {
                showSnack(requireContext().resources.getString(R.string.enter_location_error))
            }
            else -> {
                receivingData.tmpWbId = if (receivingData.tmpWbId.isEmpty()) getTmpId() else receivingData.tmpWbId
                receivingData.batchNumber =
                    if (receivingData.batchNumber?.isEmpty() == true) vm.generateBatchNumber(transactionList) else receivingData.batchNumber
                receivingData.palletType =
                    if (receivingData.palletType?.isEmpty() == true) vm.generateGrnSequnceNumber(transactionList) else receivingData.palletType
                //receivingData.unitsOfMeasure = "KG"
                receivingData.weighBridgeType = "PROCURE"
                receivingData.item = "1"
                receivingData.postDate = binding.tvpostingdate.text.toString()
                receivingData.ttFarmerList = getTTFarmerDataList()
                receivingData.farmerTransDetails = getFarmerTransDetails()
                receivingData.eudrStatus = isComplaint
                receivingData.sourceLotId = receivingData.batchNumber.toString()
                if(ttProcurementType.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION)){
                    receivingData.farmerLessTransactionId = ttFarmerlessTransactionDetails.dwTransactionId.toString()
                    receivingData.eudrStatus = ttFarmerlessTransactionDetails?.compliantFlag ?: false
                }
                callBack?.replaceFragment(GRN_WEIGHMENT, GRN_FIXED, receivingData)
            }
        }
    }

    private fun getTTFarmerDataList(): java.util.ArrayList<TrackTraceFarmerModel> {
        if(ttFarmerDataList.isNotEmpty() && ttFarmerDataList.size > 0) {
            if (AppUtils.isOnline()) {
                if(ttFarmerDataList.size==1){
                    if(ttFarmerDataList.get(0).farmerWeight.isNullOrEmpty() || ttFarmerDataList.get(0).farmerWeight=="0"){
                        ttFarmerDataList.get(0).farmerWeight = receivingData.netWeight
                    }
                }
                ttFarmerDataList.forEach {
                    it.uom = receivingData.unitsOfMeasure
                }
                return ttFarmerDataList
            } else{
                ttFarmerDataList.forEach {
                    it.tmpWbId = receivingData.tmpWbId
                    if(it.farmerWeight.isNullOrEmpty())it.farmerWeight = receivingData.netWeight
                    it.uom = receivingData.unitsOfMeasure
                }
                return ttFarmerDataList
            }
        }
        return ttFarmerDataList
    }

    private fun getFarmerTransDetails(): TrackTraceFarmerTransDetails {
        var farmerTransData = TrackTraceFarmerTransDetails()
        farmerTransData.tmpWbId = receivingData.tmpWbId
        farmerTransData.netWeight = receivingData.netWeight
        farmerTransData.uom = receivingData.unitsOfMeasure
        farmerTransData.supplierCode = receivingData.supplierCode.toString()
        farmerTransData.supplierName = receivingData.supplierName.toString()
        return farmerTransData
    }

    private fun updateMandatory() {
        if(vendorFlag) {
            binding.tvVendorLabel.text =
                with(UIUtils) { with(requireContext().resources.getString(R.string.vendor)) { mandatoryStars() } }
        } else {
            binding.tvVendorLabel.text =
                with(UIUtils) { with(requireContext().resources.getString(R.string.farmer)) { mandatoryStars() } }
        }
        if(ttComplaintType.equals(Constants.NON_COMPLAINT) || ttComplaintType.isEmpty()){
            binding.tvVendorLabel.text =
                with(UIUtils) { with(requireContext().resources.getString(R.string.vendor)) { mandatoryStars() } }
        }
        binding.tvMaterialLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.material)) { mandatoryStars() } }
        binding.tvGradeLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.grade)) { mandatoryStars() } }
        binding.tvPoNumberLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.po_number)) { mandatoryStars() } }
    }

    override fun clickOnItem(
        data: String,
        isMaterial: Boolean,
        isVendor: Boolean,
        isGrade: Boolean,
        isLocation: Boolean
    ) {

        if (isVendor) {
            var vendors = vendorList.filter { it.vendorCode == data.split("-")[0] }
           /* if (vendors.get(0).vendorType.isNullOrEmpty()) {
                Toast.makeText(context, getString(R.string.invalid_vendor_error), Toast.LENGTH_SHORT).show()
            } else {*/
                binding.tvVendor.text = data
                receivingData.supplierCode = data.split("-")[0]
                receivingData.supplierName = data.split("-")[1]
//            if(ttProcurementType.equals(Constants.DIRECT)) {
//                validateFarmer(receivingData.supplierName ?: "")
//            }
                if (vendors.size > 0) receivingData.taxId = vendors.get(0).taxNumber
                binding.tvVendor.hideKeyboard()
                binding.tvPoNumber.text = getString(R.string.select_po_number)
                receivingData.purchaseDocNum = ""
                binding.tvPoQuantity.text = ""
                customDialog?.dismiss()
//            }
        } else if (isMaterial) {
//            var vendor = vendorList.filter { it.vendorCode==receivingData.supplierCode }.get(0)

//            var isValidMaterial:Boolean=true
            /* if ((!vendor.vendorType.isNullOrEmpty()) && vendor.vendorType!!.equals("NC", true)) {
                 if (data.contains("Certificado", true)) {
                     isValidMaterial = false
                 }
             }
             if (isValidMaterial) {*/
                customDialog?.dismiss()
            var selectedText = data.split("-")
            binding.tvMaterial.text = data
            materialList.forEach {
                if (it.materialCode.equals(selectedText[0])) {
                    receivingData.materialCode =
                        if (it.materialCode.length != 18) "000000".plus(it.materialCode) else it.materialCode
                    receivingData.materialName = it.materialName
                    receivingData.materialPrice = it.price
                    receivingData.grnType = getString(R.string.fixed)
                    if (data.isNotEmpty()) getQualityGrades()
                    when (it.productGroup) {
                        "ROBU" -> receivingData.ftdcValue = getFtdcValues(it.productGroup.toString())
                        "ARAB" -> receivingData.ftdcValue = getFtdcValues(it.productGroup.toString())
                    }
                }
            }
            /* } else {
                 Toast.makeText(
                     context,
                     getString(R.string.invalid_non_certified_material_error_msg),
                     Toast.LENGTH_SHORT
                 ).show()
             }*/
        } else if (isGrade) {
            customDialog?.dismiss()
            binding.tvGrade.text = data
            receivingData.grade = data.split("-")[0]
            receivingData.gradeDesc = data.split("-")[1]
            var gradeData = materialQualityGradeList.filter {
                it.gradeCode == receivingData.grade?.split(" ")?.get(receivingData.grade?.split(" ")!!.size - 1) ?: 0
            }.get(0)
            receivingData.bagTareWeight = gradeData.tareWeight
            receivingData.bagType = gradeData.bagType

        } else if (isLocation) {
            customDialog?.dismiss()
            binding.tvLocation.text = data
            receivingData.storageLocationCode = data.split("-")[0]
            receivingData.storageLocationName = data.split("-")[1]
        } else {
            customDialog?.dismiss()
            binding.tvPoNumber.text = data
            receivingData.purchaseDocNum = data
            purchaseOrderList.forEach {
                if (it.poId.equals(data)) {
                    binding.tvPoQuantity.text = it.openQuantity.plus(" ").plus(it.meins)
                    receivingData.purchaseDocQty = it.openQuantity
                    receivingData.unitsOfMeasure = it.meins
                    receivingData.price=it.unitPrice
                    binding.tvPoQuantityLabel.visible()
                    binding.tvPoQuantity.visible()
                }
            }
        }
    }

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {

    }

    private fun loadTTSupplierTypeFragment(){
        val bundle = Bundle()
        bundle.putString(
            Constants.PROCUREMENT_TYPE, ttProcurementType)
        displayFragment(VegaTrackTraceFragment.newInstance(bundle), false)
        if(ttProcurementType.equals(Constants.DIRECT)) {
            binding.tvVendorLabel.gone()
            binding.tvVendor.gone()
        }
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            "",
            allowStateLoss = true,
            containerViewId = R.id.flTTContainer,
            allowBackStack = flag
        )
    }

    fun replaceFragment(
        fragment: Fragment,
        tag: String?,
        allowStateLoss: Boolean = false,
        @IdRes containerViewId: Int,
        @AnimRes enterAnimation: Int = 0,
        @AnimRes exitAnimation: Int = 0,
        @AnimRes popEnterAnimation: Int = 0,
        @AnimRes popExitAnimation: Int = 0,
        allowBackStack: Boolean = false
    ) {
        val ft = activity?.supportFragmentManager
            ?.beginTransaction()
            ?.setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
        when {
            allowBackStack -> {
                ft?.add(containerViewId, fragment, tag)
                ft?.addToBackStack(tag)
            }
            else -> ft?.replace(containerViewId, fragment, tag)
        }
        if (!activity?.supportFragmentManager?.isStateSaved!!) {
            ft?.commit()
        } else if (allowStateLoss) {
            ft?.commitAllowingStateLoss()
        }
    }

//    fun isVendor(flag: Boolean){
//        binding.tvEudrStatus.gone()
//        if(flag) {
//            binding.tvVendorLabel.gone()
//            binding.tvVendor.gone()
//            vendorFlag = true
//        } else {
//            binding.tvVendorLabel.visible()
//            binding.tvVendor.visible()
//            vendorFlag = false
//        }
//    }

    fun updateFarmerListDetails(farmerList: java.util.ArrayList<TrackTraceFarmerModel>){
        resetMaterialData()
        ttFarmerDataList.clear()
        ttFarmerDataList.addAll(farmerList)
        if(ttFarmerDataList.isNotEmpty()) {
            receivingData.supplierCode = ttFarmerDataList.get(0).supplier?.split("-")?.get(0)?.trim()
            receivingData.supplierName = ttFarmerDataList.get(0).supplier?.split("-")?.get(1)
            var vendorList = vendorList.filter { it.vendorCode.equals(ttFarmerDataList.get(0).supplier?.split("-")?.get(0)?.trim()) }
            if (vendorList.size > 0) receivingData.taxId = vendorList.get(0).taxNumber
            var result = ttFarmerDataList.any { it.isComplaint == 0 }
            if(result){
                isComplaint = false
            }else{
                isComplaint = true
            }
        } else {
            isComplaint = false
        }
//        updateEudrStatus()

    }

//    fun updateEudrStatus(){
//        if(ttProcurementType.equals(Constants.DIRECT)) {
//            binding.tvEudrStatus.visible()
//            binding.tvEudrStatus.text =
//                if (isComplaint) "Eudr Status : ${Constants.COMPLAINT}" else "Eudr Status : ${Constants.UNKNOWN_ATTRIBUTE}"
//        }
//    }

    fun isComplaint(status:Int){
        if(status == 0){
            isComplaint = false
        } else {
            isComplaint = true
        }
    }

    private fun resetMaterialData(){
        binding.tvMaterial.text = ""
        receivingData.materialCode = ""
    }

    fun updateFarmerlessTransactionDetails(transactionIdDetails: TrackTraceTransactionIdDetails){
        resetMaterialData()
        isComplaint = transactionIdDetails?.compliantFlag?:false
        this.ttFarmerlessTransactionDetails = transactionIdDetails
        binding.tvVendor.text=""
        if(transactionIdDetails.vendorCode?.isNotEmpty() == true){
            var checkSupplier = vendorList.filter { it.vendorCode.equals(transactionIdDetails.vendorCode) }
            if(checkSupplier.isNullOrEmpty()){
                showOkDialog("The vendor details in the source lot is not available in SAP, do you want to continue?")
            } else {
                var data = checkSupplier.get(0).vendorCode.plus(" - ").plus(checkSupplier.get(0).vendorName)
                val split = data.split("-")
                binding.tvVendor.text = data
                receivingData.supplierCode = data.split("-")[0].trim()
                receivingData.supplierName = data.split("-")[1].trim()
            }
        } else {
            showOkDialog("The vendor details in the source lot is not available in SAP, do you want to continue?")
        }
    }

  /*  private fun validateFarmer(farmerName: String){
        var localList = mutableListOf<VegaTrackTraceFarmerData>()
        ttDbFarmerList.forEach {
            if(it.farmerName.contains(farmerName)){
                localList.add(it)
            }
        }
        if(localList.size > 1){
            chooseOneLotDialog(localList)
        } else if(localList.size == 1){
            if(localList.get(0).isComplaint == 1){
                isComplaint = true
            }
        } else{
            isComplaint = false
        }
//        updateEudrStatus()
    }

    @SuppressLint("CheckResult")
    private fun chooseOneLotDialog(farmerList: List<VegaTrackTraceFarmerData>) {
        val farmerItem = farmerList.map { it.farmerName.plus(" : ").plus(it.farmerId) }
        MaterialDialog(requireContext()).show {
            message(R.string.choose_farmer)
            cancelOnTouchOutside(false)
            cancelable(false)
            listItemsSingleChoice(items = farmerItem) { _, index, text ->
//                context.toast("dfedf")
                binding.tvVendor.text = text
                receivingData.supplierCode = text.split("-")[0]
                receivingData.supplierName = text.split("-")[1]
                if(farmerList.get(index).isComplaint == 1){
                    isComplaint = true
                } else {
                    isComplaint = false
                }
//                updateEudrStatus()
            }
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.login.R.string.ok),
                    true
                )
            )
        }
    }*/
}
