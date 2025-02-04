package com.olam.warehouse.vegax.processingghana.ui.fgrn.offline

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.data.domain.model.VegaProcessingRminPo
import com.olam.warehouse.master.vega.entity.VegaFgrnProcessingOrder
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.entity.VegaProcessingList
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnBagCosumption
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnItemWithGrades
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineFgrnData
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineFgrnProcessLotDetails
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminProcessLotDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingghana.R
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaFgrnBagMaterialWithId
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessingFgrnResponse
import com.olam.warehouse.vegax.processingghana.databinding.FragmentVegaGhanaFgrnSummaryBinding
import com.olam.warehouse.vegax.processingghana.ui.fgrn.VegaGhanaFgrnViewModel
import com.olam.warehouse.vegax.processingghana.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGhanaFgrnOfflineSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_ghana_fgrn_summary
    private lateinit var binding: FragmentVegaGhanaFgrnSummaryBinding
    private val vm: VegaGhanaFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var fgrnItem = ArrayList<VegaGhanaOfflineFgrnProcessLotDetails>()
    private var fgrnEachItem = VegaGhanaOfflineFgrnProcessLotDetails()
    private var bagItem = VegaCoffeeFgrnGradesMatrialWeights()
    private var fgrnItemWithGrades = VegaCoffeeFgrnItemWithGrades()

    private var gradesWithBags = listOf<VegaCoffeeFgrnGradesWithBagItems>()
    private var rminList = listOf<VegaGhanaOfflineRminProcessLotDetails>()
    private val materialList = arrayListOf<VegaGhanaFgrnBagMaterialWithId>()
    private var poList = ArrayList<VegaFgrnProcessingOrder>()
    private var rmin = ArrayList<VegaProcessingList>()
    private var totalBagConsumed: ArrayList<String>? = null
    private var stageFevor: String = ""
    private var cfgNo: String? = ""
    private var versionID: String? = ""
    private var processOrder: String? = ""
    private var OutputMaterialCode: String = ""
    private var bagConsumptionQuantity: String = ""
    private var bagNetWeight: String = ""
    private var standardWeight: String = ""
    private var bagType: String = ""
    private var batchNo: String = ""
    private var totalBagCount: Int = 0
    private var newBagValue: String = ""
    private var materialName: String = ""
    private var temRminId: String = ""
    private var vegaStage = ArrayList<VegaGhanaOfflineFgrnData>()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    val conData = arrayListOf<VegaCocoaFgrnBagCosumption>()
    private var vegaStage_RMIN = VegaProcessingStage()

    interface CallBack {
        fun replaceFgrnFragment(
            fragment: String,
            model: VegaCoffeeFgrnItems,
            id: String,
            vegaStage: VegaProcessingStage
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(
            model: List<VegaGhanaOfflineFgrnProcessLotDetails>,
            lotData: List<VegaGhanaOfflineFgrnData>, vegaStage: VegaProcessingStage
        ) = VegaGhanaFgrnOfflineSummaryFragment().putArgs {
            putParcelableArrayList(FRAG_ITEM, model as ArrayList<VegaGhanaOfflineFgrnProcessLotDetails>)
            putParcelableArrayList(VEGA_STAGE, lotData as ArrayList<VegaGhanaOfflineFgrnData>)
            putParcelable(MODEL_BUNDLE, vegaStage)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaGhanaFgrnSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingsesame/ui/fgrn/VegaSesameFgrnSummaryFragment").title("Processing Coffee")
            .with(tracker)
    }

    private fun initUI() {
        fgrnItem =
            arguments?.getParcelableArrayList<VegaGhanaOfflineFgrnProcessLotDetails>(FRAG_ITEM)!!
        vegaStage = arguments?.getParcelableArrayList<VegaGhanaOfflineFgrnData>(VEGA_STAGE)!!
        vegaStage_RMIN = arguments?.getParcelable(MODEL_BUNDLE)!!
        vm.material.observe(viewLifecycleOwner, Observer { bagTypeList = it.toMutableList() })
        vm.getMaterials()
        vm.vegaOfflineRMINItems.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                versionID = it.get(0).versionId
                OutputMaterialCode = it.get(0).outputMaterialCode!!
            }
        })
        vm.getofflineRMINVersionID(fgrnItem.get(0).rminTempId!!)
        binding.llrminQty.isVisible = true
        vegaStage.forEach {
            binding.rminQty.text = it.rminQty
        }

//        vm.fgrnItem.observe(viewLifecycleOwner, Observer {
//            if(it != null){
//                updateItems(it)
//            }
//        })
//
//        vm.getFgrnItems(fgrnItem.fgrnId)
        fgrnItem.forEach {

            fgrnEachItem = it
            vm.getofflineRminPostItem(it.rminTempId!!)
        }

        vm.rminItemLocal.observe(viewLifecycleOwner, Observer {
            rminList = it
        })

        updateAdapter(fgrnItem)


        binding.tvWeightData.text = fgrnEachItem.netWeight.plus(" MT")
        binding.tvBagCountValue.text = fgrnEachItem.bagCount
        if (fgrnEachItem.bagCount == "0") {
            binding.tvBagTypeValue.text = "JUTE BAG"
        } else
            binding.tvBagTypeValue.text = fgrnEachItem.bagType
        processOrder = fgrnEachItem.processOrderNum
        binding.tvLotNo.text = fgrnEachItem.processOrderNum
        if (!isOnline()) {
            binding.tvConfirm.text = "OK"
        }
