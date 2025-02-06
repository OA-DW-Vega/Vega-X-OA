package com.olam.warehouse.vegax.portwarehouse.ui.dispatch

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.FragmentPortDispatchSelectionBinding
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil

/**
 * Created by Baskaran Kannan on 3/30/2021.
 */
class DispatchSelectionFragment : BaseFragment() {


    private var callBack: CallBack? = null
    override val layoutResourceId = R.layout.fragment_port_dispatch_selection
    private lateinit var binding: FragmentPortDispatchSelectionBinding

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPortDispatchSelectionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    fun initUI() {
        binding.llOtDispatch.setOnClickListener { callBack?.replaceFragment(PortWHUtil.FRAG_OT_DISPATCH) }
        binding.llMtnDispatch.setOnClickListener { callBack?.replaceFragment(PortWHUtil.FRAG_MTN_DISPATCH) }
    }
}
