package com.olam.warehouse.vegax.qualityapprovenigeria.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.vegax.qualityapprovenigeria.R
import com.olam.warehouse.vegax.qualityapprovenigeria.di.injectQualityApproveNigeriaFeature
import com.olam.warehouse.vegax.qualityapprovenigeria.ui.quality.VegaQualityApproveNigeriaFragment
import com.olam.warehouse.vegax.qualityapprovenigeria.ui.weighbridge.VegaQualityApproveNigeriaWeighbridgeListFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 1/21/2020.
 */

class VegaQualityApproveNigeriaActivity : HomeBaseActivity(), OnFragmentQualityApproveNigeriaInteractionListener,
    VegaQualityApproveNigeriaFragment.OnParamsListener {
    override fun onFragmentInteraction(fragment: Fragment) {
        displayFragment(fragment, true)
    }
    private var qualityParameter = ArrayList<VegaQualityParameter?>()
    private var finalApprovalStatus: String = ""
    private var wbDetails: VegaQualityWBDetails? = VegaQualityWBDetails()
    private var qualityPostList = arrayListOf<VegaQualityWBDetails>()
    private val vm: VegaQualityApproveNigeriaViewModel by viewModel()

    private val mTAG = VegaQualityApproveNigeriaActivity::class.java.canonicalName
    override val layoutResourceId: Int
        get() = R.layout.activity_vega_qualty_approve_nigeria //To change initializer of created properties use File | Settings | File Templates.


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectQualityApproveNigeriaFeature()
        initNavigationView()
        initUi()
    }

    private fun initUi() {
        displayFragment(VegaQualityApproveNigeriaWeighbridgeListFragment(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(fragment, mTAG, true, R.id.flApprove, allowBackStack = flag)
    }

    override fun onParamsProceed(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String
    ) {
        /*this.qualityParameter = qualityParameter
        finalApprovalStatus = finalApproval
        val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }
        println("Roshna => $qtyParams")
        if (AppUtils.isOnline()) {
            @Suppress("UNCHECKED_CAST")
            wbDetails?.qualityDetails = qtyParams as List<VegaQuality>
//            val filterData = thirdPartyMaterials?.filter { wbDetails?.materialCode == it.materialCode}
//            if (filterData?.isNotEmpty() == true) {
//                filterData[0].typeCode
//            }
//            var position4 = Calendar.getInstance().get(Calendar.YEAR).toString().takeLast(1)
            //wbDetails?.batchNumber = wbDetails?.plant?.takeLast(2).plus(1).plus(0).plus("S").plus("1").plus(rand(1,100))
            //wbDetails?.batchNumber = wbDetails?.plant?.takeLast(2).plus(wbDetails?.storageLocationCode?.takeLast(1)).plus(position4).plus(grnProcessType).plus(rand(1,100))

            wbDetails?.finalApproval = finalApproval
            qualityPostList.clear()
            wbDetails?.let { qualityPostList.add(it) }
             vm.postQualityParams(
                VegaNigeriaQualityApprovePost(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    lotDetails = qualityPostList
                )
            )
        } else {
            saveData(qualityParameter, wbId)
            moveToSuccessPage(wbId, "", "","")
        }*/
    }

    fun saveData(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?
    ) {
        qualityParameter.forEachIndexed { index, it ->
            it?.wbid = wbId.toString()
            it?.position = index
//            vm.saveQualityData(prepareVegaQualityApproveData(it!!), batchNo)
        }
    }



}
