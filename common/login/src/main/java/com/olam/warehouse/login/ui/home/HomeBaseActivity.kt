package com.olam.warehouse.login.ui.home

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.gson.Gson
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.ActivityHomeBinding
import com.olam.warehouse.login.databinding.DialogStartLoadingLayout1Binding
import com.olam.warehouse.login.databinding.NavigationViewBinding
import com.olam.warehouse.login.ui.FrequentlyAskedQActivity
import com.olam.warehouse.login.ui.LoginActivity
import com.olam.warehouse.login.ui.bottomsheet.VegaInventorySyncDialog
import com.olam.warehouse.login.ui.bottomsheet.VegaTransMasterSyncDialog
import com.olam.warehouse.login.ui.common.VegaCocoaDispatchTypeActivity
import com.olam.warehouse.login.ui.common.VegaCommonModuleNavigation
import com.olam.warehouse.login.ui.bottomsheet.VegaUserProfileScreen
import com.olam.warehouse.login.ui.notification.NotificationActivity
import com.olam.warehouse.login.ui.quickpinaccess.VegaQuickAccessPinActivity
import com.olam.warehouse.login.ui.quickpinaccess.createpin.VegaCreatePinActivity
import com.olam.warehouse.login.ui.settings.SettingsActivity
import com.olam.warehouse.login.ui.transaction.TransactionActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.AppDatabase
import com.olam.warehouse.master.DODatabase
import com.olam.warehouse.master.VegaDatabase
import com.olam.warehouse.master.common.data.work.*
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.common.utils.getRoles
import com.olam.warehouse.master.ui.BaseActivity
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaFeatureMaster
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.navigation.features.*
import com.olam.warehouse.presentation.enums.UserProduct
import com.olam.warehouse.presentation.ui.widget.FlipTab
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.LOGOUT
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.Constants.TRANS_FIRST_SYNC
import com.olam.warehouse.presentation.utils.DateUtils.getLastInventorySyncTime
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.ginningwarehouse.ui.work.GinningBaleTransMasterDataWorker
import com.olam.warehouse.vegax.ginningwarehouse.ui.work.GinningTransDeliveryMasterDataWorker
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by Baskaran Kannan on 12/24/2019.
 */
