package com.olam.warehouse.vegax.dispatchindiacoffee.ui.mtnt

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.olam.warehouse.master.vega.entity.VegaDispatchDelivery
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaDispatchTrucks
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants.IS_STOCK_FETCHED
import com.olam.warehouse.presentation.utils.Constants.MATERIAL_NUMBER
import com.olam.warehouse.presentation.utils.Constants.SELECTED_STOCKS
import com.olam.warehouse.presentation.utils.Constants.SELECTED_STOCKS_ID
import com.olam.warehouse.presentation.utils.Constants.SELECTED_STOCKS_LIST
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.dispatchindiacoffee.R
import com.olam.warehouse.vegax.dispatchindiacoffee.databinding.FragmentVegaMtntDispatchBinding
import com.olam.warehouse.vegax.dispatchindiacoffee.ui.VegaDispatchViewModel
import com.olam.warehouse.vegax.dispatchindiacoffee.ui.stock.VegaDispatchLotsActivity
import com.olam.warehouse.vegax.dispatchindiacoffee.utils.DISPATCH_DATA
import com.olam.warehouse.vegax.dispatchindiacoffee.utils.DISPATCH_DATA_LIST
import com.olam.warehouse.vegax.dispatchindiacoffee.utils.DISPATCH_SUMMARY_FRAG
import kotlinx.android.synthetic.main.item_vega_dispatch_lots_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 2/10/2020.
 */

class VegaDispatchMtntFragment : BaseFragment() {

    private var dispatchLotsList = mutableListOf<VegaDispatchLots>()
    private var addedLotsList = ArrayList<String>()
    private var dispatchData = VegaDispatchTrucks()
    private var isStockFetched = false

