package com.olam.warehouse.vegax.grnindo.ui.offline

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
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.quickpinaccess.createpin.VegaCreatePinActivity
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnindo.R
import com.olam.warehouse.vegax.grnindo.databinding.FragmentVegaIndoCoffeeGrnTransactionBinding
import com.olam.warehouse.vegax.grnindo.databinding.ItemVegaIndoGrnTransactionBinding
import com.olam.warehouse.vegax.grnindo.ui.VegaIndoCoffeeGrnViewModel
import com.olam.warehouse.vegax.grnindo.utils.GRN_FRAG
import com.olam.warehouse.vegax.grnindo.works.getIndoGrnOneTimeRequestWorker
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 4/26/2021.
 */
class VegaIndoCoffeeGrnTransactionDetailsFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_indo_coffee_grn_transaction
    private lateinit var binding: FragmentVegaIndoCoffeeGrnTransactionBinding
    private val vm: VegaIndoCoffeeGrnViewModel by viewModel()
    private var offloadingList = arrayListOf<VegaGrnWeighBridgeId>()
    private var grnSearchList = arrayListOf<VegaGrnWeighBridgeId>()
    private var grnSortList = arrayListOf<VegaGrnWeighBridgeId>()
    private var selectedReceiving = VegaGrnWeighBridgeId()
    private var callBack: CallBack? = null
