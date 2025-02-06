package com.olam.warehouse.vegax.gateentryapprovalnigeria.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentWorkflowDetails
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaGateEntryDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.entity.WorkflowFields
import com.olam.warehouse.presentation.BuildConfig
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentryapprovalnigeria.R
import com.olam.warehouse.vegax.gateentryapprovalnigeria.data.domain.model.VegaDMSImageResponse
import com.olam.warehouse.vegax.gateentryapprovalnigeria.data.domain.model.VegaGateEntryApprovalNigeriaPost
import com.olam.warehouse.vegax.gateentryapprovalnigeria.data.domain.model.VegaGateEntryApprovalNigeriaResponse
import com.olam.warehouse.vegax.gateentryapprovalnigeria.databinding.FragmentGateEntryApprovalNigeriaSummaryBinding
import com.olam.warehouse.vegax.gateentryapprovalnigeria.utils.*
import com.olam.warehouse.vegax.gateentrynigeria.utils.isNGCashewEnabled
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 3/9/2020.
 */
class VegaGateEntryApprovalNigeriaSummaryFragment : BaseFragment() {

    private var gateEntryData = VegaGateEntryDetails()
    private var postData = mutableListOf<VegaGateEntryDetails>()
    private var plantDetails = Plant()
    private var selectedStatus: String? = ""
    private var uom: String = ""
    private var materialName: String = ""
    private var vendorName: String = ""
    private var materialList = mutableListOf<VegaMaterial>()
    private var supplierList = mutableListOf<VegaVendor>()

    private val vm: VegaGateEntryApprovalNigeriaViewModel by viewModel()
    private lateinit var binding: FragmentGateEntryApprovalNigeriaSummaryBinding
    override val layoutResourceId = R.layout.fragment_gate_entry_approval_nigeria_summary
    private var workFlowData: WorkflowFields? = null


    companion object {
        fun newInstance(
            gateEntryData: VegaGateEntryDetails,
            plantDetails: Plant, materialName: String,
            uom: String, vendorName: String
        ) = VegaGateEntryApprovalNigeriaSummaryFragment().putArgs {
            putParcelable(GATE_ENTRY_DATA, gateEntryData)
            putParcelable(GATE_ENTRY_PLANT_DETAILS, plantDetails)
            putString(MATERIALNAME, materialName)
            putString(UOM, uom)
            putString(VENDORNAME, vendorName)

        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentGateEntryApprovalNigeriaSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("gateentryApprovalNigeria/ui/VegaGateEntryApprovalNigeriaSummaryFragment").title("Vega_ApprovalNigeria/Gate Entry")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        workFlowData = getCurrentWorkflowDetails(getPlantDetails().plantId, "2")
        gateEntryData = arguments?.getParcelable(GATE_ENTRY_DATA)!!
        plantDetails = arguments?.getParcelable(GATE_ENTRY_PLANT_DETAILS)!!
        materialName = arguments?.getString(MATERIALNAME) ?: ""
        uom = arguments?.getString(UOM) ?: ""
        vendorName = arguments?.getString(VENDORNAME) ?: ""
        vm.product.observe(viewLifecycleOwner, Observer {
            materialList = it as MutableList<VegaMaterial>
        })
        vm.getProducts()

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList = it.toMutableList()
        })
        vm.getSuppliers()

        if (gateEntryData.wtype == PROCURE) {
            binding.llSupplier.visible()
            binding.llMtnr.gone()
        } else {
            binding.llSupplier.gone()
            binding.llMtnr.visible()
        }
        /* val directory_path =
             Environment.getExternalStorageDirectory().getAbsolutePath() + '/';

         val file1 = File(directory_path)
         if (!file1.exists()) {
             file1.mkdirs()
         }
         val targetPdf = directory_path + "receipt.pdf"
         val filePath = File(targetPdf)*/

        /* val cw = ContextWrapper(context)
         val directory_path = cw.getDir("imageDir", Context.MODE_PRIVATE)
         val file1 = File(directory_path, "UniqueFileName" + ".jpg")
         if (file1.exists()) {
         }else {
             file1.createNewFile()
         }*/
        val targetPdf = context?.cacheDir?.absolutePath + '/' + "receipt.pdf"
        //base64ToFile(targetPdf, gateEntryData.imageString)

        vm.imageWs.observe(viewLifecycleOwner, Observer {
            getUploadedImages(it)
        })

        //binding.tvSupplierZone.text = gateEntryData.vendorCode.s
        //binding.tvObdNumber.text = gateEntryData.delivery
        //binding.tvDispatchWarehouse.text = gateEntryData.supplierName
        binding.tvTruckNo.text = gateEntryData.truckNumber
        binding.tvDriverName.text = gateEntryData.driverName
        //  binding.tvPhoneNo.text = gateEntryData.contactNumber

