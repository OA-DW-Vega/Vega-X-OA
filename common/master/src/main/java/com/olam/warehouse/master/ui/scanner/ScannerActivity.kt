package com.olam.warehouse.master.ui.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.olam.warehouse.master.R
import com.olam.warehouse.master.ui.BaseActivity
import com.olam.warehouse.presentation.databinding.ActivityScannerBinding
import com.olam.warehouse.presentation.utils.Constants.SCANNED_ID
import com.olam.warehouse.presentation.utils.UIUtils.showRationaleDialog
import com.olam.warehouse.presentation.utils.extension.toast
import permissions.dispatcher.*

@RuntimePermissions
class ScannerActivity : BaseActivity(), SurfaceHolder.Callback, View.OnClickListener, Detector.Processor<Barcode> {

    private var detector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null
    private lateinit var binding: ActivityScannerBinding
    override val layoutResourceId = R.layout.activity_scanner

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
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
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

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
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
            intent.putExtra(SCANNED_ID, id)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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

}
