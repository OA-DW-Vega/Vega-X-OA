package com.olam.warehouse.vegax.invoicenicaragua.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.AdvanceLineItemDetails
import com.olam.warehouse.master.common.utils.generateInvoiceRefNumber
import com.olam.warehouse.master.common.utils.generateInvoiceRefNumberAgain
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.common.utils.saveInvoiceSequence
import com.olam.warehouse.master.common.utils.validateInvoiceNoIsAlreadyExist
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.GrnDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceLineItemGrn
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceLineItems
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaPriceConfigDetails
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoiceGrnInventoryModal
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaInvoicePriceInfo
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils.GRN_LIST
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnecuador.utils.*
import com.olam.warehouse.vegax.invoicenicaragua.R
import com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaInvoiceDetails
import com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaInvoicePostRequest
import com.olam.warehouse.vegax.invoicenicaragua.databinding.FragmentVegaNicaraguaInvoicePriceCalculationBinding
import com.olam.warehouse.vegax.invoicenicaragua.work.getGrnInvoiceSequnceOneTimeRequestWorker
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*

class VegaNicaraguaInvoicePriceCalculationFragment : BaseFragment(),
    VegaNicaraguaInvoicePriceCalculationAdvanceLineAdaptor.CallBack {

    private var callBack: CallBack? = null
    private var priceConfigDetails: VegaNicaraguaPriceConfigDetails? = null
    private lateinit var binding: FragmentVegaNicaraguaInvoicePriceCalculationBinding

    private var vendorData: VegaVendor = VegaVendor()
    private var grnData = GrnDetails()
    private var inventoryDetails = VegaNicaraguaInvoiceGrnInventoryModal()

    private val vm: VegaNicaraguaInvoiceViewModel by viewModel()

    private var netWeight: String? = "0"
    private var totalPrice: Double? = 0.0

    private var yieldPercentage: String? = "1"//it is in percentage
    private var exchangeRate: String? = "0"//it is in percentage
    private var USDAmount: String? = "1"

    private var certificatePremium: Double? = 0.0
    private var volumePremium: Double? = 0.0
    private var humidityPremium: Double? = 0.0
    private var qualityDiscount: Double? = 0.0
    var netPayment: Double? = 0.0
    private var currency: String? = ""
    private var invoiceSequnceNo: String? = ""
    private var advanceValues = listOf<VegaNicaraguaAdvanceLineItemGrn>()
    private var selectedGrnCount = ArrayList<GrnDetails>()


    var selectedAdvanceKnockOfList: ArrayList<AdvanceLineItemDetails>? = ArrayList<AdvanceLineItemDetails>()

    private var advanceLineAdaptor: VegaNicaraguaInvoicePriceCalculationAdvanceLineAdaptor? = null

    private var qualityGradeDesc: String? = ""

    private var tempId: String = ""
    private var invoiceTransList = mutableListOf<com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaInvoiceDetails>()
    private var grnTransList = mutableListOf<VegaReceiving>()
    private var isMoveOfflineSuccess = false
    private var isMoveOnlineSuccess = false
    private var intent = Intent()

    interface CallBack {
        fun replaceFragment(moveFrag: String)
    }

    companion object {
        fun newInstance(data: Bundle) =
            VegaNicaraguaInvoicePriceCalculationFragment().putArgs {
                putAll(data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_invoice_price_calculation

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaInvoicePriceCalculationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("invoicenicaragua/ui/VegaNicaraguaInvoiceGrnFragment")
            .title("Invoice  GRN list")
            .with(tracker)
        intent = Intent(requireContext(), SuccessActivity::class.java)
        initExtra()
        initUI()
    }

    private fun initExtra() {
        arguments?.let {
            vendorData = it.getParcelable(UIUtils.VENDOR_DATA)!!
            grnData = it.getParcelable(UIUtils.GRN_DATA)!!
            inventoryDetails = it.getParcelable(UIUtils.BAGS_DATA)!!
            selectedGrnCount = it.getParcelableArrayList<GrnDetails>(GRN_LIST) ?: ArrayList()
            netWeight = grnData.toatlGrnQty
            yieldPercentage = inventoryDetails.exportable
        }
    }


    private fun initUI() {
        tempId = getTmpId()
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        val currentDateAndTime: String = simpleDateFormat.format(Date())
        binding.tvpostingdate.text = currentDateAndTime
        binding.tvpostingdate.setOnClickListener{
            getDatePickerDialog()
        }
        binding.btnProceed.setOnClickListener {
            isMoveOfflineSuccess = false
            isMoveOnlineSuccess = false
            var isValid: Boolean? = true
            var advance = 0.0
            var genericErrorMsg = ""
            if (covertToDouble(binding.basePriceEt.text.toString()) <= 0.0) {
                isValid = false
                binding.basePriceEt.error = "Please Enter Base Price"
            }
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

                if (!isValid!!) {
                    showSnack(genericErrorMsg)
                }
            }


            if(removeDollerInValue(binding.TvFinalPayment.text.toString() ).toDouble() <= 0.0 && advance.formatTwoDigits().toDouble() == 0.00)
            {
                isValid = false
                showErrorDialogWithFAQLink(requireContext(),
                    getString(R.string.final_payment_val))
            }

            if (isValid!!) {

                if (AppUtils.isOnline()) {
                    isMoveOnlineSuccess = true
                    if(isdatefunc(binding.tvpostingdate.text.toString()))
                        showConfirmDialog()
                    else {
                        moveToFailurePage(getString(R.string.future_date_pendinglist))
                        saveData(false, getString(R.string.future_date_pendinglist))
                    }
                }else{
                    showConfirmDialog()
                }

            }
        }
        vm.invoiceOffline.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateTransUI(it) })
        vm.getInvoiceOfflineData()
        vm.grnTransList.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateGrnUI(it) })
        vm.getReceivingWithLineItem()
        vm.advanceItems.observe(viewLifecycleOwner, androidx.lifecycle.Observer { advanceValues = it })
        vm.getAdvanceItem(grnData.tempId.toString())
        fetchingExchangeRate()
        vm.postInvoice.observe(viewLifecycleOwner, androidx.lifecycle.Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let {

                            if (it.data.invoiceFlag == true) {
                                moveToSuccessPage()
                                saveData(true, it.message)

                            } else {
                                val msg =
                                    if (it.data.errorMessage?.isNotEmpty() == true) it.data.errorMessage.toString() else it.message.toString()
                                moveToFailurePage(msg)
                                saveData(false, msg)
                            }
                        }
                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        saveData(false, it.error.toString())
                        hideLoading()
//                        showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                        moveToFailurePage(it.error.toString())
                    }
                }
            }
        })
    }

    private fun updateGrnUI(it: List<VegaReceiving>?) {
        it?.let {item ->
            grnTransList.clear()
            grnTransList.addAll(item.filter { it.status.equals(Status.SYNC_PENDING) || it.status.equals(Status.SYNC_COMPLETED) })
        }
    }

    private fun updateTransUI(it: List<com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaInvoiceDetails>) {
        it.let {items->
            invoiceTransList.clear()
            invoiceTransList.addAll(items.filter { it.isSynced || !it.isSynced })
        }
        intent.putExtra(AppUtils.SUB_TITLE, "Invoice Id : " + invoiceSequnceNo)
        if(isMoveOfflineSuccess) {
            isMoveOfflineSuccess = false
            saveInvoiceSequence(grnTransList, false, invoiceTransList, false)
            startActivity(intent)
            requireActivity().finish()
        }
        if (isMoveOnlineSuccess) {
            isMoveOnlineSuccess = false
            saveInvoiceSequence(grnTransList, false,invoiceTransList, false)
                postUpdateInvoiceSequence("")
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun getDatePickerDialog() {
        val cal = Calendar.getInstance()
        val DATE_FORMAT = "dd/MM/yyyy"
        val UTC = "UTC"
        context?.let {
            val datePicker = DatePickerDialog(
                it,
                com.olam.warehouse.presentation.R.style.DatePickerTheme,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val sdf = SimpleDateFormat(DATE_FORMAT)
                    sdf.timeZone = TimeZone.getTimeZone(UTC)
                    binding.tvpostingdate.text = sdf.format(cal.time)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            // datePicker.datePicker.maxDate = System.currentTimeMillis()
            datePicker.show()
            datePicker.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
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

        vm.exchangeRateOffline.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            exchangeRate = it?.exchangeRate
            USDAmount = it?.currencyValue
            currency = it?.currencyCode
            binding.tvExchangeRate.text = exchangeRate
            getAdvanceLineItems()
        })

        if (AppUtils.isOnline()) vm.getExchangeRate() else vm.getExchangeRateOffline()
    }

    private fun getAdvanceLineItems() {
        if (!vendorData.vendorCode.isNullOrEmpty()) {
            vm.advanceLineItemsDetails.observe(
                viewLifecycleOwner,
                androidx.lifecycle.Observer { response ->
                    response?.let {
                        when (it.status) {
                            Resource.Status.SUCCESS -> {
                                when (it.data?.success) {
                                    true -> {
                                        if (!it.data?.data!!.isNullOrEmpty()) {
                                            val dataValue =
                                                it.data?.data!![0].advanceLineItemDetails!!
                                            setAdvanceLinesAdapter(dataValue)
                                        }
                                    }
                                    else -> showErrorDialogWithFAQLink(
                                        requireContext(),
                                        "${it.data?.message}"
                                    )
                                }
                                getQualityGradeDesc()
                                hideLoading()
                            }
                            Resource.Status.LOADING -> showLoading()
                            Resource.Status.ERROR -> {
                                hideLoading()
                                // requireContext().toast(it.error.toString())
                                getQualityGradeDesc()
                            }
                        }
                    }
                })

            vm.advanceLineItemsOfflineDetails.observe(
                viewLifecycleOwner,
                androidx.lifecycle.Observer {
                    var data = prepareAdvanceLineItemDetailsList(it.reversed())
                    if (data != null && data.size > 0) {

                        setAdvanceLinesAdapter(data)
                    }
                    getQualityGradeDesc()
                })

            if (AppUtils.isOnline()) vm.getAdvanceDetailsByVendor(vendorData.vendorCode) else vm.getAdvanceDetailsByVendorOffline(
                "000".plus(vendorData.vendorCode)
            )

        } else {
            showSnack(requireContext().resources.getString(R.string.vendor_info_not_found))
        }
    }

    private fun getPricingInfo() {
        vm.priceConfigInfo.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            priceConfigDetails = it
            setPriceDetails()
        })
        if (inventoryDetails.qualityGrade!!.isNotEmpty() && inventoryDetails.qualityGrade!!.length > 4)
            vm.getPriceConfigInfo(
                grnData.materialNumber.toString(),
                inventoryDetails.qualityGrade!!.substring(inventoryDetails.qualityGrade!!.length - 4)
            )
    }

    private fun getQualityGradeDesc() {
        vm.getQualityGradeDesc.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                qualityGradeDesc = it.paramDesc
            }
            getPricingInfo()
        })

        grnData.qualityGrade?.replace("\\s+".toRegex(), " ")?.let { vm.getQualityGradeDesc(it) }
    }

    private fun setPriceDetails() {
        if (grnData.basePrice?.isNotEmpty() == true) {
            binding.basePriceEt.setText(grnData.basePrice)
            setPriceDetails(grnData.basePrice.toString())
        }
        binding.basePriceEt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                setPriceDetails(s.toString())
            }

        })
    }

    private fun setPriceDetails(s: String) {
        if (s.toString().length > 0) {
            var basePricePerKG: Double = s.toString().toDouble()

            if (basePricePerKG > 0) {

                if (priceConfigDetails != null) {
                    try {
                        binding.tvNetWeight.text = netWeight!! + " " + "KG(S)"

                        totalPrice = basePricePerKG * netWeight!!.toDouble()

                        binding.tvTotalPrice.text = formatString(totalPrice!!)

                        certificatePremium =
                            covertToDouble(priceConfigDetails?.certificatePremium) * netWeight!!.toDouble()

                        var volumeTo = covertToDouble(priceConfigDetails?.volumePremiumTo)
                        var volumeFrom = covertToDouble(priceConfigDetails?.volumePremiumFrom)
                        volumePremium = 0.0
                        var ftdc: Double = 0.0

                        selectedGrnCount.forEach {
                            val netWeightForEach = it.grnQty
                            var ftdcValue = if(grnData.ftdcValue.isNullOrEmpty()) priceConfigDetails?.ftdc else grnData.ftdcValue
                            ftdc = ftdc.plus(
                                ((covertToDouble(ftdcValue)) * (covertToDouble(
                                    exchangeRate!!
                                )) * (covertToDouble(it.inventoryDetail?.exportable) / 100) * netWeightForEach!!.toDouble()) / 46
                            )

                            if (volumeFrom > 0 && volumeTo > 0) {
                                if (netWeightForEach.toDouble() >= volumeFrom && netWeightForEach.toDouble() <= volumeTo) {
                                    volumePremium =
                                        volumePremium?.plus((covertToDouble(priceConfigDetails?.volumePremium) * netWeightForEach.toDouble()))
                                }
                            } else if (volumeFrom > 0) {
                                if (netWeightForEach.toDouble() >= volumeFrom) {
                                    volumePremium =
                                        volumePremium?.plus(covertToDouble(priceConfigDetails?.volumePremium) * netWeightForEach.toDouble())
                                }
                            } else if (volumeTo > 0) {
                                if (netWeightForEach.toDouble() <= volumeTo) {
                                    volumePremium =
                                        volumePremium?.plus(covertToDouble(priceConfigDetails?.volumePremium) * netWeightForEach.toDouble())
                                }
                            } else {
                                volumePremium = 0.00
                            }
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

                        var grossValue: Double =
                            formatDouble((totalPrice!! + certificatePremium!! + volumePremium!! + humidityPremium!!) - (qualityDiscount!! + humidityDiscount))
                        binding.TvGrossValue.text = formatString(grossValue)

                        var exportIncentive: Double =
                            (grossValue) * (covertToDouble(priceConfigDetails?.exportIncentive) / 100)

                        binding.exportIncentive.text = formatString(exportIncentive)

                        var withHoldingTax: Double =
                            (grossValue) * (covertToDouble(priceConfigDetails?.withHoldingTax) / 100)

                        binding.tvWitrHoldingTax.text = formatString(withHoldingTax)

                        var NSETax: Double =
                            (grossValue) * (covertToDouble(priceConfigDetails?.neCommission) / 100)

                        binding.tvNationalStockExchange.text = formatString(NSETax)

                        var commissionTax: Double =
                            (grossValue) * (covertToDouble(priceConfigDetails?.bankCommsion) / 100)

                        binding.tvBankCommission.text = formatString(commissionTax)

                        binding.tvFTDC.text = formatString(ftdc)

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
            } else {
                clearingPriceDetails()
            }
        } else {
            clearingPriceDetails()
        }
    }

    private fun clearingPriceDetails() {
        binding.tvCertificatePremium.text =
            formatString(0.00)
        binding.tvVolumePremium.text =
            formatString(0.00)
        binding.tvHumidityPremium.text =
            formatString(0.00)
        binding.tvQualityDiscounting.text =
            formatString(0.00)
        binding.tvHumidityDiscounting.text =
            formatString(0.00)
        binding.TvGrossValue.text =
            formatString(0.00)
        binding.exportIncentive.text =
            formatString(0.00)
        binding.tvWitrHoldingTax.text =
            formatString(0.00)
        binding.tvNationalStockExchange.text =
            formatString(0.00)
        binding.tvBankCommission.text =
            formatString(0.00)
        binding.tvFTDC.text = formatString(0.00)
        binding.subTotalDeduction.text =
            formatString(0.00)
        binding.tvNetPayment.text =
            formatString(0.00)
        binding.TvFinalPayment.text =
            formatString(0.00)
    }

    private fun setAdvanceLinesAdapter(data1: List<AdvanceLineItemDetails>) {
        val data = data1.filter { it.documentNumber?.startsWith("15", true) == true || it.documentNumber?.startsWith("13", true) == true }.sortedBy { it.documentNumber }
        if (data != null && data.size > 0) {
            binding.advanceLineItems.layoutManager =
                LinearLayoutManager(activity).apply { LinearLayoutManager.VERTICAL }
            advanceLineAdaptor = VegaNicaraguaInvoicePriceCalculationAdvanceLineAdaptor(requireContext(), data)
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

    private fun saveData(isSync: Boolean, msg: String) {
        var advance = 0.0
        selectedAdvanceKnockOfList!!.forEach {
            advance = covertToDouble(it.totalAdvanceKnockAmount!!) + advance
        }
        grnData.netPayment = removeDollerInValue(binding.tvNetPayment.text.toString())
        grnData.advance = advance.toString()


        var invoiceInfo =
            PrepareInvoiceData(
                vendorData,
                grnData,
                binding.basePriceEt.text.toString(),
                preparingInvoiceData(),
                removeDollerInValue(binding.tvTotalPrice.text.toString())
            )

        invoiceInfo.totalFTDC = removeDollerInValue(binding.tvFTDC.text.toString())
        invoiceInfo.totalVolumePremium = removeDollerInValue(binding.tvVolumePremium.text.toString())
        invoiceInfo.bagsCount = inventoryDetails.bagCount
        invoiceInfo.tareWeight = inventoryDetails.tareWeight
        invoiceInfo.grossWeight = inventoryDetails.grossWeight
        invoiceInfo.certification=inventoryDetails.certification
        invoiceInfo.tempId = tempId
        invoiceInfo.isSynced = isSync
        invoiceInfo.yieldPercentage = yieldPercentage
        invoiceInfo.charg = selectedGrnCount.joinToString { it.batchNumber.toString() }
        invoiceInfo.invoiceNo =
            if (invoiceInfo.invoiceNo?.isEmpty() == true) generateInvoiceRefNumber(grnTransList, invoiceTransList) else invoiceInfo.invoiceNo
       /* if(validateInvoiceNoIsAlreadyExist(grnTransList, invoiceTransList, invoiceSequnceNo.toString())){
            invoiceInfo.invoiceNo = generateInvoiceRefNumberAgain(grnTransList, invoiceTransList)
        }*/
        invoiceInfo.postDate= binding.tvpostingdate.text.toString()
        grnData.charg = invoiceInfo.charg
        val gson = Gson()
        PreferenceHelper.save(invoiceInfo.tempId, gson.toJson(prepareGrnListWithPrice()))
        vm.saveInvoiceDetails(invoiceInfo)
        vm.saveAdvanceLineItem(prepareAdvanceLineItem(selectedAdvanceKnockOfList!!, tempId))
        /*if (AppUtils.isOnline()) {
            saveInvoiceSequence(grnTransList, false,invoiceTransList, false)
            postUpdateInvoiceSequence("")
        }*/ /*else {
            saveInvoiceSequence(grnTransList, false, invoiceTransList)
        }*/

    }

    private fun moveToSuccessPage() {
        /* if (AppUtils.isOnline()) postUpdateInvoiceSequence("")
         else {
             saveInvoiceSequence()
         }*/
        var info = VegaNicaraguaInvoicePriceInfo()
        info.basePrice = removeDollerInValue(binding.basePriceEt.text.toString())
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
        info.finalPayment = removeDollerInValue(binding.TvFinalPayment.text.toString())
        info.grossValue = removeDollerInValue(binding.TvGrossValue.text.toString())
        info.currency = currency
        info.qualityGradeDesc = qualityGradeDesc
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
            var currencyDevaluationSummary: Double = 0.0
            var totalAdvanceSummary: Double = 0.0

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
            info.totalAdvanceSummary = String.format(Locale.ENGLISH, "%.2f", totalAdvanceSummary)
            info.advanceMaintainceSummary = String.format(Locale.ENGLISH, "%.2f", currencyDevaluationSummary)
            info.advanceLineItemDetails = selectedAdvanceKnockOfList as ArrayList<AdvanceLineItemDetails>
        }
        intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, requireContext().resources.getString(R.string.invoice_success))
        intent.putExtra(AppUtils.SUB_TITLE, "Invoice Id : " + invoiceSequnceNo)
        intent.putExtra(AppUtils.PRINT_ENABLE, true)
        intent.putExtra(UIUtils.INVOICE_PRICE_INFO, info)
        intent.putExtra(UIUtils.VENDOR_DATA, vendorData)
        intent.putExtra(UIUtils.FROM_NICARAGUA_COFFEE, true)
        intent.putExtra(UIUtils.NICARAGUA_PRINT_TYPE, UIUtils.NICARAGUA_PRINT_INVOICE_PTBF)
        intent.putExtra(UIUtils.GRN_DATA, grnData)
        intent.putExtra(UIUtils.BAGS_DATA, inventoryDetails)
        // intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS, tallyPrintKeys)
        /*if(isOnline()) {
            startActivity(intent)
            requireActivity().finish()
        }*/


    }

    override fun updatePriceDetails(selectedList: ArrayList<AdvanceLineItemDetails>) {

        selectedAdvanceKnockOfList = selectedList

        if (selectedAdvanceKnockOfList!!.size > 0) {
            var advance = 0.0
            selectedAdvanceKnockOfList!!.forEach {
                advance = covertToDouble(it.totalAdvanceKnockAmount!!) + advance
            }
            if ((netPayment?.formatTwoDigits()?.toDouble() ?: 0.0) >= advance) {
                vm.saveAdvanceLineItem(prepareAdvanceLineItem(selectedAdvanceKnockOfList!!, tempId))
                binding.TvFinalPayment.text = formatString((netPayment?.formatTwoDigits()?.toDouble() ?: 0.0) - advance)
            } else {
                showSnack(getString(R.string.advance_is_more_than_net_payment_info))
            }
        } else {
            binding.TvFinalPayment.text = binding.tvNetPayment.text.toString()
        }
    }

    override fun removeItem(model: AdvanceLineItemDetails) {
        vm.removeAdvanceLineItem(model.documentNumber, tempId)
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

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.invoice_proceed)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (AppUtils.isOnline()) {
                        postCreateInvoice()
                    } else {
                        isMoveOfflineSuccess = true
                        moveToSuccessPage()
                        saveData(false, getString(R.string.invoice_offline_success))

                    }
                },
                { dismiss() })
        }
    }

    private fun preparingInvoiceData(): ArrayList<VegaNicaraguaInvoiceDetails> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        var invoiceList = ArrayList<VegaNicaraguaInvoiceDetails>()
        var advanceSummary: Double = 0.0
        var interestSummary: Double = 0.0
        var commissionSummary: Double = 0.0
        var legalExpenseSummary: Double = 0.0
        var currencyDevaluationSummary: Double = 0.0
        var totalAdvanceSummary: Double = 0.0
        if (selectedAdvanceKnockOfList!!.size > 0) {
            selectedAdvanceKnockOfList!!.forEach {
                advanceSummary = covertToDouble(it.advanceKnockAmount!!) + advanceSummary
                interestSummary = covertToDouble(it.interestAmount!!) + interestSummary
                commissionSummary = covertToDouble(it.commissionAmount!!) + commissionSummary
                legalExpenseSummary = covertToDouble(it.legalExpenseAmount!!) + legalExpenseSummary
                totalAdvanceSummary = covertToDouble(it.totalAdvanceKnockAmount!!) + totalAdvanceSummary
                currencyDevaluationSummary = covertToDouble(it.currencyDevaluationAmount!!) + currencyDevaluationSummary
            }
        }
        val invoiceDetails = VegaNicaraguaInvoiceDetails()

        invoiceDetails.advanceKnockOffAmt = String.format(Locale.ENGLISH, "%.2f", advanceSummary)
        invoiceDetails.advanceSummary = advanceSummary.toString()
        invoiceDetails.advanceInterestSummary = interestSummary.toString()
        invoiceDetails.advanceCommissionSummary = commissionSummary.toString()
        invoiceDetails.advanceLegalExpenseSummary = legalExpenseSummary.toString()
        invoiceDetails.totalAdvanceSummary = totalAdvanceSummary.toString()
        invoiceDetails.advanceMaintainceSummary = currencyDevaluationSummary.toString()
        invoiceDetails.advanceLineItems = prepareAdvanceLineItems()
        invoiceDetails.bankCommission = removeDollerInValue(binding.tvBankCommission.text.toString())
        invoiceDetails.certificatePremium = removeDollerInValue(binding.tvCertificatePremium.text.toString())
        invoiceDetails.exchangeRate = exchangeRate
        invoiceDetails.exportIncentive = removeDollerInValue(binding.exportIncentive.text.toString())
        invoiceDetails.finalPayment = removeDollerInValue(binding.TvFinalPayment.text.toString())
        invoiceDetails.ftdc = removeDollerInValue(binding.tvFTDC.text.toString())
        invoiceDetails.grade = grnData.qualityGrade
        invoiceDetails.grnNetWeight = netWeight
        invoiceDetails.grnType = getString(R.string.ptbf)
        invoiceDetails.humidityPremium=removeDollerInValue(binding.tvHumidityPremium.text.toString())
        invoiceDetails.grossValue = removeDollerInValue(binding.TvGrossValue.text.toString())
        invoiceDetails.humidityDiscounting = removeDollerInValue(binding.tvHumidityDiscounting.text.toString())
        invoiceDetails.invoiceDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        invoiceDetails.key = currentKey
        invoiceDetails.materialCode = grnData.materialNumber
        invoiceDetails.materialName = grnData.materialName
        invoiceDetails.nationalStockExchange = removeDollerInValue(binding.tvNationalStockExchange.text.toString())
        invoiceDetails.netPayment = removeDollerInValue(binding.tvNetPayment.text.toString())
        invoiceDetails.netWeight = netWeight
        invoiceDetails.plant = getPlantDetails()
        invoiceDetails.poPrice = removeDollerInValue(binding.basePriceEt.text.toString())
        invoiceDetails.qualityGradeDesc = grnData.qualityGradeDesc
        invoiceDetails.qualityPremium = removeDollerInValue(binding.tvQualityDiscounting.text.toString())
        // invoiceDetails.storageLocationCode=grnData.
        //invoiceDetails.storageLocationName=grnData.
        invoiceDetails.subTotalDeductions = removeDollerInValue(binding.subTotalDeduction.text.toString())
        invoiceDetails.tempNumber = generateInvoiceRefNumber(grnTransList, invoiceTransList)
        invoiceSequnceNo = generateInvoiceRefNumber(grnTransList, invoiceTransList)
        /*if(validateInvoiceNoIsAlreadyExist(grnTransList, invoiceTransList, invoiceSequnceNo.toString())){
            invoiceDetails.tempNumber = generateInvoiceRefNumberAgain(grnTransList, invoiceTransList)
            invoiceSequnceNo = generateInvoiceRefNumberAgain(grnTransList, invoiceTransList)
        }*/
        invoiceDetails.totalPrice = removeDollerInValue(binding.tvTotalPrice.text.toString())
        invoiceDetails.vendorCode = vendorData.vendorCode
        invoiceDetails.vendorName = vendorData.vendorName
        invoiceDetails.volumePremium = removeDollerInValue(binding.tvVolumePremium.text.toString())
        invoiceDetails.withHoldingTax = removeDollerInValue(binding.tvWitrHoldingTax.text.toString())
        invoiceDetails.bagCount = inventoryDetails.bagCount
        invoiceDetails.grossWeight = inventoryDetails.grossWeight
        invoiceDetails.tareWeight = inventoryDetails.tareWeight
        invoiceDetails.batchNumber = grnData.charg
        invoiceDetails.uom = grnData.meins
        invoiceDetails.currency = currency
        invoiceDetails.certification = inventoryDetails.certification
        invoiceDetails.qualityDiscounting = removeDollerInValue(binding.tvQualityDiscounting.text.toString())
        invoiceDetails.grnList = prepareGrnListWithPrice()
        invoiceList.add(invoiceDetails)
        return invoiceList
    }

    fun prepareGrnListWithPrice(): ArrayList<GrnDetails> {
        val basePricePerKG = binding.basePriceEt.text.toString().toDouble()
        if (priceConfigDetails != null) {
            try {
                selectedGrnCount.forEach {
                    totalPrice = basePricePerKG * it.grnQty!!.toDouble()
                    certificatePremium =
                        covertToDouble(priceConfigDetails?.certificatePremium) * it.grnQty!!.toDouble()
                    var volumeTo = covertToDouble(priceConfigDetails?.volumePremiumTo)
                    var volumeFrom = covertToDouble(priceConfigDetails?.volumePremiumFrom)
                    volumePremium = 0.0
                    var ftdc: Double = 0.0

                    val netWeightForEach = it.grnQty
                    var ftdcValue = if(grnData.ftdcValue.isNullOrEmpty()) priceConfigDetails?.ftdc else grnData.ftdcValue
                    ftdc = ftdc.plus(
                        ((covertToDouble(ftdcValue)) * (covertToDouble(exchangeRate!!)) * (covertToDouble(
                            it.inventoryDetail?.exportable
                        ) / 100) * netWeightForEach!!.toDouble()) / 46
                    )

                    if (volumeFrom > 0 && volumeTo > 0) {
                        if (netWeightForEach.toDouble() >= volumeFrom && netWeightForEach.toDouble() <= volumeTo) {
                            volumePremium =
                                volumePremium?.plus((covertToDouble(priceConfigDetails?.volumePremium) * netWeightForEach.toDouble()))
                        }
                    } else if (volumeFrom > 0) {
                        if (netWeightForEach.toDouble() >= volumeFrom) {
                            volumePremium =
                                volumePremium?.plus(covertToDouble(priceConfigDetails?.volumePremium) * netWeightForEach.toDouble())
                        }
                    } else if (volumeTo > 0) {
                        if (netWeightForEach.toDouble() <= volumeTo) {
                            volumePremium =
                                volumePremium?.plus(covertToDouble(priceConfigDetails?.volumePremium) * netWeightForEach.toDouble())
                        }
                    } else {
                        volumePremium = 0.00
                    }


                    humidityPremium = covertToDouble(priceConfigDetails?.moisturePremium) * it.grnQty!!.toDouble()
                    qualityDiscount = covertToDouble(priceConfigDetails?.qualityDiscount) * it.grnQty!!.toDouble()

                    //g) Moisture Discounting - (Total Price + Certification Premium + Volume Premium + Moisture Premium) * Discount Percentage.
                    //h) Gross Value - Total Price + Certification Premium + Volume Premium + Moisture Premium - Quality Discounting - Moisture Discounting.
                    var humidityDiscount: Double =
                        formatDouble(
                            (totalPrice!! + certificatePremium!! + volumePremium!! + humidityPremium!!) * (covertToDouble(
                                priceConfigDetails?.moistureDiscount
                            ) / 100)
                        )

                    var grossValue: Double =
                        formatDouble((totalPrice!! + certificatePremium!! + volumePremium!! + humidityPremium!!) - (qualityDiscount!! + humidityDiscount))

                    var exportIncentive: Double =
                        (grossValue) * (covertToDouble(priceConfigDetails?.exportIncentive) / 100)

                    var withHoldingTax: Double =
                        (grossValue) * (covertToDouble(priceConfigDetails?.withHoldingTax) / 100)

                    var NSETax: Double =
                        (grossValue) * (covertToDouble(priceConfigDetails?.neCommission) / 100)

                    var commissionTax: Double =
                        (grossValue) * (covertToDouble(priceConfigDetails?.bankCommsion) / 100)
                    var totalDeductions =
                        formatDouble(withHoldingTax) + formatDouble(commissionTax) + formatDouble(NSETax) + formatDouble(
                            ftdc
                        )
                    netPayment = grossValue + formatDouble(exportIncentive) - formatDouble(totalDeductions)
                    it.totalPrice = grossValue.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return selectedGrnCount
    }

    private fun prepareAdvanceLineItems(): ArrayList<com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaAdvanceLineItems> {
        var advanceLineItems =
            ArrayList<com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaAdvanceLineItems>()

        selectedAdvanceKnockOfList!!.forEach {
            var advanceItem =
                com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaAdvanceLineItems()
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


    private fun postCreateInvoice() {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        val post = VegaNicaraguaInvoicePostRequest()
        post.invoiceDetails = preparingInvoiceData()
        post.key = currentKey
        vm.postCreateInvoice(post)
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

    private fun removeDollerInValue(value: String): String {
        var text = value.replace(getString(R.string.c_doller), "").trim()
        return text
    }

    private fun moveToFailurePage(msg: String) {
        /*if (AppUtils.isOnline()) postUpdateInvoiceSequence("")
        else {
            saveInvoiceSequence()
        }*/
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.transaction_fail_msg))
        intent.putExtra(AppUtils.SUB_TITLE, msg.plus("\n").plus(getString(R.string.transaction_retry)))
        intent.putExtra(AppUtils.FAILURE, false)
        intent.putExtra(AppUtils.MSG, msg)
       // startActivity(intent)
    }

    private fun postUpdateInvoiceSequence(batchNumber: String?) {
        val input = workDataOf(UIUtils.GRN_DATA to batchNumber)
        val worker = getGrnInvoiceSequnceOneTimeRequestWorker(input)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                        }
                        WorkInfo.State.FAILED -> {
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                        else -> {}
                    }
                }

            })
    }
}

