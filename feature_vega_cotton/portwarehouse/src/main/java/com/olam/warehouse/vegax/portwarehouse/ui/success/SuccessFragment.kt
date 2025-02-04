package com.olam.warehouse.portwarehouse.ui.success

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.olam.warehouse.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.MTN_ID
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.ui.incoming.offline.PortIncommingMtnOfflineActivity
import kotlinx.android.synthetic.main.success_fragment.*
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


    /*override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.success_fragment, container, false)
    }*/

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
            llMaterialDoc?.visible()
            tvMaterialDoc?.text = materialDoc ?: ""
            if (materialDoc?.isNotEmpty()!!) {
                llMaterialDoc?.visible()
                tvSucessTitle.text = getString(R.string.offloaded_successfully)
                // tvIdNumber.text = "Bales for MTN ($mtnNumber) successfully verified and offloaded"
                tvIdNumber.text = "MTN Number $mtnNumber"
                btnViewSummary.gone()
            } else {
                tvSucessTitle.text = getString(R.string.offline_successfully)
                tvIdNumber.text = "MTN Number $mtnNumber"
                btnViewSummary.visible()
                llMaterialDoc?.gone()
                btnViewSummary.setOnClickListener {
                    moveToOfflineSummary()
                }
            }
            btnViewSealed.gone()
        } else {
            llMaterialDoc?.gone()
            btnViewSummary?.gone()
            tvSucessTitle.text = getString(R.string.success_dispatch)
            tvIdNumber.text = otNumber
            btnViewSealed.gone()
        }

    }

    private fun moveToOfflineSummary() {
        startActivity(Intent(activity, PortIncommingMtnOfflineActivity::class.java))
    }
}

