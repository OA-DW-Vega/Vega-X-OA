package com.olam.warehouse.vegax.mtntindo.ui.offline

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.common.OverlapDecoration
import com.olam.warehouse.login.ui.quickpinaccess.createpin.VegaCreatePinActivity
import com.olam.warehouse.master.common.model.SyncStatusProgress
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchWithLineItems
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntindo.R
import com.olam.warehouse.vegax.mtntindo.databinding.FragmentVegaIndoCoffeeMtntTransactionBinding
import com.olam.warehouse.vegax.mtntindo.ui.VegaIndoCoffeeDispatchViewModel
import com.olam.warehouse.vegax.mtntindo.utils.ADD_LOT
import com.olam.warehouse.vegax.mtntindo.utils.DISPATCH_SUMMARY
import com.olam.warehouse.vegax.mtntindo.works.getEcuadorDispatchOneTimeRequestWorker
import kotlinx.android.synthetic.main.item_indo_mtnt_sync_status_progress_bar.view.*
import kotlinx.android.synthetic.main.item_vega_indo_mtnt_transaction.view.*
import kotlinx.android.synthetic.main.item_vega_indo_mtnt_transaction.view.ivScaleClose
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 4/26/2021.
 */
class VegaIndoCoffeeMtntTransactionDetailsFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_indo_coffee_mtnt_transaction
    private lateinit var binding: FragmentVegaIndoCoffeeMtntTransactionBinding
    private val vm: VegaIndoCoffeeDispatchViewModel by viewModel()
    private var offloadingList = arrayListOf<VegaEcuadorDispatchWithLineItems>()
    private var grnSearchList = arrayListOf<VegaEcuadorDispatchWithLineItems>()
    private var grnSortList = arrayListOf<VegaEcuadorDispatchWithLineItems>()
    private var selectedReceiving = VegaEcuadorDispatchWithLineItems()
    private var callBack: CallBack? = null
    private var priceEdit = false
    private val REQUEST_CODE = 220
    private val LOCK_REQUEST_CODE = 221
    private val SECURITY_SETTING_REQUEST_CODE = 233

    companion object {
        fun newInstance() = VegaIndoCoffeeMtntTransactionDetailsFragment().putArgs {
        }
    }

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            dispatchData: VegaEcuadorDispatch,
            data: Any
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaIndoCoffeeMtntTransactionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        PreferenceHelper.save(Constants.START_SYNC, false)
        binding.clProgress.gone()
        vm.dispatchItemLocal.observe(this, Observer {
            if (it != null) {
                updateUI(it)
            }
        })
        vm.getDispatchWithLineItem()

        binding.tvPending.setOnClickListener { moveToPending() }
        binding.tvHistory.setOnClickListener { moveToHistory() }
        binding.etSearchVendor.onChange {
            if (it.isNotEmpty()) {
                grnSearchList.clear()
                offloadingList.filter { it.dispatch.status.equals(Status.MTNT_COMPLETED) }.forEach { item ->
                    if (item.dispatch.supplierName?.contains(it, true) == true || item.dispatch.supplierCode?.contains(
                            it,
                            true
                        ) == true
                    ) grnSearchList.add(item)
                }
                setupAdapter(grnSearchList)
            } else {
                setupAdapter(offloadingList.filter { it.dispatch.status.equals(Status.MTNT_COMPLETED) })
            }
        }
        binding.tvSortByDate.setOnClickListener { sortByDate() }
        binding.tvSync.setOnClickListener {
            if (offloadingList.filter { it.dispatch.status.equals(Status.SYNC_PENDING) }
                    .isNotEmpty()) showConfirmDialog()
            else activity?.toast(getString(R.string.no_data_found))
        }
        EnableSync(AppUtils.isOnline())
    }

    private fun updateUI(data: List<VegaEcuadorDispatchWithLineItems>?) {
        offloadingList.clear()
        data?.let {
            offloadingList.addAll(it)
        }
        moveToPending()
    }

    private fun moveToHistory() {
        binding.clProgress.gone()
        binding.etSearchVendor.gone()
        binding.tvSortByDate.gone()
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
        binding.tvPendingCount.gone()
        setupAdapter(offloadingList.filter { it.dispatch.status.equals(Status.MTNT_COMPLETED) })
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
        binding.tvPendingCount.visible()
        val count = offloadingList.filter { it.dispatch.status.equals(Status.SYNC_PENDING) }
        binding.tvPendingCount.text = count.size.toString()
        if (count.size > 0) binding.tvPendingCount.visible() else binding.tvPendingCount.gone()
        EnableSync(count.size > 0 && AppUtils.isOnline())
        setupAdapter(offloadingList.filter { it.dispatch.status.equals(Status.SYNC_PENDING) })
    }

    private fun sortByDate() {
        if (offloadingList.filter { it.dispatch.status.equals(Status.MTNT_COMPLETED) }.isNotEmpty()) {
            grnSortList =
                offloadingList.filter { it.dispatch.status.equals(Status.MTNT_COMPLETED) } as ArrayList<VegaEcuadorDispatchWithLineItems>
            if (grnSortList.size > 0)
                setupAdapter(grnSortList.asReversed())
        }
    }

    private fun setupAdapter(itemList: List<VegaEcuadorDispatchWithLineItems>) {
        if (itemList.size > 0) {
            binding.rvTransaction.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvTransaction.gone()
            binding.tvNoData.visible()
        }
        binding.rvTransaction.setUp(
            itemList as ArrayList,
            R.layout.item_vega_indo_mtnt_transaction,
            { item, pos ->
                val it = item.dispatch
                tvOffloadTempIdValue.text =
                    if (it.deliveryId.isNotEmpty() == true) it.deliveryId else it.wbTempId
                tvVendorValue.text = it.storageLocationCode
                tvMaterialValue.text = it.materialName
                tvBatchNoValue.text = it.purchaseDocNum
                tvErrorValue.text =
                        /*if (it.syncStarted == 1) getString(R.string.sync_already_triggered) else*/
                    it.message
                //if(it.isSynced) ivScaleClose.gone() else ivScaleClose.visible()
                if ("null" != it.erdat) {
                    val times = it.erdat?.split('(', ')')
                    tvDateValue.text = times?.get(1)?.let { it1 ->
                        DateUtils.getUTCDateTime(
                            it1,
                            App.getAppContext()
                        )
                    }
                }
                //ivScaleClose.setOnClickListener {view -> showItemDeleteDialog(it.tmpWbId) }
                ivEdit.setOnClickListener { view ->
                    val popupMenu: PopupMenu = PopupMenu(requireActivity(), view)
                    popupMenu.menuInflater.inflate(
                        com.olam.warehouse.login.R.menu.transaction_menu,
                        popupMenu.menu
                    )
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_Sync).isVisible =
                        false
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_copy).isVisible =
                        false
                    if (it.isSynced) {
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit).isVisible =
                            true
                    } else {
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit).isVisible =
                            true
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit1).isVisible =
                            true
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_delete).isVisible =
                            true
                    }
                    popupMenu.setOnMenuItemClickListener { item1 ->
                        when (item1.itemId) {
                            com.olam.warehouse.login.R.id.action_edit -> {
                                moveViewSummary(item)
                            }
                            com.olam.warehouse.login.R.id.action_edit1 -> {
                                moveEdit(item)
                            }
                            com.olam.warehouse.login.R.id.action_delete -> {
                                showItemDeleteDialog(it.wbTempId.toString())
                            }
                        }
                        true
                    }
                    popupMenu.show()
                }
                if (it.message.isNullOrEmpty() || it.isSynced) {
                    tvReadMore.gone()
                    tvError.gone()
                    tvErrorValue.gone()
                } else {
                    tvReadMore.visible()
                    tvError.visible()
                    tvErrorValue.visible()
                }
                if (it.message.isNullOrEmpty() && it.status.equals(Status.SYNC_PENDING)) clErrorStatus.gone() else clErrorStatus.visible()
                tvReadMore.setOnClickListener { view -> showErrorDialog(it.message.toString()) }
                val itemList = arrayListOf<SyncStatusProgress>()
                val data1 = SyncStatusProgress(1, getString(R.string.delivery), it.delivery)
                val data2 = SyncStatusProgress(2, getString(R.string.picking), it.picking)
                val data3 = SyncStatusProgress(3, getString(R.string.pgi), it.pgi)
                itemList.add(data1)
                itemList.add(data2)
                itemList.add(data3)
                rvSynProgress.addItemDecoration(OverlapDecoration())
                rvSynProgress.setUp(
                    itemList.sortedBy { it.itemOrder }.toMutableList(),
                    R.layout.item_indo_mtnt_sync_status_progress_bar,
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
                //displayFragment(VegaSynStatusProgressFragment.newInstance(itemList), false)
            },
            {
                // movePriceCalculationSummary(this)
            })
    }

    private fun moveEdit(it: VegaEcuadorDispatchWithLineItems) {
        it.dispatch.isView = false
        val data = it.dispatch
        callBack?.replaceFragment(
            ADD_LOT,
            data,
            it.lineItems as ArrayList<VegaEcuadorDispatchLots>
        )
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.sync_all)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    startSync(
                        offloadingList.filter { it.dispatch.status.equals(Status.SYNC_PENDING) },
                        0
                    )
