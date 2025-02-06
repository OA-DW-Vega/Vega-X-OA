package com.olam.warehouse.login.ui.transaction.indocoffee

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.FragmentVegaTransactionTypeBinding
import com.olam.warehouse.login.ui.transaction.TransactionViewModel
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchWithLineItems
import com.olam.warehouse.master.vegaindocoffee.entity.VegaIndoCoffeeExportSalesOrder
import com.olam.warehouse.navigation.features.*
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 4/20/2021.
 */
class VegaIndoTransactionTypeFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_transaction_type
    private lateinit var binding: FragmentVegaTransactionTypeBinding
    private val vm: TransactionViewModel by viewModel()

    companion object {
        fun newInstance() = VegaIndoTransactionTypeFragment().putArgs {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVegaTransactionTypeBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding.tvOffloading.text = getString(R.string.offloading)
        binding.tvQuality.text = getString(R.string.quality)
        binding.tvMtnt.text = getString(R.string.mtnt)
        binding.tvGrn.text = getString(R.string.trans_grn)
        binding.tvExportSales.text = getString(R.string.export_sales)
        binding.clOffloading.visible()
        binding.clQuality.visible()
        binding.clMtnt.visible()
        binding.clGrn.visible()
        binding.clExportSales.visible()
        binding.clOffloading.setOnClickListener {
            VegaIndoCoffeeOffloadingNavigation.dynamicStart?.let { intent ->
                intent.putExtra(UIUtils.TRANS_OFFLOADING, true)
                startActivity(intent)
            }
        }

        binding.clQuality.setOnClickListener {
            VegaIndoCoffeeQualityNavigation.dynamicStart?.let { intent ->
                intent.putExtra(UIUtils.TRANS_QUALITY, true)
                startActivity(intent)
            }
        }

        binding.clGrn.setOnClickListener {
            VegaIndoCoffeeGrnNavigation.dynamicStart?.let { intent ->
                intent.putExtra(UIUtils.TRANS_GRN, true)
                startActivity(intent)
            }
        }

        binding.clMtnt.setOnClickListener {
            VegaIndoCoffeeMtntNavigation.dynamicStart?.let { intent ->
                intent.putExtra(UIUtils.TRANS_MTNT, true)
                startActivity(intent)
            }
        }
        binding.clExportSales.setOnClickListener {
            VegaIndoCoffeeSalesCoffeeNavigation.dynamicStart?.let { intent ->
                intent.putExtra(UIUtils.TRANS_SALES, true)
                startActivity(intent)
            }
        }
        vm.offlodingList.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getOffloadingItem()

        vm.qualityList.observe(viewLifecycleOwner, Observer { updateQualityUI(it) })
        vm.getQualityItem()

        vm.weighBridgeOfflineCount.observe(viewLifecycleOwner, Observer { enableGrnBlock(it) })
        vm.getOfflineWeighBridgeDetailCount()

        vm.dispatchItemCountLocal.observe(viewLifecycleOwner, Observer { updateMtntUI(it) })
        vm.getDispatchWithLineItemCount()

        vm.exportSalesLocal.observe(viewLifecycleOwner, Observer { updateExportSalesUI(it) })
        vm.getIndoExportSalesItem()

    }

    private fun updateExportSalesUI(data: List<VegaIndoCoffeeExportSalesOrder>?) {
        if (data?.isNotEmpty() == true) binding.tvExportSalesPendSync.visible() else binding.tvExportSalesPendSync.gone()
    }

    private fun updateMtntUI(data: List<VegaEcuadorDispatchWithLineItems>?) {
        if (data?.isNotEmpty() == true) binding.tvMtntSycData.visible() else binding.tvMtntSycData.gone()
    }

    private fun enableGrnBlock(data: List<VegaGrnWeighBridgeId>?) {
        if (data?.isNotEmpty() == true) binding.tvGrnSycData.visible() else binding.tvGrnSycData.gone()
    }

    private fun updateQualityUI(data: List<VegaQualityWBDetails>?) {
        if (data?.isNotEmpty() == true) binding.tvQualityPendSync.visible() else binding.tvQualityPendSync.gone()
    }

    private fun updateUI(data: List<VegaCoffeeReceiving>) {
        if (data.isNotEmpty()) binding.tvOffloadingPendSync.visible() else binding.tvOffloadingPendSync.gone()
    }

}
