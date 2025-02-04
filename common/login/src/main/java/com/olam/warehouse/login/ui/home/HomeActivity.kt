package com.olam.warehouse.login.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.distribute.Distribute
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.ActivityHomeBinding
import com.olam.warehouse.login.di.*
import com.olam.warehouse.login.ui.appcenterdialog.AppDistributeListener
import com.olam.warehouse.login.ui.common.VegaCocoaDispatchTypeActivity
import com.olam.warehouse.login.ui.common.VegaCoffeeDispatchTypeActivity
import com.olam.warehouse.login.ui.transaction.TransactionActivity
import com.olam.warehouse.login.ui.transaction.TransactionViewModel
import com.olam.warehouse.master.common.data.work.VegaScanDetailsWorkerForGinning
import com.olam.warehouse.master.common.data.work.VegaScanDetailsWorkerForPort
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.master.user.model.HomeItems
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.GenericScanDetails
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortBale
import com.olam.warehouse.master.work.getFeatureReleaseOneTimeRequestWorker
import com.olam.warehouse.master.work.getFeatureReleaseUrlOneTimeRequestWorker
import com.olam.warehouse.navigation.features.*
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.databinding.ToolbarBinding
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.enums.UserRoles.Companion.valueOfEnum
import com.olam.warehouse.presentation.ui.widget.FlipTab
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.canSyncMaster
import com.olam.warehouse.presentation.utils.AppUtils.getEnviroment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.Constants.ACTIVITY_SCAN_REQUEST_CODE
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_CM_COCO_PROD
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_COCO_PROD
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_COCO_UAT
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_COFF_PROD
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_COFF_UAT
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_ECUA_PROD
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_ECUA_UAT
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_GHANA_CASHEW_PROD
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_GH_COCO_PROD
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_INDO_COFFEE
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_IVC_CASHEW_PROD
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_IVC_COTTON
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_NG_CASHEW_PROD
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_NICA_PROD
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_NICA_UAT
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_PROD_OD
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_SESAME_PROD
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_TOGO_COTTON
import com.olam.warehouse.presentation.utils.Constants.APPCENTER_SECRET_UAT_OD
import com.olam.warehouse.presentation.utils.Constants.SCANNED_ID
import com.olam.warehouse.presentation.utils.PreferenceHelper.ÏS_LOGGED_OUT
import com.olam.warehouse.presentation.utils.UIUtils.COCOA_MTNR
import com.olam.warehouse.presentation.utils.UIUtils.COCOA_MTNT
import com.olam.warehouse.presentation.utils.UIUtils.REPRINT
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.presentation.utils.extension.visible
import kotlinx.android.synthetic.main.item_home.view.*
import kotlinx.android.synthetic.main.navigation_view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by SangiliPandian C on 26-11-2019.
 */
class HomeActivity : HomeBaseActivity() {

    private var currentKey = ""
    private var selectedKeys = emptyList<String>()
    private var homeList = mutableListOf<HomeItems>()
    private var roleData = listOf<UserRole>()
    private var menu: Menu? = null
    private var tvProcureType: TextView? = null

    private lateinit var bindingActivity: ActivityHomeBinding
    private lateinit var bindingToolbar: ToolbarBinding
    private val vm: TransactionViewModel by viewModel()

    override val layoutResourceId = R.layout.activity_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingActivity = ActivityHomeBinding.inflate(layoutInflater)
        bindingToolbar = ToolbarBinding.inflate(layoutInflater)
        setContentView(bindingActivity.root)
        getCurrenKeyModel()
        if (currentKey.split("_")[2].contains("COTT")) {
            injectCottonFeature()
            injectDOFeature()
            injectVegaFeature()
            injectCommonFeature()
        } else {
            if (selectedKeys.size > 1 || selectedKeys.any { it.split("_")[0].contains("VEGA") }) {
                injectDOFeature()
                injectVegaFeature()
                injectCommonFeature()
            } else {
                injectDOFeature()
                injectDOCommonFeature()
            }
        }
        /*injectDOFeature()
        injectVegaFeature()
        injectCommonFeature()*/

