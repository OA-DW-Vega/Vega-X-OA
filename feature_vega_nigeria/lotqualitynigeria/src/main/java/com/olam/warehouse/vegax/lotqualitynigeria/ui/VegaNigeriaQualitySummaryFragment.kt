package com.olam.warehouse.vegax.lotqualitynigeria.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.common.model.VegaBatchDetails
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.WB_ID
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.lotqualitynigeria.R
import com.olam.warehouse.vegax.lotqualitynigeria.databinding.FragmentVegaNigeriaCocoaLotQualitySummaryBinding
import com.olam.warehouse.vegax.lotqualitynigeria.utils.*
import kotlinx.android.synthetic.main.item_vega_nigeria_quality_lot_summary_params.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */

class VegaNigeriaQualitySummaryFragment : BaseFragment() {

    //private var approveQuality = mutableListOf<VegaApproveQuality>()
    private var qualityParameterList = arrayListOf<VegaQualityParams?>()
    private var vegaBatchDetailsList = arrayListOf<VegaBatchDetails?>()
    private lateinit var binding: FragmentVegaNigeriaCocoaLotQualitySummaryBinding
    private val vm: VegaCocoaLotQualityViewModel by viewModel()
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
            qualityParameter: ArrayList<VegaQualityParams?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String,
            challan: String
        )

        fun onMtnrParamsProceed(
            qualityParameter: ArrayList<VegaQualityParams?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String,
            lotItems: ArrayList<VegaCoffeeLot>,
            lotDetails: VegaCoffeeLot
        )
    }

    override val layoutResourceId = R.layout.fragment_vega_nigeria_cocoa_lot_quality_summary

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVegaNigeriaCocoaLotQualitySummaryBinding.inflate(layoutInflater)
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
        /*val wbid = arguments?.get(APPROVE_DATA) as VegaApproveWeighBridgeId
        approveQuality =
            arguments?.getParcelableArrayList<VegaApproveQuality>(ccc) as MutableList<VegaApproveQuality>
        binding.tvApproveParamsWeighBID.text = wbid

        binding.btnOkApprove.setOnClickListener { activity?.onBackPressed() }

        setUpAdapter(approveQuality)*/

        wbid = arguments?.getString(WB_ID)
        batchNo = arguments?.getString(BATCH_NO)
        finalApproval = arguments?.getString(FINAL_APPROVAL)
        challanNo = arguments?.getString(CHALLAN)
        flag = arguments?.getString(FLAG)
        mouldValue = arguments?.getString(MOULD_VALUE)
        addMixture = arguments?.getString(ADD_MIXTURE)
        beanWtInGram = arguments?.getString(BEAN_WT_GRAM)
        beanCount = arguments?.getString(BEAN_COUNT)
        slaty = arguments?.getString(SLATY)
        moisture = arguments?.getString(MOISTURE)
        storageLocationCode = arguments?.getString(STORAGELOCATION_CODE)
        qualityParameterList =
            arguments?.getParcelableArrayList<VegaQualityParams>(APPROVE_QUALITY_DATA) as ArrayList<VegaQualityParams?>

        vegaBatchDetailsList =
            arguments?.getParcelableArrayList<VegaBatchDetails>(APPROVE_BATCH_DETAILS) as ArrayList<VegaBatchDetails?>
        //  intent.getParcelableArrayListExtra<VegaNicaraguaWeighmentBagMaterial>(UIUtils.BAGS_DATA)

        if (flag == "MTNR") {
            lotItems =
                arguments?.getParcelableArrayList<VegaCoffeeLot>("LOT_LIST") as ArrayList<VegaCoffeeLot>
            lotDetails = arguments?.getParcelable("LOT_DETAILS")!!
            material = "000000".plus(lotDetails.materialCode!!)
        } else {
            // material = qualityParameterList[0]?.materialCode.toString()
        }

        //vm.getQualityParams(material, isData, wbid)
        /* vm.qualitylist.observe(viewLifecycleOwner, Observer { it ->
             qualityMasterData = it.toMutableList()*/

        // var list = qualityParameterList.filter { it1 -> it1?.nameChar != "B_GRNQTY1" }
        // setUpAdapter(list as ArrayList<VegaQualityParams?>)
        setUpAdapter(vegaBatchDetailsList)
        //})
        binding.tvApproveParamsWeighBID.text = wbid
        var currentDate = DateUtils.formatDate(DateUtils.getDate())
        var currentMonth = currentDate.split("-")[1]
        var currentYear = (currentDate.split("-")[0])
        var twoDigitsYear = currentYear.substring(currentYear.length - 2)

        /*  var qualityCode = getQualityCode(( if (beanWtInGram?.replace("%","")?.trim().isNullOrEmpty()) 0.0 else (beanWtInGram?.replace("%","")?.trim())?.toDouble()) ?: 0.0,
              ( if (mouldValue?.replace("%","")?.trim().isNullOrEmpty()) 0.0 else (mouldValue?.replace("%","")?.trim())?.toDouble()) ?: 0.0,
              ( if (addMixture?.replace("%","")?.trim().isNullOrEmpty()) 0.0 else (addMixture?.replace("%","")?.trim())?.toDouble()) ?: 0.0)
          var thresholdLimit = getQualityThresholdLimit(( if (beanCount?.replace("%","")?.trim().isNullOrEmpty()) 0.0 else (beanCount?.replace("%","")?.trim())?.toDouble()) ?: 0.0,
              ( if (moisture?.replace("%","")?.trim().isNullOrEmpty()) 0.0 else (moisture?.replace("%","")?.trim())?.toDouble()) ?: 0.0,
              ( if (mouldValue?.replace("%","")?.trim().isNullOrEmpty()) 0.0 else (mouldValue?.replace("%","")?.trim())?.toDouble()) ?: 0.0,
              ( if (slaty?.replace("%","")?.trim().isNullOrEmpty()) 0.0 else (slaty?.replace("%","")?.trim())?.toDouble()) ?: 0.0,
              ( if (addMixture?.replace("%","")?.trim().isNullOrEmpty()) 0.0 else (addMixture?.replace("%","")?.trim())?.toDouble()) ?: 0.0)

         *//* var qualityCode = getQualityCode(291.0,6.0,3.4)
        var thresholdLimit = getQualityThresholdLimit(251.0,9.0,9.0,14.0,4.0)
       *//*
        var twoDigitsLocationCode = (storageLocationCode.toString()).substring((((storageLocationCode.toString())).length)-2)
        batchNo = twoDigitsYear.plus(currentMonth).plus(qualityCode).plus("-").plus(twoDigitsLocationCode)*/
        binding.tvBatchNo.text = batchNo

        binding.btnOkApprove.setOnClickListener {
            var fnQuality = ""
            /*if(thresholdLimit==true) {
                fnQuality = FNQUALITY_THRESHOLD
            }else {*/
            fnQuality = FNQUALITY
            //  }
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

    /* private fun setUpAdapter(data: ArrayList<VegaQualityParams?>) {
         data.let {
             var i = 0
             binding.rvApproveQuality.setUp(data,
                     R.layout.item_vega_nigeria_quality_summary_params,
                     { item, pos ->
                         i++
                         if (i % 2 == 0) {
                             this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                         } else {
                             this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                         }

                         tvQualityNameApprove.text = item?.descrChar

                         var spinnerItem = qualityMasterData.filter { it.qualitative?.size ?: 0 > 0 }

                         var slectedItem =
                                 spinnerItem.filter { it.qualityParameter.descrChar == item?.descrChar }

                         if (item?.qualitative?.size!! > 0) {
                             tvUnitApprove.text = item.qualityParameterValue.plus(" ").plus(item.unitsOfMeasure)
                         } else
                             tvUnitApprove.text = item.qualityParameterValue.plus(" ").plus(item.unitsOfMeasure)
                         if (slectedItem.size > 0) {
                             var i =
                                     slectedItem[0].qualitative?.filter { it.charValue == item.qualityParameterValue }

                             tvUnitApprove.text = i?.get(0)?.descValue.plus(" ").plus(item.unitsOfMeasure)
                         }

                     },
                     {

                     })
         }
     }*/

    private fun setUpAdapter(data: ArrayList<VegaBatchDetails?>) {
        data.let {
            var i = 0
            binding.rvApproveQuality.setUp(data,
                R.layout.item_vega_nigeria_quality_lot_summary_params,
                { item, pos ->
                    i++
                    if (i % 2 == 0) {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                    } else {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                    }

                    tvQualityNameApprove.text = item?.desc

                    /* var spinnerItem = qualityMasterData.filter { it.qualitative?.size ?: 0 > 0 }

                     var slectedItem =
                         spinnerItem.filter { it.qualityParameter.descrChar == item?.desc }*/

                    //if (item?.qualitative?.size!! > 0) {
                    tvUnitApprove.text = item?.atwtb
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

    private fun showConfirmDialog(batchNo: String, msg: Int, finalApproval: String) {
        MaterialDialog(requireContext()).show {
            message(msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (flag == "SUPPLIER")
                        mListener.onParamsProceed(
                            qualityParameterList,
                            wbid,
                            batchNo,
                            finalApproval,
                            challanNo.toString()
                        )
                    else
                        mListener.onMtnrParamsProceed(
                            qualityParameterList,
                            wbid,
                            batchNo,
                            finalApproval,
                            lotItems,
                            lotDetails
                        )

                },
                { dismiss() })
        }
    }


}
