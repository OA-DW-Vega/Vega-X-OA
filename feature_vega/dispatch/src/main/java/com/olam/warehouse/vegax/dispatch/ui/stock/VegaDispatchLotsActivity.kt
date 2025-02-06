package com.olam.warehouse.vegax.dispatch.ui.stock

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.dispatch.R
import com.olam.warehouse.vegax.dispatch.data.domain.model.VegaDispatchLotQuality
import com.olam.warehouse.vegax.dispatch.databinding.ActivityVagaDispatchLotsBinding
import com.olam.warehouse.vegax.dispatch.ui.VegaDispatchViewModel
import com.olam.warehouse.vegax.dispatch.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 2/20/2020.
 */
class VegaDispatchLotsActivity : HomeBaseActivity() {

    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var dispatchLotsList = mutableListOf<VegaDispatchLots>()
    private var dispatchLocationLotsList = mutableListOf<VegaDispatchLots>()
    private val mSearchList = mutableListOf<VegaDispatchLots>()
    private var selectedLotsList = mutableListOf<VegaDispatchLots>()
    private var addedLotsList = ArrayList<String>()
    private var materialNo: String? = ""
    private var selectedPos: Int = 0
    private var isStockFetched = false

    private var mAdapter = VegaDispatchLotsAdapter({ moveChecked(it) }, { moveUnChecked(it) })
    private val vm: VegaDispatchViewModel by viewModel()
    private lateinit var binding: ActivityVagaDispatchLotsBinding
    override val layoutResourceId = R.layout.activity_vaga_dispatch_lots

    companion object {
        const val SEARCH_HINT_TEXT = "Search Lot Item"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVagaDispatchLotsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("dispatch/ui/stock/VegaDispatchLotsActivity").title("Dispatch").with(tracker)
        initNavigationView()
        initUI()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(com.olam.warehouse.vegax.dispatch.utils.getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint = SEARCH_HINT_TEXT
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setUpAdapter(dispatchLocationLotsList)
                        } else {
                            mSearchList.clear()
                            dispatchLocationLotsList.forEach { lots ->
                                newText?.let { text ->
                                    if (lots.batchNumber.contains(text)) {
                                        mSearchList.add(lots)
                                    }
                                }
                            }
                            setUpAdapter(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
        return true
    }

    private fun initUI() {
        this.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnAdd, it, true)
        }
        binding.rvLots.layoutManager = LinearLayoutManager(this)
        binding.rvLots.adapter = mAdapter

        materialNo = intent.getStringExtra(Constants.MATERIAL_NUMBER)
        isStockFetched = intent.getBooleanExtra(Constants.IS_STOCK_FETCHED, false)
//        addedLotsList =
//            intent.getStringArrayListExtra(Constants.SELECTED_STOCKS_ID) as ArrayList<String>
        binding.llSelectDW.setOnClickListener { showLocationDialog() }
        binding.btnAdd.setOnClickListener { moveBackAddStock() }

        vm.customLocation.observe(this, Observer { custonLocationList = it.toMutableList() })
        vm.getCustomLocations()

//        if (!isStockFetched) {
            vm.stocks.observe(this, Observer { updateUI(it) })
            materialNo?.let { vm.fetchStocks(it) }
//        } else {
//            vm.stocksOffline.observe(this, Observer { updateUiOffline(it) })
//            materialNo?.let { vm.fetchStocksOffline(it) }
//        }

        //vm.qualityDetails.observe(this, Observer { updateParams(it) })

        updateStockWeight()
    }

    private fun moveBackAddStock() {
        val gson = GsonUtils()
        val stocks = gson.toJson(selectedLotsList)
        val resultIntent = Intent()
        resultIntent.putExtra(Constants.SELECTED_STOCKS_LIST, stocks)
        resultIntent.putExtra(Constants.IS_STOCK_FETCHED, isStockFetched)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()

    }

