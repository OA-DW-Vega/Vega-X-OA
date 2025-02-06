package com.olam.warehouse.vegax.grnnigeria.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getMultiPlantList
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.master.veganigeria.model.DecimalRestrictionFilter
import com.olam.warehouse.master.veganigeria.utils.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnnigeria.R
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.*
import com.olam.warehouse.vegax.grnnigeria.databinding.CutsomDialogBcApproveNigeriaCocoaLayoutBinding
import com.olam.warehouse.vegax.grnnigeria.databinding.FragmentVegaNigeriaGrnDetailsBinding
import com.olam.warehouse.vegax.grnnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 6/30/2020.
 */
class VegaNigeriaGrnDetailsFragment : BaseFragment() {
    private var paidWeight: Double = 0.0
    private var mAlertDialog: AlertDialog? = null
    private val vm: VegaNigeriaGrnViewModel by viewModel()
    private var weighBridgeId = VegaGrnWeighBridgeId()
    private var wbPostDetails = VegaQualityWBDetails()
    private var qualityQcPostList = arrayListOf<VegaQualityWBDetails>()
    private var wbDetails = VegaGrnWeighBridgeId()
    private var currentMaterial: String = ""
    private var defaultStorageLocation: String = ""
    private var callBack: VegaNigeriaGrnWBListFragment.CallBack? = null
    private var approveQualityList = ArrayList<VegaNigeriaGRNQuality>()
    private var offlineDataList = mutableListOf<VegaGrnWeighBridgeId>()
    private var weighBridgeUnFilteredList = mutableListOf<VegaGrnWeighBridgeId>()
    private var weighBridgeList = mutableListOf<VegaGrnWeighBridgeId>()
    private var qualitylist = arrayListOf<VegaQualityParams>()
    private var qualitylistDB = mutableListOf<VegaQualityParameter>()
    private var admixtureValue: String = ""
    private var receivingPlant: String = ""
    private var materialCode: String = ""
    private var receivingPlantName: String = ""
    private var qualitycallBack: QualityDetailsCallBack? = null
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    private var stocksList = mutableListOf<VegaCocoaRminLots>()
    private var b_mosit = "0"
    private var ng_admix = "0"
    private var disc_mould = "0"
    private var dis_bean_weight = "0"
    private var dis_bean_slay = "0"
    private var discountOnOthers = "0"

    private lateinit var binding: FragmentVegaNigeriaGrnDetailsBinding
    override val layoutResourceId = R.layout.fragment_vega_nigeria_grn_details

    interface QualityDetailsCallBack {
        fun replaceQualityDetailsFragment(
            moveFrag: String,
            wbDetails: VegaGrnWeighBridgeId,
            approveQualityList: ArrayList<VegaNigeriaGRNQuality>
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
        binding = FragmentVegaNigeriaGrnDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnnigeria/ui/VegaNigeriaGrnDetailsFragment").title("GRN Ecuador")
            .with(tracker)
        initUI()
    }

    companion object {
        fun newInstance(grnData: VegaGrnWeighBridgeId) = VegaNigeriaGrnDetailsFragment().putArgs {
            putParcelable(GRN_DATA, grnData)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        qualitycallBack = context as QualityDetailsCallBack
    }

    private fun initUI() {
        wbDetails = arguments?.getParcelable(GRN_DATA)!!
        wbDetails.unitsOfMeasure = "MT"
        /* vm.getQualityDetails(wbDetails.batchNumber.toString(), wbDetails.materialCode.toString())
         vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateUI(it) })*/
        if (wbDetails.unitPrice!!.isNotEmpty() && !wbDetails.unitPrice.equals("0.00")) {
            //binding.etPrice.setText(wbDetails.unitPrice.toString())
            binding.etPrice.isEnabled = true
           // binding.tvTotalValue.text = wbDetails.totalPrice
        }
        currentMaterial = wbDetails.materialCode.toString()
        updateUIValues()
        vm.grn.observe(viewLifecycleOwner, Observer { updateGrnUI(it) })

        vm.material.observe(viewLifecycleOwner, Observer { bagTypeList = it.toMutableList() })
        vm.getMaterials()

       // vm.stocks.observe(viewLifecycleOwner, Observer { updateUIStocks(it) })

        vm.currentBagIssue.observe(viewLifecycleOwner, Observer { updateCurrentBagsUI(it) })

        if (isOnline()) {
            vm.weighBridge.observe(viewLifecycleOwner, Observer { updateWeighBrideUI(it) })
            vm.getWeighBridgeList()
        } else {
            vm.weighBridgeLocal.observe(viewLifecycleOwner, Observer { updateUIWithLocalData(it) })
            vm.getWeighBridgeDetail()
        }

        vm.getQualityDetails(wbDetails.batchNumber.toString(), wbDetails.materialCode.toString())
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateUI(it) })

