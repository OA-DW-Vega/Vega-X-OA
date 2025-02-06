package com.olam.warehouse.vegax.portwarehouse.ui.incoming.offline

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtnGrades
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.ActivityIncommingMtnOfflineBinding
import com.olam.warehouse.vegax.portwarehouse.ui.incoming.PortIncomingMtnViewModel
import com.olam.warehouse.vegax.portwarehouse.ui.incoming.PortIncomingVerifyBaleActivity
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.GRADES
import com.olam.warehouse.vegax.portwarehouse.utils.getMtnOneTimeRequestWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/5/2021.
 */
class PortIncommingMtnOfflineActivity : HomeBaseActivity() {

    private var mtnOfflineList = mutableListOf<PortMtn?>()
    private var mAdapter = PortIncommingMtnOfflineAdapter({ moveMtnDeatils(it) }, { viewDetails(it) })

    private val vm: PortIncomingMtnViewModel by viewModel()
    private lateinit var binding: ActivityIncommingMtnOfflineBinding
    override val layoutResourceId = R.layout.activity_incomming_mtn_offline

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncommingMtnOfflineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("portwarehouse/ui/incoming/offline/PortIncommingMtnOfflineActivity")
            .title("Portwarehouse").with(tracker)
    }

    private fun initUI() {
        binding.rvOfflineMtn.layoutManager = LinearLayoutManager(this)
        binding.rvOfflineMtn.adapter = mAdapter
        if (isOnline()) {
            binding.btnSyncProceed.isEnabled = true
            binding.btnSyncProceed.setBackgroundColor(PortWHUtil.getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btnSyncProceed.isEnabled = false
            binding.btnSyncProceed.setBackgroundColor(PortWHUtil.getColor(R.color.grey))
        }
        binding.btnSyncProceed.setOnClickListener { showConfirmDialog() }

        val mtnBales = runBlocking {
            withContext(Dispatchers.IO)
            {
                vm.getOfflineMtnWithBales()
            }
        }
        mtnBales.forEach {
            it.mtn.baleCount = it.bales.filter { mtnBale -> mtnBale.isOfflineData }.size.toString()
            mtnOfflineList.add(it.mtn)
        }
        mAdapter.addItems(mtnOfflineList)

    }

    private fun showItemDeleteDialog(mtn: PortMtn?) {
        MaterialDialog(this).show {
            message(R.string.delete_msg)
            getMetirialCustomView(this, getString(R.string.proceed), getString(R.string.cancel), {
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
            val input = workDataOf(PortWHUtil.INCOMING_MTN to mtn!!.mtnNumber)
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
                                val position = PortWHUtil.posExtension(workInfo.tags)
                                mAdapter.setSyncStatus(
                                    position,
                                    true,
                                    4,
                                    workInfo.outputData.getString(PortWHUtil.MTN_OUTPUT_DATA)
                                )
                                toast("Sync success $position")
                            }
                            WorkInfo.State.FAILED -> {
                                hideLoading()
                                val position = PortWHUtil.posExtension(workInfo.tags)
                                mAdapter.setSyncStatus(
                                    position,
                                    false,
                                    3,
                                    workInfo.outputData.getString(PortWHUtil.MTN_OUTPUT_DATA)
                                )
                                toast("Sync failed $position")
                            }
                            WorkInfo.State.RUNNING -> {
                                val position = PortWHUtil.posExtension(workInfo.tags)
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

    private fun moveMtnDeatils(mtn: PortMtn?) {
        showItemDeleteDialog(mtn)
    }

    private fun viewDetails(mtn: PortMtn?) {
        val grades = mtn?.mtnNumber?.let {
            runBlocking { withContext(Dispatchers.IO)
            {
                vm.getGrades(it)
            }}

        }
        val intent = Intent(this, PortIncomingVerifyBaleActivity::class.java)
        intent.putExtra(PortWHUtil.MTN, mtn)
        intent.putParcelableArrayListExtra(GRADES, grades as ArrayList<PortMtnGrades>)
        intent.putExtra("ViewStatus", 2)
        startActivity(intent)
    }

    override fun onBackPressed() {
        moveToHome()
    }

    private fun moveToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            when (it.itemId) {
                android.R.id.home -> {
                    moveToHome()
                }
            }
        }
        return false
    }
}
