package com.olam.warehouse.vegax.processingghana.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.VegaGhanaProcessingOrderDetails
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminProcessLotDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingghana.R
import com.olam.warehouse.vegax.processingghana.databinding.FragmentVegaGhanaFgrnGradesBinding
import com.olam.warehouse.vegax.processingghana.databinding.ItemVegaGhanaFgrnGradesBinding
import com.olam.warehouse.vegax.processingghana.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGhanaFgrnGradesFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_ghana_fgrn_grades
    private lateinit var binding: FragmentVegaGhanaFgrnGradesBinding
    private val vm: VegaGhanaFgrnViewModel by viewModel()
    private var callBack: CallBack? = null

    private var gradeList = mutableListOf<VegaCoffeeFgrnItemsGrades>()
    private var cachedGradeList = listOf<VegaCoffeeFgrnItemsGrades>()
    private var fgrnItem = VegaCoffeeFgrnItems()
    private var offlineList = VegaCoffeeFgrnItemsGrades()
    private var fgrnId: String = ""
    private val isRmin: Boolean = false
    private var thirdPartyMaterials: List<VegaCoffeeThirdPartyMaterialDetail>? = null
    private var vegaStage = VegaProcessingStage()

    interface CallBack {
        fun replaceFgrnFragment(fragment: String, model: VegaCoffeeFgrnItems, id: String, stage: VegaProcessingStage)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCoffeeFgrnItems, id: String, vegaStage: VegaProcessingStage) = VegaGhanaFgrnGradesFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
            putString(FRAG_ID, id)
            putParcelable(VEGA_STAGE, vegaStage)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaGhanaFgrnGradesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        //menu.clear()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcocoa/ui/fgrn/VegaCocoaFgrnGradesFragment").title("Processing Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCoffeeFgrnItems()
        fgrnId = arguments?.getString(FRAG_ID) ?: ""
        vegaStage = arguments?.getParcelable(VEGA_STAGE) ?: VegaProcessingStage()
        binding.tvLotNo.text = fgrnItem.processOrderNo
        vm.poGradeList.observe(viewLifecycleOwner, Observer { updateFgrnGradeUI(it) })
        //  if (AppUtils.isOnline()) {

        if (fgrnItem.processOrderNo.contains("TMP_RMIN_"))
            vm.getofflineRminPostItem(fgrnItem.processOrderNo)
        else {
            if (AppUtils.isOnline())
                vm.getPoGrades(fgrnItem.processOrderNo, isRmin)
            else
                vm.getProcessOrderDetails(fgrnItem.processOrderNo)
        }
        /* } else {
             if(fgrnItem.processOrderNo.contains("TMP_RMIN_"))
             vm.getofflineRminPostItem(fgrnItem.processOrderNo)
                 else

         }*/
        vm.rminItemLocal.observe(viewLifecycleOwner, Observer {
            var tempList = it
            tempList.forEach {
                offlineList = prepareOfflineTempGrades(it)
                gradeList.add(offlineList)
            }
            updateAdapter(gradeList)

        })
        vm.processOrderDetailsItemLocal.observe(viewLifecycleOwner, Observer {
            var tempList = it.filter { it.bwart == "101" }
            tempList.forEach {
                offlineList = prepareOfflineGrades(it)
                gradeList.add(offlineList)
            }
            updateAdapter(gradeList)
        })

        vm.offlineGradeList.observe(viewLifecycleOwner, Observer { updateOfflineGradeUI(it) })
        binding.tvProceed.setOnClickListener { validateGrade() }
        vm.getThirdPartyMaterials()
        vm.thirdPartyMaterial.observe(viewLifecycleOwner, Observer {
            thirdPartyMaterials = it
        })
    }

    private fun prepareOfflineGrades(tempList: VegaGhanaProcessingOrderDetails): VegaCoffeeFgrnItemsGrades {
        var offline = VegaCoffeeFgrnItemsGrades()
        offline.processOrderNo = tempList.processOrderNo
        offline.storageLocationCode = tempList.storageLocationCode
        offline.bwart = tempList.bwart
        offline.materialCode = tempList.materialCode!!
        offline.materialName = tempList.materialName!!
        offline.meins = tempList.meins
        offline.unitOfMeasure = tempList.meins
        offline.phase = tempList.phase
        offline.plant = tempList.plant
        offline.resource = tempList.resource
        offline.rsNum = tempList.rsNum
        offline.rsPos = tempList.rsPos
        offline.xchpf = tempList.xchpf
        offline.isGradeChecked = false
        return offline
    }

    private fun prepareOfflineTempGrades(tempList: VegaGhanaOfflineRminProcessLotDetails): VegaCoffeeFgrnItemsGrades {
        var offline = VegaCoffeeFgrnItemsGrades()
        offline.processOrderNo = tempList.poNo!!
        offline.storageLocationCode = tempList.storageLocationCode
        offline.bwart = ""
        offline.materialCode = tempList.materialCode!!
        offline.materialName = tempList.materialName!!
        offline.meins = tempList.menge
        offline.unitOfMeasure = tempList.unitsOfMeasure
        offline.phase = tempList.phase
        offline.plant = tempList.plant
        offline.resource = ""
        offline.rsNum = ""
        offline.rsPos = ""
        offline.xchpf = tempList.xchpf
        offline.isGradeChecked = false
        return offline
    }

    private fun updateOfflineGradeUI(offlineGrades: List<VegaCoffeeFgrnItemsGrades>) {
        cachedGradeList = offlineGrades
        gradeList.forEach {
            if (offlineGrades.any { it1 -> it.materialCode.equals(it1.materialCode) }) {
                it.isGradeChecked = true
            }
        }
        binding.rvGrades.adapter?.notifyDataSetChanged()
        enableDisableBtn()
    }

    private fun validateGrade() {
        when (gradeList.any { it.isGradeChecked!! }) {
            true -> {
                val selectedGrades = gradeList.filter { it.isGradeChecked!! }
                val matName = selectedGrades.map { it.materialCode }
                val removeItem = arrayListOf<String>()
                fgrnItem.fgrnId = if (fgrnItem.fgrnId.isEmpty()) getTmpId() else fgrnItem.fgrnId
                selectedGrades.forEach {
                    it.fgrnId = fgrnItem.fgrnId
                    it.processOrderNo = fgrnItem.processOrderNo
                    it.fgrnIdMaterialCode = fgrnItem.fgrnId.plus(it.materialCode)
                }
                cachedGradeList.forEach {
                    if (!matName.contains(it.materialCode)) {
                        removeItem.add(it.fgrnIdMaterialCode)
                    }
                }
                vm.saveFgrnItem(fgrnItem, selectedGrades, removeItem)
                val gson = GsonUtils()
                val poGrade = gson.toJson(selectedGrades)
                cachedGradeList = selectedGrades
                if (isOnline()) {
                    callBack?.replaceFgrnFragment(FRAG_ADD_WEIGHT, fgrnItem, poGrade, vegaStage)
                } else {
                    callBack?.replaceFgrnFragment(FRAG_RMIN_LIST, fgrnItem, poGrade, vegaStage)
                }
            }
            false -> showSnack(requireContext().resources.getString(R.string.please_check_atleast_one_grade))
        }
    }

    private fun updateFgrnGradeUI(response: Resource<GenericReqAndResp<List<VegaCoffeeFgrnItemsGrades>>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let {
                                gradeList = it as MutableList<VegaCoffeeFgrnItemsGrades>
                                updateAdapter(gradeList)
                                if (fgrnItem.fgrnId.isNotEmpty()) vm.getOfflineGrades(
                                    fgrnItem.processOrderNo,
                                    fgrnItem.fgrnId
                                )
                            }
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun updateAdapter(gradeList: MutableList<VegaCoffeeFgrnItemsGrades>) {

        binding.rvGrades.setUpAdapter(
            gradeList,
            R.layout.item_vega_ghana_fgrn_grades,
            ItemVegaGhanaFgrnGradesBinding::inflate,
            { item, pos, bindItem ->
                bindItem.cbGrades.text = item.materialName
                bindItem.cbGrades.isChecked = item.isGradeChecked!!
                val filterData =
                    thirdPartyMaterials?.filter {
                        gradeList[pos].materialCode.contains(
                            it.materialCode ?: ""
                        )
                    }
                if (filterData?.isNotEmpty() == true) {
                    gradeList[pos].isThirdPartyMaterial = true
                }
                bindItem.grades.setOnClickListener {
                    if (!bindItem.cbGrades.isChecked) {
                        val filterData =
                            thirdPartyMaterials?.filter {
                                gradeList[pos].materialCode.contains(
                                    it.materialCode ?: ""
                                )
                            }
                        if (filterData?.isNotEmpty() == true) {
                            gradeList[pos].isThirdPartyMaterial = true
                            if (fgrnItem.vendor?.isNotEmpty() == true) {
                                updateGradeSelection(gradeList, pos, bindItem)
                            } else
                                showVendorErrorDialog()
                        } else {
                            updateGradeSelection(gradeList, pos, bindItem)
                        }

                    } else {
                        gradeList[pos].isThirdPartyMaterial = false
                        updateGradeSelection(gradeList, pos, bindItem)
                    }
                }
            })
    }

    private fun View.updateGradeSelection(
        gradeList: MutableList<VegaCoffeeFgrnItemsGrades>,
        pos: Int,
        bindItem: ItemVegaGhanaFgrnGradesBinding
    ) {
        gradeList[pos].isGradeChecked = !bindItem.cbGrades.isChecked
        binding.rvGrades.adapter?.notifyItemChanged(pos)
        enableDisableBtn()
    }

    private fun enableDisableBtn() {
        if (gradeList.any { it.isGradeChecked!! }) {
            binding.tvProceed.isEnabled = true
            binding.tvProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.tvProceed.isEnabled = false
            binding.tvProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
    }

    private fun showVendorErrorDialog() {
        MaterialDialog(requireActivity()).show {
            message((R.string.third_party_error_msg))
            positiveButton(
                text = UIUtils.getSpannedText(
                    view.context.getString(com.olam.warehouse.presentation.R.string.ok),
                    true
                )
            ) {
                dismiss()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }
}
