package com.olam.warehouse.vegax.grnnicaragua.ui.spot

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.model.AdvanceLineItemDetails
import com.olam.warehouse.master.common.utils.generateInvoiceRefNumber
import com.olam.warehouse.master.common.utils.generateInvoiceRefNumberAgain
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.common.utils.saveGrnSequence
import com.olam.warehouse.master.common.utils.saveInvoiceSequence
import com.olam.warehouse.master.common.utils.validateInvoiceNoIsAlreadyExist
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnPriceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.master.veganicaragua.model.*
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnnicaragua.R
import com.olam.warehouse.vegax.grnnicaragua.databinding.FragmentVegaNicaraguaGrnSpotSummaryBinding
import com.olam.warehouse.vegax.grnnicaragua.databinding.ItemVegaNicaraguaAdvanceBinding
import com.olam.warehouse.vegax.grnnicaragua.databinding.ItemVegaNicaraguaGrnSummaryBinding
import com.olam.warehouse.vegax.grnnicaragua.ui.VegaNicaraguaGrnViewModel
import com.olam.warehouse.vegax.grnnicaragua.utils.covertToDouble
import com.olam.warehouse.vegax.grnnicaragua.utils.getFormatedDate
import com.olam.warehouse.vegax.grnnicaragua.utils.isdatefunc
import com.olam.warehouse.vegax.grnnicaragua.utils.saveLotSequence
import com.olam.warehouse.vegax.grnnicaragua.work.getGrnLotSequnceOneTimeRequestWorker
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

class VegaNicaraguaGrnSpotSummaryFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private lateinit var binding: FragmentVegaNicaraguaGrnSpotSummaryBinding
    private var receivingData = VegaReceiving()
    private var pricingInfo = VegaNicaraguaInvoicePriceInfo()
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var priceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()
    private var transactionList = mutableListOf<VegaReceiving>()
    private var weighDetails: List<VegaNicaraguaWeighmentBagMaterial>? = null
    private val vm: VegaNicaraguaGrnViewModel by viewModel()
    var advanceKnockOfList: ArrayList<AdvanceLineItemDetails>? = ArrayList<AdvanceLineItemDetails>()
    var count:Int=0
    private var isMoveOfflineSuccess = false
    private var intent = Intent()
    private var invoiceTransList = mutableListOf<com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaInvoiceDetails>()

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            data: Bundle
        )
    }

    companion object {
        fun newInstance(data: Bundle) =
            VegaNicaraguaGrnSpotSummaryFragment().putArgs {
                putAll(data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_grn_spot_summary

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaGrnSpotSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnSpotSummary/ui/VegaNicaraguaGrnSpotSummaryFragment")
            .title("GRN SPOT Summary")
            .with(tracker)
        intent = Intent(requireContext(), SuccessActivity::class.java)
        initExtra()
        initUI()
    }

    private fun initUI() {
        if (receivingData.grnType.equals(getString(R.string.fixed))) {
            binding.title.text = getString(R.string.grn_fixed_summary)
            binding.tvPoNumber.text = receivingData.purchaseDocNum
            binding.tvPoHeader.visible()
            binding.tvPoNumber.visible()
        }

        setVendorData()

        setQualityDetails()

        setPricingInfo()
        settingTrackTracevalue()

        vm.getConfigItems(UserRoles.DAYS_LIMIT.role)
        vm.configItems.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            updateConfigItems(it)
        })
        binding.btnProceed.setOnClickListener(View.OnClickListener {
            isMoveOfflineSuccess = false
            receivingData.invoiceNumber =
                if (receivingData.invoiceNumber?.isEmpty() == true || receivingData.invoiceNumber.isNullOrBlank()) generateInvoiceRefNumberAgain(transactionList, invoiceTransList) else receivingData.invoiceNumber
           /* if(receivingData.invoiceNumber?.let { it1 -> validateInvoiceNoIsAlreadyExist(transactionList, invoiceTransList, it1)} == true){
                receivingData.invoiceNumber = generateInvoiceRefNumberAgain(transactionList, invoiceTransList)
            }*/
            if (AppUtils.isOnline()) {
                if (!receivingData.postDate.toString().isNullOrEmpty()) {
                    if (isdatefunc(receivingData.postDate.toString()))
                        showConfirmDialog()
                    else
                        moveToFailurePage(getString(R.string.future_date_pendinglist))
                } else
                    showConfirmDialog()
            } else showConfirmDialog()

        })
        binding.btnSave.setOnClickListener { saveEditData() }
        vm.postGRN.observe(viewLifecycleOwner, androidx.lifecycle.Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let {
                            receivingData.price = String.format(
                                Locale.ENGLISH, "%.4f",
                                covertToDouble(
                                    (binding.TvGrossValue.text.toString()).replace(
                                        getString(R.string.c_doller),
                                        ""
                                    )
                                        .trim()
                                ) / covertToDouble(receivingData.netWeight)
                            )
                            receivingData.syncStatusMsg = it.message
                            receivingData.netPayment = pricingInfo.netPayment
                            var advance = 0.0
                            advanceKnockOfList!!.forEach {
                                advance = covertToDouble(it.totalAdvanceKnockAmount!!) + advance
                            }
                            receivingData.advance = advance.toString()
                            vm.updateGrnDataSuccess(it.data, receivingData)
                            val poNumber =
                                if (it.data.poNumber.isNullOrEmpty()) receivingData.purchaseDocNum else it.data.poNumber
                            if (it.data.wbFlag == true && it.data.qcFlag == true && it.data.grnFlag == true && it.data.invoiceFlag == true)
                                moveToSuccessPage(
                                    it.message,
                                    it.data.wbId,
                                    it.data.batchNumber,
                                    it.data.grnNumber,
                                    poNumber,
                                    receivingData.invoiceNumber
                                )
                            else
                                moveToFailurePage(it.message)
                        }
                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        receivingData.syncStatusMsg = it.error.toString()
                        receivingData.netPayment = pricingInfo.netPayment
                        var advance = 0.0
                        advanceKnockOfList!!.forEach {
                            advance = covertToDouble(it.totalAdvanceKnockAmount!!) + advance
                        }
                        receivingData.advance = advance.toString()
                        vm.updateGrnDataSuccess(VegaNicaraguaGrnPost(), receivingData)
                        hideLoading()
                        //showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                        moveToFailurePage(
                            if (it.error.toString().isNotEmpty()) it.error.toString() else ""
                        )
                    }
                }
            }
        })
    }

    private fun updateTransUI(it: List<VegaReceiving>?) {
        it?.let {
            transactionList.clear()
            transactionList.addAll(it)
        }

        receivingData.invoiceNumber =
            if (receivingData.invoiceNumber?.isEmpty() == true && !receivingData.invoiceNumber.isNullOrBlank()) generateInvoiceRefNumber(transactionList, invoiceTransList) else receivingData.invoiceNumber
        if(isMoveOfflineSuccess){
            isMoveOfflineSuccess = false
            saveLotSequence(receivingData.batchNumber!!, transactionList, false)
            saveInvoiceSequence(transactionList, false, invoiceTransList, false)
            saveGrnSequence(transactionList, false)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun moveToFailurePage(msg: String) {
        if (AppUtils.isOnline()) postUpdateLotSequence(receivingData.batchNumber.toString())
        else {
            isMoveOfflineSuccess = true
           /* saveLotSequence(receivingData.batchNumber!!, transactionList, false)
            saveInvoiceSequence(transactionList, false, invoiceTransList)
            saveGrnSequence(transactionList, false)*/
        }
//        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.transaction_fail_msg))
        intent.putExtra(AppUtils.SUB_TITLE, msg.plus("\n").plus(getString(R.string.transaction_retry)))
        intent.putExtra(AppUtils.FAILURE, false)
        intent.putExtra(AppUtils.MSG, msg)
//        startActivity(intent)
    }

    private fun setVendorData() {
        binding.tvMaterial.text = receivingData.materialCode.plus("\n").plus(receivingData.materialName)
        binding.tvGrade.text = receivingData.grade.plus("\n").plus(receivingData.gradeDesc)
        binding.tvStorageLoctaion.text = receivingData.storageLocationName
        binding.tvNetWeight.text = pricingInfo.netWeight    
        binding.vendorNameTitle.text = receivingData.supplierName
        binding.tvpostingdateVal.text =receivingData.postDate
    }

    private fun setQualityDetails() {

        binding.qualityDetails.setOnClickListener(View.OnClickListener {
            if (binding.qualityDetailsLayout.isVisible) {
                binding.qualityDetailsLayout.visibility = View.GONE
                binding.qualityDetails.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_baseline_expand_more,
                    0
                )
            } else {
                binding.qualityDetailsLayout.visibility = View.VISIBLE
                binding.qualityDetails.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_baseline_expand_less,
                    0
                )
            }
        })

        binding.rvQuality.setUpAdapter(
            qualityParameterList,
            R.layout.item_vega_nicaragua_grn_summary,
            ItemVegaNicaraguaGrnSummaryBinding::inflate,
            { it, pos, bindItem ->

                bindItem.description.text = it!!.descrChar
                bindItem.value.text = it.qualityParameterValue!!.trim()
            })
        binding.rvQuality.isNestedScrollingEnabled = false
    }

    private fun settingTrackTracevalue(){
        /*indirect flow*/
        if(receivingData.sourceLotId.isNotEmpty()){
            binding.llSourceLot.visible()
            binding.llEudrStatus.visible()
            binding.tvSourceLotId.setText(receivingData.sourceLotId)
            if(receivingData.eudrStatus){
                binding.tvEudrStatus.setText(Constants.EUDR_QP_VALUE)
            } else {
                binding.tvEudrStatus.setText(Constants.ATTR_UNKNOWN_QP_VALUE)
            }
        }

    }

    private fun setPricingInfo() {
        binding.priceCalculationDetails.setOnClickListener(View.OnClickListener {
            if (binding.priceCalculationLayout.isVisible) {
                binding.priceCalculationLayout.visibility = View.GONE
                binding.priceCalculationDetails.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_baseline_expand_more,
                    0
                )
            } else {
                binding.priceCalculationLayout.visibility = View.VISIBLE
                binding.priceCalculationDetails.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_baseline_expand_less,
                    0
                )
            }
        })

        binding.tvExchangeRate.text = pricingInfo.exchangeRate
        binding.tvTotalPrice.text = getString(R.string.c_doller) + " " + pricingInfo.totalPrice
        binding.tvBasePrice.text = getString(R.string.c_doller) + " " + pricingInfo.basePrice
        binding.tvCertificatePremium.text = getString(R.string.c_doller) + " " + pricingInfo.certificatePremium
        if(receivingData.grnType.equals(getString(R.string.fixed)))
        {
            binding.volumepremiumLL.gone()
            binding.humiditypremiumLL.gone()
        }else{
            binding.volumepremiumLL.gone()
            binding.humiditypremiumLL.gone()
        }
        binding.tvVolumePremium.text = getString(R.string.c_doller) + " " + pricingInfo.volumePremium
        binding.tvHumidityPremium.text = getString(R.string.c_doller) + " " + pricingInfo.humidityPremium
        binding.tvQualityDiscounting.text = getString(R.string.c_doller) + " " + pricingInfo.qualityDiscounting
        binding.tvHumidityDiscounting.text = getString(R.string.c_doller) + " " + pricingInfo.humidityDiscounting
        binding.exportIncentive.text = getString(R.string.c_doller) + " " + pricingInfo.exportnCentives
        binding.tvWitrHoldingTax.text = getString(R.string.c_doller) + " " + pricingInfo.withholdingTax
        binding.tvNationalStockExchange.text = getString(R.string.c_doller) + " " + pricingInfo.NSExchangeRate
        binding.tvBankCommission.text = getString(R.string.c_doller) + " " + pricingInfo.bankCommission
        binding.tvFTDC.text = getString(R.string.c_doller) + " " + pricingInfo.FTDC
        binding.subTotalDeduction.text = getString(R.string.c_doller) + " " + pricingInfo.totalDduction
        binding.tvNetPayment.text = getString(R.string.c_doller) + " " + pricingInfo.netPayment
        binding.TvFinalPayment.text = getString(R.string.c_doller) + " " + pricingInfo.finalPayment
        binding.TvGrossValue.text = getString(R.string.c_doller) + " " + pricingInfo.grossValue
        if (advanceKnockOfList?.size!! > 0) {
            binding.advanceMainLayout.visibility = View.VISIBLE
            binding.advanceLineItems.setUpAdapter(
                advanceKnockOfList!!.toMutableList(),
                R.layout.item_vega_nicaragua_advance,
                ItemVegaNicaraguaAdvanceBinding::inflate,
                { it, pos, bindItem ->
                    bindItem.tvAdvanceNo.text = it.documentNumber
                    bindItem.tvAmt.text = it.amount
                    bindItem.tvKnockAmt.text = it.advanceKnockAmount
                    bindItem.tvInterest.text = it.interestAmount
                    bindItem.tvCommission.text = it.commissionAmount
                    bindItem.tvLegalExp.text = it.legalExpenseAmount
                    bindItem.tvCurrencyDevaluation.text = it.currencyDevaluationAmount
                    val totalAmt =
                        (if (it.advanceKnockAmount?.isNotEmpty() == true) it.advanceKnockAmount?.toDouble() else 0.0)?.plus(
                            if (it.interestAmount?.isNotEmpty() == true) it.interestAmount?.toDouble()
                                ?: 0.0 else 0.0
                        )?.plus(
                            if (it.commissionAmount?.isNotEmpty() == true) it.commissionAmount?.toDouble()
                                ?: 0.0 else 0.0
                        )
                            ?.plus(
                                if (it.legalExpenseAmount?.isNotEmpty() == true) it.legalExpenseAmount?.toDouble()
                                    ?: 0.0 else 0.0
                            )?.plus(
                                if (it.currencyDevaluationAmount?.isNotEmpty() == true) it.currencyDevaluationAmount?.toDouble()
                                    ?: 0.0 else 0.0
                            )
                    bindItem.tvTotalAmt.text = totalAmt.toString()
                    val date = DateUtils.getUTCDateTimeNicaragua(
                        DateUtils.getCurrentTimeInMills().toString(),
                        context
                    )
                    if (!date.isNullOrEmpty()) {
                        bindItem.tvAdvanceDate.text = date
                    }
                    bindItem.ivDown.setOnClickListener {
                        when (bindItem.llAmtDetails.isVisible) {
                            true -> {
                                bindItem.llAmtDetails.gone()
                                bindItem.ivDown.setImageDrawable(
                                    bindItem.ivDown.context.getDrawable(
                                        com.olam.warehouse.presentation.R.drawable.ic_arrow_down_black_24dp
                                    )
                                )
                            }
                            else -> {
                                bindItem.llAmtDetails.visible()
                                bindItem.ivDown.setImageDrawable(
                                    bindItem.ivDown.context.getDrawable(
                                        com.olam.warehouse.presentation.R.drawable.ic_arrow_up
                                    )
                                )
                            }
                        }
                    }
                })
        } else {
            binding.advanceMainLayout.visibility = View.GONE
        }


    }

    private fun initExtra() {
        arguments?.let {
            receivingData = it.getParcelable(UIUtils.RECEIVING_DATA)!!
            pricingInfo = it.getParcelable(UIUtils.INVOICE_PRICE_INFO)!!
            qualityParameterList = it.getParcelableArrayList<VegaQualityParameter>(UIUtils.QUALITY_DATA)!!
            priceDetails = it.getParcelableArrayList<VegaNicaraguaGrnPriceDetails>(UIUtils.PRICE_DATA)!!
            weighDetails = it.getParcelableArrayList<VegaNicaraguaWeighmentBagMaterial>(UIUtils.BAGS_DATA)!!
            advanceKnockOfList = it.getParcelableArrayList<AdvanceLineItemDetails>(UIUtils.ADVANCE_KNOCK_DATA)!!
            updatePriceDeatilsToDB()
            if (receivingData.isEdit) binding.btnSave.visible()
            /*receivingData.invoiceNumber =
                if (receivingData.invoiceNumber?.isEmpty() == true) generateInvoiceRefNumber() else receivingData.invoiceNumber*/
        }
        vm.invoiceOffline.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateInvoiceTransUI(it) })
        vm.getInvoiceOfflineData()
        vm.grnTransList.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateTransUI(it) })
    }

    private fun updateInvoiceTransUI(it: List<com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaInvoiceDetails>?) {
        it?.let { item ->
            invoiceTransList.clear()
            invoiceTransList.addAll(item)
        }
        vm.getReceivingWithLineItem()
    }

    private fun updatePriceDeatilsToDB() {
        receivingData.basePrice = pricingInfo.basePrice
        receivingData.certificatePremium = pricingInfo.certificatePremium
        receivingData.volumePremium = pricingInfo.volumePremium
        receivingData.humidityPremium = pricingInfo.humidityPremium
        receivingData.qualityDiscounting = pricingInfo.qualityDiscounting
        receivingData.humidityDiscounting = pricingInfo.humidityDiscounting
        receivingData.grossValue = pricingInfo.grossValue
        receivingData.exportnCentives = pricingInfo.exportnCentives
        receivingData.withholdingTax = pricingInfo.withholdingTax
        receivingData.NSExchangeRate = pricingInfo.NSExchangeRate
        receivingData.bankCommission = pricingInfo.bankCommission
        receivingData.totalDduction = pricingInfo.totalDduction
        receivingData.totalPrice = pricingInfo.totalPrice
        receivingData.finalPayment = pricingInfo.finalPayment
        receivingData.currency = pricingInfo.currency
        receivingData.grossValuePerKg = pricingInfo.grossValuePerKg
        receivingData.qualityGradeDesc = pricingInfo.qualityGradeDesc
        receivingData.netWeightQQs = pricingInfo.netWeightQQs
        receivingData.advanceSummary = pricingInfo.advanceSummary
        receivingData.advanceInterestSummary = pricingInfo.advanceInterestSummary
        receivingData.advanceCommissionSummary = pricingInfo.advanceCommissionSummary
        receivingData.advanceLegalExpenseSummary = pricingInfo.advanceLegalExpenseSummary
        receivingData.advanceMaintainceSummary = pricingInfo.advanceMaintainceSummary
        receivingData.totalAdvanceSummary = pricingInfo.totalAdvanceSummary
        receivingData.exchangeRate = pricingInfo.exchangeRate
        receivingData.bagCount =
            weighDetails?.sumOf { if (it.bagCount.isNotEmpty()) it.bagCount.toInt() else 0 }.toString()
        receivingData.grossWeight =
            weighDetails?.sumOf { if (it.grossWeight.isNotEmpty()) it.grossWeight.toDouble() else 0.0 }
                .toString()
        receivingData.tareWeight = weighDetails?.sumOf {
            if (it.tareWeight?.isNotEmpty() == true) it.tareWeight?.toDouble() ?: 0.0 else 0.0
        }.toString()
        receivingData.netWeight = weighDetails?.sumOf {
            if (it.netWeight.isNotEmpty() == true) it.netWeight.toDouble() else 0.0
        }.toString()
    }

    private fun savefutureData() {
        PreferenceHelper.save(Constants.IS_EDIT_TRANS_VALUE_CHANGED, false)
        receivingData.price = binding.tvBasePrice.text.toString().replace(getString(R.string.c_doller), "").trim()
        receivingData.createdDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receivingData.erdat = DateUtils.getCurrentTimeInMills().toString()
        receivingData.docDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receivingData.netPayment = binding.tvTotalPrice.text.toString()
        isMoveOfflineSuccess = true
        vm.saveGrnData(receivingData)
        moveToFailurePage("Future Posting data saved successfully")
    }
    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.grn_proceed)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    if (AppUtils.isOnline())
                        postCreateGRN()
                    else
                        saveData()
                },
                { dismiss() })
        }
    }

    private fun saveData() {
        PreferenceHelper.save(Constants.IS_EDIT_TRANS_VALUE_CHANGED, false)
        receivingData.price = String.format(
            Locale.ENGLISH, "%.4f",
            covertToDouble(
                pricingInfo.grossValue
            ) / covertToDouble(receivingData.netWeight)
        )

        receivingData.createdDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receivingData.erdat = DateUtils.getCurrentTimeInMills().toString()
        receivingData.docDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receivingData.netPayment = pricingInfo.netPayment
        var advance = 0.0
        advanceKnockOfList!!.forEach {
            advance = covertToDouble(it.totalAdvanceKnockAmount!!) + advance
        }
        receivingData.advance = advance.toString()
        receivingData.direction = "Offline"
        isMoveOfflineSuccess = true
        vm.saveGrnData(receivingData)
        moveToSuccessPage(
            getString(R.string.grn_offline_success),
            receivingData.tmpWbId,
            receivingData.batchNumber,
            receivingData.palletType,
            "",
            receivingData.invoiceNumber
        )
    }

    private fun saveEditData() {
        PreferenceHelper.save(Constants.IS_EDIT_TRANS_VALUE_CHANGED, false)
        receivingData.price = String.format(
            Locale.ENGLISH, "%.4f",
            covertToDouble(
                pricingInfo.grossValue
            ) / covertToDouble(receivingData.netWeight)
        )

        receivingData.createdDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receivingData.erdat = DateUtils.getCurrentTimeInMills().toString()
        receivingData.docDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receivingData.netPayment = pricingInfo.netPayment
        var advance = 0.0
        advanceKnockOfList!!.forEach {
            advance = covertToDouble(it.totalAdvanceKnockAmount!!) + advance
        }
        receivingData.advance = advance.toString()
        vm.saveGrnData(receivingData)
        showEditConfirmDialog()
    }

    private fun showEditConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.edit_confirm)
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true)) {
                activity?.finish()
            }
        }
    }

    private fun postCreateGRN() {
        PreferenceHelper.save(Constants.IS_EDIT_TRANS_VALUE_CHANGED, false)
        vm.updateSyncStartedStatus(receivingData.tmpWbId)
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        val post = VegaNicaraguaGrnPost()

        post.batchNumber = receivingData.batchNumber
        post.wbId = receivingData.weighBridgeId
        post.certificate = receivingData.certificate
        post.qualityGradeDesc = receivingData.gradeDesc
        post.cascara = receivingData.cascara
        post.humedad = receivingData.humedad
        post.rendimientoBruto = receivingData.rendimientoBruto
        post.exchangeRate = pricingInfo.exchangeRate
        post.grade = null
        post.key = currentKey
        post.grnType = receivingData.grnType
        post.plant = getPlantDetails()
        post.grnData = preparingGrnData()
        post.lotDetails = preparingLotDetails()
        post.priceDetails = priceDetails
        post.tollingDetails = null
        post.wbFlag = receivingData.wbFlag
        post.qcFlag = receivingData.qcFlag
        post.grnFlag = receivingData.grnFlag
        //adding weighDetails
        post.weighDetails = weighDetails
        post.poNumber = receivingData.purchaseDocNum
        post.invoiceDetails = preparingInvoiceData()
        post.tempGRNNumber = receivingData.palletType
        post.transactionMode = "ON"
        post.ttFarmerList = receivingData.ttFarmerList
        post.farmerTransDetails = receivingData.farmerTransDetails
        post.farmerLessTransactionId = receivingData.farmerLessTransactionId.toString()
        if (DateUtils.isFirstDayOfMonth(requireContext(), count)) {
            vm.postCreateGRN(post)
        } else {
            showSnack(getString(R.string.month_close_error))
        }
    }
    private fun preparingInvoiceData():java.util.ArrayList< VegaNicaraguaInvoiceDetails> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

        var invoiceList = java.util.ArrayList<VegaNicaraguaInvoiceDetails>()

        var advanceSummary:Double=0.0
        var interestSummary:Double=0.0
        var commissionSummary:Double=0.0
        var legalExpenseSummary:Double=0.0
        var currencyDevaluationSummary:Double=0.0
        var totalAdvanceSummary:Double=0.0

        if (advanceKnockOfList!!.size > 0) {

            advanceKnockOfList!!.forEach {
                advanceSummary = covertToDouble(it.advanceKnockAmount!!) + advanceSummary
                interestSummary = covertToDouble(it.interestAmount!!) + interestSummary
                commissionSummary = covertToDouble(it.commissionAmount!!) + commissionSummary
                legalExpenseSummary = covertToDouble(it.legalExpenseAmount!!) + legalExpenseSummary
                totalAdvanceSummary = covertToDouble(it.totalAdvanceKnockAmount!!) + totalAdvanceSummary
                currencyDevaluationSummary = covertToDouble(it.currencyDevaluationAmount!!) + currencyDevaluationSummary
            }
        }

        val invoiceDetails = VegaNicaraguaInvoiceDetails()

        invoiceDetails.advanceKnockOffAmt=String.format(Locale.ENGLISH, "%.2f", advanceSummary)
        invoiceDetails.advanceLineItems=prepareAdvanceLineItems()
        invoiceDetails.bankCommission= pricingInfo.bankCommission
        invoiceDetails.certificatePremium= pricingInfo.certificatePremium
        invoiceDetails.exchangeRate=pricingInfo.exchangeRate
        invoiceDetails.exportIncentive= pricingInfo.exportnCentives
        invoiceDetails.finalPayment= pricingInfo.finalPayment
        invoiceDetails.ftdc= pricingInfo.FTDC
        invoiceDetails.grade=receivingData.grade
        invoiceDetails.grnNetWeight=receivingData.netWeight
        invoiceDetails.grnType=receivingData.grnType
        invoiceDetails.grossValue= pricingInfo.grossValue
        invoiceDetails.humidityDiscounting= pricingInfo.humidityDiscounting
        if(receivingData.grnType.equals(getString(R.string.fixed))) {
            invoiceDetails.humidityPremium = "0.0"
            invoiceDetails.volumePremium = "0.0"
        }else{
            invoiceDetails.volumePremium = pricingInfo.volumePremium
            invoiceDetails.humidityPremium = pricingInfo.humidityPremium
        }
        invoiceDetails.invoiceDate= DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        invoiceDetails.key=currentKey
        invoiceDetails.materialCode=receivingData.materialCode
        invoiceDetails.materialName=receivingData.materialName
        invoiceDetails.nationalStockExchange= pricingInfo.NSExchangeRate
        invoiceDetails.netPayment=pricingInfo.netPayment
        invoiceDetails.netWeight=receivingData.netWeight
        invoiceDetails.plant= getPlantDetails()
        invoiceDetails.poPrice= pricingInfo.basePrice
        invoiceDetails.qualityGradeDesc=receivingData.gradeDesc
        invoiceDetails.qualityPremium = pricingInfo.qualityDiscounting
        invoiceDetails.storageLocationCode = receivingData.storageLocationCode
        invoiceDetails.storageLocationName = receivingData.storageLocationName
        invoiceDetails.subTotalDeductions = pricingInfo.totalDduction
        invoiceDetails.tempNumber = receivingData.invoiceNumber
        invoiceDetails.totalPrice = pricingInfo.totalPrice
        invoiceDetails.vendorCode = receivingData.supplierCode
        invoiceDetails.vendorName = receivingData.supplierName
        invoiceDetails.withHoldingTax = pricingInfo.withholdingTax
        invoiceDetails.bagCount =
            weighDetails?.sumOf { if (it.bagCount.isNotEmpty()) it.bagCount.toInt() else 0 }.toString()
        invoiceDetails.grossWeight =
            weighDetails?.sumOf { if (it.grossWeight.isNotEmpty()) it.grossWeight.toDouble() else 0.0 }
                .toString()
        invoiceDetails.tareWeight = weighDetails?.sumOf {
            if (it.tareWeight?.isNotEmpty() == true) it.tareWeight?.toDouble() ?: 0.0 else 0.0
        }.toString()
        invoiceDetails.batchNumber = receivingData.batchNumber
        invoiceDetails.certification = receivingData.certificate
        invoiceDetails.currency = pricingInfo.currency
        invoiceDetails.uom = receivingData.unitsOfMeasure
        invoiceDetails.poNumber = receivingData.purchaseDocNum

        invoiceList.add(invoiceDetails)
        return invoiceList
    }

    private fun prepareAdvanceLineItems(): java.util.ArrayList<VegaNicaraguaAdvanceLineItems> {
        var advanceLineItems = java.util.ArrayList<VegaNicaraguaAdvanceLineItems>()

        advanceKnockOfList!!.forEach {
            var advanceItem = VegaNicaraguaAdvanceLineItems()
            advanceItem.advanceAmount = it.amount
            advanceItem.itemNum = it.itemNum
            advanceItem.advanceDate = it.documentDate
            advanceItem.advanceDocumentNumber = it.documentNumber
            advanceItem.currencyDevaluation = it.currencyDevaluationAmount
            advanceItem.interestCommission = it.commissionAmount
            advanceItem.interest = it.interestAmount
            advanceItem.legalExpense = it.legalExpenseAmount
            advanceItem.knockOffAmt = it.advanceKnockAmount
            advanceItem.grossValue = it.totalAdvanceKnockAmount
            advanceLineItems.add(advanceItem)
        }

        return advanceLineItems
    }

    private fun preparingGrnData(): ArrayList<VegaNicaraguaGrnData> {
        val grnDataList = ArrayList<VegaNicaraguaGrnData>()
        val grnData = VegaNicaraguaGrnData()
        grnData.batchNumber = receivingData.batchNumber
        grnData.currency = ""
        grnData.price = String.format(
            Locale.ENGLISH, "%.4f",
            covertToDouble(
                pricingInfo.grossValue
            ) / covertToDouble(receivingData.netWeight)
        )
        grnData.netWeight = receivingData.netWeight
        grnData.materialCode = receivingData.materialCode
        grnData.supplierCode = receivingData.supplierCode
        grnData.weighBridgeType = receivingData.weighBridgeType
        grnData.weighBridgeId = receivingData.weighBridgeId
        grnData.unitsOfMeasure = receivingData.unitsOfMeasure
        grnData.purchaseOrderNum = receivingData.purchaseDocNum
        grnData.storageLocationCode = receivingData.storageLocationCode
        grnData.plant = receivingData.plantId
        grnData.item = receivingData.item
        grnData.createdDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        grnData.docDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        grnData.currency = pricingInfo.currency!!
        grnData.postDate = getFormatedDate(receivingData.postDate.toString())
        grnDataList.add(grnData)

        return grnDataList
    }

    private fun preparingLotDetails(): ArrayList<VegaNicaraguaLotDetails> {
        val lotDetailsList = ArrayList<VegaNicaraguaLotDetails>()
        val lotDetails = VegaNicaraguaLotDetails()

        lotDetails.batchNumber = receivingData.batchNumber
        lotDetails.netWeight = receivingData.netWeight
        lotDetails.materialCode = receivingData.materialCode
        lotDetails.supplierCode = receivingData.supplierCode
        lotDetails.weighBridgeType = receivingData.weighBridgeType
        lotDetails.weighBridgeId = receivingData.weighBridgeId
        lotDetails.unitsOfMeasure = receivingData.unitsOfMeasure
        lotDetails.purchaseDocNum = receivingData.purchaseDocNum
        lotDetails.storageLocationCode = receivingData.storageLocationCode
        lotDetails.storageLocation = receivingData.storageLocationName
        lotDetails.grossWeight = receivingData.grossWeight
        lotDetails.bagType = receivingData.bagType
        lotDetails.plant = receivingData.plantId
        lotDetails.item = receivingData.item
        //adding quality details to lot
        val qualityDetailsList = ArrayList<VegaNicaraguaQualityDetails>()
        qualityParameterList.forEach {

            val qualityDetail = VegaNicaraguaQualityDetails()
            qualityDetail.descrChar = it!!.descrChar
            qualityDetail.nameChar = it.nameChar
            qualityDetail.qualityParameterValue = it.qualityParameterValue
            qualityDetailsList.add(qualityDetail)
        }

        lotDetails.qualityDetails = qualityDetailsList
        lotDetailsList.add(lotDetails)

        return lotDetailsList
    }

    private fun moveToSuccessPage(
        message: String,
        wbid: String?,
        batchNumber: String?,
        grnNumber: String?,
        poNumber: String?,
        invoiceNumber: String?
    ) {
        if (AppUtils.isOnline() && receivingData.batchNumber!!.isNotEmpty()) postUpdateLotSequence(receivingData.batchNumber!!)
       /* else {
            saveLotSequence(receivingData.batchNumber!!, transactionList, false)
            saveInvoiceSequence(transactionList, false, invoiceTransList)
            saveGrnSequence(transactionList, false)
        }*/
        //val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, message)
        if (AppUtils.isOnline()) {
            intent.putExtra(
                AppUtils.SUB_TITLE, getString(R.string.weighbridge_is).plus(wbid)
                    .plus(getString(R.string.batch_no_is)).plus(batchNumber)
                    .plus(getString(R.string.grn_no_is)).plus(grnNumber)
                    .plus(getString(R.string.po_no_is)).plus(poNumber)
                    .plus(getString(R.string.invoice_no_is)).plus(invoiceNumber)
            )
        } else {
            intent.putExtra(
                AppUtils.SUB_TITLE, getString(R.string.weighbridge_is).plus(wbid)
                    .plus(getString(R.string.batch_no_is)).plus(batchNumber)
                    .plus(getString(R.string.grn_no_is)).plus(grnNumber)
                    .plus(getString(R.string.invoice_no_is)).plus(invoiceNumber)
            )
        }

        receivingData.batchNumber = batchNumber
        receivingData.grnNumber = grnNumber
        receivingData.invoiceNumber = invoiceNumber

        intent.putExtra(AppUtils.PRINT_ENABLE, true)
        intent.putExtra(UIUtils.INVOICE_PRICE_INFO, pricingInfo)
        intent.putExtra(UIUtils.RECEIVING_DATA, receivingData)
        intent.putExtra(UIUtils.FROM_NICARAGUA_COFFEE, true)
        intent.putExtra(UIUtils.NICARAGUA_PRINT_TYPE, UIUtils.NICARAGUA_PRINT_GRN_RECEIPT)
        intent.putParcelableArrayListExtra(UIUtils.BAGS_DATA, weighDetails as ArrayList<out Parcelable>)
        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, prepareLotCard(receivingData.batchNumber))
       /* if(!AppUtils.isOnline()) {
            startActivity(intent)
            requireActivity().finish()
        }*/
    }

    private fun postUpdateLotSequence(batchNumber: String?) {
        val input = workDataOf(UIUtils.GRN_DATA to receivingData.grnType)
        val worker = getGrnLotSequnceOneTimeRequestWorker(input)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
//                            saveInvoiceSequence()
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        WorkInfo.State.FAILED -> {
//                            saveInvoiceSequence()
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                        else -> {}
                    }
                }

            })
    }

    private fun prepareLotCard(batchNumber: String?): ArrayList<VegaCoffeeSalesLots> {
        val lotList = arrayListOf<VegaCoffeeSalesLots>()
        val lot = VegaCoffeeSalesLots()
        lot.batchNumber = batchNumber.toString()
        lot.editedWeight = receivingData.netWeight
        lot.unitOfMeasure = receivingData.unitsOfMeasure
        lot.materialName = receivingData.materialName
        lot.materialCode = receivingData.materialCode ?: ""
        lot.grade = receivingData.gradeDesc
        lot.certificate = receivingData.certificate
        lot.storageLocationCode = receivingData.storageLocationCode
        lotList.add(lot)
        return lotList
    }
    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.DAYS_LIMIT.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> {
                            if(!it.value!!.isNullOrEmpty())
                                count=it.value!!.toInt()
                            else
                                count=0
                        }
                        it.applicable?.contains("N")!! -> {
                            count=0
                        }
                    }
                }
            }
        }
    }

}
