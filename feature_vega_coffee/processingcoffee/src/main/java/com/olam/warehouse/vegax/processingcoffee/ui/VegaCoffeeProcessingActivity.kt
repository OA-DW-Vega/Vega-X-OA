package com.olam.warehouse.vegax.processingcoffee.ui

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
import com.olam.warehouse.vegax.processingcoffee.R
import com.olam.warehouse.vegax.processingcoffee.di.injectCoffeeProcessingFeature
import com.olam.warehouse.vegax.processingcoffee.ui.fgrn.*
import com.olam.warehouse.vegax.processingcoffee.ui.rmin.*
import com.olam.warehouse.vegax.processingcoffee.utils.*

class VegaCoffeeProcessingActivity : HomeBaseActivity(), VegaCoffeeProcessingSelectTypeFragment.CallBack,
    VegaCoffeeFgrnPendingFragment.CallBack, VegaCoffeeFgrnAddWeightAndLotsFragment.CallBack,
    VegaCoffeeFgrnAssignLotFragment.CallBack, VegaCoffeeFgrnFilterFragment.CallBack,
    VegaCoffeeFgrnGradesFragment.CallBack, VegaCoffeeFgrnAddWeightAndLotsFragmentEdit.CallBack,
    VegaCoffeeFgrnPODetailsFragment.CallBack, VegaCoffeeFgrnShiftSelectionFragment.CallBack,
    VegaCoffeeFgrnSummaryFragment.CallBack, VegaCoffeeRMINSelectPoFragment.CallBack,
    VegaCoffeeRMINGradesFragment.CallBack, VegaCoffeeRMINAddLotFragment.CallBack,
    CoffeeAddLotsListener, VegaCocoaAddPalletFragment.CallBackPallet, VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaCoffeeFgrnBagConsumptionFragment.CallBack, VegaCoffeeRMINAddWeightAndLotsFragment.CallBack,
    VegaCoffeeRMINShiftSelectFragment.CallBack, VegaCoffeeRMINEditLotFragment.CallBack,
    VegaCoffeeRminSummaryFragment.CallBack {
    override val layoutResourceId: Int =
        R.layout.activity_vega_coffee_processing
    private val mTAG = VegaCoffeeProcessingActivity::class.java.canonicalName
    private var isLastBack: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCoffeeProcessingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaCoffeeProcessingSelectTypeFragment.newInstance(), false)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
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
            is VegaCoffeeFgrnAddWeightAndLotsFragment -> {
                when (isLastBack) {
                    true -> super.onBackPressed()
                    else -> fragment.onBackPressed()
                }
            }
            is VegaCoffeeRMINAddLotFragment -> {
                fragment.saveLotDetails()
                when (isLastBack) {
                    true -> super.onBackPressed()
                    else -> fragment.onBackPressed()
                }
            }
            is VegaCoffeeRMINAddWeightAndLotsFragment -> {
                super.onBackPressed()
                fragment.onBackPressed()
            }
            is VegaCoffeeRMINEditLotFragment -> {
                fragment.moveToShiftSelect()
                // super.onBackPressed()
            }
            else -> super.onBackPressed()
        }
    }

    override fun replaceFragment(fragment: String, model: VegaCocoaRminProcessing) {
        when (fragment) {
            RMIN -> {
                displayFragment(VegaCoffeeRMINSelectPoFragment.newInstance(), true)
            }
            FGRN -> {
                displayFragment(VegaCoffeeFgrnPODetailsFragment.newInstance(), true)
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
                    VegaCoffeeFgrnAssignLotFragment.newInstance(model, id, isThirdPartyMaterial, material),
                    true
                )
            }
            FRAG_SHIFT -> {
                displayFragment(VegaCoffeeFgrnShiftSelectionFragment.newInstance(model), true)
            }
        }
    }

    override fun replaceFragment(fragment: String, data: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaCoffeeRMINAddLotFragment -> {
                fragment.updateAddWeight(data)
            }
            is VegaCoffeeRMINEditLotFragment -> {
                fragment.updateAddWeight(data)
            }
        }
    }

    /*override fun replaceFragment(fragment: String, model: VegaCoffeeRminProcessing, id: String) {
        displayFragment(VegaCoffeeRMINShiftSelectFragment.newInstance(false, model), true)
    }*/

    override fun replaceFgrnFragment(fragment: String, model: VegaCoffeeFgrnItems, id: String) {
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            FRAG_PENDING -> {
                when (fragment1) {
                    is VegaCoffeeFgrnPODetailsFragment -> displayFragment(
                        VegaCoffeeFgrnPendingFragment.newInstance(model),
                        true
                    )
                }
            }
            FRAG_GRADES -> {
                when (fragment1) {
                    is VegaCoffeeFgrnPODetailsFragment -> displayFragment(
                        VegaCoffeeFgrnGradesFragment.newInstance(
                            model,
                            id
                        ), true
                    )
                    is VegaCoffeeFgrnPendingFragment -> displayFragment(
                        VegaCoffeeFgrnGradesFragment.newInstance(
                            model,
                            id
                        ), true
                    )
                }
            }
            FRAG_ADD_WEIGHT -> {
                isLastBack = false
                displayFragment(VegaCoffeeFgrnAddWeightAndLotsFragment.newInstance(model, id, false), true)
            }
            FRAG_BAG_CONSUMP -> {
                displayFragment(VegaCoffeeFgrnBagConsumptionFragment.newInstance(model), true)
            }
            FRAG_SUMMARY -> displayFragment(VegaCoffeeFgrnSummaryFragment.newInstance(model), true)
            FRAG_ADD_WEIGHT_EDIT -> displayFragment(
                VegaCoffeeFgrnAddWeightAndLotsFragmentEdit.newInstance(
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
                displayFragment(VegaCoffeeRMINShiftSelectFragment.newInstance(flag, model), true)
            }

            FRAG_ADD_WEIGHT -> {
                isLastBack = false
                displayFragment(
                    VegaCoffeeRMINAddWeightAndLotsFragment.newInstance(
                        model,
                        false,
                        materialCode,
                        batchNumer
                    ), true
                )
            }
            SUMMARY -> displayFragment(VegaCoffeeRminSummaryFragment.newInstance(model), true)
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
                VegaCoffeeRMINLotListFragment.newInstance(
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
            FRAG_FILTER -> displayFragment(VegaCoffeeFgrnFilterFragment.newInstance(bundle, fullFilter), true)
        }
    }

    override fun updateLotDetails(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaCoffeeFgrnAddWeightAndLotsFragment -> fragment.updateLotDetails(bundle)
            is VegaCoffeeFgrnAddWeightAndLotsFragmentEdit -> fragment.updateLotDetails(bundle)
        }
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaCoffeeFgrnAssignLotFragment -> fragment.applyFilter(bundle)

        }
    }

    override fun replaceFragment(fragment: String, model: VegaCoffeeRminProcessing) {
        when (fragment) {
            RMIN_GRADES -> displayFragment(
                VegaCoffeeRMINGradesFragment.newInstance(model, fragment), true
            )
            SUMMARY -> {
                displayFragment(VegaCoffeeRminSummaryFragment.newInstance(model), true)
            }
        }
    }

    override fun addedLots(lots: ArrayList<VegaCoffeeRminLots>) {

        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (lotAddFragment) {
            is VegaCoffeeRMINAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
            is VegaCoffeeRMINEditLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
        }
    }

    override fun replaceFragment(fragment: String, flag: Boolean, model: VegaCoffeeRminProcessing) {
        when (fragment) {
            ADDLOT -> {
                isLastBack = false
                displayFragment(
                    VegaCoffeeRMINAddLotFragment.newInstance(false, model), true
                )
            }
            /*FRAG_ADD_WEIGHT_EDIT -> displayFragment(
                VegaCoffeeRMINAddWeightAndLotsFragmentEdit.newInstance(
                    model,
                    true
                ), true
            )*/

        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaCoffeeFgrnAddWeightAndLotsFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
            is VegaCoffeeFgrnAddWeightAndLotsFragmentEdit -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
            is VegaCoffeeRMINAddWeightAndLotsFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
            /*is VegaCoffeeRMINAddWeightAndLotsFragmentEdit -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }*/
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaCoffeeFgrnAddWeightAndLotsFragment -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }
            is VegaCoffeeFgrnAddWeightAndLotsFragmentEdit -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }
            is VegaCoffeeRMINAddWeightAndLotsFragment -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }
            /*is VegaCoffeeRMINAddWeightAndLotsFragmentEdit -> {
                fragment.updateBagWeight(processToProcessingMaterial(bagMaterial))
            }*/
        }
    }

    override fun replaceFragment(
        fragment: String,
        flag: Boolean,
        fgrnMaterialCode: String,
        model: VegaCoffeeRminProcessing
    ) {
        when (fragment) {
            EDITLOT -> displayFragment(VegaCoffeeRMINEditLotFragment.newInstance(fgrnMaterialCode, model), true)
            SHIFT ->
                displayFragment(VegaCoffeeRMINShiftSelectFragment.newInstance(flag, model), true)
        }
    }

}
