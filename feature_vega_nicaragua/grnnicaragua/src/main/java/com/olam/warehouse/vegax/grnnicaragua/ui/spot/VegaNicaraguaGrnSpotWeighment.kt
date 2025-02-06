package com.olam.warehouse.vegax.grnnicaragua.ui.spot

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.RECEIVING_DATA
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnnicaragua.R
import com.olam.warehouse.vegax.grnnicaragua.databinding.FragmentVegaNicaraguaGrnWeighmentBinding
import com.olam.warehouse.vegax.grnnicaragua.databinding.ItemVegaNicaraguaGrnSupplierBagBinding
import com.olam.warehouse.vegax.grnnicaragua.ui.VegaNicaraguaGrnViewModel
import com.olam.warehouse.vegax.grnnicaragua.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.random.Random

/**
 * Created by Keerthi Santhanam on 9/05/2020
 */
class VegaNicaraguaGrnSpotWeighment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_vega_nicaragua_grn_weighment

    private lateinit var binding: FragmentVegaNicaraguaGrnWeighmentBinding
    private var callBack: CallBack? = null
    private val vm: VegaNicaraguaGrnViewModel by viewModel()

    private var receivingData = VegaReceiving()
    private var bagList = arrayListOf<VegaNicaraguaWeighmentBagMaterial>()
    private var palletWeight: String = "0"
    private var palletCount: String = "0"
    private var palletAvg: String = "0"
    private var bagCountTotal = 0

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            weighmentBagMaterialData: Any
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(receivingData: VegaReceiving) = VegaNicaraguaGrnSpotWeighment().putArgs {
            putParcelable(RECEIVING_DATA, receivingData)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaGrnWeighmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnnicaragua/ui/VegaNicaraguaGrnSpotWeighment")
            .title("Nicaragua GRN")
            .with(tracker)
        initUI()
    }
    override fun onResume() {
        changeState(2, binding.stateBar.root, context)
        super.onResume()
    }
    private fun initUI() {
        receivingData = arguments?.getParcelable(RECEIVING_DATA)!!
        if (receivingData.grnType.equals(getString(R.string.ptbf))) binding.tvTitle.text =
            getString(R.string.grn_ptbf_weighment_summary)
        else if (receivingData.grnType.equals(getString(R.string.fixed))) binding.tvTitle.text =
            getString(R.string.grn_fixed_weighment_summary)
        binding.btAddWeight.setOnClickListener { moveBagAddWeight(VegaNicaraguaWeighmentBagMaterial()) }
        binding.btProceed.setOnClickListener { moveToQuality() }
        vm.getBagItems(receivingData.tmpWbId)
        vm.bagItems.observe(viewLifecycleOwner, Observer { updateBagItems(it) })
    }

    private fun moveBagAddWeight(bagMaterial: VegaNicaraguaWeighmentBagMaterial) {
        val bundle = Bundle()
        bundle.putString(MATERIAL_NAME, receivingData.materialName)
        bundle.putString(UNITS_OF_MEASURE, receivingData.unitsOfMeasure)
        bundle.putParcelable(BAG_MATERIAL, bagMaterial)
        bundle.putString(BAG_TYPE, receivingData.bagType)
        callBack?.replaceFragment(FRAG_ADD_BAG_WEIGHT, bundle)
    }

    private fun updateBagItems(bagItems: List<VegaNicaraguaWeighmentBagMaterial>?) {
        bagItems?.let {
            bagList.clear()
            bagList.addAll(bagItems)
            if (bagItems.size > 0) {
                val bundle = Bundle()
                bundle.putString(
                    Constants.PALLET_WEIGHT,
                    if (palletWeight.isEmpty() || palletWeight.equals("0")) bagList[0].palletWeight else palletWeight
                )
                bundle.putString(
                    Constants.PALLET_COUNT,
                    if (palletCount.isEmpty() || palletCount.equals("0")) bagList[0].noOfPallet else palletCount
                )
                bundle.putBoolean(Constants.PALLET_EDIT, true)
                bundle.putInt(Constants.PALLET_ADDED, bagList.size)
                if (palletCount.isNotEmpty() && !palletCount.equals("0")) binding.btProceed.isEnabled =
                    (bagList.size == palletCount.toInt()) else binding.btProceed.isEnabled = bagList.size > 0
                if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
                    getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
                )
                else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                updateTotalWeights()
            }
            setUpAdapter(bagList)
        }
    }

    fun updateBagWeight(bagMaterial: VegaNicaraguaWeighmentBagMaterial) {
        PreferenceHelper.save(Constants.IS_EDIT_TRANS_VALUE_CHANGED, true)
        var isExistValue = false
        var pos: Int = 0
        bagList.forEachIndexed { index, it ->
            if (it.id == bagMaterial.id) {
                isExistValue = true
                pos = index
            }
        }
        if (!isExistValue) {
            bagMaterial.id = Random.nextInt()
            bagMaterial.materialCode = receivingData.materialCode.toString()
            bagMaterial.supplierCode = receivingData.supplierCode.toString()
            //bagMaterial.purcheseOrderNo = receivingData.purchaseDocNum.toString()
        } else {
            bagList.removeAt(pos)
        }
        bagList.add(bagMaterial)
        bagList.forEachIndexed { index, material ->
            material.message = getString(R.string.stored_locally)
            material.tmpWbId = receivingData.tmpWbId
            material.storageLocationCode = receivingData.storageLocationCode
            material.item = index.inc().toString()
            vm.saveBagDetails(material)
        }

        if (palletCount.isNotEmpty() && !palletCount.equals("0")) binding.btProceed.isEnabled =
            (bagList.size == palletCount.toInt())
        else binding.btProceed.isEnabled = bagList.size > 0
        if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
            getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
        )
        else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        updateTotalWeights()
    }

    fun updateTotalWeights() {
        var grossWeight = 0.0
        var tareWeight = 0.0
        val netWeight: Double
        var bagCount = 0
        for (item in bagList) {
            grossWeight = grossWeight.plus(item.grossWeight.toDouble())
            tareWeight = tareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
                .plus(palletAvg.toDouble())
            bagCount = bagCount.plus(item.bagCount.toInt())
        }
        netWeight = grossWeight.minus(tareWeight)
        binding.clNet.tvGrossWeightValue.text = grossWeight.formatThreeDigits().plus(receivingData.unitsOfMeasure)
        binding.clNet.tvTareWeightValue.text = tareWeight.formatThreeDigits().plus(receivingData.unitsOfMeasure)
        binding.clNet.tvNetWeightValue.text = netWeight.formatThreeDigits().plus(receivingData.unitsOfMeasure)
        receivingData.grossWeight = grossWeight.formatThreeDigits()
        receivingData.tareWeight = tareWeight.formatThreeDigits()
        receivingData.netWeight = netWeight.formatThreeDigits()
        receivingData.bagCount = bagCount.toString()
    }

    private fun setUpAdapter(bagList: ArrayList<VegaNicaraguaWeighmentBagMaterial>) {
        if (bagList.size > 0) {
            binding.tvNoWeight.gone()
            binding.rvWeight.visible()
            binding.rvWeight.setUpAdapter(
                bagList,
                R.layout.item_vega_nicaragua_grn_supplier_bag,
                ItemVegaNicaraguaGrnSupplierBagBinding::inflate,
                { it, pos, bindItem ->
                    if (pos % 2 == 0) {
                        bindItem.clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.rectangle_grey_border)
                    } else {
                        bindItem.clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_light_grey1)
                    }
                    bindItem.tvSno.text = pos.plus(1).toString()
                    bindItem.tvBag.text = it.bagCount
                    bindItem.tvGrossWeight.text =
                        it.grossWeight.toDouble().formatThreeDigits().plus(" ")
                            .plus(receivingData.unitsOfMeasure)
                    val avgAvlue =
                        it.palletAverage?.toDouble()
                            ?.plus(it.bagCount.toInt().times(it.tareWeight?.toDouble()!!))
                    bindItem.tvTarWeight.text =
                        avgAvlue?.formatThreeDigits().plus(" ").plus(receivingData.unitsOfMeasure)
                    bindItem.tvNetWeight.text =
                        it.grossWeight.toDouble().minus(avgAvlue!!).formatThreeDigits().plus(" ")
                            .plus(receivingData.unitsOfMeasure)
                    bindItem.ivEdit.setOnClickListener { view ->
                        val popupMenu: PopupMenu = PopupMenu(requireActivity(), view)
                        popupMenu.menuInflater.inflate(
                            com.olam.warehouse.login.R.menu.transaction_menu,
                            popupMenu.menu
                        )
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_Sync).isVisible =
                            false
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_copy).isVisible =
                            false
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit).isVisible =
                            false
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit1).isVisible =
                            true
                        popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_delete).isVisible =
                            true
                        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                            when (item.itemId) {
                                com.olam.warehouse.login.R.id.action_edit1 -> {
                                    it.netWeight = it.grossWeight.toDouble().minus(avgAvlue)
                                        .formatThreeDigits()
                                    moveBagAddWeight(it)
                                }
                                com.olam.warehouse.login.R.id.action_delete -> {
                                    showDeleteConfirmationDialog(it, pos)
                                }
                            }
                            true
                        })
                        popupMenu.show()
                    }
                })
        } else {
            binding.tvNoWeight.visible()
            binding.rvWeight.gone()
        }
    }

    fun showDeleteConfirmationDialog(item: VegaNicaraguaWeighmentBagMaterial, pos: Int) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    PreferenceHelper.save(Constants.IS_EDIT_TRANS_VALUE_CHANGED, true)
                    bagList.remove(item)
                    binding.rvWeight.adapter?.notifyItemRemoved(pos)
                    vm.deleteBagDetails(item.id, item.tmpWbId)
                    if (bagList.size == 0) {
                        binding.btProceed.isEnabled = false
                        binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                    }
                },
                { dismiss() })
        }
    }

    private fun moveToQuality() {
        if (receivingData.grnType.equals(getString(R.string.fixed))) {

            if (covertToDouble(receivingData.netWeight) > covertToDouble(receivingData.purchaseDocQty)) {
                showSnack(getString(R.string.po_quantity_error_msg))
            } else {
                callBack?.replaceFragment(GRN_QUALITY, receivingData)
            }
        } else {
            callBack?.replaceFragment(GRN_QUALITY, receivingData)
        }
    }

}