        vm.quality.observe(viewLifecycleOwner, Observer {
            try {
                updateQualityApproval(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        vm.getQualityDetailsDB(wbDetails.materialCode.toString(), "X")
        vm.qualityDetailsDB.observe(viewLifecycleOwner, Observer { updateUIDB(it) })

        binding.tvUsdLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.grn_price)) { mandatoryStars() } }
        binding.tvDiscountLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.discount_on_others)) { mandatoryStars() } }
        binding.tvDiscountLabel1.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.mould_discount_mt)) { mandatoryStars() } }
        binding.tvDiscountLabel2.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.bean_weight_discount_mt)) { mandatoryStars() } }
        binding.tvDiscountLabel3.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.bean_slaty_discount_mt)) { mandatoryStars() } }
        binding.etPrice.filters = arrayOf(DecimalRestrictionFilter())
        binding.etDiscount1.filters = arrayOf(DecimalRestrictionFilter())
        binding.etDiscount2.filters = arrayOf(DecimalRestrictionFilter())
        binding.etDiscount3.filters = arrayOf(DecimalRestrictionFilter())
        binding.etDiscount.filters = arrayOf(DecimalRestrictionFilter())
        binding.etPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // grade = binding.tvGradeValue.text.toString()
                var value = p0.toString()
                try {
                    if(value!= ".") {
                        binding.etPrice1.text = ""
                        disc_mould = binding.etDiscount1.text.toString()
                        dis_bean_weight = binding.etDiscount2.text.toString()
                        dis_bean_slay = binding.etDiscount3.text.toString()
                        var moist = calcualateB_MoistDiscount(value, b_mosit)
                        binding.etCalcMoistDisc.text = moist
                       // println("nnnnnnoist $moist")
                        var admix = calcualateAdmixDiscount(value, ng_admix)
                        binding.etCalcAdmixDisc.text = admix
                        binding.etTotalDiscount.text = TOTAL_DISC
                       // println("nnnnnkkkkngad $admix")
                        var price =
                            calcualateGrnPaidPrice(value, disc_mould, dis_bean_weight, dis_bean_slay, discountOnOthers)
                        binding.etPrice1.text = price
                        calculateTotalValue()
                    }
                   /* if(binding.etDiscount.text.isNotEmpty())  {
                        binding.etPrice1.setText(calculateFinalPrice(binding.etDiscount.text.toString()))
                    }*/
                    //wbDetails.totalPrice = value.toDouble().toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                //
            }
        })

        binding.etDiscount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                discountOnOthers = if(p0?.isNotBlank() == true) p0.toString() else "0"
                if(binding.etPrice.text.isNotEmpty())  {
                    if(discountOnOthers != "-" && discountOnOthers != ".") {
                        val value = binding.etPrice.text.toString()
                        binding.etPrice1.text = calcualateGrnPaidPrice(
                            value,
                            disc_mould,
                            dis_bean_weight,
                            dis_bean_slay,
                            discountOnOthers
                        )
                        binding.etTotalDiscount.text = TOTAL_DISC
                        calculateTotalValue()
                    }
                } else {
                    binding.etPrice1.text = ""
                    showSnack(getString(R.string.enter_grn_unit_price))
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                //
            }
        })

        binding.btnProceed.setOnClickListener {
            if (wbDetails.purchaseDocNum.isNullOrEmpty()) {
                wbDetails.totalPrice = binding.etPrice1.text.toString()
                wbDetails.unitPrice = binding.etPrice.text.toString()
                when {
                    binding.etPrice.text.isNullOrEmpty() -> showSnack(getString(R.string.price_validation))
                    binding.etDiscount.text.isNullOrEmpty() || binding.etDiscount1.text.isNullOrEmpty() || binding.etDiscount2.text.isNullOrEmpty() ||
                            binding.etDiscount3.text.isNullOrEmpty() -> showSnack("Please enter all mandatory discount values")
                    else -> showConfirmDialog()
                }
            } else showConfirmDialog()
        }
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        vm.getConfigItems(UserRoles.GRN.role)
        binding.btQualityDetails.setOnClickListener { moveToQualityDetails() }

        //btQualityDetails.setOnClickListener { view -> callBack?.replaceQualityFragment(GRN_QUALITY_DETAILS, wbDetails, approveQualityList) }
    }

    private fun moveToQualityDetails() {
        qualitycallBack?.replaceQualityDetailsFragment(
            GRN_QUALITY_DETAILS,
            wbDetails,
            approveQualityList
        )
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>) {
        val defaultStorageLoc =
            configItems.filter { it.process.equals(ConfigItems.DEFAULT_STORAGE_LOC.item) }
        var isExist = false
        val isMaterial =
            defaultStorageLoc.map { it.materialCode }.contains(currentMaterial.removeRange(0, 6))
        defaultStorageLoc.forEach {
            if (currentMaterial.contains(it.materialCode) && !it.materialCode.isEmpty() && !isExist && isMaterial) {
                if (it.applicable?.contains("Y")!!) {
                    defaultStorageLocation = it.value.toString()
                    isExist = true
                }
            } else if (it.materialCode.isEmpty() && !isExist && !isMaterial) {
                if (it.applicable?.contains("Y")!!) {
                    defaultStorageLocation = it.value.toString()
                    isExist = true
                }
            }
        }
        vm.storageLocation.observe(viewLifecycleOwner, Observer {
        })
        vm.fetchStorageLocation(defaultStorageLocation)
    }

    private fun updateCurrentBagsUI(data: Resource<GenericReqAndResp<List<VegaNigeriaCurrentBagsIssued>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    if (it.data?.data?.size ?: 0 > 1) {
                        var currentBalance = it.data?.data?.get(1)?.unresConStock
                      /*  bagIssueData.currentBalance = currentBalance
//                    binding.llBagIssue.visibility = View.VISIBLE
                        clearAll()
                        binding.tvCurrentBagIssuedLabel.text =
                                getString(R.string.current_bag_issued).plus(" ")
                                        .plus(currentBalance.toString().toDouble().toLong()).plus(" bags")*/
                        var totalWeight: Double? = (currentBalance)?.toDouble()
                        var enteredBags: Double? = (wbDetails.bagCount.toString()).toDouble()
                        if (((totalWeight!!) >= (enteredBags!!))) {
                            val wbData = preparePostGrnData(wbDetails, admixtureValue, bagTypeList)
                            if (isOnline()) {
                                vm.postGrn(
                                        VegaNigeriaGrnPost(
                                                key = getCurrentKey(),
                                                plant = getPlantDetails(),
                                                grnData = listOf(wbData),
                                                qualityDetails = preparePostGrnData1(qualitylist, qualitylistDB),
                                                userName = PreferenceHelper.get(Constants.USER_NAME, ""),
                                                grntNumber = wbDetails.grntNumber.toString()
                                        )
                                )
                            } else {
                                hideLoading()
                                saveData()
                            }
                        } else {
                            hideLoading()
                            showErrorDialogWithFAQLink(
                                    requireContext(),
                                    getString(R.string.dis_stock_not_available)
                            )
                        }
                    } else {
                        hideLoading()
                        showErrorDialogWithFAQLink(requireContext(), getString(R.string.no_data_found))
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {

                }

            }
        }
    }

   /* private fun updateUIStocks(response: Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            stocksList.clear()
                            val dataValue = it.data?.data!!
                            stocksList.addAll(dataValue)
                            var materialCode = materialCode
                            var stockValue =
                                stocksList.filter { it.storageLocationCode.equals(wbDetails.storageLocationCode) }
                                    .filter {
                                        it.plantId.equals(wbDetails.plantId)
                                    }.filter { it.materialCode.equals(materialCode) }
                            var weight: Double? = 0.0
                            var totalWeight: Double? = 0.0
                            stockValue.forEach { it1 ->
                                totalWeight = ((weight!!) + (it1.weight?.toDouble()!!))
                            }
                            //var enteredBags: Double? = (calculateWeight()).toDouble()
                            var enteredBags: Double? = (wbDetails.bagCount.toString()).toDouble()
                            if (((totalWeight!!) >= (enteredBags!!))) {
                                val wbData = preparePostGrnData(wbDetails, admixtureValue, bagTypeList)
                                if (isOnline()) {
                                    vm.postGrn(
                                            VegaNigeriaGrnPost(
                                                    key = getCurrentKey(),
                                                    plant = getPlantDetails(),
                                                    grnData = listOf(wbData),
                                                    qualityDetails = preparePostGrnData1(qualitylist, qualitylistDB),
                                                    userName = PreferenceHelper.get(Constants.USER_NAME, ""),
                                                    grntNumber = wbDetails.grntNumber.toString()
                                            )
                                    )
                                } else {
                                    hideLoading()
                                    saveData()
                                }
                            } else {
                                hideLoading()
                                showErrorDialogWithFAQLink(
                                    requireContext(),
                                    getString(R.string.dis_stock_not_available)
                                )
                            }
                        }
                        else ->
                        {
                            hideLoading()
                            showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                        }
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }


    }*/

    private fun updateWeighBrideUI(data: Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    val weighBridgeUnFiltered = it.data?.data
                    if (weighBridgeUnFiltered?.size!! > 0) {
                        weighBridgeUnFiltered.forEach { wb ->
                            if (!offlineDataList.map { it.weighBridgeId }
                                    .contains(wb.weighBridgeId)) {
                                weighBridgeUnFilteredList.add(wb)
                            }
                        }
                    }

                    val weighBridge =
                        it.data?.data?.filter { it.qcStatus == "X" && it.grnNumber.isNullOrEmpty() }
                    if (weighBridge?.size!! > 0) {
                        weighBridge.forEach { wb ->
                            if (!offlineDataList.map { it.weighBridgeId }
                                    .contains(wb.weighBridgeId)) {
                                weighBridgeList.add(wb)
                            }
                        }
                    } else {
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun updateUIWithLocalData(data: List<VegaGrnWeighBridgeId>?) {
        val weighBridge =
            data?.filter { it.qcStatus.isNullOrEmpty() && it.grnNumber.isNullOrEmpty() }
        if (weighBridge?.size!! > 0) {
            weighBridgeList = weighBridge as MutableList<VegaGrnWeighBridgeId>
        }
    }

    private fun updateGrnUI(data: Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    mAlertDialog?.dismiss()
                    //moveToSuccessPage(it.data?.data?.grnNumber.toString())
                    vm.updateGrnNoToQuality(
                        wbDetails.weighBridgeId.toString(),
                        it.data?.data?.grnNumber.toString(),
                        it.data?.data?.batchNumber.toString()
                    )
                    vm.updateGrnSuccess(
                        wbDetails.weighBridgeId.toString(),
                        it.data?.data?.grnNumber.toString(),
                        it.data?.data?.batchNumber.toString(),
                        getString(R.string.grn_success),
                        4
                    )

                    showConfirmApproveDialog(it.data?.data?.grnNumber.toString())

                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun showConfirmApproveDialog(grnNo: String) {

        /*val mDialogView = LayoutInflater.from(activity?.applicationContext)
            .inflate(R.layout.cutsom_dialog_bc_approve_nigeria_cocoa_layout, null)*/
        val mDialogView =
            CutsomDialogBcApproveNigeriaCocoaLayoutBinding.inflate(LayoutInflater.from(activity?.applicationContext))
        val mBuilder = AlertDialog.Builder(activity)
            .setView(mDialogView.root)
        mAlertDialog = mBuilder?.show()
        mAlertDialog?.setCancelable(false)
        mAlertDialog?.setCanceledOnTouchOutside(false)
        mDialogView.llGrn.gone()
        mDialogView.llApproval.visible()
        mDialogView.tvGrnNo.text =
            getString(R.string.procurement_completed).plus("\n").plus(getString(R.string.grn_no))
                .plus(grnNo)
        mDialogView.ivClose.gone()

        mDialogView.tvConfirmApproval.setOnClickListener {
            mAlertDialog?.dismiss()
            wbPostDetails.batchNumber = wbDetails.batchNumber

            wbPostDetails.bagCount = wbDetails.bagCount
            wbPostDetails.bagType = wbDetails.bagType
            wbPostDetails.bagMaterialCode = bagTypeList.find { it.bagType.equals(wbDetails.bagType) }?.bagMaterialCode
            wbPostDetails.bagWeight = wbDetails.bagWeight
            wbPostDetails.grossWeight = wbDetails.grossWeight
            wbPostDetails.item = "00001"
            wbPostDetails.materialCode = wbDetails.materialCode
            wbPostDetails.netWeight = wbDetails.netWeight
            wbPostDetails.plant = wbDetails.plantId
            wbPostDetails.supplierCode = wbDetails.supplierCode
            wbPostDetails.storageLocationCode = wbDetails.storageLocationCode.toString()
            wbPostDetails.unitsOfMeasure = "MT"
            wbPostDetails.vehicleNumber = wbDetails.vehicleNumber
            wbPostDetails.weighBridgeId = wbDetails.weighBridgeId.toString()
            wbPostDetails.weighBridgeType = wbDetails.weighBridgeType
            wbPostDetails.deliveryItem = wbDetails.deliveryItem

            wbPostDetails.finalApproval = "X"
            wbPostDetails.qualityFlag = true
            wbPostDetails.appName = "BC"
            wbPostDetails.grnModel = wbDetails.grnModel
            wbPostDetails.procurementType = wbDetails.procurementType
            /* if(wbDetails.procurementType.equals(DD) || wbDetails.procurementType.equals(DX)) {
                 wbPostDetails.autoTransfer = "T"
                 wbPostDetails.recStorageLocation = wbDetails.plantId.plus(":").plus(wbDetails.storageLocationCode)
             }*/
           approveQualityList.forEach {
                it.qualityParameters.forEach {
                    if(it.sapQCName.equals(RECEIVING_PLANT)){
                        receivingPlantName = it.satNam.toString()
                    }
                    else if(it.sapQCName?.trim().equals("ZNGCOCOA_DIS_MOULD")) {
                        it.satNam = disc_mould + NGN
                    }
                    else if(it.sapQCName?.trim().equals("ZNGCOCOA_DIS_BW")) {
                        it.satNam = dis_bean_weight + NGN
                    }
                    else if(it.sapQCName?.trim().equals("ZNGCOCOA_GRNPRICE") /*|| it.sapQCName?.trim().equals("ZNGCOCOA_GRNPRICE1")*/) {
                        it.satNam = binding.etPrice.text.toString() + NGN
                    }
                    else if(it.sapQCName?.trim().equals("ZNGCOCOA_DIS_ON_OTHERS")) {
                        it.satNam = binding.etDiscount.text.toString() + NGN
                    }
                    else if(it.sapQCName?.trim().equals("ZNGCOCOA_DIS_BS") /*|| it.sapQCName?.trim().equals("ZNGCOCOA_GRNPAIDPRICE1")*/) {
                         it.satNam = dis_bean_slay + NGN
                    }
                }
            }
            var qualityListData = VegaNigeriaGRNQualityParams()

            /*Ticket no : DWALL - 5333,*/
            var plant = getMultiPlantList().filter { plantList -> plantList.plantId.equals(receivingPlantName) }.single()
            var recvPlantAndStorLocation =  receivingPlantName.plus(":").plus(plant.storageLocation.get(0).storageLocationCode)

            if(wbDetails.procurementType.equals(DD) || wbDetails.procurementType.equals(DX)) {
                wbPostDetails.autoTransfer = "T"
//                wbPostDetails.recStorageLocation =
//                        receivingPlantName.plus(":").plus(wbDetails.storageLocationCode)
                wbPostDetails.recStorageLocation = recvPlantAndStorLocation
            }else {
                //if ((PreferenceHelper.get(Constants.WERKS, "")).equals(wbDetails.plantId.toString())) {
                if ((PreferenceHelper.get(Constants.WERKS, "")).equals(receivingPlant)) {
                    wbPostDetails.autoTransfer = ""
                } else {
                    wbPostDetails.autoTransfer = "T"
//                    wbPostDetails.recStorageLocation =
//                            receivingPlantName.plus(":").plus(wbDetails.storageLocationCode)
                    wbPostDetails.recStorageLocation = recvPlantAndStorLocation
                }
            }
            /* wbPostDetails.LOBM_UDCODE = "01       A"
             wbPostDetails.let { qualityQcPostList.addAll(listOf(it)) }*/
            val qualityDetails: ArrayList<VegaQuality> = ArrayList()
            approveQualityList.forEach {
                it.qualityParameters.forEach {
                    val quality = VegaQuality()
                    quality.descrChar = ""
                    quality.nameChar = it.sapQCName.toString()
                    quality.qualityParameterValue = it.satNam
                    qualityDetails.add(quality)

                }
                val quality = VegaQuality()
                quality.descrChar = ""
                quality.nameChar = "ZNGCOCOA_GRNPRICE"
                quality.qualityParameterValue = binding.etPrice.text.toString() + NGN
                qualityDetails.add(quality)

            }

            // val usageDecision = VegaQuality()
            // usageDecision.descrChar = "Usage Decision"
            //usageDecision.nameChar = "LOBM_UDCODE"
            //usageDecision.qualityParameterValue = "OL-RM    A"

            //qualityDetails.add(usageDecision)

            wbPostDetails.qualityDetails = qualityDetails
            wbPostDetails.let { qualityQcPostList.addAll(listOf(it)) }
            showLoading()
            vm.postQualityParams(
                VegaCameroonQcPost(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    lotDetails = qualityQcPostList
                )
            )

        }

        mAlertDialog?.show()
    }

    private fun updateUIValues() {
        binding.tvParamsWeighBID.text = wbDetails.weighBridgeId
        binding.tvTruckSupplier.text = wbDetails.supplierName
        binding.tvNumberOfBags.text = wbDetails.bagCount
        binding.tvTruckMaterial.text = wbDetails.materialName
        binding.tvNetWeight.text = wbDetails.netWeight.plus(" ").plus(wbDetails.unitsOfMeasure)
        paidWeight = wbDetails.netWeight.toDouble()
        binding.tvPaidWeight.text = wbDetails.netWeight.plus(" ").plus(wbDetails.unitsOfMeasure)
        binding.etbatchNo.text = wbDetails.batchNumber.toString()

        val times = wbDetails.erdat?.split('(', ')')
        binding.tvDate.text = times?.get(1).let { it1 ->
            it1?.let { it2 ->
                DateUtils.getUTCDateTime(
                    it2,
                    App.getAppContext()
                )
            }
        }

        //paidWeight.formatThreeDigits().replace(",", "").plus(" ").plus(wbDetails.unitsOfMeasure)
        //binding.tvStorageLocation.text = defaultStorageLocation
        if (wbDetails.purchaseDocNum.isNullOrEmpty()) {
//            binding.grnDetailsTitle.text = getString(R.string.grn).plus(" - ").plus(SPOT)
//            binding.tvProcurementType.text = getString(R.string.spot_purchase)
         /*   binding.etPrice.onChange {
                try {
                    val paidData = paidWeight.formatThreeDigits().replace(",", "")
                    val totalVal = it.toDouble() * paidData.toDouble()
                    binding.tvTotalValue.text = totalVal.formatThreeDigits()
                    wbDetails.totalPrice = totalVal.formatThreeDigits()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }*/
        } else {
//            binding.grnDetailsTitle.text = getString(R.string.grn).plus(" - ").plus(FIXED)
//            binding.tvProcurementType.text = getString(R.string.fixed_purchase)
            binding.tvTruckMaterial.text = wbDetails.materialName
            binding.tvPoNumber.text = wbDetails.purchaseDocNum
            binding.llPoNumber.visible()
            binding.llPriceDetails.gone()
        }

    }

    private fun updateUIDB(data: List<VegaQualityParameter>) {
        qualitylistDB = data as MutableList<VegaQualityParameter>
    }

    private fun updateQualityApproval(response: Resource<GenericReqAndResp<VegaNigeriaCocoaQualityApprovePostResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            it.data?.data?.currentWbid?.let { it1 ->
                                moveToApprovalSuccessPage(it.data?.data?.charg!!)
                            }
                        }
                        else -> {
                            showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                        }
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun moveToApprovalSuccessPage(lotId: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.approve_success))
        intent.putExtra(AppUtils.SUB_TITLE, lotId)
        intent.putExtra(
            AppUtils.SUB_TITLE,
            "\n ".plus((getString(R.string.lot_id))).plus(lotId)
        )
        startActivity(intent)
        requireActivity().finish()
    }

    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaNigeriaGRNQuality>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    qualitylist.clear()
                    it.data?.data?.let { it1 ->
                        if (it1.isNotEmpty()) approveQualityList =
                            it1 as ArrayList<VegaNigeriaGRNQuality>
                        approveQualityList.forEach {
                            it.qualityParameters.forEach {
                                if (it.sapQCName == "ZNG_ADMIXTURE") {
                                    admixtureValue = it.satNam!!
                                }
                                if(it.sapQCName=="RECEIVING_PLANT"){
                                    receivingPlant= it.satNam.toString()
                                }
                                val item = VegaQualityParams()
                                item.qualityParameterName = it.qualityParameterName
                                item.sapQCName = it.sapQCName
                                item.satNam = it.satNam
                                qualitylist.add(item)


                            }
                        }
                        updateUIValues()
                    }
                    qualitylist.forEach { it ->
                        if(it.sapQCName?.trim().equals("B_MOIST")) {
                            b_mosit = it.satNam?.replace("%","")?.trim().toString()
                        }
                        else if(it.sapQCName?.trim().equals("NG_ADMIX")) {
                            ng_admix = it.satNam?.replace("%","")?.trim().toString()
                        }
                        else if(it.sapQCName?.trim().equals("ZNGCOCOA_ACTBW")) {
                            ZNGCOCOA_ACTBW = it.satNam?.replace("%","")?.trim().toString()
                        }
                        else if(it.sapQCName?.trim().equals("B_DCTBW1")) {
                            B_DCTBW1 = it.satNam?.replace("%", "")?.trim().toString()
                        }
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_grn)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    //wbDetails.storageLocationCode = binding.tvStorageLocation.text.toString()
                   /* if( (!(wbDetails.bagType.isNullOrEmpty())) && wbDetails.bagType.equals(JUTE_BAG)){
                        val data =
                            bagTypeList.filter { it.bagType.equals(wbDetails.bagType.toString()) }
                        if (data.isNotEmpty()) {
                            materialCode = data[0].bagMaterialCode
                            //if (wbDetails.materialCode.toString().isNotEmpty() && !wbDetails.supplierCode.isNullOrEmpty() && !wbDetails.storageLocationCode.isNullOrEmpty())
                                vm.getCurrentBagsIssued(
                                        materialCode,
                                        wbDetails.supplierCode.toString(),
                                        wbDetails.storageLocationCode.toString()
                                )
                        }else{
                            showSnack(getString(R.string.bag_type_empty))
                        }
                    }else {*/
                        val wbData = preparePostGrnData(wbDetails, admixtureValue, bagTypeList)
                        if (isOnline()) {
                            vm.postGrn(
                                VegaNigeriaGrnPost(
                                    key = getCurrentKey(),
                                    plant = getPlantDetails(),
                                    grnData = listOf(wbData),
                                    //qualityDetails = preparePostGrnData1(qualitylist, qualitylistDB,binding.etPrice.text.toString(),binding.etDiscount.text.toString()),
                                    qualityDetails = prepareQualityPostData1(qualitylist,qualitylistDB,binding.etPrice.text.toString(),binding.etDiscount.text.toString(),disc_mould,dis_bean_weight,dis_bean_slay),
                                    userName = PreferenceHelper.get(Constants.USER_NAME, ""),
                                    grntNumber = wbDetails.grntNumber.toString()
                                )
                            )
                        } else {
                            saveData()
                        }
                    //}
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
        moveToSuccessPage(wbDetails.weighBridgeId.toString())
    }

    private fun moveToSuccessPage(grn: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (isOnline()) {
            intent.putExtra(AppUtils.TITLE, getString(R.string.grn_success))
            // vm.updateDeletedItem(wbDetails.weighBridgeId.toString())
        } else
            intent.putExtra(AppUtils.TITLE, getString(R.string.grn_success_offline))
        intent.putExtra(AppUtils.SUB_TITLE, grn)
        if(PreferenceHelper.get(Constants.DIRECT,"").isNotEmpty() || PreferenceHelper.get(Constants.IN_DIRECT,"").isNotEmpty())
            intent.putExtra(AppUtils.EUDR_STATUS, if(wbDetails.complianceFlag.equals(Constants.COMPLAINT)) "1" else "0" )
        startActivity(intent)
    }

    private fun moveToFailurePage(wbId: String, msg: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.grn_fail_msg))
        intent.putExtra(AppUtils.SUB_TITLE, msg.plus("\n").plus(wbId))
        intent.putExtra(AppUtils.FAILURE, false)
        intent.putExtra(AppUtils.MSG, msg)
        startActivity(intent)
    }

    private fun calculateTotalValue() {
        try {
            var paidPrice = binding.etPrice1.text.toString()
            var netWeight = binding.tvNetWeight.text.toString()

            binding.tvTotalValue.text =
                paidPrice.toDouble().times(netWeight.replace("MT", "").toDouble()).formatTwoDigits()
        }catch (e:NumberFormatException){
            e.printStackTrace()
        }
    }
}
