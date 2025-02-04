package com.olam.warehouse.vegax.grnindo.ui.offline

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
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.work.getGrnOneTimeRequestWorker
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnindo.R
import com.olam.warehouse.vegax.grnindo.databinding.FragmentIndoCoffeeGrnOfflineBinding
import com.olam.warehouse.vegax.grnindo.ui.VegaIndoCoffeeGrnViewModel
import com.olam.warehouse.vegax.grnindo.utils.GRN_FRAG
import com.olam.warehouse.vegax.grnindo.utils.getColor
import kotlinx.android.synthetic.main.item_indo_coffee_grn_offline.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */
class VegaIndoCoffeeGrnOfflineSummary : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_indo_coffee_grn_offline
    private val vm: VegaIndoCoffeeGrnViewModel by viewModel()
    private lateinit var binding: FragmentIndoCoffeeGrnOfflineBinding
    private var grnList = arrayListOf<VegaGrnWeighBridgeId>()
    private var callBack: CallBack? = null

    companion object {
        fun newInstance() = VegaIndoCoffeeGrnOfflineSummary().putArgs {
        }
    }

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            wbDetails: VegaGrnWeighBridgeId
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentIndoCoffeeGrnOfflineBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnecuador/ui/offline/VegaEcuadorGrnOfflineSummary")
            .title("Vega_Ecuador/Grn")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        if (AppUtils.isOnline()) {
            binding.btnSyncProceed.isEnabled = true
            binding.btnSyncProceed.setBackgroundColor(
                getColor(
                    if (getCurrentOriginEntity().contains(
                            "OFI"
                        )
                    ) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green
                )
            )
        } else {
            binding.btnSyncProceed.isEnabled = false
            binding.btnSyncProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }
        binding.btnSyncProceed.setOnClickListener {
            val btnText = binding.btnSyncProceed.text
            if (btnText.contains("OK")) activity?.finish()
            else showDialog()
        }
        vm.weighBridgeOffline.observe(this, Observer { updateUI(it) })
        vm.getOfflineWeighBridgeDetail()
    }

    private fun updateUI(data: List<VegaGrnWeighBridgeId>) {
        data.let { quality ->
            val grnList = arrayListOf<VegaGrnWeighBridgeId>()
            when {
                quality.isNotEmpty() -> {
                    grnList.addAll(quality)
                    setUpAdapter(grnList)
                }
                else -> {
                    setUpAdapter(grnList)
                    setErrorContentView("No data available")
                }
            }
        }

    }

    private fun showDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_grn)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    startSync(grnList)
                },
                { dismiss() })
        }
    }

    private fun setUpAdapter(qtyList: ArrayList<VegaGrnWeighBridgeId>) {
        grnList = qtyList
        changeBtn(grnList)
        if (grnList.size > 0) {
            binding.tvNoData.gone()
            binding.rvOfflineGrn.visible()
        } else {
            binding.tvNoData.visible()
            binding.rvOfflineGrn.gone()
        }
        binding.rvOfflineGrn.setUp(
                grnList,
                R.layout.item_indo_coffee_grn_offline,
                { it, pos ->
                    val status = it.status ?: 1
                    tvGradeItem.text = it.materialName
                    tvWeighBridgeNo.text = it.weighBridgeId

                    val isError = it.isErrorStatus
                    if (isError) {
                        llError.gone()
                    } else {
                        tvErrorMsg.text = it.message
                        llError.visible()
                    }

                    ivStatus.setImageResource(UIUtils.getSyncStatusIcon(status))
                    vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                    context,
                                    UIUtils.getSyncItemBackgroundColor(status)
                            )
                    )
                    if (status == 4) {
                        //ivDeleteData.gone()
                        tvViewWeighBridge.isEnabled = false
                        tvViewWeighBridge.text = "Grn No \n".plus(it.grnNumber)
                    } else {
                        //ivDeleteData.visible()
                        tvViewWeighBridge.isEnabled = true
                        tvViewWeighBridge.text = context.getString(R.string.view_details)
                    }
                    tvViewWeighBridge.setOnClickListener { _ -> viewDetails(it) }
                    ivDeleteData.setOnClickListener { _ -> showDeleteDialog(it) }


                })
    }

    private fun viewDetails(it: VegaGrnWeighBridgeId) {
        callBack?.replaceFragment(
                GRN_FRAG, it
        )
    }

    private fun showDeleteDialog(item: VegaGrnWeighBridgeId) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    grnList.remove(item)
                    vm.updateDeletedItem(item.weighBridgeId.toString())
                },
                { dismiss() })
        }
    }

    private fun startSync(item: ArrayList<VegaGrnWeighBridgeId>) {
        item.forEachIndexed { _index, _element ->
            if (_element.status != 4) {
                val input = workDataOf(UIUtils.GRN_DATA to _element.weighBridgeId)
                val worker = getGrnOneTimeRequestWorker(input, _index)
                enQueueWorker(worker, requireContext())
                WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
                    .observe(this, Observer { workInfo ->
                        if (workInfo != null) {
                            when (workInfo.state) {
                                WorkInfo.State.SUCCEEDED -> {
                                    hideLoading()
                                    val position = workInfo.tags.first().toInt()
                                    //mAdapter.setSyncStatus(position, true, workInfo.outputData)
                                    if (_index == this.grnList.size - 1) {
                                        changeBtn(item)
                                    }
                                }
                                WorkInfo.State.FAILED -> {
                                    hideLoading()
//                                    val position = workInfo.tags.first().toInt()
                                    /*mAdapter.setSyncStatus(position, false, workInfo.outputData)
                                    vm.updateReceivingFailMsg(
                                        workInfo.outputData.getString(RECEIVING_OUTPUT_DATA).toString(),
                                        this.receivings[position].receiving.tmpWbId
                                    )*/
                                    if (_index == this.grnList.size - 1) {
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

    private fun changeBtn(item: ArrayList<VegaGrnWeighBridgeId>) {
        val count = grnList.filter { it.status == 4 }
        when {
            grnList.size == count.size -> binding.btnSyncProceed.text = getString(com.olam.warehouse.presentation.R.string.ok)
            grnList.any { it.status == 3 } -> binding.btnSyncProceed.text = getString(com.olam.warehouse.presentation.R.string.re_sync)
            else -> binding.btnSyncProceed.text = getString(com.olam.warehouse.presentation.R.string.sync)
        }
    }
}

