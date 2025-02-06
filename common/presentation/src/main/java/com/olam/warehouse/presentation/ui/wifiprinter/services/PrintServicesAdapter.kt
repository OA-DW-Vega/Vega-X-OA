package com.olam.warehouse.presentation.ui.wifiprinter.services

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import androidx.fragment.app.Fragment
import com.example.mywifiprinter.observers.Observable
import com.example.mywifiprinter.observers.Observer
import com.olam.warehouse.presentation.ui.wifiprinter.Constants.PRINTER_STATUS_CANCELLED
import com.olam.warehouse.presentation.ui.wifiprinter.Constants.PRINTER_STATUS_COMPLETED
import com.olam.warehouse.presentation.ui.wifiprinter.ObservableSingleton
import com.olam.warehouse.presentation.ui.wifiprinter.utils.Util
import java.io.*

/**
 * Created by Baskaran Kannan on 10/13/2020.
 */

class PrintServicesAdapter(mActivity: Activity, mFragment: Fragment?, pdfFile: File) : PrintDocumentAdapter(),
    Observer {
    private val mActivity: Activity
    private var totalpages = 1
    private val pdfFile: File
    private var mPrintCompleteService: PrintCompleteService? = null
    private val mFragment: Fragment?
    private var mObservable: Observable? = null

    init {
        this.mActivity = mActivity
        this.mFragment = mFragment
        this.pdfFile = pdfFile
        try {
            totalpages = Util.countPages(pdfFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (mFragment != null) {
            mPrintCompleteService = mFragment as PrintCompleteService
        } else {
            mPrintCompleteService = mActivity as PrintCompleteService
        }
        mObservable = ObservableSingleton.getInstance()
        mObservable?.attach(this)
    }

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        metadata: Bundle?
    ) {
        try {
            if (cancellationSignal.isCanceled) {
                callback.onLayoutCancelled()
                return
            }
            if (totalpages > 0) {
                val builder = PrintDocumentInfo.Builder(pdfFile.name)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(totalpages)
                val info = builder.build()
                callback.onLayoutFinished(info, true)
            } else {
                totalpages = 0
                callback.onLayoutFailed("Page count is zero.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onWrite(
        pageRanges: Array<PageRange?>?,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) {
        var input: InputStream? = null
        var output: OutputStream? = null
        try {
            input = FileInputStream(pdfFile)
            output = FileOutputStream(destination.fileDescriptor)
            val buf = ByteArray(1024)
            var bytesRead: Int
            while (input.read(buf).also { bytesRead = it } > 0) {
                output.write(buf, 0, bytesRead)
            }
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (ee: FileNotFoundException) {
            //Catch exception
            mPrintCompleteService?.onMessage(PRINTER_STATUS_CANCELLED)
        } catch (e: Exception) {
            //Catch exception
            mPrintCompleteService?.onMessage(PRINTER_STATUS_CANCELLED)
        }catch (e: Exception) {
            e.printStackTrace()
            mPrintCompleteService?.onMessage(PRINTER_STATUS_CANCELLED)
        } finally {
            try {
                input?.close()
                output?.close()
            } catch (e: IOException) {
                e.printStackTrace()
                mPrintCompleteService?.onMessage(PRINTER_STATUS_CANCELLED)
            }catch (e: Exception) {
                e.printStackTrace()
                mPrintCompleteService?.onMessage(PRINTER_STATUS_CANCELLED)
            }
        }
        cancellationSignal.setOnCancelListener { mPrintCompleteService?.onMessage(PRINTER_STATUS_CANCELLED) }
    }

    override fun onFinish() {
        mPrintCompleteService?.onMessage(PRINTER_STATUS_COMPLETED)
    }

    override fun update() {
        mObservable?.detach(this)
    }

    override fun updateObserver(bool: Boolean) {}
    override fun updateObserverProgress(percentage: Int) {}


}
