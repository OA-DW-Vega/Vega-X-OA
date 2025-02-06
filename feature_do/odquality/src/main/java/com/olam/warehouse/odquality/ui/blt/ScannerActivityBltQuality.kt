package com.olam.warehouse.odquality.ui.blt

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.odquality.R
import com.olam.warehouse.odquality.databinding.ActivityScannerBltQualityBinding
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import permissions.dispatcher.*
import java.lang.ref.WeakReference

@RuntimePermissions
class ScannerActivityBltQuality : HomeBaseActivity(), SurfaceHolder.Callback, View.OnClickListener, Detector.Processor<Barcode> {

    companion object {
        val REQUEST_SHOWCAMERA: Int = 0
        val PERMISSION_SHOWCAMERA: Array<String> = arrayOf("android.permission.CAMERA")
    }

    private var detector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null
    private lateinit var binding: ActivityScannerBltQualityBinding
    override val layoutResourceId = R.layout.activity_scanner_blt_quality

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showCameraWithPermissionCheck()
        binding = ActivityScannerBltQualityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odquality/ui/blt/ScannerActivityBltQuality").title("OD/Quality")
            .with(tracker)
        initUI()
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

    class ScannerActivityBltShowCameraPermissionRequest(target: ScannerActivityBltQuality): PermissionRequest {
        private val weakTarget: WeakReference<ScannerActivityBltQuality> = WeakReference(target)

        override fun proceed() {
            val target = weakTarget.get() ?: return
            ActivityCompat.requestPermissions(target, PERMISSION_SHOWCAMERA, REQUEST_SHOWCAMERA)
        }

        override fun cancel() {
            val target = weakTarget.get() ?: return
            target.onCameraDenied()
        }
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

        binding.tvQRCode.setOnClickListener {
            cameraSource?.stop()
            cameraSource?.release()
            binding.scannerLayout.visibility = View.GONE
            binding.manualQRCodeLayout.visibility = View.VISIBLE
            binding.btnSave.visibility = View.VISIBLE

        }

        binding.btnSave.setOnClickListener {
            val text = binding.tvQRCodeManual.text.toString()
            if (text.isEmpty() || text.isBlank()) {
                toast("Enter valid bag qr code")
            } else {
                val intent = Intent()
                intent.putExtra(Constants.SCANNED_ID, text)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        if (PermissionUtils.hasSelfPermissions(this, Manifest.permission.CAMERA)) {
            updateSurface()
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateSurface() {
        try {
            cameraSource?.start(binding.cameraPreview.holder)
        } catch (e: UnsupportedOperationException) {
            toast(e.message.toString())
        }
    }

    override fun surfaceChanged(p0: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun onClick(v: View?) {
    }

    override fun release() {
    }

    override fun receiveDetections(p0: Detector.Detections<Barcode>?) {
        val qrcodes = p0?.detectedItems
        if (qrcodes?.size() != 0) {
            val id = (qrcodes?.valueAt(0) as Barcode).rawValue
            val intent = Intent()
            intent.putExtra(Constants.SCANNED_ID, id)
            setResult(Activity.RESULT_OK, intent)
            finish()
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

    /*Runtime permission check*/
    @NeedsPermission(Manifest.permission.CAMERA)
    fun showCamera() {
        updateSurface()
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    fun showRationaleForCamera(request: PermissionRequest) {
        UIUtils.showRationaleDialog(this, com.olam.warehouse.presentation.R.string.permission_camera_rationale, request)
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
}
