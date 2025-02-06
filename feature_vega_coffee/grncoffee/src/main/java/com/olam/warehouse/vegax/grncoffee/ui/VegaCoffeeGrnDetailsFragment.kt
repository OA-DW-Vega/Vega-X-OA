package com.olam.warehouse.vegax.grncoffee.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeGrnPost
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeGrnResponse
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grncoffee.R
import com.olam.warehouse.vegax.grncoffee.databinding.FragmentVegaCoffeeGrnDetailsBinding
import com.olam.warehouse.vegax.grncoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*


/**
 * Created by Keerthi Santhanam on 6/30/2020.
 */
class VegaCoffeeGrnDetailsFragment : BaseFragment(), VegaSingleSelectCommonListener {
    private var paidWeight: Double = 0.0
    private var mAlertDialog: AlertDialog? = null
    private val vm: VegaCoffeeGrnViewModel by viewModel()
    private var wbDetails = VegaGrnWeighBridgeId()
    private var currentMaterial: String = ""
    private var defaultStorageLocation: String = ""
    private var callBack: CallBack? = null
    private var purchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var qualitylist = arrayListOf<VegaQualityParams>()
    private var qualitylist1 = arrayListOf<VegaQualityParams>()
    private var filterpurchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var yearFilter = mutableListOf<String>()
    private var year = ""
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null
    private lateinit var binding: FragmentVegaCoffeeGrnDetailsBinding
    override val layoutResourceId = R.layout.fragment_vega_coffee_grn_details
    private var origin: String? = ""
    private var department: String? = ""
    private var challanNumber: String = ""

    interface CallBack {
        fun replaceFragment(moveFrag: String, item: VegaGrnWeighBridgeId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCoffeeGrnDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("approveCoffee/ui/details/VegaCoffeeGrnDetailsFragment").title("IVC/Coffee/GRN/Details")
            .with(tracker)
        initUI()
    }

    companion object {
        fun newInstance(grnData: VegaGrnWeighBridgeId) = VegaCoffeeGrnDetailsFragment().putArgs {
            putParcelable(GRN_DATA, grnData)
        }
    }

