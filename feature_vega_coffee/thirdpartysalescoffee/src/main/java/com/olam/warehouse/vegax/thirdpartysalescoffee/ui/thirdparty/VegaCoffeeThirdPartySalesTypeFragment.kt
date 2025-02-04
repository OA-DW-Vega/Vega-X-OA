package com.olam.warehouse.vegax.thirdpartysalescoffee.ui.thirdparty

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.thirdpartysalescoffee.R
import com.olam.warehouse.vegax.thirdpartysalescoffee.databinding.FragmentThirdPartySalesSelectionBinding
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCoffeeThirdPartyReplaceFragmentCallback
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.SAME_TP
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.TP_WB_WS
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.TP_WS
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCoffeeThirdPartySalesTypeFragment : BaseFragment() {

    private val mTAG = VegaCoffeeThirdPartySalesTypeFragment::class.java.canonicalName
    private lateinit var binding: FragmentThirdPartySalesSelectionBinding
    private var callBack: VegaCoffeeThirdPartyReplaceFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeThirdPartyReplaceFragmentCallback
    }

    override val layoutResourceId = R.layout.fragment_third_party_sales_selection

    companion object {
        fun newInstance() = VegaCoffeeThirdPartySalesTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentThirdPartySalesSelectionBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("thirdpartysalescoffee/ui/VegaCoffeeThirdPartySalesTypeFragment")
            .title("Third Party Sales")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        binding.tvTitle.text = getString(R.string.third_party_transfer)
        binding.tvFirst.text = getString(R.string.weighbridge)
        binding.tvSecond.text = getString(R.string.weighscale)
        binding.llOwnershipTransfer.setOnClickListener { moveToOwnershipTransfer() }
        binding.llThirdPartyOlam.setOnClickListener { moveToThirdPartyOlam() }
        binding.llThirdPartySales.visibility = View.GONE
    }

    private fun moveToOwnershipTransfer() {
        callBack?.replaceFragment(TP_WB_WS, SAME_TP)
    }

    private fun moveToThirdPartyOlam() {
        callBack?.replaceFragment(TP_WS, SAME_TP)
    }

}