    private val vm: VegaDispatchViewModel by viewModel()
    private lateinit var binding: FragmentVegaMtntDispatchBinding
    override val layoutResourceId = R.layout.fragment_vega_mtnt_dispatch

    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            dispatchLotsList: MutableList<VegaDispatchLots>,
            dispatchData: VegaDispatchTrucks
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(
            dispatchData: VegaDispatchTrucks,
            postData: ArrayList<VegaDispatchLots>?
        ) = VegaDispatchMtntFragment().putArgs {
            putParcelable(DISPATCH_DATA, dispatchData)
            putParcelableArrayList(DISPATCH_DATA_LIST, postData)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaMtntDispatchBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("dispatch/ui/mtnt/VegaDispatchMtntFragment").title("Dispatch").with(tracker)
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
        dispatchData = arguments?.getParcelable<VegaDispatchTrucks>(DISPATCH_DATA)!!
        dispatchLotsList = arguments?.getParcelableArrayList(DISPATCH_DATA_LIST)!!
        if (dispatchLotsList.size > 0) {
            binding.tvWaybillNumber.setText(dispatchData.wayBillNo)
            dispatchLotsList.forEach { addedLotsList.add(it.batchNumber) }
            setUpAdapter(dispatchLotsList)
        }
        updateMandatory()
        binding.tvMaterial.text = dispatchData.materialName
        binding.tvObdNo.text = dispatchData.delivery.toString()
        binding.tvTruckID.text = getString(R.string.truck_id_is).plus(dispatchData.vehicleNumber.toString())
        binding.tvObdNo.isEnabled = false
        binding.tvAddDispatch.setOnClickListener { moveToLotDetails() }
        binding.btnConfirm.setOnClickListener { validateInputs() }

        vm.delivery.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getDelivery(dispatchData.delivery.toString(), dispatchData.deliveryItem.toString())
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaDispatchDelivery>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.data.let { it1 ->
                    dispatchData.deliveryQty = it1?.deliveryQty
                    dispatchData.deliveryUOM = it1?.deliveryUOM
                    dispatchData.stockUOM = it1?.stockUOM
                    dispatchData.stockQty = it1?.stockQty
                    dispatchData.denominator = it1?.denominator
                    dispatchData.numerator = it1?.numerator
                    binding.tvDeliveryQty.text = it1?.deliveryQty.plus(" ").plus(it1?.deliveryUOM)
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
            }
        }
    }

    private fun moveToLotDetails() {
        val intent = Intent(requireContext(), VegaDispatchLotsActivity::class.java)
        intent.putExtra(MATERIAL_NUMBER, dispatchData.materialCode)
        intent.putExtra(IS_STOCK_FETCHED, isStockFetched)
        intent.putStringArrayListExtra(SELECTED_STOCKS_ID, addedLotsList)
        startActivityForResult(intent, SELECTED_STOCKS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECTED_STOCKS && resultCode == Activity.RESULT_OK) {
            data?.extras?.getString(SELECTED_STOCKS_LIST)?.trim()?.let {
                isStockFetched = data.extras?.getBoolean(IS_STOCK_FETCHED, false)!!
                val selectedStocks =
                    Gson().fromJson<List<VegaDispatchLots>>(data.extras?.getString(SELECTED_STOCKS_LIST) ?: "")
                selectedStocks.forEach {
                    if (!addedLotsList.contains(it.batchNumber)) dispatchLotsList.add(it)
                }
                dispatchLotsList.forEach { addedLotsList.add(it.batchNumber) }
                setUpAdapter(dispatchLotsList)
            }
        }
    }

    private fun setUpAdapter(selectedStocks: List<VegaDispatchLots>) {
        val dispatchLots = selectedStocks as MutableList<VegaDispatchLots>
        binding.rvLots.setUp(dispatchLots.asReversed(), R.layout.item_vega_dispatch_lots_details, { it, pos ->
            tvLotNo.text = it.batchNumber
            tvWeight.text = it.weight.plus(" ").plus(it.unitOfMeasure)
            etDispatchWeight.setText(it.editedWeight, TextView.BufferType.EDITABLE)
            tvKor.text = it.kor
//            tvOrigin.text = it.region
            tvOrigin.text = it.storageLocationCode
            ivDeleteData.setOnClickListener { view -> showItemDeleteDialog(it) }
            etDispatchWeight.onChange { text ->
                dispatchLotsList.forEach { item ->
                    if (item.batchNumber.equals(it.batchNumber)) {
                        when (text.isEmpty()) {
                            true -> item.editedWeight = text
                            else -> {
                                if (text.toDouble() < item.weight?.toDouble()!!) {
                                    item.editedWeight = text
                                } else activity?.toast(context.getString(R.string.exceed_weight))
                            }
                        }
                    }
                }
            }
            etDispatchWeight.onRightDrawableClicked { item -> item.text.clear() }
        }, {
            val item = this

        })
    }

    private fun showItemDeleteDialog(item: VegaDispatchLots) {
        MaterialDialog(requireContext()).show {
            message(R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    addedLotsList.remove(item.batchNumber)
                    dispatchLotsList.remove(item)
                    vm.updateLot(item.batchNumber)
                    binding.rvLots.adapter?.notifyDataSetChanged()
                },
                { dismiss() })
        }
    }


    private fun validateInputs() {
        when {
            binding.tvWaybillNumber.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_waybill_number))
            binding.tvObdNo.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_obd_no))
            dispatchLotsList.size == 0 -> showSnack(getString(R.string.error_valid_dispatch_lots))
            else -> moveToDispatchSummary()
        }
    }


    private fun updateMandatory() {
        binding.tvWaybillNumberLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.waybill_number)) { mandatoryStars() } }
        binding.tvObdNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.obd_no)) { mandatoryStars() } }
        binding.tvLotsLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.lots_to_dispatch)) { mandatoryStars() } }

    }

    private fun moveToDispatchSummary() {
        dispatchData.wayBillNo = binding.tvWaybillNumber.text.toString()
        dispatchData.message = "Data cached offline"
        dispatchData.status = 1
        vm.saveDispatchAndLots(dispatchData, dispatchLotsList)
        callBack?.replaceFragment(DISPATCH_SUMMARY_FRAG, dispatchLotsList, dispatchData)
    }
}

@SuppressLint("ClickableViewAccessibility")
private fun EditText.onRightDrawableClicked(onClicked: (view: EditText) -> Unit) {
    this.setOnTouchListener { v, event ->
        var hasConsumed = false
        if (v is EditText) {
            if (event.x >= v.width - v.totalPaddingRight) {
                if (event.action == MotionEvent.ACTION_UP) {
                    onClicked(this)
                }
                hasConsumed = true
            }
        }
        hasConsumed
    }
}
