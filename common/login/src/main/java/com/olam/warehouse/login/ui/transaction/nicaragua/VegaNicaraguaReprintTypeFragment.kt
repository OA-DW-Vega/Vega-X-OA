package com.olam.warehouse.login.ui.transaction.nicaragua

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.FragmentVegaTransactionTypeBinding
import com.olam.warehouse.login.ui.transaction.TransactionViewModel
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.navigation.SplitInstall
import com.olam.warehouse.navigation.features.*
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_CERTIFICATE_PREMIUM
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_FORWARDPO
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_GRN
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_INVOICE
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_MTNR
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_MTNT
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_SAMPLE_TICKET
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_TICKET
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_WITH_HOLD_TAX
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.observeOnce
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
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
    private var isMtnt = false
    private var isMtnr = false
    private var isGrn = false
    private var isInvoice = false
    private var isAdvance = false

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
        //plantList.add("1550")
        val roleData = Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
            .filter { key -> key.roleKey.equals(PreferenceHelper.get(Constants.CURRENT_KEY, "")) }
        roleData.forEach { rol ->
            when (UserRoles.valueOfEnum(rol.roleName.trim())) {
                UserRoles.MTNT -> isMtnt = true
                UserRoles.OFFLOADING -> isMtnr = true
                UserRoles.GRN -> isGrn = true
                UserRoles.ADVANCING -> isAdvance = true
                UserRoles.INVOICE -> isInvoice = true
                else -> {}
            }
        }
        if(isMtnt) binding.clMtnt.visible() else binding.clMtnt.gone()
        if(isMtnr) binding.mtnrflowlayout.visible() else binding.mtnrflowlayout.gone()
        if(isGrn) binding.clGrn.visible() else binding.clGrn.gone()
//        if(isAdvance) binding.clAdvance.visible() else binding.clAdvance.gone()
        if(isInvoice) {
            binding.clInvoice.visible()
            binding.cICertificatePremiumReceiptLayout.visible()
            binding.cIWithHoldTaxReceiptLayout.visible()
            binding.cIWithForwardPoReceiptLayout.visible()
        } else {
            binding.clInvoice.gone()
            binding.cICertificatePremiumReceiptLayout.gone()
            binding.cIWithHoldTaxReceiptLayout.gone()
            binding.cIWithForwardPoReceiptLayout.gone()
        }

       /* if (getCurrentKey().split("_")[1].contains("NI")) {
            vm.configItems.observeOnce(viewLifecycleOwner, Observer { updateConfigItems(it) })
            vm.getConfigItems(UserRoles.PROCESSING.role)
        } else {*/
            binding.tvGrn.text = getString(R.string.trans_grn)
            binding.tvInvoice.text = getString(R.string.trans_invoice)
            binding.tvMtnt.text = getString(R.string.mtnt)
            binding.tvAdvanceCreation.text = getString(R.string.advancing)
            binding.tvCertificatePremiumReceiept.text =
                getString(R.string.certificate_premium_reciept)
            binding.tvMtnrReceipt.text = getString(R.string.tally_sheet_txt)
//            binding.cICertificatePremiumReceiptLayout.visibility = View.VISIBLE
//            binding.cIWithHoldTaxReceiptLayout.visibility = View.VISIBLE
//            binding.cIWithForwardPoReceiptLayout.visibility = View.VISIBLE
            binding.tvWithHoldTax.text = getString(R.string.with_holding_tax)
            binding.tvWithForwardPo.text = getString(R.string.trans_forward_po)
            binding.tvTallyReceipt.text = getString(R.string.tally_sheet_txt)
            binding.tvTicketReceipt.text = getString(R.string.ticket)
            binding.tvTicketSampleReceipt.text = getString(R.string.ticket_sample_txt)
       // }
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
        binding.clSampleTicket.setOnClickListener {
            VegaNicaraguaTicketNavigation.dynamicStart?.let { intent ->
                intent.putExtra(REPRINT_SAMPLE_TICKET, true)
                startActivity(intent)
            }
        }
        binding.clMtnrReceipt.setOnClickListener{
            //binding.normalFlowlayout.gone()
            binding.mtnrflowlayout.visible()
            binding.tvTallyReceipt.text = getString(R.string.tally_sheet_txt)
            binding.tvTicketReceipt.text = getString(R.string.ticket)
            binding.tvTicketSampleReceipt.text = getString(R.string.ticket_sample_txt)
        }
        binding.clTallyReceipt.setOnClickListener {
              VegaNicaraguaMtnrReprintNavigation.dynamicStart?.let { intent ->
                  intent.putExtra(REPRINT_MTNR, true)
                  startActivity(intent)
              }
        }
        /*vm.grnTransList.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getGrnItem()
        vm.invoiceOffline.observe(viewLifecycleOwner, Observer { updateInvoiceUI(it) })
        vm.getInvoiceOfflineData()*/
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>) {
        val dryingPlants = configItems.filter { it.process.equals(ConfigItems.DRYING_PLANT.item)
                || it.process.equals(ConfigItems.MILLING_PLANT.item)}
        plantList.clear()
        dryingPlants.forEach {
           // var plantLists = it.value
            plantList.add(it.plant.toString())
        }
        //segregating plants based on unit and applying changes
        //reprint Ticket module required for drying unit only
          if (plantList.contains(plantId)) {
              binding.clInvoice.visibility = View.GONE
              binding.clGrn.visibility = View.GONE
              binding.clMtnt.visibility = View.VISIBLE
              binding.cICertificatePremiumReceiptLayout.visibility = View.GONE
              binding.cIWithHoldTaxReceiptLayout.visibility = View.GONE
              binding.cIWithForwardPoReceiptLayout.visibility = View.GONE
              binding.clMtnrReceipt.visible()
              binding.tvMtnrReceipt.text = getString(R.string.tally_sheet_txt)
              binding.tvMtnt.text = getString(R.string.mtnt)

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
              binding.clMtnrReceipt.gone()
          }


    }

    /*private fun updateInvoiceUI(data: List<VegaNicaraguaInvoiceDetails>) {
        if (data.size > 0) binding.tvInvoiceSycData.visible() else binding.tvInvoiceSycData.gone()
    }

    private fun updateUI(data: List<VegaReceiving>) {
        if (data.size > 0) binding.tvGrnSycData.visible() else binding.tvGrnSycData.gone()
    }*/

}
