package com.olam.warehouse.vegax.secretidcommon.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.vegax.secretid.R
import com.olam.warehouse.vegax.secretid.databinding.FragmentCommonSecretidSummaryBinding

class VegaSecretIdCommonSummaryFragment() :BaseFragment() {

    override val layoutResourceId = R.layout.fragment_common_secretid_summary

    private lateinit var binding:FragmentCommonSecretidSummaryBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding= FragmentCommonSecretidSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}
