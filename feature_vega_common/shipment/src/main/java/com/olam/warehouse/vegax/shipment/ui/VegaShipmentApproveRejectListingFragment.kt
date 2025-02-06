package com.olam.warehouse.vegax.shipment.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.shipment.R
import com.olam.warehouse.vegax.shipment.databinding.FragmentShipmentListBinding

class VegaShipmentApproveRejectListingFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_shipment_list

    private lateinit var binding: FragmentShipmentListBinding

    companion object {
        fun newInstance() = VegaShipmentApproveRejectListingFragment().putArgs {
            // putParcelableArrayList(UIUtils.LOT_DETAIL, lotList)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentShipmentListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
