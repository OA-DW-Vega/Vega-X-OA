package com.olam.warehouse.vegax.mtntnicaragua.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMtnt
import com.olam.warehouse.master.veganicaragua.model.VegaNicDispatchLotsWithBags
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.mtntnicaragua.R
import com.olam.warehouse.vegax.mtntnicaragua.databinding.FragmentVegaNicaraguaMergeLotBinding
import com.olam.warehouse.vegax.mtntnicaragua.databinding.ItemNicWeighscaleMergelotCardLayoutBinding
import com.olam.warehouse.vegax.mtntnicaragua.utils.FRAG_ADD_WEIGHT
import com.olam.warehouse.vegax.mtntnicaragua.utils.FRAG_SUMMARY
import com.olam.warehouse.vegax.mtntnicaragua.utils.MODEL_BUNDLE
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
class VegaNicaraguaMergeLotFragment : BaseFragment(),BaseFragment.DialogClick {
    override val layoutResourceId = R.layout.fragment_vega_nicaragua_merge_lot
    private lateinit var binding: FragmentVegaNicaraguaMergeLotBinding
    private var callBack: Callback? = null
    private val vm: VegaNicaraguaMtntViewModel by viewModel()
    private var mtnt: VegaNicaraguaMtnt? = null
    private var editLotId: String = ""
    private var lots: Any? = null
    var lineItems = mutableListOf<VegaNicDispatchLotsWithBags>()

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
                val lotWithBags = it.lineItems.filter { it.lots.isMergedLot == true }
                vm.lotList.addAll(lotWithBags)
                updateAdapter()
            }
        })
        vm.getMtntWithLots(mtnt?.tempId ?: "")
        binding.btnProceed.setOnClickListener {
            proceedLotAfterCheck()
            //showRemarkDialog()
        }
    }

    private  fun proceedLotAfterCheck(){
        if (validateExceedSoWeight()) {
            if (validateEmptyWeight()) {
                showRemarkDialog()

            } else Toast.makeText(
                activity,
                getString(R.string.zero_weight_error),
                Toast.LENGTH_SHORT
            ).show()
        } else Toast.makeText(
            activity,
            getString(R.string.less_weight_so_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun validateEmptyWeight(): Boolean {
        val emptyWeight =
            vm.lotList.filter {
                it.lots.editedWeight.equals("0.0") || it.lots.editedWeight.equals("0") || it.lots.editedWeight.equals(
                    ""
                )
            }
        return emptyWeight.isEmpty()
    }

    private fun validateExceedSoWeight(): Boolean {
//        val soWeight = 1000.0
        val soWeight = vm.mtnt.soWeight?.toDouble() ?: 0.0
        val editedWeight = vm.lotList.sumByDouble {
            if (it.lots.editedWeight?.isNotEmpty() == true) it.lots.editedWeight?.toDouble() ?: 0.0 else 0.0
        }
        return when {
            vm.mtnt.soUOM.equals("kg", true) -> editedWeight <= soWeight
            vm.mtnt.soUOM.equals("MT", true) -> editedWeight.div(1000) <= soWeight
            else -> true
        }
    }

    private fun updateAdapter() {

        if (editLotId.isNotEmpty()) {

            lineItems =
                vm.lotList.filter { it.lots.batchNumber.equals(editLotId) } as MutableList<VegaNicDispatchLotsWithBags>
        } else {
            lineItems = vm.lotList
        }
        binding.rvList.setUpAdapter(
            lineItems,
            R.layout.item_nic_weighscale_mergelot_card_layout,
            ItemNicWeighscaleMergelotCardLayoutBinding::inflate,
            { it, pos, bindItem ->

                val lot = it.lots
                val lotBags = it.bagItems
                if (it.bagItems.size > 0)
                    lot.editedWeight =
                        it.bagItems.sumByDouble { if (it.netWeight.isNotEmpty()) it.netWeight.toDouble() else 0.0 }
                            .formatThreeDigits()
                else lot.editedWeight = "0"
                vm.lotList[pos].lots.editedWeight = lot.editedWeight
                bindItem.tvScaleLotValue.text = lot.batchNumber
                bindItem.tvScaleWeightValue.text =
                    (if (lot.weight?.isNotEmpty() == true) lot.weight?.toDouble()?.formatTwoDigits()
                        .plus(" ")
                        .plus(lot.unitOfMeasure) else "").toString()
                bindItem.tvStLocationValue.text = lot.storageLocationCode
                bindItem.tvScaleGradeValue.text = lot.materialName
                bindItem.tvScaleDispatchValue.text =
                    lot.editedWeight.plus(" ").plus(lot.unitOfMeasure)
                //cbEndLot.isChecked = lot.isEndLot ?: false
                bindItem.cbEndLot.setOnCheckedChangeListener { buttonView, isChecked ->
                    lot.isEndLot = isChecked
                    bindItem.cbEndLot.isChecked = lot.isEndLot ?: false
                    vm.lotList[pos].lots.isEndLot = lot.isEndLot
                }
                bindItem.tvEndLot.setOnClickListener {
                    lot.isEndLot = !lot.isEndLot!!
                    bindItem.cbEndLot.isChecked = lot.isEndLot ?: false
                    vm.lotList[pos].lots.isEndLot = lot.isEndLot
                }
                lot.isEndLot = true
                if (editLotId.isNotEmpty()) bindItem.ivScaleClose.gone() else bindItem.ivScaleClose.visible()
                bindItem.ivScaleClose.setOnClickListener {
                    showConformationDialog(pos, bindItem.ivScaleClose)
                }

                bindItem.tvAddWeight.setOnClickListener {
                    if (lotBags.size == 0) vm.mtnt.startTime =
                        DateUtils.getCurrentTimeInMills().toString()
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


    private fun showRemarkDialog() {
        showDialogWithInput(getString(com.olam.warehouse.login.R.string.remarks),this,true,getString(R.string.remarks))

    }

    override fun onPositive(remark: String) {
        if (!remark.isNullOrEmpty()) {
            vm.mtnt.remarks = remark

            vm.saveWeighBridgeDetails()
            vm.updateLotDetails()
            callBack?.replaceFragment(FRAG_SUMMARY, vm.mtnt)
        }else{
            Toast.makeText(requireContext(), getString(R.string.enter_remark), Toast.LENGTH_SHORT).show()
        }
    }


}

