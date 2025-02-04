package com.olam.warehouse.vegax.processingcameroon.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.processingcameroon.R
import com.olam.warehouse.vegax.processingcameroon.di.injectCameroonProcessingFeature
import com.olam.warehouse.vegax.processingcameroon.ui.fgrn.*
import com.olam.warehouse.vegax.processingcameroon.ui.rmin.*
import com.olam.warehouse.vegax.processingcameroon.utils.*

class VegaCameroonProcessingActivity : HomeBaseActivity(), VegaCameroonProcessingSelectTypeFragment.CallBack,
    VegaCameroonFgrnPendingFragment.CallBack, VegaCameroonFgrnAddWeightAndLotsFragment.CallBack,
    VegaCameroonFgrnAssignLotFragment.CallBack, VegaCameroonFgrnFilterFragment.CallBack,
    VegaCameroonFgrnGradesFragment.CallBack, VegaCameroonFgrnAddWeightAndLotsFragmentEdit.CallBack,
    VegaCameroonFgrnPODetailsFragment.CallBack, VegaCameroonFgrnShiftSelectionFragment.CallBack,
    VegaCameroonFgrnSummaryFragment.CallBack, VegaCameroonRMINSelectPoFragment.CallBack,
    VegaCameroonRMINGradesFragment.CallBack, VegaCameroonRMINAddLotFragment.CallBack,
    CameroonAddLotsListener, VegaCocoaAddPalletFragment.CallBackPallet, VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaCameroonFgrnBagConsumptionFragment.CallBack, VegaCameroonRMINAddWeightAndLotsFragment.CallBack,
    VegaCameroonRMINShiftSelectFragment.CallBack, VegaCameroonRMINEditLotFragment.CallBack,
    VegaCameroonRminSummaryFragment.CallBack {
    override val layoutResourceId: Int =
        R.layout.activity_vega_cameroon_processing
    private val mTAG = VegaCameroonProcessingActivity::class.java.canonicalName
    private var isLastBack: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCameroonProcessingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaCameroonProcessingSelectTypeFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flProcessing,
            allowBackStack = flag
        )
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

    private fun backNaviagation() {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaCameroonFgrnAddWeightAndLotsFragment -> {
                when (isLastBack) {
                    true -> super.onBackPressed()
                    else -> fragment.onBackPressed()
                }
                closeSocket()
            }
            is VegaCameroonRMINAddLotFragment -> {
                fragment.saveLotDetails()
                when (isLastBack) {
                    true -> super.onBackPressed()
                    else -> fragment.onBackPressed()
                }
            }
            is VegaCameroonRMINAddWeightAndLotsFragment -> {
                super.onBackPressed()
                fragment.onBackPressed()
                closeSocket()
            }
            is VegaCameroonRMINEditLotFragment -> {
                fragment.moveToShiftSelect()
            }
            else -> super.onBackPressed()
        }
    }

    override fun replaceFragment(fragment: String, model: VegaCocoaRminProcessing) {
        when (fragment) {
            RMIN -> {
                displayFragment(VegaCameroonRMINSelectPoFragment.newInstance(), true)
            }
            FGRN -> {
                displayFragment(VegaCameroonFgrnPODetailsFragment.newInstance(), true)
            }
        }
    }

    override fun replaceFgrnFragment(fragment: String, bundle: Bundle) {
        when (fragment) {
            FRAG_ADD_BAG_WEIGHT -> displayFragment(VegaSweepingWeightEntryFragment.newInstance(bundle, false), true)
        }
    }

    override fun replaceFgrnFragment(
        fragment: String,
        model: VegaCoffeeFgrnItems,
        id: String,
        isThirdPartyMaterial: Boolean,
        material: String
    ) {
        when (fragment) {
            FRAG_CREATE_LOT -> {
                displayFragment(
                    VegaCameroonFgrnAssignLotFragment.newInstance(model, id, isThirdPartyMaterial, material),
                    true
                )
            }
            FRAG_SHIFT -> {
                displayFragment(VegaCameroonFgrnShiftSelectionFragment.newInstance(model), true)
            }
        }
    }

    override fun replaceFragment(fragment: String, data: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaCameroonRMINAddLotFragment -> {
                fragment.updateAddWeight(data)
            }
            is VegaCameroonRMINEditLotFragment -> {
                fragment.updateAddWeight(data)
            }
        }
    }


    override fun replaceFgrnFragment(fragment: String, model: VegaCoffeeFgrnItems, id: String) {
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            FRAG_PENDING -> {
                when (fragment1) {
                    is VegaCameroonFgrnPODetailsFragment -> displayFragment(
                        VegaCameroonFgrnPendingFragment.newInstance(model),
                        true
                    )
                }
            }
            FRAG_GRADES -> {
                when (fragment1) {
                    is VegaCameroonFgrnPODetailsFragment -> displayFragment(
                        VegaCameroonFgrnGradesFragment.newInstance(
                            model,
                            id
                        ), true
                    )
                    is VegaCameroonFgrnPendingFragment -> displayFragment(
                        VegaCameroonFgrnGradesFragment.newInstance(
                            model,
                            id
                        ), true
                    )
                }
            }
            FRAG_ADD_WEIGHT -> {
                isLastBack = false
                displayFragment(
                    VegaCameroonFgrnAddWeightAndLotsFragment.newInstance(
                        model,
                        id,
                        false
                    ), true
                )
                initBt(object : BtObserve {
                    override fun valueObserve(btValue: String) {
                        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
                        when (fragment) {
                            is VegaCameroonFgrnAddWeightAndLotsFragment -> {
                                val fragment1 = VegaCocoaAddPalletFragment()
                                fragment1.updateBtWeight(btValue)
                            }
                            is VegaCameroonFgrnAddWeightAndLotsFragmentEdit -> {
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
            FRAG_BAG_CONSUMP -> {
                displayFragment(VegaCameroonFgrnBagConsumptionFragment.newInstance(model), true)
            }
            FRAG_SUMMARY -> displayFragment(VegaCameroonFgrnSummaryFragment.newInstance(model), true)
            FRAG_ADD_WEIGHT_EDIT -> displayFragment(
                VegaCameroonFgrnAddWeightAndLotsFragmentEdit.newInstance(
                    model,
                    id,
                    true
                ), true
            )
        }
    }

    override fun replaceFragment(
        fragment: String,
        flag: Boolean,
        model: VegaCoffeeRminProcessing,
        materialCode: String, batchNumer: String
    ) {
        when (fragment) {
            SHIFT -> {
                displayFragment(VegaCameroonRMINShiftSelectFragment.newInstance(flag, model), true)
            }

            FRAG_ADD_WEIGHT -> {
                isLastBack = false
                displayFragment(
                    VegaCameroonRMINAddWeightAndLotsFragment.newInstance(
                        model,
                        false,
                        materialCode,
                        batchNumer
                    ), true
                )
                initBt(object : BtObserve {
                    override fun valueObserve(btValue: String) {
                        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
                        when (fragment) {
                            is VegaCameroonRMINAddWeightAndLotsFragment -> {
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
            SUMMARY -> displayFragment(VegaCameroonRminSummaryFragment.newInstance(model), true)
        }
    }

    override fun replaceFragment(
        fragment: String,
        flag: Boolean,
        model: VegaCoffeeRminProcessing,
        materialCode: String,
        batchNo: String,
        isThirdParty: Boolean
    ) {
        when (fragment) {
            Lot_List -> displayFragment(
                VegaCameroonRMINLotListFragment.newInstance(
                    model,
                    flag,
                    materialCode,
                    isThirdParty
                ), true
            )
        }
    }

    override fun isLastBack() {
        isLastBack = true
    }

    override fun replaceFGrnFragment(fragFilter: String, bundle: Bundle, fullFilter: ArrayList<String>) {
        when (fragFilter) {
            FRAG_FILTER -> displayFragment(VegaCameroonFgrnFilterFragment.newInstance(bundle, fullFilter), true)
        }
    }

    override fun updateLotDetails(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaCameroonFgrnAddWeightAndLotsFragment -> fragment.updateLotDetails(bundle)
            is VegaCameroonFgrnAddWeightAndLotsFragmentEdit -> fragment.updateLotDetails(bundle)
        }
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaCameroonFgrnAssignLotFragment -> fragment.applyFilter(bundle)

        }
    }

    override fun replaceFragment(fragment: String, model: VegaCoffeeRminProcessing) {
        when (fragment) {
            RMIN_GRADES -> displayFragment(
                VegaCameroonRMINGradesFragment.newInstance(model, fragment), true
            )
            SUMMARY -> {
                displayFragment(VegaCameroonRminSummaryFragment.newInstance(model), true)
            }
        }
    }

    override fun addedLots(lots: ArrayList<VegaCoffeeRminLots>) {

        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (lotAddFragment) {
            is VegaCameroonRMINAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
            is VegaCameroonRMINEditLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
        }
    }

    override fun replaceFragment(fragment: String, flag: Boolean, model: VegaCoffeeRminProcessing) {
        when (fragment) {
            ADDLOT -> {
                isLastBack = false
                displayFragment(
                    VegaCameroonRMINAddLotFragment.newInstance(false, model), true
                )
            }


        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaCameroonFgrnAddWeightAndLotsFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
            is VegaCameroonFgrnAddWeightAndLotsFragmentEdit -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
            is VegaCameroonRMINAddWeightAndLotsFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }

        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaCameroonFgrnAddWeightAndLotsFragment -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }
            is VegaCameroonFgrnAddWeightAndLotsFragmentEdit -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }
            is VegaCameroonRMINAddWeightAndLotsFragment -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }

        }
    }

    override fun replaceFragment(
        fragment: String,
        flag: Boolean,
        fgrnMaterialCode: String,
        model: VegaCoffeeRminProcessing
    ) {
        when (fragment) {
            EDITLOT -> displayFragment(
                VegaCameroonRMINEditLotFragment.newInstance(
                    fgrnMaterialCode,
                    model
                ), true
            )
            SHIFT ->
                displayFragment(VegaCameroonRMINShiftSelectFragment.newInstance(flag, model), true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeSocket()
    }

}
