package com.olam.warehouse.master.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.work.*
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.gson.Gson
import com.olam.warehouse.master.R
import com.olam.warehouse.master.R.array.user_info
import com.olam.warehouse.master.common.data.work.MasterDataFetchWorker
import com.olam.warehouse.master.common.data.work.TrackTraceFarmerDataWorker
import com.olam.warehouse.master.common.data.work.TrackTrackSourceLotWorker
import com.olam.warehouse.master.common.data.work.TrackTrackTransactionIdWorker
import com.olam.warehouse.master.common.model.MessageModel
import com.olam.warehouse.master.common.model.NotificationModel
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentUserName
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaFeatureMaster
import com.olam.warehouse.master.work.getSessionRequestWorker
import com.olam.warehouse.presentation.data.work.MasterDataFetchScheduleWorker
import com.olam.warehouse.presentation.databinding.*
import com.olam.warehouse.presentation.ui.widget.CustomProgressBar
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.canSyncMaster
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.Constants.CONNECTIVITY_CHANGE
import com.olam.warehouse.presentation.utils.Constants.CONNECTIVITY_TYPE
import com.olam.warehouse.presentation.utils.Constants.IS_NETWORK_AVAILABLE
import com.olam.warehouse.presentation.utils.Constants.SESSION_EXPIRED
import com.olam.warehouse.presentation.utils.DateUtils.setLastSyncTime
import com.olam.warehouse.presentation.utils.UIUtils.getDeviceBatteryLevel
import com.olam.warehouse.presentation.utils.UIUtils.showErrorDialog
import com.olam.warehouse.presentation.utils.extension.*
import java.util.concurrent.TimeUnit

/**
 * Created by SangiliPandian C on 15-11-2019.
 */
abstract class BaseActivity : AppCompatActivity() {

    protected var currentView: View? = null
    private var lastViewId: Int? = null
    private val progressBar = CustomProgressBar()
    private lateinit var binding: ActivityBaseContentBinding
    private lateinit var errorBinding: ErrorContentBinding
    private val REQUEST_CODE_ASK_PERMISSIONS = 1002

    @get:LayoutRes
    protected abstract val layoutResourceId: Int

    private var callBack: PushObserve? = null

    interface PushObserve {
        fun clickObserve()
        fun logoutObserve()
    }


