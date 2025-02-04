package com.olam.warehouse.odreceiving.ui.offline

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.common.model.TransactionListPojo
import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.master.dorigin.entity.DOReceivingLineItem
import com.olam.warehouse.master.dorigin.model.DOReceivingWithLineItems
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.databinding.ActivityDoReceivingOfflineSummaryBinding
import com.olam.warehouse.odreceiving.ui.DOReceivingViewModel
import com.olam.warehouse.odreceiving.ui.summary.DOReceivingSummaryActivity
import com.olam.warehouse.odreceiving.utils.RECEIVING_DATA
import com.olam.warehouse.odreceiving.utils.RECEIVING_OUTPUT_DATA
import com.olam.warehouse.odreceiving.utils.getReceivingOneTimeRequestWorker
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.observeOnce
import com.olam.warehouse.vegax.App
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
class DOReceivingOfflineSummaryActivity : HomeBaseActivity() {

    private val vm: DOReceivingViewModel by viewModel()
    private val receivings = arrayListOf<DOReceivingWithLineItems>()
    private var mAdapter = DOReceivingOfflineSummaryAdapter({ deleteItem(it) }, { viewDetails(it) })

    private lateinit var binding: ActivityDoReceivingOfflineSummaryBinding
    override val layoutResourceId = R.layout.activity_do_receiving_offline_summary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoReceivingOfflineSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odreceiving/ui/offline/DOReceivingOfflineSummaryActivity").title("OD/Receiving")
            .with(tracker)
    }

    private fun initUI() {
        this.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnSyncProceed, it, true)
        }
        vm.receiveWithLineItemLocal.observeOnce(this, Observer {
            if (it != null) {
                updateUI(it)
            }
        })
        vm.getReceivingWithLineItem()
        binding.rvReceivingOffline.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rvReceivingOffline.adapter = mAdapter

        if (isOnline()) {
            binding.btnSyncProceed.isEnabled = true
            binding.btnSyncProceed.setBackgroundColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.green
                )
            )
        } else {
            binding.btnSyncProceed.isEnabled = false
            binding.btnSyncProceed.setBackgroundColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.grey
                )
            )

        }
        binding.btnSyncProceed.setOnClickListener {
            val btnText = binding.btnSyncProceed.text
            if (btnText.contains("OK")) finish()
            else showDialog()
        }
    }

    private fun updateUI(data: List<DOReceivingWithLineItems>?) {
        data?.let { receiving ->
            when {
                receiving.isNotEmpty() -> {
                    receivings.clear()
                    receivings.addAll(receiving)
                    mAdapter.addItems(receivings)
                }
                else -> setErrorContentView(getString(R.string.no_data_available))
            }
        }
//        if(data?.size ==0) setErrorContentView("No data available")
    }

    private fun showDialog() {
        MaterialDialog(this).show {
            message(R.string.confirm_message_receive_sync)
            getMetirialCustomView(this, getString(R.string.confirm), getString(R.string.cancel), {
                if (binding.btnSyncProceed.text.equals(getString(R.string.resync))) startSync(
                    mAdapter.getItems()
                )
                else startSync(receivings)
            }, { dismiss() })
        }
    }

    private fun startSync(item: ArrayList<DOReceivingWithLineItems>) {
//        item.forEach {
//        }


        val gson = Gson()
        item.forEachIndexed { _index, _element ->
            if (!_element.receiving.status.equals(Status.RECEVING_COMPLETED)) {
                val input = workDataOf(RECEIVING_DATA to gson.toJson(_element))
                val worker = getReceivingOneTimeRequestWorker(input, _index)
                enQueueWorker(worker, this)
                WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
                    .observe(this, Observer { workInfo ->
                        if (workInfo != null) {
                            when (workInfo.state) {
                                WorkInfo.State.SUCCEEDED -> {
                                    hideLoading()
                                    val position = workInfo.tags.first().toInt()
                                    mAdapter.setSyncStatus(position, true, workInfo.outputData)
                                    if (_index == this.receivings.size - 1) {
                                        changeBtn(item)
                                    }
                                }
                                WorkInfo.State.FAILED -> {
                                    hideLoading()
                                    val position = workInfo.tags.first().toInt()
                                    mAdapter.setSyncStatus(position, false, workInfo.outputData)
                                    vm.updateReceivingFailMsg(
                                        workInfo.outputData.getString(RECEIVING_OUTPUT_DATA).toString(),
                                        this.receivings[position].receiving.tmpWbId
                                    )
                                    if (_index == this.receivings.size - 1) {
                                        changeBtn(item)
                                    }
                                }
                                WorkInfo.State.RUNNING -> showLoading()
                                else -> {
                                }
                            }
                        }

                    })
                hideLoading()
            }
        }
    }

    fun changeBtn(item: ArrayList<DOReceivingWithLineItems>) {
        val count = mAdapter.getItems().filter { it.receiving.status.equals(Status.RECEVING_COMPLETED) }
        if (this.receivings.size == count.size) binding.btnSyncProceed.text = getString(R.string.ok)
        else binding.btnSyncProceed.text = getString(R.string.resync)
    }

    private fun deleteItem(it: DOReceivingWithLineItems?) {
        showDeleteDialog(it)
    }

    private fun viewDetails(it: DOReceivingWithLineItems?) {
        getDoTxnDetail(it?.receiving!!, it)
    }

    private fun getDoTxnDetail(
        receiving: DOReceiving,
        doReceivingWithLineItems: DOReceivingWithLineItems
    ) {
        val str: String = PreferenceHelper.get("txnData", "")
        if (str != "") {
            val transPojo = Gson().fromJson(str, TransactionListPojo::class.java)
            transPojo?.list?.let {
                vm.getProducts()
                vm.product.observe(this, Observer { materials ->
                    for (doDetail in it) {
                        val hasMaterialId = materials.any { material -> material.materialCode == doDetail.materialId }
                        if (hasMaterialId) {
                            doDetail.unitsOfMeasure = materials.first { material -> material.materialCode == doDetail.materialId }.unitsOfMeasure

                            if (doDetail.lotTransactionId == receiving.txnId) {
//                                val intent = Intent(this, DOReceivingWeighActivity::class.java)
                                val intent = Intent(this, DOReceivingSummaryActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                intent.putExtra(RECEIVING_DATA, receiving)
                                intent.putExtra("doTxnDetail", doDetail)
                                Log.i("lineItems", doReceivingWithLineItems.toString())
                                intent.putParcelableArrayListExtra("postDataOffline", doReceivingWithLineItems.lineItems as ArrayList<DOReceivingLineItem>)
                                startActivity(intent)
                                break
                            }
                        }
                    }
                })
            }
        }
    }

    private fun showDeleteDialog(item: DOReceivingWithLineItems?) {
        MaterialDialog(this).show {
            message(R.string.delete_msg)
            getMetirialCustomView(this, getString(R.string.confirm), getString(R.string.cancel), {
                receivings.remove(item)
                mAdapter.removeItems(item)
                vm.updateDeletedItem(item?.receiving?.tmpWbId, item?.receiving?.txnId)
            }, { dismiss() })

        }
    }
}
