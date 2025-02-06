package com.olam.warehouse.vegax.offloadingghana.ui.offline

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.PrintPreviewDialogFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils.RECEIVING_DATA
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingghana.R
import com.olam.warehouse.vegax.offloadingghana.databinding.ActivityVegaOffloadingGhanaOfflineSummaryBinding
import com.olam.warehouse.vegax.offloadingghana.databinding.ItemVagaOffloadingGhanaOfflineSummaryBinding
import com.olam.warehouse.vegax.offloadingghana.ui.VegaGhanaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingghana.ui.work.getMtnrOneTimeRequestWorker
import com.olam.warehouse.vegax.offloadingghana.utils.OFFLOADING_OFFLINE_SUMMARY_FRAG
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 7/20/2020.
 */
class VegaGhanaMTNROffloadingOfflineSummary : BaseFragment() {
    override val layoutResourceId = R.layout.activity_vega_offloading_ghana_offline_summary
    private val vm: VegaGhanaOffloadingViewModel by viewModel()
    private lateinit var binding: ActivityVegaOffloadingGhanaOfflineSummaryBinding
    private var offloadingItems = arrayListOf<VegaCoffeeReceivingMtnrWithLots>()
    private var transactionList = mutableListOf<VegaCoffeeReceivingMtnrWithLots>()
    private var callBack: CallBack? = null
    private var mReceiving = mutableListOf<VegaCoffeeReceiving>()

    companion object {
        fun newInstance() = VegaGhanaMTNROffloadingOfflineSummary().putArgs {
        }
    }

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            receivingData: VegaCoffeeReceivingMtnrWithLots
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ActivityVegaOffloadingGhanaOfflineSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingecuador/ui/offline/VegaEcuadorOffloadingOfflineSummaery")
            .title("Vega_Ecuador/offloading")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        vm.getTransactions()

