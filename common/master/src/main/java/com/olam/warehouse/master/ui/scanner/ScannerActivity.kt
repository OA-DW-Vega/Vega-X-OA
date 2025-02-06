package com.olam.warehouse.master.ui.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.olam.warehouse.master.R
import com.olam.warehouse.master.ui.BaseActivity
import com.olam.warehouse.presentation.databinding.ActivityScannerBinding
import com.olam.warehouse.presentation.utils.Constants.SCANNED_ID
import com.olam.warehouse.presentation.utils.UIUtils.showRationaleDialog
import com.olam.warehouse.presentation.utils.extension.toast
import permissions.dispatcher.*
import java.io.IOException
import java.lang.ref.WeakReference

@RuntimePermissions
class ScannerActivity : BaseActivity(), SurfaceHolder.Callback, View.OnClickListener, Detector.Processor<Barcode> {

    private var detector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null
    private lateinit var binding: ActivityScannerBinding
    override val layoutResourceId = R.layout.activity_scanner

    companion object {
        val REQUEST_SHOWCAMERA: Int = 0
        val PERMISSION_SHOWCAMERA: Array<String> = arrayOf("android.permission.CAMERA")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showCameraWithPermissionCheck()
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    private fun initUI() {
        detector = BarcodeDetector.Builder(applicationContext)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        cameraSource = CameraSource.Builder(applicationContext, detector)
            .setRequestedPreviewSize(1280, 720)
            .setRequestedFps(30.0f)
            .setAutoFocusEnabled(true)
            .build()

        binding.cameraPreview.holder?.addCallback(this)
        binding.cameraPreview.setOnClickListener(this)
        detector?.setProcessor(this)
        binding.tvUploadFromGallery.setOnClickListener {
            changeImage.launch("image/*")
        }
    }

    private val changeImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                processImage(uri)
            } catch (e: Exception) {
                Log.e("QRCode", "Error loading image", e)
                toast("Failed to load image")
            }
        }
    }

    private fun processImage(imageUri: Uri) {
        val image: InputImage
        try {
            image = InputImage.fromFilePath(this, imageUri)
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.QR_CODE,
                    Barcode.CODE_128
                )
                .build()
            val scanner = BarcodeScanning.getClient(options)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isEmpty()) {
                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                        val value = extractQRCode(bitmap)
                        if(value.isNullOrEmpty())
                            toast(getString(R.string.no_qrcode_found))
                        else
                            returnQRValue(value)
                    } else {
                        // for (barcode in barcodes) {
                        val barcode = barcodes.get(0)
                        val rawValue = barcode.rawValue
                        rawValue?.let { returnQRValue(it) }
                        Log.d("QR Code", "Value: $rawValue")
                        // }
                    }
                }
                .addOnFailureListener {
                    // Handle the error
                    toast("Error: ${it.message}")
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun returnQRValue(qrValue: String) {
        val intent = Intent()
        intent.putExtra(SCANNED_ID, qrValue)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun extractQRCode(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            val hints = mapOf(
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE)
            )

            val reader = MultiFormatReader()
            val result = reader.decode(binaryBitmap, hints)
            result.text
        } catch (e: NotFoundException) {
            Log.d("QRCode", "No QR code found in image")
            null
        } catch (e: Exception) {
            Log.e("QRCode", "Error processing QR code", e)
            null
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (PermissionUtils.hasSelfPermissions(this, Manifest.permission.CAMERA)) {
            updateSurface()
        }
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    private fun updateSurface() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
               return
            }
            cameraSource?.start(binding.cameraPreview.holder)
        } catch (e: UnsupportedOperationException) {
            toast(e.message.toString())
        }
    }

    override fun onClick(v: View?) {
    }

    override fun release() {
    }

    override fun receiveDetections(p0: Detector.Detections<Barcode>?) {
        val qrcodes = p0?.detectedItems
        if (qrcodes?.size() != 0) {
            val id = (qrcodes?.valueAt(0) as Barcode).rawValue
            returnQRValue(id)
            /*val intent = Intent()
            intent.putExtra(SCANNED_ID, id)
            setResult(Activity.RESULT_OK, intent)
            finish()*/
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
    }

    /*Runtime permission check*/
    @NeedsPermission(Manifest.permission.CAMERA)
    fun showCamera() {
        updateSurface()
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    fun showRationaleForCamera(request: PermissionRequest) {
        showRationaleDialog(this, com.olam.warehouse.presentation.R.string.permission_camera_rationale, request)
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun onCameraDenied() {
        toast(getString(com.olam.warehouse.presentation.R.string.permission_camera_denied))
        finish()
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    fun onCameraNeverAskAgain() {
        toast(getString(com.olam.warehouse.presentation.R.string.permission_camera_never_ask_again))
        finish()
    }
    private fun showCameraWithPermissionCheck() {
        if (PermissionUtils.hasSelfPermissions(this, *PERMISSION_SHOWCAMERA)) {
            showCamera()
        } else {
            if (PermissionUtils.shouldShowRequestPermissionRationale(this, *PERMISSION_SHOWCAMERA)) {
                showRationaleForCamera(ScannerActivityBltShowCameraPermissionRequest(this))
            } else {
                ActivityCompat.requestPermissions(this, PERMISSION_SHOWCAMERA, REQUEST_SHOWCAMERA)
            }
        }
    }

    internal fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_SHOWCAMERA -> {
                if (PermissionUtils.verifyPermissions(*grantResults)) {
                    showCamera()
                } else {
                    if (!PermissionUtils.shouldShowRequestPermissionRationale(
                            this,
                            *PERMISSION_SHOWCAMERA
                        )
                    ) {
                        onCameraNeverAskAgain()
                    } else {
                        onCameraDenied()
                    }
                }
            }
        }
    }

    class ScannerActivityBltShowCameraPermissionRequest(target: ScannerActivity): PermissionRequest {
        private val weakTarget: WeakReference<ScannerActivity> = WeakReference(target)

        override fun proceed() {
            val target = weakTarget.get() ?: return
            ActivityCompat.requestPermissions(target, PERMISSION_SHOWCAMERA, REQUEST_SHOWCAMERA)
        }

        override fun cancel() {
            val target = weakTarget.get() ?: return
            target.onCameraDenied()
        }
    }
}
