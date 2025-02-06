package com.olam.warehouse.vegax.qualityindo.ui.lot

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityindo.R
import com.olam.warehouse.vegax.qualityindo.data.domain.model.VegaIndoCoffeeQualityParamPost
import com.olam.warehouse.vegax.qualityindo.databinding.FragmentIndoCoffeeWeighbridgeLotBinding
import com.olam.warehouse.vegax.qualityindo.databinding.ItemIndoCoffeeLotCardLayoutBinding
import com.olam.warehouse.vegax.qualityindo.ui.VegaIndoCoffeeQualityViewModel
import com.olam.warehouse.vegax.qualityindo.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/16/2021.
 */
class VegaIndoCoffeeQualityWBLotListFragment : BaseFragment() {

    private val vm: VegaIndoCoffeeQualityViewModel by viewModel()
    private lateinit var mListener: OnLotListener
    private lateinit var binding: FragmentIndoCoffeeWeighbridgeLotBinding
    override val layoutResourceId = R.layout.fragment_indo_coffee_weighbridge_lot
    private var weightmentType: String? = ""
    private var copiedWbid: String = ""
    private var copiedMaterial: String = ""
    private val mSearchList: MutableList<VegaCoffeeLot> = mutableListOf()
    private var mQualityLotList: MutableList<VegaCoffeeLot> = mutableListOf()
    private var mOfflineQualityLotList: MutableList<VegaCoffeeLot> = mutableListOf()
    private var weighBridgeDetails = VegaQualityWBDetails()

    interface OnLotListener {
        fun onLotClick(lotDetails: VegaCoffeeLot?, lotList: MutableList<VegaCoffeeLot>)
        fun setQualityLotList(it: List<VegaCoffeeLot>?)
    }

