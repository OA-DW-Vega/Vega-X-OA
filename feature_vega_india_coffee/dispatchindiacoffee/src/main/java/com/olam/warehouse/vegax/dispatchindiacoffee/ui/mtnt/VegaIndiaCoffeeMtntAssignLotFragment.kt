package com.olam.warehouse.vegax.dispatchindiacoffee.ui.mtnt

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.dispatchindiacoffee.R
import com.olam.warehouse.vegax.dispatchindiacoffee.data.domain.model.VegaIndiaCoffeeMtntAssignLot
import com.olam.warehouse.vegax.dispatchindiacoffee.data.domain.model.VegaIndiaCoffeeMtntMaterial
import com.olam.warehouse.vegax.dispatchindiacoffee.databinding.FragmentVegaIndiaCoffeeMtntAssignLotBinding
import com.olam.warehouse.vegax.dispatchindiacoffee.ui.VegaDispatchViewModel
import com.olam.warehouse.vegax.dispatchindiacoffee.utils.MATERIAL_LIST
import com.olam.warehouse.vegax.dispatchindiacoffee.utils.MTNT_LOT_DATA
import kotlinx.android.synthetic.main.item_india_coffee_mtnt_assign_lot.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaIndiaCoffeeMtntAssignLotFragment : BaseFragment() {
    private var totalDisplayList: List<VegaIndiaCoffeeMtntAssignLot> = mutableListOf()
    private lateinit var list: ArrayList<VegaIndiaCoffeeMtntAssignLot>
    private var mergedLotIds: List<String?> = mutableListOf()
    override val layoutResourceId: Int
        get() = R.layout.fragment_vega_india_coffee_mtnt_assign_lot

    private val vm: VegaDispatchViewModel by viewModel()
    private var callback: VegaIndiaCoffeeAsignLotCallback? = null
    private lateinit var binding: FragmentVegaIndiaCoffeeMtntAssignLotBinding
    private var dispatchLotList = ArrayList<VegaDispatchLots>()
    private var materialList = ArrayList<VegaIndiaCoffeeMtntMaterial>()
    private var selectedStorageLocation: String = ""
    private var isEmptySpace: Boolean = false
    private var isDuplicateLot: Boolean = false

    companion object {
        fun newInstance(
            dispatchlotList: ArrayList<VegaDispatchLots>,
            materialList: ArrayList<VegaIndiaCoffeeMtntMaterial>
        ) =
            VegaIndiaCoffeeMtntAssignLotFragment().putArgs {
                putParcelableArrayList(MTNT_LOT_DATA, dispatchlotList)
                putParcelableArrayList(MATERIAL_LIST, materialList)
            }
    }

    interface VegaIndiaCoffeeAsignLotCallback{
        fun updateAssignLotText(
            mergedLotIds: List<String?>,
            list: ArrayList<VegaIndiaCoffeeMtntAssignLot>,
            selectedStorageLocation: String,
            dispatchLotList: ArrayList<VegaDispatchLots>
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as? VegaIndiaCoffeeAsignLotCallback
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaIndiaCoffeeMtntAssignLotBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("exportsalescoffee/ui/VegaCoffeeExportAddLotFragment").title("Export Sales")
            .with(tracker)
        initExtra()
        initUI()
    }

    private fun initExtra() {
        dispatchLotList = arguments?.getParcelableArrayList<VegaDispatchLots>(MTNT_LOT_DATA) ?: ArrayList()
        materialList = arguments?.getParcelableArrayList<VegaIndiaCoffeeMtntMaterial>(MATERIAL_LIST) ?: ArrayList<VegaIndiaCoffeeMtntMaterial>()

    }

    private fun initUI() {
        vm.customLocation.observe(viewLifecycleOwner, Observer {
            updateDefaultStorageLocationAdapter(it)
        })
        vm.getCustomLocations()
        updateContainerAdapter(dispatchLotList)
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
                callback?.updateAssignLotText(mergedLotIds,list,selectedStorageLocation,dispatchLotList)
            }
        }
    }

    private fun updateDefaultStorageLocationAdapter(stLocationData: List<VegaCustomStLocation>) {
        val stLocationList = ArrayList<String>()
        stLocationList.add(getString(R.string.select_location))

        stLocationData.forEach {
            stLocationList.add(it.procureLocationCode.toString())
        }
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_india_coffee_mtnt_location, stLocationList)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spStLocation.adapter = stageAdapter
//        var preSelectedStorageLocation = dispatchLotList[0].lots[0].receivingStorageLocation
        val preSelectedStorageLocation = dispatchLotList[0].storageLocationCode
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

    private fun updateContainerAdapter(data: ArrayList<VegaDispatchLots>) {
        if (data.size > 0) {
            binding.rvLots.visible()
        } else {
            binding.rvLots.gone()
        }
        list = ArrayList<VegaIndiaCoffeeMtntAssignLot>()
        data.forEach { lot ->
            list.add(VegaIndiaCoffeeMtntAssignLot(lot.materialName,lot.mergedLotId))
        }

        totalDisplayList = list.distinct()
        val filter = totalDisplayList.filter { it.mergedLotId == "" }
        if(filter.size != totalDisplayList.size){
            totalDisplayList.forEach {display ->
                if(display.mergedLotId != "" )
                {
                    filter.forEach {
                        if( display.materialName == it.materialName)
                        {
                            list.remove(display)
                        }
                    }
                }
            }
        }

        binding.rvLots.setUp(
            list.distinct().toMutableList(),
            R.layout.item_india_coffee_mtnt_assign_lot,
            { item, pos ->
                tvMaterialValue.text = item.materialName
                etLotId.setText(item.mergedLotId)
                etLotId.onChange {
                    item.mergedLotId = it
                    updateAdapter(it,item.materialName)
                }
            })
    }

    private fun updateAdapter(
        mergedLotId: String?,
        materialName: String?
    ) {
        list.forEach {
            if (it.materialName == materialName)
                it.mergedLotId = mergedLotId
        }
//        list[pos].mergedLotId = value
//        this.list = list
//        binding.rvLots.adapter?.notifyDataSetChanged()
    }

}
