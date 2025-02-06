package com.olam.warehouse.vegax.processingcameroon.ui.rmin

import android.annotation.SuppressLint
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
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesWithBagMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingcameroon.R
import com.olam.warehouse.vegax.processingcameroon.data.domain.model.VegaCameroonRminWeighScalePallet
import com.olam.warehouse.vegax.processingcameroon.databinding.FragmentVegaCameroonRminAddWeightBinding
import com.olam.warehouse.vegax.processingcameroon.databinding.ItemVegaCameroonFgrnAddBagBinding
import com.olam.warehouse.vegax.processingcameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.random.Random

class VegaCameroonRMINAddWeightAndLotsFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_cameroon_rmin_add_weight
    private lateinit var binding: FragmentVegaCameroonRminAddWeightBinding
    private val vm: VegaCameroonRminViewModel by viewModel()
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
    private var currentPosition = 0
    private var batchNo: String? = ""
    private var isFirstCall = true
    private var currentLot = VegaCoffeeRminLots()

    interface CallBack {
        fun replaceFgrnFragment(fragment: String, bundle: Bundle)
        fun replaceFragment(
            fragment: String,
            id: String,
            batchNumber: String,
            palletWeight: String,
            palletCount: String,
            isSave: Boolean
        )
        fun isLastBack()
        fun saveItemLocally()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(
            model: VegaCoffeeRminProcessing,
            isEdit: Boolean, currentPosition: String, batchNumber: String, lot: VegaCoffeeRminLots
        ) = VegaCameroonRMINAddWeightAndLotsFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
            putBoolean(IS_EDIT, isEdit)
            putInt("current", currentPosition.toInt())
            putString("batchNo", batchNumber)
            putParcelable(BATCH_LIST,lot)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCameroonRminAddWeightBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcameroon/ui/rmin/VegaCameroonRMINAddWeightAndLotsFragment").title("Vega_Cameroon/Processing")
            .with(tracker)
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
        }
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCoffeeRminProcessing()
        currentLot= arguments?.getParcelable(BATCH_LIST)?: VegaCoffeeRminLots()
        gradeList =
            Gson().fromJson<List<VegaCoffeeFgrnItemsGrades>>(fgrnItem.gradeListDetails ?: "")
        isEdit = arguments?.getBoolean(IS_EDIT) ?: false
        batchNo = arguments?.getString("batchNo")
        currentPosition = arguments?.getInt("current", 0) ?: 0
        currentGrade=gradeList[currentPosition]
        binding.btProceed.text = getString(R.string.proceed)
        binding.tvLotValue.text = batchNo
        binding.btAddWeight.setOnClickListener { moveBagAddWeight(VegaCoffeeFgrnGradesMatrialWeights()) }
        binding.btProceed.setOnClickListener { validateGrades() }
        binding.btSave.setOnClickListener { saveItemsLocally() }
        updatePalletBlock(null)



        if (gradeList.size > 0)
            vm.getRminGradeWithBags(
            fgrnItem.rminId,
            batchNo ?: ""
        )

        vm.rminBagWithMaterial.observe(
            viewLifecycleOwner,
            Observer {
                updateRminGradesWithBagItems(it)
            })

       /* vm.bagWithMaterial.observe(
            viewLifecycleOwner,
            Observer {
                updateGradesWithBagItems(it)
            })

        if (gradeList.size > 0)
            vm.getRminOfflineGradeWithBags(
                gradeList[currentPosition].fgrnIdMaterialCode,
                batchNo ?: ""
            )*/



        vm.pallet.observe(viewLifecycleOwner, Observer { updatePalletUI(it) })

    }

    private fun updatePalletUI(response: Resource<GenericReqAndResp<List<VegaCameroonRminWeighScalePallet>>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                isFirstCall = false
                if (response.data?.data?.size!! > 0) {
                    palletCount = response.data?.data?.get(0)?.noOfPallet ?: ""
                    palletWeight = response.data?.data?.get(0)?.totalPalletWeight ?: ""
                    if(palletWeight == "0.0") palletWeight="0"
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
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
            else -> {}
        }
    }


    private fun validateGrades() {
        validateGrades1()
    }

    private fun validateGrades1() {
        moveToShiftSelection()
    }

    private fun moveToShiftSelection() {
        activity?.onBackPressed()
        callBack?.replaceFragment(
            UPDATE_WEIGHT,
            binding.clNet.tvNetWeightValue.text.toString(),
            batchNo!!,
            palletWeight,
            palletCount,
            true
        )
    }

    fun onBackPressed() {
        val weight =
            if (binding.clNet.tvNetWeightValue.text.toString().length < 3) "0.0 KG" else binding.clNet.tvNetWeightValue.text.toString()
        callBack?.replaceFragment(UPDATE_WEIGHT, weight, batchNo!!, palletWeight, palletCount, false)
    }

    private fun updateRminGradesWithBagItems(it: VegaCoffeeFgrnGradesWithBagMatrialWeights?) {
        if(it==null){
            return
        }
        it.let {
            currentLot= it.lots.find { it.batchNumber.equals(batchNo) }?:VegaCoffeeRminLots()
            palletWeight= currentLot.palletWeight?:"0"
           palletCount=currentLot.palletCount?:"0"

            currentGrade = gradeList[currentPosition]

            var items = it.lineItems.filter { it.batchNumber.equals(batchNo) }

            items.let {
                //currentGrade = gradeList[currentPosition]
                currentMaterial = gradeList[currentPosition].materialName
                currentMaterialCode = gradeList[currentPosition].materialCode
                binding.tvMaterialValue.text = gradeList[currentPosition].materialName
                binding.tvDateValue.text = fgrnItem.poNumber
                bagList.clear()
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
                    updatePalletBlock(bundle)
                }
                setUpAdapter(bagList)
                if (currentGrade.batchNumber.isEmpty()) vm.getConfigItems(UserRoles.PROCESSING.role)
            }

            if((palletWeight.isEmpty() || palletWeight=="0.0" || palletWeight=="0") && bagList.isEmpty()){
                vm.getPalletInfo(batchNo?:"",fgrnItem.materialCode)
            }

        }
    }
