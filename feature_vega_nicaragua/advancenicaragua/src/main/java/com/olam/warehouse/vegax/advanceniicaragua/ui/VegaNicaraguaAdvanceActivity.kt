package com.olam.warehouse.vegax.advanceniicaragua.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.ui.transaction.VegaTransactionFragment
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.GrnDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaReconcilCashMovement
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import  com.olam.warehouse.vegax.advancenicaragua.R
import com.olam.warehouse.vegax.advanceniicaragua.di.injectNicaraguaInvoiceFeature
import com.olam.warehouse.vegax.advanceniicaragua.ui.transaction.VegaNicaraguaAdvanceTransactionFragment
import com.olam.warehouse.vegax.advanceniicaragua.utils.ADVANCE_DETAILS_FRAG
import com.olam.warehouse.vegax.advanceniicaragua.utils.ADVANCE_SUMMARY_FRAG

class VegaNicaraguaAdvanceActivity : HomeBaseActivity(), Callback {

    private val mTAG = VegaNicaraguaAdvanceActivity::class.java.canonicalName


    override val layoutResourceId: Int = R.layout.activity_advance_layout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNicaraguaInvoiceFeature()
        initNavigationView()
        initUI()
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flNicaraguaAdvance, allowBackStack = flag)
    }

    private fun initUI() {

        if (intent.hasExtra(UIUtils.TRANS_ADVANCE_CREATION))
            displayFragment(VegaNicaraguaAdvanceTransactionFragment(), false)
        else
            displayFragment(VegaNicaraguaAdvanceVendorsFragment.newInstance(), false)
    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
        }
    }

    override fun replaceFragment(moveFrag: String, data: Bundle) {
        when (moveFrag) {

            ADVANCE_DETAILS_FRAG -> displayFragment(VegaNicaraguaAdvanceDetailsFragment.newInstance(data), true)
            ADVANCE_SUMMARY_FRAG -> displayFragment(VegaNicaraguaAdvanceSummaryFragment.newInstance(data), true)
        }

    }


}
