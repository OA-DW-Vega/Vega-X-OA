package com.olam.warehouse.vegax.dispatchnigeria.ui.weighscale

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
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.dispatchnigeria.R
import com.olam.warehouse.vegax.dispatchnigeria.data.domain.model.VegaNigeriaCocoaMtntWeighScalePallet
import com.olam.warehouse.vegax.dispatchnigeria.databinding.FragmentNigeriaCocoaMtntWeighscaleAddPalletBinding
import com.olam.warehouse.vegax.dispatchnigeria.databinding.ItemVegaNigeriaCocoaMtntAddBagBinding
import com.olam.warehouse.vegax.dispatchnigeria.ui.VegaNigeriaCocoaMtntViewModel
import com.olam.warehouse.vegax.dispatchnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.random.Random

class VegaNigeriaCocoaMtntWeighScalePalletFragment : BaseFragment() {

    override val layoutResourceId: Int =
        R.layout.fragment_nigeria_cocoa_mtnt_weighscale_add_pallet
    private lateinit var binding: FragmentNigeriaCocoaMtntWeighscaleAddPalletBinding

    // private var callBack: VegaNigeriaCocoaReplaceFragmentCallback? = null
    private var callBack: CallBack? = null
    private val vm: VegaNigeriaCocoaMtntViewModel by viewModel()
    private var defaultLot = VegaCocoaDispatchLots()
    private var bagList = arrayListOf<VegaCocoaSweepingBagMaterial>()
    private var palletWeight: String = "0"
    private var palletCount: String = "0"
    private var palletAvg: String = "0"
    private var startTime: String = ""
    private var endTime: String = ""
    private var isFirstCall = true
    private var isJuteBagLot = false

    interface CallBack {
        fun replaceFragment(
            receivingType: String,
            grossWeight: String,
            data: Any
        )
    }

    companion object {
        fun newInstance(item: VegaCocoaDispatchLots) =
            VegaNigeriaCocoaMtntWeighScalePalletFragment().putArgs {
                putParcelable(MODEL_BUNDLE, item)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNigeriaCocoaMtntWeighscaleAddPalletBinding.inflate(inflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        defaultLot = arguments?.getParcelable<VegaCocoaDispatchLots>(MODEL_BUNDLE) as VegaCocoaDispatchLots
        isJuteBagLot = defaultLot.unitOfMeasure.equals("EA") && defaultLot.materialName?.contains("Jute")?:false
        binding.tvTitle.text =
            getString(com.olam.warehouse.login.R.string.mtnt).plus(" - ").plus(getString(R.string.weigh_scale))
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.getConfigItems(UserRoles.SALES.role)
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

    private fun updatePalletUI(response: Resource<GenericReqAndResp<List<VegaNigeriaCocoaMtntWeighScalePallet>>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                isFirstCall = false
                if (response.data?.data?.size!! > 0) {
                    palletCount = response.data?.data?.get(0)?.noOfPallet ?: ""
                    palletWeight = convertMtToKg(
                        response.data?.data?.get(0)?.totalPalletWeight ?: "",
                        response.data?.data?.get(0)?.packingMaterial1.toString()
                    ).toString()
                    Bundle().apply {
                        val isEdit =
                            if (response.data?.data?.get(0)?.totalPalletWeight == "0.0") "0" else response.data?.data?.get(
                                0
                            )?.totalPalletWeight
                        putString(
                            Constants.PALLET_WEIGHT,
                            convertMtToKg(
                                response.data?.data?.get(0)?.totalPalletWeight ?: "",
                                response.data?.data?.get(0)?.packingMaterial1.toString()
                            )
                        )
                        putString(Constants.PALLET_COUNT, response.data?.data?.get(0)?.noOfPallet)
                        putBoolean(
                            Constants.PALLET_EDIT,
                            convertMtToKg(
                                response.data?.data?.get(0)?.totalPalletWeight ?: "",
                                response.data?.data?.get(0)?.packingMaterial1.toString()
                            ).isNullOrEmpty() || convertMtToKg(
                                response.data?.data?.get(0)?.totalPalletWeight ?: "",
                                response.data?.data?.get(0)?.packingMaterial1.toString()
                            ) == "0.0"
                        )
                        putBoolean(Constants.PALLET_COUNT_EDIT, !isEdit.equals("0"))
                        putString(Constants.UOM, "MT")
                        updatePalletBlock(this)
                    }
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                isFirstCall = false
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
        callBack?.replaceFragment(FRAG_ADD_BAG_WEIGHT, "", bundle)
    }

    fun onBackPressed() {
        /*val weight =
            if (binding.tvNetWtValue.text.toString().length < 3) "0.0 KG" else binding.tvNetWtValue.text.toString()
        callBack?.replaceFragment(UPDATE_WEIGHT, weight)*/
    }

    private fun moveToSummary() {
        val grossValue = binding.tvStockValue.text.toString().split(" ")
        val netValue = binding.tvNetWtValue.text.toString().split(" ")
        activity?.onBackPressed()
        callBack?.replaceFragment(
            UPDATE_WEIGHT,
            binding.tvStockValue.text.toString(),
            binding.tvNetWtValue.text.toString()
        /* if(isJuteBagLot)binding.tvStockValue.text.toString() else  com.olam.warehouse.master.work.convertKgToMT(grossValue[0]).plus(" ").plus(grossValue[1]),
            if(isJuteBagLot)binding.tvNetWtValue.text.toString() else com.olam.warehouse.master.work.convertKgToMT(netValue[0]).plus(" ").plus(netValue[1])*/
        )
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

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.LOT_VALUE.item -> {
                    binding.tvLotValue.text = it.value.toString()
                }
            }
        }
    }

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
                bundle.putString(Constants.UOM, "MT")
                updatePalletBlock(bundle)

                updatePalletDetails(
                    if (palletCount.isEmpty() || palletCount.equals("0")) bagList[0].noOfPallet.toString() else palletCount,
                    if (palletWeight.isEmpty() || palletWeight.equals("0")) bagList[0].palletWeight.toString() else palletWeight
                )
                if (palletCount.isNotEmpty() && !palletCount.equals("0")) enableProceed((bagList.size == palletCount.toInt()))
                else enableProceed(bagList.size > 0)
                if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
                    getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
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
                bundle.putBoolean(Constants.PALLET_COUNT_EDIT, false)
                bundle.putString(Constants.UOM, "MT")
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
            getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
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
            R.layout.item_vega_nigeria_cocoa_mtnt_add_bag,
            ItemVegaNigeriaCocoaMtntAddBagBinding::inflate,
            { it, pos, bindItem ->
                if (pos % 2 == 0) {
                    bindItem.clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.rectangle_grey_border)
                } else {
                    bindItem.clBagHead.setBackgroundResource(R.drawable.shape_nigeriacoco_rect_light_grey1)
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
                getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
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