    companion object {
        fun newInstance() = VegaIndoCoffeeQualityWBLotListFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = "Search Lot"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnLotListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentIndoCoffeeWeighbridgeLotBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initExtra()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("quality/ui/weighbridge/VegaCoffeeQualityWBListFragment - $weightmentType")
            .title("Quality").with(tracker)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        activity?.menuInflater?.inflate(R.menu.search_vega_quality_indo_coffee_menu, menu)
        //search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
        //searchView = search?.actionView as SearchView?
        try {
            val search = menu.findItem(R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint = SEARCH_HINT_TEXT
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setAdapter(mQualityLotList)
                        } else {
                            mSearchList.clear()
                            mQualityLotList.forEach { qtyLot ->
                                newText?.let { text ->
                                    if (qtyLot.batchNumber.contains(text)) {
                                        mSearchList.add(qtyLot)
                                    }
                                }
                            }
                            setAdapter(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun initUI() {
        if (isOnline()) {
            vm.lotOnline.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
            //vm.lotOnlineWB.observe(viewLifecycleOwner, Observer { updateUIWithOnlineDataWB(it) })
            /*if (weighBridgeDetails.weighMethod == "WB") {
                vm.getLotDetailOnlineWB(weighBridgeDetails.weighBridgeId, weighBridgeDetails.weighMethod == "WB")
            } else*/
            //vm.getLotDetailOnline(weighBridgeDetails.weighBridgeId, weighBridgeDetails.weighMethod == "WB")
        } else {
            // vm.getLotDetailOfflineLocal(weighBridgeDetails.weighBridgeId)
        }
        vm.lotOfflineLocal.observe(viewLifecycleOwner, Observer { updateUIWithOfflineLocalData(it) })
        vm.lotOfflineQtyLocal.observe(viewLifecycleOwner, Observer { updateUIWithOfflineQtyLocalData(it) })
        vm.getLotDetailOfflineQtyLocal(weighBridgeDetails.weighBridgeId)
        binding.btCreateGrn.setOnClickListener { showConfirmDialog() }
        vm.quality.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun initExtra() {
        arguments?.let {
            weightmentType = it.getString(WEIGHBRIDGE_LIST_TYPE)
            copiedWbid = it.getString(COPIED_WBID).toString()
            copiedMaterial = it.getString(COPIED_MATERIAL).toString()
            weighBridgeDetails = it.getParcelable(WEIGHBRIDGE)!!
        }

        binding.tvWeighBridgeIdValue.text = weighBridgeDetails.weighBridgeId
    }

    private fun updateUIWithOfflineQtyLocalData(it: List<VegaCoffeeLot>?) {
        var weighTypedata: List<VegaCoffeeLot>? = null
        weighTypedata = it
        weighTypedata?.let { it1 ->
            var data1 = listOf<VegaCoffeeLot>()
            data1 = it1
            if (data1.isNotEmpty()) {
                mOfflineQualityLotList = data1 as MutableList<VegaCoffeeLot>
                mQualityLotList = data1
            }
        }
        vm.getLotDetailOfflineLocal(weighBridgeDetails.weighBridgeId)
    }

    private fun updateUIWithOfflineLocalData(it: List<VegaCoffeeReceiveLots>?) {
        var weighTypedata: List<VegaCoffeeLot>? = null
        weighTypedata = prepareLotList(it)
        weighTypedata.let { it1 ->
            var data1 = listOf<VegaCoffeeLot>()
            data1 = it1
            if (data1.isNotEmpty()) {
                mOfflineQualityLotList.addAll(data1.reversed() as MutableList<VegaCoffeeLot>)
                mQualityLotList.addAll(data1.reversed() as MutableList<VegaCoffeeLot>)
                mListener.setQualityLotList(mQualityLotList)
                binding.tvNoLots.gone()
                binding.rvLots.visible()
            } else {
                binding.tvNoLots.visible()
                binding.rvLots.gone()
            }
        }
        if (!isOnline()) setAdapter(mQualityLotList)
        if (isOnline()) vm.getLotDetailOnline(
            weighBridgeDetails.weighBridgeId,
            weighBridgeDetails.weighMethod == "WB"
        )
    }

    /* private fun updateUIWithOnlineDataWB(response: Resource<GenericReqAndResp<VegaCoffeeLot>>) {
         when (response.status) {
             Resource.Status.SUCCESS -> {
                 hideLoading()
                 val data = response.data?.data
                 var weighTypedata: VegaCoffeeLot? = null
                 weighTypedata = data
                 weighTypedata?.let { it1 ->
                     val data1 = ArrayList<VegaCoffeeLot>()
                     data1.add(it1)
                     *//*data1 = if (!copiedMaterial.equals("null") && copiedMaterial.isNotEmpty()) it1.filter {
                        it.materialCode.equals(copiedMaterial)
                    } else it1*//*
                    if (data1.isNotEmpty()) {
                        mListener.setQualityLotList(data1)
                        mQualityLotList.clear()
                        mQualityLotList = data1.reversed() as MutableList<VegaCoffeeLot>
//                        mAdapter.addItems(data1)
                        setAdapter(mQualityLotList)
                        binding.tvNoLots.gone()
                        binding.rvLots.visible()
                    } else {
                        binding.tvNoLots.visible()
                        binding.rvLots.gone()
                    }

                }!!
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }*/


    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaCoffeeLot>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                val data = response.data?.data
                var weighTypedata: List<VegaCoffeeLot>? = null
                weighTypedata = data
                weighTypedata?.let { it1 ->
                    var data1 = listOf<VegaCoffeeLot>()
                    data1 = it1
                    /*data1 = if (!copiedMaterial.equals("null") && copiedMaterial.isNotEmpty()) it1.filter {
                        it.materialCode.equals(copiedMaterial)
                    } else it1*/
                    if (data1.isNotEmpty()) {
                        mListener.setQualityLotList(data1)
                        mQualityLotList.clear()
                        mQualityLotList = data1.reversed() as MutableList<VegaCoffeeLot>
//                        mAdapter.addItems(data1)
                        mOfflineQualityLotList.filter { it.isOffline == true }.forEach { item ->
                            mQualityLotList.single { it.batchNumber.equals(item.batchNumber) }
                                .apply {
                                    isOffline = true
                                    qualityFlag = false
                                }
                        }
                        setAdapter(mQualityLotList)
                        binding.tvNoLots.gone()
                        binding.rvLots.visible()
                    } else {
                        binding.tvNoLots.visible()
                        binding.rvLots.gone()
                    }

                }!!
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun setAdapter(mQualityLotList1: MutableList<VegaCoffeeLot>) {
//        val data = mQualityLotList1.filter { it.qcStatus.isNullOrEmpty() }.toMutableList()
        mQualityLotList.forEach {
            it.bagCount = it.bagCount.toString().trim()
            it.bagType = it.bagType.toString().trim()
            it.bagWeight = it.bagWeight.toString().trim()
            it.pmat2Count = it.pmat2Count.toString().trim()
            it.pmat2Type = it.pmat2Type.toString().trim()
            it.pmat2Weight = it.pmat2Weight.toString().trim()
            it.pmat3Count = it.pmat3Count.toString().trim()
            it.pmat3Type = it.pmat3Type.toString().trim()
            it.pmat3Weight = it.pmat3Weight.toString().trim()
            it.netWeight = it.netWeight.toString().trim()
            it.grossWeight = it.grossWeight.toString().trim()
            it.bagTareWeight = it.bagTareWeight.toString().trim()
            it.storageLocation = it.storageLocationCode.toString().trim()
            it.weighBridgeType =
                if (it.weighBridgeType.isNullOrEmpty()) weighBridgeDetails.weighBridgeType else it.weighBridgeType
        }
        val isCreateGrn = mQualityLotList.any { it.qcStatus.isNullOrEmpty() }
        if (isCreateGrn) binding.btCreateGrn.gone() else binding.btCreateGrn.visible()
        if (mQualityLotList1.size > 0) {
            binding.tvNoLots.gone()
            binding.rvLots.visible()
        } else {
            binding.rvLots.gone()
            binding.tvNoLots.visible()
        }
        binding.rvLots.setUpAdapter(
            mQualityLotList1,
            R.layout.item_indo_coffee_lot_card_layout,
            ItemIndoCoffeeLotCardLayoutBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvScaleLotValue.text = it.batchNumber
                bindItem.tvScaleWeightValue.text =
                    it.netWeight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitsOfMeasure)
                bindItem.tvStLocationValue.text = it.storageLocationCode
                bindItem.tvScaleGradeValue.text = it.materialName
                if (it.qcStatus.isNullOrEmpty()) {
                    bindItem.tvQcStatusValue.text = getString(R.string.qc_not_done)
                    bindItem.tvQcStatusValue.setTextColor(
                        ContextCompat.getColor(
                            bindItem.tvQcStatusValue.context,
                            com.olam.warehouse.presentation.R.color.black
                        )
                    )
                } else {
                    bindItem.tvQcStatusValue.text = getString(R.string.qc_done)
                    bindItem.tvQcStatusValue.setTextColor(
                        ContextCompat.getColor(
                            bindItem.tvQcStatusValue.context,
                            com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                        )
                    )
                }
            },
            {
                val lot = this
                if (lot.qcStatus.isNullOrEmpty()) {
                    lot.vehicleNumber = weighBridgeDetails.vehicleNumber
                    mListener.onLotClick(this, mQualityLotList)
                }
            })
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_Grn_message)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    postQuality()
                },
                { dismiss() })
        }
    }

    fun postQuality() {
        //val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }
        if (isOnline()) {
            @Suppress("UNCHECKED_CAST")
            /* lotDetails.qualityDetails = qtyParams as List<VegaQuality>
             lotDetails.batchNumber = batchNo
             lotDetails.finalApproval = finalApproval*/
            mQualityLotList.forEach { it.qualityFlag = true }

            vm.postQualityParams(
                VegaIndoCoffeeQualityParamPost(
                    grnApplicable = true,
                    grnFlag = false,
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    lotDetails = mQualityLotList,
                    bcMessage = "",
                    charg = "",
                    currentWbid = "",
                    errorMessage = "",
                    grnNumber = ""
                )
            )
        } else {
            //saveData(qualityParameter, wbId)
            //moveToSuccessPage(this.wbId, "", "")
        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaIndoCoffeeQualityParamPost>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            moveToSuccessPage(
                                it.data?.data?.currentWbid,
                                it.data?.data?.charg,
                                it.data?.data?.grnNumber
                            )
                            val batch = it.data?.data?.charg
                            val msg = it.data?.message
                            //saveWB(it.data?.data?.currentWbid.toString(), batch.toString(), msg.toString(), 4)
                            //saveData(qualityParameterList, it.data?.data?.currentWbid)
                        }
                        else -> {
                            showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                            it.data?.data?.let {
                                it.lotDetails?.let {
                                    it.forEach {
                                        it.let { it1 ->
                                            mQualityLotList.forEach { it2 ->
                                                if (it.batchNumber.equals(it1.batchNumber)) it2.qualityFlag =
                                                    it1.qualityFlag
                                            }
                                        }
                                    }
                                }
                            }
                            /*saveWB(
                                weighBridgeDetails.weighBridgeId.toString(),
                                batchNo.toString(),
                                it.data?.message.toString(),
                                3
                            )
                            saveData(qualityParameterList, weighBridgeDetails.weighBridgeId)*/
                        }
                        //toast("${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")

                    //saveWB(weighBridgeDetails.weighBridgeId.toString(), batchNo.toString(), it.error.toString(), 3)
                    //saveData(qualityParameterList, weighBridgeDetails.weighBridgeId)
                    //toast("${it.error}")
                }
            }
        }
    }

    /*fun saveWB(weighBrideId: String, batchNo: String, message: String, status: Int) {
        weighBridgeDetails.batchNumber = batchNo
        weighBridgeDetails.wbTempId = weighBrideId
        weighBridgeDetails.status = status
        weighBridgeDetails.finalApproval = FNQUALITY
        weighBridgeDetails.message = message
        weighBridgeDetails.let { vm.saveWBDB(it) }
    }*/

    private fun moveToSuccessPage(currentWbid: String?, charg: String?, grnNo: String?) {
        val lotIds = mQualityLotList.map { it.batchNumber }
        val lotItems = lotIds.toString().replace("[", "").replace("]", "")
        val intent = Intent(activity, SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.quality_success))
        if (!grnNo.isNullOrEmpty())
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.new_lot_id_created).plus(lotItems).plus("\n GRN No : ").plus(grnNo)
            )
        else if (grnNo.isNullOrEmpty())
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.new_lot_id_created).plus(lotItems)
            )
        else
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.weigh_bridge_id).plus(mQualityLotList[0].weighBridgeId)
            )
        startActivity(intent)
        activity?.finish()
    }
}
