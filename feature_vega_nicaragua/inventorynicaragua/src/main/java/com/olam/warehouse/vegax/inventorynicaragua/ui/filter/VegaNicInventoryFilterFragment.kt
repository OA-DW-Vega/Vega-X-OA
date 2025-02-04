package com.olam.warehouse.vegax.inventorynicaragua.ui.filter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventorynicaragua.R
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.FilterList
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.NicStorageLoc
import com.olam.warehouse.vegax.inventorynicaragua.databinding.FragmentVegaNicInventoryFilterBinding
import kotlinx.android.synthetic.main.item_vega_nic_inventory_filter_layout.view.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 12/14/2020.
 */
class VegaNicInventoryFilterFragment : BaseFragment() {
    private var callBack: CallBack? = null
    private lateinit var binding: FragmentVegaNicInventoryFilterBinding
    override val layoutResourceId: Int = R.layout.fragment_vega_nic_inventory_filter
    private val filtermaterial = mutableListOf<FilterList>()
    private val filterQualityGrade = mutableListOf<FilterList>()
    private val filterCertification = mutableListOf<FilterList>()
    private val filterStLocation = mutableListOf<FilterList>()
    private var fullFilter = ArrayList<String>()
    private var materialList = arrayListOf<VegaMaterial>()
    /*private val filterOrigin = ArrayList<FilterList>()
    private val filterKOR = ArrayList<FilterList>()*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //binding.btApply.setOnClickListener { callBack?.filterList(ArrayList()) }
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("inventory/ui/filter/VegaCoffeeInventoryFilterFragment").title("Inventory").with(tracker)
        materialList = arguments?.getParcelableArrayList("Material_List") ?: ArrayList()
        makeFilterData()
    }

    interface CallBack {
        fun applyFilters(fragment: String, data: Any)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicInventoryFilterBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        binding.cbCertification.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                filterCertification.forEach { it.isSelected = true }
            } else {
                filterCertification.forEach { it.isSelected = false }
            }
            binding.rvCertification.adapter?.notifyDataSetChanged()
        }
        binding.cbQualityGrade.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                filterQualityGrade.forEach { it.isSelected = true }
            } else {
                filterQualityGrade.forEach { it.isSelected = false }
            }
            binding.rvQualityGrade.adapter?.notifyDataSetChanged()
        }
        binding.cbMaterial.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                filtermaterial.forEach { it.isSelected = true }
            } else {
                filtermaterial.forEach { it.isSelected = false }
            }
            binding.rvMaterial.adapter?.notifyDataSetChanged()
        }

        binding.cbStorageLoc.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                filterStLocation.forEach { it.isSelected = true }
            } else {
                filterStLocation.forEach { it.isSelected = false }
            }
            binding.rvStorageLoc.adapter?.notifyDataSetChanged()
        }
        binding.btFilter.setOnClickListener { finishAndFilter() }
    }

    private fun finishAndFilter() {
        val stLocFiltered = filterStLocation.filter { item -> item.isSelected }.map { it.value } as ArrayList
        val certificationFiltered = filterCertification.filter { item -> item.isSelected }.map { it.value } as ArrayList
        val qualityFiltered = filterQualityGrade.filter { item -> item.isSelected }.map { it.value } as ArrayList
        val materialFiltered = filtermaterial.filter { item -> item.isSelected }.map { it.value } as ArrayList
        fullFilter.clear()
        fullFilter.addAll(stLocFiltered)
        fullFilter.addAll(certificationFiltered)
        fullFilter.addAll(qualityFiltered)
        fullFilter.addAll(materialFiltered)

        val bundle = Bundle().apply {
            putStringArrayList("Full_Filter", fullFilter)
            putStringArrayList("Material", materialFiltered)
            putStringArrayList("Certification", certificationFiltered)
            putStringArrayList("QualityGrade", qualityFiltered)
            putStringArrayList("Storage", stLocFiltered)
        }
        activity?.onBackPressed()
        callBack?.applyFilters("filterList", bundle)
    }

    private fun setStLocationAdapter(data: MutableList<FilterList>) {
        binding.rvStorageLoc.setUp(data, R.layout.item_vega_nic_inventory_filter_layout, { item, pos ->
            tv_inventory_filter.text = getMaterialName(item.value)
            tv_inventory_filter.setOnClickListener {
                item.isSelected = !item.isSelected
                binding.rvStorageLoc.adapter?.notifyDataSetChanged()
            }
            ViewCompat.setBackground(
                    tv_inventory_filter,
                    ContextCompat.getDrawable(
                            main.context,
                            if (item.isSelected) com.olam.warehouse.presentation.R.drawable.item_selector_green else
                                com.olam.warehouse.presentation.R.drawable.item_deselected_white
                    )
            )

            tv_inventory_filter.setTextColor(
                    ContextCompat.getColor(
                            main.context,
                            if (item.isSelected) com.olam.warehouse.presentation.R.color.white else
                                com.olam.warehouse.presentation.R.color.black
                    )
            )
        }, { }, GridLayoutManager(context, 3))
    }

    private fun setCertificationAdapter(data: MutableList<FilterList>) {
        binding.rvCertification.setUp(data, R.layout.item_vega_nic_inventory_filter_layout, { item, pos ->
            tv_inventory_filter.text = getMaterialName(item.value)
            tv_inventory_filter.setOnClickListener {
                item.isSelected = !item.isSelected
                binding.rvCertification.adapter?.notifyDataSetChanged()
            }
            ViewCompat.setBackground(
                    tv_inventory_filter,
                    ContextCompat.getDrawable(
                            main.context,
                            if (item.isSelected) com.olam.warehouse.presentation.R.drawable.item_selector_green else
                                com.olam.warehouse.presentation.R.drawable.item_deselected_white
                    )
            )

            tv_inventory_filter.setTextColor(
                    ContextCompat.getColor(
                            main.context,
                            if (item.isSelected) com.olam.warehouse.presentation.R.color.white else
                                com.olam.warehouse.presentation.R.color.black
                    )
            )
        }, { }, GridLayoutManager(context, 3))
    }

    private fun setQualityGradeFilter(data: MutableList<FilterList>) {
        binding.rvQualityGrade.setUp(data, R.layout.item_vega_nic_inventory_filter_layout, { item, pos ->
            tv_inventory_filter.text = getMaterialName(item.value)
            tv_inventory_filter.setOnClickListener {
                item.isSelected = !item.isSelected
                binding.rvQualityGrade.adapter?.notifyDataSetChanged()
            }
            ViewCompat.setBackground(
                    tv_inventory_filter,
                    ContextCompat.getDrawable(
                            main.context,
                            if (item.isSelected) com.olam.warehouse.presentation.R.drawable.item_selector_green else
                                com.olam.warehouse.presentation.R.drawable.item_deselected_white
                    )
            )

            tv_inventory_filter.setTextColor(
                    ContextCompat.getColor(
                            main.context,
                            if (item.isSelected) com.olam.warehouse.presentation.R.color.white else
                                com.olam.warehouse.presentation.R.color.black
                    )
            )
        }, { }, GridLayoutManager(context, 3))
    }

    private fun setMaterialFilter(data: MutableList<FilterList>) {
        binding.rvMaterial.setUp(data, R.layout.item_vega_nic_inventory_filter_layout, { item, pos ->
            tv_inventory_filter.text = getMaterialName(item.value)
            tv_inventory_filter.setOnClickListener {
                item.isSelected = !item.isSelected
                binding.rvMaterial.adapter?.notifyDataSetChanged()
            }
            ViewCompat.setBackground(
                    tv_inventory_filter,
                    ContextCompat.getDrawable(
                            main.context,
                            if (item.isSelected) com.olam.warehouse.presentation.R.drawable.item_selector_green else
                                com.olam.warehouse.presentation.R.drawable.item_deselected_white
                    )
            )

            tv_inventory_filter.setTextColor(
                    ContextCompat.getColor(
                            main.context,
                            if (item.isSelected) com.olam.warehouse.presentation.R.color.white else
                                com.olam.warehouse.presentation.R.color.black
                    )
            )
        }, { }, GridLayoutManager(context, 3))
    }

    private fun getMaterialName(materialCode: String): String {
        val material = materialList.filter { ("000000".plus(it.materialCode)).contains(materialCode) }
        return if (material.size > 0) material[0].materialName.toString() else materialCode
    }

    private fun makeFilterData() {
        val fullFilter = arguments?.getStringArrayList("Selected_List") ?: ArrayList()
        val storageList = arguments?.getParcelableArrayList("Storage_List") ?: ArrayList<NicStorageLoc>()
        val isStroge = arguments?.getBoolean("IS_STORAGE") ?: false

        val material = arrayListOf<FilterList>()
        val qualityGrade = arrayListOf<FilterList>()
        val certification = arrayListOf<FilterList>()
        val storageLocation = arrayListOf<FilterList>()
        storageList.forEach { sto ->
            sto.procureLocationCode = sto.warehouseLocation.procureLocationCode
            sto.procureLocationName = sto.warehouseLocation.procureLocationName
            storageLocation.add(FilterList(sto.procureLocationCode, false))
            if (isStroge) return@forEach
            sto.inventory.forEach { inven ->
                if (!material.contains(
                        FilterList(
                            inven.materialCode,
                            false
                        )
                    )
                ) material.add(FilterList(inven.materialCode, false))
                if (!qualityGrade.contains(FilterList(inven.gradeDesc.toString(), false))) qualityGrade.add(
                    FilterList(
                        inven.gradeDesc.toString(),
                        false
                    )
                )
                if (!certification.contains(FilterList(inven.certification.toString(), false))) certification.add(
                    FilterList(inven.certification.toString(), false)
                )
            }

        }

        filtermaterial.clear()
        filterQualityGrade.clear()
        filterCertification.clear()
        filterStLocation.clear()
        if (!isStroge) {
            for (item in material.filter { !it.value.equals("null") }.distinct()) {
                if (fullFilter.size > 0 && fullFilter.contains(item.value)) filtermaterial.add(
                    FilterList(
                        item.value,
                        true
                    )
                )
                else filtermaterial.add(FilterList(item.value))
            }
            for (item in qualityGrade.filter { !it.value.equals("null") }.distinct()) {
                if (fullFilter.size > 0 && fullFilter.contains(item.value)) filterQualityGrade.add(
                    FilterList(
                        item.value,
                        true
                    )
                )
                else filterQualityGrade.add(FilterList(item.value))
            }

            for (item in certification.filter { !it.value.equals("null") }.distinct()) {
                if (fullFilter.size > 0 && fullFilter.contains(item.value)) filterCertification.add(
                    FilterList(
                        item.value,
                        true
                    )
                )
                else filterCertification.add(FilterList(item.value))
            }
        }
        for (item in storageLocation.filter { !it.value.equals("null") }.distinct()) {
            if (fullFilter.size > 0 && fullFilter.contains(item.value)) filterStLocation.add(FilterList(item.value, true))
            else filterStLocation.add(FilterList(item.value))
        }
        if (!filtermaterial.any { !it.isSelected }) binding.cbMaterial.isChecked = true
        if (!filterQualityGrade.any { !it.isSelected }) binding.cbQualityGrade.isChecked = true
        if (!filterCertification.any { !it.isSelected }) binding.cbCertification.isChecked = true
        if (!filterStLocation.any { !it.isSelected }) binding.cbStorageLoc.isChecked = true
        setStLocationAdapter(filterStLocation)
        setCertificationAdapter(filterCertification)
        setQualityGradeFilter(filterQualityGrade)
        setMaterialFilter(filtermaterial)
        hideItems(isStroge)
        certificationBlock(filterCertification.size > 0)
        materialBlock(filtermaterial.size > 0)
        qualityBlock(filterQualityGrade.size > 0)
    }

    private fun certificationBlock(isCerti: Boolean) {
        if (isCerti) binding.clCertification.visible() else binding.clCertification.gone()
    }

    private fun materialBlock(isMat: Boolean) {
        if (isMat) binding.clMaterial.visible() else binding.clMaterial.gone()
    }

    private fun qualityBlock(isqua: Boolean) {
        if (isqua) binding.clQualityGrade.visible() else binding.clQualityGrade.gone()
    }

    private fun hideItems(stroge: Boolean) {
        if (!stroge) {
            binding.clMaterial.visible()
            binding.clQualityGrade.visible()
            binding.clCertification.visible()
        } else {
            binding.clMaterial.gone()
            binding.clQualityGrade.gone()
            binding.clCertification.gone()
        }
    }
}

