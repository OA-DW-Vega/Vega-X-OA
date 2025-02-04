package com.olam.warehouse.vegax.localsalesnigeria.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.localsalesnigeria.R
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.model.VegaNigeriaLocalSalesAssignLot
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.model.materialList
import com.olam.warehouse.vegax.localsalesnigeria.databinding.FragmentVegaNigeriaLocalSalesAssignLotBinding
import com.olam.warehouse.vegax.localsalesnigeria.utils.MATERIAL_LIST
import com.olam.warehouse.vegax.localsalesnigeria.utils.SALE_ORDER_DATA
import kotlinx.android.synthetic.main.item_nigeria_localsales_assign_lot.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaLocalSalesAssignLotFragment : BaseFragment() {
    private var totalDisplayList = mutableListOf<VegaNigeriaLocalSalesAssignLot>()
    private lateinit var list: ArrayList<VegaNigeriaLocalSalesAssignLot>

    //    private lateinit var list: VegaCoffeeSalesOrder
    private var mergedLotIds: List<String?> = mutableListOf()
    override val layoutResourceId: Int
        get() = R.layout.fragment_vega_nigeria_local_sales_assign_lot

    private val vm: VegaNigeriaSalesViewModel by viewModel()
    private var callback: VegaNigeriaAsignLotCallback? = null
    private lateinit var binding: FragmentVegaNigeriaLocalSalesAssignLotBinding
    private var salesOrder = VegaCoffeeSalesOrder()
    private var materialList = ArrayList<materialList>()
    private var selectedStorageLocation: String = ""
    private var isEmptySpace: Boolean = false
    private var isDuplicateLot: Boolean = false

    companion object {
        fun newInstance(
            salesOrder: VegaCoffeeSalesOrder,
            materialList: ArrayList<materialList>
        ) =
            VegaNigeriaLocalSalesAssignLotFragment().putArgs {
                putParcelable(SALE_ORDER_DATA, salesOrder)
                putParcelableArrayList(MATERIAL_LIST, materialList)
            }
    }

    interface VegaNigeriaAsignLotCallback{
        fun updateAssignLotText(
            mergedLotIds: List<String?>,
            list: ArrayList<VegaNigeriaLocalSalesAssignLot>,
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
        binding = FragmentVegaNigeriaLocalSalesAssignLotBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("localsalesNigeria/ui/VegaNigeriaLocalSalesAssignLotFragment")
            .title("Vega_Nigeria/Local Sales").with(tracker)
        initExtra()
        initUI()
    }

    private fun initExtra() {
        salesOrder = arguments?.getParcelable<VegaCoffeeSalesOrder>(SALE_ORDER_DATA) ?: VegaCoffeeSalesOrder()
        materialList = arguments?.getParcelableArrayList<materialList>(MATERIAL_LIST) ?: ArrayList<materialList>()
    }

    private fun initUI() {
        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            updateDefaultStorageLocationAdapter(it)
        })
        vm.getCustomLocations()
        updateMergeListAdapter(salesOrder)
        binding.btAssignLot.setOnClickListener {
            isEmptySpace = false
            isDuplicateLot = false
            mergedLotIds = totalDisplayList.map { it.mergedLotId }.distinct()
            mergedLotIds.forEach {
                if (it != null) {
                    if(it.contains(" "))
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
            selectedStorageLocation.isNullOrEmpty()  -> showSnack("Select Storage Location to proceed")
            mergedLotIds.contains("") ->  showSnack("Enter Merged Lot id")
            isEmptySpace -> showSnack("Enter Merged Lot id without spaces")
            isDuplicateLot -> showSnack("Duplicate Merged Lot ids found")
            else -> {
                activity?.onBackPressed()
                callback?.updateAssignLotText(mergedLotIds,
                    totalDisplayList as ArrayList<VegaNigeriaLocalSalesAssignLot>,selectedStorageLocation)
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
            ArrayAdapter(requireContext(), R.layout.item_vega_nigeria_local_sales_grade, stLocationList)
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
//        if (data.size > 0) {
//            binding.rvLots.visible()
//        } else {
//            binding.rvLots.gone()
//        }
        list = ArrayList<VegaNigeriaLocalSalesAssignLot>()
        data.lotList.forEach {lot ->
            list.add(VegaNigeriaLocalSalesAssignLot(lot.materialName.toString(),lot.mergedLotId.toString()))
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

        binding.rvLots.setUp(
            list.distinct().toMutableList(),
            R.layout.item_nigeria_localsales_assign_lot,
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
                it.mergedLotId = mergedLotId.toString()
        }
//        list[pos].mergedLotId = value
//        this.list = list
//        binding.rvLots.adapter?.notifyDataSetChanged()
    }

}
