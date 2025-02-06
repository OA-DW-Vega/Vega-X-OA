 package com.olam.warehouse.vegax.qualityecuador.ui.params

import android.content.Context
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
import com.olam.warehouse.master.common.model.Material
import com.olam.warehouse.master.dorigin.entity.DOMaterial
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorQualitySavedResponse
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityecuador.R
import com.olam.warehouse.vegax.qualityecuador.databinding.FragmentVegaEcuadorQualityParamsBinding
import com.olam.warehouse.vegax.qualityecuador.ui.VegaEcuadorQualityViewModel
import com.olam.warehouse.vegax.qualityecuador.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

 /**
  * Created by Keerthi Santhanam on 6/27/2020.
  */
 class VegaEcuadorQualityParameterFragment : BaseFragment() {

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
     private var mAdapter = VegaEcuadorQualityParamsAdapter { enableProceedBtn(it) }
     private var isSort: Boolean = false
     private var custonLocationList = mutableListOf<VegaCustomStLocation>()
     private var preQualityList = mutableListOf<VegaQualityParams>()
     private val vm: VegaEcuadorQualityViewModel by viewModel()
     private lateinit var mListener: OnParamsListener
     private var paidWeight: String = ""
     private var grnQty: Double = 0.0
     private var refraction: Double = 0.0
     private var discWeight: Double = 0.0
     private var grnNumber: String? = ""
     private var grnPrice: String? = ""
     private var materialObj = VegaMaterial()


     interface OnParamsListener {
         fun onParamsProceed(
             qualityParameter: ArrayList<VegaQualityParameter?>,
             wbId: String?,
             batchNo: String,
             finalApproval: String,
             eudrStatus:Boolean
         )
     }

    companion object {
        fun newInstance() = VegaEcuadorQualityParameterFragment().putArgs {}
    }

    private lateinit var binding: FragmentVegaEcuadorQualityParamsBinding
    override val layoutResourceId = R.layout.fragment_vega_ecuador_quality_params

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
        binding = FragmentVegaEcuadorQualityParamsBinding.inflate(layoutInflater)
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
        binding.btnParamsProceed.setOnClickListener { proceedToPost(FNQUALITY, R.string.confirm_quality_message) }
        binding.btnAccept.setOnClickListener { proceedToPost(FNQUALITY, R.string.confirm_quality_message) }
        binding.btnReject.setOnClickListener { proceedToPost(FNREJECT, R.string.confirm_reject_message) }
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
            grnPrice = weighBridgeDetails.unitPrice
        }
        binding.tvParamsWeighBID.text = getString(R.string.wb_id).plus(" ").plus(wbId)
        if(!grnNumber.isNullOrEmpty() && !grnNumber!!.contains("TMP"))
            binding.tvGrn.text = getString(com.olam.warehouse.presentation.R.string.grn).plus(grnNumber)
        else binding.tvGrn.gone()
        binding.etBatchNo.setText(batchNo, TextView.BufferType.EDITABLE)
        binding.etBatchNo.isEnabled = false

        materialNo?.let {
            vm.getQualityParams(materialNo!!, isData, wbId)
           /* vm.qualitylist.observe(viewLifecycleOwner, Observer {
                updateUI(it)
            })*/
        }
        if(materialNo?.isNotEmpty() == true){
            materialNo?.takeLast(12)?.let { vm.getSAPMaterialUsingMaterialCode(it) }
            vm.sapMaterialUsingMaterialCode.observe(viewLifecycleOwner, Observer{
                it?.let {
                    materialObj = it
                }
            })
        }
        vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })

        if (wbType.equals(PROCURE)) {
            binding.tvType.text = (" ").plus(getString(R.string.supplier))
        } else {
            binding.tvType.text = MTNR
        }
        enableProceedBtn(weighBridgeDetails.status)
        vm.fetchSavedWeighBridgeDetail(plant!!, materialNo!!, batchNo!!)
