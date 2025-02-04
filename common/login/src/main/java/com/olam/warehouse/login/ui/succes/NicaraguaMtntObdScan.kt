package com.olam.warehouse.login.ui.succes

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.NicaraguaMtntObdScanBinding
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.login.ui.home.HomeBaseActivity

class NicaraguaMtntObdScan : HomeBaseActivity() {

    private lateinit var binding: NicaraguaMtntObdScanBinding
    override val layoutResourceId = R.layout.nicaragua_mtnt_obd_scan

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = NicaraguaMtntObdScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //val bundle = intent?.extras
        binding.tvTruckNoValue.text = intent?.getStringExtra("truckNo")
        binding.tvOBDNoValue.text = intent?.getStringExtra("Obdnumber")

        //val obdNumber = "8490021690"
        val obdNumber = intent?.getStringExtra("Obdnumber")
        val writer = MultiFormatWriter()
        val matrix: BitMatrix = writer.encode(obdNumber, BarcodeFormat.QR_CODE, 350, 350)
        val encoder = BarcodeEncoder()
        val bitmap: Bitmap = encoder.createBitmap(matrix)
        binding.ivOBDNoQRCode.setImageBitmap(bitmap)

        binding.btnOkay.setOnClickListener {
            val i = Intent(this, HomeActivity::class.java)
            startActivity(i)
        }

    }
}
