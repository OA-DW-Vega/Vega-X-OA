package com.olam.warehouse.vegax.processing.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.vega.entity.ProcessingLotDetails
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaProcessingRminBoms
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.RMIN_POST_DATA
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processing.R
import com.olam.warehouse.vegax.processing.data.domain.model.VegaProcessingQualityDetails
import com.olam.warehouse.vegax.processing.databinding.FragmentVegaRminLotToProcessBinding
import com.olam.warehouse.vegax.processing.ui.VegaProcessingViewModel
import com.olam.warehouse.vegax.processing.utils.*
import kotlinx.android.synthetic.main.item_vega_selected_lots.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaRminLotToProcessFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_rmin_lot_to_process
    private lateinit var binding: FragmentVegaRminLotToProcessBinding
    private var callBack: VegaRminLotToProcessFragment.CallBack? = null
    private val vm: VegaProcessingViewModel by viewModel()
    private var dispatchLotsList = mutableListOf<VegaDispatchLots>()
    private var materialNo: String? = ""
    private var materialName: String? = ""
    private var bom: VegaProcessingRminBoms? = null
    private var whLotsList = mutableListOf<VegaDispatchLots>()
    private var stageFevor: String? = ""
    private var cfgNumber: String? = ""
    private var selectedSL: String = ""
    private var selectedBatch: String = ""
    private var storageLocaionList = listOf<VegaCustomStLocation>()
    private var storageListdata = listOf<String>()


    interface CallBack {
        fun replaceFragment(
            stageFevor: String,
            cfgNumber: String,
            materialName: String,
            materialNo: String,
            bom: VegaProcessingRminBoms,
            lotDetails: VegaDispatchLots
        )
    }

    companion object {
        fun newInstance(
            stageFevor: String,
            cfgNumber: String,
            materialName: String,
            materialNo: String,
            bom: VegaProcessingRminBoms,
            rminPostData: ArrayList<ProcessingLotDetails>
        ) = VegaRminLotToProcessFragment().putArgs {
            putString(MATERIAL_NAME, materialName)
            putString(MATERIAL_NUMBER, materialNo)
            putString(STAGEFEVOR, stageFevor)
            putString(CFG_NO, cfgNumber)
            putParcelable(BOM_ITEM, bom)
            putParcelableArrayList(RMIN_POST_DATA, rminPostData)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as VegaRminLotToProcessFragment.CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaRminLotToProcessBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processing/ui/rmin/VegaRminLotToProcessFragment").title("Processing").with(tracker)
        initUI()

    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvType, it, false)
        }
        bom = arguments?.getParcelable(BOM_ITEM)!!
        materialNo = arguments?.getString(MATERIAL_NUMBER)!!
        materialName = arguments?.getString(MATERIAL_NAME)!!
        stageFevor = arguments?.getString(STAGEFEVOR)!!
        cfgNumber = arguments?.getString(CFG_NO)!!
        var rminPostData = ArrayList<ProcessingLotDetails>()
        rminPostData = arguments?.getParcelableArrayList(RMIN_POST_DATA)!!
        if (!rminPostData.equals("null") && rminPostData.size > 0) {
            selectedBatch = rminPostData[0].batchNumber
            selectedSL = rminPostData[0].storageLocationCode.toString()
        }
        vm.storageLocation.observe(
            viewLifecycleOwner,
            Observer { updateStroageUI(it.filter { !it.storageLocationType.equals("P") }) })
        vm.stocks.observe(viewLifecycleOwner, Observer { updateUI(it) })
        //vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateParams(it) })
        vm.fetchStocks(materialNo!!)
        vm.fetchStorageLocation()

    }

    private fun moveToSummary(bom: VegaProcessingRminBoms, lotDetails: VegaDispatchLots) {
        vm.saveRmin(bom, prepareProcessLots(lotDetails), stageFevor, materialName!!, materialNo!!)
        callBack?.replaceFragment(stageFevor!!, cfgNumber!!, materialName!!, materialNo!!, bom, lotDetails)
    }

    private fun updateStroageUI(storageLocaionList: List<VegaCustomStLocation>) {
        this.storageLocaionList = storageLocaionList
        val storageLocation = ArrayList<VegaCustomStLocation>()
        val storageLocationinit = VegaCustomStLocation()
        storageLocationinit.procureLocationName = "--Select Storage--"
        storageLocation.add(storageLocationinit)
        for (stroageItem in storageLocaionList) {
            storageLocation.add(stroageItem)
        }
        storageListdata = storageLocation.distinctBy { it.procureLocationCode }
            .map { data -> data.procureLocationCode.plus("-").plus(data.procureLocationName) }
        val stroageAdapter = ArrayAdapter(requireContext(), R.layout.item_vega_processing_rmin_grade, storageListdata)
        stroageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spStroageLocaion.adapter = stroageAdapter
        binding.spStroageLocaion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                for (data in storageLocaionList) {
                    val code = storageListdata[position].split("-")
                    if (data.procureLocationCode == code[0]) {
                        whLotsList.clear()
                        // val processLots=dispatchLotsList.distinctBy { i->i.storageLocationCode.equals(data.procureLocationCode) }
                        dispatchLotsList.forEach {
                            if (it.storageLocationCode.equals(data.procureLocationCode)) {
                                whLotsList.add(it)
                            }
                        }
                        if (whLotsList.isNotEmpty()) {
                            binding.rvSelectLots.visibility = View.VISIBLE
                            updateSelectedLot(whLotsList)
                        } else {
                            binding.rvSelectLots.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()
                            val dataValue = it.data?.data!!
                            dispatchLotsList.addAll(dataValue)
                            updateSelectLotValues()
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

    private fun updateSelectLotValues() {
        if (selectedSL.isNotEmpty()) {
            storageListdata.forEachIndexed { index, s ->
                if (s.contains(selectedSL)) {
                    binding.spStroageLocaion.setSelection(index)
                    whLotsList.clear()
                    whLotsList =
                        dispatchLotsList.filter { it.storageLocationCode.equals(selectedSL) } as MutableList<VegaDispatchLots>
                    if (whLotsList.isNotEmpty()) {
                        binding.rvSelectLots.visibility = View.VISIBLE
                        updateSelectedLot(whLotsList)
                    } else {
                        binding.rvSelectLots.visibility = View.GONE
                    }
                }
            }
        }
    }


    private fun updateSelectedLot(data: MutableList<VegaDispatchLots>) {
        bom = arguments?.getParcelable(BOM_ITEM)!!
        binding.rvSelectLots.setUp(data, R.layout.item_vega_selected_lots, { it, pos ->

            /* if (pos % 2 == 0) {
                 this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
             } else {
                 this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
             }*/

            when {
                it.batchNumber.equals(selectedBatch) -> tvSelectedLot.visible()
                else -> tvSelectedLot.gone()
            }

            val batchNo = it.batchNumber.toString()
            val materialNo = it.materialCode.toString()
            tvLotId.text = it.batchNumber.toString()
            tvOrigin.text = it.region
            tvKor.text = it.kor
            tvWeight.text = it.weight.toString()
            if (it.isProgress!!) llProgressBar.visible() else llProgressBar.gone()
            if (it.region?.isNotEmpty()!! && it.kor?.isNotEmpty()!!) ivDisplayInfo.gone() else ivDisplayInfo.visible()
            if (it.region?.isNotEmpty()!! || it.kor?.isNotEmpty()!!) llQualityParams.visible() else llQualityParams.gone()
            ivDisplayInfo.setOnClickListener { item ->
                //vm.getQualityParams(batchNo, materialNo)
                if (it.region?.isEmpty()!! || it.kor?.isEmpty()!!) onGetQualityParams(batchNo, materialNo, pos)
            }
        }, itemClick = {
            moveToSummary(bom!!, this)
        })
    }


    private fun updateParams(data: Resource<GenericReqAndResp<List<VegaProcessingQualityDetails>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let { items ->
                        var region: String? = ""
                        var kor: String? = ""
                        items[0].qualityParameters.forEach {
                            if (it.sapQCName?.equals("CI_RCN_REGION")!!) {
                                region = it.satNam
                            } else if (it.sapQCName?.equals("CI_RCN_KOR")!!) {
                                kor = it.satNam
                            }
                        }
                        var msg = ""
                        msg = if (region!!.isNotEmpty() && kor!!.isNotEmpty()) {
                            "Region  :   ".plus(region) + "\n" + "\n" + "KOR  :   ".plus(kor)
                        } else {
                            "Region  :   ".plus("-") + "\n" + "\n" + "KOR  :   ".plus("-")
                        }
                        MaterialDialog(requireContext()).show {
                            title(R.string.quality_details)
                            message(null, msg)
                            positiveButton(
                                text = UIUtils.getSpannedText(
                                    context.getString(com.olam.warehouse.presentation.R.string.ok),
                                    true
                                )
                            )
                        }
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(
                        requireContext(),
                        requireContext().resources.getString(R.string.quality_fetching_failed)
                    )
                    // context?.toast()
                }
                else -> {
                }
            }
        }
    }

    fun onGetQualityParams(batchNumber: String, materialCode: String, pos: Int) {
        whLotsList[pos].isProgress = true
        binding.rvSelectLots.adapter?.notifyItemChanged(pos, whLotsList[pos])
        val input = workDataOf(BATCH_NUMBER to batchNumber, MATERIAL to materialCode)
        val worker = getProcessingQualityRequestWorker(input, pos)
        enQueueWorker(worker, App.getAppContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, Observer { workInfo ->

                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val position = posExtension(workInfo.tags)
                            whLotsList[position].region = workInfo.outputData.getString(REGION)
                            whLotsList[position].kor = workInfo.outputData.getString(KOR)
                            whLotsList[position].isProgress = false
                            binding.rvSelectLots.adapter?.notifyItemChanged(position, whLotsList[position])
                        }
                        WorkInfo.State.FAILED -> {
                            val position = posExtension(workInfo.tags)
                            whLotsList[position].isProgress = false
                            binding.rvSelectLots.adapter?.notifyItemChanged(position, whLotsList[position])

                        }
                        else -> {
                        }
                    }
                }

            })
    }


}
