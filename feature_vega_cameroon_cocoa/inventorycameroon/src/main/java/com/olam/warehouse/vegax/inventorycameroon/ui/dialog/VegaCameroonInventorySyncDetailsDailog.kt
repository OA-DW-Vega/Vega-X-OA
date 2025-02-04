package com.olam.warehouse.vegax.inventorycameroon.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils.getSyncItemBackgroundColor
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.inventorycameroon.R
import com.olam.warehouse.vegax.inventorycameroon.data.domain.model.SyncStatus
import com.olam.warehouse.vegax.inventorycameroon.databinding.FragmentBottomSheetCameroonInventorySyncBinding
import com.olam.warehouse.vegax.inventorycameroon.utils.SYNC_STATUS_DATA
import kotlinx.android.synthetic.main.item_vega_cameroon_inventory_sync_details.view.*
import java.util.*

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
class VegaCameroonInventorySyncDetailsDailog : BottomSheetDialogFragment() {

    private var syncStatusList = ArrayList<SyncStatus>()

    companion object {
        fun newInstance(syncStatusList: ArrayList<SyncStatus>) = VegaCameroonInventorySyncDetailsDailog().putArgs {
            putParcelableArrayList(SYNC_STATUS_DATA, syncStatusList)
        }
    }

    private lateinit var binding: FragmentBottomSheetCameroonInventorySyncBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBottomSheetCameroonInventorySyncBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        syncStatusList = arguments?.getParcelableArrayList<SyncStatus>(SYNC_STATUS_DATA)!!
        setUpAdapter(syncStatusList)
    }

    private fun setUpAdapter(syncStatusList: java.util.ArrayList<SyncStatus>) {
        binding.rvSyncDetails.setUp(syncStatusList, R.layout.item_vega_cameroon_inventory_sync_details, { it, pos ->
            tvPlantId.text = it.plant
            tvStatus.text = it.status
            tvSyncTime.text = it.updatedTimeInMS?.let { it1 -> DateUtils.getLastInventorySyncTime(context, it1) }

            when (it.status) {
                "PEND" -> tvStatus.setTextColor(ContextCompat.getColor(tvStatus.context, getSyncItemBackgroundColor(5)))
                "ERR" -> tvStatus.setTextColor(ContextCompat.getColor(tvStatus.context, getSyncItemBackgroundColor(3)))
                else -> tvStatus.setTextColor(ContextCompat.getColor(tvStatus.context, getSyncItemBackgroundColor(4)))
            }
        })
    }
}
