package com.olam.warehouse.vegax.portwarehouse.ui.dispatch

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.Container
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortBale
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.SCANNED_ID
import com.olam.warehouse.presentation.utils.extension.format
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.DispatchAddBaleActivityBinding
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.vegax.portwarehouse.utils.enums.ContainerStatus
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
class PortDispatchAddBaleActivity : HomeBaseActivity() {
    private var baleId: String = ""
    private var deleteBaleId: PortBale = PortBale()
    private var containerId: String = ""
    private var otNumber: String = ""
    private var newContainer = false
    private var uom: String? = ""
    private var isDirectDispatch: Boolean = false

    private val vm: PortDispatchViewModel by viewModel { emptyParametersHolder() }
    var mBaleAdapter = PortDispatchAddBaleAdapter({ showDeleteDialog(it) }, false)

    override val layoutResourceId = R.layout.dispatch_add_bale_activity
    private lateinit var binding: DispatchAddBaleActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DispatchAddBaleActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        initExtras()
        initUI()
        getContainerWithBale()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/dispatch/PortDispatchAddBaleActivity")
            .title("Portwarehouse").with(tracker)

    }

    private fun getContainerWithBale() {
        when {
            newContainer -> updateUI()
            else -> {
                vm.setContainerId(containerId)
                vm.getContainerDetailsWithBales(otNumber)
            }
        }
    }

    private fun updateDataUI(response: Resource<GenericReqAndResp<Container>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
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

                    }

                    hideLoading()
                    updateUI()
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(this, it.error.toString())
                }
            }
        }
    }


    private fun updateUI() {
        runBlocking {
            withContext(Dispatchers.IO) {
                vm.updateContainerAndBaleDetail(containerId, otNumber)
            }
        }
        vm.mContainer?.let {
            uom = it.unitOfMeasure
            binding.tvContainerNo.text = it.containerNumber
            binding.tvContainertype.text = it.containerType
            binding.tvHomeGrade.text = it.grade?.replace(",", ", ")
            binding.tvWeightContainer.text = it.weight?.format().plus(" ").plus(it.unitOfMeasure)
        }
        mBaleAdapter.addItems(vm.mBaleList)
        showHideBaleDetails()
        setVisibility()
    }

    private fun showHideBaleDetails() {
        if (mBaleAdapter.itemCount > 0) {
            binding.llAddedWeight.visibility = View.VISIBLE
            binding.llNoOfBales.visibility = View.VISIBLE
            binding.tvAddedWeight.text =
                PortWHUtil.getBaleWeightWithUOM(mBaleAdapter.getBaleWeight(), uom ?: "")
            binding.tvNoOfBales.text = mBaleAdapter.itemCount.toString()
        } else {
            binding.llAddedWeight.visibility = View.GONE
            binding.llNoOfBales.visibility = View.GONE
        }
    }

    private fun initExtras() {
        containerId = intent.getStringExtra(PortWHUtil.CONTAINER_ID) ?: ""
        otNumber = intent.getStringExtra(PortWHUtil.OT_NUMBER) ?: ""
        newContainer = intent.getBooleanExtra(PortWHUtil.NEW_CONTAINER, false)
        isDirectDispatch = intent.getBooleanExtra(PortWHUtil.DISPATCH_TYPE, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.ADD_TASK_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                baleId = data?.extras?.getString(SCANNED_ID)?.trim().toString()
                validateBaleId(
                    data?.extras?.getString(SCANNED_ID)
                        ?.trim()
                )
            }
        }
    }

    override fun onBackPressed() {
        showHoldDialog()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            when (it.itemId) {
                android.R.id.home -> {
                    showHoldDialog()
                }
            }
        }
        return false
    }

    private fun initUI() {
        binding.tvHContainerNo.text = containerId
        binding.rvBaleSummary.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rvBaleSummary.adapter = mBaleAdapter
        binding.btnScanBale.setOnClickListener { moveToScan() }
        binding.btnAddBale.setOnClickListener { moveToAddBale() }
        binding.btnhold.setOnClickListener { showHoldDialog() }
        binding.etBaleID.onChange { changeButttonColor(it) }
        binding.btnSealcontainer.setOnClickListener { moveToSealContainer() }
        vm.containerWithBalesDetails.observe(this, Observer { data ->
            updateDataUI(data)
        })
        vm.baleDetails.observe(this, Observer { updateBaleDetail(it) })
        vm.dbBaleDetails.observe(this, Observer { updateDbBaleDetail(it) })
        vm.deleteBale.observe(this, Observer { deleteBaleLocalDB(it, deleteBaleId) })
        vm.holdStuffing.observe(this, Observer { updateHoldStuffingUI(it) })
    }


    private fun moveToSealContainer() {
        when (mBaleAdapter.itemCount > 0) {
            true -> containerId.let {
                val intent = Intent(this, PortDispatchSealContainerActivity::class.java)
                intent.putExtra(PortWHUtil.CONTAINER_ID, containerId)
                intent.putExtra(PortWHUtil.OT_NUMBER, otNumber)
                intent.putExtra(PortWHUtil.DISPATCH_TYPE, isDirectDispatch)
                startActivity(intent)
            }
            else -> showErrorDialogWithFAQLink(
                this, getString(R.string.atleast_one_bale)
            )
        }

    }

    private fun moveToScan() {
        binding.etBaleID.text.clear()
        binding.etBaleID.clearFocus()
        val intent = Intent(this, ScannerActivity::class.java)
        startActivityForResult(intent, Constants.ADD_TASK_REQUEST)
    }

    private fun moveToAddBale() {
        baleId = binding.etBaleID.text?.toString()?.trim()!!
        validateBaleId(baleId)

    }

    private fun validateBaleId(baleId: String?) {
        baleId?.let {
            val isValid = vm.validateBaleId(baleId)
            when {
                isValid && isAlreadyExistBale(baleId) -> showErrorDialogWithFAQLink(
                    this, getString(R.string.bale_already_exist)
                )
                isValid -> moveToBaleDetails(baleId, isDirectDispatch)
                else -> showErrorDialogWithFAQLink(
                    this, getString(R.string.error_bale_id)
                )
            }
        }
    }

    private fun moveToBaleDetails(baleId: String, isDirect: Boolean) {
        vm.getBaleDetails(baleId, containerId, vm.mContainer?.otNumber, isDirect)

    }

    private fun updateBaleDetail(response: Resource<GenericReqAndResp<PortBale>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    /* runBlocking {
                         withContext(Dispatchers.IO) {*/
                    vm.insertOrReplaceBale(it.data?.data ?: PortBale())
                    /*}
                }*/

                    /*val updatedData = runBlocking {
                        withContext(Dispatchers.IO)
                        {*/
                    vm.getBaleDeatils(it.data?.data?.baleId ?: baleId)
                    /* }
                 }
                 updateAdapter(updatedData)*/
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(
                        this, it.error.toString()
                    )
                }
            }
        }
    }

    private fun updateDbBaleDetail(it: PortBale?) {
        updateAdapter(it)

    }

    private fun updateAdapter(data: PortBale?) {
        binding.rvBaleSummary.visible()
        binding.tvNoBaleData.gone()
        vm.setBale(data)
        when {
            !baleId.isEmpty() -> binding.etBaleID.text.clear()
        }
        mBaleAdapter.addItem(vm.mBale)
        showHideBaleDetails()
    }

    private fun deleteBaleFromContainer(bale: PortBale?) {
        bale?.let {
            vm.deleteBaleDetail(bale.baleId, bale.containerNumber)
            this.deleteBaleId = it
        }
    }

    private fun deleteBaleLocalDB(
        response: Resource<GenericReqAndResp<GenericMessage>>,
        bale: PortBale
    ) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when {
                        it.data?.data?.success!! -> updateBaleDB(bale)
                        else -> showErrorDialogWithFAQLink(
                            this, it.data?.data?.message.toString()
                        )
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(
                        this, it.error.toString()
                    )
                }
            }
        }
    }

    private fun updateBaleDB(bale: PortBale) {
        mBaleAdapter.removeItem(bale)
        runBlocking {
            withContext(Dispatchers.IO) {
                vm.deleteBaleFromDB(bale)
            }
        }
        showHideBaleDetails()
        runBlocking {
            withContext(Dispatchers.IO) {
                vm.updateContainerAndBaleDetail(containerId, otNumber)
            }
        }
        setVisibility()
    }

    private fun moveToHoldStuffing() {
        vm.holdStuffing(containerId, otNumber, ContainerStatus.OnHold.id)

    }

    private fun updateHoldStuffingUI(response: Resource<GenericReqAndResp<GenericMessage>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when {
                        it.data?.data?.success!! -> moveToContainerOT()
                        else -> showErrorDialogWithFAQLink(
                            this, it.data?.data?.message.toString()
                        )
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(
                        this, it.error.toString()
                    )
                }
            }
        }
    }

    private fun moveToContainerOT() {
        finish()
    }

    fun isAlreadyExistBale(id: String?): Boolean {
        return mBaleAdapter.getBales().any { it.baleId == id }
    }

    private fun changeButttonColor(value: String) {
        when (value.length) {
            10 -> ViewCompat.setBackgroundTintList(
                binding.btnAddBale,
                ContextCompat.getColorStateList(
                    this,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btnAddBale,
                ContextCompat.getColorStateList(
                    this,
                    com.olam.warehouse.presentation.R.color.colorSecondaryGrey
                )
            )
        }
    }

    private fun setVisibility() {
        when (vm.mBaleList.size) {
            0 -> {
                binding.rvBaleSummary.gone()
                binding.tvNoBaleData.visible()
            }
            else -> {
                binding.tvNoBaleData.gone()
                binding.rvBaleSummary.visible()
            }
        }
    }

    private fun showDeleteDialog(bale: PortBale?) {

        showDialog(
            resources.getString(R.string.dialog_confirm_bale_delete),
            object : DialogClick {
                override fun onPositive(dialog: DialogInterface) {
                    deleteBaleFromContainer(bale)
                }

                override fun onNegative(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            })

        /*MaterialDialog(this).show {
            cancelable(false)
            title(R.string.dialogTitle)
            positiveButton(R.string.yes) { deleteBaleFromContainer(bale) }
            negativeButton(R.string.cancel) { dismiss() }
            message(R.string.dialogDeleteTitle)
        }*/
    }

    private fun showHoldDialog() {

        showDialog(resources.getString(R.string.dialogHoldTitle),
            object : DialogClick {
                override fun onPositive(dialog: DialogInterface) {
                    moveToHoldStuffing()
                }

                override fun onNegative(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            })

        /*MaterialDialog(this).show {
            cancelable(false)
            title(R.string.dialogTitle)
            positiveButton(R.string.yes) { moveToHoldStuffing() }
            negativeButton(R.string.cancel) { dismiss() }
            message(R.string.dialogHoldTitle)
        }*/
    }
}
