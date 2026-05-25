package com.olam.warehouse.vegax

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
//import com.dynatrace.android.agent.Dynatrace
//import com.dynatrace.android.agent.conf.DynatraceConfigurationBuilder
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.olam.warehouse.login.ui.LoginActivity
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.presentation.di.createNetworkModule
import com.olam.warehouse.presentation.di.initStetho
import com.olam.warehouse.presentation.ui.wifiprinter.ObservableSingleton
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.LocaleHelper
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.BuildConfig.DEBUG
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import timber.log.Timber


/**
 * Created by SangiliPandian C on 07-11-2019.
 */
class App : SplitCompatApplication() {

    companion object {
        lateinit var instance: App private set
        fun getAppContext(): Context = instance.applicationContext

        private var tracker: Tracker? = null
        var qualitycount: String? = "0"
        private var siteId: Int = 0
        private var mCurrentActivity: FragmentActivity? = null

        fun getCurrentActivity() = mCurrentActivity?: FragmentActivity()

        fun setCurrentActivity(activity: FragmentActivity){
            mCurrentActivity = activity
        }

        @Synchronized
        fun getTracker(): Tracker? {
            if (tracker == null) {
                if (AppUtils.getEnviroment().equals("release")) siteId = 41 else siteId = 40
                tracker = TrackerBuilder.createDefault("https://olammatomo.azurewebsites.net/matomo.php", siteId)
                    .build(Matomo.getInstance(getAppContext()))
            }
            return tracker
        }
    }

    override fun attachBaseContext(context: Context?) {
        super.attachBaseContext(context?.let { LocaleHelper.onAttach(it) })
        context?.let { SplitCompat.install(it) }
    }

    fun setqualitycount(count: String) {
        qualitycount = count
    }

    override fun onCreate() {
        super.onCreate()
        // Force to light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        instance = this
        initFabric() // Crashlytics
        initKoin()
        initTimber()
        initStetho(this)
        PreferenceHelper.init(this)
        initSingleton()
        //init In App Libirary
//        val permissionCheckerActs = arrayOf<Activity>(LoginActivity(), HomeActivity())
//        InApp.initiateWith(this, permissionCheckerActs).setPosition(Gravity.LEFT, -200)
//        InApp.identifiedUserWithEnvironment(PreferenceHelper.get(Constants.USER_NAME, ""), AppUtils.getEnviroment())

        val applicationId = when (AppUtils.getEnviroment()) {
            "release" -> Constants.applicationId_Prod
            "uat" -> Constants.applicationId_uat
            else -> Constants.applicationId_Prod
        }

        val beconUrl = when (AppUtils.getEnviroment()) {
            "release" -> Constants.beconUrl_prod
            "uat" -> Constants.beconUrl_uat
            else -> Constants.beconUrl_prod
        }
       /* Dynatrace.startup(
            this,
            DynatraceConfigurationBuilder(
                applicationId,
                beconUrl
            ).buildConfiguration()
        )
        Dynatrace.identifyUser(PreferenceHelper.get(Constants.USER_NAME, ""))*/
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.onAttach(this)
    }

    private fun initFabric() {
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
        /*val fabric = Fabric.Builder(this).kits(Crashlytics()).debuggable(true).build()
        Fabric.with(fabric)*/
        FirebaseMessaging.getInstance().subscribeToTopic("vega").addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                toast("failed")
            }

        }
    }

    private fun initKoin() {
        // Only initialize Koin if not already started
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidLogger(Level.ERROR)
                androidContext(this@App)
                modules(
                    listOf(
                        createNetworkModule(this@App)
                    )
                )
                allowOverride(true)
            }
        }
    }

    private fun initTimber() {
        if (DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initSingleton() {
        ObservableSingleton.initInstance()
    }

}
