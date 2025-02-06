package com.olam.warehouse.presentation.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import okhttp3.ResponseBody
import java.io.*

/**
 * Created by Baskaran Kannan on 7/27/2020.
 */

fun copyFileToSdCard(context: Context) {
    val afile = context.assets.open("pw.apk")
    val root = context.filesDir.toString() + File.separator + "directory"
    val dir = File(root)
    dir.mkdirs()
    val file = File(root + File.separator + "pwm.apk")
    if (file.exists()) file.delete()
    file.createNewFile()
    val inStream = afile
    val outStream = FileOutputStream(file)
    val buffer = ByteArray(inStream.readBytes().size)
    var length = inStream.read(buffer)
    while (length > 0) {
        outStream.write(buffer, 0, length)
        length = inStream.read(buffer)
    }
    inStream.close()
    outStream.close()
}

fun writeResponseBodyToDisk(body: ResponseBody?, directory: String, path: String): Boolean {
    body?.let {
        try {
            /*val dir = File(directory)
            val futureStudioIconFile = File.createTempFile(path,".apk",dir)*/
            val futureStudioIconFile = File(directory, path)

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val fileReader = ByteArray(4096)

                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0

                inputStream = body.byteStream()
                outputStream = FileOutputStream(futureStudioIconFile)

                while (true) {
                    val read = inputStream.read(fileReader)

                    if (read == -1) {
                        break
                    }

                    outputStream.write(fileReader, 0, read)

                    fileSizeDownloaded += read.toLong()

                    Log.d(ContentValues.TAG, "file download: $fileSizeDownloaded of $fileSize")
                }

                outputStream.flush()

                return true
            } catch (e: IOException) {
                return false
            } finally {
                inputStream?.close()
                outputStream?.close()
            }

        } catch (e: IOException) {
            return false
        }
    }
    return false
}


fun createDirectory(folderName: String, context: Context): String {
//    val root = Environment.getExternalStorageDirectory().absolutePath.toString() + File.separator + folderName
    val root = context.getExternalFilesDir(File.separator + folderName)?.absolutePath.toString()
    val dir = File(root)
    if (!dir.exists()) dir.mkdirs()
    return dir.absolutePath.toString()
}

fun getFilePath(fileName: String): String {
    return Environment.getExternalStorageDirectory().absolutePath.toString() + File.separator + fileName
}

fun bitmapToFile(bitmap: Bitmap, fileNameToSave: String, context: Context): File? { // File name like "image.png"
    //create a file to write bitmap data
    var file: File? = null
    return try {
        file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath.toString() + File.separator
                +fileNameToSave)
        file.createNewFile()

        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
        val bitmapdata = bos.toByteArray()

        //write the bytes in file
        val fos = FileOutputStream(file)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        file // it will return null
    }
}

fun convertFileToBitmap(file: File) = BitmapFactory.decodeFile(file.path)
