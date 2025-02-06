package com.olam.warehouse.vegax.bagissuenigeriacocoa.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getMultiPlantList
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.bagissuenigeriacocoa.R
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.model.VegaNigeriaCocoaBagIssue
import com.olam.warehouse.vegax.bagissuenigeriacocoa.databinding.FragmentVegaNigeriaCocoaBagIssueBinding
import com.olam.warehouse.vegax.bagissuenigeriacocoa.utils.*

import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaCocoaBagIssueFragment : BaseFragment(), VegaSingleSelectCommonListener {

    private var bagIssueData: VegaNigeriaCocoaBagIssue = VegaNigeriaCocoaBagIssue()
    private val vm: VegaNigeriaCocoaBagIssueViewModel by viewModel()
    private lateinit var binding: FragmentVegaNigeriaCocoaBagIssueBinding
    override val layoutResourceId = R.layout.fragment_vega_nigeria_cocoa_bag_issue
    private var callBack: VegaNigeriaCocoaBagMgmtCallBackListener? = null
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var selectedSendingWH: String = ""
    private var materialStorageLocationList = mutableListOf<VegaStorageLocationDetail>()
    private lateinit var productList: List<VegaPackageMaterial>
    private lateinit var suppliersLIst: List<VegaVendor>
    private var stocksList = mutableListOf<VegaCocoaRminLots>()
    private var sendingWareHouseList = mutableListOf<VegaCustomStLocation>()
    private var plantList = mutableListOf<Plant>()
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null
    private var screenType:String? = ""


    interface CallBack {
//        fun replaceFragment(
//            paramsListFrag: String,
//            item: VegaNigeriaCocoaBagIssue
//        )
    }

    companion object {
        fun newInstance(screenType: String) = VegaNigeriaCocoaBagIssueFragment().putArgs {
            putString(SCREEN_TYPE, screenType)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNigeriaCocoaBagIssueBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as VegaNigeriaCocoaBagMgmtCallBackListener
        screenType = arguments?.getString(SCREEN_TYPE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("containermanagement/ui/VegaCameroonAddContainerFragment").title("Gate Entry")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        binding.tvTitle.setText(if(screenType.equals(BAG_ISSUE_FRAG))getString(R.string.supplier_bag_issue) else getString(R.string.supplier_bag_return))
        updateMandatory()
//        vm.currentBagIssue.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.stocks.observe(viewLifecycleOwner, Observer { updateUI(it) })


        vm.material.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (getCurrentKey().contains("IV") && getCurrentKey().contains("CASH")){
                    binding.tvGatePassLabel.gone()
                    binding.tvGatePassValue.gone()
                    productList = it.filter { item-> item.isDefault == true }
                }else{
                    productList = it
                }

            }
        })
        vm.getBagMaterial()

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            it?.let {
                suppliersLIst = it
            }
        })

        if(getCurrentKey().contains("VEGA_NG") && getCurrentKey().contains("COCO"))
        vm.getSuppliers(getPlantDetails().plantId)
        else vm.getVendors()

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            custonLocationList = it.toMutableList()
            var receivingLocationList = custonLocationList.filter {
                !it.storageLocationType.equals(
                    "B"
                )
            }
            if (receivingLocationList.size == 1) {
                binding.tvStorageLocation.text = custonLocationList[0].procureLocationCode.plus(" - ")
                    .plus("${custonLocationList[0].procureLocationName}")
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
                showSingleSelectDialog(getString(R.string.storage_location_popup), "")
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

        binding.tvProduct.setOnClickListener {
            showSingleSelectDialog(getString(R.string.select_bag_type), PRODUCT)
        }
        binding.tvSupplier.setOnClickListener {
            showSingleSelectDialog(getString(R.string.select_supplier), SUPPLIER)
        }

    }

    private fun getBagIssueData() {
        if (AppUtils.isOnline()) {
            if (bagIssueData.materialCode.isNotEmpty() && !bagIssueData.supplierCode.isNullOrEmpty() && !bagIssueData.storageLocationCode.isNullOrEmpty())
                vm.fetchStocks(
                    bagIssueData.materialCode,

                    )
            else if (getCurrentKey().contains("IV") && getCurrentKey().contains("CASH"))
                vm.fetchStocks(
                    bagIssueData.materialCode,
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
        binding.tvBagIssueLabel.text = if(screenType.equals(BAG_ISSUE_FRAG))
            with(UIUtils) { with(requireContext().resources.getString(R.string.enter_bag_qty_to_be_issued)) { mandatoryStars() } }
        else
            with(UIUtils) { with(requireContext().resources.getString(R.string.enter_bag_qty_retn)) { mandatoryStars() } }
        binding.tvGatePassLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.gate_pass_no)) { mandatoryStars() } }
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
            binding.tvGatePassValue.text.isNullOrEmpty() && !(getCurrentKey().contains("IV") && getCurrentKey().contains("CASH")) -> showSnack(getString(R.string.enter_gate_pass_number))
            bagIssueData.currentBalance.isNullOrEmpty() -> showSnack(getString(R.string.enter_current_balance))
            binding.tvNoOfBagsValue.text.toString().toLong()<= 0 -> showSnack(
                getString(R.string.enter_zero_bag_number))
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
        bagIssueData.screenType = if(screenType.equals(BAG_ISSUE_FRAG)) BAG_ISSUE_FRAG else BAG_RETN_FRAG
        callBack?.replaceFragment(SUMMARY_FRAG, bagIssueData)
    }

    private fun clearAll() {
        binding.tvNoOfBagsValue.setText("")
        binding.tvGatePassValue.setText("")
        bagIssueData.gatePassNum = ""
        bagIssueData.bagIssued = ""
//        enableProceed(false)
    }

    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            data.data?.data?.let {
                                it.forEach { item ->
                                    var materialCode = (binding.tvProduct.text.toString())
                                    var selectedSendingWH = binding.tvStorageLocation.text.toString().split(" - ")[0]
                                    plantList = getMultiPlantList() as MutableList<Plant>
                                    var loggedInPlantId = (getPlantDetails().plantId)
                                    if ((item.storageLocationCode?.equals(
                                            selectedSendingWH
                                        ) == true) && (item.plantId?.equals(loggedInPlantId) == true)
                                    ) {

                                        var currentBalance = item.weight
                                        bagIssueData.currentBalance = currentBalance
                                        BATCHNUMBER = item.batchNumber
                                        UOM = item.unitOfMeasure.toString()
//                    binding.llBagIssue.visibility = View.VISIBLE
                                        clearAll()
                                        binding.tvCurrentBagIssuedLabel.text =
                                            getString(R.string.current_bag_issued).plus(" ")
                                                .plus(currentBalance.toString().toDouble().toLong()).plus(" bags")

                                    }

                                }
                            }

                            if (bagIssueData.currentBalance.isNullOrEmpty())
                                showErrorDialogWithFAQLink(requireContext(), getString(R.string.no_data_found))

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
    }

    override fun clickOnItem(data: String, currentFlag: String) {
        customDialog?.dismiss()
        when (currentFlag) {
            PRODUCT -> {
                binding.tvProduct.text = data
                productList.forEach { material ->
                    if (material.bagType == binding.tvProduct.text.toString()) {
                        bagIssueData.materialCode = (material.bagMaterialCode)
//                            MATERIAL_CODE.plus(material.materialCode.toString())
                        bagIssueData.materialName = material.bagType.toString()
                        bagIssueData.unitsOfMeasure = material.unitsOfMeasure.toString()
                        return@forEach
                    }
                }
                validateAndGetBagIssueData()
            }

            SUPPLIER -> {
                binding.tvSupplier.text = data
                suppliersLIst.forEach { vendor ->
                    if (vendor.vendorCode == binding.tvSupplier.text.toString().split(" - ")[0]) {
                        bagIssueData.supplierCode = vendor.vendorCode
                        bagIssueData.supplierName = vendor.vendorName
                        return@forEach
                    }
                }
                validateAndGetBagIssueData()
            }

            else -> {
                binding.tvStorageLocation.text = data

                   val list = if (getCurrentKey().contains("IV")&& getCurrentKey().contains("CASH"))
                    custonLocationList
                   else  custonLocationList.filter { it.storageLocationType.equals("P") }
                   val code = if (data.contains("-")) data.split(" - ")[0] else data
                   list.forEach {
                       if (it.procureLocationCode == code) {
                           bagIssueData.storageLocationCode = it.procureLocationCode
                           bagIssueData.storageLocationName = it.procureLocationName
                           return@forEach
                       }
                   }
                validateAndGetBagIssueData()
            }
        }
    }

    private fun validateAndGetBagIssueData(){
        if(binding.tvProduct.text.isNotEmpty() && binding.tvSupplier.text.isNotEmpty() && binding.tvStorageLocation.text.isNotEmpty()){
            getBagIssueData()
        }
    }

    private fun showSingleSelectDialog(title: String, currentFalg: String) {
        val list: List<String>
        when (currentFalg) {
            PRODUCT -> {
                list = productList.map { data -> data.bagType }
            }
            SUPPLIER -> {
                list = suppliersLIst.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            }
            else -> {
                list = if (getCurrentKey().contains("VEGA_IV") && getCurrentKey().contains("CASH")) {
                    custonLocationList
                        .map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }
                } else {
                    custonLocationList.filter { it.storageLocationType.equals("P") }
                        .map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }
                }
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

}

