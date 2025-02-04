package com.olam.warehouse.vegax.qualityindiacoffee.ui.offline

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.observeOnce
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityindiacoffee.R
import com.olam.warehouse.vegax.qualityindiacoffee.databinding.FragmentVegaIndiaCoffeeQualityOfflineBinding
import com.olam.warehouse.vegax.qualityindiacoffee.ui.VegaIndiaCoffeeQualityViewModel
import com.olam.warehouse.vegax.qualityindiacoffee.utils.*
import com.olam.warehouse.vegax.qualityindiacoffee.work.getQualityOneTimeRequestWorker
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaIndiaCoffeeQualityOfflineFragment : BaseFragment() {

    private var wbId: String? = ""
    private var qualityOfflineList = mutableListOf<VegaQualityWBDetails?>()
    private var qualityPostList = arrayListOf<VegaQualityWBDetails>()
    private var mAdapter =
        VegaIndiaCoffeeQualityOfflineAdapter({ moveQualityParms(it) }, { viewDetails(it) })
    private lateinit var mListener: OnOfflineListener

    companion object {
        fun newInstance() = VegaIndiaCoffeeQualityOfflineFragment().putArgs {
        }
    }

    interface OnOfflineListener {
        fun onUpdateDeletedItem(wbid: String?)
        fun onViewDetails(weighBridge: VegaQualityWBDetails?)
    }

    private val vm: VegaIndiaCoffeeQualityViewModel by viewModel()

    private lateinit var binding: FragmentVegaIndiaCoffeeQualityOfflineBinding
    override val layoutResourceId = R.layout.fragment_vega_india_coffee_quality_offline

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaIndiaCoffeeQualityOfflineBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("quality/ui/offline/VegaQualityOfflineFragment").title("Quality").with(tracker)
        initUI()
        initExtra()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnOfflineListener
    }


    private fun moveQualityParms(it: VegaQualityWBDetails?) {
        showItemDeleteDialog(it)
    }

    private fun initUI() {
        binding.rvOfflineQuality.layoutManager = LinearLayoutManager(this.context)
        binding.rvOfflineQuality.adapter = mAdapter
        if (isOnline()) {
            binding.btnSyncProceed.isEnabled = true
            binding.btnSyncProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnSyncProceed.isEnabled = false
            binding.btnSyncProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }
        binding.btnSyncProceed.setOnClickListener {
            if (binding.btnSyncProceed.text.equals(getString(R.string.ok))) activity?.finish()
            else showConfirmDialog()
        }
        vm.wbWithParams.observeOnce(this, Observer { enableOfflineLabel(it) })
        vm.getWBWithQuality("")
    }

    private fun initExtra() {
        wbId = arguments?.getString(WEIGHBRIDGE_LIST)
    }

    private fun enableOfflineLabel(response: List<VegaWeighBridgeWithQualityParams>) {
        response.let { offlineList ->
            //vm.qualityOfflineList.removeObserver(this)
            qualityOfflineList.clear()
            offlineList.forEach {
                it.qualityWBDetails.qualityDetails = it.quality
                qualityOfflineList.add(it.qualityWBDetails)
            }
            mAdapter.addItems(qualityOfflineList)
        }
    }

    private fun viewDetails(weighBridge: VegaQualityWBDetails?) {
        mListener.onViewDetails(weighBridge)
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.sync_all)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (binding.btnSyncProceed.text.equals(getString(R.string.resync))) onSyncProceed(
                        mAdapter.getItems()
                    )
                    else onSyncProceed(qualityOfflineList)
                },
                { dismiss() })
        }
    }

    private fun showItemDeleteDialog(quality: VegaQualityWBDetails?) {
        MaterialDialog(requireContext()).show {
            message(R.string.delete_msg)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    qualityOfflineList.remove(quality)
                    mListener.onUpdateDeletedItem(quality?.weighBridgeId)
                    mAdapter.removeItems(quality)
                },
                { dismiss() })
        }
    }

    fun onSyncProceed(qualityOfflineList: MutableList<VegaQualityWBDetails?>) {
        qualityOfflineList.forEachIndexed { _index, weighBridge ->
            if (weighBridge?.status != 4) {
                if (weighBridge!!.isNotWBID) return@forEachIndexed
                val input = workDataOf(QUALITY_DATA to weighBridge.weighBridgeId)
                val worker = getQualityOneTimeRequestWorker(input, _index)
                enQueueWorker(worker, requireContext())
                showLoading()
                WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
                    .observe(viewLifecycleOwner, Observer { workInfo ->

                        if (workInfo != null) {
                            when (workInfo.state) {
                                WorkInfo.State.SUCCEEDED -> {
                                    hideLoading()
                                    val position = posExtension(workInfo.tags)
                                    mAdapter.setSyncStatus(
                                        position,
                                        true,
                                        4,
                                        workInfo.outputData.getString(QUALITY_OUTPUT_DATA)
                                    )
                                    UIUtils.showErrorDialog(requireContext(), "Sync success $position")
                                    //context?.toast("Sync success $position")
                                }
                                WorkInfo.State.FAILED -> {
                                    hideLoading()
                                    val position = posExtension(workInfo.tags)
                                    mAdapter.setSyncStatus(
                                        position,
                                        false,
                                        3,
                                        workInfo.outputData.getString(QUALITY_OUTPUT_DATA)
                                    )
                                    vm.updateWBMessage(
                                        workInfo.outputData.getString(QUALITY_OUTPUT_DATA),
                                        qualityOfflineList[position]?.weighBridgeId
                                    )
                                    UIUtils.showErrorDialog(requireContext(), "Sync failed $position")
                                    // context?.toast("Sync failed $position")
                                }
                                WorkInfo.State.RUNNING -> {
                                    val position = posExtension(workInfo.tags)
                                    mAdapter.setSyncStatus(position, true, 2, "")
                                    showLoading()
                                }
                                else -> {
                                    //nothing is selected
                                }
                            }
                        }

                    })
                hideLoading()
                if (_index == qualityOfflineList.size - 1) {
                    val count = mAdapter.getItems().filter { it?.status == 4 }.size
                    if (qualityOfflineList.size == count) binding.btnSyncProceed.text = getString(R.string.ok)
                    else binding.btnSyncProceed.text = getString(R.string.resync)
                }
            }
        }
    }

}
