package com.olam.warehouse.vegax.gateentrynigeria.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
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
import com.olam.warehouse.vegax.gateentrynigeria.R
import com.olam.warehouse.vegax.gateentrynigeria.data.domain.model.VegaGateEntryNigeriaPost
import com.olam.warehouse.vegax.gateentrynigeria.data.domain.model.VegaGateEntryNigeriaResponse
import com.olam.warehouse.vegax.gateentrynigeria.databinding.FragmentGateEntryNigeriaSummaryBinding
import com.olam.warehouse.vegax.gateentrynigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 3/9/2020.
 */
class VegaGateEntryNigeriaSummaryFragment : BaseFragment() {

    private var gateEntryData = VegaGateEntry()
    private var postData = mutableListOf<VegaGateEntry>()
    private var plantDetails = Plant()

    private val vm: VegaGateEntryNigeriaViewModel by viewModel()
    private lateinit var binding: FragmentGateEntryNigeriaSummaryBinding
    override val layoutResourceId = R.layout.fragment_gate_entry_nigeria_summary

    companion object {
        fun newInstance(
            gateEntryData: VegaGateEntry,
            plantDetails: Plant
        ) = VegaGateEntryNigeriaSummaryFragment().putArgs {
            putParcelable(GATE_ENTRY_DATA, gateEntryData)
            putParcelable(GATE_ENTRY_PLANT_DETAILS, plantDetails)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentGateEntryNigeriaSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("gateentryNigeria/ui/VegaGateEntrySummaryFragment").title("Gate Entry")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        gateEntryData = arguments?.getParcelable(GATE_ENTRY_DATA)!!
        plantDetails = arguments?.getParcelable(GATE_ENTRY_PLANT_DETAILS)!!
        if (gateEntryData.weighBridgeType == PROCURE) {
            binding.llSupplier.visible()
            binding.llMtnr.gone()
        } else {
            binding.llSupplier.gone()
            binding.llMtnr.visible()
        }

        binding.tvProduct.text =
            gateEntryData.materialCode.plus("-").plus(gateEntryData.materialName)
        binding.tvSupplier.text =
            gateEntryData.supplierCode.plus("-").plus(gateEntryData.supplierName)
        binding.tvSupplierZone.text = gateEntryData.supplierZone
        binding.tvObdNumber.text = gateEntryData.delivery
        binding.tvDispatchWarehouse.text = gateEntryData.supplierName
        binding.tvTruckNo.text = gateEntryData.vehicleNumber
        binding.tvDriverName.text = gateEntryData.driverName
        binding.tvProcurementType.text = gateEntryData.procurementType
        binding.tvPhoneNo.text = gateEntryData.contactNumber
        binding.tvReceivingLocation.text =
            gateEntryData.storageLocationCode.plus("-").plus(gateEntryData.storageLocationName)
        binding.tvPlant.text = gateEntryData.plantId.plus("-").plus(gateEntryData.plantName)
        binding.tvWeight.text = gateEntryData.approximateWeight.plus(" KG")
        binding.tvMtntNumber.text = gateEntryData.mtnCode
        binding.tvNoOfBags.text = gateEntryData.tempBagCount
        binding.tvDate.text = gateEntryData.erdat?.let { it1 ->
            DateUtils.getUTCDateTime(
                it1,
                App.getAppContext()
            )
        }
        if (gateEntryData.imagePath?.isNotEmpty()!! && gateEntryData.imagesList.size > 0) {
            binding.photoView.visible()
            binding.photoView.setImageBitmap(setScaledBitmap(gateEntryData.imagesList.get(0)))
            if (gateEntryData.imagesList.size > 1 && gateEntryData.imagesList.size >= 2) {
                binding.photoView1.visible()
                binding.photoView1.setImageBitmap(setScaledBitmap(gateEntryData.imagesList.get(1)))
            }
            if (gateEntryData.imagesList.size > 2 && gateEntryData.imagesList.size == 3) {
                binding.photoView2.visible()
                binding.photoView2.setImageBitmap(setScaledBitmap(gateEntryData.imagesList.get(2)))
            }
        }

        binding.btnConfirm.setOnClickListener { showConfirmDialog() }
        binding.tvViewImage.setOnClickListener{
        }

        vm.gateEntry.observe(viewLifecycleOwner, Observer { updateUI(it) })

    }

    private fun setScaledBitmap(imagePath: String): Bitmap? {
        try {
            val imageViewWidth = 100
            val imageViewHeight = 100

            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, bmOptions)
            val bitmapWidth = bmOptions.outWidth
            val bitmapHeight = bmOptions.outHeight

            val scaleFactor = Math.min(bitmapWidth / imageViewWidth, bitmapHeight / imageViewHeight)

            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor

            return BitmapFactory.decodeFile(imagePath, bmOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_gate_entry)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(R.string.cancel),
                {
                    postGateEntry()
                },
                { dismiss() })
        }
    }

    private fun updateUI(data: Resource<GenericReqAndResp<VegaGateEntryNigeriaResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> prepareSuccessData(it.data?.data?.wbId)
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun postGateEntry() {
        postData.clear()
        var sampleID = generateSampleID()
        gateEntryData.challan = sampleID.toString()
        postData.add(gateEntryData)
        postData.forEachIndexed { index, vegaReceiving -> vegaReceiving.item = index.inc().toString() }
        if (AppUtils.isOnline()) {
//            vm.postGateEntryData(VegaGateEntryNigeriaPost(getCurrentKey(), getPlantDetails(), postData))
            vm.postGateEntryData(
                VegaGateEntryNigeriaPost(
                    getCurrentKey(),
                    plantDetails,
                    true,
                    GATE_ENTRY_PENDING,
                    postData
                )
            )
        }
    }

    private fun generateSampleID(): Long {
        var sampleID = createRandomInteger(1000000000, 9999999999L, Random)
//        getRandomNumber(1000000000,9999999999L)
        return sampleID
    }

    private fun prepareSuccessData(wbId: String?) {
        hideLoading()
        moveToSuccessPage(wbId)
    }

    private fun moveToSuccessPage(wbId: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.success_entry))

//        intent.putExtra(
//            AppUtils.SUB_TITLE,
//            getString(R.string.sample_id_created).plus("\n ").plus(getString(R.string.weigh_bridge_id_is)).plus(" ")
//                .plus(wbId)
//        )
        intent.putExtra(
            AppUtils.SUB_TITLE,
            getString(R.string.weigh_bridge_id_is).plus(" ")
                .plus(wbId)
        )

        val lotlist = ArrayList<VegaCoffeeSalesLots>()
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
        )

//        intent.putExtra(UIUtils.FROM_Nigeria_GATEENTRY_COCOA, true)
//        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotlist)
//        intent.putExtra(AppUtils.PRINT_ENABLE, true)

        startActivity(intent)
        requireActivity().finish()
    }
}