//        binding.tvShift.text = getString(R.string.shift).plus(": ").plus(fgrnItem.shiftSelection)
//        binding.tvOperatorName.text = fgrnItem.remarks
//        binding.tvShift.setOnClickListener { activity?.onBackPressed() }
        binding.tvConfirm.setOnClickListener {
            if (isOnline()) {
                showConfirmDialog()
            } else {
                activity?.onBackPressed()
            }
        }
//        vm.stages.observe(viewLifecycleOwner, Observer { updageStageUI(it) })
//        vm.fetchStages()
//        vm.poDetailListFgrn.observe(viewLifecycleOwner, Observer { updateFgrnPoUI(it) })
        vm.postFgrn.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.createPo.observe(viewLifecycleOwner, Observer { updateAutoPoUI(it) })


    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaGhanaProcessingFgrnResponse>>>) {
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

    private fun updateAutoPoUI(response: Resource<GenericReqAndResp<VegaProcessingRminPo>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            val success = it.data?.data
                            updateFgrnStatus(it.data?.message, 4)
                            moveToSuccessPageAutoPo(success, it.data?.message!!)
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
        message?.let { vm.updateFgrnStatus(it, status, fgrnEachItem.fgrnTempId) }
    }

    private fun moveToSuccessPage(success: List<VegaGhanaProcessingFgrnResponse>?) {
        //LotCard
        vm.deleteLotDetails()
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
                tareWeight = tareWeight.plus(item.tareWeight?.toDouble()?.times(item.totalBagCount?.toDouble()!!)!!)
                    .plus(palletAvg!!)
                bagTareWeight =
                    bagTareWeight.plus(item.tareWeight?.toDouble()?.times(item.totalBagCount?.toDouble()!!)!!)
            }
            when (it.fgrnGrades.meins) {
                "MT" -> netWeight = (grossWeight.minus(tareWeight))
                "KG" -> netWeight = grossWeight.minus(tareWeight)
                else -> netWeight = grossWeight.minus(tareWeight)
            }
            lot.batchNumber = success?.get(0)?.batchNumber!!
            val netwg = success.get(0).netWeight?.toDouble()
            lot.editedWeight = netwg?.formatThreeDigits()
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
//        intent.putExtra("fromsesameprocessing", true)
//        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotList)
//        intent.putExtra(AppUtils.PRINT_ENABLE, true)
//        val tallyKeys = ArrayList<String>()
//        tallyKeys.add(success?.get(0)?.encodedImageContent ?: "")
//        intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS, tallyKeys)
        intent.putExtra(
            AppUtils.SUB_TITLE,
            message.plus(" ").plus(data.toString().replace("[", "").replace("]", ""))
        )
        startActivity(intent)
        requireActivity().finish()
    }

    private fun moveToSuccessPageAutoPo(success: VegaProcessingRminPo?, message: String) {
        //LotCard
        vm.deleteLotDetails()
        val msg = message
        var message = ""
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, requireContext().resources.getString(R.string.fgrn_success))
        intent.putExtra(AppUtils.SUB_TITLE, msg)
        startActivity(intent)
        requireActivity().finish()
    }


    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.fgrn_post_confirm)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    fgrnItemWithGrades.gradeItems?.forEach {
                        it.bagItems?.forEach { item ->
                            item.bagMaterialCode = bagItem.bagMaterialCode
                            item.bagType = bagItem.bagType
                            item.netWeight = bagItem.netWeight
                            item.bagCount = bagItem.bagCount
                        }
                    }
                    val postReq =
                        prepareSummaryOfflineFgrnPostRequest(fgrnItem, vegaStage, rminList)
                    var poRequest = prepareRminofflineCreatePoRequest(
                        vegaStage_RMIN.fevor, vegaStage_RMIN.cfgNo,
                        fgrnEachItem.materialName!!,
                        OutputMaterialCode,
                        postReq, versionID!!

                    )
                    if (fgrnEachItem.processOrderNum!!.contains("TMP_RMIN_"))
                        vm.createPoRequest(poRequest)
                    else
                        vm.postFgrn(postReq)
                    /* needed val postReq = prepareSummaryOfflineFgrnPostRequest(fgrnItem, vegaStage, rminList)
                      vm.postFgrn(postReq)*/
                },
                { dismiss() })
        }
    }

    private fun updateAdapter(gradeItems: ArrayList<VegaGhanaOfflineFgrnProcessLotDetails>) {
        gradeItems.forEach {
            binding.tvLotId.text = it.batchNumber
            binding.tvStLocation.text = it.storageLocationCode
        }
        fgrnItem.forEach {
            binding.tvMaterialName.text = it.materialName
        }

    }


}
