package com.olam.warehouse.login.ui.appcenterdialog

import android.app.Activity
import com.afollestad.materialdialogs.MaterialDialog
import com.microsoft.appcenter.distribute.Distribute
import com.microsoft.appcenter.distribute.DistributeListener
import com.microsoft.appcenter.distribute.ReleaseDetails
import com.microsoft.appcenter.distribute.UpdateAction
import com.olam.warehouse.login.R
import com.olam.warehouse.login.ui.appcenterdialog.Enviroment.Companion.valueOfEnum
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils

/**
 * Created by Baskaran Kannan on 3/6/2020.
 */

class AppDistributeListener : DistributeListener {
    override fun onReleaseAvailable(activity: Activity?, releaseDetails: ReleaseDetails?): Boolean {
        val versionName = releaseDetails?.shortVersion
        val versionCode = releaseDetails?.version
        val releaseNotes = releaseDetails?.releaseNotes
        val groupId = releaseDetails?.distributionGroupId
        val releaseNotesUrl = releaseDetails?.releaseNotesUrl
        activity?.let {
            MaterialDialog(it).show {
                val msg =
                    "New Version $versionName available! for ${getEnviroment(AppUtils.getEnviroment())} environment"
                title(text = msg)
                message(text = releaseNotes)
                cancelOnTouchOutside(false)
                cancelable(false)
                positiveButton(text = UIUtils.getSpannedText(context.getString(R.string.update), true)) {
                    Distribute.notifyUpdateAction(UpdateAction.UPDATE)
                }
                /*negativeButton(text = UIUtils.getSpannedText(context.getString(R.string.postpone), true)) {
                    Distribute.notifyUpdateAction(UpdateAction.POSTPONE)
                }*/
            }
        }
        return true
    }

    fun getEnviroment(enviroment: String): String {
        return when (valueOfEnum(enviroment)) {
            Enviroment.DEV -> "Dev"
            Enviroment.UAT -> "Uat"
            Enviroment.SIT -> "Sit"
            Enviroment.PROD -> "Prod"
            else -> "Dev"
        }

    }
}

enum class Enviroment(val enviroment: String) {
    DEV("debug"),
    UAT("uat"),
    SIT("sit"),
    PROD("release");

    companion object {
        fun valueOfEnum(value: String): Enviroment? = values().find { it.enviroment == value }
    }
}
