package com.olam.warehouse.login.ui.quickpinaccess

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.KeyguardManager
import android.content.Context.KEYGUARD_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.FragmentQuickAccessPinSelectionBinding
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.login.ui.quickpinaccess.createpin.VegaCreatePinActivity
import com.olam.warehouse.login.worker.updateQuickPinOneTimeRequestWorker
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast

/**
 * Created by Baskaran Kannan on 11/24/2020.
 */
class VegaQuickAccessPinSelectFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_quick_access_pin_selection
    private lateinit var binding: FragmentQuickAccessPinSelectionBinding
    private val REQUEST_CODE = 220
    private val LOCK_REQUEST_CODE = 221
    private val SECURITY_SETTING_REQUEST_CODE = 233

    companion object {
        fun newInstance() = VegaQuickAccessPinSelectFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentQuickAccessPinSelectionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        val userName = PreferenceHelper.get(Constants.USER_NAME, "")
        binding.tvWelcome.text = getString(R.string.welcome).plus(" ").plus(userName)
        binding.rgRadioPin.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbCreateNewPin -> {
                    PreferenceHelper.save(Constants.IS_DEVICE_PIN, false)
                    val intent = Intent(activity, VegaCreatePinActivity::class.java)
                    intent.putExtra(UIUtils.EXTRA_SET_PIN, true)
                    startActivityForResult(intent, REQUEST_CODE)
                }
                R.id.rbUseExistingPin -> {
                    PreferenceHelper.save(Constants.QUICK_PIN, "")
                    authenticateApp()
                }
            }
        }
    }

    //method to authenticate app
    private fun authenticateApp() {
        //Get the instance of KeyGuardManager
        val keyguardManager = activity?.getSystemService(KEYGUARD_SERVICE) as KeyguardManager

        //Check if the device version is greater than or equal to Lollipop(21)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Create an intent to open device screen lock screen to authenticate
            //Pass the Screen Lock screen Title and Description
            val i = keyguardManager.createConfirmDeviceCredentialIntent(
                resources.getString(R.string.unlock),
                resources.getString(R.string.confirm_pattern)
            )
            try {
                //Start activity for result
                startActivityForResult(i, LOCK_REQUEST_CODE)
            } catch (e: Exception) {

                //If some exception occurs means Screen lock is not set up please set screen lock
                //Open Security screen directly to enable patter lock
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                try {

                    //Start activity for result
                    startActivityForResult(intent, SECURITY_SETTING_REQUEST_CODE)
                } catch (ex: Exception) {

                    //If app is unable to find any Security settings then user has to set screen lock manually
//                    textView.setText(resources.getString(R.string.setting_label))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LOCK_REQUEST_CODE -> if (resultCode == RESULT_OK) {
                //If screen lock authentication is success update text
                PreferenceHelper.save(Constants.IS_DEVICE_PIN, true)
                postUpdateQuickPin()
                startActivity(Intent(activity, HomeActivity::class.java))
                activity?.finish()
            } else {
                //If screen lock authentication is failed update text
//                textView.setText(resources.getString(R.string.unlock_failed))
            }
            SECURITY_SETTING_REQUEST_CODE ->                 //When user is enabled Security settings then we don't get any kind of RESULT_OK
                //So we need to check whether device has enabled screen lock or not
                if (isDeviceSecure()) {
                    //If screen lock enabled show toast and start intent to authenticate user
                    activity?.toast(resources.getString(R.string.device_is_secure))
                    authenticateApp()
                } else {
                    //If screen lock is not enabled just update text
//                    textView.setText(resources.getString(R.string.security_device_cancelled))
                }
            REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                //If screen lock authentication is success update text
                postUpdateQuickPin()
                startActivity(Intent(activity, HomeActivity::class.java))
                activity?.finish()
            } else {
                //If screen lock authentication is failed update text
//                textView.setText(resources.getString(R.string.unlock_failed))
            }
        }
    }

    private fun postUpdateQuickPin() {
        val deviceId = context?.let { AppUtils.getDeviceID(it) }
        val input = workDataOf(UIUtils.DEVICE_ID to deviceId)
        val worker = updateQuickPinOneTimeRequestWorker(input)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(context!!).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                        }
                        WorkInfo.State.FAILED -> {
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                    }
                }
            })
    }

    /**
     * method to return whether device has screen lock enabled or not
     */
    private fun isDeviceSecure(): Boolean {
        val keyguardManager = activity?.getSystemService(KEYGUARD_SERVICE) as KeyguardManager

        //this method only work whose api level is greater than or equal to Jelly_Bean (16)
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && keyguardManager.isKeyguardSecure

        //You can also use keyguardManager.isDeviceSecure(); but it requires API Level 23
    }
}
