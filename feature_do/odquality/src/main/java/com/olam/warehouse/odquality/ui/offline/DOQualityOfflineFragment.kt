package com.olam.warehouse.odquality.ui.offline

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
import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails
import com.olam.warehouse.master.dorigin.model.DOWeighBridgeWithQualityParams
import com.olam.warehouse.odquality.R
import com.olam.warehouse.odquality.databinding.FragmentDoQualityOfflineBinding
import com.olam.warehouse.odquality.ui.DOQualityViewModel
import com.olam.warehouse.odquality.utils.*
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.observeOnce
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */

class DOQualityOfflineFragment : BaseFragment() {

    private var wbId: String? = ""
    private var qualityOfflineList = mutableListOf<DOQualityWBDetails?>()
    private var qualityPostList = arrayListOf<DOQualityWBDetails>()
    private var mAdapter = DOQualityOfflineAdapter({ moveQualityParms(it) }, { viewDetails(it) })
    private lateinit var mListener: OnOfflineListener

    companion object {
        fun newInstance() = DOQualityOfflineFragment().putArgs {
        }
    }

    interface OnOfflineListener {
        fun onUpdateDeletedItem(wbid: String?)
        fun onViewDetails(weighBridge: DOQualityWBDetails?)
    }

    private val vm: DOQualityViewModel by viewModel()

    private lateinit var binding: FragmentDoQualityOfflineBinding
    override val layoutResourceId = R.layout.fragment_do_quality_offline

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDoQualityOfflineBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odquality/ui/offline/DOQualityOfflineFragment").title("OD/Quality")
            .with(tracker)
        initUI()
        initExtra()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnOfflineListener
    }


    private fun moveQualityParms(it: DOQualityWBDetails?) {
        showItemDeleteDialog(it)
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnSyncProceed, it, true)
        }
        binding.rvOfflineQuality.layoutManager = LinearLayoutManager(this.context)
        binding.rvOfflineQuality.adapter = mAdapter
        if (isOnline()) {
            binding.btnSyncProceed.isEnabled = true
            binding.btnSyncProceed.setBackgroundColor(
                getColor(
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
        } else {
            binding.btnSyncProceed.isEnabled = false
            binding.btnSyncProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }
        binding.btnSyncProceed.setOnClickListener {
            val btnText = binding.btnSyncProceed.text
            if (btnText.contains("OK")) activity?.finish()
            else showConfirmDialog()
        }
        vm.wbWithParams.observeOnce(this, Observer { enableOfflineLabel(it) })
        vm.getWBWithQuality("")
    }

    private fun initExtra() {
        wbId = arguments?.getString(WEIGHBRIDGE_LIST)
    }

    private fun enableOfflineLabel(response: List<DOWeighBridgeWithQualityParams>) {
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

    private fun viewDetails(weighBridge: DOQualityWBDetails?) {
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

    private fun showItemDeleteDialog(quality: DOQualityWBDetails?) {
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

    fun onSyncProceed(qualityOfflineList: MutableList<DOQualityWBDetails?>) {
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
                                    if (_index == qualityOfflineList.size - 1) {
                                        changeBtn(qualityOfflineList)
                                    }
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
                                    if (_index == qualityOfflineList.size - 1) {
                                        changeBtn(qualityOfflineList)
                                    }
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
    }

    fun changeBtn(qualityOfflineList: MutableList<DOQualityWBDetails?>) {
        val count = mAdapter.getItems().filter { it?.status!!.equals(4) }
        if (qualityOfflineList.size == count.size) binding.btnSyncProceed.text = getString(R.string.ok)
        else binding.btnSyncProceed.text = getString(R.string.resync)
    }

}
