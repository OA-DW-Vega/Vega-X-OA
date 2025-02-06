package com.olam.warehouse.vegax.nigeriaweighment.ui.truckin.supplier

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.mandatoryStars
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.nigeriaweighment.R
import com.olam.warehouse.vegax.nigeriaweighment.databinding.FragmentVegaNigeriaSupplierTruckinBinding
import com.olam.warehouse.vegax.nigeriaweighment.ui.VegaNigeriaReceivingViewModel
import com.olam.warehouse.vegax.nigeriaweighment.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaSupplierFragment : BaseFragment(), VegaSingleSelectCommonListener {

    private var receivingData = VegaQualityWBDetails()
    private lateinit var supplierList :List<VegaVendor>
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null


    private val vm: VegaNigeriaReceivingViewModel by viewModel()
    private lateinit var binding: FragmentVegaNigeriaSupplierTruckinBinding
    override val layoutResourceId = R.layout.fragment_vega_nigeria_supplier_truckin

    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            receivingData: VegaReceiving
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(receivingData: VegaQualityWBDetails) = VegaNigeriaSupplierFragment().putArgs {
            putParcelable(RECEIVING_DATA, receivingData)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNigeriaSupplierTruckinBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("receiving/ui/truckin/supplier/VegaSupplierFragment").title("Receiving")
            .with(tracker)
        initUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun initUI() {
        receivingData = arguments?.getParcelable(RECEIVING_DATA)!!
        binding.tvProduct.setText(receivingData.materialName)
        binding.tvProduct.isEnabled = false
        binding.tvSupplier.setText(receivingData.supplierName)
        binding.tvSupplier.isEnabled = false
        binding.tvReceivingLocation.text = receivingData.storageLocationCode
        binding.tvReceivingLocation.isEnabled = false
        binding.tvTruckNo.setText(receivingData.vehicleNumber)
        binding.tvTruckNo.isEnabled = false
        binding.tvDriverName.setText(receivingData.driverName)
        binding.tvDriverName.isEnabled = false
        binding.tvPhoneNo.setText(receivingData.contactNumber)
        binding.tvPhoneNo.isEnabled = false
        binding.tvGrossWeight.setText(receivingData.grossWeight)
        binding.tvGrossWeight.isEnabled = false
        binding.tvNetWeight.setText(receivingData.netWeight)
        binding.tvNetWeight.isEnabled = false
        binding.tvTareWeight.setText(receivingData.bagWeight)
        binding.tvTareWeight.isEnabled = false

        binding.tvSupplier.setOnClickListener {
            showSingleSelectDialog(getString(R.string.select_supplier), SUPPLIER)
        }

        if (receivingData.weighBridgeId.isNotEmpty()) {
            vm.weighBridgeId.observe(viewLifecycleOwner, Observer { updateValueUI(it) })
            vm.getWeighBridgeIdDetail(receivingData.weighBridgeId)
        }

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            supplierList = it
        })
        vm.getSuppliers()

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            custonLocationList = it.toMutableList()
            custonLocationList.forEach {
                if (it.procureLocationCode.equals(receivingData.storageLocationCode)) {
                    binding.tvReceivingLocation.text =
                        receivingData.storageLocationCode.plus("-").plus(it.procureLocationName)
                    receivingData.storageLocation = it.procureLocationName
                }
            }
        })
        vm.getCustomLocations()

        binding.btnOk.setOnClickListener {
            val intent = Intent(requireContext(), HomeActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

    }

    private fun updateValueUI(response: Resource<GenericReqAndResp<VegaReceiving>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()

                binding.tvDriverName.setText(response.data?.data?.driverName)
                binding.tvDriverName.isEnabled = false
                binding.tvPhoneNo.setText(response.data?.data?.contactNumber)
                binding.tvPhoneNo.isEnabled = false
                binding.tvGrossWeight.setText((response.data?.data?.grossWeight + " "
                        ).plus(response.data?.data?.unitsOfMeasure))
                binding.tvGrossWeight.isEnabled = false
                binding.tvNetWeight.setText((response.data?.data?.netWeight + " "
                        ).plus(response.data?.data?.unitsOfMeasure))
                binding.tvNetWeight.isEnabled = false
                binding.tvTareWeight.setText((response.data?.data?.tareWeight + " "
                        ).plus(response.data?.data?.unitsOfMeasure))
                binding.tvTareWeight.isEnabled = false

            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
            else -> {}
        }
    }

    override fun clickOnItem(data: String, currentFlag: String) {
        customDialog?.dismiss()
        when(currentFlag){
            SUPPLIER->{
                supplierList.forEach { vendor ->
                    if (vendor.vendorCode == binding.tvSupplier.text.toString().split(" - ")[0]) {
                        receivingData.supplierCode = vendor.vendorCode
                        receivingData.supplierName = vendor.vendorName
                    }
                    return
                }

            }
        }
    }

    private fun showSingleSelectDialog(title: String, currentFlag: String) {
        val list= ArrayList<String>()
        when (currentFlag) {
            SUPPLIER->{
                list.clear()
                list.addAll(supplierList.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) })
            }

        }
        customDialog =
            VegaCommonSingleSelectDialogWithSearch(
                title,
                currentFlag,
                list,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }


}
