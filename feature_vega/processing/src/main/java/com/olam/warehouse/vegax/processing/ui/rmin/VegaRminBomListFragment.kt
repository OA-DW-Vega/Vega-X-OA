package com.olam.warehouse.vegax.processing.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.entity.VegaProcessingRminBoms
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processing.R
import com.olam.warehouse.vegax.processing.databinding.FragmentVegaRminBomListBinding
import com.olam.warehouse.vegax.processing.databinding.ItemVegaRminBomListBinding
import com.olam.warehouse.vegax.processing.utils.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaRminBomListFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_rmin_bom_list
    private lateinit var binding: FragmentVegaRminBomListBinding
    private var callBack: VegaRminBomListFragment.CallBack? = null
    private var bomListData: ArrayList<VegaProcessingRminBoms>? = null
    private var materialNo: String? = ""
    private var materialName: String? = ""
    private var stageFevor: String? = ""
    private var cfgNumber: String? = ""

    interface CallBack {
        fun replaceFragment(
            stageFevor: String,
            cfgNumber: String,
            materialName: String,
            materialNo: String,
            bom: VegaProcessingRminBoms
        )
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaRminBomListFragment.CallBack
    }
    companion object {
        fun newInstance(
            stageFevor: String,
            cfgNumber: String,
            materialName: String,
            materialNo: String,
            bomList: ArrayList<VegaProcessingRminBoms>?
        ) = VegaRminBomListFragment().putArgs {
            putString(MATERIAL_NAME, materialName)
            putString(MATERIAL_NUMBER, materialNo)
            putString(STAGEFEVOR, stageFevor)
            putString(CFG_NO, cfgNumber)
            putParcelableArrayList(BOM_LIST, bomList)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaRminBomListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processing/ui/rmin/VegaRminBomListFragment").title("Processing").with(tracker)
        initUI()
    }
    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvType, it, false)
        }
        bomListData = arguments?.getParcelableArrayList<VegaProcessingRminBoms>(BOM_LIST)!!
        materialNo = arguments?.getString(MATERIAL_NUMBER)!!
        materialName = arguments?.getString(MATERIAL_NAME)!!
        stageFevor = arguments?.getString(STAGEFEVOR)!!
        cfgNumber = arguments?.getString(CFG_NO)!!
        binding.rvBomList.setUpAdapter(
            bomListData!!,
            R.layout.item_vega_rmin_bom_list,
            ItemVegaRminBomListBinding::inflate,
            { it, pos, bindingItem ->
                bindingItem.tvBomNo.text = it.cfgno.toString()
                bindingItem.tvInputGrade.text = materialName.toString()
                bindingItem.tvOutputGrade.text = it.materialName.toString()
            },
            {

                callBack?.replaceFragment(
                    stageFevor!!,
                    cfgNumber!!,
                    materialName!!,
                    materialNo!!,
                    this
                )
            })

    }

}
