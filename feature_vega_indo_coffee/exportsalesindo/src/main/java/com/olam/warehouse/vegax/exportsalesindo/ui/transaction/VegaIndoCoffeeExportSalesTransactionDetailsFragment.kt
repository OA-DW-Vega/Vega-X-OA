package com.olam.warehouse.vegax.exportsalesindo.ui.transaction

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.common.OverlapDecoration
import com.olam.warehouse.login.ui.quickpinaccess.createpin.VegaCreatePinActivity
import com.olam.warehouse.master.common.model.SyncStatusProgress
import com.olam.warehouse.master.vegaindocoffee.entity.IndoExporSalesMaterialList
import com.olam.warehouse.master.vegaindocoffee.entity.VegaIndoCoffeeExportSalesOrder
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.exportsalesindo.R
import com.olam.warehouse.vegax.exportsalesindo.databinding.FragmentVegaIndoCoffeeExsalesTransactionBinding
import com.olam.warehouse.vegax.exportsalesindo.ui.VegaIndoCoffeeExportSalesViewModel
import com.olam.warehouse.vegax.exportsalesindo.utils.MOVE_SUMMARY
import com.olam.warehouse.vegax.exportsalesindo.utils.prepareSalesOrderToMaterialList
import com.olam.warehouse.vegax.exportsalesindo.works.getIndoExportSalesOneTimeRequestWorker
import kotlinx.android.synthetic.main.item_indo_sales_sync_status_progress_bar.view.*
import kotlinx.android.synthetic.main.item_vega_indo_export_sales_transaction.view.*
import kotlinx.android.synthetic.main.item_vega_indo_export_sales_transaction.view.ivScaleClose
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 4/27/2021.
 */
class VegaIndoCoffeeExportSalesTransactionDetailsFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_indo_coffee_exsales_transaction
    private lateinit var binding: FragmentVegaIndoCoffeeExsalesTransactionBinding
    private val vm: VegaIndoCoffeeExportSalesViewModel by viewModel()
    private var offloadingList = arrayListOf<VegaIndoCoffeeExportSalesOrder>()
    private var grnSearchList = arrayListOf<VegaIndoCoffeeExportSalesOrder>()
    private var grnSortList = arrayListOf<VegaIndoCoffeeExportSalesOrder>()
    private var selectedReceiving = VegaIndoCoffeeExportSalesOrder()
    private var callBack: CallBack? = null
