package com.olam.warehouse.login.ui.transaction.cocoa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.FragmentVegaTransactionTypeBinding
import com.olam.warehouse.login.ui.transaction.TransactionViewModel
import com.olam.warehouse.master.vega.model.VegaCocoaNoWeighmentWithLots
import com.olam.warehouse.master.vegacocoa.model.VegaCoCoaReceivingMtnrWithLots
import com.olam.warehouse.navigation.features.VegaCocoaMTNRNavigation
import com.olam.warehouse.navigation.features.VegaCocoaMtntNavigation
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.TRANS_MTNR
import com.olam.warehouse.presentation.utils.UIUtils.TRANS_MTNT
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import org.koin.androidx.viewmodel.ext.android.viewModel


class VegaCocoaTransactionTypeFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_transaction_type
    private lateinit var binding: FragmentVegaTransactionTypeBinding
    private val vm: TransactionViewModel by viewModel()

    companion object {
        fun newInstance() = VegaCocoaTransactionTypeFragment().putArgs {
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
        binding.tvGrn.text = getString(R.string.trans_mtnt)
        binding.tvInvoice.text = getString(R.string.trans_mtnr)
        binding.clGrn.setOnClickListener {
            VegaCocoaMtntNavigation.dynamicStart?.let { intent ->
                intent.putExtra(TRANS_MTNT, true)
                startActivity(intent)
            }
        }
        binding.clInvoice.setOnClickListener {
            VegaCocoaMTNRNavigation.dynamicStart?.let { intent ->
                intent.putExtra(TRANS_MTNR, true)
                startActivity(intent)
            }
        }

        vm.offlinePendingMtnr.observe(viewLifecycleOwner, Observer {
            updatePendingUI(it)
        })
        vm.getPendingList()
        vm.getPendingListWithLot()
        vm.dispatchPendingWithLot.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun updatePendingUI(data: List<VegaCoCoaReceivingMtnrWithLots>) {
        if (data.size > 0) binding.tvInvoiceSycData.visible() else binding.tvInvoiceSycData.gone()
    }


    private fun updateUI(data: List<VegaCocoaNoWeighmentWithLots>) {
        if (data.size > 0) binding.tvGrnSycData.visible() else binding.tvGrnSycData.gone()
    }


}
