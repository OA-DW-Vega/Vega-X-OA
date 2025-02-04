package com.olam.warehouse.vegax.mtntcocoa.ui.virtualmtnt

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentLot
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcocoa.R
import com.olam.warehouse.vegax.mtntcocoa.data.domain.model.VegaCocoaNWBagModel
import com.olam.warehouse.vegax.mtntcocoa.databinding.FragmentCocoaNoWeighmentAddWeightBinding
import com.olam.warehouse.vegax.mtntcocoa.ui.CallBack
import com.olam.warehouse.vegax.mtntcocoa.ui.VegaCocoaMtntViewModel
import com.olam.warehouse.vegax.mtntcocoa.utils.UPDATE_WEIGHT
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.random.Random

class VegaCocoaNoWeighmentAddWeightFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_cocoa_no_weighment_add_weight
    private lateinit var binding: FragmentCocoaNoWeighmentAddWeightBinding
    private val vm: VegaCocoaMtntViewModel by viewModel()
    private lateinit var callback: CallBack

    private var truckInWeight: Double = 0.0
    private var truckOutWeight: Double = 0.0
    private var bagWeight1: Double = 0.0
    private var bagWeight2: Double = 0.0
    private var bagWeight3: Double = 0.0

    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    var bagMaterial = VegaCocoaSweepingBagMaterial()
    var bagMaterialList = mutableListOf<VegaCocoaSweepingBagMaterial>()
    var bagTypeChosenList = mutableListOf<String>()
    private var defaultLot = VegaCocoaNoWeighmentLot()
    private var alreadyAddedBags = ArrayList<VegaCocoaSweepingBagMaterial>()
    private var isWeighscale: Boolean = false

    companion object {
        fun newInstance(item: VegaCocoaNoWeighmentLot) =
            VegaCocoaNoWeighmentAddWeightFragment().putArgs {
                putParcelable("MODEL_BUNDLE", item)
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("/mtntcocoa/ui/virtualmtnt/VegaCocoaNoWeighmentAddWeightFragment")
            .title("Dispatch Cocoa")
            .with(tracker)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCocoaNoWeighmentAddWeightBinding.inflate(inflater)
        initUI()
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as CallBack
    }

    private fun initUI() {
        defaultLot = arguments?.getParcelable<VegaCocoaNoWeighmentLot>("MODEL_BUNDLE") as VegaCocoaNoWeighmentLot
        binding.tvMaterial.text = getString(R.string.material).plus(" : ").plus(defaultLot.materialName)
        vm.packingMaterial.observe(viewLifecycleOwner, Observer {
            bagTypeList = it.toMutableList()
            vm.getBagItems(isWeighscale,defaultLot.batchNumber, defaultLot.weighBridgeId)
        })
        vm.getPackingMaterials()
        vm.bagItems.observe(viewLifecycleOwner, Observer {
            updateBagItems(it) })

        bagTypeChosenList.clear()

        binding.tvBagType1.setOnClickListener { showBagTypeDiaog(1, bagTypeList) }
        binding.tvBagType2.setOnClickListener { showBagTypeDiaog(2, bagTypeList) }
        binding.tvBagType3.setOnClickListener { showBagTypeDiaog(3, bagTypeList) }

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
                val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType1.text.toString()) }
                val bagcount = it.toInt()
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
                val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType2.text.toString()) }
                val bagcount = it.toInt()
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
                val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType3.text.toString()) }
                val bagcount = it.toInt()
                bagWeight3 = bagcount.times((data[0].tareWeight)!!.toDouble())
            }
            updateBagCount()
        }

        binding.btnProceed.setOnClickListener { validateInputs() }

    }

    private fun updateBagItems(bagMaterials: List<VegaCocoaSweepingBagMaterial>) {
        alreadyAddedBags.clear()
        alreadyAddedBags.addAll(bagMaterials)
        bagMaterials.forEachIndexed { index, bagMaterial ->
            bagTypeChosenList.add(bagMaterial.bagType)
            when (index) {
                0 -> {
                    binding.etTruckInWeight.setText(bagMaterial.grossWeight)
                    binding.etTruckOutWeight.setText(bagMaterial.truckOutWeight)

                    binding.tvBagType1.text = bagMaterial.bagType
                    binding.etBagCount1.setText(bagMaterial.bagCount)
                }
                1 -> {
                    binding.tvBagType2.text = bagMaterial.bagType
                    binding.etBagCount2.setText(bagMaterial.bagCount)
                }
                2 -> {
                    binding.tvBagType3.text = bagMaterial.bagType
                    binding.etBagCount3.setText(bagMaterial.bagCount)
                }
            }
        }
    }

    private fun updateBagCount() {
        /*val grossWeight =
            if (binding.etEnterWeight.text.toString().isEmpty()) 0.0
            else binding.etEnterWeight.text.toString()
                .toDouble()*/

        binding.etEnterWeight.text = truckOutWeight
            .minus(bagWeight1.plus(bagWeight2).plus(bagWeight3).plus(truckInWeight))
            .formatThreeDigits().replace(",", "")

    }

    private fun showBagTypeDiaog(bag: Int, it: List<VegaPackageMaterial>) {
        val bagTypes = it.map { data -> data.bagType }
        MaterialDialog(requireContext()).show {
            title(R.string.select_bag_type)
            listItemsSingleChoice(items = bagTypes) { _, index, text ->
                if (!BagTypeExist(text.toString())) {
                    bagTypeChosenList.add(text.toString())
                    when (bag) {
                        1 -> binding.tvBagType1.setText(text, TextView.BufferType.EDITABLE)
                        2 -> binding.tvBagType2.setText(text, TextView.BufferType.EDITABLE)
                        3 -> binding.tvBagType3.setText(text, TextView.BufferType.EDITABLE)
                    }
                }

            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.presentation.R.string.ok), true))
        }
    }

    private fun validateInputs() {
        //if (receivingData.weighBridgeType == PROCURE) {
        when {
            binding.etTruckInWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_truck_in_weight))
            binding.etTruckOutWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_truck_out_weight))
            else -> {
                bagMaterialList.clear()
                //receivingData.tareWeight = binding.etTruckTarWeight.text.toString()
                if (binding.tvBagType1.text.isNotEmpty() || binding.etBagCount1.text.isNotEmpty()) {
                    val receiving = bagMaterial.copy()
                    //bagMaterialList.clear()
                    val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType1.text.toString()) }

                    receiving.bagCount = binding.etBagCount1.text.toString()
                    receiving.bagType = data[0].bagType
                    receiving.unitsOfMeasure = data[0].unitsOfMeasure
                    receiving.tareWeight = data[0].tareWeight
                    receiving.bagMaterialCode = data[0].bagMaterialCode
                    receiving.createdPosition = 0
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
                    receiving1.createdPosition = 1
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
                    receiving2.createdPosition = 2
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
        updateBagWeight()
        val weight =
            if (binding.etEnterWeight.text.toString().isEmpty()) "0.0 KG" else binding.etEnterWeight.text.toString()
                .plus(" KG")
        val truckOutWeight =
            if (binding.etTruckOutWeight.text.toString().isEmpty()) "0.0" else binding.etTruckOutWeight.text.toString()
        callback.replaceFragment(
            UPDATE_WEIGHT,
            VegaCocoaNWBagModel(weight, truckOutWeight, true)
        )
    }

    private fun updateBagWeight() {
        val materialBagType = alreadyAddedBags.map { it.bagType }
        val positionMap = alreadyAddedBags.map { it.createdPosition }
        bagMaterialList.forEachIndexed { index, material ->
            if ((materialBagType.contains(material.bagType) || positionMap.contains(material.createdPosition))) {
                material.id =
                    alreadyAddedBags.filter { it.bagType == material.bagType || it.createdPosition == material.createdPosition }[0].id
            } else {
                material.id = Random.nextInt()
            }

            material.grossWeight = truckInWeight.formatThreeDigits()
            material.truckOutWeight = truckOutWeight.formatThreeDigits()
            material.batchNumber = defaultLot.batchNumber
            material.baseMaterial = defaultLot.materialCode
            material.createdPosition = index
            material.netWeight =
                (material.tareWeight?.toDouble()?.times(material.bagCount.toInt())!!).formatThreeDigits()
            material.unitsOfMeasure = if (material.unitsOfMeasure?.isEmpty()!!) "KG" else material.unitsOfMeasure
            material.message = activity!!.getString(R.string.stored_locally)
            material.weighBridgeId = defaultLot.weighBridgeId
            material.baseMaterial = defaultLot.materialCode
            vm.saveBagDetails(material)
        }

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

}
