package com.olam.warehouse.vegax.processingnigeria.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingnigeria.R
import com.olam.warehouse.vegax.processingnigeria.databinding.FragmentVegaNigeriaFgrnGradesBinding
import com.olam.warehouse.vegax.processingnigeria.utils.*
import kotlinx.android.synthetic.main.item_vega_nigeria_fgrn_grades.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaFgrnGradesFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_nigeria_fgrn_grades
    private lateinit var binding: FragmentVegaNigeriaFgrnGradesBinding
    private val vm: VegaNigeriaFgrnViewModel by viewModel()
    private var callBack: CallBack? = null

    private var gradeList = mutableListOf<VegaCoffeeFgrnItemsGrades>()
    private var cachedGradeList = listOf<VegaCoffeeFgrnItemsGrades>()
    private var fgrnItem = VegaCoffeeFgrnItems()
    private var fgrnId: String = ""
    private val isRmin: Boolean = false
    private var thirdPartyMaterials: List<VegaCoffeeThirdPartyMaterialDetail>? = null

    interface CallBack {
        fun replaceFgrnFragment(fragment: String, model: VegaCoffeeFgrnItems, id: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCoffeeFgrnItems, id: String) = VegaNigeriaFgrnGradesFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
            putString(FRAG_ID, id)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNigeriaFgrnGradesBinding.inflate(layoutInflater)
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
        binding.tvLotNo.text = fgrnItem.processOrderNo
        vm.poGradeList.observe(viewLifecycleOwner, Observer { updateFgrnGradeUI(it) })
        vm.getPoGrades(fgrnItem.processOrderNo, isRmin)
        vm.offlineGradeList.observe(viewLifecycleOwner, Observer { updateOfflineGradeUI(it) })
        binding.tvProceed.setOnClickListener { validateGrade() }
        vm.getThirdPartyMaterials()
        vm.thirdPartyMaterial.observe(viewLifecycleOwner, Observer {
            thirdPartyMaterials = it
        })
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
                callBack?.replaceFgrnFragment(FRAG_ADD_WEIGHT, fgrnItem, poGrade)
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

        binding.rvGrades.setUp(gradeList, R.layout.item_vega_nigeria_fgrn_grades, { item, pos ->
            cbGrades.text = item.materialName
            cbGrades.isChecked = item.isGradeChecked!!
            val filterData =
                thirdPartyMaterials?.filter { gradeList[pos].materialCode.contains(it.materialCode ?: "") }
            if (filterData?.isNotEmpty() == true) {
                gradeList[pos].isThirdPartyMaterial = true
            }
            grades.setOnClickListener {
                if (!cbGrades.isChecked) {
                    val filterData =
                        thirdPartyMaterials?.filter { gradeList[pos].materialCode.contains(it.materialCode ?: "") }
                    if (filterData?.isNotEmpty() == true) {
                        gradeList[pos].isThirdPartyMaterial = true
                        if (fgrnItem.vendor?.isNotEmpty() == true) {
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }
}