        vm.storageLocation.observe(viewLifecycleOwner, Observer {
            binding.tvReceivingLocation.text =
                gateEntryData.storageLocationCode.plus("-").plus(it?.procureLocationName)
        })
        vm.fetchStorageLocation(gateEntryData.storageLocationCode.toString())
        binding.tvPlant.text =
            gateEntryData.plant.plantId.plus("-").plus(gateEntryData.plant.plantName)
        //binding.tvWeight.text = gateEntryData.approximateWeight.plus(" KG")
        //binding.tvMtntNumber.text = gateEntryData.mtnCode
        //binding.tvNoOfBags.text = gateEntryData.tempBagCount

        if (gateEntryData.updatedAt.isNullOrEmpty()) {
            binding.tvDate.visibility = View.GONE
        } else {
            binding.tvDate.visibility = View.VISIBLE
            binding.tvDate.text = DateUtils.getUTCDateTime(System.currentTimeMillis().toString(), App.getAppContext())



        }
        if(isNGCashewEnabled()){
            binding.edtRemarks.visibility=View.VISIBLE
        }

        binding.btnConfirm.setOnClickListener { showConfirmDialog() }
        binding.btViewImages.setOnClickListener {
            if (gateEntryData.wbid.isNullOrEmpty()) {
                showSnack(getString(R.string.error_obd_no))
            } else {
                vm.getDMSUploadedImages(
                    gateEntryData.wbid.toString(),
                    gateEntryData.plant.plantId,
                    gateEntryData.currentYear.toString()
                )
            }
        }
        binding.btnReject.setOnClickListener {
            showRejectDialog()
        }
        vm.gateEntry.observe(viewLifecycleOwner, Observer { updateUI(it) })

