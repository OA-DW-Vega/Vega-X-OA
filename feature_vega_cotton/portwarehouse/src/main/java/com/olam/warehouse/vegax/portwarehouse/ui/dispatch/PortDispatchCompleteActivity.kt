package com.olam.warehouse.vegax.portwarehouse.ui.dispatch

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.Container
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.DispatchOT
import com.olam.warehouse.portwarehouse.ui.success.SuccessPortActivity
import com.olam.warehouse.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.format
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.ActivityDispatchCompleteBinding
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
class PortDispatchCompleteActivity : HomeBaseActivity(), PortDispatchCompleteAdapter.OnViewContainerListener {

    private var otNumber: String = ""
    private val vm: PortDispatchViewModel by viewModel { emptyParametersHolder() }
    private lateinit var mAdapter: PortDispatchCompleteAdapter

    override fun onViewContainer(container: Container) {
        val intent = Intent(this, PortDispatchBreakSealActivity::class.java)
        intent.putExtra(Constants.SEAL_ID, container.sealNumber)
        intent.putExtra(PortWHUtil.CONTAINER_ID, container.containerNumber)
        intent.putExtra(PortWHUtil.OT_NUMBER, container.otNumber)
        startActivity(intent)
        finish()
    }

    override val layoutResourceId = R.layout.activity_dispatch_complete
    private lateinit var binding: ActivityDispatchCompleteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDispatchCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        initExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/dispatch/PortDispatchCompleteActivity")
            .title("Portwarehouse").with(tracker)
        when (savedInstanceState) {
            null -> getOTDetails()
            else -> updateUI()
        }
    }

    private fun initUI() {
        binding.rvSummary.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mAdapter = PortDispatchCompleteAdapter(mListener = this)
        binding.rvSummary.adapter = mAdapter
        binding.btnConfirm.setOnClickListener { showConfirmDialog() }
    }

    private fun initExtras() {
        otNumber = intent.getStringExtra(PortWHUtil.OT_NUMBER) ?: ""
        vm.setOtId(otNumber)
    }

    private fun updateUI() {
        vm.mSelectedOT?.let {
            binding.tvOtNumber.text = it.otNumber
            binding.tvNumberOfBales.text = it.totalNumberOfBales.toString()
            binding.tvTotalWeight.text = it.weight?.format().plus(" ").plus(it.unitOfMeasure)
            binding.tvNetWeight.text = it.otBaleNetWeight?.format()
            val tareWT: Double? = it.otBaleGrossWeight?.minus(it.otBaleNetWeight!!)
            binding.tvTareWeight.text = tareWT?.format()
            binding.tvGrossWeight.text = it.otBaleGrossWeight?.format()
        }
        vm.mOTWithContainers?.containers?.let {
            mAdapter.addItems(it)
        }
    }

    /*Fetch OT details and Populate UI*/
    private fun getOTDetails() {
        vm.getOTDetailCom(otNumber)
        vm.otDetailsCom.observe(this, Observer { updateOTDetails(it) })
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
                    UIUtils.showErrorDialog(
                        this, it.error.toString()
                    )
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
            val OtData =
                runBlocking {
                    withContext(Dispatchers.IO)
                    {
                        vm.getOTById(otNumber)
                    }
                }
            vm.setSelectedOT(OtData)
            updateUI()
        }
    }

    private fun showConfirmDialog() {

        showDialog(resources.getString(R.string.dialog_confirm_dispatch),
            object : DialogClick {
                override fun onPositive(dialog: DialogInterface) {
                    getAllDetailsforOT()
                }

                override fun onNegative(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            })

        /*  MaterialDialog(this).show {
              positiveButton(R.string.yes) {
                  getAllDetailsforOT()
              }
              negativeButton(R.string.cancel) { dismiss() }
              message(R.string.dialog_confirm_dispatch)
          }*/
    }

    private fun getAllDetailsforOT() {
        if (vm.mSelectedOT?.numberOfContainer?.let { isValidCount(it) } == true) {
            vm.confirmDispatch(otNumber)
            vm.confirmDispatch.observe(this, Observer { updateSuccess(it) })
        } else {
            showErrorDialog(getString(R.string.count_mismatch))
        }
    }

    private fun updateSuccess(response: Resource<GenericReqAndResp<GenericMessage>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when {
                        it.data?.data?.success!! -> moveToSuccess()
                        else -> toast(it.data?.data?.message.toString())
                    }
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

    private fun moveToSuccess() {
        val intent = Intent(this, SuccessPortActivity::class.java)
        intent.putExtra(
            PortWHUtil.OT_NUMBER,
            getString(R.string.for_ot).format().plus(" ").plus(otNumber)
        )
        intent.putExtra(PortWHUtil.SUCCESS, PortWHUtil.DISPATCH_SUCCESS)
        startActivity(intent)
    }

    private fun isValidCount(count: Int): Boolean {
        val originalCount = vm.mOTWithContainers?.containers?.size
        return when {
            count == originalCount!! -> true
            else -> false
        }

    }

    private fun showErrorDialog(message: String) {
        showSingleDialog(message, object : DialogSingleClick {
            override fun onClick(dialog: DialogInterface) {
                dialog.dismiss()
            }

        })
    }
}
