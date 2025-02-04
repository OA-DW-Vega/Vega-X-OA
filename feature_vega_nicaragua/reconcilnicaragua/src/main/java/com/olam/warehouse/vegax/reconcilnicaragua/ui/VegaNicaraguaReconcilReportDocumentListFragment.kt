package com.olam.warehouse.vegax.reconcilnicaragua.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentDate
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.reconcilnicaragua.R
import com.olam.warehouse.vegax.reconcilnicaragua.data.domain.model.VegaNicaraguaReportModel
import com.olam.warehouse.vegax.reconcilnicaragua.databinding.FragmentReconcilReportBinding
import com.olam.warehouse.vegax.reconcilnicaragua.utils.getColor
import kotlinx.android.synthetic.main.item_invoice_layout.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList

class VegaNicaraguaReconcilReportDocumentListFragment : BaseFragment() {

    private val vm: VegaNicaraguaReconcilReportViewModel by viewModel()
    private var callBack: Callback? = null
    private lateinit var binding: FragmentReconcilReportBinding
    private var reportList = ArrayList<VegaNicaraguaReportModel>()
    private var advanceList = ArrayList<VegaNicaraguaAdvanceLineItems>()
    private var qualityGradeWithDescList = ArrayList<QualitativeParams>()
    var startTime: Long = 0
    var endTime: Long = 0
    private var openCash = 0.0
    private var remittance = 0.0
    private var urgentAdvance = 0.0
    private var ptbfAdvance = 0.0

    companion object {
        fun newInstance() =
            VegaNicaraguaReconcilReportDocumentListFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? Callback
    }

