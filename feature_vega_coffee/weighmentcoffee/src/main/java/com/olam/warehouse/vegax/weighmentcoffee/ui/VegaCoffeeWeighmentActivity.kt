package com.olam.warehouse.vegax.weighmentcoffee.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.weighmentcoffee.R
import com.olam.warehouse.vegax.weighmentcoffee.di.injectVegaCoffeeWeighmentFeature
import com.olam.warehouse.vegax.weighmentcoffee.ui.mtnt.truckin.VegaCoffeeTruckInMtntFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.mtnt.truckin.VegaCoffeeTruckInMtntSummaryFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.mtnt.truckout.VegaCoffeeTruckOutMtntAddWeightFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.mtnt.truckout.VegaCoffeeTruckOutMtntSummaryFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.mtnt.truckout.VegaCoffeeTruckOutMtntWBListFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.truckin.VegaCoffeeTruckInListFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.truckin.VegaCoffeeTruckInSummaryFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.truckin.mtnr.VegaCoffeeMtnrFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.truckin.sales.VegaCoffeeSalesFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.truckin.sales.VegaCoffeeSalesTruckInSummaryFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.truckin.supplier.VegaCoffeeSupplierFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.truckout.VegaCoffeeTruckOutAddWeightAndBagFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.truckout.VegaCoffeeTruckOutSummaryFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.truckout.VegaCoffeeTruckOutWBListFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.truckout.sales.VegaCoffeeSalesTruckOutAddWeightFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.truckout.sales.VegaCoffeeSalesTruckOutSummaryFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.truckout.sales.VegaCoffeeSalesTruckOutWBListFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.truckout.sales.VegaCoffeeTruckOutTypeSelectFragment
import com.olam.warehouse.vegax.weighmentcoffee.ui.type.VegaCoffeeWeighmentTypeFragment
import com.olam.warehouse.vegax.weighmentcoffee.utils.*

/**
 * Created by Baskaran Kannan on 9/3/2020.
 */
