package com.olam.warehouse.vegax.salescocoa.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.salescocoa.R
import com.olam.warehouse.vegax.salescocoa.di.injectCocoaSalesFeature
import com.olam.warehouse.vegax.salescocoa.ui.weighbridge.VegaCocoaSalesTruckListFragment
import com.olam.warehouse.vegax.salescocoa.ui.weighscale.VegaCocoaSalesPalletFragment
import com.olam.warehouse.vegax.salescocoa.ui.weighscale.VegaCocoaSalesPendingListFragment
import com.olam.warehouse.vegax.salescocoa.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaCocoaDispatchSalesActivity : HomeBaseActivity(), VegaCocoaSalesSelectSaleTypeFragment.CallBack,
    VegaCocoaSalesPendingListFragment.CallBack, VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaCocoaSalesTruckListFragment.CallBack, VegaCocoaSalesAddLotFragment.CallBack,
    VegaCocoaSalesPalletFragment.CallBack, VegaCocoaAddPalletFragment.CallBackPallet,
    VegaCocoaSalesLotListFragment.CallBack {
    override val layoutResourceId = R.layout.activity_vega_cocoa_sales
    private val mTAG = VegaCocoaDispatchSalesActivity::class.java.canonicalName
    private val vm: VegaCocoaSalesViewModel by viewModel()
    private var pendingList = ArrayList<VegaCocoaSalesWB>()
    private var isWeighBridge: Boolean = false
    private var isWeighScale: Boolean = false
    private var isAnticipated: Boolean = false
    private var isAnticipatedVirtual: Boolean = true
    private var isInitialCall: Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCocoaSalesFeature()
        initNavigationView()
        vm.getConfigItems(UserRoles.SALES.role)
        vm.configItems.observe(this, Observer {
            updateConfigItems(it)
        })
        vm.pendingList.observe(this, Observer {
            if (it.isEmpty()) {
                isInitialCall = true
                replaceFragment(SALES_TYPE_WEIGHSCALE)
            } else {
                isInitialCall = false
                replaceFragment(PENDING)
            }
        })
        vm.dispatchPendingWB.observe(
            this,
            Observer { pendingList.addAll(vm.dispatchPendingWB.value ?: ArrayList()) })
        //moveSuccess()

        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("salescocoa/ui/VegaCocoaDispatchSalesActivity")
            .title("Sales Cocoa")
            .with(tracker)

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

    override fun replaceFragment(fragment: String) {
        when (fragment) {
            BRIDE -> displayFragment(VegaCocoaSalesTruckListFragment.newInstance(), true)
            SALES_TYPE_ANTICIPATED -> displayFragment(
                VegaCocoaSalesAddLotFragment.newInstance(
                    VegaCocoaSalesWB(),
                    SALES_TYPE_ANTICIPATED_VIRTUAL
                    , false
                ), !isInitialCall
            )
            SALES_TYPE_ANTICIPATED_VIRTUAL -> displayFragment(
                VegaCocoaSalesAddLotFragment.newInstance(
                    VegaCocoaSalesWB(),
                    SALES_TYPE_ANTICIPATED_VIRTUAL, false
                ), !isInitialCall
            )
            SCALE -> {
                vm.pendingList(SALES_TYPE_WEIGHSCALE)
            }
            SALES_TYPE_WEIGHSCALE -> {
                if (pendingList.isEmpty()) displayFragment(
                    VegaCocoaSalesAddLotFragment.newInstance(
                        VegaCocoaSalesWB(), SALES_TYPE_WEIGHSCALE, false
                    ), isInitialCall
                )
            }
            else ->
                displayFragment(VegaCocoaSalesPendingListFragment.newInstance(), true)
        }
    }

    override fun replaceFragment(fragment: String, item: VegaCocoaSalesWB, salesType: String) {
        when (fragment) {
            ADD_LOT -> displayFragment(VegaCocoaSalesAddLotFragment.newInstance(item, salesType, false), true)
            SUMMARY -> displayFragment(VegaCocoaSalesSummaryFragment.newInstance(item, false), true)
        }
    }

    override fun replaceFragment(fragment: String, item: VegaCocoaSalesWB, salesType: String, isRoundoff: Boolean) {
        when (fragment) {
            ADD_LOT -> displayFragment(VegaCocoaSalesAddLotFragment.newInstance(item, salesType, isRoundoff), true)
            SUMMARY -> displayFragment(VegaCocoaSalesSummaryFragment.newInstance(item, isRoundoff), true)
        }
    }

    override fun replaceFragment(fragment: String, lot: VegaCocoaSalesLots, isRoundoff: Boolean) {
        displayFragment(VegaCocoaSalesPalletFragment.newInstance(lot, isRoundoff), true)
        initBt(object : BtObserve {
            override fun valueObserve(btValue: String) {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaCocoaSalesPalletFragment -> {
                        val fragment1 = VegaCocoaAddPalletFragment()
                        fragment1.updateBtWeight(btValue)
                    }
                    is VegaSweepingWeightEntryFragment -> {
                        fragment.updateBtWeight(btValue)
                    }
                }
            }

        })
    }


    override fun replaceFragment(
        fragment: String,
        model: VegaCocoaSalesWB,
        materialList: ArrayList<String>,
        isMultipleLot: Boolean,
        isRoundoff: Boolean
    ) {
        displayFragment(VegaCocoaSalesLotListFragment.newInstance(model, materialList, isMultipleLot, isRoundoff), true)
    }

    override fun replaceFragment(receivingType: String, data: Any, isRoundoff: Boolean) {
        when (receivingType) {
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaCocoaSalesAddLotFragment -> {
                        fragment.updateAddWeight(data as String, isRoundoff)
                    }
                }
            }
            else -> displayFragment(VegaSweepingWeightEntryFragment.newInstance(data as Bundle, true), true)
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCocoaSalesPalletFragment -> {
                fragment.updateBagWeight(bagMaterial)
            }
        }
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.SALES_ANTICIPATED.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> isAnticipated = true
                        it.applicable?.contains("N")!! -> isAnticipated = false
                    }
                }

                ConfigItems.SALES_ANTICIPATED_VIRTUAL.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> isAnticipatedVirtual = true
                        it.applicable?.contains("N")!! -> isAnticipatedVirtual = false
                    }
                }

                ConfigItems.SALES_WEIGHBRIDGE.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> isWeighBridge = true
                        it.applicable?.contains("N")!! -> isWeighBridge = false
                    }
                }
                ConfigItems.SALES_WEIGHSCALE.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> isWeighScale = true
                        it.applicable?.contains("N")!! -> isWeighScale = false
                    }
                }
            }
        }
        navigateToTypeOrAdd()
    }

    private fun navigateToTypeOrAdd() {
        if ((isAnticipated && isWeighBridge) || (isAnticipated && isWeighScale) || (isWeighBridge && isWeighScale) /*||
            (isAnticipatedVirtual && isWeighBridge) || (isAnticipatedVirtual && isWeighScale)*/
        ) {
            isInitialCall = false
            displayFragment(VegaCocoaSalesSelectSaleTypeFragment.newInstance(), false)
        } else if (isWeighScale) {
            replaceFragment(
                SCALE
            )
        } else if (isWeighBridge) {
            replaceFragment(
                BRIDE
            )
        } else if (isAnticipated) {
            replaceFragment(SALES_TYPE_ANTICIPATED)
        } else if (isAnticipatedVirtual) replaceFragment(SALES_TYPE_ANTICIPATED_VIRTUAL)
        else displayFragment(VegaCocoaSalesSelectSaleTypeFragment.newInstance(), false)
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCocoaSalesPalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    private fun backNaviagation() {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCocoaSalesAddLotFragment -> {
                fragment.saveLotDetails()
                super.onBackPressed()
            }
            is VegaCocoaSalesPalletFragment -> {
                super.onBackPressed()
                val fragment1 = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment1) {
                    is VegaCocoaSalesAddLotFragment -> {
                        closeSocket()
                    }
                }
            }
            else -> super.onBackPressed()
        }
    }

    override fun onBackPressed() {
        backNaviagation()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                android.R.id.home -> {
                    backNaviagation()
                    return true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun addedLots(lots: ArrayList<VegaCocoaSalesLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        if (lotAddFragment is VegaCocoaSalesAddLotFragment) {
            lotAddFragment.updateLotList(lots)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeSocket()
    }

}
