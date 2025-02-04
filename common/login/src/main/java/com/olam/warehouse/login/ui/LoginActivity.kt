package com.olam.warehouse.login.ui

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.olam.warehouse.login.BuildConfig
import com.olam.warehouse.login.R
import com.olam.warehouse.login.data.domain.model.KeyCloakModel
import com.olam.warehouse.login.databinding.ActivityLoginBinding
import com.olam.warehouse.login.di.injectFeature
import com.olam.warehouse.login.services.APKInstallService
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.login.ui.quickpinaccess.VegaQuickAccessPinActivity
import com.olam.warehouse.login.vm.LoginViewModel
import com.olam.warehouse.master.ui.BaseActivity
import com.olam.warehouse.master.user.entity.User
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.master.work.getFeatureReleaseOneTimeRequestWorker
import com.olam.warehouse.master.work.getFeatureReleaseUrlOneTimeRequestWorker
import com.olam.warehouse.master.work.getInputData
import com.olam.warehouse.master.work.getOneTimeRequest
import com.olam.warehouse.navigation.SplitInstall
import com.olam.warehouse.presentation.data.domain.model.Auth
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.domain.model.LoginInfo
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.CountryCode
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.getDeviceID
import com.olam.warehouse.presentation.utils.AppUtils.getEnviroment
import com.olam.warehouse.presentation.utils.AppUtils.getVersionName
import com.olam.warehouse.presentation.utils.Constants.CURRENT_ORIGIN_KEY
import com.olam.warehouse.presentation.utils.Constants.IS_LOGGED_IN
import com.olam.warehouse.presentation.utils.UIUtils.RELEASE_ID
import com.olam.warehouse.presentation.utils.UIUtils.RELEASE_OUTPUT
import com.olam.warehouse.presentation.utils.UIUtils.RELEASE_URL_PATH
import com.olam.warehouse.presentation.utils.UIUtils.appcenterPosExtension
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.io.*
import java.util.*
import kotlin.collections.HashMap

@RuntimePermissions
class LoginActivity : BaseActivity() {

    private var userName: String = ""
    private var passWord: String = ""
    val gson = GsonUtils()
    private val vm: LoginViewModel by viewModel()
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var packageInstaller: PackageInstaller
    private lateinit var mSessionCallback: PackageInstaller.SessionCallback
    private var mSessionId = 0
    private var releaseIdList = arrayListOf<String>()
    var releaseUrls = HashMap<String, String>()
    private val REQUEST_CODE = 220
    private val LOCK_REQUEST_CODE = 221
    private val SECURITY_SETTING_REQUEST_CODE = 233


