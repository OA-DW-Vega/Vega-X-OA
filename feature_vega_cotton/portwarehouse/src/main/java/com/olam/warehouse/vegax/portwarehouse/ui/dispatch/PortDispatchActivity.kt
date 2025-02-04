package com.olam.warehouse.vegax.portwarehouse.ui.dispatch

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.Container
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.ContainerWithBales
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.DispatchOT
import com.olam.warehouse.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.portwarehouse.utils.enums.ContainerStatus
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.extensions.removeSpecialCharacters
import com.olam.warehouse.presentation.extensions.valueOrDefault
import com.olam.warehouse.presentation.utils.Constants.SCANNED_ID
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.ActivityDispatchBinding
import com.olam.warehouse.vegax.portwarehouse.ui.dispatch.directdispatch.DispatchDirectActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/1/2021.
 */

class PortDispatchActivity : HomeBaseActivity(), PortDispatchContainerAdapter.OnContainerRemoveListener {

    private val vm: PortDispatchViewModel by viewModel { emptyParametersHolder() }
    private var isPaused = false
    private var isFromScan = false
    private var isOneTime = false
    private lateinit var mAdapter: PortDispatchContainerAdapter
    private lateinit var dialog: MaterialDialog
    private var isDispatchTypeAdded = true

    //private var isDirectDispatch = false
    private var mContainer = Container()
    private var mContainersList = arrayListOf<Container?>()

    override val layoutResourceId = R.layout.activity_dispatch
    private lateinit var binding: ActivityDispatchBinding
    private var mAdd = false
    var modeselected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDispatchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/dispatch/PortDispatchActivity")
            .title("Portwarehouse").with(tracker)
        when (savedInstanceState) {
            null -> fetchOTList()
            else -> {
                updateUI()
                updateSpinnerUI()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isPaused && !isFromScan) {
            updateSpinnerUI()
            //getOTDetails()
            isPaused = false
        }
    }

