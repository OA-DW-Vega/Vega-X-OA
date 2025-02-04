package com.olam.warehouse.vegax.forwardponicaragua.ui

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPODetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.forwardponicaragua.R
import com.olam.warehouse.vegax.forwardponicaragua.databinding.FragmentVegaNicaraguaForwardPoTransactionDetailsBinding
import com.olam.warehouse.vegax.grnecuador.utils.FORWARD_PO_CREATION_YIELD_DETAILS_FRAG
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VegaNicaraguaForwardPOTransactionDetailsFragment : BaseFragment() {

    private var callBack: Callback? = null
    private lateinit var binding: FragmentVegaNicaraguaForwardPoTransactionDetailsBinding
    private var materialList = mutableListOf<VegaMaterial>()
    private var configmaterialList = mutableListOf<VegaMaterial>()
    private var gradeList = mutableListOf<VegaQualitative>()
    private var configgradeList = mutableListOf<VegaQualitative>()
    private var configgradeList1 = mutableListOf<VegaQualitative>()
    private var materialQualityGradeList = mutableListOf<VegaNicaraguaMaterialQualitGrades>()
    private val vm: VegaNicaraguaForwardPOViewModel by viewModel()
    private var receivingData = VegaNicaraguaForwardPODetails()
    private var vegaConfigDetails = mutableListOf<VegaConfigDetails>()

    companion object {
        fun newInstance(data: Bundle) =
            VegaNicaraguaForwardPOTransactionDetailsFragment().putArgs {
                putAll(data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? Callback
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_forward_po_transaction_details

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaForwardPoTransactionDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("FORWORDPO/ui/VegaNicaraguaForwordPOTransactionDetailsFragment")
            .title("FORWORD PO TRANSACTION DETAILS ")
            .with(tracker)
        initExtra()
        initUI()
    }

    private fun initExtra() {
        arguments?.let {
            receivingData = it.getParcelable(UIUtils.RECEIVING_DATA)!!
        }
    }

    private fun initUI() {
        getmaterialist()
        updateMandatory()
        materialList.clear()
        vm.product.observe(viewLifecycleOwner, Observer {
            val material = it.toMutableList()
            configmaterialList.forEach {it1->
                val list=material.filter { it.materialCode.equals(it1.materialCode) }
                    materialList.addAll(list)
                updateQualityMaterialInfo()
            }

        })
       /* vm.product.observe(viewLifecycleOwner, Observer {
            val material = it.toMutableList()
            material.forEach {

                if (!it.materialName!!.contains("PTBF") && !it.materialName!!.contains("Tolling"))
                    materialList.add(it)
                updateQualityMaterialInfo()
            }
        })*/
        vm.getProducts()

        vm.grade.observe(viewLifecycleOwner, Observer {
            configgradeList1.clear()

           // gradeList = it.toMutableList()
            val grade  = it.toMutableList()
            configgradeList.forEach {it1->
                val list=grade.filter { it1.charValue.equals(it.charValue.split(" ").get(it.charValue.split(" ").size - 1)) }
                configgradeList1.addAll(list)
            }
            vm.getMaterialQualityGrades(receivingData.materialCode.toString())
        })

        vm.materialQualityGrades.observe(viewLifecycleOwner, Observer {
            materialQualityGradeList = it.toMutableList()
            updateQualityGradeList()
        })

        binding.btnProceed.setOnClickListener {
            if(validateFields()) {

                   // DateUtils.getUTCDateTimeMillis(binding.tvDate.text.toString(), App.getAppContext())
                receivingData.unitsOfMeasure = "KG"
                var data = Bundle()
                data.putParcelable(UIUtils.RECEIVING_DATA, receivingData)

                callBack?.replaceFragment(
                    FORWARD_PO_CREATION_YIELD_DETAILS_FRAG, data
                )
            }
        }

        }
    fun  validateFields():Boolean
    {
        var flag = true
        if(binding.spMaterial.selectedItemPosition==0)
        {
            showSnack(getString(R.string.required_details_err))
            flag=false
        }
        else if(binding.spGrade.selectedItemPosition==0)
        {
            showSnack(getString(R.string.required_details_err))
            flag=false
        }

        return flag
    }

    fun updateQualityGradeList()
    {
        var gradeListFilter = mutableListOf<VegaQualitative>()

        materialQualityGradeList.forEach { qualityGrade ->
            gradeListFilter.addAll(configgradeList1.filter {
                it.charValue.split(" ")[1] == qualityGrade.gradeCode
            })
            /*gradeListFilter.addAll(gradeList.filter {
                it.charValue.split(" ").get(it.charValue.split(" ").size - 1) == qualityGrade.gradeCode
            })*/
        }
        val gradeListData = ArrayList<String>()
        gradeListData.add(getString(R.string.select_grade))

        gradeListData.addAll(gradeListFilter.map { it.charValue.plus("-").plus(it.descValue) } as ArrayList<String>)

        val locationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, gradeListData)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        var defaultposition: Int = 0
        if (!receivingData.grade.isNullOrEmpty()) {
            defaultposition = locationAdapter.getPosition(receivingData.grade + "-" + receivingData.qualityGradeDesc)
        }
        binding.spGrade.adapter = locationAdapter

        binding.spGrade.setSelection(defaultposition)

        binding.spGrade.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (position > 0) {
                    receivingData.grade = gradeListData[position].split("-")[0]
                    receivingData.qualityGradeDesc = gradeListData[position].split("-")[1]

                }
            }
        }

    }

    fun updateQualityMaterialInfo() {
        val material = ArrayList<VegaMaterial>()
        val item = VegaMaterial()
        item.materialName = getString(R.string.select_material)
        material.add(item)
        material.addAll(materialList)
        val materialData = material.distinctBy { it.materialName }.map { data -> data.materialName }
        val locationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, materialData)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        var defaultposition: Int = 0
        if (!receivingData.materialName.isNullOrEmpty() && !receivingData.materialName.isNullOrEmpty()) {
            defaultposition = locationAdapter.getPosition(receivingData.materialName)
        }
        binding.spMaterial.adapter = locationAdapter

        binding.spMaterial.setSelection(defaultposition)
        binding.spMaterial.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (position > 0) {
                    val selectedMaterial = materialList.filter { it.materialName.equals(materialData[position]) }.get(0)

                    receivingData.materialCode =
                        if (selectedMaterial.materialCode.length != 18) "000000".plus(selectedMaterial.materialCode) else selectedMaterial.materialCode
                    receivingData.materialName = selectedMaterial.materialName

                    receivingData.grade = ""
                    receivingData.certificate = ""
                    if (selectedMaterial.materialName!!.isNotEmpty())
                        getQualityGrades()
                }
            }
        }
    }
    private fun getQualityGrades() {

        vm.getGrades(receivingData.materialCode.toString())

    }

    private fun updateMandatory() {
        binding.tvQualityGrade.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.quality_grade)) { mandatoryStars() } }
        binding.tvMaterial.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.material)) { mandatoryStars() } }
    }
    private fun getmaterialist()
    {
        configgradeList.clear()
        configmaterialList.clear()
        val configMateriaList = Gson().fromJson<List<VegaConfigDetails>>(PreferenceHelper.get(Constants.PROCUREMENT, ""))
        if (!configMateriaList.isNullOrEmpty()) configMateriaList.forEach { plant -> vegaConfigDetails.add(plant) }
        val material = ArrayList<VegaMaterial>()
        var grades =listOf<String>()
        for (item1 in vegaConfigDetails) {
            val item = VegaMaterial()
            item.materialCode = item1.materialCode
            grades= item1.value!!.split(",")
            material.add(item)
        }
       for(gradeitem in grades)
       {
           val item = VegaQualitative()
           item.charValue= gradeitem.split(" ")[1].toString()
           configgradeList.add(item)
       }
        configmaterialList.addAll(material.toSet().toList())
    }


}
