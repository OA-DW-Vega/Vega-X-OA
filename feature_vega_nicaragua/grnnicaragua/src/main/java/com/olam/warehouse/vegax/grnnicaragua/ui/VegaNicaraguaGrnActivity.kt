package com.olam.warehouse.vegax.grnnicaragua.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT_GRN
import com.olam.warehouse.presentation.utils.UIUtils.TRANS_GRN
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.grnnicaragua.R
import com.olam.warehouse.vegax.grnnicaragua.di.injectNicaraguaGrnFeature
import com.olam.warehouse.vegax.grnnicaragua.ui.fixed.VegaNicaraguaGrnFixedTransactionDetails
import com.olam.warehouse.vegax.grnnicaragua.ui.ptbf.VegaNicaraguaGrnPTBFPriceCalculationFragment
import com.olam.warehouse.vegax.grnnicaragua.ui.ptbf.VegaNicaraguaGrnPTBFSummaryFragment
import com.olam.warehouse.vegax.grnnicaragua.ui.reprint.VegaNicaraguaGrnReprintFragment
import com.olam.warehouse.vegax.grnnicaragua.ui.spot.*
import com.olam.warehouse.vegax.grnnicaragua.ui.transaction.VegaNicaraguaTransactionFragment
import com.olam.warehouse.vegax.grnnicaragua.utils.*


