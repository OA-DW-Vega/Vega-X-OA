package com.olam.warehouse.vegax.mtntghana.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.mtntghana.R
import com.olam.warehouse.vegax.mtntghana.databinding.FragmentGhanaMtntMergeLotListBinding
import com.olam.warehouse.vegax.mtntghana.utils.DISPATCH_SUMMARY
import com.olam.warehouse.vegax.mtntghana.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.mtntghana.utils.getColor
import kotlinx.android.synthetic.main.item_ghana_mtnt_merge_preview_lot_list_detail.*
import kotlinx.android.synthetic.main.item_ghana_mtnt_merge_preview_lot_list_detail.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaGhanaMtntMergePreviewFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_ghana_mtnt_merge_lot_list
    private lateinit var binding: FragmentGhanaMtntMergeLotListBinding

    private var dispatchLotsList = mutableListOf<VegaEcuadorDispatchLots>()
    private var mergedLotsMap = mutableMapOf<Int, ArrayList<VegaEcuadorDispatchLots>>()
    private var model: VegaEcuadorDispatch? = null
    private var callBack: CallBack? = null
    private val vm: VegaGhanaMtntViewModel by viewModel()


    interface CallBack {
        fun replaceFragment(fragment: String, model: VegaEcuadorDispatch, data: Any)
    }

    companion object {
        fun newInstance(model: VegaEcuadorDispatch) =
            VegaGhanaMtntMergePreviewFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentGhanaMtntMergeLotListBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
    }

    private fun initUI() {
        binding.tvTitle.text = getString(R.string.dispatch_merge_lot_preview)
        model = arguments?.getParcelable(MODEL_BUNDLE)
        dispatchLotsList.forEach { it.isAdded = false }

        binding.btnProceed.setBackgroundColor(
            getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green)
        )
        setListeners()
        vm.getDispatchLots(model?.wbTempId ?: "")
    }

    private fun setObservers() {
        vm.dispatchLots.observe(viewLifecycleOwner, Observer {
            dispatchLotsList = it as MutableList<VegaEcuadorDispatchLots>
            dispatchLotsList.sortByDescending { it.pairId }
            dispatchLotsList.forEach { it.isAdded = false }
            setupAdapter()
        })
    }

    private fun setListeners() {
        binding.btnProceed.setOnClickListener {
            callBack?.replaceFragment(DISPATCH_SUMMARY, model!!, dispatchLotsList as ArrayList<VegaEcuadorDispatchLots>)
        }
    }

    private fun setupAdapter() {
        binding.rvSelectLots.addItemDecoration(DividerSpaceItemDecoration(16, dispatchLotsList))
        binding.rvSelectLots.setUp(
            dispatchLotsList,
            R.layout.item_ghana_mtnt_merge_preview_lot_list_detail,
            { it, pos ->
                tvLotId.text = it.batchNumber
                tvMaterialName.text = it.materialName
                tvStLocationValue.text = it.storageLocationCode
                tvWeightValue.text = it.netWeight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitsOfMeasure)
                it.editedWeight?.toDoubleOrNull()?.let { double ->
                    tvWeightToDispatch.text = double.formatThreeDigits().plus(" ").plus(it.unitsOfMeasure)
                }
                if (it.pairId!! > 0) {
                    ivClose.visible()
                    ivSelect.gone()
                } else {
                    ivSelect.visible()
                    ivClose.gone()
                }

                ivSelect.isChecked = it.isAdded ?: false

                ivClose.setOnClickListener { view ->
                    unMergeLots(it, pos)
                }

            },
            itemClick = {

            })
    }

    private fun unMergeLots(it: VegaEcuadorDispatchLots, itemClickedPos: Int) {
        var otherMergedItemPosition = 0
        val sizeOfMergedLots = dispatchLotsList.filterIndexed { position, item ->
            if (item.pairId == it.pairId && position != itemClickedPos)
                otherMergedItemPosition = position
            item.pairId == it.pairId
        }
        it.pairId = 0
        if (sizeOfMergedLots.size == 2) {
            dispatchLotsList[otherMergedItemPosition].pairId = 0
            binding.rvSelectLots.adapter?.notifyDataSetChanged()
        } else {
            ivClose.gone()
            ivSelect.visible()
            binding.rvSelectLots.adapter?.notifyItemChanged(itemClickedPos)
        }
       // vm.saveProcessOrderAndLotDetails(dispatchLotsList, model!!)
    }

}
