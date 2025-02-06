package com.olam.warehouse.vegax.localsalescameroon.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaStorageLocation
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.localsalescameroon.R
import com.olam.warehouse.vegax.localsalescameroon.data.domain.model.VegaCameroonLocalSalesAssignLot
import com.olam.warehouse.vegax.localsalescameroon.data.domain.model.materialList
import com.olam.warehouse.vegax.localsalescameroon.databinding.FragmentVegaCameroonLocalSalesAssignLotBinding
import com.olam.warehouse.vegax.localsalescameroon.databinding.ItemCameroonLocalsalesAssignLotBinding
import com.olam.warehouse.vegax.localsalescameroon.utils.MATERIAL_LIST
import com.olam.warehouse.vegax.localsalescameroon.utils.SALE_ORDER_DATA
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCameroonLocalSalesAssignLotFragment : BaseFragment() {
    private var totalDisplayList = mutableListOf<VegaCameroonLocalSalesAssignLot>()
    private lateinit var list: ArrayList<VegaCameroonLocalSalesAssignLot>

    private var mergedLotIds: List<String?> = mutableListOf()
    override val layoutResourceId: Int
        get() = R.layout.fragment_vega_cameroon_local_sales_assign_lot

    private val vm: VegaCameroonSalesViewModel by viewModel()
    private var callback: VegaCameroonAsignLotCallback? = null
    private lateinit var binding: FragmentVegaCameroonLocalSalesAssignLotBinding
    private var salesOrder = VegaCoffeeSalesOrder()
    private var materialList = ArrayList<materialList>()
    private var selectedStorageLocation: String = ""
    private var isEmptySpace: Boolean = false
    private var isDuplicateLot: Boolean = false
    private var storageLocationList = mutableListOf<VegaStorageLocation>()

    companion object {
        fun newInstance(
            salesOrder: VegaCoffeeSalesOrder,
            materialList: ArrayList<materialList>
        ) =
            VegaCameroonLocalSalesAssignLotFragment().putArgs {
                putParcelable(SALE_ORDER_DATA, salesOrder)
                putParcelableArrayList(MATERIAL_LIST, materialList)
            }
    }

    interface VegaCameroonAsignLotCallback{
        fun updateAssignLotText(
            mergedLotIds: List<String?>,
            list: ArrayList<VegaCameroonLocalSalesAssignLot>,
            selectedStorageLocation: String
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as? VegaCameroonAsignLotCallback
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCameroonLocalSalesAssignLotBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("localsalescameroon/ui/VegaCameroonLocalSalesAssignLotFragment")
            .title("Vega_Cameroon/Local Sales").with(tracker)
        initExtra()
        initUI()
    }

    private fun initExtra() {
        salesOrder = arguments?.getParcelable<VegaCoffeeSalesOrder>(SALE_ORDER_DATA) ?: VegaCoffeeSalesOrder()
        materialList = arguments?.getParcelableArrayList<materialList>(MATERIAL_LIST) ?: ArrayList<materialList>()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btAssignLot, it, true)
        }
        if (getCurrentKey().split("_")[1].contains("NI")) {

            vm.storeLocation.observe(viewLifecycleOwner, Observer {
                storageLocationList = it.toMutableList()
                updateStorageLocations(storageLocationList)
            })
            vm.getStorageLocations()
        } else {
            vm.custonLocation.observe(viewLifecycleOwner, Observer {
                updateDefaultStorageLocationAdapter(it)
            })
            vm.getCustomLocations()
        }
        updateMergeListAdapter(salesOrder)
        binding.btAssignLot.setOnClickListener {
            isEmptySpace = false
            isDuplicateLot = false
            mergedLotIds = totalDisplayList.map { it.mergedLotId }.distinct()
            mergedLotIds.forEach {
                if (it != null) {
                    if (it.contains(" "))
                        isEmptySpace = true
                }
            }
            if(mergedLotIds.size != totalDisplayList.distinct().size)
                isDuplicateLot = true
            validateInputs()
        }
    }

    private fun validateInputs() {
        when {
            selectedStorageLocation.isNullOrEmpty()  -> showSnack(getString(R.string.msg_select_st_loc))
            mergedLotIds.contains("") ->  showSnack(getString(R.string.msg_enter_merge_id))
            isEmptySpace -> showSnack(getString(R.string.enter_id_not_spaces))
            isDuplicateLot -> showSnack(getString(R.string.msg_enter_no_duplicate))
            else -> {
                activity?.onBackPressed()
                callback?.updateAssignLotText(mergedLotIds,
                    totalDisplayList as ArrayList<VegaCameroonLocalSalesAssignLot>,selectedStorageLocation)
            }
        }
    }

    private fun updateDefaultStorageLocationAdapter(stLocationData: List<VegaCustomStLocation>) {
        var stLocationList = ArrayList<String>()
        stLocationList.add(getString(R.string.select_location))

        stLocationData.forEach {
            stLocationList.add(it.procureLocationCode.toString())
        }
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_cameroon_local_sales_grade, stLocationList)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spStLocation.adapter = stageAdapter
        var preSelectedStorageLocation = salesOrder.lotList[0].receivingStorageLocation
        if(!preSelectedStorageLocation.equals("")){
            binding.spStLocation.setSelection(stLocationList.indexOf(preSelectedStorageLocation))
            selectedStorageLocation = preSelectedStorageLocation.toString()
        }
        binding.spStLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (position > 0) {
                    selectedStorageLocation = stLocationList[position]
                }
                else {
                    selectedStorageLocation = ""
                }
            }
        }
    }

    private fun updateMergeListAdapter(data: VegaCoffeeSalesOrder) {

        list = ArrayList<VegaCameroonLocalSalesAssignLot>()
        data.lotList.forEach {lot ->
            list.add(VegaCameroonLocalSalesAssignLot(lot.materialName.toString(),lot.mergedLotId.toString()))
        }
        totalDisplayList = list.distinct().toMutableList()

        var filter = totalDisplayList.filter { it.mergedLotId == "" }
        if(filter.size != totalDisplayList.size){
            totalDisplayList.forEach {display ->
                if(display.mergedLotId != "" )
                {
                    filter.forEach {
                        if(display.materialName == it.materialName)
                        {
                            list.remove(display)
                        }
                    }
                }
            }
        }

        binding.rvLots.setUpAdapter(
            list.distinct().toMutableList(),
            R.layout.item_cameroon_localsales_assign_lot,
            ItemCameroonLocalsalesAssignLotBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvMaterialValue.text = item.materialName
                bindItem.etLotId.setText(item.mergedLotId)
                bindItem.etLotId.onChange {
                    item.mergedLotId = it
                    updateAdapter(it, item.materialName)
                }
            })
    }

    private fun updateAdapter(
        mergedLotId: String?,
        materialName: String?
    ) {
        list.forEach {
            if (it.materialName == materialName)
                it.mergedLotId = mergedLotId.toString()
        }

    }

    private fun updateStorageLocations(stLocationData: List<VegaStorageLocation>) {
        var stLocationList = ArrayList<String>()
        stLocationList.add(getString(R.string.select_location))

        stLocationData.forEach {
            stLocationList.add(it.storageLocationCode)
        }
        val stageAdapter =
            ArrayAdapter(
                requireContext(),
                R.layout.item_vega_cameroon_local_sales_grade,
                stLocationList
            )
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spStLocation.adapter = stageAdapter
        var preSelectedStorageLocation = salesOrder.lotList[0].receivingStorageLocation
        if (!preSelectedStorageLocation.equals("")) {
            binding.spStLocation.setSelection(stLocationList.indexOf(preSelectedStorageLocation))
            selectedStorageLocation = preSelectedStorageLocation.toString()
        }
        binding.spStLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (position > 0) {
                    selectedStorageLocation = stLocationList[position]
                } else {
                    selectedStorageLocation = ""
                }
            }
        }
    }

}
