package com.olam.warehouse.ginning.ui.ginninginprogress

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.olam.warehouse.ginning.data.model.enums.LotStatus
import com.olam.warehouse.ginning.di.injectGinningInprogressFeature
import com.olam.warehouse.ginning.utils.*
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.GinningInprogress
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.WERKS
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.activity_ginning_inprogress.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by SangiliPandian C on 05-03-2020.
 */
class GinningInprogressActivity : HomeBaseActivity() {

    private var mLotDetail: GinningInprogress? = null
    private val mBales = arrayListOf<Bale>()
    private val vm: GinningInprogressViewModel by viewModel()
    private var baleId: String = ""
    private var mAdapter: GinningBaleListAdapter =
        GinningBaleListAdapter(listener = object : GinningBaleListAdapter.BaleChangeListener {
            override fun update(bale: Bale?, position: Int) {

                showDeleteDialog(bale, position)
            }

        })

    override val layoutResourceId = R.layout.activity_ginning_inprogress

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGinningInprogressFeature()
        initNavigationView()
        initUI()
        fetchLotDetails()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("ginningwarehouse/ui/ginninginprogress/GinningInprogressActivity")
            .title("Ginningwarehouse").with(tracker)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            RESULT_OK != resultCode -> return
            data == null -> {
                toast(getString(R.string.bale_id_not_valid))
                return
            }
            requestCode == ACTIVITY_SCAN_REQUEST_CODE -> {
                val baleId = data.getStringExtra(Constants.SCANNED_ID)?.trim()
                if (!baleId.isNullOrEmpty()) {
                    validateBaleId(baleId)
                } else {
                    toast(getString(R.string.bale_id_not_valid))
                }
            }
            requestCode == ACTIVITY_REQUEST_CODE -> {
                data.getParcelableExtra<Bale>(BALE)?.let {
                    addBale(it)
                }
            }
        }
    }

    private fun addBale(bale: Bale) {
        bale.lotNumber = mLotDetail?.lotNumber
        bale.isHold = false
        bale.createdTS = getCurrentTimeInMills().toString()
        if(!mBales.contains(bale)) {
            mBales.add(bale)
            saveBaleInDb(bale)
        }
        mAdapter.addItem(bale)
    }

    private fun saveBaleInDb(bale: Bale) {
        runBlocking {
            withContext(Dispatchers.IO) {
                vm.insertGInningBales(bale)

            }
        }
    }

    private fun initUI() {
        PreferenceHelper.save(Constants.IS_BT, true)
        val lm = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        /* mAdapter =
             GinningBaleListAdapter(listener = object : GinningBaleListAdapter.BaleChangeListener {
                 override fun update(bale: Bale?, position: Int) {

                     showDeleteDialog(bale, position)
                 }

             })*/
        rvBales.apply {
            layoutManager = lm
            addItemDecoration(DividerItemDecoration(rvBales.context, lm.orientation))
            adapter = mAdapter
        }

        etBale.onChange {
            val baleId = it.trim()
            btnAdd.isEnabled = baleId.length == 10
            val color =
                if (btnAdd.isEnabled) getColorFromId(R.color.green_button) else getColorFromId(R.color.grey)
            btnAdd.setBackgroundColor(color)
        }

        btnAdd.setOnClickListener { validateBaleId(etBale.text.toString()) }

        btnProceed.setOnClickListener { moveToGinningConfirmPage() }

        btnScan.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            startActivityForResult(intent, ACTIVITY_SCAN_REQUEST_CODE)
        }

        btnPause.setOnClickListener {
            showDialog(
                resources.getString(R.string.ginning_pause_alert),
                object : DialogClick {
                    override fun onPositive(dialog: DialogInterface) {
                        pauseGinning()
                    }

                    override fun onNegative(dialog: DialogInterface) {
                        dialog.dismiss()
                    }

                }, postiveText = R.string.yes, negativeText = R.string.cancel, isColor = false
            )
        }
        vm.validateBale.observe(this, Observer {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    it.data?.let {
                        if (it.success) {
                            it.data.let {

                                moveToExtractPage(baleId)
                            }
                        } else {
                            UIUtils.showErrorDialog(this, it.data.message)
                        }
                    }

                    hideLoading()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(this, it.error.toString())
                }
            }

        })
    }

    fun showDeleteDialog(bale: Bale?, position: Int) {

        showDialog(resources.getString(R.string.dialog_confirm_bale_delete),
            object : DialogClick {
                override fun onPositive(dialog: DialogInterface) {
                    deleteBaleFromDb(bale)
                    mBales.remove(bale)
                    mAdapter.addItems(mBales)
                }

                override fun onNegative(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            })
        /*MaterialDialog(this).show {
            cancelable(false)
            message(R.string.delete_bale_alert)
            positiveButton(R.string.yes) {
                deleteBaleFromDb(bale)
                mBales.remove(bale)
                mAdapter.addItems(mBales)
            }
            negativeButton(R.string.cancel) { dismiss() }
        }*/
    }

    private fun deleteBaleFromDb(bale: Bale?) {
        bale?.let {
            runBlocking {
                withContext(Dispatchers.IO) {
                    vm.deleteBaleFromDb(it)

                }
            }
        }
    }

    private fun moveToGinningConfirmPage() {
        if (mAdapter.isMoreBaleAdded()) {
            mLotDetail?.let {
                val isLatsShift = mBales.any { bale -> bale.isLastBaleOfShift == true }
                it.status =
                    if (isLatsShift) LotStatus.SHIFT_CHANGE.value else LotStatus.COMPLETED.value
                it.bales = mBales.filter { it -> it.isHold != true }
                it.plantId = PreferenceHelper.get(WERKS, "")
                PreferenceHelper.save(LOT_DETAIL, Gson().toJson(mLotDetail))
                startActivity(Intent(this, GinningConfirmActivity::class.java))
            }
        } else {
            showDialog(getString(R.string.add_atleast_one_bale_for_this_lot))
        }
    }

    private fun pauseGinning() {
        mLotDetail?.let {
            showLoading()
            it.status = LotStatus.HOLD.value
            it.bales = mBales.filter { bale -> bale.isHold != true }
            it.plantId = PreferenceHelper.get(WERKS, "")
            vm.saveGinning(it)
            vm.saveGinning.observe(this, Observer {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let {
                            it.data.let {

                                if (it.success) {
                                    moveToSuccessPage()
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
                        UIUtils.showErrorDialog(this, it.error.toString())
                    }
                }

            })
        }
    }

    private fun moveToSuccessPage() {
        val intent = Intent(this, SuccessActivity::class.java)
        intent.putExtra(TITLE, getString(R.string.ginning_pause_success))
        intent.putExtra(SUB_TITLE, "LOT ID: ${mLotDetail?.lotNumber}")
        startActivity(intent)
    }

    private fun validateBaleId(baleId: String) {

        if (!isBaleAlreadyExist(baleId)) {
            validateBaleOnServer(baleId)
        } else {
            hideLoading()
            toast(getString(R.string.bale_id_already_exists))
        }
    }

    private fun validateBaleOnServer(baleId: String) {
        showLoading()
        vm.validateBale(baleId)
        this.baleId = baleId
    }

    private fun moveToExtractPage(baleId: String) {
        val intent = Intent(this, GinningExtractionActivity::class.java)
        intent.putExtra(BALE_ID, baleId)
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE)
    }

    private fun isBaleAlreadyExist(id: String): Boolean {
        val bales = mAdapter.getData()
        return if (bales.isEmpty()) false
        else bales.any { it?.baleID.equals(id) }
    }

    private fun fetchLotDetails() {
        showLoading()
        vm.fetchLotDetails()
        vm.fetchLotDetails .observe(this, Observer {

            when (it.status) {
                Resource.Status.SUCCESS -> {
                    it.data?.let {
                        it.data.let {

                            it.let { updateUI(it) }
                        }

                    }

                    hideLoading()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(this, it.error.toString())
                }
            }

        })
    }

    private fun updateUI(data: GinningInprogress) {
        val lotNumber = data.lotNumber
        if (lotNumber.isNullOrEmpty()) {
            setErrorContentView("No LOTs currently in Ginning")
            return
        }
        mLotDetail = data
        tvLotNo.text = lotNumber
        data.bales?.let {
            it.forEach { bale -> bale.lotNumber = lotNumber; bale.isHold = true }
            mBales.clear()
            mBales.addAll(it)
        }
        addLocalBales(lotNumber)
    }

    private fun addLocalBales(lotNumber: String) {
        vm.getBalesByLotNumber(lotNumber)
        vm.getBalesByLotNumber.observe(this, Observer {
            it.forEach {
                if(!mBales.contains(it)) {
                    mBales.add(it)
                }
            }

            mAdapter.addItems(mBales)
        })
    }

}
