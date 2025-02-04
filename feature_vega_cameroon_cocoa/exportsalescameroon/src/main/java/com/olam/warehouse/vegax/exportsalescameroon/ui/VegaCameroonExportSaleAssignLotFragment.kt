package com.olam.warehouse.vegax.exportsalescameroon.ui

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
import com.olam.warehouse.master.vegacoffee.model.ContainerWithLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.exportsalescameroon.R
import com.olam.warehouse.vegax.exportsalescameroon.data.domain.model.VegaCameroonExportSalesAssignLot
import com.olam.warehouse.vegax.exportsalescameroon.data.domain.model.materialList
import com.olam.warehouse.vegax.exportsalescameroon.databinding.FragmentVegaCameroonExportSaleAssignLotBinding
import com.olam.warehouse.vegax.exportsalescameroon.utils.CONTAINER_DATA
import com.olam.warehouse.vegax.exportsalescameroon.utils.MATERIAL_LIST
import kotlinx.android.synthetic.main.item_export_cameroon_assign_lot.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCameroonExportSaleAssignLotFragment : BaseFragment() {
    private var totalDisplayList: List<VegaCameroonExportSalesAssignLot> = mutableListOf()
    private lateinit var list: ArrayList<VegaCameroonExportSalesAssignLot>
    private var mergedLotIds: List<String?> = mutableListOf()
    override val layoutResourceId: Int
        get() = R.layout.fragment_vega_cameroon_export_sale_assign_lot

    private val vm: VegaCameroonExportSalesViewModel by viewModel()
    private var callback: VegaCameroonAsignLotCallback? = null
    private lateinit var binding: FragmentVegaCameroonExportSaleAssignLotBinding
    private var containerList = ArrayList<ContainerWithLots>()
    private var materialList = ArrayList<materialList>()
    private var selectedStorageLocation: String = ""
    private var isEmptySpace: Boolean = false
    private var isDuplicateLot: Boolean = false
    private var currentKey = getCurrentKey()

    companion object {
        fun newInstance(
            containerList: ArrayList<ContainerWithLots>,
            materialList: ArrayList<materialList>
        ) =
            VegaCameroonExportSaleAssignLotFragment().putArgs {
                putParcelableArrayList(CONTAINER_DATA, containerList)
                putParcelableArrayList(MATERIAL_LIST, materialList)
            }
    }

    interface VegaCameroonAsignLotCallback{
        fun updateAssignLotText(
            mergedLotIds: List<String?>,
            list: ArrayList<VegaCameroonExportSalesAssignLot>,
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
        binding = FragmentVegaCameroonExportSaleAssignLotBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("exportsalescameroon/ui/VegaCameroonExportSaleAssignLotFragment")
            .title("Vega_Cameroon/Export Sales")
            .with(tracker)
        initExtra()
        initUI()
    }

    private fun initExtra() {
        containerList = arguments?.getParcelableArrayList<ContainerWithLots>(CONTAINER_DATA) ?: ArrayList()
        materialList = arguments?.getParcelableArrayList<materialList>(MATERIAL_LIST) ?: ArrayList<materialList>()

    }

    private fun initUI() {

        if (getCurrentKey().split("_")[1].contains("NI")) {
            vm.storageLocation.observe(viewLifecycleOwner, Observer {
                updateStorageLocationAdapter(it)
            })
            vm.getStorageLocations()
        } else {
            vm.custonLocation.observe(viewLifecycleOwner, Observer {
                updateDefaultStorageLocationAdapter(it)
            })
            vm.getCustomLocations()
        }
        updateContainerAdapter(containerList)
        binding.btAssignLot.setOnClickListener {
            isEmptySpace = false
            isDuplicateLot = false
            mergedLotIds = list.map { it.mergedLotId }.distinct()
            mergedLotIds.forEach {
                if (it != null) {
                    if (it.contains(" "))
                        isEmptySpace = true
                }
            }
            if(mergedLotIds.size != list.distinct().size)
                isDuplicateLot = true
            validateInputs()
        }
    }

    private fun validateInputs() {
        when {
            selectedStorageLocation.isNullOrEmpty()  -> showSnack(getString(R.string.msg_select_st_loc))
            mergedLotIds.contains("") ->  showSnack(getString(R.string.msg_enter_merge_id))
            isEmptySpace -> showSnack(getString(R.string.msg_no_space))
            isDuplicateLot -> showSnack(getString(R.string.msg_duplicate_id))
            else -> {
                activity?.onBackPressed()
                callback?.updateAssignLotText(mergedLotIds,list,selectedStorageLocation)
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
            ArrayAdapter(requireContext(), R.layout.item_vega_cameroon_export_grade, stLocationList)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spStLocation.adapter = stageAdapter
        var preSelectedStorageLocation = containerList[0].lots[0].receivingStorageLocation
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

    private fun updateStorageLocationAdapter(stLocationData: List<VegaStorageLocation>) {
        var stLocationList = ArrayList<String>()
        if (currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains("COFF")) {
            containerList.forEach { lot ->
                lot.lots.forEach { lots ->
                    lots.storageLocationCode?.let {
                        stLocationList.add(it)
                        selectedStorageLocation = it
                    }
                }
            }
        } else {
            stLocationList.add(getString(R.string.select_location))
            stLocationData.forEach {
                stLocationList.add(it.storageLocationCode.toString())
            }
        }
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_cameroon_export_grade, stLocationList)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spStLocation.adapter = stageAdapter
        if (currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains("COFF")) {
            binding.spStLocation.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                    override fun onItemSelected(
                        p0: AdapterView<*>?,
                        p1: View?,
                        position: Int,
                        p3: Long
                    ) {
                        selectedStorageLocation = stLocationList[position]
                    }
                }
        } else {
            var preSelectedStorageLocation = containerList[0].lots[0].receivingStorageLocation
            if (!preSelectedStorageLocation.equals("")) {
                binding.spStLocation.setSelection(stLocationList.indexOf(preSelectedStorageLocation))
                selectedStorageLocation = preSelectedStorageLocation.toString()
            }
            binding.spStLocation.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                    override fun onItemSelected(
                        p0: AdapterView<*>?,
                        p1: View?,
                        position: Int,
                        p3: Long
                    ) {
                        if (position > 0) {
                            selectedStorageLocation = stLocationList[position]
                        } else {
                            selectedStorageLocation = ""
                        }
                    }
                }
        }
    }

    private fun updateContainerAdapter(data: ArrayList<ContainerWithLots>) {
        if (data.size > 0) {
            binding.rvLots.visible()
        } else {
            binding.rvLots.gone()
        }
        list = ArrayList<VegaCameroonExportSalesAssignLot>()
        data.forEach {lot ->
            lot.lots.forEach{
                list.add(VegaCameroonExportSalesAssignLot(it.containerNumber,it.materialName,it.mergedLotId))
            }
        }

        totalDisplayList = list.distinct()
        var filter = totalDisplayList.filter { it.mergedLotId == "" }
        if(filter.size != totalDisplayList.size){
            totalDisplayList.forEach {display ->
                if(display.mergedLotId != "" )
                {
                    filter.forEach {
                        if(display.containerNumber == it.containerNumber && display.materialName == it.materialName)
                        {
                            list.remove(display)
                        }
                    }
                }
            }
        }

        binding.rvLots.setUp(
            list.distinct().toMutableList(),
            R.layout.item_export_cameroon_assign_lot,
            { item, pos ->
                tvContainerValue.text = item.containerNumber
                tvMaterialValue.text = item.materialName
                if (currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains("COFF")) {
                    data.forEach { lot ->
                        lot.lots.forEach { lots ->
                            etLotId.setText(lots.batchNumber)
                            etLotId.isEnabled = false
                            item.mergedLotId = lots.batchNumber
                        }
                    }
                } else {
                    etLotId.setText(item.mergedLotId)
                    etLotId.onChange {
                        item.mergedLotId = it
                        updateAdapter(it, item.containerNumber, item.materialName)
                    }
                }
            })
    }

    private fun updateAdapter(
        mergedLotId: String?,
        containerNum: String?,
        materialName: String?
    ) {
        list.forEach {
            if (it.containerNumber == containerNum && it.materialName == materialName)
                it.mergedLotId = mergedLotId
        }

    }

}
