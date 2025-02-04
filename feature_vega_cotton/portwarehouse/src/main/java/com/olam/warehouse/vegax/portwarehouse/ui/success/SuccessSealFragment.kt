package com.olam.warehouse.portwarehouse.ui.success

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.olam.warehouse.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.DISPATCH_TYPE
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.ui.dispatch.PortDispatchBreakSealActivity
import kotlinx.android.synthetic.main.success_fragment.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class SuccessSealFragment : BaseFragment() {

    private var containerNumber: String? = ""
    private var sealNumber: String? = ""
    private var otNumber: String? = ""
    private var isDirect = false
    private var sealTxt: TextView? = null
    private var sealSuccessHed: TextView? = null
    private var btnViewSealed: Button? = null
    override val layoutResourceId = R.layout.success_fragment


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initExtras()
        sealTxt = view.findViewById(R.id.tvIdNumber)
        sealSuccessHed = view.findViewById(R.id.tvSucessTitle)
        btnViewSealed = view.findViewById(R.id.btnViewSealed)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/success/SuccessSealFragment")
            .title("Portwarehouse").with(tracker)
    }

    /*override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.success_fragment, container, false)

        initExtras()
        sealTxt = view.findViewById(R.id.tvIdNumber)
        sealSuccessHed = view.findViewById(R.id.tvSucessTitle)
        btnViewSealed = view.findViewById(R.id.btnViewSealed)
        initUI()
        return view
    }*/

    private fun initExtras() {
        arguments?.let {
            containerNumber = it.getString(PortWHUtil.CONTAINER_ID)
            sealNumber = it.getString(UIUtils.SEAL_ID)
            otNumber = it.getString(PortWHUtil.OT_NUMBER)
            isDirect = it.getBoolean(DISPATCH_TYPE)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initUI() {
        sealSuccessHed!!.text = getString(R.string.success_seal)
        sealTxt?.text = getString(R.string.seal_num).format().plus(" ").plus(sealNumber)
        btnViewSealed?.setOnClickListener { moveToBreakSeal() }
        btnViewSummary.visibility = View.GONE
    }

    private fun moveToBreakSeal() {
        val intent = Intent(activity, PortDispatchBreakSealActivity::class.java)
        intent.putExtra(PortWHUtil.CONTAINER_ID, containerNumber)
        intent.putExtra(UIUtils.SEAL_ID, sealNumber)
        intent.putExtra(PortWHUtil.OT_NUMBER, otNumber)
        intent.putExtra(DISPATCH_TYPE, isDirect)
        startActivity(intent)
    }


}


