package com.olam.warehouse.vegax.offloadingecuador.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.master.vega.entity.TrackTraceFarmerModel
import com.olam.warehouse.master.vega.entity.TrackTraceSourceLotDetails
import com.olam.warehouse.login.ui.common.VegaTrackTraceListener
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.TrackTraceTransactionIdDetails
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.offloadingecuador.R
import com.olam.warehouse.vegax.offloadingecuador.di.injectEcuadorOffloadingFeature
import com.olam.warehouse.vegax.offloadingecuador.ui.offline.VegaEcuadorOffloadingOfflineSummary
import com.olam.warehouse.vegax.offloadingecuador.ui.supplier.VegaEccuadorOffloadingListener
import com.olam.warehouse.vegax.offloadingecuador.ui.supplier.VegaEcuadorOffloadingSupplierFragment
import com.olam.warehouse.vegax.offloadingecuador.ui.supplier.VegaEcuadorOffloadingSupplierSummaryFragment
import com.olam.warehouse.vegax.offloadingecuador.ui.supplier.VegaEcuadorOffloadingWBListFragment
import com.olam.warehouse.vegax.offloadingecuador.ui.supplier.VegaEcuadorOffloadingWeighmentTypeFragment
import com.olam.warehouse.vegax.offloadingecuador.ui.supplier.VegaEcuadorOffloadingWeightEntryFragment
import com.olam.warehouse.vegax.offloadingecuador.utils.CONSIGNMENT
import com.olam.warehouse.vegax.offloadingecuador.utils.FRAG_ADD_BAG_WEIGHT
import com.olam.warehouse.vegax.offloadingecuador.utils.OFFLOADING_OFFLINE
import com.olam.warehouse.vegax.offloadingecuador.utils.OFFLOADING_SUMMARY_FRAG
import com.olam.warehouse.vegax.offloadingecuador.utils.WEIGHBRIDGE
import com.olam.warehouse.vegax.offloadingecuador.utils.WEIGHSCALE
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaEcuadorOffloadingActivity : HomeBaseActivity(),
    VegaEcuadorOffloadingWeightEntryFragment.CallBackAddBags, VegaEccuadorOffloadingListener, VegaTrackTraceListener {
    private val mTAG = VegaEcuadorOffloadingActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_vega_ecuador_offloading
    private val vm: VegaEcuadorOffloadingViewModel by viewModel()
    var procurementType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectEcuadorOffloadingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        //vm.clearBagDetails()
         procurementType = intent.getStringExtra(Constants.PROCUREMENT_TYPE).toString()
       /* displayFragment(
            VegaEcuadorOffloadingSupplierFragment.newInstance(
                VegaReceiving(),
                ArrayList<VegaEcuadorOffloadingBagMaterial>(),
                procurementType.toString()
            ), false
        )*/
        displayFragment(VegaEcuadorOffloadingWeighmentTypeFragment.newInstance(),false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flEcuadorOffloading,
            allowBackStack = flag
        )
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            FRAG_ADD_BAG_WEIGHT -> displayFragment(VegaEcuadorOffloadingWeightEntryFragment.newInstance(data as Bundle), true)
        }
    }

    override fun replaceFragment(moveFrag: String) {
        when (moveFrag) {
            OFFLOADING_OFFLINE -> displayFragment(VegaEcuadorOffloadingOfflineSummary.newInstance(), true)
            WEIGHBRIDGE -> displayFragment(VegaEcuadorOffloadingWBListFragment.newInstance(moveFrag), false)
            WEIGHSCALE ->displayFragment(VegaEcuadorOffloadingSupplierFragment.newInstance(
                VegaReceiving(),
                ArrayList<VegaEcuadorOffloadingBagMaterial>(),
                procurementType.toString(),VegaQualityWBDetails(),
                moveFrag
            ),false)
        }
    }



    override fun replaceFragment(moveFrag: String, weighBridgeData: VegaQualityWBDetails) {
        when(moveFrag){
            WEIGHBRIDGE -> displayFragment(
                VegaEcuadorOffloadingSupplierFragment.newInstance(
                    VegaReceiving(),
                    ArrayList<VegaEcuadorOffloadingBagMaterial>(),
                    procurementType.toString(), weighBridgeData, moveFrag
                ), false
            )
        }

    }

    override fun replaceFragment(
        moveFrag: String,
        receivingData: VegaReceiving,
        mReceiving: MutableList<VegaReceiving>,
        bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>
    ) {
        when (moveFrag) {
            OFFLOADING_SUMMARY_FRAG -> displayFragment(
                VegaEcuadorOffloadingSupplierSummaryFragment.newInstance(
                    receivingData,
                    mReceiving as ArrayList<VegaReceiving>,
                    bagList
                ), true
            )
        }
    }

    override fun updateBagWeight(bagMaterial: VegaEcuadorOffloadingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorOffloading)
        when (fragment) {
            is VegaEcuadorOffloadingSupplierFragment -> {
                fragment.updateBagWeight(bagMaterial)
            }
        }
    }

    override fun isVendor(flag: Boolean) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorOffloading)
        when (fragment) {
            is VegaEcuadorOffloadingSupplierFragment -> {
                fragment.isVendor(flag)
            }
        }
    }

    override fun isComplaint(status: Int) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorOffloading)
        when (fragment) {
            is VegaEcuadorOffloadingSupplierFragment -> {
                fragment.isComplaint(status)
            }
        }
    }

    override fun updateSourceLotDetails(sourceLotDetails: TrackTraceSourceLotDetails) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorOffloading)
        when (fragment) {
            is VegaEcuadorOffloadingSupplierFragment -> {
                fragment.updateSourceLotDetails(sourceLotDetails)
            }
        }
    }

    override fun updateTransactionIdDetails(transactionIdDetails: TrackTraceTransactionIdDetails) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorOffloading)
        when (fragment) {
            is VegaEcuadorOffloadingSupplierFragment -> {
                fragment.updateFarmerlessTransactionDetails(transactionIdDetails)
            }
        }
    }

    override fun updateFarmerDetails(farmerListDetails: ArrayList<TrackTraceFarmerModel>) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flEcuadorOffloading)
        when (fragment) {
            is VegaEcuadorOffloadingSupplierFragment -> {
                fragment.updateFarmerListDetails(farmerListDetails)
            }
        }
    }


}
