package com.olam.warehouse.vegax.forwardponicaragua.ui.transaction

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
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.quickpinaccess.createpin.VegaCreatePinActivity
import com.olam.warehouse.master.common.model.SyncStatusProgress
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPODetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPOPriceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnPriceDetails
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.forwardponicaragua.R
import com.olam.warehouse.vegax.forwardponicaragua.databinding.FragamentVegaNicaraguaForwardPoTransactionBinding
import com.olam.warehouse.vegax.forwardponicaragua.ui.Callback
import com.olam.warehouse.vegax.forwardponicaragua.ui.VegaNicaraguaForwardPOViewModel
import com.olam.warehouse.vegax.forwardponicaragua.work.getForwardPoOneTimeRequestWorker
import com.olam.warehouse.vegax.grnecuador.utils.FORWARD_PO_CREATION_PREVIEW_FRAG
import com.olam.warehouse.vegax.grnecuador.utils.FORWARD_PO_CREATION_TRANSACTION_DETAILS_FRAG
import kotlinx.android.synthetic.main.item_vega_nicaragua_forward_po_transaction.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaNicaraguaForwardPoTransactionFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragament_vega_nicaragua_forward_po_transaction
    private lateinit var binding: FragamentVegaNicaraguaForwardPoTransactionBinding
    private val vm: VegaNicaraguaForwardPOViewModel by viewModel()
    private var forWardPOList = mutableListOf<VegaNicaraguaForwardPODetails>()
    private var forWardPOSearchList = arrayListOf<VegaNicaraguaForwardPODetails>()
    private var forWardPOSortList = mutableListOf<VegaNicaraguaForwardPODetails>()
    private var selectedReceiving = VegaNicaraguaForwardPODetails()
    private var callBack: Callback? = null
    private var priceEdit = false
    private val REQUEST_CODE = 220
    private val LOCK_REQUEST_CODE = 221
    private val SECURITY_SETTING_REQUEST_CODE = 233
    private var priceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()
    private var FilteredPriceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()
    var count:Int=0

    companion object {
        fun newInstance() = VegaNicaraguaForwardPoTransactionFragment().putArgs {
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? Callback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragamentVegaNicaraguaForwardPoTransactionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding.clProgress.gone()
        vm.getConfigItems(UserRoles.DAYS_LIMIT.role)
        vm.forwardPOTransList.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateUI(it) })
        vm.forwardPOPriceTransList.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            priceDetails = ArrayList(prepareForwardPoPriceDetails(ArrayList(it.filter { it.differential!!.isNotEmpty() })))
            FilteredPriceDetails=ArrayList(prepareForwardPoPriceDetails(ArrayList(it.filter { it.differential!!.isEmpty() })))
            var data = Bundle()
            data.putParcelable(UIUtils.RECEIVING_DATA, selectedReceiving)
            data.putParcelableArrayList(UIUtils.YIELD_DATA, priceDetails)
            data.putParcelableArrayList(UIUtils.FILTEREDPRICEDETAILS,FilteredPriceDetails )
            data.putBoolean(UIUtils.FROM_TRANSACTION, true)
            callBack?.replaceFragment(
                FORWARD_PO_CREATION_PREVIEW_FRAG, data
            )
        })
        vm.configItems.observe(this, androidx.lifecycle.Observer {
            updateConfigItems(it)
        })
        vm.getForwardPODetails()
        // vm.getForwardPOPriceDetails()

        binding.tvPending.setOnClickListener { moveToPending() }
        binding.tvHistory.setOnClickListener { moveToHistory() }
        /*val data1 = VegaReceiving(tmpWbId = "Tmp1234")
        grnList.add(data1)
        setupAdapter(grnList)*/
        binding.tvSync.setOnClickListener {
            if (DateUtils.isFirstDayOfMonth(context!!,count)) {
                if (forWardPOList.filter { it.syncStatus == false }.toMutableList().size > 0) showConfirmDialog()
                else activity?.toast(getString(R.string.no_data_found))
            } else {
                showSnack(getString(R.string.month_close_error))
            }

        }
        binding.etSearchVendor.onChange {
            if (it.isNotEmpty()) {
                forWardPOSearchList.clear()
                forWardPOList.filter { it.syncStatus == true }.forEach { item ->
                    if (item.vendorName?.contains(it, true) == true || item.vendorCode?.contains(
                            it,
                            true
                        ) == true
                    ) forWardPOSearchList.add(item)
                }
                setupAdapter(forWardPOSearchList)
            } else {
                setupAdapter(forWardPOList.filter { it.syncStatus == true }.toMutableList())
            }
        }

        binding.tvSortByDate.setOnClickListener { sortByDate() }
        EnableSync(AppUtils.isOnline())
    }

    private fun prepareForwardPoPriceDetails(forwardPoPriceDetails: ArrayList<VegaNicaraguaForwardPOPriceDetails>): java.util.ArrayList<VegaNicaraguaGrnPriceDetails> {
        val priceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()

        forwardPoPriceDetails.forEach {
            val lineItem = VegaNicaraguaGrnPriceDetails()
            lineItem.fieldName = it.fieldName
            lineItem.companyCode = it.companyCode
            lineItem.division = it.division
            lineItem.plant = it.plant
            lineItem.purchasingOrg = it.purchasingOrg
            lineItem.purchasingGroup = it.purchasingGroup
            lineItem.description = it.description
            lineItem.priceDate = it.priceDate
            lineItem.price = it.price
            lineItem.differential = it.differential
            lineItem.currency = it.currency
            lineItem.baseUnit = it.baseUnit
            lineItem.createdOn = it.createdOn
            lineItem.percentage = it.percentage
            priceDetails.add(lineItem)
        }
        return priceDetails
    }

    private fun updateUI(data: List<VegaNicaraguaForwardPODetails>?) {
        forWardPOList.clear()
        data?.let { forWardPOList.addAll(it) }
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
        setupAdapter(forWardPOList.filter { it.syncStatus == true }.toMutableList())
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
        val count = forWardPOList.filter { it.syncStatus == false }.toMutableList()
        binding.tvPendingCount.text = count.size.toString()
        if (count.size > 0) binding.tvPendingCount.visible() else binding.tvPendingCount.gone()
        EnableSync(count.size > 0 && AppUtils.isOnline())
        setupAdapter(forWardPOList.filter { it.syncStatus == false }.toMutableList())
    }

    private fun sortByDate() {
        if (forWardPOList.filter { it.syncStatus == true }.isNotEmpty()) {
            forWardPOSortList = forWardPOList.filter { it.syncStatus == true }.toMutableList()
            if (forWardPOSortList.size > 0)
                setupAdapter(forWardPOSortList.asReversed())
        }
    }


    private fun setupAdapter(itemList: MutableList<VegaNicaraguaForwardPODetails>) {
        if (itemList.size > 0) {
            binding.rvTransaction.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvTransaction.gone()
            binding.tvNoData.visible()
        }
        binding.rvTransaction.setUp(itemList, R.layout.item_vega_nicaragua_forward_po_transaction, { it, pos ->
            tvPOTempIdValue.text = if (it.poNumber?.isNotEmpty() == true) it.poNumber else it.tempId
            tvVendorValue.text = it.vendorName
            tvMaterialValue.text = it.materialName
            tvGradeValue.text = it.grade + "-" + it.qualityGradeDesc
            tvErrorValue.text = it.syncStatusMsg
            tvWeightValue.text = it.netWeight + " KG(S)"
            //  if(it.syncStatus!!) ivScaleClose.gone() else ivScaleClose.visible()

            tvDateValue.text = it.docDate?.let { it1 ->
                it1.let { it2 ->
                    DateUtils.getUTCDateTimeNicaragua(
                        DateUtils.getTimeStamp(it2).toString(),
                        App.getAppContext()
                    )
                }
            }

            //ivScaleClose.setOnClickListener {view -> showItemDeleteDialog(it.tmpWbId) }
            ivEdit.setOnClickListener { view ->
                val popupMenu: PopupMenu = PopupMenu(requireActivity(), view)
                popupMenu.menuInflater.inflate(com.olam.warehouse.login.R.menu.transaction_menu, popupMenu.menu)
                popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_Sync).isVisible = false
                popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_copy).isVisible = false

                if (it.syncStatus == true) {
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit).isVisible = true
                } else {
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit).isVisible = true
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit1).isVisible = true
                    popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_delete).isVisible = true
                }
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        com.olam.warehouse.login.R.id.action_edit -> {
                            moveToSummary(it)
                        }
                        com.olam.warehouse.login.R.id.action_edit1 -> {
                            moveEdit(it)
                        }
                        com.olam.warehouse.login.R.id.action_delete -> {
                            showItemDeleteDialog(it.tempId)
                        }
                    }
                    true
                }
                popupMenu.show()
            }
            if (it.syncStatusMsg.isNullOrEmpty() || it.syncStatus!!) {
                tvReadMore.gone()
                tvError.gone()
                tvErrorValue.gone()
            } else {
                tvReadMore.visible()
                tvError.visible()
                tvErrorValue.visible()
            }
            if (it.syncStatusMsg.isNullOrEmpty() && it.syncStatus == false) clErrorStatus.gone() else clErrorStatus.visible()
            tvReadMore.setOnClickListener { view -> showErrorDialog(it.syncStatusMsg.toString()) }
            val itemList = arrayListOf<SyncStatusProgress>()

            //setupInnerAdpter(itemList)
            //displayFragment(VegaSynStatusProgressFragment.newInstance(itemList), false)
        }, {
            // movePriceCalculationSummary(this)
        })
    }

    private fun moveEdit(it: VegaNicaraguaForwardPODetails) {

        var data = Bundle()
        data.putParcelable(UIUtils.RECEIVING_DATA, it)
        callBack?.replaceFragment(FORWARD_PO_CREATION_TRANSACTION_DETAILS_FRAG, data)

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
                        else -> startSync(forWardPOList.filter { it.syncStatus == false }
                            .toMutableList(), 0)
                    }