        binding.tvProduct.text = gateEntryData.materialCode.plus("-")
            .plus(materialName)
        binding.tvSupplier.text =
            gateEntryData.vendorCode.plus("-").plus(vendorName)
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_gate_entry)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    postGateEntry()
                },
                { dismiss() })
        }
    }

    private fun showRejectDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.reject_gate_entry)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    postRejectGateEntry()
                },
                { dismiss() })
        }
    }

    private fun getUploadedImages(data: Resource<GenericReqAndResp<VegaDMSImageResponse>>?) {
            when (data?.status) {
                Resource.Status.SUCCESS -> {
                    data.data?.data?.let { it1 ->
                         try {
                             val targetPdf = context?.cacheDir?.absolutePath + '/' + "receipt.pdf"
                             gateEntryData.imageString = it1.encodedImageContent.toString()
                             base64ToFile(targetPdf, it1.encodedImageContent.toString())
                         }catch (e:Exception){
                             e.printStackTrace()
                         }

                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), data.error.toString())
                }
                else -> {}
            }
    }

    private fun updateUI(data: Resource<GenericReqAndResp<VegaGateEntryApprovalNigeriaResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> prepareSuccessData(it.data?.data?.wbId,it.data?.data?.batchNumber.toString())
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun postGateEntry() {
        postData.clear()
        var sampleID = generateSampleID()
        gateEntryData.challan = sampleID.toString()
        gateEntryData.remarks= if (isNGCashewEnabled() &&!binding.edtRemarks.text.isNullOrEmpty()){binding.edtRemarks.text.toString()}else{""}
        postData.add(gateEntryData)
        postData.forEachIndexed { index, vegaReceiving ->
            vegaReceiving.item = index.inc().toString()
        }


        val wbData = prepareGateEntryApprovalPost(
            postData,
            binding.tvProduct.text.split("-")[1],
            binding.tvSupplier.text.split("-")[1],
            binding.tvDate.text.split("/")[2],
            uom
        )

        if (AppUtils.isOnline()) {
            selectedStatus = GATE_ENTRY_COMPLETED
//            vm.postGateEntryData(VegaGateEntryApprovalNigeriaPost(getCurrentKey(), getPlantDetails(), postData))
            //vm.postGateEntryData(VegaGateEntryApprovalNigeriaPost(getCurrentKey(), plantDetails, postData))
            vm.postGateEntryData(
                VegaGateEntryApprovalNigeriaPost(
                    key = getCurrentKey(),
                    plant = plantDetails,
                    gateEntry = false,
                    status = selectedStatus!!,
                    weighDetails = wbData,
                    notificationFlag = workFlowData?.NotificationFlag?.trim().equals("0"),
                    nextWorkFlowRole = workFlowData?.workflowRole,
                    navId = workFlowData?.workflowId,
                    currentWorkFlowRole = workFlowData?.module,
                    environment = BuildConfig.BUILD_TYPE,
                    isDelete = false
                )
            )
        }
    }

    private fun postRejectGateEntry() {
        postData.clear()
        var sampleID = generateSampleID()
        gateEntryData.challan = sampleID.toString()
        gateEntryData.remarks= if (isNGCashewEnabled() &&!binding.edtRemarks.text.isNullOrEmpty()){binding.edtRemarks.text.toString()}else{""}
        postData.add(gateEntryData)
        postData.forEachIndexed { index, vegaReceiving ->
            vegaReceiving.item = index.inc().toString()
        }

        val wbData = prepareGateEntryApprovalPost(
            postData,
            binding.tvProduct.text.split("-")[1],
            binding.tvSupplier.text.split("-")[1],
                binding.tvDate.text.split("/")[2],
            uom
        )

        if (AppUtils.isOnline()) {
            selectedStatus = GATE_ENTRY_REJECTED
//            vm.postGateEntryData(VegaGateEntryApprovalNigeriaPost(getCurrentKey(), getPlantDetails(), postData))
            //vm.postGateEntryData(VegaGateEntryApprovalNigeriaPost(getCurrentKey(), plantDetails, postData))
            vm.postGateEntryData(
                VegaGateEntryApprovalNigeriaPost(
                    getCurrentKey(),
                    plantDetails,
                    false,
                    selectedStatus!!,
                    wbData,
                    isDelete = true
                )
            )
        }
    }

    private fun generateSampleID(): Long {
        var sampleID = createRandomInteger(1000000000, 9999999999L, Random)
//        getRandomNumber(1000000000,9999999999L)
        return sampleID
    }

    
    private fun prepareSuccessData(wbId: String?, batchNumber: String?) {
        hideLoading()
        moveToSuccessPage(wbId,batchNumber)
    }

    private fun moveToSuccessPage(wbId: String?,batchNumber: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (selectedStatus.equals(GATE_ENTRY_COMPLETED)) {
            intent.putExtra(AppUtils.TITLE, getString(R.string.success_entry_approve))
        } else if (selectedStatus.equals(GATE_ENTRY_REJECTED)) {
            intent.putExtra(AppUtils.TITLE, getString(R.string.success_rejected))
        }

//        intent.putExtra(
//            AppUtils.SUB_TITLE,
//            getString(R.string.sample_id_created).plus("\n ").plus(getString(R.string.weigh_bridge_id_is)).plus(" ")
//                .plus(wbId)
//        )

      val wb=  if( batchNumber==null || batchNumber.isNullOrEmpty() || batchNumber=="null" ) {
          getString(R.string.weigh_bridge_id_is).plus(" ").plus(wbId)
      }else{
            getString(R.string.weigh_bridge_id_is).plus(" ").plus(wbId).plus("\n").plus(getString(R.string.batch_no)).plus(batchNumber)
      }

        intent.putExtra(
            AppUtils.SUB_TITLE,
            wb
        )

        /*val lotlist = ArrayList<VegaCoffeeSalesLots>()
        lotlist.add(
            VegaCoffeeSalesLots(
                "",
                gateEntryData.challan.toString(),
                gateEntryData.materialCode.toString(),
                gateEntryData.materialName.toString(),
                "",
                "",
                "",
                "",
                "",
                gateEntryData.unitsOfMeasure,
                "",
                gateEntryData.approximateWeight
            )
        )*/

//        intent.putExtra(UIUtils.FROM_CAMEROON_GATEENTRY_COCOA, true)
//        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotlist)
//        intent.putExtra(AppUtils.PRINT_ENABLE, true)

        startActivity(intent)
        requireActivity().finish()
    }



    @Throws(IOException::class)
    fun base64ToFile(path: String?, strBase64: String?) {
        val bytes: ByteArray = Base64.decode(strBase64, Base64.NO_WRAP)
        byteArrayTofile(path, bytes)
        try {
            hideCustomLoading()
            val file: File = File(path)
            val authorities = requireActivity().packageName + ".fileprovider"
            val uri: Uri? = activity?.let { FileProvider.getUriForFile(it, authorities, file) }
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            hideLoading()
            Toast.makeText(context, "Can't read pdf file", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    fun byteArrayTofile(path: String?, bytes: ByteArray?) {
        val file1 = File(path)
        if (file1.exists()) {
        } else {
            file1.createNewFile()
        }
        val output = BufferedOutputStream(FileOutputStream(file1))
        if (bytes != null) {
            output.write(bytes, 0, bytes.size)
        }
        output.close()
    }
}
