package com.olam.warehouse.vegax.grnecuador.ui.weighbridge

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
import com.afollestad.materialdialogs.list.listItemsSingleChoice
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
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnecuador.R
import com.olam.warehouse.vegax.grnecuador.databinding.FragmentVegaEcuadorWeighbridgeGrnDetailsBinding
import com.olam.warehouse.vegax.grnecuador.ui.VegaEcuadorGrnViewModel
import com.olam.warehouse.vegax.grnecuador.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Roshna Parambil on 1/13/2022.
 */
class VegaEcuadorWeighbridgeGrnDetailsFragment : BaseFragment() {
    private var paidWeight: Double = 0.0
    private var mAlertDialog: AlertDialog? = null
    private val vm: VegaEcuadorGrnViewModel by viewModel()
    private var weighBridgeId = VegaGrnWeighBridgeId()
    private var wbDetails = VegaGrnWeighBridgeId()
    private var currentMaterial: String = ""
    private var defaultStorageLocation: String = ""
    private var callBack: VegaEcuadorWeighbridgeGrnDetailsFragment.CallBack? = null
    private var procureType = ""
    private var filterpurchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var purchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()


    private lateinit var binding: FragmentVegaEcuadorWeighbridgeGrnDetailsBinding
    override val layoutResourceId = R.layout.fragment_vega_ecuador_weighbridge_grn_details

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaEcuadorWeighbridgeGrnDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnecuador/ui/details/VegaEcuadorGrnDetailsFragment").title("GRN Ecuador")
            .with(tracker)
        initUI()
    }

    companion object {
        fun newInstance(grnData: VegaGrnWeighBridgeId) = VegaEcuadorWeighbridgeGrnDetailsFragment().putArgs {
            putParcelable(GRN_WB_DATA, grnData)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as VegaEcuadorWeighbridgeGrnDetailsFragment.CallBack
    }

    private fun initUI() {
        wbDetails = arguments?.getParcelable(GRN_WB_DATA)!!
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
            if (wbDetails.purchaseDocNum.isNullOrEmpty()) {
                wbDetails.unitPrice = binding.etPrice.text.toString()
                when {
                    binding.etPrice.text.isNullOrEmpty() -> showSnack(getString(R.string.price_validation))
                    else -> moveToSummaryScreen()
                }
            } else moveToSummaryScreen()
        }
        binding.etPrice.onChange {
            try {
                val paidData = paidWeight.formatThreeDigits().replace(",", "")
                val totalVal = it.toDouble() * paidData.toDouble()
                binding.tvTotalValue.text = totalVal.formatThreeDigits()
                wbDetails.totalPrice = totalVal.formatThreeDigits()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.poList.observe(viewLifecycleOwner, Observer { updatePurchaseOrderUI(it) })
        vm.getConfigItems(UserRoles.GRN.role)
        binding.tvSelectProcurement.setOnClickListener {
            showProcurementTypeDialog()
        }
        binding.tvSelectPO.setOnClickListener {
            //if (year.isNotEmpty())
            showSingleSelectDialog(procureType)
        }
    }

    private fun showProcurementTypeDialog() {
        MaterialDialog(requireContext()).show {
            title(R.string.select_procurement_type)
            listItemsSingleChoice(R.array.procurementType) { _, index, text ->
                binding.tvSelectProcurement.text = text
                binding.tvSelectPO.text = ""
                when (index) {
                    0 -> {
                        binding.llSelectPO.visibility = View.VISIBLE
                        binding.llPrice.visibility = View.GONE
                        vm.getPOList()
                        procureType = FIXED
                    }
                    1 -> {
                        binding.llPrice.visibility = View.VISIBLE
                        binding.llSelectPO.visibility = View.GONE
                        procureType = SPOT
                        wbDetails.purchaseType = procureType
                        binding.etPrice.setText("")
                        binding.etPrice.isEnabled = true
                        binding.tvTotalValue.text = ""
                    }
                }
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }
    private fun showSingleSelectDialog(currentFlag: String) {
        val list = java.util.ArrayList<String>()
        when (currentFlag) {

            FIXED -> {
                filterpurchaseOrderList.clear()
                val purchaseOrderList1 =
                    // purchaseOrderList.filter { it.year == year.trim() }.filter { it.poType == PTBF }
                    purchaseOrderList.filter { it.bsart == FIXED }
                        .filter { it.materialNumber.contains(wbDetails.materialCode ?: "  ") }
                        .filter { it.supplier.trim().contains(wbDetails.supplierCode?.trim() ?: " ") }
                        .filter { it.openQuantity.toString().toDouble() >= wbDetails.netWeight.toString().toDouble()}
                filterpurchaseOrderList.addAll(purchaseOrderList1)
                list.addAll(filterpurchaseOrderList.map { data ->
//                    data.poId.plus("-").plus(data.openQuantity).plus(wbDetails.unitsOfMeasure)
                    data.poId
                })
            }
        }
        if(list.size>0){
            MaterialDialog(requireContext()).show {
                title(R.string.select_purchase_order)
                listItemsSingleChoice(items = list) { _, index, text ->
                    binding.tvSelectPO.text = text
                    val item = filterpurchaseOrderList.singleOrNull { it.poId == text }
                    wbDetails.purchaseDocDesc = item?.ebelp
                    wbDetails.purchaseDocNum = item?.poId
                    wbDetails.purchaseType = item?.poType
                    wbDetails.unitPrice = item?.unitPrice
                    binding.llPrice.visibility = View.VISIBLE
                    binding.etPrice.isEnabled = false
                    binding.etPrice.setText(item?.unitPrice)

                }
                positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
            }
        }else{
            showSnack(getString(R.string.no_po_available))
        }

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
            binding.tvStorageLocation.text = wbDetails.storageLocationCode

        })
        vm.fetchStorageLocation(defaultStorageLocation)
    }

    private fun updateGrnUI(data: Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    mAlertDialog?.dismiss()
                    moveToSuccessPage(it.data?.data?.grnNumber.toString())
                    vm.updateGrnNoToQuality(
                        wbDetails.weighBridgeId.toString(),
                        it.data?.data?.grnNumber.toString(),
                        it.data?.data?.batchNumber.toString()
                    )
                    vm.updateGrnSuccess(
                        wbDetails.weighBridgeId.toString(),
                        it.data?.data?.grnNumber.toString(),
                        it.data?.data?.batchNumber.toString(),
                        getString(R.string.grn_success),
                        4
                    )
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
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
        binding.tvNumberOfBags.text = wbDetails.bagCount
        binding.tvNetWeight.text = wbDetails.netWeight.plus(" ").plus(wbDetails.unitsOfMeasure)
        paidWeight = wbDetails.netWeight.toDouble()
        binding.tvPaidWeight.text = wbDetails.netWeight.plus(" ").plus(wbDetails.unitsOfMeasure)
        binding.tvMaterial.text = wbDetails.materialName
        binding.tvStorageLocation.text = wbDetails.storageLocationCode

        //paidWeight.formatThreeDigits().replace(",", "").plus(" ").plus(wbDetails.unitsOfMeasure)
        //binding.tvStorageLocation.text = defaultStorageLocation
//        if (wbDetails.purchaseDocNum.isNullOrEmpty()) {
//            binding.grnDetailsTitle.text = getString(R.string.grn).plus(" - ").plus(SPOT)
//            binding.tvMaterial.text = getString(R.string.spot_purchase)
//            binding.etPrice.onChange {
//                try {
//                    val paidData = paidWeight.formatThreeDigits().replace(",", "")
//                    val totalVal = it.toDouble() * paidData.toDouble()
//                    binding.tvTotalValue.text = totalVal.formatThreeDigits()
//                    wbDetails.totalPrice = totalVal.formatThreeDigits()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        } else {
//            binding.grnDetailsTitle.text = getString(R.string.grn).plus(" - ").plus(FIXED)
//            binding.tvMaterial.text = getString(R.string.fixed_purchase)
//            binding.tvPoNumber.text = wbDetails.purchaseDocNum
//            binding.llPoNumber.visible()
//            binding.llPriceDetails.gone()
//        }

    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_grn)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(R.string.cancel),
                {
                    //wbDetails.storageLocationCode = binding.tvStorageLocation.text.toString()
                    val wbData = preparePostGrnData(wbDetails)
                    if (isOnline()) {
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
        if (!wbDetails.wbTempId.contains("TMP")) {
            wbDetails.isNotWBID = true
            wbDetails.grnNumber = getTmpId()
        }
        vm.updateGRNPrice(wbDetails)
        moveToSuccessPage(wbDetails.weighBridgeId.toString())
    }

    private fun moveToSuccessPage(grn: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (isOnline()) {
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
        intent.putExtra(AppUtils.MSG, msg)
        startActivity(intent)
    }

    private fun moveToSummaryScreen() {
        callBack?.replaceFragment(GRN_SUMMARY_FRAG, wbDetails)
    }
}
