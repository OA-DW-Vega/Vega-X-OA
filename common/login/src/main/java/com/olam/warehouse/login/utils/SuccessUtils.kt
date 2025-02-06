package com.olam.warehouse.login.utils

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.util.Base64
import android.view.View
import android.view.View.MeasureSpec
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.DialogWithHyperLinkBinding
import com.olam.warehouse.login.ui.FrequentlyAskedQActivity
import com.olam.warehouse.login.ui.notification.NotificationActivity
import com.olam.warehouse.master.common.model.MessageModel
import com.olam.warehouse.master.common.model.NotificationModel
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentUserName
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.ERROR_MSG
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.fromJson
import java.io.ByteArrayOutputStream


const val A4_WIDTH = 3508
const val A4_HEIGHT = 2480
fun bitmapToString(bitmap: Bitmap): String {
    try{
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }catch (e: OutOfMemoryError){
        return ""
    }
}

fun getBitmapFromViewNg(view: View,defaultColor: Int): Bitmap {
    view.measure(A4_WIDTH, A4_HEIGHT)
    val bitmap = Bitmap.createBitmap(
        view.measuredWidth, view.measuredHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    canvas.drawColor(defaultColor)
    view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    view.draw(canvas)
    return bitmap
}

fun getBitmapFromView(view: View, defaultColor: Int): Bitmap {
    view.measure(A4_WIDTH, A4_HEIGHT)
    var bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
    var canvas = Canvas(bitmap)
    canvas.drawColor(defaultColor)
    view.layout(view.left, view.top, view.right, view.bottom)
    view.draw(canvas)
    return bitmap
}




fun getBitmapFromViewNic(view: View, defaultColor: Int, size: Int): Bitmap {
    view.measure(A4_WIDTH, A4_HEIGHT)
    val hight = size / 7
    var bitmap =
        Bitmap.createBitmap(
            view.measuredWidth,
            hight * view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
    var canvas = Canvas(bitmap)
    canvas.drawColor(defaultColor)
    view.layout(view.left, view.top, view.right, view.bottom)
    view.draw(canvas)
    return bitmap
}

fun getBitmap(lot: String): Bitmap? {
    val content = lot
    val writer = MultiFormatWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}

@TargetApi(Build.VERSION_CODES.M)
fun animate(view: AppCompatImageView, scanFingerprint: AnimatedVectorDrawable) {
    view.setImageDrawable(scanFingerprint)
    scanFingerprint.start()
}

fun makeUnReceivedNotification(context: Context) {
    val notifiList = arrayListOf<NotificationModel>()
    val oldNotifiList = PreferenceHelper.get(Constants.NOTIFICATION_LIST, "")
    if (oldNotifiList.isNotEmpty()) notifiList.addAll(
        Gson().fromJson<List<NotificationModel>>(
            oldNotifiList
        )
    )
    val filteredItem = notifiList.filter { !it.isViewed }
    filteredItem.forEach {
        val bodyData = Gson().fromJson<MessageModel>(it.notification)
        val splitItem = bodyData.flag?.split(",")
        when {
            splitItem?.any { it.equals(getCurrentKey(), true)} == true && !getCurrentKey().isNullOrEmpty() -> makePushNotification(
                bodyData.title.toString(),
                bodyData.message,
                context
            )
            splitItem?.any { it.equals(getPlantDetails().plantId, true)} == true && !getPlantDetails().plantId.isNullOrEmpty() -> makePushNotification(
                bodyData.title.toString(),
                bodyData.message,
                context
            )
            splitItem?.any { it.equals(getCurrentUserName(), true)} == true && !getCurrentUserName().isNullOrEmpty() -> makePushNotification(
                bodyData.title.toString(),
                bodyData.message,
                context
            )
            splitItem?.any { it.equals(Constants.ALL, true)} == true -> makePushNotification(
                bodyData.title.toString(),
                bodyData.message,
                context
            )
        }
    }
}

fun makePushNotification(title: String?, body: String?, context: Context) {
    // Make a channel if necessary
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = Constants.VERBOSE_NOTIFICATION_CHANNEL_NAME
        val description = Constants.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(Constants.CHANNEL_ID, name, importance)

        // Add the channel
        val notificationManager =
            context
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        notificationManager?.createNotificationChannel(channel)
    }
    //context.sendBroadcast(Intent(Constants.NOTIFICATION_RECEIVED))
    context.sendBroadcast(
        Intent(Constants.NOTIFICATION_RECEIVED).apply {
            setPackage(context.packageName)
        }
    )
    val intent = Intent(context, NotificationActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
    )

    // Create the notification
    val builder = NotificationCompat.Builder(context, Constants.CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(body)
        .setAutoCancel(false)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setOngoing(true)
        .setContentIntent(pendingIntent)
        .setVibrate(LongArray(0))

    // Show the notification
    NotificationManagerCompat.from(context).notify(Constants.NOTIFICATION_ID, builder.build())
}

fun showErrorDialogWithFAQLink(context: Context, msg1: String,from:String="") {
    var msg = " "
    if(msg1.contains("Unable to resolve host"))
        msg =  context.getString(com.olam.warehouse.presentation.R.string.network_not_available)
    else
        msg = msg1
    if (MaterialDialog(context).isShowing) MaterialDialog(context).dismiss()
    var msgList = msg.split(" ")
    if(!msgList.any { it.equals("401") }) {
        MaterialDialog(context).show {
//        title(R.string.error)
            cancelOnTouchOutside(false)
            cancelable(false)
            //val view = LayoutInflater.from(context).inflate(R.layout.dialog_with_hyper_link, null)
            val view = DialogWithHyperLinkBinding.inflate(layoutInflater)
            setContentView(view.root)
            if(from=="FAQ")view.tvHyperLink.gone()
            view.tvHyperLink.setOnClickListener {
                val intent = Intent(context, FrequentlyAskedQActivity::class.java)
                intent.putExtra(ERROR_MSG, msg)
                context.startActivity(intent)
            }
            view.tvOk.setOnClickListener { dismiss() }
            view.tvMsg.text = msg
//        message(null, msg)
//        positiveButton(text = UIUtils.getSpannedText(context.getString(R.string.ok), true))
        }
    }


}

