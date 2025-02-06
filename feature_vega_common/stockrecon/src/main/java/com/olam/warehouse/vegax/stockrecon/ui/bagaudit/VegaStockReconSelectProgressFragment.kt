package com.olam.warehouse.vegax.stockrecon.ui.bagaudit

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.stockrecon.R
import com.olam.warehouse.vegax.stockrecon.databinding.FragmentStockProcessTypeSelectLayoutBinding
import com.olam.warehouse.vegax.stockrecon.ui.callback.VegaStockCallbackListener
import com.olam.warehouse.vegax.stockrecon.utils.RECON_REPORT_DATE_PLANT_SELECTION
import com.olam.warehouse.vegax.stockrecon.utils.STOCK_RECON_AUDIT_TYPE_SELECT
import com.olam.warehouse.vegax.stockrecon.vm.VegaStockReconViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaStockReconSelectProgressFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_stock_process_type_select_layout
    private lateinit var binding: FragmentStockProcessTypeSelectLayoutBinding
    private var callBack: VegaStockCallbackListener? = null
    private val vm: VegaStockReconViewModel by viewModel()

    companion object {
        fun newInstance() = VegaStockReconSelectProgressFragment().putArgs { }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaStockCallbackListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStockProcessTypeSelectLayoutBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        clickListener()
    }

    private fun clickListener() {
        binding.llStockAudit.setOnClickListener { moveToStockAuditPage() }
        binding.llReconReport.setOnClickListener { moveToReconReportDatePlantSelectionPage() }
    }

    private fun moveToStockAuditPage() {
        callBack?.replaceFragment(STOCK_RECON_AUDIT_TYPE_SELECT, "")
    }

    private fun moveToReconReportDatePlantSelectionPage() {
        callBack?.replaceFragment(RECON_REPORT_DATE_PLANT_SELECTION, "")
    }
}
