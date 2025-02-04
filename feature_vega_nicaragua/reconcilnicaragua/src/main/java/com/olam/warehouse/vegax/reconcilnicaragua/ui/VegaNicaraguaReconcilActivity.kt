package com.olam.warehouse.vegax.reconcilnicaragua.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaReconcilCashMovement
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.reconcilnicaragua.R
import com.olam.warehouse.vegax.reconcilnicaragua.data.domain.model.VegaNicaraguaReportModel
import com.olam.warehouse.vegax.reconcilnicaragua.di.injectNicaraguaReconcilReportFeature

class VegaNicaraguaReconcilActivity : HomeBaseActivity(), Callback {

    private val mTAG = VegaNicaraguaReconcilActivity::class.java.canonicalName


    override val layoutResourceId: Int = R.layout.activity_reconcil_layout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNicaraguaReconcilReportFeature()
        initNavigationView()
        initUI()
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flNicaraguaGrn,
            allowBackStack = flag
        )
    }

    private fun initUI() {
        displayFragment(
            VegaNicaraguaReconcilReportDocumentListFragment.newInstance(), false
        )
    }

    override fun replaceFragment(type: String, data: Any) {
        when (type) {
            "ticket" -> displayFragment(
                VegaNicaraguaReconcilTicketFragment.newInstance(
                    data as VegaNicaraguaReconcilCashMovement, ArrayList<VegaNicaraguaReportModel>()
                ), true
            )
            /*"print" ->*/
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun replaceFragment(type: String, data: Any, items: Any) {
        when (type) {
            "ticket" -> displayFragment(
                VegaNicaraguaReconcilTicketFragment.newInstance(
                    data as VegaNicaraguaReconcilCashMovement,
                    items as ArrayList<VegaNicaraguaReportModel>
                ), true
            )
        }
    }
}
