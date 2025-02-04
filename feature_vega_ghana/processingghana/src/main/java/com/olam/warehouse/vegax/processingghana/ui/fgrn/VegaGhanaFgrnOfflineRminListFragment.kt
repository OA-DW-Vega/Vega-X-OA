package com.olam.warehouse.vegax.processingghana.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.master.vega.entity.VegaProcessingList
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminProcessLotDetails
import com.olam.warehouse.master.vegaghana.model.VegaGhanaOfflineRmin
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingghana.R
import com.olam.warehouse.vegax.processingghana.databinding.FragmentVegaGhanaOfflineRminListBinding
import com.olam.warehouse.vegax.processingghana.utils.*
import kotlinx.android.synthetic.main.item_ghana_rmin.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaGhanaFgrnOfflineRminListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_ghana_offline_rmin_list
    private lateinit var binding: FragmentVegaGhanaOfflineRminListBinding
    private val vm: VegaGhanaFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var gradeList = listOf<VegaCoffeeFgrnItemsGrades>()
    private var fgrnItem = VegaCoffeeFgrnItems()
    private var noOfGradeItems: Int = 0
    private var isEdit = false
    private var vegaStage = VegaProcessingStage()
    private var processOrder: String? = ""
    private var poGrade: String? = ""
    private var offloadingItems = arrayListOf<VegaGhanaOfflineRminProcessLotDetails>()
    private var rminlist = mutableListOf<VegaProcessingList>()

    interface CallBack {
        fun replaceFgrnFragment(fragment: String, model: VegaCoffeeFgrnItems, id: String, stage: VegaProcessingStage)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(
            model: VegaCoffeeFgrnItems,
            id: String,
            isEdit: Boolean,
            vegaStage: VegaProcessingStage
        ) = VegaGhanaFgrnOfflineRminListFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
            putString(FRAG_ID, id)
            putBoolean(IS_EDIT, isEdit)
            putParcelable(VEGA_STAGE, vegaStage)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaGhanaOfflineRminListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingsesame/ui/fgrn/VegaSesameFgrnAddWeightAndLotsFragment")
            .title("Processing Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCoffeeFgrnItems()
        poGrade = arguments?.getString(FRAG_ID) ?: ""
        gradeList = Gson().fromJson<List<VegaCoffeeFgrnItemsGrades>>(arguments?.getString(FRAG_ID) ?: "")
        isEdit = arguments?.getBoolean(IS_EDIT) ?: false
        processOrder = fgrnItem.processOrderNo
        vegaStage = arguments?.getParcelable(VEGA_STAGE) ?: VegaProcessingStage()
        noOfGradeItems = gradeList.size

        vm.offlineRminItemLocal.observe(this, Observer {
            if (it != null) {
                updateUI(it)
            }
        })
        vm.getOfflineRminItem()

        binding.tvLotNo.text = fgrnItem.processOrderNo
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    private fun updateUI(data: List<VegaGhanaOfflineRmin>?) {
        data?.let { receiving ->
            val offline = arrayListOf<VegaGhanaOfflineRminProcessLotDetails>()
            when {
                receiving.isNotEmpty() -> {
                    receiving.forEach {
                        offline.addAll(it.rminLot)
                    }
                    var tempList =
                        offline.filter { it.processOrderNum == fgrnItem.processOrderNo || it.rminTempId == fgrnItem.processOrderNo && !it.fgrnStatus!! } as ArrayList<VegaGhanaOfflineRminProcessLotDetails>
                    setUpAdapter(tempList)
                }
                else -> {
                    setUpAdapter(offline)
                    setErrorContentView("No data available")
                }
            }
        }
    }

    private fun setUpAdapter(offloadingItems1: ArrayList<VegaGhanaOfflineRminProcessLotDetails>) {
        var totalWeight = 0.0
        offloadingItems = offloadingItems1
        if (offloadingItems.size > 0) {
            binding.tvNoData.gone()
        } else {
            binding.tvNoData.visible()
        }
        binding.rvPoList.setUp(
            offloadingItems,
            R.layout.item_ghana_rmin,
            { it, pos ->
                val receiving = it
                tvScaleDispatchValue.text = receiving.netWeight.plus(" ").plus("MT")
                tvScaleGradeValue.text = receiving.materialName
                tvScaleLotValue.text = receiving.rminTempId
                val qualityParams = VegaProcessingList()
                qualityParams.processOrderNo = receiving.poNo!!
                qualityParams.netWeight = receiving.netWeight
                qualityParams.materialName = receiving.materialName
                qualityParams.materialCode = receiving.materialCode
                qualityParams.unitsOfMeasure = receiving.unitsOfMeasure
                qualityParams.storageLocationCode = receiving.storageLocationCode
                qualityParams.batchNumber = receiving.batchNumber
                rminlist.add(qualityParams)
                fgrnItem.rminList = rminlist
                rminlist.forEach {
                    totalWeight = totalWeight.plus(it.netWeight!!.toDouble())
                }
                fgrnItem.rminTotal = totalWeight.toString()
            },
            {
                val item = this
                fgrnItem.rminId = item.rminTempId
                fgrnItem.rminQty = item.netWeight

                callBack?.replaceFgrnFragment(FRAG_ADD_WEIGHT, fgrnItem, poGrade!!, vegaStage)
            })
    }

}
