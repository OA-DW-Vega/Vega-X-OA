package com.olam.warehouse.vegax.qualitynigeria.ui.params

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.veganigeria.utils.portPlantIdList
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitynigeria.R
import com.olam.warehouse.vegax.qualitynigeria.databinding.FragmentVegaNigeriaQualityParamsBinding
import com.olam.warehouse.vegax.qualitynigeria.ui.VegaNigeriaQualityCustomSingleSelectDialog
import com.olam.warehouse.vegax.qualitynigeria.ui.VegaNigeriaQualitySingleSelectListener
import com.olam.warehouse.vegax.qualitynigeria.ui.VegaNigeriaQualityViewModel
import com.olam.warehouse.vegax.qualitynigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
class VegaNigeriaQualityParameterFragment : BaseFragment(), VegaNigeriaQualitySingleSelectListener {

    private var wbId: String? = ""
    private var bagCount: String? = ""
    private var plant: String? = ""
    private var batchNo: String? = ""
    private var isData: Boolean? = false
    private var materialNo: String? = ""
    private var netWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var itemValue: String? = ""
    private var wbType: String? = ""
    private var weighBridgeDetails = VegaQualityWBDetails()
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var mAdapter = VegaNigeriaQualityParamsAdapter { enableProceedBtn(it) }
    private var isSort: Boolean = false
    private var customDialog: VegaNigeriaQualityCustomSingleSelectDialog? = null
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private val vm: VegaNigeriaQualityViewModel by viewModel()
    private lateinit var mListener: OnParamsListener
    private var paidWeight: String = ""
    private var grnQty: Double = 0.0
    private var refraction: Double = 0.0
    private var discWeight: Double = 0.0
    private var grnNumber: String? = ""
    private var storageLocationCode: String = ""
    private var grnQtyQualityParam: VegaQualityParameter = VegaQualityParameter()
    private var qualityMasterData = mutableListOf<VegaQualityParamsWithQualitative>()
    var charValue = mutableListOf<String>()
    var userPlantId = getPlantDetails().plantId
    var vegaMaterialList = mutableListOf<VegaMaterial>()
    private var materialObj = VegaMaterial()


    /* val qualityParamList = arrayListOf<VegaQualityParamsWithQualitative>()
     val qualityList = MutableLiveData<List<VegaQualityParamsWithQualitative>>()*/

    interface OnParamsListener {
        fun onParamsProceedQuality(
            qualityParameter: ArrayList<VegaQualityParameter?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String,
            storageLocationCode: String,
            complainceFlag: String?
        )
    }

    companion object {
        fun newInstance() = VegaNigeriaQualityParameterFragment().putArgs {}
    }

