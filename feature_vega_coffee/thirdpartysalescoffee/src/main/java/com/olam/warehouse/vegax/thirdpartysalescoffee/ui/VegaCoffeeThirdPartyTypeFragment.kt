package com.olam.warehouse.vegax.thirdpartysalescoffee.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.thirdpartysalescoffee.R
import com.olam.warehouse.vegax.thirdpartysalescoffee.databinding.FragmentThirdPartySalesSelectionBinding
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCoffeeThirdPartyTypeFragment : BaseFragment() {

    private lateinit var binding: FragmentThirdPartySalesSelectionBinding
    private var callBack: VegaCoffeeThirdPartyReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeThirdPartyReplaceFragmentCallback
    }

    override val layoutResourceId = R.layout.fragment_third_party_sales_selection

    companion object {
        fun newInstance() = VegaCoffeeThirdPartyTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentThirdPartySalesSelectionBinding.inflate(layoutInflater)
        if (getCurrentKey().split("_")[1].contains("NI")) {
            binding.llOwnershipTransfer.gone()
            binding.tvSecond.text = getString(R.string.tolling_purchase)
            binding.tvThird.text = getString(R.string.tolling_transfer)
        }
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("thirdpartysalescoffee/ui/VegaThirdPartyTypeFragment").title("Third Party Sales")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        binding.llOwnershipTransfer.setOnClickListener { moveToOwnershipTransfer() }
        binding.llThirdPartyOlam.setOnClickListener { moveToThirdPartyOlam() }
        binding.llThirdPartySales.setOnClickListener { movetoSales() }
    }

    private fun moveToOwnershipTransfer() {
        callBack?.replaceFragment(Ownership_Transfer, TP_TO_TP)
    }

    private fun moveToThirdPartyOlam() {
        callBack?.replaceFragment(Third_Party_Olam, TP_TO_OLAM)
    }

    private fun movetoSales() {
        callBack?.replaceFragment(Third_Party_Sales, SAME_TP)
    }

}
