package com.olam.warehouse.vegax.processingghana.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnMaerialBatch
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnItemWithGrades
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingghana.R
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaFgrnBagMaterialWithId
import com.olam.warehouse.vegax.processingghana.databinding.FragmentGhanaFgrnBagConsumptionBinding
import com.olam.warehouse.vegax.processingghana.utils.FRAG_ITEM
import com.olam.warehouse.vegax.processingghana.utils.FRAG_SUMMARY
import com.olam.warehouse.vegax.processingghana.utils.getColor
import kotlinx.android.synthetic.main.item_vega_ghana_fgrn_select_batch.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGhanaFgrnBagConsumptionFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_ghana_fgrn_bag_consumption
    private lateinit var binding: FragmentGhanaFgrnBagConsumptionBinding
    private val vm: VegaGhanaFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var fgrnItem = VegaCoffeeFgrnItems()
    private var gradesWithBags = listOf<VegaCoffeeFgrnGradesWithBagItems>()
    private val materialList = arrayListOf<VegaGhanaFgrnBagMaterialWithId>()
    private val batchList = arrayListOf<VegaCoffeeFgrnMaerialBatch>()

    interface CallBack {
        fun replaceFgrnFragment(fragment: String, model: VegaCoffeeFgrnItems, id: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCoffeeFgrnItems) = VegaGhanaFgrnBagConsumptionFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGhanaFgrnBagConsumptionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingsesame/ui/fgrn/VegaSesameFgrnShiftSelectionFragment")
            .title("Processing Coffee")
            .with(tracker)
    }

    private fun initUI() {
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCoffeeFgrnItems()
        binding.btnProceed.setOnClickListener {
            moveToSummary()
        }

        vm.fgrnItem.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getFgrnItems(fgrnItem.fgrnId)
        batchList.addAll(fgrnItem.materialBatchList ?: mutableListOf())
        updateAdapter(batchList)
    }


    private fun updateAdapter(batchList1: ArrayList<VegaCoffeeFgrnMaerialBatch>) {
        if (batchList1.size > 0) binding.clBatch.visible() else binding.clBatch.gone()
        binding.rvBatchSelection.setUp(batchList1, R.layout.item_vega_ghana_fgrn_select_batch, { it, pos ->
            tvMaterial.text = it.materialName
            val list = ArrayList<VegaCoffeeRminLots>()
            val rminLot = VegaCoffeeRminLots()
            rminLot.batchNumber = context.getString(R.string.choose_batch)
            list.add(rminLot)
            list.addAll(it.batchList)
            val lots = list.map { it.batchNumber }
            val stageAdapter = ArrayAdapter(requireContext(), R.layout.item_vega_ghana_processing_rmin_grade, lots)
            stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
            spLot.adapter = stageAdapter
            spLot.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    if (position > 0) {
                        vm.updateBatchToBagDetails(it.materialCode, lots[position], it.bagId)
                        batchList[pos].isValueAdded = true
                    } else {
                        batchList.forEach { it.isValueAdded = false }
                    }
                    if (isAdded)
                        enableDisableBtn(true)
                    else
                        enableDisableBtn(false)
                }
            }
            spLot.setSelection(it.selection + 1)
        },
            {

            })
    }

    private fun updateUI(fgrnItems: VegaCoffeeFgrnItemWithGrades) {
        if (materialList.size == 0) {
            gradesWithBags = fgrnItems.gradeItems?.toList() ?: emptyList()
        }
    }

    private fun moveToSummary() {
        callBack?.replaceFgrnFragment(FRAG_SUMMARY, fgrnItem, "")
    }

    private fun enableDisableBtn(flag: Boolean) {
        if (flag) {
            binding.btnProceed.isEnabled = true
            binding.btnProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnProceed.isEnabled = false
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
    }
}
