package com.olam.warehouse.odreceiving.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.master.dorigin.entity.DOReceivingMtnLots
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.di.injectReceivingFeature
import com.olam.warehouse.odreceiving.ui.blt.DOReceivingDispatchDetailsFragment
import com.olam.warehouse.odreceiving.ui.mtnr.DOReceivingMtnrFragment
import com.olam.warehouse.odreceiving.ui.supplier.DOReceivingSupplierFragment
import com.olam.warehouse.odreceiving.ui.weigh.DOReceivingMtnrWeighScaleFragment
import com.olam.warehouse.odreceiving.ui.weigh.DOReceivingTransactionListFragment
import com.olam.warehouse.odreceiving.utils.DISPATCH_DETAILS
import com.olam.warehouse.odreceiving.utils.RECEIVING_MTN
import com.olam.warehouse.odreceiving.utils.TRANSACTION_LIST
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.extension.replaceFragment

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
class DOReceivingActivity : HomeBaseActivity(), DOReceivingMtnrFragment.CallBack,
    DOReceivingSupplierFragment.CallBack, DOReceivingTransactionListFragment.CallBack  {

    private val mTAG = DOReceivingActivity::class.java.canonicalName

    override val layoutResourceId = R.layout.activity_do_receiving

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectReceivingFeature()
        initNavigationView()
        initUI()
    }

    override fun replaceFragment(receivingData: DOReceiving, lots: List<DOReceivingMtnLots>) {
        val gson = GsonUtils()
        displayFragment(DOReceivingMtnrWeighScaleFragment.newInstance(receivingData, gson.toJson(lots)), true)
    }

    private fun initUI() {

        when (intent?.hasExtra(RECEIVING_MTN)) {
            true -> displayFragment(DOReceivingSupplierFragment(), false)
            else -> displayFragment(DOReceivingSupplierFragment(), false)
        }
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, allowStateLoss = true, containerViewId = R.id.flReceive, allowBackStack = flag)
    }

    override fun replaceFragment(moveFrag: String) {
        if (moveFrag == TRANSACTION_LIST) {
            displayFragment(DOReceivingTransactionListFragment.newInstance(), true)
        } else if(moveFrag == DISPATCH_DETAILS) {
            displayFragment(DOReceivingDispatchDetailsFragment.newInstance(), true)
        }
    }

    override fun replaceFragment(receivingData: DOReceiving) {
      //  val fragment= DOReceivingSupplierFragment.newInstance()
        //displayFragment(fragment,true)
        val fragment = supportFragmentManager.findFragmentById(R.id.flReceive)
        when(fragment){
            is DOReceivingSupplierFragment-> fragment.updateTransactionSelection(receivingData)
        }

    }
}
