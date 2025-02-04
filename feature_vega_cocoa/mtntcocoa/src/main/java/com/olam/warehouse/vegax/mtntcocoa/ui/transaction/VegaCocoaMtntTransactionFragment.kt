package com.olam.warehouse.vegax.mtntcocoa.ui.transaction

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.common.model.SyncStatusProgress
import com.olam.warehouse.master.vega.model.VegaCocoaNoWeighmentWithLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentModel
import com.olam.warehouse.master.work.getVirtualOneTimeRequestWorker
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcocoa.R
import com.olam.warehouse.vegax.mtntcocoa.databinding.FragmentVegaCocoaVirtualTransactionBinding
import com.olam.warehouse.vegax.mtntcocoa.ui.CallBack
import com.olam.warehouse.vegax.mtntcocoa.ui.VegaCocoaMtntViewModel
import com.olam.warehouse.vegax.mtntcocoa.utils.NO_WEIGHMENT_FROM_PENDING
import com.olam.warehouse.vegax.mtntcocoa.utils.NO_WEIGHMENT_SUMMARY
import com.olam.warehouse.vegax.mtntcocoa.utils.TRANSACTION_SUMMARY
import kotlinx.android.synthetic.main.item_state_layout.view.*
import kotlinx.android.synthetic.main.item_vega_cocoa_virtual_pending_list.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCocoaMtntTransactionFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_cocoa_virtual_transaction
    private lateinit var binding: FragmentVegaCocoaVirtualTransactionBinding
    private val vm: VegaCocoaMtntViewModel by viewModel()
    private var transactionList = mutableListOf<VegaCocoaNoWeighmentWithLots>()
    private var pendingList = arrayListOf<VegaCocoaNoWeighmentWithLots>()
    private var callBack: CallBack? = null

    companion object {
        fun newInstance() = VegaCocoaMtntTransactionFragment().putArgs {
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCocoaVirtualTransactionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("/mtntcocoa/ui/transaction/VegaCocoaMtntTransactionFragment")
            .title("Dispatch Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        vm.dispatchnoWeighment.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                transactionList.clear()
                transactionList.addAll(it)
            }
        })
        vm.getTransactionList()
        vm.getPendingListWithLot()
        vm.dispatchPendingWithLot.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                pendingList.clear()
                pendingList.addAll(it)
                moveToPending()
            }
        })
        binding.tvPending.setOnClickListener { moveToPending() }
        binding.tvHistory.setOnClickListener { moveToHistory() }
        binding.tvSortByDate.setOnClickListener { sortByDate() }
        binding.tvSync.setOnClickListener { showConfirmDialog() }
        enableSync(AppUtils.isOnline())
    }

    private fun moveToHistory() {
        binding.etSearchVendor.gone()
        binding.tvSortByDate.visible()
        binding.tvSync.gone()
        binding.tvHistory.setBackgroundColor(
            ContextCompat.getColor(
                binding.tvHistory.context,
                com.olam.warehouse.presentation.R.color.colorPrimaryOfi1
            )
        )
        binding.tvPending.setBackgroundColor(
            ContextCompat.getColor(
                binding.tvHistory.context,
                com.olam.warehouse.presentation.R.color.grey_light
            )
        )
        setupAdapter(transactionList)
    }

    private fun moveToPending() {
        binding.etSearchVendor.gone()
        binding.tvSortByDate.gone()
        binding.tvSync.visible()
        binding.tvPending.setBackgroundColor(
            ContextCompat.getColor(
                binding.tvHistory.context,
                com.olam.warehouse.presentation.R.color.colorPrimaryOfi1
            )
        )
        binding.tvHistory.setBackgroundColor(
            ContextCompat.getColor(
                binding.tvHistory.context,
                com.olam.warehouse.presentation.R.color.grey_light
            )
        )
        setUpPendingListAdapter(pendingList)
    }

    private fun sortByDate() {
        if (transactionList.isNotEmpty()) {
            val data = transactionList
            transactionList = data.asReversed()
            setupAdapter(transactionList)
        }
    }

    private fun setUpPendingListAdapter(item: List<VegaCocoaNoWeighmentWithLots>) {
        if (item.isNotEmpty()) {
            binding.rvTransaction.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvTransaction.gone()
            binding.tvNoData.visible()
        }
        binding.rvTransaction.setUp(
            item as MutableList, R.layout.item_vega_cocoa_virtual_pending_list, { it, pos ->
                tvObdNoValue.text = it.dispatch.weighBridgeId
                tvDateValue.text = it.dispatch.erdat
                tvVendorValue.text = it.dispatch.weighBridgeId
                tvMaterialValue.text = it.dispatch.materialName
                tvTruckNoValue.text = it.dispatch.vehicleNumber
                tvWeightValue.text =
                    it.dispatch.netWeight.plus(" ").plus(it.dispatch.unitsOfMeasure)
                tvSendingWHValue.text =
                    it.dispatch.warehouseId.plus(" ").plus(it.dispatch.plantName)
                tvError.text = it.dispatch.message

                if (it.dispatch.message.isNullOrBlank()) {
                    tvError.visibility = View.GONE
                    tvShowMore.visibility = View.GONE
                    tv.visibility = View.GONE
                    clMoreView.visibility = View.GONE
                } else {
                    tvError.visibility = View.VISIBLE
                    tvShowMore.visibility = View.VISIBLE
                    tv.visibility = View.VISIBLE
                    clMoreView.visibility = View.VISIBLE
                }

                tvShowMore.setOnClickListener {
                    rvSynProgress.visibility = View.VISIBLE
                }

                val itemList = arrayListOf<SyncStatusProgress>()
                if (it.lineItems.isNotEmpty()) {
                    itemList.add(
                        SyncStatusProgress(
                            itemOrder = 1,
                            itemName = getString(R.string.delivery),
                            itemStatus = it.lineItems[0].weighScaleWbId?.isNotBlank() == true
                        )
                    )
                    itemList.add(
                        SyncStatusProgress(
                            itemOrder = 2,
                            itemName = getString(R.string.picking),
                            itemStatus = it.lineItems[0].deliveryFlag
                        )
                    )
                    itemList.add(
                        SyncStatusProgress(
                            itemOrder = 3,
                            itemName = getString(R.string.pgi),
                            itemStatus = it.lineItems[0].isPgiFlag && it.lineItems[0].pickingFlag
                        )
                    )
                }
                if (it.lineItems.isNotEmpty() && it.lineItems[0].isEndLot == true) {
                    itemList.add(
                        SyncStatusProgress(
                            itemOrder = 4,
                            itemName = getString(R.string.pgi),
                            itemStatus = it.lineItems[0].storageLossFlag ?: false
                        )
                    )
                }

                rvSynProgress.addItemDecoration(OverlapDecoration())
                rvSynProgress.setUp(
                    itemList.sortedBy { it.itemOrder }.toMutableList(),
                    R.layout.item_state_layout,
                    { it, pos ->
                        if (pos == 0) vBarLine.gone() else vBarLine.visible()
                        tvItemName.text = it.itemName
                        if (it.itemStatus) {
                            ViewCompat.setBackgroundTintList(
                                vBarLine,
                                context?.let {
                                    ContextCompat.getColorStateList(
                                        it,
                                        com.olam.warehouse.presentation.R.color.green
                                    )
                                }
                            )
                            ivScaleClose.setImageResource(com.olam.warehouse.presentation.R.drawable.ic_check_circle_black_24dp)
                            ivScaleClose.imageTintList = ContextCompat.getColorStateList(
                                ivScaleClose.context,
                                com.olam.warehouse.presentation.R.color.green
                            )
                        } else {
                            ViewCompat.setBackgroundTintList(
                                vBarLine,
                                context?.let {
                                    ContextCompat.getColorStateList(
                                        it,
                                        com.olam.warehouse.presentation.R.color.grey_light
                                    )
                                }
                            )
                            ivScaleClose.setImageResource(com.olam.warehouse.presentation.R.drawable.ic_coffee_cicle_close)
                            ivScaleClose.imageTintList = ContextCompat.getColorStateList(
                                ivScaleClose.context,
                                com.olam.warehouse.presentation.R.color.red_ff
                            )
                        }
                    },
                    {},
                    LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                )

                ivClose.setOnClickListener { view ->
                    val popupMenu: PopupMenu = PopupMenu(requireActivity(), view)
                    popupMenu.menuInflater.inflate(
                        com.olam.warehouse.login.R.menu.transaction_menu,
                        popupMenu.menu
                    )
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_Sync).isVisible =
                        false
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_copy).isVisible =
                        false
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit).isVisible =
                        false
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit1).isVisible =
                        true
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_delete).isVisible =
                        true
                    popupMenu.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            /*com.olam.warehouse.login.R.id.action_edit -> {
                                //movePriceCalculationSummary(it)
                            }*/
                            com.olam.warehouse.login.R.id.action_edit1 -> {
                                moveEdit(it)
                            }
                            com.olam.warehouse.login.R.id.action_delete -> {
                                showItemDeleteDialog(it.dispatch.weighBridgeId)
                            }
                        }
                        true
                    }
                    popupMenu.show()
                }
            })
    }

    private fun setupAdapter(itemList: List<VegaCocoaNoWeighmentWithLots>) {
        if (itemList.isNotEmpty()) {
            binding.rvTransaction.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvTransaction.gone()
            binding.tvNoData.visible()
        }

        binding.rvTransaction.setUp(itemList as MutableList, R.layout.item_vega_cocoa_virtual_pending_list, { it, pos ->
            tvObdNoValue.text = it.lineItems[0].delivery
            tvDateValue.text = it.dispatch.erdat
            tvMaterialValue.text = it.dispatch.materialName
            tvTruckNoValue.text = it.dispatch.vehicleNumber
            tvWeightValue.text = it.dispatch.netWeight.plus(" ").plus(it.dispatch.unitsOfMeasure)
            tvSendingWHValue.text = it.dispatch.warehouseId.plus(" ").plus(it.dispatch.plantName)
            ivClose.visibility = View.GONE
            ivNav.visibility = View.VISIBLE
            tvVendorValue.text = it.lineItems[0].weighScaleWbId
            clMain.setOnClickListener { moveToDetails(itemList[pos].dispatch) }
            if (it.lineItems[0].isEndLot == true) {
                tvPhysicalDocValue.visibility = View.VISIBLE
                tvPhysicalDoc.visibility = View.VISIBLE
                tvPhysicalDocValue.text = it.lineItems[0].region
            } else {
                tvPhysicalDocValue.visibility = View.GONE
                tvPhysicalDoc.visibility = View.GONE
            }
        })
    }


    private fun moveToDetails(id: VegaCocoaNoWeighmentModel) {
        callBack?.replaceFragment(TRANSACTION_SUMMARY, id)
    }


    private fun moveEdit(it: VegaCocoaNoWeighmentWithLots) {
        if (it.lineItems.isNotEmpty()) {
            if (!it.lineItems[0].deliveryFlag && !it.lineItems[0].pickingFlag && !it.lineItems[0].isPgiFlag)
                callBack?.replaceFragment(NO_WEIGHMENT_FROM_PENDING, it.dispatch)
            else {
                callBack?.replaceFragment(NO_WEIGHMENT_SUMMARY, it.dispatch)
            }
        }
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.sync_all)
            getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                { startSync(pendingList) },
                { dismiss() })
        }
    }

    private fun showItemDeleteDialog(tmpWbId: String) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                { vm.deleteWeighModel(tmpWbId) },
                { dismiss() })
        }
    }


    private fun showErrorDialog(title: String) {
        MaterialDialog(requireContext()).show {
            message(null, title)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.ok),
                "",
                { dismiss() },
                { })
        }
    }

    private fun enableSync(flag: Boolean) {
        binding.tvSync.isEnabled = flag
        if (flag)
            ViewCompat.setBackgroundTintList(
                binding.tvSync,
                context?.let {
                    ContextCompat.getColorStateList(
                        it,
                        com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                    )
                }
            )
        else
            ViewCompat.setBackgroundTintList(
                binding.tvSync,
                context?.let {
                    ContextCompat.getColorStateList(
                        it,
                        com.olam.warehouse.presentation.R.color.grey
                    )
                }
            )

    }

    private fun startSync(list: MutableList<VegaCocoaNoWeighmentWithLots>) {
        list.forEachIndexed { _index, _element ->
            val input = workDataOf(UIUtils.MTNT_VIRTUAL_DATA to _element.dispatch.wbTempId)
            val worker = getVirtualOneTimeRequestWorker(input, _index)
            enQueueWorker(worker, requireContext())
            WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
                .observe(this, Observer { workInfo ->
                    if (workInfo != null) {
                        when (workInfo.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                hideLoading()
                            }
                            WorkInfo.State.FAILED -> {
                                hideLoading()
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

class OverlapDecoration : RecyclerView.ItemDecoration() {
    var vertOverlap = -3
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(vertOverlap, 0, 0, 0)
    }
}
