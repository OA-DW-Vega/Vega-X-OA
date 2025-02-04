package com.olam.warehouse.vegax.processingcameroon.ui.fgrn

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnBagCosumption
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnItemWithGrades
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.FROM_CAMEROON_COCOA_FGRN
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingcameroon.R
import com.olam.warehouse.vegax.processingcameroon.data.domain.model.VegaCameroonProcessingFgrnResponse
import com.olam.warehouse.vegax.processingcameroon.databinding.FragmentVegaCameroonFgrnSummaryBinding
import com.olam.warehouse.vegax.processingcameroon.utils.FRAG_ADD_WEIGHT_EDIT
import com.olam.warehouse.vegax.processingcameroon.utils.FRAG_ITEM
import com.olam.warehouse.vegax.processingcameroon.utils.prepareFgrnPostRequest
import kotlinx.android.synthetic.main.item_vega_cameroon_bag_consump_summary.view.*
import kotlinx.android.synthetic.main.item_vega_cameroon_fgrn_summary.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCameroonFgrnSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_cameroon_fgrn_summary
    private lateinit var binding: FragmentVegaCameroonFgrnSummaryBinding
    private val vm: VegaCameroonFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var fgrnItem = VegaCoffeeFgrnItems()
    private var fgrnItemWithGrades = VegaCoffeeFgrnItemWithGrades()

    private var gradesWithBags = listOf<VegaCoffeeFgrnGradesWithBagItems>()

    interface CallBack {
        fun replaceFgrnFragment(fragment: String, model: VegaCoffeeFgrnItems, id: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCoffeeFgrnItems) = VegaCameroonFgrnSummaryFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCameroonFgrnSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcameroon/ui/fgrn/VegaCameroonFgrnSummaryFragment").title("Vega_Cameroon/Processing")
            .with(tracker)
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvHeadPoDetail, it, false)
            getActionBtnChangedView(binding.tvConfirm, it, true)
        }
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCoffeeFgrnItems()
        vm.fgrnItem.observe(viewLifecycleOwner, Observer { updateItems(it) })
        vm.getFgrnItems(fgrnItem.fgrnId)
        binding.tvLotNo.text = fgrnItem.processOrderNo
        binding.tvShift.text = getString(R.string.shift).plus(": ").plus(fgrnItem.shiftSelection)
        binding.tvOperatorName.text = fgrnItem.remarks
        binding.tvShift.setOnClickListener { activity?.onBackPressed() }
        binding.tvConfirm.setOnClickListener { showConfirmDialog() }
        vm.postFgrn.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCameroonProcessingFgrnResponse>>>) {
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
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    updateFgrnStatus(it.error.toString(), 3)
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }

            }
        }
    }

    private fun updateFgrnStatus(message: String?, status: Int) {
        message?.let { vm.updateFgrnStatus(it, status, fgrnItem.fgrnId) }
    }

    private fun moveToSuccessPage(success: List<VegaCameroonProcessingFgrnResponse>?) {
        //LotCard
        var lotList = arrayListOf<VegaCoffeeSalesLots>()
        fgrnItemWithGrades.gradeItems?.forEach {
            val lot = VegaCoffeeSalesLots()
            var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
            var netWeight = 0.0
            it.bagItems?.forEach { item ->
                val palletAvg =
                    if (item.noOfPallet?.toInt() != 0) item.palletWeight?.toDouble()
                        ?.div(item.noOfPallet?.toInt()!!) else 0.0
                grossWeight = grossWeight.plus(item.grossWeight.toDouble())
                tareWeight =
                    tareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
                        .plus(item.tareWeight1?.toDouble()?.times(item.bagCount1!!.toDouble())!!)
                        .plus(palletAvg!!.toDouble())
                bagTareWeight = bagTareWeight.plus(
                    item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!
                ).plus(item.tareWeight1?.toDouble()?.times(item.bagCount1!!.toDouble())!!)
            }
            when (it.fgrnGrades.meins) {
                "MT" -> netWeight = (grossWeight.minus(tareWeight)).div(1000)
                "KG" -> netWeight = grossWeight.minus(tareWeight)
                else -> netWeight = grossWeight.minus(tareWeight)
            }
            lot.batchNumber = it.fgrnGrades.batchNumber
            lot.editedWeight = netWeight.plus(
                if (!it.fgrnGrades.weight.isNullOrEmpty()) it.fgrnGrades.weight?.toDouble() ?: 0.0 else 0.0
            ).toString()
            lot.unitOfMeasure = it.fgrnGrades.meins
            lot.materialName = it.fgrnGrades.materialName
            lot.materialCode = it.fgrnGrades.materialCode
            lotList.add(lot)
        }

        val data = success?.map { it.batchNumber }
        val msg = success?.get(0)?.messages?.get(0)?.message
        var message = ""
        data?.forEach { item -> if (msg?.contains(item.toString())!!) message = msg.replace(item.toString(), "") }
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, requireContext().resources.getString(R.string.fgrn_success))
        val tallyKeys = ArrayList<String>()
        success?.forEach {
            tallyKeys.add(it.encodedImageContent.toString())
        }
        intent.putExtra(FROM_CAMEROON_COCOA_FGRN, true)
        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotList)
        intent.putExtra(AppUtils.PRINT_ENABLE, true)
        intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS, tallyKeys)
        intent.putExtra(AppUtils.SUB_TITLE, message.plus(" ").plus(data.toString().replace("[", "").replace("]", "")))
        startActivity(intent)
        requireActivity().finish()
    }


    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.fgrn_post_confirm)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    val postReq = prepareFgrnPostRequest(fgrnItem, fgrnItemWithGrades.gradeItems)
                    vm.postFgrn(postReq)
                },
                { dismiss() })
        }
    }

    private fun updateItems(data: VegaCoffeeFgrnItemWithGrades) {
        fgrnItemWithGrades = data
        data.gradeItems?.let { updateAdapter(it) }
        gradesWithBags = data.gradeItems?.toList() ?: emptyList()
        val bagConsumpList = arrayListOf<VegaCoffeeFgrnGradesMatrialWeights>()
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
            } else if (bagType.contains(it.bagType1)) {
                conData.forEach { item ->
                    if (item.bagType.equals(it.bagType1))
                        item.bagCount1 =
                            (item.bagCount1.toInt().plus(it.bagCount1!!.toInt())).toString()
                }
            } else {
                val dat = VegaCocoaFgrnBagCosumption()
                dat.bagCount = it.bagCount
                dat.bagCount1 = it.bagCount1!!
                dat.bagType = it.bagType
                dat.bagType1 = it.bagType1
                dat.batchNumber = it.batchNumber
                conData.add(dat)
                bagType = conData.map { (it.bagType) }
            }
        }
        updateBagConsumptionAdapter(conData)
    }

    private fun updateAdapter(gradeItems: List<VegaCoffeeFgrnGradesWithBagItems>) {

        binding.rvFgrnSummary.setUp(
            gradeItems as MutableList,
            R.layout.item_vega_cameroon_fgrn_summary,
            { it, pos ->
                tv_material_name.text = it.fgrnGrades.materialName
                var grossWeight = 0.0
                var tareWeight = 0.0
                val netWeight: Double
                it.bagItems?.forEach { item ->
                    val palletAvg =
                        if (item.noOfPallet?.toInt() != 0) item.palletWeight?.toDouble()
                            ?.div(item.noOfPallet?.toInt()!!) else 0.0
                    grossWeight = grossWeight.plus(item.grossWeight.toDouble())
                    tareWeight =
                        tareWeight.plus(
                            item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!
                        )
                            .plus(
                                item.tareWeight1?.toDouble()?.times(item.bagCount1!!.toDouble())!!
                            )
                            .plus(palletAvg!!.toDouble())
                }
                netWeight = grossWeight.minus(tareWeight)
                tv_weight_data.text = netWeight.formatThreeDigits().plus(" KG")
                tvLotId.text = it.fgrnGrades.batchNumber
                tvStLocation.text = it.fgrnGrades.lotStorageLocationCode
                tvNoOfBag.text =
                    it.bagItems?.sumBy { it1 -> it1.bagCount.toInt().plus(it1.bagCount1!!.toInt()) }
                        .toString()
                ivEdit.setOnClickListener { view ->
                    val gson = GsonUtils()
                    val poGrade = gson.toJson(listOf(it.fgrnGrades))
                    callBack?.replaceFgrnFragment(FRAG_ADD_WEIGHT_EDIT, fgrnItem, poGrade)
                }
            })
    }

    private fun updateBagConsumptionAdapter(bagConsumpList: ArrayList<VegaCocoaFgrnBagCosumption>) {
        if (bagConsumpList.size > 0) {
            binding.rvBagConsumption.visible()
            binding.tvBagConsumption.visible()
        } else {
            binding.rvBagConsumption.gone()
            binding.tvBagConsumption.gone()
        }

        binding.rvBagConsumption.setUp(bagConsumpList,
            R.layout.item_vega_cameroon_bag_consump_summary,
            { it, pos ->
                tvBagTypeValue.text = it.bagType.plus(" , ").plus(it.bagType1)
                tvBagCountValue.text = it.bagCount.toInt().plus(it.bagCount1.toInt()).toString()
                tvBatchValue.text = it.batchNumber
            })
    }

}
