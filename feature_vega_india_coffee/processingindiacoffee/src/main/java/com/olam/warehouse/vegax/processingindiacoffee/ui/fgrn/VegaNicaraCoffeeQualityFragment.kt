package com.olam.warehouse.vegax.processingindiacoffee.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.master.vegacocoa.model.VegaNicaraguaFgrnQuality
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingindiacoffee.R
import com.olam.warehouse.vegax.processingindiacoffee.databinding.FragmentVegaNicaraguaQualityParamsBinding
import com.olam.warehouse.vegax.processingindiacoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNicaraCoffeeQualityFragment : BaseFragment() {

    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var mAdapter = VegaNicaraguaCoffeeQualityAdapter { enableProceedBtn(it) }
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private val vm: VegaIndiaCoffeeFgrnViewModel by viewModel()
    private var lotItems = arrayListOf<VegaCoffeeLot>()
    private var qualityPostList = arrayListOf<VegaCoffeeLot>()
    private var qualitySupplierPostList = arrayListOf<VegaQualityWBDetails>()
    val value = mutableListOf<VegaQualityParamsWithQualitative>()
    var tallySheet: String? = ""
    var tallySequence: String? = ""
    private var model = VegaCocoaFgrnItems()
    private var fgrnId: String = ""
    var material_code = ""
    private var batchNo1: String? = ""
    private var isData: Boolean? = false
    private var materialNo: String? = ""
    private var netWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var callBack: VegaNicaraCoffeeQualityFragment.CallBack? = null
    private var qualityParameterNicList = arrayListOf<VegaNicaraguaFgrnQuality>()
    private val plantId = getPlantDetails().plantId
    private var materialcode = ""

    interface CallBack {
        fun replaceFgrnFragment(fragment: String, model: VegaCocoaFgrnItems, id: String)
    }

    companion object {
        fun newInstance(model: VegaCocoaFgrnItems, id: String) =
            VegaNicaraCoffeeQualityFragment().putArgs {
                putParcelable(FRAG_LOTS, model)
                putString(FRAG_ID, id)
            }
    }

    private lateinit var binding: FragmentVegaNicaraguaQualityParamsBinding
    override val layoutResourceId = R.layout.fragment_vega_nicaragua_quality_params

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVegaNicaraguaQualityParamsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("quality/ui/params/VegaNicaraCoffeeQualityFragment")
            .title("Quality")
            .with(tracker)
        initUI()
        initExtra()

    }

    private fun initUI() {
        model = arguments?.getParcelable(FRAG_LOTS) ?: VegaCocoaFgrnItems()
        fgrnId = arguments?.getString(FRAG_ID) ?: ""
        binding.rvBagDetail.layoutManager = LinearLayoutManager(this.context)
        binding.rvBagDetail.adapter = mAdapter
        binding.etBatchNo.filters = arrayOf<InputFilter?>(LengthFilter(11))
        binding.etBatchNo.setText(batch_no, TextView.BufferType.EDITABLE)
        binding.btnParamsProceed.setOnClickListener { proceeToPost() }
    }

    private fun initExtra() {
        model.rminList?.forEach {
            tallysheet = it.batchNumber.toString()
            /*materialNo =
                if (!it.materialCode?.length?.equals(18)!!) "000000".plus(it.materialCode) else it.materialCode*/
        }
        materialNo =
            if (!material_no.length.equals(18)) "000000".plus(material_no) else material_no
        vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })
        vm.getPreSamplingQualitydata(batch_no.trim(), materialNo?.trim()!!)
        //vm.getPreSamplingQualitydata(batch_no.trim(), model.materialCode?.trim()!!)
        vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun proceeToPost() {
        var isValueNeed = true
        batchNo1 = binding.etBatchNo.text.toString()

        qualityParameterList.clear()
        val missedPos = mutableListOf<Int>()
        var data = arrayListOf<VegaQualityParamsWithQualitative?>()
        data = mAdapter.getItems()
        mAdapter.getItems().forEachIndexed { index, itValue ->
            var qualityData = VegaNicaraguaFgrnQuality()
            qualityData.materialCode = materialNo.toString()
            qualityData.plantId = getPlantDetails().plantId
            qualityData.storageLocationCode = storageLocation
            qualityData.nameCharValue = itValue?.qualityParameter?.nameChar.toString()
            qualityData.descrCharValue = itValue?.qualityParameter?.descrChar.toString()
            qualityData.qualityParameterValue =
                itValue?.qualityParameter?.qualityParameterValue.toString()
            qualityData.batchNumber = batch_no

            if ((itValue?.qualityParameter?.entryObligatory.equals("X")) || (itValue?.qualityParameter?.vegaMandatory.equals(
                    "X"
                )) || (!itValue?.qualityParameter?.preSampling.equals(""))
            ) {
                if (itValue?.qualityParameter?.qualityParameterValue.isNullOrEmpty()) {
                    if (getCurrentKey().split("_")[1].contains("NI")) {
                        if (itValue?.qualityParameter?.formulaParam.equals("X")) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else if (itValue?.qualityParameter?.nameChar.equals("NIDANO")) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else if (itValue?.qualityParameter?.nameChar.equals("NIFG0014")) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else {
                            isValueNeed = false
                            missedPos.add(index)
                            itValue?.qualityParameter?.mandatory = 1
                        }
                    } else {
                        if (itValue?.qualityParameter?.formulaParam.equals("X")) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else {
                            isValueNeed = false
                            missedPos.add(index)
                            itValue?.qualityParameter?.mandatory = 1
                        }
                    }

                } else {
                    itValue?.qualityParameter?.mandatory = 0
                }
                qualityParameterList.add(itValue?.qualityParameter)
            }
            qualityParameterNicList.add(qualityData)
        }
        if (isValueNeed) {
            paramsNicaraguaList = qualityParameterNicList
            println("nnnccbbuttz=> $qualityParameterNicList")
            callBack?.replaceFgrnFragment(FRAG_SIFFT, model, "")
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
                            if (item.qualityParameter.vegaMandatory.equals("X") || item.qualityParameter.entryObligatory.equals(
                                    "X"
                                ) || (!item.qualityParameter.preSampling.equals("") && item.qualityParameter.qualityParameterValue?.isNotEmpty()!!)
                            ) {
                                value.add(item)
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
                        "",
                        "",
                        ""
                    )
                }
                else -> setErrorContentView("Quality params not available for this material")
            }
        }
    }

    private fun updatePreQuality(response: Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            preQualityList =
                                it.data?.data?.get(0)?.qualityParameters as MutableList<VegaQualityParams>
                            materialNo?.let { vm.getQualityParams(materialNo!!, false, "") }
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
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
            binding.btnParamsProceed.setBackgroundColor(
                getColor(
                    if (getCurrentOriginEntity().contains(
                            "OFI"
                        )
                    ) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green
                )
            )
        } else {
            binding.btnParamsProceed.isEnabled = false
            binding.btnParamsProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }
    }

}
