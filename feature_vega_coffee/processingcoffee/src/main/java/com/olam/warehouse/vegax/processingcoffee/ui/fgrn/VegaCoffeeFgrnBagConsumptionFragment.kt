package com.olam.warehouse.vegax.processingcoffee.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnMaerialBatch
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnItemWithGrades
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingcoffee.R
import com.olam.warehouse.vegax.processingcoffee.data.domain.model.VegaCoffeeFgrnBagMaterialWithId
import com.olam.warehouse.vegax.processingcoffee.databinding.FragmentFgrnBagConsumptionBinding
import com.olam.warehouse.vegax.processingcoffee.databinding.ItemVegaCoffeeFgrnSelectBatchBinding
import com.olam.warehouse.vegax.processingcoffee.utils.FRAG_ITEM
import com.olam.warehouse.vegax.processingcoffee.utils.FRAG_SUMMARY
import com.olam.warehouse.vegax.processingcoffee.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCoffeeFgrnBagConsumptionFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_fgrn_bag_consumption
    private lateinit var binding: FragmentFgrnBagConsumptionBinding
    private val vm: VegaCoffeeFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var fgrnItem = VegaCoffeeFgrnItems()
    private var gradesWithBags = listOf<VegaCoffeeFgrnGradesWithBagItems>()
    private val materialList = arrayListOf<VegaCoffeeFgrnBagMaterialWithId>()
    private val batchList = arrayListOf<VegaCoffeeFgrnMaerialBatch>()

    interface CallBack {
        fun replaceFgrnFragment(fragment: String, model: VegaCoffeeFgrnItems, id: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCoffeeFgrnItems) = VegaCoffeeFgrnBagConsumptionFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFgrnBagConsumptionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcoffee/ui/fgrn/VegaCoffeeFgrnShiftSelectionFragment")
            .title("IVC/Coffee/Processing/FGRN Bag Consumption")
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
        binding.rvBatchSelection.setUpAdapter(
            batchList1,
            R.layout.item_vega_coffee_fgrn_select_batch,
            ItemVegaCoffeeFgrnSelectBatchBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvMaterial.text = it.materialName
                val list = ArrayList<VegaCoffeeRminLots>()
                val rminLot = VegaCoffeeRminLots()
                rminLot.batchNumber = context.getString(R.string.choose_batch)
                list.add(rminLot)
                list.addAll(it.batchList)
                val lots = list.map { it.batchNumber }
                val stageAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.item_vega_coffee_processing_rmin_grade,
                    lots
                )
                stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
                bindItem.spLot.adapter = stageAdapter
                bindItem.spLot.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            position: Int,
                            p3: Long
                        ) {
                            if (position > 0) {
                                vm.updateBatchToBagDetails(
                                    it.materialCode,
                                    lots[position],
                                    it.bagId
                                )
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
                bindItem.spLot.setSelection(it.selection + 1)
            },
            {

            })
    }

    private fun updateUI(fgrnItems: VegaCoffeeFgrnItemWithGrades) {
        if (materialList.size == 0) {
            gradesWithBags = fgrnItems.gradeItems?.toList() ?: emptyList()
            //vm.getPoGrades(fgrnItem.processOrderNo, isRmin)
        }
    }

    private fun moveToSummary() {
        callBack?.replaceFgrnFragment(FRAG_SUMMARY, fgrnItem, "")
    }

    private fun enableDisableBtn(flag: Boolean) {
        if (flag) {
            binding.btnProceed.isEnabled = true
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btnProceed.isEnabled = false
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
    }
}