    private lateinit var binding: FragmentVegaNigeriaQualityParamsBinding
    override val layoutResourceId = R.layout.fragment_vega_nigeria_quality_params

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
        binding = FragmentVegaNigeriaQualityParamsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualityecuador/ui/params/VegaEcuadorQualityParameterFragment")
            .title("Ecuador Quality").with(tracker)
        initUI()
        initExtra()
    }

    private fun initUI() {
        binding.rvBagDetail.layoutManager = LinearLayoutManager(this.context)
        binding.rvBagDetail.adapter = mAdapter
        binding.btnParamsProceed.setOnClickListener {
            /* if(binding.tvReceivingLocation.text.isNullOrEmpty()) {
                 showSnack(getString(R.string.select_receiving_plant))
             }else{*/
            proceedToPost(FNQUALITY, R.string.confirm_quality_message)
            // }
        }

        binding.btnAccept.setOnClickListener {
            proceedToPost(
                FNQUALITY,
                R.string.confirm_quality_message
            )
        }
        binding.btnReject.setOnClickListener {
            proceedToPost(
                FNREJECT,
                R.string.confirm_reject_message
            )
        }

    }



    private fun initExtra() {
        arguments?.let {
            weighBridgeDetails = it.getParcelable(WEIGHSCALE)!!
            wbId = weighBridgeDetails.weighBridgeId
            batchNo = weighBridgeDetails.batchNumber
            isData = it.getBoolean(IS_PARAMS_VALUE, false)
            materialNo = weighBridgeDetails.materialCode
            netWeight = weighBridgeDetails.netWeight
            tarWeight = weighBridgeDetails.bagWeight
            challanNo = weighBridgeDetails.challan
            itemValue = weighBridgeDetails.item
            wbType = weighBridgeDetails.weighBridgeType
            bagCount = weighBridgeDetails.bagCount
            plant = weighBridgeDetails.plant
            grnNumber = weighBridgeDetails.grnNumber
            storageLocationCode = weighBridgeDetails.storageLocationCode
        }

        vm.getVegaMaterials()
        vm.vegaMaterials.observe(
            viewLifecycleOwner,
            Observer {
                vegaMaterialList = it as MutableList<VegaMaterial>
                if(vegaMaterialList.isNotEmpty()){
                    materialObj = vegaMaterialList.singleOrNull { it.materialCode.takeLast(12).contains(materialNo?.takeLast(12).toString()) }?:VegaMaterial()
                }
            })
        if (weighBridgeDetails.weighBridgeType == STO) {
            binding.tvParamsWeighBID.text = getString(R.string.wb_id).plus(" ").plus(wbId)
        } else if ( portPlantIdList.contains(userPlantId)
        ) {
            binding.tvParamsWeighBID.text = getString(R.string.secret_id).plus(" ").plus(challanNo)
        } else {
            binding.tvParamsWeighBID.text = getString(R.string.wb_id).plus(" ").plus(wbId)
        }
        if (!grnNumber.isNullOrEmpty() && !grnNumber!!.contains("TMP"))
            binding.tvGrn.text =
                getString(com.olam.warehouse.presentation.R.string.grn).plus(grnNumber)
        else binding.tvGrn.gone()
        //binding.etBatchNo.setText(batchNo, TextView.BufferType.EDITABLE)
        // binding.etBatchNo.isEnabled = false
        /*if(PreferenceHelper.get(Constants.WERKS, "").equals("6154") || PreferenceHelper.get(Constants.WERKS, "").equals("6155")) {
            binding.tvReceivingLocation.setVisibility(View.VISIBLE)
            vm.getConfigItems(UserRoles.APPROVE.role)
            vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        }else{
            binding.tvReceivingLocation.setVisibility(View.GONE)
        }*/

        vm.qualitylist.observe(viewLifecycleOwner, Observer {
            qualityMasterData =
                it.filter { it.qualityParameter.materialCode == materialNo }.toMutableList()
            updateUI(it)
        })
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        materialNo?.let {
            /*if(PreferenceHelper.get(Constants.WERKS, "").equals("6154") || PreferenceHelper.get(Constants.WERKS, "").equals("6155")) {
                vm.getConfigItems(UserRoles.APPROVE.role)
            }else {
                vm.getQualityParams(materialNo!!, isData, wbId)
            }*/
            if (portPlantIdList.contains(userPlantId)
            ) {
                vm.getConfigItems(UserRoles.APPROVE.role)
            } else {
                vm.getConfigItems(UserRoles.QUALITY.role)
            }
        }
        // vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })

        if (wbType.equals(PROCURE)) {
            binding.tvType.text = (" ").plus(getString(R.string.supplier))
        } else {
            binding.tvType.text = MTNR
        }
        enableProceedBtn(weighBridgeDetails.status)

        if(getCurrentKey().contains("VEGA_NG") && getCurrentKey().contains("CASH")){
            vm.getQualityParams(materialNo!!, isData, wbId)
        }

    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        val receivingPlantList =
            configItems?.filter { it.process.equals(ConfigItems.DEFAULT_TRANSFER_LOC.item) }
        if (((!receivingPlantList.isNullOrEmpty()) && (receivingPlantList.size > 0))) {
            var list = receivingPlantList.get(0).value


            charValue =
                list?.split(",")?.map { it.trim() }?.toMutableList() ?: mutableListOf<String>()
            vm.getQualityParams(materialNo!!, isData, wbId)
        }
        //binding.tvReceivingLocation.setOnClickListener { showReceivingLocationDialog(charValue) }

    }

    private fun showReceivingLocationDialog(charValue: MutableList<String>) {
        customDialog =
            VegaNigeriaQualityCustomSingleSelectDialog(
                getString(R.string.select_receiving_plant),
                false,
                charValue as ArrayList<String>,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    private fun enableProceedBtn(status: Int?) {

        when (status) {
            4 -> {
                binding.btnParamsProceed.isEnabled = false
                binding.btnAccept.isEnabled = false
                binding.btnReject.isEnabled = false
                ViewCompat.setBackgroundTintList(
                    binding.btnParamsProceed,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.grey
                        )
                    }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnAccept,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.grey
                        )
                    }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnReject,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.grey
                        )
                    }
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

    private fun proceedToPost(finalApproval: String, msg: Int) {
        var isValueNeed = true
        qualityParameterList.clear()
        val missedPos = mutableListOf<Int>()
        var data = arrayListOf<VegaQualityParamsWithQualitative?>()
        mAdapter.itemCount
        data = mAdapter.getItems()
        mAdapter.getItems().forEachIndexed { index, itValue ->
            if ((itValue?.qualityParameter?.entryObligatory.equals("X")) || (itValue?.qualityParameter?.preSampling.isNullOrEmpty())
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
                qualityParameterList.add(itValue?.qualityParameter)
            }
        }

        /*if(PreferenceHelper.get(Constants.WERKS, "").equals("6154") || PreferenceHelper.get(Constants.WERKS, "").equals("6155")) {
            grnQtyQualityParam.nameChar = "RECEIVING_PLANT"
            grnQtyQualityParam.descrChar = "RECEIVING_PLANT"
            grnQtyQualityParam.vegaMandatory = "X"
            grnQtyQualityParam.dataType = "CHAR"
            grnQtyQualityParam.materialCode = materialNo.toString()
            grnQtyQualityParam.qualityParameterValue = ((binding.tvReceivingLocation.text.toString()).split(":")[0])
            grnQtyQualityParam.qualitative = emptyList()
        }

        qualityParameterList.add(grnQtyQualityParam)*/

        /* if(binding.etBatchNo.text.toString()!!.length < 10)
             showSnack(requireContext().resources.getString(R.string.batch_no_should_be_10_digit))*/

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
            showConfirmDialog("", msg, finalApproval)
        } else {
            showSnack(requireContext().resources.getString(R.string.enter_all_mandatory_fields))
            mAdapter.updateMissedPos(missedPos, data)
        }
    }


    private fun updateUI(params: List<VegaQualityParamsWithQualitative>?) {
        params?.let {
            when (it.isNotEmpty()) {
                true -> {
                    val value = mutableListOf<VegaQualityParamsWithQualitative>()
                    if (!isData!!) {
                        it.forEach { item ->

                            if (item.qualityParameter.nameChar.equals("B_GRNQTY1")) {
                                item.qualityParameter.qualityParameterValue =
                                    weighBridgeDetails.netWeight
                                item.qualityParameter.qualitative = emptyList()
                                grnQtyQualityParam = item.qualityParameter
                            }

                            /*if (item.qualityParameter.vegaMandatory.equals("X") || item.qualityParameter.entryObligatory.equals(
                                    "X"
                                ) || (!item.qualityParameter.preSampling.equals("") && item.qualityParameter.qualityParameterValue?.isNotEmpty()!!)
                            ) {
                                value.add(item)
                            }*/

                            if (!(item.qualityParameter.preSampling.equals("X"))
                            ) {
                                // if ((item.qualityParameter.nameChar.equals("RECEIVING_PLANT")) && (PreferenceHelper.get(Constants.WERKS, "").equals("6154") || PreferenceHelper.get(Constants.WERKS, "").equals("6155"))) {
                                if ((item.qualityParameter.nameChar.equals("RECEIVING_PLANT"))) {
                                    val qualityParamsQualitativeNew =
                                        VegaQualityParamsWithQualitative()
                                    val qualityParamsNew = VegaQualityParameter()
                                    qualityParamsNew.wbid = item.qualityParameter.wbid
                                    qualityParamsNew.wbTempId = item.qualityParameter.wbTempId
                                    qualityParamsNew.materialCode =
                                        item.qualityParameter.materialCode
                                    qualityParamsNew.descrChar = item.qualityParameter.descrChar
                                    qualityParamsNew.nameChar = item.qualityParameter.nameChar
                                    qualityParamsNew.entryObligatory =
                                        item.qualityParameter.entryObligatory
                                    qualityParamsNew.unitText = item.qualityParameter.unitText
                                    qualityParamsNew.dataType = item.qualityParameter.dataType
                                    qualityParamsNew.unitsOfMeasure =
                                        item.qualityParameter.unitsOfMeasure
                                    qualityParamsNew.numberDigits =
                                        item.qualityParameter.numberDigits
                                    qualityParamsNew.numberDecimals =
                                        item.qualityParameter.numberDecimals
                                    qualityParamsNew.numValFm = item.qualityParameter.numValFm
                                    qualityParamsNew.numValTo = item.qualityParameter.numValTo
                                    qualityParamsNew.currValFm = item.qualityParameter.currValFm
                                    qualityParamsNew.currValTo = item.qualityParameter.currValTo
                                    qualityParamsNew.valRelatn = item.qualityParameter.valRelatn
                                    qualityParamsNew.timeStamp = item.qualityParameter.timeStamp
                                    qualityParamsNew.qualityParameterValue =
                                        item.qualityParameter.qualityParameterValue
                                    qualityParamsNew.isSyncStatus =
                                        item.qualityParameter.isSyncStatus

                                    var qualityParameterList = mutableListOf<VegaQualitative?>()
                                    charValue.forEach { it2 ->
                                        val vegaQualitative = VegaQualitative()
                                        vegaQualitative.charValue = it2
                                        qualityParameterList.add(vegaQualitative)
                                    }
                                    qualityParamsQualitativeNew.qualitative =
                                        qualityParameterList as List<VegaQualitative>
                                    qualityParamsQualitativeNew.qualityParameter = qualityParamsNew
                                    qualityParamsQualitativeNew.qualitative =
                                        qualityParamsQualitativeNew.qualitative
                                    //qualityParamList.add(qualityParamsQualitativeNew)

                                    //  qualityList.value = qualityParamList
                                    value.add(qualityParamsQualitativeNew)


                                    //  item.qualityParameter.qualityParameterValue = ""
                                    //val qualityParamsQualitative = VegaQualityParamsWithQualitative()
                                    //val qualitativeList: List<VegaQualitative>? = emptyList()
                                    /* var qualityParameterList = mutableListOf<VegaQualitative?>()
                                     charValue.forEach {it2->
                                         val vegaQualitative = VegaQualitative()
                                         vegaQualitative.charValue = it2
                                         qualityParameterList.add(vegaQualitative)
                                     }*/
                                    // qualityParamsQualitative.qualitative = qualityParameterList as List<VegaQualitative>

                                    /*item.qualityParameter.qualitative = qualityParamsQualitative.qualitative

                                    item.qualityParameter.qualitative = qualityParamsQualitative.qualitative
                                    qualityParamsQualitative.qualitative = item.qualityParameter.qualitative*/
                                } else if (!(item.qualityParameter.nameChar.equals("RECEIVING_PLANT"))) {
                                    value.add(item)
                                }
                            }

                            preQualityList.forEach { item1 ->
                                if (item.qualityParameter.nameChar == item1.sapQCName) {
                                    item.qualityParameter.qualityParameterValue =
                                        item1.satNam!!.split(" ")[0]
                                    value.add(item)
                                }
                            }

                        }
                    } else {
                        value.addAll(it)
                    }

                    mAdapter.addItems(
                        sortByListOfItems(value.distinctBy { it.qualityParameter.nameChar }),
                        tarWeight,
                        netWeight,
                        challanNo,
                        bagCount,
                        plant,
                        isData!!,
                        if(weighBridgeDetails.sourceLotId?.isEmpty() == true)weighBridgeDetails.batchNumber.toString() else weighBridgeDetails.sourceLotId.toString(),
                        materialObj.complainceFlag
                    )
                }
                else -> setErrorContentView(getString(R.string.quality_params_not_available))
            }
        }
    }

    private fun showConfirmDialog(batchNo: String, msg: Int, finalApproval: String) {
        //callBack?.replaceFragment(SUMMARY_LIST)

        mListener.onParamsProceedQuality(
            qualityParameterList,
            wbId,
            batchNo,
            finalApproval,
            storageLocationCode,
            materialObj.complainceFlag
        )


        /* MaterialDialog(requireContext()).show {
             message(msg)
             positiveButton(text = UIUtils.getSpannedText(getString(R.string.proceed), true)) {
                 //calculatePaidWeight()
                 mListener.onParamsProceed(qualityParameterList, wbId, batchNo, finalApproval)
             }
             negativeButton(text = UIUtils.getSpannedText(getString(R.string.cancel), false)) {
                 dismiss()
             }
         }*/
    }

    private fun calculatePaidWeight() {
        qualityParameterList.forEach { it ->
            if (it?.nameChar.equals("B_GRNQTY1")) {
                grnQty = it?.qualityParameterValue?.toDouble() ?: 0.0
            } else if (it?.nameChar.equals("B_SECONDARY_REFR")) {
                refraction = it?.qualityParameterValue?.toDouble() ?: 0.0
            }
        }
        discWeight = 100 - refraction
        paidWeight = ((discWeight * grnQty) / 100).formatThreeDigits()
    }

    private fun enableProceedBtn(item: List<VegaQualityParamsWithQualitative?>) {
        var isEnable = false
        item.forEach {
            it?.qualityParameter?.qualityParameterValue?.let { it1 ->
                if (it1.isNotEmpty()) isEnable = true
            }
        }
        if (isEnable) {
            binding.btnParamsProceed.isEnabled = true
            binding.btnParamsProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btnParamsProceed.isEnabled = false
            binding.btnParamsProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }
    }

    override fun clickOnItem(data: String, isWh: Boolean) {
        customDialog?.dismiss()
        binding.tvReceivingLocation.text = data
        //receivingPlant = data
    }
}
