package com.olam.warehouse.vegax.grnsesame.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.grnsesame.R
import com.olam.warehouse.vegax.grnsesame.data.domain.usecase.model.VegaGRNSesameQuality
import com.olam.warehouse.vegax.grnsesame.di.injectNigeriaSesameGrnFeature
import com.olam.warehouse.vegax.grnsesame.ui.offline.VegaNigeriaSesameGrnOfflineSummary
import com.olam.warehouse.vegax.grnsesame.utils.GRN_FRAG
import com.olam.warehouse.vegax.grnsesame.utils.GRN_OFFLINE_FRAG
import com.olam.warehouse.vegax.grnsesame.utils.GRN_QUALITY_DETAILS

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
class VegaNigeriaSesameGrnActivity : HomeBaseActivity(), VegaNigeriaSesameGrnWBListFragment.CallBack,
    VegaNigeriaSesameGrnOfflineSummary.CallBack,VegaNigeriaSesameGrnDetailsFragment.QualityDetailsCallBack {

    private val mTAG = VegaNigeriaSesameGrnActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_nigeria_sesame_grn


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNigeriaSesameGrnFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaNigeriaSesameGrnWBListFragment(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flEcuadorGrn, allowBackStack = flag)
    }


    override fun replaceFragment(
        moveFrag: String,
        item: VegaGrnWeighBridgeId
    ) {
        when(moveFrag) {
            GRN_FRAG -> displayFragment(VegaNigeriaSesameGrnDetailsFragment.newInstance(item), true)
            GRN_OFFLINE_FRAG -> displayFragment(VegaNigeriaSesameGrnOfflineSummary.newInstance(), true)

        }
    }

    override fun replaceQualityDetailsFragment(
        moveFrag: String,
        wbDetails: VegaGrnWeighBridgeId,
        approveQualityList: ArrayList<VegaGRNSesameQuality>
    ) {
        when(moveFrag) {
            GRN_QUALITY_DETAILS ->
                displayFragment(VegaNigeriaSesameGRNQualityFragment.newInstance(wbDetails,approveQualityList), true)
        }
    }

}
