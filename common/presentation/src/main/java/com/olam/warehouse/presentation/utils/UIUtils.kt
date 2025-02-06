package com.olam.warehouse.presentation.utils

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.olam.warehouse.presentation.R
import com.olam.warehouse.presentation.databinding.DialogPostingDateSelectionBinding
import com.olam.warehouse.presentation.enums.SyncStatus
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import org.json.JSONObject
import permissions.dispatcher.PermissionRequest
import java.util.*
import kotlin.random.Random

/**
 * Created by SangiliPandian C on 16-11-2019.
 */
object UIUtils {

    const val RECEIVING_DATA = "receiving_data"
    const val RECEIVING_OUTPUT_DATA = "receiving_out_data"
    const val RECEIVING_POST_DATA = "receiving_intent_post_data"
    const val QUALITY_DATA = "quality_data"
    const val PRICE_DATA = "price_data"
    const val GRN_DATA = "grn_data"
    const val MATERIAL = "material_name"
    const val QR_VALUE = "qr_value"
    const val INVENTORY_BALE = "inventory_bale"
    const val LOT_DETAIL = "lot_details"
    const val GRN_LIST = "grn_list"
    const val GRN_OUTPUT_DATA = "grn_work_data"
    const val INVOICE_DATA = "invoice_data"
    const val INVOICE_OUTPUT_DATA = "invoice_work_data"
    const val QUALITY_OUTPUT_DATA = "quality_work_data"
    const val MTNT_DATA = "mtnt_data"
    const val MTNT_POST_DATA = "mtnt_intent_post_data"
    const val MTNT_OUTPUT_DATA = "mtnt_out_data"
    const val DISPATCH_BATCH = "dispatch_batch"
    const val DISPATCH_REMARK = "dispatch_remark"
    const val DISPATCH_DATA = "dispatch_data"
    const val DISPATCH_OUTPUT_DATA = "dispatch_out_data"
    const val DISPATCH_POST_DATA = "dispatch_intent_post_data"
    const val RMIN_DATA = "rmin_data"
    const val RMIN_POST_DATA = "rmin_post_data"
    const val FGRN_DATA = "fgrn_data"
    const val FGRN_STAGE = "fgrn_stage"
    const val RELEASE_URL_PATH = "release_url_path"
    const val RELEASE_ID = "release_id"
    const val RELEASE_OUTPUT = "release_output"
    const val RELEASE_URL_OUTPUT = "release_url_output"
    const val RELEASE_VERSION = "release_version"
    const val RELEASE_NOTES = "release_notes"
    const val MTNR = "mtnr"
    const val TRANS_OFFLOADING = "trans_offloading"
    const val TRANS_QUALITY = "trans_quality"
    const val TRANS_GRN = "trans_grn"
    const val TRANS_INVOICE = "trans_invoice"
    const val TRANS_ADVANCE_CREATION = "trans_advance_creation"
    const val TRANS_FORWARD_PO = "trans_forward_po"
    const val TRANS_MTNT = "trans_mtnt"
    const val TRANS_SALES = "trans_sales"
    const val TRANS_MTNR = "trans_mtnr"
    const val REPRINT_GRN = "reprint_grn"
    const val REPRINT_INVOICE = "reprint_invoice"
    const val REPRINT_WITH_HOLD_TAX = "reprint_with_hold_tax"
    const val REPRINT_CERTIFICATE_PREMIUM = "reprint_certificate_premium"
    const val REPRINT_MTNT = "reprint_mtnt"
    const val REPRINT_FORWARDPO = "reprint_forwardpo"
    const val REPRINT = "reprint"
    const val REPRINT_TICKET = "reprint_ticket"
    const val REPRINT_SAMPLE_TICKET = "reprint_sample_ticket"
    const val REPRINT_MTNR = "reprint_mtnr"
    const val PRINT_FGRN_TALLYSHEET = "print_fgrn_tallysheet"
    const val COCOA_MTNT = "virtualMtnt"
    const val COCOA_MTNR = "virtual_Mtnr"
    const val REPRINT_QA_RECEIPT = "reprint_quality_approval_receipt"
    const val REPRINT_QA_TICKET = "reprint_quality_approval_ticket"
    const val REPRINT_OFFLOADING_RECEIPT = "reprint_offloading_receipt"
    const val REPRINT_OFFLOADING_TICKET = "reprint_offloading_ticket"
    const val TYPE_R = "Receipt"
    const val TYPE_T = "Ticket"
    var BATCH_NO = "batch_no"
    const val PRINT_TYPE = "print_type"
    const val PRINT_FGRN_REQ = "print_fgrn_req"
    const val PRINT_PPQ_REQ = "print_ppq_req"