        initNavigationView()
        initUI()
        //App Center sdk Integration
        appcenterListenser()
    }

    fun appcenterListenser() {
        Distribute.setListener(AppDistributeListener())
        Distribute.setEnabledForDebuggableBuild(true)
        if (currentKey.split("_")[0].contains("DO")) {
            when (AppUtils.getEnviroment()) {
                "release" -> {
                    when {
                        currentKey.split("_")[1].contains("GH") -> {
                        }
                        currentKey.split("_")[1].contains("ID") && currentKey.split("_")[2].contains("ARAB") -> {
                        }
                        else -> AppCenter.start(application, APPCENTER_SECRET_PROD_OD, Distribute::class.java)
                    }
                }
                "uat" -> {
                    when {
                        currentKey.split("_")[1].contains("GH") -> {
                        }
                        currentKey.split("_")[1].contains("ID") && currentKey.split("_")[2].contains("ARAB") -> {
                        }
                        else -> AppCenter.start(application, APPCENTER_SECRET_UAT_OD, Distribute::class.java)
                    }
                }
            }
        } else if (currentKey.split("_")[0].contains("VEGA")) {
            when (AppUtils.getEnviroment()) {
                "release" -> {
                    when {
                        currentKey.split("_")[1].contains("EC") -> AppCenter.start(
                            application,
                            APPCENTER_SECRET_ECUA_PROD,
                            Distribute::class.java
                        )
                        currentKey.split("_")[1].contains("NI") -> AppCenter.start(
                            application,
                            APPCENTER_SECRET_NICA_PROD,
                            Distribute::class.java
                        )
                        currentKey.split("_")[1].contains("IV") && currentKey.split("_")[2].contains(
                            "COFF"
                        ) -> AppCenter.start(
                            application,
                            APPCENTER_SECRET_COFF_PROD,
                            Distribute::class.java
                        )
                        currentKey.split("_")[1].contains("IV") && currentKey.split("_")[2].contains(
                            "COCO"
                        ) -> AppCenter.start(
                            application,
                            APPCENTER_SECRET_COCO_PROD,
                            Distribute::class.java
                        )
                        currentKey.split("_")[2].contains("COTT") -> {
                            if (currentKey.split("_")[1].contains("IV")) {
                                AppCenter.start(
                                    application,
                                    APPCENTER_SECRET_IVC_COTTON,
                                    Distribute::class.java
                                )
                            } else {
                                AppCenter.start(
                                    application,
                                    APPCENTER_SECRET_TOGO_COTTON,
                                    Distribute::class.java
                                )
                            }


                        }
                        currentKey.split("_")[1].contains("ID") && currentKey.split("_")[2].contains(
                            "ARAB"
                        ) -> AppCenter.start(
                            application,
                            APPCENTER_SECRET_INDO_COFFEE,
                            Distribute::class.java
                        )
                        currentKey.split("_")[1].contains("IV") && currentKey.split("_")[2].contains(
                            "CASH"
                        ) -> {
                            AppCenter.start(
                                application,
                                APPCENTER_SECRET_IVC_CASHEW_PROD,
                                Distribute::class.java
                            )
                        }
                        currentKey.split("_")[2].contains("SESA") -> AppCenter.start(
                            application,
                            APPCENTER_SECRET_SESAME_PROD,
                            Distribute::class.java
                        )
                        currentKey.split("_")[1].contains("GH") && currentKey.split("_")[2].contains(
                            "CASH"
                        ) -> AppCenter.start(
                            application,
                            APPCENTER_SECRET_GHANA_CASHEW_PROD,
                            Distribute::class.java
                        )
                        currentKey.split("_")[1].contains("NG") && currentKey.split("_")[2].contains(
                            "CASH"
                        ) -> AppCenter.start(
                            application,
                            APPCENTER_SECRET_NG_CASHEW_PROD,
                            Distribute::class.java
                        )
                        currentKey.split("_")[1].contains("CM") && currentKey.split("_")[2].contains(
                            "COCO"
                        ) -> AppCenter.start(
                            application,
                            APPCENTER_SECRET_CM_COCO_PROD,
                            Distribute::class.java
                        )
                        currentKey.split("_")[1].contains("GH") && currentKey.split("_")[2].contains(
                            "COCO"
                        ) -> AppCenter.start(
                            application,
                            APPCENTER_SECRET_GH_COCO_PROD,
                            Distribute::class.java
                        )
                    }
                    //AppCenter.start(application, APPCENTER_SECRET, Distribute::class.java)
                }
                "uat" -> {
                    when {
                        currentKey.split("_")[1].contains("EC") -> AppCenter.start(
                            application,
                            APPCENTER_SECRET_ECUA_UAT,
                            Distribute::class.java
                        )
                        currentKey.split("_")[1].contains("NI") -> AppCenter.start(
                            application,
                            APPCENTER_SECRET_NICA_UAT,
                            Distribute::class.java
                        )
                        currentKey.split("_")[1].contains("IV") && currentKey.split("_")[2].contains(
                            "COFF"
                        ) -> AppCenter.start(
                            application,
                            APPCENTER_SECRET_COFF_UAT,
                            Distribute::class.java
                        )
                        currentKey.split("_")[1].contains("IV") && currentKey.split("_")[2].contains(
                            "COCO"
                        ) -> AppCenter.start(
                            application,
                            APPCENTER_SECRET_COCO_UAT,
                            Distribute::class.java
                        )
                        currentKey.split("_")[1].contains("IV") && currentKey.split("_")[2].contains(
                            "CASH"
                        ) -> {
                        }
                    }
                    //AppCenter.start(application, APPCENTER_SECRET_UAT, Distribute::class.java)
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (!currentKey.split("_")[2].contains("COTT")) {
            when {
                PreferenceHelper.get(ÏS_LOGGED_OUT, false) || canSyncMaster() -> {
                    PreferenceHelper.save(ÏS_LOGGED_OUT, false)
                    setAutoSyncMaster(true)
                }
                else -> schedulePeriodicMasterWorker()
            }
        }
    }

    private fun getCurrenKeyModel() {
        val selectedKey = PreferenceHelper.get(Constants.SELECTED_KEYS, "")
        selectedKeys = Gson().fromJson<List<String>>(selectedKey)

        if (PreferenceHelper.get(Constants.CURRENT_KEY, "").isEmpty()) {
            selectedKeys.forEachIndexed { index, key ->
                if (index == 0) {
                    currentKey = key
                    PreferenceHelper.save(Constants.CURRENT_KEY, key)
                    updateProductHeader()
                }
            }
        } else {
            currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
            updateProductHeader()
        }
    }

    private fun initUI() {
        val navHeaderView = navigationView.getHeaderView(0)
        navigationView.itemIconTintList = null
        val tvLastSyncTime = navHeaderView.findViewById<TextView>(R.id.tvLastSyncTime)
        val llLastSyncTime = navHeaderView.findViewById<LinearLayout>(R.id.llLastSyncTime)

        val tvRole = navHeaderView.findViewById<TextView>(R.id.tvRole)
        tvProcureType = navHeaderView.findViewById<TextView>(R.id.tvProcureType)
        val tvUsername = navHeaderView.findViewById<TextView>(R.id.tvUsername)
        tvRole.text = PreferenceHelper.get(Constants.USER_ROLE, "")
        tvUsername.text = PreferenceHelper.get(Constants.USER_NAME, "")

        val isSecurityPin = PreferenceHelper.get(Constants.IS_SECURITY_PIN, false)
        navigationView.menu.findItem(R.id.navQuickPin).isVisible = isSecurityPin

        // vm.userRoles.observe(this, Observer { updateUI(it) })
        /*val selectedKey = PreferenceHelper.get(Constants.SELECTED_KEYS, "")
        selectedKeys = Gson().fromJson<List<String>>(selectedKey)

        if (PreferenceHelper.get(Constants.CURRENT_KEY, "").isEmpty()) {
            selectedKeys.forEachIndexed { index, key ->
                if (index == 0) {
                    currentKey = key
                    PreferenceHelper.save(Constants.CURRENT_KEY, key)
                    updateProductHeader()
                }
            }
        } else {
            currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
            updateProductHeader()
        }*/
        //vm.getUserRoles(currentKey)
        roleData = getRoles(currentKey)
        updateUI(roleData)

        if (!AppUtils.getEnviroment().equals("release")) bindingActivity.tvVersion.text =
            AppUtils.getVersionName().plus("(").plus(AppUtils.getEnviroment()).plus(")")
        else bindingActivity.tvVersion.text = AppUtils.getVersionName()
        //getAppcenterReleaseId()
        if (currentKey.split("_")[0].contains("VEGA")) {
            vm.lastSync.observe(this, Observer {
                val timestamp = PreferenceHelper.get(Constants.LAST_SYNC_TIME, "")
                if (timestamp.isNotEmpty()) {
                    if (currentKey.split("_")[1].contains("NI")) llLastSyncTime.visible()
                    tvLastSyncTime.text = DateUtils.getUTCDateWithTime(timestamp, this)
                }
            })
            vm.getLastSyncTime()
        }
        try {
            val timestamp = PreferenceHelper.get(Constants.LAST_SYNC_TIME, "")
            if (timestamp.isNotEmpty()) {
                if (currentKey.split("_")[1].contains("NI")) llLastSyncTime.visible()
                tvLastSyncTime.text = DateUtils.getUTCDateWithTime(timestamp, this)
            }
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }

    private fun getAppcenterReleaseId() {
        var baseFeature = ""
        when (getEnviroment()) {
            "release" -> baseFeature = PROD_BASE_PATH
            "uat" -> baseFeature = BASE_PATH
        }
        if (baseFeature.isNotEmpty()) {
            //showLoading()
            val basePath =
                if (getEnviroment().equals("uat")) "https://api.appcenter.ms/v0.1/apps/olam-vega/VegaX-Uat/distribution_groups/$baseFeature/releases"
                else "https://api.appcenter.ms/v0.1/apps/olam-vega/vegax/distribution_groups/$baseFeature/releases"
            val input = workDataOf(UIUtils.RELEASE_ID to basePath)
            val worker = getFeatureReleaseOneTimeRequestWorker(input, baseFeature)
            enQueueWorker(worker, this)
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(worker.id).observe(this, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            //hideLoading()
                            val apk = UIUtils.appcenterPosExtension(workInfo.tags)
                            val id = workInfo.outputData.getString(UIUtils.RELEASE_OUTPUT)
                            val version = workInfo.outputData.getString(UIUtils.RELEASE_VERSION)
                            getAppcenterReleaseUrls(version.toString(), id.toString(), baseFeature)
                        }
                        WorkInfo.State.FAILED -> {
                            //hideLoading()
                        }
                    }
                }
            })
        }
    }

    private fun getAppcenterReleaseUrls(version: String, id: String, baseFeature: String) {
        //showLoading()
        val basePath =
            if (getEnviroment().equals("uat")) "https://api.appcenter.ms/v0.1/apps/olam-vega/VegaX-Uat/distribution_groups/$baseFeature/releases/$id"
            else "https://api.appcenter.ms/v0.1/apps/olam-vega/vegax/distribution_groups/$baseFeature/releases/$id"
        val input = workDataOf(UIUtils.RELEASE_URL_PATH to basePath)
        val worker = getFeatureReleaseUrlOneTimeRequestWorker(input, baseFeature)
        enQueueWorker(worker, this)
        //showLoading()
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            //hideLoading()
                            val versionCode = workInfo.outputData.getString(UIUtils.RELEASE_VERSION)
                            val releaseNotes = workInfo.outputData.getString(UIUtils.RELEASE_NOTES)
                            if (AppUtils.getVersionCode() < versionCode?.toInt() ?: 0) ShowUpdateDialog(
                                version.toString(),
                                releaseNotes.toString()
                            )
                        }
                        WorkInfo.State.FAILED -> {
                            //hideLoading()
                        }
                    }
                }
            })
    }

    private fun ShowUpdateDialog(version: String, releaseNotes: String) {
        AppUtils.getVersionName()
        MaterialDialog(this).show {
            val msg = "New Version $version available! for ${getEnviroment()} environment"
            title(text = msg)
            message(text = releaseNotes)
            cancelOnTouchOutside(false)
            cancelable(false)
            positiveButton(text = UIUtils.getSpannedText(context.getString(R.string.update), true)) {
                clearData()
            }
            if (!releaseNotes.contains(getCurrentKey())) {
                negativeButton(text = UIUtils.getSpannedText(context.getString(R.string.postpone), true)) {
                    //Distribute.notifyUpdateAction(UpdateAction.POSTPONE)
                    dismiss()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        this.menu = menu
        updateToolbarMenuItem()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            val selectedKey = PreferenceHelper.get(Constants.SELECTED_KEYS, "")
            val selectedKeys = Gson().fromJson<List<String>>(selectedKey)

            when (it.itemId) {
                1 -> {
                    selectedKeys.forEach {
                        if (it.split("_")[0].contains("VEGA")) {
                            when {
                                it.split("_")[2].contains("CASH") && !it.equals(currentKey) -> currentKey =
                                    it
                            }
                        }
                    }
                    updateData()
                    getOfflineData()
                    return true
                }
                2 -> {
                    selectedKeys.forEach {
                        if (it.split("_")[0].contains("VEGA")) {
                            when {
                                it.split("_")[2].contains("COCO") && !it.equals(currentKey) -> currentKey = it
                            }
                        }
                    }
                    updateData()
                    return true
                }
                3 -> {
                    selectedKeys.forEach {
                        if (it.split("_")[0].contains("VEGA")) {
                            when {
                                it.split("_")[2].contains("ARAB") && !it.equals(currentKey) -> currentKey = it
                            }
                        }
                    }
                    updateData()
                    return true
                }
                4 -> {
                    selectedKeys.forEach {
                        if (it.split("_")[0].contains("VEGA")) {
                            when {
                                it.split("_")[2].contains("PEPP") && !it.equals(currentKey) -> currentKey = it
                            }
                        }
                    }
                    updateData()
                    return true
                }
                5 -> {
                    selectedKeys.forEach {
                        if (it.split("_")[0].contains("DO")) {
                            when {
                                it.split("_")[2].contains("CASH") && !it.equals(currentKey) -> currentKey =
                                    it
                            }
                        }
                    }
                    updateData()
                    getOfflineData()
                    return true
                }
                6 -> {
                    selectedKeys.forEach {
                        if (it.split("_")[0].contains("DO")) {
                            when {
                                it.split("_")[2].contains("COCO") && !it.equals(currentKey) -> currentKey = it
                            }
                        }
                    }
                    updateData()
                    return true
                }
                7 -> {
                    selectedKeys.forEach {
                        if (it.split("_")[0].contains("DO")) {
                            when {
                                it.split("_")[2].contains("ARAB") && !it.equals(currentKey) -> currentKey = it
                            }
                        }
                    }
                    updateData()
                    return true
                }
                8 -> {
                    selectedKeys.forEach {
                        if (it.split("_")[0].contains("DO")) {
                            when {
                                it.split("_")[2].contains("PEPP") && !it.equals(currentKey) -> currentKey = it
                            }
                        }
                    }
                    updateData()
                    return true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateData() {
        PreferenceHelper.save(Constants.CURRENT_KEY, currentKey)
        updateProductHeader()
        if (getSelectedKeyCount() > 1) {
            val currentKeyStatus = PreferenceHelper.get(currentKey, true)
            if (currentKeyStatus) setOnline() else setOffline()

        } else {
            setOnline()
        }
        //setOnline()
        //vm.getUserRoles(currentKey)
        roleData = getRoles(currentKey)
        updateUI(roleData)
    }

    private fun updateToolbarMenuItem() {
        RefreshToolbarMenuItems()
        selectedKeys.forEach {
            if (it.split("_")[0].contains("VEGA")) {
                when {
                    it.split("_")[2].contains("CASH") && !it.equals(currentKey) -> menu?.add(
                        0,
                        1,
                        1,
                        getString(R.string.menu_vega_cashew)
                    )
                    it.split("_")[2].contains("COCO") && !it.equals(currentKey) -> menu?.add(
                        0,
                        2,
                        2,
                        getString(R.string.menu_vega_cocoa)
                    )
                    it.split("_")[2].contains("ARAB") && !it.equals(currentKey) -> menu?.add(
                        0,
                        3,
                        3,
                        getString(R.string.menu_vega_coffee)
                    )
                    it.split("_")[2].contains("PEPP") && !it.equals(currentKey) -> menu?.add(
                        0,
                        4,
                        4,
                        getString(R.string.menu_vega_papper)
                    )
                }
            } else if (it.split("_")[0].contains("DO")) {
                when {
                    it.split("_")[2].contains("CASH") && !it.equals(currentKey) -> menu?.add(
                        0,
                        5,
                        5,
                        getString(R.string.menu_od_cashew)
                    )
                    it.split("_")[2].contains("COCO") && !it.equals(currentKey) -> menu?.add(
                        0,
                        6,
                        6,
                        getString(R.string.menu_od_cocoa)
                    )
                    it.split("_")[2].contains("ARAB") && !it.equals(currentKey) -> menu?.add(
                        0,
                        7,
                        7,
                        getString(R.string.menu_od_coffee)
                    )
                    it.split("_")[2].contains("PEPP") && !it.equals(currentKey) -> menu?.add(
                        0,
                        8,
                        8,
                        getString(R.string.menu_od_papper)
                    )
                }
            }
        }
    }

    private fun updateUI(userRoles: List<UserRole>?) {
        tvProcureType?.text = currentKey.split("_")[0]

        val navMenus = navigationView.menu

        var isVirtual = false
        val roleData =
            Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
                .filter { key ->
                    key.roleKey.equals(
                        PreferenceHelper.get(
                            Constants.CURRENT_KEY,
                            ""
                        )
                    )
                }
        roleData.forEach { rol ->
            when (valueOfEnum(rol.roleName.trim())) {
                UserRoles.VIRTUAL_MTNT -> isVirtual = true
            }
        }

//        navMenus.findItem(R.id.navLogout).isVisible = isOnline()
        if ((currentKey.split("_")[0].contains("DO")) /*&& (!currentKey.split("_")[1].contains("ID"))*/) {
            navMenus.findItem(R.id.navSync).isVisible = isOnline()
            navigationView.getHeaderView(0).findViewById<FlipTab>(R.id.fliptab).visible()
        } else if ((currentKey.split("_")[0].contains("VEGA")) && ((currentKey.split("_")[1].contains(
                "EC"
            )) ||
                    ((currentKey.split("_")[2].contains("COCO")) && isVirtual) ||
                    (currentKey.split("_")[1].contains("NI")) ||
                    (currentKey.split("_")[1].contains("GH")) ||
                    (currentKey.split("_")[1].contains("ID")) ||
                    (currentKey.split("_")[2].contains("COTTGINN")) ||
                    (currentKey.split("_")[1].contains("NG")) && (currentKey.split("_")[2].contains(
                "CASH"
            ))
                    || (currentKey.split("_")[2].contains("COTTPORT")))
        ) {
            if (!(currentKey.split("_")[2].contains("COTTGINN") || currentKey.split("_")[2].contains("COTTPORT"))) {
                navMenus.findItem(R.id.navSync).isVisible = isOnline()
            }
            navigationView.getHeaderView(0).findViewById<FlipTab>(R.id.fliptab).visible()
        } else navMenus.findItem(R.id.navSync).isVisible = false
        refreshMenuItems(navMenus)
        updateToolbarMenuItem()
        val list = mutableListOf<String>()
        homeList.clear()
        userRoles?.forEach { rol -> list.add(rol.roleName) }
        list.forEach {
            when (valueOfEnum(it.replace(" ", "_"))) {
                UserRoles.SCAN -> {
                    navMenus.findItem(R.id.navScan).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.tap_to_scan),
                            com.olam.warehouse.presentation.R.drawable.ic_menu_tap_to_scan,
                            0
                        )
                    )
                }
                UserRoles.SUPPLIER -> {
                    when {
                        currentKey.split("_")[0].contains("VEGA") && (currentKey.split("_")[2].contains(
                            "CASH"
                        ) || currentKey.split(
                            "_"
                        )[2].contains("COFF")) -> {
                            navMenus.findItem(R.id.navWeighment).isVisible = true
                            homeList.add(
                                HomeItems(
                                    getString(R.string.weighment),
                                    com.olam.warehouse.presentation.R.drawable.ic_weighment_menu,
                                    1
                                )
                            )
                        }
                        else -> {
                            navMenus.findItem(R.id.navReceving).isVisible = true
                            homeList.add(
                                HomeItems(
                                    getString(R.string.receiving),
                                    com.olam.warehouse.presentation.R.drawable.ic_receiving_menu,
                                    1
                                )
                            )
                        }
                    }
                }
                UserRoles.OFFLOADING -> {

                    when {
                        currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains(
                            "COFF"
                        ) -> {
                            navMenus.findItem(R.id.navMTnr).isVisible = true
                            homeList.add(
                                HomeItems(
                                    getString(R.string.mtnr),
                                    com.olam.warehouse.presentation.R.drawable.ic_offloading_menu,
                                    2
                                )
                            )
                        }
                        else -> {
                            navMenus.findItem(R.id.navOffloading).isVisible = true
                            homeList.add(
                                HomeItems(
                                    getString(R.string.offloading),
                                    com.olam.warehouse.presentation.R.drawable.ic_offloading_menu,
                                    2
                                )
                            )
                        }
                    }
                }
                UserRoles.QUALITY -> {
                    when {
                        currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains(
                            "COFF"
                        ) -> navMenus.findItem(R.id.navQuality).isVisible = false
                        else -> {
                            navMenus.findItem(R.id.navQuality).isVisible = true
                            homeList.add(
                                HomeItems(
                                    getString(R.string.quality),
                                    com.olam.warehouse.presentation.R.drawable.ic_quality_menu,
                                    3
                                )
                            )
                        }
                    }
                }
                UserRoles.APPROVE -> {
                    navMenus.findItem(R.id.navApproval).isVisible = true
                    when {
                        currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains(
                            "COFF"
                        ) -> {
                            homeList.add(
                                HomeItems(
                                    getString(R.string.approve),
                                    com.olam.warehouse.presentation.R.drawable.ic_quality_approve,
                                    4
                                )
                            )
                        }
                        else -> {
                            homeList.add(
                                HomeItems(
                                    getString(R.string.approve),
                                    com.olam.warehouse.presentation.R.drawable.ic_approve_menu,
                                    4
                                )
                            )
                        }
                    }
                }
                UserRoles.MTNT -> {
                    navMenus.findItem(R.id.navMtnt).isVisible = true
                    when {
                        currentKey.split("_")[0].contains("VEGA") && (currentKey.split("_")[1].contains(
                            "CM"
                        )) && (currentKey.split("_")[2].contains("COCO")) -> {
                            homeList.add(
                                HomeItems(
                                    getString(R.string.dispatch),
                                    com.olam.warehouse.presentation.R.drawable.ic_dispatch_menu,
                                    5
                                )
                            )
                            /*homeList.add(
                                            HomeItems(
                                                getString(R.string.container_management),
                                                com.olam.warehouse.presentation.R.drawable.ic_container_management,
                                                5
                                            )
                                        )*/
                        }
                        currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains(
                            "COFF"
                        ) -> {
                            homeList.add(
                                HomeItems(
                                    getString(R.string.dispatch),
                                    com.olam.warehouse.presentation.R.drawable.ic_dispatch_menu,
                                    5
                                )
                            )
                        }
                        currentKey.split("_")[1].contains("NG") && currentKey.split("_")[2].contains(
                            "COCO"
                        ) -> {
                            homeList.add(
                                HomeItems(
                                    getString(R.string.dispatch),
                                    com.olam.warehouse.presentation.R.drawable.ic_dispatch_menu,
                                    5
                                )
                            )
                        }
                        else -> {
                            homeList.add(
                                HomeItems(
                                    getString(R.string.dispatch),
                                    com.olam.warehouse.presentation.R.drawable.ic_dispatch_menu,
                                    5
                                )
                            )
                        }
                    }
                }

                UserRoles.PROCESSING -> {
                    navMenus.findItem(R.id.navProcessing).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.processing),
                            com.olam.warehouse.presentation.R.drawable.ic_menu_processing,
                            6
                        )
                    )
                }
                UserRoles.GATEENTRY -> {
                    navMenus.findItem(R.id.navGateEntry).isVisible = true
                    when {
                        currentKey.split("_")[1].contains("CM") || (currentKey.split("_")[1].contains(
                            "IV"
                        )
                                && currentKey.split("_")[2].contains("CASH")) || (currentKey.split("_")[1].contains(
                            "IV"
                        )
                                && currentKey.split("_")[2].contains("COFF")) || (currentKey.split("_")[1].contains(
                            "GH"
                        )
                                && currentKey.split("_")[2].contains("COCO")) || (currentKey.split("_")[1].contains(
                            "NG"
                        )
                                && currentKey.split("_")[2].contains("COCO")) -> /*if (currentKey.split("_")[0].contains("VEGA") && (currentKey.split("_")[1].contains("CM"))
                                        && (currentKey.split("_")[2].contains("COCO"))
                                    ) {
                                        navMenus.findItem(R.id.navGateEntry).title = getString(R.string.secret_id)
                                        homeList.add(
                                                HomeItems(
                                                        getString(R.string.secret_id),
                                                        com.olam.warehouse.presentation.R.drawable.ic_waiting_truck_menu,
                                                        7
                                                )
                                        )
                                    } else*/ homeList.add(
                            HomeItems(
                                getString(R.string.gate_entry),
                                com.olam.warehouse.presentation.R.drawable.ic_waiting_truck_menu,
                                7
                            )
                        )
                    }
                }
                UserRoles.GATEENTRYAPPROVAL -> {
                    navMenus.findItem(R.id.navGateEntryApproval).isVisible = true
                    if (currentKey.split("_")[1].contains("NG"))
                        homeList.add(
                            HomeItems(
                                getString(R.string.gate_entry_approval),
                                com.olam.warehouse.presentation.R.drawable.ic_approve_menu,
                                7
                            )
                        )
                }
                UserRoles.INVENTORY -> {
                    navMenus.findItem(R.id.navInventory).isVisible = true
                    if (!((currentKey.split("_")[2].contains("COTTGINN")) || (currentKey.split("_")[2].contains(
                            "COTTPORT"
                        )))
                    ) {

                        navMenus.findItem(R.id.navSyncInventory).isVisible = true
                    }
                    homeList.add(
                        HomeItems(
                            getString(R.string.inventroy),
                            com.olam.warehouse.presentation.R.drawable.ic_inventory,
                            8
                        )
                    )
                }
                UserRoles.SWEEPINGS -> {
                    navMenus.findItem(R.id.navSweepings).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.sweepings),
                            com.olam.warehouse.presentation.R.drawable.ic_menu_sweepings,
                            9
                        )
                    )
                }
                UserRoles.GRN -> {
                    navMenus.findItem(R.id.navGrn).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.grn1),
                            com.olam.warehouse.presentation.R.drawable.ic_grn_menu,
                            10
                        )
                    )
                }
                UserRoles.PPQ -> {
                    when {
                        (currentKey.split("_")[1].contains("NG"))
                                && (currentKey.split("_")[2].contains("COCO")) -> {

                            navMenus.findItem(R.id.navLotQuality).isVisible = true
                            homeList.add(
                                HomeItems(
                                    getString(R.string.lot_quality),
                                    com.olam.warehouse.presentation.R.drawable.ic_ppq_menu_ofi,
                                    11
                                )
                            )
                        }
                        else -> {
                            navMenus.findItem(R.id.navPpq).isVisible = true

                            homeList.add(
                                HomeItems(
                                    getString(R.string.ppq),
                                    com.olam.warehouse.presentation.R.drawable.ic_ppq_menu_ofi,
                                    11
                                )
                            )
                        }
                    }
                }
                UserRoles.CONTAINER -> {
                    if ((currentKey.split("_")[1].contains("NG"))
                        && (currentKey.split("_")[2].contains("COCO"))
                    ) {
                        navMenus.findItem(R.id.navcontainer).isVisible = true
                        homeList.add(
                            HomeItems(
                                getString(R.string.container_management),
                                com.olam.warehouse.presentation.R.drawable.ic_container_management,
                                5
                            )
                        )
                    } else if ((currentKey.split("_")[1].contains("NI"))
                        && (currentKey.split("_")[2].contains("COFF"))
                    ) {
                        navMenus.findItem(R.id.navcontainer).isVisible = true
                        homeList.add(
                            HomeItems(
                                getString(R.string.container_management),
                                com.olam.warehouse.presentation.R.drawable.ic_container_management,
                                5
                            )
                        )
                    }
                }
                UserRoles.PILE -> {
                    navMenus.findItem(R.id.navPile).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.pile),
                            com.olam.warehouse.presentation.R.drawable.ic_pile_menu_ofi,
                            11
                        )
                    )
                }
                UserRoles.INVOICE -> {
                    navMenus.findItem(R.id.navInvoice).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.invoice),
                            com.olam.warehouse.presentation.R.drawable.ic_invoice_menu,
                            12
                        )
                    )
                    navMenus.findItem(R.id.navReconcillation).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.reconcilreport),
                            com.olam.warehouse.presentation.R.drawable.ic_reconcil_menu,
                            14
                        )
                    )
                }
                UserRoles.TRANSACTIONDETAILS -> {
                    navMenus.findItem(R.id.navTransactionDetails).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.transaction_details),
                            com.olam.warehouse.presentation.R.drawable.ic_trans_details_menu,
                            13
                        )
                    )
                }
                UserRoles.ADVANCING -> {
                    navMenus.findItem(R.id.navAdvance).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.advancing),
                            com.olam.warehouse.presentation.R.drawable.ic_advance_menu,
                            17
                        )
                    )
                    //different role needed to be added for forward po
                    navMenus.findItem(R.id.navForward).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.trans_forward_po),
                            com.olam.warehouse.presentation.R.drawable.ic_forward_menu,
                            16
                        )
                    )
                }

                UserRoles.REPRINT -> {
                    navMenus.findItem(R.id.navReprint).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.reprint),
                            com.olam.warehouse.presentation.R.drawable.ic_reprint_menu,
                            14
                        )
                    )
                }
                UserRoles.PCH -> {
                    navMenus.findItem(R.id.navApproval).isVisible = true
                    when {
                        currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains(
                            "COFF"
                        ) -> {
                            homeList.add(
                                HomeItems(
                                    getString(R.string.approve),
                                    com.olam.warehouse.presentation.R.drawable.ic_quality_approve,
                                    4
                                )
                            )
                        }
                        currentKey.split("_")[0].contains("VEGA") && (currentKey.split("_")[1].contains(
                            "CM"
                        )) && (currentKey.split(
                            "_"
                        )[2].contains("COCO")) -> {
                            homeList.add(
                                HomeItems(
                                    getString(R.string.approve),
                                    com.olam.warehouse.presentation.R.drawable.ic_approve_menu,
                                    4
                                )
                            )
                        }
                    }
                }
                UserRoles.INCOMING_LOTS -> {
                    navMenus.findItem(R.id.navIncomingLots).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.incoming_lots),
                            com.olam.warehouse.presentation.R.drawable.ic_inventory,
                            1
                        )
                    )
                }

                UserRoles.GINNING_INPROGRESS -> {
                    navMenus.findItem(R.id.navGinningInProgress).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.ginning_in_progress),
                            com.olam.warehouse.presentation.R.drawable.ic_inventory,
                            2
                        )
                    )
                }

                UserRoles.DRYING_INPROGRESS -> {
                    navMenus.findItem(R.id.navDryingInProgress).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.drying_in_progress),
                            com.olam.warehouse.presentation.R.drawable.ic_inventory,
                            3
                        )
                    )
                }
                UserRoles.DISPATCH -> {
                    navMenus.findItem(R.id.navMtnt).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.dispatch),
                            com.olam.warehouse.presentation.R.drawable.ic_inventory,
                            4
                        )
                    )
                }
                UserRoles.PILEMANAGEMENT -> {
                    navMenus.findItem(R.id.navPileManagement).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.pile_managemnet),
                            com.olam.warehouse.presentation.R.drawable.ic_inventory,
                            5
                        )
                    )
                }
                UserRoles.SAMPLEID -> {
                    navMenus.findItem(R.id.navSecretId).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.secret_id),
                            com.olam.warehouse.presentation.R.drawable.ic_waiting_truck_menu,
                            6
                        )
                    )
                }
                UserRoles.SUPPLIER_BAG_MGMT -> {
                    navMenus.findItem(R.id.navBagIssue).isVisible = true
                    homeList.add(
                        HomeItems(
                            getString(R.string.bag_issue),
                            com.olam.warehouse.presentation.R.drawable.ic_bag_mgmt,
                            10
                        )
                    )
                }
            }
        }
        if (homeList.size > 0) setUpAdapter(homeList.distinctBy { it.itemName }.sortedBy { it.orderId })
        else setUpAdapter(homeList)
    }


    private fun setUpAdapter(data: List<HomeItems>) {
        bindingActivity.rvHome.setUp(
            data as MutableList<HomeItems>,
            R.layout.item_home,
            { it, pos ->
                tvHomeMenuItem.text = it.itemName
                if (it.offlineCount?.isNotEmpty()!!) rlOfflineCount.visible() else rlOfflineCount.gone()
                tvOfflineCount.text = it.offlineCount
                ivHomeMenuItem.setImageResource(it.itemImage!!)
                if (getCurrentOriginEntity().contains("OFI"))
                    ivHomeMenuItem.setImageResource(
                        setEntityLevelVarientOnMenuIcons(
                            it,
                            ivHomeMenuItem.context
                        )
                    )
                else
                    ivHomeMenuItem.setImageResource(it.itemImage!!)
                if (currentKey.split("_")[1].contains("GH")) {
                    if (currentKey.split("_")[2].contains("CASH")) {
                        if (it.itemName.equals(getString(R.string.quality))) {
                            rlOfflineCount.visible()
                            tvOfflineCount.text = it.offlineCount
                        }
                    }
                }
                /*  if (currentKey.split("_")[1].contains("GH")) {
                if (currentKey.split("_")[2].contains("CASH")) {
                    if (it.itemName.equals(getString(R.string.quality))) {
                        rlOfflineCount.visible()
                        if (!isOnline()) {
                        vm.weighBridge.observe(this@HomeActivity, Observer { response ->
                            response.let { qualityDetail ->
                                var weighTypedata: List<VegaQualityWBDetails>? = null
                                var weighTypedata1: List<VegaQualityWBDetails>? = null
                                val data =
                                    qualityDetail.filter { value -> !value.qcStatus!!.contains("X") }
                                weighTypedata = data.filter { wb -> wb.direction == UIUtils.DIRECTIONIN }
                                    .filter { value -> value.weighBridgeType == "PROCURE" }
                                var i: Int = weighTypedata.size
                                val data1 = response*//*?.filter { value -> value.qcStatus.toString() == "R" }*//*
                                weighTypedata1 = data1.filter { wb -> wb.direction == UIUtils.DIRECTIONIN }
                                    .filter { value -> value.weighBridgeType == "STO" }
                                    .filter { value -> !value.netWeight.equals("0.000") }
                                var i1: Int = weighTypedata1.size
                                tvOfflineCount.text = i.plus(i1).toString()

                                *//*  val data = response.data?.data
                                      ?.filter { value -> !value.qcStatus!!.contains("X") }*//*


                            }
                            *//*homeList.forEach { item ->
                                if (item.itemName.equals(getString(R.string.quality)))
                                 tvOfflineCount.text =it.size.toString()

                            }*//*
                        })
                        vm.getWeighBridgeDetailqualitycount()
                    }
                        else
                        {
                            var weighTypedata: List<VegaQualityWBDetails>? = null
                            vm.weighBridgeOnline.observe(this@HomeActivity, Observer {response->

                                val data = response.data?.data
                                    ?.filter { value -> !value.qcStatus!!.contains("X") }
//                            ?.filter { it.grnNumber.isNotEmpty() }
                                weighTypedata = data?.filter { wb -> wb.direction == UIUtils.DIRECTIONIN }
                                    ?.filter { value -> value.weighBridgeType == "PROCURE" }
                                    ?.filter { value -> !value.netWeight.equals("0.0") }
                                tvOfflineCount.text=weighTypedata?.size.toString()
                            })
                            vm.getWeighBridgeDataOnline()
                        }
                    }
                }
            }*/
            },
            {
                val homeItem = this
                when (homeItem.itemName) {
                    getString(R.string.tap_to_scan) -> moveToScannerActivity()
                    getString(R.string.receiving) -> moveToReceiving()
                    getString(R.string.weighment) -> moveToReceiving()
                    getString(R.string.offloading) -> moveToOffloading()
                    getString(R.string.mtnr) -> moveToOffloading()
                    getString(R.string.quality) -> moveToQuality()
                    getString(R.string.ppq) -> moveToPpq()
                    getString(R.string.lot_quality) -> moveToPpq()
                    getString(R.string.pile) -> moveToPile()
                    getString(R.string.approve) -> moveToApprove()
                    getString(R.string.dispatch) -> moveToDispatch()
                    getString(R.string.processing) -> moveToProcessing()
                    getString(R.string.gate_entry) -> moveToGateEntry()
                    getString(R.string.gate_entry_approval) -> moveToGateEntryApproval()
                    getString(R.string.secret_id) -> moveToSecretID()
                    getString(R.string.inventroy) -> moveInventory()
                    getString(R.string.sweepings) -> moveSweepings()
                    getString(R.string.grn1) -> moveToGrn()
                    getString(R.string.reconcilreport) -> moveToReconcilReport()
                    getString(R.string.invoice) -> moveToInvoice()
                    getString(R.string.transaction_details) -> {
                        var isVirtual = false
                        val roleData = Gson().fromJson<List<UserRole>>(
                            PreferenceHelper.get(
                                Constants.USER_ROLES,
                                ""
                            )
                        )
                            .filter { key ->
                                key.roleKey.equals(
                                    PreferenceHelper.get(
                                        Constants.CURRENT_KEY,
                                        ""
                                    )
                                )
                            }
                        roleData.forEach { rol ->
                            when (valueOfEnum(rol.roleName.trim())) {
                                UserRoles.VIRTUAL_MTNT -> isVirtual = true
                            }
                        }
                        if (isVirtual) moveToCocoaMtnRTransactionDetails()
                        else moveToTransactionDetails()
                    }

                    getString(R.string.reprint) -> moveToReprint()
                    getString(R.string.container_management) -> moveToContainerManagement()
                    getString(R.string.trans_forward_po) -> moveToForwardPo()
                    getString(R.string.advancing) -> moveToAdvancing()
                    getString(R.string.incoming_lots) -> moveToIncomingLots()
                    getString(R.string.drying_in_progress) -> moveToDryingLots()
                    getString(R.string.pile_managemnet) -> moveToPileManagement()
                    getString(R.string.ginning_in_progress) -> moveToGinningInprogress()
                    getString(R.string.bag_issue) -> moveToBagIssue()
                }

            },
            GridLayoutManager(this, 3)
        )
    }

    private fun setEntityLevelVarientOnMenuIcons(homeItems: HomeItems, context: Context): Int {
        return when (homeItems.itemName) {
            getString(R.string.tap_to_scan) -> com.olam.warehouse.presentation.R.drawable.ic_menu_tap_to_scan_ofi
            getString(R.string.receiving) -> com.olam.warehouse.presentation.R.drawable.ic_receiving_menu_ofi
            getString(R.string.weighment) -> com.olam.warehouse.presentation.R.drawable.ic_weighment_menu_ofi
            getString(R.string.offloading) -> com.olam.warehouse.presentation.R.drawable.ic_offloading_menu_ofi
            getString(R.string.quality) -> com.olam.warehouse.presentation.R.drawable.ic_quality_menu_ofi
            getString(R.string.ppq) -> com.olam.warehouse.presentation.R.drawable.ic_ppq_menu_ofi
            getString(R.string.pile) -> com.olam.warehouse.presentation.R.drawable.ic_pile_menu_ofi
            getString(R.string.approve) -> com.olam.warehouse.presentation.R.drawable.ic_approve_menu_ofi
            getString(R.string.dispatch) -> com.olam.warehouse.presentation.R.drawable.ic_dispatch_menu_ofi
            getString(R.string.processing) -> com.olam.warehouse.presentation.R.drawable.ic_menu_processing_ofi
            getString(R.string.gate_entry) -> com.olam.warehouse.presentation.R.drawable.ic_waiting_truck_menu_ofi
            getString(R.string.secret_id) -> com.olam.warehouse.presentation.R.drawable.ic_waiting_truck_menu_ofi
            getString(R.string.inventroy) -> com.olam.warehouse.presentation.R.drawable.ic_inventory_menu_ofi
            getString(R.string.sweepings) -> com.olam.warehouse.presentation.R.drawable.ic_menu_sweepings_ofi
            getString(R.string.grn1) -> com.olam.warehouse.presentation.R.drawable.ic_grn_menu_ofi
            getString(R.string.reconcilreport) -> com.olam.warehouse.presentation.R.drawable.ic_reconcil_menu_ofi
            getString(R.string.invoice) -> com.olam.warehouse.presentation.R.drawable.ic_invoice_menu_ofi
            getString(R.string.transaction_details) -> com.olam.warehouse.presentation.R.drawable.ic_trans_details_menu_ofi
            getString(R.string.reprint) -> com.olam.warehouse.presentation.R.drawable.ic_reprint_menu_ofi
            getString(R.string.container_management) -> com.olam.warehouse.presentation.R.drawable.ic_container_management
            getString(R.string.trans_forward_po) -> com.olam.warehouse.presentation.R.drawable.ic_forward_menu_ofi
            getString(R.string.advancing) -> com.olam.warehouse.presentation.R.drawable.ic_advance_menu_ofi
            getString(R.string.incoming_lots) -> com.olam.warehouse.presentation.R.drawable.ic_inventory
            getString(R.string.drying_in_progress) -> com.olam.warehouse.presentation.R.drawable.ic_inventory
            getString(R.string.pile_managemnet) -> com.olam.warehouse.presentation.R.drawable.ic_inventory
            getString(R.string.ginning_in_progress) -> com.olam.warehouse.presentation.R.drawable.ic_inventory
            getString(R.string.bag_issue) -> com.olam.warehouse.presentation.R.drawable.ic_bag_mgmt_ofi
            else -> com.olam.warehouse.presentation.R.drawable.ic_weighment_menu_ofi
        }
    }

    private fun moveToContainerManagement() {
        when {
            currentKey.split("_")[0].contains("VEGA") -> {
                when {
                    currentKey.split("_")[1].contains("CM") && currentKey.split("_")[2].contains("COCO") -> {
                        VegaCameroonContainerManagementNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
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
        }
    }

    private fun moveToBagIssue() {
        when {
            currentKey.split("_")[0].contains("VEGA") -> {
                if (currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains("COFF")) {
                    VegaIndiaCoffeeBagIssueNavigation.dynamicStart?.let {
                        startActivity(it)
                    }
                }
            }
        }
    }

    private fun moveToReceiving() {
        when {
            currentKey.split("_")[0].contains("DO") -> {
                DOReceivingFeatureNavigation.dynamicStart?.let {
                    startActivity(it)
                }
            }
            currentKey.split("_")[0].contains("VEGA") -> {
                /*if (!currentKey.split("_")[1].contains("EC")) {
                    VegaReceivingFeatureNavigation.dynamicStart?.let {
                        startActivity(it)
                    }
                }*/
                when {
                    currentKey.split("_")[1].contains("GH") -> VegaGhanaReceivingNavigation.dynamicStart?.let {
                        startActivity(it)
                    }
                    (currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains(
                        "COFF"
                    )) -> VegaIndiaCoffeeWeighmentFeatureNavigation.dynamicStart?.let {
                        startActivity(it)
                    }
                    currentKey.split("_")[2].contains("COFF") -> VegaCoffeeWeighmentNavigation.dynamicStart?.let {
                        startActivity(it)
                    }
                    currentKey.split("_")[2].contains("CASH") -> VegaReceivingFeatureNavigation.dynamicStart?.let {
                        startActivity(it)
                    }
                    (currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains(
                        "COFF"
                    )) -> VegaIndiaCoffeeWeighmentFeatureNavigation.dynamicStart?.let {
                        startActivity(it)
                    }
                }
            }
        }
    }

    private fun moveToQuality() {
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
                        } else if (currentKey.split("_")[2].contains("SESA") || currentKey.split("_")[2].contains(
                                "CASH"
                            )
                        ) {
                            VegaNigeriaSesameQualityNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }

                    }
                    currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains("COFF") -> {
                        VegaIndiaCoffeeQualityFeatureNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[1].contains("GH") -> {
                        VegaGhanaQualityNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[1].contains("NI") -> {

                    }
                    currentKey.split("_")[1].contains("ID") && currentKey.split("_")[2].contains("ARAB") -> {
                        VegaIndoCoffeeQualityNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[2].contains("COFF") -> {
                        VegaCoffeeQualityNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                        /*VegaQualityFeatureNavigation.dynamicStart?.let {
                                    it.putExtra(MTNR, true)
                                    startActivity(it)
                                }*/
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

    private fun moveToPpq() {

        when {
            currentKey.split("_")[0].contains("VEGA") -> {
                // Lot Quality Nigeria Cocoa - Muskan Jain
                when {
                    (currentKey.split("_")[1].contains("NG"))
                            && (currentKey.split("_")[2].contains("COCO")) -> {
                        VegaNigeriaCocoaLotQualityNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains("COFF") -> {
                        VegaIndiaCoffeePpqFeatureNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }

                    // Edited by Muskan Jain - 01-09-2021
                    (currentKey.split("_")[1].contains("NI"))
                            && (currentKey.split("_")[2].contains("COFF")) -> {
                        VegaSesamePpqNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[2].contains("COFF") -> {
                        VegaCoffeePpqNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[2].contains("SESA") -> {
                        VegaSesamePpqNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    (currentKey.split("_")[1].contains("NG")) && (currentKey.split("_")[2].contains(
                        "CASH"
                    )) -> {
                        VegaSesamePpqNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[2].contains("COCO") -> {
                        VegaCameroonPpqNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[2].contains("ARAB") -> {
                        VegaIndoCoffeePpqNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                }
            }
        }
    }

    private fun moveToPileManagement() {
        when {
            currentKey.split("_")[0].contains("VEGA") -> {
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
                }
            }
        }

    }

    private fun moveToGinningInprogress() {
        when {
            currentKey.split("_")[0].contains("VEGA") -> {
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

    }

    private fun moveToPile() {
        if (currentKey.split("_")[0].contains("VEGA")) {
            if (currentKey.split("_")[2].contains("COFF")) {
                if (currentKey.split("_")[1].contains("IN")) {
                    VegaIndiaCoffeePileNavigation.dynamicStart?.let {
                        startActivity(it)
                    }
                } else if (currentKey.split("_")[1].contains("NI")) {
                    VegaSesamePileNavigation.dynamicStart?.let {
                        startActivity(it)
                    }
                } else {
                    VegaCoffeePileNavigation.dynamicStart?.let {
                        startActivity(it)
                    }
                }
            } else if (currentKey.split("_")[2].contains("SESA")) {
                VegaSesamePileNavigation.dynamicStart?.let {
                    startActivity(it)
                }
            } else if (currentKey.split("_")[1].contains("NG") && currentKey.split("_")[2].contains(
                    "CASH"
                )
            ) {
                VegaSesamePileNavigation.dynamicStart?.let {
                    startActivity(it)
                }
            } else if (currentKey.split("_")[1].contains("NG") && currentKey.split("_")[2].contains(
                    "COCO"
                )
            ) {
                VegaNigeriaPileNavigation.dynamicStart?.let {
                    startActivity(it)
                }
            }

        }
    }

    private fun moveToApprove() {
        when {
            currentKey.split("_")[0].contains("VEGA") -> {
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
                    currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains("COFF") -> {
                        VegaIndiaCoffeeQualityApprovalFeatureNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[1].contains("NG") && currentKey.split("_")[2].contains("COCO") -> {
                        VegaNigeriaQualityApprovalFeatureNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    else -> {
                        VegaApproveNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                }
            }
        }
    }

    private fun moveToOffloading() {
        when {
            currentKey.split("_")[0].contains("VEGA") -> {
                when {
                    currentKey.split("_")[1].contains("EC") -> {
                        VegaEcuadorOffloadingNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains("COFF") -> {
                        VegaIndiaCoffeeOffloadingFeatureNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains("COFF") -> {
                        VegaNicaraguaOffloadingNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[1].contains("CM") -> {
                        if (currentKey.split("_")[2].contains("COCO")) {
                            VegaCameroonCocoaOffloadingNavigation.dynamicStart?.let {
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
                        if (currentKey.split("_")[2].contains("COCO")) {
                            VegaNigeriaOffloadingNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        } else if (currentKey.split("_")[2].contains("SESA") || currentKey.split("_")[2].contains(
                                "CASH"
                            )
                        ) {
                            VegaNigeriaSesameOffloadingNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }

                    }
                    currentKey.split("_")[1].contains("ID") -> {
                        if (currentKey.split("_")[2].contains("ARAB")) {
                            VegaIndoCoffeeOffloadingNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                    }
                    else -> {
                        when {
                            currentKey.split("_")[2].contains("COFF") -> VegaCoffeeOffloadingNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                            currentKey.split("_")[2].contains("CASH") -> VegaOffloadingNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                            currentKey.split("_")[2].contains("COCO") -> VegaCoCoaOffloadingNavigation.dynamicStart?.let {
                                var isVirtual = false
                                val roleData = Gson().fromJson<List<UserRole>>(
                                    PreferenceHelper.get(
                                        Constants.USER_ROLES,
                                        ""
                                    )
                                )
                                    .filter { key ->
                                        key.roleKey.equals(
                                            PreferenceHelper.get(
                                                Constants.CURRENT_KEY,
                                                ""
                                            )
                                        )
                                    }
                                roleData.forEach { rol ->
                                    when (valueOfEnum(rol.roleName.trim())) {
                                        UserRoles.VIRTUAL_MTNR -> isVirtual = true
                                    }
                                }
                                if (isVirtual) startActivity(it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun moveToDispatch() {
        when {
            currentKey.split("_")[0].contains("VEGA") -> {
                when {
                    currentKey.split("_")[1].contains("EC") -> {
                        VegaEcuadorDispatchNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[1].contains("NG") && currentKey.split("_")[2].contains(
                        "COCO"
                    ) -> {
                        startActivity(Intent(this, VegaCocoaDispatchTypeActivity::class.java))
                    }
                    currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains(
                        "COFF"
                    ) -> {
                        startActivity(Intent(this, VegaCocoaDispatchTypeActivity::class.java))
                    }
                    currentKey.split("_")[1].contains("GH") && currentKey.split("_")[2].contains(
                        "COCO"
                    ) -> {
                        startActivity(Intent(this, VegaCocoaDispatchTypeActivity::class.java))
                    }
                    currentKey.split("_")[1].contains("GH") -> {
                        startActivity(Intent(this, VegaCocoaDispatchTypeActivity::class.java))
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
                            currentKey.split("_")[2].contains("COFF") -> startActivity(
                                Intent(
                                    this,
                                    VegaCoffeeDispatchTypeActivity::class.java
                                )
                            )
                            currentKey.split("_")[2].contains("COCO") -> {
                                startActivity(
                                    Intent(
                                        this,
                                        VegaCocoaDispatchTypeActivity::class.java
                                    )
                                )
                            }
                            currentKey.split("_")[2].contains("SESA") -> {
                                startActivity(
                                    Intent(
                                        this,
                                        VegaCocoaDispatchTypeActivity::class.java
                                    )
                                )
                            }
                            currentKey.split("_")[2].contains("ARAB") -> {
                                startActivity(
                                    Intent(
                                        this,
                                        VegaCocoaDispatchTypeActivity::class.java
                                    )
                                )
                            }
                            currentKey.split("_")[2].contains("COTTGINN") -> {
                                moveToGinningDispatch()
                            }
                            currentKey.split("_")[2].contains("COTTPORT") -> {
                                moveToPortDispatch()
                            }
                        }
                    }
                }

            }
        }
    }

    private fun moveToProcessing() {
        when {
            currentKey.split("_")[0].contains("VEGA") -> {
                when {
                    !currentKey.split("_")[1].contains("EC") -> {
                        when {
                            currentKey.split("_")[2].contains("CASH") -> when {
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
                            currentKey.split("_")[2].contains("COCO") -> if (currentKey.split("_")[1].contains(
                                    "NG"
                                )
                            ) {
                                VegaProcessingNigeriaNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            } else if (currentKey.split("_")[1].contains("CM")) {
                                VegaCameroonProcessingCocoaNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            } else {
                                VegaProcessingCocoaNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            }
                            (currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains(
                                "COFF"
                            )) -> VegaIndiaCoffeeProcessingFeatureNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                            (currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains(
                                "COFF"
                            )) -> VegaNicaraguaCoffeeProcessingFeatureNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                            currentKey.split("_")[2].contains("COFF") -> VegaProcessingCoffeeNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                            currentKey.split("_")[2].contains("ARAB") -> VegaProcessingIndoNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                            currentKey.split("_")[2].contains("SESA") -> VegaProcessingSesameNavigation.dynamicStart?.let {
                                startActivity(it)
                            }

                        }
                    }
                }
            }
        }
    }

    private fun moveToSecretID() {
        when {
            currentKey.split("_")[0].contains("VEGA") -> {
                when {
                    currentKey.split("_")[1].contains("CM") -> {
                        VegaSecretIdCameroonNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[1].contains("NG") -> {
                        if (currentKey.split("_")[2].contains("COCO")) {
                            VegaSecretIdNigeriaNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun moveToGateEntryApproval() {
        if (currentKey.split("_")[0].contains("VEGA")) {
            if (!currentKey.split("_")[1].contains("EC")) {
                if (currentKey.split("_")[1].contains("NG")) {
                    if (currentKey.split("_")[2].contains("COCO")) {
                        VegaGateEntryApprovalNigeriaNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                }
            }
        }
    }

    private fun moveToGateEntry() {
        when {
            currentKey.split("_")[0].contains("VEGA") -> {
                when {
                    !currentKey.split("_")[1].contains("EC") -> {
                        when {
                            currentKey.split("_")[1].contains("NG") -> {
                                if (currentKey.split("_")[2].contains("COCO")) {
                                    VegaGateEntryNigeriaNavigation.dynamicStart?.let {
                                        //VegaGateEntryApprovalNigeriaNavigation.dynamicStart?.let {
                                        startActivity(it)
                                    }
                                }
                            }
                            currentKey.split("_")[1].contains("CM") -> {
                                VegaGateEntryCameroonNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            }
                            currentKey.split("_")[1].contains("GH00") && currentKey.split("_")[2].contains(
                                "COCO"
                            ) -> {
                                VegaGateEntryGhanaCocoaNavigation.dynamicStart?.let {
                                    startActivity(it)
                                }
                            }
                            currentKey.split("_")[1].contains("GH") -> {
                                if (currentKey.split("_")[2].contains("COCO")) {
                                    VegaGateEntryGhanaCocoaNavigation.dynamicStart?.let {
                                        startActivity(it)
                                    }
                                } else
                                    VegaGateEntryGhanaNavigation.dynamicStart?.let {
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
            }
        }
    }

    private fun moveInventory() {
        when {
            currentKey.split("_")[0].contains("VEGA") -> {
                when {
                    currentKey.split("_")[1].contains("EC") -> VegaEcuadorInventoryNavigation.dynamicStart?.let {
                        if (AppUtils.isOnline()) startActivity(it)
                    }
                    currentKey.split("_")[1].contains("NI") -> VegaNicaraguaInventoryNavigation.dynamicStart?.let {
                        startActivity(it)
                    }
                    currentKey.split("_")[1].contains("GH") ->
                        if (currentKey.split("_")[2].contains("COCO")) {
                            VegaInventoryGhanaCocoaNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        } else {
                            VegaInventoryGhanaNavigation.dynamicStart?.let { startActivity(it) }
                        }
                    currentKey.split("_")[1].contains("CM") -> if (currentKey.split("_")[2].contains(
                            "COCO"
                        )
                    ) {
                        VegaCameroonInventoryCocoNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[1].contains("NG") -> if (currentKey.split("_")[2].contains(
                            "COCO"
                        )
                    ) {
                        VegaNigeriaInventoryCocoNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    } else if (currentKey.split("_")[2].contains("SESA") || currentKey.split("_")[2].contains(
                            "CASH"
                        )
                    ) {
                        VegaInventorySesameNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }

                    else -> {
                        when {
                            currentKey.split("_")[2].contains("COTTGINN") -> VegaCottonGinningInventoryNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                            currentKey.split("_")[2].contains("COTTPORT") -> VegaCottonPortInventoryNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                            currentKey.split("_")[2].contains("CASH") -> VegaInventoryNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                            currentKey.split("_")[2].contains("COCO") -> VegaInventoryCocoNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                            currentKey.split("_")[2].contains("ARAB") -> VegaInventoryIndoCoffeeNavigation.dynamicStart?.let {
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

                        }
                    }
                }
            }
        }
    }

    private fun moveSweepings() {
        if (currentKey.split("_")[0].contains("VEGA")) {
            when {
                currentKey.split("_")[2].contains("COCO") -> VegaSweepingsCocoNavigation.dynamicStart?.let {
                    startActivity(it)
                }
            }
        }
    }

    private fun moveToGrn() {
        when {
            currentKey.split("_")[0].contains("VEGA") -> {
                when {
                    currentKey.split("_")[1].contains("EC") -> VegaEcuadorGrnNavigation.dynamicStart?.let {
                        startActivity(it)
                    }
                    (currentKey.split("_")[1].contains("IN") && currentKey.split("_")[2].contains(
                        "COFF"
                    )) -> VegaIndiaCoffeeGRNFeatureNavigation.dynamicStart?.let {
                        startActivity(it)
                    }
                    currentKey.split("_")[1].contains("NI") -> VegaNicaraguaGrnNavigation.dynamicStart?.let {
                        startActivity(it)
                    }
                    currentKey.split("_")[1].contains("ID") -> {
                        if (currentKey.split("_")[2].contains("ARAB")) {
                            VegaIndoCoffeeGrnNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        }
                    }
                    (currentKey.split("_")[1].contains("GH") && currentKey.split("_")[2].contains("COCO")) ->
                        VegaGhanaCocoOffloadingNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    currentKey.split("_")[2].contains("COFF") -> {
                        VegaCoffeeGrnNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }

                    currentKey.split("_")[1].contains("CM") -> if (currentKey.split("_")[2].contains(
                            "COCO"
                        )
                    ) {
                        VegaCameroonCocoaGrnNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                    currentKey.split("_")[1].contains("GH") -> {
                        if (currentKey.split("_")[2].contains("CASH")) {
                            VegaApproveGhanaNavigation.dynamicStart?.let {
                                startActivity(it)
                            }
                        } else if (currentKey.split("_")[2].contains("COCO")) {
                            VegaGhanaCocoOffloadingNavigation.dynamicStart?.let {
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
                    } else if (currentKey.split("_")[2].contains("SESA") || currentKey.split("_")[2].contains(
                            "CASH"
                        )
                    ) {
                        VegaNigeriaSesameGrnNavigation.dynamicStart?.let {
                            startActivity(it)
                        }
                    }
                }
            }
        }
    }

    private fun moveToReconcilReport() {
        VegaNicaraguaReportNavigation.dynamicStart?.let {
            startActivity(it)
        }
    }

    private fun moveToInvoice() {
        if (currentKey.split("_")[0].contains("VEGA")) {
            when {
                currentKey.split("_")[1].contains("NI") -> VegaNicaraguaInvoiceNavigation.dynamicStart?.let {
                    startActivity(it)
                }
            }
        }
    }

    private fun moveToTransactionDetails() {
        startActivity(Intent(this, TransactionActivity::class.java))
    }

    private fun moveToCocoaMtnTTransactionDetails() {
        startActivity(Intent(this, TransactionActivity::class.java).apply { putExtra(COCOA_MTNT, true) })
    }

    private fun moveToCocoaMtnRTransactionDetails() {
        startActivity(Intent(this, TransactionActivity::class.java).apply { putExtra(COCOA_MTNR, true) })
    }

    private fun moveToReprint() {
        val intent = Intent(this, TransactionActivity::class.java)
        intent.putExtra(REPRINT, true)
        startActivity(intent)

    }

    private fun moveToForwardPo() {
        VegaNicaraguaForwordPONavigation.dynamicStart?.let {
            startActivity(it)
        }
    }

    private fun moveToAdvancing() {
        VegaNicaraguaAdvanceNavigation.dynamicStart?.let {
            startActivity(it)
        }
    }

    private fun moveToIncomingLots() {
        if (currentKey.split("_")[0].contains("VEGA")) {
            if (currentKey.split("_")[2].contains("COTTGINN")) {
                VegaCottonGinningIncomingLOTsNavigation.dynamicStart?.let {
                    startActivity(it)
                }
            } else if (currentKey.split("_")[2].contains("COTTPORT")) {
                VegaCottonPortIncomingLOTsNavigation.dynamicStart?.let {
                    startActivity(it)
                }

            }
        }

    }

    private fun moveToDryingLots() {
        if (currentKey.split("_")[0].contains("VEGA")) {
            if (currentKey.split("_")[2].contains("COTTGINN")) {
                VegaCottonGinningDryingNavigation.dynamicStart?.let {
                    startActivity(it)
                }
            } else if (currentKey.split("_")[2].contains("COTTPORT")) {

            }
        }

    }

    private fun refreshMenuItems(navMenus: Menu) {
        navMenus.findItem(R.id.navReceving).isVisible = false
        navMenus.findItem(R.id.navOffloading).isVisible = false
        navMenus.findItem(R.id.navQuality).isVisible = false
        navMenus.findItem(R.id.navPpq).isVisible = false
        navMenus.findItem(R.id.navcontainer).isVisible = false
        navMenus.findItem(R.id.navPile).isVisible = false
        navMenus.findItem(R.id.navApproval).isVisible = false
        navMenus.findItem(R.id.navGrn).isVisible = false
        navMenus.findItem(R.id.navMtnr).isVisible = false
        navMenus.findItem(R.id.navSales).isVisible = false
        navMenus.findItem(R.id.navMtnt).isVisible = false
        navMenus.findItem(R.id.navProcessing).isVisible = false
        navMenus.findItem(R.id.navGateEntry).isVisible = false
        navMenus.findItem(R.id.navGateEntryApproval).isVisible = false
        navMenus.findItem(R.id.navInventory).isVisible = false
        if (!currentKey.split("_")[1].contains("NI")) navMenus.findItem(R.id.navSyncInventory).isVisible =
            false

        if (getCurrentOriginEntity().contains("OFI")) {
            navMenus.findItem(R.id.navScan).setIcon(R.drawable.ic_menu_tap_to_scan_ofi)
            navMenus.findItem(R.id.navWeighment).setIcon(R.drawable.ic_weighment_menu_ofi)
            navMenus.findItem(R.id.navReceving).setIcon(R.drawable.ic_receiving_menu_ofi)
            navMenus.findItem(R.id.navOffloading).setIcon(R.drawable.ic_offloading_menu_ofi)
            navMenus.findItem(R.id.navQuality).setIcon(R.drawable.ic_quality_menu_ofi)
            navMenus.findItem(R.id.navApproval).setIcon(R.drawable.ic_approve_menu_ofi)
            navMenus.findItem(R.id.navMtnt).setIcon(R.drawable.ic_dispatch_menu_ofi)
            navMenus.findItem(R.id.navProcessing).setIcon(R.drawable.ic_menu_processing_ofi)
            navMenus.findItem(R.id.navGateEntry).setIcon(R.drawable.ic_waiting_truck_menu_ofi)
            navMenus.findItem(R.id.navInventory).setIcon(R.drawable.ic_inventory_menu_ofi)
            navMenus.findItem(R.id.navSweepings).setIcon(R.drawable.ic_menu_sweepings_ofi)
            navMenus.findItem(R.id.navGrn).setIcon(R.drawable.ic_grn_menu_ofi)
            navMenus.findItem(R.id.navPpq).setIcon(R.drawable.ic_ppq_menu_ofi)
            navMenus.findItem(R.id.navPile).setIcon(R.drawable.ic_pile_menu_ofi)
            navMenus.findItem(R.id.navInvoice).setIcon(R.drawable.ic_invoice_menu_ofi)
            navMenus.findItem(R.id.navReconcillation).setIcon(R.drawable.ic_reconcil_menu_ofi)
            navMenus.findItem(R.id.navTransactionDetails)
                    .setIcon(R.drawable.ic_trans_details_menu_ofi)
            navMenus.findItem(R.id.navReprint).setIcon(R.drawable.ic_reprint_menu_ofi)
            navMenus.findItem(R.id.navForward).setIcon(R.drawable.ic_forward_menu_ofi)
            navMenus.findItem(R.id.navAdvance).setIcon(R.drawable.ic_advance_menu_ofi)
            navMenus.findItem(R.id.navIncomingLots).setIcon(R.drawable.ic_inventory_menu_ofi)
            navMenus.findItem(R.id.navGinningInProgress).setIcon(R.drawable.ic_inventory_menu_ofi)
            navMenus.findItem(R.id.navDryingInProgress).setIcon(R.drawable.ic_inventory_menu_ofi)
            navMenus.findItem(R.id.navPileManagement).setIcon(R.drawable.ic_inventory_menu_ofi)
            navMenus.findItem(R.id.navSecretId).setIcon(R.drawable.ic_waiting_truck_menu)
            navMenus.findItem(R.id.navBagIssue).setIcon(R.drawable.ic_bag_mgmt_ofi)
        }
    }

    private fun RefreshToolbarMenuItems() {
        /*menu?.findItem(R.id.actionVega)?.isVisible = false
        menu?.findItem(R.id.actionDO)?.isVisible = false
        menu?.findItem(R.id.actionDP)?.isVisible = false*/
        menu?.clear()
    }

    private fun getRoles(current_key: String): List<UserRole> {
        return Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
            .filter { key -> key.roleKey.equals(current_key) }
    }

    private fun moveToScannerActivity() {
        startActivityForResult(Intent(this, ScannerActivity::class.java), ACTIVITY_SCAN_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == ACTIVITY_SCAN_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                data?.extras?.getString(SCANNED_ID)?.trim()?.let { validateId(it) }
            }
        }
    }

    private fun validateId(id: String) {
        id.let {
            val isValid = validateLotId(id)
            when {
                isValid -> {
                    if (currentKey.split("_")[1].contains("NI")) {
                        VegaNicaraguaInventoryNavigation.dynamicStart?.let {
                            it.putExtra(SCANNED_ID, id)
                            startActivity(it)
                        }
                    } else if (currentKey.split("_")[2].contains("COCO")) {
                        VegaInventoryCocoNavigation.dynamicStart?.let {
                            it.putExtra(SCANNED_ID, id)
                            startActivity(it)
                        }
                    } else if (currentKey.split("_")[2].contains("COFF")) {
                        VegaInventoryCoffeeNavigation.dynamicStart?.let {
                            it.putExtra(SCANNED_ID, id)
                            startActivity(it)
                        }
                    } else if (currentKey.split("_")[2].contains("SESA")) {
                        VegaInventorySesameNavigation.dynamicStart?.let {
                            it.putExtra(SCANNED_ID, id)
                            startActivity(it)
                        }
                    } else if (currentKey.split("_")[2].contains("COTTGINN")) {
                        getScanDetailsForGinning(id)
                    } else if (currentKey.split("_")[2].contains("COTTPORT")) {
                        getScanDetailsForPort(id)
                    } else if (currentKey.split("_")[1].contains("NG") && currentKey.split("_")[2].contains(
                            "CASH"
                        )
                    ) {
                        VegaInventorySesameNavigation.dynamicStart?.let {
                            it.putExtra(SCANNED_ID, id)
                            startActivity(it)
                        }

                    } else if (currentKey.split("_")[1].contains("NG") && currentKey.split("_")[2].contains(
                            "COCO"
                        )
                    ) {
                        VegaNigeriaInventoryCocoNavigation.dynamicStart?.let {
                            it.putExtra(SCANNED_ID, id)
                            startActivity(it)
                        }

                    } else if (currentKey.split("_")[1].contains("GH") && currentKey.split("_")[2].contains(
                            "COCO"
                        )
                    ) {
                        VegaInventoryGhanaCocoaNavigation.dynamicStart?.let {
                            it.putExtra(SCANNED_ID, id)
                            startActivity(it)
                        }

                    } else {
                    }
                }
                else -> showDialog(getString(R.string.error_lot_id))
            }
        }
    }

    private fun getScanDetailsForPort(id: String?) {
        val input = workDataOf(UIUtils.QR_VALUE to id)
        showCustomLoading("Getting Scanning Details ...")
        val worker = OneTimeWorkRequest.Builder(VegaScanDetailsWorkerForPort::class.java)
            .setConstraints(constraint)
            .setInputData(input).build()
        enQueueWorker(worker, applicationContext)
        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
            .observe(this, androidx.lifecycle.Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            var bale = Gson().fromJson<PortBale>(
                                PreferenceHelper.get(
                                    TRANS_OUTPUT_DATA,
                                    ""
                                )
                            )


                            if (bale?.baleId.isNullOrBlank()) {
                                showDialog("Scanned Bale not Available...!")
                            } else {
                                VegaCottonPortBaleDetailNavigation.dynamicStart?.let {
                                    it.putExtra(UIUtils.BALE_STATUS, 1)
                                    it.putExtra(UIUtils.INVENTORY_BALE, bale as Parcelable)
                                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    applicationContext.startActivity(it)
                                }
                            }
                            hideCustomLoading()
                        }
                        WorkInfo.State.FAILED -> {
                            hideCustomLoading()
                            workInfo.outputData.getString(TRANS_OUTPUT_DATA)?.let { toast(it) }

                        }
                        WorkInfo.State.RUNNING -> {
                            hideCustomLoading()
                        }
                    }
                }

            })
    }

    private fun getScanDetailsForGinning(id: String?) {
        val input = workDataOf(UIUtils.QR_VALUE to id)
        showCustomLoading("Getting Scanning Details ...")
        val worker = OneTimeWorkRequest.Builder(VegaScanDetailsWorkerForGinning::class.java)
            .setConstraints(constraint)
            .setInputData(input).build()
        enQueueWorker(worker, applicationContext)
        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
            .observe(this, androidx.lifecycle.Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            var data = Gson().fromJson<GenericScanDetails>(
                                PreferenceHelper.get(
                                    TRANS_OUTPUT_DATA,
                                    ""
                                )
                            )
                            data?.let { scandata ->
                                if (scandata.scanType == 1) {
                                    val bale = scandata.baleDTO
                                    if (bale?.baleID.isNullOrBlank()) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Scanned Bale not Available...!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        VegaCottonGinningBaleDetailNavigation.dynamicStart?.let {
                                            it.putExtra(UIUtils.INVENTORY_BALE, bale as Parcelable)
                                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            applicationContext.startActivity(it)
                                        }
                                    }
                                } else if (scandata.scanType == 0) {
                                    val lot = scandata.lotlistDTO
                                    if (data == null) Toast.makeText(
                                        applicationContext,
                                        "Scanned Lot not Available...!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    else {
                                        VegaCottonGinningScanLotDetailsActivityNavigation.dynamicStart?.let {
                                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            it.putExtra(UIUtils.LOT_DETAIL, lot)
                                            applicationContext.startActivity(it)
                                        }

                                    }
                                } else {

                                }
                            }
                            hideCustomLoading()
                        }
                        WorkInfo.State.FAILED -> {
                            hideCustomLoading()
                            workInfo.outputData.getString(TRANS_OUTPUT_DATA)?.let { toast(it) }

                        }
                        WorkInfo.State.RUNNING -> {
                            hideCustomLoading()
                        }
                    }
                }

            })
    }


    fun validateLotId(id: String?): Boolean {
        return when {
            id.isNullOrBlank() || id.contains(" ") -> false
            else -> true
        }
    }

    override fun onResume() {
        super.onResume()
        getOfflineData()
    }

    fun getOfflineData() {
        if (currentKey.split("_")[0].contains("VEGA")) {
            if (currentKey.split("_")[1].contains("GH")) {
                if (currentKey.split("_")[2].contains("CASH")) {
                    if (AppUtils.isOnline()) {
                        vm.weighBridgeOnline.observe(this, Observer {
                            homeList.forEach { item ->
                                if (item.itemName.equals(getString(R.string.quality))) {
                                    var weighTypedata: List<VegaQualityWBDetails>? =
                                        ArrayList<VegaQualityWBDetails>(0)
                                    var weighTypedata1: List<VegaQualityWBDetails>? =
                                        ArrayList<VegaQualityWBDetails>(0)

                                    val data = it.data?.data
                                        ?.filter { value -> !value.qcStatus!!.contains("X") }
                                    weighTypedata =
                                        data?.filter { wb -> wb.direction == UIUtils.DIRECTIONIN }
                                            ?.filter { value -> value.weighBridgeType == "PROCURE" }
                                            ?.filter { value -> !value.netWeight.equals("0.0") }
                                            ?.filter { value -> value.challan.isNullOrEmpty() }

                                    val data1 = it.data?.data
                                    weighTypedata1 =
                                        data1?.filter { wb -> wb.direction == UIUtils.DIRECTIONIN }
                                            ?.filter { value -> value.weighBridgeType == "STO" }
                                            ?.filter { value -> !value.netWeight.equals("0.0") }
                                    var i: Int
                                    var i1: Int
                                    if (weighTypedata.isNullOrEmpty())
                                        i = 0
                                    else
                                        i = if (weighTypedata.size == 0) 0 else weighTypedata.size
                                    if (weighTypedata1.isNullOrEmpty())
                                        i1 = 0
                                    else
                                        i1 = weighTypedata1.size
                                    item.offlineCount = i.plus(i1).toString()
                                    /* item.offlineCount =
                            if (weighTypedata?.size == 0) "" else weighTypedata?.size.toString()*/
                                }
                            }
                            bindingActivity.rvHome.adapter?.notifyDataSetChanged()
                        })

                        vm.getWeighBridgeDataOnline()
                    } else {
                        vm.weighBridge.observe(this, Observer { response ->
                            homeList.forEach { item ->
                                if (item.itemName.equals(getString(R.string.quality))) {
                                    response.let { qualityDetail ->
                                        var weighTypedata: List<VegaQualityWBDetails>? = null
                                        var weighTypedata1: List<VegaQualityWBDetails>? = null
                                        val data =
                                            qualityDetail.filter { value ->
                                                !value.qcStatus!!.contains(
                                                    "X"
                                                )
                                            }
                                        weighTypedata =
                                            data.filter { wb -> wb.direction == UIUtils.DIRECTIONIN }
                                                .filter { value -> value.weighBridgeType == "PROCURE" }
                                                .filter { value -> value.challan.isNullOrEmpty() }
                                        var i: Int = weighTypedata!!.size
                                        val data1 =
                                            response?.filter { value -> value.qcStatus.toString() == "R" }
                                        weighTypedata1 =
                                            data1?.filter { wb -> wb.direction == UIUtils.DIRECTIONIN }
                                                ?.filter { value -> value.weighBridgeType == "STO" }
                                                ?.filter { value -> !value.netWeight.equals("0.000") }
                                        var i1: Int = weighTypedata1!!.size
                                        item.offlineCount = i.plus(i1).toString()
                                    }
                                }
                                bindingActivity.rvHome.adapter?.notifyDataSetChanged()
                            }


                        })
                        vm.getWeighBridgeDetailqualitycount()

                    }
                }
            }
        } else if (currentKey.split("_")[0].contains("DO")) {
            /* if (isOnline()) {
                 vm.DOweighBridgeOnline.observe(this, Observer {
                     homeList.forEach { item ->
                         if (item.itemName.equals(getString(R.string.quality))) {
                             it.data.let { it1 ->
                                 val doWbIds =
                                     it1?.filter { wb ->
                                         !wb.challan.isNullOrEmpty() && !wb.qcStatus!!.contains(
                                             "X"
                                         )
                                     }
                                 doWbIds?.distinctBy { Pair(it.weighBridgeId, it.weighBridgeId) }
                                 item.offlineCount = doWbIds?.size.toString()
                             }
                         }
                         bindingActivity.rvHome.adapter?.notifyDataSetChanged()
                     }
                 })
                 vm.getDOWeighBridgeDetailOnline()
             } else {
                 vm.DOweighBridgeoffline.observe(this, Observer { response ->
                     homeList.forEach { item ->
                         if (item.itemName.equals(getString(R.string.quality))) {
                             response?.let { qualityDetail ->
                                 val doWbIds =
                                     qualityDetail.filter { wb ->
                                         !wb.challan.isNullOrEmpty() && !wb.qcStatus!!.contains(
                                             "X"
                                         )
                                     }
                                 doWbIds.distinctBy { Pair(it.weighBridgeId, it.weighBridgeId) }
                                 item.offlineCount = doWbIds.size.toString()
                             }
                         }
                         bindingActivity.rvHome.adapter?.notifyDataSetChanged()
                     }
                 })
                 vm.getDOWeighBridgeDataoffline()
             }*/
        }

    }

    private fun moveToGinningDispatch() {

        VegaCottonGinningDispatchNavigation.dynamicStart?.let {
            startActivity(it)
        }
    }

    private fun moveToPortDispatch() {

        VegaCottonPortDispatchNavigation.dynamicStart?.let {
            startActivity(it)
        }
    }

    private fun updatequalityUI(response: List<VegaQualityWBDetails>?) {

        response?.let { qualityDetail ->
            if (qualityDetail.isNotEmpty()) {
                val offlineData =
                    qualityDetail.filter { value -> value.isNotWBID }
                        .filter { it.wbTempId.contains("TMP") }

                val data =
                    qualityDetail.filter { value -> !value.qcStatus!!.contains("X") }
                var weighTypedata: List<VegaQualityWBDetails>? = null

                weighTypedata = data.filter { wb -> wb.direction == UIUtils.DIRECTIONIN }
                    .filter { value -> !value.netWeight.equals("0.000") }
                    .filter { wb -> wb.direction == UIUtils.DIRECTIONIN }

            }
        }
    }
}