//    private var priceEdit = false
    private val REQUEST_CODE = 220
    private val LOCK_REQUEST_CODE = 221
    private val SECURITY_SETTING_REQUEST_CODE = 233

    companion object {
        fun newInstance() = VegaIndoCoffeeGrnTransactionDetailsFragment().putArgs {
        }
    }

    interface CallBack {
        fun replaceFragment(moveFrag: String, vegaGrnWeighBridgeId: Any)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentVegaIndoCoffeeGrnTransactionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        PreferenceHelper.save(Constants.START_SYNC, false)
        binding.clProgress.gone()
        vm.weighBridgeTrans.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getWeighBridgeTransDetail()
        /*vm.quality.observe(viewLifecycleOwner, Observer {moveSummary(it) })*/

        binding.tvPending.setOnClickListener { moveToPending() }
        binding.tvHistory.setOnClickListener { moveToHistory() }
        /*val data1 = VegaReceiving(tmpWbId = "Tmp1234")
        grnList.add(data1)
        setupAdapter(grnList)*/
        binding.etSearchVendor.onChange { it ->
            if (it.isNotEmpty()) {
                grnSearchList.clear()
                offloadingList.filter { it.status == 4 }.forEach { item ->
                    if (item.supplierName?.contains(it, true) == true || item.supplierCode?.contains(
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
        EnableSync(AppUtils.isOnline())
    }

    private fun updateUI(data: List<VegaGrnWeighBridgeId>?) {
        offloadingList.clear()
        data?.let { offloadingList.addAll(it) }
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
        EnableSync(count.isNotEmpty() && AppUtils.isOnline())
        setupAdapter(offloadingList.filter { it.status != 4 })
    }

    private fun sortByDate() {
        if (offloadingList.any { it.status == 4 }) {
            grnSortList =
                    offloadingList.filter { it.status == 4 } as ArrayList<VegaGrnWeighBridgeId>
            if (grnSortList.size > 0)
                setupAdapter(grnSortList.asReversed())
        }
    }

    private fun setupAdapter(itemList: List<VegaGrnWeighBridgeId>) {
        if (itemList.isNotEmpty()) {
            binding.rvTransaction.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvTransaction.gone()
            binding.tvNoData.visible()
        }
        binding.rvTransaction.setUpAdapter(
            itemList as ArrayList,
            R.layout.item_vega_indo_grn_transaction,
            ItemVegaIndoGrnTransactionBinding::inflate,
            { it, pos, bindItem ->
                if (it.status == 4) {
                    bindItem.tvOffloadTempId.text = getString(R.string.grn)
                    bindItem.tvOffloadTempIdValue.text = it.grnNumber
                } else {
                    bindItem.tvOffloadTempId.text = getString(R.string.weigh_bridge_id)
                    bindItem.tvOffloadTempIdValue.text =
                        if (it.weighBridgeId?.isNotEmpty() == true) it.weighBridgeId else it.wbTempId
                }
                bindItem.tvVendorValue.text =
                    if (it.supplierName?.isNotEmpty() == true) it.supplierName else it.transportVendorCode
                bindItem.tvMaterialValue.text = it.materialName
                bindItem.tvBatchNoValue.text = it.storageLocationCode
                bindItem.tvProcureTypeValue.text = it.purchaseType
                bindItem.tvErrorValue.text =
                        /*if (it.syncStarted == 1) getString(R.string.sync_already_triggered) else*/
                    it.message
                //if(it.isSynced) ivScaleClose.gone() else ivScaleClose.visible()
                if ("null" != it.erdat) {
                    val times = it.erdat?.split('(', ')')
                    bindItem.tvDateValue.text = times?.get(1)?.let { it1 ->
                        DateUtils.getUTCDateTime(
                            it1,
                            App.getAppContext()
                        )
                    }
                }
                //ivScaleClose.setOnClickListener {view -> showItemDeleteDialog(it.tmpWbId) }
                bindItem.ivEdit.setOnClickListener { view ->
                    val popupMenu = PopupMenu(requireActivity(), view)
                    popupMenu.menuInflater.inflate(
                        com.olam.warehouse.login.R.menu.transaction_menu,
                        popupMenu.menu
                    )
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_Sync).isVisible =
                        false
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_copy).isVisible =
                        false
                    if (it.isSyncStatus) {
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
                                moveViewSummary(it)
                            }
                            com.olam.warehouse.login.R.id.action_edit1 -> {
                                moveEdit(it)
                            }
                            com.olam.warehouse.login.R.id.action_delete -> {
                                showItemDeleteDialog(it.wbTempId)
                            }
                        }
                        true
                    }
                    popupMenu.show()
                }
                if (it.message.isNullOrEmpty() || it.isSyncStatus) {
                    bindItem.tvReadMore.gone()
                    bindItem.tvError.gone()
                    bindItem.tvErrorValue.gone()
                } else {
                    bindItem.tvReadMore.visible()
                    bindItem.tvError.visible()
                    bindItem.tvErrorValue.visible()
                }
                if (it.message.isNullOrEmpty() && it.status != 4) bindItem.clErrorStatus.gone() else bindItem.clErrorStatus.visible()
                bindItem.tvReadMore.setOnClickListener { view -> showErrorDialog(it.message.toString()) }

                //displayFragment(VegaSynStatusProgressFragment.newInstance(itemList), false)
            }, {
                // movePriceCalculationSummary(this)
            })
    }

    private fun moveEdit(weighBridge: VegaGrnWeighBridgeId) {
        weighBridge.isView = false
        callBack?.replaceFragment(GRN_FRAG, weighBridge)
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

    /**
     * method to return whether device has screen lock enabled or not
     */
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
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.deleteAllItem(wbTempId)
                },
                { dismiss() })
        }
    }

    private fun moveViewSummary(weighBridge: VegaGrnWeighBridgeId) {
        selectedReceiving = weighBridge
        weighBridge.isView = true
        callBack?.replaceFragment(GRN_FRAG, weighBridge)
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
    private fun startSync(grnList1: List<VegaGrnWeighBridgeId>, _index: Int) {
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
        val input = workDataOf(UIUtils.TEMP_ID to _element.wbTempId, UIUtils.WB_ID to _element.weighBridgeType)
        val worker = getIndoGrnOneTimeRequestWorker(input, _index)
        enQueueWorkerWithName(worker, _element.wbTempId, requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, Observer { workInfo ->
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
                        else -> {}
                    }
                }
            })
        //if (_index == grnList1.size - 1) {}
        //}
    }
}