//                checkQuickPin()
                },
                { dismiss() })
        }
    }

    private fun checkQuickPin() {
        val isDevicePin = PreferenceHelper.get(Constants.IS_DEVICE_PIN, false)
        val createdNewPin = PreferenceHelper.get(Constants.QUICK_PIN, "")
        if (isDevicePin) {
            authenticateApp()
        } else {
            if (createdNewPin.isEmpty()) {
                startSync(offloadingList.filter { it.dispatch.status.equals(Status.SYNC_PENDING) }, 0)
            } else {
                val intent = Intent(activity, VegaCreatePinActivity::class.java)
                intent.putExtra(UIUtils.EXTRA_SET_PIN, false)
                startActivityForResult(intent, REQUEST_CODE)
            }
        }
    }

    //method to authenticate app
    private fun authenticateApp() {
        //Get the instance of KeyGuardManager
        val keyguardManager = activity?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        //Check if the device version is greater than or equal to Lollipop(21)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Create an intent to open device screen lock screen to authenticate
            //Pass the Screen Lock screen Title and Description
            val i = keyguardManager.createConfirmDeviceCredentialIntent(
                resources.getString(com.olam.warehouse.login.R.string.unlock),
                resources.getString(com.olam.warehouse.login.R.string.confirm_pattern)
            )
            try {
                //Start activity for result
                startActivityForResult(i, LOCK_REQUEST_CODE)
            } catch (e: Exception) {

                //If some exception occurs means Screen lock is not set up please set screen lock
                //Open Security screen directly to enable patter lock
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                try {

                    //Start activity for result
                    startActivityForResult(intent, SECURITY_SETTING_REQUEST_CODE)
                } catch (ex: Exception) {

                    //If app is unable to find any Security settings then user has to set screen lock manually
//                    textView.setText(resources.getString(R.string.setting_label))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LOCK_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                //If screen lock authentication is success update text
                startSync(offloadingList.filter { it.dispatch.status.equals(Status.SYNC_PENDING) }, 0)
            } else {
                //If screen lock authentication is failed update text
//                textView.setText(resources.getString(R.string.unlock_failed))
            }
            SECURITY_SETTING_REQUEST_CODE ->                 //When user is enabled Security settings then we don't get any kind of RESULT_OK
                //So we need to check whether device has enabled screen lock or not
                if (isDeviceSecure()) {
                    //If screen lock enabled show toast and start intent to authenticate user
                    activity?.toast(resources.getString(com.olam.warehouse.login.R.string.device_is_secure))
                    authenticateApp()
                } else {
                    //If screen lock is not enabled just update text
//                    textView.setText(resources.getString(R.string.security_device_cancelled))
                }
            REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                //If screen lock authentication is success update text
                startSync(offloadingList.filter { it.dispatch.status.equals(Status.SYNC_PENDING) }, 0)
            } else {
                //If screen lock authentication is failed update text
//                textView.setText(resources.getString(R.string.unlock_failed))
            }
        }
    }


