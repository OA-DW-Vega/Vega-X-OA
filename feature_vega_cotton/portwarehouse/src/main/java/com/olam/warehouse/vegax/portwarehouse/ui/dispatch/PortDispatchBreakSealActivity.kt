package com.olam.warehouse.vegax.portwarehouse.ui.dispatch

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.Container
import com.olam.warehouse.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.DispatchBreakSealActivityBinding
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
class PortDispatchBreakSealActivity : HomeBaseActivity() {

    private var containerNumber: String? = ""
    private var sealNumber: String? = ""
    private var otNumber: String? = ""
    private var dispatchType: Boolean = false
    private val vm: PortDispatchSealViewModel by viewModel { emptyParametersHolder() }
    var mBaleAdapter = PortDispatchAddBaleAdapter({ it }, true)

    private lateinit var binding: DispatchBreakSealActivityBinding
    override val layoutResourceId = R.layout.dispatch_break_seal_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DispatchBreakSealActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        initExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/dispatch/PortDispatchBreakSealActivity")
            .title("Portwarehouse").with(tracker)
    }

    override fun onBackPressed() {
        moveToHome()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                android.R.id.home -> moveToHome()
            }
        }
        return false
    }

    private fun moveToHome() {
        startActivity(Intent(this, PortDispatchActivity::class.java))
        finish()
    }

    private fun initExtras() {
        containerNumber = intent.getStringExtra(PortWHUtil.CONTAINER_ID)
        sealNumber = intent.getStringExtra(Constants.SEAL_ID)
        otNumber = intent.getStringExtra(PortWHUtil.OT_NUMBER)
        dispatchType = intent.getBooleanExtra(PortWHUtil.DISPATCH_TYPE, false)
    }

    private fun initUI() {
        binding.rvSealBaleSummary.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rvSealBaleSummary.adapter = mBaleAdapter
        binding.tvHBreakContainerNo.text = containerNumber
        binding.btnBreakSeal.setOnClickListener { showBackConfirmDialog() }
        vm.getContainerDetailsWithBales(containerNumber.toString(), otNumber.toString())
        vm.containerWithBalesDetails.observe(this, Observer { updateDataUI(it) })
        vm.breakSeal.observe(this, Observer { updateData(it) })
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
                    UIUtils.showErrorDialog(
                        this, it.error.toString()
                    )
                }
            }
        }
    }

    private fun updateUI() {
        runBlocking {
            withContext(Dispatchers.IO)
            {
                vm.updateContainerAndBaleDetail(containerNumber.toString(), otNumber.toString())
            }
        }

        vm.mContainer?.let {
            binding.tvBreakContainerNo.text = it.containerNumber
            binding.tvSealNo.text = sealNumber
        }
        mBaleAdapter.addItems(vm.mBaleList)
        setVisibility()
    }

    private fun showBackConfirmDialog() {

        showDialog(resources.getString(R.string.dialog_confirm_breakseal),
            object : DialogClick {
                override fun onPositive(dialog: DialogInterface) {
                    breakSealContainer()
                }

                override fun onNegative(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            })

        /*MaterialDialog(this).show {
            title(R.string.dialogTitle)
            cancelable(false)
            positiveButton(R.string.yes) { breakSealContainer() }
            negativeButton(R.string.cancel) { dismiss() }
            message(R.string.dialog_confirm_breakseal)
        }*/
    }

    private fun breakSealContainer() {
        vm.doBreakSealContainer(otNumber.toString(), containerNumber.toString(), sealNumber.toString())

    }

    private fun updateData(response: Resource<GenericReqAndResp<GenericMessage>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    moveToAddBale()
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
        }
    }

    private fun moveToAddBale() {
        val intent = Intent(this, PortDispatchAddBaleActivity::class.java)
        intent.putExtra(PortWHUtil.NEW_CONTAINER, false)
        intent.putExtra(PortWHUtil.CONTAINER_ID, containerNumber)
        intent.putExtra(PortWHUtil.OT_NUMBER, otNumber)
        intent.putExtra(PortWHUtil.DISPATCH_TYPE, dispatchType)
        startActivity(intent)
        finish()
    }

    private fun setVisibility() {
        when (vm.mBaleList.size) {
            0 -> {
                binding.rvSealBaleSummary.gone()
                binding.tvSealNoBaleData.visible()
            }
            else -> {
                binding.tvSealNoBaleData.gone()
                binding.rvSealBaleSummary.visible()
            }
        }
    }
}
