package com.olam.warehouse.vegax.invoicenicaragua.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.gson.Gson
import com.olam.warehouse.master.common.model.AdvanceLineItemDetails
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceLineItemGrn
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaInvoiceDetails
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoiceGrnInventoryModal
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnecuador.utils.covertToDouble
import com.olam.warehouse.vegax.invoicenicaragua.R
import com.olam.warehouse.vegax.invoicenicaragua.databinding.FragmentVegaNicaraguaInvoiceSummaryBinding
import kotlinx.android.synthetic.main.fragment_vega_nicaragua_invoice_summary.*
import kotlinx.android.synthetic.main.item_vega_nicaragua_invoice_advance.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList

class VegaNicaraguaInvoiceSummaryFragment : BaseFragment(),
    VegaNicaraguaInvoicePriceCalculationAdvanceLineAdaptor.CallBack {

    private var callBack: CallBack? = null
    private lateinit var binding: FragmentVegaNicaraguaInvoiceSummaryBinding

    private var vendorData: VegaVendor = VegaVendor()
    private var grnData = VegaNicaraguaInvoiceDetails()
    private var inventoryInfo = VegaNicaraguaInvoiceGrnInventoryModal()

    private val vm: VegaNicaraguaInvoiceViewModel by viewModel()

    private var netWeight: String? = "0"


    var selectedAdvanceKnockOfList: ArrayList<AdvanceLineItemDetails>? = ArrayList<AdvanceLineItemDetails>()

    interface CallBack {
        fun replaceFragment(moveFrag: String)
    }

    companion object {
        fun newInstance(grnDetails: VegaNicaraguaInvoiceDetails) =
            VegaNicaraguaInvoiceSummaryFragment().putArgs {
                putParcelable("DATA", grnDetails)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_invoice_summary

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaInvoiceSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("invoicenicaragua/ui/VegaNicaraguaInvoiceGrnFragment")
            .title("Invoice  GRN list")
            .with(tracker)
        initExtra()
        initUI()
    }

    private fun initExtra() {
        grnData = arguments?.getParcelable<VegaNicaraguaInvoiceDetails>("DATA") ?: VegaNicaraguaInvoiceDetails()
        val vegaVendor = VegaVendor()
        vegaVendor.vendorCode = grnData.supplierCode!!
        vendorData = vegaVendor
        netWeight = grnData.grnQty
        binding.basePriceEt.text = grnData.basePrice
        binding.tvGrnValue.text = grnData.grn
        binding.tvQtyGradeValue.text = grnData.qualityGrade
        binding.tvcertificateValue.text = "NA"
        binding.tvMaterialValue.text = grnData.materialName
        binding.tvVendorValue.text = grnData.supplierName
        binding.tvGrnWeightValue.text = grnData.grnQty?.trim().plus("KG(S)")
        binding.tvGrnPriceValue.text = getString(R.string.c_doller).plus(" ").plus(grnData.totalPrice!!.trim())
        //setPriceDetails(grnData.basePrice.toString())
        /*arguments?.let {
            vendorData = it.getParcelable(UIUtils.VENDOR_DATA)!!
            grnData = it.getParcelable(UIUtils.GRN_DATA)!!
            netWeight = grnData.grnQty
        }*/
    }


    private fun initUI() {
        binding.tvQtyGradeValue.text = grnData.qualityGrade + ":" + grnData.qualityGradeDesc!!
        binding.tvExchangeRate.text = grnData.exchangeRate
        binding.tvcertificateValue.text=grnData.certification!!
        getAdvanceLineItems()
    }

    private fun getAdvanceLineItems() {
        vm.advanceItems.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            setPriceDetails()
            setAdvanceLinesAdapter(it)
        })
        vm.getAdvanceItem(grnData.tempId.toString())

    }


    private fun setPriceDetails() {


        basePriceEt.text=formatString(grnData.basePrice!!)

                    try {
                        binding.tvNetWeight.text = netWeight!! + " " + "KG(S)"


                        binding.tvTotalPrice.text = formatString(grnData.totalPrice!!)

                        binding.tvCertificatePremium.text = formatString(grnData.certificatePremium!!)
                        binding.tvVolumePremium.text = formatString(grnData.volumePremium!!)
                        binding.tvHumidityPremium.text = formatString(grnData.humidityPremium!!)
                        binding.tvQualityDiscounting.text = formatString(grnData.qualityDiscounting!!)

                        //g) Moisture Discounting - (Total Price + Certification Premium + Volume Premium + Moisture Premium) * Discount Percentage.
                        //h) Gross Value - Total Price + Certification Premium + Volume Premium + Moisture Premium - Quality Discounting - Moisture Discounting.
                        binding.tvHumidityDiscounting.text = formatString(grnData.humidityDiscounting!!)

                        binding.TvGrossValue.text = formatString(grnData.grossValue!!)

                        binding.exportIncentive.text = formatString(grnData.exportnCentives!!)

                        binding.tvWitrHoldingTax.text = formatString(grnData.withholdingTax!!)

                        binding.tvNationalStockExchange.text = formatString(grnData.NSExchangeRate!!)

                        binding.tvBankCommission.text = formatString(grnData.bankCommission!!)

                        binding.tvFTDC.text = formatString(grnData.totalFTDC!!)

                        binding.subTotalDeduction.text = formatString(grnData.totalDduction!!)


                        binding.tvNetPayment.text = formatString(grnData.netPayment!!)

                        binding.TvFinalPayment.text = formatString(grnData.finalPayment!!)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
    }


    private fun setAdvanceLinesAdapter(data: List<VegaNicaraguaAdvanceLineItemGrn>) {

        if (data != null && data.size > 0) {
            binding.advanceLineItemHeader.visibility = View.VISIBLE
            binding.advanceLineItems.visibility = View.VISIBLE
            var advance = 0.0
            data.forEach {
                advance = covertToDouble(it.totalAdvanceKnockAmount!!) + advance
            }
            binding.TvFinalPayment.text = formatString(( String.format(Locale.ENGLISH, "%.2f",covertToDouble(grnData.netPayment!!) - advance)))
        } else {
            binding.advanceLineItems.visibility = View.GONE
            binding.advanceLineItemHeader.visibility = View.GONE
        }

        binding.advanceLineItems.setUp(data.toMutableList(), R.layout.item_vega_nicaragua_invoice_advance, { it, pos ->
            tvAdvanceNo.text = it.documentNumber
            tvAmt.text = it.amount
            tvKnockAmt.text = it.advanceKnockAmount
            tvInterest.text = it.interestAmount
            tvCommission.text = it.commissionAmount
            tvLegalExp.text = it.legalExpenseAmount
            tvTotalAmt.text = it.amount
            if (it.date?.isNotEmpty() == true) {
                tvAdvanceDate.text = it.date.let { it1 ->
                    it1?.let { it2 ->
                        DateUtils.getDate(it2.toLong(), "dd/MM/yyyy")
                        /*DateUtils.getUTCDateTime(
                            it2,
                            App.getAppContext()
                        )*/
                    }
                }
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
    }



    private fun formatString(str: String): String {

        if(str.isNullOrEmpty())
        {
            return getString(R.string.c_doller) + " " +"0.00"
        }
        return getString(R.string.c_doller) + " " + str.format(this).replace(",", "")
    }

    fun getPlantDetails(): Plant {
        val plantDetailJson = PreferenceHelper.get(Constants.PLANT_DETAILS, "")
        return Gson().fromJson<Plant>(plantDetailJson)
    }
    override fun updatePriceDetails(selectedList: ArrayList<AdvanceLineItemDetails>) {

        selectedAdvanceKnockOfList = selectedList

        if (selectedAdvanceKnockOfList!!.size > 0) {
            var advance = 0.0
            selectedAdvanceKnockOfList!!.forEach {
                advance = covertToDouble(it.totalAdvanceKnockAmount!!) + advance
            }
            if (covertToDouble(grnData.netPayment!!) >= advance) {
                binding.TvFinalPayment.text = formatString(
                    String.format(
                        Locale.ENGLISH,
                        "%.2f",
                        (covertToDouble(grnData.netPayment!!) - advance)
                    )
                )
            } else {
                showSnack(getString(R.string.advance_is_more_than_net_payment_info))
            }
        }
    }

    override fun removeItem(model: AdvanceLineItemDetails) {

    }

}
