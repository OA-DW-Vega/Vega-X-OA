package com.olam.warehouse.master.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.splitcompat.SplitCompat
import com.olam.warehouse.master.R
import com.olam.warehouse.master.common.data.work.MasterDataFetchWorker
import com.olam.warehouse.presentation.data.work.MasterDataFetchScheduleWorker
import com.olam.warehouse.presentation.databinding.ActivityBaseContentBinding
import com.olam.warehouse.presentation.databinding.ErrorContentBinding
import com.olam.warehouse.presentation.ui.widget.CustomProgressBar
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.canSyncMaster
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.Constants.CONNECTIVITY_CHANGE
import com.olam.warehouse.presentation.utils.Constants.CONNECTIVITY_TYPE
import com.olam.warehouse.presentation.utils.Constants.IS_NETWORK_AVAILABLE
import com.olam.warehouse.presentation.utils.DateUtils.setLastSyncTime
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

    @get:LayoutRes
    protected abstract val layoutResourceId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseContentBinding.inflate(layoutInflater)
        errorBinding = ErrorContentBinding.inflate(layoutInflater)
        WorkManager.getInstance(applicationContext).cancelUniqueWork(UNIQUE_ONE_TIME_WORKER)
        WorkManager.getInstance(applicationContext).cancelUniqueWork(UNIQUE_ONE_TIME_WORKER_TRANS)
        init(layoutResourceId)
    }

    override fun attachBaseContext(context: Context?) {
        super.attachBaseContext(context?.let { LocaleHelper.onAttach(it) })
        context?.let { SplitCompat.install(it) }
        //context?.let { SplitInstall.init(it) }
    }

    override fun onResume() {
        super.onResume()
        registerNetworkListener()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkChangeReceiver)
        unregisterReceiver(networkTypeReceiver)
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
        initToolbar(true, toolBar)
    }

    private fun loadErrorView(layoutId: Int) {
        setContentView(errorBinding.root)
        binding.vsError.layoutResource = layoutId
        currentView?.gone()
        currentView = binding.vsError.inflate()
        currentView?.let { loadView(it) }
    }

    protected fun initToolbar(b: Boolean, toolBar: Toolbar) {
        setEntityLevelVarient(toolBar)
        val icon = if (b) com.olam.warehouse.presentation.R.drawable.ic_keyboard_backspace_black_24dp else
            com.olam.warehouse.presentation.R.drawable.ic_menu_black_24dp
        setSupportActionBar(toolBar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setHomeAsUpIndicator(icon)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
    }

    private fun setEntityLevelVarient(toolBar: Toolbar) {
        when {
            PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "").contains(Constants.OFI) -> {
                val logo = toolBar.findViewById<ImageView>(R.id.ivLogo)
//                logo.setPadding(0, 24, 0, 0)
                logo.setImageDrawable(logo.context.getDrawable(com.olam.warehouse.presentation.R.drawable.ic_olam_logo_ofi_new))
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
        }
    }

    /////////////////// Master data fetch worker ////////////////////////////////
    private fun fetchMasterData() {
        showCustomLoading(getString(R.string.syncing))
        makeStatusNotification(getString(R.string.master_data_downloading), applicationContext)
        val worker = OneTimeWorkRequest.Builder(MasterDataFetchWorker::class.java)
            .setConstraints(constraint).build()
        enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_WORKER, this)
        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer { workInfo ->
                workInfo?.let { info ->
                    when {
                        info.state == WorkInfo.State.SUCCEEDED -> {
                            setLastSyncTime()
                            toast(getString(R.string.master_data_download_success))
                            dismissNotification(applicationContext)
                            hideCustomLoading()
                        }
                        info.state == WorkInfo.State.FAILED -> {
                            workInfo.outputData.getString(MASTER_OUTPUT_DATA)?.let { toast(it) }
                            dismissNotification(applicationContext)
                            hideCustomLoading()
                        }
                        info.state == WorkInfo.State.CANCELLED -> {
                            workInfo.outputData.getString(MASTER_OUTPUT_DATA)?.let { toast(it) }
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

    //////////////////////////////////////////////////////////////


    /*Register broadcast receiver */
    private fun registerNetworkListener() {
        val intentFilter = IntentFilter(CONNECTIVITY_CHANGE)
        val intentFilterNetworkType = IntentFilter(CONNECTIVITY_TYPE)
        registerReceiver(networkChangeReceiver, intentFilter)
        registerReceiver(networkTypeReceiver, intentFilterNetworkType)
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

    override fun onDestroy() {
        dismissNotification(applicationContext)
        super.onDestroy()
    }

    override fun onStop() {
        dismissNotification(applicationContext)
        super.onStop()
    }
}