class VegaCoffeeWeighmentActivity : HomeBaseActivity(), VegaCoffeeReplaceCallback,
    VegaCoffeeTruckInMtntFragment.CallBack, VegaCoffeeTruckOutMtntWBListFragment.CallBack,
    VegaCoffeeTruckOutMtntAddWeightFragment.CallBack, VegaCoffeeTruckOutAddWeightAndBagFragment.CallBack,
    VegaCoffeeSalesTruckOutWBListFragment.CallBack, VegaCoffeeSalesTruckOutAddWeightFragment.CallBack,
    VegaCoffeeTruckOutTypeSelectFragment.CallBack {
    private val mTAG = VegaCoffeeWeighmentActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_coffee_weighment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaCoffeeWeighmentFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaCoffeeWeighmentTypeFragment.newInstance(), false)
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
        when (val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)) {
            is VegaCoffeeTruckOutAddWeightAndBagFragment -> {
                //fragment.saveTruckOutInfo()
                supportFragmentManager.popBackStackImmediate()
            }

            is VegaCoffeeWeighmentTypeFragment -> {
                finish()
            }
            else -> {
                supportFragmentManager.popBackStackImmediate()
            }
        }
        /*when (val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)) {
            is VegaCoffeeMtntWeighBridgeAddLotFragment -> {
                fragment.saveLotDetails()
                super.onBackPressed()
            }
            is VegaCoffeeMtntWeighScalePalletFragment -> {
                super.onBackPressed()
                fragment.onBackPressed()
            }

            else -> super.onBackPressed()
        }*/
        //super.onBackPressed()
    }

    /*override fun addedLots(lots: ArrayList<VegaCocoaDispatchLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (lotAddFragment) {
            is VegaCoffeeMtntWeighBridgeAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
            is VegaCoffeeMtntWeighScaleAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCoffeeMtntWeighScalePalletFragment -> fragment.updateBagWeight(bagMaterial)
        }
    }*/


    override fun replaceMtntFragment(flag: String, moveFrag: String, mtntData: VegaMtnt) {
        when (moveFrag) {
            TRUCKIN_SUMMARYT_FRAG -> displayFragment(VegaCoffeeTruckInMtntSummaryFragment.newInstance(mtntData), true)
            TRUCKOUT_MTNT_ADD_WEIGHT_FRAG -> displayFragment(
                VegaCoffeeTruckOutMtntAddWeightFragment.newInstance(
                    mtntData,
                    arrayListOf<VegaMtnt>()
                ), true
            )
        }
    }

    override fun replaceMtntFragment(moveFrag: String, mtntData: VegaMtnt, mMtntList: MutableList<VegaMtnt>) {

        displayFragment(
            VegaCoffeeTruckOutMtntSummaryFragment.newInstance(mtntData, mMtntList as ArrayList<VegaMtnt>),
            true
        )
    }

    override fun replaceMtntFragment(type: String, direction: String, mtntData: Any) {

        if ((type == SUPPLIER || type == MTNR) && direction == DIRECTIONIN) {
            displayFragment(VegaCoffeeTruckInListFragment.newInstance(mtntData as VegaReceiving), true)
        } else if ((type == SUPPLIER || type == MTNR) && direction == DIRECTIONOUT) {
            displayFragment(VegaCoffeeTruckOutWBListFragment.newInstance(mtntData as VegaReceiving), true)
        } else if (type == SALES && direction == DIRECTIONIN) {
            displayFragment(VegaCoffeeSalesFragment.newInstance(mtntData as VegaReceiving), true)
        } else if (type == TRUCKIN_SALES_SUMMARYT_FRAG) {
            displayFragment(VegaCoffeeSalesTruckInSummaryFragment.newInstance(mtntData as VegaReceiving), true)
        } else if (type == SALES && direction == DIRECTIONOUT) {
            displayFragment(VegaCoffeeTruckOutTypeSelectFragment.newInstance(), true)
            /*displayFragment(VegaCoffeeSalesTruckOutWBListFragment.newInstance(mtntData as VegaMtnt), true)*/
        } else if (type == MTNR_FRAG) {
            displayFragment(VegaCoffeeMtnrFragment.newInstance(mtntData as VegaReceiving), true)
        } else if (type == TRUCKIN_SUMMARYT_FRAG) {
            displayFragment(VegaCoffeeTruckInSummaryFragment.newInstance(mtntData as VegaReceiving), true)
        } else if (SUPPLIER_FRAG == type) {
            displayFragment(VegaCoffeeSupplierFragment.newInstance(mtntData as VegaReceiving), true)
        } else if (type == TRUCKOUT_ADD_WEIGHT_FRAG) {
            displayFragment(
                VegaCoffeeTruckOutAddWeightAndBagFragment.newInstance(
                    mtntData as VegaReceiving,
                    arrayListOf<VegaReceiving>()
                ), true
            )
        } else {
            when (direction) {
                DIRECTIONIN -> displayFragment(
                    VegaCoffeeTruckInMtntFragment.newInstance(mtntData as VegaMtnt),
                    true
                )
                DIRECTIONOUT -> displayFragment(
                    VegaCoffeeTruckOutMtntWBListFragment.newInstance(mtntData as VegaMtnt),
                    true
                )
            }
        }
    }

    override fun replaceFragment(
        moveFrag: String,
        receivingData: VegaReceiving,
        mReceiving: MutableList<VegaReceiving>
    ) {
        if (moveFrag == TRUCKOUT_SUMMARYT_FRAG)
            displayFragment(
                VegaCoffeeTruckOutSummaryFragment.newInstance(
                    receivingData,
                    mReceiving as ArrayList<VegaReceiving>
                ), true
            )
    }

    override fun replaceMtntFragment(flag: String, moveFrag: Boolean) {
        val mtnData = VegaMtnt()
        mtnData.direction = DIRECTIONOUT
        mtnData.weighBridgeType = "sales"
        displayFragment(VegaCoffeeSalesTruckOutWBListFragment.newInstance(mtnData, moveFrag), true)
    }

    override fun replaceSalesFragment(flag: String, moveFrag: String, mtntData: VegaMtnt, isThirdPartySale: Boolean) {
        displayFragment(
            VegaCoffeeSalesTruckOutAddWeightFragment.newInstance(mtntData, arrayListOf<VegaMtnt>(), isThirdPartySale),
            true
        )
    }

    override fun replaceSalesFragment(
        moveFrag: String,
        mtntData: VegaMtnt,
        mMtntList: MutableList<VegaMtnt>,
        isThirdParty: Boolean
    ) {
        displayFragment(
            VegaCoffeeSalesTruckOutSummaryFragment.newInstance(
                mtntData,
                mMtntList as ArrayList<VegaMtnt>,
                isThirdParty
            ),
            true
        )
    }
}
