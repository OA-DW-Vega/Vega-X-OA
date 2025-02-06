package com.olam.warehouse.vegax.offloadingnigeria.ui.mtnr

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingWarehouseWithMtns
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingnigeria.R
import com.olam.warehouse.vegax.offloadingnigeria.data.domain.usecase.model.VegaDMSImageResponse
import com.olam.warehouse.vegax.offloadingnigeria.databinding.FragmentNigeriaMtnrConsignmentLayoutBinding
import com.olam.warehouse.vegax.offloadingnigeria.databinding.ItemNigeriaMtnrWeighscaleLotCardLayoutBinding
import com.olam.warehouse.vegax.offloadingnigeria.ui.VegaNigeriaOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingnigeria.ui.VegaNigeriaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit


class VegaNigeriaMtnrConsignmentFragment : BaseFragment(), VegaSingleSelectListener {

    override val layoutResourceId = R.layout.fragment_nigeria_mtnr_consignment_layout
    private lateinit var binding: FragmentNigeriaMtnrConsignmentLayoutBinding
    private var callBack: VegaNigeriaOffloadReplaceFragmentCallback? = null
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private lateinit var moreWeightBatches: String
    private var addWeightPosition: Int = 0
    private val vm: VegaNigeriaOffloadingViewModel by viewModel()
    private var wareHouseList: MutableList<VegaSupplyStorageLocation> = mutableListOf()
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var warehouseWithMtn = VegaCoffeeReceivingWarehouseWithMtns()
    private var selectedSendingLocation = VegaSupplyStorageLocation()
    private var selectedReceivingLocation = VegaCustomStLocation()
    private var selectedOBD = VegaReceivingMtn()
    private var batchList = mutableListOf<VegaCoffeeReceiveLots>()
    private var allBatchList = mutableListOf<VegaCoffeeReceiveLots>()
    private var allOBDList = mutableListOf<VegaReceivingMtn>()
    private var filteredOBDList = mutableListOf<VegaReceivingMtn>()
    private var supplierList = mutableListOf<VegaVendor>()
    private var editLotId: String = ""
    private var vendorName: String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaNigeriaOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaNigeriaMtnrConsignmentFragment().putArgs {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNigeriaMtnrConsignmentLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingsesame/ui/mtnr/VegaSesameMtnrTypeSelectFragment")
            .title("Mtnr Coffee")
            .with(tracker)
    }

