package com.olam.warehouse.vegax.createmapar.ui.depositstock

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.createmapar.R
import com.olam.warehouse.vegax.createmapar.data.domain.model.ArLotDetails
import com.olam.warehouse.vegax.createmapar.databinding.FragmentDepositStockBinding
import com.olam.warehouse.vegax.createmapar.ui.ArViewModel
import com.olam.warehouse.vegax.createmapar.ui.arcore.ArCoreViewActivity
import com.olam.warehouse.vegax.createmapar.utils.*
import kotlinx.android.synthetic.main.item_pickup_stock.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 6/15/2021.
 */
class DepositStockFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_deposit_stock
    private lateinit var binding: FragmentDepositStockBinding
    private val vm: ArViewModel by viewModel()
    private var lotList = arrayListOf<ArLotDetails>()

    companion object {
        fun newInstance() = DepositStockFragment().putArgs {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDepositStockBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding.ivScan.setOnClickListener { moveToScan() }
        binding.tvAdd.setOnClickListener { validateLotDetails() }
        vm.lotDeatils.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getLotDeatils.observe(viewLifecycleOwner, Observer { updateLotUI(it) })
        vm.getLotDetails()
    }

    private fun updateUI(response: Resource<ArLotDetails>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    activity?.toast("Lot details save successfully")
                    vm.getLotDetails()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    activity?.toast(it.error.toString())
                    hideLoading()
                }
            }
        }
    }

    private fun moveToScan() {
        val intent = Intent(requireContext(), ScannerActivity::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    binding.etLotIdValue.setText(it)
                }
            }
        }
    }

    private fun validateLotDetails() {
        val lotId = binding.etLotIdValue.text.toString().trim()
        val stLocation = binding.etStLocationValue.text.toString().trim()
        when {
            lotId.isEmpty() -> {
                binding.etLotIdValue.error = "Enter valid lot id"
                binding.etLotIdValue.requestFocus()
            }
            stLocation.isEmpty() -> {
                binding.etStLocationValue.error = "Enter valid st location"
                binding.etStLocationValue.requestFocus()
            }
            else -> saveLotDetails()
        }
    }

    private fun saveLotDetails() {
        val lotId = binding.etLotIdValue.text.toString().trim()
        val stLocation = binding.etStLocationValue.text.toString().trim()
        val lotDetails = ArLotDetails()
        lotDetails.plant = getPlantDetails()
        lotDetails.lotId = lotId
        lotDetails.storageLocationCode = stLocation
        lotDetails.createdAt = DateUtils.getCurrentTimeInMills().toString()
        lotDetails.createdBy = PreferenceHelper.get(Constants.USER_NAME, "")
        lotDetails.updatedAt = DateUtils.getCurrentTimeInMills().toString()
        lotDetails.updatedBy = PreferenceHelper.get(Constants.USER_NAME, "")
        vm.saveLotDetails(lotDetails)
    }

    private fun updateLotUI(response: Resource<List<ArLotDetails>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    if (it.data?.isNotEmpty() == true) {
                        var currentlotitem = emptyList<ArLotDetails>()
                        if (binding.etLotIdValue.text.toString().isNotEmpty())
                            currentlotitem = it.data?.filter {
                                it.lotId?.equals(
                                    binding.etLotIdValue.text.toString().trim()
                                ) == true
                            } ?: emptyList<ArLotDetails>()
                        val lotitems = it.data?.filter { it.arReq.anchorIds.isEmpty() }
                        lotList = lotitems as ArrayList<ArLotDetails>
                        val isData = lotList.any {
                            it.lotId.equals(
                                binding.etLotIdValue.text.toString().trim()
                            )
                        }
                        if (!isData) lotList.addAll(currentlotitem)
                        lotList.forEach { it.anchorIds = it.arReq.anchorIds }
                        setUpAdapter(lotList)
                    } else {

                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    activity?.toast(it.error.toString())
                    hideLoading()
                }
            }
        }
    }

    private fun setUpAdapter(lotList: ArrayList<ArLotDetails>) {
        binding.rvLots.setUp(lotList, R.layout.item_pickup_stock, { it, pos ->
            ivCreateMap.visible()
            tv_truck_no.text = it.lotId
            tvStLocation.text = it.storageLocationCode
            if (it.arReq.anchorIds.isNotEmpty())
                ivCreateMap.setImageResource(com.olam.warehouse.presentation.R.drawable.ic_create_map)
            else
                ivCreateMap.setImageResource(com.olam.warehouse.presentation.R.drawable.ic_add_white_24dp)
//            tvMaterialName.text = it.materialName
//            tvWbId.text = it.weighBridgeId
            val times = DateUtils.getCurrentTimeInMills().toString()
            tvDate.text = times.let { it1 ->
                it1.let { it2 ->
                    DateUtils.getUTCDateTime(
                        it2,
                        App.getAppContext()
                    )
                }
            }
        }, {
            val item = this
            val anchoridList = arrayListOf<String>()
            anchoridList.addAll(item.arReq.anchorIds.map { it.arValue.toString() })
            val arLotDetails = this.copy()
            val intent = Intent(activity, ArCoreViewActivity::class.java)
            if (anchoridList.size > 0) {
                intent.putExtra(MODE, RESOLVE_MAP.toString())
                intent.putStringArrayListExtra(ANCHOR_IDS, anchoridList)
            } else
                intent.putExtra(MODE, CREATE_MAP)
            intent.putExtra(LOT_ID, item.lotId)
            intent.putExtra(LOT, arLotDetails)
            startActivity(intent)
        })
    }
}
