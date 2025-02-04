package com.olam.warehouse.vegax.reconcilnicaragua.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaReconcilCashMovement
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.PrintPreviewDialogFragment
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.Constants.BITMAP_KEYS
import com.olam.warehouse.presentation.utils.DoAsync
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.setOlamLogoDynamically
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.reconcilnicaragua.R
import com.olam.warehouse.vegax.reconcilnicaragua.data.domain.model.VegaNicaraguaReportModel
import com.olam.warehouse.vegax.reconcilnicaragua.databinding.FragmentReconcilTicketsBinding
import com.olam.warehouse.vegax.reconcilnicaragua.databinding.VegaNicaraguaReconcilReportPrintingBinding
import kotlinx.android.synthetic.main.item_vega_nicaragua_reconcil_report.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNicaraguaReconcilTicketFragment : BaseFragment() {

    private val vm: VegaNicaraguaReconcilReportViewModel by viewModel()
    private var callBack: Callback? = null
    private lateinit var binding: FragmentReconcilTicketsBinding
    private var bitmapPrintKeys = ArrayList<String>()
    private var reportList = ArrayList<VegaNicaraguaReportModel>()

    companion object {
        fun newInstance(
            data: VegaNicaraguaReconcilCashMovement,
            reportList: ArrayList<VegaNicaraguaReportModel>
        ) =
            VegaNicaraguaReconcilTicketFragment().putArgs {
                putParcelable("model", data)
                putParcelableArrayList("Report_List", reportList)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? Callback
    }

    override val layoutResourceId = R.layout.fragment_reconcil_tickets


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReconcilTicketsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("reconcilnicaragua/ui/VegaNicaraguaReconcilTicketFragment")
            .title("Reconcilition  Report")
            .with(tracker)
        initExtra()
        initUI()
    }

    private fun initExtra() {
        arguments?.let {
            vm.reportItem = it.getParcelable("model") ?: VegaNicaraguaReconcilCashMovement()
            reportList = it.getParcelableArrayList<VegaNicaraguaReportModel>("Report_List")
                ?: ArrayList<VegaNicaraguaReportModel>()
            reportList.forEachIndexed { index, item -> item.itemPos = (index + 1).toString() }
        }
    }


    private fun initUI() {
        binding.thousands.tvDenom.text = "1000"
        binding.fiveHundreds.tvDenom.text = "500"
        binding.twoHunderds.tvDenom.text = "200"
        binding.hundreds.tvDenom.text = "100"
        binding.fifty.tvDenom.text = "50"
        binding.twenty.tvDenom.text = "20"
        binding.tens.tvDenom.text = "10"
        binding.five.tvDenom.text = "5"
        binding.two.tvDenom.text = "2"
        binding.one.tvDenom.text = "1"
        binding.half.tvDenom.text = "0.5"

        vm.reconReport.observe(viewLifecycleOwner, Observer { updateReportOffline(it) })
        vm.getReconReport()

        binding.half.etDenomValue.onChange {
            if (it.isNotEmpty()) {
                binding.half.tvTotalValue.text = calculateDenomination("0.5", it)
                binding.half.ivClear.visible()
            } else {
                binding.half.ivClear.gone()
            }
            calculateToatalValue()
        }

        binding.thousands.etDenomValue.onChange {
            if (it.isNotEmpty()) {
                binding.thousands.tvTotalValue.text = calculateDenomination("1000", it)
                binding.thousands.ivClear.visible()
            } else {
                binding.thousands.ivClear.gone()
            }
            calculateToatalValue()
        }

        binding.fiveHundreds.etDenomValue.onChange {
            if (it.isNotEmpty()) {
                binding.fiveHundreds.tvTotalValue.text = calculateDenomination("500", it)
                binding.fiveHundreds.ivClear.visible()
            } else {
                binding.fiveHundreds.ivClear.gone()
            }
            calculateToatalValue()
        }

        binding.twoHunderds.etDenomValue.onChange {
            if (it.isNotEmpty()) {
                binding.twoHunderds.tvTotalValue.text = calculateDenomination("200", it)
                binding.twoHunderds.ivClear.visible()
            } else {
                binding.twoHunderds.ivClear.gone()
            }
            calculateToatalValue()
        }

        binding.hundreds.etDenomValue.onChange {
            if (it.isNotEmpty()) {
                binding.hundreds.tvTotalValue.text = calculateDenomination("100", it)
                binding.hundreds.ivClear.visible()
            } else {
                binding.hundreds.ivClear.gone()
            }
            calculateToatalValue()
        }

        binding.fifty.etDenomValue.onChange {
            if (it.isNotEmpty()) {
                binding.fifty.tvTotalValue.text = calculateDenomination("50", it)
                binding.fifty.ivClear.visible()
            } else {
                binding.fifty.ivClear.gone()
            }
            calculateToatalValue()
        }
        binding.twenty.etDenomValue.onChange {
            if (it.isNotEmpty()) {
                binding.twenty.tvTotalValue.text = calculateDenomination("20", it)
                binding.twenty.ivClear.visible()
            } else {
                binding.twenty.ivClear.gone()
            }
            calculateToatalValue()
        }
        binding.tens.etDenomValue.onChange {
            if (it.isNotEmpty()) {
                binding.tens.tvTotalValue.text = calculateDenomination("10", it)
                binding.tens.ivClear.visible()
            } else {
                binding.tens.ivClear.gone()
            }
            calculateToatalValue()
        }
        binding.five.etDenomValue.onChange {
            if (it.isNotEmpty()) {
                binding.five.tvTotalValue.text = calculateDenomination("5", it)
                binding.five.ivClear.visible()
            } else {
                binding.five.ivClear.gone()
            }
            calculateToatalValue()
        }
        binding.two.etDenomValue.onChange {
            if (it.isNotEmpty()) {
                binding.two.tvTotalValue.text = calculateDenomination("2", it)
                binding.two.ivClear.visible()
            } else {
                binding.two.ivClear.gone()
            }
            calculateToatalValue()
        }
        binding.one.etDenomValue.onChange {
            if (it.isNotEmpty()) {
                binding.one.tvTotalValue.text = calculateDenomination("1", it)
                binding.one.ivClear.visible()
            } else {
                binding.one.ivClear.gone()
            }
            calculateToatalValue()
        }


        binding.half.ivClear.setOnClickListener {
            binding.half.etDenomValue.setText("")
            binding.half.etDenomValue.requestFocus()
            binding.half.etDenomValue.showKeyboard()
            binding.half.tvTotalValue.text = ""
            calculateToatalValue()
        }

        binding.thousands.ivClear.setOnClickListener {
            binding.thousands.etDenomValue.setText("")
            binding.thousands.etDenomValue.requestFocus()
            binding.thousands.etDenomValue.showKeyboard()
            binding.thousands.tvTotalValue.text = ""
            calculateToatalValue()
        }

        binding.fiveHundreds.ivClear.setOnClickListener {
            binding.fiveHundreds.etDenomValue.setText("")
            binding.fiveHundreds.etDenomValue.requestFocus()
            binding.fiveHundreds.etDenomValue.showKeyboard()
            binding.fiveHundreds.tvTotalValue.text = ""
            calculateToatalValue()
        }

        binding.twoHunderds.ivClear.setOnClickListener {
            binding.twoHunderds.etDenomValue.setText("")
            binding.twoHunderds.etDenomValue.requestFocus()
            binding.twoHunderds.etDenomValue.showKeyboard()
            binding.twoHunderds.tvTotalValue.text = ""
            calculateToatalValue()
        }

        binding.hundreds.ivClear.setOnClickListener {
            binding.hundreds.etDenomValue.setText("")
            binding.hundreds.etDenomValue.requestFocus()
            binding.hundreds.etDenomValue.showKeyboard()
            binding.hundreds.tvTotalValue.text = ""
            calculateToatalValue()
        }

        binding.fifty.ivClear.setOnClickListener {
            binding.fifty.etDenomValue.setText("")
            binding.fifty.etDenomValue.requestFocus()
            binding.fifty.etDenomValue.showKeyboard()
            binding.fifty.tvTotalValue.text = ""
            calculateToatalValue()
        }
        binding.twenty.ivClear.setOnClickListener {
            binding.twenty.etDenomValue.setText("")
            binding.twenty.etDenomValue.requestFocus()
            binding.twenty.etDenomValue.showKeyboard()
            binding.twenty.tvTotalValue.text = ""
            calculateToatalValue()
        }
        binding.tens.ivClear.setOnClickListener {
            binding.tens.etDenomValue.setText("")
            binding.tens.etDenomValue.requestFocus()
            binding.tens.etDenomValue.showKeyboard()
            binding.tens.tvTotalValue.text = ""
            calculateToatalValue()
        }
        binding.five.ivClear.setOnClickListener {
            binding.five.etDenomValue.setText("")
            binding.five.etDenomValue.requestFocus()
            binding.five.etDenomValue.showKeyboard()
            binding.five.tvTotalValue.text = ""
            calculateToatalValue()
        }
        binding.two.ivClear.setOnClickListener {
            binding.two.etDenomValue.setText("")
            binding.two.etDenomValue.requestFocus()
            binding.two.etDenomValue.showKeyboard()
            binding.two.tvTotalValue.text = ""
            calculateToatalValue()
        }
        binding.one.ivClear.setOnClickListener {
            binding.one.etDenomValue.setText("")
            binding.one.etDenomValue.requestFocus()
            binding.one.etDenomValue.showKeyboard()
            binding.one.tvTotalValue.text = ""
            calculateToatalValue()
        }
        binding.btnPrint.setOnClickListener {
            showCustomLoading()
            saveValues(false)
        }

        binding.btnSave.setOnClickListener {
            saveValues(true)
            activity?.onBackPressed()
        }
    }

    fun saveValues(isSave: Boolean) {
        vm.reportItem.thousands = if (binding.thousands.etDenomValue.text.toString()
                .isNotEmpty()
        ) binding.thousands.etDenomValue.text.toString().toInt() else 0
        vm.reportItem.fiveHundred = if (binding.fiveHundreds.etDenomValue.text.toString()
                .isNotEmpty()
        ) binding.fiveHundreds.etDenomValue.text.toString().toInt() else 0
        vm.reportItem.twoHundred = if (binding.twoHunderds.etDenomValue.text.toString()
                .isNotEmpty()
        ) binding.twoHunderds.etDenomValue.text.toString().toInt() else 0
        vm.reportItem.hundred = if (binding.hundreds.etDenomValue.text.toString()
                .isNotEmpty()
        ) binding.hundreds.etDenomValue.text.toString().toInt() else 0
        vm.reportItem.fifty =
            if (binding.fifty.etDenomValue.text.toString()
                    .isNotEmpty()
            ) binding.fifty.etDenomValue.text.toString()
                .toInt() else 0
        vm.reportItem.twenty = if (binding.twenty.etDenomValue.text.toString()
                .isNotEmpty()
        ) binding.twenty.etDenomValue.text.toString().toInt() else 0
        vm.reportItem.ten =
            if (binding.tens.etDenomValue.text.toString()
                    .isNotEmpty()
            ) binding.tens.etDenomValue.text.toString()
                .toInt() else 0
        vm.reportItem.five =
            if (binding.five.etDenomValue.text.toString()
                    .isNotEmpty()
            ) binding.five.etDenomValue.text.toString()
                .toInt() else 0
        vm.reportItem.two =
            if (binding.two.etDenomValue.text.toString()
                    .isNotEmpty()
            ) binding.two.etDenomValue.text.toString()
                .toInt() else 0
        vm.reportItem.one =
            if (binding.one.etDenomValue.text.toString()
                    .isNotEmpty()
            ) binding.one.etDenomValue.text.toString()
                .toInt() else 0
        vm.reportItem.half =
            if (binding.half.etDenomValue.text.toString()
                    .isNotEmpty()
            ) binding.half.etDenomValue.text.toString()
                .toInt() else 0
        vm.insertOrUpdateReportData()
        if (!isSave) preparePrintSheet()
    }

    private fun updateReportOffline(data: VegaNicaraguaReconcilCashMovement?) {
        data?.let {
            vm.reportItem = it
            binding.half.etDenomValue.setText(if (it.half?.equals(0) == true) "" else it.half.toString())
            binding.thousands.etDenomValue.setText(if (it.thousands?.equals(0) == true) "" else it.thousands.toString())
            binding.fiveHundreds.etDenomValue.setText(if (it.fiveHundred?.equals(0) == true) "" else it.fiveHundred.toString())
            binding.twoHunderds.etDenomValue.setText(if (it.twoHundred?.equals(0) == true) "" else it.twoHundred.toString())
            binding.hundreds.etDenomValue.setText(if (it.hundred?.equals(0) == true) "" else it.hundred.toString())
            binding.fifty.etDenomValue.setText(if (it.fifty?.equals(0) == true) "" else it.fifty.toString())
            binding.twenty.etDenomValue.setText(if (it.twenty?.equals(0) == true) "" else it.twenty.toString())
            binding.tens.etDenomValue.setText(if (it.ten?.equals(0) == true) "" else it.ten.toString())
            binding.five.etDenomValue.setText(if (it.five?.equals(0) == true) "" else it.five.toString())
            binding.two.etDenomValue.setText(if (it.two?.equals(0) == true) "" else it.two.toString())
            binding.one.etDenomValue.setText(if (it.one?.equals(0) == true) "" else it.one.toString())
        }
    }

    private fun calculateToatalValue() {
        val thousands =
            if (binding.thousands.tvTotalValue.text.isNotEmpty()) binding.thousands.tvTotalValue.text.toString()
                .toDouble() else 0.0
        val fiveHundreds =
            if (binding.fiveHundreds.tvTotalValue.text.isNotEmpty()) binding.fiveHundreds.tvTotalValue.text.toString()
                .toDouble() else 0.0
        val twoHunderds =
            if (binding.twoHunderds.tvTotalValue.text.isNotEmpty()) binding.twoHunderds.tvTotalValue.text.toString()
                .toDouble() else 0.0
        val hundreds =
            if (binding.hundreds.tvTotalValue.text.isNotEmpty()) binding.hundreds.tvTotalValue.text.toString()
                .toDouble() else 0.0
        val fifty =
            if (binding.fifty.tvTotalValue.text.isNotEmpty()) binding.fifty.tvTotalValue.text.toString()
                .toDouble() else 0.0
        val twenty =
            if (binding.twenty.tvTotalValue.text.isNotEmpty()) binding.twenty.tvTotalValue.text.toString()
                .toDouble() else 0.0
        val tens =
            if (binding.tens.tvTotalValue.text.isNotEmpty()) binding.tens.tvTotalValue.text.toString()
                .toDouble() else 0.0
        val five =
            if (binding.five.tvTotalValue.text.isNotEmpty()) binding.five.tvTotalValue.text.toString()
                .toDouble() else 0.0
        val two =
            if (binding.two.tvTotalValue.text.isNotEmpty()) binding.two.tvTotalValue.text.toString()
                .toDouble() else 0.0
        val one =
            if (binding.one.tvTotalValue.text.isNotEmpty()) binding.one.tvTotalValue.text.toString()
                .toDouble() else 0.0
        val half =
            if (binding.half.tvTotalValue.text.isNotEmpty()) binding.half.tvTotalValue.text.toString()
                .toDouble() else 0.0

        val total =
            thousands + fiveHundreds + twoHunderds + hundreds + fifty + twenty + tens + five + two + one + half
        binding.tvTotalCashValue.text = total.formatThreeDigits()
        binding.tvNetAmountValue.text = total.formatThreeDigits()
        val diffrence = vm.reportItem.cashBalance?.toDouble()?.minus(total)
        binding.tvTotalDifferValue.text = diffrence?.formatThreeDigits()
    }

    private fun calculateDenomination(value: String, count: String): String {
        return (value.toDouble() * count.toInt()).formatTwoDigits()
    }

    private fun preparePrintSheet() {
        DoAsync {
            var view = LayoutInflater.from(context)
                .inflate(R.layout.vega_nicaragua_reconcil_report_printing, null)

            var viewBinder = VegaNicaraguaReconcilReportPrintingBinding.bind(view)
            setOlamLogoDynamically(viewBinder.ivOlamLogo)
            viewBinder.tvOpeningCash.text = vm.reportItem.openingCash
            viewBinder.tvRemittances.text = vm.reportItem.remittances
//            viewBinder.tvUrgentAdvance.text = vm.reportItem.urgentAdvance
//            viewBinder.tvPtbfAdvance.text = vm.reportItem.ptbfAdvance
            viewBinder.tvTotalAmt.text = totalNetAmount()
            viewBinder.tvTotalNetweight.text = totalNetWeight()
            viewBinder.tvTotalAmt1.text = totalNetAmount()
            viewBinder.tvTotalNetweight1.text = totalNetWeight()
            viewBinder.tvPreCashBalance.text = vm.reportItem.openingCash
            viewBinder.tvRemittance.text = vm.reportItem.remittances
            viewBinder.tvAdvance.text = totalAdvanceAmount()
            viewBinder.tvNetPaidAmt.text = totalNetAmount()
            val balance = vm.reportItem.openingCash?.toDouble()
                ?.plus(vm.reportItem.remittances?.toDouble() ?: 0.0)
                ?.minus(totalNetAmount().toDouble())?.minus(totalAdvanceAmount().toDouble())
            viewBinder.tvCashBalance.text = balance?.formatThreeDigits()

            viewBinder.tvThousandQty.text = vm.reportItem.thousands.toString()
            viewBinder.tvFivehundredQty.text = vm.reportItem.fiveHundred.toString()
            viewBinder.tvTwohundredQty.text = vm.reportItem.twoHundred.toString()
            viewBinder.tvhundredQty.text = vm.reportItem.hundred.toString()
            viewBinder.tvFiftyQty.text = vm.reportItem.fifty.toString()
            viewBinder.tvTwentyQty.text = vm.reportItem.twenty.toString()
            viewBinder.tvTenQty.text = vm.reportItem.ten.toString()
            viewBinder.tvFiveQty.text = vm.reportItem.five.toString()
            viewBinder.tvTwoQty.text = vm.reportItem.two.toString()
            viewBinder.tvOneQty.text = vm.reportItem.one.toString()
            viewBinder.tvhalfQty.text = vm.reportItem.half.toString()

            viewBinder.tvThousandAmt.text =
                vm.reportItem.thousands?.times(1000)?.toDouble()?.formatThreeDigits()
            viewBinder.tvFivehundredAmt.text =
                vm.reportItem.fiveHundred?.times(500)?.toDouble()?.formatThreeDigits()
            viewBinder.tvTwohundredAmt.text =
                vm.reportItem.twoHundred?.times(200)?.toDouble()?.formatThreeDigits()
            viewBinder.tvhundredAmt.text =
                vm.reportItem.hundred?.times(100)?.toDouble()?.formatThreeDigits()
            viewBinder.tvFiftyAmt.text =
                vm.reportItem.fifty?.times(50)?.toDouble()?.formatThreeDigits()
            viewBinder.tvTwentyAmt.text =
                vm.reportItem.twenty?.times(20)?.toDouble()?.formatThreeDigits()
            viewBinder.tvTenAmt.text = vm.reportItem.ten?.times(10)?.toDouble()?.formatThreeDigits()
            viewBinder.tvFiveAmt.text =
                vm.reportItem.five?.times(5)?.toDouble()?.formatThreeDigits()
            viewBinder.tvTwoAmt.text = vm.reportItem.two?.times(2)?.toDouble()?.formatThreeDigits()
            viewBinder.tvOneAmt.text = vm.reportItem.one?.times(1)?.toDouble()?.formatThreeDigits()
            viewBinder.tvhalfAmt.text =
                vm.reportItem.half?.times(0.5)?.toDouble()?.formatThreeDigits()

            val totalTickets = viewBinder.tvThousandAmt.text.toString().toDouble()
                .plus(viewBinder.tvFivehundredAmt.text.toString().toDouble())
                .plus(viewBinder.tvTwohundredAmt.text.toString().toDouble())
                .plus(viewBinder.tvhundredAmt.text.toString().toDouble())
                .plus(viewBinder.tvFiftyAmt.text.toString().toDouble())
                .plus(viewBinder.tvTwentyAmt.text.toString().toDouble())
                .plus(viewBinder.tvTenAmt.text.toString().toDouble())
            viewBinder.tvTicketTotal.text = totalTickets.formatThreeDigits()

            val totalCoins =
                viewBinder.tvFiveAmt.text.toString().toDouble()
                    .plus(viewBinder.tvTwoAmt.text.toString().toDouble())
                    .plus(viewBinder.tvOneAmt.text.toString().toDouble())
                    .plus(viewBinder.tvhalfAmt.text.toString().toDouble())
            viewBinder.tvCoinTotal.text = totalCoins.formatThreeDigits()
            viewBinder.tvOverTotalAmt.text = totalTickets.plus(totalCoins).formatThreeDigits()
            viewBinder.tvDiffrence.text = binding.tvTotalDifferValue.text
            bitmapPrintKeys.clear()
            val noOfBlock = reportList.size / 10
            val noOfBlockBalance = reportList.size % 10
            for (item: Int in 0 until noOfBlock) {
                if (item == 0) {
                    if (reportList.size != 10) viewBinder.tableInfo2.gone() else viewBinder.tableInfo2.visible()
                    val reports = reportList.take(10)
                    viewBinder.rvTableInfo1.setUp(
                        reports.toMutableList(),
                        R.layout.item_vega_nicaragua_reconcil_report,
                        { it, pos ->
                            tvSno.text = it.itemPos
                            tvDocType.text = it.documentType
                            tvMoveType.text = it.movementType
                            tvVendor.text = it.supplierName
                            tvQtyGrade.text = it.grade.plus(" - ").plus(it.gradeDesc)
                            tvNetWeight.text = it.netWeight
                            NetAmt.text = it.netPayment?.replace(getString(R.string.c_doller), "")
                        })
                    bitmapPrintKeys.add(
                        bitmapToString(
                            getBitmapFromView(
                                viewBinder.clHead1, Color.WHITE
                            )
                        )
                    )
                } else {
                    viewBinder.tableInfo2.visible()
                    val subItem = reportList.subList(item * 10, (item + 1) * 10).toMutableList()
                    viewBinder.rvTableInfo1.setUp(
                        subItem,
                        R.layout.item_vega_nicaragua_reconcil_report,
                        { it, pos ->
                            tvSno.text = it.itemPos
                            tvDocType.text = it.documentType
                            tvMoveType.text = it.movementType
                            tvVendor.text = it.supplierName
                            tvQtyGrade.text = it.grade.plus(" - ").plus(it.gradeDesc)
                            tvNetWeight.text = it.netWeight
                            NetAmt.text = it.netPayment?.replace(getString(R.string.c_doller), "")
                        })
                    if (((item + 1) * 10) == reportList.size) {
                        bitmapPrintKeys.add(
                            bitmapToString(
                                getBitmapFromView(
                                    viewBinder.clHead1A, Color.WHITE
                                )
                            )
                        )
                    } else
                        bitmapPrintKeys.add(
                            bitmapToString(
                                getBitmapFromView(
                                    viewBinder.rvTableInfo1, Color.WHITE
                                )
                            )
                        )
                }
            }

            if (noOfBlock == 0) {
                if (noOfBlockBalance < 8) {
                    viewBinder.rvTableInfo1.setUp(
                        reportList,
                        R.layout.item_vega_nicaragua_reconcil_report,
                        { it, pos ->
                            tvSno.text = it.itemPos
                            tvDocType.text = it.documentType
                            tvMoveType.text = it.movementType
                            tvVendor.text = it.supplierName
                            tvQtyGrade.text = it.grade.plus(" - ").plus(it.gradeDesc)
                            tvNetWeight.text = it.netWeight
                            NetAmt.text = it.netPayment?.replace(getString(R.string.c_doller), "")
                        })
                    bitmapPrintKeys.add(
                        bitmapToString(
                            getBitmapFromView(
                                view, Color.WHITE
                            )
                        )
                    )
                } else {
                    viewBinder.tableInfo2.visible()
                    viewBinder.rvTableInfo1.setUp(
                        reportList,
                        R.layout.item_vega_nicaragua_reconcil_report,
                        { it, pos ->
                            tvSno.text = it.itemPos
                            tvDocType.text = it.documentType
                            tvMoveType.text = it.movementType
                            tvVendor.text = it.supplierName
                            tvQtyGrade.text = it.grade.plus(" - ").plus(it.gradeDesc)
                            tvNetWeight.text = it.netWeight
                            NetAmt.text = it.netPayment?.replace(getString(R.string.c_doller), "")
                        })
                    bitmapPrintKeys.add(
                        bitmapToString(
                            getBitmapFromView(
                                viewBinder.clHead1, Color.WHITE
                            )
                        )
                    )
                    bitmapPrintKeys.add(
                        bitmapToString(
                            getBitmapFromView(
                                viewBinder.clHead2, Color.WHITE
                            )
                        )
                    )
                }
            } else {
                if (noOfBlockBalance != 0) {
                    viewBinder.clExtraItem.visible()
                    val subItem = reportList.takeLast(noOfBlockBalance).toMutableList()
                    viewBinder.rvTableInfo2.setUp(
                        subItem,
                        R.layout.item_vega_nicaragua_reconcil_report,
                        { it, pos ->
                            tvSno.text = it.itemPos
                            tvDocType.text = it.documentType
                            tvMoveType.text = it.movementType
                            tvVendor.text = it.supplierName
                            tvQtyGrade.text = it.grade.plus(" - ").plus(it.gradeDesc)
                            tvNetWeight.text = it.netWeight
                            NetAmt.text = it.netPayment?.replace(getString(R.string.c_doller), "")
                        })
                    bitmapPrintKeys.add(
                        bitmapToString(
                            getBitmapFromView(
                                viewBinder.clHead2, Color.WHITE
                            )
                        )
                    )
                } else
                    bitmapPrintKeys.add(
                        bitmapToString(
                            getBitmapFromView(
                                viewBinder.clHead2, Color.WHITE
                            )
                        )
                    )
            }
            runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(BITMAP_KEYS, gson.toJson(bitmapPrintKeys))
                startActivity(Intent(activity, WifiMainActivity::class.java))
            }

        }.execute()
        //showPreviewDialog()
    }

    override fun onResume() {
        super.onResume()
        hideCustomLoading()
    }

    private fun totalNetAmount(): String {
        var netValue = 0.0
        /* reportList.forEach {
             if (it.netPayment?.isNotEmpty() == true)
                 netValue =
                     netValue.plus((it.netPayment?.replace(getString(R.string.c_doller), "")?.trim() ?: "0").toDouble())
         }*/
        reportList.forEach {
            if (!it.movementType.equals(getString(R.string.title_advance), true)) {
                if (it.netPayment?.isNotEmpty() == true)
                    netValue =
                        netValue.plus(
                            (it.netPayment?.replace(getString(R.string.c_doller), "")?.trim()
                                ?: "0").toDouble()
                        )
            }
        }
        return netValue.formatThreeDigits()
    }

    private fun totalNetWeight(): String {
        var netValue = 0.0
        reportList.forEach {
            if (it.netWeight?.isNotEmpty() == true)
                netValue = netValue.plus((it.netWeight?.trim() ?: "0").toDouble())
        }
        return netValue.formatThreeDigits()
    }

    private fun totalAdvanceAmount(): String {
        var advanceValue = 0.0

        reportList.forEach {
            if (it.movementType.equals(getString(R.string.title_advance), true)) {
                if (it.advance?.isNotEmpty() == true)
                    advanceValue = advanceValue.plus((it.advance ?: "0").toDouble())
            }
        }
        /*var uAdvance = 0.0
        var ptbfAdvance = 0.0
        uAdvance = if (vm.reportItem.urgentAdvance?.isNotEmpty() == true) vm.reportItem.urgentAdvance.toString()
            .toDouble() else 0.0
        ptbfAdvance = if (vm.reportItem.ptbfAdvance?.isNotEmpty() == true) vm.reportItem.ptbfAdvance.toString()
            .toDouble() else 0.0
        advanceValue = uAdvance.plus(ptbfAdvance)*/
        return advanceValue.formatThreeDigits()
    }


    private fun showPreviewDialog() {
        val list = mutableListOf<String>()
        list.addAll(bitmapPrintKeys)
        val dialogFragment = PrintPreviewDialogFragment(list)
        activity?.supportFragmentManager?.let { dialogFragment.show(it, "signature") }
    }

}