    private fun initUI() {

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        updateMandatory()
        enableProceed()
        binding.tvWhValue.setOnClickListener {
            showSingleSelectDialog(
                true,
                getString(R.string.select_dest_wh),
                false
            )
        }
        binding.tvstoValue.setOnClickListener {
            if (binding.tvWhValue.text.toString().isNotEmpty()) showSingleSelectDialog(
                false,
                getString(R.string.select_obd),
                false
            )
        }

        binding.tvReceivingValue.setOnClickListener {
            if (customLocationList.size > 1) {
                showSingleSelectDialog(
                    false,
                    getString(R.string.select_receive_loc),
                    true
                )
            }

        }

        vm.imageWs.observe(viewLifecycleOwner, Observer {
            getUploadedImages(it)
        })

        vm.weighBridgeId.observe(viewLifecycleOwner, Observer {
//            val supplierCode = it.data?.data?.supplierCode
//            if (!supplierCode.isNullOrEmpty()) {
//                vendorName = supplierList.single { it.vendorCode.equals(supplierCode) }.vendorName!!
//            }
            updateTruckDetail(it.data?.data)
        })

        vm.location.observe(viewLifecycleOwner, Observer { wareHouseList = it.toMutableList() })

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            customLocationList = it
                .filter { it.storageLocationType.equals("P") }
                .toMutableList()
            if (customLocationList.size == 1) {
                customLocationList.forEach {
                    binding.tvReceivingValue.text =
                        customLocationList[0].procureLocationCode.plus(" - ").plus(
                            customLocationList[0].procureLocationName
                        )
                    vm.vegaCoffeeReceivingData.storageLocationCode =
                        customLocationList[0].procureLocationCode
                    vm.vegaCoffeeReceivingData.storageLocationName =
                        customLocationList[0].procureLocationName
                    selectedReceivingLocation =
                        customLocationList.single { it.procureLocationCode == customLocationList[0].procureLocationCode }
                    vm.vegaCoffeeReceivingData.materialCode = customLocationList[0].plant

                }
            }
            /*customLocationList.forEach {
                if (it.procureLocationCode.equals(receivingData.storageLocationCode)) {
                    binding.tvReceivingLocation.text =
                        receivingData.storageLocationCode.plus("-").plus(it.procureLocationName)
                    receivingData.storageLocationName = it.procureLocationName
                }
            }*/
        })
        vm.getCustomLocations()
        vm.getSuppliers()
        fetchMtnDetails()

        vm.supplier.observe(viewLifecycleOwner, Observer {
            supplierList = it/*.filter { data -> data.bcApprover?.isNotEmpty()!! }*/.toMutableList()

            val supplierData =
                supplierList/*.filter { data -> data.vendorCode.startsWith("2", true) }*/
            val suppliers =
                supplierData.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            val productAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suppliers)
            binding.tvVendorValue.threshold = 1
            binding.tvVendorValue.setAdapter(productAdapter)
            binding.tvVendorValue.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, _, _ ->
                    enableProceed()
                    it.forEach { material ->
                        if (material.vendorCode == binding.tvVendorValue.text.toString()
                                .split(" - ")[0]
                        ) {
                            vm.vegaCoffeeReceivingData.transportVendorCode = material.vendorCode
                            vm.vegaCoffeeReceivingData.transportVendorName = material.vendorName
                        }
                    }
                }
        })

        binding.tvDriverNoValue.onChange { enableProceed() }
        binding.tvDriverNameValue.onChange { enableProceed() }
        binding.tvTruckNoValue.onChange { enableProceed() }
        binding.btProceed.setOnClickListener { moveToSummary() }
        binding.btViewImages.setOnClickListener {
            if (selectedOBD.mtntWbid.isNullOrEmpty()) {
                showSnack(getString(R.string.error_obd_n))
            } else {
                vm.getDMSUploadedImages(selectedOBD.mtntWbid, selectedOBD.supplyingPlantId)
            }
        }
        binding.btSave.setOnClickListener {
            vm.vegaCoffeeReceivingData.vehicleNumber = binding.tvTruckNoValue.text.toString()
            vm.vegaCoffeeReceivingData.driverName = binding.tvDriverNameValue.text.toString()
            vm.vegaCoffeeReceivingData.contactNumber = binding.tvDriverNoValue.text.toString()
            vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, VegaCoffeeReceiveLots())
            activity?.finish()
        }
        vm.offloadingMtnr.observe(viewLifecycleOwner, Observer { updateOBDDetails(it) })
    }

    private fun moveToSummary() {
        binding.tvVendorValue.isCursorVisible = false
        vm.vegaCoffeeReceivingData.vehicleNumber = binding.tvTruckNoValue.text.toString()
        vm.vegaCoffeeReceivingData.driverName = binding.tvDriverNameValue.text.toString()
        vm.vegaCoffeeReceivingData.contactNumber = binding.tvDriverNoValue.text.toString()
        validateInputs()

    }

    private fun getUploadedImages(data: Resource<GenericReqAndResp<List<VegaDMSImageResponse>>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    val response = data.data?.data
                    if (response != null && response.size > 0) {
                        //response.get(0).encodedString?.toByteArray()
                        /* val directory_path =
                                Environment.getExternalStorageDirectory().getAbsolutePath() + '/';
                        val file1 = File(directory_path)
                        if (!file1.exists()) {z
                            file1.mkdirs()
                        }
                        val targetPdf = directory_path + "receipt.pdf"
                        val filePath = File(targetPdf)*/

                        // val decodedString = Base64.decode(response?.get(0)?.encodedString, Base64.NO_WRAP);
                        // hideCustomLoading()
                        // var strBase64New = (response?.get(0)?.encodedString).toString().trim()?.replace("=", "").replace("\\", "/").replace("\\s".toRegex(), "")
                        // val decodedString = Base64.decode(strBase64New, Base64.NO_WRAP);
                        /*runOnUiThread {
                            binding.pdfView.fromBytes(decodedString).enableSwipe(true) // allows to block changing pages using swipe
                                .swipeHorizontal(false)
                                .enableDoubletap(true)
                                .defaultPage(0)
                                .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                                .password(null)
                                .scrollHandle(null)
                                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                                // spacing between pages in dp. To define spacing color, set view background
                                .spacing(0)
                                .invalidPageColor(Color.WHITE) // color of page that is invalid and cannot be loaded
                                .load()
                        }*/

                        /*  val decodedStringSample =
                              "JVBERi0xLjMNCiXi48/TDQoNCjEgMCBvYmoNCjw8DQovVHlwZSAvQ2F0YWxvZw0KL091dGxpbmVzIDIgMCBSDQovUGFnZXMgMyAwIFINCj4+DQplbmRvYmoNCg0KMiAwIG9iag0KPDwNCi9UeXBlIC9PdXRsaW5lcw0KL0NvdW50IDANCj4+DQplbmRvYmoNCg0KMyAwIG9iag0KPDwNCi9UeXBlIC9QYWdlcw0KL0NvdW50IDINCi9LaWRzIFsgNCAwIFIgNiAwIFIgXSANCj4+DQplbmRvYmoNCg0KNCAwIG9iag0KPDwNCi9UeXBlIC9QYWdlDQovUGFyZW50IDMgMCBSDQovUmVzb3VyY2VzIDw8DQovRm9udCA8PA0KL0YxIDkgMCBSIA0KPj4NCi9Qcm9jU2V0IDggMCBSDQo+Pg0KL01lZGlhQm94IFswIDAgNjEyLjAwMDAgNzkyLjAwMDBdDQovQ29udGVudHMgNSAwIFINCj4+DQplbmRvYmoNCg0KNSAwIG9iag0KPDwgL0xlbmd0aCAxMDc0ID4+DQpzdHJlYW0NCjIgSg0KQlQNCjAgMCAwIHJnDQovRjEgMDAyNyBUZg0KNTcuMzc1MCA3MjIuMjgwMCBUZA0KKCBBIFNpbXBsZSBQREYgRmlsZSApIFRqDQpFVA0KQlQNCi9GMSAwMDEwIFRmDQo2OS4yNTAwIDY4OC42MDgwIFRkDQooIFRoaXMgaXMgYSBzbWFsbCBkZW1vbnN0cmF0aW9uIC5wZGYgZmlsZSAtICkgVGoNCkVUDQpCVA0KL0YxIDAwMTAgVGYNCjY5LjI1MDAgNjY0LjcwNDAgVGQNCigganVzdCBmb3IgdXNlIGluIHRoZSBWaXJ0dWFsIE1lY2hhbmljcyB0dXRvcmlhbHMuIE1vcmUgdGV4dC4gQW5kIG1vcmUgKSBUag0KRVQNCkJUDQovRjEgMDAxMCBUZg0KNjkuMjUwMCA2NTIuNzUyMCBUZA0KKCB0ZXh0LiBBbmQgbW9yZSB0ZXh0LiBBbmQgbW9yZSB0ZXh0LiBBbmQgbW9yZSB0ZXh0LiApIFRqDQpFVA0KQlQNCi9GMSAwMDEwIFRmDQo2OS4yNTAwIDYyOC44NDgwIFRkDQooIEFuZCBtb3JlIHRleHQuIEFuZCBtb3JlIHRleHQuIEFuZCBtb3JlIHRleHQuIEFuZCBtb3JlIHRleHQuIEFuZCBtb3JlICkgVGoNCkVUDQpCVA0KL0YxIDAwMTAgVGYNCjY5LjI1MDAgNjE2Ljg5NjAgVGQNCiggdGV4dC4gQW5kIG1vcmUgdGV4dC4gQm9yaW5nLCB6enp6ei4gQW5kIG1vcmUgdGV4dC4gQW5kIG1vcmUgdGV4dC4gQW5kICkgVGoNCkVUDQpCVA0KL0YxIDAwMTAgVGYNCjY5LjI1MDAgNjA0Ljk0NDAgVGQNCiggbW9yZSB0ZXh0LiBBbmQgbW9yZSB0ZXh0LiBBbmQgbW9yZSB0ZXh0LiBBbmQgbW9yZSB0ZXh0LiBBbmQgbW9yZSB0ZXh0LiApIFRqDQpFVA0KQlQNCi9GMSAwMDEwIFRmDQo2OS4yNTAwIDU5Mi45OTIwIFRkDQooIEFuZCBtb3JlIHRleHQuIEFuZCBtb3JlIHRleHQuICkgVGoNCkVUDQpCVA0KL0YxIDAwMTAgVGYNCjY5LjI1MDAgNTY5LjA4ODAgVGQNCiggQW5kIG1vcmUgdGV4dC4gQW5kIG1vcmUgdGV4dC4gQW5kIG1vcmUgdGV4dC4gQW5kIG1vcmUgdGV4dC4gQW5kIG1vcmUgKSBUag0KRVQNCkJUDQovRjEgMDAxMCBUZg0KNjkuMjUwMCA1NTcuMTM2MCBUZA0KKCB0ZXh0LiBBbmQgbW9yZSB0ZXh0LiBBbmQgbW9yZSB0ZXh0LiBFdmVuIG1vcmUuIENvbnRpbnVlZCBvbiBwYWdlIDIgLi4uKSBUag0KRVQNCmVuZHN0cmVhbQ0KZW5kb2JqDQoNCjYgMCBvYmoNCjw8DQovVHlwZSAvUGFnZQ0KL1BhcmVudCAzIDAgUg0KL1Jlc291cmNlcyA8PA0KL0ZvbnQgPDwNCi9GMSA5IDAgUiANCj4+DQovUHJvY1NldCA4IDAgUg0KPj4NCi9NZWRpYUJveCBbMCAwIDYxMi4wMDAwIDc5Mi4wMDAwXQ0KL0NvbnRlbnRzIDcgMCBSDQo+Pg0KZW5kb2JqDQoNCjcgMCBvYmoNCjw8IC9MZW5ndGggNjc2ID4+DQpzdHJlYW0NCjIgSg0KQlQNCjAgMCAwIHJnDQovRjEgMDAyNyBUZg0KNTcuMzc1MCA3MjIuMjgwMCBUZA0KKCBTaW1wbGUgUERGIEZpbGUgMiApIFRqDQpFVA0KQlQNCi9GMSAwMDEwIFRmDQo2OS4yNTAwIDY4OC42MDgwIFRkDQooIC4uLmNvbnRpbnVlZCBmcm9tIHBhZ2UgMS4gWWV0IG1vcmUgdGV4dC4gQW5kIG1vcmUgdGV4dC4gQW5kIG1vcmUgdGV4dC4gKSBUag0KRVQNCkJUDQovRjEgMDAxMCBUZg0KNjkuMjUwMCA2NzYuNjU2MCBUZA0KKCBBbmQgbW9yZSB0ZXh0LiBBbmQgbW9yZSB0ZXh0LiBBbmQgbW9yZSB0ZXh0LiBBbmQgbW9yZSB0ZXh0LiBBbmQgbW9yZSApIFRqDQpFVA0KQlQNCi9GMSAwMDEwIFRmDQo2OS4yNTAwIDY2NC43MDQwIFRkDQooIHRleHQuIE9oLCBob3cgYm9yaW5nIHR5cGluZyB0aGlzIHN0dWZmLiBCdXQgbm90IGFzIGJvcmluZyBhcyB3YXRjaGluZyApIFRqDQpFVA0KQlQNCi9GMSAwMDEwIFRmDQo2OS4yNTAwIDY1Mi43NTIwIFRkDQooIHBhaW50IGRyeS4gQW5kIG1vcmUgdGV4dC4gQW5kIG1vcmUgdGV4dC4gQW5kIG1vcmUgdGV4dC4gQW5kIG1vcmUgdGV4dC4gKSBUag0KRVQNCkJUDQovRjEgMDAxMCBUZg0KNjkuMjUwMCA2NDAuODAwMCBUZA0KKCBCb3JpbmcuICBNb3JlLCBhIGxpdHRsZSBtb3JlIHRleHQuIFRoZSBlbmQsIGFuZCBqdXN0IGFzIHdlbGwuICkgVGoNCkVUDQplbmRzdHJlYW0NCmVuZG9iag0KDQo4IDAgb2JqDQpbL1BERiAvVGV4dF0NCmVuZG9iag0KDQo5IDAgb2JqDQo8PA0KL1R5cGUgL0ZvbnQNCi9TdWJ0eXBlIC9UeXBlMQ0KL05hbWUgL0YxDQovQmFzZUZvbnQgL0hlbHZldGljYQ0KL0VuY29kaW5nIC9XaW5BbnNpRW5jb2RpbmcNCj4+DQplbmRvYmoNCg0KMTAgMCBvYmoNCjw8DQovQ3JlYXRvciAoUmF2ZSBcKGh0dHA6Ly93d3cubmV2cm9uYS5jb20vcmF2ZVwpKQ0KL1Byb2R1Y2VyIChOZXZyb25hIERlc2lnbnMpDQovQ3JlYXRpb25EYXRlIChEOjIwMDYwMzAxMDcyODI2KQ0KPj4NCmVuZG9iag0KDQp4cmVmDQowIDExDQowMDAwMDAwMDAwIDY1NTM1IGYNCjAwMDAwMDAwMTkgMDAwMDAgbg0KMDAwMDAwMDA5MyAwMDAwMCBuDQowMDAwMDAwMTQ3IDAwMDAwIG4NCjAwMDAwMDAyMjIgMDAwMDAgbg0KMDAwMDAwMDM5MCAwMDAwMCBuDQowMDAwMDAxNTIyIDAwMDAwIG4NCjAwMDAwMDE2OTAgMDAwMDAgbg0KMDAwMDAwMjQyMyAwMDAwMCBuDQowMDAwMDAyNDU2IDAwMDAwIG4NCjAwMDAwMDI1NzQgMDAwMDAgbg0KDQp0cmFpbGVyDQo8PA0KL1NpemUgMTENCi9Sb290IDEgMCBSDQovSW5mbyAxMCAwIFINCj4+DQoNCnN0YXJ0eHJlZg0KMjcxNA0KJSVFT0YNCg=="
                         */ /*val decodedString = Base64.decode(decodedStringSample, Base64.NO_WRAP);
                        binding.pdfView.fromBytes(decodedString).load();*/

                        val targetPdf = context?.cacheDir?.absolutePath + '/' + "receipt.pdf"
                        base64ToFile(targetPdf, response.get(0).encodedString)
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

    private fun updateTruckDetail(response: VegaReceiving?) {
        if (!response?.vehicleNumber.isNullOrEmpty()) {
            binding.tvTruckNoValue.setText(response?.vehicleNumber)
            binding.tvTruckNoValue.isEnabled = true
        } else binding.tvTruckNoValue.isEnabled = true
        if (!response?.driverName.isNullOrEmpty()) {
            binding.tvDriverNameValue.setText(response?.driverName)
            binding.tvDriverNameValue.isEnabled = true
        } else binding.tvDriverNameValue.isEnabled = true
        if (!response?.contactNumber.isNullOrEmpty()) {
            binding.tvDriverNoValue.setText(response?.contactNumber)
            binding.tvDriverNoValue.isEnabled = true
        } else binding.tvDriverNoValue.isEnabled = true

        // vm.getDMSUploadedImages(selectedOBD.mtntWbid, selectedOBD.supplyingPlantId)
        //vm.getDMSUploadedImages("216141000497", "6141")
    }

    private fun updateOBDDetails(data: VegaCoffeeReceivingMtnrWithLots?) {
        val fragment = activity?.supportFragmentManager?.findFragmentById(R.id.flContainer)
        setValueEmpty(fragment)
        if (data != null) {

            if (data.receiving.transportVendorCode?.isNotEmpty()!!) {
                when (fragment) {
                    is VegaNigeriaMtnrConsignmentFragment -> {
                        binding.tvVendorValue.setText(
                            data.receiving.transportVendorCode.plus(" - ")
                                .plus(data.receiving.transportVendorName)
                        )
                    }
                }
            } else
                binding.tvVendorValue.setText("")
            binding.tvTruckNoValue.setText(data.receiving.vehicleNumber)
            binding.tvDriverNameValue.setText(data.receiving.driverName)
            binding.tvDriverNoValue.setText(data.receiving.contactNumber)
            vm.vegaCoffeeReceivingData = data.receiving
            if (data.lineItems.size > 0) {
                batchList.forEach {
                    var edWeight = 0.0
                    val bags =
                        data.lineItems.filter { it2 -> it2.lots.mtnNumber.equals(it.mtnNumber) }
                            .filter { it1 -> it1.lots.batch.equals(it.batch) }
                    if (((bags.size == 1) && (vm.vegaCoffeeReceivingData.startTime?.isEmpty() == true))) vm.vegaCoffeeReceivingData.startTime =
                        DateUtils.getCurrentTimeInMills().toString()
                    if (bags.size > 0) edWeight =
                        bags[0].bagItem.filter { it2 -> it2.mtnNumber.equals(it.mtnNumber) }
                            .filter { it1 -> it1.batchNumber.equals(it.batch) }
                            .sumByDouble { it2 -> it2.netWeight.toDouble() }
                    it.editedWeight = edWeight.toString()
                    it.bagCount = data.lineItems[0].bagItem.filter { it1->it1.bagType.equals(JUTE_BAG, ignoreCase = true) }.sumOf { it.bagCount.toInt() }
                }
                setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
                // vm.getDMSUploadedImages(selectedOBD.mtntWbid, selectedOBD.supplyingPlantId)
                //vm.getDMSUploadedImages("216141000497", "6141")
            } else {
                setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
                // vm.getDMSUploadedImages(selectedOBD.mtntWbid, selectedOBD.supplyingPlantId)
                //vm.getDMSUploadedImages("216141000497", "6141")
            }
        } else {
            vm.getWeighBridgeIdDetail(selectedOBD.mtntWbid)
            setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
        }
    }


    private fun fetchMtnDetails() {
        vm.warehouse.observe(viewLifecycleOwner, Observer { processApiResult(it) })
        vm.fetchWarehouseWithMtns()
    }

    private fun processApiResult(data: Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> saveResult(it.data)
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> showErrorDialogWithFAQLink(
                    requireContext(),
                    it.error.toString()
                )
            }
        }
    }

    private fun saveResult(data: GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>?) {
        hideLoading()
        data?.data?.let {
            wareHouseList = data.data.storageLocationLst.distinct().toMutableList()
            allOBDList = data.data.mtns.toMutableList()
            allBatchList = data.data.batchDetails.toMutableList()
            allBatchList.forEachIndexed { index, s ->
                allBatchList[index].weight = allBatchList[index].weight.toString()
                   /*  allBatchList[index].weight = convertMtToKg(
                        allBatchList[index].weight.toString(),
                        allBatchList[index].uom.toString()
                    )*/
            }
        }
    }

    private fun showSingleSelectDialog(isWh: Boolean, title: String, isVendor: Boolean) {
        val list: List<String>
        if (isVendor) {
            list = customLocationList.map {
                it.procureLocationCode.plus(" - ").plus(it.procureLocationName)
            } as ArrayList<String>

        } else if (isWh) {
            list = wareHouseList.map {
                it.plant.plus(" - ").plus(it.storageLocationCode).plus(" - ").plus(it.storageLocationName)
            } as ArrayList<String>
        } else {
            list = filteredOBDList.map { it.mtnNumber }.toSet().toMutableList()
        }
        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isWh, isVendor, false,
                list,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {
        customDialog?.dismiss()
        enableProceed()
        if (isWh) {
            val whId = data.split("-")
            binding.tvWhValue.text = data
            wareHouseList.forEach {
                if (it.storageLocationCode == whId[1].trim() && it.storageLocationName == whId[2].trim()) {
                    vm.vegaCoffeeReceivingData.plantId = it.plant
                }
            }
            vm.vegaCoffeeReceivingData.supplierCode = whId[1].trim()
            vm.vegaCoffeeReceivingData.supplierName = whId[2].trim()
            selectedSendingLocation = wareHouseList.single {
                it.storageLocationName == whId[2].trim()
            }
            filteredOBDList =
                allOBDList.filter { it.storageLocationCode == selectedSendingLocation.storageLocationCode && it.supplyingPlantId == selectedSendingLocation.plant }
                    .toMutableList()
        } else if (!isVendor) {
            binding.tvstoValue.text = data
            vm.vegaCoffeeReceivingData.mtnCode = data
            vm.vegaCoffeeReceivingData.delivery = data
            selectedOBD = filteredOBDList.filter { it.mtnNumber == data }[0]
            batchList =
                allBatchList.filter { it.mtnNumber == selectedOBD.mtnNumber }.toMutableList()
            vm.vegaCoffeeReceivingData.purchaseDocNum =
                if (batchList.size > 0) batchList[0].purchaseOrder else selectedOBD.purchaseOrder
            vm.vegaCoffeeReceivingData.purchaseDocDesc =
                if (batchList.size > 0) batchList[0].ebelp else selectedOBD.ebelp
            vm.getOBDDetails(data)
        } else {
            val whId = data.split("-")
            binding.tvReceivingValue.text = data
            vm.vegaCoffeeReceivingData.storageLocationCode = whId[0].trim()
            vm.vegaCoffeeReceivingData.storageLocationName = whId[1].trim()
            selectedReceivingLocation =
                customLocationList.single { it.procureLocationCode == whId[0].trim() }
            customLocationList.forEach {
                if (it.procureLocationCode == whId[0].trim() && it.procureLocationName == whId[1].trim()) {
                    vm.vegaCoffeeReceivingData.materialCode = it.plant
                }
            }
        }
    }

    /*private fun updateTruckDetails(isUpdate: Boolean) {
        if (isUpdate) {
            *//*binding.tvTruckNoValue.setText()
              binding.tvDriverNameValue.setText()
              binding.tvVendorValue.text = *//*
        } else {
            binding.tvTruckNoValue.setText("")
            binding.tvDriverNameValue.setText("")
            binding.tvVendorValue.setText("")
        }
    }*/

    private fun setUpAdapter(list: ArrayList<VegaCoffeeReceiveLots>) {
        var lineItems = mutableListOf<VegaCoffeeReceiveLots>()
        if (editLotId.isNotEmpty()) {
            enableDisableItem(false)
            lineItems =
                list.filter { it.batch.equals(editLotId) } as MutableList<VegaCoffeeReceiveLots>
        } else {
            enableDisableItem(true)
            lineItems = list
        }
        binding.rvList.setUpAdapter(
            lineItems,
            R.layout.item_nigeria_mtnr_weighscale_lot_card_layout,
            ItemNigeriaMtnrWeighscaleLotCardLayoutBinding::inflate,
            { item, pos, bindItem ->
                bindItem.llWeightLoss.gone()
                bindItem.tvScaleLotValue.text = item.batch
                bindItem.tvStLocationValue.text = item.storageLocationCode
                bindItem.tvScaleWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(item.uom)
                bindItem.tvScaleGradeValue.text = item.materialName
                var receivingKg = item.editedWeight
                val editedWeight =
                    if (receivingKg.isNullOrEmpty()) "0.0" else receivingKg.toString().toDouble()
                        .formatThreeDigits()
                bindItem.ivEdit.visibility = View.GONE
                if(item.uom.equals(UNIT_EA, ignoreCase = true)){
                    bindItem.tvAddWeight.visibility = View.INVISIBLE
                    bindItem.tvScaleDispatchValue.text = item.bagCount.toString()
                    bindItem.tvDispatchUOMValue.text = UNIT_EA

                } else {
                    bindItem.tvAddWeight.visibility = View.VISIBLE
                    bindItem.tvScaleDispatchValue.text = editedWeight
                    bindItem.tvDispatchUOMValue.text = "KG"
                }
                bindItem.tvAddWeight.setOnClickListener {
                    binding.tvVendorValue.isCursorVisible = false
                    addWeightPosition = pos
                    vm.vegaCoffeeReceivingData.vehicleNumber =
                        binding.tvTruckNoValue.text.toString()
                    vm.vegaCoffeeReceivingData.driverName =
                        binding.tvDriverNameValue.text.toString()
                    vm.vegaCoffeeReceivingData.contactNumber =
                        binding.tvDriverNoValue.text.toString()
                    item.delivery = item.mtnNumber
                    vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, item)
                    callBack?.replaceFragment(ADD_WEIGHT, item)
                }
            })
    }

    private fun enableDisableItem(flag: Boolean) {
        binding.tvWhValue.isEnabled = flag
        binding.tvstoValue.isEnabled = flag
        binding.tvReceivingValue.isEnabled = flag
        binding.tvVendorValue.isEnabled = flag
        binding.tvTruckNoValue.isEnabled = flag
        binding.tvDriverNameValue.isEnabled = flag
        binding.tvDriverNoValue.isEnabled = flag
    }

    private fun setValueEmpty(fragment: Fragment?) {
        binding.tvTruckNoValue.setText("")
        binding.tvDriverNameValue.setText("")
        binding.tvDriverNoValue.setText("")
        when (fragment) {
            is VegaNigeriaMtnrConsignmentFragment -> binding.tvVendorValue.setText("")
        }
    }

    private fun validateProceed() {
        if (batchList.isNotEmpty()) {
            if (validateEmptyWeight()) {
                if (validateLotWeight()) {
                    if (validateJuteBagAvailabilityAndCount()){
                        if (editLotId.isEmpty()) showRemarkDialog() else {
                            batchList.forEach {
                                it.delivery = vm.vegaCoffeeReceivingData.delivery
                                vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, it)
                            }
                            callBack?.replaceFragment(
                                MTNR_WEIGHSCALE_SUMMARY,
                                vm.vegaCoffeeReceivingData
                            )
                        }
                } else Toast.makeText(
                        activity,
                        getString(R.string.jute_bag_count_warning_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                } else Toast.makeText(
                    activity,
                    getString(R.string.lot_more_weight),
                    Toast.LENGTH_SHORT
                ).show()
            } else Toast.makeText(
                activity,
                getString(R.string.less_weight_error),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                activity,
                getString(R.string.please_add_lot),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showRemarkDialog() {

        showDialog(getString(R.string.end_load_msg), object : DialogClick {
            override fun onPositive(remark: String) {
                if (remark.isEmpty()) Toast.makeText(
                    activity,
                    getString(com.olam.warehouse.presentation.R.string.enter_remark),
                    Toast.LENGTH_SHORT
                )
                    .show()
                else {
                    val endTime = System.currentTimeMillis()
                    vm.vegaCoffeeReceivingData.endTime = endTime.toString()
                    val duration =
                        endTime.minus(vm.vegaCoffeeReceivingData.startTime?.toLong() ?: 0)
                    vm.vegaCoffeeReceivingData.remarks = remark
                    vm.vegaCoffeeReceivingData.turnAroundTime =
                        TimeUnit.MILLISECONDS.toMinutes(duration).toString()
                    batchList.forEach {
                        it.delivery = vm.vegaCoffeeReceivingData.delivery
                        vm.saveMtnrReceivingLots(vm.vegaCoffeeReceivingData, it)
                    }
                    callBack?.replaceFragment(MTNR_WEIGHSCALE_SUMMARY, vm.vegaCoffeeReceivingData)
                }
            }

        }, true, vm.vegaCoffeeReceivingData.remarks.toString())
    }


    fun updateAddWeight(weight: String) {
        val split = weight.split(" ")
        batchList[addWeightPosition].editedWeight = split[0]
        batchList[addWeightPosition].editedUOM = split[1]
        binding.rvList.adapter?.notifyItemChanged(addWeightPosition)
        val come: Int? =
            split[0].toDouble().compareTo(batchList[addWeightPosition].weight?.toDouble() ?: 0.0)
        batchList[addWeightPosition].isLowerWeight = come ?: 0 <= 0
    }

    private fun validateInputs() {
        when {
            vm.vegaCoffeeReceivingData.vehicleNumber.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
            vm.vegaCoffeeReceivingData.driverName.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver))
            vm.vegaCoffeeReceivingData.storageLocationCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_loc))
            else -> validateProceed()
        }
    }

    private fun validateLotWeight(): Boolean {
        val selected = batchList.filter { !it.isLowerWeight }
        moreWeightBatches = selected.map { it.batch }.toString().replace("[", "").replace("]", "")
        return selected.isEmpty()
    }

    private fun validateJuteBagAvailabilityAndCount() : Boolean{
        val juteBag = batchList.any { it?.uom.equals(UNIT_EA, ignoreCase = true) }
        if(juteBag){
            val juteBagCount = batchList.filter { it?.uom.equals(UNIT_EA, ignoreCase = true) }.sumOf { it.bagCount }
            return juteBagCount > 0
        } else {
           return true
        }
    }

    private fun validateEmptyWeight(): Boolean {
        val emptyWeight =
            batchList.filter {
                it.editedWeight.equals("0.0") || it.editedWeight.equals("0") || it.editedWeight.equals(
                    ""
                )
            }
        return emptyWeight.isEmpty()
    }

    private fun enableProceed() {
        val enable =
            (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverName.text.isNotEmpty()
                    && binding.tvTruckNoValue.text.toString().isNotEmpty()
                    && binding.tvReceivingValue.text.isNotEmpty())
        if (enable) {
            binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryHeadOfi))
            binding.btSave.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryHeadOfi))
        } else {
            binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
            binding.btSave.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btProceed.isEnabled = enable
        binding.btSave.isEnabled = enable
    }

    fun getBack() {
        editLotId = ""
        vm.getOBDDetails(vm.vegaCoffeeReceivingData.delivery)
    }

    fun editLot(vegaCoffeeReceiveLots: VegaCoffeeReceiveLots) {
        editLotId = vegaCoffeeReceiveLots.batch
        vm.getOBDDetails(vegaCoffeeReceiveLots.mtnNumber)
    }

    private fun updateMandatory() {
        binding.tvDest.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.sending_wh)) { mandatoryStars() } }
        binding.tvsto.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.obd_number)) { mandatoryStars() } }
        binding.tvReceivingWH.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.receiving_locations)) { mandatoryStars() } }
        binding.tvTruckNo.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_number)) { mandatoryStars() } }
        binding.tvDriverName.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.driver_name)) { mandatoryStars() } }
    }

    @Throws(IOException::class)
    fun base64ToFile(path: String?, strBase64: String?) {
        /*val filePath = File(path)
        val decodedString: ByteArray;
        val size = filePath.length().toInt()
        val bytes = ByteArray(size)
        try {
            val buf = ByteArrayOutputStream()
            buf.toByteArray()
            decodedString = Base64.decode(bytes,Base64.NO_WRAP)
            val base64String = String(decodedString)
           // buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }*/
        // var strBase64New = strBase64.toString().trim()?.replace("=", "").replace("\\", "/").replace("\\s".toRegex(), "")
        val bytes: ByteArray = Base64.decode(strBase64, Base64.NO_WRAP)
        byteArrayTofile(path, bytes)


        /* val base64String = "data:image/png;base64,".plus(strBase64New)
         val base64Image = base64String.split(",".toRegex()).toTypedArray()[1]
         val decodedString = Base64.decode(base64Image, Base64.NO_WRAP)*/
        //val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        // binding.photoView.visible()
        // binding.photoView.setImageBitmap(decodedByte)

        /*  hideCustomLoading()
          val imageBytes = Base64.decode(strBase64New, Base64.NO_WRAP)
          val decodedImage: Bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
          binding.photoView.visible()
          binding.photoView.setImageBitmap(decodedImage)*/
        //Glide.with(context).load(imageBytes).crossFade().fitCenter().into(binding.photoView);
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
        /* val arrayInputStream = ByteArrayInputStream(bytes)
         val bitmap = BitmapFactory.decodeStream(arrayInputStream)
         hideCustomLoading()*/
        /*val imageViewWidth = 347
        val imageViewHeight = 413

        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        val bitmapWidth = bmOptions.outWidth
        val bitmapHeight = bmOptions.outHeight

        val scaleFactor = min(a = bitmapWidth / imageViewWidth, b = bitmapHeight / imageViewHeight)

        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor*/

        //val imageBytes = Base64.decode(path, Base64.NO_WRAP)
        //val decodedImage: Bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size,bmOptions)
        //image.setImageBitmap(decodedImage)
        /* val decodedString: ByteArray = Base64.decode(strBase64, Base64.DEFAULT)
         val decodedByte: Bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)*/

        /* val encodeByte = Base64.decode(strBase64?.toByteArray(), Base64.NO_WRAP)
         val options = BitmapFactory.Options()
         options.inPurgeable = true
         var image = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size, options)
         *//*if (image.height <= 400 && image.width <= 400) {
            return strBase64
        }*//*
        image = Bitmap.createScaledBitmap(image, 200, 200, false)
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, baos)
       // val b = baos.toByteArray()
        System.gc()*/
        //  return Base64.encodeToString(b, Base64.NO_WRAP)
        /* hideCustomLoading()
         val imageBytes = Base64.decode(path, Base64.NO_WRAP)
         val decodedImage: Bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

         binding.photoView.visible()
         binding.photoView.setImageBitmap(decodedImage)*/
    }

    @Throws(IOException::class)
    fun byteArrayTofile(path: String?, bytes: ByteArray?) {
        /* val imagefile = File(path)
         val dir = File(imagefile.getParent())
         if (!dir.exists()) {
             dir.mkdirs()
         }*/
        val file1 = File(path)
        if (file1.exists()) {
        } else {
            file1.createNewFile()
        }
        val output = BufferedOutputStream(FileOutputStream(file1))
        //val fos = FileOutputStream(file1)
        if (bytes != null) {
            output.write(bytes, 0, bytes.size)
        }
        output.close()
        //output.close();
    }
}
