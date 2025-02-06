package com.olam.warehouse.vegax.processing.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaFgrnGrades
import com.olam.warehouse.master.vega.entity.VegaFgrnProcessingOrder
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processing.R
import com.olam.warehouse.vegax.processing.databinding.FragmentVegaFgrnPoSelectionBinding
import com.olam.warehouse.vegax.processing.ui.IOBackpressed
import com.olam.warehouse.vegax.processing.ui.VegaProcessingViewModel
import com.olam.warehouse.vegax.processing.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.DecimalFormat

class VegaFgrnPoSelectionFragment : BaseFragment(), IOBackpressed {
    override val layoutResourceId = R.layout.fragment_vega_fgrn_po_selection
    private lateinit var binding: FragmentVegaFgrnPoSelectionBinding
    private var callBack: CallBack? = null
    private var gradeList: List<VegaFgrnGrades>? = null
    private var poOrderDetails: VegaFgrnProcessingOrder? = null
    private var storageLocaionList: List<VegaCustomStLocation>? = null
    private val vm: VegaProcessingViewModel by viewModel()
    private var noOfGradeItems: Int = 0
    private var stroageLocationCode: String? = ""
    private var selectedItemPosition: Int = 1
    private var slPostion: Int? = 0
    private var processFgrnLotsList = mutableListOf<VegaDispatchLots>()
    interface CallBack {
        fun replaceFragment(fragment: String, poOrder: VegaFgrnProcessingOrder, poGrade: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaFgrnPoSelectionFragment.CallBack
    }

    companion object {
        fun newInstance(poOrder: VegaFgrnProcessingOrder, gradeList: String) = VegaFgrnPoSelectionFragment().putArgs {
            putParcelable(PO_DETAILS, poOrder)
            putString(GRADE_LIST, gradeList)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaFgrnPoSelectionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processing/ui/fgrn/VegaFgrnPoSelectionFragment").title("Processing").with(tracker)
        initUI()
    }


    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnProceed, it, true)
        }
        updateMandatory()

