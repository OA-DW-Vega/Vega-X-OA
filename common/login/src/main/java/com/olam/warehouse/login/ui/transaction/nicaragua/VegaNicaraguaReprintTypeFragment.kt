package com.olam.warehouse.login.ui.transaction.nicaragua

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.FragmentVegaTransactionTypeBinding
import com.olam.warehouse.login.ui.transaction.TransactionViewModel
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.navigation.features.*
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_CERTIFICATE_PREMIUM
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_FORWARDPO
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_GRN
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_INVOICE
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_MTNT
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_TICKET
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_WITH_HOLD_TAX
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 9/28/2020.
 */
class VegaNicaraguaReprintTypeFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_transaction_type
    private lateinit var binding: FragmentVegaTransactionTypeBinding
    private val vm: TransactionViewModel by viewModel()
    private val plantId = getPlantDetails().plantId
    private var plantList = ArrayList<String>()

    companion object {
        fun newInstance() = VegaNicaraguaReprintTypeFragment().putArgs {
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
        //default plant id for drying unit
        plantList.add("1550")
        if (getCurrentKey().split("_")[1].contains("NI")) {
            vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
            vm.getConfigItems(UserRoles.PROCESSING.role)
        } else {
            binding.tvGrn.text = getString(R.string.trans_grn)
            binding.tvInvoice.text = getString(R.string.trans_invoice)
            binding.tvMtnt.text = getString(R.string.mtnt)
            binding.tvCertificatePremiumReceiept.text =
                getString(R.string.certificate_premium_reciept)
            binding.cICertificatePremiumReceiptLayout.visibility = View.VISIBLE
            binding.cIWithHoldTaxReceiptLayout.visibility = View.VISIBLE
            binding.cIWithForwardPoReceiptLayout.visibility = View.VISIBLE
            binding.tvWithHoldTax.text = getString(R.string.with_holding_tax)
            binding.tvWithForwardPo.text = getString(R.string.trans_forward_po)
        }
        binding.tvTransactionHeading.text = getString(R.string.select_reprint)
        binding.clGrn.setOnClickListener {
            VegaNicaraguaGrnNavigation.dynamicStart?.let { intent ->
                intent.putExtra(REPRINT_GRN, true)
                startActivity(intent)
            }
        }
        binding.clInvoice.setOnClickListener {
            VegaNicaraguaInvoiceNavigation.dynamicStart?.let { intent ->
                intent.putExtra(REPRINT_INVOICE, true)
                startActivity(intent)
            }
        }
        binding.cIWithForwardPoReceiptLayout.setOnClickListener {
            VegaNicaraguaForwordPONavigation.dynamicStart?.let { intent ->
                intent.putExtra(REPRINT_FORWARDPO, true)
                startActivity(intent)
            }
        }
        binding.clMtnt.setOnClickListener {
            VegaNicaraguaMtntNavigation.dynamicStart?.let { intent ->
                intent.putExtra(REPRINT_MTNT, true)
                startActivity(intent)
            }
        }
        binding.cICertificatePremiumReceiptLayout.setOnClickListener {
            VegaNicaraguaInvoiceNavigation.dynamicStart?.let { intent ->
                intent.putExtra(REPRINT_CERTIFICATE_PREMIUM, true)
                startActivity(intent)
            }
        }
        binding.cIWithHoldTaxReceiptLayout.setOnClickListener {
            VegaNicaraguaInvoiceNavigation.dynamicStart?.let { intent ->
                intent.putExtra(REPRINT_WITH_HOLD_TAX, true)
                startActivity(intent)
            }
        }
        binding.clTicketReceipt.setOnClickListener {
            VegaNicaraguaTicketNavigation.dynamicStart?.let { intent ->
                intent.putExtra(REPRINT_TICKET, true)
                startActivity(intent)
            }
        }
        /*vm.grnTransList.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getGrnItem()
        vm.invoiceOffline.observe(viewLifecycleOwner, Observer { updateInvoiceUI(it) })
        vm.getInvoiceOfflineData()*/
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>) {
        val dryingPlants = configItems.filter { it.process.equals(ConfigItems.DRYING_PLANT.item) }
        plantList.clear()
        dryingPlants.forEach {
            var plantLists = it.value
            plantList = plantLists?.split(",") as ArrayList<String>
        }
        //segregating plants based on unit and applying changes
        //reprint Ticket module required for drying unit only
        if (plantList.contains(plantId)) {
            binding.clInvoice.visibility = View.GONE
            binding.clGrn.visibility = View.GONE
            binding.clMtnt.visibility = View.GONE
            binding.cICertificatePremiumReceiptLayout.visibility = View.GONE
            binding.cIWithHoldTaxReceiptLayout.visibility = View.GONE
            binding.cIWithForwardPoReceiptLayout.visibility = View.GONE
            binding.clTicketReceipt.visible()
            binding.tvTicketReceipt.text = getString(R.string.ticket)

        } else {

            binding.tvGrn.text = getString(R.string.trans_grn)
            binding.tvInvoice.text = getString(R.string.trans_invoice)
            binding.tvMtnt.text = getString(R.string.mtnt)
            binding.tvCertificatePremiumReceiept.text =
                getString(R.string.certificate_premium_reciept)
            binding.cICertificatePremiumReceiptLayout.visibility = View.VISIBLE
            binding.cIWithHoldTaxReceiptLayout.visibility = View.VISIBLE
            binding.cIWithForwardPoReceiptLayout.visibility = View.VISIBLE
            binding.tvWithHoldTax.text = getString(R.string.with_holding_tax)
            binding.tvWithForwardPo.text = getString(R.string.trans_forward_po)
        }
    }

    /*private fun updateInvoiceUI(data: List<VegaNicaraguaInvoiceDetails>) {
        if (data.size > 0) binding.tvInvoiceSycData.visible() else binding.tvInvoiceSycData.gone()
    }

    private fun updateUI(data: List<VegaReceiving>) {
        if (data.size > 0) binding.tvGrnSycData.visible() else binding.tvGrnSycData.gone()
    }*/

}
