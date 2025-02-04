package com.olam.warehouse.vegax.grnnicaragua.ui.spot

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.hideKeyboard
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnnicaragua.R
import com.olam.warehouse.vegax.grnnicaragua.databinding.FragmentVegaNicaraguaGrnTransactionDetailsBinding
import com.olam.warehouse.vegax.grnnicaragua.ui.VegaNicaraguaGrnViewModel
import com.olam.warehouse.vegax.grnnicaragua.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 9/04/2020
 */
class VegaNicaraguaGrnSpotTransactionDetails : BaseFragment(), VegaSingleSelectListener {
    private var vendorList = mutableListOf<VegaVendor>()
    private var materialList = mutableListOf<VegaMaterial>()
    private var gradeList = mutableListOf<VegaQualitative>()
    private var materialQualityGradeList = mutableListOf<VegaNicaraguaMaterialQualitGrades>()
    private var locationList = mutableListOf<VegaStorageLocation>()
    private var receivingData = VegaReceiving()
    private var grnType: String? = ""
    private var receivingGrade: String? = ""
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null

    private lateinit var binding: FragmentVegaNicaraguaGrnTransactionDetailsBinding
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

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_grn_transaction_details

    companion object {
        fun newInstance(grnType: String, vegaReceiving: VegaReceiving) =
            VegaNicaraguaGrnSpotTransactionDetails().putArgs {
                putString(GRN_TYPE, grnType)
                putParcelable(GRN_DATA, vegaReceiving)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaGrnTransactionDetailsBinding.inflate(layoutInflater)
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
            if (receivingData.storageLocationCode?.isNotEmpty() == true)
                binding.tvLocation.text =
                    receivingData.storageLocationCode.plus("-").plus(receivingData.storageLocationName)
        }
        updateMandatory()
        if (grnType.equals(GRN_PTBF)) binding.tvType.text = getString(R.string.grn_ptbf_transaction_details)
        vm.supplier.observe(viewLifecycleOwner, Observer {
            vendorList = it.toMutableList()
        })
        if(grnType.equals(GRN_PTBF)) vm.getSuppliers("NI02")
        else vm.getSuppliers("NI01")
        vm.product.observe(viewLifecycleOwner, Observer {
            val material = it.toMutableList()
            material.forEach {
                if (grnType.equals(GRN_PTBF)) {
                    if (!it.materialName!!.contains("PTBF") && !it.materialName!!.contains("Tolling"))
                        materialList.add(it)
                } else if (grnType.equals(GRN_SPOT) && !it.materialName!!.contains("PTBF")) materialList.add(it)
            }
        })
        vm.getProducts()
        vm.storageLocation.observe(viewLifecycleOwner, Observer {
            locationList = it.toMutableList()
        })
        vm.getStorageLocations()
        vm.grade.observe(viewLifecycleOwner, Observer {
            gradeList = it.toMutableList()
        })

        vm.materialQualityGrades.observe(viewLifecycleOwner, Observer {
            materialQualityGradeList = it.toMutableList()
        })

        binding.tvVendor.setOnClickListener {
            showSingleSelectDialog(false, getString(R.string.enter_vendor_name), true, false)
        }
        binding.tvMaterial.setOnClickListener {
            showSingleSelectDialog(true, getString(R.string.select_material), false, false)
        }
        binding.tvGrade.setOnClickListener {
            showSingleSelectDialog(false, getString(R.string.select_grade), false, true)
        }
        binding.tvLocation.setOnClickListener {
            showSingleSelectDialog(false, getString(R.string.select_grade), false, false)
        }
        binding.btnProceed.setOnClickListener { validateFields() }
        vm.bagItems.observe(viewLifecycleOwner, Observer { updateBagItems(it) })
    }

    private fun updateBagItems(bagItems: List<VegaNicaraguaWeighmentBagMaterial>?) {
        if (bagItems?.size ?: 0 > 0) {
            binding.tvVendor.isEnabled = false
            binding.tvGrade.isEnabled = false
            binding.tvMaterial.isEnabled = false
        } else {
            binding.tvVendor.isEnabled = true
            binding.tvGrade.isEnabled = true
            binding.tvMaterial.isEnabled = true
        }
    }

