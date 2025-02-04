package com.olam.warehouse.login.services

import android.app.Service
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log

/**
 * Created by Baskaran Kannan on 7/29/2020.
 */
class APKInstallService : Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                Log.d("AppLog", "Requesting user confirmation for installation")
                val confirmationIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                } else {*/
                confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                }
                try {
                    startActivity(confirmationIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            PackageInstaller.STATUS_SUCCESS -> Log.d("AppLog", "Installation succeed")
            else -> Log.d("AppLog", "Installation failed")
        }
        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent) = null
}
