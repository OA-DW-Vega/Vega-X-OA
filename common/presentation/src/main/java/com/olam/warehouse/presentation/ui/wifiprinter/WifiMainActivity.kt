package com.olam.warehouse.presentation.ui.wifiprinter

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mywifiprinter.observers.Observable
import com.example.mywifiprinter.observers.Observer
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import com.olam.warehouse.presentation.R
import com.olam.warehouse.presentation.ui.PrintPreviewDialogFragment
import com.olam.warehouse.presentation.ui.wifiprinter.services.PrintCompleteService
import com.olam.warehouse.presentation.ui.wifiprinter.services.PrintUtility
import com.olam.warehouse.presentation.ui.wifiprinter.services.WifiScanner
import com.olam.warehouse.presentation.utils.Constants.BITMAP_KEYS
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.fromJson
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException

/**
 * Created by Baskaran Kannan on 10/13/2020.
 */
class WifiMainActivity : AppCompatActivity(), Observer, PrintCompleteService {
    var mPrintDialog: Dialog? = null
    private var mllWifiPrint: LinearLayout? = null
    private var mllBluetoothPrint: LinearLayout? = null
    private var pdfFile: File? = null
    private var externalStorageDirectory: String? = null
    private var mPrintUtility: PrintUtility? = null
    private var mWifiScanner: WifiScanner? = null
    private var mWifiManager: WifiManager? = null
    private var mLocationManager: LocationManager? = null
    private var mObservable: Observable? = null
    private var bitmapPrintKeys = ArrayList<String>()
    private var REQUEST_CHECK_SETTINGS = 199
    var image: Image? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wifi_main_activity)
        initExtra()
        initUI()
    }

    private fun initExtra() {
        val selectedKey = PreferenceHelper.get(BITMAP_KEYS, "")
        bitmapPrintKeys = Gson().fromJson<ArrayList<String>>(selectedKey)
    }

    private fun initUI() {
        try {
            externalStorageDirectory =
                this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
            val folder = File(externalStorageDirectory, Constants.CONTROLLER_PDF_FOLDER)
//            pdfFile = File(folder, "file name with extension")
            pdfFile = createpdfNew()

            /* if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }*/

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1
                    )
                }
                displayLocationSettingsRequest1(this)
            } else {
                displayLocationSettingsRequest1(this)
            }

            mObservable = ObservableSingleton.getInstance()
            mWifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            mLocationManager =
                applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (mWifiManager?.isWifiEnabled == false) {
                Toast.makeText(applicationContext, "Enabling WiFi..", Toast.LENGTH_LONG).show()
                mWifiManager?.isWifiEnabled = true
            }
            mWifiScanner = WifiScanner()
            mPrintUtility = PrintUtility(this, mWifiManager!!, mWifiScanner!!)
            mllWifiPrint = findViewById<View>(R.id.llWifi) as LinearLayout
            mllBluetoothPrint = findViewById<View>(R.id.llBluetooth) as LinearLayout

            mllWifiPrint?.setOnClickListener {
                try {
                    mPrintDialog?.show()
                    mObservable?.attach(this)
                    mPrintUtility?.print(pdfFile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            mllBluetoothPrint?.setOnClickListener {
                val list = mutableListOf<String>()
                list.addAll(bitmapPrintKeys)
                val dialogFragment = PrintPreviewDialogFragment(list)
                supportFragmentManager.let { dialogFragment.show(it, "signature") }
            }

            initPrintDialog()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun displayLocationSettingsRequest1(context: Context) {
        val googleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API).build()
        googleApiClient.connect()

        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 2000

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)


        val result =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback(object : ResultCallback<LocationSettingsResult> {
            override fun onResult(result: LocationSettingsResult) {
                val status = result.status
                if (status.statusCode == LocationSettingsStatusCodes.SUCCESS) {
//                    Log.d("Martin", "Lokacija je vključena!")
                }
                if (status.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    try {
                        status.startResolutionForResult(this@WifiMainActivity, 0x1)
                    } catch (e: IntentSender.SendIntentException) {
                        e.printStackTrace()
//                        Log.d("Martin", "PendingIntent unable to execute request.")
                    }

                }
                //if (status.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) Log.d("Martin", "Location settings are inadequate, and cannot be fixed here. Dialog "+"not created.")
            }
        })
    }
    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_CANCELED -> {

                    // The user was asked to change settings, but chose not to
                    finish()
                }
                else -> {
                }
            }
        }
    }*/

    private fun initPrintDialog() {
        mPrintDialog = Dialog(this)
        mPrintDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val window = mPrintDialog!!.window
        window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mPrintDialog?.setContentView(R.layout.dialog_progressbar)
        mPrintDialog?.setCancelable(true)
        mPrintDialog?.setCanceledOnTouchOutside(true)
        mPrintDialog?.setOnKeyListener(DialogInterface.OnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                val alert = AlertDialog.Builder(this)
                alert.setMessage("Do you want to cancel printing?")
                alert.setPositiveButton("OK") { dialogInterface, i ->
                    dialogInterface.dismiss()
                    mPrintUtility?.onPrintCancelled()
                }
                alert.setNegativeButton("Cancel") { dialogInterface, i -> dialogInterface.dismiss() }
                alert.show()
                return@OnKeyListener true
            }
            false
        })
    }

    override fun onResume() {
        super.onResume()
        try {
            // This will give list of wifi available nearby.
            registerReceiver(mWifiScanner, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            mWifiManager?.startScan()
            mWifiScanner?.getScanResults()?.let { mPrintUtility?.setScanResults(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            if (mPrintDialog != null) mPrintDialog?.dismiss()
            unregisterReceiver(mWifiScanner)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE_PRINTER && resultCode == Constants.RESULT_CODE_PRINTER) {

            // stores printer configuration and prints..
            if (mPrintDialog?.isShowing == false) mPrintDialog?.show()
            mPrintUtility?.getPrinterConfigAndPrint()
        } else if (requestCode == Constants.REQUEST_CODE_WIFI && resultCode == Constants.RESULT_CODE_PRINTER) {
            // after switch back to wifi..
        } else if (requestCode == Constants.REQUEST_CODE_PRINTER && resultCode == Constants.RESULT_CODE_PRINTER_CONNECT_FAILED) {
            if (mPrintDialog?.isShowing == false) mPrintDialog?.show()
            mPrintUtility?.onPrintCancelled()
        }
    }

    override fun onBackPressed() {
        try {
            if (mPrintDialog != null && mPrintDialog?.isShowing == true) {
            } else {
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMessage(status: Int) {
        // get List of PrintJob from PrintManager
        mPrintUtility?.completePrintJob()
    }

    override fun respondAfterWifiSwitch() {
        // code after network switch completes.
    }


    override fun update() {
        if (mPrintDialog != null && mPrintDialog!!.isShowing) {
            mPrintDialog!!.dismiss()
        }
        mObservable?.detach(this)
    }

    override fun updateObserver(bool: Boolean) {
        try {
            mObservable?.detach(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun updateObserverProgress(percentage: Int) {}

    /*private val key =
        "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"*/
    private fun createpdfNew(): File? {
        val directory_path =
            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath + "/receipt/"
        try {
            val file1 = File(directory_path)
            if (!file1.exists()) {
                file1.mkdirs()
            }
            val targetPdf = directory_path + "receipt.pdf"
            val filePath = File(targetPdf)
            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(filePath))
            document.open()
            val documentRect = document.pageSize
            bitmapPrintKeys.forEachIndexed { index, s ->
                try {
                    addImage(document, s, documentRect)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                document.newPage()
            }
            document.close()
            return filePath
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return File(directory_path + "receipt.pdf")
    }


    private fun addImage(
        document: Document,
        key: String,
        documentRect: Rectangle
    ) {
        try {
            val decodedString: ByteArray = Base64.decode(key, Base64.DEFAULT)
            image =
                Image.getInstance(decodedString) ///Here i set byte array..you can do bitmap to byte array and set in image...
        } catch (e: BadElementException) {
            e.printStackTrace()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val actualHeight =image!!.height
        val actualWidth = image!!.width
        val docWidth = documentRect.width-75
        val docHeight = documentRect.height-75
        val actualRatio = actualWidth/actualHeight
        val width = docWidth
        val height =docWidth/actualRatio
        image?.scaleAbsolute(width, height)

        try {
            image?.isScaleToFitHeight = false/*true*/
            image?.isScaleToFitLineWhenOverflow = true
            document.add(image)
        } catch (e: DocumentException) {
            e.printStackTrace()
        }
    }
}

