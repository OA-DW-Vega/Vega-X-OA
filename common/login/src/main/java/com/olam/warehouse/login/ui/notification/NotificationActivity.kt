package com.olam.warehouse.login.ui.notification

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import com.olam.warehouse.login.R
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.presentation.utils.fromJson

/**
 * Created by Baskaran Kannan on 1/27/2022.
 */
class NotificationActivity : HomeBaseActivity(), NotificationFragment.CallBack {

    override val layoutResourceId = R.layout.activity_notification
    private var menu: Menu? = null
    var isReadTab = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        initUI()
        replaceFragment(
            NotificationFragment(),
            "",
            allowStateLoss = true,
            containerViewId = R.id.flNotification
        )
    }

    private fun initUI() {
        val toolBar = findViewById<Toolbar>(com.olam.warehouse.master.R.id.tool_bar)
        val notificationLayout =
            toolBar.findViewById<ConstraintLayout>(com.olam.warehouse.master.R.id.clNotification)
        notificationLayout.gone()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.notification_menu, menu)
        this.menu = menu
        updateTabStatus(false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            val fragment = supportFragmentManager.findFragmentById(R.id.flNotification)
            when (it.itemId) {
                1 -> {
                    when(fragment){
                        is NotificationFragment -> fragment.clearNotification()
                    }
                    return true
                }
                2 -> {
                    when(fragment){
                        is NotificationFragment -> fragment.makeAllMessageToReadState()
                    }
                    return true
                }

                else -> super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun updateTabStatus(isReadTab: Boolean) {
        this.isReadTab = isReadTab
        if(isReadTab) {
            menu?.clear()
            menu?.add(0,1,1, getString(R.string.delete_all))
        }else{
            menu?.clear()
            menu?.add(0,1,1, getString(R.string.delete_all))
            menu?.add(0,2,2, getString(R.string.read_all))

        }
    }
}