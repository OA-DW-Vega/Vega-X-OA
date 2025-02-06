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
import androidx.work.workDataOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.ItemVegaInventorySyncBinding
import com.olam.warehouse.login.databinding.VegaInventoryBottomSheetBinding
import com.olam.warehouse.master.common.data.work.InventorySyncWorker
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.MATERIAL
import com.olam.warehouse.presentation.utils.AppUtils.PLANT_ID
import com.olam.warehouse.presentation.utils.AppUtils.posExtension
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.presentation.utils.extension.visible

/**
 * Created by Baskaran Kannan on 4/9/2020.
 */
class VegaInventorySyncDialog : BottomSheetDialogFragment() {

    private var syncPlantList = ArrayList<Plant>()
    private var syncMaterialList = ArrayList<Plant>()
    val gson = GsonUtils()
    private var isMaterialSync = false

    companion object {
        fun newInstance(syncStatusList: ArrayList<Plant>, matList: ArrayList<Plant>) = VegaInventorySyncDialog().putArgs {
            putParcelableArrayList("SYNC_PLANT_DATA", syncStatusList)
            putParcelableArrayList("MATERIAL_LIST",matList)
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
        syncMaterialList  = arguments?.getParcelableArrayList<Plant>("MATERIAL_LIST")!!
        if(syncMaterialList.isNotEmpty()) binding.typeSelection.visible() else binding.typeSelection.gone()
        if(getCurrentKey().split("_")[1].contains("CM")){
            syncPlantList = arguments?.getParcelableArrayList<Plant>("SYNC_PLANT_DATA")!!
            var syncPlant = syncPlantList.filter { it.plantId.equals(getPlantDetails().plantId) }
            //setUpAdapter(syncPlant as java.util.ArrayList<Plant>)
            moveToPlant(syncPlant as java.util.ArrayList<Plant>)
        }
        else{
            syncPlantList = arguments?.getParcelableArrayList<Plant>("SYNC_PLANT_DATA")!!
            moveToPlant(syncPlantList)

        }

        binding.tvClose.setOnClickListener { dialog?.dismiss() }
        binding.tvPlantSync.setOnClickListener { moveToPlant(syncPlantList) }
        binding.tvMaterialSync.setOnClickListener { moveToMaterial(syncMaterialList) }
    }

    private fun moveToPlant(syncPlantList: ArrayList<Plant>) {
        isMaterialSync = false
        binding.tvPlantSync.setBackgroundColor(
            ContextCompat.getColor(
                binding.tvPlantSync.context,
                com.olam.warehouse.presentation.R.color.colorPrimaryOfi1
            )
        )
        binding.tvMaterialSync.setBackgroundColor(
            ContextCompat.getColor(
                binding.tvMaterialSync.context,
                com.olam.warehouse.presentation.R.color.grey_light
            )
        )
        binding.tvPlantHeadMain.text = getString(R.string.plant_details)
        binding.tvPlantHeadTwo.text = getString(R.string.plant)
        setUpAdapter(syncPlantList)
    }

    private fun moveToMaterial(syncPlantList: ArrayList<Plant>) {
        isMaterialSync = true
        binding.tvMaterialSync.setBackgroundColor(
            ContextCompat.getColor(
                binding.tvMaterialSync.context,
                com.olam.warehouse.presentation.R.color.colorPrimaryOfi1
            )
        )
        binding.tvPlantSync.setBackgroundColor(
            ContextCompat.getColor(
                binding.tvPlantSync.context,
                com.olam.warehouse.presentation.R.color.grey_light
            )
        )
        binding.tvPlantHeadMain.text = getString(R.string.material_details)
        binding.tvPlantHeadTwo.text = getString(R.string.material)
        setUpAdapter(syncPlantList)
    }

    private fun setUpAdapter(syncStatusList: java.util.ArrayList<Plant>) {
        binding.rvSyncDetails.setUpAdapter(
            syncStatusList,
            R.layout.item_vega_inventory_sync,
            ItemVegaInventorySyncBinding::inflate,
            { it, pos, binding ->
                binding.tvPlantId.text = if(isMaterialSync)it.materialCode.plus("\n").plus(it.materialName.toString()) else it.plantId
                if (it.status?.isNotEmpty()!!) {
                    val stamsg = it.status!!.split("#", ".")
                    val data: String
                    if (stamsg.size > 1) {
                        data = DateUtils.getLastInventorySyncTime(context, stamsg.get(1))
                        binding.tvStatus.text =
                            stamsg.toString().replace("" + stamsg[1], data).replace("[", "")
                                .replace("]", "")
                                .replace(", 0", ". 0")
                    } else {
                        binding.tvStatus.text = stamsg.toString().replace("[", "").replace("]", "")
                    }

                    binding.llStatus.visible()
                } else {
                    binding.llStatus.gone()
                }

                if (it.updatedDateTime?.isEmpty()!!) {
                    binding.tvSyncTime.text = "-"
                } else {
                    binding.tvSyncTime.text = it.updatedDateTime?.let { it1 ->
                        DateUtils.getLastInventorySyncTime(context, it1)
                    }
                }
                binding.ivSync.setOnClickListener { _ ->
                    inventorySyncData(it.plantId, pos, it.materialCode)
                }

            })
    }

    private fun inventorySyncData(plantId: String, tag: Int, materialCode: String) {
        val materialNo = if(isMaterialSync) materialCode else ""
        binding.probressBar.visible()
        WorkManager.getInstance(activity?.applicationContext!!).cancelAllWork()
        try {
            val input = workDataOf(PLANT_ID to plantId, MATERIAL to materialNo)
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
                                if(isMaterialSync){
                                    syncMaterialList[position].updatedDateTime = getCurrentTimeInMills().toString()
                                    syncMaterialList[position].status = msg
                                    binding.rvSyncDetails.adapter?.notifyDataSetChanged()
                                    PreferenceHelper.save(Constants.PLANT_MATERIAL_LIST, gson.toJson(syncMaterialList))
                                }else{
                                    syncPlantList[position].status = msg
                                    syncPlantList[position].updatedDateTime = getCurrentTimeInMills().toString()
                                    binding.rvSyncDetails.adapter?.notifyDataSetChanged()
                                    PreferenceHelper.save(Constants.PLANT_LIST, gson.toJson(syncPlantList))
                                }
                            }
                            WorkInfo.State.FAILED -> {
                                val position = posExtension(workInfo.tags)
                                var msg: String = ""
                                workInfo.outputData.getString(INVENTORY_SYNC_OUTPUT_DATA)?.let {
                                    msg = it
                                    //activity?.toast(it)
                                }!!
                                val times = msg.split('#', '.')
                                if(isMaterialSync){
                                    if (times.size > 1)
                                        syncMaterialList[position].updatedDateTime = times.get(1)
                                    syncMaterialList[position].status = msg
                                    binding.rvSyncDetails.adapter?.notifyDataSetChanged()
                                    PreferenceHelper.save(Constants.PLANT_MATERIAL_LIST, gson.toJson(syncMaterialList))
                                }else{
                                    if (times.size > 1)
                                        syncPlantList[position].updatedDateTime = times.get(1)
                                    syncPlantList[position].status = msg
                                    binding.rvSyncDetails.adapter?.notifyDataSetChanged()
                                    PreferenceHelper.save(Constants.PLANT_LIST, gson.toJson(syncPlantList))
                                }
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
