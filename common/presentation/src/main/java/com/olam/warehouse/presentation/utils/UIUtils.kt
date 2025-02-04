package com.olam.warehouse.presentation.utils

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.olam.warehouse.presentation.R
import com.olam.warehouse.presentation.enums.SyncStatus
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import kotlinx.android.synthetic.main.dialog_posting_date_selection.view.*
import kotlinx.android.synthetic.main.dialog_start_loading_layout.view.btCancel
import kotlinx.android.synthetic.main.dialog_start_loading_layout.view.btConform
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
    const val PRINT_FGRN_TALLYSHEET = "print_fgrn_tallysheet"
    const val COCOA_MTNT = "virtualMtnt"
    const val COCOA_MTNR = "virtual_Mtnr"

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
    const val FROM_CAMEROON_GATEENTRY_COCOA = "from_cameroon_gate_entry_cocoa"
    const val FROM_CAMEROON_COCOA_ADD_CONTAINER = "from_cameroon_cocoa_add_container"
    const val FROM_CAMEROON_COCOA_QA = "from_cameroon_cocoa_quality_approve"
    const val FROM_NIGERIA_COCOA_QA = "from_nigeria_cocoa_quality_approve"
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

    const val FROM_TRANSACTION = "from_transaction"
    const val FROM_GHANA = "from_ghana"

    const val NICERTFD_SBUX = "NICERTFD SBUX"
    const val NICERTFD_UTZ = "NICERTFD UTZ"
    const val NICERTFD_RFA = "NICERTFD RFA"

    const val MTNT_VIRTUAL_DATA = "no_weighment_data"
    const val MTNT_VIRTUAL_OUTPUT_DATA = "grn_work_data"

    const val REQUEST_ENABLE_BT = 123
    const val BT_MAC = "STORED_BT_MAC"

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
            title(R.string.error)
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
        listener: UIUtils.DialogClick
    ) {
        val view =
            LayoutInflater.from(context).inflate(R.layout.dialog_posting_date_selection, null)
        view.enterPostingDate.setOnClickListener(View.OnClickListener {
            setupUi(context, view.enterPostingDate)
        })
        val alertBuilder = AlertDialog.Builder(context).setView(view)
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
                                it, R.color.green
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
                                it, R.color.dark_green_1
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
        materialDialog.view.background =
            ContextCompat.getDrawable(materialDialog.view.context, R.color.warm_white)
        confirm.setOnClickListener {
            posclickAction.invoke()
            materialDialog.dismiss()
        }
        cancel.setOnClickListener { negaclickAction.invoke() }
        if (!PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "").contains("OFI")) {
            confirm.setTextColor(ContextCompat.getColor(confirm.context, R.color.green))
            cancel.setTextColor(ContextCompat.getColor(confirm.context, R.color.red))
            ViewCompat.setBackgroundTintList(
                confirm,
                ContextCompat.getColorStateList(materialDialog.context, R.color.warm_white)
            )
            ViewCompat.setBackgroundTintList(
                cancel,
                ContextCompat.getColorStateList(materialDialog.context, R.color.warm_white)
            )
        }
        return materialDialog
    }

    fun setOlamLogoDynamically(ivOlamLogo: ImageView) {
        when {
            PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "").contains(Constants.OFI) -> {
                ivOlamLogo.setImageDrawable(ivOlamLogo.context.getDrawable(R.drawable.ic_olam_logo_ofi_new))
            }
            else -> {
                ivOlamLogo.setImageDrawable(ivOlamLogo.context.getDrawable(R.drawable.ic_olam_logo))
            }
        }
    }

    fun getTenDigRandomId() = Random.nextInt().toString().takeLast(11)

}


