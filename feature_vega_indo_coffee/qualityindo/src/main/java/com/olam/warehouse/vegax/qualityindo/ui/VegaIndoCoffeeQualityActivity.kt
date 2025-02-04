package com.olam.warehouse.vegax.qualityindo.ui

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.qualityindo.R
import com.olam.warehouse.vegax.qualityindo.di.injectIndoCoffeeQualityFeature
import com.olam.warehouse.vegax.qualityindo.ui.lot.VegaIndoCoffeeQualityWBLotListFragment
import com.olam.warehouse.vegax.qualityindo.ui.params.VegaIndoCoffeeQualityParameterFragment
import com.olam.warehouse.vegax.qualityindo.ui.transaction.VegaIndoCoffeeQualityTransactionFragment
import com.olam.warehouse.vegax.qualityindo.ui.weighbridge.VegaIndoCoffeeQualityWBListFragment
import com.olam.warehouse.vegax.qualityindo.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */
class VegaIndoCoffeeQualityActivity : HomeBaseActivity(),
    VegaIndoCoffeeQualityWBListFragment.OnWeighBridgeListener, VegaIndoCoffeeQualityWBLotListFragment.OnLotListener,
    VegaIndoCoffeeQualityParameterFragment.OnParamsListener,
    VegaIndoCoffeeQualityTypeFragment.CallBack, VegaIndoCoffeeQualityTransactionFragment.CallBack {
    private var wbDetails: VegaQualityWBDetails? = null
    private var lotDetails: VegaCoffeeLot? = null
    private var qualityPostList = arrayListOf<VegaCoffeeLot>()
    private val mTAG = VegaIndoCoffeeQualityActivity::class.java.canonicalName
    private lateinit var fragment: Fragment
    private var isParamValue = false
    private var batchNo: String = ""
    private var finalApprovalStatus: String = ""
    private var weightmentType: String? = ""
    private var menu: Menu? = null
    private val mSearchList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var mQualityWBList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var mQualityLotList: MutableList<VegaCoffeeLot> = mutableListOf()
    private var qualityParameter = ArrayList<VegaQualityParameter?>()

    private val vm: VegaIndoCoffeeQualityViewModel by viewModel()

    override val layoutResourceId = R.layout.activity_indo_coffee_quality

    companion object {
        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectIndoCoffeeQualityFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        if (intent.hasExtra(UIUtils.QUALITY_DATA)) {
            wbDetails = intent.getParcelableExtra(UIUtils.QUALITY_DATA)
            if (wbDetails?.isCopy!!) {
                val type = if (wbDetails?.weighBridgeType.equals(PROCURE)) SUPPLIER else MTNR
                val bundle = Bundle().apply {
                    putString(WEIGHBRIDGE_LIST_TYPE, type)
                    putString(COPIED_WBID, wbDetails?.weighBridgeId)
                    putString(COPIED_MATERIAL, wbDetails?.materialCode)
                }
                displayFragment(WEIGHBRIDGE_LIST, bundle, false)
            } else
                transactionWBDetails(wbDetails)
        } else {
            if (intent.hasExtra(UIUtils.TRANS_QUALITY))
                displayFragment(WEIGHBRIDGE_TRANS, Bundle(), false)
            else
                displayFragment(
                    WEIGHMENT_TYPE,
                    Bundle()/*.apply { putBoolean(UIUtils.MTNR, intent.getBooleanExtra(UIUtils.MTNR, false)) }*/,
                    false
                )
        }

    }

    override fun setQualityWBList(it: List<VegaQualityWBDetails>?) {
        this.mQualityWBList.clear()
        this.mQualityWBList = it?.toMutableList() ?: mutableListOf()
    }

    private fun displayFragment(openFragment: String, bundle: Bundle?, canReplace: Boolean) {
        val search = menu?.findItem(R.id.search)
        val searchView: SearchView? = search?.actionView as SearchView?
        when (openFragment) {
            WEIGHMENT_TYPE -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaIndoCoffeeQualityTypeFragment.newInstance()
            }
            WEIGHBRIDGE_LIST -> {
                search?.isVisible = true
                searchView?.visible()
                fragment = VegaIndoCoffeeQualityWBListFragment.newInstance()
            }
            LOT_LIST -> {
                search?.isVisible = true
                searchView?.visible()
                fragment = VegaIndoCoffeeQualityWBLotListFragment.newInstance()
            }
            PARAMS_LIST -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaIndoCoffeeQualityParameterFragment.newInstance()
            }
            QUALITY_OFFLINE_LIST -> {
            }
            WEIGHBRIDGE_TRANS -> {
                search?.isVisible = false
                searchView?.gone()
                fragment = VegaIndoCoffeeQualityTransactionFragment.newInstance()
            }
            else -> fragment = VegaIndoCoffeeQualityWBListFragment.newInstance()
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

    override fun onWeighBridgeClick(
        wbDetails: VegaQualityWBDetails?,
        copiedWbid: String,
        copiedMaterial: String
    ) {

        this.wbDetails = wbDetails
        weightmentType = wbDetails?.weighBridgeType
        when (weightmentType) {
            PROCURE -> {
                isParamValue = false
                batchNo = if (wbDetails?.batchNumber.isNullOrEmpty()) "" else wbDetails?.batchNumber.toString()
                val bundle = Bundle().apply {
                    /* putString(TRUCK_NO, wbDetails?.vehicleNumber)
                     putString(WEIGHBRIDGE_LIST, wbDetails?.weighBridgeId)
                     putString(BATCH_NO, batchNo)
                    putString(NET_WEIGHT, wbDetails?.netWeight)
                    putString(TAR_WEIGHT, wbDetails?.bagWeight)
                    putString(MATERIAL_NO, wbDetails?.materialCode)
                    putString(CHALLAN, wbDetails?.challan)
                    putString(ITEM, wbDetails?.item)
                    putString(WEIGHBRIDGE_LIST_TYPE, wbDetails?.weighBridgeType)*/
                    if (!copiedWbid.equals("null") && copiedWbid.isNotEmpty())
                        putBoolean(IS_PARAMS_VALUE, true)
                    else
                        putBoolean(IS_PARAMS_VALUE, false)
                    putString(COPIED_WBID, copiedWbid)
                    putString(COPIED_MATERIAL, copiedMaterial)
                    putParcelable(WEIGHBRIDGE, wbDetails)
                }

                displayFragment(PARAMS_LIST, bundle, true)
                // transactionWBDetails(wbDetails)
                //Log.d("success","success")
            }
            STO -> {
                isParamValue = false
                batchNo = if (wbDetails?.batchNumber.isNullOrEmpty()) "" else wbDetails?.batchNumber.toString()
                val bundle = Bundle().apply {
                    /* putString(TRUCK_NO, wbDetails?.vehicleNumber)
                     putString(WEIGHBRIDGE_LIST, wbDetails?.weighBridgeId)
                     putString(BATCH_NO, batchNo)
                    putString(NET_WEIGHT, wbDetails?.netWeight)
                    putString(TAR_WEIGHT, wbDetails?.bagWeight)
                    putString(MATERIAL_NO, wbDetails?.materialCode)
                    putString(CHALLAN, wbDetails?.challan)
                    putString(ITEM, wbDetails?.item)
                    putString(WEIGHBRIDGE_LIST_TYPE, wbDetails?.weighBridgeType)*/
                    if (!copiedWbid.equals("null") && copiedWbid.isNotEmpty())
                        putBoolean(IS_PARAMS_VALUE, true)
                    else
                        putBoolean(IS_PARAMS_VALUE, false)
                    putString(COPIED_WBID, copiedWbid)
                    putString(COPIED_MATERIAL, copiedMaterial)
                    putParcelable(WEIGHBRIDGE, wbDetails)
                }
                displayFragment(LOT_LIST, bundle, true)
            }
        }

    }

    override fun onLotClick(lotDetails: VegaCoffeeLot?, lotList: MutableList<VegaCoffeeLot>) {
        this.lotDetails = lotDetails
        isParamValue = false
        batchNo = if (wbDetails?.batchNumber.isNullOrEmpty()) "" else wbDetails?.batchNumber.toString()
        val lotItems = arrayListOf<VegaCoffeeLot>()
        lotItems.addAll(lotList)
        val bundle = Bundle().apply {
            putBoolean(IS_PARAMS_VALUE, lotDetails?.isOffline == true)
            putParcelable(LOT, lotDetails)
            putParcelableArrayList(LOT_LIST, lotItems)
            putParcelable(WEIGHBRIDGE, wbDetails)
        }
        displayFragment(PARAMS_LIST, bundle, true)
    }

    override fun setQualityLotList(it: List<VegaCoffeeLot>?) {
        this.mQualityLotList.clear()
        this.mQualityLotList = it?.toMutableList() ?: mutableListOf()
    }

    override fun onQualityOfflineClick() {
        val bundle = Bundle().apply {
            putString(WEIGHBRIDGE_LIST, wbDetails?.weighBridgeId)
        }
        displayFragment(QUALITY_OFFLINE_LIST, bundle, true)
    }

    fun transactionWBDetails(wbDetails: VegaQualityWBDetails?) {
        isParamValue = true
        batchNo = if (wbDetails?.batchNumber.isNullOrEmpty()) "" else wbDetails?.batchNumber.toString()
        val bundle = Bundle().apply {
            /*putString(TRUCK_NO, wbDetails?.vehicleNumber)
            putString(WEIGHBRIDGE_LIST, wbDetails?.weighBridgeId)*/
            putBoolean(IS_PARAMS_VALUE, true)
            putParcelable(WEIGHBRIDGE, wbDetails)
            /* putString(BATCH_NO, batchNo)
             putString(NET_WEIGHT, wbDetails?.netWeight)
             putString(TAR_WEIGHT, wbDetails?.bagWeight)
             putString(MATERIAL_NO, wbDetails?.materialCode)
             putString(CHALLAN, wbDetails?.challan)
             putString(ITEM, wbDetails?.item)
             putString(WEIGHBRIDGE_LIST_TYPE, wbDetails?.weighBridgeType)*/
        }
        displayFragment(PARAMS_LIST, bundle, false)
    }

    override fun replaceFragment(weightmentType: String) {
        if (intent?.hasExtra(QUALITY_OFFLINE)!!) {
            val bundle = Bundle().apply {
                putString(WEIGHBRIDGE_LIST, "")
                putString(WEIGHBRIDGE_LIST_TYPE, weightmentType)
            }
            displayFragment(QUALITY_OFFLINE_LIST, bundle, true)
        } else {
            val bundle = Bundle().apply {
                putString(WEIGHBRIDGE_LIST_TYPE, weightmentType)
            }
            displayFragment(WEIGHBRIDGE_LIST, bundle, true)
        }

    }

    override fun replaceFragment(moveFrag: String, receivingData: Any, vegaQualityWBDetails: Any) {
        wbDetails = vegaQualityWBDetails as VegaQualityWBDetails
        when (moveFrag) {
            SUPPLIER -> {
                batchNo = if (wbDetails?.batchNumber.isNullOrEmpty()) "" else wbDetails?.batchNumber.toString()
                val bundle = Bundle().apply {
                    putBoolean(IS_PARAMS_VALUE, true)
                    putString(COPIED_WBID, "")
                    putString(COPIED_MATERIAL, "")
                    putParcelable(WEIGHBRIDGE, wbDetails)
                }
                displayFragment(PARAMS_LIST, bundle, true)
            }
            MTNR -> {
                batchNo = if (wbDetails?.batchNumber.isNullOrEmpty()) "" else wbDetails?.batchNumber.toString()
                val bundle = Bundle().apply {
                    putBoolean(IS_PARAMS_VALUE, true)
                    putString(COPIED_WBID, "")
                    putString(COPIED_MATERIAL, "")
                    putParcelable(WEIGHBRIDGE, wbDetails)
                }
                displayFragment(LOT_LIST, bundle, true)
            }
        }
    }

}