//    private fun updateGradesWithBagItems(items: List<VegaCoffeeFgrnGradesMatrialWeights>?) {
//        if (items == null) {
//            return
//        }
//        items.let {
//            currentGrade = gradeList[currentPosition]
//            currentMaterial = gradeList[currentPosition].materialName
//            currentMaterialCode = gradeList[currentPosition].materialCode
//            binding.tvMaterialValue.text = gradeList[currentPosition].materialName
//            binding.tvDateValue.text = fgrnItem.poNumber
//            bagList.addAll(items)
//            //enableSaveBtn(bagList.size > 0)
//            if (bagList.size > 0) {
//                enableSaveBtn(true)
//                val bundle = Bundle()
//                bundle.putString(
//                    Constants.PALLET_WEIGHT,
//                    if (palletWeight.isEmpty() || palletWeight.equals("0")) bagList[0].palletWeight else palletWeight
//                )
//                bundle.putString(
//                    Constants.PALLET_COUNT,
//                    if (palletCount.isEmpty() || palletCount.equals("0")) bagList[0].noOfPallet else palletCount
//                )
//                bundle.putBoolean(Constants.PALLET_EDIT, true)
//                bundle.putInt(Constants.PALLET_ADDED, bagList.size)
//                updatePalletBlock(bundle)
//                updatePalletDetails(
//                    if (palletCount.isEmpty() || palletCount.equals("0")) bagList[0].noOfPallet.toString() else palletCount,
//                    if (palletWeight.isEmpty() || palletWeight.equals("0")) bagList[0].palletWeight.toString() else palletWeight
//                )
//
//                if (palletCount.isNotEmpty() && !palletCount.equals("0")) binding.btProceed.isEnabled = (bagList.size == palletCount.toInt())
//                else binding.btProceed.isEnabled = bagList.size > 0
//                if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
//                    getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
//                )
//                else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
//                if(getCurrentKey().contains("CM") && getCurrentKey().contains("COCO")) {
//                    if (isFirstCall && (palletCount.isEmpty() || palletCount.equals("0"))) {
//                        vm.getPalletInfo(batchNo?:"", fgrnItem.materialCode)
//                    }
//                }
//
//                updateTotalWeights()
//            } else {
//
//                val bundle = Bundle()
//                bundle.putString(Constants.PALLET_WEIGHT, palletWeight)
//                bundle.putString(Constants.PALLET_COUNT, palletCount)
//                bundle.putBoolean(Constants.PALLET_EDIT, true)
//                updatePalletBlock(bundle)
//            }
//
//            if(palletWeight.isEmpty() || palletWeight=="0.0" || palletWeight=="0"){
//                vm.getPalletInfo(batchNo?:"",fgrnItem.materialCode)
//            }
//            setUpAdapter(bagList)
//            if (currentGrade.batchNumber.isEmpty()) vm.getConfigItems(UserRoles.PROCESSING.role)
//        }
//    }

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

    @SuppressLint("SuspiciousIndentation")
    fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        if (noOfPallet.isNotEmpty() && palletWeight.isNotEmpty() && !palletWeight.equals("0")) {
            this.palletWeight = palletWeight
            palletCount = noOfPallet
            palletAvg = palletWeight.toDouble().div(noOfPallet.toInt()).formatThreeDigits().replace(",", "")
            enableSaveBtn(true)
            if (palletCount.isNotEmpty() && !palletCount.equals("0")) binding.btProceed.isEnabled =
                (bagList.size == palletCount.toInt())
            else binding.btProceed.isEnabled = bagList.size > 0
            if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
                getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
            )
            else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))

            if (bagList.size > 0) {
                bagList.forEachIndexed { index, it ->
                    bagList[index].palletAverage = palletAvg
                    bagList[index].palletWeight = palletWeight
                    bagList[index].noOfPallet = palletCount
                }
                updateTotalWeights()
                setUpAdapter(bagList)
                // binding.rvWeight.adapter?.notifyDataSetChanged()
            }
        } else {
            this.palletWeight = "0"
            palletCount = "0"
            palletAvg = "0"

            if (bagList.size > 0) {
                enableSaveBtn(true)
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
            tareWeight =
                tareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
                    .plus(item.tareWeight1?.toDouble()?.times(item.bagCount1!!.toDouble())!!)
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

        } else {
            bagList[pos].grossWeight = bagMaterial.grossWeight
            bagList[pos].tareWeight = bagMaterial.tareWeight
            bagList[pos].tareWeight1 = bagMaterial.tareWeight1
            bagList[pos].netWeight = bagMaterial.netWeight
            bagList[pos].bagType = bagMaterial.bagType
            bagList[pos].bagType1 = bagMaterial.bagType1
            bagList[pos].bagCount = bagMaterial.bagCount
            bagList[pos].bagCount1 = bagMaterial.bagCount1
            bagList[pos].batchNumber = batchNo ?: ""
            bagList[pos].bagMaterialCode = bagMaterial.bagMaterialCode
            bagList[pos].bagMaterialCode1 = bagMaterial.bagMaterialCode1
        }

        if (bagList.size == 1 && !palletCount.equals("0") && !palletCount.equals("")) currentGrade.startTime = getCurrentTimeInMills().toString()
        if (currentGrade.startTime?.isEmpty()!! && !palletCount.equals("0"))
            currentGrade.startTime = getCurrentTimeInMills().toString()
        if (palletCount.isNotEmpty()) if (bagList.size == palletCount.toInt()) currentGrade.endTime = getCurrentTimeInMills().toString()

        bagList.forEach { material ->
            material.fgrnIdMaterialCode = fgrnItem.rminId.plus(currentMaterialCode)
            material.batchNumber = batchNo ?: ""
            vm.saveBagDetails(material, currentGrade)
        }

        setUpAdapter(bagList)
        enableSaveBtn(bagList.size > 0)
        if (palletCount.isNotEmpty() && !palletCount.equals("0"))
        {
            binding.btProceed.isEnabled = (bagList.size == palletCount.toInt())
            if (bagList.size == palletCount.toInt() || palletCount.equals("0")) binding.btProceed.setBackgroundColor(
                getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
            )
            else binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        else binding.btProceed.isEnabled = bagList.size > 0

        updateTotalWeights()
    }


    private fun saveItemsLocally() {

        if(bagList.size>0) {
            bagList.forEach { material ->
                material.batchNumber = batchNo ?: ""

                vm.saveBagDetails(material, currentGrade)
            }
        }
         callBack?.saveItemLocally()
        callBack?.replaceFragment(
            UPDATE_WEIGHT,
            binding.clNet.tvNetWeightValue.text.toString(),
            batchNo!!,
            palletWeight,
            palletCount,
            true
        )

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
            R.layout.item_vega_cameroon_fgrn_add_bag,
            ItemVegaCameroonFgrnAddBagBinding::inflate,
            { it, pos, bindItem ->
                if (pos % 2 == 0) {
                    bindItem.clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.rectangle_grey_border)
                } else {
                    bindItem.clBagHead.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.shape_rect_light_grey1)
                }
                bindItem.tvSno.text = pos.plus(1).toString()
                bindItem.tvBag.text = it.bagCount.toInt().plus(it.bagCount1!!.toInt()).toString()
                bindItem.tvGrossWeight.text =
                    it.grossWeight.toDouble().formatThreeDigits().plus(" ").plus("KG")
                val avgAvlue = it.palletAverage?.toDouble()
                    ?.plus(it.bagCount.toInt().times(it.tareWeight?.toDouble()!!))
                    ?.plus(it.bagCount1?.toInt()!!.times(it.tareWeight1?.toDouble()!!))
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
                binding.btSave.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            }
            false -> {
                binding.btSave.isEnabled = false
                binding.btSave.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
            }
        }

    }





}
