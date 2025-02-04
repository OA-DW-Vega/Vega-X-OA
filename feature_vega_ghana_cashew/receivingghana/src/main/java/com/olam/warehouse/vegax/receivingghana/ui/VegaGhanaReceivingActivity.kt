package com.olam.warehouse.vegax.receivingghana.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.receivingghana.R
import com.olam.warehouse.vegax.receivingghana.di.injectGhanaReceivingFeature
import com.olam.warehouse.vegax.receivingghana.utils.FRAG_ADD_BAG_WEIGHT
import com.olam.warehouse.vegax.receivingghana.utils.OFFLOADING_SUMMARY_FRAG

/**
 * Created by Baskaran Kannan on 9/24/2020.
 */
class VegaGhanaReceivingActivity : HomeBaseActivity(),
    VegaGhanaReceivingSupplierFragment.CallBack,
    VegaGhanaReceivingSupplierSummaryFragment.CallBack,
    VegaGhanaReceivingWeightEntryFragment.CallBackAddBags {
    override val layoutResourceId = R.layout.activity_vega_ghana_receiving
    private val mTAG = VegaGhanaReceivingActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGhanaReceivingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        //vm.clearBagDetails()
        displayFragment(
            VegaGhanaReceivingSupplierFragment.newInstance(
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

    override fun replaceFragment(moveFrag: String, data: Any) {
        when (moveFrag) {
            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaGhanaReceivingWeightEntryFragment.newInstance(data as Bundle),
                true
            )
        }
    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
            //OFFLOADING_OFFLINE -> displayFragment(VegaEcuadorOffloadingOfflineSummary.newInstance(), true)
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
                VegaGhanaReceivingSupplierSummaryFragment.newInstance(
                    receivingData,
                    mReceiving as ArrayList<VegaReceiving>,
                    bagList
                ), true
            )
        }
    }

    override fun replaceFragment(moveFrag: String, receivingData: VegaReceiving) {
    }

    override fun updateBagWeight(bagMaterial: VegaEcuadorOffloadingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorOffloading)
        when (fragment) {
            is VegaGhanaReceivingSupplierFragment -> {
                fragment.updateBagWeight(bagMaterial)
            }
        }
    }
}
