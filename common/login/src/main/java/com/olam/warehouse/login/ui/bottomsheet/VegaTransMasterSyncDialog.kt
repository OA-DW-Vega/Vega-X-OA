package com.olam.warehouse.login.ui.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.BottomSheetTransMasterBinding
import com.olam.warehouse.master.common.data.work.TransMasterDataFetchWorker
import com.olam.warehouse.master.common.model.TransCountModel
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.Constants.TRANS_LAST_SYNC
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.DateUtils.getDifferenceBtMilliSconds
import com.olam.warehouse.presentation.utils.DateUtils.getLastInventorySyncTime
import com.olam.warehouse.presentation.utils.DateUtils.getTransLastSyncTime
import com.olam.warehouse.presentation.utils.extension.*

/**
 * Created by Baskaran Kannan on 10/15/2021.
 */
class VegaTransMasterSyncDialog: BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetTransMasterBinding
    private var isSyncDone: Boolean = false
    private lateinit var updateStatus: (Boolean) -> Unit
    companion object {
        fun newInstance(updateStatus: (Boolean) -> Unit): VegaTransMasterSyncDialog {
            val fragment = VegaTransMasterSyncDialog()
            fragment.updateStatus = updateStatus
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = BottomSheetTransMasterBinding.inflate(layoutInflater)
        dialog?.setCanceledOnTouchOutside(false)
        //updateStatus.invoke(false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return super.onCreateDialog(savedInstanceState).apply {
            // window?.setDimAmount(0.2f) // Set dim amount here
            setOnShowListener {
                val bottomSheet =
                    findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
                bottomSheet.setBackgroundResource(android.R.color.transparent)

//                this.window?.findViewById<View>(R.id.touch_outside)?.setOnClickListener(null)
                //this prevents dragging behavior
//                 (this.window?.findViewById<View>(R.id.design_bottom_sheet)?.layoutParams as CoordinatorLayout.LayoutParams).behavior = null
            }
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding.tvClose.setOnClickListener {
            try {
                dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val netType = context?.networkType()
        val netSpeed =
            if (netType?.contains("WIFI") == true) context?.getWifiLevel().toString()
                .plus(" kbps") else context?.networkDownloadSpeed()
        binding.tvNtSpeed.text = netSpeed.toString()
        binding.tvNtType.text = netType
        if (!getTransLastSyncTime().toString().equals("") && !getTransLastSyncTime().toString()
                .equals("0")
        )
            binding.tvSync.text = getLastInventorySyncTime(
                activity?.applicationContext!!,
                getTransLastSyncTime().toString()
            )
        syncTransMaster()
    }

    private fun syncTransMaster() {
        WorkManager.getInstance(activity?.applicationContext!!).cancelAllWork()
//        showCustomLoading(getString(R.string.syncing))
        //setTransLastSyncTime()
        binding.clProgress.visible()
        makeStatusNotification(getString(R.string.data_downloading), activity?.applicationContext!!)
        try {
            val worker =
                OneTimeWorkRequest.Builder(TransMasterDataFetchWorker::class.java)
                    .setConstraints(constraintWithoutNetwork)
                    .build()
            enQueueUniqueWorker(
                worker,
                UNIQUE_ONE_TIME_WORKER_TRANS,
                activity?.applicationContext!!
            )
            WorkManager.getInstance(activity?.applicationContext!!)
                .getWorkInfoByIdLiveData(worker.id)
                .observe(this, Observer { workInfo ->
                    workInfo?.let { info ->
                        when (info.state) {
                            WorkInfo.State.SUCCEEDED -> {

                                activity?.toast(getString(R.string.data_download_success))
                                dismissNotification(activity?.applicationContext!!)
                                var advanceCount = ""
                                workInfo.outputData.getString(TRANS_OUTPUT_DATA)
                                    ?.let {
                                        advanceCount = it
                                    }
//                                hideCustomLoading()
//                                setOffline()
                                binding.clProgress.gone()
                                binding.clTransData.visible()
                                binding.tvStatusValue.text =
                                    getString(R.string.data_download_success)
                                binding.tvStatusValue.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        com.olam.warehouse.presentation.R.color.green
                                    )
                                )
                                val syncTime = PreferenceHelper.get(Constants.TRANS_FIRST_SYNC, 0L)
                                binding.tvTimeTakenValue.text =
                                    getDifferenceBtMilliSconds(syncTime, getCurrentTimeInMills())
                                PreferenceHelper.save(TRANS_LAST_SYNC, syncTime)
                                if (!getTransLastSyncTime().toString()
                                        .equals("") && !getTransLastSyncTime().toString()
                                        .equals("0")
                                )
                                    binding.tvSync.text = getLastInventorySyncTime(
                                        activity?.applicationContext!!,
                                        getTransLastSyncTime().toString()
                                    )
                                isSyncDone = true
                                updateStatus(isSyncDone)
                                val transList = Gson().fromJson<List<TransCountModel>>(
                                    PreferenceHelper.get(
                                        Constants.TRANS_LIST,
                                        ""
                                    )
                                )
                                updateItemCounts(transList, advanceCount)
                            }
                            WorkInfo.State.FAILED -> {
//                                fliptab.selectRightTab(true)
                                workInfo.outputData.getString(TRANS_OUTPUT_DATA)
                                    ?.let {
                                        binding.tvStatusValue.text = getErrorString(it)
                                           // if (it.isNotEmpty()) "Error:".plus(it) else "Error"
                                        binding.tvStatusValue.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                com.olam.warehouse.presentation.R.color.red
                                            )
                                        )
                                        activity?.toast(it)
                                    }
                                dismissNotification(activity?.applicationContext!!)
//                                hideCustomLoading()
                                val syncTime = PreferenceHelper.get(Constants.TRANS_FIRST_SYNC, 0L)
                                binding.tvTimeTakenValue.text =
                                    getDifferenceBtMilliSconds(syncTime, getCurrentTimeInMills())
                                PreferenceHelper.save(Constants.TRANS_FIRST_SYNC, 0L)
                                binding.clProgress.gone()
                                isSyncDone = false
                                updateStatus(isSyncDone)
                                updateItemCounts(emptyList(), "")
                            }
                            WorkInfo.State.CANCELLED -> {
//                                fliptab.selectRightTab(true)
                                workInfo.outputData.getString(TRANS_OUTPUT_DATA)
                                    ?.let {
                                        binding.tvStatusValue.text =
                                            if (it.isNotEmpty()) it else "Error"
                                        binding.tvStatusValue.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                com.olam.warehouse.presentation.R.color.red
                                            )
                                        )
                                        activity?.toast(it)
                                    }
                                dismissNotification(activity?.applicationContext!!)
                                val syncTime = PreferenceHelper.get(Constants.TRANS_FIRST_SYNC, 0L)
                                binding.tvTimeTakenValue.text =
                                    getDifferenceBtMilliSconds(syncTime, getCurrentTimeInMills())
                                PreferenceHelper.save(Constants.TRANS_FIRST_SYNC, 0L)
                                binding.clProgress.gone()
//                                hideCustomLoading()
                                isSyncDone = false
                                updateStatus(isSyncDone)

                            }
                            else -> {
                                dismissNotification(activity?.applicationContext!!)
                            }
                        }
                    }

                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getErrorString(it: String): String{
        return if (it.isNotEmpty()) "Error:".plus(
            if(it.contains("Failed to allocate", true)) "Maximum response size reached" else it) else "Error"
    }

    private fun updateItemCounts(transList: List<TransCountModel>, count: String) {
        if (transList.size > 0) {
            binding.tvPOCount.text =
                transList.get(0).poCount.toString().plus(" items")
            binding.tvInvoiceCount.text =
                transList.get(0).invoiceCount.toString().plus(" items")
            binding.tvGrnReprintCount.text =
                transList.get(0).grnReprintCount.toString().plus(" items")
            binding.tvGrnPriceCount.text =
                transList.get(0).grnPriceCount.toString().plus(" items")
            binding.tvCharDetailsCount.text =
                transList.get(0).charDetailsCount.toString().plus(" items")
            binding.tvAdvanceCount.text =
                transList.get(0).advanceCount.toString()
                    .plus(" items")/*.plus("-"+count)*/
            binding.tvVendorCreditCount.text =
                transList.get(0).vendorCreditCount.toString().plus(" items")
            binding.tvStocksCount.text =
                transList.get(0).stocksCount.toString().plus(" items")
        }
    }


}