        vm.offlineMtnr.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                transactionList.clear()
                transactionList.addAll(it)
            }
            updateUI(transactionList)
        })

        if (isOnline()) {
            binding.btnSyncProceed.isEnabled = true
            binding.btnSyncProceed.setBackgroundColor(
                ContextCompat.getColor(
                    binding.btnSyncProceed.context,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
        } else {
            binding.btnSyncProceed.isEnabled = false
            binding.btnSyncProceed.setBackgroundColor(
                ContextCompat.getColor(
                    binding.btnSyncProceed.context,
                    com.olam.warehouse.presentation.R.color.grey
                )
            )

        }
        binding.btnSyncProceed.setOnClickListener {
            val btnText = binding.btnSyncProceed.text
            if (btnText.contains("OK")) {
                vm.updateSyncedMtnrDeletedItem()
                showDeleteMessage()
//                activity?.onBackPressed()
            } else showDialog()
        }
    }

    private fun showDeleteMessage() {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_mtnr)
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.okconfirm),
                    isPositive = true
                )
            ) {
//                activity?.onBackPressed()
                activity?.finish()
            }
        }
    }

    private fun updateUI(data: MutableList<VegaCoffeeReceivingMtnrWithLots>) {
        data.let { receiving ->
            val offloadings = arrayListOf<VegaCoffeeReceivingMtnrWithLots>()
            when {
                receiving.isNotEmpty() -> {

                    offloadings.addAll(receiving)
                    setUpAdapter(offloadings)
                }
                else -> {
                    setUpAdapter(offloadings)
                    setErrorContentView("No data available")
                }
            }
        }

    }

    private fun showDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_offloading)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    startSync(offloadingItems, 0)
                },
                { dismiss() })
        }
    }

    private fun setUpAdapter(offloadingItems1: ArrayList<VegaCoffeeReceivingMtnrWithLots>) {
        offloadingItems.clear()
        val temp = offloadingItems1.filter { it.receiving.isOnlineData == false }
        offloadingItems.addAll(temp)
        changeBtn(offloadingItems)
        if (offloadingItems.size > 0) {
            binding.tvNoData.gone()
            binding.rvReceivingOffline.visible()
        } else {
            binding.tvNoData.visible()
            binding.rvReceivingOffline.gone()
        }
        binding.rvReceivingOffline.setUpAdapter(
            offloadingItems,
            R.layout.item_vaga_offloading_ghana_offline_summary,
            ItemVagaOffloadingGhanaOfflineSummaryBinding::inflate,
            { it, pos, bindItem ->
                val receiving = it.receiving
                bindItem.tvNoOfBagsTitle.text = "Dispatch Weight"
                bindItem.tvWeightText.text = "Receiving Weight"
                bindItem.tvNoOfBags.text = receiving.grossWeight.plus(" ").plus("MT")
                bindItem.tvWeight.text = receiving.netWeight.plus(" ").plus("MT")
                bindItem.tvProductType.text = receiving.materialName
                bindItem.tvWbId.text = receiving.tempWBId
                //viewStatus.isVisible = receiving.isSynced
                when (receiving.status) {
                    Status.RECEVING_COMPLETED -> {
                        bindItem.ivStatus.setImageDrawable(context.getDrawable(com.olam.warehouse.presentation.R.drawable.do_icon_completed))
                        bindItem.viewStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                        bindItem.tvError.gone()
                        bindItem.tvViewDetails.text = "Success"
                        bindItem.tvViewDetails.isEnabled = false
                    }
                    Status.SYNC_ERROR -> {
                        bindItem.tvViewDetails.isEnabled = true
                        bindItem.tvError.visible()
                        bindItem.tvError.text = receiving.syncStatusMsg
                        bindItem.ivStatus.setImageDrawable(context.getDrawable(com.olam.warehouse.presentation.R.drawable.ic_do_icon_error))
                        bindItem.viewStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                UIUtils.getSyncItemBackgroundColor(3)
                            )
                        )
                    }
                    else -> {
                        bindItem.ivStatus.setImageDrawable(context.getDrawable(com.olam.warehouse.presentation.R.drawable.icon_no_progresss))
                        bindItem.viewStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                        bindItem.tvError.gone()
                        bindItem.tvViewDetails.isEnabled = true
                    }
                }
                bindItem.ivDelete.setOnClickListener { view ->
                    showDeleteDialog(it)
                }
                bindItem.tvViewDetails.setOnClickListener { view ->
                    if (receiving.status != Status.RECEVING_COMPLETED) ViewDetails(it)
                    else {
                        val tallyKeys = ArrayList<String>()
                        tallyKeys.add(it.receiving.encodedImageContent ?: "")
                        showPreviewDialog(tallyKeys)
                    }
                }
            })
    }

    private fun ViewDetails(data: VegaCoffeeReceivingMtnrWithLots) {
        callBack?.replaceFragment(
            OFFLOADING_OFFLINE_SUMMARY_FRAG,
            data
        )
    }

    private fun showDeleteDialog(item: VegaCoffeeReceivingMtnrWithLots) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg_mtnr)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    offloadingItems.remove(item)
                    vm.deleteMtnrQuality(item.receiving.tempWBId!!)
                    vm.deleteStatus(item.receiving.mtnCode!!)
                    vm.updateOBD(item.receiving.mtnCode!!, false)
                },
                { dismiss() })
        }
    }

    var syncCount = 0
    private fun startSync(item: ArrayList<VegaCoffeeReceivingMtnrWithLots>, _index: Int) {
        PreferenceHelper.save(Constants.START_SYNC, true)
        val _element = item[_index]
        //grnList1.forEachIndexed { _index, _element ->
        val input = workDataOf(RECEIVING_DATA to _element.receiving.tempWBId)
        val worker = getMtnrOneTimeRequestWorker(input, _index)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val position = AppUtils.posExtension(workInfo.tags) + 1
                            syncCount++
                            if (syncCount != item.size) startSync(item, position)
                            if (syncCount == item.size) {
                                syncCount = 0
                                PreferenceHelper.save(Constants.START_SYNC, false)
                            }
                        }
                        WorkInfo.State.FAILED -> {
                            val position = AppUtils.posExtension(workInfo.tags) + 1
                            syncCount++
                            if (syncCount != item.size) startSync(item, position)
                            if (syncCount == item.size) {
                                syncCount = 0
                                PreferenceHelper.save(Constants.START_SYNC, false)
                            }
                        }
                        WorkInfo.State.RUNNING -> {
                            showLoading()
                        }
                        else -> {}
                    }
                }
            })
        if (_index == this.offloadingItems.size - 1) {
            changeBtn(item)
        }
    }

    fun changeBtn(item: ArrayList<VegaCoffeeReceivingMtnrWithLots>) {
        hideLoading()
        val count = offloadingItems.filter { it.receiving.status.equals(Status.RECEVING_COMPLETED) }
        if (offloadingItems.size == count.size)
            binding.btnSyncProceed.text = getString(R.string.ok)
        else if (offloadingItems.any { it.receiving.status.equals(Status.SYNC_ERROR) })
            binding.btnSyncProceed.text = getString(R.string.resync)
        else
            binding.btnSyncProceed.text = getString(com.olam.warehouse.presentation.R.string.sync)
    }

    private fun showPreviewDialog(tallyPrintKeys: ArrayList<String>) {
        val list = mutableListOf<String>()
        list.addAll(tallyPrintKeys)
        val dialogFragment = PrintPreviewDialogFragment(list)
        activity?.supportFragmentManager?.let { it1 -> dialogFragment.show(it1, "signature") }
    }

}
