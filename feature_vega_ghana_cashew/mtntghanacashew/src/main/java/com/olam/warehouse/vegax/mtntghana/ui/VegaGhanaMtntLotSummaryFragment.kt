package com.olam.warehouse.vegax.mtntghana.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchPurchaseOrders
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDeliveryPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDeliveryPostResponse
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchLotsMerge
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntghana.R
import com.olam.warehouse.vegax.mtntghana.databinding.FragmentGhanaMtntSummaryBinding
import com.olam.warehouse.vegax.mtntghana.utils.MODEL_BUNDLE
import kotlinx.android.synthetic.main.item_lot_mtnt_summary.*
import kotlinx.android.synthetic.main.item_lot_mtnt_summary.view.*
import kotlinx.android.synthetic.main.item_vega_ghana_mtnt_material_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Keerthi Santhanam on 7/25/2020.
 */
class VegaGhanaMtntLotSummaryFragment : BaseFragment() {

    private var dispatchLotsList = arrayListOf<VegaEcuadorDispatchLots>()
    override val layoutResourceId: Int =
        com.olam.warehouse.vegax.mtntghana.R.layout.fragment_ghana_mtnt_summary
    private lateinit var binding: FragmentGhanaMtntSummaryBinding
    private val vm: VegaGhanaMtntViewModel by viewModel()
    private var model: VegaEcuadorDispatch = VegaEcuadorDispatch()
    private var callBack: CallBack? = null

    private var materialCodeList = arrayListOf<String>()
    private var mergedLotsMap = mutableMapOf<Int, ArrayList<VegaEcuadorDispatchLots>>()
    private var deliveryDetails = ArrayList<VegaEcuadorDispatchLotsMerge>()
    private var isLotEditable: Boolean = true

    interface CallBack {
        fun popAllFragmentsFromBackStack()
    }

    companion object {
        fun newInstance(model: VegaEcuadorDispatch, dispatchLotList: ArrayList<VegaEcuadorDispatchLots>) =
            VegaGhanaMtntLotSummaryFragment().putArgs {
                putParcelableArrayList(DISPATCH_DATA_LIST, dispatchLotList)
                putParcelable(MODEL_BUNDLE, model)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentGhanaMtntSummaryBinding.inflate(inflater)
        initExtra()
        initUI()
        vm.deliveryPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
        return binding.root
    }

    private fun initExtra() {
        dispatchLotsList = arguments?.getParcelableArrayList<VegaEcuadorDispatchLots>(DISPATCH_DATA_LIST)!!
        model = arguments?.getParcelable(MODEL_BUNDLE)!!
        setupAdapter()
    }

    private fun initUI() {
        binding.tvRemarkValue.text = model.remarks
        binding.tvDestValue.text = model.storageLocationCode.plus("-").plus(model.plantName)
        val times = model.erdat?.split('(', ')')
        binding.tvDateValue.text = times?.get(1).let { it1 ->
            it1?.let { it2 ->
                DateUtils.getUTCDateTime(
                    it2,
                    App.getAppContext()
                )
            }
        }

        binding.tvStoNoValue.text = model.purchaseDocNum
        binding.btProceed.setOnClickListener { showConfirmationDialog() }
        val linearLayoutManager = activity?.let { LinearLayoutManager(it) }
        linearLayoutManager?.orientation = LinearLayoutManager.VERTICAL
        binding.rvLotList.layoutManager = linearLayoutManager
        binding.ivEdit.setOnClickListener { showUpdateRemarkDialog() }
        //setUpLotAdapter()
        setUpMaterialAdapter(model.purchaseOrders as MutableList<VegaEcuadorDispatchPurchaseOrders>)
    }

    private fun setUpMaterialAdapter(list: MutableList<VegaEcuadorDispatchPurchaseOrders>) {
        materialCodeList.clear()
        materialCodeList.addAll(list.map { it.materialCode }.distinct())
        binding.rvMaterial.setUp(list, R.layout.item_vega_ghana_mtnt_material_details, { it, pos ->
            tvMaterialValue.text = it.materialName
            tvStoWeightValue.text = it.menge?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ").plus(it.meins)
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun setupAdapter() {
        binding.rvLotList.addItemDecoration(DividerSpaceItemDecoration(16, dispatchLotsList))
        binding.rvLotList.setUp(dispatchLotsList, R.layout.item_lot_mtnt_summary, { it, pos ->
            tvLotId.text = it.batchNumber
            tvMaterialName.text = it.materialName
            tvStLocationValue.text = it.storageLocationCode
            tvWeightValue.text = it.netWeight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitsOfMeasure)
            it.editedWeight?.toDoubleOrNull()?.let { double ->
                tvWeightToDispatch.text = double.formatThreeDigits().plus(" ").plus(it.unitsOfMeasure)
            }
            ivEdit.visible()
            ivEdit.setOnClickListener {
                if (isLotEditable) callBack?.popAllFragmentsFromBackStack()
            }

        }, itemClick = {

        })
    }

    private fun showConfirmationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_dispatch)
            getMetirialCustomView(this, getString(R.string.proceed), getString(R.string.cancel), {
                if (deliveryDetails.isEmpty())
                    prepareDeliveryList()
                else {
                    if (AppUtils.isOnline())
                        vm.postDeliveryDetail(
                            VegaEcuadorDeliveryPost(
                                getCurrentKey(),
                                getPlantDetails(),
                                model.binFormation,
                                model.delivery,
                                model.pgi,
                                model.picking,
                                deliveryDetails
                            )
                        )
                }
            }, { dismiss() })
        }
    }

