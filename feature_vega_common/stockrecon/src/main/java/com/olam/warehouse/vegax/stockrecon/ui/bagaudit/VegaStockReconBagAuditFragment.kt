package com.olam.warehouse.vegax.stockrecon.ui.bagaudit

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.model.VegaStockReconBagDetails
import com.olam.warehouse.master.vega.model.VegaStockReconIdDetails
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.stockrecon.R
import com.olam.warehouse.vegax.stockrecon.databinding.FragmentVegaStockReconBagAuditBinding
import com.olam.warehouse.vegax.stockrecon.ui.callback.VegaStockCallbackListener
import com.olam.warehouse.vegax.stockrecon.utils.BUNDLE_DATA
import com.olam.warehouse.vegax.stockrecon.utils.DAMAGED_BAGS
import com.olam.warehouse.vegax.stockrecon.utils.PLANT_DETAILS
import com.olam.warehouse.vegax.stockrecon.utils.RECON_ID_DETAILS
import com.olam.warehouse.vegax.stockrecon.utils.SELECTED_LOTS
import com.olam.warehouse.vegax.stockrecon.utils.SPILLAGE
import com.olam.warehouse.vegax.stockrecon.utils.STOCK_RECON_SUMMARY
import com.olam.warehouse.vegax.stockrecon.vm.VegaStockReconViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.IOException

class VegaStockReconBagAuditFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_stock_recon_bag_audit
    private lateinit var binding: FragmentVegaStockReconBagAuditBinding
    private var callBack: VegaStockCallbackListener? = null
    private var selectedLots: VegaDispatchLots = VegaDispatchLots()
    private val RECORD_REQUEST_CODE = 101
    var imageFilePath: String = ""
    val CAMERA_REQUEST_CODE = 0
    var reconIdDetails = VegaStockReconIdDetails()
    var bagTypeCount = 0
    var bagTypes = mutableListOf<String>()
    private val vm: VegaStockReconViewModel by viewModel()
    var packingMaterial: List<VegaPackageMaterial> = emptyList()
    private var stockAuditWeight = 0.0
    var weightLossGainDetails = "0.0"
    private var plant = Plant()


    companion object {
        fun newInstance(bundle: Bundle) = VegaStockReconBagAuditFragment().putArgs {
            putParcelable(BUNDLE_DATA, bundle)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaStockCallbackListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaStockReconBagAuditBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        getArgumentData()
        getBagTypeCount()
        observer()
        setUIValues()
        clickListener()
    }

    private fun observer() {
        /*fetching packing materials from db, for calculating bag weights*/
        vm.getPackingMaterials()
        vm.packingMaterial.observe(viewLifecycleOwner, Observer {
            packingMaterial = it
            updateWtLossValue()
        })
    }


    private fun getBagTypeCount() {
        /*if this lot contains multiple bag, you need to use comma separator,
        other wise single bag type added into bagtype array*/
        if (selectedLots.bagType?.isNotEmpty() == true) {
            /*if lot contains multiple bag, we have spilit by using "," seperator & set bagtypes and bagcount*/
            /*Note: using comma only we are finding whether the lot contain single or multiple bag*/
            if (selectedLots?.bagType?.contains(",") == true) {
                bagTypes = selectedLots?.bagType?.split(",") as ArrayList<String>
                bagTypeCount = selectedLots.bagType?.split(",")?.size ?: 0
            } else {
                /*if lot contains single bag, then directly set the bagcount and bag types values*/
                bagTypeCount = 1
                bagTypes.add(selectedLots.bagType ?: "")
            }
        }
    }

    private fun clickListener() {
        binding.tvBagCountHeader.setOnClickListener {
            /*depends on visibility of clbagcount, we are expanding and collapsing the bagcount header*/
            if (binding.clBagCount1.isVisible) {
                enableBagCountView(false)
            } else {
                enableBagCountView(true)
            }
        }
        binding.tvBagDetailsHeader.setOnClickListener {
            /*depends on visibility of clBagDetails, we are expanding and collapsing the tvBagDetailsHeader header*/
            if (binding.clBagDetails.isVisible) {
                enableBagDetailsView(false)
            } else {
                enableBagDetailsView(true)
            }
        }
        binding.tvWeightLossHeader.setOnClickListener {
            /*depends on visibility of clWeightLoss, we are expanding and collapsing the tvWeightLossHeader header*/
            if (binding.clWeightLoss.isVisible) {
                enableWeightLossView(false)
            } else {
                enableWeightLossView(true)
            }
        }
        binding.ivCamera.setOnClickListener { setupPermissions() }
        binding.tvBagDamagedValue.setOnClickListener { showYesNoOptionDialog(DAMAGED_BAGS) }
        binding.tvSpillageValue.setOnClickListener { showYesNoOptionDialog(SPILLAGE) }
        binding.etHalfBag1Count.onChange {
            /*by adding the count here, we have to calculate and update other views*/
            if (it.isNotEmpty()) {
                updateWeightDetails(true)
                updateWtLossValue()
            }
        }
        binding.etHalfBag2Count.onChange {
            /*by adding the count here, we have to calculate and update other views*/
            if (it.isNotEmpty()) {
                updateWeightDetails(true)
                updateWtLossValue()
            }
        }
        binding.etHalfBag3Count.onChange {
            /*by adding the count here, we have to calculate and update other views*/
            if (it.isNotEmpty()) {
                updateWeightDetails(true)
                updateWtLossValue()
            }
        }
        binding.etFullBag1Count.onChange {
            /*by adding the count here, we have to calculate and update other views*/
            if (it.isNotEmpty()) {
                updateWeightDetails(false)
                updateWtLossValue()
            }
        }
        binding.etFullBag2Count.onChange {
            /*by adding the count here, we have to calculate and update other views*/
            if (it.isNotEmpty()) {
                updateWeightDetails(false)
                updateWtLossValue()
            }
        }
        binding.etFullBag3Count.onChange {
            /*by adding the count here, we have to calculate and update other views*/
            if (it.isNotEmpty()) {
                updateWeightDetails(false)
                updateWtLossValue()
            }
        }
        binding.btnProceed.setOnClickListener { validateToProceed() }
    }

    private fun enableBagCountView(status: Boolean) {
        if (status) {
            /*depends on the bagcount, we are showing the halfBag, fullBag view */
            binding.clBagCount1.visible()
            if (bagTypes.isNotEmpty()) {
                binding.tvBagType1.setText(bagTypes.get(0))
            }
            if (bagTypeCount == 2) {
                binding.clBagCount2.visible()
                if (bagTypes.isNotEmpty()) {
                    binding.tvBagType2.setText(bagTypes.get(1))
                }
            } else if (bagTypeCount == 3) {
                binding.clBagCount3.visible()
                if (bagTypes.isNotEmpty()) {
                    binding.tvBagType3.setText(bagTypes.get(2))
                }
            }
        } else {
            binding.clBagCount1.gone()
            binding.clBagCount2.gone()
            binding.clBagCount3.gone()
        }
    }

    private fun enableBagDetailsView(status: Boolean) {
        if (status) {
            binding.clBagDetails.visible()
        } else {
            binding.clBagDetails.gone()
        }
    }

    private fun enableWeightLossView(status: Boolean) {
        if (status) {
            binding.clWeightLoss.visible()
        } else {
            binding.clWeightLoss.gone()
        }
    }

    private fun getArgumentData() {
        val bundle = arguments?.getBundle(BUNDLE_DATA)
        plant = bundle?.getParcelable<Plant>(PLANT_DETAILS) as Plant
        reconIdDetails =
            bundle?.getParcelable<VegaStockReconIdDetails>(RECON_ID_DETAILS) as VegaStockReconIdDetails
        selectedLots =
            bundle?.getParcelable<VegaDispatchLots>(SELECTED_LOTS) as VegaDispatchLots
    }

    private fun setUIValues() {
        binding.tvMaterialValue.setText(selectedLots.materialName)
        binding.tvSystemNetWtValue.setText(selectedLots.weight.plus(selectedLots.unitOfMeasure))
        binding.tvSystemWtValue.setText(selectedLots.weight.plus(selectedLots.unitOfMeasure))
        binding.tvNoOfBagsValue.setText(selectedLots.totalNoOfBags)
        binding.tvBagTypeValue.setText(selectedLots.bagType)
        binding.tvLotValue.setText(getString(R.string.colon_lot_no).plus(selectedLots.batchNumber))
        binding.tvDamagedBagCount.isEnabled = false
        binding.tvSpillageValue.isEnabled = false
    }

    private fun updateWtLossValue() {
        weightLossGainDetails = calculateWeightLoss().formatThreeDigits()
        /*after calculating the weight loss/gain value, updating the UI*/
        binding.tvFinalWeightLossValue.setText(
            "Final Weight Gain/Loss : ".plus(
                weightLossGainDetails.toString().plus(selectedLots.unitOfMeasure)
            )
        )
        binding.tvWeightLossValue.setText(
            weightLossGainDetails.toString().plus(selectedLots.unitOfMeasure)
        )
    }

    private fun updateWeightDetails(isHalfBag: Boolean) {
        if (isHalfBag) {
            /*calculating and updating the all half bag count value, in case if we have multiple bag count*/
            var halfBagCountValue =
                (binding.etHalfBag1Count.text.toString().toInt() + binding.etHalfBag2Count.text.toString()
                    .toInt() + binding.etHalfBag3Count.text.toString().toInt()).toString()
            binding.tvWLHalfBagCountValue.setText(halfBagCountValue)
        } else {
            /*calculating and updating the all full bag count value, in case if we have multiple bag count*/
            var fullBagCountValue =
                (binding.etFullBag1Count.text.toString().toInt() + binding.etFullBag2Count.text.toString()
                    .toInt() + binding.etFullBag3Count.text.toString().toInt()).toString()
            binding.tvWLfullBagCountValue.setText(fullBagCountValue)
        }
    }

    private fun showYesNoOptionDialog(view: String) {
        var list = ArrayList<String>()
        list.add("Yes")
        list.add("No")
        MaterialDialog(requireContext()).show {
            title(R.string.select_option)
            listItemsSingleChoice(items = list) { _, index, text ->
                when (view) {
                    DAMAGED_BAGS -> {
                        binding.tvBagDamagedValue.setText(text.toString())
                        if (text.toString().equals("Yes", true)) {
                            /*if damaged bag is yes, then only you should enable spillage and damaged bag count value*/
                            binding.tvDamagedBagCount.isEnabled = true
                            binding.tvSpillageValue.isEnabled = true
                            binding.tvDamagedBagCountLabel.text =
                                with(UIUtils) { with(requireContext().resources.getString(R.string.damaged_bags)) { mandatoryStars() } }

                        } else {
                            binding.tvDamagedBagCount.isEnabled = false
                            binding.tvSpillageValue.isEnabled = false
                            /*if value added, and then user select damaged as NO,
                            then you should empty the below text*/
                            binding.tvSpillageValue.setText("")
                            binding.tvDamagedBagCount.setText("")
                            binding.tvDamagedBagCountLabel.text = getString(R.string.damaged_bags)
                        }
                    }

                    SPILLAGE -> {
                        binding.tvSpillageValue.setText(text.toString())
                    }
                }
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.login.R.string.ok), true))
        }
    }

    private fun setupPermissions() {
        val permission =
            activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) }
        if (permission != PackageManager.PERMISSION_GRANTED) makeRequest() else moveToCameraView()
    }

    private fun makeRequest() {
        requestPermissions(
            arrayOf(Manifest.permission.CAMERA),
            RECORD_REQUEST_CODE
        )
    }

    private fun moveToCameraView() {
        try {
            val imageFile = createImageFile()
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (activity?.packageManager?.let { callCameraIntent.resolveActivity(it) } != null) {
                val authorities = requireActivity().packageName + ".fileprovider"
                val imageUri =
                    activity?.let { FileProvider.getUriForFile(it, authorities, imageFile) }
                callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
            }
        } catch (e: IOException) {
            UIUtils.showErrorDialog(
                requireContext(),
                requireContext().resources.getString(R.string.could_not_create_file)
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    updateCameraLayout()
                } else {
                    imageFilePath = ""
                }
            }

            else -> {
                UIUtils.showErrorDialog(
                    requireContext(),
                    requireContext().resources.getString(R.string.unrecognized_request_code)
                )
            }
        }
    }

    fun updateCameraLayout() {
        binding.ivCamera.text = selectedLots.batchNumber.plus(".jpg")
        ViewCompat.setBackgroundTintList(
            binding.ivCamera,
            ContextCompat.getColorStateList(
                activity?.applicationContext!!,
                com.olam.warehouse.presentation.R.color.blue_light
            )
        )
        ViewCompat.setBackgroundTintList(
            binding.ivCamera,
            ContextCompat.getColorStateList(
                activity?.applicationContext!!,
                com.olam.warehouse.presentation.R.color.blue_light
            )
        )
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        /*after capturing image from camera, converting that into byte*/
        val imageFileName: String = "JPEG_".plus(binding.tvLotValue.text)
        val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!storageDir!!.exists()) storageDir.mkdirs()
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        imageFilePath = imageFile.absolutePath
        return imageFile
    }

    private fun validateToProceed() {
        /*if bag damaged is yes, then damaged bag count should not be empty or zero*/
        if (binding.tvBagDamagedValue.text.toString().trim().equals("yes", true)
            && (binding.tvDamagedBagCount.text.toString().trim().isEmpty()
                    || binding.tvDamagedBagCount.text.toString().trim().equals("0"))
        ) {
            showSnack(getString(R.string.empty_damaged_bag_count_msg))
            return
        }

        /*if bag damaged is yes, then spillage should not be empty or NO*/
        if (binding.tvBagDamagedValue.text.toString().trim().equals("yes", true)
            && (binding.tvSpillageValue.text.toString().trim().isEmpty()
                    || binding.tvSpillageValue.text.toString().trim().equals("No", true))
        ) {
            showSnack(getString(R.string.select_spillage_warning_msg))
            return
        }

        /*the damaged bag count should not be more than sum of(half + full bag count)*/
        if ((binding.tvDamagedBagCount.text.toString().trim()
                .isNotEmpty()) && ((binding.tvWLfullBagCountValue.text.trim().toString()
                .toInt() + binding.tvWLHalfBagCountValue.text.trim()
                .toString().toInt()) < binding.tvDamagedBagCount.text.toString().trim().toInt())
        ) {
            showSnack(getString(R.string.damaged_bag_count_more_warning_msg))
            return
        }

        moveToSummary()
    }

    private fun moveToSummary() {
        callBack?.replaceFragment(STOCK_RECON_SUMMARY, prepareBagAuditDetails())
    }

    /*collect all user details, to proceed to summary & post data*/
    private fun prepareBagAuditDetails(): VegaStockReconBagDetails {
        var bagAuditDetails = VegaStockReconBagDetails()
        bagAuditDetails.selectedLots = selectedLots
        bagAuditDetails.reconIdDetails = reconIdDetails
        /*depends on the bagCount, assign bag type value*/
        if (bagTypeCount == 1) {
            bagAuditDetails.bagType1 = bagTypes.get(0)
        } else if (bagTypeCount == 2) {
            bagAuditDetails.bagType1 = bagTypes.get(0)
            bagAuditDetails.bagType2 = bagTypes.get(1)
        } else if (bagTypeCount == 3) {
            bagAuditDetails.bagType1 = bagTypes.get(0)
            bagAuditDetails.bagType2 = bagTypes.get(1)
            bagAuditDetails.bagType3 = bagTypes.get(2)
        }
        bagAuditDetails.fullBagCount1 = binding.etFullBag1Count.text.toString().trim()
        bagAuditDetails.halfBagCount1 = binding.etHalfBag1Count.text.toString().trim()
        bagAuditDetails.fullBagCount2 = binding.etFullBag2Count.text.toString().trim()
        bagAuditDetails.halfBagCount2 = binding.etHalfBag2Count.text.toString().trim()
        bagAuditDetails.fullBagCount3 = binding.etFullBag3Count.text.toString().trim()
        bagAuditDetails.halfBagCount3 = binding.etHalfBag3Count.text.toString().trim()
        bagAuditDetails.totalFullBackCount = binding.tvWLfullBagCountValue.text.toString().trim()
        bagAuditDetails.totalHalfBackCount = binding.tvWLHalfBagCountValue.text.toString().trim()
        bagAuditDetails.bagWeight1 = bagType1Wt.toString()
        bagAuditDetails.bagWeight2 = bagType2Wt.toString()
        bagAuditDetails.bagWeight3 = bagType3Wt.toString()
        bagAuditDetails.bagDamaged = binding.tvBagDamagedValue.text.toString().trim()
        bagAuditDetails.damagedBagCount = binding.tvDamagedBagCount.text.toString().trim()
        bagAuditDetails.spillage = binding.tvSpillageValue.text.toString().trim()
        bagAuditDetails.remarks = binding.etRemarks.text.toString().trim()
        bagAuditDetails.imageString = imageFilePath
        bagAuditDetails.weightLoss = weightLossGainDetails.toString()
        bagAuditDetails.stockAuditWeight = stockAuditWeight.toString()
        bagAuditDetails.plant = plant
        return bagAuditDetails
    }

    /*weight loss, calculation global variable*/
    var bagType1Wt = 0.0
    var bagType2Wt = 0.0
    var bagType3Wt = 0.0
    var bagTypeWeightDetails1: VegaPackageMaterial = VegaPackageMaterial()
    var bagTypeWeightDetails2: VegaPackageMaterial = VegaPackageMaterial()
    var bagTypeWeightDetails3: VegaPackageMaterial = VegaPackageMaterial()
    private fun calculateWeightLoss(): Double {
        var totalWeightLoss = 0.0
        /*calculating uom wt, if lot contains MT and tare weight bag contains kg,
        you have to do conversion, so that'y we are taking uom wt*/
        var uomWT = if (selectedLots.unitOfMeasure?.contains(
                "MT",
                true
            ) == true
        ) 1000 else if (selectedLots.unitOfMeasure?.contains("KG", true) == true) 1 else 1

        /*if bagTypeCount(lot) is 1, we have to take the bagtype 1 bag weight*/
        if (bagTypeCount >= 1) {
            bagTypeWeightDetails1 =
                packingMaterial.singleOrNull() { it.bagType.equals(bagTypes.get(0), true) } ?: VegaPackageMaterial()
            bagType1Wt =
                (if (bagTypeWeightDetails1.standardWeight?.isNotEmpty() == true) bagTypeWeightDetails1.standardWeight?.trim()
                    .toString().toDouble() else 0.0)
        }
        /*if bagTypeCount(lot) is 2, we have to take the bagtype 1&2 bag weight*/
        if (bagTypeCount >= 2) {
            bagTypeWeightDetails2 =
                packingMaterial.singleOrNull() { it.bagType.equals(bagTypes.get(1), true) } ?: VegaPackageMaterial()
            bagType2Wt =
                (if (bagTypeWeightDetails2.standardWeight?.isNotEmpty() == true) bagTypeWeightDetails2.standardWeight?.trim()
                    .toString().toDouble() else 0.0)
        }
        /*if bagTypeCount(lot) is 3, we have to take the bagtype 1,2&3 bag weight*/
        if (bagTypeCount >= 3) {
            bagTypeWeightDetails3 =
                packingMaterial.singleOrNull() { it.bagType.equals(bagTypes.get(2), true) } ?: VegaPackageMaterial()
            bagType3Wt =
                (if (bagTypeWeightDetails3.standardWeight?.isNotEmpty() == true) bagTypeWeightDetails3.standardWeight?.trim()
                    .toString().toDouble() else 0.0)

        }
        if (bagTypes.isNotEmpty()) {
            if (bagTypes.size == 1) {
                var halfBagCount1 = 0
                var fullBagCount1 = 0
                /*take half bag count*/
                if (binding.etHalfBag1Count.text?.isNotEmpty() == true) {
                    halfBagCount1 = binding.etHalfBag1Count.text?.toString()?.toInt() ?: 0
                }
                /*take full bag count*/
                if (binding.etFullBag1Count.text?.isNotEmpty() == true) {
                    fullBagCount1 = binding.etFullBag1Count.text?.toString()?.toInt() ?: 0
                }
                var bagTareWeight1 =
                    if (bagTypeWeightDetails1.tareWeight?.isNotEmpty() == true) bagTypeWeightDetails1?.tareWeight?.toDouble() else 0.0
                /*for calculating whole tare weight, just multiply the bag tare weight and half+full bag count*/
                var tareWeight1 = (halfBagCount1 + fullBagCount1) * bagTareWeight1!!
                /*for calculating total weight bagwt * full bag count, for half bag we have to divide /2 and minus the tare weight*/
                var totalWeight1 = ((bagType1Wt * fullBagCount1) + (bagType1Wt / 2 * halfBagCount1)) - tareWeight1
                /*for stock audit weight, totalweight divide by uomwt*/
                stockAuditWeight = totalWeight1 / uomWT
                /*for total weight  loss, we have to minus lot weight and stock audit wt*/
                totalWeightLoss =
                    (selectedLots.weight?.toDouble()?.let { (totalWeight1 / uomWT)?.minus(it) } ?: 0) as Double
//                totalWeightLoss = (selectedLots.weight?.toDouble()?.minus(totalWeight1) ?: 0) as Double
            } else if (bagTypes.size == 2) {
                /*same logic as bagsize 1, but here you have calculate for bag 2 also*/
                var halfBagCount1 = 0
                var fullBagCount1 = 0
                var halfBagCount2 = 0
                var fullBagCount2 = 0
                if (binding.etHalfBag1Count.text?.isNotEmpty() == true) {
                    halfBagCount1 = binding.etHalfBag1Count.text?.toString()?.toInt() ?: 0
                }
                if (binding.etFullBag1Count.text?.isNotEmpty() == true) {
                    fullBagCount1 = binding.etFullBag1Count.text?.toString()?.toInt() ?: 0
                }
                var bagTareWeight1 =
                    if (bagTypeWeightDetails1.tareWeight?.isNotEmpty() == true) bagTypeWeightDetails1?.tareWeight?.toDouble() else 0.0
                var tareWeight1 = (halfBagCount1 + fullBagCount1) * bagTareWeight1!!
                var totalWeight1 = ((bagType1Wt * fullBagCount1) + (bagType1Wt / 2 * halfBagCount1)) - tareWeight1

                if (binding.etHalfBag2Count.text?.isNotEmpty() == true) {
                    halfBagCount2 = binding.etHalfBag2Count.text?.toString()?.toInt() ?: 0
                }
                if (binding.etFullBag2Count.text?.isNotEmpty() == true) {
                    fullBagCount2 = binding.etFullBag2Count.text?.toString()?.toInt() ?: 0
                }

                var bagTareWeight2 =
                    if (bagTypeWeightDetails2.tareWeight?.isNotEmpty() == true) bagTypeWeightDetails2?.tareWeight?.toDouble() else 0.0
                var tareWeight2 = (halfBagCount2 + fullBagCount2) * bagTareWeight2!!
                var totalWeight2 = ((bagType2Wt * fullBagCount2) + (bagType2Wt / 2 * halfBagCount2)) - tareWeight2
                stockAuditWeight = (totalWeight1 + totalWeight2) / uomWT
                totalWeightLoss =
                    (selectedLots.weight?.toDouble()?.let { ((totalWeight1 + totalWeight2) / uomWT)?.minus(it) }
                        ?: 0) as Double
            } else if (bagTypes.size == 3) {
                /*same logic as bagsize 1, but here you have calculate for bag 2&3 also*/
                var halfBagCount1 = 0
                var fullBagCount1 = 0
                var halfBagCount2 = 0
                var fullBagCount2 = 0
                var halfBagCount3 = 0
                var fullBagCount3 = 0
                if (binding.etHalfBag1Count.text?.isNotEmpty() == true) {
                    halfBagCount1 = binding.etHalfBag1Count.text?.toString()?.toInt() ?: 0
                }
                if (binding.etFullBag1Count.text?.isNotEmpty() == true) {
                    fullBagCount1 = binding.etFullBag1Count.text?.toString()?.toInt() ?: 0
                }

                var bagTareWeight1 =
                    if (bagTypeWeightDetails1.tareWeight?.isNotEmpty() == true) bagTypeWeightDetails1?.tareWeight?.toDouble() else 0.0
                var tareWeight1 = (halfBagCount1 + fullBagCount1) * bagTareWeight1!!
                var totalWeight1 = ((bagType1Wt * fullBagCount1) + (bagType1Wt / 2 * halfBagCount1)) - tareWeight1

                if (binding.etHalfBag2Count.text?.isNotEmpty() == true) {
                    halfBagCount2 = binding.etHalfBag2Count.text?.toString()?.toInt() ?: 0
                }
                if (binding.etFullBag2Count.text?.isNotEmpty() == true) {
                    fullBagCount2 = binding.etFullBag2Count.text?.toString()?.toInt() ?: 0
                }

                var bagTareWeight2 =
                    if (bagTypeWeightDetails2.tareWeight?.isNotEmpty() == true) bagTypeWeightDetails2?.tareWeight?.toDouble() else 0.0
                var tareWeight2 = (halfBagCount2 + fullBagCount2) * bagTareWeight2!!
                var totalWeight2 = ((bagType2Wt * fullBagCount2) + (bagType2Wt / 2 * halfBagCount2)) - tareWeight2

                if (binding.etHalfBag3Count.text?.isNotEmpty() == true) {
                    halfBagCount3 = binding.etHalfBag3Count.text?.toString()?.toInt() ?: 0
                }
                if (binding.etFullBag3Count.text?.isNotEmpty() == true) {
                    fullBagCount3 = binding.etFullBag3Count.text?.toString()?.toInt() ?: 0
                }

                var bagTareWeight3 =
                    if (bagTypeWeightDetails3.tareWeight?.isNotEmpty() == true) bagTypeWeightDetails3?.tareWeight?.toDouble() else 0.0
                var tareWeight3 = (halfBagCount3 + fullBagCount3) * bagTareWeight3!!
                var totalWeight3 = ((bagType3Wt * fullBagCount3) + (bagType3Wt / 2 * halfBagCount3)) - tareWeight3
                stockAuditWeight = (totalWeight1 + totalWeight2 + totalWeight3) / uomWT
                totalWeightLoss = (selectedLots.weight?.toDouble()
                    ?.let { ((totalWeight1 + totalWeight2 + totalWeight3) / uomWT)?.minus(it) } ?: 0) as Double
            }
        }
        return totalWeightLoss
    }
}
