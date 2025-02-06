package com.olam.warehouse.vegax.nigeriaweighment.ui.truckout

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.mandatoryStars
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.nigeriaweighment.R
import com.olam.warehouse.vegax.nigeriaweighment.databinding.FragmentVegaNigeriaTruckoutAddWeightBinding
import com.olam.warehouse.vegax.nigeriaweighment.ui.VegaNigeriaReceivingViewModel
import com.olam.warehouse.vegax.nigeriaweighment.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.File
import java.io.IOException

class VegaNigeriaTruckOutAddWeightAndBagFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private var receivingData = VegaQualityWBDetails()
    private var mReceiving = mutableListOf<VegaQualityWBDetails>()

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            receivingData: VegaReceiving,
            mReceiving: MutableList<VegaReceiving>, isRoundoff: Boolean
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    private val vm: VegaNigeriaReceivingViewModel by viewModel()
    private lateinit var binding: FragmentVegaNigeriaTruckoutAddWeightBinding

    override val layoutResourceId = R.layout.fragment_vega_nigeria_truckout_add_weight

    companion object {
        fun newInstance(
            receivingData: VegaQualityWBDetails

        ) = VegaNigeriaTruckOutAddWeightAndBagFragment().putArgs {
            putParcelable(RECEIVING_DATA, receivingData)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNigeriaTruckoutAddWeightBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("receiving/ui/truckout/VegaTruckOutAddWeightAndBagFragment").title("Receiving")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        receivingData = arguments?.getParcelable(RECEIVING_DATA)!!

       /* binding.tvTruckOutLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_tar_weight)) { mandatoryStars() } }
        binding.tvBagDetails.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.bag_details)) { mandatoryStars() } }*/
        binding.tvTruckNo.text = receivingData.vehicleNumber
        binding.tvWeighBridgeId.text = receivingData.weighBridgeId
        vm.weighBridgeId.observe(viewLifecycleOwner, Observer { updateValueUI(it) })
        vm.getWeighBridgeIdDetail(receivingData.weighBridgeId)
        mReceiving.forEachIndexed { index, vegaReceiving ->
            when (index) {
                0 -> {
                    binding.tvBagType1.text = vegaReceiving.bagType
                    binding.etBagCount1.text = vegaReceiving.bagCount
                    binding.etBagWeight1.text = vegaReceiving.bagWeight
                }
                1 -> {
                    binding.tvBagType2.text = vegaReceiving.bagType
                    binding.etBagCount2.text = vegaReceiving.bagCount
                    binding.etBagWeight2.text = vegaReceiving.bagWeight
                }
                2 -> {
                    binding.tvBagType3.text = vegaReceiving.bagType
                    binding.etBagCount3.text = vegaReceiving.bagCount
                    binding.etBagWeight3.text = vegaReceiving.bagWeight
                }
            }
        }

        when (receivingData.weighBridgeType) {
            PROCURE -> {
                binding.tvdifference.text = SUPPLIER
            }
            else -> {
                receivingData.supplierCode = receivingData.customerNum
                binding.tvdifference.visibility = View.GONE
                binding.tvSupplierName.visibility = View.GONE
                binding.tvdifference.text = WAREHOUSE
            }
        }
        binding.tvTruckID.text =
            getString(R.string.truck_id).plus(": ")
                .plus(receivingData.vehicleNumber ?: receivingData.weighBridgeId)
        binding.tvSupplierName.text =
            receivingData.supplierCode.plus("-").plus(receivingData.supplierName)
        binding.tvWeight.text =
            receivingData.grossWeight.plus(" ").plus(receivingData.unitsOfMeasure)
        binding.tvDate.text = receivingData.erdat
        val times = receivingData.erdat?.split('(', ')')
        binding.tvDate.text = times?.get(1)?.let { it1 ->
            DateUtils.getUTCDateTime(
                it1,
                App.getAppContext()
            )
        }

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
                binding.tvGrossWeight.text = (response.data?.data?.grossWeight + " "
                        ).plus(response.data?.data?.unitsOfMeasure)
                binding.tvGrossWeight.isEnabled = false
                binding.tvNetWeight.text = (response.data?.data?.netWeight + " "
                        ).plus(response.data?.data?.unitsOfMeasure)
                binding.tvNetWeight.isEnabled = false
                binding.tvTareWeight.text = (response.data?.data?.tareWeight + " "
                        ).plus(response.data?.data?.unitsOfMeasure)
                binding.tvTareWeight.isEnabled = false
                binding.tvBagType1.text = response.data?.data?.bagType
                binding.tvBagType1.isEnabled = false
                binding.etBagCount1.text = response.data?.data?.bagCount
                binding.etBagCount1.isEnabled = false
                binding.etBagWeight1.text = response.data?.data?.bagWeight
                binding.etBagWeight1.isEnabled = false
                binding.tvBagType2.text = response.data?.data?.bagType1
                binding.tvBagType2.isEnabled = false
                binding.etBagCount2.text = response.data?.data?.bagCount1
                binding.etBagCount2.isEnabled = false
                binding.etBagWeight2.text = response.data?.data?.bagWeight1
                binding.etBagWeight2.isEnabled = false
                binding.tvBagType3.text = response.data?.data?.palletType
                binding.tvBagType3.isEnabled = false
                binding.etBagCount3.text = response.data?.data?.palletCount
                binding.etBagCount3.isEnabled = false
                binding.etBagWeight3.text = response.data?.data?.palletWeight
                binding.etBagWeight3.isEnabled = false
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
            else -> {}
        }
    }


}
