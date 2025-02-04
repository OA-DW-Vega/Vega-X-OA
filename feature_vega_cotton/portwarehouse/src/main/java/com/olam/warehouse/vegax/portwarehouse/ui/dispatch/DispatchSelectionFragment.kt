package com.olam.warehouse.vegax.portwarehouse.ui.dispatch

import android.content.Context
import android.os.Bundle
import android.view.View
import com.olam.warehouse.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.fragment_port_dispatch_selection.*

/**
 * Created by Baskaran Kannan on 3/30/2021.
 */
class DispatchSelectionFragment : BaseFragment() {


    private var callBack: CallBack? = null
    override val layoutResourceId = R.layout.fragment_port_dispatch_selection

    companion object {
        fun newInstance() = DispatchSelectionFragment().putArgs {
            //putString(DELIVERY_NO, deliveryNo)
        }
    }

    interface CallBack {
        fun replaceFragment(moveFrag: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    fun initUI() {
        llOtDispatch.setOnClickListener { callBack?.replaceFragment(PortWHUtil.FRAG_OT_DISPATCH) }
        llMtnDispatch.setOnClickListener { callBack?.replaceFragment(PortWHUtil.FRAG_MTN_DISPATCH) }
    }
}
