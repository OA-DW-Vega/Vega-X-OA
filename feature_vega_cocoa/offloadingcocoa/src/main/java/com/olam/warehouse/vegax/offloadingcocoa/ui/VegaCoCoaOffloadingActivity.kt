package com.olam.warehouse.vegax.offloadingcocoa.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.common.VegaTrackTraceListener
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.TrackTraceFarmerModel
import com.olam.warehouse.master.vega.entity.TrackTraceSourceLotDetails
import com.olam.warehouse.master.vega.entity.TrackTraceTransactionIdDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiveLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiving
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils.TRANS_MTNR
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.offloadingcocoa.R
import com.olam.warehouse.vegax.offloadingcocoa.di.injectVegaCoCoaOffloadingFeature
import com.olam.warehouse.vegax.offloadingcocoa.ui.mtnr.VegaCoCoaMtnrAddWeightFragment
import com.olam.warehouse.vegax.offloadingcocoa.ui.mtnr.VegaCoCoaMtnrConsignmentFragment
import com.olam.warehouse.vegax.offloadingcocoa.ui.mtnr.VegaCoCoaMtnrWeighScaleSummaryFragment
import com.olam.warehouse.vegax.offloadingcocoa.ui.supplier.VegaIvcCocoSupplierConsignmentFragment
import com.olam.warehouse.vegax.offloadingcocoa.ui.transaction.VegaCocoaMtnrTransactionFragment
import com.olam.warehouse.vegax.offloadingcocoa.ui.transaction.VegaCocoaNoWeighmentMtnrTransactionSummaryFragment
import com.olam.warehouse.vegax.offloadingcocoa.ui.wblist.VegaCocoOffloadWeighscaleListFragment
import com.olam.warehouse.vegax.offloadingcocoa.utils.*

class VegaCoCoaOffloadingActivity : HomeBaseActivity(), VegaCoCoaOffloadReplaceFragmentCallback,
    VegaTrackTraceListener {
    private val mTAG = VegaCoCoaOffloadingActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_cocoa_offloading
    private var isTransaction = false
    var procurementType = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaCoCoaOffloadingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        isTransaction = intent.getBooleanExtra(TRANS_MTNR, false)
        procurementType = intent.getStringExtra(Constants.PROCUREMENT_TYPE)?:""

        if (isTransaction)
            displayFragment(VegaCocoaMtnrTransactionFragment.newInstance(), true)
        else
            displayFragment(VegaIvcCocoOffloadTypeSelectFragment.newInstance(isTransaction), true)
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

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            WEIGHSCALE -> displayFragment(VegaCoCoaMtnrConsignmentFragment.newInstance(), true)

            ADD_WEIGHT -> /*displayFragment(
                VegaCoCoaMtnrWeighScalePalletFragment.newInstance(data as VegaCoffeeReceiveLots),
                true
            )*/
                displayFragment(VegaCoCoaMtnrAddWeightFragment.newInstance(data as Bundle), true)
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaCoCoaMtnrConsignmentFragment -> fragment.updateAddWeight(data as String)
                }
            }
            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false),
                true
            )
            MTNR_WEIGHSCALE_SUMMARY -> displayFragment(
                VegaCoCoaMtnrWeighScaleSummaryFragment.newInstance(data as VegaCoCoaReceiving),
                true
            )
            EDIT_LOT -> {
                if (isTransaction) {
                    displayFragment(VegaCoCoaMtnrConsignmentFragment.newInstance(data as VegaCoCoaReceiving), true)
                } else {
                    supportFragmentManager.popBackStackImmediate()
                }
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaCoCoaMtnrConsignmentFragment -> fragment.editLot(data as VegaCoCoaReceiveLots)
                }

            }
            TRANSACTION_SUMMARY ->
                displayFragment(
                    VegaCocoaNoWeighmentMtnrTransactionSummaryFragment.newInstance(data as VegaCoCoaReceiving),
                    true
                )
            MTNR ->
                displayFragment(
                    VegaCoCoaMtnrConsignmentFragment.newInstance(data as VegaCoCoaReceiving),
                    true
                )
            TRANSACTION ->
                displayFragment(
                    VegaCocoaMtnrTransactionFragment.newInstance(),
                    true
                )
            SUPPLIER ->
                displayFragment(
                    VegaCocoOffloadWeighscaleListFragment.newInstance(VegaCoCoaReceiving(weighBridgeType = PROCURE)),
                    true
                )
            WEIGHSCALE_SUPPLIER ->
                displayFragment(
                    VegaIvcCocoSupplierConsignmentFragment.newInstance(data as VegaCoCoaReceiving, procurementType),
                    true
                )
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
        supportFragmentManager.popBackStackImmediate()
        when (val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)) {
            is VegaCoCoaMtnrWeighScaleSummaryFragment -> {
            }
            is VegaIvcCocoSupplierConsignmentFragment -> {
            }
            is VegaIvcCocoOffloadTypeSelectFragment ->{}
            is VegaCoCoaMtnrConsignmentFragment -> {
                when (fragment1) {
                    is VegaCoCoaMtnrWeighScaleSummaryFragment -> fragment.getBack()
                }
            }
            is VegaCocoaMtnrTransactionFragment ->
                fragment.getBack()
            else -> super.onBackPressed()
        }
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

    /*override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCoCoaMtnrWeighScalePalletFragment -> fragment.updateBagWeight(prepareItem((bagMaterial)))
        }
    }*/

    /*override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCoCoaMtnrWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }*/

    override fun isVendor(flag: Boolean) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaIvcCocoSupplierConsignmentFragment -> {
//                fragment.isVendor(flag)
            }
        }
    }

    override fun isComplaint(status: Int) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaIvcCocoSupplierConsignmentFragment -> {
                fragment.isComplaint(status)
            }
        }
    }

    override fun updateSourceLotDetails(sourceLotDetails: TrackTraceSourceLotDetails) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaIvcCocoSupplierConsignmentFragment -> {
                fragment.updateSourceLotDetails(sourceLotDetails)
            }
        }
    }

    override fun updateTransactionIdDetails(transactionIdDetails: TrackTraceTransactionIdDetails) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaIvcCocoSupplierConsignmentFragment -> {
                fragment.updateFarmerlessTransactionDetails(transactionIdDetails)
            }
        }
    }

    override fun updateFarmerDetails(farmerListDetails: ArrayList<TrackTraceFarmerModel>) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaIvcCocoSupplierConsignmentFragment -> {
                fragment.updateFarmerListDetails(farmerListDetails)
            }
        }
    }
}
