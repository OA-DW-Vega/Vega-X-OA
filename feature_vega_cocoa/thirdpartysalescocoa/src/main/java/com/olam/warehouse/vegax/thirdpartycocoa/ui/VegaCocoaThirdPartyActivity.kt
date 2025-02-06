package com.olam.warehouse.vegax.thirdpartycocoa.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.thirdpartycocoa.R
import com.olam.warehouse.vegax.thirdpartysalescocoa.ui.VegaCocoaThirdPartyTypeFragment
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeThirdPartyLotListModel
import com.olam.warehouse.vegax.thirdpartysalescoffee.di.injectCocoaThirdPartyFeature
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCocoaThirdPartyAddLotListener
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCocoaThirdPartyLotListFragment
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCocoaThirdPartyOwnershipSummaryFragment
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCocoaThirdPartyReplaceFragmentCallback
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.ownership.VegaCocoaThirdPartyOwnershipAddLotFragment
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.LOT_LIST
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.Ownership_Transfer
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.Ownership_Transfer_Summary

class VegaCocoaThirdPartyActivity : HomeBaseActivity(), VegaCocoaThirdPartyReplaceFragmentCallback,
    VegaCocoaThirdPartyAddLotListener, VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaCocoaAddPalletFragment.CallBackPallet {

    private val mTAG = VegaCocoaThirdPartyActivity::class.java.canonicalName

    override val layoutResourceId = R.layout.activity_cocoa_third_party

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        injectCocoaThirdPartyFeature()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaCocoaThirdPartyTypeFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flThirdParty, allowBackStack = flag)
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            Ownership_Transfer -> {
                displayFragment(VegaCocoaThirdPartyOwnershipAddLotFragment.newInstance(data as String), true)
            }
            Ownership_Transfer_Summary -> {
                displayFragment(
                    VegaCocoaThirdPartyOwnershipSummaryFragment.newInstance(data as VegaCoffeeThirdPartyRequestModel),
                    true
                )
            }
            LOT_LIST -> displayFragment(
                VegaCocoaThirdPartyLotListFragment.newInstance(data as VegaCoffeeThirdPartyLotListModel),
                true
            )

        }
    }

    override fun addedLots(lots: ArrayList<VegaCocoaDispatchLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flThirdParty)
        when (lotAddFragment) {
            is VegaCocoaThirdPartyOwnershipAddLotFragment -> lotAddFragment.updateLotList(lots)
        }
    }

    private fun backNavigation() {
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flThirdParty)
        supportFragmentManager.popBackStackImmediate()
        when (fragment1) {
            is VegaCocoaThirdPartyOwnershipAddLotFragment -> {
                fragment1.saveAndBack()
            }

            is VegaCocoaThirdPartyTypeFragment -> {
                finish()
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

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        TODO("Not yet implemented")
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        TODO("Not yet implemented")
    }
}
