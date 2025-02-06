package com.olam.warehouse.vegax.ui

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import com.olam.warehouse.login.ui.LoginActivity
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.login.ui.quickpinaccess.createpin.VegaCreatePinActivity
import com.olam.warehouse.master.ui.BaseActivity
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.IS_LOGGED_IN
import com.olam.warehouse.presentation.utils.Constants.OFI
import com.olam.warehouse.presentation.utils.Constants.USER_NAME
import com.olam.warehouse.presentation.utils.Constants.mDelay
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.EXTRA_SET_PIN
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.BuildConfig.CURRENT_ORIGIN
import com.olam.warehouse.vegax.R
import com.olam.warehouse.vegax.databinding.ActivitySplashBinding

/**
 * Created by SangiliPandian C on 07-11-2019.
 */
class SplashActivity : BaseActivity() {

    override val layoutResourceId = R.layout.activity_splash
    private val REQUEST_CODE = 220
    private val LOCK_REQUEST_CODE = 221
    private val SECURITY_SETTING_REQUEST_CODE = 233
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //toast(CURRENT_ORIGIN)
        setEntityLevelVarient()
        supportActionBar?.hide()
        Handler().postDelayed({
            val isSecurityPin = PreferenceHelper.get(Constants.IS_SECURITY_PIN, false)
            when (isSecurityPin) {
                true -> checkQuickPin() // Quick Pin Access
                else -> moveToLoginPage()
            }
        }, mDelay)
    }

    private fun setEntityLevelVarient() {
        PreferenceHelper.save(Constants.CURRENT_ORIGIN_KEY, CURRENT_ORIGIN)
        when {
            CURRENT_ORIGIN.contains(OFI) -> {
                binding.ivLogo.setImageDrawable(binding.ivLogo.context.getDrawable(com.olam.warehouse.presentation.R.drawable.ic_olam_logo))
                /* ViewCompat.setBackground(
                     binding.cLayout,
                     ContextCompat.getDrawable(
                         binding.cLayout.context,
                         com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                     )
                 )*/
            }
            else -> {
                binding.clBottom.visible()
            }
        }
    }

    private fun checkQuickPin() {
        val isDevicePin = PreferenceHelper.get(Constants.IS_DEVICE_PIN, false)
        val createdNewPin = PreferenceHelper.get(Constants.QUICK_PIN, "")
        if (isDevicePin) {
            authenticateApp()
        } else {
            if (createdNewPin.isEmpty()) {
                moveToLoginPage()
            } else {
                val intent = Intent(this, VegaCreatePinActivity::class.java)
                intent.putExtra(EXTRA_SET_PIN, false)
                startActivityForResult(intent, REQUEST_CODE)
            }
        }
    }

    private fun moveToLoginPage() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra(IS_LOGGED_IN, PreferenceHelper.get(USER_NAME, "").isNotEmpty())
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    //method to authenticate app
    private fun authenticateApp() {
        //Get the instance of KeyGuardManager
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        //Check if the device version is greater than or equal to Lollipop(21)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Create an intent to open device screen lock screen to authenticate
            //Pass the Screen Lock screen Title and Description
            val i = keyguardManager.createConfirmDeviceCredentialIntent(
                resources.getString(com.olam.warehouse.login.R.string.unlock),
                resources.getString(com.olam.warehouse.login.R.string.confirm_pattern)
            )
            try {
                //Start activity for result
                startActivityForResult(i, LOCK_REQUEST_CODE)
            } catch (e: Exception) {
                e.printStackTrace()
                //If some exception occurs means Screen lock is not set up please set screen lock
                //Open Security screen directly to enable patter lock
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                try {

                    //Start activity for result
                    startActivityForResult(intent, SECURITY_SETTING_REQUEST_CODE)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    //If app is unable to find any Security settings then user has to set screen lock manually
//                    textView.setText(resources.getString(R.string.setting_label))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == LOCK_REQUEST_CODE || requestCode == REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                //If screen lock authentication is success update text
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } /*else {
                //If screen lock authentication is failed update text
//                textView.setText(resources.getString(R.string.unlock_failed))
            }*/
            requestCode == SECURITY_SETTING_REQUEST_CODE ->                 //When user is enabled Security settings then we don't get any kind of RESULT_OK
                //So we need to check whether device has enabled screen lock or not
                if (isDeviceSecure()) {
                    //If screen lock enabled show toast and start intent to authenticate user
                    toast(resources.getString(com.olam.warehouse.login.R.string.device_is_secure))
                    authenticateApp()
                } /*else {
                    //If screen lock is not enabled just update text
//                    textView.setText(resources.getString(R.string.security_device_cancelled))
                }*/
        }
    }

    /**
     * method to return whether device has screen lock enabled or not
     */
    private fun isDeviceSecure(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        //this method only work whose api level is greater than or equal to Jelly_Bean (16)
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && keyguardManager.isKeyguardSecure
        //You can also use keyguardManager.isDeviceSecure(); but it requires API Level 23
    }

}
