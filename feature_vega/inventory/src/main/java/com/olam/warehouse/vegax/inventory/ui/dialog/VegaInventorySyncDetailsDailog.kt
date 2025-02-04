package com.olam.warehouse.vegax.inventory.ui.dialog

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
import com.olam.warehouse.vegax.inventory.R
import com.olam.warehouse.vegax.inventory.data.domain.model.SyncStatus
import com.olam.warehouse.vegax.inventory.databinding.FragmentBottomSheetInventorySyncBinding
import com.olam.warehouse.vegax.inventory.utils.SYNC_STATUS_DATA
import kotlinx.android.synthetic.main.item_vega_inventory_sync_details.view.*

/**
 * Created by Baskaran Kannan on 4/8/2020.
 */
class VegaInventorySyncDetailsDailog : BottomSheetDialogFragment() {

    private var syncStatusList = ArrayList<SyncStatus>()

    companion object {
        fun newInstance(syncStatusList: ArrayList<SyncStatus>) = VegaInventorySyncDetailsDailog().putArgs {
            putParcelableArrayList(SYNC_STATUS_DATA, syncStatusList)
        }
    }

    private lateinit var binding: FragmentBottomSheetInventorySyncBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBottomSheetInventorySyncBinding.inflate(layoutInflater)

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
        binding.rvSyncDetails.setUp(syncStatusList, R.layout.item_vega_inventory_sync_details, { it, pos ->
            tvPlantId.text = it.plant
            tvStatus.text = it.status
            tvSyncTime.text = it.updatedTimeInMS?.let { it1 -> DateUtils.getLastInventorySyncTime(context, it1) }
            /*val dateTime = it.updatedDateTime?.split(".")
            tvSyncTime.text = dateTime?.get(0)?.replace("T", "  ") ?: ""*/
            when (it.status) {
                "PEND" -> tvStatus.setTextColor(ContextCompat.getColor(tvStatus.context, getSyncItemBackgroundColor(5)))
                "ERR" -> tvStatus.setTextColor(ContextCompat.getColor(tvStatus.context, getSyncItemBackgroundColor(3)))
                else -> tvStatus.setTextColor(ContextCompat.getColor(tvStatus.context, getSyncItemBackgroundColor(4)))
            }
        })
    }
}
