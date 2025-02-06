package com.olam.warehouse.vegax.inventorynicaragua.ui.quality

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.inventorynicaragua.R
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.NicInventory
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.QualityParams
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.VegaNicInventoryStocks
import com.olam.warehouse.vegax.inventorynicaragua.databinding.FragmentVegaNicInventoryQualityBinding
import com.olam.warehouse.vegax.inventorynicaragua.databinding.ItemVegaNicQualityDetailsBinding
import com.olam.warehouse.vegax.inventorynicaragua.ui.VegaNicInventoryViewModel
import com.olam.warehouse.vegax.inventorynicaragua.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 12/16/2020.
 */
class VegaNicInventoryQualityFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_nic_inventory_quality
    private lateinit var binding: FragmentVegaNicInventoryQualityBinding
    private val vm: VegaNicInventoryViewModel by viewModel()
    private var callBack: CallBack? = null
    private var inventoryLots: NicInventory? = null

    interface CallBack {
        fun replaceFragment()
    }

    companion object {
        fun newInstance(inventoryQuality: NicInventory) = VegaNicInventoryQualityFragment().putArgs {
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
        binding = FragmentVegaNicInventoryQualityBinding.inflate(layoutInflater)
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
        binding.tvLotNo.text = inventoryLots?.lotId
        binding.tvOk.setOnClickListener { activity?.onBackPressed() }

        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateUI(it) })

        if (inventoryLots?.qualityParameters?.size ?: 0 > 0)
            setUpAdapter(inventoryLots?.qualityParameters ?: emptyList())
        else
            vm.getLotDetails(
                inventoryLots?.lotId ?: "", inventoryLots?.materialCode ?: "",
                PreferenceHelper.get(Constants.WERKS, "")
            )
    }

    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaNicInventoryStocks>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let {
                        if (it.size > 0) {
                            setUpAdapter(it[0].materialQuality.qualityParameters)
                        }
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun setUpAdapter(qualityParameters: List<QualityParams>) {
        binding.rvQuality.setUpAdapter(
            qualityParameters as MutableList,
            R.layout.item_vega_nic_quality_details,
            ItemVegaNicQualityDetailsBinding::inflate,
            { it, pos, bindItem ->
                if (pos % 2 == 0) {
                    this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                } else {
                    this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lits))
                }
                bindItem.tvQualityNameApprove.text = it.sapQCName
                bindItem.tvUnitApprove.text = it.satNam
            })
    }
}
