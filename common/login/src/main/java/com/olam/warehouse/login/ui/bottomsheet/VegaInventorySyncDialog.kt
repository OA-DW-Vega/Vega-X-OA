package com.olam.warehouse.login.ui.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.VegaInventoryBottomSheetBinding
import com.olam.warehouse.master.common.data.work.InventorySyncWorker
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.PLANT_ID
import com.olam.warehouse.presentation.utils.AppUtils.posExtension
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.presentation.utils.extension.visible
import kotlinx.android.synthetic.main.item_vega_inventory_sync.view.*

/**
 * Created by Baskaran Kannan on 4/9/2020.
 */
class VegaInventorySyncDialog : BottomSheetDialogFragment() {

    private var syncPlantList = ArrayList<Plant>()
    val gson = GsonUtils()

    companion object {
        fun newInstance(syncStatusList: ArrayList<Plant>) = VegaInventorySyncDialog().putArgs {
            putParcelableArrayList("SYNC_PLANT_DATA", syncStatusList)
        }
    }

    private lateinit var binding: VegaInventoryBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = VegaInventoryBottomSheetBinding.inflate(layoutInflater)
        dialog?.setCanceledOnTouchOutside(false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return super.onCreateDialog(savedInstanceState).apply {
            // window?.setDimAmount(0.2f) // Set dim amount here
            setOnShowListener {
                val bottomSheet =
                    findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
                bottomSheet.setBackgroundResource(android.R.color.transparent)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        syncPlantList = arguments?.getParcelableArrayList<Plant>("SYNC_PLANT_DATA")!!
        setUpAdapter(syncPlantList)
        binding.tvClose.setOnClickListener { dialog?.dismiss() }
    }

    private fun setUpAdapter(syncStatusList: java.util.ArrayList<Plant>) {
        binding.rvSyncDetails.setUp(syncStatusList, R.layout.item_vega_inventory_sync, { it, pos ->
            tvPlantId.text = it.plantId
            if (it.status?.isNotEmpty()!!) {
                val stamsg = it.status!!.split("#", ".")
                var data = ""
                if (stamsg.size > 1) {
                    data = DateUtils.getLastInventorySyncTime(context, stamsg.get(1))
                    tvStatus.text = stamsg.toString().replace("" + stamsg[1], data).replace("[", "").replace("]", "")
                        .replace(", 0", ". 0")
                } else {
                    tvStatus.text = stamsg.toString().replace("[", "").replace("]", "")
                }

                llStatus.visible()
            } else {
                llStatus.gone()
            }

            if (it.updatedDateTime?.isEmpty()!!) {
                tvSyncTime.text = "-"
            } else {
                tvSyncTime.text = it.updatedDateTime?.let { it1 ->
                    DateUtils.getLastInventorySyncTime(context, it1)
                }
            }
            ivSync.setOnClickListener { view -> inventorySyncData(it.plantId, pos) }

        })
    }

    private fun inventorySyncData(plantId: String, tag: Int) {
        binding.probressBar.visible()
        WorkManager.getInstance(activity?.applicationContext!!).cancelAllWork()
        try {
            val input = workDataOf(PLANT_ID to plantId)
            val worker = OneTimeWorkRequest.Builder(InventorySyncWorker::class.java).addTag(tag.toString())
                .setConstraints(constraint).setInputData(input).build()
            enQueueUniqueWorker(worker, UNIQUE_ONE_TIME_WORKER_INVENTORY, activity?.applicationContext!!)
            WorkManager.getInstance(activity?.applicationContext!!).getWorkInfoByIdLiveData(worker.id)
                .observe(this, Observer { workInfo ->
                    workInfo?.let { info ->
                        when (info.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                val position = posExtension(workInfo.tags)
                                var msg: String = ""
                                workInfo.outputData.getString(INVENTORY_SYNC_OUTPUT_DATA)?.let {
                                    msg = it
                                    //activity?.toast(it)
                                }!!
                                binding.probressBar.gone()
                                syncPlantList[position].status = msg
                                syncPlantList[position].updatedDateTime = getCurrentTimeInMills().toString()
                                binding.rvSyncDetails.adapter?.notifyDataSetChanged()
                                PreferenceHelper.save(Constants.PLANT_LIST, gson.toJson(syncPlantList))
                            }
                            WorkInfo.State.FAILED -> {
                                val position = posExtension(workInfo.tags)
                                var msg: String = ""
                                workInfo.outputData.getString(INVENTORY_SYNC_OUTPUT_DATA)?.let {
                                    msg = it
                                    //activity?.toast(it)
                                }!!
                                val times = msg.split('#', '.')
                                if (times.size > 1)
                                    syncPlantList[position].updatedDateTime = times.get(1)
                                syncPlantList[position].status = msg
                                binding.rvSyncDetails.adapter?.notifyDataSetChanged()
                                PreferenceHelper.save(Constants.PLANT_LIST, gson.toJson(syncPlantList))
                                binding.probressBar.gone()
                            }
                            WorkInfo.State.CANCELLED -> {
                                workInfo.outputData.getString(INVENTORY_SYNC_OUTPUT_DATA)?.let { activity?.toast(it) }!!
                                binding.probressBar.gone()
                            }
                            else -> {
                            }
                        }
                    }

                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
