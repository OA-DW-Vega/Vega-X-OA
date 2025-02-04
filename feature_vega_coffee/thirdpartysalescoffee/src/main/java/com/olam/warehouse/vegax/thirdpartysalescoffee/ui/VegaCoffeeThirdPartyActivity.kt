package com.olam.warehouse.vegax.thirdpartysalescoffee.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.thirdpartysalescoffee.R
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeThirdPartyLotListModel
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeWSBagModel
import com.olam.warehouse.vegax.thirdpartysalescoffee.di.injectCoffeeThirdPartyFeature
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.olam.VegaCoffeeThirdPartyOlamAddLotFragment
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.ownership.VegaCoffeeThirdPartyOwnershipAddLotFragment
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.thirdparty.*
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.*

/**
 * Created by Baskaran Kannan on 8/31/2020.
 */

class VegaCoffeeThirdPartyActivity : HomeBaseActivity(), VegaCoffeeThirdPartyReplaceFragmentCallback,
    VegaCoffeeThirdPartyAddLotListener, VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaCocoaAddPalletFragment.CallBackPallet, VegaCoffeeThirdPartyWeighbridgeAddLotFragment.ReplaceFragmentCallback {

    private val mTAG = VegaCoffeeThirdPartyActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_coffee_third_party

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        injectCoffeeThirdPartyFeature()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaCoffeeThirdPartyTypeFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flThirdParty, allowBackStack = flag)
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            Ownership_Transfer -> {
                displayFragment(VegaCoffeeThirdPartyOwnershipAddLotFragment.newInstance(data as String), true)
            }
            Ownership_Transfer_Summary -> {
                displayFragment(
                    VegaCoffeeThirdPartyOwnershipSummaryFragment.newInstance(
                        data as VegaCoffeeThirdPartyRequestModel,
                        null
                    ),
                    true
                )
            }

            Third_Party_Olam -> {
                displayFragment(VegaCoffeeThirdPartyOlamAddLotFragment.newInstance(data as String), true)
            }

            TP_WS -> {
                displayFragment(VegaCoffeeThirdPartySalesAddLotFragment.newInstance(data as String), true)
            }

            TP_WB_WS -> {
                displayFragment(VegaCoffeeThirdPartyWeighbridgeFragment.newInstance(data as String), true)
            }

            WEIGHBRIDGE_ADD_LOT -> displayFragment(
                VegaCoffeeThirdPartyWeighbridgeAddLotFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )

            LOT_LIST -> displayFragment(
                VegaCoffeeThirdPartyLotListFragment.newInstance(data as VegaCoffeeThirdPartyLotListModel),
                true
            )
            INVENTORY_FRAG -> displayFragment(
                VegaCoffeeThirdPartyWeighbridgeLotlistFragment.newInstance(data as VegaCoffeeThirdPartyLotListModel)
                , true
            )
            ADD_WEIGHT -> displayFragment(
                VegaCoffeeThirdPartyWeighScalePalletFragment.newInstance(data as VegaCocoaDispatchLots),
                true
            )
            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false),
                true
            )
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flThirdParty)
                when (fragment) {
                    is VegaCoffeeThirdPartySalesAddLotFragment -> fragment.updateAddWeight(data as VegaCoffeeWSBagModel)
                }
            }
            Third_Party_Sales -> displayFragment(VegaCoffeeThirdPartySalesTypeFragment.newInstance(), true)


        }
    }

//    fun replaceFragment(
//        fragment: String, materialArrayList: ArrayList<String>,
//        alreadySelected: ArrayList<VegaCocoaDispatchLots>, pileSelectionList: VegaWeighbridgeSelectionModel) {
//        when (fragment) {
//            WEIGHBRIDGE_THIRD_PARTY_SUMMARY -> displayFragment(
//                VegaCoffeeThirdPartyWeighbridgeSummaryFragment.newInstance(
//                    materialArrayList,
//                    alreadySelected,
//                    pileSelectionList
//                ), true
//            )
//        }
//    }

    override fun addedLots(lots: ArrayList<VegaCocoaDispatchLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flThirdParty)
        when (lotAddFragment) {
            is VegaCoffeeThirdPartyOwnershipAddLotFragment -> lotAddFragment.updateLotList(lots)
            is VegaCoffeeThirdPartyOlamAddLotFragment -> lotAddFragment.updateLotList(lots)
            is VegaCoffeeThirdPartyWeighbridgeAddLotFragment -> lotAddFragment.updateLotList(lots)
            is VegaCoffeeThirdPartySalesAddLotFragment -> lotAddFragment.updateLotList(lots)
        }
    }


    private fun backNavigation() {
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flThirdParty)
        if (fragment1 is VegaCoffeeThirdPartyWeighScalePalletFragment) {
            supportFragmentManager.popBackStackImmediate()
            fragment1.onBackPressed()
        } else {
            supportFragmentManager.popBackStackImmediate()
            when (fragment1) {
                is VegaCoffeeThirdPartyOwnershipAddLotFragment -> {
                    fragment1.saveAndBack()
                }
                is VegaCoffeeThirdPartyOlamAddLotFragment -> {
                    fragment1.saveAndBack()
                }
                is VegaCoffeeThirdPartyWeighbridgeAddLotFragment -> {
                    fragment1.saveAndBack()
                }
                is VegaCoffeeThirdPartySalesAddLotFragment -> {
                    fragment1.saveAndBack()
                }
                is VegaCoffeeThirdPartyTypeFragment -> {
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        backNavigation()
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flThirdParty)
        when (fragment) {
            is VegaCoffeeThirdPartyWeighScalePalletFragment -> fragment.updateBagWeight(bagMaterial)
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flThirdParty)
        when (fragment) {
            is VegaCoffeeThirdPartyWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
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

    override fun replaceFragment(receivingType: String, data: Any, model: VegaCocoaDispatchWB) {
        displayFragment(
            VegaCoffeeThirdPartyOwnershipSummaryFragment.newInstance(data as VegaCoffeeThirdPartyRequestModel, model),
            true
        )
    }
}
