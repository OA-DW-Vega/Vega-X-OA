package com.olam.warehouse.vegax.dispatchecuador.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.dispatchecuador.R
import com.olam.warehouse.vegax.dispatchecuador.di.injectEcuadorDispatchFeature
import com.olam.warehouse.vegax.dispatchecuador.ui.offline.VegaEcuadorDispatchOfflineSummaryFragment
import com.olam.warehouse.vegax.dispatchecuador.ui.weighbridge.VegaEcuadorMtntSummaryFragment
import com.olam.warehouse.vegax.dispatchecuador.ui.weighbridge.VegaEcuadorMtntWeighBridgeAddLotFragment
import com.olam.warehouse.vegax.dispatchecuador.ui.weighbridge.VegaEcuadorMtntWeighbridgeTruckListFragment
import com.olam.warehouse.vegax.dispatchecuador.utils.*


/**
 * Created by Keerthi Santhanam on 7/12/2020.
 */

class VegaEcuadorDispatchActivity : HomeBaseActivity(), VegaEcuadorDispatchAddLotFragment.CallBack,
    VegaEcuadorDispatchAddLotsListener, VegaEcuadorDispatchMergeFragment.CallBack, VegaEcuadorDispatchLotSummaryFragment.CallBack,
    VegaEcuadorDispatchMergePreviewFragment.CallBack, VegaEcuadorDispatchOfflineSummaryFragment.CallBack, VegaEcuadorMtntSelectDispatchTypeFragment.CallBack,
VegaEcuadorMtntWeighbridgeTruckListFragment.CallBack,VegaEcuadorMtntWeighBridgeAddLotFragment.CallBack{
    override val layoutResourceId = R.layout.activity_vega_ecuador_dispatch
    private val mTAG = VegaEcuadorDispatchActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectEcuadorDispatchFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        ttFeatureListDisPatch = ttFeatureList
        displayFragment(
            VegaEcuadorDispatchAddLotFragment.newInstance(
                VegaEcuadorDispatch(),
                ArrayList<VegaEcuadorDispatchLots>()
            ), false
        )
      //  displayFragment(VegaEcuadorMtntSelectDispatchTypeFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flEcuadorDispatch, allowBackStack = flag)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean,mTag:String) {
        replaceFragment(fragment, mTag, true, R.id.flEcuadorDispatch, allowBackStack = flag)
    }


    override fun replaceFragment(fragment: String, model: VegaEcuadorDispatch, data: Any) {
        when (fragment) {
            ADD_LOT -> displayFragment(
                VegaEcuadorDispatchAddLotFragment.newInstance(
                    model,
                    data as ArrayList<VegaEcuadorDispatchLots>
                ), true
            )
            LOT_LIST -> displayFragment(
                VegaEcuadorDispatchLotListFragment.newInstance(
                    model,
                    data as ArrayList<String>
                ), true
            )
            MERGE_LIST -> displayFragment(
                VegaEcuadorDispatchMergeFragment.newInstance(
                    model
                ), true
            )
            MERGE_PREVIEW -> displayFragment(
                VegaEcuadorDispatchMergePreviewFragment.newInstance(
                    model
                ), true
            )
            DISPATCH_SUMMARY -> {
                if(FRAG_TYPE.equals("WS"))
                    //weighscale flow
                    displayFragment(
                        VegaEcuadorDispatchLotSummaryFragment.newInstance(
                            model,
                            data as ArrayList<VegaEcuadorDispatchLots>
                        ), true
                    )

                else
                   //weighbridge flow
                    displayFragment(
                        VegaEcuadorMtntSummaryFragment.newInstance(
                            model,
                            data as ArrayList<VegaEcuadorDispatchLots>
                        ), true
                    )

            }
        }
    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
            DISPATCH_OFFLINE_SUMMARY -> displayFragment(VegaEcuadorDispatchOfflineSummaryFragment.newInstance(), true)
        }
    }

    override fun addedLots(lots: ArrayList<VegaEcuadorDispatchLots>) {
        supportFragmentManager.popBackStackImmediate()
       // supportFragmentManager.popBackStack(mTAG, 1)
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flEcuadorDispatch)
        if (lotAddFragment is VegaEcuadorDispatchAddLotFragment)
            lotAddFragment.updateLotList(lots)
        else if (lotAddFragment is VegaEcuadorMtntWeighBridgeAddLotFragment)
            lotAddFragment.updateLotList(lots)
    }

    override fun popAllFragmentsFromBackStack() {
        //taking fragment back to add lot fragment
        var count = supportFragmentManager.backStackEntryCount  //backStackCount
        if(FRAG_TYPE.equals("WS")) {     //weighscale flow
            while(count>1) {
                supportFragmentManager.popBackStackImmediate()
                count--
            }
        } else {
            while(count>2) {        //weighbridge flow
                supportFragmentManager.popBackStackImmediate()
                count--
            }
        }

        //  supportFragmentManager.popBackStack("VegaEcuadorDispatchAddLotFragment",0)
    }

    override fun onBackPressed() {
        backNavigation()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            when (it.itemId) {
                android.R.id.home -> {
                    backNavigation()
                    return true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun backNavigation() {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorDispatch)
        when (fragment) {
            is VegaEcuadorDispatchLotSummaryFragment -> {
                if (fragment.getLotEditableStatus())
                    super.onBackPressed()
                else {
                    showCancelDialog()
                }
            }
            else -> super.onBackPressed()
        }
    }

    private fun showCancelDialog() {
        MaterialDialog(this).show {
            message(R.string.cancel_dispatch)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.login.R.string.confirm),
                getString(com.olam.warehouse.login.R.string.cancel),
                { moveToHomePage() },
                { dismiss() })
        }
    }

    private fun moveToHomePage() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            WEIGHBRIDGE -> {
                FRAG_TYPE = "WB"
                displayFragment(VegaEcuadorMtntWeighbridgeTruckListFragment.newInstance(), true)
            }
            WEIGHBRIDGE_ADD_LOT -> displayFragment(
                VegaEcuadorMtntWeighBridgeAddLotFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            MTNT_WEIGHSCALE -> {
                FRAG_TYPE = "WS"
                displayFragment(
                    VegaEcuadorDispatchAddLotFragment.newInstance(
                        VegaEcuadorDispatch(),
                        ArrayList<VegaEcuadorDispatchLots>()
                    ), true
                )
            }
        }
    }

}
