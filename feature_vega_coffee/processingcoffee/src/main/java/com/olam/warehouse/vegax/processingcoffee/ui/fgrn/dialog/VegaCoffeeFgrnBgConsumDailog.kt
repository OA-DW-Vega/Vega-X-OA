package com.olam.warehouse.vegax.inventory.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnMaerialBatch
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.processingcoffee.R
import com.olam.warehouse.vegax.processingcoffee.databinding.FragmentFgrnBagConsumptionBinding
import com.olam.warehouse.vegax.processingcoffee.ui.fgrn.VegaCoffeeFgrnViewModel
import com.olam.warehouse.vegax.processingcoffee.utils.BATCH_LIST
import com.olam.warehouse.vegax.processingcoffee.utils.getColor
import kotlinx.android.synthetic.main.item_vega_coffee_fgrn_select_batch.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 4/8/2020.
 */
class VegaCoffeeFgrnBgConsumDailog : BottomSheetDialogFragment() {

    private var batchList = ArrayList<VegaCoffeeFgrnMaerialBatch>()
    private val vm: VegaCoffeeFgrnViewModel by viewModel()

    companion object {
        fun newInstance(batchList: ArrayList<VegaCoffeeFgrnMaerialBatch>) = VegaCoffeeFgrnBgConsumDailog().putArgs {
            putParcelableArrayList(BATCH_LIST, batchList)
        }
    }

    private lateinit var binding: FragmentFgrnBagConsumptionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFgrnBagConsumptionBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        batchList = arguments?.getParcelableArrayList<VegaCoffeeFgrnMaerialBatch>(BATCH_LIST)!!
        setUpAdapter(batchList)
        binding.btnProceed.setOnClickListener {
            activity?.onBackPressed()
            dialog?.dismiss()
        }
    }

    private fun setUpAdapter(batchList: ArrayList<VegaCoffeeFgrnMaerialBatch>) {
        if (batchList.size > 0) binding.clBatch.visible() else binding.clBatch.gone()
        binding.rvBatchSelection.setUp(batchList, R.layout.item_vega_coffee_fgrn_select_batch, { it, pos ->
            tvMaterial.text = it.materialName
            val list = ArrayList<VegaCoffeeRminLots>()
            val rminLot = VegaCoffeeRminLots()
            rminLot.batchNumber = context.getString(R.string.choose_batch)
            list.add(rminLot)
            list.addAll(it.batchList)
            val lots = list.map { it.batchNumber }
            val stageAdapter = ArrayAdapter(requireContext(), R.layout.item_vega_coffee_processing_rmin_grade, lots)
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

    private fun enableDisableBtn(flag: Boolean) {
        if (flag) {
            binding.btnProceed.isEnabled = true
            binding.btnProceed.setBackgroundColor(getColor(if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnProceed.isEnabled = false
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
    }
}
