package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.scan

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.IncomingLot
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.IncomingLotDetails
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants.WERKS
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ActivityScanLotDetailsBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.di.injectGinningIncomingLOTsFeature
import com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.seedcotton.GinningIncomingLotActivity
import com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.seedcotton.GinningIncomingLotViewModel
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.SUB_TITLE
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.TITLE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 12/23/2020.
 */
class ScanLotDetailsActivity : HomeBaseActivity(), HomeBaseActivity.DialogSingleClick {
    override val layoutResourceId = R.layout.activity_scan_lot_details
    private var mIncomingLot: IncomingLotDetails? = null
    private val vm: GinningIncomingLotViewModel by viewModel()
    private lateinit var binding: ActivityScanLotDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanLotDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        injectGinningIncomingLOTsFeature()
        initNavigationView()
        initExtra()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ginningwarehouse/ui/scan/ScanLotDetailsActivity")
            .title("Ginningwarehouse").with(tracker)
    }

    private fun initExtra() {
        mIncomingLot = intent.getParcelableExtra<IncomingLotDetails>(UIUtils.LOT_DETAIL)
    }

    private fun initUI() {
        binding.tvLotNoValue.text = mIncomingLot?.lotNumber
        binding.tvTruckNoValue.text = mIncomingLot?.truckNumber
        binding.tvContainerNoValue.text = mIncomingLot?.containerNumber
        binding.tvReceivedOnValue.text = mIncomingLot?.createdTS
        /*if (mIncomingLot?.createdTS != null) {
            tvReceivedOnValue.text = mIncomingLot?.createdTS?.let { it1 ->
                DateUtils.getUTCDateTime(
                    it1.toString(),
                    this
                )
            }
        }*/
        binding.tvViewIncomingLot.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    GinningIncomingLotActivity::class.java
                )
            )
        }
        binding.tvAddToGinning.setOnClickListener { postLotForGinning(mIncomingLot) }

        vm.postLotForGinning.observe(this, Observer {
                response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let {
                            val data = it.data

                            mIncomingLot.let { mtn ->
                                it.data.message.let { message ->
                                    moveToSuccess()
                                }
                            }


                            //dao.insertOrReplaceDeliverys(it)
                        }

                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        showErrorDialogWithFAQLink(this, it.error.toString())
                    }
                }
            }
        })

    }

    private fun postLotForGinning(mIncomingLot: IncomingLotDetails?) {
        when (mIncomingLot != null) {
            true -> {
                showLoading()
                vm.postLotForGinning(prepareLotItems(mIncomingLot))

            }
            else -> toast("Invalid lot")
        }
    }
    fun prepareLotItems(mIncomingLot: IncomingLotDetails): IncomingLot {
        val incomingLot = IncomingLot()
        incomingLot.containerNumber = mIncomingLot.containerNumber.toString()
        incomingLot.truckNumber = mIncomingLot.truckNumber.toString()
        incomingLot.createdTS = mIncomingLot.createdTS.toString()
        incomingLot.lotNumber = mIncomingLot.lotNumber.toString()
        incomingLot.plantId = PreferenceHelper.get(WERKS, "")
        return incomingLot
    }



    private fun moveToSuccess() {
        val intent = Intent(this, SuccessActivity::class.java)
        intent.putExtra(TITLE, "Succesfully sent for Ginning")
        intent.putExtra(SUB_TITLE, "LOT ID: ${mIncomingLot?.lotNumber}")
        startActivity(intent)
    }

    override fun onClick(dialog: DialogInterface) {
        dialog.dismiss()
    }

}
