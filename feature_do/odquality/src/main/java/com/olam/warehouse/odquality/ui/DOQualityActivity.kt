package com.olam.warehouse.odquality.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.dorigin.entity.DOQuality
import com.olam.warehouse.master.dorigin.entity.DOQualityParameter
import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails
import com.olam.warehouse.odquality.R
import com.olam.warehouse.odquality.data.domain.model.DOQualityPost
import com.olam.warehouse.odquality.data.domain.model.DOQualityPostResponse
import com.olam.warehouse.odquality.data.domain.model.QrBag
import com.olam.warehouse.odquality.di.injectQualityFeature
import com.olam.warehouse.odquality.ui.offline.DOQualityOfflineFragment
import com.olam.warehouse.odquality.ui.params.DOQualityParameterFragment
import com.olam.warehouse.odquality.ui.weighbridge.DOQualityWBListFragment
import com.olam.warehouse.odquality.ui.weighbridge.DOQualityWeighBridgeBagFragment
import com.olam.warehouse.odquality.utils.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.AppUtils.SUB_TITLE
import com.olam.warehouse.presentation.utils.AppUtils.TITLE
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
class DOQualityActivity : HomeBaseActivity(), DOQualityWBListFragment.OnWeighBridgeListener,
    DOQualityParameterFragment.OnParamsListener, DOQualityOfflineFragment.OnOfflineListener, DOQualityWBListFragment.CallBack, DOQualityWeighBridgeBagFragment.CallBack {

    private var wbDetails: DOQualityWBDetails? = null
    private var qualityPostList = arrayListOf<DOQualityWBDetails>()
    private val mTAG = DOQualityActivity::class.java.canonicalName
    private lateinit var fragment: Fragment
    private var isParamValue = false
    private var batchNo: String = ""
    private var avgBagWeight: String? = ""
    private var search: MenuItem? = null
    private var searchView: SearchView? = null

    private var menu: Menu? = null
    private val mSearchList: MutableList<DOQualityWBDetails> = mutableListOf()
    private var mQualityWBList: MutableList<DOQualityWBDetails> = mutableListOf()

    private val vm: DOQualityViewModel by viewModel()

    override val layoutResourceId = R.layout.activity_do_quality

    companion object {
        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectQualityFeature()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odquality/ui/blt/DOQualityActivity").title("OD/Quality")
            .with(tracker)
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        vm.quality.observe(this, Observer { updateUI(it) })
        if (intent?.hasExtra(QUALITY_OFFLINE)!!) {
            val bundle = Bundle().apply {
                putString(WEIGHBRIDGE_LIST, "")
            }
            displayFragment(QUALITY_OFFLINE_LIST, bundle, false)
        } else if (intent?.hasExtra(Constants.ISDIRECT)!!) {
            displayFragment(intent.getStringExtra(Constants.WEIGHBRIDGETYPE).toString(), null, false)
        }else {
            displayFragment(WEIGHBRIDGE_LIST, null, false)
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.flQuality)
        if (fragment is DOQualityParameterFragment || fragment is DOQualityOfflineFragment) {
            search?.isVisible = true
            searchView?.visible()
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.search_do_quality_menu, menu)
        search = menu.findItem(R.id.search)
        searchView = search?.actionView as SearchView?
        try {
            val fragmentSearch = supportFragmentManager.findFragmentById(R.id.flQuality)
            val search = menu.findItem(R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(
                com.olam.warehouse.odquality.utils.getColor(
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            searchView.queryHint = getString(R.string.search_wb_item)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            val fragment = fragmentSearch as DOQualityWBListFragment?
                            fragment?.updateAdapter(mQualityWBList)
                        } else {
                            val fragment = fragmentSearch as DOQualityWBListFragment?
                            mSearchList.clear()
                            mQualityWBList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.weighBridgeId.contains(text)) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            fragment?.updateAdapter(mSearchList)
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

    override fun setQualityWBList(it: List<DOQualityWBDetails>?) {
        this.mQualityWBList.clear()
        this.mQualityWBList = it?.toMutableList() ?: mutableListOf()
    }

    private fun displayFragment(openFragment: String, bundle: Bundle?, canReplace: Boolean) {
        when (openFragment) {
            WEIGHBRIDGE_LIST -> {
                search?.isVisible = true
                searchView?.visible()
                fragment = DOQualityWBListFragment.newInstance()
            }
            PARAMS_LIST -> {
                search?.isVisible = false
                searchView?.gone()
//                showLoading()
                fragment = DOQualityParameterFragment.newInstance()
            }
            QUALITY_OFFLINE_LIST -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = DOQualityOfflineFragment.newInstance()
            }
            CHOOSE_QR_FOR_SAMPLING -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = DOQualityWeighBridgeBagFragment.newInstance(bundle)
            }
            else -> fragment = DOQualityWBListFragment.newInstance()
        }
        bundle?.let { fragment.arguments = bundle }
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flQuality,
            allowBackStack = canReplace
        )
    }

    override fun onWeighBridgeClick(wbDetails: DOQualityWBDetails?) {
        this.wbDetails = wbDetails
        isParamValue = false
        batchNo = if (wbDetails?.batchNumber.isNullOrEmpty()) "" else wbDetails?.batchNumber.toString()
        avgBagWeight = wbDetails?.bagCount?.toDouble()?.let {
            wbDetails.netWeight?.toDouble()?.plus(wbDetails.bagWeight?.toDouble()!!)
                ?.div(it)
                ?.formatThreeDigits().toString()
        }
        val bundle = Bundle().apply {
            putString(WEIGHBRIDGE_LIST, wbDetails?.weighBridgeId)
            putBoolean(IS_PARAMS_VALUE, false)
            putString(BATCH_NO, batchNo)
            putString(NET_WEIGHT, wbDetails?.netWeight)
            putString(GROSS_WEIGHT, wbDetails?.grossWeight)
            if(wbDetails?.weighMethod.equals("WB")) {
                putString(TAR_WEIGHT, wbDetails?.tareWeight)
            } else {
                putString(TAR_WEIGHT, wbDetails?.bagWeight)
            }
            putString(MATERIAL_NO, wbDetails?.materialCode)
            putString(CHALLAN, wbDetails?.challan)
            putString(AVG_BAG_WEIGHT, avgBagWeight)
            putString(UOM, wbDetails?.unitsOfMeasure)
            putString(BATCH_NUMBER, wbDetails?.batchNumber)
            putString(PLANT_ID, wbDetails?.plant)
            putString(QC_STATUS, wbDetails?.qcStatus)
            putString(SOURCE_LOT_ID,wbDetails?.sourceLotId)
        }
        displayFragment(PARAMS_LIST, bundle, true)
    }

    override fun onParamsSave(qualityParameter: ArrayList<DOQualityParameter?>, wbId: String?, batchNo: String) {
        val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }
        if (isOnline()) {
            @Suppress("UNCHECKED_CAST")
            wbDetails?.qualityDetails = qtyParams as List<DOQuality>
            wbDetails?.batchNumber = batchNo
//            wbDetails?.qcStatus = "R"
            wbDetails?.qcStatus = ""
            qualityPostList.clear()
            wbDetails?.let {
                if (it.plant.isNullOrEmpty()) it.plant = getPlantDetails().plantId
                qualityPostList.add(it)
            }
            vm.postQualityParams(
                DOQualityPost(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    lotDetails = qualityPostList
                )
            )
        } else {
            qtyParams.forEach {
                it?.wbid = wbId.toString()
                vm.saveQualityData(prepareDOQualityData(it!!), batchNo)
            }
            moveToSuccessPage(wbId, "")
        }
    }

    override fun onParamsProceed(qualityParameter: ArrayList<DOQualityParameter?>, wbId: String?, batchNo: String) {

        val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }
        if (isOnline()) {
            @Suppress("UNCHECKED_CAST")
            wbDetails?.qualityDetails = qtyParams as List<DOQuality>
            wbDetails?.batchNumber = batchNo
//            wbDetails?.qcStatus = "X"
            wbDetails?.qcStatus = "A"
            qualityPostList.clear()
            wbDetails?.let {
                if (it.plant.isNullOrEmpty()) it.plant = getPlantDetails().plantId
                qualityPostList.add(it)
            }
            vm.postQualityParams(
                DOQualityPost(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    lotDetails = qualityPostList
                )
            )
        } else {
            qtyParams.forEach {
                it?.wbid = wbId.toString()
                vm.saveQualityData(prepareDOQualityData(it!!), batchNo)
            }
            moveToSuccessPage(wbId, "")
        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<DOQualityPostResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            moveToSuccessPage(it.data?.data?.currentWbid, it.data?.data?.charg)
                            vm.updateWBDB(it.data?.data?.currentWbid)
                        }
                        else -> showErrorDialogWithFAQLink(this,it.data?.message?:"")
                        //toast("${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(this,it.error.toString())
                   // toast("${it.error}")
                }
            }
        }
    }

    private fun moveToSuccessPage(currentWbid: String?, charg: String?) {
        val intent = Intent(this, SuccessActivity::class.java)
        if (isOnline()) intent.putExtra(TITLE, getString(R.string.quality_success)) else intent.putExtra(
            TITLE,
            getString(R.string.quality_success_offline)
        )
        if (charg?.isNotEmpty()!!)
            intent.putExtra(SUB_TITLE, getString(R.string.new_lot_id_created).plus(charg))
        else
            intent.putExtra(SUB_TITLE, getString(R.string.weigh_bridge_id).plus(currentWbid))
        startActivity(intent)
        finish()
    }

    override fun onQualityOfflineClick() {
        val bundle = Bundle().apply {
            putString(WEIGHBRIDGE_LIST, wbDetails?.weighBridgeId)
        }
        displayFragment(QUALITY_OFFLINE_LIST, bundle, true)
    }

    override fun onUpdateDeletedItem(wbid: String?) {
        vm.updateDeletedItem(wbid)
    }

    override fun onViewDetails(weighBridge: DOQualityWBDetails?) {
        this.wbDetails = weighBridge
        isParamValue = true
        batchNo = if (weighBridge?.batchNumber.isNullOrEmpty()) "" else weighBridge?.batchNumber.toString()
        avgBagWeight = weighBridge?.bagCount?.toDouble()?.let {
            weighBridge.netWeight?.toDouble()?.plus(weighBridge.bagWeight?.toDouble()!!)
                ?.div(it)
                ?.formatThreeDigits().toString()
        }
        val bundle = Bundle().apply {
            putString(WEIGHBRIDGE_LIST, weighBridge?.weighBridgeId)
            putBoolean(IS_PARAMS_VALUE, true)
            putString(BATCH_NO, batchNo)
            putString(NET_WEIGHT, weighBridge?.netWeight)
            putString(GROSS_WEIGHT, weighBridge?.grossWeight)
            if(weighBridge?.weighMethod.equals("WB")) {
                putString(TAR_WEIGHT, weighBridge?.tareWeight)
            } else {
                putString(TAR_WEIGHT, weighBridge?.bagWeight)
            }
            putString(MATERIAL_NO, weighBridge?.materialCode)
            putString(CHALLAN, weighBridge?.challan)
            putString(AVG_BAG_WEIGHT, avgBagWeight)
        }
        displayFragment(PARAMS_LIST, bundle, true)
    }

    override fun replaceFragment(
        moveFrag: String,
        wbDetails: DOQualityWBDetails?
    ) {
        val bundle = Bundle().apply {
            putParcelable(SELECTED_QUALITY, wbDetails)
        }

        search?.isVisible = false
        searchView?.gone()
        fragment = DOQualityWeighBridgeBagFragment.newInstance(bundle)
        bundle.let { fragment.arguments = bundle }
        if (moveFrag == CHOOSE_QR_FOR_SAMPLING) {
            replaceOtherFragment(fragment,
                mTAG,
                allowStateLoss = true,
                containerViewId = R.id.flQuality)
        }
//        displayFragment(CHOOSE_QR_FOR_SAMPLING, bundle, true)
    }

//    override fun replaceFragment(moveFrag: String, map: MutableMap<String, MutableSet<String>>) {
//        displayFragment(WEIGHBRIDGE_LIST, null, false)
//    }

    override fun replaceFragment(moveFrag: String, map: MutableMap<String, MutableSet<QrBag>>) {
        displayFragment(WEIGHBRIDGE_LIST, null, false)
    }
}
