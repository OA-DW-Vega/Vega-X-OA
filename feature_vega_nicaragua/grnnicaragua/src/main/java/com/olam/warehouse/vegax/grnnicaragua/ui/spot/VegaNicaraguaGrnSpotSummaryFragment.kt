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
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.common.utils.saveGrnSequence
import com.olam.warehouse.master.common.utils.saveInvoiceSequence
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnPriceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.master.veganicaragua.model.*
import com.olam.warehouse.presentation.adapter.setUp
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
import com.olam.warehouse.vegax.grnnicaragua.ui.VegaNicaraguaGrnViewModel
import com.olam.warehouse.vegax.grnnicaragua.utils.covertToDouble
import com.olam.warehouse.vegax.grnnicaragua.utils.saveLotSequence
import com.olam.warehouse.vegax.grnnicaragua.work.getGrnLotSequnceOneTimeRequestWorker
import kotlinx.android.synthetic.main.item_vega_nicaragua_advance.view.*
import kotlinx.android.synthetic.main.item_vega_nicaragua_grn_summary.view.*
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
    private var weighDetails: List<VegaNicaraguaWeighmentBagMaterial>? = null
    private val vm: VegaNicaraguaGrnViewModel by viewModel()
    var advanceKnockOfList: ArrayList<AdvanceLineItemDetails>? = ArrayList<AdvanceLineItemDetails>()
    var count:Int=0

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

        vm.getConfigItems(UserRoles.DAYS_LIMIT.role)
        vm.configItems.observe(this, androidx.lifecycle.Observer {
            updateConfigItems(it)
        })
        binding.btnProceed.setOnClickListener(View.OnClickListener {
            showConfirmDialog()
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
                        //UIUtils.showErrorDialog(requireContext(), it.error.toString())
                        moveToFailurePage(
                            if (it.error.toString().isNotEmpty()) it.error.toString() else ""
                        )
                    }
                }
            }
        })
    }

    private fun moveToFailurePage(msg: String) {
        if (AppUtils.isOnline()) postUpdateLotSequence(receivingData.batchNumber.toString())
        else {
            saveLotSequence(receivingData.batchNumber!!)
            saveInvoiceSequence()
            saveGrnSequence()
        }
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.transaction_fail_msg))
        intent.putExtra(AppUtils.SUB_TITLE, msg.plus("\n").plus(getString(R.string.transaction_retry)))
        intent.putExtra(AppUtils.FAILURE, false)
        startActivity(intent)
    }

    private fun setVendorData() {
        binding.tvMaterial.text = receivingData.materialName
        binding.tvGrade.text = receivingData.grade
        binding.tvStorageLoctaion.text = receivingData.storageLocationName
        binding.tvNetWeight.text = pricingInfo.netWeight
        binding.vendorNameTitle.text = receivingData.supplierName
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

        binding.rvQuality.setUp(qualityParameterList, R.layout.item_vega_nicaragua_grn_summary, { it, pos ->

            description.text = it!!.descrChar
            value.text = it.qualityParameterValue!!.trim()
        })
        binding.rvQuality.isNestedScrollingEnabled = false
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
            binding.advanceLineItems.setUp(
                advanceKnockOfList!!.toMutableList(),
                R.layout.item_vega_nicaragua_advance,
                { it, pos ->
                    tvAdvanceNo.text = it.documentNumber
                    tvAmt.text = it.amount
                    tvKnockAmt.text = it.advanceKnockAmount
                    tvInterest.text = it.interestAmount
                    tvCommission.text = it.commissionAmount
                    tvLegalExp.text = it.legalExpenseAmount
                    tvCurrencyDevaluation.text = it.currencyDevaluationAmount
                    val totalAmt =
                        (if (it.advanceKnockAmount?.isNotEmpty() == true) it.advanceKnockAmount?.toDouble() else 0.0)?.plus(
                            if (it.interestAmount?.isNotEmpty() == true) it.interestAmount?.toDouble() ?: 0.0 else 0.0
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
                    tvTotalAmt.text = totalAmt.toString()
                    val date = DateUtils.getUTCDateTimeNicaragua(
                        DateUtils.getCurrentTimeInMills().toString(),
                        context
                    )
                    if (!date.isNullOrEmpty()) {
                        tvAdvanceDate.text = date
                    }
                    ivDown.setOnClickListener {
                        when (llAmtDetails.isVisible) {
                            true -> {
                                llAmtDetails.gone()
                                ivDown.setImageDrawable(ivDown.context.getDrawable(com.olam.warehouse.presentation.R.drawable.ic_arrow_down_black_24dp))
                            }
                            else -> {
                                llAmtDetails.visible()
                                ivDown.setImageDrawable(ivDown.context.getDrawable(com.olam.warehouse.presentation.R.drawable.ic_arrow_up))
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
            receivingData.invoiceNumber =
                if (receivingData.invoiceNumber?.isEmpty() == true) generateInvoiceRefNumber() else receivingData.invoiceNumber
        }
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
            weighDetails?.sumBy { if (it.bagCount.isNotEmpty()) it.bagCount.toInt() else 0 }.toString()
        receivingData.grossWeight =
            weighDetails?.sumByDouble { if (it.grossWeight.isNotEmpty()) it.grossWeight.toDouble() else 0.0 }.toString()
        receivingData.tareWeight = weighDetails?.sumByDouble {
            if (it.tareWeight?.isNotEmpty() == true) it.tareWeight?.toDouble() ?: 0.0 else 0.0
        }.toString()
        receivingData.netWeight = weighDetails?.sumByDouble {
            if (it.netWeight.isNotEmpty() == true) it.netWeight.toDouble() else 0.0
        }.toString()
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

        if (DateUtils.isFirstDayOfMonth(context!!,count)) {
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
        invoiceDetails.humidityPremium=pricingInfo.humidityPremium
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
        invoiceDetails.volumePremium = pricingInfo.volumePremium
        invoiceDetails.withHoldingTax = pricingInfo.withholdingTax
        invoiceDetails.bagCount =
            weighDetails?.sumBy { if (it.bagCount.isNotEmpty()) it.bagCount.toInt() else 0 }.toString()
        invoiceDetails.grossWeight =
            weighDetails?.sumByDouble { if (it.grossWeight.isNotEmpty()) it.grossWeight.toDouble() else 0.0 }.toString()
        invoiceDetails.tareWeight = weighDetails?.sumByDouble {
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
        else {
            saveLotSequence(receivingData.batchNumber!!)
            saveInvoiceSequence()
            saveGrnSequence()
        }
        val intent = Intent(requireContext(), SuccessActivity::class.java)
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
        startActivity(intent)
        requireActivity().finish()
    }

    private fun postUpdateLotSequence(batchNumber: String?) {
        val input = workDataOf(UIUtils.GRN_DATA to receivingData.grnType)
        val worker = getGrnLotSequnceOneTimeRequestWorker(input)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(this, androidx.lifecycle.Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
//                            saveInvoiceSequence()
                        }
                        WorkInfo.State.FAILED -> {
//                            saveInvoiceSequence()
                        }
                        WorkInfo.State.RUNNING -> {
                        }
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
