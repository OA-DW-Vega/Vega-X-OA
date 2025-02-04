package com.olam.warehouse.vegax.offloadingcameroon.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcameroon.R
import com.olam.warehouse.vegax.offloadingcameroon.databinding.FragmentVegaCameroonOffloadAddWeightLayoutBinding
import com.olam.warehouse.vegax.offloadingcameroon.utils.FRAG_ADD_BAG_WEIGHT
import com.olam.warehouse.vegax.offloadingcameroon.utils.UPDATE_WEIGHT
import com.olam.warehouse.vegax.offloadingcameroon.utils.getColor
import com.olam.warehouse.vegax.offloadingcameroon.utils.prepareCameroonBagMaterial
import kotlinx.android.synthetic.main.item_vega_cameroon_offload_add_bag.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.random.Random

class VegaCameroonMtnrWeighScalePalletFragment : BaseFragment() {

    override val layoutResourceId: Int =
        R.layout.fragment_vega_cameroon_offload_add_weight_layout
    private lateinit var binding: FragmentVegaCameroonOffloadAddWeightLayoutBinding
    private var callBack: VegaCameroonOffloadReplaceFragmentCallback? = null
    private val vm: VegaCameroonOffloadingViewModel by viewModel()
    private var defaultLot = VegaCoffeeReceiveLots()
    private var bagList = arrayListOf<VegaCoffeeOffloadingBagMaterial>()
    private var palletWeight: String = "0"
    private var palletCount: String = "0"
    private var palletAvg: String = "0"


