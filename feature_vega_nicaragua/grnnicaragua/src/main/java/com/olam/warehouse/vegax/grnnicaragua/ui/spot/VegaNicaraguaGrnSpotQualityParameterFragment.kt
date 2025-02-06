package com.olam.warehouse.vegax.grnnicaragua.ui.spot

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.GrnCharDetails
import com.olam.warehouse.master.common.utils.*
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnCharDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnPriceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.master.veganicaragua.model.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.UIUtils.RECEIVING_DATA
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnnicaragua.R
import com.olam.warehouse.vegax.grnnicaragua.data.domain.model.VegaNicaraguaGlDetailsModel
import com.olam.warehouse.vegax.grnnicaragua.databinding.FragmentVegaNicaraguaGrnQualityParamsBinding
import com.olam.warehouse.vegax.grnnicaragua.ui.VegaNicaraguaGrnViewModel
import com.olam.warehouse.vegax.grnnicaragua.utils.*
import com.olam.warehouse.vegax.grnnicaragua.utils.saveLotSequence
import com.olam.warehouse.vegax.grnnicaragua.work.getGrnLotSequnceOneTimeRequestWorker
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Keerthi Santhanam on 9/15/2020.
 */
class VegaNicaraguaGrnSpotQualityParameterFragment : BaseFragment() {

    private var wbId: String? = ""
    private var bagCount: String? = ""
    private var plant: String? = ""
    private var batchNo: String? = ""
    private var isData: Boolean? = false
    private var materialNo: String? = ""
    private var netWeight: String? = ""
    private var grossWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var grade: String? = ""
    private var gradeDesc: String? = ""
    private var wbType: String? = ""
    private var receivingData = VegaReceiving()
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var mAdapter = VegaNicaraguaGrnSpotQualityParamsAdapter { enableProceedBtn(it) }
    private var isSort: Boolean = false
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private val vm: VegaNicaraguaGrnViewModel by viewModel()
    private var grnCharList = arrayListOf<VegaNicaraguaGrnCharDetails>()
    private var weighDetails: List<VegaNicaraguaWeighmentBagMaterial>? = null
    private var jsonData = mutableListOf<String>()
    private var tollingDetails = arrayListOf<VegaNicaraguaTollingDetails>()
    private var priceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()
    private var gradeMappingDescription: String? = ""
    var count: Int = 0
    private var transactionList = mutableListOf<VegaReceiving>()

    private var callBack: CallBack? = null
    private var exchangeRate: String? = ""//it is in percentage
//    private var currency: String? = ""

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            weighmentBagMaterialData: Any, qualityParameterList: ArrayList<VegaQualityParameter?>
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    interface OnParamsListener {
        fun onParamsProceed(
            qualityParameter: ArrayList<VegaQualityParameter?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String
        )
    }

    companion object {
        fun newInstance(receivingData: VegaReceiving) = VegaNicaraguaGrnSpotQualityParameterFragment().putArgs {
            putParcelable(RECEIVING_DATA, receivingData)
        }
    }