    private fun initUI() {
        wbDetails = arguments?.getParcelable(GRN_DATA)!!
        if (wbDetails.unitPrice!!.isNotEmpty()&& !wbDetails.unitPrice.equals("0.00")) {
            binding.etPrice.setText(wbDetails.unitPrice.toString())
            binding.etPrice.isEnabled = false
            binding.tvTotalValue.text = wbDetails.totalPrice
        }
        binding.tvPo.isEnabled=false

        currentMaterial = wbDetails.materialCode.toString()
        updateUIValues()
        updateYearValues()
        if(wbDetails.weighMethod.equals("WS"))
            vm.getWeighBridgeIdDetail( wbDetails.weighBridgeId.toString(), true)
        else
            vm.getWeighBridgeIdDetail(wbDetails.weighBridgeId.toString(), false)
        vm.weighBridgeId.observe(viewLifecycleOwner, Observer { updateWeighbridgeValueUI(it) })
        vm.grn.observe(viewLifecycleOwner, Observer { updateGrnUI(it) })
        vm.poList.observe(viewLifecycleOwner, Observer { updatePurchaseOrderUI(it) })
        binding.btnQuality.setOnClickListener { callBack?.replaceFragment(QUALITY_DETAILS, wbDetails) }
        binding.tvUsdLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.final_price_per_unit)) { mandatoryStars() } }
        binding.btnProceed.setOnClickListener {
            if (wbDetails.purchaseDocNum.isNullOrEmpty()) {
                // wbDetails.unitPrice = binding.etPrice.text.toString()
                when {
                    //binding.etPrice.text.isNullOrEmpty() -> showSnack(getString(R.string.price_validation))
                    wbDetails.purchaseDocNum.isNullOrEmpty() -> showSnack(getString(R.string.select_po_error))
                    else -> showConfirmDialog()
                }
            } else showConfirmDialog()
        }
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.getConfigItems(UserRoles.GRN.role)

        binding.tvPo.setOnClickListener {
            if (year.isNotEmpty())
            {
                showLoading()
                showSingleSelectDialog(getString(R.string.select_purchase_order), PO)
            }

        }

        binding.tvYearValue.setOnClickListener {
            showLoading()
            showSingleSelectDialog(getString(R.string.select_year), YEAR)
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
            //tvStorageLocation.text = it?.storageLocationName
        })
        vm.fetchStorageLocation(defaultStorageLocation)
    }

    private fun updateYearValues() {
        val current: Int = Calendar.getInstance().get(Calendar.YEAR)
        binding.tvYearValue.text = current.toString()
        year = current.toString()
        for (i in current.minus(1)..current) {
            yearFilter.add(i.toString())
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
                                binding.tvPo.isEnabled=true
                                for (drawable in binding.tvPo.compoundDrawables) {
                                    if (drawable != null) {
                                        drawable.colorFilter = PorterDuffColorFilter(
                                            Color.parseColor("#0B4B52"),
                                            PorterDuff.Mode.SRC_IN
                                        )
                                    }
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

    }

    private fun updateGrnUI(data: Resource<GenericReqAndResp<VegaCoffeeGrnResponse>>) {
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
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
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
        binding.tvStorageLocation.text = wbDetails.storageLocationCode
        binding.tvMaterialValue.text = wbDetails.materialName
        binding.tvBatchNumber.text = wbDetails.batchNumber
        binding.tvTruckNo.text = wbDetails.vehicleNumber
        //paidWeight.formatThreeDigits().replace(",", "").plus(" ").plus(wbDetails.unitsOfMeasure)
        //binding.tvStorageLocation.text = defaultStorageLocation
        if (wbDetails.purchaseDocNum.isNullOrEmpty()) {
            binding.grnDetailsTitle.text = getString(R.string.grn).plus(" - ").plus(FIXED)
            binding.tvProcurementType.text = getString(R.string.fixed_purchase)
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
            binding.llPoNumber.gone()
            binding.llPo.visible()
            binding.llYear.visible()
            binding.llPriceDetails.gone()
            vm.getPOList()
        } else {
            binding.grnDetailsTitle.text = getString(R.string.grn).plus(" - ").plus(FIXED)
            binding.tvProcurementType.text = getString(R.string.fixed_purchase)
//            binding.tvPo.text = wbDetails.purchaseDocNum
            binding.tvPoNumber.text = wbDetails.purchaseDocNum
            binding.llPoNumber.visible()
            binding.llPriceDetails.gone()
            binding.llPo.gone()
            binding.llYear.gone()
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
                    if (isOnline()) {
                        vm.postGrn(
                            VegaCoffeeGrnPost(
                                key = getCurrentKey(),
                                challanNumber,
                                plant = getPlantDetails(),
                                grnData = listOf(wbData),
                                qualityDetails = preparePostGrnData1(qualitylist)
                            )
                        )
                    } /*else {
                    saveData()
                }*/
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

    private fun showSingleSelectDialog(title: String, currentFlag: String) {
        val list = ArrayList<String>()
        when (currentFlag) {
            PO -> {

                filterpurchaseOrderList.clear()
                val purchaseOrderList1 =
                    purchaseOrderList.filter { it.year == year.trim() }.filter { it.poType == PTBF }
                        .filter { it.materialNumber.contains(wbDetails.materialCode ?: "  ") }
                        .filter {
                            it.supplier.trim().contains(wbDetails.supplierCode?.trim() ?: " ")
                        }
                filterpurchaseOrderList.addAll(purchaseOrderList1)

                list.addAll(filterpurchaseOrderList.map { data ->
                    data.poId.plus("-").plus(data.openQuantity).plus(wbDetails.unitsOfMeasure)
                })
            }
            YEAR -> {
                list.addAll(yearFilter)

            }

        }
        customDialog =
            VegaCommonSingleSelectDialogWithSearch(
                title,
                currentFlag,
                list,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
        hideLoading()
    }

    override fun clickOnItem(data: String, currentFlag: String) {
        customDialog?.dismiss()
        when (currentFlag) {
            YEAR -> {
                year = data
                binding.tvYearValue.text = data
            }
            PO -> {
                binding.tvPo.text = data
                val split = data.split("-")
                val item = filterpurchaseOrderList.singleOrNull { it.poId == split[0] }
                wbDetails.purchaseDocNum = item?.poId
                wbDetails.purchaseDocDesc = item?.ebelp
                // wbDetails.purchaseDocQty = item?.openQuantity
                wbDetails.unitsOfMeasure = item?.meins ?: ""
            }
        }
    }
    private fun updateqyaulityUI(data: Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>?) {
        qualitylist.clear()
        qualitylist1.clear()
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let {
                        if (it.size > 0) {
                            vm.getQualityParams(wbDetails.materialCode!!, false, wbDetails.weighBridgeId)
                            vm.qualitylist.observe(viewLifecycleOwner, Observer { updatequalityparams(it) })
                            //setUpAdapter(it[0].qualityParameters)
                            qualitylist1.addAll(it[0].qualityParameters!!)
                            qualitylist.addAll(qualitylist1.filter { !it.satNam.equals("NULL") })
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
            }
        }
    }
    private fun updatequalityparams(params: List<VegaQualityParamsWithQualitative>?) {

        params?.let {
            when (it.isNotEmpty()) {
                true -> {
                    it.forEach { item ->
                        if(item.qualityParameter.nameChar=="CI_COFFEE_TRANS_ORIGIN")
                        {
                            var VegaQualityParams=VegaQualityParams()
                            VegaQualityParams.qualityParameterId=""
                            VegaQualityParams.qualityParameterName=item.qualityParameter.descrChar
                            VegaQualityParams.qualityParameterType=""
                            VegaQualityParams.maxValue=""
                            VegaQualityParams.minValue=" "
                            VegaQualityParams.sapQCName=item.qualityParameter.nameChar
                            VegaQualityParams.satNam=origin
                            qualitylist.add(VegaQualityParams)
                        }
                        if(item.qualityParameter.nameChar=="CI_COFFEE_TRANS_DEPT")
                        {
                            var VegaQualityParams=VegaQualityParams()
                            VegaQualityParams.qualityParameterId=""
                            VegaQualityParams.qualityParameterName=item.qualityParameter.descrChar
                            VegaQualityParams.qualityParameterType=""
                            VegaQualityParams.maxValue=""
                            VegaQualityParams.minValue=" "
                            VegaQualityParams.sapQCName=item.qualityParameter.nameChar
                            VegaQualityParams.satNam=department
                            qualitylist.add(VegaQualityParams)
                        }
                        qualitylist.forEach {
                            if(it.sapQCName==item.qualityParameter.nameChar)
                            {
                                it.qualityParameterName=item.qualityParameter.descrChar
                                /*if(it.sapQCName=="CI_COFFEE_TRANS_DEPT")
                                {
                                   it.satNam=department
                                }
                                else if(it.sapQCName=="CI_COFFEE_TRANS_ORIGIN")
                                {
                                    it.satNam=origin
                                }*/
                            }
                        }
                    }

                }
                else -> setErrorContentView("Quality params not available for this material")
            }
        }
    }
    private fun updateWeighbridgeValueUI(response: Resource<GenericReqAndResp<VegaReceiving>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                origin = response.data?.data?.origin
                department=response.data?.data?.department
                challanNumber = response.data?.data?.challan.toString()
                println("============challan=====================>$challanNumber")
                vm.getPreSamplingQualitydata(wbDetails.batchNumber ?: "", wbDetails.materialCode ?: "")
                vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updateqyaulityUI(it) })
                //vm.getPreSamplingQualitydata(inventoryLots.batchNumber ?: "", inventoryLots.materialCode ?: "")
                // vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updateUI(it) })

            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
            else -> {}
        }

    }
}
