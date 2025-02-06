package com.olam.warehouse.vegax.qualityindiacoffee.ui.postprocessing

import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.qualityindiacoffee.R
import com.olam.warehouse.vegax.qualityindiacoffee.ui.VegaIndiaCoffeeQualityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaIndiaCoffeeQtyPostProcessParamsFragment : BaseFragment() {

    private val vm: VegaIndiaCoffeeQualityViewModel by viewModel()
    override val layoutResourceId = R.layout.fragment_vega_india_coffee_post_quality_truck_list

    companion object {
        fun newInstance() = VegaIndiaCoffeeQtyPostProcessParamsFragment().putArgs {}
    }

}
