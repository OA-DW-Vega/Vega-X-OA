package com.olam.warehouse.vegax.portwarehouse.utils

import android.content.Context
import android.os.Build
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.utils.enums.ContainerStatus
import com.olam.warehouse.vegax.portwarehouse.utils.enums.SyncStatus
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by SangiliPandian C on 12-02-2019.
 */
object PortWHUtil {

    const val CONTAINER_ID = "container_id"
    const val OT_NUMBER = "ot_number"
    const val DISPATCH_TYPE = "dispach_type"
    const val NEW_CONTAINER = "new_container"
    const val ACTIVITY_REQUEST_CODE = 1
    const val ACTIVITY_SCAN_REQUEST_CODE = 2
    const val SEAL_SUCCESS = "seal_success"
    const val DISPATCH_SUCCESS = "dispatch_success"
    const val OFFLOAD_SUCCESS = "offload_success"
    const val OFFLINE_SUCCESS = "offline_success"
    const val SUCCESS = "success"
    const val MTN_ID = "mtn_id"
    const val BALE_ID = "bale_id"
    const val BALE_COUNT = "bale_count"
    const val BALE = "bale"
    const val PLANT = "plant"
    const val MTN = "mtn"
    const val GRADES = "grades"
    const val FILTER_GRADE = "filter_grade"
    const val FILTER_BALE = "filter_bale"
    const val FILTER_MARK = "filter_mark"
    const val FILTER_YEAR = "filter_year"
    const val FILTER_PILES = "filter_piles"
    const val INVENTORY_BALE = "inventory_bale"
    const val SELECTED_GRADE = "selected_grade"
    const val COTTON_PORT = "cotton-portWH"
    const val MATERIAL_DOC_ID = "material_doc_id"
    const val INCOMING_MTN = "incoming_mtn"
    const val MTN_OUTPUT_DATA = "mtn_work_data"
    const val BALE_STATUS = "bale_status"
    //Mtn Dispatch
    const val FRAG_OT_DISPATCH = "frag_ot_dispatch"
    const val FRAG_MTN_DISPATCH = "frag_mtn_dispatch"
    const val DELIVERY_NO = "delivery_no"
    const val SELECTED_GRADE_ITEM = 3
    const val SELECTED_BALE = "selected_bale"
    const val FRGA_DISPATCH_CONFIRM = "frag_dispatch_confirm"
    const val FRGA_DISPATCH_ADD_BALE = "frag_dispatch_add_bale"
    const val FRGA_DISPATCH_OFFLINE = "frag_dispatch_offline"
    const val TRANS_DATA = "trans_data"
    const val DATA_SYNC = "data_sync"
    const val BALE_DATA_SYNC = "bale_data_sync"
    const val DISPATCH_DATA = "dispatch_data"
    const val DISPATCH_OUTPUT_DATA = "dispatch_output_data"
    const val BALE_LIST = "BALE_LIST"
    const val TITLE = "title"
    const val SUB_TITLE = "sub_title"
    const val SELECTED_PILES = "selected_piles"
    const val PILE_LIST = "pile_list"
    const val MARK_LIST = "mark_list"
    const val CROP_YEARS = "crop_years"
    const val PILE_ID = "pile_id"
    const val CONFIRM_PILE = "pile_confirm"
    const val GRADE = "grade"
    var STORAGEID = "1001"
    //Date Formats
    const val SDF_DD_MM_YYYY = "dd/MM/yyyy"
    const val APPCENTER_SECRET_PROD = "82de3dd5-9d58-4d94-8d93-c1519ef05443"
    const val APPCENTER_SECRET_UAT = "ff75181c-43d5-4505-b47d-07f344c60d12"


    fun getContainerStatus(value: Int): String {
        val status = ContainerStatus.from(value)
        return when (status) {
            ContainerStatus.InProgress -> "Start Stuffing"
            ContainerStatus.OnHold -> "Resume Stuffing"
            ContainerStatus.Completed -> "View Bales"
            else -> "Status Unknown"
        }
    }

