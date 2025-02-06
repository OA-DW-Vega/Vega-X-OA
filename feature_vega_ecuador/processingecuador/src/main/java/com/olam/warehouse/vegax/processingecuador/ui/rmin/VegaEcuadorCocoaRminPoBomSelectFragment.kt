package com.olam.warehouse.vegax.processingecuador.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaFgrnProcessingOrder
import com.olam.warehouse.master.vega.entity.VegaProcessingList
import com.olam.warehouse.master.vega.entity.VegaProcessingRminBom
import com.olam.warehouse.master.vega.entity.VegaProcessingRminBoms
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingecuador.R
import com.olam.warehouse.vegax.processingecuador.data.domain.model.VegaEcuadorProcessingRminBomPost
import com.olam.warehouse.vegax.processingecuador.databinding.FragmentEcuadorCocoaRminPoBomSelectLayoutBinding
import com.olam.warehouse.vegax.processingecuador.databinding.ItemEcuadorCocoaRminBomLayoutBinding
import com.olam.warehouse.vegax.processingecuador.di.injectCocoaProcessingFeature
import com.olam.warehouse.vegax.processingecuador.utils.ADDLOT
import com.olam.warehouse.vegax.processingecuador.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.processingecuador.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaEcuadorCocoaRminPoBomSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_ecuador_cocoa_rmin_po_bom_select_layout
    private lateinit var binding: FragmentEcuadorCocoaRminPoBomSelectLayoutBinding

    private var callBack: CallBack? = null
    private val vm: VegaEcuadorCocoaRminViewModel by viewModel()
    private var title: String? = ""
    private var isPoSelection: Boolean = true
    private var model: VegaCocoaRminProcessing? = null
    private val mSearchList = mutableListOf<VegaFgrnProcessingOrder>()
    private var poList = ArrayList<VegaFgrnProcessingOrder>()
    private var materialCode1: String = ""
    private var bomList1 = listOf<VegaProcessingRminBoms>()

    interface CallBack {
        fun replaceFragment(fragment: String, flag: Boolean, model: VegaCocoaRminProcessing)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(title: String, isPo: Boolean, model: VegaCocoaRminProcessing) =
            VegaEcuadorCocoaRminPoBomSelectFragment().putArgs {
                putString("title", title)
                putBoolean("isPo", isPo)
                putParcelable(MODEL_BUNDLE, model)
            }

        const val SEARCH_HINT_TEXT = "Search Po Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentEcuadorCocoaRminPoBomSelectLayoutBinding.inflate(layoutInflater)
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
        }
        else {
            vm.fetchBomList(
                VegaEcuadorProcessingRminBomPost(
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
            vm.deleteAllLot(
                model?.poNumber ?: "",
                model?.cgfNo ?: "",
                model?.bom ?: "",
                model?.materialName ?: ""
            )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        injectCocoaProcessingFeature()
        //initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingecuador/ui/rmin/VegaCocoaRminPoBomSelectFragment")
            .title("Rmin Cocoa")
            .with(tracker)
    }

    private fun updateBom(response: Resource<GenericReqAndResp<VegaProcessingRminBom>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            updateBomList(it.data?.data?.boms)
                            bomList1 = it.data?.data?.boms!!
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
                                    val rmindata =
                                        item.rmin?.filter { rmin -> rmin.materialCode!!.contains(model?.materialCode.toString()) }
                                    rmindata?.forEach { it1 ->
                                        item.materialCode = it1.materialCode
                                        item.materialName = it1.materialName
                                        poList.add(item)
                                    }
                                }
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
            callBack?.replaceFragment(ADDLOT, false, model!!)

        }
        else {

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

            binding.rvPoList.setUpAdapter(
                data as MutableList,
                R.layout.item_ecuador_cocoa_rmin_bom_layout,
                ItemEcuadorCocoaRminBomLayoutBinding::inflate,
                { item, pos, bindItem ->
                    if (pos % 2 == 0) {
                        bindItem.clMain.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                    } else {
                        bindItem.clMain.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                    }
                    bindItem.tvBom.text = context.getString(R.string.pono)
                    bindItem.tvInputGrade.text = context.getString(R.string.weight_in_process)
                    bindItem.tvOutputGrade.text = context.getString(R.string.dry_start_date)

                    bindItem.tvBomValue.text = item.processOrderNo
                    /*val weightToProcess = weightToProcess(item.rminList, item.rfgrnList)
                    val weight = DecimalFormat("#########.###").format(weightToProcess).toString()*/
                    bindItem.tvInputGradeValue.text =
                        item.netWeight.plus(" ").plus(item.unitsOfMeasure.toString())
                    val times = item.startDate?.split('(', ')')
                    bindItem.tvOutputValue.text = times?.get(1).let { it1 ->
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
                    callBack?.replaceFragment(ADDLOT, false, model!!)
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
            model?.versionId = boms[0].versionId
            activity?.supportFragmentManager?.popBackStackImmediate()
            callBack?.replaceFragment(ADDLOT, false, model!!)

        }
        else {
            val bomList = boms?.toMutableList() ?: mutableListOf()
            binding.rvPoList.setUpAdapter(
                bomList,
                R.layout.item_ecuador_cocoa_rmin_bom_layout,
                ItemEcuadorCocoaRminBomLayoutBinding::inflate,
                { it, pos, bindItem ->
                    if (pos % 2 == 0) {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                    } else {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                    }
                    bindItem.tvBom.text = context.getString(R.string.b_o_m)
                    bindItem.tvInputGrade.text = context.getString(R.string.input_grade)
                    bindItem.tvOutputGrade.text = context.getString(R.string.output_grade)
                    bindItem.tvBomValue.text = it.cfgno
                    bindItem.tvInputGradeValue.text = model?.materialName
                    bindItem.tvOutputValue.text = it.materialName.toString()
                    bindItem.clMain.setOnClickListener { view ->
                        model?.bom = it.cfgno
                        model?.poNumber = ""
                        model?.baseMaterialCode = it.baseMaterialCode
                        model?.materialCode = bomList1[pos].materialCode.toString()
                        model?.baseMaterialName = model?.materialName
                        model?.versionId = it.versionId
                        callBack?.replaceFragment(ADDLOT, false, model!!)
                    }
                }, {
                    /* model?.bom = this.cfgno
                 model?.poNumber = ""
                 model?.baseMaterialCode = this.baseMaterialCode
                 model?.materialCode = this.materialCode ?: ""
                 model?.baseMaterialName = model?.materialName
                 model?.versionId = this.versionId
                 callBack?.replaceFragment(ADDLOT, false, model!!)*/
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
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
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
