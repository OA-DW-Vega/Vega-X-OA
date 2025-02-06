package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.ginninginprogress

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.GinningInprogress
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.DateUtils.getReadableCurrentDate
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.format
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ActivityGinningConfirmBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.LOT_DETAIL
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.SUB_TITLE
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.TITLE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by SangiliPandian C on 13-03-2020.
 */
class GinningConfirmActivity : HomeBaseActivity() {

    private val vm: GinningInprogressViewModel by viewModel()
    private var mLotDetail: GinningInprogress? = null
    private lateinit var mAdapter: GinningBaleListConfirmAdapter
    private lateinit var binding: ActivityGinningConfirmBinding

    override val layoutResourceId = R.layout.activity_ginning_confirm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGinningConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        initExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ginningwarehouse/ui/ginninginprogress/GinningConfirmActivity")
            .title("Ginningwarehouse").with(tracker)

    }


    private fun initExtras() {
        intent?.let {
            mLotDetail =
                Gson().fromJson(PreferenceHelper.get(LOT_DETAIL, ""), GinningInprogress::class.java)
        }
    }

    private fun initUI() {
        val lm = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mAdapter = GinningBaleListConfirmAdapter()
        binding.rvBales.apply {
            layoutManager = lm
            addItemDecoration(DividerItemDecoration(binding.rvBales.context, lm.orientation))
            adapter = mAdapter
        }
        mLotDetail?.let {
            binding.tvLotNo.text = it.lotNumber
            binding.tvNoOfBales.text = it.bales?.size.toString()
            binding.tvWeight.text =
                it.bales?.sumByDouble { it.grossWeight ?: 0.0 }?.format().plus("KG")
            binding.tvDate.text = getReadableCurrentDate()
            mAdapter.submitList(it.bales)
        }
        binding.btnProceed.setOnClickListener {
            showDialog(
                resources.getString(R.string.ginning_confirm_alert),
                object : DialogClick {
                    override fun onPositive(dialog: DialogInterface) {
                        postLotForGinning()
                    }

                    override fun onNegative(dialog: DialogInterface) {
                        dialog.dismiss()
                    }

                }, postiveText = R.string.yes, negativeText = R.string.cancel, isColor = false
            )
        }

        vm.saveGinning.observe(this, Observer {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    it.data?.let {
                        it.data.let {

                            if (it.success) {
                                moveToSuccess()
                            } else {
                                showDialog(it.message)
                            }
                        }

                    }

                    hideLoading()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(this, it.error.toString())
                }
            }

        })

    }

    private fun postLotForGinning() {
        when (mLotDetail != null) {
            true -> {
                showLoading()
                vm.saveGinning(mLotDetail!!)

            }
            else -> toast("Invalid lot")
        }
    }



    private fun moveToSuccess() {
        val lotId = mLotDetail?.lotNumber
        runBlocking {
            withContext(Dispatchers.IO) {
                lotId?.let { vm.deleteBalesByLotId(it) }
            }
        }
        val intent = Intent(this, SuccessActivity::class.java)
        intent.putExtra(TITLE, getString(R.string.ginning_completed_success))
        intent.putExtra(SUB_TITLE, "LOT ID: $lotId")
        startActivity(intent)
    }
}
