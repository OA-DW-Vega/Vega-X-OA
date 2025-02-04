package com.olam.warehouse.vegax.approveghana.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.approveghana.R
import com.olam.warehouse.vegax.approveghana.data.domain.usecase.model.GhanaGRNQualityDetails
import com.olam.warehouse.vegax.approveghana.data.domain.usecase.model.VegaGRNGhanaQuality
import com.olam.warehouse.vegax.approveghana.data.domain.usecase.model.VegaGhanaGrnPost
import com.olam.warehouse.vegax.approveghana.databinding.FragmentVegaGhanaGrnDetailsBinding
import com.olam.warehouse.vegax.approveghana.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 6/30/2020.
 */
class VegaGhanaGrnDetailsFragment : BaseFragment() {
    private var paidWeight: Double = 0.0
    private var mAlertDialog: AlertDialog? = null
    private val vm: VegaGhanaGrnViewModel by viewModel()
    private var weighBridgeId = VegaGrnWeighBridgeId()
    private var wbDetails = VegaGrnWeighBridgeId()
    private var currentMaterial: String = ""
    private var defaultStorageLocation: String = ""
    private var admixtureValue: String = ""
    private var qualitycallBack: QualityDetailsCallBack? = null
    private var approveQualityList = ArrayList<VegaGRNGhanaQuality>()
    private var storageLocation: List<VegaCustomStLocation>? = null

    private lateinit var binding: FragmentVegaGhanaGrnDetailsBinding
    override val layoutResourceId = R.layout.fragment_vega_ghana_grn_details

    interface QualityDetailsCallBack {
        fun replaceQualityDetailsFragment(
            moveFrag: String,
            wbDetails: VegaGrnWeighBridgeId,
            approveQualityList: ArrayList<VegaGRNGhanaQuality>
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaGhanaGrnDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnsesame/ui/details/VegaNigeriaSesameGrnDetailsFragment").title("GRN Ecuador")
            .with(tracker)
        initUI()
    }

    companion object {
        fun newInstance(grnData: VegaGrnWeighBridgeId) = VegaGhanaGrnDetailsFragment().putArgs {
            putParcelable(GRN_DATA, grnData)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        qualitycallBack = context as QualityDetailsCallBack
    }

    private fun initUI() {
        wbDetails = arguments?.getParcelable(GRN_DATA)!!
        vm.getQualityDetails(wbDetails.batchNumber.toString(), wbDetails.materialCode.toString())
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateUI(it) })

        if (wbDetails.unitPrice!!.isNotEmpty()) {
            binding.etPrice.setText(wbDetails.unitPrice.toString())
//            binding.etPrice.isEnabled = false
            binding.tvTotalValue.text = wbDetails.totalPrice
        }
//        updateUIValues()
        currentMaterial = wbDetails.materialCode.toString()
        vm.grn.observe(viewLifecycleOwner, Observer { updateGrnUI(it) })

        binding.tvUsdLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.final_price_per_unit)) { mandatoryStars() } }

