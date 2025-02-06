package com.olam.warehouse.vegax.inventorycocoa.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils.getSyncItemBackgroundColor
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.inventorycocoa.R
import com.olam.warehouse.vegax.inventorycocoa.data.domain.model.SyncStatus
import com.olam.warehouse.vegax.inventorycocoa.databinding.FragmentBottomSheetCocoaInventorySyncBinding
import com.olam.warehouse.vegax.inventorycocoa.databinding.ItemVegaCocoaInventorySyncDetailsBinding
import com.olam.warehouse.vegax.inventorycocoa.utils.SYNC_STATUS_DATA
import java.util.*

/**
 * Created by Baskaran Kannan on 4/8/2020.
 */
class VegaCocoaInventorySyncDetailsDailog : BottomSheetDialogFragment() {

    private var syncStatusList = ArrayList<SyncStatus>()

    companion object {
        fun newInstance(syncStatusList: ArrayList<SyncStatus>) = VegaCocoaInventorySyncDetailsDailog().putArgs {
            putParcelableArrayList(SYNC_STATUS_DATA, syncStatusList)
        }
    }

    private lateinit var binding: FragmentBottomSheetCocoaInventorySyncBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBottomSheetCocoaInventorySyncBinding.inflate(layoutInflater)

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
        binding.rvSyncDetails.setUpAdapter(
            syncStatusList,
            R.layout.item_vega_cocoa_inventory_sync_details,
            ItemVegaCocoaInventorySyncDetailsBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvPlantId.text = it.plant
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
