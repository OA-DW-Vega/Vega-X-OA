package com.olam.warehouse.vegax.portwarehouse.ui.success

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.SuccessActivityBinding
import com.olam.warehouse.vegax.portwarehouse.ui.dispatch.PortDispatchActivity
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.DISPATCH_SUCCESS
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.OFFLINE_SUCCESS
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.OFFLOAD_SUCCESS
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.SEAL_SUCCESS
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.SUCCESS
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class SuccessPortActivity : HomeBaseActivity() {

    override val layoutResourceId = R.layout.success_activity
    private lateinit var binding: SuccessActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SuccessActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/success/SuccessPortActivity")
            .title("Portwarehouse").with(tracker)
    }

    override fun onBackPressed() {
        when (intent.getStringExtra(SUCCESS)) {
            DISPATCH_SUCCESS, OFFLOAD_SUCCESS, OFFLINE_SUCCESS -> moveToHome()
            SEAL_SUCCESS -> moveToDispatchHome()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            when (it.itemId) {
                android.R.id.home -> {
                    when (intent.getStringExtra(SUCCESS)) {
                        DISPATCH_SUCCESS, OFFLOAD_SUCCESS, OFFLINE_SUCCESS -> moveToHome()
                        SEAL_SUCCESS -> moveToDispatchHome()
                    }
                }
            }
        }
        return false
    }

    private fun initUI() {
        when (intent.getStringExtra(SUCCESS)) {
            SEAL_SUCCESS -> initFragment(SuccessSealFragment(), bundle = intent.extras)
            else -> initFragment(SuccessFragment(), bundle = intent.extras)
        }
        binding.btnOk.setOnClickListener {

            when (intent.getStringExtra(SUCCESS)) {
                DISPATCH_SUCCESS, OFFLOAD_SUCCESS, OFFLINE_SUCCESS -> moveToHome()
                SEAL_SUCCESS -> moveToDispatchHome()
            }
        }
    }

    private fun moveToHome() {
        val intent = Intent(this, com.olam.warehouse.login.ui.home.HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun initFragment(fragment: Fragment, bundle: Bundle?) {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        fragment.arguments = bundle
        transaction.replace(R.id.mainContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun moveToDispatchHome() {
        startActivity(Intent(this, PortDispatchActivity::class.java))
    }
}
