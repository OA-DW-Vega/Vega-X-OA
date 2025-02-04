package com.olam.warehouse.vegax.inventoryindo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventoryindo.R
import com.olam.warehouse.vegax.inventoryindo.data.domain.model.MaterialQuality
import com.olam.warehouse.vegax.inventoryindo.data.domain.model.QualityParams
import com.olam.warehouse.vegax.inventoryindo.data.domain.model.VegaIndoCoffeeInventoryLots
import com.olam.warehouse.vegax.inventoryindo.databinding.FragmentIndoCoffeeInventoryQualityBinding
import com.olam.warehouse.vegax.inventoryindo.utils.getColor
import kotlinx.android.synthetic.main.item_indo_coffee_quality_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
class VegaIndoCoffeeInventoryQualityFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_indo_coffee_inventory_quality
    private lateinit var binding: FragmentIndoCoffeeInventoryQualityBinding
    private val vm: VegaIndoCoffeeInventoryViewModel by viewModel()
    private var callBack: CallBack? = null
    private var inventoryLots = VegaIndoCoffeeInventoryLots()

    interface CallBack {
        fun replaceFragment()
    }

    companion object {
        fun newInstance(inventoryQuality: VegaIndoCoffeeInventoryLots) =
            VegaIndoCoffeeInventoryQualityFragment().putArgs {
                putParcelable(Constants.SELECTED_STOCKS_LIST, inventoryQuality)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentIndoCoffeeInventoryQualityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("inventorycocoa/ui/VegaCocoaInventoryQualityFragment").title("Inventory Cocoa")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        inventoryLots = arguments?.getParcelable(Constants.SELECTED_STOCKS_LIST)!!
        binding.tvLotNo.text = inventoryLots.batchNumber
        binding.tvOk.setOnClickListener { activity?.onBackPressed() }

        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateUI(it) })

        if (inventoryLots.materialQuality.qualityParameters.size > 0)
            setUpAdapter(inventoryLots.materialQuality.qualityParameters)
        else
            vm.fetchQualityDetails(inventoryLots.batchNumber, inventoryLots.materialCode)
    }

    private fun updateUI(data: Resource<GenericReqAndResp<List<MaterialQuality>>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let {
                        if (it.size > 0) {
                            setUpAdapter(it[0].qualityParameters)
                        }
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun setUpAdapter(qualityParameters: List<QualityParams>) {
        binding.rvQuality.setUp(
            qualityParameters as MutableList,
            R.layout.item_indo_coffee_quality_details,
            { it, pos ->
                if (pos % 2 == 0) {
                    this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                } else {
                    this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lits))
                }
                tvQualityNameApprove.text = it.sapQCName
                tvUnitApprove.text = it.satNam
            })
    }
}
