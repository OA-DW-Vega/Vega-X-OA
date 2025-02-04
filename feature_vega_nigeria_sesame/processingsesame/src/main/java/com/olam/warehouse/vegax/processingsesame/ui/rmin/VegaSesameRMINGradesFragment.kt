package com.olam.warehouse.vegax.processingsesame.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingsesame.R
import com.olam.warehouse.vegax.processingsesame.databinding.FragmentVegaSesameFgrnGradesBinding
import com.olam.warehouse.vegax.processingsesame.utils.*
import kotlinx.android.synthetic.main.item_vega_sesame_fgrn_grades.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaSesameRMINGradesFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_sesame_fgrn_grades
    private lateinit var binding: FragmentVegaSesameFgrnGradesBinding
    private val vm: VegaSesameRminViewModel by viewModel()
    private var callBack: CallBack? = null

    private var gradeList = mutableListOf<VegaCoffeeFgrnItemsGrades>()
    private var cachedGradeList = listOf<VegaCoffeeFgrnItemsGrades>()
    private lateinit var fgrnItem: VegaCoffeeRminProcessing
    private var fgrnId: String = ""
    private var thirdPartyMaterials: List<VegaCoffeeThirdPartyMaterialDetail>? = null

    interface CallBack {
        fun replaceFragment(fragment: String, flag: Boolean, model: VegaCoffeeRminProcessing)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCoffeeRminProcessing, id: String) = VegaSesameRMINGradesFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
            putString(FRAG_ID, id)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaSesameFgrnGradesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        //menu.clear()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingsesame/ui/fgrn/VegaCoffeeRMINGradesFragment").title("Processing Coffee")
            .with(tracker)
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvHeadPoDetail, it, false)
        }
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCoffeeRminProcessing()
        vm.getRminData(fgrnItem.poNumber)
        vm.rminModel.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                fgrnItem = it
            }
        })
        vm.getThirdPartyMaterials()
        vm.thirdPartyMaterial.observe(viewLifecycleOwner, Observer {
            thirdPartyMaterials = it
        })
        binding.tvLotNo.text = fgrnItem.poNumber
        vm.poGradeList.observe(viewLifecycleOwner, Observer { updateFgrnGradeUI(it) })
        vm.getPoGrades(fgrnItem.poNumber, true)
        vm.offlineGradeList.observe(viewLifecycleOwner, Observer { updateOfflineGradeUI(it) })
        binding.tvProceed.setOnClickListener { validateGrade() }
    }

    private fun updateOfflineGradeUI(offlineGrades: List<VegaCoffeeFgrnItemsGrades>) {
        cachedGradeList = offlineGrades
        if (offlineGrades.isNotEmpty()) {
            fgrnItem.rminId = offlineGrades[0].fgrnId
        }
        gradeList.forEach {
            offlineGrades.forEachIndexed { index, vegaCoffeeFgrnItemsGrades ->
                if (vegaCoffeeFgrnItemsGrades.materialCode == it.materialCode) {
                    it.isGradeChecked = true
                    it.weightToProcess = vegaCoffeeFgrnItemsGrades.weightToProcess
                }
            }
            /*if (offlineGrades.any { it1 -> it.materialCode.equals(it1.materialCode) }) {
                it.isGradeChecked = true
            }*/
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
                fgrnItem.rminId = if (fgrnItem.rminId.isEmpty()) getTmpId() else fgrnItem.rminId
                val material = selectedGrades.map { it.materialCode }
                val materialName = selectedGrades.map { it.materialName }
                selectedGrades.forEach {
                    it.fgrnId = fgrnItem.rminId
                    it.processOrderNo = fgrnItem.poNumber
                    it.fgrnIdMaterialCode = fgrnItem.rminId.plus(it.materialCode)
                }
                cachedGradeList.forEach {
                    if (!matName.contains(it.materialCode)) {
                        removeItem.add(it.fgrnIdMaterialCode)
                    }
                }
                fgrnItem.materialCode = material.toString().replace("[", "").replace("]", "")
                fgrnItem.materialName = materialName.toString().replace("[", "").replace("]", "")
                vm.saveRMINItem(fgrnItem, selectedGrades, removeItem)
                val gson = GsonUtils()
                val poGrade = gson.toJson(selectedGrades)
                cachedGradeList = selectedGrades
                fgrnItem.gradeListDetails = poGrade
                callBack?.replaceFragment(ADDLOT, false, fgrnItem)
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
                                vm.getOfflineGrades(
                                    fgrnItem.poNumber
                                )
                            }
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
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

    private fun updateAdapter(gradeList: MutableList<VegaCoffeeFgrnItemsGrades>) {

        binding.rvGrades.setUp(gradeList, R.layout.item_vega_sesame_fgrn_grades, { item, pos ->
            cbGrades.text = item.materialName
            if(item.materialCode.startsWith("0000002"))
            {
                cbGrades.isEnabled = false
            }
            cbGrades.isChecked = item.isGradeChecked!!
            val filterData =
                thirdPartyMaterials?.filter { gradeList[pos].materialCode.contains(it.materialCode ?: "") }
            if (filterData?.isNotEmpty() == true) {
                gradeList[pos].isThirdPartyMaterial = true
            }

            grades.setOnClickListener {
                if(item.materialCode.startsWith("0000002"))
                {
                    cbGrades.isChecked= false
                }
                else{
                    if (!cbGrades.isChecked) {
                        val filterData =
                            thirdPartyMaterials?.filter { gradeList[pos].materialCode.contains(it.materialCode ?: "") }
                        if (filterData?.isNotEmpty() == true) {
                            if (fgrnItem.vendor?.isNotEmpty() == true) {
                                gradeList[pos].isThirdPartyMaterial = true
                                updateGradeSelection(gradeList, pos)
                            } else
                                showVendorErrorDialog()
                        } else {
                            updateGradeSelection(gradeList, pos)
                        }

                    } else {
                        gradeList[pos].isThirdPartyMaterial = false
                        updateGradeSelection(gradeList, pos)
                    }
                }

            }
        })
    }

    private fun View.updateGradeSelection(
        gradeList: MutableList<VegaCoffeeFgrnItemsGrades>,
        pos: Int
    ) {
        gradeList[pos].isGradeChecked = !cbGrades.isChecked
        binding.rvGrades.adapter?.notifyItemChanged(pos)
        enableDisableBtn()
    }

    private fun enableDisableBtn() {
        if (gradeList.any { it.isGradeChecked!! }) {
            binding.tvProceed.isEnabled = true
            binding.tvProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.tvProceed.isEnabled = false
            binding.tvProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
    }

    private fun showVendorErrorDialog() {
        MaterialDialog(activity!!).show {
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
}