    private lateinit var binding: FragmentVegaNicaraguaGrnQualityParamsBinding
    override val layoutResourceId = R.layout.fragment_vega_nicaragua_grn_quality_params

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaGrnQualityParamsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnnicaragua/ui/spot/VegaNicaraguaGrnSpotQualityParameterFragment")
            .title("Nicaragua GRN Spot Quality").with(tracker)
        initUI()
        initExtra()
    }
    override fun onResume() {
        changeState(3, binding.stateBar.root, context)
        super.onResume()
    }

    private fun initUI() {
        binding.rvBagDetail.layoutManager = LinearLayoutManager(this.context)
        binding.rvBagDetail.adapter = mAdapter
        binding.btnParamsProceed.setOnClickListener { proceedToPost(FNQUALITY, R.string.confirm_quality_message) }
        vm.getConfigItems(UserRoles.DAYS_LIMIT.role)
        vm.configItems.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            updateConfigItems(it)
        })
        vm.grnTransList.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateTransUI(it) })
        vm.getReceivingWithLineItem()
        fetchingExchangeRate()

    }

    private fun initExtra() {
        arguments?.let {
            receivingData = it.getParcelable(RECEIVING_DATA)!!
            receivingData.createdDate = getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
            receivingData.erdat = getCurrentTimeInMills().toString()
            receivingData.docDate = getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
            wbId = receivingData.tmpWbId
            batchNo = receivingData.palletType
            isData = it.getBoolean(IS_PARAMS_VALUE, false)
            materialNo = receivingData.materialCode
            netWeight = receivingData.netWeight
            grossWeight = receivingData.grossWeight
            tarWeight = receivingData.tareWeight
            grade = receivingData.grade
            gradeDesc = receivingData.gradeDesc
            wbType = receivingData.weighBridgeType
            bagCount = receivingData.bagCount
            plant = receivingData.plantId
        }
        binding.tvParamsWeighBID.text = getString(R.string.wb_id).plus(" ").plus(wbId)
        if (receivingData.grnType.equals(getString(R.string.ptbf))) binding.tvType.text =
            getString(R.string.grn_ptbf_quality_params)
        else if (receivingData.grnType.equals(getString(R.string.fixed))) binding.tvType.text =
            getString(R.string.grn_fixed_quality_params)
        binding.etBatchNo.setText(receivingData.batchNumber)
        vm.getGlDetails(getCurrentKey())
        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer { updateGlDetails(it) })

        vm.bagItems.observe(viewLifecycleOwner, Observer {
            weighDetails = it
            if (isOnline()) {
                if (!receivingData.postDate.toString().isNullOrEmpty()) {
                    if (isdatefunc(receivingData.postDate.toString()))
                        postCreateGRN()
                    else
                        moveToFailurePage(getString(R.string.future_date_pendinglist))
                } else {
                    postCreateGRN()
                }

            } else saveData()
        })

        vm.grnCharDetails.observe(viewLifecycleOwner, Observer {
            updateGrnCharList(it)
        })
        vm.grnCharDetailsOffline.observe(viewLifecycleOwner, Observer {
            grnCharList.clear()
//            val sor = it.sortedByDescending { it.erdate }.reversed()
            grnCharList = it as ArrayList<VegaNicaraguaGrnCharDetails>
            materialNo?.let {
                vm.getQualityParams(materialNo!!, isData, wbId)
            }
        })

        /*vm.updateLotSequence.observe(viewLifecycleOwner, Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.data.let {
                            saveLotSequence(it?.sequence.toString())
                            saveGrnSequence()
                        }
                    }
                    Resource.Status.LOADING -> {
                    }
                    Resource.Status.ERROR -> {
                        showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    }
                }
            }
        })*/

        if (isOnline()) vm.getGrnCharDetails(receivingData.grade.toString(), receivingData.materialCode.toString())
        else vm.getGrnCharDetailsOffline(receivingData.grade.toString(), receivingData.materialCode.toString())

        vm.qualitylist.observe(viewLifecycleOwner, Observer {
            updateUI(it)
            vm.getGradeMapping(receivingData.grade!!.substring(receivingData.grade!!.length - 4))
        })

        vm.gradeMapping.observe(viewLifecycleOwner, Observer {
            gradeMappingDescription = it?.description ?: ""
            fetchingGrnPriceDetails()
        })

        vm.postGRN.observe(viewLifecycleOwner, Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let {
                            it.data.errorMessage = it.message
                            vm.updateGrnDataSuccess(it.data, receivingData)
                            if (it.data.wbFlag == true && it.data.qcFlag == true && it.data.grnFlag == true)
                                moveToSuccessPage(
                                    it.message,
                                    it.data.wbId,
                                    it.data.batchNumber,
                                    it.data.grnNumber,
                                    it.data.poNumber
                                )
                            else
                                moveToFailurePage(if (it.message.isNotEmpty()) it.message else it.errors.toString())
                        }
                        hideCustomLoading()
                    }
                    Resource.Status.LOADING -> showCustomLoading()
                    Resource.Status.ERROR -> {
                        receivingData.syncStatusMsg =
                            if (it.data?.message?.isNotEmpty() == true) it.data?.message else it.error.toString()
                        vm.updateGrnDataSuccess(VegaNicaraguaGrnPost(), receivingData)
                        hideCustomLoading()
                        requireContext().toast(it.error.toString())
                        moveToFailurePage(
                            if (it.error.toString().isNotEmpty()) it.error.toString() else ""
                        )
                        // showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    }
                }
            }
        })

