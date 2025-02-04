package com.olam.warehouse.vegax.createmapar.ui.pickupstock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.createmapar.R
import com.olam.warehouse.vegax.createmapar.databinding.FragmentLotDetailsBinding
import com.olam.warehouse.vegax.createmapar.ui.ArViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 6/15/2021.
 */
class LotDeatilsFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_lot_details
    private lateinit var binding: FragmentLotDetailsBinding
    private val vm: ArViewModel by viewModel()

    companion object {
        fun newInstance() = LotDeatilsFragment().putArgs {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLotDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {

    }
}