package com.olam.warehouse.vegax.splitlot.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.master.common.model.VegaCommonSplitLotModel
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.LOT_DETAIL
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.splitlot.R
import com.olam.warehouse.vegax.splitlot.databinding.FragmentSplitLotListBinding
import com.olam.warehouse.vegax.splitlot.databinding.ItemSplitLotSummaryBinding
import com.olam.warehouse.vegax.splitlot.utils.SPLIT_LOT_ENTERED

/**
 * Created by Baskaran Kannan on 9/26/2022.
 */
class VegaCommonSplitLotListFragment: BaseFragment() {
    override val layoutResourceId = R.layout.fragment_split_lot_list
    private lateinit var binding: FragmentSplitLotListBinding
    private var lotList = mutableListOf<VegaCoffeeLot>()
    private var callBack: Callback? = null

    interface Callback {
        fun replaceFragment(SPLIT_LOT_ENTERED: String, lot: VegaCoffeeLot)
    }

    companion object {
        fun newInstance(lotList: ArrayList<VegaCoffeeLot>) = VegaCommonSplitLotListFragment().putArgs {
            putParcelableArrayList(LOT_DETAIL, lotList)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as Callback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSplitLotListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        lotList = arguments?.getParcelableArrayList<VegaCoffeeLot>(UIUtils.LOT_DETAIL)?: ArrayList()
        setUpAdapter()
        binding.btnProceed.setOnClickListener { showSplitLotExitDialog() }
    }

    private fun setUpAdapter() {
        if (lotList.size > 0) {
            binding.tvNoData.gone()
            binding.rvLotList.visible()
        } else {
            binding.rvLotList.gone()
            binding.tvNoData.visible()
        }
        binding.rvLotList.setUpAdapter(
            lotList,
            R.layout.item_split_lot_summary,
            ItemSplitLotSummaryBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvScaleLotValue.text = it.batchNumber
                bindItem.tvScaleWeightValue.text =
                    it.netWeight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitsOfMeasure)
                bindItem.tvStLocationValue.text = it.storageLocationCode
                bindItem.tvScaleGradeValue.text = it.materialName
                if (it.qcStatus.isNullOrEmpty()) {
                    bindItem.tvQcStatusValue.text = getString(R.string.qc_status_pending)
                    bindItem.tvQcStatusValue.setTextColor(
                        ContextCompat.getColor(
                            bindItem.tvQcStatusValue.context,
                            com.olam.warehouse.presentation.R.color.red1
                        )
                    )
                } else {
                    bindItem.tvQcStatusValue.text = context.getString(R.string.qc_status_done)
                    bindItem.tvQcStatusValue.setTextColor(
                        ContextCompat.getColor(
                            bindItem.tvQcStatusValue.context,
                            com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                        )
                    )
                }
            },
            {
                val lot = this
                //if (lot.qcStatus.isNullOrEmpty()) {
                    callBack?.replaceFragment(SPLIT_LOT_ENTERED, lot)
                //}
            })

    }

    private fun showSplitLotExitDialog() {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.login.R.string.split_lot_message)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.login.R.string.proceed),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                { moveToHomePage() },
                { dismiss() })
        }
    }

    private fun moveToHomePage() {
        val intent = Intent(requireContext(), HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        activity?.finish()
    }

    fun getBack() { showSplitLotExitDialog()}
    fun updateStatus( splitLotList: ArrayList<VegaCommonSplitLotModel>) {
        val splitStatus = splitLotList.any { it.batchNumber?.isNotEmpty() == true }
        if(splitStatus){
            this.lotList.forEachIndexed { index, vegaCoffeeLot ->
                if(vegaCoffeeLot.batchNumber.equals(splitLotList.get(0).parentBatchNumber)){
                    this.lotList.get(index).qcStatus = getString(R.string.qc_status_done)
                   // binding.rvLotList.adapter?.notifyItemChanged(index)
                    setUpAdapter()
                }
            }
        }
    }

}