    const val YIELD_DATA = "yield_data"
    const val FORWARD_PO_DATA = "forward_po_data"
    const val FORWARD_PO_OUTPUT_DATA = "forward_po_output_data"
    const val ADVANCE_DATA = "advance_data"
    const val ADVANCE_OUTPUT_DATA = "advance_output_data"
    const val INVOICE_PRICE_INFO = "invoice_price_info"
    const val VENDOR_DATA = "data"
    const val TEMP_ID = "temp_id"
    const val WB_ID = "wb_id"
    const val DEVICE_ID = "device_id"
    const val COPY = "copy"
    const val EDIT = "edit"
    const val DIRECTIONIN = "IN"
    const val DIRECTIONOUT = "OUT"
    const val FROM_NICARAGUA_COFFEE = "from_nicaragua_coffee"
    const val FROM_NICARAGUA_UPCOUNTRY = "from_nicaragua_country"
    const val FROM_CAMEROON_GATEENTRY_COCOA = "from_cameroon_gate_entry_cocoa"
    const val FROM_CAMEROON_COCOA_ADD_CONTAINER = "from_cameroon_cocoa_add_container"
    const val FROM_CAMEROON_COCOA_QA = "from_cameroon_cocoa_quality_approve"
    const val FROM_NIGERIA_COCOA_QA = "from_nigeria_cocoa_quality_approve"
    const val FROM_NIGERIA_COCOA_BAG_MGMT = "from_nigeria_cocoa_bag_mgmt"
    const val FROM_NIGERIA_COCOA_LOT_QUALITY = "from_nigeria_cocoa_lot_quality"
    const val FROM_CAMEROON_COCOA_OFFLOADING = "from_cameroon_cocoa_offloading"
    const val FROM_CAMEROON_COCOA_FGRN = "from_cameroon_cocoa_fgrn"
    const val NICARAGUA_PRINT_TYPE = "nicaragua_print_type"
    const val NICARAGUA_PRINT_INVOICE_PTBF = "nicaragua_print_invoice_ptbf"
    const val NICARAGUA_PRINT_GRN_RECEIPT = "nicaragua_print_grn_receipt"
    const val NICARAGUA_PRINT_MTNT_RECEIPT = "nicaragua_print_mtnt_receipt"
    const val NICARAGUA_MTNT_TRUCK_NO = "nicaragua_mtnt_truck_no"
    const val NICARAGUA_MTNT_OBD_NUMBER = "nicaragua_mtnt_obd_number"
    const val NICARAGUA_MTNT_SCAN_OBD_NUMBER = "nicaragua_mtnt_scan_obd_number"
    const val NICARAGUA_PRINT_FORWARDPO_RECEIPT = "nicaragua_print_FORWARDPO_receipt"
    const val NICARAGUA_PRINT_TALLYSHEET = "nicaragua_print_tallysheet"
    const val BAGS_DATA = "bags_data"
    const val ADVANCE_KNOCK_DATA = "advance_knock_data"
    const val PRINT_LOCAL_SALES_TALLY_SHEET = "print_local_sales_tally_sheet"
    const val LOT_DETAILS_LOCAL_SALES = "lot_details_tally_sheet"
    const val ANY_LOT_QUALITY_PRINT = "any_lot_quality_print"
    const val ANY_LOT_QUALITY_LIST = "any_lot_quality_list"

    const val FROM_TRANSACTION = "from_transaction"
    const val FROM_GHANA = "from_ghana"

    const val NICERTFD_SBUX = "NICERTFD SBUX"
    const val NICERTFD_UTZ = "NICERTFD UTZ"
    const val NICERTFD_RFA = "NICERTFD RFA"