        poOrderDetails = arguments?.getParcelable(PO_DETAILS)!!
        gradeList = Gson().fromJson<List<VegaFgrnGrades>>(arguments?.getString(GRADE_LIST) ?: "")
        binding.btnProceed.setOnClickListener { validateFidelds() }
        vm.storageLocation.observe(
            viewLifecycleOwner,
            Observer { updateStroageUI(it.filter { !it.storageLocationType.equals("P") }) })
        vm.fetchStorageLocation()
        binding.tvPono.text = poOrderDetails?.processOrderNo
        noOfGradeItems = gradeList!!.size
        when (noOfGradeItems) {
            1 -> {
                binding.btnProceed.text = requireContext().resources.getString(R.string.proceed)
            }
            else -> {
                binding.btnProceed.text = requireContext().resources.getString(R.string.next)
            }
        }
        binding.tvOutputMaterial.text = gradeList?.get(0)?.materialName
        binding.etWeight.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(8, 3))

    }


    private fun validateFidelds() {

        val weight=weightToProcess(poOrderDetails?.rminList, poOrderDetails?.rfgrnList)
        val weightToProcess= DecimalFormat("#########.###").format(weight).toString().toDouble()
        when {
            binding.etWeight.text.toString().isEmpty() -> {
                showSnack(requireContext().resources.getString(R.string.enter_weight))
            }
            binding.etWeight.text.toString() == "." -> {
                showSnack(requireContext().resources.getString(R.string.enter_valid_weight))
            }
            binding.etWeight.text.toString().toDouble() > weightToProcess -> {
                showSnack(requireContext().resources.getString(R.string.more_rmin_weight))
            }
            binding.etNoOfBags.text.toString().isEmpty() -> {
                showSnack(requireContext().resources.getString(R.string.enter_valid_bags))
            }
            stroageLocationCode.isNullOrEmpty() -> {
                showSnack(requireContext().resources.getString(R.string.select_storage_location))
            }
            else -> {
                if (!binding.tvOutputMaterial.text.toString().equals("Losses", ignoreCase = true)) {
                    when {
                        binding.etWeight.text.toString() == "0" || binding.etWeight.text.toString() == "0.0" -> {
                            showSnack(requireContext().resources.getString(R.string.enter_valid_weight))
                        }
                        binding.etNoOfBags.text.toString() == "0" -> {
                            showSnack(requireContext().resources.getString(R.string.enter_valid_bags))
                        }
                        else -> {
                            updateLotData()
                        }
                    }
                } else {
                    updateLotData()
                }


            }
        }
    }

    private fun updateLotData() {
        when (gradeList?.size) {
            processFgrnLotsList.size -> {
                if (selectedItemPosition < noOfGradeItems) {
                    selectedItemPosition += 1
                    if (selectedItemPosition == noOfGradeItems) {

                        processFgrnLotsList[selectedItemPosition - 2] = prepareLotDetailsToFgrn(
                            gradeList, poOrderDetails!!, selectedItemPosition - 1, binding.etWeight.text.toString(),
                            binding.etNoOfBags.text.toString(), stroageLocationCode!!, slPostion!!
                        )
                        binding.tvOutputMaterial.text = processFgrnLotsList[selectedItemPosition - 1].materialName
                        val withoutLossWeight = processFgrnLotsList.sumByDouble {
                            it.weight.toString().toDouble()
                        } - processFgrnLotsList[processFgrnLotsList.size - 1].weight?.toDouble()!!
                        val rminWeight = weightToProcess(poOrderDetails?.rminList!!, poOrderDetails?.rfgrnList!!)
                        val lossWeight = rminWeight.minus(withoutLossWeight)
                        binding.etWeight.setText(DecimalFormat("#########.###").format(lossWeight).toString())
                        binding.etNoOfBags.setText(processFgrnLotsList[selectedItemPosition - 1].noOfBags)
                        processFgrnLotsList[selectedItemPosition - 1].slPostion?.let {
                            binding.spLocation.setSelection(it)
                        }
                        binding.btnProceed.text = requireContext().resources.getString(R.string.proceed)
                    } else {
                        processFgrnLotsList[selectedItemPosition - 1] = prepareLotDetailsToFgrn(
                            gradeList, poOrderDetails!!, selectedItemPosition, binding.etWeight.text.toString(),
                            binding.etNoOfBags.text.toString(), stroageLocationCode!!, slPostion!!
                        )
                        binding.etWeight.isEnabled = true
                        binding.btnProceed.text = requireContext().resources.getString(R.string.next)
                    }

                } else if (selectedItemPosition == noOfGradeItems) {
                    processFgrnLotsList[selectedItemPosition - 1] = prepareLotDetailsToFgrn(
                        gradeList, poOrderDetails!!, selectedItemPosition, binding.etWeight.text.toString(),
                        binding.etNoOfBags.text.toString(), stroageLocationCode!!, slPostion!!
                    )
                    movetoSummary(poOrderDetails!!, processFgrnLotsList)
                }

            }
            else -> {
                val materialName = gradeList?.get(selectedItemPosition - 1)?.materialName
                when {
                    processFgrnLotsList.size != 0 -> {
                        val value = processFgrnLotsList.forEachIndexed { index, vegaDispatchLots ->
                            if (vegaDispatchLots.materialName == materialName)
                                processFgrnLotsList[selectedItemPosition - 1] = prepareLotDetailsToFgrn(
                                    gradeList, poOrderDetails!!, selectedItemPosition, binding.etWeight.text.toString(),
                                    binding.etNoOfBags.text.toString(), stroageLocationCode!!, slPostion!!
                                ) else {
                                processFgrnLotsList.add(prepareLotDetailsToFgrn(gradeList, poOrderDetails!!, selectedItemPosition, binding.etWeight.text.toString(), binding.etNoOfBags.text.toString(), stroageLocationCode!!, slPostion!!))
                            }
                        }
                    }
                    else -> {
                        processFgrnLotsList.add(prepareLotDetailsToFgrn(gradeList, poOrderDetails!!, selectedItemPosition, binding.etWeight.text.toString(), binding.etNoOfBags.text.toString(), stroageLocationCode!!, slPostion!!))
                    }
                }

                if (selectedItemPosition < noOfGradeItems) {
                    binding.tvOutputMaterial.text = gradeList?.get(selectedItemPosition)?.materialName
                    binding.etWeight.setText("")
                    binding.etNoOfBags.setText("")
                    stroageLocationCode = ""
                    updateStroageUI(storageLocaionList!!)
                    selectedItemPosition += 1
                    if (selectedItemPosition == noOfGradeItems) {
                        val withoutLossWeight = processFgrnLotsList.sumByDouble { it.weight.toString().toDouble() }
                        val rminWeight = weightToProcess(poOrderDetails?.rminList, poOrderDetails?.rfgrnList)
                        val lossWeight = rminWeight.minus(withoutLossWeight)
                        binding.etWeight.setText(DecimalFormat("#########.###").format(lossWeight))
                        binding.btnProceed.text = requireContext().resources.getString(R.string.proceed)
                    } else {
                        binding.etWeight.isEnabled = true
                        binding.btnProceed.text = requireContext().resources.getString(R.string.next)
                    }

                } else if (selectedItemPosition == noOfGradeItems) {
                    binding.btnProceed.text = requireContext().resources.getString(R.string.proceed)
                    movetoSummary(poOrderDetails!!, processFgrnLotsList)
                }

            }
        }

        when {
            binding.tvOutputMaterial.text.toString().equals("Losses", ignoreCase = true) -> {
                binding.etWeight.isEnabled = false
            }
            else -> {
                binding.etWeight.isEnabled = true
            }
        }
    }


    private fun movetoSummary(poOrderDetails: VegaFgrnProcessingOrder, processLotList: List<VegaDispatchLots>) {
        val gson = GsonUtils()
        val lotDetails = gson.toJson(processLotList)
        callBack?.replaceFragment(FGRNSUMMARY, poOrderDetails, lotDetails)

    }

    private fun updateStroageUI(storageLocaionList: List<VegaCustomStLocation>) {
        this.storageLocaionList = storageLocaionList
        val storageLocation = ArrayList<VegaCustomStLocation>()
        val storageLocationinit = VegaCustomStLocation()
        storageLocationinit.procureLocationName = "--Select Storage--"
        storageLocation.add(storageLocationinit)
        for (stroageItem in storageLocaionList) {
            storageLocation.add(stroageItem)
        }
        val storageListdata = storageLocation.distinctBy { it.procureLocationCode }
            .map { data -> data.procureLocationCode.plus("-").plus(data.procureLocationName) }
        val stroageAdapter = ArrayAdapter(requireContext(), R.layout.item_vega_processing_rmin_grade, storageListdata)
        stroageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spLocation.adapter = stroageAdapter
        binding.spLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                for (data in storageLocaionList) {
                    val code = storageListdata[position].split("-")
                    if (data.procureLocationCode == code[0]) {
                        slPostion = position
                        stroageLocationCode =
                            data.procureLocationCode.plus("-").plus(data.procureLocationName.toString())
                    }
                }
            }
        }
    }

    override fun onBackPressed(): Boolean {
        when {
            processFgrnLotsList.size == 0 || selectedItemPosition == 1 -> {
                return true
            }
            else -> {
                if (selectedItemPosition == processFgrnLotsList.size) {
                    selectedItemPosition -= 1
                    binding.etWeight.isEnabled = true
                    binding.btnProceed.text = requireContext().resources.getString(R.string.next)
                }
                if (selectedItemPosition < processFgrnLotsList.size) {
                    binding.tvOutputMaterial.text = processFgrnLotsList[selectedItemPosition - 1].materialName
                    binding.etWeight.setText(processFgrnLotsList[selectedItemPosition - 1].weight)
                    binding.etNoOfBags.setText(processFgrnLotsList[selectedItemPosition - 1].noOfBags)
                    processFgrnLotsList[selectedItemPosition - 1].slPostion?.let { binding.spLocation.setSelection(it) }
                }
                if (processFgrnLotsList.size == 1) {
                    selectedItemPosition -= 1
                    binding.etWeight.isEnabled = true
                    binding.tvOutputMaterial.text = processFgrnLotsList[selectedItemPosition - 1].materialName
                    binding.etWeight.setText(processFgrnLotsList[selectedItemPosition - 1].weight)
                    binding.etNoOfBags.setText(processFgrnLotsList[selectedItemPosition - 1].noOfBags)
                    processFgrnLotsList[selectedItemPosition - 1].slPostion?.let { binding.spLocation.setSelection(it) }
                    binding.btnProceed.text = requireContext().resources.getString(R.string.next)
                }
                return false
            }
        }
    }


    private fun updateMandatory() {
        binding.tvOutputMaterialLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.output_material)) { mandatoryStars() } }
        binding.etWeightLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.weight)) { mandatoryStars() } }
        binding.etNoOfBagsLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.no_bags)) { mandatoryStars() } }
        binding.tvLocationLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.select_storage_location)) { mandatoryStars() } }

    }
}