abstract class HomeBaseActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mToggle: ActionBarDrawerToggle
    private var fliptab1: FlipTab? = null
    private var llCloseDrawer: LinearLayout? = null
    private var tvLogout: TextView? = null
    private var isLogOut: String = ""
    private val REQUEST_CODE = 220
    private val LOCK_REQUEST_CODE = 221
    private val SECURITY_SETTING_REQUEST_CODE = 233
    private var isshowing = false
    private var bindingActivity: ActivityHomeBinding? = null
    private var bindingNav: NavigationViewBinding? = null
    var ttFeatureList = java.util.ArrayList<VegaFeatureMaster>()


    override fun attachBaseContext(context: Context?) {
        super.attachBaseContext(context?.let { LocaleHelper.onAttach(it) })
        context?.let { SplitCompat.install(it) }
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        mToggle.syncState()
    }

    protected fun assignBindingObeject(
        bindingActivity: ActivityHomeBinding,
        bindingNav: NavigationViewBinding
    ) {
        this.bindingActivity = bindingActivity
        this.bindingNav = bindingNav
    }

    protected fun initNavigationView() {
//        val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//        mBTAdapter = bluetoothManager.adapter
        updateProductHeader()
        val toolBar = findViewById<Toolbar>(R.id.tool_bar)
        if (bindingActivity?.drawerLayout == null) {
            initToolbar(true, toolBar, object : PushObserve {
                override fun clickObserve() {
                    moveNotificationPage()
                }
                override fun logoutObserve() {
                    updateLogOut()
                }
            })
            return
        }
        initToolbar(false, toolBar, object : PushObserve {
            override fun clickObserve() {
                moveNotificationPage()
            }
            override fun logoutObserve() {
                updateLogOut()
            }
        })
        mToggle = ActionBarDrawerToggle(
            this,
            bindingActivity?.drawerLayout,
            toolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        mToggle.syncState()
        //mToggle.isDrawerIndicatorEnabled = false
        bindingNav?.navigationView?.setNavigationItemSelectedListener(this)
        bindingActivity?.drawerLayout?.addDrawerListener(mToggle)
        setEntityLevelVarient()
        setUpOnlineOfflineToggle()

        bindingActivity?.drawerLayout?.addDrawerListener(object :
            DrawerLayout.SimpleDrawerListener() {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                bindingActivity?.drawerLayout?.getChildAt(0)?.translationX =
                    slideOffset * drawerView.width
                bindingActivity?.drawerLayout?.bringChildToFront(drawerView)
                bindingActivity?.drawerLayout?.requestLayout()
            }
        })
    }

    private fun setEntityLevelVarient() {
        val navHeaderView = bindingNav?.navigationView?.getHeaderView(0)
        val header = navHeaderView?.findViewById<LinearLayout>(R.id.llHeader)
        val userProfile = navHeaderView?.findViewById<LinearLayout>(R.id.llUserName)
        val logOut = bindingNav?.navigationView?.findViewById<TextView>(R.id.tvLogout)
        llCloseDrawer = navHeaderView?.findViewById<LinearLayout>(R.id.llCloseDrawer)
        if (getCurrentKey().split("_")[1].contains("GH") && getCurrentKey().split("_")[2].contains("COCO")) {
            bindingNav?.navigationView?.menu?.findItem(R.id.navGateEntry)?.title =
                getString(R.string.truck_reporting)
        }
        if (getCurrentKey().split("_")[1].contains("NG") && getCurrentKey().split("_")[2].contains("COCO")) {
            bindingNav?.navigationView?.menu?.findItem(R.id.navApproval)?.title =
                getString(R.string.quality_approval)
        }
        when {
            PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "").contains(Constants.OFI) -> {
                bindingNav?.navigationView?.setMargins(right = -65)
                header?.let {
                    ViewCompat.setBackground(
                        it, ContextCompat.getDrawable(
                            header.context,
                            com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                        )
                    )
                }
            }
            else -> {
                /*val navMenus = bindingNav?.navigationView?.menu
                navMenus.findItem(R.id.navLogout).isVisible = isOnline()
                logOut.gone()
                llCloseDrawer.gone()*/
                header?.let {
                    ViewCompat.setBackground(
                        it, ContextCompat.getDrawable(
                            header.context,
                            com.olam.warehouse.presentation.R.color.colorPrimaryHeadOfi
                        )
                    )
                }
                val flipt = navHeaderView?.findViewById<FlipTab>(R.id.fliptab)
                flipt?.setOverallColor(
                    ContextCompat.getColor(
                        this,
                        com.olam.warehouse.presentation.R.color.colorPrimaryOfi1
                    )
                )
            }
        }
        userProfile?.setOnClickListener {
            val bottomDialog = VegaUserProfileScreen.newInstance()
            bottomDialog.dialog?.setCanceledOnTouchOutside(false)
            supportFragmentManager.let { it1 -> bottomDialog.show(it1, "UserProfileBottomSheet") }


        }
    }

    fun moveNotificationPage() {
        startActivity(
            Intent(
                this,
                NotificationActivity::class.java
            )
        )
    }

    override fun onBackPressed() {
        if (bindingActivity?.drawerLayout != null && bindingActivity?.drawerLayout?.isDrawerOpen(
                GravityCompat.START
            ) == true
        ) {
            bindingActivity?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            when {
                it.toString().contains("Search", true) -> {
                    val logo =
                        findViewById<AppCompatImageView>(com.olam.warehouse.master.R.id.ivLogo)

                    it.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                        override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                            logo.gone()
                            return true
                        }

                        override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                            logo.visible()
                            return true
                        }
                    })
                }
                else -> {
                    when (it.itemId) {
                        android.R.id.home -> {
                            super.onBackPressed()
                            return true
                        }
                        else -> super.onOptionsItemSelected(item)
                    }
                }
            }
        }
        return item.let { super.onOptionsItemSelected(it) }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        when (menuItem.itemId) {
            R.id.navReceving -> {
                when {
                    currentKey.split("_")[0].contains("DO") -> {
                        DOReceivingFeatureNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[0].contains("VEGA") -> {
                        VegaReceivingFeatureNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                }
            }

            R.id.navTranx -> {
                when {
                    currentKey.split("_")[1].contains("GH") && currentKey.split("_")[2].contains("COCO") -> {
                        VegaGhanaCocoaHistoryTransactionsNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.contains("VEGA_NG") && currentKey.contains("COCO") -> {
                        VegaTransactionsHistoryNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    else ->{
                        VegaTransactionsHistoryNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                }


            }

            R.id.navWeighment -> {
                when {
                    currentKey.split("_")[0].contains("DO") -> {
                        DOReceivingFeatureNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains("COFF") -> {
                        VegaIndiaCoffeeWeighmentFeatureNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[0].contains("VEGA") -> {
                        VegaReceivingFeatureNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[1].contains("NG")
                            && currentKey.split("_")[2].contains("COCO") -> {
                        VegaNigeriaWeighmentFeatureNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                }
            }
            R.id.navQuality -> {
                when {
                    currentKey.split("_")[0].contains("DO") -> {
                        DOQualityFeatureNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[0].contains("VEGA") -> {
                        when {
                            currentKey.split("_")[1].contains("EC") -> {
                                VegaEcuadorQualityNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            }
                            currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains(
                                "COFF"
                            ) -> {
                                VegaIndiaCoffeeQualityFeatureNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            }
                            currentKey.split("_")[1].contains("CM") -> {
                                if (currentKey.split("_")[2].contains("COCO")) {
                                    VegaCameroonCocoaQualityNavigation.dynamicStart?.let {
                                        startActivity(it)
                                    }
                                }
                            }
                            currentKey.split("_")[1].contains("GH") -> {
                                if (currentKey.split("_")[2].contains("CASH")) {
                                    VegaGhanaCashewQualityNavigation.dynamicStart?.let {
                                        startActivity(it)
                                    }
                                }
                            }
                            currentKey.split("_")[1].contains("NG") -> {
                                if (currentKey.split("_")[2].contains("COCO")) {
                                    VegaNigeriaQualityNavigation.dynamicStart?.let {
                                        startActivity(it)
                                    }
                                } else if (currentKey.split("_")[2].contains("SESA") || currentKey.split(
                                        "_"
                                    )[2].contains("CASH")
                                ) {
                                    VegaNigeriaSesameQualityNavigation.dynamicStart?.let {
                                        startActivity(it)
                                    }
                                }

                            }
                            currentKey.split("_")[1].contains("IV") && currentKey.split("_")[2].contains("COCO") ->{
                                VegaIndoCoffeeQualityNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            }
                            currentKey.split("_")[1].contains("NI") -> {

                            }
                            else -> {
                                VegaQualityFeatureNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            }
                        }
                    }
                }
            }
            R.id.navApproval -> {
                if (currentKey.split("_")[0].contains("VEGA")) {
                    when {
                        currentKey.split("_")[1].contains("EC") -> {
                            VegaEcuadorApproveNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                        currentKey.split("_")[1].contains("CM") -> {
                            VegaQualityApproveCameroonNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                        currentKey.split("_")[1].contains("GH") -> {
                            VegaQualityApproveCameroonNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                        currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains(
                            "COFF"
                        ) -> {
                            VegaIndiaCoffeeQualityApprovalFeatureNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                        currentKey.split("_")[1].contains("NG") && currentKey.split("_")[2].contains(
                            "COCO"
                        ) -> {
                            VegaNigeriaQualityApprovalFeatureNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                        currentKey.split("_")[1].contains("IV") && currentKey.split("_")[2].contains("COCO") -> {
                            VegaIndiaCoffeeQualityApprovalFeatureNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                        else -> {
                            VegaApproveNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                    }
                    /* VegaApproveNavigation.dynamicStart?.let {
                         startActivity(it)
                     }*/
                }
            }
            R.id.navOffloading -> {
                if (currentKey.split("_")[0].contains("VEGA")) {
                    when {
                        currentKey.split("_")[1].contains("EC") -> {
                            if(ttFeatureList.isNotEmpty()) {
                                if (ttFeatureList.size > 1) {
                                    showTTDialog(VegaEcuadorOffloadingNavigation, R.string.select_procurement_type,true)
                                } else if (ttFeatureList.size == 1) {
//                                if(ttFeatureList.get(0).featureName.equals(Constants.DIRECT)){
//                                    showTTDialog(VegaEcuadorOffloadingNavigation, R.string.select_complaint_type, true)
//                                } else {
                                    VegaEcuadorOffloadingNavigation.dynamicStart?.let {
                                        it.putExtra(Constants.PROCUREMENT_TYPE, ttFeatureList.get(0).featureName)
                                        startActivity(it)
                                    }
//                                }
                                }
                            } else {
                                VegaEcuadorOffloadingNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }

                            }
                        }
                        currentKey.split("_")[1].contains("IV") && currentKey.split("_")[2].contains("COCO")-> {
                            if(ttFeatureList.isNotEmpty()) {
                                if (ttFeatureList.size > 1) {
                                    showTTDialog(VegaCoCoaOffloadingNavigation, R.string.select_procurement_type,true)
                                } else if (ttFeatureList.size == 1) {
//                                if(ttFeatureList.get(0).featureName.equals(Constants.DIRECT)){
//                                    showTTDialog(VegaEcuadorOffloadingNavigation, R.string.select_complaint_type, true)
//                                } else {
                                    VegaCoCoaOffloadingNavigation.dynamicStart?.let {
                                        it.putExtra(Constants.PROCUREMENT_TYPE, ttFeatureList.get(0).featureName)
                                        startActivity(it)
                                    }
//                                }
                                }
                            } else {
                                VegaCoCoaOffloadingNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }

                            }
                        }
                        currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains(
                            "COFF"
                        ) -> {
                            VegaIndiaCoffeeOffloadingFeatureNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                        currentKey.split("_")[1].contains("CM") -> {
                            if (currentKey.split("_")[2].contains("COCO")) {
                                if(ttFeatureList.isNotEmpty()) {
                                    if (ttFeatureList.size > 1) {
                                        showTTDialog(VegaCameroonCocoaOffloadingNavigation, R.string.select_procurement_type,true)
                                    } else if (ttFeatureList.size == 1) {
//                                        if(ttFeatureList.get(0).featureName.equals(Constants.DIRECT)){
//                                            showTTDialog(VegaGateEntryCameroonNavigation, R.string.select_complaint_type, true)
//                                        } else {
                                        VegaCameroonCocoaOffloadingNavigation.dynamicStart?.let {
                                            it.putExtra(Constants.PROCUREMENT_TYPE, ttFeatureList.get(0).featureName)
                                            startActivity(it)
                                        }
//                                        }
                                    }
                                } else {
                                    VegaCameroonCocoaOffloadingNavigation.dynamicStart?.let {
                                        startActivity(it)
                                    }

                                }
                                /* VegaCameroonCocoaOffloadingNavigation.dynamicStart?.let {
                                     startActivity(it)
                                 }*/
                            }
                        }
                        currentKey.split("_")[1].contains("GH") -> {
                            if (currentKey.split("_")[2].contains("COCO")) {
                                VegaGhanaCocoOffloadingNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            }
                        }
                        currentKey.split("_")[1].contains("GH") -> {
                            if (currentKey.split("_")[2].contains("CASH")) {
                                VegaGhanaCashewOffloadingNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            }
                        }
                        currentKey.split("_")[1].contains("NG") -> {
                            when {
                                currentKey.split("_")[2].contains("COCO") -> {
                                    if(ttFeatureList.isNotEmpty()) {
                                        if (ttFeatureList.size == 2) {
                                            showTTDialog(VegaNigeriaOffloadingNavigation, R.string.select_procurement_type,true)
                                        } else if (ttFeatureList.size == 1) {
                                            VegaNigeriaOffloadingNavigation.dynamicStart?.let {
                                                it.putExtra(Constants.PROCUREMENT_TYPE, ttFeatureList.get(0).featureName)
                                                startActivity(it)
                                            }
                                        }
                                    } else {
                                        VegaNigeriaOffloadingNavigation.dynamicStart?.let {
                                            startActivity(it)
                                        }

                                    }
                                }
                                currentKey.split("_")[2].contains("SESA") || currentKey.split(
                                    "_"
                                )[2].contains("CASH") -> {
                                    VegaNigeriaSesameOffloadingNavigation.dynamicStart?.let {
                                        startActivity(it)
                                    }
                                }
                            }

                        }
                        else -> {
                            when {
                                currentKey.split("_")[2].contains("CASH") -> VegaOffloadingNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                                currentKey.split("_")[2].contains("COFF") -> VegaCoffeeOffloadingNavigation.dynamicStart?.let {
                                    startActivity(
                                        it
                                    )
                                }
                                currentKey.split("_")[2].contains("COCO") -> VegaCoCoaOffloadingNavigation.dynamicStart?.let {
                                    startActivity(
                                        it
                                    )
                                }
                            }
                        }
                    }
                }
            }
            R.id.navMtnt -> {
                if (currentKey.split("_")[0].contains("VEGA")) {
                    when {
                        currentKey.split("_")[1].contains("EC") -> {
                            VegaEcuadorDispatchNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                        currentKey.split("_")[1].contains("NG") -> {
                            if (currentKey.split("_")[2].contains("COCO")) {
                                startActivity(
                                    Intent(
                                        this,
                                        VegaCocoaDispatchTypeActivity::class.java
                                    )
                                )
                            }
                        }
                        currentKey.split("_")[1].contains("GH") -> {
                            if (currentKey.split("_")[2].contains("COCO")) {
                                startActivity(
                                    Intent(
                                        this,
                                        VegaCocoaDispatchTypeActivity::class.java
                                    )
                                )
                            }
                        }
                        currentKey.split("_")[1].contains("GH") -> {
                            if (currentKey.split("_")[2].contains("CASH")) {
                                VegaGhanaMtntNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            }
                        }
                        currentKey.split("_")[1].contains("NI") -> {
                            if (currentKey.split("_")[2].contains("COFF")) {
                                startActivity(
                                    Intent(
                                        this,
                                        VegaCocoaDispatchTypeActivity::class.java
                                    )
                                )
                            }

                        }
                        currentKey.split("_")[1].contains("NG") && currentKey.split("_")[2].contains(
                            "CASH"
                        ) -> {
                            startActivity(Intent(this, VegaCocoaDispatchTypeActivity::class.java))
                        }
                        else -> {
                            when {
                                currentKey.split("_")[2].contains("CASH") -> VegaDispatchNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                                (currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains(
                                    "COFF"
                                )) -> VegaIndiaCoffeeDispatchFeatureNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                                currentKey.split("_")[2].contains("COCO") || currentKey.split("_")[2].contains(
                                    "COFF"
                                ) -> startActivity(
                                    Intent(
                                        this, VegaCocoaDispatchTypeActivity::class.java
                                    )
                                )
                                currentKey.split("_")[2].contains("COTTGINN") -> {
                                    VegaCottonGinningDispatchNavigation.dynamicStart?.let {
                                        startActivity(it)
                                    }
                                }
                                currentKey.split("_")[2].contains("COTTPORT") -> {
                                    VegaCottonPortDispatchNavigation.dynamicStart?.let {
                                        startActivity(it)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            R.id.navProcessing -> {
                when {
                    currentKey.split("_")[0].contains("VEGA") -> {
                        when {
                            !currentKey.split("_")[1].contains("EC") -> {
                                when {
                                    currentKey.split("_")[2].contains("CASH") ->
                                        when {
                                            currentKey.split("_")[1].contains("GH") -> {
                                                VegaGhanaProcessingNavigation.dynamicStart?.let {
                                                    startActivity(it)
                                                }
                                            }
                                            currentKey.split("_")[1].contains("NG") -> {
                                                VegaProcessingSesameNavigation.dynamicStart?.let {
                                                    startActivity(it)
                                                }
                                            }
                                            else -> {
                                                VegaProcessingNavigation.dynamicStart?.let {
                                                    startActivity(it)
                                                }
                                            }
                                        }
                                    currentKey.split("_")[1].contains("NG") -> {
                                        if (currentKey.split("_")[2].contains("COCO")) {
                                            VegaProcessingNigeriaNavigation.dynamicStart?.let {
                                                startActivity(it)
                                            }
                                        }
                                    }
                                    currentKey.split("_")[2].contains("COCO") ->
                                        when {
                                            currentKey.split("_")[1].contains("CM") -> {
                                                VegaCameroonProcessingCocoaNavigation.dynamicStart?.let {
                                                    startActivity(it)
                                                }
                                            }
                                            else -> {
                                                VegaProcessingCocoaNavigation.dynamicStart?.let {
                                                    startActivity(it)
                                                }
                                            }
                                        }
                                    (currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains(
                                        "COFF"
                                    )) -> VegaIndiaCoffeeProcessingFeatureNavigation.dynamicStart?.let {
                                        startActivity(
                                            it
                                        )
                                    }
                                    currentKey.split("_")[2].contains("COFF") -> {
                                        if (currentKey.split("_")[1].contains("NI"))
                                            VegaNicaraguaCoffeeProcessingFeatureNavigation.dynamicStart?.let {
                                                startActivity(it)
                                            }
                                        else VegaProcessingCoffeeNavigation.dynamicStart?.let {
                                            startActivity(it)
                                        }
                                    }
                                    currentKey.split("_")[2].contains("SESA") -> VegaProcessingSesameNavigation.dynamicStart?.let {
                                        startActivity(it)
                                    }
                                }
                            }
                        }
                        /*VegaProcessingNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }*/
                    }
                }
            }
            R.id.navGateEntry -> {
                when {
                    currentKey.split("_")[0].contains("VEGA") -> {
                        when {
                            !currentKey.split("_")[1].contains("EC") -> {
                                when {
                                    currentKey.split("_")[1].contains("NG") -> {
                                        if (currentKey.split("_")[2].contains("COCO")||currentKey.split("_")[2].contains("CASH")) {
                                            VegaGateEntryNigeriaNavigation.dynamicStart?.let {
                                                startActivity(it)
                                            }
                                        }
                                    }
                                    currentKey.split("_")[1].contains("GH") -> {
                                        if (currentKey.split("_")[2].contains("COCO")) {
                                            VegaGateEntryGhanaCocoaNavigation.dynamicStart?.let {
                                                startActivity(it)
                                            }
                                        }
                                    }
                                    currentKey.split("_")[1].contains("CM") -> {
                                        VegaGateEntryCameroonNavigation.dynamicStart?.let {
                                            startActivity(it)
                                        }
                                    }
                                    currentKey.split("_")[1].contains("GH") -> {
                                        VegaGateEntryCameroonNavigation.dynamicStart?.let {
                                            startActivity(it)
                                        }
                                    }
                                    currentKey.split("_")[2].contains("COFF") -> {
                                        VegaGateEntryCoffeeNavigation.dynamicStart?.let {
                                            startActivity(it)
                                        }
                                    }
                                    else -> {
                                        VegaGateEntryNavigation.dynamicStart?.let {
                                            startActivity(it)
                                        }
                                    }
                                }

                            }
                        }
                        /*VegaGateEntryNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }*/
                    }
                }
            }
            R.id.navGateEntryApproval -> {
                if (currentKey.split("_")[0].contains("VEGA")) {
                    if (!currentKey.split("_")[1].contains("EC")) {
                        when {
                            currentKey.split("_")[1].contains("NG") -> {
                                if (currentKey.split("_")[2].contains("COCO")) {
                                    VegaGateEntryApprovalNigeriaNavigation.dynamicStart?.let {
                                        startActivity(it)
                                    }
                                }
                            }
                        }

                    }
                }
            }
            R.id.navInventory -> {
                if (currentKey.split("_")[0].contains("VEGA")) {
                    when {
                        currentKey.split("_")[1].contains("EC") -> VegaEcuadorInventoryNavigation.dynamicStart?.let {
                            if (isOnline()) startActivity(it)
                        }
                        currentKey.split("_")[1].contains("CM") ->
                            if (currentKey.split("_")[2].contains("COCO")) {
                                VegaCameroonInventoryCocoNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            }
                        currentKey.split("_")[1].contains("NI") ->
                            if (currentKey.split("_")[2].contains("COFF")) {
                                VegaNicaraguaInventoryNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            }
                        currentKey.split("_")[1].contains("NG") ->
                            if (currentKey.split("_")[2].contains("COCO")) {
                                VegaNigeriaInventoryCocoNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            } else if (currentKey.split("_")[2].contains("SESA") || currentKey.split(
                                    "_"
                                )[2].contains("CASH")
                            ) {
                                VegaInventorySesameNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            }

                        else -> {
                            when {
                                currentKey.split("_")[1].contains("GH") -> if (currentKey.split("_")[2].contains(
                                        "COCO"
                                    )
                                ) {
                                    VegaInventoryGhanaCocoaNavigation.dynamicStart?.let {
                                        startActivity(it)
                                    }
                                }
                                currentKey.split("_")[2].contains("CASH") -> VegaInventoryNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                                currentKey.split("_")[2].contains("COCO") -> VegaInventoryCocoNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                                (currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains(
                                    "COFF"
                                )) -> VegaIndiaCoffeeInventoryFeatureNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                                currentKey.split("_")[2].contains("COFF") -> VegaInventoryCoffeeNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                                currentKey.split("_")[2].contains("COTTGINN") -> VegaCottonGinningInventoryNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                                currentKey.split("_")[2].contains("COTTPORT") -> VegaCottonPortInventoryNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            }
                        }
                    }
                    /*if (currentKey.split("_")[1].contains("EC")) {
                        VegaEcuadorInventoryNavigation.dynamicStart?.let {
                            if (isOnline()) startActivity(it)
                        }
                    } else {
                        when {
                            currentKey.split("_")[2].contains("CASH") -> VegaInventoryNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                            currentKey.split("_")[2].contains("COCO") -> VegaInventoryCocoNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                    }*/
                }
            }

            R.id.navSweepings -> {
                if (currentKey.split("_")[0].contains("VEGA")) {
                    VegaSweepingsCocoNavigation.dynamicStart?.let {
                        startActivity(it)
                    }
                }
            }
            R.id.navBagIssue -> {
                if (currentKey.split("_")[0].contains("VEGA")) {
                    if (currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains(
                            "COFF"
                        )
                    ) {
                        VegaIndiaCoffeeBagIssueNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    } else if (currentKey.split("_")[1].contains("NG") && currentKey.split("_")[2].contains(
                            "COCO"
                        )
                    ) {
                        VegaNigeriaCocoaBagIssueNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    else if (currentKey.split("_")[1].contains("IV") && currentKey.split("_")[2].contains("CASH")) {
                        VegaNigeriaCocoaBagIssueNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                }
            }
            R.id.navPile -> {
                if (currentKey.split("_")[0].contains("VEGA")) {
                    if (currentKey.split("_")[2].contains("COFF")) {

                        if (currentKey.split("_")[1].contains("IN"))
                            VegaIndiaCoffeePileNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        else if (currentKey.split("_")[1].contains("NI"))
                            VegaSesamePileNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        else if (currentKey.split("_")[1].contains("NG") && currentKey.split("_")[2].contains(
                                "COFF"
                            )
                        ) {
                            VegaNigeriaPileNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        } else
                            VegaCoffeePileNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                    }
                }
            }
            R.id.navPpq -> {
                when {
                    currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains("COFF") -> {
                        VegaIndiaCoffeePpqFeatureNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[1].contains("NG") && currentKey.split("_")[2].contains("CASH") -> {
                        VegaSesamePpqNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains("COFF") -> {
                        VegaSesamePpqNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[1].contains("NG") && currentKey.split("_")[2].contains("COCO") -> {
                        VegaCoffeePpqNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                }
            }
            R.id.navcontainer -> {
                when {
                    currentKey.split("_")[1].contains("NG") && currentKey.split("_")[2].contains("COCO") -> {
                        VegaNigeriaContainerManagementNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains("COFF") -> {
                        VegaNicaraguaNavigationContainerMangaement.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                }
            }
            R.id.navLotQuality -> {
                when {
                    currentKey.split("_")[1].contains("NG") && currentKey.split("_")[2].contains("COCO") -> {
                        VegaNigeriaCocoaLotQualityNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                }
            }
            R.id.navMTnr -> {
                when {
                    currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains("COFF") -> {
                        VegaNicaraguaOffloadingNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                }
            }

            R.id.navReconcillation -> {
                when {
                    currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains("COFF") -> {
                        VegaNicaraguaReportNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                }
            }
            R.id.navForward -> {
                when {
                    currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains("COFF") -> {
                        VegaNicaraguaForwordPONavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                }
            }
            R.id.navAdvance -> {
                when {
                    currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains("COFF") -> {
                        VegaNicaraguaAdvanceNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                }
            }

            R.id.navSync -> {
                /*if (currentKey.split("_")[1].contains("NI")){
                    isLogOut = MASTER_SYNC
                    isOfflineNicSyncDataAvailable()
                }else*/
                setAutoSyncMaster(isForceFetch = true)
            }
            R.id.navSyncTTFarmerData -> {
                if(PreferenceHelper.get(Constants.DIRECT,"").isNotEmpty()){
                    startTTFarmerDataSync()
                }
            }
            R.id.navSyncInventory -> {
                /* if (currentKey.split("_")[1].contains("NI")){
                     isLogOut = INVENTORY_SYNC
                     isOfflineNicSyncDataAvailable()
                 }else*/
                syncDetails()
            }
            R.id.navSyncVendor -> {
                /* if (currentKey.split("_")[1].contains("NI")) {
                     isLogOut = VENDOR_SYNC
                     isOfflineNicSyncDataAvailable()
                 }else*/
                inventorySyncData()
            }
            R.id.navSyncVendorCredit -> {
                vendorCreditLimitSyncData()
            }
            R.id.navSyncVendorAdvance -> {
                vendorAdvanceItemSyncData()
            }
            R.id.navSyncExchangeRate -> {
                exchangeRateSyncData()
            }
            R.id.navDataSync -> {
                dataSync()
            }
            R.id.navBaleDataSync -> {
                //baleDataSync()
            }
            R.id.navTransaction -> {
                startActivity(Intent(this, TransactionActivity::class.java))
            }
            R.id.navNotifyConfig -> {
                val intent = Intent(this, VegaCommonModuleNavigation::class.java)
                intent.putExtra(Constants.NAV_MODULE, Constants.NOTIFICATION_CONFIG)
                startActivity(intent)
            }
            R.id.navSettings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.navFaq -> {
                startActivity(Intent(this, FrequentlyAskedQActivity::class.java))
            }
            R.id.navQuickPin -> {
                checkQuickPin()
            }
            R.id.navGrn -> {
                if (currentKey.split("_")[0].contains("VEGA")) {
                    when {
                        currentKey.split("_")[1].contains("EC") -> VegaEcuadorGrnNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                        (currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains(
                            "COFF"
                        )) -> VegaIndiaCoffeeGRNFeatureNavigation.dynamicStart?.let {
                            startActivity(
                                it
                            )
                        }
                        (currentKey.split("_")[1].contains("GH") && currentKey.split("_")[2].contains(
                            "COCO"
                        )) -> if(ttFeatureList.isNotEmpty()) {
                                if (ttFeatureList.size > 1) {
                                    showTTDialog(VegaGhanaCocoOffloadingNavigation, R.string.select_procurement_type,true)
                                } else if (ttFeatureList.size == 1) {
//                                if(ttFeatureList.get(0).featureName.equals(Constants.DIRECT)){
//                                    showTTDialog(VegaNicaraguaGrnNavigation, R.string.select_complaint_type, true)
//                                } else {
                                    VegaGhanaCocoOffloadingNavigation.dynamicStart?.let {
                                        it.putExtra(Constants.PROCUREMENT_TYPE, ttFeatureList.get(0).featureName)
                                        startActivity(it)
                                    }
//                                }
                                }
                            } else {
                                VegaGhanaCocoOffloadingNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            }
                        currentKey.split("_")[1].contains("NI") -> {
                            if(ttFeatureList.isNotEmpty()) {
                                if (ttFeatureList.size > 1) {
                                    showTTDialog(VegaNicaraguaGrnNavigation, R.string.select_procurement_type,true)
                                } else if (ttFeatureList.size == 1) {
//                                if(ttFeatureList.get(0).featureName.equals(Constants.DIRECT)){
//                                    showTTDialog(VegaNicaraguaGrnNavigation, R.string.select_complaint_type, true)
//                                } else {
                                    VegaNicaraguaGrnNavigation.dynamicStart?.let {
                                        it.putExtra(Constants.PROCUREMENT_TYPE, ttFeatureList.get(0).featureName)
                                        startActivity(it)
                                    }
//                                }
                                }
                            } else {
                                VegaNicaraguaGrnNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            }
                        }
                        currentKey.split("_")[1].contains("NG") -> if (currentKey.split("_")[2].contains(
                                "COCO"
                            )
                        ) {
                            VegaNigeriaGrnNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                        currentKey.split("_")[1].contains("NG") -> VegaNicaraguaGrnNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                        currentKey.split("_")[1].contains("IV") -> if (currentKey.split("_")[2].contains(
                                "COCO"
                            )
                        ) {
                            VegaIndoCoffeeGrnNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                    }
                }
            }
            R.id.navInvoice -> {
                if (currentKey.split("_")[0].contains("VEGA")) {
                    when {
                        currentKey.split("_")[1].contains("NI") -> VegaNicaraguaInvoiceNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                }
            }
            R.id.navTransactionDetails -> {
                startActivity(Intent(this, TransactionActivity::class.java))
            }
            R.id.navReprint -> {
                val intent = Intent(this, TransactionActivity::class.java)
                intent.putExtra(UIUtils.REPRINT, true)
                startActivity(intent)
            }
            R.id.navLogout -> {
                if (!getCurrentOriginEntity().contains("OFI")) {
                    when {
                        (currentKey.split("_")[0].contains("DO")) && (!currentKey.split("_")[1].contains(
                            "ID"
                        )) -> {
                            isOfflineSyncDataAvailable()
                        }
                        currentKey.split("_")[1].contains("NI") -> {
                            isLogOut = LOGOUT
                            isOfflineNicSyncDataAvailable()
                        }
                        currentKey.split("_")[2].contains("COCO") -> {
                            isOfflineCocoaSyncDataAvailable()
                        }
                        else -> showLogOutDialog(false)
                    }
                }
            }
            R.id.navIncomingLots -> {
                if (currentKey.split("_")[0].contains("VEGA")) {
                    when {
                        currentKey.split("_")[2].contains("COTTGINN") -> {
                            VegaCottonGinningIncomingLOTsNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                        currentKey.split("_")[2].contains("COTTPORT") -> {
                            VegaCottonPortIncomingLOTsNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                    }
                }
            }
            R.id.navScan -> {
                startActivityForResult(
                    Intent(this, ScannerActivity::class.java),
                    Constants.ACTIVITY_SCAN_REQUEST_CODE
                )

            }
            R.id.navGinningInProgress -> {
                if (currentKey.split("_")[0].contains("VEGA")) {
                    when {
                        currentKey.split("_")[2].contains("COTTGINN") -> {
                            VegaCottonGinningInProgressNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                        currentKey.split("_")[2].contains("COTTPORT") -> {

                        }
                    }
                }
            }
            R.id.navDryingInProgress -> {
                if (currentKey.split("_")[0].contains("VEGA")) {
                    when {
                        currentKey.split("_")[2].contains("COTTGINN") -> {
                            VegaCottonGinningDryingNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                        currentKey.split("_")[2].contains("COTTPORT") -> {

                        }
                    }
                }
            }
            R.id.navPileManagement -> {
                if (currentKey.split("_")[0].contains("VEGA")) {
                    when {
                        currentKey.split("_")[2].contains("COTTGINN") -> {
                            VegaCottonGinningPileNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                        currentKey.split("_")[2].contains("COTTPORT") -> {
                            VegaCottonPortPileNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                        currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains(
                            "COFF"
                        ) -> {
                            VegaIndiaCoffeePileNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                    }
                }

            }
        }
        bindingActivity?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showLogOutDialog(offlineData: Boolean) {
        MaterialDialog(this).show {
            if (isOnline()) {
                if (offlineData) {
                    message(R.string.offline_data_logout_alert)
                } else {
                    message(R.string.logout_alert)
                }
                getMetirialCustomView(
                    this,
                    getString(R.string.confirm),
                    getString(R.string.cancel),
                    { updateLogOut() },
                    {
                        dismiss()
                        isShowing == false
                    })
                /*positiveButton(text = UIUtils.getSpannedText(getString(R.string.confirm))) {
                     updateLogOut()
                     //clearData()

                 }
                 negativeButton(text = UIUtils.getSpannedText(getString(R.string.cancel))) {
                     dismiss()
                 }*/
            } else {
                message(R.string.offline_logout_alert)
                getMetirialCustomView(this, getString(R.string.ok), "", { dismiss() }, { })
                /*negativeButton(text = UIUtils.getSpannedText(getString(R.string.ok))) {
                    dismiss()
                }*/
            }


        }

    }

    fun clearData() {
        val currentKeyOrigin = PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "")
        val currentNotificatios = PreferenceHelper.get(Constants.NOTIFICATION_LIST, "")
        val userRoles = PreferenceHelper.get(Constants.USER_ROLES, "")
        if (/*!getCurrentKey().split("_")[2].contains("COCO") &&*/ !getCurrentKey().split("_")[1].contains(
                "NI"
            )
        ) {
            DoAsync {
                PreferenceHelper.clear()
                PreferenceHelper.save(Constants.USER_NAME, "")
                PreferenceHelper.save(Constants.CURRENT_ORIGIN_KEY, currentKeyOrigin)
                PreferenceHelper.save(Constants.NOTIFICATION_LIST, currentNotificatios)
                try {
                    AppDatabase.getInstance()?.clearAllTables()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    VegaDatabase.getInstance().clearAllTables()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    DODatabase.getInstance()?.clearAllTables()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.execute()
        } else {
            val currentLotSequence = PreferenceHelper.get(Constants.LOT_SEQUENCE, "")
            val currentGrnSequence = PreferenceHelper.get(Constants.GRN_SEQUENCE, "")
            val currentPoSequence = PreferenceHelper.get(Constants.PO_SEQUENCE, "")
            val currentInvoiceSequence = PreferenceHelper.get(Constants.INVOICE_SEQUENCE, "")
            val currentUserName = PreferenceHelper.get(Constants.SAME_USER_NAME, "")
            PreferenceHelper.save(Constants.USER_NAME, "")
            PreferenceHelper.save(Constants.FGRN_BATCH_SEQUENCE, "")
            PreferenceHelper.save(Constants.PILE_SEQUENCE, "")
            PreferenceHelper.clear()
            PreferenceHelper.save(Constants.NOTIFICATION_LIST, currentNotificatios)
            PreferenceHelper.save(Constants.CURRENT_ORIGIN_KEY, currentKeyOrigin)
            PreferenceHelper.save(Constants.LOT_SEQUENCE, currentLotSequence)
            PreferenceHelper.save(Constants.GRN_SEQUENCE, currentGrnSequence)
            PreferenceHelper.save(Constants.PO_SEQUENCE, currentPoSequence)
            PreferenceHelper.save(Constants.INVOICE_SEQUENCE, currentInvoiceSequence)
            PreferenceHelper.save(Constants.SAME_USER_NAME, currentUserName)
            PreferenceHelper.save(Constants.MTNR_SEQUENCE, "")
            PreferenceHelper.save(Constants.TALLY_SEQUENCE, "")
            PreferenceHelper.save(Constants.FGRN_TALLY_SEQUENCE, "")
            PreferenceHelper.save(Constants.USER_ROLES, userRoles)
            PreferenceHelper.save(Constants.ADMIN_USER, false)
            PreferenceHelper.clear()
            DoAsync {
                //PreferenceHelper.save(Constants.ACCESS_TOKEN, "")
                try {
                    AppDatabase.getInstance()?.clearAllTables()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    VegaDatabase.getInstance().clearAllTables()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.execute()

        }

        /*try {
            *//*val runtime = Runtime.getRuntime()
        runtime.exec("pm clear " + getApplicationContext().getPackageName() + " HERE")*//*
            this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.deleteRecursively()
            FirebaseRemoteConfig.getInstance().reset()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }*/

        goToLoginPage()
    }

    private fun goToLoginPage() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }


    private fun setUpOnlineOfflineToggle() {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        val navHeaderView = bindingNav?.navigationView?.getHeaderView(0)
        tvLogout = bindingNav?.navigationView?.findViewById<TextView>(R.id.tvLogout)
        fliptab1 = navHeaderView?.findViewById<FlipTab>(R.id.fliptab)
        llCloseDrawer = navHeaderView?.findViewById<LinearLayout>(R.id.llCloseDrawer)
        if (getSelectedKeyCount() > 1) {
            val currentKeyStatus = PreferenceHelper.get(currentKey, true)
            if (currentKeyStatus) setOnline() else setOffline()

        } else {
            if (isOnline()) setOnline() else setOffline()
        }
        fliptab1?.setTabSelectedListener(object : FlipTab.TabSelectedListener {
            override fun onTabSelected(isLeftTab: Boolean, tabTextValue: String) {
                if (getSelectedKeyCount() > 1) {
                    val currentKey1 = PreferenceHelper.get(Constants.CURRENT_KEY, "")
                    val currentKeyStatus = PreferenceHelper.get(currentKey1, true)
                    if (isLeftTab) {
                        if (currentKeyStatus) showDialog()
                    } else setOnline()

                } else {
                    if (isLeftTab) showDialog() else setOnline()
                }

            }

            override fun onTabReselected(isLeftTab: Boolean, tabTextValue: String) {
            }
        })

        llCloseDrawer?.setOnClickListener {
            bindingActivity?.drawerLayout?.closeDrawer(
                Gravity.LEFT,
                false
            )
        }
        tvLogout?.setOnClickListener {
            when {
                (currentKey.split("_")[0].contains("DO")) && (!currentKey.split("_")[1].contains("ID")) -> {
                    isOfflineSyncDataAvailable()
                }
                currentKey.split("_")[1].contains("NI") -> {
                    isLogOut = LOGOUT
                    isOfflineNicSyncDataAvailable()
                }
                currentKey.split("_")[2].contains("COCO") -> {
                    isOfflineCocoaSyncDataAvailable()
                }
                else -> showLogOutDialog(false)
            }
        }
    }

    fun setOnline() {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        if (getSelectedKeyCount() > 1) PreferenceHelper.save(currentKey, true)
        fliptab1?.selectRightTab(true)
        PreferenceHelper.save(Constants.USER_ONLINE, true)

        bindingNav?.navigationView?.menu?.findItem(R.id.navLogout)?.isVisible = false

        when {
            currentKey.split("_")[2].contains("COTTGINN") -> {
                bindingNav?.navigationView?.menu?.findItem(R.id.navDataSync)?.isVisible = true
                bindingNav?.navigationView?.menu?.findItem(R.id.navBaleDataSync)?.isVisible = false
                bindingNav?.navigationView?.menu?.findItem(R.id.navLogout)?.isVisible = false
                bindingNav?.navigationView?.menu?.findItem(R.id.navSync)?.isVisible = false
                bindingNav?.navigationView?.menu?.findItem(R.id.navSyncInventory)?.isVisible = false
                bindingNav?.navigationView?.menu?.findItem(R.id.navSyncVendor)?.isVisible = false
                bindingNav?.navigationView?.menu?.findItem(R.id.navSyncVendorCredit)?.isVisible =
                    false
                bindingNav?.navigationView?.menu?.findItem(R.id.navSyncVendorAdvance)?.isVisible =
                    false
                bindingNav?.navigationView?.menu?.findItem(R.id.navSyncExchangeRate)?.isVisible =
                    false

            }
            currentKey.split("_")[0].contains("DO") -> {
                bindingNav?.navigationView?.menu?.findItem(R.id.navSyncVendor)?.isVisible = false
                bindingNav?.navigationView?.menu?.findItem(R.id.navSyncVendorCredit)?.isVisible =
                    false
                bindingNav?.navigationView?.menu?.findItem(R.id.navSyncVendorAdvance)?.isVisible =
                    false
                bindingNav?.navigationView?.menu?.findItem(R.id.navSyncExchangeRate)?.isVisible =
                    false
            }
            currentKey.split("_")[2].contains("COTTPORT") -> {
                bindingNav?.navigationView?.menu?.findItem(R.id.navLogout)?.isVisible = false
                bindingNav?.navigationView?.menu?.findItem(R.id.navSync)?.isVisible = false
                bindingNav?.navigationView?.menu?.findItem(R.id.navSyncInventory)?.isVisible = false
                bindingNav?.navigationView?.menu?.findItem(R.id.navSyncVendor)?.isVisible = false
                bindingNav?.navigationView?.menu?.findItem(R.id.navSyncVendorCredit)?.isVisible =
                    false
                bindingNav?.navigationView?.menu?.findItem(R.id.navSyncVendorAdvance)?.isVisible =
                    false
                bindingNav?.navigationView?.menu?.findItem(R.id.navSyncExchangeRate)?.isVisible =
                    false
            }
            else -> {
                bindingNav?.navigationView?.menu?.findItem(R.id.navSync)?.isVisible = true
                bindingNav?.navigationView?.menu?.findItem(R.id.navSyncInventory)?.isVisible = true

                bindingNav?.navigationView?.menu?.findItem(R.id.navSyncVendor)?.isVisible = true
                if (currentKey.split("_")[1].contains("NI")) {
                    bindingNav?.navigationView?.menu?.findItem(R.id.navSyncVendorCredit)?.isVisible =
                        false
                    bindingNav?.navigationView?.menu?.findItem(R.id.navSyncVendorAdvance)?.isVisible =
                        false
                    bindingNav?.navigationView?.menu?.findItem(R.id.navSyncExchangeRate)?.isVisible =
                        false
                    bindingNav?.navigationView?.menu?.findItem(R.id.navSyncVendor)?.isVisible = getRoles(currentKey).contains("ROLE_VENDOR_SYNC")
                }
            }

        }
//        bindingNav?.navigationView?.menu?.findItem(R.id.navSyncVendor)?.isVisible = !currentKey.split("_")[1].contains("GH")
    }

    fun setOffline() {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        if (getSelectedKeyCount() > 1) PreferenceHelper.save(currentKey, false)
        fliptab1?.selectLeftTab(true)
        PreferenceHelper.save(Constants.USER_ONLINE, false)
        bindingNav?.navigationView?.menu?.findItem(R.id.navLogout)?.isVisible = false
        bindingNav?.navigationView?.menu?.findItem(R.id.navSync)?.isVisible = false
        bindingNav?.navigationView?.menu?.findItem(R.id.navSyncInventory)?.isVisible = false
        bindingNav?.navigationView?.menu?.findItem(R.id.navSyncVendor)?.isVisible = false
        bindingNav?.navigationView?.menu?.findItem(R.id.navSyncVendorCredit)?.isVisible = false
        bindingNav?.navigationView?.menu?.findItem(R.id.navSyncVendorAdvance)?.isVisible = false
        bindingNav?.navigationView?.menu?.findItem(R.id.navSyncExchangeRate)?.isVisible = false
        bindingNav?.navigationView?.menu?.findItem(R.id.navDataSync)?.isVisible = false
        bindingNav?.navigationView?.menu?.findItem(R.id.navBaleDataSync)?.isVisible = false
    }

    fun getSelectedKeyCount(): Int {
        val selectedKey = PreferenceHelper.get(Constants.SELECTED_KEYS, "")
        val selectedKeys = Gson().fromJson<List<String>>(selectedKey)
        return selectedKeys.size
    }

    private fun setSyncTransMaster() {
        val isOnline = isNetworkAvailable()
        when {
            !isOnline && isOnline() -> {
                Snackbar.make(
                    window.decorView.rootView,
                    getString(com.olam.warehouse.presentation.R.string.network_not_available),
                    Snackbar.LENGTH_LONG
                ).show()
                setOffline()
            }
            /*currentView?.snack(
                getString(com.olam.warehouse.presentation.R.string.network_not_available),
                Snackbar.LENGTH_SHORT
            ) {}*/
            else -> fetchTransMasterData()
            // else -> fetchCurrentSeason()
        }
    }

    private fun fetchTransMasterData() {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

        when {
            currentKey.split("_")[2].contains("COTTGINN") -> {
                fetchTransMasterDataForCottonGinning()
            }
            currentKey.split("_")[2].contains("COTTPORT") -> {
                fetchTransMasterDataForCottonPort()
            }
            currentKey.split("_")[1].contains("NI") -> {
                transMaterSyncDetails()
            }
            else -> {

                WorkManager.getInstance(applicationContext).cancelAllWork()
                showCustomLoading(getString(R.string.syncing))
                makeStatusNotification(getString(R.string.data_downloading), applicationContext)
                try {
                    val worker =
                        OneTimeWorkRequest.Builder(TransMasterDataFetchWorker::class.java)
                            .setConstraints(constraintWithoutNetwork)
                            .build()
                    enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_WORKER_TRANS, this)
                    WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
                        .observe(this, Observer { workInfo ->
                            workInfo?.let { info ->
                                when (info.state) {
                                    WorkInfo.State.SUCCEEDED -> {
                                        if ((currentKey.split("_")[1].contains("GH")) && (currentKey.split(
                                                "_"
                                            )[2].contains("COCO"))
                                        ) {
                                            fetchCurrentSeason()
                                        } else {
                                            toast(getString(R.string.data_download_success))
                                            dismissNotification(applicationContext)
                                            hideCustomLoading()
                                            setOffline()
                                        }
                                    }
                                    WorkInfo.State.FAILED -> {
                                        fliptab1?.selectRightTab(true)
                                        workInfo.outputData.getString(TRANS_OUTPUT_DATA)
                                            ?.let { showErrorDialogWithFAQLink(this, it) }
                                        dismissNotification(applicationContext)
                                        hideCustomLoading()
                                    }
                                    WorkInfo.State.CANCELLED -> {
                                        fliptab1?.selectRightTab(true)
                                        workInfo.outputData.getString(TRANS_OUTPUT_DATA)
                                            ?.let { showErrorDialogWithFAQLink(this, it) }
                                        dismissNotification(applicationContext)
                                        hideCustomLoading()

                                    }
                                    else -> {
                                        dismissNotification(applicationContext)
                                    }
                                }
                            }

                        })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun fetchCurrentSeason() {
        //showCustomLoading("Syncing ...")
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        makeStatusNotification("Data downloading ...", applicationContext)
        val worker = OneTimeWorkRequest.Builder(GhanaCocoaSeasonWorker::class.java)
            .setConstraints(constraint).build()
        enQueueWorker(worker, this)

        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            // hideCustomLoading()
                            setOffline()
                            var msg = ""
                            workInfo.outputData.getString(SEASON_OUTPUT_DATA)?.let {
                                msg = it
                                //toast(it)
                                fetchVehicleDetails(msg)
                            }!!
                        }
                        WorkInfo.State.FAILED -> {
                            fliptab1?.selectRightTab(true)
                            workInfo.outputData.getString(SEASON_OUTPUT_DATA)?.let { toast(it) }
                            dismissNotification(applicationContext)
                            hideCustomLoading()
                        }
                        WorkInfo.State.CANCELLED -> {
                            fliptab1?.selectRightTab(true)
                            workInfo.outputData.getString(SEASON_OUTPUT_DATA)?.let { toast(it) }
                            dismissNotification(applicationContext)
                            hideCustomLoading()

                        }
                        else -> {
                            dismissNotification(applicationContext)
                        }
                    }
                }
            })

    }

    private fun fetchVehicleDetails(seasonId: String) {
        //showCustomLoading("Syncing ...")
        makeStatusNotification("Data downloading ..", applicationContext)
        val input = workDataOf(AppUtils.SEASON_ID to seasonId)
        val worker = OneTimeWorkRequest.Builder(GhanaCocoaVehicleWorker::class.java)
            .setConstraints(constraint).setInputData(input).build()
        enQueueWorker(worker, this)

        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            toast(getString(R.string.data_download_success))
                            dismissNotification(applicationContext)
                            hideCustomLoading()
                            setOffline()
                            /* var msg = ""
                             workInfo.outputData.getString(VEHICLE_OUTPUT_DATA)?.let {
                                 msg = it
                                 toast(it)
                             }!!*/
                        }
                        WorkInfo.State.FAILED -> {
                            fliptab1?.selectRightTab(true)
                            workInfo.outputData.getString(VEHICLE_OUTPUT_DATA)?.let { toast(it) }
                            dismissNotification(applicationContext)
                            hideCustomLoading()
                        }
                        WorkInfo.State.CANCELLED -> {
                            fliptab1?.selectRightTab(true)
                            workInfo.outputData.getString(VEHICLE_OUTPUT_DATA)?.let { toast(it) }
                            dismissNotification(applicationContext)
                            hideCustomLoading()

                        }
                        else -> {
                            dismissNotification(applicationContext)
                        }
                    }
                }
            })

    }

    private fun fetchTransMasterDataForCottonPort() {
        showCustomLoading("Syncing ...")
        makeStatusNotification("Data downloading Cotton ...", applicationContext)
        val worker = OneTimeWorkRequest.Builder(PortTransMasterDataWorker::class.java)
            .setConstraints(constraint).build()
        enQueueWorker(worker, this)

        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {

                            VegaCottonPortIncomingMtnSelectionNavigation.dynamicStart?.let {
                                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                applicationContext.startActivity(it)
                            }
                            hideCustomLoading()
                            setOffline()
                        }
                        else -> {
                        }
                    }
                }
            })

    }

    private fun fetchTransMasterDataForCottonGinning() {
        val transItems = arrayListOf<String>("Bale", "Delivery")
        showCustomLoading("Syncing ...")
        makeStatusNotification("Data downloading Cotton ...", applicationContext)
        transItems.forEachIndexed { _index, item ->
            if (item == "Bale") {
                val input = workDataOf(COTTON_GINNING_TRANS_DATA to item)
                val worker =
                    OneTimeWorkRequest.Builder(GinningBaleTransMasterDataWorker::class.java)
                        .setConstraints(constraint).setInputData(input).build()
                enQueueWorker(worker, this)

                WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
                    .observe(this, Observer { workInfo ->
                        if (workInfo != null) {
                            when (workInfo.state) {
                                WorkInfo.State.SUCCEEDED -> {
                                    //toast("Data downloaded successfully")
                                    //dismissNotification(applicationContext)
                                    //hideCustomLoading()
                                    //setOffline()
                                }
                                else -> {
                                }
                            }
                        }
                    })
            } else if (item == "Delivery") {
                val input = workDataOf(COTTON_GINNING_TRANS_DATA to item)
                val worker =
                    OneTimeWorkRequest.Builder(GinningTransDeliveryMasterDataWorker::class.java)
                        .setConstraints(constraint).setInputData(input).build()
                enQueueWorker(worker, this)
                WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
                    .observe(this, Observer { workInfo ->
                        if (workInfo != null) {
                            when (workInfo.state) {
                                WorkInfo.State.SUCCEEDED -> {
                                    toast("Data downloaded successfully")
                                    dismissNotification(applicationContext)
                                    hideCustomLoading()
                                    setOffline()
                                }
                                else -> {
                                }
                            }
                        }
                    })
            }
        }
    }

    private fun showDialog() {
        MaterialDialog(this).show {
            message(com.olam.warehouse.presentation.R.string.tran_master_sync)
            cancelOnTouchOutside(false)
            getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(R.string.cancel),
                {
                    setSyncTransMaster()
                    /*if(PreferenceHelper.get(Constants.DIRECT,"").isNotEmpty()){
                        startTTFarmerDataSync()
                    }*/
                },
                {
                    fliptab1?.selectRightTab(true)
                    dismiss()
                })
        }
    }

    private fun isOfflineSyncDataAvailable() {
        showCustomLoading("Checking Offline Data ...")
        var dataReceiving: Int? = 0
        var dataQuality: Int? = 0
        WorkManager.getInstance(applicationContext).cancelAllWork()
        try {
            val worker =
                OneTimeWorkRequest.Builder(DataFetchWorker::class.java).setConstraints(constraint)
                    .build()
            enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_WORKER, this)
            WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
                .observe(this, Observer { workInfo ->
                    workInfo?.let { info ->
                        when (info.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                hideCustomLoading()
                                dataReceiving =
                                    workInfo.outputData.getInt(DO_RECEIVING_OFFLINE_DATA, 0)
                                dataQuality = workInfo.outputData.getInt(DO_QUALITY_OFFLINE_DATA, 0)
                                if (!isshowing)
                                    showLogOutDialog(
                                        dataReceiving ?: 0 > 0 || dataQuality ?: 0 > 0
                                    )
                            }
                            else -> {
                                hideCustomLoading()
                                isshowing = true
                                showLogOutDialog(false)
                            }
                        }
                    }

                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isOfflineCocoaSyncDataAvailable() {
        showCustomLoading("Checking Offline Data ...")
        var dataReceiving: Int? = 0
        var dataQuality: Int? = 0
        WorkManager.getInstance(applicationContext).cancelAllWork()
        try {
            val worker = OneTimeWorkRequest.Builder(CocoaDataFetchWorker::class.java)
                .setConstraints(constraint).build()
            enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_WORKER, this)
            WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
                .observe(this, Observer { workInfo ->
                    workInfo?.let { info ->
                        when (info.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                hideCustomLoading()
                                dataReceiving =
                                    workInfo.outputData.getInt(DO_RECEIVING_OFFLINE_DATA, 0)
                                dataQuality = workInfo.outputData.getInt(DO_QUALITY_OFFLINE_DATA, 0)
                                showLogOutDialog(
                                    dataReceiving ?: 0 > 0 || dataQuality ?: 0 > 0
                                )
                            }
                            else -> {
                                //hideCustomLoading()
                            }
                        }
                    }

                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isOfflineNicSyncDataAvailable() {
        showCustomLoading("Checking Offline Data ...")
        var dataReceiving: Int? = 0
        WorkManager.getInstance(applicationContext).cancelAllWork()
        try {
            val worker =
                OneTimeWorkRequest.Builder(NicaraguaDataFetchWorker::class.java)
                    .setConstraints(constraint).build()
            enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_WORKER, this)
            WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
                .observe(this, Observer { workInfo ->
                    workInfo?.let { info ->
                        when (info.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                hideCustomLoading()
                                dataReceiving =
                                    workInfo.outputData.getInt(DO_RECEIVING_OFFLINE_DATA, 0)
                                when (isLogOut) {
                                    LOGOUT -> showLogOutDialog(dataReceiving ?: 0 > 0)
//                                    MASTER_SYNC -> if(dataReceiving ?:0 >0) showPendingAlertDialog(this, "Please Sync offline data") else setAutoSyncMaster(isForceFetch = true)
//                                    INVENTORY_SYNC -> if(dataReceiving ?:0 >0) showPendingAlertDialog(this, "") else syncDetails()
//                                    VENDOR_SYNC -> if(dataReceiving ?:0 >0) showPendingAlertDialog(this, "") else inventorySyncData()
                                }
                            }
                            else -> {
                                //hideCustomLoading()
                            }
                        }
                    }

                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    protected fun updateProductHeader() {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        currentKey.let {
            val toolBar = findViewById<Toolbar>(R.id.tool_bar)
            val product =
                toolBar?.findViewById<TextView>(com.olam.warehouse.presentation.R.id.tvProduct)
            try {
                product?.visible()
                when (it.split("_")[2]) {
                    UserProduct.CASHEW.product -> product?.text = getString(R.string.product_cashew)
                    UserProduct.COFFEE.product -> product?.text = getString(R.string.product_coffee)
                    UserProduct.COCOA.product -> product?.text = getString(R.string.product_cocoa)
                    UserProduct.PEPPER.product -> product?.text = getString(R.string.product_pepper)
                    UserProduct.COFFEE1.product -> product?.text =
                        getString(R.string.product_coffee)
                    UserProduct.SESAME.product -> product?.text = getString(R.string.product_sesame)
                    else -> product?.gone()
                }
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
            }
        }
    }

    private fun syncDetails() {

        /*if(getCurrentKey().split("_")[1].contains("CM")){
            val plantList = Gson().fromJson<Plant>(PreferenceHelper.get(Constants.PLANT_DETAILS, ""))
            var list = ArrayList<Plant>()
            list.add(plantList)
            val bottomDialog = VegaInventorySyncDialog.newInstance(list)
            bottomDialog.dialog?.setCanceledOnTouchOutside(false)
            supportFragmentManager.let { it1 -> bottomDialog.show(it1, "SyncBottomSheet") }
        }
        else{*/
        val plantList = Gson().fromJson<List<Plant>>(
            PreferenceHelper.get(
                Constants.PLANT_LIST,
                ""
            )
        )
        var plantMaterialList = arrayListOf<Plant>()
        val existingMatList = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.PLANT_MATERIAL_LIST,""))
        if(!existingMatList.isNullOrEmpty())plantMaterialList.addAll(existingMatList)
        var currPlant: List<Plant>
        currPlant = plantList.filter { it.plantId.equals(getPlantDetails().plantId) }
        if(getRoles(getCurrentKey()).contains("ROLE_BRANCH_CONTROLLER")){
            currPlant = plantList
        }
        var materialList: List<VegaMaterial> = emptyList<VegaMaterial>()
        DoAsync {
            materialList = VegaDatabase.getInstance().vegaNicaraguaGrnDao().getProductsLocal()
            runOnUiThread {
                if(plantMaterialList.isEmpty()){
                    materialList.forEach {
                        val plant = Plant()
                        plant.plantId = getPlantDetails().plantId
                        plant.materialCode = it.materialCode
                        plant.materialName = it.materialName.toString()
                        plantMaterialList.add(plant)
                    }
                }
                val bottomDialog = VegaInventorySyncDialog.newInstance(currPlant as ArrayList<Plant>, plantMaterialList)
                bottomDialog.dialog?.setCanceledOnTouchOutside(false)
                supportFragmentManager.let { it1 -> bottomDialog.show(it1, "SyncBottomSheet") }
            }

        }.execute()
    }

    private fun transMaterSyncDetails() {
        try {
//        showCustomLoading(getString(R.string.syncing))
            val syncTime = DateUtils.getCurrentTimeInMills()
            PreferenceHelper.save(TRANS_FIRST_SYNC, syncTime)
            val bottomDialog = VegaTransMasterSyncDialog.newInstance {
                if (it) setOffline() else fliptab1?.selectRightTab(true)
//            hideCustomLoading()
            }
            bottomDialog.dialog?.setCanceledOnTouchOutside(false)
            supportFragmentManager.let { it1 -> bottomDialog.show(it1, "SyncBottomSheet") }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun updateLastInventorySync() {
        val syncIn: MenuItem? = bindingNav?.navigationView?.menu?.findItem(R.id.navSyncInventory)
        val lastTimeMillis = PreferenceHelper.get(Constants.LAST_SYNC, 0L)
        val lastTime =
            " (last sync: ".plus(getLastInventorySyncTime(this, lastTimeMillis.toString()))
                .plus(")")
        val lastSync = getString(R.string.nav_sync_inventory).plus(lastTime)
        val spannable = SpannableString(lastSync)
        spannable.setSpan(
            ForegroundColorSpan(Color.LTGRAY), 14, 14 + lastTime.length, 0
        )
        syncIn?.title = spannable
    }

    override fun onDestroy() {
        dismissNotification(applicationContext)
        super.onDestroy()
    }

    override fun onStop() {
        dismissNotification(applicationContext)
        super.onStop()
    }

    //Data Sync
    private fun dataSync() {
        showCustomLoading(getString(R.string.data_sync))
        val worker = OneTimeWorkRequest.Builder(DataSyncWorker::class.java)
            .setConstraints(constraint).build()
        enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_WORKER_DATA, this)
        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer { workInfo ->
                workInfo?.let { info ->
                    when {
                        info.state == WorkInfo.State.SUCCEEDED -> {
                            toast(getString(R.string.data_synced_successfully))
                            //workInfo.outputData.getString(DATA_SYNC)?.let { toast(it) }
                            hideCustomLoading()
                        }
                        info.state == WorkInfo.State.FAILED -> {
                            workInfo.outputData.getString(DATA_SYNC)?.let { toast(it) }
                            hideCustomLoading()
                        }
                        info.state == WorkInfo.State.CANCELLED -> {
                            workInfo.outputData.getString(DATA_SYNC)?.let { toast(it) }
                            hideCustomLoading()
                        }
                        else -> {
                        }
                    }
                }
            })
    }

    //Bale Data Sync
    private fun baleDataSync() {
        showCustomLoading(getString(R.string.bale_sync))
        val worker = OneTimeWorkRequest.Builder(BaleDatasyncWorker::class.java)
            .setConstraints(constraint).build()
        enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_WORKER_BALE, this)
        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer { workInfo ->
                workInfo?.let { info ->
                    when {
                        info.state == WorkInfo.State.SUCCEEDED -> {
                            toast(getString(R.string.data_synced_successfully))
                            //workInfo.outputData.getString(BALE_DATA_SYNC)?.let { toast(it) }
                            hideCustomLoading()
                        }
                        info.state == WorkInfo.State.FAILED -> {
                            workInfo.outputData.getString(BALE_DATA_SYNC)?.let { toast(it) }
                            hideCustomLoading()
                        }
                        info.state == WorkInfo.State.CANCELLED -> {
                            workInfo.outputData.getString(BALE_DATA_SYNC)?.let { toast(it) }
                            hideCustomLoading()
                        }
                        else -> {
                        }
                    }
                }
            })
    }

    private fun inventorySyncData() {
        showCustomLoading(getString(R.string.syncing))
        WorkManager.getInstance(this).cancelAllWork()
        try {
            val worker = OneTimeWorkRequest.Builder(VendorSyncWorker::class.java)
                .setConstraints(constraint).build()
            enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_WORKER_INVENTORY, this)
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(worker.id)
                .observe(this, Observer { workInfo ->
                    workInfo?.let { info ->
                        when (info.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                var msg = ""
                                workInfo.outputData.getString(VENDORY_SYNC_OUTPUT_DATA)?.let {
                                    msg = it
                                    toast(it)
                                }!!
                                hideCustomLoading()
                            }
                            WorkInfo.State.FAILED -> {
                                try {
                                    var msg = ""
                                    workInfo.outputData.getString(VENDORY_SYNC_OUTPUT_DATA)?.let {
                                        msg = it
                                        //activity?.toast(it)
                                    }!!
                                    val times = msg.split('#', '.')
                                    if (times.size > 1) toast(times.get(0)) else toast(times.toString())
                                } catch (e: NullPointerException) {
                                    e.printStackTrace()
                                }
                                hideCustomLoading()
                            }
                            WorkInfo.State.CANCELLED -> {
                                workInfo.outputData.getString(VENDORY_SYNC_OUTPUT_DATA)
                                    ?.let { toast(it) }!!
                                hideCustomLoading()
                            }
                            else -> {
                            }
                        }
                    }

                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vendorCreditLimitSyncData() {
        showCustomLoading(getString(R.string.syncing))
        WorkManager.getInstance(this).cancelAllWork()
        try {
            val worker = OneTimeWorkRequest.Builder(VendorCreditLimitSyncWorker::class.java)
                .setConstraints(constraint).build()
            enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_WORKER_VENDOR_CREDIT, this)
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(worker.id)
                .observe(this, Observer { workInfo ->
                    workInfo?.let { info ->
                        when (info.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                var msg = ""
                                workInfo.outputData.getString(VENDORY_SYNC_OUTPUT_DATA)?.let {
                                    msg = it
                                    UIUtils.showSuccessDialog(this, it)
                                }!!
                                hideCustomLoading()
                            }
                            WorkInfo.State.FAILED -> {
                                try {
                                    var msg = ""
                                    workInfo.outputData.getString(VENDORY_SYNC_OUTPUT_DATA)?.let {
                                        msg = it
                                    }!!
                                    showErrorDialogWithFAQLink(
                                        this,
                                        if (msg.isNotEmpty()) msg else getString(R.string.sync_error)
                                    )
                                } catch (e: NullPointerException) {
                                    e.printStackTrace()
                                }
                                hideCustomLoading()
                            }
                            WorkInfo.State.CANCELLED -> {
                                workInfo.outputData.getString(VENDORY_SYNC_OUTPUT_DATA)
                                    ?.let { toast(it) }!!
                                hideCustomLoading()
                            }
                            else -> {
                            }
                        }
                    }

                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vendorAdvanceItemSyncData() {
        showCustomLoading(getString(R.string.syncing))
        WorkManager.getInstance(this).cancelAllWork()
        try {
            val worker = OneTimeWorkRequest.Builder(VendorAdvanceItemSyncWorker::class.java)
                .setConstraints(constraint).build()
            enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_WORKER_VENDER_ADVANCE_CREDIT, this)
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(worker.id)
                .observe(this, Observer { workInfo ->
                    workInfo?.let { info ->
                        when (info.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                var msg = ""
                                workInfo.outputData.getString(VENDORY_SYNC_OUTPUT_DATA)?.let {
                                    msg = it
                                    UIUtils.showSuccessDialog(this, it)
                                }!!
                                hideCustomLoading()
                            }
                            WorkInfo.State.FAILED -> {
                                try {
                                    var msg = ""
                                    workInfo.outputData.getString(VENDORY_SYNC_OUTPUT_DATA)?.let {
                                        msg = it
                                    }!!
                                    showErrorDialogWithFAQLink(
                                        this,
                                        if (msg.isNotEmpty()) msg else getString(R.string.sync_error)
                                    )
                                } catch (e: NullPointerException) {
                                    e.printStackTrace()
                                }
                                hideCustomLoading()
                            }
                            WorkInfo.State.CANCELLED -> {
                                workInfo.outputData.getString(VENDORY_SYNC_OUTPUT_DATA)
                                    ?.let { toast(it) }!!
                                hideCustomLoading()
                            }
                            else -> {
                            }
                        }
                    }

                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun exchangeRateSyncData() {
        showCustomLoading(getString(R.string.syncing))
        WorkManager.getInstance(this).cancelAllWork()
        try {
            val worker = OneTimeWorkRequest.Builder(ExchangeRateSyncWorker::class.java)
                .setConstraints(constraint).build()
            enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_WORKER_EXCHANGE_RATE, this)
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(worker.id)
                .observe(this, Observer { workInfo ->
                    workInfo?.let { info ->
                        when (info.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                var msg = ""
                                workInfo.outputData.getString(VENDORY_SYNC_OUTPUT_DATA)?.let {
                                    msg = it
                                    UIUtils.showSuccessDialog(this, it)
                                }!!
                                hideCustomLoading()
                            }
                            WorkInfo.State.FAILED -> {
                                try {
                                    var msg = ""
                                    workInfo.outputData.getString(VENDORY_SYNC_OUTPUT_DATA)?.let {
                                        msg = it
                                        //activity?.toast(it)
                                    }!!
                                    val times = msg.split('#', '.')
                                    if (times.size > 1) showErrorDialogWithFAQLink(
                                        this,
                                        times.get(0)
                                    ) else showErrorDialogWithFAQLink(this, times.toString())
                                } catch (e: NullPointerException) {
                                    e.printStackTrace()
                                }
                                hideCustomLoading()
                            }
                            WorkInfo.State.CANCELLED -> {
                                workInfo.outputData.getString(VENDORY_SYNC_OUTPUT_DATA)
                                    ?.let {
                                        showErrorDialogWithFAQLink(this, it)
                                        //toast(it)
                                    }!!
                                hideCustomLoading()
                            }
                            else -> {
                            }
                        }
                    }

                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateLogOut() {
        WorkManager.getInstance(applicationContext).cancelAllWork()
        try {
            val input = workDataOf(AppUtils.DEVICE_ID to AppUtils.getDeviceID(this))
            val worker =
                OneTimeWorkRequest.Builder(LogoutWorker::class.java).setConstraints(constraint)
                    .setInputData(input)
                    .build()
            enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_WORKER_LOGOUT, this)
            WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
                .observe(this, Observer { workInfo ->
                    workInfo?.let { info ->
                        when (info.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                clearData()
                            }
                            WorkInfo.State.FAILED -> {
                                clearData()
                            }
                            WorkInfo.State.CANCELLED -> {
                                clearData()
                            }
                            else -> {}
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Blutooth
    private val mPairedDevices = arrayListOf<BluetoothDevice>()

    //val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var mBTAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mBTSocket: BluetoothSocket? = null
    private lateinit var mInputStream: InputStream
    private lateinit var mOutputStream: OutputStream
    private val mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var mWeight = ""
    private var callBack: BtObserve? = null

    interface BtObserve {
        fun valueObserve(btValue: String)
    }

    
    fun initBt(listener: BtObserve) {
        callBack = listener
        registerBtListener()
        if (checkBluetoothConnectPermission()) {
            mBTAdapter = BluetoothAdapter.getDefaultAdapter()
            when {
                mBTAdapter.isEnabled && mBTSocket?.isConnected == false -> getPairedDevices()
                else -> {

                    startActivityForResult(
                        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                        UIUtils.REQUEST_ENABLE_BT
                    )

                }
            }
        }
    }


    
    private fun getPairedDevices() {
        if (checkBluetoothConnectPermission()) {
            val pairedMac = PreferenceHelper.get(UIUtils.BT_MAC, "")
            var uuids = if (mPairedDevices.isNotEmpty()) mPairedDevices[PreferenceHelper.get(
                UIUtils.BT_UUID,
                0
            )].uuids else emptyArray()
            val pairedUUID = if (uuids.isNotEmpty()) uuids[0].uuid else mUUID
            if (pairedMac.isBlank()) {
                showPairedDeviceDialog()
            } else {
                showLoading()
                // doAsync {
                try {
                    val device = mBTAdapter.getRemoteDevice(pairedMac)
                    mBTSocket?.close()
                    //mBTSocket = device.createRfcommSocketToServiceRecord(pairedUUID)
                    mBTSocket = device.createRfcommSocketToServiceRecord(mUUID)
                    mBTSocket?.let {
                        if (it.isConnected) it.close()
                        it.connect()
                        if (it.isConnected) {
                            //  runOnUiThread {
                            hideLoading()
                            toast("Weighscale connected successfully")
                            //}
                            try {
                                DoAsync {
                                    mInputStream = it.inputStream
                                    var data = ""
                                    var bytes: Int
                                    while (true) {
                                        try {
                                            val buffer = ByteArray(1024)
                                            /*bytes = */mInputStream.read(buffer, 0, buffer.size)
                                            data = String(buffer, 0, buffer.size, Charsets.UTF_8)
                                            val dataArr = data.split("\r\n", "\r", "\n", " ")
//                                            runOnUiThread { toast("WeightTest1: $data") }
                                            dataArr.forEach {
                                                data = Regex("[^0-9.]").replace(it, "")
                                                mWeight = data
                                                runOnUiThread {
                                                    //toast("WeightTest: $mWeight")
                                                    try {
                                                        if (mWeight.toDouble() > 0) {
                                                            callBack?.valueObserve(mWeight)
                                                        }
                                                    } catch (e: java.lang.Exception) {
                                                        e.printStackTrace()
                                                    }

                                                }
                                               // callBack?.valueObserve(mWeight)
                                                try {
                                                    if (mWeight.toDouble() > 0) data = ""
                                                } catch (e: java.lang.Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        } catch (e: IOException) {
                                            Log.d("BluetoothReadData", e.message ?: "")
                                            break
                                        }
                                    }
                                }.execute()
                            } catch (e: Exception) {
                                Log.d("BluetoothReadData", e.message ?: "")
                            }
                        }

                    }
                } catch (e: Exception) {
                    Log.d("BluetoothConnect", e.message ?: "")
                    runOnUiThread { toast("Connection Issue") }
                    mBTSocket?.close()
                    hideLoading()
                    /*try {
                        hideLoading()
                        mBTSocket?.close()
                        mBTSocket = mBTAdapter.getRemoteDevice(pairedMac)
                            .createRfcommSocketToServiceRecord(mUUID)
                        mBTSocket?.connect()
                        runOnUiThread { toast("Connection: ${mBTSocket?.isConnected}") }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }*/
                }
                //}.execute()
            }
        }
    }

    
    private fun showPairedDeviceDialog() {
        if(checkBluetoothConnectPermission()) {
            mPairedDevices.clear()
            mPairedDevices.addAll(mBTAdapter.bondedDevices)
            val pairedDeviceName = mBTAdapter.bondedDevices.map { it.name }
            MaterialDialog(this).show {
                title(text = getString(com.olam.warehouse.presentation.R.string.please_select_the_weighing_scale))
                listItemsSingleChoice(items = pairedDeviceName) { dialog, index, text ->
                    if (mPairedDevices.isNotEmpty()) {
                        PreferenceHelper.save(UIUtils.BT_MAC, mPairedDevices[index].address)
                        PreferenceHelper.save(UIUtils.BT_UUID, index)
                        mBTSocket?.close()
                        mBTAdapter = BluetoothAdapter.getDefaultAdapter()
                        when {
                            mBTAdapter.isEnabled -> getPairedDevices()
                            else -> startActivityForResult(
                                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                                UIUtils.REQUEST_ENABLE_BT
                            )
                        }
                    }
                }
                /* getMetirialCustomView(
                 this,
                 getString(com.olam.warehouse.presentation.R.string.ok),
                 "",
                 { dismiss() },
                 { dismiss() })*/
            }
        }
    }

    fun closeSocket() {
        try {
            unregisterReceiver(btConnectReceiver)
            if (mBTSocket != null) {
                mBTSocket?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Bluetooth Connect Receiver
    private val btConnectReceiver = object : BroadcastReceiver() {
        
        override fun onReceive(contxt: Context?, intent: Intent?) {
            when (intent?.action) {
                Constants.IS_BT_CONNECTED -> {
                    try {
                        //if (mBTSocket?.isConnected == true) {
                        mBTSocket?.close()
                        mBTAdapter = BluetoothAdapter.getDefaultAdapter()
                        when {
                            mBTAdapter.isEnabled -> getPairedDevices()
                            else -> {
                                if (checkBluetoothConnectPermission())
                                    startActivityForResult(
                                        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                                        UIUtils.REQUEST_ENABLE_BT
                                    )
                            }
                        }
                        //}
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                }
                Constants.SWITCH_BT_DEVICE -> {
                    showPairedDeviceDialog()
                }
            }
        }
    }

    fun registerBtListener() {
        if (!btConnectReceiver.isOrderedBroadcast) {
            val intentFilterBt = IntentFilter(Constants.IS_BT_CONNECTED)
            val intentFilterBtSwitch = IntentFilter(Constants.SWITCH_BT_DEVICE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(btConnectReceiver, intentFilterBt, RECEIVER_EXPORTED)
                registerReceiver(btConnectReceiver, intentFilterBtSwitch, RECEIVER_EXPORTED)
            }else{
                registerReceiver(btConnectReceiver, intentFilterBt)
                registerReceiver(btConnectReceiver, intentFilterBtSwitch)
            }

        }
    }

    interface DialogSingleClick {
        fun onClick(dialog: DialogInterface)
    }

    protected fun showDialog(msg: String) {
        MaterialDialog(this).show {
            message(text = msg)
            getMetirialCustomView(this, getString(R.string.ok), "", { dismiss() }, { dismiss() })
        }
    }

    fun showDialog(
        message: String,
        listener: DialogClick,
        isColor: Boolean = true,
        postiveText: Int = R.string.yes,
        negativeText: Int = R.string.cancel
    ) {

        //val view = LayoutInflater.from(this).inflate(R.layout.dialog_start_loading_layout1, null)
        val view = DialogStartLoadingLayout1Binding.inflate(layoutInflater)
        view.tvMsg.text = message
        view.etRemark.gone()
        val alertBuilder = AlertDialog.Builder(this).setView(view.root)
        val alertDialog = alertBuilder.create()
//        view.btConform.text = postiveText.toString()
//        view.btCancel.text = negativeText.toString()
        view.btConform.setOnClickListener {
            listener.onPositive(alertDialog)
            alertDialog.dismiss()
        }
        view.btCancel.setOnClickListener {
            listener.onNegative(alertDialog)
            alertDialog.dismiss()
        }
        ViewCompat.setBackgroundTintList(
            view.btCancel,
            ContextCompat.getColorStateList(
                this.applicationContext!!,
                R.color.colorPrimaryOfi
            )
        )
        alertDialog.show()

    }

    interface DialogClick {
        fun onPositive(dialog: DialogInterface)
        fun onNegative(dialog: DialogInterface)
    }

    fun showSingleDialog(message: String, listener: DialogSingleClick) {
//        val view = LayoutInflater.from(this).inflate(R.layout.dialog_start_loading_layout1, null)
        val view = DialogStartLoadingLayout1Binding.inflate(layoutInflater)
        view.tvMsg.text = message
        view.etRemark.gone()
        val alertBuilder = AlertDialog.Builder(this).setView(view.root)
        val alertDialog = alertBuilder.create()
        view.btConform.text = "OK"
        view.btCancel.gone()
        view.btConform.setOnClickListener {
            listener.onClick(alertDialog)
            alertDialog.dismiss()
        }
        alertDialog.show()

    }

    private fun checkQuickPin() {
        val isDevicePin = PreferenceHelper.get(Constants.IS_DEVICE_PIN, false)
        val createdNewPin = PreferenceHelper.get(Constants.QUICK_PIN, "")
        if (isDevicePin) {
            authenticateApp()
        } else {
            if (createdNewPin.isEmpty()) {
                startActivity(Intent(this, VegaQuickAccessPinActivity::class.java))
            } else {
                val intent = Intent(this, VegaCreatePinActivity::class.java)
                intent.putExtra(UIUtils.EXTRA_SET_PIN, false)
                startActivityForResult(intent, REQUEST_CODE)
            }
        }
    }

    //method to authenticate app
    private fun authenticateApp() {
        //Get the instance of KeyGuardManager
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        //Check if the device version is greater than or equal to Lollipop(21)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Create an intent to open device screen lock screen to authenticate
            //Pass the Screen Lock screen Title and Description
            val i = keyguardManager.createConfirmDeviceCredentialIntent(
                resources.getString(R.string.unlock),
                resources.getString(R.string.confirm_pattern)
            )
            try {
                //Start activity for result
                startActivityForResult(i, LOCK_REQUEST_CODE)
            } catch (e: Exception) {

                //If some exception occurs means Screen lock is not set up please set screen lock
                //Open Security screen directly to enable patter lock
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                try {

                    //Start activity for result
                    startActivityForResult(intent, SECURITY_SETTING_REQUEST_CODE)
                } catch (ex: Exception) {

                    //If app is unable to find any Security settings then user has to set screen lock manually
//                    textView.setText(resources.getString(R.string.setting_label))
                }
            }
        }
    }

    /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         if (resultCode == Activity.RESULT_OK) {
             if (requestCode == UIUtils.REQUEST_ENABLE_BT) getPairedDevices()
         }
     }*/

    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            UIUtils.REQUEST_ENABLE_BT -> if (resultCode == Activity.RESULT_OK) getPairedDevices()
            LOCK_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                //If screen lock authentication is success update text
                startActivity(Intent(this, VegaQuickAccessPinActivity::class.java))
            } else {
                //If screen lock authentication is failed update text
//                textView.setText(resources.getString(R.string.unlock_failed))
            }
            SECURITY_SETTING_REQUEST_CODE ->                 //When user is enabled Security settings then we don't get any kind of RESULT_OK
                //So we need to check whether device has enabled screen lock or not
                if (isDeviceSecure()) {
                    //If screen lock enabled show toast and start intent to authenticate user
                    toast(resources.getString(R.string.device_is_secure))
                    authenticateApp()
                } else {
                    //If screen lock is not enabled just update text
//                    textView.setText(resources.getString(R.string.security_device_cancelled))
                }
            REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                //If screen lock authentication is success update text
                startActivity(Intent(this, VegaQuickAccessPinActivity::class.java))
            } else {
                //If screen lock authentication is failed update text
//                textView.setText(resources.getString(R.string.unlock_failed))
            }
        }
    }

    /**
     * method to return whether device has screen lock enabled or not
     */
    private fun isDeviceSecure(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        //this method only work whose api level is greater than or equal to Jelly_Bean (16)
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && keyguardManager.isKeyguardSecure
        //You can also use keyguardManager.isDeviceSecure(); but it requires API Level 23
    }

    
    private fun checkBluetoothConnectPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permission = ActivityCompat.checkSelfPermission(
                this@HomeBaseActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            )
            return if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    1
                )
                false
            } else {
                true
            }
        } else return true
    }

    private val REQUEST_CODE_ASK_PERMISSIONS = 1002

    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        applicationContext,
                        "PERMISSION_STATE Denied",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
            ALL_PERMISSIONS -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                when (requestCode) {
                    1 -> {
                        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            if ((ContextCompat.checkSelfPermission(
                                    this@HomeBaseActivity,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) == PackageManager.PERMISSION_GRANTED)
                            ) {
                                callBack?.let { initBt(it) }
                            }
                        } else {
                            Toast.makeText(this, "PERMISSION_STATE Denied", Toast.LENGTH_LONG)
                                .show()
                        }
                        return
                    }
                }
            }
        }
    }

    fun showTTDialog(navigationType:DynamicFeature<Intent>, msg:Int, procurementType:Boolean) {
        MaterialDialog(this).show {
            message(msg)
            UIUtils.getTTDialogOnlyForMaterial(
                this,
                "",
                "",
                {
                        isComplaint, isThirdParty, isFarmerLessTransaction ->  if(isThirdParty)
                    navigateToTrackTracePage(navigationType, Constants.TRACK_TRACE_THIRD_PARTY,"")
                else if (isFarmerLessTransaction)
                    navigateToTrackTracePage(navigationType, Constants.TRACK_TRACE_FARMERLESS_TRANSACTION, "")
                else if
                             (isComplaint)navigateToTrackTracePage(navigationType, Constants.DIRECT,"")
                else
                    navigateToTrackTracePage(navigationType, Constants.IN_DIRECT,"")

                },

                /*{
                   if(it == true){
                        navigateToTrackTracePage(navigationType, DIRECT,"")
                    }else{
                        navigateToTrackTracePage(navigationType, IN_DIRECT,"")
                    }
                }*/
                { dismiss() },procurementType)
        }
    }

    private fun navigateToTrackTracePage(navigationType:DynamicFeature<Intent>, procurementType:String, complaintType:String){
        navigationType.dynamicStart?.let {
            it.putExtra(Constants.PROCUREMENT_TYPE, procurementType)
            it.putExtra(Constants.COMPLAINT_TYPE, complaintType)
            startActivity(it)
        }
    }
}
