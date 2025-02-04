package com.olam.warehouse.vegax.forwardponicaragua.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.forwardponicaragua.R
import com.olam.warehouse.vegax.forwardponicaragua.di.injectNicaraguaInvoiceFeature
import com.olam.warehouse.vegax.forwardponicaragua.ui.reprint.VegaNicaraguaForwardPoReprintFragment
import com.olam.warehouse.vegax.forwardponicaragua.ui.transaction.VegaNicaraguaForwardPoTransactionFragment
import com.olam.warehouse.vegax.grnecuador.utils.FORWARD_PO_CREATION_PREVIEW_FRAG
import com.olam.warehouse.vegax.grnecuador.utils.FORWARD_PO_CREATION_PRICING_DETAILS_FRAG
import com.olam.warehouse.vegax.grnecuador.utils.FORWARD_PO_CREATION_TRANSACTION_DETAILS_FRAG
import com.olam.warehouse.vegax.grnecuador.utils.FORWARD_PO_CREATION_YIELD_DETAILS_FRAG


/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */
class VegaNicaraguaForwardPOActivity : HomeBaseActivity(),
    Callback{

    private val mTAG = VegaNicaraguaForwardPOActivity::class.java.canonicalName


    override val layoutResourceId: Int
        get() = R.layout.activity_vega_nicaragua_forward_po


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNicaraguaInvoiceFeature()
        initNavigationView()
        initUI()
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flNicaraguaForwordPo, allowBackStack = flag)
    }

    private fun initUI() {
        if (intent.hasExtra(UIUtils.TRANS_FORWARD_PO))
            displayFragment(VegaNicaraguaForwardPoTransactionFragment.newInstance(), false)
        else if (intent.hasExtra(UIUtils.REPRINT_FORWARDPO))
            displayFragment(VegaNicaraguaForwardPoReprintFragment.newInstance(), false)
        else
            displayFragment(VegaNicaraguaForwardPOVendorsFragment.newInstance(), false)

    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
        }
    }

    override fun replaceFragment(moveFrag: String, data: Bundle) {
        when (moveFrag) {
            FORWARD_PO_CREATION_TRANSACTION_DETAILS_FRAG -> displayFragment(
                VegaNicaraguaForwardPOTransactionDetailsFragment.newInstance(
                    data
                ), true)

            FORWARD_PO_CREATION_YIELD_DETAILS_FRAG -> displayFragment(
                VegaNicaraguaForwardPOYieldDetailsFragment.newInstance(
                    data
                ), true)

            FORWARD_PO_CREATION_PRICING_DETAILS_FRAG -> displayFragment(
                VegaNicaraguaForwardPOPricingFragment.newInstance(
                    data
                ), true)
            FORWARD_PO_CREATION_PREVIEW_FRAG-> displayFragment(
                VegaNicaraguaForwardPOSummaryFragment.newInstance(
                    data
                ), true)
        }

    }



}