    companion object {
        const val ALL_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.READ_MEDIA_IMAGES)
                    add(Manifest.permission.READ_MEDIA_VIDEO)
                    add(Manifest.permission.READ_MEDIA_AUDIO)
                    add(Manifest.permission.POST_NOTIFICATIONS)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }.toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseContentBinding.inflate(layoutInflater)
        errorBinding = ErrorContentBinding.inflate(layoutInflater)
        WorkManager.getInstance(applicationContext).cancelUniqueWork(UNIQUE_ONE_TIME_WORKER)
        WorkManager.getInstance(applicationContext).cancelUniqueWork(UNIQUE_ONE_TIME_WORKER_TRANS)
        init(layoutResourceId)

    }
    fun isLogin() {
        if (PreferenceHelper.get(Constants.IS_LOGIN, false)) {
            userNoteDialog(this,resources.getStringArray(user_info) )
            PreferenceHelper.save(Constants.IS_LOGIN, false)
        }/*else{
            showResetPasswordDialog()
        }*/
    }

    override fun attachBaseContext(context: Context?) {
        super.attachBaseContext(context?.let { LocaleHelper.onAttach(it) })
        context?.let { SplitCompat.install(it) }
        //context?.let { SplitInstall.init(it) }
    }

    override fun onResume() {
        super.onResume()
        registerNetworkListener()
        registerNotificationListener()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkChangeReceiver)
        unregisterReceiver(networkTypeReceiver)
        unregisterReceiver(notificationReceiver)
        unregisterReceiver(sessionExpiredReceiver)
    }

    private fun init(layoutId: Int) {
        setContentView(binding.root)
        binding.vsContent.layoutResource = layoutId
        currentView = binding.vsContent.inflate()
        currentView?.let { loadView(it) }
    }

    private fun loadView(currentView: View) {
        lastViewId?.let { findViewById<View>(it)?.gone() }
        lastViewId = currentView.id
        currentView.visible()
    }

    //fun hideLoading() = binding.progressbar.gone()

    //fun showLoading() = binding.progressbar.visible()

    fun isLoading() = binding.progressbar.isShown

    fun showLoading() {
        try {
            when {
                progressBar.dialog == null -> progressBar.showLoading(this)
                !progressBar.dialog!!.isShowing -> progressBar.showLoading(this)
            }
        } catch (e: Exception) {
            when {
                progressBar.dialog != null -> progressBar.dialog?.dismiss()
            }
        }
    }

    fun hideLoading() = progressBar.dialog?.dismiss()

    fun showCustomLoading(text: String = getString(R.string.loading_text)) {
        when {
            progressBar.dialog == null -> progressBar.show(this, text)
            !progressBar.dialog!!.isShowing -> progressBar.show(this, text)
        }
    }

    fun hideCustomLoading() = progressBar.dialog?.dismiss()

    protected fun setContentViewVisibility(visibility: Boolean) {
        if (visibility) currentView?.visible() else currentView?.gone()
    }

    protected fun setErrorContentView(errorMsg: String) {
        loadErrorView(R.layout.error_content)
        errorBinding.tvErrorMsg.text = errorMsg
        val toolBar = findViewById<Toolbar>(com.olam.warehouse.presentation.R.id.tool_bar)
        initToolbar(true, toolBar, object : PushObserve {
            override fun clickObserve() {}
            override fun logoutObserve() {}
        })
    }

    private fun loadErrorView(layoutId: Int) {
        setContentView(errorBinding.root)
        binding.vsError.layoutResource = layoutId
        currentView?.gone()
        currentView = binding.vsError.inflate()
        currentView?.let { loadView(it) }
    }

    protected fun initToolbar(b: Boolean, toolBar: Toolbar, listener: PushObserve) {
        try {
            callBack = listener
            setEntityLevelVarient(toolBar)
            updateToolbarNotification(toolBar)
            val icon =
                if (b) com.olam.warehouse.presentation.R.drawable.ic_backspace_black_24dp else
                    com.olam.warehouse.presentation.R.drawable.ic_menu_corner_black_24dp
            setSupportActionBar(toolBar)
            val actionbar: ActionBar? = supportActionBar
            actionbar?.apply {
                setHomeAsUpIndicator(icon)
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowTitleEnabled(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setEntityLevelVarient(toolBar: Toolbar) {
        when {
            PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "").contains(Constants.OFI) -> {
//                val logo = toolBar.findViewById<ImageView>(R.id.ivLogo)
                val logo = findViewById<AppCompatImageView>(R.id.ivLogo)
//                logo.setPadding(0, 24, 0, 0)
                logo.setImageDrawable(logo.context.getDrawable(com.olam.warehouse.presentation.R.drawable.ic_olam_logo))
                /*logo.imageTintList = ContextCompat.getColorStateList(
                    logo.context,
                    com.olam.warehouse.presentation.R.color.black
                )*/
                /* ViewCompat.setBackground(
                     toolBar,
                     ContextCompat.getDrawable(
                         toolBar.context,
                         com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                     )
                 )*/
            }
            else -> {
//                val logo = toolBar.findViewById<ImageView>(R.id.ivLogo)
                val logo = findViewById<AppCompatImageView>(R.id.ivLogo)
//                logo.setPadding(0, 24, 0, 0)
                logo.setImageDrawable(logo.context.getDrawable(com.olam.warehouse.presentation.R.drawable.ic_olam_logo))
            }
        }
    }

    private fun updateToolbarNotification(toolBar: Toolbar) {
        val oldNotifiList = PreferenceHelper.get(Constants.NOTIFICATION_LIST, "")
        val notifiList = arrayListOf<NotificationModel>()
        if (oldNotifiList.isNotEmpty()) {
            val item = Gson().fromJson<List<NotificationModel>>(oldNotifiList)
            item.forEach {
                val bodyData = Gson().fromJson<MessageModel>(it.notification)
                val splitItem = bodyData.flag?.split(",")
                when {
                    splitItem?.any { it.equals(getCurrentKey(), true)} == true && getCurrentKey().isNotEmpty() -> notifiList.add(
                        it
                    )
                    splitItem?.any { it.equals(getPlantDetails().plantId, true)} == true  && getPlantDetails().plantId.isNotEmpty() -> notifiList.add(
                        it
                    )
                    splitItem?.any { it.equals(getCurrentUserName(), true)} == true && getCurrentUserName().isNotEmpty() -> notifiList.add(
                        it
                    )
                    splitItem?.any { it.equals(Constants.ALL, true)} == true-> notifiList.add(
                        it
                    )
                }
            }
            /*notifiList.addAll(
                Gson().fromJson<List<NotificationModel>>(
                    oldNotifiList
                )
            )*/
        }
        val unReadCount = notifiList.filter { !it.isViewed }
        if (notifiList.size > 0) {
            val notificationLayout = toolBar.findViewById<ConstraintLayout>(R.id.clNotification)
            notificationLayout.visible()
            val notificationCount = toolBar.findViewById<TextView>(R.id.tvNotificationCount)
            if (unReadCount.size == 0) notificationCount.gone() else notificationCount.visible()
            notificationCount.text =
                if (unReadCount.size > 9) unReadCount.size.toString() else "0".plus(unReadCount.size.toString())
            notificationLayout?.setOnClickListener {
                callBack?.clickObserve()
                /* startActivity(
                     Intent(
                         this,
                         NotificationActivity::class.java
                     )
                 )*/
            }
        }

    }

    /////////////////// Master data fetch worker ////////////////////////////////
    private fun fetchMasterData() {
        showCustomLoading(getString(R.string.syncing))
        makeStatusNotification(getString(R.string.master_data_downloading), applicationContext)
        val worker = OneTimeWorkRequest.Builder(MasterDataFetchWorker::class.java)
            .setConstraints(constraintWithoutNetwork).build()
        enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_WORKER, this)
        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer { workInfo ->
                workInfo?.let { info ->
                    when {
                        info.state == WorkInfo.State.SUCCEEDED -> {
                            setLastSyncTime()
                            if(PreferenceHelper.get(Constants.DIRECT,"").isNotEmpty()){
                                startTTFarmerDataSync()
                            }
                            if(PreferenceHelper.get(Constants.SOURCE_LOT_SYNC,"").isNotEmpty()){
                                startTTSourceLotDataSync()
                            }
                            if(PreferenceHelper.get(Constants.TRANS_ID_SYNC,"").isNotEmpty()){
                                startTTTransIdDataSync()
                            }
                            toast(getString(R.string.master_data_download_success))
                            dismissNotification(applicationContext)
                            hideCustomLoading()
                            // Check Login Status
                            isLogin()
                        }
                        info.state == WorkInfo.State.FAILED -> {
                            workInfo.outputData.getString(MASTER_OUTPUT_DATA)?.let {  showErrorDialog(this, it) }
                            dismissNotification(applicationContext)
                            hideCustomLoading()
                            // Check Login Status
                            isLogin()
                        }
                        info.state == WorkInfo.State.CANCELLED -> {
                            workInfo.outputData.getString(MASTER_OUTPUT_DATA)?.let { showErrorDialog(this, it) }
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

    protected fun startTTFarmerDataSync() {
        val isOnline = isNetworkAvailable()
        when {
            !isOnline -> Snackbar.make(
                window.decorView.rootView,
                getString(com.olam.warehouse.presentation.R.string.network_not_available),
                Snackbar.LENGTH_LONG
            ).show()
            else -> fetchTTFarmerData()
        }
    }

    private fun fetchTTFarmerData(){
        Log.e("farmerdata time start : ", "start" + System.currentTimeMillis().toString())
        showCustomLoading(getString(R.string.syncing))
        // makeStatusNotification(getString(R.string.farmer_data_downloading), applicationContext)
        val worker = OneTimeWorkRequest.Builder(TrackTraceFarmerDataWorker::class.java)
            .setConstraints(constraint).build()
        enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_FARMER_DATA_WORKER, this)
        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer{ workInfo ->
                workInfo?.let { info ->
                    when {
                        info.state == WorkInfo.State.SUCCEEDED -> {
                            Log.e("farmerdata time : finished : ", "end" + System.currentTimeMillis().toString())
                            toast(getString(R.string.farmer_data_downloaded_successfully))
                            dismissNotification(applicationContext)
                            hideCustomLoading()
                        }
                        info.state == WorkInfo.State.FAILED -> {
                            workInfo.outputData.getString(FARMER_OUTPUT_DATA)?.let { toast(it) }
                            dismissNotification(applicationContext)
                            toast(getString(R.string.farmer_data_failed))
                            hideCustomLoading()
                            isLogin()
                        }
                        info.state == WorkInfo.State.CANCELLED -> {
                            workInfo.outputData.getString(FARMER_OUTPUT_DATA)?.let { toast(it) }
                            dismissNotification(applicationContext)
                            toast(getString(R.string.farmer_data_cancelled))
                            hideCustomLoading()
                        }
                        else -> {
                            dismissNotification(applicationContext)
                        }
                    }
                }
            })
    }

    protected fun startTTSourceLotDataSync() {
        val isOnline = isNetworkAvailable()
        when {
            !isOnline -> Snackbar.make(
                window.decorView.rootView,
                getString(com.olam.warehouse.presentation.R.string.network_not_available),
                Snackbar.LENGTH_LONG
            ).show()
            else -> fetchSourceLotData()
        }
    }

    private fun fetchSourceLotData(){
        Log.e("sourcelotdata time start : ", "start" + System.currentTimeMillis().toString())
        showCustomLoading(getString(R.string.syncing))
        val worker = OneTimeWorkRequest.Builder(TrackTrackSourceLotWorker::class.java)
            .setConstraints(constraint).build()
        enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_SOURCE_LOT_WORKER, this)
        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer{ workInfo ->
                workInfo?.let { info ->
                    when {
                        info.state == WorkInfo.State.SUCCEEDED -> {
                            Log.e("sourcelotdata time : finished : ", "end" + System.currentTimeMillis().toString())
                            toast(getString(R.string.source_lot_downloaded_successfully))
                            dismissNotification(applicationContext)
                            hideCustomLoading()
                        }
                        info.state == WorkInfo.State.FAILED -> {
                            workInfo.outputData.getString(SOURCE_LOT_OUTPUT_DATA)?.let { toast(it) }
                            dismissNotification(applicationContext)
                            toast(getString(R.string.source_lot_data_downloaded_failed))
                            hideCustomLoading()
                            isLogin()
                        }
                        info.state == WorkInfo.State.CANCELLED -> {
                            workInfo.outputData.getString(SOURCE_LOT_OUTPUT_DATA)?.let { toast(it) }
                            dismissNotification(applicationContext)
                            toast(getString(R.string.source_lot_data_downloaded_cancelled))
                            hideCustomLoading()
                        }
                        else -> {
                            dismissNotification(applicationContext)
                        }
                    }
                }
            })
    }

    protected fun startTTTransIdDataSync() {
        val isOnline = isNetworkAvailable()
        when {
            !isOnline -> Snackbar.make(
                window.decorView.rootView,
                getString(com.olam.warehouse.presentation.R.string.network_not_available),
                Snackbar.LENGTH_LONG
            ).show()
            else -> fetchTransIdListData()
        }
    }

    private fun fetchTransIdListData(){
        Log.e("transIdData time start : ", "start" + System.currentTimeMillis().toString())
        showCustomLoading(getString(R.string.syncing))
        val worker = OneTimeWorkRequest.Builder(TrackTrackTransactionIdWorker::class.java)
            .setConstraints(constraint).build()
        enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_TRANS_ID_WORKER, this)
        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer{ workInfo ->
                workInfo?.let { info ->
                    when {
                        info.state == WorkInfo.State.SUCCEEDED -> {
                            Log.e("transIdData time : finished : ", "end" + System.currentTimeMillis().toString())
                            toast(getString(R.string.trans_id_data_downloaded_successfully))
                            dismissNotification(applicationContext)
                            hideCustomLoading()
                        }
                        info.state == WorkInfo.State.FAILED -> {
                            workInfo.outputData.getString(TRANS_ID_OUTPUT_DATA)?.let { toast(it) }
                            dismissNotification(applicationContext)
                            toast(getString(R.string.trans_id_data_downloaded_failed))
                            hideCustomLoading()
                            isLogin()
                        }
                        info.state == WorkInfo.State.CANCELLED -> {
                            workInfo.outputData.getString(TRANS_ID_OUTPUT_DATA)?.let { toast(it) }
                            dismissNotification(applicationContext)
                            toast(getString(R.string.trans_id_data_downloaded_cancelled))
                            hideCustomLoading()
                        }
                        else -> {
                            dismissNotification(applicationContext)
                        }
                    }
                }
            })
    }

    protected fun setAutoSyncMaster(isForceFetch: Boolean) {
        val isOnline = isNetworkAvailable()
        when {
            !isOnline && isOnline() -> Snackbar.make(
                window.decorView.rootView,
                getString(com.olam.warehouse.presentation.R.string.network_not_available),
                Snackbar.LENGTH_LONG
            ).show()
            /*currentView?.snack(
            getString(com.olam.warehouse.presentation.R.string.network_not_available),
            Snackbar.LENGTH_SHORT
        ) {}*/
            getDeviceBatteryLevel(this) <= 20->UIUtils.showErrorDialog(this, getString(R.string.battery_issue))
            isForceFetch || canSyncMaster() -> {
               fetchMasterData()
            }
        }
    }

    protected fun schedulePeriodicMasterWorker() {
        val worker =
            PeriodicWorkRequest.Builder(MasterDataFetchScheduleWorker::class.java, 1, TimeUnit.DAYS)
                .setConstraints(constraintWithoutNetwork).build()
        enQueueUniquePeriodicWorker(worker, UNIQUE_PERIODIC_WORKER, this)
        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                    setAutoSyncMaster(true)
                }
            })
    }

    /*Get Run Time Permission for Read Phone State*/
    fun getRunTimePermissionForReadPhoneState(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)

            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), 1)
            }
        }
        getRunTimePermissionForNotification()
    }

    /*Get Run Time Permission for Read Phone State*/
    fun getRunTimePermissionForNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS)

            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_ASK_PERMISSIONS)
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    toast("PERMISSION_STATE Denied")
                }
            }
            ALL_PERMISSIONS -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED })
                    toast("PERMISSION_STATE Granted")
                else
                    toast("PERMISSION_STATE Denied")
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /*Get multiple Run Time Permission for like Read Phone State, camera, media storage ect..*/
    fun setupAllPermissions(REQUIRED_PERMISSIONS: Array<String>) {
        if(!hasPermissions(this.applicationContext, *REQUIRED_PERMISSIONS)){
            ActivityCompat.requestPermissions(this,
                REQUIRED_PERMISSIONS, ALL_PERMISSIONS)
        }
    }

    fun hasPermissions(context: Context, vararg permissions: String): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    //////////////////////////////////////////////////////////////


    /*Register broadcast receiver */
    private fun registerNetworkListener() {
        val intentFilter = IntentFilter(CONNECTIVITY_CHANGE)
        val intentFilterNetworkType = IntentFilter(CONNECTIVITY_TYPE)
        val intentFilterSessionExpired = IntentFilter(SESSION_EXPIRED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(networkChangeReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
            registerReceiver(networkTypeReceiver, intentFilterNetworkType, RECEIVER_NOT_EXPORTED)
            registerReceiver(sessionExpiredReceiver, intentFilterSessionExpired, RECEIVER_NOT_EXPORTED)
        }else{
            registerReceiver(networkChangeReceiver, intentFilter)
            registerReceiver(networkTypeReceiver, intentFilterNetworkType)
            registerReceiver(sessionExpiredReceiver, intentFilterSessionExpired)
        }

    }

    /*Network change listener*/
    private val networkChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val isOnline = isNetworkAvailable()
            PreferenceHelper.save(IS_NETWORK_AVAILABLE, isOnline)
            if (!isOnline && isOnline()) {
                Snackbar.make(
                    window.decorView.rootView,
                    getString(com.olam.warehouse.presentation.R.string.network_not_available),
                    Snackbar.LENGTH_LONG
                ).show()
                /*currentView?.snack(
                    getString(com.olam.warehouse.presentation.R.string.network_not_available),
                    Snackbar.LENGTH_SHORT
                ) {}*/
            }
        }
    }

    //Network Type Listener
    val networkTypeReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            when (intent?.action) {
                CONNECTIVITY_TYPE -> {
                    when (networkType()) {
                        "Unknown" -> toast(getString(com.olam.warehouse.presentation.R.string.network_not_available))
                        else -> toast(
                            getString(com.olam.warehouse.presentation.R.string.network_type).plus(
                                networkType()
                            )
                        )
                    }
                }
            }
        }
    }

    //Session Expired Listener
    val sessionExpiredReceiver = object : BroadcastReceiver() {
        @SuppressLint("CheckResult")
        override fun onReceive(contxt: Context?, intent: Intent?) {
            when (intent?.action) {
                SESSION_EXPIRED -> {
                    contxt?.let { userSessionOut(it) }
                }
            }
        }
    }

    /*Register broadcast receiver */
    private fun registerNotificationListener() {
        val intentFilterNotification = IntentFilter(Constants.NOTIFICATION_RECEIVED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationReceiver, intentFilterNotification, RECEIVER_NOT_EXPORTED)
        }else{
            registerReceiver(notificationReceiver, intentFilterNotification)
        }

    }

    //Notification Listener
    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            when (intent?.action) {
                Constants.NOTIFICATION_RECEIVED -> {
                    try {
                        val toolBar = findViewById<Toolbar>(R.id.tool_bar)
                        if (toolBar != null) updateToolbarNotification(toolBar)
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        dismissNotification(applicationContext)
        super.onDestroy()
    }

    override fun onStop() {
        dismissNotification(applicationContext)
        super.onStop()
    }
    /* USer info dialog*/
    fun userNoteDialog(context: Context, stringArray: Array<String>) {
        val view = UserNoteBinding.inflate(layoutInflater)
        val alertBuilder = AlertDialog.Builder(this).setView(view.root)
        val alertDialog = alertBuilder.create()
        val modeAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray)
        view.listViewUserInfo.adapter = modeAdapter
        view.btOk.setOnClickListener {
            alertDialog.dismiss()
            //getRunTimePermissionForReadPhoneState()
            setupAllPermissions(REQUIRED_PERMISSIONS)
        }
        alertDialog.show()
    }

    /*User Session Timeout*/
    fun userSessionOut(context: Context) {
        val view = UserSessionOutBinding.inflate(layoutInflater)
        val alertBuilder = AlertDialog.Builder(this).setView(view.root)
        val alertDialog = alertBuilder.create()
        view.btOk.setOnClickListener {
            alertDialog.dismiss()
            val password = view.etPassword.text.toString()
            fetchToken(context, password, false)
        }
        view.tvLogutLogin.setOnClickListener {
            showConfirmDialog(alertDialog)
        }
        alertDialog.show()
    }

    private fun showConfirmDialog(alertDialog: AlertDialog) {
        MaterialDialog(this).show {
            title(R.string.session_out_offline_data)
            message(R.string.logout_alert)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(R.string.cancel),
                {
                    alertDialog.dismiss()
                    dismiss()
                },
                {
                    dismiss()
                })
        }
    }

    fun showResetPasswordDialog(){
        MaterialDialog(this).show {
            val view = ResetPasswordBinding.inflate(layoutInflater)
            this.setContentView(view.root)
            cancelOnTouchOutside(false)
            cancelable(false)
            this.window?.setGravity(Gravity.CENTER)
            view.btnReset.setOnClickListener {
                val newPass = view.etNewPassword.text.toString().trim()
                val newCfmPass = view.etConfirmPassword.text.toString().trim()
                when {
                    newPass.isEmpty() -> {
                        view.tilPassword.isErrorEnabled = false
                        view.tilPassword.error = "Enter valid password"
                        view.tilPassword.requestFocus()
                        view.btnReset.isClickable = true
                    }
                    newCfmPass.isEmpty() -> {
                        view.tilConfirmPassword.isErrorEnabled = false
                        view.tilConfirmPassword.error = "Enter valid password"
                        view.tilConfirmPassword.requestFocus()
                        view.tilConfirmPassword.isClickable = true
                    }
                    else -> {
                        if(!newPass.equals(newCfmPass)){
                            toast("password and confirm password should be same")
                        }else{
                            this.dismiss()
                            resetPassword(newCfmPass)
                        }
                    }
                }
            }

        }
    }

    private fun resetPassword(newCfmPass: String) {
        fetchToken(applicationContext, newCfmPass, true)
    }

    private fun fetchToken(context: Context, password: String, isPasswordReset: Boolean) {
         showCustomLoading(getString(com.olam.warehouse.presentation.R.string.processing))
        val input = workDataOf(Constants.PASS_WORD to password, Constants.RESET_PASSWORD to isPasswordReset)
        val worker = getSessionRequestWorker(input)
        enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_WORKER, context)
        WorkManager.getInstance(context).getWorkInfoByIdLiveData(worker.id)
            .observe(this
                , Observer { workInfo ->
                workInfo?.let { info ->
                    when {
                        info.state == WorkInfo.State.SUCCEEDED -> {
                            hideCustomLoading()
                            if(isPasswordReset){
                                callBack?.logoutObserve()
                            }
                        }
                        info.state == WorkInfo.State.FAILED -> {
                            hideCustomLoading()
                            workInfo.outputData.getString(SESSION_OUT_DATA)?.let { toast(it) }
                        }
                        else -> {
                        }
                    }
                }
            })
    }
}
