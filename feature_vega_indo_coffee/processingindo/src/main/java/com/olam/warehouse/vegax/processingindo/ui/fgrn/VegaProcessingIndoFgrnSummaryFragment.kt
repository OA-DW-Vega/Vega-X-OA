package com.olam.warehouse.vegax.processingindo.ui.fgrn

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnBagCosumption
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaFgrnItemWithGrades
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaProcessingFgrnResponse
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingindo.R
import com.olam.warehouse.vegax.processingindo.databinding.FragmentVegaProcessingIndoFgrnSummaryBinding
import com.olam.warehouse.vegax.processingindo.databinding.ItemVegaProcessingIndoBagConsumpSummaryBinding
import com.olam.warehouse.vegax.processingindo.databinding.ItemVegaProcessingIndoFgrnSummaryBinding
import com.olam.warehouse.vegax.processingindo.utils.FRAG_ADD_WEIGHT_EDIT
import com.olam.warehouse.vegax.processingindo.utils.FRAG_ITEM
import com.olam.warehouse.vegax.processingindo.utils.prepareFgrnPostRequest
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.math.roundToInt

/**
 * Created by Baskaran Kannan on 6/2/2020.
 */
class VegaProcessingIndoFgrnSummaryFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_processing_indo_fgrn_summary
    private lateinit var binding: FragmentVegaProcessingIndoFgrnSummaryBinding
    private val vm: VegaProcessingIndoFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var fgrnItem = VegaCocoaFgrnItems()
    private var fgrnItemWithGrades = VegaCocoaFgrnItemWithGrades()
    private var isRoundOff: Boolean = false
    private var isIndexweighmenttype: Boolean = false

    interface CallBack {
        fun replaceFgrnFragment(fragment: String, model: VegaCocoaFgrnItems, id: String
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCocoaFgrnItems) = VegaProcessingIndoFgrnSummaryFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaProcessingIndoFgrnSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcocoa/ui/fgrn/VegaCocoaFgrnSummaryFragment").title("Processing Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCocoaFgrnItems()

        vm.fgrnItem.observe(viewLifecycleOwner, Observer { updateItems(it) })
        vm.getFgrnItems(fgrnItem.fgrnId)
        binding.tvLotNo.text = fgrnItem.processOrderNo
        binding.tvShift.text = getString(R.string.shift).plus(" ").plus(fgrnItem.shiftSelection)
        binding.tvOperatorName.text = fgrnItem.operatorName
        binding.tvShift.setOnClickListener { activity?.onBackPressed() }
        binding.tvConfirm.setOnClickListener { showConfirmDialog() }
        vm.postFgrn.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaProcessingFgrnResponse>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            val success = it.data?.data
                            updateFgrnStatus(success?.get(0)?.messages?.get(0)?.message, 4)
                            moveToSuccessPage(success)
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    updateFgrnStatus(it.error.toString(), 3)
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }

            }
        }
    }

    private fun updateFgrnStatus(message: String?, status: Int) {
        message?.let { vm.updateFgrnStatus(it, status, fgrnItem.fgrnId) }
    }

    private fun moveToSuccessPage(success: List<VegaCocoaProcessingFgrnResponse>?) {
        val data = success?.map { it.batchNumber }
        val msg = success?.get(0)?.messages?.get(0)?.message
        var message = ""
        data?.forEach { item -> if (msg?.contains(item.toString())!!) message = msg.replace(item.toString(), "") }
        val tallyKeys = success?.map { it.encodedImageContent } as ArrayList
        val isPrintNeed = tallyKeys.any { it != "" }
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, requireContext().resources.getString(R.string.fgrn_success))
        /*val subTitle = requireContext().resources.getString(R.string.for_the_po).plus("\n")
            .plus(requireContext().resources.getString(R.string.lot_id)).plus(processRmin?.batchNumber)*/
        intent.putExtra(AppUtils.SUB_TITLE, message.plus(" ").plus(data.toString().replace("[", "").replace("]", "")))
        intent.putExtra(AppUtils.PRINT_ENABLE, isPrintNeed)
        intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS, tallyKeys)
        startActivity(intent)
        requireActivity().finish()
    }


    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.fgrn_post_confirm)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(R.string.cancel),
                {
                    val postReq = prepareFgrnPostRequest(
                        fgrnItem,
                        fgrnItemWithGrades.gradeItems,
                        isRoundOff,
                        isIndexweighmenttype
                    )
                    vm.postFgrn(postReq)
                },
                { dismiss() })
        }
    }

    private fun updateItems(data: VegaCocoaFgrnItemWithGrades) {
        fgrnItemWithGrades = data
        data.gradeItems?.let {
            updateAdapter(it) }
        val bagConsumpList = arrayListOf<VegaCocoaFgrnGradesMatrialWeights>()
        data.gradeItems?.forEach { item ->
            val data = item.bagItems?.filter { it.batchNumber.isNotEmpty() }
            if (data != null) {
                bagConsumpList.addAll(data)
            }
        }
        val conData = arrayListOf<VegaCocoaFgrnBagCosumption>()
        var bagType = listOf<String>()
        bagConsumpList.forEach {
            if (bagType.contains(it.bagType)) {
                conData.forEach { item ->
                    if (item.bagType.equals(it.bagType))
                        item.bagCount = (item.bagCount.toInt().plus(it.bagCount.toInt())).toString()
                }
            } else {
                val dat = VegaCocoaFgrnBagCosumption()
                dat.bagCount = it.bagCount
                dat.bagType = it.bagType
                dat.batchNumber = it.batchNumber
                conData.add(dat)
                bagType = conData.map { it.bagType }
            }
        }
        updateBagConsumptionAdapter(conData)
    }

    private fun updateAdapter(gradeItems: List<VegaCocoaFgrnGradesWithBagItems>) {

        binding.rvFgrnSummary.setUpAdapter(
            gradeItems as MutableList, R.layout.item_vega_processing_indo_fgrn_summary,
            ItemVegaProcessingIndoFgrnSummaryBinding::inflate,
            { it, pos, bindItem ->
                isIndexweighmenttype = it.fgrnGrades.isIndexweighmenttype!!
                isRoundOff = it.fgrnGrades.isRoundOff!!
                bindItem.tvMaterialName.text = it.fgrnGrades.materialName
                var grossWeight = 0.0
                var tareWeight = 0.0
                val netWeight: Double
                it.bagItems?.forEach { item ->
                    val palletAvg =
                        if (item.noOfPallet?.toInt() != 0) item.palletWeight?.toDouble()
                            ?.div(item.noOfPallet?.toInt()!!) else 0.0
                    grossWeight = grossWeight.plus(item.grossWeight.toDouble())
                    tareWeight = tareWeight.plus(
                        item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!
                    )
                        .plus(palletAvg!!)
                    if (item.isRoundOff!!) {
                        isRoundOff = item.isRoundOff!!
                    }

                }
                netWeight = grossWeight.minus(tareWeight)
                if (it.fgrnGrades.isRoundOff!!) {
                    bindItem.tvWeightData.text = netWeight.roundToInt().toString().plus(" KG")
                    //  fgrnItem.netWeight = netWeight.roundToInt().toString()

                } else {
                    bindItem.tvWeightData.text = netWeight.formatThreeDigits().plus(" KG")
                }

                bindItem.tvLotId.text = it.fgrnGrades.batchNumber

                bindItem.tvStLocation.text = it.fgrnGrades.lotStorageLocationCode
                bindItem.tvNoOfBag.text =
                    it.bagItems?.sumBy { it1 -> it1.bagCount.toInt() }.toString()
                bindItem.ivEdit.setOnClickListener { view ->
                    if (isIndexweighmenttype && !fgrnItem.gradeList.isNullOrEmpty()) {
                        it.fgrnGrades.grossWeight = fgrnItem.gradeList!!.get(0).grossWeight
                        it.fgrnGrades.tareWeight = fgrnItem.gradeList!!.get(0).tareWeight
                        it.fgrnGrades.netWeight = fgrnItem.gradeList!!.get(0).netWeight
                    }
                    val gson = GsonUtils()
                    val poGrade = gson.toJson(listOf(it.fgrnGrades))
                    activity?.onBackPressed()
                    callBack?.replaceFgrnFragment(FRAG_ADD_WEIGHT_EDIT, fgrnItem, poGrade)
                }
                if (it.fgrnGrades.isIndexweighmenttype!!)
                    bindItem.tvWeightData.text = it.fgrnGrades.netWeight.plus(" KG")
            })

    }

    fun factor(n: Number): Boolean {
        return n.toDouble() % 100.0 == 0.0
    }

    private fun updateBagConsumptionAdapter(bagConsumpList: ArrayList<VegaCocoaFgrnBagCosumption>) {
        if (bagConsumpList.size > 0) {
            binding.rvBagConsumption.visible()
            binding.tvBagConsumption.visible()
        } else {
            binding.rvBagConsumption.gone()
            binding.tvBagConsumption.gone()
        }

        binding.rvBagConsumption.setUpAdapter(
            bagConsumpList,
            R.layout.item_vega_processing_indo_bag_consump_summary,
            ItemVegaProcessingIndoBagConsumpSummaryBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvBagTypeValue.text = it.bagType
                bindItem.tvBagCountValue.text = it.bagCount
                bindItem.tvBatchValue.text = it.batchNumber
            })
    }
}