    fun canDeleteContainer(value: Int): Boolean {
        val status = ContainerStatus.from(value)
        return when (status) {
            ContainerStatus.InProgress -> false
            ContainerStatus.Resume -> false
            ContainerStatus.OnHold -> true
            ContainerStatus.Completed -> true
            else -> false
        }
    }

    fun getContainerStatusIcon(value: Int): Int {
        val status = ContainerStatus.from(value)
        return when (status) {
            ContainerStatus.Resume -> R.drawable.ic_dispatch_container_port
            ContainerStatus.InProgress -> R.drawable.ic_container_in_progress_port
            ContainerStatus.OnHold -> R.drawable.ic_container_pause
            ContainerStatus.Completed -> R.drawable.ic_container_completed_port
            else -> R.drawable.ic_dispatch_container_port
        }
    }

    fun getContainerBackgroundColor(value: Int): Int {
        val status = ContainerStatus.from(value)
        return when (status) {
            ContainerStatus.InProgress -> com.olam.warehouse.presentation.R.color.colorSecondaryOfi
            ContainerStatus.Resume -> com.olam.warehouse.presentation.R.color.colorSecondaryOfi
            ContainerStatus.OnHold -> R.color.dark_red
            ContainerStatus.Completed -> com.olam.warehouse.presentation.R.color.dark_colorPrimaryOfi
            else -> R.color.blue_light
        }
    }

    fun getBaleWeightWithUOM(weight:Double,uom:String):String {
        when(uom) {
           "KG" -> return "${weight.formatTwoDigits()} $uom"
            "MT" -> return (weight / 1000).formatTwoDigits().plus(" ").plus(uom)
            else -> return ""
        }
    }

    fun getFormattedDate(dateInMillis:Long, format:String):String {
        val sdf = SimpleDateFormat(format)
        val calendar = Calendar.getInstance()

        calendar.timeInMillis = dateInMillis
        val date = calendar.time
        return sdf.format(date)
    }

    fun isOnline(): Boolean {
        return PreferenceHelper.get(Constants.USER_ONLINE, true)
    }
    fun getStorageID(): String {
        return PreferenceHelper.get(Constants.STORAGEID, "1001")
    }

    fun getSyncStatusIcon(value: Int): Int {
        val status = SyncStatus.from(value)
        return when (status) {
            SyncStatus.NoProgress -> R.drawable.icon_no_progresss_port
            SyncStatus.InProgress -> R.drawable.ic_container_in_progress_port
            SyncStatus.OnError -> R.drawable.ic_icon_error_port
            SyncStatus.Completed -> R.drawable.ic_container_completed_port
            else -> R.drawable.icon_no_progresss_port
        }
    }

    fun getSyncItemBackgroundColor(value: Int): Int {
        return when (SyncStatus.from(value)) {
            SyncStatus.NoProgress -> R.color.blue_light
            SyncStatus.InProgress -> R.color.orange
            SyncStatus.OnError -> R.color.red
            SyncStatus.Completed -> R.color.green
            else -> R.color.blue_light
        }
    }

    fun getColor(id: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            App.getAppContext().resources.getColor(id, null)
        } else {
            App.getAppContext().resources.getColor(id)
        }
    }

    fun posExtension(tags: MutableSet<String>): Int {
        return if (tags.first().contains("com")) tags.last().toInt() else tags.first().toInt()
    }
    fun showErrorDialog(context: Context, msg: String) {
        if (MaterialDialog(context).isShowing) MaterialDialog(context).dismiss()
        MaterialDialog(context).show {
            title(com.olam.warehouse.presentation.R.string.error)
            message(null, msg)
            getMetirialCustomView(this, context.getString(R.string.ok), "", { dismiss() }, { })
        }
    }
}
