package com.olam.warehouse.vegax.qualitynigeria.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.common.model.VegaBatchDetails
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.WB_ID
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitynigeria.R
import com.olam.warehouse.vegax.qualitynigeria.databinding.FragmentVegaNigeriaQualitySummaryBinding
import com.olam.warehouse.vegax.qualitynigeria.databinding.ItemVegaNigeriaQualitySummaryParamsBinding
import com.olam.warehouse.vegax.qualitynigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.Calendar

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */

class VegaNigeriaQualitySummaryFragment : BaseFragment() {

    //private var approveQuality = mutableListOf<VegaApproveQuality>()
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var vegaBatchDetailsList = arrayListOf<VegaBatchDetails?>()
    private lateinit var binding: FragmentVegaNigeriaQualitySummaryBinding
    private val vm: VegaNigeriaQualityViewModel by viewModel()
    private var wbid: String? = ""
    private var batchNo: String? = ""
    private var finalApproval: String? = ""
    private var challanNo: String? = ""
    private var flag: String? = ""
    private var mouldValue: String? = ""
    private var addMixture: String? = ""
    private var beanWtInGram: String? = ""
    private var beanCount: String? = ""
    private var slaty: String? = ""
    private var plantId: String? = ""
    private var moisture: String? = ""
    private var storageLocationCode: String? = ""
    private var material: String = ""
    private var quality_value: String = ""
    private var quality_value_one: String = ""
    private var quality_valueList = arrayListOf<String>()
    private var quality_value_oneList = arrayListOf<String>()
    private var isData: Boolean? = false
    private lateinit var mListener: OnSummaryParamsListener
    private var qualityMasterData = mutableListOf<VegaQualityParamsWithQualitative>()
    private var lotItems = arrayListOf<VegaCoffeeLot>()
    private var lotDetails = VegaCoffeeLot()

    interface OnSummaryParamsListener {
        fun onParamsProceed(
            qualityParameter: ArrayList<VegaQualityParameter?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String,
            challan: String
        )

        fun onMtnrParamsProceed(
            qualityParameter: ArrayList<VegaQualityParameter?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String,
            lotItems: ArrayList<VegaCoffeeLot>,
            lotDetails: VegaCoffeeLot
        )
    }

