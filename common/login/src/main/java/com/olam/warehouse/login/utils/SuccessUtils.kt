package com.olam.warehouse.login.utils

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.util.Base64
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import java.io.ByteArrayOutputStream

const val A4_WIDTH = 3508
const val A4_HEIGHT = 2480
fun bitmapToString(bitmap: Bitmap): String {
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
    val b: ByteArray = baos.toByteArray()
    return Base64.encodeToString(b, Base64.DEFAULT)
}

fun getBitmapFromView(view: View, defaultColor: Int): Bitmap {
    view.measure(A4_WIDTH, A4_HEIGHT)
    var bitmap =
        Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
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

