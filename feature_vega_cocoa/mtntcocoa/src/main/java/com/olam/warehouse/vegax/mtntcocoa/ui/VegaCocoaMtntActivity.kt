package com.olam.warehouse.vegax.mtntcocoa.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.mtntcocoa.R
import com.olam.warehouse.vegax.mtntcocoa.data.domain.model.*
import com.olam.warehouse.vegax.mtntcocoa.databinding.ActivityCocoaDispatchBinding
import com.olam.warehouse.vegax.mtntcocoa.di.injectCocoaDispatchFeature
import com.olam.warehouse.vegax.mtntcocoa.ui.mtnt.VegaMtntAddLotFragment
import com.olam.warehouse.vegax.mtntcocoa.ui.mtnt.VegaMtntTruckListFragment
import com.olam.warehouse.vegax.mtntcocoa.ui.transaction.VegaCocoaMtntTransactionFragment
import com.olam.warehouse.vegax.mtntcocoa.ui.transaction.VegaCocoaNoWeighmentTransactionSummaryFragment
import com.olam.warehouse.vegax.mtntcocoa.ui.virtualmtnt.*
import com.olam.warehouse.vegax.mtntcocoa.ui.weighscale.*
import com.olam.warehouse.vegax.mtntcocoa.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class VegaCocoaMtntActivity : HomeBaseActivity(), CallBack, VegaCocoaAddLotListener,VegaCocoaWeighscaleAddLotListener, VegaCocoaAddPalletFragment.CallBackPallet, VegaSweepingWeightEntryFragment.CallBackAddBags {
    private val mTAG = VegaCocoaMtntActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_cocoa_dispatch
    private var isVirtual = false
    private var isTransaction = false
    private val vm: VegaCocoaMtntViewModel by viewModel()
    private lateinit var binding: ActivityCocoaDispatchBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCocoaDispatchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        injectCocoaDispatchFeature()
        initNavigationView()
        isVirtual = intent.getBooleanExtra("isVirtual", false)
        isTransaction = intent.getBooleanExtra("trans_mtnt", false)

        initUI()
    }

    private fun initUI() {
        vm.configItems.observe(this, Observer {
            updateConfigItems(it)
        })
        if (isTransaction) {
            displayFragment(VegaCocoaMtntTransactionFragment.newInstance(), false)
            binding.tvTitle.visibility = View.GONE
        } else if (isVirtual) {
            displayFragment(VegaCocoaMtntConsignmentFragment.newInstance(null), false)
            binding.tvTitle.visibility = View.GONE
        } else {
            /*if (intent?.hasExtra(UIUtils.MTNT_DATA)!!) {
                displayFragment(
                    VegaMtntAddLotFragment.newInstance(intent?.getParcelableExtra(UIUtils.MTNT_DATA) as VegaCocoaDispatchWB),
                    false
                )
            } else
            {
                tv_title.visibility = View.GONE
                vm.getConfigItems(UserRoles.MTNT.role)
            }*/
            //tv_title.visibility = View.GONE
            displayFragment(VegaCocoaMtntSelectDispatchTypeFragment.newInstance(), false)
            // displayFragment(VegaCocoaMtntWeighscaleConsignmentFragment.newInstance(), false)
            // displayFragment(VegaMtntTruckListFragment.newInstance(), false)
        }

    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        binding.tvTitle.visibility = View.GONE
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
            "Add_Lot_Refresh" -> displayFragment(VegaMtntAddLotFragment.newInstance(data as VegaCocoaDispatchWB), false)
            "Add_Lot" -> displayFragment(VegaMtntAddLotFragment.newInstance(data as VegaCocoaDispatchWB), true)
            "Summary" -> displayFragment(VegaMtntLotSummaryFragment.newInstance(data as VegaCocoaSummary), true)
            NO_WEIGHMENT -> displayFragment(VegaCocoaMtntConsignmentFragment.newInstance(null), true)
            NO_WEIGHMENT_FROM_PENDING -> displayFragment(
                VegaCocoaMtntConsignmentFragment.newInstance(data as VegaCocoaNoWeighmentModel),
                true
            )
            SELECT_DISPATCH_TYPE -> displayFragment(VegaCocoaMtntSelectDispatchTypeFragment.newInstance(), true)
            NO_WEIGHMENT_ADD_LOT -> displayFragment(
                VegaCocoaNoWeighmentAddLotFragment.newInstance(data as VegaCocoaNoWeighmentModel),
                true
            )
            LOT_LIST -> displayFragment(
                VegaCocoaNoWeighmentLotListFragment.newInstance(data as VegaCocoaLotListModel),
                true
            )
            MTNT_LOT_LIST -> displayFragment(
                VegaCocoaNoWeighmentLotListFragment.newInstance(data as VegaCocoaLotListModel),
                true
            )
            ADD_WEIGHT -> displayFragment(
                VegaCocoaNoWeighmentAddWeightFragment.newInstance(data as VegaCocoaNoWeighmentLot),
                true
            )
            FRAG_ADD_BAG_WEIGHT -> displayFragment(
                VegaSweepingWeightEntryFragment.newInstance(data as Bundle, false),
                true
            )
            UPDATE_WEIGHT -> {
                supportFragmentManager.popBackStackImmediate()
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaCocoaNoWeighmentAddLotFragment -> fragment.updateAddWeight(data as VegaCocoaNWBagModel)
                }
            }
            NO_WEIGHMENT_SUMMARY -> displayFragment(
                VegaCocoaNoWeighmentSummaryFragment.newInstance(data as VegaCocoaNoWeighmentModel),
                true
            )
            TRANSACTION_SUMMARY -> displayFragment(
                VegaCocoaNoWeighmentTransactionSummaryFragment.newInstance(data as VegaCocoaNoWeighmentModel),
                true
            )
            MTNT_WEIGHSCALE ->displayFragment(VegaCocoaMtntWeighscaleConsignmentFragment.newInstance(),true)
            WEIGHSCALE_ADD_LOT -> displayFragment(
                VegaCocoaMtntWeighScaleAddLotFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            WEIGHSCALE_LOT_LIST -> displayFragment(VegaCocoaWeighscaleLotListFragment.newInstance(data as VegaCocoaWsLotListModel), true)
            WEIGHSCALE_ADD_WEIGHT -> displayFragment(
                VegaCocoaMtntWeighScalePalletFragment.newInstance(data as VegaCocoaDispatchLots),
                true)

            UPDATE_WEIGHT_WEIGHSCALE -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaCocoaMtntWeighScaleAddLotFragment -> fragment.updateAddWeight(data as VegaCocoaWSBagModel)
                }
            }
            WEIGHSCALE_SUMMARY -> displayFragment(
                VegaCocoaMtntWeighScaleSummaryFragment.newInstance(data as VegaCocoaDispatchWB),
                true
            )
            WEIGHBRIDGE ->displayFragment(
                VegaMtntTruckListFragment.newInstance(),
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
        when (val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)) {
            is VegaMtntAddLotFragment -> {
                fragment.saveLotDetails()
                super.onBackPressed()
            }
            is VegaCocoaNoWeighmentAddLotFragment -> {
                fragment.saveLotDetails()
                supportFragmentManager.popBackStackImmediate()
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaCocoaMtntConsignmentFragment -> fragment.onBackResume()
                }
            }
            is VegaCocoaMtntWeighScalePalletFragment -> {
                super.onBackPressed()
                //fragment.onBackPressed()
            }
            else -> super.onBackPressed()
        }
    }

    override fun addedLots(lots: ArrayList<VegaCocoaNoWeighmentLot>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (lotAddFragment) {
            is VegaCocoaNoWeighmentAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
            is VegaMtntAddLotFragment -> {
             lotAddFragment.updateLotList(lots)
            }

        }
    }

    override fun addedLotsWeighscale(lots: ArrayList<VegaCocoaDispatchLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (lotAddFragment) {

            is VegaCocoaMtntWeighScaleAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCocoaMtntWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCocoaMtntWeighScalePalletFragment -> fragment.updateBagWeight(bagMaterial)
        }
    }
    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {

                ConfigItems.MTNT_WEIGHSCALE.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> {
                            displayFragment(VegaCocoaMtntWeighscaleConsignmentFragment.newInstance(),false)
                          }
                        it.applicable?.contains("N")!! -> {
                            displayFragment(VegaMtntTruckListFragment.newInstance(), false)
                            }
                    }
                }
                          }
        }
    }
}
