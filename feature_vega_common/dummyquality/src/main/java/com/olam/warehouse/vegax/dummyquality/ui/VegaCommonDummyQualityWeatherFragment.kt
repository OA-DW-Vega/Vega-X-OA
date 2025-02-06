package com.olam.warehouse.vegax.dummyquality.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.dummyquality.R
import com.olam.warehouse.vegax.dummyquality.databinding.FragmentDummyQualityOptionBinding
import com.olam.warehouse.vegax.dummyquality.utils.DUMMY_QUALITY_CAPTURE
import com.olam.warehouse.vegax.dummyquality.utils.IS_VIEW_QUALITY


class VegaCommonDummyQualityWeatherFragment : BaseFragment() {

    private var callBack: CallBack? = null
    override val layoutResourceId = R.layout.fragment_weather
    private lateinit var binding: FragmentDummyQualityOptionBinding

    companion object {
        fun newInstance() = VegaCommonDummyQualityWeatherFragment().putArgs {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDummyQualityOptionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    fun initUI() {
        binding.linearCaptureQuality.setOnClickListener {
            callBack?.replaceFragment(DUMMY_QUALITY_CAPTURE)
            IS_VIEW_QUALITY = false
        }
        binding.linearViewQuality.setOnClickListener {
            callBack?.replaceFragment(DUMMY_QUALITY_CAPTURE)
            IS_VIEW_QUALITY = true
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    interface CallBack {
        fun replaceFragment(moveFrag: String)
    }
}
