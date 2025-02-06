package com.olam.warehouse.vegax.stockrecon.ui.reconreport

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.stockrecon.R
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaReconReportBundleData
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaReconReportReconList
import com.olam.warehouse.vegax.stockrecon.databinding.FragmentReconReportStatisticsBinding
import com.olam.warehouse.vegax.stockrecon.ui.callback.VegaStockCallbackListener
import com.olam.warehouse.vegax.stockrecon.utils.BUNDLE_DATA
import com.olam.warehouse.vegax.stockrecon.utils.RECON_REPORT_RECON_LIST
import com.olam.warehouse.vegax.stockrecon.utils.SINGLE_SPACE
import com.olam.warehouse.vegax.stockrecon.vm.VegaStockReconViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaReconReportStatisticsFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_recon_report_statistics
    private lateinit var binding: FragmentReconReportStatisticsBinding
    private var callBack: VegaStockCallbackListener? = null
    private val vm: VegaStockReconViewModel by viewModel()
    var bundleData = VegaReconReportBundleData()
    var allDateList = mutableListOf<String>()


    companion object {
        fun newInstance(bundleData: VegaReconReportBundleData) = VegaReconReportStatisticsFragment().putArgs {
            putParcelable(BUNDLE_DATA, bundleData)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaStockCallbackListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentReconReportStatisticsBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        initExtra()
        observer()
        clickListener()
    }

    private fun initExtra() {
        bundleData = arguments?.getParcelable<VegaReconReportBundleData>(BUNDLE_DATA) as VegaReconReportBundleData
        /* allDateList = bundleData.plantAndDate?.toDate?.let {
             bundleData.plantAndDate?.fromDate?.let { it1 ->
                 DateUtils.getAllDatesBwTwoDates(
                     it1,
                     it
                 )
             }
         }!!*/

    }

    private fun observer() {
        /*The below observer is to fetch all recon list from api*/
        bundleData?.plantAndDate?.fromDate?.let { fromDate ->
            bundleData?.plantAndDate?.toDate?.let { toDate ->
                vm.getReconReportReconList(
                    bundleData?.plantAndDate?.plant?.split("-")?.get(0) ?: "",
                    fromDate, toDate
                )
            }
        }
        vm.reconReportReconList.observe(viewLifecycleOwner, Observer { updateReconReportData(it) })
    }

    private fun clickListener() {
        binding.clRoot.setOnClickListener { moveToReconListPage() }
    }

    private fun updateReconReportData(response: Resource<GenericReqAndResp<VegaReconReportReconList>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { data ->
                        updateUI(data)
                    }
                }

                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun updateUI(reconData: VegaReconReportReconList) {
        if (reconData.stockReconList.size == 0) {
            binding.tvEmpty.visible()
            binding.clRoot.gone()
        } else {
            bundleData.reconListDetails = reconData
            binding.tvTotalReconValue.setText(reconData.totalNoOfRecons)
            binding.tvReconDaysValue.setText("For last ${bundleData.selectedDaysCount} days")
            binding.tvTotalAuditValue.setText(reconData.totalNoOfLots)
            binding.tvAuditDaysValue.setText("For last ${bundleData.selectedDaysCount} days")
            binding.tvTotalAuditedWeightValue.setText(
                reconData.totalAuditWeight.plus(SINGLE_SPACE).plus(reconData.unitOfMeasure)
            )
            binding.tvTotalAuditedWeightDaysValue.setText("For last ${bundleData.selectedDaysCount} days")
            binding.tvTotalWeightLossValue.setText(
                reconData.totalGainLoss.plus(SINGLE_SPACE).plus(reconData.unitOfMeasure)
            )
            binding.tvTotalWeightLossDaysValue.setText("For last ${bundleData.selectedDaysCount} days")
            /*the date list which is taken from stock recon list, we are setting only available dates in stock recon list*/
            allDateList =
                bundleData.reconListDetails?.stockReconList?.map { it.createdAt }?.distinct()?.reversed() as MutableList<String>
            loadBarChart()
        }
    }

    private fun moveToReconListPage() {
        callBack?.replaceFragment(RECON_REPORT_RECON_LIST, bundleData)
    }

    /*The below method is to show bar chart*/
    private fun loadBarChart() {
        /*This bar chart contains 2 bars, System weight & Audit weight*/
        var barset1 = BarDataSet(barSet1(), "System Weight")
        /*Setting colors for the bars*/
        barset1.setColor(resources.getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        var barset2 = BarDataSet(barSet2(), "Audit Weight")
        barset2.setColor(resources.getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi1))
        var data = BarData(barset1, barset2)
        /*adding both bar data into barchart view*/
        binding.barChart.data = data
        /*enable/disable the bar chart description*/
        binding.barChart.description.isEnabled = false
        var xAxis = binding.barChart.xAxis
        /*setting date details in x axis of the bar chart*/
        xAxis.valueFormatter = IndexAxisValueFormatter(allDateList)
        xAxis.setCenterAxisLabels(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setGranularity(1f)
        xAxis.setGranularityEnabled(true)
        binding.barChart.setDragEnabled(true)
        binding.barChart.setVisibleXRangeMaximum(3f)
        val barSpace = 0.1f
        val groupSpace = 0.5f
        data.barWidth = 0.15f
        binding.barChart.xAxis.axisMinimum = 0f
        binding.barChart.animate()
        binding.barChart.groupBars(0f, groupSpace, barSpace)
        binding.barChart.invalidate()
        binding.barChart.getXAxis().setDrawGridLines(false)
    }


    /*The below methods is to collect bar1 data*/
    private fun barSet1(): ArrayList<BarEntry>? {
        var count: Float = 0F
        var barEntry = ArrayList<BarEntry>()
        /*we are collecting&sum all system weight from reconList in date wise*/
        for (item in allDateList) {
            ++count
            var list = bundleData.reconListDetails?.stockReconList?.filter { it.createdAt.equals(item) }
            if (list?.isNotEmpty() == true) {
                /*in case, if we have multiple recons in same date,
                we have sum all of system weight in that date and load into barchart*/
                var sumValue = list.sumOf { it.totalSystemWeight?.toDouble() ?: 0.0 }
                barEntry.add(BarEntry(count, sumValue.toFloat()))
            }
        }
        return barEntry
    }

    /*The below methods is to collect bar2 data*/
    private fun barSet2(): ArrayList<BarEntry>? {
        var count: Float = 0F
        var barEntry = ArrayList<BarEntry>()
        /*we are collecting&sum all audit weight from reconList in date wise*/
        for (item in allDateList) {
            ++count
            var list = bundleData.reconListDetails?.stockReconList?.filter { it.createdAt.equals(item) }
            /*in case, if we have multiple recons in same date,
                we have sum all of system weight in that date and load into barchart*/
            if (list?.isNotEmpty() == true) {
                var sumValue = list.sumOf { it.totalAuditWeight?.toDouble() ?: 0.0 }
                barEntry.add(BarEntry(count, sumValue.toFloat()))
            }
        }
        return barEntry
    }
}
