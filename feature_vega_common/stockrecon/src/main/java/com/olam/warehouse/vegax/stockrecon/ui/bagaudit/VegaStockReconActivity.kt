package com.olam.warehouse.vegax.stockrecon.ui.bagaudit

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.model.VegaStockReconBagDetails
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.stockrecon.R
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaReconReportBundleData
import com.olam.warehouse.vegax.stockrecon.di.injectStockReconFeature
import com.olam.warehouse.vegax.stockrecon.ui.callback.VegaStockCallbackListener
import com.olam.warehouse.vegax.stockrecon.ui.callback.VegaStockReconAddLotsListener
import com.olam.warehouse.vegax.stockrecon.ui.reconreport.VegaReconReportAuditDetailsFragment
import com.olam.warehouse.vegax.stockrecon.ui.reconreport.VegaReconReportAuditListFragment
import com.olam.warehouse.vegax.stockrecon.ui.reconreport.VegaReconReportDatePlantSelectionFragement
import com.olam.warehouse.vegax.stockrecon.ui.reconreport.VegaReconReportReconListFragment
import com.olam.warehouse.vegax.stockrecon.ui.reconreport.VegaReconReportStatisticsFragment
import com.olam.warehouse.vegax.stockrecon.ui.reconreport.VegaReconReportViewPhotoFragment
import com.olam.warehouse.vegax.stockrecon.utils.RECON_REPORT_AUDIT_DETAILS
import com.olam.warehouse.vegax.stockrecon.utils.RECON_REPORT_AUDIT_LIST
import com.olam.warehouse.vegax.stockrecon.utils.RECON_REPORT_DATE_PLANT_SELECTION
import com.olam.warehouse.vegax.stockrecon.utils.RECON_REPORT_RECON_LIST
import com.olam.warehouse.vegax.stockrecon.utils.RECON_REPORT_STATISTICS
import com.olam.warehouse.vegax.stockrecon.utils.RECON_REPORT_VIEW_PHOTO
import com.olam.warehouse.vegax.stockrecon.utils.STOCK_ALL_AUDIT_LIST
import com.olam.warehouse.vegax.stockrecon.utils.STOCK_RECON_ADD_LOT
import com.olam.warehouse.vegax.stockrecon.utils.STOCK_RECON_AUDIT_TYPE_SELECT
import com.olam.warehouse.vegax.stockrecon.utils.STOCK_RECON_BAG_AUDIT
import com.olam.warehouse.vegax.stockrecon.utils.STOCK_RECON_INPROGRESS_COMPLETED
import com.olam.warehouse.vegax.stockrecon.utils.STOCK_RECON_LOT_LIST
import com.olam.warehouse.vegax.stockrecon.utils.STOCK_RECON_SUMMARY


class VegaStockReconActivity : HomeBaseActivity(), VegaStockCallbackListener, VegaStockReconAddLotsListener {
    private val mTAG = VegaStockReconActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_vega_stock_recon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*The below method is to create all objects using koin*/
        injectStockReconFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        /*By default, loding the selection fragment*/
        displayFragment(VegaStockReconSelectProgressFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flStockRecon,
            allowBackStack = flag
        )
    }

    /*The below method is for overall navigation in this module,
    via this way only we are navigating one fragment to another*/
    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {

            STOCK_RECON_AUDIT_TYPE_SELECT -> displayFragment(VegaStockReconAuditTypeSelectFragment.newInstance(), true)

            STOCK_RECON_ADD_LOT -> displayFragment(VegaStockReconAddLotFragment.newInstance(data as Bundle), true)

            STOCK_RECON_INPROGRESS_COMPLETED -> displayFragment(
                VegaStockReconInProgressCompletedFragment.newInstance(
                    data as Bundle
                ), true
            )

            STOCK_RECON_LOT_LIST -> displayFragment(
                VegaStockReconLotListFragment.newInstance(
                    VegaEcuadorDispatch(),
                    arrayListOf(),
                    data as Bundle
                ), true
            )

            STOCK_RECON_BAG_AUDIT -> displayFragment(
                VegaStockReconBagAuditFragment.newInstance(data as Bundle),
                true
            )

            STOCK_RECON_SUMMARY -> displayFragment(
                VegaStockReconSummaryFragment.newInstance(data as VegaStockReconBagDetails),
                true
            )

            STOCK_ALL_AUDIT_LIST -> {
                var dt = data as VegaStockReconBagDetails
                if (dt.bagType1?.isEmpty() == true) {
                    displayFragment(
                        VegaStockReconAllAuditListFragment.newInstance(data as VegaStockReconBagDetails),
                        true
                    )
                } else {
                    displayFragment(
                        VegaStockReconAllAuditListFragment.newInstance(data as VegaStockReconBagDetails),
                        false
                    )
                }
            }

            RECON_REPORT_DATE_PLANT_SELECTION -> {
                displayFragment(VegaReconReportDatePlantSelectionFragement.newInstance(), true)
            }

            RECON_REPORT_STATISTICS -> {
                displayFragment(VegaReconReportStatisticsFragment.newInstance(data as VegaReconReportBundleData), true)
            }

            RECON_REPORT_RECON_LIST -> {
                displayFragment(VegaReconReportReconListFragment.newInstance(data as VegaReconReportBundleData), true)
            }

            RECON_REPORT_AUDIT_LIST -> {
                displayFragment(VegaReconReportAuditListFragment.newInstance(data as VegaReconReportBundleData), true)
            }

            RECON_REPORT_AUDIT_DETAILS -> {
                displayFragment(
                    VegaReconReportAuditDetailsFragment.newInstance(data as VegaReconReportBundleData),
                    true
                )
            }

            RECON_REPORT_VIEW_PHOTO -> {
                displayFragment(VegaReconReportViewPhotoFragment.newInstance(data as VegaReconReportBundleData), true)
            }

        }
    }

    /*as of now the below method is not used*/
    override fun replaceFragment(receivingType: String, data: Any, backStackStatus: Boolean) {
        when (receivingType) {
            STOCK_RECON_ADD_LOT -> displayFragment(
                VegaStockReconAddLotFragment.newInstance(data as Bundle),
                backStackStatus
            )

            STOCK_ALL_AUDIT_LIST -> {
                displayFragment(
                    VegaStockReconAllAuditListFragment.newInstance(data as VegaStockReconBagDetails),
                    backStackStatus
                )
            }

//            STOCK_RECON_INPROGRESS_COMPLETED -> displayFragment(
//                VegaStockReconInProgressCompletedFragment.newInstance(
//                    data as Bundle
//                ), backStackStatus
//            )
        }
    }

    /*The below method to get selected lots from lot list page, and send to the addLot fragment*/
    override fun addedLots(lots: ArrayList<VegaDispatchLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flStockRecon)
        when (lotAddFragment) {
            is VegaStockReconAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
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

    /*In some case, in particular fragment have two cases,
    like backpress should work when it come from fragmentA and should not work in fragmentB,
    * so, in that we find the previous fragment context, and avoid the backstack*/
    private fun backNavigation() {
        val fragment = supportFragmentManager.findFragmentById(R.id.flStockRecon)
        super.onBackPressed()
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flStockRecon)
        when (fragment1) {
            is VegaStockReconInProgressCompletedFragment -> {
                when (fragment) {
                    is VegaStockReconAddLotFragment -> supportFragmentManager.popBackStackImmediate()
                }
            }

            is VegaStockReconBagAuditFragment -> {
                when (fragment) {
                    is VegaStockReconAllAuditListFragment -> supportFragmentManager.popBackStackImmediate()
                }
            }

        }
    }

}
