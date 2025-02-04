package com.olam.warehouse.vegax.processingindiacoffee.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaFgrnProcessingOrder
import com.olam.warehouse.master.vega.entity.VegaProcessingList
import com.olam.warehouse.master.vega.entity.VegaProcessingRminBom
import com.olam.warehouse.master.vega.entity.VegaProcessingRminBoms
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingindiacoffee.R
import com.olam.warehouse.vegax.processingindiacoffee.data.domain.model.VegaIndiaCoffeeProcessingRminBomPost
import com.olam.warehouse.vegax.processingindiacoffee.databinding.FragmentPoBomSelectLayoutBinding
import com.olam.warehouse.vegax.processingindiacoffee.di.injectCocoaProcessingFeature
import com.olam.warehouse.vegax.processingindiacoffee.utils.ADDLOT
import com.olam.warehouse.vegax.processingindiacoffee.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.processingindiacoffee.utils.getColor
import kotlinx.android.synthetic.main.item_bom_layout.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaIndiaCoffeeRminPoBomSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_po_bom_select_layout
    private lateinit var binding: FragmentPoBomSelectLayoutBinding

    private var callBack: CallBack? = null
    private val vm: VegaIndiaCoffeeRminViewModel by viewModel()
    private var title: String? = ""
    private var isPoSelection: Boolean = true
    private var model: VegaCocoaRminProcessing? = null
    private val mSearchList = mutableListOf<VegaFgrnProcessingOrder>()
    private var poList = ArrayList<VegaFgrnProcessingOrder>()
    private var materialCode1: String = ""
    private var bomList1 = listOf<VegaProcessingRminBoms>()

    interface CallBack {
        fun replaceAddLotFragment(fragment: String, flag: Boolean, model: VegaCocoaRminProcessing, isPoSelection: Boolean)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(title: String, isPo: Boolean, model: VegaCocoaRminProcessing) =
            VegaIndiaCoffeeRminPoBomSelectFragment().putArgs {
                putString("title", title)
                putBoolean("isPo", isPo)
                putParcelable(MODEL_BUNDLE, model)
            }

        const val SEARCH_HINT_TEXT = "Search Po Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentPoBomSelectLayoutBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        title = arguments?.getString("title")
        isPoSelection = arguments?.getBoolean("isPo") ?: false
        model = arguments?.getParcelable(MODEL_BUNDLE)
        materialCode1 = model?.materialCode.toString()
    }

    private fun initUI() {
        binding.tvTitle.text = title
        if (isPoSelection) {
            vm.fetchFgrnPoDetailsList(model?.auartNo ?: "", model?.cgfNo ?: "", model?.foreverNo ?: "")
            binding.tvTitle.text = getString(R.string.select_po)
        } else {
            vm.fetchBomList(
                VegaIndiaCoffeeProcessingRminBomPost(
                    model?.cgfNo ?: "",
                    model?.foreverNo ?: "",
                    model?.materialCode ?: "",
                    getCurrentKey(),
                    getPlantDetails()
                )
            )
            binding.tvTitle.text = getString(R.string.select_bom)
        }
        vm.poDetailList.observe(viewLifecycleOwner, Observer { updateFgrnPoUI(it) })
        vm.bomList.observe(viewLifecycleOwner, Observer { updateBom(it) })
        if (getCurrentKey().split("_")[1].contains("NI")) {
            vm.deleteAllLot(
                model?.poNumber ?: "",
                model?.cgfNo ?: "",
                model?.bom ?: "",
                model?.materialName ?: ""
            )
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        injectCocoaProcessingFeature()
        //initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcocoa/ui/rmin/VegaCocoaRminPoBomSelectFragment")
            .title("Processing Cocoa").with(tracker)
    }

    private fun updateBom(response: Resource<GenericReqAndResp<VegaProcessingRminBom>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            bomList1 = it.data?.data?.boms!!
                            updateBomList(it.data?.data?.boms)
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }


    private fun updateFgrnPoUI(response: Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let {
                                poList.clear()
                                it.forEach { item ->
                                    if (getCurrentKey().split("_")[1].contains("NI")) {
                                        var currentStage = model?.stage.toString()
                                        if (item.materialName?.equals(currentStage) == true) {
                                            val rmindata = item.rmin?.filter { rmin ->
                                                rmin.materialCode!!.contains(
                                                    model?.materialCode.toString()
                                                )
                                            }
                                            rmindata?.forEach { it1 ->
                                                item.materialCode = it1.materialCode
                                                item.materialName = it1.materialName
                                                poList.add(item)
                                            }
                                        }

                                    } else {
                                        var currentStage = model?.stage.toString().split("")[1]
                                        if (item.versionId?.equals(currentStage) == true) {
                                            val rmindata = item.rmin?.filter { rmin ->
                                                rmin.materialCode!!.contains(
                                                    model?.materialCode.toString()
                                                )
                                            }
                                            rmindata?.forEach { it1 ->
                                                item.materialCode = it1.materialCode
                                                item.materialName = it1.materialName
                                                poList.add(item)
                                            }
                                        }
                                    }

/*                                    val rmindata =
                                        item.rmin?.filter { rmin ->
                                            rmin.materialCode!!.contains(
                                                model?.materialCode.toString()
                                            )
                                        }
                                    rmindata?.forEach { it1 ->
                                        item.materialCode = it1.materialCode
                                        item.materialName = it1.materialName
                                        poList.add(item)
                                    }*/
                                }


//                                }
                                /*val data =
                                    it.filter { item -> item.materialCode!!.contains(model?.materialCode.toString()) }
                                poList.addAll(data)*/
                                updateDetails(poList)
                            }

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

    private fun updateDetails(poList: List<VegaFgrnProcessingOrder>) {

        if (poList.size == 1) {
            model?.poNumber = poList[0].processOrderNo
            model?.baseMaterialCode = poList[0].materialCode
            model?.baseMaterialName = poList[0].materialName
            activity?.supportFragmentManager?.popBackStackImmediate()
            callBack?.replaceAddLotFragment(ADDLOT, false, model!!,isPoSelection)

        } else {

            //val dataList = poList.filter { !it.rminList.isNullOrEmpty() } as MutableList<VegaFgrnProcessingOrder>
            var data = poList
            when {
                data.isEmpty() -> {
                    binding.tvError.text = getString(R.string.process_not_avail)
                    binding.tvError.visibility = View.VISIBLE
                    binding.rvPoList.visibility = View.GONE
                }
                else -> {
                    binding.tvError.visibility = View.GONE
                    binding.rvPoList.visibility = View.VISIBLE
                    data = data.sortedByDescending {
                        it.startDate?.split('(', ')')?.get(1)?.let { it1 ->
                            DateUtils.getUTCDateTime(
                                it1,
                                App.getAppContext()
                            )
                        }
                    } as MutableList<VegaFgrnProcessingOrder>
                }
            }

            binding.rvPoList.setUp(data as MutableList, R.layout.item_bom_layout, { item, pos ->
                if (pos % 2 == 0) {
                    clMain.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                } else {
                    clMain.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                }
                tvBom.text = context.getString(R.string.pono)
                tvInputGrade.text = context.getString(R.string.weight_in_process)
                tvOutputGrade.text = context.getString(R.string.dry_start_date)

                tvBomValue.text = item.processOrderNo
                /*val weightToProcess = weightToProcess(item.rminList, item.rfgrnList)
                val weight = DecimalFormat("#########.###").format(weightToProcess).toString()*/
                tvInputGradeValue.text =
                    item.netWeight.plus(" ").plus(item.unitsOfMeasure.toString())
                val times = item.startDate?.split('(', ')')
                tvOutputValue.text = times?.get(1).let { it1 ->
                    it1?.let { it2 ->
                        DateUtils.getUTCDateTime(
                            it2,
                            App.getAppContext()
                        )
                    }
                }

            }, {
                model?.poNumber = this.processOrderNo
                model?.baseMaterialCode = poList[0].materialCode
                model?.baseMaterialName = poList[0].materialName
                model?.weightToProcess =
                    this.netWeight.plus(" ").plus(this.unitsOfMeasure.toString())
                callBack?.replaceAddLotFragment(ADDLOT, false, model!!, isPoSelection)
            })
        }
    }

    private fun weightToProcess(rminList: List<VegaProcessingList>?, rfgrnList: List<VegaProcessingList>?): Double {
        val rminWeight: Double = when {
            !rminList.isNullOrEmpty() -> {
                when {
                    rminList[0].netWeight.toString().isEmpty() -> 0.0
                    else -> rminList.sumByDouble { it.netWeight!!.toDouble() }
                }
            }
            else -> 0.0
        }
        val fgrnWeight: Double = when {
            !rfgrnList.isNullOrEmpty() -> {
                when {
                    rfgrnList[0].netWeight.toString().isEmpty() -> 0.0
                    else -> rfgrnList.sumByDouble { it.netWeight!!.toDouble() }
                }
            }
            else -> 0.0
        }
        return rminWeight - fgrnWeight
    }

    private fun updateBomList(boms: List<VegaProcessingRminBoms>?) {
        boms?.forEach { it.baseMaterialCode = model?.materialCode }
        if (boms?.size == 1) {
            model?.bom = boms[0].materialCode
            model?.materialCode = bomList1[0].materialCode
            model?.baseMaterialCode = boms[0].baseMaterialCode
            model?.baseMaterialName = model?.materialName
            var currentStage = model?.stage.toString().split("-")[1]
            if (getCurrentKey().split("_")[1].contains("NI")) {
                model?.versionId = boms[0].versionId
            } else {
                model?.versionId = currentStage
            }
            activity?.supportFragmentManager?.popBackStackImmediate()
            callBack?.replaceAddLotFragment(ADDLOT, false, model!!, isPoSelection)

        } else {
            val bomList = boms?.toMutableList() ?: mutableListOf()
            binding.rvPoList.setUp(bomList, R.layout.item_bom_layout, { it, pos ->
                if (pos % 2 == 0) {
                    this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                } else {
                    this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                }
                tvBom.text = context.getString(R.string.b_o_m)
                tvInputGrade.text = context.getString(R.string.input_grade)
                tvOutputGrade.text = context.getString(R.string.output_grade)
                tvBomValue.text = it.cfgno
                tvInputGradeValue.text = model?.materialName
                tvOutputValue.text = it.materialName.toString()
                clMain.setOnClickListener { view ->
                    model?.bom = it.cfgno
                    model?.poNumber = ""
                    model?.baseMaterialCode = it.baseMaterialCode
                    model?.materialCode = bomList1[pos].materialCode.toString()
                    model?.baseMaterialName = model?.materialName
                    model?.versionId = it.versionId
                    callBack?.replaceAddLotFragment(ADDLOT, false, model!!,isPoSelection)
                }
            }, {
                /* model?.bom = this.cfgno
                 model?.poNumber = ""
                 model?.baseMaterialCode = this.baseMaterialCode
                 model?.materialCode = this.materialCode ?: ""
                 model?.baseMaterialName = model?.materialName
                 model?.versionId = this.versionId
                 callBack?.replaceAddLotFragment(ADDLOT, false, model!!,isPoSelection)*/
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
            searchView.queryHint = SEARCH_HINT_TEXT
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            updateDetails(poList)
                        } else {
                            mSearchList.clear()
                            poList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.processOrderNo.contains(text)) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            updateDetails(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }
}