//        vm.qualitySaved.observe(viewLifecycleOwner, Observer { response ->
//            response.let { savedResponse ->
//                hideCustomLoading()
//                when (savedResponse.status) {
//                    Resource.Status.SUCCESS -> {
//                        when (savedResponse.data?.success) {
//                            true -> {
//                                materialNo?.let {
//                                    vm.getQualityParams(materialNo!!, isData, wbId)
//                                    vm.qualitylist.observe(viewLifecycleOwner, Observer { param ->
//                                        updatequalityUI(param, savedResponse?.data?.data!!)
//                                    })
//                                }
//                            } else -> activity?.toast("Error ${savedResponse.data?.message}")
//                        }
//                    }
//                    Resource.Status.ERROR -> {
//                        activity?.toast("${savedResponse.error}")
//                    }
//                    Resource.Status.LOADING -> {
//                    }
//                }
//            }
//        })

    }

     private fun updatequalityUI(
         params: List<VegaQualityParamsWithQualitative>?,
         savedQualityData: VegaEcuadorQualitySavedResponse? = null
     ) {
         hideCustomLoading()
         params?.let {
             it.forEach { item ->
                 if (item.qualityParameter.nameChar.equals("B_GRNPRICE1"))
                     item.qualityParameter.qualityParameterValue =
                         savedQualityData?.qualityParameters?.get(0)?.qualityParameterValue
                 item.qualitative = item.qualitative?.filter { it.materialCode.equals(materialNo) }
             }
             when (it.isNotEmpty()) {
                 true -> {
                     mAdapter.addItems(
                         sortByListOfItems(it),
                         tarWeight,
                         netWeight,
                         challanNo,
                         bagCount,
                         plant,
                         isData!!,
                         if(weighBridgeDetails.sourceLotId?.isNotEmpty() == true)weighBridgeDetails.sourceLotId else weighBridgeDetails.batchNumber,
                         materialObj.complainceFlag
                     )
                 }
                 else -> {
                     binding.btnParamsProceed.gone()
                     setErrorContentView(getString(R.string.quality_params_not_available))
                 }
             }
         }
     }

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

    private fun proceedToPost(finalApproval: String, msg: Int) {
        var isValueNeed = true
        qualityParameterList.clear()
        val missedPos = mutableListOf<Int>()
        var data = arrayListOf<VegaQualityParamsWithQualitative?>()
        data = mAdapter.getItems()
        mAdapter.getItems().forEachIndexed { index, itValue ->
            if ((itValue?.qualityParameter?.vegaMandatory.equals("X"))  || (!itValue?.qualityParameter?.preSampling.isNullOrEmpty())
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
        if (isValueNeed)
            showConfirmDialog(batchNo!!, msg, finalApproval)
        else {
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
                            if (item.qualityParameter.vegaMandatory.equals("X")|| item.qualityParameter.vegaMandatory.equals("N") || item.qualityParameter.entryObligatory.equals(
                                    "X"
                                ) || (!item.qualityParameter.preSampling.equals("") && item.qualityParameter.qualityParameterValue?.isNotEmpty()!!)
                            ) {
                                if (item.qualityParameter.nameChar.equals("B_GRNPRICE1")) {
                                    if (!grnPrice.isNullOrEmpty()) {
                                        item.qualityParameter.qualityParameterValue = grnPrice
                                    }

                                }
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
                        tarWeight,
                        netWeight,
                        challanNo,
                        bagCount,
                        plant,
                        isData!!,
                        if(weighBridgeDetails.sourceLotId?.isNotEmpty() == true)weighBridgeDetails.sourceLotId else weighBridgeDetails.batchNumber,
                        materialObj.complainceFlag
                    )
                }
                else -> setErrorContentView(getString(R.string.quality_params_not_available))
            }
        }
    }

    private fun showConfirmDialog(batchNo: String, msg: Int, finalApproval: String) {
        MaterialDialog(requireContext()).show {
            message(msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    //calculatePaidWeight()
                    mListener.onParamsProceed(qualityParameterList, wbId, batchNo, finalApproval, if(materialObj?.complainceFlag?.equals("Compliant")==true) true else false)
                },
                { dismiss() })
        }
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
}