    const val MTNT_VIRTUAL_DATA = "no_weighment_data"
    const val MTNT_VIRTUAL_OUTPUT_DATA = "grn_work_data"

    const val REQUEST_ENABLE_BT = 123
    const val BT_MAC = "STORED_BT_MAC"
    const val BT_UUID = "bt_uuid"

    const val EXTRA_SET_PIN = "set_pin"
    const val EXTRA_FONT_TEXT = "textFont"
    const val EXTRA_FONT_NUM = "numFont"
    const val BALE_STATUS = "bale_status"
    const val INCOMING_MTN = "incoming_mtn"
    const val SEAL_ID = "seal_id"
    const val NETWEIGHT = "NetWeight"
    const val FILTEREDPRICEDETAILS = "FilteredPriceDetails"
    const val STORAGELOSS = "Storageloss"
    const val FROM_NIC_MTNR_GRN = "from_nic_mtnr_grn"
    const val PRINT_TALLY_SHEET = "print_tally_sheet"
    const val PRINT_TICKET = "print_ticket"

    const val TYPE = "type"
    const val RMIN_VERSION = "version"
    const val RMIN_OUTPUTMATERIALCODE = "outputmaterialcode"
    const val WAREHOUSE_NUMBER = "warehousenumber"

    const val DELIVERYID = "delivery_id"
    const val BATCHID = "batch_id"
    const val LOTS = "lots"
    const val ISSPLIT_LOT = "issplit_lot"
    const val SPLIT_LOTS = "split_lots"
    const val MERGED = "merged"
    const val PILE_PRINT_DETAILS = "pile_print_details"
    const val HISTORY_DATA_GRN = "history_intent_data_grn"
    const val HISTORY_DATA_RMIN = "history_intent_data_rmin"
    const val HISTORY_DATA_FGRN = "history_intent_data_fgrn"
    const val HISTORY_DATA_MTNT = "history_intent_data_mtnt"
    const val HISTORY_DATA_MTNR = "history_intent_data_mtnr"

    fun showLoading(context: Context): MaterialDialog? {
        return MaterialDialog(context).show {
            cancelable(false)
            message(R.string.loading)
        }
    }

    fun showRationaleDialog(
        context: Context,
        @StringRes messageResId: Int,
        request: PermissionRequest
    ) {
        MaterialDialog(context).show {
            cancelable(false)
            positiveButton(R.string.allow) { request.proceed() }
            negativeButton(R.string.deny) { request.cancel() }
            message(messageResId)
        }
    }

    fun showErrorDialog(context: Context, msg: String) {
        if (MaterialDialog(context).isShowing) MaterialDialog(context).dismiss()
        MaterialDialog(context).show {
            cancelOnTouchOutside(false)
            title(R.string.error)
            if(msg.contains("Unable to resolve host"))
                message(null, context.getString(R.string.network_not_available))
            else
                message(null, msg)
            positiveButton(text = getSpannedText(context.getString(R.string.ok), true))
        }
    }

    fun showSuccessDialog(context: Context, msg: String) {
        if (MaterialDialog(context).isShowing) MaterialDialog(context).dismiss()
        MaterialDialog(context).show {
            message(null, msg)
            positiveButton(text = getSpannedText(context.getString(R.string.ok), true))
        }
    }

    fun showPendingAlertDialog(context: Context, msg: String) {
        MaterialDialog(context).show {
            title(R.string.alert)
            message(null, msg)
            positiveButton(text = getSpannedText(context.getString(R.string.ok), true))
        }
    }

