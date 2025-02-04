package com.olam.warehouse.vegax.grnnicaragua.ui.spot

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import com.olam.warehouse.master.common.model.AdvanceLineItemDetails
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.common.utils.prepareGrnPriceDetailsList
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoicePriceInfo
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.DateUtils.getDate
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnnicaragua.R
import com.olam.warehouse.vegax.grnnicaragua.databinding.FragmentVegaNicaraguaGrnPriceCalculationBinding
import com.olam.warehouse.vegax.grnnicaragua.ui.VegaNicaraguaGrnViewModel
import com.olam.warehouse.vegax.grnnicaragua.utils.FRAG_SPOT_GRN_SUMMARY
import com.olam.warehouse.vegax.grnnicaragua.utils.covertToDouble
import com.olam.warehouse.vegax.grnnicaragua.utils.prepareAdvanceLineItem
import com.olam.warehouse.vegax.grnnicaragua.utils.saveLotSequence
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList

class VegaNicaraguaGrnPriceCalculationFragment : BaseFragment(),
    VegaNicaraguaGrnPriceCalculationAdvanceLineAdaptor.CallBack {

    private var callBack: CallBack? = null
    private var priceConfigDetails: VegaNicaraguaPriceConfigDetails? = null
    private lateinit var binding: FragmentVegaNicaraguaGrnPriceCalculationBinding

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
    private var advanceValues = listOf<VegaNicaraguaAdvanceLineItemGrn>()
    var netPayment: Double? = 0.0

    private var weighDetails: List<VegaNicaraguaWeighmentBagMaterial>? = null
    private var priceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()


    var selectedAdvanceKnockOfList: ArrayList<AdvanceLineItemDetails>? = ArrayList<AdvanceLineItemDetails>()

    private var advanceLineAdaptor: VegaNicaraguaGrnPriceCalculationAdvanceLineAdaptor? = null

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            data: Bundle
        )
    }

    companion object {
        fun newInstance(receivingData: VegaReceiving, qualityParameterList: ArrayList<VegaQualityParameter?>) =
            VegaNicaraguaGrnPriceCalculationFragment().putArgs {
                putParcelable(UIUtils.RECEIVING_DATA, receivingData)
                putParcelableArrayList(UIUtils.QUALITY_DATA, qualityParameterList)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_grn_price_calculation

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaGrnPriceCalculationBinding.inflate(layoutInflater)
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

    private fun initUI() {
        if (receivingData.grnType.equals(getString(R.string.fixed))) {
            binding.title.text = getString(R.string.grn_fixed_pricing)
            binding.tvPoNumber.text = receivingData.purchaseDocNum
            binding.tvPoHeader.visible()
            binding.tvPoNumber.visible()
        }
        binding.tvExchangeRate.text = receivingData.exchangeRate
        if (receivingData.yieldPercentage!!.isNotEmpty()) yieldPercentage = receivingData.yieldPercentage
        vm.getBagItems(receivingData.tmpWbId)
        vm.bagItems.observe(viewLifecycleOwner, Observer {
            weighDetails = it
        })

        vm.advanceItems.observeOnce(viewLifecycleOwner, Observer {
            advanceValues = it
            if (receivingData.finalPayment?.isNotEmpty() == true) {
                /* vm.advanceLineItemsOfflineDetails.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                     var data = prepareAdvanceLineItemDetailsList(it)
                     if (data != null && data.size > 0) {
                         setAdvanceLinesAdapter(data)
                     }
                 })*/
                if (AppUtils.isOnline()) vm.getAdvanceDetailsByVendor(receivingData.supplierCode!!) else vm.getAdvanceDetailsByVendorOffline(
                    "000".plus(receivingData.supplierCode!!)
                )
                /* vm.grnPriceDetailsOffline.observe(viewLifecycleOwner, Observer {
                     priceDetails = it as ArrayList<VegaNicaraguaGrnPriceDetails>
                     priceDetails = getFilteredPriceDetails(priceDetails)
                 })*/
                vm.getGrnPriceDetailsOffline()
            }
        })
        vm.getAdvanceItem(receivingData.tmpWbId)

        vm.advanceLineItemsOfflineDetails.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            var data = prepareAdvanceLineItemDetailsList(it.reversed())
            if (data != null && data.size > 0) {

                setAdvanceLinesAdapter(data)
            }
            if (receivingData.finalPayment?.isEmpty() == true) getPricingInfo()
        })

        vm.grnPriceDetailsOffline.observe(viewLifecycleOwner, Observer {
            priceDetails = it as ArrayList<VegaNicaraguaGrnPriceDetails>
            priceDetails = getFilteredPriceDetails(priceDetails)
            if (receivingData.finalPayment?.isEmpty() == true) fetchingExchangeRate()
        })

        //vm.getGradeMapping(receivingData.grade!!.split(" ")[1])
        if (receivingData.finalPayment?.isEmpty() == true) vm.getGradeMapping(
            receivingData.grade!!.substring(
                receivingData.grade!!.length - 4
            )
        )
        else setPriceDetailsEditCase()
        vm.gradeMapping.observe(viewLifecycleOwner, Observer {
            gradeMappingDescription = it?.description ?: ""
            if (gradeMappingDescription!!.isNotEmpty()) fetchingGrnPriceDetails()
            else fetchingExchangeRate()
        })

        vm.updateLotSequence.observe(viewLifecycleOwner, Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.data.let {
                            saveLotSequence(it?.sequence.toString())
                        }
                    }
                    Resource.Status.LOADING -> {
                    }
                    Resource.Status.ERROR -> {
                        UIUtils.showErrorDialog(requireContext(), it.error.toString())
                    }
                }
            }
        })

        binding.btnProceed.setOnClickListener {
            var isValid: Boolean? = true
            var advance = 0.0
            var genericErrorMsg = ""
            if (advanceLineAdaptor != null && selectedAdvanceKnockOfList!!.size > 0) {

                selectedAdvanceKnockOfList!!.forEach {
                    advance = covertToDouble(it.totalAdvanceKnockAmount!!) + advance
                    if (it.advanceKnockAmount.isNullOrEmpty()) {
                        isValid = false
                        genericErrorMsg = getString(R.string.valid_mandatory_error_info)
                    }
                    if (it.interestAmount.isNullOrEmpty()) {
                        isValid = false
                        genericErrorMsg = getString(R.string.valid_mandatory_error_info)
                    }
                    if (it.commissionAmount.isNullOrEmpty()) {
                        isValid = false
                        genericErrorMsg = getString(R.string.valid_mandatory_error_info)
                    }
                }
                if (netPayment?.formatTwoDigits()?.toDouble() ?: 0.0 < advance.formatTwoDigits().toDouble()) {
                    isValid = false
                    if (genericErrorMsg.isEmpty()) genericErrorMsg = getString(R.string.valid_advance_error_info)
                }
            }

            if (isValid!!) {
                if (priceConfigDetails != null || receivingData.finalPayment?.isNotEmpty() == true) moveToSummary()
                else UIUtils.showErrorDialog(requireContext(), getString(R.string.price_info_not_found))
            } else {
                showSnack(genericErrorMsg)
            }
        }

        vm.advanceLineItemsDetails.observe(viewLifecycleOwner, androidx.lifecycle.Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        when (it.data?.success) {
                            true -> {
                                if (!it.data?.data!!.isNullOrEmpty()) {
                                    val dataValue = it.data?.data!![0].advanceLineItemDetails!!
                                    setAdvanceLinesAdapter(dataValue)
                                }
                            }
                            else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                        }
                        hideLoading()
                        getPricingInfo()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        // requireContext().toast(it.error.toString())
                        getPricingInfo()
                    }
                }
            }
        })

    }

    private fun fetchingExchangeRate() {
        vm.exchangeRate.observe(viewLifecycleOwner, Observer { response ->
            response?.let {

                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.data.let {
                            exchangeRate = it?.exchangeRate
                            currency = it?.currencyCode
                            //USDAmount = it?.currencyValue
                            binding.tvExchangeRate.text = exchangeRate
                            getAdvanceLineItems()
                        }
                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        requireContext().toast(it.error.toString())
                    }
                }
            }
        })

        vm.exchangeRateOffline.observe(viewLifecycleOwner, Observer {
            exchangeRate = it?.exchangeRate
            currency = it?.currencyCode
            //USDAmount = it?.currencyValue
            binding.tvExchangeRate.text = exchangeRate
            getAdvanceLineItems()
        })

        if (isOnline()) vm.getExchangeRate() else vm.getExchangeRateOffline()
    }

    private fun getAdvanceLineItems() {
        if (!receivingData.supplierCode.isNullOrEmpty()) {

            /* vm.advanceLineItemsOfflineDetails.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                 var data = prepareAdvanceLineItemDetailsList(it)
                 if (data != null && data.size > 0) {

                     setAdvanceLinesAdapter(data)
                 }
                 if(receivingData.finalPayment?.isEmpty()==true) getPricingInfo()
             })*/

            if (AppUtils.isOnline()) vm.getAdvanceDetailsByVendor(receivingData.supplierCode!!) else vm.getAdvanceDetailsByVendorOffline(
                "000".plus(receivingData.supplierCode!!)
            )


        } else {
            showSnack(requireContext().resources.getString(R.string.vendor_info_not_found))
        }
    }

    private fun fetchingGrnPriceDetails() {
        vm.grnPriceDetails.observe(viewLifecycleOwner, Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        when (it.data?.success) {
                            true -> {
                                priceDetails.clear()
                                val dataValue = it.data?.data!!
                                priceDetails = prepareGrnPriceDetailsList(dataValue)
                                priceDetails = getFilteredPriceDetails(priceDetails)
                                if (priceDetails.size > 0)
                                    fetchingExchangeRate()
                            }
                            else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                        }
                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        requireContext().toast(it.error.toString())
                    }
                }
            }
        })

        /*vm.grnPriceDetailsOffline.observe(viewLifecycleOwner, Observer {
            priceDetails = it as ArrayList<VegaNicaraguaGrnPriceDetails>
            priceDetails = getFilteredPriceDetails(priceDetails)
            if(receivingData.finalPayment?.isEmpty()==true)fetchingExchangeRate()
        })*/
        //    gradeMappingDescription.toString()
        if (isOnline()) vm.getGrnPriceDetails()
        else vm.getGrnPriceDetailsOffline()
    }

    private fun getFilteredPriceDetails(priceDetails: ArrayList<VegaNicaraguaGrnPriceDetails>): ArrayList<VegaNicaraguaGrnPriceDetails> {
        var filteredPriceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()

        priceDetails.forEach {
            if (it.fieldName.equals("LOWGRD01", true)) {
                it.percentage = receivingData.DESMA
                filteredPriceDetails.add(it)
            } else if ("LOWGRD02".equals(it.fieldName, true)) {
                it.percentage = receivingData.DESMC
                filteredPriceDetails.add(it)
            } else if ("LOWGRD03".equals(it.fieldName, true)) {
                it.percentage = receivingData.DESMD
                filteredPriceDetails.add(it)
            } else if (gradeMappingDescription?.toString().equals(it.description)) {
                it.percentage = receivingData.exportablePercentage
                filteredPriceDetails.add(it)
            }
        }
        return filteredPriceDetails
    }

    private fun getPricingInfo() {
        vm.priceConfigInfo.observeOnce(this, androidx.lifecycle.Observer {
            priceConfigDetails = it
            setPriceDetails()
        })
        vm.getPriceConfigInfo(
            receivingData.materialCode.toString(),
            receivingData.grade!!.substring(receivingData.grade!!.length - 4)
        )
    }

    private fun initExtra() {
        arguments?.let {
            receivingData = it.getParcelable(UIUtils.RECEIVING_DATA)!!
            qualityParameterList = it.getParcelableArrayList<VegaQualityParameter>(UIUtils.QUALITY_DATA)!!
            netWeight = receivingData.netWeight
            if (receivingData.netPayment?.isNotEmpty() == true) netPayment = receivingData.netPayment?.toDouble()
        }

        if (receivingData.imageString?.isNotEmpty() == true) {
            binding.rlStateProgressBar.gone()
            binding.btnProceed.gone()
        } else {
            binding.rlStateProgressBar.visible()
            binding.btnProceed.visible()
        }
        receivingData.createdDate = getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receivingData.erdat = getCurrentTimeInMills().toString()
        receivingData.docDate = getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
    }

    private fun setPriceDetails() {

        if (priceConfigDetails != null) {
            try {
                binding.tvNetWeight.text = netWeight!! + " " + "KG(S)"
                if (receivingData.grnType.equals(getString(R.string.fixed))) {
                    binding.tvBasePrice.text = getString(R.string.c_doller) + " " + receivingData.price
                    totalPrice = covertToDouble(receivingData.price!!) * netWeight!!.toDouble()
                } else {
                    binding.tvBasePrice.text = getString(R.string.c_doller) + " " + priceConfigDetails!!.dailyPrice
                    totalPrice = covertToDouble(priceConfigDetails?.dailyPrice) * netWeight!!.toDouble()
                }

                receivingData.plantName = getPlantDetails().plantName

                binding.tvTotalPrice.text = formatString(totalPrice!!)

                certificatePremium =
                    covertToDouble(priceConfigDetails?.certificatePremium) * netWeight!!.toDouble()
                var volumeTo = covertToDouble(priceConfigDetails?.volumePremiumTo)
                var volumeFrom = covertToDouble(priceConfigDetails?.volumePremiumFrom)

                if (volumeFrom > 0 && volumeTo > 0) {
                    if (netWeight!!.toDouble() >= volumeFrom && netWeight!!.toDouble() <= volumeTo) {
                        volumePremium =
                            covertToDouble(priceConfigDetails?.volumePremium) * netWeight!!.toDouble()
                    }
                } else if (volumeFrom > 0) {
                    if (netWeight!!.toDouble() >= volumeFrom) {
                        volumePremium =
                            covertToDouble(priceConfigDetails?.volumePremium) * netWeight!!.toDouble()
                    }
                } else if (volumeTo > 0) {
                    if (netWeight!!.toDouble() <= volumeTo) {
                        volumePremium =
                            covertToDouble(priceConfigDetails?.volumePremium) * netWeight!!.toDouble()
                    }
                } else {
                    volumePremium = 0.00
                }
                humidityPremium =
                    covertToDouble(priceConfigDetails?.moisturePremium) * netWeight!!.toDouble()
                qualityDiscount =
                    covertToDouble(priceConfigDetails?.qualityDiscount) * netWeight!!.toDouble()

                binding.tvCertificatePremium.text = formatString(certificatePremium!!)
                binding.tvVolumePremium.text = formatString(volumePremium!!)
                binding.tvHumidityPremium.text = formatString(humidityPremium!!)
                binding.tvQualityDiscounting.text = formatString(qualityDiscount!!)

                //g) Moisture Discounting - (Total Price + Certification Premium + Volume Premium + Moisture Premium) * Discount Percentage.
                //h) Gross Value - Total Price + Certification Premium + Volume Premium + Moisture Premium - Quality Discounting - Moisture Discounting.
                var humidityDiscount: Double =
                    formatDouble(
                        (totalPrice!! + certificatePremium!! + volumePremium!! + humidityPremium!!) * (covertToDouble(
                            priceConfigDetails?.moistureDiscount
                        ) / 100)
                    )
                binding.tvHumidityDiscounting.text = formatString(humidityDiscount)

                val grossValue: Double =
                    formatDouble((totalPrice!! + certificatePremium!! + volumePremium!! + humidityPremium!!) - (qualityDiscount!! + humidityDiscount))
                binding.TvGrossValue.text = formatString(grossValue)

                val exportIncentive: Double = (grossValue) * (covertToDouble(priceConfigDetails?.exportIncentive) / 100)
                binding.exportIncentive.text = formatString(exportIncentive)

                val withHoldingTax: Double = (grossValue) * (covertToDouble(priceConfigDetails?.withHoldingTax) / 100)
                binding.tvWitrHoldingTax.text = formatString(withHoldingTax)

                val NSETax: Double = (grossValue) * (covertToDouble(priceConfigDetails?.neCommission) / 100)
                binding.tvNationalStockExchange.text = formatString(NSETax)

                val commissionTax: Double = (grossValue) * (covertToDouble(priceConfigDetails?.bankCommsion) / 100)
                binding.tvBankCommission.text = formatString(commissionTax)

                var ftdc: Double =
                    ((covertToDouble(priceConfigDetails?.ftdc!!)) * (covertToDouble(exchangeRate!!)) * (covertToDouble(
                        yieldPercentage!!
                    ) / 100) * netWeight!!.toDouble()) / 46
                binding.tvFTDC.text = formatString(ftdc)
                receivingData.FTDC = removeDollerInValue(binding.tvFTDC.text.toString())

                var totalDeductions =
                    formatDouble(withHoldingTax) + formatDouble(commissionTax) + formatDouble(NSETax) + formatDouble(
                        ftdc
                    )
                binding.subTotalDeduction.text = formatString(totalDeductions)
                val expIn = formatDouble(exportIncentive)
                val totalDed = formatDouble(totalDeductions)
                netPayment = grossValue + expIn - totalDed
                binding.tvNetPayment.text = formatString(netPayment!!)

                if (selectedAdvanceKnockOfList!!.size > 0) {
                    var advance = 0.0
                    selectedAdvanceKnockOfList!!.forEach {
                        advance = covertToDouble(it.totalAdvanceKnockAmount!!) + advance
                    }
                    if ((netPayment?.formatTwoDigits()?.toDouble() ?: 0.0) >= advance) {
                        binding.TvFinalPayment.text =
                            formatString((netPayment?.formatTwoDigits()?.toDouble() ?: 0.0) - advance)
                    } else {
                        showSnack("Please check advance is more than Net payment")
                    }
                } else {
                    binding.TvFinalPayment.text = formatString(netPayment!!)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            showSnack(getString(R.string.price_info_not_found))
        }
    }

    private fun setPriceDetailsEditCase() {
        vm.priceConfigInfo.observeOnce(this, androidx.lifecycle.Observer {
            priceConfigDetails = it
            exchangeRate = receivingData.exchangeRate
            setPriceDetails()
            /*var ftdc: Double =
                ((covertToDouble(it?.ftdc!!)) * (covertToDouble(receivingData.exchangeRate!!)) * (covertToDouble(yieldPercentage!!) / 100) * netWeight!!.toDouble()) / 46
            binding.tvNetWeight.text = netWeight!! + " " + "KG(S)"
            if (receivingData.grnType.equals(getString(R.string.fixed))) {
                binding.tvBasePrice.text = getString(R.string.c_doller) + " " + receivingData.price
            } else {
                binding.tvBasePrice.text = getString(R.string.c_doller) + " " + receivingData.basePrice
            }
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
            binding.tvFTDC.text = formatString(ftdc)
            binding.subTotalDeduction.text = formatStrings(receivingData.totalDduction ?: "")
            binding.tvNetPayment.text = formatStrings(receivingData.netPayment ?: "")
            binding.TvFinalPayment.text = formatStrings(receivingData.finalPayment ?: "")*/

        })
        vm.getPriceConfigInfo(
            receivingData.materialCode.toString(),
            receivingData.grade!!.substring(receivingData.grade!!.length - 4)
        )


    }

    private fun clearingPriceDetails() {
        binding.tvCertificatePremium.text = formatString(0.00)
        binding.tvVolumePremium.text = formatString(0.00)
        binding.tvHumidityPremium.text = formatString(0.00)
        binding.tvQualityDiscounting.text = formatString(0.00)
        binding.tvHumidityDiscounting.text = formatString(0.00)
        binding.TvGrossValue.text = formatString(0.00)
        binding.exportIncentive.text = formatString(0.00)
        binding.tvWitrHoldingTax.text = formatString(0.00)
        binding.tvNationalStockExchange.text = formatString(0.00)
        binding.tvBankCommission.text = formatString(0.00)
        binding.tvFTDC.text = formatString(0.00)
        binding.subTotalDeduction.text = formatString(0.00)
        binding.tvNetPayment.text = formatString(0.00)
        binding.TvFinalPayment.text = formatString(0.00)
    }

    private fun setAdvanceLinesAdapter(data: List<AdvanceLineItemDetails>) {
        if (data != null && data.size > 0) {
            binding.advanceLineItems.layoutManager =
                LinearLayoutManager(activity).apply { LinearLayoutManager.VERTICAL }
            advanceLineAdaptor = VegaNicaraguaGrnPriceCalculationAdvanceLineAdaptor(context!!, data)
            binding.advanceLineItems.adapter = advanceLineAdaptor
            advanceLineAdaptor!!.setAdapterListener(this)

            binding.advanceLineItemHeader.visibility = View.VISIBLE
            binding.advanceLineItems.visibility = View.VISIBLE
        } else {
            binding.advanceLineItems.visibility = View.GONE
            binding.advanceLineItemHeader.visibility = View.GONE
        }
        advanceLineAdaptor?.updateItem(advanceValues)
    }

    private fun moveToSummary() {

        val info = VegaNicaraguaInvoicePriceInfo()

        info.basePrice = removeDollerInValue(binding.tvBasePrice.text.toString())
        info.netWeight = removeDollerInValue(binding.tvNetWeight.text.toString())
        info.totalPrice = removeDollerInValue(binding.tvTotalPrice.text.toString())
        info.certificatePremium = removeDollerInValue(binding.tvCertificatePremium.text.toString())
        info.volumePremium = removeDollerInValue(binding.tvVolumePremium.text.toString())
        info.humidityPremium = removeDollerInValue(binding.tvHumidityPremium.text.toString())
        info.qualityDiscounting = removeDollerInValue(binding.tvQualityDiscounting.text.toString())
        info.humidityDiscounting = removeDollerInValue(binding.tvHumidityDiscounting.text.toString())
        info.exportnCentives = removeDollerInValue(binding.exportIncentive.text.toString())
        info.withholdingTax = removeDollerInValue(binding.tvWitrHoldingTax.text.toString())
        info.NSExchangeRate = removeDollerInValue(binding.tvNationalStockExchange.text.toString())
        info.bankCommission = removeDollerInValue(binding.tvBankCommission.text.toString())
        info.FTDC = removeDollerInValue(binding.tvFTDC.text.toString())
        info.totalDduction = removeDollerInValue(binding.subTotalDeduction.text.toString())
        info.netPayment = removeDollerInValue(binding.tvNetPayment.text.toString())
        receivingData.netPayment = removeDollerInValue(binding.tvNetPayment.text.toString())
        info.finalPayment = removeDollerInValue(binding.TvFinalPayment.text.toString())
        info.grossValue = removeDollerInValue(binding.TvGrossValue.text.toString())
        info.exchangeRate = if (exchangeRate?.isNotEmpty() == true) exchangeRate else receivingData.exchangeRate
        info.currency = if (currency?.isNotEmpty() == true) currency else receivingData.currency
        info.qualityGradeDesc = receivingData.gradeDesc
        info.netWeightQQs = String.format(Locale.ENGLISH, "%.2f", covertToDouble(netWeight) / 46)
        info.grossValuePerKg = String.format(
            Locale.ENGLISH, "%.2f",
            covertToDouble(
                removeDollerInValue(binding.TvGrossValue.text.toString())
            ) / covertToDouble(netWeight!!)
        )

        if (selectedAdvanceKnockOfList!!.size > 0) {
            var advanceSummary: Double = 0.0
            var interestSummary: Double = 0.0
            var commissionSummary: Double = 0.0
            var legalExpenseSummary: Double = 0.0
            var totalAdvanceSummary: Double = 0.0
            var currencyDevaluationSummary: Double = 0.0

            selectedAdvanceKnockOfList!!.forEach {
                advanceSummary = covertToDouble(it.advanceKnockAmount!!) + advanceSummary
                interestSummary = covertToDouble(it.interestAmount!!) + interestSummary
                commissionSummary = covertToDouble(it.commissionAmount!!) + commissionSummary
                legalExpenseSummary = covertToDouble(it.legalExpenseAmount!!) + legalExpenseSummary
                totalAdvanceSummary = covertToDouble(it.totalAdvanceKnockAmount!!) + totalAdvanceSummary
                currencyDevaluationSummary = covertToDouble(it.currencyDevaluationAmount!!) + currencyDevaluationSummary

            }

            info.advanceSummary = String.format(Locale.ENGLISH, "%.2f", advanceSummary)
            info.advanceInterestSummary = String.format(Locale.ENGLISH, "%.2f", interestSummary)
            info.advanceCommissionSummary = String.format(Locale.ENGLISH, "%.2f", commissionSummary)
            info.advanceLegalExpenseSummary = String.format(Locale.ENGLISH, "%.2f", legalExpenseSummary)
            info.advanceMaintainceSummary = String.format(Locale.ENGLISH, "%.2f", currencyDevaluationSummary)
            info.totalAdvanceSummary = String.format(Locale.ENGLISH, "%.2f", totalAdvanceSummary)

        }

        runOnUiThread {
            var data = Bundle()
            data.putParcelable(UIUtils.RECEIVING_DATA, receivingData)
            data.putParcelable(UIUtils.INVOICE_PRICE_INFO, info)
            data.putParcelableArrayList(UIUtils.BAGS_DATA, ArrayList(weighDetails!!.toMutableList()))
            data.putParcelableArrayList(UIUtils.QUALITY_DATA, ArrayList(qualityParameterList))
            data.putParcelableArrayList(UIUtils.PRICE_DATA, priceDetails)
            data.putParcelableArrayList(UIUtils.ADVANCE_KNOCK_DATA, selectedAdvanceKnockOfList)
            callBack?.replaceFragment(FRAG_SPOT_GRN_SUMMARY, data)
        }
    }

    override fun updatePriceDetails(selectedList: ArrayList<AdvanceLineItemDetails>) {

        selectedAdvanceKnockOfList = selectedList

        if (selectedAdvanceKnockOfList!!.size > 0) {
            var advance = 0.0
            selectedAdvanceKnockOfList!!.forEach {
                advance = covertToDouble(it.totalAdvanceKnockAmount!!) + advance
            }
            if ((netPayment?.formatTwoDigits()?.toDouble() ?: 0.0) >= advance) {
                vm.saveAdvanceLineItem(prepareAdvanceLineItem(selectedAdvanceKnockOfList!!, receivingData.tmpWbId))
                binding.TvFinalPayment.text = formatString((netPayment?.formatTwoDigits()?.toDouble() ?: 0.0) - advance)
            } else {
                showSnack(getString(R.string.advance_is_more_than_net_payment_info))
            }
        } else {
            binding.TvFinalPayment.text = binding.tvNetPayment.text.toString()
        }
    }

    override fun removeItem(model: AdvanceLineItemDetails) {
        vm.removeAdvanceLineItem(model.documentNumber, receivingData.tmpWbId)
    }

    fun prepareAdvanceLineItemDetailsList(grnPriceDetails: List<VegaNicaraguaAdvanceLineItems>): ArrayList<AdvanceLineItemDetails> {
        val advanceLineItemsList = ArrayList<AdvanceLineItemDetails>()
        grnPriceDetails.forEach {

            val advanceLineItem = AdvanceLineItemDetails()

            advanceLineItem.documentNumber = it.documentNumber
            advanceLineItem.financialYear = it.financialYear
            advanceLineItem.currency = it.currency
            advanceLineItem.postingDate = it.postingDate
            advanceLineItem.documentDate = it.documentDate
            advanceLineItem.baselineDate = it.baselineDate
            advanceLineItem.indicator = it.indicator
            advanceLineItem.businessArea = it.businessArea
            advanceLineItem.amount = it.amount
            advanceLineItem.itemNum = it.itemNum
            advanceLineItem.companyCode = it.companyCode
            advanceLineItem.vendor = it.vendor

            advanceLineItemsList.add(advanceLineItem)
        }

        return advanceLineItemsList
    }

    private fun formatString(str: Any): String {
        val strFormat = String.format(Locale.ENGLISH, "%.2f", str)
        return getString(R.string.c_doller) + " " + strFormat.format(this).replace(",", "")
    }

    private fun formatDouble(str: Any): Double {
        val strFormat = String.format(Locale.ENGLISH, "%.2f", str)
        return if (strFormat.format(this).replace(",", "").isNotEmpty()) strFormat.format(this).replace(",", "")
            .toDouble() else 0.0
    }

    private fun formatStrings(str: Any): String {
        return getString(R.string.c_doller) + " " + str.toString().format(this).replace(",", "")
    }

    private fun removeDollerInValue(value: String): String {

        var text = value.replace(getString(R.string.c_doller), "").trim()

        return text
    }

}