//        binding.btQualityDetails.isEnabled = false

        binding.btQualityDetails.setOnClickListener { moveToQualityDetails() }


        binding.btnProceed.setOnClickListener {
            if (wbDetails.purchaseDocNum.isNullOrEmpty()) {
                wbDetails.unitPrice = binding.etPrice.text.toString()
                when {
                    binding.etPrice.text.isNullOrEmpty() -> showSnack(getString(R.string.price_validation))
                    else -> showConfirmDialog()
                }
            } else showConfirmDialog()
        }
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.getConfigItems(UserRoles.GRN.role)
    }

    private fun moveToQualityDetails() {

        qualitycallBack?.replaceQualityDetailsFragment(GRN_QUALITY_DETAILS,wbDetails,approveQualityList)
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>) {
        val defaultStorageLoc = configItems.filter { it.process.equals(ConfigItems.DEFAULT_STORAGE_LOC.item) }
        var isExist = false
        val isMaterial = defaultStorageLoc.map { it.materialCode }.contains(currentMaterial.removeRange(0, 6))
        defaultStorageLoc.forEach {
            if (currentMaterial.contains(it.materialCode) && !it.materialCode.isEmpty() && !isExist && isMaterial && it.applicable?.contains("Y")!!) {
                    defaultStorageLocation = it.value.toString()
                    isExist = true
            } else if (it.materialCode.isEmpty() && !isExist && !isMaterial && it.applicable?.contains("Y")!!) {
                    defaultStorageLocation = it.value.toString()
                    isExist = true
            }
        }
        vm.storageLocation.observe(viewLifecycleOwner, Observer {
        })
        vm.fetchStorageLocation(defaultStorageLocation)
    }

    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaGRNGhanaQuality>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let { it1 ->
                        if (it1.isNotEmpty()) approveQualityList = it1 as ArrayList<VegaGRNGhanaQuality>
                        approveQualityList.forEach {
                            it.qualityParameters.forEach {
                                if(it.sapQCName == "ZNG_ADMIXTURE"){
                                    admixtureValue = it.satNam!!
                                }
                            }
                        }
                        updateUIValues()
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun updateGrnUI(data: Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    mAlertDialog?.dismiss()
                    moveToSuccessPage(
                        it.data?.data?.grnNumber.toString(),
                        it.data?.data?.weighBridgeId.toString(),
                        it.data?.data?.lotId.toString()
                    )
                    vm.updateGrnNoToQuality(
                        wbDetails.weighBridgeId.toString(),
                        it.data?.data?.grnNumber.toString(),
                        it.data?.data?.lotId.toString()
                    )
                    vm.updateGrnSuccess(
                        wbDetails.weighBridgeId.toString(),
                        it.data?.data?.grnNumber.toString(),
                        it.data?.data?.lotId.toString(),
                        getString(R.string.grn_success),
                        4
                    )
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun updateUIValues() {
        binding.tvParamsWeighBID.text = wbDetails.weighBridgeId
        binding.tvTruckSupplier.text = wbDetails.supplierName
        binding.tvNumberOfBags.text = wbDetails.bagCount
        binding.tvMaterial.text = wbDetails.materialName

        vm.custonLocation.observe(this, Observer {
            storageLocation = it

            storageLocation?.forEach { item ->
                if (item.procureLocationCode.contains(wbDetails.storageLocationCode.toString(), true)) {
                    binding.tvStorageLocation.text =
                        wbDetails.storageLocationCode.plus("-").plus(item.procureLocationName.toString())
                }
            }
        })
        vm.getCustomLocations()



        val times = wbDetails.erdat?.split('(', ')')
        binding.tvDate.text = times?.get(1).let { it1 ->
            it1?.let { it2 ->
                DateUtils.getUTCDateTime(
                    it2,
                    App.getAppContext()
                )
            }
        }
        binding.tvNetWeight.text = wbDetails.netWeight.plus(" ").plus(wbDetails.unitsOfMeasure)
        paidWeight = wbDetails.netWeight.toDouble()
        binding.tvPaidWeight.text = wbDetails.netWeight.plus(" ").plus(wbDetails.unitsOfMeasure)

        if (wbDetails.purchaseDocNum.isNullOrEmpty()) {
            binding.grnDetailsTitle.text = getString(R.string.grn)
            binding.tvProcurementType.text = wbDetails.batchNumber
            binding.etPrice.onChange {
                try {
                    val paidData = paidWeight.formatThreeDigits().replace(",", "")
                    val totalVal = it.toDouble() * paidData.toDouble()
                    binding.tvTotalValue.text = totalVal.formatThreeDigits()
                    wbDetails.totalPrice = totalVal.formatThreeDigits()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            binding.grnDetailsTitle.text = getString(R.string.grn).plus(" - ").plus(FIXED)
            binding.tvProcurementType.text = getString(R.string.fixed_purchase)
            binding.tvPoNumber.text = wbDetails.purchaseDocNum
            binding.llPoNumber.visible()
            binding.llPriceDetails.gone()
        }

    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_grn)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    var qualityDetails: ArrayList<GhanaGRNQualityDetails> = ArrayList()
                    approveQualityList.forEach {
                        it.qualityParameters.forEach {
                            var quality = GhanaGRNQualityDetails()
                            if (it.sapQCName == "GH_CASHEW_AVG_BAG_WT") {
                                quality.nameChar = it.sapQCName
                                quality.qualityParameterValue = it.satNam!!.removeSuffix(" MT")
                                qualityDetails.add(quality)
                            } else {
                                quality.nameChar = it.sapQCName
                                quality.qualityParameterValue = it.satNam
                                qualityDetails.add(quality)
                            }
                        }
                    }
//                wbDetails.storageLocationCode = binding.tvStorageLocation.text.toString()
                    val wbData = preparePostGrnData(wbDetails, admixtureValue, qualityDetails)
                    if (isOnline()) {
                        vm.postGrn(
                            VegaGhanaGrnPost(
                                key = getCurrentKey(),
                                plant = getPlantDetails(),
                                grnData = listOf(wbData)
                            )
                        )
                    } else {
                        saveData()
                    }
                },
                { dismiss() })
        }
    }

    private fun saveData() {
        wbDetails.isOfflineData = true
        if (!wbDetails.wbTempId.contains("TMP")) {
            wbDetails.isNotWBID = true
            wbDetails.grnNumber = getTmpId()
        }
        vm.updateGRNPrice(wbDetails)
        moveToSuccessPage(wbDetails.weighBridgeId.toString(),"","")
    }

    private fun moveToSuccessPage(grn: String,wbId: String,batchNo: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (isOnline()) {
            intent.putExtra(AppUtils.TITLE, getString(R.string.grn_success))
        } else
            intent.putExtra(AppUtils.TITLE, getString(R.string.grn_success_offline))
        intent.putExtra(AppUtils.SUB_TITLE, "WB ID : ".plus(wbId).plus("\n Merged To Lot : ").plus(batchNo).plus("\n GRN No : ").plus(grn))

//        intent.putExtra(AppUtils.PRINT_ENABLE, true)
//
//        val grnDco = ArrayList<String>()
//        grnDco.add(encodedImageContent ?: "")
//        intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS, grnDco)
//        intent.putExtra("fromsesamegrn", true)

        startActivity(intent)
        requireActivity().finish()

    }
}
