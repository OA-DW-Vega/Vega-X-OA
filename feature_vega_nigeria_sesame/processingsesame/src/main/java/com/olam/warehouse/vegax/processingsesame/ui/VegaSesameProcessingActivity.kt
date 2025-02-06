package com.olam.warehouse.vegax.processingsesame.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.processingsesame.R
import com.olam.warehouse.vegax.processingsesame.di.injectSesameProcessingFeature
import com.olam.warehouse.vegax.processingsesame.ui.fgrn.*
import com.olam.warehouse.vegax.processingsesame.ui.rmin.*
import com.olam.warehouse.vegax.processingsesame.utils.*

class VegaSesameProcessingActivity : HomeBaseActivity(), VegaSesameProcessingSelectTypeFragment.CallBack,
    VegaSesameFgrnPendingFragment.CallBack, VegaSesameFgrnAddWeightAndLotsFragment.CallBack,
    VegaSesameFgrnAssignLotFragment.CallBack, VegaSesameFgrnFilterFragment.CallBack,
    VegaSesameFgrnGradesFragment.CallBack, VegaSesameFgrnAddWeightAndLotsFragmentEdit.CallBack,
    VegaSesameFgrnPODetailsFragment.CallBack, VegaSesameFgrnShiftSelectionFragment.CallBack,
    VegaSesameFgrnSummaryFragment.CallBack, VegaSesameRMINSelectPoFragment.CallBack,
    VegaSesameRMINGradesFragment.CallBack, VegaSesameRMINAddLotFragment.CallBack,
    SesameAddLotsListener, VegaCocoaAddPalletFragment.CallBackPallet, VegaSweepingWeightEntryFragment.CallBackAddBags,
    VegaSesameFgrnBagConsumptionFragment.CallBack, VegaSesameRMINAddWeightAndLotsFragment.CallBack,
    VegaSesameRMINShiftSelectFragment.CallBack, VegaSesameRMINEditLotFragment.CallBack,
    VegaSesameRminSummaryFragment.CallBack {
    override val layoutResourceId: Int =
        R.layout.activity_vega_sesame_processing
    private val mTAG = VegaSesameProcessingActivity::class.java.canonicalName
    private var isLastBack: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectSesameProcessingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaSesameProcessingSelectTypeFragment.newInstance(), false)
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
            is VegaSesameFgrnAddWeightAndLotsFragment -> {
                when (isLastBack) {
                    true -> super.onBackPressed()
                    else -> fragment.onBackPressed()
                }
            }
            is VegaSesameRMINAddLotFragment -> {
                fragment.saveLotDetails()
                when (isLastBack) {
                    true -> super.onBackPressed()
                    else -> fragment.onBackPressed()
                }
            }
            is VegaSesameRMINAddWeightAndLotsFragment -> {
                super.onBackPressed()
                fragment.onBackPressed()
            }
            is VegaSesameRMINEditLotFragment -> {
                fragment.moveToShiftSelect()
            }
            else -> super.onBackPressed()
        }
    }

    override fun replaceFragment(fragment: String, model: VegaCocoaRminProcessing) {
        when (fragment) {
            RMIN -> {
                displayFragment(VegaSesameRMINSelectPoFragment.newInstance(), true)
            }
            FGRN -> {
                displayFragment(VegaSesameFgrnPODetailsFragment.newInstance(), true)
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
        material: String,
        vegaStage: VegaProcessingStage
    ) {
        when (fragment) {
            FRAG_CREATE_LOT -> {
                displayFragment(
                    VegaSesameFgrnAssignLotFragment.newInstance(model, id, isThirdPartyMaterial, material,vegaStage),
                    true
                )
            }
            FRAG_SHIFT -> {
                displayFragment(VegaSesameFgrnShiftSelectionFragment.newInstance(model,vegaStage), true)
            }
        }
    }

    override fun replaceFragment(fragment: String, data: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaSesameRMINAddLotFragment -> {
                fragment.updateAddWeight(data)
            }
            is VegaSesameRMINEditLotFragment -> {
                fragment.updateAddWeight(data)
            }
        }
    }

    /*override fun replaceFragment(fragment: String, model: VegaCoffeeRminProcessing, id: String) {
        displayFragment(VegaCoffeeRMINShiftSelectFragment.newInstance(false, model), true)
    }*/

    override fun replaceFgrnFragment(fragment: String, model: VegaCoffeeFgrnItems, id: String, vegaStage: VegaProcessingStage) {
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            FRAG_PENDING -> {
                when (fragment1) {
                    is VegaSesameFgrnPODetailsFragment -> displayFragment(
                        VegaSesameFgrnPendingFragment.newInstance(model,vegaStage),
                        true
                    )
                }
            }
            FRAG_GRADES -> {
                when (fragment1) {
                    is VegaSesameFgrnPODetailsFragment -> displayFragment(
                        VegaSesameFgrnGradesFragment.newInstance(
                            model,
                            id,vegaStage
                        ), true
                    )
                    is VegaSesameFgrnPendingFragment -> displayFragment(
                        VegaSesameFgrnGradesFragment.newInstance(
                            model,
                            id,vegaStage
                        ), true
                    )
                }
            }
            FRAG_ADD_WEIGHT -> {
                isLastBack = false
                displayFragment(VegaSesameFgrnAddWeightAndLotsFragment.newInstance(model, id, false,vegaStage), true)
            }
            FRAG_BAG_CONSUMP -> {
                displayFragment(VegaSesameFgrnBagConsumptionFragment.newInstance(model), true)
            }
            FRAG_SUMMARY -> displayFragment(VegaSesameFgrnSummaryFragment.newInstance(model,vegaStage), true)
            FRAG_ADD_WEIGHT_EDIT -> displayFragment(
                VegaSesameFgrnAddWeightAndLotsFragmentEdit.newInstance(
                    model,
                    id,
                    true,vegaStage
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
                displayFragment(VegaSesameRMINShiftSelectFragment.newInstance(flag, model), true)
            }

            FRAG_ADD_WEIGHT -> {
                isLastBack = false
                displayFragment(
                    VegaSesameRMINAddWeightAndLotsFragment.newInstance(
                        model,
                        false,
                        materialCode,
                        batchNumer
                    ), true
                )
            }
            SUMMARY -> displayFragment(VegaSesameRminSummaryFragment.newInstance(model), true)
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
                VegaSesameRMINLotListFragment.newInstance(
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
            FRAG_FILTER -> displayFragment(VegaSesameFgrnFilterFragment.newInstance(bundle, fullFilter), true)
        }
    }

    override fun updateLotDetails(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaSesameFgrnAddWeightAndLotsFragment -> fragment.updateLotDetails(bundle)
            is VegaSesameFgrnAddWeightAndLotsFragmentEdit -> fragment.updateLotDetails(bundle)
        }
    }

    override fun applyFilter(bundle: Bundle) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (fragment) {
            is VegaSesameFgrnAssignLotFragment -> fragment.applyFilter(bundle)

        }
    }

    override fun replaceFragment(fragment: String, model: VegaCoffeeRminProcessing) {
        when (fragment) {
            RMIN_GRADES -> displayFragment(
                VegaSesameRMINGradesFragment.newInstance(model, fragment), true
            )
            SUMMARY -> {
                displayFragment(VegaSesameRminSummaryFragment.newInstance(model), true)
            }
        }
    }

    override fun addedLots(lots: ArrayList<VegaCoffeeRminLots>) {

        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flProcessing)
        when (lotAddFragment) {
            is VegaSesameRMINAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
            is VegaSesameRMINEditLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
        }
    }

    override fun replaceFragment(fragment: String, flag: Boolean, model: VegaCoffeeRminProcessing) {
        when (fragment) {
            ADDLOT -> {
                isLastBack = false
                displayFragment(
                    VegaSesameRMINAddLotFragment.newInstance(false, model), true
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
            is VegaSesameFgrnAddWeightAndLotsFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
            is VegaSesameFgrnAddWeightAndLotsFragmentEdit -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
            is VegaSesameRMINAddWeightAndLotsFragment -> {
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
            is VegaSesameFgrnAddWeightAndLotsFragment -> {
                fragment.updateBagWeight(processToProcessingFGRNMaterial(bagMaterial))
            }
            is VegaSesameFgrnAddWeightAndLotsFragmentEdit -> {
                fragment.updateBagWeight(processToProcessingFGRNMaterial(bagMaterial))
            }
            is VegaSesameRMINAddWeightAndLotsFragment -> {
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
            EDITLOT -> displayFragment(VegaSesameRMINEditLotFragment.newInstance(fgrnMaterialCode, model), true)
            SHIFT ->
                displayFragment(VegaSesameRMINShiftSelectFragment.newInstance(flag, model), true)
        }
    }

    override fun replaceFgrnFragment(fragment: String, fgrnItem: VegaCoffeeFgrnItems, fgrnId: String) {
        TODO("Not yet implemented")
    }


}
