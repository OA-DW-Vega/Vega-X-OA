package com.olam.warehouse.vegax.inventorynicaragua.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils.getSyncItemBackgroundColor
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.inventorynicaragua.R
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.NicSyncStatus
import com.olam.warehouse.vegax.inventorynicaragua.databinding.FragmentNicBottomSheetInventorySyncBinding
import com.olam.warehouse.vegax.inventorynicaragua.databinding.ItemVegaNicInventorySyncDetailsBinding
import com.olam.warehouse.vegax.inventorynicaragua.utils.SYNC_STATUS_DATA
import com.olam.warehouse.vegax.inventorynicaragua.utils.SYNC_STATUS_DATA_MAT

/**
 * Created by Baskaran Kannan on 4/8/2020.
 */
class VegaNicInventorySyncDetailsDailog : BottomSheetDialogFragment() {

    private var syncStatusList = ArrayList<NicSyncStatus>()
    private var materialSyncStatusList = ArrayList<NicSyncStatus>()
    private var isMaterialSync = false

    companion object {
        fun newInstance(syncStatusList: ArrayList<NicSyncStatus>, materialSyncStatusList: ArrayList<NicSyncStatus>) = VegaNicInventorySyncDetailsDailog().putArgs {
            putParcelableArrayList(SYNC_STATUS_DATA, syncStatusList)
            putParcelableArrayList(SYNC_STATUS_DATA_MAT, materialSyncStatusList)
        }
    }

    private lateinit var binding: FragmentNicBottomSheetInventorySyncBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNicBottomSheetInventorySyncBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        syncStatusList = arguments?.getParcelableArrayList<NicSyncStatus>(SYNC_STATUS_DATA)!!
        materialSyncStatusList = arguments?.getParcelableArrayList<NicSyncStatus>(SYNC_STATUS_DATA_MAT)!!
        if(materialSyncStatusList.isEmpty()){
            binding.typeSelection.gone()
        }
        //setUpAdapter(syncStatusList)
        moveToPlant()
        binding.tvPlantSync.setOnClickListener { moveToPlant() }
        binding.tvMaterialSync.setOnClickListener { moveToMaterial() }
    }

    private fun moveToPlant() {
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
        binding.tvPlant.text = getString(com.olam.warehouse.login.R.string.plant)
        if(syncStatusList.isNotEmpty())setUpAdapter(syncStatusList)
    }

    private fun moveToMaterial() {
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
        binding.tvPlant.text = getString(com.olam.warehouse.login.R.string.material)
        setUpAdapter(materialSyncStatusList)
    }

    private fun setUpAdapter(syncStatusList: ArrayList<NicSyncStatus>) {
        binding.rvSyncDetails.setUpAdapter(
            syncStatusList,
            R.layout.item_vega_nic_inventory_sync_details,
            ItemVegaNicInventorySyncDetailsBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvPlantId.text = if(isMaterialSync) it.materialCode else it.plant
                bindItem.tvStatus.text = it.status
                bindItem.tvSyncTime.text = it.updatedTimeInMS?.let { it1 ->
                    DateUtils.getLastInventorySyncTime(
                        context,
                        it1
                    )
                }
                /*val dateTime = it.updatedDateTime?.split(".")
                tvSyncTime.text = dateTime?.get(0)?.replace("T", "  ") ?: ""*/
                when (it.status) {
                    "PEND" -> bindItem.tvStatus.setTextColor(
                        ContextCompat.getColor(
                            bindItem.tvStatus.context,
                            getSyncItemBackgroundColor(5)
                        )
                    )
                    "ERR" -> bindItem.tvStatus.setTextColor(
                        ContextCompat.getColor(
                            bindItem.tvStatus.context,
                            getSyncItemBackgroundColor(3)
                        )
                    )
                    else -> bindItem.tvStatus.setTextColor(
                        ContextCompat.getColor(
                            bindItem.tvStatus.context,
                            getSyncItemBackgroundColor(4)
                        )
                    )
                }
            })
    }
}
