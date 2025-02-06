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
import com.olam.warehouse.login.databinding.FragmentVegaCocoaTransactionBinding
import com.olam.warehouse.login.databinding.ItemProcessingTransactionBinding
import com.olam.warehouse.login.databinding.ItemRminTransactionBinding
import com.olam.warehouse.login.databinding.ItemTransactionBinding
import com.olam.warehouse.master.vega.model.VegaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaFgrnItemWithGrades
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaRminItemWithLots
import com.olam.warehouse.master.work.getDispatchMtntOneTimeRequestWorker
import com.olam.warehouse.navigation.features.VegaDispatchCocoNavigation
import com.olam.warehouse.navigation.features.VegaProcessingCocoaNavigation
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_DATA
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_REMARK
import com.olam.warehouse.presentation.utils.UIUtils.FGRN_DATA
import com.olam.warehouse.presentation.utils.UIUtils.MTNT_DATA
import com.olam.warehouse.presentation.utils.UIUtils.MTNT_POST_DATA
import com.olam.warehouse.presentation.utils.UIUtils.RMIN_DATA
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.presentation.utils.extension.visible
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 5/26/2020.
 */
class VegaCocoaTransactionFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_cocoa_transaction
    private lateinit var binding: FragmentVegaCocoaTransactionBinding
    private val vm: TransactionViewModel by viewModel()

    private val mtntList = arrayListOf<VegaCocoaMtntWithLots>()
    private val fgrnList = arrayListOf<VegaCocoaFgrnItemWithGrades>()
    private val rminList = arrayListOf<VegaCocoaRminItemWithLots>()

    companion object {
        fun newInstance() = VegaCocoaTransactionFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCocoaTransactionBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        setVisibilityHide()
        binding.llMtnt.setOnClickListener {
            when (binding.rvMtnt.isVisible) {
                true -> binding.rvMtnt.gone()
                false -> {
                    setVisibility()
                    binding.rvMtnt.visible()
                }
            }
        }

        binding.llFgrn.setOnClickListener {
            when (binding.rvFgrn.isVisible) {
                true -> binding.rvFgrn.gone()
                false -> {
                    setVisibility()
                    binding.rvFgrn.visible()
                }
            }
        }

        binding.llRmin.setOnClickListener {
            when (binding.rvRmin.isVisible) {
                true -> binding.rvRmin.gone()
                false -> {
                    setVisibility()
                    binding.rvRmin.visible()
                }
            }
        }

        vm.mtntLots.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mtntList.clear()
                mtntList.addAll(it)
                if (mtntList.size > 0) {
                    binding.llMtnt.visible()
                } else binding.llMtnt.gone()
                setUpMtntAdapter(mtntList)
            }
        })
        vm.getMtntWithLots()

        vm.fgrnItem.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                fgrnList.clear()
                fgrnList.addAll(it)

                if (fgrnList.size > 0) {
                    binding.llFgrn.visible()
                } else binding.llFgrn.gone()
                setUpFgrnAdapter(fgrnList)
            }
        })
        vm.getFgrnItems()

        vm.rminItem.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                rminList.clear()
                rminList.addAll(it)

                if (rminList.size > 0) {
                    binding.llRmin.visible()
                } else binding.llRmin.gone()
                setUpRminAdapter(rminList)
            }
        })
        vm.getRminItems()

    }

    private fun setUpMtntAdapter(mtntList: ArrayList<VegaCocoaMtntWithLots>) {
        binding.rvMtnt.setUpAdapter(
            mtntList,
            R.layout.item_transaction,
            ItemTransactionBinding::inflate,
            { it, pos, binding ->
                val dispatch = it.dispatch
                binding.tvWeighBridgeId.text = dispatch.weighBridgeId
                when (dispatch.status) {
                    1 -> binding.tvSyncStatus.text = Status.SYNC_PENDING.toString()
                    3 -> binding.tvSyncStatus.text = Status.SYNC_ERROR.toString()
                    4 -> binding.tvSyncStatus.text = "Success"
                }
                binding.tvType.text = dispatch.weighBridgeType
                binding.tvErrorMsg.text = dispatch.message
                binding.tvErrorMsg1.text = dispatch.message
                binding.tvDirection.gone()
                binding.tvDirectionLabel.gone()
                if (dispatch.isProgress) binding.pbItemLoading.visible() else binding.pbItemLoading.gone()
                val times = dispatch.erdat?.split('(', ')')
                binding.tvDate.text = times?.get(1)?.let { it1 ->
                    DateUtils.getUTCDateTime(it1, context)
                }
                binding.tvErrorMsg.setOnClickListener {
                    binding.tvErrorMsg.gone()
                    binding.tvErrorMsg1.visible()
                }
                when (dispatch.status) {
                    4 -> {
                        binding.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                binding.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                        binding.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                binding.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                    }
                    3 -> {
                        binding.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                binding.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(2)
                            )
                        )
                        binding.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                binding.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(2)
                            )
                        )
                    }
                    1 -> {
                        binding.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                binding.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                        binding.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                binding.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                    }
                }

                binding.ivDeleteData.setOnClickListener { view -> showItemDeleteDialogMtnt(it) }
                binding.tvViewWeighBridge.setOnClickListener { view ->
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
                                when (it.lineItems.size > 0) {
                                    true -> {
                                        when (dispatch.remarks.isNotEmpty()) {
                                            true -> startDispatchMtntSync(
                                                dispatch.weighBridgeId,
                                                pos,
                                                dispatch.remarks
                                            )
                                            else -> showRemarkDialog(dispatch.weighBridgeId, pos)
                                        }
                                    }
                                    else -> activity?.toast(context.getString(R.string.loading_not_end))
                                }
                            }
                            R.id.action_edit -> {
                                moveFlag = true
                            }
                        }
                        if (moveFlag) {
                            VegaDispatchCocoNavigation.dynamicStart?.let { intent ->
                                intent.putExtra(MTNT_DATA, it.dispatch)
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

    private fun showRemarkDialog(weighBridgeId: String, pos: Int) {
        MaterialDialog(requireContext()).show {
            message(R.string.remarks)
            var remark = ""
            input(waitForPositiveButton = false, hint = "Remarks") { dialog, text ->
                val inputField = dialog.getInputField()
                val isValid = text.isNotEmpty()
                remark = text.toString()
                inputField.error = if (isValid) null else "Enter remarks"
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
            }
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    startDispatchMtntSync(weighBridgeId, pos, remark)
                },
                { dismiss() })
        }
    }

    private fun startDispatchMtntSync(weighBridgeId: String, pos: Int, remark: String) {
        mtntList[pos].dispatch.isProgress = true
        binding.rvMtnt.adapter?.notifyItemChanged(pos, mtntList[pos])
        val input = workDataOf(DISPATCH_DATA to weighBridgeId, DISPATCH_REMARK to remark)
        val worker = getDispatchMtntOneTimeRequestWorker(input, pos)
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

    private fun showItemDeleteDialogMtnt(mtnt: VegaCocoaMtntWithLots) {
        MaterialDialog(requireContext()).show {
            message(R.string.delete_msg)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    mtntList.remove(mtnt)
                    mtnt.dispatch.weighBridgeId.let { it1 -> vm.deleteMtntItem(it1) }
                },
                { dismiss() })
        }
    }

    private fun setUpFgrnAdapter(fgrnList: ArrayList<VegaCocoaFgrnItemWithGrades>) {
        binding.rvFgrn.setUpAdapter(
            fgrnList,
            R.layout.item_processing_transaction,
            ItemProcessingTransactionBinding::inflate,
            { it, pos, binding ->
                val fgrn = it.fgrnItems
                binding.tvWeighBridgeId.text = fgrn.fgrnId
                when (fgrn.status) {
                    1 -> binding.tvSyncStatus.text = Status.SYNC_PENDING.toString()
                    3 -> binding.tvSyncStatus.text = Status.SYNC_ERROR.toString()
                    4 -> binding.tvSyncStatus.text = "Success"
                }
                binding.tvType.text = fgrn.materialName
                binding.tvErrorMsg.text = fgrn.message
                binding.tvErrorMsg1.text = fgrn.message
                binding.tvDirection.gone()
                binding.tvDirectionLabel.gone()
                if (fgrn.isProgress) binding.pbItemLoading.visible() else binding.pbItemLoading.gone()
                val times = fgrn.startDate?.split('(', ')')
                binding.tvDate.text = times?.get(1)?.let { it1 ->
                    DateUtils.getUTCDateTime(it1, context)
                }
                binding.tvErrorMsg.setOnClickListener {
                    binding.tvErrorMsg.gone()
                    binding.tvErrorMsg1.visible()
                }
                when (fgrn.status) {
                    4 -> {
                        binding.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                binding.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                        binding.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                binding.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                    }
                    3 -> {
                        binding.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                binding.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(2)
                            )
                        )
                        binding.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                binding.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(2)
                            )
                        )
                    }
                    1 -> {
                        binding.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                binding.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                        binding.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                binding.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                    }
                }

                binding.ivDeleteData.setOnClickListener { view -> showItemDeleteDialogFgrn(it) }
                binding.tvViewWeighBridge.setOnClickListener { view ->
                    val popupMenu: PopupMenu = PopupMenu(requireActivity(), view)
                    popupMenu.menuInflater.inflate(R.menu.transaction_menu, popupMenu.menu)
                    if (fgrn.status == 4) popupMenu.menu.findItem(R.id.action_Sync).isVisible =
                        false
                    popupMenu.menu.findItem(R.id.action_copy).isVisible = false
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        val dispatchData = fgrn.copy()
                        var moveFlag = false
                        when (item.itemId) {
                            R.id.action_copy -> {
                                moveFlag = true
                                dispatchData.fgrnId = ""
                                dispatchData.message = "Data Cached Offline"
                                dispatchData.status = 1
                            }
                            R.id.action_Sync -> {
                                /*when (it.lineItems.size > 0) {
                                true -> {
                                    when (dispatch.remarks.isNotEmpty()) {
                                        true -> startDispatchMtntSync(dispatch.weighBridgeId, pos, dispatch.remarks)
                                        else -> showRemarkDialog(dispatch.weighBridgeId, pos)
                                    }
                                }
                                else -> activity?.toast(context.getString(R.string.loading_not_end))
                            }*/
                            }
                            R.id.action_edit -> {
                                moveFlag = true
                            }
                        }
                        if (moveFlag) {
                            VegaProcessingCocoaNavigation.dynamicStart?.let { intent ->
                                intent.putExtra(FGRN_DATA, it.fgrnItems)
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

    private fun showItemDeleteDialogFgrn(fgrn: VegaCocoaFgrnItemWithGrades) {
        MaterialDialog(requireContext()).show {
            message(R.string.delete_msg)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    fgrnList.remove(fgrn)
                    fgrn.fgrnItems.fgrnId.let { it1 -> vm.deleteFgrnItem(it1) }
                },
                { dismiss() })
        }
    }

    private fun setUpRminAdapter(rminList: ArrayList<VegaCocoaRminItemWithLots> ) {
        binding.rvRmin.setUpAdapter(
            rminList,
            R.layout.item_rmin_transaction,
            ItemRminTransactionBinding::inflate,
            { it, pos, binding ->
                val rmin = it.rminItem
                binding.tvrminId.text = rmin.rminId
                when (rmin.status) {
                    1 -> binding.tvSyncStatus.text = Status.SYNC_PENDING.toString()
                    3 -> binding.tvSyncStatus.text = Status.SYNC_ERROR.toString()
                    4 -> binding.tvSyncStatus.text = "Success"
                }

                binding.tvType.text = rmin.materialName
                binding.tvPono.text = rmin.poNumber


                when (rmin.status) {
                    4 -> {
                        binding.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                binding.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                        binding.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                binding.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                    }
                    3 -> {
                        binding.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                binding.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(2)
                            )
                        )
                        binding.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                binding.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(2)
                            )
                        )
                    }
                    1 -> {
                        binding.tvSyncStatus.setTextColor(
                            ContextCompat.getColor(
                                binding.tvSyncStatus.context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                        binding.vStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                binding.vStatus.context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                    }
                }

                binding.ivDeleteData.setOnClickListener { view -> showItemDeleteDialogRmin(it) }
                binding.tvViewWeighBridge.setOnClickListener { view ->
                    val popupMenu: PopupMenu = PopupMenu(requireActivity(), view)
                    popupMenu.menuInflater.inflate(R.menu.transaction_menu, popupMenu.menu)
                    if (rmin.status == 4) popupMenu.menu.findItem(R.id.action_Sync).isVisible =
                        false
                    popupMenu.menu.findItem(R.id.action_copy).isVisible = false
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        val dispatchData = rmin.copy()
                        var moveFlag = false
                        when (item.itemId) {
                            R.id.action_copy -> {
                                moveFlag = true
                                dispatchData.rminId = ""
                                //dispatchData. = "Data Cached Offline"
                                dispatchData.status = 1
                            }
                            R.id.action_Sync -> {
                                /*when (it.lineItems.size > 0) {
                                true -> {
                                    when (dispatch.remarks.isNotEmpty()) {
                                        true -> startDispatchMtntSync(dispatch.weighBridgeId, pos, dispatch.remarks)
                                        else -> showRemarkDialog(dispatch.weighBridgeId, pos)
                                    }
                                }
                                else -> activity?.toast(context.getString(R.string.loading_not_end))
                            }*/
                            }
                            R.id.action_edit -> {
                                moveFlag = true
                            }
                        }
                        if (moveFlag) {
                            VegaProcessingCocoaNavigation.dynamicStart?.let { intent ->
                                intent.putExtra(RMIN_DATA, it.rminItem)
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

    private fun showItemDeleteDialogRmin(rmin: VegaCocoaRminItemWithLots) {
        MaterialDialog(requireContext()).show {
            message(R.string.delete_msg)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    rminList.remove(rmin)
                    rmin.rminItem.rminId.let { it1 -> vm.deleteRminItem(it1) }
                },
                { dismiss() })

        }
    }

    private fun setVisibility() {
        binding.rvMtnt.gone()
        binding.rvSales.gone()
        binding.rvRmin.gone()
        binding.rvFgrn.gone()
    }

    private fun setVisibilityHide() {
        binding.llMtnt.gone()
        binding.llSales.gone()
        binding.llRmin.gone()
        binding.llFgrn.gone()
    }
}
