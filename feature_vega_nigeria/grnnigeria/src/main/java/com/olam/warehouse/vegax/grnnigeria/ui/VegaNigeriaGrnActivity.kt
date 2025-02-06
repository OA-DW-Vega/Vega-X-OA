package com.olam.warehouse.vegax.grnnigeria.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.grnnigeria.R
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.VegaNigeriaGRNQuality
import com.olam.warehouse.vegax.grnnigeria.di.injectNigeriaGrnFeature
import com.olam.warehouse.vegax.grnnigeria.ui.offline.VegaNigeriaGrnOfflineSummary
import com.olam.warehouse.vegax.grnnigeria.utils.*

/**
 * Created by Roshna Parambil on 9/9/2020.
 */
class VegaNigeriaGrnActivity : HomeBaseActivity(), VegaNigeriaGrnWBListFragment.CallBack,
    VegaNigeriaGrnOfflineSummary.CallBack, VegaNigeriaGrnDetailsFragment.QualityDetailsCallBack,
    VegaNigeriaGrnTypeFragment.CallBack,
    VegaNigeriaGRNTDetailsFragement.CallBack {
    private val mTAG = VegaNigeriaGrnActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_nigeria_grn


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNigeriaGrnFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaNigeriaGrnTypeFragment(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flEcuadorGrn, allowBackStack = flag)
    }

    override fun replaceQualityFragment(
        moveFrag: String,
        wbDetails: VegaGrnWeighBridgeId,
        approveQualityList: ArrayList<VegaNigeriaGRNQuality>
    ) {
        when (moveFrag) {
            GRN_QUALITY_DETAILS ->
                displayFragment(
                    VegaNigeriaGRNQualityFragment.newInstance(
                        wbDetails,
                        approveQualityList
                    ), true
                )
        }
    }


    override fun replaceFragment(moveFrag: String, item: VegaGrnWeighBridgeId) {
        when (moveFrag) {
            GRN_FRAG -> displayFragment(VegaNigeriaGrnDetailsFragment.newInstance(item), true)
            GRN_OFFLINE_FRAG -> displayFragment(VegaNigeriaGrnOfflineSummary.newInstance(), true)
        }
    }

    override fun replaceQualityDetailsFragment(
        moveFrag: String,
        wbDetails: VegaGrnWeighBridgeId,
        approveQualityList: ArrayList<VegaNigeriaGRNQuality>
    ) {
        when (moveFrag) {
            GRN_QUALITY_DETAILS ->
                displayFragment(
                    VegaNigeriaGRNQualityFragment.newInstance(
                        wbDetails,
                        approveQualityList
                    ), true
                )
        }
    }

    override fun replaceFragment(gateEntryType: String, gateEntryData: VegaGateEntry) {
        when (gateEntryType) {
            GRN -> displayFragment(VegaNigeriaGrnWBListFragment(), false)
            GRNT -> displayFragment(
                VegaNigeriaGRNTDetailsFragement.newInstance(gateEntryData),
                true
            )
        }
    }

    override fun replaceFragment(
        gateEntryType: String,
        gateEntryData: VegaGateEntry,
        grnPrice: String
    ) {
        when (gateEntryType) {
            SUMMARY_FRAG -> displayFragment(
                VegaGrntNigeriaSummaryFragment.newInstance(
                    gateEntryData,
                    grnPrice
                ), true
            )
        }
    }
}
