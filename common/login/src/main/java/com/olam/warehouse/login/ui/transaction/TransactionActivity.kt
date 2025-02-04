package com.olam.warehouse.login.ui.transaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.R
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.ui.transaction.cocoa.VegaCocoaTransactionTypeFragment
import com.olam.warehouse.login.ui.transaction.indocoffee.VegaIndoTransactionTypeFragment
import com.olam.warehouse.login.ui.transaction.nicaragua.VegaNicaraguaReprintTypeFragment
import com.olam.warehouse.login.ui.transaction.nicaragua.VegaNicaraguaTransactionTypeFragment
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.replaceFragment

/**
 * Created by Baskaran Kannan on 5/6/2020.
 */
class TransactionActivity : HomeBaseActivity() {

    private val mTAG = TransactionActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_transaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if (getCurrentKey().split("_")[0].contains("VEGA")) {
            when {
                getCurrentKey().split("_")[2].contains("CASH") -> displayFragment(
                    VegaTransactionFragment.newInstance(),
                    false
                )
                getCurrentKey().split("_")[2].contains("COCO") -> {
                    if (intent?.hasExtra(UIUtils.COCOA_MTNT) == true || intent?.hasExtra(UIUtils.COCOA_MTNR) == true) {
                        displayFragment(VegaCocoaTransactionTypeFragment.newInstance(), false)
                    } else
                        displayFragment(
                            VegaCocoaTransactionFragment.newInstance(),
                            false
                        )
                }
                getCurrentKey().split("_")[1].contains("NI") -> {
                    if (intent?.hasExtra(UIUtils.REPRINT) == true)
                        displayFragment(VegaNicaraguaReprintTypeFragment.newInstance(), false)
                    else
                        displayFragment(VegaNicaraguaTransactionTypeFragment.newInstance(), false)
                }
                getCurrentKey().split("_")[2].contains("ARAB") -> {
                    displayFragment(VegaIndoTransactionTypeFragment.newInstance(), false)
                }
            }
        }

    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flTransaction,
            allowBackStack = flag
        )
    }
}
