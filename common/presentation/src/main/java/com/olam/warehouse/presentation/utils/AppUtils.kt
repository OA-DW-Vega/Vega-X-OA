package com.olam.warehouse.presentation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.olam.warehouse.presentation.BuildConfig

/**
 * Created by SangiliPandian C on 18-11-2019.
 */
object AppUtils {

    const val TITLE = "title"
    const val SUB_TITLE = "sub_title"
    const val FAILURE = "failure"
    const val PLANT_ID = "plant_id"
    const val SEASON_ID = "season_id"
    const val DEVICE_ID = "device_id"
    const val PRINT_ENABLE = "print"
    const val TALLY_SHEETS = "tally_sheet"
    const val LOT_CARD = "lot_card"
    const val WH_RECEIPT = "wh_receipt"
    const val GRN_DOCUMENT = "grn_document"
    const val LOGOUT = "logout"
    const val MASTER_SYNC = "master_sync"
    const val INVENTORY_SYNC = "inventory_sync"
    const val VENDOR_SYNC = "vendor_sync"
    const val TALLY_SHEET = "tally_sheet"
    var count: String = "0"

    fun getVersionName(): String {
        return BuildConfig.VERSION_NAME
    }

    fun getVersionCode(): Int {
        return BuildConfig.VERSION_CODE
    }

    fun getEnviroment(): String {
        return BuildConfig.BUILD_TYPE
    }

    fun canSyncMaster(): Boolean {
        return (DateUtils.getLastSyncTime() == 0L) || DateUtils.getCountOfDays() > 1
    }

    fun isOnline(): Boolean {
        return PreferenceHelper.get(Constants.USER_ONLINE, true)
    }

    fun posExtension(tags: MutableSet<String>): Int {
        return if (tags.first().contains("com")) tags.last().toInt() else tags.first().toInt()
    }

    @SuppressLint("HardwareIds")
    fun getDeviceID(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
