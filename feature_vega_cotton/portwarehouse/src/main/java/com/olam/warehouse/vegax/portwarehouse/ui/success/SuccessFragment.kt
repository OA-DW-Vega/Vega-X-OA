package com.olam.warehouse.vegax.portwarehouse.ui.success

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.SuccessFragmentBinding
import com.olam.warehouse.vegax.portwarehouse.ui.incoming.offline.PortIncommingMtnOfflineActivity
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.MTN_ID
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class SuccessFragment : BaseFragment() {

    private var otNumber: String? = ""
    private var mtnNumber: String? = ""
    private var materialDoc: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initExtras()
    }

    override val layoutResourceId = R.layout.success_fragment
    private lateinit var binding: SuccessFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SuccessFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/success/SuccessFragment")
            .title("Portwarehouse").with(tracker)
    }

    private fun initExtras() {
        arguments?.let {
            otNumber = it.getString(PortWHUtil.OT_NUMBER)
            materialDoc = it.getString(PortWHUtil.MATERIAL_DOC_ID)
            mtnNumber = it.getString(MTN_ID)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initUI() {

        if (otNumber == null) {
            binding.llMaterialDoc.visible()
            binding.tvMaterialDoc.text = materialDoc ?: ""
            if (materialDoc?.isNotEmpty()!!) {
                binding.llMaterialDoc.visible()
                binding.tvSucessTitle.text = getString(R.string.offloaded_successfully)
                // tvIdNumber.text = "Bales for MTN ($mtnNumber) successfully verified and offloaded"
                binding.tvIdNumber.text = "MTN Number $mtnNumber"
                binding.btnViewSummary.gone()
            } else {
                binding.tvSucessTitle.text = getString(R.string.offline_successfully)
                binding.tvIdNumber.text = "MTN Number $mtnNumber"
                binding.btnViewSummary.visible()
                binding.llMaterialDoc.gone()
                binding.btnViewSummary.setOnClickListener {
                    moveToOfflineSummary()
                }
            }
            binding.btnViewSealed.gone()
        } else {
            binding.llMaterialDoc.gone()
            binding.btnViewSummary.gone()
            binding.tvSucessTitle.text = getString(R.string.success_dispatch)
            binding.tvIdNumber.text = otNumber
            binding.btnViewSealed.gone()
        }

    }

    private fun moveToOfflineSummary() {
        startActivity(Intent(activity, PortIncommingMtnOfflineActivity::class.java))
    }
}