//    private var priceEdit = false
    private val REQUEST_CODE = 220
    private val LOCK_REQUEST_CODE = 221
    private val SECURITY_SETTING_REQUEST_CODE = 233

    companion object {
        fun newInstance() = VegaIndoCoffeeExportSalesTransactionDetailsFragment().putArgs {
        }
    }

    interface CallBack {
        fun replaceSummaryFragment(
            fragment: String,
            data: Any,
            data1: Any,
            materialList: ArrayList<IndoExporSalesMaterialList>
        )

        fun replaceFragment(it: VegaIndoCoffeeExportSalesOrder)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentVegaIndoCoffeeExsalesTransactionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        PreferenceHelper.save(Constants.START_SYNC, false)
        binding.clProgress.gone()
        vm.exportSalesLocal.observe(this, Observer {
            if (it != null) {
                updateUI(it)
            }
        })
        vm.getIndoExportSalesItem()

        binding.tvPending.setOnClickListener { moveToPending() }
        binding.tvHistory.setOnClickListener { moveToHistory() }
        binding.etSearchVendor.onChange { it ->
            if (it.isNotEmpty()) {
                grnSearchList.clear()
                offloadingList.filter { it.status == 4 }.forEach { item ->
                    if (item.saleOrderId?.contains(it, true) == true || item.saleOrderId?.contains(
                                    it,
                                    true
                            ) == true
                    ) grnSearchList.add(item)
                }
                setupAdapter(grnSearchList)
            } else {
                setupAdapter(offloadingList.filter { it.status == 4 })
            }
        }
        binding.tvSortByDate.setOnClickListener { sortByDate() }
        binding.tvSync.setOnClickListener {
            if (offloadingList.any { it.status != 4 }) showConfirmDialog()
            else activity?.toast(getString(R.string.no_data_found))
        }
        enableSync(AppUtils.isOnline())
    }

    private fun updateUI(data: List<VegaIndoCoffeeExportSalesOrder>?) {
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
        setupAdapter(offloadingList.filter { it.status == 4 })
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
        val count = offloadingList.filter { it.status != 4 }
        binding.tvPendingCount.text = count.size.toString()
        if (count.isNotEmpty()) binding.tvPendingCount.visible() else binding.tvPendingCount.gone()
        enableSync(count.isNotEmpty() && AppUtils.isOnline())
        setupAdapter(offloadingList.filter { it.status != 4 })
    }

    private fun sortByDate() {
        if (offloadingList.any { it.status == 4 }) {
            grnSortList =
                    offloadingList.filter { it.status == 4 } as ArrayList<VegaIndoCoffeeExportSalesOrder>
            if (grnSortList.size > 0)
                setupAdapter(grnSortList.asReversed())
        }
    }

    private fun setupAdapter(itemList: List<VegaIndoCoffeeExportSalesOrder>) {
        if (itemList.isNotEmpty()) {
            binding.rvTransaction.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvTransaction.gone()
            binding.tvNoData.visible()
        }
        binding.rvTransaction.setUp(
            itemList as ArrayList,
            R.layout.item_vega_indo_export_sales_transaction,
            { it, pos ->
                tvOffloadTempIdValue.text = it.tmpId
                tvMaterialValue.text = it.materialName
                tvBatchNoValue.text = it.saleOrderId
                tvProcureTypeValue.text = it.openQuantity.plus(" ").plus(it.unitOfMeasure)
                tvErrorValue.text =
                        /*if (it.syncStarted == 1) getString(R.string.sync_already_triggered) else*/
                    it.syncStatusMsg
                //if(it.isSynced) ivScaleClose.gone() else ivScaleClose.visible()
                if (it.erdat?.isNotEmpty() == true)
                    tvDateValue.text = it.erdat.let { it1 ->
                        it1?.let { it2 ->
                            DateUtils.getUTCDateTimeNicaragua(
                                it2,
                                App.getAppContext()
                            )
                        }
                    }
                //ivScaleClose.setOnClickListener {view -> showItemDeleteDialog(it.tmpWbId) }
                ivEdit.setOnClickListener { view ->
                    val popupMenu = PopupMenu(requireActivity(), view)
                    popupMenu.menuInflater.inflate(
                        com.olam.warehouse.login.R.menu.transaction_menu,
                        popupMenu.menu
                    )
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_Sync).isVisible =
                        false
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_copy).isVisible =
                        false
                    if (it.isSynced == true) {
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
                                moveViewSummary(it)
                            }
                            com.olam.warehouse.login.R.id.action_edit1 -> {
                                moveEdit(it)
                            }
                            com.olam.warehouse.login.R.id.action_delete -> {
                                showItemDeleteDialog(it.tmpId)
                            }
                        }
                        true
                    }
                    popupMenu.show()
                }
                if (it.syncStatusMsg.isNullOrEmpty() || it.isSynced == true) {
                    tvReadMore.gone()
                    tvError.gone()
                    tvErrorValue.gone()
                } else {
                    tvReadMore.visible()
                    tvError.visible()
                    tvErrorValue.visible()
                }
                if (it.syncStatusMsg.isNullOrEmpty() && it.status != 4) clErrorStatus.gone() else clErrorStatus.visible()
                tvReadMore.setOnClickListener { view -> showErrorDialog(it.syncStatusMsg.toString()) }
                val itemList = arrayListOf<SyncStatusProgress>()
                val data1 =
                    SyncStatusProgress(1, getString(R.string.delivery), it.deliveryFlag == true)
                val data2 =
                    SyncStatusProgress(2, getString(R.string.picking), it.pickingFlag == true)
                itemList.add(data1)
                itemList.add(data2)
                rvSynProgress.addItemDecoration(OverlapDecoration())
                rvSynProgress.setUp(
                    itemList.sortedBy { it.itemOrder }.toMutableList(),
                    R.layout.item_indo_sales_sync_status_progress_bar,
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

    private fun moveEdit(it: VegaIndoCoffeeExportSalesOrder) {
        it.isView = false
//        val data = it
        callBack?.replaceFragment(it)
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.sync_all)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    startSync(offloadingList.filter { it.status != 4 }, 0)
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
                startSync(offloadingList.filter { it.status != 4 }, 0)
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
                startSync(offloadingList.filter { it.status != 4 }, 0)
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
                startSync(offloadingList.filter { it.status != 4 }, 0)
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

    private fun showItemDeleteDialog(tmpId: String) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.deletedItem(tmpId)
                },
                { dismiss() })
        }
    }

    private fun moveViewSummary(it: VegaIndoCoffeeExportSalesOrder) {
        selectedReceiving = it
        it.isView = true
        val materilList = prepareSalesOrderToMaterialList(it)
        callBack?.replaceSummaryFragment(MOVE_SUMMARY, it.saleOrderId.toString(), it.tmpId, materilList)
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

    var syncCount = 0
    private fun startSync(grnList1: List<VegaIndoCoffeeExportSalesOrder>, _index: Int) {
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
        val input = workDataOf(UIUtils.DISPATCH_DATA to _element.tmpId)
        val worker = getIndoExportSalesOneTimeRequestWorker(input, _index)
        enQueueWorkerWithName(worker, _element.tmpId, requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
//                            val position = AppUtils.posExtension(workInfo.tags) + 1
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
//                            val position = AppUtils.posExtension(workInfo.tags) + 1
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
