package com.olam.warehouse.vegax.dispatch.ui.mtnt

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaDispatchTrucks
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.dispatch.R
import com.olam.warehouse.vegax.dispatch.data.domain.model.VegaDeliveryPost
import com.olam.warehouse.vegax.dispatch.data.domain.model.VegaDeliveryPostResponse
import com.olam.warehouse.vegax.dispatch.databinding.FragmentVegaDispatchSummaryBinding
import com.olam.warehouse.vegax.dispatch.ui.VegaDispatchViewModel
import com.olam.warehouse.vegax.dispatch.utils.DISPATCH_DATA
import com.olam.warehouse.vegax.dispatch.utils.DISPATCH_DATA_LIST
import kotlinx.android.synthetic.main.item_vega_dispatch_summary_details_list.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 2/11/2020.
 */
class VegaDispatchSummaryFragment : BaseFragment() {

    private var dispatchLotsList = mutableListOf<VegaDispatchLots>()
    private var deliveryList = mutableListOf<VegaDispatchTrucks>()
    private var dispatchData = VegaDispatchTrucks()
    private var deliveryStockWeight: Double? = 0.0
    private var stocksWeight: Double? = 0.0
    private var stocksUOM: String? = ""
    private var deliveryUOM: String? = ""


    private val vm: VegaDispatchViewModel by viewModel()
    private lateinit var binding: FragmentVegaDispatchSummaryBinding
    override val layoutResourceId = R.layout.fragment_vega_dispatch_summary

    companion object {
        fun newInstance(
            dispatchLotsList: ArrayList<VegaDispatchLots>,
            dispatchData: VegaDispatchTrucks
        ) = VegaDispatchSummaryFragment().putArgs {
            putParcelable(DISPATCH_DATA, dispatchData)
            putParcelableArrayList(DISPATCH_DATA_LIST, dispatchLotsList)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaDispatchSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("dispatch/ui/mtnt/VegaDispatchSummaryFragment").title("Dispatch").with(tracker)
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
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnConfirmDispatch, it, true)
        }
        dispatchLotsList = arguments?.getParcelableArrayList<VegaDispatchLots>(DISPATCH_DATA_LIST)!!
        dispatchData = arguments?.getParcelable<VegaDispatchTrucks>(DISPATCH_DATA)!!

        dispatchData.status?.let { enableProceedBtn(it) }
        deliveryUOM = dispatchData.deliveryUOM
        dispatchLotsList.forEach { stocksUOM = it.unitOfMeasure }
        deliveryStockWeight =
            dispatchData.stockQty!!.toDouble() * (dispatchData.numerator!!.toDouble()
                .div(dispatchData.denominator!!.toDouble()))
        stocksWeight = dispatchLotsList.sumByDouble { it.editedWeight?.toDouble() ?: 0.0 }

        binding.tvWeight.text = stocksWeight.toString().plus(" ").plus(stocksUOM)
        binding.tvWaybillNumber.text = dispatchData.wayBillNo
        binding.tvObdNo.text = dispatchData.delivery
        binding.tvTruckID.text = getString(R.string.truck_id_is).plus(dispatchData.vehicleNumber.toString())
        binding.tvDeliveryWeight.text = deliveryStockWeight.toString().plus(" ").plus(deliveryUOM)
        if ("null" != dispatchData.erdat) {
            val times = dispatchData.erdat?.split('(', ')')
            binding.tvDate.text = times?.get(1)?.let { it1 ->
                DateUtils.getUTCDateTime(
                    it1,
                    App.getAppContext()
                )
            }
        }
        if (dispatchLotsList.size > 1) binding.llBatchNo.visible() else binding.llBatchNo.gone()

        setUpAdapter(dispatchLotsList)
        binding.btnConfirmDispatch.setOnClickListener {
            if (dispatchLotsList.size > 1) {
                if (validate(binding.etBatchNo.text.toString())) showConfirmDialog()
                else showSnack(getString(R.string.batch_no_empty))
            } else {
                showConfirmDialog()
            }
        }

        vm.deliveryPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaDeliveryPostResponse>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                updateDispatch(dispatchData.weighBridgeId, true, 4, response.data?.data?.msg)
                moveToSuccessPage(response.data?.data?.delivery)
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                updateDispatch(dispatchData.weighBridgeId, false, 3, response.error.toString())
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
            }
        }
    }

    private fun updateDispatch(weighBridgeId: String, syncStatus: Boolean, status: Int, msg: String?) {
        vm.updateDispatchStatus(weighBridgeId, syncStatus, status, msg, binding.etBatchNo.text.toString())
    }

    private fun setUpAdapter(selectedStocks: List<VegaDispatchLots>) {
        val dispatchLots = selectedStocks as MutableList<VegaDispatchLots>
        binding.rvDispatchLots.setUp(
            dispatchLots.asReversed(),
            R.layout.item_vega_dispatch_summary_details_list,
            { it, pos ->
            tvLotNo.text = it.batchNumber
            tvWeight.text = it.weight.plus(" ").plus(it.unitOfMeasure)
            tvDispatchWeight.text = it.editedWeight.plus(" ").plus(it.unitOfMeasure)
            tvLocation.text = it.storageLocationCode
            tvKor.text = it.kor
            tvOrigin.text = it.region
        }, {
            val item = this

        })
    }


    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_dispatch_message)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(R.string.cancel),
                {
                    PostDeliveryDetail()
                },
                { dismiss() })
        }
    }

    private fun validate(batchNo: String): Boolean {
        return !batchNo.isEmpty()
    }

    private fun PostDeliveryDetail() {
        deliveryList.clear()
        dispatchLotsList.forEach {
            val dispatchItem = dispatchData.copy()
            dispatchItem.unitsOfMeasure = deliveryUOM
            dispatchItem.batchNumber = it.batchNumber
            dispatchItem.recStorageLocationCode = it.storageLocationCode
            if (deliveryUOM.equals(stocksUOM))
                dispatchItem.netWeight = it.editedWeight.toString()
            else if (deliveryUOM.equals("MT") && stocksUOM.equals("KG"))
                dispatchItem.netWeight = (it.editedWeight?.toDouble()?.div(1000)).toString()
            else if (deliveryUOM.equals("KG") && stocksUOM.equals("MT"))
                dispatchItem.netWeight = (it.editedWeight?.toDouble()?.times(1000)).toString()
            deliveryList.add(dispatchItem)
        }
        vm.postDeliveryDetail(
            VegaDeliveryPost(
                getCurrentKey(),
                getPlantDetails(),
                binding.etBatchNo.text.toString(),
                deliveryList
            )
        )
    }

    private fun moveToSuccessPage(deliveryId: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_dispatch)
        ) else intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_dispatch_offline)
        )
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.delivery_no).plus(deliveryId))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun enableProceedBtn(status: Int) {

        when (status) {
            4 -> {
                binding.btnConfirmDispatch.isEnabled = false
                ViewCompat.setBackgroundTintList(
                    binding.btnConfirmDispatch,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.grey) }
                )
            }
            else -> {
                binding.btnConfirmDispatch.isEnabled = true
                ViewCompat.setBackgroundTintList(
                    binding.btnConfirmDispatch,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                        )
                    }
                )
            }
        }
    }

}