    private lateinit var binding: ActivityLoginBinding
    override val layoutResourceId = R.layout.activity_login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setEntityLevelVarient()
        initFirebase()
        doManualFetch()
        injectFeature()
        initExtras()
        initUI()
        packageInstaller = packageManager.packageInstaller
        mSessionCallback = InstallSessionCallback()
    }

    private fun initUI() {
        supportActionBar?.hide()
        if (!getEnviroment().equals("release")) binding.tvVersion.text =
            getVersionName().plus("(").plus(getEnviroment()).plus(")")
        else binding.tvVersion.text = getVersionName()
        binding.btnLogin.setOnClickListener {
            PreferenceHelper.save(Constants.ACCESS_TOKEN, "")
            validateInput()
        }

        vm.auth.observe(this, Observer { updateToken(it) })
        vm.user.observe(this, Observer { updateUI(it) })
        //vm.keyCloakId.observe(this, Observer { updateKeyCloakResponse(it) })
        //getAppcenterReleaseId()
    }


    private fun initExtras() {
        when {
            intent.getBooleanExtra(IS_LOGGED_IN, false) -> {
                authenticateUser()//TODO should navigate based on user
            }
        }
    }

    private fun setEntityLevelVarient() {
        when {
            PreferenceHelper.get(CURRENT_ORIGIN_KEY, "").contains(Constants.OFI) -> {
                binding.ivLogo.setImageDrawable(binding.ivLogo.context.getDrawable(com.olam.warehouse.presentation.R.drawable.ic_olam_logo_ofi_new))
                /*ViewCompat.setBackgroundTintList(
                    binding.btnLogin,
                    this.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                        )
                    }
                )*/
                getActionBtnChangedView(binding.btnLogin, this, true)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        packageInstaller.registerSessionCallback(mSessionCallback)
    }

    override fun onDestroy() {
        hideLoading()
        super.onDestroy()
    }

    private fun getAppcenterReleaseId() {
        var featureList = listOf<String>()
        when (getEnviroment()) {
            "release" -> featureList = getProdFeatureList()
            "uat" -> featureList = getUatFeatureList()
        }
        val release_ids = Gson().fromJson<HashMap<String, String>>(PreferenceHelper.get(Constants.RELEASEID, ""))
        if (featureList.size > 0) {
            if (release_ids.isNullOrEmpty() || featureList.size != release_ids.size) {
                featureList.forEachIndexed { index, item ->
                    showLoading()
                    var group = ""
                    when {
                        item.equals(BASE_PATH) -> group = item
                        item.equals(PROD_BASE_PATH) -> group = item
                        else -> group = item
                    }
                    val basePath =
                        if (getEnviroment().equals("uat")) "https://api.appcenter.ms/v0.1/apps/olam-vega/VegaX-Uat/distribution_groups/$group/releases"
                        else "https://api.appcenter.ms/v0.1/apps/olam-vega/vegax/distribution_groups/$group/releases"
                    val input = workDataOf(RELEASE_ID to basePath)
                    val worker = getFeatureReleaseOneTimeRequestWorker(input, group)
                    enQueueWorker(worker, this)
                    WorkManager.getInstance(this).getWorkInfoByIdLiveData(worker.id)
                        .observe(this, Observer { workInfo ->
                            if (workInfo != null) {
                                when (workInfo.state) {
                                    WorkInfo.State.SUCCEEDED -> {
                                        hideLoading()
                                        val apk = appcenterPosExtension(workInfo.tags)
                                        val id = workInfo.outputData.getString(RELEASE_OUTPUT)
                                        val version = workInfo.outputData.getString(UIUtils.RELEASE_VERSION)
                                        releaseIdList.add(apk.plus("&").plus(id.toString()))
                                        if (featureList.size == releaseIdList.size) getAppcenterReleaseUrls()
                                    }
                                    WorkInfo.State.FAILED -> {
                                        hideLoading()
                                    }
                                }
                            }
                        })
                }
            } else {
                releaseUrls = release_ids
            }
        }
    }

    private fun getAppcenterReleaseUrls() {
        releaseIdList.forEachIndexed { index, item ->
            showLoading()
            val basePath =
                if (getEnviroment().equals("uat")) "https://api.appcenter.ms/v0.1/apps/olam-vega/VegaX-Uat/distribution_groups/${item.split(
                    "&"
                )[0]}/releases/${item.split("&")[1]}"
                else "https://api.appcenter.ms/v0.1/apps/olam-vega/vegax/distribution_groups/${item.split("&")[0]}/releases/${item.split(
                    "&"
                )[1]}"
            val input = workDataOf(RELEASE_URL_PATH to basePath)
            val worker = getFeatureReleaseUrlOneTimeRequestWorker(input, item.split("&")[0])
            enQueueWorker(worker, this)
            showLoading()
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(worker.id)
                .observe(this, Observer { workInfo ->
                    if (workInfo != null) {
                        when (workInfo.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                hideLoading()
                                val apk = appcenterPosExtension(workInfo.tags)
                                val url = workInfo.outputData.getString(UIUtils.RELEASE_URL_OUTPUT)
                                val version = workInfo.outputData.getString(UIUtils.RELEASE_VERSION)
                                releaseUrls.put(apk, url.toString())
                                if (index.equals(releaseUrls.size - 1)) {
                                    PreferenceHelper.save(Constants.RELEASEID, gson.toJson(releaseUrls))
                                }
                            }
                            WorkInfo.State.FAILED -> {
                                hideLoading()
                            }
                        }
                    }
                })
        }
    }

    private fun initFirebase() {
        remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(BuildConfig.DEBUG)
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

    }

    private fun doManualFetch() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d("RMLog", "Config params updated: $updated")
                } else {
                    //toast("Fetch failed")

                }
            }
    }

    private fun validateInput() {
        userName = binding.etUserName.text.toString().trim()
        passWord = binding.etPassword.text.toString().trim()
        when {
            userName.isEmpty() -> {
                binding.tilPassword.isErrorEnabled = false
                binding.tilUserName.error = "Enter valid username"
                binding.etUserName.requestFocus()
                binding.btnLogin.isClickable = true
            }
            passWord.isEmpty() -> {
                binding.tilUserName.isErrorEnabled = false
                binding.tilPassword.error = "Enter valid password"
                binding.tilPassword.requestFocus()
                binding.btnLogin.isClickable = true
            }
            else -> fetchToken()
        }
    }

    private fun fetchToken() {
        val loginTime = DateUtils.getCurrentTimeInMills()
        PreferenceHelper.save(Constants.LOGIN_AT, loginTime.toString())
        binding.tilUserName.isErrorEnabled = false
        binding.tilPassword.isErrorEnabled = false
        showLoading()
        val loginInfo = LoginInfo(username = userName, password = passWord, grant_type = Constants.GRANT_TYPE)
        vm.getAuthDetail(loginInfo)
        PreferenceHelper.save("languageCode", LocaleHelper.getLocale(this).language)
    }

    private fun updateToken(data: Resource<Auth>?) {
        data?.let { response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    //hideLoading()
                    vm.saveToken(response.data)
                    vm.getUserDetail(getDeviceID(this))
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    binding.btnLogin.isClickable = true
                    val msg = response.error?.let {
                        if (it.contains("400") ||
                            it.contains("401")
                        ) "Enter the valid username or password"
                        else it
                    }
                    toast(msg.toString())
                }
            }
        }
    }

    private fun updateUI(response: Resource<User>?) {
        response?.let {
            binding.btnLogin.isClickable = true
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    //hideLoading()
                    val validEntity = PreferenceHelper.get(Constants.IS_VALID_ENTITY, false)
                    if (validEntity == true) {
                        PreferenceHelper.save(Constants.IS_VALID_ENTITY, false)
                        it.data?.let { data -> vm.saveWarehouseId(data, passWord) }
                        val keys =
                            Gson().fromJson<List<String>>(PreferenceHelper.get(Constants.KEYS, ""))
                        /*if (keys.size == 1 && (getEnviroment().equals("release")*//* || getEnviroment().equals("uat")*//*)) {
                        downloadFeature(keys)
                    } else {*/
                        if (keys.size == 1) PreferenceHelper.save(
                            Constants.SELECTED_KEYS,
                            gson.toJson(keys)
                        )
                        val keyCloakID = PreferenceHelper.get(Constants.KEYCLOAK_ID, "")
                        /*if (keyCloakID.isNullOrEmpty()) updateKeyCloakId(keyCloakID.toString()) else*/ authenticateUser()
//                    }
                    } else {
                        hideLoading()
                        UIUtils.showPendingAlertDialog(
                            this,
                            "You are not allowed to access this app. Kindly contact the technical team for more information"
                        )
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    PreferenceHelper.save(Constants.ACCESS_TOKEN, "")
                    hideLoading()
                    toast(it.error ?: "User Authentication failed...")
                }
            }
        }
    }

    private fun updateKeyCloakId(keyCloakID: String) {
        vm.updateKeyCloakId()
    }

    private fun updateKeyCloakResponse(response: Resource<GenericReqAndResp<KeyCloakModel>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    authenticateUser()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    authenticateUser()
                }
            }
        }

    }

    private fun authenticateUser() {
        hideLoading()
        val selectedKeys = Gson().fromJson<List<String>>(PreferenceHelper.get(Constants.SELECTED_KEYS, ""))
        if (selectedKeys.isNullOrEmpty()) {
            isResetLanaguage()
            startActivity(Intent(this, ConfigActivity::class.java))
            finish()
        } else {
            isResetLanaguage()
            val isSecurityPin = PreferenceHelper.get(Constants.IS_SECURITY_PIN, false)
            when (isSecurityPin) {
                true -> moveQuickAccessPinActivity() // Quick Pin Access
                else -> moveToHomeActivty()
            }
        }
    }

    private fun moveQuickAccessPinActivity() {
        val isDevicePin = PreferenceHelper.get(Constants.IS_DEVICE_PIN, false)
        val createdNewPin = PreferenceHelper.get(Constants.QUICK_PIN, "")
        if (isDevicePin) {
            moveToHomeActivty()
        } else {
            if (createdNewPin.isEmpty()) {
                startActivity(Intent(this, VegaQuickAccessPinActivity::class.java))
                finish()
            } else {
                moveToHomeActivty()
            }
        }
    }

    private fun moveToHomeActivty() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LOCK_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                //If screen lock authentication is success update text
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
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
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
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

    private fun downloadFeature(keys: List<String>) {
        val path = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath.toString() + File.separator
        var baseFilePath = remoteConfig.getString(BASE_PATH)
        baseFilePath = when (AppUtils.getEnviroment()) {
            "release" -> releaseUrls.get(PROD_BASE_PATH).toString()
            "uat" -> releaseUrls.get(BASE_PATH).toString()
            else -> releaseUrls.get(BASE_PATH).toString()
        }
        PreferenceHelper.save(Constants.SELECTED_KEYS, gson.toJson(keys))
        keys.forEachIndexed { index, it ->
            if (it.split("_")[0].contains("DO")) {
                val list = mutableListOf<String>()
                val roleData = Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
                    .filter { key -> key.roleKey.equals(it) }
                roleData.forEach { rol -> list.add(rol.roleName) }
                //vm.getRoles(it)?.forEach { rol -> list.add(rol.roleName) }
                list.forEach {
                    when (UserRoles.valueOfEnum(it.replace(" ", "_"))) {
                        UserRoles.SUPPLIER -> SplitInstall.download(this, "doreceiving", dolistener)
                        UserRoles.QUALITY -> SplitInstall.download(this, "odquality", dolistener)
                    }
                }
            } else if (it.split("_")[0].contains("VEGA")) {
                val list = mutableListOf<String>()
                val roleData = Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
                    .filter { key -> key.roleKey.equals(it) }
                roleData.forEach { rol -> list.add(rol.roleName) }
                when {
                    it.split("_")[1].contains("EC") -> {
                        val rolesConatain = arrayListOf<String>()
                        rolesConatain.add(UserRoles.BASE.role)
                        val workList = arrayListOf<OneTimeWorkRequest>()
                        val baseWorker = getOneTimeRequest(getInputData(baseFilePath, path, "base.apk"))
                        baseWorker.let { work -> workList.add(work) }
                        list.forEach { it1 ->
                            when (UserRoles.valueOfEnum(it1)) {
                                UserRoles.QUALITY -> {
                                    rolesConatain.add(UserRoles.QUALITY.role)
                                    val qualityPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_ECUADOR_QUALITY_PATH else ECUADOR_QUALITY_PATH)
                                            .toString()
                                    Log.d("RMLog", "quality file path: $qualityPath")
                                    val ecuadorQualityWorker =
                                        getOneTimeRequest(getInputData(qualityPath, path, "quality.apk"))
                                    ecuadorQualityWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(ecuadorQualityWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Quality APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.QUALITY.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.OFFLOADING -> {
                                    rolesConatain.add(UserRoles.OFFLOADING.role)
                                    val offloadingPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_ECUADOR_OFFLOADING_PATH else ECUADOR_OFFLOADING_PATH)
                                            .toString()
                                    Log.d("RMLog", "offloading file path: $offloadingPath")
                                    val ecuadorOffloadingWorker =
                                        getOneTimeRequest(getInputData(offloadingPath, path, "offloading.apk"))
                                    ecuadorOffloadingWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(ecuadorOffloadingWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Offloading APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.OFFLOADING.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                /*UserRoles.APPROVE -> {
                                    rolesConatain.add(UserRoles.APPROVE.role)
                                    val approvePath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_ECUADOR_APPROVE_PATH else ECUADOR_APPROVE_PATH)
                                            .toString()
                                    Log.d("RMLog", "approve file path: $approvePath")
                                    val ecuadorApproveWorker =
                                        getOneTimeRequest(getInputData(approvePath, path, "approve.apk"))
                                    ecuadorApproveWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(ecuadorApproveWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Approve APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.APPROVE.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }*/
                                UserRoles.MTNT -> {
                                    rolesConatain.add(UserRoles.MTNT.role)
                                    val dispatchPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_ECUADOR_DISPATCH_PATH else ECUADOR_DISPATCH_PATH)
                                            .toString()
                                    Log.d("RMLog", "dispatch file path: $dispatchPath")
                                    val ecuadorDispatchWorker =
                                        getOneTimeRequest(getInputData(dispatchPath, path, "dispatch.apk"))
                                    ecuadorDispatchWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(ecuadorDispatchWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Dispatch APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.MTNT.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.INVENTORY -> {
                                    rolesConatain.add(UserRoles.INVENTORY.role)
                                    val inventoryPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_ECUADOR_INVENTORY_PATH else ECUADOR_INVENTORY_PATH)
                                            .toString()
                                    Log.d("RMLog", "inventory file path: $inventoryPath")
                                    val ecuadorInventoryWorker =
                                        getOneTimeRequest(getInputData(inventoryPath, path, "inventory.apk"))
                                    ecuadorInventoryWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(ecuadorInventoryWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Inventory APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.INVENTORY.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.GRN -> {
                                    rolesConatain.add(UserRoles.GRN.role)
                                    val grnPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_ECUADOR_GRN_PATH else ECUADOR_GRN_PATH)
                                            .toString()
                                    Log.d("RMLog", "grn file path: $grnPath")
                                    val ecuadorGrnWorker =
                                        getOneTimeRequest(getInputData(grnPath, path, "grn.apk"))
                                    ecuadorGrnWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(ecuadorGrnWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("GRN APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.GRN.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                            }
                        }
                        enQueueChainWorker(workList, this)
                        WorkManager.getInstance(this).getWorkInfoByIdLiveData(baseWorker.id)
                            .observe(this, Observer { workInfo ->
                                if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                    toast("Base APK Downloaded successfully")
                                    rolesConatain.remove(UserRoles.BASE.role)
                                    if (rolesConatain.size == 0) {
                                        mSessionId = installApk(path)
                                    }
                                }
                            })
                    }
                    it.split("_")[2].contains("CASH") -> {
                        val rolesConatain = arrayListOf<String>()
                        rolesConatain.add(UserRoles.BASE.role)
                        val workList = arrayListOf<OneTimeWorkRequest>()
                        val baseWorker = getOneTimeRequest(getInputData(baseFilePath, path, "base.apk"))
                        baseWorker.let { work -> workList.add(work) }
                        list.forEach { it1 ->
                            when (UserRoles.valueOfEnum(it1)) {
                                UserRoles.SUPPLIER -> {
                                    //SplitInstall.download(this, "receiving", vegalistener)
                                    rolesConatain.add(UserRoles.SUPPLIER.role)
                                    val receivingPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_CASHEW_RECEIVING_PATH else CASHEW_RECEIVING_PATH)
                                            .toString()
                                    Log.d("RMLog", "receiving file path: $receivingPath")
                                    val cashewReceivingWorker =
                                        getOneTimeRequest(getInputData(receivingPath, path, "receiving.apk"))
                                    cashewReceivingWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(cashewReceivingWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Receiving APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.SUPPLIER.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })

                                }
                                UserRoles.QUALITY -> {
                                    //SplitInstall.download(this, "quality", vegalistener)
                                    rolesConatain.add(UserRoles.QUALITY.role)
                                    val qualityPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_CASHEW_QUALITY_PATH else CASHEW_QUALITY_PATH)
                                            .toString()
                                    Log.d("RMLog", "quality file path: $qualityPath")
                                    val cashewQualityWorker =
                                        getOneTimeRequest(getInputData(qualityPath, path, "quality.apk"))
                                    cashewQualityWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(cashewQualityWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Quality APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.QUALITY.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.OFFLOADING -> {
                                    //SplitInstall.download(this, "offloading", vegalistener)
                                    rolesConatain.add(UserRoles.OFFLOADING.role)
                                    val offloadingPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_CASHEW_OFFLOADING_PATH else CASHEW_OFFLOADING_PATH)
                                            .toString()
                                    Log.d("RMLog", "offloading file path: $offloadingPath")
                                    val cashewOffloadingWorker =
                                        getOneTimeRequest(getInputData(offloadingPath, path, "offloading.apk"))
                                    cashewOffloadingWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(cashewOffloadingWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Offloading APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.OFFLOADING.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.APPROVE -> {
                                    //SplitInstall.download(this, "approve", vegalistener)
                                    rolesConatain.add(UserRoles.APPROVE.role)
                                    val approvePath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_CASHEW_APPROVE_PATH else CASHEW_APPROVE_PATH)
                                            .toString()
                                    Log.d("RMLog", "approve file path: $approvePath")
                                    val cashewApproveWorker =
                                        getOneTimeRequest(getInputData(approvePath, path, "approve.apk"))
                                    cashewApproveWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(cashewApproveWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Approve APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.APPROVE.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.MTNT -> {
                                    //SplitInstall.download(this, "dispatch", vegalistener)
                                    rolesConatain.add(UserRoles.MTNT.role)
                                    val dispatchPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_CASHEW_DISPATCH_PATH else CASHEW_DISPATCH_PATH)
                                            .toString()
                                    Log.d("RMLog", "dispatch file path: $dispatchPath")
                                    val cashewDispatchWorker =
                                        getOneTimeRequest(getInputData(dispatchPath, path, "dispatch.apk"))
                                    cashewDispatchWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(cashewDispatchWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Dispatch APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.MTNT.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.SALES -> {
                                }
                                UserRoles.PROCESSING -> {
                                    // SplitInstall.download(this, "processing", vegalistener)
                                    rolesConatain.add(UserRoles.PROCESSING.role)
                                    val processingPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_CASHEW_PROCESSING_PATH else CASHEW_PROCESSING_PATH)
                                            .toString()
                                    Log.d("RMLog", "processing file path: $processingPath")
                                    val cashewProcessingWorker =
                                        getOneTimeRequest(getInputData(processingPath, path, "processing.apk"))
                                    cashewProcessingWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(cashewProcessingWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Processing APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.PROCESSING.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.GATEENTRY -> {
                                    //SplitInstall.download(this, "gateentry", vegalistener)
                                    rolesConatain.add(UserRoles.GATEENTRY.role)
                                    val gateEntryPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_CASHEW_GATEENTRY_PATH else CASHEW_GATEENTRY_PATH)
                                            .toString()
                                    Log.d("RMLog", "gateentry file path: $gateEntryPath")
                                    val cashewGateWorker =
                                        getOneTimeRequest(
                                            getInputData(
                                                gateEntryPath,
                                                path,
                                                "gateentry.apk"
                                            )
                                        )
                                    cashewGateWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(cashewGateWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("GateEntry APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.GATEENTRY.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.GATEENTRYAPPROVAL -> {
                                    //SplitInstall.download(this, "gateentryapproval", vegalistener)
                                    rolesConatain.add(UserRoles.GATEENTRYAPPROVAL.role)
                                    val gateEntryPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_CASHEW_GATEENTRY_APPROVAL_PATH else CASHEW_GATEENTRY_APPROVAL_PATH)
                                            .toString()
                                    Log.d("RMLog", "gateentryapproval file path: $gateEntryPath")
                                    val cashewGateWorker =
                                        getOneTimeRequest(
                                            getInputData(
                                                gateEntryPath,
                                                path,
                                                "gateentryapproval.apk"
                                            )
                                        )
                                    cashewGateWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(cashewGateWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("GateEntryApproval APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.GATEENTRYAPPROVAL.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.INVENTORY -> {
                                    // SplitInstall.download(this, "inventory", vegalistener)
                                    rolesConatain.add(UserRoles.INVENTORY.role)
                                    val inventoryPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_CASHEW_INVENTORY_PATH else CASHEW_INVENTORY_PATH)
                                            .toString()
                                    Log.d("RMLog", "inventory file path: $inventoryPath")
                                    val cashewInventoryWorker =
                                        getOneTimeRequest(
                                            getInputData(
                                                inventoryPath,
                                                path,
                                                "inventory.apk"
                                            )
                                        )
                                    cashewInventoryWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(cashewInventoryWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Inventory APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.INVENTORY.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.PCH -> {
                                    SplitInstall.download(this, "pch", vegalistener)
                                }
                                UserRoles.GRN -> {
                                }
                            }
                        }

                        enQueueChainWorker(workList, this)
                        WorkManager.getInstance(this).getWorkInfoByIdLiveData(baseWorker.id)
                            .observe(this, Observer { workInfo ->
                                if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                    toast("Base APK Downloaded successfully")
                                    rolesConatain.remove(UserRoles.BASE.role)
                                    if (rolesConatain.size == 0) {
                                        mSessionId = installApk(path)
                                    }
                                }
                            })

                    }
                    it.split("_")[2].contains("COCO") -> {
                        val rolesConatain = arrayListOf<String>()
                        rolesConatain.add(UserRoles.BASE.role)
                        val workList = arrayListOf<OneTimeWorkRequest>()
                        val baseWorkerCocoa = getOneTimeRequest(getInputData(baseFilePath, path, "base.apk"))
                        baseWorkerCocoa.let { work -> workList.add(work) }
                        list.forEach { it1 ->
                            when (UserRoles.valueOfEnum(it1)) {
                                UserRoles.MTNT -> {
                                    //SplitInstall.download(this, "mtntcocoa", vegalistener)
                                    rolesConatain.add(UserRoles.MTNT.role)
                                    val mtntPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_COCOA_MTNT_PATH else COCOA_MTNT_PATH)
                                            .toString()
                                    Log.d("RMLog", "mtnt file path: $mtntPath")
                                    val cocoaMtntWorker =
                                        getOneTimeRequest(getInputData(mtntPath, path, "mtnt.apk"))
                                    cocoaMtntWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(cocoaMtntWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Mtnt APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.MTNT.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.SALES -> {
                                    //SplitInstall.download(this, "salescocoa", vegalistener)
                                    rolesConatain.add(UserRoles.SALES.role)
                                    val salesPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_COCOA_SALES_PATH else COCOA_SALES_PATH)
                                            .toString()
                                    Log.d("RMLog", "sales file path: $salesPath")
                                    val cocoaSalesWorker =
                                        getOneTimeRequest(getInputData(salesPath, path, "sales.apk"))
                                    cocoaSalesWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(cocoaSalesWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Sales APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.SALES.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.PROCESSING -> {
                                    //SplitInstall.download(this, "processingcocoa", vegalistener)
                                    rolesConatain.add(UserRoles.PROCESSING.role)
                                    val processingPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_COCOA_PROCESSING_PATH else COCOA_PROCESSING_PATH)
                                            .toString()
                                    Log.d("RMLog", "processing file path: $processingPath")
                                    val cocoaProcessingWorker =
                                        getOneTimeRequest(getInputData(processingPath, path, "processing.apk"))
                                    cocoaProcessingWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(cocoaProcessingWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Processing APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.PROCESSING.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.INVENTORY -> {
                                    // SplitInstall.download(this, "inventorycocoa", vegalistener)
                                    rolesConatain.add(UserRoles.INVENTORY.role)
                                    val inventoryPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_COCOA_INVENTORY_PATH else COCOA_INVENTORY_PATH)
                                            .toString()
                                    Log.d("RMLog", "inventory file path: $inventoryPath")
                                    val cocoaInventoryWorker =
                                        getOneTimeRequest(getInputData(inventoryPath, path, "inventory.apk"))
                                    cocoaInventoryWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(cocoaInventoryWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Inventory APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.INVENTORY.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.SWEEPINGS -> {
                                    // SplitInstall.download(this, "sweepingcocoa", vegalistener)
                                    rolesConatain.add(UserRoles.SWEEPINGS.role)
                                    val sweepingPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_COCOA_SWEEPING_PATH else COCOA_SWEEPING_PATH)
                                            .toString()
                                    Log.d("RMLog", "sweeping file path: $sweepingPath")
                                    val cocoaSweepingWorker =
                                        getOneTimeRequest(getInputData(sweepingPath, path, "sweeping.apk"))
                                    cocoaSweepingWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(cocoaSweepingWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Sweeping APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.SWEEPINGS.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }

                            }
                        }

                        enQueueChainWorker(workList, this)
                        WorkManager.getInstance(this).getWorkInfoByIdLiveData(baseWorkerCocoa.id)
                            .observe(this, Observer { workInfo ->
                                if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                    toast("Base APK Downloaded successfully")
                                    rolesConatain.remove(UserRoles.BASE.role)
                                    if (rolesConatain.size == 0) {
                                        mSessionId = installApk(path)
                                    }
                                }
                            })
                    }
                    it.split("_")[2].contains("COFF") -> {
                        val rolesConatain = arrayListOf<String>()
                        rolesConatain.add(UserRoles.BASE.role)
                        val workList = arrayListOf<OneTimeWorkRequest>()
                        val baseWorker = getOneTimeRequest(getInputData(baseFilePath, path, "base.apk"))
                        baseWorker.let { work -> workList.add(work) }
                        list.forEach { it1 ->
                            when (UserRoles.valueOfEnum(it1)) {
                                UserRoles.SUPPLIER -> {
                                }
                                UserRoles.QUALITY -> {
                                }
                                UserRoles.OFFLOADING -> {
                                }
                                UserRoles.APPROVE -> {
                                }
                                UserRoles.MTNT -> {
                                }
                                UserRoles.SALES -> {
                                }
                                UserRoles.PROCESSING -> {
                                    //SplitInstall.download(this, "processingcoffee", vegalistener)
                                    rolesConatain.add(UserRoles.PROCESSING.role)
                                    val processingPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_COFFEE_PROCESSING_PATH else COFFEE_PROCESSING_PATH)
                                            .toString()
                                    Log.d("RMLog", "processing file path: $processingPath")
                                    val coffeeProcessingWorker =
                                        getOneTimeRequest(
                                            getInputData(
                                                processingPath,
                                                path,
                                                "processing.apk"
                                            )
                                        )
                                    coffeeProcessingWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(coffeeProcessingWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Processing APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.PROCESSING.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.GATEENTRY -> {
                                }
                                UserRoles.GATEENTRYAPPROVAL -> {
                                }
                                UserRoles.INVENTORY -> {
                                    //SplitInstall.download(this, "inventorycoffee", vegalistener)
                                    rolesConatain.add(UserRoles.INVENTORY.role)
                                    val inventoryPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_COFFEE_INVENTORY_PATH else COFFEE_INVENTORY_PATH)
                                            .toString()
                                    Log.d("RMLog", "inventory file path: $inventoryPath")
                                    val coffeeInventoryWorker =
                                        getOneTimeRequest(
                                            getInputData(
                                                inventoryPath,
                                                path,
                                                "inventory.apk"
                                            )
                                        )
                                    coffeeInventoryWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(coffeeInventoryWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("Inventory APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.INVENTORY.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.CONTAINER -> {
                                    rolesConatain.add(UserRoles.CONTAINER.role)
                                    val ppqPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_COFFEE_CONTAINER_PATH else COFFEE_CONTAINER_PATH)
                                            .toString()
                                    Log.d("RMLog", "container file path: $ppqPath")
                                    val coffeePpqWorker =
                                        getOneTimeRequest(
                                            getInputData(
                                                ppqPath,
                                                path,
                                                "container.apk"
                                            )
                                        )
                                    coffeePpqWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(coffeePpqWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("CONTAINER APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.CONTAINER.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.PPQ -> {
                                    rolesConatain.add(UserRoles.PPQ.role)
                                    val ppqPath =
                                        releaseUrls.get(if (getEnviroment().equals("release")) PROD_COFFEE_PPQ_PATH else COFFEE_PPQ_PATH)
                                            .toString()
                                    Log.d("RMLog", "ppq file path: $ppqPath")
                                    val coffeePpqWorker =
                                        getOneTimeRequest(getInputData(ppqPath, path, "ppq.apk"))
                                    coffeePpqWorker.let { work -> workList.add(work) }
                                    WorkManager.getInstance(this)
                                        .getWorkInfoByIdLiveData(coffeePpqWorker.id)
                                        .observe(this, Observer { workInfo ->
                                            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                                toast("PPQ APK Downloaded successfully")
                                                rolesConatain.remove(UserRoles.PPQ.role)
                                                if (rolesConatain.size == 0) {
                                                    mSessionId = installApk(path)
                                                }
                                            }
                                        })
                                }
                                UserRoles.GRN -> {
                                }
                            }
                        }
                        enQueueChainWorker(workList, this)
                        WorkManager.getInstance(this).getWorkInfoByIdLiveData(baseWorker.id)
                            .observe(this, Observer { workInfo ->
                                if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                    toast("Base APK Downloaded successfully")
                                    rolesConatain.remove(UserRoles.BASE.role)
                                    if (rolesConatain.size == 0) {
                                        mSessionId = installApk(path)
                                    }
                                }
                            })
                    }
                }
            }

            /*if (index.equals(keys.size - 1)) {
                injectDOFeature()
            }*/
        }

    }

    private fun createProductDirectory(it: String, sub: String): String {
        return when {
            it.split("_")[2].contains("CASH") -> createDirectory("/Cashew/".plus(sub), this)
            it.split("_")[2].contains("COFF") -> createDirectory("/Coffee/".plus(sub), this)
            it.split("_")[2].contains("COCO") -> createDirectory("/cocoa/".plus(sub), this)
            else -> createDirectory("/VegaX/", this)
        }
    }

    private val dpivclistener: () -> Unit = {}

    private val dolistener: () -> Unit = {}

    private val vegalistener: () -> Unit = { }

    ///////////////////////////  APK installer utility ////////////////////////////////////////////////////

    private fun installApk(apkFolderPath: String): Int {
        val nameSizeMap = HashMap<String, Long>()
        var totalSize: Long = 0
        var sessionId = 0
        val folder = File(apkFolderPath)
        val listOfFiles = folder.listFiles()
        try {
            for (listOfFile in listOfFiles) {
                if (listOfFile.isFile) {
                    Log.d("AppLog", "installApk: " + listOfFile.name)
                    nameSizeMap[listOfFile.name] = listOfFile.length()
                    totalSize += listOfFile.length()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
        val installParams = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        installParams.setSize(totalSize)
        try {
            sessionId = packageInstaller.createSession(installParams)
            Log.d("AppLog", "Success: created install session [$sessionId]")
            for ((key, value) in nameSizeMap) {
                doWriteSession(sessionId, apkFolderPath + File.separator + key, value, key)
            }
            doCommitSession(sessionId)
            Log.d("AppLog", "Success")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return sessionId
    }

    private fun doWriteSession(sessionId: Int, inPath: String?, sizeBytes: Long, splitName: String): Int {
        var inPathToUse = inPath
        var sizeBytesToUse = sizeBytes
        if ("-" == inPathToUse) {
            inPathToUse = null
        } else if (inPathToUse != null) {
            val file = File(inPathToUse)
            if (file.isFile)
                sizeBytesToUse = file.length()
        }
        var session: PackageInstaller.Session? = null
        var inputStream: InputStream? = null
        var out: OutputStream? = null
        try {
            session = packageInstaller.openSession(sessionId)
            if (inPathToUse != null) {
                inputStream = FileInputStream(inPathToUse)
            }
            out = session.openWrite(splitName, 0, sizeBytesToUse)
            var total = 0
            val buffer = ByteArray(65536)
            var c: Int
            while (true) {
                c = inputStream!!.read(buffer)
                if (c == -1)
                    break
                total += c
                out.write(buffer, 0, c)
            }
            session.fsync(out)
            Log.d("AppLog", "Success: streamed $total bytes")
            return PackageInstaller.STATUS_SUCCESS
        } catch (e: IOException) {
            Log.e("AppLog", "Error: failed to write; " + e.message)
            return PackageInstaller.STATUS_FAILURE
        } finally {
            try {
                out?.close()
                inputStream?.close()
                session?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun doCommitSession(sessionId: Int) {
        var session: PackageInstaller.Session? = null
        try {
            try {
                session = packageInstaller.openSession(sessionId)
                val callbackIntent = Intent(applicationContext, APKInstallService::class.java)
                val pendingIntent = PendingIntent.getService(applicationContext, 0, callbackIntent, 0)
                session.commit(pendingIntent.intentSender)
                session.close()
                Log.d("AppLog", "install request sent")
                Log.d("AppLog", "doCommitSession: " + packageInstaller.mySessions)
                Log.d("AppLog", "doCommitSession: after session commit ")
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } finally {
            session!!.close()
        }
    }

    private inner class InstallSessionCallback : PackageInstaller.SessionCallback() {

        override fun onProgressChanged(sessionId: Int, progress: Float) {
            showLoading()
            Log.d(
                "AppLog",
                "Session Callback: SeesionID = $sessionId onprogressValue= $progress onProgressChanged"
            )
            /* if (mSessionId == sessionId && progress > .9) {
                 validateInput()
             }*/
        }

        override fun onActiveChanged(sessionId: Int, active: Boolean) {
            Log.d("AppLog", "Session Callback: SeesionID = $sessionId onActiveChanged")
        }

        override fun onFinished(sessionId: Int, success: Boolean) {
            hideLoading()
            Log.d("AppLog", "Session Callback: SeesionID = $sessionId onFinished")
//            authenticateUser()
            if (mSessionId == sessionId && success) {
                // validateInput()
                authenticateUser()
            }
        }

        override fun onBadgingChanged(sessionId: Int) {
            Log.d("AppLog", "Session Callback: SeesionID = $sessionId onBadgingChanged")
        }

        override fun onCreated(sessionId: Int) {
            Log.d("AppLog", "Session Callback: SeesionID = $sessionId onCreated")
        }

    }


    ////////////////////////   Permission Handling //////////////////////////////////


    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showPermission() {
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun isResetLanaguage() {
        var countryCode = PreferenceHelper.get(Constants.COUNTRY_CODE, "")
        val language = Locale.getDefault().language
        if (!countryCode.isEmpty() && !language.equals("en")) {
            if (countryCode.equals(CountryCode.GUATEMALA.code)) {
                if (!language.equals("es"))
                    isReset()
            } else if (countryCode.equals(CountryCode.INDONESIA.code)) {
                if (!language.equals("in"))
                    isReset()
            } else if (countryCode.equals(CountryCode.VIETNAM.code)) {
                if (!language.equals("vi"))
                    isReset()
            } else if (countryCode.equals(CountryCode.PERU.code)) {
                if (!language.equals("es"))
                    isReset()
            } else if (countryCode.equals(CountryCode.COLUMBIA.code)) {
                if (!language.equals("es"))
                    isReset()
            } else if (countryCode.equals(CountryCode.HONDURAS.code)) {
                if (!language.equals("es"))
                    isReset()
            } else if (countryCode.equals(CountryCode.MOZAMBIQUE.code)) {
                if (!language.equals("en"))
                    isReset()
            }

        }
    }

    fun isReset() {
        LocaleHelper.setLocale(this, Locale("en", ""))
        PreferenceHelper.save("languageCode", "en")

    }

}


