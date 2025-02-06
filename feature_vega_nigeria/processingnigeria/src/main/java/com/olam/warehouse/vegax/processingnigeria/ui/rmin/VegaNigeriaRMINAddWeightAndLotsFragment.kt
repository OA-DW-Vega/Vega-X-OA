package com.olam.warehouse.vegax.processingnigeria.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingnigeria.R
import com.olam.warehouse.vegax.processingnigeria.databinding.FragmentVegaNigeriaRminAddWeightBinding
import com.olam.warehouse.vegax.processingnigeria.databinding.ItemVegaNigeriaFgrnAddBagBinding
import com.olam.warehouse.vegax.processingnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.random.Random

class VegaNigeriaRMINAddWeightAndLotsFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_nigeria_rmin_add_weight
    private lateinit var binding: FragmentVegaNigeriaRminAddWeightBinding
    private val vm: VegaNigeriaRminViewModel by viewModel()
    private var callBack: CallBack? = null

    private var gradeList = listOf<VegaCoffeeFgrnItemsGrades>()
    private var fgrnItem = VegaCoffeeRminProcessing()
    private var currentMaterial: String = ""
    private var currentMaterialCode: String = ""
    private var currentGrade = VegaCoffeeFgrnItemsGrades()

    private var bagList = arrayListOf<VegaCoffeeFgrnGradesMatrialWeights>()
    private var palletWeight: String = "0"
    private var palletCount: String = "0"
    private var palletAvg: String = "0"
    private var isEdit = false
    private var eligibleWeight: Double = 0.0
    private var DefaultStoLoc: String = ""
    private var currentPosition = 0
    private var batchNo: String? = ""

    interface CallBack {
        fun replaceFgrnFragment(fragment: String, bundle: Bundle)
        fun replaceFragment(fragment: String, id: String, batchNo: String?)
        fun isLastBack()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(
            model: VegaCoffeeRminProcessing,
            isEdit: Boolean, currentPosition: String, batchNumber: String
        ) = VegaNigeriaRMINAddWeightAndLotsFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
            putBoolean(IS_EDIT, isEdit)
            putInt("current", currentPosition.toInt())
            putString("batchNo", batchNumber)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNigeriaRminAddWeightBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        //menu.clear()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingnigeria/ui/fgrn/VegaCoffeeFgrnAddWeightAndLotsFragment")
            .title("Processing Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCoffeeRminProcessing()
        if (fgrnItem.gradeListDetails.isNullOrEmpty()) {
        } else {
            gradeList =
                Gson().fromJson<List<VegaCoffeeFgrnItemsGrades>>(fgrnItem.gradeListDetails ?: "")
        }
        isEdit = arguments?.getBoolean(IS_EDIT) ?: false
        batchNo = arguments?.getString("batchNo")
        currentPosition = arguments?.getInt("current", 0) ?: 0
        binding.btProceed.text = getString(R.string.proceed)
        binding.tvLotValue.text = batchNo
        binding.btAddWeight.setOnClickListener { moveBagAddWeight(VegaCoffeeFgrnGradesMatrialWeights()) }
        binding.btProceed.setOnClickListener { validateGrades() }
        binding.btSave.setOnClickListener { saveItemsLocally() }

        //vm.updateGradeWithBatch(gradeList[currentPosition], batchNo ?: "")

        vm.bagWithMaterial.observe(
            viewLifecycleOwner,
            Observer { updateGradesWithBagItems(it) })
        if (fgrnItem.gradeListDetails.isNullOrEmpty()) {
            vm.getRminOfflineGradeWithBags(
                fgrnItem.rminId.plus(fgrnItem.materialCode),
                batchNo ?: ""
            )
        } else {
            if (gradeList.size > 0) vm.getRminOfflineGradeWithBags(
                gradeList[currentPosition].fgrnIdMaterialCode,
                batchNo ?: ""
            )
        }
        updatePalletBlock(null)
    }

    private fun validateGrades() {
        validateGrades1()
    }

    private fun validateGrades1() {
        moveToShiftSelection()
    }

    private fun moveToShiftSelection() {
        activity?.onBackPressed()
       /* val netValue = binding.clNet.tvNetWeightValue.text.toString().split(" ")
        val netWeightWithUom = convertKgToMT(netValue[0]).plus(" ").plus(netValue[1])
        callBack?.replaceFragment(UPDATE_WEIGHT, netWeightWithUom, batchNo)*/
    }

    fun onBackPressed() {
        val netValue = binding.clNet.tvNetWeightValue.text.toString().split(" ")
        val netWeightWithUom = if (netValue.size>2)
            convertKgToMT(netValue[0]).plus(" ").plus("MT")
        else convertKgToMT(netValue[0]).plus(" ").plus("MT")
        val weight =
            if (netWeightWithUom.length < 3) "0.0 MT" else netWeightWithUom
        callBack?.replaceFragment(UPDATE_WEIGHT, weight, batchNo)

    }

    private fun updateGradesWithBagItems(items: List<VegaCoffeeFgrnGradesMatrialWeights>?) {
        if (items == null) {
            return
        }
        items.let {
            if (fgrnItem.gradeListDetails.isNullOrEmpty()) {
                // currentGrade = fgrnItem.gradeList?.get(currentPosition)!!
                currentMaterial = fgrnItem.materialName
                currentMaterialCode = fgrnItem.materialCode
                binding.tvMaterialValue.text = fgrnItem.materialName
            } else {
                currentGrade = gradeList[currentPosition]
                currentMaterial = gradeList[currentPosition].materialName
                currentMaterialCode = gradeList[currentPosition].materialCode
                binding.tvMaterialValue.text = gradeList[currentPosition].materialName
            }
            binding.tvDateValue.text = fgrnItem.poNumber
            bagList.addAll(items)
            enableSaveBtn(bagList.size > 0)
            if (bagList.size > 0) {
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
                // bundle.putString(Constants.UOM, "MT")
                updatePalletBlock(bundle)
                updatePalletDetails(
                    if (palletCount.isEmpty() || palletCount.equals("0")) bagList[0].noOfPallet.toString() else palletCount,
                    if (palletWeight.isEmpty() || palletWeight.equals("0")) bagList[0].palletWeight.toString() else palletWeight
                )
                if (palletCount.isNotEmpty() && !palletCount.equals("0")) binding.btProceed.isEnabled =
                    (bagList.size == palletCount.toInt())
                else binding.btProceed.isEnabled = bagList.size > 0
                if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
                    getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
                )
                else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
                updateTotalWeights()
            } else {
                val bundle = Bundle()
                bundle.putString(Constants.PALLET_WEIGHT, palletWeight)
                bundle.putString(Constants.PALLET_COUNT, palletCount)
                bundle.putBoolean(Constants.PALLET_EDIT, true)
                // bundle.putString(Constants.UOM, "MT")
                updatePalletBlock(bundle)
            }
            //val adapterList = bagList.filter { it.batchNumber == batchNo }
            setUpAdapter(bagList)
            if (currentGrade.batchNumber.isEmpty()) vm.getConfigItems(UserRoles.PROCESSING.role)
        }
    }

    private fun updatePalletBlock(bundle: Bundle?) {
        displayFragment(VegaCocoaAddPalletFragment.newInstance(bundle), false)
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
                        com.olam.warehouse.presentation.R.color.blue_mic_dark)
                })
            else {
                ViewCompat.setBackgroundTintList(
                    binding.btAddWeight,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.grey) })
            }*/

            if (palletCount.isNotEmpty() && !palletCount.equals("0")) binding.btProceed.isEnabled =
                (bagList.size == palletCount.toInt())
            else binding.btProceed.isEnabled = bagList.size > 0
            if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
                getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
            )
            else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))

            if (bagList.size > 0) {
                bagList.forEach {
                    it.palletAverage = palletAvg
                    it.palletWeight = palletWeight
                    it.noOfPallet = palletCount
                }
                updateTotalWeights()
                binding.rvWeight.adapter?.notifyDataSetChanged()
            }
        } else {
            this.palletWeight = "0"
            palletCount = "0"
            palletAvg = "0"
//            binding.btProceed.isEnabled = true
//            binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
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
        binding.clNet.tvGrossWeightValue.text = grossWeight.formatThreeDigits().plus(" KG")
        binding.clNet.tvTareWeightValue.text = tareWeight.formatThreeDigits().plus(" KG")
        binding.clNet.tvNetWeightValue.text = netWeight.formatThreeDigits().plus(" KG")
    }

    fun clearToatalWeights() {
        binding.clNet.tvGrossWeightValue.text = "0.0 KG"
        binding.clNet.tvTareWeightValue.text = "0.0 KG"
        binding.clNet.tvNetWeightValue.text = "0.0 KG"
    }

    private fun moveBagAddWeight(bagMaterial: VegaCoffeeFgrnGradesMatrialWeights) {
        val bundle = Bundle()
        bundle.putString(Constants.MATERIAL_NUMBER, currentMaterial)
        bundle.putString(Constants.SELECTED_STOCKS_ID, fgrnItem.poNumber)
        bundle.putString(Constants.PALLET_AVG, palletAvg)
        bundle.putParcelable(Constants.BAG_MATERIAL, processingMaterialToProcess(bagMaterial))
        bundle.putString(Constants.MATERIAL_LABEL, getString(R.string.input_material))
        bundle.putString(Constants.TITLE, getString(com.olam.warehouse.presentation.R.string.rmin_weight_entry))
        bundle.putBoolean(Constants.DISABLE_MANUAL_ENTRY, true)
        // bundle.putString(Constants.UOM, "MT")
        callBack?.replaceFgrnFragment(FRAG_ADD_BAG_WEIGHT, bundle)
    }

    fun updateBagWeight(bagMaterial: VegaCoffeeFgrnGradesMatrialWeights) {
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
            bagMaterial.fgrnId = fgrnItem.rminId
            bagMaterial.noOfPallet = palletCount
            bagMaterial.palletWeight = palletWeight
            bagMaterial.palletAverage = palletAvg
            bagMaterial.createdTime = bagList.size
            bagMaterial.batchNumber = batchNo ?: ""
            bagList.add(bagMaterial)
            /* when (bagMaterial.unitsOfMeasure) {
                 "EA" -> bagMaterial.tareWeight =
                     (bagMaterial.tareWeight!!.toDouble().div(248.58)).div(1000).formatThreeDigits()
                 "KG" -> bagMaterial.tareWeight = bagMaterial.tareWeight!!.toDouble().div(1000).formatThreeDigits()
                 "MT" -> bagMaterial.tareWeight = bagMaterial.tareWeight!!.toDouble().formatThreeDigits()
             }*/
        } else {
            bagList[pos].grossWeight = bagMaterial.grossWeight
            bagList[pos].tareWeight = bagMaterial.tareWeight
            bagList[pos].netWeight = bagMaterial.netWeight
            bagList[pos].bagType = bagMaterial.bagType
            bagList[pos].bagCount = bagMaterial.bagCount
            bagList[pos].batchNumber = batchNo ?: ""
            bagList[pos].bagMaterialCode = bagMaterial.bagMaterialCode
        }

        if (bagList.size == 1 && !palletCount.equals("0")) currentGrade.startTime = getCurrentTimeInMills().toString()
        if (currentGrade.startTime?.isEmpty()!! && !palletCount.equals("0")) currentGrade.startTime =
            getCurrentTimeInMills().toString()
        if (bagList.size == palletCount.toInt()) currentGrade.endTime = getCurrentTimeInMills().toString()
        bagList.forEach { material ->
            //material.message = getString(R.string.stored_locally)
            material.fgrnIdMaterialCode = fgrnItem.rminId.plus(currentMaterialCode)
            material.batchNumber = batchNo ?: ""
            vm.saveBagDetails(material, currentGrade)
        }
        setUpAdapter(bagList)
        enableSaveBtn(bagList.size > 0)
        if (palletCount.isNotEmpty() && !palletCount.equals("0")) binding.btProceed.isEnabled =
            (bagList.size == palletCount.toInt())
        else binding.btProceed.isEnabled = bagList.size > 0
        if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
            getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
        )
        else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        updateTotalWeights()
    }

    private fun saveItemsLocally() {
        bagList.forEach { material ->
            material.batchNumber = batchNo ?: ""
            vm.saveBagDetails(material, currentGrade)
        }
        activity?.finish()
    }

    private fun setUpAdapter(bagList: ArrayList<VegaCoffeeFgrnGradesMatrialWeights>) {
        val bagList1 = arrayListOf<VegaCoffeeFgrnGradesMatrialWeights>()
        if (bagList.size > 0) {
            binding.tvNoWeight.gone()
            binding.rvWeight.visible()
            val dat = bagList.sortedByDescending { it.createdTime }
            bagList1.addAll(dat)
        } else {
            binding.tvNoWeight.visible()
            binding.rvWeight.gone()
        }

        binding.rvWeight.setUpAdapter(
            bagList1.asReversed(),
            R.layout.item_vega_nigeria_fgrn_add_bag,
            ItemVegaNigeriaFgrnAddBagBinding::inflate,
            { it, pos, bindItem ->
                if (pos % 2 == 0) {
                    bindItem.clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.rectangle_grey_border)
                } else {
                    bindItem.clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_light_grey1)
                }
                bindItem.tvSno.text = pos.plus(1).toString()
                bindItem.tvBag.text = it.bagCount
                bindItem.tvGrossWeight.text =
                    it.grossWeight.toDouble().formatThreeDigits().plus(" ").plus("KG")
                val avgAvlue = it.palletAverage?.toDouble()
                    ?.plus(it.bagCount.toInt().times(it.tareWeight?.toDouble()!!))
                bindItem.tvTarWeight.text = avgAvlue?.formatThreeDigits().plus(" ").plus("KG")
                bindItem.tvNetWeight.text =
                    it.grossWeight.toDouble().minus(avgAvlue!!).formatThreeDigits().plus(" ")
                        .plus("KG")
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
                                it.netWeight =
                                    it.grossWeight.toDouble().minus(avgAvlue).formatThreeDigits()
                                moveBagAddWeight(it)
                            }
                            com.olam.warehouse.login.R.id.action_delete -> {
                                bagList.remove(it)
                                setUpAdapter(bagList)
                                //binding.rvWeight.adapter?.notifyItemRemoved(pos)
                                vm.deleteBagDetails(it.id)
                                if (bagList.size == 0) {
                                    clearToatalWeights()
                                    binding.btProceed.isEnabled = false
                                    binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
                                }
                            }
                        }
                        true
                    })
                    popupMenu.show()
                }
            })
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

    private fun enableSaveBtn(flag: Boolean) {
        when (flag) {
            true -> {
                binding.btSave.isEnabled = true
                binding.btSave.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.blue_light))
            }
            false -> {
                binding.btSave.isEnabled = false
                binding.btSave.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
            }
        }

    }

    private fun enableProceedBtn(flag: Boolean) {
        when (flag) {
            true -> {
                binding.btProceed.isEnabled = true
                binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            }
            false -> {
                binding.btProceed.isEnabled = false
                binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
            }
        }

    }

}