/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */
class VegaNicaraguaGrnActivity : HomeBaseActivity(), VegaNicaraguaGrnTypeFragment.CallBack,
    VegaNicaraguaGrnSpotTransactionDetails.CallBack, VegaNicaraguaGrnSpotWeighment.CallBack,
    VegaNicaraguaGrnPriceCalculationFragment.CallBack,
    VegaNicaraguaGrnSpotQualityParameterFragment.CallBack,
    VegaNicaraguaGrnSpotWeightEntryFragment.CallBackAddBags, VegaNicaraguaGrnPTBFPriceCalculationFragment.CallBack,
    VegaNicaraguaTransactionFragment.CallBack, VegaNicaraguaGrnSpotSummaryFragment.CallBack,
    VegaNicaraguaGrnFixedTransactionDetails.CallBack {

    private val mTAG = VegaNicaraguaGrnActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_nicaragua_grn


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNicaraguaGrnFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if (intent.hasExtra(TRANS_GRN))
            displayFragment(VegaNicaraguaTransactionFragment(), false)
        else if (intent.hasExtra(REPRINT_GRN))
            displayFragment(VegaNicaraguaGrnReprintFragment(), false)
        else
            displayFragment(VegaNicaraguaGrnTypeFragment(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flNicaraguaGrn, allowBackStack = flag)
    }

    override fun replaceFragment(grnType: String) {
        when (grnType) {
            GRN_SPOT -> displayFragment(
                VegaNicaraguaGrnSpotTransactionDetails.newInstance(grnType, VegaReceiving()),
                true
            )
            GRN_PTBF -> displayFragment(
                VegaNicaraguaGrnSpotTransactionDetails.newInstance(grnType, VegaReceiving()),
                true
            )
            GRN_FIXED -> displayFragment(
                VegaNicaraguaGrnFixedTransactionDetails.newInstance(grnType, VegaReceiving()),
                true
            )
        }
    }

    override fun replaceFragment(moveFrag: String, grnType: String, receivingData: VegaReceiving) {
        when (grnType) {
            GRN_SPOT -> displayFragment(VegaNicaraguaGrnSpotWeighment.newInstance(receivingData), true)
            GRN_FIXED -> displayFragment(VegaNicaraguaGrnSpotWeighment.newInstance(receivingData), true)
        }
    }

    override fun replaceFragment(moveFrag: String, data: Any) {
        when (moveFrag) {
            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaNicaraguaGrnSpotWeightEntryFragment.newInstance(data as Bundle),
                true
            )
            GRN_QUALITY -> displayFragment(
                VegaNicaraguaGrnSpotQualityParameterFragment.newInstance(data as VegaReceiving),
                true
            )
            GRN_SPOT -> {
                val dataRec = data as VegaReceiving
                when {
                    dataRec.grnType?.contains("spot", true) == true -> displayFragment(
                        VegaNicaraguaGrnSpotTransactionDetails.newInstance(
                            GRN_SPOT,
                            data
                        ), true
                    )
                    dataRec.grnType?.contains("ptbf", true) == true -> displayFragment(
                        VegaNicaraguaGrnSpotTransactionDetails.newInstance(
                            GRN_PTBF,
                            data
                        ), true
                    )
                }
            }
            GRN_FIXED -> displayFragment(
                VegaNicaraguaGrnFixedTransactionDetails.newInstance(
                    GRN_FIXED,
                    data as VegaReceiving
                ), true
            )


        }
    }

    override fun replaceFragment(moveFrag: String, data: Any, qualityParameterList: ArrayList<VegaQualityParameter?>) {
        when (moveFrag) {
            FRAG_PRICING -> displayFragment(
                VegaNicaraguaGrnPriceCalculationFragment.newInstance(data as VegaReceiving, qualityParameterList),
                true
            )
            FRAG_PTBF_PRICING -> displayFragment(
                VegaNicaraguaGrnPTBFPriceCalculationFragment.newInstance(data as VegaReceiving, qualityParameterList),
                true
            )
            FRAG_SUMMARY -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flNicaraguaGrn)
                when (fragment) {
                    is VegaNicaraguaTransactionFragment -> displayFragment(
                        VegaNicaraguaGrnSummaryFragment.newInstance(data as VegaReceiving, qualityParameterList),
                        true
                    )
                }
            }
        }
    }

    override fun updateBagWeight(bagMaterial: VegaNicaraguaWeighmentBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flNicaraguaGrn)
        when (fragment) {
            is VegaNicaraguaGrnSpotWeighment -> {
                fragment.updateBagWeight(bagMaterial)
            }
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

    private fun backNaviagation() {
        val fragment = supportFragmentManager.findFragmentById(R.id.flNicaraguaGrn)
        when (fragment) {
            is VegaNicaraguaTransactionFragment -> {
                val starSync = PreferenceHelper.get(Constants.START_SYNC, false)
                when (starSync) {
                    true -> showAlertDialog()
                    else -> super.onBackPressed()
                }
            }
            /*is VegaNicaraguaGrnSpotTransactionDetails ->{
                val isEditTrans = PreferenceHelper.get(Constants.IS_EDIT_TRANS, false)
                when (isEditTrans) {
                    true -> {
                        val valueEdited = PreferenceHelper.get(Constants.IS_EDIT_TRANS_VALUE_CHANGED, false)
                        when(valueEdited){
                            true ->editBackNavigationCheck()
                            else ->super.onBackPressed()
                        }
                    }
                    else -> super.onBackPressed()
                }
            }
            is VegaNicaraguaGrnFixedTransactionDetails ->{
                val isEditTrans = PreferenceHelper.get(Constants.IS_EDIT_TRANS, false)
                when (isEditTrans) {
                    true -> {
                        val valueEdited = PreferenceHelper.get(Constants.IS_EDIT_TRANS_VALUE_CHANGED, false)
                        when(valueEdited){
                            true ->editBackNavigationCheck()
                            else ->super.onBackPressed()
                        }
                    }
                    else -> super.onBackPressed()
                }
            }*/
            is VegaNicaraguaGrnPTBFPriceCalculationFragment -> {
                val isEditTrans = PreferenceHelper.get(Constants.IS_EDIT_TRANS, false)
                when (isEditTrans) {
                    true -> {
                        val valueEdited = PreferenceHelper.get(Constants.IS_EDIT_TRANS_VALUE_CHANGED, false)
                        when (valueEdited) {
                            true -> editBackNavigationCheck()
                            else -> super.onBackPressed()
                        }
                    }
                    else -> super.onBackPressed()
                }
            }
            is VegaNicaraguaGrnPriceCalculationFragment -> {
                val isEditTrans = PreferenceHelper.get(Constants.IS_EDIT_TRANS, false)
                when (isEditTrans) {
                    true -> {
                        val valueEdited = PreferenceHelper.get(Constants.IS_EDIT_TRANS_VALUE_CHANGED, false)
                        when (valueEdited) {
                            true -> editBackNavigationCheck()
                            else -> super.onBackPressed()
                        }
                    }
                    else -> super.onBackPressed()
                }
            }
            is VegaNicaraguaGrnSpotQualityParameterFragment -> {
                val isEditTrans = PreferenceHelper.get(Constants.IS_EDIT_TRANS, false)
                when (isEditTrans) {
                    true -> {
                        val valueEdited = PreferenceHelper.get(Constants.IS_EDIT_TRANS_VALUE_CHANGED, false)
                        when (valueEdited) {
                            true -> editBackNavigationCheck()
                            else -> super.onBackPressed()
                        }
                    }
                    else -> super.onBackPressed()
                }
            }
            is VegaNicaraguaGrnSpotWeighment -> {
                val isEditTrans = PreferenceHelper.get(Constants.IS_EDIT_TRANS, false)
                when (isEditTrans) {
                    true -> {
                        val valueEdited = PreferenceHelper.get(Constants.IS_EDIT_TRANS_VALUE_CHANGED, false)
                        when (valueEdited) {
                            true -> editBackNavigationCheck()
                            else -> super.onBackPressed()
                        }
                    }
                    else -> {
                        super.onBackPressed()
                        val fragment1 = supportFragmentManager.findFragmentById(R.id.flNicaraguaGrn)
                        when (fragment1) {
                            is VegaNicaraguaGrnSpotTransactionDetails -> fragment1.transUpdate()
                        }
                    }
                }
            }
            else -> super.onBackPressed()
        }
    }

    private fun editBackNavigationCheck() {
        MaterialDialog(this).show {
            message(R.string.edit_save)
            getMetirialCustomView(this, getString(R.string.ok), "", { dismiss() }, { dismiss() })
        }
    }

    override fun replaceFragment(moveFrag: String, data: Bundle) {
        when (moveFrag) {
            FRAG_SPOT_GRN_SUMMARY -> {
                displayFragment(
                    VegaNicaraguaGrnSpotSummaryFragment.newInstance(data),
                    true
                )
            }
            FRAG_PTBF_GRN_SUMMARY -> {
                displayFragment(
                    VegaNicaraguaGrnPTBFSummaryFragment.newInstance(data),
                    true
                )
            }
        }
    }

    private fun showAlertDialog() {
        MaterialDialog(this).show {
            message(R.string.sync_started)
            getMetirialCustomView(this, getString(R.string.ok), "", { dismiss() }, { dismiss() })
        }
    }
}


