package com.olam.warehouse.vegax.invoicenicaragua.ui.reprint

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.microsoft.appcenter.utils.HandlerUtils
import com.olam.warehouse.login.databinding.VegaNicaraguaWitholdTaxReceiptPrintingBinding
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.common.utils.PrepareInvoiceReceiptData
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaInvoiceDetails
import com.olam.warehouse.master.veganicaragua.model.VegaNicInvoiceReceipt
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoiceGrnInventoryModal
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoicePriceInfo
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnecuador.utils.PrepareInvoiceDataReverse
import com.olam.warehouse.vegax.grnecuador.utils.covertToDouble
import com.olam.warehouse.vegax.invoicenicaragua.R
import com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaInvoiceReprintModel
import com.olam.warehouse.vegax.invoicenicaragua.databinding.FragmentVegaNicaraguaInvoiceReprintBinding
import com.olam.warehouse.vegax.invoicenicaragua.ui.VegaNicaraguaInvoiceViewModel
import kotlinx.android.synthetic.main.item_vega_nicaragua_invoice_reprint.view.cbInvoiceItem
import kotlinx.android.synthetic.main.item_vega_nicaragua_invoice_reprint.view.cvInvoiceItem
import kotlinx.android.synthetic.main.item_vega_nicaragua_invoice_reprint.view.tvDateValue
import kotlinx.android.synthetic.main.item_vega_nicaragua_invoice_reprint.view.tvGrnNoValue
import kotlinx.android.synthetic.main.item_vega_nicaragua_invoice_reprint.view.tvInvoiceTempIdValue
import kotlinx.android.synthetic.main.item_vega_nicaragua_invoice_reprint.view.tvMaterialValue
import kotlinx.android.synthetic.main.item_vega_nicaragua_invoice_reprint.view.tvVendorValue
import kotlinx.android.synthetic.main.item_vega_nicaragua_withhold_tax_reprint.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

class VegaNicaraguaWithHoldTaxReprintFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_nicaragua_invoice_reprint
    private lateinit var binding: FragmentVegaNicaraguaInvoiceReprintBinding
    private val vm: VegaNicaraguaInvoiceViewModel by viewModel()
    private var invoiceList = arrayListOf<VegaNicaraguaInvoiceDetails>()
    private var printableList = arrayListOf<VegaNicaraguaInvoiceReprintModel>()
    private var selectedInvoice = VegaNicaraguaInvoiceDetails()
    private var callBack: CallBack? = null
    private var netWeight: String? = "0"
    private var supplierList = arrayListOf<VegaVendor>()
    private var bitmapPrintKeys = ArrayList<String>()
    private var inventoryInfo = VegaNicaraguaInvoiceGrnInventoryModal()


    companion object {
        fun newInstance() = VegaNicaraguaWithHoldTaxReprintFragment()
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
        vm.invoiceOffline.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateUI(it) })
        vm.grnTransList.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateGrnUI(it) })
        vm.invoiceReceipt.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateInvoiceReceiptUI(it) })
        vm.invoiceReceiptOffline.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateInvoiceReceiptOfflineUI(it) })
        vm.supplier.observe(this, androidx.lifecycle.Observer {
            supplierList = it.filter { it.purchaseOrgType.equals("NI02") } as ArrayList<VegaVendor>
            vm.getInvoiceOfflineData()
        })

        vm.getSuppliers()
        binding.tvPrint.setOnClickListener { showConfirmDialog() }

        binding.tvGrnHeading.text=getString(R.string.with_hold_tax_heading)
    }

    private fun updateInvoiceReceiptOfflineUI(it: List<VegaNicaraguaInvoiceDetails>?) {
        it?.let {
            invoiceList.addAll(it)
        }
        setupAdapter(invoiceList)
    }

    private fun updateGrnUI(it: List<VegaReceiving>?) {
        it?.let { item ->
            val data = item.filter {( it.grnType.equals("spot" ,true) || it.grnType.equals("fixed"  , true)) && covertToDouble(it.withholdingTax) >0}
            data.forEach {
                val invoice = VegaNicaraguaInvoiceDetails()
                invoice.tempId = it.tmpWbId
                invoice.grn = it.grnNumber
                invoice.wbid = it.weighBridgeId
                invoice.supplierName = it.supplierName
                invoice.supplierCode = it.supplierCode
                invoice.materialName = it.materialName
                invoice.materialNumber = it.materialCode
                invoice.qualityGrade = it.grade
                invoice.grnQty = it.netWeight
                invoice.erdat = it.erdat
                invoice.charg = it.batchNumber
                invoice.taxId = it.taxId
                invoice.bagsCount = it.bagCount
                invoice.grossWeight = it.grossWeight
                invoice.tareWeight = it.tareWeight
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
                invoice.grnType=it.grnType
                invoiceList.add(invoice)
            }
        }
        if (AppUtils.isOnline()) vm.getInvoiceReceipt() else vm.getInvoiceReceiptOffline()
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
        data?.let {
            val data = it.filter { covertToDouble(it.withholdingTax) >0.0}

            invoiceList.addAll(data) }
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
        binding.rvTransaction.setUp(itemList, R.layout.item_vega_nicaragua_withhold_tax_reprint, { it, pos ->
            tvWithHoldTaxValue.text=getString(R.string.c_doller)+" "+it.withholdingTax
            tvInvoiceTempIdValue.text = it.tempId
            tvGrnNoValue.text = it.grn
            tvVendorValue.text = it.supplierName.plus(" - ").plus(it.supplierCode)
            tvMaterialValue.text = it.materialName
            tvGrossAmountValue.text = it.grossValue
            if (it.erdat?.isNotEmpty() == true)
                if (it.erdat?.length == 8) {
                    tvDateValue.text = it.erdat?.substring(6).plus("/").plus(it.erdat?.substring(4, 6)).plus("/")
                        .plus(it.erdat?.substring(0, 4))
                }
                else
                {
                    tvDateValue.text = it.erdat.let { it1 ->
                        it1?.let { it2 ->
                            DateUtils.getUTCDateTime(
                                it2,
                                App.getAppContext()
                            )
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


    private fun setPrintableData(it: VegaNicaraguaInvoiceDetails) {
        val info = VegaNicaraguaInvoicePriceInfo()

        info.basePrice = it.basePrice
        info.netWeight = it.netWeight
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
        vendor.taxNumber = it.taxId.toString()
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
                        com.olam.warehouse.login.R.layout.vega_nicaragua_withold_tax_receipt_printing,
                        null
                    )
                val viewBinder = VegaNicaraguaWitholdTaxReceiptPrintingBinding.bind(view)
                UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogo)
                UIUtils.setOlamLogoDynamically(viewBinder.ivOlamLogoCopy)
                UIUtils.setOlamLogoDynamically(viewBinder.certificationLogoCopy)
                viewBinder.vendorName.text = vendorData.vendorName
                viewBinder.vendorId.text = vendorData.taxNumber
                viewBinder.consecutiveNumber.text = "N° " + selectedInvoice.invoiceNo
                viewBinder.withHoldTax.text = priceInfo.withholdingTax
                viewBinder.grossAmount.text = priceInfo.grossValue
                viewBinder.dateAndHour.text =
                    DateUtils.getDate(Calendar.getInstance().timeInMillis, "dd-MMM-yyyy hh:mm:ss")
                viewBinder.withHoldTaxPercentage.text = String.format(
                    Locale.ENGLISH,
                    "%.2f",
                    (covertToDouble(priceInfo.withholdingTax) * 100) / covertToDouble(priceInfo.grossValue)
                ) + "%"


                viewBinder.vendorName.text = vendorData.vendorName
                viewBinder.vendorIdCopy.text = vendorData.taxNumber
                viewBinder.consecutiveNumberCopy.text = "N° " + selectedInvoice.invoiceNo
                viewBinder.withHoldTaxCopy.text = priceInfo.withholdingTax
                viewBinder.grossAmountCopy.text = priceInfo.grossValue
                viewBinder.dateAndHourCopy.text =
                    DateUtils.getDate(Calendar.getInstance().timeInMillis, "dd-MMM-yyyy hh:mm:ss")
                viewBinder.withHoldTaxPercentageCopy.text = String.format(
                    Locale.ENGLISH,
                    "%.2f",
                    (covertToDouble(priceInfo.withholdingTax) * 100) / covertToDouble(priceInfo.grossValue)
                ) + "%"


                when (inventoryInfo.certification) {
                    UIUtils.NICERTFD_SBUX -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.starbucks_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE

                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.starbucks_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_UTZ -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.utz_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE

                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.utz_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    UIUtils.NICERTFD_RFA -> {
                        viewBinder.certificationLogo.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.rfa_logo))
                        viewBinder.certificationLogo.visibility = View.VISIBLE

                        viewBinder.certificationLogoCopy.setImageDrawable(resources.getDrawable(com.olam.warehouse.login.R.drawable.rfa_logo))
                        viewBinder.certificationLogoCopy.visibility = View.VISIBLE
                    }
                    else -> {
                        viewBinder.certificationLogo.visibility = View.GONE
                        viewBinder.certificationLogoCopy.visibility = View.GONE
                    }
                }

                bitmapPrintKeys.add(bitmapToString(getBitmapFromView(view, Color.WHITE)))
            }
            HandlerUtils.runOnUiThread {
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

}
