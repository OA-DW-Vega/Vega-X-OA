package com.olam.warehouse.vegax.mtntindo.ui

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
import com.olam.warehouse.presentation.utils.UIUtils.TRANS_MTNT
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.mtntindo.R
import com.olam.warehouse.vegax.mtntindo.di.injectIndoCoffeeDispatchFeature
import com.olam.warehouse.vegax.mtntindo.ui.callback.VegaIndoCoffeeDispatchAddLotsListener
import com.olam.warehouse.vegax.mtntindo.ui.offline.VegaIndoCoffeeDispatchOfflineSummaryFragment
import com.olam.warehouse.vegax.mtntindo.ui.offline.VegaIndoCoffeeMtntTransactionDetailsFragment
import com.olam.warehouse.vegax.mtntindo.utils.*

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */
class VegaIndoCoffeeDispatchActivity : HomeBaseActivity(), VegaIndoCoffeeDispatchAddLotFragment.CallBack,
    VegaIndoCoffeeDispatchAddLotsListener, VegaIndoCoffeeDispatchMergeFragment.CallBack,
    VegaIndoCoffeeDispatchLotSummaryFragment.CallBack, VegaIndoCoffeeMtntTransactionDetailsFragment.CallBack,
    VegaIndoCoffeeDispatchMergePreviewFragment.CallBack, VegaIndoCoffeeDispatchOfflineSummaryFragment.CallBack {
    override val layoutResourceId = R.layout.activity_indo_coffee_dispatch
    private val mTAG = VegaIndoCoffeeDispatchActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectIndoCoffeeDispatchFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if (intent.hasExtra(TRANS_MTNT))
            displayFragment(VegaIndoCoffeeMtntTransactionDetailsFragment.newInstance(), false)
        else
            displayFragment(
                VegaIndoCoffeeDispatchAddLotFragment.newInstance(
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
                VegaIndoCoffeeDispatchAddLotFragment.newInstance(
                    model,
                    data as ArrayList<VegaEcuadorDispatchLots>
                ), false
            )
            LOT_LIST -> displayFragment(
                VegaIndoCoffeeDispatchLotListFragment.newInstance(
                    model,
                    data as ArrayList<String>
                ), true
            )
            MERGE_LIST -> displayFragment(
                VegaIndoCoffeeDispatchMergeFragment.newInstance(
                    model
                ), true
            )
            MERGE_PREVIEW -> displayFragment(
                VegaIndoCoffeeDispatchMergePreviewFragment.newInstance(
                    model
                ), true
            )
            DISPATCH_SUMMARY -> displayFragment(
                VegaIndoCoffeeDispatchLotSummaryFragment.newInstance(
                    model,
                    data as ArrayList<VegaEcuadorDispatchLots>
                ), true
            )
        }
    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
            DISPATCH_OFFLINE_SUMMARY -> displayFragment(
                VegaIndoCoffeeDispatchOfflineSummaryFragment.newInstance(),
                true
            )
        }
    }

    override fun addedLots(lots: ArrayList<VegaEcuadorDispatchLots>) {
        supportFragmentManager.popBackStackImmediate()
        supportFragmentManager.popBackStack(mTAG, 1)
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flEcuadorDispatch)
        if (lotAddFragment is VegaIndoCoffeeDispatchAddLotFragment)
            lotAddFragment.updateLotList(lots)
    }

    override fun popAllFragmentsFromBackStack() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
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
            is VegaIndoCoffeeDispatchLotSummaryFragment -> {
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
