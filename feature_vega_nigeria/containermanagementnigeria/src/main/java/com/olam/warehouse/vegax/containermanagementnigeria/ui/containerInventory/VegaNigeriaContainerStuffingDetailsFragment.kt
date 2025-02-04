package com.olam.warehouse.vegax.containermanagementnigeria.ui.containerInventory

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.containermanagementnigeria.R
import com.olam.warehouse.vegax.containermanagementnigeria.data.domain.model.ContainerInventory
import com.olam.warehouse.vegax.containermanagementnigeria.databinding.FragmentVegaNigeriaContainerStuffingDetailsBinding
import com.olam.warehouse.vegax.containermanagementnigeria.utils.CONTAINER_DETAILS
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNigeriaContainerStuffingDetailsFragment : BaseFragment() {
    private lateinit var binding: FragmentVegaNigeriaContainerStuffingDetailsBinding
    private lateinit var mListener: onNavigateListner
    private lateinit var containerDetails: ContainerInventory
    private var mAdapter = VegaNigeriaContainerInventoryStuffedDetailsListAdapter { }

    override val layoutResourceId: Int
        get() = R.layout.fragment_vega_nigeria_container_stuffing_details

    companion object {
        fun newInstance(bundle: Bundle) =
            VegaNigeriaContainerStuffingDetailsFragment().apply {
                putArgs {
                    putBundle("BUNDLE_DATA", bundle)
                }
            }
    }

    interface onNavigateListner{
        fun navigateBackToInventory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVegaNigeriaContainerStuffingDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as onNavigateListner
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("containermanagement/ui/containerInventory/VegaNigeriaContainerStuffingDetailsFragment")
            .title("Vega_Nigeria/Containermanagement").with(tracker)
        initUI()
    }

    private fun initUI() {
        val bundle = arguments?.getBundle("BUNDLE_DATA")
        containerDetails = bundle?.getSerializable(CONTAINER_DETAILS) as ContainerInventory
//        binding.tvTotalLotsValue.text = containerDetails.containerSize
//        binding.tvTotalWeightValue.text =
        updateUI()
        binding.btOk.setOnClickListener {
            mListener.navigateBackToInventory()
        }
    }

    private fun updateUI() {
        binding.rvLotList.layoutManager = LinearLayoutManager(this.context)
        binding.rvLotList.adapter = mAdapter
    }

}
