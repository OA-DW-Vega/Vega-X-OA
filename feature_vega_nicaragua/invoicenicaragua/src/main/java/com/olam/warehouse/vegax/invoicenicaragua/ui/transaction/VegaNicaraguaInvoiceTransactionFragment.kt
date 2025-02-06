package com.olam.warehouse.vegax.invoicenicaragua.ui.transaction

import android.app.Activity
import android.app.DatePickerDialog
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.quickpinaccess.createpin.VegaCreatePinActivity
import com.olam.warehouse.master.common.model.SyncStatusProgress
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaInvoiceDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnecuador.utils.FRAG_SUMMARY
import com.olam.warehouse.vegax.grnecuador.utils.INVOICE_SELECT_GRN_FRAG
import com.olam.warehouse.vegax.grnecuador.utils.PrepareInvoiceDataReverse
import com.olam.warehouse.vegax.grnecuador.utils.isdatefunc
import com.olam.warehouse.vegax.invoicenicaragua.R
import com.olam.warehouse.vegax.invoicenicaragua.databinding.FragmentVegaNicaraguaInvoiceTransactionBinding
import com.olam.warehouse.vegax.invoicenicaragua.databinding.ItemVegaNicaraguaInvoiceTransactionBinding
import com.olam.warehouse.vegax.invoicenicaragua.ui.VegaNicaraguaInvoiceViewModel
import com.olam.warehouse.vegax.invoicenicaragua.work.getGrnInvoiceSequnceOneTimeRequestWorker
import com.olam.warehouse.vegax.invoicenicaragua.work.getInvoiceOneTimeRequestWorker
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Baskaran Kannan on 9/28/2020.
 */
class VegaNicaraguaTransactionFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_nicaragua_invoice_transaction
    private lateinit var binding: FragmentVegaNicaraguaInvoiceTransactionBinding
    private val vm: VegaNicaraguaInvoiceViewModel by viewModel()
    private var invoiceList = arrayListOf<VegaNicaraguaInvoiceDetails>()
    private var grnSearchList = arrayListOf<VegaNicaraguaInvoiceDetails>()
    private var invoiceSortList = listOf<VegaNicaraguaInvoiceDetails>()
    private var selectedInvoice = VegaNicaraguaInvoiceDetails()
    private var callBack: CallBack? = null
    private var priceEdit = false
    private val REQUEST_CODE = 220
    private val LOCK_REQUEST_CODE = 221
    private val SECURITY_SETTING_REQUEST_CODE = 233

    companion object {
        fun newInstance() = VegaNicaraguaTransactionFragment().putArgs {
        }
    }

    interface CallBack {
        /*  fun replaceFragment(
              moveFrag: String, receivingData: Any, qualityParameterList: ArrayList<VegaQualityParameter?>
          )*/
        fun replaceFragment(moveFrag: String, receivingData: Any)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaInvoiceTransactionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        PreferenceHelper.save(Constants.START_SYNC, false)
        binding.clProgress.gone()
        vm.invoiceOffline.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getInvoiceOfflineData()
        /* vm.quality.observe(viewLifecycleOwner, Observer {moveSummary(it) })*/

        binding.tvPending.setOnClickListener { moveToPending() }
        binding.tvHistory.setOnClickListener { moveToHistory() }
        /*val data1 = VegaNicaraguaInvoiceDetails(tmpWbId = "Tmp1234")
        grnList.add(data1)
        setupAdapter(grnList)*/
        binding.etSearchVendor.onChange {
            if (it.isNotEmpty()) {
                grnSearchList.clear()
                invoiceList.filter { it.isSynced }.forEach { item ->
                    if (item.supplierName?.contains(it) == true) grnSearchList.add(item)
                }
                setupAdapter(grnSearchList)
            } else {
                setupAdapter(invoiceList.filter { it.isSynced })
            }
        }
        binding.tvSortByDate.setOnClickListener { sortByDate() }
        binding.tvSync.setOnClickListener { showConfirmDialog() }

    }

    private fun moveSummary(qualityItems: List<VegaQuality>) {

    }

    private fun updateUI(data: List<VegaNicaraguaInvoiceDetails>?) {
        invoiceList.clear()
        data?.let { invoiceList.addAll(it) }
        moveToPending()
    }

    private fun moveToHistory() {
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
        setupAdapter(invoiceList.filter { it.isSynced })
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
        val count = invoiceList.filter { !it.isSynced }
        binding.tvPendingCount.text = count.size.toString()
        if (count.size > 0) binding.tvPendingCount.visible() else binding.tvPendingCount.gone()
        EnableSync(count.size > 0 && AppUtils.isOnline())
        setupAdapter(invoiceList.filter { !it.isSynced })

    }

    private fun sortByDate() {
        try {
            if (invoiceList.isNotEmpty()) {
                invoiceSortList = invoiceList
                if (invoiceSortList.size > 0) {
                    val rlist = invoiceSortList.reversed()
                    invoiceList.clear()
                    invoiceList.addAll(rlist)
                    setupAdapter(invoiceList.filter { it.isSynced })
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun setupAdapter(itemList: List<VegaNicaraguaInvoiceDetails>) {
        if (itemList.size > 0) {
            binding.rvTransaction.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvTransaction.gone()
            binding.tvNoData.visible()
        }
        binding.rvTransaction.setUpAdapter(
            itemList as ArrayList,
            R.layout.item_vega_nicaragua_invoice_transaction,
            ItemVegaNicaraguaInvoiceTransactionBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvGrnTempIdValue.text = it.invoiceNo
                bindItem.tvBatchNoValue.text = it.grn
                bindItem.tvVendorValue.text = it.supplierName
                bindItem.tvMaterialValue.text = it.materialName
                bindItem.tvpostingdateval.text = it.postDate
                if (isdatefunc(it.postDate.toString()))
                    bindItem.clAlertStatus.gone()
                else
                    bindItem.clAlertStatus.visible()
                bindItem.tvErrorValue.text =
                    if (it.isSynced) getString(R.string.sync_already_triggered) else it.syncStatusMsg

//            tvErrorValue.text = it.syncStatusMsg
//            if(it.isSynced) ivScaleClose.gone() else ivScaleClose.visible()
                if (it.erdat?.isNotEmpty() == true)
                    bindItem.tvDateValue.text = it.erdat.let { it1 ->
                        it1?.let { it2 ->
                            DateUtils.getDate(it2.toLong(), "dd/MM/yyyy")
                            /*DateUtils.getUTCDateTime(
                                it2,
                                App.getAppContext()
                            )*/
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
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit).isVisible =
                        true
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit1).isVisible =
                        true
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_delete).isVisible =
                        true


                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit1).isVisible =
                        !it.isSynced
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
                                showItemDeleteDialog(it.tempId)
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
                if (it.syncStatusMsg.isNullOrEmpty()) bindItem.clErrorStatus.gone() else bindItem.clErrorStatus.visible()
                bindItem.tvReadMore.setOnClickListener { view -> showErrorDialog(it.syncStatusMsg.toString()) }
                val itemList = arrayListOf<SyncStatusProgress>()
                val data1 = SyncStatusProgress(1, getString(R.string.wbid_status), false)
                val data2 = SyncStatusProgress(2, getString(R.string.quality_status), false)
                itemList.add(data1)
                itemList.add(data2)
                //setupInnerAdpter(itemList)


                //displayFragment(VegaSynStatusProgressFragment.newInstance(itemList), false)
            }, {
                // movePriceCalculationSummary(this)
            })
    }


    private fun getDatePickerDialog(
        tvpostingdateval: AppCompatTextView,
        vegainvoiceDetail: VegaNicaraguaInvoiceDetails
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
                    vegainvoiceDetail.postDate = tvpostingdateval.text.toString()

                    vm.updatePostingDate(vegainvoiceDetail.tempId, tvpostingdateval.text.toString())
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

    private fun moveEdit(it: VegaNicaraguaInvoiceDetails) {
        callBack?.replaceFragment(INVOICE_SELECT_GRN_FRAG, PrepareInvoiceDataReverse(it))
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.sync_all)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    val isSecurityPin = PreferenceHelper.get(Constants.IS_SECURITY_PIN, false)
                    when (isSecurityPin) {
                        true -> checkQuickPin() // Quick Pin Access
                        else -> startSync(invoiceList.filter { !it.isSynced }
                            .filter { isdatefunc(it.postDate.toString()) }
                                as ArrayList<VegaNicaraguaInvoiceDetails>,
                            0)
                    }
//                startSync(invoiceList.filter { !it.isSynced } as ArrayList<VegaNicaraguaInvoiceDetails>, 0)
                },
                { dismiss() })
        }
    }

    private fun showItemDeleteDialog(tmpWbId: String) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    vm.deleteInvoiceItem(tmpWbId)
                    PreferenceHelper.save(tmpWbId, "")
                },
                { dismiss() })
        }
    }

    private fun movePriceCalculationSummary(invoice: VegaNicaraguaInvoiceDetails) {
        selectedInvoice = invoice
        callBack?.replaceFragment(FRAG_SUMMARY, invoice)
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
    private fun startSync(invoiceList1: ArrayList<VegaNicaraguaInvoiceDetails>, _index: Int) {
        PreferenceHelper.save(Constants.START_SYNC, true)

        if (_index == 0) {
            binding.clProgress.visible()
            binding.textViewCount.text = invoiceList1.size.toString()
            binding.progressBar.max = invoiceList1.size
            binding.progressBar.progress = invoiceList1.size
            binding.clProgress.setBackgroundColor(Color.parseColor("#60000000"))
            //binding.clCardProgress.setBackgroundColor(Color.parseColor("#70000000"))
        }
        if (invoiceList1.size>0)
        {
            val _element = invoiceList1[_index]
            val input = workDataOf(UIUtils.INVOICE_DATA to _element.tempId)
            val worker = getInvoiceOneTimeRequestWorker(input, _index)
            enQueueWorkerWithName(worker, _element.tempId, requireContext())
            WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
                .observe(viewLifecycleOwner, Observer { workInfo ->
                    if (workInfo != null) {
                        when (workInfo.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                val position = AppUtils.posExtension(workInfo.tags) + 1
                                syncCount++
                                binding.textViewCount.text =
                                    invoiceList1.size.minus(syncCount).toString()
                                binding.progressBar.progress = invoiceList1.size.minus(syncCount)
                                if (syncCount != invoiceList1.size) startSync(
                                    invoiceList1,
                                    syncCount
                                )
                                if (syncCount == invoiceList1.size) {
                                    syncCount = 0
                                    binding.clProgress.gone()
                                    PreferenceHelper.save(Constants.START_SYNC, false)
                                }
//                                    hideLoading()
                            }
                            WorkInfo.State.FAILED -> {
                                val position = AppUtils.posExtension(workInfo.tags) + 1
                                syncCount++
                                binding.textViewCount.text =
                                    invoiceList1.size.minus(syncCount).toString()
                                binding.progressBar.progress = invoiceList1.size.minus(syncCount)
                                if (syncCount != invoiceList1.size) startSync(
                                    invoiceList1,
                                    syncCount
                                )
                                if (syncCount == invoiceList1.size) {
                                    syncCount = 0
                                    binding.clProgress.gone()
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

            if (_index == invoiceList1.size - 1) {
                postUpdateInvoiceSequence("")
            }
        } else{
            PreferenceHelper.save(Constants.START_SYNC, false)
            binding.clProgress.gone()
        }
    }

    private fun postUpdateInvoiceSequence(batchNumber: String?) {
        val input = workDataOf(UIUtils.GRN_DATA to batchNumber)
        val worker = getGrnInvoiceSequnceOneTimeRequestWorker(input)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                        }
                        WorkInfo.State.FAILED -> {
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                        else -> {}
                    }
                }

            })
    }

    //###################Security Pin Check Start######################
    private fun checkQuickPin() {
        val isDevicePin = PreferenceHelper.get(Constants.IS_DEVICE_PIN, false)
        val createdNewPin = PreferenceHelper.get(Constants.QUICK_PIN, "")
        if (isDevicePin) {
            authenticateApp()
        } else {
            if (createdNewPin.isEmpty()) {
                startSync(invoiceList.filter { !it.isSynced } as ArrayList<VegaNicaraguaInvoiceDetails>,
                    0)
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
        val keyguardManager =
            activity?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

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
                startSync(invoiceList.filter { !it.isSynced } as ArrayList<VegaNicaraguaInvoiceDetails>,
                    0)
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
                startSync(invoiceList.filter { !it.isSynced } as ArrayList<VegaNicaraguaInvoiceDetails>,
                    0)
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
        val keyguardManager =
            activity?.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        //this method only work whose api level is greater than or equal to Jelly_Bean (16)
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && keyguardManager.isKeyguardSecure
        //You can also use keyguardManager.isDeviceSecure(); but it requires API Level 23
    }

    //###################Security Pin Check End ######################
}

