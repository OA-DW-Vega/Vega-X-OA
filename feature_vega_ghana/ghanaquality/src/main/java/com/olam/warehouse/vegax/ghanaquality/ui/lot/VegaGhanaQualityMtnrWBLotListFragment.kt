package com.olam.warehouse.vegax.ghanaquality.ui.lot

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
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaQualityMtnBatch
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
import com.olam.warehouse.vegax.ghanaquality.R
import com.olam.warehouse.vegax.ghanaquality.data.domain.usecase.model.BatchNumResponse
import com.olam.warehouse.vegax.ghanaquality.data.domain.usecase.model.VegaGhanaQualityParamPost
import com.olam.warehouse.vegax.ghanaquality.databinding.FragmentVegaGhanaWeighbridgeLotBinding
import com.olam.warehouse.vegax.ghanaquality.databinding.ItemVegaGhanaLotCardLayoutBinding
import com.olam.warehouse.vegax.ghanaquality.ui.VegaGhanaQualityViewModel
import com.olam.warehouse.vegax.ghanaquality.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaGhanaQualityMtnrWBLotListFragment : BaseFragment() {

    private val vm: VegaGhanaQualityViewModel by viewModel()
    private lateinit var mListener: OnLotListener
    private lateinit var binding: FragmentVegaGhanaWeighbridgeLotBinding
    override val layoutResourceId = R.layout.fragment_vega_ghana_weighbridge_lot
    private var weightmentType: String? = ""
    private var copiedWbid: String = ""
    private var copiedMaterial: String = ""
    private val mSearchList: MutableList<VegaCoffeeLot> = mutableListOf()
    private var mQualityLotList: MutableList<VegaCoffeeLot> = mutableListOf()
    private var weighBridgeDetails = VegaQualityWBDetails()
    private var batchNo: String? = ""

    interface OnLotListener {
        fun onLotClick(lotDetails: VegaCoffeeLot?, lotList: MutableList<VegaCoffeeLot>)
        fun setQualityLotList(it: List<VegaCoffeeLot>?)
    }

    companion object {
        fun newInstance() = VegaGhanaQualityMtnrWBLotListFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = "Search Lot"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnLotListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaGhanaWeighbridgeLotBinding.inflate(layoutInflater)
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

    private fun initExtra() {
        arguments?.let {
            weightmentType = it.getString(WEIGHBRIDGE_LIST_TYPE)
            copiedWbid = it.getString(COPIED_WBID).toString()
            copiedMaterial = it.getString(COPIED_MATERIAL).toString()
            weighBridgeDetails = it.getParcelable(WEIGHSCALE)!!
        }
        binding.tvWeighBridgeIdValue.text = weighBridgeDetails.weighBridgeId
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        activity?.menuInflater?.inflate(R.menu.search_vega_ghana_quality_menu, menu)
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
//            vm.lotOnline.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
//            vm.getLotDetailOnline(weighBridgeDetails.weighBridgeId)
            vm.batch.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
            vm.getDeliveryBatchNumber(weighBridgeDetails.delivery!!, weighBridgeDetails.deliveryItem!!)
        } else {
            vm.offlineBatch.observe(viewLifecycleOwner, Observer { updateUIWithOfflineData(it) })
            vm.getOfflineDeliveryBatchNumber(weighBridgeDetails.delivery!!, weighBridgeDetails.deliveryItem!!)
        }
        binding.btCreateGrn.setOnClickListener { showConfirmDialog() }
        vm.qualityMtnr.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun updateUIWithOfflineData(offlineMtnBatch: VegaGhanaQualityMtnBatch?) {
        offlineMtnBatch?.let { it1 ->
            var data1 = listOf<VegaCoffeeLot>()
            data1 = prepareOfflineLotDetail(it1)
            /*data1 = if (!copiedMaterial.equals("null") && copiedMaterial.isNotEmpty()) it1.filter {
                it.materialCode.equals(copiedMaterial)
            } else it1*/
            if (data1.isNotEmpty()) {
                mListener.setQualityLotList(data1)
                mQualityLotList.clear()
                mQualityLotList = data1.reversed() as MutableList<VegaCoffeeLot>
                setAdapter(mQualityLotList)
                binding.tvNoLots.gone()
                binding.rvLots.visible()
            } else {
                binding.tvNoLots.visible()
                binding.rvLots.gone()
            }

        }!!
    }

    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<BatchNumResponse>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                val data = response.data?.data
                var weighTypedata: BatchNumResponse? = null
                weighTypedata = data
                weighTypedata?.let { it1 ->
                    var data1 = listOf<VegaCoffeeLot>()
                    data1 = prepareLotDetail(it1)
                    /*data1 = if (!copiedMaterial.equals("null") && copiedMaterial.isNotEmpty()) it1.filter {
                        it.materialCode.equals(copiedMaterial)
                    } else it1*/
                    if (data1.isNotEmpty()) {
                        mListener.setQualityLotList(data1)
                        mQualityLotList.clear()
                        mQualityLotList = data1.reversed() as MutableList<VegaCoffeeLot>
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

    private fun prepareOfflineLotDetail(it1: VegaGhanaQualityMtnBatch): List<VegaCoffeeLot> {
        var lotList = arrayListOf<VegaCoffeeLot>()
        var lot = VegaCoffeeLot()
        lot.item = weighBridgeDetails.item
        lot.delivery = weighBridgeDetails.delivery
        lot.customerNum = weighBridgeDetails.customerNum!!
        lot.purchaseDocNum = weighBridgeDetails.purchaseDocNum!!
        lot.purchaseDocDesc = weighBridgeDetails.purchaseDocDesc!!
        lot.batchNumber = it1.batchNumber!!
        batchNo = lot.batchNumber
        lot.receivedWeight = it1.deliveryQty!!
////            lot.sentWeight =
        lot.materialName = weighBridgeDetails.materialName
        lot.materialCode = weighBridgeDetails.materialCode
        lot.supplierName = weighBridgeDetails.supplierName
        lot.supplierCode = weighBridgeDetails.supplierCode
        lot.deliveryItem = weighBridgeDetails.deliveryItem
        lot.bagType = weighBridgeDetails.bagType
        lot.bagCount = weighBridgeDetails.bagCount
        lot.bagWeight = weighBridgeDetails.bagWeight
        lot.pmat2Count = "0"
        lot.pmat2Type = "0"
        lot.pmat2Weight = "0"
        lot.pmat3Count = "0"
        lot.pmat3Type = "0"
        lot.pmat3Weight = "0"
        lot.unitsOfMeasure = weighBridgeDetails.unitsOfMeasure
        lot.netWeight = weighBridgeDetails.netWeight
        lot.grossWeight = weighBridgeDetails.grossWeight
        lot.weighBridgeId = weighBridgeDetails.weighBridgeId
        lot.challan = weighBridgeDetails.challan
        lot.plant = weighBridgeDetails.plant
        lot.direction = weighBridgeDetails.direction
        lot.weighBridgeType = weighBridgeDetails.weighBridgeType
        lot.erdat = weighBridgeDetails.erdat
        lot.ertim = weighBridgeDetails.ertim
        lot.qcStatus = weighBridgeDetails.qcStatus
//            lot.bcApprover = weighBridgeDetails
        lot.vehicleNumber = weighBridgeDetails.vehicleNumber
        lot.storageLocationCode = weighBridgeDetails.storageLocationCode
        lot.storageLocation = weighBridgeDetails.storageLocation
        lot.transportVendorCode = weighBridgeDetails.transportVendorCode
//            lot.tareWeight = weighBridgeDetails.tareWeight
//            lot.batchPicking = weighBridgeDetails.
//            lot.vehicleType = weighBridgeDetails.ve
        lot.contactNumber = weighBridgeDetails.contactNumber
//            lot.driverNumber = weighBridgeDetails.driverNumber
//            lot.truckType = weighBridgeDetails.
//            lot.truckDirection = weighBridgeDetails.
//            lot.bagTareWeight = weighBridgeDetails.bagTareWeight
        lot.driverName = weighBridgeDetails.driverName
//            lot.batchWeight = weighBridgeDetails.batchWeight
        lot.weighMethod = weighBridgeDetails.weighMethod
        lot.grnNumber = weighBridgeDetails.grnNumber
//            lot.dstorageLocationName = weighBridgeDetails.dstorageLocationCode
//            lot.dstorageLocationCode = weighBridgeDetails.dstorageLocationName
        lot.finalApproval = weighBridgeDetails.finalApproval

        lotList.add(lot)
        return lotList
    }

    private fun prepareLotDetail(it1: BatchNumResponse): List<VegaCoffeeLot> {
        var lotList = arrayListOf<VegaCoffeeLot>()
        var lot = VegaCoffeeLot()
        lot.item = weighBridgeDetails.item
        lot.delivery = weighBridgeDetails.delivery
        lot.customerNum = weighBridgeDetails.customerNum!!
        lot.purchaseDocNum = weighBridgeDetails.purchaseDocNum!!
        lot.purchaseDocDesc = weighBridgeDetails.purchaseDocDesc!!
        lot.batchNumber = it1.batchNumber!!
        lot.receivedWeight = it1.deliveryQty!!
////            lot.sentWeight =
        lot.materialName = weighBridgeDetails.materialName
        lot.materialCode = weighBridgeDetails.materialCode
        lot.supplierName = weighBridgeDetails.supplierName
        lot.supplierCode = weighBridgeDetails.supplierCode
        lot.deliveryItem = weighBridgeDetails.deliveryItem
        lot.bagType = weighBridgeDetails.bagType
        lot.bagCount = weighBridgeDetails.bagCount
        lot.bagWeight = weighBridgeDetails.bagWeight
        lot.pmat2Count = "0"
        lot.pmat2Type = "0"
        lot.pmat2Weight = "0"
        lot.pmat3Count = "0"
        lot.pmat3Type = "0"
        lot.pmat3Weight = "0"
        lot.unitsOfMeasure = weighBridgeDetails.unitsOfMeasure
        lot.netWeight = weighBridgeDetails.netWeight
        lot.grossWeight = weighBridgeDetails.grossWeight
        lot.weighBridgeId = weighBridgeDetails.weighBridgeId
        lot.challan = weighBridgeDetails.challan
        lot.plant = weighBridgeDetails.plant
        lot.direction = weighBridgeDetails.direction
        lot.weighBridgeType = weighBridgeDetails.weighBridgeType
        lot.erdat = weighBridgeDetails.erdat
        lot.ertim = weighBridgeDetails.ertim
        lot.qcStatus = weighBridgeDetails.qcStatus
//            lot.bcApprover = weighBridgeDetails
        lot.vehicleNumber = weighBridgeDetails.vehicleNumber
        lot.storageLocationCode = weighBridgeDetails.storageLocationCode
        lot.storageLocation = weighBridgeDetails.storageLocation
        lot.transportVendorCode = weighBridgeDetails.transportVendorCode
//            lot.tareWeight = weighBridgeDetails.tareWeight
//            lot.batchPicking = weighBridgeDetails.
//            lot.vehicleType = weighBridgeDetails.ve
        lot.contactNumber = weighBridgeDetails.contactNumber
//            lot.driverNumber = weighBridgeDetails.driverNumber
//            lot.truckType = weighBridgeDetails.
//            lot.truckDirection = weighBridgeDetails.
//            lot.bagTareWeight = weighBridgeDetails.bagTareWeight
        lot.driverName = weighBridgeDetails.driverName
//            lot.batchWeight = weighBridgeDetails.batchWeight
        lot.weighMethod = weighBridgeDetails.weighMethod
        lot.grnNumber = weighBridgeDetails.grnNumber
//            lot.dstorageLocationName = weighBridgeDetails.dstorageLocationCode
//            lot.dstorageLocationCode = weighBridgeDetails.dstorageLocationName
        lot.finalApproval = weighBridgeDetails.finalApproval

        lotList.add(lot)
        return lotList
    }

    private fun setAdapter(mQualityLotList1: MutableList<VegaCoffeeLot>) {
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
            R.layout.item_vega_ghana_lot_card_layout,
            ItemVegaGhanaLotCardLayoutBinding::inflate,
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
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    postQuality()
                },
                { dismiss() })
        }
    }

    fun postQuality() {
            @Suppress("UNCHECKED_CAST")
            /* lotDetails.qualityDetails = qtyParams as List<VegaQuality>
             lotDetails.batchNumber = batchNo
             lotDetails.finalApproval = finalApproval*/
            mQualityLotList.forEach {
                it.qualityFlag = true
                it.item = "00001"
            }
        if (isOnline()) {
            vm.postQualityParamsMtnr(
                VegaGhanaQualityParamPost(
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
            var list = ArrayList<VegaCoffeeLot>()
            mQualityLotList.forEach {
                list.add(it)
            }
            saveOfflinePostRequest(list)
            moveToSuccessPage(mQualityLotList[0].weighBridgeId, "", "")
        }
    }

    private fun saveOfflinePostRequest(qualityPostList: ArrayList<VegaCoffeeLot>) {
        vm.savePostLotDetails(preparePostData(qualityPostList))
//        vm.saveVegaQualityWBDetails(mQualityLotList[0].weighBridgeId.toString(),batchNo.toString())
        vm.saveVegaQualityWeightWBDetails(
            mQualityLotList[0].weighBridgeId.toString(),
            batchNo.toString(),
            mQualityLotList[0].netWeight.toString()
        )

        /*qualityPostList.forEach {item ->
            saveData(item.qualityDetails as ArrayList<VegaQualityParameter?> ,item.weighBridgeId)
//            saveWB(item.weighBridgeId.toString(),item.batchNumber,"",1)
        }*/
    }

    fun saveData(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?
    ) {
        qualityParameter.forEachIndexed { index, it ->
            it?.wbid = wbId.toString()
            it?.position = index
            vm.saveQualityData(prepareVegaQualityData(it!!), batchNo.toString())
//            vm.saveMtnrQualityData(prepareVegaMtnrQualityData(it!!), batchNo.toString())
        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaGhanaQualityParamPost>>) {
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
                        }
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    fun saveWB(weighBrideId: String, batchNo: String, message: String, status: Int) {
        weighBridgeDetails.batchNumber = batchNo
        weighBridgeDetails.wbTempId = weighBrideId
        weighBridgeDetails.status = status
        weighBridgeDetails.finalApproval = FNQUALITY
        weighBridgeDetails.message = message
        weighBridgeDetails.let { vm.saveWBDB(it) }
    }

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
