package com.olam.warehouse.vegax.qualityofanylot.ui.params

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.qualityofanylot.R
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.*
import com.olam.warehouse.vegax.qualityofanylot.databinding.FragmentVegaAnyLotQualityParamsBinding
import com.olam.warehouse.vegax.qualityofanylot.ui.VegaAnyLotQualityViewModel
import com.olam.warehouse.vegax.qualityofanylot.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaAnyLotQualityParamsFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_any_lot_quality_params
    private val vm: VegaAnyLotQualityViewModel by viewModel()
    private lateinit var binding:FragmentVegaAnyLotQualityParamsBinding
    private var postRequest = VegaAnyLotQualityPostRequest()
    private var isSap: Boolean = false
    private var mAdapter = VegaAnyLotQualityParamsAdapter { enableProceedBtn(it) }
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var materialNo: String? = ""
    private var lotbatchNo: String? = ""
    private var lotmaterialNo: String? = ""
    private var plantId: String? = ""
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var preQualityList = mutableListOf<QualityDetails>()
    private var createPreQualityList = mutableListOf<VegaQualityParams>()
    var lotId=""
    var typeSelect=""
    var secretId:String?=""
    private var callBack: CallBack? = null
    private lateinit var singleItem: VegaQualityParamsWithQualitative



    companion object {
        fun newInstance(inspectionLot: VegaAnyLotQualityPostRequest,id:String="",type:String) =
            VegaAnyLotQualityParamsFragment().putArgs {
                putParcelable(INSPECTION_LOT, inspectionLot)
                putString(LOT_ID,id)
                putString(TYPE_SELECT,type)
            }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    interface CallBack {
        fun replaceFragment(
            from: String,
            data: Any,
            typeSelect: String
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaAnyLotQualityParamsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initExtra()
        initUI()
        clickEvent()

    }

    private fun clickEvent() {
        binding.btnParamsProceed.setOnClickListener {
            proceedToPost(isSAP = true, isMoveToSummary = false)
        }
     binding.btnParamsSave.setOnClickListener {
         proceedToPost(isSAP = false,isMoveToSummary = false)
     }
        binding.btnProceedToNxt.setOnClickListener {
            proceedToPost(isSAP = false,isMoveToSummary = true)
        }
    }

    private fun initExtra() {
        postRequest = arguments?.getParcelable(INSPECTION_LOT)!!
        lotId= arguments?.getString(LOT_ID,"").toString()
        typeSelect= arguments?.getString(TYPE_SELECT,"").toString()

        lotbatchNo = postRequest.batchNumber
        lotmaterialNo = postRequest.materialCode
        secretId=postRequest.secretKey

       // lotId= postRequest.batchNumber?:""

        if(typeSelect== CREATE_QUALITY_LOT) {
            binding.btnParamsSave.visible()
            binding.btnParamsProceed.visible()

            vm.getPreSamplingQualityData(lotbatchNo?:"", lotmaterialNo?.trim()!!)

        }else{
            binding.btnParamsSave.gone()
            binding.btnParamsProceed.visible()
            vm.getQualityById(getCurrentKey(),lotId)
            binding.llLot.gone()

        }


    }

    private fun initUI() {

        if(getCurrentKey().contains("VEGA_NG")&& getCurrentKey().contains("COCO") && typeSelect==CREATE_QUALITY_LOT){
            binding.btnProceedToNxt.visible()
            binding.btnParamsProceed.gone()
            binding.btnParamsSave.gone()
        }else{
            binding.btnProceedToNxt.gone()
            binding.btnParamsProceed.visible()
            binding.btnParamsSave.visible()
        }

        mAdapter.updateType(typeSelect)
        binding.tvMaterial.text = postRequest.materialName  //materialNo = inspectionLot.materialCode
        binding.etBatchNo.setText(postRequest.batchNumber)
        plantId = postRequest.werks

        binding.rvBagDetail.layoutManager = LinearLayoutManager(this.context)
        binding.rvBagDetail.adapter = mAdapter
        materialNo =
            if (!postRequest.materialCode?.length?.equals(18)!!) "000000".plus(postRequest.materialCode) else postRequest.materialCode



        vm.custonLocation.observe(
            viewLifecycleOwner,
            Observer {
                custonLocationList =
                    it.filter { !it.storageLocationType.equals("P") }.toMutableList()
            })
        vm.getCustomLocations()


        vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.qualityPost.observe(viewLifecycleOwner, Observer { updateUIResponse(it) })
        vm.qualityById.observe(viewLifecycleOwner, Observer {
            updateQualityById(it)
        })

        vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })

    }

    private fun updateQualityById(response: Resource<GenericReqAndResp<VegaAnyLotListData>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            preQualityList = it.data!!.data.qualityParameters as MutableList<QualityDetails>
                            materialNo?.let { vm.getQualityParams(materialNo!!, false, "") }

                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }

    }


    private fun updateUIResponse(response: Resource<GenericReqAndResp<List<VegaAnyLotListData>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            moveToSuccess()
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
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



    private fun updateUI(params: List<VegaQualityParamsWithQualitative>?) {
        params?.let {
            when (it.isNotEmpty()) {
                true -> {
                    var listString= ArrayList<String>()
                    it.forEach { item ->
                        if (item.qualityParameter.nameChar == "CI_SLOC_KOR") {
                            item.qualitative =
                                prepareQualitative(
                                    custonLocationList,
                                    item.qualityParameter.materialCode
                                )
                        }
                    }
                    val value = mutableListOf<VegaQualityParamsWithQualitative>()

                    var preQualityListString= preQualityList.map { it.nameChar }
                    var qualityListString = it.map { it.qualityParameter.nameChar }.distinctBy { it }

                    var filteredStrings= preQualityListString.filter { !qualityListString.contains(it) }


                        it.forEach { item ->


                            if (item.qualityParameter.vegaMandatory.equals("X") || item.qualityParameter.entryObligatory.equals(
                                    "X"
                                ) || (!item.qualityParameter.preSampling.equals("") && item.qualityParameter.qualityParameterValue?.isNotEmpty()!!)
                            ) {
                                value.add(item)
                            }


                            if(typeSelect== CREATE_QUALITY_LOT) {
                                createPreQualityList.forEach { item1 ->
                                    if (item.qualityParameter.nameChar == item1.sapQCName) {
                                        item.qualityParameter.qualityParameterValue = item1.satNam!!
                                       /* if (getCurrentKey().split("_")[1].contains("NI")) {
                                            item.qualityParameter.qualityParameterValue = item1.satNam!!
                                        } else
                                            item.qualityParameter.qualityParameterValue = item1.satNam!!.split(" ")[0]*/
                                       value.add(item)
                                    }
                                }

                            }else{
                                preQualityList.forEach { item1 ->
                                    if (item.qualityParameter.nameChar == item1.nameChar) {
                                        item.qualityParameter.preSampling = "X"
                                        item.qualityParameter.qualityParameterValue = item1.qualityParameterValue
                                        value.add(item)
                                    }
                                }


                            }
                            singleItem= item
                        }

                            preQualityList.forEach { item1 ->
                                if (filteredStrings.contains(item1.nameChar) ) {
                                    val singleItemQuality= VegaQualityParamsWithQualitative()
                                    val qualityParam= VegaQualityParameter()
                                    qualityParam.preSampling = "X"
                                    qualityParam.qualityParameterValue = item1.qualityParameterValue
                                    qualityParam.nameChar = item1.nameChar.toString()
                                    qualityParam.descrChar = item1.descrChar.toString()
                                    qualityParam.qualitative = ArrayList<VegaQualitative>()
                                    singleItemQuality.qualityParameter= qualityParam
                                    singleItemQuality.qualitative= qualityParam.qualitative
                                    value.add(singleItemQuality)
                                }

                            }


                    mAdapter.addItems(
                        sortByListOfItems(value.distinctBy { it.qualityParameter.nameChar }),
                        "",
                        "",
                        "",
                        typeSelect
                    )
                }
                else -> setErrorContentView("Quality params not available for this material")
            }
        }
    }



    private fun  moveToSuccess() {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (isSap){
            intent.putExtra(AppUtils.TITLE, getString(R.string.success_post))

            if(secretId?.isNotEmpty() == true){
                intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.secret_id).plus(" : ").plus(secretId))
            }else intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.batch_number).plus(" : ").plus(lotbatchNo))

        }
        else {
            intent.putExtra(AppUtils.TITLE, getString(R.string.success_save))
            if(secretId?.isNotEmpty() == true){
                intent.putExtra(
                    AppUtils.SUB_TITLE, getString(R.string.secret_id).plus(" : ").plus(secretId)
                )
            }
            intent.putExtra(
                AppUtils.SUB_TITLE, getString(R.string.trans_no).plus(" : ").plus(postRequest.transactionNumber)+"\n".
                plus(getString(R.string.batch_number)).plus(" : ").plus(lotbatchNo)
            )

        }
        startActivity(intent)
        requireActivity().finish()
    }


    private fun validateRangeParamValue(id: String?, noFrom: Double?, noTo: Double?): Boolean {
        return when {
            id?.isNotEmpty()!! -> {
                return when {
                    id.toDouble() < noFrom!! || id.toDouble() > noTo!! -> false
                    else -> true
                }
            }
            else -> true
        }
    }

    @SuppressLint("WrongConstant")
    private fun handleScroll(editText: EditText) {
        editText.setOnEditorActionListener(TextView.OnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                val view = textView.focusSearch(View.FOCUS_FORWARD)
                if (view != null) {
                    if (!view.requestFocus(View.FOCUS_FORWARD)) {
                        return@OnEditorActionListener true
                    }
                }
                return@OnEditorActionListener false
            }
            false
        })
    }

    private fun showConfirmDialog(msg: String, isSapLocal: Boolean) {
           isSap=isSapLocal
        MaterialDialog(requireContext()).show {
            title(text = msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {

                    postQuality(isSapLocal,false)
                },
                { dismiss() })
        }
    }

    private fun postQuality(isSAP: Boolean,isMoveToSummary:Boolean) {
        val qualityDetailsList = ArrayList<QualityDetails>()
        qualityParameterList.forEach { item ->
            val dat = QualityDetails()
            dat.qualityParameterValue = item?.qualityParameterValue!!
            dat.descrChar = item.descrChar!!
            dat.nameChar = item.nameChar
            qualityDetailsList.add(dat)
        }

        postRequest.key= getCurrentKey()
        postRequest.transactionNumber= if(postRequest.transactionNumber?.isEmpty() == true)  getTmpId() else postRequest.transactionNumber
        postRequest.isDummy=false
        postRequest.date=DateUtils.getCurrentDateYearFormat()
        postRequest.isSap=isSAP
        postRequest.qualityDetails=qualityDetailsList.toList()


        if(isMoveToSummary){
            callBack?.replaceFragment(ANY_LOT_QUALITY_PARAM,postRequest,typeSelect)
        }else vm.postOrSaveQualityDetails(postRequest)

    }

    private fun proceedToPost(isSAP: Boolean, isMoveToSummary:Boolean) {
        var isValueNeed = true
        qualityParameterList.clear()
        val missedPos = mutableListOf<Int>()
        var data = arrayListOf<VegaQualityParamsWithQualitative?>()
        mAdapter.itemCount
        data = mAdapter.getItems()
        mAdapter.getItems().forEachIndexed { index, itValue ->
            if ((itValue?.qualityParameter?.vegaMandatory.equals("X"))
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
            qualityParameterList.add(itValue?.qualityParameter)

        }

       // postQuality(isSAP=false, isMoveToSummary=true)

        if (isValueNeed) {
            if(isMoveToSummary){
               postQuality(isSAP=false, isMoveToSummary=true)
            }else {
                val msg = if (isSAP) getString(R.string.accept_quality) else getString(R.string.save_quality)
                showConfirmDialog(msg, isSAP)
            }
        } else {
            showSnack(requireContext().resources.getString(R.string.atleast_one_value))
            mAdapter.updateMissedPos(missedPos, data)
        }
    }

    private fun enableProceedBtn(item: List<VegaQualityParamsWithQualitative?>) {
        var isEnable = false
        item.forEach {
            it?.qualityParameter?.qualityParameterValue?.let { it1 -> if (it1.isNotEmpty()) isEnable = true }
        }
        if (isEnable) {
            binding.btnParamsSave.isEnabled=true
            binding.btnParamsProceed.isEnabled = true
            binding.btnParamsProceed.setBackgroundColor(requireContext().getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            binding.btnParamsSave.setBackgroundColor(requireContext().getColor(com.olam.warehouse.presentation.R.color.orange))
        } else {
            binding.btnParamsSave.isEnabled = false
            binding.btnParamsProceed.isEnabled = false
            binding.btnParamsProceed.setBackgroundColor(requireContext().getColor(com.olam.warehouse.presentation.R.color.grey))
            binding.btnParamsSave.setBackgroundColor(requireContext().getColor(com.olam.warehouse.presentation.R.color.grey))
        }
    }

    private fun updatePreQuality(response: Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            createPreQualityList = it.data?.data?.get(0)?.qualityParameters as MutableList<VegaQualityParams>
                            materialNo?.let { vm.getQualityParams(materialNo!!, false, "") }

                        }
                        else ->
                            showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")

                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }



}

