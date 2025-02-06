package com.olam.warehouse.vegax.inventoryghana.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventoryghana.R
import com.olam.warehouse.vegax.inventoryghana.databinding.FragmentVegaGhanaInventoryListBinding

import com.olam.warehouse.vegax.inventoryghana.databinding.FragmentVegaGhanaInventorySelectionFragmentBinding
import com.olam.warehouse.vegax.inventoryghana.di.injectVegaGhanaInventoryFeature
import com.olam.warehouse.vegax.inventoryghana.utils.FILTER_LIST
import com.olam.warehouse.vegax.inventoryghana.utils.INVENTORY_DETAILS
import com.olam.warehouse.vegax.inventoryghana.utils.INVENTORY_LIST

class VegaGhanaInventorySelectionFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_ghana_inventory_selection_fragment
    private var callBack: CallBack? = null
    private lateinit var binding: FragmentVegaGhanaInventorySelectionFragmentBinding
    private var lotId: String = ""

    interface CallBack {
        fun replaceFragment(moveFrag: String, bundle: Bundle)
        fun replaceFilterFragment(bundle: Bundle, fullFilter: ArrayList<String>)
    }

    companion object {
        fun newInstance(bundle: Bundle) = VegaGhanaInventorySelectionFragment().putArgs {
            putBundle("BUNDLE_DATA", bundle)

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaGhanaInventoryFeature()
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }
    override fun onPrepareOptionsMenu(menu: Menu) {
        if (lotId.isNotEmpty()) menu.clear()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaGhanaInventorySelectionFragmentBinding.inflate(layoutInflater)
        return binding.root
    }
    private fun initUI() {
        lotId = arguments?.getBundle("BUNDLE_DATA")?.getString("LOTID", "") ?: ""
        val bundle = Bundle()
        bundle.putString("LOTID", lotId)
        binding.llInventList.setOnClickListener {
             when (lotId?.isNotEmpty()) {
              true ->callBack?.replaceFragment(INVENTORY_DETAILS,bundle)
              else ->callBack?.replaceFragment(INVENTORY_LIST,bundle)
          }
        }
        binding.llInventBalance.setOnClickListener {
            callBack?.replaceFragment("",bundle)
        }
    }

}
