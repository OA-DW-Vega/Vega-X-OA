package com.olam.warehouse.odreceiving.ui.mtnr

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.master.dorigin.entity.DOReceivingMtnLots
import com.olam.warehouse.master.dorigin.model.DOReceivingMtnWrapper
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.databinding.FragmentDoReceivingMtnrBinding
import com.olam.warehouse.odreceiving.ui.DOReceivingViewModel
import com.olam.warehouse.odreceiving.utils.UOM
import com.olam.warehouse.odreceiving.utils.WS01
import com.olam.warehouse.odreceiving.utils.WS02
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ReceivingType
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import kotlinx.android.synthetic.main.item_do_receiving_mtnr_lot.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
class DOReceivingMtnrFragment : BaseFragment() {

    private val receivingData = DOReceiving()
    private val mtnLots = mutableListOf<DOReceivingMtnLots>()
    private val vm: DOReceivingViewModel by viewModel()
    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(receivingData: DOReceiving, lots: List<DOReceivingMtnLots>)
    }

    private lateinit var binding: FragmentDoReceivingMtnrBinding
    override val layoutResourceId = R.layout.fragment_do_receiving_mtnr

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDoReceivingMtnrBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odreceiving/ui/mtnr/DOReceivingMtnrFragment").title("OD/Receiving")
            .with(tracker)
        initUI()
        if (isOnline()) {
            fetchMtnDetails()
        }
    }

    private fun fetchMtnDetails() {
        /* vm.warehouse.observe(viewLifecycleOwner, Observer { processApiResult(it) })
         vm.fetchWarehouseWithMtns()*/
    }

    private fun processApiResult(data: Resource<GenericReqAndResp<DOReceivingMtnWrapper>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> saveResult(it.data)
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> requireContext().toast("Faild to fetch warhouses")
            }
        }
    }

    private fun saveResult(data: GenericReqAndResp<DOReceivingMtnWrapper>?) {
        hideLoading()
        data?.data?.let {
            vm.saveWarehouseWithMtns(it)
            vm.getWarehouses()
        }
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnStartWeigh, it, true)
        }
        vm.warehouseLocal.observe(viewLifecycleOwner, Observer {
            val warehouses = it.map { data -> data.supplyingPlantName }
            val warehouseAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, warehouses)
            binding.spWarehouse.adapter = warehouseAdapter
            binding.spWarehouse.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                    override fun onItemSelected(
                        p0: AdapterView<*>?,
                        p1: View?,
                        position: Int,
                        p3: Long
                    ) {
                        val item = it[position].supplyingPlantId
                        vm.getWarehousesWithMtns(item)
                        receivingData.plantId = UIUtils.getWarehouseId().toString()
                        receivingData.supplierCode = item
                        clearValues()
                    }
                }
        })
        vm.getWarehouses()

        vm.warehouseWithMtns.observe(viewLifecycleOwner, Observer {
            val mtnNumbers = it.mtns.map { data -> data.mtn.mtnNumber }
            val mtnNumberAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, mtnNumbers)
            binding.spMtn.adapter = mtnNumberAdapter
            binding.spMtn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    val mtn = it.mtns[position]
                    receivingData.mtnCode = mtn.mtn.mtnNumber
                    setUpRecyclerView(mtn.lots)
                    clearValues()
                }
            }
        })

        binding.btnStartWeigh.setOnClickListener { validateInputs() }
    }

    private fun clearValues() {
        mtnLots.clear()
    }

    private fun setUpRecyclerView(lots: List<DOReceivingMtnLots>) {
        val data = mutableListOf<DOReceivingMtnLots>()
        data.addAll(lots)
        binding.rvLots.setUp(data, R.layout.item_do_receiving_mtnr_lot, { it, pos ->
            tvLotNo.text = it.batch
            tvGrade.text = it.materialName
            cbGrade.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) mtnLots.add(it) else mtnLots.remove(it)
            }
        })
    }

    private fun showWeighTypeDialog() {
        MaterialDialog(requireContext()).show {
            title(R.string.select_weigh_type)
            listItemsSingleChoice(R.array.weighScaleType) { _, index, _ ->
                receivingData.wsGate = if (index > 0) WS02 else WS01
                receivingData.uom = UOM
                receivingData.wtype = ReceivingType.MTN.type
                callBack?.replaceFragment(receivingData, mtnLots)
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun validateInputs() {
        when {
            receivingData.plantId.isNullOrEmpty() -> requireActivity().toast(getString(R.string.message_valid_location))
            receivingData.mtnCode.isNullOrEmpty() -> requireActivity().toast(getString(R.string.message_valid_mtn))
            mtnLots.isEmpty() -> requireActivity().toast(getString(R.string.message_select_valid_lot))
            else -> showWeighTypeDialog()
        }
    }
}
