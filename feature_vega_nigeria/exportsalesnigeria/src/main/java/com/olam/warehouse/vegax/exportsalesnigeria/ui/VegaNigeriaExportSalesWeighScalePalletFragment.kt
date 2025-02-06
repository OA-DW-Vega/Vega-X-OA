package com.olam.warehouse.vegax.exportsalesnigeria.ui

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
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.exportsalesnigeria.R
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.model.VegaNigeriaExportSalesWSBagModel
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.model.VegaNigeriaExportSalesWeighScalePallet
import com.olam.warehouse.vegax.exportsalesnigeria.databinding.FragmentVegaNigeriaExportSalesWeighScalePalletBinding
import com.olam.warehouse.vegax.exportsalesnigeria.databinding.ItemVegaNigeriaExportSalesAddBagBinding
import com.olam.warehouse.vegax.exportsalesnigeria.utils.FRAG_ADD_BAG_WEIGHT
import com.olam.warehouse.vegax.exportsalesnigeria.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.exportsalesnigeria.utils.UPDATE_WEIGHT
import com.olam.warehouse.vegax.exportsalesnigeria.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.random.Random

class VegaNigeriaExportSalesWeighScalePalletFragment : BaseFragment() {

    override val layoutResourceId: Int
        get() = R.layout.fragment_vega_nigeria_export_sales_weigh_scale_pallet

    private lateinit var binding: FragmentVegaNigeriaExportSalesWeighScalePalletBinding
    private var callBack: VegaNigeriaExportSalesReplaceFragmentCallback? = null
    private val vm: VegaNigeriaExportSalesViewModel by viewModel()
    private var defaultLot = VegaCoffeeExportSalesLots()
    private var bagList = arrayListOf<VegaCocoaSweepingBagMaterial>()
    private var palletWeight: String = "0"
    private var palletCount: String = "0"
    private var palletAvg: String = "0"
    private var isFirstCall = true

    companion object {
        fun newInstance(item: VegaCoffeeExportSalesLots) =
            VegaNigeriaExportSalesWeighScalePalletFragment().putArgs {
                putParcelable(MODEL_BUNDLE, item)
            }
    }
    interface VegaNigeriaExportSalesReplaceFragmentCallback {
        fun replaceFragment(
            receivingType: String,
            data: Any
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVegaNigeriaExportSalesWeighScalePalletBinding.inflate(inflater)
        initUI()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("exportsalesNigeria/ui/VegaNigeriaExportSalesWeighScalePalletFragment").title("Vega_Nigeria/Export Sales")
            .with(tracker)

    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaNigeriaExportSalesReplaceFragmentCallback
    }

    private fun initUI() {
        defaultLot = arguments?.getParcelable<VegaCoffeeExportSalesLots>(MODEL_BUNDLE) as VegaCoffeeExportSalesLots
        binding.tvTitle.text = getString(R.string.export_sales)
//        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
//        vm.getConfigItems(UserRoles.SALES.role)
        vm.bagItems.observe(viewLifecycleOwner, Observer { updateBagItems(it) })
        vm.getBagItems(defaultLot.batchNumber, defaultLot.materialCode)
        binding.btAddWeight.setOnClickListener { moveBagAddWeight(VegaCocoaSweepingBagMaterial()) }
        binding.btProceed.setOnClickListener { moveToSummary() }
        binding.tvLotValue.text = defaultLot.batchNumber
        binding.tvMaterialValue.text = defaultLot.materialName
        updateTotalWeights()
        vm.pallet.observe(viewLifecycleOwner, Observer { updatePalletUI(it) })
    }

    private fun updatePalletBlock(bundle: Bundle?) {
        displayFragment(VegaCocoaAddPalletFragment.newInstance(bundle), false)
    }

    private fun updatePalletUI(response: Resource<GenericReqAndResp<List<VegaNigeriaExportSalesWeighScalePallet>>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                if (response.data?.data?.size!! > 0) {
                isFirstCall = false
                palletCount = response.data?.data?.get(0)?.noOfPallet ?: ""
                palletWeight = response.data?.data?.get(0)?.totalPalletWeight ?: ""
                Bundle().apply {
                    val isEdit =
                        if (response.data?.data?.get(0)?.totalPalletWeight == "0.0") "0" else response.data?.data?.get(0)?.totalPalletWeight
                    putString(Constants.PALLET_WEIGHT, response.data?.data?.get(0)?.totalPalletWeight)
                    putString(Constants.PALLET_COUNT, response.data?.data?.get(0)?.noOfPallet)
                    putBoolean(
                        Constants.PALLET_EDIT,
                        response.data?.data?.get(0)?.totalPalletWeight.isNullOrEmpty() || response.data?.data?.get(0)?.totalPalletWeight == "0.0"
                    )
                    putBoolean(Constants.PALLET_COUNT_EDIT, !isEdit.equals("0"))
                    updatePalletBlock(this)
                }
            }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                isFirstCall = false
                //  showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
            else -> {}
        }
    }

