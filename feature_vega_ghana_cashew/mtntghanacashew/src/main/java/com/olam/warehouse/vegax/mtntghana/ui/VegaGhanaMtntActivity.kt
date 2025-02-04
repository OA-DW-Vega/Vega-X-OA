package com.olam.warehouse.vegax.mtntghana.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.mtntghana.R
import com.olam.warehouse.vegax.mtntghana.di.injectGhanaMtntFeature
import com.olam.warehouse.vegax.mtntghana.ui.offline.VegaGhanaMtntOfflineSummaryFragment
import com.olam.warehouse.vegax.mtntghana.utils.*

/**
 * Created by Keerthi Santhanam on 7/12/2020.
 */

class VegaGhanaMtntActivity : HomeBaseActivity(), VegaGhanaMtntAddLotFragment.CallBack,
    VegaGhanaMtntAddLotsListener, VegaGhanaMtntMergeFragment.CallBack, VegaGhanaMtntLotSummaryFragment.CallBack,
    VegaGhanaMtntMergePreviewFragment.CallBack, VegaGhanaMtntOfflineSummaryFragment.CallBack {
    override val layoutResourceId = R.layout.activity_vega_ghana_mtnt
    private val mTAG = VegaGhanaMtntActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGhanaMtntFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(
            VegaGhanaMtntAddLotFragment.newInstance(
                VegaEcuadorDispatch(),
                ArrayList<VegaEcuadorDispatchLots>()
            ), false
        )
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flEcuadorDispatch, allowBackStack = flag)
    }

    override fun replaceFragment(fragment: String, model: VegaEcuadorDispatch, data: Any) {
        when (fragment) {
            ADD_LOT -> displayFragment(
                VegaGhanaMtntAddLotFragment.newInstance(
                    model,
                    data as ArrayList<VegaEcuadorDispatchLots>
                ), false
            )
            LOT_LIST -> displayFragment(
                VegaGhanaMtntLotListFragment.newInstance(
                    model,
                    data as ArrayList<String>
                ), true
            )
            MERGE_LIST -> displayFragment(
                VegaGhanaMtntMergeFragment.newInstance(
                    model
                ), true
            )
            MERGE_PREVIEW -> displayFragment(
                VegaGhanaMtntMergePreviewFragment.newInstance(
                    model
                ), true
            )
            DISPATCH_SUMMARY -> displayFragment(
                VegaGhanaMtntLotSummaryFragment.newInstance(
                    model,
                    data as ArrayList<VegaEcuadorDispatchLots>
                ), true
            )
        }
    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
            DISPATCH_OFFLINE_SUMMARY -> displayFragment(VegaGhanaMtntOfflineSummaryFragment.newInstance(), true)
        }
    }

    override fun addedLots(lots: ArrayList<VegaEcuadorDispatchLots>) {
        supportFragmentManager.popBackStackImmediate()
        supportFragmentManager.popBackStack(mTAG, 1)
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flEcuadorDispatch)
        if (lotAddFragment is VegaGhanaMtntAddLotFragment)
            lotAddFragment.updateLotList(lots)
    }

    override fun popAllFragmentsFromBackStack() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun onBackPressed() {
        backNavigation()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
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
            is VegaGhanaMtntLotSummaryFragment -> {
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
}
