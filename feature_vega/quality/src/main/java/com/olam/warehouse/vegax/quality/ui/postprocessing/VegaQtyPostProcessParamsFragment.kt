package com.olam.warehouse.vegax.quality.ui.postprocessing

import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.quality.R
import com.olam.warehouse.vegax.quality.ui.VegaQualityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaQtyPostProcessParamsFragment : BaseFragment() {

    private val vm: VegaQualityViewModel by viewModel()
    override val layoutResourceId = R.layout.fragment_vega_post_quality_truck_list

    companion object {
        fun newInstance() = VegaQtyPostProcessParamsFragment().putArgs {}
    }

}
