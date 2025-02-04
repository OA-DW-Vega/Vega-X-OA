package com.olam.warehouse.vegax.mtntnicaragua.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMtnt
import com.olam.warehouse.master.veganicaragua.model.VegaNicDispatchLotsWithBags
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.mtntnicaragua.R
import com.olam.warehouse.vegax.mtntnicaragua.databinding.FragmentVegaNicaraguaMergeLotBinding
import com.olam.warehouse.vegax.mtntnicaragua.utils.FRAG_ADD_WEIGHT
import com.olam.warehouse.vegax.mtntnicaragua.utils.FRAG_SUMMARY
import com.olam.warehouse.vegax.mtntnicaragua.utils.MODEL_BUNDLE
import kotlinx.android.synthetic.main.fragment_vega_nicaragua_merge_lot.*
import kotlinx.android.synthetic.main.item_nic_weighscale_lot_card_layout.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VegaNicaraguaMergeLotFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VegaNicaraguaMergeLotFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_nicaragua_merge_lot
    private lateinit var binding: FragmentVegaNicaraguaMergeLotBinding
    private var callBack: Callback? = null
    private val vm: VegaNicaraguaMtntViewModel by viewModel()
    private var mtnt: VegaNicaraguaMtnt? = null
    private var editLotId: String = ""
    private var lots: Any? = null

    companion object {
        fun newInstance(model: VegaNicaraguaMtnt) =
            VegaNicaraguaMergeLotFragment().putArgs {
                putParcelable(MODEL_BUNDLE, model)
            }
    }

    interface Callback {
        fun replaceFragment(type: String, data: Any)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? Callback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNicaraguaMergeLotBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        mtnt = arguments?.getParcelable(MODEL_BUNDLE) ?: VegaNicaraguaMtnt()
        vm.mtnt = mtnt ?: VegaNicaraguaMtnt()
        vm.weighBridgeLotsWithBags.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                vm.lotList.clear()
                val lotWithBags = it.lineItems.filter { it.lots.isMergedLot==true }
                vm.lotList.addAll(lotWithBags)
                updateAdapter()
            }
        })
        vm.getMtntWithLots(mtnt?.tempId ?: "")
       binding.btnProceed.setOnClickListener {
           vm.saveWeighBridgeDetails()
           vm.updateLotDetails()
           callBack?.replaceFragment(FRAG_SUMMARY, vm.mtnt)  }
    }
    private fun updateAdapter() {

        var lineItems = mutableListOf<VegaNicDispatchLotsWithBags>()
        if (editLotId.isNotEmpty()) {

            lineItems =
                vm.lotList.filter { it.lots.batchNumber.equals(editLotId) } as MutableList<VegaNicDispatchLotsWithBags>
        } else {
            lineItems = vm.lotList
        }
        rvList.setUp(lineItems, R.layout.item_nic_weighscale_mergelot_card_layout, { it, pos ->

            val lot = it.lots
            val lotBags = it.bagItems
            if (it.bagItems.size > 0)
                lot.editedWeight =
                    it.bagItems.sumByDouble { if (it.netWeight.isNotEmpty()) it.netWeight.toDouble() else 0.0 }
                        .formatThreeDigits()
            else lot.editedWeight = "0"
            vm.lotList[pos].lots.editedWeight = lot.editedWeight
            tvScaleLotValue.text = lot.batchNumber
            tvScaleWeightValue.text =
                (if (lot.weight?.isNotEmpty() == true) lot.weight?.toDouble()?.formatTwoDigits().plus(" ")
                    .plus(lot.unitOfMeasure) else "").toString()
            tvStLocationValue.text = lot.storageLocationCode
            tvScaleGradeValue.text = lot.materialName
            tvScaleDispatchValue.text = lot.editedWeight.plus(" ").plus(lot.unitOfMeasure)
            //cbEndLot.isChecked = lot.isEndLot ?: false
            cbEndLot.setOnCheckedChangeListener { buttonView, isChecked ->
                lot.isEndLot = isChecked
                cbEndLot.isChecked = lot.isEndLot ?: false
                vm.lotList[pos].lots.isEndLot = lot.isEndLot
            }
            tvEndLot.setOnClickListener {
                lot.isEndLot = !lot.isEndLot!!
                cbEndLot.isChecked = lot.isEndLot ?: false
                vm.lotList[pos].lots.isEndLot = lot.isEndLot
            }
            lot.isEndLot=true
            if (editLotId.isNotEmpty()) ivScaleClose.gone() else ivScaleClose.visible()
            ivScaleClose.setOnClickListener {
                showConformationDialog(pos, ivScaleClose)
            }

            tv_add_weight.setOnClickListener {
                if (lotBags.size == 0) vm.mtnt.startTime = DateUtils.getCurrentTimeInMills().toString()
                lot.truckNo = vm.mtnt.vehicleNumber
                lot.bagType = vm.mtnt.bagType
                vm.updateLotDetails()
                callBack?.replaceFragment(FRAG_ADD_WEIGHT, lot)
            }
        })

    }
    private fun showConformationDialog(position: Int, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.conform_remove))
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    vm.removeLotFromList(
                        vm.lotList[position].lots.batchNumber,
                        vm.lotList[position].lots.tempId
                    )
                    vm.lotList.removeAt(position)
                    binding.rvList.adapter?.notifyItemRemoved(position)
                },
                { dismiss() })
        }
    }



}