//    method to return whether device has screen lock enabled or not

    private fun isDeviceSecure(): Boolean {
        val keyguardManager = activity?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        //this method only work whose api level is greater than or equal to Jelly_Bean (16)
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && keyguardManager.isKeyguardSecure
        //You can also use keyguardManager.isDeviceSecure(); but it requires API Level 23
    }

    private fun showItemDeleteDialog(wbTempId: String) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    vm.updateDeletedItem(wbTempId)
                },
                { dismiss() })
        }
    }

    private fun moveViewSummary(it: VegaEcuadorDispatchWithLineItems) {
        selectedReceiving = it
        it.dispatch.isView = true
        callBack?.replaceFragment(
            DISPATCH_SUMMARY,
            it.dispatch,
            it.lineItems as ArrayList<VegaEcuadorDispatchLots>
        )
    }

    private fun showErrorDialog(title: String) {
        MaterialDialog(requireContext()).show {
            message(null, title)
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.ok),
                    true
                )
            ) {
                dismiss()
            }
        }
    }

    private fun EnableSync(flag: Boolean) {
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

    var syncCount = 0
    private fun startSync(grnList1: List<VegaEcuadorDispatchWithLineItems>, _index: Int) {
        PreferenceHelper.save(Constants.START_SYNC, true)
        if (_index == 0) {
            binding.clProgress.visible()
            binding.textViewCount.text = grnList1.size.toString()
            binding.progressBar.max = grnList1.size
            binding.progressBar.progress = grnList1.size
            binding.clProgress.setBackgroundColor(Color.parseColor("#60000000"))
            //binding.clCardProgress.setBackgroundColor(Color.parseColor("#70000000"))
        }
        val _element = grnList1[_index]
        //grnList1.forEachIndexed { _index, _element ->
        val input = workDataOf(
            UIUtils.DISPATCH_DATA to _element.dispatch.wbTempId,
            UIUtils.DISPATCH_POST_DATA to _element.dispatch.deliveryDetail
        )
        val worker = getEcuadorDispatchOneTimeRequestWorker(input, _index)
        enQueueWorkerWithName(worker, _element.dispatch.wbTempId.toString(), requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val position = AppUtils.posExtension(workInfo.tags) + 1
                            syncCount++
                            binding.textViewCount.text = grnList1.size.minus(syncCount).toString()
                            binding.progressBar.progress = grnList1.size.minus(syncCount)
                            if (syncCount != grnList1.size) startSync(grnList1, syncCount)
                            if (syncCount == grnList1.size) {
                                syncCount = 0
                                binding.clProgress.gone()
                                PreferenceHelper.save(Constants.START_SYNC, false)
                            }
//                                    hideLoading()
                        }
                        WorkInfo.State.FAILED -> {
                            val position = AppUtils.posExtension(workInfo.tags) + 1
                            syncCount++
                            binding.textViewCount.text = grnList1.size.minus(syncCount).toString()
                            binding.progressBar.progress = grnList1.size.minus(syncCount)
                            if (syncCount != grnList1.size) startSync(grnList1, syncCount)
                            if (syncCount == grnList1.size) {
                                syncCount = 0
                                binding.clProgress.gone()
                                PreferenceHelper.save(Constants.START_SYNC, false)
                            }

//                                    hideLoading()
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                    }
                }
            })
        //if (_index == grnList1.size - 1) {}
        //}
    }
}

class OverlapDecoration : RecyclerView.ItemDecoration() {
    var vertOverlap = -3
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(vertOverlap, 0, 0, 0)
    }
}
