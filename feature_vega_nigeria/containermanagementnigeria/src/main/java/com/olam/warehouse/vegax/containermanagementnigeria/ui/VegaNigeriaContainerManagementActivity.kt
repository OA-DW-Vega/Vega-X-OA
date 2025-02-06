package com.olam.warehouse.vegax.containermanagementnigeria.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.containermanagementnigeria.R
import com.olam.warehouse.vegax.containermanagementnigeria.di.injectNigeriaContainerMangementFeature
import com.olam.warehouse.vegax.containermanagementnigeria.ui.addcontainer.VegaNigeriaAddContainerFragment
import com.olam.warehouse.vegax.containermanagementnigeria.ui.containerInventory.VegaNigeriaContainerDetailsFragment
import com.olam.warehouse.vegax.containermanagementnigeria.ui.containerInventory.VegaNigeriaContainerEditFragment
import com.olam.warehouse.vegax.containermanagementnigeria.ui.containerInventory.VegaNigeriaContainerInventoryListFragment
import com.olam.warehouse.vegax.containermanagementnigeria.ui.containerInventory.VegaNigeriaContainerStuffingDetailsFragment
import com.olam.warehouse.vegax.containermanagementnigeria.utils.ADD_CONTAINER
import com.olam.warehouse.vegax.containermanagementnigeria.utils.CONTAINER_INVENTORY

class VegaNigeriaContainerManagementActivity : HomeBaseActivity(),
    VegaNigeriaContainerManagementTypeSelectFragment.iCallBack,
    VegaNigeriaContainerInventoryListFragment.OnContainerDetailsNavigationListener,
    VegaNigeriaContainerDetailsFragment.onStuffingDetailsNavigateListner,
    VegaNigeriaContainerEditFragment.VegaContainerEditCallback,
    VegaNigeriaContainerStuffingDetailsFragment.onNavigateListner
{

    private val mTAG = VegaNigeriaContainerManagementActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_nigeria_container_management

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectNigeriaContainerMangementFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaNigeriaContainerManagementTypeSelectFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flContainerManagement,
            allowBackStack = flag
        )
    }

    override fun replaceFragment(containerType: String, data: Any) {
        if (containerType == ADD_CONTAINER) {
            displayFragment(VegaNigeriaAddContainerFragment.newInstance(), true)
        }else if(containerType == CONTAINER_INVENTORY){
            displayFragment(VegaNigeriaContainerInventoryListFragment.newInstance(), true)
        }
        /*else if (gateEntryType == PARAMS_LIST_FRAG) {
            displayFragment(VegaNigeriaAddNewTruckFragment.newInstance(gateEntryData), true)
        } else if (gateEntryType == SUMMARY_FRAG) {
            displayFragment(VegaGateEntryNigeriaSummaryFragment.newInstance(gateEntryData), true)
        }*/
    }

    override fun navigateToDetails(bundle: Bundle) {
        displayFragment(VegaNigeriaContainerDetailsFragment.newInstance(bundle), true)
    }

    override fun navigateToStuffingDetails(bundle: Bundle) {
        displayFragment(VegaNigeriaContainerStuffingDetailsFragment.newInstance(bundle), true)
    }

    override fun navigateToEditContainer(bundle: Bundle) {
        displayFragment(VegaNigeriaContainerEditFragment.newInstance(bundle), true)
    }

    override fun onDeleteContainer() {
        displayFragment(VegaNigeriaContainerInventoryListFragment.newInstance(), true)
    }

    override fun onNavigateBack() {
        supportFragmentManager.popBackStackImmediate()
//        displayFragment(VegaNigeriaContainerInventoryListFragment.newInstance(), true)
    }

    override fun onEditContainerSuccess() {
        displayFragment(VegaNigeriaContainerInventoryListFragment.newInstance(), true)
    }

    override fun navigateBackToInventory() {
        displayFragment(VegaNigeriaContainerInventoryListFragment.newInstance(), true)
    }

    override fun onBackPressed() {
        backNavigation()
    }

    private fun backNavigation() {
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flContainerManagement)
        supportFragmentManager.popBackStackImmediate()
        when (supportFragmentManager.findFragmentById(R.id.flContainerManagement)) {
            is VegaNigeriaContainerDetailsFragment -> {
                when(fragment1){
                    is VegaNigeriaContainerEditFragment -> {
                        supportFragmentManager.popBackStackImmediate()
                        var fragment2 = supportFragmentManager.findFragmentById(R.id.flContainerManagement)
                        when(fragment2){
                            is VegaNigeriaContainerInventoryListFragment ->
                                fragment2.getBack()

                        }
//                        VegaNigeriaContainerInventoryListFragment().getBack()
                    }
                }
            }
            is VegaNigeriaContainerInventoryListFragment -> {
                when(fragment1){
                    is VegaNigeriaContainerDetailsFragment -> {
                        var fragment2 = supportFragmentManager.findFragmentById(R.id.flContainerManagement)
                        when(fragment2){
                            is VegaNigeriaContainerInventoryListFragment ->
                                fragment2.getBack()
                        }
                    }
                }
            }
            else ->
                when(fragment1){
                    is VegaNigeriaContainerManagementTypeSelectFragment -> {
                        super.onBackPressed()
                    }
                }

        }
    }

/*    private fun backNaviagation() {
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flContainerManagement)
//        supportFragmentManager.popBackStackImmediate()
        when (supportFragmentManager.findFragmentById(R.id.flContainerManagement)) {

            is VegaNigeriaContainerInventoryListFragment -> {
                finish()
            }
//            else -> super.onBackPressed()
        }
    }*/
}

