package com.olam.warehouse.ginning.ui.dispatch

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.ginning.utils.*
import com.olam.warehouse.ginning.work.dispatchPostWorker
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.VegaCottonGinningDispatchDelivery
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.DeliveryWithBales
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.fragment_ginning_dispatch_offline.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 3/25/2020.
 */
class GinningOfflineDispatchFragment : BaseFragment() {

    private var deliveryList = arrayListOf<VegaCottonGinningDispatchDelivery>()
    private var deliveryWithBalesList = arrayListOf<DeliveryWithBales>()
    private var callBack: CallBack? = null
    private var syncBtn: String? = ""

    private var mAdapter = GinningOfflineDispatchAdapter({ moveParms(it) }, { viewDetails(it) })
    private val vm: GinningDispatchViewModel by viewModel()

    override val layoutResourceId = R.layout.fragment_ginning_dispatch_offline

    companion object {
        fun newInstance(deliveryNo: String) = GinningOfflineDispatchFragment().putArgs {
        }
    }

    interface CallBack {
        fun replaceFragment(moveFrag: String, deliveryNo: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intiExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ginningwarehouse/ui/dispatch/GinningOfflineDispatchFragment")
            .title("Ginningwarehouse").with(tracker)
    }

    private fun intiExtras() {
        //delNoSpin =
    }

    fun initUI() {
        rvDeliverySummary.layoutManager = LinearLayoutManager(this.context)
        rvDeliverySummary.adapter = mAdapter

        tvConfirmDispatch.setOnClickListener {
            val btnText = tvConfirmDispatch.text
            if (btnText.contains("OK")) activity?.finish()
            else showConfirmDialog()
        }
        vm.getAllDeliveryWithBales()
        vm.allDeliveryWithBales.observe(viewLifecycleOwner, Observer {
            deliveryWithBalesList = it as ArrayList<DeliveryWithBales>
            vm.fetchDeliveryDetailsOffline()

        })

        vm.deliveryDetailsOfflineVegaCotton.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            deliveryList = it as ArrayList<VegaCottonGinningDispatchDelivery>
            deliveryList.forEach { dispatch ->

                val balesData =
                    deliveryWithBalesList.filter { it.deliveryVegaCotton.deliveryNumber == dispatch.deliveryNumber }
                        .get(0)
                dispatch.baleDTO = balesData.bales
            }
            enableProceedBtn(isOnline())
            mAdapter.addItems(deliveryList)

        })

    }

    private fun showConfirmDialog() {
        syncBtn = tvConfirmDispatch.text.toString()
        MaterialDialog(requireContext()).show {
            message(R.string.sync_all)
            getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                { postdata() },
                { dismiss() })
        }
    }

    private fun postdata() {
        if (syncBtn.equals(getString(R.string.resync))) onSyncProceed(mAdapter.getItems())
        else onSyncProceed(deliveryList)
    }

    private fun enableProceedBtn(online: Boolean) {

        if (online) {
            tvConfirmDispatch.isEnabled = true
            ViewCompat.setBackgroundTintList(
                tvConfirmDispatch,
                context?.let { ContextCompat.getColorStateList(it, R.color.green) }
            )
        } else {
            tvConfirmDispatch.isEnabled = false
            ViewCompat.setBackgroundTintList(
                tvConfirmDispatch,
                context?.let { ContextCompat.getColorStateList(it, R.color.grey) }
            )
        }
    }

    private fun moveParms(it: VegaCottonGinningDispatchDelivery?) {
        it?.let { it1 -> showConfirmDialog(it1) }
    }

    private fun viewDetails(deliveryVegaCotton: VegaCottonGinningDispatchDelivery?) {
        deliveryVegaCotton?.let {
            vm.updateDeliveryStatusToEdit(deliveryVegaCotton.deliveryNumber)
            callBack?.replaceFragment(FRGA_DISPATCH_ADD_BALE, deliveryVegaCotton.deliveryNumber)
        }
    }

    private fun showConfirmDialog(deliveryVegaCotton: VegaCottonGinningDispatchDelivery) {
        MaterialDialog(requireContext()).show {
            message(R.string.delete_offline_list_alert)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.deleteOfflineDeliveryWithBales(deliveryVegaCotton.deliveryNumber)
                    deliveryList.remove(deliveryVegaCotton)
                    mAdapter.removeItems(deliveryVegaCotton)
                    mAdapter.notifyDataSetChanged()
                },
                { dismiss() })
        }
    }

    fun onSyncProceed(deliveryListVegaCotton: ArrayList<VegaCottonGinningDispatchDelivery>) {
        deliveryListVegaCotton.forEachIndexed { _index, delivery ->
            if (delivery.status != 4) {
                val input = workDataOf(DISPATCH_DATA to delivery.deliveryNumber)
                val worker = dispatchPostWorker(input, _index)
                enQueueWorker(worker, requireContext())
                showLoading()
                WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
                    .observe(viewLifecycleOwner, Observer { workInfo ->

                        if (workInfo != null) {
                            when (workInfo.state) {
                                WorkInfo.State.SUCCEEDED -> {
                                    hideLoading()
                                    val position = posExtension(workInfo.tags)

                                    mAdapter.setSyncStatus(
                                        position,
                                        true,
                                        4,
                                        workInfo.outputData.getString(DISPATCH_OUTPUT_DATA)
                                    )
                                    if (_index == deliveryListVegaCotton.size - 1) {
                                        changeBtn(deliveryListVegaCotton)
                                    }
                                }
                                WorkInfo.State.FAILED -> {
                                    hideLoading()
                                    val position = posExtension(workInfo.tags)
                                    mAdapter.setSyncStatus(
                                        position,
                                        false,
                                        3,
                                        workInfo.outputData.getString(DISPATCH_OUTPUT_DATA)
                                    )
                                    workInfo.outputData.getString(DISPATCH_OUTPUT_DATA)?.let {
                                        vm.updateErrorMessage(
                                            it,
                                            deliveryListVegaCotton[position].deliveryNumber
                                        )
                                    }
                                    if (_index == deliveryListVegaCotton.size - 1) {
                                        changeBtn(deliveryListVegaCotton)
                                    }
                                }
                                WorkInfo.State.RUNNING -> {
                                    val position = posExtension(workInfo.tags)
                                    mAdapter.setSyncStatus(position, true, 2, "")
                                    showLoading()
                                }
                                else -> {
                                }
                            }
                        }

                    })
                hideLoading()
            }

        }
    }

    fun changeBtn(offlineList: ArrayList<VegaCottonGinningDispatchDelivery>) {
        val count = mAdapter.getItems().filter { it.status!!.equals(4) }
        if (offlineList.size == count.size) tvConfirmDispatch.text = getString(R.string.ok)
        else tvConfirmDispatch.text = getString(R.string.resync)
    }
}
