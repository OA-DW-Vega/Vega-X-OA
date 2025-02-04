package com.olam.warehouse.vegax.invoicenicaragua.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.GrnDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaInvoiceDetails
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.TEMP_ID
import com.olam.warehouse.presentation.utils.UIUtils.VENDOR_DATA
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.grnecuador.utils.FRAG_SUMMARY
import com.olam.warehouse.vegax.grnecuador.utils.INVOICE_PRICE_CALCULATION_FRAG
import com.olam.warehouse.vegax.grnecuador.utils.INVOICE_SELECT_GRN_FRAG
import com.olam.warehouse.vegax.invoicenicaragua.R
import com.olam.warehouse.vegax.invoicenicaragua.di.injectNicaraguaInvoiceFeature
import com.olam.warehouse.vegax.invoicenicaragua.ui.reprint.VegaNicaraguaCertificatePremiumReprintFragment
import com.olam.warehouse.vegax.invoicenicaragua.ui.reprint.VegaNicaraguaInvoiceReprintFragment
import com.olam.warehouse.vegax.invoicenicaragua.ui.reprint.VegaNicaraguaWithHoldTaxReprintFragment
import com.olam.warehouse.vegax.invoicenicaragua.ui.transaction.VegaNicaraguaTransactionFragment

/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */
class VegaNicaraguaInvoiceActivity : HomeBaseActivity(), VegaNicaraguaInvoiceVendorsFragment.CallBack,
    VegaNicaraguaInvoiceGrnFragment.CallBack,
    VegaNicaraguaTransactionFragment.CallBack {

    private val mTAG = VegaNicaraguaInvoiceActivity::class.java.canonicalName


    override val layoutResourceId: Int
        get() = R.layout.activity_vega_nicaragua_invoice


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNicaraguaInvoiceFeature()
        initNavigationView()
        initUI()
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flNicaraguaInvoice, allowBackStack = flag)
    }

    private fun initUI() {
        PreferenceHelper.save(Constants.IS_EDIT_TRANS, false)
        //vm.clearBagDetails()
        if (intent.hasExtra(UIUtils.TRANS_INVOICE))
            displayFragment(VegaNicaraguaTransactionFragment.newInstance(), false)
        else if (intent.hasExtra(UIUtils.REPRINT_INVOICE))
            displayFragment(VegaNicaraguaInvoiceReprintFragment.newInstance(), false)
        else if (intent.hasExtra(UIUtils.REPRINT_CERTIFICATE_PREMIUM))
            displayFragment(VegaNicaraguaCertificatePremiumReprintFragment.newInstance(), false)
        else if (intent.hasExtra(UIUtils.REPRINT_WITH_HOLD_TAX))
            displayFragment(VegaNicaraguaWithHoldTaxReprintFragment.newInstance(), false)
        else
            displayFragment(VegaNicaraguaInvoiceVendorsFragment.newInstance(), false)
    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
        }
    }

    override fun replaceFragment(moveFrag: String, data: Bundle) {
        when (moveFrag) {
            INVOICE_SELECT_GRN_FRAG -> displayFragment(VegaNicaraguaInvoiceGrnFragment.newInstance(data), true)
            INVOICE_PRICE_CALCULATION_FRAG -> displayFragment(
                VegaNicaraguaInvoicePriceCalculationFragment.newInstance(
                    data
                ), true
            )

        }

    }

    override fun replaceFragment(moveFrag: String, receivingData: Any) {
        when (moveFrag) {
            FRAG_SUMMARY -> displayFragment(
                VegaNicaraguaInvoiceSummaryFragment.newInstance(receivingData as VegaNicaraguaInvoiceDetails),
                true
            )
            INVOICE_SELECT_GRN_FRAG -> {
                val grnData = receivingData as GrnDetails
                val data = Bundle().apply {
                    putParcelable(VENDOR_DATA, VegaVendor(vendorCode = grnData.supplierCode))
                    putParcelable(TEMP_ID, grnData)
                }
                displayFragment(VegaNicaraguaInvoiceGrnFragment.newInstance(data), true)
            }
        }
    }


}