    private fun moveBagAddWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val bundle = Bundle()
        bundle.putString(Constants.MATERIAL_NUMBER, defaultLot.materialName)
        bundle.putString(Constants.SELECTED_STOCKS_ID, defaultLot.batchNumber)
        bundle.putString(Constants.PALLET_AVG, palletAvg)
        bundle.putParcelable(Constants.BAG_MATERIAL, bagMaterial)
        bundle.putString(Constants.TITLE, getString(com.olam.warehouse.presentation.R.string.dispatch_weight_entry))
        bundle.putString(Constants.LOT_LABEL, getString(com.olam.warehouse.presentation.R.string.lot_no))
        bundle.putString(Constants.UOM, "MT")
        callBack?.replaceFragment(FRAG_ADD_BAG_WEIGHT, bundle)
    }

/*
    fun onBackPressed() {
        var model = true
        if (palletCount.toInt() > 0) {
            model = bagList.size == palletCount.toInt()
        }
        val weight =
            if (binding.tvNetWtValue.text.toString().length < 3) "0.0 KG" else binding.tvNetWtValue.text.toString()
        callBack?.replaceFragment(UPDATE_WEIGHT, VegaNigeriaWSBagModel(weight, model))
    }
*/

    private fun moveToSummary() {
        if (bagList.size > 0) {
            bagList.forEach {
                it.palletAverage = palletAvg
                it.palletWeight = palletWeight
                it.noOfPallet = palletCount
                vm.saveBagDetails(it)
            }
        }
        activity?.onBackPressed()
        callBack?.replaceFragment(UPDATE_WEIGHT, VegaNigeriaExportSalesWSBagModel(binding.tvNetWtValue.text.toString(), true))

//        callBack?.replaceFragment(UPDATE_WEIGHT, binding.tvNetWtValue.text.toString())
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

/*    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.LOT_VALUE.item -> {
                    binding.tvLotValue.text = it.value.toString()
                    //vm.lotId = it.value.toString()
                    //vm.materialNumber = it.dynamicParam.toString()
                    //vm.plantId = it.plant.toString()
                    //vm.getBagItems(vm.lotId)
                }
            }
        }
    }*/

    private fun updateBagItems(bagItems: List<VegaCocoaSweepingBagMaterial>?) {
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
                    getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
                )
                else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                if (isFirstCall && (palletCount.isEmpty() || palletCount.equals("0"))) {
                    vm.getPalletInfo(defaultLot.batchNumber, defaultLot.materialCode)
                }
                updateTotalWeights()
            } else {
                vm.getPalletInfo(defaultLot.batchNumber, defaultLot.materialCode)
                val bundle = Bundle()
                bundle.putString(Constants.PALLET_WEIGHT, palletWeight)
                bundle.putString(Constants.PALLET_COUNT, palletCount)
                bundle.putBoolean(Constants.PALLET_EDIT, true)
                bundle.putString(Constants.UOM, "MT")
                bundle.putBoolean(Constants.PALLET_COUNT_EDIT, false)
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
            bagMaterial.createdPosition = bagList.size
            /* when (bagMaterial.unitsOfMeasure) {
                 "EA" -> bagMaterial.tareWeight =
                     (bagMaterial.tareWeight!!.toDouble().div(248.58)).div(1000).formatThreeDigits()
                 "KG" -> bagMaterial.tareWeight = bagMaterial.tareWeight!!.toDouble().div(1000).formatThreeDigits()
                 "MT" -> bagMaterial.tareWeight = bagMaterial.tareWeight!!.toDouble().formatThreeDigits()
             }*/
        } else {
            bagList.removeAt(pos)
        }
        bagMaterial.batchNumber = defaultLot.batchNumber
        bagMaterial.baseMaterial = defaultLot.materialCode
        bagList.add(bagMaterial)
        if (bagList.size == 1 && !palletCount.equals("0")) bagList[0].startTime =
            DateUtils.getCurrentTimeInMills().toString()
        if (bagList.size == palletCount.toInt()) bagList[palletCount.toInt() - 1].endTime =
            DateUtils.getCurrentTimeInMills().toString()
        bagList.forEach { material ->
            material.message = getString(R.string.stored_locally)
            vm.saveBagDetails(material)
        }
        enableProceed(true)

        if (palletCount.isNotEmpty() && !palletCount.equals("0")) enableProceed(bagList.size == palletCount.toInt())
        else enableProceed(bagList.size > 0)
        if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
            getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
        )
        else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        updateTotalWeights()
    }

    private fun setUpAdapter(bagList: ArrayList<VegaCocoaSweepingBagMaterial>) {
        val bagList1 = arrayListOf<VegaCocoaSweepingBagMaterial>()
        if (bagList.size > 0) {
            binding.tvNoWeight.gone()
            binding.rvWeight.visible()
            val dat = bagList.sortedByDescending { it.createdPosition }
            bagList1.addAll(dat)
        } else {
            binding.tvNoWeight.visible()
            binding.rvWeight.gone()
        }
        binding.rvWeight.setUpAdapter(
            bagList1.asReversed(),
            R.layout.item_vega_nigeria_export_sales_add_bag,
            ItemVegaNigeriaExportSalesAddBagBinding::inflate,
            { it, pos, bindItem ->
                if (pos % 2 == 0) {
                    bindItem.clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.rectangle_grey_border)
                } else {
                    bindItem.clBagHead.setBackgroundResource(R.drawable.shape_cameroon_export_sales_rect_light_grey1)
                }
                bindItem.tvSno.text = pos.plus(1).toString()
                bindItem.tvBag.text = it.bagCount
                bindItem.tvGrossWeight.text =
                    it.grossWeight.toDouble().formatThreeDigits().plus(" ").plus("MT")
                val avgAvlue = it.palletAverage?.toDouble()
                    ?.plus(it.bagCount.toInt().times(it.tareWeight?.toDouble()!!))
                bindItem.tvTarWeight.text = avgAvlue?.formatThreeDigits().plus(" ").plus("MT")
                bindItem.tvNetWeight.text =
                    it.grossWeight.toDouble().minus(avgAvlue!!).formatThreeDigits().plus(" ")
                        .plus("MT")
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
        if (noOfPallet.isNotEmpty() && palletWeight.isNotEmpty() && (!palletWeight.equals("0") && !palletWeight.equals("0.0"))) {
            this.palletWeight = palletWeight
            palletCount = noOfPallet

            palletAvg = palletWeight.toDouble().div(noOfPallet.toInt()).formatThreeDigits().replace(",", "")

            if (palletCount.isNotEmpty() && !palletCount.equals("0")) enableProceed(
                (bagList.size == palletCount.toInt())
            )
            else enableProceed(bagList.size > 0)
            if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
                getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
            )
            else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))

            if (bagList.size > 0) {
                bagList.forEach {
                    it.palletAverage = palletAvg
                    it.palletWeight = palletWeight
                    it.noOfPallet = palletCount
//                    vm.saveBagDetails(it)
                }
                updateTotalWeights()
                binding.rvWeight.adapter?.notifyDataSetChanged()
                println("Roshna => updatePalletDetails")
//                vm.updatePalletBagDetails(bagList[0].batchNumber,palletCount)
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
            tareWeight = tareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
                .plus(palletAvg.toDouble())
        }
        netWeight = grossWeight.minus(tareWeight)
        binding.tvStockValue.text = grossWeight.formatThreeDigits().plus(" MT")
        binding.tvStorageValue.text = tareWeight.formatThreeDigits().plus(" MT")
        binding.tvNetWtValue.text = netWeight.formatThreeDigits().plus(" MT")
    }

    private fun enableProceed(enable: Boolean) {
        binding.btProceed.isEnabled = enable
        when (enable) {
            true -> ViewCompat.setBackgroundTintList(
                binding.btProceed,
                ContextCompat.getColorStateList(
                    requireActivity(),
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btProceed,
                ContextCompat.getColorStateList(requireActivity(), android.R.color.darker_gray)
            )
        }
    }

    fun clearTotalWeights() {
        binding.tvStockValue.text = "0.0 MT"
        binding.tvStorageValue.text = "0.0 MT"
        binding.tvNetWtValue.text = "0.0 MT"
    }

    private fun showConformationDialog(position: VegaCocoaSweepingBagMaterial) {
        MaterialDialog(requireActivity()).show {
            message((R.string.conform_bag_remove))
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
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
