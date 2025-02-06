package com.olam.warehouse.vegax.stockrecon.ui.bagaudit

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.master.common.model.StorageLocation
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaSupplyStorageLocation
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.stockrecon.R
import com.olam.warehouse.vegax.stockrecon.databinding.FragmentVegaAuditTypeBinding
import com.olam.warehouse.vegax.stockrecon.ui.callback.VegaStockCallbackListener
import com.olam.warehouse.vegax.stockrecon.utils.AUDIT_TYPE
import com.olam.warehouse.vegax.stockrecon.utils.BAG_COUNT
import com.olam.warehouse.vegax.stockrecon.utils.PLANT_DETAILS
import com.olam.warehouse.vegax.stockrecon.utils.STOCK_RECON_INPROGRESS_COMPLETED
import com.olam.warehouse.vegax.stockrecon.utils.STORAGE_LOCATION_DETAILS
import com.olam.warehouse.vegax.stockrecon.vm.VegaStockReconViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaStockReconAuditTypeSelectFragment : BaseFragment(), VegaSingleSelectListener {

    override val layoutResourceId = R.layout.fragment_vega_audit_type
    private lateinit var binding: FragmentVegaAuditTypeBinding
    var plantList = mutableListOf<Plant>()

    //    var storageLocationList = mutableListOf<StorageLocation>()
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private var hyphen: String = "-"
    private var callBack: VegaStockCallbackListener? = null
    private val vm: VegaStockReconViewModel by viewModel()
    var storageLocation = ArrayList<VegaCustomStLocation>()
    var storageLocationList = ArrayList<VegaCustomStLocation>()


    companion object {
        fun newInstance() = VegaStockReconAuditTypeSelectFragment().putArgs {

        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaStockCallbackListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaAuditTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        plantList = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.PLANT_LIST, "")) as MutableList<Plant>
        initUI()
    }

    private fun initUI() {
        setUiValues()
        changeToBackCount()
        observer()
        clickListener()
    }

    private fun setUiValues() {
        binding.warehouseLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.warehouse_plant)) { mandatoryStars() } }
    }

    private fun observer() {
        /*fetching storage location from db*/
        vm.getStorageLocations()
        vm.storageLocation.observe(
            viewLifecycleOwner,
            Observer { storageLocation = it as ArrayList<VegaCustomStLocation> })
    }

    private fun clickListener() {
        binding.tvPlant.setOnClickListener { showSingleSelectDialog(true, getString(R.string.select_plant), false) }
        binding.tvLocation.setOnClickListener {
            showSingleSelectDialog(
                false,
                getString(R.string.select_location),
                false
            )
        }
        binding.btnProceed.setOnClickListener { validateProceed() }
    }

    private fun changeToBackCount() {
        binding.ivWeightCapture.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.ic_weight_capture)
        binding.ivBagCount.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.ic_bag_count_icon)
    }

    private fun showSingleSelectDialog(isPlant: Boolean, title: String, isVendor: Boolean) {
        var list = ArrayList<String>()
        var customlist = ArrayList<VegaSupplyStorageLocation>()
        list = if (isPlant) {
            plantList.map { it.plantId.plus(hyphen).plus(it.plantName) } as ArrayList<String>
        } else {
            storageLocationList.map {
                it.procureLocationCode.plus(hyphen).plus(it.procureLocationName)
            } as ArrayList<String>
        }
        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isPlant, isVendor, false,
                list,
                requireActivity(),
                this, isOrigin = false, isDepartment = false
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    private fun validateProceed() {
        var storageLocationCT = StorageLocation()
        if (binding.tvPlant.text.isNotEmpty()) {
            val bundle = Bundle()
            var plantDetails =
                plantList.singleOrNull() { it.plantId == binding.tvPlant.text.trim().split(hyphen).get(0) }
            var storageLocation = storageLocationList.singleOrNull() {
                it.procureLocationCode == if (binding.tvLocation.text.toString().trim()
                        .isEmpty()
                ) "-" else binding.tvLocation.text.trim().split(hyphen).get(0)
            }
            storageLocationCT.plant = storageLocation?.plant.toString()
            storageLocationCT.storageLocationCode = storageLocation?.procureLocationCode.toString()
            storageLocationCT.storageLocationName = storageLocation?.procureLocationName.toString()

            bundle.putString(AUDIT_TYPE, BAG_COUNT)
            bundle.putParcelable(PLANT_DETAILS, plantDetails)
            bundle.putParcelable(STORAGE_LOCATION_DETAILS, storageLocationCT)
            moveToInprogressPage(bundle)
        } else {
            showSnack(getString(R.string.select_plant_storage_warning_msg))
        }

    }

    private fun moveToInprogressPage(bundle: Bundle) {
        callBack?.replaceFragment(STOCK_RECON_INPROGRESS_COMPLETED, bundle)
    }

    override fun clickOnItem(data: String, isPlant: Boolean, isVendor: Boolean, isGrade: Boolean) {
        customDialog?.dismiss()
        if (isPlant) {
            binding.tvPlant.text = data
            binding.tvLocation.text = ""
            var split = data.split(hyphen)
            /*after selecting the plant from dialog, filtering the storage location for the respective plant*/
            storageLocationList =
                storageLocation.filter { it.plant == split[0].trim() } as ArrayList<VegaCustomStLocation>
        } else {
            binding.tvLocation.text = data
        }

    }

}
