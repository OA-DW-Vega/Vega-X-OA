package com.olam.warehouse.login.ui.transaction.nicaragua

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.FragmentVegaTransactionTypeBinding
import com.olam.warehouse.login.ui.transaction.TransactionViewModel
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceTransactionDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPODetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaInvoiceDetails
import com.olam.warehouse.master.veganicaragua.model.VegaMtntWithLotsWithBags
import com.olam.warehouse.navigation.features.*
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.TRANS_ADVANCE_CREATION
import com.olam.warehouse.presentation.utils.UIUtils.TRANS_FORWARD_PO
import com.olam.warehouse.presentation.utils.UIUtils.TRANS_GRN
import com.olam.warehouse.presentation.utils.UIUtils.TRANS_INVOICE
import com.olam.warehouse.presentation.utils.UIUtils.TRANS_MTNT
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 9/28/2020.
 */
class VegaNicaraguaTransactionTypeFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_transaction_type
    private lateinit var binding: FragmentVegaTransactionTypeBinding
    private val vm: TransactionViewModel by viewModel()

    companion object {
        fun newInstance() = VegaNicaraguaTransactionTypeFragment().putArgs {
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
        binding.tvGrn.text = getString(R.string.trans_grn)
        binding.tvInvoice.text = getString(R.string.trans_invoice)
        binding.tvAdvanceCreation.text=getString(R.string.trans_advance)
        binding.tvForwordPO.text=getString(R.string.trans_forward_po)
        binding.tvMtnt.text=getString(R.string.trans_mtnt)
        binding.clGrn.setOnClickListener {
            VegaNicaraguaGrnNavigation.dynamicStart?.let { intent ->
                intent.putExtra(TRANS_GRN, true)
                startActivity(intent)
            }
        }
        binding.clInvoice.setOnClickListener {
            VegaNicaraguaInvoiceNavigation.dynamicStart?.let { intent ->
                intent.putExtra(TRANS_INVOICE, true)
                startActivity(intent)
            }
        }
        binding.clGrn.visibility = View.VISIBLE
        binding.clInvoice.visibility = View.VISIBLE
        binding.clMtnt.visibility = View.VISIBLE
        binding.clForwordPo.visibility = View.VISIBLE
        binding.clAdvance.visibility = View.VISIBLE
        binding.clForwordPo.setOnClickListener {
            VegaNicaraguaForwordPONavigation.dynamicStart?.let { intent ->
                intent.putExtra(TRANS_FORWARD_PO, true)
                startActivity(intent)
            }
        }
        binding.clAdvance.setOnClickListener {
            VegaNicaraguaAdvanceNavigation.dynamicStart?.let { intent ->
                intent.putExtra(TRANS_ADVANCE_CREATION, true)
                startActivity(intent)
            }
        }

        binding.clMtnt.setOnClickListener {
            VegaNicaraguaMtntNavigation.dynamicStart?.let { intent ->
                intent.putExtra(TRANS_MTNT, true)
                startActivity(intent)
            }
        }
        vm.grnTransList.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getGrnItem()
        vm.invoiceOffline.observe(viewLifecycleOwner, Observer { updateInvoiceUI(it) })
        vm.getInvoiceOfflineData()
        vm.forwardPoOffline.observe(viewLifecycleOwner, Observer { updateForwardPoUI(it) })
        vm.getForwardPOOfflineData()

        vm.advanceTransactionOffline.observe(viewLifecycleOwner, Observer { updateAdvancingUI(it) })
        vm.getAdvanceTransactionOfflineData()

        vm.listWBLotsWithBags.observe(viewLifecycleOwner, Observer {
            updateMtntUI(it)
        })
        vm.getListOfMtntWithLots()
    }

    private fun updateInvoiceUI(data: List<VegaNicaraguaInvoiceDetails>) {
        if (data.size > 0) binding.tvInvoiceSycData.visible() else binding.tvInvoiceSycData.gone()
    }

    private fun updateUI(data: List<VegaReceiving>) {
        if (data.size > 0) binding.tvGrnSycData.visible() else binding.tvGrnSycData.gone()
    }

    private fun updateForwardPoUI(data: List<VegaNicaraguaForwardPODetails>) {
        if (data.size > 0) binding.tvForwordPOSycData.visible() else binding.tvForwordPOSycData.gone()
    }

    private fun updateAdvancingUI(data: List<VegaNicaraguaAdvanceTransactionDetails>) {
        if (data.size > 0) binding.tvAdvanceSycData.visible() else binding.tvAdvanceSycData.gone()
    }

    private fun updateMtntUI(it: List<VegaMtntWithLotsWithBags>) {
        val data = it.filter { it.mtnt.isOfflineData == true }
        if (data.size > 0) binding.tvMtntSycData.visible() else binding.tvMtntSycData.gone()
    }

}
