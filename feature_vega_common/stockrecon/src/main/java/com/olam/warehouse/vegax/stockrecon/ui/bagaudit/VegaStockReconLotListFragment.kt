package com.olam.warehouse.vegax.stockrecon.ui.bagaudit

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.model.VegaStockReconGetAllAuditData
import com.olam.warehouse.master.vega.model.VegaStockReconIdDetails
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.stockrecon.R
import com.olam.warehouse.vegax.stockrecon.databinding.FragmentStockReconAuditLotListBinding
import com.olam.warehouse.vegax.stockrecon.databinding.ItemStockReconLotListDetailBinding
import com.olam.warehouse.vegax.stockrecon.ui.callback.VegaStockReconAddLotsListener
import com.olam.warehouse.vegax.stockrecon.utils.AUDIT_DETAILS
import com.olam.warehouse.vegax.stockrecon.utils.BUNDLE_DATA
import com.olam.warehouse.vegax.stockrecon.utils.MATERIAL_LIST
import com.olam.warehouse.vegax.stockrecon.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.stockrecon.utils.RECON_ID_DETAILS
import com.olam.warehouse.vegax.stockrecon.utils.getColor
import com.olam.warehouse.vegax.stockrecon.utils.listOfField
import com.olam.warehouse.vegax.stockrecon.utils.prepareLotsList
import com.olam.warehouse.vegax.stockrecon.vm.VegaStockReconViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaStockReconLotListFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_stock_recon_audit_lot_list
    private lateinit var binding: FragmentStockReconAuditLotListBinding
    private var listener: VegaStockReconAddLotsListener? = null
    private var alreadySelected = mutableListOf<VegaDispatchLots>()
    private var materialList = arrayListOf<String>()
    private var model: VegaEcuadorDispatch? = null
    private val vm: VegaStockReconViewModel by viewModel()
    private var dispatchLotsList = mutableListOf<VegaDispatchLots>()
    private var wareHouseList = mutableListOf<String>()
    private var filteredDispatchLotsList = mutableListOf<VegaDispatchLots>()
    private val mSearchList = mutableListOf<VegaDispatchLots>()
    var auditList = mutableListOf<VegaStockReconGetAllAuditData>()
    var reconIdDetails = VegaStockReconIdDetails()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as VegaStockReconAddLotsListener
    }

    companion object {
        fun newInstance(model: VegaEcuadorDispatch, materialList: ArrayList<String>, bundle: Bundle) =
            VegaStockReconLotListFragment().putArgs {
                putStringArrayList(MATERIAL_LIST, materialList)
                putParcelable(MODEL_BUNDLE, model)
                putBundle(BUNDLE_DATA, bundle)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentStockReconAuditLotListBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        clickListener()
        return binding.root
    }


    private fun clickListener() {
        binding.btnProceed.setOnClickListener { sendSelectedLots() }
    }

    private fun initUI() {
        if (AppUtils.isOnline()) reconIdDetails.plant?.let { vm.fetchStocks(materialList, it) }
        vm.stocks.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }


    private fun initExtra() {
        val bundle = arguments?.getBundle(BUNDLE_DATA)
//        model = arguments?.getParcelable(MODEL_BUNDLE)
        alreadySelected.addAll((model?.lotList ?: mutableListOf()) as Collection<VegaDispatchLots>)
        materialList = arguments?.getStringArrayList(MATERIAL_LIST) as ArrayList<String>
        auditList = bundle?.getParcelableArrayList<VegaStockReconGetAllAuditData>(AUDIT_DETAILS) ?: ArrayList()
        reconIdDetails =
            bundle?.getParcelable<VegaStockReconIdDetails>(RECON_ID_DETAILS) as VegaStockReconIdDetails
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()
                            val filteredDispatchStocksList = mutableListOf<VegaEcuadorDispatchStocks>()
                            var dataValue = if (reconIdDetails.storageLocation.isNotEmpty()) {
                                it.data?.data?.filter { it.storageLocationCode.equals(reconIdDetails.storageLocation) }
                            } else {
                                it.data?.data
                            }
                            dataValue.let { item -> item?.let { it1 -> filteredDispatchStocksList.addAll(it1) } }
                            dispatchLotsList.addAll(prepareLotsList(filteredDispatchStocksList))
                            updateSelectLotValues()
                            getWarehouseList()
                        }

                        else -> showErrorDialogWithFAQLink(requireContext(), it.data?.message.toString())
                        //UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
                    hideLoading()
                }

                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    //UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun getWarehouseList() {
        if (dispatchLotsList.isNotEmpty()) {
            wareHouseList.add("All")
            wareHouseList.addAll(dispatchLotsList.listOfField(VegaDispatchLots::storageLocationCode).toSet())
            updateWareHouseSpinner()
        }
    }

    private fun updateWareHouseSpinner() {
        val stageAdapter = ArrayAdapter(requireContext(), R.layout.item_stock_recon_wh, wareHouseList)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spWareHouse.adapter = stageAdapter
        val defaultposition = 0
        binding.spWareHouse.setSelection(defaultposition)
        binding.spWareHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filteredDispatchLotsList.clear()
                if (position > 0) {
                    val storageLocation = wareHouseList[position]
                    val lotsList = dispatchLotsList.filter { it.storageLocationCode == storageLocation }
                    filteredDispatchLotsList.addAll(lotsList)
                    setupAdapter(filteredDispatchLotsList)
                } else {
                    setupAdapter(dispatchLotsList)
                    filteredDispatchLotsList.clear()
                }
            }
        }
    }

    private fun updateSelectLotValues() {
        val filter = mutableListOf<VegaDispatchLots>()
        filter.addAll(dispatchLotsList)
        if (alreadySelected.isNotEmpty()) {
            filter.forEachIndexed { index, s ->
                alreadySelected.forEach { item ->
                    if (s.batchNumber.equals(item.batchNumber) && s.materialCode.equals(item.materialCode) && s.storageLocationCode.equals(
                            item.storageLocationCode
                        )
                    )
                        dispatchLotsList[index].isAdded = true
                }
            }
        }
        setupAdapter(dispatchLotsList)
    }

    private fun setupAdapter(data: MutableList<VegaDispatchLots>) {
        if (data.size > 0) {
            binding.rvLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
        }

        binding.rvLots.setUpAdapter(
            data, R.layout.item_stock_recon_lot_list_detail,
            ItemStockReconLotListDetailBinding::inflate, { it, pos, bindingItem ->
                bindingItem.tvLotId.text = it.batchNumber
                bindingItem.tvGradeValue.text = it.materialName
                bindingItem.tvStLocationValue.text = it.storageLocationCode
                bindingItem.tvBagTypeValue.text = it.bagType
                bindingItem.tvWeightValue.text =
                    it.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitOfMeasure)
                bindingItem.ivSelect.isChecked = it.isAdded ?: false
                bindingItem.llLotItem.setOnClickListener { view ->
                    it.isAdded = !it.isAdded!!
                    dispatchLotsList.forEach { item ->
                        if (item.batchNumber.equals(it.batchNumber) && item.materialCode.equals(it.materialCode) && (item.storageLocationCode?.equals(
                                it.storageLocationCode
                            ) == true)
                        ) item.isAdded =
                            it.isAdded
                    }
                    binding.rvLots.adapter?.notifyItemChanged(pos)
                    //updateWeight()
                }


            }, itemClick = {

            })
    }

    private fun sendSelectedLots() {
        val data = dispatchLotsList.filter { it.isAdded == true } as ArrayList
        if (data.isEmpty()) {
            showSnack(getString(R.string.kindly_select_lot))
            return
        }
        /*For stock recon, only one lot have to select*/
        if (data.size > 1) {
            showSnack(getString(R.string.more_lot_warning_msg))
            return
        }
        /*the below condition is to restrict, the same lot should not be audit again in same recon */
        if (auditList.filter { it.lotNumber == data.get(0).batchNumber }.any()) {
            showSnack(getString(R.string.already_lot_selected_warning_msg))
            return
        }
        /*we should not allow the lot without bag details,
        because without bag details we cannot do audit*/
        if (data.isNotEmpty() && data.get(0).bagType?.isEmpty() == true) {
            showSnack(getString(R.string.bag_details_not_available_msg))
            return
        }
        /*if user select lot from another plant, we are restricting it*/
        if (!reconIdDetails.plant.equals(data.get(0).plantId)) {
            showSnack(getString(R.string.plant_not_matching_warning))
            return
        }
        listener?.addedLots(data)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint = "Search by lots"
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setupAdapter(if (filteredDispatchLotsList.isEmpty()) dispatchLotsList else filteredDispatchLotsList)
                        } else {
                            mSearchList.clear()
                            (if (filteredDispatchLotsList.isEmpty()) dispatchLotsList else filteredDispatchLotsList).forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.batchNumber.contains(text)) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            setupAdapter(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }
}
