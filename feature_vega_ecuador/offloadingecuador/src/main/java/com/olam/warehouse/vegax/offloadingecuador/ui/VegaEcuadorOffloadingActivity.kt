package com.olam.warehouse.vegax.offloadingecuador.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.offloadingecuador.R
import com.olam.warehouse.vegax.offloadingecuador.di.injectEcuadorOffloadingFeature
import com.olam.warehouse.vegax.offloadingecuador.ui.offline.VegaEcuadorOffloadingOfflineSummary
import com.olam.warehouse.vegax.offloadingecuador.ui.supplier.VegaEcuadorOffloadingSupplierFragment
import com.olam.warehouse.vegax.offloadingecuador.ui.supplier.VegaEcuadorOffloadingSupplierSummaryFragment
import com.olam.warehouse.vegax.offloadingecuador.ui.supplier.VegaEcuadorOffloadingWeightEntryFragment
import com.olam.warehouse.vegax.offloadingecuador.utils.FRAG_ADD_BAG_WEIGHT
import com.olam.warehouse.vegax.offloadingecuador.utils.OFFLOADING_OFFLINE
import com.olam.warehouse.vegax.offloadingecuador.utils.OFFLOADING_SUMMARY_FRAG
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaEcuadorOffloadingActivity : HomeBaseActivity(), VegaEcuadorOffloadingSupplierFragment.CallBack,
    VegaEcuadorOffloadingWeightEntryFragment.CallBackAddBags, VegaEcuadorOffloadingOfflineSummary.CallBack {
    private val mTAG = VegaEcuadorOffloadingActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_vega_ecuador_offloading
    private val vm: VegaEcuadorOffloadingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectEcuadorOffloadingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        //vm.clearBagDetails()
        displayFragment(
            VegaEcuadorOffloadingSupplierFragment.newInstance(
                VegaReceiving(),
                ArrayList<VegaEcuadorOffloadingBagMaterial>()
            ), false
        )
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flEcuadorOffloading,
            allowBackStack = flag
        )
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            FRAG_ADD_BAG_WEIGHT -> displayFragment(VegaEcuadorOffloadingWeightEntryFragment.newInstance(data as Bundle), true)
        }
    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
            OFFLOADING_OFFLINE -> displayFragment(VegaEcuadorOffloadingOfflineSummary.newInstance(), true)
        }
    }

    override fun replaceFragment(
        moveFrag: String,
        receivingData: VegaReceiving,
        mReceiving: MutableList<VegaReceiving>,
        bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>
    ) {
        when (moveFrag) {
            OFFLOADING_SUMMARY_FRAG -> displayFragment(
                VegaEcuadorOffloadingSupplierSummaryFragment.newInstance(
                    receivingData,
                    mReceiving as ArrayList<VegaReceiving>,
                    bagList
                ), true
            )
        }
    }

    override fun updateBagWeight(bagMaterial: VegaEcuadorOffloadingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorOffloading)
        when (fragment) {
            is VegaEcuadorOffloadingSupplierFragment -> {
                fragment.updateBagWeight(bagMaterial)
            }
        }
    }
}