    override fun onPause() {
        super.onPause()
        isPaused = true
        isOneTime = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == PortWHUtil.ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                isFromScan = false
                validateScannedData(data)
            }
            requestCode == PortWHUtil.ACTIVITY_SCAN_REQUEST_CODE -> {
                modeselected = true
            }
        }
    }

    override fun onBackPressed() {
        //if (vm.mContainer == null) {
        showConfirmDialog()
        //}
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                android.R.id.home -> {
                    showConfirmDialog()
                }
            }
        }
        return false
    }

    private fun initUI() {
        binding.spOt.setSelection(vm.lastSelectedPosition)
        binding.spOt.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (position > 0) {
                    modeselected = position == vm.lastSelectedPosition
                    getOTDetails()
                }
            }
        }

        binding.tvDirect.setOnClickListener { navigateToDirectDispatch() }

        vm.isDirectDispatch.observe(this, Observer {
            when (it) {
                true -> binding.tvDirect.visibility = View.VISIBLE
                false -> binding.tvDirect.visibility = View.GONE
            }
        })
        binding.rvContainer.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mAdapter = PortDispatchContainerAdapter(mListener = this)
        binding.rvContainer.adapter = mAdapter
        binding.btnScan.setOnClickListener { moveToScanPage() }
        binding.etEnterContainer.onChange { setAddButtonColor(vm.validateContainerId(it)) }
        binding.etEnterContainer.filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(11))
        binding.btnAdd.setOnClickListener { validateAndAddContainer(binding.etEnterContainer.text.toString()) }
        binding.btnProceed.setOnClickListener { showConfirmPageDialog() }
        binding.btnOTSplit.setOnClickListener{selectOTNumber(vm.mOTs)}
        btnEnableOrDisable(false)
        btnOTSplitEnableOrDisable(false)

        vm.otDetails.observe(this, Observer {
            updateOTDetails(it)
        })
        vm.otDetailList.observe(this, Observer { updateOTList(it) })
        vm.containerDetails.observe(this, Observer { updateContainerDetails(it) })
        vm.deleteContainer.observe(this, Observer {
            updateDeleteContainerUI(it)
        })
        vm.holdStuffing.observe(this, Observer { updateHoldStuffingUI(it, mContainer) })
        vm.updateDispatchMode.observe(this, Observer {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    UIUtils.showPendingAlertDialog(
                        this, it.data?.data?.message.toString()
                    )
                    if (vm.isDirectDispatch.value == true) {
                        navigateToDirectDispatch()
                        binding.tvDirect.visibility = View.VISIBLE
                    }
                    isDispatchTypeAdded = true
                    mAdapter.setDispatchType(vm.isDirectDispatch.value ?: false)
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    toast(it.error.toString())
                }
            }
        })

        vm.updateOTNumber.observe(this, Observer {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    var newOTNumber = it.data?.data?.otNumber
                    it.data?.data?.cntrList?.forEach {
                        newOTNumber?.let { otNumber -> updateOTSplitDetails(otNumber, it) }
                    }
                    mContainersList.clear()
                    showSuccessDialog(getString(R.string.split_success))
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(
                        this, it.error.toString()
                    )
                }
            }
        })
    }

    private fun updateSpinnerUI() {
        binding.spOt.adapter =
            ArrayAdapter(
                this,
                com.olam.warehouse.presentation.R.layout.simple_spinner_item,
                vm.mOTs
            )
        binding.spOt.setSelection(vm.lastSelectedPosition)
    }

    private fun updateUI() {
        vm.mSelectedOT?.let {
            binding.tvContainerType.text =
                it.containerType?.valueOrDefault(getString(R.string.not_available))
            binding.tvGrade.text = (it.grade)?.replace(",", ", ")
                ?.valueOrDefault(getString(R.string.not_available))
            binding.tvNoOfContainer.text = it.numberOfContainer.toString()
            binding.tvWeight.text = it.weight?.format().plus(" ").plus(it.unitOfMeasure)
            vm.isDirectDispatch.value = it.direct
            mAdapter.setDispatchType(it.direct)
            /*  if (!it.direct && !it.normal) {
                  showDispatchTypeDialog(it.otNumber)
                  isDispatchTypeAdded = false
              }*/
            if (!modeselected) {
                mContainersList.clear()
                if (it.otOverallNetWeight == 0.0) {
                    showDispatchTypeDialog(it.otNumber)
                    isDispatchTypeAdded = false
                }

            }
            if (it.direct) binding.tvDirect.visibility = View.VISIBLE
            else binding.tvDirect.visibility = View.GONE
            val isAnyContainerInCompleteState = runBlocking {
                withContext(Dispatchers.IO) {
                    vm.isAnyContainerInCompleteState(it.otNumber)
                }
            }
            /*val isAllContainerInCompleteState = runBlocking {
                withContext(Dispatchers.IO) {
                    vm.isAllContainerInCompleteState(it.otNumber)
                }
            }*/
            btnEnableOrDisable(isAnyContainerInCompleteState)
        }
        vm.mOTWithContainers?.containers?.let {
            mAdapter.clearData()
            if (it.isEmpty()) {
                binding.llContainerList.gone()
            } else {
                //binding.containerCount.text=getString(R.string.container_summary) + " - " +it.size
                binding.llContainerList.visible()
                mAdapter.addItems(it)
            }
        }
        showHideBaleWeightAndCount()
    }

    /*Fetch OT details and Populate UI*/
    private fun getOTDetails() {
        btnOTSplitEnableOrDisable(false)
        binding.containercount.invisible()
        vm.setOtId(binding.spOt.selectedItem.toString())
        val position = binding.spOt.selectedItemPosition
        if (position == 0) return
        vm.lastSelectedPosition = position
        vm.clearLastOT()
        binding.llContainerList.gone()
        vm.getOTDetails()

    }

    private fun updateOTDetails(response: Resource<GenericReqAndResp<DispatchOT>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    bindOTDetails(it.data?.data)
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    toast(it.error.toString())
                }
            }
        }
    }

    private fun bindOTDetails(data: DispatchOT?) {
        data?.let {
            runBlocking {
                withContext(Dispatchers.IO) {
                    vm.updateOT(it)
                }
            }

            runBlocking {
                withContext(Dispatchers.IO) {
                    vm.setSelectedOT(vm.getOTById(binding.spOt.selectedItem.toString()))
                }
            }
            updateUI()
        }
    }
    /*OT detail update end*/

    /*Fetch OT list and Populate UI*/
    private fun fetchOTList() {
        vm.getOtList()
    }

    private fun updateOTList(response: Resource<GenericReqAndResp<List<DispatchOT>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    bindOTList(it.data?.data)
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    toast(it.error.toString())
                }
            }
        }
    }

    private fun bindOTList(it: List<DispatchOT>?) {
        it?.let { it1 ->
            runBlocking {
                withContext(Dispatchers.IO) {
                    vm.insertOrReplaceOTs(it1)
                }
            }
        }
        val otItems = runBlocking {
            withContext(Dispatchers.IO) {
                vm.getOTsLocal()
            }
        }
        vm.setOTs(otItems)
        updateSpinnerUI()
    }
    /*OT list update End*/

    /*Container details update*/
    private fun fetchContainerDetail(id: String) {
        isOneTime = false
        vm.setContainerId(id)
        vm.mSelectedOT?.let {
            vm.getContainerDetails(vm.mSelectedOT?.otNumber!!)
        }
    }

    private fun updateContainerDetails(response: Resource<GenericReqAndResp<Container>>) {
        response.let {
            if (!isOneTime) {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        hideLoading()

                        it.data.let { container ->
                            runBlocking {
                                withContext(Dispatchers.IO) {
                                    vm.deleteAllBalesByOtId(it.data?.data?.otNumber!!)
                                    vm.insertOrReplaceContainer(it.data?.data!!)
                                    it.data?.data?.bales?.let {
                                        it.forEach { bale ->
                                            bale.containerNumber = container?.data?.containerNumber!!
                                            bale.otNumber = container.data.otNumber
                                        }
                                        vm.insertOrReplaceBales(it)
                                    }
                                }
                            }


                            binding.etEnterContainer.text.clear()
                            val containerWithBales = ContainerWithBales()
                            val balesData = runBlocking {
                                withContext(Dispatchers.IO) {
                                    vm.loadContainerAndBales(
                                        vm.conId.value!!,
                                        vm.mSelectedOT!!.otNumber
                                    )
                                }
                            }
                            containerWithBales.bales = balesData.bales
                            containerWithBales.container = it.data?.data ?: Container()
                            setContainerAdapter(containerWithBales)
                            when {
                                it.data?.data?.containerNumber?.equals(vm.conId.value)!! -> moveToAddBale(
                                    it.data?.data,
                                    true
                                )
                                //else -> {}
                            }
                        }
                    }
                    Resource.Status.LOADING -> {
                        showLoading()
                    }
                    Resource.Status.ERROR -> {
                        hideLoading()
                        toast(it.error.toString())
                    }
                }
            }
        }
    }

    private fun setContainerAdapter(data: ContainerWithBales?) {
        //binding.containerCount.text=getString(R.string.container_summary) + " - " +it.size
        binding.llContainerList.visible()
        mAdapter.addItem(data)

        showHideBaleWeightAndCount()
    }

    private fun showHideBaleWeightAndCount() {

        if (mAdapter.itemCount > 0) {
            binding.llAddedWeight.visible()
            binding.llNoOfBales.visible()

            binding.tvAddedWeight.text = mAdapter.getBaleWeight()
            binding.tvNoOfBales.text = mAdapter.getTotalBaleCount()
        } else {
            binding.llAddedWeight.gone()
            binding.llNoOfBales.gone()
        }
    }

    private fun setAddButtonColor(flag: Boolean) {
        val color = if (flag) getColorId(R.color.green) else getColorId(R.color.grey)
        binding.btnAdd.setBackgroundColor(color)
    }

    private fun validateAndAddContainer(id: String) {
        if (!isDispatchTypeAdded) {
            UIUtils.showErrorDialog(
                this, getString(R.string.dispatch_type)
            )
            return
        }

        if (binding.spOt.selectedItemPosition <= 0) {
            UIUtils.showErrorDialog(
                this, getString(R.string.select_valid_ot)
            )
            return
        }
        val isValid = vm.validateContainerId(id)
        when {
            isValid && isContainerAlreadyExist(id) -> UIUtils.showErrorDialog(
                this, getString(R.string.conatiner_already_exist)
            )
            isValid -> fetchContainerDetail(id)
            else -> setErrorMessage()
        }

    }

    private fun setErrorMessage() {
        runOnUiThread {
            binding.etEnterContainer.error = getString(R.string.error_incorrect_container_id)
            binding.etEnterContainer.requestFocus()
        }
    }

    private fun moveToAddBale(data: Container?, isNewContainer: Boolean) {
        data?.let {
            val intent = Intent(this, PortDispatchAddBaleActivity::class.java)
            intent.putExtra(PortWHUtil.NEW_CONTAINER, isNewContainer)
            intent.putExtra(PortWHUtil.CONTAINER_ID, it.containerNumber)
            intent.putExtra(PortWHUtil.OT_NUMBER, it.otNumber)
            intent.putExtra(PortWHUtil.DISPATCH_TYPE, vm.isDirectDispatch.value)
            startActivity(intent)
        }
    }

    private fun validateScannedData(data: Intent?) {
        data?.let { intent ->
            val id: String? =
                intent.getStringExtra(SCANNED_ID).trim().removeSpecialCharacters().toUpperCase()

            id?.let {
                val trimmedId = it.trim().removeSpecialCharacters().toUpperCase()

                when {
                    isContainerAlreadyExist(trimmedId) -> UIUtils.showErrorDialog(
                        this, getString(R.string.conatiner_already_exist)
                    )
                    !trimmedId.isBlank() && vm.validateContainerId(trimmedId) -> fetchContainerDetail(
                        trimmedId
                    )
                    else -> UIUtils.showErrorDialog(
                        this, getString(R.string.error_incorrect_container_id)
                    )
                }
            }

        }
    }

    private fun navigateToDirectDispatch() {
        /*startActivity(
            Intent(
                this,
                DispatchDirectActivity::class.java
            )
        )*/
        startActivityForResult(
            Intent(this, DispatchDirectActivity::class.java), PortWHUtil.ACTIVITY_SCAN_REQUEST_CODE
        )
    }

    private fun moveToScanPage() {
        isFromScan = true
        when {
            binding.spOt.selectedItemPosition > 0 -> {
                startActivityForResult(
                    Intent(this, ScannerActivity::class.java), PortWHUtil.ACTIVITY_REQUEST_CODE
                )
            }
            else -> UIUtils.showErrorDialog(
                this, getString(R.string.select_valid_ot)
            )
        }
    }

    private fun isContainerAlreadyExist(id: String): Boolean {
        val otNumber = vm.mSelectedOT?.otNumber
        return mAdapter.getContainers().any { it.containerNumber == id && it.otNumber == otNumber }
    }

    override fun onDelete(container: Container) {
        showDialog(resources.getString(R.string.dialog_confirm_delete),
            object : DialogClick {
                override fun onPositive(dialog: DialogInterface) {
                    deleteContainer(container)
                }

                override fun onNegative(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            })

        /* MaterialDialog(this).show {
             positiveButton(R.string.yes) {
                 deleteContainer(container)
             }
             negativeButton(R.string.cancel) { dismiss() }
             message(R.string.dialog_confirm_delete)
         }*/
    }

    private fun deleteContainer(container: Container) {
        vm.deletedContainer = container
        vm.deleteContainerFromServer(container.containerNumber, container.otNumber)
    }

    private fun updateDeleteContainerUI(response: Resource<GenericReqAndResp<GenericMessage>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    bindDeleteContainerDetail(vm.deletedContainer.containerNumber, vm.deletedContainer.otNumber)
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    toast(it.error.toString())
                }
            }
        }
    }

    private fun bindDeleteContainerDetail(containerId: String, otNumber: String) {
        val dbData = runBlocking {
            withContext(Dispatchers.IO) {
                vm.getContainerWithBaleDetail(containerId, otNumber)
            }
        }
        mAdapter.deleteContainer(dbData)
        runBlocking {
            withContext(Dispatchers.IO) {
                vm.deleteContainer(containerId)
            }
        }

        getOTDetails()
    }

    override fun onResumeStuffing(container: Container) {

        mContainer = container
        vm.holdStuffing(
            container.containerNumber,
            container.otNumber,
            ContainerStatus.InProgress.id
        )
    }
    override fun onOTSplit(container: Container,isAdd:Boolean,isSelectedAll:Boolean) {
        mContainer = container
        mAdd=isAdd
        vm.mSelectedOT.let {
            val isAnyContainerInCompleteState = runBlocking {
                withContext(Dispatchers.IO) {
                    vm.isAnyContainerInCompleteState(it!!.otNumber)
                }
            }
            when{
                !(isAdd || mContainersList.size == 0) ->{
                    when{
                        isSelectedAll->{
                            mContainersList.forEach {
                                it?.containerNumber?.let { it1 -> updateOTSplitContainer(mAdd, it1) }
                            }
                            mContainersList.clear()
                            btnOTSplitEnableOrDisable(false)
                            binding.containercount.invisible()
                        }
                        !isSelectedAll->{
                            mContainersList.remove(mContainer)
                            updateOTSplitContainer(mAdd,mContainer.containerNumber )
                            when (mContainersList.size) {
                                0 -> {
                                    btnOTSplitEnableOrDisable(false)
                                    binding.containercount.invisible()
                                }
                            }
                        }
                    }
                    binding.containercount.text = mContainersList.size.toString()
                }
                isAdd ->{
                    when{
                        isAnyContainerInCompleteState-> {
                            if (isSplitAvailable()) {
                                updateOTSplitContainer(mAdd, mContainer.containerNumber)
                                mContainersList.add(mContainer)
                                binding.containercount.visible()
                                binding.containercount.text = mContainersList.size.toString()
                                btnOTSplitEnableOrDisable(true)
                            } else {
                                updateOTSplitContainer(false, mContainer.containerNumber)
                                mAdapter.notifyUI()
                                UIUtils.showPendingAlertDialog(
                                    this, resources.getString(R.string.split_not_available)
                                )
                                //toast("Container count exceeds the limit")
                            }
                        }else-> {
                        /*     updateOTSplitContainer(mAdd,mContainer.containerNumber )
                             mContainersList.add(mContainer)
                             btnOTSplitEnableOrDisable(true)*/
                        updateOTSplitContainer(false, mContainer.containerNumber)
                        btnOTSplitEnableOrDisable(false)
                        binding.containercount.invisible()
                        updateUIDialog(resources.getString(R.string.proceed_split_error))
                    }
                    }

                }
                else -> {
                    binding.containercount.gone()
                }
            }
         }
    }

    private fun updateHoldStuffingUI(
        response: Resource<GenericReqAndResp<GenericMessage>>,
        container: Container
    ) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when {
                        it.data?.data?.success!! -> moveToAddBale(container, false)
                        else -> UIUtils.showErrorDialog(
                            this, it.data?.data?.message.toString()
                        )
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    toast(it.error.toString())
                }
            }
        }
    }

    private fun showConfirmDialog() {

        showDialog(resources.getString(R.string.dialog_dispatch_on_back),
            object : DialogClick {
                override fun onPositive(dialog: DialogInterface) {
                    vm.lastSelectedPosition = 0
                    finish()
                }

                override fun onNegative(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            })

        /* MaterialDialog(this).show {
             positiveButton(R.string.yes) {
                 finish()
             }
             negativeButton(R.string.cancel) { dismiss() }
             message(R.string.dialog_dispatch_on_back)
         }*/
    }

    private fun showConfirmPageDialog() {

        showDialog(resources.getString(R.string.completion_msg),
            object : DialogClick {
                override fun onPositive(dialog: DialogInterface) {
                    moveToConfirmPage()
                }

                override fun onNegative(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            })

        /*  MaterialDialog(this).show {
              cancelable(false)
              title(R.string.dialogTitle)
              positiveButton(R.string.yes) { moveToConfirmPage() }
              negativeButton(R.string.cancel) { dismiss() }
              message(R.string.completion_msg)
          }*/
    }


    private fun showDispatchTypeDialog(otNum: String) {
        dialog = MaterialDialog(this)
            .customView(
                R.layout.dialog_select_dispatch,
                scrollable = false,
                noVerticalPadding = true
            ).apply {
                cancelOnTouchOutside(false)
                cancelable(false)
            }
        val dialogView = dialog.getCustomView()
        val ivClose = dialogView.findViewById<ImageView>(R.id.ivClose)
        val btnDispatchProceed = dialogView.findViewById<Button>(R.id.btnDispatchProceed)
        val rbNormal = dialogView.findViewById<RadioButton>(R.id.rbNormal)
        val rbDirect = dialogView.findViewById<RadioButton>(R.id.rbDirect)
        val rgSelectOffload = dialogView.findViewById<RadioGroup>(R.id.rgSelectDispatch)
        rbNormal.isChecked = true
        btnDispatchProceed?.setBackgroundColor(resources.getColor(R.color.green))
        btnDispatchProceed?.isEnabled = true
        rgSelectOffload?.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rbDirect || checkedId == R.id.rbNormal) {
                btnDispatchProceed?.setBackgroundColor(
                    resources.getColor(R.color.green)
                )
                btnDispatchProceed?.isEnabled = true
            }
        }

        btnDispatchProceed?.setOnClickListener {
            var isDirect = false
            when {
                rbDirect.isChecked -> {
                    vm.isDirectDispatch.value = true
                    isDirect = true

                }
                rbNormal.isChecked -> {
                    vm.isDirectDispatch.value = false
                    isDirect = false
                }
            }
            //dialog.dismiss()
            showDispatchConfirmDialog(isDirect, otNum)
        }

        ivClose?.setOnClickListener {
            vm.lastSelectedPosition = 0
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateDispatchType(otNum: String, isDirect: Boolean) {
        vm.updateDispatchMode(otNum, isDirect)

    }

    private fun moveToConfirmPage() {
        vm.mSelectedOT.let {
            val isAnyContainerInCompleteState = runBlocking {
                withContext(Dispatchers.IO) {
                    vm.isAnyContainerInCompleteState(it!!.otNumber)
                }
            }
            val isAllContainerInCompleteState = runBlocking {
                withContext(Dispatchers.IO) {
                    vm.isAllContainerInCompleteState(it!!.otNumber)
                }
            }
            when {
                it == null || it.numberOfContainer <= 0 -> UIUtils.showErrorDialog(
                    this, getString(R.string.proceed_to_confirm_error)
                )
                isAllContainerInCompleteState-> {
                    val intent = Intent(this, PortDispatchCompleteActivity::class.java)
                    intent.putExtra(PortWHUtil.OT_NUMBER, it.otNumber)
                    startActivity(intent)
                    /*when {
                        it.otOverallNetWeight >= it.thresholdOTMinWeight -> {

                        }
                        else -> UIUtils.showErrorDialog(
                            this, getString(R.string.not_reach_weight)
                        )
                    }*/
                }
                isAnyContainerInCompleteState -> UIUtils.showErrorDialog(
                    this, getString(R.string.proceed_confirm_split_error)
                )

                else -> UIUtils.showErrorDialog(
                    this, getString(R.string.proceed_confirm_error)
                )
            }
        }
    }
    /*Container details update end*/

    private fun btnEnableOrDisable(enable: Boolean) {
        binding.btnProceed.apply {
            setBackgroundColor(
                if (enable)
                    ContextCompat.getColor(context, R.color.green) else ContextCompat.getColor(
                    context,
                    R.color.light_grey
                )
            )
            isClickable=enable
        }
    }
    private fun btnOTSplitEnableOrDisable(enable: Boolean) {
        binding.btnOTSplit.apply {
            setBackgroundColor(
                    if (enable)
                        ContextCompat.getColor(context, R.color.blue) else ContextCompat.getColor(
                            context,
                            R.color.light_grey
                    )

            )
            isClickable=enable
        }
    }

    private fun showDispatchConfirmDialog(isDirect: Boolean, otNum: String) {

        showDialog(if (isDirect) resources.getString(R.string.confirm_direct_dispatch_message) else resources.getString(
            R.string.confirm_normal_dispatch_message
        ),
            object : DialogClick {
                override fun onPositive(dia: DialogInterface) {
                    updateDispatchType(otNum, isDirect)
                    dialog.dismiss()
                }

                override fun onNegative(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            })
    }

    private fun selectOTNumber(mOTs: List<String>) {
        val mOTSplitList = arrayListOf<String>()
        vm.mSelectedOT?.splitOt?.let { mOTSplitList.add(it) }
        MaterialDialog(this).show {
            title(text = getString(R.string.select_ot_number))
            listItemsSingleChoice(items = mOTSplitList) { dialog, index, text ->
                postContainer(mOTSplitList[index], binding.spOt.selectedItem.toString())

            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok))) {
                dismiss()
            }
        }
    }

    private fun postContainer(otNum: String, oldOTNumber: String) {
        val containerList = arrayListOf<String>()
        mContainersList.forEach {
            it?.containerNumber?.let { it1 -> containerList.add(it1) }
        }
        showLoading()
        vm.updateSplitOTNumberDispatch(otNum, oldOTNumber, containerList)
        /*if (it.data?.data!!.otOverallNetWeight==0.0) {
            showDispatchTypeDialog(it.data?.data!!.otNumber)
            isDispatchTypeAdded = false
        }*/
    }

    private fun updateOTSplitDetails(otNum: String, containerNumber: String){
        runBlocking {
            withContext(Dispatchers.IO) {
                vm.updateSplitContainer(false, containerNumber)
                vm.updateOTNumberToLocalDB(otNum,containerNumber )

            }
        }
        mAdapter.deleteSplitContainer(containerNumber)
    }
    private fun updateOTSplitContainer(add: Boolean, containerNumber: String){
        runBlocking {
            withContext(Dispatchers.IO) {
                vm.updateSplitContainer(add, containerNumber)

            }
        }

    }

    private fun showSuccessDialog(message: String) {
        showSingleDialog(message, object : DialogSingleClick {
            override fun onClick(dialog: DialogInterface) {
                dialog.dismiss()
                getOTDetails()
            }

        })
    }

    private fun updateUIDialog(message: String) {
        showSingleDialog(message, object : DialogSingleClick {
            override fun onClick(dialog: DialogInterface) {
                dialog.dismiss()
                mAdapter.notifyUI()
            }

        })
    }

    private fun isSplitAvailable(): Boolean {
        val mSplitOTNumber = vm.mSelectedOT?.splitOt
        return when {
            mSplitOTNumber?.compareTo("") == 0 -> false
            else -> true
        }

    }
}
