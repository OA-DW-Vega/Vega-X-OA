package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.mtntghanacocoa.databinding.ItemGhanaCocoaInventoryLotBinding
import com.olam.warehouse.vegax.mtntghanacocoa.ui.UpdateNigeriaSesameMtntSelectedLotWeightListener
import com.olam.warehouse.vegax.mtntghanacocoa.utils.listOfField
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.R
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGRNDWLotManualModel
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.databinding.FragmentGhanaCocoaGrnDwsListBinding
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.SELECTED_LOT_ID
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class VegaGhanaCocoaLotListDWGrn() : BaseFragment(), GhanaCocoaMTNTItemRemoveListener,
    UpdateNigeriaSesameMtntSelectedLotWeightListener {
    override val layoutResourceId = R.layout.fragment_ghana_cocoa_grn_dws_list
    private lateinit var binding: FragmentGhanaCocoaGrnDwsListBinding
    private val vm: VegaGhanaCocoaOffloadingViewModel by viewModel()
    private lateinit var callbackCocoa: VegaGhanaCocoaOffloadReplaceFragmentCallback
    private var wareHouseList = mutableListOf<String>()
    private var dispatchLotsList = mutableListOf<VegaGhanaCocoaDispatchLots>()
    private var filteredDispatchLotsList = mutableListOf<VegaGhanaCocoaDispatchLots>()

    // private var adapterForRecycler: VegaGhanaGrnCocaListDWGAdapter? = null
    private var listenerCocoa: VegaGhanaCocoaAddLotListener? = null
    private var selectedLotIdModel: VegaGRNDWLotManualModel? = null
    private var selectedLotId = ""

    companion object {
        fun newInstance(vegaGRNDWLotManualModel: VegaGRNDWLotManualModel, selectedLotId: String) =
            VegaGhanaCocoaLotListDWGrn()
                .putArgs {
                    putParcelable(MODEL_BUNDLE, vegaGRNDWLotManualModel)
                    putString(SELECTED_LOT_ID, selectedLotId)
                }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbackCocoa = context as VegaGhanaCocoaOffloadReplaceFragmentCallback
        listenerCocoa = context as VegaGhanaCocoaAddLotListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGhanaCocoaGrnDwsListBinding.inflate(layoutInflater)
        initExtra()
        initAdapter()
        initUI()
        return binding.root
    }


    private fun showWarehouseListDialog(it: List<String>) {
        MaterialDialog(requireContext()).show {
            title(com.olam.warehouse.vegax.mtntghanacocoa.R.string.select_wh)
            listItemsMultiChoice(items = it) { _, index, text ->
                filterWarehouseList(text.toString())
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.presentation.R.string.ok), true))
        }
    }


    private fun filterWarehouseList(locations: String) {
        val filter = locations.replace("[", "").replace("]", "").trim()
        if (filter.isEmpty()) {
            callTheApi("")
            binding.tvWareHouse.text = getString(com.olam.warehouse.vegax.mtntghanacocoa.R.string.all)
        } else {
            filteredDispatchLotsList.clear()
            binding.tvWareHouse.text = filter
            callTheApi(selectedLotId)
            /*  val lotsList = dispatchLotsList.filter { locations.contains(it.storageLocationCode ?: "") }
              filteredDispatchLotsList.addAll(lotsList)*/
        }
    }


    private fun getWarehouseList() {
        if (dispatchLotsList.isNotEmpty()) {
            wareHouseList.addAll(dispatchLotsList.listOfField(VegaGhanaCocoaDispatchLots::storageLocationCode).toSet())
        }
    }


    private fun initUI() {
        binding.apply {
            vm.apply {

                loader.observe(viewLifecycleOwner)
                {
                    when (it) {
                        true -> {
                            showLoading()
                        }

                        else -> {

                        }
                    }

                }

                enableProceed(false)
                lotListForDWS.observe(viewLifecycleOwner)
                {
                    updateLotList(it)
                }
                callTheApi(selectedLotId)
                getWarehouseList()
                tvWareHouse.setOnClickListener { showWarehouseListDialog(wareHouseList) }
                btnProceed.setOnClickListener { sendSelectedLots() }
            }
        }
    }

    private fun updateLotList(response: Resource<GenericReqAndResp<List<VegaGRNDWLotManualModel>>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data.let { its ->
                        binding.tvNoData.isVisible = it.data?.data?.isEmpty() == true
                        its?.toMutableList()?.let { it1 ->
                            selectedLotIdModel?.let {
                                it1.forEachIndexed { index, lData ->
                                    if (selectedLotIdModel?.lotId.equals(lData.lotId, true)) {
                                        lData.isAdded = true
                                        lData.isNormalAdd = true
                                    }
                                }
                            }

                            setupAdapterForRecyler(it1)

                        }
                    }
                }

                Resource.Status.LOADING -> {
                    showLoading()
                }

                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun setupAdapterForRecyler(models: MutableList<VegaGRNDWLotManualModel>) {
        binding.apply {
            vm.apply {
                rvLots.setUpAdapter(models,
                    com.olam.warehouse.vegax.mtntghanacocoa.R.layout.item_ghana_cocoa_inventory_lot,
                    ItemGhanaCocoaInventoryLotBinding::inflate, { lData, pos, bindItem ->
                        bindItem.apply {
                            tvLotId.text = lData.lotId
                            tvGradeValue.text = lData.productName
                            tvEditWeight.text = tvEditWeight.context.getString(R.string.vendor_name)
                            tvWeightValue.text = lData.vendorName
                            tvStLocationValue.text = lData.noOfBags
                            tvStLocation.text = tvEditWeight.context.getString(R.string.noofbags)
                            ivSelect.isChecked = lData.isAdded
                            llLotItem.setOnClickListener {
                                ivSelect.isChecked = !lData.isAdded
                            }
                            ivSelect.setOnCheckedChangeListener { _, hasChecked ->
                                if (hasChecked && !lData.isNormalAdd) {
                                    lData.isAdded = true
                                    lData.isNormalAdd = true
                                    models.forEachIndexed { index, vegaGRNDWLotManualModel ->
                                        if (index != pos) {
                                            vegaGRNDWLotManualModel.isAdded = false
                                            vegaGRNDWLotManualModel.isNormalAdd = false
                                        }
                                    }
                                    updateLotWeights(lData)
                                    rvLots.adapter?.notifyDataSetChanged()
                                } else {
                                    lData.isAdded = false
                                    lData.isNormalAdd = false
                                }
                            }

                        }
                    }
                )
            }
        }
    }


    private fun sendSelectedLots() {
        val selectedLot = ArrayList<VegaGRNDWLotManualModel>()
        selectedLotIdModel?.let { its -> selectedLot.add(its) }
        listenerCocoa?.addedLots(selectedLot)
    }

    private fun callTheApi(filterVendorList: String) {
        binding.apply {
            vm.apply {
                getLotListForDWSInventory(
                    vendorSAPCode = filterVendorList,
                    key = getCurrentKey()
                )
            }
        }
    }

    private fun initAdapter() {
        binding.apply {
            vm.apply {
                /* adapterForRecycler = VegaGhanaGrnCocaListDWGAdapter(
                     ArrayList<VegaGRNDWLotManualModel>(),
                     this@VegaGhanaCocoaLotListDWGrn
                 ).apply {

                 }

                 rvLots.adapter = adapterForRecycler*/
            }
        }
    }


    private fun initExtra() {
        binding.apply {
            vm.apply {
                selectedLotIdModel = arguments?.getParcelable(MODEL_BUNDLE)
                selectedLotId = arguments?.getString(SELECTED_LOT_ID) ?: ""
            }
        }
    }

    private fun enableProceed(enabled: Boolean) {
        if (enabled)
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        else {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btnProceed.isEnabled = enabled
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun itemRemoved(item: VegaGhanaCocoaDispatchLots) {

    }

    override fun updateLotWeight(count: String, weight: String) {
        binding.apply {
            vm.apply {
                enableProceed(true)
            }
        }
    }

    private fun updateLotWeights(lData: VegaGRNDWLotManualModel) {
        vm.apply {
            enableProceed(true)
            selectedLotIdModel = lData
        }
    }



}
