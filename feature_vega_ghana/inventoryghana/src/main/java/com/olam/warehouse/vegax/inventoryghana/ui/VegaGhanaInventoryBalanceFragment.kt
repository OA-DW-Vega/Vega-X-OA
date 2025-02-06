package com.olam.warehouse.vegax.inventoryghana.ui

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.inventoryghana.R
import com.olam.warehouse.vegax.inventoryghana.data.domain.model.ReportsList
import com.olam.warehouse.vegax.inventoryghana.data.domain.model.VegaGhanaInventoryPost
import com.olam.warehouse.vegax.inventoryghana.data.domain.model.VegaGhanaInventoryReportResponse
import com.olam.warehouse.vegax.inventoryghana.databinding.FragmentVegaGhanaInventoryBalanceBinding
import com.olam.warehouse.vegax.inventoryghana.utils.getFormatedDate
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*


class VegaGhanaInventoryBalanceFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_ghana_inventory_balance
    private var callBack: CallBack? = null
    private lateinit var binding: FragmentVegaGhanaInventoryBalanceBinding
    private var lotId: String = ""
    private val vm: VegaGhanaInventoryViewModel by viewModel()
    private var enddate: String = ""
    private var edate: Date = Date()
    private var sdate: Date = Date()
    private var startdate: String = ""
    private var reportList = ArrayList<ReportsList>()
    private var totalweight = ArrayList<String>()
    private var inventoryBalance = ArrayList<VegaGhanaInventoryReportResponse>()
    private var storelocCode: String = ""

    interface CallBack {

    }

    companion object {
        fun newInstance() = VegaGhanaInventoryBalanceFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (lotId.isNotEmpty()) menu.clear()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaGhanaInventoryBalanceBinding.inflate(layoutInflater)
        return binding.root
    }

    private fun initUI() {
        binding.rvInventoryBalance.layoutManager = LinearLayoutManager(activity).apply { LinearLayoutManager.VERTICAL }

        binding.tvstartdate.setOnClickListener {
            getDatePickerDialog(0)
        }
        binding.tvenddate.setOnClickListener {
            if(startdate.isNotEmpty())
            getDatePickerDialog(1)
            else
                showSnack(getString(R.string.selectionofdates))
        }

    }

    private fun getDatePickerDialog(i: Int) {
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
                    if (i == 0) {
                        sdate = cal.time
                        binding.tvstartdate.text = sdf.format(cal.time)
                        startdate = getFormatedDate(binding.tvstartdate.text.toString())
                        println("=======startdate==========$startdate")
                    } else {
                        edate = cal.time
                        binding.tvenddate.text = sdf.format(cal.time)
                        enddate = getFormatedDate(binding.tvenddate.text.toString())
                        println("==========enddate==============$enddate")
                        val difference_In_Time: Long = edate.time - sdate.time
                        println("========difference_In_Time========$difference_In_Time")
                        val difference_In_Days = ((difference_In_Time
                                / (1000 * 60 * 60 * 24))
                                % 365)
                        println("========difference_In_Days========$difference_In_Days")

                        if(difference_In_Days <= 30)
                        {
                            vm.getInventoryReports(
                                VegaGhanaInventoryPost(
                                    key = getCurrentKey(),
                                    plant = getPlantDetails(),
                                    startdate,
                                    enddate
                                )
                            )
                            vm.inventoryreport.observe(viewLifecycleOwner, { updateUI(it) })
                        }else
                        {
                            showSnack("Minimum 30 days allowed")
                            binding.tvstartdate.text = ""
                            binding.tvenddate.text = ""
                            binding.tvstartdate.hint = "Date"
                            binding.tvenddate.hint = "Date"
                            startdate =""
                            enddate =""
                        }
                    }

                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.maxDate = System.currentTimeMillis()
            if (i==0)
            {
                cal.add(Calendar.YEAR,-1)
                datePicker.datePicker.minDate = cal.timeInMillis
            }else{
                datePicker.datePicker.minDate = sdate.time
            }
            datePicker.show()
            datePicker.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaGhanaInventoryReportResponse>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()

                    binding.tvNoData.visibility = View.GONE
                    binding.rvInventoryBalance.visibility = View.VISIBLE
                    inventoryBalance = it.data?.data as ArrayList<VegaGhanaInventoryReportResponse>
                    if (inventoryBalance.size > 0)
                        binding.tvNoData.visibility = View.GONE
                        inventoryBalance.forEachIndexed { ind , vegaGhanaInventoryReportResponse ->
                            reportList = vegaGhanaInventoryReportResponse.reportsList as ArrayList<ReportsList>
                            reportList.forEachIndexed { index, reportsList ->
                                var totalweight = 0.0
                                totalweight = reportsList.totalDryingLossQty.toDouble() +
                                        reportsList.totalFgrnQty.toDouble() +
                                        reportsList.totalGrnQty.toDouble()+
                                        reportsList.totalRminQty.toDouble()+
                                        reportsList.totalMtntQty.toDouble()
                                inventoryBalance.get(ind).totalweight = totalweight.formatThreeDigits().toString().plus( "  MT")

                            }

                        }
                        setAdapter(
                            inventoryBalance as ArrayList<VegaGhanaInventoryReportResponse>,
                            reportList as ArrayList<ReportsList>
                        )

                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    if(it.error.toString().equals("Reports are not available in the selected dates"))
                    {
                        binding.tvNoData.visibility = View.VISIBLE
                        binding.rvInventoryBalance.visibility = View.GONE
                    }
                        else
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())

                }

            }
        }
    }

    private fun setAdapter(data1: ArrayList<VegaGhanaInventoryReportResponse>, data: List<ReportsList>) {

        if (data.isNotEmpty())

            binding.rvInventoryBalance.layoutManager =
                LinearLayoutManager(activity).apply { LinearLayoutManager.VERTICAL }
        binding.rvInventoryBalance.adapter = VegaGhanaInventoryBalanceParentAdapter(data1, this)
    }


}
