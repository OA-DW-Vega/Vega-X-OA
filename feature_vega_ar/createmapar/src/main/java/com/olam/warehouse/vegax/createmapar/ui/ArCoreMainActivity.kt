package com.olam.warehouse.vegax.createmapar.ui

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.*
import com.olam.warehouse.master.ui.BaseActivity
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.createmapar.R
import com.olam.warehouse.vegax.createmapar.data.domain.model.ArLotDetails
import com.olam.warehouse.vegax.createmapar.di.injectArFeature
import com.olam.warehouse.vegax.createmapar.ui.createmap.CreateMapFragment
import com.olam.warehouse.vegax.createmapar.ui.depositstock.DepositStockFragment
import com.olam.warehouse.vegax.createmapar.ui.pickupstock.LotDeatilsFragment
import com.olam.warehouse.vegax.createmapar.ui.pickupstock.PickUpStockFragment
import com.olam.warehouse.vegax.createmapar.utils.LOT_ID

/**
 * Created by Baskaran Kannan on 6/15/2021.
 */
class ArCoreMainActivity : BaseActivity() {
    override val layoutResourceId = R.layout.activity_arcore_main
    private val mTAG = ArCoreMainActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectArFeature()
        initUI()
    }

    private fun initUI() {
        when {
            (intent.getStringExtra(UIUtils.TYPE)
                ?: "").equals(getString(com.olam.warehouse.login.R.string.create_map)) -> {
                initNavigationView(true, getString(com.olam.warehouse.login.R.string.create_map))
                displayFragment(CreateMapFragment(), false)
            }
            (intent.getStringExtra(UIUtils.TYPE)
                ?: "").equals(getString(com.olam.warehouse.login.R.string.pickup_stock)) -> {
                initNavigationView(true, getString(com.olam.warehouse.login.R.string.pickup_stock))
                displayFragment(PickUpStockFragment(), false)
            }
            (intent.getStringExtra(UIUtils.TYPE)
                ?: "").equals(getString(com.olam.warehouse.presentation.R.string.lot_details)) -> {
                val lotDeatils = intent?.getParcelableExtra<ArLotDetails>(LOT_ID)
                initNavigationView(true, lotDeatils?.lotId.toString())
                displayFragment(LotDeatilsFragment(), false)
            }
            else -> {
                initNavigationView(true, getString(com.olam.warehouse.login.R.string.deposit_stock))
                displayFragment(DepositStockFragment(), false)
            }
        }

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
                        status.startResolutionForResult(this@ArCoreMainActivity, 0x1)
                    } catch (e: IntentSender.SendIntentException) {
//                        Log.d("Martin", "PendingIntent unable to execute request.")
                    }

                }
                //if (status.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) Log.d("Martin", "Location settings are inadequate, and cannot be fixed here. Dialog "+"not created.")
            }
        })
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flContainer,
            allowBackStack = flag
        )
    }

    override fun onBackPressed() {
        backNavigation()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                android.R.id.home -> {
                    backNavigation()
                    return true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun backNavigation() {
        /* when (val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)) {
             is CreateMapFragment -> {
                 super.onBackPressed()
             }
            else -> super.onBackPressed()
         }*/
        super.onBackPressed()
    }

    private fun initNavigationView(b: Boolean, title: String) {
        val toolBar = findViewById<Toolbar>(com.olam.warehouse.presentation.R.id.tool_bar)
        // val titleLogo = findViewById<AppCompatImageView>(com.olam.warehouse.presentation.R.id.abLogo)
        //if(!b)titleLogo.visibility = View.VISIBLE else titleLogo.visibility = View.GONE
        initToolbar(b, toolBar)
    }
}