package com.olam.warehouse.vegax.exportsalesnigeria.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vegacoffee.model.ContainerWithLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.exportsalesnigeria.R
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.model.VegaNigeriaExportSalesAssignLot
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.model.materialList
import com.olam.warehouse.vegax.exportsalesnigeria.databinding.FragmentVegaNigeriaExportSaleAssignLotBinding
import com.olam.warehouse.vegax.exportsalesnigeria.utils.CONTAINER_DATA
import com.olam.warehouse.vegax.exportsalesnigeria.utils.MATERIAL_LIST
import kotlinx.android.synthetic.main.item_export_nigeria_assign_lot.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaExportSaleAssignLotFragment : BaseFragment() {
    private var totalDisplayList: List<VegaNigeriaExportSalesAssignLot> = mutableListOf()
    private lateinit var list: ArrayList<VegaNigeriaExportSalesAssignLot>
    private var mergedLotIds: List<String?> = mutableListOf()
    override val layoutResourceId: Int
        get() = R.layout.fragment_vega_nigeria_export_sale_assign_lot

    private val vm: VegaNigeriaExportSalesViewModel by viewModel()
    private var callback: VegaNigeriaAsignLotCallback? = null
    private lateinit var binding: FragmentVegaNigeriaExportSaleAssignLotBinding
    private var containerList = ArrayList<ContainerWithLots>()
    private var materialList = ArrayList<materialList>()
    private var selectedStorageLocation: String = ""
    private var isEmptySpace: Boolean = false
    private var isDuplicateLot: Boolean = false

    companion object {
        fun newInstance(
            containerList: ArrayList<ContainerWithLots>,
            materialList: ArrayList<materialList>
        ) =
            VegaNigeriaExportSaleAssignLotFragment().putArgs {
                putParcelableArrayList(CONTAINER_DATA, containerList)
                putParcelableArrayList(MATERIAL_LIST, materialList)
            }
    }

    interface VegaNigeriaAsignLotCallback{
        fun updateAssignLotText(
            mergedLotIds: List<String?>,
            list: ArrayList<VegaNigeriaExportSalesAssignLot>,
            selectedStorageLocation: String
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as? VegaNigeriaAsignLotCallback
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNigeriaExportSaleAssignLotBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("exportsalesNigeria/ui/VegaNigeriaExportSaleAssignLotFragment").title("Vega_Nigeria/Export Sales")
            .with(tracker)
        initExtra()
        initUI()
    }

    private fun initExtra() {
        containerList = arguments?.getParcelableArrayList<ContainerWithLots>(CONTAINER_DATA) ?: ArrayList()
        materialList = arguments?.getParcelableArrayList<materialList>(MATERIAL_LIST) ?: ArrayList<materialList>()

    }

    private fun initUI() {
        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            updateDefaultStorageLocationAdapter(it)
        })
        vm.getCustomLocations()
        updateContainerAdapter(containerList)
        binding.btAssignLot.setOnClickListener {
            isEmptySpace = false
            isDuplicateLot = false
            mergedLotIds = list.map { it.mergedLotId }.distinct()
            mergedLotIds.forEach {
                if (it != null) {
                    if(it.contains(" "))
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
            selectedStorageLocation.isNullOrEmpty()  -> showSnack("Select Storage Location to proceed")
            mergedLotIds.contains("") ->  showSnack("Enter Merged Lot id")
            isEmptySpace -> showSnack("Enter Merged Lot id without spaces")
            isDuplicateLot -> showSnack("Duplicate Merged Lot ids found")
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
            ArrayAdapter(requireContext(), R.layout.item_vega_nigeria_export_grade, stLocationList)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spStLocation.adapter = stageAdapter
        var preSelectedStorageLocation = containerList[0].lots[0].receivingStorageLocation
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

    private fun updateContainerAdapter(data: ArrayList<ContainerWithLots>) {
        if (data.size > 0) {
            binding.rvLots.visible()
        } else {
            binding.rvLots.gone()
        }
        list = ArrayList<VegaNigeriaExportSalesAssignLot>()
        data.forEach {lot ->
            lot.lots.forEach{
                list.add(VegaNigeriaExportSalesAssignLot(it.containerNumber,it.materialName,it.mergedLotId))
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
            R.layout.item_export_nigeria_assign_lot,
            { item, pos ->
                tvContainerValue.text = item.containerNumber
                tvMaterialValue.text = item.materialName
                etLotId.setText(item.mergedLotId)
                etLotId.onChange {
                    item.mergedLotId = it
                    updateAdapter(it,item.containerNumber,item.materialName)
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
//        list[pos].mergedLotId = value
//        this.list = list
//        binding.rvLots.adapter?.notifyDataSetChanged()
    }

}
