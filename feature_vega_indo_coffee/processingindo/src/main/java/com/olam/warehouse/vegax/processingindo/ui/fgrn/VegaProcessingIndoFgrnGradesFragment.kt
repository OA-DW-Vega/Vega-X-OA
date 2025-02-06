package com.olam.warehouse.vegax.processingindo.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItemsGrades
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingindo.R
import com.olam.warehouse.vegax.processingindo.databinding.FragmentVegaProcessingIndoFgrnGradesBinding
import com.olam.warehouse.vegax.processingindo.databinding.ItemVegaProcessingIndoFgrnGradesBinding
import com.olam.warehouse.vegax.processingindo.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 6/2/2020.
 */
class VegaProcessingIndoFgrnGradesFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_processing_indo_fgrn_grades
    private lateinit var binding: FragmentVegaProcessingIndoFgrnGradesBinding
    private val vm: VegaProcessingIndoFgrnViewModel by viewModel()
    private var callBack: CallBack? = null

    private var gradeList = mutableListOf<VegaCocoaFgrnItemsGrades>()
    private var cachedGradeList = listOf<VegaCocoaFgrnItemsGrades>()
    private var fgrnItem = VegaCocoaFgrnItems()
    private var fgrnId: String = ""
    private val isRmin: Boolean = false
    private var weighmentType = ""
    private var isIndexweighmenttype:Boolean = false


    interface CallBack {
        fun replaceFgrnFragment(fragment: String, model: VegaCocoaFgrnItems, id: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCocoaFgrnItems, id: String) = VegaProcessingIndoFgrnGradesFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
            putString(FRAG_ID, id)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaProcessingIndoFgrnGradesBinding.inflate(layoutInflater)
        vm.getConfigItems(UserRoles.PROCESSING.role)
        vm.configItems.observe(viewLifecycleOwner, Observer {
            updateConfigItems(it)
        })
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcocoa/ui/fgrn/VegaCocoaFgrnGradesFragment").title("Processing Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCocoaFgrnItems()
        fgrnId = arguments?.getString(FRAG_ID) ?: ""
        binding.tvLotNo.text = fgrnItem.processOrderNo
        vm.poGradeList.observe(viewLifecycleOwner, Observer { updateFgrnGradeUI(it) })
        vm.getPoGrades(fgrnItem.processOrderNo, isRmin)
        vm.offlineGradeList.observe(viewLifecycleOwner, Observer { updateOfflineGradeUI(it) })
        binding.tvProceed.setOnClickListener { validateGrade() }


    }

    private fun updateOfflineGradeUI(offlineGrades: List<VegaCocoaFgrnItemsGrades>) {
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
                    it.unitOfMeasure = it.meins
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
                    callBack?.replaceFgrnFragment(FRAG_ADD_WEIGHT, fgrnItem, poGrade)
            }
            false -> showSnack(requireContext().resources.getString(R.string.please_check_atleast_one_grade))
        }
    }

    private fun updateFgrnGradeUI(response: Resource<GenericReqAndResp<List<VegaCocoaFgrnItemsGrades>>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let {
                                gradeList = it as MutableList<VegaCocoaFgrnItemsGrades>
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

    private fun updateAdapter(gradeList: MutableList<VegaCocoaFgrnItemsGrades>) {
        binding.rvGrades.setUpAdapter(
            gradeList,
            R.layout.item_vega_processing_indo_fgrn_grades,
            ItemVegaProcessingIndoFgrnGradesBinding::inflate,
            { item, pos, bindItem ->
                bindItem.cbGrades.text = item.materialName
                bindItem.cbGrades.isChecked = item.isGradeChecked!!
                bindItem.grades.setOnClickListener {
                    gradeList[pos].isGradeChecked = !bindItem.cbGrades.isChecked
                    if (isIndexweighmenttype && gradeList[pos].isGradeChecked!!) {
                        MaterialDialog(requireContext()).show {
                            title(R.string.select_weighment_type)
                            listItemsSingleChoice(R.array.weighmentType) { _, index, text ->
                                when (index) {
                                    0 -> {
                                        weighmentType = "Field of Index"
                                        isIndexweighmenttype = true
                                        gradeList[pos].isIndexweighmenttype = true
                                        fgrnItem.isIndexweighmenttype = true
                                        fgrnItem.weighmentType = "Field of Index"
                                        enableDisableBtn()
                                    }
                                    1 -> {
                                        weighmentType = "Pallet"
                                        isIndexweighmenttype = false
                                        gradeList[pos].isIndexweighmenttype = false
                                        fgrnItem.weighmentType = "Pallet"
                                        fgrnItem.isIndexweighmenttype = false

                                        enableDisableBtn()
                                    }
                                }
                            }
                            positiveButton(
                                text = UIUtils.getSpannedText(
                                    getString(R.string.ok),
                                    true
                                )
                            )
                        }
                    }
                    binding.rvGrades.adapter?.notifyItemChanged(pos)
                    enableDisableBtn()
                }
            })
    }

    private fun enableDisableBtn() {
        if (gradeList.any { it.isGradeChecked!! }) {
            binding.tvProceed.isEnabled = true
            binding.tvProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        }
        else {
            binding.tvProceed.isEnabled = false
            binding.tvProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
    }
    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.WEIGHMENT_METHOD_ENABLED.item -> {
                    when {
                        it.applicable?.contains("Y")!! ->{
                            isIndexweighmenttype=true
                        }

                        it.applicable?.contains("N")!! -> isIndexweighmenttype=false
                    }
                }
            }
        }
    }
}
