package com.olam.warehouse.vegax.grnindo.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCoffeeSingleSelectListener
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnindo.R
import com.olam.warehouse.vegax.grnindo.databinding.FragmentIndoCoffeeGrnDetailsBinding
import com.olam.warehouse.vegax.grnindo.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */
class VegaIndoCoffeeGrnDetailsFragment : BaseFragment(), VegaCoffeeSingleSelectListener,
    VegaSingleSelectListener {
    private var paidWeight: Double = 0.0
    private var mAlertDialog: AlertDialog? = null
    private val vm: VegaIndoCoffeeGrnViewModel by viewModel()
    private var weighBridgeId = VegaGrnWeighBridgeId()
    private var wbDetails = VegaGrnWeighBridgeId()
    private var currentMaterial: String = ""
    private var defaultStorageLocation: String = ""
    private var purchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private var callBack: CallBack? = null

    private lateinit var binding: FragmentIndoCoffeeGrnDetailsBinding
    override val layoutResourceId = R.layout.fragment_indo_coffee_grn_details

    interface CallBack {
        fun replaceFragment(moveFrag: String, item: VegaGrnWeighBridgeId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentIndoCoffeeGrnDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("approveecuador/ui/details/VegaEcuadorGrnDetailsFragment").title("GRN Ecuador")
            .with(tracker)
        initUI()
    }

    companion object {
        fun newInstance(grnData: VegaGrnWeighBridgeId) = VegaIndoCoffeeGrnDetailsFragment().putArgs {
            putParcelable(GRN_DATA, grnData)
        }
    }

    private fun initUI() {
        wbDetails = arguments?.getParcelable(GRN_DATA)!!
        if (wbDetails.unitPrice!!.isNotEmpty() && !wbDetails.unitPrice.equals("0.00")) {
            binding.etPrice.setText(wbDetails.unitPrice.toString())
            binding.etPrice.isEnabled = false
            binding.tvTotalValue.text = wbDetails.totalPrice
        }
        currentMaterial = wbDetails.materialCode.toString()
        updateUIValues()
        vm.grn.observe(viewLifecycleOwner, Observer { updateGrnUI(it) })

        binding.tvUsdLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.final_price_per_unit)) { mandatoryStars() } }
        binding.btnProceed.setOnClickListener {
            if (binding.tvPOtypeValue.text.toString().equals("Spot", true)) {
                wbDetails.unitPrice = binding.etPrice.text.toString()
                when {
                    binding.etPrice.text.isNullOrEmpty() -> showSnack(getString(R.string.price_validation))
                    else -> showConfirmDialog()
                }
            } else {
                wbDetails.purchaseDocNum = binding.tvPOValue.text.toString()
                when {
                    wbDetails.purchaseDocNum.isNullOrEmpty() -> showSnack(getString(R.string.po_validation))
                    else -> showConfirmDialog()
                }
            }
        }
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.getConfigItems(UserRoles.GRN.role)
        vm.poList.observe(viewLifecycleOwner, Observer { updatePurchaseOrderUI(it) })
        vm.poListOffline.observe(viewLifecycleOwner, Observer { updatePurchaseOrderOfflineUI(it) })
        if (isOnline()) vm.getPOList() else vm.getPOListOffline()
        binding.btnQuality.setOnClickListener { callBack?.replaceFragment(QUALITY_DETAILS, wbDetails) }

        if(!isOnline())binding.btnQuality.gone()
        binding.tvPOValue.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.select_purchase_order),
                false,
                true, false, false
            )
        }

        binding.tvPOtypeValue.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.select_purchase_type),
                false,
                false, false, false
            )
        }

        binding.etPrice.onChange {
            try {
                if (binding.tvPOtypeValue.text.toString().equals("Spot")) {
                    val paidData = paidWeight.formatThreeDigits().replace(",", "")
                    val totalVal = it.toDouble() * paidData.toDouble()
                    binding.tvTotalValue.text = totalVal.formatThreeDigits()
                    wbDetails.totalPrice = totalVal.formatThreeDigits()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>) {
        val defaultStorageLoc = configItems.filter { it.process.equals(ConfigItems.DEFAULT_STORAGE_LOC.item) }
        var isExist = false
        val isMaterial = defaultStorageLoc.map { it.materialCode }.contains(currentMaterial.removeRange(0, 6))
        defaultStorageLoc.forEach {
            if (currentMaterial.contains(it.materialCode) && !it.materialCode.isEmpty() && !isExist && isMaterial) {
                if (it.applicable?.contains("Y")!!) {
                    defaultStorageLocation = it.value.toString()
                    isExist = true
                }
            } else if (it.materialCode.isEmpty() && !isExist && !isMaterial) {
                if (it.applicable?.contains("Y")!!) {
                    defaultStorageLocation = it.value.toString()
                    isExist = true
                }
            }
        }
        vm.storageLocation.observe(viewLifecycleOwner, Observer {
            binding.tvStorageLocation.text = it?.storageLocationName
        })
        vm.fetchStorageLocation(defaultStorageLocation)
    }

    private fun updatePurchaseOrderUI(response: Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let {
                                purchaseOrderList =
                                    it.filter { it.materialNumber.contains(wbDetails.materialCode.toString()) }
                                        .filter { it.supplier.contains(wbDetails.supplierCode.toString()) }
                                        .filter { it.poType.equals("Z001") }.toMutableList()
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

    private fun updatePurchaseOrderOfflineUI(response: List<VegaEcuadorPurchaseOrder>) {
        response.let {
            purchaseOrderList =
                it.filter { it.materialNumber.contains(wbDetails.materialCode.toString()) }
                        .filter { wbDetails.supplierCode.toString().contains(it.supplier.toString()) }
                    .filter { it.poType.equals("Z001") }.toMutableList()
        }
    }

    private fun updateGrnUI(data: Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    mAlertDialog?.dismiss()
                    moveToSuccessPage(it.data?.data?.grnNumber.toString())
                    /* vm.updateGrnNoToQuality(
                         wbDetails.weighBridgeId.toString(),
                         it.data?.data?.grnNumber.toString(),
                         it.data?.data?.batchNumber.toString()
                     )*/
                    vm.updateGrnSuccess(
                        wbDetails.weighBridgeId.toString(),
                        it.data?.data?.grnNumber.toString(),
                        it.data?.data?.batchNumber.toString(),
                        getString(R.string.grn_success),
                        4,
                        wbDetails
                    )
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    vm.updateGrnSuccess(
                        wbDetails.weighBridgeId.toString(),
                        it.data?.data?.grnNumber.toString(),
                        it.data?.data?.batchNumber.toString(),
                        it.error.toString(),
                        3,
                        wbDetails
                    )
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun updateUIValues() {
        binding.tvParamsWeighBID.text = wbDetails.weighBridgeId
        binding.tvTruckSupplier.text = wbDetails.supplierName
        binding.tvMaterial.text = wbDetails.materialName
        binding.tvPOtypeValue.text = wbDetails.purchaseType
        binding.tvNumberOfBags.text = wbDetails.bagCount
        binding.tvNetWeight.text = wbDetails.netWeight.plus(" ").plus(wbDetails.unitsOfMeasure)
        paidWeight = wbDetails.netWeight.toDouble()
        binding.tvPaidWeight.text = wbDetails.netWeight.plus(" ").plus(wbDetails.unitsOfMeasure)
        //paidWeight.formatThreeDigits().replace(",", "").plus(" ").plus(wbDetails.unitsOfMeasure)
        //binding.tvStorageLocation.text = defaultStorageLocation
        if (wbDetails.purchaseDocNum.isNullOrEmpty()) {
            binding.grnDetailsTitle.text = getString(R.string.grn).plus(" - ").plus(SPOT)
            //binding.tvProcurementType.text = getString(R.string.spot_purchase)

        } else {
            binding.grnDetailsTitle.text = getString(R.string.grn).plus(" - ").plus(FIXED)
            // binding.tvProcurementType.text = getString(R.string.fixed_purchase)
            binding.tvPoNumber.text = wbDetails.purchaseDocNum
            binding.llPoNumber.visible()
            binding.llPriceDetails.gone()
        }
        if (wbDetails.isView == true) binding.btnProceed.gone() else binding.btnProceed.visible()

        when (wbDetails.purchaseType) {
            "Spot" -> {
                binding.grnDetailsTitle.text = getString(R.string.grn).plus(" - ").plus(SPOT)
                binding.llPoNumber.gone()
                binding.llPriceDetails.visible()
            }
            "Fixed" -> {
                binding.grnDetailsTitle.text = getString(R.string.grn).plus(" - ").plus(FIXED)
                binding.tvPOValue.text = wbDetails.purchaseDocNum
                binding.llPoNumber.visible()
                binding.llPriceDetails.gone()
            }
            else -> {
                binding.llPoNumber.gone()
                binding.llPriceDetails.gone()
            }
        }

    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_grn)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    //wbDetails.storageLocationCode = binding.tvStorageLocation.text.toString()
                    val wbData = preparePostGrnData(wbDetails)
                    if (AppUtils.isOnline()) {
                        vm.postGrn(
                            VegaEcuadorGrnPost(
                                key = getCurrentKey(),
                                plant = getPlantDetails(),
                                grnData = listOf(wbData)
                            )
                        )
                    } else {
                        saveData()
                    }
                },
                { dismiss() })
        }
    }

    private fun saveData() {
        wbDetails.isOfflineData = true
        wbDetails.isTransStatus = true
        wbDetails.purchaseType = binding.tvPOtypeValue.text.toString()
        if (!wbDetails.wbTempId.contains("TMP")) {
            wbDetails.isNotWBID = true
            wbDetails.grnNumber = getTmpId()
        }
        vm.updateGRNPrice(wbDetails)
        moveToSuccessPage(wbDetails.weighBridgeId.toString())
    }

    private fun moveToSuccessPage(grn: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) {
            intent.putExtra(AppUtils.TITLE, getString(R.string.grn_success))
            // vm.updateDeletedItem(wbDetails.weighBridgeId.toString())
        } else
            intent.putExtra(AppUtils.TITLE, getString(R.string.grn_success_offline))
        intent.putExtra(AppUtils.SUB_TITLE, grn)
        startActivity(intent)
    }

    private fun moveToFailurePage(wbId: String, msg: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.grn_fail_msg))
        intent.putExtra(AppUtils.SUB_TITLE, msg.plus("\n").plus(wbId))
        intent.putExtra(AppUtils.FAILURE, false)
        startActivity(intent)
    }

    private fun showSingleSelectDialog(
        isWh: Boolean,
        title: String,
        isVendor: Boolean,
        isPoList: Boolean,
        isOrgin: Boolean, isDepartment: Boolean,
        isYear: Boolean = false
    ) {
        var list = ArrayList<String>()
        if (isPoList) {
            list.addAll(purchaseOrderList.map { data ->
                data.poId.plus("-").plus(data.openQuantity).plus(" KG")
            })
        } else {
            val list1 = ArrayList<String>()
            list1.add("Spot")
            list1.add("Fixed")
            list = list1
        }
        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isWh, isVendor, isPoList,
                list,
                activity!!,
                this, isSupplier = isYear, supplierListener = this, isOrigin = isOrgin, isDepartment = isDepartment
            )

        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(
        data: String,
        isWh: Boolean,
        isVendor: Boolean,
        isGrade: Boolean,
        isSupplier: Boolean,
        isOrigin: Boolean,
        isDepartment: Boolean
    ) {
        customDialog?.dismiss()
        if (isGrade) {
            val split = data.split("-")
            binding.tvPOValue.text = split[0]
            val item = purchaseOrderList.singleOrNull { it.poId == split[0] }
            wbDetails.purchaseDocNum = item?.poId
            wbDetails.purchaseDocDesc = item?.ebelp
//            wbDetails.purchaseDocQty = item?.openQuantity
            wbDetails.unitsOfMeasure = item?.meins ?: ""
        } else {
            binding.tvPOtypeValue.text = data
            when (data) {
                "Spot" -> {
                    binding.llPoNumber.gone()
                    binding.llPriceDetails.visible()
                }
                "Fixed" -> {
                    binding.llPoNumber.visible()
                    binding.llPriceDetails.gone()
                    wbDetails.unitPrice = ""
                    wbDetails.totalPrice = ""
                }
                else -> {
                    binding.llPoNumber.gone()
                    binding.llPriceDetails.gone()
                }
            }

        }
    }

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {
        //doto
    }

}
