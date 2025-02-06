package com.olam.warehouse.vegax.mtntnicaragua.ui.transaction

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
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMtnt
import com.olam.warehouse.master.veganicaragua.model.VegaMtntWithLotsWithBags
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntnicaragua.R
import com.olam.warehouse.vegax.mtntnicaragua.databinding.FragmentVegaNicaraguaMtntTransactionBinding
import com.olam.warehouse.vegax.mtntnicaragua.databinding.ItemNicMtntSyncStatusProgressBarBinding
import com.olam.warehouse.vegax.mtntnicaragua.databinding.ItemVegaNicMtntLotTransactionBinding
import com.olam.warehouse.vegax.mtntnicaragua.databinding.ItemVegaNicaraguaMtntTransactionBinding
import com.olam.warehouse.vegax.mtntnicaragua.ui.VegaNicaraguaMtntViewModel
import com.olam.warehouse.vegax.mtntnicaragua.utils.FRAG_CONSIGN
import com.olam.warehouse.vegax.mtntnicaragua.utils.FRAG_SUMMARY
import com.olam.warehouse.vegax.mtntnicaragua.work.getMtntWSOneTimeRequestWorker
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 11/20/2020.
 */
class VegaNicMtntTransactionFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_nicaragua_mtnt_transaction
    private lateinit var binding: FragmentVegaNicaraguaMtntTransactionBinding
    private val vm: VegaNicaraguaMtntViewModel by viewModel()
    private var mtntList = mutableListOf<VegaMtntWithLotsWithBags>()
    private var mtntSearchList = arrayListOf<VegaMtntWithLotsWithBags>()
    private var mtntSortList = mutableListOf<VegaMtntWithLotsWithBags>()
    private var selectedMtnt = VegaNicaraguaMtnt()
    private var callBack: CallBack? = null
    private var priceEdit = false
    private val REQUEST_CODE = 220
    private val LOCK_REQUEST_CODE = 221
    private val SECURITY_SETTING_REQUEST_CODE = 233
    var count:Int=0

    companion object {
        fun newInstance() = VegaNicMtntTransactionFragment().putArgs {
        }
    }

    interface CallBack {
        fun replaceFragment(moveFrag: String, mtntData: Any)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaMtntTransactionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        PreferenceHelper.save(Constants.START_SYNC_MTNT, false)
        binding.clProgress.gone()
        /* vm.grnTransList.observe(viewLifecycleOwner, Observer { updateUI(it) })
         vm.getReceivingWithLineItem()
         vm.quality.observe(viewLifecycleOwner, Observer {moveSummary(it) })*/

        binding.tvPending.setOnClickListener { moveToPending() }
        binding.tvHistory.setOnClickListener { moveToHistory() }
        /*val data1 = VegaNicaraguaMtnt(tmpWbId = "Tmp1234")
        mtntList.add(data1)
        setupAdapter(mtntList)*/
        binding.etSearchVendor.onChange {
            if (it.isNotEmpty()) {
                mtntSearchList.clear()
                mtntList.filter { it.mtnt.isSyncStatus == true }.forEach { item ->
                    /*if (item.supplierName?.contains(it, true) == true || item.supplierCode?.contains(
                            it,
                            true
                        ) == true
                    ) mtntSearchList.add(item)*/
                }
                //setupAdapter(mtntSearchList)
            } else {
                setupAdapter(mtntList.filter { it.mtnt.isSyncStatus == true }.toMutableList())
            }
        }
        vm.getConfigItems(UserRoles.DAYS_LIMIT.role)
        vm.configItems.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            updateConfigItems(it)
        })
        binding.tvSortByDate.setOnClickListener { sortByDate() }
        binding.tvSync.setOnClickListener {
            if (DateUtils.isFirstDayOfMonth(requireContext(), count)) {
                if (mtntList.filter { it.mtnt.isOfflineData == true }
                        .filter { it.mtnt.isSyncStatus == false }
                        .toMutableList().size > 0) showConfirmDialog()
                else activity?.toast(getString(R.string.no_data_found))
            } else {
                showSnack(getString(R.string.month_close_error))
            }

        }
        EnableSync(AppUtils.isOnline())

        vm.listWBLotsWithBags.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mtntList = it.toMutableList()
                moveToPending()
            }
        })
        vm.getListOfMtntWithLots()
    }

    /*private fun moveSummary(qualityItems: List<VegaQuality>) {
        if (!priceEdit)
            callBack?.replaceFragment(FRAG_SUMMARY, selectedMtnt, prepareQualityParamData(qualityItems))
        else {
            if (selectedMtnt.grnType?.contains("tolling", true) == true)
                callBack?.replaceFragment(GRN_QUALITY, selectedMtnt)
            else if (selectedMtnt.grnType?.contains("ptbf", true) == true)
                callBack?.replaceFragment(FRAG_PTBF_PRICING, selectedMtnt, prepareQualityParamData(qualityItems))
            else
                callBack?.replaceFragment(FRAG_PRICING, selectedMtnt, prepareQualityParamData(qualityItems))
        }
    }*/

    /*private fun updateUI(data: List<VegaNicaraguaMtnt>?) {
        mtntList.clear()
        data?.let { mtntList.addAll(it) }
        moveToPending()
    }*/

    private fun moveToHistory() {
        binding.clProgress.gone()
        binding.etSearchVendor.visible()
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
        binding.tvPendingCount.gone()
        setupAdapter(mtntList.filter { it.mtnt.isSyncStatus == true }.toMutableList())
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
        val count =
            mtntList.filter { it.mtnt.isSyncStatus == false }.filter { it.mtnt.isOfflineData == true }.toMutableList()
        binding.tvPendingCount.text = count.size.toString()
        if (count.size > 0) binding.tvPendingCount.visible() else binding.tvPendingCount.gone()
        EnableSync(count.size > 0 && AppUtils.isOnline())
        setupAdapter(mtntList.filter { it.mtnt.isSyncStatus == false }.filter { it.mtnt.isOfflineData == true }
            .toMutableList())
    }

    private fun sortByDate() {
        if (mtntList.filter { it.mtnt.isSyncStatus == true }.isNotEmpty()) {
            mtntSortList = mtntList.filter { it.mtnt.isSyncStatus == true }.toMutableList()
            if (mtntSortList.size > 0)
                setupAdapter(mtntSortList.asReversed())
        }
    }

    private fun setupAdapter(itemList: List<VegaMtntWithLotsWithBags>) {
        if (itemList.size > 0) {
            binding.rvTransaction.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvTransaction.gone()
            binding.tvNoData.visible()
        }
        binding.rvTransaction.setUpAdapter(
            itemList.toMutableList(),
            R.layout.item_vega_nicaragua_mtnt_transaction,
            ItemVegaNicaraguaMtntTransactionBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvMtntOBDIdValue.text = it.mtnt.purchaseDocNum
                bindItem.tvTransNo.text =
                    if (it.mtnt.delivery?.isNotEmpty() == true) getString(R.string.obd_no) else getString(
                        R.string.transaction_no
                    )
                bindItem.tvTransNoValue.text =
                    if (it.mtnt.delivery?.isNotEmpty() == true) it.mtnt.delivery else it.mtnt.tempId
                bindItem.tvSendPlantValue.text = it.mtnt.storageLocationCode
                bindItem.tvGradeValue.text = it.mtnt.qualityGrade
                bindItem.tvMaterialValue.text = it.mtnt.materialName
                bindItem.tvErrorValue.text =
                        /*if (it.mtnt.syncStarted == 1) getString(R.string.sync_already_triggered) else*/
                    it.mtnt.message
                //if(it.isSynced) ivScaleClose.gone() else ivScaleClose.visible()
                if (it.mtnt.erdat?.isNotEmpty() == true)
                    bindItem.tvDateValue.text = it.mtnt.erdat.let { it1 ->
                        it1?.let { it2 ->
                            DateUtils.getUTCDateTimeNicaragua(
                                it2,
                                App.getAppContext()
                            )
                        }
                    }
                //ivScaleClose.setOnClickListener {view -> showItemDeleteDialog(it.tmpWbId) }
                bindItem.ivEdit.setOnClickListener { view ->
                    val popupMenu: PopupMenu = PopupMenu(requireActivity(), view)
                    popupMenu.menuInflater.inflate(
                        com.olam.warehouse.login.R.menu.transaction_menu,
                        popupMenu.menu
                    )
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_Sync).isVisible =
                        false
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_copy).isVisible =
                        false
                    if (it.mtnt.isSyncStatus == true) {
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
                    popupMenu.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            com.olam.warehouse.login.R.id.action_edit -> {
                                moveSummaryPage(it.mtnt)
                            }
                            com.olam.warehouse.login.R.id.action_edit1 -> {
                                moveEdit(it)
                            }
                            com.olam.warehouse.login.R.id.action_delete -> {
                                showItemDeleteDialog(it.mtnt.tempId)
                            }
                        }
                        true
                    }
                    popupMenu.show()
                }
                if (it.mtnt.isSyncStatus == true) {
                    bindItem.tvReadMore.gone()
                    bindItem.tvError.gone()
                    bindItem.tvErrorValue.gone()
                } else {
                    bindItem.tvReadMore.visible()
                    bindItem.tvError.visible()
                    bindItem.tvErrorValue.visible()
                }
                if (it.mtnt.isSyncStatus == false && it.mtnt.isErrorStatus == false) bindItem.clErrorStatus.visible() else bindItem.clErrorStatus.gone()
                bindItem.tvReadMore.setOnClickListener { view -> showErrorDialog(it.mtnt.message.toString()) }

                bindItem.rvSyncLotStatus.setUpAdapter(
                    it.lineItems.toMutableList(),
                    R.layout.item_vega_nic_mtnt_lot_transaction,
                    ItemVegaNicMtntLotTransactionBinding::inflate,
                    { it, pos, bindItem ->
                        bindItem.tvLotId.text =
                            getString(R.string.lot_id).plus(" ").plus(it.lots.batchNumber)
                        val itemList = arrayListOf<SyncStatusProgress>()
                        val data1 =
                            SyncStatusProgress(
                                1,
                                getString(R.string.delivery_flag),
                                it.lots.deliveryFlag ?: false
                            )
                        val data2 =
                            SyncStatusProgress(
                                2,
                                getString(R.string.picking_flag),
                                it.lots.pickingFlag ?: false
                            )
                        val data3 = SyncStatusProgress(
                            3,
                            getString(R.string.pgi_flag),
                            it.lots.pgiFlag ?: false
                        )
                        itemList.add(data1)
                        itemList.add(data2)
                        itemList.add(data3)
                        //setupInnerAdpter(itemList)
                        bindItem.rvSynProgress.addItemDecoration(OverlapDecoration())
                        bindItem.rvSynProgress.setUpAdapter(
                            itemList.sortedBy { it.itemOrder }.toMutableList(),
                            R.layout.item_nic_mtnt_sync_status_progress_bar,
                            ItemNicMtntSyncStatusProgressBarBinding::inflate,
                            { it, pos, bindItem ->
                                if (pos == 0) bindItem.vBarLine.gone() else bindItem.vBarLine.visible()
                                bindItem.tvItemName.text = it.itemName
                                if (it.itemStatus) {
                                    ViewCompat.setBackgroundTintList(
                                        bindItem.vBarLine,
                                        context?.let {
                                            ContextCompat.getColorStateList(
                                                it,
                                                com.olam.warehouse.presentation.R.color.green
                                            )
                                        }
                                    )
                                    bindItem.ivScaleClose.setImageResource(com.olam.warehouse.presentation.R.drawable.ic_check_circle_black_24dp)
                                    bindItem.ivScaleClose.imageTintList =
                                        ContextCompat.getColorStateList(
                                            bindItem.ivScaleClose.context,
                                            com.olam.warehouse.presentation.R.color.green
                                        )
                                } else {
                                    ViewCompat.setBackgroundTintList(
                                        bindItem.vBarLine,
                                        context?.let {
                                            ContextCompat.getColorStateList(
                                                it,
                                                com.olam.warehouse.presentation.R.color.grey_light
                                            )
                                        }
                                    )
                                    bindItem.ivScaleClose.setImageResource(com.olam.warehouse.presentation.R.drawable.ic_coffee_cicle_close)
                                    bindItem.ivScaleClose.imageTintList =
                                        ContextCompat.getColorStateList(
                                            bindItem.ivScaleClose.context,
                                            com.olam.warehouse.presentation.R.color.red_ff
                                        )
                                }
                            },
                            {},
                            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                        )
                    })
                //displayFragment(VegaSynStatusProgressFragment.newInstance(itemList), false)
            },
            {
                // movePriceCalculationSummary(this)
            })
    }

    private fun moveEdit(item: VegaMtntWithLotsWithBags) {
        var isHeaderEdit = true
        item.lineItems.forEach {
            if (it.lots.deliveryFlag == true || it.lots.pickingFlag == true || it.lots.pgiFlag == true) isHeaderEdit =
                false
        }
        if (isHeaderEdit)
            callBack?.replaceFragment(FRAG_CONSIGN, item.mtnt)
        else
            callBack?.replaceFragment(FRAG_SUMMARY, item.mtnt)

    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.sync_all)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    //                startSync(mtntList.filter { it.mtnt.isOfflineData == true }.filter { it.mtnt.isSyncStatus == false }.toMutableList(), 0)
                    val isSecurityPin = PreferenceHelper.get(Constants.IS_SECURITY_PIN, false)
                    when (isSecurityPin) {
                        true -> checkQuickPin() // Quick Pin Access
                        else -> startSync(mtntList.filter { it.mtnt.isOfflineData == true }
                            .filter { it.mtnt.isSyncStatus == false }.toMutableList(), 0)
                    }
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
                startSync(mtntList.filter { it.mtnt.isOfflineData == true }.filter { it.mtnt.isSyncStatus == false }
                    .toMutableList(), 0)
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
                startSync(mtntList.filter { it.mtnt.isOfflineData == true }.filter { it.mtnt.isSyncStatus == false }
                    .toMutableList(), 0)
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
                startSync(mtntList.filter { it.mtnt.isOfflineData == true }.filter { it.mtnt.isSyncStatus == false }
                    .toMutableList(), 0)
            } else {
                //If screen lock authentication is failed update text
//                textView.setText(resources.getString(R.string.unlock_failed))
            }
        }
    }

    /**
     * method to return whether device has screen lock enabled or not
     */
    private fun isDeviceSecure(): Boolean {
        val keyguardManager = activity?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        //this method only work whose api level is greater than or equal to Jelly_Bean (16)
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && keyguardManager.isKeyguardSecure
        //You can also use keyguardManager.isDeviceSecure(); but it requires API Level 23
    }

    private fun showItemDeleteDialog(tmpWbId: String) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    vm.deleteAllItem(tmpWbId)
                },
                { dismiss() })
        }
    }

    private fun moveSummaryPage(vegaNicaraguaMtnt: VegaNicaraguaMtnt) {
        selectedMtnt = vegaNicaraguaMtnt
        selectedMtnt.isView = true
        callBack?.replaceFragment(FRAG_SUMMARY, selectedMtnt)
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
    private fun startSync(mtntList1: MutableList<VegaMtntWithLotsWithBags>, _index: Int) {
        PreferenceHelper.save(Constants.START_SYNC_MTNT, true)
        if (_index == 0) {
            binding.clProgress.visible()
            binding.textViewCount.text = mtntList1.size.toString()
            binding.progressBar.max = mtntList1.size
            binding.progressBar.progress = mtntList1.size
            binding.clProgress.setBackgroundColor(Color.parseColor("#60000000"))
            //binding.clCardProgress.setBackgroundColor(Color.parseColor("#70000000"))
        }
        val _element = mtntList1[_index]
        //mtntList1.forEachIndexed { _index, _element ->
        val input = workDataOf(UIUtils.MTNT_DATA to _element.mtnt.tempId)
        val worker = getMtntWSOneTimeRequestWorker(input, _index)
        enQueueWorkerWithName(worker, _element.mtnt.tempId, requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val position = AppUtils.posExtension(workInfo.tags) + 1
                            syncCount++
                            binding.textViewCount.text = mtntList1.size.minus(syncCount).toString()
                            binding.progressBar.progress = mtntList1.size.minus(syncCount)
                            if (syncCount != mtntList1.size) startSync(mtntList1, syncCount)
                            if (syncCount == mtntList1.size) {
                                syncCount = 0
                                binding.clProgress.gone()
                                PreferenceHelper.save(Constants.START_SYNC_MTNT, false)
                            }
//                                    hideLoading()
                        }
                        WorkInfo.State.FAILED -> {
                            val position = AppUtils.posExtension(workInfo.tags) + 1
                            syncCount++
                            binding.textViewCount.text = mtntList1.size.minus(syncCount).toString()
                            binding.progressBar.progress = mtntList1.size.minus(syncCount)
                            if (syncCount != mtntList1.size) startSync(mtntList1, syncCount)
                            if (syncCount == mtntList1.size) {
                                syncCount = 0
                                binding.clProgress.gone()
                                PreferenceHelper.save(Constants.START_SYNC_MTNT, false)
                            }
//                                    hideLoading()
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                        else -> {}
                    }
                }
            })
        if (_index == mtntList1.size - 1) {
            val currentBatch = PreferenceHelper.get(Constants.LOT_SEQUENCE, "")
            vm.postUpdateLotSequence(currentBatch)
        }
        //}
    }
    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.DAYS_LIMIT.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> {
                            if(!it.value!!.isNullOrEmpty())
                                count=it.value!!.toInt()
                            else
                                count=0
                        }
                        it.applicable?.contains("N")!! -> {
                            count=0
                        }
                    }
                }
            }
        }
    }
}

