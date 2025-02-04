package com.olam.warehouse.ginning.ui.drying

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.ginning.di.injectGinningDryingFeature
import com.olam.warehouse.ginning.utils.SUB_TITLE
import com.olam.warehouse.ginning.utils.TITLE
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.IncomingLot
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.activity_ginning_drying.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

/**
 * Created by SangiliPandian C on 05-03-2020.
 */

class GinningDryingActivity : HomeBaseActivity() {

    private val vm: GinningDryingViewModel by viewModel()
    private lateinit var mAdapter: GinningDryingAdapter

    override val layoutResourceId = R.layout.activity_ginning_drying

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectGinningDryingFeature()
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ginningwarehouse/ui/drying/GinningDryingActivity")
            .title("Ginningwarehouse").with(tracker)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.ginning_search_menu, menu)
        val search = menu?.findItem(R.id.search)
        val searchView: SearchView = search?.actionView as SearchView
        searchView.setBackgroundColor(resources.getColor(R.color.green))
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
            GinningDryingAdapter(
                incomingList = Collections.emptyList(),
                listener = object : GinningDryingAdapter.DryingListener {
                    override fun update() {
                        val selectedLots = mAdapter.getItems().filter { it.isSelected }.count()
                        tvSelectedLots.text = "Selected: $selectedLots"
                        enableProceedButton()
                    }
                })
        rvDryingLots.apply {
            layoutManager = lm
            addItemDecoration(DividerItemDecoration(rvDryingLots.context, lm.orientation))
            adapter = mAdapter
        }
        btnProceed.setOnClickListener { postLotForGinning() }

        fetchDryingLots()
    }

    private fun postLotForGinning() {
        when (mAdapter.getItems().any { it.isSelected }) {
            true -> {
                showLoading()
                vm.postLotForGinning(mAdapter.getItems())

                vm.postLotForGinning.observe(this, androidx.lifecycle.Observer { response ->
                    response?.let {
                        when (it.status) {
                            Resource.Status.SUCCESS -> {
                                it.data?.let {
                                    when {
                                        it.data.success -> {
                                            moveToSuccess()
                                        }
                                        else -> UIUtils.showErrorDialog(this, it.data.message)
                                    }
                                }
                                //dao.insertOrReplaceDeliverys(it)
                                hideLoading()
                            }
                            Resource.Status.LOADING -> showLoading()
                            Resource.Status.ERROR -> {
                                hideLoading()
                                UIUtils.showErrorDialog(this, it.error ?: "Something went wrong")
                            }
                        }
                    }
                })
            }
            else -> UIUtils.showErrorDialog(this, getString(R.string.please_select_atleast_one_lot_to_continue))
        }
    }

    private fun enableProceedButton() {
        btnProceed.setBackgroundColor(
            resources.getColor(R.color.green)
        )
        btnProceed.isEnabled = true
    }

    private fun fetchDryingLots() {
        showLoading()
        vm.fetchDryingLots()
        vm.fetchingDryingLots.observe(this, androidx.lifecycle.Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        if (it.data!!.success == true) {
                            it.data?.let {
                                updateUI(it.data)
                            }
                        } else {
                            UIUtils.showErrorDialog(this, it.data?.message ?: "")
                        }
                        //dao.insertOrReplaceDeliverys(it)
                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        UIUtils.showErrorDialog(this, it.error.toString())
                    }
                }
            }
        })
    }



    private fun updateUI(data: List<IncomingLot>) {
        tvNoOfLots.text = "Currently: ${data.size}"
        mAdapter.updateData(data.toMutableList())
    }


    private fun moveToSuccess() {
        val intent = Intent(this, SuccessActivity::class.java)
        intent.putExtra(TITLE, getString(R.string.drying_completed_success))
        intent.putExtra(
            SUB_TITLE,
            "LOT ID: ${mAdapter.getItems().filter { it.isSelected }.joinToString { it.lotNumber }}"
        )
        startActivity(intent)
    }
}
