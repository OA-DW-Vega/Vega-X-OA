package com.olam.warehouse.ginning.ui.incominglots.incomingmtn.offline

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.ginning.ui.incominglots.incomingmtn.GinningIncomingMtnViewModel
import com.olam.warehouse.ginning.ui.incominglots.incomingmtn.GinningIncomingVerifyBaleActivity
import com.olam.warehouse.ginning.utils.*
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Mtn
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnGrades
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.enQueueWorker
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.getMtnOneTimeRequestWorker
import kotlinx.android.synthetic.main.activity_ginning_incoming_mtn_offline.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class GinningIncomingMtnOfflineActivity : HomeBaseActivity() {

    private var mtnOfflineList = mutableListOf<Mtn?>()
    private var mAdapter =
        GinningIncomingMtnOfflineAdapter({ moveMtnDeatils(it) }, { viewDetails(it) })

    private val vm: GinningIncomingMtnViewModel by viewModel()
    override val layoutResourceId = R.layout.activity_ginning_incoming_mtn_offline

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // injectGinningIncomingLOTsIncomingMtnFeature()
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("ginningwarehouse/ui/incomingmtn/offline/GinningIncomingMtnOfflineActivity")
            .title("Ginningwarehouse").with(tracker)
    }

    private fun initUI() {
        rvOfflineMtn.layoutManager = LinearLayoutManager(this)
        rvOfflineMtn.adapter = mAdapter
        if (isOnline()) {
            btnSyncProceed.isEnabled = true
            btnSyncProceed.setBackgroundColor(getColorUtil(R.color.green))
        } else {
            btnSyncProceed.isEnabled = false
            btnSyncProceed.setBackgroundColor(getColorUtil(R.color.grey))
        }
        btnSyncProceed.setOnClickListener { showConfirmDialog() }
        vm.getOfflineMtnWithBales()
        vm.getOfflineMtnWithBales.observe(this, Observer {mtnBales ->
            mtnBales.forEach {
                it.mtn.baleCount = it.bales.filter { mtnBale -> mtnBale.isOfflineData }.size.toString()
                mtnOfflineList.add(it.mtn)
            }
            mAdapter.addItems(mtnOfflineList)

        })

    }

    private fun showItemDeleteDialog(mtn: Mtn?) {
        MaterialDialog(this).show {
            message(R.string.delete_msg)
            getMetirialCustomView(this, getString(R.string.confirm), getString(R.string.cancel), {
                vm.updateofflineMtnRevertStatus(mtn!!.mtnNumber)
                mtnOfflineList.remove(mtn)
                mAdapter.removeItems(mtn)
            }, { dismiss() })
        }
    }

    private fun showConfirmDialog() {
        MaterialDialog(this).show {
            message(R.string.sync)
            getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                { onSyncProceed() },
                { dismiss() })
        }
    }

    private fun onSyncProceed() {

        mtnOfflineList.forEachIndexed { _index, mtn ->
            val input = workDataOf(INCOMING_MTN to mtn!!.mtnNumber)
            val worker =
                getMtnOneTimeRequestWorker(input, _index)
            enQueueWorker(worker, this)
            showLoading()
            WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
                .observe(this, Observer { workInfo ->

                    if (workInfo != null) {
                        when (workInfo.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                hideLoading()
                                val position = posExtension(workInfo.tags)
                                mAdapter.setSyncStatus(
                                    position,
                                    true,
                                    4,
                                    workInfo.outputData.getString(MTN_OUTPUT_DATA)
                                )
                                toast("Sync success $position")
                            }
                            WorkInfo.State.FAILED -> {
                                hideLoading()
                                val position = posExtension(workInfo.tags)
                                mAdapter.setSyncStatus(
                                    position,
                                    false,
                                    3,
                                    workInfo.outputData.getString(MTN_OUTPUT_DATA)
                                )
                                toast("Sync failed $position")
                            }
                            WorkInfo.State.RUNNING -> {
                                val position = posExtension(workInfo.tags)
                                mAdapter.setSyncStatus(position, true, 2, "")
                                showLoading()
                            }
                            else -> {
                            }
                        }
                    }

                })
            hideLoading()
        }
    }

    private fun moveMtnDeatils(mtn: Mtn?) {
        showItemDeleteDialog(mtn)
    }

    private fun viewDetails(mtn: Mtn?) {
        val grades = mtn?.mtnNumber?.let { vm.getGrades(it) }
        val intent = Intent(this, GinningIncomingVerifyBaleActivity::class.java)
        intent.putExtra(MTN, mtn)
        intent.putParcelableArrayListExtra(GRADES, grades as ArrayList<MtnGrades>)
        intent.putExtra("ViewStatus", 2)
        startActivity(intent)
    }

    override fun onBackPressed() {
        moveToHome()
    }

    private fun moveToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                android.R.id.home -> {
                    moveToHome()
                }
            }
        }
        return false
    }
}
