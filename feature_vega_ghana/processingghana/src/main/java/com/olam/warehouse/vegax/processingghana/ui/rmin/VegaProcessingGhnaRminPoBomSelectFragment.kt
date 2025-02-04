package com.olam.warehouse.vegax.processingghana.ui.rmin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingghana.R
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessingRminBomPost
import com.olam.warehouse.vegax.processingghana.databinding.FragmentProcessingGhanaBomSelectLayoutBinding
import com.olam.warehouse.vegax.processingghana.utils.ADDLOT
import com.olam.warehouse.vegax.processingghana.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.processingghana.utils.RMIN_GRADES
import com.olam.warehouse.vegax.processingghana.utils.getColor
import kotlinx.android.synthetic.main.item_processing_ghana_bom_layout.view.*
import kotlinx.android.synthetic.main.item_processing_ghana_bom_layout.view.clMain
import kotlinx.android.synthetic.main.item_vega_ghana_fgrn_po_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaProcessingGhnaRminPoBomSelectFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_processing_ghana_bom_select_layout
    private lateinit var binding: FragmentProcessingGhanaBomSelectLayoutBinding

    private var callBack: CallBack? = null
    private val vm: VegaGhanaRminViewModel by viewModel()
    private var title: String? = ""
    private var isPoSelection: Boolean = false
    private var model: VegaCocoaRminProcessing? = null
    private var modelcoffee: VegaCoffeeRminProcessing? = null
    private val mSearchList = mutableListOf<VegaFgrnProcessingOrder>()
    private var poList = ArrayList<VegaFgrnProcessingOrder>()
    private var materialCode1: String = ""
    private var bomList1 = listOf<VegaProcessingRminBoms>()
    private var poOfflineList = ArrayList<VegaFgrnProcessingOrder>()
    private var offlineList = VegaFgrnProcessingOrder()
    private var customLocationList = mutableListOf<VegaCustomStLocation>()

    interface CallBack {
        fun replaceFragment(fragment: String, flag: Boolean, model: VegaCocoaRminProcessing)
        fun replaceFragment(
            fragment: String,
            flag: Boolean,
            model: VegaCoffeeRminProcessing,
            isPoselection: Boolean
        )

        fun replaceFragment(fragment: String, model: VegaCoffeeRminProcessing)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(title: String, isPo: Boolean, model: VegaCocoaRminProcessing) =
            VegaProcessingGhnaRminPoBomSelectFragment().putArgs {
                putString("title", title)
                putBoolean("isPo", isPo)
                putParcelable(MODEL_BUNDLE, model)
            }

        const val SEARCH_HINT_TEXT = "Search Po Item"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentProcessingGhanaBomSelectLayoutBinding.inflate(layoutInflater)
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
        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            customLocationList = it.toMutableList()
        })
        vm.getCustomLocations()
        if (!isPoSelection) {
            if (AppUtils.isOnline()) {
                vm.fetchFgrnPoDetailsList(
                    model?.auartNo ?: "",
                    model?.cgfNo ?: "",
                    model?.foreverNo ?: ""
                )
                binding.tvTitle.text = getString(R.string.select_po)
            } else {
                vm.getProcessOrders()
                vm.processOrderItemLocal.observe(viewLifecycleOwner, Observer {
                    customLocationList.forEach { item ->
                        var tempList =
                            it.filter { item.procureLocationCode == it.storageLocationCode!! }
                        tempList.forEach {
                            offlineList = preparePOList(it)
                            poList.add(offlineList)
                        }
                    }
                    updatePoListDetails(poList)
                })
            }

        } else {
            if (AppUtils.isOnline()) {
                vm.fetchBomList(
                    VegaGhanaProcessingRminBomPost(
                        model?.cgfNo ?: "",
                        model?.foreverNo ?: "",
                        model?.materialCode ?: "",
                        getCurrentKey(),
                        getPlantDetails()
                    )
                )
            } else {
                vm.getbomoffline(model!!.materialCode)
                vm.BomOfflineLocal.observe(viewLifecycleOwner, Observer {
                    bomList1 = it
                    updateBomList(it)
                    Log.d("fetchbommodule", it.toString())
                })
            }

            binding.tvTitle.text = getString(R.string.select_bom)
        }
        vm.poDetailList.observe(viewLifecycleOwner, Observer { updateFgrnPoUI(it) })
        vm.bomList.observe(viewLifecycleOwner, Observer { updateBom(it) })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
                    poList.clear()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let {
                                it.forEach { item ->
                                    val rmindata =
                                        item.rmin?.filter { rmin ->
                                            rmin.materialCode!!.contains(
                                                model?.materialCode.toString()
                                            )
                                        }
                                    rmindata?.forEach { it1 ->
                                        item.materialCode = it1.materialCode
                                        item.materialName = it1.materialName

                                    }
                                    poList.add(item)
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

            binding.rvPoList.setUp(
                data as MutableList,
                R.layout.item_processing_ghana_bom_layout,
                { item, pos ->
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
                        it1.let { it2 ->
                            DateUtils.getUTCDateTime(
                                it2.toString(),
                                App.getAppContext()
                            )
                        }
                    }

                },
                {
                    modelcoffee = VegaCoffeeRminProcessing()
                    modelcoffee?.auartNo = model?.auartNo
                    modelcoffee?.foreverNo = model!!.foreverNo
                    modelcoffee?.cgfNo = model!!.cgfNo
                    modelcoffee?.poNumber = this.processOrderNo
                    modelcoffee?.baseMaterialCode = this.materialCode
                    modelcoffee?.baseMaterialName = this.materialName
                    modelcoffee?.stage = model!!.stage
                    modelcoffee?.vendor = vendor
                    navigateToNext(modelcoffee ?: VegaCoffeeRminProcessing())
                    /*model?.poNumber = this.processOrderNo
                    model?.baseMaterialCode = poList[0].materialCode
                    model?.baseMaterialName = poList[0].materialName
                    callBack?.replaceFragment(ADDLOT, false, model!!)*/
                })
        }
    }

    private fun weightToProcess(
        rminList: List<VegaProcessingList>?,
        rfgrnList: List<VegaProcessingList>?
    ): Double {
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
    private fun navigateToNext(model: VegaCoffeeRminProcessing) {
        callBack?.replaceFragment(
            RMIN_GRADES, model
        )
    }
    private fun updateBomList(boms: List<VegaProcessingRminBoms>?) {
        boms?.forEach { it.baseMaterialCode = model?.materialCode!! }
        if (boms?.size == 1) {

            modelcoffee = VegaCoffeeRminProcessing()
            modelcoffee?.bom = boms[0].cfgno
            modelcoffee?.cgfNo = boms[0].cfgno
            modelcoffee?.materialCode = model?.materialCode.toString()
            modelcoffee?.materialName = model?.materialName.toString()
            modelcoffee?.stage = model?.stage.toString()
            modelcoffee?.poNumber = ""
            modelcoffee?.baseMaterialCode = bomList1[0].materialCode
            if (AppUtils.isOnline())
                modelcoffee?.materialCode = bomList1[0].materialCode.toString()
            modelcoffee?.baseMaterialName = model?.materialName
            modelcoffee?.versionId = boms[0].versionId
            activity?.supportFragmentManager?.popBackStackImmediate()
            callBack?.replaceFragment(ADDLOT, false, modelcoffee!!, isPoSelection)

        } else {
            val bomList = boms?.toMutableList() ?: mutableListOf()
            binding.rvPoList.setUp(bomList, R.layout.item_processing_ghana_bom_layout, { it, pos ->
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
                    modelcoffee = VegaCoffeeRminProcessing()
                    modelcoffee?.bom = it.cfgno
                    modelcoffee?.bom = bomList1[pos].cfgno
                    modelcoffee?.cgfNo = bomList1[pos].cfgno
                    modelcoffee?.materialCode = model?.materialCode.toString()
                    modelcoffee?.materialName = model?.materialName.toString()
                    modelcoffee?.stage = model?.stage.toString()
                    modelcoffee?.poNumber = ""
                    modelcoffee?.baseMaterialCode = it.materialCode
                    modelcoffee?.materialCode = bomList1[pos].materialCode.toString()
                    modelcoffee?.baseMaterialName = model?.materialName
                    modelcoffee?.versionId = it.versionId
                    callBack?.replaceFragment(ADDLOT, false, modelcoffee!!, isPoSelection)
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
    private fun updatePoListDetails(poList: List<VegaFgrnProcessingOrder>) {
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
        binding.rvPoList.setUp(
            data as MutableList,
            R.layout.item_vega_ghana_fgrn_po_details,
            { item, pos ->

                tvPoNo.text = item.processOrderNo
                tv_material_name.text = item.materialName
                tvWeight.text = item.netWeight.plus(" ").plus("MT")
                val times = item.startDate?.split('(', ')')
                tvDate.text = times?.get(1).let { it1 ->
                    it1?.let { it2 ->
                        DateUtils.getUTCDateTime(
                            it2,
                            App.getAppContext()
                        )
                    }
                }

            },
            {
                modelcoffee = VegaCoffeeRminProcessing()
                modelcoffee?.auartNo = model?.auartNo!!
                modelcoffee?.foreverNo = model?.foreverNo!!
                modelcoffee?.cgfNo = model?.cgfNo ?: ""
                modelcoffee?.poNumber = this.processOrderNo
                modelcoffee?.baseMaterialCode = this.materialCode
                modelcoffee?.baseMaterialName = this.materialName
                modelcoffee?.stage = model?.stage!!
                modelcoffee?.vendor = vendor
                navigateToNext(modelcoffee ?: VegaCoffeeRminProcessing())
            })
    }
    private fun preparePOList(poOfflineList: VegaGhanaProcessingOrder): VegaFgrnProcessingOrder {
        var offline = VegaFgrnProcessingOrder()
        offline.netWeight = poOfflineList.netWeight
        offline.auart = poOfflineList.auart
        offline.processOrderNo = poOfflineList.processOrderNo
        offline.materialCode = poOfflineList.materialCode
        offline.materialName = poOfflineList.materialName
        offline.meins = poOfflineList.unitsOfMeasure!!
        offline.plant = poOfflineList.plant
        offline.startDate = poOfflineList.startDate
        offline.storageLocationCode = poOfflineList.storageLocationCode
        offline.vendor = poOfflineList.vendor
        offline.unitsOfMeasure = poOfflineList.unitsOfMeasure
        return offline
    }
}
