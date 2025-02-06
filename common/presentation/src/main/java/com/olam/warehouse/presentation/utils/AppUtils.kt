package com.olam.warehouse.presentation.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import com.olam.warehouse.presentation.BuildConfig
import com.olam.warehouse.presentation.data.domain.model.DeviceInfo
import java.io.File
import kotlin.math.roundToInt

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
    const val TALLY_SHEETS_NEW = "tally_sheet_new"
    const val LOT_CARD = "lot_card"
    const val WH_RECEIPT = "wh_receipt"
    const val GRN_DOCUMENT = "grn_document"
    const val LOGOUT = "logout"
    const val MASTER_SYNC = "master_sync"
    const val INVENTORY_SYNC = "inventory_sync"
    const val VENDOR_SYNC = "vendor_sync"
    const val TALLY_SHEET = "tally_sheet"
    const val MSG = "message"
    var count: String = "0"
    const val QUALITY_WB_DETAILS = "QUALITY_WB_DETAILS"
    const val QUALITY_DETAILS = "QUALITY_DETAILS"
    const val IS_SECRET_ID_QR = "secret_id_qr_code"
    const val SECRET_ID = "secret_id"

    const val STOCK_RECON_CARD = "stock_recon_card"
    const val BAG_DETAILS = "bag_details"
    const val AUDIT_DETAILS = "audit_details"
    const val MATERIAL = "material"
    const val BAG_MGMT = "bag management"
    const val IVC_GRN_RECEIPT = "ivc grn receipt"
    const val IVC_OFFLOAD_RECEIPT = "ivc offload receipt"
    const val EUDR_STATUS = "eudr status"


    fun getVersionName(): String {
        return BuildConfig.VERSION_NAME
    }

    fun getVersionCode(): Int {
        return BuildConfig.VERSION_CODE.toInt()
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

    @SuppressLint("HardwareIds")
    fun getDeviceInfo(context: Context): DeviceInfo {
        val info = DeviceInfo()
        info.deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        info.osVersion = System.getProperty("os.version")
        info.releaseVersion = Build.VERSION.RELEASE.replace(" ", "-")
        info.device = Build.DEVICE.replace(" ", "-")
        info.deviceModel = Build.MODEL.replace(" ", "-")
        info.deviceBrand = Build.BRAND.replace(" ", "-")
        info.deviceDisplay = Build.DISPLAY.replace(" ", "-")
        info.deviceHardware = Build.HARDWARE.replace(" ", "-")
        info.deviceMnfr = Build.MANUFACTURER.replace(" ", "-")
        info.deviceSerial = Build.SERIAL.replace(" ", "-")
        info.deviceUser = Build.USER.replace(" ", "-")
        info.deviceHost = Build.HOST.replace(" ", "-")
        info.id = Build.ID.replace(" ", "-")
        info.deviceMemoryDetails = getRamSize(context)
        return info
    }

    /*to retrieve mobile ram size and storage size details*/
    fun getRamSize(context: Context): String {
        try {
            val actManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            var memoryInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memoryInfo)
            val availMemory = memoryInfo.availMem.toDouble() / (1024 * 1024 * 1024)
            val totalMemory = memoryInfo.totalMem.toDouble() / (1024 * 1024 * 1024)
            val iPath: File = Environment.getDataDirectory().absoluteFile
            val iStat = StatFs(iPath.path)
            val iBlockSize = iStat.blockSizeLong
            val iAvailableBlocks = iStat.availableBlocksLong
            val iTotalBlocks = iStat.blockCountLong
            val iAvailableSpace = (iAvailableBlocks * iBlockSize) / (1024 * 1024 * 1024)
            val iTotalSpace = (iTotalBlocks * iBlockSize) / (1024 * 1024 * 1024)
            return "Ram-".plus(availMemory.roundToInt().toString()).plus("-").plus(
                totalMemory.roundToInt().toString().plus("-Storage-")
                    .plus(iAvailableSpace.toString().plus("-").plus(iTotalSpace.toString()))
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

}
