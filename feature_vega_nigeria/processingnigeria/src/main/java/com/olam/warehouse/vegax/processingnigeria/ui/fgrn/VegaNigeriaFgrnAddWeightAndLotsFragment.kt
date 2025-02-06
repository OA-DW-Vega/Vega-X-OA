package com.olam.warehouse.vegax.processingnigeria.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnGradesWithBagItems
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingnigeria.R
import com.olam.warehouse.vegax.processingnigeria.databinding.FragmentVegaNigeriaFgrnAddWeightAndLotsBinding
import com.olam.warehouse.vegax.processingnigeria.databinding.ItemVegaNigeriaFgrnAddBagBinding
import com.olam.warehouse.vegax.processingnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.random.Random

class VegaNigeriaFgrnAddWeightAndLotsFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_nigeria_fgrn_add_weight_and_lots
    private lateinit var binding: FragmentVegaNigeriaFgrnAddWeightAndLotsBinding
    private val vm: VegaNigeriaFgrnViewModel by viewModel()
    private var callBack: CallBack? = null

    private var gradeList = listOf<VegaCoffeeFgrnItemsGrades>()
    private var fgrnItem = VegaCoffeeFgrnItems()
    private var noOfGradeItems: Int = 0
    private var selectedItemPosition: Int = 0
    private var currentMaterial: String = ""
    private var currentMaterialCode: String = ""
    private var currentDefaultLot = VegaConfigDetails()
    private var currentGrade = VegaCoffeeFgrnItemsGrades()

    private var bagList = arrayListOf<VegaCoffeeFgrnGradesMatrialWeights>()
    private var palletWeight: String = "0"
    private var palletCount: String = "0"
    private var palletAvg: String = "0"
    private var isEdit = false
    private var eligibleWeight: Double = 0.0
    private var DefaultStoLoc: String = ""
    private var isThirdPartyMaterial: Boolean = false

    interface CallBack {
        fun replaceFgrnFragment(fragment: String, bundle: Bundle)
        fun replaceFgrnFragment(
            fragment: String,
            model: VegaCoffeeFgrnItems,
            id: String,
            isThirdPartyMaterial: Boolean,
            currentMaterial: String
        )

        fun isLastBack()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(
            model: VegaCoffeeFgrnItems,
            id: String,
            isEdit: Boolean
        ) = VegaNigeriaFgrnAddWeightAndLotsFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
            putString(FRAG_ID, id)
            putBoolean(IS_EDIT, isEdit)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNigeriaFgrnAddWeightAndLotsBinding.inflate(layoutInflater)
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
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCoffeeFgrnItems()
        gradeList = Gson().fromJson<List<VegaCoffeeFgrnItemsGrades>>(arguments?.getString(FRAG_ID) ?: "")
        isEdit = arguments?.getBoolean(IS_EDIT) ?: false
        noOfGradeItems = gradeList.size

        when {
            noOfGradeItems == 1 -> binding.btProceed.text = getString(R.string.proceed)
        }
        binding.tvPoNoValue.text = fgrnItem.processOrderNo
        val times = fgrnItem.startDate?.split('(', ')')
        binding.tvDateValue.text = times?.get(1).let { it1 ->
            it1?.let { it2 ->
                DateUtils.getUTCDateTime(
                    it2,
                    App.getAppContext()
                )
            }
        }
        binding.btAddWeight.setOnClickListener { moveBagAddWeight(VegaCoffeeFgrnGradesMatrialWeights()) }
        binding.btProceed.setOnClickListener { validateGrades() }
        binding.tvAssignLot.setOnClickListener { moveCreateLot() }
        binding.btSave.setOnClickListener { saveItemsLocally() }
        binding.icAssignLotCard.ivDelete.setOnClickListener { showItemDeleteDialog() }

        vm.offlineGradeWithBags.observe(viewLifecycleOwner, Observer { updateGradesWithBagItems(it) })
        if (gradeList.isNotEmpty()) {
            vm.getOfflineGradeWithBags(gradeList[selectedItemPosition].fgrnIdMaterialCode)
            isThirdPartyMaterial = gradeList[selectedItemPosition].isThirdPartyMaterial

        }
        if (isThirdPartyMaterial) {
            binding.tvVendorValue.visible()
            binding.tvVendor.visible()
            binding.tvVendorValue.text = fgrnItem.vendor
        } else {
            fgrnItem.vendor = ""
        }
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        updatePalletBlock(null)
    }

    private fun showItemDeleteDialog() {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    binding.flAssignLot.gone()
                    enableDisableAssign(true)
                    gradeList[selectedItemPosition].batchNumber = ""
                    gradeList[selectedItemPosition].weight = ""
                    gradeList[selectedItemPosition].lotStorageLocationCode = ""
                    vm.saveFgrnGrade(gradeList[selectedItemPosition])
                },
                { dismiss() })
        }
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>) {
        val defaultLot = configItems.filter { it.process.equals(ConfigItems.DEFAULT_LOT.item) }
        defaultLot.forEach {
            if (currentMaterialCode.contains(it.materialCode) && fgrnItem.cfgNo?.contains(it.cfgNo)!!) {
                if (it.applicable?.contains("Y")!!) {
                    currentDefaultLot = it
                    lotVisibility(true)
                    updateDefaultLot(currentDefaultLot)
                } else {
                    lotVisibility(false)
                }
            }
        }

        val eligibleWeight1 = configItems.filter { it.process.equals(ConfigItems.ELIGIBLE_WEIGHT.item) }
        eligibleWeight1.forEach {
            if (currentMaterialCode.contains(it.materialCode)) {
                if (it.applicable?.contains("Y")!!) {
                    eligibleWeight = it.value?.split(" ")?.get(0)?.toDouble() ?: 0.0
                }
            }
        }

        val defaultStorageLoc = configItems.filter { it.process.equals(ConfigItems.DEFAULT_STORAGE_LOC.item) }
        var isExist = false
        defaultStorageLoc.forEach {
            if (currentMaterial.contains(it.materialCode) && !it.materialCode.isNullOrEmpty() && !isExist) {
                if (it.applicable?.contains("Y")!!) {
                    DefaultStoLoc = it.value.toString()
                    isExist = true
                }
            } else if (it.materialCode.isNullOrEmpty() && !isExist) {
                if (it.applicable?.contains("Y")!!) {
                    DefaultStoLoc = it.value.toString()
                    isExist = true
                }
            }
        }
    }

    fun lotVisibility(isDefault: Boolean) {
        when (isDefault) {
            true -> {
                binding.clDefaultLot.visible()
                binding.clAssignLot.gone()
            }
            false -> {
                binding.clDefaultLot.gone()
                binding.clAssignLot.visible()
                //binding.flAssignLot.gone()
            }
        }
    }

    private fun updateDefaultLot(currentDefaultLot: VegaConfigDetails) {
        currentDefaultLot.let {
            val lots = currentDefaultLot.value?.split(",")!!.toList()
            val weights = currentDefaultLot.value1?.let { currentDefaultLot.value1?.split(",")!!.toList() }
            val stageAdapter = ArrayAdapter(requireContext(), R.layout.item_vega_nigeria_processing_rmin_grade, lots)
            stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
            binding.spDefaultLot.adapter = stageAdapter
            binding.spDefaultLot.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    var isCurrentLot = false
                    if (currentGrade.batchNumber.equals(lots[position])) isCurrentLot = true
                    currentGrade.batchNumber = lots[position]
                    val lotWeight = weights?.get(position)?.split(" ")?.get(0) ?: "0.0"
                    currentGrade.eligibeWeight =
                        if (!eligibleWeight.equals(0.0)) eligibleWeight.minus(lotWeight.toDouble())
                            .formatThreeDigits() else "0.0"
                    currentGrade.weight = weights?.get(position) ?: "0.0"
                    currentGrade.isDefaultLot = true
                    currentGrade.lotStorageLocationCode = DefaultStoLoc
                    currentGrade.storageLocationCode = DefaultStoLoc
                    //updateLotCard(lots[position])
                    if (!isCurrentLot) vm.saveFgrnGrade(currentGrade)
                }
            }
            binding.spDefaultLot.setSelection(if (binding.icLotCard.tvLotNoValue.text.isNotEmpty()) lots.indexOf(binding.icLotCard.tvLotNoValue.text) else 0)
        }
    }

    private fun updateLotCard(fgrnGrades: VegaCoffeeFgrnItemsGrades) {
        val uom = if (fgrnGrades.unitOfMeasure?.isNotEmpty()!!) fgrnGrades.unitOfMeasure else "KG"
        val weight = if (fgrnGrades.weight?.isNotEmpty()!!) fgrnGrades.weight else "0.0"
        if (fgrnGrades.isDefaultLot!!) {
            binding.icLotCard.ivDelete.gone()
            binding.icLotCard.cbLotId.gone()
//            binding.icLotCard.tvStorageLbl.gone()
//            binding.icLotCard.tvStorageValue.gone()
            //binding.icLotCard.tvElWeightLbl.visible()
            //binding.icLotCard.tvElWeightValue.visible()
            binding.icLotCard.tvLotNoValue.text = fgrnGrades.batchNumber
            binding.icLotCard.tvStorageValue.text = fgrnGrades.lotStorageLocationCode
            binding.icLotCard.tvElWeightValue.text = fgrnGrades.eligibeWeight.toString().plus(" ").plus("KG")
            binding.icLotCard.tvWeightValue.text = weight
            binding.tvNewLot.gone()
        } else {
            if (fgrnGrades.batchNumber.isNotEmpty() && !weight.equals("0.0")) {
                enableDisableAssign(false)
                binding.flAssignLot.visible()
                binding.icAssignLotCard.cbLotId.gone()
//                binding.icAssignLotCard.tvStorageLbl.gone()
//                binding.icAssignLotCard.tvStorageValue.gone()
                //binding.icAssignLotCard.tvElWeightLbl.visible()
                //binding.icAssignLotCard.tvElWeightValue.visible()
                binding.icAssignLotCard.tvLotNoValue.text = fgrnGrades.batchNumber
                binding.icAssignLotCard.tvStorageValue.text = fgrnGrades.lotStorageLocationCode
                binding.icAssignLotCard.tvElWeightValue.text = fgrnGrades.eligibeWeight.toString().plus(" ").plus("KG")
                binding.icAssignLotCard.tvWeightValue.text = weight.plus(" ").plus("MT")
            } else {
                enableDisableAssign(true)
                binding.flAssignLot.gone()
            }

            if (fgrnGrades.isCreateNewLot!!) {
                binding.tvNewLot.visible()
                binding.tvNewLot.text = getString(R.string.new_lot)
            } else if (weight?.equals("0.0")!! && fgrnGrades.batchNumber.isNotEmpty()) {
                binding.tvNewLot.visible()
                binding.tvNewLot.text = getString(R.string.lot_id).plus(" ").plus(fgrnGrades.batchNumber)
            } else {
                binding.tvNewLot.gone()
            }
        }
    }

    private fun enableDisableAssign(flag: Boolean) {
        when (flag) {
            true -> {
                binding.tvAssignLot.isEnabled = true
                ViewCompat.setBackgroundTintList(
                    binding.tvAssignLot,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.blue_mic_dark
                        )
                    }
                )
            }
            else -> {
                binding.tvAssignLot.isEnabled = false
                ViewCompat.setBackgroundTintList(
                    binding.tvAssignLot,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.light_grey
                        )
                    }
                )
            }
        }

    }

    private fun moveCreateLot() {
        callBack?.replaceFgrnFragment(
            FRAG_CREATE_LOT,
            fgrnItem,
            currentMaterialCode,
            isThirdPartyMaterial,
            currentMaterial
        )
    }

    private fun validateGrades() {
        if (currentGrade.batchNumber.isNotEmpty()) {
            validateLot()
        } else {
            if (currentGrade.isCreateNewLot!!)
                validateLot()
            else
                activity?.toast(getString(R.string.please_assign_lot))
        }
    }

    private fun validateLot() {
        if ((currentGrade.isDefaultLot!! && !currentGrade.eligibeWeight.equals("0.0")) ||
            (!currentGrade.isDefaultLot!! && !currentGrade.weight.equals("0.0") && !currentGrade.eligibeWeight.equals(
                "0.0"
            ))
        ) {
            val net =
                (binding.clNet.tvNetWeightValue.text.toString().split(" ")[0].toDouble()).div(1000).formatTwoDigits()
            if (net.toDouble() <= currentGrade.eligibeWeight?.toDouble()!!.plus(0.2)) {
                validateGrades1()
            } else {
                activity?.toast(getString(R.string.exceed_weight))
            }
        } else {
            validateGrades1()
        }
    }

    private fun validateGrades1() {
        if (selectedItemPosition == noOfGradeItems - 1) {
            moveToSifftSelection()
        } else {
            selectedItemPosition = selectedItemPosition + 1
            when {
                selectedItemPosition == noOfGradeItems - 1 -> {
                    clearAllValues()
                    binding.btProceed.text = getString(R.string.proceed)
                    vm.getOfflineGradeWithBags(gradeList[selectedItemPosition].fgrnIdMaterialCode)
                }
                selectedItemPosition < noOfGradeItems - 1 -> {
                    clearAllValues()
                    binding.btProceed.text = getString(R.string.next)
                    vm.getOfflineGradeWithBags(gradeList[selectedItemPosition].fgrnIdMaterialCode)
                }
                else -> {
                    moveToSifftSelection()
                }
            }
        }
    }

    private fun moveToSifftSelection() {
        if (isEdit)
            activity?.onBackPressed()
        else
            callBack?.replaceFgrnFragment(FRAG_SHIFT, fgrnItem, "", isThirdPartyMaterial, "")

        /*if((currentGrade.isDefaultLot!! && !currentGrade.eligibeWeight.equals("0.0")) ||
            (!currentGrade.isDefaultLot!! && !currentGrade.weight.equals("0.0") && !currentGrade.eligibeWeight.equals("0.0"))){
            if(binding.clNet.tvNetWeightValue.text.toString().split(" ")[0].toDouble() <= currentGrade.eligibeWeight?.toDouble()!!){
                if (isEdit)
                    activity?.onBackPressed()
                else
                    callBack?.replaceFgrnFragment(FRAG_SIFFT, fgrnItem, "")
            }else{
                activity?.toast(getString(R.string.exceed_weight))
            }
        }else{
            if (isEdit)
                activity?.onBackPressed()
            else
                callBack?.replaceFgrnFragment(FRAG_SIFFT, fgrnItem, "")
        }*/

    }

    fun onBackPressed() {
        selectedItemPosition = selectedItemPosition - 1
        when {
            selectedItemPosition < 0 -> {
                callBack?.isLastBack()
                activity?.onBackPressed()
            }
            else -> {
                clearAllValues()
                binding.btProceed.text = getString(R.string.next)
                vm.getOfflineGradeWithBags(gradeList[selectedItemPosition].fgrnIdMaterialCode)
            }
        }
    }

    private fun updateGradesWithBagItems(items: VegaCoffeeFgrnGradesWithBagItems?) {
        if (items == null) {
            return
        }
        items.let {
            currentGrade = it.fgrnGrades
            updateLotCard(it.fgrnGrades)
            it.fgrnGrades.isDefaultLot?.let { it1 -> lotVisibility(it1) }
            currentMaterial = it.fgrnGrades.materialName
            currentMaterialCode = it.fgrnGrades.materialCode
            binding.tvMaterialValue.text = it.fgrnGrades.materialName
            bagList = it.bagItems as ArrayList<VegaCoffeeFgrnGradesMatrialWeights>
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
                //bundle.putString(Constants.UOM, "MT")
                bundle.putInt(Constants.PALLET_ADDED, bagList.size)
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
                //bundle.putString(Constants.UOM, "MT")
                updatePalletBlock(bundle)
            }
            setUpAdapter(bagList)
            if (currentGrade.batchNumber.isEmpty()) vm.getConfigItems(UserRoles.PROCESSING.role)
        }
    }

    private fun clearAllValues() {
        clearToatalWeights()
        lotVisibility(false)
        binding.flAssignLot.gone()
        //updateDefaultLot(VegaConfigDetails())
        updatePalletBlock(null)
        enableProceedBtn(false)
        enableSaveBtn(false)
        bagList.clear()
        palletWeight = "0"
        palletCount = "0"
        palletAvg = "0"
        binding.rvWeight.adapter?.notifyDataSetChanged()
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
            binding.btProceed.isEnabled = true
            binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
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
        bundle.putString(Constants.SELECTED_STOCKS_ID, fgrnItem.processOrderNo)
        bundle.putString(Constants.PALLET_AVG, palletAvg)
        //bundle.putString(Constants.UOM, "MT")
        bundle.putParcelable(Constants.BAG_MATERIAL, processingMaterialToProcess(bagMaterial))
        bundle.putString(Constants.TITLE, getString(com.olam.warehouse.presentation.R.string.fgrn_weight_entry))
        bundle.putBoolean(Constants.DISABLE_MANUAL_ENTRY, true)
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
            bagMaterial.fgrnId = fgrnItem.fgrnId
            bagMaterial.noOfPallet = palletCount
            bagMaterial.palletWeight = palletWeight
            bagMaterial.palletAverage = palletAvg
            bagMaterial.createdTime = bagList.size
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
            bagList[pos].batchNumber = ""
            bagList[pos].bagMaterialCode = bagMaterial.bagMaterialCode
        }

        if (bagList.size == 1 && !palletCount.equals("0")) currentGrade.startTime = getCurrentTimeInMills().toString()
        if (currentGrade.startTime?.isEmpty()!! && !palletCount.equals("0")) currentGrade.startTime =
            getCurrentTimeInMills().toString()
        if (bagList.size == palletCount.toInt()) currentGrade.endTime = getCurrentTimeInMills().toString()
        bagList.forEach { material ->
            //material.message = getString(R.string.stored_locally)
            material.fgrnIdMaterialCode = fgrnItem.fgrnId.plus(currentMaterialCode)
            vm.saveBagDetails(material, currentGrade)
        }
        enableSaveBtn(bagList.size > 0)
        /* binding.btAddWeight.isEnabled = (bagList.size != palletCount.toInt())
         if (bagList.size != palletCount.toInt()) ViewCompat.setBackgroundTintList(
             binding.btAddWeight,
             context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.blue_mic_dark) })
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
        updateTotalWeights()
    }

    private fun saveItemsLocally() {
        bagList.forEach { material ->
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
                                binding.rvWeight.adapter?.notifyItemRemoved(pos)
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

    fun updateLotDetails(bundle: Bundle) {
        currentGrade.isDefaultLot = false
        currentGrade.isCreateNewLot = bundle.getBoolean(CREATE_NEW_LOT)
        currentGrade.lotStorageLocationCode = bundle.getString(STORAGE_LOC) ?: ""
        val stockList = bundle.getParcelableArrayList(STOCK_LIST) ?: ArrayList<VegaCoffeeRminLots>()
        if (stockList.size > 0) {
            currentGrade.batchNumber = stockList[0].batchNumber
            currentGrade.weight = stockList[0].weight
            val lotWeight = stockList[0].weight ?: "0.0"
            currentGrade.eligibeWeight =
                if (!eligibleWeight.equals(0.0)) eligibleWeight.minus(lotWeight.toDouble())
                    .formatThreeDigits() else "0.0"
            currentGrade.unitOfMeasure = stockList[0].unitOfMeasure
            currentGrade.lotStorageLocationCode = stockList[0].storageLocationCode
        } else {
            currentGrade.eligibeWeight = eligibleWeight.toString()
            if (!bundle.getBoolean(CREATE_NEW_LOT)) currentGrade.batchNumber =
                bundle.getString(LOT_ID) ?: ""
        }
        vm.saveFgrnGrade(currentGrade)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }
}
