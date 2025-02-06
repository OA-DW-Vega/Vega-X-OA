package com.olam.warehouse.vegax.ghanaquality.ui.offline

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.quickpinaccess.createpin.pinlockview.ResourceUtils
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.master.vega.utils.prepareVegaQualityList
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ghanaquality.R
import com.olam.warehouse.vegax.ghanaquality.data.domain.usecase.model.VegaGhanaQualityParamPost
import com.olam.warehouse.vegax.ghanaquality.databinding.FragmentVegaGhanaMtnrQualityParamsBinding
import com.olam.warehouse.vegax.ghanaquality.ui.VegaGhanaQualityViewModel
import com.olam.warehouse.vegax.ghanaquality.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaGhanaMtnrQualityViewDetailsFragment : BaseFragment() {

    private var savedData: ArrayList<VegaQualityWithQualitative?> =
        ArrayList<VegaQualityWithQualitative?>()
    private var wbId: String? = ""
    private var lotbatchNo: String? = ""
    private var lotmaterialNo: String? = ""
    private var batchNo1: String? = ""
    private var batchNo: String? = ""
    private var isData: Boolean? = false
    private var materialNo: String? = ""
    private var netWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var itemValue: String? = ""
    private var wbType: String? = ""
    private var trucNo: String? = ""
    private var lotDetails = VegaCoffeeLot()
    private var weighBridgeDetails = VegaQualityWBDetails()
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var mAdapter = VegaGhanaMtnrOfflineViewQualityParamsAdapter { enableProceedBtn(it) }
    private var isSort: Boolean = false
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private val vm: VegaGhanaQualityViewModel by viewModel()
    private lateinit var mListener: OnParamsListener
    private var copiedWbid: String = ""
    private var copiedMaterial: String = ""
    private var lotItems = arrayListOf<VegaCoffeeLot>()
    private var qualityPostList = arrayListOf<VegaCoffeeLot>()
    private var averageBagWeight: Double = 0.0
    private var qualityMasterData= mutableListOf<VegaQualityParamsWithQualitative>()
    interface OnParamsListener
    companion object {
        fun newInstance() = VegaGhanaMtnrQualityViewDetailsFragment().putArgs {}
    }

    private lateinit var binding: FragmentVegaGhanaMtnrQualityParamsBinding
    override val layoutResourceId = R.layout.fragment_vega_ghana_mtnr_quality_params

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnParamsListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaGhanaMtnrQualityParamsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("quality/ui/params/VegaSesameMtnrQualityParameterFragment").title("Quality").with(tracker)
        initUI()
        initExtra()
    }

    private fun initUI() {
//        initExtra()
        binding.btnParamsProceed.text = "OK"
        binding.rvBagDetail.layoutManager = LinearLayoutManager(this.context)
        binding.rvBagDetail.adapter = mAdapter

/*        binding.rvBagDetail.edQualityValue.isEnabled = false
        binding.rvBagDetail.spItem.isEnabled = false*/
        binding.btnParamsProceed.setOnClickListener {
            activity?.onBackPressed()
//            proceeToPost(FNQUALITY, R.string.confirm_Quty_message)
        }
        binding.btnAccept.setOnClickListener { proceeToPost(FNQUALITY, R.string.confirm_Quty_message) }
        binding.btnReject.setOnClickListener { proceeToPost(FNREJECT, R.string.confirm_reject_message) }
        binding.tvFilter.setOnClickListener {
            if (!isSort) {
                mAdapter.upadteFilter(mAdapter.getItems())
                isSort = true
            }
        }

        vm.custonLocation.observe(
            viewLifecycleOwner,
            Observer {
                custonLocationList =
                    it.filter { !it.storageLocationType.equals("P") }.toMutableList()
            })
        vm.getCustomLocations()
        vm.qualityMtnr.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun initExtra() {
        arguments?.let {
            lotDetails = it.getParcelable(LOT)!!
            weighBridgeDetails = it.getParcelable(WEIGHSCALE)!!
            lotItems = it.getParcelableArrayList<VegaCoffeeLot>(LOT_LIST) ?: ArrayList()
            lotItems.forEach {
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
//            if(lotDetails.bagCount != "0"){
//                averageBagWeight = lotDetails.grossWeight?.toDouble()!!/ lotDetails.bagCount?.toDouble()!!
//            }

            trucNo = lotDetails.vehicleNumber
            wbId = lotDetails.weighBridgeId
            batchNo = lotDetails.batchNumber
            isData = it.getBoolean(IS_PARAMS_VALUE, false)
            materialNo = if(!lotDetails.materialCode?.length?.equals(18)!!)"000000".plus(lotDetails.materialCode) else lotDetails.materialCode
            netWeight = lotDetails.netWeight
            tarWeight = lotDetails.bagWeight
            challanNo = lotDetails.challan
            itemValue = lotDetails.item
            wbType = lotDetails.weighBridgeType
            lotbatchNo = lotDetails.batchNumber
            lotmaterialNo = lotDetails.materialCode
        }
        vm.offlinelist.observe(viewLifecycleOwner, Observer {
            savedData = it as ArrayList<VegaQualityWithQualitative?>
            if(savedData.size>0){
                /*preQualityList.forEach {item ->
                    savedData.forEach {
                        if(item.sapQCName == it?.quality?.nameChar)
                            item.satNam = it?.quality?.qualityParameterValue
                    }
//                    mAdapter.notifyDataSetChanged()
                }*/


                var item = ArrayList<VegaQualityParams>()

                savedData.forEach {
                    var param = VegaQualityParams()
                    param.sapQCName = it?.quality?.nameChar
                    param.satNam = it?.quality?.qualityParameterValue
                    item.add(param)
                }

                preQualityList = item
                materialNo?.let { vm.getQualityParams(materialNo!!, isData, wbId) }
            }
        })
        vm.getOfflineSavedQuality(wbId.toString(),materialNo.toString())
        binding.tvParamsWeighBID.text = trucNo

//        vm.preSamplingOfflineQuality.observe(viewLifecycleOwner, Observer { updateOfflinePreQuality(it) })

//        vm.qualityMtnrlist.observe(viewLifecycleOwner, Observer { updateQuality(it) })
//        vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })

        if (isData!!) {
            materialNo?.let {
                if (!copiedWbid.equals("null") && copiedWbid.isNotEmpty())
                    vm.getMtnrQualityParams(copiedMaterial, isData, copiedWbid)
                else
                    vm.getMtnrQualityParams(materialNo!!, isData, wbId)
            }
        }
        else
        {
//            if(AppUtils.isOnline())
//                vm.getPreSamplingQualitydata(lotbatchNo?.trim()!!, lotmaterialNo?.trim()!!)
//            else
//                vm.getOfflinePreSamplingQualitydata(lotbatchNo?.trim()!!, lotmaterialNo?.trim()!!)
//            vm.getOfflineSavedQuality(wbId.toString(),materialNo.toString())

        }
        vm.qualitylist.observe(viewLifecycleOwner, Observer {
            qualityMasterData = it.filter {
                it.qualityParameter.materialCode == materialNo }.toMutableList()
            updateUI(it)
        })

        binding.etBatchNo.setText(batchNo, TextView.BufferType.EDITABLE)
        binding.etBatchNo.isEnabled = batchNo.isNullOrEmpty()
        if (wbType.equals(PROCURE)) {
            binding.btnReject.visible()
            binding.btnAccept.visible()
            binding.btnParamsProceed.gone()
            binding.tvType.text = SUPPLIER
        } else {
            binding.btnReject.gone()
            binding.btnAccept.gone()
            binding.btnParamsProceed.visible()
            binding.tvType.text = MTNR
        }
        enableProceedBtn(weighBridgeDetails.status)

    }

//    private fun updateQuality(it: List<VegaQualityParamsWithQualitative>?) {
//            var qualitySaved = it
//    }

    private fun enableProceedBtn(status: Int?) {

        when (status) {
            4 -> {
                binding.btnParamsProceed.isEnabled = false
                binding.btnAccept.isEnabled = false
                binding.btnReject.isEnabled = false
                ViewCompat.setBackgroundTintList(
                    binding.btnParamsProceed,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.grey) }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnAccept,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.grey) }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnReject,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.grey) }
                )
            }
            else -> {
                binding.btnParamsProceed.isEnabled = true
                binding.btnAccept.isEnabled = true
                binding.btnReject.isEnabled = true
                ViewCompat.setBackgroundTintList(
                    binding.btnParamsProceed,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                        )
                    }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnAccept,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                        )
                    }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnReject,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.red) }
                )
            }
        }
    }

    private fun proceeToPost(finalApproval: String, msg: Int) {
        var isValueNeed = true
        batchNo1 = binding.etBatchNo.text.toString()
        qualityParameterList.clear()
        val missedPos = mutableListOf<Int>()
        var data = arrayListOf<VegaQualityParamsWithQualitative?>()
        data = mAdapter.getItems()
        mAdapter.getItems().forEachIndexed { index, itValue ->
            if ((itValue?.qualityParameter?.entryObligatory.equals("X")) || (itValue?.qualityParameter?.vegaMandatory.equals(
                    "X"
                )) || (!itValue?.qualityParameter?.preSampling.equals(""))
            ) {
                if((itValue?.qualityParameter?.vegaValueMandatory.equals("X"))){
                    if (itValue?.qualityParameter?.qualityParameterValue.isNullOrEmpty()) {
                        if (itValue?.qualityParameter?.formulaParam.equals("X")) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else {
                            isValueNeed = false
                            missedPos.add(index)
                            itValue?.qualityParameter?.mandatory = 1
                        }
                    }
                }
                else {
                    itValue?.qualityParameter?.mandatory = 0
                }
                qualityParameterList.add(itValue?.qualityParameter)
            }
        }
        if (isValueNeed) {
            qualityParameterList.forEach { quality ->
                qualityMasterData.forEach {
                    if (!it.qualitative.isNullOrEmpty()) {
                        it.qualitative?.forEach { item ->
                            if (quality?.nameChar == item.nameChar && quality.qualityParameterValue == item.descValue) {
                                quality.qualityParameterValue = item.charValue
                            }
                        }
                    }
                }
            }
            showConfirmDialog(batchNo1!!, msg, finalApproval)
        }
        else {
            showSnack(requireContext().resources.getString(R.string.enter_all_mandatory_fields))
            mAdapter.updateMissedPos(missedPos, data)
        }
    }



    private fun updateUI(params: List<VegaQualityParamsWithQualitative>?) {
        params?.let {
            when (it.isNotEmpty()) {
                true -> {
                    it.forEach {item ->
                        if (item.qualityParameter.nameChar == "CI_SLOC_KOR") {
                            item.qualitative =
                                prepareQualitative(custonLocationList, item.qualityParameter.materialCode)
                        }
                    }
                    val value= mutableListOf<VegaQualityParamsWithQualitative>()
                    if (!isData!!) {
                        it.forEach { item ->
                            if (item.qualityParameter.vegaMandatory.equals("X") || item.qualityParameter.entryObligatory.equals(
                                    "X"
                                )|| (!item.qualityParameter.preSampling.equals("")))
                                // && item.qualityParameter.qualityParameterValue?.isNotEmpty()!!
                            {
                                println("Roshna => isdata => ")
                                //((!item.qualityParameter.nameChar.equals("GH_CASHEW_AVG_BAG_WT")) &&
                                if(  (!item.qualityParameter.nameChar.equals("DO_TRANSACTION_NO")))
                                    value.add(item)
                            }
                            preQualityList.forEach { item1 ->
                                if (item.qualityParameter.nameChar == item1.sapQCName && (!item.qualityParameter.nameChar.equals("DO_TRANSACTION_NO"))) {
                                        item.qualityParameter.qualityParameterValue = item1.satNam!!.split(" ")[0]
                                        value.add(item)
                                    }
                            }

                        }
                    } else {
                        it.forEach { item ->
                            //!item.qualityParameter.nameChar.equals("GH_CASHEW_AVG_BAG_WT") &&
                            if( (!item.qualityParameter.nameChar.equals("DO_TRANSACTION_NO")))
                                value.add(item)
                        }
//                        value.addAll(it)
                    }

                    mAdapter.addItems(
                        sortByListOfItems(value.distinctBy { it.qualityParameter.nameChar }),
                        tarWeight,
                        netWeight,
                        challanNo
                    )

                    if (lotDetails.bagCount != "0") {
                        binding.edAvgBagWgtValue.isEnabled = false
                        binding.edAvgBagWgtValue.isFocusable = false
                        binding.edAvgBagWgtValue.setText(averageBagWeight.formatThreeDigits())
                    } else {
                        binding.edAvgBagWgtValue.isEnabled = true
                        binding.edAvgBagWgtValue.isFocusable = true
                        binding.llAvgBagWgt.background = ResourceUtils.getDrawable(
                            context,
                            com.olam.warehouse.presentation.R.drawable.do_custom_edit_text
                        )
                    }

//                    binding.llavgBagWgt.visibility = View.VISIBLE

                }
                else -> setErrorContentView("Quality params not available for this material")
            }
        }
    }

  /*  private fun updateOfflinePreQuality(response: List<VegaGhanaLotQualityDetails>) {

        var offlineData = response.filter{
            it.materialNumber == materialNo
            it.charg == lotDetails.batchNumber
        }
        var item = ArrayList<VegaQualityParams>()

        offlineData.forEach {
            var param = VegaQualityParams()
            param.sapQCName = it.sapQCName
            param.satNam = it.satNam
            item.add(param)
        }

        preQualityList = item

        materialNo?.let { vm.getQualityParams(materialNo!!, isData, wbId) }
    }*/

