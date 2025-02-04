package com.olam.warehouse.vegax.containermanagement.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.containermanagement.R
import com.olam.warehouse.vegax.containermanagement.di.injectCameroonContainerMangementFeature
import com.olam.warehouse.vegax.containermanagement.ui.addcontainer.VegaCameroonAddContainerFragment
import com.olam.warehouse.vegax.containermanagement.ui.containerInventory.VegaCameroonContainerDetailsFragment
import com.olam.warehouse.vegax.containermanagement.ui.containerInventory.VegaCameroonContainerEditFragment
import com.olam.warehouse.vegax.containermanagement.ui.containerInventory.VegaCameroonContainerInventoryListFragment
import com.olam.warehouse.vegax.containermanagement.ui.containerInventory.VegaCameroonContainerStuffingDetailsFragment
import com.olam.warehouse.vegax.containermanagement.utils.ADD_CONTAINER
import com.olam.warehouse.vegax.containermanagement.utils.CONTAINER_INVENTORY

class VegaCameroonContainerManagementActivity : HomeBaseActivity(),
    VegaCameroonContainerManagementTypeSelectFragment.iCallBack,
    VegaCameroonContainerInventoryListFragment.OnContainerDetailsNavigationListener,
    VegaCameroonContainerDetailsFragment.onStuffingDetailsNavigateListner,
    VegaCameroonContainerEditFragment.VegaContainerEditCallback,
    VegaCameroonContainerStuffingDetailsFragment.onNavigateListner
{

    private val mTAG = VegaCameroonContainerManagementActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_cameroon_container_management

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectCameroonContainerMangementFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        displayFragment(VegaCameroonContainerManagementTypeSelectFragment.newInstance(), false)
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
            displayFragment(VegaCameroonAddContainerFragment.newInstance(), true)
        }else if(containerType == CONTAINER_INVENTORY){
            displayFragment(VegaCameroonContainerInventoryListFragment.newInstance(), true)
        }

    }

    override fun navigateToDetails(bundle: Bundle) {
        displayFragment(VegaCameroonContainerDetailsFragment.newInstance(bundle), true)
    }

    override fun navigateToStuffingDetails(bundle: Bundle) {
        displayFragment(VegaCameroonContainerStuffingDetailsFragment.newInstance(bundle), true)
    }

    override fun navigateToEditContainer(bundle: Bundle) {
        displayFragment(VegaCameroonContainerEditFragment.newInstance(bundle), true)
    }

    override fun onDeleteContainer() {
        displayFragment(VegaCameroonContainerInventoryListFragment.newInstance(), true)
    }

    override fun onNavigateBack() {
        supportFragmentManager.popBackStackImmediate()
    }

    override fun onEditContainerSuccess() {
        displayFragment(VegaCameroonContainerInventoryListFragment.newInstance(), true)
    }

    override fun navigateBackToInventory() {
        displayFragment(VegaCameroonContainerInventoryListFragment.newInstance(), true)
    }

    override fun onBackPressed() {
        backNavigation()
    }

    private fun backNavigation() {
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flContainerManagement)
        supportFragmentManager.popBackStackImmediate()
        when (supportFragmentManager.findFragmentById(R.id.flContainerManagement)) {
            is VegaCameroonContainerDetailsFragment -> {
                when(fragment1){
                    is VegaCameroonContainerEditFragment -> {
                        supportFragmentManager.popBackStackImmediate()
                        var fragment2 = supportFragmentManager.findFragmentById(R.id.flContainerManagement)
                        when(fragment2){
                            is VegaCameroonContainerInventoryListFragment ->
                                fragment2.getBack()

                        }
                    }
                }
            }
            is VegaCameroonContainerInventoryListFragment -> {
                when(fragment1){
                    is VegaCameroonContainerDetailsFragment -> {
                        var fragment2 = supportFragmentManager.findFragmentById(R.id.flContainerManagement)
                        when(fragment2){
                            is VegaCameroonContainerInventoryListFragment ->
                                fragment2.getBack()
                        }
                    }
                }
            }
            else ->
                when(fragment1){
                    is VegaCameroonContainerManagementTypeSelectFragment -> {
                        super.onBackPressed()
                    }
                }

        }
    }


}

