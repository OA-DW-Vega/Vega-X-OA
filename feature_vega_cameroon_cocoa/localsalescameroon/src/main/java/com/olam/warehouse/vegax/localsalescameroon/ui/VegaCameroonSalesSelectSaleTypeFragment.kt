package com.olam.warehouse.vegax.localsalescameroon.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeePendingSalesOrderWithLots
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.localsalescameroon.R
import com.olam.warehouse.vegax.localsalescameroon.databinding.FragmentCameroonSalesTypesBinding
import com.olam.warehouse.vegax.localsalescameroon.utils.SALES_PENDING
import com.olam.warehouse.vegax.localsalescameroon.utils.SALES_TYPE_ANTICIPATED
import com.olam.warehouse.vegax.localsalescameroon.utils.SALES_TYPE_WEIGHBRIDGE
import com.olam.warehouse.vegax.localsalescameroon.utils.SALES_TYPE_WEIGHSCALE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCameroonSalesSelectSaleTypeFragment : BaseFragment() {
    override val layoutResourceId: Int = R.layout.fragment_cameroon_sales_types
    private var callBack: CallBack? = null
    private lateinit var binding: FragmentCameroonSalesTypesBinding
    private val vm: VegaCameroonSalesViewModel by viewModel()

    interface CallBack {
        fun replaceFragment(fragment: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = VegaCameroonSalesSelectSaleTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCameroonSalesTypesBinding.inflate(layoutInflater)


        return binding.root
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("localsalescameroon/ui/VegaCameroonSalesSelectSaleTypeFragment")
            .title("Vega_Cameroon/Local Sales").with(tracker)
    }
    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
        }
        binding.clWeightScale.visibility = View.VISIBLE
        binding.clAnticipated.visibility = View.VISIBLE
        binding.clWeighBridge.setOnClickListener { moveToWeightBridge() }
        binding.clWeightScale.setOnClickListener { moveToWeighScale() }
        binding.clAnticipated.setOnClickListener { moveToAnticipated() }
        vm.dispatchPendingSales.observe(viewLifecycleOwner, Observer { updateUIPendingList(it) })
    }

    private fun moveToWeightBridge() {
        callBack?.replaceFragment(SALES_TYPE_WEIGHBRIDGE)
    }

    private fun moveToWeighScale() {
        vm.getPendingList(SALES_TYPE_WEIGHSCALE)
    }

    private fun updateUIPendingList(it: List<VegaCoffeePendingSalesOrderWithLots>) {
        val list = it.map { it1 -> it1.salesOrder }
        if (list.size > 0) callBack?.replaceFragment(SALES_PENDING)
        else callBack?.replaceFragment(SALES_TYPE_WEIGHSCALE)
    }

    private fun moveToAnticipated() {
        callBack?.replaceFragment(SALES_TYPE_ANTICIPATED)

    }

}
