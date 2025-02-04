package com.olam.warehouse.vegax.salescoffee.ui

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
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.salescoffee.R
import com.olam.warehouse.vegax.salescoffee.data.domain.model.VegaCoffeeSalesPallet
import com.olam.warehouse.vegax.salescoffee.databinding.FragmentSalesCoffeeAddPalletBinding
import com.olam.warehouse.vegax.salescoffee.utils.ADD_WEIGHT
import com.olam.warehouse.vegax.salescoffee.utils.LOT_DETAIL
import com.olam.warehouse.vegax.salescoffee.utils.getColor
import com.olam.warehouse.vegax.salescoffee.utils.prepareCocoaBagMaterial
import kotlinx.android.synthetic.main.item_coffee_sales_pallet_bag_info.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 8/21/2020.
 */
class VegaCoffeeSalesPalletFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_sales_coffee_add_pallet
    private lateinit var binding: FragmentSalesCoffeeAddPalletBinding
    private var callBack: CallBack? = null
    private val vm: VegaCoffeeSalesViewModel by viewModel()
    private var defaultLot = VegaCoffeeSalesLots()
    private var bagList = arrayListOf<VegaCoffeeSalesBagMaterial>()
    private var palletWeight: String = "0"
    private var palletCount: String = "0"
    private var palletAvg: String = "0"
    private var startTime: String = ""
    private var endTime: String = ""
    private var isPalletDetails = false

    companion object {
        fun newInstance(item: VegaCoffeeSalesLots) = VegaCoffeeSalesPalletFragment().putArgs {
            putParcelable(LOT_DETAIL, item)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    interface CallBack {
        fun replaceFragment(
            receivingType: String,
            data: Any
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSalesCoffeeAddPalletBinding.inflate(inflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        defaultLot = arguments?.getParcelable<VegaCoffeeSalesLots>(LOT_DETAIL) as VegaCoffeeSalesLots
        binding.tvTitle.text =
            getString(com.olam.warehouse.login.R.string.local_sales).plus(" - ").plus(getString(R.string.weighscale))
        /*vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.getConfigItems(UserRoles.SALES.role)*/
         vm.bagItems.observe(viewLifecycleOwner, Observer { updateBagItems(it) })
        vm.getBagItems(defaultLot.batchNumber, defaultLot.materialCode)
        binding.btAddWeight.setOnClickListener { moveBagAddWeight(VegaCoffeeSalesBagMaterial()) }
         binding.btProceed.setOnClickListener { moveToSummary() }
        binding.btSave.setOnClickListener { moveToSummary() }
         binding.tvLotValue.text = defaultLot.batchNumber
         binding.tvMaterialValue.text = defaultLot.materialName
         updateTotalWeights()
        // vm.getPalletInfo(defaultLot.batchNumber, defaultLot.materialCode ?: "")
        vm.pallet.observe(viewLifecycleOwner, Observer { updatePalletUI(it) })
    }

    private fun updatePalletBlock(bundle: Bundle?) {
        displayFragment(VegaCocoaAddPalletFragment.newInstance(bundle), false)
    }

    private fun updatePalletUI(response: Resource<GenericReqAndResp<List<VegaCoffeeSalesPallet>>>?) {
         when (response?.status) {
             Resource.Status.SUCCESS -> {
                 hideLoading()
                 isPalletDetails =
                     !response.data?.data?.get(0)?.totalPalletWeight.isNullOrEmpty() && response.data?.data?.get(0)?.totalPalletWeight != "0.0"
                 palletCount = response.data?.data?.get(0)?.noOfPallet ?: ""
                 palletWeight =
                     if (response.data?.data?.get(0)?.totalPalletWeight == "0.0") "0" else response.data?.data?.get(0)?.totalPalletWeight
                         ?: "0"
                 palletAvg =
                     if (!palletCount.equals("0")) palletWeight.toDouble().div(palletCount.toInt()).formatThreeDigits()
                         .replace(
                             ",",
                             ""
                         ) else "0"
                 Bundle().apply {
                     val isEdit =
                         if (response.data?.data?.get(0)?.totalPalletWeight == "0.0") "0" else response.data?.data?.get(
                             0
                         )?.totalPalletWeight
                     putString(
                         Constants.PALLET_WEIGHT,
                         if (response.data?.data?.get(0)?.totalPalletWeight == "0.0") "0" else response.data?.data?.get(
                             0
                         )?.totalPalletWeight
                     )
                     putString(Constants.PALLET_COUNT, response.data?.data?.get(0)?.noOfPallet)
                     putBoolean(Constants.PALLET_COUNT_EDIT, !isEdit.equals("0"))
                     putBoolean(
                         Constants.PALLET_EDIT,
                         response.data?.data?.get(0)?.totalPalletWeight.isNullOrEmpty() || response.data?.data?.get(0)?.totalPalletWeight == "0.0"
                     )
                     updatePalletBlock(this)
                 }
             }
             Resource.Status.LOADING -> showLoading()
             Resource.Status.ERROR -> {
                 hideLoading()
                 UIUtils.showErrorDialog(requireContext(), response.error.toString())
             }
         }
    }

    private fun moveBagAddWeight(bagMaterial: VegaCoffeeSalesBagMaterial) {
        val bundle = Bundle()
        bundle.putString(Constants.MATERIAL_NUMBER, defaultLot.materialName)
        bundle.putString(Constants.SELECTED_STOCKS_ID, defaultLot.batchNumber)
        bundle.putString(Constants.PALLET_AVG, palletAvg)
        bundle.putParcelable(Constants.BAG_MATERIAL, prepareCocoaBagMaterial(bagMaterial))
        bundle.putString(Constants.TITLE, getString(com.olam.warehouse.presentation.R.string.dispatch_weight_entry))
        bundle.putString(Constants.LOT_LABEL, getString(com.olam.warehouse.presentation.R.string.lot_no))
        callBack?.replaceFragment(ADD_WEIGHT, bundle)
    }

    private fun moveToSummary() {
        bagList.forEach { material ->
            material.noOfPallet = palletCount
            material.palletWeight = palletWeight
            material.palletAverage = palletAvg
            material.message = getString(R.string.stored_locally)
            material.salesTempId = defaultLot.salesTempId
            material.isPalletDetails = isPalletDetails
            val net = material.grossWeight.toDouble().minus(
                    material.bagCount.toInt().times(material.tareWeight?.toDouble()!!).plus(
                        material.palletAverage!!.toDouble()
                    )
                )
                .formatThreeDigits().replace(",", "")
            material.netWeight = net
            vm.saveBagDetails(material)
        }
        activity?.onBackPressed()
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

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
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
    }

    private fun updateBagItems(bagItems: List<VegaCoffeeSalesBagMaterial>?) {
        bagItems?.let {
            bagList.clear()
            bagList.addAll(bagItems)
            if (bagItems.size > 0) {
                enableSave(true)
                isPalletDetails = bagList[0].isPalletDetails!!
                val bundle = Bundle()
                bundle.putString(
                    Constants.PALLET_WEIGHT,
                    if (palletWeight.isEmpty() || palletWeight.equals("0")) bagList[0].palletWeight else palletWeight
                )
                bundle.putString(
                    Constants.PALLET_COUNT,
                    if (palletCount.isEmpty() || palletCount.equals("0")) bagList[0].noOfPallet else palletCount
                )
                bundle.putBoolean(Constants.PALLET_EDIT, !bagList[0].isPalletDetails!!)
                bundle.putInt(Constants.PALLET_ADDED, bagList.size)
                bundle.putBoolean(Constants.PALLET_COUNT_EDIT, isPalletDetails)
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
                bundle.putBoolean(Constants.PALLET_EDIT, !isPalletDetails)
                bundle.putBoolean(Constants.PALLET_COUNT_EDIT, isPalletDetails)
                updatePalletBlock(bundle)
                vm.getPalletInfo(defaultLot.batchNumber, defaultLot.materialCode)
            }
            setUpAdapter(bagList)
        }
    }

    fun updateBagWeight(bagMaterial: VegaCoffeeSalesBagMaterial) {
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
        bagList.add(bagMaterial)
        /* if (bagList.size == 1 && !palletCount.equals("0")) bagList[0].startTime =
             DateUtils.getCurrentTimeInMills().toString()
         if (bagList.size == palletCount.toInt()) bagList[palletCount.toInt() - 1].endTime =
             DateUtils.getCurrentTimeInMills().toString()*/
        bagList.forEach { material ->
            material.message = getString(R.string.stored_locally)
            material.salesTempId = defaultLot.salesTempId
            material.materialCode = defaultLot.materialCode
            bagMaterial.isPalletDetails = isPalletDetails
            vm.saveBagDetails(material)
        }
        enableSave(true)
        enableProceed(true)

        if (palletCount.isNotEmpty() && !palletCount.equals("0")) enableProceed(bagList.size == palletCount.toInt())
        else enableProceed(bagList.size > 0)
        if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
            getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green)
        )
        else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        updateTotalWeights()
    }

    private fun setUpAdapter(bagList: ArrayList<VegaCoffeeSalesBagMaterial>) {
        val bagList1 = arrayListOf<VegaCoffeeSalesBagMaterial>()
        if (bagList.size > 0) {
            binding.tvNoWeight.gone()
            binding.rvWeight.visible()
            val dat = bagList.sortedByDescending { it.createdPosition }
            bagList1.addAll(dat)
        } else {
            binding.tvNoWeight.visible()
            binding.rvWeight.gone()
        }
        binding.rvWeight.setUp(bagList1.asReversed(), R.layout.item_coffee_sales_pallet_bag_info, { it, pos ->
            if (pos % 2 == 0) {
                clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.rectangle_grey_border)
            } else {
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
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        com.olam.warehouse.login.R.id.action_edit1 -> {
                            it.netWeight = it.grossWeight.toDouble().minus(avgAvlue).formatThreeDigits()
                            moveBagAddWeight(it)
                        }
                        com.olam.warehouse.login.R.id.action_delete -> {
                            bagList.remove(it)
                            binding.rvWeight.adapter?.notifyItemRemoved(pos)
                            vm.deleteBagDetails(it)
                            if (bagList.size == 0) {
                                enableProceed(false)
                                binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                            }
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
            tareWeight = tareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
                .plus(palletAvg.toDouble())
        }
        netWeight = grossWeight.minus(tareWeight)
        binding.tvStockValue.text = grossWeight.formatThreeDigits().plus(" KG")
        binding.tvStorageValue.text = tareWeight.formatThreeDigits().plus(" KG")
        binding.tvNetWtValue.text = netWeight.formatThreeDigits().plus(" KG")
    }

    private fun enableSave(enable: Boolean) {
        binding.btSave.isEnabled = enable
        when (enable) {
            true -> ViewCompat.setBackgroundTintList(
                binding.btSave,
                ContextCompat.getColorStateList(
                    activity!!,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi1
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btSave,
                ContextCompat.getColorStateList(activity!!, android.R.color.darker_gray)
            )
        }
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
}