    fun getSpannedText(text: String, isPositive: Boolean = false): Spanned {
        val color = if (isPositive) "#0B4B52" else "#CB410B"
        val textFormat = "<font color='$color'>$text</font>"
        return HtmlCompat.fromHtml(textFormat, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    fun getSpannedTextForSync(text: String): Spanned {
        val color = "#a0c800"
        val textFormat = "<font color='$color'>$text</font>"
        return HtmlCompat.fromHtml(textFormat, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    fun getSyncStatusIcon(value: Int): Int {
        val status = SyncStatus.from(value)
        return when (status) {
            SyncStatus.NoProgress -> R.drawable.icon_no_progresss
            SyncStatus.InProgress -> R.drawable.do_icon_in_progress
            SyncStatus.OnError -> R.drawable.ic_do_icon_error
            SyncStatus.Completed -> R.drawable.do_icon_completed
            else -> R.drawable.icon_no_progresss
        }
    }

    fun getSyncItemBackgroundColor(value: Int): Int {
        return when (SyncStatus.from(value)) {
            SyncStatus.NoProgress -> R.color.blue_light
            SyncStatus.InProgress -> R.color.orange
            SyncStatus.OnError -> R.color.red1
            SyncStatus.Pending -> R.color.pink_light
            SyncStatus.Completed -> R.color.green
            else -> R.color.blue_light
        }
    }

    fun getWarehouseId() = PreferenceHelper.get(Constants.WAREHOUSE_ID, "")
    fun showPostingDateDialog(
        context: Context, count: Int,
        listener: DialogClick
    ) {
        val inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = DialogPostingDateSelectionBinding.inflate(inflator)
        //LayoutInflater.from(context).inflate(R.layout.dialog_posting_date_selection, null)
        view.enterPostingDate.setOnClickListener(View.OnClickListener {
            setupUi(context, view.enterPostingDate)
        })
        val alertBuilder = AlertDialog.Builder(context).setView(view.root)
        val alertDialog = alertBuilder.create()
        view.header.text =
            " Note: It is the first " + count + " Working days of the month. Please Select Post Date"
        view.btConform.setOnClickListener {
            listener.onPositive(view.enterPostingDate.text.toString())
            alertDialog.dismiss()
        }
        view.btCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    interface DialogClick {
        fun onPositive(remark: String)
    }

    private fun setupUi(context: Context, editText: TextView) {
        // Get calendar instance
        val calendar = Calendar.getInstance()

        // Get current time
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)


        // Create listener
        val listener = DatePickerDialog.OnDateSetListener { view, year, month, day ->
            // Show Toast after selection
            val calendar = Calendar.getInstance()
            calendar[year, month] = day

            val date = DateUtils.getDate(calendar.timeInMillis, "dd-MMM-yyyy")
            editText.text = date
        }

        // Max = current
        val maxTime = calendar.timeInMillis

        // Move day as first day of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        // Move "month" for previous one
        calendar.add(Calendar.MONTH, -1)

        // Min = time after changes
        val minTime = calendar.timeInMillis

        // Create dialog
        val datePickerDialog = DatePickerDialog(
            context,
            listener,
            currentYear,
            currentMonth,
            currentDay
        ).apply {
            // Set dates
            datePicker.maxDate = maxTime
            datePicker.minDate = minTime
        }

        // Show dialog
        datePickerDialog.show()
    }

    fun String.mandatoryStars(): CharSequence? {
        val spannable = SpannableStringBuilder(this)
        spannable.insert(spannable.length, "*")
        spannable.setSpan(
            ForegroundColorSpan(Color.RED),
            this.length,
            this.length + 1,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        return spannable
    }

    fun appcenterPosExtension(tags: MutableSet<String>): String {
        return if (tags.first().contains("com")) tags.last().toString() else tags.first().toString()
    }

    fun getActionBtnChangedView(view: View, context: Context, isBtn: Boolean) {
        when (isBtn) {
            true -> when {
                PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "").contains(Constants.OFI) -> {
                    ViewCompat.setBackgroundTintList(
                        view,
                        context.let {
                            ContextCompat.getColorStateList(
                                it, R.color.colorPrimaryOfi
                            )
                        }
                    )
                }
                PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "").contains(Constants.OGA) -> {
                    ViewCompat.setBackgroundTintList(
                        view,
                        context.let {
                            ContextCompat.getColorStateList(
                                it, R.color.colorPrimaryOfi
                            )
                        }
                    )
                }
            }
            else -> when {
                PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "").contains(Constants.OFI) -> {
                    ViewCompat.setBackgroundTintList(
                        view,
                        context.let {
                            ContextCompat.getColorStateList(
                                it, R.color.colorPrimaryHeadOfi
                            )
                        }
                    )
                }
                PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "").contains(Constants.OGA) -> {
                    ViewCompat.setBackgroundTintList(
                        view,
                        context.let {
                            ContextCompat.getColorStateList(
                                it, R.color.colorPrimaryHeadOfi
                            )
                        }
                    )
                }
            }
        }
    }

    fun getMetirialCustomView(
        materialDialog: MaterialDialog,
        positive: String,
        negative: String,
        posclickAction: () -> Unit,
        negaclickAction: () -> Unit
    ): MaterialDialog {
        val cusView = materialDialog.customView(R.layout.custom_material_layout)
        val confirm = cusView.findViewById<TextView>(R.id.btnConfirm)
        val cancel = cusView.findViewById<TextView>(R.id.btnCancel)
        confirm.text = positive
        cancel.text = negative
        if (positive.isEmpty()) confirm.gone() else confirm.visible()
        if (negative.isEmpty()) cancel.gone() else cancel.visible()
        val font = ResourcesCompat.getFont(materialDialog.view.context, R.font.gilroy_semibold)
        val message = materialDialog.view.findViewById<TextView>(android.R.id.message)
       /* try {
            message.typeface = font
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }*/
        materialDialog.view.background =
            ContextCompat.getDrawable(materialDialog.view.context, R.color.warm_white)
        confirm.setOnClickListener {
            posclickAction.invoke()
            materialDialog.dismiss()
        }
        cancel.setOnClickListener {
            PreferenceHelper.save(Constants.IS_LOGIN, false)
            negaclickAction.invoke()
        }
        if (!PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "").contains("OFI")) {
            confirm.setTextColor(ContextCompat.getColor(confirm.context, R.color.white))
            cancel.setTextColor(ContextCompat.getColor(confirm.context, R.color.text_black))
            ViewCompat.setBackgroundTintList(
                confirm,
                ContextCompat.getColorStateList(materialDialog.context, R.color.colorPrimaryOfi)
            )
            ViewCompat.setBackground(
                cancel,
                ContextCompat.getDrawable(cancel.context, R.drawable.rectangle_black_line_border)
            )
        }
        return materialDialog
    }

