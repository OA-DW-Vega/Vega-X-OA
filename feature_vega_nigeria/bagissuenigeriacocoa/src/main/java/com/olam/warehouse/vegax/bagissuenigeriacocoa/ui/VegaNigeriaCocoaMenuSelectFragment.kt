package com.olam.warehouse.vegax.bagissuenigeriacocoa.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.bagissuenigeriacocoa.R
import com.olam.warehouse.vegax.bagissuenigeriacocoa.databinding.FragmentNigeriaCocoaMenuTypeBinding
import com.olam.warehouse.vegax.bagissuenigeriacocoa.databinding.FragmentVegaNigeriaCocoaBagIssueBinding
import com.olam.warehouse.vegax.bagissuenigeriacocoa.utils.BAG_ISSUE_FRAG
import com.olam.warehouse.vegax.bagissuenigeriacocoa.utils.BAG_RETN_FRAG
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaNigeriaCocoaMenuSelectFragment : BaseFragment() {

    private val vm: VegaNigeriaCocoaBagIssueViewModel by viewModel()
    private lateinit var binding: FragmentNigeriaCocoaMenuTypeBinding
    override val layoutResourceId = R.layout.fragment_nigeria_cocoa_menu_type
    private var callBack: VegaNigeriaCocoaBagMgmtCallBackListener? = null


    companion object {
        fun newInstance() = VegaNigeriaCocoaMenuSelectFragment().putArgs {

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as VegaNigeriaCocoaBagMgmtCallBackListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNigeriaCocoaMenuTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding.llBagIssue.setOnClickListener { moveToBagIssueFragment() }
        binding.llBagRetn.setOnClickListener { moveToBagRetnFragment() }

        if(getCurrentKey().contains("VEGA_NG") && getCurrentKey().contains("COCO")){
            binding.llBagRetn.visible()
        }
    }

    private fun moveToBagIssueFragment() {
        callBack?.replaceFragment(BAG_ISSUE_FRAG, BAG_ISSUE_FRAG)
    }

    private fun moveToBagRetnFragment() {
        callBack?.replaceFragment(BAG_RETN_FRAG, BAG_RETN_FRAG)
    }
}
