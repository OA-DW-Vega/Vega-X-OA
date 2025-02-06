package com.olam.warehouse.login.ui.common

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.FragmentSyncStatusProgressBarBinding
import com.olam.warehouse.login.databinding.ItemSyncStatusProgressBarBinding
import com.olam.warehouse.master.common.model.SyncStatusProgress
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible

/**
 * Created by Baskaran Kannan on 5/28/2020.
 */
class VegaSynStatusProgressFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_sync_status_progress_bar
    private lateinit var binding: FragmentSyncStatusProgressBarBinding
    private var callBack: CallBackPallet? = null
    private var synList = arrayListOf<SyncStatusProgress>()

    companion object {
        fun newInstance(list: ArrayList<SyncStatusProgress>) =
            VegaSynStatusProgressFragment().putArgs {
                putParcelableArrayList("BUNDLE_DATA", list)
            }
    }

    interface CallBackPallet {
        fun updatePalletDetails(noOfPallet: String, palletWeight: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBackPallet
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSyncStatusProgressBarBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        synList = arguments?.getParcelableArrayList<SyncStatusProgress>("BUNDLE_DATA") ?: ArrayList()
        setupAdapter(synList)
    }

    private fun setupAdapter(synList: ArrayList<SyncStatusProgress>) {
        binding.rvSynProgress.addItemDecoration(OverlapDecoration())
        binding.rvSynProgress.addItemDecoration(OverlapDecoration())
        binding.rvSynProgress.setUpAdapter(
            synList.sortedBy { it.itemOrder }.toMutableList(),
            R.layout.item_sync_status_progress_bar,
            ItemSyncStatusProgressBarBinding::inflate,
            { it, pos, binding ->
                if (pos == 0) binding.vBarLine.gone() else binding.vBarLine.visible()
                binding.tvItemName.text = it.itemName
                if (it.itemStatus) {
                    ViewCompat.setBackgroundTintList(
                        binding.vBarLine,
                        context?.let {
                            ContextCompat.getColorStateList(
                                it,
                                com.olam.warehouse.presentation.R.color.green
                            )
                        }
                    )
                    binding.ivScaleClose.setImageResource(com.olam.warehouse.presentation.R.drawable.ic_check_circle_black_24dp)
                    binding.ivScaleClose.imageTintList = ContextCompat.getColorStateList(
                        binding.ivScaleClose.context,
                        com.olam.warehouse.presentation.R.color.green
                    )
                } else {
                    ViewCompat.setBackgroundTintList(
                        binding.vBarLine,
                        context?.let {
                            ContextCompat.getColorStateList(
                                it,
                                com.olam.warehouse.presentation.R.color.grey_light
                            )
                        }
                    )
                    binding.ivScaleClose.setImageResource(com.olam.warehouse.presentation.R.drawable.ic_coffee_cicle_close)
                    binding.ivScaleClose.imageTintList = ContextCompat.getColorStateList(
                        binding.ivScaleClose.context,
                        com.olam.warehouse.presentation.R.color.red_ff
                    )
                }
            },
            {},
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        )
    }
}

class OverlapDecoration : RecyclerView.ItemDecoration() {
    var vertOverlap = -4
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(vertOverlap, 0, 0, 0)
    }
}
