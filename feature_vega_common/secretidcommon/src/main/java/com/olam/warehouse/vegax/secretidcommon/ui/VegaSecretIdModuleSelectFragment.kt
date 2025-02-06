package com.olam.warehouse.vegax.secretidcommon.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.secretid.R
import com.olam.warehouse.vegax.secretid.databinding.FragmentSecretidModuleSelectBinding
import com.olam.warehouse.vegax.secretid.databinding.FragmentSelectLotBinding
import com.olam.warehouse.vegax.secretidcommon.utils.MODULE_SELECT
import com.olam.warehouse.vegax.secretidcommon.utils.OFFLOADING_SELECT

class VegaSecretIdModuleSelectFragment() :BaseFragment() {

    override val layoutResourceId = R.layout.fragment_secretid_module_select
    private lateinit var binding: FragmentSecretidModuleSelectBinding
    private var callBack: CallBack? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding= FragmentSecretidModuleSelectBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack

    }
    companion object {
        fun newInstance() = VegaSecretIdModuleSelectFragment().putArgs {
          //  putParcelableArrayList(UIUtils.LOT_DETAIL, lotList)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       // initUi()
        clickEvent()
    }

    private fun clickEvent() {
        binding.llOffloading.setOnClickListener {moveToOffloadingFragment()}
        binding.llInventory.setOnClickListener { moveToSelectLotFragment()  }
    }

   private fun moveToSelectLotFragment(){
        callBack?.replaceFragment(MODULE_SELECT)
    }

    private fun moveToOffloadingFragment(){
        callBack?.replaceFragment(OFFLOADING_SELECT)
    }

    interface CallBack {
        fun replaceFragment(
            fragment: String
        )
    }

}
