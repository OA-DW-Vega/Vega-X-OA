package com.olam.warehouse.vegax.offloadingcocoa.ui.mtnr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaOffloadingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiveLots
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcocoa.R
import com.olam.warehouse.vegax.offloadingcocoa.databinding.FragmentCocoaMtnrAddWeightBinding
import com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.random.Random

class VegaCoCoaMtnrAddWeightFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_cocoa_mtnr_add_weight
    private lateinit var binding: FragmentCocoaMtnrAddWeightBinding
    private val vm: VegaCoCoaOffloadingViewModel by viewModel()

    private var truckInWeight: Double = 0.0
    private var truckOutWeight: Double = 0.0
    private var bagWeight1: Double = 0.0
    private var bagWeight2: Double = 0.0
    private var bagWeight3: Double = 0.0
    private var netWeight: Double = 0.0

    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    var bagMaterial = VegaCoCoaOffloadingBagMaterial()
    var bagMaterialList = mutableListOf<VegaCoCoaOffloadingBagMaterial>()
    var bagMaterialListTemp = mutableListOf<VegaCoCoaOffloadingBagMaterial>()
    var bagTypeChosenList = mutableListOf<String>()
    private var defaultLot = VegaCoCoaReceiveLots()
    var defaultBagMaterialList = mutableListOf<VegaCoCoaOffloadingBagMaterial>()

    companion object {
        fun newInstance(data: Bundle) = VegaCoCoaMtnrAddWeightFragment().putArgs {
            putAll(data)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingcocoa/ui/mtnr/VegaCoCoaMtnrAddWeightFragment")
            .title("Mtnr CoCoa")
            .with(tracker)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCocoaMtnrAddWeightBinding.inflate(inflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        defaultLot =
            arguments?.getParcelable<VegaCoCoaReceiveLots>("MODEL_BUNDLE") as VegaCoCoaReceiveLots
        defaultBagMaterialList =
            arguments?.getParcelableArrayList<VegaCoCoaOffloadingBagMaterial>(UIUtils.BAGS_DATA)!!
                .toMutableList()
        binding.tvMaterial.text =
            getString(R.string.material).plus(" : ").plus(defaultLot.materialName!!)

        vm.material.observe(viewLifecycleOwner, Observer {
            bagTypeList = it.toMutableList()
            vm.getBagItems(defaultLot.batch, defaultLot.mtnNumber)
        })
        vm.getMaterials()
        vm.bagItems.observe(viewLifecycleOwner, Observer {
            if (it.size == 0) {
                if (defaultBagMaterialList.size > 0) {
                    updateDefaultBagItems(defaultBagMaterialList)
                }
            } else {
                updateBagItems(it)
            }
        })

        binding.tvBagType1.setOnClickListener { showBagTypeDiaog(1, bagTypeList) }
        binding.tvBagType2.setOnClickListener { showBagTypeDiaog(2, bagTypeList) }
        binding.tvBagType3.setOnClickListener { showBagTypeDiaog(3, bagTypeList) }

        binding.ivDeleteData1.setOnClickListener { deleteBagAtPosition(0) }
        binding.ivDeleteData2.setOnClickListener { deleteBagAtPosition(1) }
        binding.ivDeleteData3.setOnClickListener { deleteBagAtPosition(2) }

        binding.etTruckInWeight.onChange { truckin ->
            truckInWeight = if (truckin.isEmpty()) 0.0 else truckin.toDouble()
            updateBagCount()
        }

        binding.etTruckOutWeight.onChange { truckout ->
            truckOutWeight = if (truckout.isEmpty()) 0.0 else truckout.toDouble()
            updateBagCount()
        }

        binding.etBagCount1.onChange {
            if (binding.tvBagType1.text.toString().isEmpty()) {
                showSnack(getString(R.string.select_bag_type))
                bagWeight1 = 0.0
            } else if (it.isEmpty()) {
                bagWeight1 = 0.0
            } else {
                val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType1.text.toString().trim()) }
                val bagcount = it.trim().toInt()
                bagWeight1 = bagcount.times((data[0].tareWeight)!!.toDouble())
            }
            updateBagCount()
        }

        binding.etBagCount2.onChange {
            if (binding.tvBagType2.text.toString().isEmpty()) {
                showSnack(getString(R.string.select_bag_type))
                bagWeight2 = 0.0
            } else if (it.isEmpty()) {
                bagWeight1 = 0.0
            } else {
                val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType2.text.toString().trim()) }
                val bagcount = it.trim().toInt()
                bagWeight2 = bagcount.times((data[0].tareWeight)!!.toDouble())
            }
            updateBagCount()
        }

        binding.etBagCount3.onChange {
            if (binding.tvBagType3.text.toString().isEmpty()) {
                showSnack(getString(R.string.select_bag_type))
                bagWeight3 = 0.0
            } else if (it.isEmpty()) {
                bagWeight1 = 0.0
            } else {
                val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType3.text.toString().trim()) }
                val bagcount = it.trim().toInt()
                bagWeight3 = bagcount.times((data[0].tareWeight)!!.toDouble())
            }
            updateBagCount()
        }

        binding.btnProceed.setOnClickListener { validateInputs() }

    }

    private fun updateDefaultBagItems(bagMaterials: List<VegaCoCoaOffloadingBagMaterial>) {
        bagMaterials.forEachIndexed { index, vegaCoCoaOffloadingBagMaterial ->
            bagTypeChosenList.add(vegaCoCoaOffloadingBagMaterial.bagType)
            when (index) {
                0 -> {
                    if(vegaCoCoaOffloadingBagMaterial.grossWeight.toLong()>0) {
                        binding.etTruckInWeight.setText(vegaCoCoaOffloadingBagMaterial.grossWeight)
                    }
                    if(vegaCoCoaOffloadingBagMaterial.truckOutWeight.toLong()>0) {
                        binding.etTruckOutWeight.setText(vegaCoCoaOffloadingBagMaterial.truckOutWeight)
                    }

                    binding.tvBagType1.text = vegaCoCoaOffloadingBagMaterial.bagType.trim()
                    binding.etBagCount1.setText(vegaCoCoaOffloadingBagMaterial.bagCount.trim())
                    binding.ivDeleteData1.visibility = View.VISIBLE

                }
                1 -> {
                    binding.tvBagType2.text = vegaCoCoaOffloadingBagMaterial.bagType.trim()
                    binding.etBagCount2.setText(vegaCoCoaOffloadingBagMaterial.bagCount.trim())
                    binding.ivDeleteData2.visibility = View.VISIBLE

                }
                2 -> {
                    binding.tvBagType3.text = vegaCoCoaOffloadingBagMaterial.bagType.trim()
                    binding.etBagCount3.setText(vegaCoCoaOffloadingBagMaterial.bagCount.trim())
                    binding.ivDeleteData3.visibility = View.VISIBLE

                }
            }
        }
    }

    private fun updateBagItems(bagMaterials: List<VegaCoCoaOffloadingBagMaterial>) {
        bagMaterialList = bagMaterials.toMutableList()
        bagMaterialListTemp = bagMaterials.toMutableList()
        bagMaterials.forEachIndexed { index, vegaCoCoaOffloadingBagMaterial ->
            bagTypeChosenList.add(vegaCoCoaOffloadingBagMaterial.bagType)
            when (index) {
                0 -> {
                    if(vegaCoCoaOffloadingBagMaterial.grossWeight.toLong()>0) {
                        binding.etTruckInWeight.setText(vegaCoCoaOffloadingBagMaterial.grossWeight)
                    }
                    if(vegaCoCoaOffloadingBagMaterial.truckOutWeight.toLong()>0) {
                        binding.etTruckOutWeight.setText(vegaCoCoaOffloadingBagMaterial.truckOutWeight)
                    }

                    binding.tvBagType1.text = vegaCoCoaOffloadingBagMaterial.bagType.trim()
                    binding.etBagCount1.setText(vegaCoCoaOffloadingBagMaterial.bagCount.trim())
                    binding.ivDeleteData1.visibility = View.VISIBLE

                }
                1 -> {
                    binding.tvBagType2.text = vegaCoCoaOffloadingBagMaterial.bagType.trim()
                    binding.etBagCount2.setText(vegaCoCoaOffloadingBagMaterial.bagCount.trim())
                    binding.ivDeleteData2.visibility = View.VISIBLE

                }
                2 -> {
                    binding.tvBagType3.text = vegaCoCoaOffloadingBagMaterial.bagType.trim()
                    binding.etBagCount3.setText(vegaCoCoaOffloadingBagMaterial.bagCount.trim())
                    binding.ivDeleteData3.visibility = View.VISIBLE

                }
            }
        }
    }

    private fun updateBagCount() {
        /*val grossWeight =
            if (binding.etEnterWeight.text.toString().isEmpty()) 0.0
            else binding.etEnterWeight.text.toString()
                .toDouble()*/

        netWeight = truckInWeight.minus(bagWeight1.plus(bagWeight2).plus(bagWeight3).plus(truckOutWeight))
        binding.etEnterWeight.text = netWeight.formatThreeDigits().replace(",", "")

    }

    private fun showBagTypeDiaog(bag: Int, it: List<VegaPackageMaterial>) {
        val bagTypes = it.map { data -> data.bagType }
        MaterialDialog(requireContext()).show {
            title(R.string.select_bag_type)
            listItemsSingleChoice(items = bagTypes) { _, index, text ->
                if (!BagTypeExist(text.toString())) {
                    bagTypeChosenList.add(text.toString())
                    when (bag) {
                        1 -> {
                            binding.tvBagType1.setText(text, TextView.BufferType.EDITABLE)
                            binding.etBagCount1.setText("")
                            binding.ivDeleteData1.visibility = View.VISIBLE
                        }
                        2 -> {
                            binding.tvBagType2.setText(text, TextView.BufferType.EDITABLE)
                            binding.etBagCount2.setText("")
                            binding.ivDeleteData2.visibility = View.VISIBLE
                        }
                        3 -> {
                            binding.tvBagType3.setText(text, TextView.BufferType.EDITABLE)
                            binding.etBagCount3.setText("")
                            binding.ivDeleteData3.visibility = View.VISIBLE
                        }

                    }
                }

            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun validateInputs() {
        //if (receivingData.weighBridgeType == PROCURE) {
        when {
            binding.etTruckInWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_truck_in_weight))
            binding.etTruckOutWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_truck_out_weight))
            netWeight < 0 -> showSnack(getString(R.string.net_weight_error))

            else -> {
                //receivingData.tareWeight = binding.etTruckTarWeight.text.toString()
                if (binding.tvBagType1.text.isNotEmpty() || binding.etBagCount1.text.isNotEmpty()) {
                    val receiving = bagMaterial.copy()
                    bagMaterialList.clear()
                    val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType1.text.toString()) }

                    receiving.bagCount = binding.etBagCount1.text.toString()
                    receiving.bagType = data[0].bagType
                    receiving.unitsOfMeasure = data[0].unitsOfMeasure
                    receiving.tareWeight = data[0].tareWeight
                    receiving.bagMaterialCode = data[0].bagMaterialCode

                    bagMaterialList.add(receiving)
                }
                if (binding.tvBagType2.text.isNotEmpty() || binding.etBagCount2.text.isNotEmpty()) {
                    val receiving1 = bagMaterial.copy()
                    val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType2.text.toString()) }

                    receiving1.bagCount = binding.etBagCount2.text.toString()
                    receiving1.bagType = data[0].bagType
                    receiving1.unitsOfMeasure = data[0].unitsOfMeasure
                    receiving1.tareWeight = data[0].tareWeight
                    receiving1.bagMaterialCode = data[0].bagMaterialCode

                    bagMaterialList.add(receiving1)
                }
                if (binding.tvBagType3.text.isNotEmpty() || binding.etBagCount3.text.isNotEmpty()) {
                    val receiving2 = bagMaterial.copy()
                    val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType3.text.toString()) }

                    receiving2.bagCount = binding.etBagCount3.text.toString()
                    receiving2.bagType = data[0].bagType
                    receiving2.unitsOfMeasure = data[0].unitsOfMeasure
                    receiving2.tareWeight = data[0].tareWeight
                    receiving2.bagMaterialCode = data[0].bagMaterialCode

                    bagMaterialList.add(receiving2)
                }
                var isEmptyData = false
                bagMaterialList.forEach {
                    if (it.bagType.isEmpty() || it.bagCount.isEmpty() || it.bagCount.equals("0")) isEmptyData = true
                }
                when {
                    isEmptyData || bagMaterialList.size == 0 -> showSnack(getString(R.string.error_valid_bag_count_type))
                    else -> {
                        moveBackToConsignment()
                    }
                }
            }
        }
    }

    private fun moveBackToConsignment() {
        for (i in bagMaterialListTemp)
            vm.deleteBagDetailsById(i.id.toString())
        updateBagWeight()
    }

    fun updateBagWeight() {
        bagMaterialList.forEachIndexed { index, material ->

            material.id = Random.nextInt()
            material.grossWeight = truckInWeight.formatThreeDigits()
            material.truckOutWeight = truckOutWeight.formatThreeDigits()
            material.batchNumber = defaultLot.batch

            /*val tar = (material.tareWeight?.toDouble()?.times(material.bagCount.toInt())!!)
            material.netWeight = tar?.let { material.grossWeight.toDouble().minus(it).toString() }.toString()*/

            material.createdPosition = index
            material.netWeight = netWeight.toString()
            /*(material.tareWeight?.toDouble()?.times(material.bagCount.toInt())!!).formatThreeDigits()*/
            material.unitsOfMeasure = if (material.unitsOfMeasure?.isEmpty()!!) "KG" else material.unitsOfMeasure
            material.message = activity!!.getString(R.string.stored_locally)
            material.mtnNumber = defaultLot.mtnNumber
            material.batchNumber = defaultLot.batch
            //printBag(material)
            vm.saveBagDetails(material)
            if (index == bagMaterialList.size - 1)
                activity?.onBackPressed()
        }
        //vm.saveMtnrReceivingLots(vm.vegaCoCoaReceivingData, defaultLot)
    }

    fun BagTypeExist(bagType: String): Boolean {
        var isExistValue = false
        bagTypeChosenList.forEachIndexed { index, it ->
            if (it == bagType) {
                isExistValue = true
            }
        }
        if (isExistValue) {
            showSnack(getString(R.string.bag_type_already_exist))
        }
        return isExistValue
    }

    fun deleteBagAtPosition(pos: Int) {
        if (bagMaterialList.size > pos) {
            //vm.deleteBagDetails(bagMaterialList[pos].createdPosition.toString())
            bagMaterialList.removeAt(pos)
        }
        bagTypeChosenList.removeAt(pos)

        clearFields(pos)
    }

    fun clearFields(pos: Int) {
        when (pos) {
            0 -> {
                binding.tvBagType1.text = ""
                binding.etBagCount1.setText("")
                binding.ivDeleteData1.visibility = View.INVISIBLE
            }
            1 -> {
                binding.tvBagType2.text = ""
                binding.etBagCount2.setText("")
                binding.ivDeleteData2.visibility = View.INVISIBLE
            }
            2 -> {
                binding.tvBagType3.text = ""
                binding.etBagCount3.setText("")
                binding.ivDeleteData3.visibility = View.INVISIBLE
            }
        }
    }
}
