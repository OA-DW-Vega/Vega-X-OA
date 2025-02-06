package com.olam.warehouse.vegax.bagissueindiacoffee.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.bagissueindiacoffee.R
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeBagIssue
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeCurrentBagsIssued
import com.olam.warehouse.vegax.bagissueindiacoffee.databinding.FragmentVegaIndiaCoffeeBagIssueBinding
import com.olam.warehouse.vegax.bagissueindiacoffee.utils.PRODUCT
import com.olam.warehouse.vegax.bagissueindiacoffee.utils.SUMMARY_FRAG
import com.olam.warehouse.vegax.bagissueindiacoffee.utils.SUPPLIER
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaIndiaCoffeeBagIssueFragment : BaseFragment(), VegaSingleSelectCommonListener {

    private var bagIssueData: VegaIndiaCoffeeBagIssue = VegaIndiaCoffeeBagIssue()
    private val vm: VegaIndiaCoffeeBagIssueViewModel by viewModel()
    private lateinit var binding: FragmentVegaIndiaCoffeeBagIssueBinding
    override val layoutResourceId = R.layout.fragment_vega_india_coffee_bag_issue
    private var callBack: CallBack? = null
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var productList = ArrayList<String>()
    private var supplierLists = ArrayList<String>()
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null
    private var materials = ArrayList<VegaPackageMaterial>()

    interface CallBack {
        fun replaceFragment(
            paramsListFrag: String,
            item: VegaIndiaCoffeeBagIssue
        )
    }

    companion object {
        fun newInstance() = VegaIndiaCoffeeBagIssueFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaIndiaCoffeeBagIssueBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("containermanagement/ui/VegaCameroonAddContainerFragment").title("Gate Entry")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        updateMandatory()
        binding.tvProduct.setOnClickListener {
            showSingleSelectDialog(getString(R.string.select_material), PRODUCT )
        }
        binding.tvSupplier.setOnClickListener {
            showSingleSelectDialog(getString(R.string.select_supplier), SUPPLIER)
        }
        vm.currentBagIssue.observe(viewLifecycleOwner, Observer { updateUI(it) })

        vm.material.observe(viewLifecycleOwner, Observer {
            val products = it.map { data -> data.bagType }
            productList = products as ArrayList<String>
            materials = it as ArrayList<VegaPackageMaterial>
         /*   val productAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, products)
            binding.tvProduct.threshold = 1
            binding.tvProduct.setAdapter(productAdapter)
            binding.tvProduct.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                it.forEach { material ->
                    if (material.bagType == binding.tvProduct.text.toString()) {
                        bagIssueData.materialCode = "000000".plus(material.bagMaterialCode)
//                            MATERIAL_CODE.plus(material.materialCode.toString())
                        bagIssueData.materialName = material.bagType.toString()
                        bagIssueData.unitsOfMeasure = material.unitsOfMeasure.toString()
                    }
                }
                getBagIssueData()
            }*/
        })
        vm.getBagMaterial()

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            val suppliers = it
//                .filter { data -> data.bcApprover?.isNotEmpty()!! }
                .map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            supplierLists = suppliers as ArrayList<String>
           /* val supplierAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suppliers)
            binding.tvSupplier.threshold = 1
            binding.tvSupplier.setAdapter(supplierAdapter)
            binding.tvSupplier.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                it.forEach { vendor ->
                    if (vendor.vendorCode == binding.tvSupplier.text.toString().split(" - ")[0]) {
                        bagIssueData.supplierCode = vendor.vendorCode
                        bagIssueData.supplierName = vendor.vendorName
//                        vendor.bcApprover?.let { it1 -> vm.getSupplierZone(it1) }
                    }
                }
                getBagIssueData()
            }*/
        })
        vm.getSuppliers()

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            custonLocationList = it.toMutableList()
            var receivingLocationList = custonLocationList.filter {
                !it.storageLocationType.equals(
                    "B"
                )
            }
            if (receivingLocationList.size == 1) {
                binding.tvStorageLocation.text = custonLocationList[0].procureLocationCode.plus(" - ")
                    .plus(custonLocationList[0].procureLocationName)
                bagIssueData.storageLocationCode = custonLocationList[0].procureLocationCode
                bagIssueData.storageLocationName = custonLocationList[0].procureLocationName
            }
        })
        vm.getCustomLocations()

        binding.tvStorageLocation.setOnClickListener {
            var receivingLocationList = custonLocationList.filter {
                !it.storageLocationType.equals(
                    "B"
                )
            }
            if (receivingLocationList.size >= 1) {
                showStorageLocationDialog(receivingLocationList)
            }
        }

        binding.btnConfirm.setOnClickListener { validateInputs() }
