package com.olam.warehouse.vegax.dispatchecuador.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.dispatchecuador.R
import com.olam.warehouse.vegax.dispatchecuador.databinding.FragmentEcuadorDispatchMergeLotListBinding
import com.olam.warehouse.vegax.dispatchecuador.utils.DISPATCH_SUMMARY
import com.olam.warehouse.vegax.dispatchecuador.utils.MERGE_PREVIEW
import com.olam.warehouse.vegax.dispatchecuador.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.dispatchecuador.utils.getColor
import kotlinx.android.synthetic.main.item_ecuador_dispatch_merge_lot_list_detail.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

const val DISPATCH_DATA_LIST = "dispatch_intent_data_list"

class VegaEcuadorDispatchMergeFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_ecuador_dispatch_merge_lot_list
    private lateinit var binding: FragmentEcuadorDispatchMergeLotListBinding
    private var callBack: CallBack? = null

    private var dispatchLotsList = mutableListOf<VegaEcuadorDispatchLots>()
    private var materialCodeMap = mutableMapOf<String, Int>()
    private var totalSelectedLots = 0
    private var pairId = 0
    private var model: VegaEcuadorDispatch? = null
    private val vm: VegaEcuadorDispatchViewModel by viewModel()


    interface CallBack {
        fun replaceFragment(fragment: String, model: VegaEcuadorDispatch, data: Any)
    }

    companion object {
        fun newInstance(model: VegaEcuadorDispatch) =
            VegaEcuadorDispatchMergeFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentEcuadorDispatchMergeLotListBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
    }

    private fun initUI() {
        model = arguments?.getParcelable(MODEL_BUNDLE)
        binding.btnProceed.setBackgroundColor(
            getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green)
        )
        setListeners()
        vm.getDispatchLots(model?.wbTempId ?: "")
    }

    private fun setObservers() {
        vm.dispatchLots.observe(viewLifecycleOwner, Observer {
            dispatchLotsList = it as MutableList<VegaEcuadorDispatchLots>
            dispatchLotsList.forEach {
                it.isAdded = false
                if (it.pairId!! > pairId) pairId = it.pairId!!
            }
            dispatchLotsList = dispatchLotsList.filter { it.pairId == 0 } as MutableList<VegaEcuadorDispatchLots>
            if (dispatchLotsList.size > 0) setupAdapter(dispatchLotsList)
            else {
                binding.rvSelectLots.gone()
                binding.tvNoData.visible()
            }
        })
    }

    private fun setListeners() {
        binding.btMerge.setOnClickListener {
            // if map is 1 means => two different materials are not selected to merge.
            // if value of map is checked, whether atleast two items are selected to merge.
            if (materialCodeMap.size == 1) {
                for ((k, v) in materialCodeMap) {
                    if (v > 1) {
                        pairId++
                        val filtered = dispatchLotsList.filter { !it.isAdded!! }
                        setupAdapter(filtered as MutableList<VegaEcuadorDispatchLots>)
                        dispatchLotsList = dispatchLotsList.map {
                            if (it.isAdded!! && it.pairId!! <= 0)
                                it.pairId = pairId
                            it
                        } as MutableList<VegaEcuadorDispatchLots>
                        binding.btnProceed.visible()
                        binding.btMerge.gone()
                        if (filtered.isEmpty()) {
                            binding.rvSelectLots.gone()
                            binding.tvNoData.visible()
                        } else {
                            binding.rvSelectLots.visible()
                            binding.tvNoData.gone()
                        }
                        totalSelectedLots = 0
                        materialCodeMap.clear()
                        vm.saveProcessOrderAndLotDetails(dispatchLotsList, model!!)
                    }
                }
            }
            if (materialCodeMap.size > 1)
                Toast.makeText(activity, getString(R.string.dispatch_lot_merge_error), Toast.LENGTH_SHORT).show()
        }

        binding.btnProceed.setOnClickListener {
            if (pairId > 0)
                moveToMergePreview()
            else
                moveToSummaryScreen()
        }
    }

    private fun setupAdapter(data: MutableList<VegaEcuadorDispatchLots>) {
        binding.rvSelectLots.setUp(data, R.layout.item_ecuador_dispatch_merge_lot_list_detail, { it, pos ->
            tvLotId.text = it.batchNumber
            tvMaterialName.text = it.materialName
            tvStLocationValue.text = it.storageLocationCode
            tvWeightValue.text = it.netWeight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitsOfMeasure)
            it.editedWeight?.toDoubleOrNull()?.let { double ->
                tvWeightToDispatch.text = double.formatThreeDigits().plus(" ").plus(it.unitsOfMeasure)
            }
            ivSelect.visible()
            ivSelect.isChecked = it.isAdded ?: false

            llLotItem.setOnClickListener { view ->
                it.isAdded = !it.isAdded!!
                val incOrDec = if (it.isAdded!!) {
                    totalSelectedLots++; 1
                } else {
                    totalSelectedLots--; -1
                }

                it.materialCode?.let { materialCode ->
                    when (val count = materialCodeMap[materialCode]) {
                        null -> materialCodeMap[materialCode] = 1
                        else -> materialCodeMap[materialCode] = count + incOrDec
                    }
                }

                if (totalSelectedLots > 1) {
                    binding.btMerge.visible()
                    binding.btnProceed.gone()
                } else {
                    binding.btnProceed.visible()
                    binding.btMerge.gone()
                }

                if (materialCodeMap[it.materialCode] == 0)
                    materialCodeMap.remove(it.materialCode)

                dispatchLotsList.forEach { item ->
                    if (item.batchNumber.equals(it.batchNumber)) item.isAdded = it.isAdded
                }
                binding.rvSelectLots.adapter?.notifyItemChanged(pos)
            }

        }, itemClick = {

        })
    }

    private fun moveToMergePreview() {
        //dispatchLotsList.sortByDescending { it.pairId }
        callBack?.replaceFragment(MERGE_PREVIEW, model!!, dispatchLotsList as ArrayList<VegaEcuadorDispatchLots>)
    }

    private fun moveToSummaryScreen() {
        callBack?.replaceFragment(DISPATCH_SUMMARY, model!!, dispatchLotsList as ArrayList<VegaEcuadorDispatchLots>)
    }

}
