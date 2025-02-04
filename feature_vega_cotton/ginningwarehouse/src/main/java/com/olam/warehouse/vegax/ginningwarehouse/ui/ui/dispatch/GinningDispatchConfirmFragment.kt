package com.olam.warehouse.ginning.ui.dispatch

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.ginning.data.model.DispatchPostResponse
import com.olam.warehouse.ginning.utils.DELIVERY_NO
import com.olam.warehouse.ginning.utils.SUB_TITLE
import com.olam.warehouse.ginning.utils.TITLE
import com.olam.warehouse.ginning.utils.isOnline
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.VegaCottonGinningDispatchDelivery
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.fragment_ginning_dispatch_confirm.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 3/24/2020.
 */
class GinningDispatchConfirmFragment : BaseFragment() {

    private var deliveryNo: String? = ""
    private var totalNetWeight: String? = ""
    private var totalTarWeight: String? = ""
    private var totalGrossWeight: String? = ""
    private lateinit var mAdapter: GinningDispatchConfirmAdapter
    private var deliveryDtoVegaCotton: VegaCottonGinningDispatchDelivery? = null

    private val vm: GinningDispatchViewModel by viewModel()
    override val layoutResourceId = R.layout.fragment_ginning_dispatch_confirm
    companion object {
        fun newInstance(deliveryNo: String) = GinningDispatchConfirmFragment().putArgs {
            putString(DELIVERY_NO, deliveryNo)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intiExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ginningwarehouse/ui/dispatch/GinningDispatchConfirmFragment")
            .title("Ginningwarehouse").with(tracker)
    }

    private fun intiExtras() {
        deliveryNo = arguments?.getString(DELIVERY_NO)
    }

    fun initUI() {
        mAdapter = GinningDispatchConfirmAdapter()
        lotdetails_recyclerview.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        lotdetails_recyclerview.adapter = mAdapter

        txt_date.text = DateUtils.fromMillisToTimeString(DateUtils.getCurrentTimeInMills())

        deliveryNo?.let {
            vm.getDeliveryWithGrades(it)
            vm.getDeliveryWithBales(it)
        }


        vm.deliveryWithGrades.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it != null && it.grades != null) {
                txt_grades.text =
                    it.grades.map { it.grade }.toString().replace("[", "").replace("]", "")
            }

        })


        vm.deliveryWithBales.observe(viewLifecycleOwner, androidx.lifecycle.Observer { balesData ->
            if (balesData != null && balesData.bales.size > 0) {
                if (balesData.bales != null) {
                    deliveryDtoVegaCotton = balesData.deliveryVegaCotton
                    deliveryDtoVegaCotton!!.baleDTO = balesData.bales
                    deliveryDtoVegaCotton!!.userName = PreferenceHelper.get(Constants.USER_NAME, "")
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
                    txt_totalweight.text = totalNetWeight.plus(" ").plus("KG")
                    txt_netcount.text = totalNetWeight.plus(" ").plus("KG")
                    txt_tarecount.text = totalTarWeight.plus(" ").plus("KG")
                    txt_grosscount.text = totalGrossWeight.plus(" ").plus("KG")

                    setUpAdapter(vm.baleHashMap)
                } else {
                    setUpAdapter(HashMap<String, List<Bale>>())
                }
            } else {
                setUpAdapter(HashMap<String, List<Bale>>())
            }
        })

        btn_confirm.setOnClickListener { deliveryDtoVegaCotton?.let { it1 -> showConfirmDialog(it1) } }

    }



    private fun setUpAdapter(baleHashMap: HashMap<String, List<Bale>>) {
        mAdapter.updateData(baleHashMap)
    }

    private fun showConfirmDialog(deliveryDtoVegaCotton: VegaCottonGinningDispatchDelivery) {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_dispatch)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (isOnline()) postDispatch(deliveryDtoVegaCotton) else postDeliveryOffline(
                        deliveryDtoVegaCotton.deliveryNumber
                    )
                },
                { dismiss() })
        }
    }


    private fun postDispatch(deliveryDtoVegaCotton: VegaCottonGinningDispatchDelivery) {
        showLoading()
        vm.postDispatch(deliveryDtoVegaCotton)
        vm.postDispatch.observe(viewLifecycleOwner, androidx.lifecycle.Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let {

                            updateUI(it.data)
                        }



                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        UIUtils.showErrorDialog(requireContext(), it.error.toString())
                    }
                }
            }
        })

    }

    private fun updateUI(response: DispatchPostResponse?) {
        if (response?.success!!) {
            deliveryNo?.let { vm.deleteBales(it) }
            moveToSuccess()
        } else {
            Toast.makeText(context, response.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun postDeliveryOffline(deliveryNumber: String) {
        vm.updateDeliverySaveOffline(deliveryNumber)
        moveToSuccess()
    }

    private fun moveToSuccess() {
        val intent = Intent(context, SuccessActivity::class.java)
        if (isOnline()) intent.putExtra(TITLE, getString(R.string.dispatch_completed_success))
        else intent.putExtra(TITLE, getString(R.string.dispatch_completed_offline_success))
        intent.putExtra(SUB_TITLE, getString(R.string.deliver_no_is).plus(deliveryNo))
        context?.startActivity(intent)
    }

}
