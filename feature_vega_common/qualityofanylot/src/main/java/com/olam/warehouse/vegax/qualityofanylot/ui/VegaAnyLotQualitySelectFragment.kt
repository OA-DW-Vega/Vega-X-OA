package com.olam.warehouse.vegax.qualityofanylot.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.qualityofanylot.R
import com.olam.warehouse.vegax.qualityofanylot.databinding.FragmentAnyLotTypeSelectBinding
import com.olam.warehouse.vegax.qualityofanylot.utils.CREATE_QUALITY_LOT
import com.olam.warehouse.vegax.qualityofanylot.utils.TYPE_SELECT
import com.olam.warehouse.vegax.qualityofanylot.utils.VIEW_QUALITY_LOT

class VegaAnyLotQualitySelectFragment:BaseFragment() {

    override val layoutResourceId = R.layout.fragment_any_lot_type_select

    private lateinit var binding:FragmentAnyLotTypeSelectBinding
    private var callBack: CallBack? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding= FragmentAnyLotTypeSelectBinding.inflate(layoutInflater)
        return binding.root
    }

    companion object {
        fun newInstance() = VegaAnyLotQualitySelectFragment().putArgs {
            //  putParcelableArrayList(UIUtils.LOT_DETAIL, lotList)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickEvent()
    }

    private fun clickEvent() {
        binding.llView.setOnClickListener {
            callBack?.replaceFragment(TYPE_SELECT, VIEW_QUALITY_LOT)
        }

        binding.llCreate.setOnClickListener {
            callBack?.replaceFragment(TYPE_SELECT, CREATE_QUALITY_LOT)

        }
    }

    interface CallBack {
        fun replaceFragment(
            fragment: String,type:String
        )

    }


}