//    private fun updatePreQuality(response: Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>) {
//        response.let {
//            when (it.status) {
//                Resource.Status.SUCCESS -> {
//                    hideLoading()
//                    when (it.data?.success) {
//                        true -> {
//                            preQualityList = it.data?.data?.get(0)?.qualityParameters as MutableList<VegaQualityParams>
//                            materialNo?.let { vm.getQualityParams(materialNo!!, isData, wbId) }
//                        }
//                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
//                    }
//                }
//                Resource.Status.LOADING -> showLoading()
//                Resource.Status.ERROR -> {
//                    hideLoading()
//                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
//                }
//            }
//        }
//    }

    private fun showConfirmDialog(batchNo: String, msg: Int, finalApproval: String) {
        MaterialDialog(requireContext()).show {
            message(msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    /*qualityParameterList.clear()
                  mAdapter.getItems().forEach { qualityParameterList.add(it?.qualityParameter) }*/
                    var quality = VegaQualityParameter()
                    quality.nameChar = "GH_CASHEW_AVG_BAG_WT"
                    quality.descrChar = "Avg Bag Wt."
                    quality.unitText = "MT"
                    quality.unitsOfMeasure = "MT"
                    quality.qualityParameterValue = binding.edAvgBagWgtValue.text.toString()
                    qualityParameterList.add(quality)

                    postQuality(qualityParameterList, wbId, batchNo, finalApproval)
                },
                { dismiss() })
        }
    }

    private fun enableProceedBtn(item: List<VegaQualityParamsWithQualitative?>) {
        var isEnable = false
        item.forEach {
            it?.qualityParameter?.qualityParameterValue?.let { it1 -> if (it1.isNotEmpty()) isEnable = true }
        }
        if (isEnable) {
            binding.btnParamsProceed.isEnabled = true
            binding.btnParamsProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btnParamsProceed.isEnabled = false
            binding.btnParamsProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }
    }

    fun postQuality(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String
    ) {
        val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }
        val lotCoutPost = lotItems.filter { it.batchNumber.equals(lotDetails.batchNumber) }
        val qcDoneLot = lotItems.filter { it.qcStatus.equals("X") }
        val isApplicableGrn = lotItems.size - 1 == qcDoneLot.size
        lotDetails.qualityDetails = prepareVegaQualityList(qtyParams, mAdapter.getItems())
        lotDetails.batchNumber = batchNo
        lotDetails.finalApproval = finalApproval
        qualityPostList.clear()
        if (isApplicableGrn) {
            lotItems.forEach {
                if (it.batchNumber.equals(lotDetails.batchNumber)) it.qualityFlag = false
                it.item = "00001"
//                    it.materialCode = "000000".plus(it.materialCode)
            }
            lotDetails.let { qualityPostList.addAll(lotItems) }
        } else {
            lotCoutPost.forEach {
                it.qualityFlag = false
                it.item = "00001"
//                    it.materialCode = "000000".plus(it.materialCode)
            }
            lotDetails.let { qualityPostList.addAll(lotCoutPost) }
        }
        if (AppUtils.isOnline()) {
            vm.postQualityParamsMtnr(
                VegaGhanaQualityParamPost(
                    grnApplicable = isApplicableGrn,
                    grnFlag = false,
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    lotDetails = qualityPostList,
                    bcMessage = "",
                    charg = "",
                    currentWbid = "",
                    errorMessage = "",
                    grnNumber = ""
                )
            )
        } else {
            saveOfflinePostRequest(qualityPostList)
            moveToSuccessPage( "", "")
        }
    }

    private fun saveOfflinePostRequest(qualityPostList: ArrayList<VegaCoffeeLot>) {
        vm.savePostLotDetails(preparePostData(qualityPostList))
        qualityPostList.forEach {item ->
            saveData(item.qualityDetails as ArrayList<VegaQualityParameter?> ,item.weighBridgeId)
//            saveWB(item.weighBridgeId.toString(),item.batchNumber,"",1)
        }
    }

    fun saveData(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?
    ) {
        qualityParameter.forEachIndexed { index, it ->
            it?.wbid = wbId.toString()
            it?.position = index
            vm.saveQualityData(prepareVegaQualityData(it!!),batchNo.toString())
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
                                            lotItems.forEach { it2 ->
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

    private fun moveToSuccessPage(charg: String?, grnNo: String?) {
        val intent = Intent(activity, SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.quality_success))
        if (!grnNo.isNullOrEmpty())
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.new_lot_id_created_mtnr).plus(lotDetails.batchNumber).plus("\n GRN No : ").plus(grnNo)
            )
        else if (grnNo.isNullOrEmpty())
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.new_lot_id_created_mtnr).plus(lotDetails.batchNumber)
            )
        else
            intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id).plus(lotDetails.weighBridgeId))

        val lotlist = ArrayList<VegaCoffeeSalesLots>()
        lotlist.add(
            VegaCoffeeSalesLots(
                "",
                charg.toString(),
                weighBridgeDetails.materialCode.toString(),
                weighBridgeDetails.materialName.toString(),
                "",
                "",
                "",
                "",
                "",
                weighBridgeDetails.unitsOfMeasure,
                "",
                weighBridgeDetails.netWeight
            )
        )
//        intent.putExtra("fromcoffee", true)
//        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotlist)
//        intent.putExtra(AppUtils.PRINT_ENABLE, true)
        startActivity(intent)
        activity?.finish()
    }
}