//                startSync(forWardPOList.filter { it.syncStatus == false }.toMutableList(), 0)
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
                startSync(forWardPOList.filter { it.syncStatus == false }.toMutableList(), 0)
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
                startSync(forWardPOList.filter { it.syncStatus==false  }.toMutableList(), 0)
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
                 startSync(forWardPOList.filter { it.syncStatus==false }.toMutableList(), 0)
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
                    vm.deleteItem(tmpWbId)
                },
                { dismiss() })
        }
    }

    private fun moveToSummary(vegaReceiving: VegaNicaraguaForwardPODetails) {
        priceEdit = false
        selectedReceiving = vegaReceiving
        vm.getForwardPOPriceDetails(vegaReceiving.tempId)
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
    private fun startSync(forWardPoList1: MutableList<VegaNicaraguaForwardPODetails>, _index: Int) {
        if (_index == 0) {
            binding.clProgress.visible()
            binding.textViewCount.text = forWardPoList1.size.toString()
            binding.progressBar.max = forWardPoList1.size
            binding.progressBar.progress = forWardPoList1.size
            binding.clProgress.setBackgroundColor(Color.parseColor("#60000000"))
            //binding.clCardProgress.setBackgroundColor(Color.parseColor("#70000000"))
        }
        val _element = forWardPoList1[_index]
        //grnList1.forEachIndexed { _index, _element ->
        val input = workDataOf(UIUtils.FORWARD_PO_DATA to _element.tempId)
        val worker = getForwardPoOneTimeRequestWorker(input, _index)
        enQueueWorkerWithName(worker, _element.tempId, requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(this, androidx.lifecycle.Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val position = AppUtils.posExtension(workInfo.tags) + 1
                            syncCount++
                            binding.textViewCount.text = forWardPoList1.size.minus(syncCount).toString()
                            binding.progressBar.progress = forWardPoList1.size.minus(syncCount)
                            if (syncCount != forWardPoList1.size) startSync(forWardPoList1, syncCount)
                            if (syncCount == forWardPoList1.size) {
                                syncCount = 0
                                binding.clProgress.gone()
                            }
//                                    hideLoading()
                        }
                        WorkInfo.State.FAILED -> {
                            val position = AppUtils.posExtension(workInfo.tags) + 1
                            syncCount++
                            binding.textViewCount.text = forWardPoList1.size.minus(syncCount).toString()
                            binding.progressBar.progress = forWardPoList1.size.minus(syncCount)
                            if (syncCount != forWardPoList1.size) startSync(forWardPoList1, syncCount)
                            if (syncCount == forWardPoList1.size) {
                                syncCount = 0
                                binding.clProgress.gone()
                            }
//                                    hideLoading()
                        }
                        WorkInfo.State.RUNNING -> {/*showLoading()*/
                        }
                    }
                }
            })
        val currentBatch = PreferenceHelper.get(Constants.PO_SEQUENCE, "")
        vm.postUpdateLotSequence(currentBatch, false)

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
                            count = 0
                        }
                    }
                }
            }
        }
    }
}