//        binding.btnParamsProceed.isEnabled = true
        ViewCompat.setBackgroundTintList(
            binding.btnParamsProceed,
            context?.let {
                ContextCompat.getColorStateList(
                    it,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            }
        )
    }

    private fun updateTransUI(it: List<VegaReceiving>?) {
        it?.let {
            transactionList.clear()
            transactionList.addAll(it)
        }
       /* if(isMoveOfflineSuccess){
            startActivity(intent)
            requireActivity().finish()
        }*/
    }

    private fun fetchingGrnPriceDetails() {
        vm.grnPriceDetails.observe(viewLifecycleOwner, Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        when (it.data?.success) {
                            true -> {
                                priceDetails.clear()
                                val dataValue = it.data?.data!!
                                priceDetails = prepareGrnPriceDetailsList(dataValue)
                                priceDetails = getFilteredPriceDetails(priceDetails)
                            }
                            else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                        }
                        hideCustomLoading()
                        binding.btnParamsProceed.isEnabled = true
                    }
                    Resource.Status.LOADING -> showCustomLoading()
                    Resource.Status.ERROR -> {
                        hideCustomLoading()
                        binding.btnParamsProceed.isEnabled = true
                        requireContext().toast(it.error.toString())
                    }
                }
            }
        })

        vm.grnPriceDetailsOffline.observe(viewLifecycleOwner, Observer {
            priceDetails = it as ArrayList<VegaNicaraguaGrnPriceDetails>
            priceDetails = getFilteredPriceDetails(priceDetails)
            binding.btnParamsProceed.isEnabled = true
        })
        //    gradeMappingDescription.toString()
        if (isOnline()) vm.getGrnPriceDetails()
        else vm.getGrnPriceDetailsOffline()
    }

    private fun getFilteredPriceDetails(priceDetails: ArrayList<VegaNicaraguaGrnPriceDetails>): ArrayList<VegaNicaraguaGrnPriceDetails> {
        var filteredPriceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()
        val data = mAdapter.getItems()
        val data1 = data.filter { !it?.qualityParameter?.qualityParameterValue.isNullOrEmpty() }
        data1.forEach {
            it?.qualityParameter?.wbid = wbId.toString()
            it?.qualityParameter?.wbTempId = wbId.toString()
            if (it?.qualityParameter?.nameChar!!.contains("NIFG0014"))
                receivingData.certificate = it.qualityParameter.qualityParameterValue
            else if (it.qualityParameter.nameChar.contains("NIRM0010")) {
                receivingData.rendimientoBruto = it.qualityParameter.qualityParameterValue
                receivingData.yieldPercentage = it.qualityParameter.qualityParameterValue
            } else if (it.qualityParameter.nameChar.contains("NIEXPOP"))
                receivingData.exportablePercentage =
                    it.qualityParameter.qualityParameterValue?.replace("%", "")?.trim()
            else if (it.qualityParameter.nameChar.contains("NICASCAB"))
                receivingData.cascara = it.qualityParameter.qualityParameterValue
            else if (it.qualityParameter.nameChar.contains("NIRM0003"))
                receivingData.humedad = it.qualityParameter.qualityParameterValue
            else if (it.qualityParameter.nameChar.contains("NIDESA"))
                receivingData.DESMA = it.qualityParameter.qualityParameterValue?.replace("%", "")?.trim()
            else if (it.qualityParameter.nameChar.contains("NIDESC"))
                receivingData.DESMC = it.qualityParameter.qualityParameterValue?.replace("%", "")?.trim()
            else if (it.qualityParameter.nameChar.contains("NIDESD"))
                receivingData.DESMD = it.qualityParameter.qualityParameterValue?.replace("%", "")?.trim()
            else if (it.qualityParameter.nameChar.contains("NIDESR"))
                receivingData.DESMR = it.qualityParameter.qualityParameterValue?.replace("%", "")?.trim().toString()

        }
        priceDetails.forEach {
            if (it.fieldName.equals("LOWGRD01", true)) {
                it.percentage = receivingData.DESMA
                filteredPriceDetails.add(it)
            } else if ("LOWGRD02".equals(it.fieldName, true)) {
                it.percentage = receivingData.DESMC
                filteredPriceDetails.add(it)
            } else if ("LOWGRD03".equals(it.fieldName, true)) {
                it.percentage = receivingData.DESMD
                filteredPriceDetails.add(it)
            } else if ("LOWGRD04".equals(it.fieldName, true)) {
                it.percentage = receivingData.DESMR
                filteredPriceDetails.add(it)
            }else if (gradeMappingDescription?.toString().equals(it.description)) {
                it.percentage = receivingData.exportablePercentage
                filteredPriceDetails.add(it)
            }
        }
        return filteredPriceDetails
    }


    private fun updateGlDetails(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        val gson = Gson()
        jsonData.forEach {
            if (it.contains(GL_DETAILS)) {
                val details = gson.fromJson(it, VegaNicaraguaGlDetailsModel::class.java)
                if (details.GL_DETAILS.isNotEmpty()) {
                    details.GL_DETAILS.forEach {
                        var toll = VegaNicaraguaTollingDetails()
                        toll.costCenter = it.COST_CENTER
                        toll.glAccount = it.GL_ACCOUNT
                        tollingDetails.add(toll)
                    }
                }
            }
        }
    }

    private fun updateGrnCharList(response: Resource<GenericReqAndResp<List<GrnCharDetails>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            grnCharList.clear()
                            val dataValue = it.data?.data!!
                            grnCharList = prepareGrnCharDetails(dataValue)
                            materialNo?.let {
                                vm.getQualityParams(materialNo!!, isData, wbId)
                            }
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                    hideCustomLoading()
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                    materialNo?.let {
                        vm.getQualityParams(materialNo!!, isData, wbId)
                    }
                }
            }
        }
    }

    private fun proceedToPost(finalApproval: String, msg: Int) {
        var isValueNeed = true
        qualityParameterList.clear()
        val missedPos = mutableListOf<Int>()
        var data = arrayListOf<VegaQualityParamsWithQualitative?>()
        data = mAdapter.getItems()
        mAdapter.getItems().forEachIndexed { index, itValue ->
            if ((itValue?.qualityParameter?.entryObligatory.equals("X")) || (!itValue?.qualityParameter?.preSampling.isNullOrEmpty())
            ) {
                if (itValue?.qualityParameter?.qualityParameterValue.isNullOrEmpty()) {
                    if (itValue?.qualityParameter?.formulaParam.equals("X")) {
                        itValue?.qualityParameter?.mandatory = 0
                    } else {
                        isValueNeed = false
                        missedPos.add(index)
                        itValue?.qualityParameter?.mandatory = 1
                    }
                } else {
                    itValue?.qualityParameter?.mandatory = 0
                }
            }
        }
        if (isValueNeed) {
            val data1 = data.filter { !it?.qualityParameter?.qualityParameterValue.isNullOrEmpty() }
            data1.forEach {
                it?.qualityParameter?.wbid = wbId.toString()
                it?.qualityParameter?.wbTempId = wbId.toString()
                if (it?.qualityParameter?.nameChar!!.contains("NIFG0014"))
                    receivingData.certificate = it.qualityParameter.qualityParameterValue
                else if (it.qualityParameter.nameChar.contains("NIRM0010")) {
                    receivingData.rendimientoBruto = it.qualityParameter.qualityParameterValue
                    receivingData.yieldPercentage = it.qualityParameter.qualityParameterValue
                } else if (it.qualityParameter.nameChar.contains("NIEXPOP"))
                    receivingData.exportablePercentage =
                        it.qualityParameter.qualityParameterValue?.replace("%", "")?.trim()
                else if (it.qualityParameter.nameChar.contains("NICASCAB"))
                    receivingData.cascara = it.qualityParameter.qualityParameterValue
                else if (it.qualityParameter.nameChar.contains("NIRM0003"))
                    receivingData.humedad = it.qualityParameter.qualityParameterValue
                else if (it.qualityParameter.nameChar.contains("NIDESA"))
                    receivingData.DESMA = it.qualityParameter.qualityParameterValue?.replace("%", "")?.trim()
                else if (it.qualityParameter.nameChar.contains("NIDESC"))
                    receivingData.DESMC = it.qualityParameter.qualityParameterValue?.replace("%", "")?.trim()
                else if (it.qualityParameter.nameChar.contains("NIDESD"))
                    receivingData.DESMD = it.qualityParameter.qualityParameterValue?.replace("%", "")?.trim()
                else if (it.qualityParameter.nameChar.contains("NIDESR"))
                    receivingData.DESMR = it.qualityParameter.qualityParameterValue?.replace("%", "")?.trim().toString()

                qualityParameterList.add(it.qualityParameter)
                vm.saveQualityData(prepareQualityData(it.qualityParameter))
            }
            if (receivingData.grnType.equals(getString(R.string.toll))) {
                showConfirmDialog()
            } else if (receivingData.grnType.equals(getString(R.string.ptbf))) {
                callBack?.replaceFragment(FRAG_PTBF_PRICING, receivingData, qualityParameterList)
            } else callBack?.replaceFragment(FRAG_PRICING, receivingData, qualityParameterList)
        } else {
            showSnack(requireContext().resources.getString(R.string.enter_all_mandatory_fields))
            mAdapter.updateMissedPos(missedPos, data)
        }
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.grn_proceed)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    vm.getBagItems(receivingData.tmpWbId)
                },
                { dismiss() })
        }
    }

    private fun updateUI(params: List<VegaQualityParamsWithQualitative>?) {
        params?.let {
            it.forEach { item -> item.qualitative = item.qualitative?.filter { it.materialCode.equals(materialNo) } }
            when (it.isNotEmpty()) {
                true -> {
                    val value = mutableListOf<VegaQualityParamsWithQualitative>()
                    if (!isData!!) {
                        it.forEach { item ->
                            if (item.qualityParameter.vegaMandatory.equals("X") || item.qualityParameter.entryObligatory.equals(
                                    "X"
                                ) || (!item.qualityParameter.preSampling.equals("") && item.qualityParameter.qualityParameterValue?.isNotEmpty()!!)
                            ) {
                                value.add(item)
                            }
                            preQualityList.forEach { item1 ->
                                if (item.qualityParameter.nameChar == item1.sapQCName) {
                                    item.qualityParameter.qualityParameterValue = item1.satNam!!.split(" ")[0]
                                    value.add(item)
                                }
                            }

                        }
                    } else {
                        value.addAll(it)
                    }

                    mAdapter.addItems(
                        sortByListOfItems(value.distinctBy { it.qualityParameter.nameChar }),
                        batchNo,
                        tarWeight,
                        netWeight,
                        grossWeight,
                        challanNo,
                        bagCount,
                        plant,
                        grade,
                        gradeDesc,
                        grnCharList,
                        isData!!,
                        receivingData.supplierName,
                        receivingData.eudrStatus,
                        receivingData.batchNumber.toString()
                    )
                }
                else -> setErrorContentView(getString(R.string.quality_params_not_available))
            }
        }
    }

    private fun enableProceedBtn(item: List<VegaQualityParamsWithQualitative?>) {
        var isEnable = false
//        isEnable = !item.any { it?.qualityParameter?.qualityParameterValue?.isEmpty() == true }
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

    private fun postCreateGRN() {
        vm.updateSyncStartedStatus(receivingData.tmpWbId)
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        val post = VegaNicaraguaGrnPost()

        post.batchNumber = receivingData.batchNumber
        post.wbId = receivingData.weighBridgeId
        post.certificate = receivingData.certificate
        post.qualityGradeDesc = receivingData.gradeDesc
        post.grade = null
        post.key = currentKey
        post.grnType = receivingData.grnType
        post.plant = getPlantDetails()
        post.grnData = preparingGrnData()
        post.lotDetails = preparingLotDetails()
        post.priceDetails = priceDetails
        post.tollingDetails = tollingDetails
        post.weighDetails = weighDetails
        post.wbFlag = receivingData.wbFlag
        post.qcFlag = receivingData.qcFlag
        post.grnFlag = receivingData.grnFlag
        post.tempGRNNumber = receivingData.palletType
        post.cascara = receivingData.cascara
        post.humedad = receivingData.humedad
        post.rendimientoBruto = receivingData.rendimientoBruto
        post.exchangeRate = exchangeRate

        if (DateUtils.isFirstDayOfMonth(requireContext(), count)) {
            vm.postCreateGRN(post)
        } else {
            showSnack(getString(R.string.month_close_error))
        }
    }

    private fun preparingGrnData(): ArrayList<VegaNicaraguaGrnData> {
        val grnDataList = ArrayList<VegaNicaraguaGrnData>()
        val grnData = VegaNicaraguaGrnData()
        grnData.batchNumber = receivingData.batchNumber
        grnData.netWeight = receivingData.netWeight
        grnData.materialCode = receivingData.materialCode
        grnData.supplierCode = receivingData.supplierCode
        grnData.weighBridgeType = receivingData.weighBridgeType
        grnData.weighBridgeId = receivingData.weighBridgeId
        grnData.unitsOfMeasure = receivingData.unitsOfMeasure
        grnData.purchaseDocNum = receivingData.purchaseDocNum
        grnData.storageLocationCode = receivingData.storageLocationCode
        grnData.plant = receivingData.plantId
        grnData.item = receivingData.item
        grnData.createdDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        grnData.docDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        grnData.postDate = getFormatedDate(receivingData.postDate.toString())
        grnDataList.add(grnData)
        return grnDataList
    }

    private fun preparingLotDetails(): ArrayList<VegaNicaraguaLotDetails> {
        val lotDetailsList = ArrayList<VegaNicaraguaLotDetails>()
        val lotDetails = VegaNicaraguaLotDetails()

        lotDetails.batchNumber = receivingData.batchNumber
        lotDetails.netWeight = receivingData.netWeight
        lotDetails.materialCode = receivingData.materialCode
        lotDetails.supplierCode = receivingData.supplierCode
        lotDetails.weighBridgeType = receivingData.weighBridgeType
        lotDetails.weighBridgeId = receivingData.weighBridgeId
        lotDetails.unitsOfMeasure = receivingData.unitsOfMeasure
        lotDetails.purchaseDocNum = receivingData.purchaseDocNum
        lotDetails.storageLocationCode = receivingData.storageLocationCode
        lotDetails.storageLocation = receivingData.storageLocationName
        lotDetails.grossWeight = receivingData.grossWeight
        lotDetails.bagType = receivingData.bagType
        lotDetails.plant = receivingData.plantId
        lotDetails.item = receivingData.item
        //adding quality details to lot
        val qualityDetailsList = ArrayList<VegaNicaraguaQualityDetails>()
        qualityParameterList.forEach {

            val qualityDetail = VegaNicaraguaQualityDetails()
            qualityDetail.descrChar = it!!.descrChar
            qualityDetail.nameChar = it.nameChar
            qualityDetail.qualityParameterValue = it.qualityParameterValue
            if( it.nameChar=="TP_VENDOR"){
                qualityDetail.qualityParameterValue = receivingData.supplierCode
            }
            qualityDetailsList.add(qualityDetail)
        }

        lotDetails.qualityDetails = qualityDetailsList
        lotDetailsList.add(lotDetails)

        return lotDetailsList
    }

    private fun moveToSuccessPage(
        message: String,
        wbid: String?,
        batchNumber: String?,
        grnNumber: String?,
        poNumber: String?
    ) {
        if (isOnline() && batchNumber!!.isNotEmpty()) postUpdateLotSequence(batchNumber.toString())
        else {
            saveLotSequence(batchNumber!!, transactionList, false)
            saveGrnSequence(transactionList, false)
        }
        receivingData.plantName = getPlantDetails().plantName
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, message)
        if (isOnline()) {
            intent.putExtra(
                AppUtils.SUB_TITLE, getString(R.string.weighbridge_is).plus(wbid)
                    .plus(getString(R.string.batch_no_is)).plus(batchNumber)
                    .plus(getString(R.string.grn_no_is)).plus(grnNumber)
                    .plus(getString(R.string.po_no_is)).plus(poNumber)
            )
        } else {
            intent.putExtra(
                AppUtils.SUB_TITLE, getString(R.string.weighbridge_is).plus(wbid)
                    .plus(getString(R.string.batch_no_is)).plus(batchNumber)
                    .plus(getString(R.string.grn_no_is)).plus(grnNumber)
            )
        }
        intent.putExtra(AppUtils.PRINT_ENABLE, true)
        intent.putExtra(UIUtils.RECEIVING_DATA, receivingData)
        intent.putExtra(UIUtils.FROM_NICARAGUA_COFFEE, true)
        intent.putExtra(UIUtils.NICARAGUA_PRINT_TYPE, UIUtils.NICARAGUA_PRINT_GRN_RECEIPT)
        intent.putParcelableArrayListExtra(UIUtils.BAGS_DATA, weighDetails as ArrayList<out Parcelable>)
        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, prepareLotCard(batchNumber))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun moveToFailurePage(msg: String) {
        if (isOnline()) postUpdateLotSequence(receivingData.batchNumber.toString())
        else {
            saveLotSequence(receivingData.batchNumber!!, transactionList, false)
            saveGrnSequence(transactionList, false)
        }
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.transaction_fail_msg))
        intent.putExtra(AppUtils.SUB_TITLE, msg.plus("\n").plus(getString(R.string.transaction_retry)))
        intent.putExtra(AppUtils.FAILURE, false)
        intent.putExtra(AppUtils.MSG, msg)
        startActivity(intent)
    }

    private fun postUpdateLotSequence(batchNumber: String?) {
        val input = workDataOf(UIUtils.GRN_DATA to receivingData.grnType)
        val worker = getGrnLotSequnceOneTimeRequestWorker(input)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                        }
                        WorkInfo.State.FAILED -> {
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                        else -> {}
                    }
                }

            })
    }

    private fun saveData() {
        receivingData.createdDate = getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receivingData.erdat = getCurrentTimeInMills().toString()
        receivingData.docDate = getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
        receivingData.exchangeRate = exchangeRate
        vm.updateGrnDataSuccess(VegaNicaraguaGrnPost(), receivingData)
        moveToSuccessPage(
            getString(R.string.grn_offline_success),
            receivingData.tmpWbId,
            receivingData.batchNumber,
            receivingData.palletType,
            ""
        )
    }

    fun getDate(milliSeconds: Long, dateFormat: String?): String? {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    private fun prepareLotCard(batchNumber: String?): ArrayList<VegaCoffeeSalesLots> {
        val lotList = arrayListOf<VegaCoffeeSalesLots>()
        val lot = VegaCoffeeSalesLots()
        lot.batchNumber = batchNumber.toString()
        lot.editedWeight = receivingData.netWeight
        lot.unitOfMeasure = receivingData.unitsOfMeasure
        lot.materialName = receivingData.materialName
        lot.materialCode = receivingData.materialCode ?: ""
        lot.grade = receivingData.gradeDesc
        lot.certificate = receivingData.certificate
        lot.storageLocationCode = receivingData.storageLocationCode
        lotList.add(lot)
        return lotList
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.DAYS_LIMIT.item -> {
                    when {

                        it.applicable?.contains("Y")!! -> {
                            if (!it.value!!.isNullOrEmpty())
                                count = it.value!!.toInt()
                            else
                                count = 0
                        }
                        it.applicable?.contains("N")!! -> {
                            count = 0
                        }
                    }
                }
            }
        }
    }

    private fun fetchingExchangeRate() {
        vm.exchangeRate.observe(viewLifecycleOwner, Observer { response ->
            response?.let {

                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.data.let {
                            exchangeRate = it?.exchangeRate
//                            currency = it?.currencyCode
                            //USDAmount = it?.currencyValue
                        }
                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        requireContext().toast(it.error.toString())
                    }
                }
            }
        })

        vm.exchangeRateOffline.observe(viewLifecycleOwner, Observer {
            exchangeRate = it?.exchangeRate
//            currency = it?.currencyCode
            //USDAmount = it?.currencyValue
        })

        if (isOnline()) vm.getExchangeRate() else vm.getExchangeRateOffline()
    }
}
