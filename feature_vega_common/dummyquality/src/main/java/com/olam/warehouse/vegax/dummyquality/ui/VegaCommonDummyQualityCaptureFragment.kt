package com.olam.warehouse.vegax.dummyquality.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.hideKeyboard
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.dummyquality.R
import com.olam.warehouse.vegax.dummyquality.data.domain.model.MaterialList
import com.olam.warehouse.vegax.dummyquality.data.domain.model.ResponseVendorAndMaterial
import com.olam.warehouse.vegax.dummyquality.data.domain.model.VendorList
import com.olam.warehouse.vegax.dummyquality.databinding.FragmentDummyQualityCaptureBinding
import com.olam.warehouse.vegax.dummyquality.utils.DUMMY_QUALITY_CAPTURE_ACCEPT
import com.olam.warehouse.vegax.dummyquality.utils.DUMMY_QUALITY_LIST
import com.olam.warehouse.vegax.dummyquality.utils.IS_VIEW_QUALITY
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaCommonDummyQualityCaptureFragment : BaseFragment(), VegaSingleSelectListener {

    private val TAG = "VegaCommonDummyCaptureQ"
    private var callBack: CallBack? = null
    override val layoutResourceId = R.layout.fragment_dummy_quality_capture
    private val viewModel: VegaCommonDummyQualityViewModel by viewModel()
    private lateinit var binding: FragmentDummyQualityCaptureBinding
    private var receivingData = VegaReceiving()
    private lateinit var vendorList: List<VegaVendor>
    private lateinit var materialList: List<VegaMaterial>
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private lateinit var supplierCode: String
    private lateinit var supplierName: String
    private lateinit var materialName: String
    private lateinit var materialId: String
    private var vendorDetailsList: ArrayList<VendorList>? = ArrayList()
    private var materialDetailsList: ArrayList<MaterialList>? = ArrayList()
    private var selectedIndex:Int = -1
    private var vendorDetails: List<ResponseVendorAndMaterial>? = null

    companion object {
        fun newInstance() = VegaCommonDummyQualityCaptureFragment().putArgs {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDummyQualityCaptureBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        updateMandatory()
        viewModel.getSuppliers()
        viewModel.getProducts()
        if (IS_VIEW_QUALITY) {
            viewModel.getVendorAndMaterial(getPlantDetails().plantId)
            binding.tvManual.visibility = View.GONE
            binding.etManual.visibility = View.GONE
            binding.tvOk.visibility = View.GONE
        }

        viewModel.vendorAndMaterialList.observe(viewLifecycleOwner) {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.let { response ->
                        vendorDetails = response.data
                        response.data.forEachIndexed { index, model ->
                            val vendorDetails = VendorList()
                            vendorDetails.vendorName = model.vendorName
                            vendorDetails.vendorCode = model.vendorCode
                            vendorDetailsList?.add(vendorDetails)
                        }
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }

        viewModel.supplier.observe(viewLifecycleOwner) {
            vendorList = it.toMutableList()
        }

        viewModel.product.observe(viewLifecycleOwner) {
            materialList = it.toMutableList()
        }

        binding.tvVendor.setOnClickListener {
            showSingleSelectDialog(false, getString(R.string.enter_vendor_name), true, false)
        }
        binding.tvMaterial.setOnClickListener {
            showSingleSelectDialog(true, getString(R.string.select_material), false, false)
        }
        binding.btnProceed.setOnClickListener { validateFields() }

        binding.etManual.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int
            ) {
                if (s.isNotEmpty()) {
                    binding.tvVendor.setText("")
                    supplierCode = s.toString().trim()
                    supplierName = s.toString().trim()
                }
            }
        })

        binding.etManual.setOnClickListener {
            if (binding.etManual.text.isEmpty() && binding.tvVendor.text.isNullOrEmpty()) {
                showSnack(requireContext().resources.getString(R.string.enter_vendor_error))
            }
        }
    }


    private fun validateFields() {
        when {
            binding.tvVendor.text.isNullOrEmpty() && binding.etManual.text.isNullOrEmpty() -> {
                showSnack(requireContext().resources.getString(R.string.enter_vendor_error))
            }
            binding.tvMaterial.text.isNullOrEmpty() -> {
                showSnack(requireContext().resources.getString(R.string.select_material_error))
            }
            else -> {
                if (IS_VIEW_QUALITY) {
                    callBack?.replaceFragment(
                        DUMMY_QUALITY_LIST, supplierCode, supplierName, materialName, materialId, "0"
                    )
                } else {
                    callBack?.replaceFragment(
                        DUMMY_QUALITY_CAPTURE_ACCEPT, supplierCode, supplierName, materialName, materialId, "0"
                    )
                }
            }
        }
    }

    private fun updateMandatory() {
        binding.tvVendorLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.vendor)) { mandatoryStars() } }
        binding.tvMaterialLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.material)) { mandatoryStars() } }

    }

    private fun showSingleSelectDialog(isMaterial: Boolean, title: String, isVendor: Boolean, isGrade: Boolean) {
        var list: ArrayList<String>? = null
        vendorList.find { it.vendorCode.contains("") || it.vendorName?.contains("") == true }?.bcApprover
        if (isVendor) {
            list = if (IS_VIEW_QUALITY) {
                vendorDetailsList?.map { it.vendorCode.plus("-").plus(it.vendorName) } as ArrayList<String>
            } else {
                vendorList.filter { it.vendorCode.startsWith("1") }
                    .map { it.vendorCode.plus("-").plus(it.vendorName) } as ArrayList<String>
            }
        } else if (isMaterial) {
            list = if (IS_VIEW_QUALITY) {
                materialDetailsList?.let {
                 materialDetailsList?.map { it.materialName } as ArrayList<String>
                }
            } else {
                 materialList.map { it.materialName } as ArrayList<String>
            }
        }


        customDialog = list?.let {
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isMaterial,
                isVendor,
                isGrade,
                it,
                requireActivity(),
                this,
                false,
                false,
                null,
                null,
                false,
                false
            )
        }
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            supplierCode: String,
            supplierName: String,
            materialName: String,
            materialId: String,
            id: String
        )
    }

    override fun clickOnItem(data: String, isMaterial: Boolean, isVendor: Boolean, isGrade: Boolean) {

        if (isVendor) {
            customDialog?.dismiss()
            binding.tvVendor.text = data
            binding.tvVendor.hideKeyboard()
            supplierCode = data.split("-")[0]
            supplierName = data.split("-")[1]
            binding.etManual.setText("")
            vendorDetails?.let {
                vendorDetails?.forEachIndexed { index, response ->
                     if(supplierCode == response.vendorCode)
                         selectedIndex = index
                }
            }
            if(selectedIndex != -1){
                materialDetailsList?.clear()
                val materialList = vendorDetails?.get(selectedIndex)?.materialList
                materialList?.forEachIndexed { index, model ->
                    val material = MaterialList()
                    material.materialCode = model.materialCode
                    material.materialName = model.materialName
                    materialDetailsList?.add(material)
                }
            }

        } else if (isMaterial) {
            customDialog?.dismiss()
            binding.tvMaterial.text = data
            binding.tvMaterial.hideKeyboard()
            materialName = data
            materialList.forEach {
                if (data == it.materialName) {
                    materialId = it.materialCode
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }
}
