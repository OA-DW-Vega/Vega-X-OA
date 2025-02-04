package com.olam.warehouse.vegax.grnnigeria.ui

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
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
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
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.VegaCameroonQcPost
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.VegaNigeriaCocoaQualityApprovePostResponse
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.VegaNigeriaGRNQuality
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.VegaNigeriaGrnPost
import com.olam.warehouse.vegax.grnnigeria.databinding.FragmentVegaNigeriaGrnDetailsBinding
import com.olam.warehouse.vegax.grnnigeria.utils.*
import kotlinx.android.synthetic.main.cutsom_dialog_bc_approve_nigeria_cocoa_layout.view.*
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
    private var qualitycallBack: QualityDetailsCallBack? = null
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()

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
        /* vm.getQualityDetails(wbDetails.batchNumber.toString(), wbDetails.materialCode.toString())
         vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateUI(it) })*/
        if (wbDetails.unitPrice!!.isNotEmpty() && !wbDetails.unitPrice.equals("0.00")) {
            binding.etPrice.setText(wbDetails.unitPrice.toString())
            binding.etPrice.isEnabled = false
            binding.tvTotalValue.text = wbDetails.totalPrice
        }
        currentMaterial = wbDetails.materialCode.toString()
        updateUIValues()
        vm.grn.observe(viewLifecycleOwner, Observer { updateGrnUI(it) })

        vm.material.observe(viewLifecycleOwner, Observer { bagTypeList = it.toMutableList() })
        vm.getMaterials()

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
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
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
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun showConfirmApproveDialog(grnNo: String) {

        val mDialogView = LayoutInflater.from(activity?.applicationContext)
            .inflate(R.layout.cutsom_dialog_bc_approve_nigeria_cocoa_layout, null)
        val mBuilder = AlertDialog.Builder(activity)
            .setView(mDialogView)
        mAlertDialog = mBuilder?.show()
        mAlertDialog?.setCancelable(false)
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
            wbPostDetails.bagWeight = wbDetails.bagWeight
            wbPostDetails.grossWeight = wbDetails.grossWeight
            wbPostDetails.item = "00001"
            wbPostDetails.materialCode = wbDetails.materialCode
            wbPostDetails.netWeight = wbDetails.netWeight
            wbPostDetails.plant = wbDetails.plantId
            wbPostDetails.supplierCode = wbDetails.supplierCode
            wbPostDetails.storageLocationCode = wbDetails.storageLocationCode.toString()
            wbPostDetails.unitsOfMeasure = "KG"
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
            if ((PreferenceHelper.get(Constants.WERKS, "")).equals(wbDetails.plantId.toString())) {
                wbPostDetails.autoTransfer = ""
            } else {
                wbPostDetails.autoTransfer = "T"
                wbPostDetails.recStorageLocation =
                    wbDetails.plantId.plus(":").plus(wbDetails.storageLocationCode)
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
                            UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                        }
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
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
                                val item = VegaQualityParams()
                                item.qualityParameterName = it.qualityParameterName
                                item.sapQCName = it.sapQCName
                                item.satNam = it.satNam
                                qualitylist.add(item)


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

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_grn)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    //wbDetails.storageLocationCode = binding.tvStorageLocation.text.toString()
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
        startActivity(intent)
    }

    private fun moveToFailurePage(wbId: String, msg: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.grn_fail_msg))
        intent.putExtra(AppUtils.SUB_TITLE, msg.plus("\n").plus(wbId))
        intent.putExtra(AppUtils.FAILURE, false)
        startActivity(intent)
    }
}