    fun setOlamLogoDynamically(ivOlamLogo: ImageView) {
        when {
            PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "").contains(Constants.OFI) -> {
                ivOlamLogo.setImageDrawable(ivOlamLogo.context.getDrawable(R.drawable.ic_olam_logo))
            }
            else -> {
                ivOlamLogo.setImageDrawable(ivOlamLogo.context.getDrawable(R.drawable.ic_olam_logo))
            }
        }
    }

    fun getTenDigRandomId() = Random.nextInt().toString().takeLast(11)

    /*Get device battery level*/
    fun getDeviceBatteryLevel(context: Context) : Int{
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(null, ifilter, Context.RECEIVER_NOT_EXPORTED)
            }else{
                context.registerReceiver(null, ifilter)
            }
        }
        val batteryPct: Int = batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toInt()
        } ?: 100
        return batteryPct
        /*if (batteryPct <= 20) {

        }*/
    }

    fun decodeTokenGetKeyCloakId(): String {
        val jwt = PreferenceHelper.get(Constants.ACCESS_TOKEN, "")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "Requires SDK 26"
        val parts = jwt.split(".")
        return try {
            val charset = charset("UTF-8")
            val header = String(Base64.getUrlDecoder().decode(parts[0].toByteArray(charset)), charset)
            val payload = String(Base64.getUrlDecoder().decode(parts[1].toByteArray(charset)), charset)
            val keycloakId = JSONObject(payload).getString("sub")
            /*"$header"*/
            "$keycloakId/"
        } catch (e: Exception) {
            "Error parsing JWT: $e"
        }
    }

    fun getTTDialogOnlyForMaterial(
        materialDialog: MaterialDialog,
        positive: String,
        negative: String,
        posclickAction: (isComplaint: Boolean, isThirdParty:Boolean, isFarmerLessTransaction:Boolean) -> Unit,
        negaclickAction: (isNonComplaint: Boolean) -> Unit,
        procurementType:Boolean
    ): MaterialDialog {
        val cusView = materialDialog.customView(com.olam.warehouse.presentation.R.layout.custom_tt_layout)
        val confirm = cusView.findViewById<TextView>(com.olam.warehouse.presentation.R.id.btnConfirm)
        val cancel = cusView.findViewById<TextView>(com.olam.warehouse.presentation.R.id.btnCancel)
        val rbDirect = cusView.findViewById<RadioButton>(R.id.rbDirect)
        val rbIndirect = cusView.findViewById<RadioButton>(R.id.rbIndirect)
        val rbThirdParty = cusView.findViewById<RadioButton>(R.id.rbThirdParty)
        val rbFarmerLessTransaction = cusView.findViewById<RadioButton>(R.id.rbFarmerlessTransaction)
        val rbComplaint = cusView.findViewById<RadioButton>(R.id.rbComplaint)
        val rbNonComplaint = cusView.findViewById<RadioButton>(R.id.rbNonComplaint)
        val actionViewClayout = cusView.findViewById<LinearLayout>(R.id.actionViewCl)

        val rgComplaintType = cusView.findViewById<RadioGroup>(com.olam.warehouse.presentation.R.id.rgComplaintType)
        confirm.text = positive
        cancel.text = negative
        actionViewClayout.visible()
        if (positive.isEmpty()) confirm.gone() else confirm.visible()
        if (negative.isEmpty()) cancel.gone() else cancel.visible()

        if(!procurementType) {
            rbDirect.setText(Constants.COMPLAINT)
            rbIndirect.setText(Constants.NON_COMPLAINT)
            if(PreferenceHelper.get(Constants.TRACK_TRACE_THIRD_PARTY,"").isNotEmpty())rbThirdParty.visible() else rbThirdParty.gone()
            rbFarmerLessTransaction.gone()
        } else {
            if(PreferenceHelper.get(Constants.DIRECT,"").isEmpty()) rbDirect.gone()
            if(PreferenceHelper.get(Constants.IN_DIRECT,"").isEmpty())rbIndirect.gone()
            if(PreferenceHelper.get(Constants.TRACK_TRACE_THIRD_PARTY,"").isEmpty())rbThirdParty.gone()
            if(PreferenceHelper.get(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION,"").isEmpty())rbFarmerLessTransaction.gone()
        }


        materialDialog.view.background =
            ContextCompat.getDrawable(materialDialog.view.context, com.olam.warehouse.presentation.R.color.warm_white)


        rbDirect.setOnClickListener {
            posclickAction.invoke(true, false, false)
            materialDialog.dismiss()
        }
        rbIndirect.setOnClickListener {
            posclickAction.invoke(false, false, false)
            materialDialog.dismiss()
        }
        rbThirdParty.setOnClickListener{
            posclickAction.invoke(false, true, false)
            materialDialog.dismiss()
        }
        rbFarmerLessTransaction.setOnClickListener {
            posclickAction.invoke(false, false, true)
            materialDialog.dismiss()
        }


        confirm.setOnClickListener {
            posclickAction.invoke(rbDirect.isChecked, rbThirdParty.isChecked, rbFarmerLessTransaction.isChecked)
            materialDialog.dismiss()
        }

        /*this is only for nicaragua, forward po module*/
        if(positive.equals(Constants.DISABLE_THIRD_PARTY)){
            rbThirdParty.gone()
            confirm.gone()
        }

        if (!PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "").contains("OFI")) {
            confirm.setTextColor(ContextCompat.getColor(confirm.context, com.olam.warehouse.presentation.R.color.white))
            cancel.setTextColor(ContextCompat.getColor(confirm.context, com.olam.warehouse.presentation.R.color.text_black))
            ViewCompat.setBackgroundTintList(
                confirm,
                ContextCompat.getColorStateList(materialDialog.context, com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
            )
            ViewCompat.setBackground(
                cancel,
                ContextCompat.getDrawable(cancel.context, com.olam.warehouse.presentation.R.drawable.rectangle_black_line_border)
            )
        }
        materialDialog.cancelOnTouchOutside(false)
        return materialDialog
    }


}