    private fun showSingleSelectDialog(isMaterial: Boolean, title: String, isVendor: Boolean, isGrade: Boolean) {
        val list: ArrayList<String>
        if (isVendor) {
            list = vendorList.filter { it.vendorCode.startsWith("1") }
                .map { it.vendorCode.plus("-").plus(it.vendorName) } as ArrayList<String>
        } else if (isMaterial) {
            list = materialList.map { it.materialName } as ArrayList<String>
        } else if (isGrade)
        {
            var gradeListFilter = mutableListOf<VegaQualitative>()

            materialQualityGradeList.forEach {qualityGrade ->
                gradeListFilter.addAll(gradeList.filter {
                    it.charValue.split(" ").get(it.charValue.split(" ").size - 1) == qualityGrade.gradeCode
                })
            }

            list = gradeListFilter.map { it.charValue.plus("-").plus(it.descValue) } as ArrayList<String>
        } else {
            list =
                locationList.map { it.storageLocationCode.plus("-").plus(it.storageLocationName) } as ArrayList<String>
        }

        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isMaterial, isVendor, isGrade,
                list,
                activity!!,
                this,false,false,null,null,false,false
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    private fun getQualityGrades() {
        binding.tvGradeLabel.visible()
        binding.tvGrade.visible()
        receivingData.grade = ""
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
            receivingData.storageLocationCode.isNullOrEmpty() || receivingData.storageLocationCode.equals(getString(R.string.select_location)) -> {
                showSnack(requireContext().resources.getString(R.string.enter_location_error))
            }
            else -> {
                receivingData.tmpWbId = if (receivingData.tmpWbId.isEmpty()) getTmpId() else receivingData.tmpWbId
                receivingData.batchNumber =
                    if (receivingData.batchNumber?.isEmpty() == true) vm.generateBatchNumber() else receivingData.batchNumber
                receivingData.palletType =
                    if (receivingData.palletType?.isEmpty() == true)
                        vm.generateGrnSequnceNumber() else
                            receivingData.palletType
                receivingData.unitsOfMeasure = "KG"
                receivingData.weighBridgeType = "PROCURE"
                receivingData.item = "1"

                callBack?.replaceFragment(GRN_WEIGHMENT, GRN_SPOT, receivingData)
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
        binding.tvLocationLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.location)) { mandatoryStars() } }
    }

    override fun clickOnItem(data: String, isMaterial: Boolean, isVendor: Boolean, isGrade: Boolean) {

        if (isVendor) {
            var vendors = vendorList.filter { it.vendorCode==data.split("-")[0] }

           /* if (vendors.get(0).vendorType.isNullOrEmpty()) {
                Toast.makeText(context, getString(R.string.invalid_vendor_error), Toast.LENGTH_SHORT).show()
            } else {*/
                customDialog?.dismiss()
                binding.tvVendor.text = data
                receivingData.supplierCode = data.split("-")[0]
                receivingData.supplierName = data.split("-")[1]
                if (vendors.size > 0) receivingData.taxId = vendors.get(0).taxNumber

                binding.tvVendor.hideKeyboard()
//            }

        } else if (isMaterial) {
            var vendor = vendorList.filter { it.vendorCode == receivingData.supplierCode }.get(0)

            var isValidMaterial: Boolean = true
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
                        if (it.materialName!!.contains("Tolling") && it.price!!.equals(0.0))
                            receivingData.grnType = getString(R.string.toll)
                        else if (grnType.equals(GRN_PTBF))
                            receivingData.grnType = getString(R.string.ptbf)
                        else receivingData.grnType = getString(R.string.spot)
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
          var gradeData=materialQualityGradeList.filter {
              it.gradeCode == receivingData.grade?.split(" ")?.get(receivingData.grade?.split(" ")!!.size - 1) ?: 0
          }.get(0)
            receivingData.bagTareWeight=gradeData.tareWeight
            receivingData.bagType = gradeData.bagType

        } else if (!isGrade) {
            customDialog?.dismiss()
            binding.tvLocation.text = data
            receivingData.storageLocationCode = data.split("-")[0]
            receivingData.storageLocationName = data.split("-")[1]
        }
    }

    fun transUpdate() {
        if (receivingData.tmpWbId.isNotEmpty()) vm.getBagItems(receivingData.tmpWbId)
    }

}