    override fun onBackPressed() {
        // super.onBackPressed()
        moveBackAddStock()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            when (it.itemId) {
                android.R.id.home -> {
                    moveBackAddStock()
                    return true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateUiOffline(it: List<VegaDispatchLots>) {
        dispatchLotsList.clear()
        dispatchLotsList = it as MutableList<VegaDispatchLots>
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaDispatchLots>>>) {

        when (response.status) {
            Resource.Status.SUCCESS -> {
                response.data?.let {
                    dispatchLotsList.clear()
                    isStockFetched = true
                    dispatchLotsList = it.data as MutableList<VegaDispatchLots>
                }
                hideLoading()
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(this, response.error.toString())
            }
            else -> hideLoading()
        }
    }

    private fun updateParams(data: Resource<GenericReqAndResp<List<VegaDispatchLotQuality>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    //hideLoading()
                    it.data?.data?.let { items ->
                        items[0].qualityParameters.forEach {
                            if (it.sapQCName?.equals("CI_RCN_REGION")!!) {
                                dispatchLocationLotsList[selectedPos].region = it.satNam
                            } else if (it.sapQCName?.equals("CI_RCN_KOR")!!) {
                                dispatchLocationLotsList[selectedPos].kor = it.satNam
                            }
                        }
                        selectedLotsList.add(dispatchLocationLotsList[selectedPos])
                        updateStockWeight()
                        dispatchLocationLotsList[selectedPos].isProgress = false
                        mAdapter.notifyItemChanged(selectedPos)
                    }
                }
                Resource.Status.LOADING -> {
                    //showLoading()
                }
                Resource.Status.ERROR -> {
                    //hideLoading()
                    showErrorDialogWithFAQLink(this, "Quality Fetching Failed")
                    // context?.toast()
                }
                else -> {
                }
            }
        }
    }

    private fun setUpAdapter(dispatchLots: MutableList<VegaDispatchLots>) {
        mAdapter.addItems(dispatchLots)
    }

    private fun showLocationDialog() {
        val location =
            custonLocationList.filter { !it.storageLocationType.equals("P") }
                .map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }
        MaterialDialog(this).show {
            title(R.string.select_dispatch_warehouse)
            listItemsSingleChoice(items = location) { _, index, text ->
                binding.tvSelectDispatch.text = text
                val data = dispatchLotsList.filter { it.storageLocationCode.equals(text.split(" - ")[0]) }
                dispatchLocationLotsList = data as MutableList<VegaDispatchLots>
                if (dispatchLocationLotsList.size > 0) {
                    binding.llItems.visible()
                    binding.tvNoData.gone()
                    dispatchLocationLotsList.forEach {
                        it.editedWeight = it.weight
                        //if(addedLotsList.contains(it.batchNumber)) it.isAdded = true
                    }
                    setUpAdapter(dispatchLocationLotsList)
                } else {
                    binding.llItems.gone()
                    binding.tvNoData.visible()
                }
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.presentation.R.string.ok), true))
        }
    }

    private fun moveChecked(pos: Int) {
        selectedPos = pos
        dispatchLocationLotsList[pos].isChecked = true
        if (dispatchLocationLotsList[pos].kor.isNullOrEmpty() && dispatchLocationLotsList[pos].region.isNullOrEmpty()) {
            /*vm.getQualityParams(
                dispatchLocationLotsList[pos].batchNumber.toString(),
                dispatchLocationLotsList[pos].materialCode.toString()
            )*/
            selectedLotsList.add(dispatchLocationLotsList[pos])
            updateStockWeight()
            dispatchLocationLotsList[pos].isProgress = true
            mAdapter.notifyItemChanged(pos)
            onGetQualityParams(
                dispatchLocationLotsList[pos].batchNumber.toString(),
                dispatchLocationLotsList[pos].materialCode.toString(),
                pos
            )

        } else {
            selectedLotsList.add(dispatchLocationLotsList[selectedPos])
            mAdapter.notifyItemChanged(pos)
            updateStockWeight()
        }

    }

    private fun moveUnChecked(pos: Int) {
        dispatchLocationLotsList[pos].isChecked = false
        selectedLotsList.remove(dispatchLocationLotsList[pos])
        mAdapter.notifyItemChanged(pos)
        updateStockWeight()
    }

    private fun updateStockWeight() {
        binding.tvStocksSum.text = getString(R.string.stocks_count).plus(selectedLotsList.size.toString())
        val weightSum = selectedLotsList.sumOf { it.weight?.toDouble() ?: 0.0 }
        binding.tvWeightSum.text = getString(R.string.weight).plus(" : ").plus(weightSum.toString())
    }

    fun onGetQualityParams(batchNumber: String, materialCode: String, pos: Int) {
        val input = workDataOf(BATCH_NUMBER to batchNumber, MATERIAL to materialCode)
        val worker = getDispatchQualityRequestWorker(input, pos)
        enQueueWorker(worker, this)
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer { workInfo ->

                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val position = posExtension(workInfo.tags)
                            mAdapter.setStatus(
                                position,
                                false,
                                workInfo.outputData.getString(REGION),
                                workInfo.outputData.getString(KOR)
                            )
                        }
                        WorkInfo.State.FAILED -> {
                            val position = posExtension(workInfo.tags)
                            mAdapter.setStatus(
                                position,
                                false,
                                "",
                                ""
                            )
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                        else -> {
                        }
                    }
                }

            })
    }

}