//        binding.tvGatePassValue.onChange {
//            if(it.length > 0 && binding.tvNoOfBagsValue.text.length > 0)
//                enableProceed(true)
//            else
//                enableProceed(false)
//        }
//        binding.tvNoOfBagsValue.onChange {
//            if(it.length > 0 && binding.tvGatePassValue.text.length > 0)
//                enableProceed(true)
//            else
//                enableProceed(false)
//        }
//        enableProceed(false)
    }

    private fun getBagIssueData() {
        if (AppUtils.isOnline()) {
            if (bagIssueData.materialCode.isNotEmpty() && !bagIssueData.supplierCode.isNullOrEmpty() && !bagIssueData.storageLocationCode.isNullOrEmpty())
             vm.getCurrentBagsIssued(
                 bagIssueData.materialCode,
                 bagIssueData.supplierCode.toString(),
                 bagIssueData.storageLocationCode.toString()
             )

        }
    }

    private fun updateMandatory() {
        binding.tvProductLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.product)) { mandatoryStars() } }
        binding.tvSupplierLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.supplier)) { mandatoryStars() } }
        binding.tvLocationLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.storage_location)) { mandatoryStars() } }
        binding.tvBagIssueLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.enter_bag_qty_to_be_issued)) { mandatoryStars() } }
        binding.tvGatePassLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.gate_pass_no)) { mandatoryStars() } }
    }

    private fun showStorageLocationDialog(it: List<VegaCustomStLocation>) {
        val location = it.map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }
        MaterialDialog(requireContext()).show {
            title(R.string.storage_location_popup)
            listItemsSingleChoice(items = location) { _, index, text ->
                binding.tvStorageLocation.text = text
                bagIssueData.storageLocationCode = it[index].procureLocationCode
                bagIssueData.storageLocationName = it[index].procureLocationName
                getBagIssueData()
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun enableProceed(enable: Boolean) {
        binding.btnConfirm.isEnabled = enable
        when (enable) {
            true -> ViewCompat.setBackgroundTintList(
                binding.btnConfirm,
                ContextCompat.getColorStateList(
                    requireActivity(),
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btnConfirm,
                ContextCompat.getColorStateList(requireActivity(), android.R.color.darker_gray)
            )
        }
    }

    private fun validateInputs() {
        when {
            binding.tvProduct.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_product))
            binding.tvSupplier.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_supplier))
            binding.tvStorageLocation.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_location))
            binding.tvNoOfBagsValue.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_no_of_bags_to_be_issued))
            binding.tvGatePassValue.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_gate_pass_number))
            bagIssueData.currentBalance.isNullOrEmpty() -> showSnack(getString(R.string.enter_current_balance))
            ((bagIssueData.currentBalance?.toDouble()?.toLong()
                ?: 0).minus(binding.tvNoOfBagsValue.text.toString().toLong())) < 0 -> showSnack(
                getString(R.string.enter_less_bag_number)
            )
            else -> moveToSummary()
        }
    }

    private fun moveToSummary() {
        bagIssueData.bagIssued = binding.tvNoOfBagsValue.text.toString()
        bagIssueData.gatePassNum = binding.tvGatePassValue.text.toString()
        callBack?.replaceFragment(SUMMARY_FRAG, bagIssueData)
    }
    private fun clearAll(){
        binding.tvNoOfBagsValue.setText("")
        binding.tvGatePassValue.setText("")
        bagIssueData.gatePassNum = ""
        bagIssueData.bagIssued = ""
//        enableProceed(false)
    }

    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaIndiaCoffeeCurrentBagsIssued>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    if (it.data?.data?.size ?: 0 > 1) {
                        var currentBalance = it.data?.data?.get(1)?.unresConStock
                        bagIssueData.currentBalance = currentBalance
//                    binding.llBagIssue.visibility = View.VISIBLE
                        clearAll()
                        binding.tvCurrentBagIssuedLabel.text =
                            getString(R.string.current_bag_issued).plus(" ")
                                .plus(currentBalance.toString().toDouble().toLong()).plus(" bags")
                    } else {
                        showErrorDialogWithFAQLink(requireContext(), getString(R.string.no_data_found))
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {

                }

            }
        }
    }

    private fun showSingleSelectDialog(title: String,currentFalg: String) {
        var list = ArrayList<String>()
        when(currentFalg) {
            SUPPLIER -> {
                list = supplierLists
            }
            PRODUCT -> {
                list = productList
            }
        }
        customDialog =
            VegaCommonSingleSelectDialogWithSearch(
                title,
                currentFalg,
                list,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, currentFlag: String) {
        customDialog?.dismiss()
        when (currentFlag) {
            SUPPLIER -> {
                binding.tvSupplier.text = data
                getBagIssueData()
                bagIssueData.supplierCode = data.split("-")[0].trim()
                bagIssueData.supplierName = data.split("-")[1].trim()
                //                        vendor.bcApprover?.let { it1 -> vm.getSupplierZone(it1) }
            }
            PRODUCT -> {
                binding.tvProduct.text = data
                var list = materials.filter { it.bagType == data }
                list.forEach { material ->

                        bagIssueData.materialCode = "000000".plus(material.bagMaterialCode)
//                            MATERIAL_CODE.plus(material.materialCode.toString())
                        bagIssueData.materialName = material.bagType.toString()
                        bagIssueData.unitsOfMeasure = material.unitsOfMeasure.toString()

                }
                getBagIssueData()

            }
        }
    }

}


