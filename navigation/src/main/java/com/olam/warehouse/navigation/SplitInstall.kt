package com.olam.warehouse.navigation

import android.content.Context
import android.util.Log
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitinstall.*
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

/**
 * Created by SangiliPandian C on 12-11-2019.
 */
object SplitInstall {

    const val TAG = "SplitInstallLog"

    fun download(c: Context, dynamicModule: String, installedAction: () -> Unit) {
        try {
            val splitInstallManager: SplitInstallManager = SplitInstallManagerFactory.create(c)
            val request = SplitInstallRequest.newBuilder()
                .addModule(dynamicModule)
                .build()
            if (splitInstallManager.installedModules.contains(dynamicModule)) {
                Log.d(TAG, "Already module installed ......")
            }
            val installStateUpdateListener = object : SplitInstallStateUpdatedListener {
                override fun onStateUpdate(state: SplitInstallSessionState) {
                    state.moduleNames().forEach { _ ->
                        when (state.status()) {
                            SplitInstallSessionStatus.DOWNLOADING -> {
                                Log.d(TAG, "Downloading ......")
                            }
                            SplitInstallSessionStatus.INSTALLED -> {
                                installedAction.invoke()
                                splitInstallManager.unregisterListener(this)
                                Log.d(TAG, "Installed  ......")
                            }
                            SplitInstallSessionStatus.INSTALLING -> {
                                Log.d(TAG, "Installing ......")
                            }
                            SplitInstallSessionStatus.FAILED -> {
                                splitInstallManager.unregisterListener(this)
                                Log.d(TAG, "Download failed")
                            }
                            SplitInstallSessionStatus.CANCELED -> {
                            }
                            SplitInstallSessionStatus.CANCELING -> {
                            }
                            SplitInstallSessionStatus.PENDING -> {
                            }
                            SplitInstallSessionStatus.DOWNLOADED -> {
                            }
                            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                            }
                            SplitInstallSessionStatus.UNKNOWN -> {
                            }
                        }
                    }
                }
            }
            splitInstallManager.registerListener(installStateUpdateListener)
            splitInstallManager.startInstall(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun init(context: Context) {
        SplitCompat.install(context)
    }
}
