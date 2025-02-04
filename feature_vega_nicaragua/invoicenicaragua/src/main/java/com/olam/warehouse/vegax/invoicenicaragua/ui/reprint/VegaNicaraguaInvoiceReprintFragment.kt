package com.olam.warehouse.vegax.invoicenicaragua.ui.reprint

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import com.olam.warehouse.login.databinding.VegaNicarguaInvoicePrintRecieptBinding
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.common.utils.PrepareInvoiceReceiptData
import com.olam.warehouse.master.common.utils.getTaxIdFromVendorList
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceLineItemGrn
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaInvoiceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaPriceConfigDetails
import com.olam.warehouse.master.veganicaragua.model.VegaNicInvoiceReceipt
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoiceGrnInventoryModal
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoicePriceInfo
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.PrintPreviewDialogFragment
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.grnecuador.utils.PrepareInvoiceDataReverse
import com.olam.warehouse.vegax.grnecuador.utils.covertToDouble
import com.olam.warehouse.vegax.invoicenicaragua.R
import com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaInvoiceReprintModel
import com.olam.warehouse.vegax.invoicenicaragua.databinding.FragmentVegaNicaraguaInvoiceReprintBinding
import com.olam.warehouse.vegax.invoicenicaragua.ui.VegaNicaraguaInvoiceViewModel
import kotlinx.android.synthetic.main.item_vega_nicaragua_invoice_reprint.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 9/28/2020.
 */
class VegaNicaraguaInvoiceReprintFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_nicaragua_invoice_reprint
    private lateinit var binding: FragmentVegaNicaraguaInvoiceReprintBinding
    private val vm: VegaNicaraguaInvoiceViewModel by viewModel()
    private var invoiceList = arrayListOf<VegaNicaraguaInvoiceDetails>()
    private var printableList = arrayListOf<VegaNicaraguaInvoiceReprintModel>()
    private var selectedInvoice = VegaNicaraguaInvoiceDetails()
    private var callBack: CallBack? = null
    private var priceEdit = false
    private var exchangeRate: String? = "0"
    private var currency: String? = ""
    private var priceConfigDetails: VegaNicaraguaPriceConfigDetails? = null
    private var netWeight: String? = "0"
    private var totalPrice: Double? = 0.0
    private var grossValue: Double? = 0.0
    private var netPayment: Double? = 0.0
    private var certificatePremium: Double? = 0.0
    private var volumePremium: Double? = 0.0
    private var humidityPremium: Double? = 0.0
    private var qualityDiscount: Double? = 0.0
    private var yieldPercentage: String? = "1"//it is in percentage
    private var USDAmount: String? = "1"
    private var advanceValues = listOf<VegaNicaraguaAdvanceLineItemGrn>()
    private var supplierList = arrayListOf<VegaVendor>()
    private var bitmapPrintKeys = ArrayList<String>()
    private var inventoryInfo = VegaNicaraguaInvoiceGrnInventoryModal()
    private var qualityGradeDesc: String? = ""

    companion object {
        fun newInstance() = VegaNicaraguaInvoiceReprintFragment()
            .putArgs {
            }
    }

    interface CallBack {
        fun replaceFragment(moveFrag: String, receivingData: Any)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaInvoiceReprintBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        vm.invoiceOffline.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.grnTransList.observe(viewLifecycleOwner, Observer { updateGrnUI(it) })
        vm.invoiceReceipt.observe(viewLifecycleOwner, Observer { updateInvoiceReceiptUI(it) })
        vm.invoiceReceiptOffline.observe(viewLifecycleOwner, Observer { updateInvoiceReceiptOfflineUI(it) })
        vm.supplier.observe(this, Observer {
            supplierList = it.filter { it.purchaseOrgType.equals("NI02") } as ArrayList<VegaVendor>
            vm.getInvoiceOfflineData()
        })

        vm.getSuppliers()
        binding.tvPrint.setOnClickListener { showConfirmDialog() }
    }

    private fun updateInvoiceReceiptOfflineUI(it: List<VegaNicaraguaInvoiceDetails>?) {
        it?.let {
            invoiceList.addAll(it)
        }
        setupAdapter(invoiceList)
    }

    private fun updateGrnUI(it: List<VegaReceiving>?) {
        it?.let { item ->
            val data = item.filter { it.grnType.equals("spot", true) || it.grnType?.contains("fixed", true) == true }
            data.forEach {
                val invoice = VegaNicaraguaInvoiceDetails()
                invoice.tempId = it.invoiceNumber.toString()
                invoice.grn = it.grnNumber
                invoice.wbid = it.weighBridgeId
                invoice.supplierName = it.supplierName
                invoice.supplierCode = it.supplierCode
                invoice.materialName = it.materialName
                invoice.materialNumber = it.materialCode
                invoice.qualityGrade = it.grade
                invoice.grnQty = it.netWeight
                invoice.netWeight = it.netWeight
                invoice.erdat = it.erdat
                invoice.charg = it.batchNumber
                invoice.taxId = it.taxId
                invoice.bagsCount = it.bagCount
                invoice.grossWeight = it.grossWeight
                invoice.tareWeight = (if (it.bagCount?.isNotEmpty() == true) it.bagCount?.toInt() ?: 0 else 0).times(
                    if (it.tareWeight?.isNotEmpty() == true) it.tareWeight?.toDouble() ?: 0.0 else 0.0
                ).formatTwoDigits()
                invoice.totalFTDC = it.FTDC
                invoice.basePrice = it.basePrice
                invoice.certificatePremium = it.certificatePremium
                invoice.volumePremium = it.volumePremium
                invoice.humidityPremium = it.humidityPremium
                invoice.qualityDiscounting = it.qualityDiscounting
                invoice.humidityDiscounting = it.humidityDiscounting
                invoice.grossValue = it.grossValue
                invoice.exportnCentives = it.exportnCentives
                invoice.withholdingTax = it.withholdingTax
                invoice.NSExchangeRate = it.NSExchangeRate
                invoice.bankCommission = it.bankCommission
                invoice.totalDduction = it.totalDduction
                invoice.finalPayment = it.finalPayment
                invoice.netPayment = it.netPayment
                invoice.totalPrice = it.totalPrice
                invoice.currency = it.currency
                invoice.grossValuePerKg = it.grossValuePerKg
                invoice.qualityGradeDesc = it.qualityGradeDesc
                invoice.netWeightQQs = it.netWeightQQs
                invoice.advanceSummary = it.advanceSummary
                invoice.advanceInterestSummary = it.advanceInterestSummary
                invoice.advanceCommissionSummary = it.advanceCommissionSummary
                invoice.advanceLegalExpenseSummary = it.advanceLegalExpenseSummary
                invoice.advanceMaintainceSummary = it.advanceMaintainceSummary
                invoice.totalAdvanceSummary = it.totalAdvanceSummary
                invoice.exchangeRate = it.exchangeRate
                invoice.invoiceNo = it.invoiceNumber
                invoiceList.add(invoice)
            }
        }
        if (isOnline()) vm.getInvoiceReceipt() else vm.getInvoiceReceiptOffline()
//        setupAdapter(invoiceList)
    }

    private fun updateInvoiceReceiptUI(response: Resource<GenericReqAndResp<List<VegaNicInvoiceReceipt>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        val resultData = PrepareInvoiceReceiptData(it1, supplierList)
                        invoiceList.addAll(resultData)
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
        setupAdapter(invoiceList)
    }

    private fun updateUI(data: List<VegaNicaraguaInvoiceDetails>?) {
        invoiceList.clear()
        data?.let { invoiceList.addAll(it) }
        invoiceList.forEach { it.tempId = it.invoiceNo.toString() }
        vm.getReceivingWithLineItem()
    }

    private fun setupAdapter(itemList: MutableList<VegaNicaraguaInvoiceDetails>) {
        if (itemList.size > 0) {
            binding.rvTransaction.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvTransaction.gone()
            binding.tvNoData.visible()
        }
        binding.rvTransaction.setUp(itemList, R.layout.item_vega_nicaragua_invoice_reprint, { it, pos ->
            tvInvoiceTempIdValue.text = /*if (it.invoiceNo?.isNotEmpty() == true) it.invoiceNo else*/ it.tempId
            tvGrnNoValue.text = it.grn
            tvVendorValue.text = it.supplierName.plus(" - ").plus(it.supplierCode)
            tvMaterialValue.text = it.materialName
            tvNetWeightValue.text = it.grnQty?.trim().plus(" KG(S)")
            if (it.erdat?.isNotEmpty() == true)
                if (it.erdat?.length == 8) {
                    tvDateValue.text = it.erdat?.substring(6).plus("/").plus(it.erdat?.substring(4, 6)).plus("/")
                        .plus(it.erdat?.substring(0, 4))
                } else {
                    tvDateValue.text = it.erdat.let { it1 ->
                        it1?.let { it2 ->
                            DateUtils.getDate(it2.toLong(), "dd/MM/yyyy")
                            /* DateUtils.getUTCDateTime(
                                 it2,
                                 App.getAppContext()
                             )*/
                        }
                    }
                }
            cbInvoiceItem.isChecked = it.isChecked
            cvInvoiceItem.setOnClickListener { view ->
                it.isChecked = !it.isChecked
                cbInvoiceItem.isChecked = it.isChecked
                if (it.isChecked) setPrintData(it) else removePrintItem(it)
            }
        })
    }

    private fun removePrintItem(it: VegaNicaraguaInvoiceDetails) {
        var pos = 0
        printableList.forEachIndexed { index, vegaNicaraguaGrnReprintModel ->
            if (vegaNicaraguaGrnReprintModel.tempId.equals(it.tempId))
                pos = index
        }
        if (printableList.size > 0) printableList.removeAt(pos)
    }

    private fun setPrintData(it: VegaNicaraguaInvoiceDetails) {
        if (it.isReceiptData == false) {
            selectedInvoice = it
            netWeight = selectedInvoice.grnQty
            /*vm.advanceItems.observe(viewLifecycleOwner, Observer { advanceValues = it })
            vm.getAdvanceItem(selectedInvoice.tempId)
            getGrnInventoryDetails()*/
            setPrintableData(it)
        } else {
            selectedInvoice = it
            setPrintableData(it)
        }
    }

    fun getPlantDetails(): Plant {
        val plantDetailJson = PreferenceHelper.get(Constants.PLANT_DETAILS, "")
        return Gson().fromJson<Plant>(plantDetailJson)
    }

    /*private fun getGrnInventoryDetails() {
        if (AppUtils.isOnline()) {
            vm.grnInventoryDetails.observe(viewLifecycleOwner, Observer { response ->
                response?.let {
                    when (it.status) {
                        Resource.Status.SUCCESS -> {
                            when (it.data?.success) {
                                true -> {

                                    var qcList = it.data?.data?.let { it1 -> prepareGrnInventoryDetails(it1) }
                                    qcList?.let { it1 ->
                                        getBagParameters(it1)
                                    }
                                    fetchingExchangeRate()
                                }
                                else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                            }
                            hideLoading()
                        }
                        Resource.Status.LOADING -> showLoading()
                        Resource.Status.ERROR -> {
                            hideLoading()
                            fetchingExchangeRate()
                            //showDialog(requireContext().resources.getString(R.string.vendor_info_not_found))
                        }
                    }
                }
            })
            vm.getGrnInventoryDetailsByVendor(selectedInvoice.charg!!, selectedInvoice.materialNumber!!)
        } else {
            vm.grnInventoryDetailsOffline.observe(viewLifecycleOwner, Observer {
                var qcList = it
                getBagParameters(ArrayList(qcList))
                fetchingExchangeRate()
            })
            vm.getGrnInventoryDetailsOfflineByVendor(
                selectedInvoice.charg!!.split(",")[0].trim(),
                selectedInvoice.materialNumber?.trim()!!
            )
        }

    }

    private fun getBagParameters(list: ArrayList<VegaNicaraguaGRNInventoryDetails>) {

        var bagsQuantity: String? = ""
        var tareWeight: String? = ""
        var humidity: String? = ""
        var grossWeight: String? = ""
        var qualityGrade: String? = ""
        var certification: String? = ""
        var exportable: String? = ""

        list.forEach {
            qualityGrade = it.qualityGrade
            certification = it.certification

            if (it.qcName.equals("NISACOS")) {
                bagsQuantity = it.value
            } else if (it.qcName.equals("NIPESBRT")) {
                grossWeight = it.value
            } else if (it.qcName.equals("NIPESTAR")) {
                tareWeight = it.value
            } else if (it.qcName.equals("NIRM0003")) {
                humidity = it.value
            } else if (it.qcName.equals("NIRM0010")) {
                exportable = it.value!!.replace("%", "").trim()
            }
        }

        inventoryInfo.bagCount = bagsQuantity
        inventoryInfo.tareWeight = tareWeight
        inventoryInfo.grossWeight = grossWeight
        inventoryInfo.humidity = humidity
        inventoryInfo.qualityGrade = qualityGrade
        inventoryInfo.netWeight = selectedInvoice.grnQty
        inventoryInfo.netPrice = selectedInvoice.totalPrice
        inventoryInfo.plantId = getPlantDetails().plantId
        inventoryInfo.plantName = getPlantDetails().plantName
        inventoryInfo.exportable = exportable
        inventoryInfo.certification = certification
        yieldPercentage = exportable
        inventoryInfo.certification = certification
    }

    private fun fetchingExchangeRate() {
        vm.exchangeRate.observe(viewLifecycleOwner, androidx.lifecycle.Observer { response ->
            response?.let {

                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.data.let {
                            exchangeRate = it?.exchangeRate
                            USDAmount = it?.currencyValue
                            currency = it?.currencyCode
                            getQualityGradeDesc()
                        }
                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        getQualityGradeDesc()
                        requireContext().toast(it.error.toString())
                    }
                }
            }
        })
        vm.exchangeRateOffline.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            exchangeRate = it?.exchangeRate
            USDAmount = it?.currencyValue
            currency = it?.currencyCode

            getQualityGradeDesc()
        })

        if (AppUtils.isOnline()) vm.getExchangeRate() else vm.getExchangeRateOffline()
    }

    private fun getQualityGradeDesc() {
        vm.getQualityGradeDesc.observe(this, androidx.lifecycle.Observer {
            qualityGradeDesc = it.paramDesc
            getPricingInfo()
        })

        vm.getQualityGradeDesc(selectedInvoice.qualityGrade!!)
    }

    private fun getPricingInfo() {
        showLoading()
        vm.priceConfigInfo.observeOnce(this, androidx.lifecycle.Observer {
            priceConfigDetails = it

            setPriceDetails()
        })

        vm.getPriceConfigInfo(
            selectedInvoice.materialNumber.toString(),
            selectedInvoice.qualityGrade!!.substring(selectedInvoice.qualityGrade!!.length - 4)
        )
    }

    private fun setPriceDetails() {
        //if (priceConfigDetails != null) {

        if (priceConfigDetails != null) {
            try {
                var advance = 0.0
                if (advanceValues.size > 0) {
                    advanceValues.forEach {
                        advance = covertToDouble(it.totalAdvanceKnockAmount!!) + advance
                    }
                }

                if (priceConfigDetails?.dailyPrice?.isEmpty() == true) priceConfigDetails?.dailyPrice = "0"
                val basePricePerKG =
                    if (selectedInvoice.basePrice?.isNotEmpty() == true) selectedInvoice.basePrice?.toDouble()
                        ?: 0.0 else priceConfigDetails?.dailyPrice?.toDouble()
                totalPrice = basePricePerKG?.times(netWeight!!.toDouble())

                certificatePremium = covertToDouble(priceConfigDetails?.certificatePremium) * netWeight!!.toDouble()
                if (selectedInvoice.totalVolumePremium.isNullOrEmpty()) {
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
                } else {
                    volumePremium = covertToDouble(selectedInvoice.totalVolumePremium)
                }

                humidityPremium = covertToDouble(priceConfigDetails?.moisturePremium) * netWeight!!.toDouble()
                qualityDiscount = covertToDouble(priceConfigDetails?.qualityDiscount) * netWeight!!.toDouble()
                //g) Moisture Discounting - (Total Price + Certification Premium + Volume Premium + Moisture Premium) * Discount Percentage.
                //h) Gross Value - Total Price + Certification Premium + Volume Premium + Moisture Premium - Quality Discounting - Moisture Discounting.
                val humidityDiscount =
                    (totalPrice!! + certificatePremium!! + volumePremium!! + humidityPremium!!) * (covertToDouble(
                        priceConfigDetails?.moistureDiscount
                    ) / 100)
                grossValue =
                    (totalPrice!! + certificatePremium!! + volumePremium!! + humidityPremium!!) - (qualityDiscount!! + humidityDiscount)
                val exportIncentive = (grossValue!!) * (covertToDouble(priceConfigDetails?.exportIncentive) / 100)
                val withHoldingTax = (grossValue!!) * (covertToDouble(priceConfigDetails?.withHoldingTax) / 100)
                val NSETax = (grossValue!!) * (covertToDouble(priceConfigDetails?.neCommission) / 100)
                val commissionTax = (grossValue!!) * (covertToDouble(priceConfigDetails?.bankCommsion) / 100)
                var ftdc: Double = 0.0
                if (selectedInvoice.totalFTDC.isNullOrEmpty()) {
                    ftdc =
                        ((covertToDouble(USDAmount ?: "")) * (covertToDouble(exchangeRate ?: "")) * (covertToDouble(
                            yieldPercentage ?: ""
                        ) / 100) * netWeight!!.toDouble()) / 46
                } else {
                    ftdc = covertToDouble(selectedInvoice.totalFTDC)
                }
                val totalDeductions = withHoldingTax + commissionTax + NSETax + ftdc
                netPayment = grossValue!! + exportIncentive - totalDeductions

                val info = VegaNicaraguaInvoicePriceInfo()

                info.basePrice = selectedInvoice.basePrice
                info.netWeight = netWeight
                info.totalPrice = formatString(totalPrice!!)
                info.certificatePremium = formatString(certificatePremium!!)
                info.volumePremium = formatString(volumePremium!!)
                info.humidityPremium = formatString(humidityPremium!!)
                info.qualityDiscounting = formatString(qualityDiscount!!)
                info.humidityDiscounting = formatString(humidityDiscount)
                info.exportnCentives = formatString(exportIncentive)
                info.withholdingTax = formatString(withHoldingTax)
                info.NSExchangeRate = formatString(NSETax)
                info.bankCommission = formatString(commissionTax)
                info.FTDC = formatString(ftdc)
                info.totalDduction = formatString(totalDeductions)
                info.netPayment = formatString(netPayment!!)
                info.finalPayment = formatString(netPayment!! - advance)
                info.grossValue = formatString(grossValue!!)
                info.currency = currency
                info.qualityGradeDesc = qualityGradeDesc
                info.grossValuePerKg = formatString(
                    grossValue!! / covertToDouble(netWeight!!)
                )
                info.netWeightQQs = formatString(covertToDouble(netWeight) / 46)
                if (advanceValues.size > 0) {
                    var advanceSummary: Double = 0.0
                    var interestSummary: Double = 0.0
                    var commissionSummary: Double = 0.0
                    var legalExpenseSummary: Double = 0.0
                    var totalAdvanceSummary: Double = 0.0
                    var currencyDevaluationSummary: Double = 0.0

                    advanceValues.forEach {
                        advanceSummary = covertToDouble(it.advanceKnockAmount!!) + advanceSummary
                        interestSummary = covertToDouble(it.interestAmount!!) + interestSummary
                        commissionSummary = covertToDouble(it.commissionAmount!!) + commissionSummary
                        legalExpenseSummary = covertToDouble(it.legalExpenseAmount!!) + legalExpenseSummary
                        totalAdvanceSummary = covertToDouble(it.totalAdvanceKnockAmount!!) + totalAdvanceSummary
                        info.advanceMaintainceSummary =
                            String.format(Locale.ENGLISH, "%.2f", currencyDevaluationSummary)
                    }

                    info.advanceSummary = String.format(Locale.ENGLISH, "%.2f", advanceSummary)
                    info.advanceInterestSummary = String.format(Locale.ENGLISH, "%.2f", interestSummary)
                    info.advanceCommissionSummary = String.format(Locale.ENGLISH, "%.2f", commissionSummary)
                    info.advanceLegalExpenseSummary = String.format(Locale.ENGLISH, "%.2f", legalExpenseSummary)
                    info.totalAdvanceSummary = String.format(Locale.ENGLISH, "%.2f", totalAdvanceSummary)

                }

                val printModel = VegaNicaraguaInvoiceReprintModel()
                printModel.tempId = selectedInvoice.tempId
                printModel.grnData = PrepareInvoiceDataReverse(selectedInvoice)
                printModel.priceInfo = info

                val vendor = VegaVendor()
                vendor.vendorCode = selectedInvoice.supplierCode.toString()
                vendor.vendorName = selectedInvoice.supplierName.toString()
                vendor.taxNumber = selectedInvoice.taxId.toString()
                printModel.vendor = vendor

                printableList.add(printModel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            *//*} else {
            showSnack(getString(R.string.price_info_not_found))
        }*//*
            hideLoading()
        }
    }*/

    private fun formatTwoDigString(str: String): String {
        if (str.isEmpty() || !str.contains(".") || !str.contains(",")) return str
        val strFormat = String.format(Locale.ENGLISH, "%.2f", str)
        return strFormat.format(this).replace(",", "")
    }

    private fun setPrintableData(it: VegaNicaraguaInvoiceDetails) {
        val info = VegaNicaraguaInvoicePriceInfo()

        info.basePrice = it.basePrice
        info.netWeight = if (it.netWeight?.isNotEmpty() == true) it.netWeight else it.grnQty
        info.totalPrice = it.totalPrice
        info.certificatePremium = it.certificatePremium
        info.volumePremium = it.volumePremium
        info.humidityPremium = it.humidityPremium
        info.qualityDiscounting = it.qualityDiscounting
        info.humidityDiscounting = it.humidityDiscounting
        info.exportnCentives = it.exportnCentives
        info.withholdingTax = it.withholdingTax
        info.NSExchangeRate = it.NSExchangeRate
        info.bankCommission = it.bankCommission
        info.FTDC = it.FTDC
        info.totalDduction = it.totalDduction
        info.netPayment = it.netPayment
        info.finalPayment = it.finalPayment
        info.grossValue = it.grossValue
        info.currency = it.currency
        info.qualityGradeDesc = it.qualityGradeDesc
        info.grossValuePerKg = it.grossValuePerKg
        info.netWeightQQs = it.netWeightQQs
        info.advanceSummary = it.advanceSummary
        info.advanceInterestSummary = it.advanceInterestSummary
        info.advanceCommissionSummary = it.advanceCommissionSummary
        info.advanceLegalExpenseSummary = it.advanceLegalExpenseSummary
        info.totalAdvanceSummary = it.totalAdvanceSummary

        val printModel = VegaNicaraguaInvoiceReprintModel()
        printModel.tempId = it.tempId
        printModel.grnData = PrepareInvoiceDataReverse(it)
        printModel.priceInfo = info

        val vendor = VegaVendor()
        vendor.vendorCode = it.supplierCode.toString()
        vendor.vendorName = it.supplierName.toString()
        vendor.taxNumber =
            if (it.taxId?.isNotEmpty() == true) it.taxId else getTaxIdFromVendorList(supplierList, it.supplierCode)
        printModel.vendor = vendor

        printableList.add(printModel)
    }

    private fun generateBitMapKey() {
        bitmapPrintKeys.clear()
        showCustomLoading()
        DoAsync {
            printableList.forEachIndexed { index, item ->
                val vendorData = item.vendor
                val grnData = item.grnData
                val priceInfo = item.priceInfo

                val view = LayoutInflater.from(context)
                    .inflate(
                        com.olam.warehouse.login.R.layout.vega_nicargua_invoice_print_reciept,
                        null
                    )
                val viewBinder = VegaNicarguaInvoicePrintRecieptBinding.bind(view)
                UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogo)
                UIUtils.setOlamLogoDynamically(viewBinder.certificationLogo)
                viewBinder.vendorName.text = vendorData.vendorName
                viewBinder.currency.text = priceInfo.currency
                viewBinder.materialName.text = grnData.materialName
                viewBinder.tvBagNetWeightQQS.text =
                    String.format(Locale.ENGLISH, "%.2f", covertToDouble(priceInfo.netWeight) / 46)

                if (selectedInvoice.erdat?.isNotEmpty() == true)
                    if (selectedInvoice.erdat?.length == 8) {
                        var date = selectedInvoice.erdat?.substring(6).plus("/")
                            .plus(selectedInvoice.erdat?.substring(4, 6)).plus("/")
                            .plus(selectedInvoice.erdat?.substring(0, 4))
                        viewBinder.dateOfPay.text = date
                    } else {
                        var date = selectedInvoice.erdat.let { it1 ->
                            it1?.let { it2 ->
                                DateUtils.getDate(it2.toLong(), "dd/MM/yyyy")
                                /*DateUtils.getUTCDateTime(
                                    it2,
                                    App.getAppContext()
                                )*/
                            }
                        }
                        viewBinder.dateOfPay.text = date
                    }
                viewBinder.TvTotalPrice.text = priceInfo.totalPrice

                viewBinder.TvCertificatePremium.text = priceInfo.certificatePremium
                viewBinder.TvHumidityPremium.text = priceInfo.humidityPremium
                viewBinder.TvVolumePremium.text = priceInfo.volumePremium

                viewBinder.TvHumidityDiscount.text = priceInfo.humidityDiscounting
                viewBinder.TvQualityDiscount.text = priceInfo.qualityDiscounting

                viewBinder.TvGrossPrice.text = priceInfo.grossValue

                viewBinder.TVexportIncentive.text = priceInfo.exportnCentives

                viewBinder.tvWitrHoldingTax.text = priceInfo.withholdingTax
                viewBinder.tvBankCommission.text = priceInfo.bankCommission
                viewBinder.tvNationalStockExchange.text = priceInfo.NSExchangeRate

                if (selectedInvoice.totalFTDC?.isNotEmpty() == true) {
                    viewBinder.tvFTDC.text = selectedInvoice.totalFTDC
                } else {
                    viewBinder.tvFTDC.text = priceInfo.FTDC
                }
                viewBinder.subTotalDeduction.text = priceInfo.totalDduction

                viewBinder.tvAdvances.text = priceInfo.advanceSummary
                viewBinder.tvInterest.text = priceInfo.advanceInterestSummary
                viewBinder.tvAdvanceCommission.text = priceInfo.advanceCommissionSummary
                viewBinder.tvAdvanceLegalExpense.text = priceInfo.advanceLegalExpenseSummary
                viewBinder.tvMaintenanceValue.text = priceInfo.advanceMaintainceSummary
                viewBinder.tvSubTotalAdvanceDeductions.text = priceInfo.totalAdvanceSummary
                val nPayment = (if (priceInfo.netPayment?.isNotEmpty() == true) priceInfo.netPayment?.toDouble()
                    ?: 0.0 else 0.0).minus(
                    (if (priceInfo.totalAdvanceSummary?.isNotEmpty() == true) priceInfo.totalAdvanceSummary?.toDouble()
                        ?: 0.0 else 0.0)
                )
                viewBinder.tvNetPayment.text = nPayment.formatTwoDigits()

                viewBinder.plantName.text = getPlantDetails().plantName
                viewBinder.qualityGrade.text = priceInfo.qualityGradeDesc
                if (selectedInvoice.bagsCount?.isNotEmpty() == true) {
                    viewBinder.tvBagsQuantity.text = selectedInvoice.bagsCount
                } else {
                    viewBinder.tvBagsQuantity.text = inventoryInfo.bagCount
                }
                if (selectedInvoice.grossWeight?.isNotEmpty() == true) {
                    viewBinder.tvGrossWeight.text = formatTwoDigString(selectedInvoice.grossWeight.toString())
                } else {
                    viewBinder.tvGrossWeight.text = formatTwoDigString(inventoryInfo.grossWeight.toString())
                }

                if (selectedInvoice.tareWeight?.isNotEmpty() == true) {
                    viewBinder.tvTareWeight.text = formatTwoDigString(selectedInvoice.tareWeight.toString())
                } else {
                    viewBinder.tvTareWeight.text = formatTwoDigString(inventoryInfo.tareWeight.toString())
                }


                viewBinder.tvBagNetWeight.text = formatTwoDigString(priceInfo.netWeight.toString())
                viewBinder.tvGrossPricetKg.text = String.format(
                    Locale.ENGLISH, "%.2f",
                    covertToDouble(
                        priceInfo.grossValue
                    ) / covertToDouble(priceInfo.netWeight!!)
                )
                viewBinder.tvtotalAmount.text = priceInfo.grossValue
                viewBinder.coffeeSettlement.text = selectedInvoice.invoiceNo
                viewBinder.identificationNum.text = vendorData.taxNumber
                val plant = com.olam.warehouse.master.common.utils.getPlantDetails()
                viewBinder.sAPMIRONum.text =
                    if (selectedInvoice.tempId.contains(plant.plantId)) "" else selectedInvoice.tempId
                viewBinder.tvBagNetWeightQQS.text =
                    String.format(Locale.ENGLISH, "%.2f", covertToDouble(priceInfo.netWeight) / 46)

                viewBinder.batchList.text = selectedInvoice.charg

                viewBinder.note.text = Html.fromHtml(
                    getString(R.string.s_invoice_note_1) + " <b>" + vendorData.vendorName + " </b>" + getString(
                        com.olam.warehouse.login.R.string.s_invoice_note_2
                    )
                ).toString()


                when (selectedInvoice.certification) {
                    UIUtils.NICERTFD_SBUX -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.starbucks_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_UTZ -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.utz_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_RFA -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.rfa_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE
                    }
                    else -> {
                        viewBinder.certificationLogo.visibility = View.GONE
                    }
                }

                if (covertToDouble(priceInfo.volumePremium) > 0) {
                    viewBinder.volumePremiumLayout.visibility = View.VISIBLE
                } else {
                    viewBinder.volumePremiumLayout.visibility = View.GONE
                }

                if (covertToDouble(priceInfo.certificatePremium) > 0) {
                    viewBinder.certificatePremiumLayout.visibility = View.VISIBLE
                } else {
                    viewBinder.certificatePremiumLayout.visibility = View.GONE
                }

                if (covertToDouble(priceInfo.humidityPremium) > 0) {
                    viewBinder.humidityPremiumLayout.visibility = View.VISIBLE
                } else {
                    viewBinder.humidityPremiumLayout.visibility = View.GONE
                }

                if (covertToDouble(priceInfo.humidityDiscounting) > 0) {
                    viewBinder.humidityDiscountLayout.visibility = View.VISIBLE
                } else {
                    viewBinder.humidityDiscountLayout.visibility = View.GONE
                }

                if (covertToDouble(priceInfo.qualityDiscounting) > 0) {
                    viewBinder.qualityDiscountLayout.visibility = View.VISIBLE
                } else {
                    viewBinder.qualityDiscountLayout.visibility = View.GONE
                }

                if (!(viewBinder.qualityDiscountLayout.isVisible || viewBinder.humidityDiscountLayout.isVisible)) {
                    viewBinder.discountHeader.visibility = View.GONE
                }

                if (!(viewBinder.humidityPremiumLayout.isVisible || viewBinder.certificatePremiumLayout.isVisible || viewBinder.volumePremiumLayout.isVisible)) {
                    viewBinder.othersHeader.visibility = View.GONE
                }

                if (covertToDouble(priceInfo.advanceSummary) > 0) {

                    if (covertToDouble(priceInfo.advanceInterestSummary) > 0) {
                        viewBinder.interestLayout.visibility = View.VISIBLE
                    } else {
                        viewBinder.interestLayout.visibility = View.GONE
                    }

                    if (covertToDouble(priceInfo.advanceCommissionSummary) > 0) {
                        viewBinder.commissionLayout.visibility = View.VISIBLE
                    } else {
                        viewBinder.commissionLayout.visibility = View.GONE
                    }

                    if (covertToDouble(priceInfo.advanceLegalExpenseSummary) > 0) {
                        viewBinder.legalExpenceLayout.visibility = View.VISIBLE
                    } else {
                        viewBinder.legalExpenceLayout.visibility = View.GONE
                    }
                    if (covertToDouble(priceInfo.advanceMaintainceSummary) > 0) {
                        viewBinder.maintainanceLayout.visibility = View.VISIBLE
                    } else {
                        viewBinder.maintainanceLayout.visibility = View.GONE
                    }
                    if (covertToDouble(priceInfo.totalAdvanceSummary) > 0) {
                        viewBinder.totalAdvanceLayout.visibility = View.VISIBLE
                    } else {
                        viewBinder.totalAdvanceLayout.visibility = View.GONE
                    }

                } else {

                    viewBinder.advanceLayout.visibility = View.GONE
                    viewBinder.commissionLayout.visibility = View.GONE
                    viewBinder.interestLayout.visibility = View.GONE
                    viewBinder.legalExpenceLayout.visibility = View.GONE
                    viewBinder.maintainanceLayout.visibility = View.GONE
                    viewBinder.totalAdvanceLayout.visibility = View.GONE
                }


                bitmapPrintKeys.add(bitmapToString(getBitmapFromView(view, Color.WHITE)))
            }
            runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(bitmapPrintKeys))
                startActivity(Intent(activity, WifiMainActivity::class.java))
            }
        }.execute()
    }

    override fun onResume() {
        super.onResume()
        hideCustomLoading()
    }

    private fun showPreviewDialog() {
        val list = mutableListOf<String>()
        list.addAll(bitmapPrintKeys)
        val dialogFragment =
            PrintPreviewDialogFragment(list)
        activity?.supportFragmentManager?.let { dialogFragment.show(it, "signature") }
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_print)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    generateBitMapKey()
                },
                { dismiss() })
        }
    }

    private fun enableSync(flag: Boolean) {
        binding.tvPrint.isEnabled = flag
        if (flag)
            ViewCompat.setBackgroundTintList(
                binding.tvPrint,
                context?.let {
                    ContextCompat.getColorStateList(
                        it,
                        com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                    )
                }
            )
        else
            ViewCompat.setBackgroundTintList(
                binding.tvPrint,
                context?.let {
                    ContextCompat.getColorStateList(
                        it,
                        com.olam.warehouse.presentation.R.color.grey
                    )
                }
            )

    }

    private fun formatString(str: Any): String {
        val strFormat = String.format(Locale.ENGLISH, "%.2f", str)
        return strFormat
    }

    /*fun prepareGrnInventoryDetails(inventoryRes: InventoryResponse): ArrayList<VegaNicaraguaGRNInventoryDetails> {

        val vegaInventoryDetails = ArrayList<VegaNicaraguaGRNInventoryDetails>()
        if (!inventoryRes.inventory.isNullOrEmpty()) {
            inventoryRes.inventory.forEach { inventory ->
                inventory.inventoryQC?.forEach {

                    val inventoryDetails = VegaNicaraguaGRNInventoryDetails()
                    inventoryDetails.id = inventory.id

                    inventory.warehouseLocation?.let { wareHouseLocation ->
                        inventoryDetails.warehouseId = wareHouseLocation.warehouse!!.warehouseId
                        inventoryDetails.warehouseName = wareHouseLocation.warehouse!!.warehouseName
                        inventoryDetails.plantId = wareHouseLocation.warehouse!!.plant!!.plantCode!!
                        inventoryDetails.plantName = wareHouseLocation.warehouse!!.plant!!.plantName!!
                        inventoryDetails.procureLocationCode = wareHouseLocation.procureLocationCode
                        inventoryDetails.procureLocationName = wareHouseLocation.procureLocationName

                    }

                    inventoryDetails.materialCode = inventory.materialCode!!
                    inventoryDetails.lotId = inventory.lotId!!
                    inventoryDetails.stockQty = inventory.stockQty
                    inventoryDetails.grnDate = inventory.grnDate
                    inventoryDetails.createdDateTime = inventory.createdDateTime
                    inventoryDetails.createdBy = inventory.createdBy
                    inventoryDetails.updatedAt = inventory.updatedAt
                    inventoryDetails.updatedBy = inventory.updatedBy
                    inventoryDetails.qualityGrade = inventory.qualityGrade
                    inventoryDetails.certification = inventory.certification

                    inventoryDetails.qcId = it.qcId
                    inventoryDetails.qcName = it.qcName!!
                    inventoryDetails.dataType = it.dataType
                    inventoryDetails.numberDecimals = it.numberDecimals
                    inventoryDetails.entryObligatory = it.entryObligatory
                    inventoryDetails.value = it.value

                    vegaInventoryDetails.add(inventoryDetails)

                }
            }
        }
        return vegaInventoryDetails
    }*/
}
