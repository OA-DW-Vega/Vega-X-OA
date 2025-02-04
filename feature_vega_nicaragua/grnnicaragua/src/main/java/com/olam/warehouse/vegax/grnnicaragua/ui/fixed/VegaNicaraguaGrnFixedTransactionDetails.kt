package com.olam.warehouse.vegax.grnnicaragua.ui.fixed

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaNicaraguaSingleSelectListener
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.AppUtils
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
        fun newInstance(grnType: String, vegaReceiving: VegaReceiving) =
            VegaNicaraguaGrnFixedTransactionDetails().putArgs {
                putString(GRN_TYPE, grnType)
                putParcelable(GRN_DATA, vegaReceiving)
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

    private fun initUI() {
        arguments?.let {
            grnType = it.getString(GRN_TYPE)
            receivingData = it.getParcelable(GRN_DATA) ?: VegaReceiving()
            receivingGrade = receivingData.grade ?: ""
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
            showSingleSelectDialog(false, getString(R.string.enter_vendor_name), true, false, false)
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
    }

    private fun showSingleSelectDialog(
        isMaterial: Boolean,
        title: String,
        isVendor: Boolean,
        isGrade: Boolean,
        isLocation: Boolean
    ) {
        val list: ArrayList<String>
        if (isVendor) {
            list = vendorList.filter { it.vendorCode.startsWith("1") }
                .map { it.vendorCode.plus("-").plus(it.vendorName) } as ArrayList<String>
        } else if (isMaterial) {
            list = materialList.map { it.materialName } as ArrayList<String>
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
                activity!!,
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
                    if (receivingData.batchNumber?.isEmpty() == true) vm.generateBatchNumber() else receivingData.batchNumber
                receivingData.palletType =
                    if (receivingData.palletType?.isEmpty() == true) vm.generateGrnSequnceNumber() else receivingData.palletType
                //receivingData.unitsOfMeasure = "KG"
                receivingData.weighBridgeType = "PROCURE"
                receivingData.item = "1"

                callBack?.replaceFragment(GRN_WEIGHMENT, GRN_FIXED, receivingData)
            }
        }
    }

    private fun updateMandatory() {
        binding.tvVendorLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.vendor)) { mandatoryStars() } }
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

                if (vendors.size > 0) receivingData.taxId = vendors.get(0).taxNumber
                binding.tvVendor.hideKeyboard()
                binding.tvPoNumber.text = getString(R.string.select_po_number)
                receivingData.purchaseDocNum = ""
                binding.tvPoQuantity.text = ""
                customDialog?.dismiss()
//            }
        } else if (isMaterial) {
            var vendor = vendorList.filter { it.vendorCode==receivingData.supplierCode }.get(0)

            var isValidMaterial:Boolean=true
            /* if ((!vendor.vendorType.isNullOrEmpty()) && vendor.vendorType!!.equals("NC", true)) {
                 if (data.contains("Certificado", true)) {
                     isValidMaterial = false
                 }
             }
             if (isValidMaterial) {*/
                customDialog?.dismiss()
                binding.tvMaterial.text = data
                materialList.forEach {
                    if (it.materialName.equals(data)) {
                        receivingData.materialCode =
                            if (it.materialCode.length != 18) "000000".plus(it.materialCode) else it.materialCode
                        receivingData.materialName = it.materialName
                        receivingData.materialPrice = it.price
                        receivingData.grnType = getString(R.string.fixed)
                        if (data.isNotEmpty()) getQualityGrades()
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
}
