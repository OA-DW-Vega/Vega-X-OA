package com.olam.warehouse.vegax.grnnicaragua.ui.transaction

import android.app.Activity
import android.app.DatePickerDialog
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
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
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.SyncStatusProgress
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaGrnPost
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.posExtension
import com.olam.warehouse.presentation.utils.UIUtils.GRN_OUTPUT_DATA
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnnicaragua.R
import com.olam.warehouse.vegax.grnnicaragua.databinding.FragmentVegaNicaraguaTransactionBinding
import com.olam.warehouse.vegax.grnnicaragua.databinding.ItemNicaraguaSyncStatusProgressBarBinding
import com.olam.warehouse.vegax.grnnicaragua.databinding.ItemVegaNicaraguaTransactionBinding
import com.olam.warehouse.vegax.grnnicaragua.ui.VegaNicaraguaGrnViewModel
import com.olam.warehouse.vegax.grnnicaragua.utils.*
import com.olam.warehouse.vegax.grnnicaragua.work.getGrnOneTimeRequestWorker
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 9/28/2020.
 */
class VegaNicaraguaTransactionFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_nicaragua_transaction
    private lateinit var binding: FragmentVegaNicaraguaTransactionBinding
    private val vm: VegaNicaraguaGrnViewModel by viewModel()
    private var grnList = arrayListOf<VegaReceiving>()
    private var grnSearchList = arrayListOf<VegaReceiving>()
    private var grnSortList = arrayListOf<VegaReceiving>()
    private val reversedlist =  arrayListOf<VegaReceiving>()
    private var selectedReceiving = VegaReceiving()
    private var callBack: CallBack? = null
    private var priceEdit = false
    private val REQUEST_CODE = 220
    private val LOCK_REQUEST_CODE = 221
    private val SECURITY_SETTING_REQUEST_CODE = 233
    var count: Int = 0
    private lateinit var currentdate: Date
    private var addedWorkList = arrayListOf<String>()

    companion object {
        fun newInstance() = VegaNicaraguaTransactionFragment().putArgs {
        }
    }

    interface CallBack {
        fun replaceFragment(
            moveFrag: String, receivingData: Any, qualityParameterList: ArrayList<VegaQualityParameter?>
        )

        fun replaceFragment(moveFrag: String, receivingData: Any)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaTransactionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        PreferenceHelper.save(Constants.START_SYNC, false)
        binding.clProgress.gone()
        vm.grnTransList.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getReceivingWithLineItem()
        vm.quality.observe(viewLifecycleOwner, Observer { moveSummary(it) })

        vm.configItems.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            updateConfigItems(it)
        })
        vm.getConfigItems(UserRoles.DAYS_LIMIT.role)
        binding.tvPending.setOnClickListener { moveToPending() }
        binding.tvHistory.setOnClickListener { moveToHistory() }
        /*val data1 = VegaReceiving(tmpWbId = "Tmp1234")
        grnList.add(data1)
        setupAdapter(grnList)*/
        binding.etSearchVendor.onChange {
            if (it.isNotEmpty()) {
                grnSearchList.clear()
                grnList.filter { it.status.equals(Status.SYNC_COMPLETED) }.forEach { item ->
                    if (item.supplierName?.contains(it, true) == true || item.supplierCode?.contains(
                            it,
                            true
                        ) == true
                    ) grnSearchList.add(item)
                }
                setupAdapter(grnSearchList)
            } else {
                setupAdapter(grnList.filter { it.status.equals(Status.SYNC_COMPLETED) })
            }
        }
        binding.tvSortByDate.setOnClickListener { sortByDate() }
        binding.tvSync.setOnClickListener {
            if (DateUtils.isFirstDayOfMonth(requireContext(), count)) {
                if (grnList.filter { it.status.equals(Status.SYNC_PENDING) }.size > 0) showConfirmDialog()
                else activity?.toast(getString(R.string.no_data_found))
            } else {
                showSnack(getString(R.string.month_close_error))
            }
        }
        EnableSync(AppUtils.isOnline())
        val millis = System.currentTimeMillis()
        currentdate = Date(millis)
    }

    private fun moveSummary(qualityItems: List<VegaQuality>) {
        if (!priceEdit)
            callBack?.replaceFragment(FRAG_SUMMARY, selectedReceiving, prepareQualityParamData(qualityItems))
        else {
            if (selectedReceiving.grnType?.contains("tolling", true) == true)
                callBack?.replaceFragment(GRN_QUALITY, selectedReceiving)
            else if (selectedReceiving.grnType?.contains("ptbf", true) == true)
                callBack?.replaceFragment(FRAG_PTBF_PRICING, selectedReceiving, prepareQualityParamData(qualityItems))
            else
                callBack?.replaceFragment(FRAG_PRICING, selectedReceiving, prepareQualityParamData(qualityItems))
        }
    }

    private fun updateUI(data: List<VegaReceiving>?) {
        grnList.clear()
        data?.let { grnList.addAll(it) }
        moveToPending()
    }

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
        setupAdapter(grnList.filter { it.status.equals(Status.SYNC_COMPLETED) })
    }

    private fun moveToPending() {
        binding.etSearchVendor.gone()
        binding.tvSortByDate.gone()
        binding.tvSync.visible()
        binding.tvPending.setBackgroundColor(
            ContextCompat.getColor(
                binding.tvPending.context,
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
        val count = grnList.filter { it.status.equals(Status.SYNC_PENDING) }
        binding.tvPendingCount.text = count.size.toString()
        if (count.size > 0) binding.tvPendingCount.visible() else binding.tvPendingCount.gone()
        EnableSync(count.size > 0 && AppUtils.isOnline())
        setupAdapter(grnList.filter { it.status.equals(Status.SYNC_PENDING) })
    }

    private fun sortByDate() {
        try {
            if(reversedlist.isNotEmpty()){
                val rlist = reversedlist.reversed()
                reversedlist.clear()
                reversedlist.addAll(rlist)
                setupAdapter(reversedlist)
            }else {
                var grnLis = grnList.filter { it.status.equals(Status.SYNC_COMPLETED) }
                if (grnLis.isNotEmpty()) {
                    grnSortList.clear()
                    grnSortList.addAll(grnLis)
                    reversedlist.clear()
                    if (grnSortList.size > 0) {
                        val rlist = grnSortList.reversed()
                        reversedlist.addAll(rlist)
                        setupAdapter(reversedlist)
                    }
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun setupAdapter(itemList: List<VegaReceiving>) {
        if (itemList.size > 0) {
            binding.rvTransaction.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvTransaction.gone()
            binding.tvNoData.visible()
        }
        binding.rvTransaction.setUpAdapter(
            itemList as ArrayList,
            R.layout.item_vega_nicaragua_transaction,
            ItemVegaNicaraguaTransactionBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvGrnTempIdValue.text =
                    if (it.grnNumber?.isNotEmpty() == true) it.grnNumber else it.palletType
                bindItem.tvVendorValue.text = it.supplierName
                bindItem.tvMaterialValue.text = it.materialName
                bindItem.tvBatchNoValue.text = it.batchNumber
                if(it.invoiceNumber?.isNotEmpty() == true && it.invoiceNumber?.contains("TEM")==false){
                    bindItem.tvInvoiceNoValue.visible()
                    bindItem.tvInvoiceNo.visible()
                }else{
                    bindItem.tvInvoiceNoValue.gone()
                    bindItem.tvInvoiceNo.gone()
                }
                bindItem.tvInvoiceNoValue.text = it.invoiceNumber

                if (!it.postDate.toString().isNullOrEmpty()) {
                    bindItem.tvpostingdateval.text = it.postDate
                    if (isdatefunc(it.postDate.toString()))
                        bindItem.clAlertStatus.gone()
                    else
                        bindItem.clAlertStatus.visible()

                } else
                    bindItem.clAlertStatus.gone()

                bindItem.tvErrorValue.text =
                        /*if (it.syncStarted == 1) getString(R.string.sync_already_triggered) else*/
                    it.syncStatusMsg
                //if(it.isSynced) ivScaleClose.gone() else ivScaleClose.visible()
                if (it.erdat?.isNotEmpty() == true)
                    bindItem.tvDateValue.text = it.erdat.let { it1 ->
                        it1?.let { it2 ->
                            DateUtils.getUTCDateTimeNicaragua(
                                it2,
                                App.getAppContext()
                            )
                        }
                    }
                if(it.deleteFlag == true){
                    bindItem.clCard.setBackgroundColor( getColor(com.olam.warehouse.presentation.R.color.grey_lits))
                    bindItem.tvDeletedItem.visible()
                }else{
                    bindItem.tvDeletedItem.gone()
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
                    if (it.wbFlag == true && it.qcFlag == true && it.grnFlag == true) {
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit).isVisible =
                            true
                    } else if (it.wbFlag == false && it.qcFlag == false && it.grnFlag == false) {

                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit1).isVisible =
                            true
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_delete).isVisible =
                            true
                    } else {
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit).isVisible =
                            true

                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_delete).isVisible =
                            true
                    }

                    if(it.deleteFlag == true){
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit1).isVisible =
                            false

                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_delete).isVisible =
                            false
                    }
                    popupMenu.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            com.olam.warehouse.login.R.id.action_edit -> {
                                movePriceCalculationSummary(it)
                            }
                            com.olam.warehouse.login.R.id.action_edit1 -> {
                                getDatePickerDialog(bindItem.tvpostingdateval, it)
                                //moveEdit(it)
                            }
                            com.olam.warehouse.login.R.id.action_delete -> {
                                showItemDeleteDialog(it.tmpWbId)
                            }
                        }
                        true
                    }
                    popupMenu.show()
                }
                if (it.syncStatusMsg.isNullOrEmpty() || it.isSynced) {
                    bindItem.tvReadMore.gone()
                    bindItem.tvError.gone()
                    bindItem.tvErrorValue.gone()
                } else {
                    bindItem.tvReadMore.visible()
                    bindItem.tvError.visible()
                    bindItem.tvErrorValue.visible()
                }
                if (it.syncStatusMsg.isNullOrEmpty() && it.status.equals(Status.SYNC_PENDING)) bindItem.clErrorStatus.gone() else bindItem.clErrorStatus.visible()
                bindItem.tvReadMore.setOnClickListener { view -> showErrorDialog(it.syncStatusMsg.toString()) }
                val itemList = arrayListOf<SyncStatusProgress>()
                val data1 =
                    SyncStatusProgress(1, getString(R.string.wbid_status), it.wbFlag == true)
                val data2 =
                    SyncStatusProgress(2, getString(R.string.quality_status), it.qcFlag == true)
                val data3 =
                    SyncStatusProgress(3, getString(R.string.grn_status), it.grnFlag == true)
                val data4 = SyncStatusProgress(
                    3,
                    getString(R.string.invoice_status),
                    it.invoiceFlag == true
                )
                itemList.add(data1)
                itemList.add(data2)
                itemList.add(data3)
                if (!it.grnType.equals(getString(R.string.ptbf)) && it.grnType?.contains(
                        getString(R.string.toll),
                        true
                    ) == false
                ) {
                    itemList.add(data4)
                }

                //setupInnerAdpter(itemList)
                bindItem.rvSynProgress.addItemDecoration(OverlapDecoration())
                bindItem.rvSynProgress.setUpAdapter(
                    itemList.sortedBy { it.itemOrder }.toMutableList(),
                    R.layout.item_nicaragua_sync_status_progress_bar,
                    ItemNicaraguaSyncStatusProgressBarBinding::inflate,
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
                            bindItem.ivScaleClose.imageTintList = ContextCompat.getColorStateList(
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
                            bindItem.ivScaleClose.imageTintList = ContextCompat.getColorStateList(
                                bindItem.ivScaleClose.context,
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


    private fun getDatePickerDialog(
        tvpostingdateval: AppCompatTextView,
        vegaReceiving: VegaReceiving
    ) {
        val cal = Calendar.getInstance()
        val dateTxt = tvpostingdateval.text.split("/")
        cal.set(dateTxt[2].toInt(), dateTxt[1].toInt() - 1, dateTxt[0].toInt())
        val DATE_FORMAT = "dd/MM/yyyy"
        val UTC = "UTC"
        context?.let {
            val datePicker = DatePickerDialog(
                it,
                com.olam.warehouse.presentation.R.style.DatePickerTheme,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val sdf = SimpleDateFormat(DATE_FORMAT)
                    sdf.timeZone = TimeZone.getTimeZone(UTC)
                    tvpostingdateval.text = sdf.format(cal.time)
                    vegaReceiving.postDate = tvpostingdateval.text.toString()

                    vm.updatePostingDate(vegaReceiving.tmpWbId, tvpostingdateval.text.toString())
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            // datePicker.datePicker.maxDate = System.currentTimeMillis()
            datePicker.show()
            datePicker.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun moveEdit(it: VegaReceiving) {
        it.isEdit = true
        PreferenceHelper.save(Constants.IS_EDIT_TRANS_VALUE_CHANGED, false)
        PreferenceHelper.save(Constants.IS_EDIT_TRANS, true)
        if (it.wbFlag == true && it.qcFlag == false && it.grnFlag == false)
            callBack?.replaceFragment(GRN_QUALITY, it)
        else if (it.wbFlag == true && it.qcFlag == true && it.grnFlag == false) {
            priceEdit = true
            selectedReceiving = it
            vm.getQuality(it.tmpWbId)
        } else {
            if (it.grnType?.contains("fixed", true) == true)
                callBack?.replaceFragment(GRN_FIXED, it)
            else
                callBack?.replaceFragment(GRN_SPOT, it)
        }


    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.sync_all)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    val isSecurityPin = PreferenceHelper.get(Constants.IS_SECURITY_PIN, false)
                    when (isSecurityPin) {
                        true -> checkQuickPin() // Quick Pin Access
                        else -> {
                            syncInitiateFirstTime()
                        }
                    }
//                checkQuickPin()
                },
                { dismiss() })
        }
    }

    private fun syncInitiateFirstTime(){
        addedWorkList = arrayListOf<String>()
        postCopy = VegaNicaraguaGrnPost()
        startSync(

            grnList.filter {
                it.status.equals(Status.SYNC_PENDING)
            }.filter {
                isdatefunc(it.postDate.toString())
            },
            0
        )
    }


    private fun checkQuickPin() {
        val isDevicePin = PreferenceHelper.get(Constants.IS_DEVICE_PIN, false)
        val createdNewPin = PreferenceHelper.get(Constants.QUICK_PIN, "")
        if (isDevicePin) {
            authenticateApp()
        } else {
            if (createdNewPin.isEmpty()) {
                startSync(grnList.filter { it.status.equals(Status.SYNC_PENDING) }, 0)
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
                startSync(grnList.filter { it.status.equals(Status.SYNC_PENDING) }, 0)
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
                startSync(grnList.filter { it.status.equals(Status.SYNC_PENDING) }, 0)
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
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.deleteAllItem(tmpWbId)
                },
                { dismiss() })
        }
    }

    private fun movePriceCalculationSummary(vegaReceiving: VegaReceiving) {
        priceEdit = false
        selectedReceiving = vegaReceiving
        vm.getQuality(vegaReceiving.tmpWbId)
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
    private fun startSync(grnList1: List<VegaReceiving>, _index: Int) {
       // if(getNetworkBandwidth(requireContext())>0) {
            PreferenceHelper.save(Constants.START_SYNC, true)
            if (addedWorkList.isEmpty() || !addedWorkList.contains(grnList1[_index].tmpWbId)) {
                if (_index == 0) {
                    binding.clProgress.visible()
                    binding.clProgress.isClickable = true
                    binding.clProgress.isFocusable = true
                    binding.tvSync.isEnabled = false
                    binding.textViewCount.text = grnList1.size.toString()
                    binding.progressBar.max = grnList1.size
                    binding.progressBar.progress = grnList1.size
                    binding.clProgress.setBackgroundColor(Color.parseColor("#60000000"))
                    //binding.clCardProgress.setBackgroundColor(Color.parseColor("#70000000"))
                }
                if (grnList1.size > 0) {
                    try {
                        val _element = grnList1[_index]
                        //grnList1.forEachIndexed { _index, _element ->
                        val input = workDataOf(UIUtils.GRN_DATA to _element.tmpWbId)
                        val worker = getGrnOneTimeRequestWorker(input, _index)
                        enQueueUniqueWorker(worker, _element.tmpWbId, requireContext())
                        addedWorkList.add(_element.tmpWbId)
                        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
                            .observe(viewLifecycleOwner, Observer { workInfo ->
                                if (workInfo != null) {
                                    when (workInfo.state) {
                                        WorkInfo.State.SUCCEEDED -> {
                                            // val position = posExtension(workInfo.tags) + 1
                                            syncCount++
                                            binding.textViewCount.text =
                                                grnList1.size.minus(syncCount).toString()
                                            binding.progressBar.progress = grnList1.size.minus(syncCount)
                                            if (syncCount != grnList1.size) startSync(grnList1, syncCount)
                                            if (syncCount == grnList1.size) {
                                                syncCount = 0
                                                binding.clProgress.gone()
                                                binding.tvSync.isEnabled = true
                                                PreferenceHelper.save(Constants.START_SYNC, false)
                                            }
//                                    hideLoading()
                                        }

                                        WorkInfo.State.FAILED -> {
                                            // val position = posExtension(workInfo.tags) + 1
                                            syncCount++
                                            binding.textViewCount.text =
                                                grnList1.size.minus(syncCount).toString()
                                            binding.progressBar.progress = grnList1.size.minus(syncCount)
                                            if (syncCount != grnList1.size) {
                                                val msg = workInfo.outputData.getString(GRN_OUTPUT_DATA)
                                                if(msg?.contains("Unable to resolve host") == true) {
                                                    showErrorDialogWithFAQLink(requireContext(), msg)
                                                    syncCount = 0
                                                    binding.clProgress.gone()
                                                    binding.tvSync.isEnabled = true
                                                    PreferenceHelper.save(Constants.START_SYNC, false)
                                                }else {
                                                    startSync(grnList1, syncCount)
                                                }
                                            }
                                            if (syncCount == grnList1.size) {
                                                syncCount = 0
                                                binding.clProgress.gone()
                                                binding.tvSync.isEnabled = true
                                                PreferenceHelper.save(Constants.START_SYNC, false)
                                            }
//                                    hideLoading()
                                        }

                                        WorkInfo.State.RUNNING -> {/*showLoading()*/
                                        }

                                        else -> {}
                                    }
                                }
                            })
                        if (_index == grnList1.size - 1) {
                            val currentBatch = PreferenceHelper.get(Constants.LOT_SEQUENCE, "")
                            val isInvoiceFlag = grnList1.any {
                                it.grnType?.contains(
                                    "fixed",
                                    true
                                ) == true || it.grnType?.equals("spot", true) == true
                            }
                            vm.postUpdateLotSequence(currentBatch, isInvoiceFlag)
                        }
                    } catch (e: java.lang.IndexOutOfBoundsException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    PreferenceHelper.save(Constants.START_SYNC, false)
                    binding.clProgress.gone()
                    binding.tvSync.isEnabled = true
                }
            } else {
                syncCount = 0
                binding.clProgress.gone()
                binding.tvSync.isEnabled = true
                PreferenceHelper.save(Constants.START_SYNC, false)
            }
        /*}else{
            showErrorDialog("Network bandwidth is insufficient. Please check your connection and try again.")
        }*/
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.DAYS_LIMIT.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> {
                            if (!it.value!!.isNullOrEmpty())
                                count = it.value!!.toInt()
                            else
                                count = 0
                        }
                        it.applicable?.contains("N")!! -> {
                            count = 0
                        }
                    }
                }
            }
        }
    }
}

class OverlapDecoration : RecyclerView.ItemDecoration() {
    var vertOverlap = -3
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(vertOverlap, 0, 0, 0)
    }
}


