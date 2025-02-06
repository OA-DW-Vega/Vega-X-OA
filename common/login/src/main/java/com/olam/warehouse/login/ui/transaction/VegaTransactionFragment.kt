package com.olam.warehouse.login.ui.transaction

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.FragmentVegaTransactionBinding
import com.olam.warehouse.login.databinding.ItemTransactionBinding
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaDispatchWithLots
import com.olam.warehouse.master.vega.model.VegaMtntWithLineItems
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.master.vega.model.VegaRminBomWithLots
import com.olam.warehouse.master.work.*
import com.olam.warehouse.navigation.features.VegaDispatchNavigation
import com.olam.warehouse.navigation.features.VegaProcessingNavigation
import com.olam.warehouse.navigation.features.VegaQualityFeatureNavigation
import com.olam.warehouse.navigation.features.VegaReceivingFeatureNavigation
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.DIRECTIONOUT
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_BATCH
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_DATA
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_POST_DATA
import com.olam.warehouse.presentation.utils.UIUtils.MTNT_DATA
import com.olam.warehouse.presentation.utils.UIUtils.MTNT_POST_DATA
import com.olam.warehouse.presentation.utils.UIUtils.QUALITY_DATA
import com.olam.warehouse.presentation.utils.UIUtils.RECEIVING_DATA
import com.olam.warehouse.presentation.utils.UIUtils.RECEIVING_POST_DATA
import com.olam.warehouse.presentation.utils.UIUtils.RMIN_DATA
import com.olam.warehouse.presentation.utils.UIUtils.RMIN_POST_DATA
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 5/6/2020.
 */
class VegaTransactionFragment : BaseFragment() {

    private val rminList = arrayListOf<VegaRminBomWithLots>()
    private val dispatchList = arrayListOf<VegaDispatchWithLots>()
    private val receivingList = arrayListOf<VegaReceivingWithLineItems>()
    private val qualityList = arrayListOf<VegaQualityWBDetails>()
    private val mtntList = arrayListOf<VegaMtntWithLineItems>()

    private var isDataFound = false

    private val vm: TransactionViewModel by viewModel()
    override val layoutResourceId = R.layout.fragment_vega_transaction
    private lateinit var binding: FragmentVegaTransactionBinding

