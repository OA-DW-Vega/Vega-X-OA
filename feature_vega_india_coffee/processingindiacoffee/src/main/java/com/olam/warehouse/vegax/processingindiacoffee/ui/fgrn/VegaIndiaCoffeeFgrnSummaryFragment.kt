package com.olam.warehouse.vegax.processingindiacoffee.ui.fgrn

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnBagCosumption
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaFgrnItemWithGrades
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaProcessingFgrnResponse
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingindiacoffee.R
import com.olam.warehouse.vegax.processingindiacoffee.databinding.FragmentVegaIndiaCoffeeFgrnSummaryBinding
import com.olam.warehouse.vegax.processingindiacoffee.utils.FRAG_ADD_WEIGHT_EDIT
import com.olam.warehouse.vegax.processingindiacoffee.utils.FRAG_ITEM
import com.olam.warehouse.vegax.processingindiacoffee.utils.prepareFgrnPostRequest
import com.olam.warehouse.vegax.processingindiacoffee.utils.prepareRminFgrnPostRequest
import kotlinx.android.synthetic.main.item_vega_india_coffee_bag_consump_summary.view.*
import kotlinx.android.synthetic.main.item_vega_india_coffee_fgrn_summary.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.math.roundToInt


class VegaIndiaCoffeeFgrnSummaryFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_india_coffee_fgrn_summary
    private lateinit var binding: FragmentVegaIndiaCoffeeFgrnSummaryBinding
    private val vm: VegaIndiaCoffeeFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var fgrnItem = VegaCocoaFgrnItems()
    private var fgrnItemWithGrades = VegaCocoaFgrnItemWithGrades()
    private var isRoundOff: Boolean = false
    private var isIndexweighmenttype: Boolean = false
    private var batchNumber: String = ""
    private val plantId = getPlantDetails().plantId
    private var plantList = ArrayList<String>()
    private var rspos = ""
    private var rsnum = ""
    private var phase = ""
    private var xchpf = ""

    interface CallBack {
        fun replaceFgrnFragment(
            fragment: String, model: VegaCocoaFgrnItems, id: String
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCocoaFgrnItems) = VegaIndiaCoffeeFgrnSummaryFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaIndiaCoffeeFgrnSummaryBinding.inflate(layoutInflater)
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
        if (getCurrentKey().split("_")[1].contains("NI")) {
            vm.offlineRminItemLocal.observe(this, Observer {
                if (!it.isNullOrEmpty()) {
                    it.forEach {
                        it.rminLot.forEach {
                            if (it.processOrderNum == fgrnItem.processOrderNo) {
                                rspos = it.rspos.toString()
                                rsnum = it.rsnum.toString()
                                phase = it.phase.toString()
                                xchpf = it.xchpf.toString()
                            }
                        }
                    }
                }
            })
            vm.getOfflineRminItem()
        }
        binding.tvLotNo.text = fgrnItem.processOrderNo
        binding.tvShift.text = getString(R.string.shift).plus(" ").plus(fgrnItem.shiftSelection)
        binding.tvOperatorName.text = fgrnItem.operatorName
        binding.tvShift.setOnClickListener { activity?.onBackPressed() }
        vm.getConfigItems(UserRoles.PROCESSING.role)
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
                            /*success?.forEach {
                                    batchNumber = it.batchNumber.toString()
                            }*/
                            if (getCurrentKey().split("_")[1].contains("NI") && plantList.contains(
                                    plantId
                                )
                            ) {
                                vm.deleteOfflineRminLot(fgrnItem.message.toString())  //lotId
                            }
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

    private fun moveToSuccessPage(success: List<VegaCocoaProcessingFgrnResponse>?) {
        val data = success?.map { it.batchNumber }
        val msg = success?.get(0)?.messages?.get(0)?.message
        var message = ""
        data?.forEach { item ->
            if (msg?.contains(item.toString())!!) message = msg.replace(item.toString(), "")
        }
        val tallyKeys = success?.map { it.encodedImageContent } as ArrayList
        val isPrintNeed = tallyKeys.any { it != "" }
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, requireContext().resources.getString(R.string.fgrn_success))
        /*val subTitle = requireContext().resources.getString(R.string.for_the_po).plus("\n")
            .plus(requireContext().resources.getString(R.string.lot_id)).plus(processRmin?.batchNumber)*/
        intent.putExtra(
            AppUtils.SUB_TITLE,
            message.plus(" ").plus(data.toString().replace("[", "").replace("]", ""))
        )
        if (getCurrentKey().split("_")[1].contains("NI")) {
            //val tallySequence = PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
            //displaying below button only for drying unit
            if (batchNumber.isNotEmpty()) {
                intent.putExtra(UIUtils.PRINT_FGRN_TALLYSHEET, true)
                intent.putExtra(UIUtils.DISPATCH_BATCH, batchNumber)
            }
            intent.putExtra(AppUtils.PRINT_ENABLE, false)
        } else intent.putExtra(AppUtils.PRINT_ENABLE, isPrintNeed)
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
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (getCurrentKey().split("_")[1].contains("NI")) {
                        vm.configItems.observe(viewLifecycleOwner, Observer {
                            updateConfigItems(it)
                        })
                    } else {
                        val postReq =
                            prepareFgrnPostRequest(
                                fgrnItem,
                                fgrnItemWithGrades.gradeItems,
                                isRoundOff,
                                isIndexweighmenttype
                            )
                        vm.postFgrn(postReq)
                    }
                },
                { dismiss() })
        }
    }

    private fun updateItems(data: VegaCocoaFgrnItemWithGrades) {
        fgrnItemWithGrades = data
        data.gradeItems?.let {
            updateAdapter(it)
        }
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

        binding.rvFgrnSummary.setUp(
            gradeItems as MutableList,
            R.layout.item_vega_india_coffee_fgrn_summary,
            { it, pos ->
                isIndexweighmenttype = it.fgrnGrades.isIndexweighmenttype!!
                isRoundOff = it.fgrnGrades.isRoundOff!!
                tv_material_name.text = it.fgrnGrades.materialName
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
                    tv_weight_data.text = netWeight.roundToInt().toString().plus(" KG")
                    //  fgrnItem.netWeight = netWeight.roundToInt().toString()

                } else {
                    tv_weight_data.text = netWeight.formatThreeDigits().plus(" KG")
                }

                tvLotId.text = it.fgrnGrades.batchNumber

                tvStLocation.text = it.fgrnGrades.lotStorageLocationCode
                tvNoOfBag.text = it.bagItems?.sumBy { it1 -> it1.bagCount.toInt() }.toString()
                ivEdit.setOnClickListener { view ->
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
                    tv_weight_data.text = it.fgrnGrades.netWeight.plus(" KG")
            })

    }

    fun factor(n: Number): Boolean {
        return n.toDouble() % 100.0 == 0.0
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>) {
        plantList.clear()
        var dryingPlants = configItems.filter { it.process.equals(ConfigItems.DRYING_PLANT.item) }
        var plantLists = ""
        dryingPlants.forEach {
            plantLists = it.value?.trim() ?: ""
            if (plantLists.isNotEmpty()) {
                plantList = plantLists.split(",") as ArrayList<String>
            }
        }
        if (plantList.contains(plantId)) {
            val postReq =
                prepareRminFgrnPostRequest(
                    fgrnItem,
                    fgrnItemWithGrades.gradeItems,
                    isRoundOff,
                    isIndexweighmenttype,
                    rspos,
                    rsnum,
                    phase,
                    xchpf
                )
            vm.postFgrn(postReq)
            fgrnItem.rminList?.forEach {
                batchNumber = it.batchNumber.toString()
            }
        } else {
            val postReq =
                prepareFgrnPostRequest(
                    fgrnItem,
                    fgrnItemWithGrades.gradeItems,
                    isRoundOff,
                    isIndexweighmenttype
                )
            vm.postFgrn(postReq)
        }
    }

    private fun updateBagConsumptionAdapter(bagConsumpList: ArrayList<VegaCocoaFgrnBagCosumption>) {
        if (bagConsumpList.size > 0) {
            binding.rvBagConsumption.visible()
            binding.tvBagConsumption.visible()
        } else {
            binding.rvBagConsumption.gone()
            binding.tvBagConsumption.gone()
        }

        binding.rvBagConsumption.setUp(bagConsumpList, R.layout.item_vega_india_coffee_bag_consump_summary, { it, pos ->
            tvBagTypeValue.text = it.bagType
            tvBagCountValue.text = it.bagCount
            tvBatchValue.text = it.batchNumber
        })
    }
}
