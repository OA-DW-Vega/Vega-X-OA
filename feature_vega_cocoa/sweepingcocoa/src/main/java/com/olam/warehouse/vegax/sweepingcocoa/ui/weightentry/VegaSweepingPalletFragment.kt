package com.olam.warehouse.vegax.sweepingcocoa.ui.weightentry

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.sweepingcocoa.R
import com.olam.warehouse.vegax.sweepingcocoa.data.domain.model.VegaCocoaSweepingLots
import com.olam.warehouse.vegax.sweepingcocoa.data.domain.model.VegaSweepingSummary
import com.olam.warehouse.vegax.sweepingcocoa.databinding.FragmentPhysicalInventoryPalletBinding
import com.olam.warehouse.vegax.sweepingcocoa.ui.CallBack
import com.olam.warehouse.vegax.sweepingcocoa.ui.VegaCocoaSweepingViewModel
import com.olam.warehouse.vegax.sweepingcocoa.utils.getColor
import kotlinx.android.synthetic.main.item_vega_cocoa_sweepings_bag.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.random.Random

class VegaSweepingPalletFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_physical_inventory_pallet
    private lateinit var binding: FragmentPhysicalInventoryPalletBinding
    private var callBack: CallBack? = null
    private val vm: VegaCocoaSweepingViewModel by viewModel()
    private var defaultLot = VegaCocoaSweepingLots()
    private var bagList = arrayListOf<VegaCocoaSweepingBagMaterial>()
    private var palletWeight: String = "0"
    private var palletCount: String = "0"
    private var palletAvg: String = "0"

    companion object {
        fun newInstance() = VegaSweepingPalletFragment().putArgs {
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPhysicalInventoryPalletBinding.inflate(inflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.getConfigItems(UserRoles.SWEEPINGS.role)
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })
        vm.bagItems.observe(viewLifecycleOwner, Observer { updateBagItems(it) })

        binding.btAddWeight.setOnClickListener { moveBagAddWeight(VegaCocoaSweepingBagMaterial()) }
        binding.btProceed.setOnClickListener { moveToSummary() }
        updateWeightText()
        updateTotalWeights()
    }

    private fun updatePalletBlock(bundle: Bundle?) {
        displayFragment(VegaCocoaAddPalletFragment.newInstance(bundle), false)
    }

    private fun moveBagAddWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val bundle = Bundle()
        bundle.putString(Constants.MATERIAL_NUMBER, /*defaultLot.materialName*/"")
        bundle.putString(Constants.SELECTED_STOCKS_ID, defaultLot.batchNumber)
        bundle.putString(Constants.PALLET_AVG, palletAvg)
        bundle.putParcelable(Constants.BAG_MATERIAL, bagMaterial)
        bundle.putString(Constants.TITLE, getString(R.string.sweepings_weight_entry))
        callBack?.replaceFragment("AddWeight", bundle)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("sweepingcocoa/ui/weightentry/VegaSweepingPalletFragment")
            .title("sweeping Cocoa")
            .with(tracker)
    }

    private fun moveToSummary() {

        val bundle = Bundle()
        var noOfBags = 0
        var bagTareWeight = 0.0

        for (item in bagList) {
            noOfBags = noOfBags.plus(item.bagCount.toInt())
            bagTareWeight = bagTareWeight.plus(item.tareWeight?.toDouble() ?: 0.0)
        }
        val summary = VegaSweepingSummary(
            defaultLot,
            palletWeight.replace(",", ""),
            palletCount,
            palletAvg,
            binding.clNet.tvStockTotalValue.text.toString(),
            binding.clNet.tvTotalWeightValue.text.toString(),
            bagTareWeight.formatThreeDigits().replace(",", ""),
            noOfBags.toString()
        )
        bundle.putParcelable(Constants.LOT_INFO, summary)
        bundle.putString(Constants.TITLE, getString(R.string.sweepings_summary))
        callBack?.replaceFragment("Summary", bundle)
        bagList.forEach { material -> vm.saveBagDetails(material) }
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

    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaCocoaSweepingLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data.let { it1 ->
                        it1?.get(0)?.let { item -> defaultLot = item }
                        binding.tvMaterialValue.text = defaultLot.materialName
                        binding.tvStockValue.text = defaultLot.weight.plus(" ").plus(defaultLot.unitOfMeasure)
                        binding.tvStorageValue.text = defaultLot.storageLocationCode
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.LOT_VALUE.item -> {
                    binding.tvLotValue.text = it.value.toString()
                    vm.lotId = it.value.toString()
                    vm.materialNumber = it.dynamicParam.toString()
                    vm.plantId = it.plant.toString()
                    vm.getBagItems(vm.lotId)
                }
            }
        }
        vm.getLotDetails()
    }

    private fun updateBagItems(bagItems: List<VegaCocoaSweepingBagMaterial>?) {
        bagItems?.let {
            bagList.clear()
            bagList.addAll(bagItems)
            if (bagItems.size > 0) {
                val bundle = Bundle()
                bundle.putString(Constants.PALLET_WEIGHT,
                    if (palletWeight.isEmpty() || palletWeight.equals("0")) bagList[0].palletWeight else palletWeight)
                bundle.putString(Constants.PALLET_COUNT,
                    if (palletCount.isEmpty() || palletCount.equals("0")) bagList[0].noOfPallet else palletCount)
                bundle.putBoolean(Constants.PALLET_EDIT, true)
                bundle.putInt(Constants.PALLET_ADDED, bagList.size)
                updatePalletBlock(bundle)
                updatePalletDetails(if (palletCount.isEmpty() || palletCount.equals("0")) bagList[0].noOfPallet.toString() else palletCount,
                    if (palletWeight.isEmpty() || palletWeight.equals("0")) bagList[0].palletWeight.toString() else palletWeight)
                if (palletCount.isNotEmpty() && !palletCount.equals("0")) binding.btProceed.isEnabled =
                    (bagList.size == palletCount.toInt()) else binding.btProceed.isEnabled =
                    bagList.size > 0
                if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
                    getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green)
                )
                else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                updateTotalWeights()
            }
            else {
                val bundle = Bundle()
                bundle.putString(Constants.PALLET_WEIGHT, palletWeight)
                bundle.putString(Constants.PALLET_COUNT, palletCount)
                bundle.putBoolean(Constants.PALLET_EDIT, true)
                updatePalletBlock(bundle)
            }
            setUpAdapter(bagList)
        }
    }

    fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
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
            bagMaterial.noOfPallet = palletCount
            bagMaterial.palletWeight = palletWeight
            bagMaterial.palletAverage = palletAvg
            bagMaterial.batchNumber = defaultLot.batchNumber
            /* when (bagMaterial.unitsOfMeasure) {
                 "EA" -> bagMaterial.tareWeight =
                     (bagMaterial.tareWeight!!.toDouble().div(248.58)).div(1000).formatThreeDigits()
                 "KG" -> bagMaterial.tareWeight = bagMaterial.tareWeight!!.toDouble().div(1000).formatThreeDigits()
                 "MT" -> bagMaterial.tareWeight = bagMaterial.tareWeight!!.toDouble().formatThreeDigits()
             }*/
        }
        else {
            bagList.removeAt(pos)
        }
        bagList.add(bagMaterial)
        bagList.forEach { material ->
            material.message = getString(R.string.stored_locally)
            vm.saveBagDetails(material)
        }

        /* binding.btAddWeight.isEnabled = (bagList.size != palletCount.toInt())
         if (bagList.size != palletCount.toInt()) ViewCompat.setBackgroundTintList(
             binding.btAddWeight,
             context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.dark_marun) })
         else {
             ViewCompat.setBackgroundTintList(
                 binding.btAddWeight,
                 context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.grey) })
         }*/
        if (palletCount.isNotEmpty() && !palletCount.equals("0")) binding.btProceed.isEnabled =
            (bagList.size == palletCount.toInt()) else binding.btProceed.isEnabled =
            bagList.size > 0
        if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
            getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green)
        )
        else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        updateTotalWeights()
    }

    private fun setUpAdapter(bagList: ArrayList<VegaCocoaSweepingBagMaterial>) {
        if (bagList.size > 0) {
            binding.tvNoWeight.gone()
            binding.rvWeight.visible()
        }
        else {
            binding.tvNoWeight.visible()
            binding.rvWeight.gone()
        }
        binding.rvWeight.setUp(bagList, R.layout.item_vega_cocoa_sweepings_bag, { it, pos ->
            if (pos % 2 == 0) {
                clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.rectangle_grey_border)
            }
            else {
                clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_light_grey1)
            }
            tvSno.text = pos.plus(1).toString()
            tvBag.text = it.bagCount
            tvGrossWeight.text = it.grossWeight.toDouble().formatThreeDigits().plus(" ").plus("KG")
            val avgAvlue = it.palletAverage?.toDouble()?.plus(it.bagCount.toInt().times(it.tareWeight?.toDouble()!!))
            tvTarWeight.text = avgAvlue?.formatThreeDigits().plus(" ").plus("KG")
            tvNetWeight.text =
                it.grossWeight.toDouble().minus(avgAvlue!!).formatThreeDigits().plus(" ").plus("KG")
            ivEdit.setOnClickListener { view ->
                val popupMenu: PopupMenu = PopupMenu(requireActivity(), view)
                popupMenu.menuInflater.inflate(com.olam.warehouse.login.R.menu.transaction_menu, popupMenu.menu)
                popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_Sync).isVisible = false
                popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_copy).isVisible = false
                popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit).isVisible = false
                popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_edit1).isVisible = true
                popupMenu.menu.findItem(com.olam.warehouse.login.R.id.action_delete).isVisible = true
                popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                    when (item.itemId) {
                        com.olam.warehouse.login.R.id.action_edit1 -> {
                            it.netWeight = it.grossWeight.toDouble().minus(avgAvlue).formatThreeDigits()
                            moveBagAddWeight(it)
                        }
                        com.olam.warehouse.login.R.id.action_delete -> {
                            bagList.remove(it)
                            binding.rvWeight.adapter?.notifyItemRemoved(pos)
                            vm.deleteBagDetails(it)
                            if(bagList.size ==0){
                                binding.btProceed.isEnabled = false
                                binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                            }
                        }
                    }
                    true
                })
                popupMenu.show()
            }
        })
    }

    fun updateWeightText() {
        binding.clNet.tvStockTotal.text = getString(R.string.gross_weight)
        binding.clNet.tvNetWeightTotal.text = getString(R.string.tare_weight)
        binding.clNet.tvTotalWeight.text = getString(R.string.net_weight)
        binding.clNet.plus.text = "  -  "
    }

    fun updateTotalWeights() {
        var grossWeight = 0.0
        var tareWeight = 0.0
        val netWeight: Double
        for (item in bagList) {
            grossWeight = grossWeight.plus(item.grossWeight.toDouble())
            tareWeight = tareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
                .plus(palletAvg.toDouble())
        }
        netWeight = grossWeight.minus(tareWeight)
        binding.clNet.tvStockTotalValue.text = grossWeight.formatThreeDigits().plus(" KG")
        binding.clNet.tvNetWeightTotalValue.text = tareWeight.formatThreeDigits().plus(" KG")
        binding.clNet.tvTotalWeightValue.text = netWeight.formatThreeDigits().plus(" KG")
    }

    fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        if (noOfPallet.isNotEmpty() && palletWeight.isNotEmpty() && !palletWeight.equals("0")) {
            this.palletWeight = palletWeight
            palletCount = noOfPallet
            palletAvg = palletWeight.toDouble().div(noOfPallet.toInt()).formatThreeDigits().replace(",", "")
            /*binding.btAddWeight.isEnabled = (bagList.size != palletCount.toInt())
            if (bagList.size != palletCount.toInt()) ViewCompat.setBackgroundTintList(
                binding.btAddWeight,
                context?.let {
                    ContextCompat.getColorStateList(it,
                        com.olam.warehouse.presentation.R.color.dark_marun)
                })
            else {
                ViewCompat.setBackgroundTintList(
                    binding.btAddWeight,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.grey) })
            }*/

            if (palletCount.isNotEmpty() && !palletCount.equals("0")) binding.btProceed.isEnabled =
                (bagList.size == palletCount.toInt()) else binding.btProceed.isEnabled =
                bagList.size > 0
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
        else {
            this.palletWeight = "0"
            palletCount = "0"
            palletAvg = "0"
            binding.btProceed.isEnabled = true
            binding.btProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
            if (bagList.size > 0) {
                bagList.forEach {
                    it.palletAverage = "0"
                    it.palletWeight = "0"
                    it.noOfPallet = "0"
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
        }
        else if (allowStateLoss) {
            ft?.commitAllowingStateLoss()
        }
    }
}
