package com.olam.warehouse.vegax.mtntnicaragua.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.veganicaragua.entity.VegaNicDispatchLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMtnt
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_MTNR
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_MTNT
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_SAMPLE_TICKET
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_TICKET
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.mtntnicaragua.R
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.VegaNicLotListModel
import com.olam.warehouse.vegax.mtntnicaragua.di.injectNicaraguaMtntFeature
import com.olam.warehouse.vegax.mtntnicaragua.ui.reprint.VegaNicMtnrReprintFragment
import com.olam.warehouse.vegax.mtntnicaragua.ui.reprint.VegaNicMtntReprintFragment
import com.olam.warehouse.vegax.mtntnicaragua.ui.reprint.VegaNicTicketReprintFragment
import com.olam.warehouse.vegax.mtntnicaragua.ui.reprint.VegaNicTicketSampleReprintFragment
import com.olam.warehouse.vegax.mtntnicaragua.ui.transaction.VegaNicMtntTransactionFragment
import com.olam.warehouse.vegax.mtntnicaragua.utils.*

/**
 * Created by Baskaran Kannan on 11/11/2020.
 */
class VegaNicaraguaMtntActivity : HomeBaseActivity(), VegaNicaraguaMtntConsignmentFragment.Callback,
    VegaNicaraguaMtntWeighScaleAddLotFragment.Callback, VegaNicaraguaLotListFragment.Callback,
    VegaNicaraguaMtntSummaryFragment.Callback, VegaNicMtntTransactionFragment.CallBack,
    VegaNicaraguaMergeLotFragment.Callback {

    private val mTAG = VegaNicaraguaMtntActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_nicaragua_mtnt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNicaraguaMtntFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if (intent.hasExtra(UIUtils.TRANS_MTNT))
            displayFragment(VegaNicMtntTransactionFragment(), false)
        else if (intent.hasExtra(REPRINT_MTNT))
            displayFragment(VegaNicMtntReprintFragment(), false)
        else if (intent.hasExtra(REPRINT_TICKET))
            displayFragment(VegaNicTicketReprintFragment(), false)
        else if (intent.hasExtra(REPRINT_SAMPLE_TICKET))
            displayFragment(VegaNicTicketSampleReprintFragment(), false)
        else if (intent.hasExtra(REPRINT_MTNR))
            displayFragment(VegaNicMtnrReprintFragment(), false)
        else
            displayFragment(
                VegaNicaraguaMtntConsignmentFragment.newInstance(VegaNicaraguaMtnt()),
                false
            )

    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flContainer,
            allowBackStack = flag
        )
    }

    override fun replaceFragment(type: String, data: Any) {
        when (type) {
            FRAG_ADD_LOT -> displayFragment(
                VegaNicaraguaMtntWeighScaleAddLotFragment.newInstance(data as VegaNicaraguaMtnt),
                true
            )
            FRAG_CONSIGN -> displayFragment(
                VegaNicaraguaMtntConsignmentFragment.newInstance(data as VegaNicaraguaMtnt),
                true
            )
            FRAG_SUMMARY -> displayFragment(
                VegaNicaraguaMtntSummaryFragment.newInstance(data as VegaNicaraguaMtnt),
                true
            )
            FRAG_LOT_LIST -> displayFragment(
                VegaNicaraguaLotListFragment.newInstance(data as VegaNicLotListModel),
                true
            )
            FRAG_ADD_WEIGHT -> displayFragment(
                VegaNicAddWeightEntryFragment.newInstance(data as VegaNicDispatchLots),
                true
            )
            FRAG_MERGE_LOT -> displayFragment(
                VegaNicaraguaMergeLotFragment.newInstance(data as VegaNicaraguaMtnt),
                true
            )
            EDIT_LOT -> {
                supportFragmentManager.popBackStackImmediate()
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaNicaraguaMtntWeighScaleAddLotFragment -> fragment.editLot(data as VegaNicDispatchLots)
                }
            }
        }
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
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment1) {
            is VegaNicaraguaMtntSummaryFragment -> {
                if (fragment1.isPostCreated) {
                    fragment1.onBackRefreshed()
                } else super.onBackPressed()
            }
            else -> {
                supportFragmentManager.popBackStackImmediate()
                when (val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)) {
                    is VegaNicaraguaMtntWeighScaleAddLotFragment -> {
                        when (fragment1) {
                            is VegaNicaraguaMtntSummaryFragment -> fragment.getBack()
                            is VegaNicAddWeightEntryFragment -> {
                            }
                        }
                    }
                    is VegaNicaraguaMtntConsignmentFragment -> {
                        when (fragment1) {
                            is VegaNicaraguaMtntWeighScaleAddLotFragment -> {
                                fragment.onBackRefreshed()
                            }
                            else -> super.onBackPressed()
                        }
                    }
                    is VegaNicMtntTransactionFragment -> {
                        when (fragment1) {
                            is VegaNicaraguaMtntConsignmentFragment -> {
                            }
                            is VegaNicaraguaMtntSummaryFragment -> {
                            }
                            is VegaNicaraguaMtntWeighScaleAddLotFragment -> {
                            }
                            else -> {
                                val starSync = PreferenceHelper.get(Constants.START_SYNC_MTNT, false)
                                when (starSync) {
                                    true -> showAlertDialog()
                                    else -> super.onBackPressed()
                                }
                            }
                        }
                    }
                    is VegaNicaraguaMergeLotFragment -> {

                    }
                    else -> super.onBackPressed()
                }
            }
        }

    }

    private fun showAlertDialog() {
        MaterialDialog(this).show {
            message(R.string.sync_started)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.ok),
                "",
                { dismiss() },
                { dismiss() })
        }
    }
}