    override val layoutResourceId = R.layout.fragment_vega_nigeria_quality_summary

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVegaNigeriaQualitySummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    companion object {
        fun newInstance() = VegaNigeriaQualitySummaryFragment().putArgs {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualitynigeria/ui/VegaNigeriaQualitySummaryFragment")
            .title("Approve").with(tracker)
        initUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnSummaryParamsListener
    }

    private fun initUI() {

        wbid = arguments?.getString(WB_ID)
        batchNo = arguments?.getString(BATCH_NO)
        finalApproval = arguments?.getString(FINAL_APPROVAL)
        challanNo = arguments?.getString(CHALLAN)
        flag = arguments?.getString(FLAG)
        plantId = arguments?.getString(PLANTID)
        storageLocationCode = arguments?.getString(STORAGELOCATION_CODE)
        qualityParameterList =
            arguments?.getParcelableArrayList<VegaQualityParameter>(APPROVE_QUALITY_DATA) as ArrayList<VegaQualityParameter?>

        if(getCurrentKey().contains("VEGA_NG") && getCurrentKey().contains("CASH")){
            binding.llBatch.gone()
            setUpQualityAdapter(qualityParameterList)
        }

        if(getCurrentKey().contains("VEGA_NG") && getCurrentKey().contains("COCO")){
            mouldValue = arguments?.getString(MOULD_VALUE)
            addMixture = arguments?.getString(ADD_MIXTURE)
            beanWtInGram = arguments?.getString(BEAN_WT_GRAM)
            beanCount = arguments?.getString(BEAN_COUNT)
            slaty = arguments?.getString(SLATY)
            moisture = arguments?.getString(MOISTURE)
            vegaBatchDetailsList =
                arguments?.getParcelableArrayList<VegaBatchDetails>(APPROVE_BATCH_DETAILS) as ArrayList<VegaBatchDetails?>
        }


        if (flag == "MTNR") {
            lotItems =
                arguments?.getParcelableArrayList<VegaCoffeeLot>("LOT_LIST") as ArrayList<VegaCoffeeLot>
            lotDetails = arguments?.getParcelable("LOT_DETAILS")!!
            material = "000000".plus(lotDetails.materialCode!!)
        } else {
            material = qualityParameterList[0]?.materialCode.toString()
        }



        vm.getQualityParams(material, isData, wbid)
        vm.qualitylist.observe(viewLifecycleOwner, Observer { it ->
            qualityMasterData = it.toMutableList()

            if(getCurrentKey().contains("VEGA_NG") && getCurrentKey().contains("COCO")) {
               // var list = qualityParameterList.filter { it1 -> it1?.nameChar != "B_GRNQTY1" }
                // setUpAdapter(list as ArrayList<VegaQualityParameter?>)
                setUpAdapter(vegaBatchDetailsList)
            }
        })
        if (flag == "STO") {
            binding.tvWeighBID.text = getString(R.string.wb_id)
            binding.tvApproveParamsWeighBID.text = wbid
        } else if (((vegaBatchDetailsList.size > 0) && (PreferenceHelper.get(Constants.WERKS, "")
                .equals("6154") || PreferenceHelper.get(Constants.WERKS, "").equals("6155")))
        ) {
            binding.tvWeighBID.text = getString(R.string.secret_id)
            binding.tvApproveParamsWeighBID.text = challanNo
        } else {
            binding.tvWeighBID.text = getString(R.string.wb_id)
            binding.tvApproveParamsWeighBID.text = wbid
        }
        var currentDate = DateUtils.formatDate(DateUtils.getDate())
        var currentMonth = currentDate.split("-")[1]
        var currentYear = (currentDate.split("-")[0])
        var twoDigitsYear = currentYear.substring(currentYear.length - 2)
        var loggedInPlantId = ((PreferenceHelper.get(
            Constants.WERKS,
            ""
        )).substring((PreferenceHelper.get(Constants.WERKS, "")).length - 2))

        var qualityCode = getQualityCode(
            (if (beanWtInGram?.replace("%", "")?.trim()
                    .isNullOrEmpty()
            ) 0.0 else (beanWtInGram?.replace("%", "")?.replace(",",".")?.trim())?.toDouble()) ?: 0.0,
            (if (mouldValue?.replace("%", "")?.trim()
                    .isNullOrEmpty()
            ) 0.0 else (mouldValue?.replace("%", "")?.replace(",",".")?.trim())?.toDouble()) ?: 0.0,
            (if (addMixture?.replace("%", "")?.trim()
                    .isNullOrEmpty()
            ) 0.0 else (addMixture?.replace("%", "")?.replace(",",".")?.trim())?.toDouble()) ?: 0.0
        )
        var thresholdLimit = getQualityThresholdLimit(
            (if (beanCount?.replace("%", "")?.trim()
                    .isNullOrEmpty()
            ) 0.0 else (beanCount?.replace("%", "")?.replace(",",".")?.trim())?.toDouble()) ?: 0.0,
            (if (moisture?.replace("%", "")?.trim()
                    .isNullOrEmpty()
            ) 0.0 else (moisture?.replace("%", "")?.replace(",",".")?.trim())?.toDouble()) ?: 0.0,
            (if (mouldValue?.replace("%", "")?.trim()
                    .isNullOrEmpty()
            ) 0.0 else (mouldValue?.replace("%", "")?.replace(",",".")?.trim())?.toDouble()) ?: 0.0,
            (if (slaty?.replace("%", "")?.trim().isNullOrEmpty()) 0.0 else (slaty?.replace("%", "")?.replace(",",".")
                ?.trim())?.toDouble()) ?: 0.0,
            (if (addMixture?.replace("%", "")?.trim()
                    .isNullOrEmpty()
            ) 0.0 else (addMixture?.replace("%", "")?.replace(",",".")?.trim())?.toDouble()) ?: 0.0
        )

        /* var qualityCode = getQualityCode(291.0,6.0,3.4)
         var thresholdLimit = getQualityThresholdLimit(251.0,9.0,9.0,14.0,4.0)
        */
       /* var twoDigitsLocationCode =
            (storageLocationCode.toString()).substring((((storageLocationCode.toString())).length) - 2)*/
        //batchNo = twoDigitsYear.plus(currentMonth).plus(qualityCode).plus("-").plus(twoDigitsLocationCode)
        // batchNo = loggedInPlantId.plus(twoDigitsYear).plus(currentMonth).plus(qualityCode).plus("-")
       // batchNo = loggedInPlantId.plus(twoDigitsYear).plus(currentMonth).plus(qualityCode)
        binding.tvBatchNo.text = batchNo

        binding.btnOkApprove.setOnClickListener {
            var fnQuality = ""
            if (thresholdLimit == true) {
                fnQuality = FNQUALITY_THRESHOLD
            } else {
                fnQuality = FNQUALITY
            }
            proceedToPost(fnQuality, R.string.confirm_quality_message)
        }

        // vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        // vm.getConfigItems(UserRoles.QUALITY.role)

    }

    /*private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        quality_valueList.clear()
        quality_value_oneList.clear()
        configItems?.forEach {
            when (it.process) {
                ConfigItems.CREATE_LOT.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> {
                            quality_valueList.add(it.value.toString())
                            quality_value_oneList.add(it.value1.toString())
                        }
                    }
                }
            }
        }
        var count = -1;
        quality_valueList?.forEach {
            var bcValue = it.split(",")[0].trim()
            var tmValue = it.split(",")[1].trim()
            var admValue = it.split(",")[2].trim()
            var minbcvalue:String? = "0" ; var maxbcvalue:String? = "0";  var mintmValue:String? = "0"; var maxtmValue:String? = "0"; var minadmValue:String? = "0";var maxadmValue:String? = "0"

            if(bcValue.length>0 && bcValue.length>=10) { maxbcvalue = bcValue.substring(bcValue.length - 3) ; minbcvalue = bcValue.substring(0, 3)} else if(bcValue.length>0){ minbcvalue = "0";maxbcvalue = bcValue.substring(0, 3)} else { minbcvalue = "0"; maxbcvalue = "0"; }
            if(tmValue.length>0 && tmValue.length>=8)   { maxtmValue = tmValue.substring(tmValue.length - 2) ; mintmValue = tmValue.substring(0, 1)} else if(tmValue.length>0 && tmValue.trim().length==4){ mintmValue = "0";maxtmValue =tmValue.substring(tmValue.length - 1)}  else if(tmValue.length>0){ mintmValue = "0";maxtmValue =tmValue.substring(tmValue.length - 2)} else { mintmValue = "0"; maxtmValue = "0"; }
            if(admValue.length>0 && admValue.length>=7) { if(admValue.substring(3,4).equals("<")) { minadmValue = "0"; maxadmValue = admValue.substring(admValue.length - 3)}} else if(admValue.length>0 && admValue.length==5) {  minadmValue = "-1"; maxadmValue = "1000";} else if(admValue.length>0){ if(admValue.substring(3,4).equals(">")) {maxadmValue = admValue.substring(admValue.length - 3); minadmValue = "0";} }else { minadmValue = "0"; maxadmValue = "0";}

            var originalBCValue: String? = "291"; var originalTMValue: String? ="6"; var originalADMValue: String? ="3.4"

                if((originalBCValue?.toInt()!! <= maxbcvalue.toInt() && (originalBCValue?.toInt()!! >= minbcvalue.toInt())) &&
                    (originalTMValue?.toInt()!! <= maxtmValue.toInt() && (originalTMValue?.toInt()!! >= mintmValue.toInt())) &&
                    (originalADMValue?.toInt()!! <= maxadmValue?.toInt()!! && (originalADMValue?.toInt()!! >= minadmValue?.toInt()!!))){
                            return
                }
            count++;
            //originalBCValue?.let { it1 -> inRange(minbcvalue,maxbcvalue, it1) }

        }

       // var sgfdfg = quality_value_oneList.get(count)
       // binding.tvBatchNo.text = sgfdfg
    }*/

    private fun inRange(lowerBound: String, upperBound: String, input: String): Boolean {
        return input.compareTo(lowerBound) >= 0 && input.compareTo(upperBound) <= 0
    }

     private fun setUpQualityAdapter(data: ArrayList<VegaQualityParameter?>) {
         data.let {
             var i = 0
             binding.rvApproveQuality.setUpAdapter(data,
                     R.layout.item_vega_nigeria_quality_summary_params,
                 ItemVegaNigeriaQualitySummaryParamsBinding::inflate,
                     { item, pos,bindItem ->
                         i++
                         if (i % 2 == 0) {
                             this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                         } else {
                             this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                         }

                         bindItem.tvQualityNameApprove.text = item?.descrChar

                         var spinnerItem = qualityMasterData.filter { it.qualitative?.size ?: 0 > 0 }

                         var slectedItem =
                                 spinnerItem.filter { it.qualityParameter.descrChar == item?.descrChar }

                         if (item?.qualitative?.size!! > 0) {
                             bindItem.tvUnitApprove.text = item.qualityParameterValue.plus(" ").plus(item.unitsOfMeasure)
                         } else
                             bindItem.tvUnitApprove.text = item.qualityParameterValue.plus(" ").plus(item.unitsOfMeasure)
                         if (slectedItem.size > 0) {
                             var i =
                                     slectedItem[0].qualitative?.filter { it.charValue == item.qualityParameterValue }

                             bindItem.tvUnitApprove.text = i?.get(0)?.descValue.plus(" ").plus(item.unitsOfMeasure)
                         }

                     },
                     {

                     })
         }
     }

    private fun setUpAdapter(data: ArrayList<VegaBatchDetails?>) {
        var qualityParams = data.filterNot {
            it?.atnam.equals("ZNGCOCOA_DIS_MOULD") || it?.atnam.equals("ZNGCOCOA_DIS_BW") ||
                    it?.atnam.equals("ZNGCOCOA_DIS_ON_OTHERS") || it?.atnam.equals("ZNGCOCOA_DIS_BS")
        } as ArrayList<VegaBatchDetails?>
        qualityParams.let {
            var i = 0
            binding.rvApproveQuality.setUpAdapter(
                qualityParams,
                R.layout.item_vega_nigeria_quality_summary_params,
                ItemVegaNigeriaQualitySummaryParamsBinding::inflate,
                { item, pos, bindItem ->
                    i++
                    if (i % 2 == 0) {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                    } else {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                    }

                    bindItem.tvQualityNameApprove.text = item?.desc

                    /* var spinnerItem = qualityMasterData.filter { it.qualitative?.size ?: 0 > 0 }

                     var slectedItem =
                         spinnerItem.filter { it.qualityParameter.descrChar == item?.desc }*/

                    //if (item?.qualitative?.size!! > 0) {
                    bindItem.tvUnitApprove.text = item?.atwtb?.replace(",",".")
                   // tvUnitApprove.text = item?.atwtb?.replace(",",".")
                    /*  } else
                          tvUnitApprove.text = item.qualityParameterValue.plus(" ").plus(item.unitsOfMeasure)
                      if (slectedItem.size > 0) {
                          var i =
                              slectedItem[0].qualitative?.filter { it.charValue == item.qualityParameterValue }

                          tvUnitApprove.text = i?.get(0)?.descValue.plus(" ").plus(item.unitsOfMeasure)
                      }*/

                },
                {

                })
        }
    }

    private fun proceedToPost(finalApproval: String, msg: Int) {
        showConfirmDialog(batchNo!!, msg, finalApproval)
    }

    private fun batchCreation(){
        val countValue= qualityParameterList.find { it?.nameChar=="ZNG_CASHEW_COUNT"}?.qualityParameterValue.toString()
        val korValue= qualityParameterList.find { it?.nameChar=="ZNG_CASHEW_KOR"}?.qualityParameterValue.toString()
        val qcCode= getNgCashQualityCode(korValue, countValue)

        val  cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val mouth = cal.get(Calendar.MONTH)
        val twoDigitsYear = year.toString().substring(year.toString().length - 2)
        val loggedInPlantId = ((PreferenceHelper.get(
            Constants.WERKS,
            ""
        )).substring((PreferenceHelper.get(Constants.WERKS, "")).length - 2))

         batchNo = loggedInPlantId.plus(twoDigitsYear).plus(mouth).plus(qcCode)
    }
    private fun showConfirmDialog(batch: String, msg: Int, finalApproval: String) {
        var batchNum= batch
        if(getCurrentKey().contains("VEGA_NG") && getCurrentKey().contains("CASH")){
            batchCreation()
         batchNum= batchNo.toString()
        }
        MaterialDialog(requireContext()).show {
            message(msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (flag == "PROCURE")
                        mListener.onParamsProceed(
                            qualityParameterList,
                            wbid,
                            batchNum,
                            finalApproval,
                            challanNo.toString()
                        )
                    else
                        mListener.onParamsProceed(
                            qualityParameterList,
                            wbid,
                            batchNum,
                            "Q",
                            challanNo.toString()
                        )

                },
                { dismiss() })
        }
    }


}