    private fun showUpdateRemarkDialog() {
        showDialog(
            getString(R.string.update_remark_title),
            object : DialogClick {
                override fun onPositive(remark: String) {
                    model.remarks = remark
                    binding.tvRemarkValue.text = remark
                }
            },
            true, model.remarks
        )
    }

    private fun prepareDeliveryList() {
        dispatchLotsList.forEach label@{
            it.purchaseDocNum = model.purchaseDocNum
            it.netWeight = it.editedWeight
            it.startTime = model.startTime
            it.endTime = model.endTime
            it.turnAroundTime = model.turnAroundTime
            it.remarks = model.remarks
            it.recPlantId = model.storageLocationCode
            model.purchaseOrders.forEach { item ->
                if (item.materialCode.equals(it.materialCode)) {
                    it.purchaseDocDesc = item.purchaseDocDesc.toString()
                    it.recStorageLocationCode = item.storageLocationCode
                }
            }
            if (it.pairId!! <= 0)
                return@label
            when (val mergedDispatchLotList = mergedLotsMap[it.pairId!!]) {
                null -> {
                    val list = ArrayList<VegaEcuadorDispatchLots>()
                    list.add(it)
                    mergedLotsMap[it.pairId!!] = list
                }
                else -> {
                    mergedDispatchLotList.add(it)
                    mergedLotsMap[it.pairId!!] = mergedDispatchLotList
                }
            }
        }
        if (AppUtils.isOnline()) createDeliveryDetails()
        else prepareSuccessData(model.wbTempId, false, "Data cached offline")
    }

    private fun createDeliveryDetails() {

        for ((key, dispatchLotList) in mergedLotsMap) {
            val item = VegaEcuadorDispatchLotsMerge()

            item.lots = ArrayList<VegaEcuadorDispatchLots>()
            item.lots.addAll(dispatchLotList)
            deliveryDetails.add(item)
        }

        val unpaired = dispatchLotsList.filter { it.pairId == 0 }
        unpaired.forEach {
            val item = VegaEcuadorDispatchLotsMerge()
            item.batchNumber = it.batchNumber

            val list = ArrayList<VegaEcuadorDispatchLots>(1)
            list.add(it)

            item.lots.addAll(list)
            deliveryDetails.add(item)
        }
        vm.postDeliveryDetail(
            VegaEcuadorDeliveryPost(
                getCurrentKey(),
                getPlantDetails(),
                model.binFormation,
                model.delivery,
                model.pgi,
                model.picking,
                deliveryDetails
            )
        )
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaEcuadorDeliveryPostResponse>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.data?.let {
                    if (it.delivery && it.pgi && it.picking) {
                        prepareSuccessData(
                            response.data?.data?.message ?: "",
                            true,
                            response.data?.message.toString()
                        )
                    } else {
                        if (it.delivery || it.binFormation) {
                            isLotEditable = false
                            ivEdit.gone()
                        }
                        model.binFormation = response.data?.data?.binFormation!!
                        model.delivery = response.data?.data?.delivery!!
                        model.pgi = response.data?.data?.pgi!!
                        model.picking = response.data?.data?.picking!!
                        deliveryDetails =
                            response.data?.data?.deliveryDetails as ArrayList<VegaEcuadorDispatchLotsMerge>
                        showDispatchErrorDialog(response.data?.data?.message ?: getString(R.string.dispatch_failed))
                    }
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
            }
        }
    }

    private fun prepareSuccessData(deliveryId: String, syncStatus: Boolean, msg: String) {
        model.status = if (syncStatus) Status.MTNT_COMPLETED else Status.SYNC_PENDING
        model.wbTempId = if (!syncStatus) deliveryId else ""
        model.isSynced = syncStatus
        model.syncStatusMsg = msg
        model.deliveryId = deliveryId
        if (!syncStatus) {
            vm.saveDispatch(model)
            vm.saveDispatchLotLineItems(dispatchLotsList)
        } else {
            vm.updateDeletedItem(model.wbTempId)
        }
        moveToSuccessPage(deliveryId)
    }

    private fun moveToSuccessPage(deliveryId: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) {
            intent.putExtra(
                AppUtils.TITLE,
                getString(R.string.success_dispatch)
            )
            intent.putExtra(AppUtils.SUB_TITLE, deliveryId)
        } else {
            intent.putExtra(
                AppUtils.TITLE,
                getString(R.string.success_dispatch_offline)
            )
            intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.delivery_no).plus(deliveryId))
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showDispatchErrorDialog(msg: String) {
        MaterialDialog(requireContext()).show {
            title(com.olam.warehouse.presentation.R.string.error)
            message(null, msg)
            cancelOnTouchOutside(false)
            getMetirialCustomView(this, getString(R.string.retry), getString(R.string.cancel), {
                vm.postDeliveryDetail(
                    VegaEcuadorDeliveryPost(
                        getCurrentKey(),
                        getPlantDetails(),
                        model.binFormation,
                        model.delivery,
                        model.pgi,
                        model.picking,
                        deliveryDetails
                    )
                )
            }, { dismiss() })
        }
    }

    fun getLotEditableStatus(): Boolean {
        return isLotEditable
    }
}
