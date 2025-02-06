package com.olam.warehouse.vegax.processingnigeria.ui.fgrn

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
import com.olam.warehouse.master.vega.entity.VegaFeatureMaster
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnBagCosumption
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnItemWithGrades
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.FROM_CAMEROON_COCOA_FGRN
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingnigeria.R
import com.olam.warehouse.vegax.processingnigeria.data.domain.model.VegaNigeriaFgrnBagMaterialWithId
import com.olam.warehouse.vegax.processingnigeria.data.domain.model.VegaNigeriaProcessingFgrnResponse
import com.olam.warehouse.vegax.processingnigeria.databinding.FragmentVegaNigeriaFgrnSummaryBinding
import com.olam.warehouse.vegax.processingnigeria.databinding.ItemVegaNigeriaBagConsumpSummaryBinding
import com.olam.warehouse.vegax.processingnigeria.databinding.ItemVegaNigeriaFgrnSummaryBinding
import com.olam.warehouse.vegax.processingnigeria.utils.FRAG_ADD_WEIGHT_EDIT
import com.olam.warehouse.vegax.processingnigeria.utils.FRAG_ITEM
import com.olam.warehouse.vegax.processingnigeria.utils.convertKgToMT
import com.olam.warehouse.vegax.processingnigeria.utils.prepareFgrnPostRequest
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaFgrnSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_nigeria_fgrn_summary
    private lateinit var binding: FragmentVegaNigeriaFgrnSummaryBinding
    private val vm: VegaNigeriaFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var fgrnItem = VegaCoffeeFgrnItems()
    private var fgrnItemWithGrades = VegaCoffeeFgrnItemWithGrades()

    private var gradesWithBags = listOf<VegaCoffeeFgrnGradesWithBagItems>()
    private val materialList = arrayListOf<VegaNigeriaFgrnBagMaterialWithId>()
    private var isDirect= false
    private var isInDirect= false
    val sapMaterialList = ArrayList<VegaMaterial>()

    interface CallBack {
        fun replaceFgrnFragment(fragment: String, model: VegaCoffeeFgrnItems, id: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCoffeeFgrnItems) = VegaNigeriaFgrnSummaryFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNigeriaFgrnSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingnigeria/ui/fgrn/VegaCoffeeFgrnSummaryFragment").title("Processing Coffee")
            .with(tracker)
    }

    private fun initUI() {
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCoffeeFgrnItems()
        vm.fgrnItem.observe(viewLifecycleOwner, Observer { updateItems(it) })
        vm.getFgrnItems(fgrnItem.fgrnId)
        binding.tvLotNo.text = fgrnItem.processOrderNo
        binding.tvShift.text = getString(R.string.shift).plus(": ").plus(fgrnItem.shiftSelection)
        binding.tvOperatorName.text = fgrnItem.remarks
        binding.tvShift.setOnClickListener { activity?.onBackPressed() }
        binding.tvConfirm.setOnClickListener { showConfirmDialog() }
        vm.postFgrn.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.featureMaster.observe(viewLifecycleOwner, Observer { updateFeatureUI(it) })
        vm.getFeatureMaster(Constants.TRACK_TRACE)


        vm.materials.observe(viewLifecycleOwner, Observer {
            sapMaterialList.clear()
            sapMaterialList.addAll(it)
        })
    }

    private fun updateFeatureUI(it: List<VegaFeatureMaster>?) {
        it?.let {
            it.forEach {
                when (it.featureName) {
                    "Direct" ->isDirect= it.mandatory == true
                    "Indirect" ->isInDirect= it.mandatory == true
                }
            }
        }
        if(isDirect || isInDirect)  vm.fetchMaterials()
    }
    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaNigeriaProcessingFgrnResponse>>>) {
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

    private fun moveToSuccessPage(successes: List<VegaNigeriaProcessingFgrnResponse>?) {
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
                tareWeight = tareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
                    .plus(palletAvg!!)
                bagTareWeight = bagTareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
            }
            when (it.fgrnGrades.meins) {
                "MT" -> netWeight = (grossWeight.minus(tareWeight)).div(1000)
                "KG" -> netWeight = grossWeight.minus(tareWeight)
                else -> netWeight = grossWeight.minus(tareWeight)
            }
//            lot.batchNumber = it.fgrnGrades.batchNumber
            lot.batchNumber = successes?.get(0)?.batchNumber.toString()
            lot.editedWeight = netWeight.plus(
                if (!it.fgrnGrades.weight.isNullOrEmpty()) it.fgrnGrades.weight?.toDouble() ?: 0.0 else 0.0
            ).toString()
            lot.unitOfMeasure = it.fgrnGrades.meins
            lot.materialName = it.fgrnGrades.materialName
            lot.materialCode = it.fgrnGrades.materialCode
            lotList.add(lot)
        }

        val data = successes?.map { it.batchNumber }
        val msg = successes?.get(0)?.messages?.get(0)?.message
        var message = ""
        data?.forEach { item -> if (msg?.contains(item.toString())!!) message = msg.replace(item.toString(), "") }
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, requireContext().resources.getString(R.string.fgrn_success))
        val tallyKeys = ArrayList<String>()
        successes?.forEach {
            tallyKeys.add(it.encodedImageContent.toString())
        }
        intent.putExtra(FROM_CAMEROON_COCOA_FGRN, true)
        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotList)
        intent.putExtra(AppUtils.PRINT_ENABLE, true)
        intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS_NEW, tallyKeys)
        intent.putExtra(AppUtils.SUB_TITLE, message.plus(" ").plus(data.toString().replace("[", "").replace("]", "")))
        startActivity(intent)
        requireActivity().finish()
    }


    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.fgrn_post_confirm)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    val postReq = prepareFgrnPostRequest(fgrnItem, fgrnItemWithGrades.gradeItems,sapMaterialList)
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

    private fun updateAdapter(gradeItems: List<VegaCoffeeFgrnGradesWithBagItems>) {

        binding.rvFgrnSummary.setUpAdapter(
            gradeItems as MutableList, R.layout.item_vega_nigeria_fgrn_summary,
            ItemVegaNigeriaFgrnSummaryBinding::inflate,
            { it, pos, bindItem ->
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
                }
                netWeight = grossWeight.minus(tareWeight)
                bindItem.tvWeightData.text =  convertKgToMT(netWeight.toString()).plus(" MT")
                bindItem.tvLotId.text = it.fgrnGrades.batchNumber
                bindItem.tvStLocation.text = it.fgrnGrades.lotStorageLocationCode
                bindItem.tvNoOfBag.text =
                    it.bagItems?.sumBy { it1 -> it1.bagCount.toInt() }.toString()
                bindItem.ivEdit.setOnClickListener { view ->
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

        binding.rvBagConsumption.setUpAdapter(bagConsumpList,
            R.layout.item_vega_nigeria_bag_consump_summary,
            ItemVegaNigeriaBagConsumpSummaryBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvBagTypeValue.text = it.bagType
                bindItem.tvBagCountValue.text = it.bagCount
                bindItem.tvBatchValue.text = it.batchNumber
                //ivEdit.setOnClickListener { activity?.onBackPressed() }
            })
    }

}