    companion object {
        fun newInstance(item: VegaCoffeeReceiveLots) = VegaCameroonMtnrWeighScalePalletFragment().putArgs {
            putParcelable("MODEL_BUNDLE", item)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCameroonOffloadReplaceFragmentCallback
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCameroonOffloadAddWeightLayoutBinding.inflate(inflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
        }
        defaultLot =
            arguments?.getParcelable<VegaCoffeeReceiveLots>("MODEL_BUNDLE") as VegaCoffeeReceiveLots
        binding.tvTitle.text =
            getString(com.olam.warehouse.login.R.string.mtnr).plus(" - ")
                .plus(getString(R.string.weigh_scale))

        vm.bagItemsMtnr.observe(viewLifecycleOwner, Observer { updateBagItems(it) })
        vm.getBagItems(defaultLot.batch, defaultLot.mtnNumber)
        binding.btAddWeight.setOnClickListener { moveBagAddWeight(VegaCoffeeOffloadingBagMaterial()) }
        binding.btProceed.setOnClickListener { moveToSummary() }
        binding.tvLotValue.text = defaultLot.batch
        binding.tvMaterialValue.text = defaultLot.materialName
        updateTotalWeights()

    }

    private fun updatePalletBlock(bundle: Bundle?) {
        displayFragment(VegaCocoaAddPalletFragment.newInstance(bundle), false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingcameroon/ui/VegaCameroonMtnrWeighScalePalletFragment")
            .title("Vega_Cameroon/Offloading")
            .with(tracker)
    }

    private fun moveBagAddWeight(bagMaterial: VegaCoffeeOffloadingBagMaterial) {
        val bundle = Bundle()
        bundle.putString(Constants.MATERIAL_NUMBER, defaultLot.materialName)
        bundle.putString(Constants.SELECTED_STOCKS_ID, defaultLot.batch)
        bundle.putString(Constants.PALLET_AVG, palletAvg)
        bundle.putParcelable(Constants.BAG_MATERIAL, prepareCameroonBagMaterial(bagMaterial))
        bundle.putString(Constants.TITLE, getString(com.olam.warehouse.presentation.R.string.dispatch_weight_entry))
        bundle.putString(Constants.LOT_LABEL, getString(com.olam.warehouse.presentation.R.string.lot_no))
        callBack?.replaceFragment(FRAG_ADD_BAG_WEIGHT, bundle)
    }


    private fun moveToSummary() {
        activity?.onBackPressed()
        callBack?.replaceFragment(UPDATE_WEIGHT, binding.tvNetWtValue.text.toString())
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            "",
            allowStateLoss = true,
            containerViewId = R.id.flPallet,
            allowBackStack = flag
        )
    }


    private fun updateBagItems(bagItems: List<VegaCoffeeOffloadingBagMaterial>?) {
        bagItems?.let {
            bagList.clear()
            bagList.addAll(bagItems)
            if (bagItems.isNotEmpty()) {
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
                updatePalletBlock(bundle)
                updatePalletDetails(
                    if (palletCount.isEmpty() || palletCount.equals("0")) bagList[0].noOfPallet.toString() else palletCount,
                    if (palletWeight.isEmpty() || palletWeight.equals("0")) bagList[0].palletWeight.toString() else palletWeight
                )
                if (palletCount.isNotEmpty() && !palletCount.equals("0")) enableProceed((bagList.size == palletCount.toInt()))
                else enableProceed(bagList.size > 0)
                if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
                    getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green)
                )
                else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                updateTotalWeights()
            } else {
                val bundle = Bundle()
                bundle.putString(Constants.PALLET_WEIGHT, palletWeight)
                bundle.putString(Constants.PALLET_COUNT, palletCount)
                bundle.putBoolean(Constants.PALLET_EDIT, true)
                updatePalletBlock(bundle)
            }
            setUpAdapter(bagList)
        }
    }

    fun updateBagWeight(bagMaterial: VegaCoffeeOffloadingBagMaterial) {
        var isExistValue = false
        var pos: Int = 0
        val addedBagItems = arrayListOf<String>()
        var palletAddedCount = 0
        bagList.forEachIndexed { index, it ->
            if (it.id == bagMaterial.id) {
                isExistValue = true
                pos = index
            }
            palletAddedCount += if (it.noOfPallet?.isNotEmpty() == true) it.noOfPallet?.toInt()
                ?: 0 else 0
            if (!addedBagItems.contains(it.bagType) && it.bagType.isNotEmpty()) addedBagItems.add(it.bagType)
            if (!addedBagItems.contains(it.bagType1) && it.bagType1!!.isNotEmpty()) addedBagItems.add(
                it.bagType1!!
            )
        }
        palletAddedCount += if (bagMaterial.noOfPallet?.isNotEmpty() == true) bagMaterial.noOfPallet?.toInt()
            ?: 0 else 0
        if (bagMaterial.bagType.isNotEmpty()) addedBagItems.add(bagMaterial.bagType)
        if (bagMaterial.bagType1!!.isNotEmpty()) addedBagItems.add(bagMaterial.bagType1!!)
        val distItem = addedBagItems.distinct()
        val tareWeightCalculation = if (palletAddedCount > 0) distItem.size + 1 else distItem.size
        if (tareWeightCalculation <= 3 || (tareWeightCalculation <= 3 && bagMaterial.bagType.isEmpty() && bagMaterial.bagType1!!.isEmpty() && bagMaterial.noOfPallet?.isEmpty() == true)) {
            if (!isExistValue) {
                bagMaterial.id = Random.nextInt()
                bagMaterial.noOfPallet = palletCount
                bagMaterial.palletWeight = palletWeight
                bagMaterial.palletAverage = palletAvg
                bagMaterial.batchNumber = defaultLot.batch
                bagMaterial.createdPosition = bagList.size

            } else {
                bagList.removeAt(pos)
            }
            bagMaterial.batchNumber = defaultLot.batch
            bagList.add(bagMaterial)
            if (bagList.size == 1 && !palletCount.equals("0")) bagList[0].startTime =
                DateUtils.getCurrentTimeInMills().toString()
            if (bagList.size == palletCount.toInt()) bagList[palletCount.toInt() - 1].endTime =
                DateUtils.getCurrentTimeInMills().toString()
            bagList.forEach { material ->
                val tar = material.palletAverage?.toDouble()
                    ?.plus(material.tareWeight?.toDouble()?.times(material.bagCount.toInt())!!)
                    ?.plus(material.tareWeight1?.toDouble()?.times(material.bagCount1!!.toInt())!!)
                material.netWeight = tar?.let { material.grossWeight.toDouble().minus(it).toString() }.toString()
                material.unitsOfMeasure = if (material.unitsOfMeasure?.isEmpty()!!) "KG" else material.unitsOfMeasure
                material.message = getString(R.string.stored_locally)
                material.mtnNumber = defaultLot.mtnNumber
                vm.saveBagDetails(material)
            }
            enableProceed(true)

            if (palletCount.isNotEmpty() && !palletCount.equals("0")) enableProceed(bagList.size == palletCount.toInt())
            else enableProceed(bagList.size > 0)
            if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
                getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green)
            )
            else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
            updateTotalWeights()
        } else {
            showSnack(getString(R.string.please_add_less_three))
        }
    }

    private fun setUpAdapter(bagList: ArrayList<VegaCoffeeOffloadingBagMaterial>) {
        val bagList1 = arrayListOf<VegaCoffeeOffloadingBagMaterial>()
        if (bagList.size > 0) {
            binding.tvNoWeight.gone()
            binding.rvWeight.visible()
            val dat = bagList.sortedByDescending { it.createdPosition }
            bagList1.addAll(dat)
        } else {
            binding.tvNoWeight.visible()
            binding.rvWeight.gone()
        }
        binding.rvWeight.setUp(
            bagList1.asReversed(),
            R.layout.item_vega_cameroon_offload_add_bag,
            { it, pos ->
                if (pos % 2 == 0) {
                    clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.rectangle_grey_border)
                } else {
                    clBagHead.setBackgroundResource(R.drawable.shape_cameroon_offload_rect_light_grey1)
                }
                tvSno.text = pos.plus(1).toString()
                tvBag.text = it.bagCount.toInt().plus(it.bagCount1!!.toInt()).toString()
                tvGrossWeight.text =
                    it.grossWeight.toDouble().formatThreeDigits().plus(" ").plus("KG")
                val avgAvlue = it.palletAverage?.toDouble()
                    ?.plus(it.bagCount.toInt().times(it.tareWeight?.toDouble()!!))
                    ?.plus(it.bagCount1?.toInt()!!.times(it.tareWeight1?.toDouble()!!))
                tvTarWeight.text = avgAvlue?.formatThreeDigits().plus(" ").plus("KG")
                tvNetWeight.text =
                    it.grossWeight.toDouble().minus(avgAvlue!!).formatThreeDigits().plus(" ")
                        .plus("KG")
                ivEdit.setOnClickListener { view ->
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
                    popupMenu.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            com.olam.warehouse.login.R.id.action_edit1 -> {
                                it.netWeight =
                                    it.grossWeight.toDouble().minus(avgAvlue).formatThreeDigits()
                                moveBagAddWeight(it)
                            }
                            com.olam.warehouse.login.R.id.action_delete -> {
                                showConformationDialog(it)
                            }
                        }
                        true
                    }
                    popupMenu.show()
                }
            })
    }


    fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        if (noOfPallet.isNotEmpty() && palletWeight.isNotEmpty() && !palletWeight.equals("0")) {
            this.palletWeight = palletWeight
            palletCount = noOfPallet
            palletAvg = palletWeight.toDouble().div(noOfPallet.toInt()).formatThreeDigits().replace(",", "")

            if (palletCount.isNotEmpty() && !palletCount.equals("0")) enableProceed(
                (bagList.size == palletCount.toInt())
            )
            else enableProceed(bagList.size > 0)
            if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
                getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green)
            )
            else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))

            if (bagList.size > 0) {
                bagList.forEach {
                    it.palletAverage = palletAvg
                    it.palletWeight = palletWeight
                    it.noOfPallet = palletCount
                }
                updateTotalWeights()
                binding.rvWeight.adapter?.notifyDataSetChanged()
            }
        }
    }

    fun replaceFragment(
        fragment: Fragment,
        tag: String?,
        allowStateLoss: Boolean = false,
        @IdRes containerViewId: Int,
        @AnimRes enterAnimation: Int = 0,
        @AnimRes exitAnimation: Int = 0,
        @AnimRes popEnterAnimation: Int = 0,
        @AnimRes popExitAnimation: Int = 0,
        allowBackStack: Boolean = false
    ) {
        val ft = activity?.supportFragmentManager
            ?.beginTransaction()
            ?.setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
        when {
            allowBackStack -> {
                ft?.add(containerViewId, fragment, tag)
                ft?.addToBackStack(tag)
            }
            else -> ft?.replace(containerViewId, fragment, tag)
        }
        if (!activity?.supportFragmentManager?.isStateSaved!!) {
            ft?.commit()
        } else if (allowStateLoss) {
            ft?.commitAllowingStateLoss()
        }
    }

    private fun updateTotalWeights() {
        var grossWeight = 0.0
        var tareWeight = 0.0
        val netWeight: Double
        for (item in bagList) {
            grossWeight = grossWeight.plus(item.grossWeight.toDouble())
            tareWeight =
                tareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
                    .plus(item.tareWeight1?.toDouble()?.times(item.bagCount1!!.toDouble())!!)
                    .plus(palletAvg.toDouble())
        }
        netWeight = grossWeight.minus(tareWeight)
        binding.tvStockValue.text = grossWeight.formatThreeDigits().plus(" KG")
        binding.tvStorageValue.text = tareWeight.formatThreeDigits().plus(" KG")
        binding.tvNetWtValue.text = netWeight.formatThreeDigits().plus(" KG")
    }

    private fun enableProceed(enable: Boolean) {
        binding.btProceed.isEnabled = enable
        when (enable) {
            true -> ViewCompat.setBackgroundTintList(
                binding.btProceed,
                ContextCompat.getColorStateList(
                    activity!!,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btProceed,
                ContextCompat.getColorStateList(activity!!, android.R.color.darker_gray)
            )
        }
    }

    fun clearTotalWeights() {
        binding.tvStockValue.text = "0.0 KG"
        binding.tvStorageValue.text = "0.0 KG"
        binding.tvNetWtValue.text = "0.0 KG"
    }

    private fun showConformationDialog(position: VegaCoffeeOffloadingBagMaterial) {
        MaterialDialog(activity!!).show {
            message((R.string.conform_bag_remove))
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    bagList.remove(position)
                    setUpAdapter(bagList)
                    vm.deleteBagDetails(position.id)
                    if (bagList.size == 0) {
                        clearTotalWeights()
                        enableProceed(false)
                        binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                    }
                },
                { dismiss() })
        }
    }
}
