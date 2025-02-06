  package com.olam.warehouse.vegax.grnnicaragua.ui.spot

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.model.AdvanceLineItemDetails
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceLineItemGrn
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnPriceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaPriceConfigDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnnicaragua.R
import com.olam.warehouse.vegax.grnnicaragua.databinding.FragmentVegaNicaraguaGrnSummaryBinding
import com.olam.warehouse.vegax.grnnicaragua.databinding.ItemVegaNicaraguaAdvanceBinding
import com.olam.warehouse.vegax.grnnicaragua.databinding.ItemVegaNicaraguaQualityDetailsBinding
import com.olam.warehouse.vegax.grnnicaragua.ui.VegaNicaraguaGrnViewModel
import com.olam.warehouse.vegax.grnnicaragua.utils.covertToDouble
import com.olam.warehouse.vegax.grnnicaragua.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList

  class VegaNicaraguaGrnSummaryFragment : BaseFragment(),
      VegaNicaraguaGrnPriceCalculationAdvanceLineAdaptor.CallBack {

      private var callBack: CallBack? = null
      private var priceConfigDetails: VegaNicaraguaPriceConfigDetails? = null
      private lateinit var binding: FragmentVegaNicaraguaGrnSummaryBinding

      private val vm: VegaNicaraguaGrnViewModel by viewModel()

      private var netWeight: String? = ""
      private var totalPrice: Double? = 0.0
    private var gradeMappingDescription: String? = ""

    private var yieldPercentage: String? = "1"//it is in percentage
    private var exchangeRate: String? = ""//it is in percentage
    private var currency: String? = ""
    private var USDAmount: String? = "1"

    private var certificatePremium: Double? = 0.0
    private var volumePremium: Double? = 0.0
    private var humidityPremium: Double? = 0.0
    private var qualityDiscount: Double? = 0.0
    private var receivingData = VegaReceiving()
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    var netPayment: Double? = 0.0

    private var weighDetails: List<VegaNicaraguaWeighmentBagMaterial>? = null
    private var priceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()


    var selectedAdvanceKnockOfList: ArrayList<AdvanceLineItemDetails>? = ArrayList<AdvanceLineItemDetails>()

    private var advanceLineAdaptor: VegaNicaraguaGrnPriceCalculationAdvanceLineAdaptor? = null

    interface CallBack {
        fun replaceFragment(moveFrag: String)
    }

    companion object {
        fun newInstance(receivingData: VegaReceiving, qualityParameterList: ArrayList<VegaQualityParameter?>) =
            VegaNicaraguaGrnSummaryFragment().putArgs {
                putParcelable(UIUtils.RECEIVING_DATA, receivingData)
                putParcelableArrayList(UIUtils.QUALITY_DATA, qualityParameterList)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_grn_summary

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaGrnSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("invoicenicaragua/ui/VegaNicaraguaGrnSummaryFragment")
            .title("Invoice  GRN list")
            .with(tracker)

        initExtra()
        initUI()
    }

    private fun initUI() {
        if (receivingData.yieldPercentage!!.isNotEmpty()) yieldPercentage = receivingData.yieldPercentage
        vm.getBagItems(receivingData.tmpWbId)
        vm.bagItems.observe(viewLifecycleOwner, Observer {
            weighDetails = it
        })

        /* if (receivingData.materialName?.contains("Tolling", true) == false)
             vm.getGradeMapping(receivingData.grade!!.substring(receivingData.grade!!.length - 4))
         vm.gradeMapping.observe(viewLifecycleOwner, Observer {
             gradeMappingDescription = it?.description ?: ""
             if (gradeMappingDescription!!.isNotEmpty()) fetchingGrnPriceDetails()
             else fetchingExchangeRate()
         })*/

        getAdvanceLineItems()
    }

    private fun getAdvanceLineItems() {

        vm.advanceItems.observe(viewLifecycleOwner, androidx.lifecycle.Observer { setAdvanceLinesAdapter(it) })
        vm.getAdvanceItem(receivingData.tmpWbId)
    }

    private fun initExtra() {
        arguments?.let {
            receivingData = it.getParcelable(UIUtils.RECEIVING_DATA)!!
            qualityParameterList = it.getParcelableArrayList<VegaQualityParameter>(UIUtils.QUALITY_DATA)!!
            netWeight = receivingData.netWeight
        }
        setUpAdapter(qualityParameterList)
        if (receivingData.grnType?.contains("ptbf") == true) {
            binding.llPricing.gone()
            binding.llPtbfPricing.visible()
        }

        if (receivingData.materialName?.contains(
                "Tolling",
                true
            ) == true
        ) {
            binding.llPricing.gone()
            binding.llNetWeight.visible()
        } /*else {
            binding.llPricing.visible()
            binding.llNetWeight.gone()
        }*/
        if (receivingData.purchaseDocNum?.isNotEmpty() == true) {
            binding.llPoNo.visible()
            binding.tvPoNoValue.text = receivingData.purchaseDocNum
        }
        if (receivingData.weighBridgeId.isNotEmpty()) {
            binding.llWeighBridge.visible()
            binding.tvWbidValue.text = receivingData.weighBridgeId
        }
        if (receivingData.batchNumber?.isNotEmpty() == true) {
            binding.llBatch.visible()
            binding.tvBatchValue.text = receivingData.batchNumber
        }
        if (receivingData.grnNumber?.isNotEmpty() == true) {
            binding.llGrnNo.visible()
            binding.tvGrnNoValue.text = receivingData.grnNumber
        }
        if (receivingData.grnType.equals(getString(R.string.fixed))) {
            binding.tvPoNumber.text = receivingData.purchaseDocNum
            binding.tvPoHeader.visible()
            binding.tvPoNumber.visible()
        }
        binding.title.text = receivingData.grnType
        binding.tvVendorValue.text = receivingData.supplierName
        binding.tvMaterialValue.text = receivingData.materialName
        binding.tvGradeValue.text = receivingData.grade + ":" + receivingData.gradeDesc
        binding.tvNetweightValue.text = receivingData.netWeight.plus(" KG(S)")
        binding.tvExchangeRate.text = receivingData.exchangeRate
        binding.tvExchangeRatePtbf.text = receivingData.exchangeRate
        binding.tvBasePricePtbf.text = receivingData.materialPrice.toString()
        binding.tvNetWeightPtbf.text = receivingData.netWeight
        binding.tvTotalPricePtbf.text = receivingData.netPayment
        setPriceDetailsEditCase()
        /*   receivingData.createdDate = getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
           receivingData.erdat = getCurrentTimeInMills().toString()
           receivingData.docDate = getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")*/
    }

    private fun setPriceDetailsEditCase() {

        binding.tvNetWeight.text = netWeight!! + " " + "KG(S)"
       /* if (receivingData.grnType.equals(getString(R.string.fixed))) {
            binding.tvBasePrice.text = getString(R.string.c_doller) + " " + receivingData.price
        } else {*/
            binding.tvBasePrice.text = getString(R.string.c_doller) + " " + receivingData.basePrice
        //}
        binding.tvTotalPrice.text = formatStrings(receivingData.totalPrice ?: "")
        binding.tvCertificatePremium.text = formatStrings(receivingData.certificatePremium ?: "")
        binding.tvVolumePremium.text = formatStrings(receivingData.volumePremium ?: "")
        binding.tvHumidityPremium.text = formatStrings(receivingData.humidityPremium ?: "")
        binding.tvQualityDiscounting.text = formatStrings(receivingData.qualityDiscounting ?: "")
        binding.tvHumidityDiscounting.text = formatStrings(receivingData.humidityDiscounting ?: "")
        binding.TvGrossValue.text = formatStrings(receivingData.grossValue ?: "")
        binding.exportIncentive.text = formatStrings(receivingData.exportnCentives ?: "")
        binding.tvWitrHoldingTax.text = formatStrings(receivingData.withholdingTax ?: "")
        binding.tvNationalStockExchange.text = formatStrings(receivingData.NSExchangeRate ?: "")
        binding.tvBankCommission.text = formatStrings(receivingData.bankCommission ?: "")
        binding.tvFTDC.text = formatStrings(receivingData.FTDC ?: "")
        binding.subTotalDeduction.text = formatStrings(receivingData.totalDduction ?: "")
        binding.tvNetPayment.text = formatStrings(receivingData.netPayment ?: "")
        binding.TvFinalPayment.text = formatStrings(receivingData.finalPayment ?: "")
    }

    private fun formatStrings(str: Any): String {
        return getString(R.string.c_doller) + " " + str.toString().format(this).replace(",", "")
    }

    private fun setAdvanceLinesAdapter(data: List<VegaNicaraguaAdvanceLineItemGrn>) {
        if (!data.equals("null") && data.size > 0) {
            binding.advanceLineItemHeader.visible()
            binding.advanceLineItems.visible()
            var advance = 0.0
            data.forEach {
                advance = covertToDouble(it.totalAdvanceKnockAmount!!) + advance
            }
            //binding.TvFinalPayment.text = formatString((netPayment!! - advance))

        } else {
            binding.advanceLineItems.gone()
            binding.advanceLineItemHeader.gone()
        }
        binding.advanceLineItems.setUpAdapter(
            data.toMutableList(),
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
                if (it.date?.isNotEmpty() == true) {
                    bindItem.tvAdvanceDate.text = it.date.let { it1 ->
                        it1?.let { it2 ->
                            DateUtils.getUTCDateTimeNicaragua(
                                it2,
                                App.getAppContext()
                            )
                        }
                    }
                }
                bindItem.ivDown.setOnClickListener {
                    when (bindItem.llAmtDetails.isVisible) {
                        true -> {
                            bindItem.llAmtDetails.gone()
                            bindItem.ivDown.setImageDrawable(bindItem.ivDown.context.getDrawable(com.olam.warehouse.presentation.R.drawable.ic_arrow_down_black_24dp))
                        }
                        else -> {
                            bindItem.llAmtDetails.visible()
                            bindItem.ivDown.setImageDrawable(bindItem.ivDown.context.getDrawable(com.olam.warehouse.presentation.R.drawable.ic_arrow_up))
                        }
                    }
                }
            })
    }

    override fun updatePriceDetails(selectedList: ArrayList<AdvanceLineItemDetails>) {

        selectedAdvanceKnockOfList = selectedList

        if (selectedAdvanceKnockOfList!!.size > 0) {
            var advance = 0.0
            selectedAdvanceKnockOfList!!.forEach {
                advance = covertToDouble(it.totalAdvanceKnockAmount!!) + advance
            }
            if (netPayment!! >= advance) {
                // binding.TvFinalPayment.text = formatString((netPayment!! - advance))
            } else {
                //showSnack(getString(R.string.advance_is_more_than_net_payment_info))
            }
        }
    }

    override fun removeItem(model: AdvanceLineItemDetails) {

    }

    private fun setUpAdapter(qualityParameters: ArrayList<VegaQualityParameter?>) {
        binding.rvQuality.setUpAdapter(
            qualityParameters,
            R.layout.item_vega_nicaragua_quality_details,
            ItemVegaNicaraguaQualityDetailsBinding::inflate,
            { it, pos, bindItem ->
                if (pos % 2 == 0) {
                    this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                } else {
                    this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lits))
                }
                bindItem.tvQualityNameApprove.text = it?.descrChar
                bindItem.tvUnitApprove.text = it?.qualityParameterValue
            })
    }

    private fun formatString(str: Any): String {
        val strFormat = String.format(Locale.ENGLISH, "%.2f", str)
        return getString(R.string.c_doller) + " " + strFormat.format(this).replace(",", "")
    }
}