    companion object {
        fun newInstance() = VegaTransactionFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaTransactionBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {

        binding.llReceiving.setOnClickListener {
            when (binding.rvReceiving.isVisible) {
                true -> binding.rvReceiving.gone()
                false -> {
                    setVisibility()
                    binding.rvReceiving.visible()
                }
            }
        }

        binding.llQuality.setOnClickListener {
            when (binding.rvQuality.isVisible) {
                true -> binding.rvQuality.gone()
                false -> {
                    setVisibility()
                    binding.rvQuality.visible()
                }
            }
        }

        binding.llMtnt.setOnClickListener {
            when (binding.rvMtnt.isVisible) {
                true -> binding.rvMtnt.gone()
                false -> {
                    setVisibility()
                    binding.rvMtnt.visible()
                }
            }
        }

        binding.llDispatch.setOnClickListener {
            when (binding.rvDispatch.isVisible) {
                true -> binding.rvDispatch.gone()
                false -> {
                    setVisibility()
                    binding.rvDispatch.visible()
                }
            }
        }

        binding.llProcessing.setOnClickListener {
            when (binding.rvProcessing.isVisible) {
                true -> binding.rvProcessing.gone()
                false -> {
                    setVisibility()
                    binding.rvProcessing.visible()
                }
            }
        }

        // Get Receiving Data
        vm.receiveWithLineItemLocal.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                receivingList.clear()
                /* val list = it.sortedByDescending { item ->
                     item.receiving.erdat?.split('(', ')')?.get(1)?.let { it1 ->
                         context?.let { it2 -> DateUtils.getUTCDateTime(it1, it2) }
                     }
                 } as MutableList<VegaReceivingWithLineItems>*/
                receivingList.addAll(it)
                if (receivingList.size > 0) {
                    isDataFound = true
                    binding.llReceiving.visible()
                } else binding.llReceiving.gone()
                setUpAdapter(receivingList)
            }
        })
        vm.getReceivingWithLineItem()

        // Get Quality Data
        vm.weighBridge.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                qualityList.clear()
                /*val list = it.sortedByDescending { item ->
                    item.erdat?.split('(', ')')?.get(1)?.let { it1 ->
                        context?.let { it2 -> DateUtils.getUTCDateTime(it1, it2) }
                    }
                } as MutableList<VegaQualityWBDetails>*/
                qualityList.addAll(it)
                if (qualityList.size > 0) {
                    isDataFound = true
                    binding.llQuality.visible()
                } else binding.llQuality.gone()
                setUpQualityAdapter(qualityList)
            }
        })
        vm.getWeighBridgeDetail()

        // Get Mtnt Data
        vm.mtntWithLineItem.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mtntList.clear()
                /*val list = it.sortedByDescending { item ->
                    item.mtnt.erdat?.split('(', ')')?.get(1)?.let { it1 ->
                        context?.let { it2 -> DateUtils.getUTCDateTime(it1, it2) }
                    }
                } as MutableList<VegaMtntWithLineItems>*/
                mtntList.addAll(it)
                if (mtntList.size > 0) {
                    isDataFound = true
                    binding.llMtnt.visible()
                } else binding.llMtnt.gone()
                setUpMtntAdapter(mtntList)
            }
        })
        vm.getMtntWithLineItem()

        // Get Dispatch Data
        vm.dispatchLots.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                dispatchList.clear()
                /* val list = it.sortedByDescending { item ->
                     item.dispatch.erdat?.split('(', ')')?.get(1)?.let { it1 ->
                         context?.let { it2 -> DateUtils.getUTCDateTime(it1, it2) }
                     }
                 } as MutableList<VegaDispatchWithLots>*/
                dispatchList.addAll(it)
                if (dispatchList.size > 0) {
                    isDataFound = true
                    binding.llDispatch.visible()
                } else binding.llDispatch.gone()
                setUpDispatchAdapter(dispatchList)
            }
        })
        vm.getDispatchWithLots()

        // Get Processing Data
        vm.rminLots.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                rminList.clear()
                rminList.addAll(it)
                if (rminList.size > 0) {
                    isDataFound = true
                    binding.llProcessing.visible()
                } else binding.llProcessing.gone()
                setUpProcessingAdapter(rminList)
            }
        })
        vm.getRminWithLots()

        //if(!isDataFound) binding.tvEmptyContent.visible() else binding.tvEmptyContent.gone()
    }

    fun setVisibility() {
        binding.rvReceiving.gone()
        binding.rvQuality.gone()
        binding.rvMtnt.gone()
        binding.rvDispatch.gone()
        binding.rvProcessing.gone()
    }

    //Weighment
    private fun setUpAdapter(receivingList: ArrayList<VegaReceivingWithLineItems>) {
        binding.rvReceiving.setUpAdapter(
            this.receivingList,
            R.layout.item_transaction,
            ItemTransactionBinding::inflate,
            { it, pos, bindingItem ->
                val receiving = it.receiving
                bindingItem.tvWeighBridgeId.text =
                    if (receiving.weighBridgeId.isNullOrEmpty()) receiving.tmpWbId else receiving.weighBridgeId
                bindingItem.tvSyncStatus.text =
                    if (receiving.status.equals(Status.RECEVING_COMPLETED)) "Success" else receiving.status.toString()
                bindingItem.tvType.text = receiving.weighBridgeType
                bindingItem.tvErrorMsg.text = receiving.syncStatusMsg
                bindingItem.tvErrorMsg1.text = receiving.syncStatusMsg
                bindingItem.tvDirection.text = receiving.truckDirection
                if (receiving.isProgress) bindingItem.pbItemLoading.visible() else bindingItem.pbItemLoading.gone()
                val times = receiving.erdat?.split('(', ')')
                bindingItem.tvDate.text = times?.get(1)?.let { it1 ->
                    DateUtils.getUTCDateTime(it1, context)
                }
                bindingItem.tvErrorMsg.setOnClickListener {
                    bindingItem.tvErrorMsg.gone()
                    bindingItem.tvErrorMsg1.visible()
                }
                when (receiving.status) {
                    Status.RECEVING_COMPLETED -> {
                        bindingItem.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                bindingItem.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                        bindingItem.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                bindingItem.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                    }
                    Status.SYNC_ERROR -> {
                        bindingItem.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                bindingItem.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(2)
                            )
                        )
                        bindingItem.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                bindingItem.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(2)
                            )
                        )
                    }
                    Status.SYNC_PENDING -> {
                        bindingItem.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                bindingItem.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                        bindingItem.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                bindingItem.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                    }
                    else -> {}
                }


                bindingItem.ivDeleteData.setOnClickListener { view -> showItemDeleteDialog(it) }
                bindingItem.tvViewWeighBridge.setOnClickListener { view ->
                    val popupMenu: PopupMenu = PopupMenu(requireActivity(), view)
                    popupMenu.menuInflater.inflate(R.menu.transaction_menu, popupMenu.menu)
                    if (receiving.status.equals(Status.RECEVING_COMPLETED)) popupMenu.menu.findItem(
                        R.id.action_Sync
                    )
                        .isVisible = false
                    if (receiving.truckDirection.equals(DIRECTIONOUT)) popupMenu.menu.findItem(R.id.action_copy).isVisible =
                        false
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        val receivingData = receiving.copy()
                        var moveFlag = false
                        when (item.itemId) {
                            R.id.action_copy -> {
                                moveFlag = true
                                receivingData.weighBridgeId = ""
                                receivingData.tmpWbId = ""
                                receivingData.syncStatusMsg = "Data Cached Offline"
                                receivingData.status = Status.SYNC_PENDING
                            }
                            R.id.action_Sync -> {
                                startSync(receiving.tmpWbId, pos)
                            }
                            R.id.action_edit -> {
                                moveFlag = true
                            }
                        }
                        if (moveFlag) {
                            VegaReceivingFeatureNavigation.dynamicStart?.let { intent ->
                                intent.putExtra(RECEIVING_DATA, receivingData)
                                intent.putParcelableArrayListExtra(
                                    RECEIVING_POST_DATA,
                                    it.lineItems as ArrayList<out Parcelable>
                                )
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        }
                        true
                    })
                    popupMenu.show()
                }
            })
    }

    private fun startSync(tmpWbId: String, pos: Int) {
        receivingList[pos].receiving.isProgress = true
        binding.rvReceiving.adapter?.notifyItemChanged(pos, receivingList[pos])
        val input = workDataOf(RECEIVING_DATA to tmpWbId)
        val worker = getReceivingOneTimeRequestWorker(input, pos)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                        }
                        WorkInfo.State.FAILED -> {
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                        else -> {
                        }
                    }
                }

            })
    }

    //Quality

    private fun setUpQualityAdapter(qualityList: ArrayList<VegaQualityWBDetails>) {
        binding.rvQuality.setUpAdapter(
            qualityList,
            R.layout.item_transaction,
            ItemTransactionBinding::inflate,
            { it, pos, bindingItem ->
                bindingItem.tvWeighBridgeId.text =
                    if (it.weighBridgeId.isNullOrEmpty()) it.wbTempId else it.weighBridgeId
                when (it.status) {
                    1 -> bindingItem.tvSyncStatus.text = Status.SYNC_PENDING.toString()
                    3 -> bindingItem.tvSyncStatus.text = Status.SYNC_ERROR.toString()
                    4 -> bindingItem.tvSyncStatus.text = "Success"
                }
                bindingItem.tvType.text = it.weighBridgeType
                bindingItem.tvErrorMsg.text = it.message
                bindingItem.tvErrorMsg1.text = it.message
                if (it.isProgress) bindingItem.pbItemLoading.visible() else bindingItem.pbItemLoading.gone()
                bindingItem.tvDirection.gone()
                bindingItem.tvDirectionLabel.gone()
                bindingItem.tvErrorMsg.setOnClickListener {
                    bindingItem.tvErrorMsg.gone()
                    bindingItem.tvErrorMsg1.visible()
                }
                val times = it.erdat?.split('(', ')')
                bindingItem.tvDate.text = times?.get(1)?.let { it1 ->
                    DateUtils.getUTCDateTime(it1, context)
                }
                when (it.status) {
                    4 -> {
                        bindingItem.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                bindingItem.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                        bindingItem.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                bindingItem.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                    }
                    3 -> {
                        bindingItem.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                bindingItem.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(2)
                            )
                        )
                        bindingItem.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                bindingItem.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(2)
                            )
                        )
                    }
                    1 -> {
                        bindingItem.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                bindingItem.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                        bindingItem.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                bindingItem.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                    }
                }

                bindingItem.ivDeleteData.setOnClickListener { view -> showItemDeleteDialogQuality(it) }
                bindingItem.tvViewWeighBridge.setOnClickListener { view ->
                    val popupMenu: PopupMenu = PopupMenu(requireActivity(), view)
                    popupMenu.menuInflater.inflate(R.menu.transaction_menu, popupMenu.menu)
                    if (it.status == 4) popupMenu.menu.findItem(R.id.action_Sync).isVisible = false
                    //popupMenu.menu.findItem(R.id.action_copy).isVisible = false
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        var moveFlag = false
                        when (item.itemId) {
                            R.id.action_Sync -> {
                                startQualitySync(it.weighBridgeId, pos)
                            }
                            R.id.action_edit -> {
                                moveFlag = true
                            }
                            R.id.action_copy -> {
                                moveFlag = true
                                it.isCopy = true
                            }
                        }
                        if (moveFlag) {
                            VegaQualityFeatureNavigation.dynamicStart?.let { intent ->
                                intent.putExtra(QUALITY_DATA, it)
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        }
                        true
                    })
                    popupMenu.show()
                }
            })
    }

    private fun startQualitySync(tmpWbId: String, pos: Int) {
        qualityList[pos].isProgress = true
        binding.rvQuality.adapter?.notifyItemChanged(pos, qualityList[pos])
        val input = workDataOf(QUALITY_DATA to tmpWbId)
        val worker = getQualityOneTimeRequestWorker(input, pos)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                        }
                        WorkInfo.State.FAILED -> {
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                        else -> {
                        }
                    }
                }

            })
    }

    //MTNT
    private fun setUpMtntAdapter(mtntList: ArrayList<VegaMtntWithLineItems>) {
        binding.rvMtnt.setUpAdapter(
            mtntList,
            R.layout.item_transaction,
            ItemTransactionBinding::inflate,
            { it, pos, bindingItem ->
                val mtnt = it.mtnt
                bindingItem.tvWeighBridgeId.text =
                    if (mtnt.weighBridgeId.isNullOrEmpty()) mtnt.tmpWbId else mtnt.weighBridgeId
                bindingItem.tvSyncStatus.text =
                    if (mtnt.status.equals(Status.MTNT_COMPLETED)) "Success" else mtnt.status.toString()
                bindingItem.tvType.text = mtnt.weighBridgeType
                bindingItem.tvErrorMsg.text = mtnt.syncStatusMsg
                bindingItem.tvErrorMsg1.text = mtnt.syncStatusMsg
                bindingItem.tvDirection.text = mtnt.truckDirection
                if (mtnt.isProgress) bindingItem.pbItemLoading.visible() else bindingItem.pbItemLoading.gone()
                val times = mtnt.erdat?.split('(', ')')
                bindingItem.tvDate.text = times?.get(1)?.let { it1 ->
                    DateUtils.getUTCDateTime(it1, context)
                }
                bindingItem.tvErrorMsg.setOnClickListener {
                    bindingItem.tvErrorMsg.gone()
                    bindingItem.tvErrorMsg1.visible()
                }
                when (mtnt.status) {
                    Status.MTNT_COMPLETED -> {
                        bindingItem.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                bindingItem.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                        bindingItem.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                bindingItem.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                    }
                    Status.SYNC_ERROR -> {
                        bindingItem.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                bindingItem.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(2)
                            )
                        )
                        bindingItem.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                bindingItem.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(2)
                            )
                        )
                    }
                    Status.SYNC_PENDING -> {
                        bindingItem.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                bindingItem.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                        bindingItem.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                bindingItem.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                    }
                    else -> {}
                }


                bindingItem.ivDeleteData.setOnClickListener { view -> showItemDeleteDialogMtnt(it) }
                bindingItem.tvViewWeighBridge.setOnClickListener { view ->
                    val popupMenu: PopupMenu = PopupMenu(requireActivity(), view)
                    popupMenu.menuInflater.inflate(R.menu.transaction_menu, popupMenu.menu)
                    if (it.mtnt.status.equals(Status.MTNT_COMPLETED)) popupMenu.menu.findItem(R.id.action_Sync).isVisible =
                        false
                    if (it.mtnt.truckDirection.equals(DIRECTIONOUT)) popupMenu.menu.findItem(R.id.action_copy).isVisible =
                        false
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        var moveFlag = false
                        when (item.itemId) {
                            R.id.action_Sync -> {
                                startMtntSync(mtnt.tmpWbId, pos)
                            }
                            R.id.action_copy -> {
                                moveFlag = true
                                mtnt.weighBridgeId = ""
                                mtnt.tmpWbId = ""
                                mtnt.syncStatusMsg = "Data Cached Offline"
                                mtnt.status = Status.SYNC_PENDING
                            }
                            R.id.action_edit -> {
                                moveFlag = true
                            }
                        }
                        if (moveFlag) {
                            VegaReceivingFeatureNavigation.dynamicStart?.let { intent ->
                                intent.putExtra(MTNT_DATA, mtnt)
                                intent.putParcelableArrayListExtra(
                                    MTNT_POST_DATA,
                                    it.lineItems as ArrayList<out Parcelable>
                                )
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        }
                        true
                    })
                    popupMenu.show()
                }
            })
    }

    private fun startMtntSync(tmpWbId: String, pos: Int) {
        mtntList[pos].mtnt.isProgress = true
        binding.rvMtnt.adapter?.notifyItemChanged(pos, mtntList[pos])
        val input = workDataOf(MTNT_DATA to tmpWbId)
        val worker = getMtntOneTimeRequestWorker(input, pos)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                        }
                        WorkInfo.State.FAILED -> {
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                        else -> {
                        }
                    }
                }

            })
    }

    //Dispatch
    private fun setUpDispatchAdapter(dispatchList: ArrayList<VegaDispatchWithLots>) {
        binding.rvDispatch.setUpAdapter(
            dispatchList,
            R.layout.item_transaction,
            ItemTransactionBinding::inflate,
            { it, pos, bindingItem ->
                val dispatch = it.dispatch
                bindingItem.tvWeighBridgeId.text = dispatch.weighBridgeId
                when (dispatch.status) {
                    1 -> bindingItem.tvSyncStatus.text = Status.SYNC_PENDING.toString()
                    3 -> bindingItem.tvSyncStatus.text = Status.SYNC_ERROR.toString()
                    4 -> bindingItem.tvSyncStatus.text = "Success"
                }
                bindingItem.tvType.text = dispatch.weighBridgeType
                bindingItem.tvErrorMsg.text = dispatch.message
                bindingItem.tvErrorMsg1.text = dispatch.message
                bindingItem.tvDirection.gone()
                bindingItem.tvDirectionLabel.gone()
                if (dispatch.isProgress) bindingItem.pbItemLoading.visible() else bindingItem.pbItemLoading.gone()
                val times = dispatch.erdat?.split('(', ')')
                bindingItem.tvDate.text = times?.get(1)?.let { it1 ->
                    DateUtils.getUTCDateTime(it1, context)
                }
                bindingItem.tvErrorMsg.setOnClickListener {
                    bindingItem.tvErrorMsg.gone()
                    bindingItem.tvErrorMsg1.visible()
                }
                when (dispatch.status) {
                    4 -> {
                        bindingItem.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                bindingItem.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                        bindingItem.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                bindingItem.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                    }
                    3 -> {
                        bindingItem.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                bindingItem.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(2)
                            )
                        )
                        bindingItem.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                bindingItem.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(2)
                            )
                        )
                    }
                    1 -> {
                        bindingItem.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                bindingItem.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                        bindingItem.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                bindingItem.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                    }
                }


                bindingItem.ivDeleteData.setOnClickListener { view ->
                    showItemDeleteDialogDispatch(
                        it
                    )
                }
                bindingItem.tvViewWeighBridge.setOnClickListener { view ->
                    val popupMenu: PopupMenu = PopupMenu(requireActivity(), view)
                    popupMenu.menuInflater.inflate(R.menu.transaction_menu, popupMenu.menu)
                    if (dispatch.status == 4) popupMenu.menu.findItem(R.id.action_Sync).isVisible =
                        false
                    popupMenu.menu.findItem(R.id.action_copy).isVisible = false
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        val dispatchData = dispatch.copy()
                        var moveFlag = false
                        when (item.itemId) {
                            R.id.action_copy -> {
                                moveFlag = true
                                dispatchData.weighBridgeId = ""
                                dispatchData.message = "Data Cached Offline"
                                dispatchData.status = 1
                            }
                            R.id.action_Sync -> {
                                when (dispatch.batchNumber?.isNotEmpty()) {
                                    true -> startDispatchSync(
                                        dispatch.weighBridgeId,
                                        pos,
                                        dispatch.batchNumber.toString()
                                    )
                                    else -> showBatchNoDialog(dispatch.weighBridgeId, pos)
                                }
                            }
                            R.id.action_edit -> {
                                moveFlag = true
                            }
                        }
                        if (moveFlag) {
                            VegaDispatchNavigation.dynamicStart?.let { intent ->
                                intent.putExtra(DISPATCH_DATA, it.dispatch)
                                intent.putParcelableArrayListExtra(
                                    DISPATCH_POST_DATA,
                                    it.lineItems as ArrayList<out Parcelable>
                                )
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        }
                        true
                    })
                    popupMenu.show()
                }
            })
    }

    private fun showBatchNoDialog(weighBridgeId: String, pos: Int) {
        MaterialDialog(requireContext()).show {
            message(R.string.enter_batchno)
            var batch = ""
            input(waitForPositiveButton = false, hint = "batch number") { dialog, text ->
                val inputField = dialog.getInputField()
                val isValid = text.isNotEmpty()
                batch = text.toString()
                inputField.error = if (isValid) null else "Enter batch number"
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
            }
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    startDispatchSync(weighBridgeId, pos, batch)
                },
                { dismiss() })
        }
    }

    private fun startDispatchSync(tmpWbId: String, pos: Int, batchNumber: String) {
        dispatchList[pos].dispatch.isProgress = true
        binding.rvDispatch.adapter?.notifyItemChanged(pos, dispatchList[pos])
        val input = workDataOf(DISPATCH_DATA to tmpWbId, DISPATCH_BATCH to batchNumber)
        val worker = getDispatchOneTimeRequestWorker(input, pos)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                        }
                        WorkInfo.State.FAILED -> {
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                        else -> {
                        }
                    }
                }

            })
    }

    //RMIN
    private fun setUpProcessingAdapter(rminList: ArrayList<VegaRminBomWithLots>) {
        binding.rvProcessing.setUpAdapter(
            rminList,
            R.layout.item_transaction,
            ItemTransactionBinding::inflate,
            { it, pos, bindingItem ->
                val rmin = it.rminPo
                bindingItem.tvwbLabel.text = "CfgNo"
                bindingItem.tvwbTypeLabel.text = "Stage"
                bindingItem.tvDirectionLabel.text = "Material"
                bindingItem.tvDateLabel.text = "Batch"
                bindingItem.tvDate.text = rmin.batchNumber
                bindingItem.tvWeighBridgeId.text = rmin.cfgNo
                when (rmin.status) {
                    Status.RMIN_COMPLETED -> bindingItem.tvSyncStatus.text = "Success"
                    else -> bindingItem.tvSyncStatus.text = rmin.status.toString()
                }
                bindingItem.tvType.text = rmin.processingStage
                bindingItem.tvErrorMsg.text = rmin.syncStatusMsg
                bindingItem.tvErrorMsg1.text = rmin.syncStatusMsg
                bindingItem.tvDirection.text = rmin.materialName
                if (rmin.isProgress) bindingItem.pbItemLoading.visible() else bindingItem.pbItemLoading.gone()
                bindingItem.tvErrorMsg.setOnClickListener {
                    bindingItem.tvErrorMsg.gone()
                    bindingItem.tvErrorMsg1.visible()
                }
                when (rmin.status) {
                    Status.RMIN_COMPLETED -> {
                        bindingItem.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                bindingItem.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                        bindingItem.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                bindingItem.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                    }
                    Status.SYNC_ERROR -> {
                        bindingItem.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                bindingItem.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(2)
                            )
                        )
                        bindingItem.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                bindingItem.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(2)
                            )
                        )
                    }
                    Status.SYNC_PENDING -> {
                        bindingItem.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                bindingItem.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                        bindingItem.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                bindingItem.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                    }
                    else -> {}
                }


                bindingItem.ivDeleteData.setOnClickListener { view -> showItemDeleteDialogRmin(it) }
                bindingItem.tvViewWeighBridge.setOnClickListener { view ->
                    val popupMenu: PopupMenu = PopupMenu(requireActivity(), view)
                    popupMenu.menuInflater.inflate(R.menu.transaction_menu, popupMenu.menu)
                    if (rmin.status.equals(Status.RMIN_COMPLETED)) popupMenu.menu.findItem(R.id.action_Sync).isVisible =
                        false
                    popupMenu.menu.findItem(R.id.action_copy).isVisible = false
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        val rminData = rmin.copy()
                        var moveFlag = false
                        when (item.itemId) {
                            R.id.action_copy -> {
                                moveFlag = true

                            }
                            R.id.action_Sync -> {
                                startRminSync(pos, rmin.batchNumber)
                            }
                            R.id.action_edit -> {
                                moveFlag = true
                            }
                        }
                        if (moveFlag) {
                            VegaProcessingNavigation.dynamicStart?.let { intent ->
                                intent.putExtra(RMIN_DATA, it.rminPo)
                                intent.putParcelableArrayListExtra(
                                    RMIN_POST_DATA,
                                    it.lineItems as ArrayList<out Parcelable>
                                )
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        }
                        true
                    })
                    popupMenu.show()
                }
            })
    }

    private fun startRminSync(pos: Int, batchNumber: String) {
        rminList[pos].rminPo.isProgress = true
        binding.rvProcessing.adapter?.notifyItemChanged(pos, rminList[pos])
        val input = workDataOf(RMIN_DATA to batchNumber)
        val worker = getRminOneTimeRequestWorker(input, pos)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                        }
                        WorkInfo.State.FAILED -> {
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                        else -> {
                        }
                    }
                }

            })
    }

    private fun showItemDeleteDialog(receiving: VegaReceivingWithLineItems?) {
        MaterialDialog(requireContext()).show {
            message(R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    receivingList.remove(receiving)
                    receiving?.receiving?.tmpWbId?.let { it1 -> vm.deleteWeighBride(it1) }
                },
                { dismiss() })
        }
    }

    private fun showItemDeleteDialogQuality(qualityWBDetails: VegaQualityWBDetails) {
        MaterialDialog(requireContext()).show {
            message(R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    qualityList.remove(qualityWBDetails)
                    qualityWBDetails.weighBridgeId.let { it1 -> vm.deleteQualityWeighBride(it1) }
                },
                { dismiss() })
        }
    }

    private fun showItemDeleteDialogMtnt(mtnt: VegaMtntWithLineItems?) {
        MaterialDialog(requireContext()).show {
            message(R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    mtntList.remove(mtnt)
                    mtnt?.mtnt?.tmpWbId?.let { it1 -> vm.deleteMtnt(it1) }
                },
                { dismiss() })
        }
    }

    private fun showItemDeleteDialogDispatch(dispatch: VegaDispatchWithLots) {
        MaterialDialog(requireContext()).show {
            message(R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    dispatchList.remove(dispatch)
                    dispatch.dispatch.weighBridgeId.let { it1 -> vm.deleteDispatchItem(it1) }
                },
                { dismiss() })
        }
    }

    private fun showItemDeleteDialogRmin(rmin: VegaRminBomWithLots) {
        MaterialDialog(requireContext()).show {
            message(R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    rminList.remove(rmin)
                    rmin.rminPo.batchNumber.let { it1 -> vm.deleteRmin(it1) }
                },
                { dismiss() })
        }
    }


}
