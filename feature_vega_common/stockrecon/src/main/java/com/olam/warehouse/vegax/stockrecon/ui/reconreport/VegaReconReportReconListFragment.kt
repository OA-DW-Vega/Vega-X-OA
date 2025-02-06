package com.olam.warehouse.vegax.stockrecon.ui.reconreport

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.model.VegaStockReconGetAllAuditData
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.stockrecon.R
import com.olam.warehouse.vegax.stockrecon.data.domian.model.StockReconList
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaReconReportBundleData
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaReconReportReconList
import com.olam.warehouse.vegax.stockrecon.databinding.FragmentReconReportReconListBinding
import com.olam.warehouse.vegax.stockrecon.databinding.FragmentReconReportStatisticsBinding
import com.olam.warehouse.vegax.stockrecon.databinding.ItemAuditListBinding
import com.olam.warehouse.vegax.stockrecon.databinding.ItemReconReportReconListBinding
import com.olam.warehouse.vegax.stockrecon.ui.callback.VegaStockCallbackListener
import com.olam.warehouse.vegax.stockrecon.utils.BUNDLE_DATA
import com.olam.warehouse.vegax.stockrecon.utils.RECON_REPORT_AUDIT_LIST
import com.olam.warehouse.vegax.stockrecon.utils.RECON_REPORT_RECON_LIST
import com.olam.warehouse.vegax.stockrecon.utils.SINGLE_SPACE
import com.olam.warehouse.vegax.stockrecon.utils.getColor
import com.olam.warehouse.vegax.stockrecon.utils.getMaterialNameFromMaterialList
import com.olam.warehouse.vegax.stockrecon.vm.VegaStockReconViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaReconReportReconListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_recon_report_recon_list
    private lateinit var binding: FragmentReconReportReconListBinding
    private var callBack: VegaStockCallbackListener? = null
    private val vm: VegaStockReconViewModel by viewModel()
    var bundleData = VegaReconReportBundleData()
    private var filteredReconList = mutableListOf<StockReconList>()
    private val mSearchList = mutableListOf<StockReconList>()


    companion object {
        fun newInstance(bundleData: VegaReconReportBundleData) = VegaReconReportReconListFragment().putArgs {
            putParcelable(BUNDLE_DATA, bundleData)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaStockCallbackListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        setHasOptionsMenu(true)
        binding = FragmentReconReportReconListBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        initExtra()
        updateUI()
    }

    private fun initExtra() {
        bundleData = arguments?.getParcelable<VegaReconReportBundleData>(BUNDLE_DATA) as VegaReconReportBundleData
    }

    private fun updateUI() {
        binding.tvDateRangeValue.setText(bundleData.reconListDetails?.dateRange)
        binding.tvPlantValue.setText(bundleData.plantAndDate?.plant)
        binding.tvTotalReconValue.setText(bundleData.reconListDetails?.totalNoOfRecons)
        binding.tvNoOfLotsValue.setText(bundleData.reconListDetails?.totalNoOfLots)
        binding.tvTotalRecWeightValue.setText(bundleData.reconListDetails?.totalSystemWeight.plus(SINGLE_SPACE).plus(bundleData.reconListDetails?.unitOfMeasure))
        binding.tvTotalWeightLossValue.setText(bundleData.reconListDetails?.totalGainLoss.plus(SINGLE_SPACE).plus(bundleData.reconListDetails?.unitOfMeasure))
        filteredReconList.clear()
        filteredReconList.addAll(bundleData.reconListDetails?.stockReconList as MutableList<StockReconList>)
        setReconListAdapter(bundleData.reconListDetails?.stockReconList as MutableList<StockReconList>)
    }

    private fun setReconListAdapter(list: MutableList<StockReconList>) {
        binding.rvRecons.setUpAdapter(
            list,
            R.layout.item_recon_report_recon_list,
            ItemReconReportReconListBinding::inflate,
            { it, pos, bindingItem ->
                val item = list.get(pos)
                bindingItem.tvReconIdValue.setText(item.id)
                bindingItem.tvReconTypeValue.setText(item.reconType)
                bindingItem.tvTotalNoOfLotsValue.setText(item.totalNoOfLots)
                bindingItem.tvTotalWtLossValue.setText(item.totalGainLoss.plus(SINGLE_SPACE).plus(item.unitOfMeasure))
                bindingItem.tvTotalRecWtValue.setText(item.totalSystemWeight.plus(SINGLE_SPACE).plus(item.unitOfMeasure))
                bindingItem.tvRecDateValue.setText(item.createdAt)
            }, itemClick = {
                bundleData.selectedReconDetails = this
                moveToAuditListPage()
            })
    }

    private fun moveToAuditListPage() {
        callBack?.replaceFragment(RECON_REPORT_AUDIT_LIST, bundleData)
    }

    /*The below method is to, add search list in title bar*/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint = "Search by Recon"
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setReconListAdapter((if (filteredReconList.isEmpty()) bundleData.reconListDetails?.stockReconList else filteredReconList) as MutableList<StockReconList>)
                        } else {
                            mSearchList.clear()
                            (if (filteredReconList.isEmpty()) bundleData.reconListDetails?.stockReconList else filteredReconList)?.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.id?.contains(text) == true) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            setReconListAdapter(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

}
