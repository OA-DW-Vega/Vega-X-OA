package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.seedcotton

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.IncomingLot
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ActivityGinningIncomingLotBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.SUB_TITLE
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.TITLE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

/**
 * Created by SangiliPandian C on 05-03-2020.
 */

class GinningIncomingLotActivity : HomeBaseActivity(), HomeBaseActivity.DialogSingleClick {
    private val vm: GinningIncomingLotViewModel by viewModel()
    private var mIncomingLot: IncomingLot? = null
    private lateinit var mAdapter: GinningIncomingLotAdapter
    private var isBackEnable = true

    override val layoutResourceId = R.layout.activity_ginning_incoming_lot
    private lateinit var binding: ActivityGinningIncomingLotBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGinningIncomingLotBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //injectGinningIncomingLOTsFeature()
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("ginningwarehouse/ui/incominglots/seedcotton/GinningIncomingLotActivity")
            .title("Ginningwarehouse").with(tracker)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ginning_search_menu, menu)
        val search = menu.findItem(R.id.search)
        val searchView: SearchView = search?.actionView as SearchView
        searchView.setBackgroundColor(
            ContextCompat.getColor(
                this,
                com.olam.warehouse.presentation.R.color.colorPrimaryOfi
            )
        )
        searchView.queryHint = getString(R.string.seach_lots)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                mAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                mAdapter.filter.filter(newText)
                return false
            }
        })
        return true
    }

    private fun initUI() {
        val lm = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mAdapter =
            GinningIncomingLotAdapter(
                incomingList = Collections.emptyList(),
                listener = object :
                    GinningIncomingLotAdapter.Listener {
                    override fun update(incomingLot: IncomingLot) {
                        mIncomingLot = incomingLot
                        enableProceedButton()
                    }
                })
        binding.rvIncomingLots.apply {
            layoutManager = lm
            addItemDecoration(DividerItemDecoration(binding.rvIncomingLots.context, lm.orientation))
            adapter = mAdapter
        }
        vm.postLotForGinning.observe(this, Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let {
                            /*val data = it.data
                            mIncomingLot?.let { mtn ->
                                it.data?.message?.let { message ->
                                    moveToSuccess()
                                }
                            }*/
                            if (it.data.success)
                                moveToSuccess()
                            else
                                showErrorDialogWithFAQLink(this, it.data.message)
                            //dao.insertOrReplaceDeliverys(it)
                        }

                        hideCustomLoading()
                    }
                    Resource.Status.LOADING -> showCustomLoading()
                    Resource.Status.ERROR -> {
                        hideCustomLoading()
                        showErrorDialogWithFAQLink(this, it.error.toString())
                    }
                }
            }
        })
        binding.btnProceed.setOnClickListener { showConfirmDialog() }
        fetchIncomingLots()


        vm.fetchingIncomingLots.observe(this, androidx.lifecycle.Observer { response ->
            isBackEnable = true
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let {
                            val data = it.data
                            when (data.isNullOrEmpty()) {
                                true -> setErrorContentView(it.message.toString())
                                else -> updateUI(data)
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
            }
        })

    }

    private fun showConfirmDialog() {
        MaterialDialog(this).show {
            message(R.string.add_to_ginning_confirm)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(R.string.cancel),
                {
                    postLotForGinning()
                },
                { dismiss() })
        }
    }

    private fun postLotForGinning() {
        when (mIncomingLot != null) {
            true -> {
                vm.postLotForGinning(mIncomingLot!!)
            }
            else -> toast("Invalid lot")
        }
    }

    private fun enableProceedButton() {
        binding.btnProceed.setBackgroundColor(
            resources.getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
        )
        binding.btnProceed.isEnabled = true
    }

    private fun fetchIncomingLots() {
        showLoading()
        isBackEnable = false
        vm.fetchingIncomingLots()

    }

    private fun updateUI(data: List<IncomingLot>) {
        mAdapter.updateData(data.toMutableList())
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

    override fun onBackPressed() {
        backNaviagation()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            when (it.itemId) {
                android.R.id.home -> {
                    backNaviagation()
                    return true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun backNaviagation() {
        when (isBackEnable) {
            true -> super.onBackPressed()
            else -> {
            }
        }
    }
}