    override val layoutResourceId = R.layout.fragment_reconcil_report


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentReconcilReportBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("reconcilnicaragua/ui/VegaNicaraguaReconcilReportDocumentListFragment")
            .title("Reconcilition  Report")
            .with(tracker)
        initExtra()
        initUI()
    }

    private fun initExtra() {
        /*arguments?.let {
            vendorData = it.getParcelable(UIUtils.VENDOR_DATA)!!
        }*/
    }


    private fun initUI() {
        getQualityGradeDesc()
        enableProceed(false)
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        startTime = cal.timeInMillis/* + (24 * 60 * 60 * 1000)*/
        endTime = DateUtils.getCurrentTimeInMills()

        vm.reconReport.observe(viewLifecycleOwner, Observer { updateReportOffline(it) })
        vm.grnTransList.observe(viewLifecycleOwner, Observer { updateReportListByGrn(it) })
//        vm.getReceivingWithLineItem(startTime, endTime)
        vm.invoiceOffline.observe(viewLifecycleOwner, Observer { updateReportListByInvoice(it) })
        vm.advance.observe(viewLifecycleOwner, Observer { updateReportListByAdvance(it) })
//        vm.getInvoiceOfflineData(startTime, endTime)
        binding.tvWhValue.onChange {
            binding.tvPrevBalValue.text = if (it.isNotEmpty()) it else "0"
            openCash = binding.tvPrevBalValue.text.toString().toDouble()
            val balance =
                openCash.plus(remittance).minus(totalNetAmount().toDouble()).minus(totalAdvanceAmount().toDouble())
            binding.tvCashBalValue.text = balance.formatThreeDigits()
            if (binding.tvstoValue.text?.isNotEmpty() == true && it.isNotEmpty()) enableProceed(true)
        }
        binding.tvstoValue.onChange {
            binding.tvRemValue.text = if (it.isNotEmpty()) it else "0"
            remittance = binding.tvRemValue.text.toString().toDouble()
            val balance =
                openCash.plus(remittance).minus(totalNetAmount().toDouble()).minus(totalAdvanceAmount().toDouble())
            binding.tvCashBalValue.text = balance.formatThreeDigits()
            if (binding.tvWhValue.text?.isNotEmpty() == true && it.isNotEmpty()) enableProceed(true)
        }

      /*  binding.tvUrgentAdvanceValue.onChange {
            binding.tvAdvanceValue.text = totalAdvanceAmount()
            val balance =
                openCash.plus(remittance).minus(totalNetAmount().toDouble()).minus(totalAdvanceAmount().toDouble())
            binding.tvCashBalValue.text = balance.formatThreeDigits()
            if (binding.tvWhValue.text?.isNotEmpty() == true && it.isNotEmpty()) enableProceed(true)
        }

        binding.tvPtbfAdvanceValue.onChange {
            binding.tvAdvanceValue.text = totalAdvanceAmount()
            val balance =
                openCash.plus(remittance).minus(totalNetAmount().toDouble()).minus(totalAdvanceAmount().toDouble())
            binding.tvCashBalValue.text = balance.formatThreeDigits()
            if (binding.tvWhValue.text?.isNotEmpty() == true && it.isNotEmpty()) enableProceed(true)
        }*/

        binding.btnProceed.setOnClickListener {
            prepareCashModel()
            callBack?.replaceFragment("ticket", vm.reportItem, reportList)
        }

        binding.btnSave.setOnClickListener {
            prepareCashModel()
            activity?.onBackPressed()
        }

    }

    private fun getQualityGradeDesc() {
        vm.getQualityGradeDescList.observe(this, androidx.lifecycle.Observer {
            qualityGradeWithDescList = ArrayList(it)
            vm.getReceivingWithLineItem(startTime, endTime)
        })

        vm.getQualityGradesListWithDesc()
    }

    private fun enableProceed(enable: Boolean) {
        if (enable) {
            binding.btnProceed.setBackgroundColor(getColor(if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btnProceed.isEnabled = enable
    }

    private fun updateReportOffline(data: VegaNicaraguaReconcilCashMovement?) {
        data?.let {
            vm.reportItem = it
            binding.tvPrevBalValue.text = vm.reportItem.openingCash
            binding.tvRemValue.text = vm.reportItem.remittances
            binding.tvWhValue.setText(vm.reportItem.openingCash)
            binding.tvstoValue.setText(vm.reportItem.remittances)
            binding.tvUrgentAdvanceValue.setText(vm.reportItem.urgentAdvance)
            binding.tvPtbfAdvanceValue.setText(vm.reportItem.ptbfAdvance)
            openCash = if (vm.reportItem.openingCash?.isNotEmpty() == true) vm.reportItem.openingCash?.toDouble()
                ?: 0.0 else 0.0
            remittance = if (vm.reportItem.remittances?.isNotEmpty() == true) vm.reportItem.remittances?.toDouble()
                ?: 0.0 else 0.0

            val balance =
                openCash.plus(remittance).minus(totalNetAmount().toDouble()).minus(totalAdvanceAmount().toDouble())
            binding.tvCashBalValue.text = balance.formatThreeDigits()
            binding.tvAdvanceValue.text = totalAdvanceAmount()
        }
    }

    private fun prepareCashModel() {
        vm.reportItem.edate = getCurrentDate()
        vm.reportItem.cashBalance = binding.tvCashBalValue.text.toString()
        vm.reportItem.openingCash = binding.tvPrevBalValue.text.toString()
        vm.reportItem.remittances = binding.tvRemValue.text.toString()
        vm.reportItem.urgentAdvance = binding.tvUrgentAdvanceValue.text.toString()
        vm.reportItem.ptbfAdvance = binding.tvPtbfAdvanceValue.text.toString()
        vm.insertOrUpdateReportData()
    }

    private fun setUpAdapter(list: List<VegaNicaraguaReportModel>) {
        val m = list as MutableList
        binding.rvList.setUp(list.toMutableList(), R.layout.item_invoice_layout, { it, pos ->
            tvDocNoValue.text = it.documentType
            tvMovementTypeValue.text = it.movementType
            tvVendorNameValue.text = it.supplierName
            tvnetWeightValue.text =
                if (!it.netWeight.isNullOrEmpty()) it.netWeight?.trim().plus(" ").plus(it.unitOfMeasure) else "-"
            tvQualityGradeValue.text = it.grade.plus(" - ").plus(it.gradeDesc)
            tvNetPaymentValue.text =
                if (it.netPayment?.isNotEmpty() == true) it.netPayment else getString(R.string.c_doller).plus(" ")
                    .plus("0.0")
        })
    }

    private fun updateReportListByInvoice(list: List<VegaNicaraguaInvoiceDetails>?) {
        list?.forEach {
            val desc = qualityGradeWithDescList.filter { it1 -> it1.paramName.equals(it.qualityGrade) }
            val report = VegaNicaraguaReportModel().apply {
                movementType = getString(R.string.title_invoicenicaragua)
                documentType = it.tempId
                totalPrice = it.totalPrice
                supplierName = it.supplierName
                grade = it.qualityGrade
                gradeDesc = if (desc.size > 0) desc[0].paramDesc else ""
                netWeight = it.grnQty
                unitOfMeasure = "KG(S)"
                weighBridgeId = it.wbid
                supplierCode = it.supplierCode
                materialCode = it.materialNumber
                netPayment = it.finalPayment
                advance = it.advance
            }
            reportList.add(report)
        }
        binding.tvNetAmountValue.text = totalNetAmount()
        binding.tvNetPaidValue.text = totalNetAmount()
        binding.tvAdvanceValue.text = totalAdvanceAmount()

        vm.getTransactionAdvanceData(startTime,endTime)
    }


    private fun updateReportListByAdvance(list: List<VegaNicaraguaAdvanceTransactionDetails>?) {
        list?.forEach {

            val report = VegaNicaraguaReportModel().apply {
                movementType = getString(R.string.title_advance)
                documentType = if (it.documentNumber?.isNotEmpty() == true) it.documentNumber else it.tempId
                supplierName = it.vendorName
                unitOfMeasure = "KG(S)"
                supplierCode = it.vendorCode
                netPayment = it.requestedAdvanceAmount
                advance = it.requestedAdvanceAmount
            }
            reportList.add(report)
        }
        binding.tvNetAmountValue.text = totalNetAmount()
        binding.tvNetPaidValue.text = totalNetAmount()
        binding.tvAdvanceValue.text = totalAdvanceAmount()
        setUpAdapter(reportList)
        vm.getReconReport()
    }

    private fun updateReportListByGrn(list: List<VegaReceiving>) {
        val item = list.filter {
            it.grnType?.equals(
                getString(com.olam.warehouse.login.R.string.spot),
                true
            ) == true || it.grnType?.contains(getString(com.olam.warehouse.login.R.string.fixed), true) == true
        }
        item.forEach {
            val desc = qualityGradeWithDescList.filter { it1 -> it1.paramName.equals(it.grade) }
            val report = VegaNicaraguaReportModel().apply {
                movementType = if (it.grnType?.contains(
                        getString(com.olam.warehouse.login.R.string.spot),
                        true
                    ) == true
                ) getString(R.string.invoice_spot) else getString(R.string.invoice_fixed)
                documentType = if (it.grnNumber?.isNotEmpty() == true) it.grnNumber else it.tmpWbId
                totalPrice = it.price
                supplierName = it.supplierName
                grade = it.grade
                gradeDesc = if (desc.size > 0) desc[0].paramDesc else ""
                netWeight = it.netWeight
                unitOfMeasure = "KG(S)"
                weighBridgeId = it.weighBridgeId
                supplierCode = it.supplierCode
                materialCode = it.materialCode
                netPayment = it.finalPayment
                advance = it.advance
            }
            reportList.add(report)
        }
        vm.getInvoiceOfflineData(startTime, endTime)
    }


    private fun totalNetAmount(): String {
        var netValue = 0.0
        reportList.forEach {
            if (!it.movementType.equals(getString(R.string.title_advance), true)) {
                if (it.netPayment?.isNotEmpty() == true)
                    netValue =
                        netValue.plus(
                            (it.netPayment?.replace(getString(R.string.c_doller), "")?.trim() ?: "0").toDouble()
                        )
            }
        }
        return netValue.formatThreeDigits()
    }

    private fun totalAdvanceAmount(): String {
        var advanceValue = 0.0
        reportList.forEach {
            if(it.movementType.equals(getString(R.string.title_advance),true)) {
                if (it.advance?.isNotEmpty() == true)
                    advanceValue = advanceValue.plus((it.advance ?: "0").toDouble())
            }
        }
       /* var uAdvance = 0.0
        var ptbfAdvance = 0.0
        uAdvance =
            if (binding.tvUrgentAdvanceValue.text?.isNotEmpty() == true) binding.tvUrgentAdvanceValue.text.toString()
                .toDouble() else 0.0
        ptbfAdvance =
            if (binding.tvPtbfAdvanceValue.text?.isNotEmpty() == true) binding.tvPtbfAdvanceValue.text.toString()
                .toDouble() else 0.0
        advanceValue = uAdvance.plus(ptbfAdvance)*/
        return advanceValue.formatThreeDigits()
    }

    private fun calculateCashBalance() {

    }
}
