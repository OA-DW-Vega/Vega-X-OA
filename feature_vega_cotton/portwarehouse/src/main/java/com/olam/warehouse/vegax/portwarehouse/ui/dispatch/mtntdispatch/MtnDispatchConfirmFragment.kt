package com.olam.warehouse.vegax.portwarehouse.ui.dispatch.mtntdispatch

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnDispatchDelivery
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.DeliveryWithBales
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.MtnDeliveryWithGrades
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.MtnDispatchPostResponse
import com.olam.warehouse.portwarehouse.ui.success.SuccessPortActivity
import com.olam.warehouse.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.ui.dispatch.MtnDispatchViewModel
import kotlinx.android.synthetic.main.fragment_mtn_dispatch_confirm.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 3/30/2021.
 */
class MtnDispatchConfirmFragment : BaseFragment() {

    private var deliveryNo: String? = ""
    private var totalNetWeight: String? = ""
    private var totalTarWeight: String? = ""
    private var totalGrossWeight: String? = ""
    private lateinit var mAdapter: MtnDispatchConfirmAdapter
    private var deliveryDto: MtnDispatchDelivery? = null

    private val vm: MtnDispatchViewModel by viewModel()
    override val layoutResourceId = R.layout.fragment_mtn_dispatch_confirm

    companion object {
        fun newInstance(deliveryNo: String) = MtnDispatchConfirmFragment().putArgs {
            putString(PortWHUtil.DELIVERY_NO, deliveryNo)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intiExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("portwarehouse/ui/dispatch/mtntdispatch/MtnDispatchConfirmFragment")
            .title("Portwarehouse").with(tracker)
    }

    private fun intiExtras() {
        deliveryNo = arguments?.getString(PortWHUtil.DELIVERY_NO)
    }

    fun initUI() {
        mAdapter = MtnDispatchConfirmAdapter()
        lotdetails_recyclerview.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        lotdetails_recyclerview.adapter = mAdapter

        txt_date.text = DateUtils.fromMillisToTimeString(DateUtils.getCurrentTimeInMills())
        deliveryNo?.let {
            var gradeData: MtnDeliveryWithGrades = MtnDeliveryWithGrades()
            var balesData: DeliveryWithBales = DeliveryWithBales()
            runBlocking {
                withContext(Dispatchers.IO) {
                    gradeData = vm.getDeliveryWithGrades(it)
                    balesData = vm.getDeliveryWithBales(it)
                }
            }
            if (balesData != null && balesData.bales != null) {
                totalNetWeight = balesData.bales.sumByDouble { it.netWeight ?: 0.0 }.formatTwoDigits()
                totalGrossWeight =
                    balesData.bales.sumByDouble { it.grossWeight ?: 0.0 }.formatTwoDigits()
                try {
                    totalTarWeight =
                        (totalGrossWeight!!.toDouble()
                            .minus(totalNetWeight!!.toDouble())).formatTwoDigits()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                txt_ordernumber.text = deliveryNo
                txt_balescount.text = balesData.bales.size.toString()
                txt_totalweight.text = totalNetWeight.plus(" ").plus("KG")
                txt_netcount.text = totalNetWeight.plus(" ").plus("KG")
                txt_tarecount.text = totalTarWeight.plus(" ").plus("KG")
                txt_grosscount.text = totalGrossWeight.plus(" ").plus("KG")

                balesData.bales.forEach {
                    if (vm.baleHashMap.containsKey(it.grade)) {
                        vm.baleHashMap[it.grade.toString()] =
                            balesData.bales.filter { it1 -> it1.grade.equals(it.grade) }
                    } else {
                        vm.baleHashMap.put(
                            it.grade.toString(),
                            balesData.bales.filter { it1 -> it1.grade.equals(it.grade) })
                    }
                }
            }
            if (balesData != null && balesData.delivery != null) {
                deliveryDto = balesData.delivery
                deliveryDto!!.baleDTO = balesData.bales
            }

            deliveryDto!!.userName = PreferenceHelper.get(Constants.USER_NAME, "")

            if (gradeData != null && gradeData.grades != null) {
                txt_grades.text =
                    gradeData.grades.map { it.grade }.toString().replace("[", "").replace("]", "")

            }

            setUpAdapter(vm.baleHashMap)
        }

        btn_confirm.setOnClickListener { deliveryDto?.let { it1 -> showConfirmDialog(it1) } }
        vm.postDispatch.observe(viewLifecycleOwner, Observer { updateDispatchPost(it) })
    }

    private fun updateDispatchPost(data: Resource<GenericReqAndResp<MtnDispatchPostResponse>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    updateUI(it.data?.data)
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    context?.toast(it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun setUpAdapter(baleHashMap: HashMap<String, List<MtnBale>>) {
        mAdapter.updateData(baleHashMap)
    }

    private fun showConfirmDialog(deliveryDto: MtnDispatchDelivery) {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_dispatch)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (PortWHUtil.isOnline()) postDispatch(deliveryDto) else postDeliveryOffline(
                        deliveryDto.deliveryNumber
                    )
                },
                { dismiss() })
        }
    }

    private fun postDispatch(deliveryDto: MtnDispatchDelivery) {
        vm.postDispatch(deliveryDto)
    }

    private fun updateUI(response: MtnDispatchPostResponse?) {

        if (response?.success!!) {
            runBlocking {
                withContext(Dispatchers.IO) {
                    deliveryNo?.let { vm.deleteBales(it) }
                }
            }
            moveToSuccess()
        } else {
            activity?.toast(response.message.toString())
        }

    }

    private fun postDeliveryOffline(deliveryNumber: String) {
        runBlocking {
            withContext(Dispatchers.IO) {
                vm.updateDeliverySaveOffline(deliveryNumber)
            }
        }
        var baleData = MtnBale()
        moveToSuccess()
    }
    private fun moveToSuccess() {
        val intent = Intent(context, SuccessPortActivity::class.java)
        intent.putExtra(
            PortWHUtil.OT_NUMBER,
            getString(R.string.deliver_no_is).format().plus(" ").plus(deliveryNo)
        )
        intent.putExtra(PortWHUtil.SUCCESS, PortWHUtil.DISPATCH_SUCCESS)
        context?.startActivity(intent)
    }

}
